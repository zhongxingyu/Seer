 package com.livejournal.karino2.guitarscorevisualizer;
 
 import android.content.Intent;
 import android.content.Loader;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 
 /**
  * An activity representing a list of Scores. This activity
  * has different presentations for handset and tablet-size devices. On
  * handsets, the activity presents a list of items, which when touched,
  * lead to a {@link ScoreDetailActivity} representing
  * item details. On tablets, the activity presents the list of items and
  * item details side-by-side using two vertical panes.
  * <p/>
  * The activity makes heavy use of fragments. The list of items is a
  * {@link ScoreListFragment} and the item details
  * (if present) is a {@link ScoreDetailFragment}.
  * <p/>
  * This activity also implements the required
  * {@link ScoreListFragment.Callbacks} interface
  * to listen for item selections.
  */
 public class ScoreListActivity extends FragmentActivity
         implements ScoreListFragment.Callbacks {
 
     final int ACTIVITY_ID_EDIT = 1;
 
     /**
      * Whether or not the activity is in two-pane mode, i.e. running on a tablet
      * device.
      */
     private boolean mTwoPane;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_score_list);
 
         if (findViewById(R.id.score_detail_container) != null) {
             // The detail container view will be present only in the
             // large-screen layouts (res/values-large and
             // res/values-sw600dp). If this view is present, then the
             // activity should be in two-pane mode.
             mTwoPane = true;
 
             // In two-pane mode, list items should be given the
             // 'activated' state when touched.
             ((ScoreListFragment) getSupportFragmentManager()
                     .findFragmentById(R.id.score_list))
                     .setActivateOnItemClick(true);
         }
 
         // TODO: If exposing deep links into your app, handle intents here.
     }
 
     @Override
     public void onBackPressed() {
         if(doProcessBack()) {
             return;
         }
         super.onBackPressed();
     }
 
     private boolean doProcessBack() {
         if(mTwoPane) {
             return ((ScoreDetailFragment) getSupportFragmentManager()
                     .findFragmentById(R.id.score_detail_container)).doBackProcess();
         }
         return false;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.score_list, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch(item.getItemId()) {
             case R.id.action_new:
                 startEditActivity();
                 return true;
             case R.id.action_recreate:
                 Database.getInstance(this).recreate();
                 reload();
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     private void reload() {
        ((ScoreListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.score_list)).reloadCursor();
     }
 
     private void startEditActivity() {
         Intent intent = new Intent(this, EditActivity.class);
         startActivityForResult(intent, ACTIVITY_ID_EDIT);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch(requestCode) {
             case ACTIVITY_ID_EDIT:
                 reload();
                 return;
         }
     }
 
     /**
      * Callback method from {@link ScoreListFragment.Callbacks}
      * indicating that the item with the given ID was selected.
      */
     @Override
     public void onItemSelected(long id) {
         if (mTwoPane) {
             // In two-pane mode, show the detail view in this activity by
             // adding or replacing the detail fragment using a
             // fragment transaction.
             Bundle arguments = new Bundle();
             arguments.putLong(ScoreDetailFragment.ARG_ITEM_ID, id);
             ScoreDetailFragment fragment = new ScoreDetailFragment();
             fragment.setArguments(arguments);
             getSupportFragmentManager().beginTransaction()
                     .replace(R.id.score_detail_container, fragment)
                     .commit();
 
         } else {
             // In single-pane mode, simply start the detail activity
             // for the selected item ID.
             Intent detailIntent = new Intent(this, ScoreDetailActivity.class);
             detailIntent.putExtra(ScoreDetailFragment.ARG_ITEM_ID, id);
             startActivity(detailIntent);
         }
     }
 }
