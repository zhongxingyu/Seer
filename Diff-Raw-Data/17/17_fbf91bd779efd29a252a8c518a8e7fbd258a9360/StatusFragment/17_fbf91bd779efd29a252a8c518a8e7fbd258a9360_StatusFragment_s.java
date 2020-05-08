 package com.example.airportstatus.fragments;
 
 
 
 import java.util.ArrayList;
 
 import android.annotation.SuppressLint;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.text.Html;
 import android.text.method.LinkMovementMethod;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.example.airportstatus.Airport;
 import com.example.airportstatus.R;
 import com.example.airportstatus.StatusKeys;
 import com.example.airportstatus.models.Favorite;
 
 
 public class StatusFragment extends Fragment {
 	
 	
 	TextView delays;
 	TextView weather;
 	String code;
 	Integer airportIndex;
 	Bundle intentData;
 	ImageView favoriteStatus;
 	boolean isFavorited;
 	Button btnDrivingTime;
 	Button btnTransitTime;
 	Button btnDelays;
 	
 	@Override
 	public View onCreateView(LayoutInflater inf, ViewGroup parent, Bundle savedInstanceState) {
 		return inf.inflate(R.layout.fragment_status, parent, false);
 	}
 	
 	@SuppressLint("DefaultLocale")
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		intentData = getActivity().getIntent().getBundleExtra("data");
 		code = intentData.getString("airport_code").toUpperCase();
 		airportIndex = Integer.parseInt(intentData.getString("airport_index"));
 		setupViews();
 		setTemplateData(getActivity().getIntent());
 	}
 	
 	@SuppressLint({ "InlinedApi", "ResourceAsColor" })
 	private void setupViews() {
 		btnDrivingTime = (Button) getActivity().findViewById(R.id.btnDrivingTime);
 		btnTransitTime = (Button) getActivity().findViewById(R.id.btnTransitTime);
 		btnDelays = (Button) getActivity().findViewById(R.id.btnDelays);
		btnDrivingTime.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_bright));
		btnTransitTime.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_bright));
 		btnDelays.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
 		String airportName = new ArrayList<String>(Airport.IATA_CODES.keySet()).get(airportIndex);
 		((TextView)getActivity().findViewById(R.id.tvAirportName))
 		  .setText(airportName);
 		((TextView)getActivity().findViewById(R.id.tvBigAirportCode)).setText(code);
 		String website = Airport.WEBSITES.get(airportIndex);
 		String formattedWebsite = "<a href='http://"+website+"'>"+website+"</a>";
 		TextView tvWebsite = ((TextView)getActivity().findViewById(R.id.tvWebsite));
 		tvWebsite.setText(Html.fromHtml(formattedWebsite));
 		tvWebsite.setMovementMethod(LinkMovementMethod.getInstance());
 		weather = (TextView) getActivity().findViewById(R.id.tvWeather);
 		delays = (TextView) getActivity().findViewById(R.id.tvDelays);
 		favoriteStatus = (ImageView) getActivity().findViewById(R.id.ivFavorite);
  	}
 	
 	private void setTemplateData(Intent intent) {
 		try {
 			Bundle data = intent.getBundleExtra("data");
 			btnDrivingTime.setText("Driving Directions: " + data.getString(StatusKeys.TRAVEL_TIME_DRIVING));
 			btnTransitTime.setText("Transit Directions: " + data.getString(StatusKeys.TRAVEL_TIME_TRANSIT));
 			//delays.setText(data.getString(StatusKeys.DELAYS));
 			btnDelays.setText("Status: " + data.getString(StatusKeys.DELAYS));
 			weather.setText(data.getString(StatusKeys.WEATHER));
 			
 			setFavoritedStatus();
 		} catch (Exception e) {
 			Log.e("INVALID_INTENT_EXTRA", e.getMessage());
 		}
 	}
 	
 	private void setFavoritedStatus() {
 		this.isFavorited = Favorite.exists(code);
 		if (this.isFavorited) {
 			favoriteStatus.setImageResource(R.drawable.ic_star_filled);
 		} else {
 			favoriteStatus.setImageResource(R.drawable.ic_star_empty);
 		}
 	}
 	
 	public void onFavoriteAction(View v) {
 		if (this.isFavorited == true) {
 			Favorite.delete(code);
 			// Favorite exists; delete it
 		} else {
 			// Set item as favorite
 			Favorite newFavorite = new Favorite();
 			newFavorite.setAirportCode(code);
 			newFavorite.save();
 		}
 		this.setFavoritedStatus();
 	} 
 }
