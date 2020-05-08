 package com.cobone;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.cobone.library.ImageLoader;
 import com.cobone.library.LazyAdapter;
 
 public class AndroidList extends ListActivity {
 	SharedPreferences sharedPreferences;
 	String[] items;
     LazyAdapter adapter;
 	public class MyCustomAdapter extends ArrayAdapter<HashMap<String, Object>> {
 		private final List<HashMap<String, Object>> list;
 		private final Activity context;
 	    private String[] data;
 	    public ImageLoader imageLoader; 
 		public MyCustomAdapter(Activity  context, int textViewResourceId,
 				List<HashMap<String, Object>> list , String[] images) {
 			super(context, textViewResourceId, list);
 			this.context = context;
 			this.list = list;
 			data=images;
 	        imageLoader=new ImageLoader(context.getApplicationContext());
 			// TODO Auto-generated constructor stub
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			// TODO Auto-generated method stub
 			//return super.getView(position, convertView, parent);
 			View view = null;
 			if (convertView == null) {
 				LayoutInflater inflator = context.getLayoutInflater();
 				view = inflator.inflate(R.layout.dealbrief, null);
 				}
 			else {
 				view = convertView;
 			}			
 			TextView title=(TextView)view.findViewById(R.id.item_title);
 			title.setText((CharSequence) list.get(position).get("title"));
 			TextView price=(TextView)view.findViewById(R.id.textView2);
 			price.setText((CharSequence) list.get(position).get("price"));
 			ImageView imgView=(ImageView)view.findViewById(R.id.imageView1);
 //	         Drawable drawable = JSONfunctions.LoadImageFromWebOperations(list.get(position).get("photoUrl"));
 	         imgView.setImageDrawable((Drawable) list.get(position).get("photoUrl"));
 	         ImageView image=(ImageView)view.findViewById(R.id.imageView1);
 	         //text.setText("item "+position);
 	         imageLoader.DisplayImage(data[position], image);
 			return view;
 		}
 	}
 //	String[] DayOfWeek = {"Sunday", "Monday", "Tuesday",
 //			"Wednesday", "Thursday", "Friday", "Saturday"
 //	};
 	
 	
 	
 	
 //class AddStringTask extends AsyncTask<Void, String, Void> {
 //  @Override
 //  protected Void doInBackground(Void... unused) {
 //    for (String item : items) {
 //      publishProgress(item);
 //      SystemClock.sleep(200);
 //    }
 //    
 //    return(null);
 //  }
 //  
 //  @SuppressWarnings("unchecked")
 //  @Override
 //  protected void onProgressUpdate(String... item) {
 //	Drawable drawable = JSONfunctions.LoadImageFromWebOperations(item[0]);
 //	List<HashMap<String, Object>> mylist = new ArrayList<HashMap<String, Object>>();				
 //	HashMap<String, Object> map = new HashMap<String, Object>();
 // 	map.put("photoUrl",drawable);
 // 	mylist.add(map);
 // 	((ArrayAdapter<String>)getListAdapter()).;
 //  }
 //  
 //  @Override
 //  protected void onPostExecute(Void unused) {
 //    Toast
 //      .makeText(AndroidList.this, "Done!", Toast.LENGTH_SHORT)
 //      .show();
 //  }
 //}
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.deallist);
         sharedPreferences=getSharedPreferences("MYPRef", 0);
         final SharedPreferences.Editor editor=sharedPreferences.edit();
         List<HashMap<String, Object>> mylist = new ArrayList<HashMap<String, Object>>();
 	    String dealValue="";
 	    String dealPrice="";
 	    String dealCurrency="";	    
 	    String searchUrl="http://192.168.1.2:8080/dealAPP/searchdeals.htm?cityName="+sharedPreferences.getString("cityName","Riyadh")+"&siteName="+sharedPreferences.getString("siteName","Cobone");
         JSONObject json = JSONfunctions.getJSONfromURL(searchUrl);       
         try{     	
         	JSONArray  earthquakes = json.getJSONArray("list"); 
         	Toast.makeText(AndroidList.this, "deals size"+earthquakes.length(), Toast.LENGTH_LONG).show();
         	items = new String[earthquakes.length()];
         	for(int i=0;i<earthquakes.length();i++){						
 				HashMap<String, Object> map = new HashMap<String, Object>();	
 				JSONObject e = earthquakes.getJSONObject(i);
 				dealCurrency= e.getString("currency");
 				dealPrice=String.format("%.0f", Double.parseDouble(e.getString("price")));
 				dealValue=String.format("%.0f", Double.parseDouble(e.getString("value")));
 				map.put("id",  e.getString("id"));
 	        	map.put("title",e.getString("title"));
 	        	map.put("end",e.getString("end"));
 	        	map.put("price",dealPrice+" "+ dealCurrency+" ("+dealValue+" "+ dealCurrency+")");       	
 //	        	map.put("photoUrl",e.getString("photo"));
 	        	  //Drawable drawable = JSONfunctions.LoadImageFromWebOperations(e.getString("photo"));
 	        	 // map.put("photoUrl",drawable);
 	        	items[i] = e.getString("photo");
 	        	  mylist.add(map);
         	}		
         	//list=(ListView)findViewById(R.id.list);
             
 //        	new AddStringTask().execute();
         }catch(JSONException e)        {
         	 Log.e("log_tag", "Error parsing data "+e.toString());
         }
 //      requestWindowFeature(Window.FEATURE_PROGRESS);
          
        
         setListAdapter(new MyCustomAdapter(AndroidList.this, R.layout.dealbrief, mylist ,items));
         final ListView lv = getListView();
        
         lv.setTextFilterEnabled(true);	
         lv.setOnItemClickListener(new OnItemClickListener() {
         	public void onItemClick(AdapterView<?> parent, View view, int position, long id) { 
 //        		Toast.makeText(AndroidList.this, "we get deal to you", Toast.LENGTH_SHORT).show(); 
         		@SuppressWarnings("unchecked")
 				HashMap<String, String> o = (HashMap<String, String>) lv.getItemAtPosition(position);	
         		editor.putString("dealId",o.get("id")); 
         		 editor.commit();
     			Intent intent = new Intent(AndroidList.this,DealActivity.class);
 
     			startActivity(intent);
     			
 			}
 		});
        
 //		setProgressBarVisibility(true);
     }
 
 
 }
