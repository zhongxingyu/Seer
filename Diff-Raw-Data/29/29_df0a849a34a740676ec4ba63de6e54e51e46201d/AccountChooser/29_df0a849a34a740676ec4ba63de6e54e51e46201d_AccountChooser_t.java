 package net.gumbercules.loot.account;
 
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Currency;
 
 import net.gumbercules.loot.ChangeLogActivity;
 import net.gumbercules.loot.ConfirmationDialog;
 import net.gumbercules.loot.PinActivity;
 import net.gumbercules.loot.R;
 import net.gumbercules.loot.TipsDialog;
 import net.gumbercules.loot.backend.CopyThread;
 import net.gumbercules.loot.backend.Database;
 import net.gumbercules.loot.backend.MemoryStatus;
 import net.gumbercules.loot.preferences.SettingsActivity;
 import net.gumbercules.loot.premium.PremiumCaller;
 import net.gumbercules.loot.repeat.RepeatManagerActivity;
 import net.gumbercules.loot.transaction.TransactionActivity;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 public class AccountChooser extends ListActivity
 {
 	public static final int ACTIVITY_CREATE	= 0;
 	public static final int ACTIVITY_EDIT	= 1;
 	
 	public static final int NEW_ACCT_ID		= Menu.FIRST;
 	public static final int RESTORE_ID		= Menu.FIRST + 1;
 	public static final int CLEAR_ID		= Menu.FIRST + 2;
 	public static final int SETTINGS_ID		= Menu.FIRST + 3;
 	public static final int BACKUP_ID		= Menu.FIRST + 4;
 	public static final int BU_RESTORE_ID	= Menu.FIRST + 5;
 	public static final int EXPORT_ID		= Menu.FIRST + 6;
 	public static final int CHART_ID		= Menu.FIRST + 7;
 	public static final int CHANGELOG_ID	= Menu.FIRST + 8;
 	public static final int RMANAGER_ID		= Menu.FIRST + 9;
 	
 	public static final int CONTEXT_EDIT	= Menu.FIRST;
 	public static final int CONTEXT_DEL		= Menu.FIRST + 1;
 	public static final int CONTEXT_EXPORT	= Menu.FIRST + 2;
 	public static final int CONTEXT_CHART	= Menu.FIRST + 3;
 	public static final int CONTEXT_IMPORT	= Menu.FIRST + 4;
 	public static final int CONTEXT_PRIMARY	= Menu.FIRST + 5;
 	
 	@SuppressWarnings("unused")
 	private static final String TAG			= "net.gumbercules.loot.AccountChooser"; 
 
 	private ArrayList<Account> accountList;
 	private TextView mTotalBalance;
 	private LinearLayout mTbLayout;
 	
 	private CharSequence[] acct_names;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		
 		// required to prevent last-used from jumping back to this spot
 		@SuppressWarnings("unused")
 		Bundle bun = getIntent().getExtras();
 		
 		getListView().setOnCreateContextMenuListener(this);
 		
 		TransactionActivity.setAccountNull();
 		accountList = new ArrayList<Account>();
 		
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		
 		if (prefs.getBoolean("tips", true))
 		{
 			new TipsDialog(this).show();
 		}
 		
 		// if we're not overriding locale, check to see if the detected one is valid
 		String locale = Database.getOptionString("override_locale");
 		if (locale == null || locale.equals(""))
 		{
 			checkLocale();
 		}
 	}
 	
 	private void checkLocale()
 	{
 		Currency cur = NumberFormat.getInstance().getCurrency();
 
 		if (cur.getCurrencyCode().equalsIgnoreCase("xxx"))
 		{
 			new AlertDialog.Builder(this)
 				.setMessage(R.string.no_locale)
 				.show();
 		}
 	}
 	
 	private void setupActionBar()
 	{
     	final Context context = this;
     	final PremiumCaller pc = new PremiumCaller(this);
     	
     	ImageButton button = (ImageButton)findViewById(R.id.new_account_button);
     	button.setOnClickListener(new ImageButton.OnClickListener()
     	{
 			@Override
 			public void onClick(View v)
 			{
 				createAccount();
 			}
 		});
     	
     	button = (ImageButton)findViewById(R.id.backup_button);
     	button.setOnClickListener(new ImageButton.OnClickListener()
     	{
 			@Override
 			public void onClick(View v)
 			{
 				initiateBackup(context);
 			}
 		});
     	
     	button = (ImageButton)findViewById(R.id.export_button);
     	button.setOnClickListener(new ImageButton.OnClickListener()
     	{
 			@Override
 			public void onClick(View v)
 			{
 				pc.showActivity(PremiumCaller.EXPORT);
 			}
 		});
 
     	button = (ImageButton)findViewById(R.id.chart_button);
     	button.setOnClickListener(new ImageButton.OnClickListener()
     	{
 			@Override
 			public void onClick(View v)
 			{
 				pc.showActivity(PremiumCaller.CHART);
 			}
 		});
 	}
 	
 	private void initiateBackup(Context context)
 	{
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		CopyThread ct = null;
 		ProgressDialog pd = new ProgressDialog(context);
 		pd.setCancelable(true);
 		pd.setMax(100);
 		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 		pd.setMessage(getResources().getText(R.string.backing_up));
 		pd.show();
 		
 		// if the api level is at froyo or later and the preference is selected for online backups
 		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO &&
 				prefs.getBoolean("online_backup", false))
 		{
     		ct = new CopyThread(CopyThread.BACKUP, CopyThread.ONLINE, pd, context);
     		ct.start();
			//new BackupManager(this).dataChanged();
 		}
 		else if (MemoryStatus.checkMemoryStatus(context, true))
 		{
     		ct = new CopyThread(CopyThread.BACKUP, CopyThread.OFFLINE, pd, context);
     		ct.start();
 		}
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id)
 	{
 		Account acct = Account.getAccountById((int)id);
 		if (acct == null)
 			return;
 		
 		Intent in = new Intent(this, TransactionActivity.class);
 		in.putExtra(Account.KEY_ID, acct.id());
 		startActivityForResult(in, 0);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		menu.add(0, RESTORE_ID, 0, R.string.restore_account)
 			.setShortcut('1', 'h')
 			.setIcon(android.R.drawable.ic_menu_revert);
 		menu.add(0, CLEAR_ID, 0, R.string.clear_account)
 			.setShortcut('2', 'c')
 			.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
 		menu.add(0, BU_RESTORE_ID, 0, R.string.restore_db)
 			.setShortcut('3', 'r')
 			.setIcon(android.R.drawable.ic_menu_set_as);
 		menu.add(0, RMANAGER_ID, 0, R.string.repeat_manager)
 			.setShortcut('4', 'm')
 			.setIcon(android.R.drawable.ic_menu_recent_history);
 		menu.add(0, CHANGELOG_ID, 0, R.string.changelog)
 			.setShortcut('5', 'l')
 			.setIcon(android.R.drawable.ic_menu_agenda);
 		menu.add(0, SETTINGS_ID, 0, R.string.settings)
 			.setShortcut('6', 's')
 			.setIcon(android.R.drawable.ic_menu_preferences);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
     	{
     	case RESTORE_ID:
     		restoreAccount();
     		return true;
     		
     	case CLEAR_ID:
     		clearAccount();
     		return true;
     		
     	case SETTINGS_ID:
     		showSettings();
     		return true;
     		
     	case BU_RESTORE_ID:
     		if (MemoryStatus.checkMemoryStatus(this, false))
     		{
     			final ProgressDialog pd = new ProgressDialog(this);
     			pd.setCancelable(true);
     			pd.setMax(100);
     			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 
     			// ensure the user wishes to really restore the database
     			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
     			
 	    		final CopyThread ct = new CopyThread(CopyThread.RESTORE,
 	    				CopyThread.OFFLINE, pd, getBaseContext());
 	    		pd.setMessage(getResources().getText(R.string.restoring));
 
 	    		if (prefs.getBoolean("show_confirmation_on_restore", true))
     			{
 	    			ConfirmationDialog cd = new ConfirmationDialog(this);
 	    			cd.setButton(ConfirmationDialog.BUTTON_POSITIVE, getResources().getText(R.string.yes),
 	    					new DialogInterface.OnClickListener()
 						{
 							@Override
 							public void onClick(DialogInterface dialog, int which)
 							{
 					    		pd.show();
 					    		ct.start();
 							}
 						});
 	    			cd.setButton(ConfirmationDialog.BUTTON_NEGATIVE, getResources().getText(R.string.no),
 	    					new DialogInterface.OnClickListener()
 						{
 							@Override
 							public void onClick(DialogInterface dialog, int which) { }
 						});
 	    			cd.show();
     			}
     			else
     			{
 		    		pd.show();
 		    		ct.start();
     			}
     		}
     		return true;
     		
     	case CHANGELOG_ID:
     		showChangeLog();
     		return true;
     		
     	case RMANAGER_ID:
     		showRepeatManager();
     		return true;
     	}
     	
 		return false;
 	}
 
 	private void showRepeatManager()
 	{
 		Intent i = new Intent(this, RepeatManagerActivity.class);
 		startActivityForResult(i, 0);
 	}
 	
 	private void showChangeLog()
 	{
 		Intent i = new Intent(this, ChangeLogActivity.class);
 		startActivityForResult(i, 0);
 	}
 
 	private Account[] findDeletedAccounts()
 	{
 		int[] ids = Account.getDeletedAccountIds();
 		if (ids == null)
 		{
 			new AlertDialog.Builder(this)
 				.setMessage(R.string.no_deleted_accounts)
 				.show();
 			return null;
 		}
 		
 		int len = ids.length;
 		Account[] accounts = new Account[len];
 		for (int i = len - 1; i >= 0; --i)
 		{
 			accounts[i] = new Account();
 			accounts[i].loadById(ids[i], true);
 		}
 		
 		return accounts;
 	}
 	
 	private void restoreAccount()
 	{
 		final Account[] finalAccts = findDeletedAccounts();
 		if (finalAccts == null)
 			return;
 		int len = finalAccts.length;
 		acct_names = new CharSequence[len];
 		String[] split;
 		for (int i = 0; i < len; ++i)
 		{
 			split = finalAccts[i].name.split(" - Deleted ");
 			if (split != null)
 				acct_names[i] = split[0];
 		}
 		
 		new AlertDialog.Builder(this)
 			.setTitle(R.string.restore)
 			.setItems(acct_names, new DialogInterface.OnClickListener()
 			{
 				public void onClick(DialogInterface dialog, int which)
 				{
 					Account.restoreDeletedAccount(finalAccts[which].id());
 					fillList();
 				}
 			})
 			.show();
 	}
 
 	private void clearAccount()
 	{
 		final Account[] finalAccts = findDeletedAccounts();
 		if (finalAccts == null)
 			return;
 		int len = finalAccts.length;
 		acct_names = new CharSequence[len];
 		String[] split;
 		for (int i = 0; i < len; ++i)
 		{
 			split = finalAccts[i].name.split(" - Deleted ");
 			if (split != null)
 				acct_names[i] = split[0];
 		}
 		
 		final Context con = this;
 		new AlertDialog.Builder(this)
 			.setTitle(R.string.clear)
 			.setItems(acct_names, new DialogInterface.OnClickListener()
 			{
 				public void onClick(DialogInterface dialog, int which)
 				{
 					final int pos = which;
 					AlertDialog yn_dialog = new AlertDialog.Builder(con)
 						.setMessage("Are you sure you wish to remove this account completely?")
 						.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
 						{
 							public void onClick(DialogInterface dialog, int which)
 							{
 								Account.clearDeletedAccount(finalAccts[pos].id());
 							}
 						})
 						.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
 						{
 							public void onClick(DialogInterface dialog, int which) { }
 						})
 						.create();
 					yn_dialog.show();
 				}
 			})
 			.show();
 	}
 
 	private void createAccount()
 	{
 		Intent i = new Intent(this, AccountEdit.class);
 		startActivityForResult(i, ACTIVITY_CREATE);
 	}
 	
 	private void editAccount(int id)
 	{
 		Intent i = new Intent(this, AccountEdit.class);
 		i.putExtra(Account.KEY_ID, id);
 		startActivityForResult(i, ACTIVITY_EDIT);
 	}
 	
 	private void showSettings()
 	{
 		Intent i = new Intent(this, SettingsActivity.class);
 		startActivityForResult(i, 0);
 	}
 
 	private void fillList()
 	{
 		accountList.clear();
 		Account[] accounts = Account.getActiveAccounts();
 		
 		if (accounts != null)
 		{
 			accountList.addAll(Arrays.asList(accounts));
 		}
 		
 		AccountAdapter aa = (AccountAdapter)getListAdapter();
 		if (aa == null)
 		{
 			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 			
 			int row_res = R.layout.account_row;
 			if (prefs.getBoolean("large_fonts", false))
 			{
 				row_res = R.layout.account_row_large;
 			}
 			aa = new AccountAdapter(this, row_res, accountList);
 			setListAdapter(aa);
 			setContent();
 		}
 		aa.notifyDataSetChanged();
 		setTotalBalance();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		super.onActivityResult(requestCode, resultCode, data);
 		
 		if (resultCode == RESULT_OK)
 		{
 			TransactionActivity.setAccountNull();
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
 			Log.e(AccountChooser.class.toString(), "Bad ContextMenuInfo", e);
 			return false;
 		}
 		
 		int id = (int)getListAdapter().getItemId(info.position);
 		final Account acct = Account.getAccountById(id);
 		
 		switch (item.getItemId())
 		{
 		case CONTEXT_EDIT:
 			editAccount(id);
 			return true;
 			
 		case CONTEXT_DEL:
 			AlertDialog dialog = new AlertDialog.Builder(this)
 				.setTitle(R.string.account_del_box)
 				.setMessage("Are you sure you wish to delete " + acct.name + "?")
 				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
 				{
 					public void onClick(DialogInterface dialog, int which)
 					{
 						acct.erase();
 						fillList();
 					}
 				})
 				.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
 				{
 					public void onClick(DialogInterface dialog, int which) { }
 				})
 				.create();
 			dialog.show();
 			
 			return true;
 			
 		case CONTEXT_PRIMARY:
 			boolean set = !acct.isPrimary();
 			acct.setPrimary(set);
 			AccountAdapter aa = (AccountAdapter)getListAdapter();
 			aa.setPrimary(info.position, set);
 			aa.notifyDataSetChanged();
 			return true;
 			
 		case CONTEXT_IMPORT:
 			PremiumCaller imp = new PremiumCaller(this);
 			imp.showActivity(PremiumCaller.IMPORT, id);
 			return true;
 			
 		case CONTEXT_EXPORT:
 			PremiumCaller export = new PremiumCaller(this);
 			export.showActivity(PremiumCaller.EXPORT, id);
 			return true;
 			
 		case CONTEXT_CHART:
 			PremiumCaller graph = new PremiumCaller(this);
 			graph.showActivity(PremiumCaller.CHART, id);
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
 			Log.e(AccountChooser.class.toString(), "Bad ContextMenuInfo", e);
 			return;
 		}
 		
 		Account acct = (Account)getListAdapter().getItem(info.position);
 		if (acct == null)
 			return;
 		
 		menu.setHeaderTitle(acct.name);
 		
 		menu.add(0, CONTEXT_EDIT, 0, R.string.edit);
 		menu.add(0, CONTEXT_IMPORT, 0, R.string.import_);
 		menu.add(0, CONTEXT_EXPORT, 0, R.string.export);
 		menu.add(0, CONTEXT_CHART, 0, R.string.chart);
 		menu.add(0, CONTEXT_PRIMARY, 0, R.string.primary);
 		menu.add(0, CONTEXT_DEL, 0, R.string.del);
 	}
 
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 		
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		if (!prefs.getBoolean(PinActivity.SHOW_ACCOUNTS, true))
 		{
 			finish();
 		}
 		
 		int row_res = R.layout.account_row;
 		if (prefs.getBoolean("large_fonts", false))
 		{
 			row_res = R.layout.account_row_large;
 		}
 
 		setContent();
 		
 		registerForContextMenu(getListView());
 		AccountAdapter accounts = new AccountAdapter(this, row_res, accountList);
 		setListAdapter(accounts);
 		fillList();
 	}
 
 	private void setContent()
 	{
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		int content_res = R.layout.accounts;
 		if (prefs.getBoolean("large_fonts", false))
 		{
 			content_res = R.layout.accounts_large;
 		}
 		
 		setContentView(content_res);
 		setupActionBar();
 		mTbLayout = (LinearLayout)findViewById(R.id.total_balance_layout);
 		mTotalBalance = (TextView)findViewById(R.id.total_balance);
 	}
 	
 	private void setTotalBalance()
 	{	
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		if (prefs.getBoolean("total_balance", true))
 		{
 			NumberFormat nf = NumberFormat.getCurrencyInstance();
 			String new_currency = Database.getOptionString("override_locale");
 			if (new_currency != null && !new_currency.equals(""))
 			{
 				nf.setCurrency(Currency.getInstance(new_currency));
 			}
 			Double bal = Account.getTotalBalance();
 			String text;
 			if (bal != null)
 			{
 				text = nf.format(bal);
 			}
 			else
 			{
 				text = "Error Calculating Balance";
 			}
 			mTotalBalance.setText(text);
 			
 			if (bal < 0.0)
 			{
 				if (prefs.getBoolean("color_balance", false))
 					mTotalBalance.setTextColor(Color.rgb(255, 50, 50));
 			}
 			else
 			{
 				mTotalBalance.setTextColor(Color.LTGRAY);
 			}
 
 			mTbLayout.setVisibility(LinearLayout.VISIBLE);
 		}
 		else
 		{
 			mTbLayout.setVisibility(LinearLayout.GONE);
 		}
 	}
 }
