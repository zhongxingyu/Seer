 import java.awt.geom.*;
 import java.io.*;
 
 /** represents the movement of an object that starts at a specified position and rotation,
     moves at constant velocity to a specified end position and rotation, then stops */
 @SuppressWarnings("serial")
 public class Movement implements Serializable {
 	/** position at beginning of this move */
 	private Point2D.Double startPos;
 	/** counterclockwise rotation in radians at beginning of this move */
 	private double startRot;
 	/** time that this move starts, in milliseconds after the simulation started */
 	private long startTime;
 	/** position at end of this move */
 	private Point2D.Double endPos;
 	/** counterclockwise rotation in radians at end of this move */
 	private double endRot;
 	/** time that this move ends, in milliseconds after the simulation started */
 	private long endTime;
 	
 	private boolean paused = false;
 	private long pauseStartTime = 0;
 	
 	/** Basic constructor. For parts that have an initial position, but aren't moving yet. */
 	public Movement(Point2D.Double currentPos, double rotation)
 	{
 		startPos = currentPos;
 		startRot = rotation;
 		startTime = -1;
 		endPos = currentPos;
 		endRot = rotation;
 		endTime = 0;
 	}
 
 	/** constructor that sets all instance variables to specified values */
 	public Movement(Point2D.Double newStartPos, double newStartRot, long newStartTime,
 			Point2D.Double newEndPos, double newEndRot, long newEndTime) {
 		if (newEndTime <= newStartTime) {
 			throw new IllegalArgumentException("end time (" + newEndTime + ") must be later than start time (" + newStartTime + ")");
 		}
 		startPos = newStartPos;
 		startRot = newStartRot;
 		startTime = newStartTime;
 		endPos = newEndPos;
 		endRot = newEndRot;
 		endTime = newEndTime;
 	}
 
 	/** returns position at specified time */
 	public Point2D.Double calcPos(long time) {
 		if (paused)
 			time = pauseStartTime;
 			
 		if (time <= startTime) {
 			return startPos;
 		}
 		if (time >= endTime) {
 			return endPos;
 		}
 		return new Point2D.Double(startPos.getX() + (endPos.getX() - startPos.getX()) / (endTime - startTime) * (time - startTime),
 				startPos.getY() + (endPos.getY() - startPos.getY()) / (endTime - startTime) * (time - startTime));
 	}
 
 	/** returns rotation at specified time */
 	public double calcRot(long time) {
 		if (paused)
 			time = pauseStartTime;
 		
 		if (time <= startTime) {
 			return startRot;
 		}
 		if (time >= endTime) {
 			return endRot;
 		}
 		return startRot + (endRot - startRot) / (endTime - startTime) * (time - startTime);
 	}
 
 	/** returns whether specified time is past end time */
 	public boolean arrived(long time) {
 		if (paused)
 			time = pauseStartTime;
 		return (time >= endTime);
 	}
 	
 	public Point2D.Double getStartPos()
 	{
 		return startPos;
 	}
 
 	/** getter for endPos */
 	public Point2D.Double getEndPos() {
 		return endPos;
 	}
 
 	/** getter for endRot */
 	public double getEndRot() {
 		return endRot;
 	}
 
 	/** getter for endTime */
 	public long getEndTime() {
 		return endTime;
 	}
 	
 	public void pause(long currentTime)
 	{
 		// Check if it's already paused
 		if (pauseStartTime > 0)
 			return;
 		
 		paused = true;
 		pauseStartTime = currentTime;
 	}
 	
 	public void unPause(long currentTime)
 	{
 		// Check if it has been paused first
 		if (pauseStartTime == 0)
 			return;
 		
 		paused = false;
 		startTime += currentTime - pauseStartTime;
 		endTime += currentTime - pauseStartTime;
 		pauseStartTime = 0;
 	}
 	
 	/**
 	 * Used to make one object (slave) match the position of another (master) with an offset
 	 * @param master
 	 * @param xOffset
 	 * @param yOffset
 	 * @param currentTime
 	 */
 	public void slaveTranslation(Movement master, double xOffset, double yOffset, long currentTime)
 	{
 		this.startPos = new Point2D.Double(master.calcPos(currentTime).x + xOffset, master.calcPos(currentTime).y + yOffset);
 		this.endPos = new Point2D.Double(master.calcPos(currentTime).x + xOffset, master.calcPos(currentTime).y + yOffset);
 	}
 	
 	public void slaveRotation(Movement master, double angleOffset, long currentTime)
 	{
		this.startRot = master.calcRot(currentTime);
		this.endRot = master.calcRot(currentTime);
 	}
 
 	/** alternate method to create Movement object that asks for speed (in position units per second) instead of end time */
 	static Movement fromSpeed(Point2D.Double newStartPos, double newStartRot, long newStartTime,
			Point2D.Double newEndPos, double newEndRot, double speed) {
		return new Movement(newStartPos, newStartRot, newStartTime, newEndPos, newEndRot,
				(long)(newStartTime + (Math.sqrt(Math.pow(newEndPos.x - newStartPos.x, 2) + Math.pow(newEndPos.y - newStartPos.y, 2)) / (speed/1000.0))));
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
