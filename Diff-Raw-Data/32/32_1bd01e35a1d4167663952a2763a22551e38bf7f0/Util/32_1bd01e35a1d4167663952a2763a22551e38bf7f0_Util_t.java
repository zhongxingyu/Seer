 /*
     MazeMania; a minecraft bukkit plugin for managing a maze as an arena.
     Copyright (C) 2012 Plugmania (Sorroko,korikisulda) and contributors.
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package info.plugmania.mazemania;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.PluginDescriptionFile;
 
 public class Util {
 
 	static MazeMania plugin;
 
 	public static Logger log = Logger.getLogger("Minecraft");
 	public static PluginDescriptionFile pdfFile;
 
 	public Util(MazeMania instance) {
 		plugin = instance;
 	}
 
 	public static String formatBroadcast(String msg) {
 		String s = ChatColor.BLUE.toString() + "[MazeMania] " + msg;
 		return s;
 	}
 
 	public static String formatMessage(String msg) {
 		String s = ChatColor.DARK_PURPLE.toString() + ChatColor.ITALIC.toString() + msg;
 		return s;
 	}
 
 	public static void log(String msg) {
 		log.info("[" + pdfFile.getName() + "] " + msg);
 	}
 
 	public static void debug(String msg) {
 		if (plugin.debug) {
 			log.info("[" + pdfFile.getName() + "] [DEBUG]: " + msg);
 		}
 	}
 
 	public static void sendMessageNotPlayer(CommandSender sender) {
 		sender.sendMessage(ChatColor.RED + "You must be a player to do this!");
 	}
 
 	public static void sendMessageNoPerms(CommandSender sender) {
 		sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
 	}
 
 	public static void sendMessagePlayerNotOnline(CommandSender sender) {
 		sender.sendMessage(ChatColor.RED + "That player is not online!");
 	}
 	
 	public boolean hasPermMsg(CommandSender sender,String perm){
 		
 		if ((sender.hasPermission(plugin.basePerm + "." + perm)) || (sender.hasPermission(plugin.basePerm + ".*"))) {
 			Util.debug("Has permission for player: " + sender.getName() + " and perm: " + plugin.basePerm + "." + perm);
 			return true;
 		}
 		sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
 		return false;
 	}
 	
 	public String join(String[] a, String delimiter, Integer startIndex) {
 		try {
 			Collection<String> s = Arrays.asList(a);
 			StringBuffer buffer = new StringBuffer();
 			Iterator<String> iter = s.iterator();
 
 			while (iter.hasNext()) {
 				if (startIndex == 0) {
 					buffer.append(iter.next());
 					if (iter.hasNext()) {
 						buffer.append(delimiter);
 					}
 				} else {
 					startIndex--;
 					iter.next();
 				}
 			}
 
 			return buffer.toString();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return "";
 		}
 	}
 	
 	public HashMap<String,String> getFlags(String string){
 		HashMap<String,String> ret=new HashMap<String,String>();
 		boolean inQuotes=false;
 		boolean inFlag=true;
 		String currentflag="";
 		String flagvalue="";
 		for(char c:string.toCharArray()){
 			if(c=='\''){
 				inQuotes=!inQuotes;
 			}else if(c==' '&&!inQuotes){
 				ret.put(currentflag, flagvalue);
                 currentflag = "";
                 flagvalue = "";
 				inFlag=true;
 			}else if(c==':'&&!inQuotes){
 				inFlag=false;
 			}else if(inFlag){
 				currentflag+=c;
 			}else if(inQuotes){
 				flagvalue+=c;
 			}else if(!inFlag){
 				flagvalue+=c;
 			}
 			
 		}
 		ret.put(currentflag, flagvalue);
 		return ret;
 		}
 	
 	
 	public boolean compare(HashMap<String,String> event,HashMap<String,String> entry){
 		for(String s:entry.keySet()){
 			if(!event.containsKey(s)) return false;
 			if((event.get(s)!=entry.get(s))&&(entry.get(s)!="*")) return false;
 		}
 		return true;
 	}

	public String join(Object[] array, String delimiter, int startIndex) {
		try {
			Collection<Object> s = Arrays.asList(array);
			StringBuffer buffer = new StringBuffer();
			Iterator<Object> iter = s.iterator();

			while (iter.hasNext()) {
				if (startIndex == 0) {
					buffer.append(String.valueOf(iter.next()));
					if (iter.hasNext()) {
						buffer.append(String.valueOf(delimiter));
					}
				} else {
					startIndex--;
					iter.next();
				}
			}

			return buffer.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
 	
 }
