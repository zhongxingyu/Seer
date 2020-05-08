 package com.bukkit.gemo.FalseBook.IC;
 
 import com.bukkit.gemo.FalseBook.IC.ICs.*;
 import java.io.File;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Map.Entry;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 public class PersistenceHandler {
 
     private FalseBookICCore plugin = null;
     private ICFactory factory = null;
     private DatabaseHandler dbHandler = null;
 
     public PersistenceHandler(FalseBookICCore plugin) {
         this.plugin = plugin;
     }
 
     public void init(ICFactory factory) {
         this.factory = factory;
     }
 
     public void initSQLite() {
         this.dbHandler = new DatabaseHandler("plugins/FalseBook", "SelftriggeredICs");
     }
 
     public void initMySQL() {
         createMYSQLConfig();
         try {
             File file = new File("plugins/FalseBook/MySQL.yml");
             YamlConfiguration config = new YamlConfiguration();
             config.load(file);
             this.dbHandler = new DatabaseHandler(config.getString("mysql.host"), config.getInt("mysql.port"), config.getString("mysql.database"), config.getString("mysql.username"), config.getString("mysql.password"));
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private void createMYSQLConfig() {
         File file = new File("plugins/FalseBook/MySQL.yml");
         if (file.exists()) {
             return;
         }
         try {
             YamlConfiguration config = new YamlConfiguration();
 
             config.set("mysql.host", "localhost");
             config.set("mysql.port", Integer.valueOf(3306));
             config.set("mysql.database", "databaseName");
             config.set("mysql.username", "username");
             config.set("mysql.password", "password");
 
             config.save(file);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public void closeSQL() {
         this.dbHandler.closeConnection();
     }
 
     public void loadSelftriggeredICs() {
         ResultSet result = this.dbHandler.getAllICs();
         int loadedICs = 0;
         try {
             String WorldName;
             while ((result != null) && (result.next())) {
                 int ID = result.getInt("Id");
                 WorldName = result.getString("WorldName");
                 int x = result.getInt("SignX");
                 int y = result.getInt("SignY");
                 int z = result.getInt("SignZ");
                 Location location = new Location(Bukkit.getServer().getWorld(WorldName), x, y, z, 0.0F, 0.0F);
                 try {
                     if (location.getWorld() != null) {
                         location.getWorld().loadChunk(location.getBlock().getChunk().getX(), location.getBlock().getChunk().getZ(), true);
 
                         SelftriggeredBaseIC newIC = null;
                         int resID = result.getInt("SensorId");
 
                         if (location.getBlock().getTypeId() == Material.WALL_SIGN.getId()) {
                             Sign signBlock = (Sign)location.getBlock().getState();
                            BaseIC thisIC = factory.getIC(signBlock.getLine(0).toLowerCase());;
  
                             if (thisIC == null) {
                                 boolean upgraded;
 
                                 // Loop over all upgrades.
                                 do {
                                     upgraded = false;
                                     if(ICUpgrade.needsUpgrade(signBlock.getLine(1))) {
                                         ICUpgrader u = ICUpgrade.getUpgrader(signBlock.getLine(1));
                                         if(u.preCheckUpgrade(signBlock)) {
                                             u.upgrade(signBlock);
                                             upgraded = true;
                                         }
                                     }
                                     else if(ICUpgrade.needsUpgrade(signBlock.getLine(0))) {
                                         ICUpgrader u = ICUpgrade.getUpgrader(signBlock.getLine(0));
                                         if(u.preCheckUpgrade(signBlock)) {
                                             u.upgrade(signBlock);
                                             upgraded = true;
                                         }
                                     }
                                     if(upgraded) {
                                         String newName = signBlock.getLine(0).toLowerCase();
                                         thisIC = factory.getIC(newName);
                                         resID = newName.hashCode();
                                     }
                                 } while(upgraded && thisIC == null);
                                 
                                 if(upgraded && thisIC != null) {
                                     this.dbHandler.updateIC(ID, resID);
                                 }
                             }
                             
                             SelftriggeredBaseIC nIC = null;
                             if(thisIC instanceof SelftriggeredBaseIC &&
                                     resID == signBlock.getLine(0).toLowerCase().hashCode()) {
                                 nIC = (SelftriggeredBaseIC)thisIC;
                             }
                             
                             if (nIC != null) {
                                 try {
                                     boolean startUpComplete = false;
                                     newIC = nIC.getClass().newInstance();
                                     startUpComplete = newIC.initIC(this.plugin, location);
                                     if ((newIC.getSignBlock() == null) || (!startUpComplete)) {
                                         this.factory.addFailedIC(new NotLoadedIC(ID, "UNKNOWN", "NO SIGN FOUND", location));
                                         continue;
                                     }
                                     startUpComplete = newIC.onLoad(newIC.getSignBlock().getLines());
                                     if (startUpComplete) {
                                         newIC.initCore();
                                         newIC.initIC(FalseBookICCore.getInstance(), location);
                                         this.factory.addSelftriggeredIC(location, newIC);
                                         loadedICs++;
                                         continue;
                                     }
                                     this.factory.addFailedIC(new NotLoadedIC(ID, newIC.getSignBlock().getLine(0), "FAILED", location));
                                 } catch (Exception e) {
                                     e.printStackTrace();
                                 }
                             } else {
                                 this.factory.addFailedIC(new NotLoadedIC(ID, "UNKNOWN", "NO IC FOUND ON THE SIGN", location));
                             }
                         } else {
                             this.factory.addFailedIC(new NotLoadedIC(ID, "UNKNOWN", "NO SIGN FOUND", location));
                         }
                     }
                 } catch (Exception e) {
                     this.factory.addFailedIC(new NotLoadedIC(ID, "UNKNOWN", "BUKKIT ERROR - try /fbic reloadics", location));
                 }
             }
 
             FalseBookICCore.printInConsole("Loaded selftriggered ICs: " + loadedICs + " done");
             FalseBookICCore.printInConsole("Failed selftriggered ICs: " + this.factory.getFailedICsSize() + " failed");
             if (this.factory.getFailedICsSize() > 0) {
                 FalseBookICCore.printInConsole("List of failed ICs: ");
                 for (NotLoadedIC thisIC : this.factory.getFailedICs()) {
                     FalseBookICCore.printInConsole("ID: " + thisIC.getID() + ", " + thisIC.getICNumber() + " - " + thisIC.getName() + " @ Location - World: " + thisIC.getICLocation().getWorld().getName() + " , X: " + thisIC.getICLocation().getBlockX() + " , Y: " + thisIC.getICLocation().getBlockY() + " , Z: " + thisIC.getICLocation().getBlockZ());
                 }
             }
         } catch (SQLException e) {
             FalseBookICCore.printInConsole("Error while loading selftriggered ICs: ");
             FalseBookICCore.printInConsole("");
             e.printStackTrace();
         }
     }
 
     public boolean addSelftriggeredICToDB(int SensorID, Location location) {
         if (STICExistsInDB(location)) {
             return false;
         }
         return this.dbHandler.addIC(SensorID, location);
     }
 
     public int getNextID() {
         return this.dbHandler.getNextID();
     }
 
     public void removeSelftriggeredIC(Location location) {
         this.dbHandler.removeSelftriggeredIC(location);
     }
 
     public void removeSelftriggeredIC(int SensorID) {
         this.dbHandler.removeSelftriggeredIC(SensorID);
     }
 
     public void clearAllSelftriggeredICs() {
         this.dbHandler.deleteAllICs();
     }
 
     public boolean STICExistsInDB(Location location) {
         return this.dbHandler.ICExists(location);
     }
 }
