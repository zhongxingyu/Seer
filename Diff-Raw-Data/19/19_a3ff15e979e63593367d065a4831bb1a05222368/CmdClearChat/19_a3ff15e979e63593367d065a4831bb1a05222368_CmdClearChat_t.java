 package com.schautdollar.DonorDreams.Commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.schautdollar.DonorDreams.DonorDreams;
 
 public class CmdClearChat implements ICommand {
 
 	final String COMMAND = "clearchat";
 	final String PERMISSION_NODE = "donordreams.clearchat";
 	
 	public String getCommand() {
 		return this.COMMAND;
 	}
 
 	public String getPermissionNode() {
 		return this.PERMISSION_NODE;
 	}
 	public boolean runCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
 		
 		if(cmd.getName().equalsIgnoreCase(this.COMMAND)) {
 			if(sender instanceof Player) {
 				Player player = (Player) sender;
 				if(DonorDreams.permManager.playerHasPerm(player, this.PERMISSION_NODE)) {
					for(int i = 0; i < 100; i++)// Tested up to 300, Chat limit is 100 lines.
						player.sendMessage(" ");	
 					return true;
 				}
 				else{
 					player.sendMessage(ChatColor.RED + "You do not have permission to have your chat automatically cleared.");
 					return true;	
 				}
 			}
 			else{
 				//implement TARGETED PLAYER
 				DonorDreams.logMessage(ChatColor.RED + "Silly console. The command \"" + this.getCommand() + "\" can only be used by a player.");
 				return true;
 			}				
 		}
 		else{
 			return false;
 		}		
 	}
 }
