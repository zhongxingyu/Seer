 package ramirez57.IPAuth;
 
 import java.io.File;
 import java.io.IOException;
import java.util.Collection;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin implements Listener {
 
 	FileConfiguration logins;
 	File loginsFile;
 	FileConfiguration config;
 	File configFile;
 	Logger logger;
 	Player player;
 	Plugin plugin;
 	Server mc;
 	
 	public void onEnable() {
 		plugin = this;
 		mc = this.getServer();
 		logger = this.getLogger();
 		mc.getPluginManager().registerEvents(this, this);
 		config = this.getConfig();
 		config.options().copyDefaults(true);
 		loginsFile = new File(this.getDataFolder(), "LOGINS");
 		configFile = new File(this.getDataFolder(), "config.yml");
 		logins = YamlConfiguration.loadConfiguration(loginsFile);
 		this.saveConfig();
 		savelogins();
 	}
 	
 	public void onDisable() {
 		
 	}
 	
 	public void reloadlogins() {
 		logins = YamlConfiguration.loadConfiguration(loginsFile);
 		return;
 	}
 	
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
     	boolean isPlayer = sender instanceof Player;
     	if(isPlayer) sender = (Player) sender;
     	if(cmd.getName().equalsIgnoreCase("ipauth")) {
     		if(args.length < 1) {
     			return false;
     		}
     		if(args[0].equalsIgnoreCase("reload")) {
     			config = YamlConfiguration.loadConfiguration(configFile);
     			reloadlogins();
     			logger.info("Configurations reloaded.");
     			if(isPlayer) sender.sendMessage(ChatColor.GREEN + "Configurations reloaded.");
     			return true;
     		}
     	}
     	return false;
     }
 	
 	public void savelogins() {
 		try {
 			logins.save(loginsFile);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return;
 	}
 	
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void checkIP(PlayerLoginEvent event) {
 		Player player = event.getPlayer();
		String name = player.getName();
 		String _name = name.toLowerCase();
 		String ip = event.getAddress().getHostAddress();
 		if(player.hasPermission("IPAuth.bypass")) {
 			logger.info("Allowing " + name + " (Has bypass permission)");
 			return;
 		}
 		if(logins.getString(_name) == null || logins.getString(_name) == "") {
			Collection<Object> counting = logins.getValues(true).values();
			int count = 0;
			while(counting.contains(ip)) {
				count++;counting.remove(ip);
			}
			if(count < config.getInt("max_accounts") || config.getInt("max_accounts") < 1) {
				logins.set(_name, ip);
				savelogins();
			} else {
				event.disallow(PlayerLoginEvent.Result.KICK_OTHER, config.getString("kickaccount"));
				return;
			}
 		}
 		if(logins.getString(_name).equals(ip)) {
 			logger.info("Allowing " + name + " (" + ip + " / " + logins.getString(_name) + ")" );
 			return;
 		} else {
 			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, config.getString("kickreason"));
 			return;
 		}
 	}
 }
