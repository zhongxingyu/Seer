 package k.android.TD1;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class CoreActivity extends Activity{
 
 	// Member Variables
 	GameView m_gameView;
 	static Tower m_inputTower = null;
	static boolean m_hasTower = false;
 		
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		// Realize resources
 		Button newTowerButton = (Button) findViewById(R.id.newTowerButton_id);
 		
 		// set onClick methods for buttons
 		newTowerButton.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {
 				m_inputTower.setType(Tower.TYPE_1);
 				m_inputTower.visible = true;
 			}
 		});
 		
 		// Launch GameView
 		m_gameView = new GameView(this);
 		setContentView(R.layout.core);
 	}
 
 
 	public boolean onTouchEvent(MotionEvent event){
 		if(event.getAction() == MotionEvent.ACTION_UP){
			if(m_hasTower){
 				m_gameView.addTowerCloneToWorld(m_inputTower);
 				m_inputTower.visible = false;
 			}
 		}
 //			if(m_towerSourceBitmaps != null){
 //				Tower tmp = new Tower(m_towerSourceBitmaps.get(0));
 //				tmp.setCenter((int)event.getX(), (int)event.getY());
 //				m_towers.add(tmp);
 //			}
 		return true;
 	}
 
 	public static void setInputTower(Tower inputTower){
 		m_inputTower = inputTower;
 	}
 	
 	public void loadTowerResources(){
 		
 	}
 }
 
 
 
 class GameView extends SurfaceView implements SurfaceHolder.Callback{
 
 	// Member Variables
 	private int m_level = LevelCode.UNKNOWN_LEVEL;
 	private GameThread m_gameThread;
 	private GraphicObject m_background;
 //	private ArrayList<Bitmap> m_towerSourceBitmaps = new ArrayList<Bitmap>();
 	private ArrayList<Bitmap> m_creepSourceBitmaps = new ArrayList<Bitmap>();
 	private ArrayList<Tower> m_towers = new ArrayList<Tower>();
 	private ArrayList<Creep> m_creeps = new ArrayList<Creep>();
 	private ArrayList<CreepPath> m_paths = new ArrayList<CreepPath>();
 	private Random m_random = new Random();
 	
 	private static final String TAG = "GameView";
 	
 	private int m_bgId = R.drawable.choose;
 	
 //	private int[] m_towerIds = {
 //		R.drawable.tower1,
 //		R.drawable.tower2,
 //		R.drawable.tower3
 //	};
 	
 	private int[] m_creepIds = {
 		R.drawable.creep1,	
 		R.drawable.creep2,
 		R.drawable.creep3
 	};
 
 	// Constructors
 	public GameView(Context context) {
 		super(context);
 		getHolder().addCallback(this);
 		m_gameThread = new GameThread(getHolder(), this);
 		setFocusable(true);
 		m_level = MainActivity.m_level;
 		m_bgId = getBgResId();
 	}
 	
 	public GameView(Context context,  AttributeSet attrs) {
 		super(context, attrs);
 		getHolder().addCallback(this);
 		m_gameThread = new GameThread(getHolder(), this);
 		setFocusable(true);
 		m_level = MainActivity.m_level;
 		m_bgId = getBgResId();
 	}
 	
 	public GameView(Context context,  AttributeSet attrs, int defStyle) {
 		super(context, attrs);
 		getHolder().addCallback(this);
 		m_gameThread = new GameThread(getHolder(), this);
 		setFocusable(true);
 		m_level = MainActivity.m_level;
 		m_bgId = getBgResId();
 	}
 
 	// Surface Functions
 	@Override
 	public void surfaceCreated(SurfaceHolder arg0) {
 //		loadTowerSourceBitmaps(m_towerIds);
 		loadTowerBitmapSources();
 		loadCreepSourceBitmaps(m_creepIds);
 		loadBackgroundSourceBitmap(m_bgId);
 		
 //		CoreActivity.setInputTower(new Tower(m_towerSourceBitmaps.get(0)));
 		
 		if(m_gameThread.getState() == Thread.State.TERMINATED){
 			m_gameThread = new GameThread(getHolder(), this);
 			m_gameThread.setRunning(true);
 			m_gameThread.start();
 		}else{
 			m_gameThread.setRunning(true);
 			m_gameThread.start();
 		}
 		
 		initPaths(m_paths);
 		
 //		for(int i = 0; i < m_paths.size(); i++){
 //			Log.v(TAG, m_paths.get(i).points.toString());
 //		}
 	}
 	
 	@Override
 	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder arg0) {
 		boolean retry = true;
 		m_gameThread.setRunning(false);
 		
 		while(retry){
 			try{
 				m_gameThread.join();
 				retry = false;
 			}catch(InterruptedException e){
 				// Try again...
 			}
 		}
 	}
 	
 	// Member Functions
 	public void onDraw(Canvas canvas){
 		
 		// May add more creeps
 		if(m_random.nextInt() % 3 == 0){
 			if(m_creepSourceBitmaps.size() == 0)
 				Log.d(TAG, "No creep source");
 			int tmpRand = m_random.nextInt(3);
 			Creep tmp = new Creep(m_creepSourceBitmaps.get(tmpRand), tmpRand);
 			tmp.setOnPath(m_paths.get(m_random.nextInt(10)));
 			m_creeps.add(tmp);
 		}
 		
 		if(canvas != null){
 			canvas.drawColor(Color.BLACK);
 			if(m_background != null && m_background.bitmap != null)
 				canvas.drawBitmap(m_background.bitmap, null, canvas.getClipBounds(), null);
 		}
 		
 		for(int i = 0; i < m_towers.size(); i++)
 			drawTower(canvas, m_towers.get(i));
 		
 		for(int i = 0; i < m_creeps.size(); i++){
 			if(m_creeps.get(i).advanceAlongPath()){
 				m_creeps.get(i).onDeath();
 				m_creeps.remove(i);
 				if(i > 0) i--;
 			}
 			drawCreep(canvas, m_creeps.get(i));
 		}
 	}
 
 //	public boolean onTouchEvent(MotionEvent event){
 //		if(event.getAction() == MotionEvent.ACTION_DOWN)
 //			if(towerSourceBitmaps != null){
 //				Tower tmp = new Tower(towerSourceBitmaps.get(0));
 //				tmp.setCenter((int)event.getX(), (int)event.getY());
 //				m_towers.add(tmp);
 //			}
 //		return true;
 //	}
 	
 	public void drawTower(Canvas canvas, Tower tower){
 		if(canvas != null && tower != null && tower.bitmap != null && tower.visible){
 			canvas.drawBitmap(tower.bitmap, tower.x, tower.y, null);
 			for(int i = 0; i < tower.attackMethods.size(); i++)
 				tower.attackMethods.get(i).drawSelf(canvas);
 //				drawAttackMethod(canvas, tower.attackMethods.get(i));
 		}
 	}
 	
 	public void drawCreep(Canvas canvas, Creep creep){
 		if(canvas != null && creep != null && creep.bitmap != null)
 			canvas.drawBitmap(creep.bitmap, creep.x, creep.y, null);
 	}
 	
 //	public void drawAttackMethod(Canvas canvas, AttackMethod attackMethod){
 //		
 //	}
 	
 /*
 	public void loadTowerSourceBitmaps(int[] towerIds){
 		if(towerIds != null)
 			for(int i = 0; i < towerIds.length; i++)
 				m_towerSourceBitmaps.add(BitmapFactory.decodeResource(getResources(), towerIds[i]));
 	}
 */
 	
 	public void loadTowerBitmapSources(){
 		if(Tower.towerBitmapIds != null)
 			for(int i = 0; i < Tower.towerBitmapIds.length; i++)
 				Tower.towerBitmapSources.add(BitmapFactory.decodeResource(getResources(), Tower.towerBitmapIds[i]));
 	}
 	
 	public void loadCreepSourceBitmaps(int[] creepIds){
 		if(creepIds != null)
 			for(int i = 0; i < creepIds.length; i++)
 				m_creepSourceBitmaps.add(BitmapFactory.decodeResource(getResources(), creepIds[i]));
 	}
 	
 
 	public void loadBackgroundSourceBitmap(int backgroundId){
 		if(backgroundId != 0)
 			m_background = new GraphicObject(BitmapFactory.decodeResource(getResources(), backgroundId));
 	}
 
 	
 	public int getBgResId(){
 		switch(m_level){
 		case LevelCode.JUNGLE_LEVEL:
 			return R.drawable.jungle;
 		case LevelCode.SWAMP_LEVEL:
 			return R.drawable.swamp;
 		case LevelCode.DESERT_LEVEL:
 			return R.drawable.desert;
 		case LevelCode.LAKE_LEVEL:
 			return R.drawable.lake;
 		case LevelCode.OCEAN_LEVEL:
 			return R.drawable.ocean;
 		case LevelCode.FOREST_LEVEL:
 			return R.drawable.forest;
 		case LevelCode.TUNDRA_LEVEL:
 			return R.drawable.tundra;
 		case LevelCode.CANYON_LEVEL:
 			return R.drawable.canyon;
 		case LevelCode.VALLEY_LEVEL:
 			return R.drawable.valley;
 		case LevelCode.MOUNTAIN_LEVEL:
 			return R.drawable.mountain;
 		case LevelCode.PLATEAU_LEVEL:
 			return R.drawable.plateau;
 		case LevelCode.MOON_LEVEL:
 			return R.drawable.moon;
 		default:
 			return R.drawable.choose;
 		}
 	}
 	
 	public void initPaths(ArrayList<CreepPath> paths){
 		for(int i = 0; i < 10; i++){
 			CreepPath tmp = new CreepPath();
 			if(this.getHeight() != 0){
 				tmp.addPoint(new Point(-20, Math.abs(m_random.nextInt()) % this.getHeight()));
 				tmp.addPoint(new Point(this.getWidth() + 20, Math.abs(m_random.nextInt()) % this.getHeight()));
 				paths.add(tmp);
 			}else{
 				Log.v(TAG, "Height/Width is zero!!!");
 			}
 		}
 	}
 	
 	// TODO Continue here - call this function someday
 	public void addTowerCloneToWorld(Tower tower){
 		m_towers.add(new Tower(tower));
 	}
 }
 
 
 
 
 class GameThread extends Thread{
 	SurfaceHolder m_surfaceHolder;
 	GameView m_gameView;
 	boolean isRunning = false;
 	
 	public GameThread(SurfaceHolder holder, GameView game){
 		m_surfaceHolder = holder;
 		m_gameView = game;
 	}
 	
 	public void setRunning(boolean r){
 		isRunning = r;
 	}
 	
 	public SurfaceHolder getSurfaceHolder(){
 		return m_surfaceHolder;
 	}
 	
 	public void run(){
 		Canvas c;
 		while(isRunning){
 			c = null;
 			try{
 				c = m_surfaceHolder.lockCanvas();
 				synchronized (m_surfaceHolder){
 					m_gameView.onDraw(c);
 				}
 			}finally{
 				if(c != null)
 					m_surfaceHolder.unlockCanvasAndPost(c);
 			}
 		}
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
