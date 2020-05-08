 package tsa.actors;
 
import java.util.ArrayList;

 import akka.actor.ActorRef;
 import akka.actor.Actors;
 import akka.actor.UntypedActor;
 import tsa.messages.ActorTerminate;
 import tsa.messages.ArrivedAtJail;
 import tsa.messages.GoToDetention;
 
 public class Jail extends UntypedActor {
 
	private final ArrayList<ActorRef> inJail; 
 	private int jailIndex; 
 	private int numQueues;
 	private int queuesClosed;
 	
 	public Jail(int jailCapacity, int numQueues) { 
 		
		this.inJail = new ArrayList<ActorRef>();
 		this.numQueues = numQueues;
 		queuesClosed = 0;
 		jailIndex = 0; 
 	}
 	
 	public void onReceive(Object message) { 
 		if (message instanceof ArrivedAtJail) {
 			//Put Passenger in Jail List
 			ActorRef badPassenger = ((ArrivedAtJail) message).passenger;
			inJail.add(badPassenger);
 			System.out.println(badPassenger.getId() + " put in jail.");
 		}
 		
 		if (message instanceof GoToDetention) { 
 			// End of day passenger move to detention facility. 
			System.out.println("Moving " + inJail.size() + " Passengers to permanent detention facility");
			for(ActorRef person: inJail){
				person.tell(new ActorTerminate());
 			}
 			
 			// Now terminate the jail.
 			System.out.println("Terminating Jail");
 			this.getContext().tell(Actors.poisonPill());
 		}
 		
 		// Message to terminate and actor terminates itself. 
 		if (message instanceof ActorTerminate) {
 			queuesClosed++;
 			if(queuesClosed == numQueues){
 				this.getContext().tell(new GoToDetention());
 			}
 		}
 		
 	}
 }
