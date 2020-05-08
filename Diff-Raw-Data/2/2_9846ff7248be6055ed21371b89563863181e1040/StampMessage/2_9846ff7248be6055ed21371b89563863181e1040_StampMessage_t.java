 package com.android.locproof.stamp;
 
 import java.io.UnsupportedEncodingException;
 import java.math.BigInteger;
 import java.nio.ByteBuffer;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.security.SignatureException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 
 /**
  * Handlers for stamp message processing
  */
 public class StampMessage {
 	/* TODO: varied length fields */
 	//public static int COMMITTEDID_LEN = 11;	// fixed length
 	//public static int TIMESTAMP_LEN = 8;	// fixed length
 	//public static int LOCATION_LEN = 23;	
 	
 	// Prover operations
 	/**
 	 * Create Preq message body
 	 * Preq = Comm(ID_P, r_P)|T|L_1
 	 * @param aStampContext current context
 	 * @param aLocLevel desired location level (0 usually)
 	 * @return Preq message
 	 */
 	public static byte[] createPreq(ProverContext aStampContext){
 		byte commID[] = aStampContext.getCommittedID();
 		byte time[] = ByteBuffer.allocate(8).putLong(ProverContext.getTime()).array();
 		/* TODO: check if aLocLevel is valid */
 		byte location[] = aStampContext.getLocation().toString().getBytes();
 		
 		ArrayList<byte[]> array = new ArrayList<byte[]>();
 		array.add(commID);
 		array.add(time);
 		array.add(location);
 		
 		return MessageUtil.compileMessages(array);
 	}
 	
 	/**
 	 * Process DB start message
 	 * @param aStampContext current context
 	 * @param payload DB start message
 	 */
 	public static void processDBStart(ProverContext aStampContext, byte[] payload){
 		
 	}
 	
 	/**
 	 * Create DB ready message body
 	 * @return DB ready message
 	 */
 	public static byte[] createCeCk(ProverContext aStampContext){
 		
 		BigInteger e = aStampContext.getE();
 		BigInteger k = aStampContext.getK();
 		BigInteger p = aStampContext.getPubDSASelf().getParams().getP();
 		BigInteger g = aStampContext.getPubDSASelf().getParams().getG();
 		BigInteger h = aStampContext.getH();
 		BigInteger v = aStampContext.getV();
 		
 		ArrayList<byte[]> ces = CryptoUtil.getBitCommitments(g, p, h, v, e);
 		ArrayList<byte[]> cks = CryptoUtil.getBitCommitments(g, p, h, v, k);
 		
 		byte[] cesBytes = MessageUtil.createMessageFromArray(ces);
 		byte[] cksBytes = MessageUtil.createMessageFromArray(cks);
 		
 		ArrayList<byte[]> array = new ArrayList<byte[]>();
 		array.add(cesBytes);
 		array.add(cksBytes);
 
 		return MessageUtil.compileMessages(array);
 	}
 	
 	/**
 	 * Add received EP into EP list
 	 * @param aEPRecord EP record for current proof operation
 	 * @param payload received EP
 	 */
 	public static void processEP(StampEPRecord aEPRecord, byte[] payload){
 		aEPRecord.addEP(payload);
 	}
 
 	/**********************************************************************
 	// Witness operations
 	
 	/**
 	 * Save preq information in current context for later use
 	 * @param aStampContext current context
 	 * @param payload preq message body
 	 */
 	public static void processPreq(WitnessContext aStampContext, byte[] payload){
 		ArrayList<byte[]> array = MessageUtil.parseMessage(payload, 3);
 		
 		byte[] commID = array.get(0);
 		aStampContext.setRemoteCommittedID(commID);
 		
 		byte[] time = array.get(1);
 		aStampContext.setRemoteTime(time);
 		
		byte[] location = array.get(2);
 		aStampContext.setRemoteLocation(location);
 	}
 
 	/**
 	 * Create DB start message body
 	 * @return DB start message
 	 */
 	public static byte[] createDBStart(){
 		String payload = "DBSTART";
 		return payload.getBytes();
 	}
 
 	/**
 	 * Process DB Success message
 	 * @param aStampContext current context
 	 * @param payload DB Success message
 	 */
 	public static void processCeCk(WitnessContext aStampContext, byte[] payload){
 		// parse Ce and Ck
 		ArrayList<byte[]> ceck = MessageUtil.parseMessage(payload, 2);
 		ArrayList<byte[]> ces = MessageUtil.parseMessages(ceck.get(0));
 		ArrayList<byte[]> cks = MessageUtil.parseMessages(ceck.get(1));
 		
 		// obtain z
 		BigInteger z = CryptoUtil.getZ(ces, cks, aStampContext.getPubDSASelf().getParams().getP());
 		aStampContext.setRemoteZ(z);
 	}
 	
 	/**
 	 * Create EP message body
 	 * EP = r_w1|E^(K_ca)(ID_W|P|E^(K_W)(Hash(P)))
 	 * @param aStampContext current context
 	 * @return EP message body
 	 */
 	public static byte[] createEP(WitnessContext aStampContext){
 		byte proof[] = createP(aStampContext);
 		byte eproof[] = endorseP(aStampContext, proof);
 		byte randomW[] = aStampContext.getEPRandomW().toByteArray();
 		
 		ArrayList<byte[]> array = new ArrayList<byte[]>();
 		array.add(eproof);
 		array.add(randomW);
 		
 		return MessageUtil.compileMessages(array);
 	}
 	
 	/**
 	 * Endorse the proof
 	 * @param aStampContext current context
 	 * @param aProof proof
 	 * @return endorsed proof
 	 */
 	private static byte[] endorseP(WitnessContext aStampContext, byte aProof[]){
 		
 		// Sign on the proof first
 		byte[] sig = {};
 		try {
 			sig = CryptoUtil.signDSA(aStampContext.getPriDSASelf(), aProof);
 		} catch (InvalidKeyException e) {
 			e.printStackTrace();
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		} catch (SignatureException e) {
 			e.printStackTrace();
 		}
 		
 		// Include own ID in EP
 		byte[] wID = aStampContext.getPubDSASelf().getEncoded();
 		
 		ArrayList<byte[]> array = new ArrayList<byte[]>();
 		array.add(wID);
 		array.add(aProof);
 		array.add(sig);
 		
 		byte[] epContent = MessageUtil.compileMessages(array);
 		
 		// Encrypt with CA's public key
 		byte[] epBytes = {};
 		try {
 			epBytes = CryptoUtil.encryptRSA(aStampContext.getPubRSACA(), epContent);
 		} catch (InvalidKeyException e) {
 			e.printStackTrace();
 		} catch (IllegalBlockSizeException e) {
 			e.printStackTrace();
 		} catch (BadPaddingException e) {
 			e.printStackTrace();
 		} catch (NoSuchPaddingException e) {
 			e.printStackTrace();
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		}
 		
 		return epBytes;
 	}
 	
 	/**
 	 * Create a proof 
 	 * P = Comm(ID_p, r_p)|C_k|C_e|STPR
 	 * @param aStampContext current context 
 	 * @return proof
 	 */
 	private static byte[] createP(WitnessContext aStampContext){
 		byte commID[] = aStampContext.getRemoteCommittedID();
 		byte z[] = aStampContext.getRemoteZ().toByteArray();
 		
 		byte location[] = aStampContext.getRemoteLocation();
 		byte time[] = aStampContext.getRemoteTime();
 		byte stpr[] = createSTPR(aStampContext, location, time);
 		
 		ArrayList<byte[]> array = new ArrayList<byte[]>();
 		array.add(commID);
 		array.add(z);
 		array.add(stpr);
 		
 		return MessageUtil.compileMessages(array);
 	}
 	
 	/**
 	 * Create a STPR
 	 * STPR = T|Comm(L_1, r^1_W)|...|Comm(L_n, r^n_W)
 	 * @param aStampContext 
 	 * @param location location L_1
 	 * @param time Preq time T 
 	 * @return STPR
 	 */
 	private static byte[] createSTPR(WitnessContext aStampContext, byte location[], byte time[]){
 		
 		BigInteger rw = aStampContext.getEPRandomW();
 		
 		String locString = new String(location);
 		Location loc = new Location(locString);
 		short levelCount = (short) loc.getLevelCount();
 		
 		LinkedList<BigInteger> rws = new LinkedList<BigInteger>();
 		try {
 			rws = CryptoUtil.getHashChain(rw, levelCount);
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		
 		ArrayList<byte[]> locComms = new ArrayList<byte[]>();
 		for(int i = 0; i< levelCount; i++){
 			byte[] locComm = {};
 			try {
 				locComm = CryptoUtil.getCommitment(loc.getLevel(i).getBytes(), rws.get(i)).toByteArray();
 			} catch (NoSuchAlgorithmException e) {
 				e.printStackTrace();
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 			locComms.add(locComm);
 		}
 		
 		return MessageUtil.createMessageFromArray(locComms);
 	}
 	
 }
