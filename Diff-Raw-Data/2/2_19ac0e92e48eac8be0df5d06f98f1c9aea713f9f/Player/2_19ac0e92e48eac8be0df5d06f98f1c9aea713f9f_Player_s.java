 package object;
 
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 
 import object.Enemy.EnemyState;
 import object.GameObject.Direction;
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
 		STAND("stand",0f,0f,0f),
 		RUN("run",0f,0f,0f),
 		DODGE("dodge",0f,0f,0f),
 		DEAD("dead",0f,0f,0f),
 		STRIKE1("strike1",-1.0f,0f,0f),
 		STRIKE2("strike2",-1.0f,0f,0f),
 		STRIKE3("strike3",-1.0f,0f,0f),
 		FINISH1("finish1",0f,0f,0f),
 		FINISH2("finish2",0f,0f,0f),
 		COUNTER1("counter1",0f,0f,0f);
 		
 		private static int STRIKE_NUMBERS = 3;
 		private static int FINISH_NUMBERS = 2;
 		private static int COUNTER_NUMBERS = 1;
 		
 		public static PlayerState getRandomStrike(){
 			double randNum = Math.random();
 			int strike_number = (int) (randNum * STRIKE_NUMBERS + 1);
 			
 			return getStrike(strike_number);
 		}
 		
 		public static PlayerState getRandomFinish(){
 			double randNum = Math.random();
 			int finish_number = (int) (randNum * FINISH_NUMBERS + 1);
 			
 			return getFinish(finish_number);
 		}
 		
 		public static PlayerState getStrike(int index){
 			if(index < 1 || index > STRIKE_NUMBERS)
 				return STRIKE2;
 			
 			StringBuffer buffer = new StringBuffer();
 			buffer.append("strike");
 			buffer.append(index);
 			for(PlayerState playerState : PlayerState.values()){
 				if(buffer.toString().equals(playerState.getName())){
 					return playerState;
 				}
 			}
 			return null;
 		}
 		
 		public static PlayerState getFinish(int index){
 			if(index < 1 || index > FINISH_NUMBERS)
 				return FINISH2;
 			
 			StringBuffer buffer = new StringBuffer();
 			buffer.append("finish");
 			buffer.append(index);
 			for(PlayerState playerState : PlayerState.values()){
 				if(buffer.toString().equals(playerState.getName())){
 					return playerState;
 				}
 			}
 			return null;
 		}
 		
 		public static PlayerState getCounter(int index){
 			if(index < 1 || index > COUNTER_NUMBERS)
 				return COUNTER1;
 			
 			StringBuffer buffer = new StringBuffer();
 			buffer.append("counter");
 			buffer.append(index);
 			for(PlayerState playerState : PlayerState.values()){
 				if(buffer.toString().equals(playerState.getName())){
 					return playerState;
 				}
 			}
 			return null;
 		}
 		
 		String name;
 		float offSnapX;
 		float offSnapY;
 		float offSnapZ;
 		PlayerState(String n, float x, float y, float z){
 			name = n;
 			offSnapX = x;
 			offSnapY = y;
 			offSnapZ = z;
 		}
 		
 		public String getName(){
 			return name;
 		}
 		
 		public float getOffSnapX(){
 			return offSnapX;
 		}
 		
 		public float getOffSnapY(){
 			return offSnapY;
 		}
 		
 		public float getOffSnapZ(){
 			return offSnapZ;
 		}
 	}
 	
 	public static String OBJNAME = "player";
 	
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
 	
 	public Player(LinkedHashMap<String,GameObject> gameObjects, float x, float y, float width, float height){
 		super(gameObjects,x,y,width,height);
 		
 		playerState = PlayerState.STAND;
 		this.name = OBJNAME;
 		
 		this.moveToX = x;
 		this.moveToY = y;
 		this.z = -1.0f;
 		
 		this.punchIndex = 1;
 		this.finishIndex = 1;
 		this.counterIndex = 1;
 		
 		animations = new HashMap<GameObjectState, Plane>();
 		animations.put(PlayerState.STAND, new Plane(R.drawable.rising_stance, name+"_"+PlayerState.STAND.getName(), width, height, x, y, z, 4, 7));
 		animations.put(PlayerState.RUN, new Plane(R.drawable.rising_run, name+"_"+PlayerState.RUN.getName(), width, height, x, y, z, 11, 3));
 		animations.put(PlayerState.DODGE, new Plane(R.drawable.rising_dodge, name+"_"+PlayerState.DODGE.getName(), width, height, x, y, z, 3, 3));
 		animations.put(PlayerState.STRIKE1, new Plane(R.drawable.rising_strike1, name+"_"+PlayerState.STRIKE1.getName(), width, height, x, y, z, 2, 7));
 		animations.put(PlayerState.STRIKE2, new Plane(R.drawable.rising_strike2, name+"_"+PlayerState.STRIKE2.getName(), width, height, x, y, z, 2, 7));
 		animations.put(PlayerState.STRIKE3, new Plane(R.drawable.rising_strike3, name+"_"+PlayerState.STRIKE3.getName(), width, height, x, y, z, 2, 7));
 		animations.put(PlayerState.FINISH1, new Plane(R.drawable.rising_finish1, name+"_"+PlayerState.FINISH1.getName(), width, height, x, y, z, 8, 5));
 		animations.put(PlayerState.FINISH2, new Plane(R.drawable.rising_finish2, name+"_"+PlayerState.FINISH2.getName(), width, height, x, y, z, 4, 8));
 		animations.put(PlayerState.COUNTER1, new Plane(R.drawable.rising_counter1, name+"_"+PlayerState.COUNTER1.getName(), width, height, x, y, z, 4, 10));
 		
 		this.display = animations.get(PlayerState.STAND);
 		this.direction = Direction.RIGHT;
 	}
 
 	public PlayerState getPlayerState() {
 		return playerState;
 	}
 
 	public void setPlayerState(PlayerState playerState) {
 		this.playerState = playerState;
 	}
 
 	@Override
 	public void passTouchEvent(MotionEvent e, WorldRenderer worldRenderer){
 		Plane selectedPlane = worldRenderer.getSelectedPlane(e.getX(), e.getY());
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
 		for(GameObject gameObject : gameObjects.values()){
 			if(gameObject instanceof Enemy){
 				executeEnemyInteraction((Enemy)gameObject);
 			}
 		}
 		
		if(!isFinishState() || !isCounterState()){
 			if(GameTools.gestureBreakdownHorizontal(gesture) == Gesture.LEFT){
 				playerState = PlayerState.DODGE;
 				direction = Direction.RIGHT;
 			} else if(GameTools.gestureBreakdownHorizontal(gesture) == Gesture.RIGHT){
 				playerState = PlayerState.DODGE;
 				direction = Direction.LEFT;
 			}
 		}
 	}
 	
 	@Override
 	public void updateLogic() {
 		if(playerState == PlayerState.RUN){
 			if(direction == Direction.RIGHT)
 				x += WALK_SPEED;
 			else if(direction == Direction.LEFT)
 				x -= WALK_SPEED;
 		}
 		
 		if(isStrikeState()){
 			if(struckEnemy.getX() < x) {
 				direction = Direction.LEFT;
 				x = struckEnemy.getX()-playerState.getOffSnapX();
 				moveToX = getMidX();
 			} else if(struckEnemy.getX() > x){
 				direction = Direction.RIGHT;
 				x = struckEnemy.getX()+playerState.getOffSnapX();
 				moveToX = getMidX();
 			}
 		}
 		
 		if(isCounterState()){
 			gesture = Gesture.NONE;
 		}
 		
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
 		if(display != animations.get(playerState))
 			switchAnimation(playerState);
 		
 		if(direction==Direction.RIGHT){
 			display.flipTexture(false);
 		} else if(direction==Direction.LEFT){
 			display.flipTexture(true);
 		}
 	}
 		
 	@Override
 	public void updateAfterDisplay() {
 		if(display.isPlayed()){
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
 			
 			if(	playerState==PlayerState.DODGE){
 				gesture = Gesture.NONE;
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
 
 	private void executeEnemyInteraction(Enemy enemy){
 		
 		if(		playerState == PlayerState.STAND || playerState == PlayerState.RUN || playerState == PlayerState.DODGE ||
 				(isStrikeState() &&
 				display.getFrame() >= display.getTotalFrame()-CANCEL_STRIKE_FRAMES)){
 			
 			if(	GameTools.boxColDetect(this, enemy, COLLISION_BUFFER) && enemy.isSelected()  && !enemy.isDodging()){
 				playerState = PlayerState.getStrike(punchIndex);
 				struckEnemy = enemy;
 				
 				if(enemy.getStruck() <= 0){
 					playerState = PlayerState.getFinish(finishIndex);
 				}
 				
 				if(enemy.isEnemyStriking() && playerState == PlayerState.DODGE){
 					playerState = PlayerState.getCounter(counterIndex);
 					Log.d("open2d", ""+playerState);
 				}
 
 				updatePunchIndex();
 			}
 		}
 	}
 
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
 				playerState==PlayerState.FINISH2);
 	}
 	
 	public boolean isCounterState(){
 		return (playerState==PlayerState.COUNTER1);
 	}
 }
