 package edu.stanford.mobisocial.dungbeetle.model;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import edu.stanford.mobisocial.dungbeetle.App;
 import edu.stanford.mobisocial.dungbeetle.DBHelper;
 import edu.stanford.mobisocial.dungbeetle.DungBeetleContentProvider;
 import edu.stanford.mobisocial.dungbeetle.Helpers;
 import edu.stanford.mobisocial.dungbeetle.R;
 import edu.stanford.mobisocial.dungbeetle.feed.DbObjects;
 import edu.stanford.mobisocial.dungbeetle.feed.iface.Activator;
 import edu.stanford.mobisocial.dungbeetle.feed.iface.DbEntryHandler;
 import edu.stanford.mobisocial.dungbeetle.feed.iface.FeedRenderer;
 import edu.stanford.mobisocial.dungbeetle.feed.objects.LikeObj;
 import edu.stanford.mobisocial.dungbeetle.obj.ObjActions;
 import edu.stanford.mobisocial.dungbeetle.obj.iface.ObjAction;
 import edu.stanford.mobisocial.dungbeetle.ui.HomeActivity;
 import edu.stanford.mobisocial.dungbeetle.util.ContactCache;
 import edu.stanford.mobisocial.dungbeetle.util.Maybe;
 import edu.stanford.mobisocial.dungbeetle.util.RelativeDate;
 
 public class DbObject {
     private static final String TAG = "dbObject";
 
     public static final String TABLE = "objects";
     public static final String _ID = "_id";
     public static final String TYPE = "type";
     public static final String SEQUENCE_ID = "sequence_id";
     public static final String FEED_NAME = "feed_name";
     public static final String CONTACT_ID = "contact_id";
     public static final String DESTINATION = "destination";
     public static final String JSON = "json";
     public static final String TIMESTAMP = "timestamp";
     public static final String APP_ID = "app_id";
     public static final String SENT = "sent";
 	public static final String ENCODED = "encoded";
 	public static final String CHILD_FEED_NAME = "child_feed";
 	public static final String HASH = "hash";
 
     public static final String EXTRA_FEED_URI = "feed_uri";
 	public static final String RAW = "raw";
 
     private final Cursor mCursor;
     protected final String mType;
     protected JSONObject mJson;
     private Long mTimestamp;
     private static OnClickViewProfile sViewProfileAction;
 
     public DbObject(String type, JSONObject json) {
         mCursor = null;
         mType = type;
         mJson = json;
     }
 
     private DbObject(Cursor c) {
         mType = c.getString(c.getColumnIndexOrThrow(DbObject.TYPE));
         mCursor = c;
     }
 
     public String getType() {
         return mType;
     }
     public JSONObject getJson() {
         if (mJson == null && mCursor != null) {
             String jsonStr = mCursor.getString(mCursor.getColumnIndexOrThrow(DbObject.JSON));
             try {
                 mJson = new JSONObject(jsonStr);
             } catch (JSONException e) {
                 Log.wtf("DB", "Bad json from database.");
             }
         }
         return mJson;
     }
 
     public Long getTimestamp() {
         if (mTimestamp == null && mCursor != null) {
             mTimestamp = mCursor.getLong(mCursor.getColumnIndexOrThrow(DbObject.TIMESTAMP));
         }
         return mTimestamp;
     }
 
     public static DbObject fromCursor(Cursor c) {
         try {
             return new DbObject(c);
         } catch (Exception e) {
             Log.wtf("Bad data from db", e);
             return null;
         } 
     }
     public static final Uri OBJ_URI = Uri.parse(DungBeetleContentProvider.CONTENT_URI + "/obj");
     /**
      * @param v the view to bind
      * @param context standard activity context
      * @param c the cursor source for the object in the db object table
      * @param contactCache prevents copious lookups of contact information from the sqlite database
      * @param allowInteractions controls whether the bound view is allowed to intercept touch events and do its own processing.
      */
     public static void bindView(View v, final Context context, Cursor c,
             ContactCache contactCache, boolean allowInteractions) {
     	//there is probably a utility or should be one that does this
         long objId = c.getLong(0);
     	Cursor cursor = context.getContentResolver().query(OBJ_URI,
             	new String[] { 
             		DbObject.JSON,
             		DbObject.RAW,
             		DbObject.CONTACT_ID,
             		DbObject.TIMESTAMP,
             		DbObject.HASH,
             		DbObject.FEED_NAME
             	},
             	DbObject._ID + " = ?", new String[] {String.valueOf(objId)}, null);
     	if(cursor == null) {
     		Log.wtf("Dbbject", "cursor was null for bund view of db object");
     		return;
     	}
         if(!cursor.moveToFirst())
         	return;
         
         String jsonSrc = cursor.getString(0);
         byte[] raw = cursor.getBlob(1);
         Long contactId = cursor.getLong(2);
         Long timestamp = cursor.getLong(3);
         Long hash = cursor.getLong(4);
         String feedName = cursor.getString(5);
         Date date = new Date(timestamp);
         cursor.close();
        	///////
         
         try{
             Contact contact = contactCache.getContact(contactId).get();
 
             TextView nameText = (TextView) v.findViewById(R.id.name_text);
             nameText.setText(contact.name);
 
             final ImageView icon = (ImageView)v.findViewById(R.id.icon);
             if (sViewProfileAction == null) {
                 sViewProfileAction = new OnClickViewProfile((Activity)context);
             }
             icon.setTag(contactId);
             if (allowInteractions) {
                 icon.setOnClickListener(sViewProfileAction);
                 v.setTag(objId);
                 v.setClickable(true);
                 v.setFocusable(true);
                 v.setOnClickListener(ItemClickListener.getInstance(context));
                 v.setOnLongClickListener(ItemLongClickListener.getInstance(context));
             }
             // TODO: this is horrible
             ((App)((Activity)context).getApplication()).contactImages.lazyLoadContactPortrait(contact, icon);
 
             try {
                 JSONObject content = new JSONObject(jsonSrc);
 
                 TextView timeText = (TextView)v.findViewById(R.id.time_text);
                 timeText.setText(RelativeDate.getRelativeDate(date));
 
                 ViewGroup frame = (ViewGroup)v.findViewById(R.id.object_content);
                 frame.removeAllViews();
                 frame.setTag(R.id.object_entry, c.getPosition());
                 
         		FeedRenderer renderer = DbObjects.getFeedRenderer(content);
         		if(renderer != null)
         			renderer.render(context, frame, content, raw, allowInteractions);
 
                 if (!allowInteractions) {
                     v.findViewById(R.id.obj_attachments).setVisibility(View.GONE);
                 } else {
                     /*
                      * Set the number of current likes 
                      * TODO: Optimize
                      */
                     DBHelper helper = new DBHelper(context);
                     Cursor likes = helper.queryRelatedObjs(objId, LikeObj.TYPE);
                     Button button = (Button)v.findViewById(R.id.obj_attachments);
                     button.setText("Likes: " + likes.getCount());
                     helper.close();
                     button.setTag(R.id.object_entry, hash);
                     button.setTag(R.id.feed_label, Feed.uriForName(feedName));
                     button.setOnClickListener(LikeListener.getInstance(context));
                     // button.setBackgroundColor(Feed.colorFor(feedName)); // TODO: per-obj coloring.
                 }
             } catch (JSONException e) {
                 Log.e("db", "error opening json", e);
             }
         }
         catch(Maybe.NoValError e){}
     }
 
     public static class OnClickViewProfile implements View.OnClickListener {
         private final Activity mmContext;
 
         @Override
         public void onClick(View v) {
             Long contactId = (Long)v.getTag();
             Contact.view(mmContext, contactId);
         }
 
         public OnClickViewProfile(Activity c) {
             mmContext = c;
         }
     }
 
     private static class LikeListener implements OnClickListener {
         private Context mmContext;
         private static LikeListener mmListener;
         public static LikeListener getInstance(Context context) {
             if (mmListener == null || mmListener.mmContext != context) {
                 mmListener = new LikeListener(context);
             }
             Log.d(TAG, "returning " + mmListener);
             return mmListener;
         }
 
         private LikeListener(Context context) {
             mmContext = context;
         }
 
         @Override
         public void onClick(View v) {
             Long hash = (Long)v.getTag(R.id.object_entry);
             Uri feed = (Uri)v.getTag(R.id.feed_label);
             DbObject obj = LikeObj.forObj(hash);
             Log.d(TAG, "Sending " + obj);
             Helpers.sendToFeed(mmContext, obj, feed);
         }
     };
 
     private static class ItemClickListener implements View.OnClickListener {
         private final Context mContext;
         private static ItemClickListener sInstance;
 
         private ItemClickListener(Context context) {
             mContext = context;
         }
 
         public static ItemClickListener getInstance(Context context) {
             if (sInstance == null || sInstance.mContext != context) {
                 sInstance = new ItemClickListener(context);
             }
             return sInstance;
         }
        
         @Override
         public void onClick(View v) {
             long objId = (Long)v.getTag();
             Cursor cursor = mContext.getContentResolver().query(DbObject.OBJ_URI,
                     new String[] { 
                         DbObject.JSON,
                         DbObject.RAW,
                     },
                     DbObject._ID + " = ?", new String[] { String.valueOf(objId) }, null);
             if(!cursor.moveToFirst()) {
                 return;
             }
             
             final String jsonSrc = cursor.getString(0);
             final byte[] raw = cursor.getBlob(1);
             cursor.close();
 
             if (HomeActivity.DBG) Log.i(TAG, "Clicked object: " + jsonSrc);
             try{
                 JSONObject obj = new JSONObject(jsonSrc);
                 Activator activator = DbObjects.getActivator(obj);
                 if(activator != null){
                     activator.activate(mContext, obj, raw);
                 }
             }
             catch(JSONException e){
                 Log.e(TAG, "Couldn't parse obj.", e);
             }
         }
     };
 
     private static class ItemLongClickListener implements View.OnLongClickListener {
         private final Context mContext;
         private static ItemLongClickListener sInstance;
 
         private ItemLongClickListener(Context context) {
             mContext = context;
         }
 
         public static ItemLongClickListener getInstance(Context context) {
             if (sInstance == null || sInstance.mContext != context) {
                 sInstance = new ItemLongClickListener(context);
             }
             return sInstance;
         }
        
         @Override
         public boolean onLongClick(View v) {
             long objId = (Long)v.getTag();
             Cursor cursor = mContext.getContentResolver().query(DbObject.OBJ_URI,
                     new String[] { 
                         DbObject.JSON,
                         DbObject.RAW,
                         DbObject.TYPE,
                         DbObject.FEED_NAME
                     },
                     DbObject._ID + " = ?", new String[] { String.valueOf(objId) }, null);
             if(!cursor.moveToFirst()) {
                 return false;
             }
             
             final String jsonSrc = cursor.getString(0);
             final byte[] raw = cursor.getBlob(1);
             String type = cursor.getString(2);
             Uri feedUri = Feed.uriForName(cursor.getString(3));
             cursor.close();
 
             if (HomeActivity.DBG) Log.i(TAG, "LongClicked object: " + jsonSrc);
             try {
                 JSONObject obj = new JSONObject(jsonSrc);
                 createActionDialog(mContext, feedUri, type, obj, raw);
             } catch(JSONException e){
                 Log.e(TAG, "Couldn't parse obj.", e);
             }
             return false;
         }
     };
 
     public static Dialog createActionDialog(final Context context, final Uri feedUri,
             final String type, final JSONObject json, final byte[] raw) {
 
         final DbEntryHandler dbType = DbObjects.forType(type);
         final List<ObjAction> actions = new ArrayList<ObjAction>();
         for (ObjAction action : ObjActions.getObjActions()) {
            if (action.isActive(dbType, json)) {
                 actions.add(action);
             }
         }
         final String[] actionLabels = new String[actions.size()];
         int i = 0;
         for (ObjAction action : actions) {
             actionLabels[i++] = action.getLabel();
         }
         return new AlertDialog.Builder(context).setTitle("Handle...")
                 .setItems(actionLabels, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         actions.get(which).actOn(context, feedUri, dbType, json, raw);
                     }
                 }).create();
     }
 }
