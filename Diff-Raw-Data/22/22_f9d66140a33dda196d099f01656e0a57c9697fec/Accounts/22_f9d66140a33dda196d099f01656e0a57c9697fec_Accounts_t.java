 package com.databases.example;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Calendar;
 import android.annotation.TargetApi;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Color;
 import android.graphics.PixelFormat;
 import android.graphics.drawable.GradientDrawable;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.ViewGroup;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.SubMenu;
 
 public class Accounts extends SherlockActivity implements OnSharedPreferenceChangeListener {
 
 	int page;
 
 	final int PICKFILE_RESULT_CODE = 1;
 
 	//Balance
 	float totalBalance;
 
 	//Constants for ContextMenu
 	int CONTEXT_MENU_OPEN=1;
 	int CONTEXT_MENU_EDIT=2;
 	int CONTEXT_MENU_DELETE=3;
 
 	//Text Area for Adding Accounts
 	EditText aName;
 	EditText aBalance;
 
 	View accountStatsView;
 
 	//Variables for the Account Table
 	String accountName = null;
 	String accountTime = null;
 	String accountBalance = null;
 	String accountDate = null;
 
 	//TextView of Statistics
 	TextView statsName;
 	TextView statsValue;
 	TextView statsDate;
 	TextView statsTime;
 
 
 	ListView lv = null;
 	ArrayAdapter<AccountRecord> adapter = null;
 
 	Cursor c = null;
 	final String tblAccounts = "tblAccounts";
 	final String tblTrans = "tblTrans";
 	final String dbFinance = "dbFinance";
 	SQLiteDatabase myDB;
 	ArrayList<AccountRecord> results = new ArrayList<AccountRecord>();
 
 	//Method called upon first creation
 	@Override
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		setTitle("Accounts");
 		setContentView(R.layout.accounts);
 		page = R.layout.accounts;
 
 		lv = (ListView)findViewById(R.id.list);
 
 		//Turn clicks on
 		lv.setClickable(true);
 		lv.setLongClickable(true);
 
 		//Set Listener for regular mouse click
 		lv.setOnItemClickListener(new OnItemClickListener(){
 			@Override
 			public void onItemClick(AdapterView<?> l, View v, int position, long id) {
 				int selectionRowID = (int) adapter.getItemId(position);
 				//String item = (String) adapter.getItem(position).name;
 
 				//NOTE: LIMIT *position*,*how many after*
 				String sqlCommand = "SELECT * FROM " + tblAccounts + 
 						" WHERE AcctID IN (SELECT AcctID FROM (SELECT AcctID FROM " + tblAccounts + 
 						" LIMIT " + (selectionRowID-0) + ",1)AS tmp)";
 
 				myDB = openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);
 
 				Cursor c = myDB.rawQuery(sqlCommand, null);
 				startManagingCursor(c);
 
 				int entry_id = 0;
 				String entry_name = null;
 				String entry_balance = null;
 				String entry_time = null;
 				String entry_date = null;
 
 				c.moveToFirst();
 				do{
 					entry_id = c.getInt(0);
 					entry_name = c.getString(1);
 					entry_balance = c.getString(2);
 					entry_time = c.getString(3);
 					entry_date = c.getString(4);
 					//Toast.makeText(Accounts.this, "ID: "+entry_id+"\nName: "+entry_name+"\nBalance: "+entry_balance+"\nTime: "+entry_time+"\nDate: "+entry_date, Toast.LENGTH_SHORT).show();
 				}while(c.moveToNext());
 
 				//Close Database if Open
 				if (myDB != null){
 					myDB.close();
 				}
 
 				//Call an Intent to go to Transactions Class
 				Intent i = new Intent(Accounts.this, Transactions.class);
 				i.putExtra("ID", entry_id);
 				i.putExtra("name", entry_name);
 				i.putExtra("balance", entry_balance);
 				i.putExtra("time", entry_time);
 				i.putExtra("date", entry_date);
 				startActivity(i);
 
 			}// end onItemClick
 
 		}//end onItemClickListener
 				);//end setOnItemClickListener
 
 
 		//Allows Context Menus for each item of the list view
 		registerForContextMenu(lv);
 
 		//Set up an adapter for the listView
 		adapter = new UserItemAdapter(this, android.R.layout.simple_list_item_1, results);
 		lv.setAdapter(adapter);
 
 		//Set up a listener for changes in settings menu
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		prefs.registerOnSharedPreferenceChangeListener(this);
 
 		populate();
 
 	}// end onCreate
 
 	//Method called after creation, populates list with account information
 	protected void populate() {
 		results = new ArrayList<AccountRecord>();
 
		//A textView alerting the user if database is empty
		TextView noResult = (TextView)findViewById(R.id.account_noTransaction);
		noResult.setVisibility(View.GONE);

 		//Reset Balance
 		totalBalance=0;
 
 		// Cursor is used to navigate the query results
 		myDB = this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);
 		c = myDB.query(tblAccounts, new String[] { "AcctID", "AcctName", "AcctBalance", "AcctTime", "AcctDate" }, null,
 				null, null, null, null);
 		startManagingCursor(c);
 		int IDColumn = c.getColumnIndex("AcctID");
 		int NameColumn = c.getColumnIndex("AcctName");
 		int BalanceColumn = c.getColumnIndex("AcctBalance");
 		int TimeColumn = c.getColumnIndex("AcctTime");
 		int DateColumn = c.getColumnIndex("AcctDate");
 
 		c.moveToFirst();
 		if (c != null) {
 			if (c.isFirst()) {
 				do {
 					String id = c.getString(IDColumn);
 					String name = c.getString(NameColumn);
 					String balance = c.getString(BalanceColumn);
 					String time = c.getString(TimeColumn);
 					String date = c.getString(DateColumn);
 
 					AccountRecord entry = new AccountRecord(id, name, balance,date,time);
 					results.add(entry);
 
 					//Add account balance to total balance
 					try{
 						totalBalance = totalBalance + Float.parseFloat(balance);
 					}
 					catch(Exception e){
 						Toast.makeText(Accounts.this, "Could not calculate total balance", Toast.LENGTH_SHORT).show();
 					}
 
 				} while (c.moveToNext());
 			}
 
 			else {
 				//No Results Found
 				noResult.setVisibility(View.VISIBLE);
 			}
 		} 
 
 		//Close Database if Open
 		if (myDB != null){
 			myDB.close();
 		}
 
 		adapter = new UserItemAdapter(this, android.R.layout.simple_list_item_1, results);
 		lv.setAdapter(adapter);
 
 		//Refresh Balance
 		calculateBalance();
 
 	}//end populate
 
 	@Override  
 	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
 		super.onCreateContextMenu(menu, v, menuInfo);
 
 		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)menuInfo;
 		String name = "" + adapter.getItem(itemInfo.position).name;
 
 		menu.setHeaderTitle(name);  
 		menu.add(0, CONTEXT_MENU_OPEN, 0, "Open");  
 		menu.add(0, CONTEXT_MENU_EDIT, 1, "Edit");
 		menu.add(0, CONTEXT_MENU_DELETE, 2, "Delete");
 	}  
 
 	@Override  
 	public boolean onContextItemSelected(android.view.MenuItem item) {
 
 		if(item.getTitle()=="Open"){
 			accountOpen(item);
 		}  
 		else if(item.getTitle()=="Edit"){
 			accountEdit(item);
 		}
 		else if(item.getTitle()=="Delete"){
 			accountDelete(item);
 		}
 		else {
 			System.out.print("ERROR on ContextMenu; function not found");
 			return false;
 		}  
 
 		return true;  
 	}  
 
 	//For Opening an Account
 	public void accountOpen(android.view.MenuItem item){  
 		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
 		//Object itemName = adapter.getItem(itemInfo.position);
 
 		String sqlCommand = "SELECT * FROM " + tblAccounts + 
 				" WHERE AcctID = " + adapter.getItem(itemInfo.position).id;
 
 		myDB = openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);
 
 		Cursor c = myDB.rawQuery(sqlCommand, null);
 		startManagingCursor(c);
 
 		int entry_id = 0;
 		String entry_name = null;
 		String entry_balance = null;
 		String entry_time = null;
 		String entry_date = null;
 
 		c.moveToFirst();
 		do{
 			entry_id = c.getInt(c.getColumnIndex("AcctID"));
 			entry_name = c.getString(c.getColumnIndex("AcctName"));
 			entry_balance = c.getString(c.getColumnIndex("AcctBalance"));
 			entry_time = c.getString(c.getColumnIndex("AcctTime"));
 			entry_date = c.getString(c.getColumnIndex("AcctDate"));
 		}while(c.moveToNext());
 
 		//Close Database if Open
 		if (myDB != null){
 			myDB.close();
 		}
 
 		LayoutInflater li = LayoutInflater.from(Accounts.this);
 		accountStatsView = li.inflate(R.layout.account_stats, null);
 
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
 				Accounts.this);
 
 		// set account_add.xml to AlertDialog builder
 		alertDialogBuilder.setView(accountStatsView);
 
 		//set Title
 		alertDialogBuilder.setTitle("View Account");
 
 		// set dialog message
 		alertDialogBuilder
 		.setCancelable(true);
 
 		// create alert dialog
 		AlertDialog alertDialog = alertDialogBuilder.create();
 
 		// show it
 		alertDialog.show();
 
 		//Set Statistics
 		statsName = (TextView)accountStatsView.findViewById(R.id.TextAccountName);
 		statsName.setText(entry_name);
 		statsValue = (TextView)accountStatsView.findViewById(R.id.TextAccountValue);
 		statsValue.setText(entry_balance);
 		statsDate = (TextView)accountStatsView.findViewById(R.id.TextAccountDate);
 		statsDate.setText(entry_date);
 		statsTime = (TextView)accountStatsView.findViewById(R.id.TextAccountTime);
 		statsTime.setText(entry_time);
 
 	}  
 
 	//For Editing an Account
 	public void accountEdit(android.view.MenuItem item){
 		final AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
 		final String name = adapter.getItem(itemInfo.position).name;
 		final String balance = adapter.getItem(itemInfo.position).balance;
 
 		//Toast.makeText(this, "Editing Item:\n" + id, Toast.LENGTH_SHORT).show();  
 
 		// get account_add.xml view
 		LayoutInflater li = LayoutInflater.from(Accounts.this);
 		final View promptsView = li.inflate(R.layout.account_add, null);
 
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
 				Accounts.this);
 
 		// set account_add.xml to AlertDialog builder
 		alertDialogBuilder.setView(promptsView);
 
 		//set Title
 		alertDialogBuilder.setTitle("Edit An Account");
 
 		//Add the previous info into the fields, remove unnecessary fields
 		aName = (EditText) promptsView.findViewById(R.id.EditAccountName);
 		aBalance = (EditText) promptsView.findViewById(R.id.EditAccountBalance);
 		TextView aBalanceText = (TextView)promptsView.findViewById(R.id.BalanceTexts);
 		aName.setText(name);
 		aBalance.setVisibility(View.GONE);
 		aBalanceText.setVisibility(View.GONE);
 
 		// set dialog message
 		alertDialogBuilder
 		.setCancelable(false)
 		.setPositiveButton("Save",
 				new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog,int id) {
 				// CODE FOR "OK"
 				accountName = aName.getText().toString().trim();
 				accountBalance = balance.trim();
 
 				if(Calendar.getInstance().get(Calendar.AM_PM)==1){
 					accountTime = Calendar.getInstance().get(Calendar.HOUR)+":"+Calendar.getInstance().get(Calendar.MINUTE)+ " PM";
 				}
 				else{
 					accountTime = Calendar.getInstance().get(Calendar.HOUR)+":"+Calendar.getInstance().get(Calendar.MINUTE)+ " AM";
 				}				
 
 				accountDate = Calendar.getInstance().get(Calendar.MONTH) + "-" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "-" + Calendar.getInstance().get(Calendar.YEAR);
 
 				try{
 					final String ID = adapter.getItem(itemInfo.position).id;
 					String deleteCommand = "DELETE FROM " + tblAccounts + " WHERE AcctID = " + ID + ";";
 					String insertCommand= "INSERT INTO " + tblAccounts
 							+ " (AcctID, AcctName, AcctBalance, AcctTime, AcctDate)" + " VALUES ('"
 							+ ID + "', '" + accountName + "', '" + accountBalance + "', '" + accountTime + "', '"
 							+ accountDate + "');";
 					//Open Database
 					myDB = openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);
 
 					//Delete Old Record
 					myDB.execSQL(deleteCommand);
 
 					//Make new record with same ID
 					myDB.execSQL(insertCommand);
 
 					//Close Database if Opened
 					if (myDB != null){
 						myDB.close();
 					}
 
 				}
 				catch(Exception e){
 					Toast.makeText(Accounts.this, "Error Editing Account!\nDid you enter valid input? ", Toast.LENGTH_SHORT).show();
 				}
 
 				page = R.layout.accounts;
 
 				Accounts.this.populate();
 
 			}//end onClick "OK"
 		})
 		.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog,int id) {
 				// CODE FOR "Cancel"
 				dialog.cancel();
 			}
 		});
 
 		// create alert dialog
 		AlertDialog alertDialog = alertDialogBuilder.create();
 
 		// show it
 		alertDialog.show();
 
 	}
 
 	//For Deleting an Account
 	public void accountDelete(android.view.MenuItem item){
 		AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
 		Object itemName = adapter.getItem(itemInfo.position).name;
 
 		//NOTE: LIMIT *position*,*how many after*
 		//String sqlDeleteAccount = "DELETE FROM " + tblAccounts + 
 		//		" WHERE AcctID IN (SELECT AcctID FROM (SELECT AcctID FROM " + tblAccounts + 
 		//		" LIMIT " + (itemInfo.position-0) + ",1)AS tmp);";
 
 		String sqlDeleteAccount = "DELETE FROM " + tblAccounts + 
 				" WHERE AcctID = " + adapter.getItem(itemInfo.position).id;
 
 		//Deletes all transactions in the account
 		String sqlDeleteTransactions = "DELETE FROM " + tblTrans + 
 				" WHERE ToAcctID = " + adapter.getItem(itemInfo.position).id;
 
 		//Open Database
 		myDB = this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);
 
 		myDB.execSQL(sqlDeleteAccount);
 		myDB.execSQL(sqlDeleteTransactions);	
 
 		//Close Database if Opened
 		if (myDB != null){
 			myDB.close();
 		}
 
 		//results.remove(itemInfo.position);
 		//adapter.notifyDataSetChanged();
 
 		populate();
 
 		Toast.makeText(this, "Deleted Item:\n" + itemName, Toast.LENGTH_SHORT).show();
 
 	}//end of accountDelete
 
 	//For Adding an Account
 	public void accountAdd(){
 		// get account_add.xml view
 		LayoutInflater li = LayoutInflater.from(Accounts.this);
 		final View promptsView = li.inflate(R.layout.account_add, null);
 
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
 				Accounts.this);
 
 		// set account_add.xml to AlertDialog builder
 		alertDialogBuilder.setView(promptsView);
 
 		//set Title
 		alertDialogBuilder.setTitle("Add An Account");
 
 		// set dialog message
 		alertDialogBuilder
 		.setCancelable(false)
 		.setPositiveButton("Save",
 				new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog,int id) {
 
 				//Variables for adding the account
 				aName = (EditText) promptsView.findViewById(R.id.EditAccountName);
 				aBalance = (EditText) promptsView.findViewById(R.id.EditAccountBalance);
 				accountName = aName.getText().toString().trim();
 				accountBalance = aBalance.getText().toString().trim();
 
 				if(Calendar.getInstance().get(Calendar.AM_PM)==1){
 					accountTime = Calendar.getInstance().get(Calendar.HOUR)+":"+Calendar.getInstance().get(Calendar.MINUTE)+ " PM";
 				}
 				else{
 					accountTime = Calendar.getInstance().get(Calendar.HOUR)+":"+Calendar.getInstance().get(Calendar.MINUTE)+ " AM";
 				}				
 
 				accountDate = Calendar.getInstance().get(Calendar.MONTH) + "-" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "-" + Calendar.getInstance().get(Calendar.YEAR);
 
 				//Variables for adding Starting Balance transaction
 				final String transactionName = "STARTING BALANCE";
 				float transactionValue;
 				final String transactionCategory = "STARTING BALANCE";
 				final String transactionCheckNum = "None";
 				final String transactionMemo = "This is an automatically generated transaction created when you add an account";
 				final String transactionTime = accountTime;
 				final String transactionDate = accountDate;
 				final String transactionCleared = "true";
 				String transactionType = "Unknown";
 
 				//Check Value to see if it's valid
 				try{
 					transactionValue = Float.parseFloat(accountBalance);
 				}
 				catch(Exception e){
 					transactionValue = (float) 0.00;
 					accountBalance = "0";
 				}				
 
 				try{
 					if(Float.parseFloat(accountBalance)>=0){
 						transactionType = "Deposit";
 					}
 					else{
 						transactionType = "Withdrawl";
 						transactionValue = transactionValue * -1;
 					}
 				}
 				catch(Exception e){
 					Toast.makeText(Accounts.this, "Error\nWas balance a valid format?", Toast.LENGTH_SHORT).show();
 				}
 
 				String sqlQuery = "SELECT AcctID FROM " + tblAccounts + " WHERE AcctName='" + accountName + "' AND AcctBalance=" + accountBalance + " AND AcctTime='" + accountTime + "' AND AcctDate='" + accountDate + "';";
 
 				//Open Database
 				myDB = Accounts.this.openOrCreateDatabase(dbFinance, MODE_PRIVATE, null);
 
 				try{
 					if (accountName.length()>0) {
 
 						final String sqlCommand = "INSERT INTO " + tblAccounts
 								+ " (AcctName, AcctBalance, AcctTime, AcctDate)" + " VALUES ('"
 								+ accountName + "', '" + accountBalance + "', '" + accountTime + "', '"
 								+ accountDate + "');";
 
 						//Create a new account
 						myDB.execSQL(sqlCommand);
 
 						//Query the Newly created account
 						Cursor c = myDB.rawQuery(sqlQuery, null);
 						startManagingCursor(c);
 
 						int entry_id = 0;
 
 						c.moveToFirst();
 						do{
 							entry_id = c.getInt(0);
 						}while(c.moveToNext());
 
 						//Create Starting Balance transaction
 						final String sqlStartingBalance = "INSERT INTO " + tblTrans
 								+ " (ToAcctID, TransName, TransValue, TransType, TransCategory, TransCheckNum, TransMemo, TransTime, TransDate, TransCleared)" + " VALUES ('"
 								+ entry_id + "', '" + transactionName + "', '" + transactionValue + "', '" + transactionType + "', '" + transactionCategory + "', '" + transactionCheckNum + "', '" + transactionMemo + "', '" + transactionTime + "', '" + transactionDate + "', '" + transactionCleared + "');";;
 
 								myDB.execSQL(sqlStartingBalance);
 					} 
 
 					else {
 						Toast.makeText(Accounts.this, " No Nulls Allowed ", Toast.LENGTH_SHORT).show();
 					}
 				}
 				catch(Exception e){
 					Toast.makeText(Accounts.this, "Error Adding Account!\nDid you enter valid input? ", Toast.LENGTH_SHORT).show();
 				}
 
 				//Close Database if Opened
 				if (myDB != null){
 					myDB.close();
 				}
 
 				page = R.layout.accounts;
 
 				Accounts.this.populate();
 
 			}//end onClick "OK"
 		})
 		.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog,int id) {
 				// CODE FOR "Cancel"
 				dialog.cancel();
 			}
 		});
 
 		// create alert dialog
 		AlertDialog alertDialog = alertDialogBuilder.create();
 
 		// show it
 		alertDialog.show();
 
 	}
 
 	//Handle closing database properly to avoid corruption
 	@Override
 	public void onDestroy() {
 		if (myDB != null){
 			myDB.close();
 		}
 		super.onDestroy();
 	}
 
 	//For Menu
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.layout.account_menu, menu);
 		return true;
 	}
 
 	//For Menu Items
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:    
 			Intent intentUp = new Intent(Accounts.this, Main.class);
 			intentUp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intentUp);
 			break;
 
 		case R.id.account_menu_add:    
 			accountAdd();
 			break;
 
 		case R.id.account_menu_search:    
 			onSearchRequested();
 			break;
 
 		case R.id.account_menu_transfer:    
 			//accountTransfer();
 			break;
 
 		case R.id.account_menu_unknown:    
 			//Insert Unknown Code Here
 			pickFile(null);
 			break;
 
 		case R.id.account_menu_logout:
 			Toast.makeText(this, "You pressed Logout!", Toast.LENGTH_SHORT).show();
 			this.finish();
 			this.moveTaskToBack(true);
 			super.onDestroy();
 			break;
 
 		case R.id.account_menu_options:    
 			//Toast.makeText(this, "You pressed Options!", Toast.LENGTH_SHORT).show();
 			Intent v = new Intent(Accounts.this, Options.class);
 			startActivity(v);
 			break;
 
 		case R.id.account_menu_help:    
 			Toast.makeText(this, "You pressed Help!", Toast.LENGTH_SHORT).show();
 			break;
 		}
 		return true;
 	}
 
 	public class UserItemAdapter extends ArrayAdapter<AccountRecord> {
 		private ArrayList<AccountRecord> account;
 
 		public UserItemAdapter(Context context, int textViewResourceId, ArrayList<AccountRecord> users) {
 			super(context, textViewResourceId, users);
 			this.account = users;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			AccountRecord user = account.get(position);
 
 			//For Custom View Properties
 			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Accounts.this);
 			boolean useDefaults = prefs.getBoolean("checkbox_default", true);
 
 			if (v == null) {
 				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				v = vi.inflate(R.layout.account_item, null);
 
 				//Change Background Colors
 				try{
 					LinearLayout l;
 					l=(LinearLayout)v.findViewById(R.id.account_layout);
 					String startColor = prefs.getString(Accounts.this.getString(R.string.pref_key_account_startBackgroundColor), "#E8E8E8");
 					String endColor = prefs.getString(Accounts.this.getString(R.string.pref_key_account_endBackgroundColor), "#FFFFFF");
 					GradientDrawable defaultGradient = new GradientDrawable(
 							GradientDrawable.Orientation.BOTTOM_TOP,
 							new int[] {Color.parseColor(startColor),Color.parseColor(endColor)});
 
 					if(useDefaults){
 						l.setBackgroundResource(R.drawable.account_list_style);
 					}
 					else{
 						l.setBackgroundDrawable(defaultGradient);
 					}
 
 				}
 				catch(Exception e){
 					Toast.makeText(Accounts.this, "Could Not Set Custom Background Color", Toast.LENGTH_SHORT).show();
 				}
 
 				//Change Size of main field
 				try{
 					String DefaultSize = prefs.getString(Accounts.this.getString(R.string.pref_key_account_nameSize), "16");
 					TextView t;
 					t=(TextView)v.findViewById(R.id.account_name);
 
 					if(useDefaults){
 						t.setTextSize(16);
 					}
 					else{
 						t.setTextSize(Integer.parseInt(DefaultSize));
 					}
 
 				}
 				catch(Exception e){
 					Toast.makeText(Accounts.this, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
 				}
 
 				try{
 					String DefaultColor = prefs.getString(Accounts.this.getString(R.string.pref_key_account_nameColor), "#000000");
 					TextView t;
 					t=(TextView)v.findViewById(R.id.account_name);
 
 					if(useDefaults){
 						t.setTextColor(Color.parseColor("#000000"));
 					}
 					else{
 						t.setTextColor(Color.parseColor(DefaultColor));
 					}
 
 				}
 				catch(Exception e){
 					Toast.makeText(Accounts.this, "Could Not Set Custom Name Size", Toast.LENGTH_SHORT).show();
 				}
 
 				try{
 					String DefaultSize = prefs.getString(Accounts.this.getString(R.string.pref_key_account_fieldSize), "10");
 					TextView tmp;
 
 					if(useDefaults){
 						tmp=(TextView)v.findViewById(R.id.account_balance);
 						tmp.setTextSize(10);
 						tmp=(TextView)v.findViewById(R.id.account_date);
 						tmp.setTextSize(10);
 						tmp=(TextView)v.findViewById(R.id.account_time);
 						tmp.setTextSize(10);
 					}
 					else{
 						tmp=(TextView)v.findViewById(R.id.account_balance);
 						tmp.setTextSize(Integer.parseInt(DefaultSize));
 						tmp=(TextView)v.findViewById(R.id.account_date);
 						tmp.setTextSize(Integer.parseInt(DefaultSize));
 						tmp=(TextView)v.findViewById(R.id.account_time);
 						tmp.setTextSize(Integer.parseInt(DefaultSize));
 					}
 
 				}
 				catch(Exception e){
 					Toast.makeText(Accounts.this, "Could Not Set Custom Field Size", Toast.LENGTH_SHORT).show();
 				}
 
 				try{
 					String DefaultColor = prefs.getString(Accounts.this.getString(R.string.pref_key_account_fieldColor), "#0099CC");
 					TextView tmp;
 
 					if(useDefaults){
 						tmp=(TextView)v.findViewById(R.id.account_balance);
 						tmp.setTextColor(Color.parseColor("#0099CC"));
 						tmp=(TextView)v.findViewById(R.id.account_date);
 						tmp.setTextColor(Color.parseColor("#0099CC"));
 						tmp=(TextView)v.findViewById(R.id.account_time);
 						tmp.setTextColor(Color.parseColor("#0099CC"));
 					}
 					else{
 						tmp=(TextView)v.findViewById(R.id.account_balance);
 						tmp.setTextColor(Color.parseColor(DefaultColor));
 						tmp=(TextView)v.findViewById(R.id.account_date);
 						tmp.setTextColor(Color.parseColor(DefaultColor));
 						tmp=(TextView)v.findViewById(R.id.account_time);
 						tmp.setTextColor(Color.parseColor(DefaultColor));
 					}
 
 				}
 				catch(Exception e){
 					Toast.makeText(Accounts.this, "Could Not Set Custom Field Color", Toast.LENGTH_SHORT).show();
 				}
 
 
 				//For User-Defined Field Visibility
 				if(useDefaults||prefs.getBoolean("checkbox_account_nameField", true)){
 					TextView name = (TextView) v.findViewById(R.id.account_name);
 					name.setVisibility(View.VISIBLE);
 				}
 				else{
 					TextView name = (TextView) v.findViewById(R.id.account_name);
 					name.setVisibility(View.GONE);
 				}
 
 				if(useDefaults||prefs.getBoolean("checkbox_account_balanceField", true)){
 					TextView balance = (TextView) v.findViewById(R.id.account_balance);
 					balance.setVisibility(View.VISIBLE);
 				}
 				else{
 					TextView balance = (TextView) v.findViewById(R.id.account_balance);
 					balance.setVisibility(View.GONE);
 				}
 
 				if(useDefaults||prefs.getBoolean("checkbox_account_dateField", true)){
 					TextView date = (TextView) v.findViewById(R.id.account_date);
 					date.setVisibility(View.VISIBLE);
 				}
 				else{
 					TextView date = (TextView) v.findViewById(R.id.account_date);
 					date.setVisibility(View.GONE);
 				}
 
 				if(useDefaults||prefs.getBoolean("checkbox_account_timeField", true)){
 					TextView time = (TextView) v.findViewById(R.id.account_time);
 					time.setVisibility(View.VISIBLE);
 				}
 				else{
 					TextView time = (TextView) v.findViewById(R.id.account_time);
 					time.setVisibility(View.GONE);
 				}
 
 			}
 
 			if (user != null) {
 				TextView name = (TextView) v.findViewById(R.id.account_name);
 				TextView balance = (TextView) v.findViewById(R.id.account_balance);
 				TextView date = (TextView) v.findViewById(R.id.account_date);
 				TextView time = (TextView) v.findViewById(R.id.account_time);
 
 				//Change gradient
 				try{
 					LinearLayout l;
 					l=(LinearLayout)v.findViewById(R.id.account_gradient);
 					GradientDrawable defaultGradientPos = new GradientDrawable(
 							GradientDrawable.Orientation.BOTTOM_TOP,
 							new int[] {0xFF00FF33,0xFF000000});
 
 					GradientDrawable defaultGradientNeg = new GradientDrawable(
 							GradientDrawable.Orientation.BOTTOM_TOP,
 							new int[] {0xFFFF0000,0xFF000000});
 
 					if(useDefaults){
 						if(Float.parseFloat((user.balance)) >=0){
 							l.setBackgroundDrawable(defaultGradientPos);
 						}
 						else{
 							l.setBackgroundDrawable(defaultGradientNeg);
 						}
 
 					}
 					else{
 						if(Float.parseFloat((user.balance)) >=0){
 							l.setBackgroundDrawable(defaultGradientPos);
 						}
 						else{
 							l.setBackgroundDrawable(defaultGradientNeg);
 						}
 					}
 
 				}
 				catch(Exception e){
 					Toast.makeText(Accounts.this, "Could Not Set Custom gradient", Toast.LENGTH_SHORT).show();
 				}
 
 
 				if (user.name != null) {
 					name.setText(user.name);
 				}
 
 				if(user.balance != null) {
 					balance.setText("Balance: " + user.balance );
 				}
 
 				if(user.date != null) {
 					date.setText("Date: " + user.date );
 				}
 
 				if(user.time != null) {
 					time.setText("Time: " + user.time );
 				}
 
 			}
 			return v;
 		}
 	}
 
 	//Used after a change in settings occurs
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
 		//Toast.makeText(this, "Options Just Changed: Accounts.Java", Toast.LENGTH_SHORT).show();
 		populate();
 	}
 
 	//If android version supports it, smooth gradient
 	@TargetApi(5)
 	@Override
 	public void onAttachedToWindow() {
 		super.onAttachedToWindow();
 		Window window = getWindow();
 		window.setFormat(PixelFormat.RGBA_8888);
 
 	}
 
 	//Calculates the balance
 	public void calculateBalance(){
 		TextView balance = (TextView)this.findViewById(R.id.account_total_balance);
 		balance.setText("Total Balance: " + totalBalance);
 	}
 
 	//Override default resume to also call populate in case view needs refreshing
 	@Override
 	public void onResume(){
 		populate();
 		super.onResume();
 	}
 
 	//Override method to send the search extra data, letting it know which class called it
 	@Override
 	public boolean onSearchRequested() {
 		Bundle appData = new Bundle();
 		startSearch(null, false, appData, false);
 		return true;
 	}
 
 	//Method used to handle picking a file
 	void pickFile(File aFile) {
 		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
 		intent.setType("*/*");
 		startActivityForResult(intent,PICKFILE_RESULT_CODE);
 	}
 
 	//Method called after picking a file
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data){
 		switch (requestCode) {
 		case PICKFILE_RESULT_CODE:
 			if(resultCode==RESULT_OK){
 				String FilePath = data.getData().getPath();
 				Toast.makeText(this, "File Path : " + FilePath, Toast.LENGTH_LONG).show();
 			}
 			break;
 		}
 	}
 
 }// end Accounts
