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
 					sender.sendMessage(MinecraftColors.red + "Player" + args[0] + " not found!");
 				}else{
 					if((Bukkit.getPlayer(args[1]) == null || Bukkit.getPlayer(args[1]).equals(null))){
 						sender.sendMessage(MinecraftColors.red + "Player " + args[1] + " not found!");
 					}else{
 						Bukkit.getPlayer(args[0]).teleport(Bukkit.getPlayer(args[1]));
 					}
 				}
 				return true;
 			}else if(args.length > 2){
 				sender.sendMessage("Invalid command syntax!");
 				return false;
 			}else if(args.length < 2){
 				sender.sendMessage("You must be a player to do that!");
 				return false;
 			}
 		}else{
 			if(args.length == 1){
 				if(args[0].equalsIgnoreCase("a") || args[0].equalsIgnoreCase("accept")){
 					Player target = Bukkit.getPlayer(sender.getName());
 					if(pendingTeleports.containsKey(target)){
 						Player originalSender = pendingTeleports.get(target);
 						if(originalSender == null || originalSender.equals(null)){
 							target.sendMessage(target.getName() + MinecraftColors.red + " is not currently online!");
 							target.sendMessage(MinecraftColors.red + "Request failed!");
 						}else{
 							originalSender.sendMessage(MinecraftColors.green + "Teleport request accepted!");
 							originalSender.sendMessage(MinecraftColors.green + "Teleporting...");
 							originalSender.teleport(target);
 						}
 						pendingTeleports.remove(target);
 					}else{
 						sender.sendMessage(MinecraftColors.red + "You have no pending teleports!");
 					}
 					return true;
 				}else if(args[0].equalsIgnoreCase("d") || args[0].equalsIgnoreCase("decline")){
 					Player target = Bukkit.getPlayer(sender.getName());
 					if(pendingTeleports.containsKey(target)){
 						Player originalSender = pendingTeleports.get(target);
 						if(originalSender == null || originalSender.equals(null)){
 						}else{
 							originalSender.sendMessage(MinecraftColors.red + "Your teleport request has been denied.");		
 						}
 						pendingTeleports.remove(target);
 					}else{
 						sender.sendMessage(MinecraftColors.red + "You have no pending teleports!");
 					}
 					return true;
 				}else if(args[0].equalsIgnoreCase("setrequest")){
 					if(sender.isOp() || sender.hasPermission("basics.tpr.setrequest")){
 						basics.getConfig().set("teleport-requests", Boolean.parseBoolean(args[1]));
 					}else{
 						sender.sendMessage("You must be OP/ Admin to do that!");
 					}
 					return true;
 				}else{
 					Player target = Bukkit.getPlayer(args[0]);
 					if(target == null || target.equals(null)){
 						sender.sendMessage(MinecraftColors.red + "Player " + args[0] + " not found!");
 					}else{
 						if(this.requests){
 							if(pendingTeleports.containsKey(target)){
 								pendingTeleports.remove(target);
 							}
 							pendingTeleports.put(target, Bukkit.getPlayer(sender.getName()));
							sender.sendMessage("Teleport request sent to " + target.getName());
 							target.sendMessage(MinecraftColors.red + sender.getName() + " would like to teleport to you.");
 							target.sendMessage(MinecraftColors.red + "Type /tpr a or /tpr d, to accept or decline, respectfully.");
 						}else{
 							sender.sendMessage("Teleporting...");
 							Bukkit.getPlayer(sender.getName()).teleport(target);
 						}
 					}
 					return true;
 				}
 			}else if(args.length == 2){
 				if(Bukkit.getPlayer(args[0]) == null || Bukkit.getPlayer(args[0]).equals(null)){
 					sender.sendMessage(MinecraftColors.red + "Player" + args[0] + " not found!");
 				}else{
 					if((Bukkit.getPlayer(args[1]) == null || Bukkit.getPlayer(args[1]).equals(null))){
 						sender.sendMessage(MinecraftColors.red + "Player " + args[1] + " not found!");
 					}else{
 						Player target = Bukkit.getPlayer(args[1]);
 						Player teleportee = Bukkit.getPlayer(args[0]);
 						if(this.requests){
 							if(pendingTeleports.containsKey(target)){
 								pendingTeleports.remove(target);
 							}
 							pendingTeleports.put(target, teleportee);
 							sender.sendMessage("Teleport request send to " + target.getName() + " for " + args[0]);
 							teleportee.sendMessage("Teleport request send to " + target.getName() + " for you by " + sender.getName());
 							target.sendMessage(MinecraftColors.red + sender.getName() + " would like to teleport to you.");
 							target.sendMessage(MinecraftColors.red + "Type /tpr a or /tpr d, to accept or decline, respectfully.");
 						}else{
 							sender.sendMessage("Teleporting " + args[0] + "...");
 							teleportee.sendMessage("Teleporting to " + args[1] + " for " + sender.getName() + "...");
 							Bukkit.getPlayer(sender.getName()).teleport(target);
 						}
 					}
 				}
 				return true;
 			}else if(args.length < 1){
 				sender.sendMessage("Invalid command syntax!");
 				return false;
 			}else if(args.length > 2){
 				sender.sendMessage("Invalid command syntax!");
 				return false;
 			}
 		}
 		return false;
 	}
 
 }
