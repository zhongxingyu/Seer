 package mobile.team4.game;
 
 import java.util.ArrayList;
 
 import mobile.team4.game.BackgroundPiece.Type;
 import mobile.team4.game.GameMap.Pieces;
 
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
 	int gridHeight, gridWidth;
 	
 	ArrayList<Cannon> cannon_list;
 	ArrayList<WallPiece> wall_list;
 	ArrayList<BackgroundPiece> background_list;
 	ArrayList<Castle> castle_list;
 	ArrayList<Shot> shot_list;	//  For cannonballs.
 	GameMap game_map = new GameMap(MAP_WIDTH, MAP_HEIGHT);
 	Bitmap wall, castle, cannonball, cannon, grass, water, floor;
 	
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
 		
 		for (int i = 0; i < MAP_WIDTH; i++) {
 			for (int j = 0; j < MAP_HEIGHT; j++) {
 				BackgroundPiece bg = new BackgroundPiece(Type.Grass);
 				Point p = new Point(i, j);
 				bg.setPosition(p);
 				background_list.add(bg);
 			}
 		}
		
 		wall = BitmapFactory.decodeResource(getResources(), R.drawable.wall);
 		castle = BitmapFactory.decodeResource(getResources(), R.drawable.castle);
 		cannonball = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
 		cannon = BitmapFactory.decodeResource(getResources(), R.drawable.cannon);
 		grass = BitmapFactory.decodeResource(getResources(), R.drawable.grass);
 		water = BitmapFactory.decodeResource(getResources(), R.drawable.water);
 		floor = BitmapFactory.decodeResource(getResources(), R.drawable.floor);
 		
 		gridHeight = getHeight() / MAP_HEIGHT;
 		gridWidth = gridHeight;
 		
 		wall = resizeBitmap(wall, gridHeight, gridWidth);
 		castle = resizeBitmap(castle, 2 * gridHeight, 2 * gridWidth);
 		cannon = resizeBitmap(cannon, 2 * gridHeight, 2 * gridWidth);
 	}
 	
 	public void placeWall(Point position, Shape shape) {
 		for (Point point : shape.points) {
 			wall_list.add(new WallPiece(position.get_x() + point.get_x(), position.get_y() + point.get_y()));
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
 		for(int i = 0; i < background_list.size(); i++) {
 			Point p = background_list.get(i).getPosition();
 			if(background_list.get(i).getType() == Type.Floor) {
 				c.drawBitmap(floor, p.get_x() * gridWidth, p.get_y() * gridHeight, null);
 			} else if(background_list.get(i).getType() == Type.Grass) {
 				c.drawBitmap(grass, p.get_x() * gridWidth, p.get_y() * gridHeight, null);
 			} else if(background_list.get(i).getType() == Type.Water){
 				c.drawBitmap(water, p.get_x() * gridWidth, p.get_y() * gridHeight, null);
 			}
 		}
 		for(int i = 0; i < castle_list.size(); i++) {
 			Point p = castle_list.get(i).getPosition();
 			c.drawBitmap(castle, p.get_x() * gridWidth, p.get_y() * gridHeight, null);
 		}
 		for(int i = 0; i < cannon_list.size(); i++) {
 			Point p = cannon_list.get(i).getPosition();
 			c.drawBitmap(cannon, p.get_x() * gridWidth, p.get_y() * gridHeight, null);
 		}
 		for(int i = 0; i < wall_list.size(); i++) {
 			Point p = wall_list.get(i).getPosition();
 			c.drawBitmap(wall, p.get_x() * gridWidth, p.get_y() * gridHeight, null);
 		}
 		for(int i = 0; i < shot_list.size(); i++) {
 			Point p = shot_list.get(i).getPosition();
 			c.drawBitmap(cannonball, p.get_x() * gridWidth, p.get_y() * gridHeight, null);
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
