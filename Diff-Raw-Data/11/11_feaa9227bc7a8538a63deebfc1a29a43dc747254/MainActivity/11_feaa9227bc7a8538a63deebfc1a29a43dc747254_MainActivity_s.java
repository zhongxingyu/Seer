 package com.aanchal.youtubemysongs;
 
 import com.aanchal.youtubemysongs.Song;
 import com.aanchal.youtubemysongs.DeveloperKey;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ResolveInfo;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.net.ConnectivityManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.text.Editable;
 import android.text.Spannable;
 import android.text.SpannableStringBuilder;
 import android.text.TextWatcher;
 import android.text.style.ForegroundColorSpan;
 import android.text.style.RelativeSizeSpan;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.EditText;
 import android.widget.Filter;
 import android.widget.Filterable;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Toast;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import com.aanchal.youtubemysongs.R;
 import com.google.android.youtube.player.YouTubeInitializationResult;
 import com.google.android.youtube.player.YouTubeStandalonePlayer;
 
 
 public class MainActivity extends Activity {
 
 	static final int MAX_QUERY_SONGS = 5;
 	private static final String YOUTUBE_VIDEO_INFORMATION_URL = "http://www.youtube.com/get_video_info?&video_id=";
 
 	ListView musiclist;
     Cursor musiccursor;
     Song querySong;
     List<Song> allSongs;
     List<Song> currentSongs;
     int count;
     private ConnectivityManager cm;
     Activity myself;
     EditText inputSearch;
     MusicAdapter adapter;
     private ProgressDialog progressDialog; 
 	
     
     private TextWatcher searchTextWatcher = new TextWatcher() {
        @Override
             public void onTextChanged(CharSequence s, int start, int before, int count) {
                 // ignore
             }
 
            @Override
             public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                 // ignore
             }
 
            @Override
             public void afterTextChanged(Editable s) {
                 adapter.getFilter().filter(s.toString());
             }
         };
     
     class Process extends AsyncTask<Object, Void, String> {
 		
 		 
 		 @Override
 	        protected void onPreExecute()
 	        {
 	            super.onPreExecute();   
 	            progressDialog = ProgressDialog.show(MainActivity.this, null, "Automagic search in progress...", true, false); 
 	        }
 
 	        @Override
 	        protected String doInBackground(Object... param) {
 	        	return getVideoIdForSong(querySong);
         	 }
 	        
 	        private boolean canResolveIntent(Intent intent) {
 	            List<ResolveInfo> resolveInfo = getPackageManager().queryIntentActivities(intent, 0);
 	            return resolveInfo != null && !resolveInfo.isEmpty();
 	        }
 
 	        @Override
 	        protected void onPostExecute(String result)
 	        {
 	        	if (progressDialog != null)
 	        		progressDialog.dismiss();
 	            super.onPostExecute(result);	            
 	        	if (result == null) 
 		        	   Toast.makeText(myself, "Sorry! No video found :(", Toast.LENGTH_SHORT).show();
 	        	else {
              		Intent intent = YouTubeStandalonePlayer.createVideoIntent(
              	         myself , DeveloperKey.DEVELOPER_KEY, result,0, true, false);
              		if (intent == null || !canResolveIntent(intent))
              			YouTubeInitializationResult.SERVICE_MISSING.getErrorDialog(myself, 2).show();
              		else
              			startActivity(intent);
 		        }    
 	        }
 	}
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
           setContentView(R.layout.mainactivity);
           cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
           myself = this;
           inputSearch = (EditText) findViewById(R.id.inputSearch);
           inputSearch.addTextChangedListener(searchTextWatcher);
           init_phone_music_grid();    
       }
     
     @Override
     public void onPause() {
         super.onPause();
 
         if(progressDialog != null)
             progressDialog.dismiss();
         progressDialog = null;
     }
     
     @SuppressWarnings("deprecation")
 	private void init_phone_music_grid() {
           System.gc();
           String[] proj = { MediaStore.Audio.Media._ID,MediaStore.Audio.Media.DISPLAY_NAME,MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ALBUM };
           musiccursor = managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,proj, null, null, null);
           // TODO: Check if musiccursor is null
           count = musiccursor.getCount();
           int titleCol = musiccursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
           int artistCol = musiccursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
           int albumCol = musiccursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
           allSongs = new ArrayList<Song>();
           currentSongs = new ArrayList<Song>();
           musiccursor.moveToFirst();
           for(int i = 0; i < count; ++i, musiccursor.moveToNext()) { 
         	  allSongs.add(new Song(musiccursor.getString(titleCol), musiccursor.getString(albumCol), musiccursor.getString(artistCol)));
            	  currentSongs.add(new Song(musiccursor.getString(titleCol), musiccursor.getString(albumCol), musiccursor.getString(artistCol)));
           }
           musiclist = (ListView) findViewById(R.id.PhoneMusicList);
           adapter = new MusicAdapter(getApplicationContext());
           musiclist.setAdapter(adapter);
           musiclist.setOnItemClickListener(musicgridlistener);
     }
     
 	// returns query results in JSON form
 	private static JSONArray getResults(String query) {
 		if (query.contains("<unknown>"))
 			return null;
 		try {
 		// TODO: Get short and medium duration only, exclude long videos
 		String url="http://gdata.youtube.com/feeds/api/videos?q="+query+"&max-results="+MAX_QUERY_SONGS+"&v=2&format=5&alt=jsonc";
 		URL jsonURL = new URL(url);
 		URLConnection jc = jsonURL.openConnection();
 		InputStream is = jc.getInputStream();
 		String jsonTxt = IOUtils.toString( is );
 		JSONObject jj = new JSONObject(jsonTxt);
 		JSONObject jdata = jj.getJSONObject("data");
 		int totalItems = Math.min(MAX_QUERY_SONGS,jdata.getInt("totalItems"));
 		JSONArray aitems = null;
 		if (totalItems > 0)
 			aitems = jdata.getJSONArray("items");
 		return aitems;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 	
 	// given a song, retrieve the youtube id, returns null if no playable video is found
 	private static String getVideoIdForSong(Song song) {
 		JSONArray[] results = new JSONArray[3];
 		results[0] = getResults(song.getAlbumQueryString());
 		results[1] = getResults(song.getArtistQueryString());
 		if (results[0] == null && results[1] == null)
 			results[2] = getResults(song.getTitleQueryString());
 		else
 			results[2] = getResults(song.getArtistAlbumQueryString());
 		int[] indexes = new int[3];
 		indexes[0] = indexes[1] = indexes[2] = 0;
 		while(true) {
 			Boolean exhausted = true;
 			for(int i = 0; i < 3; ++i) 
 				if (results[i] != null && indexes[i] < results[i].length()) {
 					try {
 					  JSONObject item0 = results[i].getJSONObject(indexes[i]);
 					  indexes[i]++;
 					  String ret = item0.getString("id");
 					  HttpClient lClient = new DefaultHttpClient();
 					  HttpGet lGetMethod = new HttpGet(YOUTUBE_VIDEO_INFORMATION_URL + ret);
 					  HttpResponse lResp = null;
 					  lResp = lClient.execute(lGetMethod);
 					  ByteArrayOutputStream lBOS = new ByteArrayOutputStream();
 					  lResp.getEntity().writeTo(lBOS);
 					  String lInfoStr = new String(lBOS.toString("UTF-8"));
 					  if (!lInfoStr.contains("fail"))
 						  return ret;
 					  exhausted = false;
 					} catch (Exception e) {
 						// do nothing, continue
 					}
 					  
 				}
 			if (exhausted)
 				break;
 		}
 		return null;
 	}
 
     private OnItemClickListener musicgridlistener = new OnItemClickListener() {
           public void onItemClick(AdapterView parent, View v, int position,long id) {
         	  try {
                     //Upon clicking on a song name this will retrieve MAX_QUERY_SONGS number of songs from youtube and play the top result.
                     System.gc();
                     querySong = currentSongs.get(position);
                   	if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected())
                   		new Process().execute(null,null,null); 
                   	else 
                   		Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_LONG).show();
              
                   } catch (Exception e) {e.printStackTrace();}
           }
     };
 
     public class MusicAdapter extends BaseAdapter implements Filterable {
       private Context mContext;
 
       public MusicAdapter(Context c) {
         mContext = c;
       }
 
       public int getCount() {
         return currentSongs.size();
       }
 
       public Object getItem(int position) {
         return position;
       }
 
       public long getItemId(int position) {
         return position;
       }
 
       public View getView(int position, View convertView, ViewGroup parent) {
         System.gc();
         TextView tv;
         if (convertView == null) {
           tv = new TextView(mContext.getApplicationContext());
         } else{
           tv = (TextView) convertView;
         }
         tv.setTextSize(12);
         Song song = currentSongs.get(position);
         int titleLength = song.title.length();
         int artistLength = song.artist.length();
         SpannableStringBuilder stringBuilder = new SpannableStringBuilder(song.title + "\n"+song.artist);
         stringBuilder.setSpan(new RelativeSizeSpan(1.5f), 0, titleLength,Spannable.SPAN_INCLUSIVE_INCLUSIVE);
         stringBuilder.setSpan(new ForegroundColorSpan(Color.rgb(135, 206, 250)), titleLength + 1, titleLength + artistLength +1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
         tv.setText(stringBuilder);
         return tv;
       }
 
	@Override
 	public Filter getFilter() {
 		return new Filter() {
 			  @SuppressWarnings("unchecked")
 	            @Override
 	            protected void publishResults(CharSequence constraint, FilterResults results) {
 				  // Now we have to inform the adapter about the new list filtered
 				    if (results.count == 0)
 				        notifyDataSetInvalidated();
 				    else {
 				        currentSongs = (List<Song>) results.values;
 				        notifyDataSetChanged();
 				    }
 				}
 
 	            @Override
 	            protected FilterResults performFiltering(CharSequence constraint) {
 	            	 FilterResults results = new FilterResults();
 	            	    // We implement here the filter logic
 	            	    if (constraint == null || constraint.length() == 0) {
 	            	        // No filter implemented we return all the list
 	            	        results.values = allSongs;
 	            	        results.count = allSongs.size();
 	            	    }
 	            	    else {
 	            	        // We perform filtering operation
 	            	        List<Song> filteredSongs = new ArrayList<Song>();
 	            	        String searchString = constraint.toString().toLowerCase();
 	            	        for (Song song : allSongs) {
 	            	        	if (song.title.toLowerCase().contains(searchString) ||
 	            	        		song.artist.toLowerCase().contains(searchString))
 	            	                filteredSongs.add(song);
 	            	        }
 	            	         
 	            	        results.values = filteredSongs;
 	            	        results.count = filteredSongs.size();
 	            	 
 	            	    }
 	            	    return results;
 	            }
 		};
 	}
     }
 }
 
 
 
