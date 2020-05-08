 package net.aesircraft.machina.Objects;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import net.aesircraft.machina.Data.DataHolder;
 import net.aesircraft.machina.Machina;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class MachinaBlock {
 
     public static HashMap<Player, MachinaBlock> open = new HashMap<Player, MachinaBlock>();
     private Location location;
     private ManaLocation mloc;
     private List<Block> blocks = new ArrayList<Block>();
     private boolean powered;
     private String user;
     private int id;
     private short data;
     private boolean maint = false;
     private Player player;
     private int[] range = new int[4];
 
     public MachinaBlock(Block b) {
 	location = b.getLocation();
 	mloc = new ManaLocation(b.getLocation());
     }
 
     public MachinaBlock(String location) {
 	mloc = new ManaLocation(location);
 	this.location = mloc.getLocation();
     }
 
     public boolean isValid() {
 	int id = location.getBlock().getTypeId();
 	byte data = location.getBlock().getData();
 	if ((this.id == id && this.data == data)) {
 	    delete();
 	    return false;
 	}
 	return true;
     }
 
     public void newBlock(Player player) {
 	range[0] = location.getBlockX() - 25;
 	range[1] = location.getBlockX() + 25;
 	range[2] = location.getBlockZ() - 25;
 	range[3] = location.getBlockZ() + 25;
 	id = 0;
 	data = 0;
 	maint = false;
 	user = player.getName();
 	saveStats();
     }
 
     public void load() {
 	range[0] = location.getBlockX() - 25;
 	range[1] = location.getBlockX() + 25;
 	range[2] = location.getBlockZ() - 25;
 	range[3] = location.getBlockZ() + 25;
 	loadBlocks();
     }
 
     public boolean withingRange(Block block) {
 	int x = block.getX();
 	int z = block.getZ();
 	return (x >= range[0] && x <= range[1] && z >= range[2] && z <= range[3]);
     }
 
     public String getOwner() {
 	return user;
     }
 
     public void setBlocks() {
 	List<Block> l = blocks;
 	List<Block> r = new ArrayList<Block>();
 	boolean save = false;
 	for (Block b : l) {
 	    if (b.getTypeId() != 0) {
 		r.add(b);
 		//DataHolder.blocks.remove(b);
 		save = true;
 		continue;
 	    }
 	    b.setTypeIdAndData(id, (byte) data, true);
 	}
 	if (save) {
 	    for (Block b : r) {
 		blocks.remove(b);
 		saveBlocks();
 	    }
 	}
 	powered = true;
 	saveStats();
     }
 
     public void removeBlocks() {
 	List<Block> l = blocks;
 	List<Block> r = new ArrayList<Block>();
 	boolean save = false;
 	for (Block b : l) {
 	    if (b.getTypeId() != id || b.getData() != data) {
 		r.add(b);
 		//DataHolder.blocks.remove(b);
 		save = true;
 		continue;
 	    }
 	    b.setTypeIdAndData(0, (byte) 0, true);
 	}
 	if (save) {
 	    for (Block b : r) {
 		blocks.remove(b);
 		saveBlocks();
 	    }
 	}
 	powered = false;
 	saveStats();
     }
 
     private File getFile() {
 	File dir = Machina.getStatic().getDataFolder();
 	File dir2 = new File(dir, "MachinaBlocks");
 	File file = new File(dir2, mloc.getString() + ".mbf");
 	if (!dir.exists()) {
 	    dir.mkdir();
 	}
 	if (!dir2.exists()) {
 	    dir2.mkdir();
 	}
 	if (!file.exists()) {
 	    try {
 		file.createNewFile();
 	    } catch (IOException ex) {
 		Machina.logger.log(Level.SEVERE, "[Machina] Failed to write machine file!");
 	    }
 	}
 	return file;
     }
 
     public void removeBlock(Block block) {
 	blocks.remove(block);
 	//DataHolder.blocks.remove(block);
 	saveBlocks();
     }
 
     public void addBlock(Block block) {
 	blocks.add(block);
 	//DataHolder.blocks.put(block, this);
 	saveBlocks();
     }
 
     public void setType(ItemStack item) {
 	id = item.getTypeId();
 	data = item.getDurability();
 	saveStats();
     }
 
     public ItemStack getType() {
 	return new ItemStack(id, 1, data);
     }
 
     public void clearType() {
 	id = 0;
 	data = 0;
 	saveStats();
     }
 
     public Block getBlock() {
 	return location.getBlock();
     }
 
     private YamlConfiguration getYaml() {
 	YamlConfiguration config = new YamlConfiguration();
 	try {
 	    config.load(getFile());
 	} catch (FileNotFoundException ex) {
 	    Machina.logger.log(Level.SEVERE, "[Machina] Failed to find machine file!");
 	} catch (IOException ex) {
 	    Machina.logger.log(Level.SEVERE, "[Machina] Failed to load machine file!");
 	} catch (InvalidConfigurationException ex) {
 	    Machina.logger.log(Level.SEVERE, "[Machina] Failed to understand machine file!");
 	}
 	return config;
     }
 
     private void save(YamlConfiguration config) {
 	try {
 	    config.save(getFile());
 	} catch (IOException ex) {
 	    Machina.logger.log(Level.SEVERE, "[Machina] Failed to write macine file!");
 	}
     }
 
     public int getBlocks() {
 	return blocks.size();
     }
 
     private void loadBlocks() {
 	YamlConfiguration config = getYaml();
 	List<String> l = config.getStringList("blocks");
 	user = config.getString("user", null);
 	powered = config.getBoolean("powered", false);
 	if (l == null) {
 	    return;
 	}
 	blocks = new ArrayList<Block>();
 	for (String s : l) {
 	    ManaLocation ml = new ManaLocation(s);
 	    blocks.add(ml.getLocation().getBlock());
 	    //DataHolder.blocks.put(ml.getLocation().getBlock(), this);
 	}
 	id = config.getInt("id", 0);
 	data = (short) config.getInt("data", 0);
     }
 
     private void delete() {
 	getFile().delete();
     }
 
     public void setWorking(Player player) {
 	if (!location.getBlock().isBlockPowered()) {
 	    powered = true;
 	    setBlocks();
 	}
 	this.player = player;
 	maint = true;
 	DataHolder.workings.put(player, this);
     }
 
     public boolean isWorking() {
 	return maint;
     }
 
     public void finishWorking() {
 	if (!location.getBlock().isBlockPowered()) {
 	    powered = false;
 	    removeBlocks();
 	}
 	maint = false;
 	DataHolder.workings.remove(player);
 	player = null;
     }
 
     public boolean isPowered() {
 	return powered;
     }
 
     public Player getWorking() {
 	return player;
     }
 
     public void save() {
 	saveBlocks();
 	saveStats();
     }
 
     public void saveBlocks() {
 	YamlConfiguration config = getYaml();
 	List<String> st = new ArrayList<String>();
 	for (Block b : blocks) {
 	    ManaLocation ml = new ManaLocation(b.getLocation());
 	    st.add(ml.getString());
 	}
 	config.set("blocks", st);
 	save(config);
     }
 
     public void saveStats() {
 	YamlConfiguration config = getYaml();
 	config.set("user", user);
 	config.set("powered", powered);
 	config.set("id", id);
 	config.set("data", data);
 	save(config);
     }
 
     public void breakNaturally() {
 	if (powered) {
 	    removeBlocks();
 	}
 	for (Block b : blocks) {
 	    for (Iterator<org.bukkit.inventory.ItemStack> it = b.getDrops().iterator(); it.hasNext();) {
 		org.bukkit.inventory.ItemStack i = it.next();
 		if (i == null) {
 		    continue;
 		}
 		location.getWorld().dropItem(location, i);
 	    }
	}
 	delete();
     }
 }
