 package com.codisimus.plugins.chunkown.listeners;
 
 import com.codisimus.plugins.chunkown.ChunkOwn;
 import com.codisimus.plugins.chunkown.Econ;
 import com.codisimus.plugins.chunkown.OwnedChunk;
 import java.util.Iterator;
 import java.util.LinkedList;
 import org.bukkit.Chunk;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  * Executes Player Commands
  * 
  * @author Codisimus
  */
 public class CommandListener implements CommandExecutor {
     private static enum Action { BUY, SELL, LIST, INFO, COOWNER, CLEAR }
     public static int cornerID;
     public static String permissionMsg;
     public static String claimedMsg;
     public static String limitMsg;
     public static String unclaimedMsg;
     public static String buyFreeMsg;
     
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
             player.sendMessage(permissionMsg);
             return;
         }
         
         //Retrieve the OwnedChunk that the Player is in
         Chunk chunk = player.getLocation().getBlock().getChunk();
         String world = player.getWorld().getName();
         int x = chunk.getX();
         int z = chunk.getZ();
         OwnedChunk ownedChunk = ChunkOwn.getOwnedChunk(world, x, z);
 
         //If the owner of the OwnedChunk is not blank then the Chunk is already claimed
         if (ownedChunk.owner != null) {
             player.sendMessage(claimedMsg);
             return;
         }
         
         ownedChunk.owner = player.getName();
 
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
                 player.sendMessage(limitMsg);
                 return;
             }
         }
         
         //Charge the Player only if they don't have the 'chunkown.free' node
         if (ChunkOwn.hasPermission(player, "free"))
             player.sendMessage(buyFreeMsg);
         else if(!Econ.buy(player)) {
             //Delete the OwnedChunk because the Player could not afford it
             ChunkOwn.removeOwnedChunk(world, x, z);
             return;
         }
         
         //Increment the ChunkCounter of the Player
         ChunkOwn.chunkCounter.put(ownedChunk.owner, owned + 1);
 
         markCorners(chunk);
         ChunkOwn.save();
     }
     
     /**
      * Removes ownership of the current Chunk from the Player
      * 
      * @param player The Player selling the Chunk
      */
     public static void sell(Player player) {
         //Cancel if the Player does not have permission to use the command
         if (!ChunkOwn.hasPermission(player, "own")) {
             player.sendMessage(permissionMsg);
             return;
         }
         
         //Retrieve the OwnedChunk that the Player is in
         String world = player.getWorld().getName();
         Chunk chunk = player.getLocation().getBlock().getChunk();
         int x = chunk.getX();
         int z = chunk.getZ();
         OwnedChunk ownedChunk = ChunkOwn.findOwnedChunk(world, x, z);
 
         //Cancel if the Chunk is not owned
         if (ownedChunk == null) {
             player.sendMessage(ChunkOwn.doNotOwnMsg);
             return;
         }
         
         //Cancel if the OwnedChunk is owned by someone else
         if (!ownedChunk.owner.equals(player.getName()))
             if (ChunkOwn.hasPermission(player, "admin"))
                 Econ.sell(player, ownedChunk.owner);
             else {
                 player.sendMessage(ChunkOwn.doNotOwnMsg);
                 return;
             }
         else
             Econ.sell(player);
         
         ChunkOwn.removeOwnedChunk(world, x, z);
     }
     
     /**
      * Display to the Player all of the Chunks that they own
      * 
      * @param player The Player requesting the list
      */
     public static void list(Player player) {
         //Cancel if the Player does not have permission to use the command
         if (!ChunkOwn.hasPermission(player, "own")) {
             player.sendMessage(permissionMsg);
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
 
         //Iterate through all OwnedChunks
         for (int i=0; i<100; i++)
             for (int j=0; j<100; j++) {
                 LinkedList<OwnedChunk> chunkList = (LinkedList<OwnedChunk>)ChunkOwn.matrix[i][j];
                 if (chunkList != null)
                     for (OwnedChunk ownedChunk: chunkList)
                         //Display Chunk info if it is owned by the given Player
                         if (ownedChunk.owner.equals(name))
                             player.sendMessage("Chunk in world="+ownedChunk.world+", centered @: x="+(ownedChunk.x*16+8)+" z="+(ownedChunk.z*16+8));
             }
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
             player.sendMessage(permissionMsg);
             return;
         }
         
         //Retrieve the OwnedChunk that the Player is in
         String world = player.getWorld().getName();
         Chunk chunk = player.getLocation().getBlock().getChunk();
         int x = chunk.getX();
         int z = chunk.getZ();
         OwnedChunk ownedChunk = ChunkOwn.findOwnedChunk(world, x, z);
 
         //Cancel if the OwnedChunk does not exist
         if (ownedChunk == null) {
             player.sendMessage(unclaimedMsg);
             return;
         }
 
         //Display the world and x/y-coordinates of the center of the OwnedChunk to the Player
        player.sendMessage("Chunk @ world="+world+" x="+(x*16+8)+" z="+(z*16+8));
 
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
             player.sendMessage(permissionMsg);
             return;
         }
         
         //Retrieve the OwnedChunk that the Player is in
         Chunk chunk = player.getLocation().getBlock().getChunk();
         OwnedChunk ownedChunk = ChunkOwn.findOwnedChunk(player.getWorld().getName(), chunk.getX(), chunk.getZ());
 
         //Cancel if the OwnedChunk does not exist
         if (ownedChunk == null) {
             player.sendMessage(unclaimedMsg);
             return;
         }
 
         //Cancel if the OwnedChunk is owned by someone else
         if (!ownedChunk.owner.equals(player.getName())) {
             player.sendMessage(ChunkOwn.doNotOwnMsg);
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
         else if(type.equals("group"))
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
         
         ChunkOwn.save();
     }
     
     /**
      * Removes all the Chunks that are owned by the given Player
      * A Chunk is owned buy a Player if the owner field is the Player's name
      * 
      * @param player The name of the Player
      */
     public static void clear(Player player) {
         String name = player.getName();
         
         //Iterate through all OwnedChunks
         for (int i=0; i<100; i++)
             for (int j=0; j<100; j++) {
                 LinkedList<OwnedChunk> chunkList = (LinkedList<OwnedChunk>)ChunkOwn.matrix[i][j];
                 if (chunkList != null) {
                     Iterator itr = chunkList.iterator();
                     while (itr.hasNext()) {
                         OwnedChunk ownedChunk = (OwnedChunk)itr.next();
 
                         //Sell the Chunk if it is owned by the given Player
                         if (ownedChunk.owner.equals(name)) {
                             itr.remove();
                             Econ.sell(player);
                         }
                     }
                     
                     //Delete the ChunkList if it is empty
                     if (chunkList.isEmpty())
                         ChunkOwn.matrix[i][j] = null;
                 }
             }
         
         //Reset the ChunkCounter of the Player to 0
         ChunkOwn.chunkCounter.put(player.getName(), 0);
         
         ChunkOwn.save();
     }
     
     /**
      * Places Blocks of a predetermined type just above the highest Block at each corner of the given Chunk
      *
      * @param chunk The given Chunk
      */
     public static void markCorners(Chunk chunk) {
         //Get the highest Block (should be empty) at the South-West corner
         Block block = chunk.getBlock(0, 127, 0);
         //Move down until a non-empty Block is found
         while (block.getTypeId() == 0)
             block = block.getRelative(BlockFace.DOWN);
         //Change the empty Block just above the Block found
         block.getRelative(BlockFace.UP).setTypeId(cornerID);
         
         //Get the highest Block (should be empty) at the South-East corner
         block = chunk.getBlock(0, 127, 15);
         //Move down until a non-empty Block is found
         while (block.getTypeId() == 0)
             block = block.getRelative(BlockFace.DOWN);
         //Change the empty Block just above the Block found
         block.getRelative(BlockFace.UP).setTypeId(cornerID);
         
         //Get the highest Block (should be empty) at the North-West corner
         block = chunk.getBlock(15, 127, 0);
         //Move down until a non-empty Block is found
         while (block.getTypeId() == 0)
             block = block.getRelative(BlockFace.DOWN);
         //Change the empty Block just above the Block found
         block.getRelative(BlockFace.UP).setTypeId(cornerID);
         
         //Get the highest Block (should be empty) at the North-East corner
         block = chunk.getBlock(15, 127, 15);
         //Move down until a non-empty Block is found
         while (block.getTypeId() == 0)
             block = block.getRelative(BlockFace.DOWN);
         //Change the empty Block just above the Block found
         block.getRelative(BlockFace.UP).setTypeId(cornerID);
     }
     
     /**
      * Displays the ChunkOwn Help Page to the given Player
      *
      * @param Player The Player needing help
      */
     public static void sendHelp(Player player) {
         player.sendMessage("§e     ChunkOwn Help Page:");
         player.sendMessage("§2/chunk buy§b Purchase the current chunk for "+Econ.format(Econ.buyPrice));
         player.sendMessage("§2/chunk sell§b Sell the current chunk for "+Econ.format(Econ.sellPrice));
         player.sendMessage("§2/chunk list§b List locations of owned Chunks");
         player.sendMessage("§2/chunk info§b List Owner and CoOwners of current Chunk");
         player.sendMessage("§2/chunk clear§b Sell all owned Chunks");
         player.sendMessage("§2/chunk coowner [Action] [Type] [Name]");
         player.sendMessage("§bAction = 'add' or 'remove'");
         player.sendMessage("§bType = 'player' or 'group'");
         player.sendMessage("§bName = The group name or the Player's name");
     }
 }
