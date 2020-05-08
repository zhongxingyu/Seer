 package gt.general.trigger;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 
 /**
  * uses a minecraft lever or stone button as trigger
  * 
  * @author roman
  */
 public class AttachableRedstoneTrigger extends RedstoneTrigger {
 
 	private Block trigger;
 	
 	private Material material;
 	
 	/**
 	 * @param trigger the lever to be used as trigger
 	 */
 	public AttachableRedstoneTrigger(final Block trigger) {
 		super("attachable_trigger");
 		
 		this.trigger = trigger;
 		material = trigger.getType();
 		
 		setLabel("_"+ hashCode());
 	}
 	
 	
 	@Override
 	public void setup(final Map<String, Object> values, final World world) {
 		super.setup(values, world);
 
 		material = (Material) values.get("material");
 		
 		trigger = blockFromCoordinates(values, world);
 		trigger.setType(material);
 	}
 	
 	@Override
 	public void dispose() {
 		super.dispose();
 		
 		trigger.setType(Material.AIR);
 	}
 	
 	@Override
 	public Map<String, Object> dump() {
 		Map<String, Object> map = new HashMap<String,Object>();
 		
 
 		map.putAll(coordinatesFromPoint(trigger));
 		map.put("material", material);
 
 		return map;
 	}
 
 	@Override
 	protected Block getBlock() {
 		return trigger;
 	}
 
 }
