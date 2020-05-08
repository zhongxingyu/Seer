 package linewars.network;
 
 import java.net.SocketException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import linewars.network.messages.Message;
 import linewars.network.messages.SupDawgMessage;
 
 /**
  * Encapsulates the process of collating and distributing the Messages - in both
  * directions - as needed on the client side.
  * 
  * Receives Messages from Display and stores them
  * 
  * Urgently polls Gatekeeper for Messages from the server when GameLogic asks for its Messages
  * 
  * GameLogic says ‘tick x is starting, give me my messages!’
  * 		- Urgently ask the Gatekeeper for the full set of all messages for tick x from the server.
  * 		- If the messages are all there, give them to GameLogic
  * 		- If they aren’t, throw an exception or return something special, GameLogic will deal with it from there.
  * 		- On success
  * 			Pack up all Messages due to be sent to the server and send them with a tick id of x + k, where k is
  * 			some positive integral constant.
  * 
  * @author Titus Klinge
  */
 public class Client implements MessageProvider, MessageReceiver, Runnable
 {
 	private static final int K = 6;
 	
	private List<Message> outgoingMessages;
 	private GateKeeper gateKeeper;
 	private String serverAddress;
 	
 	private int currentTick;
 	
 	private Object tickLock = new Object();
 	
 	public Client(String serverAddress, int port) throws SocketException
 	{
 		this.serverAddress = serverAddress;
 		gateKeeper = new GateKeeper(port);
 		
 		currentTick = 1;
		outgoingMessages = new LinkedList<Message>();
 	}
 	
 	@Override
 	public void addMessage(Message msg)
 	{
 		synchronized(tickLock)
 		{
			outgoingMessages.add(msg);
 		}
 	}
 
 	//TODO I changed this method a bit; it now sets the Messages' time steps itself
 	//and it also tells the Gatekeeper where to send the Messages and where the Messages should have
 	//come from (The Server demands this functionality)
 	@Override
 	public Message[] getMessagesForTick(int tickID)
 	{
 		Message[] toReturn = gateKeeper.urgentlyPollMessagesForTick(tickID, serverAddress);
 		
		if(toReturn == null){
			return toReturn;
		}
		
		//At this point we know we have received the full set of messages from the server
		//Now we need to send the messages that have been added to the outgoing queue
 		synchronized(tickLock)
 		{
			Message[] toSend = outgoingMessages.toArray(new Message[outgoingMessages.size()]);
			for(int i = 0; i < toSend.length; i++){
				toSend[i].setTimeStep(currentTick + K);
 			}
			gateKeeper.pushMessagesForTick(toSend, serverAddress);
 			currentTick++;
 		}
 		
 		return toReturn;
 	}
 
 	@Override
 	public void run()
 	{
 		// NOTE this method steals this thread!  the start listening uses
 		// the current thread to do the listening.
 		gateKeeper.startListening();
 	}
 }
