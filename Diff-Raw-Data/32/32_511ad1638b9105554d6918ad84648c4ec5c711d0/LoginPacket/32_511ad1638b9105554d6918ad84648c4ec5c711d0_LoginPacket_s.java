 package base.game.network.packets.TCP;
 
 import java.io.UnsupportedEncodingException;
 import java.nio.ByteBuffer;
 
 import base.game.network.packets.TCP_Packet;
 
 public class LoginPacket extends TCP_Packet {
 
 	private String username;
 	private static final int dimension = 30;
 
 	public String getUsername() {
 		return username;
 	}
 
 	public LoginPacket(String userName) {
 		super(TCP_PacketType.LOGIN);
 		if (userName.length() > 30)
 			userName = userName.substring(0, 30);
 		this.username = userName;
 		createBuffer();
 		setComplete(true); // we created it so it better be!
 	}
 
 	public LoginPacket(ByteBuffer buffer) {
 		super(TCP_PacketType.LOGIN);
 		this.buffer = buffer;
 		setComplete(recognizePacket());
 	}
 
 	@Override
 	public void createBuffer() {
 		buffer = ByteBuffer.allocate(dimension+1);
 		buffer.clear();
		buffer.put((byte) -128);
 
 		int i = 0;
 		try {
 			for (; i < username.getBytes("ASCII").length; i++)
 				buffer.put(username.getBytes("ASCII")[i]);
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 
 		for (; i < 30; i++)
 			buffer.put((byte) 32);
 		buffer.rewind();
 	}
 
 	@Override
 	protected boolean recognizePacket() {
 		if (buffer.remaining() < dimension)
 			return false;
 		
 		//TODO: check that input are valid values
 		char[] tmp = new char[30];
 		for (int i = 0; i < 30; i++)
 			tmp[i] = (char) buffer.get();
 		username = String.copyValueOf(tmp).trim();
 		return true;
 	}
 
 }
