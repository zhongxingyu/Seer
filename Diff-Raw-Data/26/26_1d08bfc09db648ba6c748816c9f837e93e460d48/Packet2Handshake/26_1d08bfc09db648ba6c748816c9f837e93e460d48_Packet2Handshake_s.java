 package org.Epixcrafted.EpixServer.protocol;
 
 import org.Epixcrafted.EpixServer.misc.NotSupportedOperationException;
 import org.jboss.netty.buffer.ChannelBuffer;
 
 public class Packet2Handshake extends Packet{
 
 	public int protocol;
 	public String username;
 	public String password;
 	public String host;
 	public int port;
 	
 	@Override
 	public int getPacketId() {
 		return (int) 0x2;
 	}
 	
 	@Override
 	public void get(ChannelBuffer buf) {
 		protocol = buf.readByte();
 		int length = buf.readShort();
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < length; i++) sb.append(buf.readChar());
 		username = sb.toString();
		/*length = buf.readShort();
		sb = new StringBuilder();
		for (int i = 0; i < length; i++) sb.append(buf.readChar());
		password = sb.toString();*/
 		length = buf.readShort();
 		sb = new StringBuilder();
 		for (int i = 0; i < length; i++) sb.append(buf.readChar());
 		host = sb.toString();
 		port = buf.readInt();
 	}
 	
 	@Override
 	public ChannelBuffer send(ChannelBuffer buf) throws NotSupportedOperationException {
 		throw new NotSupportedOperationException();
 	}
 }
