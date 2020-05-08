 package com.htb.cnk;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnSystemUiVisibilityChangeListener;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.LinearLayout;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 import com.htb.cnk.adapter.TableAdapter;
 import com.htb.cnk.data.Info;
 import com.htb.cnk.lib.ScrollLayout;
 import com.htb.cnk.ui.base.TableBaseActivity;
 
 public class TableActivity extends TableBaseActivity {
 
 	static final String TAG = "TablesActivity";
 
 	private final String IMAGE_ITEM = "imageItem";
 	private final String ITEM_TEXT = "ItemText";
 
 	private final static int EXTERN_PAGE_NUM = 1; //除了楼层以外还有几个页面
 	protected Button mBackBtn;
 	protected Button mUpdateBtn;
 	protected Button mStatisticsBtn;
 	protected Button mManageBtn;
 	protected GridView mGridView;
 
 	private ScrollLayout curPage;
 	private LinearLayout layoutBottom;
 	ArrayList<HashMap<String, Object>> mTableItem = new ArrayList<HashMap<String, Object>>();
 	private TextView imgCur;
 	private boolean flag = false;
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		if (NETWORK_ARERTDIALOG == 1) {
 			mNetWrorkcancel.cancel();
 			NETWORK_ARERTDIALOG = 0;
 		}
 		showProgressDlg(getResources().getString(R.string.getStatus));
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Info.setMode(Info.WORK_MODE_WAITER);
 		setContentView(R.layout.table_activity);
 		findViews();
 		mpDialog.show();
 		setClickListeners();
 		setHandler();
 		mTableInfo = new TableAdapter(mTableItem, mNotification, mSettings);
 	}
 
 	private void setGridView() {
 		imgCur.setTextColor(0xFF4D2412);
 		imgCur.setTextSize(22);
 		layoutBottom.addView(imgCur);
 		setCurPage(0);
 		curPage.getLayoutParams().height = this.getWindowManager()
 				.getDefaultDisplay().getHeight() * 4 / 5;
 		setTableInfos();
 		curPage.setPageListener(new ScrollLayout.PageListener() {
 			@Override
 			public void page(int page) {
 				if (page < 0) {
 					return;
 				}
 				Log.d("TAG", "currentPage:" + page);
 				setCurPage(page);
 				mGridView = (GridView) curPage.getChildAt(page);
 				updateGridViewAdapter(page);
 			}
 		});
 	}
 
 	/**
 	 * 更新当前页码
 	 */
 	public void setCurPage(int page) {
 		switch (page) {
 		case 0:
 			imgCur.setText("全部");
 			break;
 
 		default:
 			imgCur.setText(page - EXTERN_PAGE_NUM + 1 + "楼");
 			break;
 		}
 		currentPage = page;
 	}
 
 	private void setHandler() {
 		setTableHandler(tableHandler);
 		mNotificationHandler = notificationHandler;
 		setRingtoneHandler(ringtoneHandler);
 		mTotalPriceTableHandler = totalPriceTableHandler;
 		mChangeTIdHandler = changeTIdHandler;
 		mCopyTIdHandler = copyTIdHandler;
 		mCombineTIdHandler = combineTIdHandler;
 		mNotificationTypeHandler = notificationTypeHandler;
 	}
 
 	private void findViews() {
 		mBackBtn = (Button) findViewById(R.id.back);
 		mUpdateBtn = (Button) findViewById(R.id.checkOutTable);
 		mStatisticsBtn = (Button) findViewById(R.id.logout);
 		mManageBtn = (Button) findViewById(R.id.management);
 		curPage = (ScrollLayout) findViewById(R.id.scr);
 		layoutBottom = (LinearLayout) findViewById(R.id.layout_scr_bottom);
 		
 		imgCur = new TextView(TableActivity.this);
 	}
 
 	protected void setClickListeners() {
 		mBackBtn.setOnClickListener(backClicked);
 		mUpdateBtn.setOnClickListener(checkOutClicked);
 		mStatisticsBtn.setOnClickListener(logoutClicked);
 		mManageBtn.setOnClickListener(manageClicked);
 		mNetWrorkAlertDialog = networkDialog();
 	}
 
 	Handler changeTIdHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what == -2) {
 				toastText(R.string.changeTIdWarning);
 			} else if (msg.what == -1) {
 				netWorkDialogShow("转台失败，"
 						+ getResources()
 								.getString(R.string.networkErrorWarning));
 			} else if (isPrinterError(msg)) {
 				toastText(R.string.changeSucc);
 			} else {
 				binderStart();
 				toastText(R.string.changeSucc);
 			}
 		}
 	};
 
 	Handler copyTIdHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what == -2) {
 				toastText(R.string.copyTIdwarning);
 			} else if (msg.what == -1) {
 				netWorkDialogShow("复制失败，"
 						+ getResources()
 								.getString(R.string.networkErrorWarning));
 			} else {
 				intent.setClass(TableActivity.this, MyOrderActivity.class);
 				Info.setMode(Info.WORK_MODE_WAITER);
 				TableActivity.this.startActivity(intent);
 			}
 		}
 	};
 
 	Handler combineTIdHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what == -2) {
 				toastText(R.string.checkOutWarning);
 			} else if (msg.what == -1) {
 				netWorkDialogShow("合并出错，"
 						+ getResources()
 								.getString(R.string.networkErrorWarning));
 			} else if (isPrinterError(msg)) {
 				toastText(R.string.combineError);
 			} else {
 				binderStart();
 				toastText(R.string.combineSucc);
 			}
 		}
 	};
 
 	Handler ringtoneHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what > 0) {
 				mRingtone.play();
 			}
 		}
 	};
 
 	Handler notificationHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what < 0) {
 				toastText(R.string.notificationWarning);
 			} else {
 				binderStart();
 			}
 		}
 	};
 
 	Handler notificationTypeHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what < 0) {
 				toastText(R.string.notificationTypeWarning);
 			}
 		}
 	};
 
 	Handler totalPriceTableHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				netWorkDialogShow("统计失败，"
 						+ getResources()
 								.getString(R.string.networkErrorWarning));
 			} else {
 				mTotalPrice = (double) msg.what;
 				if (mTotalPrice <= 0) {
 					toastText(R.string.dishNull);
 				} else {
 					sendPriceToCheckout();
 				}
 			}
 			mpDialog.cancel();
 		}
 	};
 
 	/**
 	 * 
 	 */
 	private void sendPriceToCheckout() {
 		Intent checkOutIntent = new Intent();
 		Bundle bundle = new Bundle();
 		bundle.putDouble("price", mTotalPrice);
 		bundle.putIntegerArrayList("tableId",
 				(ArrayList<Integer>) selectedTable);
 		bundle.putStringArrayList("tableName", (ArrayList<String>) tableName);
 		checkOutIntent.putExtras(bundle);
 		checkOutIntent.setClass(TableActivity.this, CheckOutActivity.class);
 		TableActivity.this.startActivity(checkOutIntent);
 	}
 
 	Handler tableHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				if (NETWORK_ARERTDIALOG == 1) {
 					mNetWrorkcancel.cancel();
 				}
 				netWorkDialogShow(getResources().getString(
 						R.string.networkErrorWarning));
 			} else {
 				switch (msg.what) {
 				case UPDATE_TABLE_INFOS:
 					if (!flag) {
 						setGridView();
 					}
 					updateGrid(currentPage);
 					flag = true;
 					if (getSettings().hasPendedPhoneOrder()) {
 						ringtoneHandler.sendEmptyMessage(1);
 					}
 					break;
 				case DISABLE_GRIDVIEW:
 					if (mGridView != null)
 						mGridView.setOnItemClickListener(null);
 					break;
 				default:
 					Log.e(TAG, "unhandled case:"+msg.what+(new Exception()).getStackTrace()[2].getLineNumber());
 					break;
 				}
 			}
 		}
 	};
 
 	private void updateGrid(int page) {
 		mGridView = (GridView) curPage.getChildAt(page);
 		updateGridViewAdapter(page);
 	}
 
 	protected void setTableInfos() {
 		int pageCount = getSettings().getFloorNum() + EXTERN_PAGE_NUM;
 
 		mImageItems = new SimpleAdapter(TableActivity.this, mTableItem,
 				R.layout.table_item,
 				new String[] { IMAGE_ITEM, ITEM_TEXT }, new int[] {
 						R.id.ItemImage, R.id.ItemText }) {
 		};
 		
 		for (int floorNum = 0; floorNum < pageCount; floorNum++) {
 			mGridView = new GridView(TableActivity.this);
 			mGridView.setAdapter(mImageItems);
 			mImageItems.notifyDataSetChanged();
			mGridView.setNumColumns(4);
 			mGridView.setHorizontalSpacing(10);
 			mGridView.setVisibility(View.VISIBLE);
 			mGridView.setOnItemClickListener(tableItemClickListener);
 			curPage.addView(mGridView);
 		}
 	}
 
 	/**
 	 * @param page
 	 */
 	private void updateGridViewAdapter(int page) {
 		mGridView.setOnItemClickListener(tableItemClickListener);
 		switch (page) {
 		case 0:
 			mTableInfo.filterTables(page, TableAdapter.FILTER_NONE);
 			break;
 
 		default:
 			mTableInfo.filterTables(page-EXTERN_PAGE_NUM, TableAdapter.FILTER_FLOOR);
 			break;
 		}
 		
 		mImageItems.notifyDataSetChanged();
 	}
 }
