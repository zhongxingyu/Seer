 package csci422.final_project;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import csci422.final_project.R;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.view.View.OnFocusChangeListener;
 import android.widget.*;
 
 public class report extends Activity {
 	public void onCreate(Bundle b) {
 		super.onCreate(b);
 		setContentView(R.layout.report);
 
 		final EditText zombieCode = (EditText) findViewById(R.id.zombie);
 		final EditText humanCode = (EditText) findViewById(R.id.human);
 		final TimePicker time = (TimePicker) findViewById(R.id.time);
 		final DatePicker date = (DatePicker) findViewById(R.id.date);
 		final Button report = (Button) findViewById(R.id.reportKill);
 		
 		//remove text from human when selected
 		humanCode.setOnFocusChangeListener(new OnFocusChangeListener()
 		{
 			public void onFocusChange(View V, boolean hasFocus) {
 				if(hasFocus==true){
 					if(humanCode.getText().toString().compareTo("Human Player Code")==0){
 						humanCode.setText("");
 					}
 				}
 			}
 		});
 		
 		//set zombie code to that in file
 		String path = getInternalCacheDirectory();
		final String FILENAME = path + "/PlayerCodeFile" ;
 
 		int len = 1024;
 		byte[] buffer = new byte[len];
 		try {
 			FileInputStream fis = openFileInput(FILENAME);
 			int nrb = fis.read(buffer, 0, len);
 			while (nrb != -1) {
 				nrb = fis.read(buffer, 0, len);
 			}
 			System.out.println(buffer.toString());
 			zombieCode.setRawInputType(Integer.parseInt(buffer.toString()));
 			fis.close();
 		} catch (FileNotFoundException e) {
 			System.out.println("NO FILE");
 		} catch (IOException e) {
 			System.out.println("FAILED");
 		}
 		
 		
 		report.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View V) {
 				// Perform action on clicks
 				int zombie = Integer.parseInt(zombieCode.getText().toString());
 				int human = Integer.parseInt(humanCode.getText().toString());
 				int hour = time.getCurrentHour();
 				int min = time.getCurrentMinute();
 				String ap = " ";
 				if(hour < 12) {
 					ap = "AM";
 					if (hour==0){ 
 						hour = 12;
 					}
 				}
 				else {
 					ap = "PM";
 					if(hour > 12){
 						hour -=12;
 					}
 				}
 				int day = date.getDayOfMonth();
 				int month = date.getMonth();
 				int year = date.getYear();
 				String[] args= {String.valueOf(zombie), String.valueOf(human), String.valueOf(hour), String.valueOf(min), ap, String.valueOf(month), String.valueOf(day), String.valueOf(year)}; 
 				CgiReport report = new CgiReport("CgiGet", args, "GET");
 				report.reportKill();
 			}
 
 		});
 		//Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://inside.mines.edu/~kraber/report"));
 		//startActivity(i);
 	}
 	public String getInternalCacheDirectory() {
 	    String cacheDirPath = null;
 	    File cacheDir = getCacheDir();
 	    if (cacheDir != null) {
 	        cacheDirPath = cacheDir.getPath();
 	    }
 	    return cacheDirPath;        
 	}
 }
