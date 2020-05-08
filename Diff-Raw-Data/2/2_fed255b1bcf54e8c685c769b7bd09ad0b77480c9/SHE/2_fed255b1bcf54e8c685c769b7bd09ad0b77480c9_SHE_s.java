 /**
  * 
  */
 package blackdoor.util;
 
 import java.io.Serializable;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.util.Arrays;
 
 /**
  * @author nfischer3
  * Secure Hash Encryption. SHA256 in CTR mode implemented with methods similar to the standard Crypto.java library.
  * uses SHE256 v1.1 (H(key || incremented IV) instead of H(key XOR incremented IV))
  */
 public class SHE {
 	
 	public static final int BLOCKSIZE = 32;
 	private int blockNo;
 	private byte[] IV;
 	private byte[] key;
 	private boolean cfg;
 	private byte[] buffer = new byte[BLOCKSIZE];
 	private int bufferIndex; //index at which to place next byte in buffer
 	private MessageDigest mD;
 	
 	//parameters for different algo and block sizes will go here
 	/**
 	 * Creates a Cipher object.
 	 */
 	public SHE(){
 		blockNo = 0;
 		cfg = false;
 		try {
 			mD = MessageDigest.getInstance("SHA-256");
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Initializes the cipher with key, creates a random IV to use with the cipher.
 	 * @param key A 256 bit key to encrypt with.
 	 * @return A 256 bit IV that has been created for this cipher to use.
 	 */
 	public byte[] init(byte[] key){
 		byte[] iv = new byte[BLOCKSIZE];
 		new SecureRandom().nextBytes(iv);
 		init(iv, key);
 		return iv;
 	}
 	
 	/**
 	 * Initializes the cipher with key and IV
 	 * @param IV A 256 bit initialization vector to use for the cipher.
 	 * @param key A 256 bit key to encrypt with.
 	 */
 	public void init(byte[] IV, byte[] key){
 		if(IV.length != BLOCKSIZE || key.length != BLOCKSIZE)
 			throw new RuntimeException("key and IV need to be same as block size (" + BLOCKSIZE + ")."); //TODO subclass exception
 		this.key = key;
 		this.IV = IV;
 		cfg = true;
 		blockNo = 0;
 		buffer = new byte[BLOCKSIZE];
 	}
 	
 	private byte[] cryptBlock(){
 		byte[] iv = Arrays.copyOf(IV, IV.length);// + BLOCKSIZE);
 		//System.arraycopy(IV, 0, iv, 0, BLOCKSIZE);
 		iv[blockNo % BLOCKSIZE] += blockNo + 1;
 		//iv = Misc.cleanXOR(iv, key); //for some reason this line runs much faster than the following two lines
 		//Misc.XORintoA(iv, key);
 		iv = Arrays.copyOf(iv, BLOCKSIZE + iv.length);
 		//Misc.arraycopy(key, 0, iv, BLOCKSIZE, BLOCKSIZE);
 		System.arraycopy(key, 0, iv, BLOCKSIZE, BLOCKSIZE);
 		return Misc.cleanXOR(buffer, mD.digest(iv));
 	}
 	
 //	public byte[] updateWithInterrupts(byte[] input){
 //		if(!cfg){
 //			throw new RuntimeException("Cipher not configured.");
 //		}
 //		int numBlocks = (int) Math.floor((input.length + bufferIndex)/BLOCKSIZE);
 //		byte[] out = new byte[numBlocks*BLOCKSIZE];
 //		
 //		for(int i=0; i < input.length; i++){
 //			try{
 //				buffer[bufferIndex++] = input[i];
 //			}catch(IndexOutOfBoundsException e){
 //				bufferIndex = 0;
 //				i--;
 //				//System.out.println(Misc.bytesToHex(buffer));
 //				System.arraycopy(cryptBlock(), 0, out, blockNo*BLOCKSIZE, BLOCKSIZE);
 //				blockNo++;
 //				buffer = new byte[BLOCKSIZE];
 //			}
 //		}
 //		if(bufferIndex == 32){
 //			bufferIndex = 0;
 //			System.arraycopy(cryptBlock(), 0, out, blockNo*BLOCKSIZE, BLOCKSIZE);
 //			buffer = new byte[BLOCKSIZE];
 //		}
 //		//System.out.println(bufferIndex);
 //		//System.out.println(Misc.bytesToHex(out));
 //		return out;
 //	}
 	
 	/**
 	 * Continues a multiple-part encryption or decryption operation (depending on how this cipher was initialized), processing another data part.
 	 * The bytes in the input buffer are processed, and the result is stored in a new buffer.
 	 *
 	 * If input has a length of zero, this method returns null.
 	 * @param input
 	 * @return
 	 */
 	public byte[] update(byte[] input){
 		if(!cfg){
 			throw new RuntimeException("Cipher not configured.");//TODO
 		}
 		if(input.length == 0)
			return null;
 		if(bufferIndex != 0){
 			byte[] in2 = Arrays.copyOf(buffer, input.length + bufferIndex);//new byte[input.length + bufferIndex];
 			//System.out.println(Misc.bytesToHex(in2));
 			//System.arraycopy(buffer, 0, in2, 0, bufferIndex);
 			System.arraycopy(input, 0, in2, bufferIndex, input.length);
 			input = in2;
 		}
 		
 		int numBlocks = (int) Math.floor(input.length/BLOCKSIZE);
 		//System.out.println(numBlocks);
 		byte[] out = new byte[BLOCKSIZE * numBlocks];
 		for(int i = 0; i < numBlocks; i++){
 			//System.out.println("i:"+i+" block:" + blockNo);
 			System.arraycopy(input, BLOCKSIZE*i, buffer, 0, BLOCKSIZE);
 			System.arraycopy(cryptBlock(), 0, out, i * BLOCKSIZE, BLOCKSIZE);//TODO do encryption on buffer
 			blockNo++;
 		}
 		buffer = new byte[BLOCKSIZE];
 		if(input.length % BLOCKSIZE == 0){
 			
 			bufferIndex = 0;
 		}else{
 			//buffer = new byte[BLOCKSIZE];
 			System.arraycopy(input, numBlocks*BLOCKSIZE, buffer, 0, input.length - numBlocks*BLOCKSIZE);
 			bufferIndex = input.length - numBlocks*BLOCKSIZE;
 		}
 		//System.out.println(Misc.bytesToHex(out));
 		return out;
 	}
 //	public byte[] doFinalWithInterrupts(byte[] input){
 //		byte[] main = updateWithInterrupts(input);
 //		byte[] out;
 //		//if buffer isn't empty add a padding indicator to the end of data
 //		if(bufferIndex != 0){
 //			
 //			buffer[bufferIndex] = 0x69;
 //			bufferIndex++;
 //			//System.out.println(Misc.bytesToHex(buffer));
 //			buffer = cryptBlock();
 //			//add buffer to end of main
 //			out = new byte[main.length + buffer.length];
 //			System.arraycopy(main, 0, out, 0, main.length);
 //			System.arraycopy(buffer, 0, out, main.length, buffer.length);
 //		}else{
 //			//remove padding
 //			int endIndex = main.length-1 ;
 //			while(main[endIndex] == 0 || main[endIndex] == 0x69){
 //				endIndex --;
 //				if(main[endIndex] == 0x69){
 //					endIndex--;
 //					break;
 //				}
 //			}
 //			//System.out.println("endindex " + endIndex);
 //			out = new byte[endIndex + 1];
 //			System.arraycopy(main, 0, out, 0, endIndex+1);
 //		}
 //				
 //		blockNo = 0;
 //		IV = null;
 //		key = null;
 //		cfg = false;
 //		bufferIndex = 0;
 //		
 //		return out;
 //	}
 	/**
 	 * Encrypts or decrypts data in a single-part operation, or finishes a multiple-part operation.
 	 * The bytes in the input buffer, and any input bytes that may have been buffered during a previous update operation, are processed, with padding (if requested) being applied. 
 	 *
 	 * Upon finishing, this method resets this cipher object to the state it was in before initialized via a call to init. That is, the object is reset and needs to be re-initialized before it is available to encrypt or decrypt more data.
 	 * @param input the input buffer
 	 * @return the new buffer with the result
 	 */
 	public byte[] doFinal(){
 		return doFinal(new byte[]{});
 	}
 	
 	/**
 	 * Encrypts or decrypts data in a single-part operation, or finishes a multiple-part operation.
 	 * The bytes in the input buffer, and any input bytes that may have been buffered during a previous update operation, are processed, with padding (if requested) being applied. 
 	 *
 	 * Upon finishing, this method resets this cipher object to the state it was in before initialized via a call to init. That is, the object is reset and needs to be re-initialized before it is available to encrypt or decrypt more data.
 	 * @param input the input buffer
 	 * @return the new buffer with the result
 	 */
 	public byte[] doFinal(byte[] input){
 		byte[] main = update(input);
 		byte[] out;
 		//if buffer isn't empty add a padding indicator to the end of data
 		if(bufferIndex != 0){
 			
 			buffer[bufferIndex] = 0x69;
 			bufferIndex++;
 			//System.out.println(Misc.bytesToHex(buffer));
 			buffer = cryptBlock();
 			//add buffer to end of main
 			out = new byte[main.length + buffer.length];
 			System.arraycopy(main, 0, out, 0, main.length);
 			System.arraycopy(buffer, 0, out, main.length, buffer.length);
 		}else{
 			//remove padding
 			int endIndex = main.length-1 ;
 			while(main[endIndex] == 0 || main[endIndex] == 0x69){
 				endIndex --;
 				if(main[endIndex] == 0x69){
 					endIndex--;
 					break;
 				}
 			}
 			//System.out.println("endindex " + endIndex);
 			out = new byte[endIndex + 1];
 			System.arraycopy(main, 0, out, 0, endIndex+1);
 		}
 				
 		blockNo = 0;
 		IV = null;
 		key = null;
 		cfg = false;
 		bufferIndex = 0;
 		
 		return out;
 	}
 	
 	public static class EncryptionResult implements Serializable{
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -6451163680434801851L;
 		private byte[] text;
 		private byte[] iv;
 		/**
 		 * @param text
 		 * @param iv
 		 */
 		public EncryptionResult(byte[] iv, byte[] text) {
 			//super();
 			this.text = text;
 			this.iv = iv;
 		}
 		
 		public EncryptionResult(byte[] simpleSerial){
 			int ivLength = simpleSerial[0];
 			int outputLength = simpleSerial.length - ivLength -1;
 			iv = new byte[ivLength];
 			text = new byte[outputLength];
 			System.arraycopy(simpleSerial, 1, iv, 0, ivLength);
 			System.arraycopy(simpleSerial, ivLength + 1, text, 0, outputLength);
 		}
 		
 		/**
 		 * needs testing
 		 * @return the encryption result as a byte array in the form (ivLength|iv|ciphertext) 
 		 */
 		public byte[] simpleSerial(){
 			byte[] out = new byte[text.length + iv.length + 1];
 			out[0] = (byte) iv.length;
 			System.arraycopy(iv, 0, out, 1, iv.length);
 			System.arraycopy(text, 0, out, iv.length + 1, text.length);
 			return out;
 		}
 		
 		/**
 		 * @return the cipherText
 		 */
 		public byte[] getText() {
 			return text;
 		}
 		
 		/**
 		 * @return the iv
 		 */
 		public byte[] getIv() {
 			return iv;
 		}
 		
 		@Override
 		public String toString() {
 			return "EncryptionResult [iv="
 					+ Misc.bytesToHex(iv) + "[text=" + Misc.bytesToHex(text)+ "]\n" + Misc.bytesToHex(simpleSerial());
 		}
 	}
 
 }
