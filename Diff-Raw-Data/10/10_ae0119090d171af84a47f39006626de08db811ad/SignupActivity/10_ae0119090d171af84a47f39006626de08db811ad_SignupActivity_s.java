 package org.giv2giv;
 
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map.Entry;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.util.Pair;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class SignupActivity extends Activity
 {
 	private Hashtable<String, Pair<Boolean, String>> signupInfo;
 	
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		signupInfo = new Hashtable<String, Pair<Boolean, String>>();
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.sign_up_screen);
 		Button nextButton = (Button)findViewById(R.id.signUpButton);
 		/*Button loginButton = (Button)findViewById(R.id.connectButton);
 		final EditText usernameEntry = (EditText)findViewById(R.id.usernameField);
 		final EditText connectEntry = (EditText)findViewById(R.id.connectField);
 		ImageButton setButton = (ImageButton)findViewById(R.id.dashSetButton);*/
         nextButton.setOnClickListener(new OnClickListener() 
         {
             @Override
             public void onClick(View v)
             {
             	Intent nextScreen = new Intent(v.getContext(), ConfirmationActivity.class);
             	//Second address is at the end as it is the only non-mandatory field
             	//This is done to simplify the check for empty responses immediately below
             	signupInfo.put("First Name", new Pair<Boolean, String>(true, 
             			((EditText)findViewById(R.id.firstNameField)).getText().toString()));
             	signupInfo.put("Last Name", new Pair<Boolean, String>(true, 
             			((EditText)findViewById(R.id.lastNameField)).getText().toString()));
             	signupInfo.put("First Address", new Pair<Boolean, String>(false, 
             			((EditText)findViewById(R.id.firstAddressField)).getText().toString()));
             	signupInfo.put("Second Address", new Pair<Boolean, String>(false, 
             			((EditText)findViewById(R.id.secondAddressField)).getText().toString()));
             	signupInfo.put("City",new Pair<Boolean, String>(false, 
             			((EditText)findViewById(R.id.cityField)).getText().toString()));
             	signupInfo.put("State", new Pair<Boolean, String>(false, 
             			((EditText)findViewById(R.id.stateField)).getText().toString()));
             	signupInfo.put("Zip Code", new Pair<Boolean, String>(false, 
             			((EditText)findViewById(R.id.zipcodeField)).getText().toString()));
 //            	signupInfo.put("country", new Pair<Boolean, String>(false, 
 //            			((EditText)findViewById(R.id.stateField)).getText().toString()));
             	String emailAddress = ((EditText)findViewById(R.id.emailField)).getText().toString();
             	signupInfo.put("Email Address", new Pair<Boolean, String>(true, emailAddress));
             	String firstPW = ((EditText)findViewById(R.id.passwordField)).getText().toString();
             	signupInfo.put("Password", new Pair<Boolean, String>(true, firstPW));
             	String secondPW = ((EditText)findViewById(R.id.confPasswordField)).getText().toString();
             	signupInfo.put("Password Confirmation", new Pair<Boolean, String>(true, secondPW));
             	
             	HashMap<String, String> infoMap = new HashMap<String, String>();
             	Bundle info = new Bundle();
            	if (firstPW.equals(secondPW))
             	{
             		createDialog(v.getContext(), "Password mismatch",
             				"Please enter the same password in both fields.");
             		return;
             	}
             	if (UpdateQueue.CheckEmailInUse(emailAddress))
             	{
             		createDialog(v.getContext(), "Email in Use",
             				"Sorry, that email is in use. Please enter a different email.");
             		return;
             	}
             	for (Entry<String, Pair<Boolean, String>> entry : signupInfo.entrySet())
             	{
             		Pair<Boolean, String> value = (Pair<Boolean, String>)entry.getValue();
             		Log.i("SIGNUP_INFO", entry.getKey() + value.second);
             		if (value.first && value.second.equals(""))
             		{
             			createDialog(v.getContext(), "Missing Information",
             					"Please fill in the following required field: " + entry.getKey());
                     	return;
             		}
             		infoMap.put(entry.getKey(), value.second);
             	}
             	info.putSerializable("info", infoMap);
             	nextScreen.putExtra("info", info);
             	startActivity(nextScreen);
             	return;
             }
         });
 	}
 
 	public void createDialog(Context context, String title, String message)
 	{
 		AlertDialog.Builder builder = new AlertDialog.Builder(context);
     	builder.setTitle(title).setCancelable(false)
     	.setMessage(message)
     	.setNegativeButton("Return", new DialogInterface.OnClickListener() 
     	{
     		public void onClick(DialogInterface dialog, int id) 
     		{
     			dialog.cancel();
     		}
     	});
     	AlertDialog alert = builder.create();
     	alert.show();
     	return;
 	}
 }
