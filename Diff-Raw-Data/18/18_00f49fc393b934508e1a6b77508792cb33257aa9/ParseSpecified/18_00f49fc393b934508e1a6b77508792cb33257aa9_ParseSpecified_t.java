 package simulation.communications.channels.data;
 
 import simulation.networks.*;
 import simulation.networks.nodes.*;
 import java.util.*;
 
 /** Class to map user specified channel information to channels/links in a network.
  * Channel information assumed to be sparse, since user is inputting.
  * @author ykk
  */
public class SparseSpecified
     extends ChannelMap
 {
     //Members
     /** Indicates if channel map is symmetrical.
      * Defaulted to true.
      */ 
     public boolean symmetricalChannel = true;
    /** Vector of {@link NodePairProb}.
      */
    public Vector data = new Vector();
 
     //Methods
     /** Constructor
      */
     public ParseSpecified()
     {
 	super(null);
     }
 
     /** Function to map channel probability map.
      * Dummy function, since channel is built when specified.
      */
     public void buildMap()
     {
     }
 
     /** Function add links.
      * @param source source node
      * @param destination destination node
      * @param prob reception probability between nodes
      */
     public void addLink(Node source, Node destination, double prob)
     {
 	data.add(new NodePairProb(source,destination,prob));
     }
 
     /** Return channel reception probabilities using map.
      * Return zero by default, unless probabilities is specified.
      * @param source source node
      * @param destination destination node
      * @return reception probability from source to destination
      */
     public double channelProb(Node source, Node destination)
     {
 	NodePairProb d;
 	for (int i = 0; i < data.size(); i++)
 	{
 	    d = (NodePairProb) data.get(i);
 	    if ((d.source == source && d.destination == destination) ||
 		(d.source == destination && d.destination == source && symmetricalChannel))
 		return d.prob;
 	}
 
 	return 0.0;
     }
 }
