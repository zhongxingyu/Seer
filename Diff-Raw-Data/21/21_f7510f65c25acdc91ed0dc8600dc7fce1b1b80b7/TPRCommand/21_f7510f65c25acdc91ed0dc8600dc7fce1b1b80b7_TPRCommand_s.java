 package coolawesomeme.basics_plugin.commands;
 
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import coolawesomeme.basics_plugin.Basics;
 import coolawesomeme.basics_plugin.MinecraftColors;
 
 public class TPRCommand implements CommandExecutor{
 	
 	private Basics basics;
 	private HashMap<Player, Player> pendingTeleports = new HashMap<Player, Player>();
 	private boolean requests = Basics.teleportRequests;
 	
 	public TPRCommand(Basics instance){
 		basics = instance;
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if(!(sender instanceof Player)){
 			if(args.length == 2){
 				if(Bukkit.getPlayer(args[0]) == null || Bukkit.getPlayer(args[0]).equals(null)){
 					sender.sendMessage(MinecraftColors.lightRed + "Player" + args[0] + " not found!");
 				}else{
 					if((Bukkit.getPlayer(args[1]) == null || Bukkit.getPlayer(args[1]).equals(null))){
 						sender.sendMessage(MinecraftColors.lightRed + "Player " + args[1] + " not found!");
 					}else{
 						Bukkit.getPlayer(args[0]).teleport(Bukkit.getPlayer(args[1]));
 					}
 				}
 				return true;
 			}
 		}else{
 			if(args.length == 1){
				if(args[0].equals("a") || args[0].equals("accept")){
 					Player target = Bukkit.getPlayer(sender.getName());
 					if(pendingTeleports.containsKey(target)){
 						Player originalSender = pendingTeleports.get(target);
 						if(originalSender == null || originalSender.equals(null)){
 							target.sendMessage(target.getName() + MinecraftColors.lightRed + " is not currently online!");
 							target.sendMessage(MinecraftColors.lightRed + "Request failed!");
 						}else{
 							originalSender.sendMessage(MinecraftColors.lightRed + "Teleport request accepted!");
 							originalSender.sendMessage(MinecraftColors.lightRed + "Teleporting...");
 							originalSender.teleport(target);
 						}
 						pendingTeleports.remove(target);
 					}else{
 						sender.sendMessage(MinecraftColors.lightRed + "You have no pending teleports!");
 					}
 					return true;
				}else if(args[0].equals("d") || args[0].equals("decline")){
 					Player target = Bukkit.getPlayer(sender.getName());
 					if(pendingTeleports.containsKey(target)){
 						Player originalSender = pendingTeleports.get(target);
 						if(originalSender == null || originalSender.equals(null)){
 						}else{
 							originalSender.sendMessage(MinecraftColors.lightRed + "Your teleport request has been denied.");		
 						}
 						pendingTeleports.remove(target);
 					}else{
 						sender.sendMessage(MinecraftColors.lightRed + "You have no pending teleports!");
 					}
 					return true;
 				}else{
 					Player target = Bukkit.getPlayer(args[0]);
 					if(target == null || target.equals(null)){
 						sender.sendMessage(MinecraftColors.lightRed + "Player " + args[0] + " not found!");
 					}else{
 						if(this.requests){
 							if(pendingTeleports.containsKey(target)){
 								pendingTeleports.remove(target);
 							}
 							pendingTeleports.put(target, Bukkit.getPlayer(sender.getName()));
 							target.sendMessage(MinecraftColors.lightRed + sender.getName() + " would like to teleport to you.");
 							target.sendMessage(MinecraftColors.lightRed + "Type /tpr a or /tpr d, to accept or decline, respectfully.");
 						}else{
 							sender.sendMessage("Teleporting...");
 							Bukkit.getPlayer(sender.getName()).teleport(target);
 						}
 					}
 					return true;
 				}
			}else{
				if(sender.isOp()){
					basics.getConfig().set("teleport-requests", Boolean.parseBoolean(args[1]));
				}else{
					sender.sendMessage("You must be OP to do that!");
				}
				return true;
 			}
 		}
 		return false;
 	}
 
 }
