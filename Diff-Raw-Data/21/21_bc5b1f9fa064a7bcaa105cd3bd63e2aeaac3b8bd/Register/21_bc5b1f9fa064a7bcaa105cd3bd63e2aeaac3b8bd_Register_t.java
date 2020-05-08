 package de.hska.shareyourspot.android.activites;
 
 import java.net.HttpURLConnection;
 
 import android.app.Activity;
 import android.content.Context;
import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import de.hska.shareyourspot.android.R;
 import de.hska.shareyourspot.android.domain.User;
 import de.hska.shareyourspot.android.helper.AlertHelper;
 import de.hska.shareyourspot.android.restclient.RestClient;
 
 public class Register extends Activity {
 
 	final Context context = this;
 	private RestClient restClient = new RestClient();
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.register);
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 		final Button registerButton = (Button) findViewById(R.id.registerBtnRegister);
 
 		registerButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				EditText email = (EditText) findViewById(R.id.registerInputEmail);
 				String inputEmail = email.getText().toString();
 
 				EditText username = (EditText) findViewById(R.id.registerInputUsername);
 				String inputUsername = username.getText().toString();
 
 				EditText passwordFirst = (EditText) findViewById(R.id.registerInputPasswordFirst);
 				String pw1 = passwordFirst.getText().toString();
 
 				EditText passworSec = (EditText) findViewById(R.id.registerInputPasswordSecound);
 				String pw2 = passworSec.getText().toString();
 
 				if (inputEmail.isEmpty() || inputUsername.isEmpty() || pw1.isEmpty() || pw2.isEmpty()) 
 					{
 					new AlertHelper(context, R.string.fillInputsTitle, R.string.fillInputsText, "Back").fireAlert();
 					return;
 					}
 					
 					
 				if (!pw1.equals(pw2)) {
 					new AlertHelper(context, R.string.passwordsNotMatchingTitle, R.string.passwordsNotMatchingText, "Back").fireAlert();
 					return;
 					}
 					User newUser = new User(inputEmail, inputUsername, pw1);
 					
 					int code = restClient.registerUser(newUser);
 					if(code ==  HttpURLConnection.HTTP_CREATED)
 					{
						AlertHelper alert = new AlertHelper(context, R.string.registerDoneTitle, R.string.registerDoneText);
						alert.alertDialogBuilder.setCancelable(false);
						alert.alertDialogBuilder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {          
					        @Override
					        public void onClick(DialogInterface dialog, int which) {
					            dialog.dismiss();
					            startLoginIntent();
					        }
					    });
						alert.fireAlert();
 					}
 					else
 					{
 						new AlertHelper(context, R.string.registerFailureTitle, R.string.registerFailureText, "Try Again").fireAlert();
 					}
 			}
 		});
 
 	}
 
 	public void startLoginIntent()
 	{
 		Intent intent = new Intent(this, MainActivity.class);
 		startActivity(intent);
 	}
 	
 	public void registerStart(View view) {
 		Intent intent = new Intent(this, Register.class);
 		startActivity(intent);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	    // Respond to the action bar's Up/Home button
 	    case android.R.id.home:
 	        NavUtils.navigateUpFromSameTask(this);
 	        return true;
 	    }
 	    return super.onOptionsItemSelected(item);
 	}
 }
