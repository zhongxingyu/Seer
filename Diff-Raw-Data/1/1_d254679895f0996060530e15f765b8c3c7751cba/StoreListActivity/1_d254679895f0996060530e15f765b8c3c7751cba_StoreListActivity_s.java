 package com.fonenet.fonemarket.activity;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 
 import com.fonenet.fonemarket.R;
 import com.fonenet.fonemarket.adapter.MyAdapter;
 import com.fonenet.fonemarket.download.Downloader;
 import com.fonenet.fonemarket.download.LoadInfo;
 import com.fonenet.fonemarket.service.DownloaderService;
 import com.fonenet.fonemarket.service.DownloaderService.MyIBinder;
 import com.fonenet.fonemarket.utils.FoneConstValue;
 import com.fonenet.fonemarket.utils.FoneNetUntils;
 import com.fonenet.fonemarket.xmltools.FoneNetXmlParser;
 import com.fonenet.fonemarket.xmltools.Page;
 
 public class StoreListActivity extends ListActivity {
 
 	public final static int LIST_TYPE_RECOMMAND = 0;
 	public final static int LIST_TYPE_CATEGORY = 1;
 	public final static int LIST_TYPE_RANK = 2;
 	public final static int LIST_TYPE_MANAGE = 3;
 	public final static int LIST_TYPE_MORE = 4;
 
 	private Integer listType = LIST_TYPE_RECOMMAND;
 	private ListView lv;
 	private FoneNetXmlParser parser;
 
 	private Handler handler;
 	private boolean isBound;
 	private Map<String, Downloader> downloaders = new HashMap<String, Downloader>();
 	// ӦĽ
 	private static Map<String, ProgressBar> ProgressBars = new HashMap<String, ProgressBar>();
 	private static Map<String, Button> Buttons = new HashMap<String, Button>();
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		handler = new Handler() {
 			@Override
 			public void handleMessage(Message msg) {// һHandlerڴ߳UIͨѶ
 				if (!Thread.currentThread().isInterrupted()) {
 					if (msg.what == FoneConstValue.MESSAGE_HANDLE_WAHT_DOWNLOADBINDSERVICE) {
 
 						Intent i = new Intent(StoreListActivity.this,
 								DownloaderService.class);
 						Log.i("handle", "bind service");
 
 					} else if (msg.what == FoneConstValue.MESSAGE_HANDLE_WAHT_DOWNLOADSTARTCNF) {
 						LoadInfo loadInfo = (LoadInfo) msg.obj;
 						String urlstr = loadInfo.getUrlstring();
 						ProgressBar bar = ProgressBars.get(urlstr);
 						if (Buttons.get(urlstr) == null) {
 							Log.e("StoreListHandler", "button is null!!!");
 							super.handleMessage(msg);
 							return;
 						}
 
 						Buttons.get(urlstr).setEnabled(true);
 
 						if (msg.arg1 == FoneConstValue.MESSAGE_HANDLE_ARG_DOWNLOADISPAUSE) {
 
 							Buttons.get(urlstr).setText("xia zai");
 							Log.i("handle", "downloader is pause!");
 						} else {
 							showProgress(loadInfo, urlstr,
 									(View) Buttons.get(urlstr));
 							Buttons.get(urlstr).setText("zan ting");
 						}
 
 						Log.i("handle",
 								"receive MESSAGE_HANDLE_WAHT_DOWNLOADSTARTCNF");
 
 					} else if (msg.what == FoneConstValue.MESSAGE_HANDLE_WAHT_DOWNLOADCONTINUE) {
 						Log.i("handle",
 								"receive MESSAGE_HANDLE_WAHT_DOWNLOADCONTINUE");
 
 						if (msg.arg2 == FoneConstValue.FILE_TYPE_STORE_APP) {
 							// LoadInfo loadInfo = (LoadInfo) msg.obj;
 							String url = (String) msg.obj;
 							int conpleteSize = msg.arg1;
 							ProgressBar bar = ProgressBars.get(url);
 							if (bar != null) {
 								// ýȡlengthȸ
 								bar.setProgress(conpleteSize);
 								if (bar.getProgress() == bar.getMax()) {
 
 									String filename = url.substring(url
 											.lastIndexOf("/") + 1);
 									String localfile = FoneConstValue.CONFIG_DOWNLOADPATH
 											+ filename;
 									// Toast.makeText(this, "ɣ", 0).show();
 									// ɺmapе
 									LinearLayout layout = (LinearLayout) bar
 											.getParent();
 									layout.removeView(bar);
 									ProgressBars.remove(url);
 
 									Buttons.get(url).setText("xia zai");
 									Buttons.get(url).setEnabled(true);
 									Buttons.remove(url);
 									FoneNetUntils.installApk(localfile,
 											StoreListActivity.this);
 
 								}
 							}
 						}
 
 					}
 				}
 				super.handleMessage(msg);
 			}
 		};
 
 		Intent i = new Intent(StoreListActivity.this, DownloaderService.class);
 
 		doBindService(this.getApplicationContext(), i);
 
 	}
 
 	public void onStart() {
 		super.onStart();
 		Intent intent = getIntent();
 		// Uri uri;
 		// uri = intent.getData();
 
 		listType = intent.getIntExtra("type", LIST_TYPE_RECOMMAND);
 		updateListView();
 
 	}
 
 	public void onRestart() {
 		super.onRestart();
 	}
 
 	public void onStop() {
 
 		doUnbindService(this.getApplicationContext());
 		super.onStop();
 		// finish();
 		Log.i("Onstop", "doUnbindService!");
 	}
 
 	private void doUnbindService(Context context) {
 		if (isBound) {
 			context.unbindService(myLocalServiceConnection);
 			isBound = false;
 		}
 	}
 
 	private void doBindService(Context context, Intent i) {
 		Log.i("bind", "begin to bind");
 
 		context.bindService(i, myLocalServiceConnection,
 				Context.BIND_AUTO_CREATE);
 
 	}
 
 	private ServiceConnection myLocalServiceConnection = new ServiceConnection() {
 
 		private DownloaderService bsi;
 
 		public void onServiceConnected(android.content.ComponentName name,
 				android.os.IBinder service) {
 
 			// Ϊ ͻ   ͬһڣһͿ֪ "service"ˣҲͿԽʾǿתˡ
 			//  ͻͬһеĻô˴ǲԽʾǿתģ
 			// ΪͨDebugԷִʱ Service  BinderProxy
 			MyIBinder myIBinder = (MyIBinder) service;
 			bsi = (DownloaderService) myIBinder.getService();
 			isBound = true;
 			Log.i("onbinder", "binded!!");
 			bsi.SetActivityHandler(handler);
 
 			bsi.downloadConfigFile();
 		};
 
 		public void onServiceDisconnected(android.content.ComponentName name) {
 
 			isBound = false;
 		};
 	};
 
 	public void updateListView() {
 		updateListView(listType);
 	}
 
 	protected void updateListView(int type) {
 		switch (type) {
 		case LIST_TYPE_CATEGORY:
 			break;
 		case LIST_TYPE_RANK:
 			break;
 		case LIST_TYPE_MANAGE:
 			break;
 		case LIST_TYPE_MORE:
 			break;
 		case LIST_TYPE_RECOMMAND:
 		default:
 			parser = new FoneNetXmlParser(this, FoneConstValue.XML_FOLDER
 					+ "store-recommend.xml");
 			parser.readXML(FoneConstValue.XML_FOLDER + "store-recommend.xml");
 			ArrayList<HashMap<String, Object>> data = getData();
 			MyAdapter adapter = new MyAdapter(this, data);
 			setListAdapter(adapter);
 			break;
 		}
 	}
 
 	/**
 	 * @author chenzheng_java
 	 * @description ׼һЩ
 	 * @return һϢhashMap
 	 */
 	private ArrayList<HashMap<String, Object>> getData() {
 		ArrayList<HashMap<String, Object>> arrayList = new ArrayList<HashMap<String, Object>>();
 		if (parser != null && parser.getPages() != null) {
 			Page page = parser.getPages().get(0);
 			int num = parser.getPages().get(0).getItemNum();
 			for (int i = 0; i < num; i++) {
 				HashMap<String, Object> tempHashMap = new HashMap<String, Object>();
 				Item item = page.getItems().get(i);
 				Bitmap bm = BitmapFactory.decodeFile(FoneConstValue.ICON_FOLDER+item.getIconName());
 				if(bm!=null){
 					tempHashMap.put("image", bm);
 				}
 				else {
 					tempHashMap.put("image", R.drawable.ic_launcher);
 				}
 				String title = item.getName();
 				tempHashMap.put("title", title);
 				tempHashMap.put("url", "http://192.168.7.66/Market4.apk");
 				String info = item.getIntro();
 				tempHashMap.put("info", info);
 				arrayList.add(tempHashMap);
 			}
 		} else {
 			for (int i = 0; i < 3; i++) {
 				HashMap<String, Object> tempHashMap = new HashMap<String, Object>();
 				tempHashMap.put("image", R.drawable.ic_launcher);
 				tempHashMap.put("title", "" + i);
 				tempHashMap.put("info", "Ϣ");
 				tempHashMap.put("url", "http://192.168.7.66/Market4.apk");
 				// tempHashMap.put("title", "2222");
 				// tempHashMap.put("info", "Ϣ");
 				arrayList.add(tempHashMap);
 			}
 		}
 		return arrayList;
 	}
 
 	public void buttonOnClick(View v) {
 		RelativeLayout layout = (RelativeLayout) (v.getParent().getParent());
 		ZuJian zuJian = (ZuJian) (layout.getTag());
 
 		String urlstr = zuJian.url;
 
 		Button button = Buttons.get(urlstr);
 		// if (downLoadStatus == 1)
 		// return;
 
 		// downLoadStatus = 1;
 		if (button == null) {
 			button = (Button) v;
 			button.setEnabled(false);
 
 			Buttons.put(urlstr, button);
 		}
 		// showProgress(null, urlstr, v);
 
 		// õϢĸɼ
 		// LoadInfo loadInfo = DownloaderService.getDownloaderInfo(urlstr);
 		// ʾ
 		// showProgress(loadInfo, urlstr, v);
 		Message msg = Message.obtain();
 		msg.what = FoneConstValue.MESSAGE_HANDLE_WAHT_DOWNLOADSTART;
 		msg.obj = urlstr;
 		DownloaderService.GetServiceHandler().sendMessage(msg);
 
 	}
 
 	/**
 	 * ʾ
 	 */
 	private void showProgress(LoadInfo loadInfo, String url, View v) {
 		ProgressBar bar = ProgressBars.get(url);
 		if (bar == null) {
 			bar = new ProgressBar(this, null,
 					android.R.attr.progressBarStyleHorizontal);
 			bar.setMax(loadInfo.getFileSize());
 			bar.setProgress(loadInfo.getComplete());
 			System.out.println(loadInfo.getFileSize() + "--"
 					+ loadInfo.getComplete());
 
 			ProgressBars.put(url, bar);
 
 			LinearLayout.LayoutParams params = new LayoutParams(
 					LayoutParams.MATCH_PARENT, 5);
 
 			((LinearLayout) (v.getParent())).addView(bar, params);
 		}
 	}
 
 	public static void updateActivity(String urlstr, int length) {
 		ProgressBar bar = ProgressBars.get(urlstr);
 		if (bar != null) {
 			// ýȡlengthȸ
 			bar.incrementProgressBy(length);
 			if (bar.getProgress() == bar.getMax()) {
 
 				String filename = urlstr.substring(urlstr.lastIndexOf("/") + 1);
 				String localfile = FoneConstValue.CONFIG_DOWNLOADPATH
 						+ filename;
 				// Toast.makeText(this, "ɣ", 0).show();
 				// ɺmapе
 				LinearLayout layout = (LinearLayout) bar.getParent();
 				layout.removeView(bar);
 				ProgressBars.remove(urlstr);
 
 				Buttons.get(urlstr).setText("xia zai");
 				Buttons.remove(urlstr);
 				// FoneNetUntils.installApk(localfile,
 				// StoreListActivity.this);
 
 			}
 		}
 	}
 	//
 	// private void setProgressValue(ProgressBar bar, LoadInfo loadInfo) {
 	// if (loadInfo == null)
 	// return;
 	// bar.setMax(loadInfo.getFileSize());
 	// bar.setProgress(loadInfo.getComplete());
 	// System.out.println(loadInfo.getFileSize() + "--"
 	// + loadInfo.getComplete());
 	// }
 
 };
