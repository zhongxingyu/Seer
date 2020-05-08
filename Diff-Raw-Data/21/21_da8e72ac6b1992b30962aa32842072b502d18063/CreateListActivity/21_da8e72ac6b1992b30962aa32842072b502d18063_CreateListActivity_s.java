 package edu.pitt.cs1635group3.Activities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import service.CustomListTask;
 
 import zebrafish.util.DBHelper;
 import zebrafish.util.JSONCustomList;
 import zebrafish.util.JSONItem;
 import zebrafish.util.UIUtil;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.EditText;
 import android.widget.ListAdapter;
 import android.widget.SimpleAdapter;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 import edu.pitt.cs1635group3.R;
 import edu.pitt.cs1635group3.Activities.Classes.CustomList;
 import edu.pitt.cs1635group3.Activities.Classes.Item;
 import edu.pitt.cs1635group3.Activities.Classes.User;
 
 public class CreateListActivity extends SherlockListActivity {
 
 	private ArrayList<HashMap<String, String>> mylist;
 	private static int newListPK;
 	private EditText listNameSpace, CIDSpace;
 	private String listName, CID;
 	public boolean PUSH_TO_CLOUD = true;
 
 	
 	private ArrayList<Integer> newItemPKs; // track the new slices of the web
 											// servers we allocate (in case of
 											// cancel)
 
 	private static Context context;
 	private static String TAG = "CreateListActivity";
 
 	private static DBHelper db;
 	private static int userID;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.editlist);
 		context = this;
 		
 		new CustomListTask().reservePrimaryKey(); // grab a new PK for the list in an AsyncTask
 
 		int uID = User.getCurrUser(context);
 		Log.i(TAG, "User ID fetched: " + uID);
 
 		getSupportActionBar();
 		setTitle("New List");
 
 		listNameSpace = (EditText) findViewById(R.id.edit_list_name);
 		CIDSpace = (EditText) findViewById(R.id.edit_list_CID);
 
 		mylist = new ArrayList<HashMap<String, String>>();
 		newListPK = -1;
 		newItemPKs = new ArrayList<Integer>();
 
 		ListAdapter adapter = new SimpleAdapter(this, mylist,
 				R.layout.new_item_row, new String[] { "name" },
 				new int[] { R.id.element_title });
 
 		setListAdapter(adapter);
 
 	} // end onCreate
 	
 
 	public static void setListID(int pk) { // from AsyncTask
 		newListPK = pk;
 		Log.i(TAG, "Received new list PK from AsyncTask: " +newListPK);
 	} // end setListID
 
 	public void addListItem() {
 
 		// Whenever the list gets refreshed, the other layout pieces (List name)
 		// get refreshed too.
 		// Store the user's inputted list name prior to list refresh. Restore it
 		// after the list refresh.
 		listName = listNameSpace.getText().toString().trim();
 		CID = CIDSpace.getText().toString().trim();
 
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		alert.setTitle("New List Item Name");
 
 		final EditText input = new EditText(this); // Set an EditText view to
 													// get user input
 		alert.setView(input);
 
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				String value = input.getText().toString().trim();
 
 				HashMap<String, String> newItem = new HashMap<String, String>();
 				newItem.put("name", value);
 				newItem.put("assignee", "");
 				mylist.add(newItem); // append the new item to our list of items
 										// (can be saved when saveList() is
 										// called)
 				((SimpleAdapter) getListView().getAdapter())
 						.notifyDataSetChanged();
 				setContentView(R.layout.editlist);
 
 				listNameSpace = (EditText) findViewById(R.id.edit_list_name);
 				listNameSpace.setText(listName); // restore list name
 				CIDSpace = (EditText) findViewById(R.id.edit_list_CID);
 				CIDSpace.setText(CID); // restore list name
 
 			}
 		});
 
 		alert.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						// Canceled.
 					}
 				});
 
 		// alert.show();
 		AlertDialog dialog = alert.create();
 		dialog.getWindow().setSoftInputMode(
 				WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
 		dialog.show();
 	}
 
 	public void saveList(View v) {
 
 		listName = listNameSpace.getText().toString();
 		if (listName.equals("")) { // invalid name. don't allow save.
 			listNameSpace.setBackgroundColor(Color.parseColor("red"));
 			CID = CIDSpace.getText().toString();
 
 		} else { // allow save
 			int userID = User.getCurrUser(context);
 
 			CustomList newList = new CustomList();
 			newList.setID(newListPK); // received from AsyncTask
 			newList.setCreator(userID);
 			newList.setCustomID(CID);
 			newList.setName(listName);
 
 			// get ID better
 			db = new DBHelper(this);
 			db.open();
 
 			Item newItem;
 			for (HashMap<String, String> map : mylist) {
 				newItem = new Item(map);
 				newItem.setParent(context, newListPK);
 
 				int itemID = JSONItem.getItemPK();
 				newItemPKs.add(itemID);
 				newItem.setID(context, itemID);
 				newList.addItem(newItem);
 			}
 
 			db.insertList(newList, PUSH_TO_CLOUD);
 			db.close();
 			CustomList.setLinks(context, newList.getItems());/*
 
 			Item itemA, itemB, itemC;
 			if (newList.getItems() != null) {
 				int listSize = newList.getItems().size();
 				for (int i = 0; i < listSize; i++) { // make linked list
 
 					if (listSize - 1 == i) {
 						// At the last index
 						itemA = newList.getItem(i);
 						itemB = newList.getItem(0);
 						itemA.setNext(itemB.getID());
 
 					} else if (listSize > 1 && i == 0) {
 						// Two or more items in the list, insert the first two
 						itemA = newList.getItem(i);
 						itemB = newList.getItem(i + 1);
 						itemC = newList.getItem(listSize - 1);
 						itemA.setNext(itemB.getID());
 						itemA.setPrev(itemC.getID());
 						itemB.setPrev(itemA.getID());
 
 					} else if (listSize > 1 && i < listSize) {
 						// Two or more items in the list
 						itemA = newList.getItem(i);
 						itemB = newList.getItem(i + 1);
 						itemA.setNext(itemB.getID());
 						itemB.setPrev(itemA.getID());
 					}
 
 					else {
 						// Only one item in the list
 						itemA = newList.getItem(0);
 						itemA.setNext(itemA.getID());
 						itemA.setPrev(itemA.getID());
 					}
 
 					
 				}
 
 		
 			}*/
 		Item.insertOrUpdateItems(context, newList.getItems(),
 				PUSH_TO_CLOUD);
 		
 			
 			UIUtil.showMessage(context, "List Created!");
 			finish();
 			startActivity(getIntent());
 
 		}
 
 	} // end saveList
 
 	public void explainCID(View v) {
 
 		new AlertDialog.Builder(this)
 				.setTitle("Template IDs")
 				.setMessage(getString(R.string.CID_explain))
 				.setPositiveButton("Got it",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog,
 									int which) {
 								// do nothing
 							}
 						}).show();
 	} // end explainCID
 
 	public void cancelNewList(View v) {
 
 		// if this button is clicked, then the list wasn't saved to the user's
 		// device;
 		// all that happened was we got space on the web server for the list and
 		// its items.
 /*
 		JSONCustomList.deleteList(newListPK);
 		for (int i : newItemPKs) { // delete the items as well
 			JSONItem.deleteItem(i);
 		}*/
 		
 		new CustomListTask().uncreateList(newListPK, newItemPKs);
 
 		finish();
 
 	} // end cancelNewList
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.new_list_menu, menu);
 
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featuredId, MenuItem item) {
 		Intent intent;
 		if (item.getItemId() == android.R.id.home) {
 			intent = new Intent(this, HomeActivity.class);
 			startActivity(intent);
 			return true;
 		} else if (item.getItemId() == R.id.menu_add) {
 			addListItem();
 			return false;
 		} else if (item.getItemId() == R.id.menu_invite) {
 			intent = new Intent(this, InviteActivity.class);
 			startActivity(intent);
 			return true;
 		} else {
 			return false;
 		}
 	}
 }
