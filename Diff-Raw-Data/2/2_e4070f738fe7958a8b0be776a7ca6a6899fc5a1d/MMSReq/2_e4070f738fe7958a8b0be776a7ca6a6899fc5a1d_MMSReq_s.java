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
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.util.Log;
 import android.preference.PreferenceManager;
 
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.conn.params.ConnRouteParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.util.EntityUtils;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import java.io.IOException;
 import java.util.Date;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import android.os.AsyncTask;
 import android.app.ProgressDialog;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.*;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class MMSReq extends Activity {
     private static final String LOG_TAG = "MMSReq";
 
     private static final String SMILE_PROXY = "smileweb.softbank.ne.jp";
     private static final String SBMMS_PROXY = "sbwapproxy.softbank.ne.jp";
     private static final int    PROXY_PORT  = 8080;
     private static final String REQUEST_URL = "http://mail/cgi-ntif/mweb_ntif_res.cgi?jpn=1";
     private static final String SMILE_USER_AGENT = "smailhelp";
     private static final String SBMMS_USER_AGENT = "SoftBank/1.0/708SC/SCJ001 Browser/NetFront/3.3 Profile/MIDP-2.0 Configuration/CLDC-1.1";
     private static final String SBMMS_USER = "softbank";
    private static final String SBMMS_PASS = "qceffknarlurqgb";
 
     private static final int APN_ALREADY_ACTIVE     = 0;
     private static final int APN_REQUEST_STARTED    = 1;
     // private static final int APN_TYPE_NOT_AVAILABLE = 2;
     // private static final int APN_REQUEST_FAILED     = 3;
 
     private static final String PREFS_MMS_TYPE = "mms_type";
 
     private static ProgressDialog mProgressDialog;
     private static TextView mTextResult;
     private static Spinner mSpinnerMMSType;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         mSpinnerMMSType = (Spinner) findViewById(R.id.spinner_type);
         mTextResult = (TextView) findViewById(R.id.t_result);
         Button b = (Button) findViewById(R.id.b_request);
         b.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 AsyncRequest req = new AsyncRequest();
                 switch (mSpinnerMMSType.getSelectedItemPosition()) {
                 case 0: // smile.world
                     req.setProxy(SMILE_PROXY, null, null);
                     req.setUserAgent(SMILE_USER_AGENT);
                     break;
                 case 1: // sbmms
                     req.setProxy(SBMMS_PROXY, SBMMS_USER, SBMMS_PASS);
                     req.setUserAgent(SBMMS_USER_AGENT);
                     break;
                 default:
                   }
                 req.execute();
             }
          });
         mSpinnerMMSType.setSelection(PreferenceManager
                                      .getDefaultSharedPreferences(getApplicationContext())
                                      .getInt(PREFS_MMS_TYPE, 0));
         mSpinnerMMSType.setOnItemSelectedListener(new OnItemSelectedListener() {
              @Override
              public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                Spinner spinner = (Spinner) parent;
                int val = (int)spinner.getSelectedItemPosition();
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                                 .edit().putInt(PREFS_MMS_TYPE, val).commit();
               }
              @Override
              public void onNothingSelected(AdapterView<?> parent) {}
         });
     }
 
     class ClickListener implements OnClickListener {
         public void onClick(View v) {
             AsyncRequest req = new AsyncRequest();
             switch (mSpinnerMMSType.getSelectedItemPosition()) {
             case 0: // smile.world
                 req.setProxy(SMILE_PROXY, null, null);
                 req.setUserAgent(SMILE_USER_AGENT);
                 break;
             case 1: // sbmms
                 req.setProxy(SBMMS_PROXY, SBMMS_USER, SBMMS_PASS);
                 req.setUserAgent(SBMMS_USER_AGENT);
                 break;
             default:
               }
             req.execute();
         }
     }
 
     // Background Task class
     class AsyncRequest extends AsyncTask<Void, String, String> {
         private String mProxyHost = null;
         private String mProxyUser = null;
         private String mProxyPass = null;
         private String mUserAgent = null;
 
         public AsyncRequest() {
              mProgressDialog = new ProgressDialog(MMSReq.this);
              mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
         }
 
         @Override
         protected String doInBackground(Void... params) {
             return requestMMS();
         }
 
         public void setProxy(String host, String user, String pass) {
             mProxyHost = host;
             mProxyUser = user;
             mProxyPass = pass;
         }
 
         public void setUserAgent(String agent) {
             mUserAgent = agent;
         }
 
         protected void onPostExecute(String result) {
             Log.d(LOG_TAG, "onPostExecute");
             super.onPostExecute(result);
             if(mProgressDialog != null &&
                 mProgressDialog.isShowing()){
                 mProgressDialog.dismiss();
                 mProgressDialog = null;
               }
             DateFormat df = new SimpleDateFormat("MM/dd HH:mm:ss");
             mTextResult.setText(result + "\n" + df.format(new Date()));
         }
 
         protected void onPreExecute() {
             super.onPreExecute();
             mTextResult.setText("");
             mProgressDialog.setIndeterminate(true);
             mProgressDialog.setTitle(R.string.start_request);
             mProgressDialog.show();
         }
 
         protected void onProgressUpdate(String... progress) {
             mProgressDialog.setMessage(progress[0]);
         }
 
         protected int beginMmsConnectivity(ConnectivityManager ConnMgr) throws IOException {
             Log.d(LOG_TAG, "startUsingNetworkFeature: MOBILE, enableMMS");
             int result = ConnMgr.startUsingNetworkFeature(
             ConnectivityManager.TYPE_MOBILE, "enableMMS");
 
             Log.d(LOG_TAG, "beginMmsConnectivity: result=" + result);
 
             switch (result) {
                 case APN_ALREADY_ACTIVE:
                 case APN_REQUEST_STARTED:
                     return result;
             }
 
             throw new IOException("Cannot establish MMS connectivity");
         }
 
         protected void ensureRoute(ConnectivityManager ConnMgr) throws IOException {
             int addr = lookupHost(mProxyHost);
             if (addr == -1) {
                throw new IOException("Cannot resolve host: " + mProxyHost);
             } else {
                 // FIXME: should be ConnectivityManager.TYPE_MOBILE_MMS
                 if (!ConnMgr.requestRouteToHost(2, addr) )
                     throw new IOException("Cannot establish route to :" + addr);
             }
         }
 
         protected HttpResponse requestHttp() throws ClientProtocolException, IOException {
             HttpGet reqGet = new HttpGet(REQUEST_URL);
             DefaultHttpClient client = new DefaultHttpClient();
             HttpParams params = client.getParams();
             ConnRouteParams.setDefaultProxy(params, new HttpHost(mProxyHost, PROXY_PORT));
             if (mProxyUser != null && mProxyPass != null) {
                 client.getCredentialsProvider().setCredentials(
                     new AuthScope(mProxyHost, PROXY_PORT),
                     new UsernamePasswordCredentials(mProxyUser, mProxyPass));
              }
             HttpProtocolParams.setUserAgent(params, mUserAgent);
             reqGet.setParams(params);
             return (HttpResponse) client.execute(reqGet);
         }
 
         protected String requestMMS() {
             ConnectivityManager ConnMgr;
             Context ctx;
             ctx = getApplicationContext();
             ConnMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
             String message = null;
             try {
                 if (!ConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable()) {
                     message = getString(R.string.not_available);
                     throw new IOException("Cannot establish Network for MMS Request");
                   }
                 publishProgress(getString(R.string.connect_to_mobile));
                 while(true) {
                     int result = beginMmsConnectivity(ConnMgr);
                     if (result != APN_ALREADY_ACTIVE) {
                         Log.d(LOG_TAG, "Extending MMS connectivity returned " +  result +
                                 " instead of APN_ALREADY_ACTIVE, waiting for ready");
                         // Just wait for connectivity startup without
                         // any new request of APN switch.
                         Thread.sleep(1500);
                     } else {
                         break;
                        }
                   }
                 publishProgress(getString(R.string.connect_to_server));
                 Thread.sleep(500);
                 ensureRoute(ConnMgr);
                 publishProgress(getString(R.string.request_to_server));
                 HttpResponse res = requestHttp();
                 StatusLine status = res.getStatusLine();
                 if (status.getStatusCode() != 200) {
                     message = getString(R.string.error_from_server) + " HTTP:" + status.getStatusCode();
                     Log.e(LOG_TAG, "HTTP Response: " + status.getStatusCode() + ", " + status.getReasonPhrase());
                 } else {
                     message = getString(R.string.request_successed);
                     Log.d(LOG_TAG, "HTTP Response: 200, " + status.getReasonPhrase());
                     String body = EntityUtils.toString(res.getEntity());
                     Matcher m = Pattern.compile("未読メッセージはありません。|\\d+件の受信通知の再送を受け付けました。").matcher(body);
                     if (m.find()) {
                            message = m.group();
                     }
                 }
             } catch (Exception e) {
                 if (message == null)
                     message = getString(R.string.failed_to_connect);
                 Log.e(LOG_TAG, e.toString());
             } finally {
                 Log.d(LOG_TAG, "stopUsingNetworkFeature: MOBILE, enableMMS");
                 ConnMgr.stopUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableMMS");
               }
             return message;
         }
     } /* AsyncRequest */
 
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
 }
