 package muCkk.DeathAndRebirth.ghost;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import muCkk.DeathAndRebirth.DAR;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Graves {
 	
 	private FileConfiguration customConfig = null;
 	private File graveFile;
 	private DAR plugin;
 	private Ghosts ghosts;
 	
 	public Graves(DAR instance, String dataDir) {
 		this.plugin = instance;
 		graveFile = new File(dataDir+"/graves");
 	}
 	
 	public void reloadCustomConfig() {
 	    if (graveFile == null) {
 	    	graveFile = new File(plugin.getDataFolder(), "graves");
 	    }
 	    customConfig = YamlConfiguration.loadConfiguration(graveFile);
 	 
 	    // Look for defaults in the jar
 	    InputStream defConfigStream = plugin.getResource("graves");
 	    if (defConfigStream != null) {
 	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
 	        customConfig.setDefaults(defConfig);
 	    }
 	}
 	
 	public FileConfiguration getCustomConfig() {
 	    if (customConfig == null) {
 	        reloadCustomConfig();
 	    }
 	    return customConfig;
 	}
 	
 	public void saveCustomConfig() {
 	    if (customConfig == null || graveFile == null) {
 	    	return;
 	    }
 	    try {
 	        customConfig.save(graveFile);
 	    } catch (IOException ex) {
 	        Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save config to " + graveFile, ex);
 	    }
 	}
 	
 	public void setGhosts(Ghosts ghosts) {
 		this.ghosts = ghosts;
 	}
 	public void addGrave(String name, Block block, String l1, String world) {		
 //		if (config.getBoolean(CFG.GRAVE_SIGNS)) placeSign(block, l1, name);
 		int x = block.getX();
 		int y = block.getY();
 		int z = block.getZ();
 		Block otherBlock;
 		boolean placed = false;
	/*	// water
		if (block.getTypeId() == 8 || block.getTypeId() == 9) {
 			otherBlock = block.getRelative(BlockFace.UP);
 			while (otherBlock.getTypeId() != 0) {
 				otherBlock = otherBlock.getRelative(BlockFace.UP);
 			}
 			otherBlock.getRelative(BlockFace.DOWN).setTypeId(3);
 			x = otherBlock.getX();
 			y = otherBlock.getY();
 			z = otherBlock.getZ();
 			if (plugin.getConfig().getBoolean("GRAVE_SIGNS")) {
 				placeSign(otherBlock, l1, name);
 				ghosts.setLocationOfDeath(otherBlock, name);
 				placed = true;
 			}
		} */
 //		else if (config.getBoolean(CFG.GRAVE_SIGNS)) placeSign(block, l1, name);
 		
 		// explosion
 		if (block.getRelative(BlockFace.DOWN).getTypeId() == 0) {
 			otherBlock = block.getRelative(BlockFace.DOWN);
 			while (otherBlock.getRelative(BlockFace.DOWN).getTypeId() == 0) {
 				otherBlock = otherBlock.getRelative(BlockFace.DOWN);
 			}
 			x = otherBlock.getX();
 			y = otherBlock.getY();
 			z = otherBlock.getZ();
 			if (plugin.getConfig().getBoolean("GRAVE_SIGNS")) {
 				placeSign(otherBlock, l1, name);
 				ghosts.setLocationOfDeath(otherBlock, name);
 				placed= true;
 			}
 		}
 		
 		if (plugin.getConfig().getBoolean("GRAVE_SIGNS") && !placed) {
 			placeSign(block, l1, name);
 			ghosts.setLocationOfDeath(block, name);
 		}
 			
 		getCustomConfig().set("graves." +world +"." +name+".x", x);
 		getCustomConfig().set("graves." +world +"." +name+".y", y);
 		getCustomConfig().set("graves." +world +"." +name+".z", z);
 		getCustomConfig().set("graves." +world +"." +name+".l1", l1);
 		getCustomConfig().set("graves." +world +"." +name+".l2", name);
 		
 		saveCustomConfig();
 	}
 	
 	public void deleteGrave(Block block, String name, String worldName) {
 		// check for lava, now for water too
 		if (getCustomConfig().getInt("graves."+block.getWorld().getName()+"."+name+".blockid", 0) != 10 && getCustomConfig().getInt("graves."+block.getWorld().getName()+"."+name+".blockid", 0) != 11 && getCustomConfig().getInt("graves."+block.getWorld().getName()+"."+name+".blockid", 0) != 10 && getCustomConfig().getInt("graves."+block.getWorld().getName()+"."+name+".blockid", 0) != 9) {
 			if (plugin.getConfig().getBoolean("GRAVE_SIGNS")) removeSign(block, name, worldName);
 		}
 		getCustomConfig().set("graves." +worldName +"." +name, null);
 		saveCustomConfig();
 	}
 	
 	public void placeSign(Block block, String l1, String name) {
 		int id = block.getTypeId();
 		getCustomConfig().set("graves."+block.getWorld().getName()+"."+name+".blockid", id);
 		if (id == 43 || id == 44 || id == 35) {
 			int data = (int) block.getData();
 			getCustomConfig().set("graves."+block.getWorld().getName()+"."+name+".blockdata", data);
 		}
 		//avoid placing signs in lava, now on water too
		if(id == 10 || id == 11 || id == 9 || id == 8) return;
 		
 		block.setType(Material.SIGN_POST);
 		Sign sign = (Sign) block.getState();
 		sign.setLine(1, l1);
 		sign.setLine(2, name);
 		if(ghosts.getCustomConfig().getBoolean("players."+name +"."+ block.getWorld().getName() +".offline"))
 		{
 			sign.setLine(3, "(offline)");
 		}
 		sign.update(true);
 	}
 	
 	public void removeSign(Block block, String name, String worldName) {
 		int id = getCustomConfig().getInt("graves."+worldName+"."+name+".blockid", 0);
 		int x =  getCustomConfig().getInt("graves."+worldName+"."+name+".x", block.getX());
 		int y =  getCustomConfig().getInt("graves."+worldName+"."+name+".y", block.getY());
 		int z =  getCustomConfig().getInt("graves."+worldName+"."+name+".z", block.getZ());
 //		block.setTypeId(id);
 		block.getWorld().getBlockAt(x, y, z).setTypeId(id);
 		if (id == 43 || id == 44 || id == 35) block.setData((byte)getCustomConfig().getInt("graves."+worldName+"."+name+".blockdata", 0));
 	}
 	
 	public boolean isProtected(String name, String world, int x, int y, int z) {
 		ConfigurationSection cfgsel = getCustomConfig().getConfigurationSection("graves." +world);
 		if (cfgsel == null) return false;
 		Set<String> graves = cfgsel.getKeys(false);
 		try {
 			for (String grave : graves) {
 				if(grave.equalsIgnoreCase(name)
 						&& getCustomConfig().getInt("graves." +world +"." +grave+".x", 0) == x
 						&&	getCustomConfig().getInt("graves." +world +"." +grave+".y", 0) == y
 						&&	getCustomConfig().getInt("graves." +world +"." +grave+".z", 0) == z)
 					
 						return true;
 			}
 		}catch (NullPointerException e) {
 			return false;
 		}
 		return false;		
 	}
 }
