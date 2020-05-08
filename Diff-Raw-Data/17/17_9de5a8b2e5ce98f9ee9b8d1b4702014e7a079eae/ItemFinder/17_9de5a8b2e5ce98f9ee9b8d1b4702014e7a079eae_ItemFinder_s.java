 package net.guipsp.cts;
 
 import org.bukkit.*;
 import org.bukkit.block.*;
 import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.util.Vector;
 
 public class ItemFinder implements Runnable {
 	public volatile boolean running = true;
 	private Plugin plugin;
 	private int range;
 	private int dragrange;
 
 	public ItemFinder(Plugin plugin, int range, int dragrange) {
 		this.plugin = plugin;
 		this.range = range;
 		this.dragrange = dragrange;
 	}
 
 	@Override
 	public void run() {
 		while (running) {
 			try {
 				for (World world : plugin.getServer().getWorlds()) {
 					for (Entity entity : world.getEntitiesByClass(Item.class)) {
 						doEntity(entity, world);
 
 					}
 				}
 			} catch (Exception e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			try {
 				Thread.sleep(500);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	private void doEntity(Entity entity, World world) {
 		for (int x = entity.getLocation().getBlockX() - range; x < entity
 				.getLocation().getBlockX() + range; x++) {
 			for (int y = entity.getLocation().getBlockY() - range; y < entity
 					.getLocation().getBlockY() + range; y++) {
 				for (int z = entity.getLocation().getBlockZ() - range; z < entity
 						.getLocation().getBlockZ() + range; z++) {
 					Block block = world.getBlockAt(x, y, z);
 					if (block.getType() == Material.CHEST
 							&& block.isBlockPowered()) {
 						Inventory inv = ((Chest) block.getState())
 								.getInventory();
						inv.addItem(((Item) entity).getItemStack());
 						entity.remove();
 						world.playEffect(block.getLocation(), Effect.CLICK1, 0);
 						return;
 					}
 
 				}
 			}
 		}
 		for (int x = entity.getLocation().getBlockX() - dragrange; x < entity
 				.getLocation().getBlockX() + dragrange; x++) {
 			for (int y = entity.getLocation().getBlockY() - dragrange; y < entity
 					.getLocation().getBlockY() + dragrange; y++) {
 				for (int z = entity.getLocation().getBlockZ() - dragrange; z < entity
 						.getLocation().getBlockZ() + dragrange; z++) {
 					Block block = world.getBlockAt(x, y, z);
 					if (block.getType() == Material.CHEST
 							&& block.isBlockPowered()) {
 						Vector Vel = entity.getVelocity();
 						Vector Direction = entity.getLocation()
 								.subtract(block.getLocation().toVector())
 								.toVector().normalize();
 						Vel = Vel.subtract(Direction.multiply(0.1));
 						entity.setVelocity(Vel);
 						return;
 					}
 
 				}
 			}
 		}
 	}
 
 }
