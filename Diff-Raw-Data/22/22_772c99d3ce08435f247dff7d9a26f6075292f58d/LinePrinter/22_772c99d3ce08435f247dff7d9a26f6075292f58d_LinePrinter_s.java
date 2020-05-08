 package org.reprap.devices.pseudo;
 
 import java.io.IOException;
 
 import org.reprap.Extruder;
 import org.reprap.devices.GenericStepperMotor;
 
 /**
  * This is a pseudo device that provides an apparent single device
  * for plotting lines.
  */
 public class LinePrinter {
 
 	
 	/**
 	 * Stepper motors
 	 */
 	private GenericStepperMotor motorX;
 	private GenericStepperMotor motorY;
 	private Extruder extruder;
 
 	/**
 	 * 
 	 */
 	private boolean initialisedXY = false;
 	
 	/**
 	 * 
 	 */
 	private int currentX, currentY;
 	
 	/**
 	 * @param motorX
 	 * @param motorY
 	 * @param extruder
 	 */
 	public LinePrinter(GenericStepperMotor motorX, GenericStepperMotor motorY, Extruder extruder) {
 		this.motorX = motorX;
 		this.motorY = motorY;
 		this.extruder = extruder;
 	}
 	
 	public void changeExtruder(Extruder e)
 	{
 		extruder = e;
 	}
 	
 	/**
 	 * @throws IOException
 	 */
 	public void initialiseXY() throws IOException {
 		if (!initialisedXY) {
 			currentX = motorX.getPosition();
 			currentY = motorY.getPosition();
 			initialisedXY = true;
 		}
 	}
 
 	/**
 	 * Move to a 2-space point in a direct line.  At the moment this is just the pure 2D Bresenham algorithm.
 	 * It would be good to generalise this to a 3D DDA.
 	 * @param endX
 	 * @param endY
 	 * @param movementSpeed
 	 * @throws IOException
 	 */
 	public void moveTo(int endX, int endY, int movementSpeed) throws IOException {
 		initialiseXY();
 
 		if (currentX == endX && currentY == endY)
 			return;
 		
 		GenericStepperMotor master, slave;
 
 		int x0, x1, y0, y1;
 		
 		// Whichever is the greater distance will be the master
 		// From an algorithmic point of view, we'll just consider
 		// the master to be X and the slave to be Y, which eliminates
 		// the need for mapping quadrants.
 		if (Math.abs(endX - currentX) > Math.abs(endY - currentY)) {
 			master = motorX;
 			slave = motorY;
 			x0 = currentX;
 			x1 = endX;
 			y0 = currentY;
 			y1 = endY;
 		} else {
 			master = motorY;
 			slave = motorX;
 			x0 = currentY;
 			x1 = endY;
 			y0 = currentX;
 			y1 = endX;
 		}
 				
 		master.setSync(GenericStepperMotor.SYNC_NONE);
 		if (y0 < y1)
 			slave.setSync(GenericStepperMotor.SYNC_INC);
 		else
 			slave.setSync(GenericStepperMotor.SYNC_DEC);
 
 		int deltaY = Math.abs(y1 - y0); 
 		//int deltaX = Math.abs(x1 - x0); 
 				
 		master.dda(movementSpeed, x1, deltaY);
 		
 		slave.setSync(GenericStepperMotor.SYNC_NONE);
 
 		currentX = endX;
 		currentY = endY;
 	}
 	
 	/**
 	 * Correct a speed change (in (0, 1]) for the fact that it's click times that get
 	 * send to the controller.
 	 * @param oldSpeed
 	 * @param factor
 	 * @return
 	 */
 	
 	public static int speedFix(int oldSpeed, double factor)
 	{
 		if(factor <= 0 || factor > 1)
 			return oldSpeed;
 		
 		double x = 256 + (oldSpeed - 256)/factor;
 		int speed = (int)Math.round(x);
 		if(speed < 1)
 			speed = 1;
 		if(speed > 255)
 			speed = 255;
 		return speed;		
 	}
 	
 
 	/**
 	 * Correct the speed for the angle of the line to the axes
 	 * @param movementSpeed
 	 * @param dx
 	 * @param dy
 	 * @return
 	 */
 	private int angleSpeed(int movementSpeed, double dx, double dy)	{
 		double length = Math.sqrt(dx*dx + dy*dy);
 		if(length == 0)
 			return movementSpeed;
 		double longSide = Math.max(Math.abs(dx), Math.abs(dy));
 		return speedFix(movementSpeed, longSide/length);
 	}
 
 	/**
 	 * @param endX
 	 * @param endY
 	 * @param movementSpeed
 	 * @param extruderSpeed
 	 * @param turnOff True if extruder should be turned off after this segment is printed.
 	 * @throws IOException
 	 */
 	public void printTo(int endX, int endY, int movementSpeed, 
 			int extruderSpeed, boolean turnOff) throws IOException {
 		// Determine the extruder speed, based on the geometry of the line
 		// to be printed
 		double dx = endX - currentX;
 		double dy = endY - currentY;
 		extruder.setExtrusion(extruderSpeed);
 		moveTo(endX, endY, angleSpeed(movementSpeed, dx, dy));
 		if(turnOff)
 			extruder.setExtrusion(0);
 	}
 	
 	public void stopExtruding() throws IOException
 	{
 		extruder.setExtrusion(0);
 	}
 	
 	/**
 	 * @param startX
 	 * @param startY
 	 * @param endX
 	 * @param endY
 	 * @param movementSpeed
 	 * @param extruderSpeed
 	 * @param turnOff True if the extruder should be turned off at the end of this segment.
 	 * @throws IOException
 	 */
 	public void printLine(int startX, int startY, int endX, int endY, 
 			int movementSpeed, int extruderSpeed, boolean turnOff) throws IOException {
 		moveTo(startX, startY, movementSpeed);
 		printTo(endX, endY, movementSpeed, extruderSpeed, turnOff);
 	}
 
 	/**
 	 * @return Returns the currentX.
 	 */
 	public int getCurrentX() {
 		return currentX;
 	}
 	/**
 	 * @return Returns the currentY.
 	 */
 	public int getCurrentY() {
 		return currentY;
 	}
 }
