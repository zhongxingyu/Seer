 import java.awt.Graphics2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Point2D.Double;
 import java.awt.image.BufferedImage;
 import java.io.Serializable;
 import java.util.ArrayList;
 
 //Hey Clayton just so you know the lane class is meant to be one side of the lane
 //this makes it easier to store the object without having to keep track of where it is
 //the wholelane class will have 2 lane objects representing each side
 //let me know if you want me to change something -Anthony
 //Edit: I created a combo lane class and changed your Lane to ComboLane in GUILane and V01
 
 @SuppressWarnings("serial")
 public class GUILane implements Serializable
 {
 	public ComboLane lane;
 	public Movement movement; // For storing lane position info
 	
 	boolean isForParts;
 	private ArrayList<GUIPallet> pallets;
 	private ArrayList<GUIPart> topParts, bottomParts;
 	
 	private int laneLength;
 	private ArrayList<GUILaneSegment> guiLaneSegments;
 	
 	private final int conveyorEndPadding = 30;
 		
 	public GUILane(ComboLane lane, boolean isForParts, int laneLength, double x, double y)
 	{
 		this.lane = lane;
 		
 		this.isForParts = isForParts;
 		if (isForParts)
 		{
 			topParts = new ArrayList<GUIPart>();
 			bottomParts = new ArrayList<GUIPart>();
 		} else pallets = new ArrayList<GUIPallet>();
 		
 		this.laneLength = laneLength;
 		guiLaneSegments = new ArrayList<GUILaneSegment>();
 		movement = new Movement(new Point2D.Double(x,y), 0);
 		
 		// Create the lane segments
 		for (int i=0; i<laneLength+1; i++)
 			guiLaneSegments.add(new GUILaneSegment(new Movement(getLaneSegStartPos(i), 0, -1,
 								getLaneSegEndPos(i), 0, 0)));
 	}
 
 	public void draw(Graphics2D g, long currentTime)
 	{
 		BufferedImage i_temp = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g_temp = i_temp.createGraphics();
 		
 		checkMotion(currentTime);
 		
 		// Draw to the bufferedImage
 		for (GUILaneSegment guiLaneSegment : guiLaneSegments)
 		{
 			guiLaneSegment.draw(g_temp, currentTime);
 		}
 		
 		// Crop the bufferedImage
		BufferedImage croppedBI = i_temp.getSubimage((int)movement.getStartPos().x, (int)movement.getStartPos().y, laneLength*60, 200);
 		// Draw it to the original graphics object
 		g.drawImage(croppedBI, (int)movement.getStartPos().x, (int)movement.getStartPos().y, null);
 		
 		// Draw the shadows at the ends
		Painter.draw(g, Painter.ImageEnum.SHADOW2, 10, 90, currentTime, movement, false);
		Painter.draw(g, Painter.ImageEnum.SHADOW2, 10, 90, currentTime, 
 					 new Movement(new Point2D.Double(movement.getStartPos().x + 60*laneLength-10, movement.getEndPos().y), Math.PI), false);
 		
 		
 		// Draw the pallets or parts
 		if (isForParts)
 		{
 			for (GUIPart p : topParts)
 				p.draw(g, currentTime);
 			for (GUIPart p : bottomParts)
 				p.draw(g, currentTime);
 		} else {
 			for (GUIPallet p : pallets)
 				p.draw(g, currentTime);
 		}
 		
 	}
 	
 	private void checkMotion(long currentTime)
 	{
 		if (lane.isLaneOn())
 		{
 			// Start the lane
 			for (int i = 0; i < guiLaneSegments.size(); i++)
 				guiLaneSegments.get(i).movement = guiLaneSegments.get(i).movement.moveToAtSpeed(currentTime, getLaneSegEndPos(i), 0, lane.getSpeed());
 			
 			if (isForParts)
 			{
 				// part movement never seems to have been set in the first place,
 				// so Andrew doesn't know what to set end pos to and commented this out for now
 				/*for (GUIPart p : topParts)
 					p.movement.unPause(currentTime);
 				for (GUIPart p : bottomParts)
 					p.movement.unPause(currentTime);*/
 			} else {
 				for (GUIPallet p : pallets)
 					p.movement.moveToAtSpeed(currentTime, getPalletEndPos(p), 0, lane.getSpeed());
 			}
 			
 			if (guiLaneSegments.get(0).movement.arrived(currentTime))
 			{
 				// Set each lane segment back to their start position to keep lane animating
 				for (int i = 0; i < guiLaneSegments.size(); i++)
 				{
 					guiLaneSegments.get(i).movement = Movement.fromSpeed(getLaneSegStartPos(i), 0, currentTime, getLaneSegEndPos(i), 0, lane.getSpeed());
 				}
 			}
 		} else {
 			// Stop the lane
 			for (GUILaneSegment guiLaneSegment : guiLaneSegments)
 			{
 				guiLaneSegment.movement = guiLaneSegment.movement.freeze(currentTime);
 			}
 			
 			if (isForParts)
 			{
 				for (GUIPart p : topParts)
 					p.movement = p.movement.freeze(currentTime);
 				for (GUIPart p : bottomParts)
 					p.movement = p.movement.freeze(currentTime);
 			} else {
 				for (GUIPallet p : pallets)
 					p.movement = p.movement.freeze(currentTime);
 			}
 		}
 	}
 	
 	public void addPallet()
 	{
 		GUIPallet pallet = new GUIPallet(new Pallet(), new GUIKit(new Kit(), 0,0), movement.getStartPos().x-50+60*laneLength, movement.getStartPos().y-12);
 		pallets.add(pallet);
 		pallet.movement = Movement.fromSpeed(pallet.movement.getStartPos(), Math.PI/2, System.currentTimeMillis(), 
 				new Point2D.Double((movement.getStartPos().x+conveyorEndPadding+120*(pallets.size()-1)), pallet.movement.getStartPos().y), Math.PI/2, lane.getSpeed());
 	}
 	
 	public void addPallet(GUIPallet pallet)
 	{
 		pallets.add(pallet);
 		pallet.movement = Movement.fromSpeed(pallet.movement.getStartPos(), Math.PI/2, System.currentTimeMillis(), 
 				getPalletEndPos(pallet), Math.PI/2, lane.getSpeed());
 	}
 	
 	public GUIPallet removeEndPallet()
 	{
 		GUIPallet removedPallet = pallets.remove(0);
 		// Move all other pallet's destinations down one
 
 		for (GUIPallet p : pallets)
 			p.movement = Movement.fromSpeed(p.movement.getStartPos(), Math.PI/2, System.currentTimeMillis(), getPalletEndPos(p), Math.PI/2, lane.getSpeed());
 		return removedPallet;
 	}
 	
 	public GUIKit removeEndPalletKit()
 	{
 		return pallets.get(0).removeKit();
 	}
 	
 	public boolean hasEmptyPalletAtEnd(long currentTime)
 	{
 		if (pallets.size() == 0)
 			return false;
 		
 		GUIPallet p = pallets.get(0);
 			if (p.movement.arrived(currentTime))
 //				if (p.movement.calcPos(currentTime).x == movement.getStartPos().x+conveyorEndPadding)
 					if (!p.hasKit())
 						return true;
 		return false;
 	}
 	
 	public boolean hasFullPalletAtEnd(long currentTime)
 	{
 		if (pallets.size() == 0)
 			return false;
 		
 		GUIPallet p = pallets.get(0);
 			if (p.movement.arrived(currentTime))
 //				if (p.movement.calcPos(currentTime).x == movement.getStartPos().x+conveyorEndPadding)
 					if (p.hasKit())
 						return true;
 		return false;
 	}
 	
 	public boolean containsPallets()
 	{
 		if (pallets.size() == 0)
 			return false;
 		return true;
 	}
 	
 	public Point2D.Double getLocationOfEndPallet(long currentTime)
 	{
 		if (pallets.size() == 0)
 			return null;
 		
 		return new Point2D.Double(pallets.get(0).movement.calcPos(currentTime).x+60, pallets.get(0).movement.calcPos(currentTime).y+40);
 	}
 	
 	public int getLaneLength()
 	{
 		return laneLength;
 	}
 
 	public Point2D.Double getLaneSegStartPos(int segID)
 	{
 		return new Point2D.Double(movement.getStartPos().x + 60*(laneLength-1) - 60*(segID-1), movement.getStartPos().y);
 	}
 
 	public Point2D.Double getLaneSegEndPos(int segID)
 	{
 		return new Point2D.Double(movement.getStartPos().x + 60*(laneLength-1) - 60*(segID), movement.getStartPos().y);
 	}
 
 	public Point2D.Double getPalletEndPos(GUIPallet pallet)
 	{
 		return new Point2D.Double(movement.getStartPos().x+conveyorEndPadding, pallet.movement.getStartPos().y);
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
