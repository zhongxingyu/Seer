 package com.dumptruckman.gmtools.listeners;
 
 import com.dumptruckman.gmtools.GMTools;
 import com.dumptruckman.gmtools.configuration.Config;
 import com.dumptruckman.gmtools.permissions.Perms;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Fireball;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 import org.getchunky.chunky.ChunkyManager;
 import org.getchunky.chunky.module.ChunkyPermissions;
 import org.getchunky.chunky.object.ChunkyChunk;
 import org.getchunky.chunky.object.ChunkyPlayer;
 import org.getchunky.chunky.permission.AccessLevel;
 import org.getchunky.chunky.permission.PermissionChain;
 
 /**
  * @author dumptruckman
  */
 public class GMToolsPlayerListener extends PlayerListener {
 
     public void onPlayerMove(PlayerMoveEvent event) {
         Player player = event.getPlayer();
         if (!GMTools.getExplosionPlayers().contains(player)) return;
 
        event.getTo().getWorld().createExplosion(event.getTo(), 0);
     }
 
     public void onPlayerInteract(PlayerInteractEvent event) {
         Player player = event.getPlayer();
         if (player.getItemInHand().getTypeId() == 51) fireBall(event);
         //if (player.getItemInHand().getTypeId() == Config.BED_SPAWN_ITEM.getInteger()) bedSpawn(event);
     }
 
     public void fireBall(PlayerInteractEvent event) {
         Player player = event.getPlayer();
         if (!GMTools.getFireBallPlayers().contains(player)) return;
 
         if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
 
         final Vector direction = player.getEyeLocation().getDirection().multiply(2);
                 player.getWorld().spawn(player.getEyeLocation().add(direction.getX(), direction.getY(), direction.getZ()), Fireball.class);
     }
 
     public void bedSpawn(PlayerInteractEvent event) {
         Player player = event.getPlayer();
         if (!Perms.BED_SPAWN.has(player)) return;
 
         if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
 
         if (GMTools.isChunky()) {
             ChunkyChunk cChunk = ChunkyManager.getChunkyChunk(player.getLocation());
             ChunkyPlayer cPlayer = ChunkyManager.getChunkyPlayer(player);
             AccessLevel access = PermissionChain.hasPerm(cChunk, cPlayer, ChunkyPermissions.ITEM_USE);
             if (access.causedDenial()) {
                 player.sendMessage("You may not teleport out of chunks you cannot use items on!");
                 return;
             }
         }
 
         Location loc = null;
         try {
             loc = player.getBedSpawnLocation();
         } catch (Exception ignore) {}
         if (loc == null) Bukkit.getWorld(Config.SPAWN_WORLD.getString()).getSpawnLocation();
         player.teleport(loc);
         
         ItemStack itemInHand = player.getItemInHand();
         int newAmount = itemInHand.getAmount() - 1;
         if (newAmount < 1)
             player.getInventory().clear(player.getInventory().getHeldItemSlot());
         else
             itemInHand.setAmount(newAmount);
     }
 
 
 
     public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
         if (event.getMessage().startsWith("/spawn")) commandSpawn(event);
     }
 
     public void commandSpawn(PlayerCommandPreprocessEvent event) {
         Player player = event.getPlayer();
         if (GMTools.isChunky()) {
             ChunkyChunk cChunk = ChunkyManager.getChunkyChunk(player.getLocation());
             ChunkyPlayer cPlayer = ChunkyManager.getChunkyPlayer(player);
             if (PermissionChain.hasPerm(cChunk, cPlayer, ChunkyPermissions.ITEM_USE).causedDenial()) {
                 player.sendMessage(ChatColor.RED + "You may not teleport out of chunks you cannot use items on!");
                 player.sendMessage("You may " + ChatColor.GREEN + "/suicide" + ChatColor.WHITE + " if you are stuck.");
                 player.sendMessage("A single chest will appear to try and save your inventory.");
                 event.setCancelled(true);
             }
         }
     }
 
 
 }
