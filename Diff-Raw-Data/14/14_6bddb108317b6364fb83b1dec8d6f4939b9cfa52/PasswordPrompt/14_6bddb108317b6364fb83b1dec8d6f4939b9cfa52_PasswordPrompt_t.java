 package com.example.projectv1;
 
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Typeface;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class PasswordPrompt extends Activity {		// prompt user to enter a password in order to access preferences activity
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_password_prompt);
 		
 		Typeface qs1=Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Bold.ttf");
 		Typeface qs2=Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Regular.ttf");
 		 
 		
 		TextView prompt = new TextView(this);
 		prompt = (TextView) findViewById(R.id.passwordPrompt);
 		prompt.setTypeface(qs2);
 		
 		Button back = new Button(this);
 		back = (Button) findViewById(R.id.backFromPrompt);
 		back.setTypeface(qs1);
 		
 		Button pass = new Button(this);
 		pass = (Button) findViewById(R.id.submitPass);
 		pass.setTypeface(qs1);
 		
 		EditText et = new EditText(this);
 		et = (EditText) findViewById(R.id.ETpassword);
 		et.setText("");
 		
 		if(isPasswordSet())
 		{
 			prompt.setText(getResources().getString(R.string.main_pass_prompt));
 		}else
 		{
 			prompt.setText(getResources().getString(R.string.passwordNotSet));
 		}
 	}
 	
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		EditText et = new EditText(this);
 		et = (EditText) findViewById(R.id.ETpassword);
 		et.setText("");
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
 		case R.id.submitPass:
 			
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
			//Intent openMenu = new Intent("com.project.MENU");
			//startActivity(openMenu);
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
