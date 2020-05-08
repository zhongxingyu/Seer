 package me.taedium.android.profile;
 
 import me.taedium.android.ApplicationGlobals;
 import me.taedium.android.HeaderActivity;
 import me.taedium.android.R;
 import me.taedium.android.api.Caller;
 import me.taedium.android.domain.FilterItem;
 import me.taedium.android.domain.FilterItemAdapter;
 import me.taedium.android.domain.RankingItem;
 import me.taedium.android.domain.RankingItemAdapter;
 import me.taedium.android.domain.UserStats;
 import me.taedium.android.listener.LoggedInChangedListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewSwitcher;
 
 
 public class ProfileActivity extends HeaderActivity implements LoggedInChangedListener {
 	
 	private static final int LIST_ITEM_ADDED = 0;
 	private static final int LIST_ITEM_LIKED = 1;
 	private static final int LIST_ITEM_DISLIKED = 2;
 	private static final String MODULE = "ProfileActivity";
 	private ViewSwitcher vsMain;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setTitle(R.string.profile_page_title);
 		setContentView(R.layout.profile);
 		addLoggedInListener(this);
 		
 		// Display appropriate view depending on if user is logged in or not
 		vsMain = (ViewSwitcher) findViewById(R.id.vsProfile);
 		if (ApplicationGlobals.getInstance().isLoggedIn(this)) {
 			vsMain.showNext();
 			setupLoggedInView();
 		} else {
 			vsMain.showNext();
 			vsMain.showNext();
 			setupNotLoggedInView();
 		}
 	}
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		removeLoggedInListener(this);
 	}
 	
 	private void setupLoggedInView() {
 		
 		// Set user's name in main text
 		TextView tvWelcome = (TextView) findViewById(R.id.tvLoggedInAs);
 		tvWelcome.setText(String.format(getString(R.string.tvLoggedInAs), ApplicationGlobals.getInstance().getUser(this)));
 		
 		// Set top list view displaying info about activities created, liked, disliked
         ListView lvSummary = (ListView) findViewById(R.id.lvProfileSummary);
         final FilterItem[] summaryItems = getSummaryItems();
         lvSummary.setAdapter(new FilterItemAdapter(this, R.id.list_item_text, summaryItems));
         lvSummary.setTextFilterEnabled(true);
         lvSummary.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             	int stat = Integer.parseInt(summaryItems[(int) id].feedbackLabel);
             	if (stat > 0) {
 	            	switch((int)id) {
 	                    case LIST_ITEM_ADDED:                    	
 		                    	startActivitiesList(RecommendationOverviewListActivity.KEY_USER_ADDED_ACTIVITIES);
 	                        break;
 	                    case LIST_ITEM_LIKED:
 	                    	startActivitiesList(RecommendationOverviewListActivity.KEY_USER_LIKED_ACTIVITIES);
 	                        break;
 	                    case LIST_ITEM_DISLIKED:
 	                    	startActivitiesList(RecommendationOverviewListActivity.KEY_USER_DISLIKED_ACTIVITIES);
 	                        break;
 	                    default:
 	                }
             	}
             }
         });
         
         // Setup scoreboard
         ListView lvRankings= (ListView) findViewById(R.id.lvRankings);
         RankingItem[] rankings = Caller.getInstance(getApplicationContext()).getRankings();
         if (rankings != null && rankings.length >0) {
 	        lvRankings.setAdapter(new RankingItemAdapter(this, R.id.tvListItemUser, rankings));
 	        lvRankings.setTextFilterEnabled(true); 
         } else {
         	lvRankings.setVisibility(View.INVISIBLE);
         }
 		
 	}
 	
 	private void startActivitiesList(int typeKey) {
 		Bundle bundle = new Bundle();
     	bundle.putInt(RecommendationOverviewListActivity.KEY_ACTIVITIES_TYPE, typeKey);
         Intent i = new Intent(ProfileActivity.this, RecommendationOverviewListActivity.class);
         i.putExtras(bundle);
         startActivityForResult(i, LIST_ITEM_ADDED);
 	}
 	
 	// Get list items for the activity summary at the top of the page
     private FilterItem[] getSummaryItems() {
         String [] options = getResources().getStringArray(R.array.lvProfileSummaryArray);
         FilterItem[] filterItems = new FilterItem[options.length];
         UserStats stats = Caller.getInstance(getApplicationContext()).getUserStats();
         
         // Check if stats weren't found
         if (stats == null) {
         	Log.e(MODULE, "Got back null stats object.");
         	stats = new UserStats(0, 0, 0);
         	Toast.makeText(getApplicationContext(), getString(R.string.msgCannotGetStats), Toast.LENGTH_LONG).show();
         }
         
         for (int i = 0; i <options.length; i++) {            
             switch (i) {
             case LIST_ITEM_ADDED:
                 filterItems[i] = new FilterItem(R.drawable.round_plus, options[i], Integer.toString(stats.created));
                 break;
             case LIST_ITEM_LIKED:
                 filterItems[i] = new FilterItem(R.drawable.hand_pro, options[i], Integer.toString(stats.likes));
                 break;
             case LIST_ITEM_DISLIKED:
                 filterItems[i] = new FilterItem(R.drawable.hand_contra, options[i], Integer.toString(stats.dislikes));
                 break;
             }
         }
         return filterItems;
     }
     
 	private void setupNotLoggedInView() {
 		
 		Button bLogin = (Button) findViewById(R.id.bLoginProfile);
 		bLogin.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				login();
 			}
 		});
 		
 		Button bRegister = (Button) findViewById(R.id.bRegisterProfile);
 		bRegister.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				register();
 			}
 		});
 	}
 
 	@Override
 	public void loggedIn() {
 		vsMain.showNext();
 		setupLoggedInView();
 	}
 	
 	public void loggedOut() {}
 	
 
 }
