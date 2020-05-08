 package com.mezcode.wikiwidgets.widgets;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.RemoteViews;
 import android.widget.RemoteViewsService;
 
 import com.mezcode.wikiwidgets.NetworkHelper;
 import com.mezcode.wikiwidgets.PicItem;
 import com.mezcode.wikiwidgets.R;
 
 public class GeoViewFactory implements RemoteViewsService.RemoteViewsFactory {
     private PicItem[] mListWidgetItems;
     private int mListType; //provider info xml file
 
     private static Context mListContext;
     private static final String TAG = "GeoViewFactory";
     private static String pkg;
 
     public GeoViewFactory(Context context, Intent intent, int widgetId) {
         mListContext = context.getApplicationContext();        
         pkg = context.getPackageName();
         mListType = widgetId;
     }
 
     public void onCreate() {
         // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
         // for example downloading or creating content etc, should be deferred to onDataSetChanged()
         // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
     	
     }    
     
     void createGeoList( ) {
     	mListWidgetItems = NetworkHelper.geoDataFetch(mListContext);
     	//Log.d(TAG, "end createList, size is " + mListWidgetItems.length);
     }
     
     public void onDestroy() {
         // In onDestroy() you should tear down anything that was setup for your data source,
         // eg. cursors, connections, etc.
         mListWidgetItems = null;
     }
 
     public int getCount() {
     	//Log.d(TAG, "getCount");
     	if(mListWidgetItems != null) {
     		return mListWidgetItems.length;
     	} else return 0;
     }
 
     public RemoteViews getViewAt(int position) {
         // position will always range from 0 to getCount() - 1.
         // Construct a remote views item based on our widget item xml file 
         // set the title and summary text based on the position.
     	/*the widget info xml identifier in mListType will determine which view to instantiate when building this list*/
     	final int itemId = AtomFeedViewFactory.fetchId(position, mListType);
         final RemoteViews rv = new RemoteViews(pkg, itemId);
         final PicItem thisView = mListWidgetItems[position];
         rv.setTextViewText(R.id.text1, thisView.getTitle());
         //Log.d(TAG, "rv title is " + thisView.title);
         rv.setTextViewText(R.id.text2, thisView.getSummary());
         if(thisView.getPhoto() != null) {
         	rv.setImageViewBitmap(R.id.list_image, thisView.getPhoto());
        } else {
        	rv.setImageViewResource(R.id.list_image, R.drawable.icon);
         }
         
         /*rv.setTextViewText(R.id.widget_url, mWidgetItems.get(position).url);
          * TODO Consider an active link in a Text view as part of the layout 
          * This could replace the pending intent or provide the functionality with 2.x releases
          * */
         // Next, we set a fill-intent which to fill-in the pending intent template
         // set on the collection view in WikiWidgetProvider.
         final Bundle extras = new Bundle();
         extras.putString(BaseStackProvider.URL_TAG, thisView.getWikipediaUrl());
         //Log.d(TAG, "set url as extra " + thisView.wikipediaUrl);
         final Intent fillInIntent = new Intent(); 
         fillInIntent.putExtras(extras);
         //fillInIntent.putExtra(WikiWidgetProvider.URL_TAG, mWidgetItems.get(position).url);
         //Setting the extra on the intent rather than in a bundle did not seem to work
         rv.setOnClickFillInIntent(R.id.text1, fillInIntent);
         rv.setOnClickFillInIntent(R.id.list_image, fillInIntent);
         // You can do heaving lifting in here, synchronously. For example, if you need to
         // process an image, fetch something from the network, etc., it is ok to do it here,
         // synchronously. A loading view will show up in lieu of the actual contents in the
         // interim.
         return rv;
     }
 
     public RemoteViews getLoadingView() {
         // You can create a custom loading view (for instance when getViewAt() is slow.) If you
         // return null here, you will get the default loading view.
         return null;
     }
 
     public int getViewTypeCount() {
     	Log.d(TAG, "getTypeCount");
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
     	createGeoList();
     }
 }
