 package beamscheduling;
 
 import java.awt.geom.Point2D;
 
 import java.util.*;
 import org.apache.commons.collections15.Factory;
 
 import edu.uci.ics.jung.algorithms.generators.GraphGenerator;
 import edu.uci.ics.jung.graph.Graph;
 
 public class NetworkGenerator<V,E> implements GraphGenerator<V,E> {
     public int numRelays;
     public int numSubscribers;
     public double width;
     public double height;
     public Random random;
     public Factory<Network<V,E>> networkFactory;
     public Factory<V> vertexFactory;
     public Factory<E> edgeFactory;
 
     /**
      * Creates an instance with the specified factories and specifications.
      * @param networkFactory the factory to use to generate the graph
      * @param vertexFactory the factory to use to create vertices
      * @param edgeFactory the factory to use to create edges
      * @param numRelays the number of vertices for the generated graph
      * @param width the width of the area the network covers
      * @param height the height of the are the network covers
      */
     public NetworkGenerator(Factory<Network<V,E>> networkFactory,
                             Factory<V> vertexFactory, Factory<E> edgeFactory, 
                             int numRelays, int numSubscribers, 
                             double width, double height) {
     	this.networkFactory = networkFactory;
     	this.vertexFactory = vertexFactory;
     	this.edgeFactory = edgeFactory;
         this.numRelays = numRelays;
         this.numSubscribers = numSubscribers;
         this.width = width;
         this.height = height;
     }
 
     public Network<V,E> create() {
         Network<V,E> network = null;
         network = this.networkFactory.create();
         network.relays = new HashSet(numRelays);
         network.subscribers = new HashSet(numSubscribers);
 
         network.relayList = new Vertex[numRelays];
         for(int i=0; i<numRelays; i++) {
             V node = vertexFactory.create();
             Vertex v = (Vertex)node;
             network.addVertex(node);
             network.relays.add(v);
             network.relayList[i] = v;
         }
 
         network.subList = new Vertex[numSubscribers];
         for(int i = 0; i < numSubscribers; i++) {
             V node = vertexFactory.create();
             Vertex v = (Vertex)node;
             v.type = 2;
             // Mark this vertex as a client.
             network.addVertex(node);
             network.subscribers.add(v);
             network.subList[i] = v;
         }
 
         for(Vertex v1: network.relayList) {
             for(Vertex v2: network.relayList) {
                 if (v1 != v2 && network.findEdge((V)v1, (V)v2) == null
                     && random.nextInt(4) == 2) {
                     double dist = Point.roundTwoDecimals(v1.location.distance(v2.location));
                     // Check for connectivity & throughput
                     E edge = edgeFactory.create();
                     ((Edge)edge).length = v1.calculateThroughput(1, v2); //dist;
                     ((Edge)edge).capacity = v1.calculateThroughput(1, v2);
                     network.addEdge(edge, (V)v1, (V)v2);
                 }
             }
         }
 
         for(Vertex v1: network.subList) {
            double dist = 100000000.0;
             Vertex best = null;
             for(Vertex v2: network.relayList) {
                double d = v1.location.distance(v2.location);
                if (d < dist) {
                    dist = d;
                     best = v2;
                 }
             }
             E edge = edgeFactory.create();
            ((Edge)edge).length = dist;
            ((Edge)edge).capacity = v1.calculateThroughput(1, best);
            System.out.println(v1 + " : " + best);
             network.addEdge(edge, (V)v1, (V)best);
         }
 
         network.random = random;
         return network;
     }
 
     public Network<V,E> createCenteredRadialTree() {
         Network<V,E> network = null;
         network = this.networkFactory.create();
         network.relays = new HashSet(numRelays);
         network.subscribers = new HashSet(numSubscribers);
 
         // Create the root at the center
         V root = vertexFactory.create();
         network.addVertex(root);
         Vertex center = (Vertex)root;
         network.gateway = center;
         center.type = 0;
         center.location.setLocation(network.width/2, network.height/2);
         double max_radius = center.calculateRange(center.sectors);
 
         // Create the rest of the nodes
         network.relayList = new Vertex[numRelays];
         for(int i=0; i<numRelays; i++) {
             V node = vertexFactory.create();
             Vertex n = (Vertex)node;
             n.type = 1;
             // changed by Brendan so that 0 and numRelays-1 don't overlap
             double theta = (i * 360.0 / numRelays * Math.PI ) / 180.0; 
             double radius = random.nextDouble() * max_radius;
             n.location.setLocation(center.location.getX() + (radius * Math.cos(theta)), 
                                    center.location.getY() + (radius * Math.sin(theta)));
             network.addVertex(node);
             network.relays.add(n);
             network.relayList[i] = n;
         }
 
         // wire up the rest of the network
         for(V vertex: network.getVertices()) {
             if (vertex != root && network.findEdge(root,vertex) == null) {
                 double dist = Point.roundTwoDecimals(((Vertex)root).location.distance(((Vertex)vertex).location));
                 // Check for connectivity & throughput
                 E edge = edgeFactory.create();
                 ((Edge)edge).type = 0;
                 ((Edge)edge).length = dist;
                 network.addEdge(edge, root, vertex);
             }
         }
 
         network.subList = new Vertex[numSubscribers];
         for(int i = 0; i < numSubscribers; i++) {
             V node = vertexFactory.create();
             Vertex v = (Vertex)node;
             v.type = 2;
             // Mark this vertex as a client.
             network.addVertex(node);
             network.subscribers.add(v);
             network.subList[i] = v;
         }
         network.random = random;
         return network;
     }
 
     /**
      * Sets the seed for the random number generator.
      * @param seed input to the random number generator.
      */
     public void setSeed(long seed) {
         random = new Random(seed);
         ((VertexFactory)vertexFactory).setSeed(seed);
     }
 }
