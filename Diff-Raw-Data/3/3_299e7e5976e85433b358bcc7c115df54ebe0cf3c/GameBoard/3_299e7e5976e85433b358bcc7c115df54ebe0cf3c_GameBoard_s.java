 package com.hungry_bubbles;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.view.MotionEvent;
 
 /**
  * Manages the game logic.
  */
 public class GameBoard implements GameRenderer
 {
 	private GameView view;
 	private BubbleData playerData;
 	private List<BubbleData> opponents;
 	private int screenWidth, screenHeight, boardWidth, boardHeight;
 	private boolean initialized;
 	
 	public GameBoard()
 	{
 		// TODO: Add safety checks to ensure nothing happens until a view is registered
 		this.view = null;
 		playerData = null;
 		opponents = new ArrayList<BubbleData>();
 		initialized = false;
 	}
 
 	@Override
     public void renderGame(Canvas canvas)
     {
 		if(!initialized)
 		{
 			// Stores the size of the physical screen. This value only needs
 			// to be retrieved once since this activity is locked in horizontal
 			// orientation
 			screenWidth = canvas.getWidth();
 			screenHeight = canvas.getHeight();
 			
 			// The actual, effective size of the game board is 2 * MAX_RADIUS
 			// units larger than the physical screen in both directions. This
 			// additional area is used as a spawning area for new 
 			// computer-controlled bubbles so that bubbles will not spawn on 
 			// top of the player.
 			boardWidth = screenWidth + (2 * AppInfo.MAX_RADIUS); 
 			boardHeight = screenHeight + (2 * AppInfo.MAX_RADIUS);
 			
 			playerData = new BubbleData(Color.BLACK, screenWidth / 2, screenHeight / 2, AppInfo.PLAYER_STARTING_RADIUS);
 			initialized = true;
 		}
 
 		drawPlayer(canvas);
     }
 	
 	public int getScreenWidth()
 	{
 		return screenWidth;
 	}
 	
 	public int getScreenHeight()
 	{
 		return screenHeight;
 	}
 	
 	public int getBoardWidth()
 	{
 		return boardWidth;
 	}
 	
 	public int getBoardHeight()
 	{
 		return boardHeight;
 	}
 	
 	/**
 	 * Used to determine whether or not the point with the given x and y
 	 * coordinates is located on the physical screen.
 	 */
 	private boolean outOfBounds(float x, float y, float radius)
 	{
 		if((x + radius < 0) || (x - radius < 0) || (x + radius > screenWidth) || (x - radius > screenWidth) ||
 		   (y + radius < 0) || (y - radius < 0) || (y + radius > screenHeight) || (y - radius > screenHeight))
 		{
 			return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Determines whether or not the {@link MotionEvent} which resulted from a
 	 * user touch action is valid (i.e. is contained within the player's 
 	 * "bubble").
 	 * 
 	 * @param 	e	The {@link MotionEvent} caused by the player touching the 
 	 * 				screen.
 	 * 
 	 * @return	{@code true} if the player touched within their "bubble" and 
 	 * {@code false} otherwise.	
 	 */
 	private boolean isValidPlayerTouch(MotionEvent e)
 	{
 		float eventX = e.getX();
 		float eventY = e.getY();
 		int playerRadius = playerData.getRadius();
 		
 		if((Math.abs(playerData.getX() - eventX) <= playerRadius) && 
 		   (Math.abs(playerData.getY() - eventY) <= playerRadius) &&
 		   !outOfBounds(eventX, eventY, playerRadius))
 		{
 			return true;
 		}
 		
 		return false;
 	}
 	
 	private void drawPlayer(Canvas canvas)
 	{
 		Paint circlePaint = new Paint();
 		circlePaint.setColor(playerData.getColor());
 		canvas.drawCircle(playerData.getX(), playerData.getY(), playerData.getRadius(), circlePaint);
 	}
 
 	@Override
     public void registerView(GameView view)
     {
 		this.view = view;   
     }
 
 	@Override
     public boolean handlePlayerTouch(MotionEvent e)
     {
 		if(!isValidPlayerTouch(e))
 		{
 			return false;
 		}
 
 		playerData = BubbleData.updatePosition(playerData, e.getX(), e.getY());
 
 		// Causes the game view to be redrawn
 		view.invalidate();
 		
 		return true;
     }
 }
