 package rich.rmi;
 
 import java.rmi.registry.Registry;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.Naming;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 	
 public class HelloServer implements Hello {
 	
     public HelloServer() {}
 
     public String sayHello() {
 	return "Hello, world!";
     }
 	
     public static void main(String args[]) {
 	
 	try {
 		HelloServer obj = new HelloServer();
 //	    Hello stub = (Hello) UnicastRemoteObject.exportObject(obj, 0);
 
 	    LocateRegistry.createRegistry(1099);
 	    // Bind the remote object's stub in the registry
 	    Registry registry = LocateRegistry.getRegistry();
	    String url = "rmi://127.0.0.1:1099/Hello";
 	    registry.rebind(url, obj);
 
 	    System.err.println("Server ready");
 	} catch (Exception e) {
 	    System.err.println("Server exception: " + e.toString());
 	    e.printStackTrace();
 	}
     }
 }
 
