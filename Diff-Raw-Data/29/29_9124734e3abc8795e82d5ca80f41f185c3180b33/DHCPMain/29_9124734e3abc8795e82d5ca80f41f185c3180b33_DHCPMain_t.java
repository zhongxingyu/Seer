 package mnet;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 
 public class DHCPMain {
 	public static void main(String[] args){
		/*try{
 			byte[] data = Helper.sendDiscoverPacket();
 			FileOutputStream file = new FileOutputStream("log.txt");
 			file.write(ByteFactory.simpleFormatted(data).getBytes("US-ASCII"));
 			file.flush();
		}catch (IOException e) { e.printStackTrace(); } /**/
		try{
 			DHCPServer server = new DHCPServer();
 			server.capturePacket();
 			FileOutputStream file = new FileOutputStream("log.txt");
			DHCPPacket packet = new DHCPPacket();
			packet.read(server.getData());
			file.write(("Packet length: "+ packet.array().length + "\n").getBytes("US-ASCII"));
			file.write(ByteFactory.simpleFormatted(packet.array()).getBytes("US-ASCII"));
 			file.close();
 		}catch(SocketException e){e.printStackTrace();
 		}catch(IOException     e){e.printStackTrace();}/**/
 	}
 
 }
