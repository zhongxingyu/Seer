 package pl.ug.airport.behaviours;
 
 import pl.ug.airport.helpers.AirportLogger;
 import pl.ug.airport.messages.AgentAddresses;
 import pl.ug.airport.messages.StringMessages;
 import jade.core.AID;
 import jade.core.Agent;
 import jade.core.behaviours.Behaviour;
 import jade.lang.acl.ACLMessage;
 
 public class PassangerBehaviour extends Behaviour {
 
 	private Agent agent;
 	private String TAG = "PassangerAgent: ";
 	
 	public PassangerBehaviour(Agent _agent) {
 		this.agent = _agent;
 	}
 	
 	@Override
 	public void action() {
 		ACLMessage msg = myAgent.receive();
 		if (msg != null) {
 			try{
 				StringMessages messageStr = StringMessages.parseString(msg.getContent());
 				messageHandler(messageStr);
 			} catch(IllegalArgumentException ex){}	
 		}
 		else {
 			block();
 		} 
 	}
 	
 	private void messageHandler(StringMessages messageStr) {
 		switch(messageStr) {
 		case INFORM_ABOUT_CHANGES:
 			AirportLogger.log(TAG + " Updating flight information");
 			//send(AgentAddresses.getPassangerAgentAddress(1), StringMessages.RESPONSE_OK);
 			break;
 			
 		case INFORM_ABOUT_FLIGHT:
 			AirportLogger.log(TAG + " Reminded about flight");
 			break;
 			
 		default:
 			AirportLogger.log(TAG + "Unknown message received " + messageStr);
 			break;
 
 	
 		}
 		
 	}
 	
 	private void send(String address, StringMessages msgContent) {
 		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
 		msg.addReceiver(new AID(address, AID.ISLOCALNAME));
 		msg.setLanguage(AgentAddresses.getLang());
 		msg.setContent(msgContent.toString());
 		agent.send(msg);
 	}
 
 	@Override
 	public boolean done() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 	//sending functions
 	private void reserveFlight() {
 		StringMessages rsv = StringMessages.RESERVATION;
 		send(AgentAddresses.getPassangerAgentAddress(0), rsv);
 		
 	}
 
 }
