 package dk.dtu.imm.distributedsystems.projects.sensornetwork.common.components.listener.udp;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.GlobalUtility;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.channels.Channel;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.components.listener.AbstractPortListener;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.exceptions.ConnectionHandlerException;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.packet.Packet;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.packet.PacketType;
 
 /**
  * The standard Udp Port Listener thread that listens on the provided
  * port and receives Packet objects. Whenever a Packet is received, 
  * the execution is being forwarded to abstract handleIncomingPacket
  * method. This class also provides a method to send and ACK packet
  * back to the sender of the incoming Packet.
  *
  * @see Packet
  * 
  * @author Maciej Kucharek <a href="mailto:s091828 (at) student.dtu.dk">s091828 (at) student.dtu.dk</a>
  */
 public abstract class AbstractUdpPortListener extends AbstractPortListener {
 
 	/** The server socket. */
 	protected DatagramSocket serverSocket;
 
 	/** The received data. */
 	protected byte[] receivedData;
 	
 	/** The associated channels. */
 	protected Channel[] associatedChannels;
 
 	/**
 	 * Instantiates a new abstract udp port listener.
 	 *
 	 * @param portNumber the port number
 	 */
 	public AbstractUdpPortListener(String nodeId, DatagramSocket serverSocket, Channel[] associatedChannels) {
 		super(nodeId);
 		
 		this.serverSocket = serverSocket;
		this.associatedChannels = associatedChannels;
 	}
 
 	public DatagramSocket getServerSocket() {
 		return serverSocket;
 	}
 
 	public Channel[] getAssociatedChannels() {
 		return associatedChannels;
 	}
 
 	/* (non-Javadoc)
 	 * @see dk.dtu.imm.distributedsystems.projects.sensornetwork.common.components.AbstractPortHandler#setUp()
 	 */
 	@Override
 	protected void setUp() {
 		// nothing to set up here
 	}
 
 	/* (non-Javadoc)
 	 * @see dk.dtu.imm.distributedsystems.projects.sensornetwork.common.components.AbstractPortHandler#handleConnection()
 	 */
 	@Override
 	protected void handleConnection() throws ConnectionHandlerException {
 
 		try {
 			receivedData = new byte[GlobalUtility.UDP_PACKET_SIZE];
 
 			// Create a new Datagram packet for the client message
 			DatagramPacket receivePacket = new DatagramPacket(receivedData,
 					receivedData.length);
 
 			logger.debug("Waiting for datagram packet");
 
 			// Receive the message
 			serverSocket.receive(receivePacket);
 
 			ObjectInputStream ois = new ObjectInputStream(
 					new ByteArrayInputStream(receivePacket.getData()));
 
 			Packet packet = (Packet) ois.readObject();
 
 			// Obtain IP address and port of the sender
 			InetAddress fromIpAddress = receivePacket.getAddress();
 			int fromPortNumber = receivePacket.getPort();
 
 			// call handler method that will be provided by concrete
 			// implementations
 			this.handleIncomingPacket(packet, fromIpAddress, fromPortNumber);
 
 		} catch (IOException e) {
 			throw new ConnectionHandlerException(e, this.getClass());
 		} catch (ClassNotFoundException e) {
 			throw new ConnectionHandlerException(e, this.getClass());
 		}
 
 	}
 
 	/**
 	 * Send ack packet to the provided client.
 	 * 
 	 * @param toIpAddress
 	 *            the to ip address
 	 * @param toPortNumber
 	 *            the to port number
 	 * @throws IOException
 	 *             Signals that an I/O exception has occurred.
 	 */
 	protected void sendAck(InetAddress toIpAddress, int toPortNumber)
 			throws IOException {
 
 		// Create Packet object and serialize it to a byte array
 		Packet ackPacket = new Packet(nodeId, PacketType.ACK);
 
 		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
 		ObjectOutput oo = new ObjectOutputStream(bStream);
 		oo.writeObject(ackPacket);
 		oo.close();
 
 		byte[] serializedAckPacket = bStream.toByteArray();
 
 		// check if the packet size fits in the default size
 		if (serializedAckPacket.length > GlobalUtility.UDP_PACKET_SIZE) {
 			throw new RuntimeException("Packet " + ackPacket
 					+ "exceeds the default packet size");
 		}
 
 		// Create a new Datagram packet for the client
 		DatagramPacket sendPacket = new DatagramPacket(serializedAckPacket,
 				serializedAckPacket.length, toIpAddress, toPortNumber);
 
 		// Send reponse back to the client
 		serverSocket.send(sendPacket);
 		
 		logger.debug("Sent ACK - " + ackPacket);
 
 	}
 
 	/**
 	 * Handle an incoming packet. This method needs to be provided by the
 	 * concrete implementations of that class.
 	 * 
 	 * @param packet
 	 *            the packet
 	 * @param fromIpAddress
 	 *            the from ip address
 	 * @param fromPortNumber
 	 *            the from port number
 	 * @throws IOException
 	 *             Signals that an I/O exception has occurred.
 	 */
 	protected abstract void handleIncomingPacket(Packet packet,
 			InetAddress fromIpAddress, int fromPortNumber) throws IOException;
 	
 	/**
 	 * Gets the associated channel id.
 	 *
 	 * @param ipAddress the ip address
 	 * @param portNumber the port number
 	 * @return the associated channel id
 	 */
 	protected String getAssociatedChannelId(InetAddress ipAddress, int portNumber) {
 		
 		try {
 			for(Channel channel : associatedChannels) {
 				if(channel.getIpAddress().equals(ipAddress.getHostAddress()) &&
 						channel.getPortNumber() == portNumber) {
 					return channel.getId();
 				}
 			}
 		} catch (NullPointerException e) {
			// no channels
			logger.error("No channels associated to this listener - cannot determine sender");
 			return "UNKNOWN";
 		}
 		
		logger.warn("Did not recognise sender ID - packet came from host: " + ipAddress.getHostAddress() + ", port: " + portNumber);
		
		return "UNKNOWN";
 		
 	}
 }
