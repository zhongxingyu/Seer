 package gui;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.util.*;
 import javax.swing.*;
 import agents.Kit;
 
 public class GUI_KitRobot implements GUI_Component {
 
 	boolean kitHeld;
 	GUI_Kit guikit;
 	GUI_KitStand stand;
 	GUI_Conveyor conveyor;
 	ImageIcon robotBody;
 	ImageIcon robotHands;
 	boolean move;
 	boolean fullyExtended;
 	int x,y,robotX,moveToY,moveToX;
 	
 	public GUI_KitRobot(GUI_Conveyor con, GUI_KitStand kstand )//default constructor
 	{
 		guikit = null;
 		conveyor = con;
 		stand = kstand;
 		
 		kitHeld = false;
 		robotBody = new ImageIcon("gfx/kitRobot.png");
 		robotHands = new ImageIcon("gfx/robotarm.png");
 		x = 30;
 		robotX = x+1;
 		y = 30;
 		moveToY = 0;
 		moveToX = 0;
 		move = false;
 		fullyExtended = false;
 	}
 	
 	public GUI_KitRobot(String body, String hands, int x1, int y1)//constructor
 	{
 		guikit = null;
 		kitHeld = false;
 		robotBody = new ImageIcon(body);
 		robotHands = new ImageIcon(hands);
 		x = x1;
 		robotX = x+20;
 		y = y1;
 		moveToY = 0;
 		moveToX = 0;
 		move =false;
 		fullyExtended = false;
 	}
 	
 	public void pickupKit(GUI_Kit k)//picks up kit passed through to it
 	{	
 		if(!kitHeld)
 		{
 			kitHeld = true;
 			guikit = k;
 			guikit.setX(robotX);
 			guikit.setY(y);
 		}
 	}
 	
 	public GUI_Kit placeKit()//places kit down
 	{
 		GUI_Kit new_kit = null;
 		
 		if(kitHeld)
 		{
 			new_kit = guikit;
 			guikit.x+= 39;
 			guikit = null;
 			kitHeld = false;
 			System.out.println("kit droppped");
 		}
 		
 		if(new_kit != null)
 			return new_kit;
 		
 		else return null;
 		
 	}
 	
 	public int getX()
 	{
 		return x;
 	}
 	
 	public int getY()
 	{
 		return y;
 	}
 	
 	public int getRobotX()
 	{
 		return robotX;
 	}
 
 	public void moveToStation(int x1,int y1)//moves to a given station
 	{
 		move = true;
 		if(x1 > 0)
 			x1 = x+40;
 
 		moveToY = y1;	
 		moveToX = x1;
 	}
 	
 	public boolean arrivedAtStation()//notifies that robot has arrived at station
 	{
 		return true;
 	}
 	
 	 public void DoPlaceOnKitStand(Kit kit) 
 	 { 
 		 Kit k = kit;
 		 
 		 if(conveyor.checkKit().getKitId() == k.getKitId())
 		 {
 			int x1 = conveyor.checkKit().x;
 			int y1 = conveyor.checkKit().y;
 			
 			moveToStation(x1,y1);
 			pickupKit(conveyor.robotRemoveKit());
 			
 			if(stand.positionOpen(2))
 			{
 				int x2 = stand.getX(2);
 				int y2 = stand.getY(2);
 				
 				moveToStation(x2,y2);
 				stand.addkit(placeKit(), 2);	
 			}
 			else if (stand.positionOpen(1))
 			{
 				int x2 = stand.getX(1);
 				int y2 = stand.getY(1);
 				
 				moveToStation(x2,y2);
 				stand.addkit(placeKit(), 1);	
 			}
 			
 		 }
 		 // Grab empty kit from conveyer
 		 // Place on kit stand
 		 //check of 2 is empty
 		 //if empty place there
 		 //else check if 1 is empty
 		 //if so place kit
 		 //if not do nothing
 		 
 	 }
 
 	   public void DoMoveFromKitStandToInspection(Kit kit) 
 	    {
 
 			 Kit k = kit;
 		
 			 if(stand.positionOpen(0))
 			 {
 				 if(k.getKitId() == stand.checkKit(2).getKitId())
 				 {
 					 int x1 = stand.getX(2);
 					 int y1 = stand.getY(2);
 					 
 					 moveToStation(x1,y1);
 					 pickupKit(stand.checkKit(2));
 					 
 					 int x2 = stand.getX(0);
 					 int y2 = stand.getY(0);
 					 
 					 moveToStation(x2,y2);
 					 stand.addkit(placeKit(), 0);
 				 }
 				 
 				 else if(kit.getKitId() == stand.checkKit(1).getKitId())
 				 {
 					 int x1 = stand.getX(1);
 					 int y1 = stand.getY(1);
 					 
 					 moveToStation(x1,y1);
 					 pickupKit(stand.checkKit(1));
 					 
 					 int x2 = stand.getX(0);
 					 int y2 = stand.getY(0);
 					 
 					 moveToStation(x2,y2);
 					 stand.addkit(placeKit(), 0);
 				 }
 			 }
 	    	//check if inspection stand is empty
 	    	//check kit id from kit passed in
 	    	//check if stand 2 kit id matches passed kit id
 	    	//if so, grab and move to inspection
 	    	//if not check if stand 1 kit id matches passed kit id
 	    	//if so, grab and move to inspection
 	    	//if not error
 	    	
 	    }
 
 	    public void DoMoveFromInspectionToConveyor(Kit kit)
 	    {
 	    	
 	    	Kit k = kit;
 	    	
 	    	if(k.getKitId() == stand.checkKit(0).getKitId())
 	    	{
 	    		int x1 = 30;
 	    		int y1 = 70;
 	    		
 	    		moveToStation(x1,y1);
 	    		
	    		//conveyor.robotAddKit(placeKit());
 	    		
 	    	}
 	    	//check if inspection kit id matches passed kit id
 	    	//if so move to conveyor
 	    	//place in conveyor
 	    	//else errorrrrrrrrr
 
 	    }
 	
 	
 	public void paintComponent(JPanel j, Graphics2D g)
 	{
 		robotBody.paintIcon(j, g, x, y);
 		robotHands.paintIcon(j, g, robotX, y);
 		
 		if(guikit!=null)
 			guikit.paintComponent(j,g);
 	}
 
 
 	public void updateGraphics()
 	{
 		if(guikit!=null)
 			guikit.updateGraphics();
 		
 		if(move){
 			
 			if(robotX == moveToX)
 	         {
 	          	fullyExtended = true;
 	          	move = false;
 	        	System.out.println(robotX);
 
 	         }
             if(y < moveToY)
             {
                 y+=1;
 
             }
             else if(y > moveToY)
             {
                 y-=1;
 
             }
            
             else if(x < moveToX)
             {
                 robotX+=1;
             }
            /* else if(x > moveToX)
             {
                 robotX-=1;
                 System.out.println("moved");
             }*/
             else
             {
                 move = false;
             }
 		}
         if(fullyExtended)
             {
             	if(robotX > (x+1))
             	{
             		robotX-=1;
             	}
             	else
             	{
             		fullyExtended = false;
             	}
             }
         if(kitHeld)
         {
         	guikit.setX(robotX);
         	guikit.setY(y);
         }
 		
 	}
 
 }
