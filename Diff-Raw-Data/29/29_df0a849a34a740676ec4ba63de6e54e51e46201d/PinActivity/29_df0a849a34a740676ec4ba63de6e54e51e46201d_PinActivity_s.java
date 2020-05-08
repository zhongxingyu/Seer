 package net.gumbercules.loot;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Calendar;
 import java.util.Date;
 
 import net.gumbercules.loot.account.Account;
 import net.gumbercules.loot.account.AccountChooser;
 import net.gumbercules.loot.backend.Database;
 import net.gumbercules.loot.transaction.TransactionActivity;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.appwidget.AppWidgetManager;
 import android.content.ContentResolver;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class PinActivity extends Activity
 {
 	public static final String SHOW_ACCOUNTS = "show";
 	public static final String CHECKSUM = "checksum";
 	
 	private static final String TAG	= "net.gumbercules.loot.PinActivity";
 	
 	private Button mUnlockButton;
 	private EditText mPinEdit;
 	private TextView mInvalidView;
 	private Bundle mBundle;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		Intent i = getIntent();
 		mBundle = null;
 
 		if (i != null && i.hasCategory("net.gumbercules.category.LAUNCHER"))
 		{
 			mBundle = i.getExtras();
 		}
 		
 		housekeeping();
 
 		final byte[] pin_hash = Database.getOptionBlob("pin");
 		if (pin_hash == null || pin_hash.length == 1)
 		{
 			startAccountChooser(true);
 			finish();
 		}
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.pin);
 		
 		mUnlockButton = (Button)findViewById(R.id.unlockButton);
 		mPinEdit = (EditText)findViewById(R.id.pinEdit);
 		mInvalidView = (TextView)findViewById(R.id.invalidView);
 		
 		Button clearButton = (Button)findViewById(R.id.clearButton);
 		clearButton.setOnClickListener(new Button.OnClickListener()
 		{
 			public void onClick(View v)
 			{
 				mPinEdit.setText("");
 				mInvalidView.setText("");
 			}
 		});
 		
 		int[] buttonIds = {R.id.Button00, R.id.Button01, R.id.Button02, R.id.Button03,
 				R.id.Button04, R.id.Button05, R.id.Button06, R.id.Button07, R.id.Button08, R.id.Button09};
 		Button button;
 		for (int resId : buttonIds)
 		{
 			button = (Button)findViewById(resId);
 			if (button == null)
 				continue;
 			button.setOnClickListener(new Button.OnClickListener()
 			{
 				public void onClick(View v)
 				{
 					mPinEdit.append(v.getTag().toString());
 					mInvalidView.setText("");
 				}
 			});
 		}
 		
 		mPinEdit.setOnKeyListener(new EditText.OnKeyListener()
 		{
 			public boolean onKey(View v, int keyCode, KeyEvent event)
 			{
 				mInvalidView.setText("");
 				return false;
 			}	
 		});
 		
 		mUnlockButton.setOnClickListener(new Button.OnClickListener()
 		{
 			public void onClick(View v)
 			{
 				String pin = mPinEdit.getText().toString();
 				mPinEdit.setText("");
 
 				byte[] bytes;
 				try
 				{
 					MessageDigest md = MessageDigest.getInstance("SHA-1");
 					md.update(pin.getBytes());
 					bytes = md.digest();
 				}
 				catch (NoSuchAlgorithmException e)
 				{
 					return;
 				}
 				
 				if (bytes.length == pin_hash.length)
 				{
 					int i = bytes.length - 1;
 					for (; i >= 0; --i)
 					{
 						if (bytes[i] != pin_hash[i])
 							break;
 					}
 					
 					if (i <= 0)
 					{
 						startAccountChooser(true);
 						return;
 					}
 				}
 
 				mInvalidView.setText(R.string.invalid);
 			}
 		});
 	}
 	
 	private void housekeeping()
 	{
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		ContentResolver cr = getContentResolver();
 
 		// remove preferences if the premium package has been removed
 		if (cr.getType(Uri.parse("content://net.gumbercules.loot.premium.settingsprovider/settings")) == null)
 		{
 			SharedPreferences.Editor editor = prefs.edit();
 			String[] pref_keys = {"color_withdraw", "color_budget_withdraw", "color_deposit",
 					"color_budget_deposit", "color_check", "color_budget_check", 
 					"cal_enabled", "calendar_tag", "auto_backup"};
 			for (String key : pref_keys)
 			{
 				editor.remove(key);
 			}
 			editor.commit();
 		}
 		
 		// automatically purge transactions on load if this option is set
 		int purge_days = (int)Database.getOptionInt("auto_purge_days");
 		if (purge_days != -1)
 		{
 			Calendar cal = Calendar.getInstance();
 			cal.add(Calendar.DAY_OF_YEAR, -purge_days);
 			Date date = cal.getTime();
 			int[] acctIds = Account.getAccountIds();
			Account[] accounts = new Account[acctIds.length];
			for (int i = 0; i < accounts.length; ++i)
 			{
				accounts[i] = Account.getAccountById(acctIds[i]);
			}
			
			for (Account acct : accounts)
			{
				acct.purgeTransactions(date);
 			}
 		}
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)
 	{
 		if (keyCode == KeyEvent.KEYCODE_BACK)
 		{
 			finish();
 			return true;
 		}
 		
 		return false;
 	}
 
 	private void startAccountChooser(boolean show)
 	{
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 
         if (mBundle != null)
         {
 			if (mBundle.getInt("widget_id", AppWidgetManager.INVALID_APPWIDGET_ID) !=
 					AppWidgetManager.INVALID_APPWIDGET_ID)
 			{
 				int account = mBundle.getInt("account_id");
 				
 				if (account > 0)
 				{
 					Intent trans_intent = new Intent(this, TransactionActivity.class);
 					trans_intent.putExtra(Account.KEY_ID, account);
 					startActivity(trans_intent);
 					return;
 				}
 			}
         }
         
         Intent intent = new Intent(this, AccountChooser.class);
        	prefs.edit().putBoolean(SHOW_ACCOUNTS, show).commit();
         startActivity(intent);
 
         if (prefs.getBoolean("primary_default", false))
 		{
 			Account acct = Account.getPrimaryAccount();
 			if (acct == null)
 			{
 				new AlertDialog.Builder(this)
 					.setMessage(R.string.primary_not_set)
 					.show();
 				Log.i(TAG + ".startAccountChooser", "primary account is null");
 			}
 			else
 			{
 				Log.i(TAG + ".startAccountChooser",
 						"skipping account chooser; going directly to transaction activity");
 				Intent in = new Intent(this, TransactionActivity.class);
 				in.putExtra(Account.KEY_ID, acct.id());
 				startActivityForResult(in, 0);
 				return;
 			}
 		}
 		else
 		{
 			Log.i(TAG + ".startAccountChooser", "primary_default is false");
 		}
 	}
 }
