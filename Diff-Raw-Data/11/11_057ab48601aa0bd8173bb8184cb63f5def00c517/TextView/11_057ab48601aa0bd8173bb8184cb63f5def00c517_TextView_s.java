 package net.vincentpetry.nodereviver.view;
 
 import android.graphics.Canvas;
 import android.text.Layout;
 import android.text.StaticLayout;
 import android.text.TextPaint;
 
 public class TextView {
 
     private TextPaint paint;
     private StaticLayout layout;
 
     public TextView(TextPaint paint){
         this.paint = paint;
     }
 
     public void setText(String text, int width){
         layout = new StaticLayout(text,
                 paint,
                 width,
                 Layout.Alignment.ALIGN_NORMAL,
                 1.0f,
                 0.0f,
                 false);
     }
 
     public void render(Canvas c, float x, float y) {
         if (layout != null){
             c.save();
             // the position in question is the center of the text
            c.translate(x + layout.getWidth() / 2.0f, y + layout.getHeight() / 2.0f);
             layout.draw(c);
 
             c.restore();
         }
     }
 
     public int getHeight(){
         if (layout != null){
             return layout.getHeight();
         }
         return 0;
     }
 
 }
