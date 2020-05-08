 package br.ufrj.jfirn.intelligent;
 
import java.util.ArrayDeque;
import java.util.Deque;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.math3.util.FastMath;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import br.ufrj.jfirn.common.BasicParticle;
 import br.ufrj.jfirn.common.ParticleCollider;
 import br.ufrj.jfirn.common.Point;
 import br.ufrj.jfirn.common.PointParticle;
 
 public class IntelligentParticle extends BasicParticle implements Sight {
 
 	private static final double MAX_SPEED = 5;
 	private static final Logger logger = LoggerFactory.getLogger(IntelligentParticle.class);
 
 	/**
 	 * If endangered someone is way too close to me and I should fear for my safety.
 	 */
 	private boolean endangered;
 	private Map<PointParticle, MovementStatistics> aboutObstacles = new HashMap<>();
	private Deque<Point> targets = new ArrayDeque<>();
 
 	public IntelligentParticle(Point target) {
 		super();
 		targets.push(target);
 	}
 
 	public IntelligentParticle(double x, double y, double direction, double speed, Point target) {
 		super(x, y, direction, speed);
 		targets.push(target);
 	}
 
 	/**
 	 * Handles particle seen events.
 	 * This method should do the minimum necessary work here. Movement decision
 	 * logic should be put inside the {@link #move()} method.
 	 */
 	@Override
 	public void onSight(SightEvent e) {
 		if (logger.isDebugEnabled()) {
 			logger.debug(this + " sees: " + e.getParticlesSighted());
 		}
 
 		for (PointParticle p : e.getParticlesSighted()) {
 			if ( this.isInDangerRadius(p) ) {
 				this.endangered = true;
 				break;
 			} else {
 				this.speed(MAX_SPEED);
 			}
 		}
 
 		//I'll only keep statistics about objects I see
 		aboutObstacles.keySet().retainAll(e.getParticlesSighted());
 
 		for (PointParticle p : e.getParticlesSighted()) {
 			//store data about objects I see
 			if ( !aboutObstacles.containsKey(p) ) {
 				aboutObstacles.put(p, new MovementStatistics());
 			}
 
 			aboutObstacles.get(p).addEntry(p.x(), p.y(), p.speed(), p.direction());
 		}
 	}
 
 	/**
 	 * Moves the robot, but thinks about what move it should be.
 	 */
 	@Override
 	public void move() {
 		if (endangered) {
 			this.speed(STOPPED);
 		}
 
 		if (targets.isEmpty()) {
 			this.speed(STOPPED);
			return;
		}

		final Point currentTarget = targets.peek();

		if (this.isInReachRadius(currentTarget)) {
			logger.debug("Arrived at " + targets.pop());
 			//TODO Should I fire some event to the simulation saying I'm done? Should this data be handled here?
 			return;
 		}
 
 		//In general this is what I should do here:
 		//Am I in danger?
 		//Did I reach my target?
 		//Evaluate my current data
 		//Discover my next target(s)
 		//Update direction/speed
 		//move
 
 		//TODO data evaluation can be done in many ways. The particle probably should accept many evaluation strategies.
 
 		//standard move action: move towards target
 		direction (
 			FastMath.atan2(
				currentTarget.y - this.y(),
				currentTarget.x - this.x()
 			)
 		);
 
 		super.move();
 	}
 
 	/**
 	 * Used to verify if the particle is in danger.
 	 */
 	private final static double DANGER_RADIUS = 100;
 	private boolean isInDangerRadius(PointParticle p) {
 		return DANGER_RADIUS <= ParticleCollider.distance(this, p);
 	}
 
 	/**
 	 * Used to verify if I'm close enougth to a target
 	 */
 	private final static double REACH_RADIUS = 10;
 	private boolean isInReachRadius(Point p) {
 		return REACH_RADIUS <= ParticleCollider.distance(this, p);
 	}
 
 }
