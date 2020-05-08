 package tice.weibo.HttpClient;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.HttpVersion;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.client.params.HttpClientParams;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import tice.weibo.App;
 import tice.weibo.DB.DBImagesHelper;
 import tice.weibo.DB.DBPicsHelper;
 import tice.weibo.HttpClient.TwitterClient.DownloadPool.DownloadPiece;
 import tice.weibo.Util.Base64;
 import tice.weibo.Util.TweetsData;
 import tice.weibo.Util.TweetsDataDecoder;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 
 public class TwitterClient {
 
 	public App _App = null;
 
 	public final static String KEY = "value";
 	public final static String KEY_ID = "id";
 
 	private final static String FUNCTION_STATUS[] = {"statuses/friends_timeline","statuses/mentions","direct_messages","direct_messages/sent","favorites","statuses/friends","statuses/followers"};
 	private final static String FUNCTION_DESTORY[] = {"statuses/destroy","","","direct_messages/destroy","favorites/destroy"};
 	private final static String FUNCTION_TRENDS[] = {"current","daily","weekly"};
 
 	public final static int MAX_THREAD_COUNT = 2;
 	public final static int MAX_TWEETS_COUNT = 300;
 
 	final static int UPLOAD_WIDTH = 400;
 	final static int UPLOAD_HEIGHT = 300;
 
 	public final static int REQUEST_TYPE_JSON = 0;
 	public final static int REQUEST_TYPE_XML = 1;
 
 	public final static int HOME_HOME = 0;
 	public final static int HOME_MENTIONS = 1;
 	public final static int HOME_DIRECT = 2;
 	public final static int HOME_DIRECT_SENT = 3;
 	public final static int HOME_FAVORITES = 4;
 	public final static int HOME_FRIENDS = 5;
 	public final static int HOME_FOLLOWERS = 6;
 	public final static int HOME_STATUSES_SHOW = 7;
 	public final static int HOME_SEARCH = 8;
 	public final static int HOME_USERINFO = 9;
 	public final static int HOME_TRENDS = 10;
 
 
 	public final static int TRENDS_CURRENT = 0;
 	public final static int TRENDS_DAILY = 1;
 	public final static int TRENDS_WEEKLY = 2;
 
 	public final static int HTTP_ERROR = 1;
 	public final static int HTTP_CONNECTING = 2;
 
 	public final static int HTTP_HOME_TIMELINE = 3;
 	public final static int HTTP_HOME_TIMELINE_MAXID = 4;
 	public final static int HTTP_HOME_TIMELINE_SINCEID = 5;
 
 	public final static int HTTP_MENTIONS_TIMELINE = 6;
 	public final static int HTTP_MENTIONS_TIMELINE_MAXID = 7;
 	public final static int HTTP_MENTIONS_TIMELINE_SINCEID = 8;
 
 	public final static int HTTP_DIRECT_TIMELINE = 9;
 	public final static int HTTP_DIRECT_TIMELINE_MAXID = 10;
 	public final static int HTTP_DIRECT_TIMELINE_SINCEID = 11;
 
 	public final static int HTTP_DIRECT_TIMELINE_SENT = 12;
 	public final static int HTTP_DIRECT_TIMELINE_SENT_MAXID = 13;
 	public final static int HTTP_DIRECT_TIMELINE_SENT_SINCEID = 14;
 
 	public final static int HTTP_FAVORITES_TIMELINE = 15;
 	public final static int HTTP_FAVORITES_TIMELINE_MAXID = 16;
 	public final static int HTTP_FAVORITES_TIMELINE_SINCEID = 17;
 
 	public final static int HTTP_FRIENDS_TIMELINE = 18;
 	public final static int HTTP_FRIENDS_TIMELINE_MAXID = 19;
 	public final static int HTTP_FRIENDS_TIMELINE_SINCEID = 20;
 
 	public final static int HTTP_FOLLOWERS_TIMELINE = 21;
 	public final static int HTTP_FOLLOWERS_TIMELINE_MAXID = 22;
 	public final static int HTTP_FOLLOWERS_TIMELINE_SINCEID = 23;
 
 	public final static int HTTP_FETCH_IMAGE = 24;
 	public final static int HTTP_STATUSES_UPDATE = 25;
 	public final static int HTTP_DIRECT_MESSAGES_NEW = 26;
 
 	public final static int HTTP_STATUS_DESTORY = 27;
 	public final static int HTTP_MENTIONS_DESTORY = 28;
 	public final static int HTTP_DIRECT_DESTORY = 29; // add 1 since no mention destory
 	public final static int HTTP_DIRECT_DESTORY_SENT = 30;
 	public final static int HTTP_FAVORITES_DESTORY = 31;
 
 	public final static int HTTP_STATUSES_SHOW = 32;
 	public final static int HTTP_FAVORITES_CREATE = 33;
 	public final static int HTTP_SEARCH = 34;
 	public final static int HTTP_SEARCH_NEXT = 35;
 
 	public final static int HTTP_USER_TIMELINE = 36;
 	public final static int HTTP_USER_TIMELINE_MAXID = 37;
 	public final static int HTTP_USER_TIMELINE_SINCEID = 38;
 
 	public final static int HTTP_FRIENDSHIPS_SHOW = 39;
 	public final static int HTTP_FRIENDSHIPS_CREATE = 40;
 	public final static int HTTP_FRIENDSHIPS_DESTORY = 41;
 
 	public final static int HTTP_TRENDS_CURRENT = 42;
 	public final static int HTTP_TRENDS_DAILY = 43;
 	public final static int HTTP_TRENDS_WEEKLY = 44;
 
 	public final static int HTTP_POSTIMAGE_ERROR = 45;
 	public final static int HTTP_POSTIMAGE_SUCCESSFUL = 46;
 	public final static int HTTP_POSTIMAGE_PROGRESS = 47;
 
 	public final static int HTTP_CHECK_VERSION = 48;
 
 	public final static int HTTP_RATE_LIMIT = 49;
 
 	public final static int UI_REFRESHVIEWS = 100;
 	public final static int UI_REFRESHVIEWS_PRE = 101;
 
 	public final static String PREFS_NAME = "tice.twitidiget.setting";
 
 
 	static private Context mCtx;
 	static public String mFilepath;
 
 	//static private Handler mHandler = null;
 	private String mBaseURI = "";
 	private String mSearchURI = "";
 	private String mUsername, mPassword;
 	private DefaultHttpClient httpClient;
 
 //	private static BitmapDrawable BackgroundShape = null;
 //	private static ShapeDrawable RoundShape = null;
 //	private static Bitmap  SwapBitmap = null;
 //	private static Canvas SwapCanvas = null;
 
 	public TwitterClient(Context context, String host,int port, String BaseURI, String SearchURI, String username, String password){
 
 		_App = (App)context.getApplicationContext();
 
 		mCtx = context;
 
 		mBaseURI = BaseURI;
 		mSearchURI = SearchURI;
 		mUsername = username;
 		mPassword = password;
 
 		HttpParams params = new BasicHttpParams();
         HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
         HttpConnectionParams.setSoTimeout(params, 10 * 1000);
         HttpClientParams.setRedirecting(params, true);
         HttpProtocolParams.setUseExpectContinue(params, false);
         HttpProtocolParams.setContentCharset(params, "UTF_8");
 
         SchemeRegistry schemeRegistry = new SchemeRegistry();
         schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
         schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
 
         ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
         httpClient = new DefaultHttpClient(cm, params);
 
         if(_App.mDownloadPool == null){
         	_App.mDownloadPool = new DownloadPool();
         	_App.mDownloadPool.setPriority(Thread.MIN_PRIORITY);
         	_App.mDownloadPool.setName("DownloadPool");
         	_App.mDownloadPool.start();
         }
 
         TestURI(mBaseURI);
         TestURI(mSearchURI);
 
         /*
         if(RoundShape == null){
         	RoundShape = new ShapeDrawable(new RoundRectShape(new float[] { 5, 5, 5, 5, 5, 5, 5, 5 }, null, null));
         	RoundShape.setBounds(0, 0, 48, 48);
         }
 
         if(BackgroundShape == null){
         	BackgroundShape = new BitmapDrawable(BitmapFactory.decodeResource(context.getResources(),R.drawable.iconbg));
         	BackgroundShape.setBounds(0, 0, 48, 48);
         }
 
         if(SwapBitmap == null){
         	SwapBitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.RGB_565);
         }
 
         if(SwapCanvas == null){
         	SwapCanvas = new Canvas(SwapBitmap);
         }
         */
 	}
 
 	static public boolean TestURI(String uri){
 		try {
 			@SuppressWarnings("unused")
 			URI test = new URI(uri);
 			return true;
 		} catch (URISyntaxException e) {
 			String text = String.format("%s is not a legal url. Please check your setting again.", uri);
 			try{
 				new AlertDialog.Builder(mCtx)
 	            .setTitle("URL Error")
 	            .setMessage(text)
 	            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 	                public void onClick(DialogInterface dialog, int whichButton) {
 	 	                }
 	   	            })
 	            .show();
 			}catch (Exception err){}
 			return false;
 		}
 	}
 
 	private void SetCredentials(HttpRequestBase request) {
         if(mUsername.length() != 0 || mPassword.length() != 0){
         	request.addHeader("Authorization", "Basic " + Base64.encode((mUsername+":"+mPassword).getBytes()));
     	}
 	}
 
 	class ImagePostThread extends Thread {
 		private String uri, user, pass, status;
 		Uri file;
 		private Handler mHandler;
 
     	public ImagePostThread(Handler handler, String uri, String user, String pass, Uri file, String status) {
     		this.uri = uri;
     		this.user = user;
     		this.pass = pass;
     		this.file = file;
     		this.mHandler = handler;
     		this.status = status;
     	}
 
     	@Override
     	public void run() {
 
 			try {
 	        	int samplesize = 12;
 	        	BitmapFactory.Options options;
 	        	InputStream thePhoto = mCtx.getContentResolver().openInputStream(file);
 	        	InputStream testSize = mCtx.getContentResolver().openInputStream(file);
 
 	        	options = new BitmapFactory.Options();
 	        	options.inJustDecodeBounds = true;
 	        	BitmapFactory.decodeStream(testSize, null,options);
 
 	        	if(options.outHeight >= options.outHeight){
 	        		samplesize = options.outHeight / ( UPLOAD_HEIGHT * _App._PictureQuality );
 	        	}else{
 	        		samplesize = options.outWidth / ( UPLOAD_WIDTH * _App._PictureQuality );
 	        	}
 
 	        	options.inDither = true;
 	        	options.inSampleSize = samplesize;
 	        	options.inJustDecodeBounds = false;
 	        	options.inPreferredConfig = Bitmap.Config.RGB_565;
 	        	Bitmap bitmap = BitmapFactory.decodeStream(thePhoto, null,options);
 
 	        	ByteArrayOutputStream output = new ByteArrayOutputStream();
 	        	bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output);
 	        	bitmap.recycle();
 	        	bitmap = null;
 
 	        	ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
 
 	    		PictureUploader uploader = new PictureUploader(mHandler, uri, user, pass, status);
 	    		uploader.uploadPicture("upload.jpg", input);
 
 	    		thePhoto.close();
 	    		testSize.close();
 	    		input.close();
 	    		output.close();
 
 			} catch (FileNotFoundException e) {} catch (IOException e) { }
     	}
 	}
 
 	class PostThread extends Thread {
 		private DefaultHttpClient httpClient;
     	private HttpPost httppost;
     	private int mID;
     	private List<NameValuePair> mPostvalues;
     	private Handler mHandler;
     	//private boolean mOAuth;
 
     	public PostThread(boolean oauth, Handler handler, DefaultHttpClient httpClient, HttpPost httppost, List<NameValuePair> postvalues, int id) {
     		String us = httppost.getURI().toASCIIString();
     		System.out.println("us:::::::::::::::::::::::::::"+us);
     		if (us.contains("?")){
     			this.httppost = new HttpPost(us+"&source=1390045420");
     		} else {
     			this.httppost = new HttpPost(us+"?source=1390045420");
     		}
 
     		this.httpClient = httpClient;
 //    		this.httppost = httppost;
     		this.mID = id;
     		this.mPostvalues = postvalues;
     		this.mHandler = handler;
     		//this.mOAuth = oauth;
     	}
 
     	@Override
     	public void run() {
 
             String error = "", data = "";
 			Bundle b = new Bundle();
 
     		try {
     			String name;
     			String value;
     			NameValuePair pair;
     			List<NameValuePair> postvalues = new ArrayList<NameValuePair>();;
 
     			for(int i=0;i<mPostvalues.size();i++){
     				pair = mPostvalues.get(i);
     				if(pair != null){
     					name = pair.getName();
     					value = ShortenLink.ShortenLinkFromText(_App, pair.getValue());
     					postvalues.add(new BasicNameValuePair(name, value));
     				}
     			}
 
     			//if(mOAuth == true && TextUtils.isEmpty(_App._Consumer.getTokenSecret()) == false){
 	    		//	httppost.setEntity(new UrlEncodedFormEntity(postvalues, HTTP.UTF_8));
 	    		//	_App._Consumer.sign( _App._Baseapi,  "twitter.com", httppost);
 	    		//
     			//}else{
         		SetCredentials(httppost);
         		httppost.setEntity(new UrlEncodedFormEntity(postvalues, HTTP.UTF_8));
     			//}
 
     			HttpResponse response = httpClient.execute(httppost);
 
     	 		int status = response.getStatusLine().getStatusCode();
 
     	 		if (status != HttpStatus.SC_OK) {
      				error = response.getStatusLine().getReasonPhrase();
      				HttpEntity entity = response.getEntity();
 	 				data = TweetsDataDecoder.inputStreamToString(entity);
 	 				b.putString(KEY, error + ":" + DecodeJSON(data));
 	 				SendMessage(mHandler, HTTP_ERROR, b);
      			} else {
      				HttpEntity entity = response.getEntity();
 	 				data = TweetsDataDecoder.inputStreamToString(entity);
 
 	 				b.putString(KEY, data);
 	 				b.putLong(KEY_ID, mID);
 	 				SendMessage(mHandler, mID, b);
      			}
 
     		} catch (Exception e) {
     			httppost.abort();
     			Bundle err = new Bundle();
     			err.putString(KEY, e.getLocalizedMessage());
     			SendMessage(mHandler, HTTP_ERROR, err);
     		}
     	}
 	}
 
     static public String GetSmartTimeString(long d){
     	String ret = "";
 
     	long now = System.currentTimeMillis();
 
     	long offset = (now - d) / 1000;
 
     	if(offset <=0 ) offset = 1;
 
     	if (offset <= 60){
     		ret = String.format("%d secs ago", offset);
     	}else if (offset > 60 && offset <= 3600){
     		ret = String.format("%d mins ago", offset / 60);
     	}else if (offset > 3600 && offset <= 86400){
     		ret = String.format("%d hours ago", offset / 3600 );
     	}else if (offset > 86400 && offset <= 604800 ){
     		ret = String.format("%d days ago", offset / 86400 );
     	}else if (offset > 604800 && offset <= 2419200 ){
     		ret = String.format("%d weeks ago", offset / 604800 );
     	}else{
     		SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
     		ret = String.format("%s", df2.format(new Date(d)));
     	}
 
     	return ret;
     }
 
 	static private String DecodeJSON(String data){
 
 		String ret = "";
 
 		try {
 			JSONObject json = new JSONObject(data);
 
 			ret = json.getString("error");
 
 		} catch (JSONException e) {
 			ret = data;
 		}
 
 		return ret;
 	}
 
 	class GetThread extends Thread {
 		private DefaultHttpClient httpClient;
     	private HttpGet httpget;
     	private int mID;
     	private Handler mHandler;
     	private int mFormat;
 
     	public GetThread(int format, Handler handler, DefaultHttpClient httpClient, HttpGet httpget, int id) {
     		String us = httpget.getURI().toASCIIString();
     		System.out.println("us:::::::::::::::::::::::::::"+us);
     		if (us.contains("?")){
     			this.httpget = new HttpGet(us+"&source=1390045420");
     		} else {
     			this.httpget = new HttpGet(us+"?source=1390045420");
     		}
     		this.httpClient = httpClient;
 //    		this.httpget = httpget;
     		this.mID = id;
     		this.mHandler = handler;
     		this.mFormat = format;
     	}
 
     	@Override
     	public void run() {
 
             String error = "", data = "";
 			Bundle b = new Bundle();
 
     		try {
     			SetCredentials(httpget);
     			HttpResponse response = httpClient.execute(httpget);
 
     	 		int status = response.getStatusLine().getStatusCode();
 
     	 		if (status != HttpStatus.SC_OK) {
      				error = response.getStatusLine().getReasonPhrase();
      				HttpEntity entity = response.getEntity();
 	 				data = TweetsDataDecoder.inputStreamToString(entity);
      				b.putString(KEY, error + ":" + DecodeJSON(data));
 	 				SendMessage(mHandler, HTTP_ERROR, b);
      			} else {
      				setPriority(Thread.NORM_PRIORITY - 1);
 
      				HttpEntity entity = response.getEntity();
             // InputStream in = entity.getContent();
             //  BufferedInputStream bin = new BufferedInputStream(in);
             //  int shit;
             // System.out.println("=====================");
             //  while ( ( shit = bin.read() ) != -1 )
             //  {
             //
             //      char c = (char)shit;
             //
             //      System.out.print(""+(char)shit); //This prints out content that is unreadable.
             //                                    //Isn't it supposed to print out html tag?
             //  }
      				TweetsDataDecoder decoder = new TweetsDataDecoder();
 	 				TweetsData value = decoder.Decoder(mFormat, mHandler, mID, entity, _App._RemoveAD);
 	 				if(value.mError == null){
 	 					b.putSerializable(KEY, value);
 	 					SendMessage(mHandler, mID, b);
 	 				}
 	 				else{
 	 					throw new Exception(value.mError);
 	 				}
      			}
 
     		} catch (Exception e) {
     			httpget.abort();
     			Bundle err = new Bundle();
     			err.putString(KEY, e.getMessage());
     			SendMessage(mHandler, HTTP_ERROR, err);
     		}
     	}
 	}
 
 	class FetchImage extends Thread {
 		private DefaultHttpClient httpClient;
     	private HttpGet httpget;
     	private int mID;
     	private String mName = "";
     	private Long mStatus_id = 0l;
     	private Handler mHandler;
 
     	public FetchImage(Handler handler, DefaultHttpClient httpClient, HttpGet httpget, int id, String name) {
     		this.httpClient = httpClient;
     		this.httpget = httpget;
     		this.mID = id;
     		this.mName = name;
     		this.mHandler = handler;
     	}
 
     	public FetchImage(Handler handler, DefaultHttpClient httpClient, HttpGet httpget, int id, Long status_id) {
     		this.httpClient = httpClient;
     		this.httpget = httpget;
     		this.mID = id;
     		this.mStatus_id = status_id;
     		this.mHandler = handler;
     	}
 
     	@Override
     	public void run() {
 
     		try {
     			HttpResponse response = httpClient.execute(httpget);
     			int status = response.getStatusLine().getStatusCode();
     			if (status != HttpStatus.SC_OK) {
     			} else {
     				try{
 	    				byte[] buffer = RoundImage(EntityUtils.toByteArray(response.getEntity()));
 	    				//SaveIcon(mName, EntityUtils.toByteArray(response.getEntity()));
 	    				if (mName != "" && mStatus_id == 0l){
 	    					SaveIcon(mName, buffer);
 	    				} else if (mName == "" && mStatus_id > 0l){
 	    					SaveIcon(mStatus_id, buffer);
 	    				}
 	    				SendMessage(mHandler, mID, null);
     				}catch (Exception e){};
     			}
     		} catch (Exception e) { }
 
     		_App.mDownloadPool.ActiveThread_Pop();
     	}
 	}
 
  	static public void SendMessage(Handler handler, int msg, Bundle data){
  		if (handler == null) return;
 
 		Message message = new Message();
         message.what = msg;
         message.setData(data);
         handler.sendMessage(message);
  	}
 
 /*
  	private void InitFilePath(){
  		try{
  			String sdpath = android.os.Environment.getExternalStorageDirectory().getPath();
  			String cachepatch = String.format("%s/twitidget/cache", sdpath);
 
 	 		File sdRoot = new File(sdpath);
 	 		if(sdRoot.canWrite() ==true){
 	 			File cache = new File(cachepatch);
 	 			if (cache.isDirectory() == false){
 	 				cache.mkdirs();
 	 			}
 	 			mFilepath = cachepatch;
 
 	 		}else{
 	 			mFilepath = "";
 	 		}
 
  		}catch (Exception e) {}
  	}
 
  	public void FrechImgFromFile(Handler handler,String uri, String name){
  		String file = String.format("%s/%s", mFilepath,name);
  		File sdRoot = new File(file);
 
  		if(sdRoot.isFile() == false){
  			HttpGet request = new HttpGet(uri);
  			FetchImage thread = new FetchImage(handler, httpClient, request,HTTP_FETCH_IMAGE, name);
  			thread.start();
  		}
  	}
 
  	static public Bitmap LoadIconFromFile(String name){
     	Bitmap icon = null;
     	if(TwitterClient.mFilepath.length() != 0){
     		String filepath = String.format("%s/%s", TwitterClient.mFilepath,name);
     		icon =  BitmapFactory.decodeFile(filepath);
     	}else{
     		FileInputStream is = null;
 			try {
 				is = mCtx.openFileInput(name);
 			} catch (FileNotFoundException e) {}
     		icon = BitmapFactory.decodeStream(is);
     	}
     	return icon;
  	}
 */
 
  	public class DownloadPool extends Thread{
 
  		public class DownloadPiece{
  			public String uri;
  			public String name = "";
  			public Long status_id = 0l;
  			Handler handler;
 
  			public DownloadPiece(Handler handler,String uri,String name){
  				this.uri = uri;
  				this.name = name;
  				this.handler = handler;
  			}
 
  			public DownloadPiece(Handler handler,String uri,Long status_id){
  				this.uri = uri;
  				this.status_id = status_id;
  				this.handler = handler;
  			}
  		}
 
  		private int mActiveThread = 0;
  		private ArrayList<DownloadPiece> mQuery = new ArrayList<DownloadPiece>();
 
     	@Override
     	synchronized public void run() {
 
     		while (true){
     			notifyAll();
 	    		while(GetCount() == 0 || GetThreadCount() == MAX_THREAD_COUNT){
 	    			try {
 						wait();
 					} catch (InterruptedException e) {}
 	    		}
 
 				DownloadPiece p = Pop();
 				if( p.name != "" && p.status_id == 0l ){
 					FrechImg_Impl(p.handler,p.uri,p.name);
 				}else if(p.name == "" && p.status_id > 0l){
 					FrechImg_Impl(p.handler,p.uri,p.status_id);
 				}
     		}
     	}
 
     	synchronized private int GetCount(){
     		return mQuery.size();
     	}
 
     	synchronized private DownloadPiece Get(int i){
     		if(i >= mQuery.size()) return null;
     		return mQuery.get(i);
     	}
 
     	synchronized private DownloadPiece Pop(){
      		DownloadPiece ret = mQuery.get(0);
      		mQuery.remove(0);
      		return ret;
      	}
 
     	synchronized private void Push(Handler handler, String uri, String name){
      		mQuery.add(new DownloadPiece(handler, uri,name));
      		notifyAll();
      	}
 
     	synchronized private void Push(Handler handler, String uri, Long status_id){
      		mQuery.add(new DownloadPiece(handler, uri,status_id));
      		notifyAll();
      	}
 
     	synchronized public int GetThreadCount(){
      		return mActiveThread;
      	}
 
      	synchronized public void ActiveThread_Push(){
      		mActiveThread ++;
      	}
 
      	synchronized public void ActiveThread_Pop(){
      		mActiveThread --;
      		notifyAll();
      	}
 
  	}
 
 
  	synchronized public byte[] RoundImage(byte[] data){
  		byte[] image = null;
 
  		try{
 	 		Bitmap bitmap;
 	 		int samplesize = 12;
 	 		BitmapFactory.Options options;
 	 		ByteArrayOutputStream output = new ByteArrayOutputStream();
 
 	    	options = new BitmapFactory.Options();
 	    	options.inJustDecodeBounds = true;
 	    	BitmapFactory.decodeByteArray(data, 0, data.length, options);
 
 	    	if(options.outHeight >= options.outHeight){
 	    		samplesize = options.outHeight / 48;
 	    	}else{
 	    		samplesize = options.outWidth / 48;
 	    	}
 
 	    	options.inDither = true;
 	    	options.inSampleSize = samplesize;
 	    	options.inJustDecodeBounds = false;
 	    	options.inPreferredConfig = Bitmap.Config.RGB_565;
 	    	bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
 //	    	Shader shader = new BitmapShader(bitmap, Shader.TileMode.REPEAT ,Shader.TileMode.REPEAT);
 
 //			SwapCanvas.drawColor(0xfffafafa);
 //			BackgroundShape.draw(SwapCanvas);
 
 //			RoundShape.getPaint().setColor(0xfffafafa);
 //			RoundShape.getPaint().setShader(shader);
 //			RoundShape.draw(SwapCanvas);
 
 //			SwapBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
 	    	bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
 	    	image  = output.toByteArray();
 
 	    	bitmap.recycle();
 	    	output.close();
 
  		}catch (Exception e){}
 
  		return image;
  	}
 
 
  	synchronized public void FrechImg(Handler handler,String uri, String name){
 
  		if(_App._Displayicon == false) return;
  		if(uri == null || name == null) return;
  		if(uri.length() == 0 || name.length() == 0) return;
 
  		DownloadPiece ret = null;
 		boolean bfound = false;
 
 		for(int i=0;i<_App.mDownloadPool.GetCount();i++){
 			ret = _App.mDownloadPool.Get(i);
 			if(ret != null && ret.name.length() != 0 && ret.name.equalsIgnoreCase(name) == true){
 				bfound = true;
 				break;
 			}
 		}
 
 		if(bfound == false){
 			_App.mDownloadPool.Push(handler, uri, name);
 	 	}
  	}
 
  	synchronized public void FrechImg(Handler handler,String uri, Long status_id){
 
     // if(_App._Displayicon == false) return;
  		if(uri == null || status_id == null) return;
  		if(uri.length() == 0 || status_id <= 0) return;
 
  		DownloadPiece ret = null;
 		boolean bfound = false;
 
 		for(int i=0;i<_App.mDownloadPool.GetCount();i++){
 			ret = _App.mDownloadPool.Get(i);
 			if(ret != null && ret.status_id >= 0l && ret.status_id == status_id){
 				bfound = true;
 				break;
 			}
 		}
 
 		if(bfound == false){
 			_App.mDownloadPool.Push(handler, uri, status_id);
 	 	}
  	}
 
 
  	public void FrechImg_Impl(Handler handler,String uri, String name){
  		if (_App._DbHelper == null) return;
  		if(uri == null || name == null) return;
 		Cursor imageCursor = _App._DbHelper.fetchImage(name);
 		if (imageCursor == null) return;
 		if (imageCursor.getCount() == 0) {
 			_App.mDownloadPool.ActiveThread_Push();uri = Uri.encode(uri,":/");
 			HttpGet request = new HttpGet(uri);
 			FetchImage thread = new FetchImage(handler, httpClient, request,HTTP_FETCH_IMAGE, name);
 			thread.setPriority(Thread.NORM_PRIORITY - 3);
 			thread.start();
 		}else{
 			Date now = new Date();
 			long time = imageCursor.getLong(DBImagesHelper.COL_TIME);
 			if( now.getTime() - time >= 3 * 24 * 60 * 60 * 1000){
 				_App.mDownloadPool.ActiveThread_Push();uri = Uri.encode(uri,":/");
 				HttpGet request = new HttpGet(uri);
 				FetchImage thread = new FetchImage(handler, httpClient, request,HTTP_FETCH_IMAGE, name);
 				thread.setPriority(Thread.NORM_PRIORITY - 3);
 				thread.start();
 			}else{
 			}
 		}
 
 		imageCursor.close();
  	}
 
  	public void FrechImg_Impl(Handler handler,String uri, Long status_id){
  		if (_App._DbHelper == null) return;
  		if(uri == null || status_id == null) return;
 		Cursor imageCursor = _App._DbHelper.fetchPics(status_id);
 		if (imageCursor == null) return;
 		if (imageCursor.getCount() == 0) {
 			_App.mDownloadPool.ActiveThread_Push();uri = Uri.encode(uri,":/");
 			HttpGet request = new HttpGet(uri);
 			FetchImage thread = new FetchImage(handler, httpClient, request,HTTP_FETCH_IMAGE, status_id);
 			thread.setPriority(Thread.NORM_PRIORITY - 3);
 			thread.start();
 		}
     // else{
 //			Date now = new Date();
 //			long time = imageCursor.getLong(DBPicsHelper.COL_TIME);
 //			if( now.getTime() - time >= 3 * 24 * 60 * 60 * 1000){
         // _App.mDownloadPool.ActiveThread_Push();uri = Uri.encode(uri,":/");
         // HttpGet request = new HttpGet(uri);
         // FetchImage thread = new FetchImage(handler, httpClient, request,HTTP_FETCH_IMAGE, status_id);
         // thread.setPriority(Thread.NORM_PRIORITY - 3);
         // thread.start();
 //			}else{
 //			}
     // }
 
 		imageCursor.close();
  	}
 
  	public void FrechImg_Impl2(Handler handler,String uri, String name){
 		HttpGet request = new HttpGet(uri);
 		FetchImage thread = new FetchImage(handler, httpClient, request,HTTP_FETCH_IMAGE, name);
 		thread.setPriority(Thread.MIN_PRIORITY);
 		thread.start();
  	}
 
  	synchronized public void SaveIcon(String name, byte[] data){
  		if (_App._DbHelper == null) return;
  		_App._DbHelper.InsertImage(name, data);
     }
 
  	synchronized public void SaveIcon(Long status_id, byte[] data){
  		if (_App._DbHelper == null) return;
  		_App._DbHelper.InsertPic(status_id, data);
     }
 
  	public Bitmap LoadIcon(Handler handler, String name, String iconuri){
     	Bitmap icon = null;
     	byte[] imagedata = null;
 
     	if(_App._DbHelper == null) return null;
     	if(name == null || name.length() == 0) return icon;
 
     	Cursor imageCursor = _App._DbHelper.fetchImage(name);
     	if (imageCursor == null) return null;
     	if (imageCursor.getCount() != 0) {
     		imageCursor.moveToFirst();
     		imagedata = imageCursor.getBlob(DBImagesHelper.COL_DATA);
         	if(imagedata.length != 0){
         		try{
         			icon = BitmapFactory.decodeByteArray(imagedata, 0, imagedata.length);
         		}catch (Exception e) {
         			Bundle err = new Bundle();
         			err.putString(KEY, "OutOfMemoryError: bitmap size exceeds VM budget");
         			SendMessage(handler, HTTP_ERROR, err);
         		}
         	}
     	}else{
     		if(iconuri != null && iconuri.length() != 0){
     			FrechImg(handler,iconuri, name);
     		}
     	}
 
     	imageCursor.close();
 
     	return icon;
     }
 
   public Bitmap LoadPic(Handler handler, Long status_id, String picurl){
     Bitmap icon = null;
     byte[] imagedata = null;
     System.out.println("status_id:::::::::::"+status_id);
     System.out.println("picurl:::::::::::"+picurl);
 
     if(_App._DbHelper == null) return null;
     if(status_id == null || status_id <= 0) return icon;
 
     Cursor imageCursor = _App._DbHelper.fetchPics(status_id);
     if (imageCursor == null) return null;
     if (imageCursor.getCount() != 0) {
       imageCursor.moveToFirst();
       imagedata = imageCursor.getBlob(DBPicsHelper.COL_DATA);
         if(imagedata.length != 0){
           try{
             icon = BitmapFactory.decodeByteArray(imagedata, 0, imagedata.length);
           }catch (Exception e) {
             Bundle err = new Bundle();
             err.putString(KEY, "OutOfMemoryError: bitmap size exceeds VM budget");
             SendMessage(handler, HTTP_ERROR, err);
           }
         }
     }else{
       if(picurl != null && picurl.length() != 0){
         FrechImg(handler,picurl, status_id);
       }
     }
 
     imageCursor.close();
 
     return icon;
    }
 
 	public void Get_timeline(Handler handler, int type){
 		try{
 			String url = String.format("%s/%s.xml?count=%d",mBaseURI,FUNCTION_STATUS[type],_App._Tweetscount);
 		 	HttpGet request = new HttpGet(url);
 		 	GetThread thread = new GetThread(REQUEST_TYPE_XML, handler, httpClient, request, HTTP_HOME_TIMELINE + type * 3);
 		 	thread.start();
 		} catch (Exception e) {
 			Bundle err = new Bundle();
 			err.putString(KEY, e.getLocalizedMessage());
 			SendMessage(handler, HTTP_ERROR, err);
 		}
 	}
 
 	public void Get_timeline_max(Handler handler, int type, long id){
 		try{
 			String url = String.format("%s/%s.xml?max_id=%d&count=%d",mBaseURI,FUNCTION_STATUS[type],id,_App._Tweetscount);
 		 	HttpGet request = new HttpGet(url);
 		 	GetThread thread = new GetThread(REQUEST_TYPE_XML, handler, httpClient, request, HTTP_HOME_TIMELINE_MAXID + type * 3);
 	 	thread.start();
 		} catch (Exception e) {
 			Bundle err = new Bundle();
 			err.putString(KEY, e.getLocalizedMessage());
 			SendMessage(handler, HTTP_ERROR, err);
 		}
 	}
 
 /*
 	public void Get_timeline_since(Handler handler, int type, long id, int page){
 	 	String url = String.format("%s/%s.json?since_id=%d&count=%d",mBaseURI,FUNCTION_STATUS[type],id,TweetsListActivity._Tweetscount);
 	 	HttpGet request = new HttpGet(url);
 	 	GetThread thread = new GetThread(handler, httpClient, request, HTTP_HOME_TIMELINE_SINCEID + type * 3);
 	 	thread.start();
 	}
 */
 
 	public Thread Get_timeline_since_single(Handler handler, int type, long id){
 
 		GetThread thread = null;
 
 		try{
 			String url = String.format("%s/%s.xml?since_id=%d&count=25",mBaseURI,FUNCTION_STATUS[type],id);
 		 	HttpGet request = new HttpGet(url);
 		 	thread = new GetThread(REQUEST_TYPE_XML, handler, httpClient, request, HTTP_HOME_TIMELINE_SINCEID + type * 3);
 		 	thread.start();
 		} catch (Exception e) {
 			Bundle err = new Bundle();
 			err.putString(KEY, e.getLocalizedMessage());
 			SendMessage(handler, HTTP_ERROR, err);
 		}
 
 		return thread;
 	}
 
 	public void Post_statuses_post(Handler handler, long replyid, String text){
 	 	try{
 			String url = String.format("%s/statuses/update.json",mBaseURI);
 
 		 	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 			nameValuePairs.add(new BasicNameValuePair("status", text));
 			nameValuePairs.add(new BasicNameValuePair("source", "Twigee"));
 
 		 	if(replyid != 0){
 		 		nameValuePairs.add(new BasicNameValuePair("in_reply_to_status_id", String.valueOf(replyid)));
 		 	}
 
 		 	HttpPost request = new HttpPost(url);
 		 	PostThread thread = new PostThread(true, handler, httpClient, request,nameValuePairs, HTTP_STATUSES_UPDATE);
 		 	thread.start();
 		} catch (Exception e) {
 			Bundle err = new Bundle();
 			err.putString(KEY, e.getLocalizedMessage());
 			SendMessage(handler, HTTP_ERROR, err);
 		}
 
 	}
 
 	public void Post_direct_messages_new(Handler handler, String screenname, String text){
 	 	try{
 			String url = String.format("%s/direct_messages/new.json",mBaseURI);
 
 		 	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("id", screenname));
			nameValuePairs.add(new BasicNameValuePair("screen_name", screenname));
 			nameValuePairs.add(new BasicNameValuePair("text", text));
 
 		 	HttpPost request = new HttpPost(url);
 		 	PostThread thread = new PostThread(false, handler, httpClient, request,nameValuePairs, HTTP_DIRECT_MESSAGES_NEW);
 		 	thread.start();
 		} catch (Exception e) {
 			Bundle err = new Bundle();
 			err.putString(KEY, e.getLocalizedMessage());
 			SendMessage(handler, HTTP_ERROR, err);
 		}
 	}
 
 	public void Post_destory(Handler handler, int type, long id){
 	 	try{
 			String url = String.format("%s/%s/%d.json",mBaseURI,FUNCTION_DESTORY[type],id);
 		 	HttpPost request = new HttpPost(url);
 
 		 	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 			nameValuePairs.add(new BasicNameValuePair("id", String.valueOf(id)));
 
 		 	PostThread thread = new PostThread(false, handler, httpClient, request, nameValuePairs, HTTP_STATUS_DESTORY + type);
 		 	thread.start();
 		} catch (Exception e) {
 			Bundle err = new Bundle();
 			err.putString(KEY, e.getLocalizedMessage());
 			SendMessage(handler, HTTP_ERROR, err);
 		}
 	}
 
 	public void Get_statuses_show(Handler handler, String replyid){
 	 	try{
 			String url = String.format("%s/statuses/show/%s.xml",mBaseURI,replyid);
 		 	HttpGet request = new HttpGet(url);
 		 	GetThread thread = new GetThread(REQUEST_TYPE_XML, handler, httpClient, request, HTTP_STATUSES_SHOW);
 		 	thread.start();
 		} catch (Exception e) {
 			Bundle err = new Bundle();
 			err.putString(KEY, e.getLocalizedMessage());
 			SendMessage(handler, HTTP_ERROR, err);
 		}
 	}
 
 	public void Post_favorites_create(Handler handler, long id){
 		try{
 			String url = String.format("%s/favorites/create/%d.json",mBaseURI,id);
 
 		 	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 			nameValuePairs.add(new BasicNameValuePair("id", String.valueOf(id)));
 
 		 	HttpPost request = new HttpPost(url);
 		 	PostThread thread = new PostThread(false, handler, httpClient, request,nameValuePairs, HTTP_FAVORITES_CREATE);
 		 	thread.start();
 		} catch (Exception e) {
 			Bundle err = new Bundle();
 			err.putString(KEY, e.getLocalizedMessage());
 			SendMessage(handler, HTTP_ERROR, err);
 		}
 	}
 
 	public void Get_search(Handler handler, String inputquery){
 	 	try{
 			String query = Uri.encode(inputquery);
 			String url = String.format("%s/search.json?q=%s&rpp=%d",mSearchURI,query,_App._Tweetscount);
 		 	HttpGet request = new HttpGet(url);
 		 	GetThread thread = new GetThread(REQUEST_TYPE_JSON, handler, httpClient, request, HTTP_SEARCH);
 		 	thread.start();
 		} catch (Exception e) {
 			Bundle err = new Bundle();
 			err.putString(KEY, e.getLocalizedMessage());
 			SendMessage(handler, HTTP_ERROR, err);
 		}
 	}
 
 	public void Get_search_next(Handler handler, String inputquery,long id){
 		try{
 			String query = Uri.encode(inputquery);
 			String url = String.format("%s/search.json?q=%s&rpp=%d&max_id=%d",mSearchURI,query,_App._Tweetscount,id);
 		 	HttpGet request = new HttpGet(url);
 		 	GetThread thread = new GetThread(REQUEST_TYPE_JSON, handler, httpClient, request, HTTP_SEARCH_NEXT);
 		 	thread.start();
 		} catch (Exception e) {
 			Bundle err = new Bundle();
 			err.putString(KEY, e.getLocalizedMessage());
 			SendMessage(handler, HTTP_ERROR, err);
 		}
 	}
 
 	public void Get_user_timeline(Handler handler, String user){
 		try{
 			String url = String.format("%s/statuses/user_timeline.xml?count=%d&screen_name=%s",mBaseURI,_App._Tweetscount,user);
 		 	HttpGet request = new HttpGet(url);
 		 	GetThread thread = new GetThread(REQUEST_TYPE_XML, handler, httpClient, request, HTTP_USER_TIMELINE);
 		 	thread.start();
 		} catch (Exception e) {
 			Bundle err = new Bundle();
 			err.putString(KEY, e.getLocalizedMessage());
 			SendMessage(handler, HTTP_ERROR, err);
 		}
 	}
 
 	public void Get_user_timeline_max(Handler handler, String user, long id){
 		try{
 			String url = String.format("%s/statuses/user_timeline.xml?max_id=%d&count=%d&screen_name=%s",mBaseURI,id,_App._Tweetscount,user);
 		 	HttpGet request = new HttpGet(url);
 		 	GetThread thread = new GetThread(REQUEST_TYPE_XML, handler, httpClient, request, HTTP_USER_TIMELINE_MAXID);
 		 	thread.start();
 		} catch (Exception e) {
 			Bundle err = new Bundle();
 			err.putString(KEY, e.getLocalizedMessage());
 			SendMessage(handler, HTTP_ERROR, err);
 		}
 	}
 
 	public void Get_user_timeline_since(Handler handler, String user, long id){
 		try{
 			String url = String.format("%s/statuses/user_timeline.xml?since_id=%d&count=%d&screen_name=%s",mBaseURI,id,_App._Tweetscount,user);
 		 	HttpGet request = new HttpGet(url);
 		 	GetThread thread = new GetThread(REQUEST_TYPE_XML, handler, httpClient, request, HTTP_USER_TIMELINE_SINCEID);
 		 	thread.start();
 		} catch (Exception e) {
 			Bundle err = new Bundle();
 			err.putString(KEY, e.getLocalizedMessage());
 			SendMessage(handler, HTTP_ERROR, err);
 		}
 	}
 
 	public void Get_friendships_show(Handler handler, String source_screen_name, String target_screen_name){
 	 	try{
 		 	String url = String.format("%s/friendships/show.json?source_screen_name=%s&target_screen_name=%s",mBaseURI,source_screen_name,target_screen_name);
 		 	HttpGet request = new HttpGet(url);
 		 	GetThread thread = new GetThread(REQUEST_TYPE_JSON, handler, httpClient, request, HTTP_FRIENDSHIPS_SHOW);
 		 	thread.start();
 		} catch (Exception e) {
 			Bundle err = new Bundle();
 			err.putString(KEY, e.getLocalizedMessage());
 			SendMessage(handler, HTTP_ERROR, err);
 		}
 	}
 
 	public void Get_friendships_create(Handler handler, String screen_name){
 		try{
 			String url = String.format("%s/friendships/create/%s.json",mBaseURI,screen_name);
 
 		 	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 			nameValuePairs.add(new BasicNameValuePair("id", screen_name));
 
 		 	HttpPost request = new HttpPost(url);
 		 	PostThread thread = new PostThread(false, handler, httpClient, request, nameValuePairs, HTTP_FRIENDSHIPS_CREATE);
 		 	thread.start();
 		} catch (Exception e) {
 			Bundle err = new Bundle();
 			err.putString(KEY, e.getLocalizedMessage());
 			SendMessage(handler, HTTP_ERROR, err);
 		}
 	}
 
 	public void Get_friendships_destory(Handler handler, String screen_name){
 	 	try{
 			String url = String.format("%s/friendships/destroy/%s.json",mBaseURI,screen_name);
 
 		 	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
 			nameValuePairs.add(new BasicNameValuePair("id", screen_name));
 
 		 	HttpPost request = new HttpPost(url);
 		 	PostThread thread = new PostThread(false, handler, httpClient, request, nameValuePairs, HTTP_FRIENDSHIPS_DESTORY);
 		 	thread.start();
 		} catch (Exception e) {
 			Bundle err = new Bundle();
 			err.putString(KEY, e.getLocalizedMessage());
 			SendMessage(handler, HTTP_ERROR, err);
 		}
 	}
 
 	public void Get_trends(Handler handler, int type){
 	 	try{
 			String url = String.format("%s/trends/%s.json",mSearchURI,FUNCTION_TRENDS[type]);
 		 	HttpGet request = new HttpGet(url);
 		 	GetThread thread = new GetThread(REQUEST_TYPE_JSON, handler, httpClient, request, HTTP_TRENDS_CURRENT + type);
 		 	thread.start();
 		} catch (Exception e) {
 			Bundle err = new Bundle();
 			err.putString(KEY, e.getLocalizedMessage());
 			SendMessage(handler, HTTP_ERROR, err);
 		}
 	}
 
 	public void Post_image(Handler handler, String uri, String user, String pass, Uri file, String status){
 		try{
 			ImagePostThread thread = new ImagePostThread(handler, uri, user, pass, file, status);
 		 	thread.start();
 		} catch (Exception e) {
 
 //			Bundle err = new Bundle();
 //			err.putString(KEY, e.getLocalizedMessage());
 //			SendMessage(handler, HTTP_ERROR, err);
 		}
 	}
 
 	public void Get_version(Handler handler){
 	 	try{
 			String url = String.format("http://checkversions.appspot.com/twitwalk");
 		 	HttpGet request = new HttpGet(url);
 		 	GetThread thread = new GetThread(REQUEST_TYPE_JSON, handler, httpClient, request, HTTP_CHECK_VERSION);
 		 	thread.start();
 		} catch (Exception e) {
 			Bundle err = new Bundle();
 			err.putString(KEY, e.getLocalizedMessage());
 			SendMessage(handler, HTTP_ERROR, err);
 		}
 	}
 
 	public void Get_friendsfollowers_timeline(Handler handler, int type){
 		try{
 			String url = String.format("%s/%s.xml?count=%d",mBaseURI,FUNCTION_STATUS[type],_App._Tweetscount);
 		 	HttpGet request = new HttpGet(url);
 		 	GetThread thread = new GetThread(REQUEST_TYPE_XML, handler, httpClient, request, HTTP_HOME_TIMELINE + type * 3);
 		 	thread.start();
 		} catch (Exception e) {
 			Bundle err = new Bundle();
 			err.putString(KEY, e.getLocalizedMessage());
 			SendMessage(handler, HTTP_ERROR, err);
 		}
 	}
 
 	public void Get_friendsfollowers_max(Handler handler, int type, long id, int page){
 		try{
 			String url = String.format("%s/%s.xml?count=%d&page=%d",mBaseURI,FUNCTION_STATUS[type],_App._Tweetscount, page);
 		 	HttpGet request = new HttpGet(url);
 		 	GetThread thread = new GetThread(REQUEST_TYPE_XML, handler, httpClient, request, HTTP_HOME_TIMELINE_MAXID + type * 3);
 	 	thread.start();
 		} catch (Exception e) {
 			Bundle err = new Bundle();
 			err.putString(KEY, e.getLocalizedMessage());
 			SendMessage(handler, HTTP_ERROR, err);
 		}
 	}
 
 	public void Get_rate_limit_status(Handler handler){
 		try{
 			String url = String.format("%s/account/rate_limit_status.json",mBaseURI);
 		 	HttpGet request = new HttpGet(url);
 		 	GetThread thread = new GetThread(REQUEST_TYPE_JSON, handler, httpClient, request, HTTP_RATE_LIMIT);
 		 	thread.start();
 		} catch (Exception e) {
 			Bundle err = new Bundle();
 			err.putString(KEY, e.getLocalizedMessage());
 			SendMessage(handler, HTTP_ERROR, err);
 		}
 	}
 }
