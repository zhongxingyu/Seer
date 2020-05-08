 package andr.lexibook.mylittlestory.lrrh.ui;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.MotionEvent;
 import android.view.View;
 
 import java.io.IOException;
 
 /**
  * @author hezi
  */
 public class Splash extends BaseActivity implements View.OnTouchListener {
 
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.splash_screen);
         mPlayer = factory.getSplash();
         try {
             mPlayer.prepare();
             mPlayer.start();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         new Handler().postDelayed(new Runnable() {
             @Override
             public void run() {
                 toMenu();
             }
         }, 1000);
 
     }
 
     @Override
     public boolean onTouch(View view, MotionEvent motionEvent) {
         toMenu();
         return true;
     }
 
     private void toMenu() {
         System.out.println(" Splash " + readMode.isFirst());
         if (readMode.isFirst()) {
             readMode.setFirst(false);
             io.save(readMode);
             toPage(LangSelect.class);
         } else {
             toPage(Menu.class);
         }
        toPage(LangSelect.class);
         finish();
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         mPlayer.release();
     }
 }
