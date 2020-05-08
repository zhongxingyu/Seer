 package ssf;
 
 import java.io.*;
 import java.nio.ByteBuffer;
 import java.security.*;
 import java.security.interfaces.*;
 import java.security.spec.*;
 import javax.crypto.*;
 
 public class SSF {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		File prvFile = null;
 		File pubFile = null;
 		File inFile = null;
 		File outFile = null;
 		if (args.length == 4) {
 			prvFile = new File(args[0]);
 			pubFile = new File(args[1]);
 			inFile = new File(args[2]);
 			outFile = new File(args[3]);
 			ssf(prvFile, pubFile, inFile, outFile);
 		} else {
 			System.out
 					.println("Bitte Parameter in der Reihenfolge: <privater Schluessel des Senders> <oeffentlicher Schluessel des Empfaengers> <Queldatei> <Zieldatei> angeben.");
 			System.exit(1);
 		}
 	}
 	
 	private static void ssf(File prvFile, File pubFile, File inFile, File outFile) {
 		//Keys erzeugen
 		RSAPrivateKey privateKey = (RSAPrivateKey) getKey(prvFile,
 				"RSA", true);
 		RSAPublicKey publicKey = (RSAPublicKey) getKey(pubFile, "RSA", false);
 		SecretKey secretKey = createSecretAESKey();
 		
 		//Geheimen Schlssel signieren und verschlsseln
 		byte[] secretKeySign = sign(secretKey.getEncoded(), privateKey);
 		byte[] secretKeyEnc = encodeBytes(secretKey.getEncoded(), publicKey, "RSA");
 		
 		//Daten in Datei schreiben
 		DataOutputStream out = null;
 		try {
 			out = new DataOutputStream(new FileOutputStream(outFile));
 			//Lnge des verschlsselten geheimen Schlssels (integer)
 			out.writeInt(secretKeyEnc.length);
 			//Verschlsselter geheimer Schlssel (Bytefolge)
 			out.write(secretKeyEnc);
 			//Lnge der Signatur des geheimen Schlssels (integer)
 			out.writeInt(secretKeySign.length);
 			// Signatur des geheimen Schlssels (Bytefolge)
 			out.write(secretKeySign);
 			//Verschlsselte Dateidaten (Bytefolge)
 			encodeAndWriteFile(inFile, secretKey, "AES", out);
 			
 			System.out.println("Fertig!");
 		} catch (Exception e) {
 			error(e);
 		}
 	}
 	
 	private static RSAKey getKey(File keyFile, String algo, boolean isPrivate) {
 		String name = "";
 		RSAKey key = null;
 		try {
 			FileInputStream fis = new FileInputStream(keyFile);
 			// Namenslnge auslesen
 			byte[] intLength = new byte[4];
 			fis.read(intLength);
 			ByteBuffer bb = ByteBuffer.wrap(intLength);
 			int nameLength = bb.getInt();
 			System.out.println(nameLength);
 			// Namen auslesen
 			byte[] nameBytes = new byte[nameLength];
 			fis.read(nameBytes);
 			name = new String(nameBytes);
 			System.out.println(name);
 			// Key Lnge auslesen.
 			fis.read(intLength);
 			bb = ByteBuffer.wrap(intLength);
 			int keyLength = bb.getInt();
 			System.out.println(keyLength);
 			// KeyAuslesen
 			byte[] keyBytes = new byte[keyLength];
 			fis.read(keyBytes);
 			// PrivateKey Object erstellen
 			KeyFactory keyFactory = KeyFactory.getInstance(algo);
 			if (isPrivate) {
 				KeySpec ks = new PKCS8EncodedKeySpec(keyBytes);
 				key = (RSAPrivateKey) keyFactory.generatePrivate(ks);
 				System.out.println(((RSAPrivateKey) key).getFormat());
 			} else {
 				X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
 				key = (RSAPublicKey) keyFactory.generatePublic(x509KeySpec);
 				System.out.println(((RSAPublicKey) key).getFormat());
 			}
 			String prvOrPub = "";
 			if (isPrivate) {
 				prvOrPub = "Private";
 			} else {
 				prvOrPub = "Public";
 			}
 			System.out.println(prvOrPub + "Key erfolgreich eingelesen: " + key);
 		} catch (Exception e) {
 			error(e);
 		}
 		return key;
 	}
 	
 	private static SecretKey createSecretAESKey() {
 		SecretKey skey = null;
 		try {
 			KeyGenerator kg = KeyGenerator.getInstance("AES");
 			kg.init(128); // Schlssellnge
 			skey = kg.generateKey();
 			System.out.println("Geheimen Schlssel erzeugt: " + new String(skey.getEncoded()));
 		} catch (Exception e) {
 			error(e);
 		}
 		return skey;
 	}
 	
 	private static byte[] sign(byte[] dataBytes, RSAPrivateKey key) {
 		Signature rsa = null;
 		byte[] signature = null;
 		try {
 			rsa = Signature.getInstance("SHA1withRSA");
 			rsa.initSign(key);
 			rsa.update(dataBytes);
 			signature = rsa.sign();
 			System.out.println("Geheimen Schlssel signiert: " + new String(signature));
 		} catch (Exception e) {
 			error(e);
 		}
 		return signature;
 	}
 	
 	private static byte[] encodeBytes(byte[] dataBytes, Key key, String algo) {
 		Cipher cipher = getCipher(algo, key);
 		byte[] encData = encode(dataBytes, cipher);
 		System.out.println("Geheimen Schlssel verschlsselt: " + new String(encData));
 		return encData;
 	}
 	
 	private static byte[] encodeAndWriteFile(File file, SecretKey key, String algo, OutputStream out) {
 		byte[] encData = null;
 		try {
 			DataInputStream in = new DataInputStream(new FileInputStream(file));
			Cipher cipher = getCipher(algo, key);
			byte[] buffer = new byte[8];
			int len = 0;
			while ((len = in.read(buffer)) > 0) {
				encData = encode(buffer.clone(), cipher);
				out.write(encData.clone());
			}
 			System.out.println("Daten verschlsselt");
 		} catch (Exception e) {
 			error(e);
 		}
 		return encData;
 	}
 
 	private static Cipher getCipher(String algo, Key key) {
 		Cipher cipher = null;
 		try {
 			cipher = Cipher.getInstance(algo);
 			cipher.init(Cipher.ENCRYPT_MODE, key);
 		} catch (Exception e) {
 			error(e);
 		}
 		return cipher;
 	}
 	
 	private static byte[] encode(byte[] dataBytes, Cipher cipher) {
 		byte[] encRest = null;
 		try {
 			// Daten verschlsseln
 			// (update wird bei groen Datenmengen mehrfach aufgerufen werden!)
 			byte[] encData = cipher.update(dataBytes);
 
 			// mit doFinal abschlieen (Rest inkl. Padding ..)
 			encRest = cipher.doFinal();
 
 			//System.out.println("Verschlsselte Daten: " + new String(encData) + " # " + new String(encRest));
 		} catch (Exception e) {
 			error(e);
 		}
 		return encRest;
 	}
 
 	private static void error(Exception e) {
 		if (e instanceof IOException || e instanceof FileNotFoundException) {
 			System.out.println("File not found");
 		} else if (e instanceof NoSuchAlgorithmException) {
 			System.out.println("Algorithm not found");
 		} else if (e instanceof InvalidKeySpecException) {
 			System.out.println("Invalid key");
 		} else if (e instanceof NoSuchProviderException) {
 			System.out.println("Provider not found");
 		} else if (e instanceof SignatureException) {
 			System.out.println("Fehler beim Signieren");
 		} else if (e instanceof BadPaddingException || e instanceof IllegalBlockSizeException) {
 			System.out.println("Fehler bei Verschlsselung");
 		} else if (e instanceof NoSuchPaddingException) {
 			System.out.println("Padding not found");
 		} else {
 			System.out.println("Exception: " + e.getMessage());
 		}	
 		e.printStackTrace();
 	}
 
 }
