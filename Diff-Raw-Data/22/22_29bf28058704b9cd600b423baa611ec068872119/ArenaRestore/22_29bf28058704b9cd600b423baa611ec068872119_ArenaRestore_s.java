 package me.dalton.capturethepoints;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Chest;
 import org.bukkit.block.ContainerBlock;
 import org.bukkit.block.CreatureSpawner;
 import org.bukkit.block.Dispenser;
 import org.bukkit.block.Furnace;
 import org.bukkit.block.NoteBlock;
 import org.bukkit.block.Sign;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.Directional;
 
 /**
  *
  * @author Humsas
  */
 public class ArenaRestore {
 
     private List<CTPBlock> destroyedBlock = new LinkedList<CTPBlock>();
     // true if destroed, false if not
     private List<Boolean> blockStatus = new LinkedList<Boolean>();
     private CaptureThePoints ctp;
 
     public ArenaRestore(CaptureThePoints plugi) {
         ctp = plugi;
     }
 
     public void addBlock(Block block, boolean isDestroyed) {
         CTPBlock tmp = new CTPBlock();
         ContainerBlock dd;
         BlockState state = block.getState();
         //chest
         if (state instanceof ContainerBlock) {
             dd = (ContainerBlock) state;
             ItemStack[] contents = dd.getInventory().getContents();
             tmp.inv = contents;
         }
 
         tmp.data = block.getData();
         tmp.loc = block.getLocation();
         tmp.material = block.getTypeId();
 
         destroyedBlock.add(tmp);
         blockStatus.add(isDestroyed);
     }
 
     public void restoreAllBlocks() {
         for (int i = destroyedBlock.size() - 1; i >= 0; i--) {
             Location blockLocation = destroyedBlock.get(i).loc;
             if (blockStatus.get(i)) {
                 CTPBlock tmp = destroyedBlock.get(i);
                 blockLocation.getBlock().setTypeId(tmp.material);
                 blockLocation.getBlock().setData(tmp.data);
 
                 //chest
                 if (tmp.inv != null && tmp.inv.length > 0)
                 {
                     ContainerBlock dd = (ContainerBlock) blockLocation.getBlock().getState();
                     Inventory inv = dd.getInventory();
                     inv.setContents(tmp.inv);
                 }
             }
             else
             {
                 blockLocation.getBlock().setTypeId(0);
             }
         }
         destroyedBlock.clear();
         blockStatus.clear();
     }
 
 
     public void checkForArena(String arenaName, String world)
     {
        ResultSet lala = ctp.mysqlConnector.getData("SELECT * FROM Arena where arena_name = '"+ arenaName +"'");
         try
         {
             if (lala.next())   // We found an egzisting arena
             {
                 // delete data from mysql
                 deleteArenaData(arenaName);
             }
             else
             {
                 ctp.mysqlConnector.modifyData("INSERT INTO `Arena` (`name`, `world`, `x1`, `y1`, `z1`, `x2`, `y2`, `z2`) VALUES ( '" + arenaName + "','" + world +
                         "'," + ctp.mainArena.x1 + "," + ctp.mainArena.y1 + "," + ctp.mainArena.z1 + "," + ctp.mainArena.x2 + "," + ctp.mainArena.y2 + "," + ctp.mainArena.z2 + ");");
             }
         }
         catch (SQLException ex)
         {
             Logger.getLogger(ArenaRestore.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void deleteArenaData(String arenaName)
     {
         try 
         {
             ResultSet rs = ctp.mysqlConnector.getData("SELECT id FROM Simple_block where arena_name = '" + arenaName + "'");
             while (rs.next())
             {
                 ctp.mysqlConnector.modifyData("DELETE FROM Sign WHERE block_ID = " + rs.getInt("id"));
                 ctp.mysqlConnector.modifyData("DELETE FROM Item WHERE block_ID = " + rs.getInt("id"));
                 ctp.mysqlConnector.modifyData("DELETE FROM Spawner_block WHERE block_ID = " + rs.getInt("id"));
                 ctp.mysqlConnector.modifyData("DELETE FROM Note_block WHERE block_ID = " + rs.getInt("id"));
             }
             ctp.mysqlConnector.modifyData("DELETE FROM Simple_block where arena_name = '" + arenaName + "'");
         }
         catch (SQLException ex)
         {
             Logger.getLogger(ArenaRestore.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void storeBlock(Block block, String arenaName)
     {
         int type = block.getTypeId();
         int data = block.getData();
         Location loc = block.getLocation();
 
         switch (type)
         {
             case BlockID.WALL_SIGN:
             case BlockID.SIGN_POST:
             {
                 Sign sign = (Sign) block.getState();
                 String dir = ((Directional) block.getType().getNewData(block.getData())).getFacing().toString();
                 ctp.mysqlConnector.modifyData("INSERT INTO `Simple_block` (`data`, `x`, `y`, `z`, `arena_name`, `block_type`, `direction`) VALUES ( " + data + "," +loc.getBlockX() + "," +
                         loc.getBlockY() + "," + loc.getBlockZ() + ",'" + arenaName + "'," + type + ",'" + dir + "');");
                 int id = ctp.mysqlConnector.getLastInsertedId();
                 ctp.mysqlConnector.modifyData("INSERT INTO `Sign` (`block_ID`, `first_line`, `second_line`, `third_line`, `fourth_line`) VALUES ( " + id + ",'" + sign.getLine(0) + "','" +
                         sign.getLine(1) + "','" + sign.getLine(2) + "','" + sign.getLine(3) + "');");
                 break;
             }
 
             case BlockID.CHEST:
             {
                 Chest chest = (Chest) block.getState();
                 ctp.mysqlConnector.modifyData("INSERT INTO `Simple_block` (`data`, `x`, `y`, `z`, `arena_name`, `block_type`, `direction`) VALUES ( " + data + "," +loc.getBlockX() + "," +
                         loc.getBlockY() + "," + loc.getBlockZ() + ",'" + arenaName + "'," + type + ",'NO');");
                 int id = ctp.mysqlConnector.getLastInsertedId();
 
                 storeInventory(id, chest.getInventory());
                 break;
             }
 
             case BlockID.FURNACE:
             case BlockID.BURNING_FURNACE:
             {
                 Furnace furnace = (Furnace) block.getState();
                 String dir = ((Directional) block.getType().getNewData(block.getData())).getFacing().toString();
                 ctp.mysqlConnector.modifyData("INSERT INTO `Simple_block` (`data`, `x`, `y`, `z`, `arena_name`, `block_type`, `direction`) VALUES ( " + data + "," +loc.getBlockX() + "," +
                         loc.getBlockY() + "," + loc.getBlockZ() + ",'" + arenaName + "'," + type + ",'" + dir + "');");
                 int id = ctp.mysqlConnector.getLastInsertedId();
 
                 storeInventory(id, furnace.getInventory());
                 break;
             }
 
             case BlockID.DISPENSER:
             {
                 Dispenser dispenser = (Dispenser) block.getState();
                 String dir = ((Directional) block.getType().getNewData(block.getData())).getFacing().toString();
                 ctp.mysqlConnector.modifyData("INSERT INTO `Simple_block` (`data`, `x`, `y`, `z`, `arena_name`, `block_type`, `direction`) VALUES ( " + data + "," +loc.getBlockX() + "," +
                         loc.getBlockY() + "," + loc.getBlockZ() + ",'" + arenaName + "'," + type + ",'" + dir + "');");
                 int id = ctp.mysqlConnector.getLastInsertedId();
 
                 storeInventory(id, dispenser.getInventory());
                 break;
             }
 
             case BlockID.MOB_SPAWNER:
             {
                 CreatureSpawner mobSpawner = (CreatureSpawner) block.getState();
                 ctp.mysqlConnector.modifyData("INSERT INTO `Simple_block` (`data`, `x`, `y`, `z`, `arena_name`, `block_type`, `direction`) VALUES ( " + data + "," +loc.getBlockX() + "," +
                         loc.getBlockY() + "," + loc.getBlockZ() + ",'" + arenaName + "'," + type + ",'NO');");
                 int id = ctp.mysqlConnector.getLastInsertedId();
                 ctp.mysqlConnector.modifyData("INSERT INTO `Spawner_block` (`block_ID`, `creature_type`, `delay`) VALUES ( " + id + ",'" + mobSpawner.getCreatureTypeId() + "'," + mobSpawner.getDelay() + ");");
                 break;
             }
 
             case BlockID.NOTE_BLOCK:
             {
                 NoteBlock noteBlock = (NoteBlock) block.getState();
                 ctp.mysqlConnector.modifyData("INSERT INTO `Simple_block` (`data`, `x`, `y`, `z`, `arena_name`, `block_type`, `direction`) VALUES ( " + data + "," +loc.getBlockX() + "," +
                         loc.getBlockY() + "," + loc.getBlockZ() + ",'" + arenaName + "'," + type + ",'NO');");
                 int id = ctp.mysqlConnector.getLastInsertedId();
 
                 ctp.mysqlConnector.modifyData("INSERT INTO `Note_block` (`block_ID`, `note_type`) VALUES ( " + id + "," + noteBlock.getRawNote() + ");");
                 break;
             }
 
             default:
             {
                 // Get if this is a directional block
                 String dir = "NO";
                 if(block.getType().getNewData(block.getData()) instanceof Directional)
                 {
                     dir = ((Directional) block.getType().getNewData(block.getData())).getFacing().toString();
                 }
                 ctp.mysqlConnector.modifyData("INSERT INTO `Simple_block` (`data`, `x`, `y`, `z`, `arena_name`, `block_type`, `direction`) VALUES ( " + data + "," +loc.getBlockX() + "," +
                         loc.getBlockY() + "," + loc.getBlockZ() + ",'" + arenaName + "'," + type + ",'" + dir + "');");
                 break;
             }
         }
     }
 
     private void storeInventory(int id, Inventory inv)
     {
         for(int i = 0; i < inv.getSize(); i++)
         {
             ItemStack item = inv.getItem(i);
             if(item != null)
                 ctp.mysqlConnector.modifyData("INSERT INTO `Item` (`type`, `block_ID`, `durability`, `amount`, `place_in_inv`, `data`) VALUES ( " + item.getTypeId() + "," + id + "," +
                         item.getDurability() + "," + item.getAmount() + "," + i + "," + item.getData().getData() + ");");
         }
     }
 
 
     public void restore(String arenaName)
     {
         try
         {
            ResultSet rs1 = ctp.mysqlConnector.getData("SELECT * FROM Arena where arena_name = '"+ arenaName +"'");
             if(rs1.next())
             {
                 ResultSet lala = ctp.mysqlConnector.getData("SELECT * FROM Simple_block where arena_name = '" + arenaName + "'");
 
                 while(lala.next())
                 {
                     int id = lala.getInt("id");
                     int data = lala.getInt("data");
                     Location loc = new Location(ctp.getServer().getWorld(rs1.getString("world")), lala.getInt("x"), lala.getInt("y"), lala.getInt("z"));
                     int type = lala.getInt("block_type");
 
                     String dir = lala.getString("direction");
                     Block block = loc.getBlock();
 
                     // Set main block info
                     block.setTypeId(type);
                     block.setData((byte)data);
                     if(!dir.equalsIgnoreCase("NO"))
                     {
                         ((Directional) block.getType().getNewData(block.getData())).setFacingDirection(BlockFace.valueOf(dir));
                     }
 
                     // restore block
                     switch (type)
                     {
                         case BlockID.WALL_SIGN:
                         case BlockID.SIGN_POST:
                         {
                             ResultSet rs = ctp.mysqlConnector.getData("SELECT * FROM Sign where Sign.block_ID = " + id);
                             Sign sign = (Sign) block.getState();
 
                             while (rs.next())
                             {
                                 sign.setLine(0, rs.getString("first_line"));
                                 sign.setLine(1, rs.getString("second_line"));
                                 sign.setLine(2, rs.getString("third_line"));
                                 sign.setLine(3, rs.getString("fourth_line"));
                             }
                             break;
                         }
 
                         case BlockID.CHEST:
                         {
                             Chest chest = (Chest) block.getState();
                             Inventory inv = chest.getInventory();
 
                             ResultSet rs = ctp.mysqlConnector.getData("SELECT * FROM Item where Item.block_ID = " + id);
                             while (rs.next())
                             {
                                 ItemStack item = new ItemStack(rs.getInt("type"), rs.getInt("amount"), (short)rs.getInt("durability"), (byte)rs.getInt("data"));
                                 inv.setItem(rs.getInt("place_in_inv"), item);
                             }
                             break;
                         }
 
                         case BlockID.FURNACE:
                         case BlockID.BURNING_FURNACE:
                         {
                             Furnace furnace = (Furnace) block.getState();
                             Inventory inv = furnace.getInventory();
 
                             ResultSet rs = ctp.mysqlConnector.getData("SELECT * FROM Item where Item.block_ID = " + id);
                             while (rs.next())
                             {
                                 ItemStack item = new ItemStack(rs.getInt("type"), rs.getInt("amount"), (short)rs.getInt("durability"), (byte)rs.getInt("data"));
                                 inv.setItem(rs.getInt("place_in_inv"), item);
                             }
                             break;
                         }
 
                         case BlockID.DISPENSER:
                         {
                             Dispenser dispenser = (Dispenser) block.getState();
                             Inventory inv = dispenser.getInventory();
 
                             ResultSet rs = ctp.mysqlConnector.getData("SELECT * FROM Item where Item.block_ID = " + id);
                             while (rs.next())
                             {
                                 ItemStack item = new ItemStack(rs.getInt("type"), rs.getInt("amount"), (short)rs.getInt("durability"), (byte)rs.getInt("data"));
                                 inv.setItem(rs.getInt("place_in_inv"), item);
                             }
                             break;
                         }
 
                         case BlockID.MOB_SPAWNER:
                         {
                             CreatureSpawner mobSpawner = (CreatureSpawner) block.getState();
                             ResultSet rs = ctp.mysqlConnector.getData("SELECT * FROM Spawner_block where Spawner_block.block_ID = " + id);
                             while (rs.next())
                             {
                                 mobSpawner.setCreatureTypeId(rs.getString("creature_type"));
                                 mobSpawner.setDelay(rs.getInt("delay"));
                             }
                             break;
                         }
 
                         case BlockID.NOTE_BLOCK:
                         {
                             NoteBlock noteBlock = (NoteBlock) block.getState();
                             ResultSet rs = ctp.mysqlConnector.getData("SELECT * FROM Note_block where Note_block.block_ID = " + id);
                             while (rs.next())
                             {
                                 noteBlock.setRawNote((byte)rs.getInt("note_type"));
                             }
                             break;
                         }
                     }
                 }

             }
             else
             {
                System.out.println("ERROR [CTP] Arena data in MySql did not found. Please reselect arena boundaries!");
             }
         }
         catch (SQLException ex)
         {
             Logger.getLogger(ArenaRestore.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
 }
 
 class CTPBlock
 {
     byte data;
     Location loc;
     int material;
     ItemStack[] inv;
 }
