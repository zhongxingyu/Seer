 package net.minecore.minepermit;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import net.minecore.Metrics;
 import net.minecore.MineCore;
 import net.minecore.minepermit.miner.PermitMinerManager;
 import net.minecore.minepermit.world.WorldPermitAreaManager;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class MinePermit extends JavaPlugin {
 
 	private PermitMinerManager mm;
 
 	public static Logger log;
 	private FileConfiguration conf;
 
 	private WorldPermitAreaManager am;
 
 	private Metrics metrics;
 
 	@Override
 	public void onLoad() {
 		log = getLogger();
 		mm = new PermitMinerManager(this);
 		this.saveDefaultConfig();
 		conf = this.getConfig();
 		conf.addDefault("allowDataCollection", true);
 		if (!conf.isConfigurationSection("WorldPermitAreas"))
 			conf.createSection("WorldPermitAreas");
		am = WorldPermitAreaManager.loadFromConfigurationSection(conf.getConfigurationSection("WorldPermitAreas"));

		if (conf.getBoolean("allowDataCollection")) {
			try {
				metrics = new Metrics(this);
				metrics.start();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
 
 	}
 
 	@Override
 	public void onDisable() {
 
 		log.info("Saving players...");
 		mm.savePlayerData();
 		log.info("Players saved!");
 		log.info("MinePermit Disabled");
 
 	}
 
 	@Override
 	public void onEnable() {
 
 		// Register the blockListener
 		this.getServer().getPluginManager().registerEvents(new BlockListener(this), this);
 
 		// Set the command executer
 		getCommand("permit").setExecutor(new CommandInterpreter(this));
 
 		try {
 			Metrics metrics = new Metrics(this);
 			metrics.start();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 
 		log.info("MinePermit Enabled!");
 
 	}
 
 	public MineCore getMineCore() {
 		return (MineCore) getServer().getPluginManager().getPlugin("MineCore");
 	}
 
 	public WorldPermitAreaManager getWorldPermitAreaManager() {
 		return am;
 	}
 
 	public PermitMinerManager getPermitMinerManager() {
 		return mm;
 	}
 
 }
