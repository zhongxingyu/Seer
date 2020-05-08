 
 package com.zanoccio.packetkit.headers;
 
 import com.zanoccio.packetkit.IP4Address;
 import com.zanoccio.packetkit.MACAddress;
 import com.zanoccio.packetkit.headers.annotations.FromNetworkInterface;
 import com.zanoccio.packetkit.headers.annotations.StaticFragment;
 
 public class ARPHeader extends AbstractPacketHeader {
 
 	@StaticFragment(slot = 0)
 	private ARPHardwareType hardwaretype;
 
 	@StaticFragment(slot = 1)
 	private ARPProtocolType protocoltype;
 
 	@StaticFragment(slot = 2, size = 1, fixed = true)
 	private int hardwaresize;
 
 	@StaticFragment(slot = 3, size = 1, fixed = true)
 	private int protocolsize;
 
 	@StaticFragment(slot = 4)
 	private ARPOpcode opcode;
 
 	@StaticFragment(slot = 5)
 	@FromNetworkInterface
 	private MACAddress sendermac;
 
 	@StaticFragment(slot = 6)
 	@FromNetworkInterface
 	private IP4Address senderip;
 
 	@StaticFragment(slot = 7)
 	private MACAddress targetmac;
 
 	@StaticFragment(slot = 8)
 	private IP4Address targetip;
 
 
 	/**
 	 * Constructs a whoHas ARP packet broadcast on the local network
 	 * 
 	 * @param addr
 	 * @return
 	 */
 	public static ARPHeader whoHas(IP4Address addr) {
 		ARPHeader header = new ARPHeader();
 		header.setHardwareType(ARPHardwareType.ETHERNET);
 		header.setProtocolType(ARPProtocolType.IP4);
 		header.setHardwareSize(6);
 		header.setProtocolSize(4);
 
 		header.setTargetMAC(MACAddress.EMPTY);
 		header.setTargetIP(addr);
		header.setOpcode(ARPOpcode.REQUEST);
 
 		return header;
 	}
 
 
 	public ARPHeader() {
 		setHardwareType(ARPHardwareType.ETHERNET);
 		setProtocolType(ARPProtocolType.IP4);
 		setHardwareSize(6); // for ETHERNET
 		setProtocolSize(4); // for IP
 
 		setOpcode(ARPOpcode.REQUEST);
 	}
 
 
 	//
 	// Accessors
 	//
 	public void setHardwareType(ARPHardwareType hardwaretype) {
 		this.hardwaretype = hardwaretype;
 	}
 
 
 	public ARPHardwareType getHardwareType() {
 		return hardwaretype;
 	}
 
 
 	public void setProtocolType(ARPProtocolType protocoltype) {
 		this.protocoltype = protocoltype;
 	}
 
 
 	public ARPProtocolType getProtocolType() {
 		return protocoltype;
 	}
 
 
 	public void setHardwareSize(int hardwaresize) {
 		this.hardwaresize = hardwaresize;
 	}
 
 
 	public int getHardwareSize() {
 		return hardwaresize;
 	}
 
 
 	public void setProtocolSize(int protocolsize) {
 		this.protocolsize = protocolsize;
 	}
 
 
 	public int getProtocolSize() {
 		return protocolsize;
 	}
 
 
 	public void setOpcode(ARPOpcode opcode) {
 		this.opcode = opcode;
 	}
 
 
 	public ARPOpcode getOpcode() {
 		return opcode;
 	}
 
 
 	public void setSenderMAC(MACAddress sendermac) {
 		this.sendermac = sendermac;
 	}
 
 
 	public MACAddress getSenderMAC() {
 		return sendermac;
 	}
 
 
 	public void setSenderIP(IP4Address senderip) {
 		this.senderip = senderip;
 	}
 
 
 	public IP4Address getSenderIP() {
 		return senderip;
 	}
 
 
 	public void setTargetMAC(MACAddress targetmac) {
 		this.targetmac = targetmac;
 	}
 
 
 	public MACAddress getTargetMAC() {
 		return targetmac;
 	}
 
 
 	public void setTargetIP(IP4Address targetip) {
 		this.targetip = targetip;
 	}
 
 
 	public IP4Address getTargetIP() {
 		return targetip;
 	}
 
 }
