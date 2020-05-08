 package server;
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import states.TurnState;
 
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
				String[] commandos = outputLine.split(" ");
 				if (outputLine.equals("Bye"))
 					break;
				else if(outputLine.startsWith("NEWPLAYER")){
					
				} else if(outputLine.startsWith("DRAWCARD")){
					
				} else if(outputLine.startsWith("HARVAST")) {
					int field = Integer.valueOf(commandos[1]);
					
					
				}
 			}
 			out.close();
 			in.close();
 			socket.close();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
