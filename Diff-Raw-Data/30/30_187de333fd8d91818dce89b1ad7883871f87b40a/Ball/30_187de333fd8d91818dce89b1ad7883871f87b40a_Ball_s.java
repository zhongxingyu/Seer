 package uk.co.mrrobinsmith.ballsim.sim;
import java.awt.Color;
 
 import uk.co.mrrobinsmith.ballsim.base.*;
 
 /**
  * Ball represents a physical ball with a graphical representation. The ball
  * itself determines how it moves in response to external forces acting on it.
  * Movement is made by repeated calls to the move() method.
  * 
  * @author Robin Smith
  * @version 2 (04/11/2010)
  * 
  * Based on a simple implementation of a Ball object written by Bruce Quig,
  * Michael Kolling (mik) and David J. Barnes, for the book 
  * 'Objects First with Java - A practical Introduction Using BlueJ'
  * (23-Jan-2002).
  */
 
 public class Ball
 {
     private final Canvas canvas;
     /* vertical platform positions as a function of x */
     private final int[] platformPos;
     
     private double xPos; //initial horizontal position
     private double yPos; //initial vertical position
     private double xVel; //initial horizontal speed
     private double yVel; //initial vertical speed
     private BoundIntParameter diameter;
     private DoubleParameter gravity;
     private DoubleParameter ballHyst;
     private DoubleParameter airRes;
     private DoubleParameter rollRes;
     private DoubleParameter rightWind;
     private DataAnalyser data;
     
     //private int startDiameter;
     private boolean isStoppedX = false; //check for horizontal movement
     private boolean isStoppedY = false; //check for vertical movement
     private boolean isStopped = false; //check for no movement
     private boolean isRolling = false; //check for purely horizontal movement
     private boolean isOffScreen = false;
 
     /**
      * Constructor for BouncingBall object.
      * @param xPos initial horizontal position of the ball.
      * @param yPos initial vertical position of the ball.
      * @param xVel initial horizontal speed of the ball.
      * @param yVel initial vertical speed of the ball.
      * @param diameter IntParameter for the diameter of the ball.
      * @param ballHyst DoubleParameter for the ball's bouncing hysteresis.
      * @param airRes DoubleParameter for external air resistance.
      * @param rollRes DoubleParameter for surface rolling friction.
      * @param rightWind DoubleParameter for the strength of East-blowing wind.
      * @param canvas canvas for this Ball to be drawn on.
      * @param platformPos int array of platform heights in the simulation as a
      * function of x-coordinate in the simulation.
      * @param data DataAnalyser object to send data to.
      */
     public Ball(double xPos, double yPos, double xVel, double yVel,
                BoundIntParameter diameter, DoubleParameter gravity,
                 DoubleParameter ballHyst, DoubleParameter airRes,
                 DoubleParameter rollRes, DoubleParameter rightWind,
                 Canvas canvas, int[] platformPos, DataAnalyser data)
     {
     	this.xPos = xPos;
         this.yPos = yPos;
         this.xVel = xVel;
         this.yVel = yVel;
         this.diameter = diameter;
         //startDiameter = diameter.getValue();
         this.gravity = gravity;
         this.ballHyst = ballHyst;
         this.airRes = airRes;
         this.rollRes = rollRes;
         this.rightWind = rightWind;
         this.canvas = canvas;
         this.platformPos = platformPos;
         this.data = data;
         data.incBallsOnScreen();
     }
 
     /**
      * Gets the Canvas which this Ball is drawn on.
      * @return the Ball's Canvas
      */
     public Canvas getCanvas()
     {
     	return canvas;
     }
     
     /**
      * Checks whether the ball has stopped or not.
      * @return true if the ball has stopped, false otherwise.
      */
     public boolean isStopped()
     {
     	return isStopped;
     }
     
     /**
      * Draws this Ball at its current position onto its Canvas.
      */
    public void draw(Color colour)
     {
     	if (ballInXBounds() && ballInYBounds()) {
    		canvas.setForegroundColor(colour);
         	canvas.fillCircle((int) (xPos), (int) (yPos), diameter.getValue());
         	if (isOffScreen) {
         		isOffScreen = false;
         		data.decBallsOffScreen();
         		data.incBallsOnScreen();
         	}
     	}
     	else if (!isOffScreen){
     		isOffScreen = true;
     		data.incBallsOffScreen();
     		data.decBallsOnScreen();
     	}
     }
 
     /**
      * Erases this ball at its current position.
      */
     private void erase()
     {
     	if (ballInXBounds() && ballInYBounds())
     		canvas.eraseCircle((int) xPos, (int) yPos, diameter.getValue());
     }    
 
     /**
      * Erases this ball at its current position, then moves it according to its
      * new speed determined by the external forces acting on it in the given
      * time interval.
      * @param delta_t the time interval for movement.
      */
     public void move(double delta_t)
     {
     	erase();
     	
     	if (!isStopped) {		
     		if (!isStoppedX)
     			moveHorizontal(delta_t);
     		if (!isStoppedY)
     			moveVertical(delta_t);
     	}
     	if (isStoppedX && isStoppedY && !isStopped)
     		stopBall();
     }
     
     /**
      * Moves the ball horizontally.
      * @param delta_t the time interval for movement.
      */
     private void moveHorizontal(double delta_t)
     {
 		if (getAbsXVel() > 1.0) {
 			double acc = -1 * xVel * airRes.getValue() + rightWind.getValue();
 			xVel += acc * delta_t;
 			if (isRolling) {
 				xVel *= 1 - (rollRes.getValue() * delta_t);
 			}
 		}
 		else if (!isStoppedX) {
 			isStoppedX = true;
 		}
 			
 		if((xPos + xVel * delta_t + diameter.getValue())
 				> (platformPos.length - 1)) {
 			loopBack(delta_t);
 		}
 		else {
 			xPos += xVel * delta_t;
 		}
     }
     
     /**
      * Gets the absolute value of the xVel of the ball.
      * @return the value.
      */
     private double getAbsXVel()
     {
     	if (xVel > 0.0) {
     		return xVel;
     	}
     	else {
     		return xVel * -1.0;
     	}
     }
     
     /**
      * Brings the ball back round to the opposite side of the simulation
      * bounds when it goes off one of the sides.
      * @param delta_t the time interval for movement.
      */
     private void loopBack(double delta_t)
     {
     	double oldXPos = xPos;
         xPos += xVel * delta_t - (platformPos.length - 1)
         					   + diameter.getValue();
         yPos = platformPos[(int) xPos] - (platformPos[(int) oldXPos] - yPos);
     }
     
     /**
      * Moves the ball vertically in the
      * @param delta_t the time interval for movement.
      */
     private void moveVertical(double delta_t)
     {
     	if (((yPos + (yVel + gravity.getValue() * delta_t) * delta_t) 
     			>= platformPos[(int) xPos])) {
 			bounce(delta_t);
     	}
 		else {
 			yVel += gravity.getValue() * delta_t;
 			yPos += yVel * delta_t;
 			if (isRolling)
 				isRolling = false;
 		}
     }
     
     /**
      * Bounces the ball off the platform at its current position.
      * @param delta_t the time interval for movement.
      */
     private void bounce(double delta_t)
     {
     	yVel += gravity.getValue() * delta_t;
     	if (!isStopped) {
     		yPos = platformPos[(int) xPos];
     		yVel = -1.0 * yVel * (1.0 - ballHyst.getValue());
     	}
     	if (yVel > -1.0) {
     		isRolling = true;
         	if (isStoppedX)
         		isStoppedY = true;
     	}
     }
     
     /**
      * Checks whether the ball is within the simulation's horizontal bounds.
      * @return true or false.
      */
     private boolean ballInXBounds()
     {
     	if (xPos >= 0.0 && xPos < platformPos.length)
     		return true;
     	else return false;
     }
     
     /**
      * Checks whether the ball is within the simulation's vertical bounds.
      * @return true or false.
      */
     private boolean ballInYBounds()
     {
     	if (yPos >= 0.0)
     		return true;
     	else return false;
     }
     
     /**
      * Stops the ball and prevents future movement.
      */
     private void stopBall()
     {
     	xVel = 0.0;
     	yVel = 0.0;
     	isStopped = true;
     	data.incStoppedBalls();
     }
     
 }
