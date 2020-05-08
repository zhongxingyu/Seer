 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 public class Tracker extends Thread {
 	private static List<WaitingPeer> serverPeers = new ArrayList<WaitingPeer>();
 	private static String myAddressAsClientSees;
 	private static int port;
 	private static DatagramSocket socket;
 	private static byte[] buf = new byte[1000];
 	private static DatagramPacket packet = new DatagramPacket(buf, buf.length);
 	private static GUI guiObject;
 	private static boolean notStopped;
 	
 	private Map<WaitingPeer,String> gameIds;
 	
 	public Tracker( GUI guiObject){
 		this.guiObject = guiObject;
 		this.gameIds = new HashMap<WaitingPeer, String>();
 	}
 	
 	public void setPort(int port){
 		this.port = port;
 	}
 	
 	private static boolean isInTheList(WaitingPeer waitingP){
 		String address = waitingP.address;
 		for(WaitingPeer wpe : serverPeers){
 			if(wpe.address.equalsIgnoreCase(address))
 				return true;
 		}
 		return false;
 	}
 	
 	private static void sendBack(boolean isServer, WaitingPeer wp, String gameId) throws IOException{
 		String dString = isServer+"!"+((wp.address.equals("127.0.0.1")) ? myAddressAsClientSees : wp.address)+"!"+gameId+"!Alive_________";
         buf = dString.getBytes();
         
 		packet = new DatagramPacket(buf, buf.length, packet.getAddress(), packet.getPort());
         socket.send(packet);
 
         guiObject.outputLog("   Assigned : "+(isServer?"Server":"Client"));
 	}
 	
 	public void stopTracker(){
 		notStopped = false;
 		guiObject.outputLog("Tracker Stopped");
 	}
 	
 	public void run() {
 		guiObject.outputLog("Start listenning on port "+port+"...");
 
 		notStopped = true;
 		try {
 			socket = new DatagramSocket(port);
 		
 		    socket.setSoTimeout(500);
 
 		while(notStopped) {
 			try{
 			socket.receive(packet);
 			String receivedMSG = new String(packet.getData(), 0,
 					packet.getLength());
 			guiObject.outputLog(receivedMSG);
 			myAddressAsClientSees = receivedMSG.split("!")[0];
 			guiObject.outputLog(myAddressAsClientSees);
 			WaitingPeer wp = new WaitingPeer();
 			wp.address = packet.getAddress().getHostAddress();
 			guiObject.outputLog("Request from "+wp.address);
 			
 			if(serverPeers.size() != 0){
 				if(isInTheList(wp)){
 					sendBack(true, wp,gameIds.get(wp));
 				}else{
 					WaitingPeer waitingPeer = serverPeers.remove(0);
					String gameId = gameIds.remove(waitingPeer);
 					sendBack(false, waitingPeer,gameId);
 				}
 			}else if(serverPeers.size()==0){
 				serverPeers.add(wp);
 				String gameId = newGameId();
 				gameIds.put(wp, gameId);
 				sendBack(true, wp,gameId);
 			}
 			}catch(SocketTimeoutException e){
 				//guiObject.outputLog("patiently waiting");
 			}
 		}
 		guiObject.toggleButtons();
 		socket.close();
 		} catch (SocketException e) {
 			System.err.println("ERROR: Exception happened while trying to listen on the port "+port+". Probably another program is listening on the same port or you don't have enough permissions");
 		} catch (IOException e) {
 			System.err.println("ERROR: Exception happened trying to send and recieve data, check your connection please.");
 			socket.close();
 		}
 		
 	}
 
 	private String newGameId() {
 		Set<String> knownGameIds = new HashSet<String>(this.gameIds.values());
 		String newGameId = generateId(10);
 		while(knownGameIds.contains(newGameId)){
 			newGameId = generateId(10);
 		}
 		return newGameId;
 	}
 	
 	public static String generateId(int size){
 		char[] c = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
 		StringBuilder sb = new StringBuilder();
 		Random random = new Random();
 		for (int i = 0; i < size; i++) {
 		    char ch = c[random.nextInt(c.length)];
 		    sb.append(ch);
 		}
 		String output = sb.toString();
 		return output;
 		
 	}
 
 }
