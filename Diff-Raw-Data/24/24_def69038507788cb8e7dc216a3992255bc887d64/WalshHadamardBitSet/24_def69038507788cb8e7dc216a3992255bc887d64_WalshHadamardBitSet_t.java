 package walsh_hadamard;
 
 import java.util.BitSet;
 import java.util.Random;
 
 public class WalshHadamardBitSet {
 
	private final Random rand = new Random();

 	public BitSet encrypt(BitSet message, int messageLengthBits) {
 		int cypherLength = (int)Math.pow(messageLengthBits, 2);
 		BitSet output = new BitSet(cypherLength);
 		
 		int bitsWritten = 2;
 		
 		if(message.get(0)) {
 			output.set(1);
 		}
 		for(int i = 1; i < messageLengthBits; i++) {	//remaining bits of input
 			boolean flip = message.get(i);
 			for(int j = 0; j < bitsWritten; j++) {	//bits already worked out
 				boolean val = output.get(j);
 				output.set(j + bitsWritten, val ^ flip);
 			}
 			
 			bitsWritten *= 2;
 		}
 		
 		return output;
 	}
	
	public boolean recoverBitAtPosition (BitSet cyphertext, int messageLengthBits, int targetBit){
		int x1 = rand.nextInt(messageLengthBits);
		System.out.println(x1);
		int x2 = x1 ^ targetBit;
		System.out.println(x2);
		return cyphertext.get(x1) ^ cyphertext.get(x2);
	}
 
 	public BitSet decrypt(BitSet cyphertext, int messageLengthBits) {
 		int lengthOfPlaintext = binlog(messageLengthBits);
 		BitSet output = new BitSet(lengthOfPlaintext);
 		
 		for(int i = 0; i < lengthOfPlaintext; i++) {
 			int x1 = rand.nextInt(messageLengthBits);
 			System.out.println(x1);
			int x2 = x1 ^ (1 << i);
 			System.out.println(x2);
 			
 			output.set(i, cyphertext.get(x1) ^ cyphertext.get(x2));
 		}
 		
 		
 		return output;
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
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		WalshHadamardBitSet instance = new WalshHadamardBitSet();
		byte[] input = { 5 };
		BitSet cypher = instance.encrypt(BitSet.valueOf(input), 3);
		BitSet output = instance.decrypt(cypher, 8);
 		
 		System.out.println("Input: " + input[0]  + " Output: " + output);
 
 	}
 
 }
