 package tw.ipis.routetaiwan;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
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
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.ContextThemeWrapper;
 import android.view.Gravity;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TableRow.LayoutParams;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class pop_transit extends Activity {
 	private static String TAG = "--pop_transit--";
 	private ProgressBar process;
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
 	private String[] en_hsr_stations = {"Taipei", "Banciao", "Taoyuan", "Hsinchu", "Taichung", "Chiayi", "Tainan", "Zuoying"};
 	private static final int ID_PROVIDER_ANNOUNCEMENT = 0x12365401;
 	private static final int ID_HSR_STATUS = 0x12345001;
 	private static final int ID_HSR_STATUS_DESCRIPTION = 0x12345002;
 	private static final int ID_HSR_STATUS_TABLE = 0x12345003;
 	private static final int ID_HSR_TIME_DEPART = 0x12345004;
 	private static final int ID_HSR_TIME_TABLE_TITLE = 0x12345005;
 	private static final int ID_HSR_TIME_TABLE_TABLE = 0x12345006;
 	private static final int ID_INTERCITYBUS_CARRIER = 0x12345007;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.pop_transit);
 
 		Bundle Data = this.getIntent().getExtras();
 		String type = Data.getString("type");
 
 		long time = 0;
 		if(type != null && !type.contentEquals("null")) {
 			line = Data.getString("line");
 			if(type.contentEquals("bus")) {
 
 				bus_agency_classify();
 				station_name_replace();
 
 				agency = Data.getString("agency");
 				dept = Data.getString("dept");
 				arr = Data.getString("arr");
 				name = Data.getString("headname");
 			}
 			else if(type.contentEquals("tra")) {
 				car_class = Data.getString("class");
 				time = Data.getLong("time");
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
 
 		loading = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
 		loading.setIndeterminate(true);
 		RelativeLayout.LayoutParams loading_param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 		loading_param.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 		loading_param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
 		loading.setLayoutParams(loading_param);
 
 		/* 台鐵 */
 		/* If type = "tra", then open webview for ex: http://twtraffic.tra.gov.tw/twrail/mobile/TrainDetail.aspx?searchdate=2013/10/03&traincode=117 */
 		if(type.contentEquals("tra")) {
 			SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
 			Date date = new Date(System.currentTimeMillis()) ;
 			String str_date = formatter.format(date);
 			String tra_real_time_url = "http://twtraffic.tra.gov.tw/twrail/mobile/TrainDetail.aspx?searchdate={0}&traincode={1}";
 			String url = MessageFormat.format(tra_real_time_url, str_date, line);
 
 			create_webview_by_url(url);
 
 			/* 設定activity title, ex: 自強 123 */
 			this.setTitle(car_class + " " + line);
 
 			/* 資料由台鐵提供 */
 			show_info_provider(R.string.provide_by_tra);
 			loading.setVisibility(ProgressBar.INVISIBLE);
 		}
 		/* 高鐵 */
 		else if(type.contentEquals("hsr")) {
 			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
 			long millis = time * 1000;
 			Date date = new Date(millis) ;
 			String str_date = formatter.format(date);
 			String hsr_real_time_url = "http://www.thsrc.com.tw/tw/TimeTable/DailyTimeTable/{0}";
 			String url = MessageFormat.format(hsr_real_time_url, str_date);
 			Log.i(TAG, String.format("hsr url=%s", url));
 
 			/* 設定activity title, ex: 自強 123 */
 			this.setTitle(getResources().getString(R.string.hsr_status));
 
 			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.TAIWAN);
 			String formattedDate = sdf.format(date);
 
 			final int current_min = string_2_minutes_of_day(formattedDate);
 			final boolean southbound;
 			if(getResources().getString(R.string.locale).contentEquals("English")) {
 				dept = dept.replaceAll("high speed rail ", "");
 				arr = arr.replaceAll("high speed rail ", "");
 				dept = dept.replaceAll(" station", "");
 				arr = arr.replaceAll(" station", "");
 
 				Log.i(TAG, String.format("<%s> <%s>", dept, arr));
 
 				southbound = Arrays.asList(en_hsr_stations).indexOf(dept) < Arrays.asList(en_hsr_stations).indexOf(arr) ? true : false;
 			}
 			else {
 				dept = dept.replaceAll("高鐵", "");
 				arr = arr.replaceAll("高鐵", "");
 
 				southbound = Arrays.asList(hsr_stations).indexOf(dept) < Arrays.asList(hsr_stations).indexOf(arr) ? true : false;
 			}
 
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
 			iv.setMaxHeight(30);
 			iv.setMaxWidth(30);
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
 			/* 預計xx站出發時間 HH:MM */
 			tv = new TextView(this);
 			tv.setId(ID_HSR_TIME_DEPART);
 			SimpleDateFormat boarding_time = new SimpleDateFormat("MM/dd HH:mm", Locale.TAIWAN);
 			tv.setText(dept + getResources().getString(R.string.estimate) + getResources().getString(R.string.departure_time) + ": " + boarding_time.format(date));
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
 			iv.setMaxHeight(30);
 			iv.setMaxWidth(30);
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
 
 			/* 時刻表捲單 */
 			final ScrollView sv = new ScrollView(this);
 			sv.setId(ID_HSR_TIME_TABLE_TABLE);
 			RelativeLayout.LayoutParams svparam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
 			svparam.addRule(RelativeLayout.BELOW, ID_HSR_TIME_TABLE_TITLE);
 			sv.setLayoutParams(svparam);
 			rl.addView(sv);
 
 			/* 資料由台灣高鐵提供 */
 			show_info_provider(R.string.provide_by_thsr);
 
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
 						iv.setMaxHeight(30);
 						iv.setMaxWidth(30);
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
 					/* 最後的空白行 */
 					TableRow tr = CreateTableRow(tl);
 					tr.addView(new TextView(pop_transit.this));
 					tr.setBackgroundColor(color);
 					sv.addView(tl);
 
 					loading.setVisibility(ProgressBar.INVISIBLE);
 				}
 			});
 			timetable.execute(url);
 		}
 		/* 公車 客運 */
 		else {
 
 			if(check_agency(agency, bus_taipei, line, name)) {
 				SimpleDateFormat formatter = new SimpleDateFormat("H");
 				Date date = new Date(System.currentTimeMillis()) ;
 				String str_now = formatter.format(date);
 				int now = Integer.parseInt(str_now);
 
 				if(line.contentEquals("三鶯線先導公車"))
 					line = "三鶯捷運先導公車";
 				else if(now < 20 && now > 6 && line.contentEquals("橘18")) {
 					line = "橘18福隆路";		// 為了較好的效能..
 				}
 
 				line.replace("內科通勤", "內科通勤專車");
 
 				String tpe_bus_url = "http://pda.5284.com.tw/MQS/businfo2.jsp?routeId={0}";
 				try {
 					final String url = MessageFormat.format(tpe_bus_url, URLEncoder.encode(line, "UTF-8"));
 					/* 設定activity title, ex: 226 即時資訊 */
 					this.setTitle(line + " " + getResources().getString(R.string.realtime_info));
 
 					//					create_webview_by_url(url);
 
 					rl.removeAllViews();
 
 					rl.addView(process);
 
 					final ScrollView sv = new ScrollView(this);
 					sv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 					rl.addView(sv);
 
 					final TableLayout tl = new TableLayout(this);
 					tl.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 					tl.setOrientation(TableLayout.VERTICAL);
 
 					sv.addView(tl);
 
 					/* 資料由5284我愛巴士提供 */
 					show_info_provider(R.string.provide_by_5284);
 
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
 
 					//					create_webview_by_url(url);
 
 					rl.removeAllViews();
 
 					rl.addView(process);
 
 					final ScrollView sv = new ScrollView(this);
 					sv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 					rl.addView(sv);
 
 					final TableLayout tl = new TableLayout(this);
 					tl.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 					tl.setOrientation(TableLayout.VERTICAL);
 
 					sv.addView(tl);
 
 					/* 資料由台中市政府提供 */
 					show_info_provider(R.string.provide_by_txg);
 
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
 				/* Workaround...偉哉陳菊... */
 				if(line.contentEquals("168東"))
 					line = "環狀東線";
 				else if(line.contentEquals("168西"))
 					line = "環狀西線";
 				else if(line.contentEquals("205中華幹線"))
 					line = "中華幹線";
 				else if(line.contentEquals("70"))
 					line = "三多幹線";
 				else if(line.contentEquals("88"))
 					line = "建國幹線";
 				else if(line.contentEquals("92"))
 					line = "自由幹線";
 				else if(line.contentEquals("旗美快捷"))
 					line = "旗美國道快捷公車";
 				else if(line.contentEquals("旗山快捷"))
 					line = "旗山國道快捷公車";
 				else if(line.contains("五福幹線"))
 					line = "五福幹線";
 
 
 				SimpleDateFormat formatter = new SimpleDateFormat("H");
 				Date date = new Date(System.currentTimeMillis()) ;
 				String str_now = formatter.format(date);
 				int now = Integer.parseInt(str_now);
 
 				if(now > 17 && line.contentEquals("紅36")) {
 					line = "紅36繞駛";		// 為了較好的效能..
 				}
 
 				//				String khh_bus_url = "http://122.146.229.210/bus/pda/businfo.aspx?Routeid={0}&GO_OR_BACK=1&Line=All&lang=Cht";
 
 				String xml_bus_route = "http://122.146.229.210/xmlbus2/GetEstimateTime.xml?routeIds={0}";
 
 				/* 設定activity title, ex: 226 即時資訊 */
 				this.setTitle(line + " " + getResources().getString(R.string.realtime_info));
 
 				try {
 					final String url = MessageFormat.format(xml_bus_route, URLEncoder.encode(line, "UTF-8"));
 					rl.removeAllViews();
 
 					rl.addView(process);
 
 					final ScrollView sv = new ScrollView(this);
 					sv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 					rl.addView(sv);
 
 					final TableLayout tl = new TableLayout(this);
 					tl.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 					tl.setOrientation(TableLayout.VERTICAL);
 
 					sv.addView(tl);
 
 					/* 資料由高雄市政府提供 */
 					show_info_provider(R.string.provide_by_khh);
 
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
 									});
 							task.execute(url);
 							loading.setVisibility(ProgressBar.VISIBLE);
 							handler.postDelayed(this, 30000);
 						}
 					};
 					handler.post(runtask);
 
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
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
 
 					final ScrollView sv = new ScrollView(this);
 					sv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 					rl.addView(sv);
 
 					final TableLayout tl = new TableLayout(this);
 					tl.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 					tl.setOrientation(TableLayout.VERTICAL);
 
 					sv.addView(tl);
 
 					/* 資料由桃園縣政府提供 */
 					show_info_provider(R.string.provide_by_tyn);
 
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
 				String url_runid = "http://www.taiwanbus.tw/aspx/dyBus/BusXMLLine.aspx?Mode=6&Cus=&RouteNo={0}";
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
 				tvparam.addRule(RelativeLayout.BELOW, ID_INTERCITYBUS_CARRIER);
 				sv.setLayoutParams(tvparam);
 				rl.addView(sv);
 
 				final TableLayout tl = new TableLayout(this);
 				tl.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
 				tl.setOrientation(TableLayout.VERTICAL);
 
 				sv.addView(tl);
 
 				/* 設定activity title, ex: 9117 時刻表 */
 				this.setTitle(line + " " + getResources().getString(R.string.realtime_info));
 
 				/* 資料由公路總局提供 */
 				show_info_provider(R.string.provide_by_bus);
 
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
 										if(i == 0)
 											tv.setText(String.format("%s: %s", getResources().getString(R.string.carrier), infos1[4]));
 										/* 3126,32,0,台北→高雄,阿羅哈客運 */
 										if(i+1 < branches.length) {
 											String[] infos2 = branches[i+1].split(",");
 											if(infos1[2].equals(infos2[2])) {	// 有回頭車
 												Log.i(TAG, "有回頭車");
 												runid.add(String.format("%s,%s", infos1[0], infos2[0]));
 												listItems.add(infos1[3].replaceAll("→", "-"));
 												i++;
 												continue;
 											}
 										}
 										else {	// 沒有回頭車
 											Log.i(TAG, "沒有回頭車");
 											runid.add(String.format("%s", infos1[0]));
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
 
 												if(lines.length == 2)
 													get_real_time(headway[branch], lines[0], lines[1]);
 												else
 													get_real_time(headway[branch], lines[0], null);
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
 
 										if(lines.length == 2)
 											get_real_time(listItems.get(0), lines[0], lines[1]);
 										else
 											get_real_time(listItems.get(0), lines[0], null);
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
 
 								private void get_real_time(CharSequence headway, final String runid_go,	final String runid_back) {
 									/* 即時 http://www.taiwanbus.tw/aspx/dyBus/BusXMLLine.aspx?Mode=4&RunId=3362 */
 									Log.i(TAG, String.format("%s %s ", runid_go, runid_back));
 									String url = "http://www.taiwanbus.tw/aspx/dyBus/BusXMLLine.aspx?Mode=4&RunId={0}";
 									try {
 										final String url_go = MessageFormat.format(url, URLEncoder.encode(runid_go, "UTF-8"));
 										final String url_bk = runid_back == null ? null : MessageFormat.format(url, URLEncoder.encode(runid_back, "UTF-8"));
 										Log.i(TAG, String.format("%s", url_go));
 										Log.i(TAG, String.format("%s", url_bk));
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
 												});
 												if(url_bk != null)
 													task.execute(url_go, url_bk);
 												else
 													task.execute(url_go);
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
 							});
 					get_runid.execute(url);
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
 
 	private void find_start_dest(List<BusRoute> routes, String dept, String arr) {
 		BusRoute start = null, end = null;
 		int maxtime = 999;
 		boolean match_depart = false, match_arr = false;
 
 		dept = string_replace(dept);
 		arr = string_replace(arr);
 
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
 
 			/* Check for departure/arrival stops */
 			if(end == null && ( dept.contains(stopname) || stopname.contains(dept) )) {
 				if(dept.contentEquals(stopname)) {
 					start = temp;
 					match_depart = true;
 				}
 				else if(match_depart == false)
 					start = temp;
 			}
 			else if(start != null && ( arr.contains(stopname) || stopname.contains(arr) )) {
 				if(match_arr == false && arr.contentEquals(stopname)) {
 					end = temp;
 					match_arr = true;
 				}
 				else if(end == null && match_arr == false) {
 					end = temp;
 				}
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
 			/* 故意多一行空白在最後一行 */
 			tr = CreateTableRow(tl);
 			tr.addView(new TextView(this));
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
 		original_sta.add("台視"); after_sta.add("臺視");
 		original_sta.add("台安醫院"); after_sta.add("臺安醫院");
 		original_sta.add("(松壽路)"); after_sta.add("(松壽)");
 		original_sta.add("(松仁路)"); after_sta.add("(松仁)");
 		original_sta.add("松山火車站"); after_sta.add("松山車站");
 		original_sta.add("萬華火車站"); after_sta.add("萬華車站");
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
 	public boolean create_webview_by_url(String url) {
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
 
 		wv.loadUrl(url);
 		loading.setVisibility(ProgressBar.VISIBLE);
 
 		RelativeLayout rl = (RelativeLayout)findViewById(R.id.rl_pop_transit);
 		RelativeLayout.LayoutParams webview_param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
 		webview_param.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 		wv.setLayoutParams(webview_param);
 
 		rl.addView(wv);
 
 		return true;
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
 
 							if(time[i].contentEquals("即將進站"))
 								time[i]  = "1";
 							else if(time[i].contentEquals("進站中"))
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
 
 }
