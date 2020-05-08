 package dk.dtu.imm.distributedsystems.projects.sensornetwork.sensor.listener;
 
 import java.io.IOException;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.channels.Channel;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.components.listener.udp.AbstractUdpPortListener;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.components.transceiver.AbstractTwoChannelTransceiver;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.logging.LoggingUtility;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.packet.MessageType;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.packet.Packet;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.packet.PacketGroup;
 import dk.dtu.imm.distributedsystems.projects.sensornetwork.common.packet.PacketType;
 
 public final class LeftUdpPortListener extends AbstractUdpPortListener {
 
 	private AbstractTwoChannelTransceiver transceiver;
 	
 	public LeftUdpPortListener(String nodeId, AbstractTwoChannelTransceiver relatedTransceiver, DatagramSocket socket, Channel[] associatedChannels) {
 		super(nodeId, socket, associatedChannels);
 		this.transceiver = relatedTransceiver;
 		
 		this.start();
 	}
 	
 	@Override
 	protected void handleIncomingPacket(Packet packet,
 			InetAddress fromIpAddress, int fromPortNumber) throws IOException {
 		
 		if ((packet.getGroup().equals(PacketGroup.COMMAND) || packet.getGroup().equals(PacketGroup.ACKNOWLEDGEMENT))) {
 			
 			logger.debug(packet + " accepted by listener");
 						
 			if (packet.getGroup().equals(PacketGroup.COMMAND)) {
 				
 				LoggingUtility.logMessage(this.getNodeId(),
 						getAssociatedChannelId(fromIpAddress, fromPortNumber),
 						MessageType.RCV, packet.getType(),
						packet.getValue());
 						
 			} else if (packet.getType().equals(PacketType.ACK)) {
 				
 				LoggingUtility.logMessage(this.getNodeId(),
 						getAssociatedChannelId(fromIpAddress, fromPortNumber),
 						MessageType.RCV, packet.getType());
 			
 			}
 			
 			transceiver.handlePacket(packet);
 			
 		} else {
 			
 			logger.warn(packet + "dropped by listener - wrong type");
 		
 		}
 	}
 }
