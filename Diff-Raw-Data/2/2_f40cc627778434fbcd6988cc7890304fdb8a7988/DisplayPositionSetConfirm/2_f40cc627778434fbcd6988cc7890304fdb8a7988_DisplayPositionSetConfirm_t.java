 /*
  * Copyright (C) 2010 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.dbstar.settings.display;
 
 import android.os.Bundle;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.app.Activity;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Handler;
 import android.os.Message;
 
import com.dbstar.settings.R;

 public class DisplayPositionSetConfirm extends Activity {
 	private static final String TAG = "DisplayPositionSetConfirm";
 
 	/** If there is output mode option, use this. */
 
 	private AlertDialog DisplayPositionSetConfirmDiag = null;
 	private final static long set_delay = 15 * 1000;
 	private Handler mProgressHandler;
 	private String mMessages = "";
 	private int get_operation = 1;
 	private static final int GET_USER_OPERATION = 1;
 	private static final int GET_DEFAULT_OPERATION = 2;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		Bundle bundle = new Bundle();
 		bundle = this.getIntent().getExtras();
 		get_operation = bundle.getInt("get_operation");
 
 		if (get_operation == GET_USER_OPERATION) {
 			mMessages = getResources().getString(
 					R.string.display_position_set_confirm_dialog_noreboot);
 		} else if (get_operation == GET_DEFAULT_OPERATION) {
 			mMessages = getResources()
 					.getString(
 							R.string.display_position_set_default_confirm_dialog_noreboot);
 		}
 		showDispmodeSetMsg();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 
 	private void showDispmodeSetMsg() {
 
 		DisplayPositionSetConfirmDiag = new AlertDialog.Builder(this)
 				.setTitle(R.string.display_position_dialog_title)
 				.setMessage(mMessages)
 				.setPositiveButton(R.string.yes,
 						new DialogInterface.OnClickListener() {
 							public void onClick(
 									DialogInterface dialoginterface, int i) {
 								setResult(RESULT_OK, null);
 								finish();
 							}
 						})
 				.setNegativeButton(R.string.display_position_dialog_no,
 						new DialogInterface.OnClickListener() {
 							public void onClick(
 									DialogInterface dialoginterface, int i) {
 								setResult(RESULT_CANCELED, null);
 								finish();
 							}
 						})
 				.setOnKeyListener(new DialogInterface.OnKeyListener() {
 					@Override
 					public boolean onKey(DialogInterface dialog, int keyCode,
 							KeyEvent event) {
 						if (keyCode == KeyEvent.KEYCODE_BACK
 								&& event.getAction() == KeyEvent.ACTION_DOWN
 								&& !event.isCanceled()
 								&& DisplayPositionSetConfirmDiag.isShowing()) {
 							dialog.cancel();
 							setResult(RESULT_CANCELED, null);
 							finish();
 							return true;
 						}
 						return false;
 					}
 				}).show();
 	}
 }
