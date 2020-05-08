 package com.noveogroup.android.task.example;
 
 import android.os.Bundle;
 import android.view.View;
 import com.noveogroup.android.task.Task;
 import com.noveogroup.android.task.TaskEnvironment;
import com.noveogroup.android.task.TaskHandler;
 import com.noveogroup.android.task.ui.AndroidTaskExecutor;
 
 public class SimpleTaskExampleActivity extends ExampleActivity {
 
     private AndroidTaskExecutor executor = new AndroidTaskExecutor(this);
 
     @Override
     protected void onResume() {
         super.onResume();
         executor.onResume();
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         executor.onPause();
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         loadWebViewExample();
 
         findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 executor.execute(new Task() {
                     @Override
                    public void run(TaskHandler handler, TaskEnvironment env) throws Throwable {
                         Utils.download(3000);
                     }
                 });
             }
         });
     }
 
 }
