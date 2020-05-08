 package watsons.wine;
 
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
import android.content.pm.ActivityInfo;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class WineListProduct extends Activity {
 	// url to make request
     private static String country_url = "http://watsonwine.bull-b.com/CodeIgniter_2.1.3/index.php/api/list_products_by_country/";
     private static String province_url = "http://watsonwine.bull-b.com/CodeIgniter_2.1.3/index.php/api/list_products_by_province/";
     private static String url = "http://watsonswineiphone.pacim.com/index.php/api/list_products_by_location/10";;
  
     // JSON Node names
     private static final String TAG_PRODUCT = "products";
     private static final String TAG_ID = "id";
     private static final String TAG_NAME = "name";
     private static final String TAG_SIZE = "size";
     private static final String TAG_ORIGINAL_PRICE = "original_price";
     private static final String TAG_PROMOTE_PRICE = "promotional_price";
 //    private static final String TAG_VINTAGE = "vintage";
 //    private static final String TAG_COLOR = "color";
 //    private static final String TAG_GRAPE = "grape";
 //    private static final String TAG_BODY = "body";
 //    private static final String TAG_SWEETNESS = "sweetness";
 //    private static final String TAG_NOTE = "tasting_note";
     private static final String TAG_RP	= "rp";
     private static final String TAG_WS	= "ws";
     private static final String TAG_JH	= "jh";
     private static final String TAG_PHOTO	= "photo";
 //    private static final String TAG_IN_STOCK	= "in_stock";
 //    private static final String TAG_DELETED	= "deleted";
 //    private static final String TAG_RPDDEPT	= "prddept";
 //    private static final String TAG_PRDCLASS	= "prdclass";
 //    private static final String TAG_PRDSUBCLASS	= "prdsubclass";
 //    private static final String TAG_COUNTRY_ID	= "country_id";
 //    private static final String TAG_PROVINCE_ID	= "province_id";
 //    private static final String TAG_FOOD_MATCH	= "food_match";
  
     //  JSONArray
     JSONArray products = null;
     JSONArray provinces = null;
     JSONArray provinces_children = null;
     Context mContext = WineListProduct.this;   
     
     List<String> nameArray = new ArrayList<String>();
     ArrayList<Wine> wineList = new ArrayList<Wine>();
     
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.winelist_product);
 		/* Wine List Tab Content */
 		
 		// Hashmap for ListView
 		final List<Map<String, String>> productList = new ArrayList<Map<String, String>>();
 
         // Creating JSON Parser instance
         JSONParser jParser = new JSONParser();
  
         // Receive Parameter
         Bundle bundle = this.getIntent().getExtras();
         Boolean country = bundle.getBoolean("country");
         String id = bundle.getString("id");
         String name = bundle.getString("name");
      
         // Top TextView
      	TextView nameText = (TextView) findViewById(R.id.name_text);
         
         if (country)
         {
         	nameText.setText(name);
         	url = country_url+id;
         }
         else
         {
         	String countryName = bundle.getString("country_name");
         	nameText.setText(countryName+" | "+name);
         	url = province_url+id;
         }
         // getting JSON string from URL
         JSONObject json = jParser.getJSONFromUrl(url);
                 
         try {
             // Getting Array of Contacts
         	products = json.getJSONArray(TAG_PRODUCT);
             // looping through All Contacts
             for(int i = 0; i < products.length(); i++){
                 JSONObject p = products.getJSONObject(i);
                 
                 // creating new HashMap
                 LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                 // adding each child node to HashMap key => value
                 map.put(TAG_ID, p.getString(TAG_ID));
 //                map.put(TAG_NAME, p.getString(TAG_NAME));
 //                map.put(TAG_SIZE, p.getString(TAG_SIZE));
 //                map.put(TAG_ORIGINAL_PRICE, p.getString(TAG_ORIGINAL_PRICE));
 //                map.put(TAG_PROMOTE_PRICE, p.getString(TAG_PROMOTE_PRICE));
 //                map.put(TAG_VINTAGE, p.getString(TAG_VINTAGE));
 //                map.put(TAG_COLOR, p.getString(TAG_COLOR));
 //                map.put(TAG_GRAPE, p.getString(TAG_GRAPE));
 //                map.put(TAG_BODY, p.getString(TAG_BODY));
 //                map.put(TAG_SWEETNESS, p.getString(TAG_SWEETNESS));
 //                map.put(TAG_NOTE, p.getString(TAG_NOTE));
 //                map.put(TAG_RP, p.getString(TAG_RP));
 //                map.put(TAG_WS, p.getString(TAG_WS));
 //                map.put(TAG_JH, p.getString(TAG_JH));
 //                map.put(TAG_PHOTO, p.getString(TAG_PHOTO));
 //                map.put(TAG_IN_STOCK, p.getString(TAG_IN_STOCK));
 //                map.put(TAG_DELETED, p.getString(TAG_DELETED));
 //                map.put(TAG_RPDDEPT, p.getString(TAG_RPDDEPT));
 //                map.put(TAG_PRDCLASS, p.getString(TAG_PRDCLASS));
 //                map.put(TAG_PRDSUBCLASS, p.getString(TAG_PRDSUBCLASS));
 //                map.put(TAG_COUNTRY_ID, p.getString(TAG_COUNTRY_ID));
 //                map.put(TAG_PROVINCE_ID, p.getString(TAG_PROVINCE_ID));
 //                map.put(TAG_FOOD_MATCH, p.getString(TAG_FOOD_MATCH)); 
  
                 // adding HashList to ArrayList
                 wineList.add(new Wine(
                 		p.getString(TAG_PHOTO),
                 		p.getString(TAG_NAME),
                 		p.getString(TAG_SIZE),
                 		p.getString(TAG_ORIGINAL_PRICE),
                 		p.getString(TAG_PROMOTE_PRICE),
                 		p.getString(TAG_RP),
                 		p.getString(TAG_WS),
                 		p.getString(TAG_JH)));
                 productList.add(map);
             }
         } catch (JSONException e) {
             e.printStackTrace();
         } catch (IOException e) {
 			e.printStackTrace();
 		}   
 
         
 		// Define a new Adapter
 		// First parameter - Context
 		// Second parameter - Layout for the row
 		// Third parameter - ID of the TextView to which the data is written
 		// Forth - the Array of data
         WineAdapter adapter = new WineAdapter(this,R.layout.list_product_item,wineList);
 
 		// Assign adapter to ListView
 		ListView listView = (ListView) findViewById(R.id.list_product);
 		listView.setVerticalFadingEdgeEnabled(false);
 		listView.setAdapter(adapter);
 		listView.setDividerHeight(0);
 		listView.setOnItemClickListener(new OnItemClickListener()
 		{
 			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
 					long arg3) 
 			{
 				Bundle bundle = new Bundle();
 	        	bundle.putBoolean("country", false);
 	        	bundle.putString("id", productList.get(position).get(TAG_ID));
 	        	Intent intent = new Intent(getParent(), WineProductWeb.class);
 	        	TabGroupBase parentActivity = (TabGroupBase)getParent();
 	        	intent.putExtras(bundle);
 	        	parentActivity.startChildActivity("WineProductWeb", intent);
 			}
 		}
 		);
 	}
 	
 	public static Bitmap loadBitmap(String url) throws IOException {
 		Bitmap bitmap = null;
 		try {
 			bitmap = BitmapFactory.decodeStream((InputStream)new URL(url).getContent());
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} 
 		return bitmap;
 	}
 }
