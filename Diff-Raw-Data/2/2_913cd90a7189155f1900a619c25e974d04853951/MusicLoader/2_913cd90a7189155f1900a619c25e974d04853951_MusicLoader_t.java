 package com.secondhand.view.resource.loader;
 
 import java.io.IOException;
 
 import org.anddev.andengine.audio.music.Music;
 import org.anddev.andengine.audio.music.MusicFactory;
 
 public final class MusicLoader extends Loader {
 
 	private static MusicLoader instance = new MusicLoader();
 	
 	private final static String BASE_PATH = "mfx/";
	private final static String TWIRL_THEME_PATH = "twirltheme(high).ogg";
 	
 	private MusicLoader () {super();}
 	
 	public static MusicLoader getInstance() {
 		return instance;
 	}
 	
 	public Music getMainTheme() {
 		try {
 			return MusicFactory.createMusicFromAsset(engine.getMusicManager(), context, BASE_PATH + TWIRL_THEME_PATH);
 		} catch(final IOException e) {
 			throw new AssertionError("could not load main theme");
 		}
 	}
 }
