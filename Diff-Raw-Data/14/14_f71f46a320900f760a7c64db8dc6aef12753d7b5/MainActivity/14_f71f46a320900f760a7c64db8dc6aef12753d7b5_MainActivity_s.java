 package com.googlemail.micmunze.miwotreff;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.CalendarContract;
 import android.provider.CalendarContract.Events;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ListView;
 import android.widget.Toast;
 
 /**
  * Main Activity with the features of the app.
  *
  * @author Michael Munzert
  * @version 1.0, 11.08.2012
  */
 public class MainActivity extends ListActivity {
    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_EDIT = 1;
    //   private static final String TAG = "MiWoTreff";
    private DbAdapter mDbHelper; // Database Helper
    
    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);
       mDbHelper = new DbAdapter(this);
       try {
          mDbHelper.open();
       } catch (SQLException s) {
          Toast.makeText
          (this, R.string.db_open_error, Toast.LENGTH_SHORT).show();
          return;
       }
       fillData();
       registerForContextMenu(getListView());
    }
    
    /**
     * @see android.app.ListActivity#onPause()
     */
    @Override
    public void onPause() {  
       super.onPause();
    }
    
    /**
     * @see android.app.Activity#onResume()
     */
    @Override
    public void onResume() {
       super.onResume();
    }
    
    /**
     * @see android.app.ListActivity#onDestroy()
     */
    @Override
    public void onDestroy() {
       mDbHelper.close();
       super.onDestroy();
    }
    
    /**
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       getMenuInflater().inflate(R.menu.menu_main, menu);
       return true;
    }
    
    /**
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       switch (item.getItemId())  {
          case R.id.menu_search:
             onSearchRequested();
             return true;
          case R.id.menu_import:
             importProg();
             return true;
          case R.id.menu_add:
             addProg();
             return true;
          default:
             return super.onOptionsItemSelected(item);
       }
    }
    
    /**
     * Fills Data from Database in List.
     */
    private void fillData() {
       Cursor entryCursor = mDbHelper.fetchAllEntries(null);
       SpecialCursorAdapter adapter = new SpecialCursorAdapter(this,entryCursor);
       setListAdapter(adapter);
    }
    
    /**
     * Imports the program from the homepage of the "Evangelische Gemeinschaft".
     */
    private void importProg() {
       new ImportTask().execute(new Void[] {});
    }
    
    /**
     * Adds a new program entry.
     */
    private void addProg() {
       Intent i = new Intent(this, EditActivity.class);
       startActivityForResult(i, ACTIVITY_CREATE);
    }
    
    /**
     * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
       super.onListItemClick(l, v, position, id);
       // Edit the entry
       Intent i = new Intent(this, EditActivity.class);
       i.putExtra(DbAdapter.KEY_ROWID, id);
       startActivityForResult(i, ACTIVITY_EDIT);
    }
    
    /**
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, 
                                    Intent intent) {
       super.onActivityResult(requestCode, resultCode, intent);
       fillData();
    }
    
    /**
     * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, 
                                    ContextMenu.ContextMenuInfo menuInfo) {
       super.onCreateContextMenu(menu, v, menuInfo);
       MenuInflater inflater = getMenuInflater();
       inflater.inflate(R.menu.context_menu, menu);
    }
    
    /**
     * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
       AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
       
       switch (item.getItemId()) {
          case R.id.addToCal: // Add to google calendar
             add2Call(info);
             return true;
          default:
             return super.onContextItemSelected(item);
       }
    }
    
    /**
     * Adds the entry to the calendar.
     * 
     * @param  info
     *         Info about the entry.
     */
    private void add2Call(AdapterContextMenuInfo info) {
       Cursor c = mDbHelper.fetchEntry(info.id);
       // Date and time of the calendar entry
       long d = c.getLong(1);
       GregorianCalendar start = new GregorianCalendar();
       GregorianCalendar end = new GregorianCalendar();
       start.setTimeInMillis(d);
       start.set(GregorianCalendar.HOUR_OF_DAY, 19);
       start.set(GregorianCalendar.MINUTE, 30);
       end.setTimeInMillis(d);
       end.set(GregorianCalendar.HOUR_OF_DAY, 21);
       
       // title
      String title = "MiWoTreff: " + c.getString(2);
       // location
      String loc = "Möhlstraße 20, 80935 München";
       
       // Calendar: Insert per Intent
       Intent intent = new Intent(Intent.ACTION_INSERT)
       .setData(Events.CONTENT_URI)
       .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start.getTimeInMillis())
       .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end.getTimeInMillis())
       .putExtra(Events.TITLE, title)
       .putExtra(Events.EVENT_LOCATION, loc)
       .putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);
       startActivity(intent);
    }
    
    /**
     * Imports the program from Homepage asynchronous in the background.
     *
     * @author Michael Munzert
     * @version 1.0, 12.08.2012
     */
    private class ImportTask extends AsyncTask<Void, Void, Void>
    {
       /**
        * @see android.os.AsyncTask#doInBackground(Params[])
        */
       @Override
       protected Void doInBackground(Void... params) {
          HtmlParser parser = new HtmlParser();
          String table = parser.getHtmlFromUrl 
          ("http://www.gemeinschaft-muenchen.de/index.php?id=7&no_cache=1");
          ArrayList<HashMap<String, Object>> prog = parser.getProg(table);
          if (prog != null) {
             for (HashMap<String, Object> m : prog) {
                mDbHelper.createEntry((Date)m.get(DbAdapter.KEY_DATUM), 
                                      (String)m.get(DbAdapter.KEY_THEMA), 
                                      (String)m.get(DbAdapter.KEY_PERSON));
             }
          }
          return null;
       }
       
       /**
        * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
        */
       @Override
       protected void onPostExecute(Void result) {
          fillData();
       }
    }
 }
