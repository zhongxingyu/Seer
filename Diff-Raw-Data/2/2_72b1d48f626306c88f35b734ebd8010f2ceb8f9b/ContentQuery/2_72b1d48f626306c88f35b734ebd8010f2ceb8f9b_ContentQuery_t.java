 package net.anei.cadpage;
 
 import java.util.List;
 
 import android.app.ActivityManager;
 import android.content.ComponentName;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 
 public class ContentQuery {
   
   private static String[] MMS_COL_LIST = new String[]{"_ID", "sub"};
   private static String[] PART_COL_LIST = new String[]{"text", "_data"};
   
   public static void query(Context context) {
     retrieveMsg(context);
   }
   
   public static void dumpEverything(Context context) {
     
     ContentResolver res = context.getContentResolver();
     Uri uri  = Uri.parse("content://mms");
     Cursor cur = res.query(uri, null, null, null, null);
     dumpCursor("Full MMS", cur);
   }
   
   public static void retrieveMsg(Context context) {
     
     ContentResolver res = context.getContentResolver();
     int id = -1;
     String msg_id = "0ECCC4DDA63A0000D940000101";
     
     if (id < 0) {
       Uri uri  = Uri.parse("content://mms");
       Cursor cur = res.query(uri, MMS_COL_LIST, "tr_id=?", new String[]{msg_id}, null);
       if (cur == null) return;
       Log.w("rec count:" + cur.getCount());
       if (!cur.moveToFirst()) return;
       id = cur.getInt(0);
       String sub = cur.getString(1);
       Log.w("_ID=" + id + "   sub=" + sub);
     }
     
     Uri uri = Uri.parse("content://mms/"+id+"/part");
     Cursor cur = res.query(uri, PART_COL_LIST, null, null, null);
     dumpCursor("MMS part", cur);
     
     if (cur == null || !cur.moveToFirst()) return;
     String text = cur.getString(0);
     if (text == null) {
       byte[] ba = cur.getBlob(1);
       if (ba == null) return;
       text = new String(ba);
     }
     Log.w("Contents=" + text);
 //    
 //    InputStream is = null;
 //    ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
 //    try { 
 //      is = res.openInputStream(uri); 
 //  
 //      byte[] buffer = new byte[256]; 
 //      int len = is.read(buffer); 
 //      while (len >= 0) { 
 //      baos.write(buffer, 0, len); 
 //      len = is.read(buffer); 
 //      } 
 //    } catch (IOException ex) {
 //      throw new RuntimeException(ex.getMessage(), ex);
 //    } finally {
 //      if (is != null) try {is.close();} catch (IOException ex) {}
 //    }
 //    
 //    Log.w("Msg contents: " + baos.toString());
   }
   
   
   public static void dumpCursor(String title, Cursor cursor) {
     
     if (cursor == null) {
       Log.w(title + ": No Results returned");
       return;
     }
     
     Log.w(title + ": " + cursor.getCount() + " row returned");
     if (cursor.moveToFirst()) {
       do {
         Log.w("\n");
         for (int ndx = 0; ndx < cursor.getColumnCount(); ndx++) {
           Log.w("    " + cursor.getColumnName(ndx) + " = " + cursor.getString(ndx));
         }
       } while (cursor.moveToNext()); 
     }
     
     
   }
   
   public static void dumpRecentTasks(Context context) {
     ActivityManager mgr = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
     List<ActivityManager.RecentTaskInfo> taskList = mgr.getRecentTasks(100,ActivityManager.RECENT_WITH_EXCLUDED);
     Log.w("Recent task information");
     for (ActivityManager.RecentTaskInfo task : taskList) {
       Intent intent = task.baseIntent;
       dumpIntent(intent);
       Log.w("");
     }
   }
 
   public static void dumpIntent(Intent intent) {
     dumpIntent("", intent);
   }
   
   private static void dumpIntent(String prefix, Intent intent) {
     Log.v(prefix + "Flags:" + dumpFlags(intent.getFlags()));
     Log.v(prefix + "Action:" + intent.getAction());
     Log.v(prefix + "Categories:");
     if (intent.getCategories() != null) {
       for (String str : intent.getCategories()) Log.v(prefix + "  " + str);
     }
     Log.v(prefix + "Type:" + intent.getType());
     ComponentName comp = intent.getComponent();
     Log.v(prefix + "Comp:" + (comp == null ? null : intent.getComponent().getClassName()));
     Bundle extra = intent.getExtras();
     if (extra != null) {
       for (String key : extra.keySet()) {
         dumpKeyValue(prefix + "  ", key, extra.get(key));
       }
     }
   }
   
   public static void dumpBundle(Bundle bundle) {
     dumpBundle("", bundle);
   }
   
   private static void dumpBundle(String prefix, Bundle bundle) {
     for (String key : bundle.keySet()) {
       dumpKeyValue(prefix, key, bundle.getString(key));
     }
   }
   
   private static void dumpKeyValue(String prefix, String key, Object value) {
     Intent intent = (value instanceof Intent ? (Intent)value : null);
     Bundle bundle = (value instanceof Bundle ? (Bundle)value : null);
    String dispValue = (intent != null ? "Intent" : bundle != null ? "Bundle" : value == null ? "null" : value.toString());
     Log.v(prefix + key + ':' + dispValue);
     if (intent != null) dumpIntent(prefix+"  ", intent);
     else if (bundle != null) dumpBundle(prefix+"  ", bundle);
     
   }
   
   private static String dumpFlags(int flags) {
     StringBuilder sb = new StringBuilder();
     addFlag(sb, flags, Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT, "FLAG_ACTIVITY_BROUGHT_TO_FRONT");
     addFlag(sb, flags, Intent.FLAG_ACTIVITY_CLEAR_TOP, "FLAG_ACTIVITY_CLEAR_TOP");
     addFlag(sb, flags, Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET, "FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET");
     addFlag(sb, flags, Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS, "FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS");
     addFlag(sb, flags, Intent.FLAG_ACTIVITY_FORWARD_RESULT, "FLAG_ACTIVITY_FORWARD_RESULT");
     addFlag(sb, flags, Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY, "FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY");
     addFlag(sb, flags, Intent.FLAG_ACTIVITY_MULTIPLE_TASK, "FLAG_ACTIVITY_MULTIPLE_TASK");
     addFlag(sb, flags, Intent.FLAG_ACTIVITY_NEW_TASK, "FLAG_ACTIVITY_NEW_TASK");
     addFlag(sb, flags, Intent.FLAG_ACTIVITY_NO_ANIMATION, "FLAG_ACTIVITY_NO_ANIMATION");
     addFlag(sb, flags, Intent.FLAG_ACTIVITY_NO_HISTORY, "FLAG_ACTIVITY_NO_HISTORY");
     addFlag(sb, flags, Intent.FLAG_ACTIVITY_NO_USER_ACTION, "FLAG_ACTIVITY_NO_USER_ACTION");
     addFlag(sb, flags, Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP, "FLAG_ACTIVITY_PREVIOUS_IS_TOP");
     addFlag(sb, flags, Intent.FLAG_ACTIVITY_REORDER_TO_FRONT, "FLAG_ACTIVITY_REORDER_TO_FRONT");
     addFlag(sb, flags, Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED, "FLAG_ACTIVITY_RESET_TASK_IF_NEEDED");
     addFlag(sb, flags, Intent.FLAG_ACTIVITY_SINGLE_TOP, "FLAG_ACTIVITY_SINGLE_TOP");
     addFlag(sb, flags, Intent.FLAG_RECEIVER_REGISTERED_ONLY, "FLAG_RECEIVER_REGISTERED_ONLY");
     addFlag(sb, flags, Intent.FLAG_RECEIVER_REPLACE_PENDING, "FLAG_RECEIVER_REPLACE_PENDING");
     return sb.toString();
   }
   
   private static void addFlag(StringBuilder sb, int flags, int flag, String desc) {
     if ((flags & flag) != 0) {
       if (sb.length() > 0) sb.append(',');
       sb.append(desc);
     }
   }
 
 }
