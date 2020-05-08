 package net.vincentpetry.nodereviver.view;
 
 import android.graphics.Canvas;
 import android.graphics.Paint.Align;
 import android.text.TextPaint;
 
 public class TitleScreenView extends View {
 
     private TextView message;
     private TextView author;
     private ViewContext viewContext;
     private int middleY;
 
     public TitleScreenView(ViewContext viewContext){
         this.viewContext = viewContext;
         TextPaint paint = new TextPaint();
         paint.setARGB(255, 0, 192, 0);
         paint.setTypeface(viewContext.getTypeface());
         paint.setTextSize(viewContext.getFontHeightBig());
         paint.setTextAlign(Align.CENTER);
 
         TextPaint paint2 = new TextPaint();
         paint2.setARGB(255, 0, 192, 0);
         paint2.setTypeface(viewContext.getTypeface());
         paint2.setTextSize(viewContext.getFontHeightNormal());
         paint2.setTextAlign(Align.LEFT);
 
         message = new TextView(paint, "Tap the screen to start playing", viewContext.getWidth());
         author = new TextView(paint2, "Vincent Petry <PVince81@yahoo.fr>", viewContext.getWidth());
 
        middleY = (int)(viewContext.getHeight() / 2.0f - 50.0f);
     }
 
     @Override
     public void render(Canvas c) {
         message.render(c, 0, middleY);
         author.render(c, 0.0f, viewContext.getHeight() - author.getHeight() * 2.0f);
     }
 
 }
