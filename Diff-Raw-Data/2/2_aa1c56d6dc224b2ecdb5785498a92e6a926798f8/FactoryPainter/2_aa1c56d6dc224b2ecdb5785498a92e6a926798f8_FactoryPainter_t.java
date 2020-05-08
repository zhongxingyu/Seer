 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 
 
 public class FactoryPainter 
 {
 	// Use painter static methods to scale and crop the images
 	
 	private FactoryStateMsg state;
 	/** Size of the factory manager's screen (size of the entire factory view) */
 	private static final Dimension entireFactoryArea = new Dimension(1400, 800);
 	/** Size of the kit manager's screen */
 	private static final Dimension kitManagerArea = new Dimension(900, 600);
 	/** Size of the part manager's screen */
 	private static final Dimension partManagerArea = new Dimension(900, 450);
 	/** Size of the feeder manager's screen */
 	private static final Dimension feederManagerArea = new Dimension(350, 500);
 	/** Size of the lane manager's screen */
	private static final Dimension laneManagerArea = new Dimension(800, 550);
 	/** Size of the gantry manager's screen */
 	private static final Dimension gantryManagerArea = new Dimension(1400, 800);
 	
 	public enum FactoryArea {
 		ENTIRE_FACTORY, KIT_MANAGER, PART_MANAGER, FEEDER_MANAGER, LANE_MANAGER, GANTRY_MANAGER
 	}
 	
 	
 	public FactoryPainter()
 	{
 		this.state = null;
 	}
 	
 	public FactoryPainter(FactoryStateMsg factoryState)
 	{
 		this.state = factoryState;
 	}
 	
 	public void update(FactoryUpdateMsg updateMsg)
 	{
 		state.update(updateMsg);
 	}
 	
 	public void setFactoryState(FactoryStateMsg factoryState)
 	{
 		this.state = factoryState;
 	}
 	
 	/**
 	 * Draws a image of the factory with everything in it
 	 * @return An image of the entire factory
 	 */
 	public BufferedImage drawEntireFactory()
 	{
 		state.updateTime();
 
 		BufferedImage factoryImg = new BufferedImage(entireFactoryArea.width, entireFactoryArea.height, BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g = factoryImg.createGraphics();
 
 		for (GUIItem item : state.items.values())
 		{
 			item.draw(g, state.timeElapsed);
 		}
 
 		g.dispose();
 		return factoryImg;
 	}
 
 	/**
 	 * Draws an image of the factory with only the specified items drawn.
 	 * @param itemsToDraw - array of the items to be drawn
 	 * @return An image of the factory
 	 */
 	@SuppressWarnings("rawtypes")
 	public BufferedImage drawFactoryIncluding(Class[] itemsToDraw)
 	{
 		state.updateTime();
 
 		BufferedImage factoryImg = new BufferedImage(entireFactoryArea.width, entireFactoryArea.height, BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g = factoryImg.createGraphics();
 
 		for (GUIItem item : state.items.values())
 		{
 			for (int i = 0; i<itemsToDraw.length; i++)
 			{
 				if (item.getClass().equals(itemsToDraw[i]))
 				{
 					item.draw(g, state.timeElapsed);
 					break;
 				}
 			}
 		}
 
 		g.dispose();
 		return factoryImg;
 	}
 	
 	/**
 	 * Draws an image of the factory with everything except the specified items.
 	 * @param itemsToOmit - array of the items to be drawn
 	 * @return An image of the factory
 	 */
 	@SuppressWarnings("rawtypes")
 	public BufferedImage drawFactoryExcluding(Class[] itemsToOmit)
 	{
 		state.updateTime();
 
 		BufferedImage factoryImg = new BufferedImage(entireFactoryArea.width, entireFactoryArea.height, BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g = factoryImg.createGraphics();
 		boolean containsItem = false;
 
 		for (GUIItem item : state.items.values())
 		{
 			containsItem = false;
 			
 			for (int i = 0; i<itemsToOmit.length; i++)
 			{
 				if (item.getClass().equals(itemsToOmit[i]))
 				{
 					containsItem = true;
 					break;
 				}
 			}
 			
 			if (!containsItem)
 				item.draw(g, state.timeElapsed);
 		}
 
 		g.dispose();
 		return factoryImg;
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public BufferedImage drawFactoryArea(FactoryArea area)
 	{
 		BufferedImage factoryImg = null;
 
 		switch (area)
 		{
 		// NOTE: The try-catch blocks are to simulate a C# using block - to define a custom scope
 		case KIT_MANAGER:
 			try {
 				Class[] drawOnly = {GUIKitDeliveryStation.class, GUIPallet.class, GUIKitStand.class, 
 									GUIKitRobot.class, GUIKitCamera.class, GUIFlash.class, GUIPartRobot.class};
 				
 				factoryImg = drawFactoryIncluding(drawOnly);
 				factoryImg = Painter.cropImage(factoryImg, 0, 0, kitManagerArea.width, kitManagerArea.height);
 			} catch (Exception e) {}
 			break;
 			
 		case PART_MANAGER:
 			try {
 				Class[] drawOnly = {GUINest.class, GUIPartRobot.class, GUIKitStand.class, GUIFlash.class};
 				
 				factoryImg = drawFactoryIncluding(drawOnly);
 				factoryImg = Painter.cropImage(factoryImg, 0, 0, partManagerArea.width, partManagerArea.height);
 			} catch (Exception e) {}
 			break;
 
 		case FEEDER_MANAGER:
 			try {
 				Class[] drawOnly = {GUIFeeder.class, GUIBin.class, GUIGantry.class, GUIDiverter.class, GUIDiverterArm.class};
 				
 				factoryImg = drawFactoryIncluding(drawOnly);
 				factoryImg = Painter.cropImage(factoryImg, 1050, 100, feederManagerArea.width, feederManagerArea.height);
 			} catch (Exception e) {}
 			break;
 
 		case LANE_MANAGER:
 			try {
 				Class[] drawOnly = {GUIFeeder.class, GUILane.class, GUIDiverter.class, GUIDiverterArm.class, GUINest.class,
 									GUIKitCamera.class, GUIFlash.class};
 				
 				factoryImg = drawFactoryIncluding(drawOnly);
 				factoryImg = Painter.cropImage(factoryImg, 550, 120, laneManagerArea.width, laneManagerArea.height);
 			} catch (Exception e) {}
 			break;
 			
 		case GANTRY_MANAGER:
 			try {				
 				Class[] drawOnly = {GUIGantry.class, GUIBin.class, GUIFeeder.class};
 				
 				factoryImg = drawFactoryIncluding(drawOnly);
 			} catch (Exception e) {}
 			break;
 
 		default:
 			break;
 		}
 		
 		return factoryImg;
 	}
 	
 	public static Dimension getAreaSize(FactoryArea area)
 	{
 		switch (area)
 		{
 		case ENTIRE_FACTORY:
 			return entireFactoryArea;
 		case KIT_MANAGER:
 			return kitManagerArea;
 		case PART_MANAGER:
 			return partManagerArea;
 		case FEEDER_MANAGER:
 			return feederManagerArea;
 		case LANE_MANAGER:
 			return laneManagerArea;
 		case GANTRY_MANAGER:
 			return gantryManagerArea;
 		default:
 			return null;
 		}
 	}
 }
 
 
 
 
 
 
 
 
 
 
