 package jp.ddo.neko_daisuki.android.widget;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.util.AttributeSet;
 import android.util.Log;
 
 class RotatingUzumakiSlider extends UzumakiSlider {
 
     private static String LOG_TAG = "RotatingUzumakiSlider";
 
     private int headerSize;
 
     public RotatingUzumakiSlider(Context context) {
         super(context);
         this.initialize();
     }
 
     public RotatingUzumakiSlider(Context context, AttributeSet attrs) {
         super(context, attrs);
         this.initialize();
     }
 
     public RotatingUzumakiSlider(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         this.initialize();
     }
 
     public void onDraw(Canvas canvas) {
         super.onDraw(canvas);
         this.drawRotatingUzumaki(canvas);
         this.drawHeader(canvas);
     }
 
     private void drawHeader(Canvas canvas) {
         Path path = new Path();
         path.moveTo(this.computeHeaderPosition(), this.getHeight() / 2);
         path.rLineTo(this.headerSize, - this.headerSize);
         path.rLineTo(-2 * this.headerSize, 0);
         path.close();
 
         Paint paint = new Paint();
         paint.setColor(Color.YELLOW);
         paint.setStyle(Paint.Style.FILL);
         canvas.drawPath(path, paint);
     }
 
     private void initialize() {
         this.headerSize = 42;
     }
 
     private int computeHeaderPosition() {
         int center = this.getWidth() / 2;
         int innerRadius = this.getAbsoluteInnerDiameter() / 2;
         int max = this.getMax();
         int step = max - this.getProgress();
         int size = max - this.getMin();
         int outerRadius = this.getAbsoluteOuterDiameter() / 2;
         int span = (outerRadius - innerRadius) * step / size;
         return center + innerRadius + span;
     }
 
     private void drawRotatingUzumaki(Canvas canvas) {
        int x = this.getWidth() / 2;
        int y = this.getHeight() / 2;
        int min = this.getMin();
        int progress = this.getProgress() - min;
        int size = this.getMax() - min;
        int degree = this.getSweepAngle() * progress / size;
         canvas.save();
         try {
            canvas.rotate(- degree, x, y);
             this.drawUzumaki(canvas);
         }
         finally {
             canvas.restore();
         }
     }
 }
 
 // vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
