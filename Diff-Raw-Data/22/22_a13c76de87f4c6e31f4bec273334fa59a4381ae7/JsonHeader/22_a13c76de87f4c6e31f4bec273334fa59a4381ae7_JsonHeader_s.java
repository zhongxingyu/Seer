 package ch.cern.atlas.apvs.ptu.server;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 import ch.cern.atlas.apvs.domain.Error;
 import ch.cern.atlas.apvs.domain.Event;
 import ch.cern.atlas.apvs.domain.Measurement;
 import ch.cern.atlas.apvs.domain.Message;
 import ch.cern.atlas.apvs.domain.Order;
 import ch.cern.atlas.apvs.domain.Report;
 
 public class JsonHeader {
 
	private Logger log = LoggerFactory.getLogger(getClass().getName());

 	transient static int currentFrameID = 0;
 	
 	String sender;
 	String receiver = "Broadcast";
 	String frameID;
 	String acknowledge = "False"; 
 	JsonMessage[] messages;
 	
	public JsonHeader(Message message) {
 		currentFrameID++;
 		frameID = String.valueOf(currentFrameID);
 		sender = message.getPtuId();
 		messages = new JsonMessage[1];
 		if (message instanceof Measurement) {
 			messages[0] = new JsonMeasurement(message);
 		} else if (message instanceof Report) {
 			messages[0] = new JsonReport(message);			
 		} else if (message instanceof Event) {
 			messages[0] = new JsonEvent(message);			
 		} else if (message instanceof Error) {
 			messages[0] = new JsonError(message);
 		} else if (message instanceof Order) {
 			messages[0] = new JsonOrder(message);
 		} else {
			log.warn("ERROR: do not know how to write message of type: "+message.getClass());
 		}
 	}
 }
