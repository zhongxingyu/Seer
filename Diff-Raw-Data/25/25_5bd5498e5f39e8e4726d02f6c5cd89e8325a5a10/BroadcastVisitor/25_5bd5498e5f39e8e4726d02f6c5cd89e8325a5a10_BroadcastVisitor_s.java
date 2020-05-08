 package hypeerweb;
 
 import java.util.Set;
 import java.util.TreeSet;
 
 /**
  * Broadcasts a message from a source node to all nodes in the HyPeerWeb.
  * The message is actually the method operation(Node, Parameters) to be performed on all nodes.
  * The method operation(Node, Parameters) must be overridden in an implementing subclass.
  * 
  * Domain: None
  * 
  * @author Matthew
  */
 public class BroadcastVisitor implements Visitor
 {
     /** The key used to identify a key-value pair in the parameters list. */
     protected static String STARTED_KEY;
     
     /** The flag that tells whether the broadcast should be started from node zero
      *  or continued from the current node*/
     private boolean startFromZero;
 
     /**
      * The default constructor
      * 
      * @pre None
      * @post startFromZero=true
      */
     public BroadcastVisitor()
     {
         startFromZero=true;
     }
     
     /**
      * Creates the minimum set of parameters needed when invoking an accept method during a broadcast.
      * At the top level (this level) there are no required parameters.
      * If there are more required parameters in a subclass, this method is overridden.
      * 
      * @pre None
      * @post See return
      * @return A Parameters of size zero.
      */
     public static Parameters createInitialParameters()
     {
         return new Parameters();
     }
     
     @Override
     public void visit(final Node node, final Parameters parameters)
     {
         operation(node, parameters);
     }
     /**
      * broadcastFromZero
      * @param node
      * @param parameters
      */
     private void broadcastFromZero(Node node, Parameters parameters)
     {
         Node cap = node.findCapNode(node);
         Node zero = cap.getFold();
         
         zero.accept(this, parameters);
     }
     /**
      * 
      * 
      * @return
      */
     private Set<Node> getNeighborsToBroadcast(Node node)
     {
         int nodeId = node.getWebId();
         int height = node.getHeight();
         Set<Node> neighborsToBroadcast = new TreeSet<Node>();
         Set<Node> neighbors = node.connections.getNeighbors();
         
         double limit = Math.pow(2, height);
         
         for(int bit=1; bit<limit; bit*=2){
             int idToBroadcast = nodeId | bit;
             
             if(idToBroadcast == nodeId) break; //reached the first '1'
             else{
                 for(Node neighbor: neighbors){
                     if(neighbor.getWebId()==idToBroadcast){
                         neighborsToBroadcast.add(neighbor);
                         break;
                     }
                 }
             }
         }
         
         return neighborsToBroadcast;
         
         
     }
     /**
      * The abstract operation to be performed on all nodes.
      * This operation must be implemented in all concrete subclasses.
      * 
      * @param node The node the operation is to be performed on
      * @param parameters The parameters needed to perform the operation.
      * @pre node is not null and parameters is not null.
      * @post True
      */
     private void operation(Node node, Parameters parameters){
         if (node.getWebId() == 0)
             startFromZero = false;
 
         if (startFromZero)
             broadcastFromZero(node, parameters);
         else
         {
             Contents contents = node.getContents();
             
             Set<String> keySet = parameters.getKeys();
             
             for(String key : keySet){
                 if (contents.containsKey(key))
                 {
                         System.err.println("Node " + node.getWebId() + " has already been visited ");
                 }
                 contents.set(key, parameters.get(key));
             }
 
             
             
             
             for(Node nodeToBroadcast: getNeighborsToBroadcast(node)){
                 nodeToBroadcast.accept(this, parameters);
             }
         }
 
     }
 }
