 package com.example.friendzyapp;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 
 /**
  * An activity representing a list of Subscriptions. This activity has different
  * presentations for handset and tablet-size devices. On handsets, the activity
  * presents a list of items, which when touched, lead to a
  * {@link SubscriptionDetailActivity} representing item details. On tablets, the
  * activity presents the list of items and item details side-by-side using two
  * vertical panes.
  * <p>
  * The activity makes heavy use of fragments. The list of items is a
  * {@link SubscriptionListFragment} and the item details (if present) is a
  * {@link SubscriptionDetailFragment}.
  * <p>
  * This activity also implements the required
  * {@link SubscriptionListFragment.Callbacks} interface to listen for item
  * selections.
  */
 public class SubscriptionListActivity extends SherlockFragmentActivity implements SubscriptionListFragment.Callbacks {
 
     private static final String TAG = "SubscriptionListActivity";
     /**
      * Whether or not the activity is in two-pane mode, i.e. running on a tablet
      * device.
      */
     private boolean mTwoPane;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_subscription_list);
         // Show the Up button in the action bar.
         getActionBar().setDisplayHomeAsUpEnabled(true);
 
         if (findViewById(R.id.subscription_detail_container) != null) {
             // The detail container view will be present only in the
             // large-screen layouts (res/values-large and
             // res/values-sw600dp). If this view is present, then the
             // activity should be in two-pane mode.
             mTwoPane = true;
 
             // In two-pane mode, list items should be given the
             // 'activated' state when touched.
             ((SubscriptionListFragment) getSupportFragmentManager().findFragmentById(R.id.subscription_list))
                     .setActivateOnItemClick(true);
         }
 
         // TODO: If exposing deep links into your app, handle intents here.
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case android.R.id.home:
             // This ID represents the Home or Up button. In the case of this
             // activity, the Up button is shown. Use NavUtils to allow users
             // to navigate up one level in the application structure. For
             // more details, see the Navigation pattern on Android Design:
             //
             // http://developer.android.com/design/patterns/navigation.html#up-vs-back
             //
             // NavUtils.navigateUpFromSameTask(this);
             
             // Don't do this; we need to pass back all the extras we got.
             startActivity((Intent) getIntent().getParcelableExtra("STATUS_ORIGINAL_INTENT"));
             finish();
            return true;
         case R.id.action_new_subscription:
             Intent intent = new Intent(this, SubscriptionAddActivity.class);
             intent.putExtras(getIntent());
             startActivity(intent);
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getSupportMenuInflater().inflate(R.menu.subscription_list, menu);
         return true;
     }
 
     /**
      * Callback method from {@link SubscriptionListFragment.Callbacks}
      * indicating that the item with the given ID was selected.
      */
     @Override
     public void onItemSelected(int position) {
         // In single-pane mode, simply start the detail activity
         // for the selected item ID.
         // We're always in single pane mode. gface
         
         Log.d(TAG, "item selected at " + Integer.toString(position));
         
         Intent detailIntent;
         // Maybe we're supposed to add a new subscription?
         detailIntent = new Intent(this, SubscriptionDetailActivity.class);
         
         // Pass it along.
         detailIntent.putExtras(getIntent());
         detailIntent.putExtra(SubscriptionDetailFragment.ARG_ITEM_ID, position);
         startActivity(detailIntent);
     }
 }
