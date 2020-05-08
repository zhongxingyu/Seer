 package me.corriekay.pppopp3.chat;
 
 import java.io.File;
 
 import me.corriekay.packets.server.ChatPacket;
 import me.corriekay.pppopp3.modules.Equestria;
 import me.corriekay.pppopp3.ponyville.Pony;
 import me.corriekay.pppopp3.ponyville.Ponyville;
 import me.corriekay.pppopp3.rpa.RemotePonyAdmin;
 import me.corriekay.pppopp3.utils.PonyLogger;
 import me.corriekay.pppopp3.utils.Utils;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 public class WorldChannel extends Channel{
 
 	public WorldChannel(String channelName, String icon, String permission, ChatColor channelColor, String quick){
 		super(channelName, icon, permission, channelColor, quick);
 	}
 
 	public String broadcastToChannel(String who, String whoplayer, String message, boolean log){
 		message = parseForLinks(message);
 		if(message.startsWith(">")) {
 			message = ChatColor.GREEN + message;
 		}
 		String message2send = cc + icon + " " + w + who + w + ": " + cc + message;
 		World senderWorld = Equestria.get().getParentWorld(Bukkit.getPlayerExact(whoplayer).getWorld());
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
			World recieverworld = Equestria.get().getParentWorld(player.getWorld());
			if(recieverworld.getName().equals(senderWorld.getName()) || player.hasPermission("pppopp3.chat.staff")) {
				player.sendMessage(msg2sendfinal);
 			}
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
 }
