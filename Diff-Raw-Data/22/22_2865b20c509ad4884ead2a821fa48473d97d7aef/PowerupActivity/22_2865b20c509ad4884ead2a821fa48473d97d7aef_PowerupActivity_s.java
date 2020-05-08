 package com.sternerlearn;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 
 public class PowerupActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_powerup);
 		
         // First check if they've logged in
 		SharedPreferences settings = getSharedPreferences(Types.PREFS_FILE, MODE_PRIVATE);
 		
 		String login = settings.getString(Types.LOGIN_KEY, "null");
 		if( login != "null" )
 		{
 			// If we have stored data for them, go ahead and log them in
 			String pw = settings.getString(Types.PW_KEY, "null");
 	    	AsyncCallLogin task = new AsyncCallLogin();
 	    	task.execute( login, pw );			
 		}
 		else
 		{
 			// Otherwise just push the login page
 			end();
 		}
 	}
 
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 	}
 	
 	private void end()
 	{
 		finish();
 		startActivity(new Intent(this, LoginActivity.class));
 	}
 	
 	public Account login(String... values)
 	{
         Account account = new Account( values[0], values[1] ); 
         
         if( Types.AccountType.PARENT.ordinal() == account.mAccountType )
         {
         	// If we're a parent, check if we have a student saved off
         	SharedPreferences settings = getSharedPreferences(Types.PREFS_FILE, MODE_PRIVATE);
         	
     		String login = settings.getString(Types.STUDENT_LOGIN_KEY, "null");
     		if( login != "null" )
     		{
     			// If we have stored data for them, go ahead and log them in
     			String pw = settings.getString(Types.STUDENT_PW_KEY, "null");
     			
     			// Use the student account instead of the parent account
     	    	Account student = new Account( login, pw );	
     	    	account.mLinkedAccount = student;
     	    	student.mLinkedAccount = account;
     		}
     		else
     		{
     			// It doesn't seem like this should happen, but just in case...
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
 	
 	public void finishLogin(Account acct)
 	{
 
     	boolean error = false;
     
     	if( acct.mValid )
     	{
 			if( Types.AccountType.PARENT.ordinal() == acct.mAccountType )
 			{
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
 				SharedData.getInstance().setAccount( acct );
 				startActivity(new Intent(this, TeacherMainMenu.class));	
 			}
 			else if( Types.AccountType.STUDENT.ordinal() == acct.mAccountType )
 			{
 				SharedData.getInstance().setAccount( acct );
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
     		// Just let them log in if somehow our saved login failed
     		end();
     	}
 	}
 	
     private class AsyncCallLogin extends AsyncTask<String, Void, Account> {
         @Override
         protected Account doInBackground(String... values) {
         	return login( values );
         }
 
         @Override
         protected void onPostExecute(Account value)
         {
         	finishLogin(value);        	
         }
 
         @Override
         protected void onPreExecute() {}
 
         @Override
         protected void onProgressUpdate(Void... values) {}
 
     }	
 
 }
