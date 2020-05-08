 package net.gumbercules.loot;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 public class AccountChooser extends ListActivity
 {
 	public static final int ACTIVITY_CREATE	= 0;
 	public static final int ACTIVITY_EDIT	= 1;
 	
 	public static final int NEW_ACCT_ID		= Menu.FIRST;
 	public static final int SETTINGS_ID		= Menu.FIRST + 1;
 	
 	public static final int CONTEXT_EDIT	= Menu.FIRST + 2;
 	public static final int CONTEXT_DEL		= Menu.FIRST + 3;
 
 	private ArrayList<Account> accountList;
 	private UpdateThread mUpdateThread;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.accounts);
 
 		getListView().setOnCreateContextMenuListener(this);
 		
 		accountList = new ArrayList<Account>();
 		AccountAdapter accounts = new AccountAdapter(this, R.layout.account_row, accountList);
 		setListAdapter(accounts);
 		fillList();
 		
 		mUpdateThread = new UpdateThread(this);
 		mUpdateThread.start();
 		UpdateChecker uc = new UpdateChecker();
 		uc.start();
 
 		// automatically purge transactions on load if this option is set
 		int purge_days = (int)Database.getOptionInt("auto_purge_days");
 		if (purge_days != -1)
 		{
 			Calendar cal = Calendar.getInstance();
 			cal.add(Calendar.DAY_OF_YEAR, -purge_days);
 			Date date = cal.getTime();
 			for (Account acct : accountList)
 			{
 				acct.purgeTransactions(date);
 			}
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
 		boolean result = super.onCreateOptionsMenu(menu);
 		menu.add(0, NEW_ACCT_ID, 0, R.string.new_account)
 			.setIcon(android.R.drawable.ic_menu_add);
 		menu.add(0, SETTINGS_ID, 0, R.string.settings)
 			.setIcon(android.R.drawable.ic_menu_preferences);
 		return result;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
     	switch (item.getItemId())
     	{
     	case NEW_ACCT_ID:
     		createAccount();
     		return true;
     		
     	case SETTINGS_ID:
     		showSettings();
     		return true;
     	}
     	
 		return super.onOptionsItemSelected(item);
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
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		super.onActivityResult(requestCode, resultCode, data);
 		fillList();
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void fillList()
 	{
 		int[] acctIds = Account.getAccountIds();
 		accountList.clear();
 		
 		if (acctIds != null)
 			for ( int id : acctIds )
 				accountList.add(Account.getAccountById(id));
 		((ArrayAdapter<Account>)getListAdapter()).notifyDataSetChanged();
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
 		switch (item.getItemId())
 		{
 		case CONTEXT_EDIT:
 			editAccount(id);
 			return true;
 			
 		case CONTEXT_DEL:
 			final Account acct = Account.getAccountById(id);
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
 		menu.add(0, CONTEXT_DEL, 0, R.string.del);
 	}
 	
 	private class UpdateChecker extends Thread
 	{
 		@Override
 		public void run()
 		{
 			try
 			{
 				URL url = new URL("http", "gumbercules.net", "/loot/version.html");
 				URLConnection conn = url.openConnection();
 				conn.connect();
 				InputStream is = conn.getInputStream();
 				byte[] bytes = new byte[10];
 				int len = is.read(bytes);
 				int current_ver = Integer.valueOf(new String(bytes, 0, len).trim());
 				PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
 
				if (current_ver > pi.versionCode)
 				{
 					Message msg = new Message();
 					msg.what = R.string.update;
 					mUpdateThread.mHandler.sendMessage(msg);
 				}
 			}
 			catch (MalformedURLException e)
 			{
 				e.printStackTrace();
 			}
 			catch (IOException e)
 			{
 				e.printStackTrace();
 			} catch (NameNotFoundException e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private class UpdateThread extends Thread
 	{
 		public Handler mHandler;
 		private Context mContext;
 
 		public UpdateThread(Context con)
 		{
 			mContext = con;
 		}
 		
 		@Override
 		public void run()
 		{
 			Looper.prepare();
 		
 			mHandler = new Handler()
 			{
 				public void handleMessage(Message msg)
 				{
 					Toast t = Toast.makeText(mContext, msg.what, Toast.LENGTH_LONG);
 					t.show();
 				}
 			};
 			
 			Looper.loop();
 		}
 	}
 }
