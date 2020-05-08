 package com.logtracking.lib.internal;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.androidlogtracker.R;
 
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class DefaultReportIssueActivity extends ReportIssueActivity {
 	
 	private static final List<Integer> sHeaderMessageModeId = new ArrayList<Integer>();
 	private static final List<Integer> sMainMessageModeId = new ArrayList<Integer>();
 	
 	static {
 		sHeaderMessageModeId.add(R.string.alt_crash_header_message);
 		sHeaderMessageModeId.add(R.string.alt_issue_report_header_message);
 		sHeaderMessageModeId.add(R.string.alt_crash_header_message);
 		
 		sMainMessageModeId.add(R.string.alt_crash_message);
 		sMainMessageModeId.add(R.string.alt_issue_message);
 		sMainMessageModeId.add(R.string.alt_crash_notification);
 	}
 	
 	private OnClickListener mSendReportClickListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
             sendIssueReport();
 		}
 	};
 	
 	private OnClickListener mCloseClickListener = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			onCancel();
 		}
 	};
 
 	private EditText mMessageEditText;
 	
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContent();
 	}
 
 	/* "alt" prefix was added to avoid possible id's conflicts with application that will use this library */
 	private void setContent(){
 		setContentView(R.layout.report_issue_layout);
 		
 		Button okButton = (Button) findViewById(R.id.altYesButton);
 		okButton.setOnClickListener(mSendReportClickListener);
 		
 		Button cancelButton = (Button) findViewById(R.id.altNoButton);
 		cancelButton.setOnClickListener(mCloseClickListener);
 		
 		TextView appTextView = (TextView) findViewById(R.id.altAppNameTextView);
 		String appName = getApplicationInfo().name;
 		String libraryName = getResources().getString(R.string.alt_library_name);
 		appTextView.setText(!TextUtils.isEmpty(appName) ? appName:libraryName);
 		
 		ImageView appImageView = (ImageView) findViewById(R.id.altAppImageView);
 		appImageView.setImageResource(getApplicationInfo().logo);
 		
 		TextView headerMessageTextView = (TextView) findViewById(R.id.altHeaderMessageTextView);
 		headerMessageTextView.setText(sHeaderMessageModeId.get(getMode()));
 		
 		TextView mainMessageTextView = (TextView) findViewById(R.id.altMessageTextView);
 		mainMessageTextView.setText(sMainMessageModeId.get(getMode()));
 		
 		mMessageEditText = (EditText) findViewById(R.id.altIssueDescEditText);
 		
 		switch (getMode()) {
 		
 			case SHOW_CRASH_REPORT_DIALOG:
 				mMessageEditText.setVisibility(View.GONE);
 				break;
 	
 			case SHOW_CRASH_NOTIFICATION_DIALOG:
 				mMessageEditText.setVisibility(View.GONE);
 				cancelButton.setVisibility(View.GONE);
 				break;
 		}
 	}
 
 	
 	private void sendIssueReport(){
 		String reportMessage = mMessageEditText.getText().toString();
		if (!TextUtils.isEmpty(reportMessage)){
			onSendIssueReport(reportMessage);
			showToast(R.string.alt_thanks_for_report);
		} else {
			showToast(R.string.alt_toast_message_could_be_empty);
 		}
 	}
 	
 	private void showToast(int messageId){
 		Toast.makeText(this,messageId, Toast.LENGTH_SHORT).show();
 	}
 }
