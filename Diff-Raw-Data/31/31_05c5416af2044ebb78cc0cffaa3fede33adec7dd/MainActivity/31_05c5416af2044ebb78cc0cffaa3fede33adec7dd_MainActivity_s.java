 package com.nauka;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.SmoothCamera;
 import org.anddev.andengine.engine.options.EngineOptions;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.opengl.font.Font;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.anddev.andengine.ui.activity.BaseActivity;
 import org.anddev.andengine.ui.activity.BaseGameActivity;
 
 import android.graphics.Color;
 import android.view.Display;
 
 
 
 public class MainActivity extends BaseGameActivity
 {
 
 	 public Font mFont; //czcionka do pisania na ekranie
 	 public SmoothCamera mCamera ;//uchwyt do kamery
 	 public Scene mCurrentScene; //uchwyt do obecnie aktywnej sceny           
 	 
 	 public Gra game;
 	 public static BaseActivity instance; //?
 
 	 int WIDTH,HEIGHT;//wys i szer okna gry
 	  
 	    @SuppressWarnings("deprecation")
 		@Override
 	    public Engine onLoadEngine() {//c-structor dla startowania gry ONCREATE
 	        
 	        instance = this;
 	        
 	        final Display display = getWindowManager().getDefaultDisplay();
 	        WIDTH = display.getWidth();
 	        HEIGHT = display.getHeight();
 	        mCamera = new SmoothCamera(0, 0, WIDTH, HEIGHT,35000,35000,1);
 	        
 	        return new Engine(new EngineOptions(true, ScreenOrientation.PORTRAIT,//+down
 	               new RatioResolutionPolicy(WIDTH, HEIGHT), mCamera)); //to jest jedna linia! 
   
 	    }
 	    
 	    @Override
 		public void onLoadResources() 
 	    { //tu zalaczam wszystkie zasoby
 	    	
 	    	org.anddev.andengine.opengl.font.FontFactory.setAssetBasePath("Fonts/"); //ustawiam sciezke dla czcionek w folderze Assets/Fonts/ 	
 	    	BitmapTextureAtlas mDroidFontTexture = new BitmapTextureAtlas( 256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 	    	
 	    	mFont = org.anddev.andengine.opengl.font.FontFactory.createFromAsset(mDroidFontTexture,this,"spaintfont.ttf",
 	    			11f ,true,Color.WHITE);
 	    	mFont.reload();
 	    	
	    	this.setCurrentScene(new Gra());
 	    	
 	    }
 	 
 	    @Override
 		public Scene onLoadScene() 
 	    {
 	        return mCurrentScene;
 	    }
 	    
 	    public Scene getCurrentScene()
 	    {
 	    	return mCurrentScene;
 	    }
 	    
 	    
 	    public static MainActivity getSharedInstance() //do pobierania aktywnosci okna glownego
 	    {
 	        return (MainActivity) instance;
 	    }
 
 	    public void setCurrentScene(Scene scene)//zmienia aktywn scene 
 	    {
 	    	mCurrentScene = scene;
 	        getEngine().setScene(mCurrentScene);
 	    }
 
 	    @Override
 		public void onLoadComplete() 
 	    {
 			// TODO Auto-generated method stub
 			
 		}
 	}
 	
 	
 	
 	
 	
 
