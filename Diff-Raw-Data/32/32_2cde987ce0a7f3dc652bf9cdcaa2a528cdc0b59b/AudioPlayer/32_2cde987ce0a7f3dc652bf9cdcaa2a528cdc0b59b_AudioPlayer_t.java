 package de.exoticorn.gameframework;
 
 import android.media.AudioTrack;
 import android.media.AudioFormat;
 import android.media.AudioManager;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
 
 public class AudioPlayer extends Thread {
 	long m_gameFramework;
 	AudioTrack m_track;
 	short[] m_buffer;
	final ReentrantLock m_lock = new ReentrantLock();
	final Condition m_playingCondition = m_lock.newCondition();
	boolean m_isPlaying = true;
 	
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
			m_lock.lock();
			try
			{
				while(!m_isPlaying)
				{
					m_playingCondition.await();
				}
			}
			catch(InterruptedException e)
			{
				break;
			}
			finally
			{
				m_lock.unlock();
			}
 			Native.fillAudioBuffer(m_gameFramework, m_buffer);
 			m_track.write(m_buffer, 0, m_buffer.length);
 		}
 	}
 	
 	public void onPause()
 	{
 		m_track.stop();
		m_lock.lock();
		m_isPlaying = false;
		m_lock.unlock();
 	}
 	
 	public void onResume()
 	{
 		m_track.play();
		m_lock.lock();
		m_isPlaying = true;
		m_playingCondition.signal();
		m_lock.unlock();
 	}
 }
