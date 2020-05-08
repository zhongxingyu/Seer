 /**
  * 
  */
 package org.morganm.loginlimiter;
 
 import java.util.HashSet;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 
 /** Class for keeping track of "on duty" status for moderators.
  * 
  * @author morganm
  *
  */
 public class OnDuty {
 	public HashSet<String> offDutyList = new HashSet<String>(3);
 	
 	public OnDuty(final LoginLimiter plugin) {
 	}
 	
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		if( "onduty".equals(command.getName()) ) {
 			offDutyList.remove(sender.getName());
 			sender.sendMessage(ChatColor.YELLOW+"You are now ON duty.");
 			
 		}
 		else if( "offduty".equals(command.getName()) ) {
 			offDutyList.add(sender.getName());
 			sender.sendMessage(ChatColor.YELLOW+"You are now OFF duty.");
 		}
 		
 		return true;
 	}
 	
 	public boolean isOffDuty(String playerName) {
 		return offDutyList.contains(playerName);
 	}
 }
