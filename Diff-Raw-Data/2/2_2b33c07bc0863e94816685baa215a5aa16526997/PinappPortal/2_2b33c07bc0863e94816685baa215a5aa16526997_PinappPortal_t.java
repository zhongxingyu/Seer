 package com.ubempire.not.a.portal;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerPortalEvent;
 import org.bukkit.event.world.PortalCreateEvent;
 import org.bukkit.event.world.WorldListener;
 
 public class PinappPortal extends PlayerListener {
 	Pinapp p;
 
 	PinappPortal(Pinapp p) {
 		this.p = p;
 	}
 
 	public void register() {
 		p.getServer()
 				.getPluginManager()
 				.registerEvent(Event.Type.PLAYER_PORTAL, this, Priority.Normal,
 						p);
 		p.getServer()
 				.getPluginManager()
 				.registerEvent(Event.Type.PORTAL_CREATE, new PinappWorld(),
 						Priority.Normal, p);
 		p.log("Registered PLAYER_PORTAL and PORTAL_CREATE events successfully.");
 	}
 
 	@SuppressWarnings("unused")
 	public void onPlayerPortal(PlayerPortalEvent event) {
 		if(event.isCancelled())
 			return;
 		// Debugging code
 		// p.log("Player portal event!");
 		Player player = event.getPlayer();
 		event.setCancelled(true);
 		if (!(player.hasPermission("pinapp.portal.travel") || player.isOp()))
 			return;
 		Location from = event.getFrom();
 		String fromWorld = from.getWorld().getName();
 		Block block = from.getBlock();
 		if(block.getTypeId() == 0) {
 			// Yeah... I know.
 			block = block.getWorld().getBlockAt(block.getLocation());
 		}
 		for (int i = 0; i <= 4; i++) {
 			if (block.getRelative(0, -i, 0).getType() != Material.PORTAL
 					&& block.getRelative(0, -1, 0).getType() != Material.AIR) {
 				block = block.getRelative(0, -i, 0);
 				break;
 			}
 		}
 		CheckBlock b = new CheckBlock(block);
 		int orientation = b.check();
 		int typeTo = block.getTypeId();
 		
 		int typeId = p.pc.getId(from.getWorld().getName());
 		// Debugging code
 		// p.log(block.getTypeId() + ":" + orientation);
 		String worldToString = p.pc.getWorld(typeTo);
 		if(worldToString == null)
 			return;
 		World worldTo = p.getServer().getWorld(worldToString);
 		if(worldTo == null)
 			return;
 		Block block2 = worldTo.getBlockAt(block.getX(), block.getY(), block.getZ());
 		CheckBlock.createPortal(block2, typeId, orientation);
 		Location tp;
 		tp = block2.getRelative(2, 1, 0).getLocation();
 		if(orientation>1)
 			tp = block2.getRelative(0, 1, 2).getLocation();
		player.setNoDamageTicks(2);
 		player.teleport(tp);
 	}
 
 }
 
 class PinappWorld extends WorldListener {
 
 	public void onPortalCreate(PortalCreateEvent event) {
 		event.setCancelled(true);
 	}
 }
