 package dslab.auctionserver;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.util.Date;
 
 import dslab.analyticsserver.BidEvent;
 import dslab.analyticsserver.EventNotFoundException;
 import dslab.analyticsserver.UserEvent;
 import dslab.channels.Base64Channel;
 import dslab.channels.Channel;
 import dslab.channels.ChannelDecorator;
 import dslab.channels.SecureChannel;
 import dslab.channels.TcpChannel;
 import java.io.UnsupportedEncodingException;
 import java.net.Socket;
 import java.nio.charset.Charset;
 import java.security.InvalidAlgorithmParameterException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.security.spec.AlgorithmParameterSpec;
 import java.security.spec.MGF1ParameterSpec;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.KeyGenerator;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.SecretKey;
 import javax.crypto.spec.IvParameterSpec;
 import javax.crypto.spec.OAEPParameterSpec;
 import javax.crypto.spec.PSource;
 import javax.crypto.spec.SecretKeySpec;
 import org.bouncycastle.util.encoders.Base64;
 
 public class tcpRequestCommunication extends Thread {
     private final Socket socket;
     private User currentUser;
     private Channel channel;
     private boolean shakedHands;
 
     tcpRequestCommunication(Channel channel, User currentUser, Socket socket) {
 	this.currentUser = currentUser;
 	this.channel = channel;
 	this.socket=socket;
     }
 
     public void run() {
 	Protocol p = new Protocol();
 	String inputLine;
 	String outputWhole = "";
 	while (true) {
 	    inputLine = channel.receive();
 	    if (inputLine != null) {
 		try {
 		    outputWhole = p.processInput(inputLine, this);
 		} catch (UnknownParameterException ex) {
 		    //TODO
 		}
 		channel.send(outputWhole);
 	    }
 	    try {
 		Thread.sleep(40);
 	    } catch (InterruptedException ex) {
 		break;
 	    }
 	}
 	try {
 	    AnalyticsServerProtocol.getInstance().processEvent(new UserEvent(UserEvent.disconnect, new Date().getTime(), currentUser.getUsername()));
 	} catch (EventNotFoundException enf) {
 	    // TODO Auto-generated catch block
 	    System.out.println(enf.getMessage());
 	} catch (NullPointerException ex) {
 	    try {
 		AnalyticsServerProtocol.getInstance().processEvent(new UserEvent(UserEvent.disconnect, new Date().getTime(), "Unknown User"));
 	    } catch (EventNotFoundException ex1) {
 		Logger.getLogger(tcpRequestCommunication.class.getName()).log(Level.SEVERE, null, ex1);
 	    }
 	}
 	System.out.println("Exiting tcpRequestCommunication");
     }
 
     public User getCurrentUser() {
 	return currentUser;
     }
 
     public void setCurrentUser(User currentUser) {
 	this.currentUser = currentUser;
     }
 
     public Channel getChannel() {
 	return channel;
     }
 
     public void setChannel(Channel channel) {
 	this.channel = channel;
     }
 
     public boolean isShakedHands() {
 	return shakedHands;
     }
 
     public void setShakedHands(boolean shakedHands) {
 	this.shakedHands = shakedHands;
     }
 
     public String shakeHands(String firstMessage) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
 	//shake hands!
 	final String B64 = "a-zA-Z0-9/+";
 	byte[] firstMessageBytes = Base64.decode(firstMessage);
 
 
 	Cipher crypt = Cipher.getInstance("RSA/NONE/OAEPWithSHA256AndMGF1Padding");
 	crypt.init(Cipher.DECRYPT_MODE, Data.getInstance().getPrivateKeyServer());
 	firstMessageBytes = crypt.doFinal(firstMessageBytes);
 
 	firstMessage = new String(firstMessageBytes);
 	assert firstMessage.matches("!login [a-zA-Z0-9_\\-]+ [0-9]+ ["+B64+"]{43}=") : "1st message";
 
 	String[] args = firstMessage.split(" ");
 
 	if (args.length != 4) {
 	    System.out.println("length of arguments not 4");
 	    return "Wrong command: !login requires exactly one additional argument! Usage: !login name";
 	} else {
 	    if (!"!login".equals(args[0])) {
 		System.out.println("first message does not start with !login");
 	    }
 	    if (currentUser == null) {
 		currentUser = new User(args[1]);
 		int index = Data.getInstance().getUserIndex(currentUser);
 		if (index == -1) {
 		    Data.getInstance().addUser(currentUser);
 		} else {
 		    currentUser = Data.getInstance().getUser(index);
 		    if (currentUser.isActive()) {
 			currentUser = null;
 			return "User already logged in!";
 		    }
 		}
 		currentUser.setActive(true);
 		currentUser.setTcpPort(args[2]);
 		currentUser.initPublicKey();
 		currentUser.initSecretKey();
 	    } else {
 	    	return "Already logged in! Log out first!";
 		//return "!login " + currentUser.getUsername() + System.getProperty("line.separator") + "Already logged in as " + currentUser.getUsername() + "! Please log out before you log in again!";
 	    }
 	}
 
 	SecureRandom secureRandom = new SecureRandom();
 	final byte[] number = new byte[32];
 	secureRandom.nextBytes(number);
 
 	byte[] serverChallenge = Base64.encode(number);
 	String encServerChallenge = new String(serverChallenge);
 	
 	
 	crypt.init(Cipher.ENCRYPT_MODE, this.currentUser.getPublicKey());
 
 
 	KeyGenerator generator = KeyGenerator.getInstance("AES");
 	generator.init(256);
 	SecretKey key = generator.generateKey();
 	byte[] secretKey = Base64.encode(key.getEncoded());
 
 	final byte[] iv = new byte[16];
 	secureRandom.nextBytes(iv);
 	String secondmessage = "!ok " + args[3]/*clientChallenge*/ + " " + encServerChallenge + " " + new String(secretKey) + " " + new String(Base64.encode(iv));
 	assert secondmessage.matches("!ok ["+B64+"]{43}= ["+B64+"]{43}= ["+B64+"]{43}= ["+B64+"]{22}==") : "2nd message";
 	byte[] scdmsgb64enc = crypt.doFinal(Base64.encode(secondmessage.getBytes()));
 
 	scdmsgb64enc = Base64.encode(scdmsgb64enc);
 	this.channel.send(new String(scdmsgb64enc));
 	
 	AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
 	channel = new SecureChannel(channel, key, paramSpec);
 	String thirdMessage;
 	while ((thirdMessage = channel.receive()) == null) {
 	    
 	}
 	if(thirdMessage.equals(encServerChallenge)){
 		System.out.println("Server-Challenge came back right");
 	} else{
 		System.out.println("Server-Challenge came back WRONG");
 	}
 	try {
 	    AnalyticsServerProtocol.getInstance().processEvent(new UserEvent(UserEvent.login, new Date().getTime(), currentUser.getUsername()));
 	} catch (EventNotFoundException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 
 	return "Successfully logged in as " + currentUser.getUsername() + "!";
     }
     
     public synchronized void resetChannel(){
	this.channel.reset();
     }
     
 }
