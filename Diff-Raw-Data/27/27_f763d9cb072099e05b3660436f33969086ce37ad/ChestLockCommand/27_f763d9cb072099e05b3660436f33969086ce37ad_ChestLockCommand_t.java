 package com.codisimus.plugins.chestlock;
 
 import com.google.common.collect.Sets;
 import java.io.FileInputStream;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Properties;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * Executes Player Commands
  * 
  * @author Codisimus
  */
 public class ChestLockCommand implements CommandExecutor {
     public static enum Action { HELP, LIST, COOWNER, NEVER }
     public static enum ListType { TOOLS, CHESTS, FURNACES, DISPENSERS, DOORS, OWNER, CLEAR }
     public static enum CoOwnerType { PLAYER, GROUP }
     public static enum Add_Remove { ADD, REMOVE }
     private static final HashSet TRANSPARENT = Sets.newHashSet((byte)0, (byte)27,
             (byte)28, (byte)37, (byte)38, (byte)39, (byte)40, (byte)50, (byte)63,
             (byte)65, (byte)66, (byte)68, (byte)69, (byte)70, (byte)72, (byte)75,
             (byte)76, (byte)78);
     public static String command;
     public static int cornerID;
     
     /**
      * Listens for ChestLock commands to execute them
      * 
      * @param sender The CommandSender who may not be a Player
      * @param command The command that was executed
      * @param alias The alias that the sender used
      * @param args The arguments for the command
      * @return true always
      */
     @Override
     public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
         //Cancel if the command is not from a Player
         if (!(sender instanceof Player))
             return true;
         
         Player player = (Player)sender;
         
         //Display the help page if the Player did not add any arguments
         if (args.length == 0) {
             lock(player);
             return true;
         }
         
         Action action;
         
         try {
             action = Action.valueOf(args[0].toUpperCase());
         }
         catch (Exception notEnum) {
             sendHelp(player);
             return true;
         }
         
         //Execute the correct command
         switch (action) {
             case LIST:
                 if (args.length == 2) {
                     ListType listType;
         
                     try {
                         listType = ListType.valueOf(args[1].toUpperCase());
                     }
                     catch (Exception notEnum) {
                         sendHelp(player);
                         return true;
                     }
                     
                     list(player, listType);
                 }
                 else
                     sendHelp(player);
                 
                 return true;
                 
             case COOWNER:
                 if (args.length == 4) {
                     CoOwnerType coOwnerType;
         
                     try {
                         coOwnerType = CoOwnerType.valueOf(args[1].toUpperCase());
                     }
                     catch (Exception notEnum) {
                         sendHelp(player);
                         return true;
                     }
                     
                     Add_Remove add_Remove;
         
                     try {
                         add_Remove = Add_Remove.valueOf(args[2].toUpperCase());
                     }
                     catch (Exception notEnum) {
                         sendHelp(player);
                         return true;
                     }
                     
                     coowner(player, coOwnerType, add_Remove, args[3]);
                 }
                 else
                     sendHelp(player);
                 
                 return true;
                 
             case NEVER:
                 try {
                     setLockable(player, !Boolean.parseBoolean(args[1]));
                 }
                 catch (Exception notBool) {
                     sendHelp(player);
                 }
                 
                 return true;
                 
             default: sendHelp(player); return true;
         }
     }
     
     /**
      * Display a list of the given type to the given Player
      * 
      * @param player The given Player
      * @param type The type to list
      */
     public static void list(Player player, ListType type) {
         //Cancel if the Player does not have permission to use the command
         if (!ChestLock.hasPermission(player, "list."+type)) {
             player.sendMessage(ChestLockMessages.getPermissionMsg());
             return;
         }
 
         //Determine which list to display
         switch (type) {
             case TOOLS: //List the Tools that the Player can use
                 try {
                     //Load the Config file to read Tool settings
                     Properties p = new Properties();
                     FileInputStream fis = new FileInputStream("plugins/ChestLock/config.properties");
                     p.load(fis);
                     
                     player.sendMessage("§2Own Tool:§b "+p.getProperty("OwnTool")+
                             " §2Lock Tool:§b "+p.getProperty("LockTool")+
                             " §2Disown Tool:§b "+p.getProperty("DisownTool"));
 
                     //Only display Admin Tools to Players who can use them
                     if (ChestLock.hasPermission(player, "admin")) {
                         player.sendMessage("§2Admin Lock Tool:§b "+p.getProperty("AdminLockTool")+
                             " §2Admin Info Tool:§b "+p.getProperty("AdminInfoTool"));
                         player.sendMessage("§2Admin Disown Tool:§b "+p.getProperty("AdminDisownTool")+
                             " §2Admin Global Key:§b "+p.getProperty("AdminGlobalKey"));
                     }
                     
                     fis.close();
                 }
                 catch (Exception ex) {
                     ex.printStackTrace();
                 }
                 
                 break;
                 
             case CHESTS: //List Chests that the Player Owns
                 LinkedList<Safe> ownedChests = ChestLock.getOwnedChests(player.getName());
                 
                 //Display amount of Chests owned
                 String chestMsg = "You own "+ownedChests.size()+" Chests";
                 
                 //Display the limit if there is one
                 int chestLimit = ChestLock.getOwnLimit(player, "chest");
                 if (chestLimit > -1)
                     chestMsg.concat("and you may own a total of "+chestLimit);
                 
                 player.sendMessage(chestMsg);
                 
                 chestMsg = "";
                 
                 //Display the location of each Chest
                 for (Safe safe: ownedChests)
                     chestMsg.concat(", @"+getLocation(safe.block));
                 
                 //Do not send the message if it is blank
                 if (!chestMsg.isEmpty())
                     player.sendMessage(chestMsg.substring(2));
                 
                 break;
                 
             case FURNACES: //List Furnaces that the Player Owns
                 LinkedList<Safe> ownedFurnaces = ChestLock.getOwnedFurnaces(player.getName());
                 
                 //Display amount of Furnaces owned
                 String furnaceMsg = "You own "+ownedFurnaces.size()+" Furnaces";
                 
                 //Display the limit if there is one
                 int furnaceLimit = ChestLock.getOwnLimit(player, "furnace");
                 if (furnaceLimit > -1)
                     furnaceMsg.concat("and you may own a total of "+furnaceLimit);
                 
                 player.sendMessage(furnaceMsg);
                 
                 furnaceMsg = "";
                 
                 //Display the location of each Furnace
                 for (Safe safe: ownedFurnaces)
                     furnaceMsg.concat(", @"+getLocation(safe.block));
                 
                 //Do not send the message if it is blank
                 if (!furnaceMsg.isEmpty())
                     player.sendMessage(furnaceMsg.substring(2));
                 
                 break;
                 
             case DISPENSERS: //List Dispensers that the Player Owns
                 LinkedList<Safe> ownedDispensers = ChestLock.getOwnedDispensers(player.getName());
                 
                 //Display amount of Dispensers owned
                 String dispenerMsg = "You own "+ownedDispensers.size()+" Dispensers";
                 
                 //Display the limit if there is one
                 int dispenerLimit = ChestLock.getOwnLimit(player, "dispener");
                 if (dispenerLimit > -1)
                     dispenerMsg.concat("and you may own a total of "+dispenerLimit);
                 
                 player.sendMessage(dispenerMsg);
                 
                 dispenerMsg = "";
                 
                 //Display the location of each Dispenser
                 for (Safe safe: ownedDispensers)
                     dispenerMsg.concat(", @"+getLocation(safe.block));
                 
                 //Do not send the message if it is blank
                 if (!dispenerMsg.isEmpty())
                     player.sendMessage(dispenerMsg.substring(2));
                 
                 break;
                 
             case DOORS: //List Doors That the Player owns and what their key is
                 LinkedList<LockedDoor> ownedDoors = ChestLock.getOwnedDoors(player.getName());
                 
                 //Display amount of Dispensers owned
                 String doorMsg = "You own "+ownedDoors.size()+" Doors";
                 
                 //Display the limit if there is one
                 int doorLimit = ChestLock.getOwnLimit(player, "door");
                 if (doorLimit > -1)
                     doorMsg.concat("and you may own a total of "+doorLimit);
                 
                 player.sendMessage(doorMsg);
                 
                 doorMsg = "";
                 
                 //Display the location of each Dispenser
                 for (LockedDoor door: ownedDoors)
                     player.sendMessage(", Key: "+Material.getMaterial(door.key).name()+" @"+getLocation(door.block));
                 
                 //Do not send the message if it is blank
                 if (!doorMsg.isEmpty())
                     player.sendMessage(doorMsg.substring(2));
                 
                 break;
                 
             case OWNER: //List Owner and CoOwners of the target Block
                 Block block = player.getTargetBlock(TRANSPARENT, 10);
                 
                 //Check if the Block is a LockedDoor
                 LockedDoor door = ChestLock.findDoor(block);
                 if (door != null) {
                     player.sendMessage("Owner: "+door.owner);
                     return;
                 }
                 
                 //Display the Help Page if the Block is not a Safe
                 Safe safe = ChestLock.findSafe(block);
                 if (safe == null) {
                     sendHelp(player);
                     return;
                 }
 
                 //Display the Owner of the safe
                 player.sendMessage("Owner: "+safe.owner);
 
                 //Display CoOwners of OwnedChunk to Player
                 String coOwners = "CoOwners:  ";
                 for (String coOwner: safe.coOwners)
                     coOwners.concat(coOwner.concat(", "));
                 player.sendMessage(coOwners.substring(0, coOwners.length() - 2));
 
                 //Display CoOwner Groups of OwnedChunk to Player
                 String groups = "CoOwner Groups:  ";
                 for (String group: safe.groups)
                     groups.concat(group.concat(", "));
                 player.sendMessage(groups.substring(0, groups.length() - 2));
                 
                 break;
                 
             case CLEAR: ChestLock.clear(player.getName()); player.sendMessage(ChestLockMessages.getClearMsg()); break;
                 
             default: sendHelp(player); break;
         }
     }
     
     /**
      * Manages CoOwnership of the target Block if the Player is the Owner
      * 
      * @param player The given Player who may be the Owner
      * @param type The given type: 'player' or 'group'
      * @param action The given action: 'add' or 'remove'
      * @param coOwner The given CoOwner
      */
     public static void coowner(Player player, CoOwnerType type, Add_Remove action, String coOwner) {
         //Cancel if the Player does not have permission to use the command
         if (!ChestLock.hasPermission(player, "coowner")) {
             player.sendMessage(ChestLockMessages.getPermissionMsg());
             return;
         }
         
         //Display Help Page if the Block is not a Safe
         Block block = player.getTargetBlock(TRANSPARENT, 10);
         Safe safe = ChestLock.findSafe(block);
         if (safe == null) {
             sendHelp(player);
             return;
         }
         
         //Return if the Player is not the Owner
         if (!safe.owner.equals(player.getName())) {
             player.sendMessage(ChestLockMessages.getDoNotOwnMsg(block.getType()));
             return;
         }
 
         //Determine the command to execute
         switch (type) {
             case PLAYER:
                 switch (action) {
                     case ADD:
                         //Cancel if the Player is already a CoOwner
                         if (safe.coOwners.contains(coOwner))
                             player.sendMessage(coOwner+" is already a CoOwner");
                         else {
                             safe.coOwners.add(coOwner);
                             player.sendMessage(coOwner+" added as a CoOwner");
                             ChestLock.save(safe.block.getWorld());
                         }
                         
                         break;
                     
                     case REMOVE:
                         //Cancel if the Player is not a CoOwner
                         if (safe.coOwners.contains(coOwner)) {
                             safe.coOwners.add(coOwner);
                             player.sendMessage(coOwner+" is no longer as a CoOwner");
                             ChestLock.save(safe.block.getWorld());
                         }
                         else
                             player.sendMessage(coOwner+" is not as a CoOwner");
                         
                         break;
                         
                     default: sendHelp(player); break;
                 }
                 
             case GROUP:
                 switch (action) {
                     case ADD:
                         //Cancel if the Group is already a CoOwner
                         if (safe.groups.contains(coOwner))
                             player.sendMessage(coOwner+" is already a CoOwner");
                         else {
                             safe.groups.add(coOwner);
                             player.sendMessage(coOwner+" added as a CoOwner");
                             ChestLock.save(safe.block.getWorld());
                         }
                         
                         break;
                     
                     case REMOVE:
                         //Cancel if the Group is not a CoOwner
                         if (safe.groups.contains(coOwner)) {
                             safe.groups.add(coOwner);
                             player.sendMessage(coOwner+" is no longer as a CoOwner");
                             ChestLock.save(safe.block.getWorld());
                         }
                         else
                             player.sendMessage(coOwner+" is not as a CoOwner");
                         
                         break;
                         
                     default: sendHelp(player); break;
                 }
                 
             default: sendHelp(player); break;
         }
     }
     
     /**
      * Sets whether the target Safe is lockable
      * 
      * @param player The Player targeting the Safe
      * @param bool True if the Safe will be set to lockable
      */
     public static void setLockable(Player player, boolean bool) {
         //Display Help Page if the target Block is not a Safe
         Block block = player.getTargetBlock(TRANSPARENT, 10);
         Safe safe = ChestLock.findSafe(block);
         if (safe == null) {
             sendHelp(player);
             return;
         }
         
         //Cancel if the Player is not the Owner
         if (!safe.owner.equals(player.getName())) {
             player.sendMessage(ChestLockMessages.getDoNotOwnMsg(block.getType()));
             return;
         }
         
         String type = block.getType().toString().toLowerCase();
         
         //Toggle whether the Safe is lockable
         if (safe.lockable)
             if (bool)
                 player.sendMessage(type.concat(" is already lockable."));
             else {
                 safe.lockable = false;
                 player.sendMessage(type.concat(" is now unlockable."));
             }
         else
             if (bool) {
                 safe.lockable = true;
                 player.sendMessage(type.concat(" is now lockable."));
             }
             else
                 player.sendMessage(type.concat(" is already unlockable"));
         
         ChestLock.save(safe.block.getWorld());
     }
     
     /**
      * Toggles the locked status of the target Safe
      * 
      * @param player The Player targeting the Safe
      */
     public static void lock(Player player) {
         //Cancel if the Player does not have permission to use the command
         if (!ChestLock.hasPermission(player, "lock")) {
             player.sendMessage(ChestLockMessages.getPermissionMsg());
             return;
         }
         
         //Display Help Page if the Block is not a Door
         Block block = player.getTargetBlock(TRANSPARENT, 10);
         switch (block.getType()) {
             case WOOD_DOOR: break;
             case WOODEN_DOOR: break;
             case IRON_DOOR: break;
             case IRON_DOOR_BLOCK: break;
             case TRAP_DOOR: break;
             case FENCE_GATE: break;
             default: sendHelp(player); return;
         }
         
         ItemStack item = player.getItemInHand();
         int id = item.getTypeId();
         LockedDoor door = ChestLock.findDoor(block);
         
         //If the Door is not owned, give ownership to the Player
         if (door == null) {
             //Cancel if the Player has reached their own limit for Doors
             int limit = ChestLock.getOwnLimit(player, "door");
             if (limit > -1 && ChestLock.getOwnedDoors(player.getName()).size() >= limit) {
                 player.sendMessage(ChestLockMessages.getLimitMsg(block.getType()));
                 return;
             }  
             
             ChestLock.doors.add(new LockedDoor(player.getName(), block, id));
         }
        else if (!door.owner.equals(player.getName()) && !ChestLock.hasPermission(player, "admin")) {
             player.sendMessage(ChestLockMessages.getDoNotOwnMsg(block.getType()));
             return;
         }
        else
            door.key = id;
 
         if (id == 0)
             player.sendMessage(ChestLockMessages.getUnlockableMsg(block.getType()));
         else
             player.sendMessage(ChestLockMessages.getKeySetMsg(item.getType()));
         
         ChestLock.save(block.getWorld());
     }
     
     /**
      * Displays the ChestLock Help Page to the given Player
      *
      * @param player The Player needing help
      */
     public static void sendHelp(Player player) {
         player.sendMessage("§e     ChestLock Help Page:");
         player.sendMessage("§2/"+command+"§b Set item in hand as key to target door");
         player.sendMessage("§2/"+command+" never ['true' or 'false']§b Set if target can be locked");
         player.sendMessage("§2/"+command+" (while holding nothing)§b Make target door unlockable");
         player.sendMessage("§2/"+command+" coowner group add [Name]§b Add Group as CoOwner");
         player.sendMessage("§2/"+command+" coowner group remove [Name]§b Remove Group as CoOwner");
         player.sendMessage("§2/"+command+" coowner player add [Name]§b Add Player as CoOwner");
         player.sendMessage("§2/"+command+" coowner player remove [Name]§b Remove Player");
         player.sendMessage("§2/"+command+" list tools§b List Tools for doing various things");
         player.sendMessage("§2/"+command+" list [BlockType]§b List all Blocks you own of given type");
         player.sendMessage("§2/"+command+" list owner§b List the owner/CoOwners of target");
         player.sendMessage("§2/"+command+" list clear§b Disown all chests and doors");
     }
 
     /**
      * Returns the location of a given block in the form of a string
      * 
      * @param The Block whose location will be returned
      * @return The location of a given block
      */
     public static String getLocation(Block block) {
         return "World: "+block.getWorld().getName()+" X: "+block.getX()
                 +" Y: "+block.getY()+" Z: "+block.getZ();
     }
 }
