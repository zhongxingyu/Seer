 package de.exoticorn.gameframework;
 
 import android.media.AudioTrack;
 import android.media.AudioFormat;
 import android.media.AudioManager;
 
 public class AudioPlayer extends Thread {
 	long m_gameFramework;
 	AudioTrack m_track;
 	short[] m_buffer;
 	
 	public AudioPlayer(long gameFramework)
 	{
 		m_gameFramework = gameFramework;
 		m_buffer = new short[1024];
 		int bufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT);
 		m_track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
 	}
 	
 	public void run()
 	{
 		m_track.play();
 		while(true)
 		{
 			Native.fillAudioBuffer(m_gameFramework, m_buffer);
 			m_track.write(m_buffer, 0, m_buffer.length);
 		}
 	}
 	
 	public void onPause()
 	{
 		m_track.stop();
 	}
 	
 	public void onResume()
 	{
 		m_track.play();
 	}
 }
