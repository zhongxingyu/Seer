 /*
  * Copyright (C) 2011 Morphoss Ltd
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package com.morphoss.acal.activity;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.View.OnClickListener;
 import android.view.View.OnCreateContextMenuListener;
 import android.widget.BaseAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.morphoss.acal.R;
 import com.morphoss.acal.acaltime.AcalDateTime;
 import com.morphoss.acal.davacal.AcalEvent;
 import com.morphoss.acal.davacal.AcalEventAction;
 
 /**
  * <p>
  * Adapter for providing views for events.
  * </p>
  * 
  * @author Morphoss Ltd
  * 
  */
 public class EventListAdapter extends BaseAdapter implements OnClickListener, ListAdapter {
 
 	/**
 	 * <p>
 	 * Presently this view is only supported in month view. If we decide to extend this view further we should
 	 * create An interface for providing callbacks.
 	 * </p>
 	 */ 
 	private MonthView context;
 	private AcalDateTime viewDate;
 	private AcalDateTime viewDateEnd;
 	public static final String TAG = "aCal EventListAdapter";
 	private volatile boolean clickEnabled = true;
 	public static final int CONTEXT_EDIT = 0;
 	public static final int CONTEXT_DELETE_ALL = 0x10000;
 	public static final int CONTEXT_DELETE_JUSTTHIS = 0x20000;
 	public static final int CONTEXT_DELETE_FROMNOW = 0x30000;
 	public static final int CONTEXT_COPY = 0x40000;
 	
 	private SharedPreferences prefs;	
 
 	/**
 	 * <p>Create a new adaptor with the attributes provided. The date range provided specifies the date range that all
 	 * events provided by this adapter fall within. This may change as the calling class may have the required events
 	 * in memory already.</p>
 	 */
 	public EventListAdapter(MonthView monthview, AcalDateTime date) {
 		this.context = monthview;
 		viewDate = date;
 		viewDate.applyLocalTimeZone();
 		viewDate.setDaySecond(0);
 		viewDateEnd = AcalDateTime.addDays(viewDate, 1);
 
 		// Get preferences
 		prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
 	}
 
 	/**
 	 * <p>Returns the number of elements in this adapter.</p>
 	 * 
 	 * (non-Javadoc)
 	 * @see android.widget.Adapter#getCount()
 	 */
 	@Override
 	public int getCount() {
 		return context.getNumberEventsForDay(viewDate);
 		//return 0;
 	}
 
 	/**
 	 * <p>Returns the event at specified the position in this adapter or null if position is invalid.</p> 
 	 * 
 	 * (non-Javadoc)
 	 * @see android.widget.Adapter#getItem(int)
 	 */
 	@Override
 	public Object getItem(int position) {
 		return context.getNthEventForDay(viewDate, position);
 		//return null;
 	}
 
 	/**
 	 * <p>Returns the id associated with the event at specified position. Currently not implemented (i.e. returns position)</p>
 	 * 
 	 * (non-Javadoc)
 	 * @see android.widget.Adapter#getItemId(int)
 	 */
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 
 	/**
 	 * <p>Returns the view asscoiated with the event at the specified position. Currently, views do not respond to any events.</p> 
 	 * 
 	 * (non-Javadoc)
 	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
 	 */
 	@Override
 	public View getView(final int position, View convertView, ViewGroup parent) {
 		LinearLayout rowLayout;
 
 		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		TextView title = null, time = null, location = null;
 		rowLayout = (LinearLayout) inflater.inflate(R.layout.event_list_item, parent, false);
 
 		title = (TextView) rowLayout.findViewById(R.id.EventListItemTitle);
 		time = (TextView) rowLayout.findViewById(R.id.EventListItemTime);
 		location = (TextView) rowLayout.findViewById(R.id.EventListItemLocation);
 
 		LinearLayout sideBar = (LinearLayout) rowLayout.findViewById(R.id.EventListItemColorBar);
 
 		AcalEvent event = context.getNthEventForDay(viewDate, position);
		if ( event == null ) return rowLayout;

 		final boolean isPending = event.isPending;
 		if (isPending) {
 			sideBar.setBackgroundColor(event.getColour()|0xa0000000); title.setTextColor(event.getColour()|0xa0000000);
 			((LinearLayout) rowLayout.findViewById(R.id.EventListItemText)).setBackgroundColor(0x44000000);
 		}
 		else {
 			sideBar.setBackgroundColor(event.getColour()); 
 			title.setTextColor(event.getColour());
 		}
 		if (event.getSummary()!=null)
 			title.setText(event.getSummary());
 		else
 			title.setText("Untitled");
 
 		time.setText(event.getTimeText(viewDate, viewDateEnd,
 					prefs.getBoolean(context.getString(R.string.prefTwelveTwentyfour), false))
 					+ (isPending ? " (saving)" : "") );
 
 		if (event.getLocation() != null && !event.getLocation().equals("") )
 			location.setText(event.getLocation());
 		else
 			location.setHeight(2);
 
 		rowLayout.setTag(event);
 		rowLayout.setOnTouchListener(this.context);
 		rowLayout.setOnClickListener(this);
 		final boolean repeats = (event.repetition != null && !event.repetition.equals(""));
 		//add context menu
 		this.context.registerForContextMenu(rowLayout);
 		rowLayout.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
 		
 
 			@Override
 			public void onCreateContextMenu(ContextMenu menu, View view,
 					ContextMenuInfo info) {
 				menu.setHeaderTitle("Event");
 				if ( !isPending ) menu.add(0, position, 0, "Edit");
 
 				menu.add(0, CONTEXT_COPY + position, 0, "Copy");
 				if (repeats) {
 					menu.add(0,CONTEXT_DELETE_ALL+position,0, "Delete All Instances");
 					menu.add(0,CONTEXT_DELETE_JUSTTHIS+position, 0, "Delete This Instance");
 					menu.add(0,CONTEXT_DELETE_FROMNOW+position,0, "Delete This and Future");
 				} else {
 					menu.add(0,CONTEXT_DELETE_ALL+position,0, "Delete");
 				}
 			}
 		});
 
 		return rowLayout;
 	}
 
 	public void setClickEnabled(boolean enabled) {
 		this.clickEnabled = enabled;
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		if (clickEnabled) {
 			Object tag = arg0.getTag();
 			if (tag instanceof AcalEvent) {
 				//start event activity
 				Bundle bundle = new Bundle();
 				bundle.putParcelable("Event", new AcalEventAction((AcalEvent)tag));
 				Intent eventViewIntent = new Intent(context, EventView.class);
 				eventViewIntent.putExtras(bundle);
 				context.startActivityForResult(eventViewIntent, MonthView.PICK_TODAY_FROM_EVENT_VIEW);
 			}
 		} else {
 			clickEnabled = true;
 		}
 
 	}
 	
 	public boolean contextClick(MenuItem item) {
 		try {
 			int id = item.getItemId();
 			int action = id & 0xf0000;
 			id = id & 0xffff;
 			String parcelKey = "Copy";
 			switch( action ) {
 				case CONTEXT_EDIT:
 					parcelKey = "Event";  // And then fall through
 					//start event activity
 				case CONTEXT_COPY:
 					Bundle bundle = new Bundle();
 					bundle.putParcelable(parcelKey, new AcalEventAction((AcalEvent)this.getItem(id)));
 					Intent eventViewIntent = new Intent(context, EventEdit.class);
 					eventViewIntent.putExtras(bundle);
 					context.startActivity(eventViewIntent);
 					return true;
 				
 				case CONTEXT_DELETE_ALL:
 					this.context.deleteAllEvent(viewDate,id);
 					return true;
 
 				case CONTEXT_DELETE_JUSTTHIS:
 					this.context.deleteSingleEvent(viewDate,id);
 					return true;
 
 				case CONTEXT_DELETE_FROMNOW:
 					this.context.deleteFutureEvent(viewDate,id);
 					return true;
 			}
 			return false;
 		}
 		catch (ClassCastException e) {
 			return false;
 		}
 		
 	}
 
 }
