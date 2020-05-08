 package net.rdrei.android.wakimail.ui;
 
 import net.rdrei.android.wakimail.R;
 import net.rdrei.android.wakimail.data.MailTable;
 import net.rdrei.android.wakimail.robo.RoboSherlockListFragment;
 import net.rdrei.android.wakimail.task.MailSyncTask;
 
 import org.acra.ErrorReporter;
 
import roboguice.fragment.RoboListFragment;
 import roboguice.util.Ln;
 import roboguice.util.RoboAsyncTask;
 import android.app.Activity;
 import android.content.ContentUris;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 public class MailListFragment extends RoboSherlockListFragment implements
 		LoaderCallbacks<Cursor> {
 
 	private static final String[] PROJECTION = { MailTable.Columns._ID,
 			MailTable.Columns.TITLE, MailTable.Columns.SENDER, };
 
 	public static final String USER_EXTRA = "user";
 
 	private SimpleCursorAdapter adapter;
 
 	private RoboAsyncTask<?> mSyncTask;
 
 	public interface OnLogoutRequestedListener {
 		void onLogoutRequested();
 	}
 
 	private OnLogoutRequestedListener mLogoutListener;
 
 	/**
 	 * Requests the logout listener from the activity we're attached to.
 	 * 
 	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
 	 */
 	@Override
 	public void onAttach(Activity activity) {
 		try {
 			mLogoutListener = (OnLogoutRequestedListener) activity;
 		} catch (ClassCastException e) {
 			Ln.e(e);
 			throw new ClassCastException(activity.toString()
 					+ " must implement OnLogoutRequestedListener "
 					+ "to use MailListFragment.");
 		}
 
 		super.onAttach(activity);
 	}
 
 	/**
 	 * Inflates the layout used for this fragment from XML.
 	 * 
 	 * @see android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater,
 	 *      android.view.ViewGroup, android.os.Bundle)
 	 */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.fragment_mail_list, container, false);
 	}
 
 	/**
 	 * Bind views from the fragment.
 	 * 
 	 * @see roboguice.fragment.RoboListFragment#onViewCreated(android.view.View,
 	 *      android.os.Bundle)
 	 */
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		super.onViewCreated(view, savedInstanceState);
 
 		bindListViewOnClick();
 
 		// Trigger initial loading.
 		getLoaderManager().initLoader(0, null, this);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Set up the mail display using a SimpleCursorAdapter.
 		adapter = new SimpleCursorAdapter(this.getActivity(),
 				android.R.layout.two_line_list_item, null, new String[] {
 						MailTable.Columns.TITLE, MailTable.Columns.SENDER },
 				new int[] { android.R.id.text1, android.R.id.text2 }, 0);
 		setListAdapter(adapter);
 		setHasOptionsMenu(true);
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.mail_list_menu, menu);
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 
 	private void bindListViewOnClick() {
 		final ListView listView = getListView();
 		listView.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 
 				// When clicked, open the detail view.
 				final Uri uri = ContentUris.withAppendedId(
 						MailTable.ALL_MAILS_URI, id);
 				final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
 				MailListFragment.this.startActivity(intent);
 			}
 		});
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 
 		if (mSyncTask != null) {
 			Ln.d("Canceling old sync task.");
 			mSyncTask.cancel(true);
 		}
 	}
 
 	/**
 	 * Implements the action handlers for when an item in the options menu is
 	 * selected.
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_about:
 			final AboutDialogFragment dialog = AboutDialogFragment
 					.newInstance();
 			dialog.show(getFragmentManager(), "dialog");
 			return true;
 		case R.id.menu_logout:
 			mLogoutListener.onLogoutRequested();
 			return true;
 		case R.id.menu_refresh:
 			this.refresh();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	private void refresh() {
 		final MailSyncTask task = new MailSyncTask(this.getActivity()) {
 			@Override
 			protected void onException(Exception err) {
 				super.onException(err);
 
 				ErrorReporter.getInstance().handleException(err);
 			}
 
 			@Override
 			protected void onFinally() throws RuntimeException {
 				hideLoadingSpinner();
 				MailListFragment.this.mSyncTask = null;
 				super.onFinally();
 			}
 		};
 
 		Ln.d("Starting mail sync task.");
 		synchronized (this) {
 			if (mSyncTask == null) {
 				mSyncTask = task;
 				task.execute();
 				showLoadingSpinner();
 			} else {
 				Ln.w("Refusing to start new sync task while old is still "
 						+ "running!");
 			}
 		}
 	}
 
 	private void showLoadingSpinner() {
 		((SherlockFragmentActivity) this.getActivity())
 				.setSupportProgressBarIndeterminateVisibility(true);
 	}
 
 	private void hideLoadingSpinner() {
 		((SherlockFragmentActivity) this.getActivity())
 				.setSupportProgressBarIndeterminateVisibility(false);
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 		showLoadingSpinner();
 		return new CursorLoader(this.getActivity(), MailTable.ALL_MAILS_URI,
 				PROJECTION, null, null, null);
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
 		adapter.swapCursor(cursor);
 		hideLoadingSpinner();
 
 		if (adapter.isEmpty()) {
 			Ln.d("List is empty. Triggering initial loading.");
 			refresh();
 		}
 
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> arg0) {
 		adapter.swapCursor(null);
 	}
 }
