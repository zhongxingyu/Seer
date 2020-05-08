 package jp.ddo.neko_daisuki.android.widget;
 
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Path;
 
 public class UzumakiDiagram
 {
     private int centerX;
     private int centerY;
     private int startAngle;
     private int sweepAngle;
     private int outerDiameter;
     private int innerDiameter;
     private Paint paint;
 
     public UzumakiDiagram(int centerX, int centerY, int startAngle, int sweepAngle, int outerDiameter, int innerDiameter, Paint paint) {
         this.centerX = centerX;
         this.centerY = centerY;
         this.startAngle = startAngle;
         this.sweepAngle = sweepAngle;
         this.outerDiameter = outerDiameter;
         this.innerDiameter = innerDiameter;
         this.paint = paint;
     }
 
     public void draw(Canvas canvas) {
         Point p = new Point();
         Path path = new Path();
         this.computePoint(p, this.startAngle);
         path.moveTo(p.x, p.y);
 
         int sweepAngle = Math.abs(this.sweepAngle);
         /*
          * About "resolution"
          * ==================
          *
          * I'm using Acer A500. Its screen is 10inch (25.4cm) and 1270x800pixel.
          * So it is 0.17mm/pixel. If I draw one circle as large as possible
          * (in other words, the circle's radius is 400pixel), the length of its
          * outline is about 2,500pixel (because of 2 * pi * radius). The length
          * for one degree is about 7pixel (2,500 / 360), it will be 2mm
          * (7pixel * 0.17mm/pixel).
          *
          * The outline length of four degrees is about 8mm. I felt that it is
          * smooth enough.
          */
         int resolution = 4;
         int direction = 0 < this.sweepAngle ? 1 : -1;
        for (int angle = 0; angle < sweepAngle; angle += resolution) {
             this.computePoint(p, this.startAngle + direction * angle);
             path.lineTo(p.x, p.y);
         }
 
         canvas.drawPath(path, this.paint);
     }
 
     private void computePoint(Point dest, int angle) {
         int diameterDelta = this.outerDiameter - this.innerDiameter;
         float ratio = (float)(this.startAngle - angle) / Math.abs(this.sweepAngle);
         float radius = (this.outerDiameter - diameterDelta * ratio) / 2;
         float x = radius * (float)Math.cos(Math.toRadians(angle));
         float y = radius * (float)Math.sin(Math.toRadians(angle));
         dest.x = centerX + x;
         dest.y = centerY + y;
     }
 
     private class Point {
 
         public float x = 0.0f;
         public float y = 0.0f;
     }
 }
 
 // vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
