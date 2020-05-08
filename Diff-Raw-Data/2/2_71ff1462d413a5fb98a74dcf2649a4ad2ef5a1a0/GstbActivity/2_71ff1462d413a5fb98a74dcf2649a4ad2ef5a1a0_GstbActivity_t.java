 package com.estudio.cheke.game.gstb;
 /*
  * Created by Estudio Cheke, creative purpose.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import com.estudio.cheke.game.gstb.CanvasSurfaceView;
 import com.estudio.cheke.game.gstb.Mover;
 import com.estudio.cheke.game.gstb.SimpleCanvasRenderer;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.KeyEvent;
 import android.view.MotionEvent;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class GstbActivity extends Activity {
 	public static boolean pause;
 	public static boolean touch=false;
 	CanvasSurfaceView mCanvasSurfaceView;
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		pause=true;
 		SoundManager.initSounds(this);
 		SoundManager.playSound(0, true);
 		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
 		Cache.setDimensions(display.getWidth(), display.getHeight());
 	}
 	public void onStart(){
 		super.onStart();
 		Cache.resource=getResources();
 		mCanvasSurfaceView = new CanvasSurfaceView(this);
 		SimpleCanvasRenderer spriteRenderer = new SimpleCanvasRenderer();
 		Mover simulationRuntime = new Mover();
 		mCanvasSurfaceView.setRenderer(spriteRenderer);
 		mCanvasSurfaceView.setEvent(simulationRuntime);
 		setContentView(mCanvasSurfaceView);
 	}
 	public void onPause(){
 		super.onPause();
 		SoundManager.pause();
 	}
 	public void onResume(){
 		super.onResume();
 		SoundManager.resume();
 	}
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(Cache.loadImagesB){
 			touch=true;
 			int movMin=10;
 			int mov=movMin;
 			boolean x=false;
 			boolean back=false;
 			switch (keyCode) {
 			case KeyEvent.KEYCODE_DPAD_LEFT:
 				mov=-movMin;
 			case KeyEvent.KEYCODE_DPAD_RIGHT:
 				x=true;
 				break;
 			case KeyEvent.KEYCODE_DPAD_UP:
 				mov=-movMin;
 			case KeyEvent.KEYCODE_DPAD_DOWN:
 				x=false;
 				break;
 			case KeyEvent.KEYCODE_BACK:
 				back=true;
 				finish();
 				break;
 			default:
 				return super.onKeyDown(keyCode, event);
 			}
 			if(!back){
 				Cache.canvasArray.moveBolsa(mov, x);
 			}
 		}
 		return true;
 	}
 	public boolean onTouchEvent(MotionEvent event){
 		int action = event.getAction();
 		int x = ((int) event.getX());
 		int y = ((int) event.getY());
 		switch(action){
 		case MotionEvent.ACTION_DOWN:
 			if(pause||Cache.menuFin){//if(menuFin){
 				if(x>Cache.menuleft&&x<Cache.width-Cache.menuleft){
 					if(!Cache.menuFin){
 						if(y>Cache.menutop*2&&y<(Cache.menutop*2)+Cache.menuY){//play
 							pause=false;
 							SoundManager.playSound(1, true);
 						}
 					}
 					if(y>(Cache.menutop*3)+Cache.menuY&&y<(Cache.menutop*3)+(Cache.menuY*2)){//exit
 						finish();
 					}
 				}
 			}
 			break;
 		case MotionEvent.ACTION_MOVE:
 			if(!pause){
 				Cache.control.touched(x, y);
 			}
 			break;
 		}
 		return false;
 	}
 	public void finish(){
 		super.finish();
 		mCanvasSurfaceView.stopDrawing();
 		Cache.setdefault=true;
 		SoundManager.stop();
 		SoundManager.cleanup();
 		System.gc();
 	}
 }
