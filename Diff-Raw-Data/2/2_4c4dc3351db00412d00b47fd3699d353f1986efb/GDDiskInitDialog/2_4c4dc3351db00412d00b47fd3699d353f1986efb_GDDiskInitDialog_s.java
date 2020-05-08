 package com.dbstar.app.alert;
 
 import com.dbstar.R;
 import com.dbstar.DbstarDVB.DbstarServiceApi;
 import com.dbstar.app.GDApplication;
 import com.dbstar.util.upgrade.RebootUtils;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class GDDiskInitDialog extends Dialog implements
 		DialogInterface.OnShowListener {
 	public static final int StateNone = 0;
 	public static final int StateStart = 1;
 	public static final int StateProgress = 2;
 	public static final int StateSuccessed = 3;
 	public static final int StateFailed = 4;
 
 	private TextView mTitleView, mMessageView;
 	private int mState = StateNone;
 	private String mData = "";
 	Handler mHandler = null;
 
 	public GDDiskInitDialog(Context context) {
 		super(context, R.style.GDAlertDialog);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.alert_dialog);
 
 		mTitleView = (TextView) findViewById(R.id.alertTitle);
 		mMessageView = (TextView) findViewById(R.id.message);
 
 		Button okButton = (Button) findViewById(R.id.buttonOK);
 		Button cancelButton = (Button) findViewById(R.id.buttonCancel);
 		okButton.setVisibility(View.GONE);
 		cancelButton.setVisibility(View.GONE);
 
 		setCancelable(false);
 		setCanceledOnTouchOutside(false);
 		setOnShowListener(this);
 	}
 
 	@Override
 	public void onShow(DialogInterface dialog) {
 		mTitleView.setText(R.string.disk_init_title);
 		updateViews(mState, mData);
 	}
 	
 	private int getStateByType (int type) {
 		int state = StateNone;
 		switch(type) {
 		case DbstarServiceApi.MOTHER_DISK_INITIALIZE_START: {
 			state = StateStart;
 			break;
 		}
 		case DbstarServiceApi.MOTHER_DISK_INITIALIZE_PROCESS: {
 			state = StateProgress;
 			break;
 		} 
 		case DbstarServiceApi.MOTHER_DISK_INITIALIZE_SUCCESS: {
 			state = StateSuccessed;
 			break;
 		}
 		case DbstarServiceApi.MOTHER_DISK_INITIALIZE_FAILED: {
 			state = StateFailed;
 			 break;
 		}
 		}
 		
 		return state;
 	}
 
 	public void updateState(int type, String data) {
 		int state = getStateByType(type);
 		mState = state;
 		mData = data;
 		if (isShowing()) {
 			updateViews(state, data);
 		}
 		
 		if (mState == StateFailed || mState == StateSuccessed) {
 			mHandler.postDelayed(new Runnable() {
 				public void run() {
 					rebootSystem();
 				}
 			}, 5000);
 		}
 	}
 	
 	void rebootSystem() {
 		RebootUtils.rebootNormal(GDApplication.getAppContext());
 	}
 	
 	private void updateViews(int state, String data) {
 		switch(state) {
 		case StateStart: {
 			mMessageView.setText(R.string.disk_init_start);
 			break;
 		}
 		case StateProgress: {
 			Context context = GDApplication.getAppContext();
 			String str = context.getResources().getString(R.string.disk_init_progress);
 			String progress = String.format(str, data);
 			mMessageView.setText(progress);
 			break;
 		}
 		case StateFailed: {
 			Context context = GDApplication.getAppContext();
 			String str = context.getResources().getString(R.string.disk_init_failed);
 			String info = String.format(str, data);
 			mMessageView.setText(info);
 			break;
 		}
 		case StateSuccessed: {
 			mMessageView.setText(R.string.disk_init_successed);
 			break;
 		}
 		}
 	}
 }
