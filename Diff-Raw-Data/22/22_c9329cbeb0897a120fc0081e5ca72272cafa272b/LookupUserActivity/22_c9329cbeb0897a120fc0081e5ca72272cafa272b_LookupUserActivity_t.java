 package edu.gatech.oad.fullhouse.findmystuff.view;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
import android.view.Window;
 import android.widget.TextView;
 import edu.gatech.oad.fullhouse.findmystuff.R;
 import edu.gatech.oad.fullhouse.findmystuff.pres.LookupUserPresenter;
 
 public class LookupUserActivity extends Activity {
 	
 	private LookupUserPresenter presenter;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
        //must be before adding contents
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(false);        

 		setContentView(R.layout.activity_lookup_user);
         presenter = new LookupUserPresenter(this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_lookup_user, menu);
 		return true;
 	}
 	
 	public void userNotFoundError() {
 		findViewById(R.id.notFoundError).setVisibility(View.VISIBLE);
 	}
 	
 	public void setFields(String username, String name, String email, boolean locked, boolean admin) {
 		((TextView)findViewById(R.id.usernameDisplay)).setText(username);
 		((TextView)findViewById(R.id.usernameDisplay)).setVisibility(View.VISIBLE);
 		((TextView)findViewById(R.id.nameDisplay)).setText(name);
 		((TextView)findViewById(R.id.nameDisplay)).setVisibility(View.VISIBLE);
 		((TextView)findViewById(R.id.emailDisplay)).setText(email);
 		((TextView)findViewById(R.id.emailDisplay)).setVisibility(View.VISIBLE);
 		changeLockedField(locked);
 		changeAdminField(admin);
 	}
 	
 	public void removeFields() {
 		((TextView)findViewById(R.id.usernameDisplay)).setText("Username");
 		((TextView)findViewById(R.id.usernameDisplay)).setVisibility(View.INVISIBLE);
 		((TextView)findViewById(R.id.nameDisplay)).setText("Name");
 		((TextView)findViewById(R.id.nameDisplay)).setVisibility(View.INVISIBLE);
 		((TextView)findViewById(R.id.emailDisplay)).setText("Email");
 		((TextView)findViewById(R.id.emailDisplay)).setVisibility(View.INVISIBLE);
 		((TextView)findViewById(R.id.lockedDisplay)).setText("Locked");
 		((TextView)findViewById(R.id.lockedDisplay)).setVisibility(View.INVISIBLE);
 		((TextView)findViewById(R.id.adminDisplay)).setText("Admin");
 		((TextView)findViewById(R.id.adminDisplay)).setVisibility(View.INVISIBLE);		
 	}
 	
 	public void changeLockedField(boolean locked) {
 		((TextView)findViewById(R.id.lockedDisplay)).setText(locked ? "Yes" : "No");
 		((TextView)findViewById(R.id.lockedDisplay)).setVisibility(View.VISIBLE);
 	}
 	
 	public void changeAdminField(boolean admin) {
 		((TextView)findViewById(R.id.adminDisplay)).setText(admin ? "Yes" : "No");
 		((TextView)findViewById(R.id.adminDisplay)).setVisibility(View.VISIBLE);
 	}
 	
 	public void lookup(View v) {
 		findViewById(R.id.notFoundError).setVisibility(View.GONE);
 		String username = ((TextView)findViewById(R.id.userameEntry)).getText().toString();
 		presenter.lookupUser(username);
 	}
 	
 	public void delete(View v) {
 		presenter.deleteUser();
 	}
 	
 	public void makeAdmin(View v) {
 		presenter.makeAdmin();
 	}
 	
 	public void unlock(View v) {
 		presenter.unlockUser();
 	}
 }
