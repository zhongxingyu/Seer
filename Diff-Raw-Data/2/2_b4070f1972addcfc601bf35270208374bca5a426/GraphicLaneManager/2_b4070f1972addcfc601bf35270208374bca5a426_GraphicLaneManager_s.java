 //Minh La
 
 package factory.graphics;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 
 import javax.swing.ImageIcon;
 
 import factory.Part;
 
 
 public class GraphicLaneManager{
 
 	//Image Icons
 	ImageIcon lane1Icon, lane2Icon;
 	ImageIcon divergeLaneIcon;
 	ImageIcon feederIcon;
 	GraphicBin bin;
 
 	//Bin coordinates
 	int feederX,feederY;
 
 	//Items
 	ArrayList <GraphicItem> lane1Items;
 	ArrayList <GraphicItem> lane2Items;
 	ArrayList <Boolean> lane1QueueTaken;			//The queue
 	ArrayList <Boolean> lane2QueueTaken;			//The queue
 
 	//variables
 	int vX, vY;		//velocities
 	int lane_xPos, lane_yPos;	//lane relative position
 	boolean laneStart;			//default = false
 	boolean divergeUp;			//true - up, false - down
 	int timerCount;				//counter to periodic add item to lane
 	int binItemCount;			//current item in bin to dump
 	int vibrationCount;			//every 2 paint, it'll vibrate
 	private int vibrationAmplitude;
 	int laneManagerID;					//lane Manager Number
 	int laneAnimationCounter, laneAnimationSpeed;
 	boolean feederOn;			//Feeder on/off
 	boolean binExists;			
 	boolean lane1PurgeOn;
 	boolean lane2PurgeOn;
 	boolean feederPurged;
 	int feederPurgeTimer;
 	int stabilizationCount[];
 	boolean isStable[];
 	int laneSpeed;
 	int itemYMax, itemXMax;
 	int itemXLane, itemYLaneUp, itemYLaneDown;
 
 	GraphicPanel graphicPanel;
 
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
 		laneSpeed = 8;				//Change speed of the lane later
 		vX = -laneSpeed; vY = laneSpeed;
 		itemXMax = lane_xPos + 220 - 160;
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
 		timerCount = 1; binItemCount = 0; vibrationCount = 0; vibrationAmplitude = 2;
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
 
 
 	}	
 
 	public void setBin(GraphicBin bin){
 		this.bin = bin;
 		bin.getBinType().setX(feederX+35);
 		bin.getBinType().setY(feederY+55);
 		if (bin != null)
 			binExists = true;
 	}
 
 	public GraphicBin getBin(){
 		return bin;
 	}
 	
 	public boolean hasBin()
 	{
 		return binExists;
 	}
 	
 	public GraphicBin popBin()
 	{
 		GraphicBin binCopy = bin;
 		bin = null;
 		binExists = false;
 		return binCopy;
 	}
 	
 	public void purgeFeeder() {
 		bin.getBinItems().clear();
 		feederOn = false;
 		feederPurged = true;
 		feederPurgeTimer = 0;
 	}
 	
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
 
 	public void paintFeeder(Graphics g) {
 		if (binExists)
 			g.drawImage(bin.getBinImage().getImage(), feederX + 50, feederY+15, null);
 		g.drawImage(feederIcon.getImage(), feederX, feederY, null);
 		g.setColor(new Color(60, 33, 0));
 		g.fillRect(feederX+34, feederY+54, 22, 22);
 		if (binExists && feederPurgeTimer < 7)
 			bin.getBinType().paint(g);
 	}
 
 	public void moveLane() {
 		for (int i = 0; i < 2; i++) {
 			stabilizationCount[i]++;
 			if (binExists && stabilizationCount[i] >= bin.getStabilizationTime()) {
 				isStable[i] = true;
 				if (stabilizationCount[i] == bin.getStabilizationTime())
 					graphicPanel.sendMessage("fa cmd neststabilized n" + laneManagerID + (i==0?"t":"b")); 
 			} 
 			else
 				isStable[i] = false;
 		}
 		
 		if (feederPurged) {
 			feederPurgeTimer++;
 			if (feederPurgeTimer < 7)
 				bin.getBinType().moveX(5);
 			else {
 				feederPurged = false;
 				graphicPanel.purgeFeederDone(laneManagerID);
 			}
 		}
 		if(binExists){
 			if(laneStart){
 				if(feederOn){
 					if(timerCount % 10 == 0){		//Put an item on lane on a timed interval
 						if(bin.getBinItems().size() > 0){
 							bin.getBinItems().get(0).setX(lane_xPos + 220);
 							bin.getBinItems().get(0).setY(lane_yPos + 70);
 							if(divergeUp){
 								bin.getBinItems().get(0).setVY(-8);
 								bin.getBinItems().get(0).setDivergeUp(true);
 							}
 							else{
 								bin.getBinItems().get(0).setVY(8);
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
 		else{
 			processLane();
 		}
 	}
 
 	public void processLane(){
 
 		if(lane1PurgeOn){		//If purge is on, empties the nest and destroys items on lane
 			graphicPanel.getNest().get(laneManagerID * 2).clearItems();
 			for(int j = 0; j <lane1Items.size();j++){
 				if(lane1Items.get(j).getStepY() > 0){
 					lane1Items.get(j).setVY(-8);
 				}
 				else if(lane1Items.get(j).getStepX() > 0){
 					lane1Items.get(j).setVX(vX);
 				}
 			}
 			System.out.println("size " + lane1QueueTaken.size());
 			
 			if(lane1Items.size() == 0){
 				lane1PurgeOn = false; //This is where the purge ends
 				lane1QueueTaken.clear();
 				System.out.println("size " + lane1QueueTaken.size());
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
 						lane1Items.get(i).setStepY(lane1Items.get(i).getStepY() - 1);
 						if(lane1Items.get(i).getStepY() == 0){
 							lane1Items.get(i).setVY(0);
 							lane1Items.get(i).setVX(vX);
 						}
 					}
 					//Lane items move horizontally
 					if(lane1Items.get(i).getVX() == vX){
 						lane1Items.get(i).setStepX(lane1Items.get(i).getStepX() - 1);
 						if(vibrationCount % 4 == 1){	//Vibration up and down every 2 paint calls
 							if(i%2 == 0){
 								lane1Items.get(i).setY(itemYLaneUp);
 							}
 							else if(i%2 == 1){
 								lane1Items.get(i).setY(itemYLaneUp + vibrationAmplitude);
 							}
 						}
 						else if(vibrationCount % 4 == 3){
 							if(i%2 == 0){
 								lane1Items.get(i).setY(itemYLaneUp + vibrationAmplitude);
 							}
 							else if(i%2 == 1){
 								lane1Items.get(i).setY(itemYLaneUp);
 							}
 						}
 						if(lane1Items.get(i).getStepX() == 0){
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
 					lane1Items.get(i).setStepY(lane1Items.get(i).getStepY() - 1);
 					if(lane1Items.get(i).getStepY() == 0){
 						lane1Items.get(i).setVY(0);
 						lane1Items.get(i).setVX(vX);
 					}
 				}
 				//Queue entering Nests
 				if(graphicPanel.getNest().get(laneManagerID * 2).getSize() < 9){
 					for(int j = 0; j <lane1Items.size();j++){
 						if(lane1Items.get(j).getStepY() > 0){
 							lane1Items.get(j).setVY(-8);
 						}
 						else if(lane1Items.get(j).getStepX() > 0){
 							lane1Items.get(j).setVX(vX);
 						}
 					}
 					if(lane1Items.get(i).getStepX() == 0){
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
 				else{
 					for(int j = 0; j <lane1Items.size();j++){
 						if(lane1Items.get(j).getStepX() < lane1QueueTaken.size() + 1){
 							lane1Items.get(j).setVX(0);
 						}
 					}
 					if(lane1Items.get(i).getStepX() == lane1QueueTaken.size() + 1){		//IT IS THIS LINE TO FIX
 						//Queue is full, delete crashing Items
 						if(lane1QueueTaken.size() > 17){ // To be changed according to size of lane
 							lane1Items.remove(i);
 							i--;
 							continue;
 						}
 						else{
 							lane1Items.get(i).setVX(0);
 							lane1QueueTaken.add(new Boolean(true));
 							
 						}
 						continue;
 					}
 				} // End of Queue entering nest
 
 				//Lane items move horizontally
 				if(lane1Items.get(i).getVX() == vX){
 					lane1Items.get(i).setStepX(lane1Items.get(i).getStepX() - 1);
 					if(vibrationCount % 4 == 1){	//Vibration up and down every 2 paint calls
 						if(i%2 == 0){
 							lane1Items.get(i).setY(itemYLaneUp);
 						}
 						else if(i%2 == 1){
 							lane1Items.get(i).setY(itemYLaneUp + vibrationAmplitude);
 						}
 					}
 					else if(vibrationCount % 4 == 3){
 						if(i%2 == 0){
 							lane1Items.get(i).setY(itemYLaneUp + vibrationAmplitude);
 						}
 						else if(i%2 == 1){
 							lane1Items.get(i).setY(itemYLaneUp);
 						}
 					}
 
 					if(graphicPanel.getNest().get(laneManagerID * 2).getSize() >= 9){
 						if(lane1Items.get(i).getStepX() == lane1QueueTaken.size() + 1){
 							//Queue is full, delete crashing Items
 							if(lane1QueueTaken.size() > 17){ // To be changed according to size of lane
 
 								lane1Items.remove(i);
 								i--;
 							}
 							else{
 								lane1Items.get(i).setVX(0);
 								lane1QueueTaken.add(new Boolean(true));
 							}
 						}
 					}
 					else if(lane1Items.get(i).getStepX() == 0){
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
 				else{
 					if(lane1Items.get(i).getVY() == 0){	//In the queue
 						if(vibrationCount % 4 == 1){	//Vibration up and down every 2 paint calls
 							if(i%2 == 0){
 								lane1Items.get(i).setY(itemYLaneUp);
 							}
 							else if(i%2 == 1){
 								lane1Items.get(i).setY(itemYLaneUp + vibrationAmplitude);
 							}
 						}
 						else if(vibrationCount % 4 == 3){
 							if(i%2 == 0){
 								lane1Items.get(i).setY(itemYLaneUp + vibrationAmplitude);
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
 				if(lane2Items.get(j).getStepY() > 0){
 					lane2Items.get(j).setVY(8);
 				}
 				else if(lane2Items.get(j).getStepX() > 0){
 					lane2Items.get(j).setVX(vX);
 				}
 			}
 
 			if(lane2Items.size() == 0){
 				lane2PurgeOn = false; //This is where the purge ends
 				lane2QueueTaken.clear();
 				graphicPanel.purgeTopLaneDone(laneManagerID);
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
 						lane2Items.get(i).setStepY(lane2Items.get(i).getStepY() - 1);
 						if(lane2Items.get(i).getStepY() == 0){
 							lane2Items.get(i).setVY(0);
 							lane2Items.get(i).setVX(vX);
 						}
 					}
 					//Lane items move horizontally
 					if(lane2Items.get(i).getVX() == vX){
 						lane2Items.get(i).setStepX(lane2Items.get(i).getStepX() - 1);
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
 						if(lane2Items.get(i).getStepX() == 0){
 							lane2Items.remove(i);
 							i--;
 						}
 					}
 					if(lane2Items.size() == 0){
 						lane2PurgeOn = false; //This is where the purge ends
 						lane2QueueTaken.clear();
 						graphicPanel.purgeTopLaneDone(laneManagerID);
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
 					lane2Items.get(i).setStepY(lane2Items.get(i).getStepY() - 1);
 					if(lane2Items.get(i).getStepY() == 0){
 						lane2Items.get(i).setVY(0);
 						lane2Items.get(i).setVX(vX);
 					}
 				}
 
 
 				//Queue entering Nests
 				if(graphicPanel.getNest().get(laneManagerID * 2 + 1).getSize() < 9){
 					for(int j = 0; j <lane2Items.size();j++){
 						if(lane2Items.get(j).getStepY() > 0){
 							lane2Items.get(j).setVY(8);
 						}
 						else if(lane2Items.get(j).getStepX() > 0){
 							lane2Items.get(j).setVX(vX);
 						}
 					}
 					if(lane2Items.get(i).getStepX() == 0){
 						lane2Items.get(i).setVY(0);
 						lane2Items.get(i).setVX(0);
 						stabilizationCount[1] = 0;
 						graphicPanel.sendMessage("fa cmd nestdestabilized n" + laneManagerID + "b");
 						lane2Items.get(i).setX(lane_xPos + 3 + 25 * (int)(graphicPanel.getNest().get(laneManagerID * 2 + 1).getSize() / 3));
 						boolean testDiverge = lane2Items.get(i).getDivergeUp();
 						lane2Items.get(i).setY(lane_yPos + 3 + 25 * (graphicPanel.getNest().get(laneManagerID * 2 + 1).getSize() % 3) + 80 * ((testDiverge)?0:1));
 						graphicPanel.getNest().get(laneManagerID * 2).addItem(lane2Items.get(i));
 						if(lane2QueueTaken.size() > 0)
 							lane2QueueTaken.remove(0);
 						lane2Items.remove(i);
 						i--;
 					}
 				}
 				else{
 					for(int j = 0; j <lane2Items.size();j++){
 						if(lane2Items.get(j).getStepX() < lane2QueueTaken.size() + 1){
 							lane2Items.get(j).setVX(0);
 						}
 					}
 					if(lane2Items.get(i).getStepX() == lane2QueueTaken.size() + 1){		//IT IS THIS LINE TO FIX
 						//Queue is full, delete crashing Items
 						if(lane2QueueTaken.size() > 17){ // To be changed according to size of lane
 							lane2Items.remove(i);
 							i--;
 							continue;
 						}
 						else{
 							lane2Items.get(i).setVX(0);
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
 					lane2Items.get(i).setStepX(lane2Items.get(i).getStepX() - 1);
 					if(lane2PurgeOn){
 						graphicPanel.getNest().get(laneManagerID * 2 + 1).clearItems();
 						for(int j = 0; j <lane2Items.size();j++){
 							lane2Items.get(j).setVX(vX);
 						}
 						if(lane2Items.get(i).getStepX() == 0){
 							lane2Items.remove(i);
 							i--;
 						}
 						if(lane2Items.size() == 0)
 							lane2PurgeOn = false;
 					}
 					else if(graphicPanel.getNest().get(laneManagerID * 2 + 1).getSize() >= 9){
 						if(lane2Items.get(i).getStepX() == lane2QueueTaken.size() + 1){
 							//Queue is full, delete crashing Items
 							if(lane2QueueTaken.size() > 17){
 								lane2Items.remove(i);
 								i--;
 							}
 							else{
 								lane2Items.get(i).setVX(0);
 								lane2QueueTaken.add(new Boolean(true));
 							}
 						}
 					}
 					else if(lane2Items.get(i).getStepX() == 0){ // reaches Nest
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
 
 
