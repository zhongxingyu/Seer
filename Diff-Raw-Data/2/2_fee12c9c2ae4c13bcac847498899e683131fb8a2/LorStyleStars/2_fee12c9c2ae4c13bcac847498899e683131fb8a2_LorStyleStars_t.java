 package ru.ttyh.LorStyleStars;
 
 import org.bukkit.Bukkit;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 // TODO Permissions
 // TODO sqlite3
 // TODO Chat stars
 
 public class LorStyleStars extends JavaPlugin {
 	
 
 	@Override
 	public void onDisable() {
 		LorStyleStarsSystem.disable();
 	}
 	
 	public void onEnable() {
 		LorStyleStarsSystem.setup();
 		PluginManager pm = this.getServer().getPluginManager();
 
 		pm.registerEvent(Type.PLAYER_LOGIN, new LorStyleStarsPlayerListener(), Priority.Normal, this);
 
 	}
 
 	@Override 
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if (! (sender instanceof Player) ) {
 			return false;
 		}
 		Player p = (Player) sender;
 
 		if (command.getName().equalsIgnoreCase("score") ) {
 			switch (args.length) {
 			case 0:
 				p.sendMessage(ChatColor.YELLOW + "Your score is " +
 						LorStyleStarsSystem.getScore(p.getName()) );
 				return true;
 			case 1:
 				if (args[0].equalsIgnoreCase("stars") ) {
 					p.sendMessage(ChatColor.YELLOW + "Stars mark: " +
 							LorStyleStarsSystem.scoreToStars(p.getName()) );
 					return true;
 				} else if (args[0].equalsIgnoreCase("reload") ) {
 					LorStyleStarsSystem.reload();
 					p.sendMessage(ChatColor.YELLOW + "Config reloaded" );
 					return true;
 				} else  if (args[0].equalsIgnoreCase("save") ) {
 					LorStyleStarsSystem.save();
 					p.sendMessage(ChatColor.YELLOW + "Config saved" );
 					return true;
 				} else if (args[0].equalsIgnoreCase("help") ) {
 					p.sendMessage(ChatColor.YELLOW + "LorStyleScore commands:" );
 					p.sendMessage("/score - show your score" );
 					p.sendMessage("/score stars - show your stars" );
					p.sendMessage("/score help - show this page" );
 					if (p.isOp()) {
 						p.sendMessage(ChatColor.YELLOW+ "LorStyleScore ops commands:" );
 						p.sendMessage("/score save - force save config" );
 						p.sendMessage("/score reload - reload config (without save)" );
 						p.sendMessage("/score get <username> - get score of username" );
 						p.sendMessage("/score set <username> <count> - set score of username" );
 						p.sendMessage("/score add <username> <+/-><count> - add or remove score" );
 						
 					}
 					return true;
 				} else return false;
 			case 2:
 				if (args[0].equalsIgnoreCase("get")  ) {
 					if (p.getName() == args[1] || p.isOp())
 							p.sendMessage(ChatColor.YELLOW + args[1] + "'s score is " +
 							LorStyleStarsSystem.getScore(args[1]) );
 					return true;
 				} else return false;
 			case 3:
 				try {
 					Integer.parseInt(args[2]);
 				} catch (NumberFormatException e){
 					p.sendMessage(ChatColor.RED + "arg[2] is not a nuber!");
 					return true;
 				}
 				if (! p.isOp() ) {
 					p.sendMessage(ChatColor.RED + "You can't do it, you are not OP");
 					return true;
 				}
 				if ( args[0].equalsIgnoreCase("set") ) {
 					LorStyleStarsSystem.setScore(args[1], args[2]);
 					Bukkit.getServer().broadcastMessage(
 							ChatColor.GRAY + p.getName() + " sets " + 
 							args[1] + "'s score to " + args[2]);
 					return true;
 				} else if ( args[0].equalsIgnoreCase("add")) {
 					LorStyleStarsSystem.addScore(args[1], args[2]);
 					if ( Integer.parseInt(args[2]) > 0 )
 						Bukkit.getServer().broadcastMessage(
 								ChatColor.GREEN + p.getName() + " gives " + 
 								args[1] + " " + args[2] + " score" );
 					else
 						Bukkit.getServer().broadcastMessage(
 								ChatColor.RED + p.getName() + " takes from " + 
 								args[1] + " " + Math.abs(Integer.parseInt
 										(args[2])) + " score");
 					return true;
 				} else return false;
 			}
 		}
 		return false;
 	}
 }
