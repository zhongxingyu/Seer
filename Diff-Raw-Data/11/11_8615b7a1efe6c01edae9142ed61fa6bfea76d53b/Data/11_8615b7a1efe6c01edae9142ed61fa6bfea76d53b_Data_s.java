 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package dslab.bidclient;
 
 import dslab.channels.Channel;
 import dslab.channels.SecureChannel;
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.security.InvalidAlgorithmParameterException;
 import java.security.InvalidKeyException;
 import java.security.Key;
 import java.security.KeyPair;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.SecureRandom;
 import java.security.spec.AlgorithmParameterSpec;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.SecretKey;
 import javax.crypto.spec.IvParameterSpec;
 import javax.crypto.spec.SecretKeySpec;
 import org.bouncycastle.openssl.PEMReader;
 import org.bouncycastle.openssl.PasswordFinder;
 import org.bouncycastle.util.encoders.Base64;
 import org.bouncycastle.util.encoders.Hex; 
 
 /**
  * "Datenbank" des Bid-Client
  * @author Robert Rainer
  */
 public class Data {
 
 	private static Data instance = null;
 	private PrivateKey privateKeyClient = null;
 	private PublicKey publicKeyServer = null;
 	private String keydirpath = null;
 	private String pathToServerKey = null;
	private String user = "alice";
 	private String clientTcpPort;
 	public Channel channel;
 	private Key key;
 	private String lastCommand;
 	/**
 	 * Default-Konstruktor, der nicht außerhalb dieser Klasse
 	 * aufgerufen werden kann
 	 */
 	private Data() {
 		//hier sollten alle Objekt-Datentypen instanziert werden
 
 	}
 
 	/**
 	 * Statische Methode, liefert die einzige Instanz dieser
 	 * Klasse zurück
 	 */
 	public static Data getInstance() {
 		if (instance == null) {
 			instance = new Data();
 		}
 		return instance;
 	}
 
 
 
 	public void initKeys(){
 		//first get the private key:
 		PEMReader privIn = null, publIn = null;
 		try {
 			//private key       
 			System.out.println(keydirpath+user+".pem");
 			privIn = new PEMReader(new FileReader(keydirpath+user+".pem"), new PasswordFinder() {
 				@Override
 				public char[] getPassword() {
 					// reads the password from standard input for decrypting the private key
 					System.out.println("Enter pass phrase:");
 					try {
 						return new BufferedReader(new InputStreamReader(System.in)).readLine().toCharArray();
 					} catch (IOException ex) {
 						return "".toCharArray();
 					}
 				}
 			});
 		} catch (FileNotFoundException ex) {
 			System.out.println(user+": cannot find his/her private key file...");
 		}
 		KeyPair keyPair = null; 
 		try {
 			keyPair = (KeyPair) privIn.readObject();
 		} catch (IOException ex) {
 			System.out.println("Wrong password");
 			initKeys();
 			return;
 		}
 
 		privateKeyClient = keyPair.getPrivate();
 
 		try {
 			publIn = new PEMReader(new FileReader(this.pathToServerKey));
 		} catch (FileNotFoundException ex) {
 			System.out.println(user+": cannot find the servers public key...");
 		}
 		try {
 			publicKeyServer = (PublicKey) publIn.readObject();
 		} catch (IOException ex) {
 			Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
 		}
 		try {
 			privIn.close();
 			publIn.close();
 		} catch (IOException ex) {
 			Logger.getLogger(Data.class.getName()).log(Level.SEVERE, null, ex);
 		}
 
 		byte[] keyBytes = new byte[1024];
 		try{
 			String pathToSecretKey = keydirpath+user+".key";
 					FileInputStream fis = new FileInputStream(pathToSecretKey);
 			fis.read(keyBytes);
 			fis.close();
 			byte[] input = Hex.decode(keyBytes);
 			key = new SecretKeySpec(input, "HmacSHA256"); 
 		}
 		catch (FileNotFoundException ex) {
 			System.out.println(user+": cannot find his/her secret key file...");
 		}
 		catch (IOException ioe) {
 			System.out.println("ERROR: IOException reading secret key");
 		}
 
 	}
 
 
 
 	public PrivateKey getPrivateKeyClient() {
 		return privateKeyClient;
 	}
 
 	public void setPrivateKeyClient(PrivateKey privateKeyClient) {
 		this.privateKeyClient = privateKeyClient;
 	}
 	
 	public Key getSecretKey() {
 		return key;
 	}
 
 	public void setSecretKey(Key key) {
 		this.key = key;
 	}
 
 	public PublicKey getPublicKeyServer() {
 		return publicKeyServer;
 	}
 
 	public void setPublicKeyServer(PublicKey publicKeyServer) {
 		this.publicKeyServer = publicKeyServer;
 	}
 
 	public String getKeydirpath() {
 		return keydirpath;
 	}
 
 	public void setKeydirpath(String keydirpath) {
 		this.keydirpath = keydirpath;
 	}
 
 	public String getPathToServerKey() {
 		return pathToServerKey;
 	}
 
 	public void setPathToServerKey(String pathToServerKey) {
 		this.pathToServerKey = pathToServerKey;
 	}
 
 	public String getUserName() {
 		return user;
 	}
 
 	public void setUserName(String user) {
 		this.user = user;
 	}
 
 	public String getUser() {
 		return user;
 	}
 
 	public void setUser(String user) {
 		this.user = user;
 	}
 
 	public String getClientTcpPort() {
 		return clientTcpPort;
 	}
 
 	public void setClientTcpPort(String clientTcpPort) {
 		this.clientTcpPort = clientTcpPort;
 	}
 	
 	public void setLastCommand(String lastCommand){
 		this.lastCommand = lastCommand;
 	}
 	
 	public String getLastCommand(){
 		return lastCommand;
 	}
 
 	public void shakeHands() throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidAlgorithmParameterException{
 		final String B64 = "a-zA-Z0-9/+";
 		SecureRandom secureRandom = new SecureRandom(); 
 		final byte[] number = new byte[32]; 
 		secureRandom.nextBytes(number);
 
 		byte[] encodedNumber = Base64.encode(number);
 		String clientChallenge = new String(encodedNumber);
 		String firstMessage = "!login "+user+" "+clientTcpPort+" "+(clientChallenge);
 		assert firstMessage.matches("!login [a-zA-Z0-9_\\-]+ [0-9]+ ["+B64+"]{43}=") : "1st message";
 
 		byte[] message = firstMessage.getBytes();
 		Cipher crypt = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding"); 
 		crypt.init(Cipher.ENCRYPT_MODE, publicKeyServer);
 
 		message = crypt.doFinal(message);
 		message = Base64.encode(message);
 		String encryptedFirstMessage = new String(message);
 		channel.send(encryptedFirstMessage);
 
 		String sndMessage;
 		while((sndMessage=channel.receive())==null){
 			//maybe implement some kind of timeout    
 		}
 		if(sndMessage.startsWith("Error")){
 		    System.out.println(sndMessage);
 		    return;
 		}
 		
 		
 		byte[] secondMessage = Base64.decode(sndMessage.getBytes());
 		crypt.init(Cipher.DECRYPT_MODE, this.privateKeyClient);
 
 		secondMessage = crypt.doFinal(secondMessage);
 
 		sndMessage = new String(secondMessage);
 		assert sndMessage.matches("!ok ["+B64+"]{43}= ["+B64+"]{43}= ["+B64+"]{43}= ["+B64+"]{22}==") : "2nd message";
 
 		String[] args = sndMessage.split(" ");
 
 		if(args.length!=5){
 			System.out.println("length of arguments not 5");
 		} else{
 			if(!"!ok".equals(args[0])){
 				System.out.println("second message does not start with !ok");
 				return;
 			}
 			if(!args[1].equals(clientChallenge)){
 				System.out.println("client Challenge wrong");
 			} 
 			byte[] secretKey = Base64.decode(args[3].getBytes());
 			SecretKey  key = new SecretKeySpec(secretKey,"AES/CTR/NoPadding" );
 
 			byte[] ivParam = Base64.decode(args[4]);
 			AlgorithmParameterSpec paramSpec = new IvParameterSpec(ivParam);
 
 			this.channel = new SecureChannel(channel, key, paramSpec);
 
 			assert args[2].matches("["+B64+"]{43}=") : "3rd message";
 
 			String thirdMessage = args[2];
 
 			channel.send(thirdMessage);
 		}
 	}
 }
