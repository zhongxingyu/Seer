 package com.foreveross.chameleon;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.foreveross.chameleon.util.*;
 
 import org.acra.CrashReport;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.HttpVersion;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.graphics.Rect;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.Toast;
 
 import com.csair.impc.R;
 import com.foreveross.chameleon.activity.FacadeActivity;
 import com.foreveross.chameleon.event.EventBus;
 import com.foreveross.chameleon.event.PresenceEvent;
 import com.foreveross.chameleon.imagemanager.AsyncImageManager;
 import com.foreveross.chameleon.imagemanager.LruImageCache;
 import com.foreveross.chameleon.phone.ActivityManager;
 import com.foreveross.chameleon.phone.modules.CubeApplication;
 import com.foreveross.chameleon.phone.modules.CubeModule;
 import com.foreveross.chameleon.phone.modules.CubeModuleManager;
 import com.foreveross.chameleon.phone.modules.LoginModel;
 import com.foreveross.chameleon.phone.modules.MessageFragmentModel;
 import com.foreveross.chameleon.phone.modules.task.ThreadPlatformUtils;
 import com.foreveross.chameleon.phone.muc.MucManager;
 import com.foreveross.chameleon.push.client.NotificationService;
 import com.foreveross.chameleon.push.client.NotificationService.NotificationServiceBinder;
 import com.foreveross.chameleon.push.client.Notifier;
 import com.foreveross.chameleon.push.client.XmppManager;
 import com.foreveross.chameleon.push.client.XmppManager.Type;
 import com.foreveross.chameleon.push.cubeparser.type.NoticeModuleMessage;
 import com.foreveross.chameleon.push.mina.library.api.MinaMobileClient;
 import com.foreveross.chameleon.push.mina.library.api.SessionIdAware;
 import com.foreveross.chameleon.push.mina.library.inf.vo.DeviceCheckinVo;
 import com.foreveross.chameleon.push.mina.library.inf.vo.TagEntryVo;
 import com.foreveross.chameleon.push.mina.library.service.MinaPushService;
 import com.foreveross.chameleon.push.mina.library.service.MinaPushService.MinaPushServiceBinder;
 import com.foreveross.chameleon.push.mina.library.util.NetworkUtil;
 import com.foreveross.chameleon.push.mina.library.util.PropertiesUtil;
 import com.foreveross.chameleon.push.mina.library.util.ThreadPool;
 import com.foreveross.chameleon.push.parser.OriginalParser;
 import com.foreveross.chameleon.push.receiver.MdmSubsrcriber;
 import com.foreveross.chameleon.service.ModuleOperationService;
 import com.foreveross.chameleon.service.ModuleOperationService.ModuleOperationServiceBinder;
 import com.foreveross.chameleon.store.core.ModelCreator;
 import com.foreveross.chameleon.store.core.ModelFinder;
 import com.foreveross.chameleon.store.core.StaticReference;
 import com.foreveross.chameleon.store.model.IMModelManager;
 import com.foreveross.chameleon.store.model.ViewModuleRecord;
 import com.google.gson.Gson;
 import com.j256.ormlite.stmt.DeleteBuilder;
 import com.squareup.otto.Subscribe;
 
 /**
  * o [一句话功能简述]<BR>
  * [功能详细描述]
  * 
  * @author 冯伟立
  * @version [CubeAndroid, 2013-7-16]修改
  */
 public class Application extends android.app.Application implements
 		SessionIdAware {
 
 	/** 全局SharePreferences对象 */
 	public static SharedPreferences sharePref = null;
 
 	public static boolean isAppExit = false;
 
 	private int loginType;
 	public  static String  token = null;
 
 	private ActivityManager activityManager = null;
 	private final static Logger log = LoggerFactory
 			.getLogger(Application.class);
 	private HttpClient httpClient; // 采用apache网络连接组件
 	/**
 	 * [作用描述]
 	 */
 	private CubeApplication cubeApplication;
 
 	public CubeApplication getCubeApplication() {
 		return cubeApplication;
 	}
 
 	public NotificationService getNotificationService() {
 		return notificationService;
 	}
 
 	public void setNotificationService(NotificationService notificationService) {
 		this.notificationService = notificationService;
 	}
 
 	public void setCubeApplication(CubeApplication application) {
 		this.cubeApplication = application;
 	}
 
 	public ActivityManager getActivityManager() {
 
 		return activityManager;
 
 	}
 
 	public void setActivityManager(ActivityManager activityManager) {
 		this.activityManager = activityManager;
 	}
 
 	/** 初始化配置 */
 	public void onCreate() {
 		super.onCreate();
 		initConfig();
 		initStores();
 		initServices();
 		initHandlers();
 		initPosts();
 		httpClient = createHttpClient();
 		// 初始化自定义Activity管理器
 		activityManager = ActivityManager.getScreenManager();
 
 		CrashReport crashReport = new CrashReport();
 		crashReport.start(this);
 		
 		// 初始化图片管理器
 		final int memClass = ((android.app.ActivityManager) getSystemService(Context.ACTIVITY_SERVICE))
 				.getMemoryClass();
 		// Use 1/8th of the available memory for this memory cache.
 		final int cacheSize = 1024 * 1024 * memClass / 8;
 		AsyncImageManager.buildInstance(new LruImageCache(cacheSize));
 
 		// 效能监控启动定时器代码
 
 		 mHandler.removeCallbacks(mUpdateTimeTask);
 		 mHandler.postDelayed(mUpdateTimeTask, 60000);
 		 
 
 	}
 
 	/**
 	 * 自个写，框架里面的耦合性太强，下面子类无法感知请求返回状态 post请求
 	 * 
 	 * @param url
 	 *            url地址
 	 * @param map
 	 *            请求参数
 	 * @return 返回的内容
 	 * @throws IOException
 	 */
 	private static int DEFAULT_CONNECT_TIMEOUT = 10000;
 
 	private static int DEFAULT_SO_TIMEOUT = 10000;
 
 	public static HttpResponse postJson(String url, JSONArray jsons)
 			throws IOException, JSONException {
 
 		HttpParams httpParams = new BasicHttpParams();
 		HttpConnectionParams.setConnectionTimeout(httpParams,
 				DEFAULT_CONNECT_TIMEOUT);
 		HttpConnectionParams.setSoTimeout(httpParams, DEFAULT_SO_TIMEOUT);
 
 		DefaultHttpClient client = new DefaultHttpClient(httpParams);
 		HttpPost post = new HttpPost(url);
 		post.setEntity(new UrlEncodedFormEntity(getHttpData(jsons), HTTP.UTF_8));
 
 		return client.execute(post);
 	}
 
 	/**
 	 * post方法map转化为httpClient参数的通用转化方法
 	 * 
 	 * @param map
 	 * @return
 	 * @throws JSONException
 	 */
 	public static List<NameValuePair> getHttpData(JSONArray jsons)
 			throws JSONException {
 
 		List<NameValuePair> list = new ArrayList<NameValuePair>();
 
 		String key = "postString";
 		String val = jsons.toString();
 		NameValuePair nameValue = new BasicNameValuePair(key, val);
 		list.add(nameValue);
 
 		return list;
 	}
 
 	String realTableName = "ViewModuleRecord";
 
 	public void delectedDataFromTable(String[] datetime) {
 		StaticReference.userMf.deleteBuilder(ViewModuleRecord.class).where();
 
 		DeleteBuilder<ViewModuleRecord, Long> viewDeleteBuilder = StaticReference.userMf
 				.deleteBuilder(ViewModuleRecord.class);
 		try {
 			viewDeleteBuilder.where().in("datetimes", datetime);
 			viewDeleteBuilder.delete();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	long handlerStartTime = 0L;
 	Handler mHandler = new Handler();
 	Runnable mUpdateTimeTask = new Runnable() {
 
 		@Override
 		public void run() {
 
 			final long start = handlerStartTime;
 			long millis = System.currentTimeMillis() - start;
 			int seconds = (int) (millis / 1000);
 			int minutes = seconds / 60;
 			seconds = seconds % 60;
 			final String sessionKey = Preferences
 					.getSESSION(Application.sharePref);
 			final String appKey = getCubeApplication().getAppKey();
 
 			System.out.println("----------------------线程开启了：" + minutes + "分，"
 					+ seconds + "秒");
 			try {
 				final JSONArray ja = getJsonFromTable();
 				System.out.println("上传JSON:" + ja.toString());
 				if (!ja.toString().equals("[]")) {
 					new Thread(new Runnable() {
 
 						@Override
 						public void run() {
 							String posturl = URL.BASE_WS
 									+ "csair-monitor/api/monitor/saveAll?appKey="
 									+ appKey + "&sessionKey=" + sessionKey;
 							try {
 								System.out.println("开始上传");
 								HttpResponse response = postJson(posturl, ja);
 								String result = "";
 								int status = response.getStatusLine()
 										.getStatusCode();
 								Log.e("post请求返回状态为：", "" + status);
 								if (status == 200) {
 									result = EntityUtils.toString(response
 											.getEntity());
 									int len = ja.length();
 									for (int i = 0; i < len; i++) {
 										try {
 											delectedDataFromTable(new String[] { ja
 													.getJSONObject(i)
 													.getString("datetimes") });
 										} catch (JSONException e) {
 											// TODO Auto-generated catch block
 											e.printStackTrace();
 										}
 									}
 								}
 							} catch (IOException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							} catch (JSONException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 						}
 					}).start();
 				}
 			} catch (Exception e) {
 				System.out.println("-----------------线程异常终止了：" + minutes + "分，"
 						+ seconds + "秒");
 				return;
 			}
 			mHandler.postDelayed(this, 60000);
 		}
 
 	};
 
 	public JSONArray getJsonFromTable() throws JSONException {
 		List<ViewModuleRecord> viewModuleRecord = null;
 		try {
 			viewModuleRecord = StaticReference.userMf.queryBuilder(
 					ViewModuleRecord.class).query();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Gson gson = new Gson();
 		String json = gson.toJson(viewModuleRecord);
 		JSONArray jsons = new JSONArray(json);
 		return jsons;
 	}
 
 	public void saveModulerRecord(CubeModule cubeModule) {
 
 		String userName = Preferences.getUserName(Application.sharePref);
 		String name = this.getPackageName();
 		String appName = name.substring(name.length() - 8, name.length());
 		Date date = new Date();
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		String loginTime = sdf.format(date);
 		ViewModuleRecord v = new ViewModuleRecord();
 		v.setAction("Module");
 		v.setAppName(appName);
 		v.setClassName("");
 		v.setMethodName("");
 		v.setModuleName(cubeModule.getName());
 		v.setUserName(userName);
 		v.setDatetimes(loginTime);
 		StaticReference.userMf.createOrUpdate(v);
 	}
 
 	public int getStatusHeight(Activity activity) {
 		int statusHeight = 0;
 		Rect localRect = new Rect();
 		activity.getWindow().getDecorView()
 				.getWindowVisibleDisplayFrame(localRect);
 		statusHeight = localRect.top;
 		if (0 == statusHeight) {
 			Class<?> localClass;
 			try {
 				localClass = Class.forName("com.android.internal.R$dimen");
 				Object localObject = localClass.newInstance();
 				int i5 = Integer.parseInt(localClass
 						.getField("status_bar_height").get(localObject)
 						.toString());
 				statusHeight = activity.getResources()
 						.getDimensionPixelSize(i5);
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 			} catch (IllegalArgumentException e) {
 				e.printStackTrace();
 			} catch (SecurityException e) {
 				e.printStackTrace();
 			} catch (NoSuchFieldException e) {
 				e.printStackTrace();
 			}
 		}
 		return statusHeight;
 	}
 
 	Activity a;
 
 	/**
 	 * 
 	 * @Method: showTopWindow
 	 * @Description: 显示最顶层view
 	 */
 	public void showTopWindow(Activity c) {
 		this.a = c;
 		View views = null;
 		if (PadUtils.isPad(this)) {
 			views = LayoutInflater.from(this).inflate(
 					R.layout.pad_splash_screen, null);
 		} else {
 			views = LayoutInflater.from(this).inflate(
 					R.layout.phone_splash_screen, null);
 		}
 		WindowManager windowManager = (WindowManager) getApplicationContext()
 				.getSystemService(WINDOW_SERVICE);
 		WindowManager.LayoutParams params = new WindowManager.LayoutParams();
 		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
 				| WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
 		int screenWidth = windowManager.getDefaultDisplay().getWidth();
 		int screenHeight = windowManager.getDefaultDisplay().getHeight(); // 屏幕高
 		params.x = 0;
 		params.y = 0;
 		params.width = screenWidth;
 		if (PadUtils.isPad(this)) {
 			params.height = screenHeight;
 
 		} else {
 			params.height = screenHeight - getStatusHeight(c);
 		}
 		final View view = views;
 		// topWindow显示到最顶部
 		windowManager.addView(view, params);
 
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					Thread.sleep(2400);
 					clearTopWindow(view);
 
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 
 			}
 		}).start();
 	}
 
 	/**
 	 * 
 	 * @Method: clearTopWindow
 	 * @Description: 移除最顶层view
 	 */
 	public void clearTopWindow(View view) {
 		if (view != null && view.isShown()) {
 			WindowManager windowManager = (WindowManager) getApplicationContext()
 					.getSystemService(WINDOW_SERVICE);
 			windowManager.removeView(view);
 		}
 	}
 
 	private void shutdownHttpClient() {
 		if (httpClient != null && httpClient.getConnectionManager() != null) {
 			httpClient.getConnectionManager().shutdown();
 		}
 	}
 
 	private HttpClient createHttpClient() {
 		HttpParams params = new BasicHttpParams();
 		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
 		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
 		HttpProtocolParams.setUseExpectContinue(params, true);
 		SchemeRegistry schReg = new SchemeRegistry();
 		schReg.register(new Scheme("http", PlainSocketFactory
 				.getSocketFactory(), 80));
 		schReg.register(new Scheme("https",
 				SSLSocketFactory.getSocketFactory(), 443));
 		// 解决多线程访问安全问题
 		ClientConnectionManager connectionManager = new ThreadSafeClientConnManager(
 				params, schReg);
 		return new DefaultHttpClient(connectionManager, params);
 	}
 
 	public HttpClient getHttpClient() {
 		if (httpClient != null) {
 			return httpClient;
 		} else {
 			return createHttpClient();
 		}
 	}
 
 	public void initPosts() {
 	}
 
 	/**
 	 * [初始化服务]<BR>
 	 * [功能详细描述] 2013-7-16 上午11:53:16
 	 */
 	private void initServices() {
 		this.bindService(ModuleOperationService.getIntent(this),
 				moduleServiceConnection, Context.BIND_AUTO_CREATE);
 		this.bindService(NotificationService.getIntent(this),
 				notificationServiceConnection, Context.BIND_AUTO_CREATE);
 //		this.bindService(new Intent(this, MinaPushService.class),
 //				minaServiceConnection, Context.BIND_AUTO_CREATE);
 
 	}
 
 	/**
 	 * [初始化handler]<BR>
 	 * [功能详细描述] 2013-7-16 上午11:53:29
 	 */
 	private void initHandlers() {
 		initUIHandler();
 		// new Timer().schedule(new TimerTask() {
 		//
 		// @Override
 		// public void run() {
 		// if (hasLogined) {
 		//
 		// Pool.run(new Runnable() {
 		//
 		// @Override
 		// public void run() {
 		// int nCount = findNewNoticeCount();
 		// EventBus.getEventBus(
 		// TmpConstants.EVENTBUS_MESSAGE_COUNT)
 		// .post(new MessageCountChangeEvent(
 		// TmpConstants.ANNOUCE_RECORD_IDENTIFIER,
 		// nCount));
 		// }
 		// });
 		// Pool.run(new Runnable() {
 		//
 		// @Override
 		// public void run() {
 		// int mCount = findNewMessageCount();
 		// EventBus.getEventBus(
 		// TmpConstants.EVENTBUS_MESSAGE_COUNT)
 		// .post(new MessageCountChangeEvent(
 		// TmpConstants.MESSAGE_RECORD_IDENTIFIER,
 		// mCount));
 		// }
 		// });
 		//
 		// }
 		//
 		// }
 		// }, 0, 4 * 1000);
 	}
 
 	// TODO[FENGWEILI] find bester method to do it!
 	public int findNewMessageCount() {
 		int count1 = ((int) StaticReference.defMf.queryRawValue(
 				NoticeModuleMessage.class,
 				"select count(*) from NoticeModuleMessageStub where hasRead=0"));
 		int count2 = ((int) StaticReference.defMf.queryRawValue(
 				NoticeModuleMessage.class,
 				"select count(*) from CommonModuleMessage where hasRead=0"));
 		int count3 = ((int) StaticReference.defMf.queryRawValue(
 				NoticeModuleMessage.class,
 				"select count(*) from SystemMessage where hasRead=0"));
 		return count1 + count2 + count3;
 	}
 
 	public int findNewNoticeCount() {
 		return (int) StaticReference.defMf.queryRawValue(
 				NoticeModuleMessage.class,
 				"select count(*) from NoticeModuleMessage where hasRead=0");
 	}
 
 	/**
 	 * [初始化store]<BR>
 	 * [功能详细描述] 2013-7-16 上午11:53:42
 	 */
 	private void initStores() {
 		StaticReference.defMC = ModelCreator.build(this);
 		StaticReference.defMf = ModelFinder.build(this);
 	}
 
 	/**
 	 * [安装应用]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @param cubeModule
 	 *            2013-7-16 上午11:51:48
 	 */
 	public void install(CubeModule cubeModule) {
 		if (moduleOperationService == null) {
 			Toast.makeText(this, "服务未启动成功!", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		moduleOperationService.install(cubeModule);
 	}
 
 	/**
 	 * [卸载应用]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @param cubeModule
 	 *            2013-7-16 上午11:52:09
 	 */
 	public void uninstall(CubeModule cubeModule) {
 		if (moduleOperationService == null) {
 			Toast.makeText(this, "服务未启动成功!", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		moduleOperationService.uninstall(cubeModule);
 	}
 
 	/**
 	 * [更新应用]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @param cubeModule
 	 *            2013-7-16 上午11:52:23
 	 */
 	public void upgrade(CubeModule cubeModule) {
 		if (moduleOperationService == null) {
 			Toast.makeText(this, "服务未启动成功!", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		moduleOperationService.upgrade(cubeModule);
 
 	}
 
 	/**
 	 * [下载附件]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @param attach
 	 *            2013-7-16 上午11:54:36
 	 */
 	public void downloadAttachMent(String attach) {
 		if (moduleOperationService == null) {
 			Toast.makeText(this, "服务未启动成功!", Toast.LENGTH_SHORT).show();
 			throw new IllegalStateException();
 		}
 		moduleOperationService.downloadAttachMent(attach);
 
 	}
 
 	/*
 	 * 初始化配置
 	 */
 
 	private OriginalParser originalParser = null;
 	private MdmSubsrcriber mdmSubsrcriber = null;
 
 	private void initConfig() {
 		initWebUrls();
 		EventBus.registerApp(this);
 		/** 获取SharedPreferences对象 */
 		sharePref = PreferenceManager.getDefaultSharedPreferences(this);
 		Boolean b = Preferences.getFirsttime(Application.sharePref);
 		// 判断是不是第一次安装应用,
 
 		CubeApplication app = CubeApplication.getInstance(this);
 		app.loadApplication();
 		this.setCubeApplication(app);
 		// 同步前先显示ui
 		CubeModuleManager.getInstance().init(app);
 		// 注册
 		EventBus.getEventBus(TmpConstants.EVENTBUS_COMMON).register(
 				originalParser = new OriginalParser(this));
 		EventBus.getEventBus(TmpConstants.EVENTBUS_COMMON).register(this);
 		EventBus.getEventBus(TmpConstants.EVENTBUS_COMMON).register(
 				mdmSubsrcriber = new MdmSubsrcriber(this));
         //开启定位
         new GeoManager(this.getApplicationContext());
 
 	}
 
 	private void initWebUrls() {
 
 		PropertiesUtil propertiesUtil = PropertiesUtil.readProperties(this,
 				CubeConstants.CUBE_CONFIG);
 		URL.ANNOUNCE = propertiesUtil.getString("ANNOUNCE", "");
 		URL.BASE_WEB = propertiesUtil.getString("BASE_WEB", "");
 		URL.MUC_BASE = propertiesUtil.getString("MUC_BASE", "");
 		URL.BASE_WS = propertiesUtil.getString("BASE_WS", "");
         URL.GEOPOSITION_URL=URL.BASE_WS + "csair-mam/api/mam/device/position/add";
 		// 请在cube.properties中配置
 		URL.PAD_MAIN_URL = propertiesUtil.getString("PAD_MAIN_URL", "");
 		URL.PAD_LOGIN_URL = propertiesUtil.getString("PAD_LOGIN_URL", "");
 		URL.PHONE_MAIN_URL = propertiesUtil.getString("PHONE_MAIN_URL", "");
 		URL.PHONE_LOGIN_URL = propertiesUtil.getString("PHONE_LOGIN_URL", "");
 		URL.PHONE_REGISTER_URL = propertiesUtil.getString("PHONE_REGISTER_URL", "");
 		URL.PAD_REGISTER_URL = propertiesUtil.getString("PAD_REGISTER_URL", "");
 		URL.UPLOAD_URL = URL.BASE_WEB + "mam/attachment/clientUpload";
 		URL.SYNC = URL.BASE_WS + "csair-extension/api/csairauth/privileges";
 		URL.UPLOAD = URL.BASE_WS + "csair-mam/api/mam/attachment/upload";
 //		URL.LOGIN = URL.BASE_WS + "csair-extension/api/oalogin/validate";
 		URL.LOGIN = URL.BASE_WS + "csair-extension/api/csairauth/login";
 		URL.UPDATE = URL.BASE_WS + "csair-mam/api/mam/clients/update/android";
 		URL.UPDATE_RECORD = URL.BASE_WS
 				+ "csair-mam/api/mam/clients/update/appcount/android/";
 		URL.SNAPSHOT = URL.BASE_WS + "csair-mam/api/mam/clients/widget/";
 		URL.PUSH_BASE_URL = URL.BASE_WS + "csair-push/api/";
 //		URL.GETPUSHMESSAGE =URL.PUSH_BASE_URL+"receipts/none-receipts/";
 		URL.GETPUSHMESSAGE =URL.PUSH_BASE_URL+"push-msgs/none-receipts/";
 		URL.CHECKIN_URL = URL.PUSH_BASE_URL + "checkinservice/checkins";
 		URL.CHECKOUT_URL = URL.PUSH_BASE_URL + "checkinservice/checkout";
 		URL.FEEDBACK_URL = URL.PUSH_BASE_URL + "receipts";
 		URL.MUC_ALLROOM = URL.MUC_BASE + "csair-im/api/chat/queryAllRoom/"; // :jid/:status
 		URL.MUC_AddMembers = URL.MUC_BASE + "csair-im/api/chat/addMembers"; // :jid/:sex/:status/:roomid/:
 		URL.MUC_AddMember = URL.MUC_BASE + "csair-im/api/chat/addMember"; // :jid/:username/:
 		URL.MUC_QueryMembers = URL.MUC_BASE + "csair-im/api/chat/query/"; // :roomid/:
 		URL.MUC_DeleteMember = URL.MUC_BASE + "csair-im/api/chat/deleteMember/"; // :roomId/:jid
 		URL.MUC_DeleteMembers = URL.MUC_BASE
 				+ "csair-im/api/chat/deleteMembers"; // :jid/:
 		URL.MUC_UpdateStatue = URL.MUC_BASE + "csair-im/api/chat/updateStatue"; // :jid/:statue/:
 		URL.MUC_DeleteRoom = URL.MUC_BASE + "csair-im/api/chat/deleteRoom/"; // :roomid/:
 		URL.MUC_ReRoomName = URL.MUC_BASE
 				+ "csair-im/api/chat/roommember/roomname";// :roomid/:roomName/:
 		URL.CHATDELETE = URL.MUC_BASE + "csair-im/api/chat/delete";// userId/jid
 																	// get
 		URL.CHATSAVE = URL.MUC_BASE + "csair-im/api/chat/save";// chat/save/:jid/:username/:sex/:status/:userId
 		URL.CHATSHOW = URL.MUC_BASE + "csair-im/api/chat/show";// :userId get
 		URL.CHATUPDATE = URL.MUC_BASE + "csair-im/api/chat/update";// :jid/:status
 	}
 
 	public void onTerminate() {
 		super.onTerminate();
 		this.unbindService(notificationServiceConnection);
 		this.unbindService(moduleServiceConnection);
 		shutdownHttpClient();
 		EventBus.getEventBus(TmpConstants.EVENTBUS_COMMON).unregister(
 				originalParser);
 		EventBus.getEventBus(TmpConstants.EVENTBUS_COMMON).unregister(this);
 		EventBus.getEventBus(TmpConstants.EVENTBUS_COMMON).unregister(
 				mdmSubsrcriber);
 
 	}
 
 	/** 用户注销 */
 	public void logOff() {
 		try {
 			log.debug("exit app and disconnect xmpp..");
 			hasLogined = false;
 			notificationService.disconnect(getChatManager());
 		} catch (Exception e) {
 			log.error("disconnect xmpp error!", e);
 		}
 		MucManager.getInstanse(this).offLine();
 		showTopWindow(a);
 		ThreadPlatformUtils.shutdownAllTask();
 		Intent in = new Intent(BroadcastConstans.CANCEELDIOLOG);
 		sendBroadcast(in);
 		IMModelManager.instance().clear();
 		
 		boolean outline = Preferences.getOutLine(Application.sharePref);
 		if (!outline){
 			CubeApplication application = getCubeApplication();
 			application.save(application);
 		}
 		LoginModel.instance().clear();
 		Intent i = null;
 		if (PadUtils.isPad(this)) {
 			i = new Intent();
 			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			i.setClass(this, FacadeActivity.class);
 			i.putExtra("direction", 1);
 			i.putExtra("type", "web");
 			i.putExtra("isPad", true);
 			i.putExtra("value", "file:///android_asset/www/pad/login.html");
 		} else {
 
 			// i = new Intent();
 			// i.setClass(this, LoginActivity.class);
 			// i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			// i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			i = new Intent();
 			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			i.setClass(this, FacadeActivity.class);
 			i.putExtra("value", URL.PHONE_LOGIN_URL);
 			i.putExtra("isPad", false);
 		}
 		activityManager.popAllActivity();
 		// activityManager.popAllActivityExceptOne(FacadeActivity.class);
 		startActivity(i);
 	}
 
 	private boolean hasLogined = false;
 
 	public boolean isHasLogined() {
 		return hasLogined;
 	}
 
 	public void setHasLogined(boolean hasLogined) {
 		this.hasLogined = hasLogined;
 	}
 
 	/**
 	 * @return 退出应用操作
 	 **/
 	public void exitApp() {
 		try {
 			log.debug("exit app and disconnect xmpp..");
 			hasLogined = false;
 			notificationService.disconnect(getChatManager());
 		} catch (Exception e) {
 			log.error("disconnect xmpp error!", e);
 		}
 		new AsyncTask<String, Integer, String>() {
 
 			@Override
 			protected String doInBackground(String... params) {
 				MucManager.getInstanse(Application.this).offLine();
 				return null;
 			}
 
 		}.execute();
 
 		ThreadPlatformUtils.shutdownAllTask();
 		IMModelManager.instance().clear();
 		boolean outline = Preferences.getOutLine(Application.sharePref);
 		if (!outline){
 			CubeApplication application = getCubeApplication();
 			application.save(application);
 		}
 		LoginModel.instance().clear();
 		activityManager.popAllActivity();
 	}
 
 	/**
 	 * @param type
 	 *            :文档类型
 	 * @param path
 	 *            :文档路径 打开公告附件
 	 * */
 	public void openAttachment(String type, String path) {
 		if (moduleOperationService == null) {
 			Toast.makeText(this, "服务未启动成功!", Toast.LENGTH_SHORT).show();
 			throw new IllegalStateException();
 		}
 		moduleOperationService.openAttachment(type, path);
 	}
 
 	/**
 	 * @return 获取模块唯一标识 如 "com.foreveross.cube.android"
 	 **/
 	public String getAppIdentifier() {
 		return getPackageName();
 	}
 
 	/**
 	 * @return 获取当前应用build号
 	 **/
 	public int getAppBuild() {
 		PackageManager pm = getPackageManager();
 		PackageInfo pi;
 		try {
 			pi = pm.getPackageInfo(getPackageName(), 0);
 			return pi.versionCode;
 		} catch (NameNotFoundException e) {
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	/**
 	 * @return 获取当前应用版本
 	 **/
 	public String getAppVersion() {
 		PackageManager pm = getPackageManager();
 		PackageInfo pi;
 		try {
 			pi = pm.getPackageInfo(getPackageName(), 0);
 			return pi.versionName;
 		} catch (NameNotFoundException e) {
 			e.printStackTrace();
 		}
 		return "";
 	}
 
 	public void loginXmppClient(final String username, final String password,final XmppManager manager) {
 		if (notificationService == null) {
 			this.bindService(NotificationService.getIntent(this),
 					notificationServiceConnection, Context.BIND_AUTO_CREATE);
 			notificationCallback = new NotificationCallback() {
 
 				@Override
 				public void doStuff() {
 					notificationService.connect(username, password,manager);
 				}
 			};
 		} else {
 			notificationService.connect(username, password,manager);
 		}
 
 	}
 
 	public void disconnect(XmppManager manager) {
 		if (notificationService == null) {
 			Toast.makeText(this, "服务未启动成功!", Toast.LENGTH_SHORT).show();
 			return;
 		}
 		notificationService.disconnect(manager);
 	}
 
 	public int getLoginType() {
 		return loginType;
 	}
 
 	public void setLoginType(int loginType) {
 		this.loginType = loginType;
 	}
 
 	@Override
 	public void onLowMemory() {
 		super.onLowMemory();
 		shutdownHttpClient();
 
 	}
 
 	/******************************************************************************
 	 * 
 	 * ModuleService
 	 * 
 	 *****************************************************************************/
 	private ServiceConnection minaServiceConnection = new ServiceConnection() {
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			minaPushService = null;
 		}
 
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			minaPushService = ((MinaPushServiceBinder) service).getService();
 
 		}
 	};
 
 	private MinaPushService minaPushService = null;
 
 	public MinaPushService getMinaPushService() {
 		return minaPushService;
 	}
 
 	/******************************************************************************
 	 * 
 	 * ModuleService
 	 * 
 	 *****************************************************************************/
 	private ServiceConnection moduleServiceConnection = new ServiceConnection() {
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			moduleOperationService = null;
 		}
 
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			moduleOperationService = ((ModuleOperationServiceBinder) service)
 					.getService();
 
 		}
 	};
 
 	private ModuleOperationService moduleOperationService = null;
 	/******************************************************************************
 	 * 
 	 * Xmpp 推送API
 	 * 
 	 *****************************************************************************/
 	private NotificationService notificationService = null;
 	private NotificationCallback notificationCallback;
 
 	private interface NotificationCallback {
 		public void doStuff();
 	}
 
 	private ServiceConnection notificationServiceConnection = new ServiceConnection() {
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			notificationService = null;
 		}
 
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			notificationService = ((NotificationServiceBinder) service)
 					.getService();
 			if (notificationCallback != null) {
 				notificationCallback.doStuff();
 			}
 //			String deviceId = DeviceInfoUtil.getDeviceId(Application.this);
 			String token = PushUtil.createMD5Token(Application.this);
 			loginXmppClient(token,token,notificationService.getPushManager());
 		}
 	};
 
 	/******************************************************************************
 	 * b Mina 推送API
 	 * 
 	 *****************************************************************************/
 	/**
 	 * [一句话功能简述]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @param sessionId
 	 * @param minaMobileClient
 	 *            2013-7-16 上午10:55:55
 	 */
 	private Long sessionId;
 	private MinaMobileClient minaMobileClient;
 
 	@Override
 	public void sessionIdCreated(final Long sessionId,
 			MinaMobileClient minaMobileClient) {
 		this.sessionId = sessionId;
 		this.minaMobileClient = minaMobileClient;
 		Preferences.saveSessionID(sessionId, Application.sharePref);
 
 //		ThreadPool.run(new Runnable() {
 //			@Override
 //			public void run() {
 //				HttpClient httpClient = new DefaultHttpClient();
 //				HttpPut httpPut = new HttpPut(URL.CHECKIN_URL);
 //
 //				try {
 //					httpPut.addHeader("Accept", "application/json");
 //					httpPut.addHeader("Content-Type", "application/json");
 //					DeviceCheckinVo checkinVo = new DeviceCheckinVo();
 //					checkinVo.setDeviceId(DeviceInfoUtil
 //							.getDeviceId(Application.this));
 //					// checkinVo.setAppId("51f787228314bd3f4de8f98e");
 //					checkinVo.setAppId(getCubeApplication().getAppKey());
 //					// checkinVo.setAlias(alias);
 //					checkinVo.setChannelId("mina");
 //					checkinVo.setDeviceName(android.os.Build.MODEL);
 //					// checkinVo.setGis("128,68");
 //					checkinVo.setOsName("android");
 //					checkinVo.setOsVersion(android.os.Build.VERSION.RELEASE);
 //					checkinVo.setPushToken(sessionId + "");
 //					checkinVo.setTags(new TagEntryVo[] { new TagEntryVo(
 //							"platform", "Android") });
 //
 //					httpPut.setEntity(new StringEntity(new Gson()
 //							.toJson(checkinVo), "utf-8"));
 //
 //					HttpResponse httpResponse = httpClient.execute(httpPut);
 //					int statusCode = httpResponse.getStatusLine()
 //							.getStatusCode();
 //					Log.d("MinaMobileClient", "签到 code == " + statusCode);
 //					if (statusCode == HttpStatus.SC_OK) {
 //						Log.d("MinaMobileClient", "Application 签到成功");
 //					}
 //				} catch (ClientProtocolException e) {
 //					Log.e("MessageContentHandler", "MessageContentHandler", e);
 //				} catch (IOException e) {
 //					Log.e("MessageContentHandler", "MessageContentHandler", e);
 //				}
 //			}
 //		});
 
 //		Log.i("ApplicationEx", "session authenticated " + sessionId);
 
 	}
 
 	/**
 	 * [一句话功能简述]<BR>
 	 * [功能详细描述]
 	 * 
 	 * @param sessionId
 	 *            2013-7-16 上午10:55:55
 	 */
 	@Override
 	public void sessionIdDestroyed(Long sessionId) {
 		this.sessionId = null;
 		this.minaMobileClient = null;
 	}
 
 	/******************************************************************************
 	 * 
 	 * 初始化Handler
 	 * 
 	 *****************************************************************************/
 	private Handler uiHandler;
 
 	private void initUIHandler() {
 		if (uiHandler == null) {
 			uiHandler = new Handler() {
 
 				@Override
 				public void handleMessage(Message msg) {
 					super.handleMessage(msg);
 //					PushUtil.regisrerPush(Application.this, msg.getData()
 //							.getString("tokenId"));
 					PushUtil.registerPush(Application.this);
 				}
 			};
 		}
 	}
 
 	public Handler getUIHandler() {
 		return uiHandler;
 	}
 	
 	
 	// public boolean isForegroundApp(){
 	//
 	// }
 	/**************************************************************************
 	 * 
 	 * xmpp 启动时间间隔
 	 * 
 	 ************************************************************************/
 
 	/**
 	 * [心跳超时剩余时间]
 	 */
 	private boolean excceedHartBeat = false;
 	/**
 	 * [心跳超时时间间隔]
 	 */
 	private int heartBeatInterval = 120;
 	/**
 	 * [是否正在计算]
 	 */
 	private boolean caculating = false;
 	/**
 	 * 执行定时器
 	 */
 	private Timer timer = new Timer();
 
 	public boolean getRemainTimes() {
 		return excceedHartBeat;
 	}
 
 	public void caculateHeartBeartRemain() {
 		// 如果正在计算则返回
 		if (caculating) {
 			return;
 		}
 		// 标识为正在计算
 		caculating = true;
 		// 时间为heartBeatInterval
 		excceedHartBeat = false;
 		// 每秒钟计算一次
 		timer.scheduleAtFixedRate(new TimerTask() {
 
 			@Override
 			public void run() {
 				// 如果剩余时间没有了，则停止计算，并返回
 				caculating = false;
 				this.cancel();
 				if (!NetworkUtil.isNetworkConnected(Application.this)) {
 					log.debug(
 							"network disconnected excceed in {} seconds,disconnect connection really!",
 							heartBeatInterval);
 					notificationService.disconnect(getChatManager());
 				} else {
 					log.debug(
 							"network connection restore in {} senconds,ignore disconnect connnection op really!",
 							heartBeatInterval);
 
 				}
 
 			}
 		}, 0, heartBeatInterval * 1000);
 	}
 
 	/****************************************************************************
 	 * 
 	 *
 	 ****************************************************************************/
 	private boolean isMessageViewPresence;
 	private boolean isAnnouceViewPresence;
 	private boolean isChatViewPresence;
 	private boolean isMessageCountViewPresence;
 	private boolean isAnnouceCountViewPresence;
 	private boolean isChatCountViewPresence;
 
 	@Subscribe
 	public void onMessageViewPresence(PresenceEvent presenceEvent) {
 		if (TmpConstants.VIEW_MESSAGE_PRESENCE.equals(presenceEvent
 				.getIdentifier())) {
 			isMessageViewPresence = presenceEvent.isPresence();
 		} else if (TmpConstants.VIEW_ANNOUNCE_PRESENCE.equals(presenceEvent
 				.getIdentifier())) {
 			isAnnouceViewPresence = presenceEvent.isPresence();
 		} else if (TmpConstants.VIEW_ANNOUNCE_COUNT_PRESENCE
 				.equals(presenceEvent.getIdentifier())) {
 			isAnnouceCountViewPresence = presenceEvent.isPresence();
 			if (isAnnouceCountViewPresence) {
 				CubeModule cubeModule = CubeModuleManager.getInstance()
 						.getModuleByIdentify(
 								TmpConstants.ANNOUCE_RECORD_IDENTIFIER);
 				if (cubeModule != null) {
 					cubeModule.notifyCountChange();
 				}
 			}
 		} else if (TmpConstants.VIEW_MESSAGE_COUNT_PRESENCE
 				.equals(presenceEvent.getIdentifier())) {
 			isMessageCountViewPresence = presenceEvent.isPresence();
 			if (isMessageCountViewPresence) {
 				CubeModule cubeModule = CubeModuleManager.getInstance()
 						.getModuleByIdentify(
 								TmpConstants.MESSAGE_RECORD_IDENTIFIER);
 				if (cubeModule != null) {
 					cubeModule.notifyCountChange();
 				}
 			}
 		} else if (TmpConstants.VIEW_CHAT_COUNT_PRESENCE.equals(presenceEvent
 				.getIdentifier())) {
 			isChatCountViewPresence = presenceEvent.isPresence();
 			if (isChatCountViewPresence) {
 				CubeModule cubeModule = CubeModuleManager.getInstance()
 						.getModuleByIdentify(
 								TmpConstants.CHAT_RECORD_IDENTIFIER);
 				if (cubeModule != null) {
 					cubeModule.notifyCountChange();
 				}
 			}
 		}
 
 	}
 
 	public boolean isMessageViewPresence() {
 		return isMessageViewPresence;
 	}
 
 	public boolean isNoticeViewPresence() {
 		return isAnnouceViewPresence;
 	}
 
 	public boolean isMessageCountViewPresence() {
 		return isMessageCountViewPresence;
 	}
 
 	public boolean isChatViewPresence() {
 		return isChatViewPresence;
 	}
 
 	public void setMessageCountViewPresence(boolean isMessageCountViewPresence) {
 		this.isMessageCountViewPresence = isMessageCountViewPresence;
 	}
 
 	public boolean isAnnouceCountViewPresence() {
 		return isAnnouceCountViewPresence;
 	}
 
 	public void setAnnouceCountViewPresence(boolean isAnnouceCountViewPresence) {
 		this.isAnnouceCountViewPresence = isAnnouceCountViewPresence;
 	}
 
 	/****************************************************************************
 	 * 
 	 *
 	 ****************************************************************************/
 	private Boolean shouldSendMessageNotification = null;
 	private Boolean shouldSendNoticeNotification = null;
 	private Boolean shouldSendChatNotification = null;
 
 	private Boolean isInChatRoomFragment = false;
 
 	public Boolean getIsInChatRoomFragment() {
 		return isInChatRoomFragment;
 	}
 
 	public void setIsInChatRoomFragment(Boolean isInChatRoomFragment) {
 		this.isInChatRoomFragment = isInChatRoomFragment;
 	}
 
 	public void setShouldSendMessageNotification(
 			boolean shouldSendMessageNotification) {
 		this.shouldSendMessageNotification = shouldSendMessageNotification;
 		if (!shouldSendMessageNotification) {
 			Notifier.cancel(
 					this,
 					com.foreveross.chameleon.push.client.Constants.ID_MESSAGE_NOTIFICATION);
 		}
 	}
 
 	public void setShouldSendNoticeNotification(
 			boolean shouldSendNoticeNotification) {
 		this.shouldSendNoticeNotification = shouldSendNoticeNotification;
 		if (!shouldSendNoticeNotification) {
 			Notifier.cancel(
 					this,
 					com.foreveross.chameleon.push.client.Constants.ID_NOTICE_NOTIFICATION);
 		}
 	}
 
 	public void setShouldSendChatNotification(boolean shouldSendChatNotification) {
 		this.shouldSendChatNotification = shouldSendChatNotification;
 		if (!shouldSendChatNotification) {
 			Notifier.cancel(
 					this,
 					com.foreveross.chameleon.push.client.Constants.ID_CHAT_NOTIFICATION);
 		}
 	}
 
 	public boolean shouldSendMessageNotification() {
 		return shouldSendMessageNotification == null
 				|| shouldSendMessageNotification;
 	}
 
 	public Boolean getShouldSendMessageNotification() {
 		return shouldSendMessageNotification;
 	}
 
 	public Boolean getShouldSendNoticeNotification() {
 		return shouldSendNoticeNotification;
 	}
 
 	public Boolean getShouldSendChatNotification() {
 		return shouldSendChatNotification;
 	}
 
 	public boolean shouldSendNoticeNotification() {
 		return shouldSendNoticeNotification == null
 				|| shouldSendNoticeNotification;
 	}
 
 	public boolean shouldSendChatNotification() {
 		return shouldSendChatNotification == null || shouldSendChatNotification;
 	}
 
 	public XmppManager getChatManager() {
 		if (null != notificationService) {
 			return notificationService.getChatManager();
 			
 				
 		} else {
 			// String username = Preferences.getUserName(Application.sharePref);
 			// loginChatClient(username, username);
 			Log.e("notificationService====", "notificationServiceConnection="
 					+ notificationServiceConnection);
 			Log.e("notificationService====", "BeforeBindnotificationService="
 					+ notificationService);
 
 			this.bindService(NotificationService.getIntent(this),
 					notificationServiceConnection, Context.BIND_AUTO_CREATE);
 			loginXmppClient(Preferences.getUserName(Application.sharePref),
 					Preferences.getUserName(Application.sharePref),notificationService.getChatManager());
 			Log.e("notificationService====", "AffterBindnotificationService="
 					+ notificationService);
 			if (notificationService != null) {
 				return notificationService.getChatManager();
 			}
 			return null;
 		}
 	}
 
 	public XmppManager getPushManager() {
 		if (null != notificationService) {
 			return notificationService.getPushManager();
 			
 				
 		} else {
 			// String username = Preferences.getUserName(Application.sharePref);
 			// loginChatClient(username, username);
 			Log.e("notificationService====", "notificationServiceConnection="
 					+ notificationServiceConnection);
 			Log.e("notificationService====", "BeforeBindnotificationService="
 					+ notificationService);
 
 			this.bindService(NotificationService.getIntent(this),
 					notificationServiceConnection, Context.BIND_AUTO_CREATE);
 			loginXmppClient(Preferences.getUserName(Application.sharePref),
 					Preferences.getUserName(Application.sharePref),notificationService.getPushManager());
 			Log.e("notificationService====", "AffterBindnotificationService="
 					+ notificationService);
 			if (notificationService != null) {
 				return notificationService.getPushManager();
 			}
 			return null;
 		}
 	}
 	
 	/**
 	 * 更新用户标签
 	 */
 	public void refreshRegisrer() {
 
 		String privileges = Preferences.getPrivileges(Application.sharePref);
 		if (privileges == null || privileges.equals("")) {
 			return;
 		}
 		String mytag = "";
 		try {
 			JSONArray privs = new JSONArray(privileges);
 			for (int i = 0; i < privs.length(); i++) {
 				String name = privs.getJSONObject(i).getString("name");
 				if (name != null && !name.equals("")) {
 					mytag += name + ",";
 				}
 			}
 		} catch (JSONException e1) {
 			e1.printStackTrace();
 		}
 		List<NameValuePair> values = new ArrayList<NameValuePair>();
 
 		String names = getPackageName();
 		values.add(new BasicNameValuePair("deviceId", DeviceInfoUtil
 				.getDeviceId(this)));
 		values.add(new BasicNameValuePair("appId", cubeApplication.getAppKey()));
 		String username = Preferences.getUserName(Application.sharePref);
 		String sex = Preferences.getSex(Application.sharePref) != null ? Preferences
 				.getSex(Application.sharePref) : "";
 		String phone = Preferences.getPhone(Application.sharePref);
 		// 去掉了标签的userName，phone
         values.add(new BasicNameValuePair("alias",username));
 		values.add(new BasicNameValuePair("tags", "{privileges=" + mytag
 				+ "sex=" + sex + "}"));
 
 		HttpPut request = new HttpPut(URL.CHECKIN_URL + "/tags");
 		request.setHeader(
 				"Accept",
 				"application/x-www-form-urlencoded, application/xml, text/html, text/*, image/*, */*");
 		DefaultHttpClient client = new DefaultHttpClient();
 		if (values != null && values.size() > 0) {
 			try {
 				UrlEncodedFormEntity entity;
 				entity = new UrlEncodedFormEntity(values, "utf-8");
 				request.setEntity(entity);
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 		}
 		try {
 			HttpResponse response = client.execute(request);
 			int statusCode = response.getStatusLine().getStatusCode();
 			if (statusCode != 200) {
 
 				return;
 			}
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public static String moduleName = null;
 	public static void setModuleName(String moduleName) {
 		Application.moduleName = moduleName;
 	}
 }
