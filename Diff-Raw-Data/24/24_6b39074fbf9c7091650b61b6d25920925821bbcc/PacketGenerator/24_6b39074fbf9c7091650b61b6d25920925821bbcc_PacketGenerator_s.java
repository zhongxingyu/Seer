 package org.bitducks.spoofing.packet;
 import java.net.InetAddress;
 
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
 		
		return null;
 		
 	}
 
 }
