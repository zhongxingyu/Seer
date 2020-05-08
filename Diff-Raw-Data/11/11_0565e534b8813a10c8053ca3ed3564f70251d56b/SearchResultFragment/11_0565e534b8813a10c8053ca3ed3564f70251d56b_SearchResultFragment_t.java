 package com.parworks.mars.view.search;
 
 import java.util.List;
 
 import android.annotation.SuppressLint;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import com.parworks.mars.R;
 import com.parworks.mars.model.db.SiteInfoTable;
 import com.parworks.mars.model.provider.MarsContentProvider;
 import com.parworks.mars.view.search.SearchResultAdapter.ViewHolder;
 import com.parworks.mars.view.siteexplorer.ExploreActivity;
 
 @SuppressLint("ValidFragment")
 public class SearchResultFragment extends Fragment implements LoaderCallbacks<Cursor> {
 
 	private static final String TAG = "SearchResultFragment";
 	private static final int SEARCH_SITE_INFO_LOADER_ID = 33;
 	
 	private List<String> siteIds;
 	private SearchResultAdapter newAdapter;	
 
 	public SearchResultFragment() {
 		super();
 	}
 	
 	public SearchResultFragment(List<String> siteIds) {
 		this.siteIds = siteIds;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		
 		// init Loader for TrendingSites
 		this.getLoaderManager().initLoader(SEARCH_SITE_INFO_LOADER_ID, null, this);		
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.fragment_search_result, null);
 		
 		// initialize search result		
 		final ListView lv = (ListView) v.findViewById(R.id.searchResultList);
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
 				Intent i = new Intent(SearchResultFragment.this.getActivity(), ExploreActivity.class);
 				i.putExtra(ExploreActivity.SITE_ID_ARGUMENT_KEY, ((ViewHolder) view.getTag()).textView.getText());
 				startActivity(i);				
 			}
 		});				
 		
		newAdapter = new SearchResultAdapter(this.getActivity());	
		if (siteIds != null) {
			for(String id : siteIds) {
			    newAdapter.add(new SearchResultItem(id, null));			  	
			}
 		}
 		lv.setAdapter(newAdapter);
 		
 		v.requestFocus();
 		return v;
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
 		if (siteIds == null || siteIds.size() == 0) {
 			return null;
 		}
 		
 		String[] projection = SiteInfoTable.ALL_COLUMNS;		
 		// construct the select args
 		String selection = getSelection(siteIds.size());
 		String[] selectionArgs = new String[siteIds.size()]; 
 		for(int i = 0; i < siteIds.size(); i++) {
 			selectionArgs[i] = siteIds.get(i);
 		}
 		// qeury all the siteIds
 		CursorLoader cursorLoader = new CursorLoader(this.getActivity(), 
 				MarsContentProvider.CONTENT_URI_ALL_SITES, projection, selection, selectionArgs, null);
 		return cursorLoader;
 	}
 	
 	private static String getSelection(int args) {
 	    StringBuilder sb = new StringBuilder(SiteInfoTable.COLUMN_SITE_ID + " IN  (");
 	    boolean first = true;
 	    for (int i = 0; i < args; i ++) {
 	        if (first) {
 	            first = false;
 	        } else {
 	            sb.append(',');
 	        }
 	        sb.append('?');
 	    }
 	    sb.append(')');
 	    return sb.toString();
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
 		Log.d(TAG, "Finished loading site info in search");
 		if (data.getCount() == 0) {				  
 		    return;				  	
 		}
 				
 		Log.d(TAG, "Finished loading site info in search");
 		while(!data.isAfterLast()) {				  	
 		    String siteId = data.getString(				  	
 		    		data.getColumnIndex(SiteInfoTable.COLUMN_SITE_ID));			  	
 		    String posterUrl = data.getString(				  	
 		    		data.getColumnIndex(SiteInfoTable.COLUMN_POSTER_IMAGE_URL));			
 		    newAdapter.updateRecord(siteId, posterUrl);
 			data.moveToNext();			  	
 		}
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> loader) {
 
 	}
 }
