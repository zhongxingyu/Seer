 package com.bot42;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 public class Bot42 {
 	public static String nick = "Bot42";
 	public static String host = "irc.kottnet.net";
 	public static int port = 6667;
 	public static Socket ircSocket;
 	
 	public static PrintWriter ircWriter = null;
 	public static BufferedReader ircReader = null;
 	
 	public static List<String> joinedChannels = new LinkedList<String>();
 	public static HashMap<String, List<String>> channelOps = new HashMap<String, List<String>>();
 	
 	public static void main(String[] args) {
 		System.out.println("Bot42 IRC Bot by Vijfhoek and F16Gaming.");
 		System.out.println("TODO: Add copyright information if we're going to use a license.");
 		System.out.println("TODO: Add more TODO statements.");
 		System.out.println();
 		
 		boolean connected = false;
 		
 		try {
 			ircSocket = new Socket(host, port);
 			ircWriter = new PrintWriter(ircSocket.getOutputStream());
 			ircReader = new BufferedReader(new InputStreamReader(ircSocket.getInputStream()));
 			
 			write("NICK " + nick);
 			write("USER " + nick.toLowerCase() + " 0 * :" + nick);
 			
 			while (true) {
 				String message = read();
 				String[] splitMessage = message.split(" ");
 				
 				if (splitMessage[0].equals("PING")) {
 					write("PONG " + splitMessage[1]);
 				}
 				if (!connected) {
 					if (splitMessage[1].equals("376")) {
 						connected = true;
 						write("JOIN #Bot42");
 						write("PRIVMSG #Bot42 :Hello World!");
 					} else if (splitMessage[1].equals("433")) {
 						nick = nick + "|2";
 						write("NICK " + nick);
 					}
 				}
 				
 				if (splitMessage[1].equals("366")) {
 					joinedChannels.add(splitMessage[3]);
 				} else if (splitMessage[1].equals("353")) {
 					List<String> ops = new LinkedList<String>(); 
 					for (int i = 5; i < splitMessage.length; i++) {
 						String targetNick = splitMessage[i].replace(":", "");
 						if (targetNick.substring(0, 1).equals("@")) {
 							ops.add(targetNick.substring(1));
 						}
 					}
 					channelOps.put(splitMessage[4], ops);
 				}
 				
 				if (splitMessage[1].equals("KICK") && splitMessage[3].equals(nick)) {
 					joinedChannels.remove(splitMessage[2]);
 				} else if (splitMessage[1].equals("PRIVMSG")) {
 					if (isOp(hostToNick(splitMessage[0]), splitMessage[2])) {
 						if (splitMessage[3].equals(":.print")) {
 							String buffer = "";
 							for (int i = 4; i < splitMessage.length; i++) {
 								buffer += splitMessage[i] + " ";
 							}
 							buffer = buffer.trim();
 							write("PRIVMSG " + splitMessage[2] + " :" + buffer);
						} else if (splitMessage[3].equals(".raw")) {
 							String buffer = "";
 							for (int i = 4; i < splitMessage.length; i++) {
 								buffer += splitMessage[i] + " ";
 							}
 							buffer = buffer.trim();
 							write(buffer);
 						}
 					}
 				}
 			}
 		} catch (UnknownHostException e) {
 			System.out.println("[ERR] Can't connect to host " + host + ":" + port);
 			return;
 		} catch (IOException e) {
 			System.out.println("[ERR] IO Exception " + e + " occured");
 			return;
 		}
 	}
 	
 	public static void write(String line) {
 		ircWriter.print(line + "\r\n");
 		ircWriter.flush();
 		System.out.println("[OUT] " + line);
 	}
 	
 	public static String read() throws IOException {
 		String line = "";
 		line = ircReader.readLine();
 		System.out.println(" [IN] " + line);
 		return line;
 	}
 	
 	public static boolean isOp(String nick, String channel) {
 		if (!channelOps.containsKey(channel))
 			return false;
 		List<String> ops = channelOps.get(channel);
 		if (!ops.contains(nick))
 			return false;
 		else
 			return true;
 	}
 	
 	public static String hostToNick(String host) {
 		host = host.replaceFirst(":", "");
 		if (!host.contains("!"))
 			return null;
 		String nick = host.split("!")[0];
 		return nick;
 	}
 }
