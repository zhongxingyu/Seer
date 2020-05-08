 package com.tzapps.tzpalette.ui.view;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.ImageView;
 
 import com.tzapps.common.utils.ColorUtils;
 
 public class ColorBar extends ImageView implements OnTouchListener
 {
     private static final String TAG = "ColorBar";
     
     public interface OnColorBarChangedListener
     {
         public void onColorChanged(ColorBar colorBar, int color);
     }
     
     public enum ColorBarType
     {
         RGB_R,
         RGB_G,
         RGB_B,
         HSV_H,
         HSV_S,
         HSV_V,
         NONE
     }
     
     private int mColor;
     private ColorBarType mType;
     private Paint mPaint;
     private OnColorBarChangedListener mCallback;
     
     private void init()
     {
         mPaint = new Paint();
         mType = ColorBarType.NONE;
         
         setOnTouchListener(this);
     }
     
     public ColorBar(Context context)
     {
         super(context);
         init();
     }
 
     public ColorBar(Context context, AttributeSet attrs)
     {
         super(context, attrs);
         init();
     }
 
     public ColorBar(Context context, AttributeSet attrs, int defStyle)
     {
         super(context, attrs, defStyle);
         init();
     }
 
     public void setColor(int color)
     {
         mColor  = color;
         update();
     }
 
     public int getColor()
     {
         return mColor;
     }
     
     public void setType(ColorBarType type)
     {
         mType = type;
         update();
     }
     
     public ColorBarType getType()
     {
         return mType;
     }
     
     public void setOnColorChangeListener(OnColorBarChangedListener listenr)
     {
         mCallback = listenr;
     }
     
     public void update()
     {
         invalidate();
     }
     
     @Override
     public boolean onTouch(View v, MotionEvent event)
     {
         int w, h;
         int x, y;
         
         switch(event.getAction())
         {
            case MotionEvent.ACTION_DOWN:
             case MotionEvent.ACTION_MOVE:
                 w = getWidth();
                 h = getHeight();
                 x = (int)event.getX();
                 y = (int)event.getY();
                 
                 if (x < 0 || x > w || y < 0 | y > h) 
                     return false;
                 
                 int color = getColorAt(x,y,w,h);
                 setColor(color);
                 
                 if (mCallback != null)
                     mCallback.onColorChanged(this, color);
                 
                 break;
         }
         
         return false;
     }
     
     @Override
     protected void onDraw(Canvas canvas)
     {
         int w = getWidth();
         int h = getHeight();
         
         for (int i = 0; i < w; i++)
         {
             mPaint.setColor(getColorAt(i,h,w,h));
             canvas.drawLine(i, 0, i, h, mPaint);
         }
         
         // draw cursor
         mPaint.setColor(Color.WHITE);
         int cursorPos = getCursorPosition();
         canvas.drawLine(cursorPos, 0, cursorPos, h, mPaint);
         mPaint.setColor(Color.GRAY);
         canvas.drawLine(cursorPos-1, 0, cursorPos-1, h, mPaint);
         mPaint.setColor(Color.GRAY);
         canvas.drawLine(cursorPos+1, 0, cursorPos+1, h, mPaint);
     }
     
     private int getCursorPosition()
     {
         int position;
         int w = getWidth();
         int[] rgb = ColorUtils.colorToRGB(mColor);
         int[] hsv = ColorUtils.colorToHSV(mColor);
         
         switch(mType)
         {
             case RGB_R:
                 position = rgb[0] * w / 256;
                 break;
                 
             case RGB_G:
                 position = rgb[1] * w / 256;
                 break;
                 
             case RGB_B:
                 position = rgb[2] * w / 256;
                 break;
                 
             case HSV_H:
                 position = hsv[0] * w / 360;
                 break;
                 
             case HSV_S:
                 position = hsv[1] * w / 100;
                 break;
                 
             case HSV_V:
                 position = hsv[2] * w / 100;
                 break;
                 
             default:
             case NONE:
                 position = 0;
                 break;
         }
         
         return position;
     }
     
     private int getColorAt(int xPos, int yPos, int width, int height)
     {
         int[] rgb = ColorUtils.colorToRGB(mColor);
         int[] hsv = ColorUtils.colorToHSV(mColor);
         int color, r, g, b, h, s, v;
         
         switch(mType)
         {
             case RGB_R:
                 r = xPos * 256 / width;
                 g = rgb[1];
                 b = rgb[2];
                 color = ColorUtils.rgbToColor(r,g,b);
                 break;
                 
             case RGB_G:
                 r = rgb[0];
                 g = xPos * 256 / width;
                 b = rgb[2];
                 color = ColorUtils.rgbToColor(r, g, b);
                 break;
                 
             case RGB_B:
                 r = rgb[0];
                 g = rgb[1];
                 b = xPos * 256 / width;
                 color = ColorUtils.rgbToColor(r, g, b);
                 break;
                 
             case HSV_H:
                 h = xPos * 360 / width;
                 s = hsv[1];
                 v = hsv[2];
                 color = ColorUtils.hsvToColor(h,s,v);
                 break;
                 
             case HSV_S:
                 h = hsv[0];
                 s = xPos * 100 / width;
                 v = hsv[2];
                 color = ColorUtils.hsvToColor(h, s, v);
                 break;
                 
             case HSV_V:
                 h = hsv[0];
                 s = hsv[1];
                 v = xPos * 100 / width;
                 color = ColorUtils.hsvToColor(h, s, v);
                 
                 break;
                 
             default:
             case NONE:
                 color = Color.LTGRAY;
                 break;
         }
         
         return color;
     }
 
 }
