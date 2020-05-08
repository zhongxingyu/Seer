 package uk.co.jacekk.bukkit.spawnertest;
 
 import java.util.ArrayList;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.entity.EntityExplodeEvent;
 
import uk.co.jacekk.bukkit.baseplugin.v9_1.event.BaseListener;
 
 public class TNTListener extends BaseListener<SpawnerTest> {
     
     public TNTListener(SpawnerTest plugin) {
         super(plugin);
     }
     
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onEntityExplode(EntityExplodeEvent event){
         ArrayList<Block> remove = new ArrayList<Block>();
         
         for (Block block : event.blockList()){
             if (block.getType() == Material.MOB_SPAWNER){
                 remove.add(block);
             }
         }
         
         event.blockList().removeAll(remove);
     }
     
 }
