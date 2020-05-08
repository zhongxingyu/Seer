 package com.example.phat_am;
 
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Point;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.Display;
 import android.view.WindowManager;
 import android.webkit.WebSettings.PluginState;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.widget.ListView;
 import android.widget.MediaController;
 import android.widget.VideoView;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.example.listview.ListVideoAdapter;
 import com.example.listview.Model_Video;
 import com.example.utils.VideoViewCustom;
 
 public class VideoViewActivity extends SherlockActivity{
 	
 	VideoViewCustom vv;
 	
 	String videoUrl;
 	MediaController mc;
 	int MeasureWidth = 0;
 	int MeasureHeight = 0;
 	Point p = new Point();
 //	WindowManager wm = getWindowManager();
 	public ArrayList<Model_Video> list_model = new ArrayList<Model_Video>();
 	ListView list;
 	ListVideoAdapter adapter;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.videoview);
 		vv = (VideoViewCustom) findViewById(R.id.VideoView);
 //		vv = (VideoViewCustom)vv;
 		mc = new MediaController(VideoViewActivity.this, false);
 		vv.setMediaController(mc);
 		mc.show();
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
 		{
 			getWindowManager().getDefaultDisplay().getSize(p);
 			MeasureWidth = p.x;
 			MeasureHeight = p.y;
 		}
 		else
 		{
 			Display d = getWindowManager().getDefaultDisplay();
 			MeasureWidth = d.getWidth();
 			MeasureHeight = d.getHeight();
 		}
 		int smallWidth =  MeasureWidth/4*3;
         if (smallWidth > MeasureHeight/5*2)
         	smallWidth = MeasureHeight /5*2;
 		vv.setDimensions(smallWidth*4/3, smallWidth);
 		String[] list_video = getResources().getStringArray(R.array.detail_video_array);
 		String[] list_author = getResources().getStringArray(R.array.detail_video_author_array);
 		
 		Bitmap bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.image);
 		
 		for(int i=0; i<5; i++)
 		{
 			list_model.add(new Model_Video(bm, list_video[i], list_author[i]));
 		}
 		
 		ListView list = (ListView)findViewById(R.id.videoview_list);
 		adapter = new ListVideoAdapter(this, list_model);
 		list.setAdapter(adapter);
 		
 		YourAsyncTask async = new YourAsyncTask();
 		async.execute();
 	}
 	
 	@Override
 		public void onConfigurationChanged(Configuration newConfig) {
 			// TODO Auto-generated method stub
 			Log.v("on configuration change", newConfig.orientation+"");
 			super.onConfigurationChanged(newConfig);
 	
 		    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
 		        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
 		        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		        
 		        vv.setDimensions(MeasureHeight, MeasureWidth);
 		        vv.getHolder().setFixedSize(MeasureHeight, MeasureWidth);
 	
 		    } else {
 		        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
 		        int smallWidth =  MeasureWidth/4*3;
 		        if (smallWidth > MeasureHeight/5*2)
 		        	smallWidth = MeasureHeight /5*2;
 		        vv.setDimensions(smallWidth*4/3,smallWidth);
 		        vv.getHolder().setFixedSize(MeasureWidth, smallWidth);
 
 	    }
	}
	
 	
 	private class YourAsyncTask extends AsyncTask<Void, Void, Void>
     {
         ProgressDialog progressDialog;
 
         @Override
         protected void onPreExecute()
         {
             super.onPreExecute();
             progressDialog = ProgressDialog.show(VideoViewActivity.this, "", "Loading Video wait...", true);
         }
 
         @Override
         protected Void doInBackground(Void... params)
         {
             try
             {
                 String url = "http://www.youtube.com/watch?v=1FJHYqE0RDg";
                 videoUrl = getUrlVideoRTSP(url);
                 Log.e("Video url for playing=========>>>>>", videoUrl);
             }
             catch (Exception e)
             {
                 Log.e("Login Soap Calling in Exception", e.toString());
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(Void result)
         {
             super.onPostExecute(result);
             progressDialog.dismiss();
             Log.v("prepare to post excuse", "1");
             vv.setVideoURI(Uri.parse(videoUrl));
             vv.setMediaController(mc);
             vv.requestFocus();
             vv.start();          
             mc.show();         
         }
 
     }
 
 public static String getUrlVideoRTSP(String urlYoutube)
     {
         try
         {
             String gdy = "http://gdata.youtube.com/feeds/api/videos/";
             DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
             String id = extractYoutubeId(urlYoutube);
             URL url = new URL(gdy + id);
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
             Document doc = documentBuilder.parse(connection.getInputStream());
             Element el = doc.getDocumentElement();
             NodeList list = el.getElementsByTagName("media:content");///media:content
             String cursor = urlYoutube;
             for (int i = 0; i < list.getLength(); i++)
             {
                 Node node = list.item(i);
                 if (node != null)
                 {
                     NamedNodeMap nodeMap = node.getAttributes();
                     HashMap<String, String> maps = new HashMap<String, String>();
                     for (int j = 0; j < nodeMap.getLength(); j++)
                     {
                         Attr att = (Attr) nodeMap.item(j);
                         maps.put(att.getName(), att.getValue());
                     }
                     if (maps.containsKey("yt:format"))
                     {
                         String f = maps.get("yt:format");
                         if (maps.containsKey("url"))
                         {
                             cursor = maps.get("url");
                         }
                         if (f.equals("1"))
                             return cursor;
                     }
                 }
             }
             return cursor;
         }
         catch (Exception ex)
         {
             Log.e("Get Url Video RTSP Exception======>>", ex.toString());
         }
         return urlYoutube;
 
     }
 
 protected static String extractYoutubeId(String url) throws MalformedURLException
     {
         String id = null;
         try
         {
             String query = new URL(url).getQuery();
             if (query != null)
             {
                 String[] param = query.split("&");
                 for (String row : param)
                 {
                     String[] param1 = row.split("=");
                     if (param1[0].equals("v"))
                     {
                         id = param1[1];
                     }
                 }
             }
             else
             {
                 if (url.contains("embed"))
                 {
                     id = url.substring(url.lastIndexOf("/") + 1);
                 }
             }
         }
         catch (Exception ex)
         {
             Log.e("Exception", ex.toString());
         }
         return id;
     }
 
 }
