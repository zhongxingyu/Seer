 	package com.taxishout.main;
 	import java.io.BufferedReader;
 	import java.io.IOException;
 	import java.io.InputStream;
 	import java.io.InputStreamReader;
 	import java.net.URI;
 	import java.net.URISyntaxException;
 	import java.util.ArrayList;
 	import java.util.List;
 	import java.util.Locale;
 
 	import org.apache.http.HttpEntity;
 	import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 	import org.apache.http.StatusLine;
 	import org.apache.http.client.ClientProtocolException;
 	import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 	import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.StringEntity;
 	import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 	import org.json.JSONException;
 	import org.json.JSONObject;
 
 import android.app.Activity;
 	import android.app.AlertDialog;
 	import android.app.PendingIntent;
 	import android.app.ProgressDialog;
 	import android.content.Context;
 	import android.content.DialogInterface;
 	import android.content.Intent;
 	import android.graphics.drawable.Drawable;
 	import android.location.Address;
 	import android.location.Geocoder;
 import android.net.Uri;
 	import android.os.Handler;
 	import android.telephony.SmsManager;
 	import android.util.Log;
 	import android.widget.ArrayAdapter;
 	import android.widget.EditText;
 	import android.widget.LinearLayout;
 	import android.widget.LinearLayout.LayoutParams;
 	import android.widget.ScrollView;
 	import android.widget.TextView;
 
 	import com.google.android.maps.GeoPoint;
 	import com.google.android.maps.ItemizedOverlay;
 import com.google.android.maps.OverlayItem;
 
 	public class MyOverlays  extends ItemizedOverlay<OverlayItem> {
 
 		private ArrayList<OverlayItem> mapOverlays = new ArrayList<OverlayItem>();
 		private ProgressDialog ProgressBar;
 		private int progressBarStatus = 0,AddressId=0;
 		
 		private Handler progressBarHandler = new Handler();
 		private Handler dialogHandler = new Handler();
 		private String data_string;
 		private String ProgressBarMessage;
 		private String message,MYAddress,DriverNumber;
 		private int ErrorStatus = 0;
 		private String objectId;
 		final Runnable dialogRunnable = new Runnable() {
 	        public void run() {
 	            //displays the dialog
 	        	AlertDialog.Builder dialog = new AlertDialog.Builder(context);
 	        	final EditText input = new EditText(context);
 	        	TextView text = new TextView(context);
 	        	text.setText(message);
 	        	input.setHint("Enter your location details");
 	        	dialog.setTitle("Book Cab");
 			    dialog.setCancelable(false)
 			    	.setView(input)
 			    	.setMessage(message)
 			    	.setNeutralButton("Call driver", new DialogInterface.OnClickListener(){
 
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							// TODO Auto-generated method stub
 							Intent i = new Intent(Intent.ACTION_CALL);
 							i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 							i.setData(Uri.parse("tel:"+DriverNumber));
 							context.startActivity(i);
 							
 						}})
 			    	.setPositiveButton("Book Cab", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			               //Set isAvaiable to false
 			        	   setUnavailable(objectId);
 			        	   //Sending SMS; PendingIntent activities to be added
 			        	   String sms = "New Client Request" + "\n" + 
 			        	   "Location:" + MYAddress + "\nComments:" + input.getText();
 			        	   String SENT = "SMS_SENT";
 			       			PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0, new Intent(SENT), 0);
 			       			SmsManager SMS = SmsManager.getDefault();
 			       			//SMS.sendTextMessage(DriverNumber, null, sms, sentIntent, null);	
 			           }
 
 					private void setUnavailable(String objectId) {
 						// TODO Auto-generated method stub
 						HttpParams httpParameters = new BasicHttpParams();
 						DefaultHttpClient client = new DefaultHttpClient(httpParameters);
 						HttpPut put = new HttpPut("http://api.parse.com/1/classes/taxishout/"+objectId);
 
 						try
 						{
 						    put.setEntity(new StringEntity("{\"isAvailable\":false}"));
 						    put.addHeader("X-Parse-Application-Id", "3EN6GtbpYtprWOJyqHNaPjXrJixp66F2qTQVOS30");
 						    put.addHeader("X-Parse-REST-API-Key","KTNYweSLVvp8hI2mR3ekXUCzbJW4fIqay21aQk1O");
 						    put.addHeader("Content-Type", "application/json");
 						    HttpResponse response = client.execute(put);
 
 						    // 200 type response.
 						    if (response.getStatusLine().getStatusCode() >= HttpStatus.SC_OK &&
 						    response.getStatusLine().getStatusCode() < HttpStatus.SC_MULTIPLE_CHOICES)
 						    {
 						      // Handle OK response etc........
 						    	Log.d("DEBUG",response.toString());
 						    }
 						}
 						catch (Exception e)
 						{
 							Log.d("DEBUG",e.toString());
 						}
 					}
 			       })
 			       
 			       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			                dialog.cancel();
 			           }
 			       });
 			    AlertDialog alert = dialog.create();
 			    alert.show();
 			    
 	        }
 	    };
 		   
 		   private Context context;
 		   
 		   public MyOverlays(Drawable defaultMarker) {
 		        super(boundCenterBottom(defaultMarker));
 		   }
 		   
 		   public MyOverlays(Drawable defaultMarker, Context context) {
 		        this(defaultMarker);
 		        this.context = context;
 		   }
 
 		   @Override
 		   protected OverlayItem createItem(int i) {
 		      return mapOverlays.get(i);
 		   }
 
 		   @Override
 		   public int size() {
 		      return mapOverlays.size();
 		   }
 		   
 		   @Override
 		   protected boolean onTap(int index) {
 			  //Executed when the overlay item is tapped
 			  final OverlayItem item = mapOverlays.get(index);
 		      data_string = item.getSnippet();
 		      ProgressBar = new ProgressDialog(context);
 		      ProgressBar.setCancelable(false);
 		      ProgressBar.setMessage("Parsing Driver Data");
 		      ProgressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 		      ProgressBar.setProgress(0);
 		      ProgressBar.setMax(100);
 		      ProgressBarMessage = "Parsing Driver Data";
 		      ProgressBar.show();
		      
 		      new Thread(new Runnable() {
 		    	  //Thread handles the Progress Bar Updating. The other Heavy stuff happens in a different thread
 		    	  public void run() {
 		    		  while (progressBarStatus < 100) {
 		    			  try {
 		    				  Thread.sleep(1000);
 		    			  } catch (InterruptedException e) {
 						e.printStackTrace();
 					  }
 	 
 					  // Update the progress bar
 					  progressBarHandler.post(new Runnable() {
 						public void run() {
 						  ProgressBar.setProgress(progressBarStatus);
 						  ProgressBar.setMessage(ProgressBarMessage);
 						}
 					  });
 					}
 	 
 					// ok, file is downloaded,
 					if (progressBarStatus >= 100) {
 	 
 						// sleep 2 seconds, so that you can see the 100%
 						try {
 							Thread.sleep(2000);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 	 
 						// close the progress bar dialog
 						ProgressBar.dismiss();
 						//Activate handler to show Dialogs
 						dialogHandler.post(dialogRunnable);
 						
 					}
 				  }
 			       }).start();
 			
 			
 			
 			//
 			new Thread(new Runnable()
 			{
 				//Thread handles the Network calls, separate from the Progress Bar thread
 				public void run()
 				{
 					JSONObject result = null;
 					ErrorStatus = 0;
 					String Name = null;
 					int Rating = 0;
 					JSONObject Location = null;
 					double Latitude = 0;
 					double Longitude = 0;
 					int LatMD = (int) (Latitude*1000000);
 					int LngMD = (int) (Longitude*1000000);
 					int MyLatMD = 0;
 					int MyLngMD = 0;
 					Geocoder gc = new Geocoder(context,Locale.getDefault());
 					String Driver_address = null;
 					String My_address = null;
 					String query="origin=" +Driver_address+
 					"&destination="+My_address+
 					"&sensor=true&alternatives=false";
 					URI uri = null;
 					//Initialized variables
 			
 					try {
 						result = new JSONObject(data_string);
 						Name = result.getString("driverName");
 						Rating = result.getInt("driverRating");
 						Location = result.getJSONObject("location");
 						Latitude = Location.getDouble("latitude");
 						Longitude = Location.getDouble("longitude");
 						MyLatMD = result.getInt("MYLAT");
 						MyLngMD = result.getInt("MYLNG");
 						double MyLat =((float)MyLatMD)/1000000;
 						double MyLng =((float)MyLngMD)/1000000;
 						DriverNumber = result.getString("phoneNumber");
 						objectId =result.getString("objectId"); 
 						//Got all attributes from the JSON
 						progressBarStatus = 25;
 						ProgressBarMessage="Getting driver's address";
 						//Getting Driver, User addresses
 						Driver_address = getAddress(Latitude,Longitude);
 						if(Driver_address == "FAIL")
 						{
 							ErrorStatus = 1;
 							//change teh progress bar
 						}
 						progressBarStatus = 50;
 						ProgressBarMessage="Getting your address";
 						My_address = getAddress(MyLat,MyLng);
 						if(My_address == "FAIL")
 						{
 							ErrorStatus = 2;
 							//change teh progress bar
 						}
 						//Getting the time taken in minutes
 						progressBarStatus = 75;
 						ProgressBarMessage="Getting estimated time taken";
 						uri = new URI(
 						        "http", 
 						        "maps.googleapis.com", 
 						        "/maps/api/directions/json",
 						        query,
 						        null);
 						String url = uri.toASCIIString();
 						long  minutes = getMinutes(url);
 						//Updating a bunch of Global Variables
 						MYAddress= My_address;
 						message = "Driver Name:"+Name + 
 						"\nDriver Rating:"+Rating + 
 						"\nEstimated Time:" + minutes+
 						"minutes\nBook the cab and send an SMS to the driver?"+
 						"\nYour address is "+My_address + "\nEnter more details about your location";
 						progressBarStatus = 100;
 						ProgressBarMessage="Done";
 					} catch (JSONException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (URISyntaxException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}).start(); 
 		     	      //dialog.setMessage(item.getSnippet());
 		      return true;
 		   }
 		   
 		   private String getAddress(double latMD, double lngMD) throws IOException {
 			   
 			// TODO Auto-generated method stub
 			   Geocoder gc = new Geocoder(this.context,Locale.getDefault());
 			   List<Address> addresses = gc.getFromLocation(latMD, lngMD, 1);
 			   String address="";
 			   if(addresses.size()<1)
 				{
 					Log.d("ADDRESS","NOT FOUND!");
 					return "FAIL!";
 				}
 				else 
 				{
 					address = "";
 					int j = 0;
 					Address gen = addresses.get(0);
 					while(j<=gen.getMaxAddressLineIndex())
 					{
 						address =address+gen.getAddressLine(j)+",";
 						j++;
 					}
 					Log.d("ADDRESS","FULL ADDRESS:"+address);
 					return address;
 				}
 		}
 
 		private long getMinutes(String dist_url) {
 			// TODO Auto-generated method stub
 			   HttpClient client = new DefaultHttpClient();
 				HttpGet httpGet = new HttpGet(dist_url);
 				StringBuilder builder = new StringBuilder();
 				try {
 					//Similar to xhr in js
 					
 					HttpResponse response = client.execute(httpGet);
 					StatusLine statusLine = response.getStatusLine();
 					int statusCode = statusLine.getStatusCode();
 					if (statusCode == 200) {
 						HttpEntity entity = response.getEntity();
 						InputStream content = entity.getContent();
 						BufferedReader reader = new BufferedReader(new InputStreamReader(content));
 						String line;
 						while ((line = reader.readLine()) != null) {
 							builder.append(line);
 						}
 					} else {
 						return -1;
 						//Returns FAIL on all errors. Else returns the JSON String
 					}
 				} catch (ClientProtocolException e) {
 					e.printStackTrace();
 					return -1;
 				} catch (IOException e) {
 					e.printStackTrace();
 					return -1;
 					
 				}
 				try {
 					JSONObject JSON = new JSONObject(builder.toString());
 					JSONObject routes = JSON.getJSONArray("routes").getJSONObject(0);
 					JSONObject legs = routes.getJSONArray("legs").getJSONObject(0);
 					long distance = legs.getJSONObject("distance").getInt("value");
 					Log.d("JSON","DISTANCE IS "+distance);
 					long mins = 15 + distance/333;
 					return mins;
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					return -1;
 				}
 		}
 
 		public void addOverlay(OverlayItem overlay) {
 		      mapOverlays.add(overlay);
 		       this.populate();
 		   }
 	}
