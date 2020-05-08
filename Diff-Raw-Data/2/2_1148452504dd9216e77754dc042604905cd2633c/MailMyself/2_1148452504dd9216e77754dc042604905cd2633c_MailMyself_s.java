 package com.pfalabs;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class MailMyself extends MailMyselfBase implements OnClickListener,
 		OnFocusChangeListener {
 
 	private Button send;
 	private EditText sbj;
 	private EditText msg;
 
 	private boolean isDirty = false;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		//
 		sbj = (EditText) findViewById(R.id.subject_in);
 		sbj.setOnFocusChangeListener(this);
 		sbj.setText(R.string.sbj);
 		//
 		msg = (EditText) findViewById(R.id.text_in);
 		//
 		send = (Button) findViewById(R.id.send);
 		send.setOnClickListener(this);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		boolean showSubject = getSubjectVisibility();
		findViewById(R.id.subject_wrap).setVisibility(
 				showSubject ? View.VISIBLE : View.GONE);
 	}
 
 	private boolean getSubjectVisibility() {
 		SharedPreferences preferences = PreferenceManager
 				.getDefaultSharedPreferences(this);
 		return preferences.getBoolean("SHOW_SUBJECT_VARIABLE", true);
 	}
 
 	@Override
 	public void onFocusChange(View v, boolean hasFocus) {
 		if (hasFocus && !isDirty) {
 			// Log.i("MailMyself", "Got first click on subject");
 			isDirty = true;
 			sbj.setText(R.string.sbj_sel);
 			// sbj.setTextColor(android.R.color.primary_text_dark);
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v == send) {
 			// Log.i("MailMyself", "Got 'send'click!");
 
 			String subject = "";
 			if (sbj.getText() != null) {
 				subject = sbj.getText().toString();
 			}
 			String body = "";
 			if (msg.getText() != null) {
 				body = msg.getText().toString();
 			}
 			//
 			sendMail(subject, body, null, false);
 
 		} else {
 			// Log.i("MailMyself", "Got unknown click on " + v);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.settings:
 			startActivity(new Intent(this, MMPreferencesActivity.class));
 			return true;
 		}
 		return false;
 	}
 
 }
