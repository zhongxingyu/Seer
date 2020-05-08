 package com.mark.games.fallingblocks;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import com.mark.games.fallingblocks.framework.Screen;
 import com.mark.games.fallingblocks.framework.impl.GLGame;
 
 public class FallingBlocksActivity extends GLGame {
 	
 	public boolean firstTime = true;
 	@Override
 	public Screen getStartScreen() {
 		return new MainMenuScreen(this);
 	}
 	
 	@Override
     public void onSurfaceCreated(GL10 gl, EGLConfig config) {         
         super.onSurfaceCreated(gl, config);
        if(firstTime) {
     	   Assets.load(this);
        }
        else {
     	   Assets.reload();
        }
     }     
     
     @Override
     public void onPause() {
         super.onPause();

     }
 }
