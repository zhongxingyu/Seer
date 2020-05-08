 package net.dmulloy2.autocraft.types;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import lombok.Data;
 import net.dmulloy2.autocraft.util.MaterialUtil;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 
 /**
  * @author dmulloy2
  */
 
 @Data
 public class ShipData implements ConfigurationSerializable {
 	private transient int fastFlyAtSize = 1000;
 	private transient int maxAltitude = 254;
 	private transient int minAltitude = 2;
 
 	private transient String shipType;
 
 	private String mainType;
 	private String cannonMaterial;
 
 	private int moveSpeed;
 	private int maxBlocks;
 	private int minBlocks;
 	private int maxCannonLength;
 	private int maxShipDimensions;
 	private int maxNumberOfCannons;
 
 	private boolean firesTnt;
 	private boolean dropsBomb;
 	private boolean dropsNapalm;
 	private boolean firesTorpedo;
 	private boolean needsPermission;
 	private boolean ignoreAttachments;
 
	// Not exactly ideal, but basically, since integers cant be casted to strings
	// for whatever reason, allowedBlocks is the old integer list, which we will
	// convert into string form and save it as allowedTypes. The allowedBlocks
	// list will load as normal, but will not save, since it is transient. The
	// allowedTypes list will save instead.
	private List<String> allowedTypes = new ArrayList<String>();
	private transient List<Integer> allowedBlocks = new ArrayList<Integer>();
 
 	public ShipData() {
 
 	}
 
 	public ShipData(Map<String, Object> args) {
 		for (Entry<String, Object> entry : args.entrySet()) {
 			try {
 				for (Field field : getClass().getDeclaredFields()) {
 					if (field.getName().equals(entry.getKey())) {
 						boolean accessible = field.isAccessible();
 
 						field.setAccessible(true);
 
 						field.set(this, entry.getValue());
 
 						field.setAccessible(accessible);
 					}
 				}
 			} catch (Throwable ex) {
 			}
 		}

		convertToStringList();
 	}
 
 	@Override
 	public Map<String, Object> serialize() {
 		Map<String, Object> data = new HashMap<String, Object>();
 
 		for (Field field : getClass().getDeclaredFields()) {
 			if (Modifier.isTransient(field.getModifiers()))
 				continue;
 
 			try {
 				boolean accessible = field.isAccessible();
 
 				field.setAccessible(true);
 
 				if (field.getType().equals(Integer.TYPE)) {
 					data.put(field.getName(), field.getInt(this));
 				} else if (field.getType().equals(Long.TYPE)) {
 					data.put(field.getName(), field.getLong(this));
 				} else if (field.getType().equals(Boolean.TYPE)) {
 					data.put(field.getName(), field.getBoolean(this));
 				} else if (field.getType().isAssignableFrom(Collection.class)) {
 					data.put(field.getName(), field.get(this));
 				} else if (field.getType().isAssignableFrom(String.class)) {
 					data.put(field.getName(), field.get(this));
 				} else if (field.getType().isAssignableFrom(Map.class)) {
 					data.put(field.getName(), field.get(this));
 				} else {
 					data.put(field.getName(), field.get(this));
 				}
 
 				field.setAccessible(accessible);
 			} catch (Throwable ex) {
 			}
 		}
 
 		return data;
 	}
 
	private final void convertToStringList() {
		if (! allowedBlocks.isEmpty()) {
			for (int id : allowedBlocks) {
				allowedTypes.add(Integer.toString(id));
			}

			allowedBlocks.clear();
		}
	}

 	public boolean isValidMaterial(Block block) {
		for (String allowed : allowedTypes) {
 			if (block.getType() == MaterialUtil.getMaterial(allowed))
 				return true;
 		}
 
 		return block.getType() == MaterialUtil.getMaterial(mainType) 
 				|| block.getType() == MaterialUtil.getMaterial(cannonMaterial)
 				|| block.getType() == Material.DISPENSER;
 	}
 }
