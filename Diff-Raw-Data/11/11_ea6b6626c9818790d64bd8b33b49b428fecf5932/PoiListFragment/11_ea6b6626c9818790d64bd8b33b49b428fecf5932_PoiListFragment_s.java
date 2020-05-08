 package com.twormobile.mytravelasia.feed;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CursorAdapter;
 import android.widget.ListView;
 import com.twormobile.mytravelasia.db.MtaPhProvider;
 import com.twormobile.mytravelasia.model.Poi;
 import com.twormobile.mytravelasia.util.AppConstants;
 import com.twormobile.mytravelasia.util.Log;
 
 /**
  * A fragment which shows a list of POIs (points of interest).
  *
  * @author avendael
  */
 public class PoiListFragment extends ListFragment {
     private static final String TAG = PoiListFragment.class.getSimpleName();
 
     private PoiCursorAdapter mAdapter;
     private LoaderCallbacks<Cursor> mLoader;
 
     private Callbacks mCallbacks = sDummyCallbacks;
     private static Callbacks sDummyCallbacks = new Callbacks() {
         @Override
         public void onPoiSelected(int position) {
         }
     };
 
     public interface Callbacks {
         /**
          * The action that the activity will execute when a POI is clicked.
          * @param position The clicked POI's position in the list.
          */
         public void onPoiSelected(int position);
     }
 
     /**
      * Loads POI items from the DB into the adapter asynchronously.
      */
     private class PoiListLoader implements LoaderCallbacks<Cursor> {
         String[] projection = {
                 Poi._ID, Poi.NAME, Poi.ADDRESS, Poi.TOTAL_COMMENTS, Poi.TOTAL_LIKES,
                 Poi.LATITUDE, Poi.LONGITUDE, Poi.IMAGE_THUMB_URL
         };
 
         @Override
         public Loader<Cursor> onCreateLoader(int id, Bundle args) {
             Log.d(TAG, "created loader");
             return new CursorLoader(getActivity(), MtaPhProvider.POI_URI, projection, null, null, null);
         }
 
         @Override
         public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
             Log.d(TAG, "load finsihed");
             mAdapter.swapCursor(data);
         }
 
         @Override
         public void onLoaderReset(Loader<Cursor> loader) {
             mAdapter.swapCursor(null);
         }
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
 
         if (!(activity instanceof Callbacks)) {
             throw new IllegalStateException(AppConstants.EXC_ACTIVITY_CALLBACK);
         }
 
         mCallbacks = (Callbacks) activity;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         Log.d(TAG, "onCreateView() -- START");
         View view = super.onCreateView(inflater, container, savedInstanceState);
         mAdapter = new PoiCursorAdapter(getActivity(), null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
         mLoader = new PoiListLoader();
 
         setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, mLoader);
 
         Log.d(TAG, "onCreateView() -- END");
         return view;
     }
 
     @Override
     public void onListItemClick(ListView listView, View view, int position, long id) {
         super.onListItemClick(listView, view, position, id);
 
         mCallbacks.onPoiSelected(position);
     }
 }
