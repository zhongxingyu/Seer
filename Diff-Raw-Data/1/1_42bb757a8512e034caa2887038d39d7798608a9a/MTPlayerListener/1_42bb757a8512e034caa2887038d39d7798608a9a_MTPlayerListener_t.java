 package de.diddiz.MeasuringTape;
 
 import org.bukkit.Material;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 
 class MTPlayerListener extends PlayerListener
 {
 	private final MeasuringTape mt;
 
 	MTPlayerListener(MeasuringTape mt) {
 		this.mt = mt;
 	}
 
 	@Override
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		if (event.getMaterial() == Material.STRING && mt.hasPermission(event.getPlayer(), "measuringtape.measure")) {
 			final Action action = event.getAction();
 			if (action == Action.LEFT_CLICK_BLOCK)
 				mt.attach(event.getPlayer(), event.getClickedBlock(), action);
 			else if (action == Action.RIGHT_CLICK_BLOCK)
 				mt.attach(event.getPlayer(), event.getClickedBlock(), action);
 			else if (action == Action.LEFT_CLICK_AIR && mt.useTargetBlock)
 				mt.attach(event.getPlayer(), event.getPlayer().getTargetBlock(null, Integer.MAX_VALUE), action);
 			else if (action == Action.RIGHT_CLICK_AIR && mt.useTargetBlock)
 				mt.attach(event.getPlayer(), event.getPlayer().getTargetBlock(null, Integer.MAX_VALUE), action);
			event.setCancelled(true);
 		}
 	}
 }
