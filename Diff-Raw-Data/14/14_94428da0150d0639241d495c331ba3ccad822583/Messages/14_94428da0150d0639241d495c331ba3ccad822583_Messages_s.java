 package me.rosswalker.serverstats;
 
 import org.bukkit.ChatColor;
 
 /* @name Messages
  * @author DicaxDorcas
  * @description Class to handle getters for player messages.
  */
 
 
 public class Messages {
 
 	ServerStats plugin;
 	
 	/* @name getHeader()
 	 * @description Returns a string containing the plugin header.
 	 */
 	String getHeader(){
 		return ChatColor.AQUA +"[" + plugin.getName() + "]" + ChatColor.WHITE;
 	}
 	
 	/* @name getHeader()
 	 * @param1 integer
 	 * @description Returns a string containing the plugin header.
 	 */
 	String getError(int number){
 	    return "";
 	}
 }
