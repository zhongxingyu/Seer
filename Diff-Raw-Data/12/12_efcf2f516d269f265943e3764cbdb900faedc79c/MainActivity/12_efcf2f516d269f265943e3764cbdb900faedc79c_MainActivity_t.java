 package com.portman.touchtest;
 
 
 import java.io.File;
 import java.io.FileWriter;
 
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
import android.widget.EditText;
 import android.widget.ImageView;
import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	private static final String DEBUG_TAG = "TouchTest";
 	
	private EditText edittext;
	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         ImageView imageView =  (ImageView) findViewById(R.id.imageView1);
         imageView.setOnTouchListener(new View.OnTouchListener() {
 
             @Override
             public boolean onTouch(View v, MotionEvent event) {
             	printSamples(event);
                 return true;
             }
         });
        
        edittext = (EditText) findViewById(R.id.editText);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     void printSamples(MotionEvent ev) {
     	File log_file; 
         File root = Environment.getExternalStorageDirectory();
         FileWriter log_writer = null;
         String s;
 		try {
			log_file = new File(root,edittext.getText() + ".txt");
             if(!log_file.exists()) {
                     log_file.createNewFile();
                 }
             log_writer = new FileWriter(log_file, true);
 			
 	        
 	        final int historySize = ev.getHistorySize();
 	        final int pointerCount = ev.getPointerCount();
 	        for (int h = 0; h < historySize; h++) {
 	            for (int p = 0; p < pointerCount; p++) {
 		            s = ev.getHistoricalEventTime(h) + ", "
 		            	+ ev.getDownTime() + ", "	
 		            	+ ev.getPointerId(p) + ", " 
 		            	+ ev.getHistoricalX(p, h) + ", " 
 		                + ev.getHistoricalY(p, h) + ", "
 		                + ev.getHistoricalPressure(p,h) + ", "
 		                + ev.getHistoricalSize(p, h) + "\n";
 		            log_writer.append(s);
 	            }
 	        }
 	        for (int p = 0; p < pointerCount; p++) {
 	            s = ev.getEventTime() + ", "
 	            		+ ev.getDownTime() + ", "
 		            	+ ev.getPointerId(p) + ", " 
 		            	+ ev.getX(p) + ", " 
 		                + ev.getY(p) + ", "
 		                + ev.getPressure(p) + ", "
 		                + ev.getSize(p) + "\n";
 	            log_writer.append(s);
 	        }
 	        
 	        log_writer.flush();
 	        log_writer.close();
 	        
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
     
 }
