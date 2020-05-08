 package me.asofold.bukkit.fattnt.config;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.util.Vector;
 
 /**
  * Default settings and values:
  * TODO: for simply members: move values back to Settings and use newSettings() to get the default value ! [spares double defs] 
  * @author mc_dev
  *
  */
 public class Defaults {
 	
 	/**
 	 * To put in front of messages.
 	 */
 	public static final String msgPrefix = "[FatTnt] ";
 
 	// -----------------------------------------------
 	// config paths ----------------------------------
 	
 	// multipliers:
 	public static final String cfgMult = "multiplier";
 	public static final String cfgMultRadius = cfgMult+".radius";
 	public static final String cfgMultDamage = cfgMult+".damage";
 	public static final String cfgMultMaxPath = cfgMult+".max-path";
 	public static final String cfgMultProjectiles = cfgMult+".projectiles";
 	public static final String cfgFStraight = cfgMult+".straight";
 	
 	// explosion/propagation settings:
 	public static final String cfgPassthrough= "passthrough";
	public static final String cfgDefaultPassthrough= cfgPassthrough+".default";
 	public static final String cfgResistence = "resistence";
	public static final String cfgDefaultResistence = cfgResistence+".default";
	public static final String cfgRadius = "radius";
	public static final String cfgMaxRadius = cfgRadius+".max";
	public static final String cfgRandRadius = cfgRadius+".random"; // UNUSED
 	public static final String cfgEntities= "entities";
 	
 	// block effects:
 	public static final String cfgYield = "yield";
 	public static final String cfgStepPhysics= "step-physics";
 	
 	// tnt specific
 	public static final String cfgMinPrime = "min-prime";
 	public static final String cfgMaxPrime = "max-prime";
 	public static final String cfgThresholdTntDirect = "tnt.thresholds.direct-explode"; // UNUSED
 	
 	// entity effects:
 	public static final String cfgEntityYield = "entity-yield";
 	public static final String cfgItemTnt = "item-tnt";
 	public static final String cfgItemArrows = "item-arrows";
 	public static final String cfgMaxItems = "max-items";
 	public static final String cfgProjectiles = "projectiles";
 
 	// velocity:
 	public static final String cfgVel = "velocity";
 	public static final String cfgVelUse = cfgVel+".use";
 	public static final String cfgVelMin = cfgVel+".min";
 	public static final String cfgVelCen= cfgVel+".center";
 	public static final String cfgVelRan = cfgVel+".random";
 	public static final String cfgVelOnPrime = cfgVel+".tnt-primed";
 	public static final String cfgVelCap = cfgVel+".cap";
 
 	
 	
 	// -------------------------------------------------------------------------------
 	
 	// Block id presets (resistance) -------------------------------------------------
 	
 	public static final int[] defaultIgnoreBlocks = new int[]{
 	//			7, // bedrock
 				8,9, // water
 				10,11, // lava
 	//			49,90, // obsidian/nether portal
 	//			119,120 // end portal / frame
 				};
 	public static final int[] defaultLowResistance = new int[]{
 			0, // air
 			8, 18, 30, 31, 32, 37,38, 39, 40, 50, 51, 55,
 			59,	63, 75,76, 78, 83, 102, 104, 105, 106, 111,
 	};
 	public static final int[] defaultHigherResistance = new int[]{
 			23, 41,42, 45, 54, 57, 95,
 			98, 108, 109
 	};
 	public static final int[] defaultStrongResistance = new int[]{
 			49, 116, 
 	};
 	public static final int[] defaultMaxResistance = new int[]{
 			7, // bedrock
 	};
 	
 	// entitiy presets -------------------------------------------------
 	public static final String[] handledEntities = new String[]{
 		"PRIMED_TNT",
 	};
 	
 	// some default settings ------------------------------------
 	
 	/**
 	 * Maximum explosion strength that will be accepted by config.
 	 */
 	public static final float radiusLock = 100.0f;
 	
 	/**
 	 * Center of a block (for addition to a block coordinate).
 	 */
 	public static final Vector vCenter = new Vector(0.5,0.5,0.5);
 	
 	/**
 	 * Maximum size of entity id arrays.
 	 */
 	public static final int blockArraySize = 4096; 
 	
 	
 	
 	/**
 	 * 
 	 * @param cfg
 	 * @return If changes were done.
 	 */
 	public static boolean addDefaultSettings(FileConfiguration cfg) {
 		boolean changed = false;
 		Settings defaults = new Settings(null); // read defaults from here.
 		
 		if ( !cfg.contains(cfgEntities)){
 			List<String> l = new LinkedList<String>();
 			for (String et : handledEntities){
 				l.add(et);
 			}
 			cfg.set(cfgEntities, l);
 			changed = true;
 		}
		if ( !cfg.contains(cfgDefaultPassthrough)){
			cfg.set(cfgDefaultPassthrough, defaults.defaultPassthrough);
 			changed = true;
 		}
 		// no default ids for passthrough
 		if ( !cfg.contains(cfgResistence)){
 			float[] v = new float[]{1.0f, 4.0f, 20.0f, Float.MAX_VALUE};
 			int[][] ids = new int[][]{defaultLowResistance, defaultHigherResistance, defaultStrongResistance, defaultMaxResistance};
 			String[] keys = new String[]{"low", "higher", "strongest", "indestructible"};
 			for ( int i = 0; i<v.length; i++){
 				String base = cfgResistence+"."+keys[i];
 				List<Integer> l = new LinkedList<Integer>();
 				for ( int id: ids[i]) {
 					l.add(id);
 				}
 				cfg.set(base+".value", v[i]);
 				cfg.set(base+".ids", l);
 			}
 			changed = true;
 		}
 		if ( !cfg.contains(cfgDefaultResistence)){
 			cfg.set(cfgDefaultResistence, defaults.defaultResistance);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgMaxRadius)){
 			cfg.set(cfgMaxRadius, defaults.maxRadius);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgMultDamage)){
 			cfg.set(cfgMultDamage, defaults.damageMultiplier);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgMultRadius)){
 			cfg.set(cfgMultRadius, defaults.radiusMultiplier);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgMultMaxPath)){
 			cfg.set(cfgMultMaxPath, defaults.maxPathMultiplier);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgRandRadius)){
 			cfg.set(cfgRandRadius, defaults.randDec); // TODO DEPRECATED ?
 			changed = true;
 		}
 		if ( !cfg.contains(cfgYield)){
 			cfg.set(cfgYield, defaults.yield);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgEntityYield)){
 			cfg.set(cfgEntityYield, defaults.entityYield);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgVelUse)){
 			cfg.set(cfgVelUse, defaults.velUse);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgVelMin)){
 			cfg.set(cfgVelMin, defaults.velMin);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgVelCen)){
 			cfg.set(cfgVelCen, defaults.velCen);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgVelRan)){
 			cfg.set(cfgVelRan, defaults.velRan);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgFStraight)){
 			cfg.set(cfgFStraight, defaults.fStraight);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgVelOnPrime)){
 			cfg.set(cfgVelOnPrime, defaults.velOnPrime);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgVelCap)){
 			cfg.set(cfgVelCap, defaults.velCap);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgThresholdTntDirect)){
 			cfg.set(cfgThresholdTntDirect, defaults.thresholdTntDirect);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgItemTnt)){
 			cfg.set(cfgItemTnt, defaults.itemTnt);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgMaxItems)){
 			cfg.set(cfgMaxItems, defaults.maxItems);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgItemArrows)){
 			cfg.set(cfgItemArrows, defaults.itemArrows);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgProjectiles)){
 			cfg.set(cfgProjectiles, defaults.projectiles);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgMinPrime)){
 			cfg.set(cfgMinPrime, defaults.minPrime);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgMaxPrime)){
 			cfg.set(cfgMaxPrime, defaults.maxPrime);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgStepPhysics)){
 			cfg.set(cfgStepPhysics, defaults.stepPhysics);
 			changed = true;
 		}
 		if ( !cfg.contains(cfgMultProjectiles)){
 			cfg.set(cfgMultProjectiles, defaults.projectileMultiplier);
 			changed = true;
 		}
 		return changed;
 	}
 	
 	/**
 	 * Convenience method to allow for integers and block names. [Integers work, blocks?]
 	 * @param cfg
 	 * @param path
 	 * @return
 	 */
 	public static List<Integer> getIdList(Configuration cfg, String path){
 		List<Integer> out = new LinkedList<Integer>();
 		List<String> ref = cfg.getStringList(path);
 		for ( Object x : ref){
 			Integer id = null;
 			if ( x instanceof Number){
 				// just in case
 				id = ((Number) x).intValue();
 			} else if ( x instanceof String){
 				try{
 					id = Integer.parseInt((String) x);
 				} catch(NumberFormatException exc) {
 					Material mat = Material.matchMaterial((String) x);
 					if ( mat != null){
 						id = mat.getId();
 					}
 				}
 			}
 			if (id!=null){
 				if ( id>=0 && id<4096) out.add(id);
 				continue;
 			}
 			Bukkit.getServer().getLogger().warning(Defaults.msgPrefix+"Bad item ("+path+"): "+x);
 		}
 		return out;
 	}
 
 }
