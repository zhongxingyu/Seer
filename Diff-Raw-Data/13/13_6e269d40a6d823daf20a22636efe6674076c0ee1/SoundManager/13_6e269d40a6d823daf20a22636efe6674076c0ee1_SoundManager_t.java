 package com.davidjennes.ElectroJam;
 
 import java.io.IOException;
 import java.util.HashMap;
import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.content.Context;
 import android.content.res.AssetFileDescriptor;
 import android.media.MediaPlayer;
 import android.util.Log;
 
 public class SoundManager {
 	private final static String TAG = "SoundManager";
 	private final static Random RANDOM = new Random();
 
 	private Context m_context;
 	private Map<Integer, Sound> m_sounds;
 	private Timer m_timer = new Timer();
 	private Queue<ScheduledSound> m_soundQueue;
 
 	/**
 	 * Constructor
 	 * @param context The activity's context
 	 */
 	public SoundManager(Context context) {
 		m_context = context;
 		m_sounds = new HashMap<Integer, Sound>();
 		m_soundQueue = new LinkedList<ScheduledSound>();
 		m_timer.scheduleAtFixedRate(new Action(), 0, 3750);
 	}
 
 	/**
 	 * Load a sound and prepare it for playback
 	 * @param resid The resource ID to load from
 	 * @return The sound's ID
 	 */
 	public int loadSound(int resid) {
 		Sound s = new Sound(RANDOM.nextInt(), m_context, resid);
 		m_sounds.put(s.id, s);
 
 		return s.id;
 	}
 
 	/**
 	 * Free up the resources used by a sound
 	 * @param id The sound's ID
 	 */
 	public void unloadSound(int id) {
 		m_sounds.remove(id);
 	}
 
 	/**
 	 * Play a sound, looping if need be
 	 * @param id The sound's ID
 	 * @param looped Will loop if true
 	 */
 	public void playSound(int id, boolean looped) {
 		synchronized (m_soundQueue) {
 			m_soundQueue.add(new ScheduledSound(id, looped));
 		}
 	}
 
 	/**
 	 * Stop a sound which is playing, and prepare it for playback again
 	 * @param id The sound's ID
 	 */
 	public void stopSound(int id) {
		synchronized (m_soundQueue) {
			if (m_sounds.containsKey(id))
				m_sounds.get(id).stop();
			
			for (Iterator<ScheduledSound> it = m_soundQueue.iterator(); it.hasNext(); ) {
				if (it.next().id == id)
					it.remove();
			}
		}
 	}
 	
 	/**
 	 * Beat timer task
 	 */
 	class Action extends TimerTask {
 		public void run() {
 			synchronized (m_soundQueue) {
 				Queue<ScheduledSound> looped = new LinkedList<ScheduledSound>();
 				
 				while (!m_soundQueue.isEmpty()) {
 					ScheduledSound sound = m_soundQueue.remove();
 					m_sounds.get(sound.id).play();
 					
 					// store looped sounds
 					if (sound.looped)
 						looped.add(sound);
 				}
 				
 				// re-enqueue looped sounds
 				m_soundQueue.addAll(looped);
 			}
 		}
 	}
 	
 	/**
 	 * Stores MediaPlayers connected to a single sound 
 	 */
 	class Sound {
 		private MediaPlayer m_mp1, m_mp2, m_current;
 		public int id;
 		
 		/**
 		 * Constructor
 		 * @param context The activity's context
 		 * @param resid The resource ID to load from
 		 */
 		public Sound(int id, Context context, int resid) {
 			m_mp1 = create(context, resid);
 			m_mp2 = create(context, resid);
 			m_current = null;
 			this.id = id;
 		}
 		
 		/**
 		 * Destructor
 		 */
 		protected void finalize() throws Throwable {
 			m_mp1.stop();
 			m_mp2.stop();
 			m_mp1.release();
 			m_mp2.release();
 			super.finalize();
 		}
 		
 		/**
 		 * Start playing (with new media player, and prepare old one)
 		 */
 		public void play() {
 			MediaPlayer old = m_current;
 			
 			// switch players
 			if (m_current == m_mp2)
 				m_current = m_mp1;
 			else
 				m_current = m_mp2;
 			
 			// stop old (and prepare it)
 			if (old != null)
 				try {
 					old.stop();
 					old.prepareAsync();
 				} catch (IllegalStateException e) {
 					e.printStackTrace();
 				}				
 			
 			// start new one
 			m_current.start();
 		}
 		
 		/**
 		 * Stop current media player and prepare it again
 		 */
 		public void stop() {
 			// stop current (and prepare it again)
 			if (m_current != null)
 				try {
 					m_current.stop();
 					m_current.prepareAsync();
 				} catch (IllegalStateException e) {
 					e.printStackTrace();
 				}
 			
 			// switch players
 			if (m_current == m_mp2)
 				m_current = m_mp1;
 			else
 				m_current = m_mp2;
 		}
 		
 		/**
 		 * Create MediaPlayer, but don't wait for prepare to finish
 		 * @param context The activity context
 		 * @param resid The resource ID
 		 * @return A MediaPlayer instance
 		 */
 		private MediaPlayer create(Context context, int resid) {
 			try {
 				AssetFileDescriptor afd = context.getResources().openRawResourceFd(
 						resid);
 				if (afd == null)
 					return null;
 
 				MediaPlayer mp = new MediaPlayer();
 				mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
 						afd.getLength());
 				afd.close();
 				mp.prepareAsync();
 
 				return mp;
 			} catch (IOException ex) {
 				Log.d(TAG, "create failed:", ex);
 			} catch (IllegalArgumentException ex) {
 				Log.d(TAG, "create failed:", ex);
 			} catch (SecurityException ex) {
 				Log.d(TAG, "create failed:", ex);
 			}
 
 			return null;
 		}
 	}
 	
 	/**
 	 * Used for scheduling sound until next timer beat
 	 */
 	class ScheduledSound {
 		public int id;
 		public boolean looped;
 		
 		public ScheduledSound(int i, boolean b) {
 			id = i;
 			looped =b;
 		}
 	}
 }
