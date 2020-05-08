 package net.buddat.mineserv.net.packet;
 
 import java.io.UnsupportedEncodingException;
 
 public class PacketBuilder {
 
 	private byte[] payload;
 	private int curLength = 0;
 	private int id;
 	
 	public PacketBuilder(int size) {
 		payload = new byte[size];
 	}
 	
 	public PacketBuilder setId(int id) {
 		this.id = id;
 		return this;
 	}
 	
 	public PacketBuilder addBytes(byte[] b) {
 		System.arraycopy(b, 0, payload, curLength, b.length);
 		curLength += b.length;
 		
 		return this;
 	}
 	
 	public PacketBuilder addByte(byte b) {
 		payload[curLength++] = b;
 		
 		return this;
 	}
 	
 	public PacketBuilder addShort(short s) {
 		addByte((byte) (s >> 8));
 		addByte((byte) s);
 		
 		return this;
 	}
 	
 	public PacketBuilder addInt(int i) {
 		addByte((byte) (i >> 24));
 		addByte((byte) (i >> 16));
 		addByte((byte) (i >> 8));
 		addByte((byte) i);
 		
 		return this;
 	}
 	
 	public PacketBuilder addLong(long l) {
 		addInt((int) (l >> 32));
 		addInt((int) (l & -1L));
 		
 		return this;
 	}
 	
 	public PacketBuilder addString(String s, String encoding) {
 		if (!encoding.equals("UTF-16BE") && !encoding.equals("UTF-8"))
 			encoding = "UTF-16BE";
 		
 		try {
 			byte[] sBytes = s.getBytes(encoding);
 			
			addShort((short) sBytes.length);
 			System.arraycopy(sBytes, 0, payload, curLength, sBytes.length);
 			
 			curLength += sBytes.length;
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		return this;
 	}
 	
 	public Packet toPacket() {
 		return new Packet(null, id, payload);
 	}
 
 }
