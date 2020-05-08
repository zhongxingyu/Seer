 package robot;
 
 import barcode.Barcode;
 import barcode.BarcodeParser;
 import barcode.BarcodeType;
 
 import communication.Buffer;
 import communication.PilotController;
 
 import ultrasonicsensor.UltrasonicSensor;
 import lejos.nxt.*;
 import lightsensor.Color;
 import touchsensor.TouchSensor;
 import lightsensor.*;
 import lightsensor.LightSensor;
 
 /**
  * @author  Samuel
  */
 public class Robot {
 		// Volgens verslag: 54.3, 54.9, 129.8 // 127.93
 		private static final robot.DifferentialPilot PILOT = new robot.DifferentialPilot(54.55, 54.7, 128.7, Motor.B, Motor.C, false);
 		private static Robot instance = new Robot();
 		private boolean isScanning;
 		private boolean isCentering;
 		private LightSensor lightSensor = LightSensor.getInstance();
 		private TouchSensor touchSensor = TouchSensor.getInstance();
 		private boolean hasBall;
 		private int objectNr;
 		private int teamNr;
 		
 		public static final int DEFAULT_TRAVEL_SPEED = 170;
 		public static final int DEFAULT_ROTATE_SPEED = 100;
 		public static final int DEFAULT_ACCELERATION = 500;
 		
 		public static double speed;
 
 		private Robot() {
 			PILOT.setTravelSpeed(DEFAULT_TRAVEL_SPEED);
 			PILOT.setRotateSpeed(DEFAULT_ROTATE_SPEED);
 			PILOT.setAcceleration(DEFAULT_ACCELERATION);
 			speed = DEFAULT_TRAVEL_SPEED;
 			teamNr = -1;
 		}
 
 		public static Robot getInstance(){
 			return instance;
 		}
 		
 		public static double getSpeed(){
 			return speed;
 		}
 			
 		public void forward(){
 			PILOT.forward();
 		}
 		
 		public void backward(){
 			PILOT.backward();
 		}
 
 		public void rotateLeft() {
 			PILOT.rotateLeft();
 		}
 		
 		public void rotateRight() {
 			PILOT.rotateRight();
 		}
 				
 		public void rotateLeft(double angle, boolean immediateReturn){
 			PILOT.rotate(angle, immediateReturn);
 		}
 		
 		public void rotateRight(double angle, boolean immediateReturn){
 			PILOT.rotate(-angle, immediateReturn);
 		}
 		
 		public void travel(double distance, boolean immediateReturn){
 			PILOT.travel(distance, immediateReturn);
 		}
 		
 		public void stop(){
 			PILOT.stop();
 		}
 		
 		public void setTravelSpeed(double travelSpeedInUnitsPerSecond){
 			PILOT.setTravelSpeed(travelSpeedInUnitsPerSecond);
 		}
 		
 		public void setRotateSpeed(double rotateSpeedInDegreesPerSecond){
 			PILOT.setRotateSpeed(rotateSpeedInDegreesPerSecond);
 		}
 		
 		public boolean isMoving(){
 			return PILOT.isMoving();
 		}
 		
 		public float getMovementIncrement(){
 			return PILOT.getMovementIncrement();
 		}
 		
 		public float getAngleIncrement(){
 			return PILOT.getAngleIncrement();
 		}
 		
 		public double getTravelSpeed() {
 			return PILOT.getTravelSpeed();
 		}
 		
 		public void resetMovementAngleIncrement() {
 			PILOT.reset();
 		}
 		
 		public void calibrateLS() {
 			lightSensor.calibrateLightSensor();
 		}
 		
 		public LightSensor getLightSensor(){
 			return lightSensor;
 		}
 		
 		private void orientScan(){
 			double[] distances = UltrasonicSensor.getInstance().roundScanRobotTurns(20);
 			double max = -1;
 			int direction = -1;
 			for(int i = 0; i < distances.length; i++){
 				double sum = distances[(i-1+distances.length) % distances.length]+distances[i]+distances[(i+1)%distances.length];
 				if(sum > max){
 						max = sum;
 						direction =i;
 				}
 			}
 			Robot.getInstance().rotateLeft(360/20*direction, false);
 		}
 		
 		private void setCentering(boolean center){
 			isCentering = center;
 		}
 		
 		public boolean isCentering(){
 			return isCentering;
 		}
 		
 		public void ultimateCentering(boolean bool){
 			if(bool){
 				Motor.A.rotate(100);
 				PilotController.stopStream();
 			}
 			else{
 //			double left = UltrasonicSensor.getInstance().readValue()+4;
 //			Button.waitForAnyPress(100);
 //			Motor.A.rotate(200);
 //			double right = UltrasonicSensor.getInstance().readValue()-4;
 //			Button.waitForAnyPress(100);
 //			if(left<=20 || right<=20){
 //				if(left<right){
 //					rotateLeft(20, false);
 //					travel(-20, false);
 //					rotateLeft(70,false);
 //					travel(300,false);
 //					if(hasBall)
 //						travel(-100,false);
 //					else
 //					Robot.getInstance().travel(-120, false);
 //					rotateRight(90, false);
 //				}
 //				else{
 //					rotateRight(20, false);
 //					travel(-20, false);
 //					rotateRight(70,false);
 //					travel(300,false);
 //					if(hasBall)
 //						travel(-100,false);
 //					else
 //					Robot.getInstance().travel(-120, false);
 //					rotateLeft(90, false);
 //				}
 //			}
 				Motor.A.rotate(-100);
 				PilotController.startStream();
 			}
 		}
 		
 		public void orientOnWhiteLine(boolean scan) {
 			LightSensorVigilante.pause();
 			setNoLines(true);
 			if(scan){
 				orientScan();
 			}
 
 			double previousTravelSpeed = PILOT.getTravelSpeed();
 			double previousRotateSpeed = PILOT.getRotateSpeed();
 			
 			
 			Robot.getInstance().forward();
 			int buffer = 0;
 			
 			//Buffer.addDebug("Begin brown");
 			while (buffer<10) {
 				Button.waitForAnyPress(5);
 				//Buffer.addDebug(lightSensor.getLastColor().toString());
 				//Buffer.addDebug("buffer size: "+buffer);
 
 				if (Color.getColor(lightSensor.readValue()) == Color.BROWN)
 						buffer++;
 				else 
 					buffer = 0;
 			}
 			
 			buffer = 0;
 			
 			//Buffer.addDebug("Begin white");
 			while (buffer<5) {
 				Button.waitForAnyPress(5);
 				if (Color.getColor(lightSensor.readValue()) == Color.WHITE)
 						buffer++;
 				else if (Color.getColor(lightSensor.readValue()) == Color.BROWN){
 					if (buffer > 0) {
 						buffer--;
 					}
 //					buffer = 0;
 				}
 //				if(touchSensor.isPressed()){
 //					PILOT.travel(-30);
 //					orientScan();
 //					Robot.getInstance().forward();
 //				}	
 			}
 			
 			buffer = 0;
 			
 			//Buffer.addDebug("Begin brown");
 			while (buffer<20) {
 				//Buffer.addDebug(lightSensor.getLastColor().toString());
 				//Buffer.addDebug("buffer size: "+buffer);
 				Button.waitForAnyPress(5);
 
 				if (Color.getColor(lightSensor.readValueWithoutAdd()) == Color.BROWN)
 						buffer++;
 				else 
 					buffer = 0;
 			}
 
 			Robot.getInstance().stop();
 			Robot.getInstance().travel(-45, false);
 			buffer = 0;
 			PilotController.stopStream();
 			
 			PILOT.reset();
 			PILOT.rotateRight();
 			buffer = 0;
 			//Buffer.addDebug("Begin white 2");
 			while (buffer<10) {
 				Button.waitForAnyPress(5);
 				//Buffer.addDebug(lightSensor.getLastColor().toString());
 				//Buffer.addDebug("buffer size: "+buffer);
 
 				if (Color.getColor(lightSensor.readValue()) == Color.WHITE)
 						buffer++;
 				else
 					buffer = 0;
 			}
 			buffer = 0;
 			
 			PILOT.stop();
 			PILOT.reset();
 			PILOT.rotate(10);
 			PILOT.rotateLeft();
 			
 			//Buffer.addDebug("Begin brown 2");
 			while (buffer<10) {
 				Button.waitForAnyPress(5);
 				//Buffer.addDebug(lightSensor.getLastColor().toString());
 				//Buffer.addDebug("buffer size: "+buffer);
 
 
 				if (Color.getColor(lightSensor.readValue()) == Color.BROWN)
 						buffer++;
 				else buffer = 0;
 			}
 			buffer = 0;
 			
 			//Buffer.addDebug("Begin white 3");
 			while (buffer<5) {
 				Button.waitForAnyPress(5);
 				//Buffer.addDebug(lightSensor.getLastColor().toString());
 				//Buffer.addDebug("buffer size: "+buffer);
 
 
 				if (Color.getColor(lightSensor.readValue()) == Color.WHITE)
 						buffer++;
 				else if(buffer>0)
 					buffer--;
 			}
 			PILOT.stop();
 			double arc2 = PILOT.getAngleIncrement();
 			PILOT.reset();
 			PILOT.rotate(-((arc2+10)/ 2));
 			
 			Robot.getInstance().backward();
 			buffer = 0;
 			
 			//Buffer.addDebug("Begin white 4");
			while (buffer<10) {
 				Button.waitForAnyPress(5);
 				//Buffer.addDebug(lightSensor.getLastColor().toString());
 				//Buffer.addDebug("buffer size: "+buffer);
 
 
 				if (Color.getColor(lightSensor.readValue()) == Color.WHITE)
 						buffer++;
 				else 
 					buffer = 0;
 			}
 			Robot.getInstance().stop();
 			Robot.getInstance().travel(40, false);
 			Robot.getInstance().stop();
 			setNoLines(false);
 			Buffer.addLightUpdate(LightSensorUpdate.LINE.ordinal());
 			
 			PILOT.setRotateSpeed(previousRotateSpeed);
 			PILOT.setTravelSpeed(previousTravelSpeed);
 			PilotController.startStream();
 			LightSensorVigilante.resume();
 		}
 		
 		
 		public double[] newTileScan() {
 			return UltrasonicSensor.getInstance().newTileScan();
 		}
 		
 		public double[] checkScan() {
 			return UltrasonicSensor.getInstance().checkScan();
 		}
 
 		public double[] scanForWalls(){
 			double[] distances = new double[4];
 			for(int i=0; i<4; i++){
 				try {
 					Thread.sleep(300);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				double v1 = UltrasonicSensor.getInstance().getLastVal();
 				try {
 					Thread.sleep(200);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				double v2 = UltrasonicSensor.getInstance().getLastVal();
 				double max = Math.max(v1, v2);
 				double min = Math.min(v1, v2);
 				distances[i] = max * .7 + min * .3;
 				if(i!=3){
 //					if(i<2)
 //					Motor.A.rotate(-100);
 					rotateLeft(90, false);
 				}
 //				if(i==2)
 //					Motor.A.rotate(300);
 //				else if(i==3)
 //					Motor.A.rotate(-100);
 			}
 			rotateLeft(90, false);
 			for (int i = 0; i < 4; i++) {
 				Buffer.addDistance((int)distances[i]);
 			}
 			return distances;
 			//return UltrasonicSensor.getInstance().getDistancesAroundRobot();
 //			if(getGrid() == null){
 //				return determineGrid();
 //			}
 //			else{
 //				return defaultScanForWalls();
 //			}
 		}
 		
 		public void arc(double radius, double angle, boolean immediateReturn){
 			PILOT.arc(radius, angle, immediateReturn);
 		}
 		/**
 		 * result[0] = ahead
 		 * result[1] = left
 		 * result[2]= behind
 		 * result[3]= right
 		 */
 		public void setOnCenterTileAfterSeesaw(boolean leftFlag) {
 			PilotController.setStopWhiteLineStream(true);
 			Buffer.addDebug("post stopwhitelinestream");
 			PilotController.stopStream();
 			Buffer.addDebug("post stopstream");
 			LightSensorVigilante.pause();
 			Buffer.addDebug("post vigilantepauze");
 			PILOT.reset();
 			Buffer.addDebug("post reset");
 			Button.waitForAnyPress(100);
 			Motor.A.rotate(-100);
 			double left = UltrasonicSensor.getInstance().readValue()+4;
 			Button.waitForAnyPress(100);
 			/*Motor.A.rotate(100);
 			double front = UltrasonicSensor.getInstance().readValue();
 			Button.waitForAnyPress(100);*/
 			Motor.A.rotate(200);
 			double right = UltrasonicSensor.getInstance().readValue()-4;
 			Button.waitForAnyPress(100);
 			Buffer.addDebug("values l: " + left + " r: " + right);
 
 			Robot.getInstance().setCentering(true);
 			Buffer.addDebug("centering set to true");
 			if (left >= right){
 				Buffer.addDebug("right wall");
 				Robot.getInstance().rotateRight(30, false);
 				Robot.getInstance().travel(-30, false);
 				Robot.getInstance().rotateRight(60, false);
 				Robot.getInstance().travel(400, false);
 				if(hasBall)
 					travel(-100,false);
 				else
 				Robot.getInstance().travel(-120, false);
 				Robot.getInstance().rotateLeft(90, false);
 			} else if (right > left){
 				Buffer.addDebug("left wall");
 				Robot.getInstance().rotateLeft(30, false);
 				Robot.getInstance().travel(-30, false);
 				Robot.getInstance().rotateLeft(60, false);
 				Robot.getInstance().travel(400, false);
 				if(hasBall)
 					travel(-100,false);
 				else
 				Robot.getInstance().travel(-120, false);
 				Robot.getInstance().rotateRight(90, false);
 			} else {
 				/*Buffer.addDebug("no walls");
 				if (front < 20) {
 					Buffer.addDebug("front wall");
 					Robot.getInstance().travel(500, false);
 					Robot.getInstance().travel(-130, false);
 					Robot.getInstance().rotateLeft(90, false);
 				} else {
 					Buffer.addDebug("no front wall");
 					if (leftFlag)
 						Robot.getInstance().rotateLeft(90, false);
 					else
 						Robot.getInstance().rotateRight(90, false);
 				}
 				Robot.getInstance().orientOnWhiteLine(false);
 				PilotController.stopStream();
 				Robot.getInstance().travel(-200, false);
 				if (front < 20) {
 					Robot.getInstance().rotateRight(90, false);
 				} else {
 					if (leftFlag) 
 						Robot.getInstance().rotateRight(90, false);
 					else
 						Robot.getInstance().rotateLeft(90, false);
 				}*/
 			}
 			Motor.A.rotate(-100);
 			isCentering = false;
 			Robot.getInstance().setCentering(false);
 			Buffer.addDebug("post set centering false");
 			Robot.getInstance().orientOnWhiteLine(false);
 			Buffer.addDebug("post orient on white line");
 			PilotController.stopStream();
 			Robot.getInstance().travel(200, false);
 			PILOT.reset();
 			PilotController.startStream();
 			PilotController.setStopWhiteLineStream(false);
 
 			Buffer.addDebug("post whitelinestream");
 			LightSensorVigilante.resume();
 			Buffer.addDebug("post lightsensorresume" + LightSensorVigilante.getPaused());
 		}
 		
 		public void setOnCenterTile(){
 			Robot.getInstance().setCentering(true);
 			orientOnWhiteLine(true);
 			this.travel(-200, false);
 			//double distances[] = UltrasonicSensor.getInstance().newTileScan();
 			double distances[] = UltrasonicSensor.getInstance().roundScanRobotTurns(4);
 			boolean wallInSomeDirection = false;
 			int direction = -1;
 			int maxi = -1;
 			double maxDistance = -1;
 			for(int i=1; i<distances.length; i++){
 				System.out.println("distance i: "+distances[i]);
 				if(i != 2) {
 					if(distances[i]<20){
 						if(wallInSomeDirection){
 							if(distances[direction] > distances[i]){
 								direction = i;
 							}
 						}
 						else{
 							wallInSomeDirection = true;
 							direction = i;
 						}
 					}
 					if(distances[i]> maxDistance){
 						maxi = i;
 						maxDistance = distances[i];
 					}
 				}
 			}
 			if(wallInSomeDirection){ // walls in current Tile
 				Robot.getInstance().rotateLeft(360/4*direction,false);
 				Robot.getInstance().forward();
 				while(!touchSensor.isPressed()){System.out.println("waiting to touch wall.");};
 				System.out.println("isTouched!");
 				Robot.getInstance().stop();
 				System.out.println("stopped.");
 				Robot.getInstance().travel(-100, false);
 				System.out.println("end of setCenter before turns");
 				Robot.getInstance().rotateLeft(-360/4*direction,false);
 				System.out.println("The End 1");
 			}
 			else{ // no walls in current Tile
 				Robot.getInstance().rotateLeft(360/4*maxi,false);
 				System.out.println("noWallsOnCurrTile --> orientWhiteLine");
 				Robot.getInstance().orientOnWhiteLine(false);
 				System.out.println("endOfOrient in center method");
 				Robot.getInstance().travel(-200, false);
 				Robot.getInstance().rotateLeft(-360/4*maxi,false);
 				System.out.println("The End 2");
 			}
 			Robot.getInstance().setCentering(false);
 		}
 
 		public void sendAndExecuteBarcode(Barcode barcode) {
 //			barcode.execute();
 			sendBarcode(barcode);
 			if(barcode.getBarcodeType().equals(BarcodeType.OBJECT)){
 				if(barcode.getObjectNr() == Robot.getInstance().getObjectNr()){
 					Robot.getInstance().setTeamNr(barcode.getTeamNr());	
 					Buffer.addDebug("Teamnr Sent: "+barcode.getTeamNr());
 				}
 					
 			}
 		}
 		
 		public void sendBarcode(Barcode barcode) {
 			Buffer.addBarcode(barcode.getCode());
 			Buffer.addBarcodeType(barcode.getBarcodeType().toString());
 		}
 
 		public boolean isScanning() {
 			return isScanning;
 		}
 
 		public void setScanning(boolean isScanning) {
 			this.isScanning = isScanning;
 		}
 
 		public void scanOnlyLines(boolean flag) {
 			if(flag == true)
 				LightSensorVigilante.scanOnlyLines();
 			else
 				LightSensorVigilante.scanBarcodesAndLines();
 		}
 	
 		private boolean noLines;
 
 		public boolean isNoLines() {
 			return noLines;
 		}
 
 		public void setNoLines(boolean noLines) {
 			this.noLines = noLines;
 		}
 
 		public void setObjectNr(double objectNr) {
 			Buffer.addDebug("ObjectNr received: "+objectNr);
 			this.objectNr = (int) objectNr;
 		}
 		
 		public int getObjectNr(){
 			return objectNr;
 		}
 
 		public boolean hasBall() {
 			return hasBall;
 		}
 
 		public void setHasBall() {
 			Buffer.addDebug("Robot has found object");
 			this.hasBall = true;
 			Buffer.setHasBall();
 		}
 		
 		public void setTeamNr(int i){
 			Buffer.addDebug("TeamNr calculated: "+i);
 			teamNr = i;
 		}
 		
 		public int getTeamNr(){
 			return teamNr;
 		}
 
 		public void turnBarcode() {
 			Buffer.addDebug("presetturningonbarcode");
 			System.out.println("presetturningonbarcode");
 			LightSensorVigilante.pause();
 			LightSensorVigilante.setTurningOnBarcode(true);
 			Buffer.addDebug("postsetturningonbarcode");
 			System.out.println("postsetturningonbarcode");
 			Button.waitForAnyPress(100);
 			Robot.getInstance().rotateLeft(180, false);
 			LightSensorVigilante.resume();
 			Buffer.addDebug("post180");
 			System.out.println("post180");
 			Robot.getInstance().forward();
 			Buffer.addDebug("forw");
 			System.out.println("forw");
 			while(!cond){
 				Buffer.addDebug("while cond");
 				System.out.println("while cond");
 			}
 			Robot.getInstance().setOnBrownAfterBarcode(false);
 			Buffer.addDebug("stop");
 			System.out.println("stop");
 			Robot.getInstance().stop();
 			LightSensorVigilante.setTurningOnBarcode(false);
 			Buffer.addDebug("postturningonbarcodefalse");
 			System.out.println("postturningonbarcodefalse");
 			Buffer.addDebug("posttravel");
 			System.out.println("posttravel");
 		}
 
 		public void setOnBrownAfterBarcode(boolean b) {
 			cond = b;
 		}
 		
 		private boolean cond = false;
 }
