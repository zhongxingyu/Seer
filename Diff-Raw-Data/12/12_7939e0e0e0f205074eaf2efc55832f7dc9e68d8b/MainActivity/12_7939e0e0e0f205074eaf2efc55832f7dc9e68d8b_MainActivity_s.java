 package com.melchor629.musicote;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.widget.SearchView;
 import com.melchor629.musicote.R;
 import com.melchor629.musicote.basededatos.DB;
 import com.melchor629.musicote.basededatos.DB_entry;
 
 import android.content.pm.ActivityInfo;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.CursorIndexOutOfBoundsException;
 import android.database.MatrixCursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.AsyncTask;
 import android.os.Build.VERSION;
 import android.os.Build.VERSION_CODES;
 import android.os.Bundle;
 import android.os.Looper;
 import android.preference.PreferenceManager;
 import android.provider.BaseColumns;
 import android.support.v4.widget.CursorAdapter;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.app.NotificationManager;
 import android.app.ProgressDialog;
 import android.app.SearchManager;
 
 /**
  * Musicote App
  * Melchor629 2013
  *
  *    Copyright 2013 Melchor629
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
 **/
 
 /**
  * Actividad principal de la App, hace muchas cosas<br>
  * <b>TODO</b> añadir la función de Inicio de sesión
  * @author melchor
  */
 
 public class MainActivity extends SherlockListActivity implements SearchView.OnQueryTextListener, SearchView.OnSuggestionListener {
 
     public final static String EXTRA_MESSAGE = "com.melchor629.musicote.MESSAGE";
     public final static String Last_STRING = "asdasda";
 
     public static String Last_String = "";
     public static volatile int response = 0;
     public static String url;
 
     private ProgressDialog progressDialog;
     private Toast tostado;
     private SuggestionsAdapter mSuggestionsAdapter;
 
     // contacts JSONArray
     private static JSONArray contacts = null;
     private ArrayList<HashMap<String, String>> contactList;
 
     TextView mTextView; // Member variable for text view in the layout
     @Override
     public void onCreate(Bundle savedInstanceState) {
         // Set the user interface layout for this Activity
         // The layout file is defined in the project res/layout/main.xml file
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         // Check whether we're recreating a previously destroyed instance
         if (savedInstanceState != null) {
             // Restore value of members from saved state
             Last_String = savedInstanceState.getString(Last_STRING);
         }
         
         // La app prueba en busca de la dirección correcta
         WifiManager mw = (WifiManager) getSystemService(Context.WIFI_SERVICE);
         WifiInfo wi = mw.getConnectionInfo();
         String SSID = wi.getSSID();
         Log.d("MainActivity", "Wifi conectado: "+SSID);
         if(SSID.equals("wifi5eber") || System.getProperty("os.version").equals("2.6.29-gea477bb")){
             MainActivity.url = "192.168.1.133";
         } else {
             MainActivity.url = "reinoslokos.no-ip.org";
         }
         Log.d("MainActivity", "url: "+url);
         
         //Create a new progress dialog
         progressDialog = new ProgressDialog(MainActivity.this);
         progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
         progressDialog.setTitle("Musicote en camino...");
         progressDialog.setMessage("Cargando datos del servidor, espere...");
         progressDialog.setCancelable(false);
         if(VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
             progressDialog.setIndeterminate(false);
             progressDialog.setMax(100);
             progressDialog.setProgress(0);
         } else {
             progressDialog.setIndeterminate(true);
         }
         progressDialog.show();
 
         //Deletes the notification if remains (BUG)
         NotificationManager mn = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         if(Reproductor.a == -1)
             mn.cancel(1);
 
         //Revisa la base de datos
         DB mDbHelper = new DB(getBaseContext());
         SQLiteDatabase db = mDbHelper.getWritableDatabase();
         if(mDbHelper.ifTableExists(db, "canciones") == false || mDbHelper.ifTableExists(db, "acceso") == false) {
             db.execSQL(DB_entry.CREATE_ACCESO);
             ContentValues values = new ContentValues();
             values.put("tabla", "canciones");
             values.put("fecha", System.currentTimeMillis());
                 Log.e("newDB", "Dado "+db.insert("acceso", "null", values));
         }
 
         //Actualización de la lista
         SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
         if(mDbHelper.isNecesaryUpgrade(db, pref))
             db.execSQL(DB_entry.DELETE_CANCIONES);
 
         if(!mDbHelper.ifTableExists(db, "canciones"))
             async();
         else
            cursordb();
         
         db.close();
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
     }
     
     private void sis(){
         if(contactList == null){
             Log.e("MainActivity", "contactList viene vacío...");
             return;
         }
         /**
          * Updating parsed JSON data into ListView
          * */
         ListAdapter adapter = new SimpleAdapter(this, contactList,
                 R.layout.list_item,
                 new String[] { "titulo", "artista", "album" }, new int[] {
                     R.id.name, R.id.email, R.id.mobile });
 
         try {
             setListAdapter(adapter);
         }catch (Exception e){
             Log.e("ServerHostDetector","Er ordenata de mershor ta apagao... "+e.toString());
             tostado = Toast.makeText(MainActivity.this, "Ningún servidor activo...", Toast.LENGTH_LONG);
             tostado.show();
         }
 
         // selecting single ListView item
         ListView lv = getListView();
         
         lv.setFastScrollEnabled(true);
 
         // Launching new screen on Selecting Single ListItem
         lv.setOnItemClickListener(new OnItemClickListener() {
 
             @Override
             public void onItemClick(AdapterView<?> parent, View view,
                 int position, long id) {
                 // getting values from selected ListItem
                 //JSONObject datos = null;
                 HashMap<String, String> datos = null;
                 try{
                     datos = contactList.get(position);
                 }catch(Exception e){
                     Log.e("com.melchor629.musicote", "250<<"+e.toString()); e.printStackTrace();
                 }
                 String name = getString(R.string.vacio);
                 String cost = getString(R.string.vacio);
                 String description = getString(R.string.vacio);
                 String album = "-00:00";
                 String archivo = "http://"+url+"/multimedia/escucha.php";
                 try{
                     name = ((TextView) view.findViewById(R.id.name)).getText().toString();
                     cost = ((TextView) view.findViewById(R.id.email)).getText().toString();
                     description = ((TextView) view.findViewById(R.id.mobile)).getText().toString();
                     album = datos.get("duracion");
                     archivo = datos.get("archivo");
                     //album = datos.getString("duracion");
                     //archivo = datos.getString("archivo");
                 } catch (Exception e){
                     Log.e("com.melchor629.musicote", e.toString());
                 }
 
                 // Starting new intent
                 Intent in = new Intent(getApplicationContext(), SingleMenuItemActivity.class);
                 in.putExtra("titulo", name);
                 in.putExtra("artista", cost);
                 in.putExtra("album", description);
                 in.putExtra("duracion", album);
                 in.putExtra("archivo", archivo);
 
                 startActivity(in);
             }
         });
     }
     
     @Override
     public void onPause() {
         super.onPause();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getSupportMenuInflater().inflate(R.menu.main, menu);
         
         //TODO http://developer.android.com/intl/es/guide/topics/search/search-dialog.html
         //Create the search view
         SearchView searchView = new SearchView(getSupportActionBar().getThemedContext());
         searchView.setQueryHint(getResources().getString(R.string.menu_search));
         searchView.setOnQueryTextListener(this);
         searchView.setOnSuggestionListener(this);
         searchView.setSuggestionsAdapter(mSuggestionsAdapter);
         
         String[] COLUMNS = {
             BaseColumns._ID,
             SearchManager.SUGGEST_COLUMN_TEXT_1,
         };
         
         if (mSuggestionsAdapter == null) {
             MatrixCursor cursor = new MatrixCursor(COLUMNS);
             cursor.addRow(new String[]{"1", "'Murica"});
             cursor.addRow(new String[]{"2", "Canada"});
             cursor.addRow(new String[]{"3", "Denmark"});
             mSuggestionsAdapter = new SuggestionsAdapter(getSupportActionBar().getThemedContext(), cursor);
         }
         
         menu.add("Search")
             .setIcon(R.drawable.abs__ic_search)
             .setActionView(searchView)
             .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item){
         switch(item.getItemId()){
             case R.id.ajustesm:
                 Intent intent = new Intent(MainActivity.this, Ajustes.class);
                 startActivity(intent);
                 break;
             case R.id.parar:
                 Intent intento = new Intent(MainActivity.this, ReproductorGrafico.class);
                 startActivity(intento);
             default:
                 return super.onOptionsItemSelected(item);
         }
         return true;
     }
 
     // Intento de guardar lo ultimo enviado al otro .class
     @Override
     public void onSaveInstanceState(Bundle savedInstanceState) {
         // Save the user's current game state
         savedInstanceState.putString(Last_STRING, EXTRA_MESSAGE);
 
         // Always call the superclass so it can save the view hierarchy state
         super.onSaveInstanceState(savedInstanceState);
     }
 
     /**
      * Clase AsyncTask para descargar el JSON y parsearlo y no desesperar a la peña
      * @author melchor
      *
      */
     private class JSONParseDialog extends AsyncTask<Void, Integer, ArrayList<HashMap<String,String>>> {
         @Override
         protected ArrayList<HashMap<String, String>> doInBackground(Void... params){
             // Hashmap for ListView
             final ArrayList<HashMap<String, String>> contactList = new ArrayList<HashMap<String, String>>();
 
             // Creating JSON Parser instance
             ParseJSON jParser = new ParseJSON();
 
             synchronized (this){
                 publishProgress(1);
                 
                 publishProgress(2);
                 if(MainActivity.url!=null){
                     // getting JSON string from URL
                     response = jParser.HostTest(MainActivity.url);
                     
                     Log.d("MainActivity", "Response code: "+response);
                     
                     publishProgress(25);
                     if(response == 200){
                         JSONObject json = jParser.getJSONFromUrl("http://"+MainActivity.url+"/cgi-bin/archivos.py");
                         publishProgress(30);
                         try {
                             // Getting Array of Songs
                             MainActivity.contacts = json.getJSONArray("canciones");
                             publishProgress(35);
 
                             // looping through All Songs
                             publishProgress(37);
                             DB dbHelper = new DB(getBaseContext());
                             SQLiteDatabase db = dbHelper.getWritableDatabase();
                             if(!dbHelper.ifTableExists(db, "canciones"))
                                 db.execSQL(DB_entry.CREATE_CANCIONES);
                             for(int i = 0; i < MainActivity.contacts.length(); i++){
                                 JSONObject c = MainActivity.contacts.getJSONObject(i);
                                 ContentValues values = new ContentValues();
 
                                 int counter = 40 + ((i*100/MainActivity.contacts.length())/2);
                                 publishProgress(counter);
                                 // Storing each json item in variable
                                 int id = c.getInt("id");
                                 String archivo = c.getString("archivo");
                                 String titulo = c.getString("titulo");
                                 String artista = c.getString("artista");
                                 String album = c.getString("album");
                                 String duracion = c.getString("duracion");
 
                                 // creating new HashMap
                                 HashMap<String, String> map = new HashMap<String, String>();
 
                                 // adding each child node to HashMap key => value
                                 map.put("id", ""+id);
                                 map.put("titulo", titulo);
                                 map.put("artista", artista);
                                 map.put("album", album);
                                 map.put("archivo", "http://"+MainActivity.url+""+archivo);
                                 map.put("duracion", duracion);
                                 
                                 //DB
                                 values.put(DB_entry.COLUMN_NAME_ID, id);
                                 values.put(DB_entry.COLUMN_NAME_ARCHIVO, archivo);
                                 values.put(DB_entry.COLUMN_NAME_TITULO, titulo);
                                 values.put(DB_entry.COLUMN_NAME_ARTISTA, artista);
                                 values.put(DB_entry.COLUMN_NAME_ALBUM, album);
                                 values.put(DB_entry.COLUMN_NAME_DURACION, duracion);
 
                                 // adding HashList to ArrayList
                                 contactList.add(map);
                                 
                                 //Adding data into DB
                                 db.insert(DB_entry.TABLE_CANCIONES, "null", values);
                             }
                             dbHelper.actualizarAcceso(db, "canciones", System.currentTimeMillis());
                         } catch (JSONException e) {
                             e.printStackTrace();
                             Log.i("com.melchor629.musicote","Excepción encontrada: "+e.toString());
                         }
                         publishProgress(90);
                     }
 
                     publishProgress(95);
                     return contactList;
                 }else{
                     return null;
                 }
             }
         }
 
         @Override
         protected void onProgressUpdate(Integer... progress){
             progressDialog.setProgress(progress[0]);
         }
 
         @Override
         protected void onPostExecute(ArrayList<HashMap<String, String>> result){
             super.onPostExecute(result);
             try {
                 publishProgress(99);
             } catch (Exception e) {}
         }
     }
 
     //SearchBar methods
     @Override
     public boolean onSuggestionSelect(int position) {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public boolean onSuggestionClick(int position) {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public boolean onQueryTextSubmit(String query) {
         // TODO Auto-generated method stub
         return false;
     }
 
     @Override
     public boolean onQueryTextChange(String newText) {
         // TODO Auto-generated method stub
         return false;
     }
 
     private class SuggestionsAdapter extends CursorAdapter {
 
         public SuggestionsAdapter(Context context, Cursor c) {
             super(context, c, 0);
         }
 
         @Override
         public View newView(Context context, Cursor cursor, ViewGroup parent) {
             LayoutInflater inflater = LayoutInflater.from(context);
             View v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
             return v;
         }
 
         @Override
         public void bindView(View view, Context context, Cursor cursor) {
             TextView tv = (TextView) view;
             final int textIndex = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1);
             tv.setText(cursor.getString(textIndex));
         }
     }
 
     //Desastre de la carga de datos
     /**
      * Si tiene que hacer la descarga
      */
     private void async() {
         new Thread(new Runnable(){
             @Override
             public void run() {
                 Looper.prepare();
                 try {
                     AsyncTask<Void, Integer, ArrayList<HashMap<String, String>>> asd = new JSONParseDialog().execute();
                     contactList = asd.get();
                 } catch (InterruptedException e) {
                     Log.e("AsyncTask","AsyncTask not finished: "+e.toString());
                     e.printStackTrace();
                 } catch (Exception e) {
                     Log.e("AsyncTask","AsyncTask not finished: "+e.toString());
                     e.printStackTrace();
                 }
                 MainActivity.this.runOnUiThread(
                     new Runnable(){
                         @Override
                         public void run(){
                             sis();
                             try {
                                 this.finalize();
                             } catch (Throwable e) {
                                 Log.e(MainActivity.EXTRA_MESSAGE, "Error: "+ e.toString());
                             }
                         }
                     }
                 );
                 try {
                     progressDialog.dismiss();
                     this.finalize();
                 } catch (Throwable e) {
                     Log.e("UIUpdate" ,"Error: "+ e.toString());
                 }
             }
         }).start();
     }
 
     /**
      * Si solo carga desde la base de datos
      */
    private void cursordb() {
         // Define a projection that specifies which columns from the database
         // you will actually use after this query.
         String[] projection = {
             DB_entry.COLUMN_NAME_ID,
             DB_entry.COLUMN_NAME_TITULO,
             DB_entry.COLUMN_NAME_ARTISTA,
             DB_entry.COLUMN_NAME_ALBUM,
             DB_entry.COLUMN_NAME_DURACION,
             DB_entry.COLUMN_NAME_ARCHIVO
         };
 
         // How you want the results sorted in the resulting Cursor
         String sortOrder =
         DB_entry.COLUMN_NAME_ID + " ASC";
 
         Cursor c = db.query(
                             DB_entry.TABLE_CANCIONES,                    // The table to query
                             projection,                               // The columns to return
                             null,                                     // The columns for the WHERE clause
                             null,                                     // The values for the WHERE clause
                             null,                                     // don't group the rows
                             null,                                     // don't filter by row groups
                             sortOrder                                 // The sort order
                             );
         contactList = new ArrayList<HashMap<String, String>>();
 
         c.moveToFirst();
         try{
             do {
                 // creating new HashMap
                 HashMap<String, String> map = new HashMap<String, String>();
 
                 long id = c.getLong(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_ID));
                 String titulo = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_TITULO));
                 String artista = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_ARTISTA));
                 String album = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_ALBUM));
                 String archivo = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_ARCHIVO));
                 String duracion = c.getString(c.getColumnIndexOrThrow(DB_entry.COLUMN_NAME_DURACION));
 
                 // adding each child node to HashMap key => value
                 map.put("id", ""+id);
                 map.put("titulo", titulo);
                 map.put("artista", artista);
                 map.put("album", album);
                 map.put("archivo", "http://"+MainActivity.url+""+archivo);
                 map.put("duracion", duracion);
 
                 contactList.add(map);
             } while(c.moveToNext());
         } catch(CursorIndexOutOfBoundsException e) {
             db.execSQL(DB_entry.DELETE_CANCIONES);
             Log.e("DB", "Mala integridad de la BD");
             try {
                 this.finalize();
             } catch (Throwable e1) {
                 Log.e("error","Error: "+ e1.toString());
             }
         }
         progressDialog.dismiss();
         c.close();
         sis();
     }
 }
