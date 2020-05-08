 package com.htb.cnk;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.widget.Toast;
 
 import com.htb.cnk.data.Info;
 import com.htb.constant.ErrorNum;
 
 public class TableActivity extends TableClickActivity {
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		handler();
 	}
 
 	private void handler() {
 		mNotificationHandler = notificationHandler;
 		mTableHandler = tableHandler;
 		mRingtoneHandler = ringtoneHandler;
 		mTotalPriceTableHandler = totalPriceTableHandler;
 		mChangeTIdHandler = changeTIdHandler;
 		mCopyTIdHandler = copyTIdHandler;
 		mCheckOutHandler = checkOutHandler;
 	}
 
 	Handler tableHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			mpDialog.cancel();
 
 			if (msg.what < 0) {
 				if (ARERTDIALOG == 1) {
 					mNetWrorkcancel.cancel();
 				}
 				ARERTDIALOG = 1;
 				mNetWrorkcancel = mNetWrorkAlertDialog.show();
 			} else {
 				switch (msg.what) {
 				case UPDATE_TABLE_INFOS:
 					setTableInfos();
 					if (mSettings.hasPendedPhoneOrder()) {
 						ringtoneHandler.sendEmptyMessage(1);
 					}
 					break;
 				case DISABLE_GRIDVIEW:
 					gridview.setOnItemClickListener(null);
 					break;
 				default:
 					break;
 				}
 			}
 		}
 	};
 
 	Handler totalPriceTableHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what < 0) {
 				ARERTDIALOG = 1;
 				mNetWrorkAlertDialog.setMessage("统计失败，请检查连接网络重试");
 				mNetWrorkcancel = mNetWrorkAlertDialog.show();
 			} else {
 				mTotalPrice = (double) msg.what;
				mChangeDialog = checkOutSubmitDialog();
 				mChangeDialog.show();
 			}
 			mpDialog.cancel();
 		}
 	};
 
 	Handler changeTIdHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what == -2) {
 				toastText(R.string.changeTIdWarning);
 			} else if (msg.what == -1) {
 				ARERTDIALOG = 1;
 				mNetWrorkAlertDialog.setMessage("转台失败，请检查连接网络重试");
 				mNetWrorkcancel = mNetWrorkAlertDialog.show();
 			} else if (msg.what == ErrorNum.PRINTER_ERR_CONNECT_TIMEOUT
 					|| msg.what == ErrorNum.PRINTER_ERR_NO_PAPER) {
 				String errMsg = "退菜订单失败";
 				Toast.makeText(getApplicationContext(), errMsg,
 						Toast.LENGTH_SHORT).show();
 			} else {
 				binderStart();
 				toastText(R.string.changeTId);
 			}
 		}
 	};
 
 	Handler copyTIdHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what == -2) {
 				toastText(R.string.copyTIdwarning);
 			} else if (msg.what == -1) {
 				ARERTDIALOG = 1;
 				mNetWrorkAlertDialog.setMessage("复制失败，请检查连接网络重试");
 				mNetWrorkcancel = mNetWrorkAlertDialog.show();
 			} else {
 				intent.setClass(TableActivity.this, MyOrderActivity.class);
 				Info.setMode(Info.WORK_MODE_WAITER);
 				TableActivity.this.startActivity(intent);
 			}
 		}
 	};
 
 	Handler checkOutHandler = new Handler() {
 		public void handleMessage(Message msg) {
 			if (msg.what == -2) {
 				toastText(R.string.checkOutWarning);
 			} else if (msg.what == -1) {
 				ARERTDIALOG = 1;
 				mNetWrorkAlertDialog.setMessage("收银出错，请检查连接网络重试");
 				mNetWrorkcancel = mNetWrorkAlertDialog.show();
 			} else {
 				binderStart();
 				toastText(R.string.checkOutSucc);
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
 			mpDialog.cancel();
 			if (msg.what < 0) {
 				// Todo network failure warning
 			} else {
 				binderStart();
 			}
 		}
 	};
 
 }
