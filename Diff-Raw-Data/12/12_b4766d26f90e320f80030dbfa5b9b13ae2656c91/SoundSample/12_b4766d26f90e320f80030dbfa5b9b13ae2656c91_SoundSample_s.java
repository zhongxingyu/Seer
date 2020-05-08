 package com.badlogic.gamedev.samples;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import android.content.Context;
 import android.content.res.AssetFileDescriptor;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.os.Bundle;
 import android.util.Log;
 
 import com.badlogic.gamedev.tools.GameActivity;
 import com.badlogic.gamedev.tools.GameListener;
 
 public class SoundSample extends GameActivity implements GameListener
 {
 	int soundId = 0;
 	SoundPool soundPool;
 	AudioManager audioManager;
 	boolean playedSound = false;
 	
 	public void onCreate( Bundle bundle )
 	{
 		super.onCreate(bundle);
 		setGameListener( this );
 	}
 	
 	@Override
 	public void setup(GameActivity activity, GL10 gl) 
 	{	
 		soundPool = new SoundPool( 5, AudioManager.STREAM_MUSIC, 0);
 		audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
 		
 		try
 		{
			AssetFileDescriptor descriptor = getAssets().openFd( "shot.wav" );
 			soundId = soundPool.load( descriptor, 1 );
 		}
 		catch( Exception ex )
 		{
			Log.d( "Sound Sample", "couldn't load sound 'shot.wav'" );
 			throw new RuntimeException( ex );
 		}
 	}
 	
 	@Override
 	public void mainLoopIteration(GameActivity activity, GL10 gl) 
 	{
 		if( activity.isTouched() && playedSound == false )
 		{
 			int volume = audioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
 			soundPool.play(soundId, volume, volume, 1, 0, 1);
 			playedSound = true;
 		}
 		
 		if( !activity.isTouched() )
 			playedSound = false;		
 	}
 }
