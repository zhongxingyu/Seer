 package doggy.rush;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.LinearGradient;
 import android.graphics.Paint;
 import android.graphics.Shader;
 import android.graphics.Shader.TileMode;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class DRView extends SurfaceView{
 	
 	private static final int VIEW_DESIGNED_HEIGHT = 480;
 	private static final int SCORE_SCALE = 5;
 	private SurfaceHolder holder;
 	private DRDog dog;
 	private DRRoad road;
 	private int dogOnLane;
 	private DRSprite title, start, restart, fireBtn, win, lose, resume, one, two, three, paused, hint1, hint2, distance, highest;	
 	private DRThread thread;
 	private Paint powerBarPaint, scoreTextPaint;
 	private int redScreenAlpha;
     private int width, height, count = -1;
     private Boolean init=false;
     private double widthCenter, heightCenter;
     private double scale;
     private int score, oldScore;
     
 	public enum State {
 	    MENU, START, WIN, LOSE, PAUSE, NOOP, COUNT
 	}
 	private State state=State.MENU;
 	
 	public DRView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		
 
 		holder= getHolder();
 		holder.addCallback(new SurfaceHolder.Callback(){
 			
 			public void surfaceDestroyed(SurfaceHolder holder){
 				//boolean retry=true;
 				thread.setRunning(false);
 
 			}
 			
 			public void surfaceCreated(SurfaceHolder holder){
 				thread=createThread();
 				thread.setRunning(true);
				prepareItems();
				if (state==State.START){
 					state=State.PAUSE;
 				}else{
 					resetStatus();
 					state=State.MENU;
 				}
 				thread.start();
 			}
 			
 			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}
 		});
 		
 	}
 	
 	private DRThread createThread(){
 		return new DRThread(this);
 	}
 	
 	private void prepareItems(){
 		
 		Resources res = getResources();
 		
 		BitmapFactory.Options options = new BitmapFactory.Options();
 		scale = (double)res.getDisplayMetrics().heightPixels/VIEW_DESIGNED_HEIGHT;
 		options.inDensity = VIEW_DESIGNED_HEIGHT;
 		options.inTargetDensity =  res.getDisplayMetrics().heightPixels;
 		options.inScaled = true;
 		
 		dog = new DRDog(res, options);
 		road = new DRRoad(res, options);
 		
 		powerBarPaint = new Paint(); 
 		powerBarPaint.setShader(new LinearGradient(0, 0, 0, (int)scale(380), Color.YELLOW, Color.RED, TileMode.CLAMP)); 
 		
 		scoreTextPaint = new Paint(); 
 		scoreTextPaint.setTextSize(Math.round(scale(24)));
 		scoreTextPaint.setColor(Color.WHITE);
 		
 		distance=new DRSprite(BitmapFactory.decodeResource(res, R.drawable.distance, options),1);
 		highest=new DRSprite(BitmapFactory.decodeResource(res, R.drawable.highest, options),1);
 		title=new DRSprite(BitmapFactory.decodeResource(res, R.drawable.title, options),1);
 		start=new DRSprite(BitmapFactory.decodeResource(res, R.drawable.start, options),4);
 		restart=new DRSprite(BitmapFactory.decodeResource(res, R.drawable.restart, options),4);
 		fireBtn=new DRSprite(BitmapFactory.decodeResource(res, R.drawable.firebtn, options),1);
 		win=new DRSprite(BitmapFactory.decodeResource(res, R.drawable.win, options),1);
 		lose=new DRSprite(BitmapFactory.decodeResource(res, R.drawable.lose, options),1);
 		resume=new DRSprite(BitmapFactory.decodeResource(res, R.drawable.resume, options),4);
 		one=new DRSprite(BitmapFactory.decodeResource(res, R.drawable.one, options),1);
 		two=new DRSprite(BitmapFactory.decodeResource(res, R.drawable.two, options),1);
 		three=new DRSprite(BitmapFactory.decodeResource(res, R.drawable.three, options),1);
 		paused=new DRSprite(BitmapFactory.decodeResource(res, R.drawable.paused, options),1);
 		hint1=new DRSprite(BitmapFactory.decodeResource(res, R.drawable.hint1, options),1);
 		hint2=new DRSprite(BitmapFactory.decodeResource(res, R.drawable.hint2, options),1);
 	}
 
 	public void setOldScore(int value){
 		
 		oldScore = value*SCORE_SCALE;
 	}
 	private void resetStatus(){
 		
 		road.resetCars();
 		road.setCarExists(false);
 		dog.revive();
 		dog.setCurrentPosition(scale(-250), heightCenter);
 		count = 90;
 		dogOnLane=1;
 		score = 0;
 		redScreenAlpha = 0;
 	}
 	public boolean back(){
 		
 		switch(state){
 			case START:
 				state = State.PAUSE;
 				return false;
 			case PAUSE:
 				state = State.START;
 				return false;
 			case MENU:
 				return true;
 			default:
 				resetStatus();
 				state = State.MENU;
 				return false;
 		}
 		
 	}
 	public void moveDogUp(){
 		dogOnLane = dogOnLane-1<0 ? 0 : dogOnLane-1;
 		dog.setTargetY(DRRoad.getLanePosition(height, dogOnLane));
 	}
 	public void moveDogDown(){
 		dogOnLane = dogOnLane+1>2 ? 2 : dogOnLane+1;
 		dog.setTargetY(DRRoad.getLanePosition(height, dogOnLane));
 	}
 	public int touch(float x, float y){
 		switch(state){
 			case MENU:
 				if(start.isCoveringPoint(x, y)){
 					state=State.COUNT;
 				}
 			break;
 			
 			case START:
 				if(fireBtn.isCoveringPoint(x, y)){
 					dog.setOnFire();
 				}
 			break;
 			
 			case WIN:
 			case LOSE:
 				if(restart.isCoveringPoint(x, y)){
 					int newScore = score > oldScore ? score : 0;
 					resetStatus();
 					state=State.MENU;
 					return newScore/SCORE_SCALE;
 				}
 			break;
 			
 			case PAUSE:
 				if(restart.isCoveringPoint(x, y)){
 					resetStatus();
 					state=State.MENU;
 					return -1;
 				}
 				if(resume.isCoveringPoint(x, y)){
 					state=State.START;
 				}
 			break;			
 		}
 		return -1;
 		
 	}
 	// Functions will be called in every frame //
 	private double scale(double p){
 		return p*scale;
 	}
 	@Override
 	protected void onDraw(Canvas canvas){
 	
 		if (canvas==null) return;
 		
 		if (init==false){
			height = (int) Math.round(getHeight());
			heightCenter=height/2;
			width = (int) Math.round(getWidth());
			widthCenter=width/2;
			resetStatus();
 			init=true;
 		}
 		canvas.drawColor(Color.BLACK);
 		switch (state){
 			case MENU:
 				road.draw(canvas);
 					
 				dog.setTargetX(-10);
 				dog.setTargetY(heightCenter);
 				dog.drawDogs(canvas);
 				
 				title.drawObject(canvas, width/3, height/3);
 				start.drawObject(canvas, width/1.8, height/1.5);
 				highest.drawObject(canvas, scale(20), height-highest.getHeight());
 				Log.d("DoggyRush", "OldScore: "+oldScore/SCORE_SCALE+"m");
 				canvas.drawText(oldScore/SCORE_SCALE+"m", (float)scale(20)+highest.getWidth(), height-highest.getHeight()+(float)scale(7), scoreTextPaint);
 				
 			break;
 			
 			case COUNT:
 				road.draw(canvas);
 				dog.drawDogs(canvas);
 				
 				if (count==0){
 					state=State.START;
 				}else if (count<33){
 					one.drawObjectCentered(canvas, widthCenter, heightCenter);
 				}else if (count<67){
 					two.drawObjectCentered(canvas, widthCenter, heightCenter);
 				}else if (count<100){
 					three.drawObjectCentered(canvas, widthCenter, heightCenter);
 				}
 				count--;
 				
 				hint1.drawObject(canvas, width-hint1.getWidth()-fireBtn.getWidth()-scale(10), height-fireBtn.getHeight());
 				hint2.drawObjectCentered(canvas, widthCenter, height/4);
 				fireBtn.drawObject(canvas, width-fireBtn.getWidth()-scale(10), height-fireBtn.getHeight());
 				distance.drawObject(canvas, scale(20), distance.getHeight());
 				canvas.drawText(score/5+"m", (float)scale(20)+distance.getWidth(), distance.getHeight()+(float)scale(10), scoreTextPaint);
 			break;
 			
 			case START:
 				road.setCarExists(true);
 				road.draw(canvas);
 				road.setSpeedByScore(score);
 				dog.drawDogs(canvas);
 				
 				fireBtn.drawObject(canvas, width-fireBtn.getWidth()-scale(10), height-fireBtn.getHeight());
 				distance.drawObject(canvas, scale(20), distance.getHeight());
 				canvas.drawText(score++/SCORE_SCALE+"m", (float)scale(20)+distance.getWidth(), distance.getHeight()+(float)scale(10), scoreTextPaint);
 				canvas.drawRect(width-(float)scale(40), (float)scale(Math.round(30+(350-350*dog.getPowerRatio()))), width-(float)scale(15), (float)scale(380), powerBarPaint);
 				if( redScreenAlpha > 0){
 					canvas.drawARGB(redScreenAlpha, 255, 0, 0);
 					redScreenAlpha-=5;
 				}
 				if (road.killDog(dog) && dog.canKill()){	
 					redScreenAlpha = 100;
 					if (dog.kill()==0) state = State.LOSE; 
 				}		
 
 			break;
 			
 			case WIN:				
 				road.draw(canvas);
 				dog.setTargetX(1000);
 				dog.drawDogs(canvas);
 				
 				win.drawObjectCentered(canvas,widthCenter, height/3);
 				restart.drawObjectCentered(canvas,  widthCenter, height/1.5);
 				
 			break;
 			
 			case LOSE:
 				road.draw(canvas);
 	
 				lose.drawObjectCentered(canvas,widthCenter, height/3);
 				restart.drawObjectCentered(canvas, widthCenter, height/1.5);
 				distance.drawObject(canvas, scale(20), distance.getHeight());
 				canvas.drawText(score/SCORE_SCALE+"m"+(score > oldScore ? " (New record!)":""), (float)scale(20)+distance.getWidth(), distance.getHeight()+(float)scale(10), scoreTextPaint);
 				if( redScreenAlpha > 0){
 					canvas.drawARGB(redScreenAlpha, 255, 0, 0);
 					redScreenAlpha-=5;
 				}
 			break;
 			
 			case PAUSE:
 				
 				paused.drawObjectCentered(canvas, widthCenter, height/4);
 				resume.drawObjectCentered(canvas, widthCenter, heightCenter);
 				restart.drawObjectCentered(canvas, widthCenter, height/1.5);
 				distance.drawObject(canvas, scale(20), distance.getHeight());
 				canvas.drawText(score/SCORE_SCALE+"m", (float)scale(20)+distance.getWidth(), distance.getHeight()+(float)scale(10), scoreTextPaint);
 				
 			break;
 			
 		}	
 	
 	}
 }
