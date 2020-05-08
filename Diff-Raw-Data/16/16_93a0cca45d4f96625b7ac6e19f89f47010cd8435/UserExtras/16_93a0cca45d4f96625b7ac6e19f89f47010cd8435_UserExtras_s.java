 package com.Ticketline.Ticketline;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.R.color;
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class UserExtras extends Activity{
 	String Name;
 	Dialog myDialog;
 	Dialog myDialog1;
 	Functions f;
 	String response;
 	ListView lv;
 	Button yourDetails;
 	Button payment;
 	Button billing;
 	Button history;
 	TextView nohistory;
 	Typeface first;
 	Typeface folksFont;
 	
 	ArrayList<String>idArray = new ArrayList<String>();
 	ArrayList<String>eventArray = new ArrayList<String>();
 	ArrayList<String>venueArray = new ArrayList<String>();
 	ArrayList<String>dateArray = new ArrayList<String>();
 	ArrayList<String>qtArray = new ArrayList<String>();
 	ArrayList<String>totalArray = new ArrayList<String>();
 	ArrayList<String>statusArray = new ArrayList<String>();
 	
 	ArrayList<History>hisArray = new ArrayList<History>();
 	
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.user_extras);
         Name = getIntent().getStringExtra("username");
         
         lv = (ListView)findViewById(R.id.lstHistory);
         folksFont = Typeface.createFromAsset(this.getAssets(),"Folks-Normal-webfont.ttf");
         first = Typeface.createFromAsset(this.getAssets(),"Arial.ttf");
         yourDetails = (Button)findViewById(R.id.Button01);
     	payment = (Button)findViewById(R.id.Button02);
         billing = (Button)findViewById(R.id.Button03);
         history = (Button)findViewById(R.id.btnHistory);
         nohistory = (TextView)findViewById(R.id.txtError);
     	  
         yourDetails.setTypeface(first);
         payment.setTypeface(first);
         billing.setTypeface(first);
         history.setTypeface(first);
         nohistory.setTypeface(first);
         f = new Functions();
         grabURL("");
         
 	}
 	
 	public void Profile(View button){
 		Intent PR = new Intent(getParent(),UserProfile.class);
 		PR.putExtra("username",Name );
  		TabGroupActivity parentActivity = (TabGroupActivity)getParent();
  		parentActivity.startChildActivity("UserProfileActivity", PR);
 	}
 	public void Messages(View button){
 		Intent MSG = new Intent(getParent(),UserMessages.class);
 		MSG.putExtra("username",Name );
  		TabGroupActivity parentActivity = (TabGroupActivity)getParent();
  		parentActivity.startChildActivity("UserMessagesActivity", MSG);
 	}
 	
 	public void Payment(View button){
 		Intent PMT = new Intent(getParent(),UserPayment.class);
 		PMT.putExtra("username",Name );
  		TabGroupActivity parentActivity = (TabGroupActivity)getParent();
  		parentActivity.startChildActivity("UserMessagesActivity", PMT);
 	}
 	
 	 public void grabURL(String url) {
 	        new GrabURL().execute(url);
 	    }
 	    
 	    private class GrabURL extends AsyncTask<String, Void, Void> {
 	     
 	       
 	        
 	        protected void onPreExecute() {
 
 	        	 showLoadDialog("Loading...");
 	        }
 
 	        protected Void doInBackground(String... urls) {
 	        	
 	       response =  f.getOrderHistory();
 				return null;
 	        	
 	       
 	            
 	            
 	        }
 
 			protected void onPostExecute(Void unused) {
 	        	//arImage.setImageDrawable(drawable);
 				myDialog.dismiss();
 				try {
 					validateResponse(response);
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 				
 	        }
 	        
 	    }
 	    
 	    public void validateResponse(String res) throws JSONException{
 	    	
 	    	 JSONObject jArray = new JSONObject(res);
 	           JSONArray  earthquakes = jArray.getJSONArray("categories");
 	           
 	          
 	           if(earthquakes.toString().equalsIgnoreCase("[]")){
 	        	   Log.i("empty",earthquakes.toString()); 
 	        	   lv.setVisibility(View.GONE);
 	        	   nohistory.setVisibility(View.VISIBLE);
 	         	   nohistory.setGravity(Gravity.CENTER);
 	         	   nohistory.setText("No History Available");
 	           }else{
 	        	
 	           for(int i=0;i<earthquakes.length();i++){						
 					JSONObject e = earthquakes.getJSONObject(i);
 					
 					//Log.i("Order id",e.getString("id"));
 					idArray.add(e.getString("id"));
 					Log.i("Session date",e.getString("session_date"));
 					dateArray.add(e.getString("session_date"));
 					//Log.i("EVent name",e.getString("event_name"));
 					eventArray.add(e.getString("event_name"));
 					//Log.i("Venue name",e.getString("venue_name"));
 					venueArray.add(e.getString("venue_name"));
 					//Log.i("Quantity ",e.getString("quantity"));
 					qtArray.add(e.getString("quantity"));
 					//Log.i("Total ", e.getString("value"));
 					totalArray.add(e.getString("value"));
 					statusArray.add(e.getString("status_display_name"));
 					
 					// Question: If all the data is being added in the array below, what is the point in adding it to the arrays above? Would that not be unnecessary memory?
 					hisArray.add(  new History (e.getString("id"),e.getString("session_date"),e.getString("event_name"),e.getString("venue_name"),e.getString("quantity"),e.getString("value"),e.getString("status_display_name") ));
 							
 							
 	           }
 	           
 	           
 	           Collections.sort(hisArray, new Comparator<History>() {  
  
 	               @Override  
 	               public int compare(History lhs, History rhs) { 
 	            	   
 	                   Date date1 = stringToDate(lhs.date);  
 	                   Date date2 = stringToDate(rhs.date);  
 	                   if (date1.before(date2)) {  
 	                       return 1;  
 	                   }  
 	                   return -1;  
 	               }  
 	           });
 	           
 	           int div = 0;
 	           
 	           while(div < hisArray.size())
 	           {
	        	   if(stringToDate(hisArray.get(div).date).before(new Date()))
 	        		   break;
 	        	   else
 	        	   {
 	        		   div++;
 	        	   }
 	           }
 	           
 	           final int divider = div;
 	           
 	           lv.setAdapter(new HistoryAdapter(getParent(), R.layout.cusrow, eventArray, hisArray, first, folksFont, divider)
 	           {
 	        	   public boolean areAllItemsEnabled() 
 	               { 
 	                       return false; 
 	               } 
 	               public boolean isEnabled(int position) 
 	               {
 	            	   if(position == divider)
 	                       return false;
 	            	   else
 	            		   return true;
 	               } 
 	           });
 	           lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 	               public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 							long arg3) {					
 						if(arg2 != divider)
 						{
 					        Intent eve = new Intent(getApplicationContext(), HistoryDetails.class);
 					        if(arg2 >= divider)
 					        	eve.putExtra("order-id", idArray.get(arg2 - 1));
 					        else
 					        	eve.putExtra("order-id", idArray.get(arg2));
 					        startActivity(eve);
 						}
 					}
 		        });
 	           }//end if statement
 	    }
 	    
 	    public void showLoadDialog(String n){
 	    	
 	   	 myDialog = new Dialog(getParent());
 	   	 myDialog.getWindow().setFlags( 
 	   				WindowManager.LayoutParams.FLAG_BLUR_BEHIND, 
 	   				WindowManager.LayoutParams.FLAG_BLUR_BEHIND); 
 	   		myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 	   		myDialog.setContentView(R.layout.load_dialog);
 	   		
 	   		TextView tv = (TextView)myDialog.findViewById(R.id.txtLoad);
 	   		tv.setText(n);
 	   		myDialog.show();
 	   }
 	    
 	    public static Date stringToDate(String dateString) {  
 	        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");  
 	        Date dateValue = null;
 			try {
 				dateValue = simpleDateFormat.parse(dateString);
 			} catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}  
 	        return dateValue;  
 	    }	    
  	    
 }
