 package com.aragaer.reminder;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.graphics.ColorMatrixColorFilter;
 import android.os.Bundle;
 import android.text.format.DateFormat;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class ReminderViewActivity extends Activity {
 	ReminderItem memo = null;
 	ImageView glyph_view;
 	EditText comment;
 
 	public void onCreate(Bundle savedInstanceState) {
 		long id;
 		super.onCreate(savedInstanceState);
 		Resources r = getResources();
 
 		if (savedInstanceState != null)
 			id = savedInstanceState.getLong("reminder_id");
 		else
 			id = getIntent().getLongExtra("reminder_id", 0);
 		Cursor c = getContentResolver().query(
 				ContentUris.withAppendedId(ReminderProvider.content_uri, id),
 				null, null, null, null);
 		c.moveToFirst();
 		memo = ReminderProvider.getItem(c);
 		c.close();
 
 		setContentView(R.layout.view);
 		glyph_view = (ImageView) findViewById(R.id.glyph);
 		glyph_view.setImageBitmap(memo.getGlyph(r.getDimensionPixelSize(R.dimen.view_glyph_size)));
 		glyph_view.setColorFilter(new ColorMatrixColorFilter(Bitmaps.filters[memo.color]));
 		comment = (EditText) findViewById(R.id.comment);
 		comment.setBackgroundDrawable(Bitmaps.border(
 				r.getDimensionPixelSize(R.dimen.notification_glyph_margin),
 				Color.LTGRAY));
 		comment.setText(memo.text);
 		((TextView) findViewById(R.id.date)).setText(DateFormat.getDateFormat(this).format(memo.when));
 		((TextView) findViewById(R.id.time)).setText(DateFormat.getTimeFormat(this).format(memo.when));
 
 		ActionBar ab = getActionBar();
 		ab.setDisplayShowHomeEnabled(true);
 		ab.setDisplayHomeAsUpEnabled(true);
 	}
 
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putLong("reminder_id", memo._id);
 	}
 
 	public void onPause() {
 		String new_text = comment.getText().toString();
 		if (!new_text.equals(memo.text)) {
 			memo.text = new_text;
 			ContentValues row = new ContentValues();
 			if (new_text.length() > 0)
 				row.put("comment", new_text);
 			else
 				row.putNull("comment");
 			getContentResolver().update(
 					ContentUris.withAppendedId(ReminderProvider.content_uri,
 							memo._id), row, null, null);
 		}
 		super.onPause();
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(Menu.NONE, R.string.delete, Menu.NONE, R.string.delete)
 				.setIcon(android.R.drawable.ic_menu_delete)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			startActivity(new Intent(this, ReminderListActivity.class)
 					.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
 			finish();
 			break;
 		case R.string.delete:
 			getContentResolver().delete(
 					ContentUris.withAppendedId(ReminderProvider.content_uri,
 							memo._id), null, null);
 			finish();
 			break;
 		}
 		return true;
 	}
 }
 
