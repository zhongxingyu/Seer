 package org.tamanegi.quicksharemail.service;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.Charset;
 import java.util.Date;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.mail.MessagingException;
 import javax.mail.util.ByteArrayDataSource;
 
 import org.apache.http.Header;
 import org.apache.http.HeaderElement;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.ProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.client.DefaultRedirectHandler;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HttpContext;
 import org.tamanegi.quicksharemail.R;
 import org.tamanegi.quicksharemail.content.MessageContent;
 import org.tamanegi.quicksharemail.content.MessageDB;
 import org.tamanegi.quicksharemail.content.SendSetting;
 import org.tamanegi.quicksharemail.mail.MailComposer;
 import org.tamanegi.quicksharemail.mail.UriDataSource;
 import org.tamanegi.quicksharemail.receiver.NetworkStateChangeReceiver;
 import org.tamanegi.quicksharemail.receiver.RetryAlarmReceiver;
 import org.tamanegi.quicksharemail.ui.ConfigSendActivity;
 import org.tamanegi.util.StringCustomFormatter;
 
 import android.app.AlarmManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.Uri;
 import android.os.Build;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.os.SystemClock;
 import android.widget.Toast;
 
 public class SenderService extends Service
 {
     public static final String ACTION_ENQUEUE =
         "org.tamanegi.quicksharemail.intent.action.ENQUEUE";
     public static final String ACTION_RETRY =
         "org.tamanegi.quicksharemail.intent.action.RETRY";
     public static final String ACTION_DELETE_ALL =
         "org.tamanegi.quicksharemail.intent.action.DELETE_ALL";
     public static final String ACTION_COMPLETE =
         "org.tamanegi.quicksharemail.intent.action.COMPLETE";
     public static final String ACTION_SHOW_TOAST =
         "org.tamanegi.quicksharemail.intent.action.SHOW_TOAST";
 
     public static final String EXTRA_ACTION = "action";
     public static final String EXTRA_DATA = "data";
     public static final String EXTRA_TYPE = "type";
     public static final String EXTRA_CATEGORIES = "categories";
     public static final String EXTRA_EXTRAS = "extras";
     public static final String EXTRA_SUBJECT_FORMAT = "subjectFormat";
     public static final String EXTRA_ADDRESS = "address";
 
     public static final String EXTRA_MSG_STRING = "notifyMsg";
     public static final String EXTRA_MSG_DURATION = "notifyDuration";
 
     private static final int NOTIFY_ID = 0;
 
     private static final int REQUEST_TYPE_START = 0;
     private static final int REQUEST_TYPE_STOP = 1;
     private static final int REQUEST_TYPE_ENQUEUE = 2;
     private static final int REQUEST_TYPE_RETRY = 3;
     private static final int REQUEST_TYPE_DELETE_ALL = 4;
 
     private static final int SNIP_LENGTH = 40;
 
     private static final Pattern URL_PATTERN =
        Pattern.compile("https?://[\\p{Alnum}-_.!~*'();\\/?:@=+$,%&#]*");
     private static final String EXTRACT_SEP = "\n-> ";
 
     private static final int RETRIEVE_CONTENT_SIZE = 1024 * 32;
     private static final Pattern RETRIEVE_CONTENT_META_PATTERN =
         Pattern.compile(
             "<meta(\\s[^>]*)?>", Pattern.CASE_INSENSITIVE);
     private static final Pattern RETRIEVE_CONTENT_HTTPEQ_PATTERN =
         Pattern.compile(
             "http-equiv=\"?content-type\"?", Pattern.CASE_INSENSITIVE);
     private static final Pattern RETRIEVE_CONTENT_CTYPE_PATTERN =
         Pattern.compile(
             "content=(?:\"([^\"]*)\"|([^\\s]*))", Pattern.CASE_INSENSITIVE);
     private static final Pattern RETRIEVE_CONTENT_TITLE_PATTERN =
         Pattern.compile(
             "<title[^>]*>\\s*(.*?)\\s*</title[^>]*>",
             Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
     private String http_user_agent = null;
 
     private int req_cnt = 0;
     private int notif_cnt = 0;
 
     private volatile boolean is_running = false;
     private Thread main_thread = null;
     private LinkedBlockingQueue<Object> queue;
 
     private SendSetting setting;
     private MessageDB message_db;
 
     private PowerManager.WakeLock wakelock;
 
     @Override
     public IBinder onBind(Intent intent)
     {
         return null;
     }
 
     @Override
     public void onCreate()
     {
         super.onCreate();
 
         // prepare data
         setting = new SendSetting(this);
         message_db = new MessageDB(this);
 
         PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
         wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                                   "QuickShareMail");
 
         // prepare thread
         is_running = true;
         main_thread = new Thread(new Runnable() {
                 public void run() {
                     mainLoop();
                 }
             });
         queue = new LinkedBlockingQueue<Object>();
 
         main_thread.start();
 
         // for service restart time
         req_cnt = pushRequest(REQUEST_TYPE_START);
     }
 
     @Override
     public void onDestroy()
     {
         try {
             is_running = false;
             pushRequest(REQUEST_TYPE_STOP);
 
             main_thread.join();
         }
         catch(InterruptedException e) {
             e.printStackTrace();
         }
 
         message_db.close();
 
         super.onDestroy();
     }
 
     @Override
     public void onStart(Intent intent, int startId)
     {
         req_cnt += processRequest(intent);
 
         if(req_cnt <= 0) {
             boolean need_retry = (message_db.getRetryCount() > 0);
 
             // prepare network state receiver
             getPackageManager().setComponentEnabledSetting(
                 new ComponentName(getApplicationContext(),
                                   NetworkStateChangeReceiver.class),
                 (need_retry ?
                  PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                  PackageManager.COMPONENT_ENABLED_STATE_DISABLED),
                 PackageManager.DONT_KILL_APP);
 
             // prepare retry alarm
             Intent alerm_intent = new Intent(
                 getApplicationContext(), RetryAlarmReceiver.class);
             PendingIntent alerm_pending_intent =
                 PendingIntent.getBroadcast(this, 0, alerm_intent, 0);
             AlarmManager mgr =
                 (AlarmManager)getSystemService(Context.ALARM_SERVICE);
             if(need_retry) {
                 mgr.setInexactRepeating(
                     AlarmManager.ELAPSED_REALTIME_WAKEUP,
                     SystemClock.elapsedRealtime() + (30 * 60 * 1000),
                     AlarmManager.INTERVAL_HALF_HOUR,
                     alerm_pending_intent);
             }
             else {
                 mgr.cancel(alerm_pending_intent);
             }
 
             // stop service
             stopSelfResult(startId);
             req_cnt = 0;
         }
     }
 
     private int pushRequest(int req_type)
     {
         try {
             queue.put(new Integer(req_type));
             return 1;
         }
         catch(InterruptedException e) {
             e.printStackTrace();
             Toast.makeText(getApplicationContext(),
                            R.string.msg_fail_request,
                            Toast.LENGTH_LONG)
                 .show();
 
             return 0;
         }
     }
 
     private int popRequest() throws InterruptedException
     {
         return ((Integer)queue.take()).intValue();
     }
 
     private void mainLoop()
     {
         try {
             while(is_running) {
                 int req = popRequest();
                 if(! is_running) {
                     return;
                 }
 
                 switch(req) {
                 case REQUEST_TYPE_STOP:
                     return;
 
                 case REQUEST_TYPE_RETRY:
                     clearRetryFlag();
                     break;
 
                 case REQUEST_TYPE_DELETE_ALL:
                     deleteAllMessage();
                     break;
                 }
 
                 wakelock.acquire();
                 try {
                     while(is_running) {
                         // show remaining
                         updateRemainNotification();
 
                         // send message, until message exists
                         if(! sendMessage()) {
                             break;
                         }
                     }
                 }
                 finally {
                     wakelock.release();
                 }
 
                 if(! is_running) {
                     return;
                 }
 
                 // show remaining
                 updateRemainNotification();
 
                 // complete
                 startService(
                     new Intent(SenderService.ACTION_COMPLETE, null,
                                getApplicationContext(),
                                SenderService.class));
             }
         }
         catch(Exception e) {
             e.printStackTrace();
         }
     }
 
     private int processRequest(Intent intent)
     {
         if(intent == null) {
             return 0;
         }
 
         String action = intent.getAction();
 
         if(ACTION_ENQUEUE.equals(action)) {
             return pushRequest(REQUEST_TYPE_ENQUEUE);
         }
         else if(ACTION_RETRY.equals(action)) {
             return pushRequest(REQUEST_TYPE_RETRY);
         }
         else if(ACTION_DELETE_ALL.equals(action)) {
             return pushRequest(REQUEST_TYPE_DELETE_ALL);
         }
         else if(ACTION_SHOW_TOAST.equals(action)) {
             Toast.makeText(getApplicationContext(),
                            intent.getStringExtra(EXTRA_MSG_STRING),
                            intent.getIntExtra(EXTRA_MSG_DURATION,
                                               Toast.LENGTH_LONG)).
                 show();
             return 0;
         }
         else if(ACTION_COMPLETE.equals(action)) {
             return -1;
         }
         else {
             return 0;
         }
     }
 
     private boolean sendMessage()
     {
         // get message
         MessageContent msg = message_db.popFront();
         if(msg == null) {
             return false;
         }
 
         // send mail
         if(msg.getAddressCount() > 0) {
             send(msg);
         }
 
         // delete processed field
         message_db.delete(msg);
 
         // check invalid address
         checkInvalidAddress(msg);
 
         return true;
     }
 
     private void clearRetryFlag()
     {
         // clear retry-later flag
         message_db.clearRetryFlag();
     }
 
     private void deleteAllMessage()
     {
         message_db.deleteAllMessage();
     }
 
     private void send(MessageContent msg)
     {
         try {
             String type = msg.getType();
             String body = msg.getText();
             String snip_body = snipBody(body);
             String stream = msg.getStream();
             UriDataSource attach_src = null;
             ByteArrayDataSource link_info_src = null;
 
             // for attachment
             if(stream != null) {
                 Uri uri = Uri.parse(stream);
                 attach_src = new UriDataSource(getContentResolver(), uri);
             }
             String filename =
                 (attach_src != null ? attach_src.getName() : null);
 
             // subject
             StringCustomFormatter formatter =
                 createFormatter(msg.getId(),
                                 snip_body, filename,
                                 msg.getDate());
             String subject = formatter.format(msg.getSubjectFormat());
 
             // body
             if(body == null) {
                 body = (attach_src != null ?
                         formatter.format(msg.getBodyFormat()) : "");
                 type = "text/plain";
             }
             else {
                 // link info
                 String link_info = retrieveLinkInfo(body);
                 if(link_info != null && link_info.length() > 0) {
                     body = body + "\n";
                     link_info_src =
                         new ByteArrayDataSource(link_info, "text/plain");
                 }
             }
 
             // smtp settings
             String server = setting.getSmtpServer();
             String port = String.valueOf(setting.getSmtpPort());
             String sec_str = setting.getSmtpSec();
             int sec_type;
             if(sec_str.equals("ssl")) {
                 sec_type = MailComposer.SmtpConfig.SECURITY_TYPE_SSL;
             }
             else if(sec_str.equals("starttls")) {
                 sec_type = MailComposer.SmtpConfig.SECURITY_TYPE_STARTTLS;
             }
             else {
                 sec_type = MailComposer.SmtpConfig.SECURITY_TYPE_NONE;
             }
 
             MailComposer.SmtpConfig smtp =
                 new MailComposer.SmtpConfig(server, port, sec_type);
 
             if(setting.getSmtpAuth()) {
                 smtp.setAuth(setting.getSmtpUser(), setting.getSmtpPass());
             }
 
             // mail content
             MailComposer.MailConfig mail =
                 new MailComposer.MailConfig(setting.getMailFrom(),
                                             msg.getAddressInfo(),
                                             subject,
                                             msg.getDate());
 
             mail.setBody(new ByteArrayDataSource(body, type));
             if(link_info_src != null) {
                 mail.appendPart(link_info_src);
             }
             if(attach_src != null) {
                 mail.appendPart(attach_src);
             }
 
             // send mail
             MailComposer composer = new MailComposer(smtp, mail);
             composer.send();
         }
         catch(MessagingException e) {
             e.printStackTrace();
             // todo: err msg
             showWarnToast(getString(R.string.msg_fail_send, e.getMessage()));
             return;
         }
         catch(SecurityException e) {
             e.printStackTrace();
             // todo: err msg
             showWarnToast(
                 getString(R.string.msg_fail_send,
                           getString(R.string.msg_fail_send_security)));
             return;
         }
         catch(Exception e) {
             e.printStackTrace();
             // todo: err msg
             showWarnToast(getString(R.string.msg_fail_send, e.getMessage()));
             return;
         }
     }
 
     private StringCustomFormatter createFormatter(long id,
                                                   String snip_body,
                                                   String filename,
                                                   Date date)
     {
         return new StringCustomFormatter(
             new StringCustomFormatter.IdValue[] {
                 new StringCustomFormatter.IdValue('i', String.valueOf(id)),
                 new StringCustomFormatter.IdValue('s', snip_body),
                 new StringCustomFormatter.IdValue('f', filename),
                 new StringCustomFormatter.IdValue(
                     't', (snip_body != null ? snip_body :
                           filename != null ? filename : "")),
                 new StringCustomFormatter.IdValue(
                     'T', String.format("%tT", date)),
                 new StringCustomFormatter.IdValue(
                     'F', String.format("%tF", date)),
                 new StringCustomFormatter.IdValue(
                     'D', String.format("%tF", date)),
             });
     }
 
     private void checkInvalidAddress(MessageContent msg)
     {
         StringBuilder invalid = new StringBuilder();
         String sep = getString(R.string.address_separator);
 
         int invalid_cnt = 0;
         for(int i = 0; i < msg.getAddressCount(); i++) {
             MessageContent.AddressInfo addr = msg.getAddressInfo(i);
 
             if(addr.isProcessed() && (! addr.isValid())) {
                 if(invalid_cnt > 0) {
                     invalid.append(sep);
                 }
                 invalid.append(addr.getAddress());
                 invalid_cnt += 1;
             }
         }
 
         if(invalid_cnt > 0) {
             showWarnToast(getString(R.string.notify_invalid_addr, invalid));
         }
     }
 
     private String snipBody(String body)
     {
         if(body == null) {
             return null;
         }
 
         String nospbody = body.replaceAll("\\s+", " ");
         return (nospbody.length() <= SNIP_LENGTH ?
                 nospbody : nospbody.substring(0, SNIP_LENGTH - 3) + "...");
     }
 
     private String retrieveLinkInfo(String text)
     {
         // check config
         if(! (setting.isExpandUrl() || setting.isRetrieveTitle())) {
             return null;
         }
 
         final StringBuilder info = new StringBuilder();
         Matcher matcher = URL_PATTERN.matcher(text);
 
         // retrieve link info
         DefaultHttpClient http = new DefaultHttpClient();
         HttpProtocolParams.setUserAgent(http.getParams(), getHttpUserAgent());
 
         try {
             while(matcher.find()) {
                 final StringBuilder link_info = new StringBuilder();
                 http.setRedirectHandler(new DefaultRedirectHandler() {
                         public URI getLocationURI(
                             HttpResponse response, HttpContext context)
                             throws ProtocolException {
                             URI uri =
                                 super.getLocationURI(response, context);
                             if(setting.isExpandUrl()) {
                                 link_info.append(EXTRACT_SEP).append(uri);
                             }
 
                             updateRemainNotification();
                             return uri;
                         }
                     });
 
                 // extract link
                 String link = matcher.group();
 
                 // execute GET request
                 HttpResponse response = null;
                 try {
                     try {
                         response = http.execute(new HttpGet(link));
                     }
                     catch(IOException e) {
                         // just ignore
                         continue;
                     }
 
                     // retrieve HTML title
                     String title = getResponseTitle(response);
                     if(title != null) {
                         link_info.append(EXTRACT_SEP).append(title);
                     }
 
                     // check additional info exist?
                     if(link_info.length() == 0) {
                         continue;
                     }
 
                     // append info
                     info.append(info.length() != 0 ? "\n\n" : "\n")
                         .append(link)
                         .append(link_info);
                 }
                 finally {
                     try {
                         if(response != null) {
                             HttpEntity entity = response.getEntity();
                             if(entity != null) {
                                 entity.consumeContent();
                             }
                         }
                     }
                     catch(IOException e) {
                         // just ignore
                     }
                 }
             }
         }
         finally {
             http.getConnectionManager().shutdown();
             updateRemainNotification();
         }
 
         return info.toString();
     }
 
     private String getResponseTitle(HttpResponse response)
     {
         // check config
         if(! setting.isRetrieveTitle()) {
             return null;
         }
 
         HttpEntity entity = response.getEntity();
         if(entity == null) {
             return null;
         }
 
         // get charset from HTTP header
         Charset charset =
             getCharsetFromContentTypeHeader(entity.getContentType(),
                                             Charset.defaultCharset());
 
         // get content
         InputStream content;
         try {
             content = entity.getContent();
             if(content == null) {
                 return null;
             }
         }
         catch(IllegalStateException e) {
             e.printStackTrace();
             return null;
         }
         catch(IOException e) {
             e.printStackTrace();
             return null;
         }
 
         // get content bytes
         ByteBuffer buf = ByteBuffer.allocate(RETRIEVE_CONTENT_SIZE);
         byte[] buf_array = buf.array();
         try {
             while(buf.remaining() > 0) {
                 int len =
                     content.read(buf_array, buf.position(), buf.remaining());
                 if(len < 0) {
                     break;
                 }
 
                 buf.position(buf.position() + len);
             }
         }
         catch(IOException e) {
             // ignore
         }
         buf.limit(buf.position());
 
         // decode bytes by charset/encoding
         buf.rewind();
         CharBuffer charbuf = charset.decode(buf);
 
         Matcher meta_matcher = RETRIEVE_CONTENT_META_PATTERN.matcher(charbuf);
         while(meta_matcher.find()) {
             String meta = meta_matcher.group(1);
             if(meta == null ||
                ! RETRIEVE_CONTENT_HTTPEQ_PATTERN.matcher(meta).find()) {
                 continue;
             }
 
             Matcher ctype_matcher =
                 RETRIEVE_CONTENT_CTYPE_PATTERN.matcher(meta);
             if(! ctype_matcher.find()) {
                 continue;
             }
 
             String content_ctype_str = ctype_matcher.group(1);
             if(content_ctype_str == null || content_ctype_str.length() == 0) {
                 content_ctype_str = ctype_matcher.group(2);
             }
             if(content_ctype_str == null || content_ctype_str.length() == 0) {
                 break;
             }
 
             // get charset from content, and decode again
             Header content_ctype =
                 new BasicHeader("content-type", content_ctype_str);
             charset =
                 getCharsetFromContentTypeHeader(content_ctype, charset);
             buf.rewind();
             charbuf = charset.decode(buf);
             break;
         }
 
         // get title
         Matcher title_matcher = RETRIEVE_CONTENT_TITLE_PATTERN.matcher(charbuf);
         if(! title_matcher.find()) {
             return null;
         }
 
         String title = title_matcher.group(1);
         if(title == null || title.length() == 0) {
             return null;
         }
 
         return title.replaceAll("\\s+", " ");
     }
 
     private String getHttpUserAgent()
     {
         if(http_user_agent == null) {
             String pver = "unknown";
             try {
                 PackageInfo pinfo = getPackageManager().getPackageInfo(
                     getApplicationContext().getPackageName(), 0);
                 pver = pinfo.versionName;
             }
             catch(NameNotFoundException e) {
                 e.printStackTrace();
                 // ignore
             }
 
             http_user_agent =
                 getString(R.string.http_user_agent,
                           pver,
                           Build.VERSION.RELEASE,
                           Build.MODEL,
                           Build.ID);
         }
 
         return http_user_agent;
     }
 
     private Charset getCharsetFromContentTypeHeader(Header ctype, Charset def)
     {
         if(ctype != null) {
             HeaderElement[] elems = ctype.getElements();
             if(elems != null) {
                 for(int i = 0; i < elems.length; i++) {
                     NameValuePair param =
                         elems[i].getParameterByName("charset");
                     if(param != null) {
                         String charset_str = param.getValue();
                         try {
                             return Charset.forName(charset_str);
                         }
                         catch(Exception e) {
                             // ignore
                         }
                     }
                 }
             }
         }
 
         return def;
     }
 
     private void updateRemainNotification()
     {
         // remaining count
         if(setting.isShowProgressNotification()) {
             int rest_cnt = message_db.getRestCount();
             int retry_cnt = message_db.getRetryCount();
 
             if(rest_cnt + retry_cnt > 0) {
                 String msg;
                 int icon_level;
 
                 if(rest_cnt != 0) {
                     icon_level = (notif_cnt++) % 2 + 2;
                     msg = (retry_cnt != 0 ?
                            getString(
                                R.string.notify_remain, rest_cnt, retry_cnt) :
                            getString(
                                R.string.notify_remain_rest, rest_cnt));
                 }
                 else {
                     icon_level = 1;
                     msg = getString(R.string.notify_remain_retry, retry_cnt);
                 }
 
                 showNotification(R.drawable.status, icon_level,
                                  null,
                                  getString(R.string.app_name),
                                  msg,
                                  ConfigSendActivity.class,
                                  Notification.FLAG_ONGOING_EVENT);
             }
             else {
                 cancelNotification();
             }
         }
         else {
             cancelNotification();
         }
     }
 
     private void showNotification(int icon, int icon_level,
                                   CharSequence ticker_text,
                                   CharSequence content_title,
                                   CharSequence content_text,
                                   Class<?> activity_class,
                                   int flags)
     {
         long when = System.currentTimeMillis();
         Notification notify = new Notification(icon, ticker_text, when);
         notify.iconLevel = icon_level;
 
         Intent intent = new Intent(getApplicationContext(), activity_class);
         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         PendingIntent content_intent =
             PendingIntent.getActivity(this, 0, intent, 0);
         notify.setLatestEventInfo(getApplicationContext(),
                                   content_title,
                                   content_text,
                                   content_intent);
         notify.flags = flags;
 
         NotificationManager nm = (NotificationManager)
             getSystemService(Context.NOTIFICATION_SERVICE);
         nm.notify(NOTIFY_ID, notify);
     }
 
     private void cancelNotification()
     {
         NotificationManager nm = (NotificationManager)
             getSystemService(Context.NOTIFICATION_SERVICE);
         nm.cancel(NOTIFY_ID);
     }
 
     private void showWarnToast(String msg)
     {
         Intent intent = new Intent(SenderService.ACTION_SHOW_TOAST, null,
                                    getApplicationContext(),
                                    SenderService.class);
         intent.putExtra(EXTRA_MSG_STRING, msg);
         intent.putExtra(EXTRA_MSG_DURATION, Toast.LENGTH_LONG);
         startService(intent);
     }
 }
