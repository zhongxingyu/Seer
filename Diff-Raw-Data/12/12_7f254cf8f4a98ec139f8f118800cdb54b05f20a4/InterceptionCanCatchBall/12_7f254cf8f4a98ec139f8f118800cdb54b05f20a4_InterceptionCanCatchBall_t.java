 package balle.strategy;
 
 import java.awt.Color;
 import java.util.Iterator;
 
 import org.apache.log4j.Logger;
 
 import balle.controller.Controller;
 import balle.main.drawable.Circle;
 import balle.main.drawable.Dot;
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
 import balle.world.Snapshot;
 import balle.world.objects.Ball;
 import balle.world.objects.CircularBuffer;
 import balle.world.objects.Goal;
 import balle.world.objects.Point;
 
 public class InterceptionCanCatchBall extends AbstractPlanner {
 
     private int     ballCount          = 0;
     private int     countNeeded        = 6;              // require 6 readings
                                                           // of ball position
     private boolean ballHasMoved = false;
     private Coord   intercept          = new Coord(0, 0);
     private double  lineLength         = 200;
     private boolean shouldPlayGame;
     private static final double STRATEGY_STOP_DISTANCE = Globals.ROBOT_LENGTH + 0.05;
 
 	protected final boolean mirror;
     protected CircularBuffer<Coord> ballCoordBuffer;
 
     private static Logger LOG                = Logger.getLogger(InterceptionCanCatchBall.class);
 
     private MovementExecutor movementExecutor;
     private OrientedMovementExecutor orientedMovementExecutor;
     private AbstractPlanner gameStrategy;
 
     protected void setIAmDoing(String message) {
         LOG.info(message);
     }
 
 	public InterceptionCanCatchBall(boolean mirror,
 			MovementExecutor movementExecutor,
             OrientedMovementExecutor orientedMovementExecutor) {
         super();
         ballCoordBuffer = new CircularBuffer<Coord>(6);
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
 
		if (snapshot.getBalle().getPosition() == null) {
			return;
		}

 		if (ballIsMoving(ball)
 				&& intercept.dist(snapshot.getBalle().getPosition()) > 0.1 /*
 																			 * &&
 																			 * !
 																			 * predictionCoordSet
 																			 */) {
 			intercept = getPredictionCoordVelocityvector(snapshot, mirror);
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
 
 	@FactoryMethod(designator = "AAAAAA Slow Intercepts BZR")
     public static final InterceptionCanCatchBall factoryNCPBZR() {
 		return new InterceptionCanCatchBall(true, null, new BezierNav(
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
 
 
 	protected Coord getPredictionCoordVelocityvector(Snapshot s, boolean mirror) {
 		Ball ball = s.getBall();
 
         Coord bp = ball.getPosition();
         Coord bv = ball.getVelocity();
         if(bv.abs() == 0) {
         	bv = bp.sub(s.getOpponent().getPosition());
         }
 		bv = bv.getUnitCoord().mult(5);
 		Line ballPath = new Line(bp, bp.add(bv));
 		Coord wallIntersect = null;
 		for (Line l : s.getPitch().getWalls()) {
 			if (l.intersects(ballPath)) {
 				wallIntersect = l.getIntersect(ballPath);
 				break;
 			}
 		}
 		// move towards ball a bit
 		if (wallIntersect == null) {
 			System.out.println("NO WALL INTERSECT?!??!?!?!");
 			return null;
 		}
 		Coord target = wallIntersect.add(bv.getUnitCoord().mult(-0.4));
 		return target;
         
         
         
         
     }
 
 
 }
