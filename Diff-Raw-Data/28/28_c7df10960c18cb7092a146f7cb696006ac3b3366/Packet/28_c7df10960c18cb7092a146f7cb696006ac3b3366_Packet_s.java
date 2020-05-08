 package org.sdu.network;
 
 import java.net.Socket;
 
 /**
  * Warp byte stream data into abstract cloneable Packet class.
  * 
 * @version 0.1 rev 8001 Dec. 17, 2012.
  * Copyright (c) HyperCube Dev Team.
  */
public abstract class Packet
 {
 	/**
 	 * Retrive byte data from packet.
 	 * 
 	 * @return	Data reference.
 	 */
	public abstract byte[] getData();
 	
 	/**
 	 * Get packet's socket.
 	 * 
 	 * @return	Socket provided the packet.
 	 */
	public abstract Socket getSocket();
 }
