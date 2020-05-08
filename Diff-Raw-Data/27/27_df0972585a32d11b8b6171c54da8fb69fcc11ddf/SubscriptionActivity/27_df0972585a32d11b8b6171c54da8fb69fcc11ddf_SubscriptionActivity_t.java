 package com.novel.subscription;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.database.Cursor;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import cn.domob.android.ads.DomobAdEventListener;
 import cn.domob.android.ads.DomobAdManager.ErrorCode;
 import cn.domob.android.ads.DomobAdView;
 
 public class SubscriptionActivity extends Activity {
 	
 	private TextView tvRefresh, tvNet ;
 	private Button btnRefresh;
 	private ProgressBar pbCircle;
 	private boolean isRunning;
 	private ProgressThread pgThread;
 	private ListView lvBookList;
 	
 	private EditText etSearchName;
 	private Button btnSearch;
 	
 	private void refresh(int position) {
 		if(TestNetworkStatus() == -1) {
 			tvNet.setVisibility(View.VISIBLE);
 			tvRefresh.setVisibility(View.GONE);
 			pbCircle.setVisibility(View.GONE);
 			return;
 		}
 		else {
 			tvNet.setVisibility(View.GONE);
 		}
 		
 		btnRefresh.setEnabled(false);
 		if(!isRunning) {
 			isRunning = true;
 			pbCircle.setVisibility(View.VISIBLE);
 			tvRefresh.setVisibility(View.VISIBLE);
 			pgThread = new ProgressThread();
 			
 			Context ctx = getApplicationContext();
 			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
 			
 			boolean autoDownloadInWifi = prefs.getBoolean("autoDownloadInWifi", true);
 			autoDownloadInWifi = autoDownloadInWifi && (TestNetworkStatus() == ConnectivityManager.TYPE_WIFI);
 			
 			pgThread.isRunning = true;
 			pgThread.position = position;
 			pgThread.autoDownloadInWifi = autoDownloadInWifi;
 			pgThread.start();
 		}
 	}
 	
 	private int TestNetworkStatus() {
 		  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		  NetworkInfo ni = cm.getActiveNetworkInfo();
 		  if (ni == null)
 			  return -1;
 		  else {
 			  return ni.getType();
 		  }
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_content);
 		
 		isRunning = false;
 		
 		lvBookList = (ListView)findViewById(R.id.book_list);
 		btnRefresh = (Button) findViewById(R.id.bth_refresh);
 		tvRefresh = (TextView) findViewById(R.id.tvRefresh);
 		tvNet = (TextView) findViewById(R.id.tvNet);
 		
 		etSearchName = (EditText) findViewById(R.id.etSearchName);
 		btnSearch = (Button) findViewById(R.id.btn_OK);
 		
 		pbCircle = (ProgressBar) findViewById(R.id.circlie_progressbar);
 		pbCircle.setIndeterminate(false);
 		pgThread = null;
 		
 		NovelDB db = new NovelDB(getApplicationContext());
 		Cursor cursorSource = db.getRecordsBySQL("select * from source ");
 		try {
 			if(cursorSource != null && cursorSource.moveToFirst()) { 
 				SourceEntity srcEntity = SourceEntity.getSourceEntityFromRecord(cursorSource);
 				srcMap.put(srcEntity.getId(), srcEntity);
 			} 
 		} finally {
 			if(cursorSource != null)
 				cursorSource.close();
 			db.close();
 		}
 		
 		lvBookList.setOnItemClickListener(new OnItemClickListener() {
 			
 			@Override
 			public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
 				
 				Intent intent = new Intent(SubscriptionActivity.this, BookChapterActivity.class);
 				Bundle bundle = new Bundle();
 				bundle.putSerializable("book", ((SubscriptionEntity)adapter.getItemAtPosition(pos)).getBook());
 				bundle.putInt("page", 0);
 				intent.putExtras(bundle);
 				startActivity(intent);
 			}
 			
 		});
 		
 		lvBookList.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> adapter, View view, final int pos, long id) {
 				
 				final SubscriptionEntity subscription = (SubscriptionEntity)(adapter.getItemAtPosition(pos));
 				final String[] arrayFruit = new String[] {"ˢ", "¼½б", "ѡԴ", "ȡ"};
 				new AlertDialog.Builder(SubscriptionActivity.this)
 	              .setTitle("Ĺ")
 	              .setIcon(R.drawable.ic_launcher)
 	              .setItems(arrayFruit, new DialogInterface.OnClickListener() {
 	         
 			         public void onClick(DialogInterface dialog, int which) {
 			        	 if(which == 0) {
 			        		 refresh(pos);
 			        	 }
 			        	 else if(which == 1) {
 			        		 items.get(pos).getBook().setUpdateOrder(0);
 			        		 refresh(pos);
 			        	 }
 			        	 else if(which == 2) {
 			        		Intent intent = new Intent(SubscriptionActivity.this, ChooseSourceActivity.class);
 			 				Bundle bundle = new Bundle();
 			 				bundle.putSerializable("subscription", subscription);
 			 				intent.putExtras(bundle);
 			 				startActivityForResult(intent, 100);
 			        	 }
 			        	 else if(which == 3){
 			        		 new AlertDialog.Builder(SubscriptionActivity.this)
 			 			 	.setTitle("ȡ")
 			 			 	.setMessage("ȡ  " +  subscription.getBook().getName() + " ?")
 			 			 	.setPositiveButton("ǵ", new DialogInterface.OnClickListener() {
 			 					
 			 					@Override
 			 					public void onClick(DialogInterface dialog, int which) {
 			 						NovelDB db = new NovelDB(getApplicationContext());
 			 						
 			 						ContentValues values = new ContentValues();
 			 						values.put(NovelDB.SUBSCRIPTION_VALID, 0);
 			 						db.update(NovelDB.SUBSCRIPTION_TABLE, values, "_id=" + subscription.getId(), null);
 			 						db.close();
 			 						showSubscription();
 			 					}
 			 				})
 			 			 	.setNegativeButton("ȡ", null)
 			 			 	.show();
 			        	 }
 			         }
 			        })
 			        
 			     .setNegativeButton("ȡ", null)
 			     .show();
 				
 				return false;
 			}
 			
 		});
 			
 		btnRefresh.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				refresh(-1);
 			}
 		});
 		
 		
 		btnSearch.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				if(TestNetworkStatus() == -1) {
 					tvNet.setVisibility(View.VISIBLE);
 					tvRefresh.setVisibility(View.GONE);
 					pbCircle.setVisibility(View.GONE);
 					return;
 				}
 				else {
 					tvNet.setVisibility(View.GONE);
 				}
 				
 				searchThread = new SearchThread();
 				searchThread.sKeyWord = etSearchName.getText().toString();
 				btnSearch.setEnabled(false);
 				searchThread.isRunning = true;
 				searchThread.start();
 			}
 		});
 		
		showSubscription();
		
 		mAdContainer = (RelativeLayout) findViewById(R.id.adcontainer);
 		
 		Calendar now = Calendar.getInstance();
 		int hour = now.get(Calendar.HOUR_OF_DAY);
 		
 		// Create ad view
 		mAdview320x50 = new DomobAdView(this, MainActivity.PUBLISHER_ID, MainActivity.InlinePPID, DomobAdView.INLINE_SIZE_320X50);
 		if(hour >= 20)
 			mAdview320x50.setKeyword("game");
 //		mAdview320x50.setUserGender("male");
 //		mAdview320x50.setUserBirthdayStr("2000-08-08");
 //		mAdview320x50.setUserPostcode("123456");
 
 		mAdview320x50.setAdEventListener(new DomobAdEventListener() {
 						
 			@Override
 			public void onDomobAdReturned(DomobAdView adView) {
 				Log.i("DomobSDKDemo", "onDomobAdReturned");				
 			}
 
 			@Override
 			public void onDomobAdOverlayPresented(DomobAdView adView) {
 				Log.i("DomobSDKDemo", "overlayPresented");
 			}
 
 			@Override
 			public void onDomobAdOverlayDismissed(DomobAdView adView) {
 				Log.i("DomobSDKDemo", "Overrided be dismissed");			
 				
 				Calendar now = Calendar.getInstance();
 		    	now.set(Calendar.HOUR_OF_DAY, 0);
 		    	now.set(Calendar.MINUTE, 0);
 		    	now.set(Calendar.SECOND, 0);
 				
 				Context ctx = getApplicationContext();
 				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
 				
 		   		Editor editor = prefs.edit();
 				editor.putLong(ConstDefinition.LAST_CLK_AD_TIME, now.getTimeInMillis());
 				editor.commit();
 				
 				mAdContainer.setVisibility(View.GONE);
 			}
 
 			@Override
 			public void onDomobAdClicked(DomobAdView arg0) {
 				Log.i("DomobSDKDemo", "onDomobAdClicked");				
 			}
 
 			@Override
 			public void onDomobAdFailed(DomobAdView arg0, ErrorCode arg1) {
 				Log.i("DomobSDKDemo", "onDomobAdFailed");				
 			}
 
 			@Override
 			public void onDomobLeaveApplication(DomobAdView arg0) {
 				Log.i("DomobSDKDemo", "onDomobLeaveApplication");				
 			}
 
 			@Override
 			public Context onDomobAdRequiresCurrentContext() {
 				return SubscriptionActivity.this;
 			}
 		});
 		
 		mAdContainer.addView(mAdview320x50);
 		
 	}
 	
 	RelativeLayout mAdContainer;
 	DomobAdView mAdview320x50;
 	
 	private HashMap<Integer, SourceEntity> srcMap = new HashMap<Integer, SourceEntity>();
 	
 	private SearchThread searchThread = null;
 	
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
		
