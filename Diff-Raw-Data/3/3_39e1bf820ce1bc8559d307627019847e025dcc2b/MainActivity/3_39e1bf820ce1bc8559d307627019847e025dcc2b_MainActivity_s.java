 package uag.basket;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 import org.apache.cordova.*;
 
 public class MainActivity extends DroidGap
 {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
        //super.loadUrl("file:///android_asset/www/barcodescanner-demo.html");//TMP
         super.loadUrl("file:///android_asset/www/uag-basket-view.html");
        //super.loadUrl("file:///android_asset/www/test.html");//TMP
     }
 }
