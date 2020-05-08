 package smarthouse.simulation;
 
 import jade.core.behaviours.CyclicBehaviour;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 
 import java.io.IOException;
 
 import Data.Constants;
 import Data.MessageContent;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 public class ReceiveInformBehaviour extends CyclicBehaviour {
 	public void action() {
 		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
 		ACLMessage msg = myAgent.receive(mt);
 		if (msg != null) {
 			parseContent(msg);
 		} else {
 			block();
 		}
 	}
 
 	private void parseContent(ACLMessage msg) {
 		ObjectMapper mapper = new ObjectMapper();
 		MessageContent json;
 		try {
 			json = mapper.readValue(msg.getContent(), MessageContent.class);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return;
 		}
 
 		String action = json.getContent().get(0);
 		if (action.equals(Constants.ACTION_LOG_APPEND)) {
			((SimulationAgent) myAgent).getWindow().appendLog(json.getContent().get(1));
 		} else {
 			Room room = ((SimulationAgent) myAgent).getWindow().getRoom(json.getPlace());
 			int id = Integer.parseInt(action);
 			String s = json.getType();
 			if(s.equals(Constants.LIGHT)) {
 				room.setLight(id, json.getValue() > 0);
 			} else if(s.equals(Constants.SHUTTER)) {
 				room.setShutter(id, json.getValue() > 0);
 			} else if(s.equals(Constants.HEATER)) {
 				room.setHeater(id, json.getValue() > 0);
 			} else if(s.equals(Constants.WINDOW)) {
 				room.setWindow(id, json.getValue() > 0);
 			}
 		}
 	}
 }
