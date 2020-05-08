 package object;
 
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import object.Enemy.EnemyState;
 import object.GameObject.Direction;
 import object.GameObject.GameObjectState;
 import structure.ActionData;
 import structure.HitBox;
 import android.util.Log;
 import android.view.MotionEvent;
 import engine.open2d.draw.Plane;
 import engine.open2d.renderer.WorldRenderer;
 import engine.open2d.texture.AnimatedTexture.Playback;
 import game.GameTools;
 import game.GameTools.Gesture;
 import game.open2d.R;
 
 public class Player extends GameObject{
 	public static enum PlayerState implements GameObjectState{
 		STAND("jack_stand"),
 		RUN("jack_run"),
 		DODGE("jack_dodge"),
 		DEAD("jack_dead"),
 		NTAP("jack_n_tap"),
 		NFSWIPE("jack_n_fswipe");
 		
 		public static PlayerState getStateFromName(String name){
 			for(PlayerState playerState : PlayerState.values()){
 				if(name.equals(playerState.getName())){
 					return playerState;
 				}
 			}
 			return null;
 		}
 		
 		String name;
 		PlayerState(String n){
 			name = n;
 		}
 		
 		public String getName(){
 			return name;
 		}
 	}
 
 	public static String OBJNAME = "player";
 	private static PlayerState INIT_STATE = PlayerState.STAND;
 	
 	private static float WALK_SPEED = 0.2f;
 	private static float STRIKE_SPEED = 0.1f;
 	private static float DODGE_SPEED = 0.23f;
 	private static float BUFFER = 0.4f;
 	private static float COLLISION_BUFFER = 1.0f;
 	public static float CANCEL_STRIKE_FRAMES = 8;
 
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
 		this.direction = Direction.RIGHT;
 	}
 	
 	@Override
 	public void setupAnimRef() {
 		animationRef = new HashMap<GameObjectState, Integer>();
 		//animationRef.put(PlayerState.STAND, new Plane(R.drawable.rising_stance, name+"_"+PlayerState.STAND.getName(), width, height, 4, 7));
 		animationRef.put(PlayerState.STAND, R.drawable.jack_stand);
 		animationRef.put(PlayerState.DEAD, R.drawable.rising_stance);
 		animationRef.put(PlayerState.RUN, R.drawable.jack_run);
 		animationRef.put(PlayerState.DODGE, R.drawable.rising_dodge);
 		animationRef.put(PlayerState.NTAP, R.drawable.jack_n_tap);
 		animationRef.put(PlayerState.NFSWIPE, R.drawable.jack_n_fswipe);
 		
 	}
 	
 	@Override
 	public void mapActionData(List<ActionData> actionData) {
 		for(ActionData data : actionData){
 			PlayerState state = PlayerState.getStateFromName(data.getName());
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
 
 	@Override
 	public void updateState() {
 		if(playerState == PlayerState.RUN || playerState == PlayerState.STAND){
 			executeMovement();
 		}
 		
 		if(GameTools.gestureBreakdownHorizontal(gesture) == Gesture.LEFT){
 			playerState = PlayerState.NFSWIPE;
 			this.direction = Direction.LEFT;
 		} else if(GameTools.gestureBreakdownHorizontal(gesture) == Gesture.RIGHT) {
 			playerState = PlayerState.NFSWIPE;
 			this.direction = Direction.RIGHT;
 		}
 		
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
 		if(playerState == PlayerState.RUN){
 			if(direction == Direction.RIGHT)
 				x += WALK_SPEED;
 			else if(direction == Direction.LEFT)
 				x -= WALK_SPEED;
 		}
 		
 		if(playerState == PlayerState.NFSWIPE){
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
		Plane display = currentAction.getAnimation();
 		if(currentAction != actionData.get(playerState))
 			this.switchAnimation(playerState);
		
		if(direction==Direction.RIGHT){
			display.flipTexture(false);
		} else if(direction==Direction.LEFT){
			display.flipTexture(true);
		}
 	}
 	
 	@Override
 	public void updateAfterDisplay() {
 		Plane display = currentAction.getAnimation();
 		if(display.isPlayed()){
 			/*
 			if(isStrikeState()){
 				display.resetAnimation();
 				playerState = PlayerState.STAND;
 				
 				updatePunchIndex();
 			}
 			
 			if(isFinishState()){
 				display.resetAnimation();
 				playerState = PlayerState.STAND;
 				
 				updateFinishIndex();
 			}
 
 			if(isCounterState()){
 				display.resetAnimation();
 				playerState = PlayerState.STAND;
 				
 				updateCounterIndex();
 			}
 			*/
 			
 			if(	playerState==PlayerState.NFSWIPE){
 				gesture = Gesture.NONE;
 				display.resetAnimation();
 				playerState = PlayerState.STAND;
 			}
 			
 			if(	playerState==PlayerState.DODGE){
 				gesture = Gesture.NONE;
 				display.resetAnimation();
 				playerState = PlayerState.STAND;				
 			}
 			
 			if(playerState==PlayerState.DEAD){
 				display.resetAnimation();
 				playerState = PlayerState.STAND;
 			}
 		}
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
