 package com.karlliu.combinephrase;
 
 import java.util.Random;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceHolder.Callback;
 import android.view.SurfaceView;
 
 /**
  * 
  * @author Himi
  * 
  */
public class MySurfaceView extends SurfaceView implements Callback, Runnable {
 	private SurfaceHolder sfh;
 
 	// different paint pen
 	private Paint paintDropArea;
 	private Paint paintWordBlock;
 	private Paint paintText;
 	private Paint paintBackGround;
 	private Paint paintWin;
 
 	private final int COLOR_OLIVER = 0xffcc9933;
 
 	// main UI thread related
 	private Thread th;
 	private boolean flag;
 	private Canvas canvas;
 	private int screenW, screenH;
 	private WordBlock[] mWB;
 	private FixedWordBlock[] fixedWB;
 	private boolean initialized = false;
 	private long svStartTime;
 	private long currentTime;
 	private long leftTime;
 
 	// constant
 	private final int randomBlockPlaceY = 200; // random word block initial Y position - below the Y lines
 	private final int textPaintSize = 30;
 	private final int winPaintSize = 80;
 	private final int charLengthX = 20;
 	private final int charHeightY = 40;
 	private final int xBlockPadding = 20; // word block x need to be longer to hold all chars
 	private final float textShiftX = 5.0f;
 	private final float textShiftY = textPaintSize;
 	private final float statusBarYPadding = 5.0f;
 	private final float statusBarXLeftPadding = charLengthX;
 
 	private Bitmap bmpBackGround;
 	private Bitmap bmpNext;
 	private Bitmap bmpRetry;
 	private int result;
 
 	// constant drop area
 	private final float lineSpace = 30.0f;
 	private final float columnSpace = 10.0f;
 	private final float dropAreaLeft = 10.0f;
 	private final float dropAreaTop = 10.0f;
 	private final float dropBoxWidth = charLengthX * 8.0f; // every drop box width = 20 * 8 =160 160*5=800
 	private final float dropBoxHeight = charHeightY;
 	private int dropBoxColumns = 5; // columns one line
 	private int TIMEOUT = 60000; // 60 seconds
 
 	private final int WIN = 2;
 	private final int FAIL = 1;
 	private final int ONGOING = 0;
 
 	/**
 	 * SurfaceView constructor
 	 */
 	public MySurfaceView(Context context) {
 		super(context);
 		sfh = this.getHolder();
 		sfh.addCallback(this);
 
 		// drop area paint
 		paintDropArea = new Paint();
 		paintDropArea.setColor(Color.YELLOW);
 		paintDropArea.setAntiAlias(true);
 
 		// paint word block
 		paintWordBlock = new Paint();
 		paintWordBlock.setColor(Color.GRAY);
 		paintWordBlock.setAntiAlias(true);
 
 		// paint the text
 		paintText = new Paint();
 		paintText.setColor(Color.WHITE);
 		paintText.setAntiAlias(true);
 		paintText.setTextSize(textPaintSize);
 
 		// paint background
 		paintBackGround = new Paint();
 		paintBackGround.setColor(COLOR_OLIVER);
 		paintBackGround.setAntiAlias(true);
 
 		// paint win or fail
 		paintWin = new Paint();
 		paintWin.setColor(Color.YELLOW);
 		paintWin.setTextSize(winPaintSize);
 		paintWin.setAntiAlias(true);
 
 		setFocusable(true);
 
		mWB = PhraseSplit.split();		
 
 		fixedWB = new FixedWordBlock[mWB.length];
 		for (int i = 0; i < fixedWB.length; i++) {
 			fixedWB[i] = new FixedWordBlock();
 		}
 
 		bmpBackGround = BitmapFactory.decodeResource(this.getResources(), R.drawable.background);
 		bmpNext = BitmapFactory.decodeResource(this.getResources(), R.drawable.next);
 		bmpRetry = BitmapFactory.decodeResource(this.getResources(), R.drawable.retry);
 		result = ONGOING;
 	}
 
