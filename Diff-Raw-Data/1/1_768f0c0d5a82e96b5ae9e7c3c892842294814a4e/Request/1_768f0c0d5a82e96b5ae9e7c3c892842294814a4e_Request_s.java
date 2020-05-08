 /*
  * Copyright (C) 2010 Takuo Kitame
  * Copyright (C) 2007-2010 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package jp.takuo.android.mmsreq;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.conn.params.ConnRouteParams;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.util.EntityUtils;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.util.Log;
 
 public class Request {
     private static final String LOG_TAG = "MmsReq";
     
     public static final int APN_PROFILE_SMILE = 0;
     public static final int APN_PROFILE_SBMMS = 1;
     public static final int APN_PROFILE_OPEN  = 2;
 
     // MMS Proxy server
     private static final String SMILE_PROXY = "smileweb.softbank.ne.jp";
     private static final String VFJP_PROXY  = "sbwapproxy.softbank.ne.jp";
     private static final String OPEN_PROXY  = "webopen.softbank.ne.jp";
     private static final String ANDRO_PROXY = "pband.softbank.ne.jp";
 
     // User Agent Strings
     private static final String SMILE_USER_AGENT = "smailhelp";
     private static final String VFJP_USER_AGENT  = "SoftBank/1.0/708SC/SCJ001 Browser/NetFront/3.3 Profile/MIDP-2.0 Configuration/CLDC-1.1";
     private static final String OPEN_USER_AGENT  = "Mozilla/5.0 (SymbianOS/9.2; U; Series60/3.1 NokiaN95/21.0.201 Profile/MIDP-2.0 Configuration/CLDC-1.1) AppleWebKit/413 (KHTML, like Gecko) Safari/413 Nokia/X02NK";
     private static final String ANDRO_USER_AGENT = "Mozilla/5.0 (Linux; U; Android 2.1-update1; ja-jp; HTCX06HT Build/ERE27) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17";
 
     private static final int APN_ALREADY_ACTIVE     = 0;
     private static final int APN_REQUEST_STARTED    = 1;
 
     // common
     private static final int    PROXY_PORT  = 8080;
     private static final String REQUEST_URL = "http://mail/cgi-ntif/mweb_ntif_res.cgi?jpn=1";
 
     private Context mContext;
     private String mProxyHost;
     private String mUserAgent;
     private ConnectivityManager mConnMgr;
 
     public Request(Context context, int type) {
     	mContext = context;
         switch (type) {
         case APN_PROFILE_SMILE: // smile.world
             mProxyHost = SMILE_PROXY;
             mUserAgent = SMILE_USER_AGENT;
             break;
         case APN_PROFILE_SBMMS: // vfjp
             mProxyHost = VFJP_PROXY;
             mUserAgent = VFJP_USER_AGENT;
             break;
         case APN_PROFILE_OPEN: // openmms
             mProxyHost = OPEN_PROXY;
             mUserAgent = OPEN_USER_AGENT;
             break;
         case APN_PROFILE_ANDRO: // andglobal
             mProxyHost = ANDRO_PROXY;
             mUserAgent = ANDRO_USER_AGENT;
             break;
         default:
             mProxyHost = null;
             mUserAgent = null;
         }
     }
 
     private int beginMmsConnectivity(ConnectivityManager ConnMgr) throws NoConnectivityException {
         Log.d(LOG_TAG, "startUsingNetworkFeature: MOBILE, enableMMS");
         int result = ConnMgr.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableMMS");
 
         Log.d(LOG_TAG, "beginMmsConnectivity: result=" + result);
 
         switch (result) {
             case APN_ALREADY_ACTIVE:
             case APN_REQUEST_STARTED:
                 return result;
         }
 
         throw new NoConnectivityException("Cannot establish MMS connectivity");
     }
 
     private void ensureRoute() throws NoRouteToHostException {
         int addr = lookupHost(mProxyHost);
         if (addr == -1) {
            throw new NoRouteToHostException("Cannot resolve host: " + mProxyHost);
         } else {
             if (!mConnMgr.requestRouteToHost(ConnectivityManager.TYPE_MOBILE_MMS, addr) )
                 throw new NoRouteToHostException("Cannot establish route to :" + addr);
         }
     }
 
     protected HttpResponse requestHttp() throws ClientProtocolException, IOException {
         HttpGet reqGet = new HttpGet(REQUEST_URL);
         DefaultHttpClient client = new DefaultHttpClient();
         HttpParams params = client.getParams();
         Log.d(LOG_TAG, "Proxy: "+mProxyHost + ":" + PROXY_PORT);
         ConnRouteParams.setDefaultProxy(params, new HttpHost(mProxyHost, PROXY_PORT));
         Log.d(LOG_TAG,"UserAgent: " + mUserAgent);
         HttpProtocolParams.setUserAgent(params, mUserAgent);
         reqGet.setParams(params);
         Log.d(LOG_TAG, "HTTP Get: " + REQUEST_URL);
         return (HttpResponse) client.execute(reqGet);
     }
 
     protected void getConnectivity() throws NoConnectivityException, ConnectTimeoutException, InterruptedException {
         mConnMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
         if (!mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS).isAvailable()) {
             throw new NoConnectivityException(mContext.getString(R.string.not_available));
           }
         int count = 0;
         int result = beginMmsConnectivity(mConnMgr);
         if (result != APN_ALREADY_ACTIVE) {
             NetworkInfo info = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
             while (!info.isConnected()) {
                 Thread.sleep(1500);
                 info = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
                 Log.d(LOG_TAG, "Waiting for CONNECTED: state=" + info.getState());
                 if(count++ > 5)
                     throw new ConnectTimeoutException(mContext.getString(R.string.failed_to_connect));
             }
         }
         Thread.sleep(1500); // wait for adding dns
     }
 
     // public delegation for ensureRoute()
     protected void tryConnect() throws NoRouteToHostException {
         ensureRoute();
     }
 
     protected String httpRequest() throws ClientProtocolException, IOException {
         String message;
         HttpResponse res = requestHttp();
         StatusLine status = res.getStatusLine();
         if (status.getStatusCode() != 200) {
             message = mContext.getString(R.string.error_from_server) + " HTTP:" + status.getStatusCode();
             Log.e(LOG_TAG, "HTTP Response: " + status.getStatusCode() + ", " + status.getReasonPhrase());
         } else {
             String body = EntityUtils.toString(res.getEntity());
             Matcher m = Pattern.compile("未読メッセージはありません。|\\d+件の受信通知の再送を受け付けました。").matcher(body);
             if (m.find()) {
                 message = m.group();
             } else {
                 message = mContext.getString(R.string.request_successed);
             }
             Log.d(LOG_TAG, "HTTP Response: 200, " + status.getReasonPhrase());
         }
         return message;
     }
 
     protected void disconnect() {
         Log.d(LOG_TAG, "stopUsingNetworkFeature: MOBILE, enableMMS");
         mConnMgr.stopUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableMMS");
     }
 
     public String requestMMS() {
         if (mProxyHost == null) return null;
         String message = null;
         try {
             getConnectivity();
             tryConnect();
             message = httpRequest();
         } catch (Request.NoConnectivityException e) {
             message = e.toString();
         } catch (Request.NoRouteToHostException e) {
             message = e.toString();
         } catch (Request.ConnectTimeoutException e) {
             message = e.toString();
         } catch (Exception e) {
             message = e.toString();
             e.printStackTrace();
         } finally {
             disconnect();
         }
         return "Mms: " + message;
     }
 
     /**
      * Look up a host name and return the result as an int. Works if the argument
      * is an IP address in dot notation. Obviously, this can only be used for IPv4
      * addresses.
      * @param hostname the name of the host (or the IP address)
      * @return the IP address as an {@code int} in network byte order
      */
     private static int lookupHost(String hostname) {
         InetAddress inetAddress;
         try {
             inetAddress = InetAddress.getByName(hostname);
         } catch (UnknownHostException e) {
             Log.d(LOG_TAG, "Failed to resolve address: " + hostname);
             return -1;
         }
         Log.d(LOG_TAG, "Resolved Address: " + inetAddress.toString());
         byte[] addrBytes;
         int addr;
         addrBytes = inetAddress.getAddress();
         addr = ((addrBytes[3] & 0xff) << 24)
                     | ((addrBytes[2] & 0xff) << 16)
                     | ((addrBytes[1] & 0xff) << 8)
                     |  (addrBytes[0] & 0xff);
         return addr;
     }
 
     public final class NoConnectivityException extends IOException {
         private static final long serialVersionUID = 1L;
         public NoConnectivityException() {
             super();
         }
         public NoConnectivityException(String msg) {
             super(msg);
         }
     }
 
     public final class ConnectTimeoutException extends IOException {
         private static final long serialVersionUID = 2L;
         public ConnectTimeoutException() {
             super();
         }
         public ConnectTimeoutException(String msg) {
             super(msg);
         }
     }
 
     public final class NoRouteToHostException extends IOException {
         private static final long serialVersionUID = 3L;
         public NoRouteToHostException() {
             super();
         }
         public NoRouteToHostException(String msg) {
             super(msg);
         }
     }
 }
