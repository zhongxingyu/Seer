 package com.example.marslandingsimulation;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.BitmapShader;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Movie;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.graphics.Shader;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.media.MediaPlayer;
 import android.media.SoundPool;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnTouchListener;
 
 public class SView extends SurfaceView implements Runnable,
 		SurfaceHolder.Callback, OnTouchListener, SensorEventListener {
 	
 	SoundPool soundPool;
 	MediaPlayer mp;
 	MediaPlayer mp2;
 	Movie explodeGif;
 	
 	static final int REFRESH_RATE = 5;
 	static final int GRAVITY = 4;
 	static final int MOVEMENT = 4;
 	static final int fuelUsage = 1;
 	double t = 0.1;
 	static final int maxFuel = 800;
 	int fuel = maxFuel;
 	boolean fuelFinished = false;
 	boolean pause = false;
 	Thread main;
 	Paint paint = new Paint();
 	Bitmap ship,shipCrash, sMain, sLeft,sRight, sGround, sStars; 
 	int shipW, shipH, sMainSize, sLeftSize, sGroundSize, sStarsSize, sMainSizeH, sLeftSizeH, sGroundSizeH, sStarsSizeH;
 	BitmapShader fillBMPshaderGround, fillBMPshaderStars;
 	float x, y;
 	float sX, sY;
 	float mMovieStart = 0;
 	SensorManager newSensor = null;
 	float xAxis = 0;
 	float yAxis = 0;
 	int width = 0;
 	int height = 0;
 	ArrayList<Integer> xcor = new ArrayList<Integer>();
 	ArrayList<Integer> ycor = new ArrayList<Integer>();
 	Canvas offscreen;
 	Bitmap buffer;
 	Boolean gameover = false;
 	int Changedwidth = 0;
 	Path path;
 
 	public SView(Context context, int width, int height) {
 		super(context);
 		newSensor = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
 		newSensor.registerListener(this, newSensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
 		this.width = width;
 		this.height = height;
 		sX = 1920 / (float) width;
 		sY = 1080 / (float) height;
 		x = (float)(100/sX);
 		y = (float)(40/sY);
 		shipW = (int) (120 / sX);
 		shipH = (int) (60 / sY);
 		sMainSize = (int) (50 / sX);
 		sMainSizeH = (int) (50 / sY);
         sLeftSize = (int) (30 / sX);
         sLeftSizeH = (int) (30 / sY);
         sGroundSize = (int) (300 / sX);
         sGroundSizeH = (int) (300 / sY);
         sStarsSize = (int) (300 / sX);
         sStarsSizeH = (int) (300 / sY);
         Bitmap temShip = BitmapFactory.decodeResource(getResources(), R.drawable.ship1);
         Bitmap temShipcrash = BitmapFactory.decodeResource(getResources(), R.drawable.shipcrash);
 		Bitmap temSMain = BitmapFactory.decodeResource(getResources(), R.drawable.main);
 		Bitmap temSLeft = BitmapFactory.decodeResource(getResources(), R.drawable.left);
 		Bitmap temSRight = BitmapFactory.decodeResource(getResources(), R.drawable.right);
 		Bitmap temSground = BitmapFactory.decodeResource(getResources(), R.drawable.ground);
 		Bitmap temSStars = BitmapFactory.decodeResource(getResources(), R.drawable.stars);
 		ship = Bitmap.createScaledBitmap(temShip, shipW, shipH, true);
 		shipCrash = Bitmap.createScaledBitmap(temShipcrash, shipW, shipH, true);
 		sMain = Bitmap.createScaledBitmap(temSMain, sMainSize, sMainSizeH, true);
 		sLeft = Bitmap.createScaledBitmap(temSLeft, sLeftSize, sLeftSizeH, true);
 		sRight = Bitmap.createScaledBitmap(temSRight, sLeftSize, sLeftSizeH, true);
 		sGround = Bitmap.createScaledBitmap(temSground , sGroundSize, sGroundSizeH, true);
 		sStars = Bitmap.createScaledBitmap(temSStars, sStarsSize, sStarsSizeH, true);
 		fillBMPshaderGround = new BitmapShader(sGround, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
 		fillBMPshaderStars = new BitmapShader(sStars, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
 		
 		init();
 		mp = MediaPlayer.create(getContext(), R.raw.rocket);
 		mp.setLooping(true);
 		mp2 = MediaPlayer.create(getContext(), R.raw.explosion);
 		mp2.setLooping(true);
 		InputStream is = this.getContext().getResources().openRawResource(R.drawable.explosion);
 		explodeGif = Movie.decodeStream(is);
 	}
 
 	public SView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 
 	public SView(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init();
 	}
 
 
 	public void init() {
 		setOnTouchListener(this);
 		getHolder().addCallback(this);
 	}
  
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		// TODO Auto-generated method stub
 		x = event.getX();
 		y = event.getY();
		t = 0;
 		gameover = false;
 		return true;
 	}
 	
 	@Override
 	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
 		super.onSizeChanged(w, h, oldw, oldh);
 		
 		sX = 1920 / (float) w;
 		sY = 1080 / (float) h;
 		
 		xcor.add(0);	ycor.add((int) (700 / sY));
 		xcor.add((int) (200 / sX));	ycor.add((int) (700 / sY));
 		xcor.add((int) (200 / sX));	ycor.add((int) (750 / sY));
 		xcor.add((int) (400 / sX));	ycor.add((int) (750 / sY));
 		xcor.add((int) (400 / sX));	ycor.add((int) (600 / sY));
 		xcor.add(width);	ycor.add((int) (700 / sY));
 		xcor.add(width);	ycor.add(height);
 		xcor.add(0);	ycor.add(height);
 		xcor.add(0);	ycor.add((int) (700 / sY));
 		// Draw path
 		path = new Path();
 		for (int i = 0; i < xcor.size(); i++) {
 			path.lineTo(xcor.get(i), ycor.get(i));
 		}
 	}
 
 	@Override
 	public void run() {
 		while (!Thread.interrupted()) {
 			while (!pause) {
 				Canvas canvas = null;
 				SurfaceHolder holder = getHolder();
 				synchronized (holder) {
 					canvas = holder.lockCanvas();
 					// Stars background
 					Paint paintstars = new Paint();
 					paintstars.setColor(Color.BLACK);
 					paintstars.setStyle(Paint.Style.FILL);
 					paintstars.setShader(fillBMPshaderStars);
 					canvas.drawPaint(paintstars);
 					Paint paint1 = new Paint();
 					paint1.setColor(Color.GREEN);
 					paint1.setStyle(Paint.Style.FILL);
 					paint1.setShader(fillBMPshaderGround);
 					canvas.drawPath(path, paint1);
 					// Fuel
 					Paint paintFuel = new Paint();
 					paintFuel.setColor(Color.GRAY);
 					canvas.drawRect(0, (float)((height)-(5/sY)), (float)((width/2)+(5/sY)), (float)((height)-(35/sY)), paintFuel);
 					paint.setColor(Color.CYAN);
 					canvas.drawRect(0, (float)((height)-(10/sY)), fuel * (float)(width/2) / maxFuel, (float) ((height)-(30/sY)), paint);
					paint.setTextSize((float)(45/sY));
					canvas.drawText("FUEL:", 0, (float)((height)-(40/sY)), paint);
 					if (!gameover) {
 						if (yAxis > 2 && yAxis < 8 && !fuelFinished) {
 							x = x + (MOVEMENT/sX);
 							y = y - 1;
 							t = 1;
 							canvas.drawBitmap(sLeft, x - (65/sX), y + (28/sY), paint);
 							fuel -= fuelUsage;
 							mp.start();
 						} else if (yAxis < -2 && yAxis > -8 && !fuelFinished) {
 							x = x - (MOVEMENT/sX);
 							y = y - (1/sY);
 							t = 1;
 							canvas.drawBitmap(sRight, x + (35/sX), y + (28/sY), paint);
 							fuel -= fuelUsage;
 							mp.start(); 
 						} else if (xAxis < 8 && xAxis > 2 && !fuelFinished) {
 							y = y - (4/sY);
 							t = 0.5;
 							canvas.drawBitmap(sMain, x - (25/sX), y + (35/sY), paint);
 							fuel -= (fuelUsage + 1);
 							mp.start();
 						} else {
 							y = (int) y + (int)(t + (0.5 * ((GRAVITY/sY) * t * t)));
 							t = t + 0.01;
 							mp.pause();
 						}
 					}
 
 					// If object hit the side
 					if (x < 0) {
 						x = width;
 					}
 					if (y < (float)(30/sY)) {
 						y = (float)(30/sY);
 					}
 					if (x > width) {
 						x = 0;
 					}
 					if (y > height) {
 						y = height;
 					}
 				}
 				if (fuel < 0) {
 					fuelFinished = true;
 				}
 
 				if (contains(xcor, ycor, x + (60/sX), y + (30/sY))) {
 					long now = android.os.SystemClock.uptimeMillis();
 					if (mMovieStart == 0) { // first time
 						mMovieStart = now;
 					}
 					int dur = explodeGif.duration();
 					if (dur == 0)
 						dur = 1000;
 					
 					int relTime = (int) ((now - mMovieStart) % dur);
 					explodeGif.setTime(relTime);
 					
					Bitmap movieBitmap = Bitmap.createBitmap((int) (80 / sX),
							(int) (40 / sY), Bitmap.Config.ARGB_8888);
 					canvas.drawBitmap(shipCrash, x - (60/sX), y - (30/sY), paint);
 					Canvas movieCanvas = new Canvas(movieBitmap);
//					movieCanvas.scale(width*sX, height*sY);
 					explodeGif.draw(movieCanvas, 0, 0, paint);
 					canvas.drawBitmap(movieBitmap,x - (60/sX),y - (30/sY),paint);
 					gameover = true;
 				}
 				else
 				{
 					canvas.drawBitmap(ship, x - (60/sX), y - (30/sY), paint);
 				}
 				
 
 				try {
 					Thread.sleep(REFRESH_RATE);
 				} catch (Exception e) {
 				}
 
 				holder.unlockCanvasAndPost(canvas);
 			}
 		}
 
 	}
 
 	public boolean contains(ArrayList<Integer> xcor, ArrayList<Integer> ycor, double x0, double y0) {
 		int crossings = 0;
 
 		for (int i = 0; i < xcor.size() - 1; i++) {
 			int x1 = xcor.get(i);
 			int x2 = xcor.get(i + 1);
 
 			int y1 = ycor.get(i);
 			int y2 = ycor.get(i + 1);
 
 			int dy = y2 - y1;
 			int dx = x2 - x1;
 
 			double slope = 0;
 			if (dx != 0) {
 				slope = (double) dy / dx;
 			}
 
 			boolean cond1 = (x1 <= x0) && (x0 < x2); // is it in the range?
 			boolean cond2 = (x2 <= x0) && (x0 < x1); // is it in the reverse
 														// range?
 			boolean above = (y0 < slope * (x0 - x1) + y1); // point slope y - y1
 
 			if ((cond1 || cond2) && above) {
 				crossings++;
 			}
 		}
 		return (crossings % 2 != 0); // even or odd
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void onSensorChanged(SensorEvent event) {
 		xAxis = event.values[0];
 		yAxis = event.values[1];
 	}
 
 	public void reset() {
 		gameover = false;
 		x = Changedwidth / 2;
 		y = 0;
 		t = 0;
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		main = new Thread(this);
 		if (main != null)
 			main.start();
 		pause = false;
 	}
 	public void pause()
 	{
 		pause = true;
 		mp.pause();
 		newSensor.unregisterListener(this);
 	}
 	public void resume(){
 		mp.start();
 		newSensor.registerListener(this, newSensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		// TODO Auto-generated method stub
 		pause = true;
 	}
 }
