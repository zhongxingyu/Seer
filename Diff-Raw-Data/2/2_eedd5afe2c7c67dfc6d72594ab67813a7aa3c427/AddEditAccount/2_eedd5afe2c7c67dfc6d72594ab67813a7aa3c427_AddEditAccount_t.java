 /*******************************************************************************
  * Copyright 2012 Sorin Otescu <sorin.otescu@gmail.com>
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package com.udesign.cashlens;
 
 import com.quietlycoding.android.picker.NumberPickerPreference;
 import com.udesign.cashlens.CashLensStorage.Account;
 import com.udesign.cashlens.CashLensStorage.Currency;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.preference.EditTextPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceActivity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Toast;
 
 public final class AddEditAccount extends PreferenceActivity
 {
 	private CashLensStorage mStorage;
 	private Account mAccount;
 	private ArrayListWithNotify<Currency> mCurrencies;
 	private EditTextPreference mName;
 	private ListPreference mCurrency;
 	private NumberPickerPreference mMonthStart;
 	private Button mSaveButton;
 	private boolean mShouldSave;
 	
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		
 		addPreferencesFromResource(R.xml.account_settings);
 		setContentView(R.layout.add_edit_account);	// for Save button
 		
 		try
 		{
 			mStorage = CashLensStorage.instance(this);
 		} 
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			finish();
 		}
 		
 		mSaveButton = (Button)findViewById(R.id.btnSave);
 		mName = (EditTextPreference)findPreference("name");
 		mCurrency = (ListPreference)findPreference("currency");
 		mMonthStart = (NumberPickerPreference)findPreference("monthStart");
 
 		mSaveButton.setOnClickListener(new View.OnClickListener()
 		{
 			public void onClick(View v)
 			{
 				if (saveAccountIfAllowed())
 					finish();
 			}
 		});
 		
 		int accountId = getIntent().getIntExtra("account_id", 0);
 		if (accountId == 0)
 		{
 			mAccount = new Account(mStorage);
 
 			// Show the save button; if the account is already created,
 			// we save it automatically as it changes (no need for a save button)
 			mSaveButton.setVisibility(View.VISIBLE);
 		}
 		else
 			mAccount = mStorage.getAccount(accountId);
 		
		mCurrencies = mStorage.getCurrencies();
 		
 		OnPreferenceChangeListener onPreferenceChangeListener = new OnPreferenceChangeListener()
 		{
 			public boolean onPreferenceChange(Preference preference, Object newValue)
 			{
 				if (preference == mName)
 				{
 					String name = (String)newValue;
 					
 					if (name.length() == 0)
 						return false;	// invalid
 					
 					if (mAccount.name == null || !mAccount.name.equals(name))
 					{
 						mAccount.name = name;
 						
 						mShouldSave = true;
 						updateSummariesAndSaveEnabled();
 					}
 				}
 				else if (preference == mCurrency)
 				{
 					int currencyId = Integer.parseInt((String)newValue);
 					
 					if (mAccount.currencyId != currencyId)
 					{
 						AppSettings.instance(AddEditAccount.this).setLastUsedCurrency(currencyId);
 						
 						mAccount.currencyId = currencyId;
 						
 						mShouldSave = true;
 						updateSummariesAndSaveEnabled();
 					}
 				}
 				else if (preference == mMonthStart)
 				{
 					int monthStart = ((Integer)newValue).intValue();
 					
 					if (mAccount.monthStartDay != monthStart)
 					{
 						mAccount.monthStartDay = monthStart;
 						
 						mShouldSave = true;
 						updateSummariesAndSaveEnabled();
 					}
 				}
 				
 				return true;
 			}
 		};
 		
 		mName.setOnPreferenceChangeListener(onPreferenceChangeListener);
 		mCurrency.setOnPreferenceChangeListener(onPreferenceChangeListener);
 		mMonthStart.setOnPreferenceChangeListener(onPreferenceChangeListener);
 		
 		if (mAccount.name != null)
 			mName.setText(mAccount.name);
 		mMonthStart.setValue(mAccount.monthStartDay);
 		
 		populateCurrency();
 		updateSummariesAndSaveEnabled();
 	}
 	
 	private void populateCurrency()
 	{
 		String[] currencyNames = new String[mCurrencies.size()];
 		String[] currencyIds = new String[mCurrencies.size()];
 		
 	    // automatically select last used currency
 		int currencyId;
 		
 		if (mAccount.currencyId != 0)
 			currencyId = mAccount.currencyId;
 		else
 			currencyId = AppSettings.instance(this).getLastUsedCurrency();
 		
 	    int index = 0;
 		
 		int i = 0;
 		for (Currency currency : mCurrencies)
 		{
 			currencyNames[i] = currency.name;
 			currencyIds[i] = Integer.toString(currency.id);
 			
 			if (currencyId == currency.id)
 				index = i;
 			
 			++i;
 		}
 		
 		mCurrency.setEntries(currencyNames);
 		mCurrency.setEntryValues(currencyIds);
 		mCurrency.setValueIndex(index);
 	}
 	
 	private void updateSummariesAndSaveEnabled()
 	{
 		if (mAccount.name != null)
 			mName.setSummary(mAccount.name);
 		else
 			mName.setSummary(R.string.not_set);
 		
 		if (mAccount.currencyId > 0)
 		{
 			Currency currency = mStorage.getCurrency(mAccount.currencyId);
 			mCurrency.setSummary(currency.fullName());
 		}
 		else
 			mCurrency.setSummary(R.string.not_set);
 		
 		mMonthStart.setSummary(Integer.toString(mAccount.monthStartDay));
 		
 		mSaveButton.setEnabled(mShouldSave && isAccountValid());
 	}
 	
 	private boolean isAccountValid()
 	{
 		return 
 				mAccount.name != null &&
 				mAccount.name.length() > 0 &&
 				mAccount.currencyId != 0;
 	}
 	
 	// Returns true if the account was saved
 	private boolean saveAccountIfAllowed()
 	{
 		if (!mShouldSave || !isAccountValid())
 			return false;
 		
 		try
 		{
 			if (mAccount.id == 0)
 				mStorage.addAccount(mAccount);
 			else
 				mStorage.updateAccount(mAccount);
 			
 			mShouldSave = false;	// don't try to save twice
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 			Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
 			return false;
 		}
 		
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.add_edit_account_menu, menu);
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 	    // Handle item selection
 	    switch (item.getItemId()) 
 	    {
 	    case R.id.delAccount:
 	    	delAccountWithConfirm();
 	        return true;
 	        
 	    default:
 	        return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	protected void delAccountWithConfirm()
 	{
 		if (mAccount.id == 0)
 			return;
 		
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		
 		alert.setTitle(getString(R.string.delete_account) + ": " + mAccount.name);
 		alert.setMessage(getString(R.string.delete_account_are_you_sure));
 
 		alert.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				onDelAccountOK();
 			}
 		});
 
 		alert.setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				// do nothing
 			}
 		});
 
 		alert.show();
 	}
 	
 	private void onDelAccountOK()
 	{
 		if (mAccount.id == 0)
 			return;
 		
 		try
 		{
 			mStorage.deleteAccount(mAccount);
 			mAccount = null;
 			Toast.makeText(this, R.string.account_deleted, Toast.LENGTH_SHORT).show();
 			finish();
 		} 
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onBackPressed()
 	 */
 	@Override
 	public void onBackPressed()
 	{
 		// New accounts must be saved via the Save button, not when the activity 
 		// is closed (which means Discard in this case)
 		if (mAccount.id != 0)
 			saveAccountIfAllowed();
 
 		super.onBackPressed();
 	}
 }
