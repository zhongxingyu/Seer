 package com.wolvencraft.prison.mines.upgrade;
  
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.configuration.serialization.SerializableAs;
 import org.bukkit.material.MaterialData;
 
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.mine.MineBlock;
 import com.wolvencraft.prison.region.PrisonRegion;
 import com.wolvencraft.prison.util.Message;
  
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
  
 /**
  * Here lies the mine object code as written by jjkoletar in MineResetLite 0.3.2<br />
  * I, bitWolfy, do not claim ownership of this code. If you wish to have it removed, contact me immediately.
  * @author jjkoletar
  */
 
 @SerializableAs("MRLMine")
 public class MRLMine implements ConfigurationSerializable {
     private int minX;
     private int minY;
     private int minZ;
     private int maxX;
     private int maxY;
     private int maxZ;
     private World world;
     private Map<SerializableBlock, Double> composition;
     private long resetTime;
     private int resetDelay;
     private List<Integer> resetWarnings;
     private String name;
     private SerializableBlock surface;
     private boolean fillMode;
  
     public MRLMine(Map<String, Object> me) {
         try {
             minX = (Integer) me.get("minX");
             minY = (Integer) me.get("minY");
             minZ = (Integer) me.get("minZ");
             maxX = (Integer) me.get("maxX");
             maxY = (Integer) me.get("maxY");
             maxZ = (Integer) me.get("maxZ");
         } catch (Throwable t) {
             throw new IllegalArgumentException("Error deserializing coordinate pairs");
         }
         try {
             world = Bukkit.getServer().getWorld((String) me.get("world"));
         } catch (Throwable t) {
             throw new IllegalArgumentException("Error finding world");
         }
         try {
             @SuppressWarnings("unchecked")
 			Map<String, Double> sComposition = (Map<String, Double>) me.get("composition");
             composition = new HashMap<SerializableBlock, Double>();
             for (Map.Entry<String, Double> entry : sComposition.entrySet()) {
                 composition.put(new SerializableBlock(entry.getKey()), entry.getValue());
             }
         } catch (Throwable t) {
             throw new IllegalArgumentException("Error deserializing composition");
         }
         name = (String) me.get("name");
         resetTime = Long.valueOf(me.get("resetTime").toString());
         resetDelay = (Integer) me.get("resetDelay");
         @SuppressWarnings("unchecked")
 		List<String> warnings = (List<String>) me.get("resetWarnings");
         resetWarnings = new LinkedList<Integer>();
         for (String warning : warnings) {
             try {
                 resetWarnings.add(Integer.valueOf(warning));
             } catch (NumberFormatException nfe) {
                 throw new IllegalArgumentException("Non-numeric reset warnings supplied");
             }
         }
         if (me.containsKey("surface")) {
             if (!me.get("surface").equals("")) {
                 surface = new SerializableBlock((String) me.get("surface"));
             }
         }
         if (me.containsKey("fillMode")) {
             fillMode = (Boolean) me.get("fillMode");
         }
     }
  
     public Map<String, Object> serialize() {
         Map<String, Object> me = new HashMap<String, Object>();
         me.put("minX", minX);
         me.put("minY", minY);
         me.put("minZ", minZ);
         me.put("maxX", maxX);
         me.put("maxY", maxY);
         me.put("maxZ", maxZ);
         me.put("world", world.getName());
         //Make string form of composition
         Map<String, Double> sComposition = new HashMap<String, Double>();
         for (Map.Entry<SerializableBlock, Double> entry : composition.entrySet()) {
             sComposition.put(entry.getKey().toString(), entry.getValue());
         }
         me.put("composition", sComposition);
         me.put("name", name);
         me.put("resetTime", resetTime);
         me.put("resetDelay", resetDelay);
         List<String> warnings = new LinkedList<String>();
         for (Integer warning : resetWarnings) {
             warnings.add(warning.toString());
         }
         me.put("resetWarnings", warnings);
         if (surface != null) {
             me.put("surface", surface.toString());
         } else {
             me.put("surface", "");
         }
         me.put("fillMode", fillMode);
         return me;
     }
     
 	public Mine importMine() {
     	Location one = new Location(world, minX, minY, minZ);
     	Location two = new Location(world, maxX, maxY, maxZ);
     	PrisonRegion region = new PrisonRegion(one, two);
     	Mine mine = new Mine(name, region, world, two, "RANDOM");
     	
     	Iterator<Entry<SerializableBlock, Double>> it = composition.entrySet().iterator();
     	double totalValue = 0;
         List<MineBlock> blocks = mine.getBlocks();
        blocks.remove(new MineBlock(new MaterialData(Material.AIR), 1.0));
         while (it.hasNext()) {
 			Map.Entry<SerializableBlock, Double> pairs = (Map.Entry<SerializableBlock, Double>) it.next();
             blocks.add(new MineBlock(new MaterialData(pairs.getKey().getBlockId()), pairs.getValue()));
             totalValue += pairs.getValue().doubleValue();
             it.remove();
         }
         
         if(totalValue < 1) {
         	blocks.add(new MineBlock(new MaterialData(Material.AIR), (1.0 - totalValue)));
         }
         
         
         if(resetDelay != 0) {
         	mine.setAutomaticReset(true);
         	mine.setResetPeriod(resetDelay * 60);
         }
         
         for(Integer warning : resetWarnings) {
         	mine.getWarningTimes().add(warning);
         }
         
         mine.save();
         Message.log("Imported mine from MineResetLite: " + name);
     	return mine;
     }
 }
