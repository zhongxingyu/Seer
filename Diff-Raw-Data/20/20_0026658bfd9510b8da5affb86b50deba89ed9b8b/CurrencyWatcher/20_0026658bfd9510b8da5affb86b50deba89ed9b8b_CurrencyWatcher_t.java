 package net.gumbercules.loot.backend;
 
 import java.text.DecimalFormatSymbols;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Currency;
 import java.util.Locale;
 
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.os.Build;
 import android.preference.PreferenceManager;
 import android.text.Editable;
 import android.text.InputType;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.widget.EditText;
 
 public class CurrencyWatcher implements TextWatcher
 {
	private static final String TAG			= "net.gumbercules.loot.backend.CurrencyWatcher";
 	private static final String PHONE_TYPE	= "PHONE";
 	private static final String NUMBER_TYPE	= "NUMBER";
 	
 	private String mOld;
 	private final char mSeparator;
 	private boolean mChanged;
 	private final int mFractionDigits;
 	private Character[] mAccepted;
 	
 	// these phones seem to have issues with replacing periods with commas on the hard keyboard
 	private static final String[] mBadPhones = 
 	{
 		"MB200",
 		"SPH-M900",
 		"sdk"		// not actually bad, but useful for testing purposes
 	};
 	
 	public CurrencyWatcher()
 	{
 		Log.i(TAG + ".CurrencyWatcher()", "Detected locale: " + Locale.getDefault().getDisplayName());
 		
 		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
 		mChanged = false;
 		String new_currency = Database.getOptionString("override_locale");
 		Currency cur = null;
 		if (new_currency != null && !new_currency.equals(""))
 		{
 			cur = Currency.getInstance(new_currency);
 		}
 		else
 		{
 			cur = NumberFormat.getInstance().getCurrency();
 		}
 		dfs.setCurrency(cur);
 		mSeparator = dfs.getMonetaryDecimalSeparator();
 		mFractionDigits = cur.getDefaultFractionDigits();
 		mAccepted = new Character[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', mSeparator};
 		Log.i(TAG + ".CurrencyWatcher()", "Separator: " + mSeparator);
 		Log.i(TAG + ".CurrencyWatcher()", "Model: " + Build.MODEL);
 		Log.i(TAG + ".CurrencyWatcher()", "Bad model?: " + 
 				new ArrayList<String>(Arrays.asList(mBadPhones)).contains(Build.MODEL));
 	}
 	
 	public char[] getAcceptedChars()
 	{
 		int len = mAccepted.length;
 		char[] accepted = new char[len];
 		for (int i = len - 1; i >= 0; --i)
 		{
 			accepted[i] = mAccepted[i];
 		}
 		return accepted;
 	}
 
 	public void afterTextChanged(Editable s)
 	{
 		String str = s.toString();
 		if (mChanged)
 		{
 			mChanged = false;
 			mOld = str;
 			return;
 		}
 
 		ArrayList<String> bad = new ArrayList<String>(Arrays.asList(mBadPhones));
 		if (str.contains(",") && mSeparator == '.' && bad.contains(Build.MODEL))
 		{
 			Log.i(TAG + ".afterTextChanged()", "Replacing commas in string \"" + str + "\"");
 			s.clear();
 			str = str.replace(',', '.');
 			s.append(str);
 		}
 		
		//Log.i(TAG + ".afterTextChanged()", "Attempting input: " + str);
 
 		final ArrayList<Character> accepted = new ArrayList<Character>(Arrays.asList(mAccepted));
 		int pos = 0;
 		for (char c : str.toCharArray())
 		{
 			if (!accepted.contains(c))
 			{
 				s.replace(pos, pos + 1, "", 0, 0);
 				Log.i(TAG + ".afterTextChanged()", "Input rejected because of invalid character: " + c);
 				return;
 			}
 			++pos;
 		}
 		
 		int separator_count = 0;
 		int last_sep = str.indexOf(mSeparator);
 		int previous_sep = -1;
 		
 		// see if there is more than one separator being added
 		while (last_sep != -1)
 		{
 			++separator_count;
 			previous_sep = last_sep;
 			last_sep = str.indexOf(mSeparator, last_sep + 1);
 		}
 		
 		// check if there is a separator being added that would make
 		// us exceed the maximum fractional digits
 		if (separator_count > 1 || (previous_sep != -1 && 
 				(s.length() - previous_sep - 1) > mFractionDigits))
 		{
 			mChanged = true;
 			s.replace(0, s.length(), mOld);
 		}
 	}
 
 	public void beforeTextChanged(CharSequence s, int start, int count, int after)
 	{
 		mOld = s.toString();
 	}
 
 	public void onTextChanged(CharSequence s, int start, int before, int count) { }	
 	
 	public static void setInputType(EditText e)
 	{
 		Configuration c = e.getContext().getResources().getConfiguration();
 		if (c.keyboard != Configuration.KEYBOARD_NOKEYS && 
 				c.hardKeyboardHidden != Configuration.HARDKEYBOARDHIDDEN_YES)
 		{
 			Log.i(TAG + ".setInputType",
 				"Hardware keyboard not hidden, changing input type");
 			e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
 		}
 		else
 		{
 			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(e.getContext());
 			String type = prefs.getString("key_input_type", PHONE_TYPE);
 			int input_type = InputType.TYPE_CLASS_PHONE;
 			
 			if (type.equals(NUMBER_TYPE))
 			{
 				input_type = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
 			}
 			
 			Log.i(TAG + ".setInputType",
 				"Hardware keyboard hidden (or not present), setting input type");
 			e.setInputType(input_type);
 		}
 	}
 }
