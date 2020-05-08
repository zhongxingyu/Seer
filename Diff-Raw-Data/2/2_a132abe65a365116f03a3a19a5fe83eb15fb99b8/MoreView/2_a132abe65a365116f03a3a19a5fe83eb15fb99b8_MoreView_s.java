 package com.Ticketline.Ticketline;
 
 
 import java.io.File;
 import java.io.InputStream;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.graphics.Typeface;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Display;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 
 public class MoreView extends Activity  {
 	String Images;
 	String url;
 	String arName;
 	String arId;
 	Dialog myDialog;
 	String Id;
 	String name;
 	String slug;
 	ListView recList;
 	final ArrayList<HashMap<String, String>> recmylist = new ArrayList<HashMap<String, String>>();
 	
 	 //declare 10 recommended image views
 	ImageView im1;
 	ImageView im2;
 	ImageView im3;
 	ImageView im4;
 	ImageView im5;
 	ImageView im6;
 	ImageView im7;
 	ImageView im8;
 	
 	TextView name1;
 	TextView name2;
 	TextView name3;
 	TextView name4;
 	TextView name5;
 	TextView name6;
 	TextView name7;
 	TextView name8;
 	
 	String imageName;
 	TextView reco;
 	TextView near;
 	
 	Functions f;
 	Typeface font ;
 	public ArrayList<String> recomendedArray = new ArrayList<String>();
 	public ArrayList<String> setArray = new ArrayList<String>();
 	public ArrayList<String> idsArray = new ArrayList<String>();
 	public ArrayList<String> namesArray = new ArrayList<String>();
 	public ArrayList<String> captionArray = new ArrayList<String>();
 	public ArrayList<String> imageUrl = new ArrayList<String>();
 	public ArrayList<String> imageNames = new ArrayList<String>();
 	public ArrayList<String> recArray = new ArrayList<String>();
 	public ArrayList<String> slugsArray = new ArrayList<String>();
 	JSONArray  earthquakes;
 	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd";
 	
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         Log.e("moreview","working");
         File newxmlfile = new File(Environment.getExternalStorageDirectory()+"/Ticketline/");
         
         if(!newxmlfile.exists()){
 		    newxmlfile.mkdir();
 		}
         
         f =new Functions();
         Display display = getWindowManager().getDefaultDisplay(); 
         int width = display.getWidth();
         int height = display.getHeight();
         
         Log.i("Screen Size",Integer.toString(width)+" "+Integer.toString(height));
         font = Typeface.createFromAsset(this.getAssets(),"Folks-Normal-webfont.ttf");
 
         setContentView(R.layout.home);
 
         im1 = (ImageView)findViewById(R.id.rec1);
         im2 = (ImageView)findViewById(R.id.rec2);
         im3 = (ImageView)findViewById(R.id.rec3);
         im4 = (ImageView)findViewById(R.id.rec4);
         im5 = (ImageView)findViewById(R.id.rec5);
         im6 = (ImageView)findViewById(R.id.rec6);
         im7 = (ImageView)findViewById(R.id.rec7);
         im8 = (ImageView)findViewById(R.id.rec8);
         
         name1 = (TextView)findViewById(R.id.txtName1);
         name2 = (TextView)findViewById(R.id.txtName2);
         name3 = (TextView)findViewById(R.id.txtName3);
         name4 = (TextView)findViewById(R.id.txtName4);
         name5 = (TextView)findViewById(R.id.txtName5);
         name6 = (TextView)findViewById(R.id.txtName6);
         name7 = (TextView)findViewById(R.id.txtName7);
         name8 = (TextView)findViewById(R.id.txtName8);
      
         recList = (ListView)findViewById(R.id.lstRecomended);
         name1.setTypeface(font);
         name2.setTypeface(font);
         name3.setTypeface(font);
         name4.setTypeface(font);
         name5.setTypeface(font);
         name6.setTypeface(font);
         name7.setTypeface(font);
         name8.setTypeface(font);
         
         reco = (TextView)findViewById(R.id.txtRecomended);
         reco.setTypeface(font);
         
         grabURL("");
         
         recArray.add("On Sale Today");
         recArray.add("Coming Soon");
         
       
         
         for(int i=0;i<recArray.size();i++){
         	 HashMap<String, String> map = new HashMap<String, String>();	
     	map.put("name", recArray.get(i));
     	recmylist.add(map);
     	    	
         }
     	
 	       ListAdapter adapter =new SimpleAdapter(this, recmylist , R.layout.venue_list_item, 
 		        	new String[] {"name"} , 
 	                   new int[] { R.id.list_item_title});
 	      recList.setAdapter(adapter);
 	      recList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 						long arg3) {
 					if(arg2==0){
 						Log.i("On sale","On sale today");
 						Intent AR = new Intent(getApplicationContext(),  Events.class);
 						
 						 AR.putExtra("name","On Sale Today" );
 						 AR.putExtra("class","Home");
 						 AR.putExtra("from",now());
 						 AR.putExtra("to",now());
 						 AR.putExtra("method", "onSaleDate");
 				         startActivity(AR);
 				         
 					}else{
 					
 						
 							Intent AR = new Intent(getApplicationContext(),  Events.class);
 							
 							 AR.putExtra("name","Coming Soon" );
 							 AR.putExtra("class","Home");
 							 AR.putExtra("from",tomorrow());
 							 AR.putExtra("to",fiveDays());
 							 AR.putExtra("method", "onSaleDate");
 					         startActivity(AR);
 					}
 				}
 	      });
 
 	}
 	
 	public void request(){
 		
 	        final ArrayList<HashMap<String, String>> mylist = new ArrayList<HashMap<String, String>>();
 
 	        JSONObject json = JSONfunctions.getJSONfromURL("http://api.ticketline.co.uk//recommendation?method=getHighlights&limit=8&api-key=NGNkZGRhYjkzY2Z&on-sale=true");       
 
 	        try{
 	        	
 
 	        	  earthquakes = json.getJSONArray("categories");
 	        	
 		        for(int i=0;i<earthquakes.length();i++){						
 					HashMap<String, String> map = new HashMap<String, String>();	
 					JSONObject e = earthquakes.getJSONObject(i);
 					
 					map.put("id",  String.valueOf(i));
 		        	map.put("name",  e.getString("name"));
 		        	map.put("magnitude",   e.getString("id"));
 		        	mylist.add(map);
 		        	
 		        	idsArray.add(e.getString("id"));
 		        	namesArray.add(e.getString("name"));
 		        	slugsArray.add(e.getString("slug"));
 		        	captionArray.add(e.getString("caption"));
 		        	Images = "{\"images\":"+e.getString("Images")+"}";
 		        	
 		        	//Log.i("Base Url",e.getString("image_base_url"));
 		        	url = e.getString("image_base_url");
 		        	imageName = e.getString("image_default") ;
 		        	
 		        	imageUrl.add(e.getString("image_base_url"));
 		        	imageNames.add(e.getString("image_default"));
 		        	//Log.i("image jason",Images);
 		        	recomendedArray.add(url+imageName);
 		        		//get the images
 		        	 JSONObject imageArray = new JSONObject(Images);
 				        JSONArray  img = imageArray.getJSONArray("images");
 			        	for(int a=0;a<img.length();a++){
 			        		JSONObject ee = img.getJSONObject(a);
 
 			        		try {
 			        			Log.i("Full url",url+ee.getString("filename"));
 
 			    
 			        		} catch (Exception e1) {
 			        		    //do something
 			        		}
 			        	}
 				}		
 	        }catch(JSONException e)        {
 	        	 Log.e("log_tag", "Error parsing data "+e.toString());
 	        }
 	        
 	        for(int i=0;i<recomendedArray.size();i++){
 				Log.i("From Array",i+" "+recomendedArray.get(i));
 			}
 	}
 	
 	public void populate(){
 	    
         Drawable drawable1 = LoadImageFromWeb(recomendedArray.get(0));
 		Drawable drawable2 = LoadImageFromWeb(recomendedArray.get(1));
 		Drawable drawable3 = LoadImageFromWeb(recomendedArray.get(2));
 		
 		Drawable drawable4 = LoadImageFromWeb(recomendedArray.get(3));
 		Drawable drawable5 = LoadImageFromWeb(recomendedArray.get(4));
 		Drawable drawable6 = LoadImageFromWeb(recomendedArray.get(5));
 		Drawable drawable7 = LoadImageFromWeb(recomendedArray.get(6));
 		Drawable drawable8 = LoadImageFromWeb(recomendedArray.get(7));
 		
 		
 			
 		 im1.setImageDrawable(drawable1);
 		 im2.setImageDrawable(drawable2);
 		 im3.setImageDrawable(drawable3);
 		 
 		 
 		 im4.setImageDrawable(drawable4);
 		 im5.setImageDrawable(drawable5);
 		 im6.setImageDrawable(drawable6);
 		 im7.setImageDrawable(drawable7);
 		 im8.setImageDrawable(drawable8);
 		 
 		 
 		
 		 name1.setText(namesArray.get(0));
 		 name2.setText(namesArray.get(1));
 		 name3.setText(namesArray.get(2));
 		 
 		 
 		 name4.setText(namesArray.get(3));
 		 name5.setText(namesArray.get(4));
 		 name6.setText(namesArray.get(5));
 		 name7.setText(namesArray.get(6));
 		 name8.setText(namesArray.get(7));
 		 
 		 
 		 //set the on click actions of the images
 		 setImageClickActions();
 	}
 	
 	public void setImages() {
 		for(int i=0;i<10;i++){
 			Log.i("From Array",recomendedArray.get(i));
 		}
 
 	}
 	
 	public void see(View button){
 
 		Intent AR = new Intent(getApplicationContext(),SeeMore.class);
 		 AR.putStringArrayListExtra("ids",idsArray);
 		 AR.putStringArrayListExtra("names",namesArray);
 		 AR.putStringArrayListExtra("captions",captionArray);
 		 AR.putStringArrayListExtra("images",recomendedArray);
 		 AR.putStringArrayListExtra("urls",imageUrl);
 		 AR.putStringArrayListExtra("imageName",imageNames);
         startActivity(AR);
 	}
 	
 	public void see_two(View button){
 		Intent AR = new Intent(getApplicationContext(),SeeMore.class);
 	        startActivity(AR);
 	}
        
 	 private Drawable LoadImageFromWeb(String url)
 	   {
 	  try
 	  {
 	   InputStream is = (InputStream) new URL(url).getContent();
 	   Drawable d = Drawable.createFromStream(is, "src name");
 	   return d;
 	  }catch (Exception e) {
 	   System.out.println("Exc="+e);
 	   return null;
 	  }
 	 }
        
     public void showDialog(String id,String name){
     	this.arName = name;
     	this.arId = id;
     	 myDialog = new Dialog(getParent());
     	 myDialog.getWindow().setFlags( 
     				WindowManager.LayoutParams.FLAG_BLUR_BEHIND, 
     				WindowManager.LayoutParams.FLAG_BLUR_BEHIND); 
 			myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 			myDialog.setContentView(R.layout.artist_options_dialog);
 			
 			TextView tv = (TextView)myDialog.findViewById(R.id.txtName);
 			tv.setText(name);
 			Button button = (Button)myDialog.findViewById(R.id.btnBio);
 			button.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 			    // TODO Auto-generated method stub
 			   // showBio(arId,arName);
 			}});
 			
 			Button button1 = (Button)myDialog.findViewById(R.id.btnEvents);
 			button1.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 			    // TODO Auto-generated method stub
 			    showEvents(arId,arName);
 			}});
 			
 			myDialog.show();
     }
 
 	public void showBio(String id,String name, String Slug){
 		Log.i("Bio","Bio clicked");
 		this.Id = id;
 		this.name = name;
 		this.slug = Slug;
 		//grabURL("");
 		Intent AR = new Intent(getApplicationContext(),  Tickets.class);
   		 AR.putExtra("Name",name );
   		 AR.putExtra("Slug", slug);
   		 AR.putExtra("artist-id",Id );
   		 AR.putExtra("saved", "false");
            startActivity(AR);
 		
 	}
 	
 	public void showEvents(String id,String name){
 		Log.i("Events","Events clicked");
 		Intent AR = new Intent(getApplicationContext(), Events.class);
 		 AR.putExtra("name",name );
 		 AR.putExtra("artist-id",id );
 		 
 		 AR.putExtra("method","getByArtist" );
         startActivity(AR);
          myDialog.dismiss();
 	}
 	
 	public void setImageClickActions(){
 		 im1.setOnClickListener(new OnClickListener() {
 			    @Override
 			    public void onClick(View v) {
 			    	showBio(idsArray.get(0),namesArray.get(0),slugsArray.get(0));
 			    }
 			});
 		 
 		 im2.setOnClickListener(new OnClickListener() {
 			    @Override
 			    public void onClick(View v) {
 			    	showBio(idsArray.get(1),namesArray.get(1),slugsArray.get(1));
 			    }
 			});
 		 
 		 im3.setOnClickListener(new OnClickListener() {
 			    @Override
 			    public void onClick(View v) {
 			    	showBio(idsArray.get(2),namesArray.get(2),slugsArray.get(2));
 			    }
 			});
 		 
 		 im4.setOnClickListener(new OnClickListener() {
 			    @Override
 			    public void onClick(View v) {
 			    	showBio(idsArray.get(3),namesArray.get(3),slugsArray.get(3));
 			    }
 			});
 		 
 		 im5.setOnClickListener(new OnClickListener() {
 			    @Override
 			    public void onClick(View v) {
 			    	showBio(idsArray.get(4),namesArray.get(4),slugsArray.get(4));
 			    }
 			});
 		 
 		 im6.setOnClickListener(new OnClickListener() {
 			    @Override
 			    public void onClick(View v) {
 			    	showBio(idsArray.get(5),namesArray.get(5),slugsArray.get(5));
 			    }
 			});
 		 
 		 im7.setOnClickListener(new OnClickListener() {
 			    @Override
 			    public void onClick(View v) {
 			    	showBio(idsArray.get(6),namesArray.get(6),slugsArray.get(6));
 			    }
 			});
 		 
 		 im8.setOnClickListener(new OnClickListener() {
 			    @Override
 			    public void onClick(View v) {
 			       showBio(idsArray.get(7),namesArray.get(7),slugsArray.get(7));
 			    }
 			});
 		 
 	
 	}
 	
 	public void grabURL(String url) {
         new GrabURL().execute(url);
     }
     
     private class GrabURL extends AsyncTask<String, Void, Void> {
      
        
         
         protected void onPreExecute() {
         
         	showLoadDialog("Loading...");
         }
 
         protected Void doInBackground(String... urls) {
         	  
         	 request();
         	
 		return null;
             
             
         }
         
         protected void onPostExecute(Void unused) {
         	
         	populate();
         	myDialog.dismiss();
 
           
         }
         
     }
     
     public void showLoadDialog(String n){
     	
       	 myDialog = new Dialog(getParent());
       	 myDialog.getWindow().setFlags( 
       				WindowManager.LayoutParams.FLAG_BLUR_BEHIND, 0); 
       		myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
       		myDialog.setContentView(R.layout.load_dialog);
       		
       		TextView tv = (TextView)myDialog.findViewById(R.id.txtLoad);
       		tv.setText(n);
 
       		myDialog.show();
       }
     
     public static String now() {
         Calendar cal = Calendar.getInstance();
         SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
         return sdf.format(cal.getTime());
 
       }
     
     public static String fiveDays() {
         Calendar nowDay = Calendar.getInstance();
         nowDay.add(Calendar.DATE, 5);
         SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
         
         Log.i("in 5 days",sdf.format(nowDay.getTime()));
         return sdf.format(nowDay.getTime());
 
       }
     
     public static String tomorrow() {
         Calendar nowDay = Calendar.getInstance();
         nowDay.add(Calendar.DATE, 1);
         SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
         
         Log.i("in 5 days",sdf.format(nowDay.getTime()));
         return sdf.format(nowDay.getTime());
 
       }
     
     
     
     
     
     
     @Override
     protected void onDestroy() {
     super.onDestroy();
 
     unbindDrawables(findViewById(R.id.scrollView1));
         System.gc();
     }
 
     private void unbindDrawables(View view) {
         if (view.getBackground() != null) {
         view.getBackground().setCallback(null);
         }
         if (view instanceof ViewGroup) {
             for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                 unbindDrawables(((ViewGroup) view).getChildAt(i));
             }
            if(view instanceof ViewGroup)
             ((ViewGroup) view).removeAllViews();
         }
     }    
 	
 }
