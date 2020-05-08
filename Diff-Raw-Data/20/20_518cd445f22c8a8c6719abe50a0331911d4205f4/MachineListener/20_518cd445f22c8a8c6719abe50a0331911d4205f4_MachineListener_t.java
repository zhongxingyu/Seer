 package net.aesircraft.machina.Listeners;
 
 import net.aesircraft.machina.Machina;
 import net.aesircraft.machina.Objects.MachinaBlock;
 import org.bukkit.Bukkit;
 import org.bukkit.block.Block;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.*;
 import org.bukkit.event.entity.EntityChangeBlockEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.getspout.spoutapi.block.SpoutBlock;
 
 public class MachineListener implements Listener {
 
     public MachineListener(Machina plugin) {
 	Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPiston(BlockPistonRetractEvent e) {
 	SpoutBlock sb = (SpoutBlock) e.getRetractLocation().getBlock();
 	if (!sb.isCustomBlock()) {
 	    return;
 	}
 	if (!sb.getCustomBlock().getFullName().toLowerCase().contains("machina block")) {
 	    return;
 	}
 	e.setCancelled(true);
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPiston(BlockPistonExtendEvent e) {
 	for (Block b : e.getBlocks()) {
 	    SpoutBlock sb = (SpoutBlock) b;
 	    if (!sb.isCustomBlock()) {
 		continue;
 	    }
 	    if (!sb.getCustomBlock().getFullName().toLowerCase().contains("machina block")) {
 		continue;
 	    }
 	    e.setCancelled(true);
 	    return;
 	}
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onChange(EntityChangeBlockEvent e) {
 	SpoutBlock sb = (SpoutBlock) e.getBlock();
 	if (!sb.isCustomBlock()) {
 	    return;
 	}
 	if (!sb.getCustomBlock().getFullName().toLowerCase().contains("machina block")) {
 	    return;
 	}
 	e.setCancelled(true);
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onExplode(EntityExplodeEvent e) {
 	for (Block b : e.blockList()) {
 	    SpoutBlock sb = (SpoutBlock) b;
 	    if (!sb.isCustomBlock()) {
 		continue;
 	    }
 	    if (!sb.getCustomBlock().getFullName().toLowerCase().contains("machina block")) {
 		continue;
 	    }
 	    e.setCancelled(true);
 	}
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onRedstone(BlockPhysicsEvent e) {
 	if (e.getBlock() == null) {
 	    return;
 	}
 	SpoutBlock b = (SpoutBlock) e.getBlock();
 	if (!b.isCustomBlock()) {
 	    return;
 	}
 	if (!b.getCustomBlock().getName().contains("Machina Block")) {
 	    return;
 	}
 	MachinaBlock mb = new MachinaBlock(e.getBlock());
 	mb.load();
	if (mb.isWorking()){
	    return;
	}
 	if (e.getBlock().isBlockPowered() && !mb.isPowered()) {
 	    mb.setBlocks();
 	    return;
 	}
 	if (!e.getBlock().isBlockPowered() && mb.isPowered()) {
 	    {
 		mb.removeBlocks();
 		return;
 	    }
 	}
 
     }
 }
