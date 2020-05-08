 // BagScan
 package tsa.actors;
 
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Random;
 
 import tsa.messages.ScanBag;
 import tsa.messages.ScanBagResults;
 import tsa.messages.TimeBagMessage;
 import akka.actor.ActorRef;
 import akka.actor.Actors;
 import akka.actor.UntypedActor;
 import akka.actor.UntypedActorFactory;
 import tsa.messages.ActorTerminate;
 
 public class BagScan extends UntypedActor {
 	
 	private final int number;
 	private final ActorRef security;
 	private final ActorRef bagTimer;
 	private Queue<ActorRef> passengerBags = new LinkedList<ActorRef>();
 	
 	public BagScan(int number, ActorRef security) {
 		bagTimer = Actors.actorOf(BagTimer.class);
		bagTimer.start();
 		
 		this.number = number;
 		this.security = security;
 		
 		this.getContext().setId("BagScan-" + Integer.toString(this.number));
 	}
 
 	@Override
 	public void onReceive(Object message) {
 		if (message instanceof ScanBag) {
 			// Baggage randomly fails inspection with a probability of 20%.
 			ActorRef passenger = ((ScanBag) message).passenger;
 			passengerBags.add(passenger);
 			
 			// Bag processing takes a random amount of time, between 200 and 300 millis.
 			Random rand = new Random();
 			long whenToWakeUp = System.currentTimeMillis() + 200 + rand.nextInt(100);
 			// Tell BagTimer actor.
 			bagTimer.tell(new TimeBagMessage(whenToWakeUp, this.getContext()));
 		}
 		
 		if(message instanceof String && ((String) message).equals("finished")){
 			ActorRef passenger = passengerBags.remove();
 			boolean passed = (Math.random() < 0.8);
 			
 			if (passed) {
 				System.out.println(passenger.getId() + ": Passed BagScan-" + number);
 			} else {
 				System.out.println(passenger.getId() + ": Failed BagScan-" + number);
 			}
 
 			ScanBagResults resultsMessage = 
 					new ScanBagResults(passenger, passed);
 			security.tell(resultsMessage);
 			
 		}
 		
 		//Message to terminate and actor terminates itself. 
 		if (message instanceof ActorTerminate) { 
 			
 			//Try and tell the security to die. If already dead then it will 
 			//throw an exception because it can't tell it to die. Catch the exception
 			//and print info message. 
 			try { 
 				security.tell(new ActorTerminate());
 			} catch (Exception excep) { 
 				System.out.println("Security Actor already terminated OR there is another error.");
 			}
 			this.getContext().tell(Actors.poisonPill());
 		}
 	}
 
 }
