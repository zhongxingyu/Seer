 package dk.dtu.imm.distributedsystems.projects.sensornetwork.sensor.sender;
 
 import java.io.IOException;
 import java.net.DatagramSocket;
 import java.util.Queue;
 
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.channels.Channel;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.components.sender.udp.AbstractNodeUdpPortSender;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.exceptions.ConnectionHandlerException;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.exceptions.WrongPacketSizeException;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.packet.Packet;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.packet.PacketGroup;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.packet.PacketType;
 
 public class UdpPortSender extends AbstractNodeUdpPortSender {
 	
 	/**
 	 * Instantiates a new sender.
 	 *
 	 * @param portNumber the port number
 	 * @param buffer the buffer
 	 * @param leftChannels the left channels
 	 * @param rightChannels the right channels
 	 * @param ackTimeout the ack timeout
 	 */
 	public UdpPortSender(String nodeId, DatagramSocket leftSocket, DatagramSocket rightSocket, Queue<Packet> buffer, Channel[] leftChannels,
 			Channel[] rightChannels, int ackTimeout) {
 		super(nodeId, leftSocket, rightSocket, 0, buffer, leftChannels, rightChannels, ackTimeout);
 		this.leftChannels = leftChannels;
 		this.rightChannels = rightChannels;
 		
 		this.start();
 		
 	}
 	
 	/**
 	 * Gets the left channels.
 	 *
 	 * @return the left channels
 	 */
 	public Channel[] getLeftChannels() {
 		return leftChannels;
 	}
 
 	/**
 	 * Gets the right channels.
 	 *
 	 * @return the right channels
 	 */
 	public Channel[] getRightChannels() {
 		return rightChannels;
 	}
 
 	/* (non-Javadoc)
 	 * @see dk.dtu.imm.distributedsystems.projects.sensornetwork.common.components.sender.AbstractPortSender#handleOutgoingPacket(dk.dtu.imm.distributedsystems.projects.sensornetwork.common.packet.Packet)
 	 */
 	@Override
 	protected void handleOutgoingPacket(Packet packet) throws ConnectionHandlerException, InterruptedException {
 		
 		logger.debug("Sending " + packet);
 		
 		try {
			if (packet.getGroup() == PacketGroup.SENSOR_DATA) {
 				
				if (packet.getType() == PacketType.DAT) { // DATA
 					sendUnicastLeft(packet);
 					
				} else if (packet.getType() == PacketType.ALM) { // ALARM_DATA
 					
 					if (!sendUnicastLeft(packet)) {
 						sendUnicastLeft(packet); // retransmit
 					}
 					
 				} else {
 					logger.info("Unknown SENSOR_DATA packet type - dropped");
 				}
			} else if (packet.getGroup() == PacketGroup.COMMAND) {
 				sendMulticastRight(packet);
 			} else {
 				logger.info("Invalid packet received: [Group = '" + packet.getGroup() + "', Type = '" + packet.getType() + "'] - dropped");
 			}
 		} catch (WrongPacketSizeException e) {
 			logger.warn(e.getMessage() + " The actual packet size is: " + e.getActualSize(), e);
 		} catch (IOException e) {
 			throw new ConnectionHandlerException(e, this.getClass());
 		}
 		
 	}
 	
 }
