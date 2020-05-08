 package de.minestar.moneypit.database;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.Collection;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Hanging;
 import org.bukkit.entity.ItemFrame;
 import org.bukkit.entity.Painting;
 
 import de.minestar.minestarlibrary.database.AbstractSQLiteHandler;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 import de.minestar.moneypit.MoneyPitCore;
 import de.minestar.moneypit.data.BlockVector;
 import de.minestar.moneypit.data.protection.Protection;
 import de.minestar.moneypit.data.protection.ProtectionType;
 import de.minestar.moneypit.modules.Module;
 import de.minestar.moneypit.utils.ListHelper;
 
 public class DatabaseManager extends AbstractSQLiteHandler {
 
     private PreparedStatement addProtection, removeProtection, updateGuestList, getProtectionAtPosition;
     private PreparedStatement loadAllProtections;
 
     public DatabaseManager(String pluginName, File SQLConfigFile) {
         super(pluginName, SQLConfigFile);
     }
 
     @Override
     protected void createStructure(String pluginName, Connection con) throws Exception {
         StringBuilder builder = new StringBuilder();
 
         // open statement
         builder.append("CREATE TABLE IF NOT EXISTS `tbl_protections` (");
 
         // Unique ID
         builder.append("`ID` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT");
         builder.append(", ");
 
         // Protectionowner
         builder.append("`owner` TEXT NOT NULL");
         builder.append(", ");
 
         // ProtectionType as Integer
         builder.append("`protectionType` INTEGER NOT NULL");
         builder.append(", ");
 
         // Worldname
         builder.append("`blockWorld` TEXT NOT NULL");
         builder.append(", ");
 
         // BlockX
         builder.append("`blockX` INTEGER NOT NULL");
         builder.append(", ");
 
         // BlockY
         builder.append("`blockY` INTEGER NOT NULL");
         builder.append(", ");
 
         // BlockZ
         builder.append("`blockZ` INTEGER NOT NULL");
         builder.append(", ");
 
         // GuestList - Format : GUEST;GUEST;GUEST
         builder.append("`guestList` TEXT NOT NULL");
 
         // close statement
         builder.append(");");
 
         // execute statement
         PreparedStatement statement = con.prepareStatement(builder.toString());
         statement.execute();
 
         // clear
         statement = null;
         builder.setLength(0);
     }
 
     @Override
     protected void createStatements(String pluginName, Connection con) throws Exception {
         //@formatter:off;
         this.addProtection              = con.prepareStatement("INSERT INTO `tbl_protections` (owner, protectionType, blockWorld, blockX, blockY, blockZ, guestList) VALUES (?, ?, ?, ?, ?, ?, ?);");
         this.removeProtection           = con.prepareStatement("DELETE FROM `tbl_protections` WHERE ID=?;");
         this.updateGuestList            = con.prepareStatement("UPDATE `tbl_protections` SET guestList=? WHERE ID=?;");
         this.getProtectionAtPosition    = con.prepareStatement("SELECT * FROM `tbl_protections` WHERE blockWorld=? AND blockX=? AND blockY=? AND blockZ=? LIMIT 1;");
         this.loadAllProtections         = con.prepareStatement("SELECT * FROM `tbl_protections` ORDER BY ID ASC");
         //@formatter:on;
     }
 
     /**
      * Init this Manager
      */
     public void init() {
         this.loadAllProtections();
     }
 
     /**
      * Get the protection at a certain BlockVector
      * 
      * @param vector
      * @return the Protection
      */
     public Protection getProtectionAtPosition(BlockVector vector) {
         try {
             this.getProtectionAtPosition.setString(1, vector.getWorldName());
             this.getProtectionAtPosition.setInt(2, vector.getX());
             this.getProtectionAtPosition.setInt(3, vector.getY());
             this.getProtectionAtPosition.setInt(4, vector.getZ());
             ResultSet results = this.getProtectionAtPosition.executeQuery();
             while (results.next()) {
                 return new Protection(results.getInt("ID"), vector.getRelative(0, 0, 0), results.getString("owner"), ProtectionType.byID(results.getInt("protectionType")));
             }
             return null;
         } catch (Exception e) {
             ConsoleUtils.printException(e, MoneyPitCore.NAME, "Can't get the protection at position: " + vector.toString() + "!");
             return null;
         }
     }
 
     /**
      * Create a new Protection
      * 
      * @param vector
      * @param owner
      * @param type
      * @return the newly created Protection
      */
     public Protection createProtection(BlockVector vector, String owner, ProtectionType type) {
         try {
             this.addProtection.setString(1, owner);
             this.addProtection.setInt(2, type.getID());
             this.addProtection.setString(3, vector.getWorldName());
             this.addProtection.setInt(4, vector.getX());
             this.addProtection.setInt(5, vector.getY());
             this.addProtection.setInt(6, vector.getZ());
             this.addProtection.setString(7, "");
             this.addProtection.executeUpdate();
             return this.getProtectionAtPosition(vector);
         } catch (Exception e) {
             ConsoleUtils.printException(e, MoneyPitCore.NAME, "Can't create protection!");
             return null;
         }
     }
 
     /**
      * Update the guestlist of a protection
      * 
      * @param protection
      * @param guestList
      * @return <b>true</b> if the update was successful, otherwise <b>false</b>
      */
     public boolean updateGuestList(Protection protection, String guestList) {
         try {
             this.updateGuestList.setString(1, guestList);
             this.updateGuestList.setInt(2, protection.getID());
             this.updateGuestList.executeUpdate();
             return true;
         } catch (Exception e) {
             ConsoleUtils.printException(e, MoneyPitCore.NAME, "Can't save guestList in database! ID=" + protection.getID());
             return false;
         }
     }
 
     /**
      * Delete a given protection
      * 
      * @param protection
      * @return <b>true</b> if the deletion was successful, otherwise <b>false</b>
      */
     public boolean deleteProtection(BlockVector vector) {
         try {
             Protection protection = this.getProtectionAtPosition(vector);
             if (protection != null) {
                 this.removeProtection.setInt(1, protection.getID());
                 this.removeProtection.executeUpdate();
                 return true;
             } else {
                 return false;
             }
         } catch (Exception e) {
             ConsoleUtils.printException(e, MoneyPitCore.NAME, "Can't delete protection from database @ " + vector.toString());
             return false;
         }
     }
 
     /**
      * Load all protections from the Database
      */
     private void loadAllProtections() {
         try {
             ResultSet results = this.loadAllProtections.executeQuery();
             int count = 0;
             int noProtectionCount = 0;
             while (results.next()) {
                 BlockVector vector = new BlockVector(results.getString("blockWorld"), results.getInt("blockX"), results.getInt("blockY"), results.getInt("blockZ"));
                 try {
                     Location location = vector.getLocation();
                     if (location == null) {
                         ++noProtectionCount;
                         continue;
                     }
 
                     location.getChunk().load(true);
                     Module module = MoneyPitCore.moduleManager.getRegisteredModule(location.getBlock().getTypeId());
 
                     Hanging entityHanging = null;
 
                     if (module == null) {
                         Collection<ItemFrame> frameList = location.getWorld().getEntitiesByClass(ItemFrame.class);
                         boolean found = false;
                         for (ItemFrame frame : frameList) {
                             BlockVector otherVector = new BlockVector(frame.getLocation());
                             if (vector.equals(otherVector)) {
                                 module = MoneyPitCore.moduleManager.getRegisteredModule(Material.ITEM_FRAME.getId());
                                 entityHanging = frame;
                                 found = true;
                                 break;
                             }
                         }
                         if (!found) {
                             Collection<Painting> paintList = location.getWorld().getEntitiesByClass(Painting.class);
                             for (Painting paint : paintList) {
                                 BlockVector otherVector = new BlockVector(paint.getLocation());
                                 if (vector.equals(otherVector)) {
                                    module = MoneyPitCore.moduleManager.getRegisteredModule(Material.PAINTING.getId());
                                     entityHanging = paint;
                                     found = true;
                                     break;
                                 }
                             }
                             if (!found) {
                                 ++noProtectionCount;
                                 continue;
                             }
                         }
                     }
                     Protection protection = new Protection(results.getInt("ID"), vector, results.getString("owner"), ProtectionType.byID(results.getInt("protectionType")));
                     protection.setGuestList(ListHelper.toList(results.getString("guestList")));
                     byte subData = location.getBlock().getData();
                     if (module.getRegisteredTypeID() == Material.ITEM_FRAME.getId() || module.getRegisteredTypeID() == Material.PAINTING.getId()) {
                         if (entityHanging != null) {
                             subData = (byte) entityHanging.getAttachedFace().ordinal();
                         } else {
                             ++noProtectionCount;
                             continue;
                         }
                     }
                     if (module.addProtection(protection, subData)) {
                         ++count;
                     } else {
                         ++noProtectionCount;
                     }
                 } catch (Exception error) {
                     ConsoleUtils.printWarning(MoneyPitCore.NAME, "Can't load protection: ID=" + results.getInt("ID") + " -> " + vector.toString());
                 }
             }
             ConsoleUtils.printInfo(MoneyPitCore.NAME, count + " protections loaded!");
             if (noProtectionCount > 0) {
                 ConsoleUtils.printInfo(MoneyPitCore.NAME, noProtectionCount + " protections are NOT loaded due to missing blocks or locations!");
             }
         } catch (Exception e) {
             ConsoleUtils.printException(e, MoneyPitCore.NAME, "Can't load protections!");
         }
     }
 }
