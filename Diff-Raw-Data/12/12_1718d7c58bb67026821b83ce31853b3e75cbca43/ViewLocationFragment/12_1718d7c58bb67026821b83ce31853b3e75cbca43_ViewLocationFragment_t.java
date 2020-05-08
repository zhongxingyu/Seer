 package com.charlesmadere.android.fishnspots;
 
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.charlesmadere.android.fishnspots.models.SimpleLocation;
 import com.charlesmadere.android.fishnspots.utilities.DeleteAlertDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.MapFragment;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.MarkerOptions;
 
 
 public class ViewLocationFragment extends DialogFragment
 {
 
 
 	public final static String TAG = "ViewLocationFragment";
 
 	public final static String KEY_LOCATION_ID = "KEY_LOCATION_ID";
 	public final static String KEY_LOCATION_NAME = "KEY_LOCATION_NAME";
 	public final static String KEY_LOCATION_ALTITUDE = "KEY_LOCATION_ALTITUDE";
 	public final static String KEY_LOCATION_LATITUDE = "KEY_LOCATION_LATITUDE";
 	public final static String KEY_LOCATION_LONGITUDE = "KEY_LOCATION_LONGITUDE";
 
 
 
 
 	private MapFragment mapFragment_map;
 	private TextView textView_name;
 	private TextView textView_altitude;
 	private TextView textView_latitude;
 	private TextView textView_longitude;
 	private Button button_deleteLocation;
 	private Button button_updateLocation;
 
 
 	/**
 	 * The SimpleLocation object that this Fragment will be viewing.
 	 */
 	private SimpleLocation location;
 
 
 	/**
 	 * Object that allows us to run any of the methods that are defined in the
 	 * ViewLocationFragmentListeners interface.
 	 */
 	private ViewLocationFragmentListeners listeners;
 
 
 	/**
 	 * A bunch of listener methods for this Fragment.
 	 */
 	public interface ViewLocationFragmentListeners
 	{
 
 
 		/**
 		 * This is fired whenever the user clicks the Delete Location button in
 		 * this Fragment's layout.
 		 * 
 		 * @param location
 		 * The SimpleLocation that the user has decided to delete.
 		 */
 		public void viewLocationFragmentOnClickDeleteLocation(final SimpleLocation location);
 
 
 		/**
 		 * This is fired whenever the user taps the Update Location button in
 		 * this Fragment's layout.
 		 * 
 		 * @param location
 		 * The SimpleLocation that the user has decided to update.
 		 */
 		public void viewLocationFragmentOnClickUpdateLocation(final SimpleLocation location);
 
 
 	}
 
 
 
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 	}
 
 
 	@Override
 	public Dialog onCreateDialog(final Bundle savedInstanceState)
 	{
 		final Dialog dialog = super.onCreateDialog(savedInstanceState);
 		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 
 		return dialog;
 	}
 
 
 	@Override
 	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
 	{
 		return inflater.inflate(R.layout.view_location_fragment, null);
 	}
 
 
 	@Override
 	public void onActivityCreated(final Bundle savedInstanceState)
 	{
 		super.onActivityCreated(savedInstanceState);
 
 		final Bundle arguments = getArguments();
 
 		location = new SimpleLocation
 		(
 			arguments.getLong(KEY_LOCATION_ID),
 			arguments.getString(KEY_LOCATION_NAME),
 			arguments.getDouble(KEY_LOCATION_ALTITUDE),
 			arguments.getDouble(KEY_LOCATION_LATITUDE),
 			arguments.getDouble(KEY_LOCATION_LONGITUDE)
 		);
 
 		findViews();
 
 		button_deleteLocation.setOnClickListener(new View.OnClickListener()
 		{
 			@Override
 			public void onClick(final View v)
 			{
 				final DeleteAlertDialog deleteAlertDialog = new DeleteAlertDialog(getActivity(), location, new DeleteAlertDialog.DeleteAlertDialogListeners()
 				{
 					@Override
 					public void cancel()
 					{
 
 					}
 
 
 					@Override
 					public void delete(final SimpleLocation location)
 					{
 						listeners.viewLocationFragmentOnClickDeleteLocation(location);
 					}
 				});
 
 				deleteAlertDialog.show();
 			}
 		});
 
 		button_updateLocation.setOnClickListener(new View.OnClickListener()
 		{
 			@Override
 			public void onClick(final View v)
 			{
 				listeners.viewLocationFragmentOnClickUpdateLocation(location);
 			}
 		});
 
 		flushViews();
 	}
 
 
 	@Override
 	public void onAttach(final Activity activity)
 	// This makes sure that the Activity containing this Fragment has
 	// implemented the callback interface. If the callback interface has not
 	// been implemented, an exception is thrown.
 	{
 		super.onAttach(activity);
 
 		try
 		{
 			listeners = (ViewLocationFragmentListeners) activity;
 		}
 		catch (final ClassCastException e)
 		{
 			throw new ClassCastException(activity.toString() + " must implement listeners!");
 		}
 	}
 
 
 
 
 	/**
 	 * Checks to see if all of the layout items that we're using from this
 	 * class's layout have been found (and are not null). If any single one has
 	 * not been found (and is therefore null), then this method will find every
 	 * single layout item.
 	 */
 	private void findViews()
 	{
 		if (mapFragment_map == null || textView_name == null || textView_altitude == null || textView_latitude == null
 			|| textView_longitude == null || button_deleteLocation == null || button_updateLocation == null)
 		{
 			final View view = getView();
 
 			mapFragment_map = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.view_location_fragment_map_fragment);
 			textView_name = (TextView) view.findViewById(R.id.view_location_fragment_textview_name);
 			textView_altitude = (TextView) view.findViewById(R.id.view_location_fragment_textview_altitude);
 			textView_latitude = (TextView) view.findViewById(R.id.view_location_fragment_textview_latitude);
 			textView_longitude = (TextView) view.findViewById(R.id.view_location_fragment_textview_longitude);
 			button_deleteLocation = (Button) view.findViewById(R.id.view_location_fragment_button_delete_location);
 			button_updateLocation = (Button) view.findViewById(R.id.view_location_fragment_button_update_location);
 		}
 	}
 
 
 	/**
 	 * Sets the text for all of this layout's TextView item's to the
 	 * actual data in the SimpleLocation object.
 	 */
 	private void flushViews()
 	{
		final LatLng latLng = new LatLng((float) location.getLatitude(), (float) location.getLongitude());

 		final GoogleMap map = mapFragment_map.getMap();
		map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		map.moveCamera(CameraUpdateFactory.zoomBy(10));
		map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

 		map.addMarker(new MarkerOptions()
			.position(latLng)
 			.title(location.getName()));
 
 		textView_name.setText(location.getName());
 		textView_altitude.setText(String.valueOf(location.getAltitude()));
 		textView_latitude.setText(String.valueOf(location.getLatitude()));
 		textView_longitude.setText(String.valueOf(location.getLongitude()));
 	}
 
 
 }
