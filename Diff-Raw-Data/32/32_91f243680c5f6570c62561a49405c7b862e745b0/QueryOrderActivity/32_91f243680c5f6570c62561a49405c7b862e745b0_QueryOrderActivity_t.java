 package com.htb.cnk;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.htb.cnk.adapter.MyOrderAdapter;
 import com.htb.cnk.data.Info;
 import com.htb.cnk.data.MyOrder.OrderedDish;
 import com.htb.cnk.lib.OrderBaseActivity;
 
 public class QueryOrderActivity extends OrderBaseActivity {
 	
 	private MyOrderAdapter mMyOrderAdapter;
 	private static int ARERTDIALOG = 0;
 	private AlertDialog mNetWrorkcancel;
 	private AlertDialog.Builder mNetWrorkAlertDialog;
 	
 	
 	@Override
 	protected void onResume() {
 		if (ARERTDIALOG == 1) {
 			mNetWrorkcancel.cancel();
 			ARERTDIALOG = 0;
 		}
 		showProgressDlg("正在获取菜品。。。");
 		new Thread(new queryThread()).start();
 		super.onResume();
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setQueryViews();
 		mNetWrorkAlertDialog = networkDialog();
 	}
 
 	private void setQueryViews() {
 		mSubmitBtn.setText("上菜");
 		mLeftBtn.setVisibility(View.GONE);
 		mRefreshBtn.setVisibility(View.GONE);
 		mComment.setVisibility(View.GONE);
 		mSubmitBtn.setOnClickListener(submitClicked);
 	}
 
 	private void setAdapter() {
 		mMyOrderAdapter = new MyOrderAdapter(this, mMyOrder) {
 			@Override
 			public View getView(int position, View convertView, ViewGroup arg2) {
 				TextView dishName;
 				TextView dishPrice;
 				TextView dishQuantity;
 				TextView dishServedQuantity;
 				if (convertView == null) {
 					convertView = LayoutInflater.from(QueryOrderActivity.this)
 							.inflate(R.layout.item_queryorder, null);
 				}
 				OrderedDish dishDetail = mMyOrder.getOrderedDish(position);
 
 				dishName = (TextView) convertView.findViewById(R.id.dishName);
 				dishPrice = (TextView) convertView.findViewById(R.id.dishPrice);
 				dishServedQuantity = (TextView) convertView
 						.findViewById(R.id.dishServedQuantity);
 				dishQuantity = (TextView) convertView
 						.findViewById(R.id.dishQuantity);
 
 				dishName.setText(dishDetail.getName());
 
 				dishPrice.setText(Double.toString(dishDetail.getPrice())
 						+ " 元/份");
 				dishQuantity
 						.setText(mMyOrder.convertFloat(dishDetail.getQuantity()));
 				dishServedQuantity.setText(Integer.toString(dishDetail.getServedQuantity()));
 				if ((dishDetail.getQuantity()-dishDetail.getServedQuantity()) <= 0) {
 					int color = Color.rgb(255, 0, 0);
 					dishName.setTextColor(color);
 					dishPrice.setTextColor(color);
 					dishQuantity.setTextColor(color);
 					dishServedQuantity.setTextColor(color);
 					dishServedQuantity.setText(mMyOrder.convertFloat(dishDetail.getQuantity()));
 				} else {
 					int color = Color.rgb(0, 0, 0);
 					dishName.setTextColor(color);
 					dishPrice.setTextColor(color);
 					dishQuantity.setTextColor(color);
 					dishServedQuantity.setTextColor(color);
 				}
 				return convertView;
 			}
 		};
 		mMyOrderLst.setAdapter(mMyOrderAdapter);
 	}
 
 	Handler queryHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 			if (msg.what == -2) {
 				Toast.makeText(getApplicationContext(),
 						getResources().getString(R.string.delWarning),
 						Toast.LENGTH_SHORT).show();
 			} else if (msg.what == -1) {
 				ARERTDIALOG = 1;
 				mNetWrorkAlertDialog.setMessage("查询菜品失败，请检查连接网络重试");
 				mNetWrorkcancel = mNetWrorkAlertDialog.show();
 			} else {
 				setAdapter();
 				mMyOrderAdapter.notifyDataSetChanged();
 				updateTabelInfos();

				TextView tableName = (TextView) findViewById(R.id.tableName);
				tableName.setText("桌号/人数");
				mTableNumTxt.setText(Info.getTableName()+"/" + persons);
 			}
 			
 		}
 	};
 
 	class queryThread implements Runnable {
 		public void run() {
 			try {
 				int ret = mMyOrder.getOrderFromServer(Info.getTableId());
 				queryHandler.sendEmptyMessage(ret);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private AlertDialog.Builder networkDialog() {
 		final AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(
 				QueryOrderActivity.this);
 		mAlertDialog.setTitle("错误");// 设置对话框标题
 		mAlertDialog.setMessage("网络连接失败，请检查网络后重试");// 设置对话框内容
 		mAlertDialog.setCancelable(false);
 		mAlertDialog.setPositiveButton("重试",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int i) {
 						ARERTDIALOG = 0;
 						showProgressDlg("正在连接服务器...");
 						new Thread(new queryThread()).start();
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
 
 	private OnClickListener submitClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View arg0) {
 			Intent intent = new Intent();
 			intent.setClass(QueryOrderActivity.this, ServeOrderActivity.class);
 			startActivity(intent);
 			finish();
 		}
 
 	};
 	
 }
