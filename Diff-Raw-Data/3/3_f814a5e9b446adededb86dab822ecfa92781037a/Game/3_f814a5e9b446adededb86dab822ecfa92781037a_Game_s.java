 package mobile.team4.game;
 
 import java.util.ArrayList;
 
 import mobile.team4.game.GameState.Mode;
 
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
 	
 	ArrayList<Shot> shot_list;	//  For cannonballs.
 	GameMap game_map = new GameMap(MAP_WIDTH, MAP_HEIGHT);
 	Bitmap wall, castle, cannonball, cannon, grass, water, floor;
 	Server server;
 	GameState state;
 	Mode mode;
 	Timer stateTimer;
 	
 	public Game(Context context) {
 		super(context);
 		getHolder().addCallback(this);
         _thread = new GameLoopThread(getHolder(), this);
         setFocusable(true);
         
 		shot_list = new ArrayList<Shot>();
 		server = Server.getInstance();
 		//server.newGame();
 		mode = Mode.CANNONS;
 		stateTimer = new Timer();
 		stateTimer.start();
 	}
 	
 	public void init() {
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
 			game_map.placeWall(position, shape);
 		}
 	}
 	
 	public void deleteFrom(Point position) {
 		
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
 		/*
 		if(stateTimer.getElapsedTime() > 200) {		// Calls 5 times per second.
 			state = server.getGameState();
 			mode = state.mode;
 			stateTimer.start();
 		}
 		for(int i = 0; i < state.cannons.size(); i++) {
 			Point p = state.cannons.get(i);
 			Cannon c = new Cannon(p);
 			map.insert_at(p, c);
 		}
 		for(int i = 0; i < state.walls.size(); i++) {
 			Point p = state.walls.get(i);
 			WallPiece w = new WallPiece(p);
 			map.insert_at(p, w);
 		}
 		for(int i = 0; i < state.shots.size(); i++) {
 			Shot s = state.shots.get(i);
 			shot_list.add(s);
 		} */
 	}
 	
 	public void updateVideo(Canvas c) {
 		for(int i = 0; i < MAP_WIDTH; i++) {
 			for(int j = 0; j < MAP_HEIGHT; j++) {
 				GameObject toDraw = map.get_at(i, j);
 				Point p = toDraw.getPosition();
 				switch(toDraw.getType()) {
 					case Floor:
 						c.drawBitmap(floor, p.get_x() * gridWidth, p.get_y() * gridHeight, null); break;
 					case Grass:
 						c.drawBitmap(grass, p.get_x() * gridWidth, p.get_y() * gridHeight, null); break;
 					case Water:
 						c.drawBitmap(water, p.get_x() * gridWidth, p.get_y() * gridHeight, null); break;
 					case Cannon:
 						c.drawBitmap(cannon, p.get_x() * gridWidth, p.get_y() * gridHeight, null); break;
 					case Castle:
 						c.drawBitmap(castle, p.get_x() * gridWidth, p.get_y() * gridHeight, null); break;
 					case Wall:
 						c.drawBitmap(wall, p.get_x() * gridWidth, p.get_y() * gridHeight, null); break;					
 				}
 			}
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
 		init();
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
