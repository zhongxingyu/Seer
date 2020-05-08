 package com.htb.cnk.ui.base;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.os.Handler;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 import com.htb.cnk.ManageActivity;
 import com.htb.cnk.R;
 import com.htb.cnk.data.Info;
 
 public class TableBaseActivity extends TableGridDeskActivity {
 
 	static final String TAG = "TablesActivity";
 	protected int NETWORK_ARERTDIALOG = 0;
 	protected double mIncome;
 	protected double mChange;
 	protected double mTotalPrice;
 	protected AlertDialog.Builder mChangeDialog;
 	protected AlertDialog mNetWrorkcancel;
 	protected AlertDialog.Builder mNetWrorkAlertDialog;
 	protected Handler mTotalPriceTableHandler;
 	protected Handler mCheckOutHandler;
 
 	protected AlertDialog.Builder networkDialog() {
 		return mTitleAndMessageDialog.networkDialog(networkPositiveListener,
 				networkNegativeListener);
 	}
 
 	DialogInterface.OnClickListener networkPositiveListener = new DialogInterface.OnClickListener() {
 		@Override
 		public void onClick(DialogInterface dialog, int i) {
 			dialog.cancel();
 			NETWORK_ARERTDIALOG = 0;
 			showProgressDlg(getResources().getString(R.string.getStatus));
 			binderStart();
 		}
 	};
 	DialogInterface.OnClickListener networkNegativeListener = new DialogInterface.OnClickListener() {
 		@Override
 		public void onClick(DialogInterface dialog, int i) {
 			NETWORK_ARERTDIALOG = 0;
 			dialog.cancel();
 			finish();
 		}
 	};
 
 	protected void netWorkDialogShow(String messages) {
 		NETWORK_ARERTDIALOG = 1;
 		mNetWrorkcancel = mNetWrorkAlertDialog.setMessage(messages).show();
 	}
 
 	protected Builder listTableNameDialog(final int type) {
 		final List<Integer> tableId = new ArrayList<Integer>();
 		final List<String> tableNameStr = new ArrayList<String>();
 		ArrayList<HashMap<String, Object>> checkOut = getSettings()
 				.getTableOpen();
 		if (checkOut.size() <= 0) {
 			return mTitleAndMessageDialog.messageDialog(false, getResources()
 					.getString(R.string.tableNotOpen), getResources()
 					.getString(R.string.ok), null, null, null);
 		}
 		setNameAndId(tableId, tableNameStr, checkOut);
 		final boolean selected[] = new boolean[getSettings().tableSeetingsSize()];
 
 		DialogInterface.OnClickListener listPositiveListener = new DialogInterface.OnClickListener() {
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
 				if (type == CHECKOUT_LIST) {
 					showProgressDlg(getResources().getString(
 							R.string.statisticsAmount));
 					getTotalPriceTable();
 				}
 
 			}
 		};
 
 		DialogInterface.OnMultiChoiceClickListener listListener = new DialogInterface.OnMultiChoiceClickListener() {
 			@Override
 			public void onClick(DialogInterface dialogInterface, int which,
 					boolean isChecked) {
 				selected[which] = isChecked;
 			}
 		};
 
 		return mMultiChoiceItemsDialog.titleDialog(false, getResources()
 				.getString(R.string.chooseTableId), (String[]) tableNameStr
 				.toArray(new String[0]), null, listListener, getResources()
 				.getString(R.string.ok), listPositiveListener, getResources()
 				.getString(R.string.cancel), null);
 	}
 
 	private void setNameAndId(final List<Integer> tableId,
 			final List<String> tableNameStr,
 			ArrayList<HashMap<String, Object>> checkOut) {
 		for (HashMap<String, Object> item : checkOut) {
 			tableNameStr.add(item.get("name").toString());
 			tableId.add(item.get("id").hashCode());
 		}
 	}
 
 	void getTotalPriceTable() {
 		new Thread() {
 			public void run() {
 				try {
 					double ret = getSettings().getTotalPriceTable(
 							selectedTable, tableName);
					mTotalPrice = ret;
 					mTotalPriceTableHandler.sendEmptyMessage((int) ret);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	public void checkOut(final List<Integer> destIId,
 			final List<String> tableName, final Double receivable,
 			final Double income, final Double change) {
 		new Thread() {
 			public void run() {
 				try {
 					int ret = getSettings().checkOut(destIId, tableName,
 							receivable, income, change);
 					mCheckOutHandler.sendEmptyMessage(ret);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	protected OnClickListener checkOutClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			listTableNameDialog(CHECKOUT_LIST).show();
 		}
 	};
 
 	protected OnClickListener manageClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			setClassToActivity(ManageActivity.class);
 		}
 	};
 
 	protected OnClickListener backClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			finish();
 		}
 	};
 
 	protected OnClickListener logoutClicked = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			mTitleAndMessageDialog.titleAndMessageDialog(false,
 					getResources().getString(R.string.notice),
 					getResources().getString(R.string.islogOut),
 					getResources().getString(R.string.ok),
 					logoutPositiveListener,
 					getResources().getString(R.string.cancel), null).show();
 		}
 	};
 
 	DialogInterface.OnClickListener logoutPositiveListener = new DialogInterface.OnClickListener() {
 		@Override
 		public void onClick(DialogInterface dialogInterface, int which) {
 			Info.setMode(Info.WORK_MODE_CUSTOMER);
 			Info.setTableId(-1);
 			finish();
 		}
 	};
 
 }
