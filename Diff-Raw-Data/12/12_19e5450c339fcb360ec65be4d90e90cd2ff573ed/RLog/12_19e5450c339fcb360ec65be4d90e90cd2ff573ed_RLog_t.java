 package com.scurab.android.rlw;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.text.TextUtils;
 import android.view.View;
 import com.scurab.android.KnowsActiveActivity;
 import com.scurab.gwt.rlw.shared.model.LogItem;
 import com.scurab.gwt.rlw.shared.model.LogItemBlobRequest;
 
 import java.io.ByteArrayOutputStream;
 import java.lang.reflect.Method;
 
 public class RLog {
 
     private static boolean sLocalMode = false;
 
     private static final String ERROR2 = "Error";
 
     private static final String SEPARATOR = "|";
 
     /**
      * Completely turn off remote logging<br/>
      * You should always have at least {@value #EXCEPTION} for getting uncaught
      * exceptions!
      */
     public static final int TURN_OFF = 0;
 
     /**
      * Allow logging via {@link RLog#i(Object, String)} and
      * {@link RLog#i(Object, String, String)}
      */
     public static final int INFO = 1;
 
     /**
      * Allow logging via {@link RLog#v(Object, String)} and
      * {@link RLog#v(Object, String, String)}
      */
     public static final int VERBOSE = 1 << 1;
 
     /**
      * Allow logging via {@link RLog#d(Object, String)} and
      * {@link RLog#d(Object, String, String)}
      */
     public static final int DEBUG = 1 << 2;
 
     /**
      * Allow logging via {@link RLog#w(Object, String)} and
      * {@link RLog#w(Object, String, String)}
      */
     public static final int WARNING = 1 << 3;
 
     /**
      * Allow logging via {@link RLog#e(Object, String)} and
      * {@link RLog#e(Object, String, String)} and
      * {@link RLog#e(Object, String, Throwable)}
      */
     public static final int ERROR = 1 << 4;
 
     /**
      * Allow logging any uncaught exception<br/>
      * {@link RemoteLog#catchUncaughtErrors(Thread)} This value is default log
      * mode
      */
     public static final int EXCEPTION = 1 << 5;
 
     /**
      * Allow logging via {@link RLog#wtf(Object, String)} and
      * {@link RLog#wtf(Object, String, String)}
      */
     public static final int WTF = 1 << 6;
 
     /**
      * Allow logging via <br/>
      * {@link RLog#takeScreenshot(Object, String, Activity)}<br/>
      * {@link RLog#takeScreenshot(Object, String, KnowsActiveActivity)}<br/>
      * {@link RLog#takeScreenshot(Object, String, View)}<br/>
      */
     public static final int SCREENSHOT = 1 << 7;
 
     /**
      * Allow any log way
      */
     public static final int ALL = INFO | VERBOSE | DEBUG | WARNING | ERROR
             | EXCEPTION | WTF | SCREENSHOT;
 
     private static int sMode = EXCEPTION;
 
     private static ILog sLog = null;
 
     /**
      * Used for PushNotifiaction, this particular category of logging doesn't
      * have specific mode value. Log item is not sent only if mode is
      * {@value #TURN_OFF}
      *
      * @param source
      * @param category
      * @param msg
      */
     public static void n(Object source, String category, String msg) {
         if (sMode != TURN_OFF) {
             send(source, category, msg);
         }
         if (sLog != null) {
             sLog.d(source, category, msg);
         }
     }
 
     public static void i(Object source, String msg) {
         i(source, "Info", msg);
 
     }
 
     public static void i(Object source, String category, String msg) {
         if ((sMode & INFO) == INFO) {
             send(source, category, msg);
         }
         if (sLog != null) {
             sLog.i(source, category, msg);
         }
     }
 
     public static void v(Object source, String msg) {
         v(source, "Verbose", msg);
     }
 
     public static void v(Object source, String category, String msg) {
         if ((sMode & VERBOSE) == VERBOSE) {
             send(source, category, msg);
         }
         if (sLog != null) {
             sLog.v(source, category, msg);
         }
     }
 
     public static void d(Object source, String msg) {
         d(source, "Debug", msg);
     }
 
     public static void d(Object source, String category, String msg) {
         if ((sMode & DEBUG) == DEBUG) {
             send(source, category, msg);
         }
         if (sLog != null) {
             sLog.d(source, category, msg);
         }
     }
 
     public static void e(Object source, String msg) {
         e(source, ERROR2, msg);
     }
 
     public static void e(Object source, String category, String msg) {
         if ((sMode & ERROR) == ERROR) {
             send(source, category, msg);
         }
         if (sLog != null) {
             sLog.e(source, category, msg);
         }
     }
 
     public static void e(Object source, Throwable t) {
         if ((sMode & ERROR) == ERROR) {
             e(source, ERROR2, "[" + t.getClass().getSimpleName() + "]", t);
         }
     }
 
     public static void e(Object source, String message, Throwable t) {
         e(source, ERROR2, message, t);
     }
 
     public static void e(Object source, String category, String message, Throwable t) {
         if ((sMode & ERROR) == ERROR) {
             send(source, category, TextUtils.isEmpty(message) ? getMessageOrClassName(t) : message,
                     new LogItemBlobRequest("text/plain", "error.txt", RemoteLog
                             .getStackTrace(t).getBytes()));
         }
         if (sLog != null) {
             sLog.e(source, category, t);
         }
     }
 
     private static String getMessageOrClassName(Throwable t) {
         String s = t.getMessage();
         if (!(s != null && s.length() > 0)) {
             s = t.getClass().getSimpleName();
         }
         return s;
     }
 
     public static void w(Object source, String msg) {
         w(source, "Warning", msg);
     }
 
     public static void w(Object source, String category, String msg) {
         if ((sMode & WARNING) == WARNING) {
             send(source, category, msg);
         }
         if (sLog != null) {
             sLog.w(source, category, msg);
         }
     }
 
     public static void wtf(Object source, String msg) {
         wtf(source, "WTF", msg);
     }
 
     public static void wtf(Object source, String category, String msg) {
         if ((sMode & WTF) == WTF) {
             send(source, category, msg);
         }
         if (sLog != null) {
             sLog.wtf(source, category, msg);
         }
     }
 
     /**
      * Save screenshot of app<br/>
      * Application must implement {@link KnowsActiveActivity}
      *
      * @param source
      * @param msg
      * @parem c
      */
     public static Bitmap takeScreenshot(Object source, String msg,
                                       KnowsActiveActivity c) {
         Bitmap b = null;
         try {
             Activity a = c.getCurrentActivity();
             if (a != null) {
                 View v = a.getWindow().getDecorView();
                 b = takeScreenshot(source, msg, v);
             } else {
                 send(source, "Screenshot", "Current Activity is null\n" + msg);
             }
         } catch (Exception e) {
             e.printStackTrace();
             e(RLog.class,
                     String.format("%s\n%s", e.getMessage(),
                             RemoteLog.getStackTrace(e, null)));
         }
         return b;
     }
 
     /**
      * Save screenshot of Activity
      *
      * @param source
      * @param msg
      * @param act
      */
     public static Bitmap takeScreenshot(Object source, String msg, Activity act) {
         return takeScreenshot(source, msg, act.getWindow().getDecorView());
     }
 
     /**
      * Save screenshot of view
      *
      * @param source
      * @param msg
      * @param view
      * @return Bitmap of screenshot, null if any problem or mode for screenshot is disabled
      */
     public static Bitmap takeScreenshot(Object source, String msg, View view) {
         if (sMode == TURN_OFF || (sMode & SCREENSHOT) != SCREENSHOT) {
             return null;
         }
         Bitmap b = null;
         try {
             // prepare view
             view.destroyDrawingCache();
             view.buildDrawingCache(false);
             // get bitmap
             b = view.getDrawingCache();
             final ByteArrayOutputStream baos = new ByteArrayOutputStream();
 
             Exception e = null;
             boolean saved = false;
             // save it
             try {
                 saved = b.compress(Bitmap.CompressFormat.JPEG, 85, baos);
             } catch (Exception ex) {
                 e = ex;
             }
             // create blob item
             LogItemBlobRequest libr = null;
             if (saved) {
                 libr = new LogItemBlobRequest("image/jpeg", String.format(
                         "%s.jpg", System.currentTimeMillis()),
                         baos.toByteArray());
 
             } else {
                 libr = new LogItemBlobRequest("text/plain", String.format(
                         "%s.txt", System.currentTimeMillis()), e.getMessage()
                         .getBytes());
             }
             view.destroyDrawingCache();
             // send it
             send(source, "Screenshot", msg, libr);
         } catch (Exception e) {
             e.printStackTrace();
             e(RLog.class, String.format("takeScreenshot\n%s\n%s",
                     e.getMessage(), RemoteLog.getStackTrace(e, null)));
         }
         return b;
     }
 
     /**
      * Send custom log item
      *
      * @param source
      * @param category
      * @param msg
      */
     public static void send(Object source, String category, String msg) {
         send(source, category, msg, null);
     }
 
     /**
      * Send custim log item with blob
      *
      * @param source
      * @param category
      * @param msg
      * @param libr
      */
     public static void send(Object source, String category, String msg,
                             LogItemBlobRequest libr) {
         if(sLocalMode || sMode == TURN_OFF){
             return;
         }
 
         LogItem li = RemoteLog.createLogItem();
         if (li == null) {// not yet initialized
             return;
         }
         li.setCategory(category);
         li.setMessage(msg);
         li.setSource(getObjectName(source));
         LogSender ls = RemoteLog.getLogSender(true);
         if (ls != null) {
             ls.addLogItem(li, libr);
         }
     }
 
     private static String getObjectName(Object source) {
        String value = null;
         if (source != null) {
            value = source.getClass().getSimpleName();
            if (!(value != null && value.length() > 0)) {
                 //anon class
                 Class c = source.getClass();
                 Class encc = c.getEnclosingClass();
                 Method m = c.getEnclosingMethod();
 
                 Class<?>[] paramTypes = m.getParameterTypes();
                 StringBuilder sb = new StringBuilder();
                 for (Class clz : paramTypes) {
                     sb.append(clz.getSimpleName()).append(", ");
                 }
 
                 if (sb.length() > 0) {
                     sb.setLength(sb.length() - 2);
                 }
 
                value = String.format("%s.this.%s(%s)$AnonymousClass", encc.getSimpleName(), m.getName(), sb.toString());
             }
         }
        return value;
     }
 
     public static int getMode() {
         return sMode;
     }
 
     /**
      * Enable one particular log mode, see {@link RLog} for mode constants
      *
      * @param sMode
      */
     public static void addMode(int sMode) {
         RLog.sMode |= sMode;
     }
 
     /**
      * Set exactly modes defined by input param
      *
      * @param sMode
      */
     public static void setMode(int sMode) {
         RLog.sMode = sMode;
     }
 
     /**
      * Set local log handler<br/>
      * Methods will be called always, regardless on mode<br/>
      * For mutliple log instances use Decorator pattenr :]
      *
      * @param sLog
      */
     public static void setLog(ILog sLog) {
         RLog.sLog = sLog;
     }
 
     /**
      * @param values
      * @return -1 if nothing interesting in string found
      */
     protected static int getMode(String values) {
         int result = 0;
         boolean found = false;
         values = values.trim();
         if (!TextUtils.isEmpty(values)) {
             try {
                 int v = Integer.parseInt(values);
                 found = (v <= ALL && v >= TURN_OFF);
                 if (found) {
                     result = v;
                     return result;
                 }
             } catch (Exception e) {
                 // ignore parseInt exception
             }
             int subvalue = 0;
             // array
             if (values.contains(SEPARATOR)) {
                 String[] vs = values.split("\\" + SEPARATOR);
                 for (String v : vs) {
                     subvalue = getModeValue(v);
                     if (subvalue != -1) {
                         found = true;
                         result |= subvalue;
                     }
                 }
             } else {// single value
                 subvalue = getModeValue(values);
                 if (subvalue != -1) {
                     found = true;
                     result = subvalue;
                 }
             }
         }
         return found ? result : -1;
     }
 
     protected static int getModeValue(String value) {
         int result = -1;
         if ("TURN_OFF".equals(value)) {
             return TURN_OFF;
         } else if ("INFO".equals(value)) {
             return INFO;
         } else if ("VERBOSE".equals(value)) {
             return VERBOSE;
         } else if ("DEBUG".equals(value)) {
             return DEBUG;
         } else if ("WARNING".equals(value)) {
             return TURN_OFF;
         } else if ("ERROR".equals(value)) {
             return ERROR;
         } else if ("EXCEPTION".equals(value)) {
             return EXCEPTION;
         } else if ("WTF".equals(value)) {
             return WTF;
         } else if ("SCREENSHOT".equals(value)) {
             return SCREENSHOT;
         } else if ("ALL".equals(value)) {
             return ALL;
         }
         return result;
     }
 
     /**
      * Set local mode to avoid sending logitems to server
      * @param localmode true to not send items to server
      */
     public static void setLocalMode(boolean localmode){
         sLocalMode = localmode;
     }
 }
