 package com.mezcode.wikiwidgets.widgets;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import android.content.Context;
 import android.content.Intent;
import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.RemoteViews;
 import android.widget.RemoteViewsService;
 
 import com.mezcode.wikiwidgets.NetworkHelper;
 import com.mezcode.wikiwidgets.PicItem;
 import com.mezcode.wikiwidgets.R;
 
 public class AtomFeedViewFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = "AtomWidgetViewFactory";

     private PicItem[] mFeedWidgetItems;
     private int mWidgetType;
     private static String mPkg;
    private static Bitmap mIcon;
 
    /* public AtomWidgetViewFactory(Context context, Intent intent) {
         //mContext = context.getApplicationContext();   
         mPkg = context.getPackageName();
         Log.d(TAG, "package string set " + mPkg);
     }*/
     
     public AtomFeedViewFactory(Context context, Intent intent, int widget_id) {
     	mPkg = context.getPackageName();
     	mWidgetType = widget_id;
     }
 
     public void onCreate() {
         // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
         // for example downloading or creating content etc, should be deferred to onDataSetChanged()
         // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
     	Log.d(TAG, "atom onCreate");    	
     }
     
         
     void createList( ) {
     	//use an RSS feed to build the widget collection 
     	URL webpage = null;
     	try {
 			switch(mWidgetType) {
 			case R.xml.feature_stack_info:
 			case R.xml.feature_list_info:
 				webpage = new URL(NetworkHelper.FEATURED_FEED);
 				break;
 			case R.xml.pic_stack_info:
 			case R.xml.pic_list_info:
 				Log.d(TAG, "fetch photo stream");
 				webpage = new URL(NetworkHelper.POTD_STREAM);
 			}
 			mFeedWidgetItems = NetworkHelper.fetchRssFeed(webpage);
 		} catch (MalformedURLException e) {
 			Log.e(TAG, "createList exception " + e.getMessage());
 			e.printStackTrace();
 		}
     }
     
 
     public void onDestroy() {
         // In onDestroy() you should tear down anything that was setup for your data source,
         // eg. cursors, connections, etc.
         mFeedWidgetItems = null;
     }
 
     public int getCount() {
     	//Log.d(TAG, "getCount");
     	if(mFeedWidgetItems != null) {
     		return mFeedWidgetItems.length;
     	} else return 0;
     }
         
     public static int fetchId(int position, int viewID) {
     	//vertical stack of three items or photo on left with text on right?
 		switch(viewID) {
 		case R.xml.geo_list_info:
 		case R.xml.feature_list_info:
 		case R.xml.feature_stack_info:
 		case R.xml.geo_stack_info:	
 			return (position % 2 == 0 ? R.layout.feature_item
                     : R.layout.feature_item2);
 			
 		case R.xml.pic_stack_info:
 			return (position % 2 == 0 ? R.layout.pic_widget_item
 	                : R.layout.pic_widget_item2);
 		case R.xml.pic_list_info:
 			return (position % 2 == 0 ? R.layout.photo_item
 	                : R.layout.photo_item2);
     	}
 		return (position % 2 == 0 ? R.layout.photo_item
                 : R.layout.photo_item2);
 
     }
 
     public RemoteViews getViewAt(int position) {
         // position will always range from 0 to getCount() - 1.
     	
         // Construct a remote views item based on our widget item xml file 
         // set the title and summary text based on the position.
     	final int itemId = fetchId(position, mWidgetType);
         final RemoteViews rv = new RemoteViews(mPkg, itemId);
         final PicItem displayItem = mFeedWidgetItems[position];
         rv.setTextViewText(R.id.text1, displayItem.getTitle());
         rv.setTextViewText(R.id.text2, displayItem.getSummary());
         /*************/
         
         if(displayItem.getPhoto() != null) {
        	//Log.d(TAG, "set Image, actual photo " + position);
         	rv.setImageViewBitmap(R.id.list_image, displayItem.getPhoto());
         } else {
        	//Log.d(TAG, "set Image, no photo " + position);
         	rv.setImageViewResource(R.id.list_image, R.drawable.icon);
         }
         /*************/
         // Next, we set a fill-intent which to fill-in the pending intent template
         // set on the collection view in WikiWidgetProvider.
         final Bundle extras = new Bundle();
         extras.putString(BaseStackProvider.URL_TAG, displayItem.getWikipediaUrl());
         //Log.d(TAG, "set url as extra " + mListWidgetItems.get(position).wikipediaUrl);
         final Intent fillInIntent = new Intent(); 
         fillInIntent.putExtras(extras);
         //fillInIntent.putExtra(WikiWidgetProvider.URL_TAG, mWidgetItems.get(position).url);
         //Setting the extra on the intent rather than in a bundle did not seem to work
         //        fillInIntent.setData(Uri.parse(mListWidgetItems.get(position).wikipediaUrl));
         //not able to use the "set data" call and go directly to an action view, popping toast back in the provider instead
         rv.setOnClickFillInIntent(R.id.text1, fillInIntent);
         rv.setOnClickFillInIntent(R.id.list_image, fillInIntent);
 
         return rv;
     }
 
     public RemoteViews getLoadingView() {
         // You can create a custom loading view (for instance when getViewAt() is slow.) If you
         // return null here, you will get the default loading view.
         return null;
     }
 
     public int getViewTypeCount() {
     	//the list switches between 2 xml layouts with different backgrounds
         return 2;
     }
 
     public long getItemId(int position) {
         return position;
     }
 
     public boolean hasStableIds() {
         return true;
     }
 
     public void onDataSetChanged() {
         // For use with a content provider?  Run after 
     	//Log.d(TAG, "onDataSetChanged");
     	createList();
     }
 }
