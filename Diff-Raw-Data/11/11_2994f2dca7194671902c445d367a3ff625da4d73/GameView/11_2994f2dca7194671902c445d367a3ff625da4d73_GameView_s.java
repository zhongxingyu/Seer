 package com.project.gemswapper;
 
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.os.SystemClock;
 import android.util.DisplayMetrics;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class GameView extends View {
 
 	public final static String SCORE = "com.project.gemswapper.SCORE";
 	public final static String COUNTERS = "com.project.gemswapper.COUNTERS";
 	
 	private final int GRIDSIZE = 8;
 	private final int NUM_PATTERNS = 32;
 	private int timeTreshold;
 	private int elapsedTime;
 	private long currentTime; 
 	private long startTime;
 	
 	Paint paint;
 	Resources res;
 	Bitmap grid;
 	Bitmap background;
 	Context mContext;
 	Tile tiles[][];
 	Random randomGen;
 	
 	private float mScale;
 	private float tileScale;
 	private int mScore;
 	
 	private int gridOffset;
 	private int gridOffsetMotionEvent;
 	private int tileSize;
 	
 	private float mViewWidth;
 	private float mViewHeight;
 	
 	private int startX;
 	private int startY;
 	private int endX;
 	private int endY;
 	
 	private int patterns[][];
 	private int patternTypes[][];
 	
 	private final int FIVE_T_SCORE = 3873;
 	private final int FOUR_T_SCORE = 1761;
 	private final int FIVE_ROW_SCORE = 1123;
 	private final int THREE_T_SCORE  = 725;
 	private final int CORNER_SCORE = 725;
 	private final int FOUR_ROW_SCORE = 475;
 	private final int THREE_ROW_SCORE = 379;
 	
 	private boolean dragStarted;
 	
 	public Timer timer;
 	
 	SoundPool sounds;
 	private int sFailure;
 	private int sSuccess;
 	
 	public GameView(Context context) {
 		super(context);
 		paint = new Paint();
 		tiles = new Tile[8][8];
 		patterns = new int[NUM_PATTERNS][];
 		patternTypes = new int[7][2];		// [i][0] = The counter for how many of this type solved this round.
 											// [i][1] = The score given for the pattern type. 
 		
 		timeTreshold = 25;
 
 		startTime = SystemClock.uptimeMillis();
 		
 		fillPatterns();
 		mContext = context;
 		res = this.getResources();
 		grid = BitmapFactory.decodeResource(res,  R.drawable.grid_01);
 		background = BitmapFactory.decodeResource(res, R.drawable.background);
 		randomGen = new Random();
 		
 		//get width and height of screen for scaling
 		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
 		mViewWidth = metrics.widthPixels;
 		mViewHeight = metrics.heightPixels;
 		mScale = mViewWidth / 720;
 		
 		gridOffset = (int)(mViewHeight/(mScale) - mViewWidth/(mScale));
 		tileSize = (int) (mViewWidth / 8);
 		
 		gridOffsetMotionEvent = (int)(mViewHeight - mViewWidth);
 		
 		System.out.println(gridOffset);
 		System.out.println(mViewHeight);
 		System.out.println(mViewWidth);
 		dragStarted = false;
 		
 		startX = -1;
 		startY = -1;
 		endX = -1;
 		endY = -1;
 		
 		mScore = 0;
 		
 		sounds = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
 		sFailure = sounds.load(mContext, R.raw.failure, 1);
 		sSuccess = sounds.load(mContext, R.raw.success, 1);
 		
 
 		tileScale = ( 90 - ( mScale * 90 ) ) / 2;
 		
 		fillGrid();
 		
 		timer = new Timer();
 		
 		TimerTask task = new TimerTask(){	// The timer runs every 500 ms and updates the elapsed time. 
 											// In doing so it also invalidates the screen so the time remaining
 			@Override						// is appropriately rendered. 
 			public void run()
 			{
 				currentTime = SystemClock.uptimeMillis();
 				elapsedTime = (int) ((currentTime - startTime) / 1000);
 				
 				postInvalidate();
 				
 				if(elapsedTime >= timeTreshold)
 				{
 					endGame();
 				}
 				
 			}
 		};
 		
 		timer.schedule(task, 0, 500);
 	}
 	
 	// Patterns are defined as [Above, Above, Left, Left, CENTER, Right, Right, Down, Down, SUMNEEDED, PATTERNTYPE] with 1's and 0's 
 	// corresponding to the pattern we want to match. e.g [1, 1, 0, 0, 1, 0, 0, 0, 0, 3, TYPE_THREE_ROW] for 3 in a row starting from the center node
 	// and going up, with a type of TYPE_THREE_ROW. 
 	
 	// Patterns should be added in descending order of SUM needed. 
 	private void fillPatterns()
 	{	
 		final int TYPE_FIVE_T = 0;
 		final int TYPE_FOUR_T = 1;
 		final int TYPE_FIVE_ROW = 2;
 		final int TYPE_THREE_T = 3;
 		final int TYPE_CORNER = 4;
 		final int TYPE_FOUR_ROW = 5;
 		final int TYPE_THREE_ROW = 6;
 		
 		// Five-T shapes.
 		patterns[0] = new int[] {1, 1, 1, 1, 1, 1, 1, 0, 0, 7, TYPE_FIVE_T}; // Left, right, up
 		patterns[1] = new int[] {0, 0, 1, 1, 1, 1, 1, 1, 1, 7, TYPE_FIVE_T}; // left, right, down
 		patterns[2] = new int[] {1, 1, 1, 1, 1, 0, 0, 1, 1, 7, TYPE_FIVE_T}; // up, down, left
 		patterns[3] = new int[] {1, 1, 0, 0, 1, 1, 1, 1, 1, 7, TYPE_FIVE_T}; // up, down, right.
 		
 		// Four-T Shapes
 		patterns[4]  = new int[] {1, 1, 0, 1, 1, 1, 1, 0, 0, 6, TYPE_FOUR_T}; // up, 1 left, 2 right. 
 		patterns[5]  = new int[] {1, 1, 1, 1, 1, 1, 0, 0, 0, 6, TYPE_FOUR_T}; // up, 2 left, 1 right. 
 		patterns[6]  = new int[] {0, 0, 0, 1, 1, 1, 1, 1, 1, 6, TYPE_FOUR_T}; // down, 1 left, 2 right.
 		patterns[7]  = new int[] {0, 0, 1, 1, 1, 1, 0, 1, 1, 6, TYPE_FOUR_T}; // down, 2 left, 1 right.
 		patterns[8]  = new int[] {0, 1, 1, 1, 1, 0, 0, 1, 1, 6, TYPE_FOUR_T}; // 1 up, 2 left, 2 down.
 		patterns[9]  = new int[] {1, 1, 1, 1, 1, 0, 0, 1, 0, 6, TYPE_FOUR_T}; // 2 up, 2 left, 1 down. 
 		patterns[10] = new int[] {0, 1, 0, 0, 1, 1, 1, 1, 1, 6, TYPE_FOUR_T}; // 1 up, 2 right, 2 down. 
 		patterns[11] = new int[] {1, 1, 0, 0, 1, 1, 1, 1, 0, 6, TYPE_FOUR_T}; // 2 up, 2 right, 1 down. 
 		
 		// Five in a row.
 		patterns[12] = new int[] {1, 1, 0, 0, 1, 0, 0, 1, 1, 5, TYPE_FIVE_ROW}; // up and down
 		patterns[13] = new int[] {0, 0, 1, 1, 1, 1, 1, 0, 0, 5, TYPE_FIVE_ROW}; // left and right
 		
 		// Three-T shapes
 		patterns[14] = new int[] {0, 0, 0, 1, 1, 1, 0, 1, 1, 5, TYPE_THREE_T}; // 1 left, 1 right, 2 down
 		patterns[15] = new int[] {0, 1, 0, 0, 1, 1, 1, 1, 0, 5, TYPE_THREE_T}; // 1 up, 1 down, 2 right.
 		patterns[16] = new int[] {0, 1, 1, 1, 1, 0, 0, 1, 0, 5, TYPE_THREE_T}; // 1 up, 1 down, 2 left. 
 		patterns[17] = new int[] {1, 1, 0, 1, 1, 1, 0, 0, 0, 5, TYPE_THREE_T}; // 1 left, 1 right, 2 up. 
 		
 		// Corner shapes
 		patterns[18] = new int[] {0, 0, 1, 1, 1, 0, 0, 1, 1, 5, TYPE_CORNER}; // left, down
 		patterns[19] = new int[] {0, 0, 0, 0, 1, 1, 1, 1, 1, 5, TYPE_CORNER}; // right, down
 		patterns[20] = new int[] {1, 1, 1, 1, 1, 0, 0, 0, 0, 5, TYPE_CORNER}; // left, up
 		patterns[21] = new int[] {1, 1, 0, 0, 1, 1, 1, 0, 0, 5, TYPE_CORNER}; // right, up
 		
 		// Four in a row
 		patterns[22] = new int[] {0, 0, 1, 1, 1, 1, 0, 0, 0, 4, TYPE_FOUR_ROW}; // 2 left, 1 right
 		patterns[23] = new int[] {0, 0, 0, 1, 1, 1, 1, 0, 0, 4, TYPE_FOUR_ROW}; // 1 left, 2 right
 		patterns[24] = new int[] {1, 1, 0, 0, 1, 0, 0, 1, 0, 4, TYPE_FOUR_ROW}; // 2 up, 1 down
 		patterns[25] = new int[] {0, 1, 0, 0, 1, 0, 0, 1, 1, 4, TYPE_FOUR_ROW}; // 1 up, 2 down
 				
 		// Three in a row
 		patterns[26] = new int[] {0, 0, 0, 1, 1, 1, 0, 0, 0, 3, TYPE_THREE_ROW}; // 1 left, 1 right
 		patterns[27] = new int[] {0, 0, 0, 0, 1, 1, 1, 0, 0, 3, TYPE_THREE_ROW}; // 2 right
 		patterns[28] = new int[] {0, 0, 1, 1, 1, 0, 0, 0, 0, 3, TYPE_THREE_ROW}; // 2 left
 		patterns[29] = new int[] {0, 1, 0, 0, 1, 0, 0, 1, 0, 3, TYPE_THREE_ROW}; // 1 up, 1 down
 		patterns[30] = new int[] {0, 0, 0, 0, 1, 0, 0, 1, 1, 3, TYPE_THREE_ROW}; // 2 down
 		patterns[31] = new int[] {1, 1, 0, 0, 1, 0, 0, 0, 0, 3, TYPE_THREE_ROW}; // 2 up
 		
 		for(int i = 0; i < 7; ++i)
 		{
 			patternTypes[i][0] = 0;
 		}
 		
 		patternTypes[TYPE_FIVE_T][1] = FIVE_T_SCORE;
 		patternTypes[TYPE_FOUR_T][1] = FOUR_T_SCORE;
 		patternTypes[TYPE_FIVE_ROW][1] = FIVE_ROW_SCORE;
 		patternTypes[TYPE_THREE_T][1] = THREE_T_SCORE;
 		patternTypes[TYPE_CORNER][1] = CORNER_SCORE;
 		patternTypes[TYPE_FOUR_ROW][1] = FOUR_ROW_SCORE;
 		patternTypes[TYPE_THREE_ROW][1] = THREE_ROW_SCORE;
 	}
 	
 	private void fillGrid()
 	{
 		int typeTemp;
 		boolean goodType = true;
 		
 		for(int y = 0; y < GRIDSIZE; ++y)
 		{
 			for(int x = 0; x < GRIDSIZE; ++x)
 			{
 				do
 				{
 					goodType = true;
 					typeTemp = randomGen.nextInt(5);
 					
 					if(x >= 2)
 					{
 						if(tiles[y][x - 1].getType() == typeTemp && tiles[y][x - 2].getType() == typeTemp)
 						{
 							goodType = false;
 						}
 					}
 					
 					if(y >= 2)
 					{
 						if(tiles[y - 1][x].getType() == typeTemp && tiles[y - 2][x].getType() == typeTemp)
 						{
 							goodType = false;
 						}
 					}
 					
 				}while(!goodType);
 				tiles[y][x] = new Tile(mContext, x * 90 + (int)tileScale , y * 90 + (int)(tileScale + gridOffset), typeTemp);
 			}
 		}
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event)
 	{
 		int action = event.getAction();
 
 		if(action == MotionEvent.ACTION_DOWN)
 		{
 			
 		}
 		else if(action == MotionEvent.ACTION_MOVE)
 		{
 			if(!dragStarted)
 			{
 				int x = (int) event.getX();
 				int y = (int) event.getY();
 				
 				System.out.println(gridOffsetMotionEvent);
 				
 				if(y > gridOffsetMotionEvent)
 				{
 					startX = x / tileSize;
 					startY = (y - gridOffsetMotionEvent) / tileSize;
 					
 					System.out.print(startY);
 					System.out.print(", ");
 					System.out.println(startX);
 					
 					dragStarted = true;
 				}
 			} 
 			else
 			{
 				int x = (int) event.getX();
 				int y = (int) event.getY();
 				
 				if(y > gridOffsetMotionEvent)
 				{
 					endX = x / tileSize;
 					endY = (y - gridOffsetMotionEvent) / tileSize;
 					
 //					System.out.print("End: ");
 //					System.out.print(endY);
 //					System.out.print(", ");
 //					System.out.println(endX);
 				}
 			}
 		}
 		else if(action == MotionEvent.ACTION_UP)
 		{
 			dragStarted = false;
 			
 //			System.out.print(startX);
 //			System.out.print(", ");
 //			System.out.print(startY);
 //			System.out.print(", ");
 //			System.out.print(endX);
 //			System.out.print(", ");
 //			System.out.println(endY);
 			
 			boolean failure = false;
 			
 			if(startX != endX || startY != endY)
 			{	
 				if(endX > startX)	// drag right.
 				{
 					swapTile(startX, startY, 1, 0);
 					if(checkMatch(startY, startX + 1, tiles[startY][startX + 1].getType()))
 					{
 						failure = false;
 					}
 					else if(checkMatch(startY, startX, tiles[startY][startX].getType()))
 					{
 						failure = false;
 					}
 					else
 					{
 						failure = true;
 					}
 					
 					if(!failure)
 					{
 						playSound(sSuccess);
 					}
 					else
 					{
 						swapTile(startX, startY, 1, 0);
 						playSound(sFailure);
 					}
 				}
 				else if(endX < startX) // drag left
 				{
 					
 					swapTile(startX, startY, -1, 0);
 					if(checkMatch(startY, startX - 1, tiles[startY][startX - 1].getType()))
 					{
 						failure = false;
 					}
 					else if(checkMatch(startY, startX, tiles[startY][startX].getType()))
 					{
 						failure = false;
 					}
 					else
 					{
 						failure = true;
 					}
 					
 					if(!failure)
 					{
 						playSound(sSuccess);
 					}
 					else
 					{
 						swapTile(startX, startY, -1, 0);
 						playSound(sFailure);
 					}		
 				}
 				else if(endY > startY) // drag down.
 				{
 					swapTile(startX, startY, 0, 1);
 					
 					if(checkMatch(startY + 1, startX, tiles[startY + 1][startX].getType()))
 					{
 						failure = false;
 					}
 					else if(checkMatch(startY, startX, tiles[startY][startX].getType()))
 					{
 						failure = false;
 					}
 					else
 					{
 						failure = true;
 					}
 					
 					if(!failure)
 					{
 						playSound(sSuccess);
 					}
 					else
 					{
 						swapTile(startX, startY, 0, 1);
 						playSound(sFailure);
 					}
 				}
 				else if(endY < startY) // drag up
 				{
 					swapTile(startX, startY, 0, -1);
 					
 					if(checkMatch(startY - 1, startX, tiles[startY - 1][startX].getType()))
 					{
 						failure = false;
 					}
 					else if(checkMatch(startY, startX, tiles[startY][startX].getType()))
 					{
 						failure = false;
 					}
 					else
 					{
 						failure = true;
 					}
 					
 					if(!failure)
 					{
 						playSound(sSuccess);
 					}
 					else
 					{
 						swapTile(startX, startY, 0, -1);
 						playSound(sFailure);
 					}
 				}
 			}
 			
 			this.invalidate();		// Force redraw. 
 		}
 		return true;
 	}
 	
 	private void swapTile(int startX, int startY, int x, int y)
 	{
 		Tile temp = new Tile(mContext, tiles[startY][startX].getXPos(), tiles[startY][startX].getYPos(), tiles[startY + y][startX + x].getType());
 		tiles[startY + y][startX + x] = new Tile(mContext, tiles[startY + y][startX + x].getXPos(), tiles[startY + y][startX + x].getYPos(), tiles[startY][startX].getType());
 		tiles[startY][startX] = temp;
 	}
 	
 	@Override
 	protected void onDraw(Canvas canvas)
 	{
 		System.out.println("Drawing...");
 		// Fill screen white
 		paint.setStyle(Paint.Style.FILL);
 		paint.setColor(Color.WHITE);
 		canvas.drawPaint(paint);
 		
 		paint.setAntiAlias(true);
 		paint.setColor(Color.BLACK);
 		paint.setTextSize(50);
 		
 		canvas.scale(mScale, mScale);
 
 		canvas.drawBitmap(background, 0, 0, paint);
 		canvas.drawBitmap(grid, 0, gridOffset, paint);
 		
 		canvas.drawText(mContext.getString(R.string.game_time) + ": " + String.valueOf(timeTreshold - elapsedTime), 10, 100, paint);
 		canvas.drawText(mContext.getString(R.string.game_score) + ": " + String.valueOf(mScore), 10, 50, paint); // Draw score.
 		
 		for(int i = 0; i < GRIDSIZE; ++i)
 		{
 			for(int j = 0; j < GRIDSIZE; ++j)
 			{
 				tiles[i][j].draw(canvas);
 			}
 		}
 	}
 	
 	private boolean checkMatch(int y, int x, int type)
 	{
 		int typeMatch[] = {0, 0, 0, 0, 1, 0, 0, 0, 0}; // The center, is always a match.
 		
 //		System.out.print("(X, y, type): ");
 //		System.out.print(x);
 //		System.out.print(", ");
 //		System.out.print(y);
 //		System.out.print(", ");
 //		System.out.println(type);
 		
 		// Fills the typematch array with matches..
 		if(x > 0 && tiles[y][x - 1].getType() == type)
 		{
 			typeMatch[3] = 1;
 			
 			if(x > 1 && tiles[y][x - 2].getType() == type)
 			{
 				typeMatch[2] = 1;
 			}
 		}
 		
 		if(x < GRIDSIZE - 1 && tiles[y][x + 1].getType() == type)
 		{
 			typeMatch[5] = 1;
 			
 			if(x < GRIDSIZE - 2 && tiles[y][x + 2].getType() == type)
 			{
 				typeMatch[6] = 1;
 			}
 		}
 		
 		if(y > 0 && tiles[y - 1][x].getType() == type)
 		{
 			typeMatch[1] = 1;
 			
 			if(y > 1 && tiles[y - 2][x].getType() == type)
 			{
 				typeMatch[0] = 1;
 			}
 		}
 		
 		if(y < GRIDSIZE - 1 && tiles[y + 1][x].getType() == type)
 		{
 			typeMatch[7] = 1;
 			
 			if(y < GRIDSIZE - 2 && tiles[y + 2][x].getType() == type)
 			{
 				typeMatch[8] = 1;
 			}
 		}
 		
 		int sum = 0;
 		
 //		System.out.print("TypeMatch: ");
 //		System.out.print(typeMatch[0]);
 //		System.out.print(", ");
 //		System.out.print(typeMatch[1]);
 //		System.out.print(", ");
 //		System.out.print(typeMatch[2]);
 //		System.out.print(", ");
 //		System.out.print(typeMatch[3]);
 //		System.out.print(", ");
 //		System.out.print(typeMatch[4]);
 //		System.out.print(", ");
 //		System.out.print(typeMatch[5]);
 //		System.out.print(", ");
 //		System.out.print(typeMatch[6]);
 //		System.out.print(", ");
 //		System.out.print(typeMatch[7]);
 //		System.out.print(", ");
 //		System.out.println(typeMatch[8]);
 
 		
 		for(int i = 0; i < NUM_PATTERNS; ++i)
 		{
 			// Take the sum of the ANDing of the pattern and the current match.
 			sum = typeMatch[0] * patterns[i][0] +
 				  typeMatch[1] * patterns[i][1] +
 				  typeMatch[2] * patterns[i][2] +
 				  typeMatch[3] * patterns[i][3] +
 				  typeMatch[4] * patterns[i][4] +
 				  typeMatch[5] * patterns[i][5] +
 				  typeMatch[6] * patterns[i][6] +
 				  typeMatch[7] * patterns[i][7] + 
 				  typeMatch[8] * patterns[i][8];
 			
 //			System.out.print("Pattern ");
 //			System.out.print(i);
 //			System.out.print(": ");
 //			System.out.print(patterns[i][0]);
 //			System.out.print(", ");
 //			System.out.print(patterns[i][1]);
 //			System.out.print(", ");
 //			System.out.print(patterns[i][2]);
 //			System.out.print(", ");
 //			System.out.print(patterns[i][3]);
 //			System.out.print(", ");
 //			System.out.print(patterns[i][4]);
 //			System.out.print(", ");
 //			System.out.print(patterns[i][5]);
 //			System.out.print(", ");
 //			System.out.print(patterns[i][6]);
 //			System.out.print(", ");
 //			System.out.print(patterns[i][7]);
 //			System.out.print(", ");
 //			System.out.println(patterns[i][8]);
 			
 			
 			if(sum >= patterns[i][9])		// If we have a pattern. 
 			{
 				int patternType = patterns[i][10];
 				patternTypes[patternType][0]++; 		// Increments the counter for this patternType. 
 				mScore += patternTypes[patternType][1];	// Adds to the score. 
 				
 				clearOut(i, x, y);			// Moves everything above, down, and fills in new tiles in the empty positions. 
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	private void clearOut(int pattern, int x, int y)
 	{
		tiles[y - patterns[pattern][0]][x].setType(-1);	
 		tiles[y - patterns[pattern][1]][x].setType(-1);	// If the pattern bit is set, set the type to -1.
 		tiles[y + patterns[pattern][7]][x].setType(-1);	// if the bit isn't set, the center will be set to -1 instead,
		tiles[y + patterns[pattern][8]][x].setType(-1);	// Which works for all patterns. 
 		
 		tiles[y][x + patterns[pattern][5]].setType(-1);
		tiles[y][x + patterns[pattern][6]].setType(-1);
 		tiles[y][x - patterns[pattern][3]].setType(-1);
		tiles[y][x - patterns[pattern][2]].setType(-1);
 		
 		tiles[y][x].setType(-1);
 		
 		// Checking from the bottom up for matches of type -1. 
 		for(int i = GRIDSIZE - 1; i >= 0; --i)
 		{
 			for(int j = 0; j < GRIDSIZE; ++j)
 			{
 				if(tiles[i][j].getType() == -1)
 				{
 					int tempY = i;
 					int verticalCount = 1;
 					
 					while(tempY > 0 && tiles[tempY - 1][j].getType() == -1)
 					{
 						--tempY;
 						++verticalCount;
 					}
 					
 					if(tempY == 0)
 					{
 						// create verticalCount new tiles...
 						for(int v = verticalCount - 1; v >= 0; --v)
 						{
 							tiles[v][j] = new Tile(mContext, j * 90 + (int)tileScale , v * 90 + (int)(tileScale + gridOffset), randomGen.nextInt(5));
 							checkMatch(v, j, tiles[v][j].getType());
 						}
 					}
 					else
 					{	
 						swapTile(j, i, 0, -verticalCount); // Swap down the one above (above all the matches). 
 						checkMatch(i, j, tiles[i][j].getType());
 					}
 				}
 			}
 		}
 	}
 	
 	private void playSound(int type)
 	{
 		sounds.play(type, 1.0f, 1.0f, 0, 0, 1.0f);
 	}
 	
 	private void endGame()
 	{
 		timer.cancel();
 		
 		Context context = getContext();
 		Intent intent = new Intent(context, EndgameActivity.class);
 		
 		int counters[] = new int[8];
 		
 		for (int i = 0; i < patternTypes.length; i++)
 		{
 			counters[i] = patternTypes[i][0];
 		}
 		counters[patternTypes.length] = mScore;
 		
 		String score = String.valueOf(mScore);
 		
 		intent.putExtra(SCORE, score);
 		intent.putExtra(COUNTERS, counters);
 		
 		context.startActivity(intent);
 	}
 }
