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
 
 import org.mustard.android.R;
import org.mustard.android.service.OAuthKeysService;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 
 
 public class Login extends Activity {
 	
 	protected static final int DIALOG_VERIFING_ID=0;
 	protected static final int DIALOG_AUTHENTICATING_ID=1;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		OAuthKeysService.schedule(this);
 		setContentView(R.layout.account_create);
 		doPrepareButtons();
 	}
 
 	private void doPrepareButtons() {
 		
 		Button mIdenticaButton = (Button) findViewById(R.id.btn_identica);
 		mIdenticaButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				doStartIdentica();
 			}
 		});
 		
 		Button mTwitterButton = (Button) findViewById(R.id.btn_twitter);
 		mTwitterButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				doStartTwitter();
 			}
 		});
 		
 		Button mStatusNetButton = (Button) findViewById(R.id.btn_statusnet);
 		mStatusNetButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				doStartStatusNet();
 			}
 		});
 		
 	}
 	
 	private void doStartIdentica() {
 		OAuthLogin.actionHandleLogin(this, "https://identi.ca");
 		finish();
 	}
 	
 	private void doStartTwitter() {
 		OAuthLogin.actionHandleLogin(this, "twitter.com");
 		finish();
 	}
     
 	private void doStartStatusNet() {
 		//BasicAuthLogin.actionHandleLogin(this);
 		OAuthLogin.actionHandleLogin(this,"");
 		finish();
 	}
 	
 	public static void actionHandleLogin(Context context) {
 		Intent intent = new Intent(context, Login.class);
 	    context.startActivity(intent);
 	}
 }
