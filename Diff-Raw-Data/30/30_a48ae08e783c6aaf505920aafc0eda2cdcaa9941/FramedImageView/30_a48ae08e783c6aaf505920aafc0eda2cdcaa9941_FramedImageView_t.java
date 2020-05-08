 package com.wmbest.widgets;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.*;
 import android.graphics.Bitmap.Config;
 import android.graphics.drawable.*;
 import android.net.Uri;
 import android.util.AttributeSet;
import android.util.DisplayMetrics;
 import android.widget.ImageView;
 
 import com.wmbest.widgets.R;
 
 public class FramedImageView extends ImageView {
 
     private Shape mShape = Shape.CIRCLE;
     private Drawable mFrame;
 
     private Paint mPaint = new Paint();
 
     public FramedImageView(Context aContext, AttributeSet aAttrs) {
         super(aContext, aAttrs, 0);
         setup(aContext, aAttrs);
     }
 
     public FramedImageView(Context aContext, AttributeSet aAttrs, int defStyle) {
         super(aContext, aAttrs, defStyle);
         setup(aContext, aAttrs);
     }
 
     @Override
     public void setImageURI(Uri aUri) {
         super.setImageURI(aUri);
         initPaint();
     }
 
     @Override
     public void setImageResource(int aRes) {
         super.setImageResource(aRes);
         initPaint();
     }
 
     @Override
     public void setImageDrawable(Drawable aDrawable) {
         super.setImageDrawable(aDrawable);
         initPaint();
     }
 
     private void setup(Context aContext, AttributeSet aAttrs) {
         TypedArray a = aContext.obtainStyledAttributes(aAttrs,
                 R.styleable.FramedImageView);
 
         int shape = a.getInt(R.styleable.FramedImageView_shape, 0);
         mShape = Shape.values()[shape];
         mFrame = a.getDrawable(R.styleable.FramedImageView_frame);
 
         a.recycle();
     }
 
     private void initPaint() {
        if (getDrawable() == null) return;
        if (getWidth() == 0 || getHeight() == 0) return;
        Bitmap b = drawableToBitmap(getDrawable());
        BitmapShader shader = new BitmapShader(b, 
                 Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        Matrix m = new Matrix();
        float scaleX = getWidth() / ((float) b.getWidth());
        float scaleY = getHeight() / ((float) b.getHeight());

        m.setScale(scaleX, scaleY);

        shader.setLocalMatrix(m);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
         mPaint.setShader(shader);
     }
 
     @Override
     protected void onDraw(Canvas canvas) {
        if (mPaint.getShader() == null) initPaint();
 
         if (mFrame != null) {
            mFrame.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
         }
 
         switch (mShape) {
             case CIRCLE:
                 drawCircle(canvas);
                 break;
             default:
                 super.onDraw(canvas);
         }
 
         if (mFrame != null) {
             mFrame.draw(canvas);
         }
     }
 
     private void drawCircle(Canvas aCanvas) {
        int center = getWidth() / 2;
         aCanvas.drawCircle(center, center, center, mPaint);
     }
 
     public static Bitmap drawableToBitmap (Drawable aDrawable) {
         if (aDrawable instanceof BitmapDrawable) {
             return ((BitmapDrawable) aDrawable).getBitmap();
         }
 
         int width = aDrawable.getIntrinsicWidth();
         width = width > 0 ? width : 1;
         int height = aDrawable.getIntrinsicHeight();
         height = height > 0 ? height : 1;
 
         Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
         Canvas canvas = new Canvas(bitmap); 
         aDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
         aDrawable.draw(canvas);
 
         return bitmap;
     }
 
     public static enum Shape {
         NONE, SQUARE, CIRCLE;
     }
 }
 
