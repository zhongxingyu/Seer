 package main.java.CryptoEngine;
 
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import main.java.RSAEngine.Crypter;
 
 public class Signature {
 	
 	MessageDigest SHA256;
 	Crypter a;
 	
 	public Signature() throws NoSuchAlgorithmException{
 		//initialize my objects
 		SHA256 = MessageDigest.getInstance("SHA-256");
 		a = new Crypter();
 	}
 	
 	public String generateSignature(String username, String message, BigInteger e, BigInteger n) throws JSONException{
 		byte[] h = SHA256.digest(message.getBytes());				//get me a hash of message
		BigInteger hash = new BigInteger(h);
		BigInteger cypherhash = a.RSAEncryptPrimitive(hash, e, n);	//Encrypt my hash
 		JSONObject signature = new JSONObject();					//Initialize the JSON object and shove everything there
 		signature.put("username", username);
 		signature.put("message", message);
		signature.put("signature", cypherhash.toString(16));
 		return signature.toString();								//return the string
 	}
 	
 	public boolean verifySignature(String signature, BigInteger d, BigInteger n) throws JSONException{
 		JSONObject sig = new JSONObject(signature);					//Build the JSON
 		String message = sig.getString("message");					//Pull out the message
 		byte[] h = SHA256.digest(message.getBytes());				//hash it
 		BigInteger hash = new BigInteger(h);
		String cypherhash = (String) sig.getString("signature");	//get out the cyphertext
		BigInteger c = new BigInteger(cypherhash, 16);
		BigInteger plainsig = a.RSADecryptPrimitive(c, d, n);		//decrypt it
		if(hash.compareTo(BigInteger.ZERO) < 0){
			return plainsig.subtract(n).equals(hash);
		}
 		return plainsig.equals(hash);								//check validity
 	}
 }
