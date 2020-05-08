 import java.io.IOException;
 import java.util.*;
 import java.io.ObjectInputStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 
 public class MapleJuiceThread implements Runnable {
 	Socket sock;
 	Machine machine;
 
 	public MapleJuiceThread(Socket s, Machine m) {
 		// TODO Auto-generated constructor stub
 		sock = s;
 		machine = m;
 	}
 	public void start()
 	{
 		Thread thread  = new Thread(this);
 		thread.start();
 	}
 
 
 	public void run(){
 		//ObjectInputStream ois = null;
 		GenericPayload generic_action=null;
 		/*try {
 			ois = new ObjectInputStream(sock.getInputStream());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}*/
 
 		MapleJuicePayload mjPayload = new MapleJuicePayload();
 		mjPayload.receiveMapleJuicePacket(sock);
 		/*try {
 			mjPayload = (MapleJuicePayload)(ois.readObject());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}*/
 		try {
 			WriteLog.writelog(machine.myName, "Received payload of type " + mjPayload.messageType);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (mjPayload.messageType.equalsIgnoreCase("MapleTask")) {
 			MapleAction maple_action=null;
 			generic_action = mjPayload.parseByteArray();
 			maple_action = (MapleAction)generic_action;
 			((MapleAction)maple_action).processMapleActionPayload(machine);
 		}
 		if (mjPayload.messageType.equalsIgnoreCase("TaskStatus")) {
 			TaskStatus status=null;
 			generic_action = mjPayload.parseByteArray();
 			status = (TaskStatus)generic_action;
 			status.processPayload(sock);
 		}
 		//ois.close();
 
 
 
 		try {
 			String myName = InetAddress.getLocalHost().getHostName();
 			//WriteLog.writelog(myName, "sourceFN: "+sourceFN);
 		} catch (UnknownHostException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}

 	}
 
 }
