 package com.vloxlands.net.packet;
 
 /**
  * @author Dakror
  */
 public class Packet05UsernameTaken extends Packet
 {
 	
 	public Packet05UsernameTaken()
 	{
 		super(05);
 	}
 	
 	@Override
 	public byte[] getData()
 	{
		return "05".getBytes();
 	}
 }
