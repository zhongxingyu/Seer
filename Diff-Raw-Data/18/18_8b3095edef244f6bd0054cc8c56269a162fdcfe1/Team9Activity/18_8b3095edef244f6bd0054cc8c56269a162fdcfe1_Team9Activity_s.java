 package team.nine;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
<<<<<<< HEAD
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class Team9Activity extends Activity implements OnClickListener{
     EditText et1, et2;
     TextView tv;
 	
 	/** Called when the activity is first created. */
=======
import android.widget.TextView;

public class Team9Activity extends Activity implements OnClickListener{
    /** Called when the activity is first created. */
>>>>>>> 24385cba42bd5af775c5c0b9949732f05a246597
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         boolean notLoggedIn = false;
         if(notLoggedIn){
         	setContentView(R.layout.login);
         }
         setContentView(R.layout.home);
         tv = (TextView) findViewById(R.login.title);
         et1 = (EditText) findViewById(R.login.username);
         et1.setOnClickListener(this);
         et2 = (EditText) findViewById(R.login.password);
         et2.setOnClickListener(this);
     }
 
	@Override
<<<<<<< HEAD
 	public void onClick(View view) {
 		switch(view.getId()){
 		case R.login.username:
 			et1.getText().toString();
 			break;
 		case R.login.password:
 			et2.getText().toString();
 			break;
 		}
=======
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
>>>>>>> 24385cba42bd5af775c5c0b9949732f05a246597
 	}
 }
