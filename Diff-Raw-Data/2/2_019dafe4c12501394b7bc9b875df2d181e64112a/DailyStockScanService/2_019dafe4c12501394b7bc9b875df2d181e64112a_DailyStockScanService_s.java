 package com.zhangwei.stock.service;
 
 
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import com.zhangwei.stock.MainActivity;
 import com.zhangwei.stock.R;
 import com.zhangwei.stock.gson.DailyList;
 import com.zhangwei.stock.gson.GoodStock;
 import com.zhangwei.stock.gson.Stock;
 import com.zhangwei.stock.gson.StockList;
 import com.zhangwei.stock.net.TencentStockHelper;
 import com.zhangwei.stock.net.WifiHelper;
 import com.zhangwei.stock.receiver.DailyReceiver;
 import com.zhangwei.stock.receiver.NetworkConnectChangedReceiver;
 import com.zhangwei.stock.utils.DateUtils;
 import com.zhangwei.stocklist.StockListHelper;
 
 import android.app.AlarmManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.ConnectivityManager;
 import android.net.wifi.WifiManager;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.os.Message;
 import android.os.SystemClock;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.app.TaskStackBuilder;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import cn.zipper.framwork.io.network.ZHttp2;
 import cn.zipper.framwork.io.network.ZHttpResponse;
 import cn.zipper.framwork.service.ZService;
 
 public class DailyStockScanService extends ZService {
 	private final String TAG = "DailyStockScanService";
 	
 	private  AlarmManager alarms;
 	private  PendingIntent alarmIntent;
 	//DailyList dailylist;
 	StockList stocklist;
 	private  final long alarm_interval = 24*60*60*1000;  //24 hour
 	
 	private DailyStockScanTask lastLookup;   
 	private KudnsRefreshTask lastRefresh;  
 	private final int HANDLER_FLAG_TODAY_COMPLETE =  0x12345612;
 	private final int HANDLER_FLAG_TASK_COMPLETE =  0x12345678;
 	private final int HANDLER_FLAG_WIFI_CONNECTED = 0x12345679;
 	
 	NetworkConnectChangedReceiver  myBroadcastReceiver;
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		Log.e(TAG, "onCreate");
 	
 	    alarms = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
 
 	    String ALARM_ACTION = DailyReceiver.ACTION_REFRESH_DAILYSCAN_ALARM; 
 	    Intent intentToFire = new Intent(ALARM_ACTION);
 	    alarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, 0);
 	    
 	    
 /*	    myBroadcastReceiver = new NetworkConnectChangedReceiver();
 	    IntentFilter filter = new IntentFilter();
 	    filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
 	    filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
 	    filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
 	    this.registerReceiver(myBroadcastReceiver, filter);*/
 	    
 /*	    LocalBroadcastManager.getInstance(this).registerReceiver(mWifiStatusReceiver,
 	    	      new IntentFilter(NetworkConnectChangedReceiver.ACTION_WIFI_CONNECTED));*/
 
 	}
 	
 	private BroadcastReceiver mWifiStatusReceiver = new BroadcastReceiver() {
 		  @Override
 		  public void onReceive(Context context, Intent intent) {
 		    // Get extra data included in the Intent
 		    //String message = intent.getStringExtra("message");
 		    Log.d("mWifiStatusReceiver", "Got message HANDLER_FLAG_WIFI_CONNECTED" );
 		    
 		    //Message msg = handler.obtainMessage(HANDLER_FLAG_WIFI_CONNECTED);
 		    handler.sendEmptyMessageDelayed(HANDLER_FLAG_WIFI_CONNECTED, 10000);
 		  }
 		};
 
 		
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		Log.e(TAG, "onDestroy");
 		
 /*		this.unregisterReceiver(myBroadcastReceiver);*/
 		
 /*		LocalBroadcastManager.getInstance(this).unregisterReceiver(mWifiStatusReceiver);*/
 	}
 	
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) { 
 		Log.e(TAG, "onStartCommand, flags:" + flags + " startId" + startId);
 		
 	    int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
 	    long timeToRefresh = SystemClock.elapsedRealtime() + alarm_interval;
 	    alarms.setRepeating(alarmType, timeToRefresh, alarm_interval, alarmIntent);  
 	    
 	    //alarms.cancel(alarmIntent);
 	    //dailylist = StockListHelper.getInstance().getDailyList();
 		stocklist = StockListHelper.getInstance().getStockList();
 		
 		DailyStockScan(stocklist.getlastScanID());
 		//stocklist.seekTo("sh600055");
 		//DailyStockScan(stocklist.generateStockID(false));
 
 
 		return Service.START_STICKY;
 	}
 	
 	private void showNotify(String title, String content){
 		int mId = 0x12345678;
 		NotificationCompat.Builder mBuilder =
 		        new NotificationCompat.Builder(this)
 		        .setSmallIcon(R.drawable.ic_launcher)
 		        .setContentTitle(title)
 		        .setContentText(content)
 		        .setAutoCancel(true);
 		
 		Intent resultIntent = new Intent(this, MainActivity.class);
 		resultIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
 		
 		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
 		stackBuilder.addParentStack(MainActivity.class);
 		
 		// Adds the Intent that starts the Activity to the top of the stack
 		stackBuilder.addNextIntent(resultIntent);
 		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT  );
 		mBuilder.setContentIntent(resultPendingIntent);
 		NotificationManager mNotificationManager =
 		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		// mId allows you to update the notification later on.
 		Notification notification = mBuilder.getNotification();
 		notification.tickerText = title;
 		notification.defaults =  Notification.DEFAULT_SOUND | Notification.FLAG_AUTO_CANCEL;
 		mNotificationManager.notify(mId, notification);
 	}
 
 	@Override
 	public boolean handleMessage(Message msg) {
 		// TODO Auto-generated method stub
 		switch(msg.what){
 		case HANDLER_FLAG_TODAY_COMPLETE:
 			break;
 		case HANDLER_FLAG_TASK_COMPLETE:
 			Log.e(TAG, "handle task complete, stopSelf");
 			String now = DateFormat.getDateInstance().format(new Date());
 			showNotify("扫描完成", now);
 			this.stopSelf();
 			break;
 			
 		case HANDLER_FLAG_WIFI_CONNECTED:
 			//只有在service活着的时候才能接收局部广播并重启service
 			//应用在一次异步任务已退出，但没有完成（没有发出HANDLER_FLAG_TASK_COMPLETE）
 			//这时的service还没有结束，等待网络状态的改变
 			Log.e(TAG, "handle wifi connected, refreshVersionCheck");
 /*			Intent startIntent = new Intent(this, DailyStockScanService.class);
 		    this.startService(startIntent);*/
 			//dailylist = StockListHelper.getInstance().getDailyList();
 			stocklist = StockListHelper.getInstance().getStockList();
 			DailyStockScan(stocklist.getlastScanID());
 			
 			//refreshKudns_com();
 		    break;
 		}
 		return false;
 	}
 	
 	
 	public void DailyStockScan(String stockID) {
 	    if (lastLookup==null ||
 	    		lastLookup.getStatus().equals(AsyncTask.Status.FINISHED)) {
 	      lastLookup = new DailyStockScanTask(handler);
 	      lastLookup.execute(stockID);
 
 	    }
 	}
 	
 	public void refreshKudns_com() {
 	    if (lastRefresh==null ||
 	    		lastRefresh.getStatus().equals(AsyncTask.Status.FINISHED)) {
 	    	lastRefresh = new KudnsRefreshTask();
 	    	lastRefresh.execute();
 
 	    }
 	}
 	
 	private class KudnsRefreshTask extends AsyncTask<Void,Void,Void>{
 
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			// TODO Auto-generated method stub
 			//GET /islogin.php HTTP/1.1 (必须，得到PHPSESSID)
 			HashMap<String, String> headers = new HashMap<String, String>();
 			String cookie_str = "Hm_lvt_33ea14b096016df36e0a555e947b927e=1365233496,1365298626,1366705886;";
 			headers.put("User-Agent", "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31");
 			headers.put("Cookie", cookie_str);
 			ZHttp2 http2 = new ZHttp2();
 			http2.setHeaders(headers);
 			ZHttpResponse httpResponse = http2.get("http://www.kudns.com/islogin.php");
 			Map<String, List<String>> ret = httpResponse.getHeaders();
 			List<String> list = ret.get("Set-Cookie");
 			Log.e(TAG, "set-cookie:" + list.get(0));
 			
 			//POST /user/ilogin.php HTTP/1.1  （必须，登陆， 让phpsession合法）
 			headers.put("Cookie", cookie_str + " " + list.get(0) + ";");
 			String post_data = "username=hustwei&pwd=lmx%401984&submit=%26%23160%3B%26%23160%3B%26%23160%3B%26%23160%3B";
 			httpResponse = http2.post("http://www.kudns.com/user/ilogin.php", post_data.getBytes());
 			
 			ret = httpResponse.getHeaders();
 
 			
 			
 			
 			//GET /user/host/add_date.php?Tid=81343 HTTP/1.1
 			headers.put("Cookie", cookie_str + " " + list.get(0) + ";");
 			http2.get("http://www.kudns.com/user/host/add_date.php?Tid=81343");
 			
 			
 			return null;
 		}
 		
 	}
 	
 	/**
 	 * 
 	 *  @param 输入 上次记录的stock: sh600031(上次已完成)
 	 *  @param 输出 这次完成的stock： sh600032
 	 * 
 	 *  @author zhangwei
 	 * */
 	private class DailyStockScanTask extends AsyncTask<String,Void,String>{
 
 		private Handler handler;
 		private boolean update;
 		private boolean isAbort;
 		private String completeID;
 		
 		public DailyStockScanTask(Handler handler) {
 			// TODO Auto-generated constructor stub
 			this.handler = handler;
 			update = false;
 			isAbort = false;
 			completeID = null;
 		}
 
 		@Override
 		protected String doInBackground(String... params) {
 			// TODO Auto-generated method stub
 
 			StockList stocklist = StockListHelper.getInstance().getStockList();
 			String curScanStockID = null;
 			
 			int errCount = 0; //连续出错计数
 			int retry = 0; //重试计数
 			
 			//stocklist.setlastScanID("sz300355");
 			
 			do{
 				Log.e(TAG, " lastStockID:" + curScanStockID + " errCount:" + errCount + " retry:" + retry);
 				if(errCount<1){
 					curScanStockID = stocklist.getCurStockID();
 					Date lastscan_day = new Date(stocklist.getlastScanTime());
 					Date now_day = new Date();
 					
 					if(curScanStockID.equals(StockList.TAIL)){
 						if(DateUtils.compareDay(lastscan_day, now_day)==0){
 							Log.e(TAG,"last scan time is the same day of the today, ingore");
							completeID = StockList.TAIL;
 							break;
 						}else{
 							//new day
 							stocklist.rewind();
 							errCount = 0;
 							retry = 0;
 							continue;
 						}
 
 					}
 					
 				}else{
 					if(TencentStockHelper.getInstance().judgeNetwork()){
 						Log.e(TAG, "www.baidu.com is ok");
 						//网络可用情况下，如果重试超过3次，则说明目的端有问题，取下一个
 						if(retry>3){
 							retry=0;
 							errCount = 0;
 							stocklist.next();
 							continue;
 						}
 
 						retry++;
 
 					}else{
 						Log.e(TAG, "www.baidu.com not connected");
 						//没有网络就一直连接百度看是否能连上
 						try {
 							Thread.sleep(10000);
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 				}
 
 				Log.e(TAG, "curScanStockID:" + curScanStockID);
 				
 				//check net, only wifi can run
 				if(!(WifiHelper.VALUE_WIFI.equals(WifiHelper.getNetType())||WifiHelper.VALUE_3G.equals(WifiHelper.getNetType()))){
 					Log.e(TAG, "WifiHelper,  status:" + WifiHelper.getNetType() + " curScanStockID:" + curScanStockID);
 					isAbort = true;
 					break;
 				}
 				
 				if(isCancelled()){
 					Log.e(TAG, "isCancelled, curScanStockID:" + curScanStockID);
 					isAbort = true;
 					break;
 				}
 				
 				/*stocklist.next();
 				stocklist.setlastScanTime(System.currentTimeMillis());*/
 				
 				Stock stock = TencentStockHelper.getInstance().get_stock_from_tencent(curScanStockID);
 				if(stock!=null){
 					Log.e(TAG, "a stock done,  stock.id:" + stock.id);
 					//lastStockID = stock.id;
 					//实时记录扫描的id到dailyList中
 					//stocklist.setlastScanID(lastStockID);
 					update = true;
 					completeID = stock.id;
 					stocklist.next();
 					stocklist.setlastScanTime(System.currentTimeMillis());
 					errCount = 0;
 					
 					//对比laststock和这个stock是否有变化
 					Stock lastStock = StockListHelper.getInstance().getLastStock(stock.id);
 					
 					if(StockListHelper.isChangeStock(lastStock, stock)){
 						//save stock into history stocks					
 						StockListHelper.getInstance().persistHistoryStock(stock);
 						
 						//save stock into internal storage
 						StockListHelper.getInstance().persistLastStock(stock);
 					}
 					
 					StockListHelper.getInstance().persistStockList(stocklist);
 
 				}else{
 					errCount++;
 				}
 				
 
 				
 			}while(curScanStockID!=null);
 
 
 			
 			Log.e(TAG, "loop over, update:" + update + " isAbort:" + isAbort + " completeID:" + completeID);
 
 			if(update){
 				if(!isAbort){
 					//完成这次扫描(中途被终止的不算)，记录时间
 					stocklist.setlastScanTime(System.currentTimeMillis());
 				}
 				Log.e(TAG, "persistStockList!");
 				StockListHelper.getInstance().persistStockList(stocklist);
 			}
 
 			
 			return completeID;
 		}
 		
 		protected void onPostExecute(String result) {  
 			if(!isAbort && completeID!=null &&  completeID.equals(StockList.TAIL)){
 				handler.sendEmptyMessage(HANDLER_FLAG_TASK_COMPLETE);
 			}
 		}  
 	}
 
 
 }
