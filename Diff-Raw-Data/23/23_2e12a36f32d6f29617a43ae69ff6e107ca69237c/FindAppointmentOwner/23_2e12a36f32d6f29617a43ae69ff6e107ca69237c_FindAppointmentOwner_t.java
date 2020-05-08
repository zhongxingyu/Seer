 package jadeCW;
 
 import java.util.HashMap;
 import java.util.HashSet;
 
 import jade.core.AID;
 import jade.core.behaviours.Behaviour;
 import jade.lang.acl.ACLMessage;
 import jade.lang.acl.MessageTemplate;
 import jade.lang.acl.UnreadableException;
 
 public class FindAppointmentOwner extends Behaviour {
 
 	public int step = 0;
 	private PatientAgent patientAgent;
 	private HashMap<Integer, HashSet<Integer>> higherPriorities;
 	private int assignedPriority;
 	private int checkedCount;
 	private MessageTemplate reqTemplate;
 
 	public FindAppointmentOwner(PatientAgent patientAgent){
 		super(patientAgent);
 		this.patientAgent = patientAgent;
 		this.checkedCount = 0;
 	}
 
 	@Override
 	public void action() {
 		switch (step) {
 		case 0:
 			requestPreferredAppointment();
 			break;
 		case 1:
 			try {
 				getPreferredAppointmentConfirmation();
 			} catch (UnreadableException e) {
 				e.printStackTrace();
 			}
 			break;
 		}
 	}
 
 	private int getNextTimeSlot(){
 
 		for (int i = 0; i<assignedPriority; i++){
 			higherPriorities.get(i);
 		}
 
 		return -1;
 	}
 
 	public void requestPreferredAppointment(){
 		
 		if (patientAgent.highPriorityAppointmentOwner != null)
 			return;
 		
 		if (patientAgent.allocatedAppointment == -2)
 			return;
 		
 		this.assignedPriority = patientAgent.preferedAppointmentPriority();
 
 		if (assignedPriority == -1)
 			assignedPriority = patientAgent.preferences.size();
 
 		//Highest priority reached!
 		if (assignedPriority == 0){
 			step = 2;
 			return;
 		}
 
 		higherPriorities = new HashMap<Integer, HashSet<Integer>>();
 		for (int i = 0; i< assignedPriority; i++){
 			higherPriorities.put(i, patientAgent.preferences.get(i));
 		}
 
 		Integer nextSlot = null;
 		for (Integer i : higherPriorities.keySet()){
 			HashSet<Integer> prefs = higherPriorities.get(i);
 			for (Integer j : prefs){
 				if (!patientAgent.excluded.contains(j)){
 					nextSlot = j;
 					break;
 				}
 			}
 		}
 
 		// If all have been checked then we empty the excluded set and regenerate it with an empty set
 		if (nextSlot == null){
 			checkedCount++;
 
 			if (checkedCount == 3){
 				step = 2;
 				return;
 			}
 
 			patientAgent.excluded.clear();
 			step = 0;
 			return;
 		}
 
 		String conversationId = "find-appointment-owner";
 
 		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
 		request.addReceiver(patientAgent.allocationAgent);
 		request.setContent(nextSlot.toString());
 		System.out.println(patientAgent.getName() + " requesting slot " + nextSlot.toString());
 
 		request.setSender(patientAgent.getAID());
 		request.setConversationId(conversationId);
 		request.setReplyWith(conversationId + " " + System.currentTimeMillis());
 		patientAgent.send(request);
 		reqTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId(conversationId),
 				MessageTemplate.MatchInReplyTo(request.getReplyWith())); 
 		step = 1;
 
 	}
 
 	public void getPreferredAppointmentConfirmation() throws UnreadableException{
 		ACLMessage reply = patientAgent.receive(reqTemplate);
		
 		if (reply != null) {
 			if (!reqTemplate.match(reply)) {
 				System.err.println("Message template doesn't match!");
 			} else {
 				if (reply.getPerformative() == ACLMessage.FAILURE)
 					System.out.println(reply.getContent());
 				else {
 					AID resourceOwner = (AID) reply.getContentObject();
 					patientAgent.highPriorityAppointmentOwner = resourceOwner;
 					System.out.println("slot owned by " + resourceOwner.getName());
 				}
 				step = 0;
 			}
 		} else {
 			block();
 		}
 	}
 
 	@Override
 	public boolean done() {
 		return false;
 	}
 
 }
