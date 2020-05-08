 package edu.wpi.tmathmeyer.protocol;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 public interface Packet {
 	/**
 	 * 
 	 * @return the ID of the packet
 	 */
 	public byte getPacketID();
 	
 	
 	/**
 	 * 
 	 * @param d the dataoutputstream for the packet data to be written to
 	 * @throws IOException a closed or malformed DoS
 	 */
	public void write(DataOutputStream d) throws IOException;
 }
