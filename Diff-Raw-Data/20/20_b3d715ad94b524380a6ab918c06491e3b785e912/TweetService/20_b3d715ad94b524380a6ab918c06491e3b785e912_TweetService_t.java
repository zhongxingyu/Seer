 package org.mockup.wvuta;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import winterwell.jtwitter.Twitter;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.AsyncTask;
 import android.os.IBinder;
 import android.util.Log;
 
 public class TweetService extends Service {
 
 	private static final String TAG = "WVUTA::TWEETSERVICE";
 	public static final String TWEETS = "org.mockup.wvuta.TweetService";
 	private TweetLookupTask tweetTask;
 	public static ArrayList<Report> all_tweets;
 	private ArrayList<String> times = new ArrayList<String>();
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	@Override
 	public void onCreate() {
 		Log.d(TAG, "TweetService onCreate");
 		super.onCreate();
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		tweetTask = new TweetLookupTask();
 		tweetTask.execute((Void[]) null);
 		return super.onStartCommand(intent, flags, startId);
 	}
 
	/**
	 * Checks times to see if tweets were found, if so check if preferences
	 * were updated.  Then, broadcast results.
	 */
 	private void announce_results() {
 		Log.d(TAG, "Broadcasting tweets");
 		if (!times.isEmpty()) {
 			update_latest();
 		}
 		Intent i = new Intent(TWEETS);
 		sendBroadcast(i);
 		stopSelf();
 	}
 
 	/**
 	 * Compiles list of tweets into reports using regular expressions.
 	 * 
 	 * @param tweets
 	 *            - List<Status> of tweets from WVUDOT
 	 */
 	private void compile_tweets(List<winterwell.jtwitter.Status> tweets) {
 		String beech = null;
 		String walnut = null;
 		String eng = null;
 		String tow = null;
 		String med = null;
 		Date time = null;
 		// prt pattern
 		Pattern prt_pattern = Pattern.compile("[pP][rR][tT].*");
 
 		// station patterns
 		Pattern beech_pattern = Pattern.compile("BEECH.*|BEE.*");
 		Pattern walnut_pattern = Pattern.compile("WAL.*");
 		Pattern eng_pattern = Pattern.compile("ENG.*");
 		Pattern tow_pattern = Pattern.compile("TOW.*");
 		Pattern med_pattern = Pattern.compile("MED.*");
 
 		// tweet time pattern
 		Pattern time_pattern = Pattern.compile("\\(.*\\)");
 
 		// status patterns
 		Pattern down_pattern = Pattern.compile("DOWN.*|OUT OF SERVICE.*");
 		Pattern all_down_pattern = Pattern.compile("closed.*|DOWN\\.");
 		Pattern up_pattern = Pattern.compile("RUNNING.*NORM");
 
 		Calendar cal = Calendar.getInstance();
 		for (int i = 0; i < tweets.size(); i++) {
 			beech = null;
 			walnut = null;
 			eng = null;
 			tow = null;
 			med = null;
 			time = null;
 			Matcher prt_matcher = prt_pattern.matcher(tweets.get(i).toString());
 			SimpleDateFormat df = new SimpleDateFormat("(MMM. dd @ hh:mmaa)");
 			if (prt_matcher.find()) {
 				String prt_tweet = prt_matcher.group().toUpperCase();
				Log.d(TAG, "PRT Tweet: " + prt_tweet);
 				int bus_index = -1;
 				String prt_no_bus = prt_tweet;
				// remove bus portion if exists
 				if ((bus_index = prt_tweet.indexOf("BUS")) != -1) {
 					prt_no_bus = prt_tweet.substring(0, bus_index);
 				}
 				Matcher up_matcher = up_pattern.matcher(prt_no_bus);
 				Matcher down_matcher = down_pattern.matcher(prt_no_bus);
 				Matcher all_down_matcher = all_down_pattern.matcher(prt_no_bus);
 				Matcher time_matcher = time_pattern.matcher(prt_tweet);
 				if (time_matcher.find()) {
 					try {
 						time = df.parse(time_matcher.group());
 					} catch (ParseException e) {
 						Log.d(TAG, "Invalid tweet time format.");
 					}
 				}
				if (time != null && cal.get(Calendar.DATE) == time.getDate()
 						&& cal.get(Calendar.MONTH) == time.getMonth()) {
					// all stations up
 					if (up_matcher.find()) {
 						beech = "Up";
 						walnut = "Up";
 						eng = "Up";
 						tow = "Up";
 						med = "Up";
					// all stations down
 					} else if (all_down_matcher.find()) {
 						beech = "Down";
 						walnut = "Down";
 						eng = "Down";
 						tow = "Down";
 						med = "Down";
					// some station(s) down
 					} else if (down_matcher.find()) {
 						Matcher beech_matcher = beech_pattern
 								.matcher(down_matcher.group());
 						Matcher wal_matcher = walnut_pattern
 								.matcher(down_matcher.group());
 						Matcher eng_matcher = eng_pattern.matcher(down_matcher
 								.group());
 						Matcher tow_matcher = tow_pattern.matcher(down_matcher
 								.group());
 						Matcher med_matcher = med_pattern.matcher(down_matcher
 								.group());
 						if (beech_matcher.find()) {
 							beech = "Down";
 						}
 						if (wal_matcher.find()) {
 							walnut = "Down";
 						}
 						if (eng_matcher.find()) {
 							eng = "Down";
 						}
 						if (tow_matcher.find()) {
 							tow = "Down";
 						}
 						if (med_matcher.find()) {
 							med = "Down";
 						}
 					}
 				}
 			}
 			if (time != null) {
 				SimpleDateFormat display_time = new SimpleDateFormat("hh:mm aa");
 				String time_2 = display_time.format(time);
 				if (beech != null) {
 					all_tweets.add(new Report("Beechurst", time_2, beech,
 							"WVUDOT"));
 					times.add(df.format(time));
 				}
 				if (walnut != null) {
 					all_tweets.add(new Report("Walnut", time_2, walnut,
 							"WVUDOT"));
 					times.add(df.format(time));
 				}
 				if (eng != null) {
 					all_tweets.add(new Report("Engineering", time_2, eng,
 							"WVUDOT"));
 					times.add(df.format(time));
 				}
 				if (tow != null) {
 					all_tweets.add(new Report("Towers", time_2, tow, "WVUDOT"));
 					times.add(df.format(time));
 				}
 				if (med != null) {
 					all_tweets
 							.add(new Report("Medical", time_2, med, "WVUDOT"));
 					times.add(df.format(time));
 				}
 			}
 		}
 
 	}
 
 	private void update_latest() {
 		Log.d(TAG, "Updating prefs with tweets");
 		String beech_status = null;
 		String eng_status = null;
 		String med_status = null;
 		String tow_status = null;
 		String wal_status = null;
 		String b_source = null, e_source = null, m_source = null;
 		String t_source = null, w_source = null;
 		String b_time = null, e_time = null, m_time = null;
 		String t_time = null, w_time = null;
 
 		Iterator<Report> report_it = all_tweets.iterator();
 		Iterator<String> time_it = times.iterator();
 		SharedPreferences prefs = getSharedPreferences(Constants.LATEST,
 				Context.MODE_PRIVATE);
		while (report_it.hasNext() && time_it.hasNext()) {
 			Report current = report_it.next();
 			String status = current.getStatus();
 			String location = current.getLocation();
 			String source = current.getNote();
 			String time = time_it.next();
 			if (beech_status == null && location.equalsIgnoreCase("beechurst")) {
 				beech_status = status;
 				b_source = source;
 				b_time = time;
 			}
 			if (eng_status == null && location.equalsIgnoreCase("engineering")) {
 				eng_status = status;
 				e_source = source;
 				e_time = time;
 			}
 			if (med_status == null && location.equalsIgnoreCase("medical")) {
 				med_status = status;
 				m_source = source;
 				m_time = time;
 			}
 			if (tow_status == null && location.equalsIgnoreCase("towers")) {
 				tow_status = status;
 				t_source = source;
 				t_time = time;
 			}
 			if (wal_status == null && location.equalsIgnoreCase("walnut")) {
 				wal_status = status;
 				w_source = source;
 				w_time = time;
 			}
 		}
 
 		Date b_date = null, e_date = null, m_date = null, t_date = null, w_date = null;
 		try {
 			b_date = b_time == null ? null : Constants.TWEETFORMAT
 					.parse(b_time);
 			e_date = e_time == null ? null : Constants.TWEETFORMAT
 					.parse(e_time);
 			m_date = m_time == null ? null : Constants.TWEETFORMAT
 					.parse(m_time);
 			t_date = t_time == null ? null : Constants.TWEETFORMAT
 					.parse(t_time);
 			w_date = w_time == null ? null : Constants.TWEETFORMAT
 					.parse(w_time);
 		} catch (ParseException e) {
 			Log.d(TAG, "Couldn't parse new dates: " + e.getMessage());
 			return;
 		}
 
 		Date beech = null, eng = null, tow = null, med = null, wal = null;
 		try {
 			beech = Constants.TWEETFORMAT.parse(prefs.getString("btime", null));
 			eng = Constants.TWEETFORMAT.parse(prefs.getString("etime", null));
 			med = Constants.TWEETFORMAT.parse(prefs.getString("mtime", null));
 			tow = Constants.TWEETFORMAT.parse(prefs.getString("ttime", null));
 			wal = Constants.TWEETFORMAT.parse(prefs.getString("wtime", null));
 		} catch (Exception e) {
 			Log.d(TAG, "Couldn't parse old dates: " + e.getMessage());
 			return;
 		}
 
 		Editor ed = prefs.edit();
 		if (b_date != null && beech != null && b_date.after(beech)) {
 			if (beech_status != null) {
 				ed.putString(Constants.BEECHURST, beech_status);
 				ed.putString("bsource", b_source);
 				ed.putString("btime", b_time);
 			}
 		}
 		if (e_date != null && eng != null && e_date.after(eng)) {
 			if (eng_status != null) {
 				ed.putString(Constants.ENGINEERING, eng_status);
 				ed.putString("esource", e_source);
 				ed.putString("etime", e_time);
 			}
 		}
 		if (m_date != null && med != null && m_date.after(med)) {
 			if (med_status != null) {
 				ed.putString(Constants.MEDICAL, med_status);
 				ed.putString("msource", m_source);
 				ed.putString("mtime", m_time);
 			}
 		}
 		if (t_date != null && tow != null && t_date.after(tow)) {
 			if (tow_status != null) {
 				ed.putString(Constants.TOWERS, tow_status);
 				ed.putString("tsource", t_source);
 				ed.putString("ttime", t_time);
 			}
 		}
 		if (w_date != null && wal != null && w_date.after(wal)) {
 			if (wal_status != null) {
 				ed.putString(Constants.WALNUT, wal_status);
 				ed.putString("wsource", w_source);
 				ed.putString("wtime", w_time);
 			}
 		}
 		Log.d(TAG, "updated prefs with tweets");
 		ed.commit();
 	}
 
 	/**
 	 * AsyncTask which pulls top twenty tweets from WVUDOT. Compiles these
 	 * tweets into reports if successful.
 	 * 
 	 * @author Steve
 	 * 
 	 */
 	private class TweetLookupTask extends AsyncTask<Void, Void, String> {
 
 		@Override
 		protected String doInBackground(Void... params) {
 			all_tweets = new ArrayList<Report>();
 			Twitter mtwitter = new Twitter();
 			try {
 				List<winterwell.jtwitter.Status> tweets = mtwitter
 						.getUserTimeline("WVUDOT");
 
 				compile_tweets(tweets);
 			} catch (Exception e) {
 				Log.d(TAG,
 						"Exception when retrieving tweets: " + e.getMessage());
 				all_tweets.add(new Report("Unable to retrieve tweets", " ",
 						" ", " "));
 			}
 			announce_results();
 			return null;
 		}
 
 	}
 
 }
