 package com.tuit.ar.activities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
import oauth.signpost.exception.OAuthMessageSignerException;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemLongClickListener;
 
 import com.tuit.ar.R;
 import com.tuit.ar.api.Twitter;
 import com.tuit.ar.api.TwitterAccount;
 import com.tuit.ar.api.TwitterAccountObserver;
 import com.tuit.ar.api.TwitterObserver;
 import com.tuit.ar.databases.Cache;
 
 public class SelectAccount extends ListActivity implements TwitterObserver, OnItemLongClickListener {
 	protected static final int ACCOUNT_MENU_DELETE = 0;
 
 	static private AccountsAdapter accountsAdapter;
 	private ArrayList<TwitterAccount> accounts;
 	private Twitter twitter;
 	private TextView text = null;
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.select_account);
 
 		Cache.getInstance(this);
 		
 		twitter = Twitter.getInstance();
 		accounts = (ArrayList<TwitterAccount>) twitter.getAccounts().clone();
 		setLegend();
 		this.setListAdapter(accountsAdapter = new AccountsAdapter(this));
 		getListView().setOnItemLongClickListener(this);
 
 		twitter.addObserver(this);
 	}
 
 	private void setLegend() {
 		if (accounts.size() == 0) {
 			text = new TextView(this);
 			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 			text.setText(getString(R.string.accountSummary));
 			addContentView(text, params);
 		} else if (text != null) {
 			text.setVisibility(View.INVISIBLE);
 		}
 	}
 
 	public void accountListHasChanged(Twitter twitter) {
 		accounts.clear();
 		accounts.addAll(Twitter.getInstance().getAccounts());
 		accountsAdapter.notifyDataSetChanged();
 	}
 
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add(0, 0, 0, R.string.newAccount);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {  
 		addAccount();
 		return true;
 	}
 
 	protected void onListItemClick (ListView l, View v, int position, long id) {
 		Twitter twitter = Twitter.getInstance();
 		TwitterAccount account = accounts.get(position);
 		twitter.setDefaultAccount(account);
 
 		Intent intent = new Intent(this, com.tuit.ar.activities.timeline.Friends.class);
 		this.startActivity(intent);
 	}
 
 
 	public boolean onItemLongClick(AdapterView<?> l, View v, int position, long id) {
 		new AlertDialog.Builder(this).
 		setTitle(getString(R.string.executeAction)).
 		setItems(R.array.selectAccountOptions, new OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				switch (which) {
 				case ACCOUNT_MENU_DELETE:
 					try {
 						Twitter.getInstance().removeAccount(accounts.get(which));
 					} catch (Exception e) {
 						Toast.makeText(SelectAccount.this, getString(R.string.unableToDeleteAccount), Toast.LENGTH_SHORT).show();
 					}
 					break;
 				}
 			}
 		}).show();
 		return true;
 	}
 
 	private void addAccount() {
 		
 		try {
 			final ProgressDialog dialog = new ProgressDialog(this);
 			dialog.setTitle(R.string.loading);
 			dialog.show();
 			final Handler handler = new Handler();
 			final AddAccountRunnable runnable = new AddAccountRunnable();
 			new Thread() {
 				public void run() {
 					try {
 						runnable.dialog = dialog;
 						runnable.authUrl = twitter.getProvider().retrieveRequestToken(twitter.getConsumer(), Twitter.CALLBACK_URL);
 					} catch (Exception e) {
 					}
 					handler.post(runnable);
 				}
 			}.start();
 		} catch (Exception e) {
 		}  
 	}
 
 	private class AddAccountRunnable implements Runnable {
 		protected ProgressDialog dialog;
 		protected String authUrl;
 
 		public void run() {
 			dialog.hide();
 			if (authUrl != null) {
 				SelectAccount.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)));
 			} else {
 				Toast.makeText(SelectAccount.this, getString(R.string.unableToAddAccount), Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 		Twitter.getInstance().removeObserver(this);
 		getListView().setOnItemLongClickListener(null);
 	}
 
 	protected class AccountsAdapter extends ArrayAdapter<TwitterAccount> 
 	{
 		Activity context;
 		HashMap<View, AccountElement> elements = new HashMap<View, AccountElement>();
 
 		public AccountsAdapter(Activity context)
 		{
 			super(context, R.layout.select_account_element, accounts);
 			this.context = context;
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent)
 		{
 			AccountElement element = getAccountElement(convertView);
 
 			TwitterAccount account = accounts.get(position);
 			if (element.currentAccount == account) return element.getView();
 
 			String username = account.getUsername();
 			element.getUsername().setText(username != null ? username : "Loading...");
 			element.setCurrentAccount(account);
 
 			return element.getView();
 		}
 
 		private AccountElement getAccountElement(View convertView) {
 			if (convertView == null)
 			{
 				convertView = View.inflate(this.context, R.layout.select_account_element, null);
 			}
 
 			if (!elements.containsKey(convertView)) {
 				elements.put(convertView, new AccountElement(convertView));
 			}
 			return elements.get(convertView);
 		}
 	}
 
 	protected class AccountElement implements TwitterAccountObserver {
 		private View view;
 		private TextView username;
 		public TwitterAccount currentAccount; 
 
 		public AccountElement(View view) {
 			super();
 			this.view = view;
 		}
 
 		public void setCurrentAccount(TwitterAccount account) {
 			if (account == currentAccount) return;
 			if (currentAccount != null) currentAccount.removeObserver(this);
 			currentAccount = account;
 			if (currentAccount != null) currentAccount.addObserver(this);
 		}
 
 		public TextView getUsername() {
 			if (username != null) return username;
 			else return username = (TextView)view.findViewById(R.id.username);
 		}
 
 		public View getView() { return view; }
 
 		public void accountHasChanged(TwitterAccount account) {
 			String username = account.getUsername();
 			this.getUsername().setText(username != null ? username : "Loading...");
 		}
 	}
 }
