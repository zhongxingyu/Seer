 package team.GunsPlus.Manager;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 //import net.morematerials.morematerials.materials.SMCustomItem;
 
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 //import org.getspout.spoutapi.material.CustomBlock;
 
 import team.GunsPlus.Enum.Effect;
 import team.GunsPlus.Enum.EffectSection;
 import team.GunsPlus.Enum.EffectType;
 import team.GunsPlus.Enum.KeyType;
 import team.GunsPlus.Item.Addition;
 import team.GunsPlus.Item.Ammo;
 import team.GunsPlus.Item.Gun;
 import team.GunsPlus.Util.Util;
 import team.GunsPlus.GunsPlus;
 
 public class ConfigParser {
 
 	public static List<ItemStack> parseItems(String s){
         List<ItemStack> result = new LinkedList<ItemStack>();
         
         String[] items = s.split(",");
         for (String item : items){
             ItemStack mat = parseItem(item.trim());
             if (mat != null)
                 result.add(mat);
         }
         
         return result;
     }
     
     public static ItemStack parseItem(String item){
         if (item == null || item.equals(""))
             return null;
         
         String[] parts = item.split(":");
         if (parts.length == 1)
             return singleItem(parts[0]);
         if (parts.length == 2)
             return withDurability(parts[0], parts[1]);
         if(parts.length == 3)
         	return withAmount(parts[0], parts[1], parts[2]);
         
         return null;
     }
     
     private static ItemStack singleItem(String item){
     	SpoutItemStack custom = null;
         Material m = getMaterial(item);
         if(m==null){
 			for(Ammo a:GunsPlus.allAmmo){
 				if(a.getName().toString().equals(item)){
 					custom = new SpoutItemStack(a);
 				}
 			}
 			for(Gun g:GunsPlus.allGuns){
 				if(g.getName().toString().equals(item)){
 					custom = new SpoutItemStack(g);
 				}
 			}
 			for(Addition a : GunsPlus.allAdditions){
 				if(a.getName().toString().equals(item)){
 					custom = new SpoutItemStack(a);
 				}
 			}
         }
         if(custom==null){
         	if(m==null){
         		return null;
         	}else{
         		return new ItemStack(m);
         	}
         }else{
         	return new SpoutItemStack(custom);
         }
     }
     
     private static ItemStack withDurability(String item, String durab){
     	Material m = getMaterial(item);
         if (m == null)
             return null;
         SpoutItemStack sis = new SpoutItemStack(new ItemStack(m));
         if(durab.matches("[0-9]+")){
         	sis.setDurability(Short.parseShort(durab));
         }
         
         return sis;
     }
     
     private static ItemStack withAmount(String item, String durab, String amount){
     	Material m = getMaterial(item);
         if (m == null)
             return null;
         SpoutItemStack sis = new SpoutItemStack(new ItemStack(m, Integer.parseInt(amount)));
         if(durab.matches("[0-9]+")){
         	sis.setDurability(Short.parseShort(durab));
         }
         
         return sis;
     }
     
     private static Material getMaterial(String item){
         if (item.matches("[0-9]*"))
             return Material.getMaterial(Integer.parseInt(item));
         
         return Material.getMaterial(item.toUpperCase());
     }
     
     public static KeyType getKeyType(String string){
     	KeyType key = null;
     	if (string.startsWith("@")) {
 			if (string.endsWith("_"))
 				key = KeyType.HOLDLETTER(string.replace("@", ""));
 			else
 				key = KeyType.LETTER(string.replace("@", ""));
 		} else if (string.startsWith("#")) {
 			if (string.endsWith("_"))
 				key = KeyType.HOLDNUMBER(string.replace("#", ""));
 			else
 				key = KeyType.NUMBER(string.replace("#", ""));
 		} else {
 			if (string.endsWith("_")) {
 				switch (KeyType.getType(string)) {
 				case RIGHT:
 					key = KeyType.HOLDRIGHT;
 					break;
 				case LEFT:
 					key = KeyType.HOLDLEFT;
 					break;
 				case RIGHTSHIFT:
 					key = KeyType.HOLDRIGHTSHIFT;
 					break;
 				case LEFTSHIFT:
 					key = KeyType.HOLDLEFTSHIFT;
 					break;
 				}
 			} else {
 				switch (KeyType.getType(string)) {
 				case RIGHT:
 					key = KeyType.RIGHT;
 					break;
 				case LEFT:
 					key = KeyType.LEFT;
 					break;
 				case RIGHTSHIFT:
 					key = KeyType.RIGHTSHIFT;
 					break;
 				case LEFTSHIFT:
 					key = KeyType.LEFTSHIFT;
 					break;
 				}
 			}
 		}
     	return key;
     }
     
     public static List<Effect> getEffects(String path){
     	List<Effect> effects = new ArrayList<Effect>();
     	if(!GunsPlus.gunsConfig.isConfigurationSection(path)||GunsPlus.gunsConfig.getConfigurationSection(path).getKeys(false).isEmpty()) return effects;
     	for(String effectsection: GunsPlus.gunsConfig.getConfigurationSection(path).getKeys(false)){
     		EffectSection effsec = EffectSection.valueOf(effectsection.toUpperCase());
     		for(String effecttype : GunsPlus.gunsConfig.getConfigurationSection(path+"."+effectsection).getKeys(false)){
     			EffectType efftyp = EffectType.valueOf(effecttype.toUpperCase());
     			if(Util.isAllowedInEffectSection(efftyp, effsec)){
     				effects.add(buildEffect(efftyp, effsec, path+"."+effectsection+"."+effecttype));
     			}
     		}
     	}
     	return effects;
     }
     
     private static Effect buildEffect(EffectType efftyp, EffectSection es, String path){
     		Effect e = new Effect(efftyp, es);
     		switch(efftyp){
 		    	case EXPLOSION:
 		    		e.addArgument("SIZE", GunsPlus.gunsConfig.getInt(path+".size"));
 		    		break;
 		    	case LIGHTNING:
 		    		break;
 		    	case SMOKE:
 		    		e.addArgument("DENSITY", GunsPlus.gunsConfig.getInt(path+".density"));
 		    		break;
 		    	case FIRE:
 		    		if(es.equals(EffectSection.SHOOTER)||es.equals(EffectSection.TARGETENTITY))
 		    			e.addArgument("DURATION", GunsPlus.gunsConfig.getInt(path+".duration"));
 		    		else
 		    			e.addArgument("STRENGTH", GunsPlus.gunsConfig.getInt(path+".strength"));
 		    		break;
 		    	case PUSH:
 		    		e.addArgument("SPEED", GunsPlus.gunsConfig.getDouble(path+".speed"));
 		    		break;
 		    	case DRAW:
 		    		e.addArgument("SPEED", GunsPlus.gunsConfig.getDouble(path+".speed"));
 		    		break;
 		    	case POTION:
 		    		e.addArgument("ID", GunsPlus.gunsConfig.getInt(path+".id"));
 		    		e.addArgument("DURATION", GunsPlus.gunsConfig.getInt(path+".duration"));
 		    		e.addArgument("STRENGTH", GunsPlus.gunsConfig.getInt(path+".strength"));
 		    		break;
 		    	case SPAWN:
 		    		e.addArgument("ENTITY", GunsPlus.gunsConfig.getString(path+".entity"));
 		    		break;
 		    	case PLACE:
 		    		e.addArgument("BLOCK", GunsPlus.gunsConfig.getInt(path+".block"));
 		    		break;
 		    	case BREAK:
 		    		e.addArgument("POTENCY", GunsPlus.gunsConfig.getDouble(path+".potency"));
 		    		break;
     	}
     	return e;
     }
     
     public static ArrayList<Addition> getAdditions(String path){
     	ArrayList<Addition> adds = new ArrayList<Addition>();
     	String string = GunsPlus.gunsConfig.getString(path);
     	if(string!=null){
 	    	String[] split = string.split(",");
 	    	for(String splitString : split){
 	    		for(Addition a : GunsPlus.allAdditions){
 		    		if(a.getName().equalsIgnoreCase(splitString.trim())){
 		    			adds.add(a);
 		    		}
 		    	}
 	    	}
     	}
     	return adds;
     }
 }
