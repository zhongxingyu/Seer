 package com.htb.cnk;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.htb.cnk.data.Info;
 import com.htb.cnk.data.Lisence;
 import com.htb.cnk.data.MyOrder;
 import com.htb.cnk.data.Setting;
 import com.htb.cnk.data.Version;
 import com.htb.cnk.data.WifiAdmin;
 import com.htb.cnk.dialog.LoginDlg;
 import com.htb.cnk.lib.Http;
 import com.htb.cnk.service.NotificationTableService;
 import com.htb.cnk.ui.base.BaseActivity;
 import com.htb.constant.Permission;
 import com.htb.constant.Server;
 
 public class Cnk_orderPadActivity extends BaseActivity {
 	public final static String TAG = "Cnk_orderPad";
 	
 	private final static int UPDATE_MENU = 0;
 	private final static int LATEST_MENU = 1;
 	private final static int DOWNLOAD_NEW_APP = 2;
 	private final static int DO_UPGRADE = 3;
 
 	private ImageButton mMenuBtn;
 	private TextView mMenuTxt;
 	private ImageButton mSettingsBtn;
 	private TextView mSettingsTxt;
 	private TextView mVersionTxt;
 	private ProgressDialog mpDialog;
 	private String mUrl;
 	private WifiAdmin mWifiAdmin;
 	private String mUpdateAkpDir;
 	private Handler handler = new Handler();
 	private Version version;
 	private Setting mAppSetting;
 
 	private int retry;
 
 	protected NotificationTableService.MyBinder pendOrderBinder;
 	protected boolean binded;
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		version = new Version(Cnk_orderPadActivity.this);
 		mAppSetting = new Setting(Cnk_orderPadActivity.this);
 		mWifiAdmin = new WifiAdmin(Cnk_orderPadActivity.this);
 		
 		findViews();
 		initSyncProgressBar();
 		setClickListeners();
 		initInfo();
 		
 		Intent intent = new Intent(this, NotificationTableService.class);  
 	    startService(intent);
 	    bindService(intent, conn, Context.BIND_AUTO_CREATE);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		mpDialog.show();
 		initWifi();
 		retry = 0;
 		if (binded) {
 			validatePadWhenConnected();
 		}
 	}
 
 	private void findViews() {
 		mMenuBtn = (ImageButton) findViewById(R.id.menu);
 		mMenuTxt = (TextView) findViewById(R.id.menuTxt);
 		mSettingsBtn = (ImageButton) findViewById(R.id.settings);
 		mSettingsTxt = (TextView) findViewById(R.id.settingsTxt);
 		mVersionTxt = (TextView) findViewById(R.id.versionTxt);
 		mVersionTxt.setText("ver " + version.getVersion());
 	}
 
 	private void setClickListeners() {
 		mMenuBtn.setOnClickListener(menuClicked);
 		mMenuTxt.setOnClickListener(menuClicked);
 		mSettingsBtn.setOnClickListener(settingsClicked);
 		mSettingsTxt.setOnClickListener(settingsClicked);
 		mVersionTxt.setOnLongClickListener(versionClicked);
 	}
 
 	private void initInfo() {
 		Info.setNewCustomer(true);
 		Info.setMode(Info.WORK_MODE_CUSTOMER);
 		Info.setTableId(-1);
 		mUpdateAkpDir = Environment.getDataDirectory() + "/data/"
 				+ this.getPackageName() + "/files/";
 	}
 
 	private void validatePadWhenConnected() {
 		if (pendOrderBinder.getNetworkStatus()) {
 			validatePad();
 		} else {
 			new Thread() {
 				public void run() {
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 					validateHandler.sendEmptyMessage(0);
 				}
 			}.start();
 		}
 	}
 	
 	private void validatePad() {
 		new Thread() {
 			public void run() {
 				int ret = Lisence.validateDevice(getBaseContext());
 				Log.d(TAG, "lisence" + ret);
 				LisenceHandle.sendEmptyMessage(ret);
 			}
 		}.start();
 	}
 	
 	private int getCurrentMenuVer() {
 		SharedPreferences sharedPre = getSharedPreferences("menuDB",
 				Context.MODE_PRIVATE);
 		return sharedPre.getInt("ver", -1);
 	}
 
 	private void initSyncProgressBar() {
 		mpDialog = new ProgressDialog(Cnk_orderPadActivity.this);
 		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		mpDialog.setMessage("正在与服务器同步...");
 		mpDialog.setIndeterminate(false);
 		mpDialog.setCancelable(false);
 	}
 
 	private boolean getServerVerCode() {
 		String response = Http.get(Server.APK_VERSION, null);
 		if (response == null || "".equals(response)) {
 			Log.e(TAG, "server not response for version request");
 			return false;
 		} else {
 			try {
 				JSONObject versionInfo = new JSONObject(response);
 				String versionString = versionInfo.getString("ver");
 				Log.i(TAG, "ver:" + versionString);
 				String[] ver = versionString.split("\\.");
 				int major = Integer.parseInt(ver[0]);
 				int minor = Integer.parseInt(ver[1]);
 				int build = Integer.parseInt(ver[2]);
 				if (version.isUpdateNeed(major, minor, build)) {
 					String name = versionInfo.getString("name");
 					mUrl = Server.SERVER_DOMIN + "/" + Server.APK_DIR + name;
 					return true;
 				} else {
 					return false;
 				}
 			} catch (Exception e) {
 				Log.e(TAG, "APK ver response:" + response);
 				e.printStackTrace();
 			}
 		}
 		return false;
 	}
 
 	private void doNewVersionUpdate() {
 		handlerSync.sendEmptyMessage(DOWNLOAD_NEW_APP);
 		downFile(mUrl);
 	}
 
 	private void startLock() {
 		mWifiAdmin.creatWifiLock();
 		mWifiAdmin.acquireWifiLock();
 	}
 
 	private void LisenceErrDlg(String msg) {
 		new AlertDialog.Builder(Cnk_orderPadActivity.this).setTitle("注意")
 				.setCancelable(false).setMessage(msg)
 				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
 	
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						finish();
 					}
 				}).show();
 	}
 
 	private void downFile(final String url) {
 		new Thread() {
 			public void run() {
 				Log.i(TAG, "download new version apk:" + url);
 				HttpParams httpParameters1 = new BasicHttpParams();
 	
 				HttpConnectionParams.setConnectionTimeout(httpParameters1,
 						10 * 1000);
 				HttpConnectionParams.setSoTimeout(httpParameters1, 10 * 1000);
 				HttpClient client = new DefaultHttpClient(httpParameters1);
 	
 				try {
 					HttpGet get = new HttpGet(url);
 					HttpResponse response;
 					response = client.execute(get);
 					HttpEntity entity = response.getEntity();
 					long length = entity.getContentLength();
 					Log.i(TAG, "update apk, size: " + length);
 					InputStream is = entity.getContent();
 					FileOutputStream fileOutputStream = null;
 					if (is != null) {
 						fileOutputStream = openFileOutput(
 								version.UPDATE_SAVENAME, MODE_WORLD_READABLE);
 	
 						byte[] buf = new byte[1024];
 						int ch = -1;
 						while ((ch = is.read(buf)) != -1) {
 							fileOutputStream.write(buf, 0, ch);
 							if (length > 0) {
 							}
 						}
 					}
 					fileOutputStream.flush();
 					if (fileOutputStream != null) {
 						fileOutputStream.close();
 					}
 					Log.i(TAG, "download apk done");
 					down();
 				} catch (ClientProtocolException e) {
 					e.printStackTrace();
 					errHandler.sendEmptyMessage(-1);
 				} catch (IOException e) {
 					e.printStackTrace();
 					errHandler.sendEmptyMessage(-1);
 				} catch (Exception e) {
 					e.printStackTrace();
 					errHandler.sendEmptyMessage(-1);
 				}
 			}
 	
 		}.start();
 	}
 
 	private void down() {
 		handler.post(new Runnable() {
 			public void run() {
 				update();
 			}
 		});
 	
 	}
 
 	private void popErrorDlg(int err) {
 		new AlertDialog.Builder(Cnk_orderPadActivity.this).setTitle("错误")
 				.setMessage("更新软件失败！").setPositiveButton("确定", null).show();
 	
 	}
 
 	private void update() {
 		handlerSync.sendEmptyMessage(DO_UPGRADE);
 	}
 
 	private void getFLavor() {
 		new Thread() {
 			public void run() {
 				int ret = MyOrder.getFLavorFromServer();
 				flavorHandler.sendEmptyMessage(ret);
 			}
 		}.start();
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			new AlertDialog.Builder(Cnk_orderPadActivity.this)
 					.setTitle("注意")
 					.setCancelable(false)
 					.setMessage("确认退出菜脑壳点菜系统？")
 					.setPositiveButton("确定",
 							new DialogInterface.OnClickListener() {
 	
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									finish();
 								}
 							}).setNegativeButton("取消", null).show();
 			return true;
 		} else {
 			return super.onKeyDown(keyCode, event);
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		if (binded) {  
 	        unbindService(conn);              
 	    } 
 		stopService(new Intent(Cnk_orderPadActivity.this, NotificationTableService.class));
 		super.onDestroy();
 	}
 
 	class wifiConnect implements Runnable {
 		public void run() {
 			try {
 				mWifiAdmin.openNetCard();
 				if (mWifiAdmin.checkNetCardState() == 0
 						|| mWifiAdmin.checkNetCardState() == 1) {
 					Thread.sleep(1000);
 					wifiConnectHandle.sendEmptyMessage(-1);
 				} else {
 					wifiConnectHandle.sendEmptyMessage(1);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private ServiceConnection conn = new ServiceConnection() {
 		
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 		}
 		
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			if (service == null)
 				Log.d("TAG", "service==null");
 			pendOrderBinder = (NotificationTableService.MyBinder)service;
 			binded = true;
 			pendOrderBinder.start();
 			validatePadWhenConnected();
 		}
 	};
 
 	private OnClickListener menuClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View arg0) {
 			Intent intent = new Intent();
 			intent.setClass(Cnk_orderPadActivity.this, MenuActivity.class);
 			Info.setMode(Info.WORK_MODE_CUSTOMER);
 			startActivity(intent);
 		}
 
 	};
 
 	private OnClickListener settingsClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			if (Info.getMode() == Info.WORK_MODE_CUSTOMER) {
 				LoginDlg loginDlg = new LoginDlg(Cnk_orderPadActivity.this,
 						TableActivity.class);
 				loginDlg.show(Permission.STUFF);
 			} else {
 				Intent intent = new Intent();
 				intent.setClass(Cnk_orderPadActivity.this, TableActivity.class);
 				Cnk_orderPadActivity.this.startActivity(intent);
 			}
 		}
 
 	};
 
 	private OnLongClickListener versionClicked = new OnLongClickListener() {
 
 		@Override
 		public boolean onLongClick(View v) {
 			Setting.enableDebug(true);
 			Intent intent = new Intent();
 			intent.setClass(Cnk_orderPadActivity.this, SettingActivity.class);
 			Cnk_orderPadActivity.this.startActivity(intent);
 			return false;
 		}
 	};
 	public void initWifi() {
 		if (mWifiAdmin.checkNetCardState() == 0
 				|| mWifiAdmin.checkNetCardState() == 1) {
 			new Thread(new wifiConnect()).start();
 		}
 		startLock();
 	}
 
 	private Handler flavorHandler = new Handler() {
 	/** Called when the activity is first created. */
 		public void handleMessage(Message msg) {
 			if (msg.what < 0) {
 				MyOrder.getFlaovorFromSetting();
 			} else {
 				MyOrder.saveFlavorToSetting();
 			}
 		}
 	};
 
 	private Handler validateHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (retry > 3) {
 				mpDialog.cancel();
 				new AlertDialog.Builder(Cnk_orderPadActivity.this)
 				.setTitle("错误")
 				.setMessage("网络连接不可用")
 				.setPositiveButton("重试",
 						new DialogInterface.OnClickListener() {
 	
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								mpDialog.show();
 								validatePadWhenConnected();
 							}
 						})
 				.setNegativeButton("退出", new DialogInterface.OnClickListener() {
 					
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						finish();
 					}
 				}).show();
 			} else {
 				retry++;
 				validatePadWhenConnected();
 			}
 		}
 	};
 
 	private Handler handlerSync = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what == UPDATE_MENU) {
 				Intent intent = new Intent();
 				intent.setClass(Cnk_orderPadActivity.this,
 						UpdateMenuActivity.class);
 				startActivity(intent);
 			} else if (msg.what == DOWNLOAD_NEW_APP) {
 				mpDialog.setMessage("请稍候, 正在更新软件...");
 				return;
 			} else if (msg.what == DO_UPGRADE) {
 				new AlertDialog.Builder(Cnk_orderPadActivity.this)
 						.setTitle("更新")
 						.setMessage("新版本软件已下载，点击确定升级")
 						.setPositiveButton("确定",
 								new DialogInterface.OnClickListener() {
 	
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 										Intent intent = new Intent(
 												Intent.ACTION_VIEW);
 										intent.setDataAndType(
 												Uri.fromFile(new File(mUpdateAkpDir,
 														version.UPDATE_SAVENAME)),
 												"application/vnd.android.package-archive");
 										startActivity(intent);
 									}
 								}).show();
 	
 			}
 			mpDialog.cancel();
 		}
 	};
 
 	private Handler errHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what < 0) {
 				mpDialog.cancel();
 				Log.e("fetch Data failed", "errno: " + msg.what);
 				popErrorDlg(msg.what);
 			}
 		}
 	
 	};
 
 	private Handler wifiConnectHandle = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what < 0) {
 				new Thread(new wifiConnect()).start();
 			} else {
 				Toast.makeText(Cnk_orderPadActivity.this, "当前wifi状态已经连接",
 						Toast.LENGTH_SHORT).show();
 			}
 		}
 	};
 
 	private Handler LisenceHandle = new Handler() {
 		public void handleMessage(Message msg) {
 
 			if (msg.what < 0) {
 				mpDialog.cancel();
 				LisenceErrDlg("无法验证Pad合法性");
 			} else if (msg.what == 0) {
 				new Thread() {
 					public void run() {
 						if (getServerVerCode()) {
 							doNewVersionUpdate();
 						}
 						handlerSync.sendEmptyMessage(LATEST_MENU);
 						int menuVer = getCurrentMenuVer();
 						if (UpdateMenuActivity.isUpdateNeed(menuVer)) {
 							Log.d(TAG, "update Menu needed");
 							handlerSync.sendEmptyMessage(UPDATE_MENU);
 						} else {
 							Log.d(TAG, "no new menu founded, currentMenuVer:" + menuVer);
 							handlerSync.sendEmptyMessage(LATEST_MENU);
 						}
 					}
 				}.start();
 
 				getFLavor();
 			} else {
 				mpDialog.cancel();
 				LisenceErrDlg("当前许可协议只允许使用" + (msg.what) + "台Pad");
 			}
 		}
 	};
 
 }
