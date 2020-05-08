 package com.example.htw_app.mensaplan;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.http.NameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import com.example.htw_app.R;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SpeiseplanCAS extends Activity {
 	private ProgressDialog pDialog;
 	public static SpeiseplanCAS ma;
 	JSONParser jParser = new JSONParser();
 	JSONArray products = null;
 	JSONArray products2 = null;
 	public static ArrayList<Zeile> refreshAdapter;
 	public static ArrayList<Informationen> refreshInformationenAdapter;
 	public static ArrayList<Zeile> zeilenAdapter;
 	public static ArrayList<Informationen> informationenAdapter;
 
 	private static final String URL_ALL_DATA_CAS = "http://htwapp.ohost.de/get_all_data_cas.php";
 	private static final String URL_ALL_DATA_INFORMATIONEN = "http://htwapp.ohost.de/get_all_data_informationen.php";
 	private static final String TAG_SUCCESS = "success";
 	private static final String TAG_PRODUCTS = "data";
 	private static final String TAG_PID = "id";
 	private static final String TAG_NAME = "tag";
 	private static final String TAG_1 = "menue1";
 	private static final String TAG_2 = "menue2";
 	
 	private static final String TAG_CAMPUS = "campus";
 	private static final String TAG_WOCHE = "woche";
 	private static final String TAG_BEILAGEN = "beilagen";
 	private static final String TAG_ESSENSAUSGABE = "essensausgabe";
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_mensa);
 
 		final TextView textView1 = (TextView) findViewById(R.id.textView1);
 		final TextView textView2 = (TextView) findViewById(R.id.textView2);
 		final TextView textView3 = (TextView) findViewById(R.id.textView3);
 		final TextView textView4 = (TextView) findViewById(R.id.textView4);
 		final TextView textView5 = (TextView) findViewById(R.id.textView5);
 		final TextView textView6 = (TextView) findViewById(R.id.textView6);
 		final TextView textView7 = (TextView) findViewById(R.id.textView7);
 		final TextView textView8 = (TextView) findViewById(R.id.textView8);
 		final TextView textView9 = (TextView) findViewById(R.id.textView9);
 		final TextView textView10 = (TextView) findViewById(R.id.textView10);
 		final TextView textView11 = (TextView) findViewById(R.id.textView11);
 		final TextView textView12 = (TextView) findViewById(R.id.textView12);
 		final TextView textView13 = (TextView) findViewById(R.id.textView13);
 		final TextView textView14 = (TextView) findViewById(R.id.textView14);
 		final TextView textView15 = (TextView) findViewById(R.id.textView15);
 		final TextView textView16 = (TextView) findViewById(R.id.textView16);
 		final TextView textView17 = (TextView) findViewById(R.id.textView17);
 		final TextView textView21 = (TextView) findViewById(R.id.textView21);
 		final TextView textView22 = (TextView) findViewById(R.id.textView22);
 
 		refreshAdapter = new ArrayList<Zeile>();
 		refreshInformationenAdapter = new ArrayList<Informationen>();
 		zeilenAdapter = new ArrayList<Zeile>();
 		informationenAdapter = new ArrayList<Informationen>();
 
 		doRawSelectMenue();
 		for (int i = 0; i < zeilenAdapter.size(); i++) {
 			Zeile zeile = zeilenAdapter.get(i);
 			if (i == 0) {
 				textView3.setText(zeile.getTag());
 				textView4.setText(zeile.getMenue1());
 				textView5.setText(zeile.getMenue2());
 			} else if (i == 1) {
 				textView6.setText(zeile.getTag());
 				textView7.setText(zeile.getMenue1());
 				textView8.setText(zeile.getMenue2());
 			} else if (i == 2) {
 				textView9.setText(zeile.getTag());
 				textView10.setText(zeile.getMenue1());
 				textView11.setText(zeile.getMenue2());
 			} else if (i == 3) {
 				textView12.setText(zeile.getTag());
 				textView13.setText(zeile.getMenue1());
 				textView14.setText(zeile.getMenue2());
 			} else if (i == 4) {
 				textView15.setText(zeile.getTag());
 				textView16.setText(zeile.getMenue1());
 				textView17.setText(zeile.getMenue2());
 			}
 		}
 
 		doRawSelectInformationen();
 		for (int i = 0; i < informationenAdapter.size(); i++) {
 			if (i == 0) {
 				Informationen info = informationenAdapter.get(i);
 				textView2.setText(info.getCampus());
 				textView1.setText(info.getWoche());
 				textView21.setText(info.getBeilagen());
 				textView22.setText(info.getEssensausgabe());
 			}
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.menu_mensa, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
		/*
 		switch (item.getItemId()) {
 		case R.id.action_mensa_refresh:
 			if (isOnline()) {
 				new LoadAllProducts().execute();
 			} else {
 				Toast.makeText(SpeiseplanCAS.this,
 						"Internetverbindung notwendig!", Toast.LENGTH_SHORT)
 						.show();
 			}
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
		*/
		return true; //FIXME zeile entfernen!
 	}
 
 	public boolean isOnline() {
 		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo netInfo = cm.getActiveNetworkInfo();
 		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
 			return true;
 		}
 		return false;
 	}
 
 	public void doRawSelectMenue() {
 		zeilenAdapter.clear();
 		SQLiteDatabase db = new DatabaseHelper(this).getReadableDatabase();
 		String sql = "SELECT * FROM " + DatabaseHelper.TABLE_MENUE
 				+ " ORDER BY " + DatabaseHelper.MENUE_FIELD_ID;
 		Cursor result = db.rawQuery(sql, null);
 
 		if (result.moveToFirst()) {
 			int idIdx = result.getColumnIndex(DatabaseHelper.MENUE_FIELD_ID);
 			int tagIdx = result.getColumnIndex(DatabaseHelper.MENUE_FIELD_TAG);
 			int menue1Idx = result
 					.getColumnIndex(DatabaseHelper.MENUE_FIELD_MENUE1);
 			int menue2Idx = result
 					.getColumnIndex(DatabaseHelper.MENUE_FIELD_MENUE2);
 			do {
 				int id = result.getInt(idIdx);
 				String tag = result.getString(tagIdx);
 				String menue1 = result.getString(menue1Idx);
 				String menue2 = result.getString(menue2Idx);
 
 				Zeile zeile = new Zeile(id, tag, menue1, menue2);
 				zeilenAdapter.add(zeile);
 			} while (result.moveToNext());
 		}
 		result.close();
 		db.close();
 	}
 
 	public void doRawSelectInformationen() {
 		informationenAdapter.clear();
 		SQLiteDatabase db = new DatabaseHelper(this).getReadableDatabase();
 		String sql = "SELECT * FROM " + DatabaseHelper.TABLE_INFORMATIONEN
 				+ " ORDER BY " + DatabaseHelper.INFORMATIONEN_FIELD_ID;
 		Cursor result = db.rawQuery(sql, null);
 
 		if (result.moveToFirst()) {
 			int idIdx = result
 					.getColumnIndex(DatabaseHelper.INFORMATIONEN_FIELD_ID);
 			int campusIdx = result
 					.getColumnIndex(DatabaseHelper.INFORMATIONEN_FIELD_CAMPUS);
 			int wocheIdx = result
 					.getColumnIndex(DatabaseHelper.INFORMATIONEN_FIELD_WOCHE);
 			int beilagenIdx = result
 					.getColumnIndex(DatabaseHelper.INFORMATIONEN_FIELD_BEILAGEN);
 			int essensausgabeIdx = result
 					.getColumnIndex(DatabaseHelper.INFORMATIONEN_FIELD_ESSENSAUSGABE);
 			do {
 				int id = result.getInt(idIdx);
 				String campus = result.getString(campusIdx);
 				String woche = result.getString(wocheIdx);
 				String beilagen = result.getString(beilagenIdx);
 				String essensausgabe = result.getString(essensausgabeIdx);
 				Informationen info = new Informationen(id, campus, woche,
 						beilagen, essensausgabe);
 				informationenAdapter.add(info);
 			} while (result.moveToNext());
 		}
 		result.close();
 		db.close();
 	}
 
 	class LoadAllProducts extends AsyncTask<String, String, String> {
 		
 		final TextView textView1 = (TextView) findViewById(R.id.textView1);
 		final TextView textView2 = (TextView) findViewById(R.id.textView2);
 		final TextView textView3 = (TextView) findViewById(R.id.textView3);
 		final TextView textView4 = (TextView) findViewById(R.id.textView4);
 		final TextView textView5 = (TextView) findViewById(R.id.textView5);
 		final TextView textView6 = (TextView) findViewById(R.id.textView6);
 		final TextView textView7 = (TextView) findViewById(R.id.textView7);
 		final TextView textView8 = (TextView) findViewById(R.id.textView8);
 		final TextView textView9 = (TextView) findViewById(R.id.textView9);
 		final TextView textView10 = (TextView) findViewById(R.id.textView10);
 		final TextView textView11 = (TextView) findViewById(R.id.textView11);
 		final TextView textView12 = (TextView) findViewById(R.id.textView12);
 		final TextView textView13 = (TextView) findViewById(R.id.textView13);
 		final TextView textView14 = (TextView) findViewById(R.id.textView14);
 		final TextView textView15 = (TextView) findViewById(R.id.textView15);
 		final TextView textView16 = (TextView) findViewById(R.id.textView16);
 		final TextView textView17 = (TextView) findViewById(R.id.textView17);
 		final TextView textView21 = (TextView) findViewById(R.id.textView21);
 		final TextView textView22 = (TextView) findViewById(R.id.textView22);
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			pDialog = new ProgressDialog(SpeiseplanCAS.this);
 			pDialog.setMessage("Loading data. Please wait...");
 			pDialog.setIndeterminate(false);
 			pDialog.setCancelable(false);
 			pDialog.show();
 		}
 
 		protected String doInBackground(String... args) {
 			refreshAdapter.clear();
 			List<NameValuePair> params = new ArrayList<NameValuePair>();
 			JSONObject json = jParser.makeHttpRequest(URL_ALL_DATA_CAS, "GET",
 					params);
 
 			try {
 				int success = json.getInt(TAG_SUCCESS);
 
 				if (success == 1) {
 					products = json.getJSONArray(TAG_PRODUCTS);
 
 					for (int i = 0; i < products.length(); i++) {
 						JSONObject c = products.getJSONObject(i);
 
 						int id = c.getInt(TAG_PID);
 						String name = c.getString(TAG_NAME);
 						String menue1 = c.getString(TAG_1);
 						String menue2 = c.getString(TAG_2);
 
 						Zeile zeile = new Zeile(id, name, menue1, menue2);
 						refreshAdapter.add(zeile);
 
 						SQLiteDatabase db = MensaActivity.mDbHelper
 								.getWritableDatabase();
 
 						ContentValues values = new ContentValues();
 						values.put(DatabaseHelper.MENUE_FIELD_TAG, name);
 						values.put(DatabaseHelper.MENUE_FIELD_MENUE1, menue1);
 						values.put(DatabaseHelper.MENUE_FIELD_MENUE2, menue2);
 
 						db.update(DatabaseHelper.TABLE_MENUE, values,
 								DatabaseHelper.MENUE_FIELD_ID + "= '" + id
 										+ "'", null);
 						db.close();
 					}
 				}
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 			
 			List<NameValuePair> params2 = new ArrayList<NameValuePair>();
 			JSONObject json2 = jParser.makeHttpRequest(URL_ALL_DATA_INFORMATIONEN, "GET",
 					params2);
 			try {
 				int success = json2.getInt(TAG_SUCCESS);
 
 				if (success == 1) {
 					products2 = json2.getJSONArray(TAG_PRODUCTS);
 
 					for (int i = 0; i < products2.length(); i++) {
 						JSONObject c = products2.getJSONObject(i);
 
 						int id = c.getInt(TAG_PID);
 						String campus = c.getString(TAG_CAMPUS);
 						String woche = c.getString(TAG_WOCHE);
 						String beilagen = c.getString(TAG_BEILAGEN);
 						String essensausgabe = c.getString(TAG_ESSENSAUSGABE);
 
 						Informationen info = new Informationen(id, campus, woche, beilagen, essensausgabe);
 						refreshInformationenAdapter.add(info);
 
 						SQLiteDatabase db = MensaActivity.mDbHelper
 								.getWritableDatabase();
 
 						ContentValues values = new ContentValues();
 						values.put(DatabaseHelper.INFORMATIONEN_FIELD_CAMPUS, campus);
 						values.put(DatabaseHelper.INFORMATIONEN_FIELD_WOCHE, woche);
 						values.put(DatabaseHelper.INFORMATIONEN_FIELD_BEILAGEN, beilagen);
 						values.put(DatabaseHelper.INFORMATIONEN_FIELD_ESSENSAUSGABE, essensausgabe);
 
 						db.update(DatabaseHelper.TABLE_INFORMATIONEN, values,
 								DatabaseHelper.INFORMATIONEN_FIELD_ID + "= '" + id
 										+ "'", null);
 						db.close();
 					}
 				}
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 
 			return null;
 		}
 
 		protected void onPostExecute(String file_url) {
 			pDialog.dismiss();
 			runOnUiThread(new Runnable() {
 				public void run() {
 					for (int i = 0; i < refreshAdapter.size(); i++) {
 						Zeile zeile = refreshAdapter.get(i);
 						if (i == 0) {
 							textView3.setText(zeile.getTag());
 							textView4.setText(zeile.getMenue1());
 							textView5.setText(zeile.getMenue2());
 						} else if (i == 1) {
 							textView6.setText(zeile.getTag());
 							textView7.setText(zeile.getMenue1());
 							textView8.setText(zeile.getMenue2());
 						} else if (i == 2) {
 							textView9.setText(zeile.getTag());
 							textView10.setText(zeile.getMenue1());
 							textView11.setText(zeile.getMenue2());
 						} else if (i == 3) {
 							textView12.setText(zeile.getTag());
 							textView13.setText(zeile.getMenue1());
 							textView14.setText(zeile.getMenue2());
 						} else if (i == 4) {
 							textView15.setText(zeile.getTag());
 							textView16.setText(zeile.getMenue1());
 							textView17.setText(zeile.getMenue2());
 						}
 					}
 					
 					for (int i = 0; i < refreshInformationenAdapter.size(); i++) {
 						Informationen info = refreshInformationenAdapter.get(i);
 						if (i == 0) {
 							textView1.setText(info.getWoche());
 							textView2.setText(info.getCampus());
 							textView21.setText(info.getBeilagen());
 							textView22.setText(info.getEssensausgabe());
 						}
 					}
 				}
 			});
 		}
 	}
 }
