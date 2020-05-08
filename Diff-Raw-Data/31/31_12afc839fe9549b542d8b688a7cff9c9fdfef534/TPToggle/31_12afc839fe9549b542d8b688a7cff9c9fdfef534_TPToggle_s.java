 package com.hackhalo2.tp;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.hackhalo2.tp.commands.TPCommand;
 import com.hackhalo2.tp.commands.TPHCommand;
 import com.hackhalo2.tp.commands.TPTCommand;
 import com.hackhalo2.tp.utils.TPPlayer;
 import com.hackhalo2.tp.utils.TPUtil;
 import com.hackhalo2.tp.utils.TPWorkerThread;
 
 public class TPToggle extends JavaPlugin {
 	protected Map<String,TPPlayer> playerReferences = new HashMap<String,TPPlayer>();
 	protected static Map<String,Integer> processes = new HashMap<String, Integer>();
 	protected File playerDataFolder;
 	public Logger log = Bukkit.getLogger();
 	public long delay;
 	//config.yml options
 
 	public static long OPTIMAL_TICKS_PER_MINUTES = 20*60;
 
 	private final TPTListener listener = new TPTListener(this);
 
 	@Override
 	public void onLoad() {
 		TPUtil.setPlugin(this);
 		this.playerDataFolder = new File(this.getDataFolder(), "players");
 		this.playerDataFolder.mkdirs();
 
 		//load up the currently online players
 		Player[] online = Bukkit.getServer().getOnlinePlayers();
 		this.log.info("[TPToggle] Loading "+online.length+" Player Files");
 		for(int i = 0; i < online.length; i++) {
 			TPPlayer put = new TPPlayer(online[i].getName(), playerDataFolder);
 			this.playerReferences.put(online[i].getName(), put);
 		}
 
 		//TODO: add a configuration method for this
 		this.delay = OPTIMAL_TICKS_PER_MINUTES*5;
 	}
 
 	@Override
 	public void onEnable() {
 		//Register the Event handler
 		PluginManager pm = this.getServer().getPluginManager();
 		pm.registerEvents(this.listener, this);
 
 		this.getCommand("tpt").setExecutor(new TPTCommand(this)); //TPToggle command
 		this.getCommand("tp").setExecutor(new TPCommand(this)); //TP and TPO command
 		this.getCommand("tpo").setExecutor(new TPCommand(this));
 		this.getCommand("tph").setExecutor(new TPHCommand(this)); //TPH Command
 
 		this.log.info("[TPToggle] Version 3.0 Enabled");
 	}
 
 	@Override
 	public void onDisable() {
 		this.log.info("[TPToggle] Saving Player Configurations...");
 		Iterator<Entry<String, TPPlayer>> it = this.playerReferences.entrySet().iterator();
 		while(it.hasNext()) {
 			Map.Entry<String, TPPlayer> pair = it.next();
 			TPPlayer temp = pair.getValue();
 			temp.save();
 			it.remove();
 		}
 		this.playerReferences.clear();
 		this.playerReferences = null;
 		this.log.info("[TPToggle] Saved Player Configurations.");
 		this.playerDataFolder = null;
 
 		this.log.info("[TPToggle] Disabled.");
 		this.log = null;
 	}
 
 	/**
 	 * Return the TPPlayer reference for the given Bukkit Player
 	 * @param player The Bukkit Player to be converted
 	 * @return TPPlayer
 	 */
 	public TPPlayer getTPPlayer(Player player) {
 		if(this.playerReferences.containsKey(player.getName())) {
 			return this.playerReferences.get(player.getName());
 		} else {
 			TPPlayer tpplayer = new TPPlayer(player.getName(), playerDataFolder);
 			this.playerReferences.put(player.getName(), tpplayer);
 			return tpplayer;
 		}
 	}
 
	public void removeWorker(TPPlayer player, String name) {
 		String key = player.playerName+"->"+name;
 		if(processes.containsKey(key)) {
 			int processID = processes.get(key);
 			Bukkit.getServer().getScheduler().cancelTask(processID);
 			processes.remove(key);
 		} else {
			this.log.info("[TPToggle] Key "+key+" was invalid");
 		}
 	}
 
 	public static void removeProcess(String key) { processes.remove(key); }
 
 	public void addWorker(TPPlayer player, String name) {
 		TPWorkerThread thread = new TPWorkerThread(player, name);
 		int value = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, thread, this.delay);
 		String key = player.playerName+"->"+name;
 		thread.setProcessKey(key);
 		processes.put(key, value);
 	}
 }
