 package com.relurori.breakout;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import com.relurori.engine.graphics.generic.Graphic;
 import com.relurori.engine.graphics.generic.Graphic.Coordinates;
 import com.relurori.engine.graphics.generic.Graphic.Speed;
 import com.relurori.engine.graphics.meta.Scale;
 import com.relurori.engine.graphics.premade.Ball;
 import com.relurori.engine.graphics.premade.Brick;
 import com.relurori.engine.graphics.premade.Paddle;
 import com.relurori.engine.graphics.shapes.meta.Collision;
 import com.relurori.engine.graphics.shapes.meta.Intersection;
 import com.relurori.engine.io.Joystick;
 import com.relurori.engine.main.MainThread;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.SoundPool;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 
 public class MainPanel extends SurfaceView implements SurfaceHolder.Callback {
 
 	private static final String TAG = MainPanel.class.getSimpleName();
 
 	private MainThread thread;
 
 	/*
 	 * TODO does it make sense to hold a reference to the calling Context and
 	 * Activity? Seems kinda sketch.
 	 */
 	private Context context;
 	private Activity activity;
 
 	private ArrayList<Ball> balls = new ArrayList<Ball>();
 	private ArrayList<Brick> bricks = new ArrayList<Brick>();
 	private Graphic currentGraphic = null;
 	private ArrayList<Paddle> paddles = new ArrayList<Paddle>();
 	private Joystick joystick = null;
 	
 	private PhysicsCache physicsCache = new PhysicsCache(); 
 	
 	private SoundPool sp = null;
 
 	private int STATE_INPROGRESS = 0;
 	private int STATE_EXIT = 1;
 	private int STATE_RETRY = 2;
 	private int STATE_RUN = STATE_INPROGRESS;
 	private boolean STATE_PAUSE = false;
 	private boolean DEBUG = true;
 	/** affects the victory condition */
 
 	private static float eventDownX = 0;
 	private static float eventDownY = 0;
 	private static float paddleDownX = 0;
 
 	private float gameWindowWidth;
 	
 	private Canvas canvas = null;
 	private String cacheMsg = null;
 	
 	private Scale scale;
 	
 	public MainPanel(Context context, Activity activity) {
 		super(context);
 		getHolder().addCallback(this);
 
 		// instantiate game loop thread
 		thread = new MainThread(getHolder(), this);
 
 		setFocusable(true);
 
 		this.context = context;
 		this.activity = activity;
 		
 		setupSoundPool();
 	}
 
 	private void setupSoundPool() {
 		sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
 		int sid = sp.load(getContext(), R.raw.hit, 1);
 		boolean loaded = false;
 		sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
 			
 			@Override
 			public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
 				soundPool.play(sampleId, 1.0f, 1.0f, 1, 0, 1.0f);
 				Log.d(TAG,"onLoadComplete sid=" + sampleId);
 				
 			}
 		});
 	}
 
 	public Canvas getCanvas() {
 		return canvas;
 	}
 	
 	@Override
 	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder arg0) {
 		thread.setRunning(true);
 		thread.start();
 
 		scale = new Scale(getWidth(),getHeight());
 		gameWindowWidth = scale.getScaledX(800);
 	}
 
 	
 	@Override
 	public void surfaceDestroyed(SurfaceHolder arg0) {
 		boolean retry = true;
 		Log.d(TAG,"Surface destroyed");
 		while (retry) {
 			try {
 				thread.join();
 				retry = false;
 			} catch (InterruptedException e) {
 				// try again
 			}
 		}
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		synchronized (thread.getSurfaceHolder()) {
 			if (event.getAction() == MotionEvent.ACTION_DOWN) {
 				eventDownX = event.getX();
 				eventDownY = event.getY();
 
 				if (joystick.pressed(event.getX(),event.getY())) {
 					joystick.setRestingState(false);
 				}
 			} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
 				float deltaY = event.getY() - eventDownY;
 				
 				if (joystick.getRestingState() == false) {
 					float movementRange = joystick.getMovementRangePercent()*scale.getHeight();
 					movementRange = scale.getScaledY(movementRange);
 					if (deltaY > movementRange) {
 						deltaY = movementRange;
 					} else if (deltaY < (-1)*movementRange) {
 						deltaY = (-1)*movementRange;
 					}
 					joystick.getCoordinates().setX(joystick.getRestingX());
 					joystick.getCoordinates().setY(joystick.getRestingY() + deltaY);
 				}
 			} else if (event.getAction() == MotionEvent.ACTION_UP) {
 				joystick.setToRestingState();
 			}
 
 			return true;
 		}
 	}
 
 	@Override
 	public void draw(Canvas canvas) {
 		canvas.drawColor(Color.BLACK);
 
 		drawBall(canvas);
 		drawPaddles(canvas);
 		drawBricks(canvas);
 		drawJoystick(canvas);
 		
 		if (DEBUG) {
 			this.canvas = canvas;
 			drawDebug(canvas);
 		}
 	}
 
 	private void drawDebug(Canvas canvas) {
 		String DEBUG_BRICK = null;
 		Paint drawDebugPaint = new Paint();
 		drawDebugPaint.setStyle(Paint.Style.FILL);
 		drawDebugPaint.setColor(Color.WHITE);
 		
 		for (int i = 0; i < bricks.size(); i++) {
 			canvas.drawCircle(bricks.get(i).getCoordinates().getX(), bricks.get(i).getCoordinates().getY(), 10, drawDebugPaint);
 		}
 		for (int i = 0; i < paddles.size(); i++) {
 			canvas.drawCircle(paddles.get(i).getCoordinates().getX(), paddles.get(i).getCoordinates().getY(), 10, drawDebugPaint);
 		}
 		
 		int yoff = 20;
 		drawDebugPaint = new Paint();
 		drawDebugPaint.setTextSize(yoff);
 		drawDebugPaint.setStyle(Paint.Style.STROKE);
 		drawDebugPaint.setColor(Color.WHITE);
 		String DEBUG_BALL = "Ball: " + balls.get(0).toString();
 		canvas.drawText(DEBUG_BALL, 0, yoff, drawDebugPaint);
 		
 		for (int i = 0; i < bricks.size(); i++) {
 			yoff += 20;
 			DEBUG_BRICK = "Brick: " + bricks.get(i).getCoordinates().toString();
 			canvas.drawText(DEBUG_BRICK, 0, yoff, drawDebugPaint);
 		}
 		
 		drawMessage();
 	}
 	
 	private void drawMessage() {
 		if (cacheMsg != null) {
 			Paint paint = new Paint();
 			paint.setTextSize(20);
 			paint.setStyle(Paint.Style.STROKE);
 			paint.setColor(Color.WHITE);
 			getCanvas().drawText(cacheMsg, 0, 80, paint);
 		}
 	}
 	
 	private void setMessage(String m) {
 		if (!m.equals(cacheMsg))
 			cacheMsg = m;
 	}
 
 	public int getRunState() {
 		return STATE_RUN;
 	}
 	
 	public void setRunState(int state) {
 		STATE_RUN = state;
 	}
 	
 	public boolean getPauseStatus() {
 		return STATE_PAUSE;
 	}
 
 	private void resetLevel() {
 		bricks.clear();
 		addFirstBricks();
 		addFirstPaddles();
 		balls.clear();
 		addFirstBalls();
 	}
 	
 	private void addFirstBalls() {
 		if (balls.isEmpty()) {
 			Bitmap launcher = BitmapFactory.decodeResource(getResources(),
 					R.drawable.ball);
 			Ball ball = new Ball(launcher);
 			ball.getCoordinates().setX(scale.getScaledX(500));
 			ball.getCoordinates().setY(scale.getScaledY(500));
 			balls.add(ball);
 		} else {
 			for (Iterator<Ball> it = balls.iterator(); it.hasNext(); ) {
 				Ball ball = it.next();
 				ball.setVisible(true);
 				ball.getCoordinates().setX(scale.getScaledX(500));
 				ball.getCoordinates().setY(scale.getScaledY(500));
 			}
 		}
 	}
 
 	private void addFirstPaddles() {
 		if (paddles.isEmpty()) {
 			Bitmap launcher = BitmapFactory.decodeResource(getResources(),
 					R.drawable.paddle);
 			Paddle paddle = new Paddle(launcher);
 			paddle.getCoordinates().setX(gameWindowWidth - launcher.getWidth());
 			paddles.add(paddle);
 			
 			launcher = BitmapFactory.decodeResource(getResources(),
 					R.drawable.paddle);
 			paddle = new Paddle(launcher);
 			paddle.getCoordinates().setX(0);
 			paddles.add(paddle);
 		} else {
 			for (int i = 0; i < paddles.size(); i++) {
 				paddles.get(i).getCoordinates().setY(getHeight() - paddles.get(i).getHeight());
 			}
 		}
 	}
 
 	private void addFirstBricks() {
 		// draw initial blocks
 		Bitmap launcher = BitmapFactory.decodeResource(getResources(),
 				R.drawable.brick);
 		Brick brick = new Brick(launcher);
 		brick.getCoordinates().setX(scale.getScaledX(100));
 		brick.getCoordinates().setY(scale.getScaledY(150));
 		bricks.add(brick);
 		
 		launcher = BitmapFactory.decodeResource(getResources(),
 				R.drawable.brick);
 		brick = new Brick(launcher);
 		brick.getCoordinates().setX(scale.getScaledX(600));
 		brick.getCoordinates().setY(scale.getScaledY(300));
 		bricks.add(brick);
 	}
 	
 	private void addFirstJoystick() {
 
 		float x = (getWidth() + gameWindowWidth) / 2;
 		float y = (getHeight()/2);
 		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
 				R.drawable.jstick);
 		
 		x = x - (bitmap.getWidth()/2);
 		y = y - (bitmap.getHeight()/2);
 		
 		joystick = new Joystick(bitmap, x, y, 25);
 		
 		Log.d(TAG,"gWW=" + gameWindowWidth + " gW=" + getWidth() + " x=" + x + " sX=" + scale.getScaledX(x));
 	}	
 	
 	private void addFirstJoystickBg(Canvas canvas) {
 		float x = (getWidth() + gameWindowWidth) / 2;
 		float y = (getHeight()/2);
 		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
 				R.drawable.jstick_bg);
 
 		x = x - (bitmap.getWidth()/2);
 		y = y - (bitmap.getHeight()/2);
 	
 		canvas.drawBitmap(bitmap, x, y, null);
 
 	}
 	
 	private void drawJoystick(Canvas canvas) {
 		
 		if (joystick == null) {
 			addFirstJoystick();
 		}
 		addFirstJoystickBg(canvas);
 		
 		Bitmap bitmap = joystick.getGraphic();
 		float x = joystick.getCoordinates().getX();
 		float y = joystick.getCoordinates().getY();
 		
 		canvas.drawBitmap(bitmap, x, y, null);
 
 		
 		if (joystick.getDirection() != Joystick.MOVEMENT_STOP)
 			Log.d(TAG,"joystick=" + joystick.toString());
 	}
 
 	private void drawBricks(Canvas canvas) {
 		Bitmap bitmap;
 		Graphic.Coordinates coords;
 
 		if (bricks.isEmpty()) {
 			if (STATE_RUN == STATE_INPROGRESS) {
 				addFirstBricks();
 			}
 		}
 
 		for (int i = 0; i < bricks.size(); i++) {
 			bitmap = bricks.get(i).getGraphic();
 			coords = bricks.get(i).getCoordinates();
 			canvas.drawBitmap(bitmap, coords.getX(), coords.getY(), null);
 		}
 	}
 
 	private void drawPaddles(Canvas canvas) {
 		Bitmap bitmap;
 		Graphic.Coordinates coords;
 		
 		if (paddles.isEmpty()) {
 			addFirstPaddles();
 		}
 		
 		for (int i = 0; i < paddles.size(); i++) {
 			/* getHeight() returns 0 in onCreate() */
 			bitmap = paddles.get(i).getGraphic();
 			coords = paddles.get(i).getCoordinates();
 			canvas.drawBitmap(bitmap, coords.getX(), coords.getY(), null);
 		}
 	}
 
 	private void drawBall(Canvas canvas) {
 		boolean addFirst = false;
 		Bitmap bitmap;
 		Graphic.Coordinates coords;
 
 		if (balls.isEmpty()) {
 			addFirst = true;
 		}
 		
 		for (int i = 0; i < balls.size(); i++) {
 			if (balls.get(i).getVisible() == false)
 				addFirst = true;
 		}
 		
 		if (addFirst == true) {
 			addFirstBalls();
 		}
 
 		for (int i = 0; i < balls.size(); i++) {
 			if (balls.get(i).getVisible()) {
 				bitmap = balls.get(i).getGraphic();
 				coords = balls.get(i).getCoordinates();
 				canvas.drawBitmap(bitmap, coords.getX(), coords.getY(), null);
 			}
 		}
 
 	}
 
 	public void updatePhysics() {
 
 		updatePhysicsCacheRemote();
 		updatePhysicsCacheLocal();
 	}
 
 	private void updatePhysicsCacheRemote() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * updatePhysicsCache
 	 * 
 	 * Shim layer which will update the cached objects' physics 
 	 * based on single or multiplayer status.
 	 */
 	private void updatePhysicsCacheLocal() {
 		for (int i = 0; i < balls.size(); i++) {
 				updateBallPhysics(balls.get(i));
 		}
 		for (int i = 0; i < paddles.size(); i++) {
 			updatePaddlePhysics(paddles.get(i));
 		}
 		if (joystick != null) 
 			updateJoystickPhysics();
 	}
 
 	private void updateJoystickPhysics() {
 		joystick.getCoordinates().setX(joystick.getRestingX());
 	}
 
 	private void updatePaddlePhysics(Paddle paddle) {
 		Graphic.Coordinates coord = paddle.getCoordinates();
 		float deltaY = paddle.getCoordinates().getY();
 		
 		if (joystick.getDirection() == Joystick.MOVEMENT_SOUTH) {
 			deltaY += 20;
 		} else if (joystick.getDirection() == Joystick.MOVEMENT_NORTH) {
 			deltaY -= 20;
 		}
 		
 		paddle.getCoordinates().setY(deltaY);
 		
 		if (coord.getY() < 0)
 			paddle.getCoordinates().setY(0);
 		else if ((coord.getY() + paddle.getHeight()) > getHeight())
 			paddle.getCoordinates().setY(getHeight() - paddle.getHeight());
 	}
 
 	private void updateBallPhysics(Ball ball) {
 		Graphic.Coordinates coord = ball.getCoordinates();
 		Graphic.Speed speed = ball.getSpeed();
 
 		// Direction
 		coord = updateBallPhysicsDirections(coord, speed);
 
 		// Brick hit
 		speed = ballHitBricks(ball, speed);
 		
 		// borders for x...
 		if (coord.getX() < 0) {
 			ball.setVisible(false);
 		} else if (coord.getX() + ball.getGraphic().getWidth() > gameWindowWidth) {
 			ball.setVisible(false);
 		}
 		
 		// borders for y...
 		/* If we're below paddle height. We'll lose anyways. */
 		if (coord.getY() < 0) {
 			speed.toggleYDirection();
 			coord.setY(-coord.getY());
 		} else if ((coord.getY() + ball.getGraphic().getHeight()) >= getHeight()) {
 			speed.toggleYDirection();
 			coord.setY(coord.getY() + getHeight()
 					- (coord.getY() + ball.getGraphic().getHeight()));
 		}
 		
 		updateBallPaddlesPhysics(ball);
 		
 	}
 
 	private void updateBallPaddlesPhysics(Ball ball) {
 		float paddleLevel;
 		
 		for (int i = 0; i < paddles.size(); i++) {
 			paddleLevel = paddles.get(i).getCoordinates().getX();
 
 			if (paddleLevel == 0) {
 				if (ball.getCoordinates().getX() < paddles.get(i).getWidth()) {
 					if (ballHitPaddle(paddles.get(i), ball)) {
 
 						ballHitSound();
 
 						ball.getSpeed().toggleXDirection();
 						ball.getCoordinates().setX(paddleLevel + ball.getCoordinates().getX());
 					}
 				}
 			} else {
 				// So this isn't scalable at all.
 				if (ball.getCoordinates().getX() + ball.getGraphic().getWidth() > paddleLevel) {
 					if (ballHitPaddle(paddles.get(i), ball)) {
 
 						ballHitSound();
 
 						ball.getSpeed().toggleXDirection();
 						ball.getCoordinates().setX(paddleLevel - ball.getGraphic().getWidth());
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * ballHitBricks
 	 * 
 	 * @param ball
 	 * @param coord
 	 * @return Graphic.Speed, in the event that a brick is hit. We must have the
 	 *         ball bounce away.
 	 */
 	private Graphic.Speed ballHitBricks(Ball ball, Graphic.Speed speed) {
 		Graphic.Speed s = speed;
 		Intersection i = null;
 		for (int j = 0; j < bricks.size(); j++) {
 			i = ballHitBrick(ball, bricks.get(j));
 			if (i.getIntersect() == true) {
 				
 				if (DEBUG && cacheMsg == null) {
 					setMessage("ballHitBrick()");
 				}
 				
 				ballHitSound();
 				bricks.remove(j);
 				
				if (balls.isEmpty() == true) {
 					Log.d(TAG,"victory");
 					setMessage("victory()");
 					victory();
 				}
 				
 				s = ballHitFaceToggleDirection(s,i);
 			}
 		}
 
 		return s;
 	}
 
 	private Speed ballHitFaceToggleDirection(Graphic.Speed s, Intersection i) {
 		
 		switch(i.getFace()) {
 		case Collision.EAST_FACE:
 		case Collision.WEST_FACE:
 			s.toggleXDirection();
 			break;
 		case Collision.NORTH_FACE:
 		case Collision.SOUTH_FACE:
 			s.toggleYDirection();
 			break;
 		case Collision.NE_CORNER:
 		case Collision.SE_CORNER:
 		case Collision.SW_CORNER:
 		case Collision.NW_CORNER:
 		default:
 			s.toggleXDirection();
 			s.toggleYDirection();
 			break;
 		}
 		
 		return s;
 	}
 
 	private Intersection ballHitBrick(Ball ball, Brick brick) {
 
 		return brick.ballHit(ball);
 	}
 
 	private Coordinates updateBallPhysicsDirections(Coordinates coord,
 			Speed speed) {
 
 		if (speed.getXDirection() == Graphic.Speed.X_DIRECTION_RIGHT) {
 			coord.setX(coord.getX() + speed.getX());
 		} else {
 			coord.setX(coord.getX() - speed.getX());
 		}
 		if (speed.getYDirection() == Graphic.Speed.Y_DIRECTION_DOWN) {
 			coord.setY(coord.getY() + speed.getY());
 		} else {
 			coord.setY(coord.getY() - speed.getY());
 		}
 		return coord;
 	}
 
 	private boolean ballHitPaddle(Paddle paddle, Ball ball) {
 		return paddle.ballHit(ball);
 	}
 
 	public void openRetryDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
 
 		Log.d(TAG,"Building dialog");
 		
 		builder.setMessage("Retry?")
 				.setCancelable(false)
 				.setPositiveButton("Yes",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								// Reset the level
 								STATE_RUN = STATE_RETRY;
 								STATE_PAUSE = false;
 								resetLevel();
 							}
 						})
 				.setNegativeButton("No", new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 
 						STATE_RUN = STATE_EXIT;
 						STATE_PAUSE = true;
 					}
 				}).show();
 
 	}
 
 	private void victory() {
 		
 		STATE_PAUSE = true;
 		
 		this.activity.runOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				openRetryDialog();
 			}
 		});
 	}
 	
 	private void ballHitSound() {
 		/*
 		this.activity.runOnUiThread(new Runnable() {
 
 			@Override
 			public void run() {
 			*/	
 				sp.play(1, 1.0f, 1.0f, 1, 0, 1.0f);
 				Log.d(TAG,"Played soundpool");
 				/*
 			}
 			
 		});
 		*/
 	}
 }
