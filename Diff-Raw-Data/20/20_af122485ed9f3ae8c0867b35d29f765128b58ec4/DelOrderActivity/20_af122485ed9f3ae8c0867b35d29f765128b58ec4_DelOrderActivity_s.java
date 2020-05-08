 package com.htb.cnk;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.htb.cnk.adapter.MyOrderAdapter;
 import com.htb.cnk.data.Info;
 import com.htb.cnk.data.MyOrder;
 import com.htb.cnk.data.OrderedDish;
 import com.htb.cnk.lib.OrderBaseActivity;
 
 public class DelOrderActivity extends OrderBaseActivity {
 	private final int CLEANALL = -1;
	private final int CLEANITEM = -2;
 	private final int UPDATE_ORDER = 1;
 	private final int DEL_ITEM_ORDER = 2;
 	private int NETWORK_ARERTDIALOG = 0;
 	private MyOrderAdapter mMyOrderAdapter;
 	private AlertDialog mNetWrorkcancel;
 	private AlertDialog.Builder mNetWrorkAlertDialog;
 	private final int UPDATE_ORDER_QUAN = 1;
 
 	@Override
 	protected void onResume() {
 		if (NETWORK_ARERTDIALOG == 1) {
 			mNetWrorkcancel.cancel();
 			NETWORK_ARERTDIALOG = 0;
 		}
 		showProgressDlg("正在获取菜品。。。");
 		getOrderThread();
 		super.onResume();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setDelViews();
 		setDelClickListener();
 		mNetWrorkAlertDialog = networkDialog();
 	}
 
 	private void setDelViews() {
 		mSubmitBtn.setVisibility(View.GONE);
 		mLeftBtn.setText(R.string.cleanAll);
 		mRefreshBtn.setVisibility(View.GONE);
 		mComment.setVisibility(View.GONE);
 	}
 
 	private void fillDelData() {
 		mMyOrderAdapter = new MyOrderAdapter(this, mMyOrder) {
 			@Override
 			public View getView(int position, View convertView, ViewGroup arg2) {
 				TextView dishName;
 				TextView dishPrice;
 				TextView dishQuantity;
 				Button delBtn;
 
 				if (convertView == null) {
 					convertView = LayoutInflater.from(DelOrderActivity.this)
 							.inflate(R.layout.item_delorder, null);
 				}
 				OrderedDish dishDetail = mMyOrder.getOrderedDish(position);
 
 				dishName = (TextView) convertView.findViewById(R.id.dishName);
 				dishPrice = (TextView) convertView.findViewById(R.id.dishPrice);
 				dishQuantity = (TextView) convertView
 						.findViewById(R.id.dishQuantity);
 				delBtn = (Button) convertView.findViewById(R.id.dishMinus);
 
 				dishName.setText(dishDetail.getName());
 				dishPrice.setText(Double.toString(dishDetail.getPrice())
 						+ " 元/份");
 				dishQuantity.setText(MyOrder.convertFloat(dishDetail
 						.getQuantity()));
 
 				delBtn.setTag(position);
 				delBtn.setText("-1");
 				delBtn.setOnClickListener(delClicked);
 
 				return convertView;
 			}
 		};
 		updateTabelInfos();
 		mMyOrderLst.setAdapter(mMyOrderAdapter);
 
 	}
 
 	private void setDelClickListener() {
 		mLeftBtn.setOnClickListener(cleanBtnClicked);
 	}
 
 	public void showProgressDlg(String msg) {
 		mpDialog.setMessage(msg);
 		mpDialog.show();
 	}
 
 	private void delDish(final int position, final float updateOrderQuan,
 			final int type) {
 		new Thread() {
 			public void run() {
 				try {
 					int ret = mMyOrder.submitDelDish(position, type);
 					if (ret < 0) {
 						delDishHandler.sendEmptyMessage(ret);
 						return;
 					}
 					mMyOrder.minus(position, updateOrderQuan);
 					delDishHandler.sendEmptyMessage(ret);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 
 	}
 
 	private void delDishAlert(final int position) {
 		String messages;
 		if (position == CLEANALL) {
 			messages = "确认退掉所有菜品";
 		} else {
 			messages = "确认退菜：" + mMyOrder.getOrderedDish(position).getName();
 		}
 		delDishDialog(position, messages);
 	}
 
 	/**
 	 * @param position
 	 * @param messages
 	 */
 	protected void delDishDialog(final int position, String messages) {
 		new AlertDialog.Builder(DelOrderActivity.this)
 				.setTitle(getResources().getString(R.string.pleaseNote))
 				.setMessage(messages)
 				.setPositiveButton(getResources().getString(R.string.ok),
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								if (position == CLEANALL) {
 									showProgressDlg("正在退掉所有菜品");
 									cleanAllThread();
 								} else {
 									showProgressDlg("正在退掉菜品");
 									if (MyOrder.convertFloat(
 											mMyOrder.getQuantity(position))
 											.indexOf(".") == -1) {
 										delDish(position, UPDATE_ORDER_QUAN,
 												UPDATE_ORDER);
 									} else {
 										delDish(position,
 												mMyOrder.getQuantity(position),
 												DEL_ITEM_ORDER);
 									}
 								}
 							}
 						})
 				.setNegativeButton(getResources().getString(R.string.cancel),
 						null).show();
 	}
 
 	Handler getOrderHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what == -2) {
 				toastText(R.string.delWarning);
 			} else if (msg.what == -1) {
 				if (NETWORK_ARERTDIALOG == 1) {
 					mNetWrorkcancel.cancel();
 				}
 				NETWORK_ARERTDIALOG = 1;
 				mNetWrorkcancel = mNetWrorkAlertDialog.show();
 			} else {
 				mMyOrder.removeServedDishes();
 				fillDelData();
 				mMyOrderAdapter.notifyDataSetChanged();
 			}
 		}
 	};
 
 	Handler delDishHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				delHanderError(msg);
 			} else {
 				fillDelData();
 				mMyOrderAdapter.notifyDataSetChanged();
 			}
 		}
 	};
 
 	Handler cleanAllHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				if (msg.what == -2) {
 					toastText(R.string.notClean);
 				} else {
 					delHanderError(msg);
 				}
 			} else {
 				mMyOrder.clear();
 				fillDelData();
 				mMyOrderAdapter.notifyDataSetChanged();
 			}
 		}
 	};
 
 	protected void getOrderThread() {
 		new Thread() {
 			public void run() {
 				try {
 					int ret = mMyOrder.getOrderFromServer(Info.getTableId());
 					getOrderHandler.sendEmptyMessage(ret);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 
 	}
 
 	protected void cleanAllThread() {
 		new Thread() {
 			public void run() {
 				try {
 					if (mMyOrder.count() == 0) {
 						cleanAllHandler.sendEmptyMessage(-2);
 						return;
 					}
 					int result = mMyOrder.submitDelDish(CLEANALL, CLEANALL);
 					if (result < 0) {
 						cleanAllHandler.sendEmptyMessage(result);
 						return;
 					}
 					cleanAllHandler.sendEmptyMessage(result);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	protected AlertDialog.Builder networkDialog() {
 		final AlertDialog.Builder networkAlertDialog = new AlertDialog.Builder(
 				DelOrderActivity.this);
 		networkAlertDialog.setCancelable(false);
 		networkAlertDialog.setTitle(getResources().getString(R.string.error));// 设置对话框标题
 		networkAlertDialog.setMessage(getResources().getString(
 				R.string.networkErrorWarning));// 设置对话框内容
 		networkAlertDialog.setPositiveButton(
 				getResources().getString(R.string.tryAgain),
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						NETWORK_ARERTDIALOG = 0;
 						showProgressDlg(getResources().getString(
 								R.string.connectServer));
 						getOrderThread();
 					}
 				});
 		networkAlertDialog.setNegativeButton(
 				getResources().getString(R.string.exit),
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						NETWORK_ARERTDIALOG = 0;
 						finish();
 					}
 				});
 
 		return networkAlertDialog;
 	}
 
 	private OnClickListener cleanBtnClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			delDishAlert(CLEANALL);
 			fillDelData();
 			mMyOrderAdapter.notifyDataSetChanged();
 		}
 	};
 
 	private OnClickListener delClicked = new OnClickListener() {
 
 		public void onClick(View v) {
 			final int position = Integer.parseInt(v.getTag().toString());
 			delDishAlert(position);
 		}
 	};
 
 	@Override
 	public void finish() {
 		mMyOrder.clear();
 		super.finish();
 	}
 
 	private void delHanderError(Message msg) {
 		String errMsg = "退菜订单失败";
 		if (isPrinterError(msg)) {
 			errMsg += ":无法连接打印机或打印机缺纸";
 		}
 		NETWORK_ARERTDIALOG = 1;
 		mNetWrorkAlertDialog.setMessage(errMsg + ","
 				+ getResources().getString(R.string.networkErrorWarning));
 		mNetWrorkcancel = mNetWrorkAlertDialog.show();
 	}
 
 }
