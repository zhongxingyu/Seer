 package com.nakedferret.simplepass;
 
 import android.app.Activity;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.CursorAdapter;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.EFragment;
 import com.nakedferret.simplepass.PasswordStorageContract.Account;
 
 @EFragment(R.layout.list)
 public class FragListAccounts extends SherlockListFragment implements
 		android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {
 
 	private OnAccountSelectedListener mListener;
 	private CursorAdapter adapter;
 
 	public FragListAccounts() {
 
 	}
 
 	@AfterViews
 	void initInterface() {
 		adapter = new AccountAdaper(getActivity(), null, false);
 		setListAdapter(adapter);
 		Log.d("SimplePass", "Account List Fragment");
 		setEmptyText("Hello from Account List");
 		getLoaderManager().initLoader(0, null, this);
 	}
 
 	void swapCursor(Cursor c) {
 		adapter.swapCursor(c);
 	}
 
 	class AccountAdaper extends CursorAdapter {
 
 		public AccountAdaper(Context context, Cursor c, boolean autoRequery) {
 			super(context, c, autoRequery);
 		}
 
 		@Override
 		public void bindView(View v, Context cxt, Cursor c) {
 			TextView tv = (TextView) v;
 			String text = c.getString(c.getColumnIndex(Account.COL_NAME));
 			tv.setText(text);
 		}
 
 		@Override
 		public View newView(Context cxt, Cursor c, ViewGroup root) {
			c.moveToFirst();
 			String text = c.getString(c.getColumnIndex(Account.COL_NAME));
 			TextView tv = new TextView(cxt);
 			tv.setText(text);
 			return tv;
 		}
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		try {
 			mListener = (OnAccountSelectedListener) activity;
 		} catch (ClassCastException e) {
 			throw new ClassCastException(activity.toString()
 					+ " must implement OnFragmentInteractionListener");
 		}
 	}
 
 	@Override
 	public void onDetach() {
 		super.onDetach();
 		mListener = null;
 	}
 
 	public interface OnAccountSelectedListener {
 		public void onAccountSelected(Cursor c);
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int loader, Bundle args) {
 		Uri.Builder builder = new Uri.Builder();
 		builder.scheme("content");
 		builder.authority(PasswordStorageProvider.authority);
 		builder.appendPath(Account.TABLE_NAME);
 
 		String[] projection = { Account.COL_NAME };
 
 		return new CursorLoader(getActivity(), builder.build(), projection,
 				null, null, null);
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
 		adapter.changeCursor(c);
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> loader) {
 		adapter.changeCursor(null);
 	}
 
 }
