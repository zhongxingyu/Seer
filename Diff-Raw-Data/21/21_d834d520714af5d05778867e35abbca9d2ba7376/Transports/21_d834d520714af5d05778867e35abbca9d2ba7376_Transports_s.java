 ﻿package de.tum.in.tumcampus;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.MatrixCursor;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.EditText;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 import de.tum.in.tumcampus.models.TransportManager;
 
 /**
  * Activity to show transport stations and departures
  */
 public class Transports extends Activity implements OnItemClickListener,
 		OnItemLongClickListener, OnEditorActionListener {
 
 	/**
 	 * Check if a network connection is available or can be available soon
 	 * 
 	 * @return true if available
 	 */
 	public boolean connected() {
 		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo netInfo = cm.getActiveNetworkInfo();
 
 		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
 			setContentView(R.layout.transports_horizontal);
 		} else {
 			setContentView(R.layout.transports);
 		}
 
 		// get all stations from db
 		TransportManager tm = new TransportManager(this, Const.db);
 		Cursor c = tm.getAllFromDb();
 
 		ListAdapter adapter = new SimpleCursorAdapter(this,
 				android.R.layout.simple_list_item_1, c, c.getColumnNames(),
 				new int[] { android.R.id.text1 });
 
 		final ListView lv = (ListView) findViewById(R.id.listView);
 		lv.setAdapter(adapter);
 		lv.setOnItemClickListener(this);
 		lv.setOnItemLongClickListener(this);
 		tm.close();
 
 		// search stations when edit box has 3 characters
 		final EditText et = (EditText) findViewById(R.id.search);
 		et.setOnEditorActionListener(this);
 
 		et.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void onTextChanged(CharSequence input, int arg1, int arg2,
 					int arg3) {
 				if (input.length() == 3) {
 					et.onEditorAction(android.view.inputmethod.EditorInfo.IME_ACTION_DONE);
 				}
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence arg0, int arg1,
 					int arg2, int arg3) {
 				// empty
 			}
 
 			@Override
 			public void afterTextChanged(Editable arg0) {
 				// empty
 			}
 		});
 
 		// initialize empty departure list, disable on click in list
 		MatrixCursor c2 = new MatrixCursor(
 				new String[] { "name", "desc", "_id" });
 		SimpleCursorAdapter adapter2 = new SimpleCursorAdapter(this,
 				android.R.layout.two_line_list_item, c2, c2.getColumnNames(),
 				new int[] { android.R.id.text1, android.R.id.text2 }) {
 
 			@Override
 			public boolean isEnabled(int position) {
 				return false;
 			}
 		};
 		ListView lv2 = (ListView) findViewById(R.id.listView2);
 		lv2.setAdapter(adapter2);
 	}
 
 	@Override
 	public void onItemClick(final AdapterView<?> av, View v, int position,
 			long id) {
 		// click on station in list
 		Cursor c = (Cursor) av.getAdapter().getItem(position);
 		final String location = c.getString(c.getColumnIndex("name"));
 
 		TextView tv = (TextView) findViewById(R.id.transportText);
 		tv.setText("Abfahrt: " + location);
 
 		tv = (TextView) findViewById(R.id.transportText2);
 		tv.setText("Gespeicherte Stationen:");
 
 		// save clicked station into db and refresh station list
 		// (could be clicked on search result list)
 		SimpleCursorAdapter adapter = (SimpleCursorAdapter) av.getAdapter();
 		TransportManager tm = new TransportManager(this, Const.db);
 		tm.replaceIntoDb(location);
 		adapter.changeCursor(tm.getAllFromDb());
 		tm.close();
 
 		// load departures in new thread, show progress dialog during load
 		final ProgressDialog progress = ProgressDialog.show(this, "",
 				"Lade ...", true);
 
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				Cursor c = null;
 				try {
					// get departures from website
					TransportManager tm = new TransportManager(av.getContext(),
							Const.db);
 					if (!connected()) {
 						throw new Exception("<Keine Internetverbindung>");
 					}
 					c = tm.getDeparturesFromExternal(location);
					tm.close();
 				} catch (Exception e) {
 					// show errors in departures list
 					MatrixCursor c2 = new MatrixCursor(new String[] { "name",
 							"desc", "_id" });
 					c2.addRow(new String[] { e.getMessage(), "", "0" });
 					c = c2;
 				}
 
 				// show departures in list
 				final Cursor c2 = c;
 				runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						progress.hide();
 
 						ListView lv2 = (ListView) findViewById(R.id.listView2);
 						SimpleCursorAdapter adapter = (SimpleCursorAdapter) lv2
 								.getAdapter();
 						adapter.changeCursor(c2);
 					}
 				});
 			}
 		}).start();
 	}
 
 	@Override
 	public boolean onItemLongClick(final AdapterView<?> av, View v,
 			final int position, long id) {
 
 		// confirm and delete station
 		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int id) {
 
 				// delete station from list, refresh station list
 				Cursor c = (Cursor) av.getAdapter().getItem(position);
 				String location = c.getString(c.getColumnIndex("name"));
 
 				TransportManager tm = new TransportManager(av.getContext(),
 						Const.db);
 				tm.deleteFromDb(location);
 
 				SimpleCursorAdapter adapter = (SimpleCursorAdapter) av
 						.getAdapter();
 				adapter.changeCursor(tm.getAllFromDb());
 				tm.close();
 			}
 		};
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage("Wirklch löschen?");
 		builder.setPositiveButton("Ja", listener);
 		builder.setNegativeButton("Nein", null);
 		builder.show();
 		return false;
 	}
 
 	@Override
 	public boolean onEditorAction(final TextView input, int code, KeyEvent key) {
 
 		// search station in new thread, show progress dialog during search
 		final ProgressDialog progress = ProgressDialog.show(this, "",
 				"Lade ...", true);
 
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 
 				// search station on website
 				String message = "";
 				Cursor c = null;
 				try {
 					if (!connected()) {
 						throw new Exception("<Keine Internetverbindung>");
 					}
					TransportManager tm = new TransportManager(
							input.getContext(), Const.db);
 					c = tm.getStationsFromExternal(input.getText().toString());
					tm.close();
 				} catch (Exception e) {
 					message = e.getMessage();
 				}
 
 				final Cursor c2 = c;
 				final String message2 = message;
 
 				// show stations from search result in station list
 				// show error message if necessary
 				runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						progress.hide();
 
 						if (c2 != null) {
 							TextView tv = (TextView) findViewById(R.id.transportText2);
 							tv.setText("Suchergebnis:");
 
 							ListView lv = (ListView) findViewById(R.id.listView);
 							SimpleCursorAdapter adapter = (SimpleCursorAdapter) lv
 									.getAdapter();
 							adapter.changeCursor(c2);
 						}
 						if (message2.length() > 0) {
 							Toast.makeText(input.getContext(), message2,
 									Toast.LENGTH_LONG).show();
 						}
 					}
 				});
 			}
 		}).start();
 		return false;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		menu.add(0, Menu.FIRST, 0, "MVV EFA");
 		menu.add(0, Menu.FIRST + 1, 0, "MVG Live");
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		// option menu for external links
 		switch (item.getItemId()) {
 		case Menu.FIRST:
 			String url = "http://efa.mvv-muenchen.de/mvv/XSLT_TRIP_REQUEST2?language=de";
 			Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
 			startActivity(viewIntent);
 			return true;
 
 		case Menu.FIRST + 1:
 			String url2 = "http://mobil.mvg-live.de/";
 			Intent viewIntent2 = new Intent(Intent.ACTION_VIEW, Uri.parse(url2));
 			startActivity(viewIntent2);
 			return true;
 		}
 		return false;
 	}
 }
