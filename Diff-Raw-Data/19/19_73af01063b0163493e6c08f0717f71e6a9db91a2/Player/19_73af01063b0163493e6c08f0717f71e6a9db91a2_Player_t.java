 package object;
 
 import java.util.HashMap;
 import java.util.HashSet;
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
import android.view.Display;
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
 		ARC("jump_arc"),
 		LAND("jump_land"),
 		DASH("dash"),
 		DODGE("dodge"),
 		DEAD("dead"),
 		NTAP("n_tap"),
 		NFSWIPECOMBO1("n_fswipe_combo1"),
 		NFSWIPECOMBO2("n_fswipe_combo2"),
 		NFSWIPE("n_fswipe"),
 		NUSWIPE("n_uswipe"),
 		NDSWIPE("n_dswipe"),
 		AFSWIPE("a_fswipe"),
 		ADSWIPE("a_dswipe"),
 		DFSWIPE("d_fswipe"),
 		DUSWIPE("d_uswipe"),
 		DDSWIPE("d_dswipe"),
 		DDSWIPEFOLLWUP("d_dswipe_followup");
 		
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
 
 	private static final int TEMP_FRAME = 5;
 
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
 	
 	private float unprojectedX;
 	private float unprojectedY;
 	
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
 		animationRef.put(PlayerState.TEMP, R.drawable.jack_n_combo2);
 		animationRef.put(PlayerState.STAND, R.drawable.jack_stand);
 		animationRef.put(PlayerState.DEAD, R.drawable.rising_stance);
 		animationRef.put(PlayerState.RUN, R.drawable.jack_run);
 		animationRef.put(PlayerState.JUMP, R.drawable.jack_jump_startup);
 		animationRef.put(PlayerState.ARC, R.drawable.jack_jump_arc);
 		animationRef.put(PlayerState.LAND, R.drawable.jack_jump_land);
 		animationRef.put(PlayerState.DASH, R.drawable.jack_dash);
 		animationRef.put(PlayerState.DODGE, R.drawable.rising_dodge);
 		animationRef.put(PlayerState.NTAP, R.drawable.jack_n_tap);
 		animationRef.put(PlayerState.NFSWIPE, R.drawable.jack_n_fswipe);
 		animationRef.put(PlayerState.NFSWIPECOMBO1, R.drawable.jack_n_combo1);
 		animationRef.put(PlayerState.NFSWIPECOMBO2, R.drawable.jack_n_combo2);
 		animationRef.put(PlayerState.NUSWIPE, R.drawable.jack_n_uswipe);
 		animationRef.put(PlayerState.NDSWIPE, R.drawable.jack_n_dswipe);
 		animationRef.put(PlayerState.AFSWIPE, R.drawable.jack_a_fswipe);
 		animationRef.put(PlayerState.ADSWIPE, R.drawable.jack_a_dswipe);
 		animationRef.put(PlayerState.DFSWIPE, R.drawable.jack_d_fswipe);
 		animationRef.put(PlayerState.DUSWIPE, R.drawable.jack_d_uswipe);
 		animationRef.put(PlayerState.DDSWIPE, R.drawable.jack_d_dswipe);
 		animationRef.put(PlayerState.DDSWIPEFOLLWUP, R.drawable.jack_d_dswipe_followup);
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
 
 	public void setStateUsingTotalName(String state){
 		StringBuffer buffer = new StringBuffer(PlayerState.OBJECT);
 		buffer.append("_");
 		buffer.append(state);
 		this.playerState = PlayerState.getStateFromTotalName(buffer.toString());
 	}
 	
 	public void setPlayerState(PlayerState playerState) {
 		this.playerState = playerState;
 	}
 	
 	@Override
 	public void passTouchEvent(MotionEvent e, WorldRenderer worldRenderer){
 		Plane display = currentAction.getAnimation();
 		float[] unprojectedPoints = worldRenderer.getUnprojectedPoints(e.getX(), e.getY(), display);
 		/*
 		//if(playerState == PlayerState.RUN || playerState == PlayerState.STAND){
 		if(inputList.isEmpty()){
 			moveToX = unprojectedPoints[0];
 			moveToY = unprojectedPoints[1];
 		}
 		*/
 		
 		unprojectedX = unprojectedPoints[0];
 		unprojectedY = unprojectedPoints[1];
 		
 		display.unprojectDisable();
 		
 		if(playerState == PlayerState.TEMP){
 			float[] coord = worldRenderer.getUnprojectedPoints(e.getX(), e.getY(), this.getDisplay());
 			Log.i("rising_debug",coord[0] + " " + coord[1] + " " + coord[2] +" "+getMidX());
 		}
 	}
 
 	public void passDoubleTouchEvent(GestureListener g,  WorldRenderer worldRenderer){
		int width = worldRenderer.getScreenWidth();
		int height = worldRenderer.getScreenHeight();
 		Plane display = currentAction.getAnimation();
 		float[] unprojectedPoints = worldRenderer.getUnprojectedPoints(g.getDoubleTapX(), g.getDoubleTapY(), display);
 		
		//if(g.getDoubleTapY() < height/2){
 		if(unprojectedPoints[1] > (this.getY()+this.getHeight())){
 		//if(unprojectedPoints[1] > this.getMidY()){
 			inputList.add(Gesture.DTAP_UP);
		//} else if(g.getDoubleTapX() > width/2){
 		} else if(unprojectedPoints[0] > this.getX()+this.getWidth()){
 			inputList.add(Gesture.DTAP_RIGHT);
		//} else if(g.getDoubleTapX() < width/2){
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
 		
 		if(playerState == PlayerState.RUN || playerState == PlayerState.STAND){
 			executeMovement();
 		}
 		
 		/*
 		if(playerState == PlayerState.JUMP){
 			if(!initSpeed && this.getY() <= GameLogic.FLOOR && yVelocity <= 0){
 				playerState = PlayerState.LAND;
 			}
 		}
 		*/
 
 		//String state1 = playerState.toString();
 		executeTriggers();
 		executeInput();
 		/*
 		String state2 = playerState.toString();
 		if(!state1.equals(state2)){
 			Log.d("rising_debug_update_state", currentAction.getName()+" "+currentAction.getAnimation().isPlayed()+"");
 			Log.d("rising_debug_update_state",state1 +" to "+state2);
 		}
 		*/
 
 		if(	currentAction != actionData.get(playerState) || resetAnim){
 			this.switchAction(playerState);
 			resetAnim = false;
 		}
 		
 	}
 	
 	@Override
 	public void updateLogic() {
 		if(currentAction.isHitBoxActive() && isHitAvailable()){
 			this.setHitActive(true);
 		} else {
 			this.setHitActive(false);
 		}
 		
 		String hitType = currentAction.getActionProperties().getHitType(); 
 		if(hitType.equals(ActionDataTool.SINGLE_HIT)){
 			
 		}
 		
 		//should hitstop return from function?
 		if(hitStopFrames > 0){
 			hitStopFrames--;
 			this.setHitActive(false);//disable hit box for attack won't come back till next attack
 			return;
 		}
 		
 		if(playerState == PlayerState.TEMP){
 			currentAction.getAnimation().setFrame(TEMP_FRAME-1);
 			direction = Direction.RIGHT;
 			//currentAction.getAnimation().setPlayback(Playback.PAUSE);
 		}
 
 		if(playerState == PlayerState.RUN){
 			if(direction == Direction.RIGHT)
 				x += WALK_SPEED;
 			else if(direction == Direction.LEFT)
 				x -= WALK_SPEED;
 		} else if(playerState == PlayerState.LAND){
 			executeLogic();
 			moveToX = getMidX();
 			moveToY = getMidY();
 		} else if(	playerState == PlayerState.NFSWIPE||
 					playerState == PlayerState.NFSWIPECOMBO1||
 					playerState == PlayerState.NFSWIPECOMBO2||
 					playerState == PlayerState.NUSWIPE||
 					playerState == PlayerState.AFSWIPE||
 					playerState == PlayerState.NTAP||
 					playerState == PlayerState.DFSWIPE||
 					playerState == PlayerState.DDSWIPE||
 					playerState == PlayerState.DDSWIPEFOLLWUP||
 					playerState == PlayerState.DASH){
 			executeLogic();
 			moveToX = getMidX();
 		} else {
 			executeLogic();
 		}
 
 		if(playerState != PlayerState.STAND && playerState != PlayerState.RUN){
 			//Log.d(playerState.toString(), "playerpos "+getMidX()+" tap "+unprojectedX);
 			//Log.d(playerState.toString(), "xpos "+x+" xvelocity "+xVelocity+" xaccel "+xAccel);
 			//Log.d(playerState.toString(), "ypos "+y+" yvelocity "+yVelocity+" yaccel "+yAccel);
 		}
 	}
 
 	@Override
 	public void updateDisplay() {
 		if(hitStopFrames > 0){
 			//Log.d("rising_debug updateDisplay",currentAction.getAnimation().getFrame()+"");
 			//currentAction.getAnimation().setPlayback(Playback.PAUSE);
 			return;
 		} else {
 			//Playback defaultPlayback = currentAction.getPlaneData().getPlayback();
 			//currentAction.getAnimation().setPlayback(defaultPlayback);
 			deactivateHitStop();
 		}
 
 		if(direction==Direction.RIGHT){
 			currentAction.flipHorizontal(false);
 		} else if(direction==Direction.LEFT){
 			currentAction.flipHorizontal(true);
 		}
 		
 		//incrementGameFrame();
 		//currentAction.getAnimation().setFrame(gameFrame);
 	}
 	
 	@Override
 	public void updateAfterDisplay() {
 		Plane display = currentAction.getAnimation();
 		
 		if(hitStopFrames > 0){
 			return;
 		}
 		
 		if(display.isPlayed()){
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
 
 	private void executeInput(){
 		Gesture gesture = Gesture.NONE;
 		//if(!inputList.isEmpty())
 		//	Log.d("rising_debug_execute_input", inputList.toString()+" "+ playerState + " "+currentAction.getAnimation().getFrame());
 		
 		/*
 		if(hitStopFrames > 0){
 			//Log.d("rising_debug", inputList.toString()+" "+currentAction.getAnimation().getFrame());
 			return;
 		}
 		*/
 		
 		HashSet<Integer> cancelFrames = currentAction.getActionProperties().getCancelFrame();
 		if(!cancelFrames.isEmpty()){
 			if(!cancelFrames.contains(currentAction.getAnimation().getFrame())){
 				return;
 			}
 		}
 
 		if(!inputList.isEmpty()){
			//Log.d("rising_debug", inputList.toString()+" "+currentAction.getAnimation().getFrame());
			//Log.d(playerState.toString(), gesture.toString()+" "+direction.toString());
 			gesture = inputList.getFirst();
 			inputList.removeFirst();
 		}
 		
 
 		if(gesture == Gesture.TAP){
 			if(currentAction.getActionProperties().hasCancel(ActionDataTool.TAP_TRIGGER)){
 				for(GameObject gameObject : gameObjects.values()){
 					if(gameObject instanceof Enemy){
 						Enemy enemy = (Enemy) gameObject;
 						if(enemy.isSelected()){
 							String cancel = currentAction.getActionProperties().getCancel(ActionDataTool.TAP_TRIGGER);
 							setStateUsingTotalName(cancel);
 							initSpeed = true;
 							enemy.selected = false;
 						}
 					}
 				}
 			}
 			if(!initSpeed){
 				moveToX = unprojectedX;
 				moveToY = unprojectedY;
 			}
 
 			if(unprojectedX > getMidX()){
 				this.direction = Direction.RIGHT;
 			}else if(unprojectedX < getMidX()){
 				this.direction = Direction.LEFT;
 			}
 
 		} else if(gesture == Gesture.DTAP_UP){
 			if(currentAction.getActionProperties().hasCancel(ActionDataTool.DTAP_U_TRIGGER)){
 				String cancel = currentAction.getActionProperties().getCancel(ActionDataTool.DTAP_U_TRIGGER);
 				setStateUsingTotalName(cancel);
 				initSpeed = true;
 			}
 		} else if(gesture == Gesture.DTAP_LEFT || gesture == Gesture.DTAP_RIGHT){
 			if(currentAction.getActionProperties().hasCancel(ActionDataTool.DTAP_F_TRIGGER)){
 				String cancel = currentAction.getActionProperties().getCancel(ActionDataTool.DTAP_F_TRIGGER);
 				setStateUsingTotalName(cancel);
 				initSpeed = true;
 			}
 			
 			if(gesture == Gesture.DTAP_LEFT){
 				this.direction = Direction.LEFT;
 			} else if(gesture == Gesture.DTAP_RIGHT){
 				this.direction = Direction.RIGHT;
 			}
 		}
 		
 		if(Math.abs(gesture.getXDiffSize()) > Math.abs(gesture.getYDiffSize())){
 			if(GameTools.gestureBreakdownHorizontal(gesture) == Gesture.SWIPE_LEFT){
 				if(currentAction.getActionProperties().hasCancel(ActionDataTool.SWIPE_F_TRIGGER)){
 					String cancel = currentAction.getActionProperties().getCancel(ActionDataTool.SWIPE_F_TRIGGER);
 					setStateUsingTotalName(cancel);
 					initSpeed = true;
 					this.direction = Direction.LEFT;
 				}
 			} else if(GameTools.gestureBreakdownHorizontal(gesture) == Gesture.SWIPE_RIGHT) {
 				if(currentAction.getActionProperties().hasCancel(ActionDataTool.SWIPE_F_TRIGGER)){
 					String cancel = currentAction.getActionProperties().getCancel(ActionDataTool.SWIPE_F_TRIGGER);
 					setStateUsingTotalName(cancel);
 					initSpeed = true;
 					this.direction = Direction.RIGHT;
 				}
 			}
 		} else {
 			if(GameTools.gestureBreakdownVertical(gesture) == Gesture.SWIPE_UP) {
 				if(currentAction.getActionProperties().hasCancel(ActionDataTool.SWIPE_U_TRIGGER)){
 					String cancel = currentAction.getActionProperties().getCancel(ActionDataTool.SWIPE_U_TRIGGER);
 					setStateUsingTotalName(cancel);
 					initSpeed = true;
 				}
 
 				if(unprojectedX > getMidX()){
 					this.direction = Direction.RIGHT;
 				}else if(unprojectedX < getMidX()){
 					this.direction = Direction.LEFT;
 				}
 			} else if(GameTools.gestureBreakdownVertical(gesture) == Gesture.SWIPE_DOWN){
 				if(currentAction.getActionProperties().hasCancel(ActionDataTool.SWIPE_D_TRIGGER)){
 					String cancel = currentAction.getActionProperties().getCancel(ActionDataTool.SWIPE_D_TRIGGER);
 					setStateUsingTotalName(cancel);
 					initSpeed = true;
 				}
 
 				if(unprojectedX > getMidX()){
 					this.direction = Direction.RIGHT;
 				}else if(unprojectedX < getMidX()){
 					this.direction = Direction.LEFT;
 				}
 			}
 
 			/*
 			if(gesture.getXDiffSize() > 0){
 				this.direction = Direction.LEFT;
 			} else if(gesture.getXDiffSize() < 0) {
 				this.direction = Direction.RIGHT;
 			}
 			*/
 		}
 		
 		return;
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
