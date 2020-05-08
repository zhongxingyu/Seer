 package org.getman.myresume;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 
 import org.getman.myresume.R;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.view.Menu;
import android.view.MenuItem;
 import android.view.View;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.Spinner;
 
 public class SendActivity extends Activity {
 	
 	SimpleDateFormat birthDateFormat = new SimpleDateFormat("dd.MM.yyyy",Locale.getDefault());
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_send);
         Intent intent = getIntent();
         if(intent.getBooleanExtra("showDialog", false)) {
         	String response = intent.getStringExtra("responseText");
         	AlertDialog.Builder ad = new AlertDialog.Builder(SendActivity.this);
         	ad.setTitle(R.string.replyFromEmployer);
         	ad.setMessage(response);
         	ad.setPositiveButton("OK", null);
         	ad.show();
         }
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.send, menu);
         return true;
     }
     
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		AboutAlertDialog.show(SendActivity.this);
		return true;
	}
    
     public void send(View view) {
     	Intent intent = new Intent(this, ResponseActivity.class);
     	
     	EditText editName = (EditText) findViewById(R.id.editResponse);
     	DatePicker dateBirthDate = (DatePicker) findViewById(R.id.dateBirthDate);
     	String birthDate = birthDateFormat.format(getDateFromDatePicker(dateBirthDate));
     	Spinner selectionGender = (Spinner) findViewById(R.id.selectionGender);
     	EditText editJobTitle = (EditText) findViewById(R.id.editJobTitle);
     	EditText editDesiredSalary = (EditText) findViewById(R.id.editDesiredSalary);
     	EditText editPhoneNumber = (EditText) findViewById(R.id.editPhoneNumber);
     	EditText editEmail = (EditText) findViewById(R.id.editEmail);
     	
     	intent.putExtra("name",editName.getText().toString());
     	intent.putExtra("birthDate",birthDate);
     	intent.putExtra("gender", selectionGender.getSelectedItem().toString());
     	intent.putExtra("jobTitle", editJobTitle.getText().toString());
     	intent.putExtra("desiredSalary", editDesiredSalary.getText().toString());
     	intent.putExtra("phoneNumber", editPhoneNumber.getText().toString());
     	intent.putExtra("email", editEmail.getText().toString());
     	startActivity(intent);
     }
     
     public static Date getDateFromDatePicker(DatePicker datePicker){
         int day = datePicker.getDayOfMonth();
         int month = datePicker.getMonth();
         int year =  datePicker.getYear();
         
         Calendar calendar = Calendar.getInstance();
         calendar.set(year, month, day);
 
         return calendar.getTime();
     }
     
 }
