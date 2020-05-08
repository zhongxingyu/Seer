 package net.adamfoster.android.finger;
 
 import java.util.List;
 
 import net.adamfoster.android.finger.beans.BaseKey;
 import net.adamfoster.android.finger.beans.Fingering;
 import net.adamfoster.android.finger.beans.Instrument;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.drawable.Drawable;
 import android.util.AttributeSet;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.SurfaceHolder.Callback;
 
 public class FingerSurface extends SurfaceView implements Callback
 {
     private SurfaceHolder mSurfaceHolder;
     private Instrument mInstrument;
     private Paint mKeyDownPaint;
     private Paint mTrillDownPaint;
     private Paint mTrillUpPaint;
     private Paint mBaseKeyPaint;
     
 
     public FingerSurface(Context context)
     {
         super(context);
     }
 
     public FingerSurface(Context context, AttributeSet attributeSet)
     {
         super(context, attributeSet);
     }
     
     public void setInstrument(Instrument instrument)
     {
         mInstrument = instrument;
         mKeyDownPaint = new Paint();
         mKeyDownPaint.setColor(this.getResources().getColor(R.color.keyDown));
         mTrillDownPaint = new Paint();
         mTrillDownPaint.setColor(this.getResources().getColor(R.color.keyTrillDown));
         mTrillUpPaint = new Paint();
         mTrillUpPaint.setColor(this.getResources().getColor(R.color.keyTrillUp));
         mBaseKeyPaint = new Paint();
         mBaseKeyPaint.setColor(this.getResources().getColor(R.color.baseKey));
         mBaseKeyPaint.setStyle(Paint.Style.STROKE);
     }
 
     @Override
     public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
     {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void surfaceCreated(SurfaceHolder holder)
     {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void surfaceDestroyed(SurfaceHolder holder)
     {
         // TODO Auto-generated method stub
 
     }
 
     public void drawFingering(List<BaseKey> baseKeys, Fingering f)
     {
         Canvas c = null;
         try
         {
             Paint p = new Paint();
             p.setColor(this.getResources().getColor(R.color.keyDown));
             
             c = getHolder().lockCanvas(null);
             getHolder().addCallback(this);
             
             float canvasRatio = c.getWidth() / (float) c.getHeight();
             
             //draw instrument
             Drawable bg = this.getResources().getDrawable(mInstrument.image);
             float bgRatio = bg.getIntrinsicWidth() / (float) bg.getIntrinsicHeight();
             
             int w, h; // width, height
             int l, d; // left, down offset
             float ratio;
             if (canvasRatio > bgRatio) // canvas is wider than image
             {
                 h = c.getHeight();
                 w = (int) (h * bgRatio);
                 l = (c.getWidth() - w) / 2;
                 d = 0;
                 ratio = c.getWidth() / (float) w;
             }
             else // canvas is taller than image
             {
                 w = c.getWidth();
                 h = (int) (w / bgRatio);
                 l = 0;
                 d = (c.getHeight() - h) / 2;
                 ratio = c.getHeight() / (float) h;
             }
             bg.setBounds(new Rect(l, d, w+l, h+d));
             bg.draw(c);
          
             for (BaseKey bk : baseKeys)
             {
                 drawKeyOutline(c, bk, mBaseKeyPaint, bg.getBounds(), ratio);
             }
                 
             for (BaseKey bk : baseKeys)
             {
                 if (f.keysDowns.contains(bk.name))
                 {
                    drawKey(c, bk, mKeyDownPaint, bg.getBounds());
                 }
                 else if (f.keysTrillDowns.contains(bk.name))
                 {
                    drawKey(c, bk, mTrillDownPaint, bg.getBounds());
                 }
                 else if (f.keysTrillUp.contains(bk.name))
                 {
                    drawKey(c, bk, mTrillUpPaint, bg.getBounds());
                 }
                 else if (f.ringsDowns.contains(bk.name))
                 {
                     drawRing(c, bk, mKeyDownPaint, bg.getBounds(), ratio);
                 }
             }
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
         finally 
         {
             if (c != null) 
             {
                 getHolder().unlockCanvasAndPost(c);
             }
         }
     }
 
    private void drawKey(Canvas c, BaseKey bk, Paint p, Rect imgBounds)//, float ratio)
     {
         // TODO Auto-generated method stub
         switch (bk.type)
         {
             //left = imgBounds.
             case BaseKey.TYPE_CIRCLE:
                 //imgBounds.left + imgBounds.width()*bk.positionx;
                 c.drawCircle(imgBounds.left + imgBounds.width()*bk.positionx, 
                         imgBounds.top + imgBounds.height()*bk.positiony, 
                        bk.radius*imgBounds.width(), 
                         p);
                 break;
             case BaseKey.TYPE_RECTANGLE:
                 c.drawRect(imgBounds.left + imgBounds.width()*bk.positionx, 
                         imgBounds.top + imgBounds.height()*bk.positiony, 
                        imgBounds.left + imgBounds.width()*bk.positionx + bk.width*imgBounds.width(), 
                        imgBounds.top + imgBounds.height()*bk.positiony + bk.height*imgBounds.width(), 
                         p);
                 break;
             case BaseKey.TYPE_ROUNDRECT:
                 // FIX c.drawRoundRect(new RectF(c.getWidth()*bk.positionx, c.getHeight()*bk.positiony, c.getWidth()*bk.positionx+bk.width, c.getHeight()*bk.positiony+bk.height), bk.rx, bk.ry, p);
                 break;
             case BaseKey.TYPE_IMAGE:
                 //to be implemented
                 break;
         }
     }
     
     private void drawRing(Canvas c, BaseKey bk, Paint p, Rect imgBounds, float ratio)
     {
         Paint.Style s = p.getStyle();
         float sw = p.getStrokeWidth();
         p.setStyle(Paint.Style.STROKE);
         
         // TODO Auto-generated method stub
         switch (bk.type)
         {
             //left = imgBounds.
             case BaseKey.TYPE_CIRCLE:
                 p.setStrokeWidth((float) (ratio*bk.radius*.7));
                 //imgBounds.left + imgBounds.width()*bk.positionx;
                 c.drawCircle(imgBounds.left + imgBounds.width()*bk.positionx, 
                         imgBounds.top + imgBounds.height()*bk.positiony, 
                         bk.radius*ratio, 
                         p);
                 break;
             case BaseKey.TYPE_RECTANGLE:
                 p.setStrokeWidth((float) (ratio*Math.min(bk.width, bk.height)*.3));
 
                 c.drawRect(imgBounds.left + imgBounds.width()*bk.positionx, 
                         imgBounds.top + imgBounds.height()*bk.positiony, 
                         imgBounds.left + imgBounds.width()*bk.positionx + bk.width*ratio, 
                         imgBounds.top + imgBounds.height()*bk.positiony + bk.height*ratio, 
                         p);
                 break;
             case BaseKey.TYPE_ROUNDRECT:
                 // FIX c.drawRoundRect(new RectF(c.getWidth()*bk.positionx, c.getHeight()*bk.positiony, c.getWidth()*bk.positionx+bk.width, c.getHeight()*bk.positiony+bk.height), bk.rx, bk.ry, p);
                 break;
             case BaseKey.TYPE_IMAGE:
                 //to be implemented
                 break;
         }
         
         mKeyDownPaint.setStyle(s);
         mKeyDownPaint.setStrokeWidth(sw);
     }
     
     private void drawKeyOutline(Canvas c, BaseKey bk, Paint p, Rect imgBounds, float ratio)
     {
         // TODO Auto-generated method stub
         switch (bk.type)
         {
             //left = imgBounds.
             case BaseKey.TYPE_CIRCLE:
                 //imgBounds.left + imgBounds.width()*bk.positionx;
                 c.drawCircle(imgBounds.left + imgBounds.width()*bk.positionx, 
                         imgBounds.top + imgBounds.height()*bk.positiony, 
                         bk.radius*ratio, 
                         p);
                 break;
             case BaseKey.TYPE_RECTANGLE:
                 c.drawRect(imgBounds.left + imgBounds.width()*bk.positionx, 
                         imgBounds.top + imgBounds.height()*bk.positiony, 
                         imgBounds.left + imgBounds.width()*bk.positionx + bk.width*ratio, 
                         imgBounds.top + imgBounds.height()*bk.positiony + bk.height*ratio, 
                         p);
                 break;
             case BaseKey.TYPE_ROUNDRECT:
                 // FIX c.drawRoundRect(new RectF(c.getWidth()*bk.positionx, c.getHeight()*bk.positiony, c.getWidth()*bk.positionx+bk.width, c.getHeight()*bk.positiony+bk.height), bk.rx, bk.ry, p);
                 break;
             case BaseKey.TYPE_IMAGE:
                 //to be implemented
                 break;
         }
     }
     
 }
 
