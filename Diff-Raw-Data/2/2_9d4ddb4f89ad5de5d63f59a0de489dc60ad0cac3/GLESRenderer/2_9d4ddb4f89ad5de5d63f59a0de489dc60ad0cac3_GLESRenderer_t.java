 package net.votebrian.games.sof;
 
 import javax.microedition.khronos.opengles.GL10;
 import javax.microedition.khronos.egl.EGLConfig;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 
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
 
     public final int SS_SUNLIGHT = GL10.GL_LIGHT0;
 
     private boolean relBtnsEnabled = false;
 
     private int mSelectionFail = 0;
     // 0:  Ready for selection
     // 1:  Selection Failed. Deal disabled until cards cleared.
     // 2:  Cards cleared.  Reset counter
 
     public GLESRenderer(Context context) {
         mCtx = context;
         gbl = (Global) context.getApplicationContext();
         handler = new Handler();
 
 
         // Shared Preferences Setup
         mRes = context.getResources();
 
         mSettings = mCtx.getSharedPreferences( mRes.getString(R.string.prefs), Context.MODE_PRIVATE);
         mEditor = mSettings.edit();
 
         // Default Preferences
         zeroCounter();
         resetFailed();
     }
 
     public void onSurfaceCreated(GL10 gl, EGLConfig config) {
         setDisplayProperties(gl);
         initLighting(gl);
 
         mDeck = new Deck(mCtx, gl);
         mOverlayBtns = new Buttons(mCtx, gl);
         mPass = new PassButton(mCtx, gl);
     }
 
     public void onDrawFrame(GL10 gl) {
         gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
 
         gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
         gl.glLoadIdentity();
 
         // rotate card table
         gl.glRotatef(-20f, 1f, 0f, 0f);
         gl.glRotatef(5, 0f, 1f, 0f);
         gl.glRotatef( -10f, 0f, 0f, 1f);
         gl.glTranslatef(1f, 3f, 0f);
 
         mDeck.draw(gl);
 
 
         // undo rotate for buttons/overlays
         gl.glTranslatef(-1f, -3f, 0f);
         gl.glRotatef(10f, 0f, 0f, 1f);
         gl.glRotatef(-5f, 0f, 1f, 0f);
         gl.glRotatef(20f, 1f, 0f, 0f);
 
         mOverlayBtns.draw(gl);
         mPass.draw(gl);
     }
 
     public void onSurfaceChanged(GL10 gl, int width, int height) {
         mViewW = width;
         mViewH = height;
 
         setDisplayProperties(gl);
         setProjection(gl);
 
         mOverlayBtns.setVertices(mViewW, mViewH, mViewAngle);
         mPass.setVertices(mViewW, mViewH, mViewAngle);
     }
 
     private void setProjection(GL10 gl) {
         float ratio = (float) mViewW / (float) mViewH;
 
         // determine the "half-width" and "half-height" of our view at the near cutoff Z value stuff
         // stuff stuff
         mNearH = (float) (mNearZ * (Math.tan(Math.toRadians(mViewAngle))));
         mNearW = mNearH * ratio;
 
         // Define orthographic projection
         gl.glMatrixMode(GL10.GL_PROJECTION);
         gl.glFrustumf(-mNearW, mNearW, -mNearH, mNearH, mNearZ, mFarZ);
         gl.glViewport(0, 0, mViewW, mViewH);
 
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
                 }
                 break;
 
             case EVENT_MOVE:
                 if(mSelectionFail == 1) {
                     // do nothing
                 } else {
                     // move highlight to correct button
                     mOverlayBtns.settle();
                     if( (region < 2) || relBtnsEnabled ) {
                         mOverlayBtns.highlightBtn(region);
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
         mOverlayBtns.enableAbsolute();
 
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
 
         if( (y > (mViewH * 0.9)) && (x > (mViewW * 0.75)) ) {
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
 }
