 package com.zhenguo_fu.exchangemonitor;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 import org.xmlpull.v1.XmlPullParserException;
 
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.media.RingtoneManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.NetworkInfo.State;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.IBinder;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 import android.widget.Toast;
 
 public class RateMonitorService extends Service {
 
     private static final String TEST_TAG = "ExchangeMonitor";
     private final IBinder mBinder = new LocalBinder();
     public static final String WIFI = "Wi-Fi";
     public static final String ANY = "Any";
     private static final String URL = "http://spreadsheets.google.com/feeds/list/0Av2v4lMxiJ1AdE9laEZJdzhmMzdmcW90VWNfUTYtM2c/2/public/basic?alt=rss";
     private static final CharSequence USELESS_OF_RATE = "_cpzh4: ";
     private static final int REFRESH_INTERVAL_MIN = ConfigInfo.TIME_MINITE_INT;
     private static final int REFRESH_INTERVAL_MAX = ConfigInfo.TIME_DAY_INT;
     // the dimension of notify ids should equal the value of
     // ConfigInfo.NUM_MAX_MONITOR
     private static final int[] NOTIFY_MEET_EXPECT_IDS = { 100, 101, 102, 103, 104 };
     private static final int REFRESH_INTERVAL_MOBILE = ConfigInfo.TIME_MINITE_INT * 30;
     private static final int REFRESH_INTERVAL_WIFI = ConfigInfo.TIME_MINITE_INT * 5;
     private static int mRefreshInterval = REFRESH_INTERVAL_MOBILE;
 
     private static String[] CURRENCY_CODES;
     private static String[] CURRENCY_DISP_CODES;
     private static Expect_Rate_Info[] mExpect_Rate_Infos = null;
     public static boolean refreshDisplay = true;
     public static String sPref = null;
 
     private ConnectionChangeReceiver mNetworkReceiver;
     private Object mLock;
 
     private static HashMap<String, Float> mRateMap = new HashMap<String, Float>();
 
     @Override
     public IBinder onBind(Intent arg0) {
         return mBinder;
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
         CURRENCY_CODES = getResources().getStringArray(R.array.CurrencyCodeArray);
         CURRENCY_DISP_CODES = getResources().getStringArray(R.array.CurrencyDispCodeArray);
         IntentFilter filter = new IntentFilter();
         mNetworkReceiver = new ConnectionChangeReceiver();
         filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
         refreshExpectInfos();
         startMonitor();
         registerReceiver(mNetworkReceiver, filter);
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         unregisterReceiver(mNetworkReceiver);
     }
 
     @Override
     public void onRebind(Intent intent) {
         super.onRebind(intent);
         refreshExpectInfos();
     }
 
     public void saveConf(Expect_Rate_Info info) {
         if (mExpect_Rate_Infos == null) {
             return;
         }
 
         mExpect_Rate_Infos[info.getId()] = info;
     }
 
     public float getRate(String baseCurCode, String targetCurCode) {
         float ret = 0f;
         Float baseRate = mRateMap.get(baseCurCode);
         Float targetRate = mRateMap.get(targetCurCode);
         ret = RateCalculator.CalculateRate(baseRate, targetRate);
         ret = ret <= 0f ? 0f : ret;
         return ret;
     }
 
     public void refreshData() {
         if (mLock != null) {
             synchronized (mLock) {
                 mLock.notify();
             }
         }
 
     }
 
     private InputStream downloadUrl(String urlString) throws IOException {
         URL url = new URL(urlString);
         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
         conn.setReadTimeout(10000);
         conn.setConnectTimeout(15000);
         conn.setRequestMethod("GET");
         conn.connect();
         return conn.getInputStream();
     }
 
     private void refreshExpectInfos() {
         mExpect_Rate_Infos = null;
         mExpect_Rate_Infos = RateMonitorActivity.loadExpectRates(RateMonitorService.this);
     }
 
     private void setRefreshInterval(int interval) {
         if (interval >= REFRESH_INTERVAL_MIN && interval <= REFRESH_INTERVAL_MAX) {
             mRefreshInterval = interval;
         }
     }
 
     private void updateRateMap(List<RssItem> items) {
         for (RssItem rssItem : items) {
             String rateStr = rssItem.getDescription();
             Float rateVal = 0f;
             rateStr = new String(rateStr.replace(USELESS_OF_RATE, ""));
             if (rateStr.length() > 0 && rateStr.charAt(0) >= '0' && rateStr.charAt(0) <= '9') {
                 rateVal = Float.valueOf(rateStr);
             } else {
                 rateVal = Float.valueOf(0f);
             }
 
             mRateMap.put(rssItem.getTitle(), rateVal);
         }
 
     }
 
     private void checkRate() {
         if (mExpect_Rate_Infos == null) {
             return;
         }
 
         for (int idx = 0; idx < mExpect_Rate_Infos.length; idx++) {
             Expect_Rate_Info info = mExpect_Rate_Infos[idx];
             int baseId = info.getBaseCurrencyId();
             int targetId = info.getTargetCurrencyId();
             if (baseId == ConfigInfo.INVALID_INDEX || targetId == ConfigInfo.INVALID_INDEX) {
                 continue;
             }
 
             String baseCode = CURRENCY_CODES[baseId];
             String targetCode = CURRENCY_CODES[targetId];
             float expectRate = info.getExpectValue();
             boolean isBelow = info.getIsBelow();
             if (mRateMap.containsKey(baseCode) && mRateMap.containsKey(targetCode)
                     && expectRate > 0f) {
                 float currentRate = getRate(baseCode, targetCode);
                 if ((currentRate > 0f)
                         && ((currentRate == expectRate)
                                 || (currentRate < expectRate && isBelow == true) || (currentRate > expectRate && isBelow == false))) {
                     Intent intent = new Intent(this, RateMonitorActivity.class);
                     intent.putExtra(ConfigInfo.IDX_RATE_LIST, idx);
                     PendingIntent pendIntent = PendingIntent.getActivity(this, 0, intent, 0);
                     String nofifyString = getString(R.string.notify_text_meet_expectation) + " 1 "
                             + CURRENCY_DISP_CODES[baseId] + " : "
                             + String.format("%.3f", currentRate) + " "
                             + CURRENCY_DISP_CODES[targetId];
                     Notification notification = new NotificationCompat.Builder(this)
                             .setContentTitle(getText(R.string.notify_title_meet_expectation))
                             .setContentText(nofifyString).setSmallIcon(R.drawable.app_icon)
                             .setContentIntent(pendIntent).build();
                     notification.sound = RingtoneManager
                             .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                     notification.flags |= Notification.FLAG_AUTO_CANCEL
                             | Notification.FLAG_SHOW_LIGHTS;
                     NotificationManager notifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                     notifyManager.notify(NOTIFY_MEET_EXPECT_IDS[info.getId()], notification);
                 }
             }
         }
     }
 
     private synchronized void loadData() {
         InputStream inputStream = null;
         try {
             inputStream = downloadUrl(URL);
             if (inputStream == null) {
                 return;
             }
 
         } catch (IOException e) {
             showToast(R.string.connection_error);
             return;
         }
 
         try {
             RssReader rssReader = new RssReader();
 
             List<RssItem> rssItems = rssReader.getItems(inputStream);
             updateRateMap(rssItems);
         } catch (XmlPullParserException e) {
             Log.d(TEST_TAG, getResources().getString(R.string.xml_error));
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private void showToast(final int resId) {
         Handler handler = new Handler(getMainLooper());
         handler.post(new Runnable() {
 
             @Override
             public void run() {
                 Toast toast = Toast.makeText(RateMonitorService.this, resId, Toast.LENGTH_LONG);
                 toast.show();
             }
         });
     }
 
     private void startMonitor() {
         mLock = new Object();
         Thread refreshThread = new Thread(new Runnable() {
 
             @Override
             public void run() {
                 Intent refrshIntent = new Intent();
                 refrshIntent.setAction(ConfigInfo.ACTION_REFRESH);
                 synchronized (mLock) {
                     while (true) {
                         loadData();
                         checkRate();
                         sendBroadcast(refrshIntent);
                         try {
                             mLock.wait(mRefreshInterval);
                             // Thread.sleep(mRefreshInterval);
                         } catch (InterruptedException e) {
                             e.printStackTrace();
                         }
                     }
                 }
 
             }
         });
         refreshThread.start();
     }
 
     public class LocalBinder extends Binder {
         RateMonitorService getService() {
             return RateMonitorService.this;
         }
     }
 
     public static class RateCalculator {
         public static float CalculateRate(Float baseValue, Float targetValue) {
             if (baseValue == null || targetValue == null || baseValue.floatValue() == 0f
                     || targetValue.floatValue() == 0f) {
                 return 0f;
             }
 
             float retValue = targetValue.floatValue() / baseValue.floatValue();
             return retValue;
         }
     }
 
     public class ConnectionChangeReceiver extends BroadcastReceiver {
         @Override
         public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            State mobileState = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                    .getState();
            State wifiState = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                    .getState();

            if (mobileState == NetworkInfo.State.CONNECTED
                    || mobileState == NetworkInfo.State.CONNECTING) {
                 setRefreshInterval(REFRESH_INTERVAL_MOBILE);
            } else if (wifiState == NetworkInfo.State.CONNECTED
                    || wifiState == NetworkInfo.State.CONNECTING) {
                 setRefreshInterval(REFRESH_INTERVAL_WIFI);
             } else {
                 setRefreshInterval(REFRESH_INTERVAL_MAX);
             }
         }
     }
 
     public class RssItem {
         // item title
         private String title;
         // item description
         private String description;
 
         public String getTitle() {
             return title;
         }
 
         public void setTitle(String title) {
             this.title = title;
         }
 
         public String getDescription() {
             return description;
         }
 
         public void setDescription(String description) {
             this.description = description;
         }
 
         @Override
         public String toString() {
             return "\nTitle is " + title + " Decription is " + description;
         }
     }
 
     public class RssParentHandler extends DefaultHandler {
         private static final String ITEM = "item";
         private static final String TITLE = "title";
         private static final String DESCRIPTION = "description";
 
         private List<RssItem> rssItems;
         private RssItem currentItem;
         private boolean parsingTitle;
         private boolean parsingDescription;
 
         public RssParentHandler() {
             rssItems = new ArrayList<RssItem>();
         }
 
         public List<RssItem> getItems() {
             return rssItems;
         }
 
         public void startElement(String uri, String localName, String qName, Attributes attributes)
                 throws SAXException {
             if (ITEM.equals(qName)) {
                 currentItem = new RssItem();
             } else if (TITLE.equals(qName)) {
                 parsingTitle = true;
             } else if (DESCRIPTION.equals(qName)) {
                 parsingDescription = true;
             }
         }
 
         public void endElement(String uri, String localName, String qName) throws SAXException {
             if (ITEM.equals(qName)) {
                 rssItems.add(currentItem);
                 currentItem = null;
             } else if (TITLE.equals(qName)) {
                 parsingTitle = false;
             } else if (DESCRIPTION.equals(qName)) {
                 parsingDescription = false;
             }
         }
 
         public void characters(char[] ch, int start, int length) throws SAXException {
             if (currentItem == null) {
                 return;
             }
 
             if (parsingTitle) {
                 currentItem.setTitle(new String(ch, start, length));
             } else if (parsingDescription) {
                 currentItem.setDescription(new String(ch, start, length));
                 parsingDescription = false;
             }
         }
     }
 
     public class RssReader {
         private String rssUrl;
         RssParentHandler mHandler;
         SAXParser mSaxParser;
 
         public RssReader(String rssUrl) {
             this.rssUrl = rssUrl;
         }
 
         public RssReader() throws ParserConfigurationException, SAXException {
             SAXParserFactory factory = SAXParserFactory.newInstance();
             mSaxParser = factory.newSAXParser();
             mHandler = new RssParentHandler();
         }
 
         public List<RssItem> getItems() throws SAXException, IOException {
             mSaxParser.parse(rssUrl, mHandler);
             return mHandler.getItems();
         }
 
         public List<RssItem> getItems(InputStream inputStream) throws Exception {
             mSaxParser.parse(inputStream, mHandler);
             return mHandler.getItems();
         }
     }
 
 }
