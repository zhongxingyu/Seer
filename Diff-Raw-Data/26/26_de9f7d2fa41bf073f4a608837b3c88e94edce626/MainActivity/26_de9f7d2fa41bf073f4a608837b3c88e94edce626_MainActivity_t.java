 package net.skweez.sipgate.activity;
 
 import static net.skweez.sipgate.service.QueryService.STATUS_ERROR;
 import static net.skweez.sipgate.service.QueryService.STATUS_FINISHED;
 import static net.skweez.sipgate.service.QueryService.STATUS_NOT_AUTHENTICATED;
 import static net.skweez.sipgate.service.QueryService.STATUS_RUNNING;
 import static net.skweez.sipgate.service.QueryService.STATUS_UPDATED_ACCOUNT;
 import static net.skweez.sipgate.service.QueryService.STATUS_UPDATED_CALLS;
 import net.skweez.sipgate.R;
 import net.skweez.sipgate.api.Call;
 import net.skweez.sipgate.db.DataSource;
 import net.skweez.sipgate.model.AccountInfo;
 import net.skweez.sipgate.service.QueryResultReceiver;
 import net.skweez.sipgate.service.QueryService;
 import android.app.AlertDialog;
 import android.app.TabActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.provider.ContactsContract;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TabHost;
 import android.widget.TabHost.TabContentFactory;
 import android.widget.Toast;
 
 /**
  * @author Michael Kanis
  */
 public class MainActivity extends TabActivity implements
 		QueryResultReceiver.Receiver {
 
 	private final AccountInfo accountInfo;
 
 	private DataSource dataSource;
 
 	private CallListAdapter callListAdapter;
 
 	private AccountInfoAdapter accountInfoAdapter;
 
 	private QueryResultReceiver mReceiver;
 
 	public MainActivity() {
 		accountInfo = new AccountInfo();
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		accountInfoAdapter = new AccountInfoAdapter(this, accountInfo);
 		callListAdapter = new CallListAdapter(getApplicationContext(),
 				null);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		mReceiver = new QueryResultReceiver(new Handler());
 		mReceiver.setReceiver(this);
 		initializeUi();
 		reloadData();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		dataSource = new DataSource(getApplicationContext());
 		dataSource.open(true);
 		callListAdapter.changeCursor(dataSource.getAllCallsCursor());
 		accountInfoAdapter.setAccountInfo(dataSource.getAccountInfo());
 	}
 
 	@Override
 	protected void onPause() {
 		dataSource.close();
 		dataSource = null;
 		super.onPause();
 	}
 
 	@Override
 	protected void onStop() {
 		mReceiver.setReceiver(null);
 		super.onStop();
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		int currentTab = getTabHost().getCurrentTab();
 		super.onConfigurationChanged(newConfig);
 		initializeUi();
 		getTabHost().setCurrentTab(currentTab);
 	}
 
 	private void initializeUi() {
 		setContentView(R.layout.main);
 		initializeTabs();
 	}
 
 	private void initializeTabs() {
 		TabHost tabHost = getTabHost();
 		TabHost.TabSpec tabSpec;
 
 		Resources resources = getResources();
 
 		MyTabContentFactory factory = new MyTabContentFactory();
 
 		tabSpec = tabHost.newTabSpec(getString(R.string.tabs_account_tag));
 		tabSpec.setIndicator(getString(R.string.tabs_account_title),
 				resources.getDrawable(R.drawable.ic_tab_account));
 		tabSpec.setContent(factory);
 		tabHost.addTab(tabSpec);
 
 		tabSpec = tabHost.newTabSpec(getString(R.string.tabs_call_tag));
 		tabSpec.setIndicator(getString(R.string.tabs_call_title),
 				resources.getDrawable(R.drawable.ic_tab_recent));
 		tabSpec.setContent(factory);
 		tabHost.addTab(tabSpec);
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.menu, menu);
 		return true;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.refresh:
 			reloadData();
 			return true;
 		case R.id.setup:
 			showSetupActivity();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void reloadData() {
 		if (isNetworkAvailable()) {
 			final Intent intent = new Intent(Intent.ACTION_SYNC, null, this,
 					QueryService.class);
 			intent.putExtra("receiver", mReceiver);
 			intent.putExtra("command", "query");
 			startService(intent);
 		} else {
 			showToast(getString(R.string.networkNotAvailale));
 		}
 	}
 
 	private boolean isNetworkAvailable() {
 		final ConnectivityManager connMgr = (ConnectivityManager) this
 				.getSystemService(Context.CONNECTIVITY_SERVICE);
 
 		final NetworkInfo[] netInfo = connMgr.getAllNetworkInfo();
 
 		if (netInfo != null) {
 			for (NetworkInfo item : netInfo) {
 				if (item.getState() == NetworkInfo.State.CONNECTED) {
 					return true;
 				}
 			}
 		}
 
 		return false;
 	}
 
 	private void showSetupActivity() {
 		startActivity(new Intent(this, SetupActivity.class));
 	}
 
 	private class MyTabContentFactory implements TabContentFactory {
 		public View createTabContent(String tag) {
 			ListView view = new ListView(getApplicationContext());
 			if (tag.equals(getString(R.string.tabs_call_tag))) {
 				view.setOnItemClickListener(contactClickedHandler);
 				view.setAdapter(callListAdapter);
 			} else if (tag.equals(getString(R.string.tabs_account_tag))) {
 				view.setAdapter(accountInfoAdapter);
 			} else {
 				throw new IllegalArgumentException();
 			}
 			return view;
 		}
 	}
 
 	private final OnItemClickListener contactClickedHandler = new OnItemClickListener() {
 		@SuppressWarnings("rawtypes")
 		public void onItemClick(AdapterView p, View v, int position, long id) {
 			contactClicked(position);
 		}
 	};
 
 	private void contactClicked(int position) {
 		Call call = callListAdapter.getCall(position);
 
 		String number = call.getRemoteNumber();
 		final Uri uri = Uri.fromParts("tel", number, null);
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
 		final String[] items = new String[] { "Call", "Show Contact" };
 
 		builder.setTitle(number);
 		builder.setItems(items, new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int item) {
 
 				switch (item) {
 				case 0:
 					dialNumber(uri);
 					break;
 				case 1:
 					openContact(uri);
 					break;
 				}
 			}
 		});
 
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 
 	private void dialNumber(Uri number) {
 		Intent intent = new Intent(Intent.ACTION_DIAL, number);
 		startActivity(intent);
 	}
 
 	private void openContact(Uri number) {
 		Intent intent = new Intent(
 				ContactsContract.Intents.SHOW_OR_CREATE_CONTACT, number);
 		startActivity(intent);
 	}
 
 	public void onReceiveResult(int resultCode, Bundle resultData) {
 		switch (resultCode) {
 		case STATUS_RUNNING:
 			showToast("Updating …");
 			setProgressBarIndeterminateVisibility(true);
 			break;
 		case STATUS_UPDATED_CALLS:
 			callListAdapter.getCursor().requery();
 			showToast("Updated calls.");
 			break;
 		case STATUS_UPDATED_ACCOUNT:
 			accountInfoAdapter.setAccountInfo(dataSource.getAccountInfo());
 			showToast("Updated account.");
 			break;
 		case STATUS_NOT_AUTHENTICATED:
 			showSetupActivity();
 			// Fall through!
 		case STATUS_ERROR:
 			showToast(resultData.getString(Intent.EXTRA_TEXT));
 			// Fall through!
 		case STATUS_FINISHED:
 			setProgressBarIndeterminateVisibility(false);
 			break;
 		}
 	}
 
 	private void showToast(String message) {
 		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
 	}
 }
