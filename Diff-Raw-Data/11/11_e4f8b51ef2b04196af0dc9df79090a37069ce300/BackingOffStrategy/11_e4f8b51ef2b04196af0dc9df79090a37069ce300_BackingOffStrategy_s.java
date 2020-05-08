 package balle.strategy.planner;
 
 import java.util.Date;
 
 import org.apache.log4j.Logger;
 
 import balle.controller.Controller;
 import balle.misc.Globals;
 import balle.strategy.ConfusedException;
 import balle.strategy.executor.movement.GoToObjectPFN;
 import balle.world.Coord;
 import balle.world.Line;
 import balle.world.Snapshot;
 import balle.world.objects.Ball;
 import balle.world.objects.FieldObject;
 import balle.world.objects.Goal;
 import balle.world.objects.Point;
 import balle.world.objects.Robot;
 
 
 public class BackingOffStrategy extends GoToBall {
 
 	private static final Logger LOG = Logger
 			.getLogger(BackingOffStrategy.class);
 
 	/**
 	 * Distance to back off (in m)
 	 */
 	private static final double BACK_OFF_GAP = 1;
 
 	/**
 	 * Max time we can spend backing off
 	 */
 	private static final long BACK_OFF_TIME = (long) (0.5 * 1000);
 
 	/**
 	 * Velocity threshold below which we could be stuck
 	 */
	private static final double VELOCITY_THRESH = 3;// 3E-5;// NOTE: noise
 													// renders the velocity
 													// really unreliable atm
 
 	/**
 	 * Distance between our front and the other robot below which we could be
 	 * stuck
 	 */
 	private static final double DISTANCE_THRESH = Globals.ROBOT_LENGTH * 3 / 4;
 
 	/**
 	 * Time in milliseconds that must pass before we decide that we're stuck
 	 */
	private static final long TIME_THRESH = 1 * 1000;
 
 	private long timeWhenMaybeStuck = -1;
 	private long timeStartedBackingOff = -1;
 
 	private Point target;
 
 	public BackingOffStrategy() {
 		super(new GoToObjectPFN(0), false);
 		setShouldAvoidOpponent(false);
 	}
 
     public void startBackingOff(Snapshot snapshot) {
         long timeNow = new Date().getTime();
         timeStartedBackingOff = timeNow;
         setTarget(snapshot);
     }
 	@Override
 	public boolean shouldStealStep(Snapshot snapshot) {
 		Robot us = snapshot.getBalle();
 		Line ourFront = us.getFrontSide();
 		Robot opponent = snapshot.getOpponent();
         Ball ball = snapshot.getBall();
 		
 		if (timeStartedBackingOff > 0) {
 			return true;
 		}
 		// if we are close to the opponent and getting slow
 		if (ourFront.dist(opponent.getPosition()) < DISTANCE_THRESH
 				&& us.getVelocity().abs() < VELOCITY_THRESH) {
 			// if this is the first such instance
 			long timeNow = new Date().getTime();
 			if (timeWhenMaybeStuck == -1) {
 				timeWhenMaybeStuck = timeNow;
 				return false;
 			}
 			// if we feel like being stuck for a while now
 			if (timeNow - timeWhenMaybeStuck > TIME_THRESH) {
                 startBackingOff(snapshot);
 				setTarget(snapshot);
 
 				LOG.info("We're stuck! Back off");
 				return true;
 			} else {
 				return false;
 			}
 		}
 
         for (Line wall : snapshot.getPitch().getWalls()) {
             if (wall.dist(ourFront.midpoint()) < 0.05
                     && us.getVelocity().abs() < VELOCITY_THRESH) {
 
                 long timeNow = new Date().getTime();
                 if (timeWhenMaybeStuck == -1) {
                     timeWhenMaybeStuck = timeNow;
                     return false;
                 }
 				// A possible fix by toms, not tested yet.
 				if (timeNow - timeWhenMaybeStuck > TIME_THRESH * 0.5) {
                     startBackingOff(snapshot);
 
 
                     LOG.info("We're stuck to wall! Back off");
                     return true;
                 } else {
                     return false;
                 }
             }
         }
 
 		timeWhenMaybeStuck = -1;
 		return false;
 	}
 
 	private void setTarget(Snapshot snapshot) {
 		Robot ourRobot = snapshot.getBalle();
 		Coord target = ourRobot.getFacingLine().flip().extend(BACK_OFF_GAP)
 				.getB();
 
 		this.target = new Point(target);
 	}
 
 	@Override
 	protected FieldObject getTarget(Snapshot snapshot) {
 		return target;
 	}
 
 	@Override
 	protected void onStep(Controller controller, Snapshot snapshot) throws ConfusedException {
 
 
 
         Robot us = snapshot.getBalle();
         Robot opponent = snapshot.getOpponent();
         Ball ball = snapshot.getBall();
 
         if (us.possessesBall(ball) && opponent.possessesBall(ball)) {
 			// we might wish to back off if we are closer to opponents goal just  to continue to play.
 			// not tested yet
         	Goal ownGoal = snapshot.getOwnGoal();
     		Goal opponentsGoal = snapshot.getOpponentsGoal();
 			if (ownGoal.getPosition().dist(us.getPosition()) < opponentsGoal
 					.getPosition().dist(us.getPosition())) {
 	    			 	controller.stop();
 	    				LOG.warn("Both in possession, stopping");
 	    	            return;
 			} else {
				LOG.warn("Backing off!");
 				long timeNow = new Date().getTime();
 				// a possible fix by toms, not tested yet.
 				if (timeNow - timeStartedBackingOff > BACK_OFF_TIME * 0.5) {
 					timeStartedBackingOff = -1;
 					timeWhenMaybeStuck = -1;
 
 					LOG.info("Finished backing off");
 				}
 
 				super.onStep(controller, snapshot);
 			}
 
         }
 
        LOG.warn("Backing off!");
 		long timeNow = new Date().getTime();
 		// a possible fix by toms, not tested yet.
 		if (timeNow - timeStartedBackingOff > BACK_OFF_TIME) {
 			timeStartedBackingOff = -1;
 			timeWhenMaybeStuck = -1;
 
 			LOG.info("Finished backing off");
 		}
 
 		super.onStep(controller, snapshot);
 	}
 
 }
