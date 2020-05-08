 /***********************************************************************************
  * ELEC5616
  * Computer and Network Security, The University of Sydney
  *
  * PROJECT:         StealthNet
  * FILENAME:        StealthNetComms.java
  * AUTHORS:         Stephen Gould, Matt Barrie, Ryan Junee
  * DESCRIPTION:     Implementation of StealthNet Communications for ELEC5616
  *                  programming assignment.
  *                  This code has been written for the purposes of teaching
  *                  cryptography and computer security. It is to be used as
  *                  a demonstration only. No attempt has been made to optimise
  *                  the source code.
  * VERSION:         1.0
  * IMPLEMENTS:      initiateSession();
  *                  acceptSession();
  *                  terminateSession();
  *                  sendPacket();
  *                  recvPacket();
  *                  recvReady();
  *
  * REVISION HISTORY:
  *
  **********************************************************************************/
 
 /* Import Libraries **********************************************************/
 
 import java.net.*;
 
 import javax.crypto.*;
 import javax.crypto.spec.*;
 import java.security.*;
 import java.security.spec.*;
 import java.util.Date;
 
 import javax.crypto.interfaces.*;
 
 import java.io.*;
 import java.math.*;
 
 /* StealthNetComms class **************************************************** */
 
 public class StealthNetComms {
 	public static final String SERVERNAME = "localhost";
 	public static final int SERVERPORT = 5616;
 	public static final String STRSEP = ",";
 
 	private Socket commsSocket; // communications socket
 	private PrintWriter dataOut; // output data stream
 	private BufferedReader dataIn; // input data stream
 
 	private SecretKey sKey;
 	private TripleDESEncrypter encrypter;
 	private SecureRandom random;
 	private long nonce;
 	private boolean nonceSet;
 	private long othernonce;
 	private long lastdate;
 
 	public StealthNetComms() {
 		commsSocket = null;
 		dataIn = null;
 		dataOut = null;
 		//byte[] bytea = {0x0, 0x0};
 		nonce = 0;
 		othernonce = 0;
 		nonceSet = false;
 	}
 
 	protected void finalize() throws IOException {
 		if (dataOut != null)
 			dataOut.close();
 		if (dataIn != null)
 			dataIn.close();
 		if (commsSocket != null)
 			commsSocket.close();
 	}
 
 	public boolean initiateSession(Socket socket) {
 		try {
 			commsSocket = socket;
 			dataOut = new PrintWriter(commsSocket.getOutputStream(), true);
 			dataIn = new BufferedReader(new InputStreamReader(commsSocket
 					.getInputStream()));
 			this.sendKeyInfo();
 		} catch (Exception e) {
 			System.err.println("Connection terminated.");
 			System.exit(1);
 		}
 		return true;
 	}
 
 	public boolean acceptSession(Socket socket) {
 		try {
 			commsSocket = socket;
 			PublicKey publicKey;
 			dataOut = new PrintWriter(commsSocket.getOutputStream(), true);
 			dataIn = new BufferedReader(new InputStreamReader(commsSocket
 					.getInputStream()));
 			// Receives key info and extracts useful information from it
 			byte[] publicKeyBytes = javax.xml.bind.DatatypeConverter
 					.parseBase64Binary(dataIn.readLine());
 			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(
 					publicKeyBytes);
 			KeyFactory keyFact = KeyFactory.getInstance("DH");
 			publicKey = keyFact.generatePublic(x509KeySpec);
 
 			BigInteger p = ((DHKey) publicKey).getParams().getP();
 			BigInteger g = ((DHKey) publicKey).getParams().getG();
 			int l = ((DHKey) publicKey).getParams().getL();
 
 			// Use the values to generate a key pair
 			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
 			DHParameterSpec dhSpec = new DHParameterSpec(p, g, l);
 			keyGen.initialize(dhSpec);
 			KeyPair keypair = keyGen.generateKeyPair();
 
 			// Get the generated public and private keys
 			PrivateKey privatekey = keypair.getPrivate();
 			PublicKey publickey = keypair.getPublic();
 
 			// Send the public key bytes to the other party...
 			byte[] publickeyBytes = publickey.getEncoded();
 			dataOut.println(javax.xml.bind.DatatypeConverter
 					.printBase64Binary(publickeyBytes));
 
 			// Prepare to generate the secret key with the private key and
 			// public key of the other party
 			KeyAgreement ka = KeyAgreement.getInstance("DH");
 			ka.init(privatekey);
 			ka.doPhase(publicKey, true);
 
 			// Specify the type of key to generate;
 			String algorithm = "DESede";
 
 			// Generate the secret key
 			SecretKey secretKey = ka.generateSecret(algorithm);
 			sKey = secretKey;
 			encrypter = new TripleDESEncrypter(sKey);
 
 			SecureRandom r = new SecureRandom();
 			byte[] seed = new byte[256];
 			r.nextBytes(seed);
 			dataOut.println(encrypter.encrypt(javax.xml.bind.DatatypeConverter
 					.printBase64Binary(seed)));
 			random = new SecureRandom(seed);
 		} catch (InvalidKeySpecException i) {
 			System.err.println(i);
 			System.exit(1);
 		} catch (NoSuchAlgorithmException n) {
 			System.err.println(n);
 			System.exit(1);
 		} catch (InvalidKeyException i) {
 			System.err.println(i);
 			System.exit(1);
 		} catch (Exception e) {
 			System.err.println("Connection terminated.");
 			System.exit(1);
 		}
 		return true;
 	}
 	
 /*	public SecretKey getKey(String user, File file){
 		
 	}*/
 
 	public boolean terminateSession() {
 		try {
 			if (commsSocket == null)
 				return false;
 			dataIn.close();
 			dataOut.close();
 			commsSocket.close();
 			commsSocket = null;
 		} catch (Exception e) {
 			return false;
 		}
 
 		return true;
 	}
 
 	public boolean sendPacket(byte command) {
 		return sendPacket(command, new byte[0]);
 	}
 
 	public boolean sendPacket(byte command, String data) {
 		System.out.println("String data: " + data);
 		return sendPacket(command, data.getBytes());
 	}
 
 	public boolean sendPacket(byte command, byte[] data) {
 		return sendPacket(command, data, data.length);
 	}
 
 	public boolean sendPacket(byte command, byte[] data, int size) {
 		StealthNetPacket pckt = new StealthNetPacket();
 		pckt.command = command;
 		pckt.data = new byte[size];
 		System.arraycopy(data, 0, pckt.data, 0, size);
 		return sendPacket(pckt);
 	}
 
 	public boolean sendPacket(StealthNetPacket pckt) {
 		if (dataOut == null)
 			return false;
		//byte randhead[] = new byte[8];
		//Random header for entropy
 		SecureRandom sr = new SecureRandom();
		String str = Long.toHexString(sr.nextLong()).substring(0, 8); //Random header
		str = str.concat(STRSEP);
 		str = str.concat(pckt.toString());
 		str = str.concat(STRSEP);
 		Date date = new Date();
 		str = str.concat(Long.toHexString(date.getTime()));
 		str = str.concat(STRSEP);
 		// Generates the MAC for the message
 		try {
 			if (nonce == 0) {
 				nonce = sr.nextLong();
 			}
 			nonce++;
 			nonce = nonce % Long.MAX_VALUE;
 	        MessageDigest md = MessageDigest.getInstance("SHA-1");
 			md.update((byte) nonce);
 	        byte[] nonceDigest = md.digest();
 			String nonceB64 = javax.xml.bind.DatatypeConverter
 					.printBase64Binary(nonceDigest);
 	        Mac mac = Mac.getInstance("HmacMD5");
 			mac.init(sKey);
 			byte[] utf8 = pckt.toString().getBytes("UTF8");
 			byte[] digest = mac.doFinal(utf8);
 			String digestB64 = javax.xml.bind.DatatypeConverter
 					.printBase64Binary(digest);
 			str = str.concat(Long.toHexString(nonce));
 			str = str.concat(STRSEP);
 			str = str.concat(nonceB64);
 			str = str.concat(STRSEP);
 			str = str.concat(digestB64);
 			System.out.println("snd str: " + str);
 		} catch (NoSuchAlgorithmException e) {
 		} catch (InvalidKeyException e) {
 		} catch (UnsupportedEncodingException e) {
 		}
 		
 		dataOut.println(encrypter.encrypt(str, random));
 		return true;
 	}
 
 	public StealthNetPacket recvPacket() throws IOException {
		
		//int macOffset = 24;
		//int nonceOffset = 16; //40 byte hex representation. Inefficient, but don't have to deal with NULL etc
		//int baseOffset = 8; //8 bytes random header for entropy
 		StealthNetPacket pckt = null;
 		String str = dataIn.readLine();
 		String decrypted = encrypter.decrypt(str, random);
		decrypted = decrypted.substring(8); //Not a hack; no magic numbers here!
		if (decrypted.charAt(0) == ',') {
			decrypted = decrypted.substring(1);
		}
 		String[] sarr = decrypted.split(STRSEP);
 		String message = sarr[0];
 		long messageDate = new BigInteger(sarr[1], 16).longValue();
 		String messageNonce = sarr[2];
 		String messageNonceB64 = sarr[3];
 		String MAC = sarr[4];
 		System.out.println("rcv str: " + decrypted);
 		
 		try {
 			// Checks the MAC against the message
 			Mac mac = Mac.getInstance("HmacMD5");
 			mac.init(sKey);
 			byte[] utf8 = message.getBytes("UTF8");
 			byte[] digest = mac.doFinal(utf8);
 			String digestB64 = javax.xml.bind.DatatypeConverter
 					.printBase64Binary(digest);
 			if (digestB64.equals(MAC)) {
 			} else {
 				System.err.println("Incoming message altered (invalid hash), communication line unsafe.");
 				System.err.println("hash " + digestB64 + " expected " + MAC);
 				System.exit(1);
 			}
 	        long noncein = new BigInteger(messageNonce, 16).longValue();
 			if (othernonce+1 == noncein) {
 	        	othernonce++;
 	        } else if (othernonce == 0) {
 	        	nonceSet = true;
 	        	othernonce = new BigInteger(messageNonce, 16).longValue();
 	        	System.out.println("setting othernonce to " + Long.toHexString(othernonce));
 		        //nonce16 = String.format("%x", new BigInteger(1, nonce));
 	        	//System.out.println("New nonce 16 " + nonce16);
 	        } else {
 				System.err.println("Incoming message altered (invalid nonce), communication line unsafe.");
 				System.err.println("incoming " + messageNonce + ".");
 				System.err.println("other +1 " + Long.toHexString(othernonce+1) +". current " + Long.toHexString(othernonce));
 				//System.exit(1);
 	        }
 			MessageDigest md = MessageDigest.getInstance("SHA-1");
 	        md.update((byte) (othernonce)); //Not +1, we just set it to the current.
 	        byte[] nonceDigest = md.digest();
 			String nonceB64 = javax.xml.bind.DatatypeConverter
 					.printBase64Binary(nonceDigest);
 			if (messageNonceB64.equals(nonceB64)) {
 			} else {
 				System.err.println("Incoming message altered (invalid nonce hash), communication line unsafe.");
 				System.err.println("incoming " + messageNonceB64 + ".");
 				System.err.println("expected " + nonceB64 +". othernonce " + Long.toHexString(othernonce));
 				//System.exit(1);
 			}
 			if (lastdate == 0 || lastdate <= messageDate) { //Hack, needs nano
 				lastdate = messageDate;
 			} else {
 				System.err.println("Timestamp invalid: message from " + messageDate + " before " + lastdate);
 			}
 		} catch (NoSuchAlgorithmException e) {
 		} catch (InvalidKeyException e) {
 		} catch (UnsupportedEncodingException e) {
 		}
 		pckt = new StealthNetPacket(message);
 		return pckt;
 	}
 
 	public boolean recvReady() throws IOException {
 		/*
 		 * System.out.println("Connected: " + commsSocket.isConnected());
 		 * System.out.println("Closed: " + commsSocket.isClosed());
 		 * System.out.println("InClosed: " + commsSocket.isInputShutdown());
 		 * System.out.println("OutClosed: " + commsSocket.isOutputShutdown());
 		 */
 		return dataIn.ready();
 	}
 
 	public static String genDhParams() {
 		try {
 			// Create the parameter generator for a 1024-bit DH key pair
 			AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator
 					.getInstance("DH");
 			paramGen.init(1024);
 
 			// Generate the parameters
 			AlgorithmParameters params = paramGen.generateParameters();
 			DHParameterSpec dhSpec = (DHParameterSpec) params
 					.getParameterSpec(DHParameterSpec.class);
 
 			// Return the three values in a string
 			return "" + dhSpec.getP() + "," + dhSpec.getG() + ","
 					+ dhSpec.getL();
 		} catch (NoSuchAlgorithmException e) {
 		} catch (InvalidParameterSpecException e) {
 		}
 		return null;
 	}
 
 	public void sendKeyInfo() {
 		String[] values = genDhParams().split(",");
 		BigInteger p = new BigInteger(values[0]);
 		BigInteger g = new BigInteger(values[1]);
 		int l = Integer.parseInt(values[2]);
 
 		try {
 			// Use the values to generate a key pair
 			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
 			DHParameterSpec dhSpec = new DHParameterSpec(p, g, l);
 			keyGen.initialize(dhSpec);
 			KeyPair keypair = keyGen.generateKeyPair();
 
 			// Get the generated public and private keys
 			PrivateKey privateKey = keypair.getPrivate();
 			PublicKey publicKey = keypair.getPublic();
 
 			// Send the public key bytes to the other party
 			byte[] publicKeyBytes = publicKey.getEncoded();
 			dataOut.println(javax.xml.bind.DatatypeConverter
 					.printBase64Binary(publicKeyBytes));
 
 			// Get the publicKey from the other party
 			byte[] publickeyBytes = javax.xml.bind.DatatypeConverter
 					.parseBase64Binary(dataIn.readLine());
 			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(
 					publickeyBytes);
 			KeyFactory keyFact = KeyFactory.getInstance("DH");
 			publicKey = keyFact.generatePublic(x509KeySpec);
 
 			// Prepare to generate the secret key with the private key and
 			// public key of the other party
 			KeyAgreement ka = KeyAgreement.getInstance("DH");
 			ka.init(privateKey);
 			ka.doPhase(publicKey, true);
 
 			// Specify the type of key to generate;
 			String algorithm = "DESede";
 
 			// Generate the secret key
 			SecretKey secretKey = ka.generateSecret(algorithm);
 			sKey = secretKey;
 			encrypter = new TripleDESEncrypter(sKey);
 			
 			//byte [] secretBytes = ka.generateSecret();
             MessageDigest md = MessageDigest.getInstance("SHA-1");
             md.reset();
             md.update(secretKey.getEncoded());
             
             //nonce = md.digest();
             //nonce = 
             
 
 			String randomSeed = dataIn.readLine();
 			byte[] seed = javax.xml.bind.DatatypeConverter
 					.parseBase64Binary(encrypter.decrypt(randomSeed));
 			random = new SecureRandom(seed);
 			nonce = random.nextLong();
 			nonceSet = true;
 			return;
 		} catch (InvalidKeySpecException e) {
 		} catch (InvalidKeyException e) {
 		} catch (java.security.InvalidAlgorithmParameterException e) {
 		} catch (java.security.NoSuchAlgorithmException e) {
 		} catch (IOException e) {
 		}
 	}
 	
 }
 
 /*******************************************************************************
  * END OF FILE: StealthNetComms.java
  ******************************************************************************/
 
