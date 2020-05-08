 package com.me.Battleships;
 
 import Core.Main;
 import android.os.Bundle;
 
 import com.badlogic.gdx.backends.android.AndroidApplication;
 import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
 
 public class MainActivity extends AndroidApplication{
 	
 	private WebSocketHandler socketOutputHandler;
 		
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         // TODO Lobby
         
         WebSocketInputHandler socketInputHandler = new WebSocketInputHandler();
 
         socketOutputHandler = new WebSocketHandler(socketInputHandler);
         startGame();
     }
 	
 	public void startGame() {
 		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
         cfg.useGL20 = true;
         cfg.useCompass =false;
         cfg.useAccelerometer = false;
         cfg.useWakelock=true;
      
         initialize(new Main(socketOutputHandler), cfg);
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		
		socketOutputHandler.disconnect();
 	}
 
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 	}
 	
 	
 }
