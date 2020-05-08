 package coolawesomeme.basics_plugin.commands;
 
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import coolawesomeme.basics_plugin.Basics;
 import coolawesomeme.basics_plugin.CommandErrorMessages;
 
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
 					sender.sendMessage(ChatColor.RED + "Player " + args[0] + " not found!");
 				}else{
 					if((Bukkit.getPlayer(args[1]) == null || Bukkit.getPlayer(args[1]).equals(null))){
 						sender.sendMessage(ChatColor.RED + "Player " + args[1] + " not found!");
 					}else{
 						Bukkit.getPlayer(args[0]).teleport(Bukkit.getPlayer(args[1]));
 					}
 				}
 				return true;
 			}else if(args.length > 2){
 				return CommandErrorMessages.sendSyntaxError(sender);
 			}else if(args.length < 2){
 				return CommandErrorMessages.sendConsoleError(sender);
 			}
 		}else{
 			if(args.length == 1){
 				if(pendingTeleports.containsKey(sender.getName())){
 					if(args[0].equalsIgnoreCase("a") || args[0].equalsIgnoreCase("accept")){
 						Player target = Bukkit.getPlayer(sender.getName());
 						if(pendingTeleports.containsKey(target)){
 							Player originalSender = pendingTeleports.get(target);
 							if(originalSender == null || originalSender.equals(null)){
 								target.sendMessage(target.getName() + ChatColor.RED + " is not currently online!");
 								target.sendMessage(ChatColor.RED + "Request failed!");
 							}else{
 								originalSender.sendMessage(ChatColor.GREEN + "Teleport request accepted!");
 								target.sendMessage("Accepting request...");
 								originalSender.sendMessage(ChatColor.GREEN + "Teleporting...");
 								target.sendMessage("Teleporting...");
 								originalSender.teleport(target);
 							}
 							pendingTeleports.remove(target);
 						}else{
 							sender.sendMessage(ChatColor.RED + "You have no pending teleports!");
 						}
 						return true;
 					}else if(args[0].equalsIgnoreCase("d") || args[0].equalsIgnoreCase("decline")){
 						Player target = Bukkit.getPlayer(sender.getName());
 						if(pendingTeleports.containsKey(target)){
 							Player originalSender = pendingTeleports.get(target);
 							if(originalSender == null || originalSender.equals(null)){
 							}else{
 								originalSender.sendMessage(ChatColor.RED + "Your teleport request has been denied.");
 							}
 							target.sendMessage("Denying request...");
 							pendingTeleports.remove(target);
 						}else{
 							sender.sendMessage(ChatColor.RED + "You have no pending teleports!");
 						}
 						return true;
 					}else if(args[0].equalsIgnoreCase("setrequest")){
 						if(sender.isOp() || sender.hasPermission("basics.tpr.setrequest") || !(sender instanceof Player)){
							basics.getConfig().set("teleport-requests", Boolean.parseBoolean(args[1]));
 							basics.saveConfig();
 							sender.sendMessage("Value set!");
 						}else{
 							CommandErrorMessages.sendPermissionError(sender);
 						}
 						return true;
 					}
 				}else{
 					Player target = Bukkit.getPlayer(args[0]);
 					if(target == null || target.equals(null)){
 						sender.sendMessage(ChatColor.RED + "Player " + args[0] + " not found!");
 					}else{
 						if(this.requests){
 							if(pendingTeleports.containsKey(target)){
 								pendingTeleports.remove(target);
 							}
 							pendingTeleports.put(target, Bukkit.getPlayer(sender.getName()));
 							sender.sendMessage("Teleport request sent to " + target.getName());
 							target.sendMessage(ChatColor.RED + sender.getName() + " would like to teleport to you.");
 							target.sendMessage(ChatColor.RED + "Type /tpr a or /tpr d, to accept or decline, respectfully.");
 						}else{
 							sender.sendMessage("Teleporting...");
 							Bukkit.getPlayer(sender.getName()).teleport(target);
 						}
 					}
 					return true;
 				}
 			}else if(args.length >= 2){
				if(args.length == 2 && Bukkit.getPlayer(args[1]) != null){
 					if(Bukkit.getPlayer(args[0]) == null || Bukkit.getPlayer(args[0]).equals(null)){
 						sender.sendMessage(ChatColor.RED + "Player " + args[0] + " not found!");
 					}else{
 						if((Bukkit.getPlayer(args[1]) == null || Bukkit.getPlayer(args[1]).equals(null))){
 							sender.sendMessage(ChatColor.RED + "Player " + args[1] + " not found!");
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
 								target.sendMessage(ChatColor.RED + sender.getName() + " would like to teleport to you.");
 								target.sendMessage(ChatColor.RED + "Type /tpr a or /tpr d, to accept or decline, respectfully.");
 							}else{
 								sender.sendMessage("Teleporting " + args[0] + "...");
 								teleportee.sendMessage("Teleporting to " + args[1] + " for " + sender.getName() + "...");
 								Bukkit.getPlayer(sender.getName()).teleport(target);
 							}
 						}
 					}
 					return true;
 				}else if(args.length >= 2 && (args[0].equalsIgnoreCase("d") || args[0].equalsIgnoreCase("decline"))){
 					if(pendingTeleports.containsKey(sender.getName())){
 						if(args[0].equalsIgnoreCase("d") || args[0].equalsIgnoreCase("decline")){
 							Player target = Bukkit.getPlayer(sender.getName());
 							if(pendingTeleports.containsKey(target)){
 								Player originalSender = pendingTeleports.get(target);
 								if(originalSender == null || originalSender.equals(null)){
 								}else{
 									String s = "";
 									for(int i = 0; i < args.length; i++){
 										if(i != 0){
 											s = s + args[i];
 										}
 									}
 									originalSender.sendMessage(ChatColor.RED + "Your teleport request has been denied.");
 									originalSender.sendMessage(ChatColor.RED + "Reason: " + s);
 								}
 								target.sendMessage("Denying request...");
 								pendingTeleports.remove(target);
 							}else{
 								sender.sendMessage(ChatColor.RED + "You have no pending teleports!");
 							}
							return true;
 						}
 					}
 				}
 			}else if(args.length < 1){
 				return CommandErrorMessages.sendSyntaxError(sender);
 			}else if(args.length > 2){
 				return CommandErrorMessages.sendSyntaxError(sender);
 			}
 		}
 		return false;
 	}
 
 }
