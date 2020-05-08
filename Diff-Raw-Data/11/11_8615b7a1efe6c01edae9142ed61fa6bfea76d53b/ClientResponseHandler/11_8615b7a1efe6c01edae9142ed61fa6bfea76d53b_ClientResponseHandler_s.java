 package dslab.bidclient;
 
 import dslab.channels.Channel;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.security.Key;
 import javax.crypto.Mac; 
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.security.MessageDigest;
 import org.bouncycastle.util.encoders.Base64;
 
 public class ClientResponseHandler extends Thread {
 
 	public ClientResponseHandler() {
 	}
 
 	@Override
 	public void run() {
 		String fromServer;
 		boolean validHash = false;
 		boolean retry = true;
 		while (Data.getInstance().channel.isOpen()) {
 			fromServer = Data.getInstance().channel.receive();
 			if (fromServer != null) {
 				if (fromServer.equals("logging out CODE")) {
 					System.out.println("logoutCode received");
 					Data.getInstance().channel.reset();
 					System.out.println("Channel Reset");
 					continue;
 				}
				else if (fromServer.substring(1,2).equals(".") || fromServer.substring(0,2).equals("Cu")) {
 					try{
 						Key secretKey = Data.getInstance().getSecretKey();
 						Mac hMac = Mac.getInstance("HmacSHA256");
 						hMac.init(secretKey);
 						String list = fromServer.substring(0, fromServer.lastIndexOf(" "));
 						hMac.update(list.getBytes());
 						byte[] computedHash = hMac.doFinal(); 
 						byte[] receivedHash = Base64.decode(fromServer.substring(fromServer.lastIndexOf(" ")+1));
 						validHash = MessageDigest.isEqual(computedHash,receivedHash);
 						if (validHash){
 							System.out.println(list);
 							retry = true;
 						}
 						else {
 							if (retry){
 								String lastCommand;
 								lastCommand = Data.getInstance().getLastCommand();
 								Data.getInstance().channel.send(lastCommand);
 								retry = false;
 							}
 						}
 					}
 
 					catch (NoSuchAlgorithmException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					catch (InvalidKeyException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 
 				} else {
 					System.out.println(fromServer);
 
 				}
 
 
 			}
 			try {
 				Thread.sleep(40);
 			} catch (InterruptedException ex) {
 				break;
 			}
 		}
 	}
 
 
 }
