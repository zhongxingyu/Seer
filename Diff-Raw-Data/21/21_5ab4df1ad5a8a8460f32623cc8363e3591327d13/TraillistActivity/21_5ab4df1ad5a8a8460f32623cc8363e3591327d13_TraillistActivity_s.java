 package ch.hsr.traildevil.presentation;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.app.SearchManager;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.Toast;
 import ch.hsr.traildevil.R;
 import ch.hsr.traildevil.application.Controller;
 import ch.hsr.traildevil.domain.Trail;
 import ch.hsr.traildevil.util.Constants;
 
 public class TraillistActivity extends ListActivity {
 
 	public static final int DIALOG_PROGRESS_ID = 0;
 	private Controller controller;
 	private ProgressDialog progressDialog;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.tracklist);
 
 		controller = new Controller(getApplicationContext());
 
 		if (controller.isNetworkAvailable()) {
 			controller.startSynchronization(this);
 		} else {
 			displayTrailData();
 			displayToast();
 		}
 	}
 
 	private void displayToast() {
 		Toast infoToast = Toast.makeText(this, getString(R.string.error_internet_unavailable), Toast.LENGTH_LONG);
 		infoToast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
 		infoToast.show();
 	}
 
 	/**
 	 * This method is invoked by the Async Task when the Data synchronization
 	 * has completed.
 	 */
 	public void syncCompleted() {
 		displayTrailData();
 		removeDialog(DIALOG_PROGRESS_ID);
 		progressDialog = null;
 	}
 
 	/**
 	 * This method is invoked by the Async Task when the synchronization is
 	 * canceled by the user.
 	 */
 	public void syncAborted() {
 		displayTrailData();
 		removeDialog(DIALOG_PROGRESS_ID);
 		progressDialog = null;
 	}
 
 	/**
 	 * This method is invoked by the Async Task when a progress update should be
 	 * displayed.
 	 * 
 	 * @param message
 	 *            The message to display
 	 * @param progress
 	 *            The total progress
 	 */
 	public void updateProgressbar(String message, int progress, int max) {
 		if (progressDialog != null) {
 			progressDialog.setIndeterminate(false);
 			progressDialog.setMessage(message);
 			progressDialog.setProgress(progress);
 			progressDialog.setMax(max);
 		}
 	}
 
 	/**
 	 * Load Trail data from db and display
 	 */
 	private void displayTrailData() {
 		List<Trail> trails = controller.getTrails();
 		setListAdapter(new TraillistAdapter(this, R.layout.tracklist_item, trails, controller.getMaxFavorits()));
 	}
 
 	@Override
 	protected void onNewIntent(Intent intent) {
 		setIntent(intent);
 		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
 			getListView().setFilterText(intent.getStringExtra(SearchManager.QUERY));
 		}
 	}
 
 	@Override
 	protected void onListItemClick(ListView listView, View view, int position, long id) {
 		super.onListItemClick(listView, view, position, id);
 		Intent intent = new Intent(this, TrailActivity.class);
		intent.putExtra("trailPosition", position);
 		startActivity(intent);
 	}
 
 	/**
 	 * Show a dialog with a progress bar. The user is informed, that the local
 	 * data is updated.
 	 */
 	@Override
 	protected Dialog onCreateDialog(int id) {
 
 		if (id == DIALOG_PROGRESS_ID) {
 			progressDialog = new ProgressDialog(this);
 			progressDialog.setTitle(getString(R.string.progressbar_title));
 			progressDialog.setMessage(getString(R.string.progressbar_loading));
 			progressDialog.setIndeterminate(true);
 			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			progressDialog.setCancelable(false); // Back Button not supported to cancel dialog
 			progressDialog.setButton(Dialog.BUTTON_NEGATIVE, getString(R.string.button_cancel), new OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					controller.stopSynchronization();
 				}
 			});
 			return progressDialog;
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the latest modified timestamp of all trail. This timestamp could
 	 * be used to check for online updates. Note that this value is read as
 	 * shared property.
 	 * 
 	 * @return The latest timestamp or 0 if no one exists.
 	 */
 	public long getLastModifiedTimestamp() {
 		SharedPreferences preferences = getPreferences(Activity.MODE_PRIVATE);
 		return preferences.getLong(Constants.LAST_MODIFIED_TIMESTAMP_KEY, 0);
 	}
 
 	/**
 	 * Saves the latest modified timestamp of all trails as shared property.
 	 * 
 	 * @param timestamp
 	 *            The latest timestamp to set.
 	 */
 	public void setLastModifiedTimestamp(long timestamp) {
 		SharedPreferences preferences = getPreferences(Activity.MODE_PRIVATE);
 
 		Editor editor = preferences.edit();
 		editor.putLong(Constants.LAST_MODIFIED_TIMESTAMP_KEY, timestamp);
 		editor.commit();
 	}
 }
