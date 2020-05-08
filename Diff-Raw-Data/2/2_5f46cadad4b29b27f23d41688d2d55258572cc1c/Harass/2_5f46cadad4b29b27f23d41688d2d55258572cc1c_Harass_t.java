 package tk.nekotech.harass;
 
 import org.bukkit.plugin.java.*;
 
 import tk.nekotech.commands.HarassCommand;
 import tk.nekotech.harass.events.PlayerChat;
 import tk.nekotech.harass.events.PlayerDropItem;
 import tk.nekotech.harass.events.PlayerInteract;
 import tk.nekotech.harass.events.PlayerJoin;
 import tk.nekotech.harass.events.PlayerMove;
 import tk.nekotech.harass.events.PlayerRespawn;
 import tk.nekotech.harass.helpers.Achievements;
 import tk.nekotech.harass.helpers.ArrayLists;
 import tk.nekotech.harass.helpers.Colors;
 import tk.nekotech.harass.helpers.Potions;
 import tk.nekotech.harass.helpers.Staff;
 import tk.nekotech.harass.helpers.StartupLog;
 import tk.nekotech.harass.helpers.Version;
 import tk.nekotech.harass.permissions.Permissions;
 
 public class Harass extends JavaPlugin {
 	
 	public HarassCommand harasscommand = new HarassCommand(this);
 	
 	public PlayerChat playerchat = new PlayerChat(this);
 	public PlayerDropItem playerdropitem = new PlayerDropItem(this);
 	public PlayerInteract playerinteract = new PlayerInteract(this);
 	public PlayerJoin playerjoin = new PlayerJoin(this);
 	public PlayerMove playermove = new PlayerMove(this);
 	public PlayerRespawn playerrespawn = new PlayerRespawn(this);
 	
 	public Achievements achievements = new Achievements();
 	public ArrayLists arraylists = new ArrayLists();
 	public Colors colors = new Colors();
 	public Potions potions = new Potions(this);
 	public Staff staff = new Staff(this);
 	public StartupLog startuplog = new StartupLog(this);
 	public Version version = new Version(this);
 	
 	public Permissions permissions = new Permissions();
 	
 	public boolean outOfDate = false;
 	public String ver = null;
 	public String newver = null;
 
 	public void onEnable() {
 		
 		startuplog.logStartup();
 		
 		ver = getDescription().getVersion();
 		version.checkVersion();
 	
 		getCommand("harass").setExecutor(new Work(this));
 		
 		getServer().getPluginManager().registerEvents(playerchat, this);
 		getServer().getPluginManager().registerEvents(playerdropitem, this);
 		getServer().getPluginManager().registerEvents(playerjoin, this);
 		getServer().getPluginManager().registerEvents(playermove, this);
		getServer().getPluginManager().registerEvents(playerrespawn, this);
 
 	}
 		
 	
 	public void onDisable() {
 		StringBuilder msg = new StringBuilder();
 		msg.append(arraylists.HARASSED.size() + " harassed players of which ");
 		msg.append(arraylists.POTIONS.size() + " had potions flag, ");
 		msg.append(arraylists.CHAT.size() + " had quiet harass flag, ");
 		msg.append(arraylists.DROP.size() + " had drop blocking flag, ");
 		msg.append(arraylists.SILENT.size() + " had chat blocking flag, ");
 		msg.append(arraylists.INTERACT.size() + " had interaction blocking flag, ");
 		msg.append(arraylists.ACHIEVEMENT.size() + " had achievement flag");
 		getLogger().info("Cleared " + msg.toString());
 		arraylists.HARASSED.clear();
 		arraylists.POTIONS.clear();
 		arraylists.CHAT.clear();
 		arraylists.DROP.clear();
 		arraylists.SILENT.clear();
 		arraylists.INTERACT.clear();
 		arraylists.ACHIEVEMENT.clear();
 	}
 	
 }
