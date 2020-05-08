 package gt.general.logic.trigger;
 
 import gt.general.logic.persistance.PersistanceMap;
 import gt.general.logic.persistance.exceptions.PersistanceException;
 import gt.general.world.ObservableCustomBlock;
 import gt.plugin.meta.CustomBlockType;
 
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.material.Lever;
 import org.getspout.spoutapi.SpoutManager;
 
 /**
  * uses a minecraft lever or stone button as trigger
  * 
  * @author roman
  */
 public class LeverRedstoneTrigger extends RedstoneTrigger implements Listener {
 	
 	public static final String KEY_ORIENTATION = "orientation";
 	
 	private BlockFace orientation;
 	
 	/**
 	 * @param trigger the lever to be used as trigger
 	 * @param against against which block the player placed the trigger
 	 */
 	public LeverRedstoneTrigger(final Block trigger, final Block against) {
		super("lever_trigger_", trigger);
 		
 		orientation = against.getFace(trigger);
 		
 		installSignal();
 	}
 	
 	/** to be used for persistence */
 	public LeverRedstoneTrigger() {}
 	
 	@Override
 	public void setup(final PersistanceMap values, final World world) throws PersistanceException {
 		super.setup(values, world);
 		
 		orientation = values.get(KEY_ORIENTATION);
 
 		Lever lever = (Lever) getBlock().getState().getData();
 		lever.setFacingDirection(orientation);
 		
 		getBlock().setData(lever.getData());
 		
 		installSignal();
 	}
 	
 	@Override
 	public PersistanceMap dump() {
 		PersistanceMap map = super.dump();
 
 		map.put(KEY_ORIENTATION, orientation);
 
 		return map;
 	}
 	
 	@Override
 	public void dispose() {
 		super.dispose();
 		SpoutManager
 			.getMaterialManager()
 			.removeBlockOverride(getBlock()
 									.getRelative(orientation.getOppositeFace()));
 	}
 	
 	/**
 	 * installs the signal behind the trigger
 	 */
 	private void installSignal() {		
 		Block signalBlock = getBlock().getRelative(orientation.getOppositeFace());
 		ObservableCustomBlock red = CustomBlockType.RED_SIGNAL.getCustomBlock();
 		
 		SpoutManager.getMaterialManager().overrideBlock(signalBlock, red);
 	}
 	
 	@EventHandler
 	@Override
 	public void onBlockRedstoneChange(final BlockRedstoneEvent event) {
 		if(isBlockRedstoneEventHere(event)) {
 			super.onBlockRedstoneChange(event);
 			
 			Block signalBlock = getBlock().getRelative(orientation.getOppositeFace());
 			
 			if(event.getNewCurrent() > 0) {
 				ObservableCustomBlock green = CustomBlockType.GREEN_SIGNAL.getCustomBlock();
 				SpoutManager.getMaterialManager().overrideBlock(signalBlock, green);
 			} else {
 				ObservableCustomBlock red = CustomBlockType.RED_SIGNAL.getCustomBlock();
 				SpoutManager.getMaterialManager().overrideBlock(signalBlock, red);
 			}
 		}
 	}
 }
