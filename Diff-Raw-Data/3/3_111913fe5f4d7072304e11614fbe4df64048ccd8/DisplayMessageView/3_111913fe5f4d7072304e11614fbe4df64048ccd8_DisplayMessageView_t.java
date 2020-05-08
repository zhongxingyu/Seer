 package jp.arrow.angelforest.tapnumbers.main;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Bitmap.Config;
 import android.util.Log;
 import android.view.View;
 
 
 public class DisplayMessageView extends View {
     public static final int STATE_NONE = 0;
     public static final int STATE_READY = 0;
     public static final int STATE_GO = 1;
     public static final int STATE_FINISHED = 2;
 
     public static final int REFRESH = 1;
     public static final int TEXTAREA_WIDTH = 200;
     public static final int TEXTAREA_HEIGHT = 60;
 
     private Canvas dispCanvas;
     private Bitmap bitmap;
     private Paint paint;
     private int state = STATE_NONE;
     private int counter = 0;
 
     private TapTheNumbersActivity activity;
 
     public DisplayMessageView(Context context) {
         super(context);
         activity = (TapTheNumbersActivity)context;
 
         paint = new Paint();
         createNewBitmap();
 
         Thread th = new Thread(new Runnable() {
             @Override
             public void run() {
                 try {
                     displayDrawing(dispCanvas);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         });
         th.start();
     }
 
     @Override
     protected void onDraw(Canvas canvas) {
         super.onDraw(canvas);
 
         canvas.drawBitmap(bitmap, 200, 200, null);
         invalidate();
     }
 
     private void displayDrawing(Canvas canvas) throws Exception {
         while(true) {
             //init bitmap
             canvas.drawRGB(0, 0, 0);
 
             //draw Message
             chooseDisplay(canvas);
 
             Thread.sleep(REFRESH);
         }//while
     }
 
     private void createNewBitmap() {
         bitmap = Bitmap.createBitmap(TEXTAREA_WIDTH, TEXTAREA_HEIGHT, Config.RGB_565);
         dispCanvas = new Canvas(bitmap);
     }
 
     public void reset() {
         state = STATE_READY;
         counter = 0;
     }
 
     public void chooseDisplay(Canvas canvas) {
         switch(state) {
         case STATE_READY:
             displayReady(canvas);
             break;
         case STATE_GO:
             displayGo(canvas);
             break;
         case STATE_FINISHED:
             displayFinished(canvas);
             break;
         }
     }
 
     private void displayReady(Canvas canvas) {
         paint.setColor(Color.CYAN);
         canvas.drawText("READY...", 0, 10, paint);
     }
 
     private void displayGo(Canvas canvas) {
         paint.setColor(Color.CYAN);
        //TODO counter round up
         canvas.drawText("Go! " + counter, 0, 10, paint);
         counter++;
     }
 
     private void displayFinished(Canvas canvas) {
         paint.setColor(Color.CYAN);
         canvas.drawText("Finished!! " + counter, 0, 10, paint);
     }
 
     public int getState() {
         return state;
     }
 
     public void setState(int state) {
         this.state = state;
     }
 
 }
