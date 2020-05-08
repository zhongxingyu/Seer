 package com.dj.antispam;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import com.dj.antispam.actionbarcompat.ActionBarActivity;
 import com.dj.antispam.dao.SmsDao;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dj
  * Date: 23.06.13
  * Time: 13:18
  * To change this template use File | Settings | File Templates.
  */
 public class ImportActivity extends ActionBarActivity {
 
 	public static final int FIRST_IMPORT = 1;
 
 	private ImportListAdapter adapter;
 	private SmsDao dao;
 
 	/**
 	 * Called when the activity is first created.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		dao = new SmsDao(this);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.importer);
 		final ListView senders = (ListView) findViewById(R.id.listView);
 		adapter = new ImportListAdapter(this, dao);
 		senders.setAdapter(adapter);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.import_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.menu_sel_all:
 				onSelectAll();
 				return true;
 			case R.id.menu_sel_none:
 				onSelectNone();
 				return true;
 			case R.id.menu_sel_auto:
 				onSelectAuto();
 				return true;
 		}
 		return false;
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		super.finalize();
 		if (dao != null) {
 			dao.close();
 		}
 		adapter.close();
 	}
 
 	public void onOk(View view) {
 		// Save checkboxes to spam list
 		final SmsDao dao = new SmsDao(this);
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				Map<String, Boolean> senders = new HashMap<String, Boolean>(adapter.getCount());
 				List<String> denied = new ArrayList<String>();
 				List<String> allowed = new ArrayList<String>();
 				for (int i = 0; i < adapter.getCount(); i++) {
 					SenderStatus status = (SenderStatus) adapter.getItem(i);
					if (status.isSpam != null) {
 						if (status.isSpam) {
 							denied.add(status.address);
 						} else {
 							allowed.add(status.address);
 						}
 					}
 				}
 				moveToSpam(dao, denied);
 				Intent intent = new Intent(getResources().getString(R.string.update_action));
 				sendBroadcast(intent);
 				dao.markSender(denied, true);
 				dao.markSender(allowed, false);
 				dao.close();
 			}
 		}).start();
 
 		setResult(RESULT_OK);
 		finish();
 	}
 
 	private void moveToSpam(SmsDao dao, List<String> senders) {
 		String where = "address IN (" + Utils.join(senders, new Utils.Processor() {
 			@Override
 			public void format(StringBuilder builder, Object item) {
 				builder.append(DatabaseUtils.sqlEscapeString((String) item));
 			}
 		}) + ")";
		Cursor cur = getContentResolver().query(Uri.parse(Utils.URI_INBOX),
				new String[]{"address, body, date, _id"}, where, null, null);
 		try {
 			if (cur.moveToFirst()) {
 				do {
 					dao.putMessage(cur.getString(0),
 							cur.getLong(2),
 							cur.getString(1));
					getContentResolver().delete(Uri.parse(Utils.URI_SMS + "/" + cur.getLong(3)), null, null);
 				} while (cur.moveToNext());
 			}
 		} finally {
 			cur.close();
 		}
 	}
 
 	public void onCancel(View view) {
 		setResult(RESULT_CANCELED);
 		finish();
 	}
 
 	private void onSelectAll() {}
 
 	private void onSelectNone() {}
 
 	private void onSelectAuto() {}
 }
