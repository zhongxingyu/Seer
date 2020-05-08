 package hu.edudroid.sniffer;
 
 import hu.edudroid.tcp_utils.TCPIPUtils;
 
 import java.nio.ByteBuffer;
 
 public class TCPPacket extends TransportPacket {
 	
 	//TCP specific
 	public boolean URG = false;
 	public boolean ACK = false;
 	public boolean PSH = false;
 	public boolean RST = false;
 	public boolean SYN = false;
 	public boolean FIN = false;
 	public int window;
 	public long seqNum; //Sequence Number
 	public long ackNum; //Acknowledgement Number
 	public int urgPointer;
 	private int headerLength;
 	private byte[] data;
 
 	public TCPPacket(ByteBuffer buffer, int startIndex, int packetLength) {
 		super(buffer, startIndex, packetLength);
 		seqNum = TCPIPUtils.toLong(buffer.array()[startIndex + 4],buffer.array()[startIndex + 5],buffer.array()[startIndex + 6],buffer.array()[startIndex + 7]);
 		ackNum = TCPIPUtils.toLong(buffer.array()[startIndex + 8],buffer.array()[startIndex + 9],buffer.array()[startIndex + 10],buffer.array()[startIndex + 11]);
 		headerLength = TCPIPUtils.toIntUnsigned(ZERO, buffer.array()[startIndex + 12]);
 		setFlags(buffer.array()[startIndex + 13]);
 		window = TCPIPUtils.toIntUnsigned(buffer.array()[startIndex + 14], buffer.array()[startIndex + 15]);
 		if(URG){
 			urgPointer = TCPIPUtils.toIntUnsigned(buffer.array()[startIndex + 18], buffer.array()[startIndex + 19]);
 		}
 		data = new byte[packetLength - headerLength];
 		System.arraycopy(buffer, 0, data, 0, data.length);
 	}
 
 	public TCPPacket(int sourcePort, int destPort) {
 		super(sourcePort, destPort);
 		// TODO Generate packet from incoming data and headers
 	}
 	
 	public void setFlags(int flags) {
		if((flags & (1 << 5)) > 0) URG = true;
		if((flags & (1 << 4)) > 0) ACK = true;
		if((flags & (1 << 3)) > 0) PSH = true;
		if((flags & (1 << 2)) > 0) RST = true;
		if((flags & (1 << 1)) > 0) SYN = true;
		if((flags & (1 << 0)) > 0) FIN = true;
	} 	

 	public byte getFlags() {
 		int flags = 0;
 		
 		if(URG) flags = flags | (1 << 5);
 		if(ACK) flags = flags | (1 << 4);
 		if(PSH) flags = flags | (1 << 3);
 		if(RST) flags = flags | (1 << 2);
 		if(SYN)	flags = flags | (1 << 1);
 		if(FIN) flags = flags | (1 << 0);
 		
 		return (byte) flags;
 	}
 
 
 	@Override
 	public byte[] toBytes() {
 		byte[] ret = new byte[data.length];
 		writeBytes(ret,0);
 		return ret;
 	}
 
 	@Override
 	public void writeBytes(byte[] buffer, int start) {
 		int packetLength = 3;
 		System.arraycopy(TCPIPUtils.toTwoBytes(sourcePort), 0, buffer, 0, 2); // Source port
 		System.arraycopy(TCPIPUtils.toTwoBytes(destPort), 0, buffer, 2, 2); // Destination port
 		System.arraycopy(TCPIPUtils.toFourBytes(seqNum), 0, buffer, 4, 4); // Sequence number
 		System.arraycopy(TCPIPUtils.toFourBytes(ackNum), 0, buffer, 8, 4); // Acknowledgement number (if ACK set)
 		buffer[12] = (byte) ((headerLength << 4) & 0xF0); // Data offset + Reserved
 		buffer[13] = getFlags(); //TCP flags
 		System.arraycopy(TCPIPUtils.toTwoBytes(window), 0, buffer, 14, 2); // Window
 		System.arraycopy(TCPIPUtils.toTwoBytes(0), 0, buffer, 16, 2); // 0's for checksum calculation
 		if(URG){
 			System.arraycopy(TCPIPUtils.toTwoBytes(urgPointer), 0, buffer, 18, 2); // Urgent pointer (if URG set)
 		}
 		else {
 			System.arraycopy(TCPIPUtils.toTwoBytes(0), 0, buffer, 18, 2);
 		}
 		// No options
 		System.arraycopy(TCPIPUtils.toTwoBytes(TCPIPUtils.checksum(buffer, start, packetLength)), 0, buffer, 16, 2); // UDP checksum
 	}
 
 	@Override
 	public int getPacketLength() {
 		return headerLength + data.length;
 	}
 
 	@Override
 	public int getPayloadLength() {
 		return data.length;
 	}
 
 	@Override
 	public byte[] getPayload() {
 		return data;
 	}
 
 	@Override
 	public void writePayload(byte[] buffer, int start) {
 		System.arraycopy(data, 0, buffer, start, data.length);
 	}
 
 }
