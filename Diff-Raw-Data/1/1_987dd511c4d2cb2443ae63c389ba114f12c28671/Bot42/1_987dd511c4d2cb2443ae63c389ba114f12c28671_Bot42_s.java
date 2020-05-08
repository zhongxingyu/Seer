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
 	private String nick = "Bot42";
 	private String host = "irc.kottnet.net";
 	private int port = 6667;
 	private Socket ircSocket;
 	
 	private PrintWriter ircWriter = null;
 	private BufferedReader ircReader = null;
 	
 	private List<String> joinedChannels = new LinkedList<String>();
 	private List<String> globalOps = new LinkedList<String>();
 	private HashMap<String, List<String>> channelOps = new HashMap<String, List<String>>();
 	
 	private static Bot42 bot42 = new Bot42();
 	
 	public static void main(String[] args) {
 		System.out.println("Bot42 IRC Bot by Vijfhoek and F16Gaming.");
 		System.out.println("TODO: Add copyright information if we're going to use a license.");
 		System.out.println("TODO: Add more TODO statements.");
 		System.out.println();
 		
 		boolean connected = false;
 		
 		
 		// TODO Add a config file for this
 		bot42.globalOps.add("Vijfhoek");
 		bot42.globalOps.add("F16Gaming");
 		
 		try {
 			bot42.ircSocket = new Socket(bot42.host, bot42.port);
 			bot42.ircWriter = new PrintWriter(bot42.ircSocket.getOutputStream());
 			bot42.ircReader = new BufferedReader(new InputStreamReader(bot42.ircSocket.getInputStream()));
 			
 			bot42.write("NICK " + bot42.nick);
 			bot42.write("USER " + bot42.nick.toLowerCase() + " 0 * :" + bot42.nick);
 			
 			while (true) {
 				String message = bot42.read();
 				String[] splitMessage = message.split(" ");
 				
 				if (splitMessage[0].equals("PING")) {
 					bot42.write("PONG " + splitMessage[1]);
 				}
 				if (!connected) {
 					if (splitMessage[1].equals("376")) {
 						connected = true;
 						bot42.write("JOIN #Bot42");
 					} else if (splitMessage[1].equals("433")) {
 						bot42.nick += "|2";
 						bot42.write("NICK " + bot42.nick);
 					}
 				}
 				
 				if (splitMessage[1].equals("366")) {
 					bot42.joinedChannels.add(splitMessage[3]);
 				} else if (splitMessage[1].equals("353")) {
 					List<String> ops = new LinkedList<String>(); 
 					for (int i = 5; i < splitMessage.length; i++) {
 						String targetNick = splitMessage[i].replace(":", "");
 						if (targetNick.substring(0, 1).equals("@")) {
 							ops.add(targetNick.substring(1));
 						}
 					}
 					bot42.channelOps.put(splitMessage[4], ops);
 				}
 				
 				if (splitMessage[1].equals("KICK") && splitMessage[3].equals(bot42.nick)) {
 					bot42.joinedChannels.remove(splitMessage[2]);
 				} else if (splitMessage[1].equals("PRIVMSG")) {
 					if (bot42.isOp(bot42.hostToNick(splitMessage[0]), splitMessage[2])) {
 						if (splitMessage[3].equals(":.print")) {
 							String buffer = "";
 							for (int i = 4; i < splitMessage.length; i++) {
 								buffer += splitMessage[i] + " ";
 							}
 							buffer = buffer.trim();
 							bot42.write("PRIVMSG " + splitMessage[2] + " :" + buffer);
 						} else if (splitMessage[3].equals(":.raw")) {
 							String buffer = "";
 							for (int i = 4; i < splitMessage.length; i++) {
 								buffer += splitMessage[i] + " ";
 							}
 							buffer = buffer.trim();
 							bot42.write(buffer);
 						} else if (splitMessage[3].equals(":.kick")) {
 							String channel;
 							String target;
 							
 							if (bot42.isChannel(splitMessage[2])) {
 								channel = splitMessage[2];
 								target = splitMessage[4];
 							} else {
 								channel = splitMessage[4];
 								target = splitMessage[5];
 							}
 							
 							bot42.write("KICK " + channel + " " + target + " :(" + bot42.hostToNick(splitMessage[0]) + ")");
 						} else if (splitMessage[3].equals(":.quit")) {
 							String buffer = "";
 							if (splitMessage.length > 4) {
 								buffer = " :";
 								for (int i = 4; i < splitMessage.length; i++) {
 									buffer += splitMessage[i];
 								}
 							}
							buffer = buffer.trim();
 							
 							bot42.write("QUIT" + buffer);
 						}
 					}
 				}
 			}
 		} catch (UnknownHostException e) {
 			System.out.println("[ERR] Can't connect to host " + bot42.host + ":" + bot42.port);
 			return;
 		} catch (IOException e) {
 			System.out.println("[ERR] IO Exception " + e + " occured");
 			return;
 		}
 	}
 	
 	public void write(String line) {
 		ircWriter.print(line + "\r\n");
 		ircWriter.flush();
 		System.out.println("[OUT] " + line);
 	}
 	
 	public String read() throws IOException {
 		String line = "";
 		line = ircReader.readLine();
 		System.out.println(" [IN] " + line);
 		return line;
 	}
 	
 	public boolean isOp(String nick, String channel) {
 		if (channel.startsWith("#"))
 		{
 			if (!channelOps.containsKey(channel))
 				return false;
 			List<String> ops = channelOps.get(channel);
 			if (!ops.contains(nick))
 				return false;
 			else
 				return true;
 		} else if (channel.equals(bot42.nick)) {
 			if (!globalOps.contains(nick))
 				return false;
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean isChannel(String target) {
 		return target.startsWith("#");
 	}
 	
 	public String hostToNick(String host) {
 		host = host.replaceFirst(":", "");
 		if (!host.contains("!"))
 			return null;
 		String nick = host.split("!")[0];
 		return nick;
 	}
 }
