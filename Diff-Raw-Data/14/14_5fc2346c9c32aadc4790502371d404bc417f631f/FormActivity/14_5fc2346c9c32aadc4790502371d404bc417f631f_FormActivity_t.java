 package com.rhok.saaraa;
 
 import java.io.File;
import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 
 public class FormActivity extends Activity {
 	private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
 	private String imageShackAPIKey = "57AFJOQW33e358bbe3e44578402635508a6236ca";
 	
     private RelativeLayout container;
 	private List<Item> items;
 	private Map<String, Runnable> mapValueToAction;
 	private List<String> options;
 	private List<String> imageFilenames = new ArrayList<String>();
 	private List<String> imageUrls = new ArrayList<String>();
 	protected Location currentLocation;
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         container = new RelativeLayout(this);
         container.setLayoutParams(new ViewGroup.LayoutParams(
                 ViewGroup.LayoutParams.FILL_PARENT,
                 ViewGroup.LayoutParams.FILL_PARENT));
 
         ScrollView scrollview = new ScrollView(this);
         scrollview.setFillViewport(true);
         scrollview.addView(container);
         
         setContentView(scrollview);
 
         items = new ArrayList<Item>();
         mapValueToAction = new LinkedHashMap<String, Runnable>();
         options = new ArrayList<String>();
         
 		Item item = new Item();
 		items.add(item);
 
 		addEventCategory();
 		
 	    LocationListener locationListener = new LocationListener() {
 	        public void onLocationChanged(Location location) {
 	        	currentLocation = location;
 	        }
 
 	        public void onStatusChanged(String provider, int status, Bundle extras) {}
 
 	        public void onProviderEnabled(String provider) {}
 
 	        public void onProviderDisabled(String provider) {}
 		  };
 		
 		  LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
 		  locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
 	}
 
 	private void addEventCategory() {
 		label("Event Category");
         addOption("Fire", new Runnable() { public void run() {
         	addArePeopleTrapped();
     		addArePeopleInjured();
     		addSeverity( new Runnable() { public void run() { addFireCategory(); } } );
         } });
         addOption("Flood", new Runnable() { public void run() {
         	addArePeopleTrapped();
     		addArePeopleInjured();
     		addSeverity( new Runnable() { public void run() {
     			checkBox("Houses in Flood?");
     			checkBox("Over Roadway?");
     			label("Levee");
     			checkBox("Damage?");
     			checkBox("Overtopping?");
     			checkBox("Failure/Breach?");
     			label("Dam");
     			checkBox("Damage?");
     			checkBox("Overtopping?");
     			checkBox("Failure/Breach?");
     			addLocation();
     		} } );
         } });
         addOption("Gas Leak", new Runnable() { public void run() {
         	addArePeopleTrapped();
     		addArePeopleInjured();
     		addSeverity( new Runnable() { public void run() { 
     			checkBox("Visible or Audible Source?");
     			checkBox("Is There Fire?");
     			checkBox("Are structures threatened?");
     			addLocation();
     		} } );
         } });
         addOption("Downed Power Line", new Runnable() { public void run() {
         	addArePeopleTrapped();
     		addArePeopleInjured();
     		addSeverity( new Runnable() { public void run() { 
     			checkBox("Is There Sparking?");
     			label("Is the Line...");
     			Runnable action = new Runnable() {
     				public void run() {
     					checkBox("Is the Line Touching a Vehicle With People Inside?");
     					addLocation();
     				}
     			};
     			addOption("On the Road", action);
     			addOption("Front Yard", action);
     			addOption("Back Yard", action);
     			addSpinner(new Value("metadata.powerLineLocation"));
     		} } );
         } });
 //        addOption("Severe Weather", new Runnable() { public void run() {
 //        	// TODO
 //        } });
         addOption("Earthquake", new Runnable() { public void run() {
         	addArePeopleTrapped();
     		addArePeopleInjured();
     		addBuildingType(new Runnable() {
     			public void run() {
     				checkBox("Is There Fire?");
     				addSeverity(new Runnable() {
     					public void run() {
     						checkBox("Are Structures Threatened?");
     						addLocation();
     					}
 					} );
     			}
     		});
         } });
         addSpinner(new Value("category"));
 	}
 
 	protected void addSeverity(Runnable action) {
 		label("Severity Level");
 		addOption("Green", action);
 		addOption("Yellow", action);
 		addOption("Red", action);
 		addSpinner(new Value("severity"));
 	}
 
 	private void addFireCategory() {
 		label("Fire Category");
 		addOption("Building", new Runnable() { public void run() { addBuildingType(
 				new Runnable() { public void run() { addLocation(); } }
 		); } });
 		addOption("Grass/Forest", new Runnable() { public void run() { addLocation(); } });
 		addOption("Vehicle", new Runnable() { public void run() { addLocation(); } });
 		addOption("Boat / Marina / Dock", new Runnable() { public void run() { addLocation(); } });
 		addOption("Other", new Runnable() { public void run() { addLocation(); } });
         addSpinner(new Value("metadata.fireCategory"));
 	}
 	
 	private void addArePeopleInjured() {
 		String label = "Are People Injured?";
 		checkBox(label);
 	}
 
 	private void checkBox(String label) {
 		CheckBox checkBox = new CheckBox(this);
 		checkBox.setText(label);
 		add(checkBox);
 	}
 
 	private void addArePeopleTrapped() {
 		CheckBox checkBox = new CheckBox(this);
 		checkBox.setText("Are People Trapped?");
 		add(checkBox);
 	}
 
 	private void addBuildingType(final Runnable lastAction) {
 		label("Building Type");
 		addOption("Public", new Runnable() { public void run() { addBuildings( lastAction,
 				"Fire station",
 		        "Police",
 		        "Hospital",
 		        "School",
 		        "Library",
 		        "Community center",
 		        "Services (gas, power, water)",
 		        "Other"); } });
 		addOption("Private", new Runnable() { public void run() { addBuildings( lastAction,
 				"Warehouse",
 		        "Office - single level",
 		        "Office - high rise"); } });
 		addOption("Residential", new Runnable() { public void run() { addBuildings( lastAction,
 				"Single family house",
 		        "Apartment building",
 		        "High rise"); } });
 		addSpinner(new Value("metadata.buildingType"));
 	}
 	
 	private void addBuildings(final Runnable lastAction, String... buildings) {
 		label("Building");
 		Runnable action = new Runnable() {
 			public void run() {
 				lastAction.run();
 			}
 		};
         for(String building : buildings) {
         	addOption(building, action);
         }
         addSpinner(new Value("metadata.building"));
 	}
 	
 	private void addLocation() {
 		label("What do you see?");
 		
 		EditText textEdit = new EditText(this);
         add(textEdit);
         Value value;
         value = new Value("description");
         value.setSource(textEdit);
         items.get(items.size()-1).add(value);
         
 		label("Where are you?");
 		
 		textEdit = new EditText(this);
         add(textEdit);
         value = new Value("location.address");
         value.setSource(textEdit);
 		items.get(items.size()-1).add(value);
         
         Button addPhotoButton = new Button(this);
         addPhotoButton.setText("Add Photo");
         
 	    addPhotoButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent intent = new Intent(FormActivity.this, CaptureActivity.class);
 		        startActivityForResult(intent, 0);
 			}
 		});
 	      
         add(addPhotoButton);
         
         Button submitButton = new Button(this);
         submitButton.setText("Submit Report");
 
         add(submitButton);
         
         submitButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View arg0) {
 				submitImages();
 				String json = buildJson();
 				
 				postJson(json);
 			}
 
 
 		});
 	}
 	
 	protected String buildJson() {
 		try {
 			JSONObject json = new JSONObject();
 			JSONObject metadata = new JSONObject();
 			json.put("metadata", metadata);
         	JSONObject loc = new JSONObject();
 			json.put("location", loc);
         	
 			for(Item item : items) {
 				for(Value value : item.getValues()) {
 					value.populate(json);
 				}
 			}
 			json.put("captured", FORMAT.format(new Date()));
 			json.put("submitted", FORMAT.format(new Date()));
 			JSONObject reporter = new JSONObject();
         	reporter.put("phone", getCellPhone());
         	json.put("reporter", reporter);
         	JSONArray photos = new JSONArray(imageUrls);
         	json.put("photos", photos);
         	
         	if ( currentLocation != null ) {
         		double currentLatitude = currentLocation.getLatitude();
         		double currentLongitude = currentLocation.getLongitude();
         		loc.put("latitude", currentLatitude);
         		loc.put("longitude", currentLongitude);
         	}
         	else {
         		loc.put("latitude", 47.646210);
         		loc.put("longitude", -122.138750);
         	}
         	
 			return json.toString();
 		} catch (JSONException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	private void postJson(String json) {
 		Log.d("JSON", json);
 		try {
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpPost httppost = new HttpPost("http://saaraa.heroku.com/reports");
 			StringEntity se = new StringEntity( json );  
 			httppost.setEntity(se);
 			HttpResponse response = httpclient.execute(httppost);
 			if (response.getStatusLine().getStatusCode() == 200) {
 				message("Report submitted");
 				imageFilenames.clear();
 				imageUrls.clear();
 			} else {
 				message("Report failed to submit");
 			}
 			//message("Json "+json+" Response "+response.getStatusLine().getStatusCode()+" "+response.getStatusLine().getReasonPhrase());
		} catch (org.apache.http.conn.HttpHostConnectException e) {
			message("Connection refused");
		} catch (ClientProtocolException e) {
			message("ClientProtocolException");
		} catch (IOException e) {
			message("IO Exception");
 		}
 	}
	
 	private void message(String string) {
 		Toast.makeText(this, string, Toast.LENGTH_LONG).show();
 	}
 
 	public String getCellPhone() {
 		TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
 		return telephonyManager.getLine1Number();
 	}
 
 	protected void submitImages() {
 		for (String filename : imageFilenames) {
 			try {
 				HttpClient httpclient = new DefaultHttpClient();
 				HttpPost httppost = new HttpPost("http://www.imageshack.us/upload_api.php");
 				
 				List<NameValuePair> params = new ArrayList<NameValuePair>();
 	            params.add(new BasicNameValuePair("key", imageShackAPIKey));
 	            params.add(new BasicNameValuePair("rembar", "1"));
 	            params.add(new BasicNameValuePair("public", "0"));
 	            
 	            StringBody stringBody = new StringBody(params.toString());
 	            
 	            FileBody bin = new FileBody(new File(filename), "image/jpeg");
 	            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);  
 	            reqEntity.addPart("fileupload", bin);
 	            reqEntity.addPart("", stringBody);
 	            
 	            httppost.setEntity(reqEntity);
 	            
                 Log.i("IMAGE_UPLOAD", "uploading " + filename);
 
 	            HttpResponse response = httpclient.execute(httppost);  
 	            HttpEntity resEntity = response.getEntity();  
 	            if (resEntity != null) {
 	            	String responseString = EntityUtils.toString(resEntity);
 	                Log.i("IMAGE_UPLOAD", responseString);
 	                
 	                String url = responseString.split("<image_link>")[1].split("</image_link>")[0];
 	                imageUrls.add(url);
 	            }
 			}
 			catch (Exception e) {
 				Log.e("IMAGE_UPLOAD", e.getMessage());
 			}
 		}
 		
 		for (String url : imageUrls) {
 			Log.i("IMAGES", url);
 		}
 	}
 
 	private void addSpinner(Value stringValue) {
 		Spinner spinner = new Spinner(this);
 		List<String> selections = new ArrayList<String>();
 		selections.add("Select one...");
 		selections.addAll(options);
 		ArrayAdapter<CharSequence> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, selections.toArray(new String[]{}));
 	    spinner.setAdapter(adapter);
 	    final int here = items.size();
 	    final Map<String, Runnable> map = mapValueToAction;
 	    spinner.setOnItemSelectedListener(new SelectionHandler() {
 			protected void onSelected(String selection) {
 				unwindTo(here);
 				if (map.containsKey(selection)) {
 					Item item = new Item();
 					items.add(item);
 					
 					map.get(selection).run();
 				}
 			}
 		});
 		add(spinner);
 		stringValue.setSource(spinner);
 		items.get(items.size()-1).add(stringValue);
 		mapValueToAction = new HashMap<String, Runnable>(); 
 		options = new ArrayList<String>();
 	}
 
 	private void addOption(String value, Runnable runnable) {
 		mapValueToAction.put(value, runnable);
 		options.add(value);
 	}
 	
 	private int unwindTo(int parent) {
 		while(items.size() > parent) {
 			Item item = items.remove(items.size()-1);	
 			for(View view : item.getViews()) {
 				container.removeView(view);
 				index--;
 			}
 		}
 		
 		return items.size();
 	}
 
 	private TextView label(String text) {
 		TextView label = new TextView(this);
         label.setText(text);
         add(label);
 		return label;
 	}
 
 	int index = 1;
 	
 	private void add(final View child) {
 		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
 		if (index > 1) {
 			layoutParams.addRule(RelativeLayout.BELOW, index);
 		}
 		index++;
 		child.setId(index);
 		
 		container.addView(child, layoutParams);
 		items.get(items.size()-1).add(child);
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		imageFilenames.add(data.getStringExtra("com.rhok.saaraa.FileName"));
 	}
 
 }
