 package com.geekytheory.miguelcatalandev.developerdays;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.MenuItem;
 import com.geekytheory.miguelcatalandev.developerdays.maps.Maps_fragmentActivity;
 import com.google.analytics.tracking.android.EasyTracker;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 
 public class Detail_activity_Main extends SherlockActivity implements
 		OnClickListener {
 
 	Bundle extras;
 	private int result;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.detail_activity_detail);
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		initialize();
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		if (item.getItemId() == android.R.id.home) {
 			onBackPressed();
 		}
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		EasyTracker.getInstance().activityStart(this);
 
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 		EasyTracker.getInstance().activityStop(this);
 	}
 
 	private void initialize() {
 		extras = getIntent().getExtras();
 		if (extras != null) {
 
 			setTitle(extras.getString("title"));
 
 			TextView name = (TextView) findViewById(R.id.detail_textview_name);
 			name.setText(extras.getString("name"));
 
 			TextView buis = (TextView) findViewById(R.id.detail_textview_buis);
 			buis.setText(extras.getString("buis"));
 
 			TextView url = (TextView) findViewById(R.id.detail_textview_url);
 			url.setText(extras.getString("url"));
 			url.setOnClickListener(this);
 
 			TextView des = (TextView) findViewById(R.id.detail_textview_des);
 			des.setText(extras.getString("des"));
 
 			TextView time = (TextView) findViewById(R.id.detail_textview_time);
 			time.setText("Horario: " + extras.getString("time"));
 
 			TextView loc = (TextView) findViewById(R.id.detail_textview_location);
 			loc.setText("Localizacin: " + extras.getString("loc"));
 
 			ImageView profile = (ImageView) findViewById(R.id.detail_imageview_profile);
 			profile.setImageResource(extras.getInt("image",
 					R.drawable.no_photo_circle));
 			ImageView map = (ImageView) findViewById(R.id.detail_imageview_location);
 			map.setOnClickListener(this);
 		}
 
 	}
 
 	@Override
 	public void onClick(View v) {
 
 		if (result == ConnectionResult.SUCCESS) {
 			switch (v.getId()) {
 			case R.id.detail_textview_url:
 				Intent intent = new Intent(Intent.ACTION_VIEW);
 				intent.setData(Uri.parse(extras.getString("url")));
 				startActivity(intent);
 				break;
 			case R.id.detail_imageview_location:
 				Intent intent_map = new Intent(getBaseContext(),
 						Maps_fragmentActivity.class);
				if (extras.getString("title").equals("Introduccin a WP")) {
 					intent_map.putExtra("type", 6);
 				} else {
 					intent_map.putExtra("type", 5);
 				}
 
 				startActivity(intent_map);
 				break;
 			}
 		} else {
 			GooglePlayServicesUtil.getErrorDialog(result,
 					Detail_activity_Main.this, 32).show();
 		}
 
 	}
 
 }
