 package me.rohitmishra.groupbanker;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.Window;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 
 public class FriendsList extends Activity  {
 	
 	private static final String TAG = "FriendsList";
 
     protected ListView friendsList;
     protected static JSONArray jsonArray;
     
     private FriendsDbAdapter mDbHelper ;
     
     String FILENAME = "GroupBanker_Preferences";
     private SharedPreferences mPrefs;
     
     protected ProgressBar mProgressBar ;
     
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
      // Calling Indeterminate Progress bar 
         requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS) ;
         
         setContentView(R.layout.fbprogress);
         
         mProgressBar = (ProgressBar) findViewById(R.id.pbFriends) ;
         
         mPrefs = getSharedPreferences(Constants.preferences, 0);
         
         mDbHelper = new FriendsDbAdapter(this);
         mDbHelper.open() ;
         
         Bundle extras = getIntent().getExtras();
         String apiResponse = extras.getString("API_RESPONSE");
         
         GetFriends mGetFriends = new GetFriends() ;
         mGetFriends.execute(apiResponse) ;
                 
     }
     
     
     
     protected class GetFriends extends AsyncTask<String, Integer, Integer>	{
 
 		@Override
 		protected void onPreExecute()	{
 	    	super.onPreExecute() ;
 			FriendsList.this.setProgressBarIndeterminateVisibility(true) ;
 	    }
     	
     	@Override
 		protected Integer doInBackground(String... apiResponse) {
 			
 			//creating a directory in memory card to store the images
 	        
 	        // create a File object for the parent directory
 	        File GroupBankerFriends = new File("/sdcard/GroupBankerFriends/");
 	        
 	        // have the object build the directory structure, if needed.
 	        GroupBankerFriends.mkdirs();
 	        
 	        try {
 	            jsonArray = new JSONArray(apiResponse[0]);
 	            Log.v(TAG, "We have jsonArray  " +  apiResponse) ;
 	            String friendId;
 	            String name ;
 	            String stringUri ;
 
	            mProgressBar.setMax(jsonArray.length());
 	            
 	            //changing the upper limit of the for loop to 5 in order to test the code. original:jsonArray.length()
 	            for (int i = 0; i < 20; i++)	{
 	            	
 	            	JSONObject jsonObject = jsonArray.getJSONObject(i) ;
 	            	name = jsonArray.getJSONObject(i).getString("name");
 	            	final String picURL = jsonObject.getString("pic_square");
 	            	Log.v(TAG, "We have url of picture  " +  picURL) ;
 	            	
 	            	// get StringURI from picURL 
 	            	stringUri = getUriFromURL(picURL, GroupBankerFriends, name) ;
 	            	
 		           //getting fbid for the friend
 		            friendId = jsonArray.getJSONObject(i).getString("uid");
 		            	
 		           //storing the friend to the SQLite database using FriendsDbAdapter
 		            mDbHelper.createFriend(friendId, name, stringUri);
 		            Log.v(TAG, "createFriend called and friend created. i = " + i) ;	
 	            	
 		            publishProgress(i) ;
 	           }
 	       } catch (JSONException e) {
 	          // showToast("Error: " + e.getMessage());
 	            Log.v(TAG, "JSONException : " + e.getMessage()) ;
 	        	return 0;
 	        }
 			
 			return 1;
 		}
     	
     	protected void onProgressUpdate(Integer... progress)	{
     		super.onProgressUpdate(progress[0]);
     		mProgressBar.setProgress(progress[0]);
     	}
     
     	protected void onPostExecute(Integer result)	{
     		Log.v(TAG, "GetFriends onPostExecute");
         	FriendsList.this.setProgressBarIndeterminateVisibility(false);
         	// Set the mFriends shared preference
             SharedPreferences.Editor editor = mPrefs.edit();
             editor.putBoolean("friendsDownloaded", true);
             editor.commit();
             Log.v(TAG, "friendsDownloaded set to true") ;
             
             // Launch Home Activity 
             
             Intent homeIntent = new Intent(getApplicationContext(), Home.class) ;
             startActivity(homeIntent) ;
             
         }
     	
     }
     
     
     
     protected String getUriFromURL(String picURL, File GroupBankerFriends, String name)	{
     	//storing the image from the url in a folder in SD card
     	URL url = null;
 		try {
 			url = new URL (picURL);
 		} catch (MalformedURLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
     	
 		InputStream input = null;
 		
 		try {
 			input = url.openStream();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
     	
 		String stringUri ;
     	Uri imageUri ;
     	
     	try {
     		
     		// create a File object for the output file
     		File outputFile = new File(GroupBankerFriends, name);
     		Log.v(TAG, "outputFile created by the name  " +  name) ;
     		
     		// now attach the OutputStream to the file object, instead of a String representation
     		FileOutputStream fos = new FileOutputStream(outputFile);
     		
     	    try {
     	        byte[] buffer = new byte[1000];
     	        int bytesRead = 0;
     	        while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) {
     	            fos.write(buffer, 0, buffer.length);
     	        }            	        
     	        Log.v(TAG, "the image buffer for output file written:  " +  buffer) ;
     	        
     	    } catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} finally {
     	        try {
 					fos.close();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
     	    }
     	} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
     	    try {
 				input.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
     	}
     	
     //getting the uri for the image just stored in sdcard
     	
     	String filename = name;
     	String path = "/mnt/sdcard/GroupBankerFriends" + filename;
     	File f = new File(path);  
     	 
    //uri from the file
         imageUri = Uri.fromFile(f);
     	Log.v(TAG, "URI of the image is:  " +  imageUri) ;
     	
    //converting the uri to string in order to store it in a database
     	stringUri = imageUri.toString();
     	
     	return stringUri ;
     }
     
     @Override
     protected void onPause()	{
     	super.onPause();
     	mDbHelper.close();
     }
 }
