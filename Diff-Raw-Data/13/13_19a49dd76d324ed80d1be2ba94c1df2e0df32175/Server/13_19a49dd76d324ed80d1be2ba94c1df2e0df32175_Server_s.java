 /**
 * @Author Chris Card
 * CSCI 565 project 1
 * This file defines the rmi method sendReceive and also starts the server
 */
 
 package Server;
 
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
import Compute.Compute;
import Compute.Message;
 import java.util.concurrent.*;
 
 
 public class Server implements BulletinBoard
 {
 	//Stores all messages that where recieved from clients
 	private ConcurrentLinkedQueue<Message> message_queue;
 
 	public Server()
 	{
 		super();
 		message_queue = new ConcurrentLinkedQueue<Message>();
 	}
 
 	/**
 	* This is the rmi method the clients call to send and receive messages from the sever
 	* @param Message object that will be used to determine what the client is asking of the server
 	* @return If receiving from client it will return the message that was sent to it or null if the
 	*         message was not added to the queue.  if the client is asking for a message it will return
 	*			 the first message in the queue or null if the queue is empty.
 	*/
 	public Message sendReceive(Message msg)
 	{
 		if(msg.getMsgType().compareTo("send") == 0)
 		{
 			if(message_queue.add(msg))
 			{
 				return msg;
 			}
 			else
 			{
 				return null;
 			}
 		}
 		else
 		{
 			if(message_queue.isEmpty())
 			{
 				return null;
 			}
 			else 
 			{
 				return message_queue.poll();
 			}
 		}
 	}
 	
 	/**
 	* @param list of arguments in the following order
 	*			args[0] = socket
 	*/
 	public static void main(String[] args)
 	{
 		try{
 			String name = "Compute";
 
 			Compute engine = new Server();
 
 			Compute stub = (Compute) UnicastRemoteObject.exportObject(engine,0);
 
 			//This creates the rmiregistry so the user doesn't have to create it
 			Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
 			registry.rebind(name,stub);
 
 			//Notifies user the server was bound to the socket
 			System.out.println("Server bound to socket: "+args[0]);
 			System.out.println("EOF");
 		} catch (Exception e){
 			System.err.println("Server exception:");
 			e.printStackTrace();
 		}
 	}
 }
