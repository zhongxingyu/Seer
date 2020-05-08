 package com.coffeejawa.Snowball;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 public class SnowballListener implements Listener{
     Snowballs plugin;
     Set<Entity> frozenEntities;
     
     public SnowballListener(Snowballs plugin){
         this.plugin = plugin;
         frozenEntities = new HashSet<Entity>();
     }
     
     @EventHandler
     public void OnDamageByEntityEvent(EntityDamageByEntityEvent event){
         // bail out if not enabled
         if(!plugin.getConfig().getBoolean("enabled")){
             return;
         }
         
         if(event.getDamager().getType() == EntityType.SNOWBALL && event.getEntity() instanceof Player){
             if(!frozenEntities.contains(event.getEntity())){
                 frozenEntities.add(event.getEntity());
             
                 int freezeTime = plugin.getConfig().getInt("freezeTime");
                 
                 final Entity entity = event.getEntity();
                 
                 plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
 
                     @Override
                     public void run() {
                         removeEntity(entity);                        
                     }
                     
                 }, 20L * freezeTime);
                 
             
             }
         }
     }
     
     @EventHandler
     public void OnEntityMove(PlayerMoveEvent event){
         // ignore if not enabled
        if(plugin.getConfig().getBoolean("enabled")){
             return;
         }
         
         if(frozenEntities.contains((Entity) event.getPlayer())){
             event.getPlayer().teleport(event.getFrom());          
         }
 
     }
     
     public void removeEntity(Entity entity){
         if(frozenEntities.contains(entity)){
             frozenEntities.remove(entity);
         }
     }
     
 
 }
