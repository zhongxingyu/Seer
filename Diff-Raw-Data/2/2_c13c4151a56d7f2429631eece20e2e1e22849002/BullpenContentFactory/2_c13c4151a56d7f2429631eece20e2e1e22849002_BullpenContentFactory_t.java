 
 package com.smilo.bullpen;
 
 import com.smilo.bullpen.Constants.PARSING_RESULT;
 
 import net.htmlparser.jericho.CharacterReference;
 import net.htmlparser.jericho.Element;
 import net.htmlparser.jericho.EndTag;
 import net.htmlparser.jericho.HTMLElementName;
 import net.htmlparser.jericho.Segment;
 import net.htmlparser.jericho.Source;
 import net.htmlparser.jericho.StartTag;
 import net.htmlparser.jericho.Tag;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
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
 
     private static JSONObject mParsedJSONObject = null;
     private static Context mContext;
     private static int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
     private static String mSelectedItemUrl = null;
     private static BroadcastReceiver mIntentListener;
     private static PARSING_RESULT mParsingResult = PARSING_RESULT.FAILED_UNKNOWN;
     private static int mPageNum = Constants.DEFAULT_PAGE_NUM;
     
     private static final String JSON_TITLE = "title";
     private static final String JSON_WRITER = "writer";
     private static final String JSON_BODY = "body";
     private static final String JSON_BODY_TEXT = "bodyText";
     private static final String JSON_BODY_IMAGE = "bodyImage";
     private static final String JSON_COMMENT = "comment";
     private static final String JSON_COMMENT_WRITER = "commentWriter";
     private static final String JSON_COMMENT_TEXT = "commentText";
 
     public BullpenContentFactory(Context context, Intent intent) {
         mContext = context;
         mAppWidgetId = intent.getIntExtra(
                 AppWidgetManager.EXTRA_APPWIDGET_ID,
                 AppWidgetManager.INVALID_APPWIDGET_ID);
         mSelectedItemUrl = intent.getStringExtra(Constants.EXTRA_ITEM_URL);
         mPageNum = intent.getIntExtra(Constants.EXTRA_PAGE_NUM, Constants.DEFAULT_PAGE_NUM);
         Log.i(TAG, "constructor - mSelectedItemUrl[" + mSelectedItemUrl +
                 "], mPageNum[" + mPageNum + "], mAppWidgetId[" + mAppWidgetId + "]");
         
         setupIntentListener();
     }
 
     @Override
     public RemoteViews getViewAt(int position) {
         //Log.i(TAG, "getViewAt - position[" + position + "]");
 
         // Create remoteViews
         RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.content_row);
         
         switch (mParsingResult) {
             case  SUCCESS :
                 RemoteViews rvDivider = new RemoteViews(mContext.getPackageName(), R.layout.content_row_divider);
                 
                 // Set writer and title
                 String contentTitle = mParsedJSONObject.optString(JSON_TITLE);
                 String contentWriter = mParsedJSONObject.optString(JSON_WRITER);
                 if (contentTitle != null && contentTitle.length() > 0) {
                     if (contentWriter != null && contentWriter.length() > 0) {
                         rv.setTextViewText(R.id.contentRowTitleText, "[" + contentWriter + "] " + contentTitle);
                     } else {
                         rv.setTextViewText(R.id.contentRowTitleText, "[writer not existed]" + contentTitle);
                     }
                 } else {
                     if (contentWriter != null && contentWriter.length() > 0) {
                         rv.setTextViewText(R.id.contentRowTitleText, "[" + contentWriter + "]");
                     } else {
                         rv.setTextViewText(R.id.contentRowTitleText, "[writer not existed] title not existed");
                     }
                 }
                 
                 // Add a divider between title and body.
                 rv.addView(R.id.contentRowBodyLayout, rvDivider);
                 
                 // Set text and image of content body.
                 JSONArray bodyArray = mParsedJSONObject.optJSONArray(JSON_BODY);
                 if (bodyArray != null && bodyArray.length() > 0) {
                     for (int i = 0 ; i < bodyArray.length() ; i++) {
                         JSONObject obj = bodyArray.optJSONObject(i);
                         if (obj == null || obj.length() == 0) {
                             break;
                         }
                         String bodyText = obj.optString(JSON_BODY_TEXT);
                         if (bodyText != null && bodyText.length() > 0) {
                             //Log.i(TAG, "getViewAt - text[" + bodyText + "]");
                             RemoteViews rvBodyText = new RemoteViews(mContext.getPackageName(), R.layout.content_row_text);
                             rvBodyText.setTextViewText(R.id.contentRowText, bodyText);
                             rv.addView(R.id.contentRowBodyLayout, rvBodyText);
                             continue;
                         }
                         String bodyImage = obj.optString(JSON_BODY_IMAGE);
                         if (bodyImage != null && bodyImage.length() > 0) {
                             Log.i(TAG, "getViewAt - image[" + bodyImage + "]");
                             // TODO : manage bitmap
                             RemoteViews rvBodyImage = new RemoteViews(mContext.getPackageName(), R.layout.content_row_image);
                             Bitmap bitmap = null;
                             try {
                                 bitmap = getImageBitmap(bodyImage);
                                 Log.i(TAG, "getViewAt - getImageBitmap - bitmap is ok!");
                                 rvBodyImage.setImageViewBitmap(R.id.contentRowImage, bitmap);
                             } catch (IOException e) {
                                 Log.e(TAG, "getViewAt - getImageBitmap - IOException![" + e.toString() + "]");
                                 e.printStackTrace();
                                 rvBodyImage.setImageViewBitmap(R.id.contentRowImage, null);
                             } catch (RuntimeException e) {
                                 Log.e(TAG, "getViewAt - getImageBitmap - RuntimeException![" + e.toString() + "]");
                                 e.printStackTrace();
                                 rvBodyImage.setImageViewBitmap(R.id.contentRowImage, null);
                             } catch (OutOfMemoryError e) {
                                 Log.e(TAG, "getViewAt - getImageBitmap - OutOfMemoryError![" + e.toString() + "]");
                                 e.printStackTrace();
                                 rvBodyImage.setImageViewBitmap(R.id.contentRowImage, null);
                             }
                             rv.addView(R.id.contentRowBodyLayout, rvBodyImage);
                         }
                     }
                 }
                 
                 // Add a divider between body and title.
                 rv.addView(R.id.contentRowCommentLayout, rvDivider);
                 
                 // Set text of content comment.
                 JSONArray commentArray = mParsedJSONObject.optJSONArray(JSON_COMMENT);
                 if (commentArray != null && commentArray.length() > 0) {
                     for (int i = 0 ; i < commentArray.length() ; i++) {
                         JSONObject obj = commentArray.optJSONObject(i);
                         if (obj == null || obj.length() == 0) {
                             break;
                         }
                         String commentWriter = obj.optString(JSON_COMMENT_WRITER);
                         String commentText = obj.optString(JSON_COMMENT_TEXT);
                         RemoteViews rvComment = new RemoteViews(mContext.getPackageName(), R.layout.content_row_text);
                         if (commentWriter != null && commentWriter.length() > 0) {
                             if (commentText != null && commentText.length() >0) {
                                 rvComment.setTextViewText(R.id.contentRowText, "[" + commentWriter + "] " + commentText);
                             } else {
                                rvComment.setTextViewText(R.id.contentRowText, "[" + commentWriter + "] comment not existed");
                             }
                         } else {
                             if (commentText != null && commentText.length() >0) {
                                 rvComment.setTextViewText(R.id.contentRowText, "[writer not existed] " + commentText);
                             } else {
                                 rvComment.setTextViewText(R.id.contentRowText, "[writer not existed] comment not existed");
                             }
                         }
                         rv.addView(R.id.contentRowCommentLayout, rvComment);
                         rv.addView(R.id.contentRowCommentLayout, rvDivider);
                     }
                 }
                 break;
             
             case FAILED_IO_EXCEPTION :
                 rv.setTextViewText(R.id.contentRowTitleText, mContext.getResources().getString(R.string.text_failed_io_exception));
                 break;
                 
             case FAILED_JSON_EXCEPTION :
                 rv.setTextViewText(R.id.contentRowTitleText, mContext.getResources().getString(R.string.text_failed_json_exception));
                 break;
                 
             case FAILED_STACK_OVERFLOW :
                 rv.setTextViewText(R.id.contentRowTitleText, mContext.getResources().getString(R.string.text_failed_stack_overflow));
                 break;
                 
             case FAILED_UNKNOWN :
             default:
                 rv.setTextViewText(R.id.contentRowTitleText, mContext.getResources().getString(R.string.text_failed_unknown));
                 break;
         }
 
         Intent fillInIntent = new Intent();
         fillInIntent.putExtra(Constants.EXTRA_PAGE_NUM, mPageNum);
         rv.setOnClickFillInIntent(R.id.contentRowLayout, fillInIntent);
         return rv;
     }
     
     @Override
     public void onDataSetChanged() {
         Log.i(TAG, "onDataSetChanged - mSelectedItemUrl[" + mSelectedItemUrl + "]");
 
         if (mSelectedItemUrl == null) {
             Log.e(TAG, "onDataSetChanged - mSelectedItemUrl is null!");
             return;
         }
         
         // Parse MLBPark html data and add items to the widget item array list.
         try {
             mParsingResult = parseMLBParkHtmlDataMobileVer(mSelectedItemUrl);
         } catch (IOException e) {
             Log.e(TAG, "onDataSetChanged - parseMLBParkHtmlDataMobileVer - IOException![" + e.toString() + "]");
             e.printStackTrace();
             mParsingResult = PARSING_RESULT.FAILED_IO_EXCEPTION;
         } catch (JSONException e) {
             Log.e(TAG, "onDataSetChanged - parseMLBParkHtmlDataMobileVer - JSONException![" + e.toString() + "]");
             e.printStackTrace();
             mParsingResult = PARSING_RESULT.FAILED_JSON_EXCEPTION;
         } catch (StackOverflowError e) {
             Log.e(TAG, "onDataSetChanged - parseMLBParkHtmlDataMobileVer - StackOverflowError![" + e.toString() + "]");
             e.printStackTrace();
             mParsingResult = PARSING_RESULT.FAILED_STACK_OVERFLOW;
         }
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
         RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.content_row_text);
         rv.setTextViewText(R.id.contentRowText, mContext.getResources().getString(R.string.text_loadingView));
 
         return rv;
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
 
     private PARSING_RESULT parseMLBParkHtmlDataMobileVer(String urlAddress) throws IOException, JSONException, StackOverflowError {
 
         // Load HTML data from given urlAddress.
         Source source = new Source(new URL(urlAddress));
         source.fullSequentialParse();
         
         // Create an empty JSONObjects.
         JSONObject obj = new JSONObject();
         JSONArray body = new JSONArray();
         JSONArray comment = new JSONArray();
         obj.put(JSON_BODY, body);
         obj.put(JSON_COMMENT, comment);
         
         List<Element> divs = source.getAllElements(HTMLElementName.DIV);
         for (int i = 0; i < divs.size(); i++) {
             Element div = divs.get(i);
             String value = div.getAttributeValue("class");
             
             // Find the same pattern with <div class='article'>. This means the title of this article.
             if (value != null && value.equals("article")) {
                 Element h3 = div.getContent().getFirstElement("h3");
                 String itemTitle = h3.getTextExtractor().toString();
                 //Log.i(TAG, "parseMLBParkHtmlDataMobileVer - parsed title[" + itemTitle + "]");
                 
                 // Put itemTitle to the 'obj' JSONObject.
                 obj.put(JSON_TITLE, itemTitle);
                 continue;
             
             // Find the same pattern with <div class='w'>. This means the writer of this article.
             } else if (value != null && value.equals("w")) {
                 Segment seg = div.getContent();
                 boolean isAddWriter = false;
                 for (Iterator<Segment> nodeIterator = seg.getNodeIterator() ; nodeIterator.hasNext();) {
                     Segment nodeSeg = nodeIterator.next();
                     if (nodeSeg instanceof StartTag) {
                         ;
                     } else if (nodeSeg instanceof EndTag) {
                         if (((Tag) nodeSeg).getName().equals("strong")) {
                             isAddWriter = true;
                         }
                     } else if (nodeSeg instanceof CharacterReference) {
                         ;
                     } else {
                         if (isAddWriter) {
                             String itemWriter = nodeSeg.getTextExtractor().toString();
                             //Log.i(TAG, "parseMLBParkHtmlDataMobileVer - parsed writer[" + itemWriter + "]");
                             isAddWriter = false;
                             
                             // Put itemWriter to the 'obj' JSONObject.
                             obj.put(JSON_WRITER, itemWriter);
                             break;
                         }
                     }
                 }
                 continue;
                 
             // Find the same pattern with <div class='ar_txt'>. This means the body of this article.
             } else if (value != null && value.equals("ar_txt")) {
                 Segment seg = div.getContent();
                 boolean isSkipSegment = false, isAddTextToBody = false;
                 String itemBodyText = "";
                 for (Iterator<Segment> nodeIterator = seg.getNodeIterator() ; nodeIterator.hasNext();) {
                     Segment nodeSeg = nodeIterator.next();
                     if (nodeSeg instanceof StartTag) {
                         String tagName = ((Tag)nodeSeg).getName();
                         if (tagName.equals("style")) {
                             isSkipSegment = true;
                         } else if (!isSkipSegment && (tagName.equals("br") || tagName.equals("div"))) {
                             itemBodyText += "\n";
                             isAddTextToBody = true;
                         } else if (!isSkipSegment && tagName.equals("img")) {
                             // If stored itemBodyText exists before parsing image tag,
                             // put it to the 'body' JSONArray and initialize isAddTextToBody, itemBodyText.
                             if (isAddTextToBody) {
                                 JSONObject newBodyText = new JSONObject();
                                 newBodyText.put(JSON_BODY_TEXT, itemBodyText);
                                 body.put(newBodyText);
                                 isAddTextToBody = false;
                                 itemBodyText = "";
                             }
                             String itemImgUrl = ((StartTag) nodeSeg).getAttributeValue("src");
                             
                             // Put itemImgUrl to the 'body' JSONArray.
                             JSONObject newImgUrl = new JSONObject();
                             newImgUrl.put(JSON_BODY_IMAGE, itemImgUrl);
                             body.put(newImgUrl);
                         }
                     } else if (nodeSeg instanceof EndTag) {
                         String tagName = ((Tag)nodeSeg).getName();
                         if (tagName.equals("style")) {
                             isSkipSegment = false;
                         } else if (!isSkipSegment && tagName.equals("p")) {
                             itemBodyText += "\n";
                             isAddTextToBody = true;
                         }
                     } else if (nodeSeg instanceof CharacterReference) {
                         ;
                     } else {
                         if (!isSkipSegment && (nodeSeg.isWhiteSpace() == false)) {
                             itemBodyText += nodeSeg.getTextExtractor().toString();
                             isAddTextToBody = true;
                         }
                     }
                 }
                 // If stored itemBodyText exists after parsing this article,
                 // put it to the 'body' JSONArray.
                 if (isAddTextToBody) {
                     JSONObject newBodyText = new JSONObject();
                     newBodyText.put(JSON_BODY_TEXT, itemBodyText);
                     body.put(newBodyText);
                 }
                 continue;
                 
             // Find the same pattern with <div class='reply'>. This means the comment of this article.
             } else if (value != null && value.equals("reply")) {
                 Element ul = div.getFirstElement("ul");
                 boolean isSkipSegment = false, isAddNick = false, isAddComment = false;
                 String tmpComment = "";
                 for (Iterator<Segment> nodeIterator = ul.getNodeIterator() ; nodeIterator.hasNext();) {
                     Segment nodeSeg = nodeIterator.next();
                     if (nodeSeg instanceof StartTag) {
                         String tagName = ((Tag)nodeSeg).getName();
                         if (tagName.equals("strong")) {
                             isAddComment = true;
                         } else if (tagName.equals("br")) {
                             tmpComment += "\n";
                         } else if (tagName.equals("span")) {
                             String classAttr = ((StartTag) nodeSeg).getAttributeValue("class");
                             if (classAttr != null && classAttr.equals("ti")) {
                                 isAddNick = true;
                             }
                         }
                     } else if (nodeSeg instanceof EndTag) {
                         String tagName = ((Tag)nodeSeg).getName();
                         if (tagName.equals("strong")) {
                             isAddComment = false;
                         }
                     } else if (nodeSeg instanceof CharacterReference) {
                         ;
                     } else {
                         if (!isSkipSegment && isAddComment) {
                             tmpComment += nodeSeg.getTextExtractor().toString();
                         } else if (!isSkipSegment && isAddNick) {
                             // Put comment info to the 'comment' JSONArray.
                             String writer = nodeSeg.getTextExtractor().toString();
                             JSONObject newComment = new JSONObject();
                             newComment.put(JSON_COMMENT_WRITER, writer);
                             newComment.put(JSON_COMMENT_TEXT, tmpComment);
                             comment.put(newComment);
                             tmpComment = "";
                             isAddNick = false;
                         }
                     }
                 }
 
                 // Finish!
                 break;
             }
         }
         
         // Save parsed result.
         mParsedJSONObject = obj;
         //Log.i(TAG, "parseMLBParkHtmlDataMobileVer - mParsedJSONString[" + obj.toString(4) + "]");
         Log.i(TAG, "parseMLBParkHtmlDataMobileVer - done!");
         
         return PARSING_RESULT.SUCCESS;
     }
     
     private Bitmap getImageBitmap(String url) throws IOException, RuntimeException, OutOfMemoryError { 
         Bitmap bm = null; 
         URL aURL = new URL(url); 
         URLConnection conn = aURL.openConnection(); 
         conn.connect(); 
         InputStream is = conn.getInputStream(); 
         BufferedInputStream bis = new BufferedInputStream(is); 
         bm = BitmapFactory.decodeStream(bis);
         bis.close(); 
         is.close();
         return bm; 
     } 
     
     private void setupIntentListener() {
         if (mIntentListener == null) {
             mIntentListener = new BroadcastReceiver() {
                 @Override
                 public void onReceive(Context context, Intent intent) {
                     // Update mSelectedItemUrl through Broadcast Intent.
                     mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                     mSelectedItemUrl = intent.getStringExtra(Constants.EXTRA_ITEM_URL);
                     mPageNum = intent.getIntExtra(Constants.EXTRA_PAGE_NUM, Constants.DEFAULT_PAGE_NUM);
                     Log.i(TAG, "onReceive - update mSelectedItemUrl[" + mSelectedItemUrl + 
                             "], mPageNum[" + mPageNum + "], mAppWidgetId[" + mAppWidgetId + "]");
                 }
             };
             IntentFilter filter = new IntentFilter();
             filter.addAction(Constants.ACTION_UPDATE_ITEM_URL);
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
