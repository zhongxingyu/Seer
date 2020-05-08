 package ua.com.globallobgic.cahcemechanism;
 
 import ua.com.globallobgic.cahcemechanism.client.Client;
 import ua.com.globallobgic.cahcemechanism.client.jcl.Jcl;
 
 
 
 /**
  * @author Constantin
  */
 public class ClientMain {
 	public static void main(String[] args) {
 
 		Jcl serverJcl = new Jcl(new Client());
		Thread jclThread = new Thread(serverJcl, "Client JCL");
 
 		// Start Java Command Line for Server
 		jclThread.start();
 
 		try {
 
 			// Set main thread to wait JCL thread
 			jclThread.join();
 
 		} catch (InterruptedException e) {
 			System.err.println("join error");
 		}
 		
 	}
 }
