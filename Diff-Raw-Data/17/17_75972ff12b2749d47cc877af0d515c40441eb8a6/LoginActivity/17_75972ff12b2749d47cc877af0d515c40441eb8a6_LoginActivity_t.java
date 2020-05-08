 package foo.filmgur;
 
import com.actionbarsherlock.app.SherlockActivity;
 import android.os.Bundle;
 

public class LoginActivity extends SherlockActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
		getSupportActionBar().show();
 	}
 
 }
