 package tw.ipis.routetaiwan;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.jsoup.Jsoup;
 import org.jsoup.select.Elements;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.ContextThemeWrapper;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.Gravity;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TabHost;
 import android.widget.TabHost.TabContentFactory;
 import android.widget.TabHost.TabSpec;
 import android.widget.TabWidget;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TableRow.LayoutParams;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class pop_transit extends Activity {
 	private static String TAG = "--pop_transit--";
 	private ProgressBar process;
 	private ProgressBar process2;
 	private ProgressBar loading;
 	private ArrayList<bus_provider> bus_taipei = new ArrayList<bus_provider>();
 	private ArrayList<bus_provider> bus_taichung = new ArrayList<bus_provider>();
 	private ArrayList<bus_provider> bus_kaohsiung = new ArrayList<bus_provider>();
 	private ArrayList<bus_provider> bus_taoyuang = new ArrayList<bus_provider>();
 	private List<TableRow> timetable = new ArrayList<TableRow>();
 	private List<String> original_sta = new ArrayList<String>();
 	private List<String> after_sta = new ArrayList<String>();
 	String line = null, agency = null, car_class = null;
 	String dept = null;
 	String arr = null;
 	String name = null;
 	boolean err_tag_fail = false;
 	final Handler handler = new Handler();
 	Runnable runtask;
 	private String[] hsr_stations = {"台北站", "板橋站", "桃園站", "新竹站", "台中站", "嘉義站", "台南站", "左營站"};
 	private String[] cn_hsr_stations = {"台北站", "板桥站", "桃园站", "新竹站", "台中站", "嘉义站", "台南站", "左营站"};
 	private String[] en_hsr_stations = {"Taipei", "Banciao", "Taoyuan", "Hsinchu", "Taichung", "Chiayi", "Tainan", "Zuoying"};
 	private static final int ID_PROVIDER_ANNOUNCEMENT = 0x12365401;
 	private static final int ID_HSR_STATUS = 0x12345001;
 	private static final int ID_HSR_STATUS_DESCRIPTION = 0x12345002;
 	private static final int ID_HSR_STATUS_TABLE = 0x12345003;
 	private static final int ID_HSR_TIME_DEPART = 0x12345004;
 	private static final int ID_HSR_TIME_TABLE_TITLE = 0x12345005;
 	private static final int ID_HSR_TIME_TABLE_TABLE = 0x12345006;
 	private static final int ID_INTERCITYBUS_CARRIER = 0x12345007;
 	private static final int ID_BUS_TIMETABLE_DATE = 0x12345008;
 	private static final int ID_TRA_STATUS_DESCRIPTION = 0x12345009;
 	private static final int ID_TRA_TIMETABLE_TITLE = 0x1234500A;
 	private static final int ID_TRA_DATE_DESCRIPTION = 0x1234500B;
 	private static final int ID_TRA_DEPART_TIME = 0x1234500C;
 	private GestureDetector gestureDetector;
 	View.OnTouchListener gestureListener;
 	Time Current_time;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.pop_transit);
 
 		Bundle Data = this.getIntent().getExtras();
 		String type = Data.getString("type");
 
 		long time = 0;
 		long current_time_millis = System.currentTimeMillis();
 		Current_time = new Time();
 		Current_time.set(current_time_millis);
 		
 		station_name_replace();
 		if(type != null && !type.contentEquals("null")) {
 			line = Data.getString("line");
 			if(type.contentEquals("bus")) {
 
 				bus_agency_classify();
 
 				agency = Data.getString("agency");
 				dept = Data.getString("dept");
 				arr = Data.getString("arr");
 				name = Data.getString("headname");
 				time = Data.getLong("time");
 			}
 			else if(type.contentEquals("tra")) {
 				car_class = Data.getString("class");
 				time = Data.getLong("time");
 				dept = Data.getString("dept");
 				arr = Data.getString("arr");
 			}
 			else if(type.contentEquals("hsr")) {
 				time = Data.getLong("time");
 				dept = Data.getString("dept");
 				arr = Data.getString("arr");
 			}
 		}
 		else {
 			Log.d(TAG, "Unknown type, finished...");
 			finish();
 		}
 
 		process = new ProgressBar(this, null, android.R.attr.progressBarStyleInverse);
 		process.setIndeterminate(true);
 		final RelativeLayout rl = (RelativeLayout)findViewById(R.id.rl_pop_transit);
 		RelativeLayout.LayoutParams process_param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 		process_param.addRule(RelativeLayout.CENTER_IN_PARENT);
 		process.setLayoutParams(process_param);
 
 		process2 = new ProgressBar(this, null, android.R.attr.progressBarStyleInverse);
 		process2.setIndeterminate(true);
 		process2.setLayoutParams(process_param);
 
 		loading = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
 		loading.setIndeterminate(true);
 		RelativeLayout.LayoutParams loading_param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 		loading_param.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 		loading_param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 		loading.setLayoutParams(loading_param);
 
 		/* 台鐵 */
 		/* If type = "tra", then open webview for ex: http://twtraffic.tra.gov.tw/twrail/mobile/TrainDetail.aspx?searchdate=2013/10/03&traincode=117 */
 		if(type.contentEquals("tra")) {
 			Time date = new Time();
 			date.set(current_time_millis > time * 1000 ? current_time_millis : time * 1000);
 			final String str_date = String.format("%d/%02d/%02d", date.year, date.month + 1, date.monthDay);
 			String depart_time = String.format("%02d:%02d", date.hour, date.minute);
 
 			String depart_time_later = (date.hour + 6 > 23) ? "23:59" : String.format("%02d:%02d", date.hour + 6, date.minute);;
 
 			final String tra_real_time_url = "http://twtraffic.tra.gov.tw/twrail/mobile/TrainDetail.aspx?searchdate={0}&traincode={1}";
 //			String tra_timetable_url = "http://163.29.3.99/mobile_en/clean/result.jsp?from1={0}&to1={1}&carclass=2&Date={2}&sTime={3}&eTime={4}&Dep=true&Submit=Submit";
 			String tra_timetable_url = "http://twtraffic.tra.gov.tw/twrail/SearchResult.aspx?searchtype=0&searchdate={2}&fromstation={0}&tostation={1}&trainclass=2&fromtime={3}&totime={4}";
 			String current_status = getResources().getString(R.string.current_status) + ":";
 
 			int dept_station_seq = find_station_by_zhname(dept.replaceAll("[火]?車站", ""));
 			int arr_station_seq = find_station_by_zhname(arr.replaceAll("[火]?車站", ""));
 
 			String zh_stations[] = getResources().getStringArray(R.array.zh_station);
 			String en_stations[] = getResources().getStringArray(R.array.en_station_id);
 			String cn_stations[] = getResources().getStringArray(R.array.cn_station);
 
 			/* 設定activity title, ex: 自強 123 */
 			this.setTitle(car_class + " " + line);
 
 			/* 資料由台鐵提供 */
 			show_info_provider(R.string.provide_by_tra);
 			loading.setVisibility(ProgressBar.VISIBLE);
 
 			rl.removeView(process);
 
 			/* 即時資訊Layout */
 
 			final RelativeLayout realtime = new RelativeLayout(this);
 			realtime.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
 
 			final TextView status = new TextView(pop_transit.this);
 			status.setText(current_status);
 			status.setId(ID_TRA_STATUS_DESCRIPTION);
 			status.setTextColor(Color.WHITE);
 			status.setTextSize(20);
 			status.setGravity(Gravity.LEFT);
 			status.setHorizontallyScrolling(false);
 			RelativeLayout.LayoutParams tvparam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 			tvparam.addRule(RelativeLayout.ALIGN_PARENT_TOP);
 			status.setLayoutParams(tvparam);
 			realtime.addView(status);
 
 			TableLayout title = new TableLayout(pop_transit.this);
 			title.setId(ID_TRA_TIMETABLE_TITLE);
 			RelativeLayout.LayoutParams tlparam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 			tlparam.addRule(RelativeLayout.BELOW, ID_TRA_STATUS_DESCRIPTION);
 			title.setLayoutParams(tlparam);
 			title.setOrientation(TableLayout.VERTICAL);
 			realtime.addView(title);
 
 			TableRow tr_title = CreateTableRow(title);
 			tr_title.setBackgroundColor(Color.BLACK);
 			tr_title.setWeightSum(1.0f);
 
 			ImageView iv = new ImageView(pop_transit.this);
 			iv.setImageResource(R.drawable.start);
 			iv.setAlpha(0);
 			iv.setBackgroundColor(Color.TRANSPARENT);
 			iv.setMaxHeight((int) (20 * getResources().getDisplayMetrics().density));
 			iv.setMaxWidth((int) (20 * getResources().getDisplayMetrics().density));
 			iv.setAdjustViewBounds(true);
 			iv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.05f));
 			tr_title.addView(iv);
 
 			// 站名
 			TextView tv = new TextView(pop_transit.this);
 			tv.setTextColor(Color.WHITE);
 			tv.setText(getResources().getString(R.string.station));
 			tv.setTextSize(16);
 			tv.setHorizontallyScrolling(false);
 			tv.setWidth(0);
 			tv.setGravity(Gravity.CENTER);
 			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.35f));
 			tr_title.addView(tv);
 
 			// 到站時間
 			tv = new TextView(pop_transit.this);
 			tv.setTextColor(Color.WHITE);
 			tv.setText(getResources().getString(R.string.train_arr_time));
 			tv.setTextSize(16);
 			tv.setHorizontallyScrolling(false);
 			tv.setWidth(0);
 			tv.setGravity(Gravity.CENTER);
 			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.25f));
 			tr_title.addView(tv);
 
 			// 離站時間
 			tv = new TextView(pop_transit.this);
 			tv.setTextColor(Color.WHITE);
 			tv.setText(getResources().getString(R.string.train_dep_time));
 			tv.setTextSize(16);
 			tv.setHorizontallyScrolling(false);
 			tv.setWidth(0);
 			tv.setGravity(Gravity.CENTER);
 			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.25f));
 			tr_title.addView(tv);
 
 			// 車子icon
 			iv = new ImageView(pop_transit.this);
 			iv.setImageResource(R.drawable.locomotive);
 			iv.setAlpha(0);
 			iv.setBackgroundColor(Color.TRANSPARENT);
 			iv.setMaxHeight((int) (20 * getResources().getDisplayMetrics().density));
 			iv.setMaxWidth((int) (20 * getResources().getDisplayMetrics().density));
 			iv.setAdjustViewBounds(true);
 			iv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.10f));
 			tr_title.addView(iv);
 
 			final ScrollView sv = new ScrollView(pop_transit.this);
 			RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
 			param.addRule(RelativeLayout.BELOW, ID_TRA_TIMETABLE_TITLE);
 			param.addRule(RelativeLayout.ABOVE, ID_PROVIDER_ANNOUNCEMENT);
 			sv.setLayoutParams(param);
 			realtime.addView(sv);
 			//			rl.addView(sv);
 
 			final TableLayout tl = new TableLayout(pop_transit.this);
 			tl.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 			tl.setOrientation(TableLayout.VERTICAL);
 
 			sv.addView(tl);
 
 			process_param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 			process_param.addRule(RelativeLayout.BELOW, ID_TRA_TIMETABLE_TITLE);
 			process_param.addRule(RelativeLayout.CENTER_HORIZONTAL);
 			process.setLayoutParams(process_param);
 			realtime.addView(process);
 
 			/* 時刻表Layout */
 			final RelativeLayout timetable = new RelativeLayout(this);
 			timetable.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
 
 			/* 2013/12/01 */
 			TextView tv_date = new TextView(pop_transit.this);
 			tv_date.setText(String.format("%s (%s)", str_date, weekday2str(date.weekDay)));
 			tv_date.setBackgroundColor(Color.BLACK);
 			tv_date.setId(ID_TRA_DATE_DESCRIPTION);
 			tv_date.setTextColor(Color.WHITE);
 			tv_date.setTextSize(20);
 			tv_date.setGravity(Gravity.CENTER);
 			tv_date.setHorizontallyScrolling(false);
 			tvparam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 			tvparam.addRule(RelativeLayout.ALIGN_PARENT_TOP);
 			tv_date.setLayoutParams(tvparam);
 			timetable.addView(tv_date);
 
 			/* 預計xx站出發時間: MM/dd HH:mm */
 			TextView tv_depart_time = new TextView(pop_transit.this);
 			if(getResources().getString(R.string.locale).contentEquals("English"))
 				tv_depart_time.setText(en_stations[dept_station_seq] + getResources().getString(R.string.estimate) + getResources().getString(R.string.departure_time) + ": " + depart_time);
 			else if(getResources().getString(R.string.locale).contentEquals("简体中文"))
 				tv_depart_time.setText(cn_stations[dept_station_seq] + getResources().getString(R.string.estimate) + getResources().getString(R.string.departure_time) + ": " + depart_time);
 			else
 				tv_depart_time.setText(zh_stations[dept_station_seq] + getResources().getString(R.string.estimate) + getResources().getString(R.string.departure_time) + ": " + depart_time);
 			tv_depart_time.setBackgroundColor(Color.BLACK);
 			tv_depart_time.setId(ID_TRA_DEPART_TIME);
 			tv_depart_time.setTextColor(Color.WHITE);
 			tv_depart_time.setTextSize(16);
 			tv_depart_time.setGravity(Gravity.LEFT);
 			tv_depart_time.setHorizontallyScrolling(false);
 			tvparam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 			tvparam.addRule(RelativeLayout.BELOW, ID_TRA_DATE_DESCRIPTION);
 			tv_depart_time.setLayoutParams(tvparam);
 			timetable.addView(tv_depart_time);
 
 			/* 車種  車次  起站  到站  時間 */
 			TableLayout title_timetable = new TableLayout(pop_transit.this);
 			title_timetable.setId(ID_TRA_TIMETABLE_TITLE);
 			tlparam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 			tlparam.addRule(RelativeLayout.BELOW, ID_TRA_DEPART_TIME);
 			title_timetable.setLayoutParams(tlparam);
 			title_timetable.setOrientation(TableLayout.VERTICAL);
 			timetable.addView(title_timetable);
 
 			TableRow tr_title_timetable = CreateTableRow(title_timetable);
 			title_timetable.setBackgroundColor(Color.BLACK);
 			tr_title_timetable.setWeightSum(1.0f);
 
 			/* 車種 */
 			tv = new TextView(pop_transit.this);
 			tv.setTextColor(Color.WHITE);
 			tv.setText(getResources().getString(R.string.train_type));
 			tv.setTextSize(16);
 			tv.setHorizontallyScrolling(false);
 			tv.setWidth(0);
 			tv.setGravity(Gravity.CENTER);
 			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.15f));
 			tr_title_timetable.addView(tv);
 
 			/* 車次 */
 			tv = new TextView(pop_transit.this);
 			tv.setTextColor(Color.WHITE);
 			tv.setText(getResources().getString(R.string.train_id));
 			tv.setTextSize(16);
 			tv.setHorizontallyScrolling(false);
 			tv.setWidth(0);
 			tv.setGravity(Gravity.CENTER);
 			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.15f));
 			tr_title_timetable.addView(tv);
 
 			/* 起站 */
 			tv = new TextView(pop_transit.this);
 			tv.setTextColor(Color.WHITE);
 			if(getResources().getString(R.string.locale).contentEquals("English"))
 				tv.setText(en_stations[dept_station_seq]);
 			else if(getResources().getString(R.string.locale).contentEquals("简体中文"))
 				tv.setText(cn_stations[dept_station_seq]);
 			else
 				tv.setText(zh_stations[dept_station_seq]);
 			tv.setTextSize(16);
 			tv.setHorizontallyScrolling(false);
 			tv.setWidth(0);
 			tv.setGravity(Gravity.CENTER);
 			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.25f));
 			tr_title_timetable.addView(tv);
 
 			/* 訖站 */
 			tv = new TextView(pop_transit.this);
 			tv.setTextColor(Color.WHITE);
 			if(getResources().getString(R.string.locale).contentEquals("English"))
 				tv.setText(en_stations[arr_station_seq]);
 			else if(getResources().getString(R.string.locale).contentEquals("简体中文"))
 				tv.setText(cn_stations[arr_station_seq]);
 			else
 				tv.setText(zh_stations[arr_station_seq]);
 			tv.setTextSize(16);
 			tv.setHorizontallyScrolling(false);
 			tv.setWidth(0);
 			tv.setGravity(Gravity.CENTER);
 			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.25f));
 			tr_title_timetable.addView(tv);
 
 			/* 行駛時間 */
 			tv = new TextView(pop_transit.this);
 			tv.setTextColor(Color.WHITE);
 			tv.setText(getResources().getString(R.string.duration));
 			tv.setTextSize(16);
 			tv.setHorizontallyScrolling(false);
 			tv.setWidth(0);
 			tv.setGravity(Gravity.CENTER);
 			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.20f));
 			tr_title_timetable.addView(tv);
 
 			final ScrollView sv_time = new ScrollView(pop_transit.this);
 			param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
 			param.addRule(RelativeLayout.BELOW, ID_TRA_TIMETABLE_TITLE);
 			param.addRule(RelativeLayout.ABOVE, ID_PROVIDER_ANNOUNCEMENT);
 			sv_time.setLayoutParams(param);
 			timetable.addView(sv_time);
 
 			final TableLayout tl_time = new TableLayout(pop_transit.this);
 			tl_time.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 			tl_time.setOrientation(TableLayout.VERTICAL);
 
 			sv_time.addView(tl_time);
 
 			//			process_param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 			//			process_param.addRule(RelativeLayout.BELOW, ID_TRA_TIMETABLE_TITLE);
 			//			process_param.addRule(RelativeLayout.CENTER_HORIZONTAL);
 			//			process.setLayoutParams(process_param);
 			//			timetable.addView(process);
 
 			/* 建立Tabhost */
 			final TabHost tabs = new TabHost(pop_transit.this, null);
 			param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 			param.addRule(RelativeLayout.BELOW, ID_INTERCITYBUS_CARRIER);
 			param.addRule(RelativeLayout.ABOVE, ID_PROVIDER_ANNOUNCEMENT);
 			tabs.setLayoutParams(param);
 			tabs.setId(android.R.id.tabhost);
 			rl.addView(tabs);
 
 			LinearLayout lineLayout = new LinearLayout(this);
 			lineLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
 					LinearLayout.LayoutParams.MATCH_PARENT));
 			lineLayout.setOrientation( LinearLayout.VERTICAL);
 			lineLayout.setGravity( Gravity.TOP );
 
 			TabWidget tabWidget = new TabWidget(pop_transit.this);
 			param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 			tabWidget.setLayoutParams(param);
 			tabWidget.setId(android.R.id.tabs);
 			lineLayout.addView(tabWidget);
 
 			FrameLayout tabContent = new FrameLayout(pop_transit.this);
 			tabContent.setLayoutParams(param);
 			tabContent.setId(android.R.id.tabcontent);
 			lineLayout.addView(tabContent);
 
 			tabs.addView(lineLayout);
 			tabs.setup();
 
 			Drawable dr = getResources().getDrawable(R.drawable.train_realtime);
 			Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
 
 			Drawable drawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 
 					(int) (32 * getResources().getDisplayMetrics().density), (int) (32 * getResources().getDisplayMetrics().density), true)); 
 
 			TabSpec tspec1 = tabs.newTabSpec("Tab1");
 			tspec1.setIndicator(getResources().getString(R.string.realtime_info), drawable);
 			tspec1.setContent(new PreExistingViewFactory(realtime));
 			tabs.addTab(tspec1);
 
 			dr = getResources().getDrawable(R.drawable.timetable);
 			bitmap = ((BitmapDrawable) dr).getBitmap();
 
 			drawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 
 					(int) (32 * getResources().getDisplayMetrics().density), (int) (32 * getResources().getDisplayMetrics().density), true)); 
 
 
 			TabSpec tspec2 = tabs.newTabSpec("Tab2");
 			tspec2.setIndicator(getResources().getString(R.string.time_table), drawable);
 			tspec2.setContent(new PreExistingViewFactory(timetable));
 			tabs.addTab(tspec2);
 
 			gestureDetector = new GestureDetector(this, new MyGestureDetector(tabs));
 			gestureListener = new View.OnTouchListener() {
 				public boolean onTouch(View v, MotionEvent event) {
 					return gestureDetector.onTouchEvent(event);
 				}
 			};
 			sv_time.setOnTouchListener(gestureListener);
 			sv.setOnTouchListener(gestureListener);
 
 			tabs.setLongClickable(true);
 			tabs.setOnTabChangedListener(new AnimatedTabHostListener(getBaseContext(), tabs));
 			
 			if(dept_station_seq >=0 && arr_station_seq >= 0) {
 				String id_stations[] = getResources().getStringArray(R.array.zh_station_id);
 				/* 拿台鐵時刻表 */
 				TRA_TIMETABLE task = new TRA_TIMETABLE(
 						new AnalysisResult() {
 							@Override
 							public void parsestr(String result) {
 							}
 
 							@Override
 							public void parsedBUS(List<BusRoute> routes) {
 								return;
 							}
 
 							@Override
 							public void parsedHSR(List<HSRTrains> trains) {
 								return;
 							}
 
 							@Override
 							public void parsedTimeTable(List<TimeTable> time) {
 							}
 
 							@Override
 							public void TraTimeTable(List<TrainTimeTable> time) {
 								timetable.removeView(process2);
 								create_tra_timetable(time, tl_time, sv_time, tabs, tl, realtime);
 							}
 						});
 				String timetable_url = MessageFormat.format(tra_timetable_url
 						, id_stations[dept_station_seq], id_stations[arr_station_seq]
 								, str_date, depart_time.replace(":", ""), depart_time_later.replace(":", ""));
 				Log.i(TAG, "url=" + timetable_url);
 				timetable.addView(process2);
 				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, timetable_url);
 			}
 
 			runtask = new Runnable() {
 				@Override
 				public void run() {
 					HTML_BUS_PARSER task = new HTML_BUS_PARSER(
 							new AnalysisResult() {
 								@Override
 								public void parsestr(String result) {
 									realtime.removeView(process);
 									ArrayList<TrainTable> traininfo = parse_train_info(result);
 									loading.setVisibility(ProgressBar.INVISIBLE);
 									if(traininfo.isEmpty()) {
 										status.setText(getResources().getString(R.string.no_data));
 									}
 									else {
 										String current_status = getResources().getString(R.string.current_status) + ":";
 										if(traininfo.get(0).delaymins == 0)
 											current_status = String.format("%s %s", current_status, getResources().getString(R.string.on_time));
 										else if(traininfo.get(0).delaymins > 0)
 											current_status = String.format("%s %s %d %s", current_status, 
 													getResources().getString(R.string.delay), traininfo.get(0).delaymins, getResources().getString(R.string.minute));
 										status.setText(current_status);
 										tra_find_start_dest(traininfo, dept, arr);
 										create_tratime_table(traininfo, tl, sv);
 									}
 								}
 
 								@Override
 								public void parsedBUS(List<BusRoute> routes) {
 									return;
 								}
 
 								@Override
 								public void parsedHSR(List<HSRTrains> trains) {
 									return;
 								}
 
 								@Override
 								public void parsedTimeTable(List<TimeTable> time) {
 								}
 
 								private ArrayList<TrainTable> parse_train_info(String result) {
 									ArrayList<TrainTable> info = new ArrayList<TrainTable>();
 									int str_start = result.indexOf("TRSearchResult");
 									int str_end = result.indexOf("</script></form>");
 									if(str_start < 0 || str_end < 0) return info;
 									String raw = result.substring(str_start, str_end).replace("TRSearchResult.push", "&").replace("TRSearchResult.pop();", "");
 									String rawinfo[] = raw.split("&");
 									int i = 1;
 									int delaytime = 0;
 
 									String delay = result.substring(result.indexOf("traindelaytime"), result.indexOf(";</script></form>")).replace("traindelaytime=", "");
 									if (delay.length() == 0)
 										delaytime = -1;
 									else
 										delaytime = Integer.parseInt(delay);
 									Log.i(TAG, "train delay=" + delay);
 
 									while(i < rawinfo.length) {
 										if(i + 3 < rawinfo.length) {
 											TrainTable train = new TrainTable(strrefine(rawinfo[i]), strrefine(rawinfo[i+1]), strrefine(rawinfo[i+2])
 													, false, strrefine(rawinfo[i+3]).contentEquals("x") ? true : false);
 											train.set_delay(delaytime);
 											i = i + 4;
 											if(i < rawinfo.length && strrefine(rawinfo[i]).contentEquals("30")) {
 												/*FIXME: 台鐵的蠢code有時後會出現兩個以上的30...*/
 												train.set_train();
 												train.leaved=false;
 												i++;
 											}
 											Log.i(TAG, train.dumpitem());
 											info.add(train);
 										}
 										else
 											break;
 									}
 									return info;
 								}
 								private String strrefine(String raw) {
 									/* Input: ('台北') */
 									String result = raw.substring(raw.indexOf('\'') + 1, raw.lastIndexOf('\''));
 									return result;
 								}
 
 								@Override
 								public void TraTimeTable(
 										List<TrainTimeTable> time) {
 								}
 
 							});
 					String url = MessageFormat.format(tra_real_time_url, str_date, line);
 					Log.i(TAG, "line=" + line);
 					loading.setVisibility(ProgressBar.VISIBLE);
 					task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
 					handler.postDelayed(this, 60000);
 				}
 			};
 			handler.post(runtask);
 		}
 		/* 高鐵 */
 		else if(type.contentEquals("hsr")) {
 			Time date = new Time();
 			date.set(current_time_millis > time * 1000 ? current_time_millis : time * 1000);
 			
 			String str_date = String.format("%d%02d%02d", date.year, date.month + 1, date.monthDay);
 			String hsr_real_time_url = "http://www.thsrc.com.tw/tw/TimeTable/DailyTimeTable/{0}";
 			String url = MessageFormat.format(hsr_real_time_url, str_date);
 			Log.i(TAG, String.format("hsr url=%s", url));
 
 			/* 設定activity title, ex: 自強 123 */
 			this.setTitle(getResources().getString(R.string.hsr_status));
 
 			String formattedDate = String.format("%02d:%02d", date.hour, date.minute);
 
 			final int current_min = string_2_minutes_of_day(formattedDate);
 			final boolean southbound;
 			//			if(getResources().getString(R.string.locale).contentEquals("English")) {
 			//				dept = dept.replaceAll("high speed rail ", "");
 			//				arr = arr.replaceAll("high speed rail ", "");
 			//				dept = dept.replaceAll(" station", "");
 			//				arr = arr.replaceAll(" station", "");
 			//
 			//				Log.i(TAG, String.format("<%s> <%s>", dept, arr));
 			//
 			//				southbound = Arrays.asList(en_hsr_stations).indexOf(dept) < Arrays.asList(en_hsr_stations).indexOf(arr) ? true : false;
 			//			}
 			//			else {
 			dept = dept.replaceAll("高鐵", "");
 			arr = arr.replaceAll("高鐵", "");
 			int seq_dept = Arrays.asList(hsr_stations).indexOf(dept);
 			int seq_arr = Arrays.asList(hsr_stations).indexOf(arr);
 
 			southbound = seq_dept < seq_arr ? true : false;
 			//			}
 
 			/* 表格第一行 高鐵狀態 */
 			TableLayout tl = new TableLayout(this);
 			tl.setId(ID_HSR_STATUS_TABLE);
 			RelativeLayout.LayoutParams tlparam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 			tlparam.addRule(RelativeLayout.ALIGN_PARENT_TOP);
 			tl.setLayoutParams(tlparam);
 			tl.setOrientation(TableLayout.VERTICAL);
 
 			TableRow tr = CreateTableRow(tl);
 			tr.setBackgroundColor(Color.TRANSPARENT);
 
 			ImageView iv = new ImageView(this);
 			iv.setId(ID_HSR_STATUS);
 			iv.setBackgroundColor(Color.TRANSPARENT);
 			iv.setMaxHeight((int) (20 * getResources().getDisplayMetrics().density));
 			iv.setMaxWidth((int) (20 * getResources().getDisplayMetrics().density));
 			iv.setAdjustViewBounds(true);
 			iv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.05f));
 			tr.addView(iv);
 
 			TextView tv = new TextView(this);
 			tv.setId(ID_HSR_STATUS_DESCRIPTION);
 			tv.setTextColor(Color.WHITE);
 			tv.setTextSize(20);
 			tv.setGravity(Gravity.LEFT);
 			tv.setHorizontallyScrolling(false);
 			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.95f));
 			tr.addView(tv);
 
 			rl.addView(tl);
 
 			/* 表格第二行 出發站和預計時間 */
 			/* 預計xx站出發時間 MM/dd HH:mm */
 			tv = new TextView(this);
 			tv.setId(ID_HSR_TIME_DEPART);
 			String boarding_time = String.format("%02d/%02d %02d:%02d", date.month + 1, date.monthDay, date.hour, date.minute);
 			if(getResources().getString(R.string.locale).contentEquals("English"))
 				tv.setText(en_hsr_stations[seq_dept] + getResources().getString(R.string.estimate) + getResources().getString(R.string.departure_time) + ": " + boarding_time);
 			else if(getResources().getString(R.string.locale).contentEquals("简体中文"))
 				tv.setText(cn_hsr_stations[seq_dept] + getResources().getString(R.string.estimate) + getResources().getString(R.string.departure_time) + ": " + boarding_time);
 			else 
 				tv.setText(dept + getResources().getString(R.string.estimate) + getResources().getString(R.string.departure_time) + ": " + boarding_time);
 			tv.setTextColor(Color.WHITE);
 			tv.setTextSize(16);
 			tv.setGravity(Gravity.LEFT);
 			RelativeLayout.LayoutParams tvparam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 			tvparam.addRule(RelativeLayout.BELOW, ID_HSR_STATUS_TABLE);
 			tv.setLayoutParams(tvparam);
 			tv.setHorizontallyScrolling(false);
 			rl.addView(tv);
 
 			/* 表格第三行  | 建議icon | 車次 | xx站(起點)時間 | xx站(終點)時間  */
 			tl = new TableLayout(this);
 			tl.setId(ID_HSR_TIME_TABLE_TITLE);
 			tlparam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 			tlparam.addRule(RelativeLayout.BELOW, ID_HSR_TIME_DEPART);
 			tl.setLayoutParams(tlparam);
 			tl.setOrientation(TableLayout.VERTICAL);
 			rl.addView(tl);
 
 			tr = CreateTableRow(tl);
 			tr.setBackgroundColor(Color.BLACK);
 			/* 建議icon */
 			iv = new ImageView(this);
 			iv.setImageResource(R.drawable.marker);
 			iv.setAlpha(0);
 			iv.setBackgroundColor(Color.TRANSPARENT);
 			iv.setMaxHeight((int) (20 * getResources().getDisplayMetrics().density));
 			iv.setMaxWidth((int) (20 * getResources().getDisplayMetrics().density));
 			iv.setAdjustViewBounds(true);
 			iv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.05f));
 			tr.addView(iv);
 
 			/* 車次 */
 			tv = new TextView(this);
 			tv.setText(getResources().getString(R.string.train_id));
 			tv.setTextColor(Color.WHITE);
 			tv.setTextSize(16);
 			tv.setHorizontallyScrolling(false);
 			tv.setWidth(0);
 			tv.setGravity(Gravity.CENTER);
 			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.15f));
 			tr.addView(tv);
 
 			/* xx站出發時間 */
 			tv = new TextView(this);
 			if(getResources().getString(R.string.locale).contentEquals("English"))
 				tv.setText(en_hsr_stations[seq_dept]);
 			else if(getResources().getString(R.string.locale).contentEquals("简体中文"))
 				tv.setText(cn_hsr_stations[seq_dept]);
 			else
 				tv.setText(dept);
 			tv.setTextColor(Color.WHITE);
 			tv.setTextSize(16);
 			tv.setHorizontallyScrolling(false);
 			tv.setWidth(0);
 			tv.setGravity(Gravity.CENTER);
 			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.25f));
 			tr.addView(tv);
 
 			/* xx站抵達時間 */
 			tv = new TextView(this);
 			if(getResources().getString(R.string.locale).contentEquals("English"))
 				tv.setText(en_hsr_stations[seq_arr]);
 			else if(getResources().getString(R.string.locale).contentEquals("简体中文"))
 				tv.setText(cn_hsr_stations[seq_arr]);
 			else
 				tv.setText(arr);
 			tv.setTextColor(Color.WHITE);
 			tv.setTextSize(16);
 			tv.setHorizontallyScrolling(false);
 			tv.setWidth(0);
 			tv.setGravity(Gravity.CENTER);
 			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.25f));
 			tr.addView(tv);
 
 			/* 行駛時間 */
 			tv = new TextView(this);
 			tv.setText(getResources().getString(R.string.trival_time));
 			tv.setTextColor(Color.WHITE);
 			tv.setTextSize(16);
 			tv.setHorizontallyScrolling(false);
 			tv.setWidth(0);
 			tv.setGravity(Gravity.CENTER);
 			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.30f));
 			tr.addView(tv);
 
 			/* 資料由台灣高鐵提供 */
 			show_info_provider(R.string.provide_by_thsr);
 
 			/* 時刻表捲單 */
 			final ScrollView sv = new ScrollView(this);
 			sv.setId(ID_HSR_TIME_TABLE_TABLE);
 			RelativeLayout.LayoutParams svparam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
 			svparam.addRule(RelativeLayout.BELOW, ID_HSR_TIME_TABLE_TITLE);
 			svparam.addRule(RelativeLayout.ABOVE, ID_PROVIDER_ANNOUNCEMENT);
 			sv.setLayoutParams(svparam);
 			rl.addView(sv);
 
 			process_param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 			process_param.addRule(RelativeLayout.BELOW, ID_HSR_TIME_TABLE_TITLE);
 			process_param.addRule(RelativeLayout.CENTER_HORIZONTAL);
 			process.setLayoutParams(process_param);
 			rl.addView(process);
 
 			/* End of 畫Layout */
 
 			DownloadWebPageTask task = new DownloadWebPageTask();
 			task.execute(new String[] {"http://www.thsrc.com.tw/tw/Operation"});
 
 			HSR_TIMETABLE_PARSER timetable = new HSR_TIMETABLE_PARSER (new AnalysisResult() {
 				@Override
 				public void parsestr(String result) {
 					return;
 				}
 
 				@Override
 				public void parsedBUS(List<BusRoute> routes) {
 					return;
 				}
 
 				@Override
 				public void parsedHSR(List<HSRTrains> trains) {
 					rl.removeView(process);
 
 					boolean selected = false;
 					int color = Color.WHITE;
 					/* 時刻表捲單 */
 					TableLayout tl = new TableLayout(pop_transit.this);
 					tl.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 					tl.setOrientation(TableLayout.VERTICAL);
 
 					for(HSRTrains train : trains) {
 						/* 確定方向 */
 						if(southbound != train.southbound)
 							continue;
 
 						int dept_time = train.get_time_by_station(dept);
 						int arr_time = train.get_time_by_station(arr);
 
 						/* 確定有停 */
 						if(dept_time < 0 || arr_time < 0)
 							continue;
 
 						final TableRow tr = CreateTableRow(tl);
 						tr.setBackgroundColor(color);
 						color = (color == Color.LTGRAY) ? Color.WHITE : Color.LTGRAY;
 
 						/* 建議icon */
 						ImageView iv = new ImageView(pop_transit.this);
 						if(selected == false && dept_time >= current_min) {
 							iv.setImageResource(R.drawable.marker);
 							selected = true;
 							sv.post(new Runnable() {
 								@Override
 								public void run() {
 									sv.smoothScrollTo(0, tr.getTop());
 								} 
 							});
 						}
 						else
 							iv.setImageResource(0);
 						iv.setBackgroundColor(Color.TRANSPARENT);
 						iv.setMaxHeight((int) (20 * getResources().getDisplayMetrics().density));
 						iv.setMaxWidth((int) (20 * getResources().getDisplayMetrics().density));
 						iv.setAdjustViewBounds(true);
 						iv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.05f));
 						tr.addView(iv);
 
 						/* 車次 */
 						TextView tv = new TextView(pop_transit.this);
 						tv.setText(String.valueOf(train.id));
 						tv.setTextColor(Color.BLACK);
 						tv.setTextSize(16);
 						tv.setHorizontallyScrolling(false);
 						tv.setWidth(0);
 						tv.setGravity(Gravity.CENTER);
 						tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.15f));
 						tr.addView(tv);
 
 						/* (出發)HH:MM */
 						tv = new TextView(pop_transit.this);
 						tv.setText(train.minutes2str(dept_time));
 						tv.setTextColor(Color.BLACK);
 						tv.setTextSize(16);
 						tv.setHorizontallyScrolling(false);
 						tv.setWidth(0);
 						tv.setGravity(Gravity.CENTER);
 						tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.25f));
 						tr.addView(tv);
 
 						/* (抵達)HH:MM */
 						tv = new TextView(pop_transit.this);
 						tv.setText(train.minutes2str(arr_time));
 						tv.setTextColor(Color.BLACK);
 						tv.setTextSize(16);
 						tv.setHorizontallyScrolling(false);
 						tv.setWidth(0);
 						tv.setGravity(Gravity.CENTER);
 						tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.25f));
 						tr.addView(tv);
 
 						/* 行駛時間 */
 						tv = new TextView(pop_transit.this);
 						tv.setText(train.minutes2hour(arr_time - dept_time));
 						tv.setTextColor(Color.BLACK);
 						tv.setTextSize(16);
 						tv.setHorizontallyScrolling(false);
 						tv.setWidth(0);
 						tv.setGravity(Gravity.CENTER);
 						tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.30f));
 						tr.addView(tv);
 					}
 					sv.addView(tl);
 					loading.setVisibility(ProgressBar.INVISIBLE);
 				}
 
 				@Override
 				public void parsedTimeTable(List<TimeTable> time) {
 				}
 
 				@Override
 				public void TraTimeTable(List<TrainTimeTable> time) {
 				}
 			});
 			timetable.execute(url);
 		}
 		/* 公車 客運 */
 		else {
 
 			if(check_agency(agency, bus_taipei, line, name)) {
 				String encode;
 
 				if(line.contentEquals("三鶯線先導公車"))
 					encode = "三鶯捷運先導公車";
 				else if(line.contentEquals("108區"))
 					encode = "108區(二子坪)";
 				else if(line.contentEquals("小9"))
 					encode = "小9 (台灣好行-北投竹子湖)";
 				else if(Current_time.hour < 20 && Current_time.hour > 6 && line.contentEquals("橘18")) 
 					encode = "橘18福隆路";		// 為了較好的效能..
 				else
 					encode = line;
 
 				encode.replace("內科通勤", "內科通勤專車");
 
 				String tpe_bus_url = "http://pda.5284.com.tw/MQS/businfo2.jsp?routeId={0}";
 				try {
 					final String url = MessageFormat.format(tpe_bus_url, URLEncoder.encode(encode, "UTF-8"));
 					/* 設定activity title, ex: 226 即時資訊 */
 					this.setTitle(line + " " + getResources().getString(R.string.realtime_info));
 
 					//					create_webview_by_url(url);
 
 					rl.removeAllViews();
 
 					rl.addView(process);
 
 					/* 資料由5284我愛巴士提供 */
 					show_info_provider(R.string.provide_by_5284);
 
 					final ScrollView sv = new ScrollView(this);
 					RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
 					param.addRule(RelativeLayout.ABOVE, ID_PROVIDER_ANNOUNCEMENT);
 					sv.setLayoutParams(param);
 					rl.addView(sv);
 
 					final TableLayout tl = new TableLayout(this);
 					tl.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 					tl.setOrientation(TableLayout.VERTICAL);
 
 					sv.addView(tl);
 
 					/* Update every 30 seconds */
 					runtask = new Runnable() {
 						public void run () {
 							TPE_BUS_PARSER task = new TPE_BUS_PARSER(new AnalysisResult() {
 								@Override
 								public void parsestr(String result) {
 									return;
 								}
 
 								@Override
 								public void parsedBUS(List<BusRoute> routes) {
 									rl.removeView(process);
 
 									find_start_dest(routes, dept, arr);
 									create_realtime_table(routes, tl, sv);
 									loading.setVisibility(ProgressBar.INVISIBLE);
 									return;
 								}
 
 								@Override
 								public void parsedHSR(List<HSRTrains> trains) {
 								}
 
 								@Override
 								public void parsedTimeTable(List<TimeTable> time) {
 								}
 
 								@Override
 								public void TraTimeTable(
 										List<TrainTimeTable> time) {
 								}
 							});
 							loading.setVisibility(ProgressBar.VISIBLE);
 							task.execute(url);
 							handler.postDelayed(this, 30000);
 						}
 					};
 					handler.post(runtask);
 
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 					Toast.makeText(this, getResources().getString(R.string.info_internal_error) , Toast.LENGTH_LONG).show();
 					finish();
 				}
 			}
 			else if(check_agency(agency, bus_taichung, line, name)) {
 				String txg_bus_url = "http://citybus.taichung.gov.tw/pda/aspx/businfomation/roadname_roadline.aspx?ChoiceRoute=0&line={0}&lang=CHT&goback={1}&route={0}";
 				try {
 					final String url_go = MessageFormat.format(txg_bus_url, URLEncoder.encode(line, "UTF-8"), "1");
 					final String url_bk = MessageFormat.format(txg_bus_url, URLEncoder.encode(line, "UTF-8"), "2");
 
 					/* 設定activity title, ex: 226 即時資訊 */
 					this.setTitle(line + " " + getResources().getString(R.string.realtime_info));
 
 					rl.removeAllViews();
 
 					rl.addView(process);
 
 					/* 資料由台中市政府提供 */
 					show_info_provider(R.string.provide_by_txg);
 
 					final ScrollView sv = new ScrollView(this);
 					RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
 					param.addRule(RelativeLayout.ABOVE, ID_PROVIDER_ANNOUNCEMENT);
 					sv.setLayoutParams(param);
 					rl.addView(sv);
 
 					final TableLayout tl = new TableLayout(this);
 					tl.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 					tl.setOrientation(TableLayout.VERTICAL);
 
 					sv.addView(tl);
 
 					/* Update every 30 seconds */
 					runtask = new Runnable() {
 						public void run () {
 							TXN_BUS_PARSER task = new TXN_BUS_PARSER(new AnalysisResult() {
 								@Override
 								public void parsestr(String result) {
 									return;
 								}
 
 								@Override
 								public void parsedBUS(List<BusRoute> routes) {
 									rl.removeView(process);
 
 									find_start_dest(routes, dept, arr);
 									create_realtime_table(routes, tl, sv);
 									loading.setVisibility(ProgressBar.INVISIBLE);
 									return;
 								}
 
 								@Override
 								public void parsedHSR(List<HSRTrains> trains) {
 								}
 
 								@Override
 								public void parsedTimeTable(List<TimeTable> time) {
 								}
 
 								@Override
 								public void TraTimeTable(
 										List<TrainTimeTable> time) {
 								}
 							});
 							task.execute(url_go, url_bk);
 							loading.setVisibility(ProgressBar.VISIBLE);
 							handler.postDelayed(this, 30000);
 						}
 					};
 					handler.post(runtask);
 
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 					Toast.makeText(this, getResources().getString(R.string.info_internal_error) , Toast.LENGTH_LONG).show();
 					finish();
 				}
 			}
 			else if(check_agency(agency, bus_kaohsiung, line, name)) {
 				String encode;
 				/* Workaround...偉哉陳菊... */
 				if(line.contentEquals("168東"))
 					encode = "168環東";
 				else if(line.contentEquals("168西"))
 					encode = "168環西";
 				else if(line.contentEquals("民族幹線")) {
 					encode = "025民族";
 					line = "25民族幹線";
 				}
 				else if(line.contentEquals("205")) {
 					encode = "205中華";
 					line = "205中華幹線";
 				}
 				else if(line.contentEquals("36"))
 					encode = "36復興幹線";
 				else if(line.contentEquals("50"))
 					encode = "50五福";
 				else if(line.contentEquals("88"))
 					encode = "88建國";
 				else if(line.contentEquals("92"))
 					encode = "92自由";
 				else if(line.contentEquals("旗美快捷"))
 					encode = "旗美國道快捷公車";
 				else if(line.contentEquals("旗山快捷"))
 					encode = "旗山國道快捷公車";
 				else
 					encode = line;
 
 				if(line.contentEquals("16A") && Current_time.hour > 17 && Current_time.minute > 30) {
 					encode = "16A(1730-2330繞文信路口)";
 				}
 				else if(line.contentEquals("紅36") && Current_time.hour > 17) {
 					encode = "紅36繞駛";
 				}
 				else if(line.contentEquals("3") && Current_time.hour > 17) {
 					encode = "3(繞文信路)";
 				}
 				else if(line.contentEquals("橘12") && Current_time.weekDay == 2 && Current_time.hour > 16) {
 					encode = "橘12繞駛";
 				}
 
 				//				String khh_bus_url = "http://122.146.229.210/bus/pda/businfo.aspx?Routeid={0}&GO_OR_BACK=1&Line=All&lang=Cht";
 
 				String xml_bus_route = "http://122.146.229.210/xmlbus2/GetEstimateTime.xml?routeIds={0}";
 
 				/* 設定activity title, ex: 226 即時資訊 */
 				this.setTitle(line + " " + getResources().getString(R.string.realtime_info));
 
 				try {
 					final String url = MessageFormat.format(xml_bus_route, URLEncoder.encode(encode, "UTF-8"));
 					rl.removeAllViews();
 
 					rl.addView(process);
 
 					/* 資料由高雄市政府提供 */
 					show_info_provider(R.string.provide_by_khh);
 
 					final ScrollView sv = new ScrollView(this);
 					RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
 					param.addRule(RelativeLayout.ABOVE, ID_PROVIDER_ANNOUNCEMENT);
 					sv.setLayoutParams(param);
 					rl.addView(sv);
 
 					final TableLayout tl = new TableLayout(this);
 					tl.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 					tl.setOrientation(TableLayout.VERTICAL);
 
 					sv.addView(tl);
 
 					/* Update every 30 seconds */
 					runtask = new Runnable() {
 						public void run () {
 							HTML_BUS_PARSER task = new HTML_BUS_PARSER(
 									new AnalysisResult() {
 										@Override
 										public void parsestr(String result) {
 											khh_bus_xml(result, tl, sv);
 											loading.setVisibility(ProgressBar.INVISIBLE);
 										}
 
 										@Override
 										public void parsedBUS(List<BusRoute> routes) {
 											return;
 										}
 
 										@Override
 										public void parsedHSR(List<HSRTrains> trains) {
 											return;
 										}
 
 										@Override
 										public void parsedTimeTable(List<TimeTable> time) {
 										}
 
 										@Override
 										public void TraTimeTable(
 												List<TrainTimeTable> time) {
 										}
 									});
 							task.execute(url);
 							loading.setVisibility(ProgressBar.VISIBLE);
 							handler.postDelayed(this, 30000);
 						}
 					};
 					handler.post(runtask);
 
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 					finish();
 				}
 			}
 			else if(check_agency(agency, bus_taoyuang, line, name)) {
 				String txg_bus_url = "http://124.199.77.90/Taoyuan/PDA/businfo.aspx?Routeid={0}&GO_OR_BACK={1}&Line={0}&lang=Cht";
 				try {
 					final String url_go = MessageFormat.format(txg_bus_url, URLEncoder.encode(line, "UTF-8"), "1");
 					final String url_bk = MessageFormat.format(txg_bus_url, URLEncoder.encode(line, "UTF-8"), "2");
 
 					/* 設定activity title, ex: 226 即時資訊 */
 					this.setTitle(line + " " + getResources().getString(R.string.realtime_info));
 
 					//					create_webview_by_url(url);
 
 					rl.removeAllViews();
 
 					rl.addView(process);
 
 					/* 資料由桃園縣政府提供 */
 					show_info_provider(R.string.provide_by_tyn);
 
 					final ScrollView sv = new ScrollView(this);
 					RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
 					param.addRule(RelativeLayout.ABOVE, ID_PROVIDER_ANNOUNCEMENT);
 					sv.setLayoutParams(param);
 					rl.addView(sv);
 
 					final TableLayout tl = new TableLayout(this);
 					tl.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 					tl.setOrientation(TableLayout.VERTICAL);
 
 					sv.addView(tl);
 
 					/* Update every 30 seconds */
 					runtask = new Runnable() {
 						public void run () {
 							TYN_BUS_PARSER task = new TYN_BUS_PARSER(new AnalysisResult() {
 								@Override
 								public void parsestr(String result) {
 									return;
 								}
 
 								@Override
 								public void parsedBUS(List<BusRoute> routes) {
 									rl.removeView(process);
 
 									find_start_dest(routes, dept, arr);
 									create_realtime_table(routes, tl, sv);
 									loading.setVisibility(ProgressBar.INVISIBLE);
 									return;
 								}
 
 								@Override
 								public void parsedHSR(List<HSRTrains> trains) {
 								}
 
 								@Override
 								public void parsedTimeTable(List<TimeTable> time) {
 								}
 
 								@Override
 								public void TraTimeTable(
 										List<TrainTimeTable> time) {
 								}
 							});
 							task.execute(url_go, url_bk);
 							loading.setVisibility(ProgressBar.VISIBLE);
 							handler.postDelayed(this, 30000);
 						}
 					};
 					handler.post(runtask);
 
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 					Toast.makeText(this, getResources().getString(R.string.info_internal_error) , Toast.LENGTH_LONG).show();
 					finish();
 				}
 			}
 			else if(line.matches("[0-9]{4}")) {
 				final Time date = new Time();
 				date.set(current_time_millis > time * 1000 ? current_time_millis : time * 1000);
 				
 				String url_runid;
 				if(getResources().getString(R.string.locale).contentEquals("English"))
 					url_runid = "http://www.taiwanbus.tw/aspx/dyBus/BusXMLLinee.aspx?Mode=6&Cus=&RouteNo={0}";
 				else
 					url_runid = "http://www.taiwanbus.tw/aspx/dyBus/BusXMLLine.aspx?Mode=6&Cus=&RouteNo={0}";
 				final ArrayList<String> runid = new ArrayList<String>();
 				/* 拿到1915的run ID: http://www.taiwanbus.tw/aspx/dyBus/BusXMLLine.aspx?Mode=6&Cus=&RouteNo=1915 */
 
 				rl.removeAllViews();
 
 				rl.addView(process);
 
 				final TextView tv = new TextView(this);
 				tv.setId(ID_INTERCITYBUS_CARRIER);
 				tv.setTextColor(Color.WHITE);
 				tv.setTextSize(16);
 				tv.setGravity(Gravity.LEFT);
 				RelativeLayout.LayoutParams tvparam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 				tv.setLayoutParams(tvparam);
 				tv.setHorizontallyScrolling(false);
 				rl.addView(tv);
 
 				final ScrollView sv = new ScrollView(this);
 				tvparam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
 				sv.setLayoutParams(tvparam);
 
 				final ScrollView sv_timetb = new ScrollView(this);
 				tvparam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
 				tvparam.addRule(RelativeLayout.BELOW, ID_BUS_TIMETABLE_DATE);
 				sv_timetb.setLayoutParams(tvparam);
 
 				final TableLayout tl_timetb = new TableLayout(this);
 				tl_timetb.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 				tl_timetb.setOrientation(TableLayout.VERTICAL);
 				tl_timetb.setWeightSum(1.0f);
 				tl_timetb.setStretchAllColumns(true);
 
 				final TableLayout tl = new TableLayout(this);
 				tl.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 				tl.setOrientation(TableLayout.VERTICAL);
 
 				sv.addView(tl);
 				sv_timetb.addView(tl_timetb);
 
 				/* 設定activity title, ex: 9117 時刻表 */
 				this.setTitle(line + " " + getResources().getString(R.string.details));
 
 				/* 資料由公路總局提供 */
 				show_info_provider(R.string.provide_by_bus);
 
 				final TabHost tabs = new TabHost(pop_transit.this, null);
 				RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 				param.addRule(RelativeLayout.BELOW, ID_INTERCITYBUS_CARRIER);
 				param.addRule(RelativeLayout.ABOVE, ID_PROVIDER_ANNOUNCEMENT);
 				tabs.setLayoutParams(param);
 				tabs.setId(android.R.id.tabhost);
 				rl.addView(tabs);
 
 				LinearLayout lineLayout = new LinearLayout(this);
 				lineLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
 						LinearLayout.LayoutParams.MATCH_PARENT));
 				lineLayout.setOrientation( LinearLayout.VERTICAL);
 				lineLayout.setGravity( Gravity.TOP );
 
 				TabWidget tabWidget = new TabWidget(pop_transit.this);
 				param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 				tabWidget.setLayoutParams(param);
 				tabWidget.setId(android.R.id.tabs);
 				lineLayout.addView(tabWidget);
 
 				FrameLayout tabContent = new FrameLayout(pop_transit.this);
 				tabContent.setLayoutParams(param);
 				tabContent.setId(android.R.id.tabcontent);
 				lineLayout.addView(tabContent);
 
 				tabs.addView(lineLayout);
 				tabs.setup();
 
 				Drawable dr = getResources().getDrawable(R.drawable.busstop);
 				Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
 
 				Drawable drawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 
 						(int) (32 * getResources().getDisplayMetrics().density), (int) (32 * getResources().getDisplayMetrics().density), true)); 
 
 				TabSpec tspec1 = tabs.newTabSpec("Tab1");
 				tspec1.setIndicator(getResources().getString(R.string.realtime_info), drawable);
 				tspec1.setContent(new PreExistingViewFactory(sv));
 				tabs.addTab(tspec1);
 
 				dr = getResources().getDrawable(R.drawable.timetable);
 				bitmap = ((BitmapDrawable) dr).getBitmap();
 
 				drawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 
 						(int) (32 * getResources().getDisplayMetrics().density), (int) (32 * getResources().getDisplayMetrics().density), true)); 
 
 				TabSpec tspec2 = tabs.newTabSpec("Tab2");
 				tspec2.setIndicator(getResources().getString(R.string.time_table), drawable);
 				tspec2.setContent(new PreExistingViewFactory(sv_timetb));
 				tabs.addTab(tspec2);
 
 				gestureDetector = new GestureDetector(this, new MyGestureDetector(tabs));
 				gestureListener = new View.OnTouchListener() {
 					public boolean onTouch(View v, MotionEvent event) {
 						return gestureDetector.onTouchEvent(event);
 					}
 				};
 				sv.setOnTouchListener(gestureListener);
 				sv_timetb.setOnTouchListener(gestureListener);
 
 				tabs.setLongClickable(true);
 				tabs.setOnTabChangedListener(new AnimatedTabHostListener(getBaseContext(), tabs));
 
 				try {
 					String url = MessageFormat.format(url_runid, URLEncoder.encode(line, "UTF-8"), line);
 					HTML_BUS_PARSER get_runid = new HTML_BUS_PARSER(
 							new AnalysisResult() {
 								@Override
 								public void parsestr(String result) {
 									result = result.replace('|', '&');
 									Log.i(TAG, "result=" + result);
 									final String[] branches = result.split("&");
 									List<String> listItems = new ArrayList<String>();
 
 									for(int i=0; i<branches.length; i++) {
 										Log.i(TAG, "branches[i]=" + branches[i]);
 										String[] infos1 = branches[i].split(",");
 										if(getResources().getString(R.string.locale).contentEquals("English")) {
 											if(infos1.length < 4)
 												continue;
 										}
 										else {
 											if(infos1.length < 5)
 												continue;
 											if(i == 0)
 												tv.setText(String.format("%s: %s", getResources().getString(R.string.carrier), infos1[4]));
 										}
 										/* 3126,32,0,台北→高雄,阿羅哈客運 */
 										if(i+1 < branches.length) {
 											String[] infos2 = branches[i+1].split(",");
 											if(infos1[2].equals(infos2[2])) {	// 有回頭車
 												Log.i(TAG, "有回頭車");
 												runid.add(String.format("%s,%s,%s", infos1[2], infos1[0], infos2[0]));
 												listItems.add(infos1[3].replaceAll("→", "-"));
 												i++;
 												continue;
 											}
 										}
 										else {	// 沒有回頭車
 											Log.i(TAG, "沒有回頭車");
 											runid.add(String.format("%s,%s", infos1[2], infos1[0]));
 											listItems.add(infos1[3]);
 										}
 									}
 
 									if(runid.size() >= 2) {
 										// 有支線...
 										final CharSequence[] headway = listItems.toArray(new CharSequence[listItems.size()]);;
 										AlertDialog.Builder dialog = new AlertDialog.Builder(new ContextThemeWrapper(pop_transit.this, R.style.ThemeWithCorners));
 										dialog.setTitle(getResources().getString(R.string.choose_line));
 										dialog.setItems(headway, new DialogInterface.OnClickListener() {
 											public void onClick(DialogInterface dialog, int branch) {
 												String[] lines = runid.get(branch).split(",");
 
 												if(lines.length == 3)
 													get_real_time(headway[branch], lines[0], lines[1], lines[2]);
 												else
 													get_real_time(headway[branch], lines[0], lines[1], null);
 											}
 										});
 
 										dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {         
 											@Override
 											public void onCancel(DialogInterface dialog) {
 												finish();
 											}
 										});
 										dialog.show();
 									}
 									else if (runid.size() == 1){
 										String[] lines = runid.get(0).split(",");
 
 										if(lines.length == 3)
 											get_real_time(listItems.get(0), lines[0], lines[1], lines[2]);
 										else
 											get_real_time(listItems.get(0), lines[0], lines[1], null);
 									}
 									else {
 										Toast.makeText(pop_transit.this, getResources().getString(R.string.no_data) , Toast.LENGTH_LONG).show();
 										finish();
 									}
 								}
 
 								@Override
 								public void parsedBUS(List<BusRoute> routes) {
 									return;
 								}
 
 								@Override
 								public void parsedHSR(List<HSRTrains> trains) {
 									return;
 								}
 
 								private void get_real_time(CharSequence headway, final String catgory, final String runid_go, final String runid_back) {
 									/* 即時 http://www.taiwanbus.tw/aspx/dyBus/BusXMLLine.aspx?Mode=4&RunId=3362 */
 									Log.i(TAG, String.format("%s %s ", runid_go, runid_back));
 									String url, url_time_table;
 									if(getResources().getString(R.string.locale).contentEquals("English")) {
 										url = "http://www.taiwanbus.tw/aspx/dyBus/BusXMLLinee.aspx?Mode=4&RunId={0}";
 										url_time_table = "http://web.taiwanbus.tw/eBUS/subsystem/Timetable/TimeTableAPIByWeek.aspx?inputType=R01&RouteId={0}%20&RouteBranch={1}&SearchDate={2}&Language=en";
 									}
 									else {
 										url = "http://www.taiwanbus.tw/aspx/dyBus/BusXMLLine.aspx?Mode=4&RunId={0}";
 										url_time_table = "http://web.taiwanbus.tw/eBUS/subsystem/Timetable/TimeTableAPIByWeek.aspx?inputType=R01&RouteId={0}%20&RouteBranch={1}&SearchDate={2}";
 									}
 
 									try {
 										/* 時刻表 */
 										final String str_date = String.format("%d/%02d/%02d", date.year, date.month + 1, date.monthDay);
 										final String url_tt = MessageFormat.format(url_time_table, URLEncoder.encode(line, "UTF-8"), catgory, str_date);
 										Log.i(TAG, url_tt);
 										INTERCITY_TIME_TABLE get_timetable = new INTERCITY_TIME_TABLE(
 												new AnalysisResult() {
 													@Override
 													public void parsestr(String result) {
 													}
 
 													@Override
 													public void parsedBUS(List<BusRoute> routes) {
 														return;
 													}
 
 													@Override
 													public void parsedHSR(List<HSRTrains> trains) {
 														return;
 													}
 
 													@Override
 													public void parsedTimeTable(List<TimeTable> time) {
 														String carrier = create_time_table(time, tl_timetb, sv_timetb, date);
 														if(getResources().getString(R.string.locale).contentEquals("English"))
 															tv.setText(String.format("%s: %s", getResources().getString(R.string.carrier), carrier));
 													}
 
 													@Override
 													public void TraTimeTable(
 															List<TrainTimeTable> time) {
 													}
 												});
 										get_timetable.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url_tt);
 
 										/* 即時資訊 */
 										final String url_go = MessageFormat.format(url, URLEncoder.encode(runid_go, "UTF-8"));
 										final String url_bk = runid_back == null ? null : MessageFormat.format(url, URLEncoder.encode(runid_back, "UTF-8"));
 										runtask = new Runnable() {
 											public void run () {
 												INTERCITY_BUS_PARSER task = new INTERCITY_BUS_PARSER(new AnalysisResult() {
 													@Override
 													public void parsestr(String result) {
 														return;
 													}
 
 													@Override
 													public void parsedBUS(List<BusRoute> routes) {
 														rl.removeView(process);
 
 														find_start_dest(routes, dept, arr);
 														create_realtime_table(routes, tl, sv);
 														loading.setVisibility(ProgressBar.INVISIBLE);
 														return;
 													}
 
 													@Override
 													public void parsedHSR(List<HSRTrains> trains) {
 													}
 
 													@Override
 													public void parsedTimeTable(List<TimeTable> time) {
 													}
 
 													@Override
 													public void TraTimeTable(
 															List<TrainTimeTable> time) {
 													}
 												});
 												if(url_bk != null)
 													task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url_go, url_bk);
 												else
 													task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url_go);
 												loading.setVisibility(ProgressBar.VISIBLE);
 												handler.postDelayed(this, 30000);
 											}
 										};
 										handler.post(runtask);
 									} catch (Exception e) {
 										e.printStackTrace();
 										Toast.makeText(pop_transit.this, getResources().getString(R.string.info_internal_error) , Toast.LENGTH_LONG).show();
 										finish();
 									}
 								}
 
 								@Override
 								public void parsedTimeTable(List<TimeTable> time) {
 								}
 
 								@Override
 								public void TraTimeTable(
 										List<TrainTimeTable> time) {
 								}
 							});
 					
 					get_runid.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 					Toast.makeText(this, getResources().getString(R.string.info_internal_error) , Toast.LENGTH_LONG).show();
 					finish();
 				}
 
 			}
 			/* 其他狀況: 施工中... */
 			else {
 				/* 還沒做...QQ */
 				TextView tv = new TextView(this);
 				tv.setText(getResources().getString(R.string.realtime_under_construction) + "\n路線:" + line + "\n業者:" + agency);
 				tv.setTextColor(Color.WHITE);
 				tv.setTextSize(16);
 				tv.setGravity(Gravity.CENTER);
 				tv.setHorizontallyScrolling(false);
 
 				RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 				param.addRule(RelativeLayout.CENTER_IN_PARENT);
 				tv.setLayoutParams(param);
 
 				rl.addView(tv);
 			}
 		}
 	}
 
 	private int find_station_by_zhname(String station) {
 		String zh_stations[] = getResources().getStringArray(R.array.zh_station);
 		int i = 0;
 		boolean matched = false;
 		
 		if(station.matches("臺[北中南東]"))
 			station = station.replace("臺", "台");
 
 		Log.i(TAG, "station=" + station);
 
 		for(i = 0; i < zh_stations.length; i++) {
 			if(zh_stations[i].contentEquals(station)) {
 				matched = true;
 				break;
 			}
 		}
 		if(matched)
 			return i;
 		else
 			return -1;
 	}
 
 	@Override
 	protected void onResume() {
 		if(runtask != null) 
 			handler.post(runtask);
 		super.onResume();
 	}
 
 	@Override
 	protected void onStop() {
 		if(runtask != null)
 			handler.removeCallbacks(runtask);
 		super.onStop();
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 	}
 
 	private void khh_bus_xml(String result, TableLayout tl, ScrollView sv) {
 		/* 高雄市政府opendata xml */
 		RelativeLayout rl = (RelativeLayout)findViewById(R.id.rl_pop_transit);
 		InputStream is;
 
 		try {
 			is = new ByteArrayInputStream(result.getBytes("UTF-8"));
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return;
 		}
 
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
 		DocumentBuilder builder;
 		try {
 			builder = factory.newDocumentBuilder();
 			Document doc = builder.parse(is);
 
 			doc.getDocumentElement().normalize();  
 			NodeList nlRoot = doc.getElementsByTagName("BusDynInfo");
 			Element eleRoot = (Element)nlRoot.item(0);
 
 			//			String update_time = doc.getElementsByTagName("UpdateTime").item(0).getChildNodes().item(0).getNodeValue();
 
 			NodeList route = eleRoot.getElementsByTagName("EstimateTime");  
 			int routeLen = route.getLength();  
 			if(routeLen == 0) {
 				TextView textv = new TextView(this);
 				textv.setText(getResources().getString(R.string.no_data));
 				textv.setTextColor(Color.WHITE);
 				textv.setTextSize(20);
 				textv.setGravity(Gravity.CENTER);
 				textv.setHorizontallyScrolling(false);
 
 				rl.addView(textv);
 			}
 			List<BusRoute> routes = new ArrayList<BusRoute>();
 
 			for(int i = 0; i < routeLen; i++) {
 				Element station = (Element) route.item(i);
 
 				String name = station.getAttribute("StopName");
 				String goback = station.getAttribute("GoBack");
 				String seqnum = station.getAttribute("seqNo");
 				String wait_time = station.getAttribute("Value");
 				String come_time = station.getAttribute("comeTime");
 				String car_id = station.getAttribute("carId");
 
 				if(come_time.length() == 0)
 					come_time = getResources().getString(R.string.no_service);
 
 				routes.add(new BusRoute(name, Integer.valueOf(goback), 
 						Integer.valueOf(seqnum), wait_time,
 						come_time, car_id));
 			}
 			rl.removeView(process);
 			find_start_dest(routes, dept, arr);
 			create_realtime_table(routes, tl, sv);
 
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			e.printStackTrace();
 			return;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return;
 		}  
 	}
 
 	private void tra_find_start_dest(List<TrainTable> routes, String dept, String arr) {
 		dept = string_replace(dept);
 		arr = string_replace(arr);
 
 		dept = dept.replaceAll("[火]?車站","");
 		arr = arr.replaceAll("[火]?車站","");
 		Log.i(TAG, dept + " " + arr);
 
 		for (TrainTable temp : routes) {
 			if(temp.station.contentEquals(dept))
 				temp.set_start();
 			else if(temp.station.contentEquals(arr))
 				temp.set_destination();
 		}
 	}  
 
 	private void find_start_dest(List<BusRoute> routes, String dept, String arr) {
 		BusRoute start = null, end = null;
 		int maxtime = 999;
 		boolean match_depart = false, match_arr = false;
 
 		dept = string_replace(dept);
 		arr = string_replace(arr);
		
		Log.i(TAG, String.format("%s:%s", dept, arr));
 
 		for (BusRoute temp : routes) {
 			/* Check for where the buses are */
 			if(temp.Value.contentEquals("null"))
 				maxtime = 999;
 			else {
 				int wait_time = Integer.parseInt(temp.Value);
 				if(wait_time < maxtime) {
 					temp.set_car();
 				}
 				maxtime = wait_time;
 			}
 
 			String stopname = temp.StopName;
			Log.i(TAG, "stop name=" + stopname);
 
 			/* Check for departure/arrival stops */
 			if(end == null && ( dept.contains(stopname) || stopname.contains(dept) )) {
 				if(dept.contentEquals(stopname)) {
 					start = temp;
 					match_depart = true;
 				}
 				else if(match_depart == false)
 					start = temp;
				
				Log.i(TAG, "match start! " + start.StopName);
 			}
 			else if(start != null && ( arr.contains(stopname) || stopname.contains(arr) )) {
 				if(match_arr == false && arr.contentEquals(stopname)) {
 					end = temp;
 					match_arr = true;
 				}
 				else if(end == null && match_arr == false) {
 					end = temp;
 				}
				Log.i(TAG, "match end! " + end.StopName);
 			}
 		}
 		if(start != null && end != null) {
 			start.set_start();
 			end.set_destination();
 		}
 		else if(timetable.isEmpty()) {
 			Log.w(TAG, "無法標記起訖點");
 			err_tag_fail=true;
 		}
 	}  
 
 	private void create_tra_timetable(List<TrainTimeTable> routes, TableLayout tl, final ScrollView sv, final TabHost tab, final TableLayout real_tl, final RelativeLayout realtime) {
 		for(final TrainTimeTable train : routes) {
 			final TableRow tr = CreateTableRow(tl);
 			tr.setBackgroundResource(R.drawable.seletor_white);
 			
 			// 車種
 			final TextView type = new TextView(this);
 			type.setTextSize(16);
 			type.setHorizontallyScrolling(false);
 			type.setWidth(0);
 			type.setGravity(Gravity.CENTER);
 			type.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, (int) (32 * getResources().getDisplayMetrics().density), 0.15f));
 			tr.addView(type);
 
 			if(train.type.contentEquals("自強")) {
 				type.setTextColor(Color.RED);
 				type.setText(R.string.TC);
 			}
 			else if(train.type.contentEquals("莒光")) {
 				type.setTextColor(Color.rgb(0xdc, 0x8c, 0x00));
 				type.setText(R.string.CK);
 			}
 			else if(train.type.contentEquals("區間車")) {
 				type.setTextColor(Color.BLACK);
 				type.setText(R.string.Local);
 			}
 			else if(train.type.contentEquals("區間快")) {
 				type.setTextColor(Color.BLACK);
 				type.setText(R.string.Local);
 			}
 			else {
 				type.setTextColor(Color.BLACK);
 				type.setText(train.type);
 			}
 
 			// 車次
 			TextView tv = new TextView(this);
 			tv.setTextColor(Color.BLACK);
 			tv.setText(train.id);
 			tv.setTextSize(16);
 			tv.setHorizontallyScrolling(false);
 			tv.setWidth(0);
 			tv.setGravity(Gravity.CENTER);
 			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.15f));
 			tr.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View arg0) {
 					line=train.id;
 					handler.removeCallbacks(runtask);
 					real_tl.removeAllViews();
 					timetable.clear();
 					realtime.addView(process);
 					handler.post(runtask);
 					tab.setCurrentTab(0);
 					if(getResources().getString(R.string.locale).contentEquals("English"))
 						/* 設定activity title, ex: 自強 123 */
 						pop_transit.this.setTitle(train.type + " " + line);
 					else
 						pop_transit.this.setTitle(type.getText().toString() + " " + line);
 				}
 			});
 			tr.addView(tv);
 
 			// 起站
 			tv = new TextView(this);
 			tv.setTextColor(Color.BLACK);
 			tv.setText(train.depart_time);
 			tv.setTextSize(16);
 			tv.setHorizontallyScrolling(false);
 			tv.setWidth(0);
 			tv.setGravity(Gravity.CENTER);
 			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.25f));
 			tr.addView(tv);
 
 			// 迄站
 			tv = new TextView(this);
 			tv.setTextColor(Color.BLACK);
 			tv.setText(train.arr_time);
 			tv.setTextSize(16);
 			tv.setHorizontallyScrolling(false);
 			tv.setWidth(0);
 			tv.setGravity(Gravity.CENTER);
 			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.25f));
 			tr.addView(tv);
 
 			// 行駛時間
 			tv = new TextView(this);
 			tv.setTextColor(Color.BLACK);
 			tv.setText(train.duration);
 			tv.setTextSize(16);
 			tv.setHorizontallyScrolling(false);
 			tv.setWidth(0);
 			tv.setGravity(Gravity.CENTER);
 			tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.20f));
 			tr.addView(tv);
 		}
 		tl.invalidate();
 	}
 
 	private void create_tratime_table(List<TrainTable> routes, TableLayout tl, final ScrollView sv) {
 		boolean first_read = false;
 		int train_index = 0;
 		String en_stations[] = getResources().getStringArray(R.array.en_station_id);
 		String cn_stations[] = getResources().getStringArray(R.array.cn_station);
 
 		if(timetable.isEmpty()) {
 			first_read = true;
 			for(int i = 0; i < routes.size(); i++){
 				TrainTable temp = routes.get(i);
 				final TableRow tr = CreateTableRow(tl);
 				tr.setBackgroundColor(Color.WHITE);
 				tr.setWeightSum(1.0f);
 				temp.set_tablerow(tr);
 				//  起訖站icon
 				ImageView iv = new ImageView(this);
 				if(temp.start)
 					iv.setImageResource(R.drawable.start);
 				else if (temp.destination)
 					iv.setImageResource(R.drawable.destination);
 				else {
 					iv.setImageResource(R.drawable.start);
 					iv.setAlpha(0);
 				}
 				iv.setMaxHeight((int) (20 * getResources().getDisplayMetrics().density));
 				iv.setMaxWidth((int) (20 * getResources().getDisplayMetrics().density));
 				iv.setAdjustViewBounds(true);
 				iv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.05f));
 				tr.addView(iv);
 
 				// 站名
 				TextView tv = new TextView(this);
 				tv.setTextColor(Color.BLACK);
 				tv.setText(temp.station);
 				
 				int idx = find_station_by_zhname(string_replace(temp.station));
 				if(idx >= 0 && getResources().getString(R.string.locale).contentEquals("English"))
 					tv.setText(en_stations[idx]);
 				else if(idx >= 0 && getResources().getString(R.string.locale).contentEquals("简体中文"))
 					tv.setText(cn_stations[idx]);
 				
 				tv.setTextSize(16);
 				tv.setHorizontallyScrolling(false);
 				tv.setWidth(0);
 				tv.setGravity(Gravity.CENTER);
 				tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.35f));
 				tr.addView(tv);
 
 				// 到站時間
 				tv = new TextView(this);
 				tv.setTextColor(Color.BLACK);
 				tv.setText(temp.coming_time);
 				tv.setTextSize(16);
 				tv.setHorizontallyScrolling(false);
 				tv.setWidth(0);
 				tv.setGravity(Gravity.CENTER);
 				tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.25f));
 				tr.addView(tv);
 
 				// 離站時間
 				tv = new TextView(this);
 				tv.setTextColor(Color.BLACK);
 				tv.setText(temp.depart_time);
 				tv.setTextSize(16);
 				tv.setHorizontallyScrolling(false);
 				tv.setWidth(0);
 				tv.setGravity(Gravity.CENTER);
 				tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.25f));
 				tr.addView(tv);
 
 				// 車子icon
 				iv = new ImageView(this);
 				iv.setImageResource(R.drawable.track);
 				iv.setAlpha(0);
 				iv.setMaxHeight((int) (20 * getResources().getDisplayMetrics().density));
 				iv.setMaxWidth((int) (20 * getResources().getDisplayMetrics().density));
 				iv.setAdjustViewBounds(true);
 				iv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.10f));
 				tr.addView(iv);
 
 				timetable.add(tr);
 			}
 		}
 
 		for(int i = 0; i < routes.size(); i++){
 			TrainTable temp = routes.get(i);
 			final TableRow tr = timetable.get(i);
 //			tr.setBackgroundColor(Color.WHITE);
 //			temp.set_tablerow(tr);
 //			//  起訖站icon
 //			ImageView iv = (ImageView) tr.getChildAt(0);
 //			if(temp.start)
 //				iv.setImageResource(R.drawable.start);
 //			else if (temp.destination)
 //				iv.setImageResource(R.drawable.destination);
 //			else {
 //				iv.setImageResource(R.drawable.start);
 //				iv.setAlpha(0);
 //			}
 //
 //			// 站名
 //			TextView tv = (TextView) tr.getChildAt(1);
 //			tv.setText(temp.station);
 //
 //			// 到站時間
 //			tv = (TextView) tr.getChildAt(2);
 //			tv.setText(temp.coming_time);
 //
 //			// 離站時間
 //			tv = (TextView) tr.getChildAt(3);
 //			tv.setText(temp.depart_time);
 
 			// 車子icon
 			ImageView iv = (ImageView) tr.getChildAt(4);
 			if(temp.istrain) {
 				iv.setAlpha((int)255);
 				iv.setImageResource(R.drawable.locomotive);
 				train_index = i;
 			}
 
 			if(first_read && temp.start) {
 				sv.post(new Runnable() {
 					@Override
 					public void run() {
 						sv.smoothScrollTo(0, tr.getTop());
 					} 
 				});
 			}
 		}
 		if(train_index > 0) {
 			for(int i = 0; i < train_index; i++) {
 				TableRow tr = timetable.get(i);
 				ImageView iv = (ImageView) tr.getChildAt(4);
 				iv.setAlpha((int)255);
 				iv.setImageResource(R.drawable.track);
 			}
 		}
 		tl.invalidate();
 	}
 
 	private String create_time_table(List<TimeTable> routes, TableLayout tl, final ScrollView sv, Time depart) {
 		String depart_sta = null, carrier = null;
 
 		if(routes.size() == 0)
 			return "";
 
 		depart_sta = routes.get(0).depart_station;
 		carrier = routes.get(0).carrier;
 
 		/* 2013/11/25 */
 		TableRow tr_date = CreateTableRow(tl);
 		TextView timetv = new TextView(this);
 		timetv.setId(ID_BUS_TIMETABLE_DATE);
 		tr_date.setBackgroundColor(Color.WHITE);
 		timetv.setTextColor(Color.BLACK);
 		timetv.setTextSize(20);
 		timetv.setText(String.format("%04d/%02d/%02d (%s)", depart.year, depart.month + 1, depart.monthDay, weekday2str(depart.weekDay)));
 		TableRow.LayoutParams layoutparams = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f);
 		layoutparams.span = 4;
 		timetv.setLayoutParams(layoutparams);
 		timetv.setGravity(Gravity.CENTER);
 		tr_date.addView(timetv);
 
 		for(int i = 0; i<routes.size(); i++) {
 			if(routes.get(i).check_weekday(depart.weekDay) == false)
 				routes.remove(i--);
 		}
 
 		/* 台北轉運站 */
 		TableRow tr_depart = CreateTableRow(tl);
 		TextView tv_depart = new TextView(this);
 		tr_depart.setBackgroundColor(Color.BLACK);
 		tv_depart.setTextColor(Color.WHITE);
 		tv_depart.setText(depart_sta);
 		tv_depart.setTextSize(16);
 		tv_depart.setWidth(0);
 		tv_depart.setGravity(Gravity.CENTER);
 		tv_depart.setLayoutParams(layoutparams);
 		tr_depart.addView(tv_depart);
 
 		if(routes.size() == 0) {
 			tr_depart = CreateTableRow(tl);
 			tv_depart = new TextView(this);
 			tr_depart.setBackgroundColor(Color.WHITE);
 			tv_depart.setTextColor(Color.BLACK);
 			tv_depart.setText(getResources().getString(R.string.no_service_today));
 			tv_depart.setTextSize(16);
 			tv_depart.setWidth(0);
 			tv_depart.setGravity(Gravity.CENTER);
 			tv_depart.setLayoutParams(layoutparams);
 			tr_depart.addView(tv_depart);
 			return carrier;
 		}
 
 		int i = 0;
 		while(i < routes.size()) {
 			TextView tv1, tv2, tv3, tv4;
 
 			if(routes.get(i).depart_station.contentEquals(depart_sta)) {
 				TableRow tr_times = CreateTableRow(tl);
 				tr_times.setBackgroundColor(Color.WHITE);
 
 				tv1 = create_textview(tr_times);
 				tv2 = create_textview(tr_times);
 				tv3 = create_textview(tr_times);
 				tv4 = create_textview(tr_times);
 
 				fillin_time(tv1, routes.get(i++), depart);
 				if(i < routes.size() && routes.get(i).depart_station.contentEquals(depart_sta))
 					fillin_time(tv2, routes.get(i++), depart);
 				if(i < routes.size() && routes.get(i).depart_station.contentEquals(depart_sta))
 					fillin_time(tv3, routes.get(i++), depart);
 				if(i < routes.size() && routes.get(i).depart_station.contentEquals(depart_sta))
 					fillin_time(tv4, routes.get(i++), depart);
 			}
 			else {
 				depart_sta = routes.get(i).depart_station;
 				/* 返程站 */
 				tr_depart = CreateTableRow(tl);
 				tv_depart = new TextView(this);
 				tr_depart.setBackgroundColor(Color.BLACK);
 				tv_depart.setTextColor(Color.WHITE);
 				tv_depart.setText(depart_sta);
 				tv_depart.setTextSize(16);
 				tv_depart.setWidth(0);
 				tv_depart.setGravity(Gravity.CENTER);
 				tv_depart.setLayoutParams(layoutparams);
 				tr_depart.addView(tv_depart);
 			}
 		}
 		return carrier;
 	}
 
 	private String weekday2str(int weekday) {
 		switch (weekday) {
 		case 0:
 			return getResources().getString(R.string.sun);
 		case 1:
 			return getResources().getString(R.string.mon);
 		case 2:
 			return getResources().getString(R.string.tue);
 		case 3:
 			return getResources().getString(R.string.wed);
 		case 4:
 			return getResources().getString(R.string.thu);
 		case 5:
 			return getResources().getString(R.string.fri);
 		case 6:
 			return getResources().getString(R.string.sat);
 		default:
 			return "";
 
 		}
 	}
 
 	private TextView create_textview(TableRow tr) {
 		TextView tv = new TextView(this);
 		tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.25f));
 		tv.setGravity(Gravity.CENTER);
 		tr.addView(tv);
 		return tv;
 	}
 
 	private void fillin_time(TextView tv, TimeTable tt, Time depart) {
 		Time now = new Time();
 		now.setToNow();
 		if(depart.yearDay > now.yearDay)
 			tv.setTextColor(Color.BLACK);
 		else {
 			int current_time = now.hour * 60 + now.minute;
 			if(tt.time >= current_time)
 				tv.setTextColor(Color.BLACK);
 			else
 				tv.setTextColor(Color.LTGRAY);
 		}
 		tv.setText(tt.minutes2str());
 		tv.setTextSize(16);
 		tv.setWidth(0);
 	}
 
 	private void create_realtime_table(List<BusRoute> routes, TableLayout tl, final ScrollView sv) {
 		boolean first_read = false;
 		/* 公車即時資訊欄位 
 		 *  | 起訖站icon | 站名 | 到站時間 | 車子icon | */
 		if(timetable.isEmpty()) {
 			first_read = true;
 			TableRow tr = null;
 			for(int i = 0; i<routes.size(); i++) {
 				tr = CreateTableRow(tl);
 				//  起訖站icon
 				ImageView iv = new ImageView(this);
 				iv.setBackgroundColor(Color.TRANSPARENT);
 				iv.setMaxHeight((int) (20 * getResources().getDisplayMetrics().density));
 				iv.setMaxWidth((int) (20 * getResources().getDisplayMetrics().density));
 				iv.setAdjustViewBounds(true);
 				iv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.05f));
 				tr.addView(iv);
 
 				// 站名
 				TextView tv = new TextView(this);
 				tv.setTextColor(Color.BLACK);
 				tv.setTextSize(16);
 				tv.setHorizontallyScrolling(false);
 				tv.setWidth(0);
 				tv.setGravity(Gravity.CENTER);
 				tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.60f));
 				tr.addView(tv);
 
 				// 到站時間
 				tv = new TextView(this);
 				tv.setTextColor(Color.BLACK);
 				tv.setTextSize(16);
 				tv.setHorizontallyScrolling(false);
 				tv.setWidth(0);
 				tv.setGravity(Gravity.CENTER);
 				tv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.30f));
 				tr.addView(tv);
 
 				// 車子icon
 				iv = new ImageView(this);
 				iv.setBackgroundColor(Color.TRANSPARENT);
 				iv.setMaxHeight((int) (20 * getResources().getDisplayMetrics().density));
 				iv.setMaxWidth((int) (20 * getResources().getDisplayMetrics().density));
 				iv.setAdjustViewBounds(true);
 				iv.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0.05f));
 				tr.addView(iv);
 
 				timetable.add(tr);
 			}
 		}
 
 		for (int i = 0; i<routes.size(); i++) {
 			BusRoute temp = routes.get(i);
 			final TableRow tr = timetable.get(i);
 
 			if(tr.getChildCount() == 0)
 				continue;
 
 			if(temp.GoBack % 2 == 0)
 				tr.setBackgroundColor(Color.WHITE);
 			else
 				tr.setBackgroundColor(Color.LTGRAY);
 
 			/* 起訖站icon */
 			if(temp.isStart || temp.isDestination) {
 				ImageView iv = (ImageView) tr.getChildAt(0);
 				iv.setImageResource(temp.isStart ? R.drawable.start : R.drawable.destination);
 				if(first_read && temp.isStart) {
 					sv.post(new Runnable() {
 						@Override
 						public void run() {
 							sv.smoothScrollTo(0, tr.getTop());
 						} 
 					});
 				}
 			}
 			else {
 				/* Empty view */
 				ImageView iv = (ImageView) tr.getChildAt(0);
 				iv.setImageResource(0);
 			}
 
 			/* 站名 */
 			TextView tv = (TextView) tr.getChildAt(1);
 			tv.setText(temp.StopName);
 
 			/* 到站時間 */
 			tv = (TextView) tr.getChildAt(2);
 			if(temp.Value.contentEquals("0"))
 				tv.setText(getResources().getString(R.string.arriving));
 			else if(temp.Value.contentEquals("1"))
 				tv.setText(getResources().getString(R.string.almost_arriving));
 			else
 				tv.setText(temp.Value.contentEquals("null") ? temp.comeTime : showtime(temp.Value));
 
 			/* 車子icon */
 			if(temp.isCar) {
 				ImageView iv = (ImageView) tr.getChildAt(3);
 				iv.setImageResource(R.drawable.realtime_bus);
 			}
 			else {
 				/* Empty view */
 				ImageView iv = (ImageView) tr.getChildAt(3);
 				iv.setImageResource(0);
 			}
 		}
 		tl.invalidate();
 		if(first_read && err_tag_fail)
 			Toast.makeText(this, getResources().getString(R.string.error_find_start_dest) , Toast.LENGTH_LONG).show();
 	}
 
 	private String showtime(String value) {
 		int val = Integer.parseInt(value);
 		if (val < 60)
 			return val + getResources().getString(R.string.minute);
 		else
 			return String.format("%d%s%d%s", val/60, getResources().getString(R.string.hour), val%60, getResources().getString(R.string.minute));
 	}
 
 	private void show_info_provider(int r_string_id) {
 		RelativeLayout rl = (RelativeLayout)findViewById(R.id.rl_pop_transit);
 
 		TextView tv = new TextView(this);
 		tv.setId(ID_PROVIDER_ANNOUNCEMENT);
 		tv.setText(getResources().getString(r_string_id));
 		tv.setTextColor(Color.WHITE);
 		tv.setBackgroundColor(Color.DKGRAY);
 		tv.setTextSize(16);
 		tv.setGravity(Gravity.RIGHT);
 		tv.setHorizontallyScrolling(false);
 
 		RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 		param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 		param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
 		tv.setLayoutParams(param);
 
 		rl.addView(tv);
 
 		/* Add small progress bar */
 		rl.addView(loading);
 	}
 
 	private TableRow CreateTableRow(TableLayout parent){
 		TableRow tr = new TableRow(this);
 
 		TableLayout.LayoutParams params = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
 		params.setMargins(2, 2, 2, 2);
 		tr.setLayoutParams(params);
 		tr.setWeightSum(1.0f);
 		tr.setGravity(Gravity.CENTER_VERTICAL);
 
 		parent.addView(tr);
 		return tr;
 	}
 
 	private boolean check_agency(String agency, ArrayList<bus_provider> provider, String line, String headname) {
 		for (int i = 0; i < provider.size(); i++) {
 			if(agency.contentEquals(provider.get(i).provider)) {
 				if(provider.get(i).line_restriction != null) {
 					if(line.matches(provider.get(i).line_restriction)) {
 						if(provider.get(i).name == null)
 							return true;
 						else if(provider.get(i).name.contentEquals(headname))
 							return true;
 					}
 					else
 						return false;
 				}
 				else
 					return true;
 			}
 		}
 		return false;
 	}
 
 	private void bus_agency_classify() {
 		/* 台北公車 客運業者列表 */
 		bus_taipei.add(new bus_provider("大都會客運",  "[0-9]{1,3}|[^0-9][0-9]{1,3}[^0-9]?|[0-9]{1,3}[^0-9]|[^0-9]+|市民小巴[0-9]+|內科通勤[0-9]{1,2}"));	 /* 三碼數字公車, 紅32(區), 306區, 31區, 信義新幹線, 市民小巴5*/ 
 		bus_taipei.add(new bus_provider("三重客運", "[0-9]{1,3}|[^0-9][0-9]{1,3}[^0-9]?|[0-9]{1,3}[^0-9]|[^0-9]+|市民小巴[0-9]+|內科通勤[0-9]{1,2}"));
 		bus_taipei.add(new bus_provider("首都客運", "[0-9]{1,3}|[^0-9][0-9]{1,3}[^0-9]?|[0-9]{1,3}[^0-9]|[^0-9]+|市民小巴[0-9]+|內科通勤[0-9]{1,2}"));
 		bus_taipei.add(new bus_provider("指南客運", "[0-9]{1,3}|[^0-9][0-9]{1,3}[^0-9]?|[0-9]{1,3}[^0-9]|[^0-9]+|市民小巴[0-9]+|內科通勤[0-9]{1,2}"));
 		bus_taipei.add(new bus_provider("光華巴士", "[0-9]{1,3}|[^0-9][0-9]{1,3}[^0-9]?|[0-9]{1,3}[^0-9]|[^0-9]+|市民小巴[0-9]+|內科通勤[0-9]{1,2}"));
 		bus_taipei.add(new bus_provider("新店客運", "[0-9]{1,3}|[^0-9][0-9]{1,3}[^0-9]?|[0-9]{1,3}[^0-9]|[^0-9]+|市民小巴[0-9]+|內科通勤[0-9]{1,2}"));
 		bus_taipei.add(new bus_provider("中興巴士", "[0-9]{1,3}|[^0-9][0-9]{1,3}[^0-9]?|[0-9]{1,3}[^0-9]|[^0-9]+|市民小巴[0-9]+|內科通勤[0-9]{1,2}"));
 		bus_taipei.add(new bus_provider("大南汽車", "[0-9]{1,3}|[^0-9][0-9]{1,3}[^0-9]?|[0-9]{1,3}[^0-9]|[^0-9]+|市民小巴[0-9]+|內科通勤[0-9]{1,2}"));
 		bus_taipei.add(new bus_provider("欣欣客運", "[0-9]{1,3}|[^0-9][0-9]{1,3}[^0-9]?|[0-9]{1,3}[^0-9]|[^0-9]+|市民小巴[0-9]+|內科通勤[0-9]{1,2}"));
 		bus_taipei.add(new bus_provider("台北客運", "[0-9]{1,3}|[^0-9][0-9]{1,3}[^0-9]?|[0-9]{1,3}[^0-9]|[^0-9]+|市民小巴[0-9]+|內科通勤[0-9]{1,2}"));
 		bus_taipei.add(new bus_provider("淡水客運", "[0-9]{1,3}|[^0-9][0-9]{1,3}[^0-9]?|[0-9]{1,3}[^0-9]|[^0-9]+|市民小巴[0-9]+|內科通勤[0-9]{1,2}"));
 		bus_taipei.add(new bus_provider("新北客運", "[0-9]{1,3}|[^0-9][0-9]{1,3}[^0-9]?|[0-9]{1,3}[^0-9]|[^0-9]+|市民小巴[0-9]+|內科通勤[0-9]{1,2}"));
 		bus_taipei.add(new bus_provider("大有巴士", "[0-9]{1,3}|[^0-9][0-9]{1,3}[^0-9]?|[0-9]{1,3}[^0-9]|[^0-9]+|市民小巴[0-9]+|內科通勤[0-9]{1,2}"));
 		bus_taipei.add(new bus_provider("基隆客運", "78[7-9]]|79[01]|808|82[5-9]|846|88[6-8]|891|藍41"));
 		bus_taipei.add(new bus_provider("東南客運", "小[1235][^0-9]?|小1[012][^0-9]?|棕5|棕1[09]|綠11|藍5[01]|紅29|3[27][^0-9]?|207|29[78][^0-9]?|55[25]|612[^0-9]?|內科通勤[0-9]{1,2}"));
 		bus_taipei.add(new bus_provider("皇家客運", "1717"));
 
 		/* 高雄公車 客運業者列表 */
 		bus_kaohsiung.add(new bus_provider("高雄市公車處", null));
 		bus_kaohsiung.add(new bus_provider("南台灣客運", null));
 		bus_kaohsiung.add(new bus_provider("義大客運", "850[1-6]|^[0-9]{1,3}|[^0-9][0-9]{1,2}"));
 		bus_kaohsiung.add(new bus_provider("東南客運", "37|62|81|248|紅[167]|紅1[0268]|紅2[07]|橘1|橘20"));
 		bus_kaohsiung.add(new bus_provider("高雄客運", "5|2[34]|60|87|9[78]|[^0-9].*|80[0-4][0-9]|[紅橘][0-9]{1,2}[A-B]?"));
 
 		/* 台中公車 客運業者列表 */
 		/* ref: http://citybus.taichung.gov.tw/pda/aspx/businfomation/choiceRoad.aspx?lang=CHT */
 		bus_taichung.add(new bus_provider("豐原客運", "650[68]|6595|[0-9]{1,3}"));
 		bus_taichung.add(new bus_provider("台中客運", "[0-9]{1,3}|9區|6871|6899"));
 		bus_taichung.add(new bus_provider("仁友客運", "[0-9]{1,3}|6235"));
 		bus_taichung.add(new bus_provider("統聯客運", "[0-9]{1,2}|[12][0-9]{2}|75區[12]"));
 		bus_taichung.add(new bus_provider("巨業交通", "[0-9]{1,3}|68繞|168區[12]|169區"));
 		bus_taichung.add(new bus_provider("全航客運", "[0-9]{1,3}|58區[12]"));
 		bus_taichung.add(new bus_provider("彰化客運", "52|99"));
 		bus_taichung.add(new bus_provider("和欣客運", "16[01]"));
 		bus_taichung.add(new bus_provider("東南客運", "7|17|67|98"));
 		bus_taichung.add(new bus_provider("豐榮客運", "[0-9]{1,3}"));
 		bus_taichung.add(new bus_provider("苗栗客運", "181"));
 		bus_taichung.add(new bus_provider("中台灣客運", "[0-9]{1,3}"));
 
 		/* 桃園公車 客運列表 */
 		/* http://124.199.77.90/Taoyuan/PDA/busroute.aspx?lang=Cht */
 		bus_taoyuang.add(new bus_provider("桃園客運", "[0-9]{1,3}[ABS]?|BR|GR"));
 		bus_taoyuang.add(new bus_provider("中壢客運", "[0-9]{1,3}[ABS]?|BR|GR"));
 		bus_taoyuang.add(new bus_provider("統聯客運", "705"));
 		bus_taoyuang.add(new bus_provider("新竹客運", "301"));
 		bus_taoyuang.add(new bus_provider("亞通客運", "703"));
 	}
 
 	private void station_name_replace() {
 		original_sta.add("台中"); after_sta.add("臺中");
 		original_sta.add("台北"); after_sta.add("臺北");
 		original_sta.add("台東"); after_sta.add("臺東");
 		original_sta.add("台南"); after_sta.add("臺南");
 		original_sta.add("台視"); after_sta.add("臺視");
 		original_sta.add("台安醫院"); after_sta.add("臺安醫院");
 		original_sta.add("(松壽路)"); after_sta.add("(松壽)");
 		original_sta.add("(松仁路)"); after_sta.add("(松仁)");
 		original_sta.add("松山火車站"); after_sta.add("松山車站");
 		original_sta.add("萬華火車站"); after_sta.add("萬華車站");
 		original_sta.add("桃園機場一航站(下車站)"); after_sta.add("第一航");
 		original_sta.add("桃園機場二航站(下車站)"); after_sta.add("第二航");
 		original_sta.add("(下車站)"); after_sta.add("");
		original_sta.add("新崛江"); after_sta.add("新堀江");
 	}
 
 	private String string_replace(String station) {
 		for(int i = 0; i < original_sta.size(); i++) {
 			if(station.contains(original_sta.get(i))) {
 				return station.replace(original_sta.get(i), after_sta.get(i));
 			}
 		}
 		return station;
 	}
 
 	public void thsrc_current_status(String result) {
 		ImageView iv = (ImageView)findViewById(ID_HSR_STATUS);
 		TextView tv = (TextView)findViewById(ID_HSR_STATUS_DESCRIPTION);
 
 		iv.setMaxHeight((int) (32 * getResources().getDisplayMetrics().density));
 		iv.setMaxWidth((int) (32 * getResources().getDisplayMetrics().density));
 
 		if(result != null) {
 			if(result.contains("show_ok")) {
 				/* 正常運行 */
 				iv.setImageResource(R.drawable.allok);
 				tv.setText(getResources().getString(R.string.hsr_normal));
 			}
 			else {
 				/* 未知狀況 */
 				iv.setImageResource(R.drawable.warning);
 				tv.setText(getResources().getString(R.string.hsr_warning));
 			}
 		}
 	}
 
 	@SuppressLint("SetJavaScriptEnabled")
 	public WebView create_webview_by_url(String url) {
 		WebView wv = new WebView(this);
 		wv.getSettings().setJavaScriptEnabled(true);
 		wv.getSettings().setLoadWithOverviewMode(true);
 		wv.getSettings().setUseWideViewPort(false);
 		wv.setWebViewClient(new WebViewClient(){
 			@Override
 			public boolean shouldOverrideUrlLoading(WebView view, String url){
 				view.loadUrl(url);
 				return true;
 			}
 
 			@Override
 			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
 				Log.i("WEB_VIEW_TEST", "error code:" + errorCode);
 				view.reload();
 			}
 
 			@Override
 			public void onPageFinished(WebView view, String url) {
 				loading.setVisibility(ProgressBar.INVISIBLE);
 			}
 		});
 		Log.i(TAG, url);
 		wv.loadUrl(url);
 		loading.setVisibility(ProgressBar.VISIBLE);
 
 		//		RelativeLayout rl = (RelativeLayout)findViewById(R.id.rl_pop_transit);
 		//		RelativeLayout.LayoutParams webview_param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
 		//		webview_param.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 		//		wv.setLayoutParams(webview_param);
 		//
 		//		rl.addView(wv);
 
 		return wv;
 	}
 
 	/* time format sould be HH:MM */
 	private int string_2_minutes_of_day(String time) {
 		if(time.isEmpty())
 			return -1;
 		String[] hhmm = time.split(":");
 		int hh = Integer.parseInt(hhmm[0]);
 		int mm = Integer.parseInt(hhmm[1]);
 		return hh * 60 + mm;
 	}
 
 	public interface AnalysisResult {
 		public void parsestr(String p);
 		public void parsedBUS(List<BusRoute> routes);
 		public void parsedHSR(List<HSRTrains> trains);
 		public void parsedTimeTable(List<TimeTable> time);
 		public void TraTimeTable(List<TrainTimeTable> time);
 	}
 
 	private class TRA_TIMETABLE extends AsyncTask<String, Void, List<TrainTimeTable>> {
 		private AnalysisResult cb = null;
 		public TRA_TIMETABLE(AnalysisResult analysisResult) {
 			cb = analysisResult;
 		}
 
 		@Override
 		protected List<TrainTimeTable> doInBackground(String... urls) {
 			final List<TrainTimeTable> routes = new ArrayList<TrainTimeTable>();
 			for (final String url : urls) {
 				boolean success = false;
 				while(!success) {
 					try {
 						org.jsoup.nodes.Document doc;
 						doc = Jsoup.connect(url).userAgent("Mozilla").get();
 
 						Elements trs = doc.select("tr.Grid_Row");
 						
 						if(trs.size() > 1) {
 							for(int i = 1; i < trs.size(); i++) {
 								Elements tds = trs.get(i).select("td");
 								if(tds.size() == 11) {
 									TrainTimeTable tt = new TrainTimeTable(tds.get(0).text(), tds.get(1).text()
 									, tds.get(4).text(), tds.get(5).text(), tds.get(6).text());
 									Log.i(TAG, String.format("(%d)%s %s: %s %s(%s)", tds.size(), tds.get(0).text(), tds.get(1).text()
 											, tds.get(4).text(), tds.get(5).text(), tds.get(6).text()));
 									routes.add(tt);
 								}
 							}
 						}
 						
 //						Elements ps = doc.select("p.even, p.odd");
 //						for(org.jsoup.nodes.Element p : ps) {
 //							Elements fonts = p.select("font");
 //							Log.i(TAG, String.format("%s %s: %s %s(%s)", fonts.get(0).text(), fonts.get(1).text()
 //									, fonts.get(2).text(), fonts.get(3).text(), fonts.get(4).text()));
 //							TrainTimeTable tt = new TrainTimeTable(fonts.get(0).text(), fonts.get(1).text()
 //									, fonts.get(2).text(), fonts.get(3).text(), fonts.get(4).text());
 //							routes.add(tt);
 //						}
 						success = true;
 					} catch (IOException e) {
 						Log.i(TAG, "fail! retry!");
 						try {
 							Thread.sleep(3000);
 						} catch (InterruptedException e1) {
 							e1.printStackTrace();
 						} 
 					}
 				}
 			}
 			return routes;
 		}
 
 		@Override
 		protected void onPostExecute(List<TrainTimeTable> routes) {
 			cb.TraTimeTable(routes);
 		}
 	}
 
 	private class HSR_TIMETABLE_PARSER extends AsyncTask<String, Void, List<HSRTrains>> {
 		private AnalysisResult cb = null;
 		public HSR_TIMETABLE_PARSER(AnalysisResult analysisResult) {
 			cb = analysisResult;
 		}
 
 		@Override
 		protected List<HSRTrains> doInBackground(String... urls) {
 			List<HSRTrains> timetable = new ArrayList<HSRTrains>();
 			for (String url : urls) {
 				try {
 					org.jsoup.nodes.Document doc;
 
 					doc = Jsoup.connect(url).userAgent("Mozilla").get();
 
 					Elements bounds = doc.select("div.text_orange1");
 
 					Elements tables = doc.select("table[bgcolor=#CCCCCC]");
 					//					for (org.jsoup.nodes.Element bound : tables) {
 					for(int i = 0; i < tables.size(); i++) {
 						org.jsoup.nodes.Element bound = tables.get(i);
 						String str_bound = bounds.get(i).text();
 						Log.i(TAG, str_bound);
 						Elements trains = bound.select("tr[bgcolor]");
 						Log.i(TAG, String.format("found %d train", trains.size()));
 						for (org.jsoup.nodes.Element train : trains) {
 							String id = train.select("td.text_orange_link").get(0).text();	// 車次
 							String taipei = train.select("td[title=台北站]").get(0).text();
 							String banqiao = train.select("td[title=板橋站]").get(0).text();
 							String taoyuang = train.select("td[title=桃園站]").get(0).text();
 							String hsinchu = train.select("td[title=新竹站]").get(0).text();
 							String taichung = train.select("td[title=台中站]").get(0).text();
 							String chiayi = train.select("td[title=嘉義站]").get(0).text();
 							String tainan = train.select("td[title=台南站]").get(0).text();
 							String zuoying = train.select("td[title=左營站]").get(0).text();
 
 							HSRTrains newtrain = new HSRTrains(str_bound.contentEquals("南下列車")
 									, Integer.parseInt(id)
 									, string_2_minutes_of_day(taipei)
 									, string_2_minutes_of_day(banqiao)
 									, string_2_minutes_of_day(taoyuang)
 									, string_2_minutes_of_day(hsinchu)
 									, string_2_minutes_of_day(taichung)
 									, string_2_minutes_of_day(chiayi)
 									, string_2_minutes_of_day(tainan)
 									, string_2_minutes_of_day(zuoying));
 							timetable.add(newtrain);
 						}
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			return timetable;
 		}
 
 		@Override
 		protected void onPostExecute(List<HSRTrains> timetable) {
 			cb.parsedHSR(timetable);
 		}
 	}
 
 	private class HTML_BUS_PARSER extends AsyncTask<String, Void, String> {
 		private AnalysisResult cb = null;
 		public HTML_BUS_PARSER(AnalysisResult analysisResult) {
 			cb = analysisResult;
 		}
 
 		@Override
 		protected String doInBackground(String... urls) {
 			String response = "";
 			for (String url : urls) {
 				HttpGet httpGet = new HttpGet(url);
 				HttpClient client = new DefaultHttpClient();
 				try {
 					HttpResponse result = client.execute(httpGet);
 					StatusLine statusLine = result.getStatusLine();
 					int statusCode = statusLine.getStatusCode();
 					if (statusCode == 200) {
 						HttpEntity entity = result.getEntity();
 
 						response = EntityUtils.toString(entity);
 					} else {
 						Log.e(TAG, "Failed to download file");
 					}
 					return response;
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 					return "";
 				}
 			}
 			return "";
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			cb.parsestr(result);
 		}
 	}
 
 	private class TPE_BUS_PARSER extends AsyncTask<String, Void, List<BusRoute>> {
 		private AnalysisResult cb = null;
 		public TPE_BUS_PARSER(AnalysisResult analysisResult) {
 			cb = analysisResult;
 		}
 
 		@Override
 		protected List<BusRoute> doInBackground(String... urls) {
 			List<BusRoute> routes = new ArrayList<BusRoute>();
 			for (String url : urls) {
 				try {
 					org.jsoup.nodes.Document doc;
 
 					doc = Jsoup.connect(url).userAgent("Mozilla").get();
 
 					Elements trs = doc.select("tr.ttego1, tr.ttego2");
 					for (org.jsoup.nodes.Element tr : trs) {
 						String wait_time = null, pure_text = null;
 						Elements tds = tr.select("td");
 
 						pure_text = tds.get(1).text().replaceAll("[0-9A-Z]{2,3}-[0-9A-Z]{2,3} ", "");
 						//						if(tds.get(1).select("img[src^=bus]").size() != 0)
 						//							Log.i(TAG, "get BUS!");
 
 						if(pure_text.contentEquals("將到站"))
 							wait_time = "1";
 						else if(pure_text.contentEquals("進站中"))
 							wait_time = "0";
 						else if(pure_text.contains("分"))
 							wait_time = pure_text.replaceAll("分", "");
 						else
 							wait_time = "null";
 
 						routes.add(new BusRoute(tds.get(0).text(), 1, 
 								0, wait_time, getResources().getString(R.string.no_service), ""));
 
 					}
 
 					trs = doc.select("tr.tteback1, tr.tteback2");
 					for (org.jsoup.nodes.Element tr : trs) {
 						String wait_time = null, pure_text = null;
 						Elements tds = tr.select("td");
 
 						pure_text = tds.get(1).text().replaceAll("[0-9A-Za-z]{2,3}-[0-9A-Za-z]{2,3} ", "");
 
 						if(pure_text.contentEquals("將到站"))
 							wait_time = "1";
 						else if(pure_text.contentEquals("進站中"))
 							wait_time = "0";
 						else if(pure_text.contains("分"))
 							wait_time = pure_text.replaceAll("分", "");
 						else
 							wait_time = "null";
 
 						routes.add(new BusRoute(tds.get(0).text(), 2, 
 								0, wait_time, getResources().getString(R.string.no_service), ""));
 					}
 
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			return routes;
 		}
 
 		@Override
 		protected void onPostExecute(List<BusRoute> routes) {
 			cb.parsedBUS(routes);
 		}
 	}
 
 	private class TXN_BUS_PARSER extends AsyncTask<String, Void, List<BusRoute>> {
 		private AnalysisResult cb = null;
 		public TXN_BUS_PARSER(AnalysisResult analysisResult) {
 			cb = analysisResult;
 		}
 
 		@Override
 		protected List<BusRoute> doInBackground(String... urls) {
 			List<BusRoute> routes = new ArrayList<BusRoute>();
 			for (String url : urls) {
 				try {
 					org.jsoup.nodes.Document doc;
 					boolean go = true;
 
 					doc = Jsoup.connect(url).userAgent("Mozilla").get();
 
 					if(url.contains("goback=1"))
 						go = true;
 					else
 						go = false;
 
 					Elements trs = doc.select("td#ddlName");
 					for (org.jsoup.nodes.Element tr : trs) {
 						String wait_time = "null", time_come = getResources().getString(R.string.no_service);
 						Elements tds = tr.select("td");
 
 						String[] detail = tds.get(0).text().split(" ");
 
 						// example: 1 臺中站 1829 21:00 (站序 站牌名稱 站牌編號 預估到站)
 						for(int i = 4; i+3 < detail.length; i+=4) {
 							if(detail[i+3].matches("[0-9]{1,2}:[0-9]{2}")) {
 								time_come = detail[i+3];
 							}
 							else if (detail[i+3].matches("[0-9]+分")) {
 								wait_time = detail[i+3].replaceAll("分", "");
 							}
 							else if (detail[i+3].contentEquals("即將到站")) {
 								wait_time = "1";
 							}
 							else if (detail[i+3].contentEquals("進站中")) {
 								wait_time = "0";
 							}
 							else {
 								wait_time = "null";
 							}
 
 							routes.add(new BusRoute(detail[i+1], go ? 1 : 2, 
 									0, wait_time, time_come, ""));
 						}
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			return routes;
 		}
 
 		@Override
 		protected void onPostExecute(List<BusRoute> routes) {
 			cb.parsedBUS(routes);
 		}
 	}
 
 	private class TYN_BUS_PARSER extends AsyncTask<String, Void, List<BusRoute>> {
 		private AnalysisResult cb = null;
 		public TYN_BUS_PARSER(AnalysisResult analysisResult) {
 			cb = analysisResult;
 		}
 
 		@Override
 		protected List<BusRoute> doInBackground(String... urls) {
 			List<BusRoute> routes = new ArrayList<BusRoute>();
 			for (String url : urls) {
 				try {
 					org.jsoup.nodes.Document doc;
 					boolean go = true;
 
 					doc = Jsoup.connect(url).userAgent("Mozilla").get();
 
 					if(url.contains("GO_OR_BACK=1"))
 						go = true;
 					else
 						go = false;
 
 					Elements tds = doc.select("td[bgcolor=#f3f3f3]");
 					for(int i=0; i+2 < tds.size(); i+=3) {
 						org.jsoup.nodes.Element td_time = tds.get(i+2);
 						String wait_time = "null", time_come = getResources().getString(R.string.no_service), time = td_time.text();
 
 						// example: 1 中壢公車站 0分 (站序 站牌名稱 預估到站)
 						if(time.matches("[0-9]{1,2}:[0-9]{2}")) {
 							time_come = time;
 						}
 						else if (time.matches("[0-9]+分")) {
 							wait_time = time.replaceAll("分", "");
 						}
 						else if (time.contentEquals("即將到站")) {
 							wait_time = "1";
 						}
 						else if (time.contentEquals("進站中")) {
 							wait_time = "0";
 						}
 						else {
 							wait_time = "null";
 						}
 
 						Log.i(TAG, String.format("%s: %s", tds.get(i+1).text(), tds.get(i+2).text()));
 
 						routes.add(new BusRoute(tds.get(i+1).text(), go ? 1 : 2, 
 								0, wait_time, time_come, ""));
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			return routes;
 		}
 
 		@Override
 		protected void onPostExecute(List<BusRoute> routes) {
 			cb.parsedBUS(routes);
 		}
 	}
 
 	private class INTERCITY_BUS_PARSER extends AsyncTask<String, Void, List<BusRoute>> {
 		private AnalysisResult cb = null;
 		public INTERCITY_BUS_PARSER(AnalysisResult analysisResult) {
 			cb = analysisResult;
 		}
 
 		@Override
 		protected List<BusRoute> doInBackground(String... urls) {
 			List<BusRoute> routes = new ArrayList<BusRoute>();
 			String response;
 			for (String url : urls) {
 				HttpGet httpGet = new HttpGet(url);
 				HttpClient client = new DefaultHttpClient();
 				try {
 					HttpResponse result = client.execute(httpGet);
 					StatusLine statusLine = result.getStatusLine();
 					int statusCode = statusLine.getStatusCode();
 					if (statusCode == 200) {
 						HttpEntity entity = result.getEntity();
 
 						response = EntityUtils.toString(entity);
 
 						response = response.replace('|', '&');
 						String[] stops_raw = response.split("&");
 						String time_raw = stops_raw[stops_raw.length - 1];
 						String[] time = time_raw.split(",");
 						int goback = 1;
 
 						if(Arrays.asList(urls).indexOf(url) == 0) goback = 1;
 						else goback = 2;
 
 						for(int i = 0; i<time.length; i++) {
 							time[i] = time[i].replaceAll("^[0-9]+@", "");
 
 							if(time[i].contentEquals("即將進站") || time[i].contentEquals("Coming"))
 								time[i]  = "1";
 							else if(time[i].contentEquals("進站中") || time[i].contentEquals("InStop"))
 								time[i] = "0";
 							else if(time[i].contains("時")) {
 								time[i] = time[i].replaceAll("分", "");
 								int index_of_hour = time[i].indexOf("時");
 								int hour = Integer.parseInt(time[i].substring(0, index_of_hour));
 								int min = Integer.parseInt(time[i].substring(index_of_hour + 1));
 								time[i] = String.valueOf(hour * 60 + min);
 							}
 							else if(time[i].contains("分"))
 								time[i] = time[i].replaceAll("分", "");
 							else if(time[i].contains("min."))
 								time[i] = time[i].replaceAll("min.", "");
 							else
 								time[i] = "null";
 						}
 
 						for(int i = 0; i<stops_raw.length - 1; i++) {
 							routes.add(new BusRoute(stops_raw[i].split(",")[3], goback, 0
 									, time[i], getResources().getString(R.string.no_service), ""));
 						}
 
 					} else {
 						Log.e(TAG, "Failed to download file");
 					}
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			return routes;
 		}
 
 		@Override
 		protected void onPostExecute(List<BusRoute> routes) {
 			cb.parsedBUS(routes);
 		}
 	}
 
 	private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
 		@Override
 		protected String doInBackground(String... urls) {
 			String response = "";
 			for (String url : urls) {
 				HttpGet httpGet = new HttpGet(url);
 				HttpClient client = new DefaultHttpClient();
 				try {
 					HttpResponse result = client.execute(httpGet);
 					StatusLine statusLine = result.getStatusLine();
 					int statusCode = statusLine.getStatusCode();
 					if (statusCode == 200) {
 						HttpEntity entity = result.getEntity();
 
 						response = EntityUtils.toString(entity);
 					} else {
 						Log.e(TAG, "Failed to download file");
 					}
 					return response;
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 					return "";
 				}
 			}
 			return "";
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			thsrc_current_status(result);
 		}
 	}
 
 	private class INTERCITY_TIME_TABLE extends AsyncTask<String, Void, List<TimeTable>> {
 		private AnalysisResult cb = null;
 		public INTERCITY_TIME_TABLE(AnalysisResult analysisResult) {
 			cb = analysisResult;
 		}
 
 		@Override
 		protected List<TimeTable> doInBackground(String... urls) {
 			List<TimeTable> timetable = new ArrayList<TimeTable>();
 			for (String url : urls) {
 				try {
 					org.jsoup.nodes.Document doc;
 					String depart_station;
 
 					doc = Jsoup.connect(url).userAgent("Mozilla").get();
 
 					Elements tables = doc.select("table[style=border-collapse:collapse;]");
 
 					for(int i = 0; i < tables.size(); i++) {
 						Elements title = tables.get(i).select("tr.font02").select("th");
 						depart_station = title.get(1).text();
 						Log.i(TAG, "出發站: " + depart_station);
 
 						Elements trs = tables.get(i).select("tr.font03, tr.font04");
 
 						for(int j = 0; j < trs.size(); j++) {
 							TimeTable car;
 							org.jsoup.nodes.Element bound = trs.get(j);
 							Elements tds = bound.select("td");
 							Log.i(TAG, String.format("%s,%s", tds.get(1).text(), tds.get(2).text()));
 							if(tds.get(2).text().contentEquals("每日行駛") || tds.get(2).text().contentEquals("Daily")) {
 								car = new TimeTable(string_2_minutes_of_day(time2str(tds.get(1).text())), true, depart_station, tds.get(3).text(), trs.size() >=2 ? true: false);
 							}
 							else {
 								car = new TimeTable(string_2_minutes_of_day(time2str(tds.get(1).text())), false, depart_station, tds.get(3).text(), trs.size() >=2 ? true: false);
 								if(tds.get(2).text().contains("日") || tds.get(2).text().contains("Sun"))
 									car.set_sun(true);
 								if(tds.get(2).text().contains("一") || tds.get(2).text().contains("Mon"))
 									car.set_mon(true);
 								if(tds.get(2).text().contains("二") || tds.get(2).text().contains("The") || tds.get(2).text().contains("Tue"))	/* 真的是The... */
 									car.set_tue(true);
 								if(tds.get(2).text().contains("三") || tds.get(2).text().contains("Wed"))
 									car.set_wed(true);
 								if(tds.get(2).text().contains("四") || tds.get(2).text().contains("Thu"))
 									car.set_thu(true);
 								if(tds.get(2).text().contains("五") || tds.get(2).text().contains("Fri"))
 									car.set_fri(true);
 								if(tds.get(2).text().contains("六") || tds.get(2).text().contains("Sat"))
 									car.set_sat(true);
 							}
 							timetable.add(car);
 						}
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 					return timetable;
 				}
 			}
 			return timetable;
 		}
 
 		@Override
 		protected void onPostExecute(List<TimeTable> timetable) {
 			cb.parsedTimeTable(timetable);
 		}
 	}
 
 	private String time2str(String time) {
 		return String.format("%s:%s", time.substring(0, 2), time.substring(2));
 	}
 
 	public class bus_provider {
 		String provider;
 		String line_restriction;	// Regular expression
 		String name;
 		public bus_provider(String p, String l) {
 			provider = p;
 			line_restriction = l;
 			name = null;
 		}
 		public bus_provider(String p, String l, String n) {
 			provider = p;
 			line_restriction = l;
 			name = n;
 		}
 	}
 
 	class PreExistingViewFactory implements TabContentFactory{
 		private final View preExisting;
 		protected PreExistingViewFactory(View view){
 			preExisting = view;
 		}
 		public View createTabContent(String tag) {
 			return preExisting;
 		}
 	}
 
 	/**
 	 * A gesture listener that listens for a left or right swipe and uses the swip gesture to navigate a TabHost that
 	 * uses an AnimatedTabHost listener.
 	 * 
 	 * @author Daniel Kvist
 	 * 
 	 */
 	class MyGestureDetector extends SimpleOnGestureListener
 	{
 		protected MotionEvent mLastOnDownEvent = null;
 		private static final int SWIPE_MIN_DISTANCE = 120;
 		private static final int SWIPE_MAX_OFF_PATH = 150;
 		private static final int SWIPE_THRESHOLD_VELOCITY = 200;
 		private int maxTabs;
 		TabHost tabHost;
 
 		/**
 		 * An empty constructor that uses the tabhosts content view to decide how many tabs there are.
 		 */
 		public MyGestureDetector(TabHost tab)
 		{
 			tabHost = tab;
 			maxTabs = tabHost.getTabWidget().getChildCount();
 		}
 		
 		@Override
 	    public boolean onDown(MotionEvent e) {
 	        mLastOnDownEvent = e;
 	        return super.onDown(e);
 	    }
 		
 		/**
 		 * Listens for the onFling event and performs some calculations between the touch down point and the touch up
 		 * point. It then uses that information to calculate if the swipe was long enough. It also uses the swiping
 		 * velocity to decide if it was a "true" swipe or just some random touching.
 		 */
 		@Override
 		public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY)
 		{
 			int newTab = 0;
 			boolean swipe = false;
 			int currentTab = tabHost.getCurrentTab();
 			
 			if (event1 == null)
 				event1 = mLastOnDownEvent;
 			if (event1 == null || event2 == null)
 				return false;
 
 			if (Math.abs(event1.getY() - event2.getY()) > SWIPE_MAX_OFF_PATH)
 				return false;
 			if (event1.getX() - event2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
 			{
 				// Swipe right to left
 				newTab = currentTab + 1;
 				swipe = true;
 			}
 			else if (event2.getX() - event1.getX() > SWIPE_MIN_DISTANCE	&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
 			{
 				// Swipe left to right
 				newTab = currentTab - 1;
 				swipe = true;
 			}
 
 			if (newTab < 0 || newTab > (maxTabs - 1))
 				return false;
 
 			if(swipe)
 				tabHost.setCurrentTab(newTab);
 			return super.onFling(event1, event2, velocityX, velocityY);
 		}
 	}
 }
