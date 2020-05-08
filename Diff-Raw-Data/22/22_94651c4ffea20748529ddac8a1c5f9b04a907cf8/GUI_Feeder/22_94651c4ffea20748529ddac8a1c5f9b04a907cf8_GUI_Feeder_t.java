 package gui;
 
 import java.awt.geom.Rectangle2D;
 import java.util.*;
 import javax.swing.*;
 import agents.*;
 
 public class GUI_Feeder extends GUI_Component {
 	
 	ImageIcon image = new ImageIcon("gfx/Feeder.png");
 	double xCoord;
 	double yCoord;
 	int topCount;
 	int botCount;
 	boolean isOn;
 	boolean binIsLow;
 	boolean topIsFeeding;
 	boolean botIsFeeding;
 	boolean gateRaised;
 	boolean divertedUp;
 	GUI_Part currentPart = new GUI_Part(new Part("dora"));
 	GUI_Lane topLane, botLane;
 	ArrayList<GUI_Part> topPartList = new ArrayList<GUI_Part>();
 	ArrayList<GUI_Part> botPartList = new ArrayList<GUI_Part>();
 	Rectangle2D.Double upOnSign = new Rectangle2D.Double(xCoord + 50, yCoord + 50, 7, 7);
 	Rectangle2D.Double botOnSign = new Rectangle2D.Double(xCoord + 50, yCoord + 70, 7, 7);
 	FeederAgent feeder;
 	
 	int partsCount = 0;
 	int releaseTimer = 0;
 	int releaseTime = 12;
 	
 	public GUI_Feeder(FeederAgent f)
 	{	
 		feeder = f;
 		xCoord = 0;
 		yCoord = 0;
 		topCount = 0;
 		botCount = 0;
 		isOn = true;
 		binIsLow = false;
 		topIsFeeding = false;
 		botIsFeeding = false;
 		gateRaised = false;
 		divertedUp = true;
 		myDrawing = new Drawing((int)xCoord, (int)yCoord, "Feeder.png"); 
 	}
 	
 	public void setLocation(double x, double y)
 	{
 		xCoord = x;
 		yCoord = y;
 	}
 	
 	public void setTopLane(GUI_Lane l)
 	{
 		topLane = l;
 	}
 	
 	public void setBotLane(GUI_Lane l)
 	{
 		botLane = l;
 	}
 	
 	public void setPart(GUI_Part p)
 	{
 		currentPart = p;
 	}
 	
 	public void switchOn()
 	{
 		isOn = true;
 	}
 	
 	public void switchOff()
 	{
 		isOn = false;
 	}
 
 	public void startFeed()
 	{
 		if (divertedUp)
 			topIsFeeding = true;
 		else
 			botIsFeeding = true;
 	}
 	
 	public void stopFeed()
 	{
 		if (divertedUp)
 			topIsFeeding = false;
 		else
 			botIsFeeding = false;
 	}
 	
 	public int countTopFeed()
 	{
 		topCount = topPartList.size();
 		return topCount;
 	}
 	
 	public int countBotFeed()
 	{
 		botCount = botPartList.size();
 		return botCount;
 	}
 	public void raiseGate()
 	{
 		gateRaised = true;
 	}
 	
 	public void lowerGate()
 	{
 		gateRaised = false;
 	}
 	
 	public void purge(boolean topPurge)
 	{
 		if (topPurge)
 		{
 			topPartList.clear();
 			topCount = 0;
 		}
 		else
 		{	
 			botPartList.clear();
 			botCount = 0;
 		}
 		currentPart = null;
 	}
 	                                                                                                      
 	public void divertUp()                                                                              
 	{                                                                                                     
 		divertedUp = true;                                                                              
 	}                                                                                                     
 	                                                                                                      
 	public void divertDown()                                                                             
 	{                                                                                                     
 		divertedUp = false;                                                                             
 	}     
 	
 	public double getXCoord()
 	{
 		return xCoord;
 	}
 	
 	public double getYCoord()
 	{
 		return yCoord;
 	}
 
 	
 	public void paintComponent() 
 	{
 		myDrawing.posX = (int)this.xCoord; //Just update the drawing's x and y to your component's current x and y
 		myDrawing.posY = (int)this.yCoord;
		if (topIsFeeding)
		{
			myDrawing.setImage("FeederTop.png");
		}
		else if (botIsFeeding)
		{
			myDrawing.setImage("FeederBot.png");
		}
		else
		{
			myDrawing.setImage("Feeder.png");
		}
 
 		//For any GUI_Parts your class is holding, you need to do this:
 		myDrawing.subDrawings.clear();
 
 		/*for(GUI_Part p : topPartList) 
 		{
 			p.paintComponent();
 			myDrawing.subDrawings.add(p.myDrawing);
 		}
 		/*for (GUI_Part p : botPartList)
 		{
 			p.paintComponent();
 			myDrawing.subDrawings.add(p.myDrawing);
 		}*/
 	}
 	
 	public void updateGraphics()
 	{
 		if (partsCount > 0 && isOn)
 		{
 			if (releaseTimer >= releaseTime)
 			{
 				feeder.msgReadyToMove();
 				releaseTimer = 0;
 				isOn = false;
 			}
 			else releaseTimer++;
 		}
 		
 	}
 	
 	public void addPart()
 	{
 		partsCount++;
 	}
 	
 	public void removePart()
 	{
 		partsCount--;
 		isOn = true;
 	}
 	
 	public void setDiverter(boolean top)
 	{
 		if (top) divertUp();
 		else divertDown();
 	}
 
 }
