 package org.societies.thirdpartyservices;
 
 import java.util.Random;
 
 import org.societies.android.api.cis.SocialContract;
 
 
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Spinner;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class IJacketClient extends Activity implements OnItemSelectedListener {
 
     private static String LOG_TAG = "ijacketCLient";
     ContentResolver cr;
     String communityJid = "";
     String communityLocalID = "";
    String cssID = "defaultUser";
     
     /**
      * Called when the activity is first created.
      * @param savedInstanceState If the activity is being re-initialized after 
      * previously being shut down then this Bundle contains the data it most 
      * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 		Log.d(LOG_TAG, "onCreate");
 		retrieveCSSID();
 		
 		 setContentView(R.layout.main);
 		addItemsOnCISSpinner();
 		
 
 		
 		
        
     }
         
     /** Called when the user touches the button */
     public void sendMessage(View view) {
 		cr = this.getApplication().getContentResolver();
 		
 		Log.d(LOG_TAG, "send button pressed");
 		
         Uri FEED_URI = Uri.parse(SocialContract.AUTHORITY_STRING + SocialContract.UriPathIndex.COMMUNITY_ACTIVITIY);
         try{
         	ContentValues mNewValues = new ContentValues();
         	EditText editText = (EditText) findViewById(R.id.edit_message);
         	
         	/*
         	 * Sets the values of each column and inserts the word. The arguments to the "put"
         	 * method are "column name" and "value"
         	 */
         	mNewValues.put(SocialContract.CommunityActivity.GLOBAL_ID, randomStr());
         	mNewValues.put(SocialContract.CommunityActivity.GLOBAL_ID_ACTOR, cssID);
         	mNewValues.put(SocialContract.CommunityActivity.GLOBAL_ID_OBJECT, editText.getText().toString());
         	mNewValues.put(SocialContract.CommunityActivity.GLOBAL_ID_VERB, "posted");
         	mNewValues.put(SocialContract.CommunityActivity.GLOBAL_ID_FEED_OWNER, communityJid);
         	Log.d(LOG_TAG, "going to inseet: " + cssID + " posted " + editText.getText().toString() + " at " +communityJid);
         
         	Uri mNewUri = cr.insert(FEED_URI,mNewValues);
         	
         	this.runOnUiThread(new Runnable() {
                 public void run() {
                    Toast.makeText(IJacketClient.this, "activity published", Toast.LENGTH_SHORT).show();
                 }
             });
         	
         }catch (Exception e) {
 	   		// TODO Auto-generated catch block
 	   		Log.d(LOG_TAG, "exception in the insert");
 	   		e.printStackTrace();
 	   	}
 
     }
         
     private void retrieveCSSID(){
     	Log.d(LOG_TAG, "going to retrieve CSSID");
     	Uri CSS_URI = Uri.parse(SocialContract.AUTHORITY_STRING + SocialContract.UriPathIndex.ME);
         try{
         	//String[] mProjection ={SocialContract.Me.GLOBAL_ID};
         	String mSelectionClause = SocialContract.Me.ORIGIN + " = ?";
         	String[] mSelectionArgs = {"SOCIETIES"};
         	cr = this.getApplication().getContentResolver();
 	       	 Cursor cursor = cr.query(CSS_URI,null,mSelectionClause,mSelectionArgs,null);
 	       	if (null == cursor || cursor.getCount() < 1){
 	       		Log.d(LOG_TAG, "could not find the CSS id");
 	       		
 	       	}
 	       	else{
 	       		if (cursor.getCount() > 1){ 
 	       			Log.d(LOG_TAG, "too many CSS id");
 	       		}else{
 	       			int i  = cursor.getColumnIndex(SocialContract.Me.USER_NAME);
 	   		    	cssID = cursor.getString(i);
 	   		    	Log.d(LOG_TAG, "user is " + cssID);
 	       		}
 	       	}
     	}catch (Exception e) {
 	   		// TODO Auto-generated catch block
 	   		Log.d(LOG_TAG, "exception in the retrieveCSSID");
 	   		e.printStackTrace();
 	   	}
     
     }    
 
     // TODO: remove this and get the real strings
     public String randomStr(){
     	Random r = new Random();
     	int strlen = 6;
     	String s = "";
     	for(int i =0; i<strlen; i++) s+= (char)(65+ r.nextInt(26));
     	return s;
     }
         
     private void addItemsOnCISSpinner(){
         	Spinner spinnerCIS = (Spinner) findViewById( R.id.spinner1 );
         	if(null == spinnerCIS) Log.d(LOG_TAG, "spinner is null...");
     		
     	        Log.d(LOG_TAG, "going to query the social provider");
     	       
     	        cr = this.getApplication().getContentResolver();
     	        Uri COMUNITIES_URI = Uri.parse(SocialContract.AUTHORITY_STRING + SocialContract.UriPathIndex.COMMINITIES);
     	        try{
 	    	       	Cursor cursor = cr.query(COMUNITIES_URI,null,null,null,null);
 	    	       	startManagingCursor(cursor);
 
 	    	       	String[] columns = new String[] {SocialContract.Communities.NAME}; // field to display
 	    	       	int to[] = new int[] {android.R.id.text1}; // display item to bind the data
 	    	       	SimpleCursorAdapter ad =  new SimpleCursorAdapter(this,android.R.layout.simple_spinner_item,cursor ,columns ,to);
 	    	        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    	       	spinnerCIS.setAdapter(ad);
 	    	  
 	    	        spinnerCIS.setOnItemSelectedListener(this);
 	    	        Log.d(LOG_TAG, "spiner filled up");
 	    	   		
 	    	   		//if(null != cursor) cursor.close();
     		   	}catch (Exception e) {
     		   		// TODO Auto-generated catch block
     		   		Log.d(LOG_TAG, "exception in the create");
     		   		e.printStackTrace();
     		   	}
 
     	        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SpinnerArray);
     	        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 
 
     }
         
         
 
     	@Override
     	public void onItemSelected(AdapterView<?> parent, View view, int pos,
     			long id) {
     		
     		Cursor cursor = (Cursor) parent.getItemAtPosition(pos);
     		int i = cursor.getColumnIndex(SocialContract.Communities.GLOBAL_ID); 
     		communityJid = cursor.getString(i);
     		i = cursor.getColumnIndex(SocialContract.Communities._ID); 
     		communityLocalID = cursor.getString(i);
     	    Log.d("LOG_TAG", "found community with JID " + communityJid + " and id " + communityLocalID);
     	    
     		
     	}
 
 
 
     	@Override
     	public void onNothingSelected(AdapterView<?> arg0) {
     		// TODO Auto-generated method stub
     		
     	}
 
 }
 
