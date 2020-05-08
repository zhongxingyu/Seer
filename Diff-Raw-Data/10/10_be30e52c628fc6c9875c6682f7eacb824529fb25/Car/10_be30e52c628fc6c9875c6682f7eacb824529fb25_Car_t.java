 /*
  * Author: Sean Wild
  */
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.media.opengl.GL2;
 
 public class Car extends TimerTask {
 
   //*******************************
   //Fields	
   //*******************************	
 
   public enum TurnStatus { NOT_TURNING, TURNING_LEFT, TURNING_RIGHT };
   public enum Acceleration { NOT_ACCELERATING, FORWARD, BACKWARDS };
 	
   private static final double DEFAULT_COLLISION_RADIUS = 50;
   
   private static final String modelPath = "models/ferrari.obj";
   
   private static final double drag = 0.9;
   
   private static final double maxangle = 45.0;
   
   private static final int updateRate = 40;
   
   private static final double facingacc = 60.0;
   
   private static final double baseacc = 1.0;
   
   
   
   private static Model model = new Model(modelPath);
   
   private static Timer timer = new Timer(true);
 
   private static Car[] cars = new Car[8];
   
   
   
   private TurnStatus turning = TurnStatus.NOT_TURNING;
   
   //Position
   private double xpos = 0, ypos = 0, zpos = 0;
   
   //Velocity
   private double xvel = 0, yvel = 0, zvel = 0;
   
   //Acceleration
   private double xacc = 0, yacc = 0, zacc = 0;
   
   private double facing = 0, facingvel = 0;
   
   /* Owner/controller of the car
    * 0 = Computer
    * Any other value is a player
    */
   private int owner = 0;
  
   /* Distance from the centre of the car
    * that the car will collide with objects
    */
   private double collision_radius = DEFAULT_COLLISION_RADIUS;
   
   //********************************
   //Constructors
   //********************************
   public Car(int controller, double x, double y, double z, double faceAngle) {
 	  owner = controller;
 	  xpos = x;
 	  ypos = y;
 	  zpos = z;
 	  facing = faceAngle;
 	  timer.scheduleAtFixedRate(this , 0, updateRate);
 	  
 	  if (controller > 0 && controller <= 8)
 		  cars[controller] = this;
   }
   
   public static Car getPlayerCar(int controller) {
 	  return cars[controller];
   }
   
   public void run() {
 	  double turnacc = 0;
 	  double frequency = updateRate/1000.0;
 	  double veldirection = Math.toDegrees(Math.atan2(-zvel,xvel));
 	  
 	  if (veldirection < 0)
 		  veldirection += 360;
 	  
 	  if (turning == TurnStatus.NOT_TURNING) {
 		  if (facing > veldirection)
 			  turnacc = 1/-facingacc;
 		  if (facing < veldirection)
 			  turnacc = 1/facingacc;
 		  
 	  } else if (turning == TurnStatus.TURNING_LEFT) {
 		  if (facing - veldirection > maxangle) {
 			  turning = TurnStatus.NOT_TURNING;
 			  turnacc = 0;
 			  facingvel = 0;
 		  } else
 		      turnacc = facingacc;
 	  } else if (turning == TurnStatus.TURNING_RIGHT){
 		  if (facing - veldirection < -maxangle) {
 			  turning = TurnStatus.NOT_TURNING;
 			  turnacc = 0;
 			  facingvel = 0;
 		  } else
 		      turnacc = -facingacc;
 	  }
 	  
 		  
 	  if (facing > 360)
 		  facing = 0;
 	  if (facing < 0)
 		  facing = 360;
 	  
 	  xvel += (xacc - Math.signum(xvel) * (0.5 * drag * xvel * xvel)) * frequency;
 	  xpos += xvel * frequency;
 	  yvel += yacc * frequency;
 	  ypos += yacc * frequency;
 	  zvel += -(zacc - Math.signum(-zvel) *(0.5 * drag * zvel * zvel)) * frequency;
 	  zpos += zvel * frequency;
 	  facingvel += turnacc * frequency;
 	  facing += facingvel * frequency;
 	  //System.out.println("xvel: " + xvel + "  xpos: " + xpos);
 	  //System.out.println("yvel: " + yvel + "  ypos: " + ypos);
 	  //System.out.println("zvel: " + zvel + "  zpos: " + zpos);
   }
   
   public void turn(int direction) {
 	 switch (direction) {
 	   case 1:
 		   turning = TurnStatus.TURNING_LEFT;
 		   break;
 	   case -1:
 		   turning = TurnStatus.TURNING_RIGHT;
 		   break;
 	   default:
 		   turning = TurnStatus.NOT_TURNING;
 	 }
   }
   
   public void draw(GL2 gl) {
 	  gl.glPushMatrix();
	  gl.glScalef(0.2f, 0.2f, 0.2f);
 	  gl.glTranslatef((float)xpos, (float)ypos, (float)zpos);
 	  gl.glRotatef(90 + (float) facing, 0.0f, 1.0f, 0.0f);
 	  model.draw(gl);
 	  gl.glPopMatrix();
   }
   
   public static void modelInit(GL2 gl) {
 	  model.buildList(gl);
   }
   
   //********************************
   //Methods
   //********************************
   
   //Get/Set methods for owner/controller
   int getController() {
 	  return owner;
   }
   
   void setController(int controller) {
 	  owner = controller;
   }
   
   //Get/Set methods for collision radius
   double getCollision() {
 	  return collision_radius;
   }
   
   void setCollision(double collision) {
 	  collision_radius = collision;
   }
   
   double getFacingAngle() {
 	  return facing;
   }
   
   void setFacingAngle(double degrees) {
 	  facing = degrees;
   }
   
   //Get methods for pos, vel, acc
   double getXPos() {
 	  return xpos;
   }
   
   double getYPos() {
 	  return ypos;
   }
   
   double getZPos() {
 	  return zpos;
   }
   
   double getXVelocity() {
 	  return xvel;
   }
   
   double getYVelocity() {
 	  return yvel;
   }
   
   double getZVelocity() {
 	  return zvel;
   }
   
   double getXAccel() {
 	  return xacc;
   }
   
   double getYAccel() {
 	  return yacc;
   }
   
   double getZAccel() {
 	  return zacc;
   }
   
   //Set methods for pos, vel, acc
   void setXPos(double newxpos) {
 	  xpos = newxpos;
   }
   
   void setYPos(double newypos) {
 	  ypos = newypos;
   }
   
   void setZPos(double newzpos) {
 	  zpos = newzpos;
   }
   
   void setXVelocity(double newxvel) {
 	  xvel = newxvel;
   }
   
   void setYVelocity(double newyvel) {
 	  yvel = newyvel;
   }
   
   void setZVelocity(double newzvel) {
 	  zvel = newzvel;
   }
   
   void setXAccel(double newxacc) {
 	  xacc = newxacc;
   }
   
   void setYAccel(double newyacc) {
 	  yacc = newyacc;
   }
   
   void setZAccel(double newzacc) {
 	  zacc = newzacc;
   }
 
 }
