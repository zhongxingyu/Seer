 package uk.co.jackwatling.surfaceview;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback{
 
 	//Threading
 	private SurfaceHolder holder;
 	private Thread thread;
 	private boolean running;
 	
 	//Objects
 	private ArrayList<Sprite> sprites;
 	private int background;
 	
 	public GameView(Context context, AttributeSet attrs){
 		super(context, attrs);		
 		
 		//Get surface holder
 		holder = getHolder();
 		holder.addCallback(this);
 		
 		//Threading
 		thread = new Thread(this);
 		running = false;
 		
 		//Add sprite
 		sprites = new ArrayList<Sprite>();
 		sprites.add(new Sprite(getResources()));
 		
 		//Background
 		background = Color.GRAY;
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent e){
 		switch(e.getAction()){
 			case MotionEvent.ACTION_DOWN:
 			{
 				//Only handle if less than 5 sprites on screen
				if (sprites.size() >= 50)
 					return false;
 				
 				//Add new sprite
				synchronized(holder){
					Point2D position = new Point2D( MathHelper.Clamp((int) e.getX(), 0, getWidth() - 72), MathHelper.Clamp((int) e.getY(), 0, getHeight() - 72));
					sprites.add(new Sprite(getResources(), position.getX(), position.getY()));
					return true;
				}
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height){}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		//Start thread
 		thread.start();
 		running = true;
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		try {
 			thread.join();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void run() {
 		//Loop while running is true
 		while (running){
			Canvas c = null;
			try{
				c = holder.lockCanvas();
				synchronized(holder){
					update();
					onDraw(c);
				}
			} finally {
				if (c != null)
					holder.unlockCanvasAndPost(c);
			}
 		}
 	}
 	
 	
 	@Override
 	protected void onDraw(Canvas canvas){
 		canvas.drawColor(background);
 		for(Sprite sprite : sprites)
 			sprite.draw(canvas);
 	}
 
 	private void update() {
 		background = Color.GRAY;
 		ArrayList<Sprite> dead = new ArrayList<Sprite>(sprites.size());
 		for(Sprite sprite : sprites)
 		{
 			if (sprite.isDead())
 				dead.add(sprite);
 			else
 			{
 				sprite.update(getWidth(), getHeight());
 				if (sprite.isColliding())
 					background = Color.BLACK;
 			}
 		}
 		
 		for(Sprite sprite : dead)
 			sprites.remove(sprite);			
 	}
 	
 }
