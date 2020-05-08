 package de.minestar.moneypit.modules;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 import de.minestar.minestarlibrary.utils.PlayerUtils;
 import de.minestar.moneypit.Core;
 import de.minestar.moneypit.data.BlockVector;
 import de.minestar.moneypit.data.protection.Protection;
 import de.minestar.moneypit.data.protection.ProtectionType;
 import de.minestar.moneypit.data.subprotection.SubProtection;
 import de.minestar.moneypit.manager.ModuleManager;
 import de.minestar.moneypit.utils.DoorHelper;
 
 public class Module_WoodenDoor extends Module {
 
     private final String NAME = "wooddoor";
 
     public Module_WoodenDoor(YamlConfiguration ymlFile) {
         this.writeDefaultConfig(NAME, ymlFile);
     }
 
     public Module_WoodenDoor(ModuleManager moduleManager, YamlConfiguration ymlFile) {
         super();
         this.init(moduleManager, ymlFile, Material.WOODEN_DOOR.getId(), NAME);
         this.setDoNeighbourCheck(true);
         this.setHandleRedstone(ymlFile.getBoolean("protect." + NAME + ".handleRedstone", true));
     }
 
     @Override
     protected void writeExtraConfig(String moduleName, YamlConfiguration ymlFile) {
         ymlFile.set("protect." + NAME + ".handleRedstone", true);
     }
 
     @Override
     public void addProtection(int ID, BlockVector vector, String owner, ProtectionType type, byte subData) {
         // create the protection
         Protection protection = new Protection(ID, vector, owner, type);
 
         // protect the block below
         SubProtection subProtection = new SubProtection(vector.getRelative(0, -1, 0), protection);
         protection.addSubProtection(subProtection);
 
         // protect the block above
         subProtection = new SubProtection(vector.getRelative(0, 1, 0), protection);
         protection.addSubProtection(subProtection);
 
         // FETCH SAND & GRAVEL
         BlockVector tempVector = vector.getRelative(0, -1, 0);
         if (tempVector.getLocation().getBlock().getTypeId() == Material.SAND.getId() || tempVector.getLocation().getBlock().getTypeId() == Material.GRAVEL.getId()) {
             int distance = 1;
            tempVector = tempVector.getRelative(0, -1, 0);
             // search all needed blocks
             while (tempVector.getLocation().getBlock().getTypeId() == Material.SAND.getId() || tempVector.getLocation().getBlock().getTypeId() == Material.GRAVEL.getId()) {
                 ++distance;
                 tempVector = tempVector.getRelative(0, -1, 0);
             }
 
             // finally protect the blocks
             tempVector = vector.getRelative(0, -1, 0);
             for (int i = 0; i < distance; i++) {
                 // protect the blocks
                 subProtection = new SubProtection(tempVector.getRelative(0, -1 - i, 0), protection);
                 protection.addSubProtection(subProtection);
             }
         }
 
         // register the protection
         getProtectionManager().addProtection(protection);
     }
 
     @Override
     public boolean onPlace(BlockPlaceEvent event, BlockVector vector) {
         // search a second chest
         BlockVector doubleDoor = DoorHelper.getSecondWoodDoor(vector);
         if (doubleDoor == null) {
             return false;
         }
 
         // check if there is a protection
         Protection protection = Core.protectionManager.getProtection(doubleDoor);
         if (protection == null) {
             return false;
         }
 
         // check permissions
         if (!protection.canEdit(event.getPlayer())) {
             PlayerUtils.sendError(event.getPlayer(), Core.NAME, "You cannot place a door here.");
             PlayerUtils.sendInfo(event.getPlayer(), "The neighbour is a protected door.");
             event.setCancelled(true);
             return true;
         }
 
         // send info
         PlayerUtils.sendInfo(event.getPlayer(), Core.NAME, "Subprotection created.");
 
         // return true to abort the event
         return true;
     }
 }
