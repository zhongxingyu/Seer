 package gui;
 
 import javax.swing.*;
 
 import java.awt.*;
 import java.util.*;
 import agents.*;
 
 public class GUI_Lane extends GUI_Component{
 
 	boolean laneOn;
 	int vibrationLevel;
	//ImageIcon lanePicture; 
 	int x,y;
 	ArrayList<GUI_Part> parts;
 	ArrayList<Integer> steps;
 	LaneAgent laneAgent;
 	
 	public GUI_Lane(LaneAgent la)//default constructor
 	{
 		laneAgent = la;
 		laneOn = true;
 		vibrationLevel = 1;
		//lanePicture = new ImageIcon("gfx/lane.png");
 		x = 20;
 		y = 20;
 		parts = new ArrayList<GUI_Part>();
 		steps = new ArrayList<Integer>();
 		myDrawing =  new Drawing(x,y,"lane.png" );
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
 	
 	public int getVibration()//get the vibration level of the lane
 	{
 		return vibrationLevel;
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
 
 
 	public void paintComponent(Component j, Graphics2D g) //paint component
 	{
 		synchronized (parts){
			//lanePicture.paintIcon(j, g, x, y);
 			for(int i=0; i<parts.size(); i++)
 			{	
 				parts.get(i).paintComponent(j, g);
 			}
 		}
 	}
 	
 	public void paintComponent()
 	{
 		myDrawing.posX = x;
 		myDrawing.posY = y;
 		
 		myDrawing.subDrawings.clear();
 		
 		for(GUI_Part p : parts)
 		{
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
 					if (partX > 75)
 						parts.get(i).setCoordinates(partX, partY);
 					else
 					{
 						turnOffLane();
 						laneAgent.msgReadyToMove();
 					}
 					steps.set(i, steps.get(i)+1);
 					
 				}
 			}
 		}
 	}
 	
 }
