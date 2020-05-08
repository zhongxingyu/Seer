 
 package simulator;
 
 import gui.SilverSurferGUI;
 import mapping.*;
 import java.io.File;
 import java.util.Random;
 public class SimulationPilot {
 
 	/**
 	 * verandert wanneer een nieuwe map wordt ingeladen naar de positie waar het
 	 * pijltje staat wanneer de map ingeladen wordt
 	 */
 	private double startPositionAbsoluteX = 220;
 	private double startPositionAbsoluteY = 220;
 	/**
 	 * coordinaat in het echte assenstelsel van de robot
 	 */
 	private double currentPositionAbsoluteX = 220;
 	private double currentPositionAbsoluteY = 220;
 
 	private double alpha = 270;
 	private int speed = 10;
 	private SilverSurferGUI SSG = new SilverSurferGUI();
 	private File mapFile;
 	private MapGraph mapGraph;
 	private boolean isRealRobot = false;
 	
 	/**
 	 * waarde die afhangt van de robot!
 	 */
 	private final double detectionDistanceUltrasonicSensorRobot = 32;
 
 	public SimulationPilot() {
 		SSG.getSimulationPanel().setRobotLocation(
 				this.getCurrentPositionAbsoluteX(),
 				this.getCurrentPositionAbsoluteY(), this.getAlpha());
 	}
 
 	public SimulationPilot(int startPositionRelativeX,
 			int startPositionRelativeY) {
 		SSG.getSimulationPanel().setRobotLocation(
 				this.getCurrentPositionAbsoluteX(),
 				this.getCurrentPositionAbsoluteY(), this.getAlpha());
 	}
 
 	public double getCurrentPositionAbsoluteX() {
 		return currentPositionAbsoluteX;
 	}
 
 	public void setCurrentPositionAbsoluteX(double x) {
 		this.currentPositionAbsoluteX = x;
 	}
 
 	public double getCurrentPositionAbsoluteY() {
 		return currentPositionAbsoluteY;
 	}
 
 	public void setCurrentPositionAbsoluteY(double y) {
 		this.currentPositionAbsoluteY = y;
 	}
 
 	 public int getCurrentPositionRelativeX() {
 		return this.getMapGraph().getCurrentTileCoordinates()[0];
 	 }
 
 	 public int getCurrentPositionRelativeY() {
 		 return this.getMapGraph().getCurrentTileCoordinates()[1];
 	 }
 
 	 public int getStartPositionRelativeX() {
 		 return this.getMapGraph().getStartingTileCoordinates()[0];
 	 }
 
 	 public int getStartPositionRelativeY() {
 		 return this.getMapGraph().getStartingTileCoordinates()[1];
 	 }
 	 
 	 /**
 	  * The lightsensor is not attached on the middle point of the robot, but more in front of that point.
 	  * This value gives the x-coordinate of the lightsensor.
 	  */
 	 public double getLightsensorPositionX() {
 		 return (this.getCurrentPositionAbsoluteX() + 5*Math.cos(Math.toRadians(this.getAlpha())));
 	 }
 
 	 /**
 	  * The lightsensor is not attached on the middle point of the robot, but more in front of that point.
 	  * This value gives they -coordinate of the lightsensor.
 	  */
 	 public double getLightsensorPositionY() {
 		 return (this.getCurrentPositionAbsoluteY() + 5*Math.sin(Math.toRadians(this.getAlpha())));
 	 }
 	 
 	 /**
 	  * The ultrasonic sensor is not attached on the middle point of the robot, but a little behind that point.
 	  * This value gives the x-coordinate of the ultrasonic sensor.
 	  */
 	 public double getUltrasonicSensorPositionX() {
 		 return (this.getCurrentPositionAbsoluteX() - 2*Math.cos(Math.toRadians(this.getAlpha())));
 	 }
 
 	 /**
 	  * The ultrasonic sensor is not attached on the middle point of the robot, but a little behind that point.
 	  * This value gives the y-coordinate of the ultrasonic sensor.
 	  */
 	 public double getUltrasonicSensorPositionY() {
 		 return (this.getCurrentPositionAbsoluteY() - 2*Math.sin(Math.toRadians(this.getAlpha())));
 	 }
 
 	 public double getAlpha() {
 		 return alpha;
 	 }
 
 	 public void setAlpha(double alpha) {
 		 this.alpha = alpha;
 	 }
 
 	 public int getSpeed() {
 		 if (speed == 48)
 			 return 4;
 		 else if (speed == 58)
 			 return 3;
 		 else if (speed == 86)
 			 return 2;
 		 else
 			 return 1;
 	 }
 
 	 public void setSpeed(int speed) {
 		 if (speed == 1)
 			 this.speed = 194;
 		 else if (speed == 2)
 			 this.speed = 86;
 		 else if (speed == 3)
 			 this.speed = 58;
 		 else
 			 this.speed = 48;
 	 }
 
 	 public File getMapFile() {
 		 return this.mapFile;
 	 }
 
 	 public void setMapFile(File mapFile) {
 		 this.setMapFile(mapFile, 0, 0);
 		 SSG.getInformationBuffer().setXCoordinateRelative(0);
 		 SSG.getInformationBuffer().setYCoordinateRelative(0);
 
 	 }
 
 	 public void setMapFile(File mapFile, int xCo, int yCo) {
 		 this.mapFile = mapFile;
 		 this.setMapGraph(MapReader.createMapFromFile(mapFile, xCo, yCo));
 		 this.getSSG().updateCoordinates(
 				 "Simulator (" + (this.getCurrentPositionAbsoluteX()) + " , "
 						 + (this.getCurrentPositionAbsoluteY()) + " , "
 						 + (int) this.getAlpha() + "�, Map: "
 						 + this.getMapString() + ")");
 		 this.startPositionAbsoluteX = getCurrentPositionAbsoluteX();
 		 this.startPositionAbsoluteY = getCurrentPositionAbsoluteY();
 		 this.getSSG().getSimulationPanel().clearTotal();
 		 this.getSSG().getSimulationPanel().setTile(this.getMapGraph().getCurrentTileCoordinates()[0], this.getMapGraph().getCurrentTileCoordinates()[1]);
 		 SSG.getInformationBuffer().setXCoordinateRelative(xCo);
 		 SSG.getInformationBuffer().setYCoordinateRelative(yCo);
 		
 	 }
 
 	 public SilverSurferGUI getSSG() {
 		 return this.SSG;
 	 }
 
 	 public MapGraph getMapGraph() {
 		 return this.mapGraph;
 	 }
 
 	 public String getMapString() {
 		 if (this.getMapGraph() == null) {
 			 return "/";
 		 }
 		 return this.mapFile.getName();
 	 }
 
 	 /**
 	  * Use this method only intern! If you want to change the map, use the
 	  * setMapFile-method! only used when you delete the map
 	  */
 	 public void setMapGraph(MapGraph mapGraph) {
 		 if (mapGraph == null) {
 			 this.mapGraph = null;
 			 this.getSSG().getSimulationPanel().clearTotal();
 		 }
 		 this.mapGraph = mapGraph;
 	 }
 
 	 /**
 	  * Dit is de marge ten opzichte van de edge wordt gebruikt in travel :
 	  * wanneer de robot op 1 pixel verwijderd van de edge is begint hij zijn
 	  * currentPositionRelative aan te passen afhankelijk van de
 	  * currentPositionAbsolute dit gebeurt in setCurrentTileCoordinates
 	  */
 	 private double getEdgeMarge() {
 		 return (double) 1.2;
 	 }
 
 	 public void travel(double distance) {
 		 double xOld = this.getCurrentPositionAbsoluteX();
 		 double yOld = this.getCurrentPositionAbsoluteY();
 		 Orientation currentOrientation;
 
 		 if (distance >= 0) {
 
 			 for (int i = 1; i <= distance; i++) {
 				 // dit kan veranderen wanneer je over een edge gaat
 				 currentOrientation = Orientation.calculateOrientation(xOld,
 						 yOld, this.getAlpha());
 
 				 xOld = (double) (this.getCurrentPositionAbsoluteX() + 1
 						 * Math.cos(Math.toRadians(this.getAlpha())));
 				 yOld = (double) (this.getCurrentPositionAbsoluteY() + 1
 						 * Math.sin(Math.toRadians(this.getAlpha())));
 
 				 if (mapGraph != null) {
 
 					 if (onEdge(getLightsensorPositionX(), getLightsensorPositionY()) && checkForObstructionIfOnEdge(true)) {
 						 // deze if wordt uitgevoerd wanneer er een wall in de
 						 // weg staat
 						 this.setCurrentPositionAbsoluteX((double) (this
 								 .getCurrentPositionAbsoluteX() - 1
 								 * Math.cos(Math.toRadians(this.getAlpha()))));
 						 this.setCurrentPositionAbsoluteY((double) (this
 								 .getCurrentPositionAbsoluteY() - 1
 								 * Math.sin(Math.toRadians(this.getAlpha()))));
 						 this.getSSG().updateStatus();
 
 						 System.out.println("Er staat een muur in de weg");
 						 return;
 					 }
 
 					 this.travelToNextTileIfNeeded(xOld, yOld);
 				 }
 
 				 SSG.getSimulationPanel().setRobotLocation(xOld, yOld,
 						 this.getAlpha());
 				 this.setCurrentPositionAbsoluteX(xOld);
 				 this.setCurrentPositionAbsoluteY(yOld);
 				 try {
 					 Thread.sleep(speed);
 				 } catch (InterruptedException e) {
 				 }
 			 }
 
 			 this.getSSG().updateStatus();
 		 }
 
 		 else if (distance <= 0) {
 			 
 			 for (int i = -1; i >= distance; i--) {
 
 				 xOld = (double) (this.getCurrentPositionAbsoluteX() - 1
 						 * Math.cos(Math.toRadians(this.getAlpha())));
 				 yOld = (double) (this.getCurrentPositionAbsoluteY() - 1
 						 * Math.sin(Math.toRadians(this.getAlpha())));
 
 				 currentOrientation = Orientation.calculateOrientation(xOld,
 						 yOld, this.getAlpha()).getOppositeOrientation();
 
 				 if (mapGraph != null) {
 
 					 if (onEdge(xOld, yOld)
 							 && checkForObstructionIfOnEdge(false)) {
 						 this.setCurrentPositionAbsoluteX((double) (this
 								 .getCurrentPositionAbsoluteX() + 1
 								 * Math.cos(Math.toRadians(this.getAlpha()))));
 						 this.setCurrentPositionAbsoluteY((double) (this
 								 .getCurrentPositionAbsoluteY() + 1
 								 * Math.sin(Math.toRadians(this.getAlpha()))));
 
 						 this.getSSG().updateStatus();
 
 						 System.out.println("Er staat een muur in de weg");
 						 return;
 					 }
 
 					 this.travelToNextTileIfNeeded(xOld, yOld);
 
 				 }
 
 				 SSG.getSimulationPanel().setRobotLocation(xOld, yOld,
 						 this.getAlpha());
 				 this.setCurrentPositionAbsoluteX(xOld);
 				 this.setCurrentPositionAbsoluteY(yOld);
 				 try {
 					 Thread.sleep(speed);
 				 } catch (InterruptedException e) {
 				 }
 
 				 this.getSSG().updateStatus();
 			 }
 		 }
 	 }
 
 	 /**
 	  * Wanneer onEdge worden de currentTileCoordinates berekend op basis van de 
 	  * real coordinates
 	  */
 	 private void travelToNextTileIfNeeded(double xOld, double yOld) {
 		 if (onEdge(xOld,yOld)) {			 
 			 setCurrentTileCoordinates(mapGraph, xOld, yOld);} 
 		 else
 			 return;
 	 }
 	 
 	
 	 
 	 /**
 	  * Bij checkForObstructions ook direct tiles toevoegen aangrenzend aan de current,
 	  * nodig voor het algoritme! 
 	  * die worden dus hier toegevoegd en niet meer wanneer je naar een volgende tile gaat.
 	  */
 	public void checkForObstructions(){
 		for(int i = 0; i < 4; i++){
 			if(checkForObstruction()){
 				addWall();
 			}
 			else{
 				removeWall();
 				Orientation currentOrientation = Orientation.calculateOrientation(
 						 this.getCurrentPositionAbsoluteX(),
 						 this.getCurrentPositionAbsoluteY(), this.getAlpha());
 				 int xCoordinate = mapGraph.getCurrentTileCoordinates()[0] + currentOrientation.getArrayToFindNeighbourRelative()[0];
 				 int yCoordinate = mapGraph.getCurrentTileCoordinates()[1] + currentOrientation.getArrayToFindNeighbourRelative()[1];
 				 if(SSG.getSimulationPanel().getMapGraphConstructed().getTileWithCoordinates(xCoordinate, yCoordinate)==null)
 				 { SSG.getSimulationPanel().setTile(xCoordinate, yCoordinate);}
 			
 			}
 			rotate(90);
 			try {
 				//TODO gelijk gesteld worden aan tijd da de robot erover doet
 				 Thread.sleep(1000);
 			 } catch (InterruptedException e) {
 			 }
 		}
 		
 	}
 	
 	/**
 	 * checkt of de robot een obstruction ZIET
 	 */
 	 public boolean checkForObstruction() {
 		 Orientation currentOrientation = Orientation.calculateOrientation(
 				 this.getCurrentPositionAbsoluteX(),
 				 this.getCurrentPositionAbsoluteY(), this.getAlpha());
 
 //		 //afstand robot tot edge berekenen om te zien of hij er dicht genoeg bij staat als de 
 //		 //echte robot om de muur als echt te kunnen detecteren
 //		 Point2D point = ExtMath.calculateWallPoint(currentOrientation,
 //				 getCurrentPositionAbsoluteX(), getCurrentPositionAbsoluteY());
 //
 //		 double XOther = point.getX()
 //		 + currentOrientation.getOtherPointLine()[0];
 //		 double YOther = point.getY()
 //		 + currentOrientation.getOtherPointLine()[1];
 //
 //		 Double distance = Line2D.ptSegDist(point.getX(), point.getY(), XOther,
 //				 YOther, getCurrentPositionAbsoluteX(),
 //				 getCurrentPositionAbsoluteY());
 		 
 		 Double distance = calculateDistanceToWall();
 		 
 		 if (distance > detectionDistanceUltrasonicSensorRobot) {
 			 return false;
 		 }
 
 		 if (this.getMapGraph().getObstruction(currentOrientation) == Obstruction.WALL) {
 			 SilverSurferGUI.getInformationBuffer().addUltraSensorInfo(
 					 distance.intValue());
 			 return true;
 		 }
 
 		 return false;
 	 }
 	 
 	 /**
 	  * andere checkForObstructions dan checkForObstruction
 	  * deze wordt gebruikt voor als de robot op de edge staat en niet door mag kunnen
 	  * dus niet afhankelijk van of hij hem ziet of niet. Bij checkForObstruction
 	  * wordt gecheckt of de robot een obstruction ziet.
 	  */
 	 private boolean checkForObstructionIfOnEdge(boolean forwards){
 		 
 		 if(! currentPositionAndLigtsensorPositionOnSameTile()){
 			 return false;
 		 }
 		 
 		 Orientation currentOrientation = Orientation.calculateOrientation(
 				 this.getCurrentPositionAbsoluteX(),
 				 this.getCurrentPositionAbsoluteY(), this.getAlpha());
 
 		 if(!forwards){
 			 currentOrientation = currentOrientation.getOppositeOrientation();
 		 }
 		 
 		 if (this.getMapGraph().getObstruction(currentOrientation) == Obstruction.WALL) {
 			 return true;
 		 }
 		 return false;
 	 }
 	 
 	 /**
 	  * wordt gebruikt om te checken of de juiste muur gedetecteerd wordt en niet die van 
 	  * de volgende tile
 	  */
 	 private boolean currentPositionAndLigtsensorPositionOnSameTile(){
 		 int[] ligth = setAbsoluteToRelative(getLightsensorPositionX(), getLightsensorPositionY());
 		 int[] current = setAbsoluteToRelative(getCurrentPositionAbsoluteX(), getCurrentPositionAbsoluteY());
 		 return ligth[0] == current[0] && ligth[1] == current[1];
 	 }
 
 	 public void addWall() {
 		 Orientation currentOrientation = Orientation.calculateOrientation(
 				 this.getCurrentPositionAbsoluteX(),
 				 this.getCurrentPositionAbsoluteY(), this.getAlpha());
 
 			 SSG.getSimulationPanel().addWall(currentOrientation,
 					 getCurrentPositionAbsoluteX(),
 					 getCurrentPositionAbsoluteY());
 			 SSG.getSimulationPanel().setWallOnTile(getCurrentPositionRelativeX(), getCurrentPositionRelativeY(), currentOrientation);
 	 }
 	 
 	 public void removeWall(){
 	
 		 Orientation currentOrientation = Orientation.calculateOrientation(
 				 this.getCurrentPositionAbsoluteX(),
 				 this.getCurrentPositionAbsoluteY(), this.getAlpha());
 		 
 		 // roept addwhiteline op, deze methode verwijdert de muur terug uit
 		 // het panel
 		 SSG.getSimulationPanel().addWhiteLine(currentOrientation,
 				 getCurrentPositionAbsoluteX(),
 				 getCurrentPositionAbsoluteY());
 		 SSG.getSimulationPanel().removeWallFromTile(getCurrentPositionRelativeX(), getCurrentPositionRelativeY(), currentOrientation);
 	 }
 
 	 public void rotate(double alpha) {
 		 this.setAlpha(ExtMath.addDegree(this.getAlpha(), alpha));
 		 this.getSSG()
 		 .getSimulationPanel()
 		 .setRobotLocation(this.getCurrentPositionAbsoluteX(),
 				 this.getCurrentPositionAbsoluteY(), this.getAlpha());
 
 		 this.getSSG().getInformationBuffer().setAngle(this.getAlpha());
 		 
 		 this.getSSG().updateStatus();
 	 }
 
 	 public void clear() {
 		 this.getSSG().getSimulationPanel().clear();
 	 }
 	 
 	 /**
 	  * Resets the currentPositionAbsolute's and the startPositionAbsolute's to 220.
 	  * Resets alpha to 270, speed to 10;
 	  */
 	 public void reset(){
 		 currentPositionAbsoluteX = 220;
 		 currentPositionAbsoluteY = 220;
 		 startPositionAbsoluteX = 220;
 		 startPositionAbsoluteY = 220;
 		 alpha = 270;
 		 
 	 }
 
 	 public void updateArc(int distance) {
 		 getSSG().getSimulationPanel().updateArc(getCurrentPositionAbsoluteX(),
 				 getCurrentPositionAbsoluteY(), getAlpha(), distance);
 	 }
 
 	 public void setRealRobot(boolean isRealRobot) {
 		 this.isRealRobot = isRealRobot;
 	 }
 
 	 public boolean isRealRobot() {
 		 return isRealRobot;
 	 }
 
 	 /**
 	  * Returns a number from a normal districution that represents a lightsensor value.
 	  */
 	 public int getLightSensorValue() {
 		 if (this.isRealRobot()) {
 			 return SilverSurferGUI.getInformationBuffer()
 			 .getLatestLightSensorInfo();
 		 } else {
 			 // initialisation
 			 Random random = new Random();
 			 double mean = 0;
 			 double standardDeviation = 1;
 
 			 // check on which sort of underground your are standing
 			 // and adjust the mean and standardDeviation accordingly
 			 if (onEmptyTile(getLightsensorPositionX(),
 					 getLightsensorPositionY())) {
 				 mean = SimulationSensorData.getMEmptyPanelLS();
 				 standardDeviation = SimulationSensorData.getSDEmptyPanelLS();
 			 } else if (onWhiteLine(getLightsensorPositionX(),
 					 getLightsensorPositionY())) {
 				 mean = SimulationSensorData.getMWhiteLineLS();
 				 standardDeviation = SimulationSensorData.getSDWhiteLineLS();
 			 } else if (onBarcodeTile(getLightsensorPositionX(),
 					 getLightsensorPositionY())) {
				 System.out.println("onBarcode Simulationpilot line 512");
 				 int color = ((Barcode)this.getMapGraph().getContentCurrentTile())
 				 .getColorValue(getLightsensorPositionX() % 40,
 						 getLightsensorPositionY() % 40, Orientation.calculateOrientation(
 								 getCurrentPositionAbsoluteX(), getCurrentPositionAbsoluteY(),
 								 getAlpha()));
 				 mean = SimulationSensorData.getMBarcodeTileLS(color);
 				 standardDeviation = SimulationSensorData
 				 .getSDBarcodeTileLS(color);
 			 }
 			 
 			 return (int) Math.round(mean
 					 + (random.nextGaussian() * standardDeviation));
 		 }
 	 }
 
 	 public int getUltraSensorValue()
 	 {
 		 if (this.isRealRobot())
 		 {
 			 return SilverSurferGUI.getInformationBuffer().getLatestUltraSensorInfo();
 		 }
 		 else
 		 {
 			 Random random = new Random();
 
 			 double mean = this.calculateDistanceToWall();
 			 double standardDeviation = SimulationSensorData.getSDUS();
 
 			 return (int) Math.round(mean
 					 + (random.nextGaussian() * standardDeviation));
 		 }
 	 }
 
 	 public boolean getTouchSensor1Value() {
 		 if (this.isRealRobot()) {
 			 return SilverSurferGUI.getInformationBuffer()
 			 .getLatestTouchSensor1Info();
 		 } else {
 			 // to be implemented!!
 			 return false;
 		 }
 	 }
 
 	 public boolean getTouchSensor2Value() {
 		 if (this.isRealRobot()) {
 			 return SilverSurferGUI.getInformationBuffer()
 			 .getLatestTouchSensor2Info();
 		 } else {
 			 // to be implemented!!
 			 return false;
 		 }
 	 }
 
 	 /**
 	  * Calculates the distance to the first wall the robot will encounter facing its current orientation.
 	  * Returns 250 if no map is loaded or no wall is found whithin the range of the sensor.
 	  * The maximum range of the sensor is 120 cm.
 	  * 
 	  * By virtually moving forward (the temporary coordinates) en on every border checking whether there is a wall.
 	  * If so, you calculate the distance to is. If not, keep om moving (the robot doesn't move!)
 	  */
 	 private double calculateDistanceToWall()
 	 {
 		 // current temporary position; to check whether there are walls in the direction the robot is facing
 		 double xTemp = this.getUltrasonicSensorPositionX();
 		 double yTemp = this.getUltrasonicSensorPositionY();
 		 // keep the last temporary position, so you can compare with the current temporary position
 		 double xTempPrev = this.getUltrasonicSensorPositionX();
 		 double yTempPrev = this.getUltrasonicSensorPositionY();
 
 		 // there is no map loaded, so the sensor will detect no walls en returns the maximum value.
 		 if(this.getMapGraph() == null)
 		 {
 			 return 250;
 		 }
 		 Tile tileTemp = this.getMapGraph().getCurrentTile();
 		 int i = 1;
 
 		 while(i < 120)
 		 {
 			 while(!(Math.abs(xTempPrev%40 - xTemp%40) > 5) && !(Math.abs(yTempPrev%40 - yTemp%40) > 5))
 			 {
 				 xTempPrev = xTemp;
 				 yTempPrev = yTemp;
 				 
 				 xTemp = (double) (this.getUltrasonicSensorPositionX() + i* Math.cos(Math.toRadians(this.getAlpha())));
 				 yTemp = (double) (this.getUltrasonicSensorPositionY() + i* Math.sin(Math.toRadians(this.getAlpha())));
 				 i++;
 			 }
 
 			 Orientation oriTemp = Orientation.defineBorderCrossed(xTemp, yTemp, xTempPrev, yTempPrev);
 			 
 			 // the edge you have found, does not contain a wall, you can look right over it.
 			 // change the current tile to the next tile en move a few steps foreward (with the temporary coordinates).
 			 if(tileTemp.getEdge(oriTemp).isPassable())
 			 {
 				 tileTemp = tileTemp.getEdge(oriTemp).getNeighbour(tileTemp);
 				 for(int j = 0; j<35; j++)
 				 {
 					 xTempPrev = xTemp;
 					 yTempPrev = yTemp;
 
 					 xTemp = (double) (this.getUltrasonicSensorPositionX() + i* Math.cos(Math.toRadians(this.getAlpha())));
 					 yTemp = (double) (this.getUltrasonicSensorPositionY() + i* Math.sin(Math.toRadians(this.getAlpha())));
 					 i++;
 				 }
 			 }
 			 else
 			 {
 				 return Math.sqrt(Math.pow(xTemp-this.getUltrasonicSensorPositionX(), 2) + Math.pow(yTemp-this.getUltrasonicSensorPositionY(), 2));
 			 }
 		 }
 		 
 	     // no wall is found within the range of the ultrasonic sensor
 		 return 250;
 	 }
 
 		 public void allignOnWhiteLine() {
 			 Orientation orientation = Orientation.calculateOrientation(
 					 getCurrentPositionAbsoluteX(), getCurrentPositionAbsoluteY(),
 					 getAlpha());
 
 			 while (!onEdge(getCurrentPositionAbsoluteX(),
 					 getCurrentPositionAbsoluteY())) {
 				 travel(1);
 			 }
 
 			 double requiredAlpha = (double) orientation.getRightAngle();
 
 			 // double requiredAlpha = Math.round(getAlpha()/90) * 90;
 			 while (!(getAlpha() < requiredAlpha + 1 && getAlpha() > requiredAlpha - 1)) {
 				 if (getAlpha() < requiredAlpha) {
 					 rotate(1);
 				 } else
 					 rotate(-1);
 			 }
 			 for (int i = 0; i < 6; i++)
 				 travel(-1);
 		 }
 
 		 public void allignOnWalls() {
 			 //
 			 // rotate(90);
 			 //
 			 // Orientation orientation =
 			 // Orientation.calculateOrientation(getCurrentPositionAbsoluteX(),
 			 // getCurrentPositionAbsoluteY(), getAlpha());
 			 //
 			 // if(this.getMapGraph().getObstruction(orientation) ==
 			 // Obstruction.WALL){
 			 //
 			 // Point2D point = ExtMath.calculateWallPoint(orientation,
 			 // getCurrentPositionAbsoluteX(),getCurrentPositionAbsoluteY());
 			 //
 			 // double XOther = point.getX() +
 			 // Orientation.getOtherPointLine(orientation)[0];
 			 // double YOther = point.getY() +
 			 // Orientation.getOtherPointLine(orientation)[1];
 			 //
 			 // double bla = Line2D.ptSegDist(point.getX(), point.getY(), XOther,
 			 // YOther, getCurrentPositionAbsoluteX(),getCurrentPositionAbsoluteY());
 			 // System.out.println(bla);
 			 // if(bla >= 20){
 			 // travel(bla - 20);
 			 // }
 			 // else{
 			 // for(int i = 0; i<20-bla; i++)
 			 // travel(-1);
 			 // }
 			 //
 			 // }
 			 //
 			 // rotate(180);
 			 //
 			 // orientation =
 			 // Orientation.calculateOrientation(getCurrentPositionAbsoluteX(),
 			 // getCurrentPositionRelativeY(), getAlpha());
 			 //
 			 // if(this.getMapGraph().getObstruction(orientation) ==
 			 // Obstruction.WALL){
 			 //
 			 // Point2D point = ExtMath.calculateWallPoint(orientation,
 			 // getCurrentPositionAbsoluteX(),getCurrentPositionAbsoluteY());
 			 //
 			 // double XOther = point.getX() +
 			 // Orientation.getOtherPointLine(orientation)[0];
 			 // double YOther = point.getY() +
 			 // Orientation.getOtherPointLine(orientation)[1];
 			 //
 			 // double bla = Line2D.ptSegDist(point.getX(), point.getY(), XOther,
 			 // YOther, getCurrentPositionAbsoluteX(),getCurrentPositionAbsoluteY());
 			 // if(bla >= 20){
 			 // travel(bla - 20);
 			 // }
 			 // else{
 			 // for(int i = 0; i<20-bla; i++)
 			 // travel(-1);
 			 // }
 			 //
 			 // }
 			 //
 			 // rotate(90);
 			 //
 		 }
 
 		 /**
 		  * checkt of de robot zich binnen de marge van een edge bevindt
 		  */
 		 public boolean onEdge(double x, double y) {
 			 return (x % 40) > 40 - this.getEdgeMarge()
 			 || (x % 40) < this.getEdgeMarge()
 			 || (y % 40) > 40 - this.getEdgeMarge()
 			 || (y % 40) < this.getEdgeMarge();
 
 		 }
 
 		 /**
 		  * True if the robot is on an edge and this edge is not a wall
 		  */
 		 public boolean onWhiteLine(double x, double y) {
 			 // System.out.println("w: " + (this.onEdge(x,y) && (this.getMapGraph()
 			 // == null ||
 			 // this.getMapGraph().getObstruction(Orientation.calculateOrientation(x,
 			 // y, this.getAlpha())) != Obstruction.WALL)));
 			 return this.onEdge(x, y)
 			 && (this.getMapGraph() == null || this.getMapGraph()
 					 .getObstruction(
 							 Orientation.calculateOrientation(x, y,
 									 this.getAlpha())) != Obstruction.WALL);
 
 		 }
 
 		 /**
 		  * True if the robot is not on an edge, but on a tile without a content.
 		  */
 		 public boolean onEmptyTile(double x, double y) {
 			 // System.out.println("e: " + (!this.onEdge(x,y) && (this.getMapGraph()
 			 // == null || this.getMapGraph().getContentCurrentTile() == null)));
 			 return (!this.onEdge(x, y) && this.getMapGraph() == null)
 			 || (!this.onEdge(x, y) && this.getMapGraph()
 					 .getContentCurrentTile() == null);
 
 		 }
 
 		 /**
 		  * True if the robot is not on an edge, but on a tile containing a barcode.
 		  */
 		 public boolean onBarcodeTile(double x, double y) {
 			 if (this.getMapGraph() == null) {
 				 // System.out.println("b: /");
 				 return false;
 			 } else {
 				 // System.out.println("b: " + (!this.onEdge(x,y) &&
 				 // (this.getMapGraph().getContentCurrentTile() instanceof
 				 // Barcode)));
 				 return !this.onEdge(x, y)
 				 && (this.getMapGraph().getContentCurrentTile() instanceof Barcode);
 			 }
 		 }
 
 		 // zet een double om in een veelvoud van 40 kleiner dan de double (ook bij
 		 // negatief
 		 // maar doet normaal niet ter zake aangezien de coordinaten in het echte
 		 // coordinatensysteem
 		 // niet negatief kunnen zijn
 		 public static int setToMultipleOf40(double a) {
 			 return (int) (Math.floor(a / 40) * 40);
 		 }
 
 		 /**
 		  * Deze methode zet de coordinaten van het echte systeem om in de
 		  * coordinaten van de matrix
 		  */
 		 public int[] setAbsoluteToRelative(double x, double y) {
 			 double a = x - setToMultipleOf40(startPositionAbsoluteX);
 			 double b = y - setToMultipleOf40(startPositionAbsoluteY);
 			 int c;
 			 int d;
 			 c = (int) Math.floor(a / 40);
 			 d = (int) Math.floor(b / 40);
 
 			 int[] array = new int[2];
 			 array[0] = getStartPositionRelativeX() + c;
 			 array[1] = getStartPositionRelativeY() + d;
 			 return array;
 		 }
 
 		 /**
 		  * zet dus de map terug op zijn juiste currenttilecoorinates berekend uit de
 		  * xOld en yOld xOld en yOld mogen eig enkel de huidige positie
 		  * voorstellen!!!!!!!!!!!!!! moeten hier ingegeven worden omdat bij de
 		  * travelmethode je de currentabsoluteposition pas terug juist zet op het
 		  * einde van de lus
 		  */
 		 public void setCurrentTileCoordinates(MapGraph map, double xOld, double yOld) {
 			 int[] relativePosition = setAbsoluteToRelative(xOld, yOld);
 			 map.setCurrentTileCoordinates(relativePosition[0], relativePosition[1]);
 			 SSG.getInformationBuffer().setXCoordinateRelative(relativePosition[0]);
 			 SSG.getInformationBuffer().setYCoordinateRelative(relativePosition[1]);
 		 }
 
 	 }
