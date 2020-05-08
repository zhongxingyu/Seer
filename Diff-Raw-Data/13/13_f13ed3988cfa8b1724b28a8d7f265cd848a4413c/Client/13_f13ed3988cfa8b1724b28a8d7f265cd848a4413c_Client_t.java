 
 import java.rmi.Naming;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.Arrays;
 import java.util.Scanner;
 
 
 public class Client implements ClientInterface {
 	private static Client instance;
 	
 	public Client() {
 //		try {
 //			UnicastRemoteObject.exportObject(this, 0);
 //			instance = this;
 //		} catch(RemoteException ex) {
 //			System.out.println(ex.getMessage());
 //		}
 	}
 	
 	@Override
 	public void notifyMessage(String sender, String message) {
 		System.out.println("\n>>>>> " + sender + " says " + message);		
 	}
 	
 	
 	public static void main(String[] args) {
 		try {
 			ServerInterface remoteObj = (ServerInterface) Naming.lookup("rmi://127.0.0.1:9090/server");
 			
 			Scanner scanner = new Scanner(System.in);
 			System.out.println("Username: ");
 			String username = scanner.next();
 			
 			remoteObj.register(username, new Client());
 			
 			System.out.println("Users registered: " + Arrays.toString(remoteObj.getAllUser()));
 			System.out.println("exit - exit from commandline");
 			System.out.println("list - list all users");
 			System.out.println("chuser - change user");
 			
 			String text = "";
 			String receiver = null;
 			boolean userIsRegistered = false;
 			boolean closeSession = false;
 			
 			while(!closeSession) {
 				System.out.println(">>>>> ");
 				text = scanner.nextLine();
 				
 				if(text.equalsIgnoreCase("list"))
 					System.out.println("Users registered: " + Arrays.toString(remoteObj.getAllUser()));
 				else if(text.equalsIgnoreCase("chuser")) {
 					userIsRegistered = false;
 					System.out.println(">>>>> Enter User Name to chat with: ");
 					receiver = scanner.nextLine();
 					for(String name : remoteObj.getAllUser()) {
 						if(receiver.equalsIgnoreCase(name)) {
 							userIsRegistered = true;
 							break;
 						}
 					}
 					if(!userIsRegistered)
 						System.out.println("No such user logged in!");
 				} else if(text.equalsIgnoreCase("exit")) {
 					scanner.close();
 					UnicastRemoteObject.unexportObject(instance, true);
 					remoteObj.unregister(username);
 					closeSession = true;
 				} else if(userIsRegistered)
 					remoteObj.sendMessage(username, receiver, text);
 				else {
 					System.out.println("Use chuser to select user.");
 					userIsRegistered = false;
 				}
 			}
 		} catch(Exception ex) {
 			System.out.println(ex.getMessage());
 		}
 	}
 }
