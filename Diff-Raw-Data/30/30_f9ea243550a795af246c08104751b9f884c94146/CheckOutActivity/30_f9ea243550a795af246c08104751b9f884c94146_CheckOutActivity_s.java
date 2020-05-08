 package com.htb.cnk;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.text.method.ScrollingMovementMethod;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.htb.cnk.adapter.MyOrderAdapter;
 import com.htb.cnk.data.MyOrder;
 import com.htb.cnk.data.Setting;
 import com.htb.cnk.data.TableSetting;
 import com.htb.cnk.dialog.CashContext;
 import com.htb.cnk.dialog.CashDialog;
 import com.htb.cnk.ui.base.BaseActivity;
 
 public class CheckOutActivity extends BaseActivity {
 	protected Button mBackBtn;
 	protected Button mSubmitBtn;
 	protected Button mLeftBtn;
 	protected Button mRefreshBtn;
 	protected Button mComment;
 	protected TextView mTableNumTxt;
 	protected TextView mDishCountTxt;
 	protected TextView mTotalPriceTxt;
 	protected TextView mReceivableText;
 	protected TextView mCheckOutPrinte;
 	protected TextView mChangeText;
 	protected EditText mIncomeEdit;
 	protected ListView mMyOrderLst;
 	protected MyOrderAdapter mMyOrderAdapter;
 	protected MyOrder mMyOrder;
 	private Intent checkOutIntent;
 	protected double mIncome;
 	protected double mChange;
 	protected double mTotalPrice;
 	protected List<String> tableName = new ArrayList<String>();
 	protected List<Integer> selectedTable = new ArrayList<Integer>();
 	protected TableSetting mSettings;
 	
 	private final String TAG = "CheckOutActivity";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.checkout_activity);
 		checkOutIntent = this.getIntent();
 		mSettings = new TableSetting(CheckOutActivity.this);
 		mMyOrder = new MyOrder(CheckOutActivity.this);
 		checkfindViews();
 		setCheckOutView();
 	}
 
 	private void checkfindViews() {
 		mBackBtn = (Button) findViewById(R.id.back_btn);
 		mSubmitBtn = (Button) findViewById(R.id.submit);
 		mTableNumTxt = (TextView) findViewById(R.id.tableNum);
 		mDishCountTxt = (TextView) findViewById(R.id.dishCount);
 		mTotalPriceTxt = (TextView) findViewById(R.id.totalPrice);
 		mMyOrderLst = (ListView) findViewById(R.id.myOrderList);
 		mLeftBtn = (Button) findViewById(R.id.left_btn);
 		mRefreshBtn = (Button) findViewById(R.id.refresh);
 		mComment = (Button) findViewById(R.id.comment);
 		mReceivableText = (TextView) findViewById(R.id.receivableQuan);
 		mIncomeEdit = (EditText) findViewById(R.id.incomeEdit);
 		mChangeText = (TextView) findViewById(R.id.changeQuan);
 		mCheckOutPrinte = (TextView) findViewById(R.id.checkOutPrinter);
 	}
 
 	private void setCheckOutView() {
 		mSubmitBtn.setText("结账");
 		mLeftBtn.setText("明细");
 		mRefreshBtn.setVisibility(View.GONE);
 		mComment.setVisibility(View.GONE);
 		mBackBtn.setOnClickListener(backBtnClicked);
 		mSubmitBtn.setOnClickListener(submitClicked);
 		mLeftBtn.setOnClickListener(showDetailToggleClicked);
 		mIncomeEdit.addTextChangedListener(watcher);
 		mCheckOutPrinte
 				.setMovementMethod(ScrollingMovementMethod.getInstance());
 		getTableValue();
 		updateTabelInfos();
 	}
 
 	private void updateTabelInfos() {
 		DisplayMetrics metrics = new DisplayMetrics();
 
 		getWindowManager().getDefaultDisplay().getMetrics(metrics);
 		
 		mCheckOutPrinte.setText(mSettings.checkOutJson(metrics.widthPixels));
 		mReceivableText.setText(String.valueOf(MyOrder.convertFloat((float)mTotalPrice)));
 		if (mSettings.getAdvPayment() > 0) {
 			mRefreshBtn.setText("预付:" + mSettings.getAdvPayment());
 			mRefreshBtn.setVisibility(View.VISIBLE);
 		}
 	}
 
 	private void getTableValue() {
 		Bundle bundle = checkOutIntent.getExtras();
 		mTotalPrice = bundle.getDouble("price");
 		selectedTable = bundle.getIntegerArrayList("tableId");
 		tableName = bundle.getStringArrayList("tableName");
 	}
 
 	/**
 	 * 
 	 */
 	private void cashRebate() {
 		SharedPreferences sharedPre = getSharedPreferences("cashInfo",
 				Context.MODE_PRIVATE);
 		String cashType = sharedPre.getString("cashType", "");
 		String cashRebate = sharedPre.getString("cashRebate", "");
 		Log.d(TAG, cashType);
 		CashContext cashContext = new CashContext(cashType, cashRebate);
 		mTotalPrice = cashContext.getResult(mTotalPrice);
 	}
 
 	private OnClickListener backBtnClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			mMyOrder.clear();
 			finish();
 		}
 	};
 
 	private OnClickListener leftBtnClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 
 			CashDialog cashDialog = new CashDialog(CheckOutActivity.this);
 			cashDialog.show();
 		}
 	};
 
 	private OnClickListener submitClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View arg0) {
 			checkOut(selectedTable, tableName, mTotalPrice, mIncome, mChange);
 			mSubmitBtn.setEnabled(false);
 			mSubmitBtn.setTextColor(Color.GRAY);
 		}
 
 	};
 	
 	private OnClickListener showDetailToggleClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View arg0) {
 			mSettings.setCheckoutShowDetail(!mSettings.getCheckoutShowDetail());
 			if (mSettings.getCheckoutShowDetail()) {
 				mLeftBtn.setText("明细");
 			} else {
 				mLeftBtn.setText("合并");
 			}
 		}
 
 	};
 
 	TextWatcher watcher = new TextWatcher() {
 
 		String tempStr;
 
 		@Override
 		public void onTextChanged(CharSequence s, int start, int before,
 				int count) {
 			tempStr = s.toString();
 			if (tempStr.indexOf("0") == 0) {
 				tempStr = tempStr.substring(1, tempStr.length());
 				mIncomeEdit.setText(tempStr);
 			}
 			if (tempStr.length() > 0) {
 				mIncome = Double.valueOf(tempStr).doubleValue();
 				mChange = mIncome + mSettings.getAdvPayment() - mTotalPrice;
 				mChangeText.setText(String.valueOf(MyOrder.convertFloat((float)mChange)));
 			}
 		}
 
 		@Override
 		public void afterTextChanged(Editable arg0) {
 
 		}
 
 		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count,
 				int after) {
 		}
 
 	};
 
 	public void checkOut(final List<Integer> destIId,
 			final List<String> tableName, final Double receivable,
 			final Double income, final Double change) {
 		new Thread() {
 			public void run() {
 				try {
 					int ret = mSettings.checkOut(destIId, tableName,
 							receivable, income, change);
 					checkOutHandler.sendEmptyMessage(ret);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 	
 	Handler checkOutHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			
 			if (msg.what == -2) {
 				toastText(R.string.checkOutWarning);
 			} else if (isPrinterError(msg)) {
 				toastText("无法连接打印机或打印机缺纸");
 			} else if (msg.what < 0) {
 				toastText("收银出错，请检查连接网络重试");
 			} else {
 				if (Setting.enabledCleanTableAfterCheckout()) {
 					cleanTableThread(selectedTable);
 				}else{
 					toastText(R.string.checkOutSucc);
 					finish();
 				}
 			}
 		}
 	};
 	
 	protected void cleanTableThread(final List<Integer> tableId) {
 		new Thread() {
 			public void run() {
 				try {
 					int ret = mSettings.cleanTalble(tableId);
 					mTableHandler.sendEmptyMessage(ret);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 	
 	Handler mTableHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				toastText("清台不成功");
 			}else{
 				toastText(R.string.checkOutSucc);
 				finish();
 			}
 		}
 	};
 	
 	Handler notificationTypeHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				toastText(R.string.notificationTypeWarning);
 			}
 		}
 	};
 
 }
