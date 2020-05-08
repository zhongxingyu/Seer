 package zone;
 
 import distribution.ZoneDistribution;
 import experiment.ZoneCommand;
 import messaging.Messaging;
 
 public class ScenarioExecutor implements Messaging {
 	Zone myZone = null;
 	
 	public ScenarioExecutor(Zone myZone){
 		this.myZone = myZone;
 	}
 	
	public void action(ZoneCommand command){
 		switch(command.getType()){
 			case ADD_RESOURCES:{
 				// TODO
 				break;
 			}
 			case MULTIPLY_RESOURCES:{
 				// TODO
 				break;
 			}
 			case ADD_INDIVIDUALS:{
 				myZone.createIndividuals((ZoneDistribution)command.getCommandContent());
 				break;
 			}
 		}
 	}
 }
