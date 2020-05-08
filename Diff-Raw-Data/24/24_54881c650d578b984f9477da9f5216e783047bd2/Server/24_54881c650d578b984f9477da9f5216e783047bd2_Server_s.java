 import java.rmi.AlreadyBoundException;
 import java.rmi.Naming;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.Arrays;
 
 
 public class Server implements ServerInterface {
 	private UserStore userStore = new UserStore();
 	
 	public UserStore getUserStore() {
 		return userStore;
 	}
 
 	@Override
 	//Angemeldeten User speichern und begrüßen
 	public void register(String userName, ClientInterface clientObject) {
 		this.getUserStore().addUser(userName, clientObject);
 		
 		try {
 			clientObject.notifyMessage("Server", "Welcome " + userName);
 		} catch(RemoteException ex) {
 			System.out.println(ex.getMessage());
 		}
 	}
 	
 	@Override
 	//User abmelden
 	public void unregister(String username) {
 		this.getUserStore().removeUser(username);
 	}
 
 	@Override
 	// alle angemeldeten User abfragen
 	public String[] getAllUser() {
 		return this.getUserStore().getAllUser();
 	}
 
 	@Override
 	//Nachricht von User an anderen User weiterleiten
 	public void sendMessage(String sender, String receiver, String message) throws RemoteException {
 		ClientInterface client = this.getUserStore().getClientByName(receiver);
 		
 		if(client != null)
 			client.notifyMessage(sender, message);
 	}
 	
 	
 	public static void main(String[] args) {
 		Server server = new Server();
 		
 		try {
 			ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(server, 0);
			Naming.bind("rmi://127.0.0.1:9090/server", stub);
 			
 			System.out.println("Server binded object successfull!");
 			
 			while(server.userStore.getAllUser().length == 0) {
 				System.out.println("No clients registered!");
 				Thread.sleep(5000);
 			}
 			
 			while(true) {
 				System.out.println("Users registered: " + Arrays.toString(server.userStore.getAllUser()));
 				Thread.sleep(15000);
 			}
 			
 		} catch(Exception ex) {
 			System.out.println(ex.getMessage());
 		}
 	}
 	
 }
