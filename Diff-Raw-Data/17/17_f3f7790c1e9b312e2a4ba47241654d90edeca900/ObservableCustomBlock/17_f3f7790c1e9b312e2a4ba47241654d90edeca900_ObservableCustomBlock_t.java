 package gt.general.world;
 
 
 import gt.plugin.meta.CustomBlockType;
 import gt.plugin.meta.Hello;
 
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
import org.getspout.spoutapi.block.design.GenericCubeBlockDesign;
 import org.getspout.spoutapi.material.block.GenericCubeCustomBlock;
import org.getspout.spoutapi.material.block.GenericCustomBlock;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 
 
public class ObservableCustomBlock extends GenericCustomBlock {
 
 	private CustomBlockType customBlockType;
 	
 	public enum BlockEventType {
 		PLAYER_BLOCK_PLACED,
 		BLOCK_DESTROYED,
 		BLOCK_INTERACT
 	}
 	
 	// TODO this must be done better
 	public class BlockEvent {		
 		public Block block;
 		public LivingEntity entity;
 		public BlockEventType blockEventType;
 		public Player player;
 	}
 	
 	Multimap<World, BlockObserver> observers = HashMultimap.create();
 
 	
 	public ObservableCustomBlock(String name, String textureUrl, int textureSize){
		//TODO: remove 1 (underlying block) if bug in spout is fixed
		super(Hello.getPlugin(), name, 1, false);
		
		GenericCubeBlockDesign design = new GenericCubeBlockDesign(Hello.getPlugin(), textureUrl, textureSize);
		design.setRenderPass(1);
		//TODO: set these when updated to new spoutversion (old version has a bug here)
		setBlockDesign(design);
		
		setOpaque(false);
 	}
 
 	public void onBlockPlace(World world, int x, int y, int z, final LivingEntity living) {
 		
 		BlockEvent e = new BlockEvent();
 		e.block = world.getBlockAt(x, y, z);
 		e.entity = living;
 		e.blockEventType = BlockEventType.PLAYER_BLOCK_PLACED;
 		
 		fireBlockEvent(world, e);
 	}
 	
 	public void onBlockDestroyed(World world, int x, int y, int z) {
 		
 		BlockEvent e = new BlockEvent();
 		e.block = world.getBlockAt(x, y, z);
 		e.blockEventType = BlockEventType.BLOCK_DESTROYED;
 		
 		fireBlockEvent(world, e);
 	}
 	
 	
 	public boolean onBlockInteract(World world, int x, int y, int z, SpoutPlayer player) {
 		
 		BlockEvent e = new BlockEvent();
 		e.block = world.getBlockAt(x, y, z);
 		e.player = player;
 		e.blockEventType = BlockEventType.PLAYER_BLOCK_PLACED;
 		
 		fireBlockEvent(world, e);
 	
 		return true;
 		
 	}
 	
 	private void fireBlockEvent(final World world, final BlockEvent blockEvent) {
 		for(BlockObserver o : observers.get(world)) {
 			o.onBlockEvent(blockEvent);
 		}
 	}
 	
 	public void addObserver(final BlockObserver blockObserver, final World world) {
 		observers.put(world, blockObserver);
 	}
 	
 	public void removeObserver(final BlockObserver blockObserver, final World world) {
 		observers.remove(world, blockObserver);
 	}
 
 	public void setCustomBlockType(CustomBlockType customBlockType) {
 		this.customBlockType = customBlockType;
 	}
 	
 	public CustomBlockType getCustomBlockType() {
 		return customBlockType;
 	}
 }
