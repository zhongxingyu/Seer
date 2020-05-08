 package object;
 
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import object.Enemy.EnemyState;
 import object.GameObject.Direction;
 import object.GameObject.GameObjectState;
 import structure.ActionData;
 import structure.ActionDataTool;
 import structure.HitBox;
 import android.util.Log;
 import android.view.MotionEvent;
 import engine.open2d.draw.Plane;
 import engine.open2d.renderer.WorldRenderer;
 import engine.open2d.texture.AnimatedTexture.Playback;
 import game.GameLogic;
 import game.GameTools;
 import game.GestureListener;
 import game.GameTools.Gesture;
 import game.open2d.R;
 
 public class Player extends GameObject{
 	public static enum PlayerState implements GameObjectState{
 		TEMP("temp"),
 		STAND("stand"),
 		RUN("run"),
 		JUMP("jump_startup"),
 		LAND("jump_land"),
 		DODGE("dodge"),
 		DEAD("dead"),
 		NTAP("n_tap"),
 		NFSWIPE("n_fswipe"),
 		NUSWIPE("n_uswipe");
 		
 		static String OBJECT = "jack";
 		String name;
 		
 		public static PlayerState getStateFromTotalName(String name){
 			for(PlayerState playerState : PlayerState.values()){
 				if(name.equals(playerState.getTotalName())){
 					return playerState;
 				}
 			}
 			return null;
 		}
 		
 		PlayerState(String n){
 			name = n;
 		}
 		
 		public String getTotalName(){
 			return OBJECT+"_"+name;
 		}
 		
 		public String getName(){
 			return name;
 		}
 	}
 
 	public static String OBJNAME = "player";
 	private static PlayerState INIT_STATE = PlayerState.STAND;
 	
 	private static final int TEMP_FRAME = 10;
 
 	private static float WALK_SPEED = 0.2f;
 	private static float STRIKE_SPEED = 0.1f;
 	private static float NFSWIPE_SPEED = 0.2f;
 	private static float DODGE_SPEED = 0.23f;
 	private static float BUFFER = 0.4f;
 	private static float COLLISION_BUFFER = 1.0f;
 	public static float CANCEL_STRIKE_FRAMES = 8;
 	private static float JUMP_SPEED = 0.75f;
 
 	private Enemy struckEnemy;
 	private PlayerState playerState;
 	private float moveToX;
 	private float moveToY;
 	
 	private int punchIndex;
 	private int finishIndex;
 	private int counterIndex;
 	
 	public Player(LinkedHashMap<String,GameObject> gameObjects, List<ActionData> actionData, float x, float y){
 		super(gameObjects,actionData,INIT_STATE,x,y);
 		
 		playerState = INIT_STATE;
 		this.name = OBJNAME;
 		
 		this.moveToX = x;
 		this.moveToY = y;
 		this.z = -1.0f;
 		
 		this.punchIndex = 1;
 		this.finishIndex = 1;
 		this.counterIndex = 1;
 		
 		//this.currentAction = this.actionData.get(INIT_STATE);
 		this.currentAction.drawEnable();
 		this.direction = Direction.LEFT;
 	}
 	
 	@Override
 	public void setupAnimRef() {
 		animationRef = new HashMap<GameObjectState, Integer>();
 		//animationRef.put(PlayerState.STAND, new Plane(R.drawable.rising_stance, name+"_"+PlayerState.STAND.getName(), width, height, 4, 7));
 		animationRef.put(PlayerState.TEMP, R.drawable.jack_n_uswipe);
 		animationRef.put(PlayerState.STAND, R.drawable.jack_stand);
 		animationRef.put(PlayerState.DEAD, R.drawable.rising_stance);
 		animationRef.put(PlayerState.RUN, R.drawable.jack_run);
 		animationRef.put(PlayerState.JUMP, R.drawable.jack_jump_startup);
 		animationRef.put(PlayerState.LAND, R.drawable.jack_jump_land);
 		animationRef.put(PlayerState.DODGE, R.drawable.rising_dodge);
 		animationRef.put(PlayerState.NTAP, R.drawable.jack_n_tap);
 		animationRef.put(PlayerState.NFSWIPE, R.drawable.jack_n_fswipe);
 		animationRef.put(PlayerState.NUSWIPE, R.drawable.jack_n_uswipe);
 		
 	}
 	
 	@Override
 	public void mapActionData(List<ActionData> actionData) {
 		for(ActionData data : actionData){
 			PlayerState state = PlayerState.getStateFromTotalName(data.getName());
 			if(state != null){
 				int refID = animationRef.get(state);
 				data.createAnimation(refID);
 				this.actionData.put(state, data);
 			}
 		}
 		
 	}
 	
 	public PlayerState getPlayerState() {
 		return playerState;
 	}
 
 	public void setPlayerState(PlayerState playerState) {
 		this.playerState = playerState;
 	}
 
 	@Override
 	public void passTouchEvent(MotionEvent e, WorldRenderer worldRenderer){
 		Plane display = currentAction.getAnimation();
 		float[] unprojectedPoints = worldRenderer.getUnprojectedPoints(e.getX(), e.getY(), display);
 		
 		if(playerState == PlayerState.RUN || playerState == PlayerState.STAND){
 			moveToX = unprojectedPoints[0];
 			moveToY = unprojectedPoints[1];
 		}
 		
 		display.unprojectDisable();
 	}
 
 	public void passDoubleTouchEvent(GestureListener g,  WorldRenderer worldRenderer){
 		Plane display = currentAction.getAnimation();
 		float[] unprojectedPoints = worldRenderer.getUnprojectedPoints(g.getDoubleTapX(), g.getDoubleTapY(), display);
 
 		if(unprojectedPoints[1] > (this.getY()+this.getHeight())){
 			inputList.add(Gesture.DTAP_UP);
 		} else if(unprojectedPoints[0] > this.getX()+this.getHeight()){
 			inputList.add(Gesture.DTAP_RIGHT);
 		} else if(unprojectedPoints[0] < this.getX()){
 			inputList.add(Gesture.DTAP_LEFT);
 		}
 		
 		/*
 		if(playerState == PlayerState.RUN || playerState == PlayerState.STAND){
 			if(unprojectedPoints[1] > (this.getY()+this.getHeight())){
 				this.playerState = PlayerState.JUMP;
 				initSpeed = true;
 				if(unprojectedPoints[0] > getMidX()){
 					direction = Direction.RIGHT;
 				} else if(unprojectedPoints[0] < getMidX()){
 					direction = Direction.LEFT;
 				}
 			}
 		}
 		*/
 
 		display.unprojectDisable();
 	}
 
 	@Override
 	public void updateState() {
 		if(hitStopFrames > 0){
 			return;
 		}
 		
 		Gesture gesture = Gesture.NONE;
 		PlayerState inputState = executeInput();
 		if(inputState != null){
 			playerState = inputState;
 		}
 		
 		if(playerState == PlayerState.RUN || playerState == PlayerState.STAND){
 			executeMovement();
 		}
 		
 		if(playerState == PlayerState.JUMP){
 			if(!initSpeed && this.getY() <= GameLogic.FLOOR && yVelocity <= 0){
 				playerState = PlayerState.LAND;
 			}
 		}
 
 		if(currentLogic.hasTrigger(ActionDataTool.PLAYED_TRIGGER)){
 			if(currentAction.getAnimation().isPlayed()){
 				String state = currentLogic.getTrigger(ActionDataTool.PLAYED_TRIGGER);
 				playerState = PlayerState.getStateFromTotalName(PlayerState.OBJECT+"_"+state);
 				interProperties = null;
 				initSpeed = true;
 			}
 		}
 		
		if(currentAction != actionData.get(playerState))
			this.switchAction(playerState);

 		/*
 		for(GameObject gameObject : gameObjects.values()){
 			if(gameObject instanceof Enemy){
 				executeEnemyInteraction((Enemy)gameObject);
 			}
 		}
 		
 		if(!isFinishState() && !isCounterState()){
 			if(GameTools.gestureBreakdownHorizontal(gesture) == Gesture.LEFT){
 				playerState = PlayerState.DODGE;
 				direction = Direction.RIGHT;
 			} else if(GameTools.gestureBreakdownHorizontal(gesture) == Gesture.RIGHT){
 				playerState = PlayerState.DODGE;
 				direction = Direction.LEFT;
 			}
 		}
 		*/
 	}
 	
 	@Override
 	public void updateLogic() {
 		if(currentAction.isHitBoxActive()){
 			this.setHitActive(true);
 		} else {
 			this.setHitActive(false);
 		}
 		//should hitstop return from function?
 		if(hitStopFrames > 0){
 			hitStopFrames--;
 			return;
 		}
 		
 		if(playerState == PlayerState.TEMP){
 			currentAction.getAnimation().setFrame(TEMP_FRAME);
 			direction = Direction.RIGHT;
 			//currentAction.getAnimation().setPlayback(Playback.PAUSE);
 		}
 
 		if(playerState == PlayerState.RUN){
 			if(direction == Direction.RIGHT)
 				x += WALK_SPEED;
 			else if(direction == Direction.LEFT)
 				x -= WALK_SPEED;
 		}
 		
 		if(playerState == PlayerState.JUMP){
 			if(initSpeed){
 				initSpeed = false;
 				initYPhys(JUMP_SPEED, GameLogic.GRAVITY);
 			} else{
 				executeYPhys();
 			}
 
 			if(direction == Direction.RIGHT)
 				x += WALK_SPEED;
 			else if(direction == Direction.LEFT)
 				x -= WALK_SPEED;
 		}
 
 		if(playerState == PlayerState.LAND){
 			y = GameLogic.FLOOR;
 			initYPhys(0,0);
 			moveToX = getMidX();
 			moveToY = getMidY();
 		}
 
 		if(playerState == PlayerState.NFSWIPE){
 			moveToX = getMidX();
 			if(currentAction.getAnimation().getFrame() <= 7 && currentAction.getAnimation().getFrame() >= 5){
 				if(direction == Direction.RIGHT)
 					x += NFSWIPE_SPEED;
 				else if(direction == Direction.LEFT)
 					x -= NFSWIPE_SPEED;
 			}
 		}
 
 		if(playerState == PlayerState.NUSWIPE){
 			moveToX = getMidX();
 		}
 		
 		/*
 		if(isStrikeState()){
 			if(struckEnemy.getX() < x) {
 				direction = Direction.LEFT;
 				//x = struckEnemy.getX()-playerState.getOffSnapX();
 				moveToX = getMidX();
 			} else if(struckEnemy.getX() > x){
 				direction = Direction.RIGHT;
 				//x = struckEnemy.getX()+playerState.getOffSnapX();
 				moveToX = getMidX();
 			}
 		}
 
 		if(isCounterState()){
 			gesture = Gesture.NONE;
 		}
 		*/
 		
 		if(playerState == PlayerState.DODGE){
 			if(direction == Direction.RIGHT)
 				x -= DODGE_SPEED;
 			else if(direction == Direction.LEFT)
 				x += DODGE_SPEED;
 			moveToX = getMidX();
 			moveToY = getMidY();
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
 	}
 	
 	@Override
 	public void updateAfterDisplay() {
 		Plane display = currentAction.getAnimation();
 
 		if(display.isPlayed()){
 			if(playerState==PlayerState.LAND){
 				display.resetAnimation();
 				playerState = PlayerState.STAND;
 			}
 			
 			if(	playerState==PlayerState.DODGE){
 				//gesture = Gesture.NONE;
 				display.resetAnimation();
 				playerState = PlayerState.STAND;				
 			}
 			
 			if(playerState==PlayerState.DEAD){
 				display.resetAnimation();
 				playerState = PlayerState.STAND;
 			}
 		}
 	}
 	
 	private PlayerState executeInput(){
 		Gesture gesture = Gesture.NONE;
 		PlayerState state = null;
 		
 		if(!inputList.isEmpty()){
 			gesture = inputList.getFirst();
 			inputList.removeFirst();
 		}
 		
 		if(gesture == Gesture.DTAP_UP){
 			if(currentAction.getActionProperties().hasCancel(ActionDataTool.DTAP_U_TRIGGER)){
 				String cancel = currentAction.getActionProperties().getCancel(ActionDataTool.DTAP_U_TRIGGER);
 				state = PlayerState.getStateFromTotalName(PlayerState.OBJECT+"_"+cancel);
 				initSpeed = true;
 			}
 		}
 		
 		if(Math.abs(gesture.getXDiffSize()) > Math.abs(gesture.getYDiffSize())){
 			
 			if(GameTools.gestureBreakdownHorizontal(gesture) == Gesture.SWIPE_LEFT){
 				if(currentAction.getActionProperties().hasCancel(ActionDataTool.SWIPE_F_TRIGGER)){
 					String cancel = currentAction.getActionProperties().getCancel(ActionDataTool.SWIPE_F_TRIGGER);
 					state = PlayerState.getStateFromTotalName(PlayerState.OBJECT+"_"+cancel);
 					this.direction = Direction.LEFT;
 				}
 			} else if(GameTools.gestureBreakdownHorizontal(gesture) == Gesture.SWIPE_RIGHT) {
 				if(currentAction.getActionProperties().hasCancel(ActionDataTool.SWIPE_F_TRIGGER)){
 					String cancel = currentAction.getActionProperties().getCancel(ActionDataTool.SWIPE_F_TRIGGER);
 					state = PlayerState.getStateFromTotalName(PlayerState.OBJECT+"_"+cancel);
 					this.direction = Direction.RIGHT;
 				}
 			}
 		} else {
 			if(GameTools.gestureBreakdownVertical(gesture) == Gesture.SWIPE_UP) {
 				if(currentAction.getActionProperties().hasCancel(ActionDataTool.SWIPE_U_TRIGGER)){
 					String cancel = currentAction.getActionProperties().getCancel(ActionDataTool.SWIPE_U_TRIGGER);
 					state = PlayerState.getStateFromTotalName(PlayerState.OBJECT+"_"+cancel);
 				}				
 			}
 			
 			if(gesture.getXDiffSize() > 0){
 				this.direction = Direction.LEFT;
 			} else if(gesture.getXDiffSize() < 0) {
 				this.direction = Direction.RIGHT;
 			}
 		}
 		
 		return state;
 	}
 
 	private void executeMovement(){
 		float checkX = getMidX();
 		if(moveToX > checkX){
 			direction = Direction.RIGHT;
 			playerState = PlayerState.RUN;
 		} else if(moveToX < checkX) {
 			direction = Direction.LEFT;
 			playerState = PlayerState.RUN;
 		}
 
 		if(moveToX > checkX - Player.BUFFER && moveToX < checkX + Player.BUFFER) {
 			playerState = PlayerState.STAND;
 		}
 	}
 
 	/*
 	private void executeEnemyInteraction(Enemy enemy){
 		Plane display = currentAction.getAnimation();
 		if(enemy.isStrikeState()){
 			if(enemy.isStrikingPlayer() &&
 				enemy.getDisplay().getFrame() == Enemy.COLLISION_FRAME){
 				if(	playerState != PlayerState.DODGE &&
 					!isFinishState() && 
 //					!isStrikeState() &&
 					!isCounterState()){
 						playerState = PlayerState.DEAD;
 				}
 			}
 		}
 		
 		if(		playerState == PlayerState.STAND || playerState == PlayerState.RUN || playerState == PlayerState.DODGE ||
 				(isStrikeState() &&
 				display.getFrame() >= display.getTotalFrame()-CANCEL_STRIKE_FRAMES)){
 			
 			if(	GameTools.boxColDetect(this, enemy, COLLISION_BUFFER) && enemy.isSelected()  && !enemy.isDodging()){
 				if(enemy.isStrikeState() && playerState == PlayerState.DODGE){
 					playerState = PlayerState.getCounter(counterIndex);
 				} else {
 					playerState = PlayerState.getStrike(punchIndex);
 					struckEnemy = enemy;
 					
 					if(enemy.getStruck() <= 0){
 						playerState = PlayerState.getFinish(finishIndex);
 					}
 
 					updatePunchIndex();
 				}
 			}
 		}
 	}
 	
 	/*
 	private void updatePunchIndex() {
 		punchIndex++;
 		if(punchIndex > PlayerState.STRIKE_NUMBERS){
 			punchIndex = 1;
 		}
 	}
 	
 	private void updateFinishIndex() {
 		finishIndex++;
 		if(finishIndex > PlayerState.FINISH_NUMBERS){
 			finishIndex = 1;
 		}
 	}
 	
 	private void updateCounterIndex() {
 		counterIndex++;
 		if(counterIndex > PlayerState.COUNTER_NUMBERS){
 			counterIndex = 1;
 		}
 	}
 	
 	public boolean isStrikeState(){
 		return (playerState==PlayerState.STRIKE1||
 				playerState==PlayerState.STRIKE2||
 				playerState==PlayerState.STRIKE3);
 	}
 	
 	public boolean isFinishState(){
 		return (playerState==PlayerState.FINISH1|| 
 				playerState==PlayerState.FINISH2||
 				playerState==PlayerState.FINISH3||
 				playerState==PlayerState.FINISH4||
 				playerState==PlayerState.FINISH5);
 	}
 	
 	public boolean isCounterState(){
 		return (playerState==PlayerState.COUNTER1);
 	}
 	*/
 }
