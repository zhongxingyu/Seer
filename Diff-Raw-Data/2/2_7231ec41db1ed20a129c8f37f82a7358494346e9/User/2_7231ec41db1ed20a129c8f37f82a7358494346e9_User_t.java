 package com.tuit.ar.activities.timeline;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import com.tuit.ar.R;
 import com.tuit.ar.activities.NewDirectMessage;
 import com.tuit.ar.api.Twitter;
 
 public class User extends Status {
 	static private com.tuit.ar.models.User user;
 
 	static public void setUser(com.tuit.ar.models.User _user) {
 		user = _user;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setTitle(user.getScreenName());
 	}
 
 	@Override
 	protected com.tuit.ar.models.timeline.Status getTimeline() {
 		return new com.tuit.ar.models.timeline.User(Twitter.getInstance().getDefaultAccount(), user);
 	}
 
 	@Override  
 	public boolean onCreateOptionsMenu(Menu menu) {  
 		menu.add(0, MENU_REFRESH, 0, R.string.refresh);  
 		menu.add(0, MENU_NEW_DIRECT_MESSAGE, 0, R.string.newDirectMessage);
 		return true;  
 	}  
 
 	public boolean onOptionsItemSelected(MenuItem item) {  
 	    if (item.getItemId() == MENU_NEW_DIRECT_MESSAGE) {
 			Intent intent = new Intent(this.getApplicationContext(), NewDirectMessage.class);
 			intent.putExtra("to_user", user.getScreenName());
 			this.startActivity(intent);		
 	        return true;
 	    }
 	    return super.onOptionsItemSelected(item);
 	}
 }
