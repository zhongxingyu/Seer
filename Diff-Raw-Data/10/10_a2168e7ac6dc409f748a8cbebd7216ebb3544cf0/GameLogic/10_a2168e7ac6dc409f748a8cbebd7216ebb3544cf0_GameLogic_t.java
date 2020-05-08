 package game.open2d;
 
 import java.util.LinkedHashMap;
 
 import object.Enemy;
 import object.Enemy.EnemyState;
 import object.Player.PlayerState;
 import object.GameObject;
 import object.Player;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.view.MotionEvent;
 import engine.open2d.renderer.WorldRenderer;
 
 public class GameLogic extends AsyncTask<Void, Void, Void>{
 	private static final int GESTURE_INTERVAL_CHECK = 4;
 	
 	public static float CAM_X_CHANGE = 0.65f;
 	public static float CAM_Y_CHANGE = 0.65f;
 	public static float CAM_Z_CHANGE = 0.65f;
 	public static float CAM_BUFFER = 1.0f;
 	
 	WorldRenderer worldRenderer;
 	Context context;
 	
 	float camX = 0.0f;
 	float camY = 0.0f;
 	float camZ = 0.0f;
 	
 	int enemyLimit = 2;
 	
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
 										new String[]{"a_Position","a_Color","a_Normal","a_TexCoordinate"}
 									);
 		
 		gameObjects = new LinkedHashMap<String,GameObject>();
 		
 		Player player = new Player(gameObjects, 3.0f, -1.0f, 3.5f, 3.5f);
 		player.loadAnimIntoRenderer(worldRenderer);
 		
 		Enemy enemy0 = new Enemy(gameObjects, player, 0, -3.7f, -1.0f, 3.5f, 3.5f);
 		enemy0.loadAnimIntoRenderer(worldRenderer);
 		
 		gameObjects.put(player.getName(), player);
 		gameObjects.put(enemy0.getName(), enemy0);
 	}
 	
 	public void update(){
 		if(gameObjects.size() < enemyLimit){
 			Enemy enemy0 = new Enemy(gameObjects, (Player)gameObjects.get("player"), 0, (float)(3.7f*Math.random()-7.4f*Math.random()), -1.0f, 3.5f, 3.5f);
 			enemy0.loadAnimIntoRenderer(worldRenderer);
 			gameObjects.put(enemy0.getName(), enemy0);
 		}
 		
 		for(GameObject gameObject : gameObjects.values()){
 			gameObject.update();
 			if(gameObject instanceof Enemy){
 				Enemy enemy = (Enemy)gameObject;
 				if(enemy.getEnemyState() == EnemyState.DEAD){
 					gameObject.unloadAnimFromRenderer(worldRenderer);
 					gameObjects.remove(gameObject.getName());
 				}
 			}
 		}
 	}
 	
 	public void draw(){
 		worldRenderer.setCamera(camX, camY, camZ);
 		Player player = null;
 		for(GameObject gameObject : gameObjects.values()){
 			gameObject.draw(worldRenderer);
 			if(gameObject instanceof Player){
 				player = (Player)gameObject;
 			}
 		}
 		
 		if(player.getPlayerState() == PlayerState.FINISH){
 //			finishCamZoom(player);
 			camZoomTo(	player.getX()+player.getWidth()/2,
 						player.getY()+player.getHeight()/2,
 						player.getZ()-0.5f,
 						0.7f
 					);
 		} else {
 			camZoomTo(	0.0f,
 						0.0f,
 						0.0f,
 						0.7f
 				);
 		}
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
 		
 //		if(checkZ + CAM_BUFFER > camZ){
 //			camZ += CAM_Z_CHANGE;
 //		} else
 		if (checkZ < camZ){
 			camZ -= CAM_Z_CHANGE;
 		}
 	}
 	
 	public void passTouchEvents(MotionEvent e){
 		if(e.getAction() == MotionEvent.ACTION_DOWN){
 			gestureX = e.getX();
 			gestureY = e.getY();
 			gestureCheck = 0;
 			for(GameObject gameObject : gameObjects.values()){
 				if(gameObject instanceof Enemy)
 					gameObject.passTouchEvent(e, worldRenderer);
 			}
 		} else if(e.getAction() == MotionEvent.ACTION_UP){
 			gestureCheck = 0;
 		} else if(e.getAction() == MotionEvent.ACTION_MOVE){
 			gestureCheck++;
 			Log.d("gesture", gestureCheck+"");
 			if(gestureCheck > GESTURE_INTERVAL_CHECK){
 				gestureCheck = 0;
 			}
 		}
 		
 		Player player = (Player) gameObjects.get(Player.OBJNAME);
 		player.passTouchEvent(e, worldRenderer);
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
 	            draw();
 	            next_game_tick += SKIP_TICKS;
 	        }
 	    }
 	    
 	    return null;
 	}
 }
