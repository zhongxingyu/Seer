 package com.lol768.LiteKits.extensions.signs;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.lol768.LiteKits.LiteKits;
 
 public class Signs extends JavaPlugin implements Listener {
     private LiteKits lk;
 
     public void onEnable() {
         Object obj = Bukkit.getServer().getPluginManager().getPlugin("LiteKits");
         if (obj != null) {
             lk = (LiteKits) obj;
             if (lk.getDescription().getVersion().equals("1.0")) {
                 getLogger().severe("LiteKits version is too old to use this extension. Disabling self...");
                 setEnabled(false);
             }
         } else {
             getLogger().severe("Couldn't find LiteKits. Disabling self...");
             setEnabled(false);
         }
         getServer().getPluginManager().registerEvents(this, this);
     }
     
     public void onDisable() {
         getConfig().options().header("Auto-generated. Do not edit.");
         saveConfig();
     }
     
     @EventHandler
     public void onBlockBreak(BlockBreakEvent e) {
         if ( e.getBlock().getType() == Material.SIGN_POST || e.getBlock().getType() == Material.WALL_SIGN) {
             String locKey = e.getBlock().getX() + "-" + e.getBlock().getY() + "-" + e.getBlock().getZ() + "-" + e.getBlock().getWorld().getName();
             if (getConfig().contains("signs." + locKey)) {
                 if (!e.getPlayer().hasPermission("LiteKits.extension.signs.delete")) {
                     e.getPlayer().sendMessage(lk.getBrand(true) + ChatColor.RED + "You need LiteKits.extension.signs.delete to remove a kit sign.");
                     e.setCancelled(true);
                 } else {
                     getConfig().set("signs." + locKey, null);
                     e.getPlayer().sendMessage(lk.getBrand(true) + ChatColor.GREEN + "Kit sign successfully removed.");
                 }
             }
         }
     }
     
     @EventHandler
     public void onSignUse(PlayerInteractEvent e) {
         if ( e.getClickedBlock().getType() == Material.SIGN_POST || e.getClickedBlock().getType() == Material.WALL_SIGN) {
             String locKey = e.getClickedBlock().getX() + "-" + e.getClickedBlock().getY() + "-" + e.getClickedBlock().getZ() + "-" + e.getClickedBlock().getWorld().getName();
             if (getConfig().contains("signs." + locKey)) {
                 if (!e.getPlayer().hasPermission("LiteKits.extension.signs.use")) {
                     e.getPlayer().sendMessage(lk.getBrand(true) + ChatColor.RED + "You need LiteKits.extension.signs.use to use a kit sign.");
                     
                 }
                 
                 if (getConfig().getString("signs." + locKey + ".kit") == null || !lk.kitExists(getConfig().getString("signs." + locKey + ".kit"))) {
                     e.getPlayer().sendMessage(lk.getBrand(true) + ChatColor.RED + "Kit does not exist.");
                     return;
                 } else {
                     lk.supplyKitToPlayer(getConfig().getString("signs." + locKey + ".kit"), e.getPlayer());
                    e.getPlayer().sendMessage(lk.getBrand(true) + ChatColor.GREEN + "Debug: gievn");
                 }
             }
         }
     }
     
     @EventHandler
     public void onSignCreate(SignChangeEvent e) {
         if (e.getLine(0) != null && e.getLine(0).equals(ChatColor.stripColor(lk.getBrand(false)))) {
             if (!e.getPlayer().hasPermission("LiteKits.extension.signs.create")) {
                 e.getBlock().breakNaturally();
                 e.getPlayer().sendMessage(lk.getBrand(true) + ChatColor.RED + "You need LiteKits.extension.signs.create to make a kit sign.");
             } else {
                 if (e.getLine(1) == null ||!lk.kitExists(e.getLine(1))) {
                     e.getBlock().breakNaturally();
                     e.getPlayer().sendMessage(lk.getBrand(true) + ChatColor.RED + "You must supply a valid kit on line 2.");
                 } else {
                     
                     //Save sign
                     String locKey = e.getBlock().getX() + "-" + e.getBlock().getY() + "-" + e.getBlock().getZ() + "-" + e.getBlock().getWorld().getName();
                     e.getPlayer().sendMessage(lk.getBrand(true) + ChatColor.GREEN + "Created kit sign successfully.");
                     getConfig().set("signs." + locKey + ".kit", e.getLine(1));
                     
                 }
             }
         }
     }
 
 }
