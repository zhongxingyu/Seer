 package org.BrokenWorlds.Water;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Biome;
 import org.bukkit.craftbukkit.inventory.CraftItemStack;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import com.google.common.collect.Lists;
 
 public class CleansingWater implements Listener {
 
     private List<String> cleanCoolDown = Lists.newArrayList();
 
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
 
         final Player player = event.getPlayer();
         Action action = event.getAction();
 
         if (!action.equals(Action.LEFT_CLICK_AIR) && !action.equals(Action.LEFT_CLICK_BLOCK))
             return;
 
         ItemStack item = player.getItemInHand();
         String sItem = item.getType().name();
         if (sItem.equals("WRITTEN_BOOK")) {
             String title = ((CraftItemStack) item).getHandle().tag.getString("title");
             if (title.equals("Cleansing Waters"))
                 cleanPlayer(event.getPlayer());
         }
     }
 
     @EventHandler
     public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
         if (event.getEntity() instanceof Player) {
            Player damagee = (Player) event.getEntity();
            Player damager = (Player) event.getDamager();
            ItemStack item = damager.getItemInHand();
            String sItem = item.getType().name();
            if (sItem.equals("WRITTEN_BOOK")) {
                String title = ((CraftItemStack) item).getHandle().tag.getString("title");
                if (title.equals("Cleansing Waters")) {
                    cleanPlayer(damagee);
                    event.setCancelled(true);
                 }
             }
         }
     }
 
     public void cleanPlayer(final Player player) {
         if (!cleanCoolDown.contains(player.getName())) {
             if (player.getLocation().getBlock().isLiquid() || inStorm(player)) {
                 int health = player.getHealth();
                 if (health + 6 > 20)
                     player.setHealth(20);
                 else
                     player.setHealth(health + 6);
                 Collection<PotionEffect> effects = player.getActivePotionEffects();
                 Iterator<PotionEffect> it = effects.iterator();
                 while (it.hasNext()) {
                     PotionEffect potionEffect = it.next();
                     PotionEffectType effect = potionEffect.getType();
                     if (effect.equals(PotionEffectType.BLINDNESS)) {
                         player.removePotionEffect(PotionEffectType.BLINDNESS);
                         player.sendMessage(ChatColor.GREEN + "Potion of " + ChatColor.YELLOW + "Blindness" + ChatColor.GREEN + " removed!");
                     } else if (effect.equals(PotionEffectType.CONFUSION)) {
                         player.removePotionEffect(PotionEffectType.CONFUSION);
                         player.sendMessage(ChatColor.GREEN + "Potion of " + ChatColor.YELLOW + "Confusion" + ChatColor.GREEN + " removed!");
                     } else if (effect.equals(PotionEffectType.POISON)) {
                         player.removePotionEffect(PotionEffectType.POISON);
                         player.sendMessage(ChatColor.GREEN + "Potion of " + ChatColor.YELLOW + "Poison" + ChatColor.GREEN + " removed!");
                     } else if (effect.equals(PotionEffectType.WEAKNESS)) {
                         player.removePotionEffect(PotionEffectType.WEAKNESS);
                         player.sendMessage(ChatColor.GREEN + "Potion of " + ChatColor.YELLOW + "Weakness" + ChatColor.GREEN + " removed!");
                     } else if (effect.equals(PotionEffectType.SLOW)) {
                         player.removePotionEffect(PotionEffectType.SLOW);
                         player.sendMessage(ChatColor.GREEN + "Potion of " + ChatColor.YELLOW + "Slowness" + ChatColor.GREEN + " removed!");
                     }
                     Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(Bukkit.getPluginManager().getPlugin("SkillTesting"), new Runnable() {
 
                         public void run() {
                             removePlayer(player);
                         }
                     }, 300L);
                 }
             } else
                 player.sendMessage(ChatColor.YELLOW + "Cleansing Waters" + ChatColor.GREEN + " can only be casted in water!");
         } else
             player.sendMessage(ChatColor.YELLOW + "Cleansing Waters" + ChatColor.GREEN + " is on cooldown!");
     }
 
     public void addPlayer(Player player) {
         cleanCoolDown.add(player.getName());
         player.sendMessage(ChatColor.GREEN + "-- " + ChatColor.YELLOW + " Cleansing Waters " + ChatColor.YELLOW + " successfully casted!");
 
     }
 
     public void removePlayer(Player player) {
         cleanCoolDown.remove(player.getName());
         player.sendMessage(ChatColor.GREEN + "--" + ChatColor.YELLOW + " Cleaning Waters" + ChatColor.GREEN + " cooldown has reset --");
     }
 
     public boolean inStorm(Player player) {
         World world = player.getWorld();
 
         if (world.hasStorm()) {
             Location loc = player.getLocation();
             Biome biome = world.getBiome(loc.getBlockX(), loc.getBlockZ());
 
             if (biome != Biome.DESERT && biome != Biome.DESERT_HILLS && world.getHighestBlockYAt(loc) <= loc.getBlockY()) {
                 return true;
             } else
                 return false;
         } else
             return false;
     }
 }
