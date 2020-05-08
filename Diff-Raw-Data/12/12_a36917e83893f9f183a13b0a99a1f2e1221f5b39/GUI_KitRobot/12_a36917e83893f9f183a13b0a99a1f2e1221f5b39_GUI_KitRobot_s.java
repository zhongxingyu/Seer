 package gui;
 
 import javax.swing.*;
 import agents.Kit;
 import agents.KitRobotAgent;
 
 public class GUI_KitRobot extends GUI_Component {
 
 	boolean kitHeld;
 	GUI_Kit guikit;
 	GUI_KitStand stand;
 	KitRobotAgent kitRobot;
 	GUI_Conveyor conveyor;
 	ImageIcon robotBody;
 	ImageIcon robotHands;
 	boolean move;
 	boolean fullyExtended;
 	boolean goToOne = false,goToTwo = false,moveToOne = false,moveToTwo = false,moveToInspection = false,goToConveyor = false, finalMove = false;
 	int x,y,robotX,moveToY,moveToX,x2,y2,x1,y1;
 	
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
 		
 		myDrawing = new Drawing(0, 0, "NOPIC");
 		myDrawing.subDrawings.add(new Drawing(x,y,"kitRobot.png"));
 		myDrawing.subDrawings.add(new Drawing(robotX,y,"robotarm.png"));
 		
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
 			if(guikit != null)
 			{
 				new_kit = guikit;
 				guikit = null;
 				//guikit.posX+= 30;
 				//guikit = null;
 				kitHeld = false;
 			}
 
 		}
 		
 		return new_kit;
 	
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
 	
 	 public void DoPlaceOnKitStand(Kit kit, KitRobotAgent robotAgent) 
 	 { 
 		 Kit k = kit;
 		 kitRobot = robotAgent;
 		 
 		 
 		 if(conveyor.checkKit().kit == k)
 			 {
 				int x1 = conveyor.checkKit().posX;
 				int y1 = conveyor.checkKit().posY;
 				
 				moveToStation(x1,y1);
 				pickupKit(conveyor.robotRemoveKit());
 				
 				if(stand.positionOpen(2))
 				{
 					goToTwo = true;
 					
 					x2 = stand.getX(2);
 					y2 = stand.getY(2);
 					
 					moveToStation(x2,y2);
 					if(y == y2)
 					{
 						//stand.addkit(placeKit(), 2);
 						kitHeld = false;
 						guikit = null;
 						kitRobot.conveyor2StandLockRelease();
 					}
 				}
 				else if (stand.positionOpen(1))
 				{
 					goToOne = true;
 					
 					x2 = stand.getX(1);
 					y2 = stand.getY(1);
 					
 					moveToStation(x2,y2);
 					if(y == y2)
 					{
 						//stand.addkit(placeKit(), 1);
 						kitHeld = false;
 						guikit = null;
 						kitRobot.conveyor2StandLockRelease();
 					}
 				
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
 
 	   public void DoMoveFromKitStandToInspection(Kit kit, KitRobotAgent agent) 
 	    {
 		   
 			 Kit k = kit;
 			 kitRobot = agent;
 			 		 
 			 if(stand.positionOpen(0))
 			 {
 				 if (stand.checkKit(2) != null)
 				 {
 					 if(stand.checkKit(2).kit== k)
 					 {
 						 
 						 
 						 x1 = stand.getX(2);
 						 y1 = stand.getY(2);
 						 
 						 moveToTwo = true;
 						 						 
 						 moveToStation(x1,y1);
 						 
 	
 						 
 						 x2 = stand.getX(0);
 						 y2 = stand.getY(0);
 						 
 					 }
 				 }
 				 
 				 else if(stand.checkKit(1) != null)
 				 {
 					 
 					 if(kit.getKitId() == stand.checkKit(1).getKitId())
 					 {
 						 moveToOne = true;
 						 
 						 x1 = stand.getX(1);
 						 y1 = stand.getY(1);
 						 
 						 moveToStation(x1,y1);
 						 
 						 x2 = stand.getX(0);
 						 y2 = stand.getY(0);
 						 
 					 }
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
 	    		//goToConveyor = true;
 	    		
 	    		 x1 = stand.getX(0);
 				 y1 = stand.getY(0);
 				 
 				 moveToStation(x1,y1);
 
 				 goToConveyor = true;
 				 
 	    		x2 = conveyor.x;
	    		y2 = conveyor.y;
 	    		
 	    		//conveyor.robotAddKit(placeKit());
 	    		kitHeld = false;
 				guikit = null;
 	    		
 	    	}
 	    	//added by Sam to continue end of simulation
 	    	//should be placed in correct location though.
	    	kitRobot.inspectionToConveyorRelease();
 	    	
 	    	
 	    	//check if inspection kit id matches passed kit id
 	    	//if so move to conveyor
 	    	//place in conveyor
 	    	//else errorrrrrrrrr
 
 	    }
 	
 	public void paintComponent()
 	{
 		myDrawing.posX = x;
 		myDrawing.posY = y;
 		//y
 		
 		myDrawing.subDrawings.clear();
 		
 		myDrawing.subDrawings.add(new Drawing(x,y,"kitRobot.png"));
 		myDrawing.subDrawings.add(new Drawing(robotX,y,"robotarm.png"));
 		if(guikit!=null)
 		{
 			guikit.paintComponent();
 			myDrawing.subDrawings.add(guikit.myDrawing);
 		}
 			
 	}
 
 
 	public void updateGraphics()
 	{
 		if(guikit!=null)
 		{
 			//set coord
 			guikit.posX = robotX;
 			guikit.posY = y;
 			guikit.updateGraphics();
 		}
 		
 		if(move){
 			
 			if(robotX == moveToX)
 	         {
 	          	fullyExtended = true;
 	          	move = false;
 
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
         	guikit.posX =robotX;
         	guikit.posY =y;
        }
         
         if(goToTwo)
         {
 	        if(y >= y2)
 			{
 	        	if(fullyExtended)
 	        	{
 					placeKit();
                     kitHeld = false;
                                         
 					guikit = null;
                                         
 					kitRobot.conveyor2StandLockRelease();
 					goToTwo = false;
 	        	}
 			}
         }
         else if (goToOne)
         {
         	  if(y >= y2)
   			{
         		  if(fullyExtended)
         		  {
 	  				placeKit();
                     kitHeld = false;
                                        
   					guikit = null;
                                         
 	  				kitRobot.conveyor2StandLockRelease();
 	  				goToOne = false;
         		  }
   			}
         }
         if(moveToTwo)
         {
         	if(y == y1)
         	{
         		if(fullyExtended)
         		{
         			pickupKit(stand.checkKit(2));
         			moveToTwo = false;
         			moveToInspection = true;
         		}
         	}
         }
         else if(moveToOne)
         {
         	if(y == y1)
         	{
         		if(fullyExtended)
         		{
         			pickupKit(stand.checkKit(1));
         			moveToOne = false;
         			moveToInspection = true;
         		}
         	}
         }
         if(moveToInspection)
         {
         	moveToStation(x2,y2);
         	guikit.posX = robotX;
         	guikit.posY = y;
    
         	if(y == y2)
         	{
         		if(fullyExtended)
         		{
         			kitRobot.inspectLockRelease();
         			stand.addkit(placeKit(), 0);
         			kitHeld = false;
         			guikit = null;
         			moveToInspection = false;
         		}
         	}
         }
         
         if(goToConveyor)
         {
         	if(y == y1)
         	{
         		if(fullyExtended)
         		{
         			pickupKit(stand.checkKit(0));
         			goToConveyor = false;
         			finalMove = true;
         		}
         	}
         }
         if(finalMove)
         {
         	moveToStation(x2,y2);
         	   
        	if(y == y2)
         	{
         		if(fullyExtended)
         		{
         			conveyor.DoGetKitOut(placeKit());
         			finalMove = false;
         		}
         	}
         	
         }
 		
 	}
 }
