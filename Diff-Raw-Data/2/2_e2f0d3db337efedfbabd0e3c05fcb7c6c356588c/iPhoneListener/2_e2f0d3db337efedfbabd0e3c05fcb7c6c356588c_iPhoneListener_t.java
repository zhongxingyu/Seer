 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.UnknownHostException;
 
 
 public class iPhoneListener {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		System.out.println("iPhoneListener");
 		DatagramSocket socket = null;
 		FileWriter fwriter = null;
 		PrintWriter pwriter = null;
 
 		try {
 //			sock = new Socket("10.0.2.1", 10552);
 //			get a datagram socket
 			socket = new DatagramSocket(5169);
 			fwriter = new FileWriter("locLog.csv");
 			pwriter = new PrintWriter(fwriter);
 
 			// send request
 			byte[] buf = new byte[256];
 
 			// get response
 			DatagramPacket packet = new DatagramPacket(buf, buf.length);
 			while (1==1) {
 				socket.receive(packet);
 				if (packet.getLength() > 0) {
 					// display response
					String received = new String(packet.getData(), 0, packet.getLength());
 					System.out.print(received );
 					pwriter.print(received);
 				}
 			}
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			if (socket != null)
 				socket.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			if (socket != null)
 				socket.close();
 		}
 		finally {
 			if (socket != null)
 				socket.close();
 		}
 
 	}
 
 }
