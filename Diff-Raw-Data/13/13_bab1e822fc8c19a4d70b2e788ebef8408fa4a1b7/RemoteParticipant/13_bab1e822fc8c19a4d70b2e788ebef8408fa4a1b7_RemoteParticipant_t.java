 package game;
 import java.util.*;
 import java.io.*;
 import java.net.*;
 import com.google.gson.*;
 
 public class RemoteParticipant extends Participant {
 
 	private Socket socket;
 	
 	RemoteParticipant (Socket clientSocket) throws IOException {
 		this.setSocket(clientSocket);
 		this.setControllerString("No commands sent yet.");
 		this.setReader(new BufferedReader(new InputStreamReader(getSocket().getInputStream())));
 		this.setWriter(new PrintWriter(getSocket().getOutputStream(),true));
 		json = new Gson();
 	}
 	
 	
 	@Override
 	public void readControllerString() throws IOException {
 		this.setControllerString(this.getReader().readLine()); // Should block until line received from client
 	}
 	
 	
 	public void setSocket(Socket socket) {
 		this.socket = socket;
 	}
 	
 	public Socket getSocket() {
 		return socket;
 	}
 
 }
