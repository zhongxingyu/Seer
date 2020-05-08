 package com.noughmad.slashdotcomments;
 
 import android.app.Activity;
 import android.content.ContentUris;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ShareActionProvider;
 
 import com.tapfortap.Interstitial;
 import com.tapfortap.TapForTap;
 
 /**
  * An activity representing a list of Stories. This activity has different
  * presentations for handset and tablet-size devices. On handsets, the activity
  * presents a list of items, which when touched, lead to a
  * {@link StoryDetailActivity} representing item details. On tablets, the
  * activity presents the list of items and item details side-by-side using two
  * vertical panes.
  * <p>
  * The activity makes heavy use of fragments. The list of items is a
  * {@link StoryListFragment} and the item details (if present) is a
  * {@link StoryDetailFragment}.
  * <p>
  * This activity also implements the required
  * {@link StoryListFragment.Callbacks} interface to listen for item selections.
  */
 public class StoryListActivity extends Activity implements
 		StoryListFragment.Callbacks {
 
 	/**
 	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
 	 * device.
 	 */
 	private boolean mRefreshing;
 	private ShareActionProvider mShareProvider;
 	
 	private static double INTERSTITIAL_PROBABILITY = 1.0 / 30.0;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_story_list);
 
 		if (isTwoPane()) {
 			// In two-pane mode, list items should be given the
 			// 'activated' state when touched.
 			((StoryListFragment) getFragmentManager().findFragmentById(
 					R.id.story_list)).setActivateOnItemClick(true);
 		}
 		
 		TapForTap.initialize(this, "664a57b6f74bac48b3700d7cd1310139");
 		Interstitial.prepare(this);
 
 		// TODO: If exposing deep links into your app, handle intents here.
 	}
 
 	/**
 	 * Callback method from {@link StoryListFragment.Callbacks} indicating that
 	 * the item with the given ID was selected.
 	 */
 	@Override
 	public void onItemSelected(long id) {
 		Log.w("StoryListActivity", "Item selected: " + id);
 		if (isTwoPane()) {
 			// In two-pane mode, show the detail view in this activity by
 			// adding or replacing the detail fragment using a
 			// fragment transaction.
 			Bundle arguments = new Bundle();
 			arguments.putLong(StoryDetailFragment.ARG_ITEM_ID, id);
 			StoryDetailFragment fragment = new StoryDetailFragment();
 			fragment.setArguments(arguments);
 			getFragmentManager().beginTransaction()
 					.replace(R.id.story_detail_container, fragment).commit();
 			
 			Uri uri = ContentUris.withAppendedId(Uri.withAppendedPath(SlashdotProvider.BASE_URI, SlashdotProvider.STORIES_TABLE_NAME), id);
 			Cursor cursor = getContentResolver().query(uri, new String[] {SlashdotProvider.STORY_TITLE, SlashdotProvider.STORY_URL}, null, null, null);
 			
 			if (cursor.moveToFirst()) {
 				Intent i = new Intent(Intent.ACTION_SEND);
 				i.setType("text/plain");
 				i.putExtra(Intent.EXTRA_SUBJECT, cursor.getString(0));
 				i.putExtra(Intent.EXTRA_TEXT, cursor.getString(1));
 				mShareProvider.setShareIntent(i);
 			} else {
 				Log.wtf("StoryListActivity", "Item selected with no story");
 			}
 		} else {
 			// In single-pane mode, simply start the detail activity
 			// for the selected item ID.
 			Intent detailIntent = new Intent(this, StoryDetailActivity.class);
 			detailIntent.putExtra(StoryDetailFragment.ARG_ITEM_ID, id);
 			startActivity(detailIntent);
 		}
 		
 		if (Math.random() < INTERSTITIAL_PROBABILITY) {
 			Interstitial.show(this);
 		}
 	}
 
 	@Override
 	public boolean isTwoPane() {
 		return this.getResources().getBoolean(R.bool.is_two_pane);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.story_list_menu, menu);
 		
 		if (isTwoPane()) {
 			getMenuInflater().inflate(R.menu.story_detail_menu, menu);
 			mShareProvider = (ShareActionProvider) menu.findItem(R.id.share).getActionProvider();
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.refresh_stories:
 			StoryListFragment fragment = (StoryListFragment) getFragmentManager().findFragmentById(R.id.story_list);
 			fragment.refreshStories();
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		Log.i("StoryListActivity", "Updating menu: " + mRefreshing);
 		if (mRefreshing) {
 			menu.getItem(0).setActionView(R.layout.actionbar_indeterminate_progress);
 		} else {
 			menu.getItem(0).setActionView(null);
 		}
 		return true;
 	}
 
 	@Override
 	public void onRefreshStateChanged(boolean refreshing) {
 		Log.i("StoryListActivity", "Refreshing changed: " + refreshing);
 		mRefreshing = refreshing;
 		invalidateOptionsMenu();
 	}
 	
 	
 }
