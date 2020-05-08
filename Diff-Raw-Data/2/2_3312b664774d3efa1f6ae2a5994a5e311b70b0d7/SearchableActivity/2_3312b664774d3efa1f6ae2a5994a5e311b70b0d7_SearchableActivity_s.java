 package com.andriod.tailorassist;
 
 import android.app.ListActivity;
 import android.app.SearchManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.DragEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnDragListener;
 import android.view.View.OnTouchListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SearchView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SearchableActivity extends ListActivity {
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// setContentView(R.layout.activity_searchable);
 		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
 
 		// Get the intent, verify the action and get the query
 		getActionBar().setHomeButtonEnabled(true);
 		Intent intent = getIntent();
 
 		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
 			String query = intent.getStringExtra(SearchManager.QUERY);
 			doMySearch(query);
 			if (getListView().getCount() <= 0) {
 				Toast.makeText(this,
 						"No Customer Found with \"" + query + "\"",
 						Toast.LENGTH_LONG).show();
 			}
 		} else {
 			// listAll
 			Bundle extras = this.getIntent().getExtras();
 
 			Log.d("TEST", "extras " + extras);
 			if (extras != null) {
 				if (extras.getBoolean("listAll"))// != null &&
 													// "edit".equals(extras.getString("mode"))){
 				{
 					doMySearch("");
 					if (getListView().getCount() <= 0) {
 						Toast.makeText(this, "No Customer Found",
 								Toast.LENGTH_LONG).show();
 					}
 				} else if (extras.getString("searchedOn") != null) {
 					doMySearch(extras.getString("searchedOn"));
 					if (getListView().getCount() <= 0) {
 						goHome();
 					}
 				}
 			}
 		}
 
 		//
 		// this.setOnItemClickListener(new OnItemClickListener() {
 		//
 		// @Override
 		// public void onItemClick(AdapterView<?> parent, View view, int
 		// position, long id) {
 		// // // Build the Intent used to open WordActivity with a specific word
 		// Uri
 		// // Intent wordIntent = new Intent(getApplicationContext(),
 		// WordActivity.class);
 		// // Uri data = Uri.withAppendedPath(DictionaryProvider.CONTENT_URI,
 		// // String.valueOf(id));
 		// // wordIntent.setData(data);
 		// // startActivity(wordIntent);
 		//
 		// String custNumSelected =
 		// ((TextView)(view).findViewById(R.id.text2)).getText().toString();
 		// // int pos = custNumSelected.indexOf(':');
 		// // String custNum =custNumSelected.substring(pos+1);
 		// long custNumber = Long.parseLong(custNumSelected);
 		// Intent intent = new
 		// Intent(getApplicationContext(),ShowCustomerDetails.class);
 		// /* sending the customer details to next activity */
 		// Bundle bundle = new Bundle();
 		// bundle.putString("newCustmerNumber", custNumSelected);
 		// intent.putExtras(bundle);
 		// //start the activity
 		// startActivity(intent);
 		// }
 		// });
 		com.andriod.tailorassist.screen.Styles.setActionBarStyle(
 				getActionBar(), getResources());
 	}
 
 	// @Override
 	// protected void onNewIntent(Intent intent) {
 	// adapter.clear();
 	// setIntent(intent);
 	// performSearch();
 	// }
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the options menu from XML
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.options_menu, menu);
 
 		// Get the SearchView and set the searchable configuration
 		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
 		SearchView searchView = (SearchView) menu.findItem(R.id.search)
 				.getActionView();
 		ComponentName cn = getComponentName();
 		// cn = new ComponentName(getApplicationContext(),
 		// "SearchableActivity");
 		// Log.d("ComponentName", "ComponentName :"+cn);
 		searchView.setSearchableInfo(searchManager
 				.getSearchableInfo(getComponentName()));
 		searchView.setIconifiedByDefault(false); // Do not iconify the widget;
 													// expand it by default
 		int id = searchView.getContext().getResources()
 				.getIdentifier("android:id/search_src_text", null, null);
 		TextView textView = (TextView) searchView.findViewById(id);
 		textView.setWidth(400);
 		searchView.requestFocus();
 		return true;
 	}
 
 	public void doMySearch(final String query) {
 
 		// Query for all people contacts using the Contacts.People convenience
 		// class.
 		// Put a managed wrapper around the retrieved cursor so we don't have to
 		// worry about
 		// requerying or closing it as the activity changes state.
 		CustomerTable customerTable = new CustomerTable(getApplicationContext());
 		customerTable.open();
 		Cursor mCursor = customerTable.searchCustomer(query);
 		// customerTable.close();
 		startManagingCursor(mCursor);
 
 		// Now create a new list adapter bound to the cursor.
 		// SimpleListAdapter is designed for binding to a Cursor.
 
 		ListAdapter adapter = new SimpleCursorAdapter(this, // Context.
 				R.layout.list_row, // Specify the row template to use (here, two
 									// columns bound to the two retrieved cursor
 				// rows).
 				mCursor, // Pass in the cursor to bind to.
 				new String[] { CustomerTable.KEY_ROWID, CustomerTable.KEY_NAME,
 						CustomerTable.KEY_MOBILE }, // Array of cursor columns
 													// to bind to.
 				// new int[] {R.id.text2, R.id.text1}); // Parallel array of
 				// which template objects to bind to those columns.
 				new int[] { R.id.custId, R.id.title, R.id.details }); // Parallel
 																		// array
 																		// of
 																		// which
 																		// template
 																		// objects
 																		// to
 																		// bind
 																		// to
 																		// those
 																		// columns.
 		
 		// Bind to our new adapter.
 		setListAdapter(adapter);
 
 		BitmapDrawable listItemDivider = new BitmapDrawable(
 				BitmapFactory.decodeResource(getResources(),
 						R.drawable.list_view_separator));
 		BitmapDrawable listBackground = new BitmapDrawable(
 				BitmapFactory.decodeResource(getResources(),
 						R.drawable.bg_tylor));
 		ListView listView = getListView();
 
 		listView.setDivider(listItemDivider);
		listView.setBackground(listBackground);
 		// if(listView.getCount()<=0){
 		// TextView empty = (TextView) findViewById(android.R.id.empty);
 		// if (query != null && query.trim().length() > 0)
 		// empty.setText("No customer found with " + query);
 		// else
 		// empty.setText("No customer found");
 		// listView.setEmptyView(empty);
 		// listView.getEmptyView().setEnabled(true);
 		// }
 		listView.setOnItemClickListener(new OnItemClickListener() {
 			//
 			// @Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				// // Build the Intent used to open WordActivity with a specific
 				// word Uri
 				// Intent wordIntent = new Intent(getApplicationContext(),
 				// WordActivity.class);
 				// Uri data =
 				// Uri.withAppendedPath(DictionaryProvider.CONTENT_URI,
 				// String.valueOf(id));
 				// wordIntent.setData(data);
 				// startActivity(wordIntent);
 
 				String custNumSelected = ((TextView) (view)
 						.findViewById(R.id.custId)).getText().toString();
 				Log.d("on item click", "custNumSelected : " + custNumSelected);
 				// int pos = custNumSelected.indexOf(':');
 				// String custNum =custNumSelected.substring(pos+1);
 				// long custNumber = Long.parseLong(custNumSelected);
 				Intent intent = new Intent(getApplicationContext(),
 						ShowCustomerDetails.class);
 				/* sending the customer details to next activity */
 				Bundle bundle = new Bundle();
 				bundle.putString("newCustmerNumber", custNumSelected);
 				bundle.putString("searchedOn", query);
 				intent.putExtras(bundle);
 				// start the activity
 				startActivity(intent);
 			}
 		});
 		/*
 		 * listView.setOnDragListener(new OnDragListener() {
 		 * 
 		 * @Override public boolean onDrag(View v, DragEvent event) {
 		 * 
 		 * Toast.makeText(getParent(), "Testing.....drag", Toast.LENGTH_LONG);
 		 * return false; } });
 		 */
 		/*listView.setOnTouchListener(new OnTouchListener() {
 			float historicX = Float.NaN;
 			float historicY = Float.NaN;
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 					historicX = event.getX();
 					historicY = event.getY();
 					break;
 				case MotionEvent.ACTION_UP:
 					if (event.getX() - historicX > -40) {
 						// left
 						// Toast.makeText(getParent(), "Do you want to delete?",
 						// Toast.LENGTH_LONG);
 					} else if (event.getX() - historicX > 40) {
 						// right
 						String custNumSelected = ((TextView) (v)
 								.findViewById(R.id.custId)).getText()
 								.toString();
 						Log.d("on item click", "custNumSelected : "
 								+ custNumSelected);
 						// int pos = custNumSelected.indexOf(':');
 						// String custNum =custNumSelected.substring(pos+1);
 						// long custNumber = Long.parseLong(custNumSelected);
 						Intent intent = new Intent(getApplicationContext(),
 								ShowCustomerDetails.class);
 						// sending the customer details to next activity 
 						Bundle bundle = new Bundle();
 						bundle.putString("newCustmerNumber", custNumSelected);
 						bundle.putString("searchedOn", query);
 						intent.putExtras(bundle);
 						// start the activity
 						startActivity(intent);
 						return true;
 
 					}
 					break;
 
 				}
 				// Toast.makeText(getParent(), "Testing.....touch",
 				// Toast.LENGTH_LONG);
 				return false;
 			}
 		});*/
 		// TextView empty = (TextView)findViewById(R.id.empty);
 		// if(query != null && query.trim().length()>0)
 		// empty.setText("No customer found with "+query);
 		// else
 		// empty.setText("No customer found");
 		// getListView().getEmptyView().setEnabled(true);
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 
 		case android.R.id.home:
 			// app icon in action bar clicked; go home
 			goHome();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 
 	}
 
 	public void goHome() {
 		Intent intent = new Intent(this, Matrix.class);
 		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		startActivity(intent);
 	}
 
 }
