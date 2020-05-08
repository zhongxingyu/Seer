 package com.example.forestgame;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 
 public class ScoresScene extends Scene {
     
     private Sprite background = new Sprite( 0
             				, 0
             				, MainActivity.TEXTURE_WIDTH
             				, MainActivity.TEXTURE_HEIGHT
             				, MainActivity.mainActivity.textureBackground
             				, MainActivity.mainActivity.getVertexBufferObjectManager());
     
     public ScoresScene() {
 	
 	setBackgroundEnabled(true);
 	setBackground(new Background(MainActivity.BACKGROUND_COLOR));
 	attachChild(background);
 	background.registerEntityModifier(MainActivity.SHOW_ALPHA_MODIFIER.deepCopy());
 	background.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_COLOR);
     }
     
     public void show() {
 	ScoresTable scoresTable = new ScoresTable();
	scoresTable.createFile();
	scoresTable.addScores(1000);
	scoresTable.sort();
	scoresTable.save();
 	scoresTable.show();
 	setVisible(true);
 	setIgnoreUpdate(false);
    	background.registerEntityModifier(MainActivity.SHOW_ALPHA_MODIFIER.deepCopy());
     }
     
     public void hide() {
 	
    	setVisible(false);
    	setIgnoreUpdate(true);
    	background.setAlpha(1.0f);
     }
 }
