 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.jp.custom;
 
 import it.sayservice.platform.smartplanner.data.message.alerts.CreatorType;
 
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.Map;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 import eu.trentorise.smartcampus.jp.Config;
 import eu.trentorise.smartcampus.jp.R;
import eu.trentorise.smartcampus.jp.helper.RoutesHelper;
import eu.trentorise.smartcampus.jp.model.RouteDescriptor;
 import eu.trentorise.smartcampus.jp.model.TripData;
 
 public class SmartCheckRoutesListAdapter extends ArrayAdapter<TripData> {
 
 	Context mContext;
 	int layoutResourceId;
 
 	String[] lines;
 	TypedArray icons;
 	TypedArray colors;
 
 	public SmartCheckRoutesListAdapter(Context context, int layoutResourceId) {
 		super(context, layoutResourceId);
 		this.mContext = context;
 		this.layoutResourceId = layoutResourceId;
 
 		lines = mContext.getResources().getStringArray(R.array.smart_checks_bus_number);
 		icons = mContext.getResources().obtainTypedArray(R.array.smart_checks_bus_icons);
 		colors = mContext.getResources().obtainTypedArray(R.array.smart_checks_bus_color);
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View row = convertView;
 
 		final TripData tripData = getItem(position);
 
 		RowHolder holder = null;
 		if (row == null) {
 			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
 			row = inflater.inflate(layoutResourceId, parent, false);
 
 			holder = new RowHolder();
 			holder.route = (TextView) row.findViewById(R.id.smartcheck_trip_route);
 			holder.time = (TextView) row.findViewById(R.id.smartcheck_trip_time);
 			holder.delaySystem = (TextView) row.findViewById(R.id.smartcheck_trip_delay_system);
 			holder.delayUser = (TextView) row.findViewById(R.id.smartcheck_trip_delay_user);
 			row.setTag(holder);
 		} else {
 			holder = (RowHolder) row.getTag();
 		}
 
 		// separator
 		if (position == 0 || !(getItem(position - 1).getRouteId()).equalsIgnoreCase(tripData.getRouteId())) {
 			holder.route.setBackgroundColor(mContext.getResources().getColor(R.color.sc_gray));
 			for (int i = 0; i < lines.length; i++) {
 				if (tripData.getRouteId().startsWith(lines[i])) {
 					holder.route.setBackgroundColor(colors.getColor(i, 0));
 					break;
 				}
 			}
 
			RouteDescriptor rd = RoutesHelper.getRouteDescriptorByRouteId(tripData.getRouteId());
			holder.route.setText(rd.getShortNameResource() + " - " + mContext.getResources().getString(rd.getNameResource()));

			rd.getNameResource();
 			holder.route.setVisibility(View.VISIBLE);
 		} else {
 			holder.route.setVisibility(View.GONE);
 		}
 
 		// time
 		Calendar time = Calendar.getInstance();
 		time.setTimeInMillis(tripData.getTime() * 1000);
 		String timeFromString = Config.FORMAT_TIME_UI.format(time.getTime());
 		holder.time.setText(timeFromString);
 
 		// delay
 		/*
 		 * TODO: TEST
 		 */
 		// if (tripData.getDelays() == null) {
 		// Map<CreatorType, String> testDelays = new HashMap<CreatorType,
 		// String>();
 		// if (position == 0) {
 		// testDelays.put(CreatorType.USER, "2");
 		// testDelays.put(CreatorType.SERVICE, "1");
 		// } else if (position == 1) {
 		// testDelays.put(CreatorType.USER, "2");
 		// } else if (position == 2) {
 		// testDelays.put(CreatorType.SERVICE, "1");
 		// }
 		//
 		// tripData.setDelays(testDelays);
 		// }
 		/*
 		 * 
 		 */
 
 		Map<CreatorType, String> delays = tripData.getDelays();
 		if (delays != null) {
 			if (delays.get(CreatorType.USER) != null) {
 				holder.delayUser
 						.setText(mContext.getString(R.string.smart_check_stops_delay_user, delays.get(CreatorType.USER)));
 				holder.delayUser.setVisibility(View.VISIBLE);
 			} else {
 				holder.delayUser.setVisibility(View.GONE);
 			}
 
 			if (delays.get(CreatorType.SERVICE) != null) {
 				holder.delaySystem
 						.setText(mContext.getString(R.string.smart_check_stops_delay, delays.get(CreatorType.SERVICE)));
 				holder.delaySystem.setVisibility(View.VISIBLE);
 			} else if (delays.get(CreatorType.DEFAULT) != null) {
 				holder.delaySystem
 						.setText(mContext.getString(R.string.smart_check_stops_delay, delays.get(CreatorType.DEFAULT)));
 				holder.delaySystem.setVisibility(View.VISIBLE);
 			} else {
 				holder.delaySystem.setVisibility(View.GONE);
 			}
 
 			if (holder.delaySystem.getVisibility() == View.VISIBLE && holder.delayUser.getVisibility() == View.VISIBLE) {
 				holder.delaySystem.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
 				holder.delayUser.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
 			} else {
 				holder.delaySystem.setTextAppearance(mContext, android.R.style.TextAppearance_Medium);
 				holder.delayUser.setTextAppearance(mContext, android.R.style.TextAppearance_Medium);
 			}
 
 			holder.delaySystem.setTextColor(mContext.getResources().getColor(R.color.red));
 			holder.delayUser.setTextColor(mContext.getResources().getColor(R.color.blue));
 
 			LinearLayout delaysLinearLayout = (LinearLayout) row.findViewById(R.id.smartcheck_trip_delays);
 			delaysLinearLayout.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					Map<CreatorType, String> delays = tripData.getDelays();
 					if (delays != null && !delays.isEmpty()) {
 						DelaysDialogFragment delaysDialog = new DelaysDialogFragment();
 						Bundle args = new Bundle();
 						args.putSerializable(DelaysDialogFragment.ARG_DELAYS, (Serializable) delays);
 						delaysDialog.setArguments(args);
 						delaysDialog.show(((SherlockFragmentActivity) mContext).getSupportFragmentManager(), "delaysdialog");
 					}
 				}
 			});
 		} else {
 			holder.delayUser.setVisibility(View.GONE);
 			holder.delaySystem.setVisibility(View.GONE);
 		}
 
 		return row;
 	}
 
 	static class RowHolder {
 		TextView route;
 		TextView time;
 		TextView delaySystem;
 		TextView delayUser;
 	}
 
 }
