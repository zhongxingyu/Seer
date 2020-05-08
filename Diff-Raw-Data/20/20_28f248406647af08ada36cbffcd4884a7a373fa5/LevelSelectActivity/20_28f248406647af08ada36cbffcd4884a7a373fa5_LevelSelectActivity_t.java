 package com.bugfullabs.qube;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.engine.options.EngineOptions;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
 import org.anddev.andengine.entity.scene.background.SpriteBackground;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.util.FPSLogger;
 import org.anddev.andengine.input.touch.TouchEvent;
 import org.anddev.andengine.input.touch.detector.ClickDetector;
 import org.anddev.andengine.input.touch.detector.ClickDetector.IClickDetectorListener;
 import org.anddev.andengine.input.touch.detector.ScrollDetector;
 import org.anddev.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
 import org.anddev.andengine.input.touch.detector.SurfaceScrollDetector;
 import org.anddev.andengine.opengl.font.StrokeFont;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.ui.activity.BaseGameActivity;
 import org.anddev.andengine.util.HorizontalAlign;
 import org.anddev.andengine.util.VerticalAlign;
 import com.bugfullabs.qube.util.AlignedText;
 import com.bugfullabs.qube.level.Level;
 import com.bugfullabs.qube.level.LevelFileReader;
 
 
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.util.Log;
 import android.widget.Toast;
 
 
 /**
  * 
  * @author Bugful Labs
  * @email admin@bugfullabs.pl
  *
  */
 public class LevelSelectActivity extends BaseGameActivity implements IScrollDetectorListener, IOnSceneTouchListener, IClickDetectorListener {
        
 	    protected static int CAMERA_WIDTH = 800;
         protected static int CAMERA_HEIGHT = 480;
  
         protected static int FONT_SIZE = 36;
         protected static int PADDING = 100;
         
         protected static int MENUITEMS = 3;
 
         
     	//liczba NUMBER_OF_ITEMS / NUMBER_OF_ITEMS_IN_ROW = N
     	//n - liczby naturalne
     	public static final int NUMBER_OF_ITEMS = 15;
     	public static final int NUMBER_OF_ITEMS_IN_ROW = 5;
     	
     	public static final float X_OFFSET = 92;
     	public static final float Y_OFFSET = 92;
         
     	public static final float MARGIN_X = CAMERA_WIDTH/2 - ((X_OFFSET)*(NUMBER_OF_ITEMS_IN_ROW/2));
     	public static final float MARGIN_Y = CAMERA_HEIGHT/2 - (((Y_OFFSET)*((NUMBER_OF_ITEMS/NUMBER_OF_ITEMS_IN_ROW)/2)));
     	
         
         
         private Scene mScene;
         private Camera mCamera;
  
         private Scene gridScene;
         
         private StrokeFont mFont; 
         private BitmapTextureAtlas mFontTexture;     
  
         // Scrolling
         private SurfaceScrollDetector mScrollDetector;
         private ClickDetector mClickDetector;
  
         private float mMinX = 0;
         private float mMaxX = 0;
         private float mCurrentX = 0;
         private int iItemClicked = -1;
         
         private List<TextureRegion> columns = new ArrayList<TextureRegion>();
 
         
         private BitmapTextureAtlas mBackground;
         private TextureRegion background;
         
         private BitmapTextureAtlas mBackground_0;
         private TextureRegion background_0;
         private TextureRegion Item;
         
         
         
         private LevelFileReader LevelReader;
 		private BitmapTextureAtlas mBackground_1;
 		private TextureRegion background_1;
         
         
         @Override
         public void onLoadResources() {
                 // Paths
                 BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/menu/");
  
                 this.mBackground = new BitmapTextureAtlas(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
         		this.background = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBackground, this, "bg.png", 0, 0);
                 this.Item = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBackground, this, "levelitem_0.png", 800, 0);
                 
                 this.mBackground_0 = new BitmapTextureAtlas(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
         		this.background_0 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBackground_0, this, "bg_0.png", 0, 0);
                 
         		this.mBackground_1 = new BitmapTextureAtlas(1024, 512, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
        		this.background_1 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBackground_1, this, "bg_0.png", 0, 0);
                 
         		
         		
                 // Font
                 this.mFontTexture = new BitmapTextureAtlas(256, 256);
                 
                 Typeface typeface = Typeface.createFromAsset(getAssets(), "font/FOO.ttf");
                 this.mFont = new StrokeFont(mFontTexture, typeface, FONT_SIZE, true, Color.WHITE, 2, Color.BLACK);
                 this.mEngine.getTextureManager().loadTextures(this.mFontTexture, this.mBackground, this.mBackground_0);
                 this.mEngine.getFontManager().loadFonts(this.mFont);
                 
                 //Images for the menu
                 for (int i = 0; i < MENUITEMS; i++) {				
                 	BitmapTextureAtlas mMenuBitmapTextureAtlas = new BitmapTextureAtlas(256,256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
             		TextureRegion mMenuTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(mMenuBitmapTextureAtlas, this, "level"+i+".png", 0, 0);
             		
                 	this.mEngine.getTextureManager().loadTexture(mMenuBitmapTextureAtlas);
                 	columns.add(mMenuTextureRegion);	
                 }
                 
                 
                 
         }
  
         @Override
         public Engine onLoadEngine() {
                 this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
  
                 final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new FillResolutionPolicy(), this.mCamera);
                 engineOptions.getTouchOptions().setRunOnUpdateThread(true);
  
                 final Engine engine = new Engine(engineOptions);
                 return engine;
         }
  
         @Override
         public Scene onLoadScene() {
                 this.mEngine.registerUpdateHandler(new FPSLogger());
  
                 this.mScene = new Scene();
                 this.mScene.setBackground(new SpriteBackground(new Sprite(0 ,0 , this.background)));
                
                 this.mScrollDetector = new SurfaceScrollDetector(this);
                 this.mClickDetector = new ClickDetector(this);
  
                 this.mScene.setOnSceneTouchListener(this);
                 this.mScene.setTouchAreaBindingEnabled(true);
                 this.mScene.setOnSceneTouchListenerBindingEnabled(true);
  
                 CreateMenuBoxes();
  
                 return this.mScene;
  
         }
  
         @Override
         public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
                 this.mClickDetector.onTouchEvent(pSceneTouchEvent);
                 this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
                 return true;
         }
  
         @Override
         public void onScroll(final ScrollDetector pScollDetector, final TouchEvent pTouchEvent, final float pDistanceX, final float pDistanceY) {
 
         	//Return if ends are reached
                 if ( ((mCurrentX - pDistanceX) < mMinX)  ){                	
                     return;
                 }else if((mCurrentX - pDistanceX) > mMaxX){
                 	
                 	return;
                 }
                 
                 //Center camera to the current point
                 this.mCamera.offsetCenter(-pDistanceX,0 );
                 mCurrentX -= pDistanceX;
                 	
                
                //Because Camera can have negativ X values, so set to 0
             	if(this.mCamera.getMinX()<0){
             		this.mCamera.offsetCenter(0,0 );
             		mCurrentX=0;
             	}
             	
  
         }
  
         @Override
         public void onClick(ClickDetector pClickDetector, TouchEvent pTouchEvent) {
                 loadLevel(iItemClicked);
         };
  
         // ===========================================================
         // Methods
         // ===========================================================
         
         private void CreateMenuBoxes() {
         	
              int spriteX = PADDING;
         	 int spriteY = ((CAMERA_HEIGHT/2 - columns.get(0).getHeight()/2)+PADDING);
         	 
         	 int strings[] = new int[3];
         	 
         	 strings[0] = R.string.levelpack1;
         	 strings[1] = R.string.levelpack2;
         	 strings[2] = R.string.levelpack3;
         	 
         	 
         	 //current item counter
              int iItem = 1;
 
         	 for (int x = 0; x < columns.size(); x++) {
         		 
         		 //On Touch, save the clicked item in case it's a click and not a scroll.
                  final int itemToLoad = iItem;
         		 
         		 Sprite sprite = new Sprite(spriteX,spriteY,columns.get(x)){
         			 
         			 public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                          iItemClicked = itemToLoad;
                          return false;
         			 }        			 
         		 };        		 
         		
         		 final AlignedText text = new AlignedText(spriteX, spriteY, mFont, iItem+"."+getString(strings[x]), HorizontalAlign.CENTER, VerticalAlign.CENTER, columns.get(0).getWidth(), columns.get(0).getHeight());
         		 
         		 
         		 this.mScene.attachChild(sprite);        		 
         		 this.mScene.attachChild(text);        		 
         		 this.mScene.registerTouchArea(sprite);        		 
 
      		 
         		 spriteX += 20 + PADDING+sprite.getWidth();
         		 iItem++;
         		 
 			}
         	
         	 mMaxX = spriteX - CAMERA_WIDTH;
         }
         
         
  
         @Override
         public void onLoadComplete() {
  
         }
  
        
         //Here is where you call the item load.
         private void loadLevel(final int iLevel) {
                 if (iLevel != -1) {
                         runOnUiThread(new Runnable() {
                                 @Override
                                 public void run() {
                                 		
                                 		setLevelSelectGrid(background_0, Item, iLevel);
                                 	
                                         Toast.makeText(LevelSelectActivity.this, "Load Item " + String.valueOf(iLevel), Toast.LENGTH_SHORT).show();
                                         iItemClicked = -1;
                                 }
                         });
                 }
         }
         
         
         
         
       private void setLevelSelectGrid(TextureRegion bg, TextureRegion Item, final int iLevel){
     	
     	  gridScene = new Scene();
     	  
     	  gridScene.setBackground(new SpriteBackground(new Sprite(0, 0, bg)));
     	  
     	  
     	  
     	  int i = 0;
     	  
     	  for(int j = 0; j < (NUMBER_OF_ITEMS/NUMBER_OF_ITEMS_IN_ROW); j++){
     		  
   			for(int k = 0; k < NUMBER_OF_ITEMS_IN_ROW; k++){
   				
   				final int id = i;
   					  
     		  
       		  final Sprite sprite = new Sprite(MARGIN_X + (k * X_OFFSET) - (Item.getWidth()/2), MARGIN_Y + (j * Y_OFFSET)- (Item.getHeight()/2), Item){
       			  @Override
       			  public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY){
   					
       				  if(pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN){
       					  
       					LevelSelectActivity.this.onLevelSelected(id, iLevel);
       					  
       				  }
       				  return true;
       				  
       			  }
       		  };
       		  
       		  final AlignedText text = new AlignedText(MARGIN_X + (k * X_OFFSET) - (Item.getWidth()/2), MARGIN_Y + (j * Y_OFFSET)- (Item.getHeight()/2), mFont, Integer.toString(i+1), HorizontalAlign.CENTER, VerticalAlign.CENTER, sprite.getWidth(), sprite.getHeight());
       		  gridScene.attachChild(sprite);
       		  gridScene.attachChild(text);	
       		  gridScene.registerTouchArea(sprite);
       		  i++;
   			}
   		}
     	  
     	  gridScene.setTouchAreaBindingEnabled(true);
     	  
     	  this.mCamera.reset();
     	  this.mCamera.offsetCenter(-this.mCurrentX, 0);
     	  
     	  this.mEngine.setScene(gridScene);
     	  
       }
     		  
        
       
       private void onLevelSelected(int id, int iLevel)
       {
     	  
     	  this.LevelReader = new LevelFileReader(this, "level_"+Integer.toString(iLevel)+"_"+Integer.toString(id+1));
     	  
     	  final Level level = this.LevelReader.getLevel();
     	  
     	  GameActivity.level = level;
     	  
     	  
     	  this.startActivity(new Intent(this, GameActivity.class));
     	  this.finish();
     	  
     	  Log.i("TOUCHED", Integer.toString(id+1));
     	  
       }
        
         
         
 }
  
