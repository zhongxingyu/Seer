 package org.openintents.news.services;
 /* 
  * Copyright (C) 2007-2008 OpenIntents.org
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import android.app.NotificationManager;
 import android.app.Notification;
 import android.app.Service;
 import android.os.IBinder;
 import android.os.Parcel;
 import android.util.Log;
 import android.content.ContentUris;
 import android.database.Cursor;
 import android.widget.Toast;
 
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 
 import java.util.HashMap;
 
 import org.openintents.provider.News;
 import org.openintents.news.*;
 
 
 			 
 public class NewsreaderService1 extends Service implements Runnable{
 
 	private boolean alive=false;
 	
 	public static final String PREFS_NAME="newsdaemon_prefs";
 	public static final String DO_ROAMING="doRoaming";
 	public static final String ON_BOOT_START="onBootStart";
 	public static final String DEBUG_MODE="debugMode";
 
 //	private NotificationManager mNM;
 	
 	private static final String _TAG="NewsReaderService";
 
 	private static long now;
 	
 	private Cursor mRSSCursor;
 	private Cursor mATMCursor;
 
 	private boolean useWhileRoaming=false;
 	private boolean isRoaming=false;
 	private boolean debugMode=false;
 	
 	private boolean startOnSystemBoot=false;
 
 	private static final String[] RSS_PROJECTION= new String[] {
 		News.RSSFeeds._ID,
 		News.RSSFeeds._COUNT,
 		News.RSSFeeds.CHANNEL_NAME,
 		News.RSSFeeds.CHANNEL_LINK,
 		News.RSSFeeds.UPDATE_CYCLE,
 		News.RSSFeeds.LAST_UPDATE		
 	};
 	
 	private static final String[] ATOM_PROJECTION=new String[]{
 		News.AtomFeeds._ID,
 		News.AtomFeeds._COUNT,
 		News.AtomFeeds.FEED_TITLE,
 		News.AtomFeeds.FEED_LINK_SELF,
 		News.AtomFeeds.UPDATE_CYCLE,
 		News.AtomFeeds.FEED_LAST_CHECKED
 
 	};
 
 
     protected void onCreate() {
     	//Log.v("x","oncreate");     
         
     	//init NotifyManager.
   //  	mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 
         // Start up the thread running the service.  Note that we create a
         // separate thread because the service normally runs in the process's
         // main thread, which we don't want to block.
 		Toast.makeText(this, "NewsReaderService started", Toast.LENGTH_SHORT).show();
 
         News.mContentResolver=getContentResolver();
 
 		SharedPreferences settings = getSharedPreferences(NewsreaderService1.PREFS_NAME, 0);
 		this.useWhileRoaming		=settings.getBoolean(NewsreaderService1.DO_ROAMING,false);
 		this.startOnSystemBoot		=settings.getBoolean(NewsreaderService1.ON_BOOT_START,false);
 		this.debugMode				=settings.getBoolean(NewsreaderService1.DEBUG_MODE,false);
 		
 		Log.d(_TAG,"startup: \nuseWhileRoaming>>"+useWhileRoaming+"<<\n startOnSystemBoot>>+"+startOnSystemBoot+"<< \n debugMode>>"+debugMode+"<<");
 
         this.alive=true;
         Thread thr = new Thread(null, this, "NewsReaderService1");
         thr.start();
         
     }
 	
 	
 	public IBinder onBind(android.content.Intent i){
 		return null;
 	}
 
 	@Override
 	public void onDestroy(){
		Toast.makeText(this, "NewsReaderService stopping..", Toast.LENGTH_SHORT).show();
 		this.alive=false;
 	//	mNM.notifyWithText(1, "thread stopping", NotificationManager.LENGTH_SHORT,null);
 	}
 	
 
 	
 	public void run() {		
 
 			mRSSCursor=News.mContentResolver.query(News.RSSFeeds.CONTENT_URI,RSS_PROJECTION,null,null,null);			
 			mATMCursor=News.mContentResolver.query(News.AtomFeeds.CONTENT_URI,ATOM_PROJECTION,null,null,null);
 			
 
 
 		while (this.alive){
 			
 			try{
 				Thread.sleep(60*1000);
 				//wake up cursor, see if data changed.
 				mRSSCursor.requery();
 				mRSSCursor.first();
 
 				mATMCursor.requery();
 				mATMCursor.first();
 
 				int rssLen=mRSSCursor.count();
 				int atmLen=mATMCursor.count();
 
 				int rssCLinkCol		=mRSSCursor.getColumnIndex(News.RSSFeeds.CHANNEL_LINK);
 				int rssIDCol		=mRSSCursor.getColumnIndex(News.RSSFeeds._ID);
 				int rssUpdCyCol		=mRSSCursor.getColumnIndex(News.RSSFeeds.UPDATE_CYCLE);
 				int rssLastUpdCol	=mRSSCursor.getColumnIndex(News.RSSFeeds.LAST_UPDATE);
 
 				int atmCLinkCol		=mATMCursor.getColumnIndex(News.AtomFeeds.FEED_LINK_SELF);
 				int atmIDCol		=mATMCursor.getColumnIndex(News.AtomFeeds._ID);
 				int atmUpdCyCol		=mATMCursor.getColumnIndex(News.AtomFeeds.UPDATE_CYCLE);
 				int atmLastUpdCol	=mATMCursor.getColumnIndex(News.AtomFeeds.FEED_LAST_CHECKED);
 
 				now=System.currentTimeMillis();
 				if (debugMode)
 				{
 					Log.d(_TAG,"# RSS Feeds>>"+rssLen+"<< , time is >>"+now+"<<");
 				}
 				
 
 				for (int i1=0;i1<rssLen ;i1++ )
 				{
 					if (debugMode)
 					{
 						Log.d(_TAG,"# i1>>"+i1+"<<");
 					}
 				
 					
 					long lastUpdate=mRSSCursor.getLong(rssLastUpdCol);
 					long updateCycle=mRSSCursor.getLong(rssUpdCyCol);
 				
 					//convert update cycle to milliseconds
 					updateCycle=updateCycle * 60 * 1000;
 					if (lastUpdate+updateCycle<now)
 					{					
 						HashMap <String,String>data=new HashMap();
 						data.put(News.RSSFeeds._ID,mRSSCursor.getString(rssIDCol));
 						data.put(News.RSSFeeds.CHANNEL_LINK,mRSSCursor.getString(rssCLinkCol));
 						RSSFetcherThread rt=new RSSFetcherThread(data);
 						if (debugMode){	Log.d(_TAG,"# >>"+i1+"<< will start thread now");}
 						rt.start();
 						if (debugMode){Log.d(_TAG,"# >>"+i1+"<< called start.");}
 					}
 					mRSSCursor.next();
 
 
 
 				}
 
 				now=System.currentTimeMillis();
 				if (debugMode){Log.d(_TAG,"# ATOM Feeds>>"+atmLen+"<< , time is >>"+now+"<<");}
 				for (int i2=0;i2<atmLen ;i2++ )
 				{
 
 					if (debugMode){
 						Log.d(_TAG,"# i2>>"+i2+"<<");
 						Log.d(_TAG,"# i2>>"+i2+"<< [xx]supposed LastUpdColIndex"+atmLastUpdCol);
 					
 						String[] cols=mATMCursor.getColumnNames();
 						StringBuffer b=new StringBuffer();
 						for (int x=0;x<cols.length ;x++ )
 						{
 							b.append("["+x+"]::"+cols[x]+"\n");
 						}
 						Log.d(_TAG,"# i2>>"+i2+"<< [xxx] cursor cols----\n"+b.toString()+"\n-----");
 					}					
 					
 					long lastUpdate=mATMCursor.getLong(atmLastUpdCol);
 					long updateCycle=mATMCursor.getLong(atmUpdCyCol);
 					if (debugMode){Log.d(_TAG,"# i2>>"+i2+"<<, last update>>"+lastUpdate);}
 			
 					//convert update cycle to milliseconds
 					updateCycle=updateCycle * 60 * 1000;
 					if (lastUpdate+updateCycle<now)
 					{					
 						HashMap <String,String>data=new HashMap();
 						data.put(News.AtomFeeds._ID,mATMCursor.getString(atmIDCol));
 						data.put(News.AtomFeeds.FEED_LINK_SELF,mATMCursor.getString(atmCLinkCol));
 						data.put(AtomFetcherThread.DEBUG_MODE,Boolean.toString(debugMode));
 						AtomFetcherThread rt=new AtomFetcherThread(data);
 						if (debugMode){Log.d(_TAG,"#atm >>"+i2+"<< will start thread now");}
 						rt.start();
 						if (debugMode){Log.d(_TAG,"#atm >>"+i2+"<< called start.");}
 					}
 					mATMCursor.next();
 
 				}
 
 
 				//next thing to do is sleep, so keep resources low.
 				mRSSCursor.deactivate();
 				mATMCursor.deactivate();
 				if (debugMode){Log.d(_TAG,"will sleep now");}
 			}catch(Exception e){   
 				
 				Log.e(_TAG,"Error:"+e.getMessage());
 				e.printStackTrace();
 			}
 			
 		}
 		//finished/stopp called. cleanup & exit
 		this.cleanup();
 	}
 
 	
 	private void cleanup(){
 		Log.d(_TAG,"Cleaning up...");
 	//	mNM.notifyWithText(1, "cleaning up...", NotificationManager.LENGTH_SHORT,null);
 		mRSSCursor.close();
 		mATMCursor.close();
 		
 	}
 	
 	public static void Test(){
 		Log.d(_TAG,"TEST CALL");
 	}
 	
 	
 	
 }/*eoc*/
