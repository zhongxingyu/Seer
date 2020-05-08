 package serialclient;
 
 
 public class PacketInfo {
 	public static byte PACKET_FLAG = (byte) 0x7e;
 	public static byte CONTROL_FLAG = (byte) 0x7d;
 	public static byte PF_CID = (byte) 0x5e;
 	public static byte CF_CID = (byte) 0x5d;
 
 	private byte[] bytes;
 	private int buffer_length;
 	private byte checksum, data_length1, data_length2;
 
 	public PacketInfo(byte[] bytes){
 		this.bytes = bytes;
 		buffer_length = 2; // start + end packet flags
 		checksum = 0;
 		data_length1 = (byte) bytes.length;
 		data_length2 = (byte) (bytes.length >> 8);
 
 		for(byte b : bytes){
 			addLength(b);
 			checksum += b;
 		}
 
 		addLength(checksum);
 		addLength(data_length1);
 		addLength(data_length2);
 	}
 
     public byte[] packageBytes(){
 		byte[] buffer = new byte[buffer_length];
 
 		int i = 0;
 		buffer[i] = PACKET_FLAG;
		i = addNext(buffer, i, data_length1);
		i = addNext(buffer, i, data_length2);
 		for(byte b : bytes){
			i = addNext(buffer, i, b);
 		}
		i = addNext(buffer, i, checksum);
 		buffer[++i] = PACKET_FLAG;
 
 		return buffer;
 	}
 
 	private void addLength(byte b){
 		buffer_length += ((b == PACKET_FLAG || b == CONTROL_FLAG) ? 2 : 1);
 	}
 
	private static int addNext(byte[] buffer, int offset, byte b){
 		if(b == PACKET_FLAG){
			buffer[++offset] = CONTROL_FLAG;
 			buffer[++offset] = PF_CID;
 		}
 		else if(b == CONTROL_FLAG){
			buffer[++offset] = CONTROL_FLAG;
 			buffer[++offset] = CF_CID;
 		}
		else{
			buffer[++offset] = b;
		}
		return offset;
 	}
 }
