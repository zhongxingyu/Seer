 package testlets;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.LinkedList;
 
 import kademlia.BootstrapListener;
 import kademlia.NetworkInstance;
 import rpc.RPCHandler;
 
 public class KadThisBetterWork {
 	public static void main(String[] args) {
		int port = 7010;
 		LinkedList<NetworkInstance> networkInstances = new LinkedList<NetworkInstance>();
 		NetworkInstance instance = new NetworkInstance(RPCHandler.getUDPInstance(port++));
 		networkInstances.add(instance);
 		System.out.println("First node created");
 		
 		try {
 			Thread.sleep(400);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		for (int i = 0; i < 1; i++) {
 			instance = new NetworkInstance(RPCHandler.getUDPInstance(port++));
 			LinkedList<URI> startURIs = new LinkedList<URI>();
 			try {
 				startURIs.add(new URI("udp://localhost:" + (port - 1)));
 			} catch (URISyntaxException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			System.out.println("Second node created");
 			instance.bootstrap(startURIs, new BootstrapListener() {
 
 				@Override
 				public void onBootstrapSuccess() {
 					System.out.println("Second Node Bootstrap Complete");
 					
 				}
 
 				@Override
 				public void onBootstrapFailure() {
 					System.out.println("Second Node Bootstrap Failed");
 					
 				}
 				
 			});
 			networkInstances.add(instance);
 		}
 		
 	}
 }
