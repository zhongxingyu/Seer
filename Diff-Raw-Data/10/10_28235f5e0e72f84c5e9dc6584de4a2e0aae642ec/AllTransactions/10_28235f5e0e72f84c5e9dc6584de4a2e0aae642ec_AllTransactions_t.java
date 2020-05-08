 package edu.ucsb.cs.cs185.moneysaver;
 
 import java.util.Comparator;
 import java.util.List;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 public class AllTransactions extends Activity {
 	private ListView lv_all_transactions;
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
     			i = new Intent(this, MoneySaverActivity.class);
     			startActivity(i);
     			finish();
     			return true;
     		case R.id.tran:
     			i = new Intent(this, MoneySaverActivity.class);
     			i.putExtra(MoneySaverActivity.TRANS_NEW, true);
     			startActivity(i);
     			finish();
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
     	ArrayAdapter<Transaction> adapter  = new ArrayAdapter<Transaction>(AllTransactions.this,
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
 	        	EditTransaction(adapter.getItem((int)info.id));
 	        	
 	        	
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
 		
         m_dialog = new Dialog(AllTransactions.this);
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
 
 	public void EditTransaction(Transaction transaction) {
		Intent intent = new Intent(this, Transactions.class);
 		intent.putExtra(MoneySaverActivity.TRANS_NAME, transaction.getName());
     	intent.putExtra(MoneySaverActivity.TRANS_AMOUNT, transaction.getValue());
     	intent.putExtra(MoneySaverActivity.TRANS_DESCRIPTION, transaction.getCategory());
    	intent.putExtra(MoneySaverActivity.TRANS_CATEGORY, transaction.getCategory());
     	intent.putExtra(MoneySaverActivity.TRANS_DATE, transaction.getDate());
     	long id = transaction.getId();
     	intent.putExtra(MoneySaverActivity.TRANS_ID, id);
     	intent.putExtra(MoneySaverActivity.TRANS_EDIT, true);
     	
     	startActivity(intent);
     	finish();
 	}
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.all_transactions);
      
         ActionBar actionbar = getActionBar();
         actionbar.setDisplayShowHomeEnabled(false);
         actionbar.setDisplayShowTitleEnabled(false);
         
       //create database here
         m_database = new DataBaseWrapper(getApplicationContext());
         
         lv_all_transactions = (ListView) findViewById(R.id.lv_all_transactions);
       //Setting the listView for a floating Context Menu
         registerForContextMenu(lv_all_transactions);
         updateList();
     }
     
     public void updateList() {
     	List<Transaction> trans_list = m_database.getAllTransactions();
     	ArrayAdapter<Transaction> adapter = new ArrayAdapter<Transaction>(this,
         			android.R.layout.simple_list_item_1, trans_list);
         
     	
     	//Sort the transactions to have the most recent on top
     	//Sorting by id, but might want to sort by date
     	adapter.sort(new Comparator<Transaction>(){
     		public int compare(Transaction trans1, Transaction trans2){
     			if(trans1.getId() > trans2.getId())
     				return -1;
     			return 1;
     		};    	    		
     	});
     	
     	lv_all_transactions.setAdapter(adapter);
     	
     }
 }
