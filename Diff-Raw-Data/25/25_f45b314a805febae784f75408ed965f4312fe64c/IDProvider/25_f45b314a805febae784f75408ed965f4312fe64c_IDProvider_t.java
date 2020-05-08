 /*
  * Jaffas and more!
  * author: monnef
  */
 
 package monnef.core.utils;
 
 import cpw.mods.fml.common.registry.EntityRegistry;
 import net.minecraft.block.Block;
 import net.minecraftforge.common.ConfigCategory;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.Property;
 
 import java.util.HashSet;
 
 import static monnef.core.MonnefCorePlugin.Log;
 
 public class IDProvider {
     private static final String BOOT_CATEGORY_STRING = "boot";
 
     public int startBlockID;
     private int actualBlockID;
 
     public int startItemID;
     private int actualItemID;
 
     public final int startModEntityID = 0;
     private int actualModEntityID;
 
     private String modName;
     private Configuration config;
 
     private HashSet<Integer> BlockIDsAssigned;
     private HashSet<Integer> ItemIDsAssigned;
 
     public IDProvider(int startBlockID, int startItemID, String modName) {
         this.startBlockID = startBlockID;
         this.modName = modName;
 
         this.startItemID = startItemID;

        initActualIDs();
 
         this.BlockIDsAssigned = new HashSet<Integer>();
         this.ItemIDsAssigned = new HashSet<Integer>();
     }
 
    private void initActualIDs() {
        this.actualBlockID = this.startBlockID;
        this.actualItemID = this.startItemID;
    }

     public int getItemID() {
         int newId = this.actualItemID;
         while (ItemIDsAssigned.contains(newId)) {
             newId++;
         }
         return this.actualItemID = newId;
     }
 
     public int getBlockID() {
         int newId = this.actualBlockID;
         while (BlockIDsAssigned.contains(newId)) {
             newId++;
         }
         return this.actualBlockID = newId;
     }
 
     public int getModEntityID() {
         return this.actualModEntityID++;
     }
 
     public int getBlockIDFromConfig(String name) {
         int newUsedId = this.config.getBlock(name, this.getBlockID()).getInt();
         BlockIDsAssigned.add(newUsedId);
         return newUsedId;
     }
 
     public int getItemIDFromConfig(String name) {
         int newUsedId = this.config.getItem(name, this.getItemID()).getInt();
         ItemIDsAssigned.add(newUsedId);
         return newUsedId;
     }
 
     public int getEntityIDFromConfig(String name) {
         return this.config.get("entity", name, EntityRegistry.findGlobalUniqueEntityId()).getInt();
     }
 
     public int getModEntityIDFromConfig(String name) {
         return this.config.get("mod_entity", name, getModEntityID()).getInt();
     }
 
     public void linkWithConfig(Configuration config) {
         if (this.config != null) {
             throw new RuntimeException("multiple linking with config!");
         }
 
         this.config = config;

        initStartsOfIntervals();
        initActualIDs();

         loadDataFromConfig(config.getCategory(Configuration.CATEGORY_BLOCK), BlockIDsAssigned);
         loadDataFromConfig(config.getCategory(Configuration.CATEGORY_ITEM), ItemIDsAssigned);
     }
 
     // doesn't mark, it's expected to release the id
     public int getTempBlockId() {
         int id = 256;
         while (Block.blocksList[id] != null) {
             id++;
             if (id >= Block.blocksList.length) {
                 throw new RuntimeException("All block IDs exhausted. Are you crazy?");
             }
         }
         return id;
     }
 
     public void safelyRemoveTempBlock(int tempBlockId, Block tempBlock) {
         if (!(tempBlock.getClass().isInstance(Block.blocksList[tempBlockId]))) {
             throw new RuntimeException(String.format("What trickery is going on here? Block registered with ID %d but now it's not there (new ID is %d), halting. This behaviour will break other things.", tempBlockId, tempBlock.blockID));
         }
         Block.blocksList[tempBlockId] = null; // don't consume block id
     }
 
     protected void initStartsOfIntervals() {
         int newStartBlockID = config.get(BOOT_CATEGORY_STRING, "blockStartId", startBlockID).getInt();
         int newStartItemID = config.get(BOOT_CATEGORY_STRING, "itemStartId", startItemID).getInt();
         Log.printFine(String.format("Starts of ID intervals for \"%s\" are item = %d (default: %d) and block = %d (default: %d)", modName, newStartItemID, startItemID, newStartBlockID, startBlockID));
         startBlockID = newStartBlockID;
         startItemID = newStartItemID;
     }
 
     protected void loadDataFromConfig(ConfigCategory category, HashSet<Integer> used) {
         for (Property entry : category.getValues().values()) {
             int id = entry.getInt();
             if (id != -1) {
                 used.add(id);
             }
         }
     }
 }
