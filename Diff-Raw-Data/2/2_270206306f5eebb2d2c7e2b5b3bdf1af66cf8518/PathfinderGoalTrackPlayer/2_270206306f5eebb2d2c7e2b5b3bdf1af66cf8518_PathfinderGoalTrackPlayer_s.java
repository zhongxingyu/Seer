 /*
  * 
  */
 package graindcafe.tribu.TribuZombie;
 
 import graindcafe.tribu.Tribu;
 import graindcafe.tribu.TribuSpawner;
 import net.minecraft.server.v1_6_R2.Entity;
 import net.minecraft.server.v1_6_R2.EntityCreature;
 import net.minecraft.server.v1_6_R2.EntityLiving;
 import net.minecraft.server.v1_6_R2.Navigation;
 import net.minecraft.server.v1_6_R2.PathfinderGoal;
 
 import org.bukkit.Location;
 import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
 
 /**
  * Based on FollowOwner
  * 
  * @author Graindcafe
  * 
  */
 
 public class PathfinderGoalTrackPlayer extends PathfinderGoal {
 	/**
 	 * The controlled entity
 	 */
 	private final EntityCreature creature;
 	/**
 	 * The entity to look at and move towards
 	 */
 	private Entity target;
 	/**
 	 * Distance
 	 */
 	private final int incrementalDistance;
 
 	private double x = 0;
 	private double y = 0;
 	private double z = 0;
 	private boolean getRandomPlayer = false;
 	private final Tribu plugin;
 	private final Navigation nav;
 	private boolean i;
 	private int h;
 	private double rushCoef = 1d;
 
 	/**
 	 * 
 	 * @param creature
 	 * @param targetClass
 	 * @param distance
 	 */
 	public PathfinderGoalTrackPlayer(final Tribu plugin,
 			final boolean getRandomPlayer, final EntityCreature creature,
 			final double rushCoef, final int distance) {
 		this.creature = creature;
 		incrementalDistance = distance;
 		this.plugin = plugin;
 		this.getRandomPlayer = getRandomPlayer;
 		this.nav = creature.getNavigation();
 		this.rushCoef = rushCoef;
 		// the goal priority
 		a(1);
 	}
 
 	/**
 	 * ? Decide if we should take an action or not
 	 * 
 	 * if a() true and (?:goalSelector.a(PathfinderGoalSelectorItem)) should be
 	 * executed, then do c() before adding it
 	 */
 	@Override
 	public boolean a() {
 		// System.out.println("Deciding");
 		EntityLiving entityliving = null;
 		/*
 		 * if (target != null) {
 		 * System.err.println("We shouldn't start doing this"); return false; }
 		 */
 		// get target
 		// target = creature.az();
 		x = creature.locX;
 		y = creature.locY;
 		z = creature.locZ;
 		if (creature.target == null && plugin != null)
 			entityliving = (getRandomPlayer ? ((CraftPlayer) plugin
 					.getRandomPlayer()) : ((CraftPlayer) plugin
 					.getNearestPlayer(x, y, z))).getHandle();
		else if (creature instanceof EntityLiving)
 			entityliving = (EntityLiving) creature.target;
 		if (entityliving == null) {
 			// System.out.println("Impossible happens sometime");
 			return false;
 		} else {
 			this.target = entityliving;
 			creature.setTarget(entityliving);
 			return true;
 		}
 	}
 
 	/**
 	 * Decide if we should continue doing this if b() false and
 	 * (?:goalSelector.a(PathfinderGoalSelectorItem)) should be executed, then
 	 * do d() before removing it nav.g() : return PathEntity == null ||
 	 * PathEntity.isDone()
 	 */
 	@Override
 	public boolean b() {
 		// System.out.println("Continuing ?");
 		return target != null && target.isAlive() && creature != null
 				&& creature.isAlive(); // &&
 		// this.creature.e(this.target)
 		// >
 		// (squaredActiveDistance);
 	}
 
 	/**
 	 * Before adding it if a() true and
 	 * (?:goalSelector.a(PathfinderGoalSelectorItem)) should be executed, then
 	 * do this before adding it
 	 */
 	@Override
 	public void c() {
 		// System.out.println("Before");
 		this.h = 0;
 		this.i = this.creature.getNavigation().a();
 		this.creature.getNavigation().a(false);
 	}
 
 	/**
 	 * Before stop doing this if b() false and
 	 * (?:goalSelector.a(PathfinderGoalSelectorItem)) should be executed, then
 	 * do this before removing it
 	 */
 	@Override
 	public void d() {
 		// System.out.println("Stop Doing");
 		this.target = null;
 		this.nav.h();
 		this.creature.getNavigation().a(this.i);
 	}
 
 	boolean tooFarAway = false;
 	int predX = 0;
 	int predZ = 0;
 
 	/**
 	 * Do the action
 	 */
 	@Override
 	public void e() {
 		// System.out.println("Doing");
 		if (this.target == null) {
 			// System.out.println("Impossible happens sometime²");
 			return;
 		}
 		this.creature.getControllerLook().a(this.target, 10.0F,
 				this.creature.bp());
 		if (--this.h <= 0) {
 			// System.out.println("a(target,rush)");
 			this.h = 100;
 			if (!this.nav.a(this.target, rushCoef)) {
 				Location loc = TribuSpawner.generatePointBetween(new Location(
 						creature.world.getWorld(), creature.locX,
 						creature.locY, creature.locZ), new Location(
 						target.world.getWorld(), target.locX, target.locY,
 						target.locZ), incrementalDistance);
 				if (loc != null) {
 					this.creature.getNavigation().a(loc.getX(), loc.getY(),
 							loc.getZ(), 1);
 					// System.out.println(String.format("Moving to\nC:%f,%f,%f\nT:%f,%f,%f\nL:%f,%f,%f",
 					// creature.locX, creature.locY, creature.locZ, target.locX,
 					// target.locY, target.locZ, loc.getX(), loc.getY(),
 					// loc.getZ()));
 				} else {
 					// System.out.println(String.format("Moving to\nC:%f,%f,%f\nT:%f,%f,%f\nL:null",
 					// creature.locX, creature.locY, creature.locZ, target.locX,
 					// target.locY, target.locZ));
 				}
 			}
 		}
 
 	}
 }
