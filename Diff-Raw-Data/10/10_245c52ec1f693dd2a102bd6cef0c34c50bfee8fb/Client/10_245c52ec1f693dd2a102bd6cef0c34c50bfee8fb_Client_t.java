 /**
 * @Author Chris Card
 * 9/16/13
 * This Contains code for the clients
 */
 
 package Client;
 
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import Compute.BulletinBoard;
 import Compute.Article;
 import java.io.*;
 
 public class Client
 {
 	
 	/**
 	* @param String list of arguments that must be in following order: 
 	*        1 = hostname
 	*			2 = socket
 	*			3 = send | receive
 	*			4 = message file if send
 	*/
 	public static void main(String args[])
 	{
 		String host="",sendReceive="",message="";
 		int port = 5555;
 		if(args.length >= 3)
 		{
 			host = args[0];
 			try{
 				port = Integer.parseInt(args[1]);
 			} catch(Exception e){
 				System.err.println("Invalide port");
 				e.printStackTrace();
 			}
 			sendReceive = args[2];
 			
			//message = (args.length == 4 ? makeMessageText(args[3]) : "");
 		}
 		else{
 			for(int i = 0; i < args.length; i++)
 			{
 				System.out.println(args[i]);
 			}
 			System.err.println("Invalide args");
 		}
 		try {
 			String name = "Compute";
 
 			Registry registry = LocateRegistry.getRegistry(host,port);
			BulletinBoard comp = (BulletinBoard) registry.lookup(name);
 
			//Messages task = new Messages(message,sendReceive);
			//Message ret = comp.sendReceive(task);
 
 			
 		}catch(Exception e){
 			System.err.println("Client exception:");
 			e.printStackTrace();
 		}
 	}
 }
