 package me.bluejelly.main;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 import me.bluejelly.main.commands.CmdGuild;
 import me.bluejelly.main.configs.*;
 import me.bluejelly.main.listeners.*;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class GuildZ extends JavaPlugin {
 
 	private PlayerListener PlayerListener;
 	private MobListener MobListener;
 	
 	public static GuildConfig gC;
 	public static PlayerConfig pC;
 	public static ChunkConfig cC;
	public static LevelConfig lC;
 
 	public static Map<String, Chunk> playerChunk = new HashMap<String, Chunk>();
 	public static Map<String, String> playerTerritory = new HashMap<String, String>();
 	
 	public ConsoleCommandSender console;
 	
 	public void onEnable() {
 
 		gC = new GuildConfig(this);
 		pC = new PlayerConfig(this);
 		cC = new ChunkConfig(this);
		lC = new LevelConfig(this);
 		
 		console = Bukkit.getServer().getConsoleSender();
 		console.sendMessage("Version " + ChatColor.GOLD + getDescription().getVersion() + ChatColor.WHITE + " enabled!");
 		
 		//CONFIG
 		getConfig().options().copyDefaults(true);
 		/*/////*/
 		getConfig().addDefault("plugin.useguildvault", true);
 		getConfig().addDefault("plugin.somethingelse", true);
 		/*/////*/
 		saveConfig();
 		//CONFIG END
 		
 		//COMMANDS START
 		getCommand("g").setExecutor(new CmdGuild(this));
 		getCommand("guild").setExecutor(new CmdGuild(this));
 		//COMMANDS END
 
 		//REGISTER LISTENERS
 		this.PlayerListener = new PlayerListener(this);
 		this.MobListener = new MobListener(this);
 		
 		getServer().getPluginManager().registerEvents(this.PlayerListener, this);
 		getServer().getPluginManager().registerEvents(this.MobListener, this);
 		//REGISTER LISTENERS
 		
 		//CUSTOM_CONFIGS
 		GuildConfig.saveConfig();
 		PlayerConfig.saveConfig();
 		ChunkConfig.saveConfig();
 		LevelConfig.saveConfig();
 		
 		File PluginDir = getDataFolder();
 		if (!PluginDir.exists())
 	    {
 			PluginDir.mkdir();
 	    }
 		
 		for(Player p: Bukkit.getOnlinePlayers()) {
 			playerChunk.put(p.getName(), p.getLocation().getChunk());
 		}
 		
 		schedules();
 		
 	}
 
 	public void onDisable() {
 
 		console.sendMessage("Version " + ChatColor.GOLD + getDescription().getVersion() + ChatColor.WHITE + " disabled!");
 		
 	}
 	
 	public void schedules() {
 		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 			@Override
 			public void run() {
 				for(Player p: Bukkit.getOnlinePlayers()) {
 					if(ChunkConfig.config.contains(""+p.getLocation().getChunk())) {
 						if(playerTerritory.containsKey(p.getName())) {
 							if(!playerTerritory.get(p.getName()).equals(ChunkConfig.config.getString(p.getLocation().getChunk()+".owner"))) {
 								playerTerritory.put(p.getName(), ChunkConfig.config.getString(p.getLocation().getChunk()+".owner"));
 								p.sendMessage(ChatColor.RED + ChunkConfig.config.getString(p.getLocation().getChunk()+".owner"));
 							}
 						} else {
 							playerTerritory.put(p.getName(), ChunkConfig.config.getString(p.getLocation().getChunk()+".owner"));
 							p.sendMessage(ChatColor.RED + ChunkConfig.config.getString(p.getLocation().getChunk()+".owner"));
 						}
 					} else {
 						if(playerTerritory.containsKey(p.getName())) {
 							if(!playerTerritory.get(p.getName()).equals("~Unclaimed territory~")) {
 								playerTerritory.put(p.getName(), "~Unclaimed territory~");
 								p.sendMessage(ChatColor.GREEN + "~Unclaimed territory~");
 							}
 						} else {
 							playerTerritory.put(p.getName(), "~Unclaimed territory~");
 							p.sendMessage(ChatColor.GREEN + "~Unclaimed territory~");
 						}
 					}
 				}
 			}
 		}, 20, 10);
 	}
 	public void writelevels() {
 		FileConfiguration lConfig = LevelConfig.config;
 		
 		lConfig.addDefault("2","12530");
 		lConfig.addDefault("3","13900");
 		lConfig.addDefault("4","15550");
 		lConfig.addDefault("5","17550");
 		lConfig.addDefault("6","19210");
 		lConfig.addDefault("7","20880");
 		lConfig.addDefault("8","22530");
 		lConfig.addDefault("9","24530");
 		lConfig.addDefault("10","26200");
 		lConfig.addDefault("11","28510");
 		lConfig.addDefault("12","30170");
 		lConfig.addDefault("13","31830");
 		lConfig.addDefault("14","33490");
 		lConfig.addDefault("15","35140");
 		lConfig.addDefault("16","36800");
 		lConfig.addDefault("17","38450");
 		lConfig.addDefault("18","40110");
 		lConfig.addDefault("19","41770");
 		lConfig.addDefault("20","43430");
 		lConfig.addDefault("21","45100");
 		lConfig.addDefault("22","46750");
 		lConfig.addDefault("23","48410");
 		lConfig.addDefault("24","50070");
 		lConfig.addDefault("25","51730");
 		
 	}
 }
