 package experiment;
 
 import jade.core.AID;
 import jade.core.Agent;
 import jade.wrapper.AgentController;
 import jade.wrapper.ContainerController;
 import jade.wrapper.StaleProxyException;
 
 import java.util.Vector;
 
 import distribution.ExperimentDistribution;
 import distribution.ZoneDistribution;
 
 public class Experiment extends Agent {
 
 	private static final long serialVersionUID = 1L;
 	
 	private static final String ZONE_CLASS_PATH = "zone.Zone";
 	
 	Vector<AID> zonesAIDs;
 	int numberOfModelingYears;
 	Scenario scenario;
 	
 	@Override
 	protected void setup(){
 		zonesAIDs = new Vector<AID>();
		// scenario = (Scenario)getArguments()[1];				// TODO to future
 		numberOfModelingYears = (int)((Integer)getArguments()[2]);
 		startZones(createZones());
 		addBehaviour(new ExperimentBehaviour());				// implement ExperimentBehaviour and define constructor args
 	}
 	
 	private Vector<AgentController> createZones(){
 		ContainerController controller = this.getContainerController();
 		Vector<AgentController> zoneAgents = new Vector<AgentController>();
 		ExperimentDistribution distribution = (ExperimentDistribution)getArguments()[0];
 		int i=0;
 		for (ZoneDistribution zoneDistr : distribution.getZoneDistributions()) {
 			try {
 				zoneAgents.add(
 						controller.createNewAgent(
 								getZoneName(i),
 								ZONE_CLASS_PATH, 
 								new Object[]{zoneDistr}));			// agent created
 			} catch (StaleProxyException e) {
 				e.printStackTrace();// TODO Auto-generated catch block
 			}
 			zonesAIDs.add(new AID(getZoneName(i), AID.ISLOCALNAME));		// agent ID saved to private list
 			i++;
 		}
 		return zoneAgents;
 	}
 	
 	private void startZones(Vector<AgentController> zoneAgents){
 		for (AgentController agent : zoneAgents){
 			try {
 				agent.start();									// agent behaviors started
 			} catch (StaleProxyException e) {
 				e.printStackTrace();// TODO Auto-generated catch block
 			}
 		}
 	}
 	
 	AID getZoneAID(int zoneNumber){
 		return zonesAIDs.get(zoneNumber);					// if invalid zoneNumber, then ignore it.		
 	}
 	
 	private String getZoneName(int i){
 		return "" + getLocalName() + "_Zone_" + i;
 	}
 	
 }
