 package littlegruz.domination.listeners;
 
 import littlegruz.domination.DomMain;
 import littlegruz.domination.entities.CapturePoint;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 
 public class PlayerInteract implements Listener{
    private DomMain plugin;
    
    public PlayerInteract(DomMain instance){
       plugin = instance;
    }
 
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
       
       if(event.getPlayer().getItemInHand().getType().compareTo(Material.STICK) == 0){
          RegionManager rm;
          ProtectedRegion region;
          Location loc;
          
          rm = plugin.getWorldGuard().getRegionManager(event.getClickedBlock().getWorld());
          loc = event.getClickedBlock().getLocation();
          region = plugin.getRegionByLocation(loc, rm.getRegions());
 
         /* Create capture region*/
         if(plugin.getCapturePointMap().get(region.getId()) == null){
            plugin.getCapturePointMap().put(region.getId(), new CapturePoint(region.getId()));
            event.getPlayer().sendMessage("Capture point set to " + region.getId());
         }
         /* Remove capture region*/
         else{
            plugin.getCapturePointMap().remove(region.getId());
            event.getPlayer().sendMessage("Capture point " + region.getId() + " removed");
          }
       }
    }
 }
