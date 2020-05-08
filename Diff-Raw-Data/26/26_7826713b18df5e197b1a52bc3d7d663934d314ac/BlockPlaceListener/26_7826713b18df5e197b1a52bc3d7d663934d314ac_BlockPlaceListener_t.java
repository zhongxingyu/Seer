 package to.joe.j2mc.fun;
 
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 import to.joe.j2mc.core.J2MC_Manager;
 
 public class BlockPlaceListener implements Listener{
 
     J2MC_Fun plugin;
     
     public BlockPlaceListener(J2MC_Fun fun){
         this.plugin = fun;
     }
     
     @EventHandler
     public void onBlockPlace(BlockPlaceEvent event){
         int blockid = event.getBlock().getTypeId();
         boolean isAdmin = J2MC_Manager.getPermissions().isAdmin(event.getPlayer().getName());
         boolean isTrusted = J2MC_Manager.getPermissions().hasFlag(event.getPlayer().getName(), 't');
         
         if(plugin.BlockedForNormals.contains(blockid)){
            if(!isAdmin && !isTrusted){
                 if(plugin.BlockedForTrusted.contains(blockid)){
                     event.getPlayer().sendMessage(ChatColor.RED + "You can't place that block");
                 }else{
                     event.getPlayer().sendMessage(ChatColor.RED + "You need to be trusted to place that");
                 }
                 event.setCancelled(true);
                 return;
             }
         }
         if(plugin.BlockedForTrusted.contains(blockid)){
             if(isTrusted && !isAdmin){
                 event.getPlayer().sendMessage(ChatColor.RED + "Even trusted have their limits, you can't place that");
                 event.setCancelled(true);
                 return;
            }
            if(!isAdmin && !isTrusted){
                 event.getPlayer().sendMessage(ChatColor.RED + "You can't place that block");
                 event.setCancelled(true);
                 return;
             }
         }
     }
     
 }
