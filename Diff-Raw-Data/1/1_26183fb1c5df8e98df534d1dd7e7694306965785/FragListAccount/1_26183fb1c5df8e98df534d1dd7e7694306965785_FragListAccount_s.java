 package com.nakedferret.simplepass.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.CursorAdapter;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.view.ActionMode;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AbsListView.MultiChoiceModeListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import com.activeandroid.ActiveAndroid;
 import com.activeandroid.Cache;
 import com.activeandroid.content.ContentProvider;
 import com.activeandroid.query.JoinView;
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.App;
 import com.googlecode.androidannotations.annotations.Background;
 import com.googlecode.androidannotations.annotations.EFragment;
 import com.googlecode.androidannotations.annotations.FragmentArg;
 import com.googlecode.androidannotations.annotations.UiThread;
 import com.nakedferret.simplepass.Account;
 import com.nakedferret.simplepass.Category;
 import com.nakedferret.simplepass.IFragListener;
 import com.nakedferret.simplepass.MultiUriCursorLoader;
 import com.nakedferret.simplepass.R;
 import com.nakedferret.simplepass.SimplePass;
 
 @EFragment
 public class FragListAccount extends ListFragment implements
 		OnItemClickListener, LoaderCallbacks<Cursor> {
 
 	@FragmentArg
 	String vaultUriString;
 
 	@App
 	SimplePass app;
 
 	private static Uri URI;
 	private static String SELECTION;
 	private static JoinView VIEW;
 	// View that represents the join of Account and Category
 
 	private String[] selectionArgs;
 	private Uri vaultUri;
 	private CursorAdapter adapter;
 	private IFragListener mListener;
 
 	public FragListAccount() {
 		// Required empty public constructor
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.frag_list_account, menu);
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		try {
 			mListener = (IFragListener) activity;
 		} catch (ClassCastException e) {
 			throw new ClassCastException(activity.toString()
 					+ " must implement IFragListener");
 		}
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		VIEW = (JoinView) Cache.getView("account_w_cat");
 		URI = VIEW.getUri();
 		SELECTION = VIEW.getColumnName(Account.class, "vault") + " = ?";
 
 		adapter = getAdapter();
 		setListShown(false);
 		setEmptyText(getText(R.string.empty_vault_message));
 		getListView().setOnItemClickListener(this);
 
 		setActionMode();
 	}
 
 	private void setActionMode() {
 		ListView listView = getListView();
 		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
 		listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
 
 			private List<Long> selectedItems = new ArrayList<Long>();
 
 			@Override
 			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 				switch (item.getItemId()) {
 				case R.id.action_delete_account:
 					deleteAccounts(selectedItems, mode);
 				default:
 					return false;
 				}
 
 			}
 
 			@Override
 			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 				mode.getMenuInflater().inflate(R.menu.ac_frag_list_account,
 						menu);
 				return true;
 			}
 
 			@Override
 			public void onDestroyActionMode(ActionMode mode) {
 
 			}
 
 			@Override
 			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
 				return false;
 			}
 
 			@Override
 			public void onItemCheckedStateChanged(ActionMode mode,
 					int position, long id, boolean checked) {
 
 				if (checked)
 					selectedItems.add(id);
 				else
 					selectedItems.remove(id);
 			}
 		});
 	}
 
 	@Background
 	void deleteAccounts(List<Long> selectedItems, ActionMode mode) {
 		ActiveAndroid.beginTransaction();
 		for (Long l : selectedItems) {
 			Account.delete(Account.class, l);
 		}
 		ActiveAndroid.setTransactionSuccessful();
 		ActiveAndroid.endTransaction();
 		exitMode(mode);
 	}
 
 	@UiThread
 	void exitMode(ActionMode mode) {
 		mode.finish();
 	}
 
 	private SimpleCursorAdapter getAdapter() {
 		final int LAYOUT = android.R.layout.simple_list_item_activated_2;
 		final String[] PROJECTION = {
 				VIEW.getColumnName(Account.class, "name"),
 				VIEW.getColumnName(Category.class, "name") };
 		final int[] VIEWS = { android.R.id.text1, android.R.id.text2 };
 
 		return new SimpleCursorAdapter(getActivity(), LAYOUT, null, PROJECTION,
 				VIEWS, 0);
 	}
 
 	@AfterViews
 	void init() {
 		vaultUri = Uri.parse(vaultUriString);
 		selectionArgs = new String[] { vaultUri.getLastPathSegment() };
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		getLoaderManager().initLoader(0, null, this);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		getActivity().setTitle(R.string.frag_list_account_title);
 	}
 
 	@Override
 	public void onDetach() {
 		super.onDetach();
 		mListener = null;
 	}
 
 	public interface OnAccountSelectedListener {
 		public void onAccountSelected(Uri uri);
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
 			long id) {
 
 		Uri accountUri = ContentProvider.createUri(Account.class, id);
 		mListener.onAccountSelected(accountUri);
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
 		MultiUriCursorLoader loader = new MultiUriCursorLoader(getActivity(),
 				URI, null, SELECTION, selectionArgs, null);
 
 		loader.addUri(ContentProvider.createUri(Account.class, null));
 		loader.addUri(ContentProvider.createUri(Category.class, null));
 
 		return loader;
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
 		setListAdapter(adapter);
 		adapter.changeCursor(c);
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> arg0) {
 		adapter.changeCursor(null);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.action_lock_vault:
 			app.lockVault(vaultUri);
 			return true;
 		case R.id.action_add_account:
 			mListener.requestCreateAccount(vaultUri);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 
 	}
 
 }
