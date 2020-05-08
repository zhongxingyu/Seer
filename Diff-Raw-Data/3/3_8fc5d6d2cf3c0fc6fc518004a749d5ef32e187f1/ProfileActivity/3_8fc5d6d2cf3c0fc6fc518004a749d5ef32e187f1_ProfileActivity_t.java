 package com.codepath.apps.restclienttemplate;
 
 import org.json.JSONObject;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.view.Menu;
 import android.widget.TextView;
 
 import com.codepath.apps.restclienttemplate.fragements.UserTimelineFragment;
 import com.codepath.apps.restclienttemplate.models.User;
 import com.loopj.android.http.JsonHttpResponseHandler;
 import com.loopj.android.image.SmartImageView;
 
 public class ProfileActivity extends FragmentActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_profile);
 		
 		FragmentManager manager = getSupportFragmentManager();
         UserTimelineFragment fragment = (UserTimelineFragment) manager.findFragmentById(R.id.fragment1);
 
		User user = (User) getIntent().getSerializableExtra("user");
 
 		if(user != null) {
 	        fragment.user = user;
 	        populateWithUser(user);
 		} else {
 			loadUserInfo();
 		}
 		fragment.displayTweets();
 	}
 
 	private void loadUserInfo() {
 		RestClientApp.getRestClient().getAccountCredentials(new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(JSONObject arg0) {
 				User currentUser = User.fromJson(arg0);
 				populateWithUser(currentUser);
 			}
 		});
 	}
 
 	public void populateWithUser(User currentUser) {
 		getActionBar().setTitle("@" + currentUser.getSreenName());
 		SmartImageView profileImage = (SmartImageView) findViewById(R.id.profile_image);
 		TextView titleView = (TextView) findViewById(R.id.titleName);
 		TextView descriptionView = (TextView) findViewById(R.id.description);
 		TextView followersTextView = (TextView) findViewById(R.id.followers);
 		TextView followingTextView = (TextView) findViewById(R.id.following);
 		
 		//Populate the view with the info in tweet
 		profileImage.setImageUrl(currentUser.getProfileImageURL());
 		titleView.setText(currentUser.getName());
 		descriptionView.setText(currentUser.getDescription());
 		followersTextView.setText(currentUser.getFollowersCount() + "Followers");
 		followingTextView.setText(currentUser.getFriendsCount() + "Following");
 	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.profile, menu);
 		return true;
 	}
 
 }
