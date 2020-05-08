 package com.example.itsbeenawhile;
 
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.View;
 
 /**
  * Created by Fareki on 9/28/13.
  * Hijacked by Ongman shortly after.
  */
 public class NewTaskActivity extends Activity {
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_newtask);
     }
     
     
     
     
     
     public void saveTask(View view) {
     	try{
    	Calendar calendar = Calendar.getInstance();
    	long c = calendar.getTimeInMillis()/1000;
    	String task = "task" + Long.toString(c);
     	byte[] dateBytes = new byte[5];
     	dateBytes[0] = (byte) 2;
     	dateBytes[1] = (byte) 2;
     	dateBytes[2] = (byte) 2;
     	dateBytes[3] = (byte) 2;
     	
     	File file = new File("data/data/com.example.itsbeenawhile/task.txt");
     	FileOutputStream fos = getApplicationContext().openFileOutput(task+".txt", Context.MODE_PRIVATE);
     	fos.write(dateBytes);
     	fos.close();
     	} catch (IOException e) {
     		e.printStackTrace();
     	}
     	super.onBackPressed();
     }
     
 }
