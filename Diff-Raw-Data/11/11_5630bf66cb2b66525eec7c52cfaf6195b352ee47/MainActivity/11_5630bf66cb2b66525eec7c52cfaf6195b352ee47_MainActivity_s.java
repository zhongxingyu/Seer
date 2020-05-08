 package com.mytutor;
 
 
 import com.mytutor.authentication.AuthenticationHelper;
 import com.mytutor.session.ServerSession;
 import com.mytutor.session.Session.SessionStateEnum;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
import android.widget.ImageButton;
 
 public class MainActivity extends Activity {
 	AuthenticationHelper ah_;
 	ServerSession session_;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         ah_ = new AuthenticationHelper(this);
         
         try {
 			session_ = ServerSession.getInstance();
 		} catch (Exception e) {
 			// do nothing
 		}
         
         // If we have an account, try to login
         if(ah_.has_account()){
         	this.try_login();
         }
         
         
         
 //        
 //        // Set up handler for the msg_button on the bottom of the activity
 //        ImageButton msg_button = (ImageButton)findViewById(R.id.msg_button);
 //
 //        msg_button.setOnClickListener(
 //    		new View.OnClickListener()
 //    		{
 //    			public void onClick(View v)
 //    			{
 //    		    	Log.d("MainActivity", "Got message button click event");
 //    				Intent intent = new Intent(v.getContext(), MessageActivity.class);
 //    				v.getContext().startActivity(intent);
 //    			}
 //    		}
 //		);    
 //        
 //        
 //        // Set up handler for the profile button on the bottom of the activity
 //        ImageButton profile_button = (ImageButton)findViewById(R.id.profile_button);
 //
 //        profile_button.setOnClickListener(
 //    		new View.OnClickListener()
 //    		{
 //    			public void onClick(View v)
 //    			{
 //    		    	Log.d("MainActivity", "Got profile button click event");
 //    				Intent intent = new Intent(v.getContext(), ProfileActivity.class);
 //    				v.getContext().startActivity(intent);
 //    			}
 //    		}
 //		); 
 //        
 //        
 //        // Set up handler for the profile button on the bottom of the activity
 //        ImageButton search_button = (ImageButton)findViewById(R.id.search_button);
 //
 //        search_button.setOnClickListener(
 //    		new View.OnClickListener()
 //    		{
 //    			public void onClick(View v)
 //    			{
 //    		    	Log.d("MainActivity", "Got map button click event");
 //    				Intent intent = new Intent(v.getContext(), MapActivity.class);
 //    				v.getContext().startActivity(intent);
 //    			}
 //    		}
 //		);   
     }
 
 
     private void try_login() {
 
     	Log.d("MainMenu", "Trying to login");
     	
 		String username = ah_.get_username();
 		String password = ah_.get_password();
 		
 		Log.d("MainMenu", "Trying username/password: " + username + "/" + password);
 		
 		if(null != session_){
 			SessionStateEnum ret = session_.login(username,  password);
 			Log.d("MainMenu", "session_.login returned: " + ret);
 			
 			// If it didn't turn out ok, just return
 			if(SessionStateEnum.OK != ret){
 				return;
 			}
 			
 			// Alllllright.......  Giggity...  Hide the login/register buttons
 			// and display the profile
 			Button playButton = (Button) findViewById(R.id.main_login_button);
 			playButton.setVisibility(View.INVISIBLE);
 			
			Button registerButton = (Button) findViewById(R.id.main_register_button);
			registerButton.setVisibility(View.INVISIBLE);
 		}
 	}
 
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     public boolean onOptionsItemSelected (MenuItem item){
     	Log.d("MainMenu", "Got a menu click event");
     	
 		switch (item.getItemId()) {
 					
 		   	case R.id.action_remove_all_accts:
 				Log.d("MainActivity", "Got remove all accounts click event");
 				ah_.remove_all_of_our_accounts();
 				return true;
 				
 		   	case R.id.action_edit_profile:
 		    	Log.d("MainActivity", "Got profile button click event");
 				Intent intent = new Intent(this, ProfileActivity.class);
 				this.startActivity(intent);
 					
 		   	default:
 		   		return super.onOptionsItemSelected(item);
 		}
     }
     
     public void onClickLogin(View view) {
     	Log.d("MainActivity", "Got login click event");
 		Intent intent = new Intent(this, LoginActivity.class);
 		startActivity(intent);
     }
     
     public void onClickRegister(View view) {
     	Log.d("MainActivity", "Got register click event");
 		Intent intent = new Intent(this, RegisterActivity.class);
 		startActivity(intent);
     }    
     
     public void onClickSearch(View view) {
     	Log.d("MainActivity", "Got search click event");
 		Intent intent = new Intent(this, com.mytutor.search.SearchParams.class);
 		startActivity(intent);
     }
     
 //    public void onClickProfile(View view) {
 //    	Log.d("MainActivity", "Got profile click event");
 //		Intent intent = new Intent(this, ProfileActivity.class);
 //		startActivity(intent);
 //    }
     
     /**
      * Called when the "Search Nearby" button is clicked
      * @param view
      */
     public void onClickSearchNearby(View view){
     	Log.d("MainActivity::onClickMap", "Got map button click event");
 		Intent intent = new Intent(view.getContext(), MapActivity.class);
 		view.getContext().startActivity(intent);
     }
 }
