 package com.schautdollar.DonorDreams.Commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.schautdollar.DonorDreams.DonorDreams;
 
 /**
  * @author Ryan
  *
  */
 public class CmdFeed implements ICommand {
 
 	final String COMMAND = "feed";
 	final String PERMISSION_NODE = "donordreams.feed";
 	final String PERMISSION_NODE_OTHERS = "donordreams.feed.others";
 	
 	@Override
 	public String getCommand() {
 		return this.COMMAND;
 	}
 	public String getPermissionNode(){
 		return this.PERMISSION_NODE;
 	}
 	public String getPermissionNodeOthers(){
 		return this.PERMISSION_NODE_OTHERS;
 	}
 
 	@Override
 	public boolean runCommand(CommandSender sender, Command cmd, String lbl,String[] args) {
		if(cmd.getName().equalsIgnoreCase(COMMAND)) {
 			if(sender instanceof Player) {
 				Player player = (Player) sender;
 				if(DonorDreams.permManager.playerHasPerm(player, this.PERMISSION_NODE)){
 					if(args.length == 0){
 						player.setFoodLevel(20);
 						player.sendMessage(ChatColor.GRAY + "Fed!");
 						return true;
 					
 					}else if(args.length == 1 && DonorDreams.permManager.playerHasPerm(player, PERMISSION_NODE_OTHERS)){
 						Player targetPlayer = player.getServer().getPlayer(args[0]);
 						targetPlayer.setFoodLevel(20);
 						targetPlayer.sendMessage(ChatColor.GRAY + "You've been fed!");
 						return true;
 					}else{
 						player.sendMessage(ChatColor.RED + "You do not have permission to feed others.");
 						return true;
 					}
 				}
 				else{
 					player.sendMessage(ChatColor.RED + "You do not have permission to feed yourself.");
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
