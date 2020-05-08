 package com.games.androidgames.pang;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import com.games.androidgames.framework.Screen;
 import com.games.androidgames.framework.impl.GLGame;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 
 public class Start extends GLGame {
 
 	boolean firstTimeCreate = true;
 	    
 	public Screen getStartScreen() {
 		Resources.load(this);
 	    return new MainMenuScreen(this);
 	}
 	    
     @Override
     public void onSurfaceCreated(GL10 gl, EGLConfig config) {         
         super.onSurfaceCreated(gl, config);
         //Resources.resetTopTen(this);
        Settings.WORLD_HEIGHT = this.getGLGraphics().getHeight();
        Settings.WORLD_WIDTH = this.getGLGraphics().getWidth();
         Resources.reload();
     }     
     
     @Override
     public void onPause() {
         super.onPause();
        // if(Settings.soundEnabled)
             //Assets.music.pause();
     }
 }
