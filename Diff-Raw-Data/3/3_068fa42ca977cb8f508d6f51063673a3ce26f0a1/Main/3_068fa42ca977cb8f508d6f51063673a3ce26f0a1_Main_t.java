 package com.etriacraft.EtriaShop;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.logging.Logger;
 import org.bukkit.Location;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin {
     
     public static String PREFIX;
     
     public static final Logger log = Logger.getLogger("Minecraft");
     
     public HashMap<Pair<Integer, Integer>, String> material_names;
     
     //Shop signs
     public HashMap<Location, ShopSign> signs;
     
     //Classes
     private PlayerListener playerListener;
     private BlockListener blockListener;
     private Config config;
     private Econ economy;
     private Commands commands;
 
     @Override
     public void onDisable() {
         log.info(PREFIX + "disabled.");
 
 Connection.disable();
     }
     
     @Override
     public void onEnable() {
         signs = new HashMap<Location, ShopSign>();
         material_names = new HashMap<Pair<Integer, Integer>, String>();
         
         playerListener = new PlayerListener(this);
         blockListener = new BlockListener(this, playerListener);
         config = new Config(this);
         economy = new Econ(this);
         commands = new Commands(this, playerListener, config);
         
         commands.initialize();
         
         config.initialize();
         economy.setupEconomy();
         
         getServer().getPluginManager().registerEvents(playerListener, this);
         getServer().getPluginManager().registerEvents(blockListener, this);
         
         ShopSign.plugin = this;
         
         Connection.init();
         
         loadSigns();
         populateNames();
         
     }
     
     @SuppressWarnings({ "unchecked", "rawtypes" })
 	private void populateNames() {
         material_names.put(new Pair(35, 1), "Orange Wool");
         material_names.put(new Pair(35, 2), "Magenta Wool");
         material_names.put(new Pair(35, 3), "Light Blue Wool");
         material_names.put(new Pair(35, 4), "Yellow Wool");
         material_names.put(new Pair(35, 5), "Lime Wool");
         material_names.put(new Pair(35, 6), "Pink Wool");
         material_names.put(new Pair(35, 7), "Grey Wool");
         material_names.put(new Pair(35, 8), "Light Grey Wool");
         material_names.put(new Pair(35, 9), "Cyan Wool");
         material_names.put(new Pair(35, 10), "Purple Wool");
         material_names.put(new Pair(35, 11), "Blue Wool");
         material_names.put(new Pair(35, 12), "Brown Wool");
         material_names.put(new Pair(35, 13), "Green Wool");
         material_names.put(new Pair(35, 14), "Red Wool");
         material_names.put(new Pair(35, 15), "Black Wool");
 
         material_names.put(new Pair(351, 1), "Rose Red");
         material_names.put(new Pair(351, 2), "Cactus Green");
         material_names.put(new Pair(351, 3), "Cocoa Beans");
         material_names.put(new Pair(351, 4), "Lapis Lazuli");
         material_names.put(new Pair(351, 5), "Purple Dye");
         material_names.put(new Pair(351, 6), "Cyan Dye");
         material_names.put(new Pair(351, 7), "Light Grey Dye");
         material_names.put(new Pair(351, 8), "Grey Dye");
         material_names.put(new Pair(351, 9), "Pink Dye");
         material_names.put(new Pair(351, 10), "Lime Dye");
         material_names.put(new Pair(351, 11), "Dandelion Yellow");
         material_names.put(new Pair(351, 12), "Light Blue Dye");
         material_names.put(new Pair(351, 13), "Magenta Dye");
         material_names.put(new Pair(351, 14), "Orange Dye");
         material_names.put(new Pair(351, 15), "Bone Meal");
 
         material_names.put(new Pair(44, 0), "Stone Slab");
         material_names.put(new Pair(44, 1), "Sandstone Slab");
         material_names.put(new Pair(44, 2), "Wooden Slab");
         material_names.put(new Pair(44, 3), "Cobblestone Slab");
         material_names.put(new Pair(44, 4), "Bricks Slab");
         material_names.put(new Pair(44, 5), "Stone Bricks Slab");
 
         material_names.put(new Pair(98, 0), "Stone Brick");
         material_names.put(new Pair(98, 1), "Mossy Stone Brick");
         material_names.put(new Pair(98, 2), "Cracked Stone Brick");
         material_names.put(new Pair(98, 3), "Circle Stone Brick");
 
 material_names.put(new Pair(17, 0), "Oak Logs");
 material_names.put(new Pair(17, 1), "Pine Logs");
 material_names.put(new Pair(17, 2), "Birch Logs");
 material_names.put(new Pair(17, 3), "Jungle Logs");
 
 material_names.put(new Pair(18, 0), "Oak Leaves");
 material_names.put(new Pair(18, 1), "Pine Leaves");
 material_names.put(new Pair(18, 2), "Birch Leaves");
 material_names.put(new Pair(18, 3), "Jungle Leaves");
 
 material_names.put(new Pair(6, 0), "Oak Sapling");
 material_names.put(new Pair(6, 1), "Pine Sapling");
 material_names.put(new Pair(6, 2), "Birch Sapling");
 material_names.put(new Pair(6, 3), "Jungle Sapling");
 
 material_names.put(new Pair(263, 1), "Charcoal");
 
 material_names.put(new Pair(32, 0), "Dead Shrub");
 material_names.put(new Pair(32, 1), "Tall Grass");
 material_names.put(new Pair(32, 2), "Fern");
 
 material_names.put(new Pair(383, 90), "Pig Egg");
 material_names.put(new Pair(383, 91), "Sheep Egg");
 material_names.put(new Pair(383, 92), "Cow Egg");
 material_names.put(new Pair(383, 93), "Chicken Egg");
 material_names.put(new Pair(383, 95), "Wolf Egg");
 material_names.put(new Pair(383, 96), "Mooshroom Egg");
 material_names.put(new Pair(383, 98), "Ocelot Egg");
 material_names.put(new Pair(383, 120), "Villager Egg");
     }
     
     public void loadSigns() {
         signs.clear();
         
         ResultSet rs = Connection.query("SELECT shops.*, chests.world AS 'chest_world', chests.x AS 'chest_x', chests.y AS 'chest_y', chests.z AS 'chest_z' FROM shops LEFT JOIN chests ON chests.id = shops.chestid", false);
         try {
             while (rs.next()) {
                 Location chest = null;
                 
                 rs.getInt("chestid");
                 if (!rs.wasNull() && rs.getInt("chestid") > 0) chest = new Location(getServer().getWorld(rs.getString("chest_world")), rs.getInt("chest_x"), rs.getInt("chest_y"), rs.getInt("chest_z"));
                 
                 Location sign = new Location(getServer().getWorld(rs.getString("world")), (double)rs.getInt("x"), (double)rs.getInt("y"), (double)rs.getInt("z"));
                 
                 ShopSign ss = new ShopSign(sign, chest, rs.getInt("item"), rs.getInt("data"), rs.getInt("amount"), rs.getFloat("buy_price"), rs.getFloat("sell_price"), rs.getString("shopType"), rs.getString("owner"));
                 
                 signs.put(sign, ss);
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
     
     public String prettify(String s) {
         StringBuilder b = new StringBuilder(s.toLowerCase().replace("_", " "));
         int i = 0;
         do {
         b.replace(i, i + 1, b.substring(i,i + 1).toUpperCase());
         i = b.indexOf(" ", i) + 1;
         } while (i > 0 && i < b.length());
 
         return b.toString();
     }
 
 }
