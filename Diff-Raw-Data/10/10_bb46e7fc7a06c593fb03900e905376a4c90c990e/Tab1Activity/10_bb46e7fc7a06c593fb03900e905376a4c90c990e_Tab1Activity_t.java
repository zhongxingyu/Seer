 package com.fonenet.fonemarket.activity;
 
 /*
  import android.os.Bundle;
  import android.app.Activity;
  import android.view.Menu;
 
  public class Tab1Activity extends Activity {
 
  @Override
  public void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  setContentView(R.layout.activity_tab1);
  }
 
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
  getMenuInflater().inflate(R.menu.activity_tab1, menu);
  return true;
  }
  }
  */
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.zip.ZipException;
 
 import android.app.ListActivity;
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
 
import com.fonenet.fonemarket.FoneNetUntils;
import com.fonenet.fonemarket.R;
 import com.fonenet.fonemarket.adapter.MyAdapter;
 import com.fonenet.fonemarket.download.Downloader;
 import com.fonenet.fonemarket.download.LoadInfo;
 import com.fonenet.fonemarket.utils.FileUtils;
 import com.fonenet.fonemarket.utils.FoneConstValue;
 import com.fonenet.fonemarket.xmltools.FoneNetXmlParser;
 import com.fonenet.fonemarket.xmltools.Page;
 
 public class Tab1Activity extends ListActivity {
 
 	private FoneNetXmlParser parser;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// download file
 
 		handler = new Handler() {
 			@Override
 			public void handleMessage(Message msg) {// һHandlerڴ߳UIͨѶ
 				if (!Thread.currentThread().isInterrupted()) {
 
 					if (msg.what == FoneConstValue.FILE_TYPE_STORE_APP) {
 						String url = (String) msg.obj;
 						int length = msg.arg1;
 						ProgressBar bar = ProgressBars.get(url);
 						if (bar != null) {
 							// ýȡlengthȸ
 							bar.incrementProgressBy(length);
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
 								downloaders.get(url).delete(url);
 								downloaders.get(url).reset();
 								downloaders.remove(url);
 								Buttons.get(url).setText("xia zai");
 								Buttons.remove(url);
 								FoneNetUntils.installApk(localfile,
 										Tab1Activity.this);
 
 							}
 						}
 					} else if (msg.what == FoneConstValue.FILE_TYPE_STORE_CONFIG) {
 						String url = (String) msg.obj;
 						Downloader downloader = downloaders.get(url);
 						if (downloader != null) {
 							LoadInfo loadInfo = downloader
 									.getDownloaderInfors();
 							if (loadInfo.getFileSize() == loadInfo
 									.getComplete()) {
 
 								String filename = FoneConstValue.CONFIG_DOWNLOADPATH
 										+ FoneConstValue.STORE_CONFIG_FILENAME;
 								downloaders.get(url).delete(url);
 								downloaders.get(url).reset();
 								downloaders.remove(url);
 								try {
 
 									File zFile = new File(filename);
 
 									FoneNetUntils.upZipFile(zFile,
 											FoneConstValue.XML_FOLDER);
 								} catch (ZipException e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 								} catch (IOException e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 								}
 
 							}
 						}
 
 					}
 				}
 				super.handleMessage(msg);
 			}
 		};
 
 		downloadConfigFile(); // zb add download store config xml
 		parser = new FoneNetXmlParser(this, FoneConstValue.XML_FOLDER+"store-recommend.xml");
 		parser.readXML(FoneConstValue.XML_FOLDER+"store-recommend.xml");
 		// ȡݣݵĸʽϸҪŶ
 		ArrayList<HashMap<String, Object>> data = getData();
 		// ģSimpleAdapterʵֵԼadapter
 		MyAdapter adapter = new MyAdapter(this, data);
 
 		/**
 		 * Щ˺ԺǶ֪vlist2.xml൱ڴ洢һݵ֣ǰߵĴУһļmain.xmlģ
 		 * ļǷļʾģһжͨsetContentView()ָļġΪûõ
 		 * listViewһء ǿsetListAdapterListActivityеʵ֣ public
 		 * void setListAdapter(ListAdapter adapter) { synchronized (this) {
 		 * ensureList(); mAdapter = adapter; mList.setAdapter(adapter); } }
 		 * һensureList private void ensureList() { if (mList
 		 * != null) { return; }
 		 * setContentView(com.android.internal.R.layout.list_content);
 		 * 
 		 * } ڿˣи setContentViewǵһandroidԼṩĽʾ
 		 * ԭǵۻõģֻListActivityҽʵ֡
 		 */
 		setListAdapter(adapter);
 
 	}
 
 	/**
 	 * @author chenzheng_java
 	 * @description ׼һЩ
 	 * @return һϢhashMap
 	 */
 	private ArrayList<HashMap<String, Object>> getData() {
 		ArrayList<HashMap<String, Object>> arrayList = new ArrayList<HashMap<String, Object>>();
 		if(parser != null && parser.getPages() != null){
 			
 			Page page = parser.getPages().get(0);
 			int num = parser.getPages().get(0).getItemNum();
 			for (int i = 0; i < num; i++) {
 				HashMap<String, Object> tempHashMap = new HashMap<String, Object>();
 				tempHashMap.put("image", R.drawable.ic_launcher);
 				String title = page.getItems().get(i).getName();
 				tempHashMap.put("title", title);
 				tempHashMap.put("url", "http://192.168.7.66/Market4.apk");
 				String info = page.getItems().get(i).getIntro();
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
 
 				arrayList.add(tempHashMap);
 			}
 		}
 		return arrayList;
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 
 		Log.e("Ϣ", "clickitem");
 	}
 
 	public void buttonOnClick(View v) {
 		RelativeLayout layout = (RelativeLayout) (v.getParent().getParent());
 		ZuJian zuJian = (ZuJian) (layout.getTag());
 		int threadcount = FoneConstValue.DOWNLOAD_THREAD_COUNT;
 		String urlstr = zuJian.url;
 		String filename = urlstr.substring(urlstr.lastIndexOf("/") + 1);
 		String localfile = FoneConstValue.CONFIG_DOWNLOADPATH + filename;
 
 		// ʼһdownloader
 		Downloader downloader = downloaders.get(urlstr);
 		if (downloader == null) {
 			downloader = new Downloader(urlstr, localfile, threadcount, this,
 					handler, FoneConstValue.FILE_TYPE_STORE_APP);
 			downloaders.put(urlstr, downloader);
 		}
 		Button button = Buttons.get(urlstr);
 		if (button == null) {
 			button = (Button) v;
 			Buttons.put(urlstr, button);
 		}
 		if (downloader.isdownloading()) {
 			button.setText("xia zai");
 			downloader.pause();
 			return;
 		}
 
 		button.setText("zan ting");
 		// õϢĸɼ
 		LoadInfo loadInfo = downloader.getDownloaderInfors();
 		// ʾ
 		showProgress(loadInfo, urlstr, v);
 		// ÷ʼ
 		downloader.download();
 	}
 
 	private void downloadConfigFile() {
 		String urlstr = FoneConstValue.STORE_CONFIG_URL;
 		String localfile = FoneConstValue.CONFIG_DOWNLOADPATH
 				+ FoneConstValue.STORE_CONFIG_FILENAME;
 		int threadcount = FoneConstValue.DOWNLOAD_THREAD_COUNT;
 
 		// ʼһdownloader
 		Downloader downloader = downloaders.get(urlstr);
 		if (downloader == null) {
 			downloader = new Downloader(urlstr, localfile, threadcount, this,
 					handler, FoneConstValue.FILE_TYPE_STORE_CONFIG);
 			downloaders.put(urlstr, downloader);
 		}
 		downloader.delete(urlstr);
 		downloader.reset();
 
 		// õϢĸɼ
 		downloader.getDownloaderInfors();
 
 		// ÷ʼ
 		downloader.download();
 
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
 
 	private Handler handler;
 	private Map<String, Downloader> downloaders = new HashMap<String, Downloader>();
 	// ӦĽ
 	private Map<String, ProgressBar> ProgressBars = new HashMap<String, ProgressBar>();
 	private Map<String, Button> Buttons = new HashMap<String, Button>();
 }
