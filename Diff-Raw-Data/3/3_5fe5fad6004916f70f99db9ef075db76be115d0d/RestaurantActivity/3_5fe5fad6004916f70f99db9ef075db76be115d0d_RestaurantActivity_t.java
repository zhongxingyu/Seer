 package com.example.grubber;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 @SuppressLint("NewApi")
 public class RestaurantActivity extends Activity {
 
 	public final Context context = this;	
 	private TextView restNameTV;
 	private TextView restAddressTV;
 	private TextView restCityTV;
 	private TextView restPhoneTV;
 	private TextView restWebsiteTV;
 	private ImageView restImageIV;
 	private ListView foodListLV;
 	private Button addBTN;
 	private ProgressDialog progDialog; 
 
 	String rest_id;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_restaurant);
 		getActionBar().setDisplayHomeAsUpEnabled(true);		
 		
 		//***set restaurant name, address, etc
 		//get value pass from previous page
 		//define textview
 		restNameTV = (TextView)findViewById(R.id.restNameTV);
 		restAddressTV = (TextView)findViewById(R.id.restAddressTV);
 		restAddressTV.setClickable(true);
 		restWebsiteTV = (TextView)findViewById(R.id.restWebsiteTV);
 		restWebsiteTV.setClickable(true);
 		restPhoneTV = (TextView)findViewById(R.id.restPhoneTV);
 		restPhoneTV.setClickable(true);
 		restCityTV = (TextView)findViewById(R.id.restCityTV);
 		restImageIV = (ImageView)findViewById(R.id.restImageIV);
 		foodListLV = (ListView)findViewById(R.id.foodListLV);
 		addBTN = (Button)findViewById(R.id.addBTN);
 		
 		//show picture
 		String picurl = "http://maps.googleapis.com/maps/api/streetview?size=150x150&location="+ getIntent().getStringExtra("longitude")+","+getIntent().getStringExtra("latitude") +"&fov=90&heading=235&pitch=10&sensor=false";
 		new DownloadImageTask((ImageView) findViewById(R.id.restImageIV)).execute(picurl);
 		
 		//set the textview
 		restNameTV.setText(getIntent().getStringExtra("name"));
 		restAddressTV.setText(getIntent().getStringExtra("address"));
 		restCityTV.setText(getIntent().getStringExtra("city"));
 		if (getIntent().getStringExtra("website").equals(""))
 			restWebsiteTV.setText("-");
 		else
 			restWebsiteTV.setText(getIntent().getStringExtra("website"));
 		restPhoneTV.setText(getIntent().getStringExtra("phone"));
 		rest_id = getIntent().getStringExtra("rest_id");
 		//***Open navigation app when click on the address
 		restAddressTV.setOnClickListener(new OnClickListener(){
 			public void onClick(View v){
 				String uri = "google.navigation:q="+getIntent().getStringExtra("longitude")+","+getIntent().getStringExtra("latitude");
 			    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
 			    startActivity(i); 
 			}
 		});
 		
 		restWebsiteTV.setOnClickListener(new OnClickListener(){
 			public void onClick(View v){
 			    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(getIntent().getStringExtra("website")));
 			    startActivity(i); 
 			}
 		});
 		
 		restPhoneTV.setOnClickListener(new OnClickListener(){
 			public void onClick(View v){
 			    Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + getIntent().getStringExtra("phone")));
 			    startActivity(i); 
 			}
 		});
 
 		
 		addBTN.setOnClickListener(new OnClickListener(){
 			public void onClick(View v){
 			    Intent i = new Intent(RestaurantActivity.this, NewFoodActivity.class);
 			    i.putExtra("rest_id", rest_id);
 			    startActivity(i); 
 			}
 		});
 		//query to get the top 3 food list
 		try {
 			getFoodList(rest_id);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void onResume() {
     	super.onResume();
     	//Refresh the options menu when this activity comes in focus
     	invalidateOptionsMenu();
     	//this.tracker.trackPageView("/TopTracksActivity");
     }
 
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.restaurant, menu);
                       
         //Change profile button to login/register if they are not logged in
         if(SaveSharedPreference.getUserId(RestaurantActivity.this) == 0)
         {
             MenuItem profileItem = menu.findItem(R.id.action_profile);
         	profileItem.setTitle(R.string.login);
             //Toast.makeText(this,"Not logged in",Toast.LENGTH_SHORT).show();
         }
         else {
         	MenuItem signout = menu.findItem(R.id.action_signout);
         	signout.setVisible(true);
             signout.setOnMenuItemClickListener(new OnMenuItemClickListener() {
             	public boolean onMenuItemClick(MenuItem item) {            		        	
         			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
         			alertDialogBuilder.setTitle(R.string.logout_msg);
         			alertDialogBuilder.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
         				public void onClick(DialogInterface dialog,int id) {            					    							
 							//int tempUserName = SaveSharedPreference.getUserId(context);    			        		
 			        		dialog.cancel();    			        		
 			        		SaveSharedPreference.setUserId(context, 0);
         					Toast.makeText(context , "Logged out" , Toast.LENGTH_SHORT).show();
         					invalidateOptionsMenu();
 						}    						
 					}).setNegativeButton("No", new DialogInterface.OnClickListener() {
     					public void onClick(DialogInterface dialog,int id) {    						
     						dialog.cancel();    					
     					}}
     				  );            		
             		AlertDialog alertDialog = alertDialogBuilder.create();
             		alertDialog.show();   
             		return true;            		
             	} 	
             });        	
         }        
         return true;
       } 					
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId()) {
 	      case R.id.action_profile:
 	    	  if(SaveSharedPreference.getUserId(RestaurantActivity.this) != 0){
 	    		  Intent intent3 = new Intent(context, ProfileActivity.class);
 	    		  startActivity(intent3);   
 	    	  } else {
 	    		  Intent intent3 = new Intent(context, LoginActivity.class);
 	    		  startActivity(intent3);   
 	    	  }
 	          break; 
 	      case android.R.id.home:
 	    	  //NavUtils.navigateUpFromSameTask(this);
 	    	  finish();
 	    	  break;	          
 	      default:
 	    	  break;
       }
 
       return true;
     }
 
 	public void getFoodList(String id) throws Exception {
 		progDialog = ProgressDialog.show( this, "Process ", "Loading Data...",true,true);
 		List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
 		nameValuePair.add(new BasicNameValuePair("rest_id", id));
 		nameValuePair.add(new BasicNameValuePair("min", "0"));
 		nameValuePair.add(new BasicNameValuePair("max", "10"));
 		// url with the post data
 		HttpPost httpost = new HttpPost("http://cse190.myftp.org:8080/cse190/findFood");
 
 		// sets the post request as the resulting string
 		httpost.setEntity(new UrlEncodedFormEntity(nameValuePair));
 		// Handles what is returned from the page
 		//ResponseHandler responseHandler = new BasicResponseHandler();
 		//Log.d("bugs", "execute request");
 		new GetHttpRequest().execute(httpost);
 
 	}
 	private class GetHttpRequest extends AsyncTask<HttpPost, Void, String> {
 
 		@Override
 		protected String doInBackground(HttpPost... params) {
 			DefaultHttpClient httpclient = new DefaultHttpClient();
 			String json = "wrong";
 			try {
 				HttpResponse resp = httpclient.execute(params[0]);
 				BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent(), "UTF-8"));
 				json = reader.readLine();
 				return json;
 			} catch (Exception e) {
 				Log.d("bugs", "Catch in HTTPGETTER");
 			}
 			return null;
 		}
 
 		protected void onPostExecute(String json) {
 			try {
 				getFood(json);
 			} catch (Exception e) { 
 				Log.d("bugs","reader"); 
 				Toast.makeText(RestaurantActivity.this, "Failed to get the data", Toast.LENGTH_SHORT).show();			
 			}
 			//stop progress bar
 			progDialog.dismiss();
 		}
 		
 		protected void getFood(String json)
 		{
             JsonParser parser = new JsonParser();
             JsonObject obj = (JsonObject) parser.parse(json);
             JsonArray jarr = (JsonArray) obj.get("result");
             
 	        final ArrayList<FoodContent> list_result = new ArrayList<FoodContent>();
 	        for (int i = 0; i < jarr.size(); i++) {
 	        	JsonObject result = (JsonObject) jarr.get(i);
 
 	        	//set for adapter value
 	        	list_result.add(new FoodContent(result.get("food_id").getAsString(), result.get("name").getAsString(),
 						  result.get("description").getAsString(), result.get("vote").getAsString()));
 	        }
 	        
 	        FoodAdapter radapter = new FoodAdapter(RestaurantActivity.this, list_result);
 
 	        //Show the food list to ListView
 	        foodListLV.setAdapter(radapter);
 	        foodListLV.setOnItemClickListener(new OnItemClickListener() {
 	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
 	            {//set onClick and open RestaurantActivity page
 	            	Intent intent = new Intent(RestaurantActivity.this, FoodPageActivity.class);
 	            	FoodContent tmp = list_result.get((int) id);
	            	Log.d("bug", rest_id + tmp.getName() + tmp.getDescription() + tmp.getVote());
	            	intent.putExtra("food_id", tmp.getFoodId());
 	            	intent.putExtra("name", tmp.getName());
 	            	intent.putExtra("description", tmp.getDescription());
 	            	intent.putExtra("total_vote", tmp.getVote());
 	        		startActivity(intent);
 	            }
 	        });
 		}
 	}
 	
 	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
 	    ImageView bmImage;
 
 	    public DownloadImageTask(ImageView bmImage) {
 	        this.bmImage = bmImage;
 	    }
 
 	    protected Bitmap doInBackground(String... urls) {
 	        String urldisplay = urls[0];
 	        Bitmap mIcon11 = null;
 	        try {
 	            InputStream in = new java.net.URL(urldisplay).openStream();
 	            mIcon11 = BitmapFactory.decodeStream(in);
 	        } catch (Exception e) {
 	            Log.e("Error", e.getMessage());
 	            e.printStackTrace();
 	        }
 	        return mIcon11;
 	    }
 
 	    protected void onPostExecute(Bitmap result) {
 	        bmImage.setImageBitmap(result);
 	    }
 	}
 }
