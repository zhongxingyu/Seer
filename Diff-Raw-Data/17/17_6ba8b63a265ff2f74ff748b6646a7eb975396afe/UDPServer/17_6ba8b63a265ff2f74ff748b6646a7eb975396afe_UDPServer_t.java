 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 
 
 public class UDPServer {
 	private byte[] receivedData = new byte[1000];
 	private byte[] sendingData = new byte[1000];
 	private int sendingPort = 0;
 	private InetAddress clientAddress;
 	private DatagramSocket serverSocket;
 	private int seq = 0;	/*sequence number of sending UDP packet*/ 
 	
 	public void Listen(int port) throws IOException{
 		serverSocket = new DatagramSocket(port);
 		System.out.println("Server is listenning on port " + port);
 		while (true){
 			DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
 			serverSocket.receive(receivedPacket);
 			/*something received from the client*/
 			System.out.println("Server received " + new String(receivedPacket.getData()));
 			String data = new String(receivedPacket.getData());
 			clientAddress = receivedPacket.getAddress();
 			sendingPort = receivedPacket.getPort();
			//if (data.equals("request")){	//reply the client's request.
 				StartReply();
			//}
 		}
 	}
 
 			
 	private void StartReply() throws IOException{
 		seq = 0;	/*reset the seq number*/
 		DatagramPacket sendPacket;
 		System.out.println("Server stats replying ....");
 		while (true){
 			sendingData = Integer.toString(seq).getBytes();
 			sendPacket = new DatagramPacket(sendingData, sendingData.length, clientAddress, sendingPort);
 			serverSocket.send(sendPacket);
 			System.out.println("Server: " +seq);
 			seq++;
 		}
 	}
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		// TODO Auto-generated method stub
 		UDPServer server = new UDPServer();
 		server.Listen(6789);
 	}
 	
 }
