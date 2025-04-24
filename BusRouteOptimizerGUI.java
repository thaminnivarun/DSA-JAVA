import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class BusRoute {
    String destination;
    int price;
    int travel_time;
    BusRoute next;

    public BusRoute(String destination, int price, int travel_time) {
        this.destination = destination;
        this.price = price;
        this.travel_time = travel_time;
        this.next = null;
    }
}

class City {
    String name;
    BusRoute routes;

    public City(String name) {
        this.name = name;
        this.routes = null;
    }

    public void add_route(String destination, int price, int travel_time) {
        BusRoute new_route = new BusRoute(destination, price, travel_time);
        if (this.routes == null) {
            this.routes = new_route;
        } else {
            BusRoute temp = this.routes;
            while (temp.next != null) {
                temp = temp.next;
            }
            temp.next = new_route;
        }
    }
}

public class BusRouteOptimizerGUI {
    private static HashMap<String, City> cities = new HashMap<String, City>(){{
        put("Coimbatore", new City("Coimbatore"));
        put("Palakkad", new City("Palakkad"));
        put("Madurai", new City("Madurai"));
        put("Chennai", new City("Chennai"));
        put("Bangalore", new City("Bangalore"));
        put("Hyderabad", new City("Hyderabad"));
        put("Pune", new City("Pune"));
        put("Mumbai", new City("Mumbai"));
        put("Kochi", new City("Kochi"));
    }};

    private static Random randomObj = new Random();

    static class Pair {
        String src;
        String dest;

        public Pair(String src, String dest) {
            this.src = src;
            this.dest = dest;
        }
    }

    static class RouteInfo {
        String current_city;
        List<Pair> path;
        int total_price;
        int total_time;

        public RouteInfo(String current_city, List<Pair> path, int total_price, int total_time) {
            this.current_city = current_city;
            this.path = path;
            this.total_price = total_price;
            this.total_time = total_time;
        }
    }

    public static void initialize_routes() {
        Object[][] route_data = {
            {"Coimbatore", "Palakkad", 200, 60},
            {"Coimbatore", "Madurai", 400, 240},
            {"Coimbatore", "Kochi", 350, 150},
            {"Coimbatore", "Chennai", 550, 360},
            {"Palakkad", "Kochi", 350, 150},
            {"Madurai", "Chennai", 550, 360},
            {"Chennai", "Bangalore", 650, 330},
            {"Bangalore", "Hyderabad", 750, 450},
            {"Hyderabad", "Pune", 850, 450},
            {"Pune", "Mumbai", 350, 150}
        };

        for (Object[] data : route_data) {
            String start = (String)data[0];
            String destination = (String)data[1];
            int price = (Integer)data[2];
            int time = (Integer)data[3];
            cities.get(start).add_route(destination, price, time);
        }
    }

    public static BusRoute apply_traffic_conditions(BusRoute route) {
        double traffic_multiplier = 1.0 + randomObj.nextDouble() * 0.5;
        int adjusted_time = (int)(route.travel_time * traffic_multiplier);
        int adjusted_price = (traffic_multiplier <= 1.2) ? route.price : (int)(route.price * 1.2);
        return new BusRoute(route.destination, adjusted_price, adjusted_time);
    }

    public static String find_best_route(String start, String destination) {
        if (!cities.containsKey(start) || !cities.containsKey(destination)) {
            return "❌ Invalid city selection. Please select valid cities.";
        }

        Set<String> visited = new HashSet<>();
        Queue<RouteInfo> routes_to_explore = new LinkedList<>();
        routes_to_explore.offer(new RouteInfo(start, new ArrayList<Pair>(), 0, 0));
        List<Pair> best_path = null;
        int best_time = Integer.MAX_VALUE;
        int best_price = Integer.MAX_VALUE;

        while (!routes_to_explore.isEmpty()) {
            RouteInfo currentInfo = routes_to_explore.poll();
            String current_city = currentInfo.current_city;
            List<Pair> path = currentInfo.path;
            int total_price = currentInfo.total_price;
            int total_time = currentInfo.total_time;

            if (visited.contains(current_city)) {
                continue;
            }
            visited.add(current_city);

            BusRoute route_node = cities.get(current_city).routes;
            while (route_node != null) {
                BusRoute adjusted_route = apply_traffic_conditions(route_node);
                List<Pair> new_path = new ArrayList<>(path);
                new_path.add(new Pair(current_city, adjusted_route.destination));
                int new_price = total_price + adjusted_route.price;
                int new_time = total_time + adjusted_route.travel_time;

                if (adjusted_route.destination.equals(destination)) {
                    if (new_time < best_time) {
                        best_time = new_time;
                        best_price = new_price;
                        best_path = new_path;
                    }
                } else {
                    routes_to_explore.offer(new RouteInfo(adjusted_route.destination, new_path, new_price, new_time));
                }
                route_node = route_node.next;
            }
        }

        if (best_path != null) {
            StringBuilder result = new StringBuilder("\n🚍 Best Route Found:\n");
            for (Pair pair : best_path) {
                result.append("   ").append(pair.src).append(" → ").append(pair.dest).append("\n");
            }
            int hrs = best_time / 60;
            int mins = best_time % 60;
            result.append("   ⏳ Estimated Travel Time: ").append(hrs).append(" hrs ").append(mins).append(" min\n");
            result.append("   💰 Total Ticket Price: ₹").append(best_price);
            return result.toString();
        } else {
            return "\n❌ No route found from " + start + " to " + destination + ".";
        }
    }

    public static void main(String[] args) {
        initialize_routes();
        
        JFrame frame = new JFrame("Bus Route Optimizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel startLabel = new JLabel("Starting City:");
        JComboBox<String> startCombo = new JComboBox<>(cities.keySet().toArray(new String[0]));
        JLabel destLabel = new JLabel("Destination City:");
        JComboBox<String> destCombo = new JComboBox<>(cities.keySet().toArray(new String[0]));
        JButton findButton = new JButton("Find Best Route");

        inputPanel.add(startLabel);
        inputPanel.add(startCombo);
        inputPanel.add(destLabel);
        inputPanel.add(destCombo);
        inputPanel.add(new JLabel()); // Empty cell
        inputPanel.add(findButton);

        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(resultArea);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        findButton.addActionListener(e -> {
            String start = (String) startCombo.getSelectedItem();
            String destination = (String) destCombo.getSelectedItem();
            resultArea.setText(find_best_route(start, destination));
        });

        frame.setVisible(true);
    }
}