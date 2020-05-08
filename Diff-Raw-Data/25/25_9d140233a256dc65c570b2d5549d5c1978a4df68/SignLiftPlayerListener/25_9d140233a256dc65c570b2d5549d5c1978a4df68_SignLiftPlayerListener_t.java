 package com.minecraftserver.improvedsignlifts;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.material.Button;
 
 public class SignLiftPlayerListener implements Listener {
     static ImprovedSignLift plugin;
 
     public SignLiftPlayerListener(ImprovedSignLift parent) {
         plugin = parent;
     }
 
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (!event.hasBlock()) return;
        plugin.remSignEditStatus(event.getPlayer());
         if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
             if (event.getClickedBlock().getType() == Material.STONE_BUTTON
                     || event.getClickedBlock().getType() == Material.WOOD_BUTTON) {
                 Block block = event.getClickedBlock();
                 byte data = block.getData();
                 Button btn = new Button(event.getClickedBlock().getType(), data);
                 BlockFace face = btn.getAttachedFace();
                 if (face == null) return;
                 block = block.getRelative(face, 2);
                 if (plugin.isBlockSignLift(block))
                     plugin.executeSignLiftAction((Sign) block.getState(), event.getPlayer());
             } else if (event.getClickedBlock().getType() == Material.SIGN
                     || event.getClickedBlock().getType() == Material.WALL_SIGN) {
                 Block block = event.getClickedBlock();
                 if (plugin.isBlockSignLift(block)) {
                     Sign sign = (Sign) block.getState();
                     if (!event.getPlayer().isSneaking())
                         plugin.executeSignLiftAction(sign, event.getPlayer());
                     else {
                         Player player = event.getPlayer();
                         if (plugin.isSignLiftPrivate(block)
                                 && (plugin.shortPlayerName(player.getName()).equals(
                                         sign.getLine(3).toString()) && player
                                         .hasPermission("signlift.user.modify.private.member"))
                                 || (player.hasPermission("signlift.admin") || player.isOp())) {
                             // TODO
                             player.sendMessage(ChatColor.BLUE + "Sign Lift Edit Mode");
                             List<String> memberList = LiftDataManager.getMembersOfLift(
                                     sign.getLocation(), player.getName());
                             // List to string
                             String members = "";
                             if (memberList != null) for (String s : memberList)
                                 members += " " + s;
                             player.sendMessage(ChatColor.BLUE + "Currently allowed players:"
                                     + ChatColor.GOLD + members);
                             player.sendMessage(ChatColor.BLUE + "Use" + ChatColor.GOLD + " /sl add"
                                     + ChatColor.BLUE + " or" + ChatColor.GOLD + " /sl remove"
                                     + ChatColor.BLUE + " to modify the members");
                             plugin.addSignEditStatus(player, sign);
                         } else player.sendMessage(ChatColor.RED
                                 + "You dont have permission to edit this lift");
                     }
                 }
             }
         }
     }
 }
