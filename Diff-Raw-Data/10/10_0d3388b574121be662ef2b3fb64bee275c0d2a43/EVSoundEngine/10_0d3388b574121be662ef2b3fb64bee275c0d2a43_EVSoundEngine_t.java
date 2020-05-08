 package com.evervoid.client.sound;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 import com.evervoid.client.EVFrameManager;
 import com.evervoid.client.graphics.FrameUpdate;
 import com.evervoid.client.interfaces.EVFrameObserver;
 import com.evervoid.json.Json;
 import com.evervoid.utils.MathUtils;
 import com.jme3.asset.AssetManager;
 import com.jme3.audio.AudioRenderer;
 
 public class EVSoundEngine implements EVFrameObserver
 {
 	private static MP3 bgMusic;
 	private final static ArrayList<Clip> sfxList = new ArrayList<Clip>();
 	private static EVSoundEngine sInstance;
 	public static final Logger sSoundEngineLog = Logger.getLogger(EVSoundEngine.class.getName());
 
 	public static void cleanup()
 	{
 		if (bgMusic != null) {
 			bgMusic.close();
 		}
 	}
 
 	public static EVSoundEngine getInstance()
 	{
 		return sInstance;
 	}
 
 	public static void init(final AssetManager pAssetManager, final AudioRenderer pAudioRenderer)
 	{
 		if (sInstance == null) {
 			sInstance = new EVSoundEngine(pAssetManager, pAudioRenderer);
 		}
 	}
 
 	/**
 	 * Play a sound effect that can be stacked and played simultaneously.
 	 */
 	public static void playEffect(final int sfxNumber)
 	{
 		// Because an audioNode can only be playing once, we play a copy instead so that
 		// we can play more than one simultaneously
 		final Clip currentClip = sfxList.get(sfxNumber);
 		if (currentClip.isRunning()) {
 			currentClip.stop();
 		}
 		currentClip.setFramePosition(0);
 		currentClip.start();
 	}
 
 	/**
 	 * Play a sound effect that can not be played simultaneously.
 	 */
 	public static void playSound(final int sfxNumber)
 	{
 		// aAudioRenderer.playSource(sfxList.get(sfxNumber));
 	}
 
 	private final AssetManager aManager;
 	private float aTimeLeft = 0;
 	private final ArrayList<String> songList = new ArrayList<String>();
 
 	private EVSoundEngine(final AssetManager pAssetManager, final AudioRenderer pAudioRenderer)
 	{
 		aManager = pAssetManager;
 		sSoundEngineLog.setLevel(Level.WARNING);
 		Clip tempClip;
 		EVFrameManager.register(this);
 		final Json musicInfo = Json.fromFile("res/snd/music/soundtracks.json");
 		for (final String music : musicInfo.getAttributes()) {
 			songList.add("res" + File.separator + "snd" + File.separator + "music" + File.separator + music);
 		}
 		// Load the sound effects in memory.
 		final Json sfxInfo = Json.fromFile("res/snd/sfx/soundeffects.json");
 		for (final String sound : sfxInfo.getAttributes()) {
 			try {
 				final File file = new File("res" + File.separator + "snd" + File.separator + "sfx" + File.separator + sound);
				final AudioInputStream tempSteam = AudioSystem.getAudioInputStream(file);
				tempClip = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class, tempSteam.getFormat()));
				tempClip.open(tempSteam);
 				sfxList.add(tempClip);
 			}
 			catch (final LineUnavailableException e) {
 				e.printStackTrace();
 			}
 			catch (final IOException e) {
 				e.printStackTrace();
 			}
 			catch (final UnsupportedAudioFileException e) {
 				e.printStackTrace();
 			}
 		}
 		// Simplify the constants naming
 		Collections.reverse(sfxList);
 	}
 
 	@Override
 	public void frame(final FrameUpdate f)
 	{
 		aTimeLeft -= f.aTpf;
 		if (aTimeLeft <= 0) {
 			if (bgMusic != null) {
 				bgMusic.close();
 			}
 			final String randomSong = (String) MathUtils.getRandomElement(songList);
 			bgMusic = new MP3(randomSong);
 			try {
 				bgMusic.play();
 			}
 			catch (final Exception e) {
 				aTimeLeft = 0;
 				sSoundEngineLog.warning("Could not load \"" + randomSong + "\", this song will be disabled.");
 				songList.remove(randomSong);
 				if (songList.size() == 0) {
 					sSoundEngineLog.severe("Could not load any songs, music will be disabled entirely.");
 					EVFrameManager.deregister(this);
 					sInstance = null;
 				}
 			}
 			// TODO: You know...
 			aTimeLeft = 521;
 		}
 	}
 }
