 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 
 public class WClientHandler extends Thread {
 	private final Socket socket;
 	private final WServe server;
 	private final BufferedReader input;
 	private final PrintWriter output; 
 	private WUser me;
 	
 	public WClientHandler(WServe server, Socket client, WUser who) throws IOException {
 		this.server = server;
 		this.socket = client;
 		this.me = who;
 		
 		input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 		output = new PrintWriter(socket.getOutputStream(), true);
 	}
 	
 	protected void iAm(String name) {
 		me.setName(name);
 	}
 	
 	public String getNick() {
 		return me.getName();
 	}
 	
 	public Integer getClientId() {
 		return me.getId();
 	}
 
 	public void run() {
 		try {
 			String message = input.readLine(); 
 			if (message.startsWith("user ")) {
 				String[] m = message.split(" ");
 				server.setName(this, m[1]);
 				
				server.broadcastTo(me.getId(), server.getUsers()); //send users to client
 				
 			} else {
 				server.broadcastTo(me.getId(), "Please provide a username.");
 				server.removeUser(me.getId());
 				return;
 			}
 			
 			// Begin handling loop
 			while ((message = readMessage()) != null) {
 				decodeMessage(message);
 			}	
 
 		}
 		catch (Exception e) {
 			System.err.println(e.getMessage());
 		}
 		finally {
 			try {
 				server.removeUser(me.getId());
 
 				output.close();
 				input.close();
 				socket.close();
 
 			}
 			catch (IOException e) {
 				System.err.println(e.getMessage());
 			}
 		}
 	}
 
 	//get input from terminal
 	public String readMessage() {
 		try{
 			String line = input.readLine().trim();
 			if(line.equals(""))
 				return null;
 			return line;
 		}
 		catch (IOException e){
 			//System.err.println("IO Error"); 
 			return null;
 		}
 	}
 
 	private void decodeMessage(String message) throws Exception {
 		String[] m = message.split(" ");
 		
 		if(message.startsWith("exit"))
 			server.removeUser(me.getId());
 		else if (message.startsWith("draw")) {
 			String[] mess = message.split(" ");
 			String send = mess[0]+" "+me.getId()+" "+mess[1]+" "+mess[2];
 			server.broadcastExcept(me.getId(), send);
 		} else
 			server.broadcastTo(me.getId(), "Unknow message: " + message);
 	}
 
 	public void sendMessage(String message) {
 		System.out.println(message);
 		output.println(message);
 	}
 
 	public void exit() throws Exception {
 		input.close();
 		output.close();
 		socket.close(); //Closing this socket will also close the socket's InputStream and OutputStream
 	}
 }
