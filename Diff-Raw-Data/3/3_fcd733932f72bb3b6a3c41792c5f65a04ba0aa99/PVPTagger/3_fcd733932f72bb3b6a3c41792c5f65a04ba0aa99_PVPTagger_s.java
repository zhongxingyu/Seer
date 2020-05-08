 package com.minecraftserver.pvptoolkit;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 public class PVPTagger implements Listener {
     private PVPToolkit            pvptoolkit;
     private int                   pvpTagDuration;
 
     private HashMap<String, Long> taggedPlayers = new HashMap<>();
 
     private List<String>          pvpTagBlockedCmds;
     public final String           MODULVERSION  = "1.0";
     private boolean               enabled;
 
     public PVPTagger(PVPToolkit toolkit) {
         pvptoolkit = toolkit;
         pvpTagDuration = pvptoolkit.getPvpTagDuration();
         pvpTagBlockedCmds = pvptoolkit.getPvpTagBlockedCmds();
         enabled = true;
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
         if (event.isCancelled() || !enabled) return;
         Player player = event.getPlayer();
         boolean notallowed = false;
         String command = event.getMessage().toLowerCase().substring(1, event.getMessage().length());
         stopTagging(player.getName());
         if (isTagged(player)) {
             for (String cmd : pvpTagBlockedCmds)
                 if (command.toLowerCase().startsWith(cmd)) {
                     notallowed = true;
                     break;
                 }
             if (notallowed) {
                 if (command.toLowerCase().startsWith("fly") && player.getAllowFlight()) return;
                 player.sendMessage(ChatColor.DARK_RED + "/" + command + " is disabled in combat");
                 event.setCancelled(true);
                 return;
             }
         }
     }
 
     private void startTagging(final Player player) {
         taggedPlayers.put(player.getName(), System.currentTimeMillis());
     }
 
     private boolean stopTagging(String playername) {
         long millis = System.currentTimeMillis();
         if (taggedPlayers.containsKey(playername)) {
             if (millis - taggedPlayers.get(playername).longValue() >= (pvpTagDuration * 1000)) {
                 taggedPlayers.remove(playername);
                 return true;
             }
         }
         return false;
     }
 
     private void resetTagging(Player player) {
         if (taggedPlayers.containsKey(player.getName())) {
             stopTagging(player.getName());
             startTagging(player);
         }
 
     }
 
     public boolean isTagged(Player player) {
         stopTagging(player.getName());
         if (taggedPlayers.containsKey(player.getName())) return true;
         return false;
 
     }
 
     public void checkTaggedPlayers() {
        Iterator iterator = taggedPlayers.entrySet().iterator();
         while (iterator.hasNext()) {
             Map.Entry pairs = (Map.Entry) iterator.next();
             String key = (String) pairs.getKey();
             Player player = pvptoolkit.getServer().getPlayer(key);
             if (stopTagging(key) && player != null)
                 player.sendMessage(ChatColor.GOLD + "You are no longer tagged");
         }
 
     }
 
     @EventHandler
     public void onPlayerDamage(EntityDamageEvent event) {
         if (event.isCancelled() || (event.getDamage() == 0) || !enabled) {
             return;
         }
         if (event instanceof EntityDamageByEntityEvent) {
             EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
             Entity dmgr = e.getDamager();
             if (dmgr instanceof Projectile) {
                 dmgr = ((Projectile) dmgr).getShooter();
             }
             if ((dmgr instanceof Player) && (e.getEntity() instanceof Player)) {
                 Player damager = (Player) dmgr;
                 Player receiver = (Player) e.getEntity();
                 if (!damager.getAllowFlight()) {
                     if (!damager.hasPermission("pvptoolkit.blocker.nottagable") && !damager.isOp()
                             && !damager.hasPermission("pvptoolkit.admin")) if (isTagged(damager)) {
                         resetTagging(damager);
                     } else startTagging(damager);
                     if (!receiver.hasPermission("pvptoolkit.blocker.nottagable")
                             && !receiver.isOp() && !receiver.hasPermission("pvptoolkit.admin"))
                         if (isTagged(receiver)) {
                             resetTagging(receiver);
                         } else startTagging(receiver);
                 }
             }
         }
 
     }
 
     public void disable() {
         enabled = false;
     }
 }
