 package beamscheduling;
 
 import java.util.*;
 import java.io.IOException;
 
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Level;
 
 import org.apache.commons.collections15.Transformer;
 import org.apache.commons.lang.StringUtils;
 
 import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
 import edu.uci.ics.jung.algorithms.shortestpath.PrimMinimumSpanningTree;
 import edu.uci.ics.jung.graph.util.Pair;
 import edu.uci.ics.jung.graph.Graph;
 
 // for dijkstra the weights can just be the physical link distance 
 // (assuming there is at least one available channel on the link). 
 // basically we should assign random channels to each link, 
 // then place primary users (removing some channels), any link 
 // that still has >= 1 channel survives, then we run routing algorithms 
 // to get paths, then channel selection on each path to determine the 
 // end-to-end quality
 public class Rcs {
 
     static Logger logger = Logger.getLogger("RoutingChannelSelection");
 
     public static int dfsPath(Graph network, Vertex source,
             Vertex destination, String prefix,
             List<Edge> path) {
         //System.out.println(prefix + source);
 
         for (Object o : network.getNeighbors(source)) {
             Vertex v = (Vertex) o;
             Edge e = (Edge) network.findEdge(source, v);
 
             if (e == null) {
                 return (0);
             }
 
             if (path.size() > 0) {
                 return (1);
             }
 
             // Found the destination, add it to the result triggering
             // return path collection
             if (v == destination) {
                 System.out.println("-Adding e: " + e);
                 path.add(e);
                 //System.out.println(prefix+"+"+destination);
                 return (1);
             } else if (!e.isMarked) {
                 e.isMarked = true;
                 dfsPath(network, v, destination, prefix + "|", path);
                 // On the way back out
                 if (path.size() > 0) {
                     System.out.println("+Adding e: " + e);
                     path.add(e);
                     return (1);
                 }
             }
         }
         return (1);
     }
 
     public static void main(String[] args) {
         HashMap subscribers;
         NetworkGenerator networkGenerator;
         Network network;
         RcsOptions options = new RcsOptions();
         CmdLineParser parser = new CmdLineParser(options);
         parser.setUsageWidth(80);
 
         BasicConfigurator.configure();
         logger.setLevel(Level.DEBUG);
 
         try {
             parser.parseArgument(args);
         } catch (CmdLineException e) {
             logger.error("Failed to parse command line arguments.");
             logger.error(e.getMessage());
             parser.printUsage(System.err);
             System.exit(1);
         }
 
         // Handle options that matter
         System.out.println("Random Seed: " + options.seed);
         networkGenerator = Network.getGenerator(options.relays,
                 options.subscribers,
                 options.width, options.height,
                 options.seed, options.channels);
         network = networkGenerator.create();
 
         Transformer<Edge, Double> wtTransformer = new Transformer<Edge, Double>() {
 
             public Double transform(Edge e) {
                 if (e.capacity > 0.0) {
                     return e.length;
                 } else {
                     return Double.MAX_VALUE;
                 }
             }
         };
 
         Transformer<Edge, Double> pTransformer = new Transformer<Edge, Double>() {
 
             public Double transform(Edge e) {
                 return e.bottleNeckWeight();
             }
         };
 
         Vertex source = network.randomRelay();
         Vertex destination = network.randomRelay();
         while (source == destination) {
             destination = network.randomRelay();
         }
 
         source.type = 3;
         destination.type = 4;
 
         if (options.verbose) {
             System.out.println("Source: " + source 
                                + " Destination: " + destination);
         }
 
         // Find dmax and dmin
         double dmin = Double.MAX_VALUE, dmax = Double.MIN_VALUE;
         for(Object e1: network.getEdges()){
             Pair<Object> ends = network.getEndpoints(e1);
             Vertex a = (Vertex)ends.getFirst();
             Vertex b = (Vertex)ends.getSecond();
             double ad = (a.distanceTo(source) + b.distanceTo(destination)) /2.0;
             if (ad < dmin) { dmin = ad; }
             if (ad > dmax) { dmax = ad; }
         }
 
         // Compute weights for the edges
         for(Object e1: network.getEdges()) {
             Edge e = (Edge)e1;
             Pair<Object> ends = network.getEndpoints(e1);
             Vertex a = (Vertex)ends.getFirst();
             Vertex b = (Vertex)ends.getSecond();
             double d = (a.distanceTo(source) + b.distanceTo(destination)) /2.0;
             e.weight = (1.0 + (dmax - d)/(dmax - dmin)) / 2.0;
             if (options.verbose) {
                 System.out.println("Edge: " + e.id + " W: " + e.weight);
             }
         }
 
         if (options.verbose) {
             System.out.println("S: " + source + " D: " + destination);
         }
 
         DijkstraShortestPath<Vertex, Edge> dsp = new DijkstraShortestPath(network, wtTransformer, false);
         List<Edge> dpath = dsp.getPath(source, destination);
         System.out.println("Dijkstra Path");
         System.out.println(dpath.toString());
         for (Edge e : dpath) {
             e.type = 1;
         }
 
        ChannelSelection cs = new ChannelSelection(network);
        double dijkstraThpt = cs.selectChannels(dpath);
 
         PrimMinimumSpanningTree psp = new PrimMinimumSpanningTree(networkGenerator.networkFactory, pTransformer);
         Graph primTree = psp.transform(network);
         if (options.verbose) {
             System.out.println("Prim Tree");
             System.out.println(primTree.toString());
         }
         for (Object e : primTree.getEdges()) {
             ((Edge) e).type = 2;
         }
 
         // Clear out markings
         for (Object v : primTree.getVertices()) {
             ((Vertex) v).isMarked = false;
         }
         for (Object e : primTree.getEdges()) {
             ((Edge) e).isMarked = false;
         }
 
         // Internal implementation - Not Used IRJ - 2011
         //List<Edge> p = new ArrayList<Edge>();
         //dfsPath(primTree, source, destination, "", p);
  
        DijkstraShortestPath<Vertex, Edge> dsp2 = new DijkstraShortestPath(primTree, wtTransformer, false);
         List<Edge> p = dsp2.getPath(source, destination);
 
         for (Edge e : p) {
             e.type = 4;
             if (options.verbose) {
                 Pair<Edge> ends = primTree.getEndpoints(e);
                 System.out.println("Painting Edge Red: " + e 
                                    + "[" + ends.getFirst() + ","
                                    + ends.getSecond() + "]");
             }
         }
         System.out.println("Prim Path");
         System.out.println(p.toString());
 
         double primThpt = cs.selectChannels(p);
 
         if(options.display) {
             network.draw(1024, 768, 
                          "Routing and Channel Selection Application");
         }
 
         System.out.println("Seed, Width, Height, Nodes, Users, Channels, Dijkstra, Prim");
         System.out.println(options.seed + ", " + options.width + ", " + options.height + ", " + options.relays + ", " + options.subscribers + ", " + options.channels + ", " + dijkstraThpt + ", " + primThpt);
 
         if(options.display) { network.jf.repaint(); }
     }
 }
