 package in.liquidmetal.dubsteptetris;
 
 import android.opengl.GLES20;
 import android.opengl.GLSurfaceView;
 import android.opengl.Matrix;
 import android.os.ConditionVariable;
 import android.support.v4.view.GestureDetectorCompat;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 /**
  * Created by utkarsh on 26/5/13.
  */
 public class GameSurfaceRenderer implements GLSurfaceView.Renderer, GestureDetector.OnGestureListener {
 
     // Member variables used to keep track of the viewport
     static float mProjectionMatrix[] = new float[16];       // TODO Why was it 'final' in the original code?
     private int mViewportWidth, mViewportHeight;
     private int mViewportXoff, mViewportYoff;
 
     private GestureDetectorCompat mDetector;
 
     private GameSurfaceView mSurfaceView;
     private GameState mGameState;
     private AlignedRect rect;
     private float oldX;
 
     private boolean bSurfaceCreated = false;
 
     private GLText textScore, textStatic;
 
     private LinkedList<TexturedAlignedRect> tempTextureObjects = new LinkedList<TexturedAlignedRect>();
     private LinkedList<Animator> animatorObjects = new LinkedList<Animator>();
 
     private LinkedList<TexturedAlignedRect> destroyList = new LinkedList<TexturedAlignedRect>();
 
     public GameSurfaceRenderer(GameState gameState, GameSurfaceView gameSurfaceView) {
         mGameState = gameState;
         mSurfaceView = gameSurfaceView;
 
         mDetector = new GestureDetectorCompat(gameSurfaceView.getContext(), this);
     }
 
     @Override
     public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
         AlignedRect.createProgram();
         GLText.createProgram();
 
         textScore = new GLText(300, 300, "ABCD", 100, 0, 0);
         textScore.setPosition(700, 1000);
 
         textStatic = new GLText(300, 300, "static", 100, 0, 0);
         textStatic.setPosition(700, 700);
         textStatic.setAlphaMultiplier(0.5f);
 
         GLES20.glDisable(GLES20.GL_CULL_FACE);
         bSurfaceCreated = true;
     }
 
     @Override
     public void onSurfaceChanged(GL10 gl10, int width, int height) {
         float arenaRatio = GameState.ARENA_HEIGHT/ GameState.ARENA_WIDTH;
         int x, y, viewWidth, viewHeight;
 
         // Limited by width?
         if(height > (int)(width*arenaRatio)) {
             viewWidth = width;
             viewHeight = (int)(width*arenaRatio);
             x = 0;
             y = (height - viewHeight) / 2;
         } else {
             viewHeight = height;
             viewWidth = (int)(height / arenaRatio);
             x = (width - viewWidth) / 2;
             y = 0;
         }
 
         // Setup the OpenGL viewport based on these calculations
         GLES20.glViewport(x, y, viewWidth, viewHeight);
 
         mViewportHeight = viewHeight;
         mViewportWidth = viewWidth;
         mViewportXoff = x;
         mViewportYoff = y;
 
         // Now, setup an orthographic projection
         Matrix.orthoM(mProjectionMatrix, 0, 0, GameState.ARENA_WIDTH, 0, GameState.ARENA_HEIGHT, -1.0f, 1.0f);
     }
 
     private void updateAnimators() {
         for(Animator a:animatorObjects) {
             a.update();
         }
     }
 
     @Override
     public void onDrawFrame(GL10 gl10) {
         mGameState.calculateNextFrame();
         updateAnimators();
 
         mGameState.clearScreen();
 
         mGameState.drawBoard();
         mGameState.drawNextPiece();
 
         // Draw things that require blending
         GLES20.glEnable(GLES20.GL_BLEND);
         GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
         textScore.draw();
         textStatic.draw();
         for(TexturedAlignedRect r:tempTextureObjects) {
             r.draw();
         }
         GLES20.glDisable(GLES20.GL_BLEND);
 
         mGameState.updateRenderTime();
 
         // Get rid of objects we can delete
         gcAnimators();
         gcTempObjects();
 
         // After everything's done, swap buffers
         mSurfaceView.requestRender();
     }
 
     public void touchEvent(MotionEvent e) {
         boolean used = mDetector.onTouchEvent(e);
         if(used)
             return;
 
         switch(e.getAction()) {
             case MotionEvent.ACTION_MOVE:
                 mGameState.signalMotion((int)e.getX(), (int)e.getY());
                 break;
 
             case MotionEvent.ACTION_DOWN:
                 mGameState.signalDown((int)e.getX(), (int)e.getY());
                 break;

            case MotionEvent.ACTION_UP:
                mGameState.signalUp((int)e.getX(), (int)e.getY());
                break;
         }
 
     }
 
     public void OnScoreChange(int change) {
         if(bSurfaceCreated && change > 0) {
             // Update the text displayed on the scoreboard
             textScore.setText("" + mGameState.getScore());
 
             // Create a new piece of text
             GLText animatedText = new GLText(255, 63, "+" + change, 72, 0, 0);
             animatedText.setPosition(650, 200);
             tempTextureObjects.add(animatedText);
 
             PositionAnimator pa = new PositionAnimator(1000, 0, 200);
             pa.setObjectToAnimate((TexturedAlignedRect)animatedText);
             pa.start();
 
             DestroyAnimator da = new DestroyAnimator(1000, this);
             da.addObject(animatedText);
             da.start();
 
             AlphaAnimator aa = new AlphaAnimator(1000, animatedText, 0.0f);
             aa.start();
 
             animatorObjects.add(pa);
             animatorObjects.add(da);
             animatorObjects.add(aa);
         }
     }
 
     private void gcTempObjects() {
         Iterator<TexturedAlignedRect> destroyIterator = destroyList.iterator();
         while(destroyIterator.hasNext()) {
             TexturedAlignedRect theObj = destroyIterator.next();
             destroyIterator.remove();
             theObj = null;
         }
     }
 
     private void gcAnimators() {
         Iterator<Animator> animIterator = animatorObjects.iterator();
         while(animIterator.hasNext()) {
             Animator tempObj = animIterator.next();
             if(tempObj.canGarbageCollect()) {
                 animIterator.remove();
             }
         }
     }
 
     public void deleteTempObject(TexturedAlignedRect r) {
         destroyList.add(r);
     }
 
     public void onViewPause(ConditionVariable syncObj) {
         // Save game state
 
         syncObj.open();
     }
 
     @Override
     public boolean onDown(MotionEvent motionEvent) {
         return false;
     }
 
     @Override
     public void onShowPress(MotionEvent motionEvent) {
 
     }
 
     @Override
     public boolean onSingleTapUp(MotionEvent motionEvent) {
         mGameState.handleRotation((int)motionEvent.getX());
         return true;
     }
 
     @Override
     public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float distanceX, float distanceY) {
         //mGameState.signalTranslate((int)(motionEvent2.getX() - motionEvent.getX()));
         return false;
     }
 
     @Override
     public void onLongPress(MotionEvent motionEvent) {
 
     }
 
     @Override
     public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
         return false;
     }
 }
