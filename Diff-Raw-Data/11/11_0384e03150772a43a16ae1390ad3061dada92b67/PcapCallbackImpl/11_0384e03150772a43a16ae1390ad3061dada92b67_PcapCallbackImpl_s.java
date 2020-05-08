 package se.su.it.pcap2neo.pcap.impl;
 
 
 import org.apache.log4j.Logger;
import org.neo4j.graphdb.Relationship;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import edu.gatech.sjpcap.IPPacket;
 import edu.gatech.sjpcap.TCPPacket;
 
 import se.su.it.pcap2neo.neo.NeoPcapFacade;
 import se.su.it.pcap2neo.pcap.PcapCallback;
 
 @Component
 public class PcapCallbackImpl implements PcapCallback {
 	
 	private Logger logger = Logger.getLogger(PcapCallbackImpl.class);
 	private NeoPcapFacade neo = null;
 
 	@Override
 	public int ipPacketCallback(IPPacket packet) {
 		if(packet instanceof TCPPacket){
 			TCPPacket tcpPacket = (TCPPacket) packet;
 			logger.debug("TCP " + tcpPacket);
			Relationship rel = neo.addTcpSyn(tcpPacket.src_ip, tcpPacket.dst_ip, tcpPacket.dst_port);
 		} else {
 			logger.debug("Discaring unknown packet " + packet.src_ip.getHostAddress() + "=>" + packet.dst_ip.getHostAddress());
 		}
 		return 0;
 	}
 	
 	public NeoPcapFacade getNeo() {
 		return neo;
 	}
 
 	@Autowired
 	public void setNeo(NeoPcapFacade neo) {
 		this.neo = neo;
 	}
 	
 }
