 package app.server.udpservlet;
 
 import packet.CoordinatorPacket;
 import packet.ElectionPacket;
 import udp.FIFOObjectUDPServlet;
 import utils.LiteLogger;
 import app.server.Config;
 import app.server.RetailStoreServerImpl;
 
 public class ElectionServlet extends FIFOObjectUDPServlet<RetailStoreServerImpl> {
 
 	public static int electionId = 0;
 	public ElectionServlet(int port, RetailStoreServerImpl owner) {
 		super(port, owner);
 	}
 
 	@Override
 	public void run() {		
 		LiteLogger.log("Statring ElectionServlet...");
		if (getOwner().getId() >= getOwner().getGroupMap().size()) {
 			LiteLogger.log("id = ",getOwner().getId(), " is the new leader by default");	
 			CoordinatorPacket coordinator = new CoordinatorPacket();
 			coordinator.setLeaderId(getOwner().getId());
 			getOwner().broadcastAll(coordinator, Config.ELECTION_RECEIVE_LISTEN_PORT);
 			getOwner().setLeaderId(getOwner().getId());
 		}
 		else {
 			LiteLogger.log("id = ",getOwner().getId(), " is NOT the new leader. Broadcasting to higher ids");	
 			ElectionPacket electionPacket = new ElectionPacket();
 			electionId += 1;
 			electionPacket.setId(electionId);
 			electionPacket.setSenderId(getOwner().getId());
 			
 			getOwner().setElectionState(ElectionState.WAIT_FOR_REPLY);
 			//getOwner().broadcastHigherId(electionPacket);									
 		}
 		
 	}
 }
