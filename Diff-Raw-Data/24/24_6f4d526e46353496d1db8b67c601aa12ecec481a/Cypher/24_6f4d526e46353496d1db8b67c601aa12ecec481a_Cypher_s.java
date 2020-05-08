 
 public class Cypher {
 	private int padding;
 	
 	public Cypher() {
 		this.padding = 0;
 	}
 	
 	/**
 	 * Functions for encryption and decryption.
 	 */
 	byte[] encrypt(String message, double key, double initVect) {
 		byte[] messageToBytes = message.getBytes();
 		/* TODO: hash the key here. */
 		byte[] hashInitVect = Double.toHexString(initVect).getBytes();
 		final int BLOCK_SIZE = hashInitVect.length;
 		final int MSG_SIZE = messageToBytes.length;
 		
 		/* Determine the size of padding. */
 		this.padding = 0;
 		if(MSG_SIZE > BLOCK_SIZE)
 			this.padding = BLOCK_SIZE - (MSG_SIZE % BLOCK_SIZE); 
 		else if(MSG_SIZE < BLOCK_SIZE)
 			this.padding = BLOCK_SIZE - MSG_SIZE;
 		
 		byte[] encryptedMessage = new byte[MSG_SIZE + this.padding];
 		
 		/* Do the padding. */
 		for(int i = 0; i < MSG_SIZE; i++) {
 			encryptedMessage[i] = messageToBytes[i];
 		}
 		
 		/**
 		 * Remove later.
 		 */
 		System.out.println("Padded message: message length = " + MSG_SIZE + " padding length = " + padding);
 		for (int i = 0; i < encryptedMessage.length; i++) {
 			System.out.print(encryptedMessage[i] + " ");
 		}
 		
 		/**********************************************************************/
 		
 		/* Do the encryption. <david> */ 
 		byte[] keyToBytes = new byte[BLOCK_SIZE];
 		byte[] tempKey = new byte[BLOCK_SIZE];
 		for(int i = 0; i < encryptedMessage.length; i++) {
			if(i < BLOCK_SIZE)
				encryptedMessage[i] = (byte)(encryptedMessage[i]^hashInitVect[i]);
			else
				encryptedMessage[i] = (byte)(encryptedMessage[i]^keyToBytes[i%BLOCK_SIZE]);
 			
 			tempKey[i%BLOCK_SIZE] = encryptedMessage[i];
 			
 			if((i%BLOCK_SIZE) == 1) {
 				for(int j = 0; j < BLOCK_SIZE; j++) {
 					keyToBytes[j] = tempKey[j];
 				}
 			}
 		}
 		
 		/**
 		 * Remove later.
 		 */
 		System.out.println();
 		System.out.println("Encrypted message: padding length = " + this.padding);
 		for (int i = 0; i < encryptedMessage.length; i++) {
 			System.out.print(encryptedMessage[i] + " ");
 		}
 		
 		/**********************************************************************/
 		
 		return encryptedMessage;
 	}
 	
 	public String decrypt(byte[] encryptedMessage, double key, double initVect) {
 		String message = new String();
 		/* TODO: hash the key here. */
 		byte[] hashInitVect = Double.toHexString(initVect).getBytes();
 		final int BLOCK_SIZE = hashInitVect.length;
 		System.out.println("block size = " + BLOCK_SIZE);
 		final int MSG_SIZE = encryptedMessage.length;
 		System.out.println("msg size = " + MSG_SIZE);
 		
 		
 		System.out.println("padding = " + this.padding);
 		
 		/* Do the decryption. <david> */
 		byte[] keyToBytes = new byte[BLOCK_SIZE];
 		byte[] tempKey = new byte[BLOCK_SIZE];
 		for(int i = 0; i < encryptedMessage.length; i++) {
			if(i < BLOCK_SIZE)
				encryptedMessage[i] = (byte)(encryptedMessage[i]^hashInitVect[i]);
			else
				encryptedMessage[i] = (byte)(encryptedMessage[i]^keyToBytes[i%BLOCK_SIZE]);
 			
 			tempKey[i%BLOCK_SIZE] = encryptedMessage[i];
 			
 			if((i%BLOCK_SIZE) == 1) {
 				for(int j = 0; j < BLOCK_SIZE; j++) {
 					keyToBytes[j] = tempKey[j];
 				}
 			}
 		}
 		
 		byte[] unpaddedMessage = new byte[MSG_SIZE - this.padding];
 		for(int i = 0; i < MSG_SIZE - this.padding; i++) {
 			unpaddedMessage[i] = encryptedMessage[i]; 
 		}
 		message = new String(unpaddedMessage);
 		System.out.println();
 		System.out.println(message);
 		
 		
 		return message;
 	}
 }
