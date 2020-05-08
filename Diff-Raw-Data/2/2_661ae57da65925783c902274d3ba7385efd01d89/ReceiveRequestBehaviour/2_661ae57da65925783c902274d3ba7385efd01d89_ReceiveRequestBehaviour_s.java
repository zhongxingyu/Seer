 package smarthouse.simulation;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import Data.Constants;
 import Data.MessageContent;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 import jade.core.behaviours.CyclicBehaviour;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 
 public class ReceiveRequestBehaviour extends CyclicBehaviour {
 	private ObjectMapper mapper = new ObjectMapper();
 
 	public void action() {
 		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
 		ACLMessage msg = myAgent.receive(mt);
 		if (msg != null) {
 			parseContent(msg);
 		} else {
 			block();
 		}
 	}
 
 	private void parseContent(ACLMessage msg) {
 		MessageContent json;
 		try {
 			json = mapper.readValue(msg.getContent(), MessageContent.class);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return;
 		}
 
 		Room room = ((SimulationAgent) myAgent).getWindow().getRoom(json.getPlace());
 		MessageContent replyJson = new MessageContent();
 		replyJson.setPlace(json.getPlace());
 		replyJson.setType(json.getType());
 		double value = 0;
 		ArrayList<String> content = new ArrayList<String>();
 		content.add(json.getContent().get(0));
 		String s = json.getContent().get(0);
 		if(s.equals("day")) {
 			value = ((SimulationAgent) myAgent).getWindow().isDay() ? 1 : 0;
 		} else if(s.equals("temperature")) {
 			value = room.getTemperature();
 		} else if(s.equals(Constants.LIGHT)) {
 			value = room.getLightLevel();
 		} else if(s.equals("time")) {
 			int[] datetime = ((SimulationAgent) myAgent).getWindow().getTime();
 			content.add(String.valueOf(datetime[0]));
 			content.add(String.valueOf(datetime[1]));
			content.add(String.valueOf(datetime[1]));
 		} else if(s.equals("isOn") || s.equals("isOpen")) {
 			int id = Integer.parseInt(json.getContent().get(1));
 			content.add(String.valueOf(id));
 			String tmp = json.getType();
 			if(tmp.equals(Constants.LIGHT)) {
 				value = room.getLightStatus(id);
 			} else if(tmp.equals(Constants.SHUTTER)) {
 				value = room.getShutterStatus(id);					
 			} else if(tmp.equals(Constants.HEATER)) {
 				value = room.getHeaterStatus(id);
 			} else if(tmp.equals(Constants.WINDOW)) {
 				value = room.getWindowStatus(id);
 			}
 		}
 		replyJson.setContent(content);
 		replyJson.setValue(value);
 
 		sendReply(msg, replyJson);
 	}
 
 	private void sendReply(ACLMessage msg, MessageContent json) {
 		ACLMessage reply = msg.createReply();
 		reply.setPerformative(ACLMessage.INFORM);
 		try {
 			reply.setContent(mapper.writeValueAsString(json));
 		} catch (JsonProcessingException e) {
 			e.printStackTrace();
 			return;
 		}
 		myAgent.send(reply);
 	}
 }
