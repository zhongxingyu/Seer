 /* FileClient provides all the client functionality regarding the file server */
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.security.Key;
 import java.security.PublicKey;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.ArrayBlockingQueue;
 
 import javax.crypto.Cipher;
 import javax.crypto.KeyGenerator;
 import javax.crypto.spec.IvParameterSpec;
 import javax.crypto.spec.SecretKeySpec;
 
 import org.bouncycastle.util.encoders.Base64;
 
 public class FileClient extends Client implements FileClientInterface {
 
 	private String fingerprint; // Printable version of FS's public key :)
 	protected FileClientThread fcThread;
 	protected ArrayBlockingQueue<Object> inputQueue;
 	protected Ticket myTicket;
 	
 	public FileClient(String inputServer, int inputPort, ClientController _cc) {
 		super(inputServer, inputPort, _cc);
 		inputQueue = new ArrayBlockingQueue<Object>(1);
 	}
 	
 	public boolean connect() {
 		if (!super.connect())
 			return false;
 		
 		return getPublicKey();
 	}
 	
 	public String getFingerprint() {
 		return fingerprint;
 	}
 
 
 	private int beginSession(ArrayList<Object> list) {
 		try
 		{
 			//Envelope message = null, response = null;
 			SecureEnvelope secureMessage = null;
 			SecureEnvelope secureResponse = null;
 			
 			// Create the secure message
 			secureMessage = new SecureEnvelope("SESSIONINIT");
 			
 			// Set the payload using the encrypted ArrayList
 			secureMessage.setPayload(encryptPayload(listToByteArray(list), false, null));
 			
 			// Write the envelope to the socket
 			output.writeObject(secureMessage);
 		
 			// Get the response from the server
 			secureResponse = (SecureEnvelope)input.readObject();
 			
 			// If the message is empty or does not equal FAIL, then it is a SecureEnvelope with a nonce-1 and sequenceNumber
 			if ((secureResponse.getMessage() == null) || (!secureResponse.getMessage().equals("FAIL"))) {
 				ArrayList<Object> tempList = getDecryptedPayload(secureResponse);
 				// Successful response
 				if (((String)(tempList.get(0))).equals("OK")) {
 					// If there is a return nonce in the Envelope, return it
 					if (tempList.size() == 4) {
 						int returnNonce = (Integer) tempList.get(2);
 						// Grab the sequenceNumber from the message as well
 						sequenceNumber = (Integer)tempList.get(1);
 						// Get the Ticket from the message
 						myTicket = (Ticket)tempList.get(3);
 						return returnNonce;
 					}
 				}
 			}
 			
 			return -1;
 		}
 		catch(Exception e)
 		{
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 			return -1;
 		}
 	}
 	
 	private boolean getPublicKey() {
 		Envelope message = null;
 		try {
 			message = (Envelope)input.readObject();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		if ((message != null) && message.getMessage().equals("KEYANNOUNCE")) {
 			publicKey = (PublicKey)message.getObjContents().get(0);
 			if(publicKey == null) {
 				System.out.println("Problem getting the public key.");
 			}
 			else {
 				fingerprint = new String(Base64.encode(publicKey.getEncoded()));
 				System.out.println("Got the public key: " + fingerprint);
 				
 			}
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	public boolean setupChannel() {
 		
 		// Create a secure random number generator
 		SecureRandom rand = new SecureRandom();
 		
 		// Get random integer nonce
 		int nonce = rand.nextInt();
 		
 		// Generate an AES128 key
 		System.out.println("Generating an AES 128 key...");
 		
 		KeyGenerator AESkeygen = null;
 		Key AES128key = null;
 		
 		try {
 			AESkeygen = KeyGenerator.getInstance("AES", "BC");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		// Initialize with a key size of 128
 		AESkeygen.init(128);
 		
 		// Actual key generation
 		AES128key = AESkeygen.generateKey();
 		sessionKey = AES128key;
 		
 		// Create the payload ArrayList with the key and the nonce
 		ArrayList<Object> payloadList = new ArrayList<Object>();
 		payloadList.add(AES128key);
 		payloadList.add(nonce);
 		
 		// Initialize the secure session
 		int nonceReturn = beginSession(payloadList);
 		
 		// If the group server returns the nonce - 1, then we know it is actually the group server.
 		// We also know to begin using the session key
 		if (nonceReturn == (nonce - 1)) {
 			System.out.println("Successfully created a secure session!");
 			// Create the listening thread
 			fcThread = new FileClientThread(output, input, this);
 			// Run the thread
 			fcThread.start();
 			return true;
 		}
 		else {
 			sessionKey = null;
 			
 			// TODO: Might have to modify this later to have an encrypted disconnect
 			secureDisconnect();
 			//
 			
 			return false;
 		}
 		
 	}
 
 	public boolean delete(String filename, UserToken token) {
 		String remotePath;
 		SecureEnvelope secureMessage = null;
 		
 		if (filename.charAt(0)=='/') {
 			remotePath = filename.substring(1);
 		}
 		else {
 			remotePath = filename;
 		}
 		ArrayList<Object> list = new ArrayList<Object>();
 		list.add(remotePath);
 		list.add(token);
 		list.add(myTicket);
 		secureMessage = makeSecureEnvelope("DELETEF", list);
 	    
 	    try {
 			output.writeObject(secureMessage);
 			
 			//secureResponse = (SecureEnvelope)input.readObject();
 			
 			ArrayList<Object> contents = (ArrayList<Object>)inputQueue.take();
 			String msg = (String)contents.get(0);
 		    
 			if (msg.equals("OK")) {
 				System.out.printf("File %s deleted successfully\n", filename);				
 			}
 			else {
 				System.out.printf("Error deleting file %s (%s)\n", filename, msg);
 				return false;
 			}			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	    	
 		return true;
 	}
 
 	public boolean download(String sourceFile, String destFile, UserToken token, SecretKeySpec keySpec, byte[] iv) {
 		SecureEnvelope secureMessage = null;
 		
 		if (sourceFile.charAt(0) == '/') {
 			sourceFile = sourceFile.substring(1);
 		}
 
 		
 		
 		File file = new File(destFile);
 		try {
 			IvParameterSpec ivSpec = new IvParameterSpec(iv);
 			Cipher inCipher = Cipher.getInstance("AES", "BC");
 			inCipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
 			if (!file.exists()) {
 				file.createNewFile();
 				FileOutputStream fos = new FileOutputStream(file);
 
 				ArrayList<Object> list = new ArrayList<Object>();
 				list.add(sourceFile);
 				list.add(token);
 				list.add(myTicket);
 				secureMessage = makeSecureEnvelope("DOWNLOADF", list);
 				
 				output.writeObject(secureMessage);
 
 				ArrayList<Object> contents = (ArrayList<Object>)inputQueue.take();
 				String msg = (String)contents.get(0);
 
 				while (msg.equals("CHUNK")) {
 					byte[] plainText;
					byte[] cipherText = (byte[]) contents.get(2);
					int n = (Integer) contents.get(3);
					if(n == 4096){
						plainText = inCipher.update(cipherText, 0, n);
					}
					else{
						plainText = inCipher.update(cipherText, 0, n);
					}
					//System.out.println(Arrays.toString((byte[])contents.get(2)));
					//plainText = inCipher.doFinal((byte[]) contents.get(2), 0, (Integer) contents.get(3));
 				
 					fos.write(plainText, 0, plainText.length);
 					System.out.printf(".");
 					secureMessage = makeSecureEnvelope("DOWNLOADF"); // Success
 					output.writeObject(secureMessage);
 					contents = (ArrayList<Object>)inputQueue.take();
 					msg = (String)contents.get(0);
 				}
 				fos.close();
 
 				if (msg.equals("EOF")) {
 					fos.close();
 					System.out.printf("\nTransfer successful file %s\n", sourceFile);
 					secureMessage = makeSecureEnvelope("OK"); // Success
 					output.writeObject(secureMessage);
 				} else {
 					System.out.printf("Error reading file %s (%s)\n", sourceFile, msg);
 					file.delete();
 					return false;
 				}
 			}
 
 			else {
 				System.out.printf("Error couldn't create file %s\n", destFile);
 				return false;
 			}
 
 		} catch (IOException e) {
 			System.out.printf("Error couldn't create file %s\n", destFile);
 			return false;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return true;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<String> listFiles(UserToken token) {
 		SecureEnvelope secureMessage = null;
 		try {
 			ArrayList<Object> list = new ArrayList<Object>();
 			list.add(token);
 			list.add(myTicket);
 			secureMessage = makeSecureEnvelope("LFILES", list);
 
 			output.writeObject(secureMessage);
 
 			ArrayList<Object> contents = (ArrayList<Object>) inputQueue.take();
 			String msg = (String) contents.get(0);
 
 			// If server indicates success, return the member list
 			if (msg.equals("OK")) {
 				// This cast creates compiler warnings, sorry.
 				return (List<String>) contents.get(2);
 			}
 
 			return null;
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public boolean upload(String sourceFile, String destFile, String group, UserToken token, SecretKeySpec keySpec, byte[] seed, int keyId) {
 		SecureEnvelope secureMessage = null;
 		
 		if (destFile.charAt(0) != '/') {
 			destFile = "/" + destFile;
 		}
 
 		try {
 			ArrayList<Object> list = new ArrayList<Object>();
 			list.add(destFile);
 			list.add(group);
 			list.add(token);
 			list.add(myTicket);
 			secureMessage = makeSecureEnvelope("UPLOADF", list);
 
 		    // Pre encryption stuff.
 			byte[] iv = new byte[16];
 		    SecureRandom rng = SecureRandom.getInstance("SHA1PRNG");
 		    rng.nextBytes(iv);
 			IvParameterSpec ivSpec = new IvParameterSpec(iv);
 			Cipher inCipher = Cipher.getInstance("AES", "BC");
 			inCipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
 			
 			output.writeObject(secureMessage);
 
 			FileInputStream fis = new FileInputStream(sourceFile);
 			ArrayList<Object> contents = (ArrayList<Object>) inputQueue.take();
 			String msg = (String) contents.get(0);
 
 			// If server indicates success, return the member list
 			if (msg.equals("READY")) {
 				System.out.printf("Meta data upload successful\n");
 			} else {
 				System.out.printf("Upload failed: %s\n", msg);
 				return false;
 			}
 
 			do {
 				byte[] buf = new byte[4096];
 				byte[] cipherText;
 				if (!msg.equals("READY")) {
 					System.out.printf("Server error: %s\n", msg);
 					return false;
 				}
 
 				int n = fis.read(buf); // Can throw an IOException
				if (n > 0 && n == 4096) {
 					System.out.printf(".");
					cipherText = inCipher.update(buf, 0, n);
 				} else if (n < 0) {
 					System.out.println("Read error");
 					return false;
 				}
				else{
					cipherText = inCipher.doFinal(buf, 0, n);
				}
 	
 
 				// Encrypt the file.
 				// TODO : Make sure this works in degenerate cases.
 				System.out.println(Arrays.toString(cipherText));
 
 				ArrayList<Object> tempList = new ArrayList<Object>();
 				tempList.add(cipherText);
 				tempList.add(new Integer(cipherText.length));
 				secureMessage = makeSecureEnvelope("CHUNK", tempList);
 
 				output.writeObject(secureMessage);
 
 				contents = (ArrayList<Object>) inputQueue.take();
 				msg = (String) contents.get(0);
 
 			} while (fis.available() > 0);
 
 			// If server indicates success, return the member list
 			if (msg.equals("READY")) {
 
 				ArrayList<Object> listSend = new ArrayList<Object>();
 				listSend.add(iv);
 				listSend.add(seed);
 				listSend.add(keyId);
 				secureMessage = makeSecureEnvelope("EOF", listSend);
 
 				output.writeObject(secureMessage);
 
 				contents = (ArrayList<Object>) inputQueue.take();
 				msg = (String) contents.get(0);
 
 				if (msg.equals("OK")) {
 					System.out.printf("\nFile data upload successful\n");
 				} else {
 					System.out.printf("\nUpload failed: %s\n", msg);
 					return false;
 				}
 			} else {
 				System.out.printf("Upload failed: %s\n", msg);
 				return false;
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 		
 		return true;
 	}
 	
 	List<Object> getFileInfo(String remotePath, UserToken token){
 		SecureEnvelope secureMessage = null;
 		if (remotePath.charAt(0) == '/') {
 			remotePath = remotePath.substring(1);
 		}
 
 
 		ArrayList<Object> list = new ArrayList<Object>();
 		list.add(remotePath);
 		list.add(token);
 		list.add(myTicket);
 		secureMessage = makeSecureEnvelope("FILEINFO", list);
 
 		try {
 			output.writeObject(secureMessage);
 			ArrayList<Object> contents;
 			contents = (ArrayList<Object>) inputQueue.take();
 			String msg = (String) contents.get(0);
 
 			// If server indicates success, return the member list
 			if (msg.equals("OK")) {
 				System.out.printf("Meta data retrieved successful\n");
 				return contents;
 			} else {
 				System.out.printf("info not retrieved failed: %s\n", msg);
 				return null;
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 }
 
