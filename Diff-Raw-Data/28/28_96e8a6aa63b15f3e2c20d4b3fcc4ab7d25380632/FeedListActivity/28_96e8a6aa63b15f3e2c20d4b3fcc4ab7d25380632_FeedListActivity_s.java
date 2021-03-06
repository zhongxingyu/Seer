 
 package edu.stanford.mobisocial.dungbeetle.ui;
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.view.MenuItem;
 import android.view.View;
 import edu.stanford.mobisocial.dungbeetle.AboutActivity;
 import edu.stanford.mobisocial.dungbeetle.R;
 import edu.stanford.mobisocial.dungbeetle.SearchActivity;
 import edu.stanford.mobisocial.dungbeetle.model.Feed;
 import edu.stanford.mobisocial.dungbeetle.ui.fragments.FeedListFragment;
 import edu.stanford.mobisocial.dungbeetle.ui.fragments.FeedViewFragment;
 import edu.stanford.mobisocial.dungbeetle.ui.fragments.FeedActionsFragment;
 import edu.stanford.mobisocial.dungbeetle.util.ActivityCallout;
 import edu.stanford.mobisocial.dungbeetle.util.InstrumentedActivity;
 
 /**
  * Displays a list of all user-accessible threads (feeds).
  */
 public class FeedListActivity extends FragmentActivity
         implements FeedListFragment.OnFeedSelectedListener, InstrumentedActivity {
 
    private String FRAGMENT_VIEW_SELECTOR = "viewSelector";
     private boolean mDualPane;
 
     public void goHome(Context context) {
         final Intent intent = new Intent(context, HomeActivity.class);
         intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         context.startActivity(intent);
     }
 
     public void onClickHome(View v) {
         goHome(this);
     }
 
     public void onClickSearch(View v) {
         startActivity(new Intent(getApplicationContext(), SearchActivity.class));
     }
 
     public void onClickAbout(View v) {
         startActivity(new Intent(getApplicationContext(), AboutActivity.class));
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_feed_list);
         DashboardBaseActivity.doTitleBar(this);
         getSupportFragmentManager().beginTransaction()
             .replace(R.id.feed_list, new FeedListFragment()).commit();
         mDualPane = (null != findViewById(R.id.feed_view));
     }
 
     @Override
     public void onFeedSelected(Uri feedUri) {
         if (mDualPane) {
             Bundle args = new Bundle();
             args.putParcelable(FeedViewFragment.ARG_FEED_URI, feedUri);
             Fragment feedView = new FeedViewFragment();
            Fragment feedViewSelector = new FeedActionsFragment();
             feedView.setArguments(args);
            feedViewSelector.setArguments(args);
             Fragment oldSelector =
                    getSupportFragmentManager().findFragmentByTag(FRAGMENT_VIEW_SELECTOR);
             FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
             if (oldSelector != null) {
                 ft.remove(oldSelector);
             }
            ft.add(feedViewSelector, "viewSelector");
             ft.replace(R.id.feed_view, feedView);
             ft.commit();
         } else {
             Intent launch = new Intent(Intent.ACTION_VIEW);
             launch.setDataAndType(feedUri, Feed.MIME_TYPE);
             startActivity(launch);
         }
     }
 
     private static int ACTIVITY_CALLOUT = 39;
     private static ActivityCallout mCurrentCallout;
     public void doActivityForResult(ActivityCallout callout) {
         mCurrentCallout = callout;
         Intent launch = callout.getStartIntent();
         startActivityForResult(launch, ACTIVITY_CALLOUT);
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == ACTIVITY_CALLOUT) {
             mCurrentCallout.handleResult(resultCode, data);
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 // app icon in Action Bar clicked; go home
                 Intent intent = new Intent(this, HomeActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 }
