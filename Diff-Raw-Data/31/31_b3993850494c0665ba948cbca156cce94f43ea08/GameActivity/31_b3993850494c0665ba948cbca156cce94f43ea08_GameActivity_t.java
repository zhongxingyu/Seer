 package de.exoticorn.gameframework;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 abstract public class GameActivity extends Activity
 {
 	GameView m_view;
 	AudioPlayer m_audioPlayer;
 	long m_gameFramework;
	boolean m_hasFocus = true;
	boolean m_isWaitingForFocus = false;
 	
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
		m_isWaitingForFocus = false;
 	}
 	
 	@Override protected void onResume()
 	{
 		super.onResume();
		if(m_hasFocus)
		{
			handleResume();
		}
		else
		{
			m_isWaitingForFocus = true;
		}
	}
	
	@Override public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		if(hasFocus && m_isWaitingForFocus)
		{
			handleResume();
		}
		m_hasFocus = hasFocus;
	}
	
	void handleResume()
	{
 		m_view.onResume();
 		m_audioPlayer.onResume();
		m_isWaitingForFocus = false;
	}
 }
