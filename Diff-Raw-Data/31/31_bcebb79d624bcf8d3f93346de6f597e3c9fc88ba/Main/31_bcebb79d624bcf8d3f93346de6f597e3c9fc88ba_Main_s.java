 package com.macaroon.piztor;
 
 import java.util.Calendar;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.Vector;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.baidu.location.BDLocation;
 import com.baidu.location.BDLocationListener;
 import com.baidu.location.LocationClient;
 import com.baidu.location.LocationClientOption;
 import com.baidu.mapapi.map.LocationData;
 import com.baidu.mapapi.map.MKMapTouchListener;
 import com.baidu.mapapi.map.MapView;
 import com.baidu.mapapi.map.OverlayItem;
 import com.baidu.mapapi.map.PopupOverlay;
 import com.baidu.mapapi.utils.DistanceUtil;
 import com.baidu.platform.comapi.basestruct.GeoPoint;
 
 public class Main extends PiztorAct {
 	final static int SearchButtonPress = 1;
 	final static int FocuseButtonPress = 3;
 	final static int SuccessFetch = 4;
 	final static int FailedFetch = 5;
 	final static int mapViewtouched = 7;
 
 
 	MapMaker mapMaker = null;
 	MapView mMapView;
 	AlertMaker alertMaker;
 	private Calendar calendar;
 	GeoPoint markerPoint = null;
 	private MKMapTouchListener mapTouchListener;
 	
 	/**
 	 * Locating component
 	 */
 	LocationManager locationManager;
 	boolean isGPSEnabled;
 	LocationClient mLocClient;
 	LocationData locData = null;
 	public MyLocationListener myListener = new MyLocationListener();
 	boolean isFirstLocation = true;
 
 	ImageButton btnSearch, btnFetch, btnFocus, btnSettings;
 	MapInfo mapInfo;
 	
 	@SuppressLint("HandlerLeak")
 	Handler handler = new Handler() {
 		@Override
 		public void handleMessage(Message m) {
 			switch (m.what) {
 			case 1:// 上传自己信息成功or失败
 				ResUpdate update = (ResUpdate) m.obj;
 				if (update.status == 0)
 					System.out.println("update success");
 				else {
 					System.out.println("update failed");
 					actMgr.trigger(AppMgr.errorToken);
 				}
 				break;
 			case 2:// 得到别人的信息
 				ResLocation location = (ResLocation) m.obj;
 				if (location.status == 0) {
 					mapInfo.clear();
 					for (RLocation i : location.l) {
 						System.out.println(i.id + " : " + i.latitude + " "
 								+ i.longitude);
 						UserInfo info = new UserInfo(i.id);
 						info.setLocation(i.latitude, i.longitude);
 						mapInfo.addUserInfo(info);
 					}
 //					actMgr.trigger(SuccessFetch);
 					flushMap();
 				} else {
 					System.out.println("resquest for location failed!");
 					actMgr.trigger(AppMgr.errorToken);
 				}
 				break;
 			case 3:// 得到用户信息
 				ResUserInfo r = (ResUserInfo) m.obj;
 				if (r.status == 0) {
 					System.out.println("id : " + r.uid + " sex :  " + r.sex
 							+ " group : " + r.section);
 					if (r.uid == Infomation.myInfo.uid) {
 						System.out.println("flush myself info!!!");
 						Infomation.myInfo.section = r.section;
 						Infomation.myInfo.company = r.company;
 						Infomation.myInfo.sex = r.sex;
 					} else {
 						UserInfo user = mapInfo.getUserInfo(r.uid);
 						if (user != null)
 							user.setInfo(r.company, r.section, r.sex);
 						else
 							System.out.println("fuck!!!!");
 					}
 					flushMap();
 				} else {
 					System.out.println("reqest for userInfo must be wrong!!!");
 					actMgr.trigger(AppMgr.errorToken);
 				}
 				break;
 			case 4:// 登出
 				ResLogout logout = (ResLogout) m.obj;
 				if (logout.status == 0) {
 					actMgr.trigger(AppMgr.logout);
 				} else {
 					Toast toast = Toast.makeText(getApplicationContext(),
 							"logout failed", Toast.LENGTH_LONG);
 					toast.show();
 				}
 				break;
 			case Transam.StartPush:
 				ResStartPush startpush = (ResStartPush) m.obj;
 				if (startpush.status == 1) {
 					System.out.println("!!!!!!!!!!!!!jian gui le!!!!!!!!!!!!!");
 				}
 				break;
 			case Transam.PushMessage:
 				ResPushMessage pushMessage = (ResPushMessage) m.obj;
 				receiveMessage(pushMessage.message);
 				break;
 			case Transam.SendMessage:
 				ResSendMessage resMessage = (ResSendMessage) m.obj;
 				System.out.println("res message         " + resMessage.status);
 				break;
 			case Transam.PushLocation:
 				ResPushLocation pushLocation = (ResPushLocation) m.obj;
 				upDateInfo(pushLocation.l);
 				break;
 			default:
 				break;
 			}
 		}
 
 		void upDateInfo(Vector<RLocation> l) {
 			// TODO
 			for (RLocation i : l) {
 				UserInfo info = AppMgr.mapInfo.getUserInfo(i.id);
 				if (info != null) {
 					info.setLocation(i.latitude, i.longitude);
 				} else {
 					info = new UserInfo(i.id);
 					info.setLocation(i.latitude, i.longitude);
 					AppMgr.mapInfo.addUserInfo(info);
 				}
 			}
 			flushMap();
 		}
 	};
 
 	String cause(int t) {
 		switch (t) {
 		case SearchButtonPress:
 			return "Search Button Press";
 		case FocuseButtonPress:
 			return "Focuse Button Press";
 		case SuccessFetch:
 			return "Success Fetch";
 		case FailedFetch:
 			return "Failed Fetch";
 		default:
 			return "Fuck!!!";
 		}
 	}
 
 	// TODO flush map view
 	void flushMap() {
 		if (mapMaker != null)
 			mapMaker.UpdateMap(AppMgr.mapInfo);
 		else
 			System.out
 					.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
 	}
 	
 	void receiveMessage(String msg) {
 		System.out.println("receiveed push message!!!!!");
 		System.out.println(msg);
 		Toast toast = Toast.makeText(getApplicationContext(),
 				msg, Toast.LENGTH_LONG);
 		toast.show();
 	}
 	
 
 	public class MyLocationListener implements BDLocationListener {
 		int cnt = 0;
 		GeoPoint lastPoint = null;
 		@Override
 		public void onReceiveLocation(BDLocation location) {
 			Log.d("GPS", "Gotten");
 			cnt++;
 			if (location == null) {
 				return;
 			}
 			locData.latitude = location.getLatitude();
 			locData.longitude = location.getLongitude();
 			locData.accuracy = location.getRadius();
 			locData.direction = location.getDerect();
 			
 			GeoPoint point = new GeoPoint((int)(locData.latitude * 1e6), (int)(locData.longitude * 1e6));
 			if (lastPoint == null || cnt > 5 || DistanceUtil.getDistance(point, lastPoint) > 10 ) {
 				if (Infomation.token != null) {
 					Infomation.myInfo.setLocation(locData.latitude,
 							locData.longitude);
 					AppMgr.transam.send(new ReqUpdate(Infomation.token,
 							Infomation.username, locData.latitude,
 							locData.longitude, System.currentTimeMillis(), 2000));
 					lastPoint = point;
 					cnt = 0;
 				}
 			}
 			mapMaker.UpdateLocationOverlay(locData, false);
 		}
 
 		@Override
 		public void onReceivePoi(BDLocation poiLocation) {
 			if (poiLocation == null) {
 				return;
 			}
 		}
 	}
 
 	class StartStatus extends ActStatus {
 
 		@Override
 		void enter(int e) {
 			System.out.println("enter start status!!!!");
 			if (e == ActMgr.Create) {
 				System.out.println(Infomation.token + "  "
 						+ Infomation.username + "   " + Infomation.myInfo.uid);
 				AppMgr.transam.send(new ReqUserInfo(Infomation.token,
 						Infomation.username, Infomation.myInfo.uid, System
 								.currentTimeMillis(), 5000));
 			}
 			if (e == SuccessFetch)
 				flushMap();
 		}
 
 		@Override
 		void leave(int e) {
 			System.out.println("leave start status!!!! because" + cause(e));
 		}
 
 	}
 
 
 	void requesLocation(int company, int section) {
 		ReqLocation r = new ReqLocation(Infomation.token, Infomation.username,
 				company, section, System.currentTimeMillis(), 2000);
 		AppMgr.transam
 				.send(new ReqSendMessage(Infomation.token, Infomation.username,
 						"hahaha", System.currentTimeMillis(), 5000));
 		System.out.println("get others infomation!!!");
 		AppMgr.transam.send(r);
 	}
 
 	void focusOn() {
 		mapMaker.mMapController.animateTo(Infomation.myInfo.location);
 	}
 	
 	public void InitTouchListenr() {
 
 		mapTouchListener = new MKMapTouchListener() {
 			
 			@Override
 			public void onMapLongClick(GeoPoint arg0) {
 				closeBoard(Main.this);
 				alertMaker.showMarkerAlert(arg0);
 			}
 			
 			@Override
 			public void onMapDoubleClick(GeoPoint arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void onMapClick(GeoPoint arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 		};
 		mMapView.regMapTouchListner(mapTouchListener);
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		id = "Main";
 		super.onCreate(savedInstanceState);
 		
 		locationManager = (LocationManager)this.getSystemService(LOCATION_SERVICE);
 		isGPSEnabled = locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);
 		
 		mapInfo = AppMgr.mapInfo;
 		ActStatus[] r = new ActStatus[1];
 		ActStatus startStatus = r[0] = new StartStatus();
 //		ActStatus fetchStatus = r[1] = new FetchStatus();
 //		ActStatus focusStatus = r[2] = new FocusStatus();
 		AppMgr.transam.setHandler(handler);
 		actMgr = new ActMgr(this, startStatus, r);
 //		actMgr.add(startStatus, FocuseButtonPress, focusStatus);
 //		actMgr.add(startStatus, Fetch, fetchStatus);
 //		actMgr.add(startStatus, SuccessFetch, startStatus);
 //		actMgr.add(startStatus, Fetch, startStatus);
 //		actMgr.add(fetchStatus, Fetch, startStatus);
 //		actMgr.add(fetchStatus, FailedFetch, startStatus);
 //		actMgr.add(fetchStatus, SuccessFetch, startStatus);
 //		actMgr.add(focusStatus, FocuseButtonPress, startStatus);
 //		actMgr.add(focusStatus, mapViewtouched, startStatus);
 //		actMgr.add(focusStatus, SuccessFetch, focusStatus);
 //		actMgr.add(focusStatus, Fetch, focusStatus);
 		setContentView(R.layout.activity_main);
 		
 		mMapView = (MapView) findViewById(R.id.bmapView);
 		mapMaker = new MapMaker(mMapView, getApplicationContext());
 		alertMaker = new AlertMaker(Main.this, mapMaker);
 		if (isGPSEnabled == false) alertMaker.showSettingsAlert();
 		mapMaker.clearOverlay(mMapView);
 		mapMaker.InitMap();
 		InitTouchListenr();
 		mLocClient = new LocationClient(this);
 		locData = new LocationData();
 		mLocClient.registerLocationListener(myListener);
 		LocationClientOption option = new LocationClientOption();
 		option.setOpenGps(true);
 		option.setCoorType("bd09ll");
 		option.setScanSpan(5000);
 		mLocClient.setLocOption(option);
 		mLocClient.start();
 		mapMaker.UpdateLocationOverlay(locData, false);
 	}
 
 	public static void closeBoard(Context cc) {
 		InputMethodManager imm = (InputMethodManager) cc
 				.getSystemService(Context.INPUT_METHOD_SERVICE);
 		  if (imm.isActive())
 			  imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
 		 InputMethodManager.HIDE_NOT_ALWAYS);
 		 }
 	
 	/*
 	 * public boolean onTap(int index) { OverlayItem item = getItem(index);
 	 * mCurItem = item; if () }
 	 */
 	@Override
 	protected void onStart() {
 		super.onStart();
 		btnFetch = (ImageButton) findViewById(R.id.footbar_btn_fetch);
 		btnFocus = (ImageButton) findViewById(R.id.footbar_btn_focus);
 		btnSearch = (ImageButton) findViewById(R.id.footbar_btn_search);
 		btnSettings = (ImageButton) findViewById(R.id.footbar_btn_settings);
 		btnFetch.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				requesLocation(Infomation.myInfo.company, Infomation.myInfo.section);
 			}
 		});
 		
 		btnFocus.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				focusOn();
 			}
 		});
 		
 		btnSettings.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				actMgr.trigger(AppMgr.toSettings);
 			}
 		});
 
 	}
 
 	@Override
 	protected void onRestart() {
 		super.onRestart();
 		AppMgr.transam.setHandler(handler);
 	}
 	
 	@Override
 	protected void onResume() {
 		isFirstLocation = true;
 		mapMaker.onResume();
 		flushMap();
 		requesLocation(Infomation.myInfo.company, Infomation.myInfo.section);
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		mapMaker.onPause();
 		super.onPause();
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 	}
 
 	@Override
 	protected void onDestroy() {
 		if (mLocClient != null) {
 			mLocClient.stop();
 		}
 		mapMaker.onDestroy();
 		super.onDestroy();
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AppMgr.exit();
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return false;
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		mMapView.onSaveInstanceState(outState);
 	}
 
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		super.onRestoreInstanceState(savedInstanceState);
 		mMapView.onRestoreInstanceState(savedInstanceState);
 	}
 
 }
