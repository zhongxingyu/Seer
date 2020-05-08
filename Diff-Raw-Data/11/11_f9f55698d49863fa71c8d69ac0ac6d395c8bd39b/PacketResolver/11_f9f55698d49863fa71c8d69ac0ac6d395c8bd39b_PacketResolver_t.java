 package org.sdu.command;
 
 import java.nio.ByteBuffer;
 import java.util.LinkedList;
 import org.sdu.net.Packet;
 
 /**
  * Resolve the packet on the client received from the server.
  * 
  * @version 0.1 rev 8000 Jan. 3, 2013.
  * Copyright (c) HyperCube Dev Team.
  */
 
 public class PacketResolver {
 
 	private byte statusMain;
 	private byte statusSub;
 	private byte instMain;
 	private byte instSub;
 	
 	public class par{
 		public int pos;
 		public int length;
 	}
 
 	private LinkedList<par> list;
 	
 	
 	private par part;
 	
 	/**
 	 * Initialize a PacketResovler object.
 	 * @param p --> The Packet received.
 	 */
 	public PacketResolver(Packet p){
 		resolveBuffer(p.getData());
 	}
 	
 	/**
 	 * Resolve the ByteBuffer in the Packet.
 	 * @param buf --> The ByteBuffer in the Packet.
 	 */
 	public void resolveBuffer(ByteBuffer buf){
 		setStatusMain(buf.get());
 		setStatusSub(buf.get());
 		setInstMain(buf.get());
 		setInstSub(buf.get());
 
 		list = new LinkedList<par>();
 		
 		while (buf.position() < buf.capacity()){
 		buf.get();
 		part = new par();
 		part.length = (buf.get() << 8) + buf.get();
 		part.pos = buf.position();
 		list.add(part);
 		buf.position(buf.position() + part.length);
 		}
 		
 	}
 
 	public byte getInstSub() {
 		return instSub;
 	}
 
	private void setInstSub(byte instSub) {
 		this.instSub = instSub;
 	}
 
 	public byte getInstMain() {
 		return instMain;
 	}
 
	private void setInstMain(byte instMain) {
 		this.instMain = instMain;
 	}
 
 	public byte getStatusSub() {
 		return statusSub;
 	}
 
	private void setStatusSub(byte statusSub) {
 		this.statusSub = statusSub;
 	}
 
 	public byte getStatusMain() {
 		return statusMain;
 	}
 ;
	private void setStatusMain(byte statusMain) {
 		this.statusMain = statusMain;
 	}	
 	
 	/**
 	 * A LinkedList of the params' positions and lengths.
 	 */
 	public LinkedList<par> getList(){
 		return list;
 	}
 }
