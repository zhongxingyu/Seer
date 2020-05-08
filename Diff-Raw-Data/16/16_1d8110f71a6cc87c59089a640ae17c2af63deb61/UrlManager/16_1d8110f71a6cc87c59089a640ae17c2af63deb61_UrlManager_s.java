 package com.tools.tvguide.managers;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.acra.ACRA;
 
 import android.content.Context;
 
 public class UrlManager 
 {
     public static final String TAG                      = "UrlManager";
     public static final String URL_CHINAZ_IP            = "http://ip.chinaz.com";
     public static final String URL_IPCN                 = "http://www.ip.cn";
     public static final String URL_PUB_HOT              = "http://m.tvsou.com/juqing.asp";
     public static final String DEV_IP                   = "192.168.1.100";
     public static final String REAL_HOST                = "bigeyecow.oicp.net";
     
     public static final String ACRA_PROXY_REAL          = "http://" + REAL_HOST + ":59840/acra-tvguide/_design/acra-storage/_update/report";
     public static final String ACRA_PROXY_DEV           = "http://" + DEV_IP + ":5984/acra-tvguide/_design/acra-storage/_update/report";
     
     public static final int URL_CATEGORIES              = 1;
     public static final int URL_CHANNELS                = 2;
     public static final int URL_ON_PLAYING_PROGRAM      = 3;
     public static final int URL_ON_PLAYING_PROGRAMS     = 4;
     public static final int URL_CHOOSE                  = 5;
     public static final int URL_SEARCH                  = 6;
     public static final int URL_HOT                     = 7;
     public static final int URL_UPDATE                  = 8;
     public static final int URL_FEEDBACK                = 9;
     public static final int URL_LOGIN                   = 10;
     public static final int URL_REPORT                  = 11;
     public static final int URL_QUERY                   = 12;
     public static final int URL_LOGOUT                  = 13;
         
     private static final boolean ENABLE_TEST            = EnvironmentManager.isDevelopMode;
     private String  mProxyHostName                      = REAL_HOST;
     private String  mProxyHostIP;
     private String  BASE_PATH;
     private int     PORT;
     
     private static final String PATH_CATEGORIES           = "/public/json/categories.php";
     private static final String PATH_CHANNELS             = "/public/json/channels.php";
     private static final String PATH_ON_PLAYING_PROGRAM   = "/public/json/onplaying_program.php";
     private static final String PATH_ON_PLAYING_PROGRAMS  = "/public/json/onplaying_programs.php";
     private static final String PATH_CHOOSE               = "/public/json/programs.php";
     private static final String PATH_SEARCH               = "/public/json/search.php";
     private static final String PATH_HOT                  = "/public/json/hot.php";
     private static final String PATH_QUERY                = "/public/json/query.php";
     
     private static final String PATH_UPDATE               = "/public/update/update.php";
     private static final String PATH_FEEDBACK             = "/public/feedback.php";
     private static final String PATH_LOGIN                = "/public/login.php";
     private static final String PATH_REPORT               = "/public/report.php";
     private static final String PATH_LOGOUT               = "/public/logout.php";
     
     private Context mContext;
     private ReentrantLock mLock                           = new ReentrantLock();
     private Condition mCondition;
     private boolean mHasInit                              = false;
     public interface OnInitCompleteCallback
     {
         void OnInitComplete(int result);
     }
     
     public UrlManager(Context context)
     {
         mContext = context;
         mCondition = mLock.newCondition();
         
         if (ENABLE_TEST)
         {
             BASE_PATH = "/projects/TV";
             PORT = 80;
         }
         else
         {
             BASE_PATH = "/projects/TV";
             PORT = 52719;
         }
     }
     
     public void init(final OnInitCompleteCallback callback)
     {
         if (mProxyHostIP != null)
         {
             if (callback != null)
             {
                 callback.OnInitComplete(0);
             }
             mHasInit = true;
             return;
         }
         
         new Thread() 
         {        
             @Override
             public void run() 
             {
                 try 
                 {
                    mProxyHostIP = AppEngine.getInstance().getDnsManager().getProxyIPAddress(mProxyHostName);
                     if (ENABLE_TEST)
                         mProxyHostIP = DEV_IP;
                     if (EnvironmentManager.enableACRA)
                     	ACRA.getConfig().setFormUri(tryToReplaceHostNameWithIP(ACRA.getConfig().formUri()));
                     
                     mLock.lock();
                     mCondition.signalAll();
                     mLock.unlock();
                     mHasInit = true;
                     if (callback != null)
                     {
                         callback.OnInitComplete(0);
                     }
                     AppEngine.getInstance().getDnsManager().preloadFrequentlyUsedDns();
                 } 
                 catch (UnknownHostException e) 
                 {
                 }
             }
         }.start();
     }
     
     public String tryToGetDnsedUrl(int type)
     {
         if (mProxyHostIP != null || mHasInit)
             return getUrl(type);
         
         // 等待HostIP解析完毕
         try 
         {
             mLock.lock();
             mCondition.await(20000, TimeUnit.MILLISECONDS);
         }
         catch (InterruptedException e) 
         {
             e.printStackTrace();
         }
         finally
         {
             mLock.unlock();
         }
         return getUrl(type);
     }
     
     public String getUrl(int type)
     {
         String url = "http://";
        if (mProxyHostIP != null)
             url += mProxyHostIP;
         else
             url += mProxyHostName;
         
         url += ":" + PORT;
         url += BASE_PATH;
         switch (type)
         {
             case URL_CATEGORIES:
                 url += PATH_CATEGORIES;
                 break;
             case URL_CHANNELS:
                 url += PATH_CHANNELS;
                 break;
             case URL_ON_PLAYING_PROGRAM:
                 url += PATH_ON_PLAYING_PROGRAM;
                 break;
             case URL_ON_PLAYING_PROGRAMS:
                 url += PATH_ON_PLAYING_PROGRAMS;
                 break;
             case URL_CHOOSE:
                 url += PATH_CHOOSE;
                 break;
             case URL_SEARCH:
                 url += PATH_SEARCH;
                 break;
             case URL_HOT:
                 url = URL_PUB_HOT;
                 break;
             case URL_UPDATE:
                 url += PATH_UPDATE;
                 break;
             case URL_FEEDBACK:
                 url += PATH_FEEDBACK;
                 break;
             case URL_LOGIN:
                 url += PATH_LOGIN;
                 break;
             case URL_REPORT:
                 url += PATH_REPORT;
                 break;
             case URL_QUERY:
                 url += PATH_QUERY;
                 break;
             case URL_LOGOUT:
                 url += PATH_LOGOUT;
             default:
                 assert false: "Not reach here";
                 break;
         }
         return url;
     }
     
     public String getHost()
     {
         if (mProxyHostIP != null)
             return mProxyHostIP;
         return mProxyHostName;
     }
     
     public int getPort()
     {
         return PORT;
     }
     
     public String getWebChannelUrl(String channelId, int day)
     {
         String tvmaoId = AppEngine.getInstance().getContentManager().getTvmaoId(channelId);
         
         if (tvmaoId == null)
             return null;
         
         // eg. http://www.tvmao.com/program/CCTV-CCTV1-w6.html
         String prefix = "http://www.tvmao.com/program/";
         String sufix = ".html";
         return prefix + tvmaoId + "-w" + String.valueOf(day) + sufix;
     }
     
     public static String tryToReplaceHostNameWithIP(String url)
     {
         if (url == null)
             return null;
         
         try 
         {
             String hostName = new URL(url).getHost();
             String ipAddress = AppEngine.getInstance().getDnsManager().getIpAddress(hostName);
             if (ENABLE_TEST)
                 ipAddress = DEV_IP;
             if (ipAddress != null)
                 return url.replace(hostName, ipAddress);
         } 
         catch (MalformedURLException e) 
         {
         } 
         catch (UnknownHostException e) 
         {
         }
         
         return url;
     }
 }
