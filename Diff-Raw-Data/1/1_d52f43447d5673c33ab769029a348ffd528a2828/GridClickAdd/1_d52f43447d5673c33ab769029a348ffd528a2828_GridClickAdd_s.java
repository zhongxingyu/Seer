 package com.htb.cnk.lib;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Handler;
 import android.os.Message;
 import android.widget.EditText;
 
 import com.htb.cnk.MenuActivity;
 import com.htb.cnk.MyOrderActivity;
 import com.htb.cnk.R;
 import com.htb.cnk.data.Info;
 import com.htb.cnk.ui.base.TableGridActivity;
 import com.htb.constant.Table;
 
 public class GridClickAdd extends GridClick {
 	private static final String TAG = "GridClickAdd";
 	public GridClickAdd(Context context) {
 		super(context);
 		resultDialog().show();
 	}
 
 	@Override
 	protected AlertDialog.Builder resultDialog() {
 		return addDialog();
 	}
 
 	private AlertDialog.Builder addDialog() {
 		final CharSequence[] additems = mContext.getResources().getStringArray(
 				R.array.normalStatus);
 		if (mItemDialog != null)
 			return mItemDialog.itemChooseFunctionDialog(additems, addListener);
 		return null;
 	}
 
 	private DialogInterface.OnClickListener addListener = new DialogInterface.OnClickListener() {
 		@Override
 		public void onClick(DialogInterface dialog, int which) {
			mPhoneOrder.clear();
 			addDialogChoiceMode(which);
 		}
 	};
 
 	void addDialogChoiceMode(int which) {
 		switch (which) {
 		case 0:
 			if (TableGridActivity.networkStatus) {
 				Info.setMode(Info.WORK_MODE_CUSTOMER);
 				Info.setNewCustomer(true);
 				setClassToActivity(MenuActivity.class);
 				TableGridActivity.instance.finish();
 			} else {
 				networkErrDlg();
 			}
 			break;
 		case 1:
 			chooseTypeToMenu();
 			break;
 		case 2:
 			if (TableGridActivity.networkStatus) {
 				copyTableDialog().show();
 			} else {
 				networkErrDlg();
 			}
 			break;
 		default:
 			break;
 		}
 	}
 
 	private Builder copyTableDialog() {
 		final EditText copyTableText = editTextListener();
 		copyTableText.addTextChangedListener(watcher(copyTableText));
 
 		DialogInterface.OnClickListener copyTableListener = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				String tableName = copyTableText.getEditableText().toString()
 						.toUpperCase();
 				if (tableName.equals("")) {
 					toastText(R.string.idAndPersonsIsNull);
 				} else if (isBoundaryLegal(tableName, Table.OPEN_TABLE_STATUS)) {
 					copyTable(mSettings.getId(tableName));
 				} else {
 					toastText(R.string.copyTIdwarning);
 				}
 			}
 		};
 
 		return mViewDialog.viewAndTitleAndButtonDialog(false, copyTableText,
 				mContext.getResources().getString(R.string.pleaseInput) + "桌号",
 				null, copyTableListener);
 	}
 
 	private void copyTable(final int srcTId) {
 		mpDialog.show();
 		new Thread() {
 			public void run() {
 				try {
 					int ret = getSettings().getOrderFromServer(srcTId);
 					copyTIdHandler.sendEmptyMessage(ret);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	Handler copyTIdHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			switch (msg.what) {
 			case -10:
 				toastText("本地数据库出错，请从网络重新更新数据库");
 				break;
 			case -2:
 				toastText(R.string.copyTIdwarning);
 				break;
 			case -1:
 				showNetworkErrDlg("复制失败，"
 						+ mContext.getResources().getString(
 								R.string.networkErrorWarning));
 				break;
 			default:
 				intent.setClass(mContext, MyOrderActivity.class);
 				Info.setMode(Info.WORK_MODE_WAITER);
 				mContext.startActivity(intent);
 				break;
 			}
 		}
 	};
 
 }
