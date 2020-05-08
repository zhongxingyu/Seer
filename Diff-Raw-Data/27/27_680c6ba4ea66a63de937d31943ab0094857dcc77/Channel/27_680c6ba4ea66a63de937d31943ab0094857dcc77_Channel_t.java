 package me.corriekay.pppopp3.chat;
 
 import static me.corriekay.pppopp3.chat.ChatHandler.regex;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.HashSet;
 
 import me.corriekay.packets.server.ChatPacket;
 import me.corriekay.pppopp3.ponyville.Pony;
 import me.corriekay.pppopp3.ponyville.Ponyville;
 import me.corriekay.pppopp3.rpa.RemotePonyAdmin;
 import me.corriekay.pppopp3.utils.PonyLogger;
 import me.corriekay.pppopp3.utils.Utils;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class Channel{
 
 	protected final String name;
 	protected final String icon;
 	protected final String perm;
 	protected final String quick;
 	protected final ChatColor cc;
 	protected final ChatColor w = ChatColor.WHITE;
 
 	protected final HashSet<String> listeners = new HashSet<String>();
 	protected final HashSet<String> chatters = new HashSet<String>();
 
 	public Channel(String channelName, String icon, String permission, ChatColor channelColor, String quick){
 		name = channelName;
 		this.icon = icon;
 		perm = permission;
 		cc = channelColor;
 		this.quick = quick;
 	}
 
 	public String broadcastToChannel(String who, String whoplayer, String message, boolean log){
 		message = parseForLinks(message);
 		if(message.startsWith(">")) {
 			message = ChatColor.GREEN + message;
 		}
 		String message2send = cc + icon + " " + w + who + w + ": " + cc + message;
 		for(String name : listeners) {
 			try {
 				if(ChatHandler.silenced.get(name).contains(whoplayer)) {
 					continue;
 				}
 			} catch(Exception e) {
 				continue;
 			}
 			Player player = Bukkit.getPlayerExact(name);
			if(player == null) {
				System.out.println("Chat desync - Player null: " + name);
				continue;
			}
 			Pony pony = Ponyville.getPony(player);
 			ChatColor cc2 = pony.getChannelColor(this.name);
 			String msg2sendfinal;
 			if(cc2 != null) {
 				msg2sendfinal = cc2 + icon + " " + w + who + w + ": " + cc2 + message;
 			} else {
 				msg2sendfinal = message2send;
 			}
			player.sendMessage(msg2sendfinal);
 		}
 		Bukkit.getConsoleSender().sendMessage(message2send);
 		if(log) {
 			PonyLogger.logMessage("ChatLogs" + File.separator + Utils.getFileDate(System.currentTimeMillis()), this.name, "[" + Utils.getTimeStamp(System.currentTimeMillis()) + "] [" + ChatColor.stripColor(who) + "]: " + message);
 		}
 		ChatPacket cp = new ChatPacket();
 		cp.channel = name;
 		cp.message = ChatColor.stripColor(message2send);
 		RemotePonyAdmin.rpa.sendChatPacket(cp);
 		return message2send;
 	}
 
 	public void broadcastRawMessage(String message, boolean log){
 		message = parseForLinks(message);
 		for(String name : listeners) {
 			Player player = Bukkit.getPlayerExact(name);
 			if(player != null) {
 				player.sendMessage(message);
 			}
 		}
 		if(log) {
 			PonyLogger.logMessage("ChatLogs" + File.separator + Utils.getFileDate(System.currentTimeMillis()), name + ".txt", "[" + Utils.getTimeStamp(System.currentTimeMillis()) + "]: " + message);
 		}
 		ChatPacket cp = new ChatPacket();
 		cp.channel = name;
 		cp.message = ChatColor.stripColor(message);
 		RemotePonyAdmin.rpa.sendChatPacket(cp);
 		Bukkit.getConsoleSender().sendMessage(message);
 	}
 
 	protected String permission(){
 		return perm;
 	}
 
 	protected void joinChatters(Player player, boolean notify){
 		chatters.add(player.getName());
 		if(notify) {
 			player.sendMessage(cc + "Now chatting in channel " + name + "!");
 		}
 	}
 
 	protected void joinListeners(Player player, boolean notify){
 
 		listeners.add(player.getName());
 		if(notify) {
 			player.sendMessage(cc + "Now listening to channel " + name + "!");
 		}
 	}
 
 	protected void leaveListeners(Player player, boolean notify){
 		listeners.remove(player.getName());
 		if(notify) {
 			player.sendMessage(cc + "No longer listening to channel " + name + "!");
 		}
 	}
 
 	protected void leaveChatters(Player player, boolean notify){
 		chatters.remove(player.getName());
 		if(notify) {
 			player.sendMessage(cc + "No longer chatting in channel " + name + "!");
 		}
 	}
 
 	protected boolean isListening(Player player){
 		return listeners.contains(player.getName());
 	}
 
 	protected boolean isChatting(Player player){
 		return chatters.contains(player.getName());
 	}
 
 	protected String getName(){
 		return name;
 	}
 
 	protected HashSet<String> getChatters(){
 		return chatters;
 	}
 
 	protected HashSet<String> getListeners(){
 		return listeners;
 	}
 
 	protected String getQuick(){
 		return quick;
 	}
 
 	protected ChatColor color(){
 		return cc;
 	}
 
 	protected String parseForLinks(String msg){
 		String msg2 = "";
 		String[] msgArray = msg.split(" ");
 		for(String s : msgArray) {
 			if(s.matches(regex) || s.startsWith("www.")) {
 				String site = "http://mlpf.im/yourls-api.php?signature=2a0114bbbb&action=shorturl&format=simple&url=" + s;
 				URL fURL;
 				try {
 					fURL = new URL(site);
 					BufferedReader in = new BufferedReader(new InputStreamReader(fURL.openStream()));
 					s = in.readLine();
 					in.close();
 				} catch(Exception e) {}
 			}
 			msg2 += s + " ";
 		}
 		return msg2;
 	}
 }
