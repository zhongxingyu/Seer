 package com.bixito;
 
 import java.io.IOException;
 import java.util.Date;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.bixito.station.BikeStation;
 
 public class StationDetailFragment extends DialogFragment {
 
 	BikeStation bikeStation = null;
 	TextView stationPopTextView = null;
 	TextView stationNameTextView = null;
 	TextView stationLastUpdatedTextView = null;
 	TextView stationIsPublicTextView = null;
 	TextView stationIsLockedTextView = null;
 	TextView stationIsTempTextView = null;
 	Button getDirectionsButton = null;
 	Button dismissDialogButton = null;
 	ImageView rankImage = null;
 	int stationRank = -1;
 	int stationRankChange = 0;
 	TextView loadingLabel = null;
 	
 	static StationDetailFragment newInstance(BikeStation station){
 		StationDetailFragment f = new StationDetailFragment();
 		
 		//Put the bikeStation onto the args (using a parcel)
 		Bundle args = new Bundle();
 		args.putParcelable("bikeStation", station);
 		
 		f.setArguments(args);
 		
 		return f;
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		bikeStation = getArguments().getParcelable("bikeStation");		
 		
 	}
 	
 	/**
 	 * Loads the selected station's popularity by using a json call to the ruby database
 	 * @author andrew
 	 *
 	 */
 	private class LoadStationPopularityTask extends AsyncTask<String, Void, String> {
 		String jsonData = "";
 		
 		@Override
 		protected String doInBackground(String... urls) {
 			try{
 				HttpClient httpClient = new DefaultHttpClient();
 				HttpGet httpGet = new HttpGet(urls[0]);
 				HttpResponse httpResponse = httpClient.execute(httpGet);
 				HttpEntity entity = httpResponse.getEntity();
 				
 				if(entity != null){
 					jsonData = EntityUtils.toString(entity);
 				}
 			}
 			catch (ClientProtocolException e){
 				Log.e("ERROR", e.getMessage());
 				stationPopTextView.setText(getString(R.string.station_pop_error));
 			}
 			catch (IOException e){
 				Log.e("ERROR", e.getMessage());
 				stationPopTextView.setText(getString(R.string.station_pop_error));
 			}
 			
 			return jsonData;
 		}
 		
 		@Override
 		protected void onPostExecute(String result){
 			try{
 				//Parse the json data
 				JSONObject jsonObject = new JSONObject(result);
 
 				stationRank = jsonObject.getInt("rank");
 				stationRankChange = jsonObject.getInt("upordown");

				stationPopTextView.setText(getString(R.string.station_pop_low));
				stationPopTextView.setText(" " + stationRank );
				if(stationRankChange > 0){
					rankImage.setImageDrawable(getView().getResources().getDrawable(R.drawable.up));
 					rankImage.setVisibility(rankImage.VISIBLE);
 					loadingLabel.setVisibility(loadingLabel.INVISIBLE);
 				}
				else if(stationRankChange < 0){
					rankImage.setImageDrawable(getView().getResources().getDrawable(R.drawable.down));
					rankImage.setVisibility(rankImage.VISIBLE);
					loadingLabel.setVisibility(loadingLabel.INVISIBLE);
				}
				else{
					//Rank didn't change
					rankImage.setVisibility(rankImage.INVISIBLE);
					loadingLabel.setVisibility(loadingLabel.INVISIBLE);
				}
				stationPopTextView.setVisibility(stationPopTextView.VISIBLE);
 			}
 			catch(JSONException e){
 				Log.e("ERROR", e.getMessage());
 				stationPopTextView.setText(getString(R.string.station_pop_error));
 
 			}
 		}
 		
 		
 	}
 	
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.station_details, container, false);
         getDialog().setTitle(getString(R.string.station_details_title));
         
         
 		stationNameTextView = (TextView) view.findViewById(R.id.stationNameTextView);
 		stationIsPublicTextView = (TextView) view.findViewById(R.id.stationIsPublicTextView);
 		stationIsLockedTextView = (TextView) view.findViewById(R.id.stationIsLockedTextView);
 		stationIsTempTextView = (TextView) view.findViewById(R.id.stationIsTempTextView);
 		stationLastUpdatedTextView = (TextView) view.findViewById(R.id.stationLastUpdatedTextView);
 		stationPopTextView = (TextView) view.findViewById(R.id.stationPopTextView);
 		getDirectionsButton = (Button) view.findViewById(R.id.getDirectionsButton);
 		dismissDialogButton = (Button) view.findViewById(R.id.dismissDialogButton);
 		rankImage = (ImageView) view.findViewById(R.id.rankImage);
 		loadingLabel = (TextView) view.findViewById(R.id.loadingLabel);
 		
 		getDirectionsButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				String intentUrl = "http://maps.google.com/maps?daddr=";
 				intentUrl += bikeStation.getLatitude() + "," + bikeStation.getLongitude();
 				Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
 					    Uri.parse(intentUrl));
 					startActivity(intent);
 			}
 			
 		});
 		
 		
 		dismissDialogButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				getDialog().dismiss();
 			}
 		});
 		
 		stationNameTextView.setText(bikeStation.getStationName());
 		
 		String publicStation;
 		if(bikeStation.isPublicStation())
 			publicStation = getString(R.string.station_is_public_true);
 		else
 			publicStation = getString(R.string.station_is_public_false);
 		stationIsPublicTextView.setText(publicStation);
 		
 		String lockedStation;
 		if(bikeStation.isLocked())
 			lockedStation = getString(R.string.station_is_locked_true);
 		else
 			lockedStation = getString(R.string.station_is_locked_false);
 		stationIsLockedTextView.setText(" " + lockedStation);
 		
 		String tempStation;
 		if(bikeStation.isTemporary())
 			tempStation = getString(R.string.station_is_temp_true);
 		else
 			tempStation = getString(R.string.station_is_temp_false);
 		stationIsTempTextView.setText(" " + tempStation);
 		
 		Date time=new java.util.Date((long) bikeStation.getLatestUpdateTime());
 		stationLastUpdatedTextView.setText(getString(R.string.station_last_update_label) + " " + time.toString());
 		
 		
 		String stationPopUrl = getString(R.string.station_pop_json_url).replace("STATION_ID", Integer.toString(bikeStation.getStationId()));
 		new LoadStationPopularityTask().execute(new String[] { stationPopUrl });
         
 		rankImage.setVisibility(rankImage.INVISIBLE);
 		
         return view;
     }   
 
 }
