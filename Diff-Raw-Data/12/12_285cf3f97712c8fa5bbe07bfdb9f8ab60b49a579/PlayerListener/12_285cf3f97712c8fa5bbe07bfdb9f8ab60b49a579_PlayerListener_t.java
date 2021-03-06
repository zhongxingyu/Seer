 package net.h31ix.anticheat.event;
 
 import net.h31ix.anticheat.manage.*;
 import net.h31ix.anticheat.Anticheat;
 import net.h31ix.anticheat.PlayerTracker;
 import net.h31ix.anticheat.checks.*;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerAnimationEvent;
 import org.bukkit.event.player.PlayerAnimationType;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.vehicle.VehicleEnterEvent;
 
 public class PlayerListener implements Listener {
     Anticheat plugin;
     AnimationManager am;
     ExemptManager ex;
     PlayerTracker tracker;
     ItemManager im;
     HealthManager hm;
    
     public PlayerListener(Anticheat plugin)
     {
         this.plugin = plugin;
         this.am = plugin.am;
         this.ex = plugin.ex;
         this.im = plugin.im;
         this.tracker = plugin.tracker;
         this.hm = plugin.hm;
     }
     
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event)
     {
        event.getPlayer().sendMessage("§f §f §1 §0 §2 §4");
        event.getPlayer().sendMessage("§f §f §2 §0 §4 §8");
        event.getPlayer().sendMessage("§f §f §4 §0 §9 §6");            
     }
     
     @EventHandler
     public void onPlayerDropItem(PlayerDropItemEvent event)
     {
         Player player = event.getPlayer();
         if(!im.hasDropped(player))
         {
             im.logDrop(player);
         }
         else
         {
             plugin.log(player.getName()+" tried to drop blocks too fast!");
             event.setCancelled(true);
         }
     }
     
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerAnimation(PlayerAnimationEvent event)
     {
         if(event.getAnimationType() == PlayerAnimationType.ARM_SWING)
         {
             am.logAnimation(event.getPlayer());
         }
     }
     
     @EventHandler
     public void onVehicleEnter(VehicleEnterEvent event)
     {
         if(event.getEntered() instanceof Player)
         {
             ex.logEnter((Player)event.getEntered());
         }
     }    
     
     @EventHandler
     public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
     {
         if(!plugin.lagged)
         {        
             plugin.cm.addChat(event.getPlayer());
         }        
     }
     
     @EventHandler
     public void onPlayerChat(PlayerChatEvent event)
     {
         if(!plugin.lagged)
         {        
             plugin.cm.addChat(event.getPlayer());
         }
     }
     
     @EventHandler
     public void onPlayerKick(PlayerKickEvent event)
     {
         plugin.cm.clear(event.getPlayer());
     }
     
     @EventHandler
     public void onPlayerMove(PlayerMoveEvent event)
     {
         Player player = event.getPlayer();
         if(!plugin.lagged && !ex.isHit(player))
         {
             hm.log(player);
             LengthCheck c = new LengthCheck(event.getFrom(), event.getTo());
             double xd = c.getXDifference();
             double zd = c.getZDifference();
             double yd = c.getYDifference();
             Block p1 = player.getLocation().getWorld().getBlockAt(player.getLocation());
             if(p1.isLiquid())
             {
                 if (player.getVehicle() != null)
                 {
                     if(xd > 2.0D || zd > 2.0D)
                     {
                         tracker.increaseLevel(player);
                         plugin.log(player.getName()+" is using a boat too fast! XSpeed="+xd+" ZSpeed="+zd);
                     }
                 }                
                 else if(xd > 0.19D || zd > 0.19D)
                 {
                     if(!player.isSprinting() && !player.isFlying())
                     {
                         tracker.increaseLevel(player);
                         plugin.log(player.getName()+" is walking too fast in water! XSpeed="+xd+" ZSpeed="+zd);
                         event.setTo(event.getFrom().clone());
                     }  
                 }
                 else
                 {
                     if(xd > 0.3D || zd > 0.3D)
                     {
                         tracker.increaseLevel(player);
                         plugin.log(player.getName()+" is flying/sprinting too fast in water! XSpeed="+xd+" ZSpeed="+zd);
                         event.setTo(event.getFrom().clone());
                     }
                 }                
             }
             else
             {
                 if (player.getVehicle() != null)
                 {
                     if(!ex.isEntering(player))
                     {
                         if(xd > 0.6D || zd > 0.6D)
                         {
                             tracker.increaseLevel(player);
                             plugin.log(player.getName()+" is using a vehicle too fast! XSpeed="+xd+" ZSpeed="+zd);
                             event.setTo(event.getFrom().clone());
                         }
                     }
                 }               
                 else if(player.isSneaking())
                 {
                     if(xd > 0.17D || zd > 0.17D)
                     {
                         tracker.increaseLevel(player);
                         plugin.log(player.getName()+" is sneaking too fast! XSpeed="+xd+" ZSpeed="+zd);
                         event.setTo(event.getFrom().clone());
                         player.setSneaking(false);
                     }
                 }
                 else if(xd > 0.32D || zd > 0.32D)
                 {
                     if(!player.isSprinting() && !player.isFlying())
                     {
                         tracker.increaseLevel(player);
                         plugin.log(player.getName()+" is walking too fast! XSpeed="+xd+" ZSpeed="+zd);
                         event.setTo(event.getFrom().clone());
                     }              
                     else
                     {
                         if(xd > 0.62D || zd > 0.62D)
                         {
                             tracker.increaseLevel(player);
                             plugin.log(player.getName()+" is flying/sprinting too fast! XSpeed="+xd+" ZSpeed="+zd);
                             event.setTo(event.getFrom().clone());
                         }
                     }
                 }
                 if(event.getFrom().getY() < event.getTo().getY())
                 {
                     //TODO: This is a little hacky. Any better way to figure this out?
                     if(yd <= 0.11761 && yd >= 0.11759)
                     {
                         if(player.getLocation().getBlock().getType() != Material.VINE && player.getLocation().getBlock().getType() != Material.LADDER)
                         {
                             plugin.log(player.getName()+" tried to climb a wall!");
                             tracker.increaseLevel(player);
                             event.setTo(event.getFrom().clone());
                         }
                     }
                     else if(yd > 0.5D)
                     {
                         tracker.increaseLevel(player);
                         plugin.log(player.getName()+" is ascending too fast! YSpeed="+yd);
                         event.setTo(event.getFrom().clone());
                     }
                 } 
                 if(event.getFrom().getY() > event.getTo().getY())
                 {         
                     
                     if(player.getGameMode() != GameMode.CREATIVE && player.getVehicle() == null)
                     {
                         hm.log(player);
                         if(hm.checkFall(player))
                         {
                             plugin.log(player.getName()+" tried to avoid fall damage!");
                             tracker.increaseLevel(player);
                         }
                     }
                 }       
             }
         }
     } 
 }
