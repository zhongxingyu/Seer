 package com.rampantmonk3y.p0ng;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import com.rampantmonk3y.p0ng.p0ng_game.GraphicObject.Coordinates;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class p0ng_game extends Activity {
 	private Panel _p;
 	private int aiPaddleSpeed = 10;
 	private int maxDirectionalSpeed = 25;
 	Random generator;
 	private dbmanager db;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         _p = new Panel(this);
         setContentView(_p);
         generator = new Random();
         try{db = new dbmanager(this);
         }catch(Exception e){};
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu){
     	MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.gamemenu, menu);
     	return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item){
     	//handle item selection
     	switch (item.getItemId()){
     	case R.id.game_ball:
     		_p.startBall();
     		return true;
     	case R.id.game_new:
     		_p.newGame();
     		return true;
     	case R.id.game_home:
     		_p.saveHighScore();
     		Intent myIntent = new Intent(this, p0ng_main.class);
     		startActivityForResult(myIntent, 0);
     		return true;
     	default:
     		return super.onOptionsItemSelected(item);
     	}
     }
     
     class Panel extends SurfaceView implements SurfaceHolder.Callback{
     	private gamethread _thread;
     	private ArrayList<GraphicObject> _graphics;
     	GraphicObject _paddle;
     	private int userScore = 0;
     	private int aiScore = 0;
     	public GraphicObject background; 
     	
     	private Paint _paint = new Paint();
     	
         private void saveHighScore(){
         	if(userScore > 0){//Add score to db.
         		try{
         			db.addRow(Integer.toString(userScore), Integer.toString(aiScore));
         		}catch(Exception e){
         			Log.e("Add Error", e.toString());
         			e.printStackTrace();
         		}
         	}
         }
     	
         private void newGame(){
         	synchronized (_thread.getSurfaceHolder()){
         		_graphics = new ArrayList<GraphicObject>();
         		_paddle = new GraphicObject(BitmapFactory.decodeResource(getResources(), R.drawable.paddle));
         		_paddle.getCoordinates().setX(0);
         		_paddle.getCoordinates().setY((_p.getHeight()-_paddle.getGraphic().getHeight())/2);
         		_paddle.getSpeed().setX(0);
         		_paddle.getSpeed().setY(0);
         		_paddle.setRemovable(false);
         		_graphics.add(_paddle);
         		GraphicObject _aipaddle = new GraphicObject(BitmapFactory.decodeResource(getResources(), R.drawable.paddle));
         		_aipaddle.getCoordinates().setX(this.getWidth() - _aipaddle.getGraphic().getWidth());
         		_aipaddle.getCoordinates().setY((this.getHeight()-_aipaddle.getGraphic().getHeight())/2);
         		_aipaddle.getSpeed().setX(0);
         		_aipaddle.getSpeed().setY(aiPaddleSpeed);
         		_aipaddle.setRemovable(false);
         		_graphics.add(_aipaddle);
         		_paint.setColor(Color.WHITE);
         		_paint.setAntiAlias(true);
         		_paint.setTextSize(50);
         		background = new GraphicObject(BitmapFactory.decodeResource(getResources(), R.drawable.gameboard));
     		}
         	saveHighScore();
         	userScore = 0;
         	aiScore = 0;
         }
         
         private void startBall(){
         	synchronized (_thread.getSurfaceHolder()){
         		GraphicObject _newBall = new GraphicObject(BitmapFactory.decodeResource(getResources(), R.drawable.ball));
         		_newBall.getCoordinates().setX((this.getWidth() - _newBall.getGraphic().getWidth())/2);
         		_newBall.getCoordinates().setY((this.getHeight() - _newBall.getGraphic().getHeight())/2);
         		_newBall.getSpeed().setX(generator.nextInt(maxDirectionalSpeed));
         		_newBall.getSpeed().setY(generator.nextInt(maxDirectionalSpeed));
         	    if(generator.nextInt(2) == 0) _newBall.getSpeed().setXDirection(-1);
         	    if(generator.nextInt(2) == 0) _newBall.getSpeed().setYDirection(-1);
         		_graphics.add(_newBall);
         	}
         }
         
     	@Override
     	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
     	}
     	
     	@Override
     	public void surfaceCreated(SurfaceHolder holder){
     		_thread.setRunning(true);
     		_thread.start();
     		newGame();
     	}
     	
     	@Override
     	public void surfaceDestroyed(SurfaceHolder holder){
     		boolean retry = true;
     		_thread.setRunning(false);
     		while (retry){
     			try{
     				_thread.join();
     				retry = false;
     			} catch (InterruptedException e){
     				//Just keep trying!
     			}
     		}
     	}
     	
     	public Panel(Context context){
     		super(context);
     		getHolder().addCallback(this);
     		_thread = new gamethread(getHolder(), this);
     	}
     	
     	public void onDraw(Canvas canvas){
     		Bitmap bitmap;
     		Coordinates coords;
     		bitmap = background.getResizedGraphic(this.getHeight(), this.getWidth());
     		canvas.drawBitmap(bitmap, 0, 0, null);
     		for(GraphicObject graphic : _graphics){
     			if(!graphic.removed){
     			bitmap = graphic.getGraphic();
     			coords = graphic.getCoordinates();
     			canvas.drawBitmap(bitmap, coords.getX(), coords.getY(), null);
     			}
     		}
     		canvas.drawText(Integer.toString(userScore), this.getWidth()/2 - 75, 60, _paint);
     		canvas.drawText(Integer.toString(aiScore), this.getWidth()/2 + 20, 60, _paint);
     	}
     	
     	public boolean onTouchEvent (MotionEvent event){
     		synchronized (_thread.getSurfaceHolder()){
     			if ((int)event.getX()<this.getWidth()/2)
         		_paddle.getCoordinates().setY((int)event.getY() - _paddle.getGraphic().getHeight()/2);
         		_graphics.set(0, _paddle);
         		return true;
     		}
     	}
     	
     	public void updatePhysics(){
     		GraphicObject.Coordinates coord;
     		GraphicObject.Speed speed;
     		int x = 0;
     		for (GraphicObject graphic : _graphics){
     			if(!graphic.removed){
     				coord = graphic.getCoordinates();
     				speed = graphic.getSpeed();
     			
     				if(speed.getXDirection() == GraphicObject.Speed.X_DIRECTION_RIGHT){
     					coord.setX(coord.getX() + speed.getX());
     				}else{
     					coord.setX(coord.getX() - speed.getX());
     				}
     				if(speed.getYDirection() == GraphicObject.Speed.Y_DIRECTION_DOWN){
     					coord.setY(coord.getY() + speed.getY());
     				}else{
     					coord.setY(coord.getY() - speed.getY());
     				}
     			
     				//Borders for x...
     				if(graphic.removable && coord.getX() < _paddle.getGraphic().getWidth() && speed.getXDirection() == GraphicObject.Speed.X_DIRECTION_LEFT){
     					GraphicObject temp = _graphics.get(0);//Check for collision with paddle
    					if(coord.getY() >= temp.getCoordinates().getY()-temp.getGraphic().getHeight()/2 && coord.getY() <= temp.getCoordinates().getY() + temp.getGraphic().getHeight()/2){
     						speed.toggleXDirection();
     					}else{
     						++aiScore;
     						graphic.removed = true;
     						_graphics.set(x, graphic);
     						}
     				}else if (graphic.removable && coord.getX() + graphic.getGraphic().getWidth() > getWidth() - _paddle.getGraphic().getWidth() && speed.getXDirection() == GraphicObject.Speed.X_DIRECTION_RIGHT){
     					GraphicObject temp = _graphics.get(1);//Check for collision with ai paddle
    					if(coord.getY() >= temp.getCoordinates().getY()+temp.getGraphic().getHeight()/2 && coord.getY() <= temp.getCoordinates().getY() + temp.getGraphic().getHeight()/2){
     						speed.toggleXDirection();
     					}else{
     						++userScore;
     						graphic.removed = true;
     						_graphics.set(x, graphic);
     					}
     				}
     			
     			//Borders for y...
     				if(coord.getY() < 0){
     					speed.toggleYDirection();
     					coord.setY(-coord.getY());
     				}else if (coord.getY() + graphic.getGraphic().getHeight() > getHeight()){
     					speed.toggleYDirection();
     					coord.setY(coord.getY() + getHeight() - (coord.getY() + graphic.getGraphic().getHeight()));
     					}
     				x++;
     		
     			}
     		}
     	}
     }
     
     class gamethread extends Thread{
     	private SurfaceHolder _surfaceHolder;
     	private Panel _panel;
     	private boolean _run = false;
     	
     	public gamethread(SurfaceHolder surfaceHolder, Panel panel){
     		_surfaceHolder = surfaceHolder;
     		_panel = panel;
     		
     	}
     	
     	public void setRunning(boolean run){
     		_run = run;
     	}
     	
     	@Override
     	public void run(){
     		Canvas c;
     		while(_run){
     			c = null;
     			try{
     				c = _surfaceHolder.lockCanvas(null);
     				synchronized (_surfaceHolder){
     					_panel.updatePhysics();
     					_panel.onDraw(c);
     				}
     				
     			}finally{
     				if(c != null){
     					_surfaceHolder.unlockCanvasAndPost(c);
     				}
     			}
     		}
     	}
     	
     	public SurfaceHolder getSurfaceHolder(){
     		return _surfaceHolder;
     	}
     }
     
     class GraphicObject {
     	
     	public class Speed{
     		public static final int X_DIRECTION_RIGHT = 1;
     		public static final int X_DIRECTION_LEFT = -1;
     		public static final int Y_DIRECTION_DOWN = 1;
     		public static final int Y_DIRECTION_UP = -1;
     		
     		private int _x = 1;
     		private int _y = 1;
     		
     		private int _xDirection = X_DIRECTION_RIGHT;
     		private int _yDirection = Y_DIRECTION_DOWN;
     		
     		
     		
     		public int getXDirection(){
     			return _xDirection;
     		}
     		
     		public void setXDirection(int direction){
     			_xDirection = direction;
     		}
     		
     		public void toggleXDirection(){
     			_xDirection *= -1;
     		}
     		
     		public int getYDirection(){
     			return _yDirection;
     		}
     		
     		public void setYDirection(int direction){
     			_yDirection = direction;
     		}
     		
     		public void toggleYDirection(){
     			_yDirection *= -1;
     		}
     		
     		public int getX(){
     			return _x;
     		}
     		
     		public void setX(int speed){
     			_x = speed;
     		}
     		
     		public int getY(){
     			return _y;
     		}
     		
     		public  void setY(int speed){
     			_y = speed;
     		}
     		
     		public String toString(){
     			String xDirection;
     			if(_xDirection == X_DIRECTION_RIGHT){
     				xDirection = "right";
     			}else {
     				xDirection = "left";
     			}
     			return "Speed: x: " + _x + " | y: " + _y + " | xDirection: " + xDirection;
     		}
     		
     	}
     	public class Coordinates{
     		private int _x = 100;
     		private int _y = 0;
     		
     		public int getX(){
     			return _x + _bitmap.getWidth()/2;
     		}
     		
     		public void setX(int value){
     			_x = value - _bitmap.getWidth()/2;
     		}
     		
     		public int getY(){
    			return _y + _bitmap.getHeight()/2;
     		}
     		
     		public void setY(int value){
    			_y = value - _bitmap.getHeight()/2;
     		}
     		
     		public String toString(){
     			return "Coordinates: (" + _x + "/" + _y + ")";
     		}
     	}
     	
     	private Bitmap _bitmap;
     	private Coordinates _coordinates;
     	private Speed _speed;
     	private boolean removable = true;
     	public boolean removed = false;
     	
     	public GraphicObject (Bitmap bitmap){
     		_bitmap = bitmap;
     		_coordinates = new Coordinates();
     		_speed = new Speed();
     	}
     	
     	public Bitmap getGraphic(){
     		return _bitmap;
     	}
     	
     	public Coordinates getCoordinates(){
     		return _coordinates;
     	}
     	
     	public Speed getSpeed(){
     		return _speed;
     	}
     	
 		public void setRemovable(boolean _r){
 			removable = _r;
 		}
 		
 		public boolean isRemovable(){
 			return removable;
 		}
 		
 		public Bitmap getResizedGraphic(int newHeight, int newWidth){
 			int width = _bitmap.getWidth();
 			int height = _bitmap.getHeight();
 			float scaleWidth = ((float) newWidth)/width;
 			float scaleHeight = ((float) newHeight)/height;
 			
 			Matrix _matrix = new Matrix();
 			
 			_matrix.postScale(scaleWidth, scaleHeight);
 			
 			Bitmap resized = Bitmap.createBitmap(_bitmap, 0, 0, width, height, _matrix, false);
 			return resized;
 			
 		}
     }
     
 }
