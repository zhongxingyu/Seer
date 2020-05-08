 package org.reprap.machines;
 
 import java.io.IOException;
 import javax.media.j3d.*;
 import org.reprap.CartesianPrinter;
 import org.reprap.Preferences;
 import org.reprap.ReprapException;
 import org.reprap.comms.Communicator;
 import org.reprap.comms.snap.SNAPAddress;
 import org.reprap.comms.snap.SNAPCommunicator;
 import org.reprap.devices.GenericExtruder;
 import org.reprap.devices.GenericStepperMotor;
 import org.reprap.devices.pseudo.LinePrinter;
 import org.reprap.gui.CalibrateZAxis;
 import org.reprap.gui.Previewer;
 
 /**
  * 
  * A Reprap printer is a 3-D cartesian printer with one or more
  * extruders
  *
  */
 public class Reprap implements CartesianPrinter {
 	private final int localNodeNumber = 0;
 	private final int baudRate = 19200;
 
 	private Communicator communicator;
 	private Previewer previewer = null;
 
 	private GenericStepperMotor motorX;
 	private GenericStepperMotor motorY;
 	private GenericStepperMotor motorZ;
 	
 	double totalDistanceMoved = 0.0;
 	double totalDistanceExtruded = 0.0;
 
 	private LinePrinter layer;
 	
 	double scaleX, scaleY, scaleZ;
 	
 	double currentX, currentY, currentZ;
 	
 	double offsetX, offsetY, offsetZ;
 	
 	
 	private int speedXY = 230;  			// Initial default speed
 	private int fastSpeedXY = 230;
 	private int speedZ = 230;  			// Initial default speed
 	private int speedExtruder = 200;    // Initial default extruder speed
 	
 	private double extrusionSize;
 	private double extrusionHeight;
 	private double infillWidth;
 	
 	private double overRun;
 	private long delay;
 	
 	private GenericExtruder extruder;  ///< Only one supported for now
 
 	private boolean excludeZ = false;  ///< Don't perform Z operations.  Should be removed later.
 	
 	private long startTime;
 	
 	private boolean idleZ;
 	
 	public Reprap(Preferences prefs) throws Exception {
 		startTime = System.currentTimeMillis();
 		
 		int axes = prefs.loadInt("AxisCount");
 		if (axes != 3)
 			throw new Exception("A Reprap printer must contain 3 axes");
 		int extruders = prefs.loadInt("ExtruderCount");
 		if (extruders < 1)
 			throw new Exception("A Reprap printer must contain at least one extruder");
 		
 		offsetX = offsetY = offsetZ = 0.0;
 		
 		String commPortName = prefs.loadString("Port");
 		
 		SNAPAddress myAddress = new SNAPAddress(localNodeNumber); 
 		communicator = new SNAPCommunicator(commPortName, baudRate, myAddress);
 		
 		motorX = new GenericStepperMotor(communicator,
 				new SNAPAddress(prefs.loadInt("Axis1Address")), prefs, 1);
 		motorY = new GenericStepperMotor(communicator,
 				new SNAPAddress(prefs.loadInt("Axis2Address")), prefs, 2);
 		motorZ = new GenericStepperMotor(communicator,
 				new SNAPAddress(prefs.loadInt("Axis3Address")), prefs, 3);
 		
 		extruder = new GenericExtruder(communicator,
 				new SNAPAddress(prefs.loadInt("Extruder1Address")), prefs, 1);
 
 		extrusionSize = prefs.loadDouble("ExtrusionSize");
 		extrusionHeight = prefs.loadDouble("ExtrusionHeight");
 		infillWidth = prefs.loadDouble("ExtrusionInfillWidth");
 		
 		overRun = prefs.loadDouble("ExtrusionOverRun");
 		delay = prefs.loadInt("ExtrusionDelay");
 		
 		layer = new LinePrinter(motorX, motorY, extruder);
 
 		// TODO This should be from calibration
 		scaleX = prefs.loadDouble("Axis1Scale");
 		scaleY = prefs.loadDouble("Axis2Scale");
 		scaleZ = prefs.loadDouble("Axis3Scale");
 
 		offsetX = prefs.loadDouble("Extruder1OffsetX");
 		offsetY = prefs.loadDouble("Extruder1OffsetY");
 		offsetZ = prefs.loadDouble("Extruder1OffsetZ");
 	
 		idleZ = prefs.loadBool("IdleZAxis");
 		
 		try {
 			currentX = convertToPositionZ(motorX.getPosition());
 			currentY = convertToPositionZ(motorY.getPosition());
 		} catch (Exception ex) {
 			throw new Exception("Warning: X and/or Y controller not responding, cannot continue");
 		}
 		try {
 			currentZ = convertToPositionZ(motorZ.getPosition());
 		} catch (Exception ex) {
 			System.out.println("Z axis not responding and will be ignored");
 			excludeZ = true;
 		}
 		
 	}
 	
 	public void calibrate() {
 	}
 
 //	public void printSegment(double startX, double startY, double startZ, double endX, double endY, double endZ) throws ReprapException, IOException {
 //		moveTo(startX, startY, startZ);
 //		printTo(endX, endY, endZ);
 //	}
 
 	public void moveTo(double x, double y, double z, boolean startUp, boolean endUp) throws ReprapException, IOException {
 		
 		if (isCancelled()) return;
 		
 		int stepperX = convertToStepX(x);
 		int stepperY = convertToStepY(y);
 		int stepperZ = convertToStepZ(z);
 		int currentStepperX = convertToStepX(currentX);
 		int currentStepperY = convertToStepY(currentY);
 		int currentStepperZ = convertToStepZ(currentZ);		
 		
 		if (currentStepperX == stepperX && 
 				currentStepperY ==stepperY && 
 				currentStepperZ == stepperZ && 
 				!startUp)
 			return;
 
 		double liftedZ = z + extrusionHeight;
 		int stepperLiftedZ = convertToStepZ(liftedZ);
 		int targetZ;
 		
 		// Raise head slightly before move?
 		if(startUp)
 		{
 			targetZ = stepperLiftedZ;
 			currentZ = liftedZ;
 		} else
 		{
 			targetZ = stepperZ;
 			currentZ = z;
 		}
 		
 		if (targetZ != currentStepperZ) {
 			totalDistanceMoved += Math.abs(currentZ - liftedZ);
 			if (!excludeZ) motorZ.seekBlocking(speedZ, targetZ);
 			if (idleZ) motorZ.setIdle();
 			currentStepperZ = targetZ;
 		}
 		
 		layer.moveTo(stepperX, stepperY, speedXY);
 		totalDistanceMoved += segmentLength(x - currentX, y - currentY);
 		currentX = x;
 		currentY = y;
 		
 		if(endUp)
 		{
 			targetZ = stepperLiftedZ;
 			currentZ = liftedZ;
 		} else
 		{
 			targetZ = stepperZ;
 			currentZ = z;
 		}
 		
 		// Move head back down to surface?
 		if(targetZ != currentStepperZ)
 		{
 			totalDistanceMoved += Math.abs(currentZ - z);
 			if (!excludeZ) motorZ.seekBlocking(speedZ, targetZ);
 			if (idleZ) motorZ.setIdle();
 			currentStepperZ = targetZ;
 		} 
 	}
 	
 	public void printTo(double x, double y, double z) throws ReprapException, IOException {
 		if (isCancelled()) return;
 		EnsureNotEmpty();
 		if (isCancelled()) return;
 		EnsureHot();
 		if (isCancelled()) return;
 
 		int stepperX = convertToStepX(x);
 		int stepperY = convertToStepY(y);
 		int stepperZ = convertToStepZ(z);
 		
 		if ((stepperX != layer.getCurrentX() || stepperY != layer.getCurrentY()) && z != currentZ)
 			throw new ReprapException("Reprap cannot print a line across 3 axes simultaneously");
 
 		if (previewer != null)
 			previewer.addSegment(convertToPositionX(layer.getCurrentX()),
 					convertToPositionY(layer.getCurrentY()), currentZ,
 					x, y, z);
 
 		if (isCancelled()) return;
 		
 		
 		if (z != currentZ) 
 		{
 			// Print a simple vertical extrusion
 			double distance = Math.abs(currentZ - z);
 			totalDistanceExtruded += distance;
 			totalDistanceMoved += distance;
 			extruder.setExtrusion(speedExtruder);
 			if (!excludeZ) motorZ.seekBlocking(speedZ, stepperZ);
 			extruder.setExtrusion(0);
 			currentZ = z;
 			return;
 		}
 
 		// Otherwise printing only in X/Y plane
 		double deltaX = x - currentX;
 		double deltaY = y - currentY;
 		double distance = segmentLength(deltaX, deltaY);
 		totalDistanceExtruded += distance;
 		totalDistanceMoved += distance;
 		layer.printTo(stepperX, stepperY, speedXY, speedExtruder);
 		currentX = x;
 		currentY = y;
 	}
 
 	// Move to zero stop on X axis.
 	public void homeToZeroX() throws ReprapException, IOException {
 		motorX.homeReset(getSpeed());
 		currentX=0;
 	}
 	
 	// Move to zero stop on Y axis.
 	public void homeToZeroY() throws ReprapException, IOException {
 		motorY.homeReset(getSpeed());
 		currentY=0;
 	}
 
 	public void selectMaterial(int materialIndex) {
 		if (isCancelled()) return;
 
 		if (previewer != null)
 			previewer.setMaterial(materialIndex, extrusionSize, extrusionHeight);
 
 		if (isCancelled()) return;
 		// TODO Select new material
 		// TODO Load new x/y/z offsets for the new extruder
 	}
 
 	// Why don't these use round()? - AB.
 	protected int convertToStepX(double n) {
 		return (int)((n + offsetX) * scaleX);
 	}
 
 	protected int convertToStepY(double n) {
 		return (int)((n + offsetY) * scaleY);
 	}
 
 	protected int convertToStepZ(double n) {
 		return (int)((n + offsetZ) * scaleZ);
 	}
 
 	protected double convertToPositionX(int n) {
 		return n / scaleX - offsetX;
 	}
 
 	protected double convertToPositionY(int n) {
 		return n / scaleY - offsetY;
 	}
 
 	protected double convertToPositionZ(int n) {
 		return n / scaleZ - offsetZ;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.reprap.Printer#terminate()
 	 */
 	public void terminate() throws Exception {
 		motorX.setIdle();
 		motorY.setIdle();
 		if (!excludeZ) motorZ.setIdle();
 		extruder.setExtrusion(0);
 		extruder.setTemperature(0);
 	}
 	
 	public void dispose() {
 		motorX.dispose();
 		motorY.dispose();
 		motorZ.dispose();
 		extruder.dispose();
 		communicator.close();
 		communicator.dispose();
 	}
 
 	/**
 	 * @return Returns the speed for the X & Y axes.
 	 */
 	public int getSpeed() {
 		return speedXY;
 	}
 	/**
 	 * @return Returns the maximum speed for the X & Y axes in air movement.
 	 */
 	public int getFastSpeed() {
 		return fastSpeedXY;
 	}	
 	
 	/**
 	 * @param speed The speed to set for the X and Y axes.
 	 */
 	public void setSpeed(int speed) {
 		this.speedXY = speed;
 	}
 	
 	/**
 	 * @param speed The speed to set for the X and Y axes moving in air.
 	 */
 	public void setFastSpeed(int speed) {
 		this.fastSpeedXY = speed;
 	}
 
 	/**
 	 * @return Returns the speed for the Z axis.
 	 */
 	public int getSpeedZ() {
 		return speedZ;
 	}
 	/**
 	 * @param speed The speed to set for the Z axis.
 	 */
 	public void setSpeedZ(int speed) {
 		this.speedZ = speed;
 	}
 
 	/**
 	 * @return Returns the speedExtruder.
 	 */
 	public int getExtruderSpeed() {
 		return speedExtruder;
 	}
 	/**
 	 * @param speedExtruder The speedExtruder to set.
 	 */
 	public void setExtruderSpeed(int speedExtruder) {
 		this.speedExtruder = speedExtruder;
 	}
 	
 	public void setPreviewer(Previewer previewer) {
 		this.previewer = previewer;
 	}
 
 	public void setTemperature(int temperature) throws Exception {
 		extruder.setTemperature(temperature);
 	}
 	
 	/**
 	 * outline speed and the infill speed
 	 */
 	public double getOutlineSpeed()
 	{
 		return extruder.getOutlineSpeed();
 	}
 	public double getInfillSpeed()
 	{
 		return extruder.getInfillSpeed();
 	}
 	
 	/**
 	 * The length in mm to speed up when going round corners
 	 */
 	public double getAngleSpeedUpLength()
 	{
 		return extruder.getAngleSpeedUpLength();
 	}
 	
 	/**
 	 * The factor by which to speed up when going round a corner.
 	 * The formula is speed = baseSpeed*[1 + 0.5*(1 - ca)*getAngleSpeedFactor()]
 	 * where ca is the cos of the angle between the lines.  So it goes fastest when
 	 * the line doubles back on itself, and slowest when it continues straight.
 	 */	
 	public double getAngleSpeedFactor()
 	{
 		return extruder.getAngleSpeedFactor();
 	}
 
 	private void EnsureNotEmpty() {
 		if (!extruder.isEmpty()) return;
 		
 		while (extruder.isEmpty() && !isCancelled()) {
 			if (previewer != null)
 				previewer.setMessage("Extruder is out of feedstock.  Waiting for refill.");
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 			}
 		}
 		if (previewer != null) previewer.setMessage(null);
 	}
 	
 	private void EnsureHot() throws ReprapException, IOException {
 		double threshold = extruder.getTemperatureTarget() * 0.65;	// Changed from 0.95 by Vik.
 		
 		if (extruder.getTemperature() >= threshold)
 			return;
 
 		double x = currentX;
 		double y = currentY;
 		int tempReminder=0;
 		temperatureReminder();
 		System.out.println("Moving to heating zone");
 		moveToHeatingZone();
 		while(extruder.getTemperature() < threshold && !isCancelled()) {
 			if (previewer != null) previewer.setMessage("Waiting for extruder to reach working temperature (" + Math.round(extruder.getTemperature()) + ")");
 			try {
 				Thread.sleep(1000);
 				// If it stays cold for 10s, remind it of its purpose.
 				if (tempReminder++ >10) {
 					tempReminder=0;
 					temperatureReminder();
 				}
 			} catch (InterruptedException e) {
 			}
 		}
 		System.out.println("Returning to previous position");
 		moveTo(x, y, currentZ, true, false);
 		if (previewer != null) previewer.setMessage(null);
 		
 	}
 
 	/** A bodge to fix the extruder's current tendency to forget what temperature
 	 * it is supposed to be reaching.
 	 * 
 	 * Vik
 	 */
 	private void temperatureReminder() {
 		System.out.println("Reminding it of the temperature");
 		try {
 			setTemperature(Preferences.loadGlobalInt("ExtrusionTemp"));
 		} catch (Exception e) {
 			System.out.println("Error resetting temperature.");
 		}
 	}
 	
 	/**
 	 * Moves the head to the predefined heating area
 	 * @throws IOException
 	 * @throws ReprapException
 	 */
 	private void moveToHeatingZone() throws ReprapException, IOException {
 		moveTo(5, 5, currentZ, true, false);
 	}
 
 	public boolean isCancelled() {
 		if (previewer == null)
 			return false;
 		return previewer.isCancelled();
 	}
 	
 	public void initialise() throws Exception {
 		if (previewer != null)
 			previewer.reset();
 		motorX.homeReset(speedXY);
 		motorY.homeReset(speedXY);
 		if (!excludeZ) motorZ.homeReset(speedZ);
 		currentX = currentY = currentZ = 0.0;
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
 	
 	public double getExtrusionSize() {
 		return extrusionSize;
 	}
 
 	public double getExtrusionHeight() {
 		return extrusionHeight;
 	}
 	
 	public double getInfillWidth() {
 		return infillWidth;
 	}
 	
 	public void setCooling(boolean enable) throws IOException {
 		extruder.setCooler(enable);
 	}
 	
 	/**
 	 * Get the length before the end of a track to turn the extruder off
 	 * to allow for the delay in the stream stopping.
 	 * @return
 	 */
 	public double getOverRun() { return overRun; }
 	
 	/**
 	 * Get the number of milliseconds to wait between turning an 
 	 * extruder on and starting to move it.
 	 * @return
 	 */
 	public long getDelay() { return delay; }
 	
 	public double getTotalElapsedTime() {
 		long now = System.currentTimeMillis();
 		return (now - startTime) / 1000.0;
 	}
 
 	/**
 	 * Extrude for the given time in milliseconds, so that polymer is flowing
 	 * before we try to move the extruder.
 	 */
 	public void printStartDelay(long msDelay) {
 		try
 		{
 			extruder.setExtrusion(speedExtruder);
 			Thread.sleep(msDelay);
 			extruder.setExtrusion(0);
 		} catch(Exception e)
 		{
 			// If anything goes wrong, we'll let someone else catch it.
 		}
 	}
 	
 	public void setLowerShell(Shape3D ls)
 	{
 		previewer.setLowerShell(ls);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.reprap.Printer#setZManual()
 	 */
 	public void setZManual() throws IOException {
 		setZManual(0.0);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.reprap.Printer#setZManual(double)
 	 */
 	public void setZManual(double zeroPoint) throws IOException {
 		
 		CalibrateZAxis msg =
 			new CalibrateZAxis(null, motorZ, scaleZ, speedZ);
 		msg.setVisible(true);
 		try {
 			synchronized(msg) {
 				msg.wait();
 			}
 		} catch (Exception ex) {
 		}
 		msg.dispose();
 		
 		motorZ.setPosition(convertToStepZ(zeroPoint));
 	}
 }
 
 
