 package netproj.hosts;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import netproj.skeleton.Packet;
 
 /**
  * A host that can ping and reply to pings.
  * 
  * @author robert, emil
  */
 public class Pinger extends Host {
 	private static final Logger logger = Logger.getLogger(Pinger.class.getCanonicalName());
 	
 	private static final int PACKET_SIZE = 32 * 8; // Ping packet size is usually 32 bytes.
 	
 	private Packet prevpkt = new Packet(0, 0, 0);
 	private long replyAt;
 
 	public Pinger(int inputBuffSize, int outputBuffSize, int address) {
 		super(inputBuffSize, outputBuffSize, address);
 	}
 	
 	/**
 	 * Pings {@code targetAddress} with a single packet with TTL of 64 and timeout 1000ms.
 	 */
 	public long ping(int targetAddress) {
 		return ping(targetAddress, 64, 1000);
 	}
 	
 	/**
 	 * Pings a target with a single packet.
 	 * 
 	 * @param targetAddress     IPv4 address of the target.
 	 * @param ttl				TTL for the ping packet.
 	 * 
 	 * @return                  The time in ms it took for the reply to come back or -1 if it timed
 	 *                          out.
 	 */
 	public synchronized long ping(int targetAddress, int ttl, long timeout) {
 		long sentAt = System.currentTimeMillis();
 		
 		// Keep track of the packet we sent, so when we get a reply we know it's a reply to this
 	    // packet.
 		prevpkt = new Packet(getAddress(), targetAddress, PACKET_SIZE, ttl);
 		replyAt = -1;
 		sendPacket(prevpkt);
 
 		try {
 			wait(timeout);
 		} catch (InterruptedException e) {
 			logger.log(Level.WARNING, getAddress() + "was interrupted while waiting for reply "
 					   + "from " + targetAddress, e);
 		}
 
 		if (replyAt != -1) {
 			return replyAt - sentAt;
 		}
 		
 		return -1;
 	}
 
 	@Override
 	public synchronized void processPacket(Packet pkt) {
 		// If this device isn't the intended recipient, we don't do anything.
 		if (this.getAddress() == pkt.getDestAddress()) {
 			if (pkt.getPacketId() == prevpkt.getPacketId()) {
 				// We've received a reply!
 				replyAt = System.currentTimeMillis();
 				notify();
 			} else {
 				// Reply to this ping
 				
 				// The return packet switches the dest and source.
 				int address = pkt.getSourceAddress();
 				pkt.setSourceAddress(pkt.getDestAddress());
 				pkt.setDestAddress(address);
 				
 				sendPacket(pkt);
 			}
 		}
 	}
 	
 	@Override
 	public void handleCommand(String command) {
 		if (command.equals("help")) {
			super.handleCommand("help");
 			System.out.println("ping <address> - pings <address>");
		} else if (command.startsWith("ping")) {
 			long replyTime = ping(Integer.decode(command.split(" ")[1]));
 			if (replyTime == -1) {
 				System.out.println("Ping timed out.");
 			} else {
 				System.out.println("Ping reply in " + replyTime + "ms.");
 			}
		} else {
			super.handleCommand(command);
 		}
 	}
 }
