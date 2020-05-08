 package fr.noogotte.useful_commands.command;
 
import java.util.List;

 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.command.Command;
 import fr.aumgn.bukkitutils.command.CommandArgs;
 import fr.aumgn.bukkitutils.command.NestedCommands;
 import fr.aumgn.bukkitutils.command.exception.CommandUsageError;
 import fr.noogotte.useful_commands.exception.PlayerNotInServer;
 
 @NestedCommands(name = "useful")
 public class PlayerCommands extends UsefulCommands {
 
 	@Command(name = "gamemode", min = 0, max = 1)
 	public void gamemode(Player player, CommandArgs args) {
 		if(args.length() == 0) {
 			if(player.getGameMode() == GameMode.CREATIVE) {
 				player.setGameMode(GameMode.SURVIVAL);
 				player.sendMessage(ChatColor.GREEN + "Vous vous êtes mis en " + ChatColor.AQUA + player.getGameMode());
 			} else {
 				player.setGameMode(GameMode.CREATIVE);
 				player.sendMessage(ChatColor.GREEN + "Vous vous êtes mis en " + ChatColor.AQUA + player.getGameMode());
 			}
 		} else if (args.length() == 1) {
 			Player target = Bukkit.getPlayer(args.get(0));
 			if(target == null) {
 				throw new PlayerNotInServer();
 			} else {
 				if(target.getGameMode() == GameMode.CREATIVE) {
 					target.setGameMode(GameMode.SURVIVAL);
 					player.sendMessage(ChatColor.GREEN + "Vous vous avez mis en " + ChatColor.AQUA + "SURVIVAL" + 
 							ChatColor.GREEN + target.getName());
 				} else {
 					target.setGameMode(GameMode.CREATIVE);
 					player.sendMessage(ChatColor.GREEN + "Vous vous avez mis en " + ChatColor.AQUA + "CREATIVE " +
 							ChatColor.GREEN + target.getName());
 				}
 			}
 		} else {
 			throw new CommandUsageError("Argument " + args.get(0) + " inconnu.");
 		}
 	}
 	
 	@Command(name = "heal", min = 0, max = 1)
 	public void heal(Player player, CommandArgs args) {
 		if(args.length() == 0) {
 			player.setHealth(20);
 			player.setFoodLevel(20);
 			player.sendMessage("Vous voila soigné et nourris");
 		} else if (args.length() == 1) {
 			Player target = Bukkit.getPlayer(args.get(0));
 			if(target == null) {
 				throw new PlayerNotInServer();
 			} else {		
 					target.setHealth(20);	
 					target.setFoodLevel(20);		
 					player.sendMessage(ChatColor.GREEN + "Vous vous avez soignés et nourris" + ChatColor.BLUE + target.getName());
 					target.sendMessage(ChatColor.YELLOW + "Vous êtes soignés et nourris");
 			}
 		} else {
 			throw new CommandUsageError("Argument " + args.get(0) + " inconnu.");	
 		}
 	}
 	
 	@Command(name = "clear", min = 0, max = 1)
 	public void clear(Player player, CommandArgs args) {
 		if(args.length() == 0) {
 			for (int j = 0; j <= 39; j++) {
                 player.getInventory().setItem(j, null);
             }
 			player.sendMessage(ChatColor.YELLOW + "Clear !");
 		} else if (args.length() == 1) {
 			Player target = Bukkit.getPlayer(args.get(0));
 			if(target == null) {
 				throw new PlayerNotInServer();
 			} else {
 				for (int j = 0; j <= 39; j++) {
 	                target.getInventory().setItem(j, null);
 	            }		
 				player.sendMessage(ChatColor.GREEN + "Vous avez vidés l'inventaire de " + ChatColor.BLUE + target.getName());
 				target.sendMessage(ChatColor.YELLOW + "Inventaire vidé !");
 			}
 		} else {
 			
 		}
 	}
 }
