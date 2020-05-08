 package com.messedagliavr.messeapp;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.ConnectivityManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.Html;
 import android.text.InputType;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 
 public class MainActivity extends Activity  {
     int layoutid;
     static String nointernet = "false";
 
     public boolean CheckInternet() {
         boolean connected = false;
         ConnectivityManager connec = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         android.net.NetworkInfo wifi = connec
                 .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
         android.net.NetworkInfo mobile = connec
                 .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
         if (wifi.isConnected()) {
             connected = true;
         } else {
             try {
                 if (mobile.isConnected())
                     connected = true;
             } catch (Exception e) {
             }
         }
         return connected;
 
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         layoutid = R.id.activity_main;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     public void onBackPressed() {
         if (layoutid == R.id.info || layoutid == R.id.social
                 || layoutid == R.id.contatti || layoutid == R.id.settings) {
             setContentView(R.layout.activity_main);
             layoutid = R.id.activity_main;
         } else {
             super.finish();
         }
 
     }
 
     public void onToggleClicked(View view) {
         ToggleButton toggle = (ToggleButton) findViewById(R.id.saveenabled);
         boolean on = toggle.isChecked();
         EditText user = (EditText) findViewById(R.id.username);
         EditText password = (EditText) findViewById(R.id.password);
         Button save = (Button) findViewById(R.id.savesett);
         CheckBox checkbox = (CheckBox) findViewById(R.id.checkBox1);
         if (on) {
             Database databaseHelper = new Database(getBaseContext());
             SQLiteDatabase db = databaseHelper.getWritableDatabase();
             String[] columns = { "username", "password" };
             Cursor query = db.query("settvoti", // The table to query
                     columns, // The columns to return
                     null, // The columns for the WHERE clause
                     null, // The values for the WHERE clause
                     null, // don't group the rows
                     null, // don't filter by row groups
                     null // The sort order
             );
             user.setVisibility(View.VISIBLE);
             query.moveToFirst();
             user.setText(query.getString(query.getColumnIndex("username")));
             password.setVisibility(View.VISIBLE);
             password.setText(query.getString(query.getColumnIndex("password")));
             save.setVisibility(View.VISIBLE);
             checkbox.setVisibility(View.VISIBLE);
             query.close();
             db.close();
         } else {
             user.setVisibility(View.GONE);
             password.setVisibility(View.GONE);
             save.setVisibility(View.GONE);
             checkbox.setVisibility(View.GONE);
             Database databaseHelper = new Database(getBaseContext());
             SQLiteDatabase db = databaseHelper.getWritableDatabase();
             ContentValues values = new ContentValues();
             values.put("enabled", "false");
             @SuppressWarnings("unused")
             long samerow = db.update("settvoti", values, null, null);
             db.close();
             Toast.makeText(MainActivity.this, getString(R.string.noautologin),
                     Toast.LENGTH_LONG).show();
         }
     }
 
     public void onCheckClicked(View view) {
         CheckBox toggle = (CheckBox) findViewById(R.id.checkBox1);
         EditText password = (EditText) findViewById(R.id.password);
         if (toggle.isChecked()) {
             password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
         } else {
             password.setInputType(129);
         }
     }
 
     public void onSaveClicked(View view) {
         EditText usert = (EditText) findViewById(R.id.username);
         EditText passwordt = (EditText) findViewById(R.id.password);
         String username = usert.getText().toString();
         String password = passwordt.getText().toString();
         Database databaseHelper = new Database(getBaseContext());
         SQLiteDatabase db = databaseHelper.getWritableDatabase();
         ContentValues values = new ContentValues();
         values.put("enabled", "true");
         values.put("username", username);
         values.put("password", password);
         @SuppressWarnings("unused")
         long samerow = db.update("settvoti", values, null, null);
         db.close();
         Toast.makeText(MainActivity.this, getString(R.string.settingssaved),
                 Toast.LENGTH_LONG).show();
     }
 
 
     @SuppressLint("NewApi")
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.settings:
                 setContentView(R.layout.settings);
                 layoutid = R.id.settings;
                 EditText user = (EditText) findViewById(R.id.username);
                 EditText password = (EditText) findViewById(R.id.password);
                 CheckBox check = (CheckBox) findViewById(R.id.checkBox1);
                 Button save = (Button) findViewById(R.id.savesett);
                 ToggleButton toggle = (ToggleButton) findViewById(R.id.saveenabled);
                 Database databaseHelper = new Database(getBaseContext());
                 SQLiteDatabase db = databaseHelper.getWritableDatabase();
                 String[] columns = { "enabled", "username", "password" };
                 Cursor query = db.query("settvoti", // The table to query
                         columns, // The columns to return
                         null, // The columns for the WHERE clause
                         null, // The values for the WHERE clause
                         null, // don't group the rows
                         null, // don't filter by row groups
                         null // The sort order
                 );
                 query.moveToFirst();
                 String enabled = query.getString(query.getColumnIndex("enabled"));
                 db.close();
                 if (enabled.matches("true")) {
                     user.setVisibility(View.VISIBLE);
                     user.setText(query.getString(query.getColumnIndex("username")));
                     password.setVisibility(View.VISIBLE);
                     password.setText(query.getString(query
                             .getColumnIndex("password")));
                     save.setVisibility(View.VISIBLE);
                     check.setVisibility(View.VISIBLE);
                     toggle.setChecked(true);
                 } else {
                     user.setVisibility(View.GONE);
                     password.setVisibility(View.GONE);
                     save.setVisibility(View.GONE);
                     check.setVisibility(View.GONE);
                 }
                 query.close();
                 break;
             case R.id.info:
                 PackageInfo pinfo = null;
                 try {
                     pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                 } catch (NameNotFoundException e) {
                     e.printStackTrace();
                 }
                 String versionName = pinfo.versionName;
                 setContentView(R.layout.info);
                 TextView vername = (TextView) findViewById(R.id.versionname);
                 vername.setText(versionName);
                 layoutid = R.id.info;
                 break;
             case R.id.exit:
                 super.finish();
                 break;
             case R.id.contatti:
                 startActivity(new Intent(this, contacts.class));
                 break;
             case R.id.migliora:
                 AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 builder.setTitle(getString(R.string.suggestion));
                 final EditText input = new EditText(this);
                 input.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                 input.setVerticalScrollBarEnabled(true);
                 input.setSingleLine(false);
                 builder.setView(input);
                 builder.setPositiveButton("OK",
                         new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 String m_Text = input.getText().toString();
                                 Intent emailIntent = new Intent(
                                         Intent.ACTION_SENDTO, Uri.fromParts(
                                         "mailto", "null.p.apps@gmail.com",
                                         null));
                                 emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                                         getString(R.string.suggestion));
                                 emailIntent.putExtra(Intent.EXTRA_TEXT,
                                         Html.fromHtml(m_Text));
                                 startActivity(Intent.createChooser(emailIntent,
                                         getString(R.string.suggestion)));
                             }
                         });
                 builder.setNegativeButton(getString(R.string.cancel),
                         new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 dialog.cancel();
                             }
                         });
 
                 builder.show();
                 break;
         }
         return true;
     }
 
     public void social(View v) {
         startActivity(new Intent(this, social.class));
         layoutid = R.id.social;
     }
 
     public void voti(View v) {
         Database databaseHelper = new Database(getBaseContext());
         SQLiteDatabase db = databaseHelper.getWritableDatabase();
         String[] columns = { "enabled", "username", "password" };
         Cursor query = db.query("settvoti", // The table to query
                 columns, // The columns to return
                 null, // The columns for the WHERE clause
                 null, // The values for the WHERE clause
                 null, // don't group the rows
                 null, // don't filter by row groups
                 null // The sort order
         );
         query.moveToFirst();
         String enabled = query.getString(query.getColumnIndex("enabled"));
         db.close();
         if (enabled.matches("true")) {
             String user = query.getString(query.getColumnIndex("username"));
             String password = query.getString(query.getColumnIndex("password"));
             Intent voti = new Intent(Intent.ACTION_VIEW);
             voti.setData(Uri
                     .parse("https://web.spaggiari.eu/home/app/default/login.php?custcode=VRLS0003&login="
                             + user + "&password=" + password));
             query.close();
             startActivity(voti);
         } else {
             Intent voti = new Intent(Intent.ACTION_VIEW);
             voti.setData(Uri
                     .parse("https://web.spaggiari.eu/home/app/default/login.php?custcode=VRLS0003"));
             query.close();
             startActivity(voti);
         }
     }
 
     public void news(View v) {
         if (CheckInternet() == true) {
             nointernet = "false";
             startActivity(new Intent(this, news.class));
         } else {
             String[] outdated = { "newsdate", "calendardate" };
             Database databaseHelper = new Database(getBaseContext());
             SQLiteDatabase db = databaseHelper.getWritableDatabase();
             String nodata = "1995-01-19 23:40:20";
             Cursor date = db.query("lstchk", // The table to query
                     outdated, // The columns to return
                     null, // The columns for the WHERE clause
                     null, // The values for the WHERE clause
                     null, // don't group the rows
                     null, // don't filter by row groups
                     null // The sort order
             );
             date.moveToFirst();
             String verifydatenews = date.getString(date
                     .getColumnIndex("newsdate"));
             date.close();
             if (nodata != verifydatenews) {
                 nointernet = "true";
                 startActivity(new Intent(this, news.class));
             } else {
                 Toast.makeText(MainActivity.this, R.string.noconnection,
                         Toast.LENGTH_LONG).show();
             }
         }
     }
 
     public void calendar(View v) {
         if (CheckInternet() == true) {
             nointernet = "false";
             startActivity(new Intent(this, calendar.class));
         } else {
             String[] outdated = { "newsdate", "calendardate" };
             Database databaseHelper = new Database(getBaseContext());
             SQLiteDatabase db = databaseHelper.getWritableDatabase();
             String nodata = "1995-01-19 23:40:20";
             Cursor date = db.query("lstchk", // The table to query
                     outdated, // The columns to return
                     null, // The columns for the WHERE clause
                     null, // The values for the WHERE clause
                     null, // don't group the rows
                     null, // don't filter by row groups
                     null // The sort order
             );
             date.moveToFirst();
             String verifydatenews = date.getString(date
                     .getColumnIndex("newsdate"));
             date.close();
             if (nodata != verifydatenews) {
                 nointernet = "true";
                 startActivity(new Intent(this, calendar.class));
             } else {
                 Toast.makeText(MainActivity.this,
                         R.string.noconnection, Toast.LENGTH_LONG)
                         .show();
             }
         }
     }
 
     public void orario(View v) {
         startActivity(new Intent(this, timetable.class));
     }
 
     public void notavailable(View v) {
         Toast.makeText(MainActivity.this, R.string.notavailable,
                 Toast.LENGTH_LONG).show();
     }
 
 }
