 package com.noveogroup.android.task.example;
 
 import android.os.Bundle;
 import android.view.View;
 import com.noveogroup.android.task.Task;
 import com.noveogroup.android.task.TaskEnvironment;
 import com.noveogroup.android.task.TaskHandler;
 import com.noveogroup.android.task.TaskListener;
 import com.noveogroup.android.task.ui.AndroidTaskExecutor;
 
 public class TaskSequenceExampleActivity extends ExampleActivity {
 
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
                    public void run(TaskEnvironment env) throws Throwable {
                         Utils.download(1500);
                         executor.execute(
                                 new Task() {
                                     @Override
                                    public void run(TaskEnvironment env) throws Throwable {
                                         Utils.download(1500);
                                     }
                                 },
                                 new TaskListener.Default() {
                                     @Override
                                     public void onFinish(TaskHandler<?, ?> handler) {
                                         executor.execute(new Task() {
                                             @Override
                                            public void run(TaskEnvironment env) throws Throwable {
                                                 Utils.download(1500);
                                             }
                                         });
                                     }
                                 }
                         );
                     }
                 });
             }
         });
     }
 
 }
