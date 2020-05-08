 package remote;
 
 import java.net.InetAddress;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 
 import message.Message;
 import server.Server;
 
 public class RmiServer extends java.rmi.server.UnicastRemoteObject implements ReceiveMessageInterface {
 
     /**
 	 * 
 	 */
 	private static final long serialVersionUID = -2401572816494063347L;
 	int thisPort;
     String thisAddress;
     Registry registry;    // rmi registry for lookup the remote objects.
     
     Server server;
 
     // This method is called from the remote client by the RMI.
    // This is the implementation of the ReceiveMessageInterface.
 	public boolean sendMessageToClient(String username, String devicename, Message m) throws RemoteException {
 		return server.sendMessageToClient(username, devicename, m);
 	}
 
     public RmiServer(Server server) throws RemoteException {
         this.server = server;
     	
     	try {
             // get the address of this host.
             thisAddress= (InetAddress.getLocalHost()).toString();
         }
         catch(Exception e) {
             throw new RemoteException("can't get inet address.");
         }
 
        thisPort=10500;  // this port(registrys port)
 
         System.out.println("this address="+thisAddress+",port="+thisPort);
 
         try {
         	// create the registry and bind the name and object.
         	registry = LocateRegistry.createRegistry( thisPort );
             registry.rebind("rmiServer", this);
         }
         catch(RemoteException e){
         	throw e;
         }
     }
 }
