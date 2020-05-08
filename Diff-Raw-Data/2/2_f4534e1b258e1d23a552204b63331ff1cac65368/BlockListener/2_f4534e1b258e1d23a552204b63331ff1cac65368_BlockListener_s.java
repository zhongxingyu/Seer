import org.bukkit.entity.player;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
  
 public class BlockListener implements Listener {
       
       @Override
     public void onBlockBreak (BlockBreakEvent event) {
         Player p = event.getPlayer();
     }
     }
