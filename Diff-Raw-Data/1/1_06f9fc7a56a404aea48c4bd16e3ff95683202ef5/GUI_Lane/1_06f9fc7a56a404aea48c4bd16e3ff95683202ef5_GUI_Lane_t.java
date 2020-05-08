 package gui;
 
 import agents.*;
 import java.util.*;
 import javax.swing.*;
 
 public class GUI_Lane extends GUI_Component{
 
 	boolean laneOn, moveFast;
 	int vibrationLevel, animCounter, waterVibrationLevel;
 	ImageIcon lanePicture; 
 	int x,y,waterLevelCount;
 	ArrayList<GUI_Part> parts;
 	ArrayList<Integer> steps;
 	LaneAgent laneAgent;
 	
 	public GUI_Lane(LaneAgent la)//default constructor
 	{
 		laneAgent = la;
 		laneOn = true;
 		moveFast = false;
 		vibrationLevel = 1;
 		waterVibrationLevel = 1;
 		waterLevelCount = 0;
 		lanePicture = new ImageIcon("gfx/laneMov1.png");
 		x = 20;
 		y = 20;
 		parts = new ArrayList<GUI_Part>();
 		steps = new ArrayList<Integer>();
 		myDrawing =  new Drawing(x,y,"laneMov1.png" );
 		animCounter = 0;
 	}
 	
 	public GUI_Lane(boolean on, int vibrate,String pic, int x1, int y1)//constructor
 	{
 		laneOn = on;
 		vibrationLevel = vibrate;
 		//lanePicture = new ImageIcon(pic);
 		x = x1;
 		y = y1;
 		parts = new ArrayList<GUI_Part>();
 		steps = new ArrayList<Integer>();
 		myDrawing = new Drawing(x,y,pic);
 	}
 	
 	public void turnOffLane()//turn off lane
 	{
 		laneOn = false;
 	}
 	
 	public void turnOnLane()//turn on lane
 	{
 		laneOn = true;
 	}
 	
 	public void setCoordinates(int x1, int y1)//set lane coordinates
 	{
 		x = x1;
 		y = y1;
 	}
 	
 	public int getX()//get lane x coordinate
 	{
 		return x;
 	}
 	
 	public int getY()//set lane y coordinate
 	{
 		return y;
 	}
 	
 	public boolean checkLane()//check if lane is on or off
 	{
 		return laneOn;
 	}
 	
 	public void setVibration(int vibrate)//set the vibration level of the lane
 	{
 		vibrationLevel = vibrate;
 	}
 	
 	public void setWaterVibration(int vibrate)//set the vibration level of the lane
 	{
 		waterVibrationLevel = vibrate;
 	}
 	
 	public int getVibration()//get the vibration level of the lane
 	{
 		return vibrationLevel;
 	}
 	
 	public void fasterWater()
 	{
 		waterVibrationLevel = 2;
 	}
 	
 	public void slowWater()
 	{
 		waterVibrationLevel = 1;
 	}
 	
 	public void DoMakeLaneFaster()
 	{
 		moveFast = true;
 	}
 	
 	public void notifyLane(GUI_Part p)//notify the lane that a part will be fed onto it, passes over the the part to be fed down lane
 	{
 		p.setCoordinates(x - 1000, y - 1000);  //makes part invisible from screens before being painted so its location can be updated first.
 		parts.add(p);
 		steps.add(0);
 	}
 	
 	public void removePart(int i)//remove part from the lane
 	{
 		synchronized (parts){
 			parts.remove(i);
 			steps.remove(i);
 		}
 	}
 	
 	public void paintComponent()
 	{
 		myDrawing.posX = x;
 		myDrawing.posY = y;
         if(laneOn){
         		myDrawing.filename = ("laneMov" + ((animCounter/10) + 1) + ".png");
         		if(moveFast)
         		{
         			fasterWater();
         			animCounter = (animCounter+waterVibrationLevel)%30;
         			waterLevelCount++;
         			if(waterLevelCount == 3)
         			{
         				moveFast = false;
        				waterLevelCount = 0;
         			}
         		}
         		else if(!moveFast)
         		{
         			slowWater();
         			animCounter = (animCounter+waterVibrationLevel)%30;
         		}
         }
         else{
             myDrawing.filename = "laneMov1.png";
         }
                 
 		myDrawing.subDrawings.clear();
 		
 		for(GUI_Part p : parts) {
 			p.paintComponent();
 			myDrawing.subDrawings.add(p.myDrawing);
 		}
 	}
 	
 	//Clears the lane
 	public void clear()
 	{
 		synchronized (parts)
 		{
 			parts.clear();
 			steps.clear();
 		}
 	}
 
 	
 	public void updateGraphics() //update function
 	{
 		synchronized (parts)
 		{
 			if(!parts.isEmpty() && laneOn)
 			{
 				for(int i=0; i<parts.size(); i++)
 				{	
 					int partX = x - (steps.get(i) * 2 * vibrationLevel ) + 180;
 					int partY = y+10;
 					if (partX > x)
 						parts.get(i).setCoordinates(partX, partY);
 					else
 					{
 						turnOffLane();
 						laneAgent.msgReadyToMove();
 					}
 					if ((-partX+x+180)/(2*vibrationLevel) == 14)
 					{
 						laneAgent.msgSpaceOpened();
 					}
 					steps.set(i, steps.get(i)+1);
 					
 				}
 			}
 		}
 	}
 	
 }
