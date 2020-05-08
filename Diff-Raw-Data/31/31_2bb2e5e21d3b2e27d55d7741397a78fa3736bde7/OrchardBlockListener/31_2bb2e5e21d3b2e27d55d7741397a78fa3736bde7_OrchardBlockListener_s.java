 package io.rampant.bukkit.orchard;
 
 import org.bukkit.Material;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.LeavesDecayEvent;
 
 /**
  *
  * @author jonathan
  */
 public class OrchardBlockListener extends BlockListener {
 	private final Orchard plugin;
 
 	public OrchardBlockListener(Orchard instance) {
 		plugin = instance;
 	}
 
 	@Override
 	public void onBlockBreak(BlockBreakEvent event) {
 		if( event.getBlock().getType() == Material.LEAVES ) {
			Tree.pruneLeaf(event.getBlock(), false);
 		}
 	}
 
 	@Override
 	public void onLeavesDecay(LeavesDecayEvent event) {
 		if( event.getBlock().getType() == Material.LEAVES ) {
			Tree.pruneLeaf(event.getBlock(), true);
 		}
 	}
 
 }
