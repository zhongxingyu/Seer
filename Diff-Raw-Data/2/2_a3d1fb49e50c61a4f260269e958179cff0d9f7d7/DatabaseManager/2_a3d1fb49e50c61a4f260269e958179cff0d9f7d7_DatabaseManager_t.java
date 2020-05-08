 package de.minestar.moneypit.database;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 
 import de.minestar.minestarlibrary.config.MinestarConfig;
 import de.minestar.minestarlibrary.database.AbstractDatabaseHandler;
 import de.minestar.minestarlibrary.database.DatabaseConnection;
 import de.minestar.minestarlibrary.database.DatabaseType;
 import de.minestar.minestarlibrary.database.DatabaseUtils;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 import de.minestar.moneypit.Core;
 import de.minestar.moneypit.data.BlockVector;
 import de.minestar.moneypit.data.protection.Protection;
 import de.minestar.moneypit.data.protection.ProtectionType;
 
 public class DatabaseManager extends AbstractDatabaseHandler {
 
     private PreparedStatement addProtection, removeProtection, updateGuestList, getProtectionAtPosition;
     // private PreparedStatement loadAllProtections;
 
     public DatabaseManager(String pluginName, File dataFolder) {
         super(pluginName, dataFolder);
     }
 
     @Override
     protected DatabaseConnection createConnection(String pluginName, File dataFolder) throws Exception {
         File configFile = new File(dataFolder, "sqlconfig.yml");
         if (!configFile.exists()) {
             DatabaseUtils.createDatabaseConfig(DatabaseType.SQLLite, configFile, pluginName);
             return null;
         } else {
             return new DatabaseConnection(pluginName, DatabaseType.SQLLite, new MinestarConfig(configFile));
         }
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
         this.addProtection = con.prepareStatement("INSERT INTO `tbl_protections` (owner, protectionType, blockWorld, blockX, blockY, blockZ, guestList) VALUES (?, ?, ?, ?, ?, ?, ?);");
         this.removeProtection = con.prepareStatement("DELETE FROM `tbl_protections` WHERE ID=?;");
         this.updateGuestList = con.prepareStatement("UPDATE `tbl_protections` SET guestList=? WHERE ID=?;");
         this.getProtectionAtPosition = con.prepareStatement("SELECT * FROM `tbl_protections` WHERE blockWorld=? AND blockX=? AND blockY=? AND blockZ=? LIMIT 1;");
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
             ConsoleUtils.printException(e, Core.NAME, "Can't get the protection at position: " + vector.toString() + "!");
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
             ConsoleUtils.printException(e, Core.NAME, "Can't create protection!");
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
             ConsoleUtils.printException(e, Core.NAME, "Can't save guestList in database! ID=" + protection.getID());
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
             ConsoleUtils.printException(e, Core.NAME, "Can't delete protection from database @ " + vector.toString());
             return false;
         }
     }
 
     public void init() {
 
     }
 
 }
