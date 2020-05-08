 package game;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import object.Enemy;
 import object.Enemy.EnemyState;
 import object.Player.PlayerState;
 import object.GameObject;
 import object.Player;
 import structure.ActionData;
 import structure.ActionDataTool;
 import structure.HitBox;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.webkit.WebView.FindListener;
 import android.widget.TextView;
 import engine.open2d.renderer.WorldRenderer;
 import game.GameTools.Gesture;
 import game.open2d.R;
 
 public class GameLogic extends AsyncTask<Void, Void, Void>{
 	private static final int DOUBLE_TAP_INTERVAL_MIN_CHECK = 3;
 	private static final int DOUBLE_TAP_INTERVAL_MAX_CHECK = 5;
 	private static final int GESTURE_INTERVAL_MIN_CHECK = 2;
 	private static final int GESTURE_INTERVAL_MAX_CHECK = 11;
 	
 	public static float CAM_X_CHANGE = 0.65f;
 	public static float CAM_Y_CHANGE = 0.65f;
 	public static float CAM_Z_CHANGE = 0.65f;
 	public static float CAM_BUFFER = 0.7f;
 	public static float CAM_X_DEFAULT = 0.0f;
 	public static float CAM_Y_DEFAULT = 2.0f;
 	public static float CAM_Z_DEFAULT = 1.0f;
 	
 	public static float GRAVITY = -0.098f;
 	public static float FLOOR = -1.0f;
 	public static float WALL_RIGHT = 5.0f;
 	public static float WALL_LEFT = -5.0f;
 	
 	WorldRenderer worldRenderer;
 	Context context;
 	
 	float camX = CAM_X_DEFAULT;
 	float camY = CAM_Y_DEFAULT;
 	float camZ = CAM_Z_DEFAULT;
 	
 	int enemyLimit = 1;
 	int enemyIndex;
 	
 	boolean gameRun = false;
 	
 	LinkedHashMap<String,GameObject> gameObjects;
 	
 	float gestureX;
 	float gestureY;
 	int gestureCheck;
 	
 	public GameLogic(Context context, WorldRenderer worldRenderer){
 		this.worldRenderer = worldRenderer;
 		this.context = context;
 
 		worldRenderer.addCustomShader(	WorldRenderer.WORLD_SHADER,
 										R.raw.vertex_shader_texture,
 										R.raw.fragment_shader_texture,
 										new String[]{"a_Position","a_Color","a_Normal","a_TexCoordinate","a_useTexture"}
 									);
 		
 		gameObjects = new LinkedHashMap<String,GameObject>();
 		
 		ActionDataTool parser = new ActionDataTool(context);
 		parser.readFile(R.raw.jack_frame_data);
 		List<ActionData> playerData = parser.parseFrameData();
 		
 		Player player = new Player(gameObjects, playerData, -3.0f, FLOOR);
 		player.loadAnimIntoRenderer(worldRenderer);
 		gameObjects.put(player.getName(), player);
 
 		enemyIndex = 1;
 		parser.readFile(R.raw.enemy_frame_data);
 		List<ActionData> enemyData = parser.parseFrameData();
 		//Enemy enemy = new Enemy(gameObjects, (Player)gameObjects.get("player"), 0, (float)(5.7f), -1.0f, 3.5f, 3.5f);
 		Enemy enemy = new Enemy(gameObjects, enemyData, (Player)gameObjects.get("player"), enemyIndex, 1.0f, -1.0f);
 		//enemy.getDisplay().drawDisable();
 		enemy.loadAnimIntoRenderer(worldRenderer);
 		gameObjects.put(enemy.getName(), enemy);
 
 		/*
 		enemyIndex = 2;
 		parser.readFile(R.raw.enemy_frame_data);
 		List<ActionData> enemyData2 = parser.parseFrameData();
 		Enemy enemy2 = new Enemy(gameObjects, enemyData2, (Player)gameObjects.get("player"), enemyIndex, -1.0f, -1.0f);
 		enemy2.loadAnimIntoRenderer(worldRenderer);
 		gameObjects.put(enemy2.getName(), enemy2);
 		*/
 	}
 	
 	public void update(){
 		if(gameObjects.size() <= enemyLimit && gameRun){
 			//Enemy enemy = new Enemy(gameObjects, (Player)gameObjects.get("player"), enemyIndex, (float)(3.7f*Math.random()-7.4f*Math.random()), -1.0f, 3.5f, 3.5f);
 			//enemy.loadAnimIntoRenderer(worldRenderer);
 			//gameObjects.put(enemy.getName(), enemy);
 			//enemyIndex++;
 		}
 		
 		ArrayList<String> removeObjects = new ArrayList<String>();
 		for(GameObject gameObject : gameObjects.values()){
 			gameObject.update();
 			if(gameObject instanceof Enemy){
 				Enemy enemy = (Enemy)gameObject;
 				if(enemy.getEnemyState() == EnemyState.DEAD){
 					removeObjects.add(gameObject.getName());
 					if(enemyLimit < 4){
 						//enemyLimit++;
 					}
 				}
 			} else if (gameObject instanceof Player){
 				Player player = (Player)gameObject;
 				if(player.getPlayerState() == PlayerState.DEAD){
 					gameRun = false;
 					enemyLimit = 1;
 				}
 			}
 		}
 
 		/*
 		if(!gameRun){
 			for(GameObject gameObject : gameObjects.values()){
 				if(gameObject instanceof Enemy){
 					removeObjects.add(gameObject.getName());
 				}
 			}
 		}
 		*/
 		for(Object remove:removeObjects.toArray()){
 			GameObject gameObject = gameObjects.get((String)remove);
 			
 			gameObject.unloadAnimFromRenderer(worldRenderer);
 			gameObjects.remove(gameObject.getName());
 //			float spawnLoc = Math.random() > 0.5 ? 1 : -1;
 //			gameObject.setX(spawnLoc*3.7f);
 		}
 	}
 	
 	public void updateDrawData(){
 		worldRenderer.setCamera(camX, camY, camZ);
 		Player player = null;
 		for(GameObject gameObject : gameObjects.values()){
 			gameObject.updateDrawData(worldRenderer);
 			if(gameObject instanceof Player){
 				player = (Player)gameObject;
 			}
 		}
 		
 		/*
 		if(player.isFinishState() || player.isCounterState()){
 			camZoomTo(	player.getX()+player.getWidth()/2,
 						player.getY()+player.getHeight()/2,
 						player.getZ()-0.5f,
 						CAM_BUFFER
 					);
 		} else {
 			camZoomTo(	CAM_X_DEFAULT,
 						CAM_Y_DEFAULT,
 						CAM_Z_DEFAULT,
 						CAM_BUFFER
 				);
 		}
 		*/
 	}
 
 	public void camZoomTo(float x, float y, float z, float buffer){
 		if(x > camX){
 			camX += CAM_X_CHANGE;
 		} else if (x < camX){
 			camX -= CAM_X_CHANGE;
 		}
 		
 		if(camX < x + buffer && camX > x - buffer){
 			camX = x;
 		}
 		
 		if(y > camY){
 			camY += CAM_Y_CHANGE;
 		} else if (y < camY){
 			camY -= y;
 		}
 		
 		if(camY < y + buffer&& camY > y - buffer){
 			camY = y;
 		}
 		
 		if(z > camZ){
 			camZ += CAM_Z_CHANGE;
 		} else if (z < camZ){
 			camZ -= CAM_Z_CHANGE;
 		}
 		
 		if(camZ < z + buffer&& camZ > z - buffer){
 			camZ = z;
 		}
 		
 		
 	}
 	
 	public void finishCamZoom(Player player){
 		float checkX = player.getX()+player.getWidth()/2;
 		float checkY = player.getY()+player.getHeight()/2; 
 		float checkZ = player.getZ();
 		
 		if(checkX + CAM_BUFFER > camX && checkX - CAM_BUFFER < camX){
 			camX = checkX;
 		} else if(checkX > camX){
 			camX += CAM_X_CHANGE;
 		} else if (checkX < camX){
 			camX -= CAM_X_CHANGE;
 		}
 		
 		if(checkY + CAM_BUFFER > camY && checkY - CAM_BUFFER < camY){
 			camY = checkY;
 		} else if(checkY > camY){
 			camY += CAM_Y_CHANGE;
 		} else if (checkY - CAM_BUFFER < camY){
 			camY -= CAM_Y_CHANGE;
 		}
 		
 		if (checkZ < camZ){
 			camZ -= CAM_Z_CHANGE;
 		}
 	}
 	
 	//USE THIS FOR SWIPES
 	public Gesture gestureProcessing(MotionEvent e, GestureListener listner){
 		if(e.getAction() == MotionEvent.ACTION_DOWN){
 			gestureX = e.getX();
 			gestureY = e.getY();
 			gestureCheck = 0;
 		} else if(e.getAction() == MotionEvent.ACTION_UP){
 			if(gestureCheck > GESTURE_INTERVAL_MIN_CHECK && gestureCheck < GESTURE_INTERVAL_MAX_CHECK){
 				gestureCheck = 0;
 				return GameTools.gestureDetection(gestureX, e.getX(), gestureY, e.getY()).setxTap(e.getX()).setyTap(e.getY());
 			} else {
 				gestureCheck = 0;
 				return Gesture.TAP.setxTap(e.getX()).setyTap(e.getY());
 			}
 		} else if(e.getAction() == MotionEvent.ACTION_MOVE){
 			gestureCheck++;
 		}
 		
 		return Gesture.NONE;
 	}
 	
 	public void passTouchEvents(MotionEvent e, GestureListener dTapListner){
 		Player player = (Player) gameObjects.get(Player.OBJNAME);
 		Gesture gesture = gestureProcessing(e, dTapListner);
 		
 		if(e.getAction() == MotionEvent.ACTION_DOWN){
 			for(GameObject gameObject : gameObjects.values()){
 				if(gameObject instanceof Enemy)
 					gameObject.passTouchEvent(e, worldRenderer);
 			}
 		} else if(e.getAction() == MotionEvent.ACTION_UP){
 			if(gesture != Gesture.NONE && gesture != Gesture.TAP){
 				//Log.d("rising_debug_gameLogic_passtouch",gesture.toString());
 				player.addGesture(gesture);
				dTapListner.setdTapped(false);
 			}else if(dTapListner.isdTapped()){//for if want swipe as precedence dtap 
 				player.passDoubleTouchEvent(dTapListner, worldRenderer);
 				dTapListner.setdTapped(false);
 			} else if(gesture == Gesture.TAP){
 				player.addGesture(gesture);
 			}
 			
 			if(gesture == Gesture.SWIPE_UP){
 				gameRun = true;
 			}
 			
 		} else if(e.getAction() == MotionEvent.ACTION_MOVE){
 		}
 		
 		player.passTouchEvent(e, worldRenderer);
 	}
 	
 	public void passDoubleTouchEvents(GestureListener gesture){
 		Player player = (Player) gameObjects.get(Player.OBJNAME);
 		player.passDoubleTouchEvent(gesture, worldRenderer);
 		
 	}
 
 	@Override
 	protected Void doInBackground(Void... params) {
 		//TODO fix up later - find method for manual renders
 	    int TICKS_PER_SECOND = 24;
 	    int SKIP_TICKS = 1000 / TICKS_PER_SECOND;
 
 	    long next_game_tick = System.currentTimeMillis();
 
 	    boolean game_is_running = true;
 	    while( game_is_running ) {
 	        while( System.currentTimeMillis() > next_game_tick) {
 	            update();
 	            updateDrawData();
 	            next_game_tick += SKIP_TICKS;
 	        }
 	    }
 	    
 	    return null;
 	}
 }
