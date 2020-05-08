 package jp.ddo.neko_daisuki.android.widget;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.util.AttributeSet;
 
 class RotatingUzumakiSlider extends UzumakiSlider {
 
     public RotatingUzumakiSlider(Context context) {
         super(context);
     }
 
     public RotatingUzumakiSlider(Context context, AttributeSet attrs) {
         super(context, attrs);
     }
 
     public RotatingUzumakiSlider(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
     }
 
     public void onDraw(Canvas canvas) {
         super.onDraw(canvas);
        canvas.rotate(this.getProgress(), this.getWidth() / 2, this.getHeight() / 2);
        this.drawUzumaki(canvas);
     }
 }
 
 // vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
