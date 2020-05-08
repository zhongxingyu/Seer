 package com.noughmad.plusfive;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.DialogFragment;
 import android.content.ContentUris;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ShareActionProvider;
 import android.widget.Toast;
 
 import org.jsoup.Connection;
 import org.jsoup.Jsoup;
 
 import java.io.IOException;
 
 import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
 
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
 public class StoryListActivity extends RefreshActivity implements
 		StoryListFragment.Callbacks {
 
 	/**
 	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
 	 * device.
 	 */
 	private boolean mRefreshing;
 	private ShareActionProvider mShareProvider;
 
     private static final String TAG = "StoryListActivity";
 
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
 
         if (!getPreferences(Context.MODE_PRIVATE).contains("welcome-login-dialog")) {
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
             builder.setTitle(R.string.app_name);
             builder.setMessage(R.string.welcome_message);
 
             builder.setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     DialogFragment newFragment = new LoginFragment();
                     newFragment.show(getFragmentManager(), "login");
                 }
             });
 
             builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
 
                 }
             });
 
             builder.create().show();
             getPreferences(MODE_PRIVATE).edit().putBoolean("welcome-login-dialog", true).commit();
         }
 		
 		Log.w("BASE_URI", SlashdotProvider.BASE_URI.toString());
 
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
 			
 			cursor.close();
 		} else {
 			// In single-pane mode, simply start the detail activity
 			// for the selected item ID.
 			Intent detailIntent = new Intent(this, StoryDetailActivity.class);
 			detailIntent.putExtra(StoryDetailFragment.ARG_ITEM_ID, id);
 			startActivity(detailIntent);
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
 			
 		case R.id.open_in_browser:
 			Uri storiesUri = Uri.withAppendedPath(SlashdotProvider.BASE_URI, SlashdotProvider.STORIES_TABLE_NAME);
 			StoryDetailFragment detail = (StoryDetailFragment) getFragmentManager().findFragmentById(R.id.story_detail_container);
 			Uri uri = ContentUris.withAppendedId(storiesUri, detail.getStoryId());
 			
 			String[] projection = new String[] {SlashdotProvider.STORY_URL};
 			Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
 			if (cursor.moveToFirst()) {
 				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(cursor.getString(0)));
 				startActivity(i);
 			}
 			return true;
 
         case R.id.login:
             DialogFragment newFragment = new LoginFragment();
             newFragment.show(getFragmentManager(), "login");
             return true;
 
         case R.id.logout:
             logout();
             return true;
 
         default:
             return false;
         }
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		if (mRefreshing) {
 			menu.getItem(0).setActionView(R.layout.actionbar_indeterminate_progress);
 		} else {
 			menu.getItem(0).setActionView(null);
 		}
 
         boolean logged = SlashdotContent.isLoggedIn(this);
 
 
         menu.findItem(R.id.login).setVisible(!logged);
         menu.findItem(R.id.logout).setVisible(logged);
 
 		return true;
 	}
 
 	@Override
 	public void onRefreshStateChanged(boolean refreshing) {
 		if (refreshing != mRefreshing) {
 			mRefreshing = refreshing;
 			invalidateOptionsMenu();
 
             if (refreshing == false) {
                 getAttacher().setRefreshComplete();
             }
 		}
 	}
 
     void logout() {
        Log.d(TAG, "Log out called");
         AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
             protected Void doInBackground(Void... params) {
                 try {
                     Jsoup.connect("http://slashdot.org/my/logout")
                         .cookie("user", getSharedPreferences("cookie", Context.MODE_PRIVATE).getString("user", ""))
                         .method(Connection.Method.GET).execute();
                 } catch (IOException e) {
                    e.printStackTrace();
                 }
 
                 return null;
             }
 
             protected void onPostExecute(Void v) {
                 getSharedPreferences("cookie", Context.MODE_PRIVATE).edit().remove("user").commit();
                 invalidateOptionsMenu();
                 Toast.makeText(StoryListActivity.this, R.string.logout_success, Toast.LENGTH_SHORT).show();
             }
         };

        task.execute();
     }
 
     @Override
     public void onRefreshStarted(View view) {
         Log.i(TAG, "Refresh by pull started");
         StoryListFragment fragment = (StoryListFragment) getFragmentManager().findFragmentById(R.id.story_list);
         fragment.refreshStories();
     }
 }
