 package mobile.team4.game;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public class Game extends SurfaceView implements SurfaceHolder.Callback {
 
 	GameMap map = new GameMap(MAP_WIDTH, MAP_HEIGHT);
 	
 	GameLoopThread _thread;
 	Boolean isRunning;
 	Boolean isPaused;
 	float deltaTime;
 	
 	static int MAP_WIDTH = 10;
 	static int MAP_HEIGHT = 10;
 	
 	ArrayList<Cannon> cannon_list;
 	ArrayList<WallPiece> wall_list;
 	ArrayList<BackgroundPiece> background_list;
 	ArrayList<Castle> castle_list;
 	ArrayList<Shot> shot_list;	//  For cannonballs.
 	GameMap game_map = new GameMap(MAP_WIDTH, MAP_HEIGHT);
 
 	
 	public Game(Context context) {
 		super(context);
 		getHolder().addCallback(this);
         _thread = new GameLoopThread(getHolder(), this);
         setFocusable(true);
         
 		cannon_list = new ArrayList<Cannon>();
 		wall_list = new ArrayList<WallPiece>();
 		background_list = new ArrayList<BackgroundPiece>();
 		castle_list = new ArrayList<Castle>();
 		shot_list = new ArrayList<Shot>();
 	}
 	
 	public void placeWall(Point position, Shape shape) {
 		for (Point point : shape.points) {
			wall_list.add(new WallPiece(position.x + point.x, position.y + point.y));
 			game_map.placeWall(position, shape);
 		}
 	}
 
 	public void updateAnimations() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void updateSound() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void updateInput() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void updateState() {
 		// TODO Auto-generated method stub
 		
 		
 	}
 	
 	public void updateVideo(Canvas c) {
 		Bitmap wall = BitmapFactory.decodeResource(getResources(), R.drawable.wall);
 		Bitmap castle = BitmapFactory.decodeResource(getResources(), R.drawable.castle);
 		Bitmap cannonball = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
 		Bitmap cannon = BitmapFactory.decodeResource(getResources(), R.drawable.cannon);
 		
 		int gridHeight = getHeight() / map.getHeight();
 		int gridWidth = gridHeight;
 		
 		wall = resizeBitmap(wall, gridHeight, gridWidth);
 		castle = resizeBitmap(castle, gridHeight, gridWidth);
 		cannon = resizeBitmap(cannon, gridHeight, gridWidth);
 		
 		for(int i = 0; i < castle_list.size(); i++) {
 			Point p = castle_list.get(i).getPosition();
			c.drawBitmap(castle, p.x * gridWidth, p.y * gridHeight, null);
 		}
 		for(int i = 0; i < cannon_list.size(); i++) {
 			Point p = cannon_list.get(i).getPosition();
			c.drawBitmap(cannon, p.x * gridWidth, p.y * gridHeight, null);
 		}
 		for(int i = 0; i < wall_list.size(); i++) {
 			Point p = wall_list.get(i).getPosition();
			c.drawBitmap(wall, p.x * gridWidth, p.y * gridHeight, null);
 		}
 		for(int i = 0; i < shot_list.size(); i++) {
 			Point p = shot_list.get(i).getPosition();
			c.drawBitmap(cannonball, p.x * gridWidth, p.y * gridHeight, null);
 		}
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder holder, int format, int width,
 			int height) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder holder) {
 		_thread.setRunning(true);
         _thread.start();		
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder holder) {
 		boolean retry = true;
         _thread.setRunning(false);
         while (retry) {
             try {
                 _thread.join();
                 retry = false;
             } catch (InterruptedException e) {
             }
         }		
 	}
 	
 	private Bitmap resizeBitmap(Bitmap bm, int newHeight, int newWidth) {
 		int width = bm.getWidth();
 		int height = bm.getHeight();
 		float widthScale = ((float)newWidth) / width;
 		float heightScale = ((float)newHeight) / height;
 		Matrix matrix = new Matrix();
 		matrix.postScale(widthScale, heightScale);
 		Bitmap resizedbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
 		return resizedbm;
 	}
 }
