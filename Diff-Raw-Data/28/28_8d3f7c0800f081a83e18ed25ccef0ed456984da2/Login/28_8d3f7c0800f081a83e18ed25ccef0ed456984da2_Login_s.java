 package uk.co.ipodling.newsblurb;
 
 import java.io.IOException;
 
 import org.json.JSONObject;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.Toast;
 // must authenticate before you can make calls to endpoints
 public class Login extends Activity {
     CheckBox checkBox;
     EditText userText;
     EditText passText;
     private NewsblurbPreferences newsblurbPreferences;
    Networking networking;
     ParseTheJSON parser;
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 		checkBox = (CheckBox) findViewById(R.id.checkBox1);
 		userText = (EditText)findViewById(R.id.editText1);
 		passText = (EditText)findViewById(R.id.editText2);
         newsblurbPreferences = new NewsblurbPreferences(getApplicationContext());
        networking = new Networking();
         parser = new ParseTheJSON();
         Button button = (Button) findViewById(R.id.button1);
         button.setOnClickListener(new View.OnClickListener() {
             @Override
 			public void onClick(View v) {
             	final String user = userText.getText().toString();
         		final String pass = passText.getText().toString();
         		if(user.matches("")){
     				Toast.makeText(Login.this, "Please enter username and password!", Toast.LENGTH_LONG).show();
         		} else {
         			if (checkBox.isChecked()){
         				newsblurbPreferences.setUser(userText.getText().toString());
         				newsblurbPreferences.setPass(passText.getText().toString());
         				new Thread(new Runnable(){     //for message sending
                 			@Override
                 			public void run(){
                 				JSONObject response = parser.gettheJSONLogin(user,  pass);
                	        		networking.login(userText.getText().toString(), passText.getText().toString());
                 				}
                 			}).start(); //
         				// log into newsblur
         				// if successful intent to move to main activity
         				// if not toast error
         				finish(); //herp derp, finish not intent because i don't want a new instance *headdesk*
             			} else {
         				// log in without saving passing
         				// if successful intent to move to main activity
         				// if not toast error
             				new Thread(new Runnable(){     //for message sending
                     			@Override
                     			public void run(){
                     				JSONObject response = parser.gettheJSONLogin(user,  pass);
                    	        		networking.login(userText.getText().toString(), passText.getText().toString());
                     				}
                     			}).start();
         				finish();
         			}
         		}
             }
         });
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_login, menu);
 		return true;
 	}
 
 
 }
