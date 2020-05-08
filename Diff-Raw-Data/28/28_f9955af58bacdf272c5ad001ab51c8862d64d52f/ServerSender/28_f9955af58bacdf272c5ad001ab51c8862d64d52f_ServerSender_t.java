 package com.test9.irc.engine;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 
 public class ServerSender extends Thread implements Runnable {
 
 	private String output = null;
 	BufferedWriter out = null;
 	private BufferedReader in = null;
 	private Server server;
 
 	ServerSender(Server server) throws IOException {
 		this.server = server;
 
 		this.out = new BufferedWriter(new OutputStreamWriter(server.getSocket()
 				.getOutputStream()));
 		this.in = new BufferedReader(new InputStreamReader(System.in));
 
 		// Begin initial chat with server.
 		this.out.write("USER " + server.getLogin() + " 0 * JavaIRC"
 				+ Server.RNtail);
 		this.out.write("NICK " + server.getNick() + Server.RNtail);
 
 		this.out.flush();
 	}
 
 	@Override
 	public void run() {
 		while (true) {
			//System.out.println("in serversender run()");
			//System.out.println(server.isConnected);
 			if (server.isConnected == true) {
 				joinChannel();
 				return;
 			}
 			try {
 				this.output = this.in.readLine();
 
 				if (this.output != null) {
 
 					if (this.output.toLowerCase().startsWith("ping ")) {
 						this.ping_pong(this.output.substring(5));
 					} else {
 						// Print the raw line received by the bot.
 						System.out.println("ServerSender outgoing message: "
 								+ this.in);
 
 						this.sendMessage(this.output);
 
 						this.out.flush();
 					}
 				}
 			} catch (IOException e) {
 				e.getStackTrace();
 			}
 
 			try {
 				this.out.flush();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			Thread.yield();
 		}
 	}
 
 	synchronized public void joinChannel() {
 		//TODO: This currently only joins the initial channels.
 		for(Channel c : server.getChannels()){
 		System.out.println("Attempting to JOIN " + c);
 		try {
 			this.out.write("JOIN " + c.getName() + Server.RNtail);
 			this.out.flush();
 			
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		}
 		server.isConnected = false;
 	}
 
 	synchronized public void ping_pong(String server) {
 		try {
 			System.out.println("PONG " + server);
 			this.out.write("PONG " + server + Server.RNtail);
 			this.out.flush();
 		} catch (IOException e) {
 			e.getStackTrace();
 		}
 	}
 
 	synchronized public void sendMessage(String message) {
 		//TODO: Sends message to each channel in server.getChannels().
 		for(Channel c : server.getChannels()){
 		System.out.println("[Message]" + message);
 		if (message != null) {
 			try {
 				this.out.write("PRIVMSG " + c.getName() + " :" + message
 						+ Server.RNtail);
 				this.out.flush();
 
 			} catch (IOException e) {
 				e.getStackTrace();
 			}
 		}
 		}
 	}
 }
