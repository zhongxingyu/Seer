 package com.htb.cnk;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Message;
 import android.text.Editable;
 import android.text.InputFilter;
 import android.text.TextWatcher;
 import android.text.method.DigitsKeyListener;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.htb.cnk.data.Info;
 import com.htb.cnk.data.Setting;
 import com.htb.constant.Table;
 
 public class TableClickActivity extends TableBaseActivity {
 
 	protected final int UPDATE_TABLE_INFOS = 5;
 	protected final int DISABLE_GRIDVIEW = 10;
 	private List<Integer> selectedTable = new ArrayList<Integer>();
 	private double mIncome;
 	private double mChange;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		mNetWrorkAlertDialog = networkDialog();
 		setClickListeners();
 	}
 	
 	protected void setClickListeners() {
 		mTableClicked = new tableItemClickListener();
 		mBackBtn.setOnClickListener(backClicked);
 		mUpdateBtn.setOnClickListener(checkOutClicked);
 		mStatisticsBtn.setOnClickListener(logoutClicked);
 		mManageBtn.setOnClickListener(manageClicked);
 	}
 
 	protected AlertDialog.Builder networkDialog() {
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
 		final CharSequence[] cleanitems = { "清台", "转台", "退菜", "添加菜", "查看菜" };
 		Dialog cleanDialog = alertDialogBuilder(true).setTitle("选择功能")
 				.setItems(cleanitems, new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						cleanChioceMode(which);
 					}
 
 					private void cleanChioceMode(int which) {
 						switch (which) {
 						case 0:
 							final AlertDialog.Builder mAlertDialog = cleanTableDialog();
 							mAlertDialog.show();
 							break;
 						case 1:
 							final AlertDialog.Builder ChangeDialog = changeTableDialog();
 							ChangeDialog.show();
 							break;
 						case 2:
 							intent.setClass(TableClickActivity.this,
 									DelOrderActivity.class);
 							TableClickActivity.this.startActivity(intent);
 							break;
 
 						case 3:
 							intent.setClass(TableClickActivity.this,
 									MenuActivity.class);
 							Info.setMode(Info.WORK_MODE_WAITER);
 							TableClickActivity.this.startActivity(intent);
 							break;
 						case 4:
 							intent.setClass(TableClickActivity.this,
 									QueryOrderActivity.class);
 							TableClickActivity.this.startActivity(intent);
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
 		cleanTableAlertDialog.setMessage("请确认是否清台");
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
 		cleanPhoneAlertDialog.setMessage("是否清除顾客点的菜");
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
 		addDialog.setTitle("选择功能")
 				.setItems(additems, new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						mMyOrder.clear();
 						addDialogChoiceMode(which);
 					}
 
 					private void addDialogChoiceMode(int which) {
 						switch (which) {
 						case 0:
 							intent.setClass(TableClickActivity.this,
 									MenuActivity.class);
 							Info.setMode(Info.WORK_MODE_CUSTOMER);
 							Info.setNewCustomer(true);
 							TableClickActivity.this.startActivity(intent);
 							TableClickActivity.this.finish();
 							break;
 						case 1:
 							if (Info.getMenu() == Info.ORDER_QUCIK_MENU) {
 								intent.setClass(TableClickActivity.this,
 										QuickMenuActivity.class);
 							} else {
 								intent.setClass(TableClickActivity.this,
 										MenuActivity.class);
 								Info.setMode(Info.WORK_MODE_WAITER);
 							}
 							TableClickActivity.this.startActivity(intent);
 							break;
 						case 2:
 							final AlertDialog.Builder ChangeDialog = copyTableDialog();
 							ChangeDialog.show();
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
 		addPhoneDialog.setTitle("选择功能") 
 				.setItems(additems, new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						addPhoneChoiceMode(position, which);
 					}
 
 					private void addPhoneChoiceMode(final int position,
 							int which) {
 						switch (which) {
 						case 0:
 							intent.setClass(TableClickActivity.this,
 									PhoneActivity.class);
 							TableClickActivity.this.startActivity(intent);
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
 						cleanNotification();
 					}
 				}).create();
 		return notificationDialog;
 	}
 
 	private Builder changeTableDialog() {
 		final AlertDialog.Builder changeTableAlertDialog = alertDialogBuilder(false);
 		final EditText tableIdEdit;
 		final EditText personsEdit;
 		View layout = getDialogLayout(R.layout.change_dialog, R.id.change);
 		personsEdit = (EditText) layout.findViewById(R.id.personsEdit);
 		if (Setting.enabledPersons()) {
 			tableIdEdit = (EditText) layout.findViewById(R.id.tableIdEdit);
 			changeTableAlertDialog.setView(layout);
 		} else {
 			tableIdEdit = editTextListener();
 			changeTableAlertDialog.setView(tableIdEdit);
 		}
 		changeTableAlertDialog.setTitle("请输入");
 		changeTableAlertDialog.setPositiveButton("确定",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						String changePersons;
 						if (Setting.enabledPersons()) {
 							changePersons = personsEdit.getText().toString();
 						} else {
 							changePersons = "0";
 						}
 						String changeTId = tableIdEdit.getText().toString();
 						if (changeTId.equals("") || changePersons.equals("")) {
 							toastText(R.string.idAndPersonsIsNull);
 						} else if (isBoundaryLegal(changeTId,
 								Table.NORMAL_TABLE_STAUTS)) {
 							changeTable(mSettings.getId(changeTId),
 									Integer.parseInt(changePersons));
 						} else {
 							toastText(R.string.changeTIdWarning);
 						}
 					}
 				});
 		changeTableAlertDialog.setNegativeButton("取消", null);
 		return changeTableAlertDialog;
 	}
 
 	private Builder copyTableDialog() {
 		final EditText copyTableText = editTextListener();
 		final AlertDialog.Builder copyTableAlertDialog = alertDialogBuilder(false);
 		copyTableAlertDialog.setTitle("请输入");
 		copyTableAlertDialog.setView(copyTableText);
 		copyTableAlertDialog.setPositiveButton("确定",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						String changeTId = copyTableText.getEditableText()
 								.toString();
 						if (changeTId.equals("")) {
 							toastText(R.string.idAndPersonsIsNull);
 						} else if (isBoundaryLegal(changeTId,
 								Table.OPEN_TABLE_STATUS)) {
 							copyTable(mSettings.getId(changeTId));
 						} else {
 							toastText(R.string.copyTIdwarning);
 						}
 					}
 
 				});
 		copyTableAlertDialog.setNegativeButton("取消", null);
 		return copyTableAlertDialog;
 	}
 
 	private EditText editTextListener() {
 		final EditText copyTableText = new EditText(this);
 		copyTableText.setKeyListener(new DigitsKeyListener(false, true));
 		copyTableText
 				.setFilters(new InputFilter[] { new InputFilter.LengthFilter(3) });
 		return copyTableText;
 	}
 
 	private View getDialogLayout(int layout_dialog, int id) {
 		LayoutInflater inflater = getLayoutInflater();
 		View layout = inflater.inflate(layout_dialog,
 				(ViewGroup) findViewById(id));
 		return layout;
 	}
 
 	private Builder checkOutDialog() {
 		ArrayList<HashMap<String, Object>> checkOut = mSettings.getCombine();
 		final List<Integer> tableId = new ArrayList<Integer>();
		final List<String> tableNameStr = new ArrayList<String>();
 		for (HashMap<String, Object> item : checkOut) {
			tableNameStr.add(item.get("name").toString());
 			tableId.add(item.get("id").hashCode());
 		}
 		final int size = mSettings.size();
 		final boolean selected[] = new boolean[size];
 		final AlertDialog.Builder checkOutAlertDialog = alertDialogBuilder(false);
 		checkOutAlertDialog.setTitle("请选择合并桌号");
 		checkOutAlertDialog.setMultiChoiceItems(
				(String[]) tableNameStr.toArray(new String[0]), null,
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
				tableName.clear();
 				for (int i = 0; i < selected.length; i++) {
 					if (selected[i] == true) {
 						selectedTable.add(tableId.get(i));
						tableName.add(tableNameStr.get(i));
 					}
 				}
 				showProgressDlg("正在统计金额，请稍等");
 				getTotalPriceTable();
 
 			}
 		};
 
 		checkOutAlertDialog.setPositiveButton("确认", btnListener);
 		checkOutAlertDialog.setNegativeButton("取消", null);
 		return checkOutAlertDialog;
 	}
 
	protected Builder checkOutSubmitDialog() {
 		final AlertDialog.Builder changeTableAlertDialog = alertDialogBuilder(false);
 		View layout = getDialogLayout(R.layout.checkout_dialog, R.id.check_out);
 		TextView receivableText = (TextView) layout
 				.findViewById(R.id.receivableQuan);
 		final EditText incomeEdit = (EditText) layout
 				.findViewById(R.id.incomeEdit);
 		final TextView changeText = (TextView) layout
 				.findViewById(R.id.changeQuan);
 		TextWatcher watcher = new TextWatcher() {
 			String tempStr;
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 				tempStr = s.toString();
 			}
 
 			@Override
 			public void afterTextChanged(Editable arg0) {
 				if (tempStr.length() > 0) {
 					mIncome = Double.valueOf(tempStr).doubleValue();
 					mChange = mIncome - mTotalPrice;
 					if (mChange > 0) {
 						changeText.setText(String.valueOf(mChange));
 					} else {
 						changeText.setText("客户你好，你给的金额不足!");
 					}
 				}
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 
 		};
 		incomeEdit.addTextChangedListener(watcher);
 		receivableText.setText(String.valueOf(mTotalPrice));
 		changeTableAlertDialog.setView(layout);
 		changeTableAlertDialog.setTitle("请输入");
 		changeTableAlertDialog.setPositiveButton("确定",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						if (mChange < 0) {
 							toastText("你所给金额不足，请重新结账！");
 						} else {
 							checkOut(selectedTable, tableName, mTotalPrice,
 									mIncome, mChange);
 						}
 					}
 				});
 		changeTableAlertDialog.setNegativeButton("取消", null);
 		return changeTableAlertDialog;
 	}
 
 	private AlertDialog.Builder alertDialogBuilder(boolean cancelable) {
 		final AlertDialog.Builder alertDialog = new AlertDialog.Builder(
 				TableClickActivity.this);
 		alertDialog.setIcon(R.drawable.ic_launcher);
 		alertDialog.setCancelable(cancelable);
 		return alertDialog;
 	}
 
 	private void cleanNotification() {
 		new Thread() {
 			public void run() {
 				try {
 					int ret = mNotificaion
 							.cleanNotifications(Info.getTableId());
 					mNotificationHandler.sendEmptyMessage(ret);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	private void cleanTableThread() {
 		new Thread() {
 			public void run() {
 				try {
 					Message msg = new Message();
 					int ret;
 					ret = mSettings.cleanTalble(Info.getTableId());
 					if (ret < 0) {
 						mTableHandler.sendEmptyMessage(ret);
 						return;
 					}
 					ret = mNotificaion.getNotifiycations();
 					mRingtoneHandler.sendEmptyMessage(ret);
 					ret = mSettings.getTableStatusFromServer();
 					if (ret < 0) {
 						mTableHandler.sendEmptyMessage(ret);
 						return;
 					}
 					msg.what = UPDATE_TABLE_INFOS;
 					mTableHandler.sendMessage(msg);
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
 						mTableHandler.sendEmptyMessage(ret);
 						return;
 					}
 
 					ret = mMyOrder.cleanServerPhoneOrder(Info.getTableId());
 					if (ret < 0) {
 
 						mTableHandler.sendEmptyMessage(ret);
 						return;
 					}
 
 					ret = mNotificaion.getNotifiycations();
 					mRingtoneHandler.sendEmptyMessage(ret);
 					ret = mSettings.getTableStatusFromServer();
 					if (ret < 0) {
 						mTableHandler.sendEmptyMessage(ret);
 						return;
 					}
 					msg.what = UPDATE_TABLE_INFOS;
 					mTableHandler.sendMessage(msg);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	private void getTotalPriceTable() {
 		new Thread() {
 			public void run() {
 				try {
 					double ret = mSettings.getTotalPriceTable(
 							TableClickActivity.this, selectedTable);
 					mTotalPriceTableHandler.sendEmptyMessage((int) ret);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	private void changeTable(final int destTId, final int persons) {
 		new Thread() {
 			public void run() {
 				try {
 					int ret = mSettings.changeTable(TableClickActivity.this,
 							Info.getTableId(), destTId,
 							mSettings.getName(Info.getTableId()),
 							mSettings.getName(destTId), persons);
 					mChangeTIdHandler.sendEmptyMessage(ret);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	private void copyTable(final int srcTId) {
 		new Thread() {
 			public void run() {
 				try {
 					int ret = mSettings.getOrderFromServer(
 							TableClickActivity.this, srcTId);
 					mCopyTIdHandler.sendEmptyMessage(ret);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	private void checkOut(final List<Integer> destIId,
 			final List<String> tableName, final Double receivable,
 			final Double income, final Double change) {
 		new Thread() {
 			public void run() {
 				try {
 					int ret = mSettings.checkOut(TableClickActivity.this,
 							destIId, tableName, receivable, income, change);
 					mCheckOutHandler.sendEmptyMessage(ret);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	protected void toastText(int r) {
 		Toast.makeText(getApplicationContext(), getResources().getString(r),
 				Toast.LENGTH_SHORT).show();
 	}
 
 	private void toastText(String r) {
 		Toast.makeText(getApplicationContext(), r, Toast.LENGTH_SHORT).show();
 	}
 
 	private boolean isNameMinimum(int tId) {
 		return tId >= Integer.parseInt(mSettings.getNameIndex(0));
 	}
 
 	private boolean isNameMaximum(int tId) {
 		return tId <= Integer
 				.parseInt(mSettings.getNameIndex(mSettings.size() - 1));
 	}
 
 	private boolean isStatusLegal(String changeTId, int status) {
 		return mSettings.getStatusTableId(mSettings.getId(changeTId)) == status;
 	}
 
 	private boolean isBoundaryLegal(String changeTId, int status) {
 		int tId = Integer.parseInt(changeTId);
 		return isNameMinimum(tId) && isNameMaximum(tId)
 				&& isStatusLegal(changeTId, status);
 	}
 
 	private OnClickListener checkOutClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			final AlertDialog.Builder ChangeDialog = checkOutDialog();
 			ChangeDialog.show();
 		}
 	};
 	
 	private OnClickListener manageClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			Intent intent = new Intent();
 			intent.setClass(TableClickActivity.this, ManageActivity.class);
 			TableClickActivity.this.startActivity(intent);
 		}
 	};
 	
 	private OnClickListener backClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			TableClickActivity.this.finish();
 		}
 	};
 	
 	private OnClickListener logoutClicked = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			new AlertDialog.Builder(TableClickActivity.this)
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
 
 	class tableItemClickListener implements OnItemClickListener {
 
 		public void onItemClick(AdapterView<?> arg0,// The AdapterView where the
 													// click happened
 				View arg1,// The view within the AdapterView that was clicked
 				int arg2,// The position of the view in the adapter
 				long arg3// The row id of the item that was clicked
 		) {
 
 			Info.setTableName(mSettings.getNameIndex(arg2));
 			Info.setTableId(mSettings.getIdIndex(arg2));
 			tableItemChioceDialog(arg2, mSettings.getStatus(arg2));
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
 }
