 package gt.general.trigger.response;
 
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Effect;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.material.Door;
 
 public class DoorResponse extends Response {
 
 	private Block doorBlock;	
 	private Material material;
 	
 	public DoorResponse(Block doorBlock) {
 		super("door");
 		this.doorBlock = doorBlock;
 	}
 
 	public DoorResponse() {
 	}
 
 	@Override
 	public void setup(final Map<String, Object> values, final World world) {
 		
 		material = (Material) values.get("material");
 		//TODO: do we need the orientation here?
 		
 		doorBlock = blockFromCoordinates(values, world);
 		doorBlock.setType(material);
 	}
 
 
 	@Override
 	public void triggered(final boolean active) {
 
 		Door door = (Door) doorBlock.getState().getData();
 		Block otherhalf;
 		
 		if (door.isTopHalf()) {
 			otherhalf = doorBlock.getRelative(BlockFace.DOWN);			
 		} else {
 			otherhalf = doorBlock.getRelative(BlockFace.UP);
 		}		
 
 		door.setOpen(active);
 		// play the door toggle sound
 		doorBlock.getWorld().playEffect(doorBlock.getLocation(), Effect.DOOR_TOGGLE, 10); // we can set the radius here
 		
 		doorBlock.setData(door.getData(), true);
 		door.setTopHalf(!door.isTopHalf());
 		otherhalf.setData(door.getData(), true);
 
 	}
 	
 
 	@Override
 	public void  dispose() {
 		doorBlock.setType(Material.AIR);
 	}
 
 	@Override
 	public Map<String, Object> dump() {
 		Map<String, Object> map = new HashMap<String,Object>();
 		
 		map.putAll(coordinatesFromPoint(doorBlock));
 		map.put("material", material);
 		
 		Door door = (Door) doorBlock.getState().getData();
 		map.put("orientation", door.getFacing());
 		
 		return map;
 	}
 
 }
