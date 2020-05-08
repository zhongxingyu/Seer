 /**
  * 
  */
 package uk.ac.imperial.dws04.Presage2Experiments.IPCon.Messages;
 
 import uk.ac.imperial.dws04.Presage2Experiments.RoadLocation;
import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.Chosen;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConRIC;
 import uk.ac.imperial.dws04.utils.record.Pair;
 import uk.ac.imperial.presage2.core.Time;
 import uk.ac.imperial.presage2.core.messaging.Performative;
 import uk.ac.imperial.presage2.core.network.BroadcastMessage;
 import uk.ac.imperial.presage2.core.network.NetworkAddress;
 
 /**
  * Class for broadcast message indicating the RIC an agent is in and the currently chosen value, if one exists
  * @author dws04
  *
  */
public class ClusterPing extends BroadcastMessage<Pair<RoadLocation, Pair<IPConRIC, Chosen>>> {
 
 	/**
 	 * Broadcast message indicating the RIC an agent is in and the currently chosen fact, if one exists
 	 * @param performative
 	 * @param type
 	 * @param timestamp
 	 * @param from
 	 * @param data the location of the agent, the IPConRIC in question, and the chosen fact if one exists, or null otherwise
 	 */
 	public ClusterPing(Performative performative, Time timestamp,
			NetworkAddress from, final Pair<RoadLocation, Pair<IPConRIC, Chosen>> data) {
 		super(performative, "ClusterPing", timestamp, from, data);
 		// TODO Auto-generated constructor stub
 	}
 	
 	public RoadLocation getLocation() {
 		return getData().getA();
 	}
 	
 	/**
 	 * 
 	 * @return the RIC the agent is in
 	 */
 	public IPConRIC getRIC() {
 		return getData().getB().getA();
 	}
 	
 	/**
 	 * 
 	 * @return The currently chosen fact in the RIC, or null if nothing is chosen
 	 */
 	public Object getChosen() {
 		return getData().getB().getB();
 	}
 
 }
