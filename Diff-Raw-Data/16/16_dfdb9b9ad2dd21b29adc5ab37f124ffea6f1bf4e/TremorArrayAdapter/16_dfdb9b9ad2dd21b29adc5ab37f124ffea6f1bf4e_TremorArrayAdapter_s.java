 /**
  * 
  */
 package com.captech.mehalso.tremorfeed;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 import com.captech.mehalso.tremorfeed.pojo.TremorRecord;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 /**
  * @author mmehalso
  *
  */
 public class TremorArrayAdapter extends ArrayAdapter<TremorRecord> {
 	private final Activity context;
 	private List<TremorRecord> deletedRecords = new ArrayList<TremorRecord>();
 	private String dateFormat = null;
 	
 	Object syncLock = new Object();
 	
 	static class ViewHolder {
 		//This should match our row item layout.
 		public TextView magFloor;
 		public TextView title;
 		public TextView dateTime;
 		public TextView latLong;
 	}
 	
 	/** 
 	 * Constructor... We want to pull data asynchronously, so empty for now.
 	 * @param context
 	 */
 	public TremorArrayAdapter(Activity context) {
 		super(context, R.layout.row_layout);
 		this.context = context;
 		dateFormat = context.getString(R.string.row_date_format);
 	}
 	
 	/**
 	 * Convenience method to sort tremor records by date after update.
 	 */
 	public void sortRecordsByDate() {
 		//Need to synchronize because we depend on index of a given record during the merge process			
 		this.sort(new Comparator<TremorRecord>() {
 			public int compare(TremorRecord lhs, TremorRecord rhs) {
 				if(lhs.getEventDateUtc().after(rhs.getEventDateUtc())) {
 					return -1;
 				} else if(lhs.getEventDateUtc().equals(rhs.getEventDateUtc())) {
 					return 0;
 				} else {
 					return 1;
 				}
 			}
 		});
 	}
 	
 	/**
 	 * Custom getView for row layout.  Uses tagging pattern for performance. 
 	 */
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View rowView = convertView;
 		
 		if(rowView == null) {
 			rowView = context.getLayoutInflater().inflate(R.layout.row_layout, null);
 			ViewHolder viewHolder = new ViewHolder();
 			viewHolder.title = (TextView) rowView.findViewById(R.id.row_title);
 			viewHolder.magFloor = (TextView) rowView.findViewById(R.id.row_magFloor);
 			viewHolder.latLong = (TextView) rowView.findViewById(R.id.row_lat_long);
 			viewHolder.dateTime = (TextView) rowView.findViewById(R.id.row_date_time);
 			 
 			rowView.setTag(viewHolder);
 		}
 		
 		ViewHolder holder = (ViewHolder) rowView.getTag();
 		
 		TremorRecord record = getItem(position);
 		holder.title.setText(record.getTitle());
 		holder.magFloor.setText(record.getFloorMag().toString());
 		
 		//sdf not threadsafe... can I be sure that this method will always 
 		//get called on the UI thread? 
 		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
 		
 		holder.dateTime.setText(sdf.format(record.getEventDateUtc()));
 		holder.latLong.setText("Location [" + record.getLatitude() + ", " + record.getLongitude() + "]");
 		
 		if(record.getFloorMag().intValue() >= 5 && 
 				record.getFloorMag().intValue() < 7) {
 			holder.magFloor.setTextColor(Color.rgb(255, 0, 0));
 			//no background on the medium size eq's = better UX
 			//rowView.setBackgroundResource(R.color.large_quake_row_color);
			//rowView.setBackgroundResource(R.color.normal_row_color);
 		} else if(record.getFloorMag().intValue() >= 7){
 			//dark red
 			holder.magFloor.setTextColor(Color.rgb(128, 0, 0));
			//rowView.setBackgroundResource(R.color.huge_quake_row_color);
 		} else {
 			//normal 
 			holder.magFloor.setTextColor(Color.BLACK);
			//rowView.setBackgroundResource(R.color.normal_row_color);
 		}
 
 		return rowView;
 	}
 	
 	/**
 	 * Intercepting add() operation so that we can check whether item already exists or
 	 * has been deleted before adding to collection.
 	 */
 	@Override
 	public void add (TremorRecord record) {
 		//No need to synchronize here, because all data is added on the UI thread.
 		int index = 0;
 		if(deletedRecords.contains(record)){
 			//item already deleted by user, do not re-add.
 			Log.i(TremorConstants.LOG_TAG, "Attempt to add record explicitly deleted by user. Ignoring.  " +
 					"Record URL [" + record.getUrl() + "]");
 		} else if((index = this.getPosition(record)) > -1) {
 			//item already exists in the list, do not re-add. Copy any updated data that might exist.  
 			Log.i(TremorConstants.LOG_TAG, "Attempt to add record already in list. Merging record URL [" + 
 					record.getUrl() + "].");
 			
 			this.getItem(index).copyValues(record);
 		} else {
 			Log.i(TremorConstants.LOG_TAG, "Adding record with URL [" + record.getUrl() + "].");
 			super.add(record);
 		}
 	}
 	
 	/**
 	 * Intercepting clear() operation to remove from deleted items as well.
 	 */
 	@Override
 	public void clear () {
 		deletedRecords.clear();
 		super.clear();
 	}
 	
 	/**
 	 * Explicit deletion of a record per user action.
 	 * @param record
 	 */
 	public void delete(TremorRecord record) {
 		deletedRecords.add(record);
 		super.remove(record);
 	}
 }
