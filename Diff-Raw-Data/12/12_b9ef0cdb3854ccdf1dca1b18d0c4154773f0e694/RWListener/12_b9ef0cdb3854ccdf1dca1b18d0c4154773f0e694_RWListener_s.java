 package com.ACStache.RangedWolves;
 
 import java.util.HashSet;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Creeper;
 import org.bukkit.entity.Egg;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Fireball;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.SmallFireball;
 import org.bukkit.entity.Snowball;
 import org.bukkit.entity.ThrownPotion;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityTameEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 import com.garbagemule.MobArena.framework.Arena;
 import com.garbagemule.MobArena.MobArena;
 import com.garbagemule.MobArena.events.ArenaEndEvent;
 import com.garbagemule.MobArena.events.ArenaPlayerDeathEvent;
 import com.garbagemule.MobArena.events.ArenaPlayerLeaveEvent;
 import com.garbagemule.MobArena.events.ArenaStartEvent;
 
 public class RWListener implements Listener
 {
     private RangedWolves plugin;
     private MobArena mobArena;
     private HashSet<World> worlds = new HashSet<World>();
 
     public RWListener(RangedWolves rangedWolves)
     {
         plugin = rangedWolves;
     }
 
     /*
      * Entity Events
      */
     @EventHandler
     public void onEntityDamage(EntityDamageEvent event)
     {
         if(event.isCancelled()) {return;}
         
         Entity target = event.getEntity();
         if(!(target instanceof LivingEntity)) {return;}
 
         LivingEntity newTarget = (LivingEntity)target;
         
         DamageCause damager = target.getLastDamageCause().getCause();
         if(damager != DamageCause.PROJECTILE) {return;}
         
         Entity cause = ((EntityDamageByEntityEvent)event).getDamager();
         if(!(cause instanceof Projectile)) {return;}
         
         Projectile proj = (Projectile)cause;
         if(!(proj.getShooter() instanceof Player)) {return;}
     
         if(proj instanceof Arrow && !RWConfig.RWProj("Arrow")) {return;}
         if(proj instanceof Egg && !RWConfig.RWProj("Egg")) {return;}
         if(proj instanceof Fireball && !RWConfig.RWProj("Fireball")) {return;}
         if(proj instanceof SmallFireball && !RWConfig.RWProj("Small-Fireball")) {return;}
         if(proj instanceof Snowball && !RWConfig.RWProj("Snowball")) {return;}
         if(proj instanceof ThrownPotion && !RWConfig.RWProj("Potions")) {return;}
         
         Player player = (Player)proj.getShooter();
 
         if(newTarget instanceof Creeper && !RWConfig.RWCreepers())
         {
             if(!player.hasPermission("RangedWolves.Creep")) {return;}
         }
         
         if(RWArenaChecker.isPlayerInArena(player))
         {
             if(!player.hasPermission("RangedWolves.Arenas")) {return;}
             
             Arena arena = RangedWolves.am.getArenaWithPlayer(player);
             boolean arenaPvP = arena.getSettings().getBoolean("pvp-enabled");
             HashSet<Wolf> pets = (HashSet<Wolf>)RWOwner.getPets(player);
             
             if(!RWConfig.RWinArena(arena)) {return;}
             
             if(newTarget instanceof Wolf)
             {
                 Wolf wolf = (Wolf)newTarget;
                 
                 if(RWOwner.checkArenaWolf(wolf))
                 {
                     if(player == (Player)wolf.getOwner())
                     {
                         event.setCancelled(true);
                     }
                     else
                     {
                         if(arenaPvP)
                         {
                             //RWTargetting.addArenaTarget(pets, newTarget, player);
                             setArenaTarget(pets, newTarget);
                         }
                         else
                         {
                             event.setCancelled(true);
                         }
                     }
                 }
                 else
                 {
                     //RWTargetting.addArenaTarget(pets, newTarget, player);
                     setArenaTarget(pets, newTarget);
                 }
             }
             else if(newTarget instanceof Player)
             {
                 if(!(player == (Player)newTarget))
                 {
                     if(arenaPvP)
                     {
                         //RWTargetting.addArenaTarget(pets, newTarget, player);
                         setArenaTarget(pets, newTarget);
                     }
                     else
                     {
                         event.setCancelled(true);
                     }
                 }
             }
             else
             {
                 //RWTargetting.addArenaTarget(pets, newTarget, player);
                 setArenaTarget(pets, newTarget);
             }
         }
         else
         {
             if(!player.hasPermission("RangedWolves.Worlds")) {return;}
             
             World world = player.getWorld();
             HashSet<Wolf> pets = (HashSet<Wolf>)RWOwner.getPets(player);
             boolean worldPvP = world.getPVP();
             
             if(!RWConfig.RWinWorld(world)) {return;}
             
             if(newTarget instanceof Wolf)
             {
                 Wolf wolf = (Wolf)newTarget;
                 
                 if(RWOwner.checkWorldWolf(wolf))
                 {
                     if(worldPvP)
                     {
                         if(wolf.getOwner() instanceof Player)
                         {
                             if(player.equals((Player)wolf.getOwner()))
                             {
                                 event.setCancelled(true);
                             }
                             else
                             {
                                 //RWTargetting.addWorldTarget(pets, newTarget, player);
                                 setWorldTarget(pets, newTarget);
                             }
                         }
                         else
                         {
                             //RWTargetting.addWorldTarget(pets, newTarget, player);
                             setWorldTarget(pets, newTarget);
                         }
                     }
                     else
                     {
                         event.setCancelled(true);
                     }
                 }
                 else
                 {
                     //RWTargetting.addWorldTarget(pets, newTarget, player);
                     setWorldTarget(pets, newTarget);
                 }
             }
             else if(newTarget instanceof Player)
             {
                 if(!(player == (Player)newTarget))
                 {
                     if(worldPvP)
                     {
                         //RWTargetting.addWorldTarget(pets, newTarget, player);
                         setWorldTarget(pets, newTarget);
                     }
                 }
             }
             else
             {
                 //RWTargetting.addWorldTarget(pets, newTarget, player);
                 setWorldTarget(pets, newTarget);
             }
         }
     }
     
     @EventHandler
     public void onEntityDeath(EntityDeathEvent event)
     {
         if(!(event.getEntity() instanceof LivingEntity)) {return;}
         
         LivingEntity dead = (LivingEntity)event.getEntity();
         if(dead instanceof Wolf)
         {
             Wolf wolf = (Wolf)dead;
             if(mobArena != null && mobArena.isEnabled())
             {
                 if(RangedWolves.am.getArenaWithPet(wolf) != null) {return;}
             }
             if(!RWOwner.checkWorldWolf(wolf)) {return;}
             
             if(wolf.getOwner() instanceof OfflinePlayer)
             {
                 OfflinePlayer offPlayer = (OfflinePlayer)wolf.getOwner();
                 if(offPlayer != null)
                 {
                     RWOwner.removeWolf(offPlayer.getName(), wolf);
                 }
             }
             else
             {
                 Player owner = (Player)wolf.getOwner();
                 if(owner != null)
                 {
                     RWOwner.removeWolf(owner.getName(), wolf);
                     owner.sendMessage(ChatColor.AQUA + "RW: You've lost a wolf");
                 }
             }
         }
         
         RWTargetting.removeTarget(dead);
     }
     
     @EventHandler
     public void onEntityTame(EntityTameEvent event)
     {
         Entity pet = event.getEntity();
         if(event.getOwner() instanceof Player)
         {
             Player owner = (Player)event.getOwner();
             if(pet instanceof Wolf)
             {
                 if(RWOwner.getPetAmount(owner) >= RWConfig.RWMaxWolves())
                 {
                     if(!owner.hasPermission("RangedWolves.Unlimited"))
                     {
                         owner.sendMessage(ChatColor.AQUA + "RW: You don't have permission to have more than " + RWConfig.RWMaxWolves() + " wolves");
                        return;
                     }
                 }
                 Wolf wolf = (Wolf)pet;
                 RWOwner.addWolf(owner.getName(), wolf);
                 owner.sendMessage(ChatColor.AQUA + "RW: You've tamed a wolf");
             }
         }
     }
     
     @EventHandler
     public void onCreatureSpawn(CreatureSpawnEvent event)
     {
         Entity spawn = event.getEntity();
         if(spawn instanceof Wolf)
         {
             Wolf wolf = (Wolf)spawn;
             if(mobArena != null && mobArena.isEnabled())
             {
                 if(RangedWolves.am.getArenaWithPet(wolf) != null) {return;}
             }
             if(wolf.isTamed())
             {
                 Player owner = (Player)wolf.getOwner();
                 if(owner != null)
                 {
                     if(RWOwner.checkWorldWolf(wolf)) {return;}
                     
                     if(RWOwner.getPetAmount(owner) >= RWConfig.RWMaxWolves())
                     {
                        if(!owner.hasPermission("RangedWolves.Unlimited")) {return;}
                     }
                     RWOwner.addWolf(owner.getName(), wolf);
                 }
             }
         }
     }
     
     @EventHandler
     public void onEntityTarget(EntityTargetEvent event)
     {
         if(!(event.getEntity() instanceof Wolf)) {return;}
         Wolf w = (Wolf)event.getEntity();
         
         if(!(RWOwner.checkWorldWolf(w)) || !(RWOwner.checkArenaWolf(w))) {return;}
         System.out.println("wolf changed target");
         RWTargetting.Target((HashSet<Wolf>)RWOwner.getPets((Player)w.getOwner()), (Player)w.getOwner());
     }
     
     
     /*
      * Player Events
      */
     @EventHandler
     public void onPlayerDeath(PlayerDeathEvent event)
     {
         RWTargetting.clearTargets(event.getEntity());
     }
     
     @EventHandler
     public void onPlayerTeleport(PlayerTeleportEvent event)
     {
         World world = event.getPlayer().getWorld();
         if(worlds.contains(world)) {return;}
 
         for(LivingEntity e : world.getLivingEntities())
         {
             if(e instanceof Wolf)
             {
                 Wolf wolf = (Wolf)e;
                 if(wolf.getOwner() instanceof OfflinePlayer)
                 {
                     OfflinePlayer offPlayer = (OfflinePlayer)wolf.getOwner();
                     if(offPlayer != null)
                     {
                         RWOwner.addWolf(offPlayer.getName(), wolf);
                     }
                 }
                 else
                 {
                     Player owner = (Player)wolf.getOwner();
                     if(owner != null)
                     {
                         if(RWOwner.getPetAmount(owner) >= RWConfig.RWMaxWolves())
                         {
                            if(!owner.hasPermission("RangedWolves.Unlimited")) {return;}
                         }
                      
                         RWOwner.addWolf(owner.getName(), wolf);
                     }
                 }
             }
         }
         
         worlds.add(world);
     }
     
     
     /*
      * MobArena Events
      */
     @EventHandler
     public void onArenaStart(ArenaStartEvent event)
     {
         final Arena arena = event.getArena();
         Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
         {
             public void run()
             {
                 HashSet<Wolf> pets = (HashSet<Wolf>)arena.getMonsterManager().getPets();
                 if(!(pets == null))
                     for (Wolf w : pets)
                         RWOwner.addWolf(arena, ((Player)w.getOwner()).getName(), w);
             }
         }, 20);
     }
     
     @EventHandler
     public void onArenaEnd(ArenaEndEvent event)
     {
         RWOwner.clearWolves(event.getArena());
     }
     
     @EventHandler
     public void onArenaPlayerDeath(ArenaPlayerDeathEvent event)
     {
         RWTargetting.clearArenaTargets(event.getPlayer());
     }
     
     @EventHandler
     public void onArenaPlayerLeave(ArenaPlayerLeaveEvent event)
     {
         RWTargetting.clearArenaTargets(event.getPlayer());
     }
     
     
     /*
      * Extra methods for targetting purposes
      */
     private void setArenaTarget(HashSet<Wolf> pets, LivingEntity target)
     {
         if(pets == null) {return;}
         for(Wolf w : pets)
         {
             if(w.isSitting())
             {
                 w.setSitting(false);
             }
             if(w.getTarget() == null)
             {
                 w.setTarget(target);
             }
         }
     }
     
     private void setWorldTarget(HashSet<Wolf> pets, LivingEntity target)
     {
         if(pets == null) {return;}
         for(Wolf w : pets)
         {
             if(!w.isSitting() && w.getTarget() == null)
             {
                 w.setTarget(target);
             }
         }
     }
 }
