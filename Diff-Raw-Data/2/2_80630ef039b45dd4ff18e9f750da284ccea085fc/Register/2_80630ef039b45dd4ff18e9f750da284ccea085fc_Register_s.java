 package me.taedium.android;
 
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.widget.ViewSwitcher;
 
 import me.taedium.android.R;
 import me.taedium.android.api.Caller;
 
 public class Register extends HeaderActivity {
 
 	private static final int DOB_DIALOG_ID = 0;
 	public static final String KEY_SUCCESS = "success";
 	
 	private Button bDOB;
 	private int mMonth = 0;
 	private int mDay = 1;
 	private int mYear = 1990;
 	
 	@Override
     protected void onCreate(Bundle savedState) {
         super.onCreate(savedState);
         setContentView(R.layout.register);
         setTitle(R.string.register_title);
         
         initializeHeader();
         
         // Set header buttons to no longer be visable since we are on Register page
        Button btAdd = (Button)findViewById(R.id.bAdd);
         btAdd.setVisibility(ViewSwitcher.INVISIBLE);
         
         // Setup onClick event for DOB
         bDOB = (Button)findViewById(R.id.bDOB);
         bDOB.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				showDialog(DOB_DIALOG_ID);
 			}
 		});
         
         // Setup onClick event for Register button
         Button bRegister = (Button)findViewById(R.id.bRegister);
         bRegister.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				String user = ((EditText)findViewById(R.id.etUserName)).getText().toString();
 				String password = ((EditText)findViewById(R.id.etPassword)).getText().toString();
 				String confirmPassword = ((EditText)findViewById(R.id.etPasswordConfirm)).getText().toString();
 				String email = ((EditText)findViewById(R.id.etEmail)).getText().toString();
 				String dob = ((Button)findViewById(R.id.bDOB)).getText().toString();
 				
 				// verify all fields are completed
 				if(user.equalsIgnoreCase("") || password.equalsIgnoreCase("") || 
 						confirmPassword.equalsIgnoreCase("") || email.equalsIgnoreCase("") 
 						|| dob.equalsIgnoreCase("")) {
 					Toast.makeText(Register.this, getString(R.string.msgRegisterAllFieldsMandatory), Toast.LENGTH_LONG).show();
 				}
 				// verify password fields match
 				else if (!password.equals(confirmPassword)) {
 					Toast.makeText(Register.this, getString(R.string.msgPasswordsNotMatching), Toast.LENGTH_LONG).show();
 				}
 				else {
 					boolean success = Caller.getInstance(getApplicationContext()).addUser(user, password, email, getFormattedDOB());
 					if(success) {
 						Toast.makeText(Register.this, getString(R.string.msgRegistered), Toast.LENGTH_LONG).show();						
 						finish();
 					}
 					else {
 						Toast.makeText(Register.this, getString(R.string.msgRegistrationFailed), Toast.LENGTH_LONG).show();
 					}					
 				}
 			}
 		});
 	}
     
     // the callback received when the user "sets" the date in the dialog
     private DatePickerDialog.OnDateSetListener mDateSetListener =
         new DatePickerDialog.OnDateSetListener() {
 
             public void onDateSet(DatePicker view, int year, 
                                   int monthOfYear, int dayOfMonth) {
                 mYear = year;
                 mMonth = monthOfYear;
                 mDay = dayOfMonth;
                 bDOB.setText(getFormattedDOB());
             }
     };
             
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
         case DOB_DIALOG_ID:
             return new DatePickerDialog(this,
                         mDateSetListener,
                         mYear, mMonth, mDay);
         }
         return null;
     }
     
     // helper to get DOB in format required by API (MM/DD/YYYY)
     private String getFormattedDOB() {
     	String dob;
     	// month
     	int month = mMonth + 1;
     	if(month < 10) dob = "0" + month + "/";
     	else dob = Integer.toString(month) + "/";
     	
     	// day
     	if (mDay < 10) dob = dob + "0" + mDay + "/";
     	else dob = dob + mDay + "/";
     	
     	// year
     	dob = dob + mYear;
     	
     	return dob;
     }
 }
