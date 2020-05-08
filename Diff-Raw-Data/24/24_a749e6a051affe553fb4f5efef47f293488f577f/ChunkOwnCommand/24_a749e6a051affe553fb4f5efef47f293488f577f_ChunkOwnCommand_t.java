 package com.codisimus.plugins.chunkown;
 
 import java.util.HashMap;
import java.util.Iterator;
 import java.util.Map;
 import org.bukkit.Chunk;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  * Executes Player Commands
  * 
  * @author Codisimus
  */
 public class ChunkOwnCommand implements CommandExecutor {
     public static String command;
     private static enum Action { BUY, SELL, LIST, INFO, COOWNER, CLEAR, PREVIEW }
     public static long cooldown;
     public static int cornerID;
     private Map<String, Long> playerLastPreview;
     
     public ChunkOwnCommand() {
         playerLastPreview = new HashMap<String, Long>();
     }
     
     /**
      * Listens for ChunkOwn commands to execute them
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
         
         //Display help page if the Player did not add any arguments
         if (args.length == 0) {
             sendHelp(player);
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
             case BUY: buy(player); return true;
                 
             case SELL: sell(player); return true;
                 
             case LIST: list(player); return true;
                 
             case INFO: info(player); return true;
                 
             case COOWNER:
                 if (args.length == 4)
                     coowner(player, args[2], args[1], args[3]);
                 else
                     sendHelp(player);
                 return true;
                 
             case CLEAR: clear(player); return true;
                 
             case PREVIEW: preview(player); return true;
                 
             default: sendHelp(player); return true;
         }
     }
     
     /**
      * Gives ownership of the current Chunk to the Player
      * 
      * @param player The Player buying the Chunk
      */
     public static void buy(Player player) {
         //Cancel if the Player does not have permission to use the command
         if (!ChunkOwn.hasPermission(player, "own")) {
             player.sendMessage(ChunkOwnMessages.permission);
             return;
         }
         
         //Retrieve the OwnedChunk that the Player is in
         Chunk chunk = player.getLocation().getBlock().getChunk();
         OwnedChunk ownedChunk = ChunkOwn.findOwnedChunk(chunk);
 
         //If the owner of the OwnedChunk is not blank then the Chunk is already claimed
         if (ownedChunk != null) {
             player.sendMessage(ChunkOwnMessages.claimed);
             return;
         }
         
         ownedChunk = new OwnedChunk(chunk);
 
         int limit = ChunkOwn.getOwnLimit(player);
         int owned = 0;
         
         //Check if the Player is limited
         if (limit != -1) {
             //Retrieve the ChunkCounter value of the Player
             if (ChunkOwn.chunkCounter.containsKey(player.getName()))
                 owned = ChunkOwn.chunkCounter.get(player.getName());
             
             //Cancel if the Player owns their maximum limit
             if (owned >= limit) {
                 player.sendMessage(ChunkOwnMessages.limit);
                 return;
             }
         }
         
         String name = player.getName();
         
         //Check if a group size is required
         if (ChunkOwn.groupSize > 1) {
             //Check if the Chunk is a loner (not connected to other owned Chunks
             if (ownedChunk.isLoner())
                 if (!ChunkOwn.canBuyLoner(ChunkOwn.getOwnedChunks(name))) {
                     player.sendMessage(ChunkOwnMessages.groupLand.replace("<MinimumGroupSize>", String.valueOf(ChunkOwn.groupSize)));
                     return;
                 }
         }
         
         //Charge the Player only if they don't have the 'chunkown.free' node
         if (ChunkOwn.hasPermission(player, "free"))
             player.sendMessage(ChunkOwnMessages.buyFree);
         else if(!Econ.buy(player))
             return;
         
         ownedChunk.owner = name;
         
         ChunkOwn.saveSnapshot(chunk);
         markCorners(chunk);
         ChunkOwn.addOwnedChunk(ownedChunk);
     }
     
     /**
      * Previews the boundaries of the current Chunk
      * 
      * @param player The Player previewing the Chunk
      */
     public void preview(Player player) {
         //Cancel if the Player does not have permission to use the command
         if (!ChunkOwn.hasPermission(player, "preview")) {
             player.sendMessage(ChunkOwnMessages.permission);
             return;
         }
         
         // If not admin, enforce a cooldown period for this command
         if (!ChunkOwn.hasPermission(player, "admin") && playerLastPreview.containsKey(player.getName())) {
             long lastPreviewTime = playerLastPreview.get(player.getName());
             long currentTime = System.currentTimeMillis() / 1000;
             long delta = currentTime - lastPreviewTime;
             
             if (delta < cooldown) {
                 player.sendMessage("You must wait " + (cooldown - delta) + " seconds before previewing another chunk.");
                 return;
             }
         }
         
         //Retrieve the OwnedChunk that the Player is in
         Chunk chunk = player.getLocation().getBlock().getChunk();
 
         if (ChunkOwn.findOwnedChunk(chunk) != null) {
             // If an OwnedChunk is found, the chunk is already claimed
             player.sendMessage(ChunkOwnMessages.claimed);
             return;
         }
 
         int limit = ChunkOwn.getOwnLimit(player);
         int owned = 0;
         
         //Don't check how many are owned if the Player is not limited
         if (limit != -1) {
             //Retrieve the ChunkCounter value of the Player
             Object object = ChunkOwn.chunkCounter.get(player.getName());
             if (object != null)
                 owned = (Integer)object;
             
             //Cancel if the Player owns their maximum limit
             if (owned >= limit) {
                 player.sendMessage(ChunkOwnMessages.limit);
                 return;
             }
         }
         
         markCorners(chunk);
         
         playerLastPreview.put(player.getName(), System.currentTimeMillis() / 1000);
     }
     
     /**
      * Removes ownership of the current Chunk from the Player
      * 
      * @param player The Player selling the Chunk
      */
     public static void sell(Player player) {
         //Cancel if the Player does not have permission to use the command
         if (!ChunkOwn.hasPermission(player, "own")) {
             player.sendMessage(ChunkOwnMessages.permission);
             return;
         }
         
         //Retrieve the OwnedChunk that the Player is in
         Chunk chunk = player.getLocation().getBlock().getChunk();
         OwnedChunk ownedChunk = ChunkOwn.findOwnedChunk(chunk);
 
         //Cancel if the Chunk is not owned
         if (ownedChunk == null) {
             player.sendMessage(ChunkOwnMessages.doNotOwn);
             return;
         }
         
         //Cancel if the OwnedChunk is owned by someone else
         if (!ownedChunk.owner.equals(player.getName()))
             if (ChunkOwn.hasPermission(player, "admin"))
                 Econ.sell(player, ownedChunk.owner);
             else {
                 player.sendMessage(ChunkOwnMessages.doNotOwn);
                 return;
             }
         else
             Econ.sell(player);
         
         ChunkOwn.removeOwnedChunk(chunk);
     }
     
     /**
      * Display to the Player all of the Chunks that they own
      * 
      * @param player The Player requesting the list
      */
     public static void list(Player player) {
         //Cancel if the Player does not have permission to use the command
         if (!ChunkOwn.hasPermission(player, "own")) {
             player.sendMessage(ChunkOwnMessages.permission);
             return;
         }
         
         String name = player.getName();
         
         //Retrieve the ChunkCounter value to display to the Player
         int owned = 0;
         Object object = ChunkOwn.chunkCounter.get(name);
         if (object != null)
             owned = (Integer)object;
         player.sendMessage("Number of Chunks owned: "+owned);
 
         //Retrieve the ownLimit to display to the Player
         int ownLimit = ChunkOwn.getOwnLimit(player);
         if (ownLimit > -1)
             player.sendMessage("Total amount you may own: "+ownLimit);
         
         for (OwnedChunk ownedChunk: ChunkOwn.getOwnedChunks())
             if (ownedChunk.owner.equals(name))
                 player.sendMessage(ownedChunk.toString());
     }
     
     /**
      * Display to the Player the info of the current Chunk
      * Info displayed is the Location of the Chunk and the current CoOwners
      * 
      * @param player The Player requesting the info
      */
     public static void info(Player player) {
         //Cancel if the Player does not have permission to use the command
         if (!ChunkOwn.hasPermission(player, "info")) {
             player.sendMessage(ChunkOwnMessages.permission);
             return;
         }
         
         //Retrieve the OwnedChunk that the Player is in
         Chunk chunk = player.getLocation().getBlock().getChunk();
         OwnedChunk ownedChunk = ChunkOwn.findOwnedChunk(chunk);
 
         //Cancel if the OwnedChunk does not exist
         if (ownedChunk == null) {
             player.sendMessage(ChunkOwnMessages.unclaimed);
             return;
         }
 
         //Display the world and x/y-coordinates of the center of the OwnedChunk to the Player
         player.sendMessage(ownedChunk.toString()+" belongs to "+ownedChunk.owner);
 
         //Display CoOwners of OwnedChunk to Player
         String coOwners = "CoOwners:  ";
         for (String coOwner: ownedChunk.coOwners)
             coOwners = coOwners.concat(coOwner.concat(", "));
         player.sendMessage(coOwners.substring(0, coOwners.length() - 2));
 
         //Display CoOwner Groups of OwnedChunk to Player
         String groups = "CoOwner Groups:  ";
         for (String group: ownedChunk.groups)
             groups = groups.concat(group.concat(", "));
         player.sendMessage(groups.substring(0, groups.length() - 2));
     }
     
     /**
      * Manages CoOwnership of the given Chunk if the Player is the Owner
      * 
      * @param player The given Player who may be the Owner
      * @param type The given type: 'player' or 'group'
      * @param action The given action: 'add' or 'remove'
      * @param coOwner The given CoOwner
      */
     public static void coowner(Player player, String type, String action, String coOwner) {
         //Cancel if the Player does not have permission to use the command
         if (!ChunkOwn.hasPermission(player, "coowner")) {
             player.sendMessage(ChunkOwnMessages.permission);
             return;
         }
         
         //Retrieve the OwnedChunk that the Player is in
         Chunk chunk = player.getLocation().getBlock().getChunk();
         OwnedChunk ownedChunk = ChunkOwn.findOwnedChunk(chunk);
 
         //Cancel if the OwnedChunk does not exist
         if (ownedChunk == null) {
             player.sendMessage(ChunkOwnMessages.unclaimed);
             return;
         }
 
         //Cancel if the OwnedChunk is owned by someone else
         if (!ownedChunk.owner.equals(player.getName())) {
             player.sendMessage(ChunkOwnMessages.doNotOwn);
             return;
         }
 
         //Determine the command to execute
         if (type.equals("player"))
             if (action.equals("add")) {
                 //Cancel if the Player is already a CoOwner
                 if (ownedChunk.coOwners.contains(coOwner)) {
                     player.sendMessage(coOwner+" is already a CoOwner");
                     return;
                 }
                 
                 ownedChunk.coOwners.add(coOwner);
                 player.sendMessage(coOwner+" added as a CoOwner");
             }
             else if (action.equals("remove"))
                 ownedChunk.coOwners.remove(coOwner);
             else {
                 sendHelp(player);
                 return;
             }
         else if (type.equals("group"))
             if (action.equals("add")) {
                 //Cancel if the Group is already a CoOwner
                 if (ownedChunk.groups.contains(coOwner)) {
                     player.sendMessage(coOwner+" is already a CoOwner");
                     return;
                 }
                 
                 ownedChunk.groups.add(coOwner);
                 player.sendMessage(coOwner+" added as a CoOwner");
             }
             else if (action.equals("remove"))
                 ownedChunk.groups.remove(coOwner);
             else {
                 sendHelp(player);
                 return;
             }
         else {
             sendHelp(player);
             return;
         }
         
         ChunkOwn.save(chunk.getWorld());
     }
     
     /**
      * Removes all the Chunks that are owned by the given Player
      * A Chunk is owned buy a Player if the owner field is the Player's name
      * 
      * @param player The given Player
      */
     public static void clear(Player player) {
         clear(player, player.getName());
     }
     
     /**
      * Removes all the Chunks that are owned by the given Player
      * A Chunk is owned buy a Player if the owner field is the Player's name
      * 
      * @param player The name of the Player
      */
     public static void clear(String player) {
         clear(null, player);
     }
     
     /**
      * Removes all the Chunks that are owned by the given Player
      * A Chunk is owned buy a Player if the owner field is the Player's name
      * 
      * @param player The given Player
      */
     private static void clear(Player player, String name) {
        Iterator <OwnedChunk> itr = ChunkOwn.getOwnedChunks().iterator();
        OwnedChunk ownedChunk;
        
        while (itr.hasNext()) {
            ownedChunk = itr.next();
            
             //Sell the Chunk if it is owned by the given Player
             if (ownedChunk.owner.equals(name)) {
                 if (player == null)
                     Econ.sell(name);
                 else
                     Econ.sell(player);
 
                 ChunkOwn.removeOwnedChunk(ownedChunk);
             }
        }
         
         ChunkOwn.saveAll();
     }
     
     /**
      * Places Blocks of a predetermined type just above the highest Block at each corner of the given Chunk
      *
      * @param chunk The given Chunk
      */
     public static void markCorners(Chunk chunk) {
         for (int x = 0; x <= 15; x = x + 15)
             for (int z = 0; z <= 15; z = z + 15) {
                 int y = chunk.getWorld().getMaxHeight() - 2;
                 
                 Block block = chunk.getBlock(x, y, z);
                 while (y >= 0) {
                     switch(block.getType()) {
                         case LEAVES: //Fall through
                         case AIR: y--; break;
                         
                         case SAPLING: //Fall through
                         case LONG_GRASS: //Fall through
                         case DEAD_BUSH: //Fall through
                         case YELLOW_FLOWER: //Fall through
                         case RED_ROSE: //Fall through
                         case BROWN_MUSHROOM: //Fall through
                         case RED_MUSHROOM: //Fall through
                         case SNOW:
                             block.setTypeId(cornerID);
                             y = -1;
                             break;
                         
                         case BED_BLOCK: //Fall through
                         case POWERED_RAIL: //Fall through
                         case DETECTOR_RAIL: //Fall through
                         case RAILS: //Fall through
                         case STONE_PLATE: //Fall through
                         case WOOD_PLATE:
                             block.getRelative(0, 2, 0).setTypeId(cornerID);
                             y = -1;
                             break;
                         
                         default:
                             block.getRelative(0, 1, 0).setTypeId(cornerID);
                             y = -1;
                             break;
                     }
                     
                     block = block.getRelative(0, -1, 0);
                 }
             }
     }
     
     /**
      * Displays the ChunkOwn Help Page to the given Player
      *
      * @param Player The Player needing help
      */
     public static void sendHelp(Player player) {
         player.sendMessage("§e     ChunkOwn Help Page:");
         player.sendMessage("§2/"+command+" buy§b Purchase the current chunk for "+Econ.format(Econ.getBuyPrice(player.getName())));
         player.sendMessage("§2/"+command+" sell§b Sell the current chunk for "+Econ.format(Econ.getSellPrice(player.getName())));
         player.sendMessage("§2/"+command+" preview§b Preview the current chunk's boundaries");
         player.sendMessage("§2/"+command+" list§b List locations of owned Chunks");
         player.sendMessage("§2/"+command+" info§b List Owner and CoOwners of current Chunk");
         player.sendMessage("§2/"+command+" clear§b Sell all owned Chunks");
         player.sendMessage("§2/"+command+" coowner [Action] [Type] [Name]");
         player.sendMessage("§bAction = 'add' or 'remove'");
         player.sendMessage("§bType = 'player' or 'group'");
         player.sendMessage("§bName = The group name or the Player's name");
     }
 }
