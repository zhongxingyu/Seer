 
 package com.smilo.bullpen;
 
 import net.htmlparser.jericho.Element;
 import net.htmlparser.jericho.HTMLElementName;
 import net.htmlparser.jericho.Segment;
 import net.htmlparser.jericho.Source;
 
 import android.appwidget.AppWidgetManager;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.util.Log;
 import android.widget.RemoteViews;
 import android.widget.RemoteViewsService;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 public class BullpenListViewFactory implements RemoteViewsService.RemoteViewsFactory {
 
     private static final String TAG = "BullpenListViewFactory";
 
     private List<listItem> mlistItems = new ArrayList<listItem>();
     private Context mContext;
     private int mAppWidgetId;
     private ConnectivityManager mConnectivityManager;
     
     private class listItem {
         String itemTitle;
         String itemUrl;
 
         listItem(String title, String url) {
             itemTitle = title;
             itemUrl = url;
         }
 
         public String getTitle() {
             return itemTitle;
         }
 
         public String getUrl() {
             return itemUrl;
         }
     }
 
     public BullpenListViewFactory(Context context, Intent intent) {
         Log.i(TAG, "constructor");
 
         mAppWidgetId = intent.getIntExtra(
                 AppWidgetManager.EXTRA_APPWIDGET_ID,
                 AppWidgetManager.INVALID_APPWIDGET_ID);
         mContext = context;
         
         mConnectivityManager =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
     }
 
     @Override
     public RemoteViews getViewAt(int position) {
         //Log.i(TAG, "getViewAt - position[" + position + "]");
 
         // Create a RemoteView and set widget item array list to the RemoteView.
         RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.list_row);
         rv.setTextViewText(R.id.listRowText, mlistItems.get(position).getTitle());
 
         Intent fillInIntent = new Intent();
         fillInIntent.putExtra(Constants.EXTRA_ITEM_URL, mlistItems.get(position).getUrl());
         rv.setOnClickFillInIntent(R.id.listRowText, fillInIntent);
 
         return rv;
     }
 
     @Override
     public void onDataSetChanged() {
         Log.i(TAG, "onDataSetChanged");
 
         if (Utils.checkInternetConnectivity(mConnectivityManager)) {
             // Parse MLBPark html data and add items to the widget item array list.
             try {
                 parseMLBParkHtmlData(Constants.mMLBParkUrl_mlbtown);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         } else {
             Log.e(TAG, "onDataSetChanged - Internet net connected!");
         }
     }
 
     @Override
     public int getCount() {
         return Constants.LISTVIEW_MAX_ITEM_COUNT;
     }
 
     @Override
     public long getItemId(int position) {
         return position;
     }
 
     @Override
     public RemoteViews getLoadingView() {
         return null;
     }
 
     @Override
     public int getViewTypeCount() {
         return 1;
     }
 
     @Override
     public boolean hasStableIds() {
         return true;
     }
 
     @Override
     public void onCreate() {
         // no-op
     }
     
     @Override
     public void onDestroy() {
         // no-op
     }
 
     private void parseMLBParkHtmlData(String urlAddress) throws IOException {
         Source source = new Source(new URL(urlAddress));
         source.fullSequentialParse();
 
         // Initialize widget item array list here.
         mlistItems.clear();
 
         List<Element> tables = source.getAllElements(HTMLElementName.TABLE);
         int addedItemCount = 0;
         for (int i = 0; i < tables.size(); i++) {
             Element table = tables.get(i);
 
             // Find the same pattern with <table width='100%' border='0' cellspacing='6' cellpadding='0'
            String widthAttr = table.getAttributeValue("width");
            String borderAttr = table.getAttributeValue("border");
            String cellspacingAttr = table.getAttributeValue("cellspacing");
            String cellpaddingAttr = table.getAttributeValue("cellpadding");
            if ((widthAttr != null && widthAttr.equals("100%"))
                    && (borderAttr != null && borderAttr.equals("0"))
                    && (cellspacingAttr != null && cellspacingAttr.equals("6"))
                    && (cellpaddingAttr != null && cellpaddingAttr.equals("0"))) {
 
                 Segment content;
                 
                 // Skip Notice
                 content= table.getFirstElementByClass("A11gray").getContent();
                 if (content.getTextExtractor().toString().equals("공지"))
                     continue;
                 
                 content = table.getFirstElementByClass("G12read").getContent();
 
                 // Get title and url
                 String title = content.getTextExtractor().toString();
                 String url = content.getURIAttributes().get(0).getValue();
                 if (url.startsWith("/")) {
                     StringBuffer strBuf = new StringBuffer();
                     strBuf.append(Constants.mMLBParkUrl_base);
                     strBuf.append(url);
                     url = strBuf.toString();
                 }
                 Log.i(TAG, "parseMLBParkHtmlData - title[" + title + "],url["
                         + url + "]");
 
                 // Add widget item array list
                 listItem item = new listItem(title, url);
                 mlistItems.add(item);
                 addedItemCount++;
 
                 if (addedItemCount == Constants.LISTVIEW_MAX_ITEM_COUNT) {
                     return;
                 }
             }
         }
     }
 }
