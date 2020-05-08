 package com.gmail.zariust.otherbounds.boundary;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 
 import com.gmail.zariust.otherbounds.Log;
 import com.gmail.zariust.otherbounds.OtherBoundsConfig;
 import com.gmail.zariust.otherbounds.parameters.actions.Action;
 
 public abstract class Boundary {
     public String name;
 
     public double centerX;
     public double centerZ;
     
     public boolean invertLimits = false; // this will cause damage to apply if inside the limits, if true
     public int damage;
     public int damageRate;
     public Map<String, Boolean> worlds;
     public List<String> except; // list of players that are not effected by this boundary
     public List<String> exceptPermissions; // list of permissions (or groups) that are not effected by this boundary
 
     public String dangerMessage;
     public String safeMessage;
 
     public final List<Action> actions = new ArrayList<Action>();
     
  //   public List<Action> actions;
     
     public Boundary() {
 
     }
     
     abstract public boolean isInside(Player player, Boundary boundary);
 
     
 
 	public static Boundary parseFrom(String name, ConfigurationSection node) {
 		Log.high("Parsing boundary ("+name+") keys:"+node.getKeys(true).toString());
 		String regionName = node.getString("region");
 		Double radius = node.getDouble("radius", 0);
 		Double centerX = node.getDouble("center-x", 0);
		if (node.getString("center-x") == null) centerX = node.getDouble("centre-x", 0);
		Double centerZ = node.getDouble("center-z", 0);
		if (node.getString("center-x") == null) centerZ = node.getDouble("centre-z", 0);
 
 		//Action.parseNodes(node);
 		
 		Boundary boundary;
 		if (regionName != null) { // region
 			boundary = new RegionBound(regionName);
 		} else if (radius != null) { // Circle
 			boundary = new CircleBound(centerX, centerZ, radius);
 		} else { // assume rectangle
 			Integer length = node.getInt("length", 0);
 			Integer width = node.getInt("width", 0);
 			boundary = new RectangleBound(centerX, centerZ, length, width);
 
 		}
 /*
 	    world: ALL
 	    damage: 1
 	    except: [player1, player2]
 	    exceptpermissions: [main_boundary_override]  # give players otherbounds.custom.main_boundary_override
 */
 		
 		boundary.except = getMaybeList(node, "except");
 		boundary.exceptPermissions = getMaybeList(node, "exceptpermissions");
 		boundary.worlds = OtherBoundsConfig.parseWorldsFrom(node, null);
 		boundary.name = name;
 		boundary.damage = node.getInt("damage", 0);
 		boundary.invertLimits = node.getBoolean("invertlimits", false);
 		boundary.safeMessage = node.getString("messagesafe", "");
 		boundary.dangerMessage = node.getString("messagedanger");
 		
 		Log.dMsg("Adding the actions.");
         boundary.addActions(Action.parseNodes(node));
 
 
 		Log.normal("Loaded boundary ("+name+"): "+boundary.toString());
 		
 		return boundary;
 	}
 	
     public void addActions(List<Action> parse) {
         if (parse != null)
             this.actions .addAll(parse);
     }
 
 	public static List<String> getMaybeList(ConfigurationSection node, String... keys) {
 		if(node == null) return new ArrayList<String>();
 		Object prop = null;
 		String key = null;
 		for (int i = 0; i < keys.length; i++) {
 			key = keys[i];
 			prop = node.get(key);
 			if(prop != null) break;
 		}
 		List<String> list;
 		if(prop == null) return new ArrayList<String>();
 		else if(prop instanceof List) list = node.getStringList(key);
 		else list = Collections.singletonList(prop.toString());
 		return list;
 	}
 	@Override
 	abstract public String toString();
 
 }
