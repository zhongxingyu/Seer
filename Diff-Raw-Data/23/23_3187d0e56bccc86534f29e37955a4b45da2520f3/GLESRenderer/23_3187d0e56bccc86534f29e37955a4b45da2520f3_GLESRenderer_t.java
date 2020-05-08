 /*
  *  Copyright 2012 Brian Flores
  *
  *  This file is part of SmokeOrFire.
  *
  *  SmokeOrFire is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  SmokeOrFire is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.votebrian.games.sof;
 
 import javax.microedition.khronos.opengles.GL10;
 import javax.microedition.khronos.egl.EGLConfig;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 
 import java.util.HashMap;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.media.AudioManager;
 import android.media.SoundPool;
 
 import android.opengl.GLSurfaceView;
 import android.opengl.GLSurfaceView.Renderer;
 
 import android.os.Handler;
 
 import android.util.Log;
 
 class GLESRenderer
         implements GLSurfaceView.Renderer {
     private Global gbl;
     private Context mCtx;
     private Deck mDeck;
     private Buttons mOverlayBtns;
     private PassButton mPass;
 
     private Handler handler;
 
     private Resources mRes;
 
     public SharedPreferences         mSettings;
     public SharedPreferences.Editor mEditor;
 
     private SoundPool mSoundPool;
     private AudioManager mAudioManager;
     private HashMap<Integer, Integer> mSoundPoolMap;
 
     private final int EVENT_DOWN = 0;
     private final int EVENT_MOVE = 1;
     private final int EVENT_UP   = 2;
 
     private ByteBuffer mLbb;
     private FloatBuffer mLinesBuffer;
     private int mNumLines = 0;
     private int POINTS_PER_LINE = 3;
     private int BYTES_PER_POINT = 4;
     private float[] mLineVert = new float[24];
 
     private int mViewW = 0;
     private int mViewH = 0;
     private float mViewAngle = 10f;
 
     private float mNearH = 0f;
     private float mNearW = 0f;
     private float mNearZ = 5f;
     private float mFarZ  = 15f;
 
     private FloatBuffer mPositionBuffer;
     private FloatBuffer mDiffuseBuffer;
 
     private float mXAngle = 0;
     private float mYAngle = 0;
 
     private int mCurrRegion = -1;
 
     public final int SS_SUNLIGHT = GL10.GL_LIGHT0;
 
     private boolean relBtnsEnabled = false;
 
     private int mSelectionFail = 0;
     // 0:  Ready for selection
     // 1:  Selection Failed. Deal disabled until cards cleared.
     // 2:  Cards cleared.  Reset counter
 
     public GLESRenderer(Context context) {
         mCtx = context;
         handler = new Handler();
 
         mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
         mAudioManager = (AudioManager) mCtx.getSystemService(Context.AUDIO_SERVICE);
         mSoundPoolMap = new HashMap();
     }
 
     @Override
     public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 
         gbl = (Global) mCtx.getApplicationContext();
 
 
         // Shared Preferences Setup
         mRes = mCtx.getResources();
 
         mSettings = mCtx.getSharedPreferences( mRes.getString(R.string.prefs), Context.MODE_PRIVATE);
         mEditor = mSettings.edit();
 
         // Load chime
         mSoundPoolMap.put(1, mSoundPool.load(mCtx, R.raw.audio_chime, 1));
 
         // Default Preferences
         zeroCounter();
         resetFailed();
         mSelectionFail = 0;
 
         // Create Models
         mDeck = new Deck(mCtx, gl);
         mOverlayBtns = new Buttons(mCtx, gl);
         mPass = new PassButton(mCtx, gl);
 
         blink();
     }
 
     @Override
     public void onDrawFrame(GL10 gl) {
         gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
 
         gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
 

        gl.glPushMatrix();

         // rotate card table
         gl.glRotatef(-20f, 1f, 0f, 0f);
        gl.glRotatef(  5f, 0f, 1f, 0f);
        gl.glRotatef(-10f, 0f, 0f, 1f);
        gl.glTranslatef( 1f, 3f, 0f);
 
         mDeck.draw(gl);
 
        gl.glPopMatrix();
 

        gl.glPushMatrix();
 
         mOverlayBtns.draw(gl);
         mPass.draw(gl);

        gl.glPopMatrix();
     }
 
     @Override
     public void onSurfaceChanged(GL10 gl, int width, int height) {
         mViewW = width;
         mViewH = height;
 
         initLighting(gl);
 
         setDisplayProperties(gl);
         setProjection(gl);
 
         mOverlayBtns.setVertices(mViewW, mViewH, mViewAngle);
         mPass.setVertices(mViewW, mViewH, mViewAngle);
 
         gl.glLoadIdentity();
     }
 
     private void setProjection(GL10 gl) {
         float ratio = (float) mViewW / (float) mViewH;
 
         // determine the "half-width" and "half-height" of our view at the near cutoff Z value stuff
         // stuff stuff
         mNearH = (float) (mNearZ * (Math.tan(Math.toRadians(mViewAngle))));
         mNearW = mNearH * ratio;
 
         gl.glViewport(0, 0, mViewW, mViewH);
 
         // Define orthographic projection
         gl.glMatrixMode(GL10.GL_PROJECTION);
         gl.glLoadIdentity();
         gl.glFrustumf(-mNearW, mNearW, -mNearH, mNearH, mNearZ, mFarZ);
 
         gl.glMatrixMode(GL10.GL_MODELVIEW);
     }
 
     private void setDisplayProperties(GL10 gl) {
         // Set background color
         gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
         gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
 
         // Set to remove CW triangles
         gl.glEnable(GL10.GL_CULL_FACE);
         gl.glFrontFace(GL10.GL_CCW);
         gl.glCullFace(GL10.GL_BACK);
 
         gl.glClearDepthf(1.0f);
         gl.glEnable(GL10.GL_DEPTH_TEST);
         gl.glDepthFunc(GL10.GL_LEQUAL);
 
         // set blend parameter
         gl.glEnable(GL10.GL_BLEND);
         gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 
         gl.glDisable(GL10.GL_COLOR_MATERIAL);
     }
 
     private void initLighting(GL10 gl) {
         float[] diffuse = { 1.0f, 1.0f, 1.0f, 1.0f };
         ByteBuffer mDbb = ByteBuffer.allocateDirect( diffuse.length * 4 );
         mDbb.order(ByteOrder.nativeOrder());
         mDiffuseBuffer = mDbb.asFloatBuffer();
         mDiffuseBuffer.put(diffuse);
         mDiffuseBuffer.position(0);
 
         float[] pos = { 0.0f, 0.0f, 0.0f, 1.0f };
         ByteBuffer mPbb = ByteBuffer.allocateDirect( pos.length * 4 );
         mPbb.order(ByteOrder.nativeOrder());
         mPositionBuffer = mPbb.asFloatBuffer();
         mPositionBuffer.put(pos);
         mPositionBuffer.position(0);
 
         gl.glLightfv(SS_SUNLIGHT, GL10.GL_POSITION, mPositionBuffer);
         gl.glLightfv(SS_SUNLIGHT, GL10.GL_DIFFUSE, mDiffuseBuffer);
         // gl.glShadeModel(GL10.GL_SMOOTH);
         // gl.glEnable(GL10.GL_LIGHTING);
         gl.glDisable(GL10.GL_LIGHTING);
         // gl.glEnable(SS_SUNLIGHT);
     }
 
     public void buttonEvent(float x, float y, int event) {
         // determine region of button event
         // 0: SMOKE
         // 1: FIRE
         // 2: HIGHER
         // 3: LOWER
         int region = regionCalc(x, y);
 
         switch(event) {
             case EVENT_DOWN:
                 if(mSelectionFail == 1) {
                     // do nothing
                 } else if(region == Global.PASS) {
                     // do nothing
                 } else if( (region < 2) || relBtnsEnabled ) {
                     // highlight button
                     mOverlayBtns.highlightBtn(region);
                     mCurrRegion = region;
                 }
                 break;
 
             case EVENT_MOVE:
                 if(mSelectionFail == 1) {
                     // do nothing
                 } else {
                     // move highlight to correct button
                     // mOverlayBtns.settle();
                     if( (region < 2) || relBtnsEnabled ) {
                         if(region == mCurrRegion) {
                             // do nothing
                         } else {
                             mOverlayBtns.highlightBtn(region);
                             mCurrRegion = region;
                         }
                     }
                 }
                 break;
 
             case EVENT_UP:
                 if(mSelectionFail == 1) {
                     clearTable();
                 } else if(region == Global.PASS) {
                     // User has pressed the pass button on the bottom right
                     int temp_count = mSettings.getInt( mRes.getString(R.string.counter_pref), -1);
                     if(temp_count > 4) {
                         mPass.reset();
                     }
                 } else if( (region < 2) || relBtnsEnabled ) {
                     // remove highlight after a slight delay
                     handler.postDelayed( new Runnable() {
                         public void run() {
                             mOverlayBtns.settle();
                         }
                     }, 100);
                     mCurrRegion = -1;
 
                     // deal card and determine outcome
                     // cards[0] is the previous card
                     // cards[1] is the current card
                     int[] cards = mDeck.deal();
                     int result = Global.BAD;
 
                     if(region < 2) {
                         // absolute selection
                         if( (region == Global.SMOKE) && (Deck.getSuit(cards[1]) > 1) ) {
                             result = Global.GOOD;
                         } else if( (region == Global.FIRE) && (Deck.getSuit(cards[1]) < 2) ) {
                             result = Global.GOOD;
                         }
                     } else {
                         // relative selection
                         int diff = Deck.getValue(cards[1]) - Deck.getValue(cards[0]);
                         if(diff == 0) {
                             result = Global.SOCIAL;
                         } else if(diff > 0 && region == Global.HIGHER) {
                             result = Global.GOOD;
                         } else if (diff < 0 && region == Global.LOWER) {
                             result = Global.GOOD;
                         }
                     }
 
 
                     switch(result) {
                         case Global.BAD:
                             // expand drink counter
 
                             // increment drink counter
                             incrementCounter();
 
                             // indicate fail preference
                             flagFailed();
 
                             // show cards on table
 
                             // disable all selections until cleared
                             mSelectionFail = 1;
 
                             // disable higher/lower selections
                             relBtnsEnabled = false;
                             mOverlayBtns.disableAll();
                             mPass.reset();
                             break;
                         case Global.GOOD:
                             // Check to see if user has earned ad-free preference
                             int temp_count = mSettings.getInt(mRes.getString(R.string.counter_pref), 0);
                             if(temp_count + 1 == Global.AD_FREE_THRESHOLD) {
                                 // play sound
                                 if(mSettings.getInt(mRes.getString(R.string.ad_pref), 0) == 0) {
                                     mSoundPool.play(mSoundPoolMap.get(1), 0.5f, 0.5f, 0, 0, 1f);
                                 }
 
                                 // set preference
                                 mEditor.putInt(mRes.getString(R.string.ad_pref), 1);
                                 mEditor.commit();
                             }
 
                             // increment drink counter
                             incrementCounter();
 
                             // enable higher/lower selections
                             relBtnsEnabled = true;
                             mOverlayBtns.enableRelative();
                             break;
                         case Global.SOCIAL:
                             // highlight drink counter increment
                             incrementCounter();
 
                             // show social splash image
                             // show matching cards
                             break;
                     }
                 }
                 break;
         }
     }
 
     private void clearTable() {
         // clear cards from table
         mDeck.burnTable();
 
         // disable Relative selections
         mOverlayBtns.enableAll();
         mOverlayBtns.disableRelative();
 
         // Set counter to zero
         zeroCounter();
 
         // reset fail indicator
         resetFailed();
 
         mSelectionFail = 0;
     }
 
     private void incrementCounter() {
         int temp_count = mSettings.getInt( mRes.getString(R.string.counter_pref), -1);
         temp_count = temp_count + 1;
         mEditor.putInt( mRes.getString(R.string.counter_pref), temp_count);
         mEditor.commit();
 
         mPass.increment();
     }
 
     private void zeroCounter() {
         mEditor.putInt( mRes.getString(R.string.counter_pref), 0);
         mEditor.commit();
     }
 
     private void flagFailed() {
         mEditor.putBoolean( mRes.getString(R.string.fail_pref), true);
         mEditor.commit();
     }
 
     private void resetFailed() {
         mEditor.putBoolean( mRes.getString(R.string.fail_pref), false);
         mEditor.commit();
     }
 
     private int regionCalc(float x, float y) {
         float slope = (float) mViewH / (float) mViewW;
         float upslope = -1 * slope * (float) x + mViewH;
         float downslope = slope * x;
 
         // Specify touch region for PASS "button"
         // Set to the intersection of the bottom 20% and right-most 30% of screen.
         if( (y > (mViewH * 0.8)) && (x > (mViewW * 0.70)) ) {
             return Global.PASS;
         }
 
         if( y > upslope ) {
             if( y > downslope ) {
                 return Global.LOWER;
             } else {
                 return Global.FIRE;
             }
         } else {
             if( y > downslope ) {
                 return Global.SMOKE;
             } else {
                 return Global.HIGHER;
             }
         }
     }
 
     private void blink() {
         handler.postDelayed(new Runnable() {
             public void run() {
                 mOverlayBtns.highlightAbsolute();
             }
         }, 1000);
 
         handler.postDelayed(new Runnable() {
             public void run() {
                 mOverlayBtns.settle();
             }
         }, 1100);
 
         handler.postDelayed(new Runnable() {
             public void run() {
                 mOverlayBtns.highlightAbsolute();
             }
         }, 1200);
 
         handler.postDelayed(new Runnable() {
             public void run() {
                 mOverlayBtns.settle();
             }
         }, 1300);
 
         handler.postDelayed(new Runnable() {
             public void run() {
                 mOverlayBtns.highlightAbsolute();
             }
         }, 1400);
 
         handler.postDelayed(new Runnable() {
             public void run() {
                 mOverlayBtns.settle();
             }
         }, 1500);
     }
 }
