 package Server;
 
 import java.net.*;
 import java.io.*;
 
 public class ServerClient implements Runnable {
 	public Socket socket;
 	private Server server;
 	public DataOutputStream out = null;
 	private DataInputStream in = null;
 	public boolean running;
 	private String nickname;
 
 	public ServerClient(Socket socket, Server server) {
 		this.socket = socket;
 		this.server = server;
 		try {
 			in = new DataInputStream(socket.getInputStream());
 			out = new DataOutputStream(socket.getOutputStream());
 			this.nickname = in.readUTF();
 		} catch (IOException e) {
 			System.out.println("IOexception: I or O failed");
 		}
 	}
 
 	@Override
 	public void run() {
 		running = true;
 		while (running) {
 			String intext;
 			try {
 				intext = in.readUTF();
 				if (intext.equals("$DISCONNECT")) {
 					disconnect();
 					break;
 				} else {
 					server.sendText(nickname + ": " + intext);
 				}
 			} catch (IOException e) {
				e.printStackTrace();
 				server.addText("Could not read. " + nickname + " probably dropped.");
 				running = false;
 				server.removeClient(this);
 			}
 		}

 	}
 
 	public void sendText(String text) {
 		try {
 			out.writeUTF(text);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public String getNickname() {
 		return nickname;
 	}
 
 	public void disconnect() {
 		server.sendText("User " + this.nickname + " at " + socket.getInetAddress().toString().replace("/", "") + " disconnected.");
 		server.removeClient(this);
 		try {
 			socket.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 }
