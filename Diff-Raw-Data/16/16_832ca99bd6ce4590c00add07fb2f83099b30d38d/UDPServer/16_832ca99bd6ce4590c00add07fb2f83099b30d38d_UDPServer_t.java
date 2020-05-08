 package devin.udp;
 
 import java.net.*;
 import java.util.StringTokenizer;
 
 import maze.Maze;
 
 public class UDPServer {
 	
 	public UDPServer() {
 	}
 	
 	
 	public void startServer(Maze m) throws Exception {
 		DatagramSocket serverSocket = new DatagramSocket(10001);
		byte[] receiveData;
		byte[] sendData;
 		for (int i = 0; i < 5; i++) {
			sendData = new byte[1024];
			receiveData = new byte[1024];
 			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
 			serverSocket.receive(receivePacket);
 			String sentence = new String( receivePacket.getData());
 			
 			System.out.println("RECEIVED: " + sentence);
 			String direction = "";
 			String x = "";
 			String y = "";
 			int xInt = 0, yInt = 0;
 			
 			StringTokenizer st = new StringTokenizer(sentence, ",");
 			while (st.hasMoreTokens()) {
 				direction = st.nextToken();
 				//System.out.println(direction);
 				x = st.nextToken();
 				//System.out.println(x);
 				y = st.nextToken();
 				//System.out.println(y);
 			}
 			try {
 				xInt = Integer.parseInt(x.trim());
 				yInt = Integer.parseInt(y.trim());
 			}
 			catch (NumberFormatException e) {
 				System.out.println("Error");
 			}
 			//System.out.println("Intx " + xInt + " Inty " + yInt);
 			//System.out.println(direction + " " + x + " " + y);
 			boolean result = m.getWall(direction, xInt, yInt);
 			
 			InetAddress IPAddress = receivePacket.getAddress();
 			int port = receivePacket.getPort();
 			String response = new Boolean(result).toString();
 			sendData = response.getBytes();
 			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
 			serverSocket.send(sendPacket);
 		}
 		serverSocket.close();
 	}
 }
