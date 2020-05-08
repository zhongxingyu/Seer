 package com.android.cb;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import com.android.cb.source.CBDishesScanner;
 import com.android.cb.source.CBOrderFactory;
 import com.android.cb.source.CBResource;
 import com.android.cb.source.CBSettings;
 import com.android.cb.source.CBValidityChecker;
 import com.android.cb.support.CBMenuEngine;
 import com.android.cb.support.CBMenuItemsSet;
 import com.android.cb.support.CBOrder;
 import com.android.cb.support.CBOrdersSet;
 import com.android.cb.view.CBBitmapFactory;
 import com.android.cb.view.CBDialogButton;
 import com.android.cb.view.ConfirmDialog;
 import com.android.cb.view.LaunchingDialog;
 import com.android.cb.view.OrderPrepareDialog;
 import com.android.cb.view.OrdersListDialog;
 
 import android.app.Activity;
 import android.content.Intent;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 
 public class CBActivity extends Activity implements LaunchingDialog.Callback {
 
 	private CBDialogButton mButtonNewOrder;
 	private CBDialogButton mButtonOrdersList;
 	private CBDialogButton mButtonQuit;
 
 	private LaunchingDialog mLaunchingDialog = null;
 
 	private boolean mIsInitDone = false;
 	private boolean mIsValidDevice = false;
 	private Intent mGridViewActivityIntent = null;
 
 	CBOrdersSet mOrdersSet = null;
 
 	public CBActivity() {
 		super();
 
 		CBSettings.load();
 	}
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
 
 		mIsValidDevice = CBValidityChecker.isValid(CBActivity.this);
 
 		mGridViewActivityIntent = new Intent();
 		mGridViewActivityIntent.setClass(CBActivity.this, GridViewActivity.class);
 
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_manage);
 
 		InitAsyncTask initTask = new InitAsyncTask();
 		initTask.execute((Object)null);
 
 		mButtonNewOrder = (CBDialogButton) this.findViewById(R.id.button_newOrder);
 		mButtonNewOrder.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				if (mIsValidDevice == false) {
 					showValidateCheckingFailedDialog();
 					return;
 				}
 
 				showOrderPrepareDialog();
 			}
 		});
 
 		mButtonOrdersList = (CBDialogButton) this.findViewById(R.id.button_ordersList);
 		mButtonOrdersList.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				if (mIsValidDevice == false) {
 					showValidateCheckingFailedDialog();
 					return;
 				}
 
 				showOrderedListDialog();
 			}
 		});
 
 		mButtonQuit = (CBDialogButton) this.findViewById(R.id.button_quit);
 		mButtonQuit.setOnClickListener(new Button.OnClickListener() {
 			public void onClick(View v) {
 				CBSettings.save();
 				finish();
 			}
 		});
     }
 
 	private void openGridViewActivity() {
 		if (mGridViewActivityIntent != null)
 			CBActivity.this.startActivity(mGridViewActivityIntent);
     }
 
 	@SuppressWarnings("unused")
 	private void openGridViewActivity(String orderRecordPath) {
 		if (orderRecordPath == null) {
 			openGridViewActivity();
 			return;
 		}
 
 		CBOrder order = CBOrderFactory.loadOrder(orderRecordPath, CBResource.menuEngine.getMenuSet());
 
 		openGridViewActivity(order);
     }
 
 	private void openGridViewActivity(CBOrder order) {
 		if (order == null) {
 			openGridViewActivity();
 			return;
 		}
 
 		CBResource.menuEngine.setOrder(order);
 
 		CBActivity.this.startActivity(mGridViewActivityIntent);
     }
 
 	private void showOrderPrepareDialog() {
 		OrderPrepareDialog dialog = new OrderPrepareDialog(this);
 
 		ArrayList<String> locationList = CBSettings.getOrderLocationList();
 		if (locationList == null) {
 			locationList = new ArrayList<String>();
 			for (int i = 0; i < 15; ++i)
 				locationList.add("testing location " + i);
 		}
 
 		dialog.setCallback(new OrderPrepareDialog.Callback() {
 
 			public void onSubmit() {
 				openGridViewActivity();
 			}
 
 			public void onSelectedItemChanged(String item) {
 				CBOrder order = CBResource.menuEngine.getOrder();
				if (order != null) {
 					order.setLocation(item);
				}
 			}
 
 			public void onCancel() {
 
 			}
 		});
 
 		dialog.setContentList(locationList);

		CBOrder order = CBOrderFactory.newOrder();
		CBResource.menuEngine.setOrder(order);

		order.setLocation(locationList.get(0));

 		dialog.setSelectedItem(0);
 		dialog.show();
 	}
 
 	public void loadOrders(Date date) {
 		mOrdersSet = CBOrderFactory.loadOrdersByDate(date, CBResource.menuEngine.getMenuSet());
 	}
 
 	private void showOrderedListDialog() {
 		loadOrders(new Date());
 
 		final OrdersListDialog dialog = new OrdersListDialog(this);
 		dialog.setOrdersSet(mOrdersSet);
 
 		dialog.setCallback(new OrdersListDialog.Callback() {
 			public void onOrdersListRefresh() {
 				loadOrders(new Date());
 				dialog.setOrdersSet(mOrdersSet);
 			}
 
 			public boolean onOrderClickedInList(CBOrder order) {
 				showOpenOrderFromListConfirmDialog(order, dialog);
 				return false;
 			}
 		});
 
 		dialog.show();
 	}
 
 	private void showOpenOrderFromListConfirmDialog(final CBOrder order, final OrdersListDialog dialog) {
 		if (order == null)
 			return;
 
 		String confirmMessage = this.getResources().getString(R.string.managing_warning_open_order_confirm_message1);
 		SimpleDateFormat formatDate = new SimpleDateFormat(this.getResources().getString(R.string.managing_warning_open_order_confirm_time_form));
 		confirmMessage += formatDate.format(order.getSubmitedTime());
 		confirmMessage += this.getResources().getString(R.string.managing_warning_open_order_confirm_message2);
 		confirmMessage += order.getLocation();
 		confirmMessage += this.getResources().getString(R.string.managing_warning_open_order_confirm_message3);
 
 		ConfirmDialog confirmDialog = new ConfirmDialog(this);
 		confirmDialog.setTitle(R.string.confirm_dialog_title_warning);
 		confirmDialog.setMessage(confirmMessage);
 		confirmDialog.setCancelButtonText(this.getResources().getString(R.string.confirm_dialog_cancel));
 
 		confirmDialog.setCallback(new ConfirmDialog.Callback() {
 			public void onConfirm() {
 				if (dialog != null)
 					dialog.dismiss();
 
 				openGridViewActivity(order);
 			}
 
 			public void onCancel() {
 
 			}
 		});
 
 		confirmDialog.show();
 	}
 
 	private void showValidateCheckingFailedDialog() {
 		ConfirmDialog dialog = new ConfirmDialog(this);
 		dialog.setTitle(R.string.confirm_dialog_title_warning);
 		dialog.setMessage(this.getResources().getString(R.string.managing_warning_validate_failed));
 		dialog.setCancelButtonText(this.getResources().getString(R.string.confirm_dialog_exit));
 		dialog.setCallback(new ConfirmDialog.Callback() {
 			public void onConfirm() {
 
 			}
 
 			public void onCancel() {
 				CBActivity.this.finish();
 			}
 		});
 
 		dialog.show();
 	}
 
 	private void showLaunchingDialog() {
 		if (mLaunchingDialog == null)
 			mLaunchingDialog = new LaunchingDialog(this);
 		mLaunchingDialog.setCallback(this);
 		mLaunchingDialog.show();
 		mLaunchingDialog.start();
 	}
 
 	private class InitAsyncTask extends AsyncTask<Object, Integer, Boolean> {
 
 		@Override
 		protected Boolean doInBackground(Object... params) {
 			initMenuEngine();
 			return Boolean.TRUE;
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 
 			mIsInitDone = true;
 			mLaunchingDialog.launchingDone();
 
 			super.onPostExecute(result);
 		}
 
 		@Override
 		protected void onPreExecute() {
 			showLaunchingDialog();
 			super.onPreExecute();
 		}
 
 	} // InitAsyncTask
 
 	public boolean onLaunchingTick() {
 		return !mIsInitDone;
 	}
 
 	private static String sLaunchingText = "";
 	public String getCurrentLaunchingText() {
 		String text = this.getResources().getString(R.string.launching_text);
 
 		sLaunchingText += ".";
 		if (sLaunchingText.length() > 3)
 			sLaunchingText = ".";
 
 		return text + sLaunchingText;
 	}
 
 	private CBMenuEngine initMenuEngine() {
 
 		CBDishesScanner scanner = new CBDishesScanner(CBSettings.getStringValue(CBSettings.CB_SETTINGS_SOURCE_DIR_DISHES));
 		CBMenuItemsSet set = scanner.scan();
 
 		CBBitmapFactory.setMenuItemsSetIcons(set, this);
		CBResource.menuEngine = new CBMenuEngine();
		CBResource.menuEngine.setMenuSet(set);
 
		return CBResource.menuEngine;
 	}
 
 //	private void testDB() {
 //		File dbDir = getDir("db", Context.MODE_PRIVATE);
 //		dbDir.mkdirs();
 //		File dbFile = new File(dbDir, "cbdb.sqllite");
 //		Log.d("-DB-", "DB created on " + dbFile.getAbsolutePath());
 //		CBDB db = new CBDB(dbFile.getAbsolutePath());
 //
 ////		db.addMenuItemsToDB(mMenuEngine.getMenuSet());
 //
 //		CBMenuItemsSet set = db.loadMenuItemsSet();
 //		for (int i = 0; i < set.count(); ++i) {
 //			Log.d("-DB-", "item loaded: " + set.get(i).getDish().getPicture());
 //		}
 //
 //		db.close();
 //	}
 
 }
