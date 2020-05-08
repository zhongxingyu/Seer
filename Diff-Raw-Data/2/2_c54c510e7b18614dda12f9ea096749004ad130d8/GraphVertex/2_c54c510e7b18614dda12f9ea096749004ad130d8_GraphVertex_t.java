 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package communitydetection;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.apache.hadoop.io.ArrayWritable;
 import org.apache.hadoop.io.MapWritable;
 import org.apache.hadoop.io.NullWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hama.graph.Edge;
 import org.apache.hama.graph.Vertex;
 
 /**
  *
  * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
  */
 public class GraphVertex extends Vertex<Text, NullWritable, MapWritable> {
 
     Set<String> Nr = new HashSet<String>(); // The remaining neighboors
     Set<String> Ni = new HashSet<String>(); // The neighboors to be insterted
     Set<String> Nd = new HashSet<String>(); // The neighboors to be deleted
     // The propinquity value map
     Map<String, Integer> P = new HashMap<String, Integer>();
     
     //cutting thresshold
     int a = 1;
     
     //emerging value
     int b = 3;
     
     private void initialize(Iterator<MapWritable> messages) throws IOException {
         List<Edge<Text, NullWritable>> neighboors;
         neighboors = this.getEdges();
 
         MapWritable outMsg = new MapWritable();
 
         if (this.getSuperstepCount() == 0) {
             outMsg = new MapWritable();
             outMsg.put(new Text("init"), this.getVertexID());
             this.sendMessageToNeighbors(outMsg);
         } else if (this.getSuperstepCount() == 1) {
             while (messages.hasNext()) {
                 Text id = (Text) messages.next().get(new Text("init"));
                 Edge<Text, NullWritable> e = new Edge<Text, NullWritable>(id, null);
                 if (!this.getEdges().contains(e)) {
                     this.addEdge(e);
                 }
             }
         } // for the first superstep, initialize the Nr Set by adding all the 
         // neighboors of the current vertex and send the Set to all neighboors
         else if (this.getSuperstepCount() == 2) {
 
             System.out.println("### SUPERSTEP 1 ###");
             for (Edge<Text, NullWritable> edge : neighboors) {
                 Nr.add(edge.getDestinationVertexID().toString());
             }
 
             System.out.println("neighboors of " + this.getVertexID() + " are: " + Nr);
 
             outMsg.put(new Text("Nr"), new ArrayWritable(Nr.toArray(new String[0])));
 
             this.sendMessageToNeighbors(outMsg);
             //voteToHalt();
         } // for the second superstep, read the messages and initialize the
         // propinquity hash map. Then send the Nr list only to one vertex
         // of each vertex pair.
         else if (this.getSuperstepCount() == 3) {
 
             System.out.println("### SUPERSTEP 2 ###");
 
             while (messages.hasNext()) {
                 ArrayWritable incoming = (ArrayWritable) messages.next().get(new Text("Nr"));
                 List<String> Nr_neighboors = Arrays.asList(incoming.toStrings());
                 for (String neighboor : Nr_neighboors) {
                     if (!neighboor.equals(this.getVertexID().toString())) {
                         P.put(neighboor, 1);
                     }
                 }
             }
 
             System.out.println("Hash for: " + this.getVertexID() + " -> " + P);
 
             for (Edge<Text, NullWritable> edge : neighboors) {
                 String neighboor = edge.getDestinationVertexID().toString();
                 if (Integer.parseInt(neighboor) > Integer.parseInt(this.getVertexID().toString())) {
                     outMsg.put(new Text("Nr"), new ArrayWritable(Nr.toArray(new String[0])));
                     this.sendMessage(edge.getDestinationVertexID(), outMsg);
                 }
             }
             //voteToHalt();
         } // read the messages and find the intersection of the message list and
         // local Nr Set.
         else if (this.getSuperstepCount() == 4) {
 
             System.out.println("### SUPERSTEP 3 ###");
 
             List<String> Nr_neighboors;
             Set<String> intersection = null;
             while (messages.hasNext()) {
                 ArrayWritable incoming = (ArrayWritable) messages.next().get(new Text("Nr"));
                 Nr_neighboors = Arrays.asList(incoming.toStrings());
                 System.out.println(this.getVertexID() + "-> Message received: " + Nr_neighboors);
 
                 boolean Nr1IsLarger = Nr.size() > Nr_neighboors.size();
                 intersection = new HashSet<String>(Nr1IsLarger ? Nr_neighboors : Nr);
                 intersection.retainAll(Nr1IsLarger ? Nr : Nr_neighboors);
                 System.out.println("Intersection of " + Nr + " and " + Nr_neighboors + ": " + intersection);
                 System.out.println("");
             }
             if (intersection != null) {
                 for (String vertex : intersection) {
                     System.out.print("destination vertex: " + vertex);
                     Set<String> messageList = new HashSet<String>(intersection);
                     System.out.print(", message list to send: " + messageList);
                     messageList.remove(vertex);
                     System.out.print(", after removal: " + messageList);
 
                     if (!messageList.isEmpty()) {
                         ArrayWritable aw = new ArrayWritable(messageList.toArray(new String[0]));
                         outMsg = new MapWritable();
                         outMsg.put(new Text("Intersection"), aw);
                         this.sendMessage(new Text(vertex), outMsg);
                         //System.out.println("message sent: " + Arrays.asList(outMsg.get(new Text("Intersection")).toStrings()));
                     }
                     System.out.println("");
                 }
             }
         } // update the conjugate propinquity
         else if (this.getSuperstepCount() == 5) {
 
             System.out.println("### SUPERSTEP 4 ###");
 
             System.out.println("BEFORE Hash for: " + this.getVertexID() + " -> " + P);
             while (messages.hasNext()) {
                 ArrayWritable incoming = (ArrayWritable) messages.next().get(new Text("Intersection"));
                 List<String> Nr_neighboors = Arrays.asList(incoming.toStrings());
                 System.out.println("");
                 System.out.println(this.getVertexID() + "-> Message received: " + Nr_neighboors);
                 for (String vertex : Nr_neighboors) {
                     int propinquity = 0;
                     propinquity = P.get(vertex);
                     if (propinquity != 0) {
                         P.put(vertex, P.remove(vertex) + 1);
                     } else {
                         P.put(vertex, 1);
                     }
                 }
             }
             System.out.println("Hash for: " + this.getVertexID() + " -> " + P);
         }
     }
 
     private void incremental(Iterator<MapWritable> messages) throws IOException {
         if (this.getSuperstepCount() % 4 == 1) {
             for (String vertex : P.keySet()) {
                 int propinquityValue = P.get(vertex);
                 if (propinquityValue <= a && Nr.contains(vertex)) {
                     Nd.add(vertex);
                     Nr.remove(vertex);
                 }
                 if (propinquityValue >= b && !Nr.contains(vertex)) {
                     Ni.add(vertex);
                 }
             }
             System.out.println("For vertex " + this.getVertexID());
             System.out.println("Nr set: " + Nr);
             System.out.println("Ni set: " + Ni);
             System.out.println("Nd set: " + Nd);
             System.out.println("");
             for (String vertex : Nr) {
                 MapWritable outMsg = new MapWritable();
 
                 outMsg.put(new Text("PU+"), new ArrayWritable(Ni.toArray(new String[0])));
                 this.sendMessage(new Text(vertex), outMsg);
 
                 outMsg = new MapWritable();
 
                 outMsg.put(new Text("PU-"), new ArrayWritable(Nd.toArray(new String[0])));
                 this.sendMessage(new Text(vertex), outMsg);
             }
             for (String vertex : Ni) {
                 MapWritable outMsg = new MapWritable();
 
                 outMsg.put(new Text("PU+"), new ArrayWritable(Nr.toArray(new String[0])));
                 this.sendMessage(new Text(vertex), outMsg);
 
                 outMsg = new MapWritable();
 
                 Set<String> tmp = new HashSet<String>(Ni);
                 tmp.remove(vertex);
             
                 outMsg.put(new Text("PU+"), new ArrayWritable(tmp.toArray(new String[0])));
                 this.sendMessage(new Text(vertex), outMsg);
 
             }
             for (String vertex : Nd) {
                 MapWritable outMsg = new MapWritable();
 
                 outMsg.put(new Text("PU-"), new ArrayWritable(Nr.toArray(new String[0])));
                 this.sendMessage(new Text(vertex), outMsg);
 
                 outMsg = new MapWritable();
 
                 Set<String> tmp = new HashSet<String>(Nd);
                 tmp.remove(vertex);
 
                 outMsg.put(new Text("PU-"), new ArrayWritable(tmp.toArray(new String[0])));
                 this.sendMessage(new Text(vertex), outMsg);
             }
             voteToHalt();
         } else if (this.getSuperstepCount() % 4 == 2) {
         } else if (this.getSuperstepCount() % 4 == 3) {
         } else if (this.getSuperstepCount() % 4 == 0) {
         }
     }
     
     @Override
     public void compute(Iterator<MapWritable> messages) throws IOException {
 
         if (this.getSuperstepCount() < 6) {
             initialize(messages);
        } else if (this.getSuperstepCount() > 8) {
             incremental(messages);
         }
     }
 
 
 }
