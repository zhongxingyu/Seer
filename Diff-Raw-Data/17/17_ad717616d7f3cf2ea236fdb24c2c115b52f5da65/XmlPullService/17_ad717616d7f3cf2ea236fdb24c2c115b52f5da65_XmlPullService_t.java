 package edu.grinnell.sandb.xmlpull;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Calendar;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.app.AlarmManager;
 import android.app.IntentService;
 import android.app.PendingIntent;
 import android.app.PendingIntent.CanceledException;
 import android.content.Intent;
 import android.database.SQLException;
 import android.util.Log;
 import edu.grinnell.sandb.MainPrefs;
 import edu.grinnell.sandb.data.Article;
 import edu.grinnell.sandb.data.ArticleTable;
 import edu.grinnell.sandb.xmlpull.XmlCheckAgeTask.CheckAgeListener;
 import edu.grinnell.sandb.xmlpull.XmlFetchTask.Result;
 
 public class XmlPullService extends IntentService {
 
 	private static final String TAG = "XmlPullService";
 	
 	public static final String DOWNLOAD_FEED    = "edu.grinnell.sandb.xmlpull.DOWNLOAD_FEED";
 	public static final String CHECK_DOWNLOAD   = "edu.grinnell.sandb.xmlpull.CHECK_DOWNLOAD";
 	public static final String RESULT_ACTION    = "edu.grinnell.sandb.xmlpull.RESULT_ACTION";
 	public static final String LAST_CHECKED_MS  = "edu.grinnell.sandb.xmlpull.LAST_CHECKED_MS";
 	
 	public static final String FEED_URL = "http://www.thesandb.com/feed";
 	
 	private XmlFetchTask mWRT;
 	private XmlCheckAgeTask mCUAT;
 	private XmlParseTask mXPT;
 	private PendingIntent finished;
 	
 	private MainPrefs mPrefs;
 	
 	public XmlPullService() {
 		super(TAG);
 		mWRT = null;
 		mCUAT = null;
 		mXPT = null;
 		mPrefs = new MainPrefs(this);
 	}
 
 	@Override
 	protected void onHandleIntent(Intent i) {
 		String action = i.getAction();
 		Log.i(TAG, "Action: " + action);
 		
 		finished = i.getParcelableExtra(RESULT_ACTION);
 		if (DOWNLOAD_FEED.equals(action)) {
 			finished = i.getParcelableExtra(RESULT_ACTION);
 			downloadFeed();
 		} else if (CHECK_DOWNLOAD.equals(action)) {
 			finished = i.getParcelableExtra(RESULT_ACTION);
 			long lastCheckedMs = i.getLongExtra(LAST_CHECKED_MS, 0);
 			lastCheckedMs = mPrefs.lastUpdated;
 			checkAndDownload(lastCheckedMs);
 		} else {
 			Log.d(TAG, "Unknown Intent.  Action = " + action);
 		}
 	}
 	
 	private void downloadFeed() {
 //		if (mWRT == null) {
 //			Log.i(TAG, "Fetching feed from server..");
 //			mWRT = new XmlFetchTask(this, new WebRequstCallback());
 //			mWRT.execute(FEED_URL);
 //		}
 		
 		InputStream feed = XmlFetchTask.downloadDataFromServer(FEED_URL);
 		if (feed != null)
 			parseXmlFromStream(feed);
 		else {
 			Log.i("DataReceived", "stream is NULL!");
 			finishUp();
 		}
 	}
 	
 	private void checkAndDownload(long lastCheckedMs) {
 //		if (mCUAT == null) {
 //			Log.i(TAG, "Checking feed age..");
 //			mCUAT = new XmlCheckAgeTask(new CheckAndDownloadListener(lastCheckedMs));
 //			mCUAT.execute(FEED_URL);
 //		}
 		try {
 			long lastModifiedMs = XmlCheckAgeTask.lastModified(FEED_URL);
 			
 			Calendar lastUpdated = Calendar.getInstance();
 			lastUpdated.setTimeInMillis(lastModifiedMs);
 			Calendar lastChecked = Calendar.getInstance();
 			lastChecked.setTimeInMillis(lastCheckedMs);
 			Long now = Calendar.getInstance().getTimeInMillis();
 			mPrefs.setLastUpdated(now);
 			if (lastChecked.before(lastUpdated)) {
 				Log.i(TAG, "Feed out of date, updating..");
 				downloadFeed();
 			} else {
 				AlarmManager m = (AlarmManager) XmlPullService.this.getSystemService(ALARM_SERVICE);
 				Intent i = new Intent(CHECK_DOWNLOAD);
 				i.putExtra(LAST_CHECKED_MS, now);
 				PendingIntent pi = PendingIntent.getService(XmlPullService.this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
 				m.set(AlarmManager.RTC, AlarmManager.INTERVAL_DAY, pi);
				stopSelf();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void parseXmlFromStream(InputStream xmlStream) {
 //		if (mXPT == null) {
 //			mXPT = new XmlParseTask(this, new DataParsedCallback());
 //			mXPT.execute(xmlStream);
 //		}
 		
 		ArticleTable table = new ArticleTable(this);
 		
 		try {
 			table.open();
 			table.clearTable();
 			XmlParseTask.parseArticlesFromStream(xmlStream, this, table);
 		} catch (IOException ioe) {
 			Log.e(TAG, "parseArticlesFromStream", ioe);
 		} catch (XmlPullParserException xppe) {
 			Log.e(TAG, "parseArticlesFromStream", xppe);
 		} catch (SQLException sqle) {
 			Log.e(TAG, "SQLExeption", sqle);
 		} catch (Exception e) {
 			Log.e(TAG, "parseArticlesFromStream", e);
 		} finally {
 			table.close();
 			finishUp();
 		}
 	}
 	
 	private void finishUp() {
 		mPrefs.setFirstRun(false);
 		try {	
 			finished.send();
 		} catch (CanceledException e) {
 			Log.w(TAG, "Intent canceled by MainActivity..");
 		} finally {
 			stopSelf();
 		}
 	}
 	
 	private class WebRequstCallback implements XmlFetchTask.RetrieveDataListener {
 		
 		@Override
 		public void onRetrieveData(Result result) {
 			Log.i(TAG, "Xml loaded from server..");
 			InputStream s = result.getStream();
 
 			if (s != null)
 				parseXmlFromStream(result.getStream());
 			else {
 				Log.i("DataReceived", "stream is NULL!");
 				finishUp();
 			}
 			mWRT = null;
 		}
 	}
 	
 	private class DataParsedCallback implements XmlParseTask.ParseDataListener {
 
 		@Override
 		public void onDataParsed(List<Article> articles) {
 			Log.d("ParseDataListener", "Feed parsed!");
 			finishUp();
 		}	
 	}
 	
 	private class CheckAndDownloadListener implements CheckAgeListener {
 
 		private final long lastCheckedMs;
 		
 		public CheckAndDownloadListener(long lastCheckedMs) {
 			this.lastCheckedMs = lastCheckedMs;
 		}
 		
 		@Override
 		public void receive(Long result) {
 			Calendar lastUpdated = Calendar.getInstance();
 			lastUpdated.setTimeInMillis(result);
 			Calendar lastChecked = Calendar.getInstance();
 			lastChecked.setTimeInMillis(lastCheckedMs);
 			Long now = Calendar.getInstance().getTimeInMillis();
 			mPrefs.setLastUpdated(now);
 			if (lastChecked.before(lastUpdated)) {
 				Log.i(TAG, "Feed out of date, updating..");
 				downloadFeed();
 			} else {
 				AlarmManager m = (AlarmManager) XmlPullService.this.getSystemService(ALARM_SERVICE);
 				Intent i = new Intent(CHECK_DOWNLOAD);
 				i.putExtra(LAST_CHECKED_MS, now);
 				PendingIntent pi = PendingIntent.getService(XmlPullService.this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
 				m.set(AlarmManager.RTC, AlarmManager.INTERVAL_DAY, pi);
 				finishUp();
 			}
 		}
 	}
 
 }
