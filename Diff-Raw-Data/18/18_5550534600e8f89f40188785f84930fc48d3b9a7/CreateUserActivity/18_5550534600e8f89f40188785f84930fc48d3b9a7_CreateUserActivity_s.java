 package dk.noitso.vaerloesefh;
 
 import noitso.chrono.stopwatch.R;
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
import android.widget.Toast;
 import dk.noitso.vaerloesefh.data.SqliteHandler;
 
 public class CreateUserActivity extends Activity implements OnClickListener {
 	private Button saveUserButton, cancelButton;
 	private EditText usernameEditText;
 	private TextView errorTextView;
 	private SqliteHandler dbHandler;
 	private String oldUsername = "";
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    
 	    oldUsername = getIntent().getStringExtra("username"); 
 	    
 	    setContentView(R.layout.create_user_layout);
 	    
 	    saveUserButton = (Button) findViewById(R.id.saveUserButton);
 	    saveUserButton.setOnClickListener(this);
 	    
 	    cancelButton = (Button) findViewById(R.id.cancelButton);
 	    cancelButton.setOnClickListener(this);
 	    
 	    usernameEditText = (EditText) findViewById(R.id.usernameEditText);
 	    usernameEditText.setText(oldUsername);
 	    errorTextView = (TextView) findViewById(R.id.errorTextView);
 	    errorTextView.setTextColor(Color.RED);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		dbHandler = new SqliteHandler(this);
 	}
 
 	
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.saveUserButton:
 			errorTextView.setVisibility(View.GONE);
 			Log.d(CreateUserActivity.class.getSimpleName(), "Save button was clicked...");
 			if(usernameEditText.getText().toString().isEmpty()) {
 				errorTextView.setText("Username can't be empty");
 				errorTextView.setVisibility(View.VISIBLE);
 				break;
 			}
 			if(oldUsername == null) {
 				if(dbHandler.insertUser(usernameEditText.getText().toString())) {
 					Log.d(CreateUserActivity.class.getSimpleName(), "Adding new user...");
 					finish();
 				} else {
 					errorTextView.setText("Username already taken.");
 					errorTextView.setVisibility(View.VISIBLE);
 				}
 			} else {
 				if(dbHandler.editUser(usernameEditText.getText().toString(), oldUsername)) {
 					Log.d(CreateUserActivity.class.getSimpleName(), "User was updated...");
 					finish();
 				} else {
 					errorTextView.setText("Username already taken.");
 					errorTextView.setVisibility(View.VISIBLE);
 				}
 			}
 			break;
 		case R.id.cancelButton:
			Toast.makeText(this, "Cancel was clicked...", Toast.LENGTH_SHORT).show();
 			finish();
 			break;
 		default:
 			break;
 		}
 	}
 }
