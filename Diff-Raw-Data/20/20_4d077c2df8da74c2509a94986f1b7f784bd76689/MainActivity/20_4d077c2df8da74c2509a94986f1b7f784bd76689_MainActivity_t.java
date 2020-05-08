 package com.example.testgateway;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import senseHuge.Dao.DBHelper;
 import senseHuge.Dao.TelosbDao;
 import senseHuge.listener.Listenable;
 import senseHuge.listener.MyEvent;
 import senseHuge.listener.MySource;
 import senseHuge.model.PackagePattern;
 import senseHuge.model.RingBuffer;
 import senseHuge.model.TelosbPackage;
 import senseHuge.service.LocalConfigService;
 import senseHuge.util.HttpClientUtil;
 import senseHuge.util.SerialUtil;
 import senseHuge.util.XmlTelosbPackagePatternUtil;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android_serialport_api.SerialPort;
 
 public class MainActivity extends FragmentActivity {
 
 	private static final int capibity = 50;
	public static boolean isWork = false;
 	private static final String tag = "sensehuge:";
 	protected static final String tags = "sensehuge:";
 	public TelosbDao telosbDao;
 	public SerialPort mSerialPort ;
 	SerialUtil serialUtil = new SerialUtil();
 	HttpClientUtil httpClientUtil;
 	PackagePattern packagePattern = null;
 	XmlTelosbPackagePatternUtil xmlTelosbPackagePatternUtil;
 	public HaveData havadata = null;
    //HaveData havadata = new HaveData();
 	List<String> list = new ArrayList<String>();
 	int listSingnal = 0;
 
 	public RingBuffer<String> ringBuffer = new RingBuffer<String>(capibity);
 
 	MySource ms;
 	MySource httpserverState;
 	MySource serialState;
 	MySource havePackage;
 
 	FragmentManager manager;
 	LinearLayout layout;
 	Fragment f_serialPort, f_server, f_listnode,f_nodeSetting,f_alertSetting,f_dataCenter,f_aboutUs;
 	Button serialPortSetting;
 	Button serverSetting;
 	Button sinkSetting;
 	Button alertSetting;
 	Button sinkCheck;
 	Button internetSetting;
 	Button wifiSetting;
 	Button dataCenter;
 	Button aboutUs;
 	Button quit;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// ڣڵ
 		manager = getSupportFragmentManager();
 		FragmentTransaction transaction = manager.beginTransaction();
 		f_serialPort = new Fragment_serialconfig();
 		f_server = new Fragment_serverconfig();
 		f_listnode = new Fragment_listNode();
 		f_nodeSetting = new Fragment_nodeSetting();
 		f_alertSetting = new Fragment_alertSetting();
 		f_dataCenter = new Fragment_dataCenter();
 		f_aboutUs = new Fragment_aboutUs();
 		
 		// õťԼðť
 		serialPortSetting = (Button) findViewById(R.id.serialPortSetting);
 		serverSetting = (Button) findViewById(R.id.serverSetting);
 		sinkSetting = (Button) findViewById(R.id.sinkSetting);
 		alertSetting = (Button) findViewById(R.id.alertSetting);
 		sinkCheck = (Button) findViewById(R.id.sinkCheck);
 		internetSetting = (Button) findViewById(R.id.internetSetting);
 		wifiSetting = (Button) findViewById(R.id.wifiSetting);
 		dataCenter = (Button) findViewById(R.id.dataCenter);
 		aboutUs = (Button) findViewById(R.id.aboutUs);
 		quit = (Button) findViewById(R.id.quit);
 
 		serialPortSetting.setOnClickListener(new ButtonClickListener());
 		serverSetting.setOnClickListener(new ButtonClickListener());
 		sinkSetting.setOnClickListener(new ButtonClickListener());
 		alertSetting.setOnClickListener(new ButtonClickListener());
 		sinkCheck.setOnClickListener(new ButtonClickListener());
 		internetSetting.setOnClickListener(new ButtonClickListener());
 		wifiSetting.setOnClickListener(new ButtonClickListener());
 		dataCenter.setOnClickListener(new ButtonClickListener());
 		aboutUs.setOnClickListener(new ButtonClickListener());
 		quit.setOnClickListener(new ButtonClickListener());
 
 		//Ĭ񣺽ڵ
 		transaction.add(R.id.fragment_container, f_serialPort);
 		transaction.commit();
 
 		init();
 		System.out.println("inited:");
 	}
 
 	class ButtonClickListener implements OnClickListener {
 		FragmentTransaction transaction;
 
 		@Override
 		public void onClick(View v) {
 			switch (v.getId()) {
 			case R.id.serialPortSetting: {
 				transaction = manager.beginTransaction();
 				transaction.replace(R.id.fragment_container, f_serialPort);
 				Log.i(tag, "button1");
 				transaction.commit();
 				break;
 			}
 			case R.id.serverSetting: {
 				transaction = manager.beginTransaction();
 				transaction.replace(R.id.fragment_container, f_server);
 				transaction.commit();
 				break;
 			}case R.id.sinkSetting:
 				transaction = manager.beginTransaction();
 				transaction.replace(R.id.fragment_container, f_nodeSetting);
 				transaction.commit();
 				break;
 			case R.id.alertSetting:
 				transaction = manager.beginTransaction();
 				transaction.replace(R.id.fragment_container, f_alertSetting);
 				transaction.commit();
 				break;
 			case R.id.sinkCheck: {
 				transaction = manager.beginTransaction();
 				transaction.replace(R.id.fragment_container, f_listnode);
 				transaction.commit();
 				break;
 			}
 
 			case R.id.internetSetting: {
 				startActivity(new Intent(
 						android.provider.Settings.ACTION_SETTINGS));
 				break;
 			}
 			case R.id.wifiSetting: {
 				startActivity(new Intent(
 						android.provider.Settings.ACTION_WIFI_SETTINGS));
 				break;
 			}
 			case R.id.dataCenter: {
 				transaction = manager.beginTransaction();
 				transaction.replace(R.id.fragment_container, f_dataCenter);
 				transaction.commit();
 				break;
 			}case R.id.aboutUs:
 				transaction = manager.beginTransaction();
 				transaction.replace(R.id.fragment_container, f_aboutUs);
 				transaction.commit();
 				break;
 			case R.id.quit: {
 				finish();
 				break;
 			}
 			default: {
 				transaction.replace(R.id.fragment_container, f_listnode);
 				transaction.commit();
 				break;
 			}
 			}
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main, menu);
 		// menu.add(0,1,1,"quit");//asshow = never
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.serialPort_check:
 
 			break;
 		case R.id.server_check:
 
 			break;
 		case R.id.quit:
 			finish();
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * mainactivity ʼ
 	 */
 	public void init() {
 		LocalConfigService localConfigService = new LocalConfigService(
 				getBaseContext());
 		localConfigService.setConfig("webserver", "192.168.10.145");
 		System.out.println(localConfigService.getConfig("webserver"));
 
 		xmlTelosbPackagePatternUtil = new XmlTelosbPackagePatternUtil(
 				getFilesDir().toString());
 		
 		System.out
 				.println(xmlTelosbPackagePatternUtil.getPackagePattern().TelosbDataField
 						.size() + "__________%%%_______");
 		Log.i("XmlTelosbPackagePatternUtil",
 				xmlTelosbPackagePatternUtil.getPackagePattern().TelosbDataField
 						.size() + "__________%%%_______");
 		httpClientUtil = new HttpClientUtil(getBaseContext());
 		
 		httpserverState = new MySource();
 		serialState = new MySource();
 		ms = new MySource();
 		havePackage = new MySource();
 		// MyListener ml=new MyListener();
 		HttpserverState hl = new HttpserverState();
 		SerialListener ml = new SerialListener();
 		PackageListener pl = new PackageListener();
 		httpserverState.addListener(hl);
 		serialState.addListener(ml);
 		havePackage.addListener(pl);
 
 		havePackage.setValue(false);
 		telosbDao = new TelosbDao(getBaseContext());
 
 		httpserverState.setValue(true);
 		serialState.setValue(true);
 		httpserverState.setValue(false);
 		System.out.println("end:");
 		try {
 			// createTable();
 			// insert() ;
 			test();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 /*		httpserverState = new MySource();
 		serialState = new MySource();
 		ms = new MySource();
 		havePackage = new MySource();
 		// MyListener ml=new MyListener();
 		HttpserverState hl = new HttpserverState();
 		SerialListener ml = new SerialListener();
 		PackageListener pl = new PackageListener();
 		httpserverState.addListener(hl);
 		ms.addListener(ml);
 		havePackage.addListener(pl);
 		
 		havePackage.setValue(false);
 		telosbDao = new TelosbDao(getBaseContext());
 		
 		httpserverState.setValue(true);
 		ms.setValue(true);
 		httpserverState.setValue(false);
 		System.out.println("end:");
 		try {
 			// createTable();
 			// insert() ;
 			test();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 */	}
 
 	public void ProcessData() {
 		if (listSingnal == 0) {
 			listSingnal = 1;
 		} else {
 			return;
 		}
 
 		while (list.size() > 0) {
 			System.out.println("ɹ" + list.get(0));
 			list.remove(0);
 
 		}
 
 		listSingnal = 0;
 		// Packagewaite();
 	}
 
 	public class SerialListener implements Listenable {
 
 		@Override
 		public void eventChanged(MyEvent e) {
 			// TODO Auto-generated method stub
 			System.out.println("e:" + e.getValue());
 			if (serialState.value) {
 				System.out.println("ڴ򿪣XXXXXXXXXXXXXXXXXXXXXXXXXXX");
 			} else {
 
 				System.out.println("ڹرСXXXXXXXXXXXXXXXXXXXXS");
 			}
 		}
 	}
 
 	public class PackageListener implements Listenable {
 
 		@Override
 		public void eventChanged(MyEvent e) {
 			// TODO Auto-generated method stub
 			System.out.println("e:" + e.getValue());
 			if (ms.value) {
 				System.out.println("յݣڳ");
 				ProcessData();
 			} else {
 				System.out.println("ݽϣȴС");
 			}
 		}
 	}
 
 	public class HttpserverState implements Listenable {
 		@Override
 		public void eventChanged(MyEvent e) {
 			// TODO Auto-generated method stub
 			System.out.println("e:" + e.getValue());
 			if (e.getValue()) {
 				System.out.println("ӣ");
 			} else {
 				System.out.println("ѶϿ");
 			}
 		}
 	}
 
 	public class HaveData extends Thread {
 
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			super.run();
 
 			// ȡݰ
 			String headTest = "00FFFF";
 			TelosbPackage telosbPackage = new TelosbPackage();
 			int i;
 			
 			while (isWork) {
 				
 				i = serialUtil.findhead(headTest);
 				if (i < 0 && serialUtil.stringBuffer.length() > 6) {
 					serialUtil.delete(serialUtil.stringBuffer.length() - 6);
 				} else if (i > 0) {
 					serialUtil.delete(i);
 				}
 				if (serialUtil.stringBuffer.length() > 300) {
 					System.out
 							.println("++++++++++++++++++++++++++++++++++++++++++++++++++");
 					System.out.println(serialUtil.stringBuffer.toString());
 					System.out
 							.println("+++++++++++++++++++++++++++++++++++++++++++++");
 					/*
 					 * //int i = serialUtil.findhead("00FFFF"); if (i < 0) {
 					 * serialUtil.delete(serialUtil.stringBuffer.length()-6); }
 					 * else { System.out.println("start:"+i);
 					 * serialUtil.delete(i);
 					 */
 					String telosbData = serialUtil.getFirstData();
 					System.out.println("receive package:" + telosbData);
 
 					if (telosbData == "") {
 						System.out.println("empty");
 
 					} else {
 						System.out
 								.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
 						try {
 							System.out.println(xmlTelosbPackagePatternUtil
 									.parseTelosbPackage(telosbData).getCtype()
 									+ ":%%%%%%%%%%%%%%");
 						} catch (Exception e) {
 
 							e.printStackTrace();
 						}
 						;
 
 						PackagePattern telosbPackagePattern = null;
 						try {
 
 							telosbPackagePattern = xmlTelosbPackagePatternUtil
 									.parseTelosbPackage(telosbData);
 						} catch (Exception e) {
 
 							e.printStackTrace();
 
 						}
 						if (httpClientUtil.PostTelosbData("", telosbData)) {
 
 							telosbPackage.setStatus("ϴ");
 
 						} else {
 
 							telosbPackage.setStatus("δϴ");
 
 						}
                        if (telosbPackagePattern.ctype==null || telosbPackagePattern.ctype == "") {
							System.out.println("errot");
						}
 						telosbPackage.setCtype(telosbPackagePattern.ctype);
 						telosbPackage.setMessage(telosbData);
 						telosbPackage.setNodeID(telosbPackagePattern.nodeID);
 						boolean result = telosbDao
 								.insertTelosbPackage(telosbPackage);
 						if (result) {
 							System.out
 									.println("insert telosbPackage to database success%%");
 						} else {
 							System.out
 									.println("insert telosbPackage to database fail    ");
 						}
 						if (telosbData != null) {
 							list.add(telosbData);
 							Packagesingnal();
 						}
 						System.out.println(list.size() + "_____________");
 						System.gc();
 					}
 				}
 			}
 
 		}
 	}
 
 	public HaveData getHaveData(){
 		return new HaveData();
 	}
 	public void Packagesingnal() {
 		havePackage.setValue(true);
 	}
 
 	public void Packagewaite() {
 		havePackage.setValue(false);
 	}
 
 	public RingBuffer<String> getRingBuffer() {
 		return ringBuffer;
 	}
 
 	public void setRingBuffer(RingBuffer<String> ringBuffer) {
 		this.ringBuffer = ringBuffer;
 	}
 
 	/**
 	 * TEst
 	 */
 	public void createTable() throws Exception {
 		DBHelper dbHelper = new DBHelper(this.getBaseContext());
 		dbHelper.open();
 
 		String deleteSql = "drop table if exists user ";
 		dbHelper.execSQL(deleteSql);
 
 		// idԶusername passwordΪֶ textΪֶε
 		String sql = "CREATE TABLE user (id integer primary key autoincrement, username text, password text)";
 		dbHelper.execSQL(sql);
 		dbHelper.closeConnection();
 	}
 
 	public void insert() throws Exception {
 		DBHelper dbHelper = new DBHelper(this.getBaseContext());
 		dbHelper.open();
 
 		ContentValues values = new ContentValues(); // ൱map
 
 		values.put("username", "test");
 		values.put("password", "123456");
 
 		dbHelper.insert("user", values);
 
 		dbHelper.closeConnection();
 	}
 
 	public void test() {
 
 		/*
 		 * telosbDao.insertTelosbPackage(); TelosbPackage telosbPackage = new
 		 * TelosbPackage(); telosbPackage.setId(4);
 		 * telosbDao.updateTelosbPackageStatus(telosbPackage);
 		 * telosbPackage.setId(9); telosbDao.deleteTelosbPackage(telosbPackage);
 		 * //telosbDao.findUploadList();
 		 * System.out.println(telosbDao.findUploadList().size());
 		 * System.out.println(telosbDao.findUnUploadList().size());
 		 */
 	}
 
 	/*
 	 * @SuppressLint("NewApi") public void initHttp(){
 	 * StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
 	 * .detectDiskReads() .detectDiskWrites() .detectNetwork() .penaltyLog()
 	 * .build());
 	 * 
 	 * StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
 	 * .detectDiskReads() .detectDiskWrites() .detectNetwork() // or
 	 * .detectAll() for all detectable problems .penaltyLog() .build());
 	 * StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
 	 * .detectLeakedSqlLiteObjects() .detectLeakedClosableObjects()
 	 * .penaltyLog() .penaltyDeath() .build()); HttpClient httpClient;
 	 * httpClient = new DefaultHttpClient();
 	 * httpClient.getParams().setParameter(
 	 * CoreConnectionPNames.CONNECTION_TIMEOUT, 1000); HttpPost post = new
 	 * HttpPost( "http://192.168.10.107:8080/foo/login.jsp");
 	 * List<NameValuePair> params = new ArrayList<NameValuePair>(); String name
 	 * ="1"; params .add(new BasicNameValuePair("name", name)); String pass
 	 * ="1"; params .add(new BasicNameValuePair("pass", pass ));
 	 * 
 	 * 
 	 * 
 	 * 
 	 * try { //  post.setEntity(new UrlEncodedFormEntity( params,
 	 * HTTP.UTF_8)); // POST HttpResponse response = httpClient
 	 * .execute(post); // ɹطӦ if (response.getStatusLine()
 	 * .getStatusCode() == 200) { String msg = EntityUtils
 	 * .toString(response.getEntity()); // ʾ¼ɹ Toast.makeText(this, msg,
 	 * 5000).show(); System.out.println("hello:"+msg); Log.i(tag, msg); } }
 	 * catch (Exception e) { e.printStackTrace(); } }
 	 */
 
 	public void testXMLParse() {
 		/*
 		 * String C2=
 		 * "00FFFF00005A00EE000000000000F9C20000C200000000045FC53C045FC53D1BFE53F9020000DC0A000A0001EA0A00090000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
 		 * ; xmlTelosbPackagePatternUtil.Parse(packagePattern, "C2", C2); String
 		 * C1=
 		 * "00FFFF00005C00EE00000000000003C10000C100000000046051B3046051B41C005403000000001F00CCCB5200000000000000000B780FB1076019D7000500000000009A00000000FFFF0100000000000000000000000000000000000000000000000000"
 		 * ; // xmlTelosbPackagePatternUtil.Parse(packagePattern, "C1", C1); //
 		 * PackagePattern packagePattern1
 		 * =xmlTelosbPackagePatternUtil.parseTelosbPackage(C1, packagePattern);
 		 * PackagePattern packagePattern1 = null; try { packagePattern1 =
 		 * xmlTelosbPackagePatternUtil.parseTelosbPackage(C1); } catch
 		 * (Exception e) { // TODO Auto-generated catch block
 		 * e.printStackTrace(); }
 		 * System.out.println("size:"+packagePattern1.getDataField
 		 * ().size()+";length ="+packagePattern1.getMessageLength());
 		 */
 	}
 
 }
