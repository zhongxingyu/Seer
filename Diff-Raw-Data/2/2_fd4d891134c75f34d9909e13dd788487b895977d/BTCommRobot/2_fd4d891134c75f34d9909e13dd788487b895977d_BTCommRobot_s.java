 package onRobot;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.IOException;
 
 
 
 import lejos.nxt.Battery;
 import lejos.nxt.LCD;
 import lejos.nxt.LightSensor;
 import lejos.nxt.Motor;
 import lejos.nxt.NXTRegulatedMotor;
 import lejos.nxt.SensorPort;
 import lejos.nxt.Sound;
 import lejos.nxt.TouchSensor;
 import lejos.nxt.UltrasonicSensor;
 import lejos.nxt.comm.BTConnection;
 import lejos.nxt.comm.Bluetooth;
 
 
 public class BTCommRobot implements CMD {
 	private  final float wheelsDiameter = 5.43F;
 	//	private static final float wheelDiameterLeft = 5.43F;
 	//	private static final float wheelDiameterRight = 5.43F;
 	private  final float trackWidth = 16.40F;
 	private int pcSendLength=2;
 	private int robotReplyLength=0;
 	int[] _command = new int[2];
 	int[] _reply = new int[4];
 	boolean _keepItRunning = true;
 	String _connected = "Connected";
 	String _waiting = "Waiting...";
 	String _closing = "Closing...";
 	DataInputStream _dis = null;
 	DataOutputStream _dos = null;
 	BTConnection _btc = null;
 	TouchSensor touchSensor = new TouchSensor(SensorPort.S1);
 	LightSensor lightSensor= new LightSensor(SensorPort.S3);
 	UltrasonicSensor ultrasonicSensor = new UltrasonicSensor(SensorPort.S2);
 	NXTRegulatedMotor sensorMotor = Motor.A;
 	DifferentialPilot pilot = new DifferentialPilot(wheelsDiameter,trackWidth);
 
 	public BTCommRobot(){
 		sensorMotor.resetTachoCount();
 		sensorMotor.setSpeed(100*Battery.getVoltage());
 		LCD.drawString(_waiting,0,0);
 		LCD.refresh();
 
 
 		// Slave waits for Master to connect
 		_btc = Bluetooth.waitForConnection();
 		Sound.twoBeeps();
 
 		LCD.clear();
 		LCD.drawString(_connected,0,0);
 		LCD.refresh();	
 		System.out.println("opening streams");
 
 		// Set up the data input and output streams
 		_dis = _btc.openDataInputStream();
 		_dos = _btc.openDataOutputStream();
 		System.out.println("the streams are open");
 
 		Sound.beepSequenceUp();
 
 		try {
 			
 			startWhileLoop();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 			System.out.println("we were trying to start the connection");
 		}
 
 		// Slave begins disconnect cycle
 
 		//try{Thread.sleep(5000);}
 		//catch(InterruptedException e){
 		//	System.exit(0);
 		//}
 
 		// Close the data input and output streams
 		try {
 			_dis.close();
 			_dos.close();
 			Thread.sleep(100); // wait for data to drain
 			LCD.clear();
 			LCD.drawString(_closing,0,0);
 			LCD.refresh();
 			// Close the bluetooth connection from the Slave's point-of-view
 			_btc.close();
 			LCD.clear();
 		} catch (IOException e) {
 			//this schould not happen
 			e.printStackTrace();
 			System.out.println("we were trying to close the connection");
 
 		}
 		catch(InterruptedException e){
 			System.out.println("The thread was interrupted before we could close the connection");
 		}
 	}
 	
 	private void startBarcodePolling(){
 		while(true){
 			if(detectBlackLine()){
 				int[] barcode = scanBarcode();
 				robotReplyLength = 8;
 				for(int i = 0; i<4; i++){
 					_reply[4+i] = barcode[i];
 				}
 			}
 			
 		}
 	}
 	private void startWhileLoop() throws IOException {
 		while (_keepItRunning)
 		{			
 			handleOneCommand();
 		}// End of while loop
 	}
 
 	private void handleOneCommand() throws IOException {
 		// Fetch the Master's command and argument
 		int command=_dis.readByte();
 		double argument= ((double)_dis.readInt())/100.0;
 		// We set the robotReplyLength to 0 because this is the case for
 		// most of the commands.
 		robotReplyLength = 0;
 		// Respond to the Master's command which is stored in command[0]
 		switch (command) {
 		// Get the battery voltage
 		case STOP:
 			stop();
 			break;
 		case GETPOSE:
 			_reply = getPose();
 			robotReplyLength=4;
 			break;
 		case BATTERY: 
 			_reply[0]=getBattery();
 			robotReplyLength=1;
 			break;
 			// Manual Ping
 		case CALIBRATELSHIGH:
 			calibrateLightSensorHigh();
 			break;
 		case CALIBRATELSLOW:
 			calibrateLightSensorLow();
 			break;
 		case TURNSENSOR: 
 			turnSensor((int)argument);
 			break;
 		case TURNSENSORTO: 
 			turnSensorTo((int)argument);
 			break;	
 			// Manual Ping
 		case GETSENSORVALUES:
 			_reply=getSensorValues();
 			robotReplyLength=4;
 			break;
 
 			// Travel forward requested distance and return sonic sensor distance
 		case TRAVEL: 
 			travel(argument);
 			break;
 			// Rotate requested angle and return sonic sensor distance
 		case TURN: 
 			turn(argument);
 			break;
 			// Master warns of a bluetooth disconnect; set while loop so it stops
 		case KEEPTRAVELLING:
 			keepTraveling(argument>0);
 			break;
 		case KEEPTURNING:
 			keepTurning(argument>0);
 			break;
 		case DISCONNECT:	
 			disconnect();
 			break;
 		case SCANBARCODE:
 			_reply=scanBarcode();
 			robotReplyLength=4;
 			break;
 		case DRIVESLOW:
 			driveSlow();
 			break;
 		case DRIVEFAST:
 			driveFast();
 			break;
 		case PLAYTUNE:
 			playTune();
 			break;
 		case WAIT5:
 			wait5();
 			break;
 		case STRAIGHTEN:
 			straighten();
 			break;
 		case SETTODEFAULTTRAVELSPEED:
 			setToDefaultTravelSpeed();
 			break;
 		case SETTODEFAULTTURNSPEED:
 			setToDefaultTurnSpeed();
 			break;
 		case SETSLOWTRAVELSPEED:
 			setSlowTravelSpeed(argument);
 			break;
 		case SETHIGHTRAVELSPEED:
 			setHighTravelSpeed(argument);
 			break;
 		case SETWHEELDIAMETER:
 			setWheelDiameter(argument);
 			break;
 		case SETTRACKWIDTH:
 			setTrackWidth(argument);
 			break;
 		case SETROTATION:
 			setRotation(argument);
 			break;
 		case SETX:
 			setX(argument);
 			break;
 		case SETY:
 			setY(argument);
 			break;
 		case SETDEFAULTTRAVELSPEED:
 			setDefaultTravelSpeed(argument);
 			break;
 		case SETDEFAULTTURNSPEED:
 			setDefaultTurnSpeed(argument);
 			break;
 		case SETTURNSPEED:
 			setTurnSpeed(argument);
 			break;
 		case SETTRAVELSPEED:
 			setTravelSpeed(argument);
 			break;
 		default:
 			Sound.beep();
 			Sound.beep();
 			Sound.beep();
 			Sound.beep();
 			break;
 		} // End case structure
 
 
 		
 		//LCD.drawInt(reply[0],0,6);
 		_dos.writeByte(robotReplyLength);
 		_dos.flush();
 		for(int k=0 ; k < robotReplyLength ; k++){
 			_dos.writeInt(_reply[k]);
 			_dos.flush();
 		}
 		try{Thread.sleep(20);}
 		catch(InterruptedException e){
 			System.exit(0);
 		}
 		LCD.refresh();
 	}
 
 	public static void main(String [] args) throws Exception 
 	{
 		new BTCommRobot();
 	}
 
 
 	@Override
 	public void travel(double distance) {
 		System.out.println("Robot.travel()");
 		if(!checkingBarcode && distance > 0)
 			travelWithCheck(distance);
 		else
 			pilot.travel(distance);
 	}
 	
 	private boolean checkingBarcode = false;
 	private void travelWithCheck(double distance) {
 		System.out.println("Robot.travel()");
 		Position startpos = pilot.getPosition();
 		double startX = startpos.getX();
 		double startY = startpos.getY();
 		boolean mayMove = true;
 		keepTraveling(true);
 		while(!detectBlackLine() && mayMove){
 			Position newpos = pilot.getPosition();
 			if(distance <= Math.sqrt(Math.pow(newpos.getX() - startX, 2) + Math.pow(newpos.getY() - startY, 2)))
 				mayMove = false;
 		}
 		pilot.stop();
 		if(detectBlackLine()){
 			checkingBarcode = true;
 			int[] barcode = scanBarcode();
 			robotReplyLength = 8;
 			_reply = new int[8];
 			for(int i = 0; i<4; i++){
 				_reply[4+i] = barcode[i];
 			}
 			_reply = new int[4];
 			robotReplyLength = 4;
 			checkingBarcode = false;
 		}
 		mayMove = true;
 	}
 
 
 	@Override
 	public void turn(double angle) {
 		System.out.println("Robot.turn()");
 		pilot.rotate(angle);		
 	}
 
 
 	@Override
 	public void turnSensor(int angle) {
 		System.out.println("Robot.turnSensor()");
 		sensorMotor.rotate(angle);
 	}
 
 
 	@Override
 	public void turnSensorTo(int angle) {
 		System.out.println("Robot.turnSensorTo()");
 		sensorMotor.rotateTo(angle);
 	}
 
 
 	@Override
 	public void stop() {
 		System.out.println("Robot.stop()");
 		pilot.stop();
 		return;
 	}
 
 
 	@Override
 	public int[] getPose() {
 		Position position= pilot.getPosition();
 		return new int[]{(int)position.getX(), (int)position.getY(), (int)pilot.getRotation(), pilot.isMoving()?1:0};
 	}
 
 
 	@Override
 	public int[] getSensorValues() {
 		int[] result = new int[4];
 		result[0] =lightSensor.getLightValue();
 		result[1] =ultrasonicSensor.getDistance();
 		if(touchSensor.isPressed() == true)
 			result[2] =1;
 		else
 			result[2] =-1;
 		result[3] = sensorMotor.getTachoCount();
 		return result;
 	}
 
 	public double readLightValue(){
 		return lightSensor.readValue();
 	}
 
 	public boolean isTouching(){
 		return touchSensor.isPressed();
 	}
 
 	public double readUltraSonicSensor(){
 		return ultrasonicSensor.getDistance();
 	}
 
 	public boolean detectBlackLine() {
 		System.out.println("Robot.detectBlackLine()");
 		//TODO: waarden checken en kalibreren
 		if(readLightValue()<-100){
 			return true;
 		}
 		else return false;
 	}
 
 	public boolean detectWhiteLine() {
 		System.out.println("Robot.detectWhiteLine()");
 		if(readLightValue() > 50) return true;
 		else return false;
 	}
 
 
 	@Override
 	public int getBattery() {
 		System.out.println("Robot.getBattery()");
 		return Battery.getVoltageMilliVolt();
 	}
 
 
 	@Override
 	public void calibrateLightSensorHigh() {
 		System.out.println("Robot.calibrateLightSensorHigh()");
 		lightSensor.calibrateHigh();
 	}
 
 
 	@Override
 	public void calibrateLightSensorLow() {
 		System.out.println("Robot.calibrateLightSensorLow()");
 		lightSensor.calibrateLow();
 	}
 
 
 	@Override
 	public void keepTurning(boolean left) {
 		System.out.println("Robot.keepTurning()");
 		pilot.keepTurning(left);
 	}
 
 
 	@Override
 	public void keepTraveling(boolean forward) {
 		System.out.println("Robot.keepTraveling()");
 		if(forward)
 			pilot.forward();
 		else pilot.backward();
 	}
 
 
 	@Override
 	public void disconnect() {
 		_keepItRunning = false;
 		for(int k = 0; k < 4; k++){
 			_reply[k] = 255;
 		}
 	}
 	
 	public void findBlackLine(){
 		pilot.forward();
 		boolean found = false;
 		while (!found){
 			found = detectBlackLine();
 		}
 		pilot.stop();
 	}
 	
 	@Override
 	public int[] scanBarcode() {
 		System.out.println("ScanBarcode())");
 		
 		if(!detectBlackLine()) {
 			findBlackLine();
 		}
 		setTravelSpeed(2);
 		int[] bits = new int[32];
 		for(int i = 0; i<32; i++){
			travel(0.5);
 			if(detectBlackLine()){
 				bits[i] = 0;
 			}
 			else{
 				bits[i] = 1;
 			}
 		}
 		driveDefault();
 		int[] pose = getPose();
 		Position pos = new Position(pose[0], pose[1]);
 		final int MAZECONSTANT  = 40;
 		int lowx = (int) (Math.floor((pos.getX())/MAZECONSTANT))*MAZECONSTANT;
 		int lowy = (int) (Math.floor((pos.getY())/MAZECONSTANT))*MAZECONSTANT;
 
 		int[] realBits = new int[6];
 		for(int i = 1; i<7; i++){
 			int count1 =0;
 			for(int j= 0; j<4; j++){
 				if(bits[4*i+j] ==1) count1++;
 			}
 			if(count1>2) realBits[i-1] = 1;
 			else realBits[i-1] = 0;
 		}
 
 		int decimal = 0;
 		for(int i = 0; i< 6; i++){
 			decimal = (int) (decimal + realBits[5-i]*Math.pow(2, i));
 		}
 		System.out.println(lowx + 20 +"   " +lowy+20+"     "+decimal+"     "+getPose()[2]);
 		return new int[]{lowx + 20,lowy+20,decimal,getPose()[2]};
 	}
 	
 	@Override
 	public void setDefaultTravelSpeed(double speed){
 		pilot.setDefaultTravelSpeed(speed);
 	}
 	
 	@Override
 	public void setDefaultTurnSpeed(double speed){
 		pilot.setDefaultRotateSpeed(speed);
 	}
 
 	@Override
 	public void driveSlow() {
 		System.out.println("Robot.driveSlow()");
 		pilot.setToSlowTravelSpeed();
 	}
 
 	@Override
 	public void driveFast() {
 		System.out.println("Robot.driveFast()");
 		pilot.setToFastTravelSpeed();
 	}
 
 	public void driveDefault(){
 		System.out.println("Robot.driveDefault()");
 		pilot.setToDefaultTravelSpeed();
 	}
 
 	@Override
 	public void playTune() {
 		try{
 			Sound.playSample(new File("tune.wav"));
 		}
 		catch(Exception e){
 			final short [] note = {
 					2349,115, 0,5, 1760,165, 0,35, 1760,28, 0,13, 1976,23, 
 					0,18, 1760,18, 0,23, 1568,15, 0,25, 1480,103, 0,18, 1175,180, 0,20, 1760,18, 
 					0,23, 1976,20, 0,20, 1760,15, 0,25, 1568,15, 0,25, 2217,98, 0,23, 1760,88, 
 					0,33, 1760,75, 0,5, 1760,20, 0,20, 1760,20, 0,20, 1976,18, 0,23, 1760,18, 
 					0,23, 2217,225, 0,15, 2217,218};
 			for(int i=0;i<note.length; i+=2) {
 				final short w = note[i+1];
 				final int n = note[i];
 				if (n != 0) Sound.playTone(n, w*10);
 				try { Thread.sleep(w*10); } catch (InterruptedException e1) {}
 			}
 		}	
 	}
 
 	@Override
 	public void wait5() {
 		try {
 			Thread.sleep(5000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			System.out.println("Lauren zegt: ziejewel!");
 		}
 	}
 
 	@Override
 	public void setSlowTravelSpeed(double speed) {
 		pilot.setSlowTravelSpeed(speed);
 	}
 
 	@Override
 	public void setHighTravelSpeed(double speed) {
 		pilot.setHighTravelSpeed(speed);
 		
 	}
 
 	@Override
 	public void setWheelDiameter(double diameter) {
 		pilot.setWheelDiameter(diameter);
 	}
 
 	@Override
 	public void setTrackWidth(double trackWidth) {
 		pilot.setTrackWidth(trackWidth);
 	}
 
 	@Override
 	public void setX(double xCo) {
 		pilot.setXCo(xCo);
 	}
 
 	@Override
 	public void setY(double yCo) {
 		pilot.setYCo(yCo);
 		
 	}
 
 	@Override
 	public void setRotation(double rotation) {
 		pilot.setRotation(rotation);
 	}
 
 	@Override
 	public void setToDefaultTravelSpeed() {
 		pilot.setToDefaultTravelSpeed();
 	}
 
 	@Override
 	public void setToDefaultTurnSpeed() {
 		pilot.setToDefaultRotateSpeed();
 	}
 
 	@Override
 	public void setTravelSpeed(double speed) {
 		pilot.setTravelSpeed(speed);
 	}
 
 	@Override
 	public void setTurnSpeed(double speed) {
 		pilot.setRotateSpeed(speed);
 	}
 
 	@Override
 	public void straighten() {
 		pilot.forward();
 		while(!detectWhiteLine()){
 		}
 		//TODO needs to be removed.
 		pilot.travelB(8,false);
 		pilot.setRotateSpeed(50);
 		pilot.keepTurning(true);
 		while(!detectWhiteLine()){
 		}
 		int distance = ultrasonicSensor.getDistance() % 40;
 		pilot.travelB(distance - 17,false);
 		pilot.setRotateSpeed(200);
 		pilot.rotateB(85,false); 
 		
 	}
 	
 //	private static class blackLinePoller extends Thread {
 //    	private BTCommRobot robot;
 //		
 //		public blackLinePoller(BTCommRobot robot) {
 //			this.robot = robot;
 //		}
 //		/**
 //		 * Infinite loop that runs while the thread is active.
 //		 */
 //		public void run(){
 //			try{
 //				while(true){
 //					if(robot.detectBlackLine()){
 //						robot.pilot.setReadingBarcode(true);
 //						robot.stop();
 //						int[] barcode = robot.scanBarcode();
 //						robot.robotReplyLength = 8;
 //						robot._reply = new int[robot.robotReplyLength];
 //						for(int i = 0; i<4; i++){
 //							robot._reply[4+i] = barcode[i];
 //						}
 //						robot.pilot.setReadingBarcode(false);
 //					}
 //					sleep(100);
 //				}
 //			} catch(InterruptedException e){
 //				//Do absolutely nothing
 //			}
 //		}
 //    }
 }
 	
