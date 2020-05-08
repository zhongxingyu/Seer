 package com.entrocorp.linearlogic.consent2combat;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 
 public class C2CListener implements Listener {
 
     private Consent2Combat plugin;
     private List<Pair<Player, Player>> duelers;
     private HashMap<Player, HashSet<Player>> pending;
 
     public C2CListener(Consent2Combat instance) {
         plugin = instance;
         duelers = new ArrayList<Pair<Player, Player>>();
         pending = new HashMap<Player, HashSet<Player>>();
     }
 
     @EventHandler
     public void onHandshake(PlayerInteractEntityEvent event) {
         if (!(event.getRightClicked() instanceof Player))
             return;
         Player clicker = event.getPlayer(), clicked = (Player) event.getRightClicked();
         if (pending.containsKey(clicker)) {
             HashSet<Player> requesters = pending.get(clicker);
             if (requesters.remove(clicked)) { // Handshake complete
                 duelers.add(getAlphabetizedPair(clicker, clicked));
                 clicker.sendMessage(plugin.getPrefix() + "You are now dueling with " + ChatColor.RED + clicked.getName() + "!");
                 clicked.sendMessage(plugin.getPrefix() + "You are now dueling with " + ChatColor.RED + clicker.getName() + "!");
                 if (plugin.isVerbose())
                     plugin.getLogger().info(clicker.getName() + " is now dueling with " + clicked.getName());
                 if (requesters.size() == 0)
                     pending.remove(clicker);
                 return;
             }
         }
         if (!pending.containsKey(clicked))
             pending.put(clicked, new HashSet<Player>());
         if (pending.get(clicked).add(clicker))
             clicked.sendMessage(plugin.getPrefix() + ChatColor.RED + clicker.getName() + ChatColor.GRAY + " wants to duel. " +
                     "Right-click the player to accept.");
     }
 
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onPvP(EntityDamageByEntityEvent event) {
         if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player))
             return;
         Player attacker = (Player) event.getDamager(), defender = (Player) event.getEntity();
         Pair<Player, Player> duelerPair = getAlphabetizedPair(attacker, defender);
         if (!duelers.contains(duelerPair)) {
             event.setCancelled(true);
             return;
         }
         if (defender.getHealth() - event.getDamage() <= 0.0 && plugin.cancelDuelsOnDeath())
             clearDuels(defender);
     }
 
     private void clearDuels(Player player) {
         List<Pair<Player, Player>> matches = new ArrayList<Pair<Player, Player>>();
         for (Pair<Player, Player> pair : duelers)
             if (player == pair.getX() || player == pair.getY())
                 matches.add(pair);
         pending.remove(player);
         for (HashSet<Player> requesters : pending.values())
             requesters.remove(player);
     }
 
     private Pair<Player, Player> getAlphabetizedPair(Player p1, Player p2) {
         if (p1 == null || p2 == null || p1 == p2)
             return null;
         return p1.getName().compareTo(p2.getName()) < 0 ? new Pair<Player, Player>(p1, p2) : new Pair<Player, Player>(p2, p1);
     }
 }
