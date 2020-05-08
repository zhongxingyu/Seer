 package org.apelikecoder.bgdictum;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.preference.PreferenceManager;
 import android.text.TextUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.CursorAdapter;
 import android.widget.Toast;
 
 public class BGDictum extends Activity implements DB,
         OnItemClickListener, View.OnClickListener {
 
     private SQLiteDatabase db;
     private MyAutoCompleteTextView searchField;
     private Button clear;
     private WordView wordInfoArea;
     private InputMethodManager mgr;
     private App app;
     private Cursor historyCursor;
     private WordAdapter wordAdapter;
 
     public static final String FINISH_MSG = "start_fataility";
 
     private int TRANSLATION_COLUMN_INDEX = -1;
     private int TRANSCRIPTION_COLUMN_INDEX = -1;
     private static final int ID_MENU_PREFS = 102;
     private static final int ID_MENU_HISTORY = 103;
     private static final int DLG_HISTORY = 1111;
     private static final int DLG_CONFIRM_NOSDCARD = 2222;
     private boolean skipExit;
 
 
     private boolean mClearHistory;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         App.setupTheme(this);
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         searchField = (MyAutoCompleteTextView) findViewById(R.id.search_edit_query);
         wordInfoArea = (WordView) findViewById(R.id.description_text);
         wordInfoArea.setPopup((PopupView)findViewById(R.id.popup));
         clear = (Button) findViewById(R.id.clear_text);
         clear.setOnClickListener(this);
         mgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
         app = (App) getApplication();
 
         if (app.getDb() == null &&
                 !Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
             showDialog(DLG_CONFIRM_NOSDCARD);
             skipExit = true;
        } else {
            postCreateCheckDB();
         }
     };
 
     private void postCreateCheckDB() { 
         if (!checkDB()) {
             startActivity(new Intent(this, Setup.class));
             finish();
         } else {
             String last = (String) getLastNonConfigurationInstance();
             if (last == null) {
                 Cursor c = app.getRecentConnector().getSearchCursor(null);
                 if (c.moveToFirst())
                     last = c.getString(c.getColumnIndex(RecentSearchesConnector.ITEM));
                 c.close();
             }
             if (!TextUtils.isEmpty(last))
                 searchForText(last);
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         if (app.getDb() == null && !skipExit) {
             skipExit = false;
             Toast.makeText(this, R.string.can_t_find_db_is_your_sdcard_mounted, Toast.LENGTH_SHORT).show();
             finish();
             return;
         }
         loadSettings();
     }
 
     private void loadSettings() {
         SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
         mClearHistory = sp.getBoolean(App.PreferenceKeys.preference_clear_history_on_exit, false);
         wordInfoArea.setClickEnabled(sp.getBoolean(App.PreferenceKeys.preference_enable_word_click, false));
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         if (isFinishing()) {
             if (mClearHistory)
                 app.getRecentConnector().clearSearchHistory();
         }
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         super.onNewIntent(intent);
         Uri uri = intent.getData();
         if (uri != null) {
             // System.out.println(uri.toString());
             String word = uri.getHost();
             //searchField.setText(word);
             //
             searchForText(word);
         } else {
             Bundle extras = intent.getExtras();
             if (extras != null && extras.getBoolean((FINISH_MSG))) {
                 finish();
             }
         }
     }
     
     private boolean checkDB() {
         App app = (App) getApplication();
         db = app.getDb();
         if (db == null) {
             if (!app.setupDB())
                 return false;
             db = app.getDb();
         }
         try {
             Cursor c = db.query(TABLE_WORDS, null, null, null, null, null, null);
             CursorAdapter ca = wordAdapter = new WordAdapter(this, c, db);
             searchField.setAdapter(ca);
             searchField.setOnItemClickListener(this);
             c = db.query(TABLE_TRANSLATIONS, null, null, null, null, null, null);
             TRANSLATION_COLUMN_INDEX = c.getColumnIndexOrThrow(COLUMN_TRANSLATION);
             TRANSCRIPTION_COLUMN_INDEX = c.getColumnIndexOrThrow(COLUMN_TRANSCRIPTION);
             c.close();
         } catch (SQLiteException ex) {
             ex.printStackTrace();
             return false;
         }
         return true;
     }
 
     private void searchForText(String word) {
         Cursor c = wordAdapter.runQueryOnBackgroundThread(word);
         boolean set = false;
         if (c.moveToFirst()) {
             if (c.getString(c.getColumnIndex(COLUMN_WORD)).equals(word)) {
                 searchField.setText(word, false);
                 String res[] = getTranslation(c.getInt(c.getColumnIndex(DB.COLUMN_ID)));
                 if (res != null) {
                     setWordInfo(word, res[1], res[0]);
                     set = true;
                 }
             }
         }
         if (!set)
             searchField.setText(word);
         searchField.setSelection(word.length());
         c.close();
     }
 
     private void setWordInfo(String word, String translation, String info) {
         wordInfoArea.setWordInfo(word, translation, info);
         app.getRecentConnector().addSearch(word);
         mgr.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
     }
 
     public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
         String[] s = getTranslation(id);
         if (s != null) {
             String word = searchField.getText().toString();
             setWordInfo(word, s[1], s[0]);
         }
     }
 
     private String[] getTranslation(long id) {
         Cursor c = db.query(TABLE_TRANSLATIONS, null, COLUMN_WORD_ID + "='" + id + "'", null, null, null, null);
         String[] res = null;
         if (c.moveToFirst()) {
             res = new String[2];
             res[0] = c.getString(TRANSLATION_COLUMN_INDEX);
             res[1] = c.getString(TRANSCRIPTION_COLUMN_INDEX);
         }
         c.close();
         return res;
     } 
 
     public void onClick(View v) {
         searchField.setText("");
         showKeyboard();
     }
 
     private void showKeyboard() {
         mgr.showSoftInput(searchField, /*InputMethodManager.SHOW_FORCED*/0);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuItem m;
         //m = menu.add(Menu.NONE, ID_MENU_PREFERENCES, Menu.NONE, R.string.preferences);
         //m.setIcon(android.R.drawable.ic_menu_preferences);
         m = menu.add(Menu.NONE, ID_MENU_HISTORY, Menu.NONE, R.string.history);
         m.setIcon(android.R.drawable.ic_menu_view);
         m = menu.add(Menu.NONE, ID_MENU_PREFS, Menu.NONE, R.string.menu_preferences);
         m.setIcon(android.R.drawable.ic_menu_preferences);
         return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case ID_MENU_HISTORY:
                 showDialog(DLG_HISTORY);
                 return true;
             case ID_MENU_PREFS:
                 startActivity(new Intent(this, Preferences.class));
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         if (DLG_HISTORY == id) {
             historyCursor = app.getRecentConnector().getSearchCursor(null);
             startManagingCursor(historyCursor);
             return new AlertDialog.Builder(this)
                 .setCursor(historyCursor, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         if (historyCursor.moveToPosition(which)) {
                             String word = historyCursor.getString(historyCursor.getColumnIndex(RecentSearchesConnector.ITEM));
                             searchForText(word);
                         }
                     }
                 }, RecentSearchesConnector.ITEM)
                 .setTitle(R.string.history)
                 .create();
         } else if (DLG_CONFIRM_NOSDCARD == id) {
             return new AlertDialog.Builder(this)
                 .setTitle(R.string.warning)
                 .setMessage(R.string.no_sdcard_setup_message)
                 .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         postCreateCheckDB();
                     }
                 })
                 .setNegativeButton(R.string.quit, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         finish();
                     }
                 }).create();
         }
         return super.onCreateDialog(id);
     }
 
     @Override
     protected void onPrepareDialog(int id, Dialog dialog) {
         super.onPrepareDialog(id, dialog);
         if (id == DLG_HISTORY) {
             historyCursor.requery();
         }
     }
 
     @Override
     public Object onRetainNonConfigurationInstance() {
         return searchField.getText().toString();
     }
 }
