 /*
  *  Copyright Mattias Liljeson Sep 14, 2011
  */
 package gameserver;
 
 import common.CarUpdate;
 import common.KeyStates;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 
 /**
  *
  * @author Mattias Liljeson <mattiasliljeson.gmail.com>
  */
 public class Car {
     // Force constants. All units are per second
     public static final double FACTOR_BREAK = 10;
     public static final double FACTOR_ACC = 10;
     public static final double FACTOR_TURN = 10;
     public static final double FACTOR_MAX_FRICTION = 0.3; // maximum friction is applied by multiplying speed with (1 - FACTOR_MAX_FRICTION)
     
     private KeyStates keyStates;
 	private double posX, posY;
     private double direction;
     private double speed;
     private Color color;
     private CarUpdate carUpdate;
     
 	public Car(double posX, double posY, double direction)
 	{
 		this(posX, posY, direction, Color.blue);
 	}
 	
     public Car(double posX, double posY, double direction, Color color){
         this.direction = direction;
 		this.posX = posX;
 		this.posY = posY;
         this.color = color;
 		
         keyStates = new KeyStates();
         carUpdate = new CarUpdate(posX, posY, direction, color);
     }
     
     public CarUpdate getCarUpdate() {
 		carUpdate.posX = posX;
 		carUpdate.posY = posY;
         carUpdate.rotation = direction;
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
     
     public void update(double dt, BufferedImage frictionMaskBuffImg){
 		//----------------------------------------------------------------------
 		// Friction
 		
 		/*
          * - Sample friction from friction mask in the raceCourse
 		 * frictionConstant is calculated in this interval:
          *   frictionColor = 0 -> frictionConstant = 1
 		 *   frictionColor = 255 -> frictionConstant = 1 - FACTOR_MAX_FRICTION
 		 * then multiplied with speed taking delta time into account
 		 */
		Color frictionColor = new Color(frictionMaskBuffImg.getRGB((int)posX, (int)posY));
        double frictionConstant = 1 - (frictionColor.getBlue()/255.0*FACTOR_MAX_FRICTION);
        speed *= frictionConstant * dt;
         
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
 		posY += 50 * dt;
     }
 }
