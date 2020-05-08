 package com.t3hh4xx0r.addons.nightlies;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.PendingIntent;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceScreen;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.t3hh4xx0r.addons.R;
 import com.t3hh4xx0r.utils.fileutils.DownloadFile;
 import com.t3hh4xx0r.utils.fileutils.DownloadService;
 import com.t3hh4xx0r.utils.fileutils.Downloads;
 
 public class Nightlies extends ListActivity  {
     /** Called when the activity is first created. */
 	private String TAG = "Nightlies";
 	boolean DBG = true;
 	private ArrayList<NightlyObject> mNightlies = null;
 	private ListView mNighltListView;
 	private NightlyAdapter mAdapter;
 	private ProgressDialog mProgressDialog = null; 
 
     private Runnable viewNightlies;
     private boolean mHasDownloadScript = true;
     private String mScriptURL;
     private String mDownloadPath;
     private String mFileReadPath; 
     //This is needed for device permanace for intents that
     // do not send the device type with it. IE. notifications
     private static String mDeviceScript;
 
 	private boolean mUserWantFlash  = false;
     
 
 	private static File extStorageDirectory = Environment.getExternalStorageDirectory();
 	private static final String DOWNLOAD_DIR = "/sdcard/t3hh4xx0r/downloads/";
 	    
 	private boolean mIsTesting = false;
     	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.nightlies);
         
         
         mNightlies = new ArrayList<NightlyObject>();
         
         this.mAdapter = new NightlyAdapter(this, R.layout.row, mNightlies);
         
         setListAdapter(this.mAdapter);
         
         Intent i = this.getIntent();
         if(mDeviceScript == null){
         	// We need to retrive the device script name, but only once
         	mDeviceScript = i.getStringExtra("DownloadScript");
         
         }
         if(mDeviceScript == null ){
         	mHasDownloadScript = false;
         }else{
         	mScriptURL = mDownloadPath + mDeviceScript;
         }
         
         // This is where the script comes in
         // We need to set the script location 
         // sdcard or from a url stream(sdcard is better, probaly should only do it this way)
         // Once that is set then set the file read path.
 
         //  mFileReadPath;
         viewNightlies = new Runnable(){
             @Override
             public void run() {
                 getNightlies();
             }
         };
         
         Thread thread =  new Thread(null, viewNightlies, "MagentoBackground");
         thread.start();
         mProgressDialog = ProgressDialog.show(Nightlies.this,    
                 "Please wait...", "Retrieving data ...", true);
         
         
         
     }
     
     private Runnable returnRes = new Runnable() {
 
         @Override
         public void run() {
             if(mNightlies != null && mNightlies.size() > 0){
             	mAdapter.notifyDataSetChanged();
                 for(int i=0;i<mNightlies.size();i++)
                 mAdapter.add(mNightlies.get(i));
             }
             mProgressDialog.dismiss();
             mAdapter.notifyDataSetChanged();
         }
       };
     
     private void getNightlies(){
     	
     	try
         {
             mNightlies = new ArrayList<NightlyObject>();
     		
             String x = "";
             InputStream is;
             // Need to actually put our sript locatio here
             if(this.mHasDownloadScript == false || mIsTesting)
             	is = this.getResources().openRawResource(R.raw.jsonomfgb);
             else{
             	
             	// Pull it from the sdcard and read it
             	// here to compile
             	// 
             	Log.d(TAG,"Updating app from file");
             	File updateFile = new File(DownloadFile.updateAppManifest(mDeviceScript));
             	try{
             		
             		is = new FileInputStream(updateFile);
             	}
             	catch(FileNotFoundException e){
             		
             			e.printStackTrace();
             			AlertBox("Warining","Nighly manifest is incoreect, please contact the ROM devlopers @r2DoesInc, @linuxmotion, @xoomdev or @davidjr621_");
             			Log.d(TAG, "Could not update app from file resource, the file was not found. Reverting to test script");
                     	is = this.getResources().openRawResource(R.raw.jsonomfgb);
             		
             	}
             	
             	
             }
                 
             byte [] buffer = new byte[is.available()];
             while (is.read(buffer) != -1);
             
             String jsontext = new String(buffer);
             JSONArray entries = new JSONArray(jsontext);
 
             x = "JSON parsed.\nThere are [" + entries.length() + "] entries.\n\n";
 
             int i;
             for (i=0;i<entries.length();i++)
             {
             	NightlyObject n = new NightlyObject();
                 JSONObject post = entries.getJSONObject(i);
                 
                 n.setDate(post.getString("date"));
                 n.setBase(post.getString("base"));
                 n.setDevice(post.getString("device"));
                 n.setURL(post.getString("url"));
                 n.setVersion(post.getString("version"));
                 n.setZipName(post.getString("name"));
                 n.setInstallable(post.getString("installable"));
 
                 mNightlies.add(n);
             }
             Log.d(TAG, x);
             Thread.sleep(200);
         }
         catch (Exception je)
         {
 
             Log.e(TAG, je.getMessage());
              je.printStackTrace();
         }
           runOnUiThread(returnRes);
       }
     
     
     
 
     static int adapters = 0; 
     private class NightlyAdapter extends ArrayAdapter<NightlyObject> implements NightlyClickListener.onNightlyClickListener{
 
     	private String TAG = "Nightly Adapter";
     	private int mNumAdapters = 0;
     	
     	private ArrayList<NightlyObject> mNightly;
     	
     
     	
     	public NightlyAdapter(Context context, int textViewResourceId, ArrayList<NightlyObject> items) {
     		super(context, textViewResourceId, items);
     		// TODO Auto-generated constructor stub
     		mNightly = items;
     	}
     	
     	@Override
         public View getView(int position, View convertView, ViewGroup parent) {
     		
     	
     		
                 View v = convertView;
                 if (v == null) 
                 {
                     LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                     v = vi.inflate(R.layout.row, null);
                 }
                 
                 NightlyObject o = mNightly.get(position);
                 
                 if (o != null) 
                 {
                 		o.mNightlyLayout = (LinearLayout) v.findViewById(R.id.nightly_row);
                         o.mNightlyVersion = (TextView) v.findViewById(R.id.nightly_ver);
                         o.mRomBaseVersion = (TextView) v.findViewById(R.id.rom_ver);
                         o.mCompiledDate = (TextView) v.findViewById(R.id.comp_date);
                         
                         if (o.mNightlyVersion != null) 
                         {
                         	o.mNightlyVersion.setText("Nightly: "+o.getVersion());
                         }
                         if (o.mRomBaseVersion != null) 
                         {
                         	o.mRomBaseVersion.setText("Rom Version: "+ o.getBase());
                         }
 						if (o.mCompiledDate != null) 
 						{
 							o.mCompiledDate.setText("Complied On: "+ o.getDate());
 						}
 						
 						//convertView.setTag(mNumAdapters);
 						//o.setTagNumber(mNumAdapters++);
 						Log.d(TAG,"Creating the listener");
 						NightlyClickListener nm = new NightlyClickListener(v,position);
 						nm.setOnNightlyClickListener(this);
 						
 						Log.d(TAG,"Setting the listener");
 						o.mNightlyLayout.setOnClickListener(nm );
                 } 
                 
                 
             	if(DBG){
         			
         			if(position > adapters)adapters++;
         			Log.d(TAG, "The number of adapter views is: " + (adapters+1)  );
       		
         		}
             
             return v;
                 
         }
 
 
     	    
     	@Override
     	public void OnNightlyClick(View v, int position) {
     		// TODO Auto-generated method stub
     		Log.d(TAG, v.toString()  );
     		
     		NightlyObject o = mAdapter.mNightly.get(position);
     		o.mNightlyLayout = (LinearLayout) v.findViewById(R.id.nightly_row);
             o.mNightlyVersion = (TextView) v.findViewById(R.id.nightly_ver);
             o.mRomBaseVersion = (TextView) v.findViewById(R.id.rom_ver);
             o.mCompiledDate = (TextView) v.findViewById(R.id.comp_date);
             
        
             
             Log.d(TAG, "About to strart the download with zip " +   o.getZipName() );
             
             Intent downloadservice  = new Intent(Nightlies.this, DownloadService.class);
             downloadservice.putExtra("URL", o.getURL());
             downloadservice.putExtra("ZIP", o.getZipName());
            
             downloadservice.putExtra("INSTALLABLE", Boolean.parseBoolean(o.getInstallable()));
             Log.d(TAG,  o.getURL());
        
             final Boolean Installable = !Boolean.parseBoolean(o.getInstallable());
             final String zipName = o.getZipName();
             	
                             File f = new File (DOWNLOAD_DIR + o.getZipName());

    	            	    if (!f.exists()){
 		            	startService(downloadservice);   
		            } else {
					if (!DownloadFile.checkFileIsCompleted(o.getURL(), o.getZipName())) {
					    //TODO: Delete file and first fresh. 
                                	    startService(downloadservice);
                                        } else {
		            		    FlashAlertBox("Warning","About to flash package: " + zipName , Installable, zipName);
		            		}			
  			    }
         }
     	
                     
    }
     	
 
 
     	
 
 	
     
 	protected void FlashAlertBox(String title, String mymessage, final boolean Installable, final String OUTPUT_NAME)
 	   {
 	   new AlertDialog.Builder(this)
 	      .setMessage(mymessage)
 	      .setTitle(title)
 	      .setCancelable(false)
 	      .setPositiveButton("OK",
 	         new DialogInterface.OnClickListener() {
 	         public void onClick(DialogInterface dialog, int whichButton){
 	        	 
 	        	 Thread FlashThread = new Thread(){
 	            		
 	            		@Override
 	            	    public void	run(){
 	            			
 	            			File f = new File (DOWNLOAD_DIR + OUTPUT_NAME);
 	            			
 	            			if(f.exists() ){
 		  				  		Log.d(TAG, "User approved flashing, begining flash.");
 		  						if (Installable) 
 		  						{
 		  						   Downloads.flashPackage(OUTPUT_NAME);
 		  						} else 
 		  						{
 		  							Downloads.installPackage(OUTPUT_NAME );
 		  						}
 	  				  
 	            			} 
 	            			
 	            	  }
 	            };
 	            
 	            FlashThread.run();
 	         }
 	         })
 	         .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 	         public void onClick(DialogInterface dialog, int whichButton){
 	        	 // Do nothing
 	        	 Log.d(TAG, "User did not approve flashing.");
 	         }
 	         })
 	         
 	         
 	      .show();
 	   		
 		
 	   }
 	protected void AlertBox(String title, String mymessage)
 	   {
 	   new AlertDialog.Builder(this)
 	      .setMessage(mymessage)
 	      .setTitle(title)
 	      .setCancelable(false)
 	      .setPositiveButton("OK",
 	         new DialogInterface.OnClickListener() {
 	         public void onClick(DialogInterface dialog, int whichButton){
 	         }
 	         })
 	         
 	         
 	      .show();
 	   		
 		
 	   }
     
     
 }
