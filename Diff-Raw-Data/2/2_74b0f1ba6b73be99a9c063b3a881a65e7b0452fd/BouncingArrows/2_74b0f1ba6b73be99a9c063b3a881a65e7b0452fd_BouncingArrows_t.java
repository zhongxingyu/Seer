 package de.blablubbabc.bouncingarrows;
 
 import java.lang.reflect.Field;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.material.TrapDoor;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.BlockIterator;
 import org.bukkit.util.Vector;
 
 public class BouncingArrows extends JavaPlugin implements Listener {
 
 	public void onEnable() {
 		getServer().getPluginManager().registerEvents(this, this);
 	}
 
 	// arrows via bow
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onEntityShootBowEvent(EntityShootBowEvent event) {
 		LivingEntity entity = event.getEntity();
 		Entity projectile = event.getProjectile();
 
 		if (((entity instanceof Player)) && (projectile.getType() == EntityType.ARROW)) {
 			Player player = (Player) entity;
 
 			if (event.getBow().getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0) {
 				if (player.hasPermission("bouncingarrows.use")) {
 					projectile.setMetadata("bouncing", new FixedMetadataValue(this, true));
 				}
 				
 				if (player.hasPermission("bouncingarrows.aim")) {
 					Projectile projectileP = (Projectile) projectile;
 					LivingEntity target = findTarget(projectileP);
 					if (target != null) {
 						playEffect(projectile);
 						aimAtTarget(projectileP, target, projectileP.getVelocity().length());
 					}
 				}
 			}
 		}
 	}
 	
 	// thrown snowballs
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onProjectileLaunch(ProjectileLaunchEvent event) {
 		Projectile projectile = event.getEntity();
 		EntityType type = projectile.getType();
 		if (type == EntityType.SNOWBALL && projectile.getShooter() instanceof Player) {
 			Player player = (Player) projectile.getShooter();
 			if (player.hasPermission("bouncingarrows.use")) {
 				projectile.setMetadata("bouncing", new FixedMetadataValue(this, true));
 			}
 		}
 	}
 
 	private LivingEntity findTarget(Projectile projectile) {
 		double radius = 150.0D;
 		LivingEntity shooter = projectile.getShooter();
 		Location projectileLocation = projectile.getLocation();
 		Vector projectileDirection = projectile.getVelocity().normalize();
 		Vector projectileVector = projectileLocation.toVector();
 
 		LivingEntity target = null;
 		double minDotProduct = Double.MIN_VALUE;
 		for (Entity entity : projectile.getNearbyEntities(radius, radius, radius)) {
 			if (entity instanceof LivingEntity && !entity.equals(shooter)) {
 				LivingEntity living = (LivingEntity) entity;
 				Location newTargetLocation = living.getEyeLocation();
 
 				// check angle to target:
 				Vector toTarget = newTargetLocation.toVector().subtract(projectileVector).normalize();
 				double dotProduct = toTarget.dot(projectileDirection);
 				if (dotProduct > 0.97D && shooter.hasLineOfSight(living) && (target == null || dotProduct > minDotProduct)) {
 					target = living;
 					minDotProduct = dotProduct;
 				}
 			}
 		}
 
 		return target;
 	}
 
 	private void aimAtTarget(final Projectile projectile, final LivingEntity target, final double speed) {
 		Location projectileLocation = projectile.getLocation();
 		Location targetLocation = target.getEyeLocation();
 		// validate target:
 		if (target.isDead() || !target.isValid() || !targetLocation.getWorld().getName().equals(projectileLocation.getWorld().getName())
 				|| targetLocation.distanceSquared(projectileLocation) > 25000) {
 			return;
 		}
 
 		// move towards target
 		Vector oldVelocity = projectile.getVelocity();
 
 		Vector direction = targetLocation.toVector().subtract(projectileLocation.toVector()).normalize().multiply(targetLocation.getY() > projectileLocation.getY() ? speed : speed / 3);
 		projectile.setVelocity(oldVelocity.add(direction).normalize().multiply(speed));
 
 		// repeat:
 		getServer().getScheduler().runTaskLater(this, new Runnable() {
 
 			@Override
 			public void run() {
 				if (!projectile.isDead() && projectile.isValid() && !projectile.isOnGround() && projectile.getTicksLived() < 600)
 					aimAtTarget(projectile, target, speed);
 			}
 		}, 1L);
 	}
 
 	private void playEffect(final Entity entity) {
 		ParticleEffect.HEART.play(entity.getLocation(), 0, 0, 0, 1, 10);
 		getServer().getScheduler().runTaskLater(this, new Runnable() {
 
 			@Override
 			public void run() {
 				if (entity.isDead() || !entity.isValid() || entity.isOnGround())
 					return;
 				playEffect(entity);
 			}
 		}, 2L);
 	}
 
 	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
 	public void onProjectileHitEvent(ProjectileHitEvent event) {
 		Projectile projectile = event.getEntity();
 		LivingEntity shooter = projectile.getShooter();
 		EntityType projectileType = projectile.getType();
 
 		if (projectile.hasMetadata("bouncing")) {
 			Vector arrowVelocity = projectile.getVelocity();
 			double speed = arrowVelocity.length();
 
 			if (speed < 0.3D || (projectileType == EntityType.ARROW && speed < 0.5D)) {
 				return;
 			}
 
 			Location arrowLocation = projectile.getLocation();
 			Block hitBlock = arrowLocation.getBlock();
 
 			BlockFace blockFace = BlockFace.UP;
 			
 			// special cases:
 			if (isWoodenTrigger(hitBlock.getType())) return;
 			if (!isHollowUpDownType(hitBlock)) {
 				BlockIterator blockIterator = new BlockIterator(arrowLocation.getWorld(), arrowLocation.toVector(), arrowVelocity, 0.0D, 3);
 
 				Block previousBlock = hitBlock;
 				Block nextBlock = blockIterator.next();
 				if (isWoodenTrigger(nextBlock.getType()))
 					return;
 
 				// to make sure, that previousBlock and nextBlock are not the same block
				while (blockIterator.hasNext() && (nextBlock.getType() == Material.AIR || nextBlock.isLiquid() || nextBlock.equals(hitBlock))) {
 					previousBlock = nextBlock;
 					nextBlock = blockIterator.next();
 					if (isWoodenTrigger(nextBlock.getType()))
 						return;
 				}
 				
 				//direction
 				blockFace = nextBlock.getFace(previousBlock);
 				
 			}
 
 			if (blockFace != null) {
 				if (blockFace == BlockFace.SELF) {
 					blockFace = BlockFace.UP;
 				}
 
 				if (isWoodenTrigger(hitBlock.getRelative(blockFace).getType()))
 					return;
 
 				Vector mirrorDirection = new Vector(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ());
 				double dotProduct = arrowVelocity.dot(mirrorDirection);
 				mirrorDirection = mirrorDirection.multiply(dotProduct).multiply(2.0D);
 
 				// reduce projectile speed
 				speed *= 0.6D;
 
 				Projectile newProjectile;
 				if (projectileType == EntityType.ARROW) {
 					// spawn with slight spray:
 					newProjectile = projectile.getWorld().spawnArrow(arrowLocation, arrowVelocity.subtract(mirrorDirection), (float) speed, 4.0F);
 					
 					// make the arrow pickup-able:
 					if (shooter.getType() == EntityType.PLAYER) {
 						Field field;
 						try {
 							Object entityArrow = newProjectile.getClass().getMethod("getHandle").invoke(newProjectile);
 							field = entityArrow.getClass().getDeclaredField("fromPlayer");
 							//field.setAccessible(true);
 							field.set(entityArrow, 1);
 						} catch (Exception e) {
 							System.out.println("[BouncingArrows] Failed to set the arrow pick-able! StackTrace: ");
 							e.printStackTrace();
 						}
 					}
 				} else {
 					// without spray:
 					newProjectile = (Projectile) projectile.getWorld().spawnEntity(arrowLocation, projectile.getType());
 					newProjectile.setVelocity(arrowVelocity.subtract(mirrorDirection).normalize().multiply(speed));
 				}
 
 				newProjectile.setShooter(shooter);
 				newProjectile.setFireTicks(projectile.getFireTicks());
 				newProjectile.setMetadata("bouncing", new FixedMetadataValue(this, true));
 
 				// remove old arrow
 				projectile.remove();
 			}
 		}
 	}
 	
 	private boolean isHollowUpDownType(Block block) {
 		Material type = block.getType();
 		// TODO removed stairs here for now because for stairs it depends from which direction they were hit..
 		return isStep(type) || type == Material.CARPET || type == Material.SNOW
 				|| type == Material.DIODE_BLOCK_OFF || type == Material.DIODE_BLOCK_ON 
 				|| type == Material.REDSTONE_COMPARATOR_OFF || type == Material.REDSTONE_COMPARATOR_ON
 				|| type == Material.CAULDRON || type == Material.BED_BLOCK || type == Material.DAYLIGHT_DETECTOR
 				|| type == Material.RAILS || type == Material.DETECTOR_RAIL || type == Material.POWERED_RAIL || type == Material.ACTIVATOR_RAIL
 				|| type == Material.GOLD_PLATE || type == Material.IRON_PLATE || type == Material.STONE_PLATE
 				|| (type == Material.TRAP_DOOR && !(new TrapDoor(type, block.getData()).isOpen()));
 	}
 	
 	private boolean isStep(Material type) {
 		return type == Material.STEP || type == Material.WOOD_STEP;
 	}
 	
 	private boolean isStair(Material type) {
 		return type == Material.WOOD_STAIRS || type == Material.SANDSTONE_STAIRS 
 		|| type == Material.COBBLESTONE_STAIRS || type == Material.BRICK_STAIRS 
 		|| type == Material.QUARTZ_STAIRS || type == Material.BIRCH_WOOD_STAIRS 
 		|| type == Material.NETHER_BRICK_STAIRS || type == Material.SMOOTH_STAIRS
 		|| type == Material.SPRUCE_WOOD_STAIRS;
 	}
 
 	private boolean isWoodenTrigger(Material type) {
 		return type == Material.WOOD_BUTTON || type == Material.WOOD_PLATE;
 	}
 }
