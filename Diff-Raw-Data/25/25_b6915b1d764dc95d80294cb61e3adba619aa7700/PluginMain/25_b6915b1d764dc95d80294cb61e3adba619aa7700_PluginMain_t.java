 package me.reddy360.theholyflint;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import me.reddy360.theholyflint.command.CommandBan;
 import me.reddy360.theholyflint.command.CommandDiscoStick;
 import me.reddy360.theholyflint.command.CommandDuty;
 import me.reddy360.theholyflint.command.CommandGamemode;
 import me.reddy360.theholyflint.command.CommandGive;
 import me.reddy360.theholyflint.command.CommandKick;
 import me.reddy360.theholyflint.command.CommandLocation;
 import me.reddy360.theholyflint.command.CommandMute;
 import me.reddy360.theholyflint.command.CommandServerStatus;
 import me.reddy360.theholyflint.command.CommandSetSpawn;
import me.reddy360.theholyflint.command.CommandSetTP;
import me.reddy360.theholyflint.command.CommandTrainCannon;
 import me.reddy360.theholyflint.command.CommandUnban;
 import me.reddy360.theholyflint.listeners.PlayerListener;
 import me.reddy360.theholyflint.listeners.TagApiEvent;
 import me.reddy360.theholyflint.listeners.WorldListener;
 
 import org.bukkit.Bukkit;
 import org.bukkit.permissions.Permission;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import de.bananaco.bpermissions.api.ApiLayer;
 import de.bananaco.bpermissions.api.util.CalculableType;
 
 public class PluginMain extends JavaPlugin{
 	public PluginManager pluginManager;
 	public List<String> mutedPlayers = new ArrayList<String>();
 	public Logger log;
 	public boolean serverStatusSigns = false;
 	private static HashMap<String, String> positions = new HashMap<String, String>();
 	@Override
 	public void onEnable() {
 		log = this.getLogger();
 		pluginManager = Bukkit.getPluginManager();
 		pluginManager.registerEvents(new PlayerListener(this), this);
 		pluginManager.registerEvents(new WorldListener(this), this);
 		pluginManager.registerEvents(new TagApiEvent(), this);
 		
 		try {
 			new File(getDataFolder(), "config.yml").createNewFile();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){@Override
 		public void run() {
 			Bukkit.getWorld("world").setTime(0L);
 		}}, 5 * 20 * 60, 5 * 20 * 60);
 		
 		
 		//Auto save
 		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){@Override
 		public void run() {
 //			Signs.setServerStatus(new String[]{"", "Saving World...", "", ""});
 			Bukkit.getWorld("world").save();
 //			Signs.setServerStatus(new String[]{"", ChatColor.GREEN + "Online", "", ""});
 		}}, 5 * 20 * 60, 5 * 20 * 60);
 		
 		//Permissions
 		pluginManager.addPermission(new Permission("thf.setspawn", PermissionDefault.OP));
 		pluginManager.addPermission(new Permission("thf.world.modify", PermissionDefault.OP));
 		pluginManager.addPermission(new Permission("thf.colours", PermissionDefault.OP));
 		pluginManager.addPermission(new Permission("thf.mute.exempt", PermissionDefault.FALSE));
 		pluginManager.addPermission(new Permission("thf.mute", PermissionDefault.OP));
 		pluginManager.addPermission(new Permission("thf.duty", PermissionDefault.OP));
 		pluginManager.addPermission(new Permission("thf.kick", PermissionDefault.OP));
 		pluginManager.addPermission(new Permission("thf.ban", PermissionDefault.OP));
 		pluginManager.addPermission(new Permission("thf.unban", PermissionDefault.OP));
 		pluginManager.addPermission(new Permission("thf.gamemode", PermissionDefault.OP));
 		pluginManager.addPermission(new Permission("thf.kick.exempt", PermissionDefault.FALSE));
 		pluginManager.addPermission(new Permission("thf.ban.exempt", PermissionDefault.FALSE));
 		pluginManager.addPermission(new Permission("thf.disco", PermissionDefault.OP));
 		pluginManager.addPermission(new Permission("thf.drop", PermissionDefault.TRUE));
 		pluginManager.addPermission(new Permission("thf.drop.destroy", PermissionDefault.FALSE));
 		pluginManager.addPermission(new Permission("thf.give", PermissionDefault.OP));
 		pluginManager.addPermission(new Permission("thf.give.other", PermissionDefault.OP));
 		pluginManager.addPermission(new Permission("thf.positions", PermissionDefault.OP));
 		pluginManager.addPermission(new Permission("thf.serverstatus", PermissionDefault.OP));
 		pluginManager.addPermission(new Permission("thf.location", PermissionDefault.OP));
 		pluginManager.addPermission(new Permission("thf.makesign", PermissionDefault.OP));
		pluginManager.addPermission(new Permission("thf.trains", PermissionDefault.OP));
		pluginManager.addPermission(new Permission("thf.settp", PermissionDefault.OP));
 		List<String> groups = getConfig().getStringList("Groups");
 		for(String group : groups){
 			pluginManager.addPermission(new Permission("thf.group." + group, PermissionDefault.FALSE));
 		}
 		
 		//Commands
 		this.getCommand("setspawn").setExecutor(new CommandSetSpawn());
 		this.getCommand("mute").setExecutor(new CommandMute(this));
 		this.getCommand("duty").setExecutor(new CommandDuty(this));
 		this.getCommand("ban").setExecutor(new CommandBan(this));
 		this.getCommand("kick").setExecutor(new CommandKick(this));
 		this.getCommand("unban").setExecutor(new CommandUnban(this));
 		this.getCommand("gamemode").setExecutor(new CommandGamemode(this));
 		this.getCommand("discostick").setExecutor(new CommandDiscoStick(this));
 		this.getCommand("give").setExecutor(new CommandGive(this));
 		this.getCommand("location").setExecutor(new CommandLocation(this));
 		this.getCommand("serverstatus").setExecutor(new CommandServerStatus(this));
		this.getCommand("traincannon").setExecutor(new CommandTrainCannon(this));
		this.getCommand("settp").setExecutor(new CommandSetTP(this));
 		
 		//Configuration Checking
 //		if(getConfig().contains("Signs.ServerStatus")){
 //			serverStatusSigns = true;
 //			Signs.setServerStatus(new String[]{"", ChatColor.GREEN + "Online!", "", ""});
 //		}
 	}
 	
 	public static void setPlayerRank(String player, String rank){
 		String[] groups = ApiLayer.getGroups("world", CalculableType.USER, player);
 		if(groups.length != 0){
 			ApiLayer.removeGroup("world", CalculableType.USER, player, groups[0]);
 		}
 		ApiLayer.addGroup("world", CalculableType.USER, player, rank);
 	}
 
 	public static void setPos(String name, String position) {
 		if(positions.containsKey(name)){
 			positions.remove(position);
 		}
 		positions.put(name, position);
 	}
 
 	public String getLocation(String player) {
 		if(positions.containsKey(player)){
 			return positions.get(player);
 		}
 		return null;
 	}
 }
