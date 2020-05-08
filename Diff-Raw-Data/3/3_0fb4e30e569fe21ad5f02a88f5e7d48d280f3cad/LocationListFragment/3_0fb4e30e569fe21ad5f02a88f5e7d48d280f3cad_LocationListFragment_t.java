 package com.charlesmadere.android.fishnspots;
 
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ListFragment;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import com.charlesmadere.android.fishnspots.models.SimpleLocation;
 import com.charlesmadere.android.fishnspots.models.SimpleLocationsDataSource;
 
 
 public class LocationListFragment extends ListFragment implements
 	OnItemClickListener,
 	OnItemLongClickListener
 {
 
 
 	private SimpleLocationsDataSource dataSource;
 
 
 	/**
 	 * Object that allows us to run any of the methods that are defined in the
 	 * FriendsListFragmentListeners interface.
 	 */
 	private LocationListFragmentListeners listeners;
 
 
 	/**
 	 * A bunch of listener methods for this Fragment.
 	 */
 	public interface LocationListFragmentListeners
 	{
 
 
 		/**
 		 * This method will be fired when the user taps the Save Current
 		 * Location button on the Action Bar.
 		 */
 		public void onClickSaveCurrentLocation();
 
 
 	}
 
 
 
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 
 		dataSource = new SimpleLocationsDataSource(getActivity());
 		dataSource.open();
 
 		refreshLocationList();
 	}
 
 
 	@Override
 	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
 	{
 		return inflater.inflate(R.layout.location_list_fragment, null);
 	}
 
 
 	@Override
 	public void onActivityCreated(final Bundle savedInstanceState)
 	{
 		super.onActivityCreated(savedInstanceState);
 
 		getListView().setOnItemClickListener(this);
 		getListView().setOnItemLongClickListener(this);
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
 			listeners = (LocationListFragmentListeners) activity;
 		}
 		catch (final ClassCastException e)
 		{
 			throw new ClassCastException(activity.toString() + " must implement listeners!");
 		}
 	}
 
 
 	@Override
 	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater)
 	{
 		inflater.inflate(R.menu.location_list_fragment, menu);
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 
 
 	@Override
 	public void onItemClick(final AdapterView<?> l, final View v, final int position, final long id)
 	{
 		
 	}
 
 
 	@Override
 	public boolean onItemLongClick(final AdapterView<?> l, final View v, final int position, final long id)
 	{
 		v.setSelected(true);
 
 		final SimpleLocation location = (SimpleLocation) l.getItemAtPosition(position);
 		final String[] items = getResources().getStringArray(R.array.location_list_fragment_context_menu);
 		final Context context = getActivity();
 
 		final AlertDialog.Builder builder = new AlertDialog.Builder(context)
 			.setItems(items, new DialogInterface.OnClickListener()
 			{
 				@Override
 				public void onClick(final DialogInterface dialog, final int which)
 				{
 					dialog.dismiss();
 
 					switch (which)
 					{
 						case 0:
 						// edit
 							
 							break;
 
 						case 1:
 						// delete
 							final AlertDialog.Builder builder = new AlertDialog.Builder(context)
 								.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
 								{
 									@Override
 									public void onClick(final DialogInterface dialog, final int which)
 									{
 										dialog.dismiss();
 									}
 								})
								.setMessage(R.string.are_you_sure_that_you_want_to_delete_this_location)
 								.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener()
 								{
 									@Override
 									public void onClick(final DialogInterface dialog, final int which)
 									{
 										dialog.dismiss();
 										deleteSimpleLocation(location);
 									}
 								})
 								.setTitle(R.string.delete);
 
 							builder.show();
 							break;
 					}
 				}
 			})
 			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
 			{
 				@Override
 				public void onClick(DialogInterface dialog, int which)
 				{
 					dialog.dismiss();
 				}
 			})
 			.setTitle(R.string.select_an_action);
 
 		builder.show();
 
 		return true;
 	}
 
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case R.id.location_list_fragment_menu_save_current_location:
 				listeners.onClickSaveCurrentLocation();
 				break;
 
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 
 		return true;
 	}
 
 
 
 
 	/**
 	 * Adds a new SimpleLocation to the database.
 	 * 
 	 * @param location
 	 * The SimpleLocation object to be added.
 	 */
 	public void createSimpleLocation(final SimpleLocation location)
 	{
 		dataSource.createSimpleLocation(location);
 		refreshLocationList();
 	}
 
 
 	/**
 	 * 
 	 * 
 	 * @param location
 	 * 
 	 */
 	private void deleteSimpleLocation(final SimpleLocation location)
 	{
 		dataSource.deleteSimpleLocation(location);
 		refreshLocationList();
 	}
 
 
 	/**
 	 * Refreshes the list of SimpleLocations so that any new ones will show up
 	 * or so that any old ones will be deleted.
 	 */
 	private void refreshLocationList()
 	{
 		final ArrayList<SimpleLocation> locations = dataSource.getAllSimpleLocations();
 		setListAdapter(new LocationListAdapter(getActivity(), R.layout.location_list_fragment_item, locations));
 	}
 
 
 
 
 	private final class LocationListAdapter extends ArrayAdapter<SimpleLocation>
 	{
 
 
 		private ArrayList<SimpleLocation> locations;
 		private LayoutInflater inflater;
 
 
 		private LocationListAdapter(final Context context, final int viewResourceId, final ArrayList<SimpleLocation> locations)
 		{
 			super(context, viewResourceId, locations);
 			this.locations = locations;
 			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		}
 
 
 		@Override
 		public View getView(final int position, View convertView, final ViewGroup parent)
 		{
 			if (convertView == null)
 			{
 				convertView = inflater.inflate(R.layout.location_list_fragment_item, null);
 
 				final ViewHolder viewHolder = new ViewHolder
 				(
 					(TextView) convertView.findViewById(R.id.location_list_fragment_item_name),
 					(TextView) convertView.findViewById(R.id.location_list_fragment_item_altitude),
 					(TextView) convertView.findViewById(R.id.location_list_fragment_item_latitude),
 					(TextView) convertView.findViewById(R.id.location_list_fragment_item_longitude)
 				);
 
 				convertView.setTag(viewHolder);
 			}
 
 			final SimpleLocation location = locations.get(position);
 			((ViewHolder) convertView.getTag()).setText(location);
 
 			return convertView;
 		}
 
 
 	}
 
 
 
 
 	private final static class ViewHolder
 	{
 
 
 		private TextView name;
 		private TextView altitude;
 		private TextView latitude;
 		private TextView longitude;
 
 
 		private ViewHolder(final TextView name, final TextView altitude, final TextView latitude, final TextView longitude)
 		{
 			this.name = name;
 			this.altitude = altitude;
 			this.latitude = latitude;
 			this.longitude = longitude;
 		}
 
 
 		private void setText(final SimpleLocation location)
 		{
 			name.setText(location.getName());
 			altitude.setText(String.valueOf(location.getAltitude()));
 			latitude.setText(String.valueOf(location.getLatitude()));
 			longitude.setText(String.valueOf(location.getLongitude()));
 		}
 
 
 	}
 
 
 }
