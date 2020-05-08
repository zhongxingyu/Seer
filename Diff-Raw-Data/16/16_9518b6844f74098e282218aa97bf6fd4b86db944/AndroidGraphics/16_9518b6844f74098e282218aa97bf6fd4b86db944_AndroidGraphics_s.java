 package org.mklab.ishikura.graph.android;
 
 import org.mklab.ishikura.graph.graphics.Color;
 import org.mklab.ishikura.graph.graphics.Graphics;
 import org.mklab.ishikura.graph.graphics.LineType;
 
 import android.graphics.Canvas;
 import android.graphics.DashPathEffect;
 import android.graphics.Paint;
 import android.graphics.Paint.FontMetricsInt;
 import android.graphics.Paint.Style;
 import android.graphics.PathEffect;
 import android.graphics.Region;
 
 
 /**
  * Androidの{@link Canvas}に対して描画を行う{@link Graphics}の実装です。
  * 
  * @author ishikura
  * @version $Revision$, 2011/06/29
  */
 public class AndroidGraphics implements Graphics {
 
   private Canvas canvas;
   private Paint paint;
   private float alpha;
  private LineType lineType;
   private float lineWidth;
   private Color color;
 
   /**
    * {@link AndroidGraphics}オブジェクトを構築します。
    * 
    * @param canvas 描画対象
    */
   public AndroidGraphics(Canvas canvas) {
     if (canvas == null) throw new NullPointerException();
     this.canvas = canvas;
     this.paint = new Paint();
 
     setAlpha(1f);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void setAlpha(float alpha) {
     final int alphaInInt = (int)(alpha * 255);
     this.paint.setAlpha(alphaInInt);
     this.alpha = alpha;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public float getAlpha() {
     return this.alpha;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void setLineType(LineType type) {
     switch (type) {
       case DEFAULT:
         this.paint.setPathEffect(new PathEffect());
         break;
       case DOT:
         this.paint.setPathEffect(new DashPathEffect(new float[] {3f, 3f}, 0f));
         break;
     }
     this.lineType = type;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public LineType getLineType() {
     return this.lineType;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void setLineWidth(float width) {
     this.paint.setStrokeWidth(width);
     this.lineWidth = width;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public float getLineWidth() {
     return this.lineWidth;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void drawLine(int x1, int y1, int x2, int y2) {
     this.paint.setStyle(Style.STROKE);
     this.canvas.drawLine(x1, y1, x2, y2, this.paint);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void drawString(String str, int x, int y) {
    this.paint.setStyle(Style.STROKE);
     this.canvas.drawText(str, x, y, this.paint);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void setColor(Color c) {
     this.paint.setARGB(this.paint.getAlpha(), c.getRed(), c.getGreen(), c.getBlue());
     this.color = c;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public Color getColor() {
     return this.color;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void fillRect(int x, int y, int width, int height) {
     this.paint.setStyle(Style.FILL);
     this.canvas.drawRect(x, y, x + width, y + height, this.paint);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void drawRect(int x, int y, int width, int height) {
     this.paint.setStyle(Style.STROKE);
     this.canvas.drawRect(x, y, x + width, y + height, this.paint);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public int computeTextWidth(String text) {
     return (int)this.paint.measureText(text);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public int getTextHeight() {
     final FontMetricsInt fontMetrics = this.paint.getFontMetricsInt();
     return -fontMetrics.ascent + fontMetrics.descent;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public int getTextAscent() {
     return -this.paint.getFontMetricsInt().ascent;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public int getTextDescent() {
     return this.paint.getFontMetricsInt().descent;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void clipRect(int x, int y, int width, int height) {
     this.canvas.clipRect(x, y, x + width, y + height, Region.Op.INTERSECT);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void clearClip() {
     this.canvas.clipRect(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Region.Op.REPLACE);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void translate(int dx, int dy) {
     this.canvas.translate(dx, dy);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void rotate(double theta) {
     this.canvas.rotate((float)Math.toDegrees(theta));
   }
 
 }
