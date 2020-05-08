 package com.hacku.swearjar;
 
 import java.math.BigDecimal;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.net.ConnectivityManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Toast;
 
 /**
  * Initial activity to bootstrap application.
  * 
  * @author Calum
  */
 public class MainLayoutActivity extends ListActivity {
 
 	private static final String JUST_GIVING_URI = "http://www.justgiving.com/donation/direct/charity/#1?frequency=single&amount=#2";
 	private TelephonyManager teleManager;
 	private PhoneStateListener callListener;
 	private Button payButton;
 	private BlackListArrayAdapter listAdapter;
 
 	private int lastSelectedIndex;
 	public static final int UPDATE_BLACK_LIST_REQUEST = 0; //Pass when starting activity to the black list
 
 	private SwearJarApplication application;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_layout);
 		
 		application = (SwearJarApplication) getApplication();
 
 		// Setup ListAdapter
 		ArrayList<BlackListItem> blackListWords = (ArrayList<BlackListItem>) application.getBlackListItems(); // Get blacklisted words from
 										// SwearJarApplication
 		listAdapter = new BlackListArrayAdapter(this, blackListWords);
 		setListAdapter(listAdapter);
 		ListView lv = getListView();
 		lv.setTextFilterEnabled(true);
 		registerForContextMenu(lv); // Context menu for edit/delete choices
 
 		// Setup onClickListener for Pay button
 		payButton = (Button) findViewById(R.id.payButton);
 		payButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// Pay through JUST GIVING!!!!!
 
 				if (!hasInternetConnectivity()) {
 					Toast.makeText(MainLayoutActivity.this,
 							"Internet Access is required to pay.",
 							Toast.LENGTH_LONG).show();
 					return;
 				}
 
 				goToJustGiving("2357"); // TODO: Search charities
 			}
 
 			/**
 			 * Sends the phone's browser to the just giving page for the charity
 			 * with charityId. Extracts the total cost due and sends that as a
 			 * uri parameter.
 			 */
 			private void goToJustGiving(String charityId) {
 				// Loop through words getting total cost
 				BigDecimal totalCost = application.getTotalCostDue();
 				NumberFormat formatter = NumberFormat.getNumberInstance();
 				formatter.setMaximumFractionDigits(2);
 
 				// Set up uri
 				String webPage = JUST_GIVING_URI.replace("#1", charityId);
 				webPage = webPage.replace("#2", formatter.format(totalCost));
 
 				// Start a web browser to go to JustGiving home page
 				Intent intent = new Intent(Intent.ACTION_VIEW);
 				intent.setData(Uri.parse(webPage));
 				startActivity(intent);
 			}
 		});
 
 		// Set up onLongClickListener for list items...
 		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				ArrayList<BlackListItem> blackListWords = (ArrayList<BlackListItem>) application.getBlackListItems();
 				lastSelectedIndex = position;
 				openContextMenu(parent);
 				return true;
 			}
 		});
 
 		// Listen for PhoneCalls
 		callListener = new PhoneCallListener(this);
 		teleManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
 
 		// Register Listener to be notified of changes to the phone call state.
 		teleManager.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);
 
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 
 		// TODO: Unregister listener and clean up resources etc.
 		teleManager.listen(callListener, PhoneStateListener.LISTEN_NONE); // Unregister
 	}
 
 	// Check for Internet access
 	public boolean hasInternetConnectivity() {
 		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
 		if (cm.getActiveNetworkInfo().isConnectedOrConnecting() == false)
 			return false;
 		return true;
 	}
 
 	// ------------MENU STUFF-------------
 
 	// What to do when hard 'MENU' button is clicked
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu_layout, menu); // Inflate menu.xml layout
 		return true;
 	}
 
 	// What to do when 'MENU' options selected
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menuItemPrefs:
 			startActivityForResult(new Intent(this, AddWordActivity.class), UPDATE_BLACK_LIST_REQUEST);
 			break;
 		}
 		return true;
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 
 		menu.setHeaderTitle("Options");
 		String[] menuItems = getResources()
 				.getStringArray(R.array.context_menu);
 		for (int i = 0; i < menuItems.length; i++)
 			menu.add(Menu.NONE, i, i, menuItems[i]);
 
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		int menuItemIndex = item.getItemId();
 		String[] menuItems = getResources()
 				.getStringArray(R.array.context_menu);
 		String menuItemName = menuItems[menuItemIndex];
 
 		if (menuItemName.equals("Edit")) // TODO: Get names more extensibly?
 		{
 			Intent intent = new Intent(this, EditWordActivity.class);
 			intent.putExtra("blackListItemIndex", lastSelectedIndex);
 			startActivityForResult(intent, UPDATE_BLACK_LIST_REQUEST);
 		} else if (menuItemName.equals("Delete")) {
 			ArrayList<BlackListItem> blackListWords = (ArrayList<BlackListItem>) application.getBlackListItems();
 
 			// Delete word
 			application.removeBlackListItem(lastSelectedIndex);
 
 			// Refresh list (adapter)
 			listAdapter.notifyDataSetChanged();
 		}
 
 		return true;
 	}
 
 	/**
 	 * Update the list when returning to this activity from an edit request
 	 */
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (requestCode == UPDATE_BLACK_LIST_REQUEST) {
 			if (resultCode == RESULT_OK) {
 				listAdapter.notifyDataSetChanged();
 			}
 		}
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		listAdapter.notifyDataSetChanged(); //Refresh list
 	}
 
 }
