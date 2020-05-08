 package com.workhub.jade.behaviour;
 
 import jade.core.behaviours.CyclicBehaviour;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 import jade.lang.acl.MessageTemplate.MatchExpression;
 
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.workhub.jade.agent.ClientAgent;
 import com.workhub.jade.agent.ElementAgent;
 import com.workhub.model.ElementModel;
 import com.workhub.utils.Constants;
 import com.workhub.utils.MessageFactory;
 import com.workhub.utils.Utils;
 // Behaviour agent client
 public class ContentClientBehaviour extends CyclicBehaviour{
 
 	
 	private MessageTemplate template = new MessageTemplate(new MatchExpression() {
 		@Override
 		public boolean match(ACLMessage msg) {
 			JsonParser js = new JsonParser();
 			int action = ((JsonObject) js.parse(msg.getContent())).get(Constants.JSON_ACTION).getAsInt();
 			switch (action) {
 			case Constants.MESSAGE_ACTION_CONTENT:
 			case Constants.MESSAGE_RECEIVE_ELEMENT_CONTENT:
 			case Constants.MESSAGE_RECEIVE_ELEMENT_TITLE:
 			case Constants.MESSAGE_ACTION_EDIT:
 			case Constants.MESSAGE_ACTION_IS_DYING:
 				return true;
 			default:
 				return false;
 			}
 		}
 	});
 	
 	
 	@Override
 	public void action() {
 		
 		ACLMessage message = myAgent.receive(template);
 		if (message!=null){
 			ACLMessage answer = null;
 
 			JsonParser js = new JsonParser();
 			int action = ((JsonObject) js.parse(message.getContent())).get(Constants.JSON_ACTION).getAsInt();
 			
 			if(action == Constants.MESSAGE_ACTION_CONTENT){
 				// si il recoit un MESSAGE_ACTION_CONTENT : demande contenu pour la mise a jour, envoie un MESSAGE_GET_CONTENT
 				answer = MessageFactory.createMessage((ClientAgent)myAgent, message.getSender(), Constants.MESSAGE_ACTION_GET_CONTENT);
 				myAgent.send(answer);
 				
 			}
 			else if(action == Constants.MESSAGE_ACTION_EDIT){
 				// Si action_edit : sait s'il peut etre editeur ou non
 				boolean editor = ((JsonObject) js.parse(message.getContent())).get("can_edit").getAsBoolean();
 				
 				if(editor == true){
 					// envoyer a l'interface un evenement de type EVENT_CAN_EDIT
 					((ClientAgent)myAgent).fireChanges(Constants.EVENT_TYPE_CAN_EDIT, message.getSender());
 				}
 				else{
 					// envoyer a l'interface un evenement de type EVENT_CANT_EDIT
 					((ClientAgent)myAgent).fireChanges(Constants.EVENT_TYPE_CANT_EDIT, message.getSender());
 				}
 
 			}
 			else if(action==Constants.MESSAGE_RECEIVE_ELEMENT_CONTENT ){
 				//mettre a jour l'interface
 				// renvoie un elementModel
 				ElementModel model = MessageFactory.getModel(message);
 				((ClientAgent)myAgent).fireChanges(Constants.EVENT_TYPE_CONTENU, model);
 			}			
 			else if(action==Constants.MESSAGE_RECEIVE_ELEMENT_TITLE){
				Constants.MESSAGE_RECEIVE_ELEMENT_TITLE){
                     ((ClientAgent)myAgent).fireChanges(Constants.EVENT_TYPE_GET_ELEMENTS, MessageFactory.getModel(message));
 			}
 			
 			else if(action == Constants.MESSAGE_ACTION_IS_DYING){
 				((ClientAgent)myAgent).fireChanges(Constants.EVENT_TYPE_DIED, message.getSender());
 			}
 		}
 		
 	
 		
 		
 	}
 
 }
