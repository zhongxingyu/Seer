 package edu.ucsb.cs.cs185.moneysaver;
 
 import java.util.Calendar;
 import java.util.Comparator;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Vector;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MoneySaverActivity extends Activity {
 	public static final String TRANS_NAME = "TRANS_NAME";
 	public static final String TRANS_AMOUNT = "TRANS_AMOUNT";
 	public static final String TRANS_CATEGORY = "TRANS_CATEGORY";
 	public static final String TRANS_DESCRIPTION = "TRANS_DESCRIPTION";
 	public static final String TRANS_DATE = "TRANS_DATE";
 	public static final String TRANS_ID = "TRANS_ID";
 	//Will be used to check if user is trying to make a 
 	//new transaction
 	public static final String TRANS_NEW = "TRANS_NEW";
 	public static final String TRANS_EDIT = "TRANS_EDIT";
 	
 	private static final int TRANSACTION_KEY = 1421; 
 	
 	
 	private ListView lv_transaction_list;
 	
 	private TextView tv_amount;
 	
 	private DataBaseWrapper m_database;
 	
 	/*
 	 * Used for displaying transaction information
 	 * Made a class variable for easy/quick fix
 	 */
 	private Dialog m_dialog;
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		Intent i;
     	switch(item.getItemId())
     	{
     		case R.id.home:    			
     			return true;
     		case R.id.tran:
     			newTransaction();
     			return true;
     		case R.id.pie:
     			i = new Intent(this, Charts.class);
     			startActivity(i);
     			finish();
     			return true;
     		case R.id.settings:
     			i = new Intent(this, Settings.class);
     			startActivity(i);
     			finish();
     			return true;
     		case R.id.help:
     			i = new Intent(this, Help.class);
     			startActivity(i);
     			finish();
     			return true;    			
     		default:
     			return true;	
     	}
 	}
 	
 	/*
 	 * Starts the Transactions activity for a 
 	 * new transaction
 	 */
 	private void newTransaction(){
 		Intent i = new Intent(this, Transactions.class);
 		startActivityForResult(i, TRANSACTION_KEY);
 	}
 	
 	/*
 	 * setting up the context menu for the ListView menus to display
 	 * when they're long clicked
 	 */
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		  super.onCreateContextMenu(menu, v, menuInfo);
 		  MenuInflater inflater = getMenuInflater();
 		  inflater.inflate(R.menu.transaction_menu, menu);
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
         
         //get list from database
     	List<Transaction> trans_list = m_database.getAllTransactions();
     	ArrayAdapter<Transaction> adapter  = new ArrayAdapter<Transaction>(MoneySaverActivity.this,
     			android.R.layout.simple_list_item_1, trans_list);
     	// sort adapter
     	adapter.sort(new Comparator<Transaction>(){
     		public int compare(Transaction trans1, Transaction trans2){
     			if(trans1.getId() > trans2.getId())
     				return -1;
     			return 1;
     		};    	    		
     	});
 	    
 	    switch (item.getItemId()) {
 	    	case R.id.transaction_info:
 	    		createTransactionDialog(adapter.getItem((int)info.id));
 	        	
 	    		return true;
 	        case R.id.transaction_edit:
 	        	//calls Transactions.java with fields to pre-populate
 	        	editTransaction(adapter.getItem((int)info.id));
 	        	
 	        	
 	            return true;
 	        case R.id.transaction_delete:
 	        	
 	        	m_database.deleteTransaction(adapter.getItem((int)info.id));
 	        	updateList();
 	        	
 	            return true;
 	        default:
 	            return super.onContextItemSelected(item);
 	    }
 	}
 	
 	private void createTransactionDialog(Transaction tran){
 		/*
 		 * Made m_dialog a class variable to be able to access 
 		 * it within the button's OnClickListener
 		 */
     	//Code from: http://www.helloandroid.com/tutorials/how-display-custom-dialog-your-android-application
     	//set up dialog
 		
         m_dialog = new Dialog(MoneySaverActivity.this);
         m_dialog.setContentView(R.layout.dialogbox);
         m_dialog.setTitle(tran.getName() + ": " + tran.getDate());
         m_dialog.setCancelable(true);
         //there are a lot of settings, for dialog, check them all out!
 
         //set up text
         TextView dialog_textview = (TextView) m_dialog.findViewById(R.id.dialog_textview);
         dialog_textview.setText(tran.toStringFull());
 
         //set up image view
         //ImageView img = (ImageView) dialog.findViewById(R.id.ImageView01);
         //img.setImageResource(R.drawable.nista_logo);
 
         //set up button
         Button dialog_button = (Button) m_dialog.findViewById(R.id.dialog_button);
         dialog_button.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				m_dialog.dismiss();
 				
 			}
 		});
         //now that the dialog is set up, it's time to show it    
         m_dialog.show();
 		
 	}
 	
 	/*
 	 * Start activity for result:
 	 * 	send default data to populate options
 	 */
 	private void editTransaction(Transaction transaction) {
 		Intent intent = new Intent(this, Transactions.class);
 		intent.putExtra(TRANS_NAME, transaction.getName());
     	intent.putExtra(TRANS_AMOUNT, transaction.getValue());
     	intent.putExtra(TRANS_CATEGORY, transaction.getCategory());
     	intent.putExtra(TRANS_DESCRIPTION, transaction.getDescription());
     	intent.putExtra(TRANS_DATE, transaction.getDate());
     	long id = transaction.getId();
     	intent.putExtra(TRANS_ID, id);
     	
     	startActivityForResult(intent, TRANSACTION_KEY);
 	}
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.home);
         
         Bundle extras = getIntent().getExtras();
         if(extras !=null) {
         	Boolean new_transaction_request = extras.getBoolean(TRANS_NEW);
         	Boolean edit_transaction_request = extras.getBoolean(TRANS_EDIT);
         	if(new_transaction_request) newTransaction();
         	if(edit_transaction_request)
         		editTransaction(new Transaction(extras.getLong(TRANS_ID), extras.getString(TRANS_NAME), 
         				extras.getFloat(TRANS_AMOUNT), extras.getString(TRANS_CATEGORY), extras.getString(TRANS_DESCRIPTION), extras.getString(TRANS_DATE)));
         	
         }
         
         
         //create database here
         m_database = new DataBaseWrapper(getApplicationContext());
         
         /* TODO
          * Update all recurring transactions
          */
         Calendar start = new GregorianCalendar(2012, Calendar.JUNE, 8);
         long cur_time = start.getTimeInMillis();
         long duration = 1000 * 60 * 60* 24; 
         
         RecurringTransaction temp = new RecurringTransaction(-1, "TEMP REC2", (float)3.2, "Alcohol", "description", cur_time, duration);
        // m_database.insertRecurringTransactions(temp);
         
         List<RecurringTransaction> rec_list = m_database.getAllRecurring();
         int size = rec_list.size();
         m_database.updateRecurringTransactions();
 
         //Insert default categories
         m_database.insertCategory(new Category("Groceries"));
         m_database.insertCategory(new Category("Restaurants"));
         m_database.insertCategory(new Category("Gas"));
         m_database.insertCategory(new Category("Alcohol"));
         m_database.insertCategory(new Category("Entertainment"));
         m_database.insertCategory(new Category("Deposit"));
       
         ActionBar actionbar = getActionBar();
         actionbar.setDisplayShowHomeEnabled(false);
         actionbar.setDisplayShowTitleEnabled(false);
         
         tv_amount = (TextView) findViewById(R.id.tv_amount);
 
         
         // Will be used for displaying transactions
         lv_transaction_list = (ListView) findViewById(R.id.lv_transaction_list);
         
         //Setting the listView for a floating Context Menu
         registerForContextMenu(lv_transaction_list);
 
         updateList();
         displayBalance();
     }
     
     private void displayBalance() {
     	float balance = m_database.getBalance();
     	
    	String balance_str = "$" + String.format("%.3g%n", balance);
     	
     	tv_amount.setText(balance_str);
     }
     
     /*
      * If show_all = true, will populate ListView with all transactions
      * else shows 5 most recent transactions
      */
     private void updateList() {
     	List<Transaction> trans_list = m_database.getAllTransactions();
     	ArrayAdapter<Transaction> adapter;
     	
     	if(trans_list.size() > 5){
     		adapter = new ArrayAdapter<Transaction>(this,
         			android.R.layout.simple_list_item_1, trans_list.subList(trans_list.size()-5, trans_list.size()));
         }else{
         	adapter = new ArrayAdapter<Transaction>(this,
         			android.R.layout.simple_list_item_1, trans_list);
         }
     	
     	//Sort the transactions to have the most recent on top
     	//Sorting by id, but might want to sort by date
     	adapter.sort(new Comparator<Transaction>(){
     		public int compare(Transaction trans1, Transaction trans2){
     			if(trans1.getId() > trans2.getId())
     				return -1;
     			return 1;
     		};    	    		
     	});
     	
     	lv_transaction_list.setAdapter(adapter);
     	
     	displayBalance();
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode,
             Intent data) {
     	    	
     	if(resultCode == RESULT_OK) {
     		if (requestCode == TRANSACTION_KEY) {
     	    	Bundle extras = data.getExtras();
 
     			String name = extras.getString(TRANS_NAME);
     			float amount = extras.getFloat(TRANS_AMOUNT);
     			String category = extras.getString(TRANS_CATEGORY);
     			String description = extras.getString(TRANS_DESCRIPTION);
     			String date = extras.getString(TRANS_DATE);
     			long id = (long)extras.getLong(TRANS_ID);
     			
     			Transaction returned_transaction = null;
     			
     			returned_transaction = new Transaction(id, name, amount, category, description, date);
 
     			/*
     			 * Determine if editting or it exists
     			 */
     			Toast.makeText(MoneySaverActivity.this, "In Home: Added Transaction: " + name + " amount: " + amount + " on: " + date, Toast.LENGTH_SHORT).show();
     			
     			//this will just update transaction if already exists (based on id != -1)
     			m_database.insertTransaction(returned_transaction);
     			
 
     			updateList();
     		}
     	}
     }
     
     public void insertCategories() {
     	Vector<String> category_names = new Vector<String>();
     	category_names.add("Groceries");
     	category_names.add("Alcohol");
     	
     	for(int index = 0; index < category_names.size(); index++) {
     		Category current_category = new Category(category_names.get(index));
     		m_database.insertCategory(current_category);
     	}
     
    
     
     }
     public void buttonClick(View view)
     {
     	Button b_clicked = (Button) view;
     	int id = b_clicked.getId();
     	
     	switch(id) {
     		case R.id.b_show_all:
     			Intent i = new Intent(this, AllTransactions.class);
     			startActivity(i);
     			return;    			
 		default:
     	}
     }
  
 }
