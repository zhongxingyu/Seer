 package com.saikali.android_skwissh.adapters;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
import android.content.SharedPreferences;
 import android.graphics.Typeface;
import android.preference.PreferenceManager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.saikali.android_skwissh.R;
 import com.saikali.android_skwissh.charts.SensorGraphViewBuilder;
 import com.saikali.android_skwissh.objects.SkwisshSensorItem;
 import com.saikali.android_skwissh.objects.SkwisshServerContent.SkwisshServerItem;
 
 public class SensorsAdapter extends BaseExpandableListAdapter {
 
 	public Context context;
 	private List<SkwisshSensorItem> sensorsItems = new ArrayList<SkwisshSensorItem>();
 	private LayoutInflater inflater;
 	private Typeface tf;
 	private SkwisshServerItem server;
 
 	public SensorsAdapter(Context context, SkwisshServerItem server) {
 		this.context = context;
 		this.inflater = LayoutInflater.from(context);
 		this.tf = Typeface.createFromAsset(context.getAssets(), "fonts/Oxygen.otf");
 		this.server = server;
 	}
 
 	public SkwisshServerItem getServer() {
 		return this.server;
 	}
 
 	@Override
 	public boolean areAllItemsEnabled() {
 		return true;
 	}
 
 	@Override
 	public Object getChild(int gPosition, int cPosition) {
 		return this.sensorsItems.get(gPosition);
 	}
 
 	@Override
 	public long getChildId(int gPosition, int cPosition) {
 		return gPosition;
 	}
 
 	@Override
 	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
 		final SkwisshSensorItem sensor = (SkwisshSensorItem) this.getChild(groupPosition, childPosition);
 		ChildViewHolder childViewHolder;
 
 		if (convertView == null) {
 			childViewHolder = new ChildViewHolder();
 			convertView = this.inflater.inflate(R.layout.activity_serverdetail_mesure_item, null);
 			childViewHolder.chartLayout = (LinearLayout) convertView.findViewById(R.id.chartLayout);
 			convertView.setTag(childViewHolder);
 		} else {
 			childViewHolder = (ChildViewHolder) convertView.getTag();
 		}
 
 		View graphView = new SensorGraphViewBuilder(this.context, sensor).createGraphView();
 		childViewHolder.chartLayout.removeAllViews();
 		childViewHolder.chartLayout.addView(graphView, parent.getMeasuredWidth(), parent.getMeasuredHeight() / 2);
 
 		return convertView;
 	}
 
 	@Override
 	public int getChildrenCount(int gPosition) {
 		return 1;
 	}
 
 	@Override
 	public Object getGroup(int gPosition) {
 		return this.sensorsItems.get(gPosition);
 	}
 
 	@Override
 	public int getGroupCount() {
 		return this.sensorsItems.size();
 	}
 
 	@Override
 	public long getGroupId(int gPosition) {
 		return gPosition;
 	}
 
 	public void updateEntries() {
 		this.sensorsItems = this.server.getSensors();
 		this.notifyDataSetChanged();
 	}
 
 	@Override
 	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
 		final SkwisshSensorItem sensor = (SkwisshSensorItem) this.getGroup(groupPosition);
 		GroupViewHolder gholder;
 
 		if (convertView == null) {
 			gholder = new GroupViewHolder();
 			convertView = this.inflater.inflate(R.layout.activity_serverdetail_sensor_item, null);
 			gholder.sensorName = (TextView) convertView.findViewById(R.id.sensorName);
 			convertView.setTag(gholder);
 		} else {
 			gholder = (GroupViewHolder) convertView.getTag();
 		}
 		gholder.sensorName.setText(sensor.getDisplayName() + " on " + this.server.getHostname());
 		gholder.sensorName.setTypeface(this.tf);
 		return convertView;
 	}
 
 	@Override
 	public boolean hasStableIds() {
 		return true;
 	}
 
 	@Override
 	public boolean isChildSelectable(int groupPosition, int childPosition) {
 		return true;
 	}
 
 	class GroupViewHolder {
 		public TextView sensorName;
 	}
 
 	class ChildViewHolder {
 		public LinearLayout chartLayout;
 	}
 
 }
