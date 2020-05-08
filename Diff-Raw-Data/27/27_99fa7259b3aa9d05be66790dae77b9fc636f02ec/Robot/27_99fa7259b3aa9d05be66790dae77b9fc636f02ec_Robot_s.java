 
 package onRobot;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.IOException;
 
 import javax.microedition.lcdui.Command;
 
 
 
 import lejos.nxt.Battery;
 import lejos.nxt.LCD;
 import lejos.nxt.LightSensor;
 import lejos.nxt.Motor;
 import lejos.nxt.NXTRegulatedMotor;
 import lejos.nxt.SensorPort;
 import lejos.nxt.Sound;
 import lejos.nxt.TachoMotorPort;
 import lejos.nxt.TouchSensor;
 import lejos.nxt.UltrasonicSensor;
 import lejos.nxt.comm.BTConnection;
 import lejos.nxt.comm.Bluetooth;
 
 
 public class Robot implements CMD {
 	private  final float wheelsDiameter = 5.43F;
 	//	private static final float wheelDiameterLeft = 5.43F;
 	//	private static final float wheelDiameterRight = 5.43F;
 	private  final float trackWidth = 16.62F;
 	private int pcSendLength=2;
 	private int robotReplyLength=4;
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
 
 	public Robot(){
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
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
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
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch(InterruptedException e){
 			System.out.println("The thread was interrupted before we could close the connection");
 		}
 	}
 
 	private void startWhileLoop() throws IOException {
 		while (_keepItRunning)
 		{			
 
 			// Fetch the Master's command
 			//We use 3 commands for extra info needed for commands like turn.
 			for(int k = 0; k<pcSendLength; k++){
 				_command[k] = _dis.readInt();
 			}
 
 						LCD.clear();
 			//			LCD.drawInt(_command[0], 0, 0);
 			//			LCD.drawInt(_command[1], 0, 2);
 			//			LCD.drawInt(_command[2], 0, 4);
 			//			LCD.refresh();
 
 			// Respond to the Master's command which is stored in command[0]
 			switch(_command[0]){
 			// Get the battery voltage
 			case STOP:
 				stop();
 				break;
 			case GETPOSE:
 				_reply = getPose();
 				break;
 			case BATTERY: 
 				_reply[0]=getBattery();
 				break;
 				// Manual Ping
 			case CALIBRATELSHIGH:
 				calibrateLightSensorHigh();
 				break;
 			case CALIBRATELSLOW:
 				calibrateLightSensorLow();
 				break;
 			case TURNSENSOR: 
 				turnSensor(_command[1]);
 				break;
 			case TURNSENSORTO: 
 				turnSensorTo(_command[1]);
 				break;	
 				// Manual Ping
 			case GETSENSORVALUES:
 				_reply=getSensorValues();
 				break;
 
 				// Travel forward requested distance and return sonic sensor distance
 			case TRAVEL: 
 				travel(_command[1]);
 				break;
 				// Rotate requested angle and return sonic sensor distance
 			case TURN: 
 				turn(_command[1]);
 				break;
 				// Master warns of a bluetooth disconnect; set while loop so it stops
 			case KEEPTRAVELLING:
 				keepTraveling(_command[1]>0);
 				break;
 			case KEEPTURNING:
 				keepTurning(_command[1]>0);
 				break;
 			case DISCONNECT:	
 				disconnect();
 				break;
 			case SCANBARCODE:
				_dos.writeInt(0);
				_dos.writeInt(getPose()[0]);
				_dos.writeInt(getPose()[1]);
				_dos.flush();
				if(_dis.readInt() == 0){
					scanBarcode();
				}
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
 			case SETTRAVELSPEED:
 				setTravelSpeed(_command[1]);
 				break;
 			case SETTURNSPEED:
 				setTurnSpeed(_command[1]);
 				break;
 
 
 			} // End case structure
 
 
 
 			//LCD.drawInt(reply[0],0,6);
 			for(int k=0 ; k < robotReplyLength ; k++){
 				_dos.writeInt(_reply[k]);
 				_dos.flush();
 			}
 			try{Thread.sleep(20);}
 			catch(InterruptedException e){
 				System.exit(0);
 			}
 			LCD.refresh();
 		}// End of while loop
 	}
 
 	public static void main(String [] args) throws Exception 
 	{
 		new Robot();
 	}
 
 
 	@Override
 	public void travel(int distance) {
 		System.out.println("Robot.travel()");
 		pilot.travel(distance);
 	}
 
 
 	@Override
 	public void turn(int angle) {
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
 	public int[] stop() {
 		System.out.println("Robot.stop()");
 		pilot.stop();
 		return getPose();
 	}
 
 
 	@Override
 	public int[] getPose() {
 		System.out.println("Robot.getPose()");
 		Position position= pilot.getPosition();
 
 		return new int[]{(int)position.getX(), (int)position.getY(), (int)pilot.getRotation(), pilot.isMoving()?1:0};
 	}
 
 
 	@Override
 	public int[] getSensorValues() {
 		System.out.println("Robot.getSensorValues()");
 		// TODO Auto-generated method stub
 		return _reply;
 	}
 
 	public double readLightValue(){
 		System.out.println("Robot.readLightValue()");
 		return lightSensor.readValue();
 	}
 
 	public boolean isTouching(){
 		System.out.println("Robot.isTouching()");
 		return touchSensor.isPressed();
 	}
 
 	public double readUltraSonicSensor(){
 		System.out.println("Robot.readUltraSonicSensor()");
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
 
 	@Override
	public void scanBarcode() throws IOException {
 		int[] bits = new int[32];
 		travel(16);
 		for(int i = 0; i<32; i++){
 			if(detectBlackLine()){
 				bits[i] = 0;
 			}
 			else{
 				bits[i] = 1;
 			}
 			int sleepTime= Math.abs((int) Math.round((1000* 0.5/pilot.getTravelSpeed())));
 			try {
 				Thread.sleep(sleepTime);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
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
 
		_dos.writeInt(1);
		_dos.writeInt(lowx + 20);
		_dos.writeInt(lowy+20);
		_dos.writeInt(decimal);
		_dos.writeInt(getPose()[2]);
		_dos.flush();
 	}
 	
 	@Override
 	public void setTravelSpeed(int speed){
 		pilot.setTravelSpeed(speed);
 	}
 	
 	@Override
 	public void setTurnSpeed(int speed){
 		pilot.setRotateSpeed(speed);
 	}
 
 	@Override
 	public void driveSlow() {
 		System.out.println("Robot.driveSlow()");
 		pilot.setTravelSpeed(5);
 	}
 
 	@Override
 	public void driveFast() {
 		System.out.println("Robot.driveFast()");
 		pilot.setTravelSpeed(30);
 	}
 
 	public void driveDefault(){
 		System.out.println("Robot.driveDefault()");
 		pilot.setTravelSpeed(15);
 	}
 
 	@Override
 	public void playTune() {
 //		final short [] note = {
 //				2349,115, 0,5, 1760,165, 0,35, 1760,28, 0,13, 1976,23, 
 //				0,18, 1760,18, 0,23, 1568,15, 0,25, 1480,103, 0,18, 1175,180, 0,20, 1760,18, 
 //				0,23, 1976,20, 0,20, 1760,15, 0,25, 1568,15, 0,25, 2217,98, 0,23, 1760,88, 
 //				0,33, 1760,75, 0,5, 1760,20, 0,20, 1760,20, 0,20, 1976,18, 0,23, 1760,18, 
 //				0,23, 2217,225, 0,15, 2217,218};
 //		for(int i=0;i<note.length; i+=2) {
 //			final short w = note[i+1];
 //			final int n = note[i];
 //			if (n != 0) Sound.playTone(n, w*10);
 //			try { Thread.sleep(w*10); } catch (InterruptedException e) {}
 //		}
 		Sound.playSample(new File("tune.wav"));
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
 	public void straighten() {
 		pilot.forward();
 		while(!detectWhiteLine()){
 		}
 		pilot.stop();
 		pilot.keepTurning(true);
 		while(!detectWhiteLine()){
 		}
 		pilot.stop();
 		pilot.rotate(90); //TODO moet nog aangepast worden
 
 	}
 
 
 }
