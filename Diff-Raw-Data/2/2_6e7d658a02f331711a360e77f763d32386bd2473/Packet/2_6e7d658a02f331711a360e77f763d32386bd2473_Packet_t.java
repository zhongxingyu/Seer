 package wifi;
 
 import java.nio.ByteBuffer;
 import java.util.Arrays;
 
 /**
  * @author Alexander  King & Michael Lim
  * 
  * The Packet class wraps all neccesary information that is required for 
  * a transmission for the 802.11~ spec.
  * 
  */
 
 public class Packet {
 
 	//byte[] frame;
 	
 	int frameType;
 	int retry = 0;
 	short seqNum;
 	short destAddr;
 	short srcAddr;
 	byte[] data;
 	byte[] crc;
 	ByteBuffer buf;
 	
 	public Packet(byte[] frame){
 		if(frame == null){
 			throw new IllegalArgumentException("Invalid packet. Packet is null.");
 		}else if(frame.length > 2038){
 			throw new IllegalArgumentException("Invalid packet size. Packet too big!");
 		} else if( frame.length < 1 ){
 			throw new IllegalArgumentException("Invalid packet size. No packet data.");
 		}
 		
 		buf = ByteBuffer.allocate(10 + data.length);
 	}
 	
 	public Packet(int frameType, short seqNum, short destAddr, short srcAddr, byte[] data, byte[] crc){
 		
 		buf = ByteBuffer.allocate(10 + data.length);
 		
 		setFrameType(frameType);
 		setSeqNum(seqNum);		
 		setDestAddr(destAddr);
 		setSrcAddr(srcAddr);
 				
 		//Check data
 		if(data == null || data.length > 2038){
 			throw new IllegalArgumentException("Invalid data.");
 		}else{
 			this.data = data;
 		}
 		
 		//Check CRC
 		if(crc == null ||crc.length != 4){
 			throw new IllegalArgumentException("Invalid CRC..");
 		}else{
 			this.crc = crc;
 		}
 		
 		fillPacket();
 	}
 	
 	private void fillPacket(){
 		byte control = makeControl(frameType,this.retry, seqNum);
 		buf.put(0, control); //put control bytes
 		buf.putShort(2, destAddr); // put destAddr bytes
		buf.putShort(4, srcAddr); // put srcAddr bytes
 		for(int i=0;i<data.length;i++){ //put data bytes
 			buf.put(i+5,data[i]);
 		}
 		for(int i=0;i<crc.length;i++){ //put crc bytes
 			buf.put(buf.limit()-4,crc[i]);
 		}
 	}
 	
 	private byte makeControl(int frameType, int retry, int seqNum){
 		int temp = 0;
 		temp = (frameType << 13) | (retry << 12) | seqNum;
 		//Test Shifting
 		//System.out.println("frameType: " + (frameType << 5));
 		//System.out.println("retry: " + retry);
 		//System.out.println("seqNum: " + seqNum);
 		//System.out.println("byte: " + temp);
 		return (byte)temp;
 	}
 	
 	public void setFrameType(int type){
 		//Check frame type
 		if(type < 0 || type > 7){
 			throw new IllegalArgumentException("Invalid frameType.");
 		}else{
 			frameType = type;
 		}
 	}
 	
 	public int getFrameType(){
 		return frameType;
 	}
 	
 	public boolean isRetry(){
 		if(retry == 1){
 			return true;
 		}
 		else{
 		return false;
 		}
 	}
 	
 	public void setSeqNum(short seq){
 		//Check seqNum
 		if(seq < 0 || seq > 4095){
 			throw new IllegalArgumentException("Invalid seqNum.");
 		}else{
 			seqNum = seq;
 		}
 	}
 	
 	public int getSeqNum(){
 		return seqNum;
 	}
 	
 	public void setDestAddr(short addr){
 		//Check destAddr
 		if(addr < 0 || addr > 65535){
 			throw new IllegalArgumentException("Invalid destAddr.");
 		}else{
 			destAddr = addr;
 		}
 	}
 	
 	public int getDestAddr(){
 		return destAddr;
 	}
 	
 	public void setSrcAddr(short addr){
 		//Check srcAddr
 		if(addr < 0 || addr > 65535){
 			throw new IllegalArgumentException("Invalid srcAddr.");
 		}else{
 			this.srcAddr = addr;
 		}
 	}
 	
 	public int getSrcAddr(){
 		return srcAddr;
 	}
 	
 	public void setData(byte[] inData){
 		//Check data
 		if(data == null || data.length > 2038){
 			throw new IllegalArgumentException("Invalid data.");
 		}else{
 			data = inData;
 		}
 	}
 	
 	public byte[] getData(){
 		return data;
 	}
 	
 	
 	public byte[] getCrc(){
 		return crc;
 	}
 	
 	public byte[] getFrame(){
 		fillPacket();
 		return buf.array();
 	}
 }
