 package net.downwithdestruction.dwdshop;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.DecimalFormat;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.block.Sign;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.ItemFrame;
 
 public class Shop {
 
 	public String name,world;
 	public int itemID,itemDamage,amount,blockX,blockY,blockZ;
 	public double buy,sell;
 	
 	private File confFile = null;
 	private FileConfiguration conf = null;
 
 	public Shop(String name) {
 		this.name = name;
 	}
 	
 	public Shop(int itemID, Location location) {
 		this.itemID = itemID;
 		this.world  = location.getWorld().getName();
 		this.blockX = location.getBlockX();
 		this.blockY = location.getBlockY();
 		this.blockZ = location.getBlockZ();
 		this.name = world+"~=~"+blockX+"~=~"+blockY+"~=~"+blockZ;
 	}
 
 
 	public Shop(Location blockLoc, int itemID, short itemDamage, int amount,
 			double buyPrice, double sellPrice) {
 		this.itemID = itemID;
 		this.itemDamage = itemDamage;
 		this.amount = amount;
 		
 		this.buy = buyPrice;
 		this.sell = sellPrice;
 
 		this.world  = blockLoc.getWorld().getName();
 		this.blockX = blockLoc.getBlockX();
 		this.blockY = blockLoc.getBlockY();
 		this.blockZ = blockLoc.getBlockZ();
 		
 		this.name = world+"~=~"+blockX+"~=~"+blockY+"~=~"+blockZ;
 		
 		DwDShopPlugin.debug("Created Shop: "+itemID+":"+itemDamage+" x"+amount+"@ B:"+buyPrice+" S:"+sellPrice+" - Admin Shop ("+blockX+","+blockY+","+blockZ+")");
 		
 	}
 
 
 	public int getItemID() {
 		return itemID;
 	}
 
 
 	public void setItemID(int itemID) {
 		this.itemID = itemID;
 	}
 
 
 	public int getItemDamage() {
 		return itemDamage;
 	}
 
 
 	public void setItemDamage(int itemDamage) {
 		this.itemDamage = itemDamage;
 	}
 
 
 	public int getAmount() {
 		return amount;
 	}
 
 
 	public void setAmount(int amount) {
 		this.amount = amount;
 	}
 
 
 	public double getBuy() {
 		return buy;
 	}
 
 
 	public double getSell() {
 		return sell;
 	}
 	
 	public Location getLocation() {
 		return Bukkit.getServer().getWorld(world).getBlockAt(blockX, blockY, blockZ).getLocation();
 	}
 	
 	public void save() {
 		confFile = new File("plugins/DwDShop/shops/", name + ".yml");
 		conf = YamlConfiguration.loadConfiguration(confFile);
 		
 		conf.set("location.world", world);
 		conf.set("location.x", blockX);
 		conf.set("location.y", blockY);
 		conf.set("location.z", blockZ);
 		conf.set("item.ID", itemID);
 		conf.set("item.damage", itemDamage);
 		conf.set("item.amount", amount);
 		conf.set("price.buy", buy);
 		conf.set("price.sell", sell);
 		
 		DwDShopPlugin.debug("World: "+world+", X:"+blockX+", Y:"+blockY+", Z:"+blockZ+", itemID:"+itemID+", itemDamage:"+itemDamage+", amount:"+amount+", buy:"+buy+", sell:"+sell);
 
 		try {
 			conf.save(confFile); // Save the file
 		} catch (IOException ex) {
 			DwDShopPlugin.log(Level.SEVERE, "Could not save config to "
 					+ confFile);
 		}
 	}
 	
 	public void load() {
 		confFile = new File("plugins/DwDShop/shops/", name + ".yml");
 		conf = YamlConfiguration.loadConfiguration(confFile);
 
 		// Set values
 		this.world = conf.getString("location.world");
 		this.blockX = conf.getInt("location.x");
 		this.blockY = conf.getInt("location.y");
 		this.blockZ = conf.getInt("location.z");
 		
 		this.itemID = conf.getInt("item.ID");
 		this.itemDamage = conf.getInt("item.damage");
 		this.amount = conf.getInt("item.amount");
 		this.buy = conf.getDouble("price.buy");
 		this.sell = conf.getDouble("price.sell");
 		
 		DwDShopPlugin.debug("World: "+world+", X:"+blockX+", Y:"+blockY+", Z:"+blockZ+", itemID:"+itemID+", itemDamage:"+itemDamage+", amount:"+amount+", buy:"+buy+", sell:"+sell);
 		
 	}
 	
 	public String[] update() {
 		
 		Location location = getLocation();
 		Sign sign = (Sign) location.getBlock().getState();
 		int x, y, z;
 		x = location.getBlockX();
 		y = location.getBlockY();
 		z = location.getBlockZ();
 
 		Entity[] entities = location.getChunk().getEntities();
 		for (Entity e : entities) {
 			Location eLoc = e.getLocation();
 			if ((eLoc.getBlockX() == x)
 					&& (eLoc.getBlockZ()) == z
 					&& (eLoc.getBlockY() > y)
 					&& (eLoc.getBlockY() <= (y + 2))
 					&& (e.getType() == EntityType.ITEM_FRAME)) {
 				// Found the entity
 
 				ItemFrame frame = (ItemFrame) e;
 				int item = frame.getItem().getTypeId();
 				short damage = frame.getItem().getDurability();
 
 				String itemID = (damage == 0) ? "" + item
 						: item + ":" + damage;
 
 				// Get Prices
 				try {
 					DwDShopPlugin.debug("Query: SELECT `buy`,`sell`,`itemName` FROM `Items` WHERE `itemID`='"
 									+ itemID + "' LIMIT 1");
 					ResultSet results = DwDShopPlugin.db
 							.query("SELECT `buy`,`sell`,`itemName` FROM `Items` WHERE `itemID`='"
 									+ itemID + "' LIMIT 1");
 					if (results.first()) {
 						DwDShopPlugin.debug("Found price");
 						double buy, sell;
 						buy = results.getDouble("buy");
 						sell = results.getDouble("sell");
 
 						// Add the local storage
						this.itemDamage = damage;
						this.buy = buy;
						this.sell = sell;
						this.blockX = location.getBlockX();
						this.blockY = location.getBlockY();
						this.blockZ = location.getBlockZ();
						this.world = location.getWorld().getName();
						this.itemID = item;
						save();
 
 						// Change the sign \o/
 						String line1, line2, line3, line4;
 
 						line1 = results.getString("itemName");
 						line2 = ""+amount;
 						
 						if(line1.length() > 16) {
 							line1 = line1.substring(0, 16);
 						}
 
 						// Lines 3-4 only go up to 10 chars (to
 						// line up) - 7 + . + decimals, always
 						// align to right!!
 						
 						buy = buy * amount;
 						sell = sell * amount;
 						
 						DecimalFormat df = new DecimalFormat("###,###.00");
 						
 						String buy1 = df.format(buy);
 						String sell1 = df.format(sell);
 
 						String buyPrice = "";
 						String sellPrice = "";
 
 						if (buy1.length() > 10) {
 							buyPrice = "999,999.00";
 						} else {
 							int spaces = 10 - buy1.length();
 							for (x = 0; x < spaces; x++) {
 								buyPrice += " ";
 							}
 							buyPrice += buy1;
 						}
 
 						if (sell1.length() > 10) {
 							sell1 = "999,999.00";
 						} else {
 							int spaces = 10 - sell1.length();
 							for (x = 0; x < spaces; x++) {
 								sellPrice += " ";
 							}
 							sellPrice += sell1;
 						}
 
 						line3 = "Buy " + buyPrice;
 						line4 = "Sell " + sellPrice;
 
 						sign.setLine(0, line1);
 						sign.setLine(1, line2);
 						sign.setLine(2, line3);
 						sign.setLine(3, line4);
 						sign.update(true);
 						String[] str = new String[] {
 							line1,
 							line2,
 							line3,
 							line4
 						};
 						
 						return str;
 					}
 				} catch (SQLException e1) {
 					e1.printStackTrace();
 				}
 
 				break;
 			}
 		}
 		return new String[] {
 				"-=-=-=-=-","Error Whilst","Parsing Sign","-=-=-=-=-"
 			};
 	}
 	
 	public void delete() {
 		confFile = new File("plugins/DwDShop/shops/", name + ".yml");
 		confFile.delete();
 	}
 
 	public String getName() {
 		return this.name;
 	}
 	
 	
 }
