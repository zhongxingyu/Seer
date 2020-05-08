 package com.example.projectv1;
 
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 
 public class PasswordPrompt extends Activity {		// prompt user to enter a password in order to access preferences activity
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_password_prompt);
 		
 		TextView prompt = new TextView(this);
 		prompt = (TextView) findViewById(R.id.passwordPrompt);
 		
 		if(isPasswordSet())
 		{
 			prompt.setText(getResources().getString(R.string.main_pass_prompt));
 		}else
 		{
 			prompt.setText(getResources().getString(R.string.passwordNotSet));
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_password_prompt, menu);
 		return true;
 	}
 	public void buttonClick(View v)
 	{
 		switch(v.getId())
 		{
 		case R.id.sumitPass:
 			
 			TextView pass = (TextView) findViewById(R.id.ETpassword);
 			long password = pass.getText().toString().hashCode();
 			
 
 			Log.v("PasswordPrompt","before startActivity");
 				
 			  Intent i = new Intent(PasswordPrompt.this, Preferences.class);
 	          i.putExtra("submitted_pass", password);
 			  startActivity(i);	     
 		      Log.v("PasswordPrompt","after startActivity");
 			
 			break;
 			
 		case R.id.backFromPrompt:
 			
 			finish();
 			break;
 		}
 	}
 	private boolean isPasswordSet()
 	{
 		boolean x=false; 
 		
 		SharedPreferences prefs = this.getSharedPreferences("com.example.projectv1", Context.MODE_PRIVATE);
 						
 		Long savedPass = prefs.getLong("savedPass", 0);
 		
 		if (savedPass!=0)
 		{
 			x=true;
 		}
 		return x;
 	}
 }
