 package myagents;
 
 import helloprotocol.HelloWorldProtocol;
 import helloprotocol.SimpleProtocol;
 
 import java.util.Set;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 
 import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
 import uk.ac.imperial.presage2.core.messaging.Input;
 import uk.ac.imperial.presage2.core.network.NetworkAddress;
 import uk.ac.imperial.presage2.util.location.Location;
 import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
 import uk.ac.imperial.presage2.util.participant.AbstractParticipant;
 
 public class MyAgent extends AbstractParticipant {
 
 	Logger logger = Logger.getLogger(MyAgent.class);
 	
 	Location loc;
 	
 	ParticipantLocationService locationService;
 	
	private HelloWorldProtocol simpleProtocol;
	
 	
 	public MyAgent(UUID id, String name, Location loc){
 		super(id,name);
 		this.loc = loc;
 	}
 	
 	@Override
 	protected void processInput(Input in) {
		this.simpleProtocol.canHandle(in);
 	}
 	
 	@Override
 	protected Set<ParticipantSharedState> getSharedState(){
 		Set<ParticipantSharedState> s = super.getSharedState();
 		s.add(ParticipantLocationService.createSharedState(getID(), loc));
 		return s;
 	}
 	
 
 	@Override
 	public void initialise(){
 		super.initialise();
 		/*try{
 			this.locationService = this.getEnvironmentService(ParticipantLocationService.class);
 		}catch(UnavailableServiceException e){
 			logger.warn(e);
 		}*/
 		
		//this.simpleProtocol = new SimpleProtocol("SIMPLE",
		//		this.network, this.authkey, this.getID(), this.environment);
 		
 		this.simpleProtocol = new HelloWorldProtocol(this.getName(), network);		
 	}
 	
 	@Override
 	public void execute(){
 		/*this.loc = locationService.getAgentLocation(this.getID());
 		logger.info("My Location is: " + this.loc);
 		
 		int dx = Random.randomInt(2) - 1;
 		int dy = Random.randomInt(2) - 1;
 		Move move = new Move(dx,dy);
 		
 		try{
 			this.environment.act(move, this.getID(), this.authkey);
 		}catch(ActionHandlingException e){
 			logger.warn("Error tyring to move", e);
 		}
 		
 		Location loca = this.locationService.getAgentLocation(getID());
 		logger.info("nearby location is" + loca.toString() + " Distance to: "+ loca.distanceTo(loc));*/
 		
 		//Put tests in here
 		if(this.simpleProtocol != null){
 			simpleProtocol.incrementTime();
 		}		
 		
 		Set<NetworkAddress> activeConversations = this.simpleProtocol.getActiveConversationMembers();
 		
		logger.info("Number of participants in the conversation with: " + activeConversations.size()
					+ " with participant" + this.getID());
 		
 		//if(activeConversations.size() == 0){
 /*			for(NetworkAddress net : this.network.getConnectedNodes()){
 				if(!activeConversations.contains(net)){
 					logger.info("agent ID= " + this.getID() + " about to spawn");
 					this.simpleProtocol.spawn(net);
 				}
 			}*/
 		//}else{
 		//	logger.info("no active conversation");
 		//}
 			
 			try {
 				Set<NetworkAddress> alreadyTalkingTo = this.simpleProtocol
 						.getActiveConversationMembers();
 				int convCount = alreadyTalkingTo.size();
 				for (NetworkAddress a : this.network.getConnectedNodes()) {
					logger.info("I'm connected to: " + a);
 					// spawn a conversation if I'm not already taking them them
 					// (limit 5 convs)
 					if (!alreadyTalkingTo.contains(a) && convCount < 5) {
 						this.simpleProtocol.spawn(a);
 						convCount++;
 					}
 				}
 			} catch (UnsupportedOperationException e) {
 				logger.warn("Can't get connected nodes", e);
 			}			
 		
 	}
 
 }
