 package se.otaino2.breakoutgame;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import se.otaino2.breakoutgame.model.Background;
 import se.otaino2.breakoutgame.model.Block;
 import se.otaino2.breakoutgame.model.Dot;
 import se.otaino2.breakoutgame.model.Entity;
 import se.otaino2.breakoutgame.model.Paddle;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnTouchListener;
 
 public class BreakoutBoardView extends SurfaceView implements SurfaceHolder.Callback, OnTouchListener {
 
     private static final String TAG = "BreakoutBoardView";
 
     // Render thread
     private BreakoutBoardThread thread;
     
     // Touch event position for the paddle
     private double lastKnownPaddlePosition;
 
     public BreakoutBoardView(Context context, AttributeSet attrs) {
         super(context, attrs);
 
         SurfaceHolder holder = getHolder();
         holder.addCallback(this);
 
         // SurfaceView must have focus to get touch events
         setFocusable(true);
         setOnTouchListener(this);
     }
 
     @Override
     public void surfaceCreated(SurfaceHolder holder) {
         Log.d(TAG, "Surface created, starting new thread...");
         thread = new BreakoutBoardThread(holder);
         thread.setRunning(true);
         thread.start();
     }
 
     @Override
     public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
         Log.d(TAG, "Surface changed, resetting game...");
         thread.setSurfaceSize(width, height);
         thread.reset();
     }
 
     @Override
     public void surfaceDestroyed(SurfaceHolder holder) {
         Log.d(TAG, "Surface destroyed, trying to shut down thread...");
         boolean retry = true;
         thread.setRunning(false);
         while (retry) {
             try {
                 thread.join();
                 retry = false;
             } catch (InterruptedException e) {
                 // Do nothing
             }
         }
         Log.d(TAG, "Thread's dead, baby. Thread's dead.");
     }
 
     @Override
     public boolean onTouch(View v, MotionEvent event) {
         switch (event.getAction()) {
             case MotionEvent.ACTION_MOVE:
                 lastKnownPaddlePosition = event.getX();
         }
         return true;
     }
 
     public BreakoutBoardThread getThread() {
         return thread;
     }
 
     class BreakoutBoardThread extends Thread {
 
         // Debug 
         private static final String TAG = "BreakoutBoardThread";
         
         // Game constants
         private static final int NBR_OF_BLOCKS = 6;
         private static final double DOT_SPEED = 400.0; 
         
         private SurfaceHolder surfaceHolder;
         private boolean running;
         private int canvasWidth;
         private int canvasHeight;
         private Background background;
         private long lastTime;
         
         // Entities
         private List<Entity> gameEntities;
         private ArrayList<Block> blocks;
         private ArrayList<Block> destroyedBlocks;
         private Paddle paddle;
         private Dot dot;
 
         public BreakoutBoardThread(SurfaceHolder surfaceHolder) {
             this.surfaceHolder = surfaceHolder;
         }
 
         public void setRunning(boolean running) {
             this.running = running;
         }
 
         public void reset() {
             synchronized (surfaceHolder) {
                 resetEntities(canvasWidth, canvasHeight);               
             }
         }
 
         @Override
         public void run() {
             while (running) {
                 Canvas c = null;
                 try {
                     c = surfaceHolder.lockCanvas(null);
                     synchronized (surfaceHolder) {
                         updatePhysics();
                         // NOTE: In newer versions of Android (4+), it seems SurfaceHolder.lockCanvas() may return null whenever
                         // SurfaceHolder.Callback.surfaceDestroyed() has been invoked. In earlier versions, a canvas was always
                         // returned until SurfaceHolder.Callback.surfaceDestroyed() was FINISHED. See bug report:
                         // https://code.google.com/p/android/issues/detail?id=38658
                         if (c != null) {
                             doDraw(c);
                         }
                     }
                 } finally {
                     // do this in a finally so that if an exception is thrown
                     // during the above, we don't leave the Surface in an
                     // inconsistent state
                     if (c != null) {
                         surfaceHolder.unlockCanvasAndPost(c);
                     }
                 }
             }
         }
 
         public void setSurfaceSize(int width, int height) {
             synchronized (surfaceHolder) {
                 canvasWidth = width;
                 canvasHeight = height;
                 reset();
             }
         }
 
         private void resetEntities(int width, int height) {
             background = new Background(width, height);
             gameEntities = new ArrayList<Entity>();
             blocks = new ArrayList<Block>();
             destroyedBlocks = new ArrayList<Block>();
             double blockWidth = width / (1.1 * NBR_OF_BLOCKS + 0.1);
             double blockHeight = 0.1 * blockWidth;
             for (int i = 0; i < NBR_OF_BLOCKS; i++) {
                 double blockX = i * (blockWidth + blockHeight) + blockHeight;
                 double blockY = 2 * blockHeight;
                 Block b = new Block((int) blockX, (int) blockY, (int) blockWidth, (int) blockHeight);
                 blocks.add(b);
                 gameEntities.add(b);
             }
             
             double paddleWidth = blockWidth * 2;
             double paddleHeight = blockHeight * 2;
             double paddleX = (width - paddleWidth) / 2;
             double paddleY = height - paddleHeight;
             paddle = new Paddle((int) paddleX, (int) paddleY, (int) paddleWidth, (int) paddleHeight);
             // TODO: Thread should not meddle with properties of the view. Refactor...
             lastKnownPaddlePosition = paddle.getX();
             gameEntities.add(paddle);
 
             double dotSide = blockHeight;
             double dotX = paddleX + (paddleWidth - dotSide) / 2;
             double dotY = paddleY - dotSide;
             double startAngle = Math.PI * Math.random() * -0.25; // Starting angle should be somewhat upwards
             Log.d(TAG, "seed:" + startAngle);
             double vx = DOT_SPEED * Math.sin(startAngle);
             double vy = DOT_SPEED * Math.cos(startAngle);
             Log.d(TAG, "vx:" + vx + ", vy:" + vy);
             dot = new Dot((int) dotX, (int) dotY, (int) dotSide, (int) dotSide, vx, vy);
             gameEntities.add(dot);
         }
 
         // Update game entities for next iteration
         private void updatePhysics() {
             long now = System.currentTimeMillis();
 
             // Make sure we don't update physics unnecessary often
             if (lastTime > now)
                 return;
 
             double elapsed = (now - lastTime) / 1000.0;
             
             // Update paddle position
             paddle.move(lastKnownPaddlePosition);
             
             // Update dot position
             double dx = dot.getVx() * elapsed;
             double dy = dot.getVy() * elapsed;
             dot.move(dx, dy, dot.getVx(), dot.getVy());
             
             // Check if dot collides with paddle
             if (dot.isColliding(paddle)) {
                // New direction depends on where the dot hits the paddle
                dot.move(-dx, -dy, dot.getVx(), -dot.getVy());
             }
             
             // Check if dot hits block
             Iterator<Block> iter = blocks.iterator();
             while (iter.hasNext()) {
                 Block b = iter.next();
                 if (dot.isColliding(b)) {
                     iter.remove();
                     gameEntities.remove(b);
                     destroyedBlocks.add(b);
                    dot.move(-dx, -dy, dot.getVx(), -dot.getVy());
                 }
             }
             
             // Check if dot hits walls
             if (dot.getX() < 0 || dot.getX() > canvasWidth) {
                dot.move(-dx, -dy, -dot.getVx(), dot.getVy());
             }
             if (dot.getY() < 0) {
                dot.move(-dx, -dy, dot.getVx(), -dot.getVy());
             }
             
             // Check if game over
             if (dot.getY() > canvasHeight) {
                 reset();
             }
             
             lastTime = now;
         }
 
         // Draws game entities on canvas. Must be run in
         private void doDraw(Canvas c) {
             renderBackground(c);
             renderEntities(c);
         }
 
         // Render the board background.
         private void renderBackground(Canvas c) {
             c.drawRect(background.getRect(), background.getPaint());
         }
 
         private void renderEntities(Canvas c) {
             for (Entity e : gameEntities) {
                 c.drawRect(e.getRect(), e.getPaint());
             }
 
             Iterator<Block> iter = destroyedBlocks.iterator();
             while (iter.hasNext()) {
                 Block b = iter.next();
                 Paint p = b.getPaint();
                 p.setAlpha(p.getAlpha() - 5);
                 c.drawRect(b.getRect(), p);
                 if (p.getAlpha() == 0) {
                     iter.remove();
                 }
             }
         }
     }
 }
