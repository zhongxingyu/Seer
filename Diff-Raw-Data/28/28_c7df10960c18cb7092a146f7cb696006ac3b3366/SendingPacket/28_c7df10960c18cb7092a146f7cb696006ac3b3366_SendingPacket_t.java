 package org.sdu.network;
 
 import java.net.Socket;
 
public class SendingPacket implements Packet{
 	private final byte[] arr;
 	private final Socket socket;
 	
 	public SendingPacket(Socket s, byte[] data)
 	{
 		arr = data;
 		socket = s;
 	}
 
 	public byte[] getData() {
 		return arr;
 	}
 
 	public Socket getSocket() {
 		return socket;
 	}
 }
