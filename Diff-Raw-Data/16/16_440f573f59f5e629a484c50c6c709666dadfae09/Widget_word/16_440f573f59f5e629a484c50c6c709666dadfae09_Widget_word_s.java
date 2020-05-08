 package com.hbsoft.noticeit;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Timer;  
 import java.util.TimerTask;  
 import android.appwidget.AppWidgetManager;  
 import android.appwidget.AppWidgetProvider;  
 import android.content.ComponentName;  
 import android.content.Context;  
 import android.content.Intent;
 import android.util.Log;
 import android.widget.RemoteViews; 
 
 public class Widget_word extends AppWidgetProvider {
 	private static final String TAG = "Widget_word";
 	public static ArrayList<String> mNoticeWords=new ArrayList<String>();
 	static int mIndex=0;
 	static Timer mTimer=null;
 	static UpdateTimer mUpdateTimer=null;
 	
 	public void onReceive (Context context, Intent intent){
 		Log.d(TAG,"onReceive");
 		super.onReceive(context, intent);
 	}
 	
 	public static void loadWords(Context context){
 		if(mNoticeWords.size()>0)
 			return;
         try {
 			InputStream is = context.getAssets().open("poems.txt");
 			Log.d(TAG, "is" + is == null ? "null" : "success");
 			InputStreamReader reader = new InputStreamReader(is, "GBK");
 			BufferedReader br = new BufferedReader(reader);
 			for (;;) {
 				String line = br.readLine();
 				if (line == null)
 					break;
 				Log.d(TAG, "read:" + line);
 				mNoticeWords.add(line);
 			}
 			br.close();
 			reader.close();
 			is.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	  
     @Override  
     public void onUpdate(Context context, AppWidgetManager appWidgetManager,  
             int[] appWidgetIds) {  
         Log.d(TAG,"onUpdate");
         synchronized(mNoticeWords){
        	if(mTimer==null){
            	loadWords(context);
            	mTimer=new Timer();
        		mUpdateTimer=new UpdateTimer(context,appWidgetManager);
        		mTimer.scheduleAtFixedRate(mUpdateTimer, 1, 10000);
        	   }
        }
 
         super.onUpdate(context, appWidgetManager, appWidgetIds);  
     }  
 
     private class UpdateTimer extends TimerTask{  
         RemoteViews remoteViews;  
         AppWidgetManager appWidgetManager;  
         ComponentName thisWidget;  
           
         public UpdateTimer(Context context,AppWidgetManager appWidgetManager){  
             this.appWidgetManager = appWidgetManager;  
             remoteViews = new RemoteViews(context.getPackageName(),R.layout.main);  
               
             thisWidget = new ComponentName(context,Widget_word.class);  
         }  
         public void run() {
         	if(mIndex<mNoticeWords.size()){
             remoteViews.setTextViewText(R.id.notice_word, mNoticeWords.get(mIndex));  
             mIndex++;
             if(mIndex>=mNoticeWords.size())
             	mIndex=0;
         	}
             appWidgetManager.updateAppWidget(thisWidget, remoteViews);  
               
         }  
           
     }  
 }
