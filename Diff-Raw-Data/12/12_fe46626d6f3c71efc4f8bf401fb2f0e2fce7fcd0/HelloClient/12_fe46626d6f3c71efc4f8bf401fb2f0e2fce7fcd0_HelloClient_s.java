 package rich.rmi;
 
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 
 public class HelloClient {
 
     private HelloClient() {}
 
     public static void main(String[] args) {
 
 	String host = (args.length < 1) ? null : args[0];
 	try {
 	    Registry registry = LocateRegistry.getRegistry("localhost");
	    Hello stub = (Hello) registry.lookup("rmi://127.0.0.1:1099/Hello");
 	    String response = stub.sayHello();
 	    System.out.println("response: " + response);
 	} catch (Exception e) {
 	    System.err.println("Client exception: " + e.toString());
 	    e.printStackTrace();
 	}
     }
 }
 
