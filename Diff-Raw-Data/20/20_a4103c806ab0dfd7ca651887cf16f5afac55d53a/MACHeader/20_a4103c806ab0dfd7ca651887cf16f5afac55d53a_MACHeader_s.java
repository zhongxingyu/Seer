 
 package com.zanoccio.packetkit.headers;
 
 import com.zanoccio.packetkit.MACAddress;
 import com.zanoccio.packetkit.headers.annotations.FromNetworkInterface;
 import com.zanoccio.packetkit.headers.annotations.StaticFragment;
 
 public class MACHeader extends AbstractPacketHeader {
 
 	@StaticFragment(slot = 0)
	private MACAddress destination;
 
 	@StaticFragment(slot = 1)
 	@FromNetworkInterface
	private MACAddress source;
 
 	@StaticFragment(slot = 2)
	private EtherType type;
 
 
 	public static MACHeader broadcast() {
 		MACHeader header = new MACHeader();
 		header.setDestination(MACAddress.BROADCAST);
 
 		return header;
 	}
 
 
 	//
 	// Accessors
 	//
 
 	public void setDestination(MACAddress destination) {
 		this.destination = destination;
 	}
 
 
 	public MACAddress getDestination() {
 		return destination;
 	}
 
 
 	public void setSource(MACAddress source) {
 		this.source = source;
 	}
 
 
 	public MACAddress getSource() {
 		return source;
 	}
 
 
 	public void setType(EtherType type) {
 		this.type = type;
 	}
 
 
 	public EtherType getType() {
 		return type;
 	}
 
 }
