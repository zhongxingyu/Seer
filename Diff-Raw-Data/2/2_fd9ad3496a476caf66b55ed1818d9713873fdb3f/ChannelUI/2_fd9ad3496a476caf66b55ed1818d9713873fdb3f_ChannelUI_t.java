 package com.example.channellist;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import mobisocial.socialkit.Obj;
 import mobisocial.socialkit.musubi.DbFeed;
 import mobisocial.socialkit.musubi.Musubi;
 import mobisocial.socialkit.obj.MemObj;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.app.Activity;
 import android.content.ActivityNotFoundException;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Toast;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 
 
 public class ChannelUI extends Activity {
 
 	private static final String ACTION_EDIT_FEED = "musubi.intent.action.EDIT_FEED";
 	private static final int REQUEST_EDIT_FEED = 2;
 
 	private static final String ADD_TITLE = "member_header";
 	private static final String ADD_HEADER = "Channel Members";
 
 	private static final String TAG = "Channel_UI"; 
 
 	static final String PICSAY_PACKAGE_PREFIX = "com.shinycore.picsay";
 	static final String ACTION_MEDIA_CAPTURE = "mobisocial.intent.action.MEDIA_CAPTURE";
 	private static final int REQUEST_CAPTURE_MEDIA = 3;
 
 	public static final String PICTURE_SUBFOLDER = "Pictures/Musubi";
 	public static final String HTML_SUBFOLDER = "Musubi/HTML";
 	public static final String FILES_SUBFOLDER = "Musubi/Files";
 	public static final String APPS_SUBFOLDER = "Musubi/Apps";
 
 	private Uri FeedUri = null;
 	private String mRowId;
 	private static Uri newImageUri;
 
 	private DbFeed activeFeed;
 	
 	private Bitmap mImageBitMap; 
 
 	
 	public final static String APP_PATH_SD_CARD = "/bao";
 
 	
 	//==============================================================
     public static final String SUPER_APP_ID = "mobisocial.musubi";
 
 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.channel_ui);	
 		
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			String uri_string = extras.getString("feedURI");
 			FeedUri = Uri.parse(uri_string); 
 			mRowId = extras.getString("rowId");
 		}		
 		
 		//Musubi musubi = Musubi.getInstance(this);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.channel_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		case R.id.add_members:
 			if (!Musubi.isMusubiInstalled(this)) {
 				Log.d(TAG, "Musubi is not installed.");
 				return super.onOptionsItemSelected(item);
 			}
 
 			Intent intent = new Intent(ACTION_EDIT_FEED);
 			intent.putExtra(ADD_TITLE, ADD_HEADER);   	
 			intent.setData(FeedUri);
 			startActivityForResult(intent, REQUEST_EDIT_FEED);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 
 	@Override
 	public void onResume() {
 		/*IntentFilter iff = new IntentFilter();
 		iff.addAction("mobisocial.intent.action.DATA_RECEIVED");
 		this.registerReceiver(this.messageReceiver, iff);*/
 		super.onResume();
 	}
 	
 	private File createFile() throws Exception
 	{
 		
 		String state = Environment.getExternalStorageState();
 		if (Environment.MEDIA_MOUNTED.equals(state)) {
 			File path = new File(Environment.getExternalStorageDirectory().getPath() + "/Astro_Channel/");
 			path.mkdirs();
 			File file = new File(path, "Feed" + mRowId.toString() + ".jpg");
 			Log.d("TAG", path + " AND " + file);
 			return file; 
 			//newImageUri = Uri.fromFile(file);
 		}
 		return null; 
 		
 	   /* File tempDir= Environment.getExternalStorageDirectory();
 	    tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
 	    if(!tempDir.exists())
 	    {
 	        tempDir.mkdir();
 	    }
 	    return File.createTempFile(part, ext, tempDir);*/
 	}
 
 	@SuppressWarnings("deprecation")
 	public void AddPhotos(View v){
 		
 		activeFeed = Musubi.getInstance(v.getContext()).getFeed(FeedUri);	
 		Log.d(TAG, activeFeed.toString());
 		
 		AlertDialog alert = new AlertDialog.Builder(ChannelUI.this).create();
 		alert.setTitle("Photo Options"); 
 		alert.setButton("From Gallery", new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
 				gallery.setType("image/*");
 				// god damn fragments.
 				//getTargetFragment().startActivityForResult(Intent.createChooser(gallery, null), REQUEST_GALLERY_THUMBNAIL);
 				
 				
 				
 			}
 		});
 
 		alert.setButton2("Take Photo", new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 												
 				Intent intent = new Intent(ACTION_MEDIA_CAPTURE);
 				    File photoFile;
 				    try
 				    {
 				        // place where to store camera taken picture		
 				        photoFile = createFile();
 				        if(photoFile == null)
 				        	return;
 				        
 				        photoFile.delete();
 				    }
 				    catch(Exception e)
 				    {
 				        Log.v(TAG, "Can't create file to take picture!");
 				        return;
 				    }
 
 				    newImageUri = Uri.fromFile(photoFile);
 				    intent.putExtra(MediaStore.EXTRA_OUTPUT, newImageUri);
 				    //start camera intent
 			    	Log.v(TAG, "SUCCESS CREATING FILE");
 
 			    	try {
 						startActivityForResult(intent, REQUEST_CAPTURE_MEDIA);
 					} catch (ActivityNotFoundException e) {
 						intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
 						startActivityForResult(intent, REQUEST_CAPTURE_MEDIA);
 					}
 			    	
 				/*
 				String state = Environment.getExternalStorageState();
 				if (Environment.MEDIA_MOUNTED.equals(state)) {
 					
 					File path = new File(Environment.getExternalStorageDirectory().getPath() + "/Astro_Channel/");
 					path.mkdirs();
 					File file = new File(path, "Feed" + mRowId.toString() + ".jpg");
 					//Log.d("TAG", path + " AND " + file);
 					newImageUri = Uri.fromFile(file);
 										
 					//Intent intent = new Intent(ACTION_MEDIA_CAPTURE);
 					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 					//intent.putExtra(MediaStore.EXTRA_OUTPUT, newImageUri);            
 
 					try {
 						startActivityForResult(intent, REQUEST_CAPTURE_MEDIA);
 					} catch (ActivityNotFoundException e) {
 						intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
 						startActivityForResult(intent, REQUEST_CAPTURE_MEDIA);
 					}	                
 				}
 				
 			*/	
 			}
 		});
 
 		alert.show(); 
 	}
 	
 	public void scaleDownBitmap(Bitmap photo, int newHeight, Context context) {
 
 		 final float densityMultiplier = context.getResources().getDisplayMetrics().density;        
 
 		 int h= (int) (newHeight*densityMultiplier);
 		 int w= (int) (h * photo.getWidth()/((double) photo.getHeight()));
 
 		 photo=Bitmap.createScaledBitmap(photo, w, h, true);
 		 
 		 mImageBitMap = photo; 
 	}
 	
 	
 	public void grabImage()
 	{
 		Bitmap newBitMap = null; 
 	    this.getContentResolver().notifyChange(newImageUri, null);
 	    ContentResolver cr = this.getContentResolver();
 	    try
 	    {
 	    	newBitMap = android.provider.MediaStore.Images.Media.getBitmap(cr, newImageUri);
 	    	scaleDownBitmap(newBitMap, 100, ChannelUI.this); 
 	    }
 	    catch (Exception e)
 	    {
 	        Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
 	        Log.d(TAG, "Failed to load", e);
 	    }
 	}
 
 	
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Log.d("MEH", "ENTERED HERE");
 
 		if (requestCode == REQUEST_CAPTURE_MEDIA && resultCode == RESULT_OK) {
 			Log.d("MEH", "ENTERED INSIDE ON IF");
 
 			grabImage();
 			if(mImageBitMap == null)
 			    Log.d("Meh", "FAILED");
 
 	    	JSONObject base = new JSONObject();
 		    byte[] byte_arr = getBytesFromBitmap(mImageBitMap); 
 		    
 		    MemObj pictureObj = new MemObj("picture", base, byte_arr);		    
 		    
 		    activeFeed.postObj(pictureObj);
 		    
             Log.d(TAG, "FEED: " + activeFeed.toString() );
             
             return; 	
 		}
 	}
 
 	public byte[] getBytesFromBitmap(Bitmap bitmap) {
 	    ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    bitmap.compress(CompressFormat.JPEG, 800, stream);
 	    return stream.toByteArray();
 	}
 	
 }
