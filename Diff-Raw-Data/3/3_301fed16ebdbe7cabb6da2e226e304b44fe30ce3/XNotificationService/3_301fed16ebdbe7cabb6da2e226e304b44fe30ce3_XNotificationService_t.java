 
 /*
  Copyright 2012-2013, Polyvi Inc. (http://polyvi.github.io/openxface)
  This program is distributed under the terms of the GNU General Public License.
 
  This file is part of xFace.
 
  xFace is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  xFace is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with xFace.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package com.polyvi.xface.extension.push;
 
 import java.util.List;
 import java.util.Properties;
 import java.util.Random;
 
 import org.jivesoftware.smack.NotificationIQ;
 import org.jivesoftware.smack.PacketListener;
 import org.jivesoftware.smack.packet.Packet;
 
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningTaskInfo;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.pm.ResolveInfo;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.IBinder;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 
 import com.polyvi.xface.event.XEvent;
 import com.polyvi.xface.event.XEventType;
 import com.polyvi.xface.event.XSystemEventCenter;
 import com.polyvi.xface.util.XConstant;
 
 /**
  * 该类继承与Service主要是读取配置文件,然后去连接服务器
  */
 public class XNotificationService extends Service {
 
     /** 通知显示的action名 */
     private static final String ACTION_SHOW_NOTIFICATION = "com.polyvi.push.SHOW_NOTIFICATION";
 
     /** 通知被点击的action名 */
     private static final String ACTION_NOTIFICATION_CLICKED = "com.polyvi.push.NOTIFICATION_CLICKED";
 
     /** 通知被取消的action名 */
     private static final String ACTION_NOTIFICATION_CLEARED = "com.polyvi.push.NOTIFICATION_CLEARED";
 
     /** 通知的id名 */
     private static final String NOTIFICATION_ID = "id";
 
     /** 通知的apikey名 */
     private static final String NOTIFICATION_API_KEY = "apiKey";
 
     /** 通知的title名 */
     private static final String NOTIFICATION_TITLE = "title";
 
     /** 通知的内容名 */
     private static final String NOTIFICATION_MESSAGE = "message";
 
     /** 通知的uri名 */
     private static final String NOTIFICATION_URI = "uri";
 
     /** 配置文件中存储登陆服务器主机的名字 */
     private static final String HOST = "host";
 
     /** 配置文件中存储登陆服务器端口的名字 */
     private static final String PORT = "port";
 
     /** 配置文件中存储是否开启push的名字 */
     private static final String IS_OPEN_PUSH = "isOpenPush";
 
     /** 配置文件存储的名字 */
     private static final String FILE_NAME = "push";
 
     /** 配置文件所在工程的目录名字 */
     private static final String FILE_DIRECTORY = "raw";
 
     /** 电话状态管理 */
     private TelephonyManager mTelephonyManager;
 
     /** 收到通知广播 */
     private BroadcastReceiver mNotificationReceiver = null;
 
     /** 网络状态改变的广播 */
     private BroadcastReceiver mConnectivityReceiver = null;
 
     /** 电话状态改变监听 */
     private PhoneStateListener mPhoneStateListener = null;
 
     /** push的连接管理 */
     private XConnectionManager mConnectionManager;
 
     /** 收到从服务器发过来通知请求的监听，主要实现发送通知广播 */
     private PacketListener mNotificationPacketListener;
 
     /** 通知管理 */
     private NotificationManager mNotificationManager;
 
     private Properties mProps;
 
     private static String mPackageName;
 
     private static String mHost;
 
     private static String mPort;
 
     public XNotificationService() {
     }
 
     @Override
     public void onCreate() {
         init();
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         if (null == mPackageName) {
             mPackageName = intent.getStringExtra(XServiceManager.PACKAGE_NAME);
             mHost = intent.getStringExtra(XServiceManager.HOST);
             mPort = intent.getStringExtra(XServiceManager.PORT);
         }
         return Service.START_REDELIVER_INTENT;
     }
 
     /**
      * 主要实现一些初始化的操作
      */
     private void init() {
         mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
         mProps = openProperties(this);
         mConnectionManager = new XConnectionManager(this);
         registerNotificationReceiver();
         registerConnectivityReceiver();
         mConnectionManager.connect();
     }
 
     /**
      * 对电话状态监听，当手机网络从断开到连接的时候去连接服务器
      */
     private void genPhoneStateListener() {
         if (null == mPhoneStateListener) {
             mPhoneStateListener = new PhoneStateListener() {
                 @Override
                 public void onDataConnectionStateChanged(int state) {
                     super.onDataConnectionStateChanged(state);
                     if (state == TelephonyManager.DATA_CONNECTED) {
                         connect();
                     }
                 }
             };
         }
     }
 
     /**
      * 对接收到通知进行处理：1.显示通知；2.点击通知；3.清除通知
      */
     private void genNotificationReceiveBroadcastReceive() {
         if (null == mNotificationReceiver) {
             mNotificationReceiver = new BroadcastReceiver() {
                 @Override
                 public void onReceive(Context context, Intent intent) {
                     String action = intent.getAction();
                     if (ACTION_SHOW_NOTIFICATION.equals(action)) {
                         String notificationId = intent
                                 .getStringExtra(NOTIFICATION_ID);
                         String notificationApiKey = intent
                                 .getStringExtra(NOTIFICATION_API_KEY);
                         String notificationTitle = intent
                                 .getStringExtra(NOTIFICATION_TITLE);
                         String notificationMessage = intent
                                 .getStringExtra(NOTIFICATION_MESSAGE);
                         String notificationUri = intent
                                 .getStringExtra(NOTIFICATION_URI);
                         XNotificationService.this.notify(context,
                                 notificationId, notificationApiKey,
                                 notificationTitle, notificationMessage,
                                 notificationUri);
                     } else if (ACTION_NOTIFICATION_CLICKED.equals(action)) {
                         String notificationMessage = intent
                                 .getStringExtra(NOTIFICATION_MESSAGE);
                         if (!isAppRunning(mPackageName)) {
                             launchApp(mPackageName, notificationMessage);
                         } else {
                             XEvent evt = XEvent.createEvent(
                                     XEventType.PUSH_MSG_RECEIVED, notificationMessage);
                             XSystemEventCenter.getInstance().sendEventAsync(evt);
                         }
                     } else if (ACTION_NOTIFICATION_CLEARED.endsWith(action)) {
                         // TODO 用户点击清除通知
                     }
                 }
             };
         }
     }
 
     /**
      * 当网络改变的通知被触发后，执行的重连服务器或者断开的操作
      */
     private void genConnectionChangeBroadcastReceive() {
         if (null == mConnectivityReceiver) {
             mConnectivityReceiver = new BroadcastReceiver() {
                 @Override
                 public void onReceive(Context context, Intent intent) {
                     ConnectivityManager connectivityManager = (ConnectivityManager) context
                             .getSystemService(Context.CONNECTIVITY_SERVICE);
                     NetworkInfo networkInfo = connectivityManager
                             .getActiveNetworkInfo();
                     if (networkInfo != null) {
                         if (networkInfo.isConnected()) {
                             connect();
                         }
                     } else {
                         disconnect();
                     }
                 }
             };
         }
     }
 
     /**
      * 加载raw下的push.properties文件的配置资源
      *
      * @param context
      *            上下文
      * @return 属性
      */
     private Properties openProperties(Context context) {
         Properties props = new Properties();
         try {
             int id = context.getResources().getIdentifier(FILE_NAME,
                     FILE_DIRECTORY, context.getPackageName());
             props.load(context.getResources().openRawResource(id));
         } catch (Exception e) {
             e.getMessage();
         }
         return props;
     }
 
     @Override
     public void onDestroy() {
         unregisterNotificationReceiver();
         unregisterConnectivityReceiver();
         mConnectionManager.disconnect();
     }
 
     /**
      * 连接服务器
      */
     public void connect() {
         mConnectionManager.connect();
     }
 
     /**
      * 与服务器断开连接
      */
     public void disconnect() {
         mConnectionManager.disconnect();
     }
 
     /**
      * 注册通知广播
      */
     private void registerNotificationReceiver() {
         if (null == mNotificationReceiver) {
             genNotificationReceiveBroadcastReceive();
         }
         IntentFilter filter = new IntentFilter();
         filter.addAction(ACTION_SHOW_NOTIFICATION);
         filter.addAction(ACTION_NOTIFICATION_CLEARED);
         filter.addAction(ACTION_NOTIFICATION_CLICKED);
         registerReceiver(mNotificationReceiver, filter);
     }
 
     /**
      * 卸载通知广播
      */
     private void unregisterNotificationReceiver() {
         unregisterReceiver(mNotificationReceiver);
     }
 
     /**
      * 注册电话状态改变的广播
      */
     private void registerConnectivityReceiver() {
         genConnectionChangeBroadcastReceive();
         genPhoneStateListener();
         mTelephonyManager.listen(mPhoneStateListener,
                 PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
         IntentFilter filter = new IntentFilter();
         filter.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
         registerReceiver(mConnectivityReceiver, filter);
     }
 
     /**
      * 卸载电话状态改变的广播监听
      */
     private void unregisterConnectivityReceiver() {
         mTelephonyManager.listen(mPhoneStateListener,
                 PhoneStateListener.LISTEN_NONE);
         unregisterReceiver(mConnectivityReceiver);
     }
 
     /**
      * 获取连接服务器的主机地址
      *
      * @return 主机地址
      */
     public String getHost() {
         if(null != mHost) {
             return mHost;
         }
         String host = mProps.getProperty(HOST, "");
         return host;
     }
 
     /**
      * 获取连接服务器的端口号
      *
      * @return 端口号
      */
     public String getPort() {
         if(null != mPort) {
             return mPort;
         }
         String port = mProps.getProperty(PORT, "");
         return port;
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     /**
      * 从配置文件上面读取是否开启push
      *
      * @param context
      *            上下文
      * @return true:启动push,false:不启动push
      */
     public boolean isOpenPush(Context context) {
         Properties props = openProperties(context);
         return new Boolean(props.getProperty(IS_OPEN_PUSH, "")).booleanValue();
     }
 
     /**
      * 显示通知
      *
      * @param notificationId
      *            通知的id号
      * @param apiKey
      *            通知的apiKey
      * @param title
      *            通知的标题
      * @param message
      *            通知的内容
      * @param uri
      *            传递的uri
      */
     private void notify(Context context, String notificationId, String apiKey,
             String title, String message, String uri) {
 
         Notification notification = new Notification();
         // 设置通知的各种属性
         notification.defaults = Notification.DEFAULT_LIGHTS;
         notification.defaults |= Notification.DEFAULT_SOUND;
         notification.defaults |= Notification.DEFAULT_VIBRATE;
         notification.flags |= Notification.FLAG_AUTO_CANCEL;
         notification.when = System.currentTimeMillis();
         notification.tickerText = message;
 
         // 点击通知
         Intent clickIntent = new Intent(ACTION_NOTIFICATION_CLICKED);
         clickIntent.putExtra(NOTIFICATION_ID, notificationId);
         clickIntent.putExtra(NOTIFICATION_API_KEY, apiKey);
         clickIntent.putExtra(NOTIFICATION_TITLE, title);
         clickIntent.putExtra(NOTIFICATION_MESSAGE, message);
         clickIntent.putExtra(NOTIFICATION_URI, uri);
         PendingIntent clickPendingIntent = PendingIntent.getBroadcast(context,
                 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
         notification.setLatestEventInfo(context, title, message,
                 clickPendingIntent);
 
         // 清除通知
         Intent clearIntent = new Intent(ACTION_NOTIFICATION_CLEARED);
         clearIntent.putExtra(NOTIFICATION_ID, notificationId);
         clearIntent.putExtra(NOTIFICATION_API_KEY, apiKey);
         PendingIntent clearPendingIntent = PendingIntent.getBroadcast(context,
                 0, clearIntent, 0);
         notification.deleteIntent = clearPendingIntent;
         Random random = new Random(System.currentTimeMillis());
         mNotificationManager.notify(random.nextInt(), notification);
     }
 
     /**
      * 初始化对收到从服务器发过来通知请求的监听，主要实现获取服务器发过来的通知显示的信息以及发送通知广播
      */
     private void genNotificationPacketListener() {
         if (null == mNotificationPacketListener) {
             mNotificationPacketListener = new PacketListener() {
                 @Override
                 public void processPacket(Packet packet) {
                     if (packet instanceof NotificationIQ) {
                         NotificationIQ notification = (NotificationIQ) packet;
                         String notificationId = notification.getId();
                         String notificationApiKey = notification.getApiKey();
                         String notificationTitle = notification.getTitle();
                         String notificationMessage = notification.getMessage();
                         String notificationUri = notification.getUri();
 
                         Intent intent = new Intent(ACTION_SHOW_NOTIFICATION);
                         intent.putExtra(NOTIFICATION_ID, notificationId);
                         intent.putExtra(NOTIFICATION_API_KEY,
                                 notificationApiKey);
                         intent.putExtra(NOTIFICATION_TITLE, notificationTitle);
                         intent.putExtra(NOTIFICATION_MESSAGE,
                                 notificationMessage);
                         intent.putExtra(NOTIFICATION_URI, notificationUri);
                         sendBroadcast(intent);
                     }
                 }
 
             };
         }
     }
 
     /**
      * 获取服务器发过来通知请求的监听
      *
      * @return 通知请求监听
      */
     public PacketListener getNotificationPacketListener() {
         genNotificationPacketListener();
         return mNotificationPacketListener;
     }
 
     /**
      * 判断程序是否在运行(也就是说程序是否位于栈顶)
      *
      * @param packageName
      *            包名
      * @return true:程序正在运行,false:程序没有运行
      */
     private boolean isAppRunning(String packageName) {
         ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
         List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(100);
         if (tasksInfo.size() > 0) {
             // 应用程序位于堆栈的顶层
             if (packageName.equals(tasksInfo.get(0).topActivity
                     .getPackageName())) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * 启动程序,并将收到的通知内容传递的参数给程序
      *
      * @param packageName
      *            包名
      * @param appParamenter
      *            通知内容的参数
      */
     private void launchApp(String packageName, String appParamenter) {
         if (null == packageName) {
             return;
         }
 
         PackageInfo pi = null;
         try {
             pi = getPackageManager().getPackageInfo(packageName, 0);
             Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
             resolveIntent.setPackage(pi.packageName);
             PackageManager pm = getPackageManager();
             List<ResolveInfo> apps = pm.queryIntentActivities(resolveIntent, 0);
             if (null == apps || null == apps.iterator().next()) {
                 return;
             }
             ResolveInfo ri = apps.iterator().next();
             if (null == ri) {
                 return;
             }
             String className = ri.activityInfo.name;
             Intent intent = new Intent(Intent.ACTION_MAIN);
             intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                     | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
             ComponentName cn = new ComponentName(packageName, className);
             intent.setComponent(cn);
             intent.putExtra(XConstant.TAG_APP_START_PARAMS, appParamenter);
             startActivity(intent);
         } catch (NameNotFoundException e) {
         }
     }
 }
