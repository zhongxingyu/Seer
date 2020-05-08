 package org.sdu.network;
 
 import java.net.Socket;
 
 /**
  * IncomingPacket class inherited from Packet class.
  * 
 * @version 0.1 rev 8002 Dec. 27, 2012.
  * Copyright (c) HyperCube Dev Team.
  */
public class IncomingPacket implements Packet
 {
 	private final byte[] arr;
 	private final Socket socket;
 	
 	/**
 	 * Make a packet from/to Socket s and set Byte[] data as source.
 	 * 
 	 * @param s		Socket locating the packet
 	 * @param data	Packet data
 	 */
 	public IncomingPacket(Socket s, byte[] data)
 	{
 		arr = data;
 		socket = s;
 	}
 
 	@Override
 	public byte[] getData() {
 		return arr;
 	}
 
 	@Override
 	public Socket getSocket() {
 		return socket;
 	}
 }
