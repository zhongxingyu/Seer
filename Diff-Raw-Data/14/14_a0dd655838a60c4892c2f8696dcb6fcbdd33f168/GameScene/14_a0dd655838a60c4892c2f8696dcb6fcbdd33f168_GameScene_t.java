 package com.example.forestgame;
 
 import org.andengine.entity.modifier.AlphaModifier;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.util.color.Color;
 
 public class GameScene extends Scene {
     
    //private SlotMatrix slotMatrix = new SlotMatrix();
     private Sprite sprite = new Sprite(0, 0, MainActivity.TEXTURE_WIDTH, MainActivity.TEXTURE_HEIGHT,
 	    MainActivity.mainActivity.textureBackground, new VertexBufferObjectManager());
     private Sprite slots = new Sprite(0, 0, MainActivity.TEXTURE_WIDTH, MainActivity.TEXTURE_HEIGHT,
 	    MainActivity.mainActivity.textureSlots, new VertexBufferObjectManager());
     
     
 
     public GameScene() {
 	setBackgroundEnabled(true);
 	setBackground(new Background(Color.BLUE));
 	sprite.registerEntityModifier(new AlphaModifier(0.55f, 0.5f, 0.8f));
 	slots.registerEntityModifier(new AlphaModifier(0.4f, 0.5f, 1.0f));
 	attachChild(sprite);
 	attachChild(slots);
     }
     
     public void show() {
 	setVisible(true);
 	setIgnoreUpdate(false);
     }
     
     public void hide() {
    	setVisible(false);
    	setIgnoreUpdate(true);
    	sprite.registerEntityModifier(new AlphaModifier(0.55f, 0.5f, 0.8f));
    	slots.registerEntityModifier(new AlphaModifier(0.4f, 0.5f, 1.0f));
        }
 }
