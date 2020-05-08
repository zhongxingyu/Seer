 package com.modwiz.blessprotect;
 
 import org.bukkit.Effect;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.DoubleChest;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPhysicsEvent;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.material.Door;
 import org.bukkit.material.MaterialData;
 import org.bukkit.material.TrapDoor;
 
 /**
  * Created with IntelliJ IDEA.
  * User: starbuck
  * Date: 1/3/13
  * Time: 11:56 AM
  * To change this template use File | Settings | File Templates.
  */
 public class ClaimListener implements Listener {
     public BlessProtect plug;
 
     public ClaimListener(BlessProtect plugin) {
         plug = plugin;
     }
 
     @EventHandler
     public void breakListener(BlockBreakEvent event) {
         ClaimedBlock claim = plug.claimManager.getClaim(event.getBlock());
         if (claim == null) {
             return;
         }else if (!claim.hasPermission(event.getPlayer())) {
             event.setCancelled(true);
             event.getPlayer().sendMessage("Sorry that block is owned by " + claim.owner +" ask them or an admin to use it.");
         } else {
             if (claim.toBlock().getType() == Material.WOODEN_DOOR ||
                     claim.toBlock().getType() == Material.IRON_DOOR_BLOCK) {
                 removeDoorClaim(plug.claimManager, claim);
                 event.getPlayer().sendMessage("Claimed door removed");
             } else if (claim.toBlock().getType() == Material.WOOD_BUTTON ||
                     claim.toBlock().getType() == Material.STONE_BUTTON) {
                 plug.claimManager.removeClaim(claim);
                 event.getPlayer().sendMessage("Claimed button removed");
             } else if (claim.toBlock().getType() == Material.CHEST) {
                 plug.claimManager.removeClaim(claim);
                 event.getPlayer().sendMessage("Claimed chest removed.");
            } else {
                claim = plug.claimManager.getClaim(event.getBlock().getRelative(BlockFace.UP));
                if (claim.toBlock().getType() == Material.WOODEN_DOOR ||
                        claim.toBlock().getType() == Material.IRON_DOOR_BLOCK) {
                    removeDoorClaim(plug.claimManager, claim);
                    event.getPlayer().sendMessage("Claimed door removed");
                }
             }
 
         }
     }
 
     @EventHandler
     public void blockInteract(PlayerInteractEvent event) {
         if (event.getAction() == Action.RIGHT_CLICK_BLOCK ||
                 event.getAction() == Action.RIGHT_CLICK_AIR) {
             Block block = event.getClickedBlock();
             if (block == null ||
                     block.getType() == Material.AIR) {
                 return;

             }
             Player player = event.getPlayer();
 
             if (block.getType() == Material.WOOD_BUTTON ||
                     block.getType() == Material.WOOD_BUTTON ||
                     block.getType() == Material.WOODEN_DOOR ||
                     block.getType() == Material.WOOD_DOOR ||
                     block.getType() == Material.IRON_DOOR_BLOCK) {
                 ClaimedBlock claim = plug.claimManager.getClaim(block);
                 if (claim != null) {
                     if (!claim.hasPermission(player)) {
                         event.setCancelled(true);
                         player.sendMessage("You don't have permission to use a block owned by " + claim.owner + " ask them or an admin for help.");
                     } else {
                         if (block.getType() == Material.IRON_DOOR_BLOCK) {
                             if (isDoorClosed(block)) {
                                 openDoor(block);
                             } else {
                                 closeDoor(block);
                             }
                         }
                     }
                 }
             }
         }
     }
 
 
 
 
     @EventHandler
     public void blockUpdate(BlockRedstoneEvent event) {
 
         Block block = event.getBlock();
         if (block == null ||
                 block.getType() == Material.AIR) {
             return;
         }
         if (block.getType() == Material.WOOD_BUTTON ||
                 block.getType() == Material.WOOD_BUTTON ||
                 block.getType() == Material.WOODEN_DOOR ||
                 block.getType() == Material.WOOD_DOOR ||
                 block.getType() == Material.IRON_DOOR_BLOCK) {
             ClaimedBlock claim = plug.claimManager.getClaim(block);
             if (claim != null) {
                 event.setNewCurrent(event.getOldCurrent());
             }
         }
 
     }
 
    @EventHandler
    public void physicsBlock(BlockPhysicsEvent event) {
        Block block = event.getBlock().getRelative(BlockFace.UP);
        ClaimedBlock claim = plug.claimManager.getClaim(block);

        if (claim != null) {
            event.setCancelled(true);
        }
    }

     private void removeDoorClaim(ClaimManager claimManager, ClaimedBlock claimedBlock) {
         String claimOwner = claimedBlock.owner;
         claimManager.removeClaim(claimedBlock);
         Block down = claimedBlock.toBlock().getRelative(BlockFace.DOWN);
         Block up = claimedBlock.toBlock().getRelative(BlockFace.UP);
 
         if ((down.getType() == Material.IRON_DOOR_BLOCK) ||
                 (down.getType()== Material.WOODEN_DOOR)) {
             claimedBlock = new ClaimedBlock(down, claimOwner);
             claimManager.removeClaim(claimedBlock);
             Block holder = down.getRelative(BlockFace.DOWN);
             ClaimedBlock claim = new ClaimedBlock(holder, claimOwner);
             claimManager.removeClaim(claim);
             return;
         }
 
         if ((up.getType() == Material.IRON_DOOR_BLOCK) ||
                 (up.getType()== Material.WOODEN_DOOR)) {
             Block oldClaim = claimedBlock.toBlock();
             claimedBlock = new ClaimedBlock(up, claimOwner);
             claimManager.addClaim(claimedBlock);
             Block holder = oldClaim.getRelative(BlockFace.DOWN);
             ClaimedBlock claim = new ClaimedBlock(holder, claimOwner);
             claimManager.removeClaim(claim);
             return;
         }
     }
 
     static void openDoor(Block block) {
         if (block.getType() == Material.TRAP_DOOR) {
             BlockState state = block.getState();
             TrapDoor trapdoor = (TrapDoor)state.getData();
             trapdoor.setOpen(true);
             state.update();
         } else {
             byte data = block.getData();
             if ((data & 0x8) == 0x8) {
                 block = block.getRelative(BlockFace.DOWN);
                 data = block.getData();
             }
             if (isDoorClosed(block)) {
                 data = (byte) (data | 0x4);
                 block.setData(data, true);
                 block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 0);
             }
         }
     }
 
     static boolean isDoorClosed(Block block) {
         if (block.getType() == Material.TRAP_DOOR) {
             TrapDoor trapdoor = (TrapDoor)block.getState().getData();
             return !trapdoor.isOpen();
         } else {
             byte data = block.getData();
             if ((data & 0x8) == 0x8) {
                 block = block.getRelative(BlockFace.DOWN);
                 data = block.getData();
             }
             return ((data & 0x4) == 0);
         }
     }
 
     static void closeDoor(Block block) {
         if (block.getType() == Material.TRAP_DOOR) {
             BlockState state = block.getState();
             TrapDoor trapdoor = (TrapDoor)state.getData();
             trapdoor.setOpen(false);
             state.update();
         } else {
             byte data = block.getData();
             if ((data & 0x8) == 0x8) {
                 block = block.getRelative(BlockFace.DOWN);
                 data = block.getData();
             }
             if (!isDoorClosed(block)) {
                 data = (byte) (data & 0xb);
                 block.setData(data, true);
                 block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 0);
             }
         }
     }
 }
