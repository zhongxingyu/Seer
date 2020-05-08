 package fr.ethilvan.bukkit.superpigmode;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.dynmap.DynmapAPI;
 
 import fr.aumgn.bukkitutils.playerref.map.PlayersRefHashMap;
 import fr.aumgn.bukkitutils.playerref.map.PlayersRefMap;
 import fr.aumgn.bukkitutils.playerref.set.PlayersRefHashSet;
 import fr.aumgn.bukkitutils.playerref.set.PlayersRefSet;
 import fr.aumgn.bukkitutils.util.Util;
 import fr.ethilvan.bukkit.api.EthilVan;
 import fr.ethilvan.bukkit.api.inventories.Inventories;
 
 public class SuperPigMode {
 
     public static enum TurnOffReason {
         Regular,
         Quit,
         Stop,
     }
 
     private PlayersRefSet spPlayers;
     private PlayersRefSet spVanished;
     private PlayersRefMap<SPMPlayerSave> spPlayersSave;
 
     public SuperPigMode() {
         spPlayers = new PlayersRefHashSet();
         spVanished = new PlayersRefHashSet();
         spPlayersSave = new PlayersRefHashMap<SPMPlayerSave>();
     }
 
     private void updateDynmapVisibility(Player player, boolean value) {
         PluginManager pm = Bukkit.getPluginManager();
         if (!pm.isPluginEnabled("dynmap")) {
             return;
         }
 
         Plugin dynmap = pm.getPlugin("dynmap");
         if (!(dynmap instanceof DynmapAPI)) {
             return;
         }
 
         ((DynmapAPI) dynmap).setPlayerVisiblity(player, value);
     }
 
     public List<Player> getSuperPigs() {
         return spPlayers.getPlayers();
     }
 
     public boolean isInSPM(Player player) {
         return spPlayers.contains(player);
     }
 
     public List<Player> getVanished() {
         return spVanished.getPlayers();
     }
 
     private void logConsole(String msg) {
         Bukkit.getConsoleSender().sendMessage(msg);
     }
 
     public boolean toggleSPM(Player player) {
         if (isInSPM(player)) {
             turnOff(player);
             return false;
         } else {
             turnOn(player);
             return true;
         }
     }
 
     public boolean isVanished(Player player) {
         return spVanished.contains(player);
     }
 
     public boolean toggleVanish(Player player) {
         if (isVanished(player)) {
             turnOffVanish(player);
             return false;
         } else {
             turnOnVanish(player);
             return true;
         }
     }
 
     private void turnOn(Player player) {
         Inventories invManager = EthilVan.getInventories();
 
         spPlayers.add(player);
         spPlayersSave.put(player, new SPMPlayerSave(player));
         EthilVan.getAccounts().addPseudoRole(player, "spm");
 
         player.setGameMode(GameMode.CREATIVE);
         player.setSleepingIgnored(true);
         invManager.set(player, "spm." + player.getName());
 
         for (Entity entity : player.getWorld().getEntities()) {
             if (entity instanceof Creature) {
                 Creature creature = (Creature)entity;
                 LivingEntity target = creature.getTarget();
                 if (target instanceof Player && target.equals(player)) {
                     creature.setTarget(null); 
                 }
             }
         }
 
         for (Player vanished : spVanished.players()) {
             player.showPlayer(vanished);
         }
 
         logConsole(ChatColor.GREEN + "SuperPig Mode activé pour "
                 + player.getDisplayName());
     }
 
     public void turnOff(Player player) {
         turnOff(player, TurnOffReason.Regular);
     }
 
     public void turnOff(Player player, TurnOffReason reason) {
        spPlayers.remove(player);
         SPMPlayerSave spPlayerSave = spPlayersSave.remove(player);
 
         spPlayerSave.restore(player);
         player.setSleepingIgnored(false);
         player.setNoDamageTicks(120);
 
         if (isVanished(player)) {
             turnOffVanish(player, reason);
         }
         for (Player vanished : spVanished.players()) {
             vanish(player, vanished);
         }
 
         if (reason != TurnOffReason.Stop) {
             EthilVan.getAccounts().removePseudoRole(player, "spm");
         }
 
         logConsole(ChatColor.GREEN + "SuperPig Mode desactivé pour "
                 + player.getDisplayName());
     }
 
     public void vanish(Player player, Player hidden) {
         if (!player.hasPermission("spm.see-hidden") && !isInSPM(player)) {
             player.hidePlayer(hidden); 
         }
     }
 
     public void turnOnVanish(Player player) {
         updateDynmapVisibility(player, false);
         for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
             vanish(onlinePlayer, player);
         }
         spVanished.add(player);
 
         Util.broadcast(player.getDisplayName() + ChatColor.YELLOW
                 + " a quitté les terres d'Ethil Van.");
 
         logConsole(ChatColor.GREEN + "Vanish activé pour "
                 + player.getDisplayName());
     }
 
     public void turnOffVanish(Player player) {
         turnOffVanish(player, TurnOffReason.Regular);
     }
 
     public void turnOffVanish(Player player, TurnOffReason reason) {
         updateDynmapVisibility(player, true);
         for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
             onlinePlayer.showPlayer(player);
         }
         spVanished.remove(player);
 
         if (reason == TurnOffReason.Regular) {
             Util.broadcast(player.getDisplayName() + ChatColor.YELLOW
                     + " est entré(e) dans le monde d'Ethil Van.");
         }
 
         logConsole(ChatColor.GREEN + "Vanish desactivé pour "
                 + player.getDisplayName());
     }
 }
