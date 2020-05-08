 package netproj.hosts.fasttcp;
 
 import netproj.skeleton.Packet;
 
 /**
  * A Fast TCP Packet.
  * 
  * Can be both a data packet and an ACK packet. The constructor only creates data packets. Use the
  * {@code acknowledge()} method to create an ACK for a packet.
  * 
  * @author emil
  */
 public class FastTcpPacket extends Packet {
 	/**
 	 * ACK packet size is usually 32 bytes.
 	 */
 	private static final int ACK_SIZE = 32 * 8;
 	
 	/**
 	 * Sequence number of the packet (unique per sender)
 	 */
 	private long id;
 	
 	/**
 	 * Type of packet.
 	 */
 	private boolean ack;
 	
 	/**
 	 * Time in ms since epoch when the original data packet left its origin.
 	 */
 	private long timestamp;
 	
 	/**
 	 * Creates a data packet.
 	 * 
 	 * The timestamp is set to the current time. If you want to create an ACK packet, use the
 	 * {@code acknowledge()} method.
 	 * 
 	 * @param source	IPv4 address of sender
 	 * @param dest		IPv4 address of receiver
 	 * @param size		Size of packet
 	 * @param id		Packet ID
 	 */
 	public FastTcpPacket(int source, int dest, int size, long id) {
 		super(source, dest, size);
 		this.id = id;
 		ack = false;
 		timestamp = System.currentTimeMillis();
 	}
 	
 	public FastTcpPacket(FastTcpPacket packet) {
 		super((Packet) packet);
 		id = packet.id;
 		ack = packet.ack;
 		timestamp = packet.timestamp;
 	}
 	
 	@Override
 	public Packet copy() {
 		return new FastTcpPacket(this);
 	}
 	
 	/**
 	 * Returns an ACK for this packet.
 	 * 
	 * Automatically switches the source and destination addresses. Preserves the timestamp.
 	 */
 	public FastTcpPacket acknowledge() {
 		FastTcpPacket packet = new FastTcpPacket(this);
 		
 		// Convert to ACK packet
 		packet.ack = true;
 		packet.setSizeInBits(ACK_SIZE);
 		
 		// Swap sender and receiver
 		int tmp = packet.getDestAddress();
 		packet.setDestAddress(getSourceAddress());
 		packet.setSourceAddress(tmp);
 		
 		return packet;
 	}
 	
 	/**
 	 * Type of the packet (ACK or data)
 	 */
 	public boolean isAck() {
 		return ack;
 	}
 	
 	/**
 	 * Sequence number of the packet (unique per sender)
 	 */
 	public long getId() {
 		return id;
 	}
 	
 	/**
 	 * Round trip time in ms.
 	 * 
 	 * Computed as the current time minus the timestamp of the packet.
 	 */
 	public long getRtt() {
 		return System.currentTimeMillis() - timestamp;
 	}
 	
 	/**
 	 * Resets the timestamp to the current time.
 	 */
 	public void reset() {
 		timestamp = System.currentTimeMillis();
 	}
 }
 
