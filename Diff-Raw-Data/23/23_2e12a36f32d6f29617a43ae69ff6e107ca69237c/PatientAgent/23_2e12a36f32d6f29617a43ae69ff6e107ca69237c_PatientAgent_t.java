 package jadeCW;
 
 import java.util.HashMap;
 import java.util.HashSet;
 
 import jade.core.AID;
 import jade.core.Agent;
 import jade.domain.DFService;
 import jade.domain.FIPAException;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.SearchConstraints;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.lang.acl.ACLMessage;
 import jade.proto.SubscriptionInitiator;
 import jade.util.leap.Iterator;
 
 public class PatientAgent extends Agent {
 
 	public HashMap<Integer, HashSet<Integer>> preferences 
 	= new HashMap<Integer, HashSet<Integer>>();
 
 	public HashSet<DFAgentDescription> agentDescriptions = new HashSet<DFAgentDescription>();
 
 	public boolean hasAppointment = false;
 	public int allocatedAppointment = -2;
 	public AID allocationAgent = null;
 	
 	public AID highPriorityAppointmentOwner = null;
 	
 	public HashSet<Integer> excluded = new HashSet<Integer>();
 
 	public void setup(){
 
 		Object[] args = getArguments();
 		if (args != null && args.length > 0) {
 
 			int count = 0;
 			preferences.put(count, new HashSet<Integer>());
 
 			for (int i=0; i<args.length; i++) {
 				if (args[i].equals("-")) {
 					count++;
 					preferences.put(count, new HashSet<Integer>());
 				}
 				else
 					preferences.get(count).add(Integer.parseInt((String) args[i]));
 			}
 
 		}
 
 		subscribe();
 		addBehaviour(new RequestAppointment(this));
 		addBehaviour(new FindAppointmentOwner(this));
 		addBehaviour(new ProposeSwap(this));
 		addBehaviour(new RespondToProposal1(this));
 		
 	}
 	
 	public void excludeSlotAndSetNextSlot(){
 		// TODO: Code that will improve the hig
 	}
 	
 	public int getPriorityOfTimeSlot(int timeSlot){
 		int priority = -1;
 		
 		for (int j = 0; j < preferences.keySet().size(); j++){
 			if (preferences.get(j).contains(timeSlot))
 				priority = j;
 		}
 		
 		return priority;
 	}
 	
 	public Integer getCurrentPriority(){
 		return this.getPriorityOfTimeSlot(this.allocatedAppointment);
 	}
 	
 	public Integer preferedAppointmentPriority(){
 		
 		for (Integer i : preferences.keySet()){
 			HashSet<Integer> prefs = preferences.get(i);
 			for (Integer k : prefs){
 				if (k.equals(allocatedAppointment))
 					return i;
 			}
 		}
 		
		return -1;
 		
 	}
 	
 	private void subscribe() {
 		final String serviceType = "allocate-appointments"; 
 
 		// Build the description used as template for the subscription
 		DFAgentDescription template = new DFAgentDescription();
 		ServiceDescription templateSd = new ServiceDescription();
 		templateSd.setType(serviceType);
 		template.addServices(templateSd);
 
 		SearchConstraints sc = new SearchConstraints();
 		// We want to receive 10 results at most
 		//sc.setMaxResults(new Long(10));
 
 		addBehaviour(new SubscriptionInitiator(this, DFService.createSubscriptionMessage(this, getDefaultDF(), template, sc)) {
 			protected void handleInform(ACLMessage inform) {
 				System.out.println("Agent "+getLocalName()+": Notification received from DF");
 				try {
 					DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
 
 					if (results.length > 0) {
 						DFAgentDescription dfd = results[0];
 						AID provider = dfd.getName();
 						Iterator it = dfd.getAllServices();
 						while (it.hasNext()) {
 							ServiceDescription sd = (ServiceDescription) it.next();
 							if (sd.getType().equals(serviceType)) {
 								System.out.println("Allocate appointment service found:");
 								System.out.println("- Service \""+sd.getName()+"\" provided by agent "+provider.getName());
 								allocationAgent = provider;
 								break;
 							}
 						}
 					}	
 					System.out.println();
 				}
 				catch (FIPAException fe) {
 					fe.printStackTrace();
 				}
 			}
 		} );
 	}
 
 	public void takeDown(){
 		System.out.println(this.getName() + ": Appointment " + this.allocatedAppointment);
 	}
 
 
 }
