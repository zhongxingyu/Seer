 package com.testgame.scene;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.andengine.entity.IEntity;
 import org.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
 import org.andengine.entity.modifier.MoveModifier;
 
 
 import org.andengine.engine.camera.BoundCamera;
 import org.andengine.engine.camera.SmoothCamera;
 import org.andengine.engine.camera.hud.HUD;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.sprite.ButtonSprite;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.entity.text.TextOptions;
 import org.andengine.extension.tmx.TMXLayer;
 import org.andengine.extension.tmx.TMXTile;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.input.touch.controller.MultiTouchController;
 import org.andengine.input.touch.detector.PinchZoomDetector;
 import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
 import org.andengine.entity.sprite.ButtonSprite.OnClickListener;
 import org.andengine.util.Constants;
 import org.andengine.util.adt.align.HorizontalAlign;
 import org.andengine.util.modifier.IModifier;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.graphics.Point;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 
 import com.parse.ParseException;
 import com.parse.FindCallback;
 import com.parse.ParseObject;
 import com.parse.ParsePush;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 import com.testgame.AGame;
 import com.testgame.mechanics.unit.AUnit;
 import com.testgame.player.APlayer;
 import com.testgame.player.ComputerPlayer;
 import com.testgame.scene.SceneManager.SceneType;
 import com.testgame.sprite.CharacterSprite;
 import com.testgame.sprite.HighlightedSquare;
 
 public class GameScene extends BaseScene implements IOnSceneTouchListener, IPinchZoomDetectorListener {
 
 	private float mTouchX = 0, mTouchY = 0, mTouchOffsetX = 0, mTouchOffsetY = 0;
 	
 	private Rectangle currentTileRectangle;
 	private AUnit selectedCharacter;
 	
 	private static float ZOOM_FACTOR_MIN = .5f;
 	private static float ZOOM_FACTOR_MAX = 1.5f;
 	
 	public IEntityModifierListener animationListener;
 	//public IEntityModifierListener computerAnimationListener;
 	
 	private AGame game;
 	
 	public HUD hud;
 	
 	public boolean animating;
 	
 	public int tileSize;
 	public int widthInTiles;
 	public int heightInTiles;
 	
 	private JSONArray moves;
 	
 	private ButtonSprite pauseButton;
 	private ButtonSprite tutorialButton;
 	
 	private Text eventsMessage;
 	private Text turnMessage;
 	
 	private Text curUnitHealth;
 	private Text curUnitEnergy;
 	private Text curUnitAttack;
 	
 	private TutorialScene tutorial;
 	
 	private AlertDialog quitDialog;
 	
 	private Text endGameMessage;
 	
 	public TMXLayer tmxLayer;
 	
 	public float OldX, OldY;
 	
 	private ArrayList<HighlightedSquare> highlightedSquares;
 	
 	public HighlightedSquare currentlySelectedMoveTile;
 	
 	private PinchZoomDetector mPinchZoomDetector;
 	private float mPinchZoomStartedCameraZoomFactor;
 	
 	private Sprite bottomBar;
 	
 	@Override
 	public void onBackKeyPressed() {
 		
 		if (this.hud.getChildScene() == null) { // tutorial window NOT up
 			activity.runOnUiThread(new Runnable() {
 	    	    @Override
 	    	    public void run() {
 	    	    	 createQuitDialog();
 	    	    }
 	    	});
 		} else {
 			this.hud.clearChildScene();
 		}
 		
 	}
 
 	@Override
 	public SceneType getSceneType() {
 		return SceneType.SCENE_GAME;
 	}
 	
 	public void setPlayerText(String player) {
 		this.turnMessage.setText(player+"'s Turn");
 	}
 	
 	public void setEventText(String eventDescription) {
 		this.eventsMessage.setText(eventDescription);
 	}
 	
 	public void setEndGameText(APlayer winner) {
 		this.endGameMessage.setText(winner.getName() + " has won!!");
 	}
 	
 	/**
 	 * @param unitCounts The number of each unit desired, in this order: jock, ditz, nerd (list is of size 3).
 	 * //TODO: need to get these counts for both players
 	 */
 	
 	@Override
 	public void createScene() { // will be passed player names and some game state representation later
 		// Set up touch.
 		this.setOnSceneTouchListener(this);
 		this.engine.setTouchController(new MultiTouchController());
 		this.mPinchZoomDetector = new PinchZoomDetector(this);
 		
 		this.animationListener = new IEntityModifierListener() {
 			@Override
 			public void onModifierStarted(IModifier<IEntity> pModifier,
 					IEntity pItem) {
 				animating = true;
 				camera.setChaseEntity(pItem);
 				
 			}
 			@Override
 			public void onModifierFinished(IModifier<IEntity> pModifier,
 					IEntity pItem) {
 				animating = false;
 				camera.setChaseEntity(null);
 				((AUnit)pItem).setCurrentTileIndex(((AUnit)pItem).start_frame);
 			}
 		};
 		
 		Log.d("AndEngine", "" + resourcesManager.tiledMap.getTMXLayers().size());
 		
 		this.tmxLayer = resourcesManager.tiledMap.getTMXLayers().get(0);  
 		moves = new JSONArray();
 		
 		this.tileSize = resourcesManager.tiledMap.getTileHeight();
 		this.heightInTiles = resourcesManager.tiledMap.getTileRows();
 		this.widthInTiles = resourcesManager.tiledMap.getTileColumns();
 		
 		Log.d("AndEngine", "Created map of "+this.widthInTiles+"x"+this.heightInTiles+" of "+this.tileSize+"px tiles.");
 		attachChild(resourcesManager.tiledMap);
 		resourcesManager.tiledMap.setOffsetCenter(0, 0);
 		
 		// Initialize highlighted squares list.
 		this.highlightedSquares = new ArrayList<HighlightedSquare>();
 		
 		// Edit camera options.
 		((BoundCamera)camera).setBounds(0, 0, resourcesManager.tiledMap.getWidth(), resourcesManager.tiledMap.getHeight());
 		((BoundCamera)camera).setBoundsEnabled(true);
 		camera.setCenter(0, 0);
 		
 		// Initialize the game.
 		this.setGame(new AGame(new APlayer("Your"), new ComputerPlayer("Opponent's"), widthInTiles, heightInTiles, this, resourcesManager.turn));
 		
 		createHUD();
 	    
 		// Initialize selection rectangle.
 		this.currentTileRectangle = new Rectangle(0, 0, resourcesManager.tiledMap.getTileWidth(), resourcesManager.tiledMap.getTileHeight(), vbom);
 		currentTileRectangle.setOffsetCenter(0, 0);
 		currentTileRectangle.setColor(1, 0, 0, 0);
 		attachChild(currentTileRectangle);
 		startCompTurn();
 		
 	}
 	
 	protected void createHUD() {
 		
 		this.hud = new HUD();
 		
 		hud.attachChild(new Sprite(240, 790, resourcesManager.top_bar, vbom));
 		
 		hud.attachChild(bottomBar = new Sprite(240, -300, resourcesManager.bottom_bar, vbom));
 		
 		// Create the game events messages.
 		this.eventsMessage = new Text(240, 760, resourcesManager.handwriting_font, "Destroy All Enemy Units\n to Win!", 200, new TextOptions(HorizontalAlign.CENTER), vbom);
 		this.endGameMessage = new Text(240, 400, resourcesManager.font, "", 50, new TextOptions(HorizontalAlign.CENTER), vbom);
 		
 		// Initialize HUD and its entities.
 		this.curUnitAttack = new Text(300, 130, resourcesManager.handwriting_font, "Attack: " , 75, new TextOptions(HorizontalAlign.LEFT), vbom);
 		this.curUnitAttack.setOffsetCenter(0, 0);
 		this.curUnitEnergy = new Text(50, 250, resourcesManager.handwriting_font, "Energy: ", 25, new TextOptions(HorizontalAlign.LEFT), vbom);
 		this.curUnitEnergy.setOffsetCenter(0,0);
 		this.curUnitHealth = new Text(50, 200, resourcesManager.handwriting_font, "Health: " , 25, new TextOptions(HorizontalAlign.LEFT), vbom);
 		this.curUnitHealth.setOffsetCenter(0, 0);
 		
 		bottomBar.attachChild(curUnitAttack);
 		bottomBar.attachChild(curUnitEnergy);
 		bottomBar.attachChild(curUnitHealth);
 		
 		final GameScene game = this;
 		
 		tutorialButton = new ButtonSprite(480 - 40, 800 - 40, resourcesManager.question_region, vbom, new OnClickListener() {
 
 			@Override
 			public void onClick(ButtonSprite pButtonSprite,float pTouchAreaLocalX, float pTouchAreaLocalY) {
 				
 				if (animating) return;
 				
 				Log.d("AndEngine", "launching tutorial scene");
 				OldX = camera.getCenterX();
 				OldY = camera.getCenterY();
 				SceneManager.getInstance().previousScene = game.getSceneType();
 				SceneManager.getInstance().loadTutorialScene(game.engine);
 			}
 		});
 		
 		tutorialButton.setScale(.5f);
 		
 		
 		pauseButton = new ButtonSprite(40, 760, resourcesManager.pause_region, vbom, new OnClickListener(){
             
             @Override
             public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX,
                             float pTouchAreaLocalY) {
             	
             	if (animating) return;
             	
             	activity.runOnUiThread(new Runnable() {
 	        	    @Override
 	        	    public void run() {
 	        	    	 pauseMenu();
 	          			 
 	        	    }
 	        	});
             				
             }
 		});
 		
 		pauseButton.setScale(2.3f);
 	
 	    //hud.attachChild(turnMessage);
 	    //hud.attachChild(nextTurnButton);
 	    //hud.registerTouchArea(nextTurnButton);
 	    hud.attachChild(eventsMessage);
 	    hud.attachChild(tutorialButton);
 		hud.registerTouchArea(tutorialButton);
 		
 	   // hud.attachChild(turnMessage);
 	    hud.attachChild(pauseButton);
 	    hud.registerTouchArea(pauseButton);
 	    //hud.attachChild(eventsMessage);
 	    
 	    camera.setHUD(hud);
 	}
 	
 	protected Scene getTutorial() {
 		if (tutorial == null) {
 			tutorial = new TutorialScene();
 		}
 		
 		return tutorial;
 	}
 
 	@Override
 	public void disposeScene()
 	{
 	    camera.setHUD(null);
 	    camera.setCenter(240, 400);
 	    ((BoundCamera)camera).setBoundsEnabled(false);
 	    ((SmoothCamera) camera).setZoomFactor(1.0f);
 	    resourcesManager.unloadGameTextures();
 	    // TODO: unload all of the graphics
 	}
 	
 	public void activateAndSelect(final CharacterSprite sprite) {
 		
 		if (this.selectedCharacter == null) { 
 			
 			Log.d("AndEngine", "[In ActivateAndSelect] no one selected before, setting character.");
 			// no character selected, select the character we touched.
 			
 			// Set selected character - displays information on HUD.
 			this.setSelectedCharacter((AUnit) sprite);
 			
 			activity.runOnUiThread(new Runnable() {
         	    @Override
         	    public void run() {
         	    	 unitOptions((AUnit)sprite);
           			 
         	    }
         	});
 			
 			return;
 		}
 
 		else if (this.selectedCharacter == sprite) { // clicked same character, deselect
 			
 			Log.d("AndEngine", "[In activateAndSelect] we were activated, deselecting now");
 			
 			this.deselectCharacter(true);
 			return;
 			
 		}
 		
 		else { // clicked different sprite.
 			Log.d("AndEngine", "[In ActivateAndSelect] other team selected before, setting character.");
 			
 			// Set selected character - displays information on HUD.
 			this.setSelectedCharacter((AUnit) sprite);
 			
 			
 			
 			activity.runOnUiThread(new Runnable() {
         	    @Override
         	    public void run() {
         	    	 unitOptions((AUnit)sprite);
           			 
         	    }
         	});
 			return;
 		}
 		
 		
 		/*else { // clicked another enemy, attack!
 			
 			((AUnit) sprite).attack(this.getSelectedCharacter());
 			this.attackNotification.setText(this.game.getCurrentPlayer().getName() + " is attacking " + this.game.getOtherPlayer().getName() + "!");
 			hud.attachChild(attackNotification);
 			
 		}*/
 	}
 	
 	public void highlightAvailableTargets(TMXTile startTile, CharacterSprite sprite, boolean type) {
 		ArrayList<AUnit> targets = ((AUnit)sprite).availableTargets();
 		
 		for (AUnit target: targets){
 		
 			int x = (int) target.getX();
 			int y = (int) target.getY();
 		
 			TMXTile t = this.tmxLayer.getTMXTileAt(x, y);
 				
 			HighlightedSquare availableMove = new HighlightedSquare(t, x, y, tileSize, this, target);
 			
 			this.highlightedSquares.add(availableMove);
 			availableMove.setOffsetCenter(0, 0);
 			float redValue;
 			if (selectedCharacter == null || t == null) { // checks because of that weird occasional null pointer exception
 				redValue = 1;
 			}
 			else {
 				redValue = 1.0f/selectedCharacter.getAttackRange() * AUnit.manhattanDistance(selectedCharacter.getMapX(), selectedCharacter.getMapY(), t.getTileColumn(), heightInTiles - t.getTileRow() - 1) / 2 + .1f;
 			}
 			availableMove.setColor(1, 0, 0, redValue);
 			attachChild(availableMove);
 			
 		}
 	}
 	
 	/**
 	 * Highlights touched unit's available spaces to move.
 	 * @param startTile Tile the unit is on.
 	 * @param sprite The unit itself.
 	 */
 	//TODO: Clean UP Code here for attack and moves.
 	public void highlightAvailableMoves(TMXTile startTile, CharacterSprite sprite, boolean type) {
 
 		ArrayList<Point> moves = ((AUnit)sprite).availableMoves();
 		
 		for (Point p : moves){
 			
 			int x = (int) (p.x*tileSize + sprite.getX());
 			int y = (int) (p.y*tileSize + sprite.getY());
 			
 			TMXTile t = this.tmxLayer.getTMXTileAt(x, y);
 			
 			//if(resourcesManager.tiledMap.getTMXTileProperties(t.getGlobalTileID()) != null)
 		    //    if (resourcesManager.tiledMap.getTMXTileProperties(t.getGlobalTileID()).containsTMXProperty("obstacle", "1")) continue;
 						
 			HighlightedSquare availableMove = new HighlightedSquare(t, x, y, tileSize, this, null);
 			
 			this.highlightedSquares.add(availableMove);
 			availableMove.setOffsetCenter(0, 0);
 			
 			float blueValue;
 			if (selectedCharacter == null || t == null) { // checks because of that weird occasional null pointer exception
 				blueValue = 1;
 			}
 			else {
 				blueValue = 1.0f/(selectedCharacter.getEnergy()/selectedCharacter.getRange()) * AUnit.manhattanDistance(selectedCharacter.getMapX(), selectedCharacter.getMapY(), t.getTileColumn(), heightInTiles - t.getTileRow() - 1) /2 + .1f;
 			}
 			availableMove.setColor(0, 0, 1, blueValue);
 			attachChild(availableMove);
 			this.registerTouchArea(availableMove);
 		}
 	}
 	
 	public void nextTurn() {
 		Log.d("Turn", getGame().getCount()+"");
 		ParseObject turns = new ParseObject("Turns");
 		turns.put("PlayerId", "user_"+ParseUser.getCurrentUser().getObjectId());
 		turns.put("Player", "user_"+ParseUser.getCurrentUser().getObjectId()+"_"+getGame().getCount());
 		turns.put("GameId", resourcesManager.gameId);
 		turns.put("Moves", moves);
 		turns.saveInBackground();
 		try {
 			JSONObject data = new JSONObject("{\"alert\": \"Next Turn\", \"action\": \"com.testgame.NEXT_TURN\"}");
 			 ParsePush push = new ParsePush();
 			 push.setChannel("user_"+resourcesManager.opponentString);
              push.setData(data);
 			 
              push.sendInBackground();
         } catch (JSONException e) {
 			e.printStackTrace();
 		}
 		 
 		
 		
 		moves = new JSONArray();
 		if(!this.getGame().isFirstTurn()) 
 			this.getGame().incrementCount(); 
 		this.getGame().getPlayer().endTurn();
 		
 	}
 	
 	public void clearSquares() {
 		if(highlightedSquares == null)
 			return;
 		for (HighlightedSquare h : this.highlightedSquares) {
 			if (h.unit != null) h.unit.inSelectedCharactersAttackRange = false;
 			this.unregisterTouchArea(h);
 			this.detachChild(h);
 		}
 		this.highlightedSquares.clear();
 	}
 	
 	public void squareTouched(HighlightedSquare sq, final TouchEvent pSceneTouchEvent) {
 		
 		float sTouchX = pSceneTouchEvent.getX();
         float sTouchY = pSceneTouchEvent.getY();
         
         final TMXTile tmxTile = tmxLayer.getTMXTileAt(sTouchX, sTouchY);
         
 		if(tmxTile != null) {
 			int x = tmxTile.getTileColumn();
 			int y = tmxTile.getTileRow();
 			// TODO: check if a possible move..
 			
 			for (HighlightedSquare h : this.highlightedSquares) {
 				if (tmxTile == h.tile) {
 					//selectedCharacter.setPosition(x, y);
 					Log.d("AndEngine", "[SquareTouched] square at "+x+", "+y);
 					getSelectedCharacter().move(x, heightInTiles - y - 1);
 				}
 			}
 			
 			this.deselectCharacter(true);
 		}
 	}
 	
 	public int getTileSceneX(int tileX, int tileY) {
 		TMXTile t = tmxLayer.getTMXTile(tileX, tileY);
 		
 		int destX = tmxLayer.getTileX(t.getTileColumn());
 		
 		return destX;
 	}
 	
 	public int getTileSceneY(int tileX, int tileY){
 		TMXTile t =  tmxLayer.getTMXTile(tileX, tileY);
 		
 		int destY = tmxLayer.getTileY(heightInTiles - t.getTileRow() - 1);
 		
 		return destY;
 	}
 
 	@Override
 	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pTouchEvent) {
 		
 		if (this.hud.getChildScene() != null) return true; // tutorial menu is up, don't move around!
 		
 		if (animating) return true; // If we're moving, don't recognize touch
 		
 		this.mPinchZoomDetector.onTouchEvent(pTouchEvent);
 		
 		if (this.mPinchZoomDetector.isZooming()) return true; // do the zoom
 		
 		if(pTouchEvent.getAction() == MotionEvent.ACTION_DOWN)
 		{		
 			mTouchX = pTouchEvent.getMotionEvent().getX();
             mTouchY = pTouchEvent.getMotionEvent().getY();
             
             pTouchEvent.getX();
             pTouchEvent.getY();
 			
         }
         else if(pTouchEvent.getAction() == MotionEvent.ACTION_MOVE)
         {    	
             float newX = pTouchEvent.getMotionEvent().getX();
             float newY = pTouchEvent.getMotionEvent().getY();
            
             mTouchOffsetX = (newX - mTouchX);
             mTouchOffsetY = (newY - mTouchY);
            
             this.camera.offsetCenter(-mTouchOffsetX, mTouchOffsetY);
                       
             mTouchX = newX;
             mTouchY = newY;
         }
         
         return true;
 	}
 
     @Override
     public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent) {
             this.mPinchZoomStartedCameraZoomFactor = ((SmoothCamera) this.camera).getZoomFactor();
     }
 
     @Override
     public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
     		float newZoom = this.mPinchZoomStartedCameraZoomFactor * pZoomFactor;
     		if (newZoom > ZOOM_FACTOR_MAX || newZoom < ZOOM_FACTOR_MIN) return;
             ((SmoothCamera) this.camera).setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);
     }
 
     @Override
     public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
     	float newZoom = this.mPinchZoomStartedCameraZoomFactor * pZoomFactor;
 		if (newZoom > ZOOM_FACTOR_MAX || newZoom < ZOOM_FACTOR_MIN) return; 
     	((SmoothCamera) this.camera).setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);
     }
     
     public void attack(CharacterSprite unit)
     {
     	// TODO: is this ever used?
     	this.getSelectedCharacter().attack((AUnit)unit);
 		this.deselectCharacter(true);
     }
 
 	public AUnit getSelectedCharacter() {
 		return selectedCharacter;
 	}
 
 	public void setSelectedCharacter(AUnit selectedCharacter) {
 		
 		Log.d("AndEngine", "[setSelectedCharacter] setting char");
 		
 		if (this.selectedCharacter != null)  deselectCharacter(false);
 		else showBar();
 		this.selectedCharacter = selectedCharacter;
 		
 		this.camera.setCenter(this.selectedCharacter.getX(), this.selectedCharacter.getY());
 		
 		this.selectedCharacter.idleAnimate();
 		
 		this.curUnitAttack.setText(attackStatusString(selectedCharacter.getAttack(), selectedCharacter.getAttackRange(), selectedCharacter.getAttackCost()));
 		this.curUnitEnergy.setText("Energy: " + selectedCharacter.getEnergy()+"/100");
 		this.curUnitHealth.setText("Health: " + selectedCharacter.getHealth() + "/"+selectedCharacter.getMaxHealth());	
 	}
 	
 	public void showBar() {
 		bottomBar.registerEntityModifier(new MoveModifier(.5f, 240, -300, 240, -25));
 	}
 
 	public static String attackStatusString(int power, int range, int cost){
 		return "Power: "+power+"\nRange: "+range+"\nCost: "+cost;
 	}
 	
 	public void deselectCharacter(boolean andHideBar){
 		
 		Log.d("AndEngine", "Deselecting character.");
 		
 		if (this.selectedCharacter == null) return;
 		
 		if (andHideBar) hideBar();
 		
 		this.selectedCharacter.stopAnimation();
 		this.selectedCharacter = null;
 		clearSquares();
 		this.currentTileRectangle.setColor(1,0,0,0);
 	}
 	
 	public void hideBar() {
 		bottomBar.registerEntityModifier(new MoveModifier(.5f, 240, -25, 240, -300));
 	}
 
 	private TMXTile placeSelectionRectangle(CharacterSprite sprite){
 		/* Get the scene-coordinates of the players feet. */
 		final float[] playerFootCordinates = sprite.convertLocalCoordinatesToSceneCoordinates(16, 1);
 
 		/* Get the tile the feet of the player are currently waking on. */
 		final TMXTile tmxTile = tmxLayer.getTMXTileAt(playerFootCordinates[Constants.VERTEX_INDEX_X], playerFootCordinates[Constants.VERTEX_INDEX_Y]);
 		if(tmxTile != null) {
 			// tmxTile.setTextureRegion(null); <-- Eraser-style removing of tiles =D
 			currentTileRectangle.setPosition(tmxLayer.getTileX(tmxTile.getTileColumn()), tmxLayer.getTileY(tmxTile.getTileRow()));
 			currentTileRectangle.setColor(0, 1, 0, .5f);
 		}
 		
 		return tmxTile;
 	}
 	
 	public void addMove(JSONObject move){ 
 		moves.put(move);
 	}
 	
 	public void startCompTurn(){
 		Log.d("Turn", getGame().getCount()+"");
 		ParseQuery query = new ParseQuery("Turns");
		query.whereEqualTo("Player", "user_"+resourcesManager.opponentString+"_"+getGame().getCount());
 		query.findInBackground(new FindCallback() {
 		    public void done(List<ParseObject> itemList, ParseException e) {
 		        if (e == null) {
 		            Log.d("score", "Retrieved " + itemList.size() + " scores");
 		            for(ParseObject ob : itemList){
 		            	if(ob.getString("GameId") == resourcesManager.gameId){
 		            		if(getGame().getCount() != 0){
 				        		JSONArray array = ob.getJSONArray("Moves");
 				        		Log.d("Turn", "Starting computer turn");
 				        		deselectCharacter(false);
 				            	getGame().getCompPlayer().startTurn(getGame(), array);
 				            	ob.deleteInBackground();
 				            	return;
 				        	}
 				        	else{
 				        		JSONObject object = ob.getJSONObject("Init");
 				        		Log.d("Turn", "Starting Init turn");
 				        		deselectCharacter(false);
 				            	getGame().getCompPlayer().init(getGame(), object);
 				            	ob.deleteInBackground();
 				            	return;
 				        		
 				        	}
 		            	}
 		            	ob.deleteInBackground();
 		            }
 		            startCompTurn();   
 		        } else {
 		            Log.d("score", "Error: " + e.getMessage());
 		        }
 		    }
 		});
 		
 	}
 	
 	private void pauseMenu(){
 		final Dialog pausemenu = new Dialog(activity);
 		pausemenu.setTitle("Paused! Turn: "+getGame().getCount());
 		LinearLayout ll = new LinearLayout(activity);
 		ll.setOrientation(LinearLayout.VERTICAL);
 		
 		Button b1 = new Button(activity);
         b1.setText("End Turn");
         b1.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if(getSelectedCharacter() != null){
 					deselectCharacter(true);
 				}
 				if(getGame().getPlayer().isTurn()){
 					nextTurn();
 				}
 				pausemenu.dismiss();
 				
 			}
         });        
         ll.addView(b1);
 
         Button b2 = new Button(activity);
         b2.setText("Resume");
         b2.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 pausemenu.dismiss();
             }
         });
         ll.addView(b2);
         
         Button b3 = new Button(activity);
         b3.setText("Quit");
         b3.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 pausemenu.dismiss();
                 activity.runOnUiThread(new Runnable() {
             	    @Override
             	    public void run() {
             	    	 createQuitDialog();
               			 
             	    }
             	});
             }
         });
         ll.addView(b3);
         
         pausemenu.setContentView(ll);      
         pausemenu.setCanceledOnTouchOutside(false);
         pausemenu.show();        
 		
 		
 	}
 	
 	private void unitOptions(final AUnit sprite){
 		final Dialog dialog = new Dialog(activity);
 		dialog.setTitle("HP: " + sprite.getHealth() + "  En: "+sprite.getEnergy());
 		LinearLayout ll = new LinearLayout(activity);
 		ll.setOrientation(LinearLayout.VERTICAL);
 		
 		Button b1 = new Button(activity);
         b1.setText("Move");
         b1.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				TMXTile tmxTile = placeSelectionRectangle(sprite);
 				highlightAvailableMoves(tmxTile, sprite, true);
 				dialog.dismiss();
 				
 			}
         });        
         ll.addView(b1);
 
         Button b2 = new Button(activity);
         b2.setText("Attack");
         b2.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	TMXTile tmxTile = placeSelectionRectangle(sprite);
 				highlightAvailableTargets(tmxTile, sprite, false);
 				dialog.dismiss();
                 dialog.dismiss();
             }
         });
         ll.addView(b2);
         
        
         
         Button b3 = new Button(activity);
         b3.setText("Cancel");
         b3.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	
             		deselectCharacter(true);
             	
                 dialog.dismiss();
             }
         });
         ll.addView(b3);
         
         
         
         
         dialog.setContentView(ll);      
         dialog.setCanceledOnTouchOutside(false);
         dialog.show();        
 		
 		
 	}
 	
 	public void textMenu(String text){
 		final Dialog pausemenu = new Dialog(activity);
 		pausemenu.setTitle(text);
 		LinearLayout ll = new LinearLayout(activity);
 		ll.setOrientation(LinearLayout.VERTICAL);
 		
 		Button b1 = new Button(activity);
         b1.setText("Ok");
         b1.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				pausemenu.dismiss();
 				
 			}
         });        
         ll.addView(b1);
 
        
         
         pausemenu.setContentView(ll);      
         pausemenu.setCanceledOnTouchOutside(false);
         pausemenu.show();        
 		
 		
 	}
 	
 	
 	public void createQuitDialog(){
 		final AlertDialog.Builder dia = new AlertDialog.Builder(activity);
 		dia.setTitle("Are you sure you wish to quit the game? All progress will be lost!");
 		dia.setNeutralButton("Yes", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
             	resourcesManager.resetGame();
             	try {
 					JSONObject data = new JSONObject("{\"alert\": \"Game Ended\", \"action\": \"com.testgame.QUIT\"}");
 					 ParsePush push = new ParsePush();
 		             push.setChannel("user_"+resourcesManager.opponentString); 
 		             push.setData(data);
 		             push.sendInBackground();
                 } catch (JSONException e) { 
 					e.printStackTrace();
 				}	
             	quitDialog.dismiss();
             	disposeScene();
 		    	SceneManager.getInstance().loadMenuScene(engine);
             	
             }
         });
 		dia.setNegativeButton("No", new DialogInterface.OnClickListener(){
 			public void onClick(DialogInterface dialog, int whichButton){
 				quitDialog.dismiss();
 			}
 		});
 		
 		quitDialog = dia.create();
 		quitDialog.setCanceledOnTouchOutside(false);
 		quitDialog.show();
 	}
 	
 	
 	public void quitDialog(){
 		final Dialog pausemenu = new Dialog(activity);
 		pausemenu.setTitle("Opponent has quit the game! You win!");
 		LinearLayout ll = new LinearLayout(activity);
 		ll.setOrientation(LinearLayout.VERTICAL);
 		
 		Button b1 = new Button(activity);
         b1.setText("Return to Main Menu");
         b1.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				pausemenu.dismiss();
 				disposeScene();
 		    	SceneManager.getInstance().loadMenuScene(engine);
 		    	resourcesManager.resetGame();
 				
 			}
         });        
         ll.addView(b1);
 
        
         
         pausemenu.setContentView(ll);      
         pausemenu.setCanceledOnTouchOutside(false);
         pausemenu.show();        
 		
 	}
 
 	@Override
 	public void onHomeKeyPressed() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public AGame getGame() {
 		return game;
 	}
 
 	public void setGame(AGame game) {
 		this.game = game;
 	}
 	
 	
 	
 	
 	
 	
 	
 }
