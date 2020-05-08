 package gt.general.trigger.response;
 
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Effect;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.material.Door;
 
 public class DoorResponse extends BlockResponse {
 	
 	public DoorResponse(Block doorBlock) {
 		super("door", doorBlock);
 	}
 
 	public DoorResponse() {
 	}
 
 	@Override
 	public void setup(final Map<String, Object> values, final World world) {
 		
 		material = (Material) values.get("material");
 		//TODO: do we need the orientation here?
 		
 		block = blockFromCoordinates(values, world);
 		block.setType(material);
 	}
 
 
 	@Override
 	public void triggered(final boolean active) {
 
 		Door door = (Door) block.getState().getData();
 
 		door.setOpen(active);
 		// play the door toggle sound
 		block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 10); // we can set the radius here
 		
 		block.setData(door.getData(), true);
 	}
 	
 
 	@Override
 	public void  dispose() {
 		block.setType(Material.AIR);
 	}
 
 	@Override
 	public Map<String, Object> dump() {
 		Map<String, Object> map = new HashMap<String,Object>();
 		
 		map.putAll(coordinatesFromBlock(block));
 		map.put("material", material);
 		
 		Door door = (Door) block.getState().getData();
 		map.put("orientation", door.getFacing());
 		
 		return map;
 	}
 
 }
