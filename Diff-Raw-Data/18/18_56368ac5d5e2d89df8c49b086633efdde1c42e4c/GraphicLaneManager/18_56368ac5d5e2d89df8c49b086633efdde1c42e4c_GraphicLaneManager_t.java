 
 
 package factory.graphics;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 
 import javax.swing.ImageIcon;
 
 import factory.Part;
 
 /**
  * @author Minh la <p>
  * <b>{@code GraphicLaneManager.java}</b> (50x720) <br>
  * This creates and processes the Lane Manager,
  * As well as the lanes, nests, feeders, items, and bins
  */
 
 public class GraphicLaneManager{
 
 	//Image Icons
 	/**The image icons of all the paintable objects in Lane Manager**/
 	ImageIcon lane1Icon, lane2Icon;
 	ImageIcon divergeLaneIcon;
 	ImageIcon feederIcon;
 	ImageIcon divergerLightOffImage, divergerLightOnImage;
 	GraphicBin bin;
 
 	//Bin coordinates
 	/**The relative coordinates of the feeder**/
 	int feederX,feederY;
 
 	//Items
 	/**The Arraylist of items for each lane**/
 	ArrayList <GraphicItem> lane1Items;
 	ArrayList <GraphicItem> lane2Items;
 	/**Arraylist of booleans for the queues**/
 	ArrayList <Boolean> lane1QueueTaken;			//The queue
 	ArrayList <Boolean> lane2QueueTaken;			//The queue
 
 	//variables
 	/**The velocities of the Lane**/
 	int vX, vY;
 	/**The relative x and y position of the beginning of the LaneManager**/
 	int lane_xPos, lane_yPos;
 	/**boolean for when lane Start, default is false**/
 	boolean laneStart;
 	/**boolean for when feeder feeds to Top or bottom lane, true - up, false - down**/
 	boolean divergeUp;
 	/**Counter to periodic add item to lane**/
 	int timerCount;
 	/**Counter for vibration of items in lane**/
 	int vibrationCount;
 	/**The height of the vibration of items**/
 	private int vibrationAmplitude;
 	/**lane Manager Number**/
 	int laneManagerID;	
 	/**Counters to keep track of lane animation. 7 sprites**/
 	int laneAnimationCounter, laneAnimationSpeed;
 	/**Counter for diverger light animation**/
 	int divergerLightAnimationCounter;
 	/**boolean for the light animation. true is top. false is bottom.**/
 	boolean divergerLightAnimationCountUp;
 	/**boolean for feeder is on. true - on. false - off**/
 	boolean feederOn;
 	/**Feeder has a bin**/
 	boolean binExists;		
 	/**boolean for Lane is being purged**/
 	boolean lane1PurgeOn,lane2PurgeOn;
 	/**boolean for feeder is being purged**/
 	boolean feederPurged;
 	/**counter for feeder to purge**/
 	int feederPurgeTimer;
 	/**Counter for stabilization**/
 	int stabilizationCount[];
 	/**boolean for when nest is stable. true - stable. false - not stable**/
 	boolean isStable[];
 	/**Lane speed for each lane. To be inputted from panel. Not for v1!**/
 	int laneSpeed;
 	/**The max distance the item must reach before changing direction**/
 	int itemYMax, itemXMax;
 	/**the coordinates of the expected location of items in lanes**/
 	int itemXLane, itemYLaneUp, itemYLaneDown;
 	/**Instance of the graphic panel**/
 	GraphicPanel graphicPanel;
 
 	/**
 	 * Create a Lane manager at the given x and y coordinates and ID
 	 * @param laneX The x coordinate of the lane manager
 	 * @param laneY the y coordinate of the lane manager
 	 * @param ID the ID number of the Lane Manager. 0-3
 	 * @param gp GraphicPanel for intercomponent communication
 	 */
 	public GraphicLaneManager(int laneX,int laneY, int ID, GraphicPanel gp){
 		lane_xPos = laneX; lane_yPos = laneY;					//MODIFY to change Lane position
 		laneManagerID = ID;
 		graphicPanel = gp;
 		//bin = null;
 		//declaration of variables
 		laneAnimationCounter = 0;
 		laneAnimationSpeed = 1;			// default value
 		lane1Items = new ArrayList<GraphicItem>();
 		lane2Items = new ArrayList<GraphicItem>();
 		lane1QueueTaken = new ArrayList<Boolean>();
 		lane2QueueTaken = new ArrayList<Boolean>();
 		laneSpeed = 2;				//Change speed of the lane later
 		vX = -laneSpeed; vY = laneSpeed;
 		itemXMax = lane_xPos + 75;
 		itemYMax = lane_yPos + 70 + 40;
 		itemXLane = lane_xPos + 220;
 		itemYLaneUp = lane_yPos + 70 - 40;
 		itemYLaneDown = lane_yPos + 70 + 40;
 		laneStart = false;
 		divergeUp = false;
 		feederOn = false;
 		binExists = false;
 		lane1PurgeOn = false;		//Nest purge is off unless turned on
 		lane2PurgeOn = false;		//Nest purge is off unless turned on
 		feederPurged = false;
 		timerCount = 1; vibrationCount = 0; vibrationAmplitude = 2;
 		stabilizationCount = new int[2];
 		isStable = new boolean[2];
 		for (int i = 0; i < 2; i++) {
 			stabilizationCount[i] = 0;
 			isStable[i] = false;
 		}
 
 		//Location of bin to appear. x is fixed
 		feederX = lane_xPos + 250; feederY = lane_yPos + 15;
 
 		//Declaration of variables
 		lane1Icon = new ImageIcon("Images/lane.png");
 		lane2Icon = new ImageIcon("Images/lane.png");
 		divergeLaneIcon = new ImageIcon("Images/divergeLane.png");
 		feederIcon = new ImageIcon("Images/feeder.png");
 		divergerLightAnimationCountUp = true;
 		divergerLightOffImage = new ImageIcon("Images/divergerLights/dLight-1.png");
 		divergerLightOnImage = new ImageIcon("Images/divergerLights/dLight0.png");
 	}	
 
 	/**
 	 * Sets the Bin for the Lane manager's feeder
 	 * @param bin The Feeder's GraphicBin
 	 */
 	public void setBin(GraphicBin bin){
 		this.bin = bin;
 		if (bin != null) {
 			binExists = true;
 			bin.getBinType().setX(feederX+54);
 			bin.getBinType().setY(feederY+54);
 			feederPurgeTimer = 0;
 			feederPurged = false;
 		}
 	}
 	/**
 	 * Gets the Bin from the Lane Manager's feeder
 	 * @return the lane manager's feeder's bin
 	 */
 	public GraphicBin getBin(){
 		return bin;
 	}
 	
 	/**
 	 * returns the boolean for hasBin
 	 * @return return binExists
 	 */
 	public boolean hasBin()
 	{
 		return binExists;
 	}
 	
 	/**
 	 * Erase and return the current bin in the lane manager's feeder
 	 * @return the copy of the bin
 	 */
 	public GraphicBin popBin()
 	{
 		GraphicBin binCopy = bin;
 		bin = null;
 		binExists = false;
 		return binCopy;
 	}
 	
 	/**
 	 * Turns the purging of feeder on
 	 */
 	public void purgeFeeder() {
 		bin.getBinItems().clear();
 		feederOn = false;
 		feederPurged = true;
 		feederPurgeTimer = 0;
 	}
 	
 	/**
 	 * Paints the Lanes and the items within the lanes
 	 * @param g the specified graphics window
 	 */
 	public void paintLane(Graphics g){
 		Graphics2D g2 = (Graphics2D) g;
 
 		// Draw lanes
 		// horizontal
 		Graphics2D g3 = (Graphics2D)g.create();
 		// vertical
 		g3.rotate(Math.toRadians(90),lane_xPos+210, lane_yPos+20);
 		g3.drawImage(new ImageIcon("Images/Lane/"+laneAnimationCounter/laneAnimationSpeed+".png").getImage(), lane_xPos+210, lane_yPos-25, 120, 40, null);
 		g3.dispose();	
 		g2.drawImage(new ImageIcon("Images/Lane/"+laneAnimationCounter/laneAnimationSpeed+".png").getImage(), lane_xPos+75, lane_yPos+20, 180, 40, null);
 		g2.drawImage(new ImageIcon("Images/Lane/"+laneAnimationCounter/laneAnimationSpeed+".png").getImage(), lane_xPos+75, lane_yPos+100, 180, 40, null);
 		laneAnimationCounter ++;
 		if(laneAnimationCounter == laneAnimationSpeed*7)		// 7 = number of images
 			laneAnimationCounter = 0;
 		for(int i = 0;i<lane1Items.size();i++)
 			lane1Items.get(i).paint(g2);
 		for(int i = 0;i<lane2Items.size();i++)
 			lane2Items.get(i).paint(g2);
 		vibrationCount++;
 	} // END Paint function
 	
 	/**
 	 * paints the feeder, also alternates the lights of the diverger
 	 * @param g
 	 */
 	public void paintFeeder(Graphics g) {
 		// Draw background
 		g.setColor(Color.WHITE);
 		g.fillRect(feederX, feederY, 110, 110);
 		// Draw item icon
 		if(binExists && feederPurgeTimer < 7)
 				bin.getBinType().paint(g);
 		// Draw bin
 		if (binExists)
 			g.drawImage(bin.getBinImage().getImage(), feederX+85, feederY+15, null);
 		// Draw feeder
 		g.drawImage(feederIcon.getImage(), feederX, feederY, null);
 		// Draw and animate diverger lights
 		int dLightOnY = feederY + 17;
 		int dLightOffY = feederY + 17;
 		if(divergeUp)
 			dLightOffY += 80;
 		else
 			dLightOnY += 80;
 		divergerLightOnImage = new ImageIcon("Images/divergerLights/dLight"+divergerLightAnimationCounter+".png");
 		g.drawImage(divergerLightOnImage.getImage(), feederX, dLightOnY, null);
 		g.drawImage(divergerLightOffImage.getImage(), feederX, dLightOffY, null);
 		if(divergerLightAnimationCounter == 20)
 			divergerLightAnimationCountUp = false;
 		else if(divergerLightAnimationCounter == 0)
 			divergerLightAnimationCountUp = true;
 		if(divergerLightAnimationCountUp)
 			divergerLightAnimationCounter += 1;
 		else
 			divergerLightAnimationCounter -= 1;
 	}
 
 	/**
 	 * processes the lane manager.
 	 * Does everything for the Lane Manager
 	 */
 	public void moveLane() {
 		for (int i = 0; i < 2; i++) {
 			stabilizationCount[i]++;
 			if (binExists && stabilizationCount[i] >= bin.getStabilizationTime()) {
 				isStable[i] = true;
 				if (stabilizationCount[i] == bin.getStabilizationTime())
 					graphicPanel.sendMessage("na cmd neststabilized n" + laneManagerID + (i==0?"t":"b")); 
 			} 
 			else
 				isStable[i] = false;
 		}
 		
 		if (feederPurged && bin != null) {
 			feederPurgeTimer++;
 			if (feederPurgeTimer < 7)
 				bin.getBinType().moveX(5);
 			else {
 				feederPurged = false;
 				graphicPanel.purgeFeederDone(laneManagerID);
 			}
 		}
 		if(binExists){	//If Bin exists, gets items from bin
 			if(laneStart){
 				if(feederOn){
 					if(timerCount % 10 == 0){		//Put an item on lane on a timed interval
 						if(bin.getBinItems().size() > 0){
 							bin.getBinItems().get(0).setX(lane_xPos + 220);
 							bin.getBinItems().get(0).setY(lane_yPos + 70);
 							if(divergeUp){
 								bin.getBinItems().get(0).setVY(-laneSpeed);
 								bin.getBinItems().get(0).setDivergeUp(true);
 							}
 							else{
 								bin.getBinItems().get(0).setVY(laneSpeed);
 								bin.getBinItems().get(0).setDivergeUp(false);
 							}
 							bin.getBinItems().get(0).setVX(0);
 							if(divergeUp)
 								lane1Items.add(bin.getBinItems().get(0));
 							else
 								lane2Items.add(bin.getBinItems().get(0));
 							bin.getBinItems().remove(0);
 							if(bin.getBinItems().size() == 0){
 								feederOn = false;
 								graphicPanel.feedLaneDone(laneManagerID);
 							}
 						}
 					}
 				}
 
 				processLane();
 
 			}
 			timerCount++;
 		}
 		else{	//if bin does not exists, processes the items in the lanes
 			processLane();
 		}
 	}
 
 	/**
 	 * Moves the items in the lane.
 	 * Processes both top and bottom lane
 	 * DOES EVERYTHING
 	 */
 
 	public void processLane(){
 
 		if(lane1PurgeOn){		//If purge is on, empties the nest and destroys items on lane
 			graphicPanel.getNest().get(laneManagerID * 2).clearItems();
 			for(int j = 0; j <lane1Items.size();j++){
 				if(lane1Items.get(j).getY() > itemYLaneUp){
 					lane1Items.get(j).setVY(-laneSpeed);
 				}
 				else if(lane1Items.get(j).getX() > itemXMax){
 					lane1Items.get(j).setVX(vX);
 				}
 			}
 			
 			if(lane1Items.size() == 0){
 				lane1PurgeOn = false; //This is where the purge ends
 				lane1QueueTaken.clear();
 				graphicPanel.purgeTopLaneDone(laneManagerID);
 			}
 			else{
 				for(int i = 0;i<lane1Items.size();i++){
 					lane1Items.get(i).setX(lane1Items.get(i).getX() + lane1Items.get(i).getVX());
 					lane1Items.get(i).setY(lane1Items.get(i).getY() + lane1Items.get(i).getVY());
 
 
 					//Lane items move vertically
 					if(lane1Items.get(i).getVY() == vY || lane1Items.get(i).getVY() == -(vY) ){
 						if(vibrationCount % 4 == 1){	//Vibration left and right every 2 paint calls
 							if(i%2 == 0)
 									lane1Items.get(i).setX(itemXLane);
 							else if(i%2 == 1)
 									lane1Items.get(i).setX(itemXLane + vibrationAmplitude);
 						}
 						else if(vibrationCount % 4 == 3){
 							if(i%2 == 0)
 									lane1Items.get(i).setX(itemXLane + vibrationAmplitude);
 							else if(i%2 == 1)
 									lane1Items.get(i).setX(itemXLane);
 						}
 						if(lane1Items.get(i).getY() <= itemYLaneUp){
 							lane1Items.get(i).setY(itemYLaneUp);
 							lane1Items.get(i).setVY(0);
 							lane1Items.get(i).setVX(vX);
 						}
 					}
 					//Lane items move horizontally
 					if(lane1Items.get(i).getVX() == vX){
 						if(vibrationCount % 4 == 1){	//Vibration up and down every 2 paint calls
 							if(i%2 == 0){
 								lane1Items.get(i).setY(itemYLaneUp);
 							}
 							else if(i%2 == 1){
 								lane1Items.get(i).setY(itemYLaneUp - vibrationAmplitude);
 							}
 						}
 						else if(vibrationCount % 4 == 3){
 							if(i%2 == 0){
 								lane1Items.get(i).setY(itemYLaneUp - vibrationAmplitude);
 							}
 							else if(i%2 == 1){
 								lane1Items.get(i).setY(itemYLaneUp);
 							}
 						}
 						if(lane1Items.get(i).getX() < itemXMax){
 							lane1Items.remove(i);
 							i--;
 						}
 					}
 					if(lane1Items.size() == 0){
 						lane1PurgeOn = false; //This is where the purge ends
 						lane1QueueTaken.clear();
 						graphicPanel.purgeTopLaneDone(laneManagerID);
 					}
 				}
 			}
 		} // end of purge statements
 		else{		//Normal lane processing
 			for(int i = 0;i<lane1Items.size();i++){
 				lane1Items.get(i).setX(lane1Items.get(i).getX() + lane1Items.get(i).getVX());
 				lane1Items.get(i).setY(lane1Items.get(i).getY() + lane1Items.get(i).getVY());
 
 
 
 				//MOVES ITEMS DOWN LANE
 				//Lane items move vertically
 				if(lane1Items.get(i).getVY() == vY || lane1Items.get(i).getVY() == -(vY) ){
 					if(vibrationCount % 4 == 1){	//Vibration left and right every 2 paint calls
 						if(i%2 == 0)
 							lane1Items.get(i).setX(itemXLane);
 						else if(i%2 == 1)
 							lane1Items.get(i).setX(itemXLane + vibrationAmplitude);
 					}
 					else if(vibrationCount % 4 == 3){
 						if(i%2 == 0)
 							lane1Items.get(i).setX(itemXLane + vibrationAmplitude);
 						else if(i%2 == 1)
 							lane1Items.get(i).setX(itemXLane);
 					}
 					if(lane1Items.get(i).getY() <= itemYLaneUp){
 						lane1Items.get(i).setY(itemYLaneUp);
 						lane1Items.get(i).setVY(0);
 						lane1Items.get(i).setVX(vX);
 					}
 				}
 				//Queue entering Nests
 				if(graphicPanel.getNest().get(laneManagerID * 2).getSize() < 9){
 					for(int j = 0; j <lane1Items.size();j++){
 						if(lane1Items.get(j).getY() > itemYLaneUp){
 							lane1Items.get(j).setVY(-laneSpeed);
 							lane1Items.get(j).setVX(0);
 						}
 						else if(lane1Items.get(j).getX() > itemXMax + 5){
 							lane1Items.get(j).setVX(vX);
 							lane1Items.get(j).setVY(0);
 						}
 					}
 					if(lane1Items.get(i).getX() < itemXMax + 6){
 						lane1Items.get(i).setVY(0);
 						lane1Items.get(i).setVX(0);
 						stabilizationCount[0] = 0;
 						graphicPanel.sendMessage("fa cmd nestdestabilized n" + laneManagerID + "t");
 						lane1Items.get(i).setX(lane_xPos + 3 + 25 * (int)(graphicPanel.getNest().get(laneManagerID * 2).getSize() / 3));
 						boolean testDiverge = lane1Items.get(i).getDivergeUp();
 						lane1Items.get(i).setY(lane_yPos + 3 + 25 * (graphicPanel.getNest().get(laneManagerID * 2).getSize() % 3) + 80 * ((testDiverge)?0:1));
 						graphicPanel.getNest().get(laneManagerID * 2).addItem(lane1Items.get(i));
 						if(lane1QueueTaken.size() > 0)
 							lane1QueueTaken.remove(0);
 						lane1Items.remove(i);
 						for(int k = 0; k <lane1Items.size();k++){
 							System.out.println(k + "  X = " + lane1Items.get(k).getX() + "    vX " + lane1Items.get(k).getVX() + "    XMAX " + itemXMax + "   Y " + lane1Items.get(k).getY());
 						}
 						//System.out.println("size = " + lane1QueueTaken.size());
 						i--;
 						continue;
 					}
 				}
 				else{	//Nest Greater than 9
 					for(int j = 0; j <lane1Items.size();j++){
 						if(lane1Items.get(j).getX() < itemXMax + (lane1QueueTaken.size() + 1) * 10 ){
 							//lane1Items.get(j).setX(itemXMax + lane1QueueTaken.size() * 8);
 							lane1Items.get(j).setVX(0);
 						}
 					}
 					
 					if(lane1Items.get(i).getX() <= (itemXMax + 5 + (lane1QueueTaken.size() + 1) * 10) && lane1Items.get(i).getX() >= itemXMax){
 						//Queue is full, delete crashing Items
 						if(!lane1Items.get(i).getInQueue()){
 							lane1Items.get(i).setX(itemXMax  + 5 + (lane1QueueTaken.size()) * 10);
 							lane1Items.get(i).setInQueue(true);
 						}
 						if(lane1Items.get(i).getX() == (itemXMax + 5 + (lane1QueueTaken.size()) * 10)){
 							if(lane1QueueTaken.size() > 13){ // To be changed according to size of lane
 	
 								lane1Items.remove(i);
 								i--;
 								//continue;
 							}
 							else{
 								lane1Items.get(i).setVX(0);
 								//System.out.println("QUEUE ADDED");
 								lane1QueueTaken.add(new Boolean(true));
 							}
 							continue;
 						}
 					}
 				} // End of Queue entering nest
 
 				//Lane items move horizontally
 				if(lane1Items.get(i).getVX() == vX){
 					if(vibrationCount % 4 == 1){	//Vibration up and down every 2 paint calls
 						if(i%2 == 0){
 							lane1Items.get(i).setY(itemYLaneUp);
 						}
 						else if(i%2 == 1){
 							lane1Items.get(i).setY(itemYLaneUp - vibrationAmplitude);
 						}
 					}
 					else if(vibrationCount % 4 == 3){
 						if(i%2 == 0){
 							lane1Items.get(i).setY(itemYLaneUp - vibrationAmplitude);
 						}
 						else if(i%2 == 1){
 							lane1Items.get(i).setY(itemYLaneUp);
 						}
 					}
 
 					if(graphicPanel.getNest().get(laneManagerID * 2).getSize() >= 9){
 						if(lane1Items.get(i).getX() <= (itemXMax + 5 + (lane1QueueTaken.size() + 1) * 10) && lane1Items.get(i).getX() >= itemXMax + 5 ){
 							//Queue is full, delete crashing Items
 							if(!lane1Items.get(i).getInQueue()){
 								lane1Items.get(i).setX(itemXMax  + 5 + (lane1QueueTaken.size()) * 10);
 								lane1Items.get(i).setInQueue(true);
 							}
 							if(lane1Items.get(i).getX() == (itemXMax + 5 + (lane1QueueTaken.size()) * 10)){
 								if(lane1QueueTaken.size() > 13){ // To be changed according to size of lane
 		
 									lane1Items.remove(i);
 									i--;
 									//continue;
 								}
 								else{
 									lane1Items.get(i).setVX(0);
 									//System.out.println("QUEUE ADDED");
 									lane1QueueTaken.add(new Boolean(true));
 								}
 								continue;
 							}
 						}
 					}
 					else if(lane1Items.get(i).getX() < itemXMax + 6){
 						lane1Items.get(i).setVY(0);
 						lane1Items.get(i).setVX(0);
 						stabilizationCount[0] = 0;
 						graphicPanel.sendMessage("fa cmd nestdestabilized n" + laneManagerID + "t");
 						lane1Items.get(i).setX(lane_xPos + 3 + 25 * (int)(graphicPanel.getNest().get(laneManagerID * 2).getSize() / 3));
 						boolean testDiverge = lane1Items.get(i).getDivergeUp();
 						lane1Items.get(i).setY(lane_yPos + 3 + 25 * (graphicPanel.getNest().get(laneManagerID * 2).getSize() % 3) + 80 * ((testDiverge)?0:1));
 						graphicPanel.getNest().get(laneManagerID * 2).addItem(lane1Items.get(i));
 						if(lane1QueueTaken.size() > 0)
 							lane1QueueTaken.remove(0);
 						lane1Items.remove(i);
 						i--;
 					}
 				}
 				else{	//Vibration while waiting in queue
 					if(lane1Items.get(i).getVY() == 0){	//In the queue
 						if(vibrationCount % 4 == 1){	//Vibration up and down every 2 paint calls
 							if(i%2 == 0){
 								lane1Items.get(i).setY(itemYLaneUp);
 							}
 							else if(i%2 == 1){
 								lane1Items.get(i).setY(itemYLaneUp - vibrationAmplitude);
 							}
 						}
 						else if(vibrationCount % 4 == 3){
 							if(i%2 == 0){
 								lane1Items.get(i).setY(itemYLaneUp - vibrationAmplitude);
 							}
 							else if(i%2 == 1){
 								lane1Items.get(i).setY(itemYLaneUp);
 							}
 						}
 					}
 				}
 			}
 		}  // END OF LANE 1
 
 
 		if(lane2PurgeOn){		//If purge is on, empties the nest and destroys items on lane
 			graphicPanel.getNest().get(laneManagerID * 2 + 1).clearItems();
 			for(int j = 0; j <lane2Items.size();j++){
 				if(lane2Items.get(j).getY() < itemYLaneDown){
 					lane2Items.get(j).setVY(laneSpeed);
 				}
 				else if(lane2Items.get(j).getX() > itemXMax){
 					lane2Items.get(j).setVX(vX);
 				}
 			}
 
 			if(lane2Items.size() == 0){
 				lane2PurgeOn = false; //This is where the purge ends
 				lane2QueueTaken.clear();
 				graphicPanel.purgeBottomLaneDone(laneManagerID);
 			}
 			else{
 				for(int i = 0;i<lane2Items.size();i++){
 					lane2Items.get(i).setX(lane2Items.get(i).getX() + lane2Items.get(i).getVX());
 					lane2Items.get(i).setY(lane2Items.get(i).getY() + lane2Items.get(i).getVY());
 
 
 					//Lane items move vertically
 					if(lane2Items.get(i).getVY() == vY || lane2Items.get(i).getVY() == -(vY) ){
 						if(vibrationCount % 4 == 1){	//Vibration left and right every 2 paint calls
 							if(i%2 == 0)
 									lane2Items.get(i).setX(itemXLane);
 							else if(i%2 == 1)
 									lane2Items.get(i).setX(itemXLane + vibrationAmplitude);
 						}
 						else if(vibrationCount % 4 == 3){
 							if(i%2 == 0)
 									lane2Items.get(i).setX(itemXLane + vibrationAmplitude);
 							else if(i%2 == 1)
 									lane2Items.get(i).setX(itemXLane);
 						}
 						if(lane2Items.get(i).getY() >= itemYLaneDown){
 							lane2Items.get(i).setY(itemYLaneDown);
 							lane2Items.get(i).setVY(0);
 							lane2Items.get(i).setVX(vX);
 						}
 					}
 					//Lane items move horizontally
 					if(lane2Items.get(i).getVX() == vX){
 						if(vibrationCount % 4 == 1){	//Vibration up and down every 2 paint calls
 							if(i%2 == 0){
 								lane2Items.get(i).setY(itemYLaneDown);
 							}
 							else if(i%2 == 1){
 								lane2Items.get(i).setY(itemYLaneDown + vibrationAmplitude);
 							}
 						}
 						else if(vibrationCount % 4 == 3){
 							if(i%2 == 0){
 								lane2Items.get(i).setY(itemYLaneDown + vibrationAmplitude);
 							}
 							else if(i%2 == 1){
 								lane2Items.get(i).setY(itemYLaneDown);
 							}
 						}
 						if(lane2Items.get(i).getX() < itemXMax){
 							lane2Items.remove(i);
 							i--;
 						}
 					}
 					if(lane2Items.size() == 0){
 						lane2PurgeOn = false; //This is where the purge ends
 						lane2QueueTaken.clear();
						graphicPanel.purgeBottomLaneDone(laneManagerID);
 					}
 				}
 			}
 		} // end of purge statements
 		else{
 			for(int i = 0;i<lane2Items.size();i++){		//Do the same for lane 2
 				lane2Items.get(i).setX(lane2Items.get(i).getX() + lane2Items.get(i).getVX());
 				lane2Items.get(i).setY(lane2Items.get(i).getY() + lane2Items.get(i).getVY());
 
 
 
 				//Lane items move vertically
 				if(lane2Items.get(i).getVY() == vY || lane2Items.get(i).getVY() == -(vY) ){
 					if(vibrationCount % 4 == 1){	//Vibration left and right every 2 paint calls
 						if(i%2 == 0)
 							lane2Items.get(i).setX(itemXLane);
 						else if(i%2 == 1)
 							lane2Items.get(i).setX(itemXLane + vibrationAmplitude);
 					}
 					else if(vibrationCount % 4 == 3){
 						if(i%2 == 0)
 							lane2Items.get(i).setX(itemXLane + vibrationAmplitude);
 						else if(i%2 == 1)
 							lane2Items.get(i).setX(itemXLane);
 					}
 					if(lane2Items.get(i).getY() >= itemYLaneDown){
 						lane2Items.get(i).setY(itemYLaneDown);
 						lane2Items.get(i).setVY(0);
 						lane2Items.get(i).setVX(vX);
 					}
 				}
 
 
 				//Queue entering Nests
 				if(graphicPanel.getNest().get(laneManagerID * 2 + 1).getSize() < 9){
 					for(int j = 0; j <lane2Items.size();j++){
 						if(lane2Items.get(j).getY() < itemYLaneDown){
 							lane2Items.get(j).setVY(laneSpeed);
 							lane2Items.get(j).setVX(0);
 						}
 						else if(lane2Items.get(j).getX() > itemXMax + 5){
 							lane2Items.get(j).setVX(vX);
 							lane2Items.get(j).setVY(0);
 						}
 					}
 					if(lane2Items.get(i).getX() < itemXMax + 6){
 						lane2Items.get(i).setVY(0);
 						lane2Items.get(i).setVX(0);
 						stabilizationCount[1] = 0;
 						graphicPanel.sendMessage("fa cmd nestdestabilized n" + laneManagerID + "b");
 						lane2Items.get(i).setX(lane_xPos + 3 + 25 * (int)(graphicPanel.getNest().get(laneManagerID * 2 + 1).getSize() / 3));
 						boolean testDiverge = lane2Items.get(i).getDivergeUp();
 						lane2Items.get(i).setY(lane_yPos + 3 + 25 * (graphicPanel.getNest().get(laneManagerID * 2 + 1).getSize() % 3) + 80 * ((testDiverge)?0:1));
 						graphicPanel.getNest().get(laneManagerID * 2 + 1).addItem(lane2Items.get(i));
 						if(lane2QueueTaken.size() > 0)
 							lane2QueueTaken.remove(0);
 						lane2Items.remove(i);
 						i--;
 						continue;
 					}
 				}
 				else{
 					for(int j = 0; j <lane2Items.size();j++){
 						if(lane2Items.get(j).getX() < itemXMax + (lane2QueueTaken.size() + 1) * 10 ){
 							lane2Items.get(j).setVX(0);
 						}
 					}
 					if(lane2Items.get(i).getX() == (itemXMax + 5 + (lane2QueueTaken.size()) * 10)){
 						if(lane2QueueTaken.size() > 13){ // To be changed according to size of lane
 
 							lane2Items.remove(i);
 							i--;
 							//continue;
 						}
 						else{
 							lane2Items.get(i).setVX(0);
 							//System.out.println("QUEUE ADDED");
 							lane2QueueTaken.add(new Boolean(true));
 						}
 						continue;
 					}
 				} // End of Queue entering nest
 
 				//Lane items move horizontally
 				if(lane2Items.get(i).getVX() == vX){
 					if(vibrationCount % 4 == 1){	//Vibration up and down every 2 paint calls
 						if(i%2 == 0){
 							lane2Items.get(i).setY(itemYLaneDown);
 						}
 						else if(i%2 == 1){
 							lane2Items.get(i).setY(itemYLaneDown + vibrationAmplitude);
 						}
 					}
 					else if(vibrationCount % 4 == 3){
 						if(i%2 == 0){
 							lane2Items.get(i).setY(itemYLaneDown + vibrationAmplitude);
 						}
 						else if(i%2 == 1){
 							lane2Items.get(i).setY(itemYLaneDown);
 						}
 					}
 					
 					if(graphicPanel.getNest().get(laneManagerID * 2 + 1).getSize() >= 9){
 						if(lane2Items.get(i).getX() <= (itemXMax + 5 + (lane2QueueTaken.size() + 1) * 10) && lane2Items.get(i).getX() >= itemXMax + 5 ){
 							//Queue is full, delete crashing Items
 							if(!lane2Items.get(i).getInQueue()){
 								lane2Items.get(i).setX(itemXMax  + 5 + (lane2QueueTaken.size()) * 10);
 								lane2Items.get(i).setInQueue(true);
 							}
 							if(lane2Items.get(i).getX() == (itemXMax + 5 + (lane2QueueTaken.size()) * 10)){
 								if(lane2QueueTaken.size() > 13){ // To be changed according to size of lane
 		
 									lane2Items.remove(i);
 									i--;
 									//continue;
 								}
 								else{
 									lane2Items.get(i).setVX(0);
 									//System.out.println("QUEUE ADDED");
 									lane2QueueTaken.add(new Boolean(true));
 								}
 								continue;
 							}
 						}
 					}
 					else if(lane2Items.get(i).getX() < itemXMax + 6){ // reaches Nest
 						//remove from queue or lane item, add to nest
 						lane2Items.get(i).setVY(0);
 						lane2Items.get(i).setVX(0);
 						stabilizationCount[1] = 0;
 						graphicPanel.sendMessage("fa cmd nestdestabilized n" + laneManagerID + "b");
 						lane2Items.get(i).setX(lane_xPos + 3 + 25 * (int)(graphicPanel.getNest().get(laneManagerID * 2 + 1).getSize() / 3));
 						boolean testDiverge = !lane2Items.get(i).getDivergeUp();
 						lane2Items.get(i).setY(lane_yPos + 3 + 25 * (graphicPanel.getNest().get(laneManagerID * 2 + 1).getSize() % 3) + 80 * ((testDiverge)?0:1));
 						graphicPanel.getNest().get(laneManagerID * 2 + 1).addItem(lane2Items.get(i));
 						if(lane2QueueTaken.size() > 0)
 							lane2QueueTaken.remove(0);
 						lane2Items.remove(i);
 						i--;
 					}
 				}
 				else{
 					if(lane2Items.get(i).getVY() == 0){ // lane 2 queue stopped moving. needs to vibrate
 						if(vibrationCount % 4 == 1){	//Vibration up and down every 2 paint calls
 							if(i%2 == 0){
 								lane2Items.get(i).setY(itemYLaneDown);
 							}
 							else if(i%2 == 1){
 								lane2Items.get(i).setY(itemYLaneDown + vibrationAmplitude);
 							}
 						}
 						else if(vibrationCount % 4 == 3){
 							if(i%2 == 0){
 								lane2Items.get(i).setY(itemYLaneDown + vibrationAmplitude);
 							}
 							else if(i%2 == 1){
 								lane2Items.get(i).setY(itemYLaneDown);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 }
 
 
