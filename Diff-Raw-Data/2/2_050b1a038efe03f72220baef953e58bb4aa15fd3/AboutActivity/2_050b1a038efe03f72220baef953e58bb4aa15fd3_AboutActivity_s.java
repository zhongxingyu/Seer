 package com.isawabird;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.TextView;
 
 public class AboutActivity extends Activity {
 
 	TextView appNameTextView;
 	TextView versionTextView;
 	TextView developedTextView;
 	TextView namesTextView;
 	TextView contactTextView;
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_about);
 		getActionBar().hide();
 
 		appNameTextView = (TextView) findViewById(R.id.aboutTextView_title);
 		appNameTextView.setTypeface(Utils.getTangerineTypeface(this));
 		versionTextView = (TextView) findViewById(R.id.aboutTextView_version);
 		versionTextView.setTypeface(Utils.getOpenSansLightTypeface(this));
 		developedTextView = (TextView) findViewById(R.id.aboutTextView_developed);
 		developedTextView.setTypeface(Utils.getOpenSansLightTypeface(this));
 		namesTextView = (TextView) findViewById(R.id.aboutTextView_names);
 		namesTextView.setTypeface(Utils.getOpenSansLightTypeface(this));
 		contactTextView = (TextView) findViewById(R.id.aboutTextView_contact);
 		contactTextView.setTypeface(Utils.getOpenSansLightTypeface(this));
 
 		namesTextView.setText("Srihari Kulkarni\nJerry Mannel\nPradeep S Bhat\nChethan Kumar SN");
 
		contactTextView.setText("Contact us at birdr@dhatu.com");
 	}
 }
