 package com.gradugation;
 
 import java.io.IOException;
 import java.util.HashSet;
 
 import org.andengine.engine.camera.BoundCamera;
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.IEntity;
 import org.andengine.entity.modifier.MoveModifier;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.util.FPSLogger;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 import org.andengine.util.adt.color.Color;
 import org.andengine.util.math.MathUtils;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Handler;
 import android.util.Log;
 import android.view.Menu;
 import android.widget.Toast;
 
 public class WhackAFlyerMiniGame extends SimpleBaseGameActivity implements IOnSceneTouchListener {
 
     private static final int CAMERA_WIDTH = 480;
     private static final int CAMERA_HEIGHT = 320;
     private static final int MAX_NUMBER_OF_FLYERS = 4;
     private static final float MAX_TIME_DELAY_FOR_FLYER = 3;
     private static final float MIN_TIME_DELAY_FOR_FLYER = .25f;
     private static final float MAX_SPAWN_DELAY = 1;
     private static final float MIN_SPAWN_DELAY = .5f;
     private static final int POINTS_REQUIRED = 20;
     private static final int CREDITS_EARNED = 3;
     private HashSet<Float> xLocTaken = new HashSet<Float>();
     private HashSet<Float> yLocTaken = new HashSet<Float>();
     
     String characterType;
     
     // Need handler for callbacks to the UI thread
     final Handler mHandler = new Handler();
 
     // Create runnable for posting
     final Runnable mUpdateResults = new Runnable() {
         public void run() {
             updateResultsInUi();
         }
     };
     
     final Runnable finishGame = new Runnable() {
         public void run() {
             gameFinished();
         }
     };
     
     final Runnable spawnerStart = new Runnable() {
     	public void run() {
     		createrSpawnerTimeHandler();
     	}
     };
     
     private BitmapTextureAtlas characterTextureAtlas;
     private ITextureRegion character;
     private BitmapTextureAtlas bgTextureAtlas;
     private ITextureRegion bgRegion;
     private Sprite sprImage;
     private float currentX;
     private float currentY;
     private float mEffectSpawnDelay = 2;
     private ITextureRegion[] flyers = new ITextureRegion[MAX_NUMBER_OF_FLYERS];
     private BitmapTextureAtlas[] flyerAtlas = new BitmapTextureAtlas[MAX_NUMBER_OF_FLYERS];
     private int points;
     private boolean finished = false;
     private boolean gameStarted = false;
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.whack_aflyer, menu);
         return true;
     }
 
     @Override
     public EngineOptions onCreateEngineOptions() {
         final Camera mCamera = new BoundCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
     
         return new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR,
                 new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
     }
 
     @Override
     protected void onCreateResources() throws IOException {
     	
     	Intent intent = getIntent();
     	characterType = intent.getStringExtra("character_type");
     	if (characterType == null) characterType = "Gradugator";
     	String imgName = "splash2.png";
     	if (characterType.equals("Athlete")) {
     		imgName = "athlete.png";
     	}
     	else if (characterType.equals("Engineer")) {
     		imgName = "engineer.png";
     	}
     	else if (characterType.equals("PreMed")) {
     		imgName = "med_student.png";
     	}
 		
         this.points = 0;
 
         this.characterTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(),
                 600,600,TextureOptions.BILINEAR);
         this.character = BitmapTextureAtlasTextureRegionFactory.createFromAsset
                 (characterTextureAtlas, this, "gfx/" + imgName, 0, 0);
         this.characterTextureAtlas.load();
         
         BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/whack_aflyer_img/");
         this.bgTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(),
                 951,720,TextureOptions.NEAREST_PREMULTIPLYALPHA);
         this.bgRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(bgTextureAtlas,
                 this, "bricks.png", 0, 0);
         this.bgTextureAtlas.load();
     
         for (int i = 0; i < MAX_NUMBER_OF_FLYERS; i++) {
             imgName = "flyer_" + (i+1) + ".png";
             this.flyerAtlas[i] = new BitmapTextureAtlas(this.getTextureManager(),
                     1024,1024,TextureOptions.BILINEAR);
             this.flyers[i] = BitmapTextureAtlasTextureRegionFactory.createFromAsset
                     (flyerAtlas[i], this, imgName, 0, 0);
             this.flyerAtlas[i].load(); 
         }
     }
  
 
     @Override
     protected Scene onCreateScene() {
         this.mEngine.registerUpdateHandler(new FPSLogger());
     
         Scene scene = new Scene();
         scene.setOnSceneTouchListener(this);
         
         final float centerX = 239;
         final float centerY = 0;
         currentX = centerX;
         currentY = centerY;
     
         scene.setTouchAreaBindingOnActionDownEnabled(true);
         scene.setTouchAreaBindingOnActionMoveEnabled(true);
         scene.setBackground(new Background(Color.WHITE));
        
         final Sprite mySprite = new Sprite(0, 0, bgRegion,
                 this.getVertexBufferObjectManager());
         
         sprImage = new Sprite(currentX,currentY, character,
                 this.getVertexBufferObjectManager());
         sprImage.setScale((float) .1);
         
         //Font mFont = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32);
         //mFont.load();
 
         //this.pointsLabel = new Text(100, 40, mFont, "Points: ", new TextOptions(), this.getVertexBufferObjectManager());
         //new Text(100, 40, mFont, &quot;Hello AndEngine!nYou can even have multilined text!&quot;, new TextOptions(HorizontalAlign.CENTER), this.getVertexBufferObjectManager());
 
         
         scene.attachChild(mySprite);
         scene.attachChild(sprImage);
         //scene.attachChild(pointsLabel);
     
         mHandler.post(mUpdateResults);
         mHandler.post(spawnerStart);
     
         
         
         return scene;
     }
     
     private void flyerWhacked(Sprite spriteWhacked) {
         this.mEngine.getScene().detachChild(spriteWhacked);
         this.points += 1;
     }
 
     @Override
     public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
         if (pSceneTouchEvent.isActionDown()) {
 
         }
         return false;
     }
     
     public void updateResultsInUi() {
     	try {
     	this.mEngine.getScene().registerUpdateHandler(new IUpdateHandler () {
             @Override
             public void reset() { }
     
             @Override
             public void onUpdate(final float pSecondsElapsed) {  
                     
                     if (!finished && gameStarted) {
                         if (currentY <= CAMERA_HEIGHT+32f) {
                             sprImage.registerEntityModifier(new MoveModifier(0.05f,
                                     currentX,currentY, currentX, currentY + 1) {
                             @Override
                             protected void onModifierStarted(IEntity pItem) {
                                 super.onModifierStarted(pItem);
                             }
             
                             @Override
                             protected void onModifierFinished(IEntity pItem) {
                                 currentX=sprImage.getX();
                                 currentY=sprImage.getY();
                                 super.onModifierFinished(pItem);
                             }
                             
                         });
                         } else {
                             mHandler.post(finishGame);
                         }
                         //pointsLabel.setText("Points: " + points);
                     }
             }
         });
         
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(getString(R.string.whack_aflyer_instructions, POINTS_REQUIRED));
         builder.setCancelable(false);
         builder.setPositiveButton("Start", new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int arg1) {
 				dialog.dismiss();
 				gameStarted = true;
 				
 			}
         	
         });
         
         AlertDialog dialog = builder.create();
         dialog.show();}
     	catch (Exception e) {
     		Log.d("ERROR", "FIND THIS:" + e.getMessage());
     	}
     }
     
     private Sprite createSprite(float pX, float pY, ITextureRegion spriteTexture) {
         final Sprite sprite = new Sprite(pX, pY, spriteTexture,
                 this.getVertexBufferObjectManager()) {
         	private boolean touched = false;
             @Override
             public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
                 if (touchEvent.isActionUp() && !this.touched) {
                 	this.touched = true;
                     flyerWhacked(this);
                 }
                 return false;
             }
             
         };
         this.mEngine.getScene().registerTouchArea(sprite);
 
         sprite.setScale((float) .1);
         this.mEngine.getScene().attachChild(sprite);
         
         return sprite;
     }
     
     private void removeSprite (Sprite sprite) {
         this.mEngine.getScene().detachChild(sprite);
     }
     
     private void createrSpawnerTimeHandler() {
     	this.mEngine.registerUpdateHandler(new TimerHandler(mEffectSpawnDelay, true,
     			new ITimerCallback() {
     		@Override
     		public void onTimePassed(TimerHandler pTimerHandler) {
     			mEffectSpawnDelay = MathUtils.random(MIN_SPAWN_DELAY, MAX_SPAWN_DELAY);
     			createFlyerSpawnTimeHandler();
     			pTimerHandler.setTimerSeconds(mEffectSpawnDelay);
     			pTimerHandler.setAutoReset(!finished);
     			
     		}
     	}));
     }
     private void createFlyerDestroyTimeHandler(Sprite sprite) {
         final Sprite spriteToRemove = sprite;
         final float timeToKeepFlyer = MathUtils.random(MIN_TIME_DELAY_FOR_FLYER,
                                                      MAX_TIME_DELAY_FOR_FLYER);
         this.getEngine().registerUpdateHandler(new TimerHandler(timeToKeepFlyer,
                 new ITimerCallback() {
             @Override
             public void onTimePassed(TimerHandler pTimerHandler) {
                 removeSprite(spriteToRemove);
                 xLocTaken.remove(spriteToRemove.getX());
                 yLocTaken.remove(spriteToRemove.getY());
                 
             } 
         }));
     }
 
     private void createFlyerSpawnTimeHandler() {
         float xPos,yPos;
         
         do {
 	        xPos = MathUtils.random(0, CAMERA_WIDTH-32.0f); 
 	    	yPos = MathUtils.random(0, CAMERA_HEIGHT-32.0f);
 	        //xPos = (int)xPos / 32;
 	        //yPos = (int)yPos / 32;
         } while (yLocTaken.contains(xPos) || xLocTaken.contains(yPos));
 
         yLocTaken.add(yPos);
         xLocTaken.add(xPos);
 
         final int imgPos = MathUtils.random(MAX_NUMBER_OF_FLYERS);
                    
         if (!finished && gameStarted) {
         	Sprite spriteCreated = createSprite(xPos, yPos, flyers[imgPos]);
         	
         	createFlyerDestroyTimeHandler(spriteCreated);
         }
     }
     
     private void gameFinished() {
         finished = true;
         Intent output = new Intent();
         if (this.points >= POINTS_REQUIRED) {
             // Code to add CREDITS_EARNED number of credits to the character
 			// Gradugator gets a credit bonus for this minigame
 			if (characterType.equals("Gradugator")) {
				Toast.makeText(WhackAFlyerMiniGame.this, getString(R.string.whack_aflyer_success, this.points,
	                    CREDITS_EARNED + 1), Toast.LENGTH_LONG).show();
 				output.putExtra(Event.WHACK_AFLYER_REQUEST_CODE+"", CREDITS_EARNED + 1);
 			}
 			else {
				Toast.makeText(WhackAFlyerMiniGame.this, getString(R.string.whack_aflyer_success, this.points,
	                    CREDITS_EARNED), Toast.LENGTH_LONG).show();
 				output.putExtra(Event.WHACK_AFLYER_REQUEST_CODE+"", CREDITS_EARNED);
 			}
         }
         else {
             Toast.makeText(WhackAFlyerMiniGame.this, getString(R.string.whack_aflyer_failure, this.points), 
                     Toast.LENGTH_LONG).show();
             output.putExtra(Event.WHACK_AFLYER_REQUEST_CODE+"", 0);
         }
         setResult(RESULT_OK, output);
         
         BitmapTextureAtlas continueButtonAtlas = new BitmapTextureAtlas(this.getTextureManager(),
                 951,720,TextureOptions.NEAREST_PREMULTIPLYALPHA);
         ITextureRegion continueButton = BitmapTextureAtlasTextureRegionFactory.createFromAsset(continueButtonAtlas,
                 this, "continue_button.png", 0, 0);
         continueButtonAtlas.load();
         
         final Sprite continueSprite = new Sprite(CAMERA_WIDTH/2, 32f, continueButton,
                 this.getVertexBufferObjectManager()) {
             @Override
             public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
                 if (touchEvent.isActionDown()) {
                    finish();
                 }
                 return false;
             }
             
         };
         continueSprite.setScale((float) .25);
         this.mEngine.getScene().registerTouchArea(continueSprite);
         this.mEngine.getScene().attachChild(continueSprite);
     }
     
     @Override
     public void finish() {
     	super.finish();
     }
 }
 
