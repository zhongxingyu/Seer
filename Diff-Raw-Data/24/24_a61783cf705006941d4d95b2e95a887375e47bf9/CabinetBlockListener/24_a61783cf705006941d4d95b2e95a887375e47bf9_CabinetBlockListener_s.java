 package com.mjmr89.Cabinet;
 
 import net.minecraft.server.IInventory;
 import net.minecraft.server.InventoryLargeChest;
 
 import org.bukkit.Material;
 import org.bukkit.World;
 
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.craftbukkit.inventory.CraftInventory;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockRightClickEvent;
 import org.bukkit.inventory.Inventory;
 
 public class CabinetBlockListener extends BlockListener {
 	Cabinet plugin;
 
 	CabinetBlockListener(Cabinet plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
 	public void onBlockRightClick(BlockRightClickEvent e) {
 		Block b = e.getBlock();
 		Player p = e.getPlayer();
 
 		// if its a chest and covered at least partially
 		if (b.getState() instanceof Chest && covered(b)) {
 			// if its a double
 			if(adjacentToChest(b)){
 				Block b2 = getAdjacentChestBlock(b);
 				openDoubleChest(b,b2,p);
 				
 			}else{
 				openSingleChest(b,p);
 			}
 			
 
 		}
 
 		//
 		// if (b.getState() instanceof Chest
 		// && (solidBlock(bAbove) || isPartiallyCovered(b))) {
 		// Player p = e.getPlayer();
 		// Inventory inv = ((Chest) b.getState()).getInventory();
 		// p.sendMessage("Size of inventory: " + inv.getSize());
 		//
 		// CraftInventory cInventory = (CraftInventory) inv;
 		// CraftPlayer cPlayer = (CraftPlayer) p;
 		// cPlayer.getHandle().a((IInventory) cInventory.getInventory());
 		// }
 
 	}
 	
 	
 
 	void openSingleChest(Block b, Player p) {
 		Inventory inv = ((Chest) b.getState()).getInventory();
 
 		CraftInventory cInventory = (CraftInventory) inv;
 		CraftPlayer cPlayer = (CraftPlayer) p;
 		cPlayer.getHandle().a((IInventory) cInventory.getInventory());
 	}
 
 	void openDoubleChest(Block b1, Block b2, Player p) {
		Chest c1 = (Chest)b1.getState();
		Chest c2 = (Chest)b2.getState();
 		CraftPlayer cPlayer = (CraftPlayer) p;
 
 		CraftInventory cInventory1 = (CraftInventory) c1.getInventory();
 		CraftInventory cInventory2 = (CraftInventory) c2.getInventory();
 		IInventory IChest = (IInventory) new InventoryLargeChest("Large chest",
 				cInventory1.getInventory(), cInventory2.getInventory());
 		cPlayer.getHandle().a(IChest);
 	}
 
 	boolean covered(Block b) {
 		if (adjacentToChest(b)) {
			if (solidBlock(getBlockAbove(getAdjacentChestBlock(b)))) {
 				return true;
 			}
		} else {
 			if (solidBlock(getBlockAbove(b))) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	Block getBlockAbove(Block b) {
 		return b.getWorld().getBlockAt(b.getX(), b.getY() + 1, b.getZ());
 	}
 
 	boolean adjacentToChest(Block b) {
 		if (getAdjacentChestBlock(b) == null) {
 			return false;
 		}
 		return true;
 	}
 
 	Block getAdjacentChestBlock(Block b) {
 		World w = b.getWorld();
 		int x = b.getX();
 		int y = b.getY();
 		int z = b.getZ();
 		Block[] bArr = { w.getBlockAt(x + 1, y, z), w.getBlockAt(x - 1, y, z),
 				w.getBlockAt(x, y, z + 1), w.getBlockAt(x, y, z - 1) };
 		for (int i = 0; i < 4; i++) {
 			if (bArr[i].getState() instanceof Chest) {
 				return bArr[i];
 			}
 		}
 		return null;
 	}
 
 	boolean isPartiallyCovered(Block b) {
 		int x = b.getX();
 		int y = b.getY();
 		int z = b.getZ();
 		World w = b.getWorld();
 
 		Block[] bAboves = { w.getBlockAt(x + 1, y + 1, z),
 				w.getBlockAt(x - 1, y + 1, z), w.getBlockAt(x, y + 1, z + 1),
 				w.getBlockAt(x, y + 1, z - 1) };
 		for (int i = 0; i < 4; i++) {
 			if (solidBlock(bAboves[i])
 					&& w.getBlockAt(bAboves[i].getX(), bAboves[i].getY() - 1,
 							bAboves[i].getZ()).getState() instanceof Chest) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	boolean solidBlock(Block b) {
 		switch (b.getType()) {
 		case AIR:
 		case TORCH:
 		case LADDER:
 		case STONE_BUTTON:
 		case LEVER:
 		case REDSTONE_TORCH_OFF:
 		case REDSTONE_TORCH_ON:
 		case PAINTING:
 		case WALL_SIGN:
 
 			return false;
 		}
 		return true;
 
 	}
 
 }
