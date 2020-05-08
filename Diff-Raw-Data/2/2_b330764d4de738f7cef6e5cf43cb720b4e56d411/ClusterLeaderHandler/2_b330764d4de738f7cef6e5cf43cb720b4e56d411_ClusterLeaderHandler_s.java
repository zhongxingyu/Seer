 package nl.mindef.c2sc.nbs.olsr.pud.uplink.server.handlers;
 
 import nl.mindef.c2sc.nbs.olsr.pud.uplink.server.dao.domainmodel.Gateway;
 
 import org.olsr.plugin.pud.ClusterLeader;
 
 public interface ClusterLeaderHandler {
 	/**
 	 * Process a ClusterLeader message and save it into the database
 	 * 
 	 * @param gateway
 	 *          the gateway from which the message was received
 	 * @param utcTimestamp
 	 *          the timestamp on which the message was received
 	 * @param clMsg
 	 *          the ClusterLeader message
 	 * @return true when the data in the message resulted in a database update
 	 */
	public boolean handleClusterLeaderMessage(Gateway gateway, long timestamp, ClusterLeader clMsg);
 }
