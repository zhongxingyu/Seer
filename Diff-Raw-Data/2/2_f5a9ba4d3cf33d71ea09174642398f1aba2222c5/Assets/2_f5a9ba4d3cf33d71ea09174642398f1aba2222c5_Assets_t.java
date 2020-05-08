 package com.ivanarellano.game.pm;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.assets.AssetErrorListener;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.stbtt.TrueTypeFontFactory;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 
 public class Assets {
 	public static AssetManager manager;
 	public static AssetErrorListener managerError;
 	public static TextureAtlas atlas;
 
 	public static final String NUMBER_CHARACTERS = "1234567890";
 
 	public static class Colors {
 		public static final Color DARK_NAVY = new Color(53.0f / 255.0f,
 				58.0f / 255.0f, 61.0f / 255.0f, 1.0f);
 		public static final Color LIGHT_NAVY = new Color(133 / 255.0f,
 				142 / 255.0f, 148 / 255.0f, 1.0f);
 		public static final Color TAN = new Color(192 / 255.0f, 164 / 255.0f,
 				91 / 255.0f, 1.0f);
 	}
 
 	public static class Fonts {
 		public static final BitmapFont TEACHERS_PET_SS_NUM = TrueTypeFontFactory
				.createBitmapFont(Gdx.files.internal("data/teacpss.ttf"),
 						NUMBER_CHARACTERS, 12.5f, 7.0f, 1.3f,
 						PmGame.SCREEN_WIDTH, PmGame.SCREEN_HEIGHT);
 	}
 
 	public static class LabelStyles {
 		public static final LabelStyle MOVES_COUNTER = new LabelStyle(
 				Fonts.TEACHERS_PET_SS_NUM, Colors.LIGHT_NAVY);
 		public static final LabelStyle TILE_NUMBERS = new LabelStyle(
 				Fonts.TEACHERS_PET_SS_NUM, Colors.TAN);
 	}
 
 	public static void create() {
 		manager = new AssetManager();
 		managerError = new AssetErrorListener() {
 			@Override
 			public void error(String fileName, Class type, Throwable t) {
 				Gdx.app.error("AssetManagerTest", "couldn't load asset '"
 						+ fileName + "'", (Exception) t);
 			}
 		};
 
 		manager.setErrorListener(managerError);
 		Texture.setAssetManager(manager);
 		init();
 	}
 
 	private static void init() {
 		manager.load("data/pack", TextureAtlas.class);
 		manager.finishLoading();
 		atlas = manager.get("data/pack", TextureAtlas.class);
 	}
 
 	public static void loadTexture(String... src) {
 		for (String file : src)
 			manager.load(file, Texture.class);
 	}
 
 	public static Texture getTexture(String src) {
 		return manager.get(src, Texture.class);
 	}
 
 	public static void unload(String... toUnload) {
 		for (String file : toUnload)
 			manager.unload(file);
 	}
 }
