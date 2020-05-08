 package client;
 
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import justen.Status;
 
 import data.Message;
 import data.Message.MESSAGE_TYPE;
 import data.MyEntry;
 import data.PropertiesOfPeer;
 
 //This will take care of each individual connection with another peer
 //It will request for a list of files that another peer has
 
 public class ConnectionInstance extends Thread {
 	private Socket socket;
 	private String ipAddress;
 	private int portNumber;
 
 	public ConnectionInstance(String ipAddress, int portNumber) throws Exception {
 		this.ipAddress = ipAddress;
 		this.portNumber = portNumber;
 	}
 
 	// Contact the peer and ask for a list of files
 	@Override
 	public void run() {
 		System.out.println("Connecting to: " + ipAddress + ", " + portNumber);
 		
 		try {
 			boolean stillTryingToConnect = PropertiesOfPeer.peerUp;
 			
 			while (stillTryingToConnect) {
 				try {
 					stillTryingToConnect = PropertiesOfPeer.peerUp;
 					socket = new Socket(ipAddress, portNumber);
 					if (socket != null) {
 						break;
 					}
 				} catch (Exception e) {
 					// handle exceptions
 					// possibly add a sleep period
 					System.err.println("Can't connect to " + ipAddress + " " + portNumber +
 							"... waiting for 5 sec");
 					Thread.sleep(5000);
 				}
 			}
 			
 			if (stillTryingToConnect == true){
 				ObjectOutputStream oos = new ObjectOutputStream(
 						socket.getOutputStream());
 				
 				PropertiesOfPeer.ipAddrPortNumMappingAlive.add(new MyEntry<String, Integer>(ipAddress, portNumber));
 				PropertiesOfPeer.broadcastStatus();
 				
 				while (stillTryingToConnect){
 					stillTryingToConnect = PropertiesOfPeer.peerUp;
 					
 					//Connection is setup
 					//Make sure the connection is still supposed to be up
 					
 					//As long as the other server is still listening
 					if (PropertiesOfPeer.CheckIfThisHostIsStillAlive(ipAddress, portNumber)){
 						//When we determine there's a new message that this peer needs to send to another peer
 						if (ClientStateManager.NewMessageToSendForPeer(ipAddress + ":" + Integer.toString(portNumber))){
 							Message sendingMessage = ClientStateManager.RetrieveMessageForPeer(ipAddress + ":" +  Integer.toString(portNumber));
 							
 							if (sendingMessage.getType() == MESSAGE_TYPE.STATUS_UPDATE){
 								Status status = (Status) sendingMessage.getData();
 								Hashtable<String, ArrayList<Integer>> versionMap = status.fileVersionMap;
 								Hashtable<String, ArrayList<Integer>> tempVersionMap = new Hashtable<String, ArrayList<Integer>>();
								
 								Iterator<Map.Entry<String, ArrayList<Integer>>> it = versionMap.entrySet().iterator();
 								while (it.hasNext()) {
 									Map.Entry<String, ArrayList<Integer>> entry = it.next();	
 									tempVersionMap.put(entry.getKey(), entry.getValue());
 								}
 								status.fileVersionMap = tempVersionMap;
 								
 								
 								sendingMessage = new Message(PropertiesOfPeer.ipAddress, PropertiesOfPeer.portNumber, MESSAGE_TYPE.STATUS_UPDATE, status);
 							}
 														
 							oos.writeObject(sendingMessage);
 						}
 					}
 					else{
 						break;
 					}
 				}
 				
 				if (PropertiesOfPeer.CheckIfThisHostIsStillAlive(ipAddress, portNumber) && !PropertiesOfPeer.peerUp){
 					Message closingMessage = new Message(PropertiesOfPeer.ipAddress, PropertiesOfPeer.portNumber, MESSAGE_TYPE.PEER_LEAVING, "shutting down connection!");
 					oos.writeObject(closingMessage);
 				}
 				
 				oos.close();
 			}
 			//socket.close();
 			
 		} catch (Exception e) {
 			System.out.println("Connection to: " + ipAddress + ":" + portNumber + " terminated");
 		}
 	}
 
 }
