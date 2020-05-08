 package graindcafe.tribu.TribuZombie;
 
 import graindcafe.tribu.Tribu;
 import net.minecraft.server.EntityCreature;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 public class PathfinderGoalTrackPlayer extends PathfinderGoalMoveToLocation {
 	Player tracked;
 	Tribu plugin;
 	boolean randomOrNearest;
 
 	/**
 	 * Make the creature tracks a player
 	 * 
 	 * @param entitycreature
 	 * @param p
 	 * @param speed
 	 * @param can
 	 *            break door ?
 	 */
 	public PathfinderGoalTrackPlayer(EntityCreature entitycreature, Tribu plugin, boolean randomOrNearest, float speed, boolean canBreakDoor) {
 		super(entitycreature, null, speed, canBreakDoor);
 		this.plugin = plugin;
 		this.randomOrNearest = randomOrNearest;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see graindcafe.tribu.TribuZombie.PathfinderGoalMoveToLocation#a()
 	 */
 	public boolean a() {
 		return this.getTrackedPlayer()!=null && super.a();
 	}
 
 	protected Player getTrackedPlayer() {
 		if (tracked == null|| !plugin.isAlive(tracked)) {
 			if (randomOrNearest)
 				tracked = plugin.getRandomPlayer();
 			else
 				tracked = plugin.getNearestPlayer(new Location(this.a.world.getWorld(), this.a.locX, this.a.locY, this.a.locZ));
 		}
 		if(tracked!=null)
 			this.loc=tracked.getLocation();
 		return tracked;
 	}
 
 }
