 package me.damo1995.AnimalProtect;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 
 public class RideListener implements Listener
 {
    public static AnimalProtect plugin;
    long lnt;
 
    public RideListener(AnimalProtect I)
    {
       plugin = I;
    }
    @EventHandler(priority=EventPriority.LOWEST)
    public void onRideEvent(PlayerInteractEntityEvent event)
    {
       Entity clickedEntity = event.getRightClicked();
       Location loc = event.getRightClicked().getLocation();
       Player player = event.getPlayer();
       String errorMsg = "";
 
       if (clickedEntity.getType() == EntityType.HORSE)
      {         
          Horse mount = (Horse) clickedEntity;
 
          List<String> protect = plugin.getConfig().getStringList("protect-from-player");
 
          if (protect.contains("HORSE"))
          {
             boolean allowMounting = false;
 
             if(!mount.isTamed())
             {
                // untamed mounts may be ridden by everyone with building rights
                if (player.isOp() ||
                      player.hasPermission("animalprotect.bypass") ||          
                      plugin.getWorldGuardPlugin().canBuild(player, loc))
                {
                   allowMounting = true;
                }
                else
                {
                   errorMsg = AnimalProtect.NoRideBecauseNoBuildPermMsg;
                }
             }
             else 
             { // tamed mount
                if(null == mount.getOwner())
                {
                   // for plugin compatibility where someone does weird stuff (taming a mount without setting its owner)
                   // tamed mounts without owner may be ridden by everyone which has building rights
                   if (player.isOp() ||
                         player.hasPermission("animalprotect.bypass") ||
                         plugin.getWorldGuardPlugin().canBuild(player, loc))
                   {
                      allowMounting = true;  
                   }
                   else
                   {
                      errorMsg = AnimalProtect.NoRideBecauseNoBuildPermMsg;
                   }
                }
                else
                {
                   // TODO Eventuell eine "Reiterliste" pro Pferd (UUID) anlegbar machen, so dass Spieler anderen Spielern Zugriff auf ein
                   //      gezaehmtes, ihnen gehoerendes Pferd geben können
 
                   if (!mount.getOwner().getName().equalsIgnoreCase(player.getName()))
                   {
                      player.sendMessage(AnimalProtect.MountOwnerMsg + " " + ChatColor.WHITE + mount.getOwner().getName() + ChatColor.RED + ".");  
                   }
 
                   // only owner or "allowed riders" may ride a tamed horse that has an owner
                   // owners may ride a horse, regardless of WG building rights
                   if (player.isOp() ||
                         player.hasPermission("animalprotect.bypass"))          
                   {
                      allowMounting = true;
                   }
                   else
                   {
                      if (mount.getOwner().getName().equalsIgnoreCase(player.getName()))
                      {
                         allowMounting = true;
                      }
                      else
                      {
                         errorMsg = AnimalProtect.NoRideBecauseNotOwnerMsg;
                      }
                   }
                }
             }
 
             if(!allowMounting)
             {
                player.sendMessage(errorMsg);               
                event.setCancelled(true);
             }
          }
       }
    }
 }
