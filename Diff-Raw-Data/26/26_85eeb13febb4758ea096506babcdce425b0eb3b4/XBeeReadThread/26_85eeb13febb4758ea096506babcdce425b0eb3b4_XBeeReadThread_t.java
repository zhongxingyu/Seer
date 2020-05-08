 package ie.wombat.ha.nic.xbee;
 
 
 import ie.wombat.ha.ByteFormatUtils;
import ie.wombat.ha.NetworkMonitor;
 import ie.wombat.ha.nic.APIFrameListener;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 /**
  * This thread listens to the XBee UART for an incoming API packet and 
  * invokes any listening {@link APIFrameListener} objects.
  * 
  * TODO: does this correctly reset if START_OF_PACKET received unexpectedly?
  * 
  * TODO: much in common with {@link ZStackReadThread} (except for the readAPIFrameFromStream() method).
  * Can we refactor to make this common code?
  * @author joe
  *
  */
 public class XBeeReadThread extends Thread implements XBeeConstants {
 
 	private static Logger log = Logger.getLogger(XBeeReadThread.class);
 
 	private InputStream xbeeIn;
 	private byte[] packet = new byte[256];
 	private List<APIFrameListener> listeners = new ArrayList<APIFrameListener>();
 	
 	/**
 	 * 
 	 * @param in The input stream corresponding to the XBee UART out for which to listen 
 	 * for XBee API packets.
 	 * 
 	 */
 	public XBeeReadThread (InputStream in) {
 		super();
 		this.xbeeIn = in;
 	}
 	
 	public void run() {
		try {
			runLoop();
		} catch (Error e) {
			// Notify network monitor
			NetworkMonitor.notifyThreadDeath(this);
		}
		
		// Experimental: signal to listeners that NIC read thread is now dead
		// by sending a 0 byte packet.
		for (APIFrameListener l : listeners) {
			l.handleAPIFrame(packet, 0);
		}
	}
	
	private void runLoop () {
 		int packetLen, packetType;

 		while (true) {
 			try {
 				
 				packetLen = XBeeUtil.readAPIFrameFromStream(xbeeIn, packet);
 				
 				log.debug("RX: " + ByteFormatUtils.byteArrayToString(packet,0,packetLen));
 				
 				packetType = packet[0] & 0xff;
 				
 				// Display frame ID if appropriate
 				if (packetType == 0x88 || packetType == 0x8B || packetType == 0x97) {
 					//int xBeeFrameId = packet[1] & 0xff;
 					//log.debug ("RX frameId=0x"  + Integer.toHexString(xBeeFrameId) + ": " 
 					//	+ DebugUtils.formatXBeeAPIPacket(packet, 0, packetLen)
 					//	);
 				} else {
 					//log.debug ("RX: " + DebugUtils.formatXBeeAPIPacket(packet, 0, packetLen));
 				}
 				
 				if (listeners.size()==0) {
 					log.warn("No listeners registered.");
 				}
 				for (APIFrameListener l : listeners) {
 					l.handleAPIFrame(packet, packetLen);
 				}
 			} catch (IOException e) {
 				log.error(e.toString());
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 		}
 	}
	
 	public synchronized void addListener (APIFrameListener o) {
 		if (!listeners.contains(o)) {
 			listeners.add(o);
 		}
 	}
 	public synchronized void removeListener (APIFrameListener o) {
 		listeners.remove(o);
 	}
 
 }
