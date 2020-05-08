 package com.quackware.tric.ui;
 
 import java.util.ArrayList;
 
 import com.facebook.android.AsyncFacebookRunner;
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.Facebook.DialogListener;
 import com.facebook.android.FacebookError;
 import com.quackware.tric.MyApplication;
 import com.quackware.tric.R;
 import com.quackware.tric.service.CollectionService;
 import com.quackware.tric.stats.Stats;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.EditTextPreference;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceScreen;
 import android.text.InputType;
 
 public class MyPreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener, OnPreferenceClickListener {
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.prefs);
 		
 		buildPreferenceActivity();
 		setPreferenceClickListeners();
 		
 		//Check to see if we need to go to a sub preference screen.
 		if (getIntent().getExtras() != null)
 		{
 			String optionalTricName = getIntent().getExtras().getString("tricName");
 			if (optionalTricName != null)
 			{
 				// Find it and "click" it
 				Stats s = MyApplication.getStatsByName(optionalTricName);
 				PreferenceScreen parent = (PreferenceScreen) findPreference("button_"
 						+ s.getType().toLowerCase() + "_category_key");
 				int pos = findPreference("button_" + optionalTricName)
 						.getOrder();
 				parent.onItemClick(null, null, pos, 0);
 			}
 		}
 	}
 	
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
 	}
 	
 	@Override
 	public void onPause()
 	{
 		super.onPause();
 		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
 	}
 	
 	@Override
 	public void onActivityResult(int requestCode,int resultCode,Intent data)
 	{
 		super.onActivityResult(requestCode, resultCode, data);
 		if(MyApplication.getService() != null)
 		{
 			Facebook f = MyApplication.getFacebook();
 			f.authorizeCallback(requestCode, resultCode, data);
 		}
 	}
 	
 	private void setPreferenceClickListeners()
 	{
 		Preference facebookPref = (Preference)findPreference("facebook_auth");
 		facebookPref.setOnPreferenceClickListener(this);
 	}
 	
 	//The general structure of preferences will remain the same, however we do need to populate it based on the Stats collection.
 	private void buildPreferenceActivity()
 	{
 		ArrayList<Stats> stats = MyApplication.getStats();
 		PreferenceScreen phonePs = (PreferenceScreen)findPreference("button_phonestats_category_key");
 		PreferenceScreen socialPs = (PreferenceScreen)findPreference("button_socialstats_category_key");
 		PreferenceScreen appPs = (PreferenceScreen)findPreference("button_appstats_category_key");
 		for(Stats s : stats)
 		{
 			PreferenceScreen ps = generatePreferenceScreen(s);
 			if(s.getType().equals("PhoneStats"))
 			{
 				phonePs.addPreference(ps);
 			}
			else if(s.getType().equals("FacebookStats") || s.getType().equals("SocialStats"))
 			{
 				socialPs.addPreference(ps);
 			}
 			else if(s.getType().equals("AppStats"))
 			{
 				appPs.addPreference(ps);
 			}
 			else
 			{
 				//Unknown type.
 			}
 		}
 	}
 	
 	private PreferenceScreen generatePreferenceScreen(Stats s)
 	{
 		CheckBoxPreference sharePreference = new CheckBoxPreference(this);
 		sharePreference.setKey("checkbox_share_" + s.getName());
 		sharePreference.setTitle(getString(R.string.pref_share_title));
 		sharePreference.setSummary(getString(R.string.pref_share_summary));
 		sharePreference.setDefaultValue(false);
 		
 		CheckBoxPreference collectPreference = new CheckBoxPreference(this);
 		collectPreference.setKey("checkbox_collect_" + s.getName());
 		collectPreference.setTitle(getString(R.string.pref_collect_title));
 		collectPreference.setSummary(getString(R.string.pref_collect_summary));
 		collectPreference.setDefaultValue(true);
 		
 		EditTextPreference collectionIntervalPreference = new EditTextPreference(this);
 		collectionIntervalPreference.setKey("edittext_collectinterval_" + s.getName());
 		collectionIntervalPreference.setTitle(getString(R.string.pref_collectinterval_title));
 		collectionIntervalPreference.setSummary(getString(R.string.pref_collectinterval_summary));
 		collectionIntervalPreference.setDefaultValue("" + s.getDefaultCollectionInterval());
 		collectionIntervalPreference.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
 		
 		PreferenceScreen ps = getPreferenceManager().createPreferenceScreen(this);
 		ps.setKey("button_" + s.getName());
 		ps.setPersistent(false);
 		ps.setTitle(s.getName() + " tric");
 		
		
 		ps.addPreference(collectPreference);
 		ps.addPreference(collectionIntervalPreference);
 		ps.addPreference(sharePreference);
 		
 		return ps;
 	}
 
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
 		
 		//Get service for callback.
 		CollectionService service = MyApplication.getService();
 		//First check what preference changed.
 		if(key.startsWith("checkbox_share_"))
 		{
 			//todo later.
 		}
 		else if(key.startsWith("checkbox_collect_"))
 		{
 			String statsName = key.replace("checkbox_collect_", "");
 			service.refreshStatsInfo(MyApplication.getStatsByName(statsName));
 		}
 		else if(key.startsWith("edittext_collectinterval_"))
 		{
 			String statsName = key.replace("edittext_collectinterval_", "");
 			service.refreshStatsInfo(MyApplication.getStatsByName(statsName));
 		}
 	}
 
 	@Override
 	public boolean onPreferenceClick(Preference pref) {
 		if(pref.getKey().equals("facebook_auth"))
 		{
 			if(MyApplication.getFacebook() != null)
 			{
 				MyApplication.authorizeFacebook(this);
 			}
 		}
 		return true;
 	}
 
 }
