 package com.codepath.apps.twitterclient;
 
 import org.json.JSONObject;
 
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.ActionBar.TabListener;
 import android.app.FragmentTransaction;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.view.Menu;
 import android.view.MenuItem;
 
 import com.codepath.apps.twitterclient.fragments.HomeTimelineFragment;
 import com.codepath.apps.twitterclient.fragments.MentionsFragment;
 import com.codepath.apps.twitterclient.models.User;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 public class TimelineActivity extends FragmentActivity implements TabListener {
 	
 	private ActionBar actionBar;
 	private Tab tabHome;
 	User user;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_timeline);
 		setupNavigationTabs();
 		TwitterApp.getRestClient().getMyInfo(new JsonHttpResponseHandler(){
 			@Override
 			public void onSuccess(JSONObject json) {
 				user = User.fromJson(json);
 				getActionBar().setTitle(user.getScreenName());
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.timeline, menu);
 		return true;
 	}
 	
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) { 
         case R.id.tweetParams:
             startActivityForResult(new Intent(this, PostTweetActivity.class), 1);
             break;
         case R.id.userProfile:
         	Intent i = new Intent(this, ProfileActivity.class);
         	i.putExtra("userId", user.getId());
         	startActivity(i);
         	break;
         default:
             break;
         }
         return true;
     }
 	
 	private void setupNavigationTabs(){
 		actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 		actionBar.setDisplayShowTitleEnabled(true);
 		tabHome = actionBar.newTab().setText("Home")
 				.setTag("HomeTimelineFragment")
 				.setIcon(R.drawable.ic_twitter)
 				.setTabListener(this);
 		Tab tabMentions = actionBar.newTab().setText("Mentions")
 				.setTag("MentionsFragment")
 				.setIcon(R.drawable.ic_comments)
 				.setTabListener(this);
 		
 		actionBar.addTab(tabHome);
 		actionBar.addTab(tabMentions);
 		actionBar.selectTab(tabHome);
 		
 	}
 	
	private boolean mReturningWithResult = false;
	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == 1) {
             if (resultCode == RESULT_OK) {
            	mReturningWithResult = true;
             }
         }
     }
	
	@Override
	protected void onPostResume() {
	    super.onPostResume();
	    if (mReturningWithResult) {
	    	FragmentManager manager = getSupportFragmentManager();
    		android.support.v4.app.FragmentTransaction fts = manager.beginTransaction();
        	fts.commit();
        	actionBar.setSelectedNavigationItem(tabHome.getPosition());
	    }
	    mReturningWithResult = false;
	}
 
 	@Override
 	public void onTabReselected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onTabSelected(Tab tab, FragmentTransaction ft) {
 		FragmentManager manager = getSupportFragmentManager();
 		android.support.v4.app.FragmentTransaction fts = manager.beginTransaction();
 		if(tab.getTag().equals("HomeTimelineFragment")){
 			fts.replace(R.id.frame_container, new HomeTimelineFragment());
 		}else{
 			fts.replace(R.id.frame_container, new MentionsFragment());
 		}
 		fts.commit();
 		
 	}
 
 	@Override
 	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
