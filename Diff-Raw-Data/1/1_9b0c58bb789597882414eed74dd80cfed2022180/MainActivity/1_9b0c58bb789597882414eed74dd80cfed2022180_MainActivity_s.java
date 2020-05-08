 package com.zhangwei.test;
 
 import java.io.IOException;
 import java.lang.ref.WeakReference;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 import android.view.Menu;
 import android.widget.TextView;
 import android.widget.Toast;
 
import org.nutz.json.*;
 
 
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.TimeUnit;
 
 import com.google.gson.Gson;
 import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
 import com.googlecode.concurrentlinkedhashmap.EntryWeigher;
 import com.iflytek.msc.Constants;
 import com.zhangwei.common.cache.WebImageView;
 import com.zhangwei.common.gson.GsonRequest;
 import com.zhangwei.common.gson.GsonTest;
 import com.zhangwei.common.gson.HttpClientTask;
 import com.zhangwei.msc.QISR;
 import com.zhangwei.msc.QTTS;
 import com.zhangwei.service.CommonService;
 
 public class MainActivity extends Activity {
 	public static final int SHOWLINE = 0;
 	public static final int SHOWHTTP = 1;
 	
 	QISRTask t;
 	MyHandler handler;
 	public TextView tv1;
 	public WebImageView im1;
 	
 	public static class MyHandler extends Handler{ 
 		WeakReference<MainActivity> mActivity;
 
         MyHandler(MainActivity activity) {
                 mActivity = new WeakReference<MainActivity>(activity);
         }
 
 		
 		@Override
         public void handleMessage(Message msg) {
 			MainActivity theActivity = mActivity.get();
 			
 			switch(msg.what ){
             case SHOWLINE :
                 theActivity.tv1.setText((String)(msg.obj ));
                break;
             case SHOWHTTP :
             	theActivity.tv1.setText((String)(msg.obj ));
                 break;
 
            }
 
 		}
 	}
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		tv1 = (TextView) this.findViewById(R.id.textView1);
 		im1 = (WebImageView) findViewById(R.id.webImage);
 		handler = new MyHandler(this);
 
 		startService(new Intent(this, CommonService.class));
 		
 /*		QTTSTask t = new QTTSTask();
 		t.execute("5150f897");*/
 /*		h = new MyHandler(this);
 		t = new QISRTask();
 		t.execute(h);*/
 		
 
 
 		im1.setImageUrl(R.drawable.ic_launcher, "http://img2.paipaiimg.com/42b87c2f/item-51452683-1A56F6320000000004010000043EB0C0.0.300x300.jpg");
 
 		//GsonTest.test();
 		/*
 		 * Thread t = new Thread(){ public void run(){ Map<String,Object> map =
 		 * new HashMap<String, Object>(); map.put("name", "Peter");
 		 * map.put("age", 21); map.put("friends", null); String s =
 		 * Json.toJson(map); //Log.d("debug", s); } }; t.start();
 		 */
 		
 		//LRULinkedHashMap t = new LRULinkedHashMap(1000);
 		Gson gson = new Gson();
 		GsonRequest.nearVideoRequest req = new GsonRequest.nearVideoRequest();
 		//.userid = "";
 		req.pageno = "1";
 		req.pagesize = "16";
 		req.latitude = "39.911370";
 		req.longitude = "116.491602";
 		req.videowidth = "320";
 		
 		
 		HttpClientTask task = new HttpClientTask(this, handler);
 		task.execute("http://v.looklook.cn:7074/vs/api/nearVideo", gson.toJson(req));
 		//task.execute("http://v.looklook.cn/vs/api/nearVideo", gson.toJson(req));
 	
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
 	@Override
 	public void onDestroy(){
 		super.onDestroy();
 		
 		if (t != null && t.getStatus() != AsyncTask.Status.FINISHED){
 			t.cancel(true);
 		}
 
 			            
 
 	}
 
 	private class QTTSTask extends AsyncTask<Handler, Void, Void> {
 		@Override
 		protected Void doInBackground(Handler... params) {
 			//String appID = params[0];
 			Handler h = params[0];
 			QTTS q = new QTTS(h, Constants.appID);
 			
 			q.Init();
 			q.Process("/sdcard/zw1.pcm", "abcdefg");
 			
 			q.Fini();
 
 			return null;
 		}
 
 
 	};
 	
 	private class QISRTask extends AsyncTask<Handler, String, String> {
 		@Override
 		protected String doInBackground(Handler... params) {
 			//String appID = params[0];
 			Handler h = params[0];
 			QISR q = new QISR(h, Constants.appID);
 			
 			q.Init();
 			//q.Process("/sdcard/test.wav");
 			q.Process("/sdcard/nihao.wav");
 			//q.Process("/sdcard/zw1.wav");
 			//q.Process("/sdcard/zw1.pcm");
 			q.Fini();
 
 			Log.e("dddd", q.getResult());
 			return q.getResult();
 		}
 
 		@Override
 		protected void onPostExecute (String result) {
 			Toast.makeText(getApplication(), result, Toast.LENGTH_LONG).show();
 		}
 
 		@Override
 		protected void onProgressUpdate (String... values){
 			
 		}
 	};
 
 }
