 /*
  *  Copyright Mattias Liljeson Sep 14, 2011
  */
 package gameserver;
 
 import common.CarUpdate;
 import common.KeyStates;
 import java.awt.*;
 import java.awt.geom.Line2D;
 import java.awt.image.BufferedImage;
 
 /**
  *
  * @author Mattias Liljeson <mattiasliljeson.gmail.com>
  */
 public class Car {
     // Force constants. All units are per second
     public static final double FACTOR_BREAK = 130;
     public static final double FACTOR_ACC = 130;
     public static final double FACTOR_TURN = 3;
     public static final double FACTOR_MAX_FRICTION = 0.85; // maximum friction is applied by multiplying speed with (1 - FACTOR_MAX_FRICTION)
     
     private KeyStates keyStates;
 	private double posX, posY;
     private double direction;
     private double speed;
     private Color color;
     private CarUpdate carUpdate;
 	private int nextCheckpoint = 0;
     private String name;
     
     public Car(Color color, String name){
         this(100,100,100,color);
     }
     
 //	public Car(double posX, double posY, double direction)
 //	{
 //		this(posX, posY, direction, Color.blue);
 //	}
 	
     public Car(double posX, double posY, double direction, Color color){
         this.direction = direction;
 		this.posX = posX;
 		this.posY = posY;
         this.color = color;
 		
         keyStates = new KeyStates();
         carUpdate = new CarUpdate(posX, posY, direction, color, 0);
     }
     
     public CarUpdate getCarUpdate() {
 		carUpdate.posX = posX;
 		carUpdate.posY = posY;
         carUpdate.rotation = direction;
 		carUpdate.nextCheckpoint = nextCheckpoint;
         return carUpdate;
     }
     
     public Color getColor() {
         return color;
     }
     
     public double getX() {
 		return posX;
 	}
 	
 	public double getY() {
 		return posY;
 	}
     
     public void setKeyStates(KeyStates keyStates){
         this.keyStates = keyStates;
     }
     
 	//TODO: pass image and checkpoints in constructor only
     public void update(double dt, BufferedImage frictionMaskBuffImg, Line2D[] checkpoints){
 		//----------------------------------------------------------------------
 		// Friction
 		
 		/*
          * - Sample friction from friction mask in the raceCourse
 		 * frictionConstant is calculated in this interval:
          *   frictionColor = 0 -> frictionConstant = 1
 		 *   frictionColor = 255 -> frictionConstant = 1 - FACTOR_MAX_FRICTION
 		 * then multiplied with speed taking delta time into account
 		 */
 		try {
 			Color frictionColor = new Color(frictionMaskBuffImg.getRGB((int)posX, (int)posY));
 			double frictionConstant = 1 - (frictionColor.getBlue()/255.0*FACTOR_MAX_FRICTION);
 			speed -= (speed * frictionConstant) * dt;
 		} catch(ArrayIndexOutOfBoundsException ex) {
 			speed *= (1 - FACTOR_MAX_FRICTION) * dt; //max friction if outside of map
			System.out.println("out of bounds");
 		}
         
 		//----------------------------------------------------------------------
         // Steering
 		
         // Counterclockwise, as the unit circle
         if(keyStates.getKeyLeft()){
             direction -= FACTOR_TURN * dt;
         }
         if(keyStates.getKeyRight()){
             direction += FACTOR_TURN * dt;
         }
         
         // Acceleration / deacceleration
         if(keyStates.getKeyUp()){
             speed += FACTOR_ACC * dt;
         }
         if(keyStates.getKeyDown()){
             speed -= FACTOR_BREAK * dt;
         }
 		
 		//----------------------------------------------------------------------
         // Apply the speed in current direction to change position
         posX += Math.cos(direction)*speed * dt;
         posY += Math.sin(direction)*speed * dt;
 	
 		//----------------------------------------------------------------------
 		// Update checkpoint data
 		if(isIntersectingCheckpoint(checkpoints[nextCheckpoint])) {
 			nextCheckpoint++;
 			if(nextCheckpoint == checkpoints.length) {
 				nextCheckpoint = 0;
 			}
 		}
     }
 
 	public int getNextCheckpoint() {
 		return nextCheckpoint;
 	}
     	
 	public boolean isIntersectingCheckpoint(Line2D checkpoint) {
 		return checkpoint.intersects(posX, posY, 20, 20);
 	}
 	
     public void setSpawnData(double direction, double posX, double posY){
         this.direction = direction;
         this.posX = posX;
         this.posY = posY;
     }
 }
