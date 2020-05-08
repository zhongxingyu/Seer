 /*
  * MyResidence, Bukkit plugin for managing your towns and residences
  * Copyright (C) 2011, Michael Hohl
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package at.co.hohl.myresidence.commands;
 
 import at.co.hohl.myresidence.ChunkManager;
 import at.co.hohl.myresidence.MyResidence;
 import at.co.hohl.myresidence.Nation;
 import at.co.hohl.myresidence.exceptions.MyResidenceException;
 import at.co.hohl.myresidence.exceptions.NotEnoughMoneyException;
 import at.co.hohl.myresidence.exceptions.PermissionsDeniedException;
 import at.co.hohl.myresidence.storage.Session;
 import at.co.hohl.myresidence.storage.persistent.Town;
 import com.sk89q.minecraft.util.commands.Command;
 import com.sk89q.minecraft.util.commands.CommandContext;
 import com.sk89q.minecraft.util.commands.CommandPermissions;
 import com.sk89q.worldedit.IncompleteRegionException;
 import com.sk89q.worldedit.Vector2D;
 import com.sk89q.worldedit.regions.Region;
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 /**
  * Commands for expanding towns.
  *
  * @author Michael Hohl
  */
 public class TownClaimCommands {
   @Command(
           aliases = {"chunk", "c"},
           desc = "Claims the single chunk, you are currently stand on",
           flags = "i",
           max = 0
   )
   @CommandPermissions({"myresidence.major"})
   public static void claimChunk(final CommandContext args,
                                 final MyResidence plugin,
                                 final Nation nation,
                                 final Player player,
                                 final Session session)
           throws MyResidenceException {
     // Get selections.
     final Town selectedTown = session.getSelectedTown();
     final Chunk playerChunk = player.getLocation().getBlock().getChunk();
     final Vector2D chunkVector = new Vector2D(playerChunk.getX(), playerChunk.getZ());
     final World chunkWorld = playerChunk.getWorld();
 
     // Check if player is major.
     if (!session.hasMajorRights(selectedTown)) {
       throw new PermissionsDeniedException("You are not the major of the selected town!");
     }
 
     // Check if player is allowed to use the i-flag
     if (args.hasFlag('i') && !nation.getPermissionsResolver().isAdmin(player)) {
       throw new PermissionsDeniedException("Only admins are allowed to use the ignore flag!");
     }
 
     // Check if already reserved?
     Town chunkOwner = nation.getChunkManager().getChunkOwner(chunkWorld, chunkVector);
     if (chunkOwner != null) {
       if (chunkOwner.equals(selectedTown)) {
         throw new MyResidenceException("Town already owns this chunk!");
       } else if (!args.hasFlag('i')) {
         throw new MyResidenceException("This chunk is already bought by another town!");
       }
     }
 
     // Check money.
     double chunkCost = plugin.getConfiguration(player.getWorld()).getChunkCost();
     double townMoney = selectedTown.getMoney();
     if (townMoney < chunkCost) {
       throw new NotEnoughMoneyException(chunkCost);
     } else {
       selectedTown.subtractMoney(chunkCost);
     }
 
     // Reserve new chunk.
     nation.getChunkManager().addChunk(selectedTown, chunkWorld, chunkVector);
 
    // Save and end transaction
    nation.getDatabase().save(selectedTown);

     player.sendMessage(ChatColor.DARK_GREEN + "Town bought chunk for " + ChatColor.GREEN +
             plugin.format(chunkCost) + ChatColor.DARK_GREEN + ".");
   }
 
   @Command(
           aliases = {"selection", "s"},
           desc = "Claims all Chunks in your WE selection.",
           flags = "i",
           max = 0
   )
   @CommandPermissions({"myresidence.major"})
   public static void claimSelection(final CommandContext args,
                                     final MyResidence plugin,
                                     final Nation nation,
                                     final Player player,
                                     final Session session)
           throws IncompleteRegionException, MyResidenceException {
     ChunkManager chunkManager = nation.getChunkManager();
 
     // Get selections.
     final Town selectedTown = session.getSelectedTown();
     final World selectedWorld = plugin.getWorldEdit().getSelection(player).getWorld();
     final Region selectedRegion = plugin.getWorldEdit().getSelection(player).getRegionSelector().getRegion();
 
     // Check if player is major.
     if (!session.hasMajorRights(selectedTown)) {
       throw new PermissionsDeniedException("You are not the major of the selected town!");
     }
 
     // Check if player is allowed to use the i-flag
     if (args.hasFlag('i') && !nation.getPermissionsResolver().isAdmin(player)) {
       throw new PermissionsDeniedException("Only admins are allowed to use the ignore flag!");
     }
 
     // Count chunks to bought.
     int numberOfChunksToBought = 0;
     for (final Vector2D chunk : selectedRegion.getChunks()) {
       Town chunkOwner = chunkManager.getChunkOwner(selectedWorld, chunk);
       if (chunkOwner == null) {
         numberOfChunksToBought++;
       } else if (chunkOwner != selectedTown && args.hasFlag('i')) {
         throw new MyResidenceException("At least on of the chunks is already bought by another town!");
       }
     }
 
     // Check money.
     double chunkCost = plugin.getConfiguration(player.getWorld()).getChunkCost();
     double cost = chunkCost * numberOfChunksToBought;
     if (selectedTown.getMoney() < cost) {
       throw new NotEnoughMoneyException(chunkCost * numberOfChunksToBought);
     }
 
     // Add chunks to town and then subtract the money from the town account.
     chunkManager.addChunks(selectedTown, selectedWorld, selectedRegion.getChunks());
     selectedTown.subtractMoney(cost);
 
     // Save and end transaction
     nation.save(selectedTown);
 
     // Notify user.
     player.sendMessage(ChatColor.DARK_GREEN + "Town bought " +
             ChatColor.GREEN + numberOfChunksToBought +
             ChatColor.DARK_GREEN + " chunks for " +
             ChatColor.GREEN + plugin.format(cost) +
             ChatColor.DARK_GREEN + ".");
   }
 }
