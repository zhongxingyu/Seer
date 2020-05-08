 package arpong.logic.game;
 
 import arpong.logic.ar.*;
 import arpong.logic.gameobjects.Ball;
 import arpong.logic.gameobjects.Paddle;
 import arpong.logic.gameobjects.TableWall;
 import arpong.logic.gameobjects.TennisTable;
 import arpong.logic.primitives.BoundingBox;
 import arpong.logic.primitives.Vector;
 import ru.knk.JavaGL.FPSCounter;
 import ru.knk.JavaGL.Interfaces.GameInterface;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class PongGame implements AgreedUponPongGameInterface, GameInterface {
 
     private TennisTable table;
     private Paddle firstPlayerPaddle;
     private Paddle secondPlayerPaddle;
     private Ball ball;
 
     private final RealityTracker realityTracker;
     private final VirtualRealityRenderer realityRenderer;
 
     private static final int firstPlayerPaddleId = 0;
     private static final int secondPlayerPaddleId = 1;
 
     private FPSCounter fpsCounter = new FPSCounter(5000, "JavaGL", "Game fps: ");
 
     public PongGame(VirtualRealityRenderer renderer, RealityTracker tracker) {
 //        this.realityTracker = new DumbRealityTracker();
         this.realityTracker = tracker;
         this.realityRenderer = renderer;
 
         firstPlayerPaddle = new Paddle(this.realityRenderer, firstPlayerPaddleId);
         secondPlayerPaddle = new Paddle(this.realityRenderer, secondPlayerPaddleId);
         ball = new Ball(this.realityRenderer);
         table = new TennisTable(this.realityRenderer);
 
         this.realityTracker.register(firstPlayerPaddleId, firstPlayerPaddle);
         this.realityTracker.register(secondPlayerPaddleId, secondPlayerPaddle);
 
         init();
     }
 
     private void init() {
        table.setPosition(new Vector(0, 0));
        table.setVelocity(new Vector(0, 0));

         ball.setPosition(table.getBoundingBox().center());
         ball.setVelocity(new Vector(2.5f, 1.5f));
 
         float tableWidth = table.getBoundingBox().denormilizedDiagonal().getX();
         float tableHeight = table.getBoundingBox().denormilizedDiagonal().getY();
 
         firstPlayerPaddle.setPosition(new Vector(tableWidth * 0.0005f, tableHeight * 0.3f));
         firstPlayerPaddle.setVelocity(new Vector(0, 1));
         secondPlayerPaddle.setPosition(new Vector(tableWidth - tableWidth * 0.0005f, tableHeight * 0.3f));
         secondPlayerPaddle.setVelocity(new Vector(0, 1));
     }
 
     public synchronized void tick() {
         //    ===================
         //(0) |    1   :   0  []| (2)
         //    |        :        |
         //    |[]      :        |
         //(1) |        :   *    | (3)
         //    ===================
         // [] --- paddle
         // * --- ball
         // 1, 0 --- score
         // () --- paddle steering button
         // First player's (#0) paddle is on the left
         // Second player's (#1) paddle is on the right
         Vector collisionPoint;
         if (table.getBoundingBox().isPointInside(ball.getPosition())) {
             Vector ballVelocity = ball.getVelocity();
             if ((collisionPoint = ball.collidesWith(firstPlayerPaddle)) != null) {
                 // 1st player hit the ball - send it back to the 2nd player
                 ball.setVelocity(new Vector(-ballVelocity.getX(),
                                              ballVelocity.getY()));
             } else if ((collisionPoint = ball.collidesWith(secondPlayerPaddle)) != null) {
                 // 2nd player hit the ball - send it back to the 1st player
                 ball.setVelocity(new Vector(-ballVelocity.getX(),
                                              ballVelocity.getY()));
             } else if ((collisionPoint = ball.collidesWith(table)) != null) {
                 wallCollision(table.wallForPoint(collisionPoint));
             }
         } else {    // ball is outside the box - return the ball to the position on the "surface" of the wall
             Vector ballPosition = ball.getPosition();
             BoundingBox tableBox = table.getBoundingBox();
             TableWall boxExitWall = table.wallForPoint(ballPosition);
             final float eps = 0.0001f;
             float dx = 0;
             float dy = 0;
             switch (boxExitWall) {
                 case LEFT_WALL:
                     dx += Math.abs(ballPosition.getX() - tableBox.getLowerLeft().getX()) + eps;
                     break;
                 case RIGHT_WALL:
                     dx -= Math.abs(ballPosition.getX() - tableBox.getUpperRight().getX()) + eps;
                     break;
                 case UPPER_WALL:
                     dy -= Math.abs(ballPosition.getY() - tableBox.getUpperRight().getY()) + eps;
                     break;
                 case LOWER_WALL:
                     dy += Math.abs(ballPosition.getY() - tableBox.getLowerLeft().getY()) + eps;
                     break;
             }
             ball.setPosition(ballPosition.plus(new Vector(dx, dy)));
             wallCollision(boxExitWall);
         }
 //        table.render();
         ball.move();
         ball.render();
 //        see also: Paddle::setPosition
 //        firstPlayerPaddle.render();
 //        secondPlayerPaddle.render();
 
         fpsCounter.update();
     }
     private void wallCollision(TableWall wall) {
         Vector ballVelocity = ball.getVelocity();
         switch (wall) {
             case LEFT_WALL:
                 table.incrementSecondPlayerScore();
                 ball.setVelocity(new Vector(-ballVelocity.getX(),
                                              ballVelocity.getY()));
                 break;
             case RIGHT_WALL:
                 table.incrementFirstPlayerScore();
                 ball.setVelocity(new Vector(-ballVelocity.getX(),
                                              ballVelocity.getY()));
                 break;
             case UPPER_WALL:
                 ball.setVelocity(new Vector( ballVelocity.getX(),
                                                 /*-ballVelocity.getY()*/
                                             -Math.abs(ballVelocity.getY())));
                 break;
             case LOWER_WALL:
                 ball.setVelocity(new Vector( ballVelocity.getX(),
                                                 /*-ballVelocity.getY()*/
                                             Math.abs(ballVelocity.getY())));
                 break;
         }
     }
 
     // To be called by Recognition
     public synchronized void updatePlayerPaddleLocal(int paddleId, float x, float y) {
         realityTracker.updatePosition(paddleId, new Vector(x, y));
     }
     public synchronized void updateSteeringButton(int buttonId, boolean pressed) {
         Map<Integer, Integer> steerableButton = new HashMap<Integer, Integer>();
         steerableButton.put(0, firstPlayerPaddleId);
         steerableButton.put(1, firstPlayerPaddleId);
         steerableButton.put(2, secondPlayerPaddleId);
         steerableButton.put(3, secondPlayerPaddleId);
 
         SteeringDirection direction = SteeringDirection.NEUTRAL;
         // top button's ids have even values
         if (pressed && (buttonId % 2 == 0)) {
             direction = SteeringDirection.TOP;
         } else if (pressed) {
             direction = SteeringDirection.BOTTOM;
         }
 
         steerObject(steerableButton.get(buttonId), direction);
     }
 
     // To be called by Graphics
     public float getXMins() {
         return table.getBoundingBox().getLowerLeft().getX();
     }
     public float getXMaxs() {
         return table.getBoundingBox().getUpperRight().getX();
     }
     public float getYMins() {
         return table.getBoundingBox().getLowerLeft().getY();
     }
     public float getYMaxs() {
         return table.getBoundingBox().getUpperRight().getY();
     }
     public float getBallRadius() {
         Vector lowerLeft = ball.getBoundingBox().getLowerLeft();
         Vector upperRight = ball.getBoundingBox().getUpperRight();
         float width = Math.abs(upperRight.getX() - lowerLeft.getX());
         float height = Math.abs(upperRight.getY() - lowerLeft.getY());
         return Math.min(width, height) / 2;
     }
     public float getPaddleXSize(int paddleId) {
         return getPaddleById(paddleId).getBoundingBox().denormilizedDiagonal().getX();
     }
     public float getPaddleYSize(int paddleId) {
         return getPaddleById(paddleId).getBoundingBox().denormilizedDiagonal().getY();
     }
 
     private void steerObject(int objectId, SteeringDirection direction) {
         TrackableObject steeredObject = realityTracker.getObjectById(objectId);
         Vector position = steeredObject.getPosition();
         float dy = 0; // direction == neutral
         switch (direction) {
             case TOP:
                 dy = 5;
                 break;
             case BOTTOM:
                 dy = -5;
                 break;
         }
         realityTracker.updatePosition(steeredObject, new Vector(position.getX(),
                 position.getY() + dy));
     }
     private Paddle getPaddleById(int paddleId) {
         Paddle paddle = null;
         if (paddleId == firstPlayerPaddleId) {
             paddle = firstPlayerPaddle;
         } else if (paddleId == secondPlayerPaddleId) {
             paddle = secondPlayerPaddle;
         }
         return paddle;
     }
 }
