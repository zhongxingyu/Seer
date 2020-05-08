 package com.transitwidget.fragments;
 
import android.app.Activity;
 import android.content.ContentUris;
 import android.database.Cursor;
 import android.net.Uri;
import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.transitwidget.R;
 import com.transitwidget.feed.model.Favorite;
 import com.transitwidget.fragments.StopListFragment.Listener;
 
 public class FavoritesListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks {
     public static final String ARG_AGENCY_TAG = "agencyTag";
 
 	private SimpleCursorAdapter mAdapter;
 	private Listener mListener;
     
     private String mAgency;
     
     public FavoritesListFragment() {
         super();
     }
     
     public FavoritesListFragment(Listener listener, Bundle args) {
         super();
         mListener = listener;
         setArguments(args);
     }
 
     @Override
     public void onStart() {
     	super.onStart();
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         mAgency = getArguments().getString(ARG_AGENCY_TAG);
       
         String[] from = {Favorite.STOP_LABEL, Favorite.ROUTE, Favorite.DIRECTION_LABEL};
         int[] to = {R.id.stop, R.id.route, R.id.direction};
 
         mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.favorite_list_item, null, from, to);
         setListAdapter(mAdapter);
         
         // Prepare the loader.  Either re-connect with an existing one,
         // or start a new one.
         getLoaderManager().initLoader(0, null, this);
     }
     
     
 
     @Override
     public void onResume() {
     	super.onResume();
     }
 
     @Override
     public void onListItemClick(ListView l, View v, int position, long id) {
     	super.onListItemClick(l, v, position, id);
 		Cursor cursor = getActivity().getContentResolver().query(
                 ContentUris.withAppendedId(Favorite.CONTENT_URI, id), 
                 new String[] { Favorite.STOP, Favorite.DIRECTION, Favorite.ROUTE }, 
                 null, null, null);
         
 		String tag = null, direction = null, route = null;
 		if (cursor.moveToFirst()) {
 			tag = cursor.getString(cursor.getColumnIndex(Favorite.STOP));
 			direction = cursor.getString(cursor.getColumnIndex(Favorite.DIRECTION));
 			route = cursor.getString(cursor.getColumnIndex(Favorite.ROUTE));
 		}
 
 		if (tag != null) {
     		mListener.stopSelected(tag, direction, route);
 		}
     }
     
     
     public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
         Uri baseUri = Favorite.CONTENT_URI;
 
         String selection = Favorite.AGENCY + " = ?";
         String[] selecitonArgs = { mAgency };
         String[] projection = { Favorite.STOP_LABEL, Favorite.ROUTE, Favorite.DIRECTION_LABEL, Favorite._ID };
         
         return new CursorLoader(getActivity(), baseUri,
                 projection, selection, selecitonArgs,
                 Favorite.ROUTE + " ASC");
     }
 
     public void onLoadFinished(Loader loader, Object data) {
        mAdapter.swapCursor((Cursor)data);
     }
 
     public void onLoaderReset(Loader loader) {
        mAdapter.swapCursor(null);
     }
 }
