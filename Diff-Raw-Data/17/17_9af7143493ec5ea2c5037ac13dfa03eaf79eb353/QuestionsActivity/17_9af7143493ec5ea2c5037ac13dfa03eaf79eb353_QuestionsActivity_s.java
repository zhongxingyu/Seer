 package ovgu.gruppe1.ehskapp;
 
 import java.io.FileNotFoundException;
 import java.lang.reflect.Array;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnKeyListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.view.View.OnClickListener;
 
 
 public class QuestionsActivity extends Activity implements OnKeyListener {
 	
 	EditText numberOfContactsView, hoursView, minutesView; 
 	String numberOfContacts, hours, minutes;
 	Button submitButton, abortButton;
 
 	/**
 	 * set the variables to the text fields and the button
 	 * set event listener
 	 */
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.question_layout);
         
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
 		final String usercode = preferences.getString("usercode", "");
         
         numberOfContactsView = (EditText) findViewById(R.id.editText1);
         hoursView = (EditText) findViewById(R.id.EditText01);
         minutesView = (EditText) findViewById(R.id.EditText02);
         
         minutesView.setOnKeyListener(this);
         hoursView.setOnKeyListener(this);
         numberOfContactsView.setOnKeyListener(this);
         
         submitButton = (Button) findViewById(R.id.button1);
         abortButton = (Button) findViewById(R.id.Button01);
         
         /**
          * 
          */
         submitButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if(hours == "" || minutes == "" || numberOfContacts==""){
 					showErrorMessager("Fehlende Eingabe");
 				} else {
 					String[] line = new String[8];//data which will be written into a csv file
					line[0]="";//user code
 					Date date = new Date();
 					line[1]= new SimpleDateFormat("dd.MM.yyyy").format(date);//date
 					line[2]="";//alert time
 					line[3]= new SimpleDateFormat("hh:mm").format(date);//answer time
 					line[4]="0";//wasn't aborted
 					line[5]=numberOfContacts;
 					line[6]=hours;
 					line[7]=minutes;
 					String filename = usercode + ".csv";
 					try {
 						CSVWriter.writeLine(line, filename);
 					} catch (FileNotFoundException e) {
 						e.printStackTrace();
 					}
 					finishActivity();
 				}
 				
 			}
 		});
         
         /**
          * action when the user aborts the question 
          */
         abortButton.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				String[] line = new String[8];//data which will be written into a csv file
 				line[0]="";//user code
 				Date date = new Date();
 				line[1]= new SimpleDateFormat("dd.MM.yyyy").format(date);//date
 				line[2]="";//alert time
 				line[3]= "-1";//answer time
 				line[4]="1";//was aborted
 				line[5]="-1";
 				line[6]="-1";
 				line[7]="-1";
 				String filename = ".csv";
 				try {
 					CSVWriter.writeLine(line, filename);
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				}
 				finishActivity();
 			}
 		});
 
     }
 	
 	/**
 	 * shows alert dialogue, for debugging purposes
 	 * @param message
 	 */
 	private void showErrorMessager(String message){
 		Context context = getApplicationContext();
 		int time = 3000;
 		
 		Toast toast = Toast.makeText(context, message, time);
 		toast.show();
 	}
 	
 	/**
 	 * closes the activity
 	 */
 	private void finishActivity(){
 		this.finish();
 	}
 
 	@Override
 	/**
 	 * onKey event listener for the text fields
 	 * get the string and store it in the variables
 	 */
 	public boolean onKey(View v, int keyCode, KeyEvent event) {
 		
 		if(v==numberOfContactsView){
 			String text = numberOfContactsView.getText().toString();
 			numberOfContacts=text;
 		}
 		
 		if(v==hoursView){
 			String text = hoursView.getText().toString();
 			hours=text;
 		}
 
 		if(v==minutesView){
 			String text = minutesView.getText().toString();
 			minutes=text;
 		}
 		
 		return false;
 	}	
 	
 }
