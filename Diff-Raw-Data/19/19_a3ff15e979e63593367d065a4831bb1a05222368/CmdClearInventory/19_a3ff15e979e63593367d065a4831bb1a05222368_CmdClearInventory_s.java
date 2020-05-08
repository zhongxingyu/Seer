 package com.schautdollar.DonorDreams.Commands;
 
 import org.bukkit.ChatColor;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.schautdollar.DonorDreams.DonorDreams;
 
 /*
  * Author: Burnett1
  * 
  */
 
 public class CmdClearInventory implements ICommand {
 
 	
 	final String COMMAND = "clearinv";
 	final String PERMISSION_NODE = "donordreams.clearinv";
 	final String PERMISSION_NODE_OTHERS = "donordreams.clearinv.others";
 	
 	
 	@Override
 	public String getCommand() {
 		return this.COMMAND;
 	}
 	
 	public String getPermissionNode(){
 		
 		return this.PERMISSION_NODE;
 	}
 
 	@Override
 	public boolean runCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		if(cmd.getName().equalsIgnoreCase(COMMAND)) {
 			if(sender instanceof Player) {
 				Player player = (Player) sender;
 				if(DonorDreams.permManager.playerHasPerm(player, this.PERMISSION_NODE)){
 					if(args.length == 0){
 						player.getInventory().clear();
 						player.sendMessage(ChatColor.GRAY + "Cleared!");
 						return true;
 					
 					}else if(args.length == 1 && DonorDreams.permManager.playerHasPerm(player, PERMISSION_NODE_OTHERS)){
 						Player targetPlayer = player.getServer().getPlayer(args[0]);
 						targetPlayer.getInventory().clear();
 						targetPlayer.sendMessage(ChatColor.GRAY + "Your inventory has been cleared!");
 						return true;
 					}else{
 						player.sendMessage(ChatColor.RED + "You do not have permission to clear others inventories.");
 						return true;
 					}
 				}
 				else{
 					player.sendMessage(ChatColor.RED + "You do not have permission to clear your inventory.");
 					return true;
 				}
 			}
 			else{
 				//implement TARGETED PLAYER
 				DonorDreams.logMessage(ChatColor.RED + "Silly console. The command \"" + this.getCommand() + "\" can only be used by a player.");
 				return true;
 			}
 		}
 		else
 			return false;
 	}
 }
