 package com.howfun.android.HF2D;
 
 import com.howfun.android.antguide.R;
 import com.howfun.android.antguide.Utils;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.drawable.Drawable;
 
 public class HomeSprite extends Sprite {
 
    private static final int HOME_W = 70;
    private static final int HOME_H = 70;
 
    private Bitmap mHoleBmp;
    private Context mContext;
    private Paint mPaint;
 
    public HomeSprite(Context context, Pos pos) {
       mContext = context;
       mPos = pos;
       mRect = new Rect();
       HF2D.calRectByPos(mRect, mPos, HOME_W, HOME_H);
 
       loadHole();
       mPaint = new Paint();
 
    }
 
    @Override
    protected boolean checkCollision(Sprite s) {
       // TODO Auto-generated method stub
       return false;
    }
 
    @Override
    public void draw(Canvas canvas) {
       if (canvas == null) {
          return;
       }
       canvas.drawBitmap(mHoleBmp, mRect.left, mRect.top, mPaint);
    }
 
    @Override
    protected Pos getNextPos() {
       return mPos;
    }
 
    private void loadHole() {
       Resources r = mContext.getResources();
       Drawable holeDrawable = r.getDrawable(R.drawable.hole);
       Bitmap bitmap = Bitmap.createBitmap(HOME_W, HOME_H,
             Bitmap.Config.ARGB_8888);
       Canvas canvas = new Canvas(bitmap);
      holeDrawable.setBounds(0, 0, HOME_W, HOME_H);
       holeDrawable.draw(canvas);
       mHoleBmp = bitmap;
    }
 
    @Override
    public void clear() {
       Utils.recycleBitmap(mHoleBmp);
       mHoleBmp = null;
    }
 }
