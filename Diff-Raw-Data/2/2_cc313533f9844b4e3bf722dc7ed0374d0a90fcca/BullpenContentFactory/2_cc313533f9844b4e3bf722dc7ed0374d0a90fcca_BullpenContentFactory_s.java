 
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
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 import android.widget.RemoteViews;
 import android.widget.RemoteViewsService;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Iterator;
 import java.util.List;
 
 public class BullpenContentFactory implements RemoteViewsService.RemoteViewsFactory {
 
     private static final String TAG = "BullpenContentFactory";
 
    private contentItem mContentItem = null;;
     private Context mContext;
     private int mAppWidgetId;
     private String mSelectedItemUrl = null;
     private BroadcastReceiver mIntentListener;
     private Bitmap mBitmap = null;
 
     private static boolean mIsSkipFirstCallOfGetViewAt = true;
     private static boolean mIsUpdateRemoteView = false;
 
     //private ConnectivityManager mConnectivityManager;
     
     private class contentItem {
         String itemBody = null;
         String itemImgUrl = null;
         String itemComment = null;
 
         contentItem(String body, String imgUrl, String comment) {
             itemBody = body;
             itemImgUrl = imgUrl;
             itemComment = comment;
         }
 
         public String getBody() {
             return itemBody;
         }
 
         public String getImgUrl() {
             return itemImgUrl;    
         }
         
         public String getComment() {
             return itemComment;
         }
         
         public boolean isEmptyBody() {
             return ((itemBody == null) || (itemBody.equals("")));
         }
         
         public boolean isEmptyImgUrl() {
             return ((itemImgUrl == null) || (itemImgUrl.equals("")));
         }
         
         public boolean isEmptyComment() {
             return ((itemComment == null) || (itemComment.equals("")));
         }
         
         public String toString() {
             return ("itemBody[" + itemBody + "], itemImgUrl[" + itemImgUrl + "], itemComment[" + itemComment + "]");
         }
     }
 
     public BullpenContentFactory(Context context, Intent intent) {
         Log.i(TAG, "constructor");
 
         mContext = context;
         mAppWidgetId = intent.getIntExtra(
                 AppWidgetManager.EXTRA_APPWIDGET_ID,
                 AppWidgetManager.INVALID_APPWIDGET_ID);
         mSelectedItemUrl = intent.getStringExtra(Constants.EXTRA_ITEM_URL);
         Log.i(TAG, "constructor - mSelectedItemUrl[" + mSelectedItemUrl + "]");
         
         //mConnectivityManager =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
         
         setupIntentListener();
     }
 
     @Override
     public RemoteViews getViewAt(int position) {
         //Log.i(TAG, "getViewAt - position[" + position + "]");
         
     	if (mIsSkipFirstCallOfGetViewAt) {
     		mIsSkipFirstCallOfGetViewAt = false;
     		return null;
     	}
 
         if (mIsUpdateRemoteView) {
         	RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.content_row);
             if (mContentItem.isEmptyBody() == false)
                 rv.setTextViewText(R.id.contentRowBodyText, mContentItem.getBody());
 
             if (mContentItem.isEmptyImgUrl() == false) {
                 mBitmap = getImageBitmap(mContentItem.getImgUrl());
                 if (mBitmap != null) {
                 	//Log.i(TAG, "setImageViewBitmap - given bitmap");
                     rv.setImageViewBitmap(R.id.contentRowImage, mBitmap);
                 } else {
                 	//Log.i(TAG, "setImageViewBitmap - null1");
                 	rv.setImageViewBitmap(R.id.contentRowImage, null);
                 }
             } else {
             	//Log.i(TAG, "setImageViewBitmap - null2");
             	rv.setImageViewBitmap(R.id.contentRowImage, null);
             }
 
             if (mContentItem.isEmptyComment() == false)
                 rv.setTextViewText(R.id.contentRowCommentText, mContentItem.getComment());
 
             Intent fillInIntent = new Intent();
             rv.setOnClickFillInIntent(R.id.contentRowLayout, fillInIntent);
 
             mIsUpdateRemoteView = false;
             return rv;
         }
         
         return null;
     	
     }
     
     @Override
     public void onDataSetChanged() {
         Log.i(TAG, "onDataSetChanged");
                 
         // We check internet connection when BaseballWidgetProvider receives intent ACTION_SHOW_ITEM.
         // So just skip to check it here.
         //if (Utils.checkInternetConnectivity(mConnectivityManager)) {
         if (mSelectedItemUrl != null) {
             // Parse MLBPark html data and add items to the widget item array list.
             try {
                 parseMLBParkHtmlData(mSelectedItemUrl);
                 mIsSkipFirstCallOfGetViewAt = true;
                 mIsUpdateRemoteView = true;
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         //}
     }
     
     @Override
     public int getCount() {
         return 1;
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
         teardownIntentListener();
     }
 
     private void parseMLBParkHtmlData(String urlAddress) throws IOException {
         Source source = new Source(new URL(urlAddress));
         source.fullSequentialParse();
 
         if (mBitmap != null) {
             mBitmap.recycle();
             mBitmap = null;
         }
         
         String itemBody = "";
         String itemImgUrl = "";
         String itemComment = "";
 
         List<Element> divs = source.getAllElements(HTMLElementName.DIV);
         for (int i = 0; i < divs.size(); i++) {
             Element div = divs.get(i);
             String value;
             
             // Find the same pattern with <div align="justify". This means the body of this article.
             value = div.getAttributeValue("align");
             if (value != null && value.equals("justify")) {
                 Segment seg = div.getContent();
                 boolean isSkipSegment = false;
                 //Log.i(TAG, "content segment[" + seg.toString() + "]");
                 
                 // Parse article body.
                 for (Iterator<Segment> nodeIterator = seg.getNodeIterator() ; nodeIterator.hasNext();) {
                     Segment nodeSegment = nodeIterator.next();
                     
                     // If article body has <head>...</head>, just skip!
                     // If <br> tag, add new line to itemBody.
                     // if <img> tag, add img address to itemImgUrl.
                     if (nodeSegment instanceof StartTag) {
                         String tagName = ((Tag)nodeSegment).getName();
                         if (tagName.equals("head")) {
                             isSkipSegment = true;
                         } else if (!isSkipSegment && tagName.equals("br")) {
                             itemBody += "\n";
                         } else if (!isSkipSegment && tagName.equals("img")) {
                             itemImgUrl = ((StartTag) nodeSegment).getAttributeValue("src");
                         }
                         continue;
                         
                     // If </p> or </div> tag, add new line to itemBody.
                     } else if (nodeSegment instanceof EndTag) {
                         String tagName = ((Tag)nodeSegment).getName();
                         if (tagName.equals("head")) {
                             isSkipSegment = false;
                         } else if (!isSkipSegment && (tagName.equals("p") || tagName.equals("div"))) {
                             itemBody += "\n";
                         }
                         continue;
                         
                     // Ignore &bnsp;
                     } else if (nodeSegment instanceof CharacterReference) {
                         continue;
                         
                     // If plain text, add it to itemBody.
                     } else {
                         if (!isSkipSegment && (nodeSegment.isWhiteSpace() == false)) {
                             itemBody += nodeSegment.getTextExtractor().toString();
                         }
                     }
                 }
             }
             
             // Find the same pattern with <div id="myArea". This means the comment of this article.
             value = div.getAttributeValue("id");
             if (value != null && value.equals("myArea")) {
                 Segment seg = div.getContent();
                 boolean isSkipSegment = false, isAddNick = false, isAddComment = false;;
                 //Log.i(TAG, "comment segment[" + seg.toString() + "]");
                 
                 // Parse article comment.
                 for (Iterator<Segment> nodeIterator = seg.getNodeIterator() ; nodeIterator.hasNext();) {
                     Segment nodeSegment = nodeIterator.next();
                     
                     // If article body has <script>...</script>, just skip!
                     // If <a> tag, prepare to add nick.
                     // if <tv class="G12"> tag, prepare to add comment.
                     if (nodeSegment instanceof StartTag) {
                         String tagName = ((Tag)nodeSegment).getName();
                         if (tagName.equals("script")) {
                             isSkipSegment = true;
                         } else if (tagName.equals("a")) {
                             isAddNick = true;
                         } else if (tagName.equals("td")) {
                             String classAttr = ((StartTag)nodeSegment).getAttributeValue("class");
                             if (classAttr != null && classAttr.equals("G12"))
                                 isAddComment = true;
                         }
                         continue;
                     
                     } else if (nodeSegment instanceof EndTag) {
                         String tagName = ((Tag)nodeSegment).getName();
                         if (tagName.equals("script")) {
                             isSkipSegment = false;
                         }
                         continue;
                         
                     // Ignore &bnsp;
                     } else if (nodeSegment instanceof CharacterReference) {
                         continue;
                         
                     // If plain text, add it to itemComment.
                     } else {
                         if (!isSkipSegment) {
                             if (isAddNick) {
                                 itemComment += ("[" + nodeSegment.getTextExtractor().toString() + "] : ");
                                 isAddNick = false;
                             } else if (isAddComment) {
                                 itemComment += (nodeSegment.getTextExtractor().toString() + "\n\n");
                                 isAddComment = false;
                             }
                         }
                     }
                 }
 
                 // Finish!
                 break;
             }
         }
         
         mContentItem = new contentItem(itemBody, itemImgUrl, itemComment);
         //Log.i(TAG, "mContentItem[" + mContentItem.toString() + "]");
 
         Log.i(TAG, "parseMLBParkHtmlData - done!");
         return;
     }
 
     private Bitmap getImageBitmap(String url) { 
         Bitmap bm = null; 
         try { 
             URL aURL = new URL(url); 
             URLConnection conn = aURL.openConnection(); 
             conn.connect(); 
             InputStream is = conn.getInputStream(); 
             BufferedInputStream bis = new BufferedInputStream(is); 
             bm = BitmapFactory.decodeStream(bis); 
             bis.close(); 
             is.close(); 
        } catch (IOException e) { 
            Log.e(TAG, "Error getting bitmap", e); 
        } 
        return bm; 
     } 
     
     private void setupIntentListener() {
         if (mIntentListener == null) {
             mIntentListener = new BroadcastReceiver() {
                 @Override
                 public void onReceive(Context context, Intent intent) {
                     // Update mSelectedItemUrl through Broadcast Intent.
                     Log.i(TAG, "onReceive - update mSelectedItemUrl[" + mSelectedItemUrl + "]");
                     mSelectedItemUrl = intent.getStringExtra(Constants.EXTRA_ITEM_URL);
                 }
             };
             IntentFilter filter = new IntentFilter();
             filter.addAction(Constants.ACTION_UPDATE_URL);
             mContext.registerReceiver(mIntentListener, filter);
         }
     }
 
     private void teardownIntentListener() {
         if (mIntentListener != null) {
             mContext.unregisterReceiver(mIntentListener);
             mIntentListener = null;
         }
     }
 }
