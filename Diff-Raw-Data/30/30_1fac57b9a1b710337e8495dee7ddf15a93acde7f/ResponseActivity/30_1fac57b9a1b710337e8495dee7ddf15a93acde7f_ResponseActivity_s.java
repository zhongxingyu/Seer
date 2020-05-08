 package org.getman.myresume;
 
 import org.getman.myresume.R;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class ResponseActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_response);
 		Intent intent = getIntent();
 		
 		TextView textName = (TextView) findViewById(R.id.textName);
 		TextView textBirthDate = (TextView) findViewById(R.id.textBirthDate);
 		TextView textGender = (TextView) findViewById(R.id.textGender);
 		TextView textJobTitle = (TextView) findViewById(R.id.textJobTitle);
 		TextView textDesiredSalary = (TextView) findViewById(R.id.textDesiredSalary);
 		TextView textPhoneNumber = (TextView) findViewById(R.id.textPhoneNumber);
 		TextView textEmail = (TextView) findViewById(R.id.textEmail);
 		
 		textName.setText(fromResources(R.string.name) + ": " + intent.getStringExtra("name"));
 		textBirthDate.setText(fromResources(R.string.birth_date) + ": " + intent.getStringExtra("birthDate"));
 		textGender.setText(fromResources(R.string.gender) + ": " + intent.getStringExtra("gender"));
 		textJobTitle.setText(fromResources(R.string.job_title) + ": " + intent.getStringExtra("jobTitle"));
 		textDesiredSalary.setText(fromResources(R.string.desired_salary) + ": " + intent.getStringExtra("desiredSalary"));
 		textPhoneNumber.setText(fromResources(R.string.phone_number) + ": " + intent.getStringExtra("phoneNumber"));
 		textEmail.setText(fromResources(R.string.email) + ": " + intent.getStringExtra("email"));
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.response, menu);
 		return true;
 	}
 	
 	public void response(View view) {
 		Intent intent = new Intent(this,SendActivity.class);
 		intent.putExtra("showDialog", true);
 		EditText editResponse = (EditText) findViewById(R.id.editResponse);
 		intent.putExtra("responseText", editResponse.getText().toString());
 		startActivity(intent);
 	}
 	
 	public String fromResources(int id) {
 		return getResources().getString(id);
 	}
 
 }
