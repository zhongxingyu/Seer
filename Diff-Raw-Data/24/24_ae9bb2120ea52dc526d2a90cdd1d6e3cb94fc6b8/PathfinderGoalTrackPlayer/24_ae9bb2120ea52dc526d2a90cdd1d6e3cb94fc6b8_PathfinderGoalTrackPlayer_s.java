 package graindcafe.tribu.TribuZombie;
 
 import graindcafe.tribu.Tribu;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityCreature;
import net.minecraft.server.PathfinderGoal;
import net.minecraft.server.RandomPositionGenerator;
import net.minecraft.server.Vec3D;
 
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 
 /**
  * Based on LookAtPlayer added MoveTowardsTarget
  * @author Graindcafe
  *
  */
 
public class PathfinderGoalTrackPlayer extends PathfinderGoal {
 	/**
 	 * The controlled entity
 	 */
 	private final EntityCreature	creature;
 	/**
 	 * The entity to look at and move towards
 	 */
 	private Entity					target;
 	private Entity					lastTarget;
 	/**
 	 * Distance
 	 */
 	private final float				squaredActiveDistance;
 
 	/**
 	 * Default : 0.02
 	 */
 	private final float				chance;
 
 	private double					x				= 0, //
 									y				= 0, //
 									z				= 0;
 	private final float				speed;
 	private boolean					doLookAt		= true;
 	private boolean					getRandomPlayer	= false;
 	private final Tribu				plugin;
 
 	/**
 	 * 
 	 * @param creature
 	 * @param targetClass
 	 * @param distance
 	 */
 	public PathfinderGoalTrackPlayer(final Tribu plugin, final boolean getRandomPlayer, final EntityCreature creature, final float speed, final float distance) {
 		this(plugin, getRandomPlayer, creature, speed, distance, 0.02f);
 	}
 
 	/**
 	 * 
 	 * @param creature
 	 * @param targetClass
 	 * @param distance
 	 * @param chance (?) default 0.02
 	 */
 	public PathfinderGoalTrackPlayer(final Tribu plugin, final boolean getRandomPlayer, final EntityCreature creature, final float speed, final float distance, final float chance) {
 		trueDebugMsg("init");
 		this.creature = creature;
 		squaredActiveDistance = distance * distance;
 		this.speed = speed;
 		this.chance = chance;
 		this.plugin = plugin;
 		this.getRandomPlayer = getRandomPlayer;
 		// the goal priority
 		a(1);
 	}
 
 	/**
 	 * ? Decide if we should take an action or not
 	 *
 	 * if a() true and (?:goalSelector.a(PathfinderGoalSelectorItem)) should be executed, then do c() before adding it
 	 */
 	@Override
 	public boolean a() {
 		// get target
 		lastTarget = target;
 		//target = creature.az();
 		if (target == null) target = (getRandomPlayer ? ((CraftPlayer) plugin.getRandomPlayer()) : ((CraftPlayer) plugin.getNearestPlayer(x, y, z))).getHandle();//
 		// if no target, do nothing
 		debugMsg("testing target");
 		if (target != null) {
 			// if too far away, do nothing
 			debugMsg("testing distance");
 			// if it's near enough
 			//if (target.e(creature) < squaredActiveDistance) return false;
 			final Vec3D localVec3D = RandomPositionGenerator.a(creature, 16, 7, Vec3D.a(target.locX, target.locY, target.locZ));
 			// if generation failed (improbable) do nothing
 			debugMsg("testing vec");
 			if (localVec3D == null) return false;
 			x = localVec3D.c;
 			y = localVec3D.d;
 			z = localVec3D.e;
 		} else if (lastTarget == null || (x == 0 && y == 0 && z == 0))
 			return false;
 		else
 			target = lastTarget;
 		return target!=null;
 	}
 
 	/**
 	 * Decide if we should continue doing this
 	 * if b() false and (?:goalSelector.a(PathfinderGoalSelectorItem)) should be executed, then do d() before removing it
 	 */
 	@Override
 	public boolean b() {
 		// lookat stuff
 		doLookAt = (creature.aB().nextFloat() >= chance);
 		// move stuff
 		return 	target != null && //
 				trueDebugMsg("testing navigation") && (!creature.getNavigation().f()) && //
 				trueDebugMsg("testing alive") && target.isAlive() && //
 				trueDebugMsg("testing distance") && creature.e(target) < squaredActiveDistance;
 
 	}
 
 	/**
 	 * Before adding it
 	 * if a() true and (?:goalSelector.a(PathfinderGoalSelectorItem)) should be executed, then do this before adding it
 	 */
 	@Override
 	public void c() {
 		//debugMsg("gonna add it ! " + ((CraftPlayer) target.getBukkitEntity()).getDisplayName());
 		
 	}
 
 	/**
 	 * Before stop doing this
 	 * if b() false and (?:goalSelector.a(PathfinderGoalSelectorItem)) should be executed, then do this before removing it
 	 */
 	@Override
 	public void d() {
 		debugMsg("gonna delete it");
 		target = null;
 	}
 
 	private void debugMsg(final String msg) {
 		// if(msg.startsWith("run"))
 		// Logger.getLogger("Minecraft").info(msg);
 	}
 
 	/**
 	 * Do the action
 	 */
 	@Override
 	public void e() {
 
 		if (doLookAt) {
 			// 									x 			y									z		   radius	?
 			// cf. PathfinderGoalLookAtPlayer
 			creature.getControllerLook().a(target.locX, target.locY + target.getHeadHeight(), target.locZ, 10.0F, creature.bp());
 		}
 		creature.getNavigation().a(x, y, z, speed);
 	}
 
 	private boolean trueDebugMsg(final String msg) {
 		debugMsg(msg);
 		return true;
 	}
 }
