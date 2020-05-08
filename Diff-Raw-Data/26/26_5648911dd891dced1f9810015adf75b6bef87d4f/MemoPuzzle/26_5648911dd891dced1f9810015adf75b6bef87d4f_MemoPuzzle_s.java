 package org.anddev.andengine.memopuzzle;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.engine.options.EngineOptions;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.Scene.IOnAreaTouchListener;
 import org.anddev.andengine.entity.scene.Scene.ITouchArea;
 import org.anddev.andengine.input.touch.TouchEvent;
 import org.anddev.andengine.memopuzzle.game.SumBox;
 import org.anddev.andengine.opengl.font.Font;
 import org.anddev.andengine.opengl.font.FontFactory;
 import org.anddev.andengine.opengl.texture.Texture;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.ui.activity.BaseGameActivity;
 import org.anddev.andengine.memopuzzle.utils.MyGameScene;
 import android.graphics.Color;
import android.widget.Toast;
 
 public class MemoPuzzle extends BaseGameActivity implements IOnAreaTouchListener {
     public static final int CAMERA_WIDTH = 480;
     public static final int CAMERA_HEIGHT = 720;
 	
     private Texture mTexFont1;
     private Texture mTexFont2;
	public Font mFontBig;
	public Font mFontSmall;
 	
 	public void onLoadComplete() {
 		
 	}
 	
 	public Engine onLoadEngine() {
 		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
         return new Engine(new EngineOptions(true, ScreenOrientation.PORTRAIT, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera));
 	}
 	
 	public void onLoadResources() {
     	this.mTexFont1 = new Texture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);    	
     	this.mTexFont2 = new Texture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);    	
 		
    	this.mFontBig = FontFactory.createFromAsset(this.mTexFont1, this, "font/akaDylan Collage.ttf", 48, true, Color.WHITE);
		this.mFontSmall = FontFactory.createFromAsset(this.mTexFont2, this, "font/akaDylan Collage.ttf", 30, true, Color.BLACK);
 		
 		getEngine().getTextureManager().loadTextures(this.mTexFont1, this.mTexFont2);
		getEngine().getFontManager().loadFonts(this.mFontBig, this.mFontSmall);
 	}
 	
 	public Scene onLoadScene() {
 		// getEngine().registerUpdateHandler(new FPSLogger());
 		return new SumBox(this);
 	}
 	
 	public void nextScene() {
 		getEngine().setScene(new SumBox(this));
 	}
 	
 	public boolean onAreaTouched(TouchEvent pSceneTouchEvent, ITouchArea pTouchArea, float pTouchAreaLocalX, float pTouchAreaLocalY) {
 		if (pSceneTouchEvent.isActionDown()) {
 			((MyGameScene) getEngine().getScene()).manageTouch(pTouchArea);
 			return true;
 		}
 		return false;
 	}
 	
 }
