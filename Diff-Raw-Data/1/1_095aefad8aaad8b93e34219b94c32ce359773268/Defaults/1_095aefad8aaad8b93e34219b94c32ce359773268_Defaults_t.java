 package me.asofold.bukkit.fattnt.config;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import me.asofold.bukkit.fattnt.config.compatlayer.CompatConfig;
 import me.asofold.bukkit.fattnt.config.compatlayer.ConfigUtil;
 import me.asofold.bukkit.fattnt.config.compatlayer.NewConfig;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
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
 	
 	public static final int[] defaultPropagateDamage = new int[]{
 		0, 
 		6,
 		8,9,
 		10,11,
 		18,
 		27, 28,
 		30,
 		31,
 		32,
 		34,
 		36,
 		37,
 		38,
 		39,
 		40,
 		50,
 		51,
 		55,
 		59,
 		63,
 		64,
 		65, 66,
 		67,
 		68,
 		69,
 		70,72,
 		75,76,
 		78,
 		83,
 		85,
 		90,
 		93,94,
 		96,
 		101,
 		104, 105,
 		106,
 		107,
 		108,109,
 		111,
 		113,
 		114,
 		115,
 		117,
 		119,
 		122,
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
 	 * Simple default values.
 	 */
 	static CompatConfig simpleDefaults;
 	
 	static{
 		simpleDefaults = getSimpleDefaultConfiguration();
 	}
 	
 	/**
 	 * Used for all entries that can be checked with if (!Configuration.contains(path)) ... (add it as a whole).
 	 * @return
 	 */
 	public static CompatConfig getSimpleDefaultConfiguration(){
 		ExplosionSettings defaults = new ExplosionSettings(0); // read defaults from here.
 		CompatConfig cfg = new NewConfig(null);
 		
 		// entities: 
 		// TODO: just set the greedy flags !
 		
 		// passthrough
 		cfg.set(Path.defaultPassthrough, defaults.defaultPassthrough);
 		
 		// resistance
 		float[] v = new float[]{1.0f, 4.0f, 20.0f, Float.MAX_VALUE};
 		int[][] ids = new int[][]{defaultLowResistance, defaultHigherResistance, defaultStrongResistance, defaultMaxResistance};
 		String[] keys = new String[]{"low", "higher", "strongest", "indestructible"};
 		for ( int i = 0; i<v.length; i++){
 			String base = Path.resistence+"."+keys[i];
 			List<Integer> resSet = new LinkedList<Integer>();
 			for ( int id: ids[i]) {
 				resSet.add(id);
 			}
 			cfg.set(base+".value", v[i]);
 			cfg.set(base+".ids", resSet);
 		}
 		cfg.set(Path.defaultResistence, defaults.defaultResistance);
 		
 		// damage propagation
 		List<Integer> entries = new LinkedList<Integer>();
 		for (int i : Defaults.defaultPropagateDamage){
 			entries.add(i);
 		}
 		cfg.set(Path.damagePropagate, entries);
 			
 		// explosion basics:
 		cfg.set(Path.maxRadius, defaults.maxRadius);
 		cfg.set(Path.multDamage, defaults.damageMultiplier);
 		cfg.set(Path.multRadius, defaults.radiusMultiplier);
 		cfg.set(Path.multMaxPath, defaults.maxPathMultiplier);
 		cfg.set(Path.randRadius, defaults.randRadius); // TODO DEPRECATED ?
 		cfg.set(Path.yield, defaults.yield);
 		cfg.set(Path.entityYield, defaults.entityYield);
 		
 		// velocity:
 		cfg.set(Path.velUse, defaults.velUse);
 		cfg.set(Path.velMin, defaults.velMin);
 		cfg.set(Path.velCen, defaults.velCen);			
 		cfg.set(Path.velRan, defaults.velRan);
 		cfg.set(Path.velOnPrime, defaults.velOnPrime);	
 		cfg.set(Path.velCap, defaults.velCap);
 		
 		// array propagation specific
 		cfg.set(Path.fStraight, defaults.fStraight);			
 			
 		// item transformationz
 		cfg.set(Path.itemTnt, defaults.itemTnt);
 		cfg.set(Path.maxItems, defaults.maxItems);
 		cfg.set(Path.itemArrows, defaults.itemArrows);
 		
 		// Projectiles:
 		cfg.set(Path.multProjectiles, defaults.projectileMultiplier);
 		cfg.set(Path.projectiles, defaults.projectiles);
 			
 		// tnt specific
 		cfg.set(Path.minPrime, defaults.minPrime);
 		cfg.set(Path.maxPrime, defaults.maxPrime);
 		cfg.set(Path.cthresholdTntDirect, defaults.thresholdTntDirect); // unused ?	
 			
 		// physics
 		cfg.set(Path.stepPhysics, defaults.stepPhysics);
 			
 		// armor
 		cfg.set(Path.armorBaseDepletion, defaults.armorBaseDepletion);
 		cfg.set(Path.armorMultDamage, defaults.armorMultDamage);
 		cfg.set(Path.armorUseDamage, defaults.armorUseDamage);
 			
 		// entity damage - beyond block damage)
 		cfg.set(Path.multEntityDistance, defaults.entityDistanceMultiplier);
 		cfg.set(Path.multEntityRadius, defaults.entityRadiusMultiplier);
 		cfg.set(Path.simpleDistanceDamage, defaults.simpleDistanceDamage);
 		cfg.set(Path.useDistanceDamage, defaults.useDistanceDamage);
 		
 		// TODO: these are a workaround:
 		cfg.set(Path.confineEnabled, false);
 		cfg.set(Path.confineYMin, 0);
 		cfg.set(Path.confineYMax, 255);
 		
 		return cfg;
 	}
 	
 	/**
 	 * Add non present default settings.
 	 * @param cfg
 	 * @return If changes were done.
 	 */
 	public static boolean addDefaultSettings(CompatConfig cfg) {
 		return ConfigUtil.forceDefaults(simpleDefaults, cfg);
 	}
 	
 	/**
 	 * Convenience method to allow for integers and block names. [Integers work, blocks?]
 	 * @param cfg
 	 * @param path
 	 * @return
 	 */
 	public static List<Integer> getIdList(CompatConfig cfg, String path){
 		List<Integer> out = new LinkedList<Integer>();
 		List<String> ref = cfg.getStringList(path);
		if (ref == null) return out;
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
