 package android.stickynotes;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.IntentFilter.MalformedMimeTypeException;
 import android.location.Location;
 import android.location.LocationManager;
 import android.net.wifi.WifiManager;
 import android.nfc.NdefMessage;
 import android.nfc.NfcAdapter;
 import android.nfc.Tag;
 import android.nfc.tech.Ndef;
 import android.nfc.tech.NdefFormatable;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class FoursqActivity extends Activity {
 
 	 int selectedIndex = -1;
 	 String returnString;
 	 JSONArray venuesCopy;
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 		//StickyNotesActivity sna = parent;
         super.onCreate(savedInstanceState);
         setContentView(R.layout.foursqview);
        
         LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);    
 //       Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);  
       Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);  
 
         if(location == null){
             toast("Sorry, could not fetch your location");
         }
         else {        
         
 		String clientID = "J5PQXMEG2EBIBL0ZCD5NDKWIXAMEGDKIMNGDBBG5FPGJEP55";
 		String clientSecret = "CH3I40KL4DLDG5DDPV0H0DMURIC3ABZFSAV5GQK5AAMXM143";
 		//^ This is very secure
 
 		HttpClient client = new DefaultHttpClient();
 		
 		String url = "https://api.foursquare.com/v2/venues/search?ll=";
 		url += "" + location.getLatitude();
 		url += "," + location.getLongitude();
 		String credentials = "&client_id=J5PQXMEG2EBIBL0ZCD5NDKWIXAMEGDKIMNGDBBG5FPGJEP55&client_secret=CH3I40KL4DLDG5DDPV0H0DMURIC3ABZFSAV5GQK5AAMXM143&v=20120222&radius=400";
 		url += credentials;
 		//HttpGet httpGet = new HttpGet(url);
 		
 		String returnString = null;
 		
 		try {
 			InputStream content = null;
 			HttpResponse response = client.execute(new HttpGet(url));
 			content = response.getEntity().getContent();
 
 				String test = convertStreamToString(content);
 				try {
 					JSONObject obj = new JSONObject(test);
 					JSONObject response1 = obj.getJSONObject("response");
 					JSONArray venues = response1.getJSONArray("venues");
 					venuesCopy = venues;
 					String[] names = new String[venues.length()];
 					
 					for (int i = 0; i < venues.length(); i++)
 					{
 						JSONObject temp = venues.getJSONObject(i);
 						String tempStr = temp.getString("name");
 						names[i] = tempStr;
 					}
 					
 					ListView listview = (ListView) findViewById(R.id.mylist);
 					TextView v = new TextView(this);
 					v.setText("Select a foursquare venue to write to tag");
 					listview.addHeaderView(v);
 					ArrayAdapter<String> adapter = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, android.R.id.text1, names);   
 					listview.setAdapter(adapter);
 					
 					listview.setOnItemClickListener(new OnItemClickListener() {
 						@Override
 						public void onItemClick(AdapterView<?> parent, View arg1,
 								int arg2, long arg3) {
 							selectedIndex = arg2-1;
 							try 
 							{
 								JSONObject locSelected = venuesCopy.getJSONObject(arg2-1);
 								String choice = locSelected.toString();
 								String shortURL = locSelected.getString("id");
 								String credentials = "?oauth_token=H2PSZNFJ45H4GKOZVU3F40N0CZCCATWTS1I43YILZJJSGUW5&v=20120308";
 
 								String details = "https://api.foursquare.com/v2/venues/" + shortURL + credentials;
 								try {
 									HttpClient client = new DefaultHttpClient();
 									InputStream content=null;
 									HttpResponse response = client.execute(new HttpGet(details));
 									content = response.getEntity().getContent();
 									String object = convertStreamToString(content);
 									JSONObject o = new JSONObject(object);
 									JSONObject o2 = o.getJSONObject("response");
 									JSONObject o3 = o2.getJSONObject("venue");
 									shortURL = o3.getString("shortUrl"); 
 								}
 								catch (IOException e)
 								{
 									toast(e.toString());
 								}
 								Intent i = new Intent();
 								i.putExtra("fsqstring", shortURL);
 								setResult(RESULT_OK, i);
 								finish();
 							}
 							catch(JSONException e)
 							{
 								toast(e.toString());
 							}
 
 						}
 					});
 				}
 				catch(JSONException e)
 				{
 					toast(e.toString());
 				}
 		}
 		catch (IOException e)
 		{
 			toast("error," + e);
 		}
         }
         
     }
     
     
     private void toast(String text) {
         Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
     }
     
     
     private static String convertStreamToString(InputStream is) {
         /*
          * To convert the InputStream to String we use the BufferedReader.readLine()
          * method. We iterate until the BufferedReader return null which means
          * there's no more data to read. Each line will appended to a StringBuilder
          * and returned as String.
          */
         BufferedReader reader = new BufferedReader(new InputStreamReader(is));
         StringBuilder sb = new StringBuilder();
  
         String line = null;
         try {
             while ((line = reader.readLine()) != null) {
                 sb.append(line + "\n");
             }
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 is.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         return sb.toString();
     }
     
     boolean writeTag(NdefMessage message, Tag tag) {
         int size = message.toByteArray().length;
 
         try {
             Ndef ndef = Ndef.get(tag);
             if (ndef != null) {
                 ndef.connect();
 
                 if (!ndef.isWritable()) {
                     toast("Tag is read-only.");
                     return false;
                 }
                 if (ndef.getMaxSize() < size) {
                     toast("Tag capacity is " + ndef.getMaxSize() + " bytes, message is " + size
                             + " bytes.");
                     return false;
                 }
 
                 ndef.writeNdefMessage(message);
                 toast("Wrote message to pre-formatted tag.");
                 return true;
             } else {
                 NdefFormatable format = NdefFormatable.get(tag);
                 if (format != null) {
                     try {
                         format.connect();
                         format.format(message);
                         toast("Formatted tag and wrote message");
                         return true;
                     } catch (IOException e) {
                         toast("Failed to format tag.");
                         return false;
                     }
                 } else {
                     toast("Tag doesn't support NDEF.");
                     return false;
                 }
             }
         } catch (Exception e) {
             toast("Failed to write tag");
         }
 
         return false;
     }
 
     public String returnStringFunc(String input)
     {
     	returnString = input;
     	return returnString;
     }
     
     public String returnStringFunc()
     {
     	return returnString;
     }
     
     }
     
