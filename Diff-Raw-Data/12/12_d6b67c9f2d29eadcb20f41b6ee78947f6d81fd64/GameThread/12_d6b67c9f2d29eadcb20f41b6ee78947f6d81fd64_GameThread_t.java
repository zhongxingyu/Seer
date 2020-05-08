 package com.manico.android_jetpack;
 
 import android.content.res.AssetManager;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.graphics.Rect;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.KeyEvent;
 import android.view.View;
 
 import java.io.InputStream;
 import java.io.IOException;
 
 public class GameThread extends Thread
 {
     private static final String TAG = "GameThread";
 
     private SurfaceHolder mHolder;
     private View mView;
     private Game mGame;
 
     private InputHandler mInput;
 
     private boolean mRunning = false;
 
     private Paint mBgPaint;
     private Paint mTextPaint;
     private Paint mPlayerPaint;
 
     private Bitmap mTilesBitmap;
     private Bitmap mEntitiesBitmap;
 
     private int mFps;
 
     private static int TILE_SIZE_SCREEN = 32;
 
     public GameThread(SurfaceHolder holder, View view, Game game)
     {
         mHolder = holder;
         mView = view;
         mGame = game;
 
         mInput = new InputHandler(view);
 
         mBgPaint = new Paint();
         mBgPaint.setColor(0xff000000);
 
         mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
         mTextPaint.setColor(0xff00ff00);
         mTextPaint.setTextSize(15);
 
         mPlayerPaint = new Paint();
         mPlayerPaint.setColor(0xff00ff00);
 
         try {
             AssetManager assets = GameApp.getContext().getAssets();
             InputStream stream = assets.open("tiles.png");
             mTilesBitmap = BitmapFactory.decodeStream(stream);
             stream.close();
 
             stream = assets.open("entities.png");
             mEntitiesBitmap = BitmapFactory.decodeStream(stream);
             stream.close();
         } catch (IOException e) {
             Log.d("AndroidJetpack", "IOException occurred while loading tiles.png");
         }
     }
 
     @Override
     public void run()
     {
         long current = System.currentTimeMillis();
         long old;
         double dtime;
 
         long last_fps_update = current;
         int frames = 0;
 
         Canvas canvas;
 
         while (mRunning) {
             old = current;
             current = System.currentTimeMillis();
             dtime = (current - old) / 1000.0;
 
             if(current - last_fps_update > 1000) {
                 mFps = frames;
                 frames = 0;
                 last_fps_update = current;
             }
             frames++;
 
             canvas = null;
             try {
                 canvas = mHolder.lockCanvas();
                 synchronized (mHolder) {
                     tick(dtime);
                     render(canvas);
                 }
             } finally {
                 if (canvas != null) {
                     mHolder.unlockCanvasAndPost(canvas);
                 }
             }
         }
     }
 
     public void setRunning(boolean b)
     {
         mRunning = b;
     }
 
     public void tick(double dtime)
     {
         Player p = mGame.getPlayer();
 
         boolean left  = mInput.isKeyDown(KeyEvent.KEYCODE_DPAD_LEFT);
         boolean right = mInput.isKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT);
         boolean jump = mInput.isKeyDown(KeyEvent.KEYCODE_DPAD_CENTER);
 
         p.tick(dtime, left, right, jump);
     }
 
     private int vpx;
     private int vpy;
 
     public void render(Canvas canvas)
     {
         Player p = mGame.getPlayer();
 
         canvas.drawPaint(mBgPaint);
 
         vpx = p.getX() + (TILE_SIZE_SCREEN / 2) - (canvas.getWidth() / 2);
         vpy = p.getY() + (TILE_SIZE_SCREEN / 2) - (canvas.getHeight() / 2);
 
         renderLevel(canvas);
         renderPlayer(canvas, p);
 
         canvas.drawText("FPS: " + mFps, 5, 15, mTextPaint);
     }
 
     private Rect src = new Rect(0, 0, 16, 16);
     private Rect dst = new Rect();
 
     public void renderLevel(Canvas canvas)
     {
         Level lvl = mGame.getLevel();
 
        int vpx_tile = Math.max(0, vpx / 32);
        int vpy_tile = Math.max(0, vpy / 32);
        int vpw_tile = Math.min(lvl.getWidth(),  vpx_tile + canvas.getWidth() / 32 + 1);
        int vph_tile = Math.min(lvl.getHeight(), vpy_tile + canvas.getHeight() / 32);

        for (int x = vpx_tile; x < vpw_tile; x++) {
            for (int y = vpy_tile; y < vph_tile; y++) {
                 int tile = lvl.getTile(x, y);
 
                 if (Tile.isWall(tile)) {
                     src = Tile.getTextureBounds(tile);
 
                     dst.left   = x * TILE_SIZE_SCREEN - vpx;
                     dst.top    = y * TILE_SIZE_SCREEN - vpy;
                     dst.right  = dst.left + TILE_SIZE_SCREEN;
                     dst.bottom = dst.top  + TILE_SIZE_SCREEN;
 
                     canvas.drawBitmap(mTilesBitmap, src, dst, null);
                 }
             }
         }
     }
 
     public void renderPlayer(Canvas canvas, Player p)
     {
         src = p.getTextureBounds();
 
         dst.left   = (int) (p.getX() - vpx);
         dst.top    = (int) (p.getY() - vpy);
         dst.right  = dst.left + src.width();
         dst.bottom = dst.top  + src.height();
 
         canvas.drawBitmap(mEntitiesBitmap, src, dst, null);
     }
 }
