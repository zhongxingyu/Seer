 /*******************************************************************************
  * Copyright 2011 Federico Paolinelli
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package com.fede.checkers.ui;
 
 
 
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.FrameLayout;
 import android.widget.RelativeLayout;
 
 import com.admob.android.ads.AdManager;
 import com.admob.android.ads.AdView;
 import com.fede.checkers.Constants;
 import com.fede.checkers.R;
 import com.fede.checkers.board.AndEngineBoard;
 import com.fede.checkers.boards.BoardClassicExtended;
 import com.fede.checkers.boards.BoardType;
 import com.fede.checkers.boards.CheckersDbHelper;
 
 import org.anddev.andengine.audio.sound.Sound;
 import org.anddev.andengine.audio.sound.SoundFactory;
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.engine.options.EngineOptions;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.background.SpriteBackground;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.util.FPSLogger;
 import org.anddev.andengine.opengl.texture.Texture;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
 import org.anddev.andengine.opengl.view.RenderSurfaceView;
 import org.anddev.andengine.ui.activity.BaseGameActivity;
 import org.anddev.andengine.util.Debug;
 
 import java.io.IOException;
 
 /**
  * Main activity class. It implements the real game
  * @author fede
  *
  */
 public class ChineseCheckers extends BaseGameActivity {
 
 
     private Camera mCamera;
     CheckersSpriteFactory mSpriteFactory;
     private BoardType mBoardType;   // Board kind
     private Boolean mLoadSaved;     // Says if a saved instance must be loaded
     private AndEngineBoard mBoard;  
     private Boolean mFinishing;
     private int mCameraWidth = Constants.CAMERA_WIDTH;
     private int mCameraHeight = 0;
     private CheckersDbHelper db;
     private String mBoardName;
     
 
     private Sound mMarbleSound;
 
 
     private TextureRegion mBackgroundRegion;
 
     public static void launch(Activity launcher, String board, Boolean restore){
         Intent i = new Intent(launcher, ChineseCheckers.class);
         i.putExtra(Constants.BOARD_NAME_INTENT, board);
         i.putExtra(Constants.BOARD_RESTORE_INTENT, restore);
         launcher.startActivity(i);
     }
     
     public static void launch(Activity launcher, String board){
         launch(launcher, board, false);
     }
     
     
     @Override
     protected void onCreate(Bundle pSavedInstanceState) {
         mFinishing = false;
         Bundle extras = getIntent().getExtras();
         String boardName = extras != null ? extras.getString(Constants.BOARD_NAME_INTENT) 
                 : BoardClassicExtended.NAME;
         
         mLoadSaved = extras != null ? extras.getBoolean(Constants.BOARD_RESTORE_INTENT, false) 
                 : false;
         
         mBoardType = BoardType.getBoardFromName(boardName);
         mBoardName = boardName;
         db = new CheckersDbHelper(this); 
         super.onCreate(pSavedInstanceState);
     }
     
     @Override
     public Engine onLoadEngine() {
         
         mCameraHeight = Constants.getHeight(mCameraWidth, this); 
         this.mCamera = new Camera(0, 0, mCameraWidth, mCameraHeight);
         return new Engine(new EngineOptions(true, ScreenOrientation.PORTRAIT, 
                 new RatioResolutionPolicy(mCameraWidth, mCameraHeight), this.mCamera).setNeedsSound(true));
     }
 
     @Override
     public void onLoadResources() {
         
         mSpriteFactory = new CheckersSpriteFactory(mEngine, this); 
         Texture texture = new Texture(512 , 1024, TextureOptions.DEFAULT);  
 
         mBackgroundRegion  = TextureRegionFactory.createFromAsset(texture, this, "gfx/back.png", 0, 0);
         mEngine.getTextureManager().loadTexture(texture);
         
         SoundFactory.setAssetBasePath("mfx/");
         try {
             this.mMarbleSound = SoundFactory.createSoundFromAsset(this.mEngine.getSoundManager(), this, "marble.ogg");
         } catch (final IOException e) {
             Debug.e("Error", e);
         }
 
     }
 
     @Override
     public Scene onLoadScene() {
         this.mEngine.registerUpdateHandler(new FPSLogger());
 
         final Scene scene = new Scene(3);
         
         Sprite back = new Sprite(0, 0, mBackgroundRegion);
         scene.setBackground(new SpriteBackground(back));
         
         mBoard = new AndEngineBoard(mCameraWidth, mCameraHeight, mBoardType, mLoadSaved, scene, mSpriteFactory, this);        
         scene.setTouchAreaBindingEnabled(true);
         return scene;
     }
 
     @Override
     public void onLoadComplete() {
 
     }
 
     @Override
     protected void onPause() {
         db.close();
         if(!mFinishing){
             mBoardType.saveDump(mBoard.serialize(), mBoard.getScore(), this);
             CheckersDbHelper.setLastBoardUsed(mBoardName, this);
         }
         super.onPause();
     }
     
     @Override
     protected void onResume() {
         db.open();
         super.onResume();
     }
     
 
 
     @Override
     protected void onRestart() {
         mLoadSaved = true;
         super.onRestart();
     }
     
     
     private int getOldScore(){
         return db.getBoardMaxScore(mBoardName);         
     }
     
     /**
      * Behaviour to be performed when no more moves are available
      */
     public void onGameStall(long thisGameScore){ 
         String button1String = getString(R.string.new_game); 
         String cancelString = getString(R.string.back);
         AlertDialog.Builder ad = new AlertDialog.Builder(this); 
        
         
         int oldScore = getOldScore();
         if(thisGameScore > oldScore){
            ad.setTitle(getString(R.string.new_record));
             ad.setMessage(String.format(getString(R.string.no_more_moves_record), thisGameScore, oldScore));
         }else{
            ad.setTitle(getString(R.string.stall));
             ad.setMessage(String.format(getString(R.string.no_more_moves), thisGameScore));
         }
         
         // Relaunch the same game
         ad.setPositiveButton(button1String,
                              new OnClickListener() { 
                                 public void onClick(DialogInterface dialog, int arg1) {
                                     mFinishing = true;
                                     launch(ChineseCheckers.this, mBoardType.getName());
                                     ChineseCheckers.this.finish();
                                 } });    
         
         
         // Returns to board list
         ad.setNegativeButton(cancelString,
                 new OnClickListener() { 
                    public void onClick(DialogInterface dialog, int arg1) {
                        mFinishing = true;
                        ChineseCheckers.this.finish();
                    } });
         ad.show();
         mBoardType.delete(this);
         db.setBoardMaxScore(mBoardName, thisGameScore);
         return;    
     }
 
 
     protected boolean showInCenter=true;
     protected boolean testMode=true;
 
     @Override
     protected void onSetContentView() {
         if(testMode){
             AdManager.setTestDevices( new String[] { 
                     AdManager.TEST_EMULATOR, 
                     "FDF5AA30500D9CBDE7CC910D1A529F77", 
                     } );
         }
         
         final RelativeLayout relativeLayout = new RelativeLayout(this);
         final FrameLayout.LayoutParams relativeLayoutLayoutParams = 
                             new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
 
         final AdView adView = new AdView(this);
         adView.refreshDrawableState();
         adView.setVisibility(View.VISIBLE);
         adView.requestFreshAd();
         adView.setRequestInterval(30);
 
         this.mRenderSurfaceView = new RenderSurfaceView(this);
         mRenderSurfaceView.setRenderer(mEngine);
 
         final android.widget.RelativeLayout.LayoutParams surfaceViewLayoutParams = 
                                new RelativeLayout.LayoutParams(super.createSurfaceViewLayoutParams());
         surfaceViewLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
 
         final android.widget.RelativeLayout.LayoutParams adViewLayoutParams=
                            new RelativeLayout.LayoutParams(super.createSurfaceViewLayoutParams());
         
         adViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
         
         adViewLayoutParams.height=LayoutParams.WRAP_CONTENT;
         adViewLayoutParams.width=LayoutParams.FILL_PARENT;
         
         relativeLayout.addView(this.mRenderSurfaceView, surfaceViewLayoutParams);
         relativeLayout.addView(adView, adViewLayoutParams);
 
         this.setContentView(relativeLayout, relativeLayoutLayoutParams);    
     }
 
     protected boolean isShowInCenter() {
         return showInCenter;
     }
 
     protected void setShowInCenter(boolean showInCenter) {
         this.showInCenter = showInCenter;
     }
 
     protected boolean isTestMode() {
         return testMode;
     }
 
     protected void setTestMode(boolean testMode) {
         this.testMode = testMode;
     }
 
     
     public void playMarbleSound()
     {
         mMarbleSound.play();
     }
 
     
 
 
     
 
 }
