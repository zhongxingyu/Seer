 package com.example.forestgame.gameinterface;
 
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.texture.region.TextureRegion;
 
 import android.util.Log;
 
 import com.example.forestgame.GameScene;
 import com.example.forestgame.MainActivity;
 import com.example.forestgame.SlotMatrix;
 import com.example.forestgame.element.Element;
 import com.example.forestgame.element.TableOfElements;
 
 public class Respawn {
 
     private boolean isEmpty;
     private Element element;
     private GameScene gameScene;
     
     TextureRegion respawnTexture;
     Sprite respawnSprite;
     
     TextureRegion movingTexture;
     Sprite movingSprite;
     
     public Respawn(GameScene scene) {
 	
 	gameScene = scene;
 	generateElement();
     }
     
     public void generateElement() {
 	
 	element = TableOfElements.getRandomElement();
 	isEmpty = false;
 	show();
     }
     
     public boolean isEmpty() {
 	
 	return isEmpty;
     }
     
     public Element getElement() {
 	
 	return element;
     }
     
     public void setElement(Element e)
     {
 	element = e;
     }
     
     public void clear() {
 	
 	element = null;
 	isEmpty = true;
 	show();
     }
     
     //For Animation
     public void backToRespawn(Element e)
     {
 	respawnSprite.setPosition(MainActivity.TEXTURE_WIDTH * 27 / 50, MainActivity.TEXTURE_HEIGHT * 1381 / 2000);
 
     }
     
     public void show() {
 	
 	if(!isEmpty) {
 	    respawnTexture = MainActivity.mainActivity.storage.getTexture(TableOfElements
 			    								    . getTextureName
 			    								    ( element));
 	    respawnSprite = new Sprite ( MainActivity.TEXTURE_WIDTH * 27 / 50
 		    			      , MainActivity.TEXTURE_HEIGHT * 1381 / 2000
 		    			      , MainActivity.TEXTURE_WIDTH * 61 / 250
 		    			      , MainActivity.TEXTURE_HEIGHT * 303 / 2000
 		    			      , respawnTexture
 		    			      , MainActivity.mainActivity.getVertexBufferObjectManager()){
 		int row = 8;
 		int colum = 8;
 		    @Override
 		    public boolean onAreaTouched( TouchEvent pSceneTouchEvent
 			    			, float pTouchAreaLocalX
 			    			, float pTouchAreaLocalY) {
 
 			if (pSceneTouchEvent.isActionDown()) {
 			    row = 8;
 			    colum = 8;
 			    Log.d("resp", "touch");
 			    Log.d("resp", Integer.toString(row));
 			    Log.d("resp", Integer.toString(colum));
 			    
 			} else if (pSceneTouchEvent.isActionUp()) {
 			    
 			    Log.d("resp", "no touch");
 			    Log.d("resp", Integer.toString(row));
 			    Log.d("resp", Integer.toString(colum));
 			
 			    if (colum == 7 && row  == 7 && gameScene.prison.isEmpty()) {
 				Log.d("resp", "newprison");
 				gameScene.prison.addElement(element);
 				clear();
 				generateElement();
 			    } 
 			    else if (row < 6 && colum < 6 && gameScene.getSlotMatrix().isSlotEmpty(row, colum)){
 				Log.d("resp", "newSlot");
 				gameScene.getSlotMatrix().putToSlot(element, row, colum);
 				clear();
 				generateElement();
 			    }
 			    else {
 				Log.d("resp", Integer.toString(row));
 				Log.d("resp", Integer.toString(colum));
 				Log.d("resp","nowhere");
 				backToRespawn(element);
 			    }
 			    
 			} else if (pSceneTouchEvent.isActionMove()) {
 			    Log.d("resp", "move");
 			    
 			    float touchX = pSceneTouchEvent.getX() - this.getWidth() / 2;
 			    float touchY = pSceneTouchEvent.getY() - this.getHeight() / 2;
 			    this.setPosition(touchX, touchY - this.getHeight() / 2);
 			  
 			    gameScene.moveElement(touchX, touchY);
 			    colum = gameScene.getPutInColum();
 			    row = gameScene.getPutInRow(); 
 			    Log.d("resp", Integer.toString(row));
 			    Log.d("resp", Integer.toString(colum));
 			}
 			return true;
 		    }
 		};
 	    gameScene.attachChild(respawnSprite);
 	    gameScene.registerTouchArea(respawnSprite);
 	    gameScene.setTouchAreaBindingOnActionDownEnabled(true);
 	    gameScene.setTouchAreaBindingOnActionMoveEnabled(true);
 	    
	    respawnSprite.setZIndex(400);
 	    respawnSprite.getParent().sortChildren();
 	}
 	else gameScene.detachChild(respawnSprite);
     }
 }
