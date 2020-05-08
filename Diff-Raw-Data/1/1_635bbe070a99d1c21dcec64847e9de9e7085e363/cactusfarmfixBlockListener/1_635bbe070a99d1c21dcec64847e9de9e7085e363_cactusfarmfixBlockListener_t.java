 package me.kukelekuuk00.cactusfarmfix;
 
 import java.util.logging.Logger;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockPhysicsEvent;
 import org.bukkit.Material;
 import org.bukkit.block.BlockFace;
 import java.util.HashSet;
 import java.util.Set;
 
 public class cactusfarmfixBlockListener extends BlockListener {
 	Logger log = Logger.getLogger("Minecraft");
 	
 	
 	public void onBlockPhysics(BlockPhysicsEvent evt) {
 		Block block = evt.getBlock();
 		if(block.getType() == Material.CACTUS) {
 			if(!isSafeCactusBlock(block.getRelative(BlockFace.NORTH, 1)) || !isSafeCactusBlock(block.getRelative(BlockFace.SOUTH, 1)) ||!isSafeCactusBlock(block.getRelative(BlockFace.EAST, 1)) ||!isSafeCactusBlock(block.getRelative(BlockFace.WEST, 1))) { 
 				evt.setCancelled(true);
 				block.setType(Material.AIR);
 			}
 		}
 	}
 
 	  private static final Set<Integer> AIR_MATERIALS = new HashSet<Integer>();
 	  
 	  static {
 	    AIR_MATERIALS.add(Material.AIR.getId());
 	    AIR_MATERIALS.add(Material.SAPLING.getId());
 	    AIR_MATERIALS.add(Material.POWERED_RAIL.getId());
 	    AIR_MATERIALS.add(Material.DETECTOR_RAIL.getId());
 	    AIR_MATERIALS.add(Material.LONG_GRASS.getId());
 	    AIR_MATERIALS.add(Material.DEAD_BUSH.getId());
 	    AIR_MATERIALS.add(Material.YELLOW_FLOWER.getId());
 	    AIR_MATERIALS.add(Material.RED_ROSE.getId());
 	    AIR_MATERIALS.add(Material.BROWN_MUSHROOM.getId());
 	    AIR_MATERIALS.add(Material.RED_MUSHROOM.getId());  
 	    AIR_MATERIALS.add(Material.TORCH.getId());
 	    AIR_MATERIALS.add(Material.REDSTONE_WIRE.getId());
 	    AIR_MATERIALS.add(Material.SEEDS.getId());
 	    AIR_MATERIALS.add(Material.SIGN_POST.getId());
 	    AIR_MATERIALS.add(Material.WOODEN_DOOR.getId());
 	    AIR_MATERIALS.add(Material.LADDER.getId());
 	    AIR_MATERIALS.add(Material.RAILS.getId());
 	    AIR_MATERIALS.add(Material.LEVER.getId());
 	    AIR_MATERIALS.add(Material.STONE_PLATE.getId());
 	    AIR_MATERIALS.add(Material.IRON_DOOR_BLOCK.getId());
 	    AIR_MATERIALS.add(Material.WOOD_PLATE.getId());  
 	    AIR_MATERIALS.add(Material.REDSTONE_TORCH_OFF.getId());
 	    AIR_MATERIALS.add(Material.REDSTONE_TORCH_ON.getId());
 	    AIR_MATERIALS.add(Material.STONE_BUTTON.getId());
 	    AIR_MATERIALS.add(Material.SUGAR_CANE_BLOCK.getId());    
 	    AIR_MATERIALS.add(Material.DIODE_BLOCK_OFF.getId());
 	    AIR_MATERIALS.add(Material.DIODE_BLOCK_ON.getId());    
 	    AIR_MATERIALS.add(Material.TRAP_DOOR.getId());
 	    AIR_MATERIALS.add(Material.PUMPKIN_STEM.getId());
 	    AIR_MATERIALS.add(Material.MELON_STEM.getId());
 	    AIR_MATERIALS.add(Material.VINE.getId());
 	    //Cactus edits
 	    AIR_MATERIALS.add(Material.WATER.getId());
 	    AIR_MATERIALS.add(Material.LAVA.getId());
 	    AIR_MATERIALS.add(Material.STATIONARY_WATER.getId());
 	    AIR_MATERIALS.add(Material.STATIONARY_LAVA.getId());
 	    AIR_MATERIALS.add(Material.FIRE.getId());
 
 	        
 	  };
 	
 	public boolean isSafeCactusBlock(Block block) {
 		int type = block.getType().getId();
 		if(AIR_MATERIALS.contains(type)) return true;
 		return false;
 	}
 
 
 	
 	
 	public static cactusfarmfix plugin;
 	 
 	public cactusfarmfixBlockListener(cactusfarmfix instance) {
 	    plugin = instance; 
 	}
 }
