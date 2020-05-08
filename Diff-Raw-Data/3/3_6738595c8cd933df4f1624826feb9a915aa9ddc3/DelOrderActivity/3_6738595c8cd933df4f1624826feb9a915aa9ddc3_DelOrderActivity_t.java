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
 import com.htb.cnk.data.MyOrder.OrderedDish;
 import com.htb.cnk.lib.OrderBaseActivity;
 
 public class DelOrderActivity extends OrderBaseActivity {
 	private final int CLEANALL = -1;
 	private static int ARERTDIALOG = 0;
 	private MyOrderAdapter mMyOrderAdapter;
 	private AlertDialog mNetWrorkcancel;
 	private AlertDialog.Builder mNetWrorkAlertDialog;
 	
 	@Override
 	protected void onResume() {
 		if (ARERTDIALOG == 1) {
 			mNetWrorkcancel.cancel();
 			ARERTDIALOG = 0;
 		}
 		showProgressDlg("正在获取菜品。。。");
 		new Thread(new getOrderThread()).start();
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
 				dishQuantity
 						.setText(Integer.toString(dishDetail.getQuantity()));
 
 				delBtn.setTag(position);
 				delBtn.setText("-1");
 				delBtn.setOnClickListener(delClicked);
 
 				return convertView;
 			}
 		};
 		mTableNumTxt.setText(Info.getTableName());
 		mDishCountTxt.setText(Integer.toString(mMyOrder.totalQuantity())
 				+ " 道菜");
 		mTotalPriceTxt
 				.setText(Double.toString(mMyOrder.getTotalPrice()) + " 元");
 		mMyOrderLst.setAdapter(mMyOrderAdapter);
 
 	}
 
 	private void setDelClickListener() {
 		mLeftBtn.setOnClickListener(cleanBtnClicked);
 	}
 
 	public void showProgressDlg(String msg) {
 		mpDialog.setMessage(msg);
 		mpDialog.show();
 	}
 
 	private void delDish(final int position) {
 		new Thread() {
 			public void run() {
 				try {
 					Message msg = new Message();
 					int ret = mMyOrder.submitDelDish(position,1);
 					if (ret < 0) {
 						delDishHandler.sendEmptyMessage(ret);
 						return;
 					}
 					mMyOrder.minus(position, 1);
 					msg.what = ret;
 					delDishHandler.sendMessage(msg);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 
 	}
 
 	private void delDishAlert(final int position) {
 		String messages ;
 		if(position == CLEANALL){
 			messages = "确认删除所有菜品";
 		}else {
 			messages = "确认删除" + mMyOrder.getOrderedDish(position).getName();
 		}
 		new AlertDialog.Builder(DelOrderActivity.this)
 				.setTitle("请注意")
 				.setMessage(
 						messages)
 				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						if (position == CLEANALL) {
 							showProgressDlg("正在删除所有菜品");
 							new Thread(new cleanAllThread()).start();
 						} else {
 							showProgressDlg("正在删除菜品");
 							delDish(position);
 						}
 					}
 				})
 				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 
 					}
 				}).show();
 
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
 
 	Handler getOrderHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what == -2) {
 				Toast.makeText(getApplicationContext(),
 						getResources().getString(R.string.delWarning),
 						Toast.LENGTH_SHORT).show();
 			} else if (msg.what == -1) {
 				ARERTDIALOG = 1;
 				mNetWrorkcancel = mNetWrorkAlertDialog.show();
 			} else {
				mMyOrder.setNullServing();
 				fillDelData();
 				mMyOrderAdapter.notifyDataSetChanged();
 			}
 		}
 	};
 
 	Handler delDishHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				ARERTDIALOG = 1;
 				mNetWrorkAlertDialog.setMessage("删除菜品失败，请检查连接网络重试");
 				mNetWrorkcancel = mNetWrorkAlertDialog.show();
 			} else {
 				fillDelData();
 				mMyOrderAdapter.notifyDataSetChanged();
 			}
 		}
 	};
 
 	Handler cleanAllHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what == -2) {
 				Toast.makeText(getApplicationContext(),
 						getResources().getString(R.string.cleanAllWarning),
 						Toast.LENGTH_SHORT).show();
 			} else if (msg.what == -1) {
 				ARERTDIALOG = 1;
 				mNetWrorkAlertDialog.setMessage("删除菜品失败，请检查连接网络重试");
 				mNetWrorkcancel = mNetWrorkAlertDialog.show();
 			} else {
 				mMyOrder.clear();
 				fillDelData();
 				mMyOrderAdapter.notifyDataSetChanged();
 			}
 		}
 	};
 
 	class getOrderThread implements Runnable {
 		public void run() {
 			try {
 				int ret = mMyOrder.getOrderFromServer(Info.getTableId());
 				getOrderHandler.sendEmptyMessage(ret);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	class cleanAllThread implements Runnable {
 		public void run() {
 			try {
 				if(mMyOrder.count() == 0){
 					cleanAllHandler.sendEmptyMessage(-2);
 					return;
 				}
 				int result = mMyOrder.submitDelDish(-1,0);
 //				int ret = mSettings.cleanTalble(Info.getTableId());
 				if (result < 0) {
 					cleanAllHandler.sendEmptyMessage(-1);
 					return;
 				}
 				cleanAllHandler.sendEmptyMessage(result);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private AlertDialog.Builder networkDialog() {
 		final AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(
 				DelOrderActivity.this);
 		mAlertDialog.setTitle("错误");// 设置对话框标题
 		mAlertDialog.setMessage("网络连接失败，请检查网络后重试");// 设置对话框内容
 		mAlertDialog.setCancelable(false);
 		mAlertDialog.setPositiveButton("重试",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						ARERTDIALOG = 0;
 						showProgressDlg("正在连接服务器...");
 						new Thread(new getOrderThread()).start();
 					}
 				});
 		mAlertDialog.setNegativeButton("退出",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						finish();
 						ARERTDIALOG = 0;
 					}
 				});
 
 		return mAlertDialog;
 	}
 
 	@Override
 	public void finish() {
 		mMyOrder.clear();
 		super.finish();
 	}
 	
 }
