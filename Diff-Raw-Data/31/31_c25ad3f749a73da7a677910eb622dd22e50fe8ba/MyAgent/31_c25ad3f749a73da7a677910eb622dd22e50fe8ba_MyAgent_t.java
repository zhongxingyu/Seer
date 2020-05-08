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
 	
 	private double carbonEmission = 10.0;
 
 	private Map<Integer, Double> carbonEmissionReports;
 	
 	ParticipantLocationService locationService;
 	
 	public MyAgent(UUID id, String name, Location loc){
 		super(id,name);
 		this.loc = loc;
 		this.carbonEmissionReports = new HashMap<Integer, Double>();
 	}
 	
 	@Override
 	protected void processInput(Input in) {
 		// TODO Auto-generated method stub
 
 	}
 	
 	@Override
 	protected Set<ParticipantSharedState> getSharedState(){
 		Set<ParticipantSharedState> s = super.getSharedState();
 		s.add(ParticipantLocationService.createSharedState(getID(), loc));
 		s.add(new ParticipantSharedState("Report", 
 	            (Serializable) this.getCarbonEmissionReports(), getID()));
 		return s;
 	}
 	
 	public Map<Integer,Double> getCarbonEmissionReports(){
 		return this.carbonEmissionReports;
 	}
 	
 	public Map<Integer,Double> addToReports(Time simTime, Double emission){
 		this.carbonEmissionReports.put(simTime.intValue(), emission);
 		return this.carbonEmissionReports;
 	}
 	
 	public Double calculateCarbonEmission(){
 		//TODO add code to calculate whether to submit true or false report (cheat)
 		return new Double(carbonEmission++);
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
 		
 		//Test for submitting reports
 		try{
 			this.environment.act(new SubmitCarbonEmissionReport(this.calculateCarbonEmission(), SimTime.get(), this), this.getID(), this.authkey);
 		}catch(ActionHandlingException e){
 			logger.warn("Error trying to submit report");
 		}
 		
 		
 	}
 
 }
