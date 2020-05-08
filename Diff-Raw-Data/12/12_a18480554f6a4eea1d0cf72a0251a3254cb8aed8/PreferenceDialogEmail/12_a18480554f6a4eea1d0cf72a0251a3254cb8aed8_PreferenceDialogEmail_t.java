 /**
  * This file is part of Plingnote.
  * Copyright (C) 2012 Barnabas Sapan
  * 
  * Plingnote is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.plingnote.preferences;
 
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Build;
 import android.preference.DialogPreference;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.EditText;
 
 import com.plingnote.R;
 
 /**
  * Custom class that displays a dialogue where the
  * user can enter text. Once the user is done
  * the message gets opened up in the default email client
  * with debug info such as device make and version attached
  * under the user typed text. To be used in Preferences.
  * 
  * @author Barnabas Sapan
  */
 public class PreferenceDialogEmail extends DialogPreference {
 	private Context context;
 	private String deviceInfoString = "------------------\n"
 									+ Build.MANUFACTURER + " " + Build.MODEL + " (" + Build.BRAND + ")\n"
 									+ Build.DISPLAY + "\n"
 			 						+ "SDK: " + Build.VERSION.SDK_INT + "\n";
 	private String appVersionName = "Plingnote ";
 	private EditText editText;
 
 	
 	public PreferenceDialogEmail(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		this.context = context;
 		this.appVersionName += this.context.getString(R.string.app_version_name);
 		setPersistent(false);
		setDialogLayoutResource(R.layout.preference_dialog_feedback);
 	}
 	
 	@Override
 	public void onClick (DialogInterface dialog, int which){
 		if(which == DialogInterface.BUTTON_POSITIVE){
 			String userText = editText.getText().toString() + "\n\n";
 			Intent intent = new Intent(Intent.ACTION_SEND);
 			intent.setType("message/rfc822");
 			intent.putExtra(Intent.EXTRA_EMAIL,new String[] { "plingnote@plingnote.com" });
 			intent.putExtra(Intent.EXTRA_SUBJECT, appVersionName);
 			intent.putExtra(Intent.EXTRA_TEXT, userText + deviceInfoString);
 			context.startActivity(Intent.createChooser(intent, "Send with..."));
 		} else if (which == DialogInterface.BUTTON_NEGATIVE){
 			dialog.dismiss();
 		}
 	}
 	
 	@Override
 	public void onBindDialogView(View view){
		editText = (EditText)view.findViewById(R.id.editText_feedback);
 		super.onBindDialogView(view);
 	}
 
 
 }
