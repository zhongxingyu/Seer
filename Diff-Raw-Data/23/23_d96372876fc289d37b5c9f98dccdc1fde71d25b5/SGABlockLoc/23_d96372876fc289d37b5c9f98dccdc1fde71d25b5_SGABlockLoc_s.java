 package net.yeticraft.xxtraineexx.sgadvanced;
 
 import java.util.HashMap;
import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.MemoryConfiguration;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.configuration.serialization.SerializableAs;
 
 
 @SerializableAs("blockLocs")
 public class SGABlockLoc implements ConfigurationSerializable{
 
 	@SuppressWarnings("unused")
 	private final static long serialVersionUID = 1L;
 	private String world;
 	private double x;
 	private double y;
 	private double z;
 	private int typeId;
 	private byte data;
 	
 	
 	public SGABlockLoc(World world, Double x, Double y, Double z, int typeId, byte data) {
         this.world = world.getName();
         this.x = x;
         this.y = y;
         this.z = z;
         this.typeId = typeId;
         this.data = data;
     }
 	
 	public SGABlockLoc(Block block) {
         this.world = block.getLocation().getWorld().getName();
         this.x = block.getLocation().getX();
         this.y = block.getLocation().getY();
         this.z = block.getLocation().getZ();
         this.typeId = block.getTypeId();
         this.data = block.getData();
     }
 	
     public SGABlockLoc(SGABlockLoc block) {
         this.world = block.world;
         this.x = block.x;
         this.y = block.y;
         this.z = block.z;
         this.typeId = block.typeId;
         this.data = block.data;
     }	
     
     public SGABlockLoc(ConfigurationSection map) {
         this.world = map.getString("world");
         this.x = map.getDouble("x");
         this.y = map.getDouble("y");
         this.z = map.getDouble("z");     
         this.typeId = map.getInt("typeId");
        List<Byte> tempByteList = map.getByteList("data");
        //TODO: This is a problem. The 0 shows up as an index out of bounds. Good times.
        this.data = tempByteList.get(0);
     }
  
     public int getTypeId(){
         return typeId;
     }
     
     public byte getData(){
         return data;
     }
     
     public Location toLocation() {
         Location l = new Location(Bukkit.getWorld(world), x, y, z);
         return l;
     }
     
     public String toString(){
     	
     	return world + " [" + Double.toString(x) + "," + Double.toString(y) + ","  + Double.toString(z) + "]";
     }
  
     @Override
     public Map<String, Object> serialize() {
         Map<String, Object> map = new HashMap<String, Object>();
  
         map.put("world", world);
         map.put("x", x);
         map.put("y", y);
         map.put("z", z);
         map.put("blockTypeId", typeId);
        map.put("data", data);
  
         return map;
     }
  
     public static SGABlockLoc deserialize(Map<String, Object> map) {
         Configuration conf = new MemoryConfiguration();
         for (Entry<String, Object> e : map.entrySet()) {
             conf.set(e.getKey(), e.getValue());
         }
         return new SGABlockLoc(conf);
     }
 	
 	
 }
