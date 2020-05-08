 package com.bendude56.hunted;
 
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class CmdExec implements CommandExecutor {
 
 	public CmdExec() {
 		Bukkit.getPluginCommand("manhunt").setExecutor(this);
 		Bukkit.getPluginCommand("m").setExecutor(this);
 	}
 	
 	Game g = HuntedPlugin.getInstance().getGame();
 	SettingsFile settings = HuntedPlugin.getInstance().getSettings();
 	WorldDataFile worlddata = HuntedPlugin.getInstance().getWorldData();
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command c, String cmd,
 			String[] args) {
 		if (!(sender instanceof Player)) {
 			sender.sendMessage(ChatColor.RED
 					+ "The /manhunt command cannot be used from the console!");
 			return true;
 		}
 		Player p = (Player) sender;
 		
 		
 		if (cmd.equalsIgnoreCase("manhunt") || cmd.equalsIgnoreCase("m")) {
 			
 			if (args.length == 0) {
 				helpCommand(args, p);
 				return true;
 			}
 			
 			if (args[0].equalsIgnoreCase("walrus")) {
 				p.sendMessage(ChatColor.AQUA + "THE WARLUS LIVES IN ALL OF US! (:3=");
 				return true;
 			}
 			
 			if (args[0].equalsIgnoreCase("seal")) {
 				p.sendMessage(ChatColor.AQUA + "HOW DARE YOU UTTER THAT WORD! >:3=");
 				return true;
 			}
 			
 			if (args[0].equalsIgnoreCase("help")) {
 				args = shiftArgs(args);
 				helpCommand(args, p);
 			
 			} else if (args[0].equalsIgnoreCase("1")
 					|| args[0].equalsIgnoreCase("2")
 					|| args[0].equalsIgnoreCase("3")) {
 				helpCommand(args, p);
 				
 			} else if (args[0].equalsIgnoreCase("rules")
 					|| args[0].equalsIgnoreCase("rule")) {
 				args = shiftArgs(args);
 				rulesCommand(args, p);
 				
 			} else if (args[0].equalsIgnoreCase("join")) {
 				args = shiftArgs(args);
 				if (args.length == 0) {
 					p.sendMessage(ChatColor.RED + "Proper use is /m join <team>");
 					return true;
 				}
 				joinCommand(args, p);
 			
 			} else if (args[0].equalsIgnoreCase("assign")) {
 				args = shiftArgs(args);
 				if (args.length < 2 || args.length == 0) {
 					p.sendMessage(ChatColor.RED + "Proper use is /m assign <player> <team>");
 					return true;
 				}
 				if (!(args[0].equalsIgnoreCase("hunter") || args[0].equalsIgnoreCase("hunters")
 						|| args[0].equalsIgnoreCase("prey") || args[0].equalsIgnoreCase("hunted")
 						|| args[0].equalsIgnoreCase("spectators") || args[0].equalsIgnoreCase("spectator") || args[0].equals("spectate"))) {
 					String a = args[0];
 					args[0] = args[1];
 					args[1] = a;
 				}
 				joinCommand(args, p);
 			
 			} else if (args[0].equalsIgnoreCase("hunter") || args[0].equalsIgnoreCase("hunters")
 					|| args[0].equalsIgnoreCase("prey") || args[0].equalsIgnoreCase("hunted")) {
 				joinCommand(args, p);
 				
 			} else if (args[0].equalsIgnoreCase("quit")) {
 				args = shiftArgs(args);
 				quitCommand(args, p);
 				
 			} else if (args[0].equalsIgnoreCase("kick")) {
 				args = shiftArgs(args);
 				if (args.length == 0) {
 					p.sendMessage(ChatColor.RED + "Proper use is /kick <player>");
 					return true;
 				}
 				kickCommand(args, p);
 				
 			} else if (args[0].equalsIgnoreCase("spectate")) {
 				args = shiftArgs(args);
 				spectateCommand(args, p);
 	
 			} else if (args[0].equalsIgnoreCase("makeall")
 					|| args[0].equalsIgnoreCase("makeeveryone")) {
 				args = shiftArgs(args);
 				if (args.length == 0) {
 					p.sendMessage(ChatColor.RED + "Proper use is /makeall <team>");
 					return true;
 				}
 				makeallCommand(args, p);
 	
 			} else if (args[0].equalsIgnoreCase("list")) {
 				args = shiftArgs(args);
 				listCommand(args, p);
 	
 			} else if (args[0].equalsIgnoreCase("status")
 					|| args[0].equalsIgnoreCase("info")) {
 				args = shiftArgs(args);
 				statusCommand(args, p);
 	
 			} else if (args[0].equalsIgnoreCase("world")
 					|| args[0].equalsIgnoreCase("spawn")) {
 				args = shiftArgs(args);
 				spawnCommand(args, p);
 				
 			} else if (args[0].equalsIgnoreCase("setspawn")) {
 				args = shiftArgs(args);
 				setspawnCommand(args, p);
 				
 			} else if (args[0].equalsIgnoreCase("startgame")) {
 				args = shiftArgs(args);
 				startCommand(args, p);
 				
 			} else if (args[0].equalsIgnoreCase("stopgame")) {
 				args = shiftArgs(args);
 				stopCommand(args, p);
 	
 			} else if (args[0].equalsIgnoreCase("restoredefaults")
 					|| args[0].equalsIgnoreCase("defaultsettings")
 					|| args[0].equalsIgnoreCase("loaddefaults")) {
 				args = shiftArgs(args);
 				defaultsCommand(args, p);
 				
 			} else if (args[0].equalsIgnoreCase("reload")
 					|| args[0].equalsIgnoreCase("load")) {
 				args = shiftArgs(args);
 				loadCommand(args, p);
 				
 			} else if (args[0].equalsIgnoreCase("weather")) {
 				args = shiftArgs(args);
 				if (args.length == 0) {
 					p.sendMessage(ChatColor.RED + "Proper use is /weather <clear|rainy|stormy>");
 					return true;
 				}
 				weatherCommand(args, p);
 				
 			} else if (args[0].equalsIgnoreCase("setting")
 					|| args[0].equalsIgnoreCase("settings")
 					|| args[0].equalsIgnoreCase("preferences")
 					|| args[0].equalsIgnoreCase("properties")) {
 				args = shiftArgs(args);
 				settingsCommand(args, p);
 				
 			} else {
 				p.sendMessage(ChatColor.RED
 						+ "Unknown Manhunt command! Type \"/manhunt help\" for help.");
 			}
 			return true;
 		}
 		
 		return true;
 	}
 	
 	private void helpCommand(String[] args, Player p) {
 		String page = "1";
 		
 		if (args.length > 0) {
 			if (args[0].equalsIgnoreCase("1")) {
 				page = "1";
 			}
 			if (args[0].equalsIgnoreCase("2")) {
 				page = "2";
 			}
 		}
 		
 		if (page.equalsIgnoreCase("1")) {
 			p.sendMessage(ChatColor.GOLD + "---- Manhunt help: (1/2) ----");
 			p.sendMessage(pre() + ChatColor.DARK_GREEN + "/m startgame" + ChatColor.WHITE + " Starts the Manhunt game.");
 			p.sendMessage(pre() + ChatColor.DARK_GREEN + "/m stopgame" + ChatColor.WHITE + " Stops the Manhunt game.");
 			p.sendMessage(pre() + ChatColor.DARK_GREEN + "/m hunter" + ChatColor.WHITE + " Joins team " + ChatColor.DARK_RED + "hunter.");
 			p.sendMessage(pre() + ChatColor.DARK_GREEN + "/m prey" + ChatColor.WHITE + " Joins team " + ChatColor.BLUE + "prey.");
 			p.sendMessage(pre() + ChatColor.DARK_GREEN + "/m spectate" + ChatColor.WHITE + " Joins team " + ChatColor.WHITE + "spectator.");
 			p.sendMessage(pre() + ChatColor.WHITE + "Add a player's name to assign them to that team.");
			p.sendMessage(pre() + ChatColor.DARK_GREEN + "/m makeall [team]" + ChatColor.WHITE + " Assigns everyone to a specific team.");
 			p.sendMessage(pre() + ChatColor.DARK_GREEN + "/m quit" + ChatColor.WHITE + " Quits the Manhunt game and makes you a spectator.");
 			if (args.length == 0) p.sendMessage(pre() + ChatColor.DARK_GREEN + "Type \"" + ChatColor.RED + "/m help 2" + ChatColor.DARK_GREEN + "\" to view page 2!");
 		} else if (page.equalsIgnoreCase("2")) {
 			p.sendMessage(ChatColor.GOLD + "---- Manhunt help: (2/2) ----");
 			p.sendMessage(pre() + ChatColor.DARK_GREEN + "/m setting [page]" + ChatColor.WHITE + " Lists current Manhunt settings.");
 			p.sendMessage(pre() + ChatColor.DARK_GREEN + "/m setting [setting] [value]" + ChatColor.WHITE + " Changes a Manhunt setting.");
 			p.sendMessage(pre() + ChatColor.DARK_GREEN + "/m spawn [team]" + ChatColor.WHITE + " Teleports you to the world/team spawn");
 			p.sendMessage(pre() + ChatColor.WHITE + "Include a player's name to teleport them instead.");
 			p.sendMessage(pre() + ChatColor.DARK_GREEN + "/m setspawn [team]" + ChatColor.WHITE + " Sets the world's or a team's spawn.");
 			p.sendMessage(pre() + ChatColor.DARK_GREEN + "/m makeall [team]" + ChatColor.WHITE + " Assigns everyone to a specific team.");
 			p.sendMessage(pre() + ChatColor.DARK_GREEN + "/m kick [player]" + ChatColor.WHITE + " Makes the palyer a spectator.");
 			p.sendMessage(pre() + ChatColor.DARK_GREEN + "/m list" + ChatColor.WHITE + " Lists the Manhunt players.");
 			p.sendMessage(pre() + ChatColor.DARK_GREEN + "/m status" + ChatColor.WHITE + " Gives information on the current Manhunt game.");
 		}
 	}
 	
 	private void rulesCommand(String[] args, Player p) {
 		if (args.length == 0 || args[0].equalsIgnoreCase("1")) {
 			p.sendMessage(ChatColor.GOLD + "---- Manhunt General Rules (1/3) ----");
 			p.sendMessage(pre() + "Manhunt is an exciting game where two teams go");
 			p.sendMessage(pre() + "head-to-head: the " + ChatColor.DARK_RED + "Hunters" + ChatColor.WHITE + " and the " + ChatColor.BLUE + "Prey" + ChatColor.WHITE + ".");
 			if (settings.prepTime() != 0) {
 				p.sendMessage(pre() + "The " + ChatColor.BLUE + "Prey" + ChatColor.WHITE + " have " + settings.prepTime() + " minutes to prepare before the hunt.");
 			} else {
 				p.sendMessage(pre() + "The hunt starts immediately with no preparation time.");
 			}
 			p.sendMessage(pre() + "When the game starts (at sundown), the " + ChatColor.DARK_RED + "Hunters");
 			p.sendMessage(pre() + "are released to hunt and kill the " + ChatColor.BLUE + "Prey" + ChatColor.WHITE + ".");
 			p.sendMessage(pre() + "The " + ChatColor.DARK_RED + "Hunters" + ChatColor.WHITE + " have " + settings.dayLimit() + " days to kill all the " + ChatColor.BLUE + "Prey" + ChatColor.WHITE + ".");
 			p.sendMessage(pre() + "The first team to wipe out the other " + ChatColor.GOLD + "wins the game!");
 			p.sendMessage(pre() + "But if time runs out, the "  +ChatColor.BLUE + "Prey" + ChatColor.WHITE + " will win.");
 			if (args.length == 0) p.sendMessage(pre() + ChatColor.DARK_GREEN + "Type \"" + ChatColor.RED + "/m rules 2" + ChatColor.DARK_GREEN + "\" to view page 2!");
 			
 		} else if (args[0].equalsIgnoreCase("2")) {
 			p.sendMessage(ChatColor.GOLD + "---- Manhunt Detailed Rules (2/3) ----");
 			if (settings.loadouts()) {
 				p.sendMessage(pre() + "Every player will get their own loadouts to start with.");
 				if (settings.teamHats()) {
 					p.sendMessage(pre() + "And each team gets identifying hats.");
 				}
 			}
 			p.sendMessage(pre() + "All players must remain above ground, and no spawn camping!");
 			if (settings.pvpInstantDeath()) p.sendMessage(pre() + "Insta-kill is ON, so everyone is a one-hit kill.");
 			if (settings.envDeath()) {
 				if (settings.envHunterRespawn() && settings.envPreyRespawn()) {
 					p.sendMessage(pre() + "If someone dies from the environment, they will respawn.");
 				} else if (settings.envHunterRespawn() && !settings.envPreyRespawn()) {
 					p.sendMessage(pre() + "Only " + ChatColor.DARK_RED + "Hunters" + ChatColor.WHITE + " will respawn from environmental death.");
 				} else if (!settings.envHunterRespawn() && settings.envPreyRespawn()) {
 					p.sendMessage(pre() + "Only " + ChatColor.BLUE + "Prey" + ChatColor.WHITE + " will respawn from environmental death.");
 				} else if (!settings.envHunterRespawn() && !settings.envPreyRespawn()) {
 					p.sendMessage(pre() + "Players are elminiated when killed by the environment");
 				}
 			} else {
 				p.sendMessage(pre() + "You cannot die by the environment.");
 			}
 			if (settings.preyFinder()) {
 				p.sendMessage(pre() + ChatColor.DARK_RED + "Hunter" + ChatColor.WHITE + " compasses can be used as Prey Finders.");
 				p.sendMessage(pre() + ChatColor.GRAY + " (Requires at least " + g.locatorFood()/2 + " food! Click while holding compass)");
 			}
 			if (settings.spawnHostile()) {
 				p.sendMessage(pre() + "Hostiles mobs are turned ON.");
 			} else {
 				p.sendMessage(pre() + "Hostiles mobs are turned OFF.");
 			}
 			if (settings.allTalk()) {
 				p.sendMessage(pre() + "All talk is on, so all players can chat together.");
 			} else {
 				p.sendMessage(pre() + "All talk is off, so only teammates can chat with eachother.");
 			}
 		} else if (args[0].equalsIgnoreCase("3")) {
 			p.sendMessage(ChatColor.GOLD + "---- Manhunt Miscellaneous Rules (3/3) ---");
 			p.sendMessage(pre() + "Players may not build " + worlddata.noBuildRange() + " blocks from the spawn.");
 			p.sendMessage(pre() + "Players have " + settings.offlineTimeout() + " minutes to return to the game.");
 			if (settings.flyingSpectators()) {
 				p.sendMessage(pre() + "Spectators are invisible, and can fly freely.");
 			} else {
 				p.sendMessage(pre() + "Spectators are invisible to other players");
 			}
 			if (settings.friendlyFire()) {
 				p.sendMessage(pre() + "Friendly fire is ON, so be careful.");
 			} else {
 				p.sendMessage(pre() + "Friendly fire is turned OFF.");
 			}
 		}
 	}
 	
 	private void startCommand(String[] args, Player p) {
 		if (!p.isOp()) {
 			p.sendMessage(ChatColor.RED
 					+ "Only ops can start the manhunt game!");
 			return;
 		}
 		if (g.HuntersAmount(true) == 0 || g.HuntedAmount(true) == 0) {
 			p.sendMessage(ChatColor.RED
 					+ "There must be at least one Hunter and Prey to start the game!");
 			return;
 		} else {
			if (g.HuntersAmount(true) < (g.HuntedAmount(true)-1)*4) {
 				p.sendMessage(ChatColor.RED
						+ "There must be at least 4 hunters per prey!");
 				return;
 			}
 		}
 		HuntedPlugin.getInstance().log(Level.INFO, p.getName() + " has started the Manhunt game!");
 		g.start();
 		return;
 	}
 	
 	private void stopCommand(String[] args, Player p) {
 		if (!p.isOp()) {
 			p.sendMessage(ChatColor.RED
 					+ "Only ops can stop the Manhunt game!");
 			return;
 		}
 		if (!g.gameHasBegun()) {
 			p.sendMessage(ChatColor.RED + "There is no Manhunt game running!");
 			return;
 		}
 		g.stop();
 		g.broadcastAll(ChatColor.GOLD + "-----------------------------------------------------");
 		g.broadcastAll(ChatColor.GOLD + "" + g.getColor(p) + p.getName() + ChatColor.GOLD
 				+ " has stopped the Manhunt game!");
 		g.broadcastAll(ChatColor.GOLD + "-----------------------------------------------------");
 		HuntedPlugin.getInstance().log(Level.INFO, "-------------------------------------");
 		HuntedPlugin.getInstance().log(Level.INFO, "" + p.getName() + " has stopped the Manhunt game!");
 		HuntedPlugin.getInstance().log(Level.INFO, "-------------------------------------");
 		return;
 	}
 	
 	private void joinCommand(String[] args, Player p) {
 		if (g.gameHasBegun()) {
 			p.sendMessage(ChatColor.RED
 					+ "You cannot join a game already in progress!");
 			return;
 		} else if (settings.mustBeOp() && !p.isOp()) {
 			p.sendMessage(ChatColor.RED
 					+ "Only OPs have permission to do that.");
 			return;
 		}
 		if (args.length == 0) {
 			p.sendMessage(ChatColor.RED
 					+ "You must choose a team! Hunters or Prey.");
 			return;
 		}
 		if (args[0].equalsIgnoreCase("hunter")
 				|| args[0].equalsIgnoreCase("hunters")) {
 			if (args.length == 1) {
 				g.addHunter(p);
 				g.broadcastAll(g.getColor(p) + p.getName()
 						+ ChatColor.WHITE + " has joined team "
 						+ ChatColor.DARK_RED + "Hunters.");
 				HuntedPlugin.getInstance().log(Level.INFO, p.getName() + " has joined team Hunters.");
 			} else if (args.length > 1 && p.isOp()) {
 				if (Bukkit.getPlayerExact(args[1]) == null) {
 					p.sendMessage(ChatColor.RED + "\""+ args[1] + "\" does not exist!");
 					return;
 				}
 				g.addHunter(Bukkit.getPlayerExact(args[1]).getName());
 				g.broadcastAll(ChatColor.DARK_RED + Bukkit.getPlayerExact(args[1]).getName() + ChatColor.WHITE
 						+ " has joined team " + ChatColor.DARK_RED + "Hunters.");
 				HuntedPlugin.getInstance().log(Level.INFO, Bukkit.getPlayerExact(args[1]).getName() + " has joined team Hunters.");
 			}
 		} else if (args[0].equalsIgnoreCase("hunted")
 				|| args[0].equalsIgnoreCase("prey")) {
 			if (args.length == 1) {
 				g.addHunted(p);
 				g.broadcastAll(ChatColor.BLUE + p.getName()
 						+ ChatColor.WHITE + " has joined team "
 						+ ChatColor.BLUE + "Prey.");
 				HuntedPlugin.getInstance().log(Level.INFO, p.getName() + " has joined team Prey.");
 			} else if (args.length > 1 && p.isOp()) {
 				if (Bukkit.getPlayerExact(args[1]) == null) {
 					p.sendMessage(ChatColor.RED + "\""+ args[1] + "\" does not exist!");
 					return;
 				}
 				g.addHunted(Bukkit.getPlayerExact(args[1]).getName());
 				g.broadcastAll(ChatColor.BLUE + Bukkit.getPlayerExact(args[1]).getName() + ChatColor.WHITE
 						+ " has joined team " + ChatColor.BLUE + "Prey.");
 				HuntedPlugin.getInstance().log(Level.INFO, Bukkit.getPlayerExact(args[1]).getName() + " has joined team Prey.");
 			}
 		} else if (args[0].equalsIgnoreCase("spectators")
 				|| args[0].equalsIgnoreCase("spectator")) {
 			if (args.length == 1) {
 				g.addSpectator(p);
 				g.broadcastAll(ChatColor.YELLOW + p.getName()
 						+ ChatColor.WHITE + " has become a "
 						+ ChatColor.YELLOW + "Spectator.");
 				HuntedPlugin.getInstance().log(Level.INFO, p.getName() + " has become a spectator.");
 			} else if (args.length > 1 && p.isOp()) {
 				if (Bukkit.getPlayerExact(args[1]) == null) {
 					p.sendMessage(ChatColor.RED + "\""+ args[1] + "\" does not exist!");
 					return;
 				}
 				g.addHunted(Bukkit.getPlayerExact(args[1]).getName());
 				g.broadcastAll(ChatColor.YELLOW + Bukkit.getPlayerExact(args[1]).getName()
 						+ ChatColor.WHITE + " has become a "
 						+ ChatColor.YELLOW + "Spectator.");
 				HuntedPlugin.getInstance().log(Level.INFO, Bukkit.getPlayerExact(args[1]).getName() + " has become a spectator.");
 			}
 		} else {
 			p.sendMessage(ChatColor.RED + args[0] + " is not a team! Choose either Hunters, Prey, or Spectators.");
 		}
 	}
 	
 	private void quitCommand(String[] args, Player p) {
 		if (g.isHunted(p) || g.isHunter(p)) {
 			if (g.gameHasBegun()) {
 				g.broadcastAll(ChatColor.YELLOW + p.getName()
 						+ ChatColor.WHITE
 						+ " has quit the game and become a "
 						+ ChatColor.YELLOW + "Spectator.");
 				g.onDie(p);
 				HuntedPlugin.getInstance().log(Level.INFO, p.getName() + " has quit the game and is now spectating.");
 				return;
 			} else {
 				g.addSpectator(p);
 				g.broadcastAll(ChatColor.YELLOW + p.getName()
 						+ ChatColor.WHITE + " has become a "
 						+ ChatColor.YELLOW + "Spectator.");
 				HuntedPlugin.getInstance().log(Level.INFO, p.getName() + " has become a spectator.");
 				return;
 			}
 		}
 	}
 	
 	private void kickCommand(String[] args, Player p) {
 		if (args.length >= 1) {
 			Player p2 = Bukkit.getPlayerExact(args[0]);
 			if (p2 == null) {
 				if (g.isHunted(args[0]) || g.isHunter(args[0])
 						|| g.isSpectating(args[0])) {
 					g.onDie(args[0]);
 					HuntedPlugin.getInstance().log(Level.INFO, args[0] + " was removed from the Manhunt lists by " + p.getName());
 					g.deletePlayer(args[0]);
 				} else {
 					p.sendMessage(ChatColor.RED + "No player named "
 							+ args[0] + " exists!");
 				}
 				return;
 			}
 			if (p.isOp() && !p2.isOp() && p != p2) {
 				g.broadcastAll(g.getColor(p2) + p2.getName()
 						+ ChatColor.WHITE + " has become a "
 						+ ChatColor.YELLOW + "Spectator.");
 				g.onDie(p2);
 				p2.sendMessage(ChatColor.RED
 						+ "You have been kicked from the game. You are now spectating.");
 				HuntedPlugin.getInstance().log(Level.INFO, args[0] + " was kicked from the game by " + p.getName());
 				return;
 			}
 		} else {
 			p.sendMessage(ChatColor.RED + "Who did you want to kick?");
 			return;
 		}
 	}
 	
 	private void spectateCommand(String[] args, Player p) {
 		if ((g.isHunted(p) || g.isHunter(p)) && g.gameHasBegun()
 				&& args.length == 0) {
 			p.sendMessage(ChatColor.RED
 					+ "You can't spectate while you're playing!");
 			p.sendMessage(ChatColor.RED
 					+ "Use \"/manhunt quit\" if you want to quit the game.");
 			return;
 		} else if (p.isOp() || !settings.mustBeOp()) {
 			if (args.length == 0) {
 				g.addSpectator(p);
 				g.broadcastAll(ChatColor.YELLOW + p.getName()
 						+ ChatColor.WHITE + " has become a "
 						+ ChatColor.YELLOW + "Spectator.");
 				HuntedPlugin.getInstance().log(Level.INFO, p.getName() + " has become a Spectator");
 				return;
 			} else if (args.length >= 1 && p.isOp()) {
 				Player p2 = Bukkit.getPlayerExact(args[0]);
 				if (p2 == null) {
 					p.sendMessage(ChatColor.RED + args[0]
 							+ " does not exist!");
 					return;
 				}
 				if ((g.isHunted(p2) || g.isHunter(p2)) && g.gameHasBegun()) {
 					p.sendMessage(ChatColor.RED + args[0]
 							+ " is in the middle of a game!");
 					p.sendMessage(ChatColor.RED + "Use \"/manhunt kick <"
 							+ args[0] + ">\" to properly kick them.");
 					return;
 				} else {
 					g.broadcastAll(g.getColor(p2) + p2.getName()
 							+ ChatColor.WHITE + " has become a "
 							+ ChatColor.YELLOW + "Spectator.");
 					g.addSpectator(p2);
 					HuntedPlugin.getInstance().log(Level.INFO, p2.getName() + " has become a Spectator");
 					return;
 				}
 			}
 		}
 	}
 	
 	private void makeallCommand(String[] args, Player p) {
 		if (!p.isOp()) {
 			p.sendMessage(ChatColor.RED
 					+ "You don't have permission to do that!");
 			return;
 		}
 		if (g.gameHasBegun()) {
 			p.sendMessage(ChatColor.RED + "You can't do that while the game is running!");
 		}
 		if (args.length == 0) {
 			p.sendMessage(ChatColor.RED
 					+ "You must specify a team to move everyone to!");
 			return;
 		}
 		if (args[0].equalsIgnoreCase("hunter")
 				|| args[0].equalsIgnoreCase("hunters")) {
 			for (Player s : Bukkit.getOnlinePlayers()) {
 				if (g.isHunted(s) || g.isSpectating(s)) {
 					g.addHunted(s);
 					s.sendMessage(ChatColor.WHITE
 							+ "You have been moved to team "
 							+ ChatColor.RED + "Hunters.");
 				}
 			}
 			g.broadcastAll(ChatColor.WHITE
 					+ "All players have been moved to team "
 					+ ChatColor.DARK_RED + "Hunters.");
 			HuntedPlugin.getInstance().log(Level.INFO, p.getName() + " moved everyone to team Hunters.");
 			return;
 		} else if (args[0].equalsIgnoreCase("hunted")
 				|| args[0].equalsIgnoreCase("prey")) {
 			for (Player s : Bukkit.getOnlinePlayers()) {
 				if (g.isHunter(s) || g.isSpectating(s)) {
 					g.addHunted(s);
 					s.sendMessage(ChatColor.WHITE
 							+ "You have been moved to team "
 							+ ChatColor.BLUE + "Prey.");
 				}
 			}
 			g.broadcastAll(ChatColor.WHITE
 					+ "All players have been moved to team "
 					+ ChatColor.BLUE + "Prey.");
 			HuntedPlugin.getInstance().log(Level.INFO, p.getName() + " moved everyone to team Prey.");
 			return;
 		} else if (args[0].equalsIgnoreCase("spectator")
 				|| args[0].equalsIgnoreCase("spectators")) {
 			for (Player s : Bukkit.getOnlinePlayers()) {
 				if (g.isHunter(s) || g.isHunted(s)) {
 					g.addSpectator(s);
 					s.sendMessage(ChatColor.WHITE + "You are now a "
 							+ ChatColor.YELLOW + "Spectator.");
 				}
 			}
 			g.broadcastAll(ChatColor.WHITE + "All players are now "
 					+ ChatColor.YELLOW + "Spectating.");
 			HuntedPlugin.getInstance().log(Level.INFO, p.getName() + " made everyone spectators.");
 			return;
 		} else {
 			p.sendMessage(ChatColor.RED
 					+ "Invalid team. {Hunters|Prey|Spectator}");
 		}
 		return;
 	}
 	
 	private void listCommand(String[] args, Player p) {
 		if (args.length == 0
 				|| (args.length >= 1 && args[0].equalsIgnoreCase("all"))) {
 			p.sendMessage(ChatColor.GREEN + "Manhunt players: ("+ (g.HuntedAmount(false) + g.HuntersAmount(false) + g.SpectatorsAmount(false)) + ")  "
 				+ ChatColor.DARK_RED + "Hunters: " + g.HuntersAmount(false) + ChatColor.BLUE + "  Prey: " + g.HuntedAmount(false) + ChatColor.YELLOW + "  Spectators: " + g.SpectatorsAmount(false));
 			for (String s : g.getHunters()) {
 				if (Bukkit.getPlayerExact(s) == null) {
 					s += " " + ChatColor.GRAY + "(Offline)";
 				}
 				p.sendMessage(ChatColor.DARK_RED + "  " + s);
 			}
 			for (String s : g.getHunted()) {
 				if (Bukkit.getPlayerExact(s) == null) {
 					s += " " + ChatColor.GRAY + "(Offline)";
 				}
 				p.sendMessage(ChatColor.BLUE + "  " + s);
 			}
 			for (String s : g.getSpectators()) {
 				p.sendMessage(ChatColor.YELLOW + "  " + s);
 			}
 		} else if (args.length >= 1 && args[0].equalsIgnoreCase("hunters")) {
 			p.sendMessage(ChatColor.GREEN + "Team HUNTERS: ("
 					+ g.HuntersAmount(false) + ")");
 			for (String s : g.getHunters()) {
 				p.sendMessage(ChatColor.DARK_RED + "  " + s);
 			}
 		} else if (args.length >= 1 && args[0].equalsIgnoreCase("prey")) {
 			p.sendMessage(ChatColor.GREEN + "Team HUNTED: ("
 					+ g.HuntedAmount(false) + ")");
 			for (String s : g.getHunted()) {
 				p.sendMessage(ChatColor.BLUE + "  " + s);
 			}
 		} else if (args.length >= 2
 				&& args[0].equalsIgnoreCase("spectators")) {
 			p.sendMessage(ChatColor.GREEN + "Manhunt SPECTATORS: ("
 					+ g.SpectatorsAmount(false) + ")");
 			for (String s : g.getSpectators()) {
 				p.sendMessage(ChatColor.YELLOW + "  " + s);
 			}
 		} else {
 			p.sendMessage(ChatColor.RED
 					+ "Invalid team. Available teams: Hunters, Prey, Spectators, All.");
 		}
 	}
 	
 	private void statusCommand(String[] args, Player p) {
 		if (!g.gameHasBegun()) {
 			p.sendMessage(ChatColor.RED + "No running Manhunt games.");
 			return;
 		}
 		p.sendMessage(ChatColor.AQUA + "Status of this Manhunt game");
 		String time = "";
 		if (g.huntHasBegun()) {
 			
 			time = "Time till game end:  ";
 			time += (int) Math.floor((g.getEndTick() - g.getTick()) / 72000) + ":";
 			if ((int) (Math.floor((g.getEndTick() - g.getTick()) / 1200) - Math
 							.floor((g.getEndTick() - g.getTick()) / 72000) * 60) < 10) {
 				time += "0";
 			}
 			time += (int) (Math.floor((g.getEndTick() - g.getTick()) / 1200) - Math
 					.floor((g.getEndTick() - g.getTick()) / 72000) * 60) + ":";
 			if ((int) (Math.floor((g.getEndTick() - g.getTick()) / 20) - (Math
 							.floor((g.getEndTick() - g.getTick()) / 1200) - Math
 							.floor((g.getEndTick() - g.getTick()) / 72000)) * 60) < 10) {
 				time += "0";
 			}
 			time += (int) (Math.floor((g.getEndTick() - g.getTick()) / 20) - (Math
 					.floor((g.getEndTick() - g.getTick()) / 1200) - Math
 					.floor((g.getEndTick() - g.getTick()) / 72000)) * 60) + "";
 			
 		} else {
 			
 			time = "Time till hunt starts:  ";
 			time += (int) Math.floor((g.getHunterReleaseTick() - g.getTick()) / 72000) + ":";
 			if ((int) (Math.floor((g.getHunterReleaseTick() - g.getTick()) / 1200) - Math
 							.floor((g.getHunterReleaseTick() - g.getTick()) / 72000) * 60) < 10) {
 				time += "0";
 			}
 			time += (int) (Math.floor((g.getHunterReleaseTick() - g.getTick()) / 1200) - Math
 					.floor((g.getHunterReleaseTick() - g.getTick()) / 72000) * 60) + ":";
 			if ((int) (Math.floor((g.getHunterReleaseTick() - g.getTick()) / 20) - (Math
 							.floor((g.getHunterReleaseTick() - g.getTick()) / 1200) - Math
 							.floor((g.getHunterReleaseTick() - g.getTick()) / 72000)) * 60) < 10) {
 				time += "0";
 			}
 			time += (int) (Math.floor((g.getHunterReleaseTick() - g.getTick()) / 20) - (Math
 					.floor((g.getHunterReleaseTick() - g.getTick()) / 1200) - Math
 					.floor((g.getHunterReleaseTick() - g.getTick()) / 72000)) * 60) + "";
 			
 		}
 		p.sendMessage(ChatColor.GOLD + time);
 		p.sendMessage(ChatColor.DARK_RED + "Hunters: " + g.HuntersAmount(false)
 				+ ChatColor.BLUE + "  Prey: " + g.HuntedAmount(false)
 				+ ChatColor.YELLOW + "  Spectators: "
 				+ g.SpectatorsAmount(false));
 	}
 	
 	private void spawnCommand(String[] args, Player p) {
 		if (p.isOp() || !settings.mustBeOp() || g.isSpectating(p)) {
 			if (args.length == 0) {
 				if (g.isSpectating(p) || !g.gameHasBegun()) {
 					p.teleport(HuntedPlugin.getInstance().getWorld()
 							.getSpawnLocation());
 					p.sendMessage(ChatColor.GREEN + "You have teleported to the " + ChatColor.GOLD + "Manhunt" + ChatColor.GREEN + " world spawn.");
 					return;
 				} else {
 					p.sendMessage(ChatColor.RED
 							+ "You can't warp to the Manhunt spawn once the game has started!");
 					return;
 				}
 			} else if (args.length >= 1) {
 				if (args[0].equalsIgnoreCase("prey") || args[0].equalsIgnoreCase("hunted")) {
 					if (g.gameHasBegun() && !g.isSpectating(p)) {
 						p.sendMessage(ChatColor.RED
 								+ "You can't teleport there while the game is running!");
 						return;
 					}
 					if (args.length == 1) {
 						p.teleport(worlddata.preySpawn());
 						p.sendMessage(ChatColor.GREEN
 								+ "You have teleported to the " + ChatColor.BLUE
 								+ "Prey" + ChatColor.GREEN + " spawn.");
 						return;
 					} else if (args.length > 1) {
 						if (!p.isOp()) {
 							p.sendMessage(ChatColor.RED + "You don't have permission to teleport other players!");
 							return;
 						}
 						Player p2 = Bukkit.getPlayerExact(args[1]);
 						if (p2 == null) {
 							p.sendMessage(ChatColor.RED + "Player "
 									+ args[0] + " does not exist!");
 							return;
 						}
 						if (g.isSpectating(p2) || !g.gameHasBegun()) {
 							p2.teleport(worlddata.preySpawn());
 							p2.sendMessage(ChatColor.YELLOW
 									+ "You have been teleported to the "
 									+ ChatColor.BLUE + "Prey"
 									+ ChatColor.YELLOW + " spawn.");
 							p.sendMessage(g.getColor(p2) + p2.getName() + ChatColor.YELLOW
 									+ " has been teleported to the "
 									+ ChatColor.BLUE + "Prey"
 									+ ChatColor.YELLOW + " spawn.");
 							return;
 						}
 					}
 				} else if (args[0].equalsIgnoreCase("hunter") || args[0].equalsIgnoreCase("hunters")) {
 					if (g.gameHasBegun() && !g.isSpectating(p)) {
 						p.sendMessage(ChatColor.RED
 								+ "You can't teleport there while the game is running!");
 						return;
 					}
 					if (args.length == 1) {
 						p.teleport(worlddata.hunterSpawn());
 						p.sendMessage(ChatColor.GREEN
 								+ "You have teleported to the " + ChatColor.DARK_RED
 								+ "Hunter" + ChatColor.GREEN + " spawn.");
 						return;
 					} else if (args.length > 1) {
 						if (!p.isOp()) {
 							p.sendMessage(ChatColor.RED + "You don't have permission to teleport other players!");
 							return;
 						}
 						Player p2 = Bukkit.getPlayerExact(args[1]);
 						if (p2 == null) {
 							p.sendMessage(ChatColor.RED + "Player "
 									+ args[0] + " does not exist!");
 							return;
 						}
 						if (g.isSpectating(p2) || !g.gameHasBegun()) {
 							p2.teleport(worlddata.hunterSpawn());
 							p2.sendMessage(ChatColor.YELLOW
 									+ "You have been teleported to the "
 									+ ChatColor.DARK_RED + "Hunter"
 									+ ChatColor.YELLOW + " spawn.");
 							p.sendMessage(g.getColor(p2) + p2.getName() + ChatColor.YELLOW
 									+ " has been teleported to the "
 									+ ChatColor.DARK_RED + "Hunter"
 									+ ChatColor.YELLOW + " spawn.");
 							return;
 						}
 					}
 				} else if (args[0].equalsIgnoreCase("pregame") || args[0].equalsIgnoreCase("preparation") || args[0].equalsIgnoreCase("waiting")) {
 					if (g.gameHasBegun() && !g.isSpectating(p)) {
 						p.sendMessage(ChatColor.RED
 								+ "You can't teleport there while the game is running!");
 						return;
 					}
 					if (args.length == 1) {
 						p.teleport(worlddata.pregameSpawn());
 						p.sendMessage(ChatColor.GREEN
 								+ "You have teleported to the " + ChatColor.GOLD
 								+ "Pregame" + ChatColor.GREEN + " spawn.");
 						return;
 					} else if (args.length > 1) {
 						if (!p.isOp()) {
 							p.sendMessage(ChatColor.RED + "You don't have permission to teleport other players!");
 							return;
 						}
 						Player p2 = Bukkit.getPlayerExact(args[1]);
 						if (p2 == null) {
 							p.sendMessage(ChatColor.RED + "Player "
 									+ args[0] + " does not exist!");
 							return;
 						}
 						if (g.isSpectating(p2) || !g.gameHasBegun()) {
 							p2.teleport(worlddata.pregameSpawn());
 							p2.sendMessage(ChatColor.YELLOW
 									+ "You have been teleported to the "
 									+ ChatColor.GOLD + "Pregame"
 									+ ChatColor.YELLOW + " spawn.");
 							p.sendMessage(g.getColor(p2) + p2.getName() + ChatColor.YELLOW
 									+ " has been teleported to the "
 									+ ChatColor.GOLD + "Pregame"
 									+ ChatColor.YELLOW + " spawn.");
 							return;
 						}
 					}
 				} else {
 					Player p2 = Bukkit.getPlayerExact(args[0]);
 					if (p2 == null) {
 						p.sendMessage(ChatColor.RED + "Player " + args[0]
 								+ " does not exist!");
 						return;
 					}
 					if (g.isSpectating(p2) || !g.gameHasBegun()) {
 						p2.teleport(HuntedPlugin.getInstance().getWorld()
 								.getSpawnLocation());
 						p2.sendMessage(ChatColor.YELLOW
 								+ "You have been teleported to the Manhunt world spawn.");
 						p.sendMessage(g.getColor(p2) + p2.getName()
 								+ " has been teleported to the Manhunt world spawn.");
 						return;
 					}
 				}
 			}
 		}
 	}
 	
 	private void setspawnCommand(String[] args, Player p) {
 		if (!p.isOp()) {
 			p.sendMessage(ChatColor.RED + "Only ops can set spawns!");
 			return;
 		}
 		if (g.gameHasBegun()) {
 			p.sendMessage(ChatColor.RED + "You can't change spawn points while the game is running!");
 			return;
 		}
 		if (p.getWorld() != HuntedPlugin.getInstance().getWorld()) {
 			p.sendMessage(ChatColor.RED
 					+ "You must be in the manhunt world to set it's spawn. Use \"/manhunt spawn\" to teleport to the manhunt world.");
 			return;
 		}
 		if (args.length == 0) {
 			if (g.areNearby(worlddata.hunterSpawn(),HuntedPlugin.getInstance().getWorld().getSpawnLocation(),1.0)) {
 				worlddata.changeSetting("hunterSpawn", p.getLocation());
 			}
 			if (g.areNearby(worlddata.preySpawn(),HuntedPlugin.getInstance().getWorld().getSpawnLocation(),1.0)) {
 				worlddata.changeSetting("preySpawn", p.getLocation());
 			}
 			if (g.areNearby(worlddata.pregameSpawn(),HuntedPlugin.getInstance().getWorld().getSpawnLocation(),1.0)) {
 				worlddata.changeSetting("pregameSpawn", p.getLocation());
 			}
 			HuntedPlugin.getInstance().getWorld().setSpawnLocation(
 					p.getLocation().getBlockX(),
 					p.getLocation().getBlockY(),
 					p.getLocation().getBlockZ());
 			p.sendMessage(ChatColor.GREEN + "World spawn set!");
 			HuntedPlugin.getInstance().log(Level.INFO, p.getName() + " changed the Manhunt world spawn.");
 			return;
 		} else if (args.length == 1) {
 			if (args[0].equalsIgnoreCase("hunter")
 					|| args[0].equalsIgnoreCase("hunters")) {
 				worlddata.changeSetting("hunterSpawn", p.getLocation());
 				p.sendMessage(ChatColor.DARK_RED + "Hunter" + ChatColor.GREEN
 						+ " spawn set!");
 				HuntedPlugin.getInstance().log(Level.INFO, p.getName() + " changed the Hunter spawn.");
 				return;
 			}
 			if (args[0].equalsIgnoreCase("hunted")
 					|| args[0].equalsIgnoreCase("prey")) {
 				worlddata.changeSetting("preySpawn", p.getLocation());
 				p.sendMessage(ChatColor.BLUE + "Prey" + ChatColor.GREEN
 						+ " spawn set!");
 				HuntedPlugin.getInstance().log(Level.INFO, p.getName() + " changed the Prey spawn.");
 				return;
 			}
 			if (args[0].equalsIgnoreCase("pregame")
 					|| args[0].equalsIgnoreCase("waiting")
 					|| args[0].equalsIgnoreCase("preparation")) {
 				worlddata.changeSetting("pregameSpawn", p.getLocation());
 				p.sendMessage(ChatColor.GOLD + "Pregame" + ChatColor.GREEN
 						+ " spawn set!");
 				HuntedPlugin.getInstance().log(Level.INFO, p.getName() + " changed the Pregame spawn.");
 				return;
 			}
 			if (args[0].equalsIgnoreCase("all")) {
 				HuntedPlugin.getInstance().getWorld().setSpawnLocation(
 						p.getLocation().getBlockX(),
 						p.getLocation().getBlockY(),
 						p.getLocation().getBlockZ());
 				worlddata.changeSetting("hunterSpawn", p.getLocation());
 				worlddata.changeSetting("preySpawn", p.getLocation());
 				worlddata.changeSetting("pregameSpawn", p.getLocation());
 				p.sendMessage(ChatColor.GREEN + "All spawn points set!");
 				HuntedPlugin.getInstance().log(Level.INFO, p.getName() + " changed all spawn points.");
 			}
 		}
 	}
 	
 	private void defaultsCommand(String[] args, Player p) {
 		if (p.isOp()) {
 			settings.loadDefaults();
 			worlddata.loadDefaults();
 			settings.saveFile();
 			worlddata.saveWorldFile();
 			p.sendMessage(ChatColor.GREEN + "Default Manhunt settings loaded!");
 			HuntedPlugin.getInstance().log(Level.INFO, p.getName() + " restored the default Manhunt settings!");
 			return;
 		} else {
 			p.sendMessage(ChatColor.RED
 					+ "You don't have permission to do that!");
 			return;
 		}
 	}
 	
 	private void loadCommand(String[] args, Player p) {
 		if (p.isOp()) {
 			if (g.gameHasBegun()) {
 				p.sendMessage(ChatColor.RED + "You can't do that while the plugin is running!");
 				return;
 			}
 			settings.loadFile();
 			worlddata.loadWorldFile();
 			p.sendMessage(ChatColor.GREEN + "Settings file reloaded.");
 			HuntedPlugin.getInstance().log(Level.INFO, p.getName() + " reloaded the Manhunt settings.");
 			return;
 		} else {
 			p.sendMessage(ChatColor.RED
 					+ "You don't have permission to do that!");
 			return;
 		}
 	}
 	
 	private void weatherCommand(String[] args, Player p) {
 		if (p.isOp()) {
 			if (args.length == 0) {
 				p.sendMessage(ChatColor.RED
 						+ "You must choose a weather: sunny, rain, or storm.");
 				return;
 			}
 			if (args[0].equalsIgnoreCase("sunny")
 					|| args[0].equalsIgnoreCase("norain")
 					|| args[0].equalsIgnoreCase("clear")) {
 				HuntedPlugin.getInstance().getWorld().setStorm(false);
 				HuntedPlugin.getInstance().getWorld().setThundering(false);
 				
 			} else if (args[0].equalsIgnoreCase("rain")
 					|| args[0].equalsIgnoreCase("rainy")) {
 				HuntedPlugin.getInstance().getWorld().setStorm(true);
 				HuntedPlugin.getInstance().getWorld().setThundering(false);
 				
 			} else if (args[0].equalsIgnoreCase("storm")
 					|| args[0].equalsIgnoreCase("stormy")
 					|| args[0].equalsIgnoreCase("lightning")
 					|| args[0].equalsIgnoreCase("thunder")) {
 				HuntedPlugin.getInstance().getWorld().setStorm(false);
 				HuntedPlugin.getInstance().getWorld().setThundering(false);
 				
 			} else {
 				p.sendMessage(ChatColor.RED
 						+ "You must choose a weather: sunny, rainy, or stormy.");
 				return;
 			}
 			p.sendMessage(ChatColor.GREEN + "Weather set to " + args[0]);
 			HuntedPlugin.getInstance().log(Level.INFO, p.getName() + " set the Manhunt world weather to " + args[0]);
 		} else {
 			p.sendMessage(ChatColor.RED
 					+ "You don't have permission to do that!");
 		}
 	}
 	
 	private void settingsCommand(String[] args, Player p) {
 		if (args.length == 0
 				|| (args.length == 1 && args[0].equalsIgnoreCase("1"))) {
 			p.sendMessage(ChatColor.GOLD
 					+ "Available manhunt settings: (1/3)");
 			if (settings.mustBeOp()) {
 				p.sendMessage(ChatColor.BLUE
 						+ "mustBeOp "
 						+ ChatColor.GREEN
 						+ "[true]"
 						+ ChatColor.WHITE
 						+ " Players need op permissions to any manhunt commands.");
 			} else {
 				p.sendMessage(ChatColor.BLUE
 						+ "mustBeOp "
 						+ ChatColor.RED
 						+ "[false]"
 						+ ChatColor.WHITE
 						+ " Players can choose their team and warp to world spawn.");
 			}
 			/*if (settings.easyCommands()) {
 				p.sendMessage(ChatColor.BLUE
 						+ "easyCommands "
 						+ ChatColor.GREEN
 						+ "[true]"
 						+ ChatColor.WHITE
 						+ " You do not need to prefix every command with /manhunt.");
 			} else {
 				p.sendMessage(ChatColor.BLUE
 						+ "easyCommands "
 						+ ChatColor.RED
 						+ "[false]"
 						+ ChatColor.WHITE
 						+ " You must prefex every command with /manhunt.");
 			}*/
 			if (settings.allTalk()) {
 				p.sendMessage(ChatColor.BLUE + "allTalk " + ChatColor.GREEN
 						+ "[true]" + ChatColor.WHITE
 						+ " Teams can see eachother's chat.");
 			} else {
 				p.sendMessage(ChatColor.BLUE
 						+ "allTalk "
 						+ ChatColor.RED
 						+ "[false]"
 						+ ChatColor.WHITE
 						+ " Hunters, prey, and spectators can't chat with eachother.");
 			}
 			if (settings.spawnPassive()) {
 				p.sendMessage(ChatColor.BLUE + "spawnPassive "
 						+ ChatColor.GREEN + "[true]" + ChatColor.WHITE
 						+ " Passive mobs will spawn.");
 			} else {
 				p.sendMessage(ChatColor.BLUE + "spawnPassive "
 						+ ChatColor.RED + "[false]" + ChatColor.WHITE
 						+ " Passive mobs will not spawn.");
 			}
 			if (settings.spawnHostile()) {
 				p.sendMessage(ChatColor.BLUE + "spawnHostile "
 						+ ChatColor.GREEN + "[true]" + ChatColor.WHITE
 						+ " Hostile mobs will spawn.");
 			} else {
 				p.sendMessage(ChatColor.BLUE + "spawnHostile "
 						+ ChatColor.RED + "[false]" + ChatColor.WHITE
 						+ " Hostile mobs will not spawn.");
 			}
 			if (settings.envDeath()) {
 				p.sendMessage(ChatColor.BLUE
 						+ "envDeath "
 						+ ChatColor.GREEN
 						+ "[true]"
 						+ ChatColor.WHITE
 						+ " Players can die from mobs and enviromental hazards.");
 			} else {
 				p.sendMessage(ChatColor.BLUE
 						+ "envDeath "
 						+ ChatColor.RED
 						+ "[false]"
 						+ ChatColor.WHITE
 						+ " Players can be damaged by environment, but never die.");
 			}
 			if (settings.envHunterRespawn()) {
 				p.sendMessage(ChatColor.BLUE + "envHunterRespawn "
 						+ ChatColor.GREEN + "[true]" + ChatColor.WHITE
 						+ " Hunters respawn from enviromental death.");
 			} else {
 				p.sendMessage(ChatColor.BLUE + "envHunterRespawn "
 						+ ChatColor.RED + "[false]" + ChatColor.WHITE
 						+ " Hunters are eliminated by enviromental death.");
 			}
 			if (settings.envPreyRespawn()) {
 				p.sendMessage(ChatColor.BLUE + "envPreyRespawn "
 						+ ChatColor.GREEN + "[true]" + ChatColor.WHITE
 						+ " The Prey respawn from enviromental death.");
 			} else {
 				p.sendMessage(ChatColor.BLUE + "envPreyRespawn "
 						+ ChatColor.RED + "[false]" + ChatColor.WHITE
 						+ " The Prey are eliminated by enviromental death.");
 			}
 
 		} else if (args.length == 1 && args[0].equalsIgnoreCase("2")) {
 			p.sendMessage(ChatColor.GOLD
 					+ "Available manhunt settings: (2/3)");
 			
 			if (settings.preyFinder()) {
 				p.sendMessage(ChatColor.BLUE
 						+ "preyFinder "
 						+ ChatColor.GREEN
 						+ "[true]"
 						+ ChatColor.WHITE
 						+ " Hunters can use a compass to find the direction to the nearest Prey.");
 			} else {
 				p.sendMessage(ChatColor.BLUE
 						+ "preyFinder "
 						+ ChatColor.RED
 						+ "[false]"
 						+ ChatColor.WHITE
 						+ " Hunters have to find the Prey the old-fashioned way.");
 			}
 			if (settings.friendlyFire()) {
 				p.sendMessage(ChatColor.BLUE + "friendlyFire "
 						+ ChatColor.GREEN + "[true]" + ChatColor.WHITE
 						+ " Players can kill their teammates.");
 			} else {
 				p.sendMessage(ChatColor.BLUE + "friendlyFire "
 						+ ChatColor.RED + "[false]" + ChatColor.WHITE
 						+ " Players can't hurt their teammates.");
 			}
 			if (settings.pvpInstantDeath()) {
 				p.sendMessage(ChatColor.BLUE + "pvpInstantDeath "
 						+ ChatColor.GREEN + "[true]" + ChatColor.WHITE
 						+ " PvP damage causes instant death.");
 			} else {
 				p.sendMessage(ChatColor.BLUE + "pvpInstantDeath "
 						+ ChatColor.RED + "[false]" + ChatColor.WHITE
 						+ " PvP damage is vanilla.");
 			}
 			if (settings.flyingSpectators()) {
 				p.sendMessage(ChatColor.BLUE + "flyingSpectators "
 						+ ChatColor.GREEN + "[true]" + ChatColor.WHITE
 						+ " Spectators can fly.");
 			} else {
 				p.sendMessage(ChatColor.BLUE + "flyingSpectators "
 						+ ChatColor.RED + "[false]" + ChatColor.WHITE
 						+ " Spectators are earth-bound.");
 			}
 			if (settings.loadouts()) {
 				p.sendMessage(ChatColor.BLUE + "loadouts "
 						+ ChatColor.GREEN + "[true]" + ChatColor.WHITE
 						+ " Players will start with a pre-set inventory.");
 			} else {
 				p.sendMessage(ChatColor.BLUE + "loadouts " + ChatColor.RED
 						+ "[false]" + ChatColor.WHITE
 						+ " Players' inventorys will not be reset.");
 			}
 			if (settings.teamHats()) {
 				p.sendMessage(ChatColor.BLUE + "teamHats "
 						+ ChatColor.GREEN + "[true]" + ChatColor.WHITE
 						+ " Teams get identifying headwear.");
 			} else {
 				p.sendMessage(ChatColor.BLUE + "teamHats " + ChatColor.RED
 						+ "[false]" + ChatColor.WHITE
 						+ " No fancy headwear for you!");
 			}
 			if (settings.northCompass()) {
 				p.sendMessage(ChatColor.BLUE + "northCompass "
 						+ ChatColor.GREEN + "[true]" + ChatColor.WHITE
 						+ " Hunters' compasses always point North.");
 			} else {
 				p.sendMessage(ChatColor.BLUE + "northCompass " + ChatColor.RED
 						+ "[false]" + ChatColor.WHITE
 						+ " Hunters' compasses point to the world spawn.");
 			}
 			if (worlddata.boxBoundry()) {
 				p.sendMessage(ChatColor.BLUE + "boxBoundry "
 						+ ChatColor.GREEN + "[true]" + ChatColor.WHITE
 						+ " The world boundry is box-shaped");
 			} else {
 				p.sendMessage(ChatColor.BLUE + "boxBoundry " + ChatColor.RED
 						+ "[false]" + ChatColor.WHITE
 						+ " The world boundry is eliptical.");
 			}
 
 		} else if (args.length == 1 && args[0].equalsIgnoreCase("3")) {
 			p.sendMessage(ChatColor.GOLD
 					+ "Available manhunt settings: (3/3)");
 			if (settings.autoHunter()) {
 				p.sendMessage(ChatColor.BLUE + "autoHunter "
 						+ ChatColor.GREEN + "[true]" + ChatColor.WHITE
 						+ " New players are automatically Hunters.");
 			} else {
 				p.sendMessage(ChatColor.BLUE + "autoHunter "
 						+ ChatColor.RED + "[false]" + ChatColor.WHITE
 						+ " New players are only Spectators.");
 			}
 			p.sendMessage(ChatColor.BLUE + "dayLimit " + ChatColor.GREEN
 					+ "[" + settings.dayLimit() + "]" + ChatColor.WHITE
 					+ " How many Minecraft days the game lasts.");
 			if (settings.offlineTimeout() >= 0) {
 				p.sendMessage(ChatColor.BLUE
 						+ "offlineTimeout "
 						+ ChatColor.GREEN
 						+ "["
 						+ settings.offlineTimeout()
 						+ "]"
 						+ ChatColor.WHITE
 						+ " How long absent players have till they're disqualified.");
 			} else {
 				p.sendMessage(ChatColor.BLUE + "offlineTimeout "
 						+ ChatColor.RED + "[off]" + ChatColor.WHITE
 						+ " Players won't be kicked when logging off.");
 			}
 			if (settings.prepTime() > 0) {
 				p.sendMessage(ChatColor.BLUE
 						+ "prepTime "
 						+ ChatColor.GREEN
 						+ "["
 						+ settings.prepTime()
 						+ "]"
 						+ ChatColor.WHITE
 						+ " Minutes until the Hunters are released.");
 			} else {
 				p.sendMessage(ChatColor.BLUE + "prepTime "
 						+ ChatColor.RED + "[off]" + ChatColor.WHITE
 						+ " Pre-game preparation is disabled.");
 			}
 			if (worlddata.mapBoundry() >= 0) {
 				p.sendMessage(ChatColor.BLUE
 						+ "mapBoundry "
 						+ ChatColor.GREEN
 						+ "["
 						+ worlddata.mapBoundry()
 						+ "]"
 						+ ChatColor.WHITE
 						+ " Blocks from spawn players are allowed to venture.");
 			} else {
 				p.sendMessage(ChatColor.BLUE + "mapBoundry "
 						+ ChatColor.RED + "[off]" + ChatColor.WHITE
 						+ " Players can venture out indefinately.");
 			}
 			if (worlddata.pregameBoundry() >= 0) {
 				p.sendMessage(ChatColor.BLUE + "pregameBoundry "
 						+ ChatColor.GREEN + "[" + worlddata.pregameBoundry()
 						+ "]" + ChatColor.WHITE
 						+ " Blocks from spawn hunters are confined to.");
 			} else {
 				p.sendMessage(ChatColor.BLUE + "pregameBoundry "
 						+ ChatColor.RED + "[off]" + ChatColor.WHITE
 						+ " Hunters are not confined to spawn.");
 			}
 			if (worlddata.noBuildRange() >= 0) {
 				p.sendMessage(ChatColor.BLUE + "noBuildRange "
 						+ ChatColor.GREEN + "[" + worlddata.noBuildRange()
 						+ "]" + ChatColor.WHITE
 						+ " Hunter/Prey spawn points are protected.");
 			} else {
 				p.sendMessage(ChatColor.BLUE + "noBuildRange "
 						+ ChatColor.RED + "[off]" + ChatColor.WHITE
 						+ " Huners and Prey can build anywhere.");
 			}
 			if (settings.locatorTimer() > 0) {
 				p.sendMessage(ChatColor.BLUE + "locatorTimer "
 						+ ChatColor.GREEN + "[" + settings.locatorTimer()
 						+ "]" + ChatColor.WHITE
 						+ " Seconds between Prey Finder 9001 uses.");
 			} else {
 				p.sendMessage(ChatColor.BLUE + "locatorTimer "
 						+ ChatColor.RED + "[off]" + ChatColor.WHITE
 						+ " Hunters need not wait to use the Prey Finder 9001.");
 			}
 			
 		} else if (!p.isOp()) {
 			p.sendMessage(ChatColor.RED
 					+ "Only ops can change manhunt game settings!");
 			return;
 		} else if (g.gameHasBegun()) {
 			p.sendMessage(ChatColor.RED + "You can't change settings while the Manhunt game is in progress!");
 			return;
 		}
 		
 		else if (args.length >= 1) {
 
 			if (args[0].equalsIgnoreCase("mustbeop")) {
 				if (args.length == 1) {
 					if (settings.mustBeOp()) {
 						p.sendMessage(ChatColor.BLUE
 								+ "mustBeOp "
 								+ ChatColor.GREEN
 								+ "[true]"
 								+ ChatColor.WHITE
 								+ " Players need op permissions to any manhunt commands.");
 					} else {
 						p.sendMessage(ChatColor.BLUE
 								+ "mustBeOp "
 								+ ChatColor.RED
 								+ "[false]"
 								+ ChatColor.WHITE
 								+ " Players can choose their team and warp to world spawn.");
 					}
 					return;
 				}
 				if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("true")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("on"))) {
 					settings.changeSetting("mustBeOp", "true");
 					p.sendMessage(ChatColor.BLUE
 							+ "mustBeOp "
 							+ ChatColor.GREEN
 							+ "[true]"
 							+ ChatColor.WHITE
 							+ " Players need op permissions to any manhunt commands.");
 
 				} else if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("false")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("off"))) {
 					settings.changeSetting("mustBeOp", "false");
 					p.sendMessage(ChatColor.BLUE
 							+ "mustBeOp "
 							+ ChatColor.RED
 							+ "[false]"
 							+ ChatColor.WHITE
 							+ " Players can choose their team and warp to world spawn.");
 				}
 				
 			/*} else if (args[0].equalsIgnoreCase("easycommands")) {
 				if (args.length == 1) {
 					if (settings.easyCommands()) {
 						p.sendMessage(ChatColor.BLUE
 								+ "easyCommands "
 								+ ChatColor.GREEN
 								+ "[true]"
 								+ ChatColor.WHITE
 								+ " You do not need to prefix every command with /manhunt.");
 					} else {
 						p.sendMessage(ChatColor.BLUE
 								+ "easyCommands "
 								+ ChatColor.RED
 								+ "[false]"
 								+ ChatColor.WHITE
 								+ " You must prefex every command with /manhunt.");
 					}
 					return;
 				}
 				if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("true")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("on"))) {
 					settings.changeSetting("easyCommands", "true");
 					p.sendMessage(ChatColor.BLUE
 							+ "easyCommands "
 							+ ChatColor.GREEN
 							+ "[true]"
 							+ ChatColor.WHITE
 							+ " You do not need to prefix every command with /manhunt.");
 
 				} else if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("false")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("off"))) {
 					settings.changeSetting("easyCommands", "false");
 					p.sendMessage(ChatColor.BLUE
 							+ "easyCommands "
 							+ ChatColor.RED
 							+ "[false]"
 							+ ChatColor.WHITE
 							+ " You must prefex every command with /manhunt.");
 				}*/
 
 			} else if (args[0].equalsIgnoreCase("alltalk")) {
 				if (args.length == 1) {
 					if (settings.allTalk()) {
 						p.sendMessage(ChatColor.BLUE + "allTalk "
 								+ ChatColor.GREEN + "[true]"
 								+ ChatColor.WHITE
 								+ " Teams can see eachother's chat.");
 					} else {
 						p.sendMessage(ChatColor.BLUE
 								+ "allTalk "
 								+ ChatColor.RED
 								+ "[false]"
 								+ ChatColor.WHITE
 								+ " Hunters, prey, and spectators can't chat with eachother.");
 					}
 					return;
 				}
 				if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("true")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("on"))) {
 					settings.changeSetting("allTalk", "true");
 					p.sendMessage(ChatColor.BLUE + "allTalk "
 							+ ChatColor.GREEN + "[true]" + ChatColor.WHITE
 							+ " Teams can see eachother's chat.");
 
 				} else if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("false")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("off"))) {
 					settings.changeSetting("allTalk", "false");
 					p.sendMessage(ChatColor.BLUE
 							+ "allTalk "
 							+ ChatColor.RED
 							+ "[false]"
 							+ ChatColor.WHITE
 							+ " Hunters, prey, and spectators can't chat with eachother.");
 				}
 
 			} else if (args[0].equalsIgnoreCase("spawnpassive")) {
 				if (args.length == 1) {
 					if (settings.spawnPassive()) {
 						p.sendMessage(ChatColor.BLUE + "spawnPassive "
 								+ ChatColor.GREEN + "[true]"
 								+ ChatColor.WHITE
 								+ " Passive mobs will spawn.");
 					} else {
 						p.sendMessage(ChatColor.BLUE + "spawnPassive "
 								+ ChatColor.RED + "[false]"
 								+ ChatColor.WHITE
 								+ " Passive mobs will not spawn.");
 					}
 					return;
 				}
 				if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("true")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("on"))) {
 					settings.changeSetting("spawnPassive", "true");
 					p.sendMessage(ChatColor.BLUE + "spawnPassive "
 							+ ChatColor.GREEN + "[true]" + ChatColor.WHITE
 							+ " Passive mobs will spawn.");
 
 				} else if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("false")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("off"))) {
 					settings.changeSetting("spawnPassive", "false");
 					p.sendMessage(ChatColor.BLUE + "spawnPassive "
 							+ ChatColor.RED + "[false]" + ChatColor.WHITE
 							+ " Passive mobs will not spawn.");
 				}
 
 			} else if (args[0].equalsIgnoreCase("spawnhostile")) {
 				if (args.length == 1) {
 					if (settings.spawnHostile()) {
 						p.sendMessage(ChatColor.BLUE + "spawnHostile "
 								+ ChatColor.GREEN + "[true]"
 								+ ChatColor.WHITE
 								+ " Hostile mobs will spawn.");
 					} else {
 						p.sendMessage(ChatColor.BLUE + "spawnHostile "
 								+ ChatColor.RED + "[false]"
 								+ ChatColor.WHITE
 								+ " Hostile mobs will not spawn.");
 					}
 					return;
 				}
 				if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("true")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("on"))) {
 					settings.changeSetting("spawnHostile", "true");
 					p.sendMessage(ChatColor.BLUE + "spawnHostile "
 							+ ChatColor.GREEN + "[true]" + ChatColor.WHITE
 							+ " Hostile mobs will spawn.");
 
 				} else if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("false")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("off"))) {
 					settings.changeSetting("spawnHostile", "false");
 					p.sendMessage(ChatColor.BLUE + "spawnHostile "
 							+ ChatColor.RED + "[false]" + ChatColor.WHITE
 							+ " Hostile mobs will not spawn.");
 				}
 
 			} else if (args[0].equalsIgnoreCase("envdeath")) {
 				if (args.length == 1) {
 					if (settings.envDeath()) {
 						p.sendMessage(ChatColor.BLUE
 								+ "envDeath "
 								+ ChatColor.GREEN
 								+ "[true]"
 								+ ChatColor.WHITE
 								+ " Players can die from mobs and enviromental hazards.");
 					} else {
 						p.sendMessage(ChatColor.BLUE
 								+ "envDeath "
 								+ ChatColor.RED
 								+ "[false]"
 								+ ChatColor.WHITE
 								+ " Players can be damaged by environment, but never die.");
 					}
 					return;
 				}
 				if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("true")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("on"))) {
 					settings.changeSetting("envDeath", "true");
 					p.sendMessage(ChatColor.BLUE
 							+ "envDeath "
 							+ ChatColor.GREEN
 							+ "[true]"
 							+ ChatColor.WHITE
 							+ " Players can die from mobs and enviromental hazards.");
 
 				} else if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("false")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("off"))) {
 					settings.changeSetting("envDeath", "false");
 					p.sendMessage(ChatColor.BLUE
 							+ "envDeath "
 							+ ChatColor.RED
 							+ "[false]"
 							+ ChatColor.WHITE
 							+ " Players can be damaged by environment, but never die.");
 				}
 
 			} else if (args[0].equalsIgnoreCase("envhunterrespawn")) {
 				if (args.length == 1) {
 					if (settings.envHunterRespawn()) {
 						p.sendMessage(ChatColor.BLUE
 								+ "envHunterRespawn "
 								+ ChatColor.GREEN
 								+ "[true]"
 								+ ChatColor.WHITE
 								+ " Hunters respawn from enviromental death.");
 					} else {
 						p.sendMessage(ChatColor.BLUE
 								+ "envHunterRespawn "
 								+ ChatColor.RED
 								+ "[false]"
 								+ ChatColor.WHITE
 								+ " Hunters are eliminated by enviromental death.");
 					}
 					return;
 				}
 				if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("true")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("on"))) {
 					settings.changeSetting("envHunterRespawn", "true");
 					p.sendMessage(ChatColor.BLUE + "envHunterRespawn "
 							+ ChatColor.GREEN + "[true]" + ChatColor.WHITE
 							+ " Hunters respawn from enviromental death.");
 
 				} else if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("false")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("off"))) {
 					settings.changeSetting("envHunterRespawn", "false");
 					p.sendMessage(ChatColor.BLUE
 							+ "envHunterRespawn "
 							+ ChatColor.RED
 							+ "[false]"
 							+ ChatColor.WHITE
 							+ " Hunters are eliminated by enviromental death.");
 				}
 
 			} else if (args[0].equalsIgnoreCase("envpreyrespawn")) {
 				if (args.length == 1) {
 					if (settings.envPreyRespawn()) {
 						p.sendMessage(ChatColor.BLUE
 								+ "envPreyRespawn "
 								+ ChatColor.GREEN
 								+ "[true]"
 								+ ChatColor.WHITE
 								+ " The Prey respawn from enviromental death.");
 					} else {
 						p.sendMessage(ChatColor.BLUE
 								+ "envPreyRespawn "
 								+ ChatColor.RED
 								+ "[false]"
 								+ ChatColor.WHITE
 								+ " The Prey are eliminated by enviromental death.");
 					}
 					return;
 				}
 				if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("true")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("on"))) {
 					settings.changeSetting("envPreyRespawn", "true");
 					p.sendMessage(ChatColor.BLUE + "envPreyRespawn "
 							+ ChatColor.GREEN + "[true]" + ChatColor.WHITE
 							+ " The Prey respawn from enviromental death.");
 
 				} else if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("false")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("off"))) {
 					settings.changeSetting("envPreyRespawn", "false");
 					p.sendMessage(ChatColor.BLUE
 							+ "envPreyRespawn "
 							+ ChatColor.RED
 							+ "[false]"
 							+ ChatColor.WHITE
 							+ " The Prey are eliminated by enviromental death.");
 				}
 
 			} else if (args[0].equalsIgnoreCase("preyfinder")) {
 				if (args.length == 1) {
 					if (settings.envPreyRespawn()) {
 						p.sendMessage(ChatColor.BLUE
 								+ "preyFinder "
 								+ ChatColor.GREEN
 								+ "[true]"
 								+ ChatColor.WHITE
 								+ " Hunters can use a compass to find the direction to the nearest Prey.");
 					} else {
 						p.sendMessage(ChatColor.BLUE
 								+ "preyFinder "
 								+ ChatColor.RED
 								+ "[false]"
 								+ ChatColor.WHITE
 								+ " Hunters have to find the Prey the old-fashioned way.");
 					}
 					return;
 				}
 				if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("true")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("on"))) {
 					settings.changeSetting("preyFinder", "true");
 					p.sendMessage(ChatColor.BLUE
 							+ "preyFinder "
 							+ ChatColor.GREEN
 							+ "[true]"
 							+ ChatColor.WHITE
 							+ " Hunters can use a compass to find the direction to the nearest Prey.");
 
 				} else if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("false")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("off"))) {
 					settings.changeSetting("preyFinder", "false");
 					p.sendMessage(ChatColor.BLUE
 							+ "preyFinder "
 							+ ChatColor.RED
 							+ "[false]"
 							+ ChatColor.WHITE
 							+ " Hunters have to find the Prey the old-fashioned way.");
 				}
 
 			} else if (args[0].equalsIgnoreCase("northCompass")) {
 				if (args.length == 1) {
 					if (settings.northCompass()) {
 						p.sendMessage(ChatColor.BLUE
 								+ "northCompass "
 								+ ChatColor.GREEN
 								+ "[true]"
 								+ ChatColor.WHITE
 								+ " Hunters' compasses only point North.");
 					} else {
 						p.sendMessage(ChatColor.BLUE
 								+ "northCompass "
 								+ ChatColor.RED
 								+ "[false]"
 								+ ChatColor.WHITE
 								+ " Hunters' compasses point to the world spawn.");
 					}
 					return;
 				}
 				if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("true")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("on"))) {
 					settings.changeSetting("northCompass", "true");
 					p.sendMessage(ChatColor.BLUE
 							+ "northCompass "
 							+ ChatColor.GREEN
 							+ "[true]"
 							+ ChatColor.WHITE
 							+ " Hunters' compasses always point North.");
 
 				} else if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("false")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("off"))) {
 					settings.changeSetting("northCompass", "false");
 					p.sendMessage(ChatColor.BLUE
 							+ "northCompass "
 							+ ChatColor.RED
 							+ "[false]"
 							+ ChatColor.WHITE
 							+ " Hunters' compasses point to the world spawn.");
 				}
 
 			} else if (args[0].equalsIgnoreCase("friendlyfire")) {
 				if (args.length == 1) {
 					if (settings.friendlyFire()) {
 						p.sendMessage(ChatColor.BLUE + "friendlyFire "
 								+ ChatColor.GREEN + "[true]"
 								+ ChatColor.WHITE
 								+ " Players can kill their teammates.");
 					} else {
 						p.sendMessage(ChatColor.BLUE + "friendlyFire "
 								+ ChatColor.RED + "[false]"
 								+ ChatColor.WHITE
 								+ " Players can't hurt their teammates.");
 					}
 					return;
 				}
 				if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("true")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("on"))) {
 					settings.changeSetting("friendlyFire", "true");
 					p.sendMessage(ChatColor.BLUE + "friendlyFire "
 							+ ChatColor.GREEN + "[true]" + ChatColor.WHITE
 							+ " Players can kill their teammates.");
 
 				} else if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("false")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("off"))) {
 					settings.changeSetting("friendlyFire", "false");
 					p.sendMessage(ChatColor.BLUE + "friendlyFire "
 							+ ChatColor.RED + "[false]" + ChatColor.WHITE
 							+ " Players can't hurt their teammates.");
 				}
 
 			} else if (args[0].equalsIgnoreCase("pvpinstantdeath")) {
 				if (args.length == 1) {
 					if (settings.pvpInstantDeath()) {
 						p.sendMessage(ChatColor.BLUE + "pvpInstantDeath "
 								+ ChatColor.GREEN + "[true]"
 								+ ChatColor.WHITE
 								+ " PvP damage causes instant death.");
 					} else {
 						p.sendMessage(ChatColor.BLUE + "pvpInstantDeath "
 								+ ChatColor.RED + "[false]"
 								+ ChatColor.WHITE
 								+ " PvP damage is vanilla.");
 					}
 					return;
 				}
 				if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("true")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("on"))) {
 					settings.changeSetting("pvpInstantDeath", "true");
 					p.sendMessage(ChatColor.BLUE + "pvpInstantDeath "
 							+ ChatColor.GREEN + "[true]" + ChatColor.WHITE
 							+ " PvP damage causes instant death.");
 
 				} else if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("false")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("off"))) {
 					settings.changeSetting("pvpInstantDeath", "false");
 					p.sendMessage(ChatColor.BLUE + "pvpInstantDeath "
 							+ ChatColor.RED + "[false]" + ChatColor.WHITE
 							+ " PvP damage is vanilla.");
 				}
 
 			} else if (args[0].equalsIgnoreCase("flyingspectators")) {
 				if (args.length == 1) {
 					if (settings.flyingSpectators()) {
 						p.sendMessage(ChatColor.BLUE + "flyingSpectators "
 								+ ChatColor.GREEN + "[true]"
 								+ ChatColor.WHITE
 								+ " Spectators can fly.");
 					} else {
 						p.sendMessage(ChatColor.BLUE + "flyingSpectators "
 								+ ChatColor.RED + "[false]"
 								+ ChatColor.WHITE
 								+ " Spectators are earth-bound.");
 					}
 					return;
 				}
 				if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("true")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("on"))) {
 					settings.changeSetting("flyingSpectators", "true");
 					p.sendMessage(ChatColor.BLUE + "flyingSpectators "
 							+ ChatColor.GREEN + "[true]" + ChatColor.WHITE
 							+ " Spectators can fly.");
 
 				} else if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("false")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("off"))) {
 					settings.changeSetting("flyingSpectators", "false");
 					p.sendMessage(ChatColor.BLUE + "flyingSpectators "
 							+ ChatColor.RED + "[false]" + ChatColor.WHITE
 							+ " Spectators are earth-bound.");
 				}
 
 			} else if (args[0].equalsIgnoreCase("autohunter")) {
 				if (args.length == 1) {
 					if (settings.autoHunter()) {
 						p.sendMessage(ChatColor.BLUE + "autoHunter "
 								+ ChatColor.GREEN + "[true]"
 								+ ChatColor.WHITE
 								+ " New players are automatically Hunters.");
 					} else {
 						p.sendMessage(ChatColor.BLUE + "autoHunter "
 								+ ChatColor.RED + "[false]"
 								+ ChatColor.WHITE
 								+ " New players are only Spectators.");
 					}
 					return;
 				}
 				if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("true")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("on"))) {
 					settings.changeSetting("autoHunter", "true");
 					p.sendMessage(ChatColor.BLUE + "autoHunter "
 							+ ChatColor.GREEN + "[true]" + ChatColor.WHITE
 							+ " New players are automatically Hunters.");
 
 				} else if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("false")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("off"))) {
 					settings.changeSetting("autoHunter", "false");
 					p.sendMessage(ChatColor.BLUE + "autoHunter "
 							+ ChatColor.RED + "[false]" + ChatColor.WHITE
 							+ " New players are only Spectators.");
 				}
 
 			} else if (args[0].equalsIgnoreCase("loadouts")) {
 				if (args.length == 1) {
 					if (settings.loadouts()) {
 						p.sendMessage(ChatColor.BLUE
 								+ "loadouts "
 								+ ChatColor.GREEN
 								+ "[true]"
 								+ ChatColor.WHITE
 								+ " Players will start with a pre-set inventory.");
 					} else {
 						p.sendMessage(ChatColor.BLUE + "loadouts "
 								+ ChatColor.RED + "[false]"
 								+ ChatColor.WHITE
 								+ " Players' inventorys will not be reset.");
 					}
 					return;
 				}
 				if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("true")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("on"))) {
 					settings.changeSetting("loadouts", "true");
 					p.sendMessage(ChatColor.BLUE
 							+ "loadouts "
 							+ ChatColor.GREEN
 							+ "[true]"
 							+ ChatColor.WHITE
 							+ " Players will start with a pre-set inventory.");
 
 				} else if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("false")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("off"))) {
 					settings.changeSetting("loadouts", "false");
 					p.sendMessage(ChatColor.BLUE + "loadouts "
 							+ ChatColor.RED + "[false]" + ChatColor.WHITE
 							+ " Players' inventorys will not be reset.");
 				}
 
 			} else if (args[0].equalsIgnoreCase("teamhats")) {
 				if (args.length == 1) {
 					if (settings.teamHats()) {
 						p.sendMessage(ChatColor.BLUE + "teamHats "
 								+ ChatColor.GREEN + "[true]"
 								+ ChatColor.WHITE
 								+ " Teams get identifying wool hats.");
 					} else {
 						p.sendMessage(ChatColor.BLUE + "teamHats "
 								+ ChatColor.RED + "[false]"
 								+ ChatColor.WHITE
 								+ " No identifying wool hats for you!");
 					}
 					return;
 				}
 				if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("true")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("on"))) {
 					settings.changeSetting("teamHats", "true");
 					p.sendMessage(ChatColor.BLUE + "teamHats "
 							+ ChatColor.GREEN + "[true]" + ChatColor.WHITE
 							+ " Teams get identifying wool hats.");
 
 				} else if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("false")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("off"))) {
 					settings.changeSetting("teamHats", "false");
 					p.sendMessage(ChatColor.BLUE + "teamHats "
 							+ ChatColor.RED + "[false]" + ChatColor.WHITE
 							+ " No identifying wool hats for you!");
 				}
 				
 			} else if (args[0].equalsIgnoreCase("boxboundry")) {
 				if (args.length == 1) {
 					if (worlddata.boxBoundry()) {
 						p.sendMessage(ChatColor.BLUE
 								+ "boxBoundry " + ChatColor.GREEN
 								+ "[true]" + ChatColor.WHITE
 								+ " The world boundry is box-shaped");
 					} else {
 						p.sendMessage(ChatColor.BLUE
 								+ "boxBoundry " + ChatColor.RED
 								+ "[false]" + ChatColor.WHITE
 								+ " The world boundry is eliptical.");
 					}
 					return;
 				}
 				if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("true")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("on"))) {
 					worlddata.changeSetting("boxBoundry", "true");
 					p.sendMessage(ChatColor.BLUE
 							+ "boxBoundry "
 							+ ChatColor.GREEN
 							+ "[true]"
 							+ ChatColor.WHITE
 							+ " The world boundry is box-shaped");
 
 				} else if (args.length == 2 &&
 						(args[1].equalsIgnoreCase("false")
 						|| args[1].equalsIgnoreCase("1")
 						|| args[1].equalsIgnoreCase("off"))) {
 					worlddata.changeSetting("boxBoundry", "false");
 					p.sendMessage(ChatColor.BLUE
 							+ "boxBoundry "
 							+ ChatColor.RED
 							+ "[false]"
 							+ ChatColor.WHITE
 							+ " The world boundry is eliptical.");
 				}
 			
 			} else if (args[0].equalsIgnoreCase("daylimit")) {
 				if (args.length >= 2) {
 					try {
 						int value = Integer.parseInt(args[1]);
 						if (value < 1) {
 							p.sendMessage(ChatColor.RED
 									+ "You must enter an number greater than 0!");
 						} else {
 							settings.changeSetting("dayLimit",
 									Integer.toString(value));
 							p.sendMessage(ChatColor.BLUE
 									+ "dayLimit "
 									+ ChatColor.GREEN
 									+ "["
 									+ settings.dayLimit()
 									+ "]"
 									+ ChatColor.WHITE
 									+ " How many Minecraft days the game lasts.");
 						}
 					} catch (NumberFormatException e) {
 						p.sendMessage(ChatColor.RED
 								+ "You must enter an INTEGER. (ie 1, 3, 5...)");
 					}
 				} else {
 					p.sendMessage(ChatColor.BLUE + "dayLimit "
 							+ ChatColor.GREEN + "[" + settings.dayLimit()
 							+ "]" + ChatColor.WHITE
 							+ " How many Minecraft days the game lasts.");
 				}
 
 			} else if (args[0].equalsIgnoreCase("offlinetimeout")) {
 				if (args.length >= 2) {
 					int value;
 					if (args[1].equalsIgnoreCase("off")
 							|| args[1].equalsIgnoreCase("disable")) {
 						value = -1;
 					} else {
 						try {
 							value = Integer.parseInt(args[1]);
 						} catch (NumberFormatException e) {
 							p.sendMessage(ChatColor.RED
 									+ "Invalid value. You must enter an Integer or \"OFF\"");
 							return;
 						}
 					}
 					if (value <= -1) {
 						settings.changeSetting("offlineTimeout", "-1");
 						p.sendMessage(ChatColor.BLUE
 								+ "offlineTimeout "
 								+ ChatColor.RED
 								+ "[off]"
 								+ ChatColor.WHITE
 								+ " Players won't be kicked when logging off.");
 					} else {
 						settings.changeSetting("offlineTimeout",
 								Integer.toString(value));
 						p.sendMessage(ChatColor.BLUE
 								+ "offlineTimeout "
 								+ ChatColor.GREEN
 								+ "["
 								+ settings.offlineTimeout()
 								+ "]"
 								+ ChatColor.WHITE
 								+ " How long absent players have till they're disqualified.");
 					}
 				} else {
 					p.sendMessage(ChatColor.BLUE
 							+ "offlineTimeout "
 							+ ChatColor.GREEN
 							+ "["
 							+ settings.offlineTimeout()
 							+ "]"
 							+ ChatColor.WHITE
 							+ " How long absent players have till they're disqualified.");
 				}
 
 			}  else if (args[0].equalsIgnoreCase("prepTime")) {
 				if (args.length >= 2) {
 					int value;
 					if (args[1].equalsIgnoreCase("off")
 							|| args[1].equalsIgnoreCase("disable")) {
 						value = 0;
 					} else {
 						try {
 							value = Integer.parseInt(args[1]);
 						} catch (NumberFormatException e) {
 							p.sendMessage(ChatColor.RED
 									+ "Invalid value. You must enter an Integer or \"OFF\"");
 							return;
 						}
 					}
 					if (value <= 0) {
 						settings.changeSetting("prepTime", "0");
 						p.sendMessage(ChatColor.BLUE
 								+ "prepTime "
 								+ ChatColor.RED
 								+ "[off]"
 								+ ChatColor.WHITE
 								+ " Pre-game preparation is disabled.");
 					} else {
 						if (value > 10) value = 10;
 						settings.changeSetting("prepTime",
 								Integer.toString(value));
 						p.sendMessage(ChatColor.BLUE
 								+ "prepTime "
 								+ ChatColor.GREEN
 								+ "["
 								+ settings.prepTime()
 								+ "]"
 								+ ChatColor.WHITE
 								+ " Minutes until the Hunters are released.");
 					}
 				} else {
 					p.sendMessage(ChatColor.BLUE
 							+ "prepTime "
 							+ ChatColor.GREEN
 							+ "["
 							+ settings.prepTime()
 							+ "]"
 							+ ChatColor.WHITE
 							+ " Minutes until the Hunters are released.");
 				}
 
 			} else if (args[0].equalsIgnoreCase("mapboundry")) {
 				if (args.length >= 2) {
 					int value;
 					if (args[1].equalsIgnoreCase("off")
 							|| args[1].equalsIgnoreCase("disable")) {
 						value = -1;
 					} else {
 						try {
 							value = Integer.parseInt(args[1]);
 						} catch (NumberFormatException e) {
 							p.sendMessage(ChatColor.RED
 									+ "Invalid value. You must enter an Integer or \"OFF\"");
 							return;
 						}
 					}
 					if (value <= -1) {
 						worlddata.changeSetting("mapBoundry", "-1");
 						p.sendMessage(ChatColor.BLUE + "mapBoundry "
 								+ ChatColor.RED + "[off]" + ChatColor.WHITE
 								+ "Players can venture out indefinately.");
 					} else if (value < 64) {
 						worlddata.changeSetting("mapBoundry", "64");
 						p.sendMessage(ChatColor.RED
 								+ "64 blocks is the minimum setting for this!");
 						p.sendMessage(ChatColor.BLUE
 								+ "mapBoundry "
 								+ ChatColor.GREEN
 								+ "["
 								+ worlddata.mapBoundry()
 								+ "]"
 								+ ChatColor.WHITE
 								+ " Blocks from spawn players are allowed to venture.");
 					} else {
 						worlddata.changeSetting("mapBoundry",
 								Integer.toString(value));
 						p.sendMessage(ChatColor.BLUE
 								+ "mapBoundry "
 								+ ChatColor.GREEN
 								+ "["
 								+ worlddata.mapBoundry()
 								+ "]"
 								+ ChatColor.WHITE
 								+ " Blocks from spawn players are allowed to venture.");
 					}
 				} else {
 					p.sendMessage(ChatColor.BLUE
 							+ "mapBoundry "
 							+ ChatColor.GREEN
 							+ "["
 							+ worlddata.mapBoundry()
 							+ "]"
 							+ ChatColor.WHITE
 							+ " Blocks from spawn players are allowed to venture.");
 					return;
 				}
 			} else if (args[0].equalsIgnoreCase("pregameboundry")) {
 				if (args.length >= 2) {
 					int value;
 					if (args[1].equalsIgnoreCase("off")
 							|| args[1].equalsIgnoreCase("disable")) {
 						value = -1;
 					} else {
 						try {
 							value = Integer.parseInt(args[1]);
 						} catch (NumberFormatException e) {
 							p.sendMessage(ChatColor.RED
 									+ "Invalid value. You must enter an Integer or \"OFF\"");
 							return;
 						}
 					}
 					if (value > 64) {
 						value = 64;
 						p.sendMessage(ChatColor.RED + "64 is the maximum setting for pregameBoundry!");
 					}
 					if (value <= -1) {
 						worlddata.changeSetting("pregameBoundry", "-1");
 						p.sendMessage(ChatColor.BLUE + "pregameBoundry "
 								+ ChatColor.RED + "[off]" + ChatColor.WHITE
 								+ "Hunters are not confined to spawn.");
 					} else {
 						worlddata.changeSetting("pregameBoundry",
 								Integer.toString(value));
 						p.sendMessage(ChatColor.BLUE
 								+ "pregameBoundry "
 								+ ChatColor.GREEN
 								+ "["
 								+ worlddata.pregameBoundry()
 								+ "]"
 								+ ChatColor.WHITE
 								+ " Blocks from spawn hunters are confined to.");
 					}
 				} else {
 					p.sendMessage(ChatColor.BLUE + "pregameBoundry "
 							+ ChatColor.GREEN + "["
 							+ worlddata.pregameBoundry() + "]"
 							+ ChatColor.WHITE
 							+ " Blocks from spawn hunters are confined to.");
 				}
 			} else if (args[0].equalsIgnoreCase("nobuildrange")) {
 				if (args.length >= 2) {
 					int value;
 					if (args[1].equalsIgnoreCase("off")
 							|| args[1].equalsIgnoreCase("disable")) {
 						value = -1;
 					} else {
 						try {
 							value = Integer.parseInt(args[1]);
 						} catch (NumberFormatException e) {
 							p.sendMessage(ChatColor.RED
 									+ "Invalid value. You must enter an Integer or \"OFF\"");
 							return;
 						}
 					}
 					if (value <= -1) {
 						worlddata.changeSetting("nobuildrange", "-1");
 						p.sendMessage(ChatColor.BLUE + "noBuildRange "
 								+ ChatColor.RED + "[off]" + ChatColor.WHITE
 								+ " Huners and Prey can build anywhere.");
 					} else {
 						worlddata.changeSetting("nobuildrange",
 								Integer.toString(value));
 						p.sendMessage(ChatColor.BLUE
 								+ "noBuildRange "
 								+ ChatColor.GREEN
 								+ "["
 								+ worlddata.noBuildRange()
 								+ "]"
 								+ ChatColor.WHITE
 								+ " Hunter/Prey spawn points are protected.");
 					}
 				} else {
 					p.sendMessage(ChatColor.BLUE + "noBuildRange "
 							+ ChatColor.GREEN + "[" + worlddata.noBuildRange()
 							+ "]" + ChatColor.WHITE
 							+ " Hunter/Prey spawn points are protected.");
 				}
 			} else if (args[0].equalsIgnoreCase("locatortimer")) {
 				if (args.length >= 2) {
 					int value;
 					if (args[1].equalsIgnoreCase("off")
 							|| args[1].equalsIgnoreCase("disable")) {
 						value = 0;
 					} else {
 						try {
 							value = Integer.parseInt(args[1]);
 						} catch (NumberFormatException e) {
 							p.sendMessage(ChatColor.RED
 									+ "Invalid value. You must enter an Integer or \"OFF\"");
 							return;
 						}
 					}
 					if (value <= -1) {
 						settings.changeSetting("locatorTimer", "0");
 						p.sendMessage(ChatColor.BLUE + "locatorTimer "
 								+ ChatColor.RED + "[off]" + ChatColor.WHITE
 								+ " Hunters need not wait to use the Prey Finder 9001.");
 					} else {
 						settings.changeSetting("locatorTimer",
 								Integer.toString(value));
 						p.sendMessage(ChatColor.BLUE
 								+ "locatorTimer "
 								+ ChatColor.GREEN
 								+ "["
 								+ settings.locatorTimer()
 								+ "]"
 								+ ChatColor.WHITE
 								+ " Seconds between Prey Finder 9001 uses.");
 					}
 				} else {
 					p.sendMessage(ChatColor.BLUE + "locatorTimer "
 							+ ChatColor.GREEN + "[" + settings.locatorTimer()
 							+ "]" + ChatColor.WHITE
 							+ " Seconds between Prey Finder 9001 uses.");
 				}
 			} else {
 				p.sendMessage(ChatColor.RED
 						+ "Invalid setting. Type \"/manhunt settings <pg>\" for a complete list.");
 			}
 		}
 	}
 	
 	private String[] shiftArgs(String[] args) {
 		String[] a = new String[args.length-1];
 		for (int i=1 ; i < args.length ; i++) {
 			a[i-1] = args[i];
 		}
 		return a;
 	}
 	
 	private String pre() {
 		return (ChatColor.YELLOW + "|" + ChatColor.WHITE + " ");
 	}
 	
 }
