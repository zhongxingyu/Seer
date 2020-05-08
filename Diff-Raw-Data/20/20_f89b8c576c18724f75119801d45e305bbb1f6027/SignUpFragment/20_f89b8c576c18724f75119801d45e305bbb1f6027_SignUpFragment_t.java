 package com.example.grubber;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 import com.example.grubber.R;
 import com.example.grubber.R.layout;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
import android.app.Fragment;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 
 public class SignUpFragment extends Fragment implements View.OnClickListener  {
 	
 	public final int pwdToShort = 6;
 	public final String pwdToShortError = "Password needs to be at least 6 characters.";
 	public final String noValid = "Not Valid.";
 	public final String pwdDiffError = "The passwords do not match.";
 	public final String firstnameError = "First name is required.";
 	public final String lastnameError = "Last name is required.";
 	public final String usernameError = "User name is required.";
 	//UI Id
 	private EditText usernameET ;
 	private EditText pwdET;
 	private EditText reenterpwdET;
 	private EditText lastnameET;
 	private EditText firstnameET;
 	private EditText emailET;
 	
 	
 	private Button confirmBtn;
 	private View registerView;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 					
 	}
 
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
 		registerView = inflater.inflate(R.layout.fragment_signup, container, false);
 		
 		//getActionBar().setDisplayHomeAsUpEnabled(true);
 		
 		usernameET =  (EditText) registerView.findViewById(R.id.signup_username_ET);
 		pwdET = (EditText) registerView.findViewById(R.id.signup_pwd_ET);
 		reenterpwdET = (EditText) registerView.findViewById(R.id.signup_pwd_reenter_ET);
 		lastnameET = (EditText) registerView.findViewById(R.id.signup_lastname_ET);
 		firstnameET = (EditText) registerView.findViewById(R.id.signup_firstname_ET);
 		emailET = (EditText) registerView.findViewById(R.id.signup_email_ET);
 		
 		confirmBtn = (Button) registerView.findViewById(R.id.signup_confirm_btn);
 		confirmBtn.setOnClickListener(this);	
 
 		return registerView;
 
 	}
 	
 	/*
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		getMenuInflater().inflate(R.menu.register, menu);
 		return true;
 	}
 	*/
 	
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
 	      case R.id.action_forgot_password:
 	          break;   
 	       // Respond to the action bar's Up/Home button
 	      case android.R.id.home:
 	    	  getActivity().finish();
 	          return true;	          
 	      default:
 	    	  break;
       }
 
       return true;
     }
 
 	@Override
 	public void onClick(View v) {
 	
 		if(v.getId() == R.id.signup_confirm_btn){
 			
 			boolean errorCheck = false;
 			if(!isValidEmail( emailET.getText().toString())){
 				emailET.setError(noValid);
 				errorCheck = true;
 			}
 			 if(firstnameET.length() == 0 ){
 				firstnameET.setError(firstnameError);
 				errorCheck = true;
 			}
 			if(lastnameET.length() == 0 ){
 				lastnameET.setError(lastnameError);
 				errorCheck = true;
 			}
 			if(usernameET.length() == 0){
 				usernameET.setError(usernameError);
 				errorCheck = true;
 			}
 			 if(pwdET.length() < pwdToShort ){
 				pwdET.setError(pwdToShortError);
 				errorCheck = true;
 			}
 		   if(!pwdET.getText().toString().equals(reenterpwdET.getText().toString())){
 				reenterpwdET.setError(pwdDiffError);
 				errorCheck = true;
 			}
 		   
 		   //error the input text
 			if(!errorCheck){
 				RegisterTask task = new RegisterTask();
 				task.execute((Void) null);
 			}
 		}
 	}
 	
 	public final static boolean isValidEmail(CharSequence target) {
 	    if (target == null) {
 	        return false;
 	    } else {
 	    	 return  android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
 	    }
 	    
 	}
 	
 	private class RegisterTask extends AsyncTask<Void, Void, Boolean> {
 
 		protected Boolean doInBackground(Void... params) {
 			final JsonObject obj = userRegister(emailET.getText().toString(),pwdET.getText().toString(),usernameET.getText().toString(),firstnameET.getText().toString(), 
 					lastnameET.getText().toString());
 	
 			if(obj != null){
 				getActivity().runOnUiThread(new Runnable() {
 					public void run() {
 						//Strip off the quotes around the message!
 						String msg = obj.get("message").toString();
 						msg = msg.substring(1, msg.length()-1);
 					    Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
 					    }
 					});
 				if(obj.get("result").getAsBoolean()){
 					getActivity().finish();
 				}
 				return obj.get("result").getAsBoolean();
 			}
 			return false;
 		}
 
 		
 		protected JsonObject userRegister(String email, String password, String username, String firstname, String lastname){
 			URL url;
 			URLConnection uc;
 			BufferedReader data;
 
 			try {
 				url = new URL("http://cse190.myftp.org:8080/cse190/createUser?email=" + email + "&password=" + password + "&username=" + username
 						  + "&first_name=" + firstname + "&last_name=" + lastname);
 				uc = url.openConnection();
 				data = new BufferedReader(new InputStreamReader(uc.getInputStream()));
 				String inputLine = data.readLine();
 				JsonParser parser = new JsonParser();
 				return (JsonObject) parser.parse(inputLine);
 
 			} catch (Exception e) {
 				//No Connection
 				return null;
 			}
 		}
 
 	}
 }
