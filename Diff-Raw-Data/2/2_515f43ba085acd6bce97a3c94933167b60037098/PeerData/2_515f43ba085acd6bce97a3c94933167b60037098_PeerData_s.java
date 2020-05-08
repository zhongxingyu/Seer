 package di.kdd.smartmonitor.protocol;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.TreeSet;
 
 import android.util.Log;
 
 /***
  * The data that each peer holds for each peer.
  * Thread-safe.
  */
 
 public class PeerData implements IObservable {
 	private IObserver masterNodeObserver;
 	private Set<String> peerIPs = new TreeSet<String>();
 
 	private static final String TAG = "peer data";
 	
 	/* IObservable implementation */
 	
 	@Override
 	public void subscribe(IObserver observer) {
 		this.masterNodeObserver = observer;
 	}
 
 	@Override
 	public void unsubscribe(IObserver observer) {
 		this.masterNodeObserver = null;
 	}
 
 	@Override
 	public void notify(String message) {
 		if(masterNodeObserver != null) {
 			masterNodeObserver.update(message);
 		}
 	}
 	
 	/***
 	 * Adds an IP address of a new peer
 	 * @param ip The new peer's IP address
 	 */
 	
 	public synchronized void addPeerIP(String ip) {
 		//TODO check ip validity
 
 		/* Remove the / character that is added from the socket.getInetAddress method */
 
 		if(ip.charAt(0) == '/') {
 			ip = ip.substring(1);
 		}
 		
 		if(peerIPs.contains(ip) == false) {
 			peerIPs.add(ip);	
 			
 			Log.i(TAG, "Added " + ip);
 			
 			/* Notify the Master node about the new peer's IP address
 			 * in order to broadcast the new IP to the peers.
 			 */
 			
				notify(ip);
 		}
 	}
 	
 	/***
 	 * Given a BufferedReader of a socket input stream, parses the payload 
 	 * per line and stores the IP addresses that finds
 	 * @param in Socket input stream holding the payload with the IP addresses
 	 * @throws IOException
 	 */
 	
 	public void addPeersFromMessage(Message message) throws IOException {
 		String peerDataLine;
 		Scanner in = new Scanner(message.getPayload());
 
 		while(in.hasNextLine()) {
 			peerDataLine = in.nextLine();
 			addPeerIP(peerDataLine);
 			
 			Log.i(TAG, "Added " + peerDataLine + " from message");
 		}
 	}
 	
 	/***
 	 * Removes the IP address of a fallen peer
 	 * @param ip The IP address to remove
 	 */
 	
 	public synchronized void removePeerIP(String ip) {
 		peerIPs.remove(ip);
 		
 		Log.i(TAG, "Removed peer IP addres: " + ip);
 	}
 	
 	/***
 	 * Returns the lowest IP. Must be used in order to find
 	 * the new Captain in case of Captain node failure.
 	 * @return The lowest of the peer IP addresses.
 	 */
 	
 	public synchronized String getLowestIP() {
 		return Collections.min(peerIPs);
 	}
 		
 	/***
 	 * Return the IP addresses of the peers, separated by a new line character
 	 */
 	
 	@Override
 	public synchronized String toString() {
 		String string = new String();
 		
 		for(String ip : peerIPs) {
 			string += ip + '\n';
 		}
 		
 		return string;
 	}
 }
