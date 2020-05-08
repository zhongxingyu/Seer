 package com.xdev.obliquity;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.GridView;
import android.widget.ImageView;
 
 import com.fedorvlasov.lazylist.ImageLoader;
 import com.google.gson.Gson;
 import com.google.gson.JsonIOException;
 import com.google.gson.JsonSyntaxException;
 import com.google.gson.annotations.SerializedName;
 import com.xdev.obliquity.ActivityAlbumView.ReturnedData.Data;
 
 public class ActivityAlbumView extends Activity{
 	
 	private final static String TAG = Config.TAG_ALBUM_VIEW;
 	private final static boolean DEBUG = Config.DEBUG_ALBUM_VIEW;
 	
 	Util mUtil;
 	Obliquity superState;
 	ImageLoader mImgLoader;
 	Context mContext;
 	
 	Hashtable<String, String> mData;
 	
 	@Override
 	public void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.ac_albumview);
 		
         // Setup local classes
         superState = (Obliquity)getApplication();
         mUtil = superState.getUtil();
         mImgLoader = superState.getImageLoader();
         mContext = this;
         
         // No Internet Access
         if(!mUtil.isOnline())
         	displayError("No Internet Access");
         
         // Loads JSON of the albums
         new LoadJSONTask().execute();
 	}
 	
 	private void jsonDownloaded(ReturnedData data) {
 		if(data == null) {
 			displayError("Download Error");
 			return;
 		}
 		
 		List<String> IDs = new ArrayList<String>();
 		mData = new Hashtable<String, String>();
 		
 		for(Data d:data.data) {
 			if(d.coverPhotoID == null || d.ID == null)
 				continue;
 			
 			IDs.add(d.coverPhotoID);
 			mData.put(d.coverPhotoID, d.ID);
 		}
 		
 		initGridView(IDs);
 	}
 	
 	// Sets up the gridview
 	private void initGridView(List<String> val) {
 		GridView mGridView = (GridView) findViewById(R.id.gridView);
 		mGridView.setAdapter(new ImageAdapter(this, val));
 		
 		mGridView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View v, int arg2,
 					long arg3) {
 				
 				String URL = (String) v.getTag();
 				Intent mIntent = new Intent(mContext, ActivityGalleryGrid.class);
 				mIntent.putExtra("ID", mData.get(URL));
 				startActivity(mIntent);
 				
 			}
 		
 		});
 	}
 	
 	// Displays an error cleans up grid. Display Only Error
 	private void displayError(String error) {
 		
 	}
 	
 	/*
 	 * Image Adapter
 	 * 
 	 */
 	public class ImageAdapter extends BaseAdapter {
 	    private Context mContext;
 	    private List<String> albumIDs;
 	    
 	    public ImageAdapter(Context c, List<String> albumIds) {
 	        mContext = c;
 	        albumIDs = albumIds;
 	    }
 
 	    public int getCount() { return albumIDs.size(); }
 
 	    public long getItemId(int position) { return position; }
 
 	    // create a new ImageView for each item referenced by the Adapter
 	    public View getView(int position, View convertView, ViewGroup parent) {
 //	        ImageView imageView;
 //	        if (convertView == null) {  // if it's not recycled, initialize some attributes
 //	            imageView = new ImageView(mContext);
 //	            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
 //	            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
 //	            imageView.setPadding(8, 8, 8, 8);
 //	        } else {
 //	            imageView = (ImageView) convertView;
 //	        }
 //
 //	        mImgLoader.DisplayImage(buildURL(albumIDs.get(position)), imageView, 85);
 //	        imageView.setTag(albumIDs.get(position));
 //	        return imageView;
 	        
 	        View v;
 	        if (convertView == null) {
 	        	LayoutInflater inflater = (LayoutInflater) mContext
     					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	            v = inflater.inflate(R.layout.album_item, null);
 	        } else {
 	            v = convertView;
 	        }
 
 //	        TextView titleView = (TextView) v.findViewById(R.id.title);
 
 	        GalleryPickerItem iv =
 	                (GalleryPickerItem) v.findViewById(R.id.thumbnail);
 	        mImgLoader.DisplayImage(buildURL(albumIDs.get(position)), iv, 150);
             v.setTag(albumIDs.get(position));
 	        
 //          String title = item.mName + " (" + item.mCount + ")";
 //          titleView.setText(title);	  
             
             return v;
 	    }
 	   
 	   private String buildURL(String ID) {
 		   String format = "https://graph.facebook.com/%s/picture?type=album";
 		   return String.format(format, ID);
 	   }
 
 	@Override
 	public Object getItem(int position) { return albumIDs.get(position); }
 	    
 	}
 	
 	/*
 	 * Classes dealing with downloading the JSON
 	 */
 	private class LoadJSONTask extends AsyncTask<Void, Void, String> {
 		
 		String graphApiURL = "https://graph.facebook.com/LeoMessi/albums?fields=name,cover_photo&limit=100";
 		
 		@Override
 		protected String doInBackground(Void... arg0) {
 			if(DEBUG) Log.i(TAG, "Downloading JSON");
 			
 			int timeoutSocket = Config.TIMEOUT_SOCKET;
 			int timeoutConnection = Config.TIMEOUT_CONN;
 			
 	    	HttpParams httpParameters = new BasicHttpParams();
 	    	HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
 	    	HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
 	    	HttpClient client = new DefaultHttpClient(httpParameters);
 	    	HttpGet getRequest = new HttpGet(graphApiURL);
 
 	    	try {
 	    		HttpResponse getResponse = client.execute(getRequest);
 	    		final int statusCode = getResponse.getStatusLine().getStatusCode();
 
 	    		if(statusCode != HttpStatus.SC_OK) {
 	    			if(DEBUG) Log.w(TAG, "Download Error: " + statusCode + "| for URL: " + graphApiURL);
 	    			//Error in Download
 	    			return null;
 	    		}
 	    		
 	    		String line = "";
 	    		StringBuilder total = new StringBuilder();
 	    		
 	    		HttpEntity getResponseEntity = getResponse.getEntity();
 	    		
 	    		BufferedReader reader = new BufferedReader(new InputStreamReader(getResponseEntity.getContent()));	
 	    		
 	    		while((line = reader.readLine()) != null) {
 	    			total.append(line);
 	    		}
 	    		
 	    		line = total.toString();
 	    		Log.i(TAG, "Downloaded Data : " + line);
 	    		return line;
 	    	
 	    	} catch (IOException e) {
 	    		getRequest.abort();
 	    		if(DEBUG) Log.w(TAG, "IO Exception : " + e.toString());
 	    	} catch (Exception e) {
 	     		if(DEBUG) Log.w(TAG, "Download Exception : " + e.toString());
 	    	}
 	    	
 	    	if(DEBUG) Log.w(TAG, "Download return null");
 	    	return null;
 		}
 	
 		@Override
 		protected void onPostExecute(String result) {
 			if(DEBUG) Log.i(TAG, "Parsing JSON : " + result);
 			Gson gson = new Gson();
 		    
 		    try {
 		    	jsonDownloaded(gson.fromJson(result, ReturnedData.class));
 		    	return;
 		    } 
 		    catch (JsonSyntaxException e) {
 		    	if(DEBUG) Log.w(TAG, "JsonSyntaxException :" + e.toString());
 		    }
 		    catch (JsonIOException e) {
 		    	if(DEBUG) Log.w(TAG, "JsonIOException :" + e.toString());
 		    }
 		    
 		    if(DEBUG) Log.w(TAG, "ParseJson return null");
 		    jsonDownloaded(null);
 		}
 	}
 	
 	public class ReturnedData {
 		
 		public class Data {
 			
 			@SerializedName("name")
 			public String title;
 			
 			@SerializedName("cover_photo")
 			public String coverPhotoID;
 			
 			@SerializedName("id")
 			public String ID;
 			
 			@SerializedName("created_time")
 			public String createdTime;
 		}
 		
 		@SerializedName("data")
 		public List<Data> data;
 		
 		public class Paging {			
 			@SerializedName("next")
 			public String next;
 		}
 	}
 }
