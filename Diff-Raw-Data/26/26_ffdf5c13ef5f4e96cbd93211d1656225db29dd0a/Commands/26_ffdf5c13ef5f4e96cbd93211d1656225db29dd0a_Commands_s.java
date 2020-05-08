 package tk.nekotech.war;
 
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 
 public class Commands implements CommandExecutor {
 	private War war;
 	
 	public Commands(War war) {
 		this.war = war;
 	}
 	
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		ChatColor a = ChatColor.AQUA;
 		if (label.equals("war")) {
 			sender.sendMessage(a + "Welcome to the war, " + sender.getName() + "!");
 			sender.sendMessage(a + "Repeatedly smack things with your sword!");
 			sender.sendMessage(a + "To join the game say /join and you will be teleported out of spectate.");
 		}
 		if (label.equals("join")) {
 			if (sender instanceof Player) {
 				Player p = (Player) sender;
 				if (!war.onTeam(p)) {
 					if (war.blu.size() == war.red.size()) {
 						Random rand = new Random();
 						int decider = rand.nextInt(2);
 						war.assignPlayer(p, decider);
 					} else if (war.blu.size() < war.red.size()) {
 						war.assignPlayer(p, 0);
 					} else if (war.red.size() < war.blu.size()) {
 						war.assignPlayer(p, 1);
 					}
 				} else {
 					p.sendMessage(ChatColor.DARK_RED + "You're already on a team, dolt!");
 					String team = null;
 					if (war.teamName(p) == 0) {
 						team = "blu";
 					} else if (war.teamName(p) == 1) {
 						team = "red";
 					} else {
 						p.kickPlayer(ChatColor.RED + "Uncaught exception!");
 					}
 					p.sendMessage(ChatColor.DARK_RED + "You're on team: " + team);
 				}
 			} else {
 				sender.sendMessage(a + "In-game command only.");
 			}
 		}
 		if (label.equals("blu")) {
 			if (sender.hasPermission("jtwar.admin")) {
 				if (sender instanceof Player) {
 					Player p = (Player) sender;
 					int x = (int) p.getLocation().getX();
 					int y = (int) p.getLocation().getY();
 					int z = (int) p.getLocation().getZ();
 					int yaw = (int) p.getLocation().getYaw();
 					int pitch = (int) p.getLocation().getPitch();
 					war.getConfig().set("blu-spawn-x", x);
 					war.getConfig().set("blu-spawn-y", y);
 					war.getConfig().set("blu-spawn-z", z);
 					war.getConfig().set("blu-spawn-yaw", yaw);
 					war.getConfig().set("blu-spawn-pitch", pitch);
 					war.saveConfig();
 					p.sendMessage(ChatColor.GREEN + "Blu team spawn set!");
 					p.sendMessage(ChatColor.GREEN + "When ready type /ready");
 				} else {
 					sender.sendMessage(a + "In-game command only.");
 				}
 			}
 		}
 		if (label.equals("red")) {
 			if (sender.hasPermission("jtwar.admin")) {
 				if (sender instanceof Player) {
 					Player p = (Player) sender;
 					int x = (int) p.getLocation().getX();
 					int y = (int) p.getLocation().getY();
 					int z = (int) p.getLocation().getZ();
 					int yaw = (int) p.getLocation().getYaw();
 					int pitch = (int) p.getLocation().getPitch();
 					war.getConfig().set("red-spawn-x", x);
 					war.getConfig().set("red-spawn-y", y);
 					war.getConfig().set("red-spawn-z", z);
 					war.getConfig().set("red-spawn-yaw", yaw);
 					war.getConfig().set("red-spawn-pitch", pitch);
 					war.saveConfig();
 					p.sendMessage(ChatColor.GREEN + "Red team spawn set!");
 					p.sendMessage(ChatColor.GREEN + "When ready type /ready");
 				} else {
 					sender.sendMessage(a + "In-game command only.");
 				}
 			}
 		}
 		if (label.equals("spectate")) {
 			if (sender instanceof Player) {
 				Player p = (Player) sender;
 				if (sender.hasPermission("jtwar.admin")) {
 					int x = (int) p.getLocation().getX();
 					int y = (int) p.getLocation().getY();
 					int z = (int) p.getLocation().getZ();
 					int yaw = (int) p.getLocation().getYaw();
 					int pitch = (int) p.getLocation().getPitch();
 					war.getConfig().set("spec-spawn-x", x);
 					war.getConfig().set("spec-spawn-y", y);
 					war.getConfig().set("spec-spawn-z", z);
 					war.getConfig().set("spec-spawn-yaw", yaw);
 					war.getConfig().set("spec-spawn-pitch", pitch);
 					war.saveConfig();
 					p.sendMessage(ChatColor.GREEN + "Spectator spawn set!");
 					p.sendMessage(ChatColor.GREEN + "When ready type /ready");
 				}
 			} else {
 				sender.sendMessage(a + "In-game command only.");
 			}
 		}
 		if (label.equals("ready")) {
 			if (sender.hasPermission("jtwar.admin")) {
 				if (sender instanceof Player) {
 					Player p = (Player) sender;
 					war.getConfig().set("has-started", true);
 					war.saveConfig();
 					if (war.getConfig().getBoolean("ready-to-go")) {
 						war.getConfig().set("ready-to-go", false);
 						war.saveConfig();
						war.blocks = false;
 						p.sendMessage(ChatColor.GREEN + "Server set to unready mode, block changes now allowed!");
 						p.sendMessage(ChatColor.GREEN + "Mob spawns are now disabled. When ready type /ready again!");
 						int count = 0;
 						for (Entity e : p.getWorld().getEntities()) {
 							if (!(e instanceof Player)) {
 								e.remove();
 								count++;
 							}
 						}
						p.sendMessage("Removed " + count + " mobs!");
 					} else {
 						war.getConfig().set("ready-to-go", true);
 						war.saveConfig();
						war.blocks = true;
 						p.sendMessage(ChatColor.GREEN + "Server set to ready mode, block changes now disabled!");
 						p.sendMessage(ChatColor.GREEN + "Enabling mob spawns... To disable ready mode type /ready again!");
 					}
 				} else {
 					sender.sendMessage(a + "In-game command only.");
 				}
 			}
 		}
 		if (label.equals("reset")) {
 			if (sender.hasPermission("jtwar.admin")) {
 				war.getConfig().set("has-started", false);
 				war.getConfig().set("ready-to-go", false);
 				war.saveConfig();
				war.blocks = false;
 				sender.sendMessage(ChatColor.GREEN + "Reset configuration but kept spawns!");
 			}
 		}
 		return true;
 	}
 
 }
