 import lejos.pc.comm.*;
 import java.io.*;
 import java.util.*;
 
 public class Connections implements Runnable {
 	
 	public void run() {
 		while(true) {
 			try {
 				System.out.println("Verbindungen aufbauen...");
 				connectTransporters();
 
 				Thread.yield();
				Thread.sleep(10000);
 				Thread.yield();
 			}
 			catch (Exception e) {
 			
 			}
 		}
 	}
 	
 	public static void connectTransporters() throws Exception {
 		NXTComm nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
 		
 		NXTInfo[] infos = new NXTInfo[3];
 		infos[0] = new NXTInfo(NXTCommFactory.BLUETOOTH, "IPS_05", "00:16:53:02:A5:1F");
 		infos[1] = new NXTInfo(NXTCommFactory.BLUETOOTH, "NXT_07", "00:16:53:04:ED:8C");
 		infos[2] = new NXTInfo(NXTCommFactory.BLUETOOTH, "NXT_34", "00:16:53:05:94:8F");
 		
 		for (NXTInfo info:infos) {
 			if (!isConnected(info.name)) {
 				try {
 					nxtComm.open(info);
 
 					InputStream is = nxtComm.getInputStream();
 					OutputStream os = nxtComm.getOutputStream();
 
 					DataInputStream in = new DataInputStream(is);
 					DataOutputStream out = new DataOutputStream(os);
 
 
 					Transporter t = new Transporter(info.name.substring(4), in, out);
 					System.out.println("Verbindung zu " + info.name + " wird aufgebaut..."); 
 					System.out.println("Warten auf Rückmeldung von " + info.name + "..."); 
 					t.waitForMessage('k');
 					System.out.println(info.name + " hat sich gemeldet."); 
 					Server.transporters.add(t);
 					nxtComm = null;
 				}
 				catch (Exception e){
 	
 				}
 			}
 		}
 	}
 	
 	public static boolean isConnected(String name) {
 		for (Transporter t: Server.transporters) {
 			if (t.name.equals(name)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 }
