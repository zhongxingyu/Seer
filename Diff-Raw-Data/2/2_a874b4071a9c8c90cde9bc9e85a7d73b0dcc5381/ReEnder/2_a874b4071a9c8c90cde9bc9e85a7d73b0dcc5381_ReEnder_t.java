 package me.firedroide.plugins.reender;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import me.firedroide.plugins.util.ConfigReader.ConfigReader;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ReEnder extends JavaPlugin implements CommandExecutor {
 	
 	public boolean regenerateCrystals;
 	public boolean respawnEnderdragon;
 	public boolean removeEndPortals;
 	public boolean runInAsyncMode;
 	
 	public boolean listenerEnabled;
 	public boolean repairOnLeave;
 	public boolean repairOnEnter;
 	public List<String> registeredWorlds;
 	
 	EndListener listener;
 	HashMap<World, List<Block>> crystals;
 	HashMap<World, List<Block>> portals;
 	
 	public void onEnable() {
 		crystals = new HashMap<World, List<Block>>();
 		portals = new HashMap<World, List<Block>>();
 		
 		listener = new EndListener(this);
 		Bukkit.getPluginManager().registerEvents(listener, this);
 		
 		loadConfig();
 	}
 	
 	private void loadConfig() {
 		if (getConfig().getKeys(true).size() == 0) {
 			saveResource("config.yml", false);
 			getLogger().info("Default config created!");
 			reloadConfig();
 		}
 		String v = getConfig().getString("generated", getDescription().getVersion());
 		getConfig().options().header("# Config generated with ReEnder " + v + 
									"\n#Last edited with v" + getDescription().getVersion());
 		
 		ConfigReader cr = new ConfigReader(this);
 		
 		regenerateCrystals = cr.getBoolean("main", "RegenerateCrystals", true);
 		respawnEnderdragon = cr.getBoolean("main", "RespawnEnderdragon", true);
 		removeEndPortals = cr.getBoolean("main", "RemoveEndPortals", true);
 		runInAsyncMode = cr.getBoolean("main", "RunInAsyncMode", false);
 		
 		listenerEnabled = cr.getBoolean("listener", "Enabled", false);
 		repairOnLeave = cr.getBoolean("listener", "RepairOnLeave", true);
 		repairOnEnter = cr.getBoolean("listener", "RepairOnEnter", false);
 		registeredWorlds = cr.getStringList("listener", "RegisteredWorlds", new ArrayList<String>());
 		
 		if (cr.gotErrors()) saveConfig();
 		
 		listener.reload(this);
 	}
 	
 	public void onDisable() {
 		Bukkit.getScheduler().cancelTasks(this);
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		
 		if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
 			if (!sender.hasPermission("reender.reload") && sender instanceof Player) {
 				sender.sendMessage("4You don't have the permission to use this command.");
 				return true;
 			}
 			
 			reloadConfig();
 			loadConfig();
 			
 			if (sender instanceof Player) {
 				sender.sendMessage("aReEnder v" + getDescription().getVersion() + " successfully reloaded.");
 			}
 			getLogger().info("ReEnder v" + getDescription().getVersion() + " successfully reloaded.");
 			return true;
 		}
 		
 		if (!sender.hasPermission("reender.use") && sender instanceof Player) {
 			sender.sendMessage("4You don't have the permission to use this command.");
 			return true;
 		}
 		
 		
 		World w;
 		
 		if (!hasWorldArgument(args)) {
 			if (!(sender instanceof Player)) {
 				sender.sendMessage("You need to specify a world if you want to execute this command from the console.");
 				return true;
 			} else if (!((Player) sender).getWorld().getEnvironment().equals(Environment.THE_END)) {
 				sender.sendMessage("cThe world you are in must be 'The End'.");
 				sender.sendMessage("cOtherwise you should use /reend <World>.");
 				return true;
 			}
 			w = ((Player) sender).getWorld();
 		} else {
 			if (!sender.hasPermission("reender.withworldargument") && sender instanceof Player) {
 				sender.sendMessage("4You don't have the permission to use this command with a world as an argument.");
 				return true;
 			}
 			if (!getWorld(args).getEnvironment().equals(Environment.THE_END)) {
 				sender.sendMessage("cThe world needs to be in 'The End' dimension.");
 				return true;
 			}
 			w = getWorld(args);
 		}
 		
 		if (!(sender.hasPermission("reender.worlds.*") || sender.hasPermission("reender.worlds." + w.getName())) && sender instanceof Player) {
 			sender.sendMessage("4You don't have the permission to use this command for this world.");
 			return true;
 		}
 		if (hasFlagArgument(args)) {
 			if (!sender.hasPermission("reender.withflags") && sender instanceof Player) {
 				sender.sendMessage("4You don't have the permission to use this command with flags.");
 				return true;
 			}
 		} else {
 			args = getDefaultArguments();
 		}
 		
 		EndRepairer er = new EndRepairer(this, w, args);
 		if (runInAsyncMode) {
 			Bukkit.getScheduler().scheduleAsyncDelayedTask(this, er);
 		} else {
 			er.run();
 		}
 		
 		return true;
 	}
 	
 	private boolean hasWorldArgument(String[] args) {
 		if (args == null || args.length == 0) return false;
 		for (String arg : args) {
 			if (arg.startsWith("-")) continue;
 			for (World world : Bukkit.getWorlds()) {
 				if (world.getName().equalsIgnoreCase(arg)) return true;
 			}
 		}
 		return false;
 	}
 	private boolean hasFlagArgument(String[] args) {
 		if (args == null || args.length == 0) return false;
 		for (String arg : args) {
 			if (arg.startsWith("-")) return true;
 		}
 		return false;
 	}
 	private World getWorld(String[] args) {
 		for (String arg : args) {
 			if (arg.startsWith("-")) continue;
 			for (World world : Bukkit.getWorlds()) {
 				if (world.getName().equalsIgnoreCase(arg)) return world;
 			}
 		}
 		return null;
 	}
 	
 	public String[] getDefaultArguments() {
 		StringBuilder sb = new StringBuilder();
 		if (!regenerateCrystals) sb.append(" -c");
 		if (!removeEndPortals) sb.append(" -p");
 		if (!respawnEnderdragon) sb.append(" -d"); 
 		String s = sb.toString().substring((sb.length() > 0) ? 1 : 0);
 		
 		return s.split(" ");
 	}
 }
