 package base.game.network.packets.TCP;
 
 import java.nio.ByteBuffer;
 
 import base.game.network.packets.TCP_Packet;
 
 public class PlayRequestPacket extends TCP_Packet {
 
 	private byte shipID;
 	private static final int dimension = 1;
 
 	public PlayRequestPacket(byte shipID) {
 		super(TCP_PacketType.PLAY_REQUEST);
 		this.shipID = shipID;
 		createBuffer();
 		setComplete(true); // we created it so it better be!
 	}
 
 	public PlayRequestPacket(ByteBuffer buffer) {
 		super(TCP_PacketType.PLAY_REQUEST);
 		this.buffer = buffer;
 		setComplete(recognizePacket());
 	}
 
 	@Override
 	public void createBuffer() {
 		buffer = ByteBuffer.allocate(dimension+1);
 		buffer.clear();
		buffer.put((byte) -127);
 		buffer.put(shipID);
 		buffer.rewind();
 	}
 
 	@Override
 	protected boolean recognizePacket() {
 		if (buffer.remaining()<dimension)
 			return false;
 		shipID = buffer.get();
 		return true;
 	}
 
 }
