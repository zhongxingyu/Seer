 package ru.cubelife.clearworld;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ClearWorld extends JavaPlugin {
 	
 	/** Логгер */
 	private Logger log;
 	/** Конфиг */
 	private FileConfiguration cfg;
 	/** Файл конфига */
 	private File cfgFile;
 	/** Время в днях */
 	private long t;
 	
 	/** Включено ли? */
 	public static boolean enabled;
 	/** Время в миллисекундах */
 	public static long time;
 	/** Регенирировать ли? */
 	public static boolean regen;
 	
 	/** Вызывается при включении */
 	public void onEnable() {
 		enabled = true;
 		cfgFile = new File(getDataFolder(), "config.yml");
 		cfg = YamlConfiguration.loadConfiguration(cfgFile);
 		loadCfg(); // Загружаем настройки
 		saveCfg(); // Сохраняем настройки
 		
 		PluginManager pm = getServer().getPluginManager();
 		
 		if(pm.getPlugin("WorldGuard") != null) {
 			log("Using WorldGuard!");
 		} else {
 			log("WorldGuard not founded! Disabling..");
 		}
 		
 		if(regen) {
 			if(pm.getPlugin("WorldEdit") != null) {
 				log("Using WorldEdit!");
 			} else {
 				log("WorldEdit not founded! Disabling regeneration..");
 				regen = false;
 				saveCfg();
 			}
 		}
 		
 		log = Logger.getLogger("Minecraft");
 		new AutoCleaner().start(); // Запускает в отдельном потоке AutoCleaner
 		log("Enabled!");
 	}
 	
 	/** Вызывается при выключении */
 	public void onDisable() {
 		enabled = false;
 		log("Disabled!");
 	}
 	
 	/** Логирует в консоль */
 	private void log(String msg) {
 		log.info("[ClearWorld] " + msg);
 	}
 	
 	/** Загружает конфиг */
 	private void loadCfg() {
 		t = cfg.getInt("time", 30);
 		time = (t * 24 * 3600 * 1000);
 		regen = cfg.getBoolean("regen", false);
 	}
 	
 	/** Сохраняет конфиг */
 	private void saveCfg() {
 		cfg.set("time", t);
 		cfg.set("regen", regen);
 		try {
 			cfg.save(cfgFile);
 		} catch (Exception e) { }
 	}
 
 }
