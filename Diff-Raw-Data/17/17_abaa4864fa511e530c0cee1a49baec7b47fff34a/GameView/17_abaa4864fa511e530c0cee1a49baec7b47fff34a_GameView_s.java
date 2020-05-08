 package com.amazon.dinorama;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class GameView extends SurfaceView implements SurfaceHolder.Callback {
 	private SurfaceHolder sh;
 	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 	
 	private Resources res;
 	private GameThread thread;
 	
 	private Canvas canvas;
 	
 	private ArrayList<DisplayableObject> objects = new ArrayList<DisplayableObject>(); // drawn in order
 	private TouchButton[] buttons = new TouchButton[4];
 	private Player player = null;
 	private HealthBarObject[] healthCon = new HealthBarObject[2];
 	private HealthBarState[] health = new HealthBarState[2];
 	
 	public GameView(Context context) {
 		super(context);
 		sh = getHolder();
 		sh.addCallback(this);
 		paint.setColor(Color.BLUE);
 		paint.setStyle(Style.FILL);
 		
 		res = getResources();
 		
 		// adding the callback (this) to the surface holder to intercept events
 		getHolder().addCallback(this);
 		// make the GamePanel focusable so it can handle events
 		setFocusable(true);
 		
 		// create the game loop thread
 		thread = new GameThread(getHolder(), this);
 	}
 	
 	protected void createButtons() {
 		buttons[0] = new TouchButton(res, 1120, 480, TouchButton.TouchButtonDirection.HI_ATK);
 		buttons[1] = new TouchButton(res, 32, 644, TouchButton.TouchButtonDirection.LEFT);
 		buttons[2] = new TouchButton(res, 1120, 644, TouchButton.TouchButtonDirection.LO_ATK);
 		buttons[3] = new TouchButton(res, 192, 644, TouchButton.TouchButtonDirection.RIGHT);
 	}
 	
 	protected void createHealth() {
 		healthCon[0] = new HealthBarObject(res, PlayerEnum.PLAYER);
 		healthCon[1] = new HealthBarObject(res, PlayerEnum.AI);
 		health[0] = new HealthBarState(res, PlayerEnum.PLAYER);
 		health[1] = new HealthBarState(res, PlayerEnum.AI);
 	}
 	
 	public void init() {
 		player = new Player(res, 0, 0);
 		createButtons();
 		createHealth();
 		synchronized(objects) {
 //			for (int i=0; i<100; i++)
 //				objects.add(new TestItem(res, (int)(Math.random()*1280), (int)(Math.random()*800)));
 			DisplayableObject bg = new DisplayableObject(res, 0, 0);
 			bg.setImageDisplayed(LevelBitmapWrapper.bitmap);
 			bg.setScale(1.667);
 			objects.add(bg);
 			objects.add(player);
 			for (HealthBarObject o : healthCon)
 				objects.add(o);
 			for (HealthBarState o : health)
 				objects.add(o);
 			for (TouchButton o : buttons)
 				objects.add(o);
 		}
 		
 	}
 	
 	public void activate(boolean on) {
 		thread.setRunning(on);
 	}
 	
 	public void render(Canvas canvas) {	
 		// clear canvas
 		canvas.drawColor(Color.BLACK);
 		
 		// draw everything
 		synchronized(objects) {
 			for (DisplayableObject o : objects) {
 				canvas.save();
 				canvas.scale((float) o.getScaleX(), (float) o.getScaleY(), o.getX(), o.getY());
 				canvas.drawBitmap(o.getImageDisplayed(), o.getX(), o.getY(), null);
 				canvas.restore();
 			}
 		}
 //		synchronized(buttons) {
 //			for (TouchButton o : buttons) {
 //				System.err.println("!!! "+o.getX()+" , "+o.getY());
 //				canvas.drawBitmap(o.getImageDisplayed(), o.getX(), o.getY(), null);
 //			}
 //		}
 	}
 
 	public void update() {
 		// update everything
 		synchronized(objects) {
 			for (DisplayableObject o : objects)
 				o.update();
 		}
 	}
 	
 	public void surfaceCreated(SurfaceHolder holder) {
 		thread.setRunning(true);
 		thread.start();
 		init();
 	}
 	
 	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
 	}
 	
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		boolean retry = true;
 		while (retry) {
 			try {
 				thread.join();
 				retry = false;
 			} catch (InterruptedException e) {
 				// try again shutting down the thread
 			}
 		}
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		double x = event.getX();
 		double y = event.getY();
 		if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
 			for (TouchButton b : buttons)
 				if (b.hit(x, y)) {
 					if (b.type == TouchButton.TouchButtonDirection.RIGHT)
 						player.moveRight();
 					if (b.type == TouchButton.TouchButtonDirection.LEFT)
 						player.moveLeft();
 					if (b.type == TouchButton.TouchButtonDirection.HI_ATK)
 						player.attackHigh();
 					if (b.type == TouchButton.TouchButtonDirection.LO_ATK)
 						player.attackLow();
 //					System.out.println(b);
 				}
 			return true;
		} else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
 			player.forceIdle();
 			for (TouchButton o : buttons) {
				o.release();
 			}
//			System.out.println("ACTION UP");
 		}
 		return false;
 	}
 }
