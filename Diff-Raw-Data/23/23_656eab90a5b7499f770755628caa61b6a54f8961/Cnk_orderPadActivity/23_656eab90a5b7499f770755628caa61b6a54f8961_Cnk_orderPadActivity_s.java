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
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
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
 import com.htb.cnk.data.Setting;
 import com.htb.cnk.data.Version;
 import com.htb.cnk.data.WifiAdmin;
 import com.htb.cnk.lib.BaseActivity;
 import com.htb.cnk.lib.Http;
 import com.htb.constant.Permission;
 import com.htb.constant.Server;
 
 public class Cnk_orderPadActivity extends BaseActivity {
 	final String TAG = "Cnk_orderPad";
 	/** Called when the activity is first created. */
 	private ImageButton mMenuBtn;
 	private TextView mMenuTxt;
 	private ImageButton mSettingsBtn;
 	private TextView mSettingsTxt;
 	private TextView mVersionTxt;
 	private ProgressDialog mpDialog;
 	private final static int UPDATE_MENU = 0;
 	private final static int LATEST_MENU = 1;
 	private final static int DOWNLOAD_NEW_APP = 2;
 	private final static int DO_UPGRADE = 3;
 
 	private String mUrl;
 	private WifiAdmin mWifiAdmin;
 	private String mUpdateAkpDir;
 	private Handler handler = new Handler();
 	private Version version;
 	private static int ARERTDIALOG = 0;
 	private AlertDialog mNetWrorkcancel;
 	private Setting mAppSetting;
 	private AlertDialog.Builder mNetWrorkAlertDialog;
 
 	@Override
 	protected void onResume() {
 		if (ARERTDIALOG == 1) {
 			mNetWrorkcancel.cancel();
 			ARERTDIALOG = 0;
 		}
 		initWifi();
 		syncWithServer();
 		super.onResume();
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		version = new Version(Cnk_orderPadActivity.this);
 		findViews();
 		setClickListeners();
 		Info.setNewCustomer(true);
 		Info.setMode(Info.WORK_MODE_CUSTOMER);
 		Info.setTableId(-1);
 		mWifiAdmin = new WifiAdmin(Cnk_orderPadActivity.this);
 		mpDialog = new ProgressDialog(Cnk_orderPadActivity.this);
 		mAppSetting = new Setting(Cnk_orderPadActivity.this);
 		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		mpDialog.setIndeterminate(false);
 		mpDialog.setCancelable(false);
 		setmNetWrorkAlertDialog(wifiDialog());
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
 
 	private int getCurrentMenuVer() {
 		SharedPreferences sharedPre = getSharedPreferences("menuDB",
 				Context.MODE_PRIVATE);
 		return sharedPre.getInt("ver", -1);
 	}
 
 	private void syncWithServer() {
 		mpDialog = new ProgressDialog(Cnk_orderPadActivity.this);
 		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		// mpDialog.setTitle("请稍等");
 		mpDialog.setMessage("正在与服务器同步...");
 		mpDialog.setIndeterminate(false);
 		mpDialog.setCancelable(false);
 		mpDialog.show();
 		mUpdateAkpDir = Environment.getDataDirectory() + "/data/"
 				+ this.getPackageName() + "/files/";
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
 					handlerSync.sendEmptyMessage(LATEST_MENU);
 				}
 			}
 		}.start();
 	}
 
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
 												Uri.fromFile(new File(
 														mUpdateAkpDir,
 														version.UPDATE_SAVENAME)),
 												"application/vnd.android.package-archive");
 										startActivity(intent);
 									}
 								}).show();
 
 			}
 			mpDialog.cancel();
 		}
 	};
 
 	public void initWifi() {
 		if (mWifiAdmin.checkNetCardState() == 0
 				|| mWifiAdmin.checkNetCardState() == 1) {
 			ARERTDIALOG = 1;
 			mNetWrorkcancel = wifiDialog().show();
 
 		} else {
 			mpDialog.cancel();
 		}
 		startLock();
 	}
 
 	private AlertDialog.Builder wifiDialog() {
 		final AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(
 				Cnk_orderPadActivity.this);
 		mAlertDialog.setTitle("错误");// 设置对话框标题
 		mAlertDialog.setMessage("网络连接失败，请检查网络后重试");// 设置对话框内容
 		mAlertDialog.setCancelable(false);
 		mAlertDialog.setPositiveButton("连接",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						mpDialog.setMessage("正在连接wifi，请稍等");
 						mpDialog.show();
 						new Thread(new wifiConnect()).start();
 					}
 				});
 		mAlertDialog.setNegativeButton("退出",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						finish();
 					}
 				});
 
 		return mAlertDialog;
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
 
 	void downFile(final String url) {
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
 
 	void down() {
 		handler.post(new Runnable() {
 			public void run() {
 				update();
 			}
 		});
 
 	}
 
 	private Handler errHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what < 0) {
 				mpDialog.cancel();
 				Log.e("fetch Data failed", "errno: " + msg.what);
 				popErrorDlg(msg.what);
 			}
 		}
 
 	};
 
 	void popErrorDlg(int err) {
 		new AlertDialog.Builder(Cnk_orderPadActivity.this).setTitle("错误")
 				.setMessage("更新软件失败！")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).show();
 
 	}
 
 	void update() {
 		handlerSync.sendEmptyMessage(DO_UPGRADE);
 	}
 
 	class wifiConnect implements Runnable {
 		public void run() {
 			try {
 				mWifiAdmin.openNetCard();
 				if (mWifiAdmin.checkNetCardState() == 0
 						|| mWifiAdmin.checkNetCardState() == 1) {
 					wifiConnectHandle.sendEmptyMessage(-1);
 				} else {
 					wifiConnectHandle.sendEmptyMessage(1);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private Handler wifiConnectHandle = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				ARERTDIALOG = 1;
 				mNetWrorkcancel = wifiDialog().show();
 			} else {
 				Toast.makeText(Cnk_orderPadActivity.this, "当前wifi状态已经连接", 
 						Toast.LENGTH_SHORT).show();
 			}
 		}
 	};
 
 	private void startLock() {
 		mWifiAdmin.creatWifiLock();
 		mWifiAdmin.acquireWifiLock();
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
 					})
			.setNegativeButton("取消", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog,
						int which) {
				}
			}).show();
 			return true;
 		} else {
 			return super.onKeyDown(keyCode, event);
 		}
 	}
 
 	public AlertDialog.Builder getmNetWrorkAlertDialog() {
 		return mNetWrorkAlertDialog;
 	}
 
 	public void setmNetWrorkAlertDialog(AlertDialog.Builder mNetWrorkAlertDialog) {
 		this.mNetWrorkAlertDialog = mNetWrorkAlertDialog;
 	}
 
 }
