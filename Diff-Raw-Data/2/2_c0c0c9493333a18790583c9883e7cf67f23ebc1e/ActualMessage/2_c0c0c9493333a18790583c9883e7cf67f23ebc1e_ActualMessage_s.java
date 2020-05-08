 import java.io.Serializable;
 import java.util.BitSet;
 
 
 public class ActualMessage implements Serializable {
 	
 
 	private static final long serialVersionUID = 3545119718998974040L;
 	int length;
 	Byte messageType;
 	byte[] messagePayload;
 	String stringPayload;
 	int chunkid;
 	
 	public int getChunkid(){ return chunkid;}
 	public void setChunkid(int a) { chunkid=a;}
 		
 	//Constructor for bitfield message
 	ActualMessage(int pieces) {
 		messageType = new Byte ((byte)5);
 		BitSet bitfield = new BitSet(pieces);
 		messagePayload = new byte[bitfield.length()/8];
 		messagePayload = toByteArray(bitfield);
 		length = messagePayload.length;
 	}
 	
 	//Constructor for messages with chunk
 	ActualMessage(byte[] myChunk) {
 		messageType = new Byte ((byte)7);
 //		messagePayload = new byte(myChunk);
 //		messagePayload = new byte[myChunk.length];
		messagePayload = myChunk;
 		length = messagePayload.length;
 //		peerProcess.logger.println("Actual message payload content is \n\n\n"+new String(messagePayload)+"\n\n");
 	}
 	
 	ActualMessage(String mystring){
 		stringPayload=mystring;
 		length = stringPayload.length();
 		messagePayload = stringPayload.getBytes();
 	}
 
 	
 /*	ActualMessage(int l,Byte mT, Byte mP) {
 		length = l;
 		messageType = new Byte(mT); 
 		messagePayload = new Byte(mP); // Single chunk in byte
 		bitfield = new BitSet(l);
 		bitfield.set(1,true);
 	}*/
 	
 	public BitSet toBitSet(byte[] bytes) {
 		BitSet bits = new BitSet();
 		for(int i=0;i<bytes.length*8;i++) {
 			if((bytes[bytes.length-i/8-1]&(1<<(i%8))) > 0){
 				bits.set(i);
 			}
 		}
 		return bits;
 	}
 	
 	public byte[] toByteArray (BitSet bits) {
 		byte[] bytes = new byte[bits.length()/8+1];
 		for(int i=0;i<bits.length();i++) {
 			if(bits.get(i)) {
 				bytes[bytes.length-i/8-1] |= 1<<(i%8);
 			}
 		}
 		return bytes;
 	}
 	
 	public static void main(String arg[])
 	{
 		ActualMessage a = new ActualMessage(306);
 		System.out.println(a.length);
 		//System.out.println(a.messagePayload);
 		/*
 		BitSet mb = new BitSet(10);
 		mb.set(2);
 		System.out.println(mb.get(2));
 		System.out.println(mb.get(9));
 		byte[] mby = new byte[mb.length()/8+1];
 		mby = toByteArray(mb);
 		for (int i =0;i<mby.length;i++){
 		System.out.println(mby[i]);}
 		
 		BitSet mb2 = new BitSet();
 		mb2 = toBitSet(mby);
 		System.out.println(mb2.get(2));
 		System.out.println(mb2.get(9));
 		//System.out.println(a.messageType);
 		//System.out.println(a.bitfield.toString());
 		//System.out.println(a.bitfield.get(0));
 		 * */
 		 
 	}
 
 }
