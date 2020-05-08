 package communication;
 
 import java.awt.Toolkit;
 
 import mapping.MapGraph;
 
 import simulator.SimulationJPanel;
 import gui.SilverSurferGUI;
 
 public class StatusInfoBuffer {
 
 	private SimulationJPanel panel;
 	private int lightSensorInfo;
 	private int ultraSensorInfo;
 	private boolean touchSensor1Info;
 	private boolean touchSensor2Info;
 	private boolean leftMotorMoving;
 	private boolean rightMotorMoving;
 	private int leftMotorSpeed;
 	private int rightMotorSpeed;
 	private boolean busy;
 	private int barcode;
 	private double angle;
 	private double[] coordinatesAbsolute = new double[2];
 	private int xCoordinateRelative;
 	private int yCoordinateRelative;
 	private double startPositionAbsoluteX;
 	private double startPositionAbsoluteY;
 	private int startPositionRelativeX;
 	private int startPostitionRelativeY;
 
 	private SilverSurferGUI SSG;
 	/**
 	 * Check if the buffer can be updated (can be updated when false).
 	 */
 	private boolean isBufferUsed = false;
 	/**
 	 * Amount of datapoints kept by this buffer.
 	 */
 	private final int BUFFER_SIZE = 300;
 	
 	private LSInfoNode startLSInfo = new LSInfoNode();
 	private LSInfoNode endLSInfo = new LSInfoNode();
 	private USInfoNode startUSInfo = new USInfoNode();
 	private USInfoNode endUSInfo = new USInfoNode();
 	private TS1InfoNode startTS1Info = new TS1InfoNode();
 	private TS1InfoNode endTS1Info = new TS1InfoNode();
 	private TS2InfoNode startTS2Info = new TS2InfoNode();
 	private TS2InfoNode endTS2Info = new TS2InfoNode();
 
 	private int amtLSUpdated = 0;
 	private int amtUSUpdated = 0;
 	private int amtTS1Updated = 0;
 	private int amtTS2Updated = 0;
 
 
 	/**
 	 * Used to make a linked-list stack of LS-information.
 	 */
 	public class LSInfoNode {
 		public int info;
 		public LSInfoNode next;
 	}
 	
 	/**
 	 * Used to make a linked-list stack of US-information.
 	 */
 	public class USInfoNode {
 		public int info;
 		public USInfoNode next;
 	}
 	
 	/**
 	 * Used to make a linked-list stack of TS1-information.
 	 */
 	public class TS1InfoNode {
 		public boolean info;
 		public TS1InfoNode next;
 	}
 	
 	/**
 	 * Used to make a linked-list stack of TS1-information.
 	 */
 	public class TS2InfoNode {
 		public boolean info;
 		public TS2InfoNode next;
 	}
 
 
 	/**
 	 * Set a claim on the buffer so that information in it can be used to calculate. 
 	 * Buffer won't update as long as claim has been withdraw.
 	 */
 	public void claimBuffer() {
 		isBufferUsed = true;
 
 	}
 
 	/**
 	 * Makes sure the buffer can be updated again.
 	 */
 	public void freeBuffer() {
 		isBufferUsed = false;
 
 	}
 
 	/**
 	 * Returns the latest info available for the Light Sensor
 	 */
 	public int getLatestLightSensorInfo() {
 		return lightSensorInfo;
 	}
 
 	/**
 	 * Add new info for the light Sensor.
 	 */
 	public void addLightSensorInfo(int lightSensorInfo) {
 		this.lightSensorInfo = lightSensorInfo;
 		SSG.updateStatus();
 		if(!isBufferUsed){
 			//Voeg toe aan buffer.
 			if(amtLSUpdated < BUFFER_SIZE){
 				if(amtLSUpdated != 0){
 					LSInfoNode temp = new LSInfoNode();
 					temp.info = lightSensorInfo;
 					temp.next = null;
 					endLSInfo.next = temp;
 					endLSInfo = temp;
 				}
 				else{
 					startLSInfo = new LSInfoNode();
 					startLSInfo.info = lightSensorInfo;
 					startLSInfo.next = null;
 					endLSInfo = startLSInfo;
 				}
 			}
 			//Vewijder eerste element en voeg n toe.
 			else{
 				startLSInfo = startLSInfo.next;
 				LSInfoNode temp = new LSInfoNode();
 				temp.info = lightSensorInfo;
 				temp.next = null;
 				endLSInfo.next = temp;
 				endLSInfo = temp;
 			}
 			amtLSUpdated++;
 
 		}
 	}
 
 	/**
 	 * Returns the latest info available for the Ultrasonic Sensor
 	 */
 	public int getLatestUltraSensorInfo() {
 		return ultraSensorInfo;
 	}
 
 	/**
 	 * Add new info for the Ulstrasonic Sensor.
 	 */
 	public void addUltraSensorInfo(int ultraSensorInfo) {
 		this.ultraSensorInfo = ultraSensorInfo;
 
 		if(!isBufferUsed){
 			//Voeg toe aan buffer.
 			if(amtUSUpdated < BUFFER_SIZE){
 				if(amtUSUpdated != 0){
 					USInfoNode temp = new USInfoNode();
 					temp.info = ultraSensorInfo;
 					temp.next = null;
 					endUSInfo.next = temp;
 					endUSInfo = temp;
 				}
 				else{
 					startUSInfo = new USInfoNode();
 					startUSInfo.info = ultraSensorInfo;
 					startUSInfo.next = null;
 					endUSInfo = startUSInfo;
 				}
 			}
 			//Vewijder eerste element en voeg n toe.
 			else{
 				startUSInfo = startUSInfo.next;
 				USInfoNode temp = new USInfoNode();
 				temp.info = ultraSensorInfo;
 				temp.next = null;
 				endUSInfo.next = temp;
 				endUSInfo = temp;
 			}
 			amtUSUpdated++;
 		}
 		SSG.updateStatus();
 
 	}
 
 	/**
 	 * Returns the latest info available for the first Touch Sensor
 	 */
 	public boolean getLatestTouchSensor1Info() {
 		return touchSensor1Info;
 	}
 
 	/**
 	 * Add new info for Touch Sensor 1.
 	 */
 	public void addTouchSensor1Info(boolean touchSensor1Info) {
 		//Computer makes a beep when bumping a wall.
 		if(touchSensor1Info)
 			Toolkit.getDefaultToolkit().beep();
 		
 
 		this.touchSensor1Info = touchSensor1Info;
 		SSG.updateStatus();
 		if(!isBufferUsed){
 			//Voeg toe aan buffer.
 			if(amtTS1Updated < BUFFER_SIZE){
 				if(amtTS1Updated != 0){
 					TS1InfoNode temp = new TS1InfoNode();
 					temp.info = touchSensor1Info;
 					temp.next = null;
 					endTS1Info.next = temp;
 					endTS1Info = temp;
 				}
 				else{
 					startTS1Info = new TS1InfoNode();
 					startTS1Info.info = touchSensor1Info;
 					startTS1Info.next = null;
 					endTS1Info = startTS1Info;
 				}
 			}
 			//Vewijder eerste element en voeg n toe.
 			else{
 				startTS1Info = startTS1Info.next;
 				TS1InfoNode temp = new TS1InfoNode();
 				temp.info = touchSensor1Info;
 				temp.next = null;
 				endTS1Info.next = temp;
 				endTS1Info = temp;
 			}
 			amtTS1Updated++;
 
 		}
 	}
 	/**
 	 * Returns the latest info available for the 2nd Touch Sensor
 	 */
 	public boolean getLatestTouchSensor2Info() {
 		return touchSensor2Info;
 	}
 
 	/**
 	 * Add new info for Touch Sensor 1.
 	 */
 	public void addTouchSensor2Info(boolean touchSensor2Info) {
 		//Computer makes a beep when bumping a wall.
 		if(touchSensor2Info)
 			Toolkit.getDefaultToolkit().beep();
 		
 		
 		this.touchSensor2Info = touchSensor2Info;
 		SSG.updateStatus();
 		if(!isBufferUsed){
 			//Voeg toe aan buffer.
 			if(amtTS2Updated < BUFFER_SIZE){
 				if(amtTS2Updated != 0){
 					TS2InfoNode temp = new TS2InfoNode();
 					temp.info = touchSensor2Info;
 					temp.next = null;
 					endTS2Info.next = temp;
 					endTS2Info = temp;
 				}
 				else{
 					startTS2Info = new TS2InfoNode();
 					startTS2Info.info = touchSensor2Info;
 					startTS2Info.next = null;
 					endTS2Info = startTS2Info;
 				}
 			}
 			//Vewijder eerste element en voeg n toe.
 			else{
 				startTS2Info = startTS2Info.next;
 				TS2InfoNode temp = new TS2InfoNode();
 				temp.info = touchSensor2Info;
 				temp.next = null;
 				endTS2Info.next = temp;
 				endTS2Info = temp;
 			}
 			amtTS2Updated++;
 
 		}
 	}
 
 	public boolean getLeftMotorMoving() {
 		return leftMotorMoving;
 	}
 
 	public void setLeftMotorMoving(boolean leftMotorMoving) {
 		this.leftMotorMoving = leftMotorMoving;
 		SSG.updateStatus();
 	}
 
 	public boolean getRightMotorMoving() {
 		return rightMotorMoving;
 	}
 
 	public void setRightMotorMoving(boolean rightMotorMoving) {
 		this.rightMotorMoving = rightMotorMoving;
 		SSG.updateStatus();
 	}
 
 	public int getLeftMotorSpeed() {
 		return leftMotorSpeed;
 	}
 
 	public void setLeftMotorSpeed(int leftMotorSpeed) {
 		this.leftMotorSpeed = leftMotorSpeed;
 		SSG.updateStatus();
 	}
 
 	public int getRightMotorSpeed() {
 		return rightMotorSpeed;
 	}
 
 	public void setRightMotorSpeed(int rightMotorSpeed) {
 		this.rightMotorSpeed = rightMotorSpeed;
 		SSG.updateStatus();
 	}
 
 	public SilverSurferGUI getSSG() {
 		return SSG;
 	}
 
 	public void setSSG(SilverSurferGUI SSG) {
 		this.SSG = SSG;
 	}
 	
 	/**
 	 * Get the head of the LS-infostack.
 	 */
 	public LSInfoNode getStartLSInfo() {
 		return startLSInfo;
 	}
 	/**
 	 * Get the head of the US-infostack.
 	 */
 	public USInfoNode getStartUSInfo() {
 		return startUSInfo;
 	}
 	/**
 	 * Get the head of the TS1-infostack.
 	 */
 	public TS1InfoNode getStartTS1Info() {
 		return startTS1Info;
 	}
 	/**
 	 * Get the head of the TS2-infostack.
 	 */
 	public TS2InfoNode getStartTS2Info() {
 		return startTS2Info;
 	}
 	
 	public boolean getBusy() {
 		return busy;
 	}
 	
 	public void setBusy(boolean busy) {
 		this.busy = busy;
 		SSG.updateStatus();
 	}	
 	
 	public int getBarcode() {
 		return barcode;
 	}
 	
 	public void setBarcode(int barcode) {
 		this.barcode = barcode;
 		SSG.executeBarcode();
 	}
 	
 	public double getAngle() {
 		return angle;
 	}
 	
 	public void setAngle(double angle) {
 		this.angle = angle;
 		SSG.updateStatus();
 	}
 	
 	public void setStartPositionAbsoluteX(double startPositionAbsoluteX){
 		this.startPositionAbsoluteX = startPositionAbsoluteX;
 	}
 	
 	public void setStartPositionAbsoluteY(double startPositionAbsoluteY){
 		this.startPositionAbsoluteY = startPositionAbsoluteY;
 	}
 	
 	public void setStartPositionRelativeX(int startPositionAbsoluteX){
 		this.startPositionRelativeX = startPositionRelativeX;
 	}
 	
 	public void setStartPositionRelativeY(int startPositionAbsoluteY){
 		this.startPositionAbsoluteY = startPositionAbsoluteY;
 	}
 	
 	public int getXCoordinateRelative(){
 		return xCoordinateRelative;
 	}
 	
 	public void setXCoordinateRelative(int x){
 		xCoordinateRelative = x;
 	}
 	
 	public double getYCoordinateRelative(){
 		return yCoordinateRelative;
 	}
 	
 	public void setYCoordinateRelative(int y){
 		yCoordinateRelative = y;
 	}
 
 	public double[] getCoordinatesAbsolute(){
 		return coordinatesAbsolute;
 	}
 	
 	public void setCoordinatesAbsolute(double[] coordinates){
 		coordinatesAbsolute[0] = coordinates[0];
 		coordinatesAbsolute[1] = coordinates[1];
 		panel.setRobotLocation(coordinates[0], coordinates[1], getAngle());
		SSG.updateStatus();		
 	}
 	
 	private void setCurrentTileCoordinatesRelative(double xOld, double yOld) {
 		 int[] relativePosition = setAbsoluteToRelative(xOld, yOld);
 		 setXCoordinateRelative(relativePosition[0]);
 		 setYCoordinateRelative(relativePosition[1]);
 	 }
 	
 	 /**
 	  * Deze methode zet de coordinaten van het echte systeem om in de
 	  * coordinaten van de matrix
 	  */
 	 private int[] setAbsoluteToRelative(double x, double y) {
 		 double a = x - setToMultipleOf40(startPositionAbsoluteX);
 		 double b = y - setToMultipleOf40(startPositionAbsoluteY);
 		 int c;
 		 int d;
 		 c = (int) Math.floor(a / 40);
 		 d = (int) Math.floor(b / 40);
 
 		 int[] array = new int[2];
 		 array[0] = getStartPositionRelativeX() + c;
 		 array[1] = getStartPositionRelativeX() + d;
 		 return array;
 	 }
 	 
 	 private int getStartPositionRelativeX() {
 		return startPositionRelativeX;
 	}
 
 	private int setToMultipleOf40(double a) {
 		 return (int) (Math.floor(a / 40) * 40);
 	 }
 
 	public void resetBuffer() {
 		xCoordinateRelative = 0;
 		yCoordinateRelative = 0;
 		angle = 0;
 	}
 }
 
