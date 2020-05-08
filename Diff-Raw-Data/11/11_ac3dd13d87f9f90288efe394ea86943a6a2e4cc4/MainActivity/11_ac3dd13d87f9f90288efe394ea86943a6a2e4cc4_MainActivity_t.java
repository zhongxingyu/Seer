 package com.jamtwo.randommeme;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.EventObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.GestureDetector;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.jamtwo.randommeme.asynctasks.HTMLParserAsyncTask;
 import com.jamtwo.randommeme.events.MemeDownloadFinishEvent;
 import com.jamtwo.randommeme.events.MemeDownloadFinishInterface;
 import com.jamtwo.randommeme.parseengines.LiveMemeHtmlParseEngine;
 
 public class MainActivity extends Activity implements OnClickListener, ILoadMoreMemesListener, MemeDownloadFinishInterface {
 
 	private MemeWebView mWebView;
 	private int mPageIndex = 0;
 	private int mPageIndexMax = 150;
 	private static final String CLASS = "MainActivity";
 	
     private GestureDetector flingHandler;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         MemeStack.getInstance().registerLoadMoreMemesListener(this);
         
         initView();
         
        
         
     }
     
     private void initView(){
         ImageButton nextButton = (ImageButton) findViewById(R.id.nextButton);
         nextButton.setOnClickListener((OnClickListener) this);
         
         ImageButton prevButton = (ImageButton) findViewById(R.id.prevButton);
         prevButton.setOnClickListener((OnClickListener) this);
 
         ImageButton shareButton = (ImageButton) findViewById(R.id.shareButton);
         shareButton.setOnClickListener((OnClickListener) this);
         
         mWebView = (MemeWebView)findViewById(R.id.memeView);
         mWebView.getSettings().setJavaScriptEnabled(true); //
         
         mWebView.setPadding(0, 0, 0, 0);
         mWebView.setBackgroundColor(0x000000);
         mWebView.getSettings().setLoadWithOverviewMode(true);
         mWebView.getSettings().setUseWideViewPort(true);
         mWebView.getSettings().setBuiltInZoomControls(false);
         
         flingHandler = new GestureDetector(this, new FlingHandler(mWebView));
         downloadMoreMemes(5);
         //MemeStack.getCurrentMeme();//.setDownloadListener(this);
     }
 
     
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()){
 		case R.id.nextButton:
 			//Log.w("MainActivity", "nextButton clicked");
 			displayNextMeme();
 			break;
 		case R.id.prevButton:
 			displayPrevMeme();
 			break;
 		case R.id.shareButton:
 			shareCurrentMeme();
 			break;
 		}
 	}
 	
 	private void shareCurrentMeme() 
 	{
 		String memeJpegUri = saveMemeAsJpeg(MemeStack.getCurrentMeme());
 		
 		Intent sendIntent = new Intent();
 		sendIntent.setAction(Intent.ACTION_SEND);
 		sendIntent.setType("image/jpeg");
 		sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(memeJpegUri));
 
 		startActivity(sendIntent);
 		
 	}
 	
 	private String saveMemeAsJpeg(Meme meme)
 	{
 		File memeJpeg = null;
 		File root = null;
 		byte[] jpegData = meme.getJpegData();
 		
 		String state = Environment.getExternalStorageState();
 
 		if (Environment.MEDIA_MOUNTED.equals(state))
 		{
 			root = Environment.getExternalStorageDirectory();
 		} else 
 		{
 			root = getFilesDir();
 		}
 	    
 		if(root == null)
 		{
 			//how is this even possible?
 			Toast.makeText(this, "I can't find a writable directory!", Toast.LENGTH_SHORT);
 			return "invalid";
 		}
 		
 	    if (root.canWrite())
 	    {
 	    	memeJpeg = new File(root, "meme.jpg");
 	        FileOutputStream memeJpegWriter;
 			try 
 			{
 				memeJpegWriter = new FileOutputStream(memeJpeg);
 				BufferedOutputStream out = new BufferedOutputStream(memeJpegWriter);
 			    out.write(jpegData);
 			    Log.w("MainActivity.shareCurrentMeme()", "1st:"+ memeJpeg.getAbsolutePath());
 			    out.close();
 			}
 			catch (FileNotFoundException e)
 			{
 				e.printStackTrace();
 				Toast.makeText(this, "Something went wrong sending this meme", Toast.LENGTH_SHORT);
 				return "invalid";
 			} 
 			catch (IOException e) 
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				Toast.makeText(this, "Something went wrong sending this meme", Toast.LENGTH_SHORT);
 				return "invalid";
 			}
 	    }
 	    
 	    return "file:///" +  memeJpeg.getAbsolutePath();
 	}
 
 	private boolean networkConnectionIsAvailable() 
 	{
 	    boolean wifiIsConnected = false;
 	    boolean mobileIsConnected = false;
 
 	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
 	    for (NetworkInfo ni : netInfo) 
 	    {
 	        if (ni.getTypeName().equalsIgnoreCase("WIFI"))
 	        {
 	            if (ni.isConnected())
 	            {
 	            	wifiIsConnected = true;
 	            }
 	        }
 	        if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
 	        {
 	            if (ni.isConnected())
 	            {
 	            	mobileIsConnected = true;
 	            }
 	        }
 	    }
 	    return wifiIsConnected || mobileIsConnected;
 	}
 	
 	public void loadMoreMemes(){
 		String TAG = CLASS + ".loadMoreMemes";
 		
 		//Log.v(TAG, "loading more memes");
 		mPageIndex++;
 		if (mPageIndex > mPageIndexMax){
 			mPageIndex=0;
 		}
 		
     	HTMLParserAsyncTask parser = new HTMLParserAsyncTask(this, mWebView);
     	//parser.parseEngine = new QuickMemeHtmlParseEngine(); //strategy!
     	parser.parseEngine = new LiveMemeHtmlParseEngine(this);
     	parser.execute();
 	}
 
 	//called when the async'ly downloaded jpeg is ready - this is handled throught the meme
     //public void updateWebView()
     //{
     //	String TAG = CLASS + ".updateWebView()";
 		//Log.w(TAG, "updating web view");
     	//mWebView.updateWebView();
     //}
     
     public void displayNextMeme()
     {
     	String TAG = CLASS + ".displayNextMeme()";
     	//Log.w(TAG, "is next meme ready?");
     	if(MemeStack.nMemesFromLast(3) == true)
     	{
 			//Log.w(TAG, "downloading 5 more memes because " + MemeStack.nMemesFromLast(3));
 			downloadMoreMemes(5);
 		}
 		
     	if(MemeStack.nextMemeIsReady())
     	{
     		Log.w(TAG, "Yes. Next meme is ready");
     		Meme meme = MemeStack.getNextMeme();
     		
     		mWebView.display(meme);
     		updateTvTitle(meme.getTitle());
     		mWebView.invalidate();
     	}
     	else
     	{
     		Log.w(TAG, "No. Next meme is not ready");
     	//	if(networkConnectionIsAvailable())
     	//	{
     	//		Toast.makeText(this, "I'm getting more memes ready for you", Toast.LENGTH_SHORT).show();
     	//	}
     	}
     }
     
     
     public void displayPrevMeme()
     {
     	String TAG = CLASS + ".displayPrevMeme()";
     	
     	Log.w(TAG, "called");
     	if(MemeStack.prevMemeIsReady())
     	{
     		Meme meme = MemeStack.getPrevMeme();
     		
     		mWebView.display(meme);
     		updateTvTitle(meme.getTitle());
     		mWebView.invalidate();
     		//downloadAnotherMeme();
     	}
     	else
     	{
     		Toast.makeText(this, "There are no more memes that way", Toast.LENGTH_LONG);
     	}
     }
     
     public void updateTvTitle(String currentMemeTitle)
     {
     	String TAG = CLASS + ".updateTvTitle()";
     	//Log.v(TAG, "updating with title" + currentMemeTitle);
 		TextView tvTitle;
 		tvTitle = (TextView) this.findViewById(R.id.tvTitle);
 		
 		float textSize;
 		
 		if(currentMemeTitle == null)
 		{
 			currentMemeTitle = "Tap next!";
 		}
 		//22 chars?
 		if(currentMemeTitle.length() < 22)
 		{
 			textSize = 15;
 		}
 		else if(currentMemeTitle.length() < 35)
 		{
 			textSize = 13;
 		}
 		else
 		{
 			textSize = 11;
 		}
 
 		tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
 		tvTitle.setText(currentMemeTitle);
 		
     }
 
 	@Override
 	public void downloadMoreMemes(int count) {
 		
 		String TAG = CLASS + ".downloadAnotherMeme";
 		if(networkConnectionIsAvailable())
 		{
 			Toast.makeText(this, "I'm getting more memes ready for you", Toast.LENGTH_SHORT).show();
 			
 			while(count > 0)
 			{
 				HTMLParserAsyncTask parser = new HTMLParserAsyncTask(this, mWebView);
 		    	//parser.parseEngine = new QuickMemeHtmlParseEngine(); //strategy!
 		    	parser.parseEngine = new LiveMemeHtmlParseEngine(this);
 		    	parser.execute();
 		    	count--;
 			}
 		}
 		else
 		{
 			Toast.makeText(this, "You're not connected to the Internet!", Toast.LENGTH_SHORT).show();
 		}
 		
 	}
 
 	@Override
 	public void onMemeDownloadFinish(MemeDownloadFinishEvent e) {
 		// TODO Auto-generated method stub
 		Log.w("MainActivity.onMemeDownloadFinish()", "Download finish detected (meme" + e.stackPosition + ")");
 		if(MemeStack.atFirstMeme() && e.stackPosition == 1)
 		{
 			Log.w("MainActivity.onMemeDownloadFinish()", "Displaying the first meme");
 			displayNextMeme();
 		}
 	}
 
 	
 	
 }
