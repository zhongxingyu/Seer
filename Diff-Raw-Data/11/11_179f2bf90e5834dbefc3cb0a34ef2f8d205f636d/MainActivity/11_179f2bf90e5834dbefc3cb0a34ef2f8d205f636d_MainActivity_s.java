 package com.messedagliavr.messeapp;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.ConnectivityManager;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.widget.DrawerLayout;
 import android.text.Html;
 import android.text.InputType;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 
 public class MainActivity extends Activity  {
     static int layoutid;
     static String nointernet;
     private DrawerLayout mDrawerLayout;
     private ActionBarDrawerToggle mDrawerToggle;
     private ListView mDrawerList;
 
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
                 if (mobile.isConnected()) connected = true;
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         return connected;
 
     }
     @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
     public void setDrawer(){
         String[] drawerItems={getString(R.string.settings),getString(R.string.contatti),getString(R.string.suggestion),getString(R.string.Info),getString(R.string.exit)};
         Toast.makeText(this, mDrawerList.toString(),Toast.LENGTH_LONG);
         mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                 R.layout.drawer_list_item, drawerItems));
         mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView parent, View view, int position, long id) {
                 doNavigationItem(position);
             }
         });
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                 R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
             // Called when a drawer has settled in a completely closed state.
             public void onDrawerClosed(View view) {}
             // Called when a drawer has settled in a completely open state.
             public void onDrawerOpened(View drawerView) {}
         };
         // Set the drawer toggle as the DrawerListener
         mDrawerLayout.setDrawerListener(mDrawerToggle);
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
             getActionBar().setDisplayHomeAsUpEnabled(true);
         } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
             getActionBar().setHomeButtonEnabled(true);
         }
     }
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         layoutid = R.id.activity_main;
         mDrawerList = (ListView) findViewById(R.id.left_drawer);
         setDrawer();
     }
 
     @Override
     public boolean onMenuOpened(int featureId, Menu menu) {
         manageDrawer();
         return super.onMenuOpened(featureId, menu);
     }
     public void manageDrawer(){
         try{
             if (mDrawerLayout.isDrawerOpen(findViewById(R.id.left_drawer))) {
                 mDrawerLayout.closeDrawer(findViewById(R.id.left_drawer));
             } else {
                 mDrawerLayout.openDrawer(findViewById(R.id.left_drawer));
             }
         }catch (NullPointerException e){
             Toast.makeText(this,"Loading",Toast.LENGTH_SHORT);
             e.printStackTrace();
         }
     }
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_MENU) manageDrawer();
         return super.onKeyDown(keyCode, event);
     }
 
     //Permette all'utente di aprire il navigation toccando l'app icon
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (mDrawerToggle.onOptionsItemSelected(item)) {
             return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         // Sync the toggle state after onRestoreInstanceState has occurred.
         mDrawerToggle.syncState();
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         mDrawerToggle.onConfigurationChanged(newConfig);
     }
 
     public void onBackPressed() {
         if (layoutid == R.id.info || layoutid == R.id.social
                 || layoutid == R.id.contatti || layoutid == R.id.settings) {
             setContentView(R.layout.activity_main);
             layoutid = R.id.activity_main;
             mDrawerList = (ListView) findViewById(R.id.left_drawer);
             setDrawer();
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
             Toast.makeText(this, getString(R.string.noautologin),
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
         Toast.makeText(this, getString(R.string.settingssaved),
                 Toast.LENGTH_LONG).show();
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
                 Toast.makeText(this, R.string.noconnection,
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
                 Toast.makeText(this,
                         R.string.noconnection, Toast.LENGTH_LONG)
                         .show();
             }
         }
     }
 
     public void orario(View v) {
         startActivity(new Intent(this, timetable.class));
     }
     public void panini(View v) {
         startActivity(new Intent(this, Panini.class));
     }
 
     public void notavailable(View v) {
         Toast.makeText(this, R.string.notavailable,
                 Toast.LENGTH_LONG).show();
     }
     public void doNavigationItem(int position){
         switch (position) {
             case 0:
                 setContentView(R.layout.settings);
                 mDrawerList = (ListView) findViewById(R.id.left_drawer);
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
                 setDrawer();
                 break;
             case 3:
                 PackageInfo pinfo = null;
                 try {
                     pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                     e.printStackTrace();
                 }
                 String versionName = pinfo.versionName;
                 setContentView(R.layout.info);
                ListView tempList =(ListView) findViewById(R.id.left_drawer);
                mDrawerList = tempList;
                 TextView vername = (TextView) findViewById(R.id.versionname);
                 vername.setText(versionName);
                 layoutid = R.id.info;
                 setDrawer();
                 break;
             case 4:
                 super.finish();
                 break;
             case 1:
                 startActivity(new Intent(this, contacts.class));
                 break;
             case 2:
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
     }
 }
