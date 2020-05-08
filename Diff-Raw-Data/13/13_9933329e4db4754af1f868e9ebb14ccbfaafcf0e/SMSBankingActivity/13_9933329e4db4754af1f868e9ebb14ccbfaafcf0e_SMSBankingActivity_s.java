 package com.meryrua.smsbanking;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 import java.util.HashMap;
 
 import com.meryrua.smsbanking.R;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.ComponentName;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.SubMenu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SMSBankingActivity extends ListActivity{
 	private Context context;
 	private TransactionAdapter transactionAdapter;
 	private HashMap<String, String> filterMap;
 	private Resources resources;	
 	protected static ArrayAdapter<String> cardAdapter;
 	private ListView listView;
 	private String cardAliasString;
 	private String passwordString;
 	private static String currentPassword = null;
 	private Bundle bundle;
 	private Intent viewIntent;
 	private static boolean isChecked = false;
 	
 	private IntentFilter updateIntentFilter;
 	private UpdateReceiver updateReceiver;
 	static int numberUpdate = 0;
 	private boolean firstLoading = false;
 	private boolean needToDeleteCard = false;
 	private String cardForDelete;
 	
 	private static final int IDM_PREFERENCES = 101;
 	private static final int IDM_CARD_FILTER = 102;
 	private static final int IDM_OPERATION_FILTER_ALL_OPERATION = 1031;
 	private static final int IDM_OPERATION_FILTER_CARD_OPERATION = 1032;
 	private static final int IDM_OPERATION_FILTER_INCOMING_OPERATION = 1033;
 	private static final int IDM_OPERATION_FILTER_OUTGOING_OPERATION = 1034;
 	private static final int IDM_TEST = 104;
 	private static final int IDM_DELETE_DATA = 105;
 	private static final int IDM_DELETE_CARD = 106;
 	private static final int DIALOG_SMS_DETAIL = 0;	
 	private static final int DIALOG_CARD_FILTER = 1;	
 	private static final int DIALOG_CARD_DATA = 2;
 	private static final int DIALOG_PASSWORD_CHECKING = 3;
 	private static final int DIALOG_WRONG_PASSWORD = 4;
 	private static final int DIALOG_LOADING = 5;
 	private static final int DIALOG_LOAD_DATA_REQUEST = 6;
 	private static final int DIALOG_NEED_TO_LOAD = 7;
 	private static final int DIALOG_DELETE_CARD_DATA = 8;
 	
 	public static final String UPDATE_TRANSACTION_LIST_INTENT = "com.meryrua.smsbanking.UPDATE_TRANSACTION_LIST";
 	public static final String VIEW_TRANSACTION_DETAIL_INTENT = "com.meryrua.smsbanking.VIEW_TRANSACTION_DETAIL";
 	public static final String INTENT_ACTION = "intent_action";
 	public static final String DEFAULT_PASSWORD = "1234";
 	public static final String PASSWORD_TEXT = "password";
 	
 	private static final String NEED_TO_LOAD = "need_to_load";
 	private static final String LOG_TAG = "com.meryrua.smsbanking:SMSBankingActivity";
 	
 	private ProgressDialog progressDialog;
 	private TransactionData transactionData;
 	private SMSBankingActivityHandler thisActivityHandler;
 	private DatabaseConnectionService connectionService;
 	private boolean serviceThreadIsReady = false;
 	DatabaseConnectionCallback databaseConnectionCallback = new DatabaseConnectionCallback();
 
 	private ServiceConnection serviceConnection = new ServiceConnection() {
 		
 		@Override
 		public void onServiceDisconnected(ComponentName className) {
 			connectionService = null;
 		}
 
 		@Override
 		public void onServiceConnected(ComponentName className, IBinder iBinder) {
 			// TODO Auto-generated method stub
 			Log.d(LOG_TAG, "servise connected");
 			connectionService = ((DatabaseConnectionService.MyBinder) iBinder).getService();
 			connectionService.setCallbackItem(databaseConnectionCallback);
 			//Log.d(LOG_TAG, "dialog " + progressDialog.isShowing());
 		}
 	};
 	
 	private class SMSBankingActivityHandler extends Handler{
 		static final int SHOW_ALL_CARDS_DATA = 101;
 		static final int SHOW_ONE_CARD_DATA = 102;
 		static final int NO_ERROR = 103;
 		static final int ERROR_HAPPENED = 104;
 		static final int SHOW_TRANSACTION_DATA = 1;
 		static final int SHOW_CARD_DATA = 2;
 		static final int ALIAS_WAS_UPDATED = 3;
 		static final int DATA_FROM_SMS_WAS_LOADED = 4;
 		static final int DATA_WAS_DELETED = 5;
 		static final int SET_BALANCE = 8;
 		static final int TREAD_IS_READY = 9;
 		
 		public SMSBankingActivityHandler(){
 			super();
 		}
 		
 		@Override
 		public void handleMessage(Message msg){
 	    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
 	    	SharedPreferences.Editor editor = settings.edit();
 			switch(msg.what){
 			case SHOW_TRANSACTION_DATA:
 				Log.d(LOG_TAG, "SMSBankingActivityHandler");
 				Cursor transactionCursor = (Cursor) msg.obj;
 				startManagingCursor(transactionCursor);
 				transactionAdapter = new TransactionAdapter(context, transactionCursor);
 				setListAdapter(transactionAdapter);
 				transactionAdapter.notifyDataSetChanged();
 				updateTransactionList();
 				break;
 			case SET_BALANCE:
 				Log.d(LOG_TAG, "Activity balance " + msg.obj);
 				if (msg.obj != null){
 					setBalance(msg.obj.toString());
 				}
 				break;
 			case SHOW_CARD_DATA:
 				boolean cardsExist = false;
 				Cursor cardCursor = (Cursor) msg.obj;
 				startManagingCursor(cardCursor);
 				if (cardCursor != null){
 					if (msg.arg1 == SHOW_ALL_CARDS_DATA){
 						if (cardCursor.moveToFirst()){
 							cardAdapter.clear();
 							cardsExist = true;
 							cardAdapter.add(context.getResources().getString(R.string.all));
 							do{
 								cardAdapter.add(cardCursor.getString(cardCursor.getColumnIndex(TransactionData.CARD_NUMBER)));
 							} while (cardCursor.moveToNext());
 							cardCursor.close();
 						}
 						cardAdapter.setNotifyOnChange(true);
 						if (cardsExist){
 							showDialog(DIALOG_CARD_FILTER);
 						}else {
 							Toast.makeText(context, resources.getText(R.string.no_data), 500).show();
 						}
 					}else{
 						if (cardCursor.moveToFirst()){
 							if(cardCursor.getString(cardCursor.getColumnIndex(MyDBAdapter.CARD_ALIAS)) != null){
 								cardAliasString = new String (cardCursor.getString(cardCursor.getColumnIndex(MyDBAdapter.CARD_ALIAS)));
 							}else{
 								cardAliasString = new String("");
 							}
 						}
 						else{
 							cardAliasString = new String("");
 						}
 						if (needToDeleteCard){
                             showDialog(DIALOG_DELETE_CARD_DATA);						    
 						}else{
 						    showDialog(DIALOG_CARD_DATA);
 						}
 					}
 				}else{
 					Toast.makeText(context, resources.getText(R.string.no_data), 500).show();
 				}
 				break;
 			case ALIAS_WAS_UPDATED:
 				if (msg.arg1 == ERROR_HAPPENED){
 				    Toast.makeText(context, resources.getText(R.string.error_happened), 500).show();
 				}
 				break;
 			case DATA_FROM_SMS_WAS_LOADED:
 				if (msg.arg1 == NO_ERROR){
 					Log.d(LOG_TAG, "LoadDataFromSMS success");
 			    	editor.putBoolean(NEED_TO_LOAD, false);
 			    	editor.commit();
 				}
 				else{
 			    	editor.putBoolean(NEED_TO_LOAD, true);
 			    	editor.commit();
 				}		
 				showTransactionList();
 				break;
 			case DATA_WAS_DELETED:
 				if (msg.arg1 == ERROR_HAPPENED){
 					Toast.makeText(context, resources.getText(R.string.error_happened), 500).show();
 				}else{
 			   		filterMap.remove(TransactionData.CARD_NUMBER);
 		    		filterMap.put(TransactionData.CARD_NUMBER, context.getResources().getString(R.string.all));
 		    		filterMap.remove(TransactionData.TRANSACTION_PLACE);
 		    		filterMap.put(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.all));
 
 		    	   	editor.putString(TransactionData.CARD_NUMBER, context.getResources().getString(R.string.all));
 		    	   	editor.putString(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.all));
 		    	   	editor.commit();
 				}
 				showTransactionList();
 				break;
 			case TREAD_IS_READY:
 				serviceThreadIsReady = true;
				if (progressDialog.isShowing()){
 					progressDialog.dismiss();
 				}
 				showTransactionList();
 				break;
 			}
 		}
 	}
 	
 	protected class UpdateReceiver extends BroadcastReceiver{
 
 		@Override
 		public void onReceive(Context arg0, Intent arg1) {
 			// TODO Auto-generated method stub
 			Log.d(LOG_TAG, "service " + connectionService);
 			showTransactionList();
 		}
 		
 	}
 	
 	private class DatabaseConnectionCallback implements DatabaseConnectionCallbackInterface{
 
 		public DatabaseConnectionCallback () {}
 
 		@Override
 		public void showTransactionData(Cursor cursor) {
 			// TODO Auto-generated method stub
 			Message msg = thisActivityHandler.obtainMessage();
 			msg.what = SMSBankingActivityHandler.SHOW_TRANSACTION_DATA;
 			msg.obj = cursor;
 			thisActivityHandler.sendMessage(msg);
 			Log.d(LOG_TAG, "DatabaseConnectionCallback");			
 		}
 		
 		@Override
 		public void showCardsData(Cursor cursor, String cardNumber){
 		    cardForDelete = cardNumber;
 			Message msg = thisActivityHandler.obtainMessage();
 			msg.what = SMSBankingActivityHandler.SHOW_CARD_DATA;
 			if (cardNumber.equals("")){
 				msg.arg1 = SMSBankingActivityHandler.SHOW_ALL_CARDS_DATA;
 			}else{
 				msg.arg1 = SMSBankingActivityHandler.SHOW_ONE_CARD_DATA;
 			}
 			msg.obj = cursor;
 			thisActivityHandler.sendMessage(msg);
 		}
 
 		@Override
 		public void aliasUpdated(boolean result) {
 			// TODO Auto-generated method stub
 			Message msg = thisActivityHandler.obtainMessage();
 			msg.what = SMSBankingActivityHandler.ALIAS_WAS_UPDATED;
 			if (!result){
 				msg.arg1 = SMSBankingActivityHandler.ERROR_HAPPENED;
 			}else{
 				msg.arg1 = SMSBankingActivityHandler.NO_ERROR;
 			}
 			thisActivityHandler.sendMessage(msg);
 		}
 
 		@Override
 		public void dataWasLoaded(boolean result) {
 			// TODO Auto-generated method stub
 			Message msg = thisActivityHandler.obtainMessage();
 			msg.what = SMSBankingActivityHandler.DATA_FROM_SMS_WAS_LOADED;
 			if (!result){
 				msg.arg1 = SMSBankingActivityHandler.ERROR_HAPPENED;
 			}else{
 				msg.arg1 = SMSBankingActivityHandler.NO_ERROR;
 			}
 			thisActivityHandler.sendMessage(msg);
 		}
 
 		@Override
 		public void dataWasDeleted(boolean result) {
 			// TODO Auto-generated method stub
 			Message msg = thisActivityHandler.obtainMessage();
 			msg.what = SMSBankingActivityHandler.DATA_WAS_DELETED;
 			if (!result){
 				msg.arg1 = SMSBankingActivityHandler.ERROR_HAPPENED;
 			}else{
 				msg.arg1 = SMSBankingActivityHandler.NO_ERROR;
 			}
 			thisActivityHandler.sendMessage(msg);			
 		}
 
 		@Override
 		public void setBalance(String balanceValue) {
 			// TODO Auto-generated method stub
 			Message msg = thisActivityHandler.obtainMessage();
 			msg.what = SMSBankingActivityHandler.SET_BALANCE;
 			msg.obj = balanceValue;
 			thisActivityHandler.sendMessage(msg);			
 		}
 
 		@Override
 		public void onReady() {
 			// TODO Auto-generated method stub
 			Message msg = thisActivityHandler.obtainMessage();
 			msg.what = SMSBankingActivityHandler.TREAD_IS_READY;
 			thisActivityHandler.sendMessage(msg);				
 		}
 
         @Override
         public void cardDataWasDeleted() {
             // TODO Auto-generated method stub
             showTransactionList();
         }
 
 	}
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
 	    viewIntent = getIntent();
 		bundle = viewIntent.getExtras();
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.view_history);         
 	    context = getApplicationContext();
 	    resources = context.getResources();
         
 	    isChecked = false;
 	    thisActivityHandler = new SMSBankingActivityHandler();
 	    
 	   	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
     	boolean usingPassword = settings.getBoolean(resources.getString(R.string.using_password), false);
         filterMap = new HashMap<String, String>();
         filterMap.put(TransactionData.CARD_NUMBER, settings.getString(TransactionData.CARD_NUMBER, resources.getString(R.string.all)));
         filterMap.put(TransactionData.TRANSACTION_PLACE, settings.getString(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.all)));
         if (!settings.contains(NEED_TO_LOAD)){
         	SharedPreferences.Editor editorSettings = settings.edit();
     		editorSettings.putBoolean(NEED_TO_LOAD, true);
     		editorSettings.commit();
     		Log.d(LOG_TAG, "NEED_TO_LOAD, true");
     		firstLoading = true;
         }
     	
     	//Log.d(LOG_TAG, "passw " + usingPassword);
     
     	if ((usingPassword) && (!isChecked)){
         	if (!settings.contains(PASSWORD_TEXT))
         	{
         		SharedPreferences.Editor editorSettings = settings.edit();
         		editorSettings.putString(PASSWORD_TEXT, DEFAULT_PASSWORD);
         		editorSettings.commit();
         		currentPassword = new String(DEFAULT_PASSWORD);
         		Log.d(LOG_TAG, "create PASSWORD");
         	}
         	else
         	{
         		currentPassword = settings.getString(PASSWORD_TEXT, DEFAULT_PASSWORD);
         		Log.d(LOG_TAG, "use previous PASSWORD");
         	}
  	    	showDialog(DIALOG_PASSWORD_CHECKING);
     	}
     	else
     	{
     		isChecked = true;
     	}
 	        
         try{
         backupDb();
         }
        catch (Exception e) {}
         finally{
         }
     }
     
     private void prepareActivity(){
         cardAdapter = new ArrayAdapter<String>(context, android.R.layout.select_dialog_item);
 		
 		listView = getListView();
 		//Log.d(LOG_TAG, "listView ");
 		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
 			 @Override
 			    public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
 				 return onListItemLongClick(pos, id);
 			    }
 		});
 		listView.setClickable(true);
 		//Log.d(LOG_TAG, "listView after");
 		
 	   	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
 		if (!settings.getBoolean(NEED_TO_LOAD, true)){
 			Log.d(LOG_TAG, "do not load data");
 			showActivityData();
 		}else{
 			Log.d(LOG_TAG, "show dialog load data");
 			showDialog(DIALOG_NEED_TO_LOAD);
 		}
     }
     
     protected void showActivityData(){
     	Log.d(LOG_TAG, "showActivityData");
 		if(bundle != null){
 			if (viewIntent.getAction().equals(VIEW_TRANSACTION_DETAIL_INTENT)){
 				//Log.d(LOG_TAG, "onCreate " + viewIntent.getAction());
 				transactionData = new TransactionData(bundle);
 				bundle = null;	
 				showDialog(DIALOG_SMS_DETAIL);
 			}
 		}
     }
     
     @Override
     protected void onNewIntent(Intent intent){
     	super.onNewIntent(intent);
     	if (isChecked){
 	    	//Log.d(LOG_TAG, "onNewIntent " + intent.getAction());
 	    	if (intent.getAction().equals(UPDATE_TRANSACTION_LIST_INTENT)){
 	    		//Log.d(LOG_TAG, "onNewIntent 111" + intent.getAction() + intent.getExtras());
 	    		showTransactionList();
 	    	}else
 	    	    if(intent.getAction().equals(VIEW_TRANSACTION_DETAIL_INTENT)){
 	    	        transactionData = new TransactionData(intent.getExtras());
 	    	        //Log.d(LOG_TAG, "showDialog " + intent.getAction() + intent.getExtras());
 	    	        showDialog(DIALOG_SMS_DETAIL);
 	    	    }
     	}
    	
     }
     
     private void backupDb() throws IOException {
         File sd = Environment.getExternalStorageDirectory();
         File data = Environment.getDataDirectory();
 
         if (sd.canWrite()) {
             String currentDBPath = "/data/com.meryrua.smsbanking/databases/smsbanking_base";
             String backupDBPath = "/temp/smsbanking_base";
 
             File currentDB = new File(data, currentDBPath);
             File backupDB = new File(sd, backupDBPath);
 
             if (backupDB.exists())
                 backupDB.delete();
 
             if (currentDB.exists()) {
                 makeLogsFolder();
                 copy(currentDB, backupDB);
            }
        }
     }
     
     private void copy(File from, File to) throws FileNotFoundException, IOException {
         FileChannel src = null;
         FileChannel dst = null;
         try {
             src = new FileInputStream(from).getChannel();
             dst = new FileOutputStream(to).getChannel();
             dst.transferFrom(src, 0, src.size());
         }catch(FileNotFoundException ex1){
         	Log.d(LOG_TAG, "FileNotFoundException");
         }catch(IOException ex1){
         	Log.d(LOG_TAG, "IOException");
         }
         finally {
             if (src != null)
                 src.close();
             if (dst != null)
                 dst.close();
         }
     }
 
     private void makeLogsFolder() {
        try {
            File sdFolder = new File(Environment.getExternalStorageDirectory(), "/temp/");
            sdFolder.mkdirs();
        }
        catch (Exception e) {}
      }
     
 
     public boolean onCreateOptionsMenu(Menu menu){
     	menu.add(Menu.NONE, IDM_PREFERENCES, Menu.NONE, resources.getString(R.string.settings));
     	menu.add(Menu.NONE, IDM_CARD_FILTER, Menu.NONE, resources.getString(R.string.card_filter));
         menu.add(Menu.NONE, IDM_DELETE_CARD, Menu.NONE, resources.getString(R.string.delete_card));
     	//menu.add(Menu.NONE, IDM_TEST, Menu.NONE, "TEST");
     	menu.add(Menu.NONE, IDM_DELETE_DATA, Menu.NONE, resources.getString(R.string.delete_data));
     	SubMenu subMenuFilters = menu.addSubMenu(resources.getString(R.string.operation_filter));
     	subMenuFilters.add(Menu.NONE, IDM_OPERATION_FILTER_ALL_OPERATION, Menu.NONE, resources.getString(R.string.all));
     	subMenuFilters.add(Menu.NONE, IDM_OPERATION_FILTER_CARD_OPERATION, Menu.NONE, resources.getString(R.string.card_operations));
     	subMenuFilters.add(Menu.NONE, IDM_OPERATION_FILTER_INCOMING_OPERATION, Menu.NONE, resources.getString(R.string.incoming_operations));
     	subMenuFilters.add(Menu.NONE, IDM_OPERATION_FILTER_OUTGOING_OPERATION, Menu.NONE, resources.getString(R.string.outgoing_operations));
     	subMenuFilters.setIcon(android.R.drawable.ic_menu_more);
     	return true;
     }
     
     public boolean onOptionsItemSelected(MenuItem item){
     	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
     	SharedPreferences.Editor editor = settings.edit();
     	switch(item.getItemId()){
     	case IDM_PREFERENCES:
     		Intent intentSettings = new Intent();
     		intentSettings.setClass(context, Settings.class);
     		startActivity(intentSettings);   
     		return true;
     	case IDM_CARD_FILTER:
     	    needToDeleteCard = false;
     		if ((connectionService != null) && (serviceThreadIsReady)){
     		    connectionService.getCardsData("");
     		}
     		return true;
     	case IDM_DELETE_CARD:
     	    needToDeleteCard = true;
     	    if ((connectionService != null) && (serviceThreadIsReady)){
                 connectionService.getCardsData("");
             }
     	    return true;
        	case IDM_TEST:
        		Intent intent = new Intent();
        		intent.setClass(context, SMSViewingActivity.class);
        		startActivity(intent);
     		return true;
        	case IDM_DELETE_DATA:
        		showDialog(DIALOG_LOAD_DATA_REQUEST);
     		return true;
     	case IDM_OPERATION_FILTER_ALL_OPERATION:
     		filterMap.remove(TransactionData.TRANSACTION_PLACE);
     		filterMap.put(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.all));
 
     	   	editor.putString(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.all));
     	   	editor.commit();
     	   	showTransactionList();
     		return true;
     	case IDM_OPERATION_FILTER_CARD_OPERATION:
     		filterMap.remove(TransactionData.TRANSACTION_PLACE);
     		filterMap.put(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.card_operations));
     		
     	   	editor.putString(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.card_operations));
     	   	editor.commit();
     		showTransactionList();
     		return true;
     	case IDM_OPERATION_FILTER_INCOMING_OPERATION:
     		filterMap.remove(TransactionData.TRANSACTION_PLACE);
     		filterMap.put(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.incoming_operations));
 
     		editor.putString(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.incoming_operations));
     	   	editor.commit();
     	   	showTransactionList();
         	return true;
     	case IDM_OPERATION_FILTER_OUTGOING_OPERATION:
     		filterMap.remove(TransactionData.TRANSACTION_PLACE);
     		filterMap.put(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.outgoing_operations));
     		
     		editor.putString(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.outgoing_operations));
     	   	editor.commit();
     	   	showTransactionList();
     		return true;
         }
     	return false;
     }
     
     @Override
     protected void onResume(){
     	super.onResume();
         bindService(new Intent(context, DatabaseConnectionService.class), serviceConnection, Context.BIND_AUTO_CREATE);
         if (isChecked){
             //showDialog(DIALOG_LOADING);
             progressDialog = new ProgressDialog(this);
             progressDialog.setCancelable(false);
             progressDialog.setMessage(resources.getText(R.string.loading));
             progressDialog.show();
             Log.d(LOG_TAG, "onStart ");
             prepareActivity();
             showTransactionList();
         }
         updateIntentFilter = new IntentFilter(UPDATE_TRANSACTION_LIST_INTENT);
         updateIntentFilter.addCategory("android.intent.category.DEFAULT");
         updateReceiver = new UpdateReceiver();
         registerReceiver(updateReceiver, updateIntentFilter);
     }
     
     @Override
     protected void onPause(){
     	super.onPause();
     	Log.d(LOG_TAG, "onPause");
         unbindService(serviceConnection);
         unregisterReceiver(updateReceiver);
     }
     
     @Override
     public void onConfigurationChanged(Configuration newConfig){
     	Log.d(LOG_TAG, "onConfigurationChanged");
     	if (isChecked){
     		prepareActivity();
     	}else{
     		showDialog(DIALOG_PASSWORD_CHECKING);
     	}
     	super.onConfigurationChanged(newConfig);
     }
         
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		transactionData =  new TransactionData((Cursor)getListAdapter().getItem(position));
 		showDialog(DIALOG_SMS_DETAIL);
  	}
 	
 	protected boolean onListItemLongClick(int pos, long id){
 		transactionData = new TransactionData((Cursor)getListAdapter().getItem(pos));
 	   	if ((connectionService != null) && (serviceThreadIsReady)){
 	   	    connectionService.getCardsData(transactionData.getCardNumber());
 	   	}
 		return true;
 	}
 	
 	@Override
 	protected Dialog onCreateDialog(int id){
 		AlertDialog alertDialog;
 		switch (id){
 		case DIALOG_SMS_DETAIL:
 			AlertDialog.Builder smsDetailDialogBuilder;
 			
 			LayoutInflater inflaterSMSDetail = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
 			View layoutSMSDetail = inflaterSMSDetail.inflate(R.layout.sms_detail, (ViewGroup) findViewById(R.id.sms_detail_layout));
 			
 			smsDetailDialogBuilder = new AlertDialog.Builder(this);
 			smsDetailDialogBuilder.setView(layoutSMSDetail);
 			smsDetailDialogBuilder.setTitle(resources.getString(R.string.detail_info));
 			smsDetailDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					dialog.dismiss();
 				}
 			});
 			alertDialog = smsDetailDialogBuilder.create();
 			//Log.d(LOG_TAG, "Dialog DIALOG_SMS_DETAIL create");
 			break;
 		case DIALOG_LOADING:
 			AlertDialog.Builder loadingDialogBuilder;
 			
 			LayoutInflater inflaterLoading = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
 			View layoutLoading = inflaterLoading.inflate(R.layout.my_progress, (ViewGroup) findViewById(R.id.my_progress_layout));
 			
 			loadingDialogBuilder = new AlertDialog.Builder(this);
 			loadingDialogBuilder.setView(layoutLoading);
 			alertDialog = loadingDialogBuilder.create();
 			break;
 		case DIALOG_CARD_FILTER:
 			AlertDialog.Builder cardFilterBuilder = new AlertDialog.Builder(this);
 			cardFilterBuilder.setAdapter(cardAdapter, new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					String newSelection = cardAdapter.getItem(which);
 					if (needToDeleteCard){
 					    if ((connectionService != null) && (serviceThreadIsReady)){
 					        connectionService.getCardsData(newSelection);
 			            }					    
 					}else{
     			   		filterMap.remove(TransactionData.CARD_NUMBER);
     		    		filterMap.put(TransactionData.CARD_NUMBER, newSelection);
     		    		
     		    	   	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
     		    	   	SharedPreferences.Editor editor = settings.edit();
     		    	   	editor.putString(TransactionData.CARD_NUMBER, newSelection);
     		    	   	editor.commit();
     		    	   	showTransactionList();
 					}
 					dialog.dismiss();
 				}
 			});
 			//for this style of Adapter should be android.R.layout.select_dialog_singlechoice
 			/*cardFilterBuilder.setSingleChoiceItems(cardAdapter, selectedCard, new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					String newSelection = cardAdapter.getItem(which);
 			   		filterMap.remove(TransactionData.CARD_NUMBER);
 		    		filterMap.put(TransactionData.CARD_NUMBER, newSelection);
 		    		showTransactionList();
 				}
 			});*/
 			alertDialog = cardFilterBuilder.create();
 			break;
 		case DIALOG_CARD_DATA:
 			AlertDialog.Builder cardDataDialogBuilder;
 			
 			LayoutInflater inflaterCardData = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
 			View layoutCardData = inflaterCardData.inflate(R.layout.card_data, (ViewGroup) findViewById(R.id.card_data_layout));
 			
 			cardDataDialogBuilder = new AlertDialog.Builder(this);
 			cardDataDialogBuilder.setView(layoutCardData);
 			cardDataDialogBuilder.setPositiveButton(resources.getString(R.string.save), new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					if (cardAliasString != null){
 						if ((connectionService != null) && (serviceThreadIsReady)){
 						    connectionService.updateCardAlias(transactionData.getCardNumber(), cardAliasString);
 						}
 					}
 				}
 			});
 			cardDataDialogBuilder.setNegativeButton(resources.getString(R.string.close), new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					dialog.dismiss();
 				}
 			});
 			cardDataDialogBuilder.setTitle(R.string.card_description);		
 			alertDialog = cardDataDialogBuilder.create();
 			break;
 		case DIALOG_PASSWORD_CHECKING:
 			AlertDialog.Builder passwordDialogBuilder;
 			
 			LayoutInflater inflaterPassword = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
 			View layoutPassword = inflaterPassword.inflate(R.layout.password, (ViewGroup) findViewById(R.id.password_layout));
 			
 			passwordDialogBuilder = new AlertDialog.Builder(this);
 			passwordDialogBuilder.setView(layoutPassword);
			passwordDialogBuilder.setPositiveButton(resources.getString(R.string.save), new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 						passwordCheck();
 				}
 			});
 			passwordDialogBuilder.setNegativeButton(resources.getString(R.string.close), new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					finish();
 				}
 			});
 			passwordDialogBuilder.setCancelable(false);
 			passwordDialogBuilder.setTitle(R.string.input_password);
 			alertDialog = passwordDialogBuilder.create();
 			break;
 		case DIALOG_WRONG_PASSWORD:
 			AlertDialog.Builder wrongPasswordDialogBuilder;
 			
 			wrongPasswordDialogBuilder = new AlertDialog.Builder(this);
 			wrongPasswordDialogBuilder.setMessage(resources.getString(R.string.wrong_password));
 			alertDialog = wrongPasswordDialogBuilder.create();
 			//Log.d(LOG_TAG, "Dialog DIALOG_WRONG_PASSWORD create");
 			break;
 		case DIALOG_LOAD_DATA_REQUEST:
 			AlertDialog.Builder loadDataRequestDialog;
 			
 			loadDataRequestDialog = new AlertDialog.Builder(this);
 			loadDataRequestDialog.setMessage(resources.getString(R.string.load_from_sms));
 			loadDataRequestDialog.setPositiveButton(resources.getString(R.string.yes), new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					if ((connectionService != null) && (serviceThreadIsReady)){
 					    connectionService.deleteAllData(true);
 					}
 				}
 			});
 			loadDataRequestDialog.setNegativeButton(resources.getString(R.string.no), new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					if ((connectionService != null) && (serviceThreadIsReady)){
 					    connectionService.deleteAllData(false);
 					}
 				}
 			});
 			alertDialog = loadDataRequestDialog.create();
 			
 			break;
 		case DIALOG_NEED_TO_LOAD:
 			String message;
 			AlertDialog.Builder firstDataLoadDialog;
 			
 			firstDataLoadDialog = new AlertDialog.Builder(this);
 			if (firstLoading){
 				firstLoading = false;
 				message = new String(resources.getString(R.string.first_data_load) + " " + resources.getString(R.string.load_from_sms_request));
 			}else{
 				message = resources.getString(R.string.first_data_load);
 			}
 			firstDataLoadDialog.setMessage(message);
 			firstDataLoadDialog.setPositiveButton(resources.getString(R.string.yes), new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					if ((connectionService != null) && (serviceThreadIsReady)){
 					    connectionService.loadDataFromSMS();
 					}
 				}
 			});
 			firstDataLoadDialog.setNegativeButton(resources.getString(R.string.no), new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					dialog.dismiss();
 				}
 			});
 			alertDialog = firstDataLoadDialog.create();
 			//Log.d(LOG_TAG, "Dialog DIALOG_WRONG_PASSWORD create");
 			break;
 		case DIALOG_DELETE_CARD_DATA:
 	        AlertDialog.Builder deleteCardDataDialog = new AlertDialog.Builder(this);
 	            
             deleteCardDataDialog.setPositiveButton(resources.getString(R.string.yes), new DialogInterface.OnClickListener() {
 	                
 	            @Override
 	            public void onClick(DialogInterface dialog, int which) {
 	                // TODO Auto-generated method stub
                     if ((connectionService != null) && (serviceThreadIsReady)){
                         connectionService.deleteCardData(cardForDelete);
                     }
 	            }
 	            });
             deleteCardDataDialog.setNegativeButton(resources.getString(R.string.no), new DialogInterface.OnClickListener() {
                 
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     // TODO Auto-generated method stub
                     dialog.dismiss();
                 }
             });
             deleteCardDataDialog.setMessage("");
 	        alertDialog = deleteCardDataDialog.create();
 		    break;
 		default:
 			alertDialog = null;
 		}
 		return alertDialog;
 	}
 
 	private void passwordCheck(){
 		if (currentPassword.equals(passwordString)){
 			isChecked = true;
 			prepareActivity();
 		}else{
 			showDialog(DIALOG_WRONG_PASSWORD);
 			finish();
 		}
 	}
 
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog){
 		switch (id){
 			case DIALOG_SMS_DETAIL:
 			
 				TextView cardNumberText = (TextView) dialog.findViewById(R.id.card_number);
 		        cardNumberText.setText(transactionData.getCardNumber());
 		        
 		        TextView dateText = (TextView) dialog.findViewById(R.id.date);
 		        dateText.setText(transactionData.getTransactionDate());
 
 		        TextView amountText = (TextView) dialog.findViewById(R.id.amount);
 		        String tranzValue = new String(Float.toString(transactionData.getTransactionValue()).replace(".", ","));
 		        tranzValue += transactionData.getTransactionCurrency();
 		        amountText.setText(tranzValue);
 		        
 		        TextView placeText = (TextView) dialog.findViewById(R.id.place);
 		        String placeOrOperation = transactionData.getTransactionPlace();
 		        if (placeOrOperation.equals(TransactionData.INCOMING_BANK_OPERATION)){
 		        	placeText.setText(resources.getString(R.string.operation_incoming_name));
 		        }else
 		            if (placeOrOperation.equals(TransactionData.OUTGOING_BANK_OPERATION)){
 		                placeText.setText(resources.getString(R.string.operation_outgoing_name));
 		            }else {
 		                placeText.setText(transactionData.getTransactionPlace());
 		            }
 
 		        TextView balanceText = (TextView) dialog.findViewById(R.id.balance);
 		        String balanceValue = new String(Float.toString(transactionData.getFundValue()).replace(".", ","));
 		        balanceValue += transactionData.getFundCurrency();
 		        //Log.d(LOG_TAG, "balance " + balanceValue);
 		        balanceText.setText(balanceValue);
 				break;
 			case DIALOG_CARD_FILTER:
 				/*
 				cardAdapter.clear();
 				getCardsNumber(cardAdapter);
 				cardAdapter.setNotifyOnChange(true);*/
 				break;
 			case DIALOG_CARD_DATA:
 				final EditText cardAlias = (EditText) dialog.findViewById(R.id.card_alias);
 				cardAlias.addTextChangedListener(new TextWatcher(){
 					@Override
 					public void afterTextChanged(Editable s) {
 						cardAliasString = new String (cardAlias.getText().toString());
 					}
 
 					@Override
 					public void beforeTextChanged(CharSequence arg0, int arg1,
 							int arg2, int arg3) {
 						// TODO Auto-generated method stub
 						
 					}
 
 					@Override
 					public void onTextChanged(CharSequence s, int start,
 							int before, int count) {
 						// TODO Auto-generated method stub
 						
 					}
 				});
 				cardAlias.setText(cardAliasString);
 				break;
 			case DIALOG_PASSWORD_CHECKING:
 				TextView repeatPassword = (TextView) dialog.findViewById(R.id.repeat_password_field);
 				repeatPassword.setVisibility(TextView.GONE);
 				TextView repeatPasswordText = (TextView) dialog.findViewById(R.id.repeat_password_text);
 				repeatPasswordText.setVisibility(TextView.GONE);
 
 				String passwordText = new String("");
 				
 				final EditText passwordField = (EditText) dialog.findViewById(R.id.password_field);
 				passwordField.addTextChangedListener(new TextWatcher(){
 					@Override
 					public void afterTextChanged(Editable s) {
 						passwordString = new String (passwordField.getText().toString());
 					}
 
 					@Override
 					public void beforeTextChanged(CharSequence arg0, int arg1,
 							int arg2, int arg3) {
 						// TODO Auto-generated method stub
 						
 					}
 
 					@Override
 					public void onTextChanged(CharSequence s, int start,
 							int before, int count) {
 						// TODO Auto-generated method stub
 						
 					}
 				});
 				passwordField.setText(passwordText);
 				break;
 			case DIALOG_DELETE_CARD_DATA:
 			    String aliasString = new String("");
 		        aliasString += (cardAliasString.equals("")?(""):("(" + cardAliasString + ")"));
 		        String dialogMessage = new String (resources.getString(R.string.delete_card_request) +
 		                    " " + cardForDelete + " " + aliasString);
 		        Log.d(LOG_TAG, "message " + dialogMessage);
 		        ((AlertDialog) dialog).setMessage(dialogMessage);
 			    break;
 		}
 	}
 	
 	private String getSQLWhereFromFilter(){
 		String sqlString = new String();
 		if (!filterMap.get(TransactionData.CARD_NUMBER).equals(resources.getString(R.string.all))){
 			sqlString += TransactionData.CARD_NUMBER + "='" + filterMap.get(TransactionData.CARD_NUMBER) + "'";
 		}
 		if (!filterMap.get(TransactionData.TRANSACTION_PLACE).equals(resources.getString(R.string.all))){
 			if (sqlString.length() != 0){
 			    sqlString += " AND ";
 			}
 			
 	    	if (filterMap.get(TransactionData.TRANSACTION_PLACE).equals(resources.getString(R.string.card_operations))){
 	    		sqlString += "(" + TransactionData.TRANSACTION_PLACE + "<>'" + TransactionData.INCOMING_BANK_OPERATION + "') AND (" + TransactionData.TRANSACTION_PLACE + "<>'" + TransactionData.OUTGOING_BANK_OPERATION + "')";
 	    	} else
 	    	    if (filterMap.get(TransactionData.TRANSACTION_PLACE).equals(resources.getString(R.string.incoming_operations))) {
 	    	        sqlString += TransactionData.TRANSACTION_PLACE + "='" + TransactionData.INCOMING_BANK_OPERATION + "'";
 	    	    } else {
 	    	        sqlString += TransactionData.TRANSACTION_PLACE + "='" + TransactionData.OUTGOING_BANK_OPERATION + "'";
 	    	    }
 		}
 		return sqlString;
 	}
 	
 	private void showTransactionList(){
 		if (isChecked){
 			if ((connectionService != null) && (serviceThreadIsReady)){
 		   		connectionService.getTransactionData(getSQLWhereFromFilter());
 				if (!filterMap.get(TransactionData.CARD_NUMBER).equals(resources.getString(R.string.all))){
 					connectionService.getBalance(filterMap.get(TransactionData.CARD_NUMBER));
 				}else{
 					hideBalance();
 				}
 		   	}else{
 		   	    updateTransactionList();
 		   	}
 		}
 	}
 	
 	private void setBalance(String balance){
 		TextView curBalance = (TextView) findViewById(R.id.current_balance);
 		TextView filterInfo = (TextView) findViewById(R.id.filter_information);
         String str = new String();
         Log.d(LOG_TAG, "setBalance " + balance);
         if (balance != null){
 	        str += resources.getString(R.string.operation_balance) + " " + balance;
 	        curBalance.setVisibility(TextView.VISIBLE);
 	        curBalance.setText(str);
         }
         String str1 = new String();
         str1 += resources.getString(R.string.operation_card_number) + filterMap.get(TransactionData.CARD_NUMBER);
         filterInfo.setVisibility(TextView.VISIBLE);
         filterInfo.setText(str1);
 	}
 	
 	private void  hideBalance(){
 		TextView curBalance = (TextView) findViewById(R.id.current_balance);
 		TextView filterInfo = (TextView) findViewById(R.id.filter_information);
 		if (!filterMap.get(TransactionData.TRANSACTION_PLACE).equals(resources.getString(R.string.all))){
 			curBalance.setText(null);
 			curBalance.setVisibility(TextView.GONE);
 		    String str1 = new String();
 		    str1 += filterMap.get(TransactionData.TRANSACTION_PLACE);
 		    filterInfo.setVisibility(TextView.VISIBLE);
 		    filterInfo.setText(filterMap.get(TransactionData.TRANSACTION_PLACE));			
 		}else{
 			curBalance.setText(null);
 			curBalance.setVisibility(TextView.GONE);
 			filterInfo.setText(null);
 			filterInfo.setVisibility(TextView.GONE);
 		}
 	}
 	
 	private void updateTransactionList(){
 		TextView filterInfo = (TextView) findViewById(R.id.filter_information);
 		TextView noDataField = (TextView) findViewById(R.id.no_data_field);
 		TextView noDataFieldInfo = (TextView) findViewById(R.id.no_data_field_info);
 		TextView curBalance = (TextView) findViewById(R.id.current_balance);
 	
 		if (transactionAdapter != null) {
 			if (transactionAdapter.getCursor() != null)	transactionAdapter.getCursor().requery();
 			
 			if ((transactionAdapter.getCursor() != null) && (transactionAdapter.getCursor().moveToFirst())){
 				listView.setVisibility(ListView.VISIBLE);
 				noDataField.setVisibility(TextView.GONE);
 				noDataFieldInfo.setVisibility(TextView.GONE);
 			}else {
 				filterInfo.setVisibility(TextView.GONE);
 				listView.setVisibility(TextView.GONE);
 				noDataField.setVisibility(TextView.VISIBLE);
 				noDataFieldInfo.setVisibility(TextView.VISIBLE);
 			}
 			transactionAdapter.notifyDataSetChanged();
 		}else{
 			Log.d(LOG_TAG, "updateTransactionList null ");
 			curBalance.setText(null);
 			curBalance.setVisibility(TextView.GONE);
 			filterInfo.setVisibility(TextView.GONE);
 			listView.setVisibility(TextView.GONE);
 			noDataField.setVisibility(TextView.VISIBLE);
 			noDataFieldInfo.setVisibility(TextView.VISIBLE);			
 		}
 
 	}
 	
 	class BindServiceTask extends AsyncTask<Void, Void, Void>{
 		private final ProgressDialog dialog = new ProgressDialog(SMSBankingActivity.this);
 		
 		@Override
 		protected void onPreExecute(){
 	    	dialog.setCancelable(false);
 	    	dialog.setMessage(resources.getText(R.string.loading));
 	    	dialog.show();
 		}
 
 		@Override
 		protected Void doInBackground(Void... arg0) {
 			// TODO Auto-generated method stub
 		   	bindService(new Intent(context, DatabaseConnectionService.class), serviceConnection, Context.BIND_AUTO_CREATE);
 			return null;
 		}
 		
 		@Override
 		protected void onCancelled(){
 			dialog.dismiss();			
 		}
 		
 		@Override
 		protected void onPostExecute(Void arg){
 			dialog.dismiss();
 		}
 	}
 }
