 package FirstManda;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 
 public class TCPClient {
 	
 	private Socket socket;
 	private OutputStream os;
 	private DataOutputStream dos;
 	private InputStream is;
 	private DataInputStream dis;
 	
 	public void initialize(String serverAddress, int serverPort) throws IOException{
 		System.out.println("Client: Trying to create socket with port: " + serverPort +
 				" and InetAddress: " + serverAddress);
 		
 		socket = new Socket(serverAddress, serverPort);
 		System.out.println("Client: Socket created.");
 		
 		os = socket.getOutputStream();
 		System.out.println("Client: OutputStream created.");
 		
 		dos = new DataOutputStream(os);
 		System.out.println("Client: DataOutputStream created.");
 		
 		is = socket.getInputStream();
 		System.out.println("Client: InputStream created.");
 		
 		dis = new DataInputStream(is);
 		System.out.println("Client: DataInputStream created.");
 	}
 	
 	public void sendMessage(int i, String message) throws IOException{
 		dos.writeUTF(message);
		
		dos.flush();
		System.out.println("Client: Flushed DataOutputStream.");
		
 		dos.writeInt(i);
 		
 		System.out.println("Client: Message received from server: ");
 		System.out.println(dis.readUTF());
 		
 		dos.flush();
 		System.out.println("Client: Flushed DataOutputStream.");
 		
 		socket.close();
 		System.out.println("Client: Closed socket.");
 	}
 }
