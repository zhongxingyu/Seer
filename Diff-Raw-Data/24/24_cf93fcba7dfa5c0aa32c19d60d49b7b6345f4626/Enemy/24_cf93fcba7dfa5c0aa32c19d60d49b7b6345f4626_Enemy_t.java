 package object;
 
 import engine.open2d.draw.Plane;
 import engine.open2d.renderer.WorldRenderer;
 import engine.open2d.texture.AnimatedTexture.Playback;
 import game.GameLogic;
 import game.GameTools;
 import game.GestureListener;
 import game.GameTools.Gesture;
 import game.open2d.R;
 
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import android.graphics.RectF;
 import android.util.Log;
 import android.view.MotionEvent;
 import object.GameObject.Direction;
 import object.GameObject.GameObjectState;
 import object.Player.PlayerState;
 import structure.ActionData;
 import structure.ActionDataTool;
 import structure.HitBox;
 import structure.HurtBox;
 import structure.InteractionProperties;
 
 public class Enemy extends GameObject {
 	public static enum EnemyState implements GameObjectState{
 		TEMP("temp"),
 		STAND("stand"),
 		STRIKE1("strike"),
 		STRUCK1("struck1"),
 		KNOCK_BACK("knock_back"),
 		KNOCK_DOWN("knock_down"),
 		KNOCK_DOWN_FORWARD("knock_down_forward"),
 		KNOCK_UP("knock_up"),
 		WALL_BOUNCE("wall_bounce"),
 		RUN("run"),
 		WALK("walk"),
 		JUMP_BACK("jump_back"),
 		CROSS_ROLL("cross_roll"),
 		DODGE("dodge"),
 		FREEZE("freeze"),
 		DEAD("dead");
 		
 		static String OBJECT = "enemy";
 		String name;
 		
 		public static EnemyState getStateFromTotalName(String name){
 			for(EnemyState enemyState : EnemyState.values()){
 				if(name.equals(enemyState.getTotalName())){
 					return enemyState;
 				}
 			}
 			return null;
 		}
 		
 		EnemyState(String n){
 			name = n;
 		}
 		
 		public String getTotalName(){
 			return OBJECT+"_"+name;
 		}
 		
 		public String getName(){
 			return name;
 		}
 	}
 	
 	private static String OBJNAME = "enemy";
 	
 	private static EnemyState INIT_STATE = EnemyState.STAND;
 	
 	private static final int TEMP_FRAME = 0;
 	
 	private static float RUN_SPEED = 0.16f;
 	private static float WALK_SPEED = 0.08f;
 	private static float JUMP_BACK_SPEED = 0.15f;
 	private static float CROSS_ROLL_SPEED = 0.17f;
 	private static float FAR_DIST_TO_PLAYER = 2.5f;
 	private static float CLOSE_DIST_TO_PLAYER = 1.5f;
 	private static float COLLISION_BUFFER = 1.0f;
 	private static float KNOCK_BACK_SPEED = 1.0f;
 	private static float KNOCK_BACK_DECEL = -0.1f;
 	private static float JUMP_SPEED = 0.45f;
 	public static float COLLISION_FRAME = 20.0f;
 	
 	Player playerRef;
 	EnemyState enemyState;
 	public int struck;
 	public int unfreezeTimeCount;
 	
 	//public Enemy(LinkedHashMap<String,GameObject> gameObjects, Player player, int index, float x, float y, float width, float height){
 	public Enemy(LinkedHashMap<String,GameObject> gameObjects, List<ActionData> actionData, Player player, int index, float x, float y){
 		super(gameObjects,actionData,INIT_STATE,x,y);
 		
 		enemyState = INIT_STATE;
 		this.name = OBJNAME+index;
 		this.z = -0.9f+index*0.01f;
 		this.playerRef = player;
 		
 		struck = 3;
 		unfreezeTimeCount = 0;
 		
 		//display = animations.get(EnemyState.STAND);
 		//this.currentAction = this.actionData.get(INIT_STATE);
 		
 		currentAction.drawEnable();
 		this.direction = Direction.LEFT;
 		
 	}
 
 	@Override
 	public void setupAnimRef() {
 		animationRef = new HashMap<GameObjectState, Integer>();
 		animationRef.put(EnemyState.TEMP, R.drawable.enemy_knock_back);
 		animationRef.put(EnemyState.STAND, R.drawable.enemy_stance);
 		animationRef.put(EnemyState.FREEZE, R.drawable.enemy_stance);
 		animationRef.put(EnemyState.RUN, R.drawable.enemy_run);
 		animationRef.put(EnemyState.WALK, R.drawable.enemy_run);
 		animationRef.put(EnemyState.JUMP_BACK, R.drawable.enemy_jump_back);
 		animationRef.put(EnemyState.CROSS_ROLL, R.drawable.enemy_cross_roll);
 		animationRef.put(EnemyState.DEAD, R.drawable.enemy_stance);
 		animationRef.put(EnemyState.STRIKE1, R.drawable.enemy_strike1);
 		animationRef.put(EnemyState.STRUCK1, R.drawable.enemy_struck1);
 		
 		animationRef.put(EnemyState.KNOCK_BACK, R.drawable.enemy_knock_back);
 		animationRef.put(EnemyState.KNOCK_DOWN, R.drawable.enemy_knock_down);
 		animationRef.put(EnemyState.KNOCK_DOWN_FORWARD, R.drawable.enemy_knock_down_forward);
 		animationRef.put(EnemyState.KNOCK_UP, R.drawable.enemy_knock_up);
 		animationRef.put(EnemyState.WALL_BOUNCE, R.drawable.enemy_wall_bounce);
 		
 	}
 
 	@Override
 	public void mapActionData(List<ActionData> actionData) {
 		for(ActionData data : actionData){
 			EnemyState state = EnemyState.getStateFromTotalName(data.getName());
 			if(state != null){
 				int refID = animationRef.get(state);
 				data.createAnimation(refID);
 				this.actionData.put(state, data);
 			}
 		}
 	}
 
 	@Override
 	public void updateState() {
 		if(hitStopFrames > 0){
 			return;
 		}
 		
 		float checkX = getMidX();
 		
 		//if(!isStrikeState() && !isDodging()){
 		if(		enemyState != EnemyState.KNOCK_BACK &&
 				enemyState != EnemyState.KNOCK_DOWN &&
 				enemyState != EnemyState.KNOCK_DOWN_FORWARD &&
 				enemyState != EnemyState.WALL_BOUNCE &&
 				enemyState != EnemyState.KNOCK_UP){
 			if(playerRef.getMidX() > checkX){
 				direction = Direction.RIGHT;
 			} else if(playerRef.getMidX() < checkX){
 				direction = Direction.LEFT;
 			}
 		}
 
 		if(enemyState == EnemyState.STAND){
 			
 			if(Math.abs(checkX - playerRef.getMidX()) > CLOSE_DIST_TO_PLAYER){
 				enemyState = EnemyState.WALK;
 			}
 			
 			if(Math.abs(checkX - playerRef.getMidX()) > FAR_DIST_TO_PLAYER){
 				enemyState = EnemyState.RUN;
 			}
 			
 			/*
 			if(GameTools.boxColDetect(this, playerRef, COLLISION_BUFFER) && Math.random() > 0.90){
 				enemyState = EnemyState.STRIKE1;
 			}
 			*/
 		} else if(enemyState == EnemyState.RUN || enemyState == EnemyState.WALK){
 			executeMovement();
 		}
 
 		if(isHit()){
 			interProperties = playerRef.getCurrentAction().getInterProperties();
 			playerRef.setHitStopFrames(interProperties.getHitStop());
 			this.setHitStopFrames(interProperties.getHitStop());
 			
 			if(playerRef.getDirection() == Direction.RIGHT){
 				direction = Direction.LEFT;
 			} else if(playerRef.getDirection() == Direction.LEFT){
 				direction = Direction.RIGHT;
 			}
 
 			enemyState = EnemyState.getStateFromTotalName(EnemyState.OBJECT+"_"+interProperties.getTriggerState(ActionDataTool.HIT_TRIGGER));
 			initSpeed = true;
 		}
 		
 		if(currentLogic.hasTrigger(ActionDataTool.WALL_TRIGGER)){
 			if(isAtWall()){
 				//String state = currentAction.getActionChangeState(ActionDataTool.WALL_TRIGGER); 
 				String state = currentLogic.getTrigger(ActionDataTool.WALL_TRIGGER);
 				enemyState = EnemyState.getStateFromTotalName(EnemyState.OBJECT+"_"+state);
 				interProperties = null;
 				initSpeed = true;
 			}
 		}
 		
 		if(currentLogic.hasTrigger(ActionDataTool.GROUND_TRIGGER)){
 			if(isOnGround()){
 				String state = currentLogic.getTrigger(ActionDataTool.GROUND_TRIGGER);
 				enemyState = EnemyState.getStateFromTotalName(EnemyState.OBJECT+"_"+state);
 				interProperties = null;
 				initSpeed = true;
 			}
 		}
 		
 		if(currentLogic.hasTrigger(ActionDataTool.PLAYED_TRIGGER)){
 			if(currentAction.getAnimation().isPlayed()){
 				String state = currentLogic.getTrigger(ActionDataTool.PLAYED_TRIGGER);
 				enemyState = EnemyState.getStateFromTotalName(EnemyState.OBJECT+"_"+state);
 				interProperties = null;
 				initSpeed = true;
 			}
 		}
 
 		if(currentLogic.hasTrigger(ActionDataTool.STOPPED_TRIGGER)){
 			if(isStopped()){
 				String state = currentLogic.getTrigger(ActionDataTool.STOPPED_TRIGGER);
 				enemyState = EnemyState.getStateFromTotalName(EnemyState.OBJECT+"_"+state);
 				interProperties = null;
 				initSpeed = true;
 			}
 		}
 		
 		if(currentAction != actionData.get(enemyState)){
 			switchAction(enemyState);
 		}
 		
 		//Log.d("enemyState",enemyState.toString());
 	}
 
 	@Override
 	public void updateLogic() {
 		if(hitStopFrames > 0){
 			hitStopFrames--;
 			return;
 		}
 
 		if(enemyState== EnemyState.TEMP){
 			currentAction.getAnimation().setFrame(TEMP_FRAME);
 			direction = Direction.RIGHT;
 		}
 		
 		if(enemyState == EnemyState.STAND){
 
 		}else if(enemyState == EnemyState.STRUCK1){
 			if(currentAction.getAnimation().getFrame() < Player.CANCEL_STRIKE_FRAMES){
 				selected = false;
 			}
 		}else if(isStrikeState()){
 			
 		}else if(enemyState == EnemyState.RUN){
 			if(direction == Direction.RIGHT)
 				x += RUN_SPEED;
 			else if(direction == Direction.LEFT)
 				x -= RUN_SPEED;
 		}else if(enemyState == EnemyState.WALK){
 			if(direction == Direction.RIGHT)
 				x += WALK_SPEED;
 			else if(direction == Direction.LEFT)
 				x -= WALK_SPEED;
 		}else if(enemyState == EnemyState.JUMP_BACK){
 			if(direction == Direction.RIGHT)
 				x -= JUMP_BACK_SPEED;
 			else if(direction == Direction.LEFT)
 				x += JUMP_BACK_SPEED;
 		}else if(enemyState == EnemyState.CROSS_ROLL){
 			if(direction == Direction.RIGHT)
 				x += CROSS_ROLL_SPEED;
 			else if(direction == Direction.LEFT)
 				x -= CROSS_ROLL_SPEED;
 		}else if(enemyState == EnemyState.FREEZE){
 			unfreezeTimeCount--;
 		}else {
 			executeLogic();
 		}
 	}
 
 	@Override
 	public void updateDisplay() {
 		if(hitStopFrames > 0){
 			currentAction.getAnimation().setPlayback(Playback.PAUSE);
 			return;
 		} else {
 			Playback defaultPlayback = currentAction.getPlaneData().getPlayback();
 			currentAction.getAnimation().setPlayback(defaultPlayback);
 		}
 		
 		if(direction==Direction.RIGHT){
 			currentAction.flipHorizontal(false);
 		} else if(direction==Direction.LEFT){
 			currentAction.flipHorizontal(true);
 		}
 		
 		if(enemyState == EnemyState.DEAD){
 			currentAction.drawDisable();
 		}
 		
 		if(enemyState == EnemyState.FREEZE){
 			currentAction.drawDisable();
 		}
 	}
 
 	@Override
 	public void updateAfterDisplay() {
 		Plane display = currentAction.getAnimation();
 		if(!display.isPlayed())
 			return;
 		
 		if(isStrikeState()){
 			display.resetAnimation();
 			enemyState = EnemyState.STAND;
 		} else if(enemyState == EnemyState.JUMP_BACK){
 			display.resetAnimation();
 			enemyState = EnemyState.STAND;
 		} else if(enemyState == EnemyState.CROSS_ROLL){
 			display.resetAnimation();
 			enemyState = EnemyState.STAND;
 		} else if(enemyState == EnemyState.STRUCK1){
 			display.resetAnimation();
 			struck -= 1;
 			enemyState = EnemyState.STAND;
 		} else if(enemyState == EnemyState.KNOCK_DOWN || enemyState == EnemyState.KNOCK_DOWN_FORWARD){
 			//display.resetAnimation();
 			//struck -= 1;
 			//enemyState = EnemyState.STAND;
 		} else if(enemyState == EnemyState.FREEZE){
 			if(unfreezeTimeCount <= 0){
 				display.resetAnimation();
 				enemyState = EnemyState.STAND;
 				unfreezeTimeCount = 0;
 			}
 			
 		}
 	}
 	
 	@Override
 	public void passTouchEvent(MotionEvent e, WorldRenderer worldRenderer) {
 		Plane display = currentAction.getAnimation();
 		Plane selectedPlane = worldRenderer.getSelectedPlane(e.getX(), e.getY());
 
 		float[] points = worldRenderer.getUnprojectedPoints(e.getX(), e.getY(), display);
 //		if(enemyState != EnemyState.FREEZE)
 //			selected = checkEnemySelection(points[0],points[1]);
 		
 //		if(animations.containsValue(selectedPlane)){
 //			selected = true;
 //		}
 		
 //		if(gesture != Gesture.NONE){
 //			selected = false;
 //		}
 	}
 
 	public void passDoubleTouchEvent(GestureListener g,  WorldRenderer worldRenderer){
 
 	}
 
 	public void executeMovement(){
 		float checkX = getMidX();
 		
 		if(Math.abs(checkX - playerRef.getMidX()) > CLOSE_DIST_TO_PLAYER){
 			enemyState = EnemyState.WALK;
 		}
 		
 		if(Math.abs(checkX - playerRef.getMidX()) > FAR_DIST_TO_PLAYER){
 			enemyState = EnemyState.RUN;
 		}
 		
 		if(GameTools.boxColDetect(this, playerRef, COLLISION_BUFFER)){
 			enemyState = EnemyState.STAND;
 		}
 	}
 	
 	public void executeLogic(){
 		if(currentAction.getAnimation().getFrame() < currentLogic.getActiveAfter()){
 			return;
 		}
 		
 		if(initSpeed){
 			float initxSpeed = currentLogic.getxInitSpeed();
 			float xAccel = currentLogic.getxAccel();
 			
 			if(currentAction.getActionProperties().hasModifier(ActionDataTool.REVERSE_X)){
 				initxSpeed = -1*initxSpeed;
 			}
 			
 			if(!currentLogic.isContinueSpeed()){
 				if(direction == Direction.RIGHT){
 					initxSpeed = initxSpeed;
 				} else if(direction == Direction.LEFT){
 					initxSpeed = -1*initxSpeed;
 				}
 			}
 			
 			xAccel = getAccelFromSpeed(initxSpeed, xAccel);
 
 			initXPhys(initxSpeed, xAccel);
 			initYPhys(currentLogic.getyInitSpeed(), currentLogic.getyAccel());
 			initSpeed = false;
 			
 			//Log.d(enemyState.toString()+" init", xVelocity+" "+xAccel+" wall "+isAtWall());
 			return;
 		}
 
		//Log.d(enemyState.toString(), xVelocity+" "+xAccel);
 		if(isStopped() || isAtWall()){
 			initXPhys(0, 0);
 		} else {
 			executeXPhys();
 		}
 		
 		if(isOnGround()){
 			y = GameLogic.FLOOR;
 			initYPhys(0, 0);
 		}else{
 			executeYPhys();
 		}
 	}
 	/*
 	public void executeKnockBack(){
 		if(initSpeed){
 			if(direction == Direction.LEFT){
 				//initXPhys(-currentAction.getxInitSpeed(), -currentAction.getxAccel());
 				initXPhys(playerRef.getCurrentAction().getxInitSpeed(), -currentAction.getxAccel());
 			} else if(direction == Direction.RIGHT){
 				//initXPhys(currentAction.getxInitSpeed(), currentAction.getxAccel());
 				initXPhys(-playerRef.getCurrentAction().getxInitSpeed(), currentAction.getxAccel());
 			}
 			initSpeed = false;
 			initYPhys(playerRef.getCurrentAction().getyInitSpeed(), GameLogic.GRAVITY);
 			return;
 		}
 
 		if(isStopped()){
 			initXPhys(0, 0);
 		} else {
 			executeXPhys();
 		}
 		
 		executeYPhys();
 	}
 
 	public void executeKnockUp(){
 		if(initSpeed){
 			if(direction == Direction.LEFT){
 				//initXPhys(-currentAction.getxInitSpeed(), -currentAction.getxAccel());
 				initXPhys(playerRef.getCurrentAction().getxInitSpeed(), -currentAction.getxAccel());
 			} else if(direction == Direction.RIGHT){
 				//initXPhys(currentAction.getxInitSpeed(), currentAction.getxAccel());
 				initXPhys(-playerRef.getCurrentAction().getxInitSpeed(), currentAction.getxAccel());
 			}
 			initSpeed = false;
 			initYPhys(playerRef.getCurrentAction().getyInitSpeed(), GameLogic.GRAVITY);
 			return;
 		}
 
 		if(isAtWall()){
 			initXPhys(0, 0);
 		}
 		
 		if(isStopped()){
 			initXPhys(0, 0);
 		} else {
 			executeXPhys();
 		}
 		
 		executeYPhys();
 	}
 	
 	public void executeWallBounce(){
 		//if(!currentAction.getAnimation().isPlayed())
 		if(currentAction.getAnimation().getFrame() < 9)
 			return;
 		
 		if(initSpeed){
 			if(direction == Direction.LEFT){
 				//initXPhys(-currentAction.getxInitSpeed(), -currentAction.getxAccel());
 				initXPhys(-xVelocity, -currentAction.getxAccel());
 			} else if(direction == Direction.RIGHT){
 				//initXPhys(currentAction.getxInitSpeed(), currentAction.getxAccel());
 				initXPhys(-xVelocity, currentAction.getxAccel());
 			}
 			initYPhys(yVelocity, GameLogic.GRAVITY);
 			initSpeed = false;
 			return;
 		}
 		
 		if(isStopped()){
 			initXPhys(0, 0);
 		} else {
 			executeXPhys();
 		}
 		executeYPhys();
 	}
 	
 	public void executeKnockDown(){
 		if(initSpeed){
 			initSpeed = false;
 			if(direction == Direction.LEFT){
 				initXPhys(xVelocity, -currentAction.getxAccel());
 			} else if(direction == Direction.RIGHT){
 				initXPhys(xVelocity, currentAction.getxAccel());
 			}
 			
 			y = GameLogic.FLOOR;
 			yVelocity = 0;
 			yAccel = 0;
 			return;
 		}
 
 		if(isAtWall()){
 			initXPhys(0, 0);
 		}
 
 		if(isStopped()){
 			initXPhys(0, 0);
 		} else {
 			executeXPhys();
 		}
 	}
 	*/
 	public boolean isHit(){
 		if(playerRef.getHitActive() && playerRef.getHitStopFrames() == 0){
 			ActionData playerAction = playerRef.getCurrentAction();
 			for(HitBox hitBox : playerAction.getHitBoxes()){
 				for(HurtBox hurtBox : currentAction.getHurtBoxes()){
 					if(GameTools.boxColDetect(hurtBox.getBoxData(), this, hitBox.getBoxData(), playerRef)){
 						return true;
 					}
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	public void otherAIMoveInteration(){
 		if(isDodging())
 			return;
 		
 		int counter = 0;
 		for(GameObject gameObject : gameObjects.values()){
 			counter++;
 			if(gameObject instanceof Enemy){
 				Enemy enemy = (Enemy)gameObject;
 				if(enemy != this && GameTools.boxColDetect(this, enemy, COLLISION_BUFFER)){
 					if(	enemy.getEnemyState() != EnemyState.WALK &&
 						enemy.getEnemyState() != EnemyState.RUN &&
 						enemy.getEnemyState() != EnemyState.STRUCK1 &&
 						!enemy.isDodging() &&
 						this.getEnemyState() != EnemyState.STRUCK1){
 						if(counter % 2 == 1){
 							enemyState = EnemyState.JUMP_BACK;
 						} else {
 							enemyState = EnemyState.CROSS_ROLL;
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	public boolean isStrikingPlayer(){
 		return ((GameTools.boxColDetect(	playerRef, -COLLISION_BUFFER, COLLISION_BUFFER, COLLISION_BUFFER, -COLLISION_BUFFER,
 											this, -1.0f, 1.0f, 1.5f,0.1f) && direction == Direction.RIGHT) ||
 				(GameTools.boxColDetect(	playerRef, -COLLISION_BUFFER, COLLISION_BUFFER, COLLISION_BUFFER, -COLLISION_BUFFER,
 											this, -1.0f, 1.0f, -0.1f,-1.5f) && direction == Direction.RIGHT));
 	}
 	
 	private boolean checkEnemySelection(float xPoint,float yPoint){
 //		float top = y + height - COLLISION_BUFFER;
 //		float bottom = y + COLLISION_BUFFER;
 		float top = y + height;
 		float bottom = y;
 		float left = x + COLLISION_BUFFER;
 		float right = x + width - COLLISION_BUFFER;
 
 		return(xPoint > left && xPoint < right && yPoint > bottom && yPoint < top);	
 	}
 	
 	public boolean isStrikeState(){
 		if(enemyState == EnemyState.STRIKE1){
 			return true;
 		} 
 		
 		return false;
 		
 	}
 	
 	public boolean isDodging(){
 		if(	enemyState == EnemyState.JUMP_BACK ||
 			enemyState == EnemyState.CROSS_ROLL
 		){
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public EnemyState getEnemyState() {
 		return enemyState;
 	}
 
 	public void setEnemyState(EnemyState enemyState) {
 		this.enemyState = enemyState;
 	}
 	
 	public int getStruck(){
 		return struck;
 	}
 
 	public boolean isSelected() {
 		return selected;
 	}
 }
