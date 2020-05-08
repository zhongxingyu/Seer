 package me.blha303;
 
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Mossifier extends JavaPlugin implements Listener {
 
 	public void onEnable() {
 		getServer().getPluginManager().registerEvents(this, this);
 	}
 
 	public Block getFirstNotAir(List<Block> lob) { // With thanks to meem1029 :)
 		for (Block b : lob) {
			if (b.getType() != Material.AIR) return b;
 		}
 		return null;
 	}
 
 	@EventHandler
 	public void onInteract(PlayerInteractEvent event) {
 		Player p = event.getPlayer();
 		ItemStack item = event.getItem();
		if (item == null) return;
 		Block block = getFirstNotAir(p.getLineOfSight(null, 10));
		if (block == null) return;
 
 		if (item.getType() == Material.INK_SACK && item.getDurability() == 15) { // If item is bonemeal...
 			if (block.getType() == Material.COBBLESTONE) { // ... and the block clicked is cobblestone...
 				block.setType(Material.MOSSY_COBBLESTONE); // ... set the block to moss stone,
 				item.setAmount(item.getAmount() - 1); // and take one bonemeal from the player
 				p.getInventory().setItemInHand(item);
 				return;
 			} else if (block.getType() == Material.WATER) { // ... and the block clicked is water...
 				Location lploc = block.getRelative(BlockFace.UP).getLocation(); // get the block space above it,
 				lploc.getBlock().setType(Material.WATER_LILY); // ... set that block to a lilypad,
 				item.setAmount(item.getAmount() - 1); // and take one bonemeal from the player.
 				p.getInventory().setItemInHand(item);
 				return;
 			} else if (block.getType() == Material.SMOOTH_BRICK && block.getData() == 0) { // ... and the block clicked is stone bricks... 
 				block.setData((byte) 1); // ... set the block to moss bricks,
 				item.setAmount(item.getAmount() - 1); // and take one bonemeal from the player.
 				p.getInventory().setItemInHand(item);
 				return;
			}
 		}
 		return;
 	}
 }
