 package de.exoticorn.gameframework;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 abstract public class GameActivity extends Activity
 {
 	GameView m_view;
 	AudioPlayer m_audioPlayer;
 	long m_gameFramework;
 	
 	@Override protected void onCreate(Bundle icicle)
 	{
 		super.onCreate(icicle);
 		
 		Native.loadLib();
 		
 		m_gameFramework = Native.createGameFramework();
 		
 		m_view = new GameView(getApplication(), m_gameFramework);
 		setContentView(m_view);
 		
 		m_audioPlayer = new AudioPlayer(m_gameFramework);
 		m_audioPlayer.start();
 	}
 
 	@Override protected void onPause()
 	{
 		super.onPause();
 		m_view.onPause();
 		m_audioPlayer.onPause();
 	}
 	
 	@Override protected void onResume()
 	{
 		super.onResume();
 		m_view.onResume();
 		m_audioPlayer.onResume();
	}	
 }
