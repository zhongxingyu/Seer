 package linewars.network;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import linewars.network.messages.Message;
 
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
 public class Client
 {
 	private List<Message> messages;
 	private GateKeeper gateKeeper;
 	
 	public Client()
 	{
 		messages = new LinkedList<Message>();
 	}
 	
 	public void addMessage(Message msg)
 	{
 		messages.add(msg);
 	}
 	
 	public Message[] getAllMessages()
 	{
		return null;
 	}
 	
 	public Message[] getLocalMessages()
 	{
 		return messages.toArray(new Message[messages.size()]);
 	}
 }
