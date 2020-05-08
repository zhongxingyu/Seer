 package com.lossboys.customerapp;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.TextView;
 
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 public class CustomerRegister extends Activity {
 
 	Button btnRegister;
 	Button btnLinkToLogin;
 	EditText inputFirstName;
 	EditText inputLastName;
 	EditText inputEmail;
 	EditText inputPassword;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// Set View to register.xml
 		setContentView(R.layout.customer_register);
 
 		// Importing all assets like buttons, text fields
 		inputFirstName = (EditText) findViewById(R.id.reg_firstname);
 		inputLastName = (EditText) findViewById(R.id.reg_lastname);
 		inputEmail = (EditText) findViewById(R.id.reg_email);
 		inputPassword = (EditText) findViewById(R.id.reg_password);
 		btnRegister = (Button) findViewById(R.id.btnRegister);
 		btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);
 
 		// Register Button Click event
 		btnRegister.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				String firstname = inputFirstName.getText().toString();
 				String lastname = inputLastName.getText().toString();
 				String email = inputEmail.getText().toString();
 				String password = inputPassword.getText().toString();
 				
 				Context context = CustomerRegister.this;
 				CharSequence text;
 				int duration = Toast.LENGTH_SHORT;
 				boolean done = false;
 
 				// Building post parameters
 				// key and value pair
 				List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(4);
 				nameValuePair.add(new BasicNameValuePair("email", email));
 				nameValuePair.add(new BasicNameValuePair("password", password));
 				nameValuePair.add(new BasicNameValuePair("first", firstname));
 				nameValuePair.add(new BasicNameValuePair("last", lastname));
 
 				JSONObject registerJSON = CustomHTTP.makePOST("http://23.21.158.161:4912/create_acct.php", nameValuePair);
 
 				if (registerJSON != null) {
 					try {
 						String jsonResult = registerJSON.getString("error");
 						if (jsonResult.equals("1"))
 							text = "Please fill out all information.";
 						else if (jsonResult.equals("2"))
 							text = "Email is already registered.";
 						else if (jsonResult.equals("bad_email"))
 							text = "Email is invalid.";
 						else if (jsonResult.equals("bad_password"))
 							text = "Password must be 8-32 characters.";
 						else{
 							text = "Register successful.";
 							done = true;
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 						text = "Register failed.";
 					}
 				} else
 					text = "Register failed.";
 				
 				Toast toast = Toast.makeText(context, text, duration);
 				LinearLayout toastLayout = (LinearLayout) toast.getView();
 				TextView toastTV = (TextView) toastLayout.getChildAt(0);
 				toastTV.setTextSize(20);
 				toast.setGravity(Gravity.CENTER, 0, 0);
 				toast.show();
 				
 				if(done)
 					finish();
 			}
 		});
 
 		// Link to Login Screen
 		btnLinkToLogin.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				finish();
 			}
 		});
 	}
 }
