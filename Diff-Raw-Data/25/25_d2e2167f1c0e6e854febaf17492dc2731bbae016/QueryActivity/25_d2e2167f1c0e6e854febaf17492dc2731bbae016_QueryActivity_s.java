 package com.frontcast;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.drawable.Drawable;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.Toast;
 
 import com.frontcast.model.Frontcast;
 import com.frontcast.model.FrontcastList;
 import com.frontcast.model.Person;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.MapActivity;
 import com.google.android.maps.MapController;
 import com.google.android.maps.MapView;
 import com.google.android.maps.MyLocationOverlay;
 import com.google.android.maps.OverlayItem;
 import com.google.api.client.http.GenericUrl;
 import com.google.api.client.http.HttpRequest;
 import com.google.api.client.http.HttpRequestFactory;
 import com.google.api.client.http.HttpRequestInitializer;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.http.apache.ApacheHttpTransport;
 import com.google.api.client.http.json.JsonHttpContent;
 import com.google.api.client.http.json.JsonHttpParser;
 import com.google.api.client.json.jackson.JacksonFactory;
 
 
 
 public class QueryActivity extends MapActivity {
 	
 	private static final String SERVER_URL = "http://frontcast-server.appspot.com/rpc";
 	private static final HttpTransport transport = new ApacheHttpTransport();
 	
 	private AutoCompleteTextView townsName;
 	private Button queryButton;
 	
 	private MapController mapController;
 	private MapView mapView;
 	private LocationManager locationManager;
 	private MyLocationOverlay myLocationOverlay;
 	private FrontcastList frontcastlist;
 	private FrontcastOverlay sunnyOverlay;
 	private FrontcastOverlay cloudyOverlay;
 	private FrontcastOverlay rainyOverlay;
 	private static final GeoPoint STATION_TAIPEI   = new GeoPoint((int) (25.04192 * 1E6) , (int) (121.516981 * 1E6));
	/*private static final String[] sunnyLevelTable  = {"�p�Ӷ���","����M��","�P����","���p�B�U"};
	private static final String[] cloudyLevelTable  = {"�L������","�Ӷ����Y","�צ�Ӷ�","�Q���K�G"};
	private static final String[] rainyLevelTable  = {"�B�p���","���ӫB","�j�B���b","�g���ɫB"};*/
 	
 	@Override
     protected boolean isRouteDisplayed() {
         return false;
     }
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.query);
 		findViews();
 		setListeners();
 		townNameAutoCompelete();
 		configureMap();
 		configureOverlays();
 
 	}
 	private void configureMap() {
 		mapView = (MapView) findViewById(R.id.mapview);
         mapView.setBuiltInZoomControls(true);
         mapView.setSatellite(false);
         mapView.setTraffic(true);
 		mapController = mapView.getController();
 		mapController.setZoom(14); // Zoon 1 is world view
 		mapController.animateTo(STATION_TAIPEI);
 		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		
 		myLocationOverlay = new MyLocationOverlay(this, mapView);
 		mapView.getOverlays().add(myLocationOverlay);
 		myLocationOverlay.runOnFirstFix(new Runnable() {
 			public void run() {
 				mapView.getController().animateTo(myLocationOverlay.getMyLocation());
 			}
 		});
 	}
 	
 	private void configureOverlays() {
 		Drawable sun = getResources().getDrawable(R.drawable.sunny);
 		Log.d("sunny_layer", "sun");
 		sun.setBounds(0, 0, sun.getMinimumWidth(), sun.getMinimumHeight());
 		sunnyOverlay = new FrontcastOverlay(sun);
 		mapView.getOverlays().add(sunnyOverlay);
 		
 		Drawable cloud = getResources().getDrawable(R.drawable.cloudy);
 		Log.d("cloudy_layer", "cloud");
 		cloud.setBounds(0, 0, cloud.getMinimumWidth(), cloud.getMinimumHeight());
 		cloudyOverlay = new FrontcastOverlay(cloud);
 		mapView.getOverlays().add(cloudyOverlay);
 		
 		Drawable rain = getResources().getDrawable(R.drawable.rainy);
 		Log.d("rainy_layer", "rain");
 		rain.setBounds(0, 0, rain.getMinimumWidth(), rain.getMinimumHeight());
 		rainyOverlay = new FrontcastOverlay(rain);
 		mapView.getOverlays().add(rainyOverlay);
 	}
 	
 	private void setFrontcasts() {
 		for (Frontcast frontcast : frontcastlist.results) {
 			Log.d("frontcast_type", frontcast.get("type").toString());
 			GeoPoint loc = new GeoPoint((int) (frontcast.latitude * 1E6), (int) (frontcast.longitude * 1E6));
 			mapController.animateTo(loc);
 			int progress = frontcast.level;
 			String weatherLevel;
 			if(frontcast.get("type").toString()=="rainy") {
 				if (progress < 25) {
 					weatherLevel = new String(getString(R.string.rainy_1));
 				} else if (progress < 50) {
 					weatherLevel = new String(getString(R.string.rainy_2));
 				} else if (progress < 75) {
 					weatherLevel = new String(getString(R.string.rainy_3));
 				} else {
 					weatherLevel = new String(getString(R.string.rainy_4));
 				}
 				rainyOverlay.addOverlay(new OverlayItem(loc, "Rainy", weatherLevel));
     		}
 			else if(frontcast.get("type").toString()=="cloudy") {
 				if (progress < 25) {
 					weatherLevel = new String(getString(R.string.cloudy_1));
 				} else if (progress < 50) {
 					weatherLevel = new String(getString(R.string.cloudy_2));
 				} else if (progress < 75) {
 					weatherLevel = new String(getString(R.string.cloudy_3));
 				} else {
 					weatherLevel = new String(getString(R.string.cloudy_4));
 				}
 				cloudyOverlay.addOverlay(new OverlayItem(loc, "Cloudy", weatherLevel));
     		}
 			else if(frontcast.get("type").toString()=="sunny") {
 				if (progress < 25) {
 					weatherLevel = new String(getString(R.string.sunny_1));
 				} else if (progress < 50) {
 					weatherLevel = new String(getString(R.string.sunny_2));
 				} else if (progress < 75) {
 					weatherLevel = new String(getString(R.string.sunny_3));
 				} else {
 					weatherLevel = new String(getString(R.string.sunny_4));
 				}
 				sunnyOverlay.addOverlay(new OverlayItem(loc, "Sunny", weatherLevel));
     		}
 		}
 	}
 	
 	private void setListeners() {
 		queryButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (townsName.getText().toString().isEmpty()){
 					AlertDialog.Builder builder = new AlertDialog.Builder(QueryActivity.this);
 					builder.setMessage("You have to type in something!")
 					       .setCancelable(false)
 					       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
 					           public void onClick(DialogInterface dialog, int id) {
 					        	   dialog.cancel();
 					           }
 					       });
 					AlertDialog alert = builder.create();
 					alert.show();
 				}
 				else {
 					Log.d("Send towns name", townsName.getText().toString());
 					new GetFrontcastsTask().execute();
 				}
 			}
     	});
 	}
 	
 	private void findViews() {
 		townsName = (AutoCompleteTextView) findViewById(R.id.location_input);
 		queryButton = (Button) findViewById(R.id.location_query_button);
 	}
 
 	
 	private void townNameAutoCompelete() {
 		String[] towns = getResources().getStringArray(R.array.towns_array);
 		ArrayAdapter<String> adapter = 
 		        new ArrayAdapter<String>(this, R.layout.list_item, towns);
 		townsName.setAdapter(adapter);
 		townsName.setThreshold(1);
 	}
 	
 	private class GetFrontcastsTask extends AsyncTask<Void, Void, Void> {
     	private ProgressDialog Dialog = new ProgressDialog(QueryActivity.this);
     	String locationName;
     	
     	@Override
     	protected void onPreExecute() {
     		super.onPreExecute();
     		Dialog.setMessage("Sending your query...");
     		Dialog.show();
     		
     		locationName = townsName.getText().toString();
     	}
     	
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			String[] data = {"GetTestJson"};
 			JsonHttpContent json = new JsonHttpContent(new JacksonFactory(), data);
 			
 			HttpRequestFactory httpRequestFactory = createRequestFactory(transport);
 			HttpRequest request;
 			try {
 				request = httpRequestFactory.buildPostRequest(new GenericUrl(SERVER_URL), json);
 
 				String result = request.execute().parseAsString();
 				Log.d("query_weather", result);
 				//frontcastlist = request.execute().parseAs(FrontcastList.class);
 				//setFrontcasts();
 				//Person person = request.execute().parseAs(Person.class);
 				//Log.d("query_result", person.get("name").toString());
 				return null;
 			} catch (IOException e) {
 				
 				e.printStackTrace();
 			}
 			return null;
 		}
     	
 		@Override
 		protected void onPostExecute(Void unused) {
 			Dialog.dismiss();
 			Toast.makeText(QueryActivity.this, "done!", Toast.LENGTH_SHORT).show();
 		}
     }
 	
 	public class FrontcastOverlay extends ItemizedOverlay<OverlayItem> {
 		
 		private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
 		
 		public FrontcastOverlay(Drawable defaultMarker) {
 			  super(boundCenterBottom(defaultMarker));
 		}
 
 		@Override
 		protected OverlayItem createItem(int i) {
 		  return mOverlays.get(i);
 		}
 
 		@Override
 		public int size() {
 		  return mOverlays.size();
 		}
 		
 		public void addOverlay(OverlayItem overlay) {
 		    mOverlays.add(overlay);
 		    populate();
 		}
 		
 		@Override
 		protected boolean onTap(int index) {
 		  OverlayItem item = mOverlays.get(index);
 		  Toast.makeText(QueryActivity.this, item.getTitle() + "\n" + 
 				                             item.getSnippet() 
 				                             , Toast.LENGTH_LONG).show();
 		  
 		  return true;
 		}
 
 	}
 	
 	public static HttpRequestFactory createRequestFactory(final HttpTransport transport) {
 		   
 		return transport.createRequestFactory(new HttpRequestInitializer() {
 			public void initialize(HttpRequest request) {
 			    JsonHttpParser parser = new JsonHttpParser(new JacksonFactory());
 				request.addParser(parser);
 			}
 		});
 	}
 	
 }
