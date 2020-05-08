 package edu.berkeley.cs.cs162;
 
 import java.io.BufferedReader;
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Map;
 
 
 public class ChatClient extends Thread{
 	private Socket mySocket;
 	private Map<String,ChatLog> logs;
 	private BufferedReader commands;
 	private ObjectInputStream received;
 	private ObjectOutputStream sent;
 	private Thread receiver;
 	private volatile boolean connected;
 	private Command reply; 				//what reply from server should look like
 	private volatile boolean isWaiting; //waiting for reply from server?
 	private volatile boolean isLoggedIn, isQueued;
 	
 	public ChatClient(){
 		mySocket = null;
 		logs = new HashMap<String,ChatLog>();
 		commands = new BufferedReader(new InputStreamReader(System.in));
 		
 		connected = false;
 		isWaiting = false;
 		reply = null;
 		receiver = null;
         start();
 	}
 	
 	private void connect(String hostname, int port){
 		try {
 			if (connected) {
 				System.err.println("already connected");
 				return;
 			}
 			mySocket = new Socket(hostname,port);
 			sent = new ObjectOutputStream(mySocket.getOutputStream());
 			InputStream input = mySocket.getInputStream();
 			received = new ObjectInputStream(input);
 			
 			connected = true;
 			output("connect OK");
 			if(receiver == null || !receiver.isAlive()) {
 				receiver = new Thread(){
 		            @Override
 		            public void run(){
 		            	while(connected){
 		            		receive();
 		            	}
 		            }
 		        };
 				receiver.start();
 			}
 		} catch (IllegalThreadStateException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			output("connect REJECTED");
 			e.printStackTrace();
 		}
 	}
 	
 	private void disconnect(){
 		if(!connected)
 			return;
 		try {
 			mySocket.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		if(isLoggedIn)
 			output("logout OK");
 		output("disconnect OK");
 		isLoggedIn = false;
 		isQueued = false;
 		connected = false;
 	}
 	
 	private void output(String o){
 		System.out.println(o);
 	}
 	
 	private void adduser(String username, String password){
 		if(!connected || isLoggedIn || isQueued)
 			return;
 		TransportObject toSend = new TransportObject(Command.adduser,username,password);
 		try{
 			isWaiting = true;
 			sent.writeObject(toSend);
 			reply = Command.adduser;
 			this.wait();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void login(String username, String password){
 		if (!connected && (!isLoggedIn || !isQueued))
 			return;
 		TransportObject toSend = new TransportObject(Command.login, username, password);
 		try {
 			isWaiting = true;
 			reply = Command.login;
 			sent.writeObject(toSend);
 			this.wait();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void logout(){
 		if (!connected || (!isLoggedIn && !isQueued))
 			return;
 		TransportObject toSend = new TransportObject(Command.logout);
 		try {
 			sent.writeObject(toSend);
 			isWaiting = true;
 			reply = Command.logout;
 			this.wait();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void join(String gname){
 		if(!connected || !isLoggedIn)
 			return;
 		TransportObject toSend = new TransportObject(Command.join,gname);
 		try {
 			isWaiting = true;
 			reply = Command.join;
 			sent.writeObject(toSend);
 			this.wait();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return;
 	}
 	
 	private void leave(String gname){
 		if(!connected || !isLoggedIn)
 			return;
 		TransportObject toSend = new TransportObject(Command.leave,gname);
 		try {
 			isWaiting = true;
 			reply = Command.leave;
 			sent.writeObject(toSend);
 			this.wait();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return;
 	}
 	
 	private void send(String dest, int sqn, String msg){
 		if(!connected || !isLoggedIn)
 			return;
 		TransportObject toSend = new TransportObject(Command.send,dest,sqn,msg);
 		try {
 			isWaiting = true;
 			reply = Command.send;
 			sent.writeObject(toSend);
 			this.wait();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void receive(){
 		TransportObject recObject = null;
 		try {
 			Object o = received.readObject();
 			//if(o!=null)
 				//System.out.println(o);
 			recObject = (TransportObject) o;
 		} catch (SocketException e) {
 			connected = false;
 			return;
 		} catch (EOFException e) {
 			connected = false;
 			return;
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			connected = false;
 			return;
 		}
 		
 		if (recObject == null){
 			return;
 		}
 		Command type = recObject.getCommand();
 		ServerReply servReply = recObject.getServerReply();
 		if (servReply.equals(ServerReply.error)) {
 			System.err.println("Error");
 		} else if (isWaiting && type.equals(reply)) {
 			if (reply.equals(Command.disconnect) || reply.equals(Command.login) || 
 					reply.equals(Command.logout) || reply.equals(Command.adduser)) {
 				output(type.toString() + " " + servReply.toString());
 				if (reply.equals(Command.disconnect)){
 					connected = false;
 					isLoggedIn = false;
 					isQueued = false;
 				}
 				else if (reply.equals(Command.login)){
 					if(recObject.getServerReply().equals(ServerReply.OK)){
 						isQueued = false;
 						isLoggedIn = true;
 					}else if(recObject.getServerReply().equals(ServerReply.QUEUED)){
 						isQueued = true;
 					}
 				}else if (reply.equals(Command.logout)){
 					isLoggedIn = false;
 					isQueued = false;
 				}
 			}
 			else if (reply.equals(Command.join) || reply.equals(Command.leave))
 				output(type.toString() + " " + recObject.getGname() + " " + servReply.toString());
 			else if (reply.equals(Command.send))
 				output(type.toString() + " " + recObject.getSQN() + " " + servReply.toString());
 			else
 				return;
 			
 			isWaiting = false;
 			reply = null;
 			synchronized(this) { this.notify(); }
 		} 
 		
 		else if (servReply.equals(ServerReply.sendack))
 			output(servReply.toString() + " " + recObject.getSQN() + " FAILED");			
 		else if (servReply.equals(ServerReply.receive))
 			output(servReply.toString() + " " + recObject.getTimestamp() + " " +
 					recObject.getSender() + " " + recObject.getDest() + " " + recObject.getMessage());
 		else if (servReply.equals(ServerReply.timeout)) {
 			output(servReply.toString());
 			connected = false;
 		}else if (type.equals(Command.login) && isQueued){
 			output("login OK");
 			isQueued = false;
 			isLoggedIn = true;
 		} else {
 			System.out.println("What kind of server reply is this? " + servReply);
 		}
 	}
 	
 	private void sleep(int time){
 		try {
 			output("sleep OK");
 			Thread.sleep(time);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void readlog(){
 		TransportObject toSend = new TransportObject(Command.readlog);
 		try{
 			sent.writeObject(toSend);
 		} catch (Exception e){
 			connected = false;
 			isLoggedIn = false;
 			isQueued = false; //should be already
			e.printStackTrace();
 		}
		System.out.println("sent readlog request, connect: " + connected);
 	}
 	
 	public Map<String,ChatLog> getLogs(){
 		return logs;
 	}
 	
 	public synchronized void processCommands() throws Exception {
 		String command = null;
 		try {
 			command = commands.readLine();
 		} catch (IOException e) {
 			e.printStackTrace();
 			return;
 		}
 		if(command == null)
 			return;
 		String[] tokens = command.split(" ");
 		int args = tokens.length;
 		if (tokens.length == 0)
 			return;
 		
 		if (tokens[0].equals("connect")){
 			if(args != 2)
 				throw new Exception("invalid arguments for connect command");
 			tokens = tokens[1].split(":");
 			args = tokens.length;
 			if (args != 2)
 				throw new Exception("invalid arguments for connect command");
 			String hostname = tokens[0];
 			int port;
 			try {
 				port = Integer.parseInt(tokens[1]);
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 				return;
 			}
 			connect(hostname, port);
 		}
 		else if (tokens[0].equals("disconnect")) {
 			if (args != 1)
 				throw new Exception("invalid arguments for disconnect command");
 			disconnect();
 		}
 		else if (tokens[0].equals("login")) {
 			if (args != 3)
 				throw new Exception("invalid arguments for login command");
 			String username = tokens[1];
 			String password = tokens[2];
 			login(username,password);
 		}
 		else if (tokens[0].equals("adduser")) {
 			if (args != 3)
 				throw new Exception("invalid arguments for adduser command");
 			String username = tokens[1];
 			String password = tokens[2];
 			adduser(username,password);
 		}
 		else if (tokens[0].equals("logout")) {
 			if(args != 1)
 				throw new Exception("invalid arguments for logout command");
 			logout();
 		}
 		else if (tokens[0].equals("join")) {
 			if(args != 2)
 				throw new Exception("invalid arguments for join command");
 			String gname = tokens[1];
 			join(gname);
 		}
 		else if (tokens[0].equals("leave")) {
 			if(args != 2)
 				throw new Exception("invalid arguments for leave command");
 			String gname = tokens[1];
 			leave(gname);
 		}
 		else if (tokens[0].equals("send")) {
 			if(args < 4)
 				throw new Exception("invalid arguments for send command");
 			tokens = command.split(" ",4);
 			String dest = tokens[1];
 			int sqn;
 			try {
 				sqn = Integer.parseInt(tokens[2]);
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 				return;
 			}
 			String msg = tokens[3];
 			send(dest,sqn,msg);
 		}
 		else if (tokens[0].equals("sleep")) {
 			if(args != 2)
 				throw new Exception("invalid arguments for sleep command");
 			int time;
 			try {
 				time = Integer.parseInt(tokens[1]);
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 				return;
 			}
 			sleep(time);
 		}
 		else if (tokens[0].equals("readlog")){
 			if(args!=1)
 				throw new Exception("invalid arguments for readlog command");
 			readlog();
 		}
 		
 		else {
 			throw new Exception("invalid command");
 		}
 	}
 	
 	@Override
 	public void run(){
 		while(true){
 			try {
 				processCommands();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public static void main(String[] args) throws UnknownHostException{
 		new ChatClient();
 		
 	}
 }
