 package com.hungry_bubbles;
 
 import java.util.Random;
 
 
 /**
  * Factory class for the creation of new BubbleThreads with random bubble
  * starting positions around the outside of the screen in the virtual 
  * padding area.
  * 
  * @author Timothy Heard, Shaun DeVos, John O'Brien, Mustafa Al Salihi
  */
 public class BubbleFactory
 {
 	private enum SIDE {LEFT, RIGHT, TOP, BOTTOM}
 
 	private static final int LEFT_ANGLE_OF_MOTION = 0;
 	private static final int RIGHT_ANGLE_OF_MOTION = 180;
 	private static final int TOP_ANGLE_OF_MOTION = 270;
 	private static final int BOTTOM_ANGLE_OF_MOTION = 90;;
 	
 	private NonBlockingReadQueue<UpdateRequest> updatesQueue;
 	private int screenHeight, screenWidth, virtualPadding;
 	private Random random;
 	
 	public BubbleFactory(NonBlockingReadQueue<UpdateRequest> updateRequests, 
 		int screenHeight , int screenWidth, int virtualPadding)	
 	{
 		this.updatesQueue = updateRequests;
 		this.screenHeight = screenHeight;
 		this.screenWidth = screenWidth;
 		this.virtualPadding = virtualPadding;
 		
 		random = new Random(); 
 	}
 	
 	public void startNewBubble(int color)
 	{
 		SIDE side = pickRandomSide();
 		BubbleData bubbleData = initRandomBubble(side, color);
 		
 		int angleOfMotion = getAngleOfMotionFromSide(side);
 		
 		
 		BubbleThread bubbleThread = new BubbleThread(updatesQueue, bubbleData, 
 			angleOfMotion, screenWidth, screenHeight, virtualPadding); 
 		
 		Thread bubbleRunner = new Thread(bubbleThread);
 		bubbleRunner.start();
 		
 		/* TODO: Remove
 		 int Max = AppInfo.MAX_RADIUS;
 		 int Min = AppInfo.MIN_RADIUS;
 		 int Range_Size = Min + (int)(Math.random() * ((Max - Min) + 1));
 		
 		 //  Virtual board
 		 int Y_Position = (int)(Math.random() * (AppInfo.MAX_RADIUS)); 
 		 int X_Position = (int)(Math.random() * (AppInfo.MAX_RADIUS));
 		
 		 // update color
		BubbleData bubble = new BubbleData(Color.BLACK, X_Position, Y_Position, Range_Size);
 		 
 		 if (Range_Size > AppInfo.PLAYER_STARTING_RADIUS)
 		 {
 			BubbleData.updateColor( bubble  , Color.RED);
 		 }
 		 
 		 if (Range_Size < AppInfo.PLAYER_STARTING_RADIUS)
 		 {
 			 BubbleData.updateColor( bubble  , Color.BLUE);
 		 }
 		 */
 	}
 	
 	private SIDE pickRandomSide()
 	{
 		SIDE randomSide;
 		switch(random.nextInt(4))
 		{
 		case 0:
 			randomSide = SIDE.LEFT;
 			break;
 		case 1:
 			randomSide = SIDE.RIGHT;
 			break;
 		case 2:
 			randomSide = SIDE.TOP;
 			break;
 		case 3:
 		default:
 			randomSide = SIDE.BOTTOM;
 		}
 		
 		return randomSide;
 	}
 	
 	/**
 	 * Creates and returns the data for a random bubble with the given 
 	 * {@code color} which will have a radius between AppInfo.MIN_RADIUS and 
 	 * AppInfo.MAX_RADIUS and a starting position within the virtual padding
 	 * area surrounding the physical screen to whichever side of the screen
 	 * is indicated by the {@code side} parameter in order to prevent the 
 	 * bubble from appearing on top of the player's bubble.
 	 */
 	private BubbleData initRandomBubble(SIDE side, int color)
 	{
 		BubbleData bubbleData;
 		
 		// Generate a random starting radius from AppInfo.MIN_RADIUS to 
 		// AppInfo.MAX_RADIUS, with the call to random.nextInt() returning a
 		// pseudo-random integer from 0 (inclusive to 
 		// AppInfo.MAX_RADIUS - AppInfo.MIN_RADIUS (exclusive), which is why
 		// AppInfo.MIN_RADIUS must be added to arrive a value between 
 		// AppInfo.MIN_RADIUS and AppInfo.MAX_RADIUS
 		int radius = random.nextInt(AppInfo.MAX_RADIUS - AppInfo.MIN_RADIUS) + 
 			AppInfo.MIN_RADIUS;
 		
 		float startX, startY;
 		
 		// Generate "random" starting coordinates for the new bubble on the
 		// specified side of the board in the virtual padding region, with
 		// no bubbles being spawned in the corners in order to simplify
 		// giving the bubbles a starting direction which will take them out
 		// of the spawning area and onto the screen. The bubble positions
 		// will also be centered either horizontally or vertically in the
 		// spawning area, depending on which direction is smaller (so
 		// bubbles to the top and bottom of the screen will be centered
 		// vertically and all other bubbles will be centered horizontally)
 		// in order to ensure that the bubble will be fully contained in
 		// the spawning area when it is created.
 		switch(side)
 		{
 		case LEFT:
 			 startX = virtualPadding / 2;
 			 startY = (random.nextFloat() * screenHeight) + virtualPadding;
 			break;
 		case RIGHT:
 			 startX = screenWidth + virtualPadding + 
 			 	(virtualPadding / 2);
 			 
 			 startY = (random.nextFloat() * screenHeight) + virtualPadding;
 			break;
 		case TOP:
 			 startX = (random.nextFloat() * screenWidth) + virtualPadding;
 			 startY = virtualPadding / 2;
 			break;
 		case BOTTOM:
 		default:
 			 startX = (random.nextFloat() * screenWidth) + virtualPadding;
 			 startY = screenHeight + virtualPadding + 
 				(virtualPadding / 2);
 		}
 		
 		return new BubbleData(color, startX, startY, radius);
 	}
 	
 	/**
 	 * Returns the initial angle of motion that bubbles starting on the given
 	 * {@code SIDE} should have. Note that angles based off of the positive 
 	 * x-axis going clockwise, so a 0 degree angle will be towards the right of
 	 * the screen while a 90 degree angle will be towards the bottom of the 
 	 * screen.
 	 */
 	private int getAngleOfMotionFromSide(SIDE side)
 	{
 		int angle;
 		
 		switch(side)
 		{
 		case LEFT:
 			angle = LEFT_ANGLE_OF_MOTION;
 			break;
 		case RIGHT:
 			angle = RIGHT_ANGLE_OF_MOTION;
 			break;
 		case TOP:
 			angle = TOP_ANGLE_OF_MOTION;
 			break;
 		case BOTTOM:
 		default:
 			angle = BOTTOM_ANGLE_OF_MOTION;
 		}
 		
 		return angle;
 	}
 }
