 package me.tehbeard.BeardAch;
 
 import java.io.File;
 import java.io.IOException;
 
 import me.tehbeard.BeardAch.achievement.*;
import me.tehbeard.BeardAch.commands.AchReloadCommand;
 import me.tehbeard.BeardAch.dataSource.*;
 import me.tehbeard.BeardAch.listener.BeardAchPlayerListener;
 import me.tehbeard.BeardStat.BeardStat;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.*;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.permissions.Permissible;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import de.hydrox.bukkit.DroxPerms.DroxPerms;
 import de.hydrox.bukkit.DroxPerms.DroxPermsAPI;
 
 public class BeardAch extends JavaPlugin {
 
 	public static YamlConfiguration config = null;
 	public static BeardAch self;
 
 	public static DroxPermsAPI droxAPI = null;
 	private static final String PERM_PREFIX = "ach";
 
 	public static boolean hasPermission(Permissible player,String node){
 
 		return (player.hasPermission(PERM_PREFIX + "." + node) || player.isOp());
 
 
 	}
 	public static void printCon(String line){
 		System.out.println("[BeardAch] " + line);
 	}
 
 	public static void printDebugCon(String line){
 		if(config!=null){
 			if(config.getBoolean("general.debug", false)){
 				System.out.println("[BeardAch][DEBUG] " + line);
 
 			}
 		}
 	}
 
 	public void onDisable() {
 		// TODO Auto-generated method stub
 		BeardAch.printCon("Flushing to database");
 		AchievementManager.database.flush();
 		BeardAch.printCon("Flushed to database");
 	}
 
 	private static boolean checkBeardStat(){
 		BeardStat stats = (BeardStat) Bukkit.getServer().getPluginManager().getPlugin("BeardStat");
 		return (stats!=null && stats.isEnabled());
 
 	}
 
 	public void onEnable() {
 		self = this;
 		// TODO Auto-generated method stub
 		BeardStat stats = (BeardStat)getServer().getPluginManager().getPlugin("BeardStat");
 		if(!checkBeardStat()){
 			printCon("BeardStat NOT FOUND, DISABLING PLUGIN!");
 			onDisable();
 			return;
 		}
 
 
 		//check DroxPerms
 
 		DroxPerms droxPerms = ((DroxPerms) this.getServer().getPluginManager().getPlugin("DroxPerms"));
 		if (droxPerms != null) {
 			droxAPI = droxPerms.getAPI();
 		}
 
 
 
 
 
 
 		//setup events
 		PlayerListener pl = new BeardAchPlayerListener();
 		getServer().getPluginManager().registerEvent(Type.PLAYER_MOVE, pl, Priority.Highest, this);
 		getServer().getPluginManager().registerEvent(Type.PLAYER_JOIN, pl, Priority.Highest, this);
 
 
 
 		//TEST ACHIEVEMENTS, DELETE ONCE DATASOURCE'S COMPLETE
 
 
 
 
 
 		//Load config
 		printCon("Starting BeardAch");
 		if(!(new File(getDataFolder(),"BeardAch.yml")).exists()){
 			initialConfig();
 		}
 		config =YamlConfiguration.loadConfiguration(new File(getDataFolder(),"BeardAch.yml"));
 
 		if(config.getString("ach.database.type","").equalsIgnoreCase("mysql")){
 			AchievementManager.database = new SqlDataSource();
 		}
 		if(config.getString("ach.database.type","").equalsIgnoreCase("null")){
 
 			AchievementManager.database = new NullDataSource();	
 		}
 		if(AchievementManager.database == null){
 			printCon("NO SUITABLE DATABASE SELECTED!!");
 
 			onDisable();
 			return;
 		}
 		AchievementManager.loadAchievements();
 
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
 
 			public void run() {
 				// TODO Auto-generated method stub
 				AchievementManager.checkPlayers();
 			}
 
 		}, 600L,600L);
 
 		
		//commands
		
		getCommand("ach-reload").setExecutor(new AchReloadCommand());

 
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 
 		if(sender instanceof Player){
 			Player player = (Player)sender;
 			if(args.length == 0){
 
 				player.sendMessage(ChatColor.AQUA + "Unlocked Achievements:");
 				for( Achievement a:AchievementManager.getAchievements(player.getName())){
 
 
 					player.sendMessage(ChatColor.WHITE + "#" + a.getId() + " "+ ChatColor.GOLD + a.getName());
 				}
 			}else if(args.length ==1){
 				Achievement a = AchievementManager.getAchievement(Integer.parseInt(args[0]));
 				if(a!=null){
 					player.sendMessage(ChatColor.GOLD + a.getName());
 					player.sendMessage(ChatColor.BLUE + a.getDescrip());
 				}
 			}
 
 		}
 
 		return true;
 	}
 
 
 	/**
 	 * Creates the inital config
 	 */
 	private void initialConfig() {
 		printCon("Generating Inital config");
 		File f = new File(getDataFolder(),"BeardAch.yml");
 		config = YamlConfiguration.loadConfiguration(f);
 		config.set("general.debug", false);
 		config.set("ach.database.type", "mysql");
 		config.set("ach.database.host", "localhost");
 		config.set("ach.database.username", "Beardstats");
 		config.set("ach.database.password", "changeme");
 		config.set("ach.database.database", "stats");
 		config.set("ach.msg.person", "Achievement Unlocked: <ACH>");
 		config.set("ach.msg.broadcast", "<PLAYER> Unlocked: <ACH>");
 		config.set("ach.msg.send.person", true);
 		config.set("ach.msg.send.broadcast", false);
 		config.set("achievements", null);
 		try {
 			config.save(f);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
