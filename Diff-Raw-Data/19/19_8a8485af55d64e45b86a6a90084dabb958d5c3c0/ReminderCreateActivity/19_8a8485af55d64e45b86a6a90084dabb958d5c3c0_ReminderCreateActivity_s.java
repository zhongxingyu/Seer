 package com.aragaer.reminder;
 
 import android.annotation.SuppressLint;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.LinearLayout;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 
 public class ReminderCreateActivity extends Activity {
 	DrawView dv;
 	ColorSwitch cs;
 
 	@SuppressLint("NewApi")
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		LinearLayout ll = new LinearLayout(this);
 		ll.setGravity(Gravity.CENTER);
 
 		cs = new ColorSwitch(this);
 		cs.setId(3);
 
 		dv = new DrawView(this);
 		dv.setPaint(Bitmaps.paints[cs.getValue()]);
 		dv.setId(1);
 
 		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
 			ll.setOrientation(LinearLayout.VERTICAL);
 			cs.setOrientation(LinearLayout.HORIZONTAL);
 		} else {
 			ll.setOrientation(LinearLayout.HORIZONTAL);
 			cs.setOrientation(LinearLayout.VERTICAL);
 		}
 
 		if (savedInstanceState == null)
 			dv.reset();
 		else
 			dv.setBitmap((Bitmap) savedInstanceState.getParcelable("image"));
 
 		cs.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			public void onCheckedChanged(RadioGroup group, int checkedId) {
 				dv.setPaint(Bitmaps.paints[cs.getValue()]);
 			}
 		});
 
 		ll.addView(cs);
 		ll.addView(dv);
 		setContentView(ll);
 
 		ActionBar ab = getActionBar();
 		if (Build.VERSION.SDK_INT >= 14)
 			ab.setHomeButtonEnabled(true);
 		ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
 	}
 
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putParcelable("image", dv.getBitmap());
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			finish();
 			break;
 		case R.string.no_extra:
 			save(false);
 			break;
 		case R.string.add_extra:
 			save(true);
 			break;
 		default:
 			break;
 		}
 
 		return true;
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(Menu.NONE, R.string.no_extra, Menu.NONE, R.string.no_extra)
 				.setIcon(R.drawable.ic_cab_done_holo_dark)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 		menu.add(Menu.NONE, R.string.add_extra, Menu.NONE, R.string.add_extra)
 				.setIcon(R.drawable.navigation_forward)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 		return true;
 	}
 
 	void save(boolean extra) {
 		Bitmap res = dv.getBitmap();
 		dv.reset();
 
 		ContentValues row = new ContentValues();
 		row.put("glyph", ReminderItem.bitmap_to_bytes(res));
 		row.put("date", System.currentTimeMillis());
 		row.put("color", cs.getValue());
 		Uri result_uri = getContentResolver().insert(ReminderProvider.content_uri, row);
 		if (extra)
 			startActivity(new Intent(ReminderCreateActivity.this,
 					ReminderViewActivity.class).putExtra("reminder_id",
 					ContentUris.parseId(result_uri)));
 		finish();
 	}
 }
