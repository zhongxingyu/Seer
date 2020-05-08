 package org.sankozi.rogueland.model;
 
 import java.util.EnumMap;
 import java.util.EnumSet;
 import org.sankozi.rogueland.model.Destroyable.Param;
 
 import static com.google.common.base.Preconditions.*;
 /**
  * Immutable object representing various parameters and characteristics of an item;
  * @author sankozi
  */
 public final class ItemTemplate {
 	private final String name;
     private final String description;
 	/** Destroyable parameters of an item */
     private final EnumMap<Param, Float> params;
 	/** Effect of an item when it is worn or wield; can be null if types 
 	 *  doesn't contain any of above */ 
 	private final Effect effect;
 
 	private final EnumSet<ItemType> types;
 
 	public ItemTemplate(String name, String description, EnumMap<Param, Float> params, Effect effect, EnumSet<ItemType> types) {
         checkArgument(params.containsKey(Param.MAX_DURABILITY), "params %s does not contain MAX_DURABILITY", params);
 		this.name = name;
        this.description = description;
 		this.params = checkNotNull(params, "params cannot be null");
 		this.effect = checkNotNull(effect, "effect cannot be null");
 		this.types = checkNotNull(types, "types cannot be null");
 	}
 
 	float destroyableParam(Param param) {
 		return params.get(param);
 	}
 
 	Iterable<ItemType> getTypes() {
 		return types;
 	}
 
 	String getName() {
 		return name;
 	}
 
     Effect getEffect() {
         return effect;
     }
 
     String getDescription() {
         return description;
     }
 }
