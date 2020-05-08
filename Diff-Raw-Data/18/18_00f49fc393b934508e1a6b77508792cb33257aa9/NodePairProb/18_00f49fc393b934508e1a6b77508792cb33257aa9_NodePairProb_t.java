 package simulation.communications.channels.data;
 
 import simulation.networks.nodes.*;
 
 /** Class to hold reception probability of channel between a source and destination pair.
  * @author ykk
  */
 public class NodePairProb
 {
     //Members
     /** Source node.
      */
    public Node source;
     /** Destination node.
      */
    public Node destination;
     /** Reception probability.
      */ 
     public double prob;
 
     //Methods
     /**Constructor
      * @param source source node
      * @param destination destination node
      * @param prob reception probability between nodes
      */
     public NodePairProb(Node source, Node destination, double prob)
     {
 	this.source = source;
 	this.destination = destination;
 	this.prob = prob;
     }
 }
