 package com.example.forestgame;
 
 import java.io.IOException;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import org.andengine.entity.modifier.AlphaModifier;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.input.touch.TouchEvent;
 
 import android.util.Log;
 
 import com.example.forestgame.element.Element;
 import com.example.forestgame.gameinterface.Prison;
 import com.example.forestgame.gameinterface.Respawn;
 
 public class GameScene extends Scene {
     
     private PauseScene pauseScene = new PauseScene();
     private GameOverScene gameOverScene = new GameOverScene();
     
     public SlotMatrix slotMatrix;
     private Prison prison;
     public Respawn respawn;
     private Sprite backlight;
     
     private Element movingElement;
     
     private int putInRow;
     private int putInColum;
     private boolean backlightOn;
     
     private static final float CAGE_POSITION_LEFT = MainActivity.TEXTURE_WIDTH * 136 / 625;
     private static final float CAGE_POSITION_UP = MainActivity.TEXTURE_HEIGHT * 1381 / 2000;
     private static final float CAGE_WIDTH = MainActivity.TEXTURE_WIDTH * 63 / 250;
     private static final float CAGE_HEIGHT = MainActivity.TEXTURE_HEIGHT * 313 / 2000;
     
     private static final float PAUSE_POSITION_LEFT = MainActivity.TEXTURE_WIDTH * 516 / 625;
     private static final float PAUSE_POSITION_UP = MainActivity.TEXTURE_HEIGHT * 1820 / 2000;
     private static final float PAUSE_WIDTH = MainActivity.TEXTURE_WIDTH * 30 / 250;
     private static final float PAUSE_HEIGHT = MainActivity.TEXTURE_HEIGHT * 18 / 250;
     
     private static final int CAGE_Z_INDEX = 9999;
     private static final int PAUSE_SCENE_Z_INDEX = 10000;
     private static final int GAME_OVER_SCENE_Z_INDEX = 10000;
     
     private static final AlphaModifier BACKGROUND_ALPHA_MODIFIER = new AlphaModifier(0.55f, 1.0f, 0.8f);
     private static final AlphaModifier SLOTS_ALPHA_MODIFIER = new AlphaModifier(0.4f, 0.5f, 1.0f);
     
     
     private static final float PRISON_POSITION_LEFT = CAGE_POSITION_LEFT;
     private static final float PRISON_POSITION_UP = CAGE_POSITION_UP;
     private static final float PRISON_POSITION_RIGHT = PRISON_POSITION_LEFT + CAGE_WIDTH;
     private static final float PRISON_POSITION_BOTTOM = PRISON_POSITION_UP + CAGE_HEIGHT;
     private static final float BACKLIGHT_ALPHA = 0.7f;
     private static double OFFSET_ON_MOVING;
     
     private Text scoresText;
     
     private Sprite background = new Sprite( 0
 	                              , 0
 	                              , MainActivity.TEXTURE_WIDTH
 	                              , MainActivity.TEXTURE_HEIGHT
 	                              , MainActivity.mainActivity.textureBackground
 	                              , MainActivity.mainActivity.getVertexBufferObjectManager());
     
     private Sprite slots = new Sprite( 0
 	    			     , 0
 	    			     , MainActivity.TEXTURE_WIDTH
 	    			     , MainActivity.TEXTURE_HEIGHT
 	    			     , MainActivity.mainActivity.textureSlots
 	    			     , MainActivity.mainActivity.getVertexBufferObjectManager());
     
     private Sprite cage = new Sprite( CAGE_POSITION_LEFT
 	        		    , CAGE_POSITION_UP
 	        		    , CAGE_WIDTH
 	        		    , CAGE_HEIGHT
 	        		    , MainActivity.mainActivity.textureCage
 	        		    , MainActivity.mainActivity.getVertexBufferObjectManager());
     
     private Sprite pauseIcon = new Sprite( PAUSE_POSITION_LEFT
 	    				 , PAUSE_POSITION_UP
 	    				 , PAUSE_WIDTH
 	    				 , PAUSE_HEIGHT
 	    				 , MainActivity.mainActivity.texturePauseIcon
 	    				 , MainActivity.mainActivity.getVertexBufferObjectManager()) {
     @Override
     public boolean onAreaTouched( TouchEvent pSceneTouchEvent
 	    			, float pTouchAreaLocalX
 	    			, float pTouchAreaLocalY) {
 	
 	if (pSceneTouchEvent.isActionDown()) {
 	    
 	    this.registerEntityModifier(MainActivity.TOUCH_ALPHA_MODIFIER);
 	} else if (pSceneTouchEvent.isActionUp()) {
 	    
 	    this.registerEntityModifier(MainActivity.UNTOUCH_ALPHA_MODIFIER);
 	    MainActivity.mainActivity.mClick.play();
 	    MainScene.showInGamePause();
 	}
 	return true;
 	}
     };
 
     public GameScene() {
 	
 	setBackgroundEnabled(true);
 	setBackground(new Background(MainActivity.BACKGROUND_COLOR));
 	background.registerEntityModifier(BACKGROUND_ALPHA_MODIFIER.deepCopy());
 	background.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_COLOR);
 	slots.registerEntityModifier(SLOTS_ALPHA_MODIFIER.deepCopy());
 	attachChild(background);
 	attachChild(slots);
 	attachChild(cage);
 	attachChild(pauseIcon);
 	registerTouchArea(pauseIcon);
 	
 	prison = new Prison(this);
 	respawn = new Respawn(this);
 	slotMatrix = new SlotMatrix(this);
 
 	attachChild(pauseScene);
 	attachChild(gameOverScene);
 	pauseScene.hide();
 	gameOverScene.hide();
 	
 	cage.setZIndex(CAGE_Z_INDEX);
 	pauseScene.setZIndex(PAUSE_SCENE_Z_INDEX);
 	gameOverScene.setZIndex(GAME_OVER_SCENE_Z_INDEX);
 	
 	//here the test for tablet/phone is needed
 	if (MainActivity.mainActivity.hasLargeScreen()) {
 	    OFFSET_ON_MOVING = 0.2;
 	} else {
 	    OFFSET_ON_MOVING = 0.7;
 	}
     }
     
     public double getOffsetCoef() {
 	return OFFSET_ON_MOVING;
     }
     
     public void show() {
 	
 	setVisible(true);
 	setIgnoreUpdate(false);
 	MainActivity.mainActivity.mMusic.pause();
 	MainActivity.mainActivity.mGameStart.play();
    	background.registerEntityModifier(BACKGROUND_ALPHA_MODIFIER.deepCopy());
    	slots.registerEntityModifier(SLOTS_ALPHA_MODIFIER.deepCopy());
     }
     
     public void hide() {
 	
    	setVisible(false);
    	setIgnoreUpdate(true);
    	background.setAlpha(1.0f);
    	slots.setAlpha(0.5f);
     }
     
     public SlotMatrix getSlotMatrix() {
 	
 	return slotMatrix;
     }
     
     public Prison getPrison() {
 	return prison;
     }
     
     public Respawn getRespawn() {
 	return respawn;
     }
     
     public PauseScene getPauseScene() {
 	return pauseScene;
     }
     
     public GameOverScene getGameOverScene() {
 	return gameOverScene;
     }
     
     public Element getMovingElement() {
 	return movingElement;
     }
 
     public void moveElement(float touchPointX, float touchPointY) {
 	
 	 for (int i = 0; i < SlotMatrix.getROWS(); i++) {
 	     
 	     boolean flg=false;
 	     for (int j = 0; j < SlotMatrix.getCOLUMNS(); j++) {
 		   
 		 flg=true;
 		 float slotLeftBorder = SlotMatrix.getSlotPositionLeft(j);
 		 float slotUpperBorder = SlotMatrix.getSlotPositionUp(i);
 		 float slotRightBorder = slotLeftBorder + SlotMatrix.getSlotWidth();
 		 float slotBottomBorder = slotUpperBorder + SlotMatrix.getSlotHeight();
 		 
 		 if (slotLeftBorder <= touchPointX && touchPointX <= slotRightBorder && 
 		     slotUpperBorder <= touchPointY && touchPointY <= slotBottomBorder) {
 		
 		     Log.d("slot x ",Integer.toString(j));
 		     Log.d("slot y ",Integer.toString(i));
 		     putInRow = i;
 		     putInColum = j;
 		     slotBacklight(i,j);
 		     break;
 		     
 		 } else if (PRISON_POSITION_LEFT <= touchPointX && touchPointX <= PRISON_POSITION_RIGHT && 
 			    PRISON_POSITION_UP <= touchPointY && touchPointY <= PRISON_POSITION_BOTTOM) {
 			
 		     Log.d("slotPrison x ",Integer.toString(7));
 		     Log.d("slotPrison y ",Integer.toString(7));
 		     putInRow = SlotMatrix.getPrisonPlaceRow();
 		     putInColum = SlotMatrix.getPrisonPlaceRow();
 		     putInRow = SlotMatrix.getROWS()+1;
 		     putInColum = SlotMatrix.getCOLUMNS()+1;
 		     if (backlightOn) {
 			 
 			 detachChild(backlight);
 		     }
 		     break;
 		     
 		 } else {
 		     
 		     putInRow = SlotMatrix.getMilkPointRow();
 		     putInColum = SlotMatrix.getMilkPointColumn();
 		     flg=false;
 		     if (backlightOn) {
 			 
 			 detachChild(backlight);
 		     }
 		 }
 	     }
 	     if (flg) {
 		 
 		break;
 	     }
 	 }
     }
     
     public void slotBacklight(int i, int j) {
 	
 	if (backlightOn) {
 	    
 	    detachChild(backlight);
 	}
 	
 	if (i < SlotMatrix.getROWS() && j < SlotMatrix.getCOLUMNS() && slotMatrix.isSlotEmpty(i, j)) {
 	    
 	    backlight = new Sprite(SlotMatrix.getSlotPositionLeft(j)
                 	    	   , SlotMatrix.getSlotPositionUp(i)
                 	    	   , SlotMatrix.getSlotWidth()
                 	    	   , SlotMatrix.getSlotHeight()
                 	    	   , MainActivity.mainActivity.storage.getTexture("gfx_empty.png")
                 	    	   , MainActivity.mainActivity.getVertexBufferObjectManager());
      
             backlight.setAlpha(BACKLIGHT_ALPHA);
 	    attachChild(backlight);
 	    backlight.getParent().sortChildren();
 	    backlightOn = true;
 	}
     }
     
     public int getPutInRow() {
 	
 	return putInRow;
     }  
     
     public int getPutInColum() {
 	
 	return putInColum;
     } 
     
     public void setScores(int scores) {
 	
 	Log.d("scores",""+scores);
 	detachChild(scoresText);
 	scoresText=null;
 	
 	scoresText= new Text(MainActivity.TEXTURE_WIDTH * 1 / 6
 			     , MainActivity.TEXTURE_HEIGHT / 200
 			     , MainActivity.mainActivity.tScores
 			     , Integer.toString(scores)
 			     , MainActivity.mainActivity.getVertexBufferObjectManager());
 	
 	attachChild(scoresText);
     }
 
     public boolean isBacklightOn() {
 	
         return backlightOn;
     }
 
     public void setBacklightOn(boolean backlight) {
 	
         this.backlightOn = backlight;
     }
 
     public Sprite getBacklight() {
 	
         return backlight;
     }
 
     public String savePrisonName() {
 	
 	if (prison.isEmpty()) return null;
 	
 	return prison.getElement().getName();
     }
     
     public String saveRespawnName() {
 	
 	if (respawn.isEmpty()) return null;
 	
 	return respawn.getElement().getName();
     }
     
     public void setSavedGame() throws IOException {
 	
 	slotMatrix.loadInit();
 	prison.clear();
 	respawn.clear();
 	
 	if (MainActivity.mainActivity.loadPrison() != null) {
 	    
 	    prison.addElement(new Element(MainActivity.mainActivity.loadPrison()));
 	}
 	
 	if (MainActivity.mainActivity.loadRespawn() != null) {
 	    
 	    respawn.addElement(new Element(MainActivity.mainActivity.loadRespawn())); 
 	}
     }
 }
