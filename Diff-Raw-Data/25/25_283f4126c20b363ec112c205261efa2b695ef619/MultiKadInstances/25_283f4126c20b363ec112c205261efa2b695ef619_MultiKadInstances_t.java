 package org.peerbox.demo;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.peerbox.rpc.RPCEvent;
 import org.peerbox.rpc.RPCHandler;
 import org.peerbox.rpc.RPCResponseListener;
 
 public class MultiKadInstances {
 	private static int numInstances;
 	private static String bindIP = null;
 	private static int firstBindPort;
 	private static List<URI> bootstrapURI = new LinkedList<URI>();
 	private static HashMap<Integer, KadInstance> kadInstances = new HashMap<Integer, KadInstance>();
 	
 	public static void main(String[] args){
 		final RPCHandler rpc;
 		try{			
 			if(args.length >= 3){
 				for(int i = 2; i < args.length; i++){
 					bootstrapURI.add(new URI("udp://"+args[i]));
 				}
 			}
 			if(args.length < 2){
 				printUsageAndExit();
 			}
 			else if(args.length >= 2){
 				numInstances = Integer.parseInt(args[0]);
 				if(args[1].contains(":")){
 					bindIP = args[1].substring(0, args[1].indexOf(":"));
 					firstBindPort = Integer.parseInt(args[1].substring(args[1].indexOf(":") + 1));
 					createInstances(null);
 				}
 				else{
 					firstBindPort = Integer.parseInt(args[1]);
 					rpc = RPCHandler.getUDPInstance(firstBindPort);
 					rpc.sendRequest(new URI("udp://peerbox.org:20000"), "ipaddress", "", new RPCResponseListener(){
 
 						@Override
 						public void onResponseReceived(RPCEvent event) {
 							bindIP = event.getDataString();
 							createInstances(rpc);
 						}
 
 						@Override
 						public void onTimeout() {
 							System.out.println("Could not obtain external IP");
 							System.exit(0);
 						}
 						
 					});
 				}
 			}
 		}
 		catch(Exception e){
 			printUsageAndExit();
 		}
 	}
 
 	private static void createInstances(RPCHandler rpc) {
 		for (int i = 0; i < numInstances; i++) {
 			try {
				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			KadInstance kadInstance;
 			int bindPort = firstBindPort + i;
 			if(rpc == null){
 				kadInstance = new KadInstance(bindIP, bindPort, bootstrapURI);
 			}
 			else{
 				kadInstance = new KadInstance(rpc, bindIP, bindPort, bootstrapURI);
 			}
 			kadInstances.put(bindPort, kadInstance);
 			new Thread(kadInstance).run();
 			bootstrapURI.add(kadInstance.getURI());
 		}		
 	}
 
 	private static void printUsageAndExit() {
 		System.out.println("Usage: numInstances [bind ip]:port [bootstrap ip:port]");
 		System.exit(0);
 	}
 }
