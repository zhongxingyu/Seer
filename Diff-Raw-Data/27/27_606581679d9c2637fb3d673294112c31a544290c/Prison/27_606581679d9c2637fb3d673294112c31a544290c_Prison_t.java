 package com.example.forestgame.gameinterface;
 
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.input.touch.TouchEvent;
 
 import android.util.Log;
 
 import com.example.forestgame.GameScene;
 import com.example.forestgame.MainActivity;
 import com.example.forestgame.SlotMatrix;
 import com.example.forestgame.element.Element;
 import com.example.forestgame.element.TableOfElements;
 
 public class Prison extends GameSlot {
     
     private final static float PRISON_POSITION_LEFT = MainActivity.TEXTURE_WIDTH * 136 / 625;
     private final static float PRISON_POSITION_UP = MainActivity.TEXTURE_HEIGHT * 1381 / 2000;
     private final static float PRISON_WIDTH = MainActivity.TEXTURE_WIDTH * 61 / 250;
     private final static float PRISON_HEIGHT = MainActivity.TEXTURE_HEIGHT * 303 / 2000;
     private static final int PRISON_Z_INDEX = 401;
     
     public Prison(GameScene scene) {
 	
 	super(scene);
     }
     
     protected void backToGameSlot(Element element) {
 	
 	slotSprite.setPosition( PRISON_POSITION_LEFT
     		                , PRISON_POSITION_UP);
 	
     }
     
     protected void show() {
 	
 	if(!isEmpty) {
 	    
 	    slotTexture = MainActivity.mainActivity.storage.getTexture(TableOfElements.getTextureName(element));
 	    slotSprite = new Sprite ( PRISON_POSITION_LEFT
 		    		      , PRISON_POSITION_UP
 		    		      , PRISON_WIDTH
 		    		      , PRISON_HEIGHT
 		    		      , slotTexture
 		    		      , MainActivity.mainActivity.getVertexBufferObjectManager()) {
 		
 		@Override
 		public boolean onAreaTouched( TouchEvent pSceneTouchEvent
 			    			, float pTouchAreaLocalX
 			    			, float pTouchAreaLocalY) {
 		    if (pSceneTouchEvent.isActionDown()) {
 			
 			gameSlotIsActionDown();
 			
 		    } else if (pSceneTouchEvent.isActionUp()) {
 			    
 			gameSlotIsActionUp();
 			    
 		    } else if (pSceneTouchEvent.isActionMove()) {
 			    
 			gameSlotIsActionMove(pSceneTouchEvent);
 			
 		    }
 		    return true;
 		}
 	    };
 	
 	    gameSlotAttach(PRISON_Z_INDEX);
 	    
 	} else {
 	    
 	    gameSlotDetach();
 	}
     }
     
     protected void gameSlotIsActionDown() {
 	
 	Log.d("prison", "touch");
 	row = SlotMatrix.getPrisonPlaceRow();
 	column = SlotMatrix.getPrisonPlaceColumn();
     }
     
     protected void gameSlotIsActionUp() {
 	
	if (gameScene.isBacklightOn()) {
	    
	    gameScene.detachChild(gameScene.getBacklight());
	    gameScene.setBacklightOn(false);
	}
	
 	if (row < SlotMatrix.getROWS() && column <SlotMatrix.getCOLUMNS() 
 		&& gameScene.getSlotMatrix().isSlotEmpty(row, column)) {
 		
 	    Log.d("prison", "new");
 	    gameScene.getSlotMatrix().putToSlot(element, row, column);
 	    clear();
 	    
 	} else {
 		
 	    Log.d("prison","nowhere");
 	    backToGameSlot(element);
 	}
     }
     
     protected void gameSlotIsActionMove(TouchEvent pSceneTouchEvent) {
 	
 	Log.d("prison", "move");
 	    
 	float spriteLeftBorder = pSceneTouchEvent.getX() - slotSprite.getWidth() / 2;
 	float verticalOffset = (float)(slotSprite.getHeight() * gameScene.getOffsetCoef());
 	float spriteUpBorder = pSceneTouchEvent.getY() - slotSprite.getHeight() / 2 - verticalOffset;
 	
 	slotSprite.setPosition(spriteLeftBorder, spriteUpBorder);
 	      
 	gameScene.moveElement(pSceneTouchEvent.getX(), pSceneTouchEvent.getY() - verticalOffset);
 	column = gameScene.getPutInColum();
 	row = gameScene.getPutInRow(); 
 	
 	Log.d("prison", Integer.toString(row));
 	Log.d("prison", Integer.toString(column));
     }
 }
