 package com.wnezros.hunger;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.app.Activity;
 
 public class MainActivity extends Activity {
 
 	GameSurface _gameSurface;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         _gameSurface = new GameSurface(this);
         setContentView(_gameSurface);
         if(!copyAssets()) finish();
     }
     
     @Override
     protected void onPause() {
         super.onPause();
         _gameSurface.onPause();
         GameContext.pause();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         _gameSurface.onResume();
         GameContext.resume();
     }
     
     @Override
     protected void onDestroy() {
     	super.onDestroy();
     	GameContext.close();
     }
     
     public boolean onKeyDown(int keyCode, KeyEvent event) {
     	GameContext.keyDown(keyCode);
     	return super.onKeyDown(keyCode, event);
     }
 
     public boolean onKeyUp(int keyCode, KeyEvent event) {
     	GameContext.keyUp(keyCode);
     	return super.onKeyUp(keyCode, event);
     }
     
     private boolean copyAssets() {
 		String[] files = {
 				"food.tga",
 				"map.tga",
 				"player.tga",
 				"test.map"
 		};
 		
 		final File dataPath = new File(Environment.getExternalStorageDirectory(), 
 				"Android/data/com.wnezros.hunger/files/");
 		if(!dataPath.isDirectory()) {
 			if(dataPath.isFile()) dataPath.delete();
 			dataPath.mkdirs();
 		}
 		
 		for(String filename : files) {
 			Log.d("Hunger", "Copy file " + filename);
 			try {
 				InputStream in = getAssets().open(filename);
 				File outFile = new File(dataPath, filename);
				outFile.createNewFile();
 				OutputStream out = new FileOutputStream(outFile);
 				copyFile(in, out);
 				in.close();
 				out.flush();
 				out.close();
 			} catch(Exception e) {
 				Log.e("Hunger", "Copy error: " + e.getMessage());
 				return false;
 			}       
 		}
 		return true;
     }
     
     boolean _first = true;
     
     public void onBackPressed() {
     	if(_first) {
     		//GameContext.init();
     		_first = false;
     	} else super.onBackPressed();
     }
     
     private void copyFile(InputStream in, OutputStream out) throws IOException {
         byte[] buffer = new byte[1024];
         int read;
         while((read = in.read(buffer)) != -1) {
         	out.write(buffer, 0, read);
         }
     }
 }
