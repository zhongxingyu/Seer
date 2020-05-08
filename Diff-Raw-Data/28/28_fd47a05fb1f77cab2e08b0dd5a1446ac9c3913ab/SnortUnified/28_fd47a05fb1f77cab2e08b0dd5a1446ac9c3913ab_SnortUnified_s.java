 /**
  * Copyright (c) 2010, Michael J. A. Miranda
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *    - Redistributions of source code must retain the above copyright notice, 
  *      this list of conditions and the following disclaimer.
  *
  *    - Redistributions in binary form must reproduce the above copyright notice,
  *      this list of conditions and the following disclaimer in the documentation 
  *      and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * Project: SnortUnifiedParser
  * Filename: SnortUnified.java
  * Package: org.michaelmiranda.snort.parsers
  * Timestamp: Mar 5, 2010 10:03:56 PM
  * Author: Michael J. A. Miranda
  */
 package org.michaelmiranda.snort.parsers;
 
 import java.io.*;
 import java.nio.ByteBuffer;
 import java.nio.channels.*;
 import org.apache.commons.codec.binary.*;
 
 /**
  * @author Michael J. A. Miranda
  *
  */
 public class SnortUnified {
 
 	private FileChannel fc;
 	private ByteBuffer buf;
 	private byte[] bytes1;
 	private byte[] bytes2;
 	private byte[] bytes4;
 	private byte[] bytes6;
 	
 	
 	private SnortPacketInterface snortPacket;
 	private Unified2RecordHeader header;
 	private Unified2Event event;
 	private Unified2Packet packet;
 	private EthernetFramePacket ethernetPacket;
 	private IPPacket ipPacket;
 	private TCPPacket tcpPacket;
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		String f = "/Users/haogao/snort/merged.log";
 		SnortUnified su = new SnortUnified();
 		su.parse(f);		
 	}
 	
 	public SnortUnified() {
 		bytes1 = new byte[1];
 		bytes2 = new byte[2];
 		bytes4 = new byte[4];
 		bytes6 = new byte[6];
 	}
 	
 	public void parse(String filename) {
 		// pre-parsing setup
 		// DataInputStream inputStream = Utilities.getBinaryFileStream(filename);
 		fc = Utilities.getBinaryFilechannel(filename);		
 		// start parsing
 		int packet_count = 0;
 		try {
 			while (fc.position() != fc.size()) {
 				System.out.println("Packet Count: " + ++packet_count);
 				System.out.println("Position (" + fc.size() + ": " + fc.position());
 				this.startParsing();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void startParsing() {
 		this.readRecordHeader();
 		switch ((int)header.getType()) {
 			// Snort Event Record
 			case 7:
 				this.readEventHeader(header.getLength());
 				this.snortPacket = this.event;				
 				break;
 			// IPv4 Packet Record
 			case 2:
 				this.readPacketHeader(header.getLength());
 				// based on the link type assign to proper packet 
 				switch ((int) packet.getLinktype()) {
 					case ETHERNET_LINK:
 						ethernetPacket = this.getEthernetPacketClear();
 						ethernetPacket.setU2header(header);
 						ethernetPacket.setU2Packet(packet);
 						this.parseEthernetFramePacket(fc);
 						// determine frame type and build associated packet
 						switch ((int) ethernetPacket.getFrameType()) {
 							case EthernetFramePacket.IP_TYPE:
 								ipPacket = this.getIPPacketClear();
 								ipPacket.setPacket(ethernetPacket);
 								this.parseIPPacket(buf);
 								switch ((int) ipPacket.getProto()) {
 									case TCP_PROTOCOL:
 										tcpPacket = this.getTCPPacketClear();
 										tcpPacket.setPacket(ipPacket);
 										this.parseTCPPacket(buf);
 										this.snortPacket = this.tcpPacket;
 										break;
 									case UDP_PROTOCOL:
 										
 										break;
 									
 								}								
 								break;
 							default:
 								break;
 						}
 						break;
 					default:
 						break;		
 				}
 				break;
			default:				
 				break;							
 		}
 		if (snortPacket != null) {
 			System.out.println(snortPacket.toString());
 		}
 	}
 	
 	
 	private Unified2RecordHeader getHeaderClear() {
 		if (header == null) {
 			header = new Unified2RecordHeader();
 		} else {
 			header.clear();
 		}
 		return header;		
 	}
 	
 	private Unified2Event getEventClear() {
 		if (event == null) {
 			event = new Unified2Event();
 		} else {
 			event.clear();
 		}
 		return event;
 	}
 	
 	private Unified2Packet getPacketClear() {
 		if (packet == null) {
 			packet = new Unified2Packet();			
 		} else {
 			packet.clear();
 		}
 		return packet;
 	}
 	
 	private EthernetFramePacket getEthernetPacketClear() {
 		if (ethernetPacket == null) {
 			ethernetPacket = new EthernetFramePacket();			
 		} else {
 			ethernetPacket.clear();
 		}
 		return ethernetPacket;
 	}
 	
 	private IPPacket getIPPacketClear() {
 		if (ipPacket == null) {
 			ipPacket = new IPPacket();			
 		} else {
 			ipPacket.clear();
 		}
 		return ipPacket;
 	}
 	
 	private TCPPacket getTCPPacketClear() {
 		if (tcpPacket == null) {
 			tcpPacket = new TCPPacket();			
 		} else {
 			tcpPacket.clear();
 		}
 		return tcpPacket;
 	}
 	
 	public void readRecordHeader() {
 		buf = ByteBuffer.allocate(HEADER_SIZE);
 		buf.clear();
 		try {
 			int nread = 0;		
 			do {
 				nread = fc.read(buf);			
 			} while (nread != -1 && buf.hasRemaining());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		buf.rewind();
 		// parse out header
 		header = this.getHeaderClear();	
		bytes4 = Utilities.clearBytes(bytes4);		
 		// get type
 		buf.get(bytes4, 0, TYPE_SIZE);
 		header.setType(Utilities.unsignedIntToLong(bytes4));
 		// clear buffer		
 		bytes4 = Utilities.clearBytes(bytes4);	
 		// get length		
 		buf.get(bytes4, 0, LENGTH_SIZE);		
 		header.setLength(Utilities.unsignedIntToLong(bytes4));
 		// clear buffer
 		bytes4 = Utilities.clearBytes(bytes4);
 	}
 	
 	public void readEventHeader(long recordLength) {
 		buf = ByteBuffer.allocate(EVENT_HEADER_SIZE);
 		buf.clear();
 		try {
 			int nread = 0;		
 			do {
 				nread = fc.read(buf);			
 			} while (nread != -1 && buf.hasRemaining());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		buf.rewind();
 		event = this.getEventClear();
 		bytes4 = Utilities.clearBytes(bytes4);
 		// get sensor id
 		buf.get(bytes4, 0, SENSOR_ID_SIZE);
 		event.setSensor_id(Utilities.unsignedIntToLong(bytes4));
 		bytes4 = Utilities.clearBytes(bytes4);
 		// get event id
 		buf.get(bytes4, 0, EVENT_ID_SIZE);
 		event.setEvent_id(Utilities.unsignedIntToLong(bytes4));
 		bytes4 = Utilities.clearBytes(bytes4);
 		// get event seconds
 		buf.get(bytes4, 0, EVENT_SEC_SIZE);
 		event.setEvent_second(Utilities.unsignedIntToLong(bytes4));
 		bytes4 = Utilities.clearBytes(bytes4);
 		// get event micro seconds
 		buf.get(bytes4, 0, EVENT_MSEC_SIZE);
 		event.setEvent_microsecond(Utilities.unsignedIntToLong(bytes4));
 		bytes4 = Utilities.clearBytes(bytes4);
 		// get sig id
 		buf.get(bytes4, 0, SIG_ID_SIZE);
 		event.setSignature_id(Utilities.unsignedIntToLong(bytes4));
 		bytes4 = Utilities.clearBytes(bytes4);
 		// get gen id
 		buf.get(bytes4, 0, GEN_ID_SIZE);
 		event.setGenerator_id(Utilities.unsignedIntToLong(bytes4));
 		bytes4 = Utilities.clearBytes(bytes4);
 		// get signature revision
 		buf.get(bytes4, 0, SIG_REV_SIZE);
 		event.setSignature_revision(Utilities.unsignedIntToLong(bytes4));
 		bytes4 = Utilities.clearBytes(bytes4);
 		// get class id
 		buf.get(bytes4, 0, CLASS_ID_SIZE);
 		event.setClassification_id(Utilities.unsignedIntToLong(bytes4));
 		bytes4 = Utilities.clearBytes(bytes4);
 		// get priority
 		buf.get(bytes4, 0, PRIORITY_SIZE);
 		event.setPriority_id(Utilities.unsignedIntToLong(bytes4));
 		bytes4 = Utilities.clearBytes(bytes4);
 		// get source IP address
 		buf.get(bytes4, 0, IP_SOURCE_SIZE);
 		event.setIp_source(Utilities.unsignedIntToLong(bytes4));
 		bytes4 = Utilities.clearBytes(bytes4);
 		// get destination IP address
 		buf.get(bytes4, 0, IP_DESTINATION_SIZE);
 		event.setIp_destination(Utilities.unsignedIntToLong(bytes4));
 		bytes4 = Utilities.clearBytes(bytes4);
 		// get source port
 		buf.get(bytes4, 0, PORT_SOURCE_SIZE);
 		event.setSport_itype(Utilities.unsignedIntToLong(bytes4));
 		bytes4 = Utilities.clearBytes(bytes4);
 		// get destination port
 		buf.get(bytes4, 0, PORT_DESTINATION_SIZE);
 		event.setDport_icode(Utilities.unsignedIntToLong(bytes4));
 		bytes4 = Utilities.clearBytes(bytes4);
 		// get protocol, packet action
 		bytes2 = Utilities.clearBytes(bytes2);
 		buf.get(bytes2, 0, PROTOCOL_PKTACTION_SIZE);
 		// use bit shifting to isolate the first of two bytes
 		event.setProtocol((short)(Utilities.unsignedShortToInt(bytes2)>>8));
 		// zero out the first byte to isolate the second byte
 		bytes2[0] = 0;
 		event.setPacket_action((short)(Utilities.unsignedShortToInt(bytes2)));
 		bytes2 = Utilities.clearBytes(bytes2);
 		// get padding
 		buf.get(bytes2, 0, PADDING_SIZE);
 		event.setPad(Utilities.unsignedShortToInt(bytes2));
 		bytes2 = Utilities.clearBytes(bytes2);
 	}
 	
 	public void readPacketHeader(long recordLength) {
 		buf = ByteBuffer.allocate(PACKET_HEADER_SIZE);
 		buf.clear();
 		try {
 			int nread = 0;		
 			do {
 				nread = fc.read(buf);			
 			} while (nread != -1 && buf.hasRemaining());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		buf.rewind();
 		// parse out packet
 		packet = this.getPacketClear();
 		bytes4 = Utilities.clearBytes(bytes4);
 		buf.get(bytes4, 0, SENSOR_ID_SIZE);
 		// get sensor id
 		packet.setSensor_id(Utilities.unsignedIntToLong(bytes4));
 		bytes4 = Utilities.clearBytes(bytes4);
 		// get event id
 		buf.get(bytes4, 0, EVENT_ID_SIZE);
 		packet.setEvent_id(Utilities.unsignedIntToLong(bytes4));
 		bytes4 = Utilities.clearBytes(bytes4);
 		// get seconds
 		buf.get(bytes4, 0, EVENT_SEC_SIZE);
 		packet.setEvent_second(Utilities.unsignedIntToLong(bytes4));
 		bytes4 = Utilities.clearBytes(bytes4);
 		// get packet seconds
 		buf.get(bytes4, 0, PACKET_SEC_SIZE);
 		packet.setPacket_second(Utilities.unsignedIntToLong(bytes4));
 		bytes4 = Utilities.clearBytes(bytes4);
 		// get packet mseconds
 		buf.get(bytes4, 0, PACKET_MSEC_SIZE);
 		packet.setPacket_microsecond(Utilities.unsignedIntToLong(bytes4));
 		bytes4 = Utilities.clearBytes(bytes4);
 		// get link type
 		buf.get(bytes4, 0, LINK_TYPE_SIZE);
 		packet.setLinktype(Utilities.unsignedIntToLong(bytes4));
 		bytes4 = Utilities.clearBytes(bytes4);
 		// get packet length
 		buf.get(bytes4, 0, PACKET_LENGTH_SIZE);
 		packet.setPacket_length(Utilities.unsignedIntToLong(bytes4));
 		bytes4 = Utilities.clearBytes(bytes4);
 
 	}		
 	
 	private ByteBuffer parseEthernetFramePacket(FileChannel fc) {
 		buf = ByteBuffer.allocate((int)packet.packet_length);
 		buf.clear();
 		try {			
 			int nread = 0;		
 			do {
 				nread = fc.read(buf);			
 			} while (nread != -1 && buf.hasRemaining());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		buf.rewind();
 		// parse out packet		
 		bytes6 = Utilities.clearBytes(bytes6);
 		// get dst mac address
 		buf.get(bytes6, 0, (int) EthernetFramePacket.DST_SIZE);
 		ethernetPacket.setEtherDestination(Utilities.sixBytesToLong(bytes6));
 		bytes6 = Utilities.clearBytes(bytes6);
 		// get src mac address
 		buf.get(bytes6, 0, (int) EthernetFramePacket.SRC_SIZE);
 		ethernetPacket.setEtherSource(Utilities.sixBytesToLong(bytes6));
 		bytes6 = Utilities.clearBytes(bytes6);
 		// get frame type
 		bytes2 = Utilities.clearBytes(bytes2);
 		buf.get(bytes2, 0, (int) EthernetFramePacket.FRAME_TYPE_SIZE);
 		ethernetPacket.setFrameType(Utilities.unsignedShortToInt(bytes2));
 		return buf;
 	}
 	
 	private void parseIPPacket(ByteBuffer buf) {
 
 		bytes2 = Utilities.clearBytes(bytes2);
 		// get ip protocol and length
 		buf.get(bytes2, 0, IPPacket.VERSION_IHL_TOS_SIZE);
 		this.ipPacket.setVersionIhl(Utilities.unsignedShortToInt(bytes2));
 		// it is the first of the two bytes in bytes2
 		// bit shifting is used to isolate the first byte
 		this.ipPacket.setVersionIhl(this.ipPacket.getVersionIhl()>>8);		
 		// get tos
 		// it is the last of the two bytes in bytes2
 		// zero out the first byte to isolate the last byte
 		bytes2[0] = 0;
 		this.ipPacket.setTos(Utilities.unsignedShortToInt(bytes2));
 		bytes2 = Utilities.clearBytes(bytes2);	
 		// get length of IP header
 		buf.get(bytes2, 0, IPPacket.LENGTH_SIZE);
 		this.ipPacket.setLen(Utilities.unsignedShortToInt(bytes2));
 		bytes2 = Utilities.clearBytes(bytes2);
 		// get ID
 		buf.get(bytes2, 0, IPPacket.ID_SIZE);
 		this.ipPacket.setId(Utilities.unsignedShortToInt(bytes2));
 		bytes2 = Utilities.clearBytes(bytes2);
 		// get Flag/Frag
 		buf.get(bytes2, 0, IPPacket.FLAG_FRAG_SIZE);
 		this.ipPacket.setFlagFrag(Utilities.unsignedShortToInt(bytes2));
 		bytes2 = Utilities.clearBytes(bytes2);
 		// get ttl 
 		// bit shift to isolate the first byte
 		buf.get(bytes2, 0, IPPacket.TTL_PROTO_SIZE);
 		this.ipPacket.setTtl((short)(Utilities.unsignedShortToInt(bytes2)>>8));
 		// get protocol
 		// zero out the first byte to isolate the last byte
 		bytes2[0] = 0;
 		this.ipPacket.setProto((short)(Utilities.unsignedShortToInt(bytes2)));
 		bytes2 = Utilities.clearBytes(bytes2);
 		// get chksum
 		buf.get(bytes2, 0, IPPacket.CHKSUM_SIZE);
 		this.ipPacket.setChecksum(Utilities.unsignedShortToInt(bytes2));
 		bytes2 = Utilities.clearBytes(bytes2);
 		// get source
 		bytes4 = Utilities.clearBytes(bytes4);
 		buf.get(bytes4, 0, IPPacket.SRC_SIZE);
 		this.ipPacket.setIpSource(Utilities.unsignedIntToLong(bytes4));
 		bytes4 = Utilities.clearBytes(bytes4);
 		// get destination
 		buf.get(bytes4, 0, IPPacket.DST_SIZE);
 		this.ipPacket.setIpDestination(Utilities.unsignedIntToLong(bytes4));
 		bytes4 = Utilities.clearBytes(bytes4);
 		
 	}
 	
 	public void parseTCPPacket(ByteBuffer buf) {
 		bytes4 = Utilities.clearBytes(bytes4);
 		// get source port
 		buf.get(bytes4, 0, TCPPacket.PORT_SIZE);
 		this.tcpPacket.setPortSource(Utilities.unsignedShortToInt(bytes4));
 		// get destination port
 		bytes4 = Utilities.clearBytes(bytes4);
 		buf.get(bytes4, 0, TCPPacket.PORT_SIZE);
 		this.tcpPacket.setPortDestination(Utilities.unsignedShortToInt(bytes4));
 		// get sequence number
 		bytes4 = Utilities.clearBytes(bytes4);
 		buf.get(bytes4, 0, TCPPacket.SEQ_SIZE);
 		this.tcpPacket.setSequence(Utilities.unsignedIntToLong(bytes4));
 		// get ack number
 		bytes4 = Utilities.clearBytes(bytes4);
 		buf.get(bytes4, 0, TCPPacket.ACK_SIZE);
 		this.tcpPacket.setAck(Utilities.unsignedIntToLong(bytes4));
 		// get offset number
 		bytes2 = Utilities.clearBytes(bytes2);
 		buf.get(bytes2, 0, TCPPacket.OFFSET_SIZE);
 		this.tcpPacket.setOffset(Utilities.unsignedShortToInt(bytes2));
 		// get window size
 		bytes2 = Utilities.clearBytes(bytes2);
 		buf.get(bytes2, 0, TCPPacket.WIN_SIZE);
 		this.tcpPacket.setWin(Utilities.unsignedShortToInt(bytes2));
 		// get chksum
 		bytes2 = Utilities.clearBytes(bytes2);
 		buf.get(bytes2, 0, TCPPacket.CHKSUM_SIZE);
 		this.tcpPacket.setChksum(Utilities.unsignedShortToInt(bytes2));
 		// get URG_P
 		bytes2 = Utilities.clearBytes(bytes2);
 		buf.get(bytes2, 0, TCPPacket.URGP_SIZE);
 		this.tcpPacket.setUrg_p(Utilities.unsignedShortToInt(bytes2));
 		// get payload		
 		byte[] payload_bytes = new byte[buf.limit() - buf.position()];
 		buf.get(payload_bytes, 0, buf.limit() - buf.position());
 		this.tcpPacket.setPayload(Hex.encodeHex(payload_bytes));		
 	}
 	
 	public String toString() {
 		String s = "";
 		s += this.header.toString() + "\n";
 		s += this.packet + "\n";
 		return s;
 	}
 	
 	public static final int HEADER_SIZE = 8;
 	public static final int PACKET_HEADER_SIZE = 28;
 	public static final int EVENT_HEADER_SIZE = 52;
 	
 	public static final int TYPE_SIZE = 4;
 	public static final int LENGTH_SIZE = 4;
 	public static final int SENSOR_ID_SIZE = 4;
 	public static final int EVENT_ID_SIZE = 4;
 	public static final int EVENT_SEC_SIZE = 4;
 	public static final int EVENT_MSEC_SIZE = 4;
 	public static final int SIG_ID_SIZE = 4;
 	public static final int GEN_ID_SIZE = 4;
 	public static final int SIG_REV_SIZE = 4;
 	public static final int CLASS_ID_SIZE = 4;
 	public static final int PRIORITY_SIZE = 4;
 	public static final int IP_SOURCE_SIZE = 4;
 	public static final int IP_DESTINATION_SIZE = 4;
 	public static final int PORT_SOURCE_SIZE = 4;
 	public static final int PORT_DESTINATION_SIZE = 4;
 	public static final int PROTOCOL_PKTACTION_SIZE = 2;
 	public static final int PADDING_SIZE = 2;
 	
 	
 	public static final int PACKET_SEC_SIZE = 4;
 	public static final int PACKET_MSEC_SIZE = 4;
 	public static final int LINK_TYPE_SIZE = 4;
 	public static final int PACKET_LENGTH_SIZE = 4;
 
 	public static final int ETHERNET_LINK = 1;
 	public static final int TCP_PROTOCOL = 6;
 	public static final int UDP_PROTOCOL = 11;
 }