 	/**
 	 * When SurfaceView is created, this function will be called
 	 */
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		screenW = this.getWidth(); // landscape width=854 height=480
 		screenH = this.getHeight();
 		Log.i("phrase","width=" + screenW + " height=" + screenH);
 		initializeRandomBlocks();
 		svStartTime = System.currentTimeMillis(); // record the serfaceView created time
 		flag = true;
 		// initialize the thread
 		th = new Thread(this);
 		// start the thread
 		th.start();
 	}
 
 	/**
 	 * Game draw
 	 */
 	public void myDraw() {
 		try {
 			canvas = sfh.lockCanvas();
 			if (canvas != null) {
 				canvas.drawColor(Color.BLACK);
 
 				canvas.drawBitmap(bmpBackGround, 0, 0, paintText);
 				
 				//draw timer
 				canvas.drawCircle(30, screenH - 30, 20, paintBackGround);
 				canvas.drawText(Long.toString(leftTime), 15, screenH - 20, paintText);
 
 				for (int i = 0; i < mWB.length; i++) {
 					// if locked, show gray
 					if (mWB[i].getOccupied() != -1)
 						paintWordBlock.setColor(Color.GRAY);
 					else
 						paintWordBlock.setColor(mWB[i].getColor());
 					canvas.drawRect(mWB[i].getBlockLeft(), mWB[i].getBlockTop(), mWB[i].getBlockRight(), mWB[i].getBlockBottom(), paintWordBlock);
 					canvas.drawText(mWB[i].getContent(), mWB[i].getBlockLeft() + textShiftX, mWB[i].getBlockTop() + textShiftY, paintText);
 				}
 
 				// draw clippers
 				drawDropArea();
 
 				if (result != ONGOING) {
 					if (result == WIN) {
 						paintWin.setColor(Color.YELLOW);
 						canvas.drawText("You Win", screenW / 3, screenH / 2, paintWin);
 						canvas.drawBitmap(bmpNext, screenW / 2, screenH / 2, paintWin);
 					} else if (result == FAIL) {
 						paintWin.setColor(Color.RED);
 						canvas.drawText("You FAIL", screenW / 3, screenH / 2, paintWin);
 						canvas.drawBitmap(bmpRetry, screenW / 2, screenH / 2, paintWin);
 					}
 				}
 			}
 		} catch (Exception e) {
 
 		} finally {
 			if (canvas != null)
 				sfh.unlockCanvasAndPost(canvas);
 		}
 	}
 
 	/**
 	 * Monitor screen touch event
 	 */
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		
 		//game finishes, no response needed
 		if (result == WIN || result == FAIL)
 			return true;
 		
 		// judge which word block is touched
 		if (event.getAction() == MotionEvent.ACTION_DOWN) {
 			int mostCloseWB = pickClosestWB(mWB, event.getX(), event.getY());
 			
 			updateWordBlock(mWB[mostCloseWB], event.getX(), event.getY());
 			mWB[mostCloseWB].setTouched(true);
 			
 			// if mWB is already set in fixedWB, release lock
 			if (mWB[mostCloseWB].getOccupied() != -1) {
 				fixedWB[mWB[mostCloseWB].getOccupied()].setOccupied(-1); // fixedWB is not occupied by any mWB
 				mWB[mostCloseWB].setOccupied(-1); // mWB is not set in any fixedWB
 			}
 
 		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
 			for (int i = 0; i < mWB.length; i++) {
 				if (mWB[i].getTouched()) {
 					updateWordBlock(mWB[i], event.getX(), event.getY());
 				}
 			}
 		} else if (event.getAction() == MotionEvent.ACTION_UP) {
 			for (int i = 0; i < mWB.length; i++) {
 				if (mWB[i].getTouched()) {
 					boolean movedInDropBox = false;
 					// release the finger and judge if the destination is within the fixedWB
 					for (int j = 0; j < fixedWB.length; j++) {
 						// touch point within fixed block or four points of moving block within fixed block
 						if ((fixedWB[j].getOccupied() == -1)
 								&& (judgeTouched(fixedWB[j], event.getX(), event.getY())
 										|| judgeTouched(fixedWB[j], mWB[i].getBlockLeft(), mWB[i].getBlockTop())
 										|| judgeTouched(fixedWB[j], mWB[i].getBlockLeft(), mWB[i].getBlockBottom())
 										|| judgeTouched(fixedWB[j], mWB[i].getBlockRight(), mWB[i].getBlockTop()) || judgeTouched(fixedWB[j],
 											mWB[i].getBlockRight(), mWB[i].getBlockBottom()))) {
 
 							updateWordBlock(mWB[i], fixedWB[j].getBlockLeft(), fixedWB[j].getBlockTop());
 							fixedWB[j].setOccupied(i); // fixedWB[j] is occupied by mWB[i]
 							mWB[i].setOccupied(j); // mWB[i] is set in fixedWB[j]
 
 							movedInDropBox = true;
 							// Log.i("phrase", "dropped in " + j + " drop box");
 							break;
 						}
 					}
 
 					// not moved in drop box
 					if (!movedInDropBox)
 						updateWordBlock(mWB[i], event.getX(), event.getY());
 
 					mWB[i].setTouched(false);
 					// Log.i("phrase", "up " + i + " changed");
 
 				}
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Monitor key down event
 	 */
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		return super.onKeyDown(keyCode, event);
 	}
 
 	/**
 	 * Game logic
 	 */
 	private void logic() {
 		// result judges where the program goes
 		if (result == WIN || result == FAIL)
 			return;
 
 		currentTime = System.currentTimeMillis();
 		leftTime = (TIMEOUT - currentTime + svStartTime) > 0 ? (TIMEOUT - currentTime + svStartTime)/1000 : 0;
 		if (leftTime == 0) {
 			result = FAIL;
 			return;
 		}
 
 		for (int i = 0; i < fixedWB.length; i++) {
 			if (fixedWB[i].getOccupied() == -1) {
 				// not all fixedWb are fixed
 				return;
 			}
 		}
 
 		for (int i = 0; i < fixedWB.length; i++) {
 			if (mWB[fixedWB[i].getOccupied()].getIndex() != i) {
 				// not all fixedWB index(0..n-1) equals to mWB index (1..n)
 				return;
 			}
 		}
 
 		result = WIN;
 	}
 
 	@Override
 	public void run() {
 		while (flag) {
 			long start = System.currentTimeMillis();
 			myDraw();
 			logic();
 			long end = System.currentTimeMillis();
 			try {
 				if (end - start < 50) {
 					Thread.sleep(50 - (end - start));
 				}
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * When SurfaceView status is changed, this function will be called
 	 */
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 	}
 
 	/**
 	 * When SurfaceView dies, this function will be called
 	 */
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		flag = false;
 	}
 
 	/**
 	 * Draw drop area clippers Actually the fixed word block can be invisible
 	 */
 	public void drawDropArea() {
 		float clipperXShift = 10;
 		float clipperYShift = 5;
 
 		// draw lines to hold clippers
 		for (int j = 0; j < fixedWB.length; j = j + dropBoxColumns) {
 			canvas.drawLine(0, fixedWB[j].getBlockTop(), screenW, fixedWB[j].getBlockTop(), paintBackGround);
 		}
 
 		for (int i = 0; i < fixedWB.length; i++) {
 			Clipper aClipper = new Clipper(fixedWB[i].getBlockLeft() + clipperXShift, fixedWB[i].getBlockTop() - clipperYShift);
 			canvas.drawPath(aClipper.getClipper(), paintDropArea);
 			canvas.drawCircle(aClipper.getHoleX(), aClipper.getHoleY(), aClipper.getRadius(), paintBackGround);
 		}
 	}
 
 	// change word block position
 	private void updateWordBlock(WordBlock aWb, float x, float y) {
 		aWb.setBlockLeft(x);
 		aWb.setBlockTop(y);
 		aWb.setBlockRight(x + aWb.getContent().length() * charLengthX + xBlockPadding);
 		aWb.setBlockBottom(y + charHeightY);
 	}
 
 	// judge word block is touched
 	private boolean judgeTouched(FixedWordBlock aWB, float x, float y) {
 		if (aWB.getBlockLeft() < x && x < aWB.getBlockRight() && aWB.getBlockTop() < y && y < aWB.getBlockBottom())
 			return true;
 		return false;
 	}
 	
 	// in case touch position is not in block range, auto pick one 
 	private int pickClosestWB(FixedWordBlock[] wbArray, float x, float y) {
 		int found = 0;
 		double distance = 0.0;
 		double tmpValue=0.0;
 		
 		for (int i=0;i<wbArray.length;i++) {
 			if (i==0)
 			{
 				distance = twoPointsDistance(wbArray[0].getBlockLeft(),wbArray[0].getBlockTop(),x,y);
 				continue;
 			}
 			
 			tmpValue = twoPointsDistance(wbArray[i].getBlockLeft(),wbArray[i].getBlockTop(),x,y);
 			
 			if (distance > tmpValue) {
 				distance = tmpValue;
 				found = i;
 			}				
 		}
 		
 		return found;
 	}
 	
 	private double twoPointsDistance(float x1, float y1, float x2, float y2) {
 		return Math.sqrt(Math.pow(Math.abs(x1 - x2), 2) + Math.pow(Math.abs(y1 - y2), 2));		
 	}
 
 	// initialized word blocks
 	public void initializeRandomBlocks() {
 		// Y start from 600, X starts from 0
 		if (initialized)
 			return;
 
 		ColorPalette cp = new ColorPalette();
 
 		Random rPosition = new Random();
 
 		for (int i = 0; i < mWB.length; i++) {
 			// prevent word block exceed the left, right, top and bottom limit
 			float x = rPosition.nextFloat() * screenW;
 			float y = rPosition.nextFloat() * (screenH - randomBlockPlaceY) + randomBlockPlaceY;
 			if (x < 0)
 				x = x + statusBarXLeftPadding;
 			else if (x > (screenW - mWB[i].getContent().length() * charLengthX))
 				x = screenW - mWB[i].getContent().length() * charLengthX - xBlockPadding;
 
 			if (y > (screenH - charHeightY))
 				y = screenH - charHeightY - statusBarYPadding;
 
 			updateWordBlock(mWB[i], x, y);
 			mWB[i].setColor(cp.getColor());
 		}
 
 		// initialize drop area box
 		for (int i = 0; i < fixedWB.length; i++) {
 			int row = i / dropBoxColumns; // word block in which line and column
 			int col = i % dropBoxColumns;
 
 			fixedWB[i].setBlockLeft(dropAreaLeft + col * dropBoxWidth + col * columnSpace);
 			fixedWB[i].setBlockTop(dropAreaTop + row * dropBoxHeight + row * lineSpace);
 			fixedWB[i].setBlockRight(fixedWB[i].getBlockLeft() + dropBoxWidth);
 			fixedWB[i].setBlockBottom(fixedWB[i].getBlockTop() + dropBoxHeight);
 		}
 
 		initialized = true;
 	}
 }
