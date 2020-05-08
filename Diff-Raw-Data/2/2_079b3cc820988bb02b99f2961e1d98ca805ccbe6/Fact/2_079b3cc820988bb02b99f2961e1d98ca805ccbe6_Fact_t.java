 package com.github.LeoVerto.Fact;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Fact extends JavaPlugin {
 	HashSet<Player>	playersIgnoring	= new HashSet<Player>();
 
 	@Override
 	public void onEnable() {
 		loadConfiguration();
 		autoFacts();
 	}
 
 	@Override
 	public void onDisable() {
 		getServer().getScheduler().cancelTasks(this);
 		playersIgnoring.clear();
 	}
 
 	public void loadConfiguration() {
 		reloadConfig();
 		getConfig().addDefault("Colors.PlayerFact.Fact", "'&7'");
 		getConfig().addDefault("Colors.PlayerFact.Text", "'&f'");
 		getConfig().addDefault("Colors.ConsoleFact.Fact", "'&6'");
 		getConfig().addDefault("Colors.ConsoleFact.Text", "'&f'");
 		getConfig().addDefault("Colors.AutoFact.Fact", "'&3'");
 		getConfig().addDefault("Colors.AutoFact.Text", "'&f'");
 		getConfig().addDefault("Messages.AutoFact.Delay", 5);
 		getConfig().addDefault("Messages.AutoFact.Facts",
 				Arrays.asList("This is a default autofact.", "You can change autofacts in /plugins/Fact/config.yml", "Default stuff is usually bad, so please change this!"));
 		getConfig().addDefault("Text.Fact", "Fact>");
 		getConfig().addDefault("Text.AutoFact", "AutoFact>");
 		getConfig().addDefault("Messages.Ignore.Ignoring", ("No longer displaying Fact messages!"));
 		getConfig().addDefault("Messages.Ignore.NotIgnoring", ("Now displaying Fact messages!"));
 		getConfig().addDefault("Messages.Reload", ("Reload complete!"));
 		getConfig().options().copyDefaults(true);
 		//Currently no header
 		//getConfig().options().copyHeader(true);
 		saveConfig();
 	}
 
 	public void autoFacts() {
 		final long autoFactDelay = (getConfig().getLong("Messages.AutoFact.Delay") * 1200);
 		final List<?> messages = getConfig().getList("Messages.AutoFact.Facts");
 		final int messageCount = messages.size();
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 			private int	messageNumber	= 0;
 
 			@Override
 			public void run() {
 				if (messageNumber < (messageCount)) {
 					sendFact((String) messages.get(messageNumber), "auto");
 					messageNumber++;
 				} else {
 					messageNumber = 0;
 					sendFact((String) messages.get(messageNumber), "auto");
 					messageNumber++;
 				}
 			}
 		}, 1200L, autoFactDelay);
 	}
 
 	public <player> void sendFact(final String message, final String type) {
 		final String FactColor = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Colors.PlayerFact.Fact").replace("'", ""));
 		final String TextColor = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Colors.PlayerFact.Text").replace("'", ""));
 		final String ConsoleFactColor = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Colors.ConsoleFact.Fact").replace("'", ""));
 		final String ConsoleTextColor = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Colors.ConsoleFact.Text").replace("'", ""));
 		final String AutoFactColor = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Colors.AutoFact.Fact").replace("'", ""));
 		final String AutoTextColor = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Colors.AutoFact.Text").replace("'", ""));
 		final String FactText = getConfig().getString("Text.Fact");
 		final String AutoFactText = getConfig().getString("Text.AutoFact");
 		final Player[] onlinePlayers = Bukkit.getServer().getOnlinePlayers();
 		for (int i = 0; i < onlinePlayers.length; i++) {
 			if (playersIgnoring.contains(Bukkit.getOnlinePlayers()[i]) == false) {
 				final Player player = onlinePlayers[i];
 				if (player.hasPermission("fact.receive")) {
 					if (type.equals("player")) {
 						onlinePlayers[i].sendMessage(FactColor + FactText + " " + TextColor + message);
 					} else if (type.equals("auto")) {
 						onlinePlayers[i].sendMessage(AutoFactColor + AutoFactText + " " + AutoTextColor + message);
 					} else {
 						onlinePlayers[i].sendMessage(ConsoleFactColor + FactText + " " + ConsoleTextColor + message);
 					}
 				}
 			}
 		}
 		if (!type.equals("auto")) {
 			getLogger().info(FactText + " " + message);
 		}
 	}
 
 	@Override
 	public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
 		if (cmd.getName().equalsIgnoreCase("fact")) {
 			//Reload command
 			if (args[0].equalsIgnoreCase("reload")) {
 				if ((sender instanceof Player)) {
 					final Player player = (Player) sender;
 					if (player.hasPermission("fact.reload")) {
 						loadConfiguration();
						getServer().getScheduler().cancelTasks(this);
						autoFacts();
 						player.sendMessage(getConfig().getString("Messages.Reload"));
 						getLogger().info(getConfig().getString("Messages.Reload"));
 						return true;
 					} else {
 						player.sendMessage(this.getCommand("fact").getPermissionMessage());
 						return false;
 					}
 				} else {
 					loadConfiguration();
 					getLogger().info(getConfig().getString("Messages.Reload"));
 					return true;
 				}
 			//Ignore command
 			} else if (args[0].equalsIgnoreCase("ignore")) {
 				if ((sender instanceof Player)) {
 					final Player player = (Player) sender;
 					if (player.hasPermission("fact.ignore")) {
 						if (playersIgnoring.contains(player) == false) {
 							playersIgnoring.add(player);
 							player.sendMessage(getConfig().getString("Messages.Ignore.Ignoring"));
 						} else {
 							playersIgnoring.remove(player);
 							player.sendMessage(getConfig().getString("Messages.Ignore.NotIgnoring"));
 						}
 						return true;
 					} else {
 						player.sendMessage(this.getCommand("fact").getPermissionMessage());
 						return false;
 					}
 				} else {
 					sender.sendMessage("You can only execute this command as a player!");
 					return true;
 				}
 			//Normal facts
 			} else {
 				if ((sender instanceof Player)) {
 					final Player player = (Player) sender;
 					if (player.hasPermission("fact.fact")) {
 						String message = "";
 						for (int i = 0; i < args.length; i++) {
 							message = (message + " " + args[i]);
 						}
 						sendFact(message, "player");
 						return true;
 					} else {
 						player.sendMessage(this.getCommand("fact").getPermissionMessage());
 						return false;
 					}
 				} else {
 					String message = "";
 					for (int i = 0; i < args.length; i++) {
 						message = (message + " " + args[i]);
 					}
 					sendFact(message, "nonplayer");
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 }
