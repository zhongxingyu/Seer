 package game.client;
 
 import game.util.IO.AcknowledgeFlag;
 import game.util.IO.AcknowledgeFlags;
 import game.util.IO.RequestFlag;
 import game.util.IO.RequestFlags;
 import game.util.IO.Packages.GameServerInfoPackage;
 import game.util.IO.Packages.LoginInfoPackage;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 public class EntryPoint {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Socket socket;
 		try {
 			socket = new Socket("81.229.86.19", 12345);
 		 
 			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
 			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
 			AcknowledgeFlag flag;
 			
 			
 			oos.writeObject(new RequestFlag(RequestFlags.loginRequest));
 			oos.writeObject(new LoginInfoPackage("test", "pass".hashCode()));
 			oos.flush();
 			 
 			flag = (AcknowledgeFlag) ois.readObject();
 			if (flag.flag == AcknowledgeFlags.loginGranted) {
 				System.out.println("Login Granted");
 				GameServerInfoPackage gsip = (GameServerInfoPackage) ois.readObject();
 				System.out.println("Gameserver: " + gsip.ip + ":" + gsip.port);
 			}
 			
			oos.writeObject(new RequestFlag(RequestFlags.loginRequest));
			oos.writeObject(new LoginInfoPackage("fail", "badPASS".hashCode()));
			oos.flush();
 			
			flag = (AcknowledgeFlag) ois.readObject();
			if (flag.flag == AcknowledgeFlags.loginRefused)
				System.out.println("Login Refused");
		     
 			ois.close();
 			oos.close();
 			socket.close();
 	   } catch (UnknownHostException e) {
 		   	e.printStackTrace();
 	   } catch  (IOException e) {
 		   	e.printStackTrace();
 	   } catch (ClassNotFoundException e) {
 			e.printStackTrace();
 	   }
 	}
 
 }
