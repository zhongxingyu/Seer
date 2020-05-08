 package com.htb.cnk;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.SimpleAdapter;
 import android.widget.Toast;
 
 import com.htb.cnk.data.Info;
 import com.htb.cnk.data.MyOrder;
 import com.htb.cnk.data.NotificationTypes;
 import com.htb.cnk.data.Notifications;
 import com.htb.cnk.data.TableSetting;
 import com.htb.cnk.data.UserData;
 import com.htb.cnk.lib.BaseActivity;
 
 public class TableActivity extends BaseActivity {
 	private final int UPDATE_TABLE_INFOS = 5;
 	private final int DISABLE_GRIDVIEW = 10;
 	private final int PHONE_STATUS = 50;
 	private final int NOTIFICATION_STATUS = 100;
 	private static int ARERTDIALOG = 0;
 	private TableSetting mSettings = new TableSetting();
 	private List<Map<String, String>> mTableSettings = new ArrayList<Map<String, String>>();
 	private Button mBackBtn;
 	private Button mUpdateBtn;
 	private Button mStatisticsBtn;
 	private Button mManageBtn;
 	private GridView gridview;
 	private ProgressDialog mpDialog;
 	private SimpleAdapter saImageItems;
 	private Handler handler = new Handler();
 	private ItemClickListener mTableClicked;
 	private ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
 	private Notifications mNotificaion = new Notifications();
 	private NotificationTypes mNotificationType = new NotificationTypes();
 	private MyOrder mMyOrder;
 	private AlertDialog.Builder mNetWrorkAlertDialog;
 	private AlertDialog mNetWrorkcancel;
 
 	@Override
 	protected void onDestroy() {
 		handler.removeCallbacks(runnable); // 停止刷新
 		handler.removeCallbacksAndMessages(runnable); // 停止刷新
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onStop() {
 		handler.removeCallbacks(runnable); // 停止刷新
 		handler.removeCallbacksAndMessages(runnable); // 停止刷新
 		super.onStop();
 	}
 
 	@Override
 	protected void onResume() {
 		// mNetWrorkAlertDialog.;
 		if (ARERTDIALOG == 1) {
 			mNetWrorkcancel.cancel();
 			ARERTDIALOG = 0;
 		}
 		mpDialog.setTitle("请稍等");
 		mpDialog.setMessage("正在获取状态...");
 		mpDialog.setIndeterminate(false);
 		mpDialog.setCancelable(false);
 		mpDialog.show();
 
 		handler.postDelayed(runnable, 1000 * 1);
 		super.onResume();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.table_activity);
 		mMyOrder = new MyOrder(TableActivity.this);
 		findViews();
 		setClickListeners();
 		mpDialog = new ProgressDialog(TableActivity.this);
 		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		mNetWrorkAlertDialog = networkDialog();
 	}
 
 	private void findViews() {
 		mBackBtn = (Button) findViewById(R.id.back);
 		mUpdateBtn = (Button) findViewById(R.id.updateMenu);
 		mStatisticsBtn = (Button) findViewById(R.id.statistics);
 		mManageBtn = (Button) findViewById(R.id.management);
 		gridview = (GridView) findViewById(R.id.gridview);
 	}
 
 	private void setClickListeners() {
 		mTableClicked = new ItemClickListener();
 		mBackBtn.setOnClickListener(backClicked);
 		mUpdateBtn.setOnClickListener(updateClicked);
 		mStatisticsBtn.setOnClickListener(statisticsClicked);
 		mManageBtn.setOnClickListener(manageClicked);
 	}
 
 	class tableThread implements Runnable {
 		public void run() {
 			try {
 				Message msg = new Message();
 				tableHandle.sendEmptyMessage(DISABLE_GRIDVIEW);
 				mTableSettings.clear();
 				//mSettings.clear();
 				mNotificaion.clear();
 				mNotificaion.getNotifiycations();
 				mNotificationType.getNotifiycationsType();
 				int ret = mSettings.getTableStatusFromServer();
 				if (ret < 0) {
 					Log.d("ret", "ret: " + ret);
 					tableHandle.sendEmptyMessage(ret);
 					return;
 				}
 				msg.what = UPDATE_TABLE_INFOS;
 				tableHandle.sendMessage(msg);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	class cleanNotification implements Runnable {
 		public void run() {
 			try {
 				Message msg = new Message();
 				handler.removeCallbacks(runnable); // 停止刷新
 				handler.removeCallbacksAndMessages(runnable); // 停止刷新
 				int ret = mNotificaion.cleanNotifications(Info.getTableId());
 				if (ret < 0) {
 					notificationHandle.sendEmptyMessage(ret);
 					return;
 				}
 				msg.what = ret;
 				notificationHandle.sendMessage(msg);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	class ItemClickListener implements OnItemClickListener {
 
 		public void onItemClick(AdapterView<?> arg0,// The AdapterView where the
 													// click happened
 				View arg1,// The view within the AdapterView that was clicked
 				int arg2,// The position of the view in the adapter
 				long arg3// The row id of the item that was clicked
 		) {
 
 			Info.setTableName(mSettings.getName(arg2));
 			Info.setTableId(mSettings.getId(arg2));
 			int status = mSettings.getStatus(arg2);
 			Log.d("status", "status:" + status);
 			switch (status) {
 			case 1:
 				Dialog cleanDialog = cleanDialog();
 				cleanDialog.show();
 				break;
 			case 50:
 			case 51:
 				AlertDialog.Builder addPhoneDilog = addPhoneDialog(arg2);
 				addPhoneDilog.show();
 				break;
 			case 100:
 			case 101:
 			case 150:
 			case 151:
 				AlertDialog.Builder notificationDialog = notificationDialog();
 				notificationDialog.show();
 				break;
 			default:
 				AlertDialog.Builder addDialog = addDialog();
 				addDialog.show();
 				break;
 			}
 			saImageItems.notifyDataSetChanged();
 
 		}
 	}
 
 	private AlertDialog.Builder networkDialog() {
 		final AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(
 				TableActivity.this);
 		mAlertDialog.setTitle("错误");// 设置对话框标题
 		mAlertDialog.setMessage("网络连接失败，请检查网络后重试");// 设置对话框内容
 		mAlertDialog.setCancelable(false);
 		mAlertDialog.setPositiveButton("重试",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						dialog.cancel();
 						ARERTDIALOG = 0;
 						mpDialog.setTitle("请稍等");
 						mpDialog.setMessage("正在获取状态...");
 						mpDialog.setIndeterminate(false);
 						mpDialog.setCancelable(false);
 						mpDialog.show();
 						handler.postDelayed(runnable, 1000 * 1);
 					}
 				});
 		mAlertDialog.setNegativeButton("退出",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						ARERTDIALOG = 0;
 						dialog.cancel();
 						mpDialog.cancel();
 						finish();
 					}
 				});
 		
 		return mAlertDialog;
 	}
 
 	private AlertDialog.Builder cleanTableDialog() {
 		final AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(
 				TableActivity.this);
 		mAlertDialog.setMessage("请确认是否清台");// 设置对话框内容
 		mAlertDialog.setCancelable(false);
 		mAlertDialog.setPositiveButton("清台",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						// dialog.cancel();
 						mpDialog.setTitle("请稍等");
 						mpDialog.setMessage("正在清台...");
 						mpDialog.setIndeterminate(false);
 						mpDialog.setCancelable(false);
 						mpDialog.show();
 						cleanTableThread();
 					}
 				});
 		mAlertDialog.setNegativeButton("取消",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						dialog.cancel();
 					}
 				});
 		return mAlertDialog;
 	}
 
 	private AlertDialog.Builder cleanPhoneDialog(final int position) {
 		final AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(
 				TableActivity.this);
 		mAlertDialog.setMessage("请确认是否清除手机点菜");// 设置对话框内容
 		mAlertDialog.setCancelable(false);
 		mAlertDialog.setPositiveButton("是",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						mpDialog.setTitle("请稍等");
 						mpDialog.setMessage("正在删除手机点的菜...");
 						mpDialog.setIndeterminate(false);
 						mpDialog.setCancelable(false);
 						mpDialog.show();
 						cleanPhoneThread(position);
 						dialog.cancel();
 					}
 				});
 		mAlertDialog.setNegativeButton("否",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						dialog.cancel();
 					}
 				});
 		return mAlertDialog;
 	}
 
 	private AlertDialog.Builder addDialog() {
 		final CharSequence[] additems = { "开台-顾客模式 ", "开台-服务模式" };
 
 		AlertDialog.Builder addDialog = new AlertDialog.Builder(
 				TableActivity.this);
 		addDialog.setTitle("选择功能") // 标题
 				.setIcon(R.drawable.ic_launcher) // icon
 				// .setCancelable(true) // 不响应back按钮
 				.setItems(additems, new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						mMyOrder.clear();
 						Intent intent = new Intent();
 						switch (which) {
 						case 0:
 							intent.setClass(TableActivity.this,
 									MenuActivity.class);
 							Info.setMode(Info.WORK_MODE_CUSTOMER);
 							Info.setNewCustomer(true);
 							TableActivity.this.startActivity(intent);
 							TableActivity.this.finish();
 							break;
 						case 1:
 							intent.setClass(TableActivity.this,
 									MenuActivity.class);
 							Info.setMode(Info.WORK_MODE_WAITER);
 							TableActivity.this.startActivity(intent);
 							break;
 						}
 					}
 				}).create();
 		return addDialog;
 	}
 
 	private AlertDialog.Builder addPhoneDialog(final int position) {
 		final CharSequence[] additems = { "查看手机已点的菜", "取消手机已点的菜" };
 
 		AlertDialog.Builder addPhoneDialog = new AlertDialog.Builder(
 				TableActivity.this);
 		addPhoneDialog.setTitle("选择功能") // 标题
 				.setIcon(R.drawable.ic_launcher) // icon
 				// .setCancelable(true) // 不响应back按钮
 				.setItems(additems, new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						Intent intent = new Intent();
 						switch (which) {
 						case 0:
 							intent.setClass(TableActivity.this,
 									PhoneActivity.class);
 							TableActivity.this.startActivity(intent);
 							break;
 						case 1:
 							final AlertDialog.Builder phoneDialog = cleanPhoneDialog(position);
 							phoneDialog.show();
 							break;
 						default:
 							break;
 						}
 					}
 				}).create();
 		return addPhoneDialog;
 	}
 
 	private AlertDialog.Builder notificationDialog() {
 		List<String> add = mNotificaion
 				.getNotifiycationsType(Info.getTableId());
 		String[] additems = (String[]) add.toArray(new String[add.size()]);
 		AlertDialog.Builder addPhoneDialog = new AlertDialog.Builder(
 				TableActivity.this);
 		addPhoneDialog.setTitle("客户呼叫需求").setIcon(R.drawable.ic_launcher)
 				.setItems(additems, null)
 				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 
 					}
 				})
 				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						new Thread(new cleanNotification()).start();
 					}
 				}).create();
 		return addPhoneDialog;
 	}
 
 	private Dialog cleanDialog() {
 		final CharSequence[] cleanitems = { "清台", "删除菜", "添加菜", "查看菜" };
 		Dialog cleanDialog = new AlertDialog.Builder(TableActivity.this)
 				.setTitle("选择功能")
 				// 设置标题
 				.setItems(cleanitems, new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						Intent intent = new Intent();
 						switch (which) {
 						case 0:
 							final AlertDialog.Builder mAlertDialog = cleanTableDialog();
 							mAlertDialog.show();
 							break;
 						case 1:
 							intent.setClass(TableActivity.this,
 									DelOrderActivity.class);
 							TableActivity.this.startActivity(intent);
 							break;
 						case 2:
 							intent.setClass(TableActivity.this,
 									MenuActivity.class);
 							Info.setMode(Info.WORK_MODE_WAITER);
 							TableActivity.this.startActivity(intent);
 							break;
 						case 3:
 							intent.setClass(TableActivity.this,
 									QueryOrderActivity.class);
 							TableActivity.this.startActivity(intent);
 							break;
 						default:
 							break;
 						}
 
 					}
 				}).create();
 		return cleanDialog;
 	}
 
 	private Handler tableHandle = new Handler() {
 		public void handleMessage(Message msg) {
 
 			if (msg.what < 0) {
 				handler.removeCallbacks(runnable); // 停止刷新
 				handler.removeCallbacksAndMessages(runnable); // 停止刷新
 				mpDialog.cancel();
 				ARERTDIALOG = 1;
 				mNetWrorkcancel = mNetWrorkAlertDialog.show();
 			} else {
 				switch (msg.what) {
 				case UPDATE_TABLE_INFOS:
 					setTableInfos();
 					break;
 				case DISABLE_GRIDVIEW:
 					gridview.setOnItemClickListener(null);
 					break;
 				default:
 					break;
 				}
 			}
 		}
 	};
 
 	private void setTableInfos() {
 		Log.d("Notification", "NotificationNum:" + mNotificaion.size());
 		if (lstImageItem.size() > 0) {
 			for (int i = 0, n = 0; i < mSettings.size(); i++) {
 				int status = mSettings.getStatus(i);
 				if (status < NOTIFICATION_STATUS
 						&& mNotificaion.getId(n) == mSettings.getId(i)) {
 
 					status = status + NOTIFICATION_STATUS;
 					n++;
 				}
 				setTableIcon(i, status);
 			}
 		} else {
 			for (int i = 0, n = 0; i < mSettings.size(); i++) {
 				int status = mSettings.getStatus(i);
 				if (status < NOTIFICATION_STATUS
 						&& mNotificaion.getId(n) == mSettings.getId(i)) {
 					status = status + NOTIFICATION_STATUS;
 					n++;
 				}
 				setTableIcon(i, status);
 			}
 			saImageItems = new SimpleAdapter(TableActivity.this, lstImageItem,
 					R.layout.table_item,
 					new String[] { "imageItem", "ItemText" }, new int[] {
 							R.id.ItemImage, R.id.ItemText }) {
 			};
 			gridview.setAdapter(saImageItems);
 		}
 
 		gridview.setVisibility(View.VISIBLE);
 		saImageItems.notifyDataSetChanged();
 		mpDialog.cancel();
 		gridview.setOnItemClickListener(mTableClicked);
 	}
 
 	private void setTableIcon(int position, int status) {
 		HashMap<String, Object> map;
 		if (lstImageItem.size() <= position) {
 			map = new HashMap<String, Object>();
 			map.put("ItemText", "第" + mSettings.getName(position) + "桌");
 		} else {
 			map = lstImageItem.get(position);
 		}
 
 		switch (status) {
 		case 0:
 			map.put("imageItem", R.drawable.table_red);
 			break;
 		case 1:
 			map.put("imageItem", R.drawable.table_blue);
 			break;
 		case 50:
 		case 51:
 			map.put("imageItem", R.drawable.table_yellow);
 			break;
 		case 100:
 			map.put("imageItem", R.drawable.table_rednotification);
 			mSettings.setStatus(position, status);
 			break;
 		case 101:
 			map.put("imageItem", R.drawable.table_bluenotification);
 			mSettings.setStatus(position, status);
 			break;
 		case 150:
 		case 151:
 			map.put("imageItem", R.drawable.table_yellownotification);
 			mSettings.setStatus(position, status);
 			break;
 		default:
 			map.put("imageItem", R.drawable.table_red);
 			break;
 		}
 		if (lstImageItem.size() <= position) {
 			lstImageItem.add(map);
 		}
 	}
 
 	private Handler notificationHandle = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what < 0) {
 				Toast.makeText(getApplicationContext(), "b", Toast.LENGTH_SHORT)
 						.show();
 			} else {
 				Toast.makeText(getApplicationContext(),
 						getResources().getString(R.string.claenNotificaion),
 						Toast.LENGTH_SHORT).show();
 				handler.postDelayed(runnable, 100 * 1);
 			}
 		}
 	};
 
 	private void cleanTableThread() {
 		new Thread() {
 			public void run() {
 				try {
 					Message msg = new Message();
 					int ret;
 					ret = mSettings.updatusStatus(Info.getTableId(), 0);
 					if (ret < 0) {
 						tableHandle.sendEmptyMessage(ret);
 						return;
 					}
 					
 					ret = mMyOrder.delPhoneTable(Info.getTableId(), 0, -1);
 					if (ret < 0) {
 						tableHandle.sendEmptyMessage(ret);
 						return;
 					}
 					
 					ret = mSettings.cleanTalble(Info.getTableId());
 					if (ret < 0) {
 						tableHandle.sendEmptyMessage(ret);
 						return;
 					}
 
 					//lstImageItem.clear();
 					//mSettings.clear();
 
 					mNotificaion.getNotifiycations();
 					ret = mSettings.getTableStatusFromServer();
 					if (ret < 0) {
 						tableHandle.sendEmptyMessage(ret);
 						return;
 					}
 					msg.what = UPDATE_TABLE_INFOS;
 					tableHandle.sendMessage(msg);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	private void cleanPhoneThread(final int position) {
 		new Thread() {
 			public void run() {
 				try {
 					Message msg = new Message();
 					int ret;
 					ret = mSettings.updatusStatus(Info.getTableId(),
 							mSettings.getStatus(position) - PHONE_STATUS);
 					if (ret < 0 ) {
 						tableHandle.sendEmptyMessage(ret);
 						return;
 					}
 					
 					ret = mMyOrder.delPhoneTable(Info.getTableId(), 0, -1);
 					if (ret < 0 ) {
 						tableHandle.sendEmptyMessage(ret);
 						return;
 					}
 					
 					//mSettings.clear();
 					mNotificaion.getNotifiycations();
 					mMyOrder.phoneClear();
 					ret = mSettings.getTableStatusFromServer();
 					if (ret < 0 ) {
 						tableHandle.sendEmptyMessage(ret);
 						return;
 					}
 					msg.what = UPDATE_TABLE_INFOS;
 					tableHandle.sendMessage(msg);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	private OnClickListener backClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			TableActivity.this.finish();
 		}
 	};
 
 	private OnClickListener updateClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			Intent intent = new Intent();
 			intent.setClass(TableActivity.this, UpdateMenuActivity.class);
 			TableActivity.this.startActivity(intent);
 			TableActivity.this.finish();
 
 		}
 	};
 
 	private OnClickListener manageClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			Intent intent = new Intent();
 			intent.setAction("android.intent.action.VIEW");
 			Uri content_url = Uri.parse(getResources().getString(
 					R.string.manageUri));
 			intent.setData(content_url);
 			// intent.setClassName("com.android.browser","com.android.browser.BrowserActivity");
 			startActivity(intent);
 
 		}
 	};
 
 	private OnClickListener statisticsClicked = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			LoginDlg loginDlg = new LoginDlg(TableActivity.this, StatisticsActivity.class);
 			loginDlg.show();
 		}
 
 	};
 
 	private Runnable runnable = new Runnable() {
 		public void run() {
 			this.update();
 		}
 
 		void update() {
 			// 刷新msg的内容
 			new Thread(new tableThread()).start();
 			handler.postDelayed(this, 1000 * 20);// 间隔20秒
 		}
 	};
 }
