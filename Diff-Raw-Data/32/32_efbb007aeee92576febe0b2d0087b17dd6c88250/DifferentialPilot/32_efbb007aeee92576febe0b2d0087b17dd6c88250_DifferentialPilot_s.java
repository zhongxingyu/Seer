 package onRobot;
 import lejos.nxt.Motor;
 import lejos.nxt.NXTRegulatedMotor;
 import lejos.robotics.RegulatedMotor;
 import lejos.robotics.navigation.Move.MoveType;
 
 /*
  /**
  * This is our own version of differentialPilot which should work faster
  */
 public class DifferentialPilot {
 	/**
 	 * The speed of left and right motor when robot needs to travel straight
 	 */
 	int[] tMotorSpeed = new int[3];
 
 	private final int[] rSpeed = new int[3];
 
 	private Position position = new Position(260, 180);
 	private double rotation = 0;
 	private long[] prevTachoCount = { 0, 0, 0 };
 	final int[] zeros = { 0, 0, 0 };
 	private final RegulatedMotor[] motors = new RegulatedMotor[3];
 	private MoveType previousType;
 	private double defaultTravelSpeed=10;
 	private double slowTravelSpeed=5;
 	private double fastTravelSpeed=20;
 	private double defaultRotateSpeed=100;
 
	private boolean poseIsLocked;
 	
 	/**
 	 * Allocates a DifferentialPilot object, and sets the physical parameters of
 	 * the NXT robot.<br>
 	 * Assumes Motor.forward() causes the robot to move forward.
 	 * 
 	 * @param wheelDiameter
 	 *            Diameter of the tire, in any convenient units (diameter in mm
 	 *            is usually printed on the tire).
 	 * @param trackWidth
 	 *            Distance between center of right tire and center of left tire,
 	 *            in same units as wheelDiameter.
 	 * @param leftPort
 	 *            The left Motor (e.g., Motor.C).
 	 * @param rightPort
 	 *            The right Motor (e.g., Motor.A).
 	 */
 	public DifferentialPilot(final double wheelDiameter, final double trackWidth) {
 		this(wheelDiameter, wheelDiameter, trackWidth, Motor.B, Motor.C);
 	}
 
 	/**
 	 * Allocates a DifferentialPilot object, and sets the physical parameters of
 	 * the NXT robot.<br>
 	 * 
 	 * @param leftWheelDiameter
 	 *            Diameter of the left wheel, in any convenient units (diameter
 	 *            in mm is usually printed on the tire).
 	 * @param rightWheelDiameter
 	 *            Diameter of the right wheel. You can actually fit
 	 *            intentionally wheels with different size to your robot. If you
 	 *            fitted wheels with the same size, but your robot is not going
 	 *            straight, try swapping the wheels and see if it deviates into
 	 *            the other direction. That would indicate a small difference in
 	 *            wheel size. Adjust wheel size accordingly. The minimum change
 	 *            in wheel size which will actually have an effect is given by
 	 *            minChange = A*wheelDiameter*wheelDiameter/(1-(A*wheelDiameter)
 	 *            where A = PI/(moveSpeed*360). Thus for a moveSpeed of 25
 	 *            cm/second and a wheelDiameter of 5,5 cm the minChange is about
 	 *            0,01058 cm. The reason for this is, that different while sizes
 	 *            will result in different motor speed. And that is given as an
 	 *            integer in degree per second.
 	 * @param trackWidth
 	 *            Distance between center of right tire apositionnd center of left tire,
 	 *            in same units as wheelDiameter.
 	 */
 	public DifferentialPilot(final double leftWheelDiameter,
 			final double rightWheelDiameter, final double trackWidth,
 			NXTRegulatedMotor leftMotor, NXTRegulatedMotor rightMotor) {
 		this.leftMotor = leftMotor;
 		_trackWidth = (float) trackWidth;
 		_leftWheelDiameter = (float) leftWheelDiameter;
 		_leftTurnRatio = (float) (_trackWidth / _leftWheelDiameter);
 		_leftDegPerDistance = (float) (360 / (Math.PI * _leftWheelDiameter));
 		// right
 		this.rightMotor = rightMotor;
 		_rightWheelDiameter = (float) rightWheelDiameter;
 		_rightTurnRatio = (float) (_trackWidth / _rightWheelDiameter);
 		_rightDegPerDistance = (float) (360 / (Math.PI * _rightWheelDiameter));
 		// both
 		setTravelSpeed(10);
 		setRotateSpeed(100);
 		leftMotor.resetTachoCount();
 		rightMotor.resetTachoCount();
 		setMoveType(MoveType.STOP);
 		setMoveType(MoveType.STOP);
 		leftMotor.removeListener();
 		rightMotor.removeListener();
 		motors[leftPort] = leftMotor;
 		motors[rightPort] = rightMotor;
 	}
 
 	public Position getPosition() {
 		return position.clone();
 	}
 
 	public double getRotation() {
 		return rotation;
 	}
 	
	public synchronized void disablePoseUpdate(){
 		poseIsLocked=true;
 	}
	public synchronized void enablePoseUpdate(){
 		poseIsLocked=false;
 	}
 	
	public synchronized boolean poseUpdateIsDisabled(){
 		return poseIsLocked;
 	}
 	
 	
 	public void interrupt() {
 		shouldStop = true;
 
 		if (poseUpdateThread != null)
 			poseUpdateThread.interrupt();
 		try{
 			while (!poseUpdateRunnable.reallyStopped) {
 				try {
 					Thread.sleep(1);
 				} catch (InterruptedException e) {
 					// This is not a problem
 				}
 			}
 			while (poseUpdateThread.isAlive()) {
 				try {
 					Thread.sleep(1);
 				} catch (InterruptedException e) {
 					// This is not a problem
 				}
 			}
 		} catch (NullPointerException e) {
 			// This means that another thread set the PoseUpdater to null while
 			// this method was running, this is not a problem
 		}
 		poseUpdateRunnable=null;
 		poseUpdateThread=null;
 	}
 
 	public boolean aMotorIsMoving() {
 //		 We don't think it is needed to check the left motor, because when the
 //		 right motor is moving, then so is the left one and otherwise there is
 //		 a problem.
 		return 
 //				leftMotor.isMoving()||
 				rightMotor.isMoving();
 	}
 
 //	/**
 //	 * TODO Returns the tachoCount of the left motor
 //	 * 
 //	 * @return tachoCount of left motor. Positive value means motor has moved
 //	 *         the robot forward.
 //	 */
 	private long getTachoCount(int port) {
 		return motors[port].getTachoCount();
 
 	}
 	
 	public void setSlowTravelSpeed(double newSlowTravelSpeed) {
 		slowTravelSpeed=newSlowTravelSpeed;
 	}
 
 	public void setHighTravelSpeed(double newFastTravelSpeed) {
 		fastTravelSpeed=newFastTravelSpeed;
 	}
 	
 	/**
 	 * set travel speed
 	 * 
 	 * @param travelSpeed
 	 *            : speed in random unit
 	 */
 	public void setTravelSpeed(final double travelSpeed) {
 		 _robotTravelSpeed = (float)travelSpeed;
 		// _motorSpeed = (int)Math.round(0.5 * travelSpeed *
 		// (_leftDegPerDistance + _rightDegPerDistance));
 		tMotorSpeed[leftPort] = (int) Math.round(travelSpeed * _leftDegPerDistance);
 		tMotorSpeed[rightPort] = (int) Math.round(travelSpeed * _rightDegPerDistance);
 	}
 	
 	private void recalculateMotorSpeeds(){
 		setTravelSpeed(_robotTravelSpeed);
 		setRotateSpeed(_robotRotateSpeed);
 	}
 	
 	public void setToSlowTravelSpeed() {
 		setTravelSpeed(slowTravelSpeed);
 	}
 
 	public void setToFastTravelSpeed() {
 		setTravelSpeed(fastTravelSpeed);
 	}
 
 	public void setToDefaultTravelSpeed() {
 		setTravelSpeed(defaultTravelSpeed);
 	}
 
 	
 	public double getTravelSpeed() {
 		return _robotTravelSpeed;
 	}
 
 	/**
 	 * sets the rotation speed of the vehicle, degrees per second
 	 * 
 	 * @param rotateSpeed
 	 */
 	public void setRotateSpeed(double rotateSpeed) {
 		_robotRotateSpeed = (float) rotateSpeed;
 		rSpeed[leftPort] = (int) Math.round(rotateSpeed * _leftTurnRatio);
 		rSpeed[rightPort] = -(int) Math.round(rotateSpeed * _rightTurnRatio);
 	}
 
 	public double getRotateSpeed() {
 		return _robotRotateSpeed;
 	}
 
 	/**
 	 * Starts the NXT robot moving forward.
 	 */
 	public void forward() {
 		if (!_type.equals(MoveType.STOP)){
 			stop();
 			stop();
 			}
 		setMoveType(MoveType.TRAVEL);
 		synchronized (leftMotor) {
 			synchronized (rightMotor) {
 				leftMotor.forward();
 				rightMotor.forward();
 			}
 		}
 		runPoseUpdater(false);
 	}
 
 //	private void setOppOutputState(int[] power, int[] limit) {
 //		int[] oppPower = { -power[0], -power[1] };
 //		setOutputState(oppPower, limit);
 //	}
 
 //	private void setOutputState(int[] power, int[] limit) {
 //		// System.out.println("from " + previousType + " to " + _type);
 //		if (poseUpdateRunnable != null) {
 //			poseUpdateRunnable.stop();
 //			try {
 //				while (!poseUpdateRunnable.reallyStopped)
 //					;
 //			} catch (NullPointerException e) {
 //				// This means another thread set the poseUpdateRunnable to null
 //				// during the whil loop
 //			}
 //		}
 //
 //		if (poseUpdateThread != null) {
 //			poseUpdateThread.interrupt();
 //			while (poseUpdateThread.isAlive())
 //				;
 //		}
 //		if (!previousType.equals(MoveType.STOP) && !_type.equals(MoveType.STOP)) {
 //			try {
 //				lowLevelSetOutputStates(zeros, zeros);
 //				previousType = MoveType.STOP;
 //			} catch (IOException e) {
 //				// TODO i3+;
 //			}
 //
 //		}
 //		runPoseUpdater(true);
 //		try {
 //
 //			lowLevelSetOutputStates(power, limit);
 //
 //			if (power[0] != 0 && limit[0] == 0) {
 //				poseUpdateRunnable = new PoseUpdater();
 //				poseUpdateThread = new Thread(poseUpdateRunnable);
 //				poseUpdateThread.start();
 //			} else {
 //				// power[0]==0: this make sure that the pose-updater runs only
 //				// once if the robot is stopped
 //
 //				runPoseUpdater(false);
 //			}
 //
 //		} catch (IOException e) {
 //			// TODO i3+
 //		}
 //	}
 
 	private boolean run = true;
 	public void setRunPoseUpdater(boolean bool){
 		this.run = bool;
 	}
 	public boolean getRunPoseUpdater(){
 		return this.run;
 	}
 	//This starts updating the position of the robot but first makes sure that no other pose-updater is running
 	public void runPoseUpdater(boolean once) {
 		if(getRunPoseUpdater()){
 			//Before we run the pose-updater, we first make sure that no other pose-updater is running
 			if(poseUpdateThread!=null){
 				interrupt();
 			}
 			poseUpdateRunnable = (new PoseUpdater(once));
 			poseUpdateThread = new Thread(poseUpdateRunnable);
 			if (once) {
 				poseUpdateThread.run();
 				poseUpdateThread=null;
 				poseUpdateRunnable=null;
 			} else {
 				poseUpdateThread.start();
 			}
 		}
 	}
 
 	private void setMotorInTravelMode() {
 		leftMotor.setSpeed(tMotorSpeed[leftPort]);
 		rightMotor.setSpeed(tMotorSpeed[rightPort]);
 	}
 	
 	private void setMotorInTurnMode() {
 		leftMotor.setSpeed(rSpeed[leftPort]);
 		rightMotor.setSpeed(rSpeed[rightPort]);
 	}
 
 	/**
 	 * Starts the NXT robot moving backward.
 	 */
 	public void backward() {
 		if (!_type.equals(MoveType.STOP)){
 			stop();
 			stop();
 			}
 		setMoveType(MoveType.TRAVEL);
 		synchronized (leftMotor) {
 			synchronized (rightMotor) {
 				leftMotor.backward();
 				rightMotor.backward();
 			}
 		}
 		runPoseUpdater(false);
 	}
 
 	/**
 	 * Rotates the NXT robot through a specific angle. Returns when angle is
 	 * reached. Wheels turn in opposite directions producing a zero radius turn.<br>
 	 * Note: Requires correct values for wheel diameter and track width. calls
 	 * rotate(angle)
 	 * 
 	 * @param angle
 	 *            The wanted angle of rotation in degrees. Positive angle rotate
 	 *            left (anti-clockwise), negative right.
 	 */
 	public void rotate(final double angle) {
 		if (!_type.equals(MoveType.STOP)){
 			stop();
 			stop();
 			}
 		setMoveType(MoveType.ROTATE);
 		int rotateAngleLeft = (int) (angle * _leftTurnRatio);
 		int rotateAngleRight = -(int) (angle * _rightTurnRatio);
 		synchronized (leftMotor) {
 			synchronized (rightMotor) {
 		leftMotor.rotate(rotateAngleLeft, true);
 		rightMotor.rotate(rotateAngleRight, true);
 		}
 			}		
 		runPoseUpdater(false);
 	}
 	
 	public void rotateB(final double angle,boolean block) {
 		if (!_type.equals(MoveType.STOP))
 			stop();
 			stop();
 		setMoveType(MoveType.ROTATE);
 		int rotateAngleLeft = (int) (angle * _leftTurnRatio);
 		int rotateAngleRight = -(int) (angle * _rightTurnRatio);
 		synchronized (leftMotor) {
 			synchronized (rightMotor) {
 				leftMotor.rotate(rotateAngleLeft, true);
 				rightMotor.rotate(rotateAngleRight, block);
 			}
 		}		
 		runPoseUpdater(false);
 	}
 
 	/*
 	 * This method can be overridden by subclasses to stop the robot if a hazard
 	 * is detected
 	 */
 	// protected void continueMoving()
 	// {
 	// }
 
 	/**
 	 * Stops the NXT robot.
 	 */
 	public synchronized void stop() {
 		 shouldStop = true;
 		setMoveType(MoveType.STOP);
 		simplyBreakMotors();
 		 shouldStop = false;
 		 runPoseUpdater(true);
 
 		// We change the movetype at the end so that the update can update
 		// accordingly to the movement that was going on before the stop
 		// setMoveType(MoveType.STOP);
 	}
 
 	private void simplyBreakMotors() {
 		synchronized (leftMotor) {
 			synchronized (rightMotor) {
 				leftMotor.stop(true);
 				rightMotor.stop();
 			}
 		}
 	}
 
 	/**
 	 * Moves the NXT robot a specific distance in an (hopefully) straight line.<br>
 	 * A positive distance causes forward motion, a negative distance moves
 	 * backward. If a drift correction has been specified in the constructor it
 	 * will be applied to the left motor. calls travel(distance, false)
 	 * 
 	 * @param distance
 	 *            The distance to move. Unit of measure for distance must be
 	 *            same as wheelDiameter and trackWidth.
 	 **/
 	public void travel(final double distance) {
		if (!_type.equals(MoveType.STOP)){
 			stop();
 			stop();
			}
 		setMoveType(MoveType.TRAVEL);
 		int leftRotation = (int) (distance * _leftDegPerDistance);
 		int rightRotation = (int) (distance * _rightDegPerDistance);
 		synchronized (leftMotor) {
 			synchronized (rightMotor) {
 				leftMotor.rotate(leftRotation, true);
 				rightMotor.rotate(rightRotation, true);
 			}
 		}
 		runPoseUpdater(false);
 	}
 	
 	public void travelB(final double distance,boolean block) {
		if (!_type.equals(MoveType.STOP)){
 			stop();
 			stop();
		}
 		setMoveType(MoveType.TRAVEL);
 		int leftRotation = (int) (distance * _leftDegPerDistance);
 		int rightRotation = (int) (distance * _rightDegPerDistance);
 		synchronized (leftMotor) {
 			synchronized (rightMotor) {
 				leftMotor.rotate(leftRotation, true);
 				rightMotor.rotate(rightRotation, block);
 			}
 		}
 		runPoseUpdater(false);
 	}
 
 	// /** TODO TODO
 	// * @return The move distance since it last started moving
 	// */
 	// public float getMovementIncrement()
 	// {
 	// float left = (getLeftCount() - _leftTC)/ _leftDegPerDistance;
 	// float right = (getRightCount() - _rightTC) / _rightDegPerDistance;
 	// return /**/ (left + right) / 2.0f;
 	// }
 
 	// /** TODO TODO
 	// * @return The angle rotated since rotation began.
 	// *
 	// */
 	// public float getAngleIncrement()
 	// {
 	// return /**/(((getRightCount() - _rightTC) / _rightTurnRatio) -
 	// ((getLeftCount() - _leftTC) / _leftTurnRatio)) / 2.0f;
 	// }
 
 	private void setMoveType(MoveType type) {
 		previousType = _type;
 		_type = type;
 		if(type.equals(MoveType.TRAVEL)){
 			setMotorInTravelMode();
 		}
 		else if(type.equals(MoveType.ROTATE)){
 			setMotorInTurnMode();
 		}
 	}
 
 	// private float _turnRadius = 0;
 	/**
 	 * Left motor port
 	 */
 	protected final int leftPort = 1;
 	/**
 	 * Right motor port
 	 */
 	protected final int rightPort = 2;
 
 	/**
 	 * Left motor port
 	 */
 	protected final NXTRegulatedMotor leftMotor;
 	/**
 	 * Right motor port
 	 */
 	protected final NXTRegulatedMotor rightMotor;
 
 	// /**
 	// * The motor at the inside of the turn. set by steer(turnRate)
 	// * used by other steer methods
 	// */
 	// private int insidePort;
 	/**
 	 * The motor at the outside of the turn. set by steer(turnRate) used by
 	 * other steer methodsl
 	 */
 	protected int outsidePort;
 	// /**
 	// * ratio of inside/outside motor speeds
 	// * set by steer(turnRate)
 	// * used by other steer methods;
 	// */
 	// private float _steerRatio;
 	// private boolean _steering = false;
 	/**
 	 * Left motor degrees per unit of travel.
 	 */
 	protected float _leftDegPerDistance;
 	/**
 	 * Right motor degrees per unit of travel.
 	 */
 	protected float _rightDegPerDistance;
 	/**
 	 * Left motor revolutions for 360 degree rotation of robot (motors running
 	 * in opposite directions). Calculated from wheel diameter and track width.
 	 * Used by rotate() and steer() methods.
 	 **/
 	private float _leftTurnRatio;
 	/**
 	 * Right motor revolutions for 360 degree rotation of robot (motors running
 	 * in opposite directions). Calculated from wheel diameter and track width.
 	 * Used by rotate() and steer() methods.
 	 **/
 	private float _rightTurnRatio;
 	/**
 	 * Speed of robot for moving in wheel diameter units per seconds. Set by
 	 * setSpeed(), setTravelSpeed()
 	 */
 	private float _robotTravelSpeed;
 	/**
 	 * Speed of robot for turning in degree per seconds.
 	 */
 	private float _robotRotateSpeed;
 	// /**
 	// * Motor speed degrees per second. Used by forward(),backward() and
 	// steer().
 	// */
 	// private int _motorSpeed;
 	/**
 	 * Distance between wheels. Used in steer() and rotate().
 	 */
 	private float _trackWidth;
 	/**
 	 * Diameter of left wheel.
 	 */
 	private float _leftWheelDiameter;
 	/**
 	 * Diameter of right wheel.
 	 */
 	private float _rightWheelDiameter;
 
 	/**
 	 * type of movement the robot is doing;
 	 */
 	protected MoveType _type;
 
 
 	private boolean shouldStop = false;
 
 	Thread poseUpdateThread;
 	PoseUpdater poseUpdateRunnable;
 
 	private boolean isMoving=false;
 
 	private class PoseUpdater implements Runnable {
 		boolean once;
 		private MoveType applicableType;
 		private MoveType otherType;
 		public boolean reallyStopped = false;
 
 		public PoseUpdater() {
 			this(false);
 		}
 
 //		public void stop() {
 //			shouldStop = true;
 //		}
 
 		public PoseUpdater(boolean once) {
 			this.once = once;
 		}
 
 		@Override
 		public void run() {
 			System.out.println("run "+ _type + " " + otherType );
 			reallyStopped = false;
 			long[] diffTacho = new long[3];
 
 			diffTacho[leftPort] = (-prevTachoCount[leftPort] + getTachoCount(leftPort));
 			diffTacho[rightPort] = (-prevTachoCount[rightPort] + getTachoCount(rightPort));
 
 			if (once) {
 				applicableType = previousType;
 				otherType = _type;
 				update(diffTacho);
 
 			} else {
 				applicableType = _type;
 				otherType = previousType;
 
 				while ((diffTacho[leftPort] != 0 || diffTacho[rightPort] != 0 || aMotorIsMoving())) {
 					if (Thread.interrupted() || shouldStop) {
 						stopBecauseOfInterrupt(diffTacho);
 						break;
 					}
 					
 					update(diffTacho);
 					diffTacho[leftPort] = (-prevTachoCount[leftPort] + getTachoCount(leftPort));
 					diffTacho[rightPort] = (-prevTachoCount[rightPort] + getTachoCount(rightPort));
 					try {
 						if (!_type.equals(MoveType.STOP)) {
 							// The underlying code is used to make the system wait 100 ms
 							// between updating the position, the while loop is
 							// used to react quickly to stop commando's.
 							long sleepStart = System.currentTimeMillis();
 							while (System.currentTimeMillis() - sleepStart < 100)
 								if (!shouldStop)
 									Thread.sleep(25);
 								else
 									throw new InterruptedException(
 											"an interrupt signal has been sent by the program");
 							
 						}
 					} catch (InterruptedException e) {
 							stopBecauseOfInterrupt(diffTacho);
 						break;
 					}
 				}
 			}
 			reallyStopped = true;
 
 		}
 
 		private void stopBecauseOfInterrupt(long[] diffTacho) {
 			simplyBreakMotors();
 			applicableType=_type;
 			otherType=previousType;
 			update(diffTacho);
 			diffTacho[leftPort] = (-prevTachoCount[leftPort] + getTachoCount(leftPort));
 			diffTacho[rightPort] = (-prevTachoCount[rightPort] + getTachoCount(rightPort));
 			reallyStopped = true;
 		}
 
 		private void update(long[] diffTacho) {
			if(! poseUpdateIsDisabled()){
			
 			if (applicableType.equals(MoveType.TRAVEL)) {
 				position.move(getRotation(), ((float) diffTacho[leftPort])
 						/ _leftDegPerDistance);
 			} else if (applicableType.equals(MoveType.ROTATE)) {
 				setRotation(calcNewOrientation(((float) diffTacho[leftPort])
 						/ _leftTurnRatio / 2.0));
 
 				setRotation(calcNewOrientation(-((float) diffTacho[rightPort])
 						/ _rightTurnRatio / 2.0));
 
 			} else if (applicableType.equals(MoveType.STOP)) {
 				if (otherType.equals(MoveType.TRAVEL)) {
 					position.move(rotation, ((float) diffTacho[leftPort])
 							/ _leftDegPerDistance);
 				} else if (otherType.equals(MoveType.ROTATE)) {
 					setRotation(calcNewOrientation(((float) diffTacho[leftPort])
 							/ _leftTurnRatio / 2.0));
 
 					setRotation(calcNewOrientation(-((float) diffTacho[rightPort])
 							/ _rightTurnRatio / 2.0));
 				}
			}
			}
 			prevTachoCount[leftPort] += diffTacho[leftPort];
 			prevTachoCount[rightPort] += diffTacho[rightPort];
 
 		}
 
 		private double calcNewOrientation(double turnAmount) {
 			double newOrientation = getRotation() + turnAmount;
 			while (newOrientation <= -180) {
 				newOrientation += 360;
 			}
 			while (newOrientation > 180) {
 				newOrientation -= 360;
 			}
 			return newOrientation;
 		}
  }
 
 	public void keepTurning(boolean left) {
 		if (!_type.equals(MoveType.STOP)){
 			stop();
 			stop();
 			}
 		setMoveType(MoveType.ROTATE);
 		synchronized (leftMotor) {
 			synchronized (rightMotor) {
 				if (left) {
 					leftMotor.backward();
 					rightMotor.forward();
 
 				} else {					
 					leftMotor.forward();
 					rightMotor.backward();
 				}
 			}
 		}
 		runPoseUpdater(false);
 
 	}
 	
 	public void setRotation(double rotation) {
 		this.rotation = rotation;
 	}
 	
 	
 	public void setXCo(double x) {
 		position.setX(x);
 	}
 	
 	public void setYCo(double y) {
 		position.setY(y);
 	}
 	
 	public void setReadingBarcode(boolean barcode){
 		this.barcode = barcode;
 	}
 	
 	
 	private boolean barcode;
 	/**
 	 * This method returns whether the robot is moving according to the
 	 * poseUpdater, and should be used by the getPose Method(from the bluetooth
 	 * connection class) and set by the poseUpdater We could also check the
 	 * motors in this method with leftMotor.isMoving() for example, but we think
 	 * this makes the program slower.
 	 */
 	public boolean isMoving() {
 	if(barcode){
 		return true;
 	}
 	try{
 		return !poseUpdateRunnable.reallyStopped;
 		}catch (NullPointerException e) {
 			//if the poseUpdateRunnable is null, then the robot is not moving
 			return false;
 		}
 		
 	}
 
 
 
 	public void setWheelDiameter(double diameter) {
 		_leftWheelDiameter = (float) diameter;
 		
 		_leftTurnRatio = (float) (_trackWidth / _leftWheelDiameter);
 		_leftDegPerDistance = (float) (360 / (Math.PI * _leftWheelDiameter));
 		_rightWheelDiameter = (float) diameter;
 		_rightTurnRatio = (float) (_trackWidth / _rightWheelDiameter);
 		_rightDegPerDistance = (float) (360 / (Math.PI * _rightWheelDiameter));
 		recalculateMotorSpeeds();
 	}
 
 	public void setTrackWidth(double trackWidth) {
 		_trackWidth=(float) trackWidth;
 		_leftTurnRatio = (float) (_trackWidth / _leftWheelDiameter);
 		_leftDegPerDistance = (float) (360 / (Math.PI * _leftWheelDiameter));
 		_rightTurnRatio = (float) (_trackWidth / _rightWheelDiameter);
 		_rightDegPerDistance = (float) (360 / (Math.PI * _rightWheelDiameter));
 		recalculateMotorSpeeds();
 	}
 
 	public void setToDefaultRotateSpeed() {
 		setRotateSpeed(defaultRotateSpeed);
 	}
 
 	public void setDefaultTravelSpeed(double speed) {
 		defaultTravelSpeed=speed;
 	}
 
 	public void setDefaultRotateSpeed(double speed) {
 		defaultRotateSpeed=speed;
 	}
 }
