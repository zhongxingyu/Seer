 package ca.gibstick.discosheep;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public final class DiscoSheep extends JavaPlugin {
 	
 	static final String PERMISSION_PARTY = "discosheep.party";
 	static final String PERMISSION_ALL = "discosheep.partyall";
 	static final String PERMISSION_FIREWORKS = "discosheep.fireworks";
 	static final String PERMISSION_STOPALL = "discosheep.stopall";
 	static final String PERMISSION_RELOAD = "discosheep.reload";
	static final String PERMISSION_OTHER = "discosheep.other";
 	static final String PERMISSION_CHANGEPERIOD = "discosheep.changeperiod";
 
 	Map<String, DiscoParty> parties = new HashMap<String, DiscoParty>();
 	private BaaBaaBlockSheepEvents blockEvents = new BaaBaaBlockSheepEvents(this);
 	FileConfiguration config;
 
 	@Override
 	public void onEnable() {
 		getCommand("ds").setExecutor(new DiscoSheepCommandExecutor(this));
 		getServer().getPluginManager().registerEvents(blockEvents, this);
 
 		if (config == null) {
 			config = this.getConfig();
 		}
 
 		config.addDefault("max.sheep", DiscoParty.maxSheep);
 		config.addDefault("max.radius", DiscoParty.maxRadius);
 		config.addDefault("max.duration", toSeconds_i(DiscoParty.maxDuration));
 		config.addDefault("max.period-ticks", DiscoParty.maxPeriod);
 		config.addDefault("min.period-ticks", DiscoParty.minPeriod);
 		config.addDefault("default.sheep", DiscoParty.defaultSheep);
 		config.addDefault("default.radius", DiscoParty.defaultRadius);
 		config.addDefault("default.duration", toSeconds_i(DiscoParty.defaultDuration));
 		config.addDefault("default.period-ticks", DiscoParty.defaultPeriod);
 
 		loadConfigFromDisk();
 	}
 
 	void loadConfigFromDisk() {
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 
 		DiscoParty.maxSheep = getConfig().getInt("max.sheep");
 		DiscoParty.maxRadius = getConfig().getInt("max.radius");
 		DiscoParty.maxDuration = toTicks(getConfig().getInt("max.duration"));
 		DiscoParty.maxPeriod = getConfig().getInt("max.period-ticks");
 		DiscoParty.minPeriod = getConfig().getInt("min.period-ticks");
 		DiscoParty.defaultSheep = getConfig().getInt("default.sheep");
 		DiscoParty.defaultRadius = getConfig().getInt("default.radius");
 		DiscoParty.defaultDuration = toTicks(getConfig().getInt("default.duration"));
 		DiscoParty.defaultPeriod = getConfig().getInt("default.period-ticks");
 	}
 
 	void reloadConfigFromDisk() {
 		reloadConfig();
 		loadConfigFromDisk();
 	}
 
 	@Override
 	public void onDisable() {
 		this.stopAllParties(); // or else the parties will continue FOREVER
 		this.config = null;
 	}
 
 	int toTicks(double seconds) {
 		return (int) Math.round(seconds * 20.0);
 	}
 
 	double toSeconds(int ticks) {
 		return (double) Math.round(ticks / 20.0);
 	}
 
 	int toSeconds_i(int ticks) {
 		return (int) Math.round(ticks / 20.0);
 	}
 
 	public synchronized Map<String, DiscoParty> getPartyMap() {
 		return this.parties;
 	}
 
 	public synchronized List<DiscoParty> getParties() {
 		return new ArrayList(this.getPartyMap().values());
 	}
 
 	public void stopParty(String name) {
 		if (this.hasParty(name)) {
 			this.getParty(name).stopDisco();
 		}
 	}
 
 	public void stopAllParties() {
 		for (DiscoParty party : this.getParties()) {
 			party.stopDisco();
 		}
 	}
 
 	public boolean hasParty(String name) {
 		return this.getPartyMap().containsKey(name);
 	}
 
 	public DiscoParty getParty(String name) {
 		return this.getPartyMap().get(name);
 	}
 
 	public void removeParty(String name) {
 		if (this.hasParty(name)) {
 			this.getPartyMap().remove(name);
 		}
 	}
 
 	/*-- Actual commands begin here --*/
 	 boolean helpCommand(CommandSender sender) {
 		sender.sendMessage(ChatColor.YELLOW + "DiscoSheep Help\n" + ChatColor.GRAY + "  Subcommands\n" + ChatColor.WHITE + "me, stop, all, stopall\n" + "You do not need permission to use the \"stop\" command\n" + "other <players>: start a party for the space-delimited list of players\n" + ChatColor.GRAY + "  Arguments\n" + ChatColor.WHITE + "-n <integer>: set the number of sheep per player that spawn\n" + "-t <integer>: set the party duration in seconds\n" + "-p <ticks>: set the number of ticks between each disco beat\n" + "-r <integer>: set radius of the area in which sheep can spawn\n" + "-fw: enables fireworks");
 		return true;
 	}
 
 	 boolean stopMeCommand(CommandSender sender, DiscoSheepCommandExecutor discoSheepCommandExecutor) {
 		stopParty(sender.getName());
 		return true;
 	}
 
 	 boolean stopAllCommand(CommandSender sender, DiscoSheepCommandExecutor discoSheepCommandExecutor) {
 		if (sender.hasPermission(DiscoSheep.PERMISSION_STOPALL)) {
 			stopAllParties();
 			return true;
 		} else {
 			return noPermsMessage(sender, DiscoSheep.PERMISSION_STOPALL);
 		}
 	}
 
 	 boolean partyCommand(Player player, DiscoParty party, DiscoSheepCommandExecutor discoSheepCommandExecutor) {
 		if (player.hasPermission(DiscoSheep.PERMISSION_PARTY)) {
 			if (!hasParty(player.getName())) {
 				party.setPlayer(player);
 				party.startDisco();
 			} else {
 				player.sendMessage(ChatColor.RED + "You already have a party. Are you underground?");
 			}
 			return true;
 		} else {
 			return noPermsMessage(player, DiscoSheep.PERMISSION_PARTY);
 		}
 	}
 
 	 boolean reloadCommand(CommandSender sender, DiscoSheepCommandExecutor discoSheepCommandExecutor) {
 		if (sender.hasPermission(DiscoSheep.PERMISSION_RELOAD)) {
 			reloadConfigFromDisk();
 			sender.sendMessage(ChatColor.GREEN + "DiscoSheep config reloaded from disk");
 			return true;
 		} else {
 			return noPermsMessage(sender, DiscoSheep.PERMISSION_RELOAD);
 		}
 	}
 
 	 boolean partyOtherCommand(String[] players, CommandSender sender, DiscoParty party, DiscoSheepCommandExecutor discoSheepCommandExecutor) {
 		if (sender.hasPermission(DiscoSheep.PERMISSION_OTHER)) {
 			Player p;
 			for (String playerName : players) {
 				p = Bukkit.getServer().getPlayer(playerName);
 				if (p != null) {
 					if (!hasParty(p.getName())) {
 						DiscoParty individualParty = party.DiscoParty(p);
 						individualParty.startDisco();
 					}
 				} else {
 					sender.sendMessage("Invalid player: " + playerName);
 				}
 			}
 			return true;
 		} else {
 			return noPermsMessage(sender, DiscoSheep.PERMISSION_OTHER);
 		}
 	}
 
 	 boolean partyAllCommand(CommandSender sender, DiscoParty party, DiscoSheepCommandExecutor discoSheepCommandExecutor) {
 		if (sender.hasPermission(DiscoSheep.PERMISSION_ALL)) {
 			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
 				if (!hasParty(p.getName())) {
 					DiscoParty individualParty = party.DiscoParty(p);
 					individualParty.startDisco();
 					p.sendMessage(ChatColor.RED + "LET'S DISCO!");
 				}
 			}
 			return true;
 		} else {
 			return noPermsMessage(sender, DiscoSheep.PERMISSION_ALL);
 		}
 	}
 
 	 boolean noPermsMessage(CommandSender sender, String permission) {
 		sender.sendMessage(ChatColor.RED + "You do not have the permission node " + ChatColor.GRAY + permission);
 		return false;
 	}
 }
