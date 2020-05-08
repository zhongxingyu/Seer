 package com.araeosia.Chat.utils;
 
 import com.araeosia.Chat.AraeosiaChat;
 import com.araeosia.Chat.AraeosiaChat.MsgType;
 import java.util.ArrayList;
 import org.bukkit.Bukkit;
 import java.util.Calendar;
 
 public enum Style {
 	A,
 	B,
 	C,
 	D,
 	E,
 	F;
 	@Override
 	public String toString(){
 		return "Style "+super.toString();
 	}
 	
 	public String techString(){
 		return super.toString();
 	}
 	
 	public void handleMessage(String input, Chatter chatter){
 		String[] args = input.split("§");
 		AraeosiaChat.MsgType type = AraeosiaChat.MsgType.valueOf(args[0]);
 		String playerName = args[1].replace("∞", "§");
 		switch(type){
 			case MESSAGE:
 			case EMOTE:
				handleMessage(playerName, chatter.getCurrentChannel(), args[4], args[2], type, chatter);
 				break;
 			case JOIN:
 			case LEAVE:
				handleMessage(playerName, null, args[3], args[2], type, chatter);
 				break;
 		}
 	}
	public void handleMessage(String playerName, Channel chan, String message, String world, MsgType type, Chatter chatter){
 		AraeosiaChat plugin = (AraeosiaChat) Bukkit.getServer().getPluginManager().getPlugin("AraeosiaChat");
 		String output = "";
 		Calendar cal = Calendar.getInstance();
 		String timestamp = cal.get(Calendar.HOUR)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND);
 		switch (type) {
 			case MESSAGE:
 				switch(this){
 					case A:
 						output = "§" + chan.getPrefix() + "[" + chan.getAbbreviation() + "]§f " + playerName + "§f: " + message;
 						break;
 					case B:
 						output = "§" + chan.getPrefix() + "[" + chan.getAbbreviation() + "] §f[§9"+world+"§f] " + playerName + "§f: " + message;
 						break;
 					case C:
 						output = "§8("+playerName+"§8 to §"+chan.getPrefix()+chan.getName()+"§8)§f: "+message;
 						break;
 					case D:
 						output = "§d"+timestamp+" §" + chan.getPrefix() + "[" + chan.getAbbreviation() + "]§f " + playerName + "§f: " + message;
 						break;
 					case E:
 						output = "§d"+timestamp+" §" + chan.getPrefix() + "[" + chan.getAbbreviation() + "] §f[§9"+world+"§f] " + playerName + "§f: " + message;
 						break;
 					case F:
 						output = "§d"+timestamp+" §8("+playerName+"§8 to §"+chan.getPrefix()+chan.getName()+"§8)§f: "+message;
 						break;
 				}
 				break;
 			case EMOTE:
 				switch(this){
 					case A:
 						output = "§" + chan.getPrefix() + "[" + chan.getAbbreviation() + "]§f *" + playerName + "§f: " + message;
 						break;
 					case B:
 						output = "§" + chan.getPrefix() + "[" + chan.getAbbreviation() + "] §f[§9"+world+"§f] *" + playerName + "§f: " + message;
 						break;
 					case C:
 						output = "§f*§8("+playerName+"§8 in §"+chan.getPrefix()+chan.getName()+"§8)§f "+message;
 						break;
 					case D:
 						output = "§d"+timestamp+" §" + chan.getPrefix() + "[" + chan.getAbbreviation() + "]§f *" + playerName + "§f: " + message;
 						break;
 					case E:
 						output = "§d"+timestamp+" §" + chan.getPrefix() + "[" + chan.getAbbreviation() + "] §f[§9"+world+"§f] *" + playerName + "§f: " + message;
 						break;
 					case F:
 						output = "§d"+timestamp+" §f* §8("+playerName+"§8 to §"+chan.getPrefix()+chan.getName()+"§8)§f: "+message;
 						break;
 				}
 				break;
 			case JOIN:
 				output = "§e" + playerName + " joined " + world + " on " + message;
 				if(plugin.allChatters.get(message)==null){
 					plugin.allChatters.put(message, new ArrayList<String>());
 				}
 				if(!plugin.allChatters.get(message).contains(playerName)){
 					plugin.allChatters.get(message).add(playerName);
 				}
 				break;
 			case LEAVE:
 				output = "§e" + playerName + " left " + world + " on " + message;
 				if(plugin.allChatters.get(message)==null){
 					plugin.allChatters.put(message, new ArrayList<String>());
 				}
 				if(plugin.allChatters.get(message).contains(playerName)){
 					plugin.allChatters.get(message).remove(playerName);
 				}
 				break;
 		}
 		chatter.getPlayer().sendMessage(output);
 	}
 }
