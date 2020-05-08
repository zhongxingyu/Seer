 package org.bitducks.spoofing.packet;
import java.io.IOException;
 import java.net.InetAddress;
 
import org.bitducks.spoofing.exception.UnexpectedErrorException;
import org.bitducks.spoofing.util.gateway.GatewayFinder;

 import jpcap.NetworkInterface;
 import jpcap.packet.ARPPacket;
 
 
 public class PacketGenerator {
 	
 	private NetworkInterface device;
 
 	public PacketGenerator( NetworkInterface device ) {
 		
 		this.device = device;
 		
 	}
 	
 	public InetAddress getDeviceAddress() {
 		
 		return this.device.addresses[0].address;
 		
 	}
 	
 	public ARPPacket arpRequest( InetAddress ipTarget ) {
 		
 		return PacketFactory.arpRequest(
 				this.device.mac_address,
 				this.getDeviceAddress(),
 				ipTarget );
 		
 	}
 	
 	public InetAddress getGateway() {
 		
		try {
			return GatewayFinder.find( this.device );
		} catch (IOException e) {
			throw new UnexpectedErrorException(e, "Error parsing routing table while looking up gateway");
		}
 		
 	}
 
 }
