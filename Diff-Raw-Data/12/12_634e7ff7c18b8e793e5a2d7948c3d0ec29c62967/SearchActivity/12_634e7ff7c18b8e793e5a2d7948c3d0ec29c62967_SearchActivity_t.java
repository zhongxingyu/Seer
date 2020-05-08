 package com.ijuru.kumva.activity;
 
 import java.util.List;
 
 import com.ijuru.kumva.Definition;
 import com.ijuru.kumva.R;
 import com.ijuru.kumva.search.OnlineSearch;
 import com.ijuru.kumva.ui.DefinitionListAdapter;
 import com.ijuru.kumva.util.Utils;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class SearchActivity extends Activity {
 	private DefinitionListAdapter adapter;
 	private ProgressDialog progressDialog;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
     	setContentView(R.layout.search);
         
         ListView listResults = (ListView)findViewById(R.id.listresults);
         EditText txtQuery = (EditText)findViewById(R.id.queryfield);
         
         this.adapter = new DefinitionListAdapter(this);
         listResults.setAdapter(adapter);
         
         txtQuery.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 			@Override
 			public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
 				if (actionId == EditorInfo.IME_ACTION_SEARCH || event.getAction() == KeyEvent.ACTION_DOWN)
 					doSearch(view);
 					
 				return true;
 			}
 		});
         
         listResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				Definition definition = (Definition)parent.getItemAtPosition(position);
 				((KumvaApplication)getApplication()).setDefinition(definition);
 				
 				Intent intent = new Intent(getApplicationContext(), EntryActivity.class);
 				startActivity(intent);
 			}
 		});
         
         // Check for an initial query passed via the intent
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
 	        String initQuery = (String)extras.get("query");
 	        if (initQuery != null) {
 	        	txtQuery.setText(initQuery);
 	        	doSearch(null);
 	        }
         }
     }
     
     /**
      * Performs a dictionary search
      * @param view the view
      */
     public void doSearch(View view) {
     	// Clear status message
     	setStatusMessage(null);
     	
     	// Get query
     	EditText txtQuery = (EditText)findViewById(R.id.queryfield);
     	String query = txtQuery.getText().toString();
     	
     	// Hide on-screen keyboard
     	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
     	imm.hideSoftInputFromWindow(txtQuery.getWindowToken(), 0);
     	
     	progressDialog = ProgressDialog.show(this, getString(R.string.str_searching), getString(R.string.str_pleasewait));
     	
     	adapter.clear();
     	
     	new OnlineSearch(this).execute(query);
     }
 
 	public void searchFinished(List<Definition> results) {
 		// Hide the progress dialog
 		if (progressDialog != null)
 			progressDialog.dismiss();
 		
		if (results == null)
			setStatusMessage(getString(R.string.err_communicationfailed));
		else if (results.size() == 0)
 			setStatusMessage(getString(R.string.str_noresults));
		else {
			for (Definition definition : results)
				adapter.add(definition);
		}
 	}
 	
 	private void setStatusMessage(String message) {
 		TextView txtStatus = (TextView)findViewById(R.id.statusmessage);
   
     	if (message == null) 
     		txtStatus.setVisibility(View.GONE);
     	else {
     		txtStatus.setText(message);
 	    	txtStatus.setVisibility(View.VISIBLE);
     	}
 	}
 
 	/**
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.search, menu);
 	    return true;
 	}
 
 	/**
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 	    case R.id.menuabout:
 	    	onMenuAbout();
 		}
 		return true;
 	}
 
 	/**
 	 * Displays the about dialog
 	 */
 	private void onMenuAbout() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		String title = getString(R.string.app_name) + " " + Utils.getVersionName(this);
 		String message = "Thank you for downloading Kumva\n" +
 				"\n" +
 				"If you have any problems please contact rowan@ijuru.com";
 		
 		builder.setTitle(title);
 		builder.setMessage(message);
 		builder.show();
 	}	
 }
