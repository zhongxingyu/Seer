 package se.su.it.pcap2neo.pcap.impl;
 
 import edu.gatech.sjpcap.IPPacket;
 import edu.gatech.sjpcap.Packet;
 import edu.gatech.sjpcap.PcapParser;
 import se.su.it.pcap2neo.pcap.*;
 
 import org.springframework.beans.factory.annotation.*;
 import org.springframework.stereotype.Component;
 
 @Component
 public class PcapFileParserImpl implements PcapFileParser {
 
 	private PcapCallback cb = null;
 	PcapParser pcapParser = new PcapParser();
 	
 	public PcapFileParserImpl() {
 	}
 
 	public int parseFile(String fname) {
 
         if(pcapParser.openFile(fname) < 0){
             System.err.println("Failed to open " + fname + ", exiting.");
             return -1;
         }
                 
         Packet packet = pcapParser.getPacket();
         while(packet != Packet.EOF){
             if(!(packet instanceof IPPacket)){
                 packet = pcapParser.getPacket();
                 continue;
             }
 
             IPPacket ipPacket = (IPPacket) packet;
             cb.ipPacketCallback(ipPacket);
             packet = pcapParser.getPacket();
         }
         pcapParser.closeFile();
 		return 0;
 	}
 
 	public PcapCallback getPcapCallback() {
 		return cb;
 	}
 
 	@Autowired
 	public void setPcapCallback(PcapCallback c) {
 		cb=c;
 	}
 }
