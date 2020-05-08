 import java.util.concurrent.ConcurrentLinkedQueue;
 import akka.actor.ActorRef;
 
 /**
  * Queue represents the line of bags and persons in this security station 
  * waiting to be processed by the various scanners. Incoming Persons are 
  * decoupled from their bags and sent to different queues. As space becomes
  * available in the bag and body scan, the first item from each respective 
  * queue is pulled and sent top the appropriate scanner to be checked.    
  * 
  * @author Chris
  * @author Carol
  * @author Andrew Lyne
  */
 public class Queue extends AbstractActor {
 
 	/**
 	 * BaggageScanner to send bags to when ready. 
 	 */
 	final ActorRef baggageScanner; 
 	
 	/**
 	 * BodyScanner to send Persons to when ready. 
 	 */
 	final ActorRef bodyScanner;
 	
 	/**
 	 * Integer representing what number security station this queue belongs to.
 	 */
 	private final int stationNumber;
 	
 	/**
 	 * Queue containing Persons waiting to enter the BodyScanner
 	 */
 	private ConcurrentLinkedQueue<Person> bodyQueue;
 	
 	/**
 	 * Queue containing Baggage waiting to enter the BagScanner
 	 */
 	private ConcurrentLinkedQueue<Baggage> baggageQueue;
 	
 	/**
 	 * Boolean representing if the BaggageScan is ready for another bag
 	 */
 	private boolean bagScanReady;
 	
 	/**
 	 * Boolean representing if the BodyScan is ready for another Person 
 	 */
 	private boolean bodyScanReady;
 	
 	/**
 	 * Constructor for Queue. 
 	 * 
 	 * @param stationNumber - what number security station this queue belongs 
 	 * 		to
 	 * @param bodyScan - BodyScanner Actor to send People to
 	 * @param bagScan - BaggageScanner Actor to send Baggage to
 	 * @param terminal - Terminal Actor to send log messages to
 	 */
 	public Queue(int stationNumber, ActorRef bodyScan, ActorRef bagScan, ActorRef terminal){
 		super(ActorFactory.QUEUE_SPACE, terminal);
 		this.stationNumber = stationNumber;
 		baggageScanner = bagScan;
 		bodyScanner = bodyScan;
 		bodyQueue = new ConcurrentLinkedQueue<Person>();
 		baggageQueue = new ConcurrentLinkedQueue<Baggage>();
 		bodyScanReady = true;
 		bagScanReady = true;
 	}
 	/**
 	 * Redefinition of OnRecieve method from Actor. This class handles messages
 	 * of EndDay, NextBag, NextBody and Person types.  
 	 */
 	@Override
 	public void onReceive(Object message){
 		/*
 		 * If instance of Person, decouple the Person form their Baggage and 
 		 * place each of these into the respective queues with recievePerson().
 		 * Afterwards check to see if either the bag or body scan is ready to 
 		 * receive input. If so, send the appropriate person to the appropriate
 		 * open scanner. 
 		 */
 		if (message instanceof Person) {
 			recievePerson( (Person)message );
 			if( bagScanReady && !baggageQueue.isEmpty()){
 				sendTopBagToScan();
 			}
 			if( bodyScanReady && !bodyQueue.isEmpty()){
 				sendTopBodyToScan();
 			}
 		}
 		/*
 		 * Message signaling that the baggageScanner is ready for input. If 
 		 * there is Baggage in the bodyQueue, send that Baggage to the 
 		 * baggegeScanner. Otherwise do nothing. 
 		 */
 		else if( message instanceof NextBag){
 			bagScanReady = true;
 			if( !baggageQueue.isEmpty()){
 				sendTopBagToScan();
 			}
 		}
 		/*
 		 * Message signaling that the bodyScanner is ready for input. If there 
 		 * is a Person in the bodyQueue, send that person to the bodyScanner.
 		 * Otherwise do nothing. 
 		 */
 		else if( message instanceof NextBody){
 			bodyScanReady = true;
 			if( !bodyQueue.isEmpty()){
 				sendTopBodyToScan();
 				bodyScanReady = false;
 			}
 		}
 		/*
 		 * If instance of EndDay message, check to see that both the 
 		 * baggageQueue and bodyQueue are empty. If so, pass the message 
 		 * along to the bodyScanner and BagScanner then shut self down. If 
 		 * not, resend the EndOfDay message to self. This will delay shutting 
 		 * down until all bags and people assigned to this queue have been 
 		 * processed.  
 		 */
 		else if( message instanceof EndDay){
			printToTerminal("Queue " + stationNumber + "received end of day message");
			baggageScanner.tell((EndDay)message);
			bodyScanner.tell((EndDay)message);
			getContext().stop();
 		}
 		
 		/*
 		 * All other messages are errors. Message printed here for debugging
 		 * purposes.
 		 */
 		else{
 			System.err.println("Queue recieved invalid message: " + 
 					message.toString());
 		}
 	}
 
 	/**
 	 * Override of default stop function in actor. Prints a message to 
 	 * Terminal actor before shutting down.  
 	 */
 	@Override
 	public void postStop() {
 		printToTerminal( "Queue " + stationNumber + "Closed" );
 	}
 	
 	/**
 	 * Receives the passed Person argument into the security station. This 
 	 * involves decoupling the Person from their Baggage and placing each into
 	 * separate queues.   
 	 * 
 	 * @param p - Person to enter into station
 	 * @return True if successful. False otherwise
 	 */
 	public boolean recievePerson(Person p){
 		printToTerminal("Person: " + p.getPersonId() + "enters queue " 
 				+ stationNumber + ".");
 		boolean personStatus, bagStatus = false;
 		personStatus = bodyQueue.add(p);
 		bagStatus = baggageQueue.add(p.getBaggage());
 		return (personStatus && bagStatus);
 	}
 	
 	/**
 	 * Sends the first Baggage item in the baggageQueue to the BagScanner
 	 */
 	public void sendTopBagToScan(){
 		Baggage toSend;
 		toSend = baggageQueue.poll();
 		printToTerminal("Person " + toSend.getOwner().getPersonId() 
 				+ "'s baggage sent to baggage scanner");
 		bagScanReady = false;
 		baggageScanner.tell(toSend);
 	}
 	
 	/**
 	 * Sends a person object to the body scanner
 	 */
 	public void sendTopBodyToScan() {
 		Person toSend;
 		toSend = bodyQueue.poll();
 		printToTerminal("Person " + toSend.getPersonId() 
 				+ " sent to body scanner");
 		bodyScanReady = false;
 		bodyScanner.tell(toSend);
 	}
 }
