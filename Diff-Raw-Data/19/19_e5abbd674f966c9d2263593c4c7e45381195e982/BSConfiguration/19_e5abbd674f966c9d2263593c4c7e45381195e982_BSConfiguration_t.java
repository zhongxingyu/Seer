 package net.sqdmc.bubbleshield;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import com.massivecraft.factions.Faction;
 import com.massivecraft.factions.Factions;
 
 import net.sqdmc.bubbleshield.BubbleShield;
 
 public class BSConfiguration {
 
 	private Logger log = Bukkit.getServer().getLogger();
 	
 	private BubbleShield plugin;
 	private static String pluginName;
 	private static String pluginVersion;
 	private YamlConfiguration bukkitConfig = new YamlConfiguration();
 	
 	private static String directory = "plugins" + File.separator + BubbleShield.getPluginName() + File.separator;
 	private File configFile = new File(directory + "config.yml");
 	private File durabilityFile = new File(directory + "durability.dat");
 	private File shieldsFile = new File(directory + "shields.yml");
 	
 	private YamlConfiguration shieldsDB = new YamlConfiguration();
 	
 	private int ProtectionRadius = 16;
 	private long RegenTime = 6000L;
 	private int Durability = 100;
 	private int AffectedBlockCountMax = 2;
 	
 	public BSConfiguration(BubbleShield plugin) {
 		this.plugin = plugin;
 		pluginName = BubbleShield.getPluginName();
 	}
 	
 	public boolean loadConfig() {
 		boolean isErrorFree = true;
 		pluginVersion = BubbleShield.getVersion();
 
 		new File(directory).mkdir();
 
 		if (configFile.exists()) {
 			try {
 				bukkitConfig.load(configFile);
 
 				if (bukkitConfig.getString("Version", "").equals(pluginVersion)) {
 					// config file exists and is up to date
 					log.info(pluginName + " config file found, loading config...");
 					loadData();
 				} else {
 					// config file exists but is outdated
 					log.info(pluginName + " config file outdated, adding old data and creating new values. " + "Make sure you change those!");
 					loadData();
 					writeDefault();
 				}
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else {
 			// config file does not exist
 			try {
 				log.info(pluginName + " config file not found, creating new config file...");
 				configFile.createNewFile();
 				writeDefault();
 			} catch (IOException ioe) {
 				log.severe("Could not create the config file for " + pluginName + "!");
 				ioe.printStackTrace();
 				isErrorFree = false;
 			}
 		}
 
 		return isErrorFree;
 	}
 	
 	private void loadData() {
 		try {
 			bukkitConfig.load(configFile);
 			
 			ProtectionRadius = bukkitConfig.getInt("ProtectionRadius.Distance", ProtectionRadius);
 			RegenTime = bukkitConfig.getLong("RegenTime.Time", RegenTime);
 			Durability = bukkitConfig.getInt("MaxDurability.Amount", Durability);
 			AffectedBlockCountMax = bukkitConfig.getInt("MaxHitBlocks.Amount", AffectedBlockCountMax);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void writeDefault() {
 		write("Version", BubbleShield.getVersion());
 		
 		write("ProtectionRadius.Distance", ProtectionRadius);
 		write("RegenTime.Time", RegenTime);
 		write("MaxDurability.Amount", Durability);
 		write("MaxHitBlocks.Amount", AffectedBlockCountMax);
 
 		loadData();
 	}
 	
 	private void write(String key, Object o) {
 		try {
 			bukkitConfig.load(configFile);
 			bukkitConfig.set(key, o);
 			bukkitConfig.save(configFile);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private void write3(String key, Object o) {
 		try {
 			shieldsDB.load(shieldsFile);
 			shieldsDB.set(key, o);
 			//log.info("[BubbleShield] : write3() " + key + "  " + o);
 			shieldsDB.save(shieldsFile);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	@SuppressWarnings("unused")
 	private void remove(String key, Object o) {
 		try {
 			shieldsDB.load(shieldsFile);
 			
 			if (shieldsDB.contains(key))
 			{
 				shieldsDB.get(key);
 				shieldsDB.set(key, null);
 			}
 			
 			log.info("[BubbleShield] : remove() " + key + "  " + o);
 			shieldsDB.save(shieldsFile);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private long readLong(String key, String def) {
 		try {
 			bukkitConfig.load(configFile);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// Bukkit Config has no getLong(..)-method, so we are using Strings
 		String value = bukkitConfig.getString(key, def);
 
 		long tmp = 0;
 
 		try {
 			tmp = Long.parseLong(value);
 		} catch (NumberFormatException nfe) {
 			log.warning("[BubbleShield] : readLong() " + "Error parsing a long from the config file. Key=" + key);
 			nfe.printStackTrace();
 		}
 
 		return tmp;
 	}
 	
 	public int getMaxPowerCost(){
 		return Durability;
 	}
 
 	public long getRegenTime(){
 		return RegenTime;
 	}
 	
 	public int getAffectedBlockCountMax() {
 		return AffectedBlockCountMax;
 	}
 	
 	public void saveDurabilityToFile() {
 		if (plugin.getListener() == null || plugin.getListener().getShieldDurability() == null) {
 			return;
 		}
 
 		HashMap<Integer, Integer> map = plugin.getListener().getShieldDurability();
 
 		new File(directory).mkdir();
 
 		try {
 			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(durabilityFile));
 			oos.writeObject(map);
 			oos.flush();
 			oos.close();
 		} catch (IOException e) {
 			log.severe("[BubbleShield] : saveDurabilityToFile() " + "Failed writing shields durability for " + BubbleShield.getPluginName());
 			e.printStackTrace();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public HashMap<Integer, Integer> loadDurabilityFromFile() {
 		if (!durabilityFile.exists() || plugin.getListener() == null || plugin.getListener().getShieldDurability() == null) {
 			return null;
 		}
 
 		new File(directory).mkdir();
 
 		HashMap<Integer, Integer> map = null;
 		Object result = null;
 
 		try {
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(durabilityFile));
 			result = ois.readObject();
 			map = (HashMap<Integer, Integer>) result;
 			ois.close();
 		} catch (IOException ioe) {
 			log.severe("[BubbleShield] : loadDurabilityFromFile() " + "Failed reading shields durability for " + BubbleShield.getPluginName());
 			ioe.printStackTrace();
 		} catch (ClassNotFoundException cnfe) {
 			log.severe("[BubbleShield] : loadDurabilityFromFile() " + "Shields durability file contains an unknown class, was it modified?");
 			cnfe.printStackTrace();
 		}
 
 		return map;
 	}
 	
 	/* ==========================================================================================
 	 * 
 	 * 
 	 */
 	
 	public void SaveShieldsToFile() throws FileNotFoundException, IOException, InvalidConfigurationException {
 		if (plugin.getListener() == null || plugin.getListener().getShields() == null) {
 			return;
 		}
 		
 		if (!shieldsFile.exists())
 		{
 			try {
 				shieldsFile.createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		HashMap<Block, ShieldBase> map = plugin.getListener().getShieldsBase();	
 		
 		new File(directory).mkdir();
 
 	    final Iterator<Entry<Block, ShieldBase>> iter = map.entrySet().iterator();
 	    
 
 	    while (iter.hasNext()) {
 	        final Entry<Block, ShieldBase> entry = iter.next();
 	        final Object value = entry.getValue().getShieldBaseString();
 	        final String key = entry.getValue().shield.getOwner().getId();
 	        //log.info("[BubbleShield] : SaveShieldsToFile() " + key + " " + value.toString());
 	        
 	        write3(key, value);
 	    }
 		
 	}
 	
 	public void LoadShieldFromFile() throws FileNotFoundException, IOException, InvalidConfigurationException {
 		if (!shieldsFile.exists() || plugin.getListener() == null || plugin.getListener().getShieldsBase() == null) {
 			return;
 		}
 
 		new File(directory).mkdir();
 		
 		HashMap<Block, ShieldBase> ShieldBaseMap = new HashMap<Block, ShieldBase>();
 		HashMap<ShieldOwner, Shield> ShieldMap = new HashMap<ShieldOwner, Shield>();
 		
 		int x = 0;
 		int y = 0;
 		int z = 0;
 
 		//try {
 			shieldsDB.load(shieldsFile);
			//log.info("[BubbleShield] : " + "LoadShieldFromFile() " + shieldsBaseDB.get);
 			
 			Set<String> keys = shieldsDB.getKeys(true);
 			
 			for (String str : keys)
 			{
 			String result = shieldsDB.getString(str);
 				
 			//log.info("[BubbleShield] : " + "LoadShieldBase: result: " + result);
 			
 			if (result != null && !result.equals("") && result.length() > 1)
 			{
 				String[] results = result.split(",");
 			
 				Factions factions = Factions.i;
 		
 				Faction faction = factions.get(results[0].replace(" ", ""));
 			
 				ShieldOwnerFaction fShieldOwner = new ShieldOwnerFaction(faction);
 		
 		
 				Shield _shield = new Shield(fShieldOwner);
 		
 				x = Integer.parseInt(results[2]);
 			    y = Integer.parseInt(results[3]);
 			    z = Integer.parseInt(results[4]);
 			
 				Block Sponge = Bukkit.getWorld(results[1]).getBlockAt(x, (y), z);
 				org.bukkit.Material _Sponge = Sponge.getType();
 				Block Sign = Bukkit.getWorld(results[1]).getBlockAt(x, (y+1), z);
 				org.bukkit.Material _Sign = Sign.getType();
 			    
 				ShieldBase shieldBase = new ShieldBase( Sponge ,Sign , _shield, Bukkit.getWorld(results[1]),x , y, z);
 			
 				//log.info("[BubbleShield] : " + "LoadShieldFromFile() " + "Sponge" + Sponge.getLocation().toString());
 				//log.info("[BubbleShield] : " + "LoadShieldFromFile() " + "Sign" + Sign.getLocation().toString());
 			
				if (_Sponge == org.bukkit.Material.SPONGE && (_Sign == org.bukkit.Material.SIGN_POST || _Sign == Material.WALL_SIGN || _Sign == Material.SIGN)) {
 					ShieldBaseMap.put(Sign, shieldBase);
 					ShieldBaseMap.put(Sponge, shieldBase);
 					ShieldMap.put(fShieldOwner, _shield);
 					//log.info("[BubbleShield] : " + "LoadShieldFromFile() " + Sign.toString() + " " + shieldBase.getShieldBaseLocString());
 				}
 			
 				this.plugin.getListener().setShieldBase(ShieldBaseMap);
 				this.plugin.getListener().setShields(ShieldMap);
 			}
 			}
 		//}
 		//catch (Exception e) {
 		//	log.info("[BubbleShield] : " + e.toString());	
 			
 		//}
 	}
 }
