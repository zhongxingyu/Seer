 package walsh_hadamard;
 
 import java.util.BitSet;
 import java.util.Random;
 
 public class WalshHadamard {
 
 	private final static Random rand = new Random();
 
 	public static BitSet encode(BitSet message, int messageLengthBits) {
 		int cypherLength = (int)Math.pow(2, messageLengthBits);
 		BitSet output = new BitSet(cypherLength);
 		int bitsWritten = 2;
 		
 		if(message.get(0)) {
 			output.set(cypherLength-2);
 		}
 		for(int i = 1; i < messageLengthBits; i++) {	//remaining bits of input
 			boolean flip = message.get(i);
 			for(int j = cypherLength-1; j >=cypherLength-bitsWritten ; j--) {	//bits already worked out
 				boolean val = output.get(j);
 				output.set(j - bitsWritten, val ^ flip);
 			}
 			
 			bitsWritten *= 2;
 		}
 		
 		return output;
 	}
 	
 	public static BitSet decode(BitSet cyphertext, int messageLengthBits) {
 		int lengthOfPlaintext = binlog(messageLengthBits);
 		BitSet output = new BitSet(lengthOfPlaintext);
 		
 		for(int i = 0; i < lengthOfPlaintext; i++) {
 			int x1 = rand.nextInt(messageLengthBits);
 			int x2 = x1 ^ (1 << i);
 			
 			output.set(i, cyphertext.get(x1) ^ cyphertext.get(x2));
 		}
 		
 		
 		return output;
 	}
 	
 	public static boolean recoverBitAtPosition (BitSet cyphertext, int messageLengthBits, int targetBit){
 		int codeLength = (int)Math.pow(2, messageLengthBits);
 		int x1 = rand.nextInt(messageLengthBits);
		int x2 = x1 ^ (codeLength-targetBit-1);
 		return cyphertext.get(codeLength-x1-1) ^ cyphertext.get(codeLength-x2-1);
 	}
 	
 	public static int binlog( int bits ) // returns 0 for bits=0
 	{
 	    int log = 0;
 	    if( ( bits & 0xffff0000 ) != 0 ) { bits >>>= 16; log = 16; }
 	    if( bits >= 256 ) { bits >>>= 8; log += 8; }
 	    if( bits >= 16  ) { bits >>>= 4; log += 4; }
 	    if( bits >= 4   ) { bits >>>= 2; log += 2; }
 	    return log + ( bits >>> 1 );
 	}
 
 }
