 package onRobot;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Queue;
 
 
 
 import lejos.nxt.Battery;
 import lejos.nxt.Button;
 import lejos.nxt.LCD;
 import lejos.nxt.LightSensor;
 import lejos.nxt.Motor;
 import lejos.nxt.NXTRegulatedMotor;
 import lejos.nxt.SensorPort;
 import lejos.nxt.Sound;
 import lejos.nxt.TouchSensor;
 import lejos.nxt.UltrasonicSensor;
 import lejos.nxt.addon.IRSeeker;
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
 	int[] _reply;
 	boolean _keepItRunning = true;
 	String _connected = "Connected";
 	String _waiting = "Waiting...";
 	String _closing = "Closing...";
 	DataInputStream _dis = null;
 	DataOutputStream _dos = null;
 	BTConnection _btc = null;
 	final IRSeeker  infraredSensor;
 	final LightSensor lightSensor;
 	final UltrasonicSensor ultrasonicSensor;
 	final NXTRegulatedMotor sensorMotor ;
 	final DifferentialPilot pilot;
 	private SecondThread secondThread;
 	private Queue<int[]> replyAdditionList;
 	private boolean calibratedLightHigh=false;
 	private boolean calibratedLightLow=false;
 	private int typeBeforLastWood=LightType.WOOD;
 	private int lastLightType=LightType.WOOD;
 	private List<Position> barCodePositionList;
 	private boolean fromSecondThread=false;
 
 	public BTCommRobot() {
 		barCodePositionList= new ArrayList<Position>();
 		replyAdditionList= new Queue<int[]>();
 		infraredSensor = new IRSeeker(SensorPort.S1);
 		lightSensor= new LightSensor(SensorPort.S3);
 		ultrasonicSensor = new UltrasonicSensor(SensorPort.S2);
 		pilot = new DifferentialPilot(wheelsDiameter, trackWidth);
 		sensorMotor = Motor.A;
 		sensorMotor.removeListener();
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
 
 		// Set up the data input and output streams
 		_dis = _btc.openDataInputStream();
 		_dos = _btc.openDataOutputStream();
 
 		Sound.beepSequenceUp();
 		secondThread= new SecondThread();
 
 		secondThread.addAction(new PoseUpdateAction(pilot));
 		secondThread.start();
 		Writer.forcedWrite("na secondThread start");	
 		secondThread.addAction(new CheckForBarCodeAction(this));
 
 		Writer.forcedWrite("We will now startCommunication");
 		startCommunication();
 	}
 
 	public static void main(String [] args) throws Exception 
 	{
 		try{
 		Writer.write("We will now make a BtCommRobot");
 		new BTCommRobot();
 		Writer.write("The program has ended normally");
 		Writer.close();
 
 		}catch(Exception e){
 			Sound.buzz();
 			Writer.forcedWrite("There was an exception: " + e.getCause()+", "+e.getClass()+", "+ e.getMessage());
 			Writer.close();
 			LCD.clear();
 			e.printStackTrace(System.out);
 			LCD.refresh();
 			Button.waitForAnyPress();
 		}
 	}
 
 
 	private void startCommunication() {
 		try {	
 			startWhileLoop();
 		} catch (IOException e1) {
 			Writer.write("we were trying to do the while loop, but IOException");
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
 			Writer.write("we were trying to close the connection");
 
 		}
 		catch(InterruptedException e){
 			Writer.write("The thread was interrupted before we could close the connection");
 		}
 	}
 
 //TODO checken of dit nog bruikbaar is of weg moet
 //	private void startBarcodePolling(){
 //		while(true){
 //			if(detectBlackLine()){
 //				int[] barcode = scanBarcode();
 //				robotReplyLength = 8;
 //				for(int i = 0; i<4; i++){
 //					_reply[4+i] = barcode[i];
 //				}
 //			}
 //			
 //		}
 //	}
 
 	private void startWhileLoop() throws IOException {
 		while (_keepItRunning)
 		{			
 			handleOneCommand();
 		}// End of while loop
 	}
 
 	private void handleOneCommand() throws IOException {
 		// Fetch the Master's command and argument
 		int command=_dis.readByte();
 //		Writer.write("we read command " + command);
 
 		double argument= (_dis.readInt())/100.0;
 //		Writer.write("we read argument " + argument);
 
 		// We set the robotReplyLength to 0 because this is the case for
 		// most of the commands.
 		robotReplyLength = 0;
 		_reply=new int[4];
 		// Respond to the Master's command which is stored in command[0]
 //		Writer.write("command received" + command);
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
 			if(_reply!=null){
 			robotReplyLength=4;}
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
 		case AUTOCALIBRATELS:
 			autoCalibrateLightSensor((int)argument);
 			break;
 		case FETCHBALL:
 			fetchBall();
 			break;
 		case SEASAWACTION:
 			seaSawAction();
 			break;
 		default:
 			throw new RuntimeException("A case was not implemented or did not break");
 		} // End case structure
 
 		int numberOfSpecialReplies=replyAdditionList.size();
 		if (numberOfSpecialReplies == 0 && robotReplyLength == 0) {
 			//if there is nothing to send, then we just send 0
 			_dos.writeByte(0);
 			_dos.flush();
 		} else {
 
 			// we will first write how many replies there will be
 			_dos.writeByte(numberOfSpecialReplies + 1);
 			//now we write how long the normal reply will be.
 			_dos.writeByte(robotReplyLength);
 			_dos.flush();
 			//now we write the  normal reply
 			for (int k = 0; k < robotReplyLength; k++) {
 				_dos.writeInt(_reply[k]);
 				_dos.flush();
 			}
 			//now we write the special replies
 			for (int i = 0; i < numberOfSpecialReplies; i++) {
 			Writer.write("start of write specialReply: "+i);
 				int[] specialReply=(int[]) replyAdditionList.pop();
 				//We first write the specialReplyCode
 				_dos.writeByte(specialReply[0]);
 				_dos.flush();
 				for(int j=1;j<specialReply.length;j++){
 					Writer.write("wrote specialReply argument: " +j+": "+ specialReply[j]);
 					_dos.writeInt(specialReply[j]);
 					_dos.flush();
 				}
 			Writer.write("wrote specialReply: "+i);
 
 			}
 //			 Writer.write("command replied " + command);
 			try {
 				Thread.sleep(20);
 			} catch (InterruptedException e) {
 				System.exit(0);
 			}
 		}
 //		LCD.refresh();
 	}
 
 
 
 	@Override
 	public void travel(double distance) {
 		pilot.travelNonBlocking(distance);
 
 	}
 
 //	private boolean justChecked = false;
 //	private boolean checkingBarcode = false;
 //	private void travelWithCheck(double distance) {
 //		Position startpos = pilot.getPosition();
 //		double startX = startpos.getX();
 //		double startY = startpos.getY();
 //		boolean mayMove = true;
 //		keepTraveling(true);
 //		while(!detectBlackLine() && mayMove){
 //			Position newpos = pilot.getPosition();
 //			if(distance <= Math.sqrt(Math.pow(newpos.getX() - startX, 2) + Math.pow(newpos.getY() - startY, 2)))
 //				mayMove = false;
 //		}
 //		stop();
 //		stop();
 //		if(detectBlackLine()){
 //			checkingBarcode = true;
 //			justChecked = true;
 //			int[] barcode = scanBarcode();
 //			robotReplyLength = 8;
 //			_reply = new int[8];
 //			for(int i = 0; i<4; i++){
 //				_reply[4+i] = barcode[i];
 //			}
 //			checkingBarcode = false;
 //		}
 //		mayMove = true;
 //	}
 
 	/**
 	 * 
 	 * @param SpecialReplyCode 
 	 * @param specialReply
 	 * @pre the first int in the array should be the specialReplyCode and thus negative
 	 */
 	public void addSomethingToReply(byte specialReplyCode, int[] specialReply){
 		if(specialReplyCode>=0){
 			Writer.write("the format of the specialReply was incorrect");
 		}
 		int[] completeSpecialReply=new int[specialReply.length+1];
 		completeSpecialReply[0]=specialReplyCode;
 		int i=1;
 		for(int replyPart: specialReply){
 			completeSpecialReply[i++]=replyPart;
 		}
 		replyAdditionList.push(completeSpecialReply);
 	}
 
 	@Override
 	public void turn(double angle) {
 		pilot.rotateNonBlocking(angle);		
 	}
 
 
 	@Override
 	public void turnSensor(int angle) {
 		sensorMotor.rotate(angle);
 	}
 
 
 	@Override
 	public void turnSensorTo(int angle) {
 		//the if is for correction of the pulling cable
 		if(angle<-45)angle-=5;
 		sensorMotor.rotateTo(angle);
 	}
 
 
 	@Override
 	public void stop() {
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
 		result[0] = readLightValue();
 		result[1] =ultrasonicSensor.getDistance();
 		result[2] = readInfraredValue();
 		result[3] = sensorMotor.getTachoCount();
 		return result;
 	}
 
 	public int readLightValue(){
 		return readLight(false);
 	}
 	public int readLightType(){
 		return readLight(true);
 	}
 	
 	public int readInfraredValue(){
 		//TODO parameter van methode is id van sensor ---> ?
 		return infraredSensor.getSensorValue(1);
 	}
 
 	private int readLight(boolean justType) {
 		int readValue=lightSensor.readValue();
 		int thisLightType=LightType.getLightType(readValue);
 		if(thisLightType==LightType.WOOD){
 			typeBeforLastWood=lastLightType;
 		}
 		lastLightType=thisLightType;
 		return justType?thisLightType:readValue;
 	}
 
 	public int readUltraSonicSensor(){
 		int firstRead=ultrasonicSensor.getDistance();
 
 		int lastRead=ultrasonicSensor.getDistance();
 		while(firstRead-lastRead>1||firstRead-lastRead<-1){
 			firstRead=lastRead;
 			lastRead=ultrasonicSensor.getDistance();
 		}
 		return Math.round(lastRead);
 	}
 
 	public boolean detectBlackValue() {
 	return readLightType()==LightType.BLACK;
 
 	}
 
 
 //	public boolean detectWoodToBlack() {
 //		readLightValue();
 //		readLightValue();
 //		int numberOfReadValues=lastLightValues.size();
 //		if(numberOfReadValues<4){
 //			return false;}
 //		boolean detected=true;
 //		int index = numberOfReadValues;
 //		//We first read the two last values
 //		for (index = numberOfReadValues; index >numberOfReadValues-2; index--) {
 //			if(!LightType.isBlack(lastLightValues.get(index))){
 //				detected=false;
 //			}
 //		}
 //		
 //		
 //		if(detected){
 //			//if the two last values were black, then we read the following black values
 //			while (LightType.isBlack(lastLightValues.get(index))) {
 //				index--;
 //			}
 //			//Now we take the first 2 non-black values and check whether they ar both wood
 //			for (int i = index; i > index - 2; i--) {
 //				if (!LightType.isWood(lastLightValues.get(i))) {
 //					detected = false;
 //				}
 //			}
 //		}
 //		lastLightValues.clear();
 //		return detected;
 //		
 //	}
 
 
 
 	public boolean detectWhiteValue() {
 		return readLightType()==LightType.WHITE;
 	}
 
 	public boolean detect2TimesWhite() {
 		return detectWhiteValue()&&detectWhiteValue();
 	}
 	public boolean detect2TimesBlack() {
 		return detectBlackValue()
 				&&detectBlackValue();
 	}
 
 //	public boolean detectWoodToWhite() {
 //		readLightValue();
 //		readLightValue();
 //		int numberOfReadValues=lastLightValues.size();
 //		if(numberOfReadValues<4){
 //			return false;}
 //		boolean detected=true;
 //		int index = numberOfReadValues;
 //		//We first read the two last values
 //		for (index = numberOfReadValues; index >numberOfReadValues-2; index--) {
 //			if(!LightType.isWhite(lastLightValues.get(index))){
 //				detected=false;
 //			}
 //		}
 //		
 //		
 //		if(detected){
 //			//if the two last values were white, then we read the following black values
 //			while (LightType.isWhite(lastLightValues.get(index))) {
 //				index--;
 //			}
 //			//Now we take the first 2 non-white values and check whether they ar both wood
 //			for (int i = index; i > index - 2; i--) {
 //				if (!LightType.isWood(lastLightValues.get(i))) {
 //					detected = false;
 //				}
 //			}
 //		}
 //		lastLightValues.clear();
 //		return detected;
 //		}
 
 
 	@Override
 	public int getBattery() {
 		return Battery.getVoltageMilliVolt();
 	}
 
 
 	@Override
 	public void calibrateLightSensorHigh() {
 		lightSensor.calibrateHigh();
 		calibratedLightHigh=true;
 	}
 
 
 	@Override
 	public void calibrateLightSensorLow() {
 		lightSensor.calibrateLow();
 		calibratedLightLow=true;
 	}
 
 
 	@Override
 	public void keepTurning(boolean left) {
 		pilot.keepTurning(left);
 	}
 
 
 	@Override
 	public void keepTraveling(boolean forward) {
 
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
 
 	private void findBlackLine(){
 		Writer.write("findBlackLine is called");
 		pilot.forward();
 		while (!detect2TimesBlack());
 		pilot.stop();
 		Writer.write("findBlackLine returned");
 	}
 
  
 
 	@Override
 	public int[] scanBarcode() {
 		//return scanBarcodeV1();
 		return scanBarcodeV2(); 
 	}
 	public int[] scanBarcodeV2() {
 		initiateBarcodeReading();
 		boolean[] allBits=new boolean[10000];
 		final int noWantedWoods=(int) (200*(5.0/pilot.getTravelSpeed()));
 		int numberOfBits=0;
 		int dismissed=0;
 		int noWoods=0;
 		final int sampleDivider=4;
 		pilot.forward();
 		
 		while(!detect2TimesBlack()){
 			
 		}
 		while(noWoods<noWantedWoods){
 //			we throw away some of the read values
 
 			int lightType = readLightType();
 			if (lightType== LightType.WOOD) {
 				noWoods++;
 			} else {
 				noWoods = 0;
 			}
 			if (dismissed++ >= sampleDivider) {
 				// This is to lower the sample-rate without slowing down wood
 				// Detection
 				dismissed = 0;
 			} else {
 				
 				allBits[numberOfBits++] = lightType!=LightType.BLACK;
 			}
 		}
 		pilot.stop();
 
 		numberOfBits-=(noWoods/sampleDivider);
 		Writer.write(""+numberOfBits);
 		int[] realBits= new int[]{0,0,0,0,0,0};
 		for(int j=1;j<7;j++){
 			int numberOf1s=0;
 			for(int k= (int) Math.round((numberOfBits*(j+0.25))/8.0);k<Math.round(numberOfBits*(j+0.75)/8.0); k++ ){
 				if(allBits[k]){
 					numberOf1s++;
 				}
 			}
 			Writer.write("NumberOf1s is: "+numberOf1s);
 			if(numberOf1s>numberOfBits/8/8){
 				realBits[j-1]=1;
 			}
 			else{
 				realBits[j-1]=0;
 			}
 		}
 		if(!fromSecondThread){
 		endBarcodeReading();
 		}
 		return makeBarcodeInfo(realBits);
 	}
 
 	public int[] scanBarcodeV1() {
 		initiateBarcodeReading();
 
 		int[] bits = new int[32];
 		int timesTheSame=1;
 		for(int i = 0; i<32; i++){
 			pilot.travelBlocking(0.5);
 			int lightValue=readLightValue();
 			if(lightValue<0){
 				bits[i] = 0;
 			}
 			else{
 				bits[i] = 1;
 			}
 			if(i!=0&&bits[i]==bits[i-1]){
 				timesTheSame++;
 			}
 			else{
 				timesTheSame=1;
 			}
 			Writer.write(timesTheSame+"th: "+ bits[i]+": "+lightValue);
 		}		
 
 		int[] realBits = new int[6];
 		for(int i = 1; i<7; i++){
 			int count1 =0;
 			for(int j= 0; j<4; j++){
 				if(bits[4*i+j] ==1) count1++;
 			}
 			if(count1 == 2) return null;
 			else if(count1>2) realBits[i-1] = 1;
 			else realBits[i-1] = 0;
 
 			Writer.write("realBit "+ (i-1) +": "+realBits[i-1]);
 		}
 
 		//We now make the robot go forward until there is no barcode anymore
 		pilot.forward();
 		while(detectBlackValue());
 		pilot.stop();
 		pilot.travelBlocking(0.5);
 		endBarcodeReading();		
 		return makeBarcodeInfo(realBits);
 	}
 
 	private int[] makeBarcodeInfo(int[] realBits) {
 		int decimal = calculateDecimal(realBits);
 		Writer.write("We are now going to return the barcode: "+ decimal);
 		Position pos = pilot.getPosition();
 		final int MAZECONSTANT  = 40;
 		int lowx = (int) (Math.floor((pos.getX())/MAZECONSTANT))*MAZECONSTANT;
 		int lowy = (int) (Math.floor((pos.getY())/MAZECONSTANT))*MAZECONSTANT;
 		return new int[]{lowx + 20,lowy+20,decimal,(int) pilot.getRotation()};
 	}
 
 	private int calculateDecimal(int[] realBits) {
 		String bitString=new String();
 		for(int bit:realBits){
 		bitString=new String(bitString+bit);
 		}
 		Writer.write(bitString);
 		int decimal = 0;
 		for(int i = 0; i< 6; i++){
 			Writer.write("i: "+i+" d: "+decimal+" b: "+realBits[5-i]+" pw: "+power(2,i)+" pr: "+realBits[5-i]*power(2,i));
 			decimal = (decimal + realBits[5-i]*power(2, i));
 		}
 		return decimal;
 	
 	}
 	private int power(int number, int power){
 		return power(number,number, power);
 	}
 	private int power(int number,int groundNumber, int power) {
 		if(power==0){
 			if(number==0)
 				return 0;
 			else return 1;
 			}
 		else if(power==1){
 			return number;}
 		else return power(number*groundNumber, groundNumber, power-1);
 	}
 
 	private void endBarcodeReading() {
 //		System.out.println(lowx + 20 +"   " +lowy+20+"     "+decimal+"     "+getPose()[2]);
 		setToDefaultTravelSpeed();
 		pilot.setReadingBarcode(false);
 		pilot.enablePoseUpdate();
 	}
 
 
 
 	private void initiateBarcodeReading() {
 		pilot.setReadingBarcode(true);
 		Writer.write("We want to find a barcode");
 		pilot.setToSlowTravelSpeed();
 //		if(!woodToBlackAlreadyDetected&&!detectBlackValue()) {
 ////		Writer.write("We do not detect the black line yet");
 //			findBlackLine();
 //		}
 		if (fromSecondThread) {
 			final int noWantedWoods = (int) (200 * (pilot.getTravelSpeed() / 5));
 			int noWoods = 0;
 			pilot.backward();
 			while (noWoods < noWantedWoods) {
 				if (readLightType() == LightType.WOOD) {
 					noWoods++;
 				} else {
 					noWoods = 0;
 				}
 			}
 			pilot.stop();
 		}
 		pilot.disablePoseUpdate();
 		Sound.beepSequenceUp();
 
 	}
 
 	private boolean lightIsCalibrated() {
 		return calibratedLightHigh&&calibratedLightLow;
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
 		pilot.setToSlowTravelSpeed();
 	}
 
 	@Override
 	public void driveFast() {
 		pilot.setToFastTravelSpeed();
 	}
 
 	public void driveDefault(){
 		pilot.setToDefaultTravelSpeed();
 	}
 
 	@Override
 	public void playTune() {
 		Sound.twoBeeps();
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
 			Writer.write("Lauren zegt: ziejewel!");
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
 		int middleDifference=17;
 		if(pilot.poseUpdateIsDisabled()){
 			//in this case we are either scanning a barcode or straightening already
 			return;
 		}
 		int calcDistance = 0;
 		Writer.write(" straighten" );
 		pilot.forward();
 		stopAtWoodToWhite();
 		pilot.travelBlocking(9);
 		pilot.setRotateSpeed(50);
 		pilot.keepTurning(false);
 		
 		stopAtWoodToWhite(); //TODO checken of gecommentarieerd beter is
 		calcDistance = fixDistance(readUltraSonicSensor())%40;
 		calcDistance = calcDistance - middleDifference;
 		if(calcDistance < -4 || calcDistance > 4){
 			calcDistance = (calcDistance / Math.abs(calcDistance)) * 4;
 		}
 		pilot.travelBlocking(calcDistance);
 		pilot.setToDefaultRotateSpeed();
		pilot.rotateBlocking(-90);
 		snapPoseAfterStraighten();
 
 	}
 
 	private void stopAtWoodToWhite() {
 		//TODO TODO test
 		int noWantedWoods;
 		if(readLightType()!=LightType.WOOD&& readLightType()!=LightType.WOOD){
 			noWantedWoods=(int) (200 * 5.0/(pilot.getTravelSpeed()));
 		}
 		else{
 		noWantedWoods = (int) (100 * 5.0/(pilot.getTravelSpeed() ));
 		}
 		int noWantedWhites=(int) (50 * 5.0/(pilot.getTravelSpeed() ));
 		int noWhites=0;
 		int noWoods = 0;
 		while (noWoods < noWantedWoods) {
 			if (readLightType() == LightType.WOOD) {
 				noWoods++;
 			} else {
 				if(readLightType() != LightType.WOOD){
 				noWoods = 0;}
 			}
 		}
 		while(noWhites<noWantedWhites){
 			if(readLightType()==LightType.WHITE){
 				noWhites++;
 			}
 			else{
 				if(readLightType() != LightType.WHITE){
 				noWhites=0;}
 			}
 		};
 		pilot.stop();
 		//TODO TODO test
 	}
 
 	private int fixDistance(int distanceAfterTurn) {
 		if(distanceAfterTurn<25){
 			return distanceAfterTurn-=2;
 		}
 		return distanceAfterTurn;
 	}
 	private int minInAbs(int i, int j) {
 		if(Math.abs(i)<Math.abs(j)){
 			return i;
 		}
 		else {
 			return j;
 		}
 	}
 
 
 	//Makes the 
 	private void snapPoseAfterStraighten() {
 		pilot.disablePoseUpdate();
 		int newRotation= snapTo(90,pilot.getRotation());
 		if(newRotation%180==0){
 			pilot.setXCo(snapTo(40,pilot.getPosition().getX()));	
 			pilot.setYCo(snapTo(40,20,pilot.getPosition().getY()));				
 
 			}
 		else{
 			pilot.setXCo(snapTo(40,20,pilot.getPosition().getX()));	
 		pilot.setYCo(snapTo(40,pilot.getPosition().getY()));
 		}
 
 		pilot.setRotation(newRotation);
 //		pilot.setPosition(pilot.getPosition().getX());
 		pilot.enablePoseUpdate();
 	}
 
 	private int snapTo(int mod, double notSnapped) {
 		return snapTo(mod, 0, notSnapped);
 	}
 
 	private int snapTo(int mod, int offset, double notSnapped) {
 		boolean positive=notSnapped>=0;
 		notSnapped*=(positive?1:-1);
 
 		int intNotSnapped=(int) notSnapped-offset;
 
 		int snappedNumber=(intNotSnapped/mod)*mod;
 		if(intNotSnapped-snappedNumber> mod/2){
 			snappedNumber+=mod;
 		}
 		return (positive?1:-1)*(snappedNumber+offset);
 	}
 
 	public void checkAndReadBarcode() {
 		if (!lightIsCalibrated() || pilot.poseUpdateIsDisabled()
 				|| pilot.getCurrentMoveType() != MoveType.ENDINGTRAVEL
 				|| !detect2TimesBlack()) {
 			if(lightSensor.readValue()<-150){
 //				Writer.write("CF "+ ((!lightIsCalibrated())?1:0) + "" + (pilot.poseUpdateIsDisabled()?1:0) +"" + ((pilot.getCurrentMoveType() != MoveType.ENDINGTRAVEL)?1:0)+" "+lastLightType+ " "+ detectWoodToBlack() );
 			}
 			// If this is the case, then we are either checking for a barcode,
 			// or straightening, or stopped, or turning\
 //			Writer.write("CF "+ ((!lightIsCalibrated())?1:0) + "" + (pilot.poseUpdateIsDisabled()?1:0) +"" + ((pilot.getCurrentMoveType() != MoveType.ENDINGTRAVEL)?1:0));
 			return;
 		}
 		
 			Writer.write("found barcode in check");
 			Position newBarcodePosition = new Position(snapTo(40, 20, pilot
 					.getPosition().getX()), snapTo(40, 20, pilot.getPosition()
 					.getY()));
 			for (Position otherPosition : barCodePositionList) {
 				if (otherPosition.equals(newBarcodePosition)) {
 					Writer.write("returned because already read");
 					return;
 
 				}
 			}
 			fromSecondThread=true;
 			pilot.setReadingBarcode(true);
 			pilot.disablePoseUpdate();
 			pilot.stop();
 			int[] barcode = scanBarcode();
 			barCodePositionList.add(newBarcodePosition);
 //			if (barcode == null) {
 //				travel(-40);
 //				straighten();
 //				findBlackLine();
 //				checkAndReadBarcode();
 //			} else {
 				
 			addSomethingToReply(SpecialReplyCode.ADDBARCODE, barcode);
 				//We wait until the the special reply is sent
 			while(!replyAdditionList.isEmpty());
 				endBarcodeReading();
 //			}
 			
 				
 			fromSecondThread=false;
 	}
 	@Override
 	public void autoCalibrateLightSensor(int difference) {
 		autoCalibrateLightSensor(difference, 0, 0);
 	}
 	public void autoCalibrateLightSensor(int difference, int numberOfTry, int extraTravelBackDistance) {
 		if (numberOfTry < 3) {
 			lightSensor.calibrateLow();
 			Position firstPosition = pilot.getPosition();
 			pilot.forward();
 			while (readLightValue() < difference) {
 			}
 			pilot.stop();
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			lightSensor.calibrateHigh();
 			try {
 				Thread.sleep(10);
 			} catch (InterruptedException e) {
 				Writer.write("autoCalibrate was interrupted");
 			}
 			pilot.travelNonBlocking(-(firstPosition.getDistance(pilot
 					.getPosition()) + extraTravelBackDistance));
 			boolean shouldCalibrateAgain = false;
 			while (pilot.isMoving()) {
 				if (detectBlackValue() && detectBlackValue()) {
 					shouldCalibrateAgain = true;
 					stop();
 					break;
 				}
 			}
 			int dismissedDistance = (int) firstPosition.getDistance(pilot
 					.getPosition());
 			if (shouldCalibrateAgain) {
 				autoCalibrateLightSensor(difference, ++numberOfTry,
 						dismissedDistance);
 			}
 			calibratedLightLow=true;
 			calibratedLightHigh=true;
 		}
 
 	}
 
 	public void checkTouching() {
 
 	}
 
 	@Override
 	public void fetchBall() {
 		straighten();
 		pilot.travelBlocking(30);
 		pilot.travelBlocking(-45);
 			
 	}
 
 	@Override
 	public void seaSawAction() {
 		pilot.travelNonBlocking(120);
 		//TODO values of infrared needs to be taken into consideration!
 		
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
