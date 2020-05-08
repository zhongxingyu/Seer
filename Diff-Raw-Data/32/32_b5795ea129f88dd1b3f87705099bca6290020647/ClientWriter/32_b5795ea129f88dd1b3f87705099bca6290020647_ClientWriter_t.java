 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dslab.bidclient;
 
 import dslab.channels.Channel;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.security.InvalidAlgorithmParameterException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.crypto.BadPaddingException;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 
 /**
  *
  * @author Robert Rainer
  */
 public class ClientWriter extends Thread {
 
     public ClientWriter() {
     }
 
     @Override
     public void run() {
 	BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
 	String fromUser;
 
 	try {
 	    while ((fromUser = stdIn.readLine()) != null) {
 		if (fromUser.length() > 6 && fromUser.substring(0, 6).equals("!login")) {
 		    String[] args = fromUser.split(" ");
 		    if (args.length != 2) {
 			System.out.println("!login <username> und nichts sonst!");
 			continue;
 		    } else {
 			Data.getInstance().setUserName(args[1]);
 			Data.getInstance().initKeys();
 			try {
 			    Data.getInstance().shakeHands();
 			} catch (UnsupportedEncodingException ex) {
 			    Logger.getLogger(ClientWriter.class.getName()).log(Level.SEVERE, null, ex);
 			} catch (InvalidAlgorithmParameterException ex) {
 			    Logger.getLogger(ClientWriter.class.getName()).log(Level.SEVERE, null, ex);
 			} catch (NoSuchAlgorithmException ex) {
 			    Logger.getLogger(ClientWriter.class.getName()).log(Level.SEVERE, null, ex);
 			} catch (InvalidKeyException ex) {
 			    Logger.getLogger(ClientWriter.class.getName()).log(Level.SEVERE, null, ex);
 			} catch (NoSuchPaddingException ex) {
 			    Logger.getLogger(ClientWriter.class.getName()).log(Level.SEVERE, null, ex);
 			} catch (IllegalBlockSizeException ex) {
 			    Logger.getLogger(ClientWriter.class.getName()).log(Level.SEVERE, null, ex);
 			} catch (BadPaddingException ex) {
 			    Logger.getLogger(ClientWriter.class.getName()).log(Level.SEVERE, null, ex);
 			}
 			continue;
 		    }
 		}
 		if (fromUser.length() > 7 && fromUser.substring(0, 7).equals("!logout")) {
 		    
 		    String[] args = fromUser.split(" ");
 		    if (args.length != 1) {
 			System.out.println("!logout hat keine Parameter ");
 			continue;
 		    } else {
 			Data.getInstance().channel.send(fromUser);
 			System.out.println("!logout sent to server");
 			continue;
 		    }
 		}
		
 		Data.getInstance().channel.send(fromUser);
		
 		if (fromUser.equals("!end")) {
 		    break;
 		}
 	    }
 	} catch (IOException ex) {
 	    Logger.getLogger(ClientWriter.class.getName()).log(Level.SEVERE, null, ex);
 	}
 	try {
 	    stdIn.close();
 	} catch (IOException ex) {
 	    Logger.getLogger(ClientWriter.class.getName()).log(Level.SEVERE, null, ex);
 	} finally {
 	    Client.shutdown();
 	}
     }
 }
