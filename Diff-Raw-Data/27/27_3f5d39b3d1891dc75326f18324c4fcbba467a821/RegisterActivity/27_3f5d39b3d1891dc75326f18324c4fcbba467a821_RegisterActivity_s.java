 package edu.gatech.oad.fullhouse.findmystuff.view;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 import edu.gatech.oad.fullhouse.findmystuff.R;
 import edu.gatech.oad.fullhouse.findmystuff.pres.LoginPresenter;
 import edu.gatech.oad.fullhouse.findmystuff.pres.RegisterPresenter;
 
 public class RegisterActivity extends Activity {
 	
 	private RegisterPresenter presenter;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_resgister);
 		presenter = new RegisterPresenter(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_resgister, menu);
 		return true;
 	}
 
 	public void registerAttempt(View v) {
 		String username = ((TextView)findViewById(R.id.editTextUsername)).getText().toString();
 		String email = ((TextView)findViewById(R.id.editTextEmail)).getText().toString();
         String password = ((TextView)findViewById(R.id.editTextPassword)).getText().toString();
         String repassword = ((TextView)findViewById(R.id.editTextRePassword)).getText().toString();
         if(password.equals(repassword)){
         	presenter.checkRegInfo(username, password, "Name", "Location", email, "Phone");
         } else {
         	displayPasswordMismatchError();
         }
     }
 	
 	public void displayUsernameTakenError() {
 		
 	}
 	
 	public void displayEmailTakenError() {
 		
 	}
 	
 	public void displayPasswordMismatchError() {
 		
 	}
 }
