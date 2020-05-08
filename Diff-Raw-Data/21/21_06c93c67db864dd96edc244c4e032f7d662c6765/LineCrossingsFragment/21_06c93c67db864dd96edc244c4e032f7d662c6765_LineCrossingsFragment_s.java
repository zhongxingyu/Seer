 /*******************************************************************************
  * Copyright 2013 Keith Cassidy
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package com.keithcassidy.finishline;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Locale;
 import java.util.TimeZone;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
 import android.app.AlertDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.location.Location;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.CheckedTextView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class LineCrossingsFragment extends SherlockFragment implements TabFocusInterface 
 {
 
 	protected static final String TAG = LineCrossingsFragment.class.getSimpleName();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		setHasOptionsMenu(true);
 		super.onCreate(savedInstanceState);
 	}
 
 
 	private void sendCrossing(Location crossing)
 	{
 		SimpleDateFormat dFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
 		dFormat.setTimeZone(TimeZone.getDefault());
 		String text = PreferencesUtils.getBoatName(getActivity()) + 
 						" " + dFormat.format(new Date(crossing.getTime()));
 
 		Intent sendIntent = new Intent();
 		sendIntent.setAction(Intent.ACTION_SEND);
 		sendIntent.putExtra(Intent.EXTRA_TEXT, text );
 		sendIntent.setType("text/plain");
 		startActivity(sendIntent);
 	}
 
 
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
 	{
 
 		MenuItem clearListMenuItem = menu.add(R.string.menu_clear_crossing_times);
 		clearListMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 		clearListMenuItem.setIcon(android.R.drawable.ic_menu_delete);
 
 		clearListMenuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() 
 		{
 
 			@Override
 			public boolean onMenuItemClick(MenuItem item) 
 			{
 				try
 				{
 					deleteCrossings();
 				}
 				catch(Exception e)
 				{
 					Log.e(TAG, "onMenuItemClick " + e);
 				}
 				return true;
 			}});
 
 		MenuItem shareMenuItem = menu.add(R.string.menu_share);
 		shareMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 		shareMenuItem.setIcon(android.R.drawable.ic_menu_share);
 
 		shareMenuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() 
 		{
 			@Override
 			public boolean onMenuItemClick(MenuItem item) 
 			{
 				try
 				{
 					sendCurrentCrossing();
 				}
 				catch(Exception e)
 				{
 					Log.e(TAG, "onMenuItemClick " + e);
 				}
 				return true;
 			}});
 
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 
 	protected void sendCurrentCrossing() 
 	{
 		try
 		{
			Location l = mAdapter.getItem(mListView.getCheckedItemPosition());
			if( l != null )
 			{
				sendCrossing(l);
			}			
 		}
 		catch(Exception e)
 		{
 			Log.e(TAG, "sendCurrentCrossing " + e);
 		}
 	}
 
 	protected void deleteCrossings() 
 	{
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		builder.setMessage(getString(R.string.confirm_delete_crossings)).setPositiveButton(getString(R.string.yes), dialogClickListener)
 		.setNegativeButton(getString(R.string.no), dialogClickListener).show();
 	}
 
 	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			switch (which){
 			case DialogInterface.BUTTON_POSITIVE:
 				dbStorage.deleteRaceCrossings();
 				loadList();
 				break;
 
 			case DialogInterface.BUTTON_NEGATIVE:
 				//No button clicked
 				break;
 			}
 		}
 	};	
 
 
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) 
 	{
 		super.onViewCreated(view, savedInstanceState);
 
 		loadList();
 
 	}
 
 
 	private ArrayAdapter<Location> mAdapter;
 	private FinishLineDataStorage dbStorage = null;
 	private ListView mListView;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) 
 	{
 		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onBroadcastServiceStatusReceived,
 				new IntentFilter(Constants.SERVICE_STATUS_MESSAGE));
 
 		View view = inflater.inflate(R.layout.crossing_times, container, false);
 
 		if( dbStorage == null)
 		{
 			dbStorage = new FinishLineDataStorage(getActivity()); 
 			dbStorage.open();
 		}
 
 		mListView = (ListView)view.findViewById(R.id.listViewCrossings);
 		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) 
 			{
 				Location l = ((Location)parent.getItemAtPosition(position));
 				if(l != null)
 				{
 					sendCrossing(l);
 				}
 				return true;
 			}});
 
 
 		return view;
 
 	}
 
 
 	@Override
 	public void onDestroy() 
 	{
 		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onBroadcastServiceStatusReceived);
 
 		if( dbStorage != null )
 		{
 			dbStorage.close();
 		}
 		super.onDestroy();
 	}
 
 
 	private BroadcastReceiver onBroadcastServiceStatusReceived = new BroadcastReceiver() 
 	{
 		@Override
 		public void onReceive(Context context, Intent intent) 
 		{	
 			if( intent.getBooleanExtra(Constants.FINISHLINE_CROSSED_MESSAGE, false))
 			{
 				loadList();
 			}
 		}
 	};	
 
 
 
 	private void loadList()
 	{
 		ListView list = (ListView)getActivity().findViewById(R.id.listViewCrossings);
 		TextView noneYet = (TextView)getActivity().findViewById(R.id.textViewNone);
 		if( list != null && noneYet != null )
 		{
 			list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 			
 			list.setOnItemClickListener(new OnItemClickListener() 
 			{
 				@Override
 				public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
 				{
 					
 					getListView().setItemChecked(position, true);
 					
 					CheckedTextView c = (CheckedTextView)view.findViewById(R.id.crossing_time);
 					if( c!= null )
 					{
 						c.setChecked(true);					
 					}
 				}
 			});
 			
 			ArrayList<Location> crossings = (ArrayList<Location>)dbStorage.getCrossings();
 
 			if( crossings.isEmpty())
 			{
 				list.setVisibility(ListView.INVISIBLE);
 				noneYet.setVisibility(ListView.VISIBLE);
 			}
 			else
 			{
 				list.setVisibility(ListView.VISIBLE);
 				noneYet.setVisibility(ListView.INVISIBLE);
 
				
				mAdapter = new CrossingsAdapter(getActivity(), R.layout.crossing_list_item, crossings);
				
				list.setAdapter(mAdapter);

 				//select the last one in the list by default
 				list.setItemChecked(crossings.size() - 1, true);
 			}
 			
 		    list.post(new Runnable() {
 		        @Override
 		        public void run() {
 		        	getListView().setSelection(getListView().getCount() - 1);
 		        }
 		    });			
 		}
 	}
 	
 	
 	public ListView getListView() 
 	{
 		return mListView;
 	}
 
 	public class CrossingsAdapter extends ArrayAdapter<Location> 
 	{
 		private ArrayList<Location> items;
 		private CrossingViewHolder crossingHolder;
 		final ListView list = getListView();
 
 		private class CrossingViewHolder 
 		{
 			CheckedTextView time;
 			TextView location; 
 			boolean isChecked;
 		}
 
 		public CrossingsAdapter(Context context, int tvResId, ArrayList<Location> items) 
 		{
 			super(context, tvResId, items);
 			this.items = items;
 		}
 
 		@Override
 		public View getView(int pos, View convertView, ViewGroup parent) 
 		{
 			View v = convertView;
 			if (v == null) 
 			{
 				getActivity();
 				LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(FragmentActivity.LAYOUT_INFLATER_SERVICE);
 				v = inflater.inflate(R.layout.crossing_list_item, null);
 				crossingHolder = new CrossingViewHolder();
 				crossingHolder.time = (CheckedTextView)v.findViewById(R.id.crossing_time);
 				crossingHolder.location = (TextView)v.findViewById(R.id.crossing_location);
 				v.setTag(crossingHolder);
 			} 
 			else 
 			{
 				crossingHolder = (CrossingViewHolder)v.getTag(); 
 			}
 
 
 
 			Location crossing = items.get(pos);
 			if (crossing != null) 
 			{
 
 				if( getListView().isItemChecked(pos))
 				{
 					((View) crossingHolder.time.getParent()).setBackgroundColor(getResources().getColor(R.color.finishLineHighlight));
 					crossingHolder.time.setTextColor(getResources().getColor(R.color.abs__background_holo_dark));
 					crossingHolder.location.setTextColor(getResources().getColor(R.color.abs__background_holo_dark));
 				}
 				else
 				{
 					crossingHolder.time.setTextColor(getResources().getColor(R.color.abs__primary_text_holo_dark));
 					crossingHolder.location.setTextColor(getResources().getColor(R.color.abs__primary_text_holo_dark));
 					((View) crossingHolder.time.getParent()).setBackgroundColor(getResources().getColor(R.color.abs__background_holo_dark));
 				}
 
 				DateFormat dFormat = DateFormat.getDateTimeInstance();
 				dFormat.setTimeZone(TimeZone.getDefault());
 				crossingHolder.time.setText(dFormat.format(new Date(crossing.getTime())));
 
 				//Date time = new Date(crossing.getTime());
 				//SimpleDateFormat fmt = new SimpleDateFormat("hh:mm:ss", getActivity().getResources().getConfiguration().locale); 
 				//crossingHolder.time.setText(fmt.format(time));
 				crossingHolder.location.setText(
 						PreferencesUtils.locationToString(getContext(), crossing.getLatitude(), true) +
 						" " + 
 						PreferencesUtils.locationToString(getContext(), crossing.getLongitude(), true) );
 			}
 
 			return v;
 		}
 	}
 
 
 	@Override
 	public void tabSetFocus() 
 	{
 	}
 
 
 	@Override
 	public void tabLoseFocus() 
 	{
 	}
 
 }
