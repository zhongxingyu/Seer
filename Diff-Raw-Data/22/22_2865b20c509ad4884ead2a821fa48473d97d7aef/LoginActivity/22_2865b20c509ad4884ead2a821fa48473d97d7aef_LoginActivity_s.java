 package com.sternerlearn;
 
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class LoginActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_login);
      
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     public void handleLogin
     	(
     	View aView
     	)
     {
     	String email = ((EditText)findViewById( R.id.editEmailAddress )).getText().toString();
     	String password = ((EditText)findViewById( R.id.editPassword )).getText().toString();
     	
     	AsyncCallLogin task = new AsyncCallLogin();
     	task.execute( email, password );
     }  
     
     public void finishLogin(Account acct)
     {
 		
     	boolean error = false;
     
     	if( acct.mValid )
     	{
 			if( Types.AccountType.PARENT.ordinal() == acct.mAccountType )
 			{
 				// Use the linked account since it contains the student information
 				Account ourAccount = acct;
 				if( acct.mLinkedAccount != null )
 				{
					ourAccount.mLinkedAccount = acct.mLinkedAccount;
 				}
 				SharedData.getInstance().setAccount(ourAccount);
 				startActivity(new Intent(this, MainMenu.class));	
 			}
 			else if( Types.AccountType.STAFF.ordinal() == acct.mAccountType )
 			{
 				SharedData.getInstance().setAccount(acct);
 				startActivity(new Intent(this, TeacherMainMenu.class));	
 			}
 			else if( Types.AccountType.STUDENT.ordinal() == acct.mAccountType )
 			{
 				SharedData.getInstance().setAccount(acct);
 				startActivity(new Intent(this, StudentMainMenu.class));
 				startService(new Intent(this, GPSReceiver.class));
 			}
 			else
 			{
 				error = true;			
 			}
     	}
     	else
     	{
     		error = true;
     	}
     	
     	if( error )
     	{
     		CharSequence text = "Error logging in";
     		Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
     		toast.show();
     	}
     	else
     	{
     		// Save their login information
     		SharedPreferences settings = getSharedPreferences(Types.PREFS_FILE, MODE_PRIVATE);
     		
     		SharedPreferences.Editor editor = settings.edit();
     		editor.putString(Types.LOGIN_KEY, acct.mEmailAddress );
     		editor.putString(Types.PW_KEY, acct.mPassword );
     		
     		editor.commit();
     	}
     }
     
     public Account login(String[] params)
     {
     	// Need to attempt to log them in
     	// If it succeeds, then send the account type to the MainMenu
         Account account = new Account( params[0], params[1] );
         
         if( Types.AccountType.PARENT.ordinal() == account.mAccountType )
         {
         	// If we're a parent, check if we have a student saved off
         	SharedPreferences settings = getSharedPreferences(Types.PREFS_FILE, MODE_PRIVATE);
         	
     		String login = settings.getString(Types.STUDENT_LOGIN_KEY, "null");
     		if( login != "null" )
     		{
     			// If we have stored data for them, go ahead and log them in
     			String pw = settings.getString(Types.STUDENT_PW_KEY, "null");
     			
     	    	Account student = new Account( login, pw );		
     	    	account.mLinkedAccount = student;
     	    	student.mLinkedAccount = account;
     		}
     		else
     		{
     			// Call web service to get linked information
     			account.updateLinkedAccount();
     			if( account.mLinkedAccount != null )
     			{
     				account.mLinkedAccount.mLinkedAccount = account;
     			}
     		}
     		
     		// If they aren't linked then don't bother doing anything
         }          
     	
     	return account;
     }
     
     public void handleRegister( View aView )
     {
     	startActivity(new Intent(this, RegisterActivity.class));
     }
     
     private class AsyncCallLogin extends AsyncTask<String, Void, Account> {
         @Override
         protected Account doInBackground(String... params) {
             return login(params);
         }
 
         @Override
         protected void onPostExecute(Account result) {
     		finishLogin( result );        	
         }
 
         @Override
         protected void onPreExecute() {
         }
 
         @Override
         protected void onProgressUpdate(Void... values) {
         }
 
     }     
 }
