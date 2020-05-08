 package balle.strategy;
 
 import java.awt.Color;
 import java.util.Iterator;
 
 import org.apache.log4j.Logger;
 
 import balle.controller.Controller;
 import balle.main.drawable.Circle;
 import balle.main.drawable.Dot;
 import balle.main.drawable.DrawableLine;
 import balle.main.drawable.DrawableVector;
 import balle.misc.Globals;
 import balle.strategy.bezierNav.BezierNav;
 import balle.strategy.curve.CustomCHI;
 import balle.strategy.executor.movement.GoToObjectPFN;
 import balle.strategy.executor.movement.MovementExecutor;
 import balle.strategy.executor.movement.OrientedMovementExecutor;
 import balle.strategy.friendly1.GoToBall;
 import balle.strategy.pathfinding.SimplePathFinder;
 import balle.strategy.planner.AbstractPlanner;
 import balle.world.Coord;
 import balle.world.Line;
 import balle.world.Orientation;
 import balle.world.Snapshot;
 import balle.world.Velocity;
 import balle.world.objects.Ball;
 import balle.world.objects.CircularBuffer;
 import balle.world.objects.Goal;
 import balle.world.objects.Point;
 import balle.world.objects.Robot;
 
 public class Interception extends AbstractPlanner {
 
     private int     ballCount          = 0;
     private int     countNeeded        = 6;              // require 6 readings
                                                           // of ball position
     private boolean ballHasMoved = false;
     private Coord   intercept          = new Coord(0, 0);
     private double  lineLength         = 200;
     private boolean shouldPlayGame;
     private static final double STRATEGY_STOP_DISTANCE = Globals.ROBOT_LENGTH + 0.05;
 
     protected final boolean useCpOnly;
 	protected final boolean mirror;
     protected CircularBuffer<Coord> ballCoordBuffer;
 
     private static Logger LOG                = Logger.getLogger(Interception.class);
 
     private MovementExecutor movementExecutor;
     private OrientedMovementExecutor orientedMovementExecutor;
     private AbstractPlanner gameStrategy;
 
     protected void setIAmDoing(String message) {
         LOG.info(message);
     }
 
 	public Interception(boolean useCpOnly, boolean mirror,
 			MovementExecutor movementExecutor,
             OrientedMovementExecutor orientedMovementExecutor) {
         super();
         ballCoordBuffer = new CircularBuffer<Coord>(6);
         this.useCpOnly = useCpOnly;
 		this.mirror = mirror;
         
         this.movementExecutor = movementExecutor;
         this.orientedMovementExecutor = orientedMovementExecutor;
         shouldPlayGame = false;
         this.gameStrategy = new Game(new GoToBall(new GoToObjectPFN(0)), false);
     }
 
 
     @Override
     public void onStep(Controller controller, Snapshot snapshot) {
 
         Coord optimum = new Coord(0, 0);
         Goal goal = snapshot.getOwnGoal();
         Ball ball = snapshot.getBall();
         if (ball.getPosition() == null)
             return;
 
         ballCoordBuffer.add(ball.getPosition());
 
         if (ballIsMoving(ball) /* && !predictionCoordSet */) {
 			intercept = getPredictionCoordVelocityvector(snapshot, useCpOnly,
 					mirror);
             ballHasMoved = true;
 
             if (!shouldPlayGame) {
                 addDrawable(new Circle(snapshot.getBalle().getPosition(),
                         STRATEGY_STOP_DISTANCE, Color.red));
             }
             if (snapshot.getBalle().getPosition()
                     .dist(snapshot.getBall().getPosition()) < STRATEGY_STOP_DISTANCE)
                 shouldPlayGame = true;
 
         }
 
         if (shouldPlayGame) {
             setIAmDoing("GAME!");
             gameStrategy.step(controller, snapshot);
         } else if (ballHasMoved) {
             setIAmDoing("Going to point - predict");
             addDrawable(new Dot(intercept, Color.BLACK));
             if (movementExecutor != null) {
                 movementExecutor.updateTarget(new Point(intercept));
                 addDrawables(movementExecutor.getDrawables());
                 movementExecutor.step(controller, snapshot);
             } else if (orientedMovementExecutor != null) {
                 orientedMovementExecutor.updateTarget(new Point(intercept),
                         snapshot.getOpponentsGoal().getGoalLine().midpoint()
                                 .sub(intercept).orientation());
                 addDrawables(orientedMovementExecutor.getDrawables());
                 orientedMovementExecutor.step(controller, snapshot);
             }
         } else {
             setIAmDoing("Waiting");
             controller.stop();
         }
     }
 
     @FactoryMethod(designator = "InterceptsM4-CP-PFN")
     public static final Interception factoryCPPFN() {
 		return new Interception(true, true, new GoToObjectPFN(
                 Globals.ROBOT_LENGTH / 3), null);
     }
 
     @FactoryMethod(designator = "InterceptsM4-NCP-PFN")
     public static final Interception factoryNCPPFN() {
 		return new Interception(false, true, new GoToObjectPFN(
                 Globals.ROBOT_LENGTH / 3), null);
     }
 
     @FactoryMethod(designator = "InterceptsM4-CP-PFNF")
     public static final Interception factoryCPPFNF() {
 		return new Interception(true, true, new GoToObjectPFN(
                 Globals.ROBOT_LENGTH / 3, false), null);
     }
 
     @FactoryMethod(designator = "InterceptsM4-NCP-PFNF")
     public static final Interception factoryNCPPFNF() {
 		return new Interception(false, true, new GoToObjectPFN(
                 Globals.ROBOT_LENGTH / 3, false), null);
     }
 
     @FactoryMethod(designator = "InterceptsM4-CP-BZR")
     public static final Interception factoryCPBZR() {
 		return new Interception(true, true, null, new BezierNav(
 				new SimplePathFinder(
                 new CustomCHI())));
     }
 
     @FactoryMethod(designator = "InterceptsM4-NCP-BZR")
     public static final Interception factoryNCPBZR() {
 		return new Interception(false, true, null, new BezierNav(
                 new SimplePathFinder(
                 new CustomCHI())));
     }
 
     /**
      * Checks when ball has moved since last reading
      * 
      * @return
      */
     private boolean ballIsMoving(Ball ball) {
         Iterator<Coord> it = ballCoordBuffer.iterator();
         Coord lastKnownLoc = null;
         while (it.hasNext())
             lastKnownLoc = it.next();
         
         if (lastKnownLoc == null)
             return false;
         
         if (lastKnownLoc.dist(ball.getPosition()) > 0.05) {
             return true;
         } else {
             return false;
         }
     }
 
 
 	protected Coord getPredictionCoordVelocityvector(Snapshot s,
 			boolean useCPOnly, boolean mirror) {
         Ball ball = s.getBall();
         Robot ourRobot = s.getBalle();
 
 		Coord ballPos, currPos;
 		ballPos = ball.getPosition();
 		currPos = ourRobot.getPosition();
 
 		if (mirror
				&& (s.getPitch().getHalf(ourRobot.getPosition()) != s
						.getPitch().getHalf(ball.getPosition()))) {
 
 			// Mirror X position.
 			double dX = currPos.getX() - s.getPitch().getPosition().getX();
			currPos = new Coord(currPos.getX() - dX, currPos.getY());
 
 		}
 
         Velocity vel = ball.getVelocity();
         Coord vec = new Coord(vel.getX(), vel.getY());
         vec = vec.mult(0.5 / vec.abs());
         
 		Line ballRobotLine = new Line(ballPos, currPos);
         // .extendBothDirections(Globals.PITCH_WIDTH);
         //addDrawable(new DrawableLine(ballRobotLine, Color.WHITE));
 
 		Line ballDirectionLine = new Line(ballPos, currPos.add(vec));
         ballDirectionLine = ballDirectionLine.extend(Globals.PITCH_WIDTH);
         //addDrawable(new DrawableLine(ballDirectionLine, Color.WHITE));
 
 
         Line rotatedRobotBallLine = ballRobotLine.rotateAroundPoint(
                 ballRobotLine.midpoint(), Orientation.rightAngle)
                 .extendBothDirections(Globals.PITCH_WIDTH);
         //addDrawable(new DrawableLine(rotatedRobotBallLine, Color.PINK));
 
         Coord pivot = rotatedRobotBallLine.getIntersect(ballDirectionLine);
        
 		Coord CP = ballDirectionLine.closestPoint(currPos);
         addDrawable(new DrawableLine(ballDirectionLine, Color.WHITE));
         addDrawable(new Dot(CP, Color.WHITE));
 		addDrawable(new DrawableLine(new Line(CP, currPos),
                 Color.CYAN));
         // addDrawable(new DrawableLine(new Line(CP, ball.getPosition()),
         // Color.ORANGE));
 
         
 		if (useCPOnly) {
 			// Coord earlier = CP.add(ball.getPosition().sub(CP).getUnitCoord()
 			// .mult(0.4));
 //			return earlier;
 			return CP;
 		}
         
         if (pivot == null)
             return CP;
         
         //addDrawable(new Dot(pivot, Color.RED));
 
         Coord scaler = CP.sub(pivot);
		Orientation theta = ballPos.angleBetween(
         ourRobot.getPosition(), CP);
		Orientation theta2 = ballPos.angleBetween(CP,
         ourRobot.getPosition());
         
          double minTheta = Math.min(theta.radians(), theta2.radians());
         
          // addDrawable(new Label(String.format("Theta: %.2f", minTheta),
          // new Coord(0, -0.15), Color.CYAN));
         
 
         scaler = scaler.mult(Math.abs(minTheta) / (Math.PI / 2));
         
 		Coord predictCoord = pivot.sub(scaler);
 		Line robotPredictLine = new Line(currPos, predictCoord);
         // robotPredictLine = robotPredictLine.extend(0.5);
         addDrawable(new DrawableLine(robotPredictLine, Color.PINK));
         predictCoord = robotPredictLine.getB();
 
          // addDrawable(new DrawableLine(rotatedRobotBallLine, Color.PINK));
         
          // Coord predictCoord = rotatedRobotBallLine
          // .getIntersect(ballDirectionLine);
         
          // if (predictCoord == null) {
          // // if the lines do not intersect jsut get a point thats 1m away
         // from
          // // the ball
          //
          // predictCoord = ball.getPosition().add(vec);
          // }
          //
          // //addDrawable(new DrawableVector(ball.getPosition(), vec,
          // Color.WHITE));
         addDrawable(new DrawableVector(pivot, scaler, Color.BLACK));
         return predictCoord;
     }
 
 
 }
