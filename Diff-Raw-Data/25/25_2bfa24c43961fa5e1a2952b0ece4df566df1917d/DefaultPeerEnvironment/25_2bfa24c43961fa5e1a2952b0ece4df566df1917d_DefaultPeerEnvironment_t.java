 package kimononet.peer;
 
/**
 * 
 * <pre>
 * 1. <b>beacon-service-timeout</b>: This value indicates how long in seconds a beacon service waits for receiving a beacon from a neighbor before sending out its own beacon.
 * 2. <b>max-beacon-peers</b>: Maximum number of peers to be included in beacon packets.
 * 3. <b>node-failure-rate</b>: With some frequency specified by this value, nodes will completely shutdown. This allows simulation of hostile environment.
 * 4. <b>packet-loss-rate</b>: Packets will be discarded on the receiving end at some frequency according to this value. For example 0.1 will indicate 10% loss at each node.
 * 5. <b>max-transmission-range</b>: Packets above received further away than this distance specified in meters will be discarded.
 * 6. <b>average-gps-accuracy</b>: Indicates the average value of some distribution curve to be determined.
 * </pre>
 * 
 * @author Zorayr Khalapyan
 * @version 3/12/2012
 *
 */
 public class DefaultPeerEnvironment extends PeerEnvironment{
 
 	public DefaultPeerEnvironment()
 	{
		set("beacon-service-timeout", "100");
		set("max-beacon-peers",       "34");
		set("node-failure-rate",      "0");
		set("packet-loss-rate",       "0");
		set("max-transmission-range", "0");
		set("average-gps-accuracy",   "1");
 		
 	}
 }
