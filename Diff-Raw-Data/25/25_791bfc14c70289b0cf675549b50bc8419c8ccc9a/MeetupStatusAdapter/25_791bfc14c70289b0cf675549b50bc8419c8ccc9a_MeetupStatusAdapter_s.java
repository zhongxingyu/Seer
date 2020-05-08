 package com.codepath.apps.locateme;
 
 import android.content.Context;
 import android.location.Location;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.codepath.apps.locateme.models.User;
 import com.codepath.apps.locateme.models.User.TransportMode;
 
 public class MeetupStatusAdapter extends ArrayAdapter<User> {
 
 	public MeetupStatusAdapter(Context context) {
 		super(context, 0);
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		User user = getItem(position);
 		View view = convertView;
 		if (view == null) {
 			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			view = inflater.inflate(R.layout.meetup_status_item, null);
 		}
		
 		TextView tvFriendName = (TextView) view.findViewById(R.id.tvFriendName);
 		ImageView ivTransportMode = (ImageView) view.findViewById(R.id.ivTransportMode);
 		TextView tvEtaStatus = (TextView) view.findViewById(R.id.tvEtaStatus);
		
		tvFriendName.setText(user.getName());
		
		int resId = getTransportModeDrawable(user.getCurrentTransitMode());
 		ivTransportMode.setImageResource(resId);
		
		String eta = getEta(user.getCurrentLocation(), user.getCurrentTransitMode());
 		tvEtaStatus.setText(eta);
		
 		return view;
 	}
 
 	private String getEta(Location currentLocation, TransportMode currentTransportMode) {
 		// To be filled in
 		return "1 hour";
 	}
	
 	private int getTransportModeDrawable(TransportMode currentTransportMode) {
 		// To be filled in
 		return R.drawable.car;
 	}
	
 }
