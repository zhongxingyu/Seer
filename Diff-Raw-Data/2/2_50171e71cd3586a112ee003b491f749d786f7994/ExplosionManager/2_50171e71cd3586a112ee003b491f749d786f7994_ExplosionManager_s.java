 package me.asofold.bukkit.fattnt.effects;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Random;
 
 import me.asofold.bukkit.fattnt.FatTnt;
 import me.asofold.bukkit.fattnt.config.Defaults;
 import me.asofold.bukkit.fattnt.config.Settings;
 import me.asofold.bukkit.fattnt.events.FatEntityDamageEvent;
 import me.asofold.bukkit.fattnt.events.FatEntityExplodeEvent;
 import me.asofold.bukkit.fattnt.propagation.Propagation;
 import me.asofold.bukkit.fattnt.stats.Stats;
 import me.asofold.bukkit.fattnt.utils.Utils;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.entity.CraftTNTPrimed;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.TNTPrimed;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.util.Vector;
 
 /**
  * Static method utility ( currently).
  * @author mc_dev
  *
  */
 public class ExplosionManager {
 	
 	public static final Random random = new Random(System.currentTimeMillis()-1256875);
 	
 	private static Stats stats = null;
 	
 	/**
 	 * This does not create the explosion effect !
 	 * @param world
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @param realRadius
 	 * @param fire
 	 * @param explEntity
 	 * @param entityType
 	 * @param nearbyEntities can be null
 	 * @param settings
 	 * @param propagation
 	 */
 	public static void applyExplosionEffects(World world, double x, double y, double z, float realRadius, boolean fire, Entity explEntity, EntityType entityType,
 			List<Entity> nearbyEntities, float damageMultiplier, Settings settings, Propagation propagation) {
 		if ( realRadius > settings.maxRadius){
 			// TODO: settings ?
 			realRadius = settings.maxRadius;
 		} else if (realRadius == 0.0f) return;
 		PluginManager pm = Bukkit.getPluginManager();
 		
 		// blocks:
 		long ms = System.nanoTime();
 		List<Block> affected = propagation.getExplodingBlocks(world , x, y, z, realRadius);
 		stats.addStats(FatTnt.statsGetBlocks, System.nanoTime()-ms);
 		stats.addStats(FatTnt.statsBlocksCollected, affected.size());
 		stats.addStats(FatTnt.statsStrength, (long) realRadius);
 		FatExplosionSpecs specs = new FatExplosionSpecs();
 		EntityExplodeEvent exE = new FatEntityExplodeEvent(explEntity, new Location(world,x,y,z), affected, settings.defaultYield , specs);
 		pm.callEvent(exE);
 		if (exE.isCancelled()) return;
 		// block effects:
 		ms = System.nanoTime();
 		applyBlockEffects(world, x, y, z, realRadius, exE.blockList(), exE.getYield(), settings, propagation, specs);
 		stats.addStats(FatTnt.statsApplyBlocks, System.nanoTime()-ms);
 		// entities:
 		if ( nearbyEntities != null){
 			ms = System.nanoTime();
 			applyEntityEffects(world, x, y, z, realRadius, nearbyEntities, damageMultiplier, settings, propagation, specs);
 			stats.addStats(FatTnt.statsApplyEntities, System.nanoTime()-ms);
 		}
 		
 		
 	}
 	
 	/**
 	 * Block manuipulations for explosions.
 	 * @param world
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @param realRadius
 	 * @param blocks
 	 * @param defaultYield
 	 * @param settings
 	 * @param propagation
 	 * @param specs 
 	 */
 	public static void applyBlockEffects(World world, double x, double y, double z, float realRadius, List<Block> blocks, float defaultYield, Settings settings, Propagation propagation, FatExplosionSpecs specs){
 //		final List<block> directExplode = new LinkedList<block>(); // if set in config. - maybe later (split method to avoid recursion !)
 		for ( Block block : blocks){
 			if (block.getType() == Material.TNT){
 				block.setTypeId(0, true);
 				Location loc = block.getLocation().add(Defaults.vCenter);
 				final float effRad = propagation.getStrength(loc); // effective strength/radius
 //				if ( effRad > thresholdTntDirect){
 //					directExplode.add(block);
 //					continue;
 //				}
 				// do spawn tnt-primed
 				try{
 					Entity entity = world.spawn(loc, CraftTNTPrimed.class);
 					if (entity == null) continue;
 					if ( !(entity instanceof TNTPrimed)) continue;
 					if ( effRad == 0.0f) continue; // not affected
 					if (settings.velOnPrime) addRandomVelocity(entity, loc, x,y,z, effRad, realRadius, settings);
 				} catch( Throwable t){
 					// maybe later log
 				}
 				continue;
 			}
 			// All other blocks:
 			Collection<ItemStack> drops = block.getDrops();
 			for (ItemStack drop : drops){
 				if ( random.nextFloat()<=defaultYield){
 					Location loc = block.getLocation().add(Defaults.vCenter);
 					Item item = world.dropItemNaturally(loc, drop.clone());
 					if (item==null) continue;
 					//addRandomVelocity(item, loc, x,y,z, realRadius);
 				}
 			}
 			block.setTypeId(0, true); // TODO: evaluate if still spazzing appears (if so: use false, and then iterate again for applying physics after block changes).
 		}
 	}
 	
 	/**
 	 * Entity manipulations for explosions.
 	 * @param world
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @param realRadius
 	 * @param nearbyEntities
 	 * @param damageMultiplier
 	 * @param settings
 	 * @param propagation
 	 * @param specs 
 	 */
 	public static void applyEntityEffects(World world, double x, double y, double z, float realRadius, List<Entity> nearbyEntities, float damageMultiplier, Settings settings, Propagation propagation, FatExplosionSpecs specs) {
 		if ( realRadius > settings.maxRadius){
 			// TODO: settings ?
 			realRadius = settings.maxRadius;
 		} else if (realRadius == 0.0f) return;
 		PluginManager pm = Bukkit.getPluginManager();
 		// entities:
 		for ( Entity entity : nearbyEntities){
 			// test damage:
 			final Location loc = entity.getLocation();
 			final float effRad = propagation.getStrength(loc); // effective strength/radius
 			if ( effRad == 0.0f) continue; // not affected
 			boolean addVelocity = true;
 			boolean useDamage = true;
 			if (settings.sparePrimed && (entity instanceof TNTPrimed)){
				addVelocity = false;
 				useDamage = false;
 			}
 			if (useDamage){
 				// TODO: damage entities according to type
 				int damage = 1 + (int) (effRad*settings.damageMultiplier*damageMultiplier) ;
 				// TODO: take into account armor, enchantments and such?
 				EntityDamageEvent event = new FatEntityDamageEvent(entity, DamageCause.ENTITY_EXPLOSION, damage, specs);
 				pm.callEvent(event);
 				if (!event.isCancelled()){
 					if (Utils.damageEntity(event) > 0){
 						// (declined: consider using "effective damage" for stats.)
 						// (but:) Only include >0 damage (that might lose some armored players later, but prevents including invalid entities. 
 						stats.addStats(FatTnt.statsDamage, damage); 
 					} else{
 						// CURRENTLY, MIGHT BE CHANGED
 						addVelocity = false;
 					}
 				} else{
 					// CURRENTLY, MIGHT BE CHANGED
 					addVelocity = false;
 				}
 			} else{
 				// CURRENTLY, MIGHT BE CHANGED
 				addVelocity = false;
 			}
 			if (addVelocity) addRandomVelocity(entity, loc, x,y,z, effRad, realRadius, settings);
 		}
 	}
 	
 	/**
 	 * Add velocity according to settings
 	 * @param entity
 	 * @param loc
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @param part part of radius (effective), as with FatTnt.getExplosionStrength
 	 * @param max max radius
 	 */
 	public static void addRandomVelocity(Entity entity, Location loc, double x, double y,
 			double z, float part, float max, Settings settings) {
 		// TODO: make some things configurable, possible entity dependent and !
 		if (!settings.velUse) return;
 		Vector v = entity.getVelocity();
 		Vector fromCenter = new Vector(loc.getX()-x,loc.getY()-y,loc.getZ()-z).normalize();
 		Vector rv = v.add((fromCenter.multiply(settings.velMin+random.nextFloat()*settings.velCen)).add(Vector.getRandom().multiply(settings.velRan)).multiply(part/max));
 		if (entity instanceof LivingEntity) ((LivingEntity) entity).setVelocity(rv); 
 		else if (entity instanceof TNTPrimed) ((TNTPrimed) entity).setVelocity(rv);
 		else entity.setVelocity(rv);
 	}
 
 	/**
 	 * Apply the explosion effect in that world, 
 	 * currently this simply delegates to create an explosion with radius 0,
 	 * later it might add other effects.
 	 * @param world
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @param radius
 	 * @param fire
 	 */
 	public static void createExplosionEffect(World world, double x, double y,
 			double z, float radius, boolean fire) {
 		world.createExplosion(new Location(world,x,y,z), 0.0F); 
 	}
 	
 	public static void setStats(Stats stats){
 		ExplosionManager.stats = stats;
 	}
 }
