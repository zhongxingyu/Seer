 package server;
 
import java.net.*;
import java.io.*;
 
 public class ServerThread extends Thread {
 	private Socket socket = null;
 	private Server server;
 	int id;
 
 	public ServerThread(Socket socket, Server server, int id) {
 		super("ServerThread");
 		this.socket = socket;
 		this.server = server;
 		this.id = id;
 	}
 
 	public void run() {
 
 		try {
 			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
 			BufferedReader in = new BufferedReader(
 					new InputStreamReader(
 							socket.getInputStream()));
 
 			String inputLine, outputLine;
 			outputLine = "Output";
 			out.println(outputLine);
 			
 			while ((inputLine = in.readLine()) != null) {
 				server.broadcast(id, inputLine);
 				out.println(outputLine);
 				if (outputLine.equals("Bye"))
 					break;
 			}
 			out.close();
 			in.close();
 			socket.close();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
