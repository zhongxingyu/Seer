 package tzer0.SurfaceProtect;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 import tzer0.SurfaceProtect.SurfaceProtect.WorldSettings;
 
 import ca.xshade.bukkit.towny.object.Coord;
 import ca.xshade.bukkit.towny.object.WorldCoord;
 
 
 public class SurfaceProtectBlockListener extends BlockListener {
     private final SurfaceProtect plugin;
 
     public SurfaceProtectBlockListener(final SurfaceProtect plugin) {
         this.plugin = plugin;
     }
 
     public void onBlockBreak(BlockBreakEvent event) {
         if (blockCheck(event.getBlock(), event.getPlayer())) {
             event.setCancelled(true);
         }
     }
     public void onBlockPlace(BlockPlaceEvent event) {
         if (blockCheck(event.getBlock(), event.getPlayer())) {
             event.setCancelled(true);
         }
     }
     /**
      * checks if this plugin should allow the action.
      * @param block
      * @param pl
      * @param destroy
      * @return
      */
     public boolean blockCheck(Block block, Player pl) {
         // checks if this is happening inside a town - if it is, then that town decides what happens.
         if (plugin.towny != null) {
             WorldCoord worldCoord;
             try {
                 worldCoord = new WorldCoord(plugin.towny.getTownyUniverse().getWorld(block.getWorld().getName()), Coord.parseCoord(block));
                 try { 
                     worldCoord.getTownBlock(); 
                     return false;
                 } catch (Exception e) {
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         WorldSettings tmp = plugin.ws.get(block.getWorld().getName());
         if ((plugin.permissions == null && pl.isOp()) ||
                 (plugin.permissions != null && plugin.permissions.has(pl, "surfaceprotect.ignore"))
                 || plugin.allow.contains(block.getTypeId())
                 ||  (tmp != null && (!tmp.protect || block.getY() < tmp.ylimit))) {
             return false;
         } else {
            pl.sendMessage(ChatColor.RED+"You are not allowed to modify blocks here, limit: " + tmp.ylimit);
             return true;
         }
     }
     public boolean checkArea(Block block, Player pl) {
         int starty = block.getY();
         boolean f1, f2, f3, f4, mid;
         f1 = f2 = f3 = f4 = mid = true;
 
         while ((f1 || f2 || f3 || f4) && block.getY() < 127) {
             if (f1) {
                 f1 = block.getRelative(1, 0, 0).getTypeId() == 0;
             }
             if (f2) {
                 f2 = block.getRelative(-1, 0, 0).getTypeId() == 0;
             }
             if (f3) {
                 f3 = block.getRelative(0, 0, 1).getTypeId() == 0;
             }
             if (f4) {
                 f4 = block.getRelative(0, 0, -1).getTypeId() == 0;
             }
             if (mid && block.getY() != starty) {
                 mid = block.getTypeId() == 0;
             }
             //pl.sendMessage(""+f1+""+f2+""+f3+""+f4+""+mid);
             block = block.getRelative(0, 1, 0);
             if (!(f1 || f2 || f3 || f4 || mid)) {
                 return false;
             }
         }
         if ((f1 || f2 || f3 || f4 || mid)) {
             pl.sendMessage(ChatColor.RED + "You can't remove the surface here!");
             return true;
         } else {
             return false;
         }
     }
 }
