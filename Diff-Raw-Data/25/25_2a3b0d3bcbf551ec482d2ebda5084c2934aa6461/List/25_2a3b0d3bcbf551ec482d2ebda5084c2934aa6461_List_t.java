 package com.pilot51.lclock;
 
 import java.io.BufferedInputStream;
 import java.io.InputStream;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.text.Html;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class List extends Activity {
 	protected Common common;
 	private String TAG;
 	private int src, calAccuracy;
 	private TextView txtTimer;
 	private ListView lv;
 	private SimpleAdapter adapter;
 	private HashMap<String, Object> launchMap = new HashMap<String, Object>();
 	private ArrayList<HashMap<String, Object>> launchMaps = new ArrayList<HashMap<String, Object>>();
 	private CountDownTimer timer;
	protected static final int
 		ACC_YEAR = 1,
 		ACC_MONTH = 2,
 		ACC_DAY = 3,
 		ACC_HOUR = 4,
 		ACC_MINUTE = 5,
 		ACC_SECOND = 6;
 
 	protected Common newCommon() {
 		return new Common(this);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		common = newCommon();
 		TAG = common.TAG;
 		src = getIntent().getIntExtra("source", 0);
 		setContentView(R.layout.list);
 		if (getFeed()) {
 			launchMap = launchMaps.get(0);
 			common.newAlertBuilder();
 			adapter = new SimpleAdapter(this, launchMaps, R.layout.grid,
 					new String[] { "mission", "vehicle", "location", "date", "time", "description" },
 					new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6 });
 			createList();
 			txtTimer.setVisibility(TextView.VISIBLE);
 			String mission = src == 1 ? (String)launchMap.get("mission") : (String)launchMap.get("vehicle");
 			if (((Calendar)launchMap.get("cal")).getTimeInMillis() > 0) {
 				timer = new CDTimer(launchMap, 1000, this, "Next mission: " + mission).start();
 			} else txtTimer.setVisibility(TextView.GONE);
 			lv.setOnItemClickListener(new OnItemClickListener() {
 				@SuppressWarnings("unchecked")
 				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 					launchMap = (HashMap<String, Object>) lv.getItemAtPosition(position);
 					if (timer != null)
 						timer.cancel();
 					txtTimer.setVisibility(TextView.VISIBLE);
 					String mission = null;
 					if (src == 1)
 						mission = ((String)launchMap.get("mission")).replaceAll("</a>|^[0-9a-zA-Z \\-]+\\(|\\)$", "");
 					else if (src == 2)
 						mission = (String)launchMap.get("vehicle");
 					if (((Calendar)launchMap.get("cal")).getTimeInMillis() > 0)
 						timer = new CDTimer(launchMap, 1000, List.this, mission).start();
 					else
 						txtTimer.setText(mission + "\nError parsing launch time.");
 				}
 			});
 		}
 	}
 
 	void createList() {
 		lv = (ListView) findViewById(R.id.list);
 		common.ad();
 		txtTimer = (TextView) findViewById(R.id.txtTime);
 		txtTimer.setVisibility(TextView.GONE);
 		TextView header1 = (TextView) findViewById(R.id.header1);
 		if(src == 2) header1.setText("Payload");
 		registerForContextMenu(lv);
 		lv.setAdapter(adapter);
 	}
 
 	Calendar eventCal(HashMap<String, Object> map) {
 		Calendar cal = Calendar.getInstance();
 		try {
 			boolean hasTime = true, hasDay = true;
 			String year = (String)map.get("year");
 			String date = ((String)map.get("day")).replaceAll("\\?|/[0-9]+|\\.", "").replaceFirst("Sept", "Sep");
 			String time = ((String)map.get("time")).replaceAll("^[A-Za-z]* |\\-[0-9]{4}(:[0-9]{2})?| \\([0-9a-zA-Z:; \\-]*\\)| (–|\\-|and|to)[0-9ap: ]+(m|[0-9])| */ *[0-9a-zA-Z: \\-]+$", "");
 			if ((src == 1 & time.contentEquals("")) | (src == 2 & time.contentEquals("TBD")))
 				hasTime = false;
 			if (date.matches("[A-Za-z]+"))
 				hasDay = false;
 			if (src == 1) {
 				if (time.matches("[0-9]{1,2}:[0-9]{2}:[0-9]{2} [ap]m [A-Z]+")) {
 					cal.setTime(new SimpleDateFormat("h:mm:ss a z MMM d yyyy").parse(time + " " + date + " " + year));
 					calAccuracy = ACC_SECOND;
 				} else if (hasTime) {
 					cal.setTime(new SimpleDateFormat("h:mm a z MMM d yyyy").parse(time + " " + date + " " + year));
 					calAccuracy = ACC_MINUTE;
 				} else if (hasDay) {
 					cal.setTime(new SimpleDateFormat("MMM d yyyy").parse(date + " " + year));
 					calAccuracy = ACC_DAY;
 				} else {
 					cal.setTime(new SimpleDateFormat("MMM yyyy").parse(date + " " + year));
 					calAccuracy = ACC_MONTH;
 				}
 			} else if (src == 2) {
 				if (hasTime & time.matches("[0-9]{4}:[0-9]{2} [A-Z]+")) {
 					cal.setTime(new SimpleDateFormat("HHmm:ss z MMM d yyyy").parse(time + " " + date + " " + year));
 					calAccuracy = ACC_SECOND;
 				} else if (hasTime) {
 					cal.setTime(new SimpleDateFormat("HHmm z MMM d yyyy").parse(time + " " + date + " " + year));
 					calAccuracy = ACC_MINUTE;
 				} else if (hasDay) {
 					cal.setTime(new SimpleDateFormat("MMM d yyyy").parse(date + " " + year));
 					calAccuracy = ACC_DAY;
 				} else {
 					cal.setTime(new SimpleDateFormat("MMM yyyy").parse(date + " " + year));
 					calAccuracy = ACC_MONTH;
 				}
 				Calendar cal2 = Calendar.getInstance();
 				cal2.set(Calendar.MONTH, cal2.get(Calendar.MONTH) - 1);
 				if (cal.before(cal2))
 					cal.add(Calendar.YEAR, 1);
 			}
 		} catch (Exception e) {
			Log.w(TAG, "Error parsing event date!");
 			e.printStackTrace();
 			cal.setTimeInMillis(0);
 		}
 		return cal;
 	}
 
 	boolean getFeed() {
 		String data = null;
 		if (src == 1)
 			data = downloadFile("http://www.nasa.gov/missions/highlights/schedule.html");
 		else if (src == 2)
 			data = downloadFile("http://spaceflightnow.com/tracking/index.html");
 		if (data == null) {
 			launchMaps = common.readCache(src);
 			if (launchMaps.isEmpty()) {
 				Toast.makeText(this, "Error: No data received and no cache.", Toast.LENGTH_LONG).show();
 			} else {
 				// Tell user situation if cache successfully loaded
 				Toast.makeText(this, "No data received, loaded from cache.", Toast.LENGTH_LONG).show();
 			}
 		} else {
 			try {
 				if (src == 1)
 					parseNASA(data);
 				else if (src == 2)
 					parseSfn(data);
 				// Save cache if new data downloaded
 				common.saveCache(src, launchMaps);
 			} catch (Exception e) {
 				launchMaps = common.readCache(src);
 				if (launchMaps.isEmpty()) {
 					Toast.makeText(this, "Error parsing received data,\nno cache to fall back to.", Toast.LENGTH_LONG).show();
 				} else {
 					// Tell user situation if cache successfully loaded
 					Toast.makeText(this, "Error parsing received data,\nloaded from cache.", Toast.LENGTH_LONG).show();
 				}
 				Toast.makeText(this, "Please contact developer if error persists.", Toast.LENGTH_LONG).show();
 				e.printStackTrace();
 			}
 		}
 		if (launchMaps.isEmpty()) {
 			// Finish List activity if no data loaded
 			finish();
 			return false;
 		}
 		return true;
 	}
 
 	String downloadFile(String url) {
 		InputStream input = null;
 		String strdata = null;
 		StringBuffer strbuff = new StringBuffer();
 		int count;
 		try {
 			URL url2 = new URL(url);
 			// download the file
 			input = new BufferedInputStream(url2.openStream());
 			byte data[] = new byte[1024];
 			long total = 0;
 			while ((count = input.read(data)) != -1) {
 				total += count;
 				strbuff.append(new String(data, 0, count));
 			}
 			strdata = strbuff.toString();
 			strdata = strdata.replaceAll("\r\n", "\n");
 			input.close();
 		} catch (Exception e) {
 			Log.e(TAG, "Error downloading file!");
 			e.printStackTrace();
 		}
 		// Log.d(TAG, strdata);
 		return strdata;
 	}
 
 	void parseNASA(String data) {
 		data = data.replaceAll("<[aA] [^>]*?>|</[aA]>|<font[^>]*?>|</font>|</?b>|\n|\t", "");
 		int tmp;
 		String year = null;
 		for (int i = 0; data.contains("Description:"); i++) {
 			HashMap<String, Object> map = new HashMap<String, Object>();
 			
 			// Isolate event from the rest of the HTML
 			String data2 = data.substring(data.indexOf("Date:"), data.indexOf("<br /><br />", data.indexOf("Description:")) + 12);
 			
 			// Year
 			tmp = data.indexOf("<center> 20");
 			if (tmp != -1 & tmp < data.indexOf("Date:")) {
 				data = data.substring(tmp + 9, data.length());
 				year = data.substring(0, data.indexOf(" "));
 			}
 			map.put("year", year);
 			
 			data = data.substring(data.indexOf("<br /><br />", data.indexOf("Description:")) + 12, data.length());
 
 			// Date
 			data2 = data2.substring(data2.indexOf("Date:") + 6, data2.length());
 			map.put("day", data2.substring(0, data2.indexOf("<")).replaceAll("[\\*\\+]*", "").trim());
 			map.put("date", map.get("day") + ", " + year);
 
 			// Mission
 			data2 = data2.substring(data2.indexOf("Mission:") + 9, data2.length());
 			map.put("mission", data2.substring(0, data2.indexOf("<br")).trim());
 
 			// Vehicle
 			data2 = data2.substring(data2.indexOf("Vehicle:") + 9, data2.length());
 			map.put("vehicle", data2.substring(0, data2.indexOf("<br")).trim());
 
 			// Location
 			data2 = data2.substring(data2.indexOf("Site:") + 6, data2.length());
 			map.put("location", data2.substring(0, data2.indexOf("<br")).trim());
 
 			// Time
 			if (data2.contains("Time:"))
 				tmp = data2.indexOf("Time:") + 5;
 			else if (data2.contains("Window:"))
 				tmp = data2.indexOf("Window:") + 7;
 			else if (data2.contains("Times:"))
 				tmp = data2.indexOf("Times:") + 6;
 			data2 = data2.substring(tmp, data2.length());
 			map.put("time", data2.substring(0, data2.indexOf("<br")).replaceAll("[\\.\\*\\+]*", "").replaceAll(" {2,}", " ").trim());
 
 			// Description
 			data2 = data2.substring(data2.indexOf("Description:") + 13, data2.length());
 			map.put("description", data2.substring(0, data2.indexOf("<br")).trim());
 
 			// Calendar
 			map.put("cal", eventCal(map));
 			map.put("calAccuracy", calAccuracy);
 			
 			launchMaps.add(map);
 		}
 	}
 
 	void parseSfn(String data) {
 		data = data.replaceAll("<![ \n\t]*(--([^-]|[\n]|-[^-])*--[ \n\t]*)>|<FONT[^>]*?>|</[\nFONT]{4,5}>|</?B>|<[aA]\\s[^>]*?>|</[aA]>", "");
 		int tmp = 0;
 		int year = Calendar.getInstance().get(Calendar.YEAR);
 		for (int i = 0; data.contains("CC0000"); i++) {
 			HashMap<String, Object> map = new HashMap<String, Object>();
 			
 			// Isolate event from the rest of the HTML
 			String data2 = data.substring(data.indexOf("CC0000") + 8, data.indexOf("6\"></TD"));
 			data = data.substring(data.indexOf("6\"></TD") + 8, data.length());
 
 			// Date
 			map.put("day", data2.substring(0, data2.indexOf("<")).replaceAll("\n", " ").trim());
 			map.put("year", Integer.toString(year));
 			map.put("date", map.get("day")/* + ", " + map.get("year")*/);
 
 			// Vehicle
 			data2 = data2.substring(data2.indexOf(">&nbsp;") + 7, data2.length());
 			map.put("vehicle", data2.substring(0, data2.indexOf("&nbsp")).replaceAll("\n", " ").trim());
 			
 			// Payload
 			data2 = data2.substring(data2.indexOf("&#149;") + 18, data2.length());
 			map.put("mission", data2.substring(0, data2.indexOf("</TD")).replaceAll("\n|<BR>", " ").trim());
 
 			// Time
 			if (data2.indexOf("time:") != -1)
 				tmp = data2.indexOf("time:") + 5;
 			else if (data2.indexOf("window:") != -1)
 				tmp = data2.indexOf("window:") + 7;
 			else if (data2.indexOf("times:") != -1)
 				tmp = data2.indexOf("times:") + 6;
 			data2 = data2.substring(tmp, data2.length());
 			map.put("time", data2.substring(0, data2.indexOf("<")).replaceAll("\\.", "").replaceAll("\n", " ").trim());
 
 			// Location
 			data2 = data2.substring(data2.indexOf("site:") + 5, data2.length());
 			map.put("location", data2.substring(0, data2.indexOf("<")).replaceAll("\n", " ").trim());
 
 			// Description
 			data2 = data2.substring(data2.indexOf("><BR>") + 5, data2.length());
 			map.put("description", data2.substring(0, data2.indexOf("</TD")).replaceAll("\n", " ").trim());
 			
 			// Calendar
 			map.put("cal", eventCal(map));
 			map.put("calAccuracy", calAccuracy);
 			
 			launchMaps.add(map);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		MenuInflater inflater =	getMenuInflater();
 		inflater.inflate(R.menu.context_menu, menu);
 		launchMap = (HashMap<String, Object>)lv.getItemAtPosition(((AdapterContextMenuInfo)menuInfo).position);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		launchMap = (HashMap<String, Object>)lv.getItemAtPosition(info.position);
 		switch (item.getItemId()) {
 			case R.id.ctxtMap:
 				String location = (String)launchMap.get("location");
 				Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
 				Uri.parse("geo:0,0?q=" + location));
 				startActivity(intent);
 				return true;
 			default:
 					return super.onContextItemSelected(item);
 		}
 	}
 	class CDTimer extends CountDownTimer {
 		private int cddd, cdhh, cdmm, cdss, accuracy;
 		private long tLaunch;
 		private String info, timeFormat, accFormat = "", inaccFormat = "";
 
 		CDTimer(HashMap<String, Object> map, long countDownInterval, Context context, String info) {
 			super(((Calendar)map.get("cal")).getTimeInMillis() - System.currentTimeMillis(), countDownInterval);
 			tLaunch = ((Calendar)map.get("cal")).getTimeInMillis();
 			this.info = info;
 			try {
 				accuracy = (Integer)map.get("calAccuracy");
 			} catch (NullPointerException e) {
 				accuracy = ACC_SECOND;
 				Log.w(TAG, "Time accuracy not available (likely because loaded cache from v0.6.0), using accuracy to the second.");
 			}
 			if (accuracy == ACC_SECOND) {
 				timeFormat = "yyyy-MM-dd h:mm:ss a zzz";
 				accFormat = "HH:mm:ss";
 			} else if (accuracy == ACC_MINUTE) {
 				timeFormat = "yyyy-MM-dd h:mm a zzz";
 				accFormat = "HH:mm";
 				inaccFormat = ":ss";
 			} else if (accuracy == ACC_HOUR) {
 				timeFormat = "yyyy-MM-dd h a zzz";
 				accFormat = "HH";
 				inaccFormat = ":mm:ss";
 			} else if (accuracy == ACC_DAY) {
 				timeFormat = "yyyy-MM-dd";
 				inaccFormat = "HH:mm:ss";
 			} else if (accuracy == ACC_MONTH) {
 				timeFormat = "yyyy-MM";
 				inaccFormat = "HH:mm:ss";
 			} else if (accuracy == ACC_YEAR) {
 				timeFormat = "yyyy";
 				inaccFormat = "HH:mm:ss";
 			}
 		}
 
 		@Override
 		public void onFinish() {
 			txtTimer.setText(info + "\n" + new SimpleDateFormat(timeFormat).format(tLaunch) + "\nLaunched! (supposedly)");
 		}
 
 		@Override
 		public void onTick(long millisUntilFinished) {
 			cddd = (int) (millisUntilFinished / 1000 / 24 / 60 / 60);
 			cdhh = (int) ((millisUntilFinished - cddd * 1000 * 24 * 60 * 60) / 1000 / 60 / 60);
 			cdmm = (int) ((millisUntilFinished - cddd * 1000 * 24 * 60 * 60 - cdhh * 1000 * 60 * 60) / 1000 / 60);
 			cdss = (int) ((millisUntilFinished - cddd * 1000 * 24 * 60 * 60 - cdhh * 1000 * 60 * 60 - cdmm * 1000 * 60) / 1000);
 			txtTimer.setText(Html.fromHtml(info + "<br />" + new SimpleDateFormat(timeFormat).format(tLaunch)
 					+ "<br />Countdown: " + inaccColor(cddd + "d ")
 					+ new SimpleDateFormat(accFormat).format(new Date(0, 0, 0, cdhh, cdmm, cdss))
 					+ inaccColor(new SimpleDateFormat(inaccFormat).format(new Date(0, 0, 0, cdhh, cdmm, cdss)))
 			));
 		}
 		
 		private String inaccColor(String s) {
 			if (s.endsWith("d ") & accuracy >= ACC_DAY)
 				return s;
 			return "<font color='#FF0000'>" + s + "</font>";
 		}
 	}
 }
