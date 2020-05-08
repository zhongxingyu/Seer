 package com.htb.cnk;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.text.InputFilter;
 import android.text.method.DigitsKeyListener;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.SimpleAdapter;
 import android.widget.Toast;
 
 import com.htb.cnk.NotificationTableService.MyBinder;
 import com.htb.cnk.data.Info;
 import com.htb.cnk.data.MyOrder;
 import com.htb.cnk.data.NotificationTypes;
 import com.htb.cnk.data.Notifications;
 import com.htb.cnk.data.TableSetting;
 import com.htb.cnk.lib.BaseActivity;
 import com.htb.cnk.lib.Ringtone;
 import com.htb.constant.Table;
 
 public class TableActivity extends BaseActivity {
 	private final int UPDATE_TABLE_INFOS = 5;
 	private final int DISABLE_GRIDVIEW = 10;
	private static int ARERTDIALOG = 0;
 	private TableSetting mSettings;
 	private Button mBackBtn;
 	private Button mUpdateBtn;
 	private Button mStatisticsBtn;
 	private Button mManageBtn;
 	private GridView gridview;
 	private ProgressDialog mpDialog;
 	private SimpleAdapter mImageItems;
 	private tableItemClickListener mTableClicked;
 	private ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
 	private Notifications mNotificaion = new Notifications();
 	private NotificationTypes mNotificationType = new NotificationTypes();
 	private MyOrder mMyOrder;
 	private AlertDialog.Builder mNetWrorkAlertDialog;
 	private AlertDialog mNetWrorkcancel;
 	private Ringtone mRingtone;
 	private int mTableMsg;
 	private int mRingtoneMsg;
 	private MyReceiver mReceiver;
 	private NotificationTableService.MyBinder binder;
 	private boolean binderFlag;
 	private Intent intent;
 	private List<Integer> selectedTable = new ArrayList<Integer>();
 
 	@Override
 	protected void onDestroy() {
 		unbindService(conn);
 		unregisterReceiver(mReceiver);
 		super.onDestroy(); 
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if (ARERTDIALOG == 1) {
 			mNetWrorkcancel.cancel();
 			ARERTDIALOG = 0;
 		}
 		showProgressDlg("正在获取状态，请稍等。。。");
 		binderStart();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.table_activity);
 
 		mMyOrder = new MyOrder(TableActivity.this);
 		mSettings = new TableSetting();
 		mRingtone = new Ringtone(TableActivity.this);
 		mpDialog = new ProgressDialog(TableActivity.this);
 		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		mpDialog.setTitle("请稍等");
 		mpDialog.setIndeterminate(false);
 		mpDialog.setCancelable(false);
 
 		findViews();
 		setClickListeners();
 
 		mNetWrorkAlertDialog = networkDialog();
 		Info.setMode(Info.WORK_MODE_WAITER);
 		intent = new Intent(TableActivity.this, NotificationTableService.class);
 		startService(intent);
 
 		bindService(intent, conn, Context.BIND_AUTO_CREATE);
 		mReceiver = new MyReceiver();
 		IntentFilter filter = new IntentFilter(
 				NotificationTableService.SERVICE_IDENTIFIER);
 		registerReceiver(mReceiver, filter);
 
 		new Thread(new getNotificationType()).start();
 
 	}
 
 	public void showProgressDlg(String msg) {
 		mpDialog.setMessage(msg);
 		mpDialog.show();
 	}
 
 	private void findViews() {
 		mBackBtn = (Button) findViewById(R.id.back);
 		mUpdateBtn = (Button) findViewById(R.id.combineTable);
 		mStatisticsBtn = (Button) findViewById(R.id.logout);
 		mManageBtn = (Button) findViewById(R.id.management);
 		gridview = (GridView) findViewById(R.id.gridview);
 	}
 
 	private void setClickListeners() {
 		mTableClicked = new tableItemClickListener();
 		mBackBtn.setOnClickListener(backClicked);
 		mUpdateBtn.setOnClickListener(combineClicked);
 		mStatisticsBtn.setOnClickListener(logoutClicked);
 		mManageBtn.setOnClickListener(manageClicked);
 	}
 
 	private ServiceConnection conn = new ServiceConnection() {
 
 		@Override
 		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
 			binder = (MyBinder) arg1;
 			binderFlag = true;
 		}
 
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 
 		}
 	};
 
 	// TODO define
 	class tableItemClickListener implements OnItemClickListener {
 
 		public void onItemClick(AdapterView<?> arg0,// The AdapterView where the
 													// click happened
 				View arg1,// The view within the AdapterView that was clicked
 				int arg2,// The position of the view in the adapter
 				long arg3// The row id of the item that was clicked
 		) {
 
 			Info.setTableName(mSettings.getNameIndex(arg2));
 			Info.setTableId(mSettings.getIdIndex(arg2));
 			int status = mSettings.getStatus(arg2);
 			tableItemChioceDialog(arg2, status);
 			mImageItems.notifyDataSetChanged();
 		}
 
 		private void tableItemChioceDialog(int arg2, int status) {
 			switch (status) {
 			case 0:
 				AlertDialog.Builder addDialog = addDialog();
 				addDialog.show();
 				break;
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
 				break;
 			}
 		}
 	}
 
 	private AlertDialog.Builder networkDialog() {
 		final AlertDialog.Builder networkAlertDialog = alertDialogBuilder(false);
 		networkAlertDialog.setTitle("错误");// 设置对话框标题
 		networkAlertDialog.setMessage("网络连接失败，请检查网络后重试");// 设置对话框内容
 		networkAlertDialog.setPositiveButton("重试",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						dialog.cancel();
 						ARERTDIALOG = 0;
 						showProgressDlg("正在获取状态...");
 						binderStart();
 					}
 				});
 		networkAlertDialog.setNegativeButton("退出",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						ARERTDIALOG = 0;
 						dialog.cancel();
 						finish();
 					}
 				});
 
 		return networkAlertDialog;
 	}
 
 	private Dialog cleanDialog() {
 		final CharSequence[] cleanitems = { "清台", "转台", "删除菜", "添加菜", "查看菜" };
 		Dialog cleanDialog = alertDialogBuilder(true).setTitle("选择功能")
 				.setItems(cleanitems, new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						Intent intent = new Intent();
 						cleanChioceMode(which, intent);
 					}
 
 					private void cleanChioceMode(int which, Intent intent) {
 						switch (which) {
 						case 0:
 							final AlertDialog.Builder mAlertDialog = cleanTableDialog();
 							mAlertDialog.show();
 							break;
 						case 1:
 							final AlertDialog.Builder mChangeDialog = changeTableDialog();
 							mChangeDialog.show();
 							break;
 						case 2:
 							intent.setClass(TableActivity.this,
 									DelOrderActivity.class);
 							TableActivity.this.startActivity(intent);
 							break;
 						case 3:
 							intent.setClass(TableActivity.this,
 									MenuActivity.class);
 							Info.setMode(Info.WORK_MODE_WAITER);
 							TableActivity.this.startActivity(intent);
 							break;
 						case 4:
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
 
 	private AlertDialog.Builder cleanTableDialog() {
 		final AlertDialog.Builder cleanTableAlertDialog = alertDialogBuilder(false);
 		cleanTableAlertDialog.setMessage("请确认是否清台");// 设置对话框内容
 		cleanTableAlertDialog.setPositiveButton("清台",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						showProgressDlg("正在清台...");
 						cleanTableThread();
 					}
 				});
 		cleanTableAlertDialog.setNegativeButton("取消", null);
 		return cleanTableAlertDialog;
 	}
 
 	private AlertDialog.Builder cleanPhoneDialog(final int position) {
 		final AlertDialog.Builder cleanPhoneAlertDialog = alertDialogBuilder(false);
 		cleanPhoneAlertDialog.setMessage("是否清除顾客点的菜");// 设置对话框内容
 		cleanPhoneAlertDialog.setPositiveButton("是",
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						showProgressDlg("正在删除手机点的菜...");
 						cleanPhoneThread(position);
 						dialog.cancel();
 					}
 				});
 		cleanPhoneAlertDialog.setNegativeButton("否", null);
 		return cleanPhoneAlertDialog;
 	}
 
 	private AlertDialog.Builder addDialog() {
 		final CharSequence[] additems = { "开台-顾客模式 ", "开台-服务模式", "开台-复制模式" };
 		AlertDialog.Builder addDialog = alertDialogBuilder(true);
 		addDialog.setTitle("选择功能") // 标题
 				.setItems(additems, new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						mMyOrder.clear();
 						Intent intent = new Intent();
 						addDialogChoiceMode(which, intent);
 					}
 
 					private void addDialogChoiceMode(int which, Intent intent) {
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
 							if (Info.getMenu() == Info.ORDER_QUCIK_MENU) {
 								intent.setClass(TableActivity.this,
 										QuickMenuActivity.class);
 								TableActivity.this.startActivity(intent);
 							} else {
 								intent.setClass(TableActivity.this,
 										MenuActivity.class);
 								Info.setMode(Info.WORK_MODE_WAITER);
 								TableActivity.this.startActivity(intent);
 							}
 							break;
 						case 2:
 							final AlertDialog.Builder mChangeDialog = copyTableDialog();
 							mChangeDialog.show();
 							break;
 						default:
 							break;
 						}
 					}
 				}).create();
 		return addDialog;
 	}
 
 	private AlertDialog.Builder addPhoneDialog(final int position) {
 		final CharSequence[] additems = { "查看顾客已点的菜", "取消顾客已点的菜" };
 		AlertDialog.Builder addPhoneDialog = alertDialogBuilder(true);
 		addPhoneDialog.setTitle("选择功能") // 标题
 				.setItems(additems, new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						Intent intent = new Intent();
 						addPhoneChoiceMode(position, which, intent);
 					}
 
 					private void addPhoneChoiceMode(final int position,
 							int which, Intent intent) {
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
 		AlertDialog.Builder notificationDialog = alertDialogBuilder(false);
 		notificationDialog.setTitle("客户呼叫需求").setIcon(R.drawable.ic_launcher)
 				.setItems(additems, null).setNegativeButton("取消", null)
 				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						new Thread(new cleanNotification()).start();
 					}
 				}).create();
 		return notificationDialog;
 	}
 
 	protected Builder changeTableDialog() {
 		View layout = getDialogLayout();
 		final EditText tableIdEdit = (EditText) layout
 				.findViewById(R.id.tableIdEdit);
 		final EditText personsEdit = (EditText) layout
 				.findViewById(R.id.personsEdit);
 		final AlertDialog.Builder changeTableAlertDialog = alertDialogBuilder(false);
 		changeTableAlertDialog.setTitle("请输入");
 		changeTableAlertDialog.setView(layout);
 		changeTableAlertDialog.setPositiveButton("确定",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						String changeTId = tableIdEdit.getText().toString();
 						String changePersons = personsEdit.getText().toString();
 						if (changeTId.equals("") || changePersons.equals("")) {
 							toastText(R.string.idAndPersonsIsNull);
 						} else if (isBoundaryLegal(changeTId)) {
 							changeTable(mSettings.getId(changeTId),
 									Integer.parseInt(changePersons));
 						} else {
 							toastText(R.string.changeTIdWarning);
 						}
 					}
 
 					private boolean isBoundaryLegal(String changeTId) {
 						int tId = Integer.parseInt(changeTId);
 						return tId >= Integer.parseInt(mSettings
 								.getNameIndex(0))
 								&& tId <= Integer.parseInt(mSettings
 										.getNameIndex(mSettings.size() - 1))
 								&& mSettings.getStatusTableId(mSettings
 										.getId(changeTId)) == Table.NORMAL_TABLE_STAUTS;
 					}
 				});
 		changeTableAlertDialog.setNegativeButton("取消", null);
 		return changeTableAlertDialog;
 	}
 
 	protected Builder copyTableDialog() {
 		View layout = getDialogLayout();
 		final EditText tableId = (EditText) layout
 				.findViewById(R.id.tableIdEdit);
 		final EditText persons = (EditText) layout
 				.findViewById(R.id.personsEdit);
 		final AlertDialog.Builder copyTableAlertDialog = alertDialogBuilder(false);
 		copyTableAlertDialog.setTitle("请输入");
 		copyTableAlertDialog.setView(layout);
 		copyTableAlertDialog.setPositiveButton("确定",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						String changeTId = tableId.getText().toString();
 						String changePersons = persons.getText().toString();
 						if (changeTId.equals("") || changePersons.equals("")) {
 							toastText(R.string.idAndPersonsIsNull);
 						} else if (isBoundaryLegal(changeTId)) {
 							copyTable(mSettings.getId(changeTId),
 									Integer.parseInt(changePersons));
 						} else {
 							toastText(R.string.copyTIdwarning);
 						}
 					}
 
 					private boolean isBoundaryLegal(String changeTId) {
 						int tId = Integer.parseInt(changeTId);
 						return tId >= Integer.parseInt(mSettings
 								.getNameIndex(0))
 								&& tId <= Integer.parseInt(mSettings
 										.getNameIndex(mSettings.size() - 1))
 								&& mSettings.getStatusTableId(mSettings
 										.getId(changeTId)) == Table.OPEN_TABLE_STATUS;
 					}
 				});
 		copyTableAlertDialog.setNegativeButton("取消", null);
 		return copyTableAlertDialog;
 	}
 
 	private View getDialogLayout() {
 		LayoutInflater inflater = getLayoutInflater();
 		View layout = inflater.inflate(R.layout.dialog,
 				(ViewGroup) findViewById(R.id.dialog));
 		return layout;
 	}
 
 	protected Builder combineDialog() {
 		final EditText combineTableText = new EditText(this);
 		combineTableText.setKeyListener(new DigitsKeyListener(false, true));
 		combineTableText
 				.setFilters(new InputFilter[] { new InputFilter.LengthFilter(3) });
 		ArrayList<HashMap<String, Object>> combine = mSettings.getCombine();
 		final List<String> tableName = new ArrayList<String>();
 		final List<Integer> tableId = new ArrayList<Integer>();
 		for (HashMap<String, Object> item : combine) {
 			tableName.add(item.get("name").toString());
 			tableId.add(item.get("id").hashCode());
 		}
 
 		final int size = mSettings.size();
 		final boolean selected[] = new boolean[size];
 		final AlertDialog.Builder combineAlertDialog = alertDialogBuilder(false);
 		combineAlertDialog.setTitle("请选择合并桌号");
 		combineAlertDialog.setMultiChoiceItems(
 				(String[]) tableName.toArray(new String[0]), null,
 				new DialogInterface.OnMultiChoiceClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialogInterface,
 							int which, boolean isChecked) {
 						selected[which] = isChecked;
 					}
 				});
 		DialogInterface.OnClickListener btnListener = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialogInterface, int which) {
 				selectedTable.clear();
 				for (int i = 0; i < selected.length; i++) {
 					if (selected[i] == true) {
 						selectedTable.add(tableId.get(i));
 					}
 				}
 				combineTable(selectedTable, tableName);
 			}
 		};
 
 		combineAlertDialog.setPositiveButton("确认", btnListener);
 		combineAlertDialog.setNegativeButton("取消", null);
 		return combineAlertDialog;
 	}
 
 	private AlertDialog.Builder alertDialogBuilder(boolean cancelable) {
 		final AlertDialog.Builder alertDialog = new AlertDialog.Builder(
 				TableActivity.this);
 		alertDialog.setIcon(R.drawable.ic_launcher);
 		alertDialog.setCancelable(cancelable);
 		return alertDialog;
 	}
 
 	private Handler tableHandle = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 
 			if (msg.what < 0) {
 				if (ARERTDIALOG == 1) {
 					mNetWrorkcancel.cancel();
 				}
 				ARERTDIALOG = 1;
 				mNetWrorkcancel = mNetWrorkAlertDialog.show();
 			} else {
 				switch (msg.what) {
 				case UPDATE_TABLE_INFOS:
 					setTableInfos();
 					if (mSettings.hasPendedPhoneOrder()) {
 						ringtoneHandler.sendEmptyMessage(1);
 					}
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
 
 	private Handler changeTIdHandle = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what == -2) {
 				toastText(R.string.changeTIdWarning);
 			} else if (msg.what == -1) {
 				ARERTDIALOG = 1;
 				mNetWrorkAlertDialog.setMessage("转台失败，请检查连接网络重试");
 				mNetWrorkcancel = mNetWrorkAlertDialog.show();
 			} else {
 				binderStart();
 				toastText(R.string.changeTId);
 			}
 		}
 	};
 
 	private Handler copyTIdHandle = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what == -2) {
 				toastText(R.string.copyTIdwarning);
 			} else if (msg.what == -1) {
 				ARERTDIALOG = 1;
 				mNetWrorkAlertDialog.setMessage("复制失败，请检查连接网络重试");
 				mNetWrorkcancel = mNetWrorkAlertDialog.show();
 			} else {
 				binderStart();
 				toastText(R.string.copyTId);
 			}
 		}
 	};
 
 	private Handler combineTIdHandle = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what == -2) {
 				toastText(R.string.combineTIdWarning);
 			} else if (msg.what == -1) {
 				ARERTDIALOG = 1;
 				mNetWrorkAlertDialog.setMessage("合并出错，请检查连接网络重试");
 				mNetWrorkcancel = mNetWrorkAlertDialog.show();
 			} else {
 				binderStart();
 				toastText(R.string.combineTId);
 			}
 		}
 	};
 
 	private void setTableInfos() {
 
 		if (lstImageItem.size() > 0) {
 			setStatusAndIcon();
 		} else {
 			setStatusAndIcon();
 			mImageItems = new SimpleAdapter(TableActivity.this, lstImageItem,
 					R.layout.table_item,
 					new String[] { "imageItem", "ItemText" }, new int[] {
 							R.id.ItemImage, R.id.ItemText }) {
 			};
 			gridview.setAdapter(mImageItems);
 		}
 		gridview.setVisibility(View.VISIBLE);
 		mImageItems.notifyDataSetChanged();
 		gridview.setOnItemClickListener(mTableClicked);
 	}
 
 	private void setStatusAndIcon() {
 		for (int i = 0, n = 0; i < mSettings.size(); i++) {
 			int status = mSettings.getStatus(i);
 			if (status < Table.NOTIFICATION_STATUS
 					&& mNotificaion.getId(n) == mSettings.getIdIndex(i)) {
 				status = status + Table.NOTIFICATION_STATUS;
 				n++;
 			}
 			setTableIcon(i, status);
 		}
 	}
 
 	private void setTableIcon(int position, int status) {
 		HashMap<String, Object> map;
 		if (lstImageItem.size() <= position) {
 			map = new HashMap<String, Object>();
 			map.put("ItemText", "第" + mSettings.getNameIndex(position) + "桌");
 		} else {
 			map = lstImageItem.get(position);
 		}
 
 		imageItemSwitch(position, status, map);
 		if (lstImageItem.size() <= position) {
 			lstImageItem.add(map);
 		}
 	}
 
 	private void imageItemSwitch(int position, int status,
 			HashMap<String, Object> map) {
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
 	}
 
 	private Handler ringtoneHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what > 0) {
 				mRingtone.play();
 			}
 		}
 	};
 
 	private Handler notificationHandle = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				// Todo network failure warning
 			} else {
 				binderStart();
 			}
 		}
 	};
 
 	class cleanNotification implements Runnable {
 		public void run() {
 			try {
 				int ret = mNotificaion.cleanNotifications(Info.getTableId());
 				notificationHandle.sendEmptyMessage(ret);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	class getNotificationType implements Runnable {
 		public void run() {
 			try {
 				int ret = mNotificationType.getNotifiycationsType();
 				notificationHandle.sendEmptyMessage(ret);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void cleanTableThread() {
 		new Thread() {
 			public void run() {
 				try {
 					Message msg = new Message();
 					int ret;
 					ret = mSettings.cleanTalble(Info.getTableId());
 					if (ret < 0) {
 						tableHandle.sendEmptyMessage(ret);
 						return;
 					}
 					ret = mNotificaion.getNotifiycations();
 					ringtoneHandler.sendEmptyMessage(ret);
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
 					ret = mSettings.updateStatus(Info.getTableId(),
 							mSettings.getStatus(position) - Table.PHONE_STATUS);
 					if (ret < 0) {
 						tableHandle.sendEmptyMessage(ret);
 						return;
 					}
 
 					ret = mMyOrder.cleanServerPhoneOrder(Info.getTableId());
 					if (ret < 0) {
 
 						tableHandle.sendEmptyMessage(ret);
 						return;
 					}
 
 					ret = mNotificaion.getNotifiycations();
 					ringtoneHandler.sendEmptyMessage(ret);
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
 
 	private void binderStart() {
 		if (binderFlag) {
 			binder.start();
 			return;
 		}
 	}
 
 	private void changeTable(final int destTId, final int persons) {
 		new Thread() {
 			public void run() {
 				try {
 					int ret = mSettings.changeTable(TableActivity.this,
 							Info.getTableId(), destTId,
 							mSettings.getName(Info.getTableId()),
 							mSettings.getName(destTId), persons);
 					changeTIdHandle.sendEmptyMessage(ret);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	private void copyTable(final int srcIId, final int persons) {
 		new Thread() {
 			public void run() {
 				try {
 					int ret = mSettings.copyTable(TableActivity.this, srcIId,
 							Info.getTableId(), persons);
 					copyTIdHandle.sendEmptyMessage(ret);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	private void combineTable(final List<Integer> destIId,
 			final List<String> tableName) {
 		new Thread() {
 			public void run() {
 				try {
 					int ret = mSettings.combineTable(TableActivity.this,
 							destIId, tableName);
 					combineTIdHandle.sendEmptyMessage(ret);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	private void toastText(int r) {
 		Toast.makeText(getApplicationContext(), getResources().getString(r),
 				Toast.LENGTH_SHORT).show();
 	}
 
 	private OnClickListener backClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			TableActivity.this.finish();
 		}
 	};
 
 	private OnClickListener combineClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			final AlertDialog.Builder mChangeDialog = combineDialog();
 			mChangeDialog.show();
 		}
 	};
 
 	private OnClickListener manageClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			Intent intent = new Intent();
 			intent.setClass(TableActivity.this, ManageActivity.class);
 			TableActivity.this.startActivity(intent);
 		}
 	};
 
 	private OnClickListener logoutClicked = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			new AlertDialog.Builder(TableActivity.this)
 					.setTitle("注意")
 					.setCancelable(false)
 					.setMessage("确认交班？")
 					.setPositiveButton("确定",
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									Info.setMode(Info.WORK_MODE_CUSTOMER);
 									Info.setTableId(-1);
 									finish();
 								}
 							}).setNegativeButton("取消", null).show();
 		}
 	};
 
 	public class MyReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Bundle bundle = intent.getExtras();
 			mRingtoneMsg = bundle.getInt("ringtoneMessage");
 			mTableMsg = bundle.getInt("tableMessage");
 			mSettings = (TableSetting) bundle
 					.getSerializable(NotificationTableService.SER_KEY);
 			tableHandle.sendEmptyMessage(mTableMsg);
 			ringtoneHandler.sendEmptyMessage(mRingtoneMsg);
 		}
 	}
 
 }
