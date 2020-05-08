 package org.inftel.tms.mobile.ui.fragments;
 
 import static org.inftel.tms.mobile.contentproviders.FencesContentProvider.KEY_ID;
 import static org.inftel.tms.mobile.contentproviders.FencesContentProvider.KEY_LOCATION_LAT;
 import static org.inftel.tms.mobile.contentproviders.FencesContentProvider.KEY_LOCATION_LNG;
 import static org.inftel.tms.mobile.contentproviders.FencesContentProvider.KEY_NAME;
 import static org.inftel.tms.mobile.contentproviders.FencesContentProvider.KEY_RADIUS;
 import static org.inftel.tms.mobile.contentproviders.FencesContentProvider.KEY_ZONE_TYPE;
 
 import org.inftel.tms.mobile.contentproviders.FencesContentProvider;
 import org.inftel.tms.mobile.ui.FencesActivity;
 
 import android.content.ContentUris;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.view.View;
 import android.widget.ListView;
 
 /**
  * Fragmento UI para mostra lista de fences del dispositivo.
  */
 public class FenceListFragment extends ListFragment implements LoaderCallbacks<Cursor> {
 
 	protected Cursor cursor = null;
 	protected SimpleCursorAdapter adapter;
 	protected FencesActivity activity;
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
		// this is very important in order to save the state across screen configuration changes
		setRetainInstance(true);

 		activity = (FencesActivity) getActivity();
 
 		// Create a new SimpleCursorAdapter that displays the name of each nearby
 		// venue and the current distance to it.
 		adapter = new SimpleCursorAdapter(
 			activity,
 			android.R.layout.two_line_list_item,
 			cursor,
 			new String[] { FencesContentProvider.KEY_NAME, FencesContentProvider.KEY_ZONE_TYPE },
 			new int[] { android.R.id.text1, android.R.id.text2 },
 			0);
 		// Allocate the adapter to the List displayed within this fragment.
 		setListAdapter(adapter);
 
 		// Populate the adapter / list using a Cursor Loader.
 		getLoaderManager().initLoader(0, null, this);
 	}
 
 	/**
 	 * {@inheritDoc} When a venue is clicked, fetch the details from your server and display the
 	 * detail page.
 	 */
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long theid) {
 		super.onListItemClick(l, v, position, theid);
 
 		// Find the ID and Reference of the selected fence.
 		Cursor c = adapter.getCursor();
 		c.moveToPosition(position);
 
 		Uri fenceUri = ContentUris.withAppendedId(FencesContentProvider.CONTENT_URI,
 			c.getLong(c.getColumnIndex(FencesContentProvider.KEY_ID)));
 
 		// Request the parent Activity display the venue detail UI.
 		activity.selectDetail(fenceUri.toString());
 	}
 
 	/**
 	 * {@inheritDoc} This loader will return the ID, Reference, Name, and Distance of all the venues
 	 * currently stored in the {@link PlacesContentProvider}.
 	 */
 	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 		String[] projection = new String[] { KEY_ID, KEY_NAME, KEY_ZONE_TYPE,
 			KEY_LOCATION_LAT, KEY_LOCATION_LNG, KEY_RADIUS };
 
 		return new CursorLoader(activity, FencesContentProvider.CONTENT_URI, projection,
 			null, null, null);
 	}
 
 	/**
 	 * {@inheritDoc} When the loading has completed, assign the cursor to the adapter / list.
 	 */
 	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
 		adapter.swapCursor(data);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void onLoaderReset(Loader<Cursor> loader) {
 		adapter.swapCursor(null);
 	}
 }
