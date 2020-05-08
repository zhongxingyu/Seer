 package me.limebyte.battlenight.core;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.util.Vector;
 
 public class Util {
 
     public static void prepare(Player player, Location dest) {
         PlayerData.store(player);
         player.teleport(dest, TeleportCause.PLUGIN);
         reset(player);
     }
 
     public static void restore(Player player) {
         PlayerData.restore(player, false);
     }
 
     private static void reset(Player player) {
         for (Player p : Bukkit.getServer().getOnlinePlayers()) {
             if (!player.canSee(p)) {
                 player.showPlayer(p);
             }
         }
 
         for (PotionEffect effect : player.getActivePotionEffects()) {
             player.addPotionEffect(new PotionEffect(effect.getType(), 0, 0), true);
         }
 
         player.setFlying(false);
         player.setAllowFlight(false);
         player.getEnderChest().clear();
         player.setExhaustion(0);
         player.setExp(0);
         player.setFallDistance(0);
         player.setFireTicks(0);
         player.setFoodLevel(20);
         player.setGameMode(GameMode.SURVIVAL);
         player.setHealth(player.getMaxHealth());
         player.getInventory().clear();
         player.getInventory().setArmorContents(new ItemStack[player.getInventory().getArmorContents().length]);
         player.setLevel(0);
         String pListName = ChatColor.GRAY + "[BN] " + player.getName();
         player.setPlayerListName(pListName.length() < 16 ? pListName : pListName.substring(0, 16));
         player.resetPlayerTime();
         player.setRemainingAir(player.getMaximumAir());
         player.setSaturation(20);
         player.setTicksLived(1);
         player.setVelocity(new Vector());
         player.setWalkSpeed(1);
         player.setSleepingIgnored(true);
         player.setSneaking(false);
         player.setSprinting(false);
     }
 
 }
