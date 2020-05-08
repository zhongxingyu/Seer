 package com.howfun.android.HF2D;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Matrix;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.drawable.Drawable;
 
 import com.howfun.android.antguide.R;
 import com.howfun.android.antguide.Utils;
 
 public class AntSprite extends Sprite{
    private static final String TAG = "AntSprite";
 
    
    
 
 	private Context mContext;
 	private Paint mPaint;
 	private float mAngle; // Ant's running direction. Reversed on normal.
 
 	private Bitmap mAntBmpArray[];
 
 	private Bitmap mRotatedBitmap;
 	public int mWhichAntAnim = 0;	
 
 	private int ANT_WIDTH = 32;
 	private int ANT_HEIGHT = 32;
 
 	public AntSprite(Context c) {
 		mContext = c;
 		init();
 	}
 
 	public void init() {
 		mPaint = new Paint();
 		mPaint.setColor(Color.RED);
 
 		mAngle = 30;
 		mSpeed = 1;
 
 		mRect = new Rect();
 
 		mPos = new Pos(10, 10);
 		mType = TYPE_ANT;
 		FPS = 50;
 
 		loadAnt();
 
 		// Drawable antDrawable3 =
 		// mContext.getResources().getDrawable(R.drawable.ant3);
 		// mBitmap = new BitmapDrawable(antDrawable3);
 	}
 
 	public void init(int FPS, Bitmap[] bitmaps, Pos pos, Rect rect) {
 
 	}
 
 	@Override
 	protected boolean checkCollision(Sprite s) {
 		// Check with Line or Hole
 
 		return false;
 	}
 
 	public void setAngle(float angle) {
 		mAngle = angle;
 	}
 
 	public float getAngle() {
 		return mAngle;
 	}
 
 	@Override
 	protected Pos getNextPos() {
 		HF2D.getNextPos(mPos, mSpeed, mAngle);
 		HF2D.calRectByPos(mRect, mPos, ANT_WIDTH, ANT_HEIGHT);
 		return mPos;
 	}
 
 	@Override
 	public void draw(Canvas canvas) {
 		// TODO: use fps
 		getNextPos();
		Bitmap bmp = rotate(mAntBmpArray[mWhichAntAnim], mAngle);
 		// canvas.drawBitmap(mAntBmpArray[0], mRect.left, mRect.top, mPaint);
 		if (bmp != null) {
 			canvas.drawBitmap(bmp, mRect.left, mRect.top, mPaint);
 		}
 
 	}
 
 	private void loadAnt() {
 		mAntBmpArray = new Bitmap[4];
 		Resources r = mContext.getResources();
 		Drawable antDrawable0 = r.getDrawable(R.drawable.ant0);
 		Drawable antDrawable1 = r.getDrawable(R.drawable.ant1);
 		Drawable antDrawable2 = r.getDrawable(R.drawable.ant2);
 		Drawable antDrawable3 = r.getDrawable(R.drawable.ant3);
 
 		Bitmap bitmap = Bitmap.createBitmap(ANT_WIDTH, ANT_HEIGHT,
 				Bitmap.Config.ARGB_8888);
 		Canvas canvas = new Canvas(bitmap);
 		antDrawable0.setBounds(0, 0, ANT_WIDTH, ANT_HEIGHT);
 		antDrawable0.draw(canvas);
 		mAntBmpArray[0] = bitmap;
 
 		bitmap = Bitmap.createBitmap(ANT_WIDTH, ANT_HEIGHT,
 				Bitmap.Config.ARGB_8888);
 		canvas = new Canvas(bitmap);
 		antDrawable1.setBounds(0, 0, ANT_WIDTH, ANT_HEIGHT);
 		antDrawable1.draw(canvas);
 		mAntBmpArray[1] = bitmap;
 
 		bitmap = Bitmap.createBitmap(ANT_WIDTH, ANT_HEIGHT,
 				Bitmap.Config.ARGB_8888);
 		canvas = new Canvas(bitmap);
 		antDrawable2.setBounds(0, 0, ANT_WIDTH, ANT_HEIGHT);
 		antDrawable2.draw(canvas);
 		mAntBmpArray[2] = bitmap;
 
 		bitmap = Bitmap.createBitmap(ANT_WIDTH, ANT_HEIGHT,
 				Bitmap.Config.ARGB_8888);
 		canvas = new Canvas(bitmap);
 		antDrawable3.setBounds(0, 0, ANT_WIDTH, ANT_HEIGHT);
 		antDrawable3.draw(canvas);
 		mAntBmpArray[3] = bitmap;
 	}
 
 	private Bitmap rotate(Bitmap b, float degrees) {
 		if (degrees >= 0 && b != null) {
 			Matrix m = new Matrix();
 			m.setRotate(degrees);
 
 			try {
 				mRotatedBitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b
 						.getHeight(), m, true);
 			} catch (OutOfMemoryError ex) {
 				Utils.log(TAG, "out of memory error!");
 			}
 		}
 		return mRotatedBitmap;
 	}
 
 }
