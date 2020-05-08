 package clientRMI;
 
 import java.rmi.RemoteException;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 
 import chatJSP.ChatServlet;
 
 import server.ClientOperations;
 
 public class Client extends UnicastRemoteObject implements ServerOperations{
 	String user;
 	ClientOperations mainServer=null;
 	Registry registry = null;
 
 	public Client(Registry registry, ClientOperations mainServer, String user) throws RemoteException {
 		super();
 		this.registry = registry;
 		this.mainServer = mainServer;
 		this.user = user;
 	}
 
 	
 	@Override
 	public void printUserMessage(String msg) throws RemoteException {
 		ChatServlet.sendMessage(msg, user);
 	}
 
 	@Override
 	public boolean testUser() throws RemoteException {
 		return true;
 	}
 	
	public String getUsername(){
		return user;
	}
	
 }
