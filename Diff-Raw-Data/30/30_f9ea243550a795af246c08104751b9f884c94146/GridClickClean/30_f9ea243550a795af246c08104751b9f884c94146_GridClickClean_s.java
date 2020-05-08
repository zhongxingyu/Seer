 package com.htb.cnk.lib;
 
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Handler;
 import android.os.Message;
 import android.text.InputType;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 
 import com.htb.cnk.DelOrderActivity;
 import com.htb.cnk.QueryOrderActivity;
 import com.htb.cnk.R;
 import com.htb.cnk.data.Info;
 import com.htb.cnk.data.Setting;
 import com.htb.cnk.ui.base.TableGridActivity;
 import com.htb.constant.Server;
 import com.htb.constant.Table;
 
 public class GridClickClean extends GridClick {
 	public final static String TAG = "GridClickClean";
 	private EditText tableIdEdit;
 	private EditText personsEdit;
 
 	public GridClickClean(Context context) {
 		super(context);
 		resultDialog().show();
 	}
 
 	@Override
 	protected Builder resultDialog() {
 		return cleanDialog();
 	}
 
 	private AlertDialog.Builder cleanDialog() {
 		final CharSequence[] cleanitems = mContext.getResources()
 				.getStringArray(R.array.openStatus);
 		return mItemDialog.itemChooseFunctionDialog(cleanitems, cleanListener);
 	}
 
 	private DialogInterface.OnClickListener cleanListener = new DialogInterface.OnClickListener() {
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
 			cleanChioceMode(which);
 		}
 	};
 	
 	private void cleanChioceMode(int which) {
 		switch (which) {
 		case 0:
 			if (TableGridActivity.networkStatus) {
				cleanTableDialog().show();
 			} else {
 				networkErrDlg();
 			}
 			break;
 		case 1:
 			if (TableGridActivity.networkStatus) {
 				changeOrCombineDialog(CHANGE_DIALOG).show();
 			} else {
 				networkErrDlg();
 			}
 			break;
 		case 2:
 			if (TableGridActivity.networkStatus) {
 				changeOrCombineDialog(COMBINE_DIALOG).show();
 			} else {
 				networkErrDlg();
 			}
 			break;
 		case 3:
 			if (TableGridActivity.networkStatus) {
 				showAdvPaymentDialog();
 			} else {
 				networkErrDlg();
 			}         
 			break;
 		case 4:
 			setClassToActivity(DelOrderActivity.class);
 			break;
 		case 5:
 			chooseTypeToMenu();
 			break;
 		case 6:
 			setClassToActivity(QueryOrderActivity.class);
 			break;
 		default:
 			break;
 		}
 	}
 
 	private AlertDialog.Builder cleanTableDialog() {
 		return mTitleAndMessageDialog.messageDialog(false, mContext
 				.getResources().getString(R.string.isCleanTable), mContext
 				.getResources().getString(R.string.yes), cleanTableListener,
 				mContext.getResources().getString(R.string.no), null);
 	}
 
 	private Builder changeOrCombineDialog(final int type) {
 		final AlertDialog.Builder changeTableAlertDialog;
 		View layout = getDialogLayout(R.layout.change_dialog,
 				R.id.change_dialog);
 		personsEdit = (EditText) layout.findViewById(R.id.personsEdit);
 		if (Setting.enabledPersons()) {
 			tableIdEdit = (EditText) layout.findViewById(R.id.tableIdEdit);
 			changeTableAlertDialog = mViewDialog.viewDialog(false, layout);
 		} else {
 			tableIdEdit = editTextListener();
 			changeTableAlertDialog = mViewDialog.viewDialog(false, tableIdEdit);
 		}
 		tableIdEdit.addTextChangedListener(watcher(tableIdEdit));
 		personsEdit.addTextChangedListener(watcher(personsEdit));
 		if (type == CHANGE_DIALOG) {
 			changeTableAlertDialog.setTitle(mContext.getResources().getString(
 					R.string.pleaseInput)
 					+ "转入桌号");
 		} else if (type == COMBINE_DIALOG) {
 			changeTableAlertDialog.setTitle(mContext.getResources().getString(
 					R.string.pleaseInput)
 					+ "并入桌号");
 		}
 
 		DialogInterface.OnClickListener changeTablePositiveListener = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				String changePersons;
 				String tableName = tableIdEdit.getText().toString()
 						.toUpperCase();
 				if (Setting.enabledPersons()) {
 					changePersons = personsEdit.getText().toString();
 				} else {
 					changePersons = "0";
 				}
 				if (type == CHANGE_DIALOG) {
 					judgeChangeTable(changePersons, tableName);
 				} else if (type == COMBINE_DIALOG) {
 					judgeCombineTable(changePersons, tableName);
 				}
 			}
 		};
 
 		changeTableAlertDialog.setPositiveButton(mContext.getResources()
 				.getString(R.string.ok), changeTablePositiveListener);
 		changeTableAlertDialog.setNegativeButton(mContext.getResources()
 				.getString(R.string.cancel), null);
 		return changeTableAlertDialog;
 	}
 
 	private void showAdvPaymentDialog() {
 		new Thread() {
 			public void run() {
 				String response = Http.get(Server.ADVPAYMENT, "TID=" + Info.getTableId());
 				Message msg = new Message();
 				int ret = -1;
 				if (null != response && !"".equals(response)) {
 					try {
 						Float.valueOf(response);
 						ret = 0;
 						msg.obj = response;
 					} catch (NumberFormatException e) {
 						e.printStackTrace();
 					}
 				} else {
 					Log.d(TAG, "response==null");
 				}
 				
 				msg.what = ret;
 				getAdvPaymentHandler.sendMessage(msg);
 			}
 		}.start();
 	}
 	
 	private Builder advPaymentDialog(String payment) {
 		final AlertDialog.Builder changeTableAlertDialog;
 		tableIdEdit = editTextListener();
 		changeTableAlertDialog = mViewDialog.viewDialog(false, tableIdEdit);
 		tableIdEdit.setText(payment);
 		tableIdEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
 		changeTableAlertDialog.setTitle(mContext.getResources().getString(
 				R.string.pleaseInput)
 				+ "预付金额");
 
 		DialogInterface.OnClickListener advPaymentPositiveListener = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				String payment = tableIdEdit.getText().toString();
 				try {
 					updateAdvPayment(Float.valueOf(payment));
 				} catch (NumberFormatException e) {
 					e.printStackTrace();
 					toastText(R.string.advPaymentIllegal);
 				}
 			}
 		};
 
 		changeTableAlertDialog.setPositiveButton(mContext.getResources()
 				.getString(R.string.ok), advPaymentPositiveListener);
 		changeTableAlertDialog.setNegativeButton(mContext.getResources()
 				.getString(R.string.cancel), null);
 		return changeTableAlertDialog;
 	}
 	
 	private DialogInterface.OnClickListener cleanTableListener = new DialogInterface.OnClickListener() {
 		@Override
 		public void onClick(DialogInterface dialog, int i) {
 			showProgressDlg(mContext.getResources().getString(
 					R.string.cleanTableNow));
 			selectedTable.clear();
 			selectedTable.add(Info.getTableId());
 			cleanTableThread(selectedTable);
 		}
 	};
 
 	private void judgeChangeTable(String changePersons, String tableName)
 			throws NumberFormatException {
 		if (equalNameAndPersons(changePersons, tableName)) {
 			toastText(R.string.idAndPersonsIsNull);
 			return;
 		}
 
 		if (isBoundaryLegal(tableName, Table.NORMAL_TABLE_STAUTS)) {
 			changeTable(getSettings().getId(tableName),
 					Integer.parseInt(changePersons));
 		} else {
 			toastText(R.string.changeTIdWarning);
 		}
 	}
 
 	private void judgeCombineTable(String changePersons, String tableName)
 			throws NumberFormatException {
 		if (equalNameAndPersons(changePersons, tableName)) {
 			toastText(R.string.idAndPersonsIsNull);
 			return;
 		}
 
 		if (isBoundaryLegal(tableName, Table.OPEN_TABLE_STATUS)) {
 			combineTable(getSettings().getId(tableName),
 					Integer.parseInt(changePersons));
 		} else {
 			toastText(R.string.combineTIdWarning);
 		}
 	}
 
 	private void cleanTableThread(final List<Integer> tableId) {
 		new Thread() {
 			public void run() {
 				try {
 					Message msg = new Message();
 					int ret = getSettings().cleanTalble(tableId);
 					if (ret < 0) {
 						tableHandler.sendEmptyMessage(ret);
 						return;
 					}
 					if (getNotifiycations() < 0)
 						return;
 					if (getTableStatusFromServer() < 0)
 						return;
 					msg.what = UPDATE_TABLE_INFOS;
 					tableHandler.sendMessage(msg);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	private void changeTable(final int destTId, final int persons) {
 		mpDialog.setMessage("正在转台");
 		mpDialog.show();
 		new Thread() {
 			public void run() {
 				try {
 					int ret = getSettings().changeTable(Info.getTableId(),
 							destTId, getSettings().getName(Info.getTableId()),
 							getSettings().getName(destTId), persons);
 					changeTIdHandler.sendEmptyMessage(ret);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	private void updateAdvPayment(final float payment) {
 		mpDialog.setMessage("正在提交预付信息");
 		mpDialog.show();
 		new Thread() {
 			public void run() {
 				try {
 					int ret = getSettings().updateAdvPayment(Info.getTableId(),
 							payment);
 					updateAdvPaymentHandler.sendEmptyMessage(ret);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 	private void combineTable(final int destTId, final int persons) {
 		mpDialog.setMessage("正在并台");
 		mpDialog.show();
 		new Thread() {
 			public void run() {
 				try {
 					int ret = getSettings().combineTable(Info.getTableId(),
 							destTId, mSettings.getName(Info.getTableId()),
 							mSettings.getName(destTId), persons);
 					combineTIdHandler.sendEmptyMessage(ret);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	private Handler changeTIdHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			switch (msg.what) {
 			case -10:
 				toastText("本地数据库出错，请从网络重新更新数据库");
 				break;
 			case -2:
 				toastText(R.string.changeTIdWarning);
 				break;
 			case -1:
 				showNetworkErrDlg("转台失败，"
 						+ mContext.getResources().getString(
 								R.string.networkErrorWarning));
 				break;
 			default:
 				if (!isPrinterError(msg)) {
 					binderStart();
 				}
 				toastText(R.string.changeSucc);
 				break;
 			}
 		}
 	};
 
 	private Handler updateAdvPaymentHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				toastText(R.string.updateAdvPaymentFailed);
 			}
 		}
 	};
 	
 	private Handler getAdvPaymentHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				toastText(R.string.getAdvPaymentFailed);
 			} else {
 				advPaymentDialog((String)msg.obj).show();
 			}
 		}
 	};
 	
 	private Handler combineTIdHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			switch (msg.what) {
 			case -10:
 				toastText("本地数据库出错，请从网络重新更新数据库");
 				break;
 			case -2:
 				toastText(R.string.checkOutWarning);
 				break;
 			case -1:
 				showNetworkErrDlg("合并出错，"
 						+ mContext.getResources().getString(
 								R.string.networkErrorWarning));
 				break;
 			default:
 				if (isPrinterError(msg)) {
 					toastText(R.string.combineError);
 				} else {
 					binderStart();
 					toastText(R.string.combineSucc);
 				}
 				break;
 			}
 		}
 	};
 
 }
