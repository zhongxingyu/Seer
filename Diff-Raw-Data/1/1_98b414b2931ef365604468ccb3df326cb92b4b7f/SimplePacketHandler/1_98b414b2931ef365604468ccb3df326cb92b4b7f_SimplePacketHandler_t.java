 package com.uh.nwvz.server.pcap;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jnetpcap.packet.PcapPacket;
 import org.jnetpcap.packet.PcapPacketHandler;
 import org.jnetpcap.protocol.network.Ip4;
 import org.jnetpcap.protocol.network.Ip6;
 import org.jnetpcap.protocol.tcpip.Http;
 import org.jnetpcap.protocol.tcpip.Tcp;
 import org.jnetpcap.protocol.tcpip.Udp;
 import org.jnetpcap.util.resolver.IpResolver;
 
 import com.uh.nwvz.shared.PcapUtil;
 import com.uh.nwvz.shared.SimplePacketType;
 import com.uh.nwvz.shared.dto.NetworkNodeDTO;
 import com.uh.nwvz.shared.dto.SimpleHttpPacket;
 import com.uh.nwvz.shared.dto.SimpleIPPacket;
 import com.uh.nwvz.shared.dto.SimpleTCPPacket;
 import com.uh.nwvz.shared.dto.SimpleUDPPacket;
 
 public class SimplePacketHandler implements PcapPacketHandler<StringBuilder> {
 	private Map<String, NetworkNodeDTO> nodes = new HashMap<String, NetworkNodeDTO>();
 
 	private long totalPacketCount = 0;
 
 	private long tcpPacketCount = 0;
 
 	private long nodeCount = 0;
 
 	private final Ip4 ip4 = new Ip4();
 
 	private final Ip6 ip6 = new Ip6();
 
 	private final Tcp tcp = new Tcp();
 
 	private final Http http = new Http();
 
 	private final Udp udp = new Udp();
 
 	private long firstPacketDate = Long.MAX_VALUE;
 
 	private long lastPacketDate = Long.MIN_VALUE;
 
 	private final Map<String, String> hostnames = new HashMap<String, String>();
 
 	private IpResolver ipResolver = new IpResolver();
 
 	public long getFirstPacketDate() {
 		return firstPacketDate;
 	}
 
 	private String getHostname(byte[] ip) {
 		String sIp = PcapUtil.ip(ip);
 		if (hostnames.containsKey(sIp))
 			return hostnames.get(ip);
 
 		String hostname = ipResolver.resolveToName(ip,
 				ipResolver.toHashCode(ip));
 
 		if (hostname == null)
 			hostname = "";
 
 		hostnames.put(sIp, hostname);
 
 		return hostname;
 	}
 
 	public long getLastPacketDate() {
 		return lastPacketDate;
 	}
 
 	public long getNodeCount() {
 		return nodeCount;
 	}
 
 	public Map<String, NetworkNodeDTO> getNodes() {
 		return nodes;
 	}
 
 	public long getTcpPacketCount() {
 		return tcpPacketCount;
 	}
 
 	public long getTotalPacketCount() {
 		return totalPacketCount;
 	}
 
 	@Override
 	public void nextPacket(PcapPacket packet, StringBuilder errbuf) {
 
 		totalPacketCount++;
 
 		long receiveDate = packet.getCaptureHeader().timestampInMillis();
 
 		if (receiveDate < firstPacketDate)
 			firstPacketDate = receiveDate;
 
 		if (receiveDate > lastPacketDate)
 			lastPacketDate = receiveDate;
 
 		if (packet.hasHeader(Ip4.ID) || packet.hasHeader(Ip6.ID)) {
 			tcpPacketCount++;
 
 			SimpleIPPacket simplePacket = new SimpleIPPacket();
 			simplePacket.setReceiveDate(receiveDate);
 			simplePacket.setSize(packet.getTotalSize());
 
 			if (packet.hasHeader(ip4)) {
 				simplePacket.setType(SimplePacketType.IP4);
 				simplePacket.setSrcIpAddr(ip4.source());
 				simplePacket.setDestIpAddr(ip4.destination());
 				simplePacket.setDestHostname(getHostname(ip4.destination()));
 				simplePacket.setSourceHostname(getHostname(ip4.source()));
 			} else if (packet.hasHeader(ip6)) {
 				simplePacket.setType(SimplePacketType.IP6);
 				simplePacket.setSrcIpAddr(ip6.source());
 				simplePacket.setDestIpAddr(ip6.destination());
 				simplePacket.setDestHostname(getHostname(ip6.destination()));
 				simplePacket.setSourceHostname(getHostname(ip6.source()));
 			}
 
 			String sIp = PcapUtil.ip(simplePacket.getSrcIpAddr());
 			String dIp = PcapUtil.ip(simplePacket.getDestIpAddr());
 
 			if (packet.hasHeader(tcp)) {
 				simplePacket.setType(SimplePacketType.TCP);
 
 				SimpleTCPPacket tcpPacket = new SimpleTCPPacket(tcp.source(),
 						tcp.destination());
 
 				if (packet.hasHeader(http)) {
					simplePacket.setType(SimplePacketType.HTTP);
 					SimpleHttpPacket httpPacket = new SimpleHttpPacket(
 							http.fieldValue(Http.Request.Referer),
 							http.contentType());
 					tcpPacket.setHttpPacket(httpPacket);
 				}
 
 				simplePacket.setTcpPacket(tcpPacket);
 			} else if (packet.hasHeader(udp)) {
 				simplePacket.setType(SimplePacketType.UDP);
 
 				SimpleUDPPacket udpPacket = new SimpleUDPPacket(udp.source(),
 						udp.destination());
 
 				simplePacket.setUdpPacket(udpPacket);
 			}
 
 			// Check if theres already a node with the given ip-addresses
 
 			if (!nodes.containsKey(sIp)) {
 				nodes.put(sIp, new NetworkNodeDTO(simplePacket.getSrcIpAddr(),
 						simplePacket.getReceiveDate()));
 				nodeCount++;
 			}
 
 			if (!nodes.containsKey(dIp)) {
 				nodes.put(dIp, new NetworkNodeDTO(simplePacket.getDestIpAddr(),
 						simplePacket.getReceiveDate()));
 				nodeCount++;
 			}
 
 			nodes.get(sIp).addSentPacket(simplePacket);
 			nodes.get(dIp).addReceivedPacket(simplePacket);
 		}
 	}
 
 }
