 package com.untamedears.rourke750.ExpensiveBeacons;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.untamedears.rourke750.ExpensiveBeacons.BeaconTypes.BeaconManager;
 import com.untamedears.rourke750.ExpensiveBeacons.BeaconTypes.SpeedBeacon;
 import com.untamedears.rourke750.ExpensiveBeacons.BeaconTypes.StrengthBeacon;
 
 public class ExpensiveBeaconsPlugin extends JavaPlugin {
 	public static ExpensiveBeaconsPlugin instance;
 	private BeaconManager bm;
 	private StrengthBeacon strb;
 	private SpeedBeacon sb;
 	private BeaconListener ls;
 	private StoredValues sv;
 	private SaveManager sm;
 	public BufferedWriter writer;
 	private Effects ef = null;
 	private File file;
 	private Logger logger;
 	
 	public StaticBeaconMeta meta;
 	
 	public void onLoad() {
 		instance = this;
 		
 		logger = this.getLogger();
 	}
 
 	public void onEnable() {
 		StrengthBeacon strb = new StrengthBeacon();
 		SpeedBeacon sb = new SpeedBeacon();
 		BeaconManager bm = new BeaconManager(sb, strb);
 		String dir = this.getDataFolder() + File.separator + "Expensive Beacon Types" + File.separator;
 		new File(dir).mkdirs();
 		String type=null;
 		meta= new StaticBeaconMeta();
 		String name[] = {												// Keeps file names within an array to remove ~10 if statemenets ~iebagi
 							"speed_structure1.txt", "speed_structure2.txt",
 							"speed_structure3.txt", "speed_structure4.txt",
 							"speed_structure5.txt", "strength_structure1.txt",
 							"strength_structure2.txt", "strength_structure3.txt",
 							"strength_structure4.txt", "strength_structure5.txt"
 		};
 		
 		for (int i=0; i<meta.getMaxSize(); i++){
 			
 			type = name[i];
 			
 			try {
 				File beacon = new File(dir + type);
 				if (beacon.exists()) {
 					continue;
 				}
 				else {
 					Logger.getLogger(ExpensiveBeaconsPlugin.class.getName()).log(Level.INFO, "Making a new file", "");
 					PrintWriter fstream = new PrintWriter(dir + type);
 					writer = new BufferedWriter(fstream);
 					bm.CreateBeaconFiles(beacon, type);
 				}
 
 			}
 			catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 		}
 		
 		for(int i=0; i<meta.getMaxSize(); i++){						// Sends loadFromFile to StaticBeaconMeta class. ~iebagi
 			meta.overStruct(i, StaticBeaconStructure.loadFromFile(new File(dir, name[i])));	
 		}
 	
 		
 		logger.info("Plugin Enabled, Welcome to Alpha testing!");
 		sv = new StoredValues();
 		dir = this.getDataFolder() + File.separator + "Player Beacon Saves" + File.separator;
 		new File(dir).mkdirs();
 		MultiBlockStructure ms = new MultiBlockStructure(this, ls, sv, meta);
 		ls = new BeaconListener(ms, sv);
 		SaveManager sm = new SaveManager(this, sv);
 		Effects ef = new Effects();
 		enableListener();
 		try {
 			File existing = new File(dir + "StoredBeacons.txt");
 			if (existing.exists()) {
 				Logger.getLogger(ExpensiveBeaconsPlugin.class.getName()).log(Level.INFO, "Existing file", "");
 				FileWriter fw = new FileWriter(existing.getAbsoluteFile(), true);
 				writer = new BufferedWriter(fw);
 				file = existing;
 				logger.info("Logger pre load occured.");
 				sm.load(file);
 			}
 			else {
 				Logger.getLogger(ExpensiveBeaconsPlugin.class.getName()).log(Level.INFO, "Making a new file", "");
 				PrintWriter fstream = new PrintWriter(dir + "StoredBeacons.txt");
 				writer = new BufferedWriter(fstream);
 				file = existing;
 			}
 
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 		final Effects eff = ef;
 
 		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 			public void run() {
 				eff.runEffects(sv.getTypeMap(), sv.getTierMap());
 			}
 		}, 0, 100);
 		final SaveManager smm = sm;
 		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 			public void run() {
 				logger.info("AutoScheduler");
 				try {
 					smm.save(file);
 				}
 				catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}, 0, 300);
 	}
 
 	public void onDisable() {
 		final SaveManager smm = sm;
 		try {
 			smm.save(file);
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void enableListener() {
 		getServer().getPluginManager().registerEvents(ls, this);
 	}
 }
