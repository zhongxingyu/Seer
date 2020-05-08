 package com.vidit;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Environment;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.MediaController;
 import android.widget.TextView;
 import android.widget.VideoView;
 import com.vidit.R;
 
 @SuppressLint("SimpleDateFormat")
 public class VideoDetails extends Activity {
 	
 	private TextView tvTitle,tvDateTime,tvOwner,tvLength;
 	private ImageView imgThumbnail;
 	private Button btnPlayLQ,btnDownloadLQ,btnPlayHQ,btnDownloadHQ;
 	private JSONObject jsonObj;
	private String title;
 	private VideoView fbVideo;
 	private Context context;
 	public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
 	private ProgressDialog mProgressDialog;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.video_details_layout);
 		try 
 		{
 			jsonObj = new JSONObject(getIntent().getStringExtra("video_Details"));
 		} 
 		catch (JSONException e) 
 		{
 			Log.e("Vidit_TAG","I got an error",e);
 		}
 		String strThumbLink=getIntent().getStringExtra("video_Thumb");
 		context=this;
 		tvTitle=(TextView)findViewById(R.id.tvTitle);
 		tvDateTime=(TextView)findViewById(R.id.tvDateTime);
 		tvLength=(TextView)findViewById(R.id.tvLength);
 		tvOwner=(TextView)findViewById(R.id.tvOwner);
 		imgThumbnail=(ImageView)findViewById(R.id.imgVideoThumbnail);
 		btnDownloadLQ=(Button)findViewById(R.id.btnDownloadLQ);
 		btnPlayLQ=(Button)findViewById(R.id.btnPlayLQ);
 		btnDownloadHQ=(Button)findViewById(R.id.btnDownloadHQ);
 		btnPlayHQ=(Button)findViewById(R.id.btnPlayHQ);
 		fbVideo=(VideoView)findViewById(R.id.vvPlay);
 		try
 		{
 			title=jsonObj.getString("title");
 			if(title.equalsIgnoreCase(""))
 				title="Untitled";
 			tvTitle.setText("Title: "+title);
 			DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
 			tvDateTime.setText("Added On: " + formatter.format(Long.valueOf(jsonObj.getString("created_time")).longValue()*1000));
 			float length=Float.parseFloat(jsonObj.getString("length"));
 			String prefix=" Seconds";
 			if(length>60)
 			{
 				length/=60;
 				prefix=" Minutes";
 			}
 			tvLength.setText("Length: "+length+prefix);
 			tvOwner.setText("Owner: "+getIntent().getStringExtra("ownerDetails"));
 			Bitmap bmp = BitmapFactory.decodeFile(strThumbLink);
 			imgThumbnail.setImageBitmap(bmp);
 		}
 		catch(Exception e)
 		{
 			Log.e("Vidit_TAG","I got an error",e);
 		}
 		
 		btnDownloadLQ.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) 
 			{
 				try
 				{
 					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
 						new DownloadVideoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,jsonObj.getString("src"));
 					else
 						new DownloadVideoTask().execute(jsonObj.getString("src"));
 					
 				}
 				catch(Exception e)
 				{
 					Log.e("Vidit_TAG","I got an error",e);
 				}
 			}
 		});
 		
 		btnDownloadHQ.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) 
 			{
 				try
 				{
 					new DownloadVideoTask().execute(jsonObj.getString("src_hq"));
 					
 				}
 				catch(Exception e)
 				{
 					Log.e("Vidit_TAG","I got an error",e);
 				}
 			}
 		});
 		
 		btnPlayLQ.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) 
 			{
 				try
 				{
 					Uri uri=Uri.parse(jsonObj.getString("src"));
 					fbVideo.setVideoURI(uri);
 				    fbVideo.setMediaController(new MediaController(context));
 				    fbVideo.requestFocus();
 				    fbVideo.start();
 					
 				}
 				catch(Exception e)
 				{
 					Log.e("Vidit_TAG","I got an error",e);
 				}
 			}
 		});
 		
 		btnPlayHQ.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) 
 			{
 				try
 				{
 					Uri uri=Uri.parse(jsonObj.getString("src_hq"));
 					fbVideo.setVideoURI(uri);
 				    fbVideo.setMediaController(new MediaController(context));
 				    fbVideo.requestFocus();
 				    fbVideo.start();
 					
 				}
 				catch(Exception e)
 				{
 					Log.e("Vidit_TAG","I got an error",e);
 				}
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.vidit_menu, menu);
 		return true;
 	}
 	
 	private class DownloadVideoTask extends AsyncTask<String, String, String> 
 	{
 		
 		@SuppressWarnings("deprecation")
 		@Override
         protected void onPreExecute() {
             super.onPreExecute();
             showDialog(DIALOG_DOWNLOAD_PROGRESS);
         }
 		
 	    protected String doInBackground(String... urls) 
 	    {
 	    	int i=0;
 	    	try
 	    	{
 	    		URL url = new URL (urls[0]);
 	    		InputStream input = url.openStream();
 	    	
 	    		try {
 	    			//The sdcard directory e.g. '/sdcard' can be used directly, or 
 	    			//more safely abstracted with getExternalStorageDirectory()
 	    			String root = Environment.getExternalStorageDirectory().toString();
 	    			File storagePath = new File(root + "/vidit");    
 	    			storagePath.mkdirs();
 	    			OutputStream output = new FileOutputStream (new File(storagePath,title+".mp4"));
 	    			try 
 	    			{
 	    				byte[] buffer = new byte[1024];
 	    				int bytesRead = 0;
 	    				while ((bytesRead = input.read(buffer, 0, buffer.length)) >= 0) 
 	    				{
 	    					output.write(buffer, 0, bytesRead);
 	    				}
 	    	    	}
 	    			catch(Exception e)
 	    			{	
 	    				Log.e("Vidit_TAG","I got an error",e);
 	    			}
 	    			finally 
 	    			{
 	    				output.close();
 	    			}
 	    		}
 	    		catch(Exception e)
 	    		{
 	    			Log.e("Vidit_TAG","I got an error",e);
 	    		}
 	    		finally 
 	    		{
 	    			input.close();
 	    			//tvTitle.setText("Completed");
 	    		}
 	    		
 	    	}
 	    	catch(Exception e)
 	    	{
 	    		Log.e("Vidit_TAG","I got an error",e);
 	    	}
 	    	
 			return null;
 	    }
 	    
 	    
 	    @SuppressWarnings("deprecation")
 		@Override
         protected void onPostExecute(String unused) 
 	    {
 	    	dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
 	    	alertbox(title);
         }
 	}
 	
 	protected void alertbox(String msgTitle)
 	   {
 	   new AlertDialog.Builder(this)
 	      .setMessage(msgTitle+" has been successfully downloaded!")
 	      .setTitle("Video Downloaded")
 	      .setCancelable(true)
 	      .setNeutralButton(android.R.string.ok,
 	         new DialogInterface.OnClickListener() {
 	         public void onClick(DialogInterface dialog, int whichButton){}
 	         })
 	      .show();
 	   }
 	
 	@Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
             case DIALOG_DOWNLOAD_PROGRESS:
                 mProgressDialog = new ProgressDialog(this);
                 mProgressDialog.setMessage("Downloading "+title);
                 mProgressDialog.show();
                 return mProgressDialog;
             default:
                 return null;
         }
     }
 }
