 package tv.mineinthebox.ManCo.utils;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Entity;
 import org.bukkit.inventory.ItemStack;
 
 import tv.mineinthebox.ManCo.logType;
 import tv.mineinthebox.ManCo.manCo;
 
 @SuppressWarnings("deprecation")
 public class rareCrateList {
 	public static HashMap<Entity, String> getFallingStateChest = new HashMap<Entity, String>();
 	public static HashMap<String, Chest> getCrateList = new HashMap<String, Chest>();
 	public static HashMap<String, Chest> getCrateList2 = new HashMap<String, Chest>();
 	public static ArrayList<String> ListDataValues = new ArrayList<String>();
 	public static HashMap<String, ItemStack[]> ItemsFromChest = new HashMap<String, ItemStack[]>();
 	public static HashMap<Location, Block> chestLocations = new HashMap<Location, Block>();
 	public static ArrayList<String> schedulerTime = new ArrayList<String>();
 	
	public static void setRandomItems(Chest chest, String RareID) {
		if(!ListDataValues.isEmpty()) {
			ListDataValues.clear();
		}
 		try {
 			File f = new File(manCo.getPlugin().getDataFolder() + File.separator + "items.yml");
 			if(f.exists()) {
 				FileConfiguration con = YamlConfiguration.loadConfiguration(f);
				for(String idDate : con.getStringList("rarecrates."+RareID+".items")) {
 					ListDataValues.add(idDate);
 				}
 				chest.getInventory().setItem(0, getItemStack());
 				chest.getInventory().setItem(1, getItemStack());
 				chest.getInventory().setItem(2, getItemStack());
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static ItemStack getItemStack() {
 		try {
 			Random rand = new Random();
 			int number = rand.nextInt(ListDataValues.size() + 0);
 			String[] splitItemName = ListDataValues.get(number).split(":");
 			int itemID = Integer.parseInt(splitItemName[0]);
 			int subData = Integer.parseInt(splitItemName[1]);
 			int amount = Integer.parseInt(splitItemName[2]);
 			ItemStack item = new ItemStack(Material.getMaterial(itemID));
 			item.setAmount(amount);
 			item.setDurability((byte) subData);
 			return item;
 		} catch(IllegalArgumentException e) {
 			manCo.log("it seems one of your items in items.yml is wrong!", logType.severe);
 		}
 		return null;
 	}
 }
