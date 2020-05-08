 package simulator;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 import javax.swing.Timer;
 
 import communication.SeesawStatus;
 
 import field.*;
 import field.fromfile.FieldFactory;
 import field.simulation.FieldSimulation;
 
 import robot.AbstractRobotConnector;
 import robot.Collision;
 import robot.DebugBuffer;
 import robot.Robot;
 import robot.RobotModel;
 import robot.RobotPool;
 import robot.SensorBuffer;
 import simulator.infraredsensor.InfraredSensor;
 import simulator.lightsensor.LightSensor;
 import simulator.lightsensor.LightSensorUpdate;
 import simulator.touchsensor.TouchSensor;
 import simulator.ultrasonicsensor.UltrasonicSensor;
 
 /**
  * @author  Samuel
  */
 public class VirtualRobotConnector implements ISimulator, IMovementManager {
 	
 	private static VirtualRobotConnector instance;
 	
 	public static VirtualRobotConnector getInstance() {
 		return instance;
 	}
 
 	private Timer timer = new Timer(1, new ActionListener() {
 	    public void actionPerformed(ActionEvent evt) {
 	    		tick();
 	    		TouchSensor.getInstance().isPressed();
 	    		InfraredSensor.getInstance().getInfrared();
 	        }
 	    });
 
 	public VirtualRobotConnector() {
 		this(.2, 2);
 		//this(.1, .5);
 	}
 
 	public VirtualRobotConnector(double mvSpeed, double trnSpeed) {
 		setMoveSpeed(mvSpeed);
 		setTurnSpeed(trnSpeed);
 		instance = this;
 	}
 	
 
 	private double width = 6;
 	private double length = 10;
 	
 	public double[][] getCorners(double x, double y) {
 		double[][] arr = new double[4][2];
 		
 		double tx = x;//getTDistanceX();
 		double ty = y;//getTDistanceY();
 		double ta = -getTRotation() * Math.PI / 180;
 		
 		
 		//left upper
 		double lux = - width;
 		double luy = length;
 		
 		arr[0][0] = tx + ( lux * Math.cos(ta) - luy * Math.sin(ta)); 
 		arr[0][1] = ty + ( lux * Math.sin(ta)  + luy * Math.cos(ta)); 
 		//right upper
 		double rux = width;
 		double ruy = length;
 		
 		arr[1][0] = tx + ( rux * Math.cos(ta)  - ruy * Math.sin(ta));
 		arr[1][1] = ty + ( rux * Math.sin(ta)  + ruy * Math.cos(ta)); 
 		//left lower
 		double llx = width;
 		double lly = - length;
 		
 		arr[2][0] = tx + ( llx * Math.cos(ta)  - lly * Math.sin(ta));  
 		arr[2][1] = ty + ( llx * Math.sin(ta)  + lly * Math.cos(ta));
 		//right lower
 		double rlx = - width;
 		double rly = - length;
 		
 		arr[3][0] = tx + ( rlx * Math.cos(ta)  - rly * Math.sin(ta));
 		arr[3][1] = ty + ( rlx * Math.sin(ta)  + rly * Math.cos(ta)); 
 		
 		return arr;
 	}
 	
 	public void setSimAngle(double angle) {
 		tRotation = angle;
 		this.angle = angle * Math.PI / 180;
 	}
 	
 	public void setSimLoc(double x, double y, double angle) {
 		tdistancex = x;
 		tdistancey = y;
 		tRotation = angle / Math.PI * 180;
 		this.angle = angle;
 		setStartx(x);
 		setStarty(y);
 		DebugBuffer.addInfo("pos " + x + " " + y + " " + angle);
 	}
 
 	private double moveSpeed = 0.0;
 	
 	public double getMoveSpeed() {
 		return moveSpeed;
 	}
 	
 	public void setMoveSpeed(double moveSpeed) {
 		this.moveSpeed = moveSpeed;
 	}
 	
 	private double turnSpeed = 0.0;
 	
 	public double getTurnSpeed() {
 		return turnSpeed;
 	}
 	
 	public void setTurnSpeed(double turnSpeed) {
 		this.turnSpeed = turnSpeed;
 	}
 	
 	private TurningMode turningMode = TurningMode.None;
 	
 	public void setTurningMode(TurningMode mode) {
 		turningMode = mode;
 	}
 	
 	private TurningMode getTurningMode() {
 		return turningMode;
 	}
 	
 	private double rotationTurned = 0.0;
 	// in degrees
 	private double tRotation = 0.0;
 	
 	public double getTRotation() {
 		return tRotation;
 	}
 
 	public double getRotationTurned() {
 		double temp = rotationTurned;
 		rotationTurned = 0.0;
 		return temp;
 	}
 
 	public double getRotationTurnedRadian() {
 		return rotationTurned * Math.PI / 180;
 	}
 	
 	private double angle = 0;
 	
 	public void turn() {
 		int sign = 0;
 		if (getTurningMode() == TurningMode.Right) {
 			sign = 1;
 		} else if (getTurningMode() == TurningMode.Left){
 			sign = -1;
 		}
 		rotationTurned += getTurnSpeed() * sign;
 		tRotation += getTurnSpeed() * sign;
 		if (tRotation < 0)
 			tRotation += 360;
 		if (tRotation > 360) 
 			tRotation -= 360;
 		angle += getTurnSpeed() * sign * Math.PI / 180;
 	}
 	
 	public void turnLeft(double angle) {
 		Command cmd = new Command((int)(angle / getTurnSpeed()), CommandType.LEFT);
 		//System.out.println("d "  + (int)(angle / getTurnSpeed()));
 		addCmd(cmd);
 	}
 	
 	public void turnRight(double angle) {
 		Command cmd = new Command((int)(angle / getTurnSpeed()), CommandType.RIGHT);
 		addCmd(cmd);
 	}
 	
 	private int ticksTurning;
 	
 	private int getTicksTurning() {
 		return ticksTurning;
 	}
 
 	public void setTicksTurning(int ticksTurning) {
 		this.ticksTurning = ticksTurning;
 	}
 	
 	private void checkTicksTurning() {
 		if (getTicksTurning() == 1) {
 			//resetCurrCmd();
 			stopMoving();
 		}
 	}
 	
 	private void reduceTicksTurning() {
 		if (getTicksTurning() >= 1)
 			setTicksTurning(getTicksTurning() - 1);
 	}
 	
 	public void startTurningLeft() {
 		Command cmd = new Command(CommandType.STARTLEFT);
 		addCmd(cmd);
 	}
 	
 	public void startTurningRight() {
 		Command cmd = new Command(CommandType.STARTRIGHT);
 		addCmd(cmd);
 	}
 	
 	private MovingMode movingMode = MovingMode.None;
 	
 	public void setMovingMode(MovingMode mode) {
 		movingMode = mode;
 	}
 	
 	private MovingMode getMovingMode() {
 		return movingMode;
 	}
 
 	@Override
 	public void moveForward(double distance) {
 		if (distance < 0) throw new RuntimeException();
 		distance = distance / 10;
 		Command cmd = new Command((int)Math.round(distance / getMoveSpeed()), CommandType.FORWARD);
 		addCmd(cmd);
 
 	}
 	
 	@Override
 	public void moveBackward(double distance) {
 		if (distance < 0) throw new RuntimeException();
 		distance = distance / 10;
 		Command cmd = new Command((int)Math.round(distance / getMoveSpeed()), CommandType.BACKWARD);
 		addCmd(cmd);
 	}
 	
 	private int ticksMoving;
 	
 	private int getTicksMoving() {
 		return ticksMoving;
 	}
 
 	public void setTicksMoving(int ticksMoving) {
 		this.ticksMoving = ticksMoving;
 	}
 	
 	private void checkTicksMoving() {
 		if (getTicksMoving() == 1) {
 			stopMoving();
 		}
 	}
 	
 	private void reduceTicksMoving() {
 		if (getTicksMoving() >= 1)
 			setTicksMoving(getTicksMoving() - 1);
 	}
 
 	public void startMovingForward() {
 		Command cmd = new Command(CommandType.STARTFORWARD);
 		addCmd(cmd);
 	}
 	
 	public void startMovingBackward() {
 		Command cmd = new Command(CommandType.STARTBACKWARD);
 		addCmd(cmd);
 	}
 	
 	public void stopMoving() {
 		resetCurrCmd();
 		setMovingMode(MovingMode.None);
 		setTurningMode(TurningMode.None);
 	}
 	
 	private boolean stopOnWhite = false;
 	
 	@Override
 	public void orientOnWhiteLine(boolean b) {
		moveForward(170);
 	}
 	
 	public void orientOnWhiteLineExec(boolean b) {
 		stopOnWhite = true;
 		startMovingForward();
 	}
 
 	public boolean isMoving() {
 		return !(movingMode == MovingMode.None && turningMode == TurningMode.None);
 	}
 
 	private double distanceMoved = 0.0;
 
 	public double getDistanceMoved() {
 		double temp = distanceMoved;
 		distanceMoved = 0.0;
 		return temp;
 	}
 	
 	private double tdistancex = 0;
 	public double getTDistanceX() {
 		return tdistancex;
 	}
 
 	private double tdistancey = 0;
 	
 	public double getTDistanceY() {
 		return tdistancey;
 	}
 	
 	private double startx;
 	
 	private double starty;
 
 	public double getStartx() {
 		return startx;
 	}
 
 	public void setStartx(double startx) {
 		this.startx = startx;
 	}
 
 	public double getStarty() {
 		return starty;
 	}
 
 	public void setStarty(double starty) {
 		this.starty = starty;
 	}
 
 	public void move() {
 		int sign = 0;
 		if (getMovingMode() == MovingMode.Forward) {
 			sign = 1;
 		} else if (getMovingMode() == MovingMode.Backward){
 			sign = -1;
 		}
 		double newx = tdistancex + getMoveSpeed() * sign * Math.sin(angle);
 		double newy = tdistancey + getMoveSpeed() * sign * Math.cos(angle);
 		double[][] newcorners = getCorners(newx, newy);
 		//if (newx != 0.0 || newy != 0.0)
 			//System.out.println("newx " + newx + " newy " + newy);
 		if (maze.collidesWithBall(newcorners)) {
 			hasBall = true;
 		}
 		if (!checkCollision(newcorners)) {
 			distanceMoved += getMoveSpeed() * sign;
 			tdistancex = newx;
 			tdistancey = newy;
 		} else {
 			//System.out.println("wall");
 		}
 	}
 	
 	private boolean checkCollision(double[][] corners) {
 		boolean collides = false;
 		
 		try {
 			if (maze.collidesWithBorder(corners)) {
 				collides = true;
 			}
 		} catch (IllegalArgumentException e){
 		}
 		
 		if (robotCollision) {
 			if (robotPool != null) {
 				for (RobotModel robot : robotPool.getOtherRobots()) {
 					if (Collision.collides(robot, corners)) {
 						//System.out.println("collision");
 						collides = true;
 					}
 				}
 			}
 		}
 		
 		return collides;
 	}
 	
 	private boolean robotCollision = false;
 	
 	public void setRobotCollision(boolean set) {
 		robotCollision = set;
 	}
 	
 	public void tick() {
 		move();
 		turn();
 		checkTicksMoving();
 		reduceTicksMoving();
 		checkTicksTurning();
 		reduceTicksTurning();
 		if (maze.isOnWhiteBorder() && stopOnWhite) {
 			stopOnWhite = false;
 			stopMoving();
 			
 		}
 		if (!cmdQueue.isEmpty() && currCmd == null) {
 			currCmd = cmdQueue.poll();
 			currCmd.execute(this);
 		}
 		updatePassing();
 		maze.update();
 		//System.out.println(getDistanceXMoved() + "  " + getDistanceYMoved() + " " + getRotation());
 		//System.out.println(distanceXMoved + "  " + distanceYMoved + " " + rotation);
 	}
 	
 	private Queue<Command> cmdQueue = new LinkedList<Command>();
 	
 	private Command currCmd = null;
 	
 	private void addCmd(Command cmd) {
 		if (cmd == null)
 			throw new IllegalArgumentException();
 		cmdQueue.add(cmd);
 	}
 	
 	public void resetCurrCmd() {
 		currCmd = null;
 	}
 
 	@Override
 	public void initialize() {
 		timer.start();
 		//setupField();
 	}
 
 
 	@Override
 	public void setSimField(FieldSimulation world) {
 		this.maze = world;
 		getMaze().setLocalSimulator(this);
 		Thread lsThr = new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				while (true) {
 		    		LightSensorUpdate update = LightSensor.getInstance().lightSensorVigilanteLoop();
 		    		SensorBuffer.addLightUpdate(update.ordinal());
 				}
 				
 			}
 		});
 		lsThr.start();
 	}
 	@Override
 	public void terminate() {
 		timer.stop();
 		
 	}
 	
 	private FieldSimulation maze;
 	
 	public FieldSimulation getMaze() {
 		return maze;
 	}
 
 	private void setupField() {
 		if (maze == null) {
 //			maze = new FieldSimulation("C:\\demo2.txt");
 //			maze = new FieldSimulation("/Users/elinetje2/Documents/2012-2013/Semester 2/P&O/demo2.txt");
 		}
 	}
 	
 	public FieldSimulation getField() {
 		return maze;
 	}
 	
 	public void SetupFieldFile(String filename) {
 		maze = new FieldSimulation(filename);
 	}
 	
 	boolean passedWhiteBorder = false;
 	
 	public boolean passedWhiteBorder() {
 		if (passedWhiteBorder) {
 			passedWhiteBorder = false;
 			return true;
 		}
 		return false;
 	}
 	
 	boolean wasWhite = false;
 	
 	public void updatePassing() {
 		if (maze.isOnWhiteBorder()) {
 			wasWhite = true;
 		} else {
 			if (wasWhite) {
 				passedWhiteBorder = true;
 				wasWhite = false;
 			}
 		}
 	}
 
 	public void zeroPos() {
 		tdistancex = 0;
 		tdistancey = 0;
 		tRotation = 0;
 		rotationTurned = 0;
 		distanceMoved = 0;
 		setMovingMode(MovingMode.None);
 		setTurningMode(TurningMode.None);
 		ticksMoving = 0;
 		ticksTurning = 0;
 		maze = null;
 		setupField();
 		SensorBuffer.getDistances().clear();
 	}
 	
 	@Override
 	public void scanSonar() {
 		UltrasonicSensor.getInstance().scanForWalls();		
 	}
 	
 	@Override
 	public void setOnCenterTile(Robot robot){
 		
 	}
 
 	@Override
 	public void newTileScan() {
 		UltrasonicSensor.getInstance().newTileScan();
 	}
 
 	@Override
 	public boolean isStartTile() {
 		return false;
 	}
 
 	@Override
 	public boolean isFinishTile() {
 		return false;
 	}
 
 	@Override
 	public boolean isScanning() {
 		return false;
 	}
 
 	@Override
 	public void scanOnlyLines(boolean flag) {
 		
 	}
 
 	@Override
 	public void checkScan() {
 		UltrasonicSensor.getInstance().checkScan();
 	}
 
 	private boolean hasBall;
 	
 	@Override
 	public boolean hasBall() {
 		return hasBall;
 	}
 	
 	public void setHasBall(boolean set) {
 		hasBall = set;
 	}
 
 	private int objectNr;
 	
 	@Override
 	public void setObjectNr(int objectNr) {
 		this.objectNr = objectNr;
 	}
 
 	private int teamNr = -1;
 	
 	@Override
 	public int getTeam() {
 		return teamNr;
 	}
 
 	public void setTeamNr(int nr) {
 		teamNr = nr;
 	}
 	
 	@Override
 	public int getObjectNr() {
 		return objectNr;
 	}
 
 	@Override
 	public double getWidth() {
 		return width;
 	}
 
 	@Override
 	public double getLength() {
 		return length;
 	}
 
 	@Override
 	public void setCmdMger(CommandManager cmdMger) {
 		
 	}
 	
 	private RobotPool robotPool;
 	
 	public RobotPool getRobotPool() {
 		return robotPool;
 	}
 
 	@Override
 	public void setRobotPool(RobotPool robotPool) {
 		this.robotPool = robotPool;
 	}
 
 	private SeesawStatus status;
 	
 	@Override
 	public SeesawStatus getSeesawStatus() {
 		return status;
 	}
 
 	@Override
 	public void setSeesawStatus(SeesawStatus status) {
 		this.status = status;
 	}
 
 	private boolean pausedLightSensor = false;
 	
 	@Override
 	public void pauseLightSensor() {
 		pausedLightSensor = true;
 	}
 
 	@Override
 	public void resumeLightSensor() {
 		pausedLightSensor = false;
 	}
 }
