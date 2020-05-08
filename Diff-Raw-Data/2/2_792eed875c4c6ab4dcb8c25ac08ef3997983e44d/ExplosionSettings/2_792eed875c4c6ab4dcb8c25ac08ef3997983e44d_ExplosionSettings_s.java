 package me.asofold.bpl.fattnt.config;
 
 import java.util.Collection;
 import java.util.List;
 
 import me.asofold.bpl.fattnt.config.compatlayer.CompatConfig;
 import me.asofold.bpl.fattnt.config.priorityvalues.OverridePolicy;
 import me.asofold.bpl.fattnt.config.priorityvalues.PriorityBoolean;
 import me.asofold.bpl.fattnt.config.priorityvalues.PriorityFloat;
 import me.asofold.bpl.fattnt.config.priorityvalues.PriorityInteger;
 import me.asofold.bpl.fattnt.config.priorityvalues.PriorityObject;
 import me.asofold.bpl.fattnt.config.priorityvalues.PrioritySettings;
 import me.asofold.bpl.fattnt.config.priorityvalues.PriorityValue;
 
 /**
  * General explosion settings, may have sub settings, may appear as default, world specific and entity specific.
  */
 public class ExplosionSettings extends PrioritySettings{
 	
 	public ConfinementSettings confine;
 	
 	/**
 	 * Handle and alter explosions at all.
 	 * TODO: also put to config.
 	 */
 	public final PriorityBoolean handleExplosions = addValue("handleExplosions", new PriorityBoolean(null, 0, OverridePolicy.OVERRIDE));
 	public final PriorityBoolean preventOtherExplosions = addValue("preventOtherExplosions", new PriorityBoolean(null, 0, OverridePolicy.OVERRIDE));
 	public final PriorityBoolean preventExplosions = addValue("preventExplosions", new PriorityBoolean(null, 0, OverridePolicy.OVERRIDE));
 	
 
 	/**
 	 * Explosion strength is cut off there.
 	 */
 	public final PriorityFloat maxRadius = addValue("maxRadius", new PriorityFloat(null, 0, OverridePolicy.OVERRIDE));
 	
 	/**
 	 * Multiplier for strength (radius)
 	 */
 	public final PriorityFloat radiusMultiplier = addValue("radiusMultiplier", new PriorityFloat(null, 0, OverridePolicy.OVERRIDE));
 	
 	/**
 	 * Multiplier for entity damage.
 	 */
 	public final PriorityFloat damageMultiplier = addValue("damageMultiplier", new PriorityFloat(null, 0, OverridePolicy.OVERRIDE)); // TODO: add some ray damage !
 	
 	/**
 	 * Radius multiplier to modify range for collecting affected entities.
 	 * 
 	 */
 	public final PriorityFloat entityRadiusMultiplier = addValue("entityRadiusMultiplier", new PriorityFloat(null, 0, OverridePolicy.OVERRIDE));
 	
 	/**
 	 * Default explosion  resistance value for all materials that are not in one of the resistance-lists.
 	 */
 	public final PriorityFloat defaultResistance = addValue("defaultResistance", new PriorityFloat(null, 0, OverridePolicy.OVERRIDE));
 	
 	/**
 	 * Default pass-through resistance.
 	 */
 	public final PriorityFloat defaultPassthrough = addValue("defaultPassthrough", new PriorityFloat(null, 0, OverridePolicy.OVERRIDE)); 
 	
 	/**
 	 * If to not apply damage to primed tnt.
 	 */
 	public final PriorityBoolean sparePrimed = addValue("sparePrimed", new PriorityBoolean(null, 0, OverridePolicy.OVERRIDE));
 	
 	/**
 	 * Allow tnt items to change to primed tnt if combusted or hit by explosions.
 	 */
 	public final PriorityBoolean itemTnt = addValue("itemTnt", new PriorityBoolean(null, 0, OverridePolicy.OVERRIDE));
 	
 	/**
 	 * Currently unused [aimed at fast explosions]
 	 */
 	public  final PriorityFloat thresholdTntDirect = addValue("thresholdTntDirect", new PriorityFloat(null, 0, OverridePolicy.OVERRIDE));
 	
 	// velocity settings
 	public final PriorityBoolean velUse = addValue("velUse", new PriorityBoolean(null, 0, OverridePolicy.OVERRIDE));;
 	public final PriorityFloat velMin = addValue("velMin", new PriorityFloat(null, 0, OverridePolicy.OVERRIDE));
 	public final PriorityFloat velCen = addValue("velCen", new PriorityFloat(null, 0, OverridePolicy.OVERRIDE));
 	public final PriorityFloat velRan = addValue("velRan", new PriorityFloat(null, 0, OverridePolicy.OVERRIDE));
 	public final PriorityBoolean velOnPrime = addValue("velOnPrime", new PriorityBoolean(null, 0, OverridePolicy.OVERRIDE));;
 	public final PriorityFloat velCap = addValue("velCap", new PriorityFloat(null, 0, OverridePolicy.OVERRIDE));
 	
 	/**
 	 * Maximal number of Item entities created from an ItemStack.
 	 */
 	public final PriorityInteger maxItems = addValue("maxItems", new PriorityInteger(null, 0, OverridePolicy.OVERRIDE));
 	
 	/**
 	 * Transform arrow items to real arrows (explosions).
 	 */
 	public final PriorityBoolean itemArrows = addValue("itemArrows", new PriorityBoolean(null, 0, OverridePolicy.OVERRIDE));
 	
 	/**
 	 * Affect projectiles velocity.
 	 */
 	public final PriorityBoolean projectiles = addValue("projectiles", new PriorityBoolean(null, 0, OverridePolicy.OVERRIDE));
 	
 	/**
 	 * Minimum fuse ticks, if primed tnt is created.
 	 * Set  to <=0 to have default fuse ticks.
 	 */
 	public final PriorityInteger minPrime = addValue("minPrime", new PriorityInteger(null, 0, OverridePolicy.OVERRIDE));
 	/**
 	 * Maximum fuse ticks, if primed tnt is created.
 	 * Set  to <=0 to have default fuse ticks.
 	 * If set to a value greater than minPrime, the fuse ticks will be set randomly using that interval.
 	 */
 	public final PriorityInteger maxPrime = addValue("maxPrime", new PriorityInteger(null, 0, OverridePolicy.OVERRIDE));
 	
 	/**
 	 * Drop chance from destroyed blocks.
 	 */
 	public final PriorityFloat yield = addValue("yield", new PriorityFloat(null, 0, OverridePolicy.OVERRIDE));
 	/**
 	 * Survival chance for items/entities hit by an explosion.
 	 */
 	public final PriorityFloat entityYield = addValue("entityYield", new PriorityFloat(null, 0, OverridePolicy.OVERRIDE));
 	
 	/**
 	 * Use extra distance based damage.
 	 */
 	public final PriorityBoolean useDistanceDamage = addValue("useDistanceDamage", new PriorityBoolean(null, 0, OverridePolicy.OVERRIDE));
 	
 	/**
 	 * Use a simple distance damage model.
 	 */
 	public final PriorityBoolean simpleDistanceDamage = addValue("simpleDistanceDamage", new PriorityBoolean(null, 0, OverridePolicy.OVERRIDE));
 	
 	/**
 	 * Multiply projectiles velocity by this, if affected.
 	 */
 	public final PriorityFloat projectileMultiplier = addValue("projectileMultiplier", new PriorityFloat(null, 0, OverridePolicy.OVERRIDE));
 	
 	/**
 	 * The minimal present resistance value.
 	 * Set automatically from configuration input.
 	 */
	public float minResistance = Float.MIN_VALUE; // TODO
 	
 	/**
 	 * If a block can not be destroyed this will be checked for further propagation.
 	 * created in applyConfig
 	 */
 	public final PriorityValue<float[]> passthrough = addValue("passthrough", new PriorityObject<float[]>(null, 0, OverridePolicy.OVERRIDE));
 	
 	/**
 	 * Explosion resistance values for blocks.
 	 * created in applyConfig
 	 */
 	public final PriorityValue<float[]> resistance = addValue("resistance", new PriorityObject<float[]>(null, 0, OverridePolicy.OVERRIDE));
 	
 	/**
 	 * Propagate entity damage (living only) beond explosion radius. 
 	 */
 	public final PriorityValue<boolean[]> propagateDamage = addValue("propagateDamage", new PriorityObject<boolean[]>(null, 0, OverridePolicy.OVERRIDE));
 	
 	/**
 	 * If explosions go off in these blocks, no block damage will be dealt.
 	 */
 	public final PriorityValue<boolean[]> preventBlockDamage = addValue("preventBlockDamage", new PriorityObject<boolean[]>(null, 0, OverridePolicy.OVERRIDE));
 	
 	/**
 	 * Multiplier for the distance based damage to entities.
 	 */
 	public final PriorityFloat entityDistanceMultiplier = addValue("entityDistanceMultiplier", new PriorityFloat(null, 0, OverridePolicy.OVERRIDE)); // TODO: adjust
 	
 	/**
 	 * If to damage the armor on base of damage amount.
 	 */
 	public final PriorityBoolean armorUseDamage = addValue("armorUseDamage", new PriorityBoolean(null, 0, OverridePolicy.OVERRIDE));
 	public final PriorityFloat armorMultDamage = addValue("armorMultDamage", new PriorityFloat(null, 0, OverridePolicy.OVERRIDE));
 	public final PriorityInteger armorBaseDepletion = addValue("armorBaseDepletion", new PriorityInteger(null, 0, OverridePolicy.OVERRIDE));
 	
 	/**
 	 * Restrict maximal path length for propagation multiplied by explosion strength.
 	 */
 	public final PriorityFloat maxPathMultiplier = addValue("maxPathMultiplier", new PriorityFloat(null, 0, OverridePolicy.OVERRIDE));
 	
 	/**
 	 * Strength changes with this factor, for explosion paths advancing in the same direction again.
 	 */
 	public final PriorityFloat fStraight = addValue("fStraight", new PriorityFloat(null, 0, OverridePolicy.OVERRIDE));
 	
 	/**
 	 * UNUSED (was: random resistance added to blocks)
 	 */
 	public final PriorityFloat randRadius = addValue("randRadius", new PriorityFloat(null, 0, OverridePolicy.OVERRIDE));
 	
 	
 	/**
 	 * Experimental:Currently does explosions without applying physics (not good),
 	 * intended: apply physics after setting blocks to air.
 	 */
 	public final PriorityBoolean stepPhysics = addValue("stepPhysics", new PriorityBoolean(null, 0, OverridePolicy.OVERRIDE));
 	
 	// TODO: The following need paths !
 	public final PriorityBoolean scheduleExplosions = addValue("scheduleExplosions", new PriorityBoolean(null, 0, OverridePolicy.OVERRIDE));
 	public final PriorityBoolean scheduleItems = addValue("scheduleItems", new PriorityBoolean(null, 0, OverridePolicy.OVERRIDE));
 	public final PriorityBoolean scheduleEntities = addValue("scheduleEntities", new PriorityBoolean(null, 0, OverridePolicy.OVERRIDE));
 	
 
 	/**
 	 * field names to path.
 	 * TODO: consider using path as field name ! (rather risky, though)
 	 */
 	static final String[][] fields = new String[][]{
 		{"radiusMultiplier", Path.multRadius},
 		{"damageMultiplier", Path.multDamage},
 		{"entityRadiusMultiplier", Path.multEntityRadius},
 		{"entityDistanceMultiplier", Path.multEntityDistance},
 		{"maxPathMultiplier", Path.multMaxPath},
 		{"defaultPassthrough", Path.defaultPassthrough},
 		{"defaultResistance", Path.defaultResistance},
 		{"maxRadius", Path.maxRadius},
 		{"randRadius", Path.randRadius},
 		{"velUse", Path.velUse},
 		{"velMin", Path.velMin},
 		{"velCen", Path.velCen},
 		{"velRan", Path.velRan},
 		{"velOnPrime", Path.velOnPrime},
 		{"velCap", Path.velCap},
 		{"fStraight", Path.fStraight},
 		{"thresholdTntDirect", Path.cthresholdTntDirect},
 		{"useDistanceDamage", Path.useDistanceDamage},
 		{"simpleDistanceDamage", Path.simpleDistanceDamage},
 		{"maxItems", Path.maxItems},
 		{"projectiles", Path.projectiles},
 		{"minPrime", Path.minPrime},
 		{"maxPrime", Path.maxPrime},
 		{"stepPhysics", Path.stepPhysics},
 		{"projectileMultiplier", Path.multProjectiles},
 		{"armorUseDamage", Path.armorUseDamage},
 		{"armorMultDamage", Path.armorMultDamage},
 		{"armorBaseDepletion", Path.armorBaseDepletion},
 		{"yield", Path.yield},
 		{"entityYield", Path.entityYield},
 		{"itemTnt", Path.itemTnt},
 		{"itemArrows", Path.itemArrows},
 		{"scheduleExplosions", Path.schedExplosionsUse},
 		{"scheduleEntities", Path.schedEntitiesUse},
 		{"scheduleItems", Path.schedItemsUse},
 		{"sparePrimed", Path.sparePrimed},
 		{"handleExplosions", Path.handleExplosions},
 		{"preventExplosions", Path.preventExplosions},
 		{"preventOtherExplosions", Path.preventOtherExplosions},
 	};
 	
 	public ExplosionSettings(int priority) {
 		confine = new ConfinementSettings(priority);
 		setPriority(priority);
 	}
 	
 	private void initFloats(float[] a, float def){
 		for (int i = 0;i<a.length;i++){
 			a[i] = def;
 		}
 	}
 	
 	private void initBools(boolean[] a, boolean def){
 		for (int i = 0;i<a.length;i++){
 			a[i] = def;
 		}
 	}
 	
 	public void applyConfig(CompatConfig cfg, String prefix, int priority){
 		if (cfg.contains(prefix + Path.priority)) priority  = cfg.getInt(prefix + Path.priority, (int) 0);
 		resetAllValues(priority);
 		
 		// Confinement settings:
 		confine = new ConfinementSettings(priority);
 		confine.applyConfig(cfg, prefix);
 		
 	
 		for (String[] pair : fields){
 			updateFromCfg(pair[0], priority, cfg, prefix + pair[1]);
 		}
 		
 //		ExplosionSettings ref = new ExplosionSettings(0); // default settings.
 		if (cfg.contains(prefix + Path.defaultPassthrough)) passthrough.value = new float[Defaults.blockArraySize];
 		if (cfg.contains(prefix + Path.defaultResistance)) resistance.value = new float[Defaults.blockArraySize];
 		if (cfg.contains(prefix + Path.damagePropagate)) propagateDamage.value = new boolean[Defaults.blockArraySize];
 		if (cfg.contains(prefix + Path.damagePreventBlocks)) preventBlockDamage.value = new boolean[Defaults.blockArraySize];
 		
 
 		
 		minResistance = Float.MAX_VALUE;		
 		// TODO: minresistance is special (might need to be set in applySettings)!
 		
 		
 		if ( maxRadius.getValue(0.0f) > Defaults.radiusLock) maxRadius.value = Defaults.radiusLock; // safety check
 		
 		// TODO: Lazy treatment of the follwing settings (keep null or set).
 		if (resistance.value != null){
 			initFloats(resistance.value, defaultResistance.value);
 			readResistance(cfg, prefix + Path.resistance, resistance.value, defaultResistance.value);
 		}
 		if (passthrough.value != null){
 			initFloats(passthrough.value, defaultPassthrough.value);
 			readResistance(cfg, prefix + Path.passthrough, passthrough.value, defaultPassthrough.value);
 		}
 		if (propagateDamage.value != null){
 			initBools(propagateDamage.value, false);
 			List<Integer> ids = Defaults.getIdList(cfg, prefix + Path.damagePropagate);
 			for ( Integer id : ids){
 				propagateDamage.value[id] = true;
 			}
 		}
 		if (preventBlockDamage.value != null){
 			initBools(preventBlockDamage.value, false);
 			List<Integer> ids = Defaults.getIdList(cfg, prefix + Path.damagePreventBlocks);
 			for ( Integer id : ids){
 				preventBlockDamage.value[id] = true;
 			}
 		}
 		
 		if (defaultResistance.value != null) minResistance = Math.min(minResistance, defaultResistance.value);
 		if (defaultPassthrough.value != null) minResistance = Math.min(minResistance, defaultPassthrough.value);
 	}
 
 	protected void updateFromCfg(String field, int priority, CompatConfig cfg, String path){
 		PriorityValue<?> pv = nameValueMap.get(field); // Assume it to be present.
 		if (pv instanceof PriorityFloat){
 			PriorityFloat pf = (PriorityFloat) pv;
 			Double v = cfg.getDouble(path, null);
 			if (v == null) pf.value = null;
 			else pf.setValue(v.floatValue(), priority);
 		}
 		else if (pv instanceof PriorityInteger) ((PriorityInteger) pv).setValue(cfg.getInt(path, null), priority);
 		else if (pv instanceof PriorityBoolean)	((PriorityBoolean) pv).setValue(cfg.getBoolean(path, null), priority);
 		else throw new IllegalArgumentException("Bad PriorityValue type given: " + ((pv==null)?null:pv.getClass().getSimpleName()));
 	}
 
 	private void readResistance(CompatConfig cfg, String path, float[] array, float defaultResistance){
 		Collection<String> keys = cfg.getStringKeys(path);
 		if ( keys != null){
 			for (String key : keys){
 				if ( "default".equalsIgnoreCase(key)) continue;
 				float val = cfg.getDouble(path+Path.sep+key+Path.sep+"value", (double) defaultResistance).floatValue();
 				minResistance = Math.min(minResistance, val);
 				for ( Integer i : Defaults.getIdList(cfg, path+Path.sep+key+Path.sep+"ids")){
 					array[i] = val;
 				}
 			}
 		}
 	}
 	
 	public void applySettings(ExplosionSettings other) {
 		super.applySettings(other);
 		confine.applySettings(other.confine);
 		minResistance = Math.min(minResistance, other.minResistance); // Always on every priority.
 	}
 
 	@Override
 	public void setPriority(int priority) {
 		super.setPriority(priority);
 		confine.setPriority(priority);
 	}
 
 	@Override
 	public boolean hasValues() {
 		return super.hasValues() || confine.hasValues();
 	}
 
 	@Override
 	public void resetAllValues(int priority) {
 		confine.resetAllValues(priority);
 		super.resetAllValues(priority);
 		minResistance = Float.MAX_VALUE;
 	}
 
 //	public void toConfig(Configuration cfg, String prefix) {
 //		throw new RuntimeException("Not implemented: toConfig");
 //	}
 
 	
 	
 
 }
