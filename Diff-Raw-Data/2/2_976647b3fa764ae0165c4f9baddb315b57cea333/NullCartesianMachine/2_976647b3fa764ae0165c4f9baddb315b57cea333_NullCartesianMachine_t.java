 package org.reprap.machines;
 
 import java.io.IOException;
 import javax.media.j3d.*;
 import org.reprap.CartesianPrinter;
 import org.reprap.Preferences;
 import org.reprap.Extruder;
 import org.reprap.ReprapException;
 import org.reprap.gui.Previewer;
 import org.reprap.devices.NullExtruder;
 
 public class NullCartesianMachine implements CartesianPrinter {
 	
 	private Previewer previewer = null;
 
 	double totalDistanceMoved = 0.0;
 	double totalDistanceExtruded = 0.0;
 	
 	//double extrusionSize, extrusionHeight, infillWidth;
 	
 	double currentX, currentY, currentZ;
 	
 	private double overRun;
 	private long delay;
 
 	private long startTime;
 	
 	private Extruder extruders[];
 	private int extruder;
 	private int extruderCount;
 
 	public NullCartesianMachine(Preferences config) {
 		startTime = System.currentTimeMillis();
 		
		extruderCount = config.loadInt("NumberOfExtruders");
 		extruders = new NullExtruder[extruderCount];
 		for(int i = 0; i < extruderCount; i++)
 		{
 			String prefix = "Extruder" + i;
 			extruders[i] = new NullExtruder(config, i);
 		}
 		extruder = 0;
 
 		currentX = 0;
 		currentY = 0;
 		currentZ = 0;
 		
 
 	}
 	
 	public void calibrate() {
 	}
 
 	public void printSegment(double startX, double startY, double startZ, double endX, double endY, double endZ) throws ReprapException, IOException {
 		moveTo(startX, startY, startZ, true, true);
 		printTo(endX, endY, endZ);
 	}
 
 	public void moveTo(double x, double y, double z, boolean startUp, boolean endUp) throws ReprapException, IOException {
 		if (isCancelled()) return;
 
 		totalDistanceMoved += segmentLength(x - currentX, y - currentY);
 		//TODO - next bit needs to take account of startUp and endUp
 		if (z != currentZ)
 			totalDistanceMoved += Math.abs(currentZ - z);
 
 		currentX = x;
 		currentY = y;
 		currentZ = z;
 	}
 
 
 	public void printTo(double x, double y, double z) throws ReprapException, IOException {
 		if (previewer != null)
 			previewer.addSegment(currentX, currentY, currentZ, x, y, z);
 		if (isCancelled()) return;
 
 		double distance = segmentLength(x - currentX, y - currentY);
 		if (z != currentZ)
 			distance += Math.abs(currentZ - z);
 		totalDistanceExtruded += distance;
 		totalDistanceMoved += distance;
 		
 		currentX = x;
 		currentY = y;
 		currentZ = z;
 	}
 
 	public void selectMaterial(int materialIndex) {
 		if (isCancelled()) return;
 		if (previewer != null)
 			previewer.setMaterial(materialIndex, extruders[extruder].getExtrusionSize(), 
 					extruders[extruder].getExtrusionHeight());
 	}
 
 	public void terminate() throws IOException {
 	}
 
 	public int getSpeed() {
 		return 200;
 	}
 	
 	public int getFastSpeed() {
 		return getSpeed();
 	}
 	
 	public double getAngleSpeedUpLength()
 	{
 		return 1;
 	}
 	
 	public double getAngleSpeedFactor()
 	{
 		return 0;
 	}
 	
 	public void setSpeed(int speed) {
 	}
 	
 	public void setFastSpeed(int speed) {
 	}
 
 	public int getSpeedZ() {
 		return 200;
 	}
 
 	public void setSpeedZ(int speed) {
 	}
 
 	public int getExtruderSpeed() {
 		return 200;
 	}
 
 	public void setExtruderSpeed(int speed) {
 	}
 
 	public void setPreviewer(Previewer previewer) {
 		this.previewer = previewer;
 	}
 
 	public void setTemperature(int temperature) {
 	}
 	
 	/**
 	 * outline speed and the infill speed
 	 */
 	public double getOutlineSpeed()
 	{
 		return 1.0;
 	}
 	public double getInfillSpeed()
 	{
 		return 1.0;
 	}
 	public void dispose() {
 	}
 
 	public boolean isCancelled() {
 		if (previewer != null)
 			return previewer.isCancelled();
 		return false;
 	}
 
 	public void initialise() {
 		if (previewer != null)
 			previewer.reset();
 	}
 
 	public double getX() {
 		return currentX;
 	}
 
 	public double getY() {
 		return currentY;
 	}
 
 	public double getZ() {
 		return currentZ;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.reprap.Printer#getTotalDistanceMoved()
 	 */
 	public double getTotalDistanceMoved() {
 		return totalDistanceMoved;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.reprap.Printer#getTotalDistanceExtruded()
 	 */
 	public double getTotalDistanceExtruded() {
 		return totalDistanceExtruded;
 	}
 
 	public double segmentLength(double x, double y) {
 		return Math.sqrt(x*x + y*y);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.reprap.Printer#getExtrusionSize()
 	 */
 //	public double getExtrusionSize() {
 //		return extrusionSize;
 //	}
 //
 //	public double getExtrusionHeight() {
 //		return extrusionHeight;
 //	}
 //	
 //	public double getInfillWidth() {
 //		return infillWidth;
 //	}
 
 	/* (non-Javadoc)
 	 * @see org.reprap.Printer#setCooling(boolean)
 	 */
 	public void setCooling(boolean enable) {
 	}
 	
 	/**
 	 * Get the length before the end of a track to turn the extruder off
 	 * to allow for the delay in the stream stopping.
 	 * @return
 	 */
 	public double getOverRun() { return overRun; };
 	
 	/**
 	 * Get the number of milliseconds to wait between turning an 
 	 * extruder on and starting to move it.
 	 * @return
 	 */
 	public long getDelay() { return delay; };
 
 	public double getTotalElapsedTime() {
 		long now = System.currentTimeMillis();
 		return (now - startTime) / 1000.0;
 	}
 
 	public void printStartDelay(long msDelay) {
 		// This would extrude for the given interval to ensure polymer flow.
 	}
 	
 	public void setLowerShell(Shape3D ls)
 	{
 		previewer.setLowerShell(ls);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.reprap.Printer#setZManual()
 	 */
 	public void setZManual() {
 		setZManual(0.0);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.reprap.Printer#setZManual(double)
 	 */
 	public void setZManual(double zeroPoint) {
 	}
 
 	public void homeToZeroX() throws ReprapException, IOException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void homeToZeroY() throws ReprapException, IOException {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public Extruder getExtruder()
 	{
 		return extruders[extruder];
 	}
 }
