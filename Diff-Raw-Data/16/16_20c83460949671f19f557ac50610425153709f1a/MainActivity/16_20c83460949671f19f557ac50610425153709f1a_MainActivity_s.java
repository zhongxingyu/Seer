 package com.vanderbilt.people.finder;
 
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.app.LoaderManager.LoaderManager;
 import android.support.v4.app.widget.SimpleCursorAdapter;
 import android.os.Bundle;
 
 public class MainActivity extends FragmentActivity
 	implements LoaderManager.LoaderCallbacks<Cursor>
 {
 	private SimpleCursorAdapter mAdapter;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
 		// Set up Adapter
 		mAdapter = new SimpleCursorAdapter(getActivity(), 
 			android.R.layout.simple_list_item_2, null, 
 			new String[] {Contract.USERNAME, Contract.IP_ADDRESS}, // replace these
 			new int[] {android.R.id.text1, android.R.id.text2}, 0);
		setListAdapter(mAdapter);
 
 		getSupportFragmentManager().initLoader(0, null, this);	
     }
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle args){
 		/* Fill in these parameters */
 		return new CursorLoader(this, 
 			Contract.URI,// URI
 			projection,// needed fields: _id, username, and IP
 			null, // Selection : null defaults to all entries
 			null, // SelectionArgs
 			sortOrder); // ORDER BY 
 			
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor data){
 		mAdapter.swapCursor(data);
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> loader){
 		mAdapter.swapCursor(null);
 	}
 }
