 
 package com.smilo.bullpen;
 
 import net.htmlparser.jericho.CharacterReference;
 import net.htmlparser.jericho.Element;
 import net.htmlparser.jericho.EndTag;
 import net.htmlparser.jericho.HTMLElementName;
 import net.htmlparser.jericho.Segment;
 import net.htmlparser.jericho.Source;
 import net.htmlparser.jericho.StartTag;
 import net.htmlparser.jericho.Tag;
 
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
 import java.util.Iterator;
 import java.util.List;
 
 public class BullpenListViewFactory implements RemoteViewsService.RemoteViewsFactory {
 
     private static final String TAG = "BullpenListViewFactory";
 
     private List<listItem> mlistItems = new ArrayList<listItem>();
     private Context mContext;
     private int mAppWidgetId;
     private ConnectivityManager mConnectivityManager;
 
     private static boolean mIsSkipFirstCallOfGetViewAt = true;
     
     private class listItem {
         public String itemTitle;
         public String itemUrl;
 
         listItem(String title, String url) {
             itemTitle = title;
             itemUrl = url;
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
         
         if (mIsSkipFirstCallOfGetViewAt) {
             mIsSkipFirstCallOfGetViewAt = false;
             return null;
         }
 
         // Create a RemoteView and set widget item array list to the RemoteView.
         RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.list_row);
         rv.setTextViewText(R.id.listRowText, mlistItems.get(position).itemTitle);
 
         Intent fillInIntent = new Intent();
         fillInIntent.putExtra(Constants.EXTRA_ITEM_URL, mlistItems.get(position).itemUrl);
         rv.setOnClickFillInIntent(R.id.listRowText, fillInIntent);
 
         return rv;
     }
 
     @Override
     public void onDataSetChanged() {
         Log.i(TAG, "onDataSetChanged");
 
         if (Utils.checkInternetConnectivity(mConnectivityManager)) {
             // Parse MLBPark html data and add items to the widget item array list.
             try {
                 parseMLBParkHtmlDataMobileVer(Constants.mMLBParkUrl_mlbtown);
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
 
     private void parseMLBParkHtmlDataFullVer(String urlAddress) throws IOException {
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
                 //Log.i(TAG, "parseMLBParkHtmlDataFullVer - title[" + title + "],url[" + url + "]");
 
                 // Add widget item array list
                 listItem item = new listItem(title, url);
                 mlistItems.add(item);
                 addedItemCount++;
 
                 if (addedItemCount == Constants.LISTVIEW_MAX_ITEM_COUNT) {
                     Log.i(TAG, "parseMLBParkHtmlDataFullVer - done!");
                     return;
                 }
             }
         }
     }
 
     private void parseMLBParkHtmlDataMobileVer(String urlAddress) throws IOException {
         Source source = new Source(new URL(urlAddress));
         source.fullSequentialParse();
 
         // Initialize widget item array list here.
         mlistItems.clear();
 
         // Find the same pattern with <ul id="mNewsList">. This means the body of this article.
        Element ul = source.getFirstElement(HTMLElementName.UL);
        String attrValue = ul.getAttributeValue("id");
        if (attrValue != null && attrValue.equals("mNewsList")) {
            List<Element> lis = ul.getAllElements(HTMLElementName.LI);
             int addedItemCount = 0;
             
             for (int i = 0 ; i < lis.size() ; i++ ) {
                 Segment seg = lis.get(i).getContent();
                 String title = null, url = null;
                 boolean isAddTitle = false, isAddCommentNum = false;
                 //Log.i(TAG, "seg[" + seg.toString() + "]");
 
                 // Parse title
                 for (Iterator<Segment> nodeIterator = seg.getNodeIterator() ; nodeIterator.hasNext();) {
                     Segment nodeSeg = nodeIterator.next();
                     
                     // If <a href> tag, add address to url.
                     // if <strong> tag, prepare to add title.
                     // If <span class="r"> tag, prepare to add comment number.
                     if (nodeSeg instanceof StartTag) {
                         String tagName = ((Tag)nodeSeg).getName();
                         if (tagName.equals("a")) {
                            url = ((StartTag) nodeSeg).getAttributeValue("href");
                            if (url.startsWith("/")) {
                                StringBuffer strBuf = new StringBuffer();
                                strBuf.append(Constants.mMLBParkUrl_base);
                                strBuf.append(url);
                                url = strBuf.toString();
                            }
                         } else if (tagName.equals("strong")) {
                             isAddTitle = true;
                         } else if (tagName.equals("span")) {
                             String spanClass = ((StartTag) nodeSeg).getAttributeValue("class");
                             if (spanClass != null && spanClass.equals("r")) {
                                 isAddCommentNum = true;
                             }
                         }
                         continue;
                         
                     } else if (nodeSeg instanceof EndTag) {
                         // Do nothing
                         continue;
                         
                      // Ignore &bnsp;
                     } else if (nodeSeg instanceof CharacterReference) {
                         continue;
                         
                     // If plain text, add it to title.
                     } else {
                         if (isAddTitle) {
                             title = nodeSeg.getTextExtractor().toString();
                             isAddTitle = false;
                         } else if (isAddCommentNum) {
                             title += (" [" + nodeSeg.getTextExtractor().toString() + "]");
                             isAddCommentNum = false;
                         }
                     }
                 }
                Log.i(TAG, "parseMLBParkHtmlDataMobileVer - title[" + title + "],url[" + url + "]");
                 
                 // Add widget item array list
                 listItem item = new listItem(title, url);
                 mlistItems.add(item);
                 addedItemCount++;
 
                 if (addedItemCount == Constants.LISTVIEW_MAX_ITEM_COUNT) {
                     Log.i(TAG, "parseMLBParkHtmlDataMobileVer - done!");
                     return;
                 }
             }
         } else {
             Log.e(TAG, "parseMLBParkHtmlDataMobileVer - Cannot find article list.");
         }
     }
 }
