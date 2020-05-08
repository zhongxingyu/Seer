 package FirstManda;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 public class TCPServer {
 
 	private Socket socket;
 	ServerSocket serverSocket;
 	private InputStream is;
 	private DataInputStream dis;
 	private OutputStream os;
 	private DataOutputStream dos;
 	
 	public void startServer(int serverPort) throws IOException{
 		serverSocket = new ServerSocket(serverPort);
 		System.out.println("Server: ServerSocket created on port " + serverPort);
 		
 		System.out.println("Server: Waiting for client...");
 		socket = serverSocket.accept();
 		System.out.println("Server: Established connection to client " + serverSocket.getInetAddress());
 		
 		is = socket.getInputStream();
 		System.out.println("Server: InputStream created.");
 		
 		dis = new DataInputStream(is);
 		System.out.println("Server: DataInputStream created.");
 		
 		String message = dis.readUTF();
 		System.out.println("Server: Received message from client: ");
 		System.out.println(message);
 		
 		socket = serverSocket.accept();
 		if(dis.readInt() == 0){
 			message = message.toLowerCase();
 			System.out.println("Server: Converted message to upper-case.");
 		}
 		else if(dis.readInt() == 1){
 			message = message.toUpperCase();
 			System.out.println("Server: Converted message to upper-case.");
 		}
 		else{
 			System.out.println("Wrong input.");
 		}
	
 		os = socket.getOutputStream();
 		System.out.println("Server: OutputStream created.");
 		
 		dos = new DataOutputStream(os);
 		System.out.println("Server: DataOutputStream created.");
 	
 		dos.writeUTF(message);
 		
 		dos.flush();
 		System.out.println("Server: Flushed DataOutputStream.");
 		
 		socket.close();
 		System.out.println("Server: Closed socket");
 	}
 }
