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
 
 import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;
 import android.app.Activity;
 import android.content.Context;
 import android.location.Location;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import com.google.android.maps.GeoPoint;
 
 import eu.trentorise.smartcampus.jp.R;
 
 public class SmartCheckParkingsAdapter extends ArrayAdapter<Parking> {
 
 	private Context mContext;
 	private int layoutResourceId;
 	private Location myLocation;
 
 	public SmartCheckParkingsAdapter(Context context, int layoutResourceId) {
 		super(context, layoutResourceId);
 		this.mContext = context;
 		this.layoutResourceId = layoutResourceId;
 	}
 
 	public void setMyLocation(Location location) {
 		this.myLocation = location;
 	}
 
 	public void setMyLocation(GeoPoint geoPoint) {
		Location location = new Location("");
		location.setLatitude(geoPoint.getLatitudeE6() / 1e6);
		location.setLongitude(geoPoint.getLongitudeE6() / 1e6);
		this.myLocation = location;
	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 
 		Parking parking = getItem(position);
 
 		View row = convertView;
 		ParkingHolder holder = null;
 
 		if (row == null) {
 			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
 			row = inflater.inflate(layoutResourceId, parent, false);
 
 			holder = new ParkingHolder();
 			holder.parkingName = (TextView) row.findViewById(R.id.smart_check_parking_name);
 			holder.parkingData = (TextView) row.findViewById(R.id.smart_check_parking_data);
 			holder.parkingStatus = (TextView) row.findViewById(R.id.smart_check_parking_status);
 
 			row.setTag(holder);
 		} else {
 			holder = (ParkingHolder) row.getTag();
 		}
 
 		// name
 		holder.parkingName.setText(parking.getName());
 
 		// description
 		// TODO: distance from my position
 		String desc = "";
 		if (!parking.getName().equalsIgnoreCase(parking.getDescription())) {
 			desc += parking.getDescription();
 		}
 
 		if (myLocation != null) {
 			Location parkingLocation = new Location("");
 			parkingLocation.setLatitude(parking.getPosition()[0]);
 			parkingLocation.setLongitude(parking.getPosition()[1]);
 			float distance = myLocation.distanceTo(parkingLocation);
 
 			if (desc.length() != 0) {
 				desc += " (";
 				desc += String.format("%.2f", distance / 1000) + " km";
 				desc += ")";
 			} else {
 				desc += String.format("%.2f", distance / 1000) + " km";
 			}
 		}
 
 		holder.parkingData.setText(desc);
 
 		// status
 		holder.parkingStatus.setText(mContext.getString(R.string.smart_check_parking_avail, parking.getSlotsAvailable(),
 				parking.getSlotsTotal()));
 
 		if (parking.getSlotsAvailable() > 20) {
 			holder.parkingStatus.setTextColor(mContext.getResources().getColor(R.color.parking_green));
 		} else if (parking.getSlotsAvailable() <= 20 && parking.getSlotsAvailable() > 5) {
 			holder.parkingStatus.setTextColor(mContext.getResources().getColor(R.color.parking_orange));
 		} else if (parking.getSlotsAvailable() <= 5) {
 			holder.parkingStatus.setTextColor(mContext.getResources().getColor(R.color.red));
 			if (parking.getSlotsAvailable() == 0) {
 				holder.parkingStatus.setText(mContext.getString(R.string.smart_check_parking_full));
 			} else if (parking.getSlotsAvailable() == -1) {
 				// data unavailable
 				holder.parkingStatus.setText(mContext.getString(R.string.smart_check_parking_avail, "?",
 						parking.getSlotsTotal()));
 			} else if (parking.getSlotsAvailable() == -2) {
 				// data not monitored
 				holder.parkingStatus.setTextColor(mContext.getResources().getColor(R.color.blue));
 				holder.parkingStatus.setText(Integer.toString(parking.getSlotsTotal()));
 			}
 		}
 
 		return row;
 	}
 
 	public static View buildParking(Context mContext, int layoutResourceId, Location myLocation, Parking parking,
 			View convertView, ViewGroup parent) {
 		
 		View row = convertView;
 		ParkingHolder holder = null;
 
 		if (row == null) {
 			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
 			row = inflater.inflate(layoutResourceId, parent, false);
 
 			holder = new ParkingHolder();
 			holder.parkingName = (TextView) row.findViewById(R.id.smart_check_parking_name);
 			holder.parkingData = (TextView) row.findViewById(R.id.smart_check_parking_data);
 			holder.parkingStatus = (TextView) row.findViewById(R.id.smart_check_parking_status);
 
 			row.setTag(holder);
 		} else {
 			holder = (ParkingHolder) row.getTag();
 		}
 
 		// name
 		holder.parkingName.setText(parking.getName());
 
 		// description
 		// TODO: distance from my position
 		String desc = "";
 		if (!parking.getName().equalsIgnoreCase(parking.getDescription())) {
 			desc += parking.getDescription();
 		}
 
 		if (myLocation != null) {
 			Location parkingLocation = new Location("");
 			parkingLocation.setLatitude(parking.getPosition()[0]);
 			parkingLocation.setLongitude(parking.getPosition()[1]);
 			float distance = myLocation.distanceTo(parkingLocation);
 
 			if (desc.length() != 0) {
 				desc += " (";
 				desc += String.format("%.2f", distance / 1000) + " km";
 				desc += ")";
 			} else {
 				desc += String.format("%.2f", distance / 1000) + " km";
 			}
 		}
 
 		holder.parkingData.setText(desc);
 
 		// status
 		holder.parkingStatus.setText(mContext.getString(R.string.smart_check_parking_avail, parking.getSlotsAvailable(),
 				parking.getSlotsTotal()));
 
 		if (parking.getSlotsAvailable() > 20) {
 			holder.parkingStatus.setTextColor(mContext.getResources().getColor(R.color.parking_green));
 		} else if (parking.getSlotsAvailable() <= 20 && parking.getSlotsAvailable() > 5) {
 			holder.parkingStatus.setTextColor(mContext.getResources().getColor(R.color.parking_orange));
 		} else if (parking.getSlotsAvailable() <= 5) {
 			holder.parkingStatus.setTextColor(mContext.getResources().getColor(R.color.red));
 			if (parking.getSlotsAvailable() == 0) {
 				holder.parkingStatus.setText(mContext.getString(R.string.smart_check_parking_full));
 			} else if (parking.getSlotsAvailable() == -1) {
 				// data unavailable
 				holder.parkingStatus.setText(mContext.getString(R.string.smart_check_parking_avail, "?",
 						parking.getSlotsTotal()));
 			} else if (parking.getSlotsAvailable() == -2) {
 				// data not monitored
 				holder.parkingStatus.setTextColor(mContext.getResources().getColor(R.color.blue));
 				holder.parkingStatus.setText(Integer.toString(parking.getSlotsTotal()));
 			}
 		}
 
 		return row;
 	}
 
 	public static class ParkingHolder {
 		TextView parkingName;
 		TextView parkingData;
 		TextView parkingStatus;
 	}
 }
