 package linewars.network;
 
 import java.net.SocketException;
 import java.util.ArrayList;
 
 import linewars.network.messages.Message;
 
 /**
  * Like Client, except it's a server.
  * 
  * Polls (Lazily ask) the Gatekeeper for Messages for the current tick x from the set of players from whom the server
  * hasn't heard this tick.
  * 		- If the Gatekeeper doesn’t get all of the Messages for the current tick within a certain time window, the
  * 		  server starts urgently polling
  * 
  * Once all of the Messages associated with some tick id have been received, send them all out to each Client.
  * 
  * 
  * @author Titus Klinge
  */
 public class Server implements Runnable
 {
 	private static final int MAX_NUM_LAZY_CHECKS = 10;
 	private static final long POLLING_INTERVAL_MS = 5;
 	
 	private String[] clientAddresses;
 	private GateKeeper gateKeeper;
 	
 	private int currentTick;
 	private Message[][] messagesForTick;
 	
 	public Server(String[] clientAddresses, int port) throws SocketException{
 		this.clientAddresses = clientAddresses.clone();
		gateKeeper = new GateKeeper(port + 1, port);//TODO retry if it fails?
 		
 		currentTick = 1;
 	}
 	
 	@Override
 	public void run()
 	{
 		//start Gatekeeper in a new thread
 		//TODO or should this be done elsewhere?
 		new Thread(
 			new Runnable(){
 			public void run(){
 				gateKeeper.startListening();
 			}
 		}).start();
 		
 		while(true){
 			doTick();
 		}
 	}
 	
 	/**
 	 * Handles receiving and distributing a single tick's worth of Messages.
 	 */
 	private void doTick(){
 		//setup
 		messagesForTick = new Message[clientAddresses.length][];
 		
 		//poll lazily until some messages are found//////////////////////////////////////////////////////////////////
 		getInitialMessageSet();
 		//send messages if they were all found
 		if(allMessagesFound()){
 			sendMessages();
 			finalizeTick();
 			return;			
 		}
 		
 		//poll lazily for a certain amount of time after that or until everything is found///////////////////////////
 		getAllMessagesLazily();
 		//send messages if they were all found
 		if(allMessagesFound()){
 			sendMessages();
 			finalizeTick();
 			return;			
 		}
 		
 		//some data is late, poll urgently until everything is found/////////////////////////////////////////////////
 		getAllMessagesUrgently();
 		//send messages if they were all found
 		if(allMessagesFound()){
 			sendMessages();
 			finalizeTick();
 			return;			
 		}
 	}
 	
 	/**
 	 * Checks if all messages have been found for this tick.
 	 * @return
 	 * true iff all messages have been received for this tick, false otherwise
 	 */
 	private boolean allMessagesFound() {
 		for(int i = 0; i < clientAddresses.length; i++){
 			if(messagesForTick[i] == null){
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Urgently polls the GateKeeper for the Messages from each client associated with the current tickID.
 	 */
 	private void getAllMessagesUrgently() {
 		while(true){//TODO give up eventually?
 			boolean hasAllMessages = true;
 			for(int i = 0; i < clientAddresses.length; i++){
 				messagesForTick[i] = gateKeeper.urgentlyPollMessagesForTick(currentTick, clientAddresses[i]);
 				if(messagesForTick[i] == null){
 					hasAllMessages = false;
 				}
 			}
 			if(hasAllMessages){
 				return;
 			}
 			//sleep so the thread isn't going nuts
 			try {
 				Thread.sleep(POLLING_INTERVAL_MS);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Lazily polls the GateKeeper for the Messages from each client associated with the current tickID.
 	 * 
 	 * This method will poll for POLLING_INTERVAL_MS * MAX_NUM_LAZY_CHECKS milliseconds before giving up and returning.
 	 */
 	private void getAllMessagesLazily() {
 		//Check lazily for a while first
 		for(int c = 0; c < MAX_NUM_LAZY_CHECKS; c++){
 			boolean hasAllMessages = true;
 			for(int i = 0; i < clientAddresses.length; i++){
 				messagesForTick[i] = gateKeeper.pollMessagesForTick(currentTick, clientAddresses[i]);
 				if(messagesForTick[i] == null){
 					hasAllMessages = false;
 				}
 			}
 			
 			if(hasAllMessages){
 				return;
 			}
 			
 			//sleep so the thread isn't going nuts
 			try {
 				Thread.sleep(POLLING_INTERVAL_MS);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Lazily polls the GateKeeper for the Messages from each client associated with the current tickID.
 	 * 
 	 * This method will poll until it receives a full set of Messages from any client.
 	 */
 	private void getInitialMessageSet() {
 		boolean messagesFound = false;
 		while(!messagesFound){
 			for(int i = 0; i < clientAddresses.length; i++){
 				messagesForTick[i] = gateKeeper.pollMessagesForTick(currentTick, clientAddresses[i]);
 				if(messagesForTick[i] != null){//if any messages were found
 					return;
 				}
 			}
 			
 			//sleep so the thread isn't going nuts
 			try {
 				Thread.sleep(POLLING_INTERVAL_MS);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Readies the Server for the start of the next tick.
 	 */
 	private void finalizeTick() {
 		messagesForTick = null;
 		currentTick++;
 	}
 
 	/**
 	 * Pushes all of the received Messages out to each client
 	 */
 	private void sendMessages() {
 		//Compile the messages received this tick into one big list
 		ArrayList<Message> allMessages = new ArrayList<Message>();
 		for(int i = 0; i < clientAddresses.length; i++){
 			for(Message toAdd : messagesForTick[i]){
 				allMessages.add(toAdd);
 			}
 		}
 		
 		//send the list out to everyone!
 		for(int i = 0; i < clientAddresses.length; i++){
 			gateKeeper.pushMessagesForTick(allMessages.toArray(new Message[0]), clientAddresses[i]);
 		}
 	}
 
 }
