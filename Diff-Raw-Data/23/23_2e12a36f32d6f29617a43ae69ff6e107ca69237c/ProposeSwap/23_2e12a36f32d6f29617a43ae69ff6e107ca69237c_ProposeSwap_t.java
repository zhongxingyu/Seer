 package jadeCW;
 
 import java.io.IOException;
 
 import jade.core.AID;
 import jade.core.behaviours.Behaviour;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 import jade.lang.acl.UnreadableException;
 
 public class ProposeSwap extends Behaviour {
 
 	PatientAgent patientAgent;
 	private int step;
 	MessageTemplate reqTemplate;
 	
 	public ProposeSwap(PatientAgent patientAgent) {
 		super(patientAgent);
 		this.patientAgent = patientAgent;
 		step = 0;
 	}
 	
 	@Override
 	public void action() {
 		switch (step) {
 		case 0:
 			requestSwapAppointments();
 			break;
 		case 1:
 			try {
 				recieveSwapAppointmentReply();
 			} catch (UnreadableException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			break;
 		}
 	}
 
 	@Override
 	public boolean done() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 	
 	public void recieveSwapAppointmentReply() throws UnreadableException, IOException{
 		
 		ACLMessage reply = patientAgent.receive(reqTemplate);
 
 		if (reply != null) {
 			if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
 				informHospital(reply.getSender());
				patientAgent.allocatedAppointment = Integer.parseInt(reply.getContent());
 				patientAgent.highPriorityAppointmentOwner = null;
 				step = 0;
 			}
 			else {
 				patientAgent.excludeSlotAndSetNextSlot();
 				patientAgent.excluded.add(Integer.valueOf(reply.getContent()));
 				patientAgent.highPriorityAppointmentOwner = null;
 				step = 0;
 			}
 		} else {
 			block();
 		}
 	}
 	
 	private void informHospital(AID swapPatientAgentAID) throws IOException{
 		String conversationId = "request-swap-to-hospital";
 		ACLMessage request = new ACLMessage(ACLMessage.INFORM);
 		request.addReceiver(patientAgent.allocationAgent);
 		request.setSender(patientAgent.getAID());
 		request.setContentObject(swapPatientAgentAID);
 		request.setConversationId(conversationId);
 		request.setReplyWith(conversationId + " " + System.currentTimeMillis());
 		patientAgent.send(request);
 	}
 	
 	public void requestSwapAppointments(){
 
 		if (patientAgent.highPriorityAppointmentOwner != null){
 			
 			System.out.println("Proposing to swap");
 
 			String conversationId = "request-swap";
 			ACLMessage request = new ACLMessage(ACLMessage.PROPOSE);
 			request.addReceiver(patientAgent.highPriorityAppointmentOwner);
 			request.setContent(Integer.toString(patientAgent.allocatedAppointment));
 			request.setSender(patientAgent.getAID());
 			request.setConversationId(conversationId);
 			request.setReplyWith(conversationId + " " + System.currentTimeMillis());
 			patientAgent.send(request);
 			reqTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId(conversationId),
 					MessageTemplate.MatchInReplyTo(request.getReplyWith())); 
 			step = 1;
 			
 		}
 	}
 
 }
