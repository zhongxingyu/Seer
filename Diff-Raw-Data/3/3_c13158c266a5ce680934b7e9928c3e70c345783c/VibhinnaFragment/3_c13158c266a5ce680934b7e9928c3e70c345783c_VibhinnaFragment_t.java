 package com.vibhinna.binoy;
 
 import java.io.File;
 
 import android.app.AlertDialog;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.BaseColumns;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 public class VibhinnaFragment extends SherlockListFragment implements
 		LoaderManager.LoaderCallbacks<Cursor> {
 	private static final int VFS_LIST_LOADER = 0x01;
 	protected static final String TAG = "com.vibhinna.binoy.VibhinnaFragment";
 	private VibhinnaAdapter adapter;
 	protected boolean cacheCheckBool = false;
 	protected boolean dataCheckBool = false;
 	protected boolean systemCheckBool = false;
 	int iconid = 1;
 	private ContentResolver resolver;
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		setRetainInstance(true);
 		registerForContextMenu(getListView());
 		if (!Constants.BINARY_FOLDER.exists()
 				|| Constants.BINARY_FOLDER.list().length < 3) {
 			Constants.BINARY_FOLDER.mkdirs();
 			AssetsManager assetsManager = new AssetsManager(getActivity());
 			assetsManager.copyAssets();
 		}
 		resolver = getActivity().getContentResolver();
 		setHasOptionsMenu(true);
 		startLoading();
 	}
 
 	protected void startLoading() {
 		setListShown(false);
 		adapter.notifyDataSetChanged();
 		getListView().invalidateViews();
 		LoaderManager lm = getLoaderManager();
 		lm.initLoader(VFS_LIST_LOADER, null, this);
 	}
 
 	protected void restartLoading() {
 		setListShown(false);
 		getLoaderManager().restartLoader(VFS_LIST_LOADER, null, this);
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 		CursorLoader cursorLoader = new CursorLoader(getActivity(),
 				VibhinnaProvider.LIST_DISPLAY_URI, Constants.allColumns, null,
 				null, DatabaseHelper.VIRTUAL_SYSTEM_COLUMN_NAME);
 		return cursorLoader;
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
 		adapter.swapCursor(cursor);
 		setListShown(true);
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> loader) {
 		adapter.swapCursor(null);
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		showDetailsDialog(id);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setHasOptionsMenu(true);
 
 		String[] from = { "name", "desc", "status", "path", "folder",
 				BaseColumns._ID };
 		int[] to = { R.id.name, R.id.desc, R.id.status, R.id.path };
 
 		adapter = new VibhinnaAdapter(getActivity(), R.layout.main_row, null,
 				from, to, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
 		setListAdapter(adapter);
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenu.ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		AdapterView.AdapterContextMenuInfo info;
 		PropManager propmanager = new PropManager(getActivity()
 				.getApplicationContext());
 		try {
 			// Casts the incoming data object into the type for AdapterView
 			// objects.
 			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
 		} catch (ClassCastException e) {
 			// If the menu object can't be cast, logs an error.
 			Log.w("Exception", "exception in getting menuinfo");
 			return;
 		}
 		Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
 		String s1 = Constants.SD_PATH + propmanager.mbActivePath();
 		String s2 = cursor.getString(7);
 		if (cursor.equals(null) || s1.equals(s2))
 			return;
 		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
 			LayoutInflater headerInflater = (LayoutInflater) getSherlockActivity()
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			ViewGroup header = (ViewGroup) headerInflater.inflate(
 					R.layout.context_menu_header, null);
 			TextView title = (TextView) header
 					.findViewById(R.id.context_menu_title);
 			title.setText(cursor.getString(1));
 			menu.setHeaderView(header);
		} else
			menu.setHeaderTitle(cursor.getString(1));
 		android.view.MenuInflater inflater = getActivity().getMenuInflater();
 		inflater.inflate(R.menu.context_menu, menu);
 	}
 
 	@Override
 	public boolean onContextItemSelected(android.view.MenuItem item) {
 		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 		final Cursor cursor = (Cursor) getListAdapter().getItem(
 				menuInfo.position);
 		if (cursor == null) {
 			// For some reason the requested item isn't available, do nothing
 			return false;
 		}
 		final String vPath = cursor.getString(7);
 		iconid = Integer.parseInt(cursor.getString(4));
 		final int _id;
 		try {
 			_id = Integer.parseInt(cursor.getString(0));
 		} catch (NumberFormatException e1) {
 			e1.printStackTrace();
 			return false;
 		}
 		switch (item.getItemId()) {
 		case R.id.edit:
 			showEditDialog(this, _id);
 			return true;
 		case R.id.delete:
 			AlertDialog.Builder builder;
 			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
 				builder = new AlertDialog.Builder(getActivity());
 			else
 				builder = new HoloAlertDialogBuilder(getActivity());
 			builder.setTitle(
 					getString(R.string.delete_vfs, cursor.getString(1)))
 					.setMessage(getString(R.string.confirm_delete))
 					.setPositiveButton(getString(R.string.okay),
 							new DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(
 										DialogInterface dialogInterface, int i) {
 									try {
 										MiscMethods.removeDirectory(new File(
 												vPath));
 										VibhinnaFragment.this
 												.getActivity()
 												.getContentResolver()
 												.delete(Uri
 														.parse("content://"
 																+ VibhinnaProvider.AUTHORITY
 																+ "/"
 																+ VibhinnaProvider.VFS_BASE_PATH
 																+ "/" + _id),
 														null, null);
 										restartLoading();
 									} catch (Exception e) {
 										e.printStackTrace();
 										return;
 									}
 								}
 							})
 					.setNeutralButton(getString(R.string.cancel),
 							new DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(
 										DialogInterface dialogInterface, int i) {
 								}
 							}).create().show();
 			return true;
 		case R.id.format:
 			showFormatDialog(this, _id);
 			return true;
 		default:
 			return super.onContextItemSelected(item);
 		}
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.main_options_menu, menu);
 		super.onCreateOptionsMenu(menu, inflater);
 		Intent prefsIntent = new Intent(getActivity().getApplicationContext(),
 				Preferences.class);
 
 		MenuItem preferences = menu.findItem(R.id.menu_settings);
 		preferences.setIntent(prefsIntent);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_new:
 			showNewVFSDialog();
 			return true;
 		case R.id.menu_settings:
 			// getActivity().startActivity(item.getIntent());
 			Toast.makeText(getActivity(), getString(R.string.this_is_a_stub),
 					Toast.LENGTH_SHORT).show();
 			return true;
 		case R.id.menu_scan:
 			scanVFS();
 			restartLoading();
 			return true;
 		case R.id.menu_backup:
 			resolver.query(
 					Uri.parse("content://" + VibhinnaProvider.AUTHORITY + "/"
 							+ VibhinnaProvider.VFS_BASE_PATH + "/write_xml"),
 					null, null, null, null);
 			return true;
 		case R.id.menu_restore:
 			resolver.query(
 					Uri.parse("content://" + VibhinnaProvider.AUTHORITY + "/"
 							+ VibhinnaProvider.VFS_BASE_PATH + "/read_xml"),
 					null, null, null, null);
 			restartLoading();
 			return true;
 		case R.id.menu_help:
 			Toast.makeText(getActivity(), getString(R.string.this_is_a_stub),
 					Toast.LENGTH_SHORT).show();
 			return true;
 		case R.id.menu_about:
 			Toast.makeText(getActivity(), getString(R.string.this_is_a_stub),
 					Toast.LENGTH_SHORT).show();
 			return true;
 		case R.id.menu_license:
 			Toast.makeText(getActivity(), getString(R.string.this_is_a_stub),
 					Toast.LENGTH_SHORT).show();
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	private void scanVFS() {
 		class scanTask extends AsyncTask<Void, Void, Void> {
 
 			@Override
 			protected Void doInBackground(Void... params) {
 				resolver.query(
 						Uri.parse("content://" + VibhinnaProvider.AUTHORITY
 								+ "/" + VibhinnaProvider.VFS_BASE_PATH
 								+ "/scan"), null, null, null, null);
 				return null;
 			}
 		}
 		new scanTask().execute();
 
 	}
 
 	/**
 	 * Shows the Details Dialog, which gives all info about a VFS.
 	 * 
 	 * @param id
 	 *            _id of the VFS
 	 */
 	void showDetailsDialog(long id) {
 		DetailsDialogFragment.newInstance(getSherlockActivity(), id).show(
 				getFragmentManager(), "details_dialog");
 	}
 
 	/**
 	 * Shows New VFS dialog according to API, which will create a new VFS
 	 * 
 	 * @param vibhinnaFragment
 	 */
 	private void showNewVFSDialog() {
 		NewDialogFragmentOld.newInstance(getSherlockActivity()).show(
 				getFragmentManager(), "new_dialog");
 	}
 
 	private void showFormatDialog(VibhinnaFragment vibhinnaFragment, long id) {
 		FormatDialogFragment.newInstance(vibhinnaFragment, id).show(
 				getFragmentManager(), "format_dialog");
 	}
 
 	private void showEditDialog(VibhinnaFragment vibhinnaFragment, int id) {
 		EditDialogFragment.newInstance(vibhinnaFragment, id).show(
 				getFragmentManager(), "edit_dialog");
 	}
 }
