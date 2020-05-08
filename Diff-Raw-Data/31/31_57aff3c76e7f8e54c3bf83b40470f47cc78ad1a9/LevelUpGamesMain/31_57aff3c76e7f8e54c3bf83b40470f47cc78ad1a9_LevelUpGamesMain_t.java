 package us.manniL.xapfeltortexp.LevelUpGames;
 
 import java.util.ArrayList;
 import java.util.HashMap;
import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import us.manniL.xapfeltortexp.LevelUpGames.cmd.lcredits;
 import us.manniL.xapfeltortexp.LevelUpGames.cmd.ljoin;
 import us.manniL.xapfeltortexp.LevelUpGames.cmd.lkick;
 import us.manniL.xapfeltortexp.LevelUpGames.cmd.lleave;
 import us.manniL.xapfeltortexp.LevelUpGames.cmd.lsetspawn;
 import us.manniL.xapfeltortexp.LevelUpGames.cmd.lstart;
 import us.manniL.xapfeltortexp.LevelUpGames.cmd.lstop;
 import us.manniL.xapfeltortexp.LevelUpGames.cmd.ltoggle;
 import us.manniL.xapfeltortexp.LevelUpGames.listeners.LevelUpGamesListener;
 
 public class LevelUpGamesMain extends JavaPlugin {
 	public static LevelUpGamesMain Instance;
 	final Logger log = Logger.getLogger("Minecraft");
 	public boolean toggle;
 	public boolean start;
 	public ArrayList<String> players = new ArrayList<String>();
 	public int limit;
 	public int HowMuchCanJoin;
 	public int RandomSpawnRadius;
 	public int RandomSpawnX;
 	public int RandomSpawnY;
 	public int RandomSpawnZ;
 	public int MaximumLevel;
	public List <String> VIPPlayers;
 
 	public ChatColor red = ChatColor.RED;
 	public ChatColor blue = ChatColor.BLUE;
 	public ChatColor gray = ChatColor.GRAY;
 
 	public String prefix = this.blue + "[" + this.gray + "LevelUpGames" + this.blue + "] ";
 
 	public HashMap<String, SubBase> commands = new HashMap<String, SubBase>();
 
 	public void onEnable() {
 
 		Instance = this;
 
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 		load_config();
 
 		this.toggle = false;
 		this.start = false;
 
 		this.log.info("[LevelUpGames] Plugin successful enabled.");
 
 		getServer().getPluginManager().registerEvents(new LevelUpGamesListener(this), this);
 
 		this.getCommand("lug").setExecutor(new CommandReg(this));
 		commands.clear();
 		commands.put("kick", new lkick(this));
 		commands.put("toggle", new ltoggle(this));
 		commands.put("setspawn", new lsetspawn(this));
 		commands.put("start", new lstart(this));
 		commands.put("join", new ljoin(this));
 		commands.put("leave", new lleave(this));
 		commands.put("stop", new lstop(this));
 		commands.put("credits", new lcredits(this));
 	}
 
 	public void onDisable() {
 		this.log.info("[LevelUpGames] Plugin successful disabled.");
 
 		this.players.clear();
 	}
 
 	private void load_config() {
 		this.getConfig().options().header();
 		this.HowMuchCanJoin = getConfig().getInt("LevelUpGames.HowMuchCanJoin", 20);
 		this.RandomSpawnRadius = getConfig().getInt("LevelUpGames.RandomSpawnRadius", 40);
 		this.MaximumLevel = getConfig().getInt("LevelUpGames.MaximumLevel", 32);
		this.VIPPlayers = getConfig().getStringList("LevelUpGames.VIPPlayers");
 	}
 
 	public void noPerms(Player player) {
 		player.sendMessage(this.prefix + this.gray + "You dont have Permissions.");
 	}
 
 	public void toMuchArgs(Player player) {
 		player.sendMessage(this.prefix + this.gray + "Wrong Usage. Type /lug for all LevelUpGames Commands.");
 	}
 }
