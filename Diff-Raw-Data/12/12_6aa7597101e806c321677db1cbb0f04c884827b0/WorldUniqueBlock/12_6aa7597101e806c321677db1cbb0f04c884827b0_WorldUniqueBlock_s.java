 package gt.general.world;
 
 import static com.google.common.collect.Sets.newHashSet;
 
 import gt.general.logic.persistance.PersistanceMap;
 import gt.general.logic.persistance.YamlSerializable;
 import gt.general.world.ObservableCustomBlock.BlockEvent;
 
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.getspout.spoutapi.SpoutManager;
 
 public class WorldUniqueBlock extends YamlSerializable implements BlockObserver {
 
 	private final World world;
 	
 	private final ObservableCustomBlock base;
 	private Block block = null;
 	
 	private static final String KEY_LOCATION = "location";
 	
 	/**
 	 * @param world where the block be blacked
 	 * @param base the spout custom block
 	 */
 	public WorldUniqueBlock(final World world, final ObservableCustomBlock base) {
 		this.world = world;
 		this.base = base;
 	}
 	
 	@Override
 	public void onBlockEvent(final BlockEvent blockEvent) {
 		switch (blockEvent.blockEventType) {
 		case PLAYER_BLOCK_PLACED:
 
 			if (block == null) {
 				block = blockEvent.block;
 			} else if( blockEvent.entity instanceof Player) {
 				// TODO maybe more output
 				((Player) blockEvent.entity ).sendMessage("there can be only one block of this type");
 			} else {
 				Bukkit.broadcastMessage("cannot place a block: " + base.getName());
 			}
 			break;
 		
 		case BLOCK_DESTROYED:
 			block = null;
 		break;
 
 		default:
 			break;
 		}
 		
 	}
 
 	@Override
 	public void setup(final PersistanceMap values, final World world) {
 		
 		// TODO bad style
		if(values.getMap() != null) {
 			block = values.getBlock(KEY_LOCATION, world);
 			SpoutManager.getMaterialManager().overrideBlock(block, base);
 		}
 		
 				
 		base.addObserver(this, world);
 	}
 
 	@Override
 	public PersistanceMap dump() {
 		PersistanceMap map = new PersistanceMap();
 		
 		if(block != null) {
 			map.put(KEY_LOCATION, block);
 		}
 		
 		return map;
 	}
 
 	@Override
 	public void dispose() {
 		
 		if(block != null) {
 			block.setType(Material.AIR);
 		}
 		base.removeObserver(this, world);
 		
 	}
 
 	@Override
 	public Set<Block> getBlocks() {
 		
 		Set<Block> blocks = newHashSet();
 		
 		if (block != null) {
 		
 			blocks.add(block);
 			return blocks;
 		}
 		return blocks;
 	}
 
 }
