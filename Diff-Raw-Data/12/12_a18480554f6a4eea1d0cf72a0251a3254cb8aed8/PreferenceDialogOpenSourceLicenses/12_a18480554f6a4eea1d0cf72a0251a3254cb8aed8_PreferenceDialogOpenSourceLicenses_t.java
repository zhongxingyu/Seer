 package com.plingnote.preferences;
 
 import android.content.Context;
 import android.preference.DialogPreference;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.TextView;
 
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.plingnote.R;
 
 public class PreferenceDialogOpenSourceLicenses extends DialogPreference{
 	private String dialogeMessage;
 	
 	public PreferenceDialogOpenSourceLicenses(Context context, AttributeSet attrs) {
 		super(context, attrs);
		this.dialogeMessage = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(context);
 		setPersistent(false);
 		setDialogLayoutResource(R.layout.preference_dialog_license);
 	}
 		
 	@Override
 	public void onBindDialogView(View view){
 		TextView textView = (TextView)view.findViewById(R.id.info_textview);
 		textView.setText(dialogeMessage);
 		super.onBindDialogView(view);
 	}
 }
