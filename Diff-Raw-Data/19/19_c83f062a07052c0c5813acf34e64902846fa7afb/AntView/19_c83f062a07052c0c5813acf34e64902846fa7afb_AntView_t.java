 package com.howfun.android.antguide;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.os.Handler;
 import android.util.AttributeSet;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 import com.howfun.android.HF2D.Pos;
 
 public class AntView extends SurfaceView implements SurfaceHolder.Callback {
 
    private static final String TAG = "AntView";
 
    private float touchDownX = 10;
 
    private float touchDownY = 10;
 
    private float touchUpX = 200;
 
    private float touchUpY = 300;
 
    private boolean mShowBlockLine;
 
    UpdateThread updateThread;
 
    private CanvasManager mCanvasManager;
 
    private Context mContext;
 
    public AntView(Context context) {
 
       super(context);
 
       mContext = context;
 
       getHolder().addCallback(this);
 
       mCanvasManager = new CanvasManager(mContext);
 
    }
 
    public AntView(Context context, AttributeSet attrs) {
       super(context, attrs);
 
       mContext = context;
 
       getHolder().addCallback(this);
 
       mCanvasManager = new CanvasManager(mContext);
 
    }
   
   public void init(Handler handler){
      if(mCanvasManager == null){
         mCanvasManager = new CanvasManager(mContext);
         mCanvasManager.setHandler(handler);
      }
   }
 
 
    public void setHandler(Handler handler) {
       if (mCanvasManager != null) {
          mCanvasManager.setHandler(handler);
       }
    }
 
    public void setDownPos(float x, float y) {
       touchDownX = x;
       touchDownY = y;
    }
 
    public void setUpPos(float x, float y) {
       touchUpX = x;
       touchUpY = y;
    }
 
    public void showBlockLine() {
       mShowBlockLine = true;
 
    }
 
    @Override
    protected void onDraw(Canvas canvas) {
       if (mCanvasManager == null) {
          return;
       }
       if (mShowBlockLine) {
 
          mCanvasManager.setNewLine(new Pos(touchDownX, touchDownY), new Pos(
                touchUpX, touchUpY));
          mShowBlockLine = false;
       }
 
       mCanvasManager.draw(canvas);
 
    }
 
    public void setWhichAntAnim(int which) {
       if (mCanvasManager != null) {
          mCanvasManager.setWhichAntAnim(which);
       }
    }
 
    /**
     * starts a new game
     */
    public void playGame() {
       if (mCanvasManager == null) {
          Utils.log(TAG, "canvasManager is null");
          // mCanvasManager = new CanvasManager(mContext);
          return;
       }
       mCanvasManager.initAllSprite();
       if (updateThread != null) {
          updateThread.startUpdate();
       }
    }
 
    /**
     * continue the game if it is paused
     */
    public void resumeGame() {
       updateThread.startUpdate();
    }
 
    /**
     * pause the game,u can continue the game by func continueGame() if pause btn
     * clicked or activity onpaused
     */
    public void pauseGame() {
       updateThread.stopUpdate();
    }
 
    /**
     * when ant gets home or lost,this func will execute
     */
    public void stopGame() {
       // TODO
       updateThread.stopUpdate();
    }
 
    public void surfaceCreated(SurfaceHolder holder) {
 
       Utils.log(TAG, "surfaceCreated()....");
       if (mCanvasManager != null) {
          mCanvasManager.initAllSprite();
       }
       updateThread = new UpdateThread(this);
 
       updateThread.setRunning(true);
 
       updateThread.start();
 
    }
 
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
          int border) {
       Utils.log(TAG, "surfaceChanged()....");
 
    }
 
    public void surfaceDestroyed(SurfaceHolder holder) {
 
       Utils.log(TAG, "surfaceDestroyed()....");
 
       // TODO: save running data if press HOME, and enter into pause mode
 
       if (mCanvasManager != null) {
          mCanvasManager.clear();
          mCanvasManager = null;
       }
 
       boolean retry = true;
 
       updateThread.setRunning(false);
 
       while (retry) {
 
          try {
 
             updateThread.join();
 
             retry = false;
 
          } catch (InterruptedException e) {
 
          }
 
       }
 
    }
 
 }
