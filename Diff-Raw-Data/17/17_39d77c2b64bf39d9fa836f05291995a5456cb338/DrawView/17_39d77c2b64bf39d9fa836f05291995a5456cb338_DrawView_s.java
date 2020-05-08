 package com.example;
 
 import java.io.*;
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.util.AttributeSet;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 
 
 public class DrawView extends View implements OnTouchListener, Serializable {
     Paint paint = new Paint();
     Point currentPoint;
     Point previousPoint;
     Stroke currentStroke;
     boolean isErasing = false;
 
     ArrayList<Stroke> drawing = new ArrayList<Stroke>();
 
     public DrawView(Context context, AttributeSet set) {
         super(context, set);
         init();
     }
 
     public DrawView(Context context) {
         super(context);
         init();
     }
 
     public DrawView(Context context, Paint paint) {
         super(context);
         init();
         this.paint = paint;
     }
 
 
     public void init() {
         setFocusable(true);
         setFocusableInTouchMode(true);
 
         this.setOnTouchListener(this);
         this.setBackgroundColor(Color.WHITE);
     }
 
     @Override
     public void onDraw(Canvas canvas) {
         //basically draw all the strokes;
         for (Stroke s : drawing) {
             s.draw(canvas);
         }
     }
 
     private Paint getEraserPaint() {
         Paint p = new Paint();
         p.setStrokeWidth(50);
         p.setColor(Config.DEFAULT_BG_COLOR);
         return p;
     }
 
     public boolean onTouch(View view, MotionEvent event) {
         //this is meant to be a scrolling action, not a draw
         if (Config.scrollOn)
             return true;
 
         //This is the start of a stroke
         if (event.getAction() == MotionEvent.ACTION_DOWN) {
             float x = event.getX();
             float y = event.getY();
             if (isErasing) {
                 Paint p = getEraserPaint();
                 currentPoint = new Point(x, y, p);
             } else
                 currentPoint = new Point(x, y, Config.globalPaintBrush);
             previousPoint = currentPoint;
             currentStroke = new Stroke();
             drawing.add(currentStroke);
         }
         //This is moving from the previous point to the current point
         else if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_UP) {
             float x = event.getX();
             float y = event.getY();
             if (isErasing) {
                 Paint p = getEraserPaint();
                 currentPoint = new Point(x, y, p);
             } else
                 currentPoint = new Point(x, y, Config.globalPaintBrush);
 
             Line l = new Line(previousPoint, currentPoint);
             currentStroke.addLine(l);
         }
 
         invalidate();
         previousPoint = currentPoint;
         return true;
     }
 
     public void writeObject(ObjectOutputStream out) throws IOException {
         out.writeObject(previousPoint);
         out.writeObject(currentPoint);
         out.writeObject(currentStroke);
         out.writeObject(drawing);
     }
 
     public void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
         previousPoint = (Point) in.readObject();
         currentPoint = (Point) in.readObject();
         currentStroke = (Stroke) in.readObject();
         drawing = (ArrayList<Stroke>) in.readObject();
         invalidate();
     }
 
     public void undoLastAction() {
         if (!drawing.isEmpty()) {
             drawing.remove(drawing.size() - 1);
             invalidate();
         }
     }
 
 }
