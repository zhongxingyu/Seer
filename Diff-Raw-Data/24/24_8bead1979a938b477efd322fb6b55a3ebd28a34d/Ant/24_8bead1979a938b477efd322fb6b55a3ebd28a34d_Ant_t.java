 package logic;
 
 import data.Edge;
 import data.EdgeList;
 import data.Graph;
 import data.Node;
 import data.tree.Trie;
 import logic.search.Vnd;
 
 import java.util.Collections;
 import java.util.HashMap;
 
 public class Ant extends Thread {
     private int id;
 
     private Aco aco;
     private Graph graph;
     private Trie tree;
 
     public Ant(int id, Aco aco, Graph graph) {
         this.id = id;
         this.aco = aco;
         this.graph = graph;
         tree = new Trie();
     }
 
     public void run() {
         System.out.println("START CON");
         construct_broadcast_tree();
         System.out.println("START LS");
         local_search();
 
         this.aco.ant_done(this);
     }
 
     private void local_search() {
         Sweep s = new Sweep();
         tree = s.run(tree, 3);
 
         Vnd v = new Vnd();
         tree = v.run(tree);
     }
 
     private Edge find_edge(EdgeList nh) {
         int next = Utility.get_random_int(100);
         double t = 0;
 
         for (Edge edge : nh) {
             if (t + edge.getProbability() > next) {
                 return edge;
             }
             t += edge.getProbability();
         }
         return null;
     }
 
     /* adds implicit edges? */
     private void add_edges(EdgeList nh, Edge _edge) {
         if (_edge == null) {
             return;
         }
 
         for (Edge edge : nh) {
             if (_edge.getStart() == edge.getStart() &&
                     edge.cost() < _edge.cost() && !tree.contains_node(edge.getEnd())) {
                 tree.insert(edge);
             }
         }
         tree.insert(_edge);
     }
 
     private EdgeList calculate_probabilities_for_nh(EdgeList neighborhood) {
         HashMap<Edge, Double> edge_costs = calculate_edge_costs(neighborhood);
         double total = sum_costs(edge_costs);
 
         for (Edge e : neighborhood) {
             e.setProbability(calculate_probability_for_edge(e, edge_costs, total));
         }
 
         return neighborhood;
     }
 
     private double sum_costs(HashMap<Edge, Double> edge_costs) {
         double cost = 0;
 
         for (double v : edge_costs.values()) {
             cost += v;
         }
 
         return cost;
     }
 
     private HashMap<Edge, Double> calculate_edge_costs(EdgeList neighborhood) {
         HashMap<Edge, Double> hm = new HashMap<Edge, Double>();
 
         for (Edge e : neighborhood) {
             hm.put(e, Math.pow(graph.get_pheromone_for_edge(e), graph.getAlpha()) * Math.pow(e.cost(), graph.getBeta()));
         }
 
         return hm;
     }
 
     /*  - total are the full costs of the whole neighborhood */
     private double calculate_probability_for_edge(Edge e, HashMap<Edge, Double> edge_costs, double total) {
         return (edge_costs.get(e) * 100) / total;
     }
 
     private EdgeList get_neighborhood() {
         EdgeList el = new EdgeList();
 
         for (Node node : tree.getNodes()) {
             EdgeList edges = new EdgeList();
 
             for (Edge e : graph.getEdges()) {
                 if (e.getStart() != node || tree.contains_node(e.getEnd())) {
                     continue;
                 }
 
                 edges.add(e);
             }
 
             Collections.sort(edges);
             add_best_edges_to_nh(el, edges);
         }
 
         return el;
     }
 
     private void add_best_edges_to_nh(EdgeList el, EdgeList edges) {
         for (int i = 0; i < (int) Math.ceil(edges.size() * 0.2); i++) {
             el.add(edges.get(i));
         }
     }
 
     private void construct_broadcast_tree() {
         tree.insert(graph.start_node());
 
         while (!tree.valid(graph.size())) {
//            System.out.println("N" + id + " :" + tree.size());
             EdgeList nh = calculate_probabilities_for_nh(get_neighborhood());
             Collections.sort(nh, new EdgeCostSorter());
             add_edges(nh, find_edge(nh));
         }
     }
 
     public Trie getTree() {
         return tree;
     }
 }
