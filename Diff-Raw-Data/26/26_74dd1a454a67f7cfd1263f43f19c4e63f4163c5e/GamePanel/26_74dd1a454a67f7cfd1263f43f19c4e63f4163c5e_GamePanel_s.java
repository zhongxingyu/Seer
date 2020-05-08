 package com.example.pineapple;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class GamePanel extends SurfaceView implements SurfaceHolder.Callback{
 	private final String TAG = GamePanel.class.getSimpleName();
 	private final int width = 155;
 	private final int height = 100;
 	private double screenX;
 	private double screenY;
 	private final int screenPadding = 50;
 	private MainThread thread;
 	private Protagonist protagonist;
 	private Ground ground;
 	private double scaleY, scaleX;
 	private Stick leftStick, rightStick;
 	private LevelLoader levelLoader;
 	private ArrayList<Platform> platforms;
 	private ArrayList<Bullet> bullets;
 	private boolean scaledPaths = false;
 	private int level;
 	
 	
 	public GamePanel(Context context, int level){
 		super(context);
 		getHolder().addCallback(this);
 		setFocusable(true);
 		screenX = 0;
 		screenY = 0;
 		this.level = level;
 		levelLoader = new LevelLoader();
 		//This has to be adressed later!
 		loadPlatforms();
 		bullets = new ArrayList<Bullet>();
 		ground = new Ground(levelLoader.getLevelX(level), levelLoader.getLevelY(level));
 		protagonist = new Protagonist(width/2, ground.getYFromX(77), this);
 		leftStick = new Stick(Stick.LEFT);
 		rightStick = new Stick(Stick.RIGHT);
 		thread = new MainThread(this.getHolder(), this);
 	}
 	
 	//Load the platforms from LevelLoader and add them to the platforms list 
 	public void loadPlatforms(){
 		platforms = new ArrayList<Platform>();
 		for(int i = 1; i <= levelLoader.getNumberOfPlatforms(level); i++){
 			platforms.add(new Platform(levelLoader.getPlatformUpperX(level, i), levelLoader.getPlatformUpperY(level, i), levelLoader.getPlatformLowerX(level, i), levelLoader.getPlatformLowerY(level, i)));
 		}
 	}
 	
 	//Method that gets called every frame to update the games state
 	public void update(){
 		if(leftStick.isPointed()) {
 			protagonist.handleLeftStick(leftStick.getAngle(), 0.4);
 		} else if (Math.abs(protagonist.getXVel()) > 0){
 			protagonist.slowDown();
 		}
 		if(rightStick.isPointed()){
 			//Aim
 			double angle = rightStick.getAngle();
 			protagonist.aim(angle);
 			//Fire
 			bullets.add(new Bullet(protagonist.getXPos()+protagonist.getWidth()/2*Math.cos(angle/180*Math.PI), protagonist.getYPos()-protagonist.getWidth()/2*Math.sin(angle/180*Math.PI), angle, 10));
 		}
 		moveProtagonist();
 		moveBullets();
 		moveScreen();
 	}
 	
 	public void moveProtagonist(){
 		protagonist.gravity();
 		protagonist.move();
 		protagonist.checkGround(ground);
 		protagonist.checkPlatform(platforms);
 	}
 		
 	public void moveBullets(){
 		for(int i = 0; i < bullets.size(); i++){
 			Bullet b = bullets.get(i);
 			b.gravity(1);
 			b.move();
 			int leftBound = ground.getX(0);
 			int rightBound = ground.getX(ground.getLength()-1);
 			if(b.getXPos() < leftBound || b.getXPos() > rightBound){
 				bullets.remove(i);
 				i--;
 			} else if(b.checkObstacles(ground, platforms)){
 				bullets.remove(i);
 				i--;
 				Log.d(TAG, "Removed bullet");
 			}
 			
 		}
 	}
 	
 	//Moves the screen if the protagonist is close to the edge of the screen
 	public void moveScreen(){
 		if(protagonist.getXPos() - screenX > width - screenPadding){
 			screenX = protagonist.getXPos() - width + screenPadding;
 		} else if(protagonist.getXPos() - screenX < screenPadding){
 			screenX = protagonist.getXPos() - screenPadding;
 		}
 		
 	}
 	
 	//Method that gets called to render the graphics
 	public void render(Canvas canvas){
 		canvas.drawColor(Color.WHITE);
 		renderGround(canvas);
 		renderPlatforms(canvas);
 		renderProtagonist(canvas);
 		renderBullets(canvas);
 		renderSticks(canvas);
 	}
 	
 	//Pause the game
 	public void pause(){
 		thread.setRunning(false);
 	}
 	
 	//Draw the protagonist
 	public void renderProtagonist(Canvas canvas){
 		Paint p = new Paint();
 		p.setColor(Color.BLUE);
 		canvas.drawRect((float)((protagonist.getXPos()-protagonist.getWidth()/2-screenX)*scaleX), (float)((protagonist.getYPos()-protagonist.getHeight()/2)*scaleY), (float)((protagonist.getXPos()+protagonist.getWidth()/2-screenX)*scaleX), (float)((protagonist.getYPos()+protagonist.getHeight()/2)*scaleY), p);
 	}
 	
 	//Draws the ground using a Path
 	public void renderGround(Canvas canvas){
 		int length = ground.getLength();
 		for(int i = 0; i < length-1; i++){
 			Path p = new Path();
 			p.moveTo((int)((ground.getX(i)-screenX)*scaleX), (int)(ground.getY(i)*scaleY));
 			p.lineTo((int)((ground.getX(i+1)-screenX)*scaleX), (int)(ground.getY(i+1)*scaleY));
 			p.lineTo((int)((ground.getX(i+1)-screenX)*scaleX), (int)(200*scaleY)); //Fix line 63 and 64, (200)
 			p.lineTo((int)((ground.getX(i)-screenX)*scaleX), (int)(200*scaleY));
 			p.lineTo((int)((ground.getX(i)-screenX)*scaleX), (int)(ground.getY(i)*scaleY));
 			canvas.drawPath(p, new Paint());
 		}
 	}
 	
 	//Draw the platforms
 	public void renderPlatforms(Canvas canvas){
 		Paint p = new Paint();
 		p.setColor(Color.CYAN);
 		for(int i = 0; i < platforms.size(); i++){
 			Path path = platforms.get(i).getPath();
 			Path newPath = new Path(path); 
 			Matrix m = new Matrix();
 			m.postTranslate(-(float)screenX, -(float)screenY);
 			m.postScale((float)scaleX, (float)scaleY);
 			newPath.transform(m);
 			canvas.drawPath(newPath, p);
 		}
 	}
 	
 	//Draw the sticks
 	public void renderSticks(Canvas canvas){
 		Paint p = new Paint();
 		p.setColor(Color.GRAY);
 		canvas.drawCircle((float)(leftStick.getX()*scaleX), (float)(leftStick.getY()*scaleY), (float)(leftStick.getRadius()*scaleX), p);
 		canvas.drawCircle((float)(rightStick.getX()*scaleX), (float)(rightStick.getY()*scaleY), (float)(rightStick.getRadius()*scaleX), p);
 	}
 	
 	//Draw the bullets
 	public void renderBullets(Canvas canvas){
 		Paint p = new Paint();
 		p.setColor(Color.RED);
 		for(int i = 0; i < bullets.size(); i++){
 			Bullet b = bullets.get(i);
 			int radius = b.getRadius();
 			canvas.drawCircle((float)((b.getXPos()-radius/2.-screenX)*scaleX), (float)((b.getYPos()-radius/2.-screenY)*scaleY), (float)(radius*scaleX), p);
 		}
 	}
 	
 	//Fix this (MultiTouch)
 	@Override
 	public boolean onTouchEvent(MotionEvent e){
		/*double x = e.getX()/scaleX;
 		double y = e.getY()/scaleY;
 		leftStick.handleTouch(x, y);
 		rightStick.handleTouch(x, y);
 		Log.d(TAG, leftStick.getAngle()+"");
 		
 		if(e.getAction() == MotionEvent.ACTION_UP){
 			leftStick.release();
 			rightStick.release();
 			Log.d(TAG, "release me");
		}*/
 		
 		
 		
 		return true;
 	}
 	
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		//Start the thread
 		thread.setRunning(true);
 		thread.start();
 		//Calculate the scale that we will use to render the game
 		scaleY = (double)getHeight()/height;
 		scaleX = (double)getWidth()/width;
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		//End the thread
 		boolean retry = true;
 		while(retry){
 			try{
 				thread.join();
 				retry = false;
 			} catch(InterruptedException e){
 				
 			}
 		}
 	}
 	public Ground getGround() {
 		return ground;
 	}
 }
