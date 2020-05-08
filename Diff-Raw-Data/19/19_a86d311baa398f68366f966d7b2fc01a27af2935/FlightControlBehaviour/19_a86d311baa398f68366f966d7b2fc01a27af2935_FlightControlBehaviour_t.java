 package pl.ug.airport.behaviours;
 
 import java.util.Random;
 
 import pl.ug.airport.agents.FlightControlAgent;
 import pl.ug.airport.helpers.AirportLogger;
 import pl.ug.airport.helpers.PlaneStatus;
 import pl.ug.airport.messages.AgentAddresses;
 import pl.ug.airport.messages.StringMessages;
 import jade.core.AID;
 import jade.core.Agent;
 import jade.core.behaviours.CyclicBehaviour;
 import jade.lang.acl.ACLMessage;
 
 public class FlightControlBehaviour extends CyclicBehaviour {
 
 	private FlightControlAgent agent;
 
	private String TAG = "FlightControlAgent: ";
 	
 	public FlightControlBehaviour(FlightControlAgent _agent){
 		agent = _agent;
 	}
 	
 	@Override
 	public void action() {
 		receiveMessages();
 		sendMessages();
 	}
 
 	public void receiveMessages() {
 		ACLMessage rec = agent.receive();
 		if (rec != null) {
 			AirportLogger.log(TAG + "Received: " + rec.getContent());
 			try {
				StringMessages message = StringMessages.parseString(rec.getContent());
 				handleAirportMessage(message);
 			} catch(IllegalArgumentException ex){}
 		}else{
 			block();
 		}
 	}
 	
 	private void handleAirportMessage(StringMessages message) {
 		switch (message) {
 		case CLOSE_TO_AIRPORT:
 			AirportLogger.log(TAG + "Plane is closing in. Prepare airfield");
 			break;
 		case REQUEST_LANDING:
 			AirportLogger.log(TAG + "Plane is requesting landing permission. Granted");
 			agent.setAvailablePlanes( agent.getAvailablePlanes() + 1 );
 			break;	
		case PASSANGERS_LEFT:
			AirportLogger.log(TAG + "Passangers have left the plane");
			break;
 		case TAKE_OFF:
 			AirportLogger.log(TAG + "Plane is leaving the airport. Bye");
 			agent.setAvailablePlanes( agent.getAvailablePlanes() - 1 );
 			break;
 		case PLANE_READY:
 			AirportLogger.log(TAG + "Plane is ready for scheduling");
 			break;
 		default:
 			AirportLogger.log(TAG + "Unknown message received " + message);
 		}
 	}
 	
 	private void sendMessages() {
 		if(agent.getAvailablePlanes() > 0){
 			switch (new Random().nextInt(10)) {
 			case 0://odlot
 				//FIXME: trzeba znac identyfiaktor samolotu, ktorego chcemy wystartowac
 				this.send(AgentAddresses.getPlaneAgentAddress(0),
 						StringMessages.LEAVING_AT);
 				break;
 			case 1://informuj o opoznieniu
 				//FIXME: trzeba znac identyfiaktor samolotu, ktorego chcemy opoznic
 				this.send(AgentAddresses.getPassangerServiceAgentAddress(),
 						StringMessages.FLIGHTPLAN_WAS_CHANGED);
 				break;
 			default:
 				break;
 			}
 		}
 	}
 	
 	private void send(String address, StringMessages msgContent) {
 		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
 		msg.addReceiver(new AID(address, AID.ISLOCALNAME));
 		msg.setLanguage(AgentAddresses.getLang());
 		msg.setContent(msgContent.toString());
 		agent.send(msg);
 	}
 }
