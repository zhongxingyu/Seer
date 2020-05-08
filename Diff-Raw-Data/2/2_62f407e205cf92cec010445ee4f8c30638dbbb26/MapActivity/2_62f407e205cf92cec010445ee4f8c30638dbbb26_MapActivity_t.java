 package sn.gotech.trafficjammeu;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.graphics.Color;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.maps.CameraUpdateFactory;
 import com.google.android.gms.maps.GoogleMap;
 import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
 import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
 import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
 import com.google.android.gms.maps.UiSettings;
 import com.google.android.gms.maps.model.BitmapDescriptorFactory;
 import com.google.android.gms.maps.model.CameraPosition;
 import com.google.android.gms.maps.model.LatLng;
 import com.google.android.gms.maps.model.Marker;
 import com.google.android.gms.maps.model.MarkerOptions;
 import com.google.android.gms.maps.model.Polyline;
 import com.google.android.gms.maps.model.PolylineOptions;
 
 public class MapActivity extends SherlockFragmentActivity implements android.location.LocationListener, OnMapLongClickListener, OnMarkerDragListener, OnCameraChangeListener {
 
 	private static final String MAP_VIEW_TYPE_SELECTED = "map_type_selected";
 	private static final float MIN_ZOOM_LEVEL_FOR_MARKING = 17.0f;
 	private static final int ROUTE_INDEX_FREE = 0;
 	private static final int ROUTE_INDEX_NORMAL = 1;
 	private static final int ROUTE_INDEX_FULL = 2;
 	private static final String ROUTE_FREE_STRING = "Libre";
 	private static final String ROUTE_NORMAL_STRING = "Normal";
 	private static final String ROUTE_FULL_STRING = "Embouteillé";
 	private static final int ROUTE_FREE_COLOR = Color.GREEN;
 	private static final int ROUTE_NORMAL_COLOR = Color.YELLOW;
 	private static final int ROUTE_FULL_COLOR = Color.RED;
 	private final String URL_TO_PHP_FILE = "http://usmandiaye.legtux.org/android/json.php"; 
 	private final String URL_TO_JSON_FILE = "http://usmandiaye.legtux.org/android/routes.json"; 
 	private String infosDesc = "Aucune description";
 	private GoogleMap map;
 	private UiSettings mapSettings;
 	private ArrayList<Marker> markers;
 	private SessionManager session;
 	
 	private ArrayList<Polyline> polylines;
 	private int alertType;
 	private LatLng myPos;
 	private ImageView deleteMarker;
 	private LocationManager locationManager;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.map_fragment); 
         session = new SessionManager(getApplicationContext());
 		manageTuto();
		if (isConnected()) {
 			setupInterface();
 			downloadData();
 			configureMap();
 			manageLocation();
 		} else {
 			showNoConnectionDialog(this);
 		}
     }
     
     private void setupInterface() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
     protected void onStop() {
     	// TODO Auto-generated method stub
     	super.onStop();
     	if(locationManager != null) {
     		locationManager.removeUpdates(this);
     	}
     }
 
 	public void showNoConnectionDialog(final Activity activity) {
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Etes vous connecté ?");
 		builder.setMessage("Veuillez vérifier votre connexion internet. L'activation du GPS facilitera votre localisation dans l'application.");
 		builder.setPositiveButton("Wifi",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						activity.startActivity(new Intent(
 								Settings.ACTION_WIFI_SETTINGS)); 
 					}
 				});
 		builder.setNegativeButton("Quitter",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						activity.finish(); 
 					}
 				});
 		builder.setNeutralButton("GPS", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				activity.startActivity(new Intent(
 						Settings.ACTION_LOCATION_SOURCE_SETTINGS));
 			}
 		});
 
 		builder.setCancelable(false);
 		builder.show();
 	}
 	
 	public boolean isConnected() {
 		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo ni = cm.getActiveNetworkInfo();
 		if (ni == null) {
 		    Toast.makeText(getApplicationContext(), "Veuillez verifier votre connexion internet.", Toast.LENGTH_LONG).show();
 		    return false;
 		} else {
 			return ni.isConnected();
 		}
 	}
 	
     public void downloadData(){
     	String [] params = {URL_TO_JSON_FILE};
     	if(isConnected()) {
     		new downloadDataTask().execute(params[0]);
     	}
     }
     
     private class downloadDataTask extends AsyncTask<String, Void, ArrayList<Route>>{
 
 		
     	ProgressDialog pdialog;
     	@Override
 		protected void onPostExecute(ArrayList<Route> listRoute) {
 			// TODO Auto-generated method stub
     		Marker firstMarker;
 			Marker secondMarker;
 			LatLng firstLatLng;
 			LatLng secondLatLng;
 			String desc;
 			String user;
 			boolean draggable;
 			int j = 0;
     		super.onPostExecute(listRoute);
 			while(j < listRoute.size()){
 				
 				desc = listRoute.get(j).getDesc();
 				user = listRoute.get(j).getUser();
 				firstLatLng = listRoute.get(j).getFirstLatLng();
 				secondLatLng = listRoute.get(j).getSecondLatLng();
 				draggable = (user.equals(session.getUsername()))?true:false;
 				firstMarker = map.addMarker(createMarkerOptions("A:", desc+" par:"+user, firstLatLng, draggable));
 				markers.add(firstMarker);
 				secondMarker = map.addMarker(createMarkerOptions("B:", desc+" par:"+user, secondLatLng, draggable));
 				markers.add(secondMarker);
 				drawBetween2Points(getAlertColor(listRoute.get(j).getTypealert()), firstMarker, secondMarker);
 				j++;
 			}
 			pdialog.dismiss();
 		}
 
 		@Override
 		protected void onPreExecute() {
 			// TODO Auto-generated method stub
 			super.onPreExecute();
 			pdialog = new ProgressDialog(MapActivity.this);
 			pdialog.setMessage("Chargement des routes...");
 			pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 			pdialog.setCancelable(false);
 			pdialog.show();
 		}
 
 		@Override
 		protected ArrayList<Route> doInBackground(String... params) {
 			// TODO Auto-generated method stub
 			
 			InputStream is = null;
 			BufferedReader bufreader = null;
 			StringBuilder sb = new StringBuilder();
 			String result = "";
 			String line = null;
 			String url = params[0];
 			HttpClient client = new DefaultHttpClient();
 			HttpGet get = new HttpGet(url);
 			
 			try {
 				HttpResponse response = client.execute(get);
 				is = response.getEntity().getContent();
 			} catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			try {
 				bufreader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
 			} catch (UnsupportedEncodingException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			try {
 				while((line = bufreader.readLine()) != null){
 					sb.append(line + "\n");
 				}
 				is.close();
 				result = sb.toString();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			JSONObject jObj;
 			JSONArray jsnar; //getting json master array
 			JSONObject jsubObj; //getting json sub object
 			int taille;
 			int i = 0;
 			LatLng firstLatLng;
 			LatLng secondLatLng;
 			int typealert;
 			String user;
 			String desc;
 			ArrayList<Route> listRoute = new ArrayList<Route>();
 			
 			try {
 				jObj = new JSONObject(result);
 				jsnar = jObj.getJSONArray("routes");
 				taille = jsnar.length();
 				while (i < taille) {                            //loop to get json sub objects which content datas
 					jsubObj = jsnar.getJSONObject(i);
 					user = jsubObj.getString("user");
 					desc = jsubObj.getString("desc");
 					firstLatLng = new LatLng(jsubObj.getDouble("lat1st"), jsubObj.getDouble("lng1st"));
 					secondLatLng = new LatLng(jsubObj.getDouble("lat2nd"), jsubObj.getDouble("lng2nd"));
 					typealert = jsubObj.getInt("typealert");
 					listRoute.add(new Route(firstLatLng, secondLatLng, typealert, desc, user));
 					i++;
 				}
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return listRoute;
 		}
     }
     
 	@Override
 	public void onRestoreInstanceState(Bundle savedInstanceState) {
 		// Restore the previously serialized current tab position.
 		if (savedInstanceState.containsKey(MAP_VIEW_TYPE_SELECTED)) {
 			if(map != null){
 				map.setMapType(savedInstanceState.getInt(MAP_VIEW_TYPE_SELECTED));
 //				map.animateCamera(CameraUpdateFactory.z)
 			}
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		// Serialize the current tab position.
 		if(map != null){
 			outState.putInt(MAP_VIEW_TYPE_SELECTED, map.getMapType());
 		}
 	}
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	// TODO Auto-generated method stub
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.main, menu);
 		
 		ActionBar actionBar = getSupportActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(false);
     	return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	// TODO Auto-generated method stub
     	switch (item.getItemId()) {
 		case android.R.id.home:
 			onBackPressed();
 			break;
 
 		case R.id.action_info:
 			showTuto();
 			break;
 			
 		case R.id.action_change_view:
 			changeMapType();
 			break;
 		case R.id.action_mode_text:
 			loadModeText();
 			break;
 		case R.id.action_refresh:
 			if(isConnected()){
 				map.clear();
 				markers.clear();
 				downloadData();
 			}
 			break;
 		default:
 			break;
 		}
     	return super.onOptionsItemSelected(item);
     }
     
     private void loadModeText() {
 		// TODO Auto-generated method stub
     	Toast.makeText(this, "En developpement !", Toast.LENGTH_LONG).show();
 	}
 
 	public void changeMapType(){
     	
     	if(map != null){
 
         	int type = map.getMapType(); 
         	Toast toast = new Toast(this);
         	
         	switch (type) {
     		case GoogleMap.MAP_TYPE_NORMAL:
     			map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
     			toast = getCustomToast(R.string.mapTypeSatellite);
     			break;
     		case GoogleMap.MAP_TYPE_SATELLITE:
     			map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
     			toast = getCustomToast(R.string.mapTypeStreet);
     			break;
     		case GoogleMap.MAP_TYPE_TERRAIN:
     			map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
     			toast = getCustomToast(R.string.mapTypeHybrid);
     			break;
     		case GoogleMap.MAP_TYPE_HYBRID:
     			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
     			toast = getCustomToast(R.string.mapTypeNormal);
     			break;
 
     		default:
     			break;
     		}
         	toast.show();
     	} else {
     		
     	}
     }
     
     public Toast getCustomToast(int stringResourceId){
     
     	LayoutInflater inflater = getLayoutInflater();
    	 
 		View layout = inflater.inflate(R.layout.custom_toast,
 		  (ViewGroup) findViewById(R.id.custom_toast_layout_id));
 
 		// set a message
 		TextView text = (TextView) layout.findViewById(R.id.text);
 		text.setText(stringResourceId);
 
 		// Toast...
 		Toast toast = new Toast(getApplicationContext());
 		toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
 		toast.setDuration(Toast.LENGTH_SHORT);
 		toast.setView(layout);
 		
 		return toast;
     }
     
     public Toast getCustomToast(String string){
         
     	LayoutInflater inflater = getLayoutInflater();
    	 
 		View layout = inflater.inflate(R.layout.custom_toast,
 		  (ViewGroup) findViewById(R.id.custom_toast_layout_id));
 
 		// set a message
 		TextView text = (TextView) layout.findViewById(R.id.text);
 		text.setText(string);
 
 		// Toast...
 		Toast toast = new Toast(getApplicationContext());
 		toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
 		toast.setDuration(Toast.LENGTH_SHORT);
 		toast.setView(layout);
 		
 		return toast;
     }
     
 	public boolean configureMap() {
 		
 		map = ((TransparentSupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
 		markers = new ArrayList<Marker>();
 		polylines = new ArrayList<Polyline>();
 		
 		boolean mReturn = false;
 		if (map != null) {
 			map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
 			mapSettings = map.getUiSettings();
 			mapSettings.setCompassEnabled(true);
 			mapSettings.setTiltGesturesEnabled(true);
 			mapSettings.setRotateGesturesEnabled(true);
 			mapSettings.setZoomControlsEnabled(true);
 			map.setMyLocationEnabled(true);
 			if(myPos != null){
 				map.animateCamera(CameraUpdateFactory.newLatLngZoom(myPos, session.getZoomSize()));
 			} else {
 				LatLng latLng = new LatLng(Double.parseDouble(session.getAnimeToLat()), Double.parseDouble(session.getAnimeToLng()));
 				map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, session.getZoomSize()));
 			}
 			
 	        map.setOnMapLongClickListener(this);
 	        map.setOnMarkerDragListener(this);
 	        map.setOnCameraChangeListener(this);
 	        
 			mReturn = true;
 			
 			// marker deleter on drag
 			deleteMarker = (ImageView) findViewById(R.id.deleteMarker);
 		}
 		return mReturn;
 	}
 
 	private void manageLocation() {
 		// TODO Auto-generated method stub
 		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
 		// Showing status
         if(status!=ConnectionResult.SUCCESS){ // Google Play Services are not available
  
             int requestCode = 10;
             Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
             dialog.show();
  
         } else { // Google Play Services are available
  
             locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
  
             Criteria criteria = new Criteria();
  
             String provider = locationManager.getBestProvider(criteria, true);
             Location location = locationManager.getLastKnownLocation(provider);
 
 			if (location != null) {
 				onLocationChanged(location);
 				myPos = new LatLng(location.getLatitude(), location.getLongitude());
 			}
 			
             locationManager.requestLocationUpdates(provider, 30000, 0, this);
         }
 	}
 	
 	public void manageTuto(){
 		if(!session.isTutoShown()){
         	showTuto();
         }
 	}
 	
 	public void showTuto(){
 		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
     	View view = inflater.inflate(R.layout.tuto_layout, null);
     	final LinearLayout ll = (LinearLayout) findViewById(R.id.mainLayout);
     	ll.addView(view);
     	ll.setVisibility(View.VISIBLE);
     	
     	Button closeButton = (Button) view.findViewById(R.id.closeButton);
     	final CheckBox checkBox = (CheckBox) view.findViewById(R.id.doNotShowAgain);
     	
     	closeButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				ll.setVisibility(View.GONE);
 				if(checkBox.isChecked()) {
 	        		session.setTutoShown(true);
 	        	} else {
 	        		session.setTutoShown(false);
 	        	}
 			}
 		});
 	}
 	
 	public void registerUser(){
 		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
     	View view = inflater.inflate(R.layout.registration_layout, null);
     	final LinearLayout ll = (LinearLayout) findViewById(R.id.mainLayout);
     	ll.addView(view);
     	ll.setVisibility(View.VISIBLE);
     	Button saveButton = (Button) view.findViewById(R.id.save);
     	
     	final EditText username = (EditText) view.findViewById(R.id.username);
     	final EditText name = (EditText) view.findViewById(R.id.name);
     	final EditText email = (EditText) view.findViewById(R.id.email);
     	
     	saveButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				String usernameString = username.getText().toString();
 				String nameString = name.getText().toString();
 				String emailString = email.getText().toString();
 				
 				if(!usernameString.isEmpty() && !nameString.isEmpty() && !emailString.isEmpty() && isEmailValid(emailString)){
 					ll.setVisibility(View.GONE);
 					session.createSession(
 							usernameString, 
 							nameString, 
 							emailString
 							);
 					Intent intent = new Intent(getApplicationContext(), MapActivity.class);
 					finish();
 					startActivity(intent);
 				} else {
 					Toast.makeText(MapActivity.this, "Veuillez bien remplir tous les champs !", Toast.LENGTH_LONG).show();
 				}
 			}
 		});
 	}
 		
 	public MarkerOptions createMarkerOptions(String title, String snippet, LatLng position, boolean draggable){
 		MarkerOptions markerOptions = new MarkerOptions();
 		markerOptions
 			.title(title)
 			.draggable(draggable)
 			.snippet(snippet)
 			.position(position);
 		return markerOptions;
 	}
 	
 	@Override
 	public void onMapLongClick(LatLng position) {
 		// TODO Auto-generated method stub
 
         if(!session.hasUID()){
         	registerUser();
         } else {
 
     		if(map.getCameraPosition().zoom < MIN_ZOOM_LEVEL_FOR_MARKING){
     			Toast toast = getCustomToast("Zoomez encore " + Math.round(MIN_ZOOM_LEVEL_FOR_MARKING - map.getCameraPosition().zoom) + " fois pour pouvoir marquer des routes");
     			toast.show();
     		} else {
 
     			final LatLng point = position;
     			Marker marker = map.addMarker((new MarkerOptions()).position(point).draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
     			markers.add(marker);
     			
     			if (isOdd(markers.size())) {
     				
     				AlertDialog.Builder dialog = new AlertDialog.Builder(this);
 
     				dialog.setTitle("Marquer ce trajet ?");
     				CharSequence[] csitems = new CharSequence[3];
     				csitems[ROUTE_INDEX_FREE] = ROUTE_FREE_STRING;
     				csitems[ROUTE_INDEX_NORMAL] = ROUTE_NORMAL_STRING;
     				csitems[ROUTE_INDEX_FULL] = ROUTE_FULL_STRING;
     				alertType = ROUTE_INDEX_FREE;
     				dialog.setSingleChoiceItems(csitems, ROUTE_INDEX_FREE, new OnClickListener() {
 
     					public void onClick(DialogInterface arg0, int pos) {
     						// TODO Auto-generated method stub
     						alertType = pos;
     						Log.i("SELECTED", String.valueOf(alertType));
 
     					}
     				});
 
     				dialog.setPositiveButton(android.R.string.ok,
     						new OnClickListener() {
     							public void onClick(DialogInterface dialog, int which) {
 
     								final EditText inputInfos = new EditText(MapActivity.this);
     								final AlertDialog buildInfos = new AlertDialog.Builder(MapActivity.this)
     								.setTitle("Description du trajet")
     								.setMessage("Message")
     								.setView(inputInfos)
     								.setCancelable(false)
     								.setPositiveButton(android.R.string.ok, new OnClickListener() {
 
     									@Override
     									public void onClick(DialogInterface dialog, int which) {
     										// TODO Auto-generated method stub
     										infosDesc = inputInfos.getText().toString();
     										Log.i("DESC", infosDesc);
     										ArrayList<LatLng> last2LatLng = drawBetween2LastPoints(getAlertColor(alertType), "title", infosDesc);
     										Log.i("AVLAST", String.valueOf(last2LatLng.get(0)));
     										Log.i("LAST", String.valueOf(last2LatLng.get(1)));
     										String lat1st = String.valueOf(last2LatLng.get(0).latitude);
     										String lng1st = String.valueOf(last2LatLng.get(0).longitude);
     										String lat2nd = String.valueOf(last2LatLng.get(1).latitude);
     										String lng2nd = String.valueOf(last2LatLng.get(1).longitude);
     										String usern = session.getUsername();
     										String descroute = infosDesc ;
     										String[] arrayDatas = {String.valueOf(alertType), lat1st, lng1st, lat2nd, lng2nd, usern, descroute};
     										new sendRouteDatasTask().execute(arrayDatas);
 
     									}
     								})
     								.create();
     								inputInfos.addTextChangedListener(new TextWatcher(){
 
     									@Override
     									public void afterTextChanged(Editable s) {
     										// TODO Auto-generated method stub
     									  if(s.toString().length() == 0){
     											buildInfos.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
     										}
     									  else{
     										  buildInfos.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
     									  }
     									}
 
     									@Override
     									public void beforeTextChanged(CharSequence s, int start,int count, int after) {}
 
     									@Override
     									public void onTextChanged(CharSequence s,int start, int before, int count) {}
 
     								});
 
     								buildInfos.show();
     								buildInfos.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
 
 
     							}
     						});
     				dialog.setNegativeButton(android.R.string.cancel,
     						new OnClickListener() {
 
     							public void onClick(DialogInterface dialog, int which) {
     								// on enleve le dernier marker
     								int removeIndex = markers.size() - 1;
     								Marker removeMarker = markers.get(removeIndex); // get the marker
     								removeMarker.remove(); // remove the marker
     								markers.remove(removeIndex); // remove the index
     							}
     						});
     				dialog.create().show();
     			}
     		}
         }
 	}
 	
 	private class sendRouteDatasTask extends AsyncTask<String, Void, Void> {
 
 		@Override
 		protected Void doInBackground(String... params) {
 			// TODO Auto-generated method stub
 			sendRouteDatas(params[0], params[1], params[2], params[3], params[4], params[5], params[6]);
 			return null;
 		}
 
 	}
 
 	public void sendRouteDatas(String typealert, String lat1st, String lng1st, String lat2nd, String lng2nd, String user, String desc){
 
 		List<NameValuePair> values = new ArrayList<NameValuePair>();
 		values.add(new BasicNameValuePair("typealert", typealert));
 		values.add(new BasicNameValuePair("lat1st", lat1st));
 		values.add(new BasicNameValuePair("lng1st", lng1st));
 		values.add(new BasicNameValuePair("lat2nd", lat2nd));
 		values.add(new BasicNameValuePair("lng2nd", lng2nd));
 		values.add(new BasicNameValuePair("desc", desc));
 		values.add(new BasicNameValuePair("user", user));
 		HttpClient client = new DefaultHttpClient();
 		HttpPost post = new HttpPost(URL_TO_PHP_FILE);
 		try {
 			post.setEntity(new UrlEncodedFormEntity(values));
 			client.execute(post);
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public int getAlertColor(int index) {
 		switch (index) {
 		case ROUTE_INDEX_FREE:
 			return ROUTE_FREE_COLOR;
 		case ROUTE_INDEX_NORMAL:
 			return ROUTE_NORMAL_COLOR;
 		case ROUTE_INDEX_FULL:
 			return ROUTE_FULL_COLOR;
 
 		default:
 			return ROUTE_FREE_COLOR;
 		}
 	}
 	
 	public ArrayList<LatLng> drawBetween2LastPoints(int color, String title, String snippet){
 		
 		Marker aMarker = markers.get(markers.size() - 2);
 		Marker bMarker = markers.get(markers.size() - 1);
 		ArrayList<LatLng> last2markers = new ArrayList<LatLng>();
 		last2markers.add(aMarker.getPosition());
 		last2markers.add(bMarker.getPosition());
 		
 		// if title is already set, use it directly, otherwise prefix titles by A and B depending on marker orders
 		if(aMarker.getTitle() != null && bMarker.getTitle() != null){
 			if(aMarker.getTitle().startsWith("A:") || bMarker.getTitle().startsWith("B:") ){
 				aMarker.setTitle("A: " + title);
 				bMarker.setTitle("B: " + title);
 			} else {
 				aMarker.setTitle(title);
 				bMarker.setTitle(title);
 			}
 		}
 		
 		aMarker.setSnippet(snippet);
 		bMarker.setSnippet(snippet);
 		
 		switch (color) {
 		case Color.RED:
 			aMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
 			bMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
 			break;
 		case Color.GREEN:
 			aMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
 			bMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
 			break;
 		case Color.YELLOW:
 			aMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
 			bMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
 			break;
 
 		default:
 			break;
 		}
 		
 		LatLng origin = aMarker.getPosition();
 		LatLng dest = bMarker.getPosition();
 		
 		String url = getDirectionsUrl(origin, dest);
 		DownloadTask downloadTask = new DownloadTask(color);
 		downloadTask.execute(url);
 		return last2markers;
 	}
 	
 public void drawBetween2Points(int color, Marker aMarker, Marker bMarker){
 		switch (color) {
 		case Color.RED:
 			aMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
 			bMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
 			break;
 		case Color.GREEN:
 			aMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
 			bMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
 			break;
 		case Color.YELLOW:
 			aMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
 			bMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
 			break;
 
 		default:
 			break;
 		}
 		
 		LatLng origin = aMarker.getPosition();
 		LatLng dest = bMarker.getPosition();
 		
 		String url = getDirectionsUrl(origin, dest);
 		DownloadTask downloadTask = new DownloadTask(color);
 		downloadTask.execute(url);
 	}
 
 	public boolean isOdd(int size) {
 		return size % 2 == 0;
 	}
 	
 	private String getDirectionsUrl(LatLng origin,LatLng dest){
 		
 		// Origin of route
 		String str_origin = "origin="+origin.latitude+","+origin.longitude;
 		
 		// Destination of route
 		String str_dest = "destination="+dest.latitude+","+dest.longitude;		
 		
 					
 		// Sensor enabled
 		String sensor = "sensor=false";			
 					
 		// Building the parameters to the web service
 		String parameters = str_origin+"&"+str_dest+"&"+sensor;
 					
 		// Output format
 		String output = "json";
 		
 		// Building the url to the web service
 		String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters; 
 		
 		return url;
 	}
 	
 	/** A method to download json data from url */
     private String downloadUrl(String strUrl) throws IOException{
         String data = "";
         InputStream iStream = null;
         HttpURLConnection urlConnection = null;
         try{
                 URL url = new URL(strUrl);
 
                 urlConnection = (HttpURLConnection) url.openConnection();
                 urlConnection.connect();
                 iStream = urlConnection.getInputStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
                 StringBuffer sb  = new StringBuffer();
                 String line = "";
                 while( ( line = br.readLine())  != null){
                         sb.append(line);
                 }
                 
                 data = sb.toString();
 
                 br.close();
 
         }catch(Exception e){
                 Log.d("Exception while downloading url", e.toString());
         }finally{
                 iStream.close();
                 urlConnection.disconnect();
         }
         return data;
      }
 	
 	// Fetches data from url passed
 	private class DownloadTask extends AsyncTask<String, Void, String>{			
 				
 		private int color;
 
 		public DownloadTask(int color) {
 			// TODO Auto-generated constructor stub
 			this.color = color;
 		}
 		// Downloading data in non-ui thread
 		@Override
 		protected String doInBackground(String... url) {
 				
 			// For storing data from web service
 			String data = "";
 					
 			try{
 				// Fetching the data from web service
 				data = downloadUrl(url[0]);
 			}catch(Exception e){
 				Log.d("Background Task",e.toString());
 			}
 			return data;		
 		}
 		
 		// Executes in UI thread, after the execution of
 		// doInBackground()
 		@Override
 		protected void onPostExecute(String result) {			
 			super.onPostExecute(result);			
 			
 			ParserTask parserTask = new ParserTask(color);
 			
 			// Invokes the thread for parsing the JSON data
 			parserTask.execute(result);
 				
 		}		
 	}
 	
 	/** A class to parse the Google Places in JSON format */
     private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{
     	
     	private int color;
 
 		public ParserTask(int color) {
 			// TODO Auto-generated constructor stub
     		this.color = color;
 		}
     	// Parsing the data in non-ui thread    	
 		@Override
 		protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
 			
 			JSONObject jObject;	
 			List<List<HashMap<String, String>>> routes = null;			           
             
             try{
             	jObject = new JSONObject(jsonData[0]);
             	DirectionsJSONParser parser = new DirectionsJSONParser();
             	
             	// Starts parsing data
             	routes = parser.parse(jObject);    
             }catch(Exception e){
             	e.printStackTrace();
             }
             return routes;
 		}
 		
 		// Executes in UI thread, after the parsing process
 		@Override
 		protected void onPostExecute(List<List<HashMap<String, String>>> result) {
 			ArrayList<LatLng> points = null;
 			PolylineOptions lineOptions = new PolylineOptions();
 			
 			// Traversing through all the routes
 			if(result != null) {
 				for(int i=0;i<result.size();i++){
 					points = new ArrayList<LatLng>();
 					
 					// Fetching i-th route
 					List<HashMap<String, String>> path = result.get(i);
 					
 					// Fetching all the points in i-th route
 					for(int j=0;j<path.size();j++){
 						HashMap<String,String> point = path.get(j);					
 						
 						double lat = Double.parseDouble(point.get("lat"));
 						double lng = Double.parseDouble(point.get("lng"));
 						LatLng position = new LatLng(lat, lng);	
 						
 						points.add(position);						
 					}
 					// Adding all the points in the route to LineOptions
 					lineOptions.addAll(points);
 					lineOptions.width(7);
 					lineOptions.color(color);
 				}
 				
 				// Drawing polyline in the Google Map for the i-th route
 				if(lineOptions != null){
 					Polyline polyline = map.addPolyline(lineOptions);
 					polylines.add(polyline);
 				}
 			}
 		}
     }
 
 	@Override
 	public void onLocationChanged(Location location) {
 		// TODO Auto-generated method stub
 //        double latitude = location.getLatitude();
 //        double longitude = location.getLongitude();
 //
 //        LatLng latLng = new LatLng(latitude, longitude);
 // 
 //        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
 //        map.animateCamera(CameraUpdateFactory.zoomTo(session.getZoomSize()));
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	// OnMarkerDrag methods
 	@Override
 	public void onMarkerDrag(Marker marker) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onMarkerDragEnd(Marker marker) {
 		// TODO Auto-generated method stub
 		LatLng markerLocation = marker.getPosition();
 		LatLng mapCenter = map.getCameraPosition().target;
 		
 		float[] results = new float[1];
 		Location.distanceBetween(markerLocation.latitude, markerLocation.longitude,
 				mapCenter.latitude, mapCenter.longitude, results);
 		 
 		int index = markers.indexOf(marker);
 		if(results[0] / 1000 < (0.05 /*/ map.getCameraPosition().zoom + 0.1 * map.getCameraPosition().zoom)*/ )){
 			
 			if(!isOdd(index)){
 				if(markers.size() > (index)){
 					markers.get(index - 1).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
 				}
 			}
 			marker.remove();
 			markers.remove(index);
 			
 		} else {
 			if(isOdd(index)){ // mean that its a even number (1, 3, 4, ...)
 				if(markers.size() > index+1) {
 					drawBetween2Points(getAlertColor(alertType), marker, markers.get(index+1));
 				}
 			} else {
 				drawBetween2LastPoints(getAlertColor(alertType), marker.getTitle(), marker.getSnippet());
 			}
 		}
 
 		deleteMarker.setVisibility(View.GONE);
 	} 
 	
 	@Override
 	public void onMarkerDragStart(Marker marker) {
 		// TODO Auto-generated method stub
 		deleteMarker.setVisibility(View.VISIBLE);
 		int index = markers.indexOf(marker);
 				
 		if(isOdd(index)){ // mean that its a even number (1, 3, 4, ...)
 			if(polylines.size() > (index+1)/2){
 				polylines.get((index+1)/2).remove(); // on enleve le polyline de la map
 				polylines.remove((index+1)/2); // on supprime son index de la liste
 			}
 		} else {
 			if(polylines.size() > index/2){
 				polylines.get(index / 2).remove(); // on enleve le polyline de la map
 				polylines.remove(index / 2); // on supprime son index de la liste
 			}
 		}
 	}
 
 	@Override
 	public void onCameraChange(CameraPosition position) {
 		// TODO Auto-generated method stub
 		session.setAnimeToLat(""+position.target.latitude);
 		session.setAnimeToLng(""+position.target.longitude);
 		if(map != null){
 			session.setZoomSize(map.getCameraPosition().zoom);
 		}
 	}
 		
 	public boolean isEmailValid(CharSequence email) {
 	   return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
 	}
 }
