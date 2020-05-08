 package app.server.udpservlet;
 
 import java.net.SocketTimeoutException;
 
 import packet.AnswerPacket;
 import packet.CoordinatorPacket;
 import packet.ElectionPacket;
 import udp.FIFOObjectUDPServlet;
 import utils.LiteLogger;
 import app.server.Config;
 import app.server.RetailStoreServerImpl;
 
 public class ElectionReceiveServlet extends FIFOObjectUDPServlet<RetailStoreServerImpl> {
 
 
 	private static final long serialVersionUID = 6453595686631867382L;
 	public static int electionId = 0;
 	
 	public ElectionReceiveServlet(int port, RetailStoreServerImpl owner) {
 		super(port, owner);
 	}
 
 	@Override
 	public void run() {		
 		while (true) {			
 			int timeout  = 10000;						
 			Object packet;
 			try {
 
 //				getOwner().setElectionState(ElectionState.WAIT_FOR_REPLY); //TODO: to remove, for testing only
 				packet = receiveWithTimeout(timeout);
 				
 				LiteLogger.log("ELECTION PACKET RECEIVED!! PACKET = ", packet);
 				if (packet instanceof ElectionPacket) {
 					ElectionPacket electionPacket = (ElectionPacket)packet;
 					LiteLogger.log("Election packet type = Election");
 					if (getOwner().getElectionState() == ElectionState.IDLE) {
 						AnswerPacket answerPacket = new AnswerPacket();
 						answerPacket.setElectionId(electionPacket.getId());
 					}
 				}
 				else if (packet instanceof AnswerPacket) {
 					LiteLogger.log("Election packet type = Answer");
 					getOwner().setElectionState(ElectionState.WAIT_FOR_LEADER);
 				}
 				else if (packet instanceof CoordinatorPacket) {	
 					LiteLogger.log("Election packet type = Coordinator");
 					getOwner().setElectionState(ElectionState.IDLE);
 					getOwner().setLeaderId( CoordinatorPacket.class.cast(packet).getLeaderId());
 				}
				else {
					LiteLogger.log("SOMETHING WENT WRONG, INVALID TYPE RECEIVED");
				}
 				
 			} catch (SocketTimeoutException e) {
 				System.out.println(e.getMessage() + "\n" + e.getStackTrace());
 				
 				if (getOwner().getElectionState() == ElectionState.WAIT_FOR_REPLY) {
 					LiteLogger.log("Election state = ", getOwner().getElectionState());
 					
 					CoordinatorPacket coordinator = new CoordinatorPacket();
 					coordinator.setLeaderId(getOwner().getId());
 					LiteLogger.log("New coordinator object created. Setting leader to ", getOwner().getId());
 					
 					getOwner().broadcastAll(coordinator, Config.ELECTION_RECEIVE_LISTEN_PORT);
 					getOwner().setLeaderId(getOwner().getId());
 					getOwner().setElectionState(ElectionState.IDLE);
 				}
 				else if (getOwner().getElectionState() == ElectionState.WAIT_FOR_LEADER) {
 					//(new Thread(new ElectionServlet(Config.ELECTION_LISTEN_PORT, getOwner()))).start();					
 				}
 			}							
 		}
 	}
 }
