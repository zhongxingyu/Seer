 package com.bugfullabs.qube;
 
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.background.SpriteBackground;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.opengl.font.StrokeFont;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 
 import com.bugfullabs.qube.util.Button;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.view.KeyEvent;
 
 public class MainMenuActivity extends LoadingActivity{
 
 	Scene scene;
 	Scene optionsScene;
 	Scene mainScene;
 	
 	private BitmapTextureAtlas mAtlas;
 	private TextureRegion background;
 	private TextureRegion buttonTexture;
 	private BitmapTextureAtlas mFontTexture;
 	private StrokeFont Stroke;
 	
 	private static final int NEWGAME_BUTTON = 0;
 	private static final int OPTIONS_BUTTON = 1;
 	private static final int EXIT_BUTTON = 2;
 	private static final int MUSIC_BUTTON = 3;
 	private static final int SOUND_BUTTON = 4;
 	private static final int RESET_BUTTON = 5;	
 	
 	private Button Buttons[] = new Button[6];
 	
 	SharedPreferences settings;
 	SharedPreferences.Editor editor;
 	private static final String SETTINGS_FILE = "Settings";
 	
 	private boolean sound = false;
 	private boolean music = false;
 	
 	@Override
 	protected Scene onAssetsLoaded() {
 		
 		settings = getSharedPreferences(SETTINGS_FILE, 0);
 		editor = settings.edit();
 		
 		sound = settings.getBoolean("sound", false);
 		music = settings.getBoolean("music", false);		
 
 		initMainMenu();
 		initOptionsMenu();
 		
 		if(music != true)
 		{
 		Buttons[MUSIC_BUTTON].setText(String.format(getString(R.string.music) ,getString(R.string.no)));
 		}
 		
 		if(sound != true)
 		{
 			Buttons[SOUND_BUTTON].setText(String.format(getString(R.string.sound) ,getString(R.string.no)));
 		}
 		
 		return mainScene;
 	}
 
 	@Override
 	protected void assetsToLoad() {
 		super.setLoadingProgress(10);
 		this.mAtlas = new BitmapTextureAtlas(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/menu/");
 		super.setLoadingProgress(20);
 		this.background = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAtlas, this, "bg.png", 0, 0);
         this.buttonTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAtlas, this, "button.png", 800, 0); 
         super.setLoadingProgress(60);
         this.mFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
         Typeface typeface =  Typeface.createFromAsset(getAssets(), "font/FOO.ttf");
         Stroke = new StrokeFont(mFontTexture, typeface, 26, true, Color.WHITE, 2, Color.BLACK);
         super.setLoadingProgress(90);      
         this.mEngine.getTextureManager().loadTextures(this.mAtlas, this.mFontTexture);
         this.mEngine.getFontManager().loadFonts(Stroke);      
         
         super.setLoadingProgress(100);
 	}
 	
     @Override
     public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
             if(pKeyCode == KeyEvent.KEYCODE_BACK && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
             		this.mEngine.setScene(onAssetsLoaded());
                     return true;
             } else {
                     return super.onKeyDown(pKeyCode, pEvent);
             }
     }
 
 	
 	
 	private void initMainMenu()
 	{
 		
 		mainScene = new Scene();
 		mainScene.setBackground(new SpriteBackground(new Sprite(0, 0, background)));
 		mainScene.setTouchAreaBindingEnabled(true);
 		
 		Buttons[NEWGAME_BUTTON] = new Button(mainScene, 275, 225, 250, 75, getString(R.string.newgame), buttonTexture, Stroke){
 			@Override
 			public void onButtonPressed(){	
 			
 			Intent intent = new Intent(MainMenuActivity.this, LevelSelectActivity.class);
 			MainMenuActivity.this.startActivity(intent);
 			}
 		};
 		Buttons[OPTIONS_BUTTON] = new Button(mainScene, 275, 300, 250, 75, getString(R.string.options), buttonTexture, Stroke){
 		@Override
 		public void onButtonPressed(){	
 		MainMenuActivity.this.mEngine.setScene(optionsScene);
 		}
 	};
 		Buttons[EXIT_BUTTON] = new Button(mainScene, 275, 375, 250, 75, getString(R.string.exit), buttonTexture, Stroke){
 		@Override
 		public void onButtonPressed(){	
 			MainMenuActivity.this.finish();
 		}
 	};
 		
 	}
 	private void initOptionsMenu()
 	{
 		optionsScene = new Scene();
 		optionsScene.setBackground(new SpriteBackground(new Sprite(0, 0, background)));
 		optionsScene.setTouchAreaBindingEnabled(true);
 		
 		Buttons[MUSIC_BUTTON] = new Button(optionsScene, 275, 225, 250, 75, String.format(getString(R.string.music), getString(R.string.yes)), buttonTexture, Stroke){
 			@Override
 			public void onButtonPressed(){
 			
				music = !music;
				
				editor.putBoolean("music", music);
 				editor.commit();
 				if(music == true){
 				this.setText(String.format(getString(R.string.music), getString(R.string.yes)));
 				}else{
 				this.setText(String.format(getString(R.string.music), getString(R.string.no)));
 				}
 				}
 		};
 		Buttons[SOUND_BUTTON] = new Button(optionsScene, 275, 300, 250, 75, String.format(getString(R.string.sound), getString(R.string.yes)), buttonTexture, Stroke){
 		@Override
 		public void onButtonPressed(){
 		
			sound = !sound;
			
			editor.putBoolean("sound", sound);
 			editor.commit();
			
 			if(sound == true){
 			this.setText(String.format(getString(R.string.sound), getString(R.string.yes)));
 			}else{
 			this.setText(String.format(getString(R.string.sound), getString(R.string.no)));
 			}
 			}
 	};
 		
 		Buttons[RESET_BUTTON] = new Button(optionsScene, 275, 375, 250, 75, getString(R.string.reset), buttonTexture, Stroke);
 	}	
 	
 	
 }
 
 
