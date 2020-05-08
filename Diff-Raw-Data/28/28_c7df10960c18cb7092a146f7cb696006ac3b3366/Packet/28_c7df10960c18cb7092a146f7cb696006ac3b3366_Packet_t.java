 package org.sdu.network;
 
 import java.net.Socket;
 
 /**
  * Warp byte stream data into abstract cloneable Packet class.
  * 
 * @version 0.1 rev 8002 Dec. 27, 2012.
  * Copyright (c) HyperCube Dev Team.
  */
public interface Packet
 {
 	/**
 	 * Retrive byte data from packet.
 	 * 
 	 * @return	Data reference.
 	 */
	public byte[] getData();
 	
 	/**
 	 * Get packet's socket.
 	 * 
 	 * @return	Socket provided the packet.
 	 */
	public Socket getSocket();
 }
