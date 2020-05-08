 package com.evervoid.client.sound;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.logging.Logger;
 
 import com.evervoid.client.EVFrameManager;
 import com.evervoid.client.EverVoidClient;
 import com.evervoid.client.graphics.FrameUpdate;
 import com.evervoid.client.interfaces.EVFrameObserver;
 import com.evervoid.json.Json;
 import com.evervoid.utils.LoggerUtils;
 import com.evervoid.utils.MathUtils;
 
 public class EVSoundEngine implements EVFrameObserver
 {
 	private static MP3 sBGMusic;
 	private static EVSoundEngine sInstance;
 	private final static ArrayList<MP3> sSFXList = new ArrayList<MP3>();
 	public static final Logger sSoundEngineLog = LoggerUtils.getLogger();
 
 	public static void cleanup()
 	{
 		if (sBGMusic != null) {
 			sBGMusic.close();
 		}
 	}
 
 	public static EVSoundEngine getInstance()
 	{
 		return sInstance;
 	}
 
 	public static void init()
 	{
 		if (sInstance == null) {
 			sInstance = new EVSoundEngine();
 		}
 	}
 
 	/**
 	 * Play a sound effect that can be stacked and played simultaneously.
 	 */
 	public static void playEffect(final int sfxNumber)
 	{
 		if (EverVoidClient.getSettings().getSfx()) {
 			final MP3 effect = sSFXList.get(sfxNumber);
 			if (effect != null) {
 				try {
 					effect.play();
 				}
 				catch (final Exception e) {
 					// Worst case, the effect didn't play.
 				}
 			}
 		}
 	}
 
 	public static void stopSound()
 	{
 		if (sBGMusic != null) {
 			sBGMusic.close();
 			sInstance.aTimeLeft = 0;
 		}
 	}
 
 	private float aTimeLeft = 0;
 	private final ArrayList<MP3> songList = new ArrayList<MP3>();
 
 	private EVSoundEngine()
 	{
 		EVFrameManager.register(this);
 		final Json musicInfo = Json.fromFile("snd/music/soundtracks.json");
 		for (final String music : musicInfo.getAttributes()) {
 			songList.add(new MP3("music" + File.separator + music, musicInfo.getAttribute(music).getInt()));
 		}
 		final Thread loadAllSounds = new Thread()
 		{
 			@Override
 			public void run()
 			{
 				loadSounds();
 			}
 		};
 		loadAllSounds.setDaemon(true);
 		loadAllSounds.setName("Loading all sounds");
 		loadAllSounds.start();
 	}
 
 	@Override
 	public void frame(final FrameUpdate f)
 	{
 		aTimeLeft -= f.aTpf;
 		if (aTimeLeft <= 0 && EverVoidClient.getSettings().getSound()) {
 			if (sBGMusic != null) {
 				sBGMusic.close();
 			}
 			sBGMusic = ((MP3) MathUtils.getRandomElement(songList)).clone();
 			try {
 				sBGMusic.play();
 			}
 			catch (final Exception e) {
 				aTimeLeft = 0;
				sSoundEngineLog.warning("Could not load \"" + sBGMusic.getName() + "\", this song will be disabled.");
 				songList.remove(sBGMusic);
 				if (songList.size() == 0) {
 					sSoundEngineLog.severe("Could not load any songs, music will be disabled entirely.");
 					EVFrameManager.deregister(this);
 					sInstance = null;
 				}
 			}
 			aTimeLeft = sBGMusic.getDuration();
 		}
 	}
 
 	/*
 	 * Load the sound effects in memory asynchronously.
 	 */
 	private void loadSounds()
 	{
 		// Load the sound effects in memory.
 		final Json sfxInfo = Json.fromFile("snd/sfx/soundeffects.json");
 		for (final String sound : sfxInfo.getAttributes()) {
 			sSFXList.add(new MP3("sfx" + File.separator + sound));
 		}
 		// Simplify the constants naming
 		Collections.reverse(sSFXList);
 	}
 }
