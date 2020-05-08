 package myagents;
 
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
import actions.SubmitCarbonEmissionReport;

import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
 import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
 import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
 import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.core.util.random.Random;
 import uk.ac.imperial.presage2.util.location.Location;
import uk.ac.imperial.presage2.util.location.Move;
 import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
 import uk.ac.imperial.presage2.util.participant.AbstractParticipant;
 
 public class MyAgent extends AbstractParticipant {
 
 	Location loc;
 	
 	ParticipantLocationService locationService;
 	
 	public MyAgent(UUID id, String name, Location loc){
 		super(id,name);
 		this.loc = loc;
 	}
 	
 	@Override
 	protected void processInput(Input in) {
 		// TODO Auto-generated method stub
 
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
 		try{
 			this.locationService = this.getEnvironmentService(ParticipantLocationService.class);
 		}catch(UnavailableServiceException e){
 			logger.warn(e);
 		}
 		
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
 		
 	}
 
 }
