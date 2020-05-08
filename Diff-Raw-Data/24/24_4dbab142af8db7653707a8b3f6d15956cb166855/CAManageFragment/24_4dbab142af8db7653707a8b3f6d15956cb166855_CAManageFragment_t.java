 package com.dbstar.app.settings.smartcard;
 
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 
 import com.dbstar.R;
 import com.dbstar.DbstarDVB.DbstarServiceApi;
 import com.dbstar.app.alert.GDAlertDialog;
 import com.dbstar.app.base.FragmentObserver;
 
 public class CAManageFragment extends GDSmartcardFragment {
 
 	private static final String TAG = "CAManageFragment";
 
 	private static final int DLG_CA_ALERT = 0;
 
 	Button mImportCAButton, mExportCAButton;
 	GDAlertDialog mAlertDlg = null;
 	String mCAMessage = null;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.ca_management_view, container, false);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		initializeView();
 	}
 
 	void importCA() {
 		if (mSmartcardEngine == null)
 			return;
 
 		mSmartcardEngine.manageCA(this, DbstarServiceApi.CMD_DRM_ENTITLEINFO_INPUT);
 	}
 
 	void exportCA() {
 		if (mSmartcardEngine == null)
 			return;
 
 		mSmartcardEngine.manageCA(this, DbstarServiceApi.CMD_DRM_ENTITLEINFO_OUTPUT);
 	}
 
 	// Receive data at this point
 	public void updateData(FragmentObserver observer, int type, Object key,
 			Object data) {
 
 		Log.d(TAG, " ============== manage CA message === ");
 		if (observer != this || data == null)
 			return;
 
 		int requestType = (Integer) key;
 		mCAMessage = (String) data;
 		
 		Log.d(TAG, "====== ca message ====== " + mCAMessage);
 
 		showDialog(DLG_CA_ALERT);
 	}
 
 	void initializeView() {
 		mImportCAButton = (Button) mActivity.findViewById(R.id.importCAButton);
 		mExportCAButton = (Button) mActivity.findViewById(R.id.exportCAButton);
 
 		mImportCAButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				importCA();
 			}
 		});
 
 		mExportCAButton.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				exportCA();
 			}
 		});
 	}
 
 	public Dialog onCreateDialog(int dialogId) {
 		Dialog dlg = null;
 		if (dialogId == DLG_CA_ALERT) {
 			if (mAlertDlg == null) {
 				mAlertDlg = new GDAlertDialog(mActivity, DLG_CA_ALERT);
 				mAlertDlg.setOnShowListener(mOnShowListener);
 			}
 
 			dlg = mAlertDlg;
 		}
 		
 		return dlg;
 	}
 	
 	DialogInterface.OnShowListener mOnShowListener = new DialogInterface.OnShowListener() {
 
 		@Override
 		public void onShow(DialogInterface dialog) {
			mAlertDlg.showSingleButton();
			mAlertDlg.setTitle(R.string.alert_title);
 			mAlertDlg.setMessage(getMessageByCode(mCAMessage));
 		}
 		
 	};
 
 	private int getMessageByCode(String code) {
 		int resId = -1;
 		if (code.equals(DbstarServiceApi.CA_NO_ENTITLE)) {
 			return R.string.ca_message_no_entitle;
 		} else if (code.equals(DbstarServiceApi.CA_NO_DEVICE)) {
 			return R.string.ca_message_no_device;
 		} else if (code.equals(DbstarServiceApi.CA_NOT_ENOUGH_SPACE)) {
 			return R.string.ca_message_no_space;
 		} else if (code.equals(DbstarServiceApi.CA_ENTITLE_OUTPUT_FINISH)) {
 			return R.string.ca_message_export_finished;
 		} else if (code.equals(DbstarServiceApi.CA_ENTITLE_INPUT_INTERRUPT)) {
 			return R.string.ca_message_import_failed;
 		} else if (code.equals(DbstarServiceApi.CA_ENTITLE_INPUT_FINISH)) {
 			return R.string.ca_message_import_finished;
 		}
 
 		return resId;
 	}
 }
