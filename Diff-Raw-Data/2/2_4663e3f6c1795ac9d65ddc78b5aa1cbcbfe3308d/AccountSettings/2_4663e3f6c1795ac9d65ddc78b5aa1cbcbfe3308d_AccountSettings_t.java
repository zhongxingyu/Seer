 /*
  * MUSTARD: Android's Client for StatusNet
  * 
  * Copyright (C) 2009-2010 macno.org, Michele Azzolari
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
  * for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  */
 
 package org.mustard.android.activity;
 
 import org.mustard.android.MustardApplication;
 import org.mustard.android.MustardDbAdapter;
 import org.mustard.android.R;
 import org.mustard.android.provider.StatusNet;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class AccountSettings extends Activity implements View.OnClickListener {
 	
 	private Button mPasswordButton;
 	private Button mAvatarButton;
 	private EditText mPasswordText;
 	
 	private StatusNet mStatusNet = null;
 	private MustardDbAdapter dbAdapter ;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.account_settings);
 		dbAdapter = new MustardDbAdapter(this);
 		MustardApplication _ma = (MustardApplication) getApplication();
 		mStatusNet = _ma.checkAccount(dbAdapter);
 		setTitle();
 		doPrepareLayout();
		
		// TODO set account avatar
 	}
 
 	private void setTitle() {
 		if (mStatusNet != null) {
 			setTitle(getString(R.string.app_name)  + 
 					" - " + mStatusNet.getMUsername() + "@" + mStatusNet.getURL().getHost() + 
 					" - " +  getString(R.string.menu_settings));
 		}
 	}
 	
 	private void doPrepareLayout() {
 	
 		mPasswordText = (EditText)findViewById(R.id.edit_new_password);
 		mPasswordButton = (Button) findViewById(R.id.btn_update_password);
 		mPasswordButton.setOnClickListener(this);
 		
 		mAvatarButton = (Button) findViewById(R.id.btn_update_avatar);
 		mAvatarButton.setOnClickListener(this);
 	}
 		
 	
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.btn_update_password:
 			doPasswordUpdate();
 			break;
 			
 		case R.id.btn_update_avatar:
 			Avatar.actionAvatar(this);
 			break;
 		}
 		
 	}
 	
 	private void doPasswordUpdate() {
 		
 		String mPassword = mPasswordText.getText().toString();
 		if (mPassword == null || "".equals(mPassword))
 			return;
 		
 		
 		dbAdapter.open();
 		dbAdapter.updateAccount(mPassword);
 		dbAdapter.close();
 		
 		new AlertDialog.Builder(this)
 		.setTitle(getString(R.string.warning))
         .setMessage(getString(R.string.restart_take_effect))
         .setNeutralButton(getString(R.string.close), null).show();
 		return;
 	}
     
 	
 	public static void actionAccountSettings(Context context) {
 		Intent intent = new Intent(context, AccountSettings.class);
 	    context.startActivity(intent);
 	}
 }
