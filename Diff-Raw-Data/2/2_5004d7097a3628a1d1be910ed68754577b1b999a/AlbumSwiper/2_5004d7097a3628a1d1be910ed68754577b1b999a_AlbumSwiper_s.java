 package com.lithiumli.fiction.ui;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.animation.ValueAnimator;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Matrix;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.VelocityTracker;
 import android.view.View;
 import android.widget.Scroller;
 
 import com.lithiumli.fiction.FictionActivity;
 import com.lithiumli.fiction.PlaybackQueue;
 import com.lithiumli.fiction.R;
 import com.lithiumli.fiction.Song;
 
 import java.util.ArrayList;
 
 public class AlbumSwiper extends View {
     Context mContext;
     PlaybackQueue mQueue;
     ArrayList<Cover> mCovers = new ArrayList<Cover>(3);
     double mTheta;
     FictionActivity mListener;
 
     float mStartX, mStartY;
 
     public AlbumSwiper(Context context, AttributeSet attrs) {
         super(context, attrs);
         mContext = context;
     }
 
     public void setQueue(PlaybackQueue queue) {
         mQueue = queue;
     }
 
     public void setListener(FictionActivity activity) {
         mListener = activity;
     }
 
     public int getRadius() {
         return (int) (getWidth() / 3f);
     }
 
     @Override
     public void onDraw(Canvas canvas) {
         super.onDraw(canvas);
 
         ArrayList<Cover> covers = (ArrayList<Cover>) mCovers.clone();
         java.util.Collections.sort(covers);
 
         for (Cover c : covers) {
             c.draw(canvas);
         }
     }
 
     @Override
     protected void onSizeChanged(int width, int height, int oldw, int oldh) {
         if (width == 0 || height == 0) return;
         updateCovers();
     }
 
 	@Override
 	public boolean onTouchEvent(MotionEvent ev) {
 		switch (ev.getAction()) {
         case MotionEvent.ACTION_DOWN:
             mStartX = ev.getX();
             mStartY = ev.getY();
             break;
         case MotionEvent.ACTION_MOVE:
             float dx = ev.getX() - mStartX;
 
             if (mQueue != null) {
                 int pos = mQueue.getCurrentPosition();
                 if (pos == 0 && dx > 0) {
                     break;
                 }
                 else if (pos == mQueue.getCount() - 1 && dx < 0) {
                     break;
                 }
             }
 
             double theta = dx / getRadius();
             scroll(theta);
             invalidate();
             break;
         case MotionEvent.ACTION_UP:
         case MotionEvent.ACTION_CANCEL:
             mStartX = 0f;
             mStartY = 0f;
             if (mTheta >= Math.PI / 5) {
                 scrollTo(Math.PI / 5, new Runnable() {
                         @Override
                         public void run() {
                             if (mListener != null) {
                                 mListener.prevButton(AlbumSwiper.this);
                             }
                         }
                     });
             }
             else if (mTheta <= -Math.PI / 5) {
                 scrollTo(-Math.PI / 5, new Runnable() {
                         @Override
                         public void run() {
                             if (mListener != null) {
                                 mListener.nextButton(AlbumSwiper.this);
                             }
                         }
                     });
             }
             else {
                 scrollTo(0.0, null);
             }
             break;
         default:
             break;
         }
         return true;
     }
 
     private void scroll(double theta) {
         mTheta = theta;
         int r = getRadius();
 
         for (Cover c : mCovers) {
             c.setAngle(theta);
             c.setAlphaByAngle();
         }
     }
 
     private void scrollTo(double theta, final Runnable callback) {
         ValueAnimator a = ValueAnimator.ofFloat((float) mTheta, (float) theta);
         a.setDuration(200);
         a.addUpdateListener(
             new ValueAnimator.AnimatorUpdateListener() {
                 @Override
                 public void onAnimationUpdate(ValueAnimator a) {
                     Float angle = (Float) a.getAnimatedValue();
                     scroll(angle.doubleValue());
                     invalidate();
                 }
             });
 
         if (callback != null) {
             a.addListener(new AnimatorListenerAdapter() {
                     @Override
                     public void onAnimationEnd(Animator a) {
                         // TODO: reset cover angles after animation end
                         callback.run();
                     }
                 });
         }
 
         a.start();
     }
 
     public void updateCovers() {
         if (mQueue == null) return;
 
         int count = mQueue.getCount();
         if (count <= 0) return;
 
         int position = mQueue.getCurrentPosition();
         Song prev = null, current = null, next = null;
 
         double r = (int) (getWidth() / 3.0);
        double theta = -Math.PI / 2;
         mCovers = new ArrayList<Cover>(3);
         for (int posOffset = -1; posOffset <= 1; posOffset ++) {
             int index = position + posOffset;
             int coverOffset = posOffset + 1;
 
             if (index >= 0) {
                 Song song = mQueue.getItem(index);
                 Cover c = resolve(this, song.getAlbumArt(), theta, posOffset);
                 c.setAlpha((int) (255 * Math.cos(theta)));
 
                 mCovers.add(c);
             }
             theta += Math.PI / 4;
         }
     }
 
     private Cover resolve(AlbumSwiper v, Uri uri, double angle, int position) {
         Drawable d = null;
 
         if (uri != null) {
             String scheme = uri.getScheme();
 
             if (ContentResolver.SCHEME_CONTENT.equals(scheme) ||
                 ContentResolver.SCHEME_FILE.equals(scheme)) {
                 try {
                     d = Drawable.createFromStream(
                         mContext.getContentResolver().openInputStream(uri),
                         null);
                 }
                 catch (Exception e) {
                 }
             }
             else {
                 d = Drawable.createFromPath(uri.toString());
             }
         }
 
         if (d == null) {
             return new Cover(
                 (BitmapDrawable)
                 mContext.getResources().getDrawable(R.drawable.filler_album),
                 400, 400, angle, position);
         }
         else {
             BitmapDrawable b = (BitmapDrawable) d;
             int w = (int) (0.9 * getWidth());
             int h = getHeight();
             int bw = b.getIntrinsicWidth();
             int bh = b.getIntrinsicHeight();
 
             int fw, fh;
             if (bw > bh) {
                 fw = w;
                 fh = (int) (((float) bh / (float) bw) * fw);
 
                 if (fh > h) {
                     fw = (int) (((float) h / fh) * fw);
                     fh = h;
                 }
             }
             else {
                 fh = h;
                 fw = (int) (((float) bw / (float) bh) * fh);
 
                 if (fw > w) {
                     fh = (int) (((float) w / fw) * fh);
                     fw = w;
                 }
             }
 
             return new Cover(b, fw, fh, angle, position);
         }
     }
 
     class Cover implements Comparable {
         int width, height;
         BitmapDrawable b;
         double theta;
         int position;
 
         final float MIN_SCALE = (float) Math.cos(Math.PI / 4);
         final double OFFSET = Math.PI / 4;
 
         public Cover(BitmapDrawable _b, int _width, int _height,
                      double _theta, int _position) {
             b = _b;
             width = _width;
             height = _height;
             theta = _theta;
             position = _position;
 
             setBounds();
         }
 
         public void setBounds() {
             int r = AlbumSwiper.this.getRadius();
             int viewHeight = AlbumSwiper.this.getHeight();
             int viewWidth = AlbumSwiper.this.getWidth();
             float scale = (float) Math.cos(theta);
 
             if (scale < MIN_SCALE) scale = MIN_SCALE;
 
             int fw = (int) (scale * width);
             int fh = (int) (scale * height);
             int offsetX = (int) ((viewWidth / 2f) - (fw / 2f) +  (r * Math.sin(theta)));
             int offsetY = (viewHeight - fh) / 2;
 
             b.setBounds(offsetX, offsetY, fw + offsetX, fh + offsetY);
         }
 
         public void setAlpha(int alpha) {
             b.setAlpha(alpha);
         }
 
         public void setAlphaByAngle() {
             int alpha = (int) (320 * Math.cos(theta));
             if (alpha > 255) alpha = 255;
             else if (alpha < 192) alpha = 192;
 
             setAlpha(alpha);
         }
 
         public double getAngle() {
             return theta;
         }
 
         public void setAngle(double _theta) {
             theta = _theta + position * OFFSET;
         }
 
         public BitmapDrawable getDrawable() {
             return b;
         }
 
         public void draw(Canvas c) {
             setBounds();
             b.draw(c);
         }
 
         public int compareTo(Object o) {
             if (o == null) {
                 return -1;
             }
             Cover c = (Cover) o;
             double a1 = Math.abs(getAngle());
             double a2 = Math.abs(c.getAngle());
 
             // angle of 0 is "largest"
             if (a1 > a2) {
                 return -1;
             }
             else if (a2 > a1) {
                 return 1;
             }
             return 0;
         }
     }
 }
