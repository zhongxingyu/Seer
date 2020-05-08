 package de.minestar.therock.commands;
 
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Dispenser;
 import org.bukkit.block.Furnace;
 import org.bukkit.entity.Player;
 
 import de.minestar.minestarlibrary.commands.AbstractCommand;
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 import de.minestar.therock.Core;
 import de.minestar.therock.data.BlockVector;
 import de.minestar.therock.data.CacheElement;
 
 public class RollbackCommand extends AbstractCommand {
 
     private static final Set<Integer> queue_single = new HashSet<Integer>(Arrays.asList(6, 8, 9, 10, 11, 26, 27, 28, 30, 31, 32, 34, 37, 38, 39, 40, 50, 51, 55, 59, 63, 64, 66, 68, 69, 70, 71, 72, 75, 76, 77, 78, 83, 90, 93, 94, 96, 104, 105, 106, 115, 117, 118, 119, 127, 131, 132));
     private static final Set<Integer> queue_double = new HashSet<Integer>(Arrays.asList(111));
 
     private int blockCount = 0;
 
     public RollbackCommand(String syntax, String arguments, String node) {
         super(Core.NAME, syntax, arguments, node);
         this.description = "Rollback the selected action.";
     }
 
     @SuppressWarnings("unchecked")
     public void execute(String[] args, Player player) {
         // Validate cache
         CacheElement cache = Core.cacheHolder.getCacheElement(player.getName());
         if (cache == null) {
             PlayerUtils.sendError(player, Core.NAME, "You must specify the rollback first.");
             PlayerUtils.sendInfo(player, "NOTE: Use /tr selection [Player] [SINCE]");
            PlayerUtils.sendInfo(player, "OR: Use /tr area <Radius> [<Player> [Time] | time <SINCE>]");
             return;
         }
 
         try {
             ResultSet results = cache.getResults();
             BlockVector newVector;
 
             // create blocklist to sort from 0 to 256
             int MAX = 256;
             ArrayList<BlockVector>[] blockLists = new ArrayList[MAX];
             for (int i = 0; i < MAX; i++) {
                 blockLists[i] = new ArrayList<BlockVector>();
             }
 
             // sort the blocks into the blocklist
             while (results.next()) {
                 newVector = new BlockVector(cache.getWorld().getName(), results.getInt("blockX"), results.getInt("blockY"), results.getInt("blockZ"));
                 newVector.setTypeID(results.getInt("fromID"));
                 newVector.setSubData((byte) results.getInt("fromData"));
                 blockLists[newVector.getY()].add(newVector);
             }
 
             // setup queues
             ArrayList<BlockVector> run_one = new ArrayList<BlockVector>(1024);
             ArrayList<BlockVector> run_two = new ArrayList<BlockVector>(512);
             ArrayList<BlockVector> run_three = new ArrayList<BlockVector>(256);
             for (ArrayList<BlockVector> curList : blockLists) {
                 for (BlockVector vector : curList) {
                     if (queue_single.contains(vector.getTypeID())) {
                         run_two.add(vector);
                     } else if (queue_double.contains(vector.getTypeID())) {
                         run_three.add(vector);
                     } else {
                         run_one.add(vector);
                     }
                 }
             }
 
             // clear the list
             for (int i = 0; i < MAX; i++) {
                 blockLists[i].clear();
             }
 
             // rollback blocks : Run ONE
             this.executeRun(run_one);
 
             // rollback blocks : Run TWO
             this.executeRun(run_two);
 
             // rollback blocks : Run THREE
             this.executeRun(run_three);
 
             // send info
             PlayerUtils.sendSuccess(player, Core.NAME, "Rollback finished. ( " + blockCount + " Blocks)");
             blockCount = 0;
             Core.cacheHolder.clearCacheElement(player.getName());
         } catch (Exception e) {
             PlayerUtils.sendError(player, Core.NAME, "Oooops.. something went wrong!");
             Core.cacheHolder.clearCacheElement(player.getName());
             e.printStackTrace();
         }
     }
 
     private void executeRun(ArrayList<BlockVector> list) {
         Block block;
         for (BlockVector vector : list) {
             block = vector.getLocation().getBlock();
             if (block.getTypeId() == Material.CHEST.getId()) {
                 ((Chest) block.getState()).getBlockInventory().clear();
             } else if (block.getTypeId() == Material.DISPENSER.getId()) {
                 ((Dispenser) block.getState()).getInventory().clear();
             } else if (block.getTypeId() == Material.FURNACE.getId() || block.getTypeId() == Material.BURNING_FURNACE.getId()) {
                 ((Furnace) block.getState()).getInventory().clear();
             }
             block.setTypeIdAndData(vector.getTypeID(), vector.getSubData(), true);
             ++blockCount;
         }
         list.clear();
     }
 }
