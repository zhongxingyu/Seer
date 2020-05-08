 package zone;
 
import java.io.IOException;

 import distribution.ZoneDistribution;
 import experiment.ZoneCommand;
 import messaging.Messaging;
 
 public class ScenarioExecutor implements Messaging {
 	Zone myZone = null;
 	
 	public ScenarioExecutor(Zone myZone){
 		this.myZone = myZone;
 	}
 	
	public void action(ZoneCommand command) throws IOException{
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
			default:{
				IOException e = new IOException("Reading command error: unknown type");
				throw e;
			}
 		}
 	}
 }
