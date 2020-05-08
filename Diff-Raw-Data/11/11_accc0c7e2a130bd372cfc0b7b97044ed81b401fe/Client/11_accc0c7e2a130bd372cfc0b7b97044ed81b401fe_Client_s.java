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
  * 		- If they arent, throw an exception or return something special, GameLogic will deal with it from there.
  * 		- On success
  * 			Pack up all Messages due to be sent to the server and send them with a tick id of x + k, where k is
  * 			some positive integral constant.
  * 
  * @author Titus Klinge
  */
 public class Client implements MessageHandler
 {
 	public static final int K = 6;
 	private static final long POLLING_INTERVAL_MS = 5;
 	
 	private List<Message> outgoingMessages;
 	private GateKeeper gateKeeper;
 	private String serverAddress;
 	
 	private int currentTick;
 	
 	private Object tickLock = new Object();
 	
 	public Client(String serverAddress, int port) throws SocketException
 	{
 		this.serverAddress = serverAddress;
 		gateKeeper = new GateKeeper(new String[]{serverAddress}, port, port + 1);
 		
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
 		Message[] toReturn = null;
 		if(tickID < K + 1){
 			toReturn = new Message[0];
 		} else
 		{
 			toReturn = gateKeeper.urgentlyPollMessagesForTick(tickID, serverAddress);
 			while (toReturn == null)
 			{
 				try {
 					Thread.sleep(POLLING_INTERVAL_MS);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				
 				toReturn = gateKeeper.urgentlyPollMessagesForTick(tickID, serverAddress);
 			}
 		}
 		
 		//At this point we know we have received the full set of messages from the server
 		//Now we need to send the messages that have been added to the outgoing queue
 		synchronized(tickLock)
 		{
 			Message[] toSend = outgoingMessages.toArray(new Message[outgoingMessages.size()]);
 			if(toSend.length == 0){
 				toSend = new Message[1];
 				toSend[0] = new SupDawgMessage(0);	// TODO get my player id???
 			}else{
 				int breakpoint = 0;
 				breakpoint++;
 			}
 			for(int i = 0; i < toSend.length; i++){
 				toSend[i].setTimeStep(currentTick + K);
 			}
 			gateKeeper.pushMessagesForTick(toSend, serverAddress);
 			outgoingMessages.clear();
			System.out.println(currentTick);
 			++currentTick;
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
