 package nz.co.nznetwork.minecraftplugin.subplugins.health;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import nz.co.nznetwork.minecraftplugin.NZNPlugin;
 import nz.co.nznetwork.minecraftplugin.NZNSubplugin;
 import nz.co.nznetwork.minecraftplugin.utils.CountdownTimer;
 import nz.co.nznetwork.minecraftplugin.utils.CountdownListener;
 
 public class Health implements NZNSubplugin, CommandExecutor, CountdownListener {
 
 	private NZNPlugin plugin;
 	private HealthListener listener;
 	private Map<String, Long> recentGods = new HashMap<String, Long>();
 	private Map<String, Long> recentHeals = new HashMap<String, Long>();
 	private List<String> gods = new ArrayList<String>();
 	
 	@Override
 	public void onEnable(NZNPlugin masterPlugin) {
 		plugin = masterPlugin;
 		listener = new HealthListener(this);
 		plugin.getServer().getPluginManager().registerEvents(listener, plugin);
 		plugin.getLogger().info("Subplugin Health Enabled");
 	}
 	@Override
 	public void onDisable() {
 		plugin.getLogger().info("Subplugin Health Disabled");
 	}
 	@Override
 	public void reloadConfig() {}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if(cmd.getName().equalsIgnoreCase("heal")) {
 			return heal(sender, args);
 		}
 		if(cmd.getName().equalsIgnoreCase("god")) {
 			return god(sender, args);
 		}
 		//something really screwed up to get here
 		return false;
 	}
 	private boolean heal(CommandSender sender, String[] args) {
 		if(!(sender instanceof Player) && args.length==0) {//check they are a player if using on self
 			sender.sendMessage(plugin.getUtils().ERROR_COLOR+"Only players can use /heal on themselves");
 			return true;
 		}
		if(args.length != 0 && !sender.hasPermission("nzn.health.heal.target")) { //check they have permission
 			sender.sendMessage(plugin.getUtils().ERROR_COLOR+"Incorrect format, use: /heal");
 			return true;
 		}
 		if(args.length > 1) {//check the format of command
 			sender.sendMessage(plugin.getUtils().ERROR_COLOR+"Incorrect format, use: /heal <target>");
 			return true;
 		}
 		Player player;
 		if(args.length == 0)
 			player = (Player) sender;
 		else {
 			player = plugin.getServer().getPlayer(args[0]);
 			if(player == null) {
 				sender.sendMessage(plugin.getUtils().ERROR_COLOR+"Could not find player "+ChatColor.YELLOW+args[0]);
 				return true;	
 			}
 		}
 		Long lastHeal = recentHeals.get(sender.getName());
 		if(lastHeal != null) {
 			if((System.currentTimeMillis()-lastHeal) < plugin.getConfig().getLong("nzn.health.heal.cooldown") &&
					!sender.hasPermission("nzn.health.heal.instant")) {
 				sender.sendMessage(ChatColor.DARK_GREEN+"Heal "+ChatColor.WHITE+"is still on cooldown"); //non-standard error message
 				return true;
 			}
 		}
 		recentHeals.put(sender.getName(), System.currentTimeMillis());
 		player.setFoodLevel(20);
 		player.setSaturation(20);
 		player.setHealth(20);
 		player.setExhaustion(0);
 		player.setFireTicks(0);
 		if(player.getName().equals(sender.getName())) {
 			player.sendMessage(ChatColor.DARK_GREEN+"You have been healed");
 		}
 		else {
 			player.sendMessage(ChatColor.DARK_GREEN+"You have been healed by "+plugin.getChatFormat().getColoredName(sender));
 			sender.sendMessage(ChatColor.DARK_GREEN+"You have healed "+plugin.getChatFormat().getColoredName(player));
 		}
 		return true;
 	}
 	private boolean god(CommandSender sender, String[] args) {
 		if(!(sender instanceof Player) && args.length==0) {//check they are a player if using on self
 			sender.sendMessage(plugin.getUtils().ERROR_COLOR+"Only players can use /god on themselves");
 			return true;
 		}
		if(args.length != 0 && !sender.hasPermission("nzn.health.god.target")) { //check they have permission
 			sender.sendMessage(plugin.getUtils().ERROR_COLOR+"Incorrect format, use: /god");
 			return true;
 		}
 		if(args.length > 1) {//check the format of command
 			sender.sendMessage(plugin.getUtils().ERROR_COLOR+"Incorrect format, use: /god <target>");
 			return true;
 		}
 		Player player; //if there were arguments get target... otherwise target is sender
 		if(args.length == 0)
 			player = (Player) sender;
 		else {
 			player = plugin.getServer().getPlayer(args[0]);
 			if(player == null) {
 				sender.sendMessage(plugin.getUtils().ERROR_COLOR+"Could not find player "+ChatColor.YELLOW+args[0]);
 				return true;	
 			}
 		}
 		//check god is not on cooldown
 		Long lastGod = recentGods.get(sender.getName());
 		if(lastGod != null) {
 			if((System.currentTimeMillis()-lastGod) < plugin.getConfig().getLong("nzn.health.god.cooldown") &&
					!sender.hasPermission("nzn.health.god.instant")) {
 				sender.sendMessage(ChatColor.DARK_GREEN+"GodMode "+ChatColor.DARK_RED+"is still on cooldown");
 				return true;
 			}
 		}
 		//check not already in god mode
 		if(isGod(player)) {
 			sender.sendMessage(plugin.getChatFormat().getColoredName(player)+ChatColor.DARK_RED+"is already in "+ChatColor.DARK_GREEN+"GodMode");
 			return true;
 		}
 		//if not targeting self send notification
 		if(!player.getName().equals(sender.getName())) {
 			player.sendMessage("You have set"+plugin.getChatFormat().getColoredName(player)+ChatColor.WHITE+"to "+ChatColor.DARK_GREEN+" GodMode"+ChatColor.WHITE+" for "+plugin.getUtils().secondsToString(plugin.getConfig().getLong("nzn.health.god.duration")));
 		}
 		//enable god mode
 		gods.add(player.getName());
 		recentGods.put(sender.getName(), System.currentTimeMillis());
 		new CountdownTimer(plugin.getConfig().getLong("nzn.health.god.duration"), this, player);
 		return true;
 	}
 	@Override
 	public void intervalTrigger(CommandSender sender, long timeLeft) {
 		Player p = (Player) sender;
 		if(p.isOnline()) {
 			sender.sendMessage(ChatColor.DARK_GREEN+"God Mode"+ChatColor.WHITE+" active for "+plugin.getUtils().secondsToString(timeLeft));
 		}
 	}
 	@Override
 	public void countdownComplete(CommandSender sender) {
 		gods.remove(sender.getName());
 		Player p = (Player) sender;
 		if(p.isOnline()) {
 			sender.sendMessage(ChatColor.DARK_GREEN+"God Mode"+ChatColor.WHITE+" expired");
 		}
 	}
 	public boolean isGod(Player p) {
 		return gods.contains(p.getName());
 	}
 }
