 package org.bitducks.spoofing.services;
 
 import java.net.InetAddress;
 import java.util.Collection;
 
 import jpcap.NetworkInterface;
 import jpcap.packet.ARPPacket;
 
 import org.bitducks.spoofing.core.Server;
 import org.bitducks.spoofing.core.Service;
 import org.bitducks.spoofing.packet.PacketGenerator;
 import org.bitducks.spoofing.util.IpRange;
 import org.bitducks.spoofing.util.IpUtil;
 
 
 public class ArpScanService extends Service {
 	
 	public ArpScanService() {
 		
 	}
 
 	@Override
 	public void run() {
 		
 	}
 	
 	public void runScan( Collection<InetAddress> addresses ) {
 		
 		this.logger.info("Arp scan started");
 
 		Server server = Server.getInstance();
 		NetworkInterface device = server.getInfo().getDevice();
 		
 		PacketGenerator generator = new PacketGenerator(device);
 		
 		for( InetAddress address: addresses ) {
 			
 			this.logger.info("sending request for " + address.toString() );
 			ARPPacket arpRequest = generator.arpRequest(address);	
 			server.sendPacket(arpRequest);
 			
 		}
 		
 	}
 	
 	public void runNetworkScan() {
 		
 		this.logger.info("Arp network scan started");
 		
 		Server server = Server.getInstance();
 		NetworkInterface device = server.getInfo().getDevice();
 		
 		PacketGenerator generator = new PacketGenerator(device);
 		
 		InetAddress start = IpUtil.network( device );
		InetAddress end = IpUtil.lastIpInNetwork( device );
 		
 		IpRange ipRange = new IpRange(start, end);
 		
 		for( InetAddress address: ipRange ) {
 			this.logger.info("sending request for " + address.toString() );
 			ARPPacket arpRequest = generator.arpRequest(address);	
 			server.sendPacket(arpRequest);
 		}
 			
 	}
 
 }
