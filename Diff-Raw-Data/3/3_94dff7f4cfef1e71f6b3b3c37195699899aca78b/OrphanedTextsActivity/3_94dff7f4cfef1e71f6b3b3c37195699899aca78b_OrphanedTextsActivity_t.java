 package com.michalmazur.orphanedtexts;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract.PhoneLookup;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.app.AlertDialog.Builder;
 
 public class OrphanedTextsActivity extends Activity {
 
 	String uriString;
 	Uri uri;
 	private String output;
 	ArrayList<Orphan> orphans;
 
 	public OrphanedTextsActivity() {
 		super();
 
 		uriString = "content://sms/raw";
 		uri = Uri.parse(uriString);
 		output = "";
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		RawSmsReader reader = new RawSmsReader(this.getApplicationContext());
 		// RawSmsReader reader = new RawSmsReader();
 
 		orphans = reader.getOrphans();
 		setContentView(R.layout.main);
 		output = new CsvConverter().convert(orphans);
 		displayOrphanList();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.email:
 			email();
			return true;
 		case R.id.delete_all:
 			deleteAll();
			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void displayOrphanList() {
 		ListView lv = (ListView) findViewById(R.id.listView1);
 		List<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();
 		for (Orphan o : orphans) {
 			HashMap<String, String> map = new HashMap<String, String>();
 			map.put("sender", getContactName(o.getAddress()));
 			map.put("datetime", o.getDate().toLocaleString());
 			map.put("message", o.getMessageBody());
 			items.add(map);
 		}
 		Log.d("COUNT", String.valueOf(items.size()));
 		String[] from = new String[] { "sender", "datetime", "message" };
 		int[] to = new int[] { R.id.sender, R.id.datetime, R.id.message };
 		lv.setAdapter(new SimpleAdapter(this, items, R.layout.lvitem, from, to));
 	}
 
 	public void deleteAll() {
 		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				switch (which) {
 				case DialogInterface.BUTTON_POSITIVE:
 					deleteAllRecords();
 					displayOrphanList();
 					break;
 				}
 			}
 		};
 
 		Builder builder = new Builder(this);
 		builder.setMessage("Are you sure you want to delete all orphaned messages?")
 				.setPositiveButton("Yes", dialogClickListener)
 				.setNegativeButton("No", dialogClickListener).show();
 	}
 
 	public void deleteAllRecords() {
 		getContentResolver().query(uri, null, null, null, null /* "_id limit 10" */);
 		getContentResolver().delete(uri, null, null);
 		orphans.clear();
 	}
 
 	public void email() {
 		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
 		emailIntent.setType("plain/text");
 		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Orphaned Texts");
 		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, output);
 		startActivity(emailIntent);
 	}
 
 	public String getContactName(String phoneNumber) {
 		Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
 				Uri.encode(phoneNumber));
 		String[] columns = { PhoneLookup.DISPLAY_NAME };
 		String displayName = phoneNumber;
 		Cursor cursor = getContentResolver().query(lookupUri, columns, null, null, null);
 		if (cursor != null) {
 			if (cursor.moveToFirst()) {
 				displayName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
 			}
 			cursor.close();
 		}
 		return displayName;
 	}
 }
