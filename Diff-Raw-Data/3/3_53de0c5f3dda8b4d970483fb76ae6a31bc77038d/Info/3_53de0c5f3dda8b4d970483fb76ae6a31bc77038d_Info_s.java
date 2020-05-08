 package com.Ticketline.Ticketline;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.graphics.Typeface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnLongClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.webkit.WebView;
 import android.webkit.WebView.HitTestResult;
 import android.widget.TextView;
 
 public class Info extends Activity {
 	
 	Functions f;
 	Dialog myDialog;
 	String data;
 	String title;
 	TextView topic;
 	WebView tv1;
 	Typeface arialFont;
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.moreinfo_dialog);
         f = new Functions();
         tv1 = (WebView)findViewById(R.id.txtHtml);
        // tv1.setBackgroundColor(Color.BLACK);
         topic = (TextView)findViewById(R.id.txtTopic);
         /* DAZ 18/01/13 : I split the title into its individual characters, 
          * change the first character to uppercase and then set textview to the new
          * title */
         title = getIntent().getStringExtra("type");
         arialFont = Typeface.createFromAsset(this.getAssets(),"Arial.ttf");
         topic.setTypeface(arialFont);
         setTitle();
         grabURL("");
         
         tv1.setOnLongClickListener(new OnLongClickListener() {  
         	  
             @Override  
             public boolean onLongClick(View v) {  
                 final HitTestResult hitTestResult = ((WebView) v)  
                         .getHitTestResult(); 
                 Log.e("LONG CLICK", "getExtra = "+ hitTestResult.getExtra() + "\t\t Type=" + hitTestResult.getType());
                 
                 if (hitTestResult.getType() == HitTestResult.PHONE_TYPE  ) {
                         
                           
                     Log.e("INFO", ""  + hitTestResult.getExtra().toString());
                              
                 }  
                 return true;  
             }
 
 
         });
 	}
 	
 	private void setTitle() {
 		
 		if (!title.equals("faq")) {
     		char[] stringArray = title.toCharArray();
     		stringArray[0] = Character.toUpperCase(stringArray[0]);
     		title = new String(stringArray);
     		topic.setText(title);
     		
 		} else {
 			
 			topic.setText(title.toUpperCase());
 			
 		}
 		
 	}
 
 	public void parseMoreinfoData(String response) throws JSONException {
 		
 		JSONObject jArray = new JSONObject(response);
 	    JSONArray  earthquakes = jArray.getJSONArray("categories");
 	 	
 	    for(int i=0;i<earthquakes.length();i++){						
 		JSONObject e = earthquakes.getJSONObject(i);
 				
 		     	
 			 data = e.getString("html");
 			 data = data.replaceAll("<span class=\"phoneNumber\">(.*)</span>", "<a href=\"tel:$1\">$1</a>");
			 
 		 Log.i("Info data",data);
 		
 	    }
 	}
 	    
 	 public void grabURL(String url) {
 	        new GrabURL().execute(url);
 	    }
 	    
 	    private class GrabURL extends AsyncTask<String, Void, Void> {
 	     
 	       
 	        
 	        protected void onPreExecute() {
 	     
 	        	 showLoadDialog("Loading...");
 	        }
 
 	        protected Void doInBackground(String... urls) {
 	        	
 	        	Log.i("Parse type",getIntent().getStringExtra("type"));
 					try {
 						parseMoreinfoData(f.getMoreInfo(getIntent().getStringExtra("type")));
 					} catch (JSONException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 			
 	     
 			return null;
 	            
 	            
 	        }
 	        
 	        
 			protected void onPostExecute(Void unused) {
 				
 				tv1.loadDataWithBaseURL(null, data,"text/html", "utf-8", null);
 				Log.i("onPostExecute data",data);
 	        	myDialog.dismiss();
 	            
 	        }
 	        
 	    }
 	    
 	    public void showLoadDialog(String n){
 	    	
 	   	 myDialog = new Dialog(this);
 	   	 myDialog.getWindow().setFlags( 
 	   				WindowManager.LayoutParams.FLAG_BLUR_BEHIND, 
 	   				WindowManager.LayoutParams.FLAG_BLUR_BEHIND); 
 	   		myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 	   		myDialog.setContentView(R.layout.load_dialog);
 	   		
 	   		TextView tv = (TextView)myDialog.findViewById(R.id.txtLoad);
 	   		tv.setText(n);
 	   		
 	   		myDialog.show();
 	   }
 }
