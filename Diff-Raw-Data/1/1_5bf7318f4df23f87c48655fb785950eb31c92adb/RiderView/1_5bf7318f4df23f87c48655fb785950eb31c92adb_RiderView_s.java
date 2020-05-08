 /**
  * @file RiderView.java
  *
  * RiderBall 画面表示用クラス
  *
  * @version 0.0.1
  *
  * @since 2012/02/01
  * @date  2012/02/01
  */
 package com.android.rider.common;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Cap;
 import android.graphics.PixelFormat;
 import android.graphics.Rect;
 import android.graphics.Region;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 import com.android.rider.R;
 import com.android.rider.util.ImageUtils;
 
 /** RiderBall 表示用 SurfaceView クラス.
  *
  * SurfaceViewを継承し、RiderBall 画面の表示を行う 。\n
  */
 public class RiderView extends SurfaceView implements SurfaceHolder.Callback, Runnable
 {
     /** ログ用TAG. */
     private static final String TAG = "RiderView";
 
     /** 親 Activity. */
     private Activity mParent = null;
     /** 衝突時(ボール同士)の加速度係数. */
     private static double E_HOGE = 0.8;
     /** 全体の加速度係数. */
     private static double D_HOGE = 0.99;
     /** 加速度 x 方向. */
     private float mGx;
     /** 加速度 y 方向. */
     private float mGy;
     /** ペイント. */
     private Paint mPaint;
     /** ホルダー. */
     private SurfaceHolder mHolder;
     /** スレッド. */
     private Thread mThread;
     /** ボール格納用配列. */
     private ArrayList<Circle> mCircleContainer;
     /** 画面幅 . */
     private int mWidth;
     /** 画面高さ. */
     private int mHeight;
     /** ボール消去用フラグ. */
     private Boolean mClearFlag = false;
     /** ゴールポケット用フラグ. */
     private Boolean mGoalFlag = false;
 
     /** 表示用 Bitmap 画像. */
     private Bitmap mBitmap;
     /** オーバーレイ用 Bitmap 画像. */
     private Bitmap mOverLayMap;
     /** オーバーレイ用 Canvas. */
     private Canvas mOverLayCanvas;
     /** オーバーレイ用 Paint. */
     private Paint mOverLayPaint;
     /** オーバーレイ用 Color. */
     private static final int OVERLAY_COLOR = 0xFF444444;
     /** オーバーレイ上で透過するために着色する色. */
     private static final int OVERLAY_TRANSPARENCY_COLOR = Color.GREEN;
     /** ゴールポケット用 Bitmap 画像. */
     private Item mGoalPocketItem;
     /** アイテム用 Bitmap 画像. */
     private Item mDoubleBallItem;
     /** アイテム用 フラグ. */
     private boolean mDoubleBallFlag = false;
 
     /** ボール用 Bitmap 画像. */
     private Bitmap mBallmap;
     /** 最大ボール表示数. */
     private static final int BALL_MAX = 1;
     /** ボールサイズ. */
     private float mBallSize;
 
     /** マップ幅. */
     private int mMapWidth;
     /** マップ高. */
     private int mMapHeight;
     /** マップ配列. */
     private boolean mMap[];
     /** マップ通過カウント. */
     private int mDrawMapCount = 0;
 
     /** ゴールポケット出現係数. */
     private static final double GOAL_LINE = 0.7;
 
     /** アイテム取得バイブレーション振動時間(milli sec). */
     private static final int ITEM_VIBRATE = 100;
     /** クリアバイブレーション振動パターン(OFF/ON/OFF/ON/・・・). */
     private static final long[] CLEAR_VIBRATE = {0, 50, 100, 100, 100, 200};
 
     /** コンストラクタ.
      *
      *
      * @param context 親コンテキスト
      */
     public RiderView(Context context) {
         super(context);
         Log.i(TAG, "RiderView(Context context) start");
         mHolder = null;
         mThread = null;
         mParent = (Activity) context;
         mCircleContainer = new ArrayList<Circle>();
         getHolder().setFormat(PixelFormat.TRANSPARENT);
         mPaint = new Paint();
         mPaint.setAntiAlias(true);
         mGx = mGy = 0;
 
         /** SurfaceHolder を取得し、コールバック登録を行う。 */
         getHolder().addCallback(this);
 
         Log.i(TAG, "RiderView(Context context) finish");
     }
 
     /** SurfaceView生成.
      *
      * SurfaceViewの生成時にコールされる。\n
      * ホルダーとスレッドをメンバーに保管する。\n
      */
     @Override
     public void surfaceCreated(SurfaceHolder holder){
         Log.i(TAG, "surfaceCreated(SurfaceHolder holder) start");
         this.mHolder = holder;
         mThread = new Thread(this);
 
         Log.i(TAG, "surfaceCreated(SurfaceHolder holder) finish");
     }
 
     /** SurfaceView変更.
      *
      * SurfaceViewの起動時にコールされる。\n
      * 画面の高さ・幅を保管、及びマップの初期化を行い、スレッドを起動する。\n
      */
     @Override
     public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
     {
         Log.i(TAG, "surfaceChanged(SurfaceHolder holder, int format, int width, int height) start");
 
         if(mThread != null ) {
             mWidth  = width;
             mHeight = height;
 
             /** マップの初期化 */
             SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mParent);
             mBallSize = Float.parseFloat(prefs.getString(mParent.getString(R.string.ball_size_key), "20"));
             mMapWidth = (int) (mWidth / mBallSize);
             mMapHeight = (int) (mHeight / mBallSize);
             mMap = new  boolean[mMapHeight * mMapHeight];
             int index = mMap.length;
             for(int i = 0; i < index; i++) {
                 mMap[i] = false;
             }
 
             createBitmap();
             mThread.start();
         }
         Log.i(TAG, "surfaceChanged(SurfaceHolder holder, int format, int width, int height) finish");
     }
 
     /** Bitmap画像の作成.
      *
      * 背景画像、オーバーレイ画像の作成を行う。\n
      */
     private void createBitmap() {
         /** 背景画像の作成 */
         ImageFactory factory = new ImageFactory(mParent);
         if (mBitmap != null && !mBitmap.isRecycled()) {
             mBitmap.recycle();
         }
         mBitmap = factory.getRiderBitmap();
 
         /** ・ゴールポケット・アイテムの作成 */
         Bitmap src;
         Bitmap dst;
         int index;
         int posX;
         int posY;
 
         src = factory.getItemBitmap(R.id.goal_pocket);
         dst = ImageUtils.resizeBitmapToSpecifiedSize(src, mBallSize * 3);
         posX = (mWidth / 2) + (dst.getWidth() / 2);
         posY = (mHeight / 2) + (dst.getHeight() / 2);
         index = getIndexFromPosition(posX, posY);
         if (mGoalPocketItem != null) {
             mGoalPocketItem.close();
         }
         mGoalPocketItem = new Item(dst, mWidth / 2, mHeight / 2, index);
         src.recycle();
 
         src = factory.getItemBitmap(R.id.double_ball);
         dst = ImageUtils.resizeBitmapToSpecifiedSize(src, mBallSize * 3);
         posX = (mWidth / 2) + (dst.getWidth() / 2);
         posY = (mHeight / 2) + (dst.getHeight() / 2);
         index = getIndexFromPosition(posX, posY);
         if (mDoubleBallItem != null) {
             mDoubleBallItem.close();
         }
         mDoubleBallItem = new Item(dst, mWidth /2  , mHeight / 2, index);
         src.recycle();
 
         src = factory.getItemBitmap(R.id.ball);
         if(mBallmap != null && !mBallmap.isRecycled()) {
             mBallmap.recycle();
         }
         mBallmap = ImageUtils.resizeBitmapToSpecifiedSize(src, (float) (mBallSize * 2.1));
         src.recycle();
 
         /** オーバーレイ画像の作成( オーバーレイ描画用の設定を全て行う ) */
         if (mOverLayMap == null || mOverLayMap.isRecycled()) {
             mOverLayMap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
             mOverLayCanvas = new Canvas(mOverLayMap);
 
             mOverLayPaint = new Paint();
             mOverLayPaint.setColor(OVERLAY_TRANSPARENCY_COLOR);
             mOverLayPaint.setStrokeCap(Cap.ROUND);
         }
         mOverLayCanvas.drawColor(OVERLAY_COLOR);
     }
 
     /** SurfaceView破棄.
      *
      * SurfaceViewの破棄時にコールされる。\n
      * スレッド、及び Bitmap の破棄する。
      */
     @Override
     public void surfaceDestroyed(SurfaceHolder holder) {
         Log.i(TAG, "surfaceDestroyed(SurfaceHolder holder) start");
         mThread = null;
         mBitmap.recycle();
         mOverLayMap.recycle();
         mBallmap.recycle();
         mGoalPocketItem.close();
         mDoubleBallItem.close();
         Log.i(TAG, "surfaceDestroyed(SurfaceHolder holder) finish");
     }
 
     /**
      * Thread起動
      */
     @Override
     public void run()
     {
         while (mThread != null)
         {
             /** ボールの消去 */
             if (mClearFlag == true) {
                 mCircleContainer.clear();
                 mClearFlag = false;
             }
 
             Canvas canvas = mHolder.lockCanvas();
 
             /** ボール同士の衝突計算 */
             int size = mCircleContainer.size();
             for(int i = 0; i < size; i++)
             {
                 for(int j = 0; j < size; j++)
                 {
                     if(j <= i) {
                         continue;
                     }
 
                     Circle a = mCircleContainer.get(i);
                     Circle b = mCircleContainer.get(j);
                     float ab_x = b.x - a.x;
                     float ab_y = b.y - a.y;
                     float tr = a.radius + b.radius;
 
                     if((ab_x * ab_x) + (ab_y * ab_y) < (tr * tr))
                     {
                         float len = (float)Math.sqrt((ab_x * ab_x) + (ab_y * ab_y));
                         float distance = (a.radius + b.radius) - len;
                         if(len > 0) {
                             len = 1 / len;
                         }
                         ab_x *= len;
                         ab_y *= len;
 
                         distance /= 2.0;
                         a.x -= ab_x * distance;
                         a.y -= ab_y * distance;
                         b.x += ab_x * distance;
                         b.y += ab_y * distance;
 
                         /** 衝突後の速度を算出 */
                         float ma = (float) ((b.m / (a.m + b.m)) * (1 + E_HOGE)* ((b.dx - a.dx) * ab_x + (b.dy - a.dy) * ab_y));
                         float mb = (float) ((a.m / (a.m + b.m)) * (1 + E_HOGE)* ((a.dx - b.dx) * ab_x + (a.dy - b.dy) * ab_y));
                         a.dx += ma * ab_x;
                         a.dy += ma * ab_y;
                         b.dx += mb * ab_x;
                         b.dy += mb * ab_y;
                     }
                 }
             }
 
             /** 算出結果をボールに反映 */
             for(int i = 0 ; i < size ; i++)
             {
                 Circle a = mCircleContainer.get(i);
                 a.prevX = a.x;
                 a.prevY = a.y;
                 a.dx *= D_HOGE;
                 a.dy *= D_HOGE;
                 a.dx += mGx;
                 a.dy += mGy;
                 a.x += a.dx;
                 a.y += a.dy;
 
                 if(a.x < a.radius)
                 {
                     a.x = a.radius;
                     a.dx *= -1;
                 }
                 if(a.y < a.radius)
                 {
                     a.y = a.radius;
                     a.dy *= -1;
                 }
                 if(a.x > mWidth - a.radius)
                 {
                     a.x = mWidth - a.radius;
                     a.dx *= -1;
                 }
                 if(a.y > mHeight - a.radius)
                 {
                     a.y = mHeight - a.radius;
                     a.dy *= -1;
                 }
 
                 /** オーバーレイ画像をボールサイズ分透過する */
                 if (!mOverLayMap.isRecycled()) {
                     /* 描画位置、領域を計算 */
                     Rect overlayClip = new Rect();
                     getOverLayClipX(mOverLayCanvas, a, overlayClip);
                     mOverLayPaint.setStrokeWidth(a.radius * 2);
                     mOverLayCanvas.save();
                     mOverLayCanvas.clipRect(overlayClip, Region.Op.INTERSECT);
                     mOverLayCanvas.drawLine(a.prevX, a.prevY, a.x, a.y, mOverLayPaint);
                     mOverLayCanvas.restore();
 
                     int [] pixels = new int[overlayClip.width() * overlayClip.height()];
                     mOverLayMap.getPixels(pixels, 0, overlayClip.width(), overlayClip.left, overlayClip.top, overlayClip.width(), overlayClip.height());
                     for (int count = 0; count < pixels.length; count++) {
                         if (pixels[count] != OVERLAY_COLOR) {
                             pixels[count] = 0;
                         }
                     }
                     mOverLayMap.setPixels(pixels, 0, overlayClip.width(), overlayClip.left, overlayClip.top, overlayClip.width(), overlayClip.height());
                 }
 
                 /** 描画処理 */
                 if (canvas != null) {
                     onDraw(canvas);
                 }
 
                 /** 通過率算出処理 */
                 int centerIndex = getIndexFromPosition(a.x, a.y);
                 if(!mMap[centerIndex]) {
                     mMap[centerIndex] = true;
                     mDrawMapCount++;
                 }
 
                 /**
                  * アイテム、ゴールポケットの当たり判定用に、
                  * ボールの上下左右、及び中心の index を格納
                  */
                 int indexArray[] = new int[5];
                 indexArray[0] = getIndexFromPosition(a.x, a.y);
                 indexArray[1] = getIndexFromPosition(a.x, a.y - a.radius);
                 indexArray[2] = getIndexFromPosition(a.x, a.y + a.radius);
                 indexArray[3] = getIndexFromPosition(a.x - a.radius, a.y);
                 indexArray[4] = getIndexFromPosition(a.x + a.radius, a.y);
 
                 /** アイテム取得判定 */
                 for(int index:indexArray) {
                     if(mDoubleBallItem.index == index && mDoubleBallFlag == false) {
                         createBall(a.x, a.y);
                         ((Vibrator)mParent.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(ITEM_VIBRATE);
                         mDoubleBallFlag = true;
                     }
                 }
 
                 /** クリア判定 */
                 if(mDrawMapCount >= (mMapWidth * mMapHeight * GOAL_LINE)) {
                     mGoalFlag = true;
                     for(int index:indexArray) {
                         if(mGoalPocketItem.index == index) {
                             /** バイブレーション */
                             ((Vibrator)mParent.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(CLEAR_VIBRATE, -1);
 
                             createBitmap();
                             mCircleContainer.clear();
                             mGoalFlag = false;
                             mDoubleBallFlag = false;
                             mDrawMapCount = 0;
                             for(int j = 0; j < mMap.length; j++) {
                                 mMap[j] = false;
                             }
                             onDraw(canvas);
                             System.gc();
                             break;
                         }
                     }
                 }
             }
 
             /** 初回起動時、又はクリア時は「Tap to start!」を表示 */
             if (size == 0 && canvas != null) {
                 onDraw(canvas);
                 Bitmap start = BitmapFactory.decodeResource(getResources(), R.drawable.tap_to_start);
                 canvas.drawBitmap(start, 30 , mHeight / 3, null);
                 start.recycle();
             }
 
             if(canvas != null) {
                 mHolder.unlockCanvasAndPost(canvas);
             }
         }
     }
 
     /** マップインデックス算出処理.
      *
      * ボールの座標からマップのインデックスを算出し返却する。\n
      *
      * @param x  ボールの x 座標
      * @param y  ボールの y 座標
      * @return   マップインデックス
      */
     public int getIndexFromPosition(float x, float y) {
         int posX = (int) (x / mBallSize);
         int posY = (int) (y / mBallSize);
         return (posY * mMapWidth) + posX;
     }
 
     /** 画面タップイベント.
      *
      * 画面のタップ時にコールされる。\n
      * タップ位置へボールを表示する。\n
      */
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         Log.i(TAG, "onTouchEvent(MotionEvent event) start");
         if(event.getAction() == MotionEvent.ACTION_DOWN)
         {
             /** 既にボールが表示されている場合は何もせずしない  */
             if (mCircleContainer.size() >= BALL_MAX) {
                 Log.d(TAG, "mContainer.size() is not 0");
                 Log.i(TAG, "onTouchEvent(MotionEvent event) finish");
                 return true;
             }
             float x = event.getX();
             float y = event.getY();
             createBall(x, y);
         }
         Log.i(TAG, "onTouchEvent(MotionEvent event) finish");
         return true;
     }
 
     private void createBall(float x, float y) {
         float dx = (float)(Math.random() * 10 - 5);
         float dy = (float)(Math.random() * 10 - 5);
         float r = mBallSize;
         float ran = (float)Math.random();
         float m = ran * 10 + 10;
         mCircleContainer.add(new Circle(r, x, y, dx, dy, m));
     }
 
     /** 加速度の変更.
      *
      * Activityが持つセンサー状態に変化があった際にコールされる。\n
      * 加速度を更新する。\n
      *
      * @param gx
      * @param gy
      */
     public void setAcce(float gx, float gy) {
         this.mGx = gx;
         this.mGy = gy;
     }
 
     /** ボールクリア.
      *
      * クリアフラグを true にすることにより、
      * 描画しているボールの消去を行う。\n
      */
     public void setClear() {
         Log.i(TAG, "setClear() start");
         mClearFlag = true;
         Log.i(TAG, "setClear() finish");
     }
 
     /** 描画処理.
      *
      * 背景Bitmap、オーバーレイ Bitmap、及びボールの描画処理
      *
      * @param aCanvas   Canvas
      */
     @Override
     protected void onDraw(Canvas aCanvas)
     {
         super.onDraw(aCanvas);
         try {
             /** 背景画像の描画を行う。 */
             aCanvas.drawColor(Color.BLACK);
             aCanvas.drawBitmap(mBitmap, 0, 0, null);
 
             /** ゴールラインを達成している場合、画面中央にポケットの描画を行う。 */
             if(mGoalFlag) {
                 aCanvas.drawBitmap(mGoalPocketItem.bitmap, mGoalPocketItem.x, mGoalPocketItem.y, null);
             } else {
                 /** 未達成の場合、オーバーレイ画像の描画を行う。 */
                 aCanvas.drawBitmap(mOverLayMap, 0, 0, null);
             }
 
             int size = mCircleContainer.size();
             /** アイテム表示 */
             if(mDoubleBallFlag == false && mGoalFlag == false && size > 0) {
                 aCanvas.drawBitmap(mDoubleBallItem.bitmap, mDoubleBallItem.x, mDoubleBallItem.y, null);
             }
 
             /** 保持するボール全ての描画を行う。 */
             for(int i = 0; i < size; i++) {
                 Circle circle = mCircleContainer.get(i);
                 aCanvas.drawBitmap(mBallmap, circle.x - circle.radius, circle.y - circle.radius, null);
             }
         } catch (Exception e) {
             Log.e(TAG, Log.getStackTraceString(e));
         }
     }
 
     /** オーバーレイ画像クリップ処理 */
     private void getOverLayClipX(Canvas target, Circle drawCircle, Rect clip) {
         /* X系 */
         // drawCircle.prevX     drawCircle.x
         //       ○ ---------------> ○
         if (drawCircle.x > drawCircle.prevX) {
             clip.left = (int)(drawCircle.prevX - drawCircle.radius);
             clip.right = (int)(drawCircle.x + drawCircle.radius);
         // drawCircle.x        drawCircle.prevX
         //       ○ <--------------- ○
         } else {
             clip.left = (int)(drawCircle.x - drawCircle.radius);
             clip.right = (int)(drawCircle.prevX + drawCircle.radius);
         }
         /* Y系 */
         if (drawCircle.y > drawCircle.prevY) {
             clip.top = (int)(drawCircle.prevY - drawCircle.radius);
             clip.bottom = (int)(drawCircle.y + drawCircle.radius);
         } else {
             clip.top = (int)(drawCircle.y - drawCircle.radius);
             clip.bottom = (int)(drawCircle.prevY + drawCircle.radius);
         }
 
         /* クリップ領域補正処理 */
         Rect canvas = target.getClipBounds();
         if (clip.left < canvas.left)        clip.left = canvas.left;
         else if (clip.left > canvas.right)  clip.left = canvas.right;
 
         if (clip.right < clip.left)         clip.right = clip.left;
         else if (clip.right > canvas.right) clip.right = canvas.right;
 
         if (clip.top < canvas.top)          clip.top = canvas.top;
         else if (clip.top > canvas.bottom)  clip.top = canvas.bottom;
 
         if (clip.bottom < clip.top)             clip.bottom = clip.top;
         else if (clip.bottom > canvas.bottom)   clip.bottom = canvas.bottom;
     }
 
 }