//		showSubscription();
 		if(adapter != null)
 			adapter.notifyDataSetChanged();
 		
 		Calendar now = Calendar.getInstance();
 		Context ctx = getApplicationContext();
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
 		long lastClkAdTime = prefs.getLong(ConstDefinition.LAST_CLK_AD_TIME, 0);
 		if(now.getTimeInMillis() - lastClkAdTime < 86400 * 1000) {
 			mAdContainer.setVisibility(View.GONE);
 		}
 		else {
 			mAdContainer.setVisibility(View.VISIBLE);
 		}
 	}
 	
 	@Override  
     protected void onActivityResult(int requestCode, int resultCode, Intent data)  
     {  
        if(100 == requestCode && 20 == resultCode)  
         {  
         	int book_id = data.getExtras().getInt("book_id");
         	for(int i = 0; i < items.size(); i ++) {
         		if(book_id == items.get(i).getBookId()) {
         			items.get(i).getBook().setUpdateOrder(0);
         			refresh(i);
         			break;
         		}
         	}
         }  
 //        super.onActivityResult(requestCode, resultCode, data);  
     }  
 
 	public HashSet<String> curBookList = new HashSet<String>();
 
 	ArrayList<SubscriptionEntity> items = new ArrayList<SubscriptionEntity>();
 	SubscriptionListViewAdapter adapter;
 	
 	private void showSubscription() {
 		items.clear();
 		NovelDB db = new NovelDB(getApplicationContext());
 		Cursor cursor = db.getRecordsBySQL("select * from subscription where valid=1 ");
 		try {
 			if(cursor != null && cursor.moveToFirst()) {
 				while(!cursor.isAfterLast()) {
 					int _id = cursor.getInt(0);
 					int bookId = cursor.getInt(1);
 					String latestTitle = cursor.getString(2);
 					String latestUrl = cursor.getString(3);
 					String readTitle = cursor.getString(4);
 					String readUrl = cursor.getString(5);
 					int valid = cursor.getInt(6);
 					
 					Cursor cursorBook = db.getRecordsBySQL("select * from book_list where _id = " + bookId);
 					try {
 						if(cursorBook.moveToFirst()) {
 							Cursor cursorSource = db.getRecordsBySQL("select * from source where id=" + cursorBook.getInt(1));
 							try {
 								if(cursorSource.moveToFirst()) {
 									SourceEntity srcEntity = SourceEntity.getSourceEntityFromRecord(cursorSource);
 									
 									BookEntity book = new BookEntity(cursorBook.getInt(0), cursorBook.getString(3), 
 										cursorBook.getString(5), cursorBook.getString(4),cursorBook.getString(8), 
 										cursorBook.getInt(9), cursorBook.getString(10), srcEntity);
 									
 									SubscriptionEntity subscription = 
 											new SubscriptionEntity(_id, bookId, latestTitle, latestUrl, readTitle, readUrl, valid, book);
 									
 									items.add(subscription);
 								}
 							} finally {
 								cursorSource.close();
 							}
 						}
 					} finally {
 						cursorBook.close();
 					}
 					cursor.moveToNext();
 				}
 			}
 		} finally {
 			cursor.close();
 			db.close();
 		}
 		adapter = new SubscriptionListViewAdapter(this, items);
 		lvBookList.setAdapter(adapter);
 	}
 	
 	@Override
 	protected void onStop() {
 		// TODO Auto-generated method stub
 		super.onStop();
 		isRunning = false;
 		if(pgThread != null)
 			pgThread.isRunning = false;
 	}
 	
 	private static final int UPDATE_LIST_COUNT = 50;
 	
 	private static final int START_CODE = 0;
 	private static final int UPDATING_CODE = 1;
 	private static final int FINISHED_CODE = 2;
 	private static final int SEARCH_SUCCESS_CODE = 100;
 	private static final int SEARCH_FAIL_CODE = 200;
 	
 	private static final String BUNDLE_SEARCH_KEY = "keyword";
 	private static final String BUNDLE_BOOK_NAME_KEY = "name";
 	private static final String BUNDLE_BOOK_SITE_KEY = "site";
 	private static final String BUNDLE_BOOK_URL_KEY = "url";
 	private static final String BUNDLE_BOOK_DESC_KEY = "description";
 	private static final String BUNDLE_BOOK_UPDATE_KEY = "update";
 	
 	Handler handler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
         	if(msg.what == START_CODE) {
         		Bundle bundle = msg.getData();
         		int pos = bundle.getInt("position");
         		boolean isBegin = bundle.getBoolean("begin");
         		
         		((SubscriptionEntity)adapter.getItem(pos)).setIsUpdating(isBegin);
         		adapter.notifyDataSetChanged();
         		
 //        		if(adapter != null) {
 //        			ArrayList<ProgressBar> pbItems = adapter.getPbitems();
 //        			if(isBegin)
 //        				pbItems.get(pos).setVisibility(View.VISIBLE);
 //        			else
 //        				pbItems.get(pos).setVisibility(View.GONE);
 //        		}
         	}
         	else if(msg.what == FINISHED_CODE) {
         		pbCircle.setVisibility(View.GONE);
         		tvRefresh.setVisibility(View.GONE);
         		isRunning = false;
         		pgThread = null;
         		btnRefresh.setEnabled(true);
         		showSubscription();
         	}
         	else if(msg.what == SEARCH_SUCCESS_CODE) {
         		btnSearch.setEnabled(true);
         		if(searchThread == null)
         			return;
         		searchThread.isRunning = false;
         		searchThread = null;
         		
         		Bundle bundle = msg.getData();
         		
         		final String bookName = bundle.getString(BUNDLE_BOOK_NAME_KEY);
         		final String bookUrl = bundle.getString(BUNDLE_BOOK_URL_KEY);
         		final String bookDesc = bundle.getString(BUNDLE_BOOK_DESC_KEY);
         		final String bookUpdate = bundle.getString(BUNDLE_BOOK_UPDATE_KEY);
         		final String bookSite = bundle.getString(BUNDLE_BOOK_SITE_KEY);
         		
         		new AlertDialog.Builder(SubscriptionActivity.this)
 				 	.setTitle("ΪҵС˵" + bookName)
 				 	.setMessage("\t" + bookDesc + "\n\n\tѸµ" + bookUpdate + "\n\n㶩?\n")
 				 	.setPositiveButton("ǵ", new DialogInterface.OnClickListener() {
 						
 						@Override
 						public void onClick(DialogInterface dialog, int which) {
 							boolean flag = true;
 							NovelDB db = new NovelDB(getApplicationContext());
 							long _id = -1;
 							try {
 								ContentValues values = new ContentValues();  
 								values.put(NovelDB.BOOK_NAME, bookName);
 								values.put(NovelDB.SRC_ID, 1);
 								values.put(NovelDB.BOOK_URL, bookUrl);
 								values.put(NovelDB.TYPE_ID, 0);
 								values.put(NovelDB.BOOK_SITE, bookSite);
 								_id = db.insert(NovelDB.BOOK_LIST_TABLE, null, values);  
 								
 								BookEntity book = new BookEntity(_id, bookName, "", bookUrl, bookUpdate, 0, bookSite, srcMap.get(1));
 								if(book.getLatestTitle() == null)
 									book.setLatestTitle("");
 								if(db.addSubscription(book) == -1) {
 									flag = false;
 									Toast.makeText(SubscriptionActivity.this, bookName + " Ķб", Toast.LENGTH_SHORT).show();
 								}
 								
 							} finally {
 								db.close();
 							}
 							
 							showSubscription();
 							for(int i = 0; i < items.size(); i ++) {
 				        		if(_id == items.get(i).getBookId()) {
 				        			items.get(i).getBook().setUpdateOrder(0);
 				        			refresh(i);
 				        			break;
 				        		}
 				        	}
 						}
 					})
 				 	.setNegativeButton("ȡ", null).show();
         		
         	}
         	else if(msg.what == SEARCH_FAIL_CODE) {
         		btnSearch.setEnabled(true);
         		if(searchThread == null)
         			return;
         		searchThread.isRunning = false;
         		searchThread = null;
         		
         		Bundle bundle = msg.getData();
         		String keyWord = bundle.getString(BUNDLE_SEARCH_KEY);
         		
         		new AlertDialog.Builder(SubscriptionActivity.this)
 	        		.setTitle("ҽ")  
 	        		.setMessage("ܱǸδҵС˵ " + keyWord)  
 	        		.setPositiveButton("ȷ", null)  
 	        		.show();
         	}
         }
     };
     
     class SearchThread extends Thread {
     	public boolean isRunning = false;
     	public String sKeyWord;
     	
     	@Override
     	public void run() {
     		String htmlContent = "";
 			try {
 				htmlContent = HtmlService.getHtmlByGet(ConstDefinition.MBAIDU_QUERY + URLEncoder.encode(sKeyWord, "utf-8"));
 			} catch (UnsupportedEncodingException e) {
 				Log.i("Exception", e.toString());
 			}
     		Pattern p = Pattern.compile(ConstDefinition.PATTERN_QUERY);
     		Matcher m = p.matcher(htmlContent);
     		if(m.find()) {
     			String novelBody = m.group(1);
     			Pattern pp = Pattern.compile("<a\\s*href=[\"']\\.(.*?)[\"']>[\\s\\S]*?<em>(.*?)</em>");
     			Matcher mm = pp.matcher(novelBody);
     			String novelName = null;
     			String novelUrl = null;
     			if(mm.find()) {
     				novelUrl = mm.group(1);
     				novelName = mm.group(2);
     			}
     			
     			pp = Pattern.compile("class=\"abs\">\\s*?<div>(.*?)</div>");
     			mm = pp.matcher(novelBody);
     			String novelDescription = null;
     			if(mm.find())
     				novelDescription = mm.group(1);
     			
     			pp = Pattern.compile("class=\"abs\">[\\s\\S]*?<a\\s*href.*?>(.*?)</a>");
     			mm = pp.matcher(novelBody);
     			String novelLatestUpdate = null;
     			if(mm.find())
     				novelLatestUpdate = mm.group(1);
     			
     			pp = Pattern.compile("class=\"site\">[\\s\\S]*?(.*?)\\-");
     			mm = pp.matcher(novelBody);
     			String novelSite = null;
     			if(mm.find())
     				novelSite = mm.group(1);
     			
     			Bundle bundle = new Bundle();
     			bundle.putString(BUNDLE_BOOK_URL_KEY, novelUrl.replaceAll("&amp;", "&"));
     			bundle.putString(BUNDLE_BOOK_NAME_KEY, novelName);
     			bundle.putString(BUNDLE_BOOK_SITE_KEY, novelSite);
     			bundle.putString(BUNDLE_BOOK_DESC_KEY, novelDescription);
     			bundle.putString(BUNDLE_BOOK_UPDATE_KEY, novelLatestUpdate);
     			Message msg = handler.obtainMessage();
     			msg.setData(bundle);
     			msg.what = SEARCH_SUCCESS_CODE;
     			handler.sendMessage(msg);
     		}
     		else {
     			Bundle bundle = new Bundle();
     			bundle.putString(BUNDLE_SEARCH_KEY, sKeyWord);
     			Message msg = handler.obtainMessage();
     			msg.setData(bundle);
     			msg.what = SEARCH_FAIL_CODE;
     			handler.sendMessage(msg);
     		}
     	}
     }
     
     class ProgressThread extends Thread
     {
     	public boolean isRunning;
     	public int position;
     	public boolean autoDownloadInWifi;
     	
         @Override
         public void run()
         {
         	isRunning = true;
         	NovelDB db = new NovelDB(getApplicationContext());
 
         	Queue<BookChapterEntity> chapterQueue = new LinkedList<BookChapterEntity>();
         	
         	boolean needUpdateDb = true;//false;
         	int curPage = 0, totalPage = 0;
         	int fetchPageNum = -1;
        	Log.i("test", items.size() + " size");
         	for(int i = 0; i < items.size(); i ++) {
 
         		if(position != -1 && i != position)
         			continue;
         		
         		Message msg = handler.obtainMessage();
         		Bundle bundle = new Bundle();
         		bundle.putInt("position", i);
         		bundle.putBoolean("begin", true);
         		msg.setData(bundle);
         		msg.what = START_CODE;
         		handler.sendMessage(msg);
         	
         		SubscriptionEntity subscription = items.get(i);
         		BookEntity book = subscription.getBook();
        		Log.i("test", book.getName() + " updateOrder: " + book.getUpdateOrder());
         		
         		fetchPageNum = -1;
         		curPage = 0;
         		totalPage = 0;
         		
         		Pattern pChapter = Pattern.compile(book.getSrcEntity().getPatternChapter());
             	Pattern pBody = Pattern.compile(book.getSrcEntity().getPatternBody());
             	Pattern pOrder = Pattern.compile(book.getSrcEntity().getPatternOrder());
             	Pattern pLatestTitle = Pattern.compile(book.getSrcEntity().getPatternLatestTitle());
             	Pattern pPage = Pattern.compile(book.getSrcEntity().getPatternPage());
             	Pattern pInvalidTitle = Pattern.compile("<.*>.*</.*>");
         		
         		//һҳʼң¿ʼ
         		do{
 	        		String res = HtmlService.getHtmlByGet(book.getSrcEntity().getDomain() + book.getUrl() + "&pi=" + fetchPageNum);
 	            	
 	            	Matcher mLatestTitle = pLatestTitle.matcher(res);
 	        		if(mLatestTitle.find()) {
 	        			if(mLatestTitle.group(1).equals(subscription.getLatestTitle()))
 	        				continue;
 	        			else
 	        				needUpdateDb = true;
 	//        			Message msg = handler.obtainMessage();
 	//        			msg.what = UPDATING_CODE;
 	//        			Bundle bundle = new Bundle();
 	//        			bundle.putInt("id", subscription.getId());
 	//        			bundle.putString("title", mLatestTitle.group(1));
 	//        			msg.setData(bundle);
 	//        			handler.sendMessage(msg);
 	        		}
 	            	
 	        		Matcher mBody = pBody.matcher(res);
 	    			String url = "";
 	    			String title = "";
 	    			int chapterOrder = 0;
 	    			String lastUrl, lastTitle;
 	        		if(mBody.find()) {
 	        			String body = mBody.group(1);
 	            		Matcher m = pChapter.matcher(body);
 	            		while(m.find()){// && isRunning) {
 //	            			if(chapterQueue.size() >= UPDATE_LIST_COUNT) {
 //	            				chapterQueue.remove();
 //	            				fetchPageNum = 0;
 //	            				break;
 //	            			}
 	            			lastUrl = url;
 	            			lastTitle = title;
 	            			
 	            			url = m.group(1).replaceAll("&amp;", "&");
 	            			title = m.group(2);
 	            			if(pInvalidTitle.matcher(title).find())
 	            				continue;
 	            			Matcher mUrlOrder = pOrder.matcher(url);
 	            			if(mUrlOrder.find()) {
 	            				chapterOrder = 0;
 	            				try {
 	            					chapterOrder = Integer.parseInt(mUrlOrder.group(1));
 	            				}catch (Exception e) {
 	            					Log.i("test", book.getName() + " " + mUrlOrder.group());
 	            					url = lastUrl;
 	            					title = lastTitle;
 	            					continue;
 	            				}
 	            				if(chapterOrder > book.getUpdateOrder()) {
 	            					BookChapterEntity bookChapter = new BookChapterEntity(-1, title, url, 0, 0, 0, chapterOrder, 1, book);
 	            					if(book.getUpdateOrder() != 0 && autoDownloadInWifi)
 	            						bookChapter.setContent(readChapterContent(bookChapter));
 	            					chapterQueue.add(bookChapter);
 	            				}
 	            				else 
 	            					fetchPageNum = 0; // ϴθµĵط˵걾ҳͿԽѭ
 	            			}
 	            		}
 	            		if(totalPage == 0 && !url.equals("")) {
 	            			ContentValues values = new ContentValues();
 	            			values.put(NovelDB.SUBSCRIPTION_LATEST_TITLE, title);
 	            			values.put(NovelDB.SUBSCRIPTION_LATEST_URL, url);
 	            			db.update(NovelDB.SUBSCRIPTION_TABLE, values, "_id = " + subscription.getId(), null);
 	            			
 	            			ContentValues bkValues = new ContentValues();
 	            			bkValues.put(NovelDB.BOOK_UPDATE_ORDER, chapterOrder);
 	            			db.update(NovelDB.BOOK_LIST_TABLE, bkValues, "_id=" + subscription.getBook().getPriId(), null);
 	            		}
 	        		}
 	        		if(totalPage == 0) {
 	        			curPage = 1;
 	        			totalPage = 1;
 		        		Matcher mPage = pPage.matcher(res);
 		        		if(mPage.find()) {
 		        			curPage = Integer.parseInt(mPage.group(1));
 		        			totalPage = Integer.parseInt(mPage.group(2));
 		        		}
 		        		fetchPageNum = totalPage;
 	        		}
 	        		fetchPageNum --;
         		} while (fetchPageNum > 0);
         		
         		msg = handler.obtainMessage();
         		bundle = new Bundle();
         		bundle.putInt("position", i);
         		bundle.putBoolean("begin", false);
         		msg.setData(bundle);
         		msg.what = START_CODE;
         		handler.sendMessage(msg);
         	}
         	
        	if(needUpdateDb && !chapterQueue.isEmpty()) {
        		Log.i("com.novel.subscription", "subscription needUpdateDatabase");
         		db.beginTransaction();
 		        try {
 		        	while(!chapterQueue.isEmpty()) 
 		        		db.addChapter(chapterQueue.remove());
 		        	db.setTransactionSuccessful();
 		        } finally {
 		        	db.endTransaction();
 		        	db.close();
 		        }
         	}
         	else {
         		db.close();
         	}
 	        		
         	isRunning = false;
         	handler.sendEmptyMessage(FINISHED_CODE);
         }
         
         public String readChapterContent(BookChapterEntity bookChapter) {
         	String domain = bookChapter.getBook().getSrcEntity().getDomain();
         	Pattern pContent = Pattern.compile(bookChapter.getBook().getSrcEntity().getPatternContent());
         	String chapterUrl = bookChapter.getChapterUrl();
         	
         	String res = HtmlService.getHtmlByGet(domain + chapterUrl);
         	
         	int page = 1;
     		Pattern pPage = Pattern.compile("tc_next[\\s\\S]*?href[\\s\\S]*?=[\\s\\S]*?<span[\\s\\S]*?>(\\d+?)</span>/(\\d+?)[^\\d]");
     		Matcher m = pPage.matcher(res);
     		if(m.find()) {
     			try {
     				page = Integer.parseInt(m.group(2));
     			} catch(Exception e) {
     				page = 1;
     			}
     		}
         	
     		int i = 1;
     		StringBuilder sb = new StringBuilder(65536);
     		while(i <= page) {
 	    		m = pContent.matcher(res);
 	    		if(m.find()) {
 	    			String content = m.group(1);
 	    			for(String invalidContent: ConstDefinition.INVALID_CONTENT)
 	    				content = content.replaceAll(invalidContent, "");
 	    			sb.append(content);
 	    		}
 	    		i ++;
 	    		if(i > page)
 	    			break;
 	    		res = HtmlService.getHtmlByGet(domain + chapterUrl + "&pi=" + i);
     		}
         	return sb.toString();
         }
     }
     
 }
