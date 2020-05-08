 package org.lvlv.supersearch;
 
 import static android.provider.BaseColumns._ID;
 import static org.lvlv.supersearch.Constants.*;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteCursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Spinner;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class SuperSearch extends Activity implements OnClickListener,OnKeyListener, OnItemSelectedListener
 {
 	private Button goButton;
 	private Spinner location;
 	private EditText inputText;
 	private SearchesData searches;
 	private SharedPreferences settings; 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		goButton = (Button) findViewById(R.id.go_button);
 		location = (Spinner) findViewById(R.id.search_location);
 		inputText = (EditText) findViewById(R.id.search_input);
 		
 		goButton.setOnClickListener(this);
 		location.setOnKeyListener(this);
 		inputText.setOnKeyListener(this);
 		location.setOnItemSelectedListener(this);
 
 		
 		searches = new SearchesData(this);
 		SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);
 		if (true){ //!preferences.getBoolean(FIRST_RUN, true)) {
 			this.startActivityForResult(new Intent(this, WizardActivity.class),	REQUEST_EULA);
 		}
 		
 		populateFields();
 	}
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 	   super.onSaveInstanceState(outState);
 	}
 	@Override
 	protected void onRestoreInstanceState(Bundle inState) {
 		super.onRestoreInstanceState(inState);
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 	@Override
 	protected void onStart() {
 		super.onStart();
 		setupSearches();
 	}
 
 	private void setupSearches() {
 		try {
 			Cursor cursor = getSearches();
 			String[] from = new String[] { Constants.NAME, Constants.URL };
 			int[] to = new int[] { R.id.name, R.id.url };
 	        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.spinnerrow, cursor, from, to  );
 			adapter.setDropDownViewResource(R.layout.spinnerrow);
 	        location.setAdapter(adapter);
 		} finally {
 		   searches.close();
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 
 		MenuItem help = menu.add(R.string.list_menu_help);
 		help.setIcon(android.R.drawable.ic_menu_help);
 		help.setIntent(new Intent(SuperSearch.this, HelpActivity.class));
 		
 		MenuItem settings = menu.add(R.string.manage_label);
 		settings.setIcon(android.R.drawable.ic_menu_preferences);
 		settings.setIntent(new Intent(SuperSearch.this, ModifySearches.class));
 		
 		return true;
 		
 	}
 
 	public void onItemSelected(AdapterView<?> adapterView, View view, int arg2,
 			long arg3) {
 		populateFields();
 
 	}
 
 	private void populateFields() {
 		SQLiteCursor selection =  (SQLiteCursor) location.getSelectedItem();
 		if (selection != null) {
 			goButton.setText(selection.getString(3));
 		}
 			
 	}
 
 	
 	private void doSearch() {
 		SQLiteCursor selection =  (SQLiteCursor) location.getSelectedItem();
 		if (selection != null) {
 			Uri uri = Uri.parse(selection.getString(2).replaceAll("%s",
					inputText.getText().toString()));
 			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
 			startActivity(intent);
 		}
 	}
 	public final static int REQUEST_EULA = 1;
 	
 	private static String[] FROM = { _ID, NAME, URL, TERM};
 	private static String ORDER_BY = NAME + " ASC" ;
 	private Cursor getSearches() {
 	   SQLiteDatabase db = searches.getReadableDatabase();
 	   Cursor cursor = db.query(TABLE_NAME, FROM, null, null, null,
 	         null, ORDER_BY);
 	   startManagingCursor(cursor);
 	   return cursor;
 	}
 
 	
 	public void onClick(View v) {
 			doSearch();
 	}
 
 	public boolean onKey(View v, int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_ENTER) {
 			doSearch();
 			return true;
 		}
 		return false;
 	}
 
 	public void onNothingSelected(AdapterView<?> arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 		switch(requestCode) {
 		case REQUEST_EULA:
 			if(resultCode == Activity.RESULT_OK) {
 				// yay they agreed, so store that info
 				searches.addSearch("Answers.com", "http://answers.com/%s", "Search");
 				searches.addSearch("Google", "http://google.com/search?q=%s", "Search");
 				searches.addSearch("Wikipedia", "http://en.wikipedia.org/wiki/Special:Search?search=%s", "Search");
 				searches.addSearch("Merriam-Webster", "http://www.merriam-webster.com/dictionary/%s", "Define");
 				settings = getSharedPreferences(PREFS_NAME, 0);
 				SharedPreferences.Editor editor = settings.edit();
 				editor.putBoolean(FIRST_RUN, false);
 				editor.commit();
 			} else {
 				// user didnt agree, so close
 				this.finish();
 			}
 			break;
 				
 		}
 		
 	}
 
 }
