 /*******************************************************************************
  * Copyright (c) 2012 Moarub Oy.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Moarub Oy - initial API and implementation
  ******************************************************************************/
 package com.moarub.sharemoar;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.Editable;
 import android.text.style.URLSpan;
 import android.text.util.Linkify;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.moarub.kipptapi.ClipCreatedListener;
 import com.moarub.kipptapi.CreateClip;
 
 public class ShareToKipptActivity extends Activity implements OnClickListener,
 		ClipCreatedListener {
 	protected String fUrlShared;
 	protected String fTitle;
 	private TextView fTitleView;
 	private ConnectivityManager fConnectivityManager;
 	private CheckBox fReadLater;
 	private String fGeneratedNoteText;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_sahare_moar);
 
 		handleIntentInit();
 		initViews();
 	}
 
 	private void initViews() {
 		TextView urlV = (TextView) findViewById(R.id.editText1);
 		urlV.setText(fUrlShared);
 
 		fTitleView = (TextView) findViewById(R.id.editText3);
 		fTitleView.setText(fTitle);
 
 		Button b = (Button) findViewById(R.id.button1);
 		b.setOnClickListener(this);
 
 		fReadLater = (CheckBox) findViewById(R.id.checkBox1);
 	}
 
 	protected void handleIntentInit() {
 		Intent i = getIntent();
 
 		if (i != null && i.getType() != null
 				&& i.getType().equalsIgnoreCase("text/plain")) {
 			Bundle extras = i.getExtras();
			String cleanAndLinkify = cleanAndLinkify(extras);
			fUrlShared = cleanAndLinkify != null ? cleanAndLinkify : null;
 			fTitle = extras.getString("android.intent.extra.SUBJECT");
 		}
		
		if(fUrlShared == null) {
			finishWithError(R.string.no_url_found_in_the_shared_text);
		}
 
 		if (isOnline()) {
 			fetchAPITokens();
 		} else {
 			finishWithError(R.string.inet_not_available);
 		}
 	}
 
 	private String cleanAndLinkify(Bundle extras) {
 		String urlCandidate = extras.getString("android.intent.extra.TEXT");
 		if(urlCandidate != null && urlCandidate.startsWith("http://")) {
 			return urlCandidate;	
 		} else {
 			Editable str = Editable.Factory.getInstance().newEditable(urlCandidate);
 			Linkify.addLinks(str, Linkify.WEB_URLS);
 			URLSpan[] urls = str.getSpans(0, str.length(), URLSpan.class);
 			if(urls == null || urls.length < 1) {
				return null;
 			}
 			URLSpan uspan = urls[0];
 			fGeneratedNoteText = urlCandidate;
 			return uspan.getURL();
 		}
 		
 	}
 
 	private void finishWithError(int resId) {
 		Toast.makeText(getApplicationContext(), resId, Toast.LENGTH_LONG)
 				.show();
 		finish();
 	}
 
 	private void fetchAPITokens() {
 		SharedPreferences preferences = PreferenceManager
 				.getDefaultSharedPreferences(getApplicationContext());
 		String apiTokStr = preferences.getString("kippt_token", "");
 
 		if (apiTokStr.equalsIgnoreCase("apitoken")
 				|| apiTokStr.equalsIgnoreCase("")) {
 			callLoginActivity();
 		}
 	}
 
 	private void callLoginActivity() {
 		Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
 		startActivityForResult(intent, 700);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_sahare_moar, menu);
 		return true;
 	}
 
 	protected void createClip() {
 		fTitle = fTitleView.getText().toString();
 		doCreateClip(fReadLater.isChecked(), false);
 	}
 
 	public void doCreateClip(boolean readLater, boolean star) {
 		CreateClip cl = new CreateClip(fUrlShared, this);
 
 		cl.addTitle(fTitle);
 		if(fGeneratedNoteText != null) {
 			cl.addNote(fGeneratedNoteText);
 		}
 		cl.setReadLater(readLater);
 		cl.setStar(star);
 
 		SharedPreferences preferences = PreferenceManager
 				.getDefaultSharedPreferences(getApplicationContext());
 		String apiTokStr = preferences.getString("kippt_token", "");
 		String apiTokUser = preferences.getString("kippt_username", "");
 
 		String[] params = { apiTokUser, apiTokStr };
 		cl.execute(params);
 	}
 
 	@Override
 	public void onClick(View v) {
 		createClip();
 	}
 
 	@Override
 	public void onClipCreated(int code) {
 		if (code == 201) {
 			Toast.makeText(getApplicationContext(), R.string.clip_created,
 					Toast.LENGTH_SHORT).show();
 		} else if (code == 401) {
 			Toast.makeText(getApplicationContext(), R.string.authentication_failed_password_changed,
 					Toast.LENGTH_SHORT).show();
 			callLoginActivity();
 		} else {
 			Toast.makeText(getApplicationContext(), R.string.error_creation + code,
 					Toast.LENGTH_LONG).show();
 		}
 		finish();
 	}
 
 	public boolean isOnline() {
 		fConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo ni = fConnectivityManager.getActiveNetworkInfo();
 		boolean result = ni != null && ni.isAvailable() && ni.isConnected();
 		return result;
 	}
 
 }
