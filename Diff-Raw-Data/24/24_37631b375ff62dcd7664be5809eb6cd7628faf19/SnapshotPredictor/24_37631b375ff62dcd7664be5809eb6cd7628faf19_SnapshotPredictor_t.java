 package balle.simulator;
 
 import java.util.ArrayList;
 
 import org.apache.log4j.Logger;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.World;
 
 import balle.strategy.bezierNav.ControllerHistoryElement;
 import balle.world.AngularVelocity;
 import balle.world.Coord;
 import balle.world.Orientation;
 import balle.world.Snapshot;
 import balle.world.Velocity;
 import balle.world.objects.Ball;
 import balle.world.objects.Goal;
 import balle.world.objects.Pitch;
 
 public class SnapshotPredictor extends WorldSimulator {
 
     // Recreate object if you want to update the history
     private final ArrayList<ControllerHistoryElement> controllerHistory;
     private long                                      simulatorTimestamp;
 
     private final Goal                                opponentsGoal;
     private final Goal                                ownGoal;
     private final balle.world.objects.Robot initOurRobot;
     private final balle.world.objects.Robot initOpponent;
     private final balle.world.objects.Ball initBall;
 
     private final Pitch                               pitch;
 
     private final Logger LOG = Logger.getLogger(SnapshotPredictor.class);
 
     /**
      * Create new snapshot predictor
      * 
      * @param controllerHistory
      */
 
     public SnapshotPredictor(Snapshot initialSnapshot,
             ArrayList<ControllerHistoryElement> controllerHistory) {
         this(initialSnapshot.getOpponent(), initialSnapshot.getBalle(), initialSnapshot.getBall(),
                 initialSnapshot.getOpponentsGoal(), initialSnapshot.getOwnGoal(), initialSnapshot
                         .getPitch(), initialSnapshot.getTimestamp(), controllerHistory);
     }
 
     /**
      * Create new snapshot predictor
      * 
      * @param controllerHistory
      */
 
     public SnapshotPredictor(balle.world.objects.Robot opponent, balle.world.objects.Robot balle,
             Ball ball, Goal opponentsGoal, Goal ownGoal, Pitch pitch, long timestamp,
             ArrayList<ControllerHistoryElement> controllerHistory) {
         super(false);
         setWorld(new World(new Vec2(), true));
         initWorld();
         setVisionDelay(0);
         this.simulatorTimestamp = timestamp;
         this.controllerHistory = controllerHistory;
         this.opponentsGoal = opponentsGoal;
         this.ownGoal = ownGoal;
         this.pitch = pitch;
         this.initOpponent = opponent;
         this.initBall = ball;
         this.initOurRobot = balle;
         updatePositions(opponent, balle, ball);
     }
 
     /**
      * Roll the world simulation forward for some delta.
      * 
      * @param delta
      *            how much time to move forwards
      */
     protected synchronized void simulate(long delta) {
         long startTime = simulatorTimestamp;
         long endTime = simulatorTimestamp + delta;
 
         // TODO: add the wheelspeeds again
         // clean up the history (ensure there is at least one element left in
         // history)
         while (controllerHistory.size() > 1 && controllerHistory.get(0).getTimestamp() < startTime) {
             controllerHistory.remove(0);
         }
         //
         // // setup a simulator using the current snapshot (assume we are blue)
         float lastLPower = 0, lastRPower = 0;
         if (controllerHistory.size() > 0) {
             ControllerHistoryElement lastState = controllerHistory.get(0);
             lastLPower = lastState.getPowerLeft();
             lastRPower = lastState.getPowerRight();
         }
 
         // Assume we are always blue
         SoftBot virtual = getBlueSoft();
         virtual.setWheelSpeeds((int) lastLPower, (int) lastRPower);
 
         // use the controllerHistory to simulate the wheelspeeds
         // run a simulation while adjusting wheel speeds
 
         long maxTD = 50; // low values may make this less accurate
         for (int i = 0; i < controllerHistory.size(); i++) {
             ControllerHistoryElement curr = controllerHistory.get(i);
             long nextTime = endTime;
 
             if (i < controllerHistory.size() - 1)
                 nextTime = controllerHistory.get(i + 1).getTimestamp();
             for (long tD = maxTD; startTime < nextTime; tD = Math.min(startTime + tD, nextTime)
                     - startTime) {
                 // simulator.getBlueSoft().setWheelSpeeds(900, 900);
                 virtual.setWheelSpeeds(curr.getPowerLeft(), curr.getPowerRight());
                 update(tD);
                 getWorld().step(tD / 1000f, 8, 3);
                 // System.out.println(tD);
                 // System.out
                 // .println(world.getSnapshot().getBalle().getPosition());
                 startTime += tD;
             }
         }
 
 
 
         // Finish simulation off if we run out of controller commands
         
 		for (long currTime = startTime; currTime < endTime; currTime += 50) {
 			getWorld().step(Math.min(50, endTime - currTime) / 1000f, 8, 3);
             // TODO: this does not seem to simulate the ball movement if it is
             // not
             // moved by the other robots.
             // WTF?
         }
         // Update the timestamp
         simulatorTimestamp = endTime;
     }
 
     private void updatePositions(balle.world.objects.Robot opponent,
             balle.world.objects.Robot ourRobot, balle.world.objects.Ball ball) {
 
         balle.world.objects.Robot yRobot, bRobot;
         // Assume we are blues
         yRobot = opponent;
         bRobot = ourRobot;
 
         // Can see blue robot.
         if (bRobot.getPosition() != null) {
			setBlueRobotPosition(bRobot.getPosition().mult(SCALE),
					bRobot.getOrientation());
             blue.getBody().setLinearVelocity(bRobot.getVelocity().vec2(SCALE));
         } else {
             destroyRobot(blue, getBlueSoft());
         }
 
         // Can see yellow robot.
         if (yRobot.getPosition() != null) {
			setYellowRobotPosition(yRobot.getPosition().mult(SCALE),
					yRobot.getOrientation());
             yellow.getBody()
                     .setLinearVelocity(yRobot.getVelocity().vec2(SCALE));
         } else {
             destroyRobot(yellow, getYellowSoft());
         }
 
 		// Can see ball.
         if (ball.getPosition() != null) {
             setBallPosition(ball.getPosition().mult(SCALE), ball.getVelocity());
         } else {
             destroyBody(this.ball);
             this.ball = null;
         }
     }
 
     public Snapshot getSnapshotAfterTime(long deltaTime) {
         if (deltaTime > 0) {
             simulate(deltaTime);
         }
         return getSnapshot();
     }
 
     private Coord v2C(Vec2 v) {
         return new Coord(v.x, v.y);
     }
 
     private Coord v2C(Vec2 v, int estimatedFrames) {
         return new Coord(v.x, v.y, estimatedFrames);
     }
 
     private balle.world.objects.Robot getRobotFromBody(Robot robot,
             SoftBot softbot, balle.world.objects.Robot initRobot)
     {
 
         if ((robot == null) || (softbot.getBody() == null)
                 || initRobot.getPosition() == null)
             return new balle.world.objects.Robot(null, null, null, null);
         else {
             Body body = robot.getBody();
             return new balle.world.objects.Robot(v2C(body.getPosition(),
                     initRobot.getPosition().getEstimatedFrames()).div(
                     SCALE), Velocity.fromVec2(body.getLinearVelocity(), SCALE),
                     new AngularVelocity(body.getAngularVelocity(), 1000),
                     new Orientation(body.getAngle()));
         }
     }
 
     /**
      * Returns snapshot of the current state of the simulator
      * 
      * @return the snapshot
      */
     private Snapshot getSnapshot() {
         Body ballBody = ball;
         
         // Assume we are blue and opponent is yellow again
         balle.world.objects.Robot ourRobot = getRobotFromBody(blue,
                 getBlueSoft(), initOurRobot);
         balle.world.objects.Robot opponent = getRobotFromBody(yellow,
                 getYellowSoft(), initOpponent);
       
         balle.world.objects.Ball ball;
         if ((ballBody == null) || (initBall.getPosition() == null)) {
             ball = new Ball(null, null);
         } else {
             ball = new Ball(v2C(ballBody.getPosition(),
                     initBall.getPosition().getEstimatedFrames()).div(SCALE),
                     Velocity.fromVec2(ballBody.getLinearVelocity(), SCALE));
         }
         
         return new Snapshot(opponent, ourRobot, ball, getOpponentsGoal(), getOwnGoal(), getPitch(),
                 getSimulatorTimestamp(), getControllerHistory());
     }
 
     protected ArrayList<ControllerHistoryElement> getControllerHistory() {
         return controllerHistory;
     }
 
     public long getSimulatorTimestamp() {
         return simulatorTimestamp;
     }
 
     public void setSimulatorTimestamp(long simulatorTimestamp) {
         this.simulatorTimestamp = simulatorTimestamp;
     }
 
     protected Goal getOpponentsGoal() {
         return opponentsGoal;
     }
 
     protected Goal getOwnGoal() {
         return ownGoal;
     }
 
     protected Pitch getPitch() {
         return pitch;
     }
 
 }
