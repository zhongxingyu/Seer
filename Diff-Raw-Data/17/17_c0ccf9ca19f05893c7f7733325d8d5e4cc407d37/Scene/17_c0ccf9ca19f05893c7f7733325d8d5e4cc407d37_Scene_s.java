 package se2.cleaningRobot.simulation;
 
 import java.awt.*;
 import java.awt.geom.*;
 import java.lang.*;
 import java.util.*;
 import javax.swing.JPanel;
 
 import se2.cleaningRobot.control.*;
 
 /**
  * @author SE2 Gruppe 8
  *
  * Drawing of the simulation
  */
 public class Scene extends JPanel implements Observer {
 
 	private CRobot robot;
   private ArrayList<Shape> obstacleList = new ArrayList<Shape>();
 
   // robot shapes
   private Shape robotBody, robotDisplay, robotVacuum;
   private Shape irFront, irLeft, irRight;
   private Shape irFrontRange, irLeftRange, irRightRange;
   private Shape bsFront, bsLeft, bsRight;
   private Shape bodyDummy, irFrontDummy, irLeftDummy, irRightDummy;
 
   // dotted line properties
   private static final BasicStroke dotted = new BasicStroke(1f, BasicStroke.CAP_BUTT,
                                                             BasicStroke.JOIN_ROUND,
                                                             1f, new float[] {2f}, 0f);
 
 
   public Scene(CRobot robot) {
     setBackground(Color.WHITE);
 
     this.robot = robot;
     this.robot.addObserver(this);
     this.robot.setPosition(50.0f, 50.0f);
 
     // init robot shapes
     int[] xDisplay = { -15, - 9, -9, -15 };
     int[] yDisplay = { - 8, - 8,  8,   8 };
     int[] xVacuum  = {   0,  13, 13,   0 };
     int[] yVacuum  = { -13, -13, 13,  13 };
     robotBody    = new Arc2D.Float(-this.robot.radius,  -this.robot.radius,
                                     this.robot.radius*2, this.robot.radius*2,
                                     0, 360, Arc2D.PIE);
     robotDisplay = new Polygon(xDisplay, yDisplay, xDisplay.length);
     robotVacuum  = new Polygon(xVacuum,  yVacuum,  xVacuum.length);
 
     // init infrared sensor shapes
     float degL1 = this.robot.irLeft.angle-7;
     float degL2 = this.robot.irLeft.angle+7;
     float degR1 = this.robot.irRight.angle-7;
     float degR2 = this.robot.irRight.angle+7;
     irFront = new Line2D.Float(this.robot.irFront.dx, -3, this.robot.irFront.dx, +3);
     irLeft  = new Line2D.Double(this.robot.irLeft.dx  * Math.cos(Math.toRadians(degL1)),
                                 this.robot.irLeft.dx  * Math.sin(Math.toRadians(degL1)),
                                 this.robot.irLeft.dx  * Math.cos(Math.toRadians(degL2)),
                                 this.robot.irLeft.dx  * Math.sin(Math.toRadians(degL2)));
     irRight = new Line2D.Double(this.robot.irRight.dx * Math.cos(Math.toRadians(degR1)),
                                 this.robot.irRight.dx * Math.sin(Math.toRadians(degR1)),
                                 this.robot.irRight.dx * Math.cos(Math.toRadians(degR2)),
                                 this.robot.irRight.dx * Math.sin(Math.toRadians(degR2)));
 
     //double xFront = (this.robot.irFront.dx + this.robot.irFront.range) *
                     //Math.cos(Math.toRadians(this.robot.irFront.angle));
     //double yFront = (this.robot.irFront.dx  + this.robot.irFront.range) *
                     //Math.sin(Math.toRadians(this.robot.irFront.angle));
     double xLeft  = (this.robot.irLeft.dx  + this.robot.irLeft.range) *
                     Math.cos(Math.toRadians(this.robot.irLeft.angle));
     double yLeft  = (this.robot.irLeft.dx  + this.robot.irLeft.range) *
                     Math.sin(Math.toRadians(this.robot.irLeft.angle));
     double xRight = (this.robot.irRight.dx + this.robot.irRight.range) *
                     Math.cos(Math.toRadians(this.robot.irRight.angle));
     double yRight = (this.robot.irRight.dx + this.robot.irRight.range) *
                     Math.sin(Math.toRadians(this.robot.irRight.angle));
 
     // init infrared sensor range shapes
     irFrontRange  = new Line2D.Float(this.robot.irFront.dx + this.robot.irFront.range, 0,
                                      this.robot.irFront.dx + this.robot.irFront.range, 0);
     irLeftRange   = new Line2D.Double( xLeft,  yLeft,  xLeft,  yLeft);
     irRightRange  = new Line2D.Double(xRight, yRight, xRight, yRight);
 
     // init bumperswitch shapes
     bsFront = new Arc2D.Float(-this.robot.radius,  -this.robot.radius,
                                this.robot.radius*2, this.robot.radius*2,
                               -27, 54, Arc2D.OPEN);
     bsLeft  = new Arc2D.Float(-this.robot.radius,  -this.robot.radius,
                                this.robot.radius*2, this.robot.radius*2,
                               -85, 50, Arc2D.OPEN);
     bsRight = new Arc2D.Float(-this.robot.radius,  -this.robot.radius,
                                this.robot.radius*2, this.robot.radius*2,
                                35, 50, Arc2D.OPEN);
 
     // init robot dummy shapes (for collision detection)
     bodyDummy    = robotBody;
     irFrontDummy = irFrontRange;
     irLeftDummy  = irLeftRange;
     irRightDummy = irRightRange;
 
     // add borders as obstacles
     addObstacle(new Rectangle2D.Float(-100, -100, 103, 700)); // west 
     addObstacle(new Rectangle2D.Float(-100, -100, 700, 103)); // north
     addObstacle(new Rectangle2D.Float( 497, -100, 100, 700)); // east
     addObstacle(new Rectangle2D.Float(-100,  497, 700, 100)); // south
 
     // add other objects as obstacles to the scene
     addObstacle(new Rectangle2D.Float( 70, 100,  50,  50));
     addObstacle(new Rectangle2D.Float( 60, 300,  50, 100));
     addObstacle(new Rectangle2D.Float(110, 350,  50,  50));
     addObstacle(new Rectangle2D.Float(250, 180, 170, 200));
   }
 	
   @Override
 	public void paintComponent(Graphics g) {
     super.paintComponent(g);
 
     // provide a better graphics class
     Graphics2D g2 = (Graphics2D) g.create();
     g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);
 
     // draw scene objects
     drawAllObstacles(g2);
     drawRobot(g2);
 
     g2.dispose();
 	}	
 
   @Override
   public void update(Observable o, Object arg) {
     //System.out.println("UPDATE Scene");
     repaint();
   }
 
   public void addObstacle(Shape obstacle) {
     obstacleList.add(obstacle);
   }
 
   private void drawAllObstacles(Graphics2D g2) {
     for (int i=0; i < obstacleList.size(); i++) {
       Shape ob = obstacleList.get(i);
       g2.setColor(Color.BLACK);
       g2.fill(ob);
     }
   }
 
   private void drawRobot(Graphics2D g2) {
     //calculatePoints();
 
     // save current transform
     AffineTransform at_old = g2.getTransform();
 
     // temporary transform for rotation and translation
     AffineTransform at_new = new AffineTransform(at_old);
     at_new.translate(this.robot.pos.x, this.robot.pos.y);
     at_new.rotate(Math.toRadians(this.robot.getAngle()));
     g2.setTransform(at_new);
 
     // draw robot shapes
     g2.setColor(Color.GRAY);
     g2.fill(robotBody);
     g2.setColor(Color.CYAN);
     g2.fill(robotDisplay);
     g2.setStroke(dotted);
     g2.setColor(Color.DARK_GRAY);
     g2.draw(robotVacuum);
     g2.setStroke(new BasicStroke());
 
     // draw sensor shapes
     g2.setColor(Color.ORANGE);
     g2.draw(irFront);
     g2.draw(irLeft);
     g2.draw(irRight);
     g2.setColor(Color.RED);
     g2.draw(irFrontRange);
     g2.draw(irLeftRange);
     g2.draw(irRightRange);
     g2.setColor(Color.BLUE);
     g2.draw(bsFront);
     g2.draw(bsLeft);
     g2.draw(bsRight);
 
     // create transformed shapes
     bodyDummy    = at_new.createTransformedShape(robotBody);
     irFrontDummy = at_new.createTransformedShape(irFrontRange);
     irLeftDummy  = at_new.createTransformedShape(irLeftRange);
     irRightDummy = at_new.createTransformedShape(irRightRange);
 
     // return to the saved transform
     g2.setTransform(at_old);
 
     if (isCollided()) {
       this.robot.resetPosition();
     }
   }
 
   private boolean isCollided() {
     Point2D irFrontP = new Point2D.Double(irFrontDummy.getBounds2D().getX(),
                                           irFrontDummy.getBounds2D().getY());
     Point2D irLeftP  = new Point2D.Double(irLeftDummy.getBounds2D().getX(),
                                           irLeftDummy.getBounds2D().getY());
     Point2D irRightP = new Point2D.Double(irRightDummy.getBounds2D().getX(),
                                           irRightDummy.getBounds2D().getY());
 
     for (Shape obs : obstacleList) {
       // infrared sensor (front) range detection
       if (obs.contains(irFrontP)) {
         this.robot.irFront.report();
         return true;
       } else if (obs.contains(irLeftP)) {
         this.robot.irLeft.report();
         return true;
       } else if (obs.contains(irRightP)) {
         this.robot.irRight.report();
         return true;
       }
       // check graphical collision (no need for simulation)
       if (bodyDummy.intersects(obs.getBounds2D())) {
         this.robot.bsFront.setPushed();
         return true;
       }
     }
     return false;
   }
 }
