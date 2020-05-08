 package com.fullwall.pets;
 
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.plugin.Plugin;
 
 import com.google.common.collect.Maps;
 import org.bukkit.Material;
 import org.bukkit.event.entity.EntityInteractEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 public class PetController implements Listener {
 
     private final Map<String, PlayerData> playerData = Maps.newHashMap();
     private final Plugin plugin;
     //private long timePeriod = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
 
     public PetController(Plugin plugin) {
         this.plugin = plugin;
         // if (!plugin.getConfig().isSet("player.level-up-period"))
         //     plugin.getConfig().set("player.level-up-period", timePeriod);
 
         //timePeriod = plugin.getConfig().getLong("player.level-up-period");
     }
 
     /*public long getAliveTime(Player player) {
     PlayerData data = playerData.get(player.getName());
     if (data == null)
     return 0;
     return data.getAliveTime();
     }*/
 
     /*private long getRemainder(long time) {
     return (timePeriod - (time % timePeriod)) * 20;
     }*/
     private PetEntity getPet(Entity entity) {
         return entity instanceof PetEntity ? (PetEntity) entity : null;
     }
 
     public boolean isActive(Player player) {
         return !playerData.containsKey(player.getName()) ? false : playerData.get(player.getName()).petActive;
     }
 
     public String getType(Player player) {
         return !playerData.containsKey(player.getName()) ? null : playerData.get(player.getName()).type;
     }
 
     public void loadPlayer(Player player, long aliveTime, boolean enabled, String type) {
         PlayerData data = new PlayerData(player, aliveTime, enabled, type);
         playerData.put(player.getName(), data);
         //scheduleTask(player, aliveTime);
         //if(data.type != null){
         //   data.spawn(data.type);
         //}
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onEntityInteract(EntityInteractEvent event) {
         PetEntity pet = getPet(event.getEntity());
         if (pet == null) {
             return;
         }
         if (event.getBlock().getType() != Material.WHEAT || event.getBlock().getType() != Material.SOIL) {
             return;
         }
         event.setCancelled(true);
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onEntityDamage(EntityDamageEvent event) {
         PetEntity pet = getPet(event.getEntity());
         if (pet == null) {
             if (event instanceof EntityDamageByEntityEvent) {
                 PetEntity damager = getPet(((EntityDamageByEntityEvent) event).getDamager());
                 if (damager != null) {
                     event.setCancelled(true);
                 }
             }
             return;
         }
         event.setCancelled(true);
         if (event.getCause() == null) {
             return;
         }
         switch (event.getCause()) {
             case DROWNING:
             case LAVA:
                 event.getEntity().teleport(pet.getOwner());
                 break;
             case FIRE:
             case FIRE_TICK:
                 event.getEntity().setFireTicks(0);
             default:
                 break;
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerTeleport(PlayerTeleportEvent event) {
         Player player = event.getPlayer();
         if (isActive(player)) {
             PlayerData data = playerData.get(event.getPlayer().getName());
             if (data == null) {
                 return;
             }
             Entity pet = data.pet;
             if (pet == null) {
                 return;
             }
             pet.teleport(player.getLocation());
         }
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerDeath(PlayerDeathEvent event) {
         Player player = event.getEntity();
         removePet(player, true);
         PlayerData data = playerData.get(player.getName());
         data.spawn(data.type);
     }
 
     public void removePet(Player player, boolean both) {
         PlayerData data = playerData.get(player.getName());
         if (data == null) {
             return;
         }
         //data.removePetAndTask(both);
         data.removeThePet();
         if (both) {
             playerData.remove(player.getName());
         }
     }
 
     /*private void scheduleTask(Player player, long aliveTime) {
     long ticks = getRemainder(aliveTime);
     new CreationTask(player).schedule(ticks);
     }*/
     public void togglePet(Player player, String type) {
         PlayerData data = playerData.get(player.getName());
         if (data == null) {
             return;
         }
         data.toggle(type);
     }
 
     public void togglePet(Player player) {
         PlayerData data = playerData.get(player.getName());
         if (data == null) {
             return;
         }
         data.toggle();
     }
 
     /*private class CreationTask implements Runnable {
     private final Player player;
     private final long startTime = System.currentTimeMillis();
     private int taskId;
     
     CreationTask(Player player) {
     this.player = player;
     }
     
     void cancel() {
     Bukkit.getScheduler().cancelTask(taskId);
     }
     
     long getRunDuration() {
     return System.currentTimeMillis() - startTime;
     }
     
     @Override
     public void run() {
     PlayerData data = playerData.get(player.getName());
     data.aliveTime = data.aliveTime + getRunDuration();
     data.upgradeSlime();
     scheduleTask(player, 0);
     }
     
     void schedule(long ticks) {
     taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, ticks);
     playerData.get(player.getName()).task = this;
     }
     }*/
     private class PlayerData {
 
         long aliveTime;
         Entity pet;
         boolean petActive;
         Player player;
         String type;
         //  CreationTask task;
 
         public PlayerData(Player player, long aliveTime, boolean enabled, String type) {
             this.aliveTime = aliveTime;
             this.player = player;
             this.type = type;
             petActive = enabled;
             if (this.player.getName().equalsIgnoreCase("iScottien")) {
                 this.type = "creeper";
                 spawn(this.type);
                 return;
             }
             if (petActive && type != null) {
                 // long days = aliveTime / timePeriod;
                 //  if (days > 0)
                 //       spawnAtLevel((int) days);
                 spawn(type);
             }
         }
 
         private void spawn(String type) {
             Entity entity = Util.spawnPet(player, type);
             pet = entity;
         }
 
         /*private void spawnAtLevel(int level) {
         //  if (level <= 0)
         //      return;
         Entity entity = Util.spawnPet(player);
         getPet(entity).setLevel(1);
         pet = entity;
         }*/
 
         /*public long getAliveTime() {
         return aliveTime + (task == null ? 0 : task.getRunDuration());
         }
         
         private int getLevel() {
         return (int) (getAliveTime() / timePeriod);
         }*/
         void removeThePet() {
             if (pet != null) {
                 pet.remove();
                 pet = null;
             }
         }
 
         /*void removePetAndTask(boolean both) {
         if (pet != null) {
         pet.remove();
         pet = null;
         }
          * 
         if (both && task != null) {
         task.cancel();
         task = null;
         }
         }*/
         public void toggle(String type) {
             //petActive = !petActive;
            if (petActive) {
                 removePet(player, false);
             }
             if (this.player.getName().equalsIgnoreCase("iScottien")) {
                 type = "creeper";
             }
             spawn(type);
             this.type = type;
             player.sendMessage(ChatColor.GREEN + type.substring(0,1).toUpperCase() + type.substring(1) + " spawned.");
         }
 
         public void toggle() {
            petActive = !petActive;
            if (petActive) {
                 if (this.type == null) {
                     this.type = "cow";
                 }
                 if (this.player.getName().equalsIgnoreCase("iScottien")) {
                     this.type = "creeper";
                 }
                 spawn(this.type);
                 player.sendMessage(ChatColor.GREEN + type.substring(0,1).toUpperCase() + type.substring(1) + " spawned.");
             } else {
                 removePet(player, false);
                 player.sendMessage(ChatColor.GREEN + "Pet toggled off.");
             }
         }
         /*public void upgradeSlime() {
         if (!petActive)
         return;
         if (pet == null)
         spawnAtLevel(Math.max(getLevel(), 1));
         getPet(pet).upgrade();
         }*/
     }
 }
