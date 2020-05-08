 package com.tools.tvguide.managers;
 
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.UnknownHostException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.tools.tvguide.utils.NetDataGetter;
 import com.tools.tvguide.utils.Utility;
 
 import android.content.Context;
 
 public class DnsManager 
 {
     private Context mContext;
     private String  mIpAddress;
     
     public DnsManager(Context context)
     {
         mContext = context;
     }
     
     public String getIPAddress(String hostName) throws UnknownHostException
     {
         InetAddress addr = null;
         if (mIpAddress != null)
             return mIpAddress;
         
         String ipAddress = getIPFrom_chinaz(hostName);
         if (ipAddress != null)
         {
             mIpAddress = ipAddress;
             return ipAddress;
         }
         
         addr = InetAddress.getByName (hostName);
         ipAddress = addr.getHostAddress();
         mIpAddress = ipAddress;
         return ipAddress;
     }
     
     private String getIPFrom_chinaz(final String hostName)
     {
         String ipAddress = null;
         String url = UrlManager.URL_CHINAZ_IP + "?IP=" + hostName;
         NetDataGetter getter;
         try 
         {
             getter = new NetDataGetter(url);
             String html = getter.getStringData();
             Pattern resultPattern = Pattern.compile("查询结果\\[1\\](.+)</");
             Matcher resultMatcher = resultPattern.matcher(html);
             
             if (resultMatcher.find())
             {
                 String result = resultMatcher.group();
                 Pattern ipPattern = Pattern.compile("((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]\\d|\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]\\d|[1-9])");
                 Matcher ipMatcher = ipPattern.matcher(result);
                 if (ipMatcher.find()) 
                 {
                     ipAddress = ipMatcher.group();
                 }
             }
         }
         catch (MalformedURLException e) 
         {
             e.printStackTrace();
         }
         
         if (!Utility.isIPAddress(ipAddress))
             return null;
         return ipAddress;
     }
 }
