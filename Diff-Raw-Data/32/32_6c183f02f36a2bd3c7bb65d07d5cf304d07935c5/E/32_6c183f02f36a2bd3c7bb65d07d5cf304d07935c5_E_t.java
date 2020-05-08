 package com.vyorkin.engine;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.vyorkin.engine.services.LocaleManager;
 import com.vyorkin.engine.services.MusicManager;
 import com.vyorkin.engine.services.PreferencesManager;
 import com.vyorkin.engine.services.SoundManager;
 
 public class E {
 	public static PreferencesManager preferences;
 	public static LocaleManager locales;
 	public static SoundManager sounds;
 	public static MusicManager music;
 	public static AssetManager assets;
 	public static SpriteBatch batch;
 	public static GameSettings settings;
 	public static BitmapFont font;
 	
 	public static void log(String text) {
 		Gdx.app.log(settings.log, text);
 	}
 	
	public static void log(String text, Object... args) {
		log(String.format(text, args));
	}
	
 	public static void dispose() {
 		music.dispose();
 		font.dispose();
 		batch.dispose();
 		assets.dispose();
 	}
 }
