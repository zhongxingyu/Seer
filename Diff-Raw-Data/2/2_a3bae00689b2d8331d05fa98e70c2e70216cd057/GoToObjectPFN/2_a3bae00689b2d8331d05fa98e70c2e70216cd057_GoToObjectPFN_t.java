 package balle.strategy.executor.movement;
 
 import java.awt.Color;
 import java.util.ArrayList;
 
 import org.apache.log4j.Logger;
 
 import balle.controller.Controller;
 import balle.main.drawable.Drawable;
 import balle.main.drawable.Label;
 import balle.misc.Globals;
 import balle.strategy.pFStrategy.PFPlanning;
 import balle.strategy.pFStrategy.Point;
 import balle.strategy.pFStrategy.Pos;
 import balle.strategy.pFStrategy.RectObject;
 import balle.strategy.pFStrategy.RobotConf;
 import balle.strategy.pFStrategy.Vector;
 import balle.strategy.pFStrategy.VelocityVec;
 import balle.world.Coord;
 import balle.world.Snapshot;
 import balle.world.objects.FieldObject;
 
 public class GoToObjectPFN implements MovementExecutor {
 
     private static final Logger LOG               = Logger.getLogger(MovementExecutor.class);
 
     // cm
     private static final double ROBOT_TRACK_WIDTH = Globals.ROBOT_TRACK_WIDTH * 100;
     private static final double WHEEL_RADIUS = Globals.ROBOT_WHEEL_DIAMETER * 100 / 2.0;
 
     private double OPPONENT_POWER;
     private static final double DEFAULT_OPPONENT_POWER = 0;
     private double OPPONENT_INFLUENCE_DISTANCE;
     private static final double DEFAULT_OPPONENT_INFLUENCE_DISTANCE = 0;
     private double TARGET_POWER;
     private static final double DEFAULT_TARGET_POWER = 10;
     private double ALPHA;
     private static final double DEFAULT_ALPHA = 0;
 
     private double stopDistance; // meters
     private boolean slowDownCloseToTarget;
 
     private final ArrayList<Drawable> drawables = new ArrayList<Drawable>();
 
     public boolean shouldSlowDownCloseToTarget() {
         return slowDownCloseToTarget;
     }
 
     public void setSlowDownCloseToTarget(boolean slowDownCloseToTarget) {
         this.slowDownCloseToTarget = slowDownCloseToTarget;
     }
 
     public double getStopDistance() {
         return stopDistance;
     }
 
     @Override
     public void setStopDistance(double stopDistance) {
         this.stopDistance = stopDistance;
     }
 
     private FieldObject target;
 
     PFPlanning                plann;
 
     public GoToObjectPFN(double stopDistance, boolean slowDownCloseToTarget) {
         this(stopDistance, slowDownCloseToTarget, DEFAULT_OPPONENT_POWER,
                 DEFAULT_OPPONENT_INFLUENCE_DISTANCE, DEFAULT_TARGET_POWER,
                 DEFAULT_ALPHA);
     }
 
     public GoToObjectPFN(double stopDistance, boolean slowDownCloseToTarget,
             double opponentPower, double opponentInfluenceDistance,
             double targetPower, double alpha) {
         this.stopDistance = 0;
         setSlowDownCloseToTarget(slowDownCloseToTarget);
 
         OPPONENT_POWER = opponentPower;
         OPPONENT_INFLUENCE_DISTANCE = opponentInfluenceDistance;
         TARGET_POWER = targetPower;
         ALPHA = alpha;
 
         setSlowDownCloseToTarget(slowDownCloseToTarget);
         RobotConf conf = new RobotConf(ROBOT_TRACK_WIDTH, WHEEL_RADIUS);
         // plann = new PFPlanning(conf, 0, 1, 12, 0.5); // No opponent avoidance
         // Avoids opponent:
         plann = new PFPlanning(conf, OPPONENT_POWER,
                 OPPONENT_INFLUENCE_DISTANCE, TARGET_POWER, ALPHA);
 
         // Add walls
         double wallPower = 0.05;
         double wallInfDist = 0.3;
         RectObject leftWall = new RectObject(
                 new Point(0, Globals.PITCH_HEIGHT), new Point(0, 0), wallPower,
                 wallInfDist);
         RectObject rightWall = new RectObject(new Point(Globals.PITCH_WIDTH,
                 Globals.PITCH_HEIGHT), new Point(Globals.PITCH_WIDTH, 0),
                 wallPower, wallInfDist);
         RectObject topWall = new RectObject(new Point(0, Globals.PITCH_HEIGHT),
                 new Point(Globals.PITCH_WIDTH, Globals.PITCH_HEIGHT),
                 wallPower, wallInfDist);
         RectObject bottomWall = new RectObject(new Point(0, 0), new Point(
                 Globals.PITCH_WIDTH, 0), wallPower, wallInfDist);
 
         plann.addObject(leftWall);
         plann.addObject(rightWall);
         plann.addObject(topWall);
         plann.addObject(bottomWall);
     }
 
     /**
      * Instantiates a new go to object executor that uses Potential Fields
      * Navigation
      * 
      * @param stopDistance
      *            distance to stop from (in meters)
      */
     public GoToObjectPFN(double stopDistance) {
		this(stopDistance, true);
     }
 
     @Override
     public void stop(Controller controller) {
         controller.stop();
 
     }
 
     @Override
     public void updateTarget(FieldObject target) {
         this.target = target;
 
     }
 
     @Override
     public boolean isFinished(Snapshot snapshot) {
         if ((snapshot == null) || (target == null)
                 || (target.getPosition() == null)
                 || (snapshot.getBalle().getPosition() == null))
             return false;
         return target.getPosition().dist(snapshot.getBalle().getPosition()) <= getStopDistance();
 
     }
 
     @Override
     public boolean isPossible(Snapshot snapshot) {
         return ((snapshot != null) && (target != null)
                 && (target.getPosition() != null)
                 && (snapshot.getBalle().getPosition() != null) && (snapshot
                 .getBalle().getOrientation() != null));
     }
 
     @Override
 	public void step(Controller controller, Snapshot snapshot) {
         drawables.clear();
         if (!isPossible(snapshot))
             return;
 
         Pos opponent;
         if ((snapshot.getOpponent().getOrientation() == null)
                 || (snapshot.getOpponent().getPosition() == null))
             opponent = null;
         else
             opponent = new Pos(new Point(snapshot.getOpponent().getPosition()
                     .getX(), snapshot.getOpponent().getPosition().getY()),
                     snapshot.getOpponent().getOrientation().radians());
 
         // Our pos
         Pos initPos = new Pos(new Point(snapshot.getBalle().getPosition()
                 .getX(), snapshot.getBalle().getPosition().getY()), snapshot
                 .getBalle().getOrientation().radians());
 
         // Target pos
         Point targetLoc = new Point(target.getPosition().getX(), target
                 .getPosition().getY());
 
         VelocityVec res = plann.update(initPos, opponent, targetLoc);
         if (!shouldSlowDownCloseToTarget()) {
             Vector newRes = res.mult(4);
             res = new VelocityVec(newRes.getX(), newRes.getY());
         }
         LOG.trace("UNSCALED Left speed: " + Math.toDegrees(res.getLeft())
                 + " right speed: " + Math.toDegrees(res.getRight()));
         double left, right;
 
         res = res.scale();
 
         left = Math.toDegrees(res.getLeft());
         right = Math.toDegrees(res.getRight());
         drawables.add(new Label(String.format("(%d, %d)", (int) left,
                 (int) right),
                 new Coord(1, Globals.PITCH_HEIGHT + 0.3), Color.BLACK));
 
         controller.setWheelSpeeds((int) left, (int) right);
     }
 
     @Override
     public ArrayList<Drawable> getDrawables() {
         return drawables;
     }
 }
