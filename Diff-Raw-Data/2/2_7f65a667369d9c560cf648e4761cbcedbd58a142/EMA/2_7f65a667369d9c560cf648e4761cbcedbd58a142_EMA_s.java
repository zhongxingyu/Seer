 package com.runetooncraft.plugins.EasyMobArmory;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.entity.Item;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.runetooncraft.plugins.EasyMobArmory.SpawnerHandler.SpawnerConfig;
 import com.runetooncraft.plugins.EasyMobArmory.SpawnerHandler.SpawnerHandler;
 import com.runetooncraft.plugins.EasyMobArmory.core.Config;
 import com.runetooncraft.plugins.EasyMobArmory.core.Messenger;
 import com.runetooncraft.plugins.EasyMobArmory.egghandler.EggHandler;
 import com.runetooncraft.plugins.EasyMobArmory.egghandler.Eggs;
 
 public class EMA extends JavaPlugin {
 	public static Config config = null;
 	public static Eggs eggs = null;
 	public static SpawnerConfig Spawners = null;
 	public static List<ItemStack> Helmets = new ArrayList<ItemStack>();
 	public static List<ItemStack> Chestplates = new ArrayList<ItemStack>();
 	public static List<ItemStack> Leggings = new ArrayList<ItemStack>();
 	public static List<ItemStack> Boots = new ArrayList<ItemStack>();
 	public void onEnable() {
 		getBKCommonLib();
 		loadconfig();
 		loadEggs();
 		loadSpawners();
 		getServer().getPluginManager().registerEvents(new EMAListener(config), this);
 		getCommand("easymobarmory").setExecutor(new Commandlistener());
 		getCommand("ema").setExecutor(new Commandlistener());
 		Messenger m = new Messenger(config);
 		SetItemStacks();
 		LoadSpawnerTimers();
 	}
 
 	private void LoadSpawnerTimers() {
 		List<String> RunningList = Spawners.getList("Spawners.Running.List");
 		if(!RunningList.isEmpty()) {
 			int Size = RunningList.size();
			for(int i = 0; i<=Size; i++) {
 				SpawnerHandler.StartAlreadyExistingSpawnerTimer(RunningList.get(i));
 			}
 			Messenger.info("A total of " + Size + " spawner timers were loaded.");
 		}
 	}
 
 	private void getBKCommonLib() {
 		if(getServer().getPluginManager().getPlugin("BKCommonLib") != null) {
 			Messenger.info("Found BKCommonLib");
 		}else{
 			Messenger.info("Please install BKCommonLib.");
 			Messenger.info("This is a new dependency for EMA");
 			Messenger.info("http://dev.bukkit.org/bukkit-plugins/bkcommonlib");
 			Bukkit.getPluginManager().disablePlugin(this);
 		}
 		
 	}
 
 	private void SetItemStacks() {
 		List<Integer> helmetints = config.getConfig().getIntegerList("List.Helmets");
 		List<Integer> chestplateints = config.getConfig().getIntegerList("List.Chestplates");
 		List<Integer> leggingints = config.getConfig().getIntegerList("List.Leggings");
 		List<Integer> Bootints = config.getConfig().getIntegerList("List.Boots");
 		for(int i : helmetints) {
 			Helmets.add(new ItemStack(Material.getMaterial(i)));
 		}
 		for(int i : chestplateints) {
 			Chestplates.add(new ItemStack(Material.getMaterial(i)));
 		}
 		for(int i : leggingints) {
 			Leggings.add(new ItemStack(Material.getMaterial(i)));
 		}
 		for(int i : Bootints) {
 			Boots.add(new ItemStack(Material.getMaterial(i)));
 		}
 	}
 
 	private void loadconfig() {
 		File dir = this.getDataFolder();
 		if (!dir.exists()) dir.mkdir();
 		File file = new File(this.getDataFolder(), "config.yml");
 		config = new Config(file);
 		if (!config.load()) {
 			this.getServer().getPluginManager().disablePlugin(this);
 			throw new IllegalStateException("The config-file was not loaded correctly!");
 		}
 	}
 	private void loadEggs() {
 		File dir = this.getDataFolder();
 		if (!dir.exists()) dir.mkdir();
 		File file = new File(this.getDataFolder(), "eggs.yml");
 		eggs = new Eggs(file);
 		if (!eggs.load()) {
 			this.getServer().getPluginManager().disablePlugin(this);
 			throw new IllegalStateException("The Eggs data file was not loaded correctly!");
 		}
 	}
 	private void loadSpawners() {
 		File dir = this.getDataFolder();
 		if (!dir.exists()) dir.mkdir();
 		File file = new File(this.getDataFolder(), "spawners.yml");
 		Spawners = new SpawnerConfig(file);
 		if (!Spawners.load()) {
 			this.getServer().getPluginManager().disablePlugin(this);
 			throw new IllegalStateException("The Spawners data file was not loaded correctly!");
 		}
 	}
 }
