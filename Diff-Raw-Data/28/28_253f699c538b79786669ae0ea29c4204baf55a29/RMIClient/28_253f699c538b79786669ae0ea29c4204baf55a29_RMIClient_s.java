 package client.system;
 
 import java.net.MalformedURLException;
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RMISecurityManager;
 import java.rmi.RemoteException;
 
 public class RMIClient {
 	private String server = "localhost:1099";
 
 	// public StorageI eventStorage;
 
 	public RMIClient() {
 		System.setProperty("java.security.policy", "config/openall.policy");
 
 		// Initialize Security Manager
 		if (System.getSecurityManager() == null) {
 			System.setSecurityManager(new RMISecurityManager());
 		}
 
 		// this.eventStorage = (StorageI)getRMIObjectFromServer("EventStorage");
 		// System.out.println(eventStorage.getValue());
 	}
 
 	public RMIClient(String address) {
 		this();
 		server = address;
 	}
 
 	public Object getObject(String objectname) {
 		String urlServer = new String("rmi://" + server + "/" + objectname);
 
 		// Bind to RMIServer
 		try {
 			return (Object) Naming.lookup(urlServer);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			// TODO Auto-generated catch block
 			throw new RuntimeException(e);
 		}
 	}
 }
