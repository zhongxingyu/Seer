 package com.testgame.scene;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.andengine.entity.IEntity;
 import org.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
 import org.andengine.entity.modifier.MoveModifier;
 
 
 import org.andengine.engine.camera.BoundCamera;
 import org.andengine.engine.camera.SmoothCamera;
 import org.andengine.engine.camera.hud.HUD;
 import org.andengine.entity.primitive.Line;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.sprite.ButtonSprite;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.AutoWrap;
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
 import org.andengine.util.adt.color.Color;
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
 import com.testgame.AGame;
 import com.testgame.LocalGame;
 import com.testgame.OnlineGame;
 import com.testgame.mechanics.map.GameMap;
 import com.testgame.mechanics.unit.AUnit;
 import com.testgame.player.APlayer;
 import com.testgame.player.ComputerPlayer;
 import com.testgame.scene.SceneManager.SceneType;
 import com.testgame.sprite.CharacterSprite;
 import com.testgame.sprite.HighlightedSquare;
 
 public class GameScene extends BaseScene implements IOnSceneTouchListener, IPinchZoomDetectorListener {
 
 	public final static int SPRITE_MODE = 0;
 	public final static int HEALTH_MODE = 1;
 	public final static int ENERGY_MODE = 2;
 	
 	public int mode;
 	
 	public boolean working = false;
 
 	private float mTouchX = 0, mTouchY = 0, mTouchOffsetX = 0, mTouchOffsetY = 0;
 	
 	private Rectangle currentTileRectangle;
 	private AUnit selectedCharacter;
 	
 	private static float ZOOM_FACTOR_MIN = .5f;
 	private static float ZOOM_FACTOR_MAX = 1.5f;
 	
 	public IEntityModifierListener animationListener;
 	//public IEntityModifierListener computerAnimationListener;
 	
 	public AGame game;
 	
 	public HUD hud;
 	
 	public boolean animating;
 	
 	public int tileSize;
 	public int widthInTiles;
 	public int heightInTiles;
 	
 	private ArrayList<AUnit> targets = new ArrayList<AUnit>();
 	
 	private ButtonSprite pauseButton;
 	private ButtonSprite tutorialButton;
 	private ButtonSprite healthModeButton, energyModeButton;
 	
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
 	
 	@Override
 	public void createScene() { // will be passed player names and some game state representation later
 		// Set up touch.
 		this.setOnSceneTouchListener(this);
 		this.engine.setTouchController(new MultiTouchController());
 		this.mPinchZoomDetector = new PinchZoomDetector(this);
 		
 		this.mode = SPRITE_MODE;
 		
 		this.animationListener = new IEntityModifierListener() {
 			@Override
 			public void onModifierStarted(IModifier<IEntity> pModifier,
 					IEntity pItem) {
 				Log.d("AndEngine", "animation modifier started.");
 				animating = true;
 				camera.setChaseEntity(pItem);
 				resourcesManager.walking_sound.play();
 			}
 			@Override
 			public void onModifierFinished(IModifier<IEntity> pModifier,
 					IEntity pItem) {
 				Log.d("AndEngine", "animation modifier ended.");
 				animating = false;
 				camera.setChaseEntity(null);
 				((AUnit)pItem).setCurrentTileIndex(((AUnit)pItem).start_frame);
 				resourcesManager.walking_sound.pause();
 				//pItem.clearEntityModifiers(); // perhaps gets stops repeating footsteps.
 			}
 		};
 		
 		//Log.d("AndEngine", "" + resourcesManager.selectedMap.getTMXLayers().size());
 		
 
 		this.tmxLayer = resourcesManager.tiledMap.getTMXLayers().get(0);  
 
 		
 		this.tileSize = resourcesManager.tiledMap.getTileHeight();
 		this.heightInTiles = resourcesManager.tiledMap.getTileRows();
 		this.widthInTiles = resourcesManager.tiledMap.getTileColumns();
 		
 		attachChild(resourcesManager.tiledMap);
 		resourcesManager.tiledMap.setOffsetCenter(0, 0);
 		
 		// Initialize highlighted squares list.
 		this.highlightedSquares = new ArrayList<HighlightedSquare>();
 		
 		// Edit camera options.
 		((BoundCamera)camera).setBounds(0, 0, resourcesManager.tiledMap.getWidth(), resourcesManager.tiledMap.getHeight());
 		((BoundCamera)camera).setBoundsEnabled(true);
 		camera.setCenter(0, 0);
 		
 		// Initialize the game.
 		if(!resourcesManager.isLocal){
 			this.setGame(new OnlineGame(new APlayer("Your"), new ComputerPlayer("Opponent's"), widthInTiles, heightInTiles, this, resourcesManager.turn));
 			((OnlineGame)this.getGame()).getCompPlayer().setGame(((OnlineGame)this.getGame()));
 		}
 		else{
 			this.setGame(new LocalGame(new APlayer("One's"), new APlayer("Two's"), widthInTiles, heightInTiles, this));
 		}
 		
 		createHUD();
 	    
 		// Initialize selection rectangle.
 		this.currentTileRectangle = new Rectangle(0, 0, resourcesManager.tiledMap.getTileWidth(), resourcesManager.tiledMap.getTileHeight(), vbom);
 		currentTileRectangle.setOffsetCenter(0, 0);
 		currentTileRectangle.setColor(1, 0, 0, 0);
 		attachChild(currentTileRectangle);
 		if(!resourcesManager.isLocal){
 			startCompTurn();
 		}
 		
 	}
 	
 	protected void createHUD() {
 		
 		this.hud = new HUD();
 		
 		hud.attachChild(new Sprite(240, 790, resourcesManager.top_bar, vbom));
 		
 		hud.attachChild(bottomBar = new Sprite(240, -300, resourcesManager.bottom_bar, vbom));
 		
 		// Create the game events messages.
 		this.eventsMessage = new Text(240, 760, resourcesManager.cartoon_font_white, "Destroy All Enemy Units\n to Win!", 200, new TextOptions(AutoWrap.LETTERS, 480 - 260, HorizontalAlign.CENTER), vbom);
 		this.endGameMessage = new Text(240, 400, resourcesManager.font, "", 50, new TextOptions(HorizontalAlign.CENTER), vbom);
 		
 		// Initialize HUD and its entities.
 		this.curUnitAttack = new Text(300, 175, resourcesManager.cartoon_font_white, "Attack: " , 75, new TextOptions(HorizontalAlign.LEFT), vbom);
 		this.curUnitAttack.setOffsetCenter(0, 0);
 		this.curUnitEnergy = new Text(50, 250, resourcesManager.cartoon_font_white, "Energy: ", 25, new TextOptions(HorizontalAlign.LEFT), vbom);
 		this.curUnitEnergy.setOffsetCenter(0,0);
 		this.curUnitHealth = new Text(50, 200, resourcesManager.cartoon_font_white, "Health: " , 25, new TextOptions(HorizontalAlign.LEFT), vbom);
 		this.curUnitHealth.setOffsetCenter(0, 0);
 		
 		bottomBar.attachChild(curUnitAttack);
 		bottomBar.attachChild(curUnitEnergy);
 		bottomBar.attachChild(curUnitHealth);
 		
 		final GameScene game = this;
 		
 		tutorialButton = new ButtonSprite(480 - 40, 800 - 40, resourcesManager.gear_region, vbom, new OnClickListener() {
 
 			@Override
 			public void onClick(ButtonSprite pButtonSprite,float pTouchAreaLocalX, float pTouchAreaLocalY) {
 				
 				if (animating) return;
 				
 				resourcesManager.select_sound.play();
 				
 				Log.d("AndEngine", "launching tutorial scene");
 
 				OldX = camera.getCenterX();
 				OldY = camera.getCenterY();
 				//deselectCharacter(true);
 				SceneManager.getInstance().previousScene = game.getSceneType();
 				SceneManager.getInstance().loadTutorialScene(game.engine);
 			}
 		});
 
 		pauseButton = new ButtonSprite(40, 760, resourcesManager.pause_region, vbom, new OnClickListener(){
             
             @Override
             public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX,
                             float pTouchAreaLocalY) {
             	
             	if (animating) return;
             	
             	resourcesManager.select_sound.play();
             	
             	activity.runOnUiThread(new Runnable() {
 	        	    @Override
 	        	    public void run() {
 	        	    	 pauseMenu();
 	          			 
 	        	    }
 	        	});
             				
             }
 		});
 
 		healthModeButton = new ButtonSprite(110, 760, resourcesManager.red_button, vbom, new OnClickListener() {
 
 			@Override
 			public void onClick(ButtonSprite pButtonSprite,
 					float pTouchAreaLocalX, float pTouchAreaLocalY) {
 				
 				if (animating) return;
 				
 				resourcesManager.select_sound.play();
 				
 				deselectCharacter(true);
 				
 				if (mode == HEALTH_MODE) switchMode(SPRITE_MODE);
 				else switchMode(HEALTH_MODE);
 				
 			}});
 		
 		energyModeButton = new ButtonSprite(480 - 110, 760, resourcesManager.blue_button, vbom, new OnClickListener() {
 
 			@Override
 			public void onClick(ButtonSprite pButtonSprite,
 					float pTouchAreaLocalX, float pTouchAreaLocalY) {
 				
 				if (animating) return;
 				
 				resourcesManager.select_sound.play();
 				
 				deselectCharacter(true);
 			
 				if (mode == ENERGY_MODE) switchMode(SPRITE_MODE);
 				else switchMode(ENERGY_MODE);
 				
 			}});
 	
 	    //hud.attachChild(turnMessage);
 	    //hud.attachChild(nextTurnButton);
 	    //hud.registerTouchArea(nextTurnButton);
 	    //hud.attachChild(eventsMessage);
 	    hud.attachChild(tutorialButton);
 		hud.registerTouchArea(tutorialButton);
 		
 		hud.attachChild(healthModeButton);
 		hud.registerTouchArea(healthModeButton);
 		
 		hud.attachChild(energyModeButton);
 		hud.registerTouchArea(energyModeButton);
 		
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
 		
 		if (this.selectedCharacter == sprite) { 
 			
 			
 			
 			this.deselectCharacter(true);
 			return;
 			
 		} else {
 			
 			
 			this.setSelectedCharacter((AUnit) sprite);
 			
 			highlightAvailableTargets(sprite);
 			highlightAvailableMoves(sprite);
 			working = false;
 			return;
 		}
 	}
 	
 	public void highlightAvailableTargets(CharacterSprite sprite) {
 				
 		targets = ((AUnit)sprite).availableTargets();
 		
 		for (AUnit target: targets){
 		
 			int x = (int) target.getX();
 			int y = (int) target.getY();
 		
 			TMXTile t = this.tmxLayer.getTMXTileAt(x, y);
 				
 			HighlightedSquare availableMove = new HighlightedSquare(t, x, y, tileSize, this, getSelectedCharacter());
 			
 			this.highlightedSquares.add(availableMove);
 			availableMove.setOffsetCenter(0, 0);
 			
 			availableMove.setColor(1, 0, 0, .5f); // attacks take constant energy, no point in shading
 			attachChild(availableMove);
 			
 		}
 	}
 	
 	/**
 	 * Highlights touched unit's available spaces to move.
 	 * @param startTile Tile the unit is on.
 	 * @param sprite The unit itself.
 	 */
 	//TODO: Clean UP Code here for attack and moves.
 	public void highlightAvailableMoves(CharacterSprite sprite) {
 
 		ArrayList<Point> moves = ((AUnit)sprite).availableMoves();
 		
 		for (Point p : moves){
 			
 			int x = (int) p.x*tileSize;
 			int y = (int) p.y*tileSize;
 			
 			TMXTile t = this.tmxLayer.getTMXTileAt(x, y);
 			
 						
 			HighlightedSquare availableMove = new HighlightedSquare(t, x, y, tileSize, this, getSelectedCharacter());
 			
 			this.highlightedSquares.add(availableMove);
 			availableMove.setOffsetCenter(0, 0);
 			
 			float blueValue;
 			
 			if (selectedCharacter == null ) { // checks because of that weird occasional null pointer exception
 				
 				blueValue = 1;
 			}
 			else if(t == null){
 				
 				blueValue = 1;
 			}
 			
 			else {
 				blueValue = 1.0f/(selectedCharacter.getEnergy()/selectedCharacter.getRange()) * selectedCharacter.manhattanDistance(selectedCharacter.getMapX(), selectedCharacter.getMapY(), t.getTileColumn(), heightInTiles - t.getTileRow() - 1) /2 + .1f;
 			}
 			availableMove.setColor(0, 0, 1, blueValue);
 			attachChild(availableMove);
 			this.registerTouchArea(availableMove);
 		}
 	}
 
 	public void clearSquares() {
 		if(highlightedSquares == null)
 			return;
 		for (final HighlightedSquare h : this.highlightedSquares) {
 			if (h.unit != null) h.unit.inSelectedCharactersAttackRange = false;
 			final GameScene game = this;
 			engine.runOnUpdateThread(new Runnable() {
 				@Override
 				public void run() {
 					game.unregisterTouchArea(h);
 					game.detachChild(h);
 				}});
 			
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
 					
 					getSelectedCharacter().move(x, heightInTiles - y - 1, sq.path, sq.cost);
 				}
 			}
 			
 			removePath();
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
 		
 		if (this.hud.getChildScene() != null) return false; // tutorial menu is up, don't move around!
 		
 		if (animating) return false; // If we're moving, don't recognize touch
 		
 		this.mPinchZoomDetector.onTouchEvent(pTouchEvent);
 		
 		if (this.mPinchZoomDetector.isZooming()) return false; // do the zoom
 		
 		if(pTouchEvent.getAction() == MotionEvent.ACTION_DOWN)
 		{		
 			mTouchX = pTouchEvent.getMotionEvent().getX();
             mTouchY = pTouchEvent.getMotionEvent().getY();
             
             pTouchEvent.getX();
             pTouchEvent.getY();
 
             return false;
 			
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
 
             return true;
         }
         
         return false;
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
     
 
 	public AUnit getSelectedCharacter() {
 		return selectedCharacter;
 	}
 
 	public void setSelectedCharacter(AUnit selectedCharacter) {
 		
 		
 		
 		if (this.selectedCharacter != null)  deselectCharacter(false);
 		else showBar();
 		this.selectedCharacter = selectedCharacter;
 		placeSelectionRectangle(selectedCharacter);
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
 		
 		
 		
 		if (this.selectedCharacter == null) return;
 		
 		if (andHideBar) hideBar();
 		
 		((AUnit)this.selectedCharacter).setCurrentTileIndex(((AUnit)this.selectedCharacter).start_frame);
 		
 		this.selectedCharacter.stopAnimation();
 		this.selectedCharacter = null;
 		
 		removePath();
 		
 		clearSquares();
 		
 		for (AUnit target: targets){
 			target.inSelectedCharactersAttackRange = false;
 		}
 		
 		this.currentTileRectangle.setColor(1,0,0,0);
 		working = false;
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
 	
 	public void startCompTurn(){
 		
 		ParseQuery query = new ParseQuery("Turns");
 		
 		query.whereEqualTo("Player", "user_"+resourcesManager.opponentString+"_"+getGame().getCount());
 		query.findInBackground(new FindCallback() {
 		    public void done(List<ParseObject> itemList, ParseException e) {
 		        if (e == null) {
 		           Log.d("Items", itemList.size()+""); 
 		            for(ParseObject ob : itemList){
 		            	
 		            	if (ob.getString("Device").equals(resourcesManager.opponentDeviceID)) {
 			            	
 			            	if(ob.getString("GameId").equals(resourcesManager.gameId)){
 			            		if(getGame().getCount() != 0){
 			            			Log.d("Turns", "Found turn data");
 					        		JSONArray array = ob.getJSONArray("Moves");
 					        		Log.d("Array", array.toString());
 					        		deselectCharacter(false);
 					            	((OnlineGame)getGame()).getCompPlayer().startTurn( array);
 					            	ob.deleteInBackground();
 					            	return;
 					        	}
 					        	else{
 					        		JSONObject object = ob.getJSONObject("Init");
 					        		
 					        		deselectCharacter(false);
 					            	((OnlineGame)getGame()).getCompPlayer().init(object);
 					            	ob.deleteInBackground();
 					            	return;
 					        		
 					        	}
 			            	}
 			            	ob.deleteInBackground();
 		            	} 
 		            }
 		            startCompTurn();   
 		        } 
 		    }
 		});
 		
 	}
 	
 	private void pauseMenu(){
 		final Dialog pausemenu = new Dialog(activity);
 		deselectCharacter(true);
 		pausemenu.setTitle("Paused! Turn: "+getGame().getCount());
 		LinearLayout ll = new LinearLayout(activity);
 		ll.setOrientation(LinearLayout.VERTICAL);
 		
 		
 		
 		Button b0 = new Button(activity);
         b0.setText("Refresh");
         b0.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				startCompTurn();
 				pausemenu.dismiss();
 				
 			}
         });        
         ll.addView(b0);
 		
 		
 		Button b1 = new Button(activity);
         b1.setText("End Turn");
         b1.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				getGame().nextTurn();
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
 
 	public void textMenu(String text){
 		
 		
 		
		/*ButtonSprite okButton = new ButtonSprite(0, 0, resourcesManager.continue_region, vbom, new OnClickListener() {
			@Override
			public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				gameScene.dismissDialog();
			}
		});*/
 		
		//this.setChildScene(new GameDialogBox(this, "Your Turn!", ((ButtonSprite[]) null)), false, true, true);
 		
 		
 		
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
				game.endGame();
 				
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
             	if(!resourcesManager.isLocal){
 	            	try {
 						JSONObject data = new JSONObject("{\"alert\": \"Game Ended\", \"action\": \"com.testgame.QUIT\",  \"gameId\": \""+resourcesManager.gameId+"\"}");
 						 ParsePush push = new ParsePush();
 			             push.setChannel("user_"+resourcesManager.opponentString); 
 			             push.setData(data);
 			             push.sendInBackground();
 	                } catch (JSONException e) { 
 						e.printStackTrace();
 					}	
             	}
             	quitDialog.dismiss();
             	disposeScene();
             	resourcesManager.resetGame();
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
 	
 	public void quitDialog(String Text){
 		final Dialog pausemenu = new Dialog(activity);
 		pausemenu.setTitle(Text);
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
 	
 	public void switchMode(int newMode) {
 		// go through all characters...
 		
 		if (this.mode == newMode) return; 
 		
 		for (AUnit u : game.getPlayer().getUnits()) {
 			u.switchMode(newMode);
 		}
 		
 		if (resourcesManager.isLocal) {
 			
 			for (AUnit u2 : ((LocalGame)game).getOtherPlayer().getUnits()) {
 				u2.switchMode(newMode);
 			}
 			
 		} else {
 			for (AUnit u2 : ((OnlineGame)game).getCompPlayer().getUnits()) {
 				u2.switchMode(newMode);
 			}
 		}
 		
 		this.mode = newMode;
 	}
 
 	private Line[] arrowPath;
 	
 	public void drawPath(ArrayList<Point> path) {
 		Log.d("AndEngine", "drawing path...");
 		
 		arrowPath = new Line[path.size()-1];
 	
 		// TODO: make the line actually pretty
 		
 		for (int i = 0; i < path.size() - 1; i++) {
 			Point a = path.get(i);
 			Point b = path.get(i+1);
 			
 			arrowPath[i] = new Line(a.x*tileSize + 32, a.y*tileSize + 32, b.x*tileSize + 32, b.y*tileSize + 32, 20, vbom);
 		}
 		
 		for (Line l : arrowPath) {
 			l.setColor(Color.BLUE);
 			this.attachChild(l);
 		}
 	}
 	
 	public void removePath() {
 		Log.d("AndEngine", "removing path.");
 		
 		if (arrowPath == null) return;
 		for (Line l : arrowPath) {
 			detachChild(l);
 		}
 	}
 	
 	public int costOfPath(ArrayList<Point> path) {
 		int cost = 0;
 		for (int i = 0; i < path.size() - 1; i++){
 			Point a = path.get(i);
 			Point b = path.get(i+1);
 			cost += GameMap.manhattanDistance(a, b);
 		}
 		return cost;
 	}
 }
