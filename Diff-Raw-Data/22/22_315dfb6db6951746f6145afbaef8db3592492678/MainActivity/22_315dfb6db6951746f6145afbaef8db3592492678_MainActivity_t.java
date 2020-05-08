 package com.example.AndroidThreadPool;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Toast;
 
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingDeque;
 import java.util.concurrent.TimeUnit;
 
 public class MainActivity extends Activity implements View.OnClickListener {
     /**
      * Called when the activity is first created.
      */
    CustomThreadPoolExecutor executor = null;

     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         Button buttonLaunch = (Button) findViewById(R.id.buttonlaunch);
         buttonLaunch.setOnClickListener(this);
     }
 
     @Override
     public void onClick(View v) {
         Toast.makeText(getApplicationContext(), "clicked!", Toast.LENGTH_SHORT);
         BlockingQueue<Runnable> bQueue = new LinkedBlockingDeque<Runnable>(3) {
             @Override
             public void put(Runnable runnable) throws InterruptedException {
                 super.addFirst(runnable);
             }
         };
       executor = new CustomThreadPoolExecutor(3,3,10, TimeUnit.SECONDS,bQueue);
         executor.execute(new TheTask("task1"));
         executor.execute(new TheTask("task2"));
         executor.execute(new TheTask("task3"));
         executor.execute(new TheTask("task4"));
        executor.shutdown();
     }
 }
