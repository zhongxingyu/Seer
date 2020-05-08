 package com.uauker.apps.tremrio.adapters;
 
 import java.util.List;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.uauker.apps.tremrio.R;
 import com.uauker.apps.tremrio.models.Station;
 
 public class RamalStationAdapter extends ArrayAdapter<String> {
 
 	private List<Station> datasource;
 	private LayoutInflater inflater;
 
 	public RamalStationAdapter(Context context, int textViewResourceId,
 			List<Station> objects) {
 		super(context, textViewResourceId);
 
 		this.datasource = objects;
 		this.inflater = LayoutInflater.from(context);
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View rowView = convertView;
 
 		rowView = inflater.inflate(R.layout.cell_ramal, parent, false);
 
 		Station station = this.get(position);
 		station.backgroundColor = station.backgroundColor.replace("#", "");
 
 		TextView stationView = (TextView) rowView
 				.findViewById(R.id.ramal_station_name);
 		stationView.setText(station.name);
 		stationView.setBackgroundColor(Integer.parseInt(
 				station.backgroundColor, 16) + 0xFF000000);
 
 		TextView footer = (TextView) rowView
 				.findViewById(R.id.ramal_station_indicator_footer);
 		footer.setBackgroundColor(Integer.parseInt(
 				station.backgroundColor, 16) + 0xFF000000);
 		
 		LinearLayout border = (LinearLayout) rowView
 				.findViewById(R.id.ramal_station_indicator_border);
 		border.setBackgroundColor(Integer.parseInt(
 				station.backgroundColor, 16) + 0xFF000000);
 		
 		TextView startIndicator = (TextView) rowView
 				.findViewById(R.id.ramal_start_direction);

 		TextView startDirectionStatus = (TextView) rowView
 				.findViewById(R.id.ramal_start_direction_status);
 		startDirectionStatus.setText(station.startDirectionStatus);
 
 		TextView endDirection = (TextView) rowView
 				.findViewById(R.id.ramal_end_direction);
 		endDirection.setText(station.endDirection);
 
 		TextView endDirectionStatus = (TextView) rowView
 				.findViewById(R.id.ramal_end_direction_status);
 		endDirectionStatus.setText(station.endDirectionStatus);
 
 		return rowView;
 	}
 
 	@Override
 	public int getCount() {
 		return this.datasource.size();
 	}
 
 	public Station get(int position) {
 		return this.datasource.get(position);
 	}
 }
