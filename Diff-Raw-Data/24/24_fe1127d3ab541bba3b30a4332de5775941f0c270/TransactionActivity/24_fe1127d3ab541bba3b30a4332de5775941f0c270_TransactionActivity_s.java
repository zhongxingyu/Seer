 package net.gumbercules.loot;
 
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Currency;
 import java.util.Date;
 
 import net.gumbercules.loot.TransactionAdapter.TransactionFilter;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.Editable;
 import android.text.SpannableString;
 import android.text.Spanned;
 import android.text.TextUtils;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.DatePicker;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.MultiAutoCompleteTextView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.MultiAutoCompleteTextView.Tokenizer;
 
 public class TransactionActivity extends ListActivity
 {
 	public static final String KEY_REQ		= "t_req";
 	public static final int ACTIVITY_CREATE	= 0;
 	public static final int ACTIVITY_EDIT	= 1;
 	public static final int ACTIVITY_DEL	= 2;
 	
 	public static final String KEY_TYPE		= "t_type";
 	public static final int TRANSACTION		= 0;
 	public static final int TRANSFER		= 1;
 	
 	public static final int NEW_TRANSACT_ID	= Menu.FIRST;
 	public static final int NEW_TRANSFER_ID	= Menu.FIRST + 1;
 	public static final int SORT_ID			= Menu.FIRST + 2;
 	public static final int SEARCH_ID		= Menu.FIRST + 3;
 	public static final int PURGE_ID		= Menu.FIRST + 4;
 	public static final int SETTINGS_ID		= Menu.FIRST + 5;
 	
 	public static final int CONTEXT_EDIT	= Menu.FIRST;
 	public static final int CONTEXT_COPY	= Menu.FIRST + 1;
 	public static final int CONTEXT_POST	= Menu.FIRST + 2;
 	public static final int CONTEXT_DEL		= Menu.FIRST + 3;
 	public static final int CONTEXT_REPEAT	= Menu.FIRST + 4;
 	
 	private static ArrayList<Transaction> mTransList;
 	private static Account mAcct;
 	private static TransactionAdapter mTa;
 	
 	private MultiAutoCompleteTextView searchEdit;
 	
 	private TextView budgetValue;
 	private TextView balanceValue;
 	private TextView postedValue;
 	
 	private static boolean showSearch = false;
 	private static String searchString = "";
 	
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
     	super.onCreate(savedInstanceState);
     	setContentView(R.layout.main);
         
     	Bundle bun = getIntent().getExtras();
     	int acct_id = bun.getInt(Account.KEY_ID);
     	boolean new_account = false;
     	if (mAcct == null || acct_id != mAcct.id())
     	{
     		new_account = true;
     		mAcct = Account.getAccountById(acct_id);
     	}
     	setTitle("loot :: " + mAcct.name);
     	
     	int auto_purge = (int)Database.getOptionInt("auto_purge_days");
     	if (auto_purge > 0)
     	{
     		Calendar cal = Calendar.getInstance();
     		cal.add(Calendar.DAY_OF_YEAR, -auto_purge);
     		int[] ids = mAcct.purgeTransactions(cal.getTime());
     		if (ids != null && mTa != null && !mTa.isEmpty())
     			mTa.remove(ids);
     	}
     	
     	budgetValue = (TextView)findViewById(R.id.budgetValue);
     	balanceValue = (TextView)findViewById(R.id.balanceValue);
     	postedValue = (TextView)findViewById(R.id.postedValue);
 
     	// add a listener to filter the list whenever the text changes
     	searchEdit = (MultiAutoCompleteTextView)findViewById(R.id.SearchEdit);
     	
     	// add a listener to clear searchEdit when pressed
     	ImageButton clearButton = (ImageButton)findViewById(R.id.ClearButton);
     	clearButton.setOnClickListener(new ImageButton.OnClickListener()
     	{
 			public void onClick(View v)
 			{
 				searchEdit.setText("");
 			}
     	});
     	
     	// find current orientation and send proper layout to constructor
     	int layoutResId = R.layout.trans_row_narrow;
     	if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
     		layoutResId = R.layout.trans_row_wide;
     	
     	if (new_account)
     	{
 	    	mTransList = new ArrayList<Transaction>();
 		    mTa = new TransactionAdapter(this, layoutResId, mTransList, mAcct.id());
 	        setListAdapter(mTa);
 	        fillList();
     	}
     	else
     	{
     		mTa.setResource(layoutResId);
     		mTa.setContext(this);
     		setListAdapter(mTa);
     		setBalances();
     	}
         
     	TextWatcher searchChanged = new TextWatcher()
     	{
     		// we only care what the end result is
 			public void afterTextChanged(Editable s)
 			{
 				searchString = s.toString();
 				TransactionFilter f = (TransactionFilter)mTa.getFilter();
 				f.publish(searchString, f.filtering(searchString));
 			}
 
 			public void beforeTextChanged(CharSequence s, int start, int count, int after)
 			{
 				if (after == 0)
 				{
 					searchString = "";
 					TransactionFilter f = (TransactionFilter)mTa.getFilter();
 					f.publish(searchString, f.filtering(searchString));
 				}
 			}
 
 			public void onTextChanged(CharSequence s, int start, int before, int count) { }
     	};
     	searchEdit.addTextChangedListener(searchChanged);
 
     	ListView view = getListView();
         registerForContextMenu(view);
         view.setStackFromBottom(true);
 
         // show the search if the orientation has changed and the activity has restarted
     	if (showSearch)
     	{
     		toggleSearch();
 			TransactionFilter f = (TransactionFilter)mTa.getFilter();
 			searchEdit.setText(searchString);
 			f.publish(searchString, f.filtering(searchString));
     	}
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
     	boolean result = super.onCreateOptionsMenu(menu);
     	menu.add(0, NEW_TRANSACT_ID, 0, R.string.new_trans)
     		.setShortcut('1', 'n')
     		.setIcon(android.R.drawable.ic_menu_add);
     	
     	// only show transfers if there is more than one account
     	if (Account.getAccountIds().length > 1)
     		menu.add(0, NEW_TRANSFER_ID, 0, R.string.transfer)
     			.setShortcut('2', 't')
     			.setIcon(android.R.drawable.ic_menu_send);
     	
     	menu.add(0, SORT_ID, 0, R.string.sort)
     		.setShortcut('3', 'o')
     		.setIcon(android.R.drawable.ic_menu_sort_by_size);
     	menu.add(0, SEARCH_ID, 0, R.string.search)
     		.setShortcut('4', 'e')
     		.setIcon(android.R.drawable.ic_menu_search);
     	menu.add(0, PURGE_ID, 0, R.string.purge)
     		.setShortcut('5', 'p')
     		.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
     	menu.add(0, SETTINGS_ID, 0, R.string.settings)
     		.setShortcut('6', 's')
     		.setIcon(android.R.drawable.ic_menu_preferences);
     	
     	return result;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
     	switch (item.getItemId())
     	{
     	case NEW_TRANSACT_ID:
     		createTransaction();
     		return true;
     		
     	case NEW_TRANSFER_ID:
     		createTransfer();
     		return true;
     		
     	case SORT_ID:
     		sortDialog();
     		return true;
     		
     	case SEARCH_ID:
     		toggleSearch();
     		return true;
     		
     	case PURGE_ID:
     		purgeDialog();
     		return true;
     		
     	case SETTINGS_ID:
     		showSettings();
     		return true;
     	}
     	
     	return super.onOptionsItemSelected(item);
     }
     
 	private void showSettings()
 	{
 		Intent i = new Intent(this, SettingsActivity.class);
 		startActivityForResult(i, 0);
 	}
 
 	private void toggleSearch()
     {
 		LinearLayout searchLayout = (LinearLayout)findViewById(R.id.SearchLayout);
 		int new_vis = LinearLayout.VISIBLE;
 		int cur_vis = searchLayout.getVisibility();
 		
 		// if it is currently visible, set it to gone
 		if (new_vis == cur_vis)
 		{
 			new_vis = LinearLayout.GONE;
 			searchEdit.setText("");
 			showSearch = false;
 		}
 		else
 		{
 			showSearch = true;
 			searchEdit.requestFocus();
 			
 			// set the adapter each time to get new data for the autocomplete
 			String[] strings = Transaction.getAllStrings();
 			if (strings == null)
 				strings = new String[0];
 			ArrayAdapter<String> searchAdapter = new ArrayAdapter<String>(this,
 	    			android.R.layout.simple_dropdown_item_1line, strings);
 			searchEdit.setAdapter(searchAdapter);
 			searchEdit.setTokenizer(new SpaceTokenizer());
 		}
 		searchLayout.setVisibility(new_vis);
     }
     
     private void sortDialog()
     {
     	new AlertDialog.Builder(this)
     		.setTitle(R.string.sort_column)
     		.setItems(R.array.sort, new DialogInterface.OnClickListener()
     		{
     			public void onClick(DialogInterface dialog, int which)
     			{ 
     				if (which == 0)			// Date
     					Transaction.setComparator(Transaction.COMP_DATE);
     				else if (which == 1)	// Party
     					Transaction.setComparator(Transaction.COMP_PARTY);
     				else if (which == 2)	// Amount
     					Transaction.setComparator(Transaction.COMP_AMT);
     		    	mTa.sort();
     			}
     		})
     		.show();
     }
 
     private void purgeDialog()
     {
     	final Context context = (Context)this;
 		AlertDialog dialog = new AlertDialog.Builder(this)
 			.setTitle(R.string.account_del_box)
 			.setItems(R.array.purge, new DialogInterface.OnClickListener()
 			{
 				public void onClick(DialogInterface dialog, int which)
 				{
 					final int item = which;
 					DatePickerDialog.OnDateSetListener dateSetListener =
 				        new DatePickerDialog.OnDateSetListener()
 						{
 				            public void onDateSet(DatePicker view, int year, int month,  int day)
 				            {
 								Calendar cal = Calendar.getInstance();
								cal.set(Calendar.HOUR, 23);
 								cal.set(Calendar.MINUTE, 59);
 								cal.set(Calendar.SECOND, 59);
 				            	cal.set(Calendar.YEAR, year);
 				            	cal.set(Calendar.MONTH, month);
 				            	cal.set(Calendar.DAY_OF_MONTH, day);
 				            	Date date = cal.getTime();
 				            	
 				            	switch (item)
 				            	{
 				            	case 0:	// purge
 				            		int[] purged = mAcct.purgeTransactions(date);
 				            		if (purged != null)
 			            				updateList(purged, ACTIVITY_DEL);
 				            		break;
 				            		
 				            	case 1:	// restore
 				            		int[] restored = mAcct.restorePurgedTransactions(date);
 				            		if (restored != null)
 			            				updateList(restored, ACTIVITY_CREATE);
 				            		break;
 				            		
 				            	case 2:	// clear
 				            		mAcct.deletePurgedTransactions(date);
 				            		break;
 				            	}
 				            }
 				        };
 				        
 				    String title = "";
 				    if (item == 0)
 				    	title = "Purge Through";
 				    else if (item == 1)
 				    	title = "Restore Through";
 				    else if (item == 2)
 				    	title = "Clear Through";
 				    
 				    Calendar cal = Calendar.getInstance();
 				    DatePickerDialog pickerDialog = new DatePickerDialog(context, dateSetListener,
 				    		cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
 				    pickerDialog.setTitle(title);
 				    pickerDialog.show();
 				}
 			})
 			.create();
 		dialog.show();
     }
     
 	private void createTransaction()
     {
     	Intent i = new Intent(this, TransactionEdit.class);
     	int request = ACTIVITY_CREATE;
     	i.putExtra(TransactionActivity.KEY_REQ, request);
     	i.putExtra(TransactionActivity.KEY_TYPE, TRANSACTION);
     	i.putExtra(Account.KEY_ID, mAcct.id());
     	startActivityForResult(i, request);
     }
     
     private void createTransfer()
     {
     	Intent i = new Intent(this, TransactionEdit.class);
     	int request = ACTIVITY_CREATE;
     	i.putExtra(TransactionActivity.KEY_REQ, request);
     	i.putExtra(TransactionActivity.KEY_TYPE, TRANSFER);
     	i.putExtra(Account.KEY_ID, mAcct.id());
     	startActivityForResult(i, request);
     }
     
     private void editTransaction(int id)
     {
     	Intent i = new Intent(this, TransactionEdit.class);
     	int request = ACTIVITY_EDIT;
     	i.putExtra(Transaction.KEY_ID, id);
     	i.putExtra(Account.KEY_ID, mAcct.id());
     	i.putExtra(TransactionActivity.KEY_REQ, request);
     	startActivityForResult(i, request);
     }
     
     public void setBalances()
     {
     	Double posted = mAcct.calculatePostedBalance();
     	Double balance = mAcct.calculateActualBalance();
     	Double budget = mAcct.calculateBudgetBalance();
     	
 		// change the numbers to the locale currency format
 		NumberFormat nf = NumberFormat.getCurrencyInstance();
 		String new_currency = Database.getOptionString("override_locale");
 		if (new_currency != null && !new_currency.equals(""))
 			nf.setCurrency(Currency.getInstance(new_currency));
 		
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		boolean color = prefs.getBoolean("color_balance", false);
 		
 		setBalance(postedValue, posted, color, nf);
 		setBalance(balanceValue, balance, color, nf);
 		setBalance(budgetValue, budget, color, nf);
     }
     
     private void setBalance(TextView view, Double bal, boolean color, NumberFormat nf)
     {
     	String str = "Error";
     	if (bal != null)
     		str = nf.format(bal);
     	
     	view.setText(str);
     	
     	if (bal < 0 && color)
     		view.setTextColor(Color.rgb(255, 50, 50));
     	else
     		view.setTextColor(Color.LTGRAY);
     }
     
     private void fillList()
     {
     	addRepeatedTransactions();
     	mTa.add(mAcct.getTransactions());
 		mTa.sort();
 		
 		setBalances();
     }
     
     private void updateList(int trans_id, int request)
     {
     	addRepeatedTransactions();
     	TransactionAdapter ta = mTa;
     	Transaction trans;
     	int pos;
     	
     	switch (request)
     	{
     	case ACTIVITY_EDIT:
     		pos = ta.findItemById(trans_id);
     		ta.remove(ta.getItem(pos));
     		// don't break, the transaction needs to be added back to the list
 
     	case ACTIVITY_CREATE:
     		trans = Transaction.getTransactionById(trans_id);
     		ta.add(trans);
     		ta.sort();
     		break;
     		
     	case ACTIVITY_DEL:
     		pos = ta.findItemById(trans_id);
     		ta.remove(ta.getItem(pos));
     		break;
     	}
 
 		setBalances();
     }
     
     private void updateList(int[] ids, int request)
     {
     	addRepeatedTransactions();
     	TransactionAdapter ta = mTa;
     	
     	switch (request)
     	{
     	case ACTIVITY_CREATE:
     		ta.add(ids);
     		ta.sort();
     		break;
     		
     	case ACTIVITY_DEL:
     		ta.remove(ids);
     		break;
     	}
 
 		setBalances();
     }
     
     private void addRepeatedTransactions()
     {
    		int[] ids = RepeatSchedule.processDueRepetitions(new Date());
    		
    		if (ids != null && ids.length != 0)
    		{
    			if (ids[0] == -1)
    			{
 	   			// if there is a -1 in the id list, something went wrong processing
 	   			// the repetition, and the schedule was deleted
 	   			Toast.makeText(this, R.string.bad_repeat, Toast.LENGTH_LONG).show();
    			}
    	    	mTa.add(ids);
    		}
     }
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		super.onActivityResult(requestCode, resultCode, data);
 		
 		if (resultCode == RESULT_OK && data != null)
 		{
 			try
 			{
 				Bundle extras = data.getExtras();
 				updateList(extras.getInt(Transaction.KEY_ID), extras.getInt(TransactionActivity.KEY_REQ));
 			}
 			catch (Exception e)
 			{
 				Logger.logStackTrace(e, this);
 			}
 		}
 		
 		mTa.notifyDataSetChanged();
 	}
 	
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id)
 	{
 		postListItem(position);
 	}
 
 	private int copyListItem(int id)
 	{
 		try
 		{
 			Transaction tr = Transaction.getTransactionById(id);
 			tr.setId(-1);
 			if (tr.type == Transaction.CHECK)
 				tr.check_num = mAcct.getNextCheckNum();
 			if (tr.isPosted())
 				tr.post(false);
 			id = tr.write(mAcct.id());
 		}
 		catch (Exception e)
 		{
 			id = -1;
 			Logger.logStackTrace(e, this);
 		}
 		
 		return id;
 	}
 	
 	private void postListItem(int position)
 	{
 		// ListView.getChildAt only keeps track of visible children
 		// so we have to subtract the position of the first visible view
 		// in the ListAdapter from the position of the item we want
 		int vis = getListView().getFirstVisiblePosition();
 		View v = getListView().getChildAt(position - vis);
 		if (v != null)
 		{
 			CheckBox posted = (CheckBox)v.findViewById(R.id.PostedCheckBox);
 			if (posted != null)
 				posted.setChecked(!posted.isChecked());
 		}
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item)
 	{
 		AdapterView.AdapterContextMenuInfo info;
 		try
 		{
 			info = (AdapterContextMenuInfo)item.getMenuInfo();
 		}
 		catch (ClassCastException e)
 		{
 			Log.e(TransactionActivity.class.toString(), "Bad ContextMenuInfo", e);
 			return false;
 		}
 		
 		int id = (int)getListAdapter().getItemId(info.position);
 		switch (item.getItemId())
 		{
 		case CONTEXT_EDIT:
 			editTransaction(id);
 			return true;
 			
 		case CONTEXT_COPY:
 			id = copyListItem(id);
 			updateList(id, ACTIVITY_CREATE);
 			return true;
 			
 		case CONTEXT_POST:
 			postListItem(info.position);
 			return true;
 			
 		case CONTEXT_REPEAT:
 			showDialog(id);
 			return true;
 			
 		case CONTEXT_DEL:
 			final Transaction trans = Transaction.getTransactionById(id);
 			final Context c = this;
 			AlertDialog dialog = new AlertDialog.Builder(this)
 				.setTitle(R.string.account_del_box)
 				.setMessage("Are you sure you wish to delete " + trans.party + "?")
 				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
 				{
 					public void onClick(DialogInterface dialog, int which)
 					{
 						try
 						{
 							int id = trans.id();
 							trans.erase();
 							updateList(id, ACTIVITY_DEL);
 						}
 						catch (Exception e)
 						{
 							Logger.logStackTrace(e, c);
 						}
 					}
 				})
 				.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
 				{
 					public void onClick(DialogInterface dialog, int which) { }
 				})
 				.create();
 			dialog.show();
 			
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
 	{
 		AdapterView.AdapterContextMenuInfo info;
 		try
 		{
 			info = (AdapterContextMenuInfo)menuInfo;
 		}
 		catch (ClassCastException e)
 		{
 			Log.e(TransactionActivity.class.toString(), "Bad ContextMenuInfo", e);
 			return;
 		}
 		
 		if (info == null)
 		{
 			Log.e(TransactionActivity.class.toString(), "info == null");
 			return;
 		}
 
 		Transaction trans = (Transaction)getListAdapter().getItem(info.position);
 		if (trans == null)
 			return;
 		
 		menu.setHeaderTitle(trans.party);
 		
 		menu.add(0, CONTEXT_EDIT, 0, R.string.edit);
 		menu.add(0, CONTEXT_COPY, 0, R.string.copy);
 		menu.add(0, CONTEXT_POST, 0, R.string.post);
 		menu.add(0, CONTEXT_REPEAT, 0, R.string.repeat);
 		menu.add(0, CONTEXT_DEL, 0, R.string.del);
 	}
 	
 	@Override
 	protected Dialog onCreateDialog(int id)
 	{
 		return new RepeatDialog(this, id);
 	}
 	
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog)
 	{
 		((RepeatDialog)dialog).setTransId(id);
 	}
 
 	public static void setAccountNull()
 	{
 		mAcct = null;
 	}
 	
 	private class RepeatDialog extends Dialog
 	{
 		private ArrayAdapter<String> mAdapter;
 		private int mTransId;
 		
 		public RepeatDialog(Context context, int id)
 		{
 			super(context);
 			setContentView(R.layout.repeat_list);
 			ListView lv = (ListView)findViewById(R.id.repeat_list);
 			
 			this.setTitle("Repeat");
 			
 			final ArrayList<String> repeat =
 				new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.repeat_dialog)));
 			mAdapter = new ArrayAdapter<String>(getContext(),
 					android.R.layout.simple_expandable_list_item_1, repeat);
 			lv.setAdapter(mAdapter);
 			
 			lv.setOnItemClickListener(new OnItemClickListener()
 			{
 				public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
 				{
 					Transaction trans = Transaction.getTransactionById(copyListItem(mTransId));
 					if (trans == null)
 					{
 						dismiss();
 						return;
 					}
 					
 					trans.post(false);
 					trans.budget = true;
 					Calendar cal = Calendar.getInstance();
 					cal.setTime(trans.date);
 					int cal_field = -1, cal_value = 1;
 					
 					switch (pos)
 					{
 						case 0:
 							cal_field = Calendar.DATE;
 							break;
 
 						case 2:
 							cal_value = 2;
 							
 						case 1:
 							cal_field = Calendar.WEEK_OF_YEAR;
 							break;
 							
 						case 3:
 							cal_field = Calendar.MONTH;
 							break;
 							
 						case 4:
 							cal_field = Calendar.YEAR;
 							break;
 					}
 					
 					if (cal_field != -1)
 					{
 						cal.add(cal_field, cal_value);
 						trans.date = cal.getTime();
 						mTransId = trans.write(mAcct.id());
 						updateList(mTransId, ACTIVITY_CREATE);
 					}
 					dismiss();
 				}
 			});
 			
 			mTransId = id;
 		}
 		
 		public void setTransId(int t)
 		{
 			mTransId = t;
 		}
 	}
 	
 	public static class SpaceTokenizer implements Tokenizer
 	{
 		public int findTokenEnd(CharSequence text, int cursor)
 		{
 			int i = cursor;
 			int len = text.length();
 			
 			while (i < len)
 				if (text.charAt(i) == ' ')
 					return i;
 				else
 					++i;
 			
 			return len;
 		}
 
 		public int findTokenStart(CharSequence text, int cursor)
 		{
 			int i = cursor;
 			
 			while (i > 0 && text.charAt(i - 1) != ' ')
 				--i;
 			while (i < cursor && text.charAt(i) == ' ')
 				++i;
 			
 			return i;
 		}
 
 		public CharSequence terminateToken(CharSequence text)
 		{
 			int i = text.length();
 			
 			while (i > 0 && text.charAt(i -1) == ' ')
 				--i;
 			
 			if (i > 0 && text.charAt(i - 1) == ' ')
 				return text;
 			else
 			{
 				if (text instanceof Spanned)
 				{
 					SpannableString sp = new SpannableString(text + " ");
 					TextUtils.copySpansFrom((Spanned)text, 0, text.length(), Object.class, sp, 0);
 					return sp;
 				}
 				else
 					return text + " ";
 			}
 		}
 	}
 }
