 import java.io.IOException;
 import java.net.DatagramPacket;
 
 public class UDPListener {
 
 	
 	Interpreter interpreter = new Interpreter();
 
 	public void run() {
 		
 		try {
 			byte[] buf = new byte[256];
 			DatagramPacket packet = new DatagramPacket(buf, buf.length);
 			RouterRecords.UDPsocket.receive(packet);
 			String broadcast = interpreter.packetDataToString(packet.getData());
			System.out.println("I received a BROADCAST: ");
 			RouterRecords.broadcastsCache.add(broadcast);
 			// process input
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}	
 }
