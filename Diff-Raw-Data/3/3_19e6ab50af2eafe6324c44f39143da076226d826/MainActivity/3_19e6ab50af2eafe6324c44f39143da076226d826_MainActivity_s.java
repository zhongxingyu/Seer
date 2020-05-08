 package com.example.com.example.soundcloud;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.util.Log;
 import android.view.Menu;
 
 import com.soundcloud.api.ApiWrapper;
 import com.soundcloud.api.Token;
 
 public class MainActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                                       .permitNetwork()
                                       .build()
                                   );
 
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         this.doSomethingWithSoundcloud();
     }
 
     private void doSomethingWithSoundcloud() {
         try {
             ApiWrapper wrapper = new ApiWrapper("3b70c135a3024d709e97af6b0b686ff3",
                                                 "51ec6f9c19487160b5942ccd4f642053",
                                                 null,
                                                 null);
 
            //Token token = wrapper.login("adi2188", "abcd123");
            Token token = wrapper.login("simonzxc-1", "abc123");
 
             Log.d("DEBUG", "token is: " + token);
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 }
