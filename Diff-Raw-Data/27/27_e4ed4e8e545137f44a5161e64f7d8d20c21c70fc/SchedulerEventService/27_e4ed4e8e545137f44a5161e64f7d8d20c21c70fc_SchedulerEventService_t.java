 package com.novel.subscription;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.app.AlarmManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class SchedulerEventService extends Service {
 	private static final String APP_TAG = "com.novel.subscription";
 	private UpdateThread updateThread = null;
 	private boolean isRunning = false;
 	private long lastUpdateTime = 0;
 	
 	@Override
 	public IBinder onBind(final Intent intent) {
 		return null;
 	}
 	
 	@Override
 	public int onStartCommand(final Intent intent, final int flags,
 			final int startId) {
 //		showNotification(Notification.DEFAULT_VIBRATE, "123");
 		Log.d(APP_TAG, "event received in service: " + new Date().toString());
 		android.os.Debug.waitForDebugger();
 		
 		Context ctx = getApplicationContext();
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
 		
 		boolean autoDownloadInWifi = prefs.getBoolean("autoDownloadInWifi", true);
 		autoDownloadInWifi = autoDownloadInWifi && (TestNetworkStatus() == ConnectivityManager.TYPE_WIFI);
 		
 		String intervalString = prefs.getString("updateInterval", "2"); // unit : s ; 2 hours default
 		int interval = 7200;
 		try {
 			interval = Integer.parseInt(intervalString) * 3600;
 		} catch(Exception e) {
 			interval = 7200;
 		}
 		
 //		interval = 5;
 		Log.d(APP_TAG, "Interval:" + interval);
 		
 		Calendar now = Calendar.getInstance();
 		
 		int hour = now.get(Calendar.HOUR_OF_DAY);
 		int min = now.get(Calendar.MINUTE);
 		String hourString = hour + "";
 		if(hour < 10) 
 			hourString = "0" + hour;
 		String minString = min + "";
 		if(min < 10)
 			minString = "0" + min;
 		
 		String nowTime = hourString + ":" + minString;
 		String startTime = prefs.getString("timePickerUpdateStart", "07:00");
 		String endTime = prefs.getString("timePickerUpdateEnd", "23:00");
 		
 		String[] startItems = startTime.split(":");
 		int startHour = Integer.parseInt(startItems[0]);
 		int startMin = Integer.parseInt(startItems[1]);
 		
 		String[] endItems = endTime.split(":");
 		int endHour = Integer.parseInt(endItems[0]);
 		int endMin = Integer.parseInt(endItems[1]);
 		
 		boolean isSleepyTime = !(nowTime.compareTo(startTime) >= 0 && nowTime.compareTo(endTime) <= 0);
 		
 		if(isSleepyTime) {
 			int triggerInterval = secondBetween(hour, min, startHour, startMin) * 1000;
 			now.add(Calendar.SECOND, triggerInterval);
 			
 			AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
 			Intent i = new Intent(ctx, SchedulerEventReceiver.class); // explicit intent
 			PendingIntent intentExecuted = PendingIntent.getBroadcast(ctx, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
 			alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, now.getTimeInMillis(), interval * 1000, intentExecuted);
 		}
 		else {
 			int secondGap = secondBetween(startHour, startMin, hour, min);
 			int end = endHour;
 //			if(startMin > endMin)
 //				end = endHour - 1;
 			
 			int lastHourSecondGap = 0;
 			for(int nextCheckHour = startHour; nextCheckHour <= end; nextCheckHour += interval / 3600) {
 				int nextHourSecondGap = (nextCheckHour - startHour) * 3600;
 				if(secondGap < nextHourSecondGap) {
 					now.add(Calendar.SECOND, nextHourSecondGap - secondGap);
 					AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
 					Intent i = new Intent(ctx, SchedulerEventReceiver.class); // explicit intent
 					PendingIntent intentExecuted = PendingIntent.getBroadcast(ctx, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
 					alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, now.getTimeInMillis(), interval * 1000, intentExecuted);
 					break;
 				}
 				else 
 					lastHourSecondGap = secondGap - nextHourSecondGap;
 			}
 			if(lastUpdateTime == 0)
 				lastUpdateTime = now.getTimeInMillis() - lastHourSecondGap * 1000;
 			
 //					now.add(Calendar.SECOND, interval);
 //					AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
 //					Intent i = new Intent(ctx, SchedulerEventReceiver.class); // explicit intent
 //					PendingIntent intentExecuted = PendingIntent.getBroadcast(ctx, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
 //					alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, now.getTimeInMillis(), interval * 1000, intentExecuted);
 			
 			if(updateThread == null && !isRunning 
 					&& TestNetworkStatus() != -1 
 					&& (now.getTimeInMillis() - lastUpdateTime >= (interval - 300) * 1000) ) {
 				
 				Log.d(APP_TAG, "update begins.");
 				
 				isRunning = true;
 				updateThread = new UpdateThread();
 				updateThread.autoDownloadInWifi = autoDownloadInWifi;
 				updateThread.start();
 				now = Calendar.getInstance();
 				lastUpdateTime = now.getTimeInMillis();
 			}
 		}
 		
 		return Service.START_STICKY;
 	}
 	
 	private int secondBetween(int hour, int min, int targetHour, int targetMin) {
 		int minDistance = -1;
 		int hourAdjust = 0;
 		if(min > targetMin) {
 			minDistance = 60 - min + targetMin;
 			hourAdjust = -1;
 		}
 		else {
 			minDistance = targetMin - min;
 		}
 		System.out.println(minDistance);
 		
 		int hourDistance = -1;
 		if(hour < targetHour){
 			hourDistance = targetHour - hour;
 		}
 		else if(hour > targetHour){
 			hourDistance = 24 - targetHour + hour;
 		}
 		else {
 			if(min < targetMin) {
 				hourDistance = targetHour - hour;
 			}
 			else {
 				hourDistance = 24 - targetHour + hour;
 			}
 		}
 		hourDistance += hourAdjust;
 		System.out.println(hourDistance);
 		
 		return (hourDistance * 60 + minDistance) * 60;
 	}
 	
     Handler handler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
         	Context ctx = getApplicationContext();
     		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
     		
     		boolean needNotify = prefs.getBoolean("updateNotify", true);
     		boolean notifySound = prefs.getBoolean("notifySound", false);
     		boolean notifyVibrate = prefs.getBoolean("notifyVibrate", false);
 
     		int defaultSound = 0, defaultVibrate = 0;
     		if(notifySound)
     			defaultSound = Notification.DEFAULT_SOUND;
     		if(notifyVibrate)
     			defaultVibrate = Notification.DEFAULT_VIBRATE;
     		
         	if(msg.what == 1) {
         		isRunning = false;
         		Bundle bundle = msg.getData();
         		String books = bundle.getString("updateBooks");
         		if(needNotify)
         			showNotification(defaultSound|defaultVibrate, books);
         		updateThread = null;
         	}
         	else if(msg.what == 0) {
         		isRunning = false;
         		updateThread = null;
         	}
         }
     };
 	
 	private int TestNetworkStatus() {
 		  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		  NetworkInfo ni = cm.getActiveNetworkInfo();
 		  if (ni == null)
 			  return -1;
 		  else {
 			  return ni.getType();
 		  }
 	}
     
 	class UpdateThread extends Thread
     {
 //    	public boolean isRunning;
     	public int position;
     	public boolean autoDownloadInWifi;
     	public boolean onlyRecent;
     	
     	private ArrayList<SubscriptionEntity> items = new ArrayList<SubscriptionEntity>();
     	
         @Override
         public void run()
         {
         	readSubscription();
         	
         	Context ctx = getApplicationContext();
 //			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
 			
 //			onlyRecent = prefs.getBoolean("onlyRecent", true);
 			onlyRecent = false;
         	
         	NovelDB db = new NovelDB(ctx);
         	
         	Queue<BookChapterEntity> chapterQueue = new LinkedList<BookChapterEntity>();
         	
         	int curPage = 0, totalPage = 0;
         	int fetchPageNum = -1;
         	int readContentNum = 0;
         	for(int i = 0; i < items.size(); i ++) {
 
         		if(position != -1 && i != position)
         			continue;
         		
         		SubscriptionEntity subscription = items.get(i);
         		BookEntity book = subscription.getBook();
         		
         		fetchPageNum = -1;
         		curPage = 0;
         		totalPage = 0;
         		readContentNum = 0;
         		
         		Pattern pChapter = Pattern.compile(book.getSrcEntity().getPatternChapter());
             	Pattern pBody = Pattern.compile(book.getSrcEntity().getPatternBody());
             	Pattern pOrder = Pattern.compile(book.getSrcEntity().getPatternOrder());
             	Pattern pPage = Pattern.compile(book.getSrcEntity().getPatternPage());
             	Pattern pInvalidTitle = Pattern.compile("<.*>.*</.*>");
             	
         		//һҳʼң¿ʼ
         		do{
 	        		String res = HtmlService.getHtmlByGet(book.getSrcEntity().getDomain() + book.getUrl() + "&pi=" + fetchPageNum);
 	            	
 	        		Matcher mBody = pBody.matcher(res);
 	    			String url = "";
 	    			String title = "";
 	    			int chapterOrder = 0;
 	    			String lastUrl, lastTitle;
 	        		if(mBody.find()) {
 	        			String body = mBody.group(1);
 	            		Matcher m = pChapter.matcher(body);
 	            		while(m.find()){
 	            			lastUrl = url;
 	            			lastTitle = title;
 	            			
 	            			url = m.group(1).replaceAll("&amp;", "&");
 	            			title = m.group(2);
 	            			if(pInvalidTitle.matcher(title).find())
 	            				continue;
 	            			Matcher mUrlOrder = pOrder.matcher(url);
 	            			if(mUrlOrder.find()) {
 	            				chapterOrder = 0;
	            				String ordStr = mUrlOrder.group(1);
	            				if(ordStr == null)
	            					ordStr = mUrlOrder.group(2);
 	            				try {
	            					chapterOrder = Integer.parseInt(ordStr);
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
 		        		if(onlyRecent)
 		        			fetchPageNum = 0;
 	        		}
 	        		fetchPageNum --;
         		} while (fetchPageNum > 0);
         		
         	}
         	
         	if(!chapterQueue.isEmpty()) {
         		HashSet<String> updateSet = new HashSet<String>();
         		db.beginTransaction();
 		        try {
 		        	while(!chapterQueue.isEmpty()) {
 		        		BookChapterEntity bookChapter = chapterQueue.remove();
 		        		updateSet.add(bookChapter.getBook().getName());
 		        		db.addChapter(bookChapter);
 		        	}
 		        	db.setTransactionSuccessful();
 		        	String[] updateBooks = (String[])updateSet.toArray(new String[0]);
 		        	String bundleUpdateBooks = " ";
 		        	for(int i = 0; i < updateBooks.length; i ++) {
 		        		bundleUpdateBooks += updateBooks[i];
 		        		if(i < updateBooks.length - 1)
 		        			bundleUpdateBooks += ",";
 		        	}
 		        	Message msg = handler.obtainMessage();
 		        	Bundle bundle = new Bundle();
 		        	bundle.putString("updateBooks", bundleUpdateBooks + " ");
 		        	msg.what = 1;
 		        	msg.setData(bundle);
 		        	handler.sendMessage(msg);
 		        } finally {
 		        	db.endTransaction();
 		        	db.close();
 		        }
         	}
         	else {
         		db.close();
         		handler.sendEmptyMessage(0);
         	}
         }
         
         private void readSubscription() {
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
 	
 	  public void showNotification(int defaults, String info)
 	  {
 		NotificationManager _nm = ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE));
 	    Notification notification = new Notification(R.drawable.ic_launcher, "ĵС˵и", System.currentTimeMillis());
 	    notification.defaults = defaults;
 
 	    notification.flags |= Notification.FLAG_AUTO_CANCEL;
 	    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
 	    notification.setLatestEventInfo(this, "ĸ", "ĵС˵" + info + "иˣ鿴", contentIntent);
 	    _nm.notify(2313, notification);
 	  }
 }
