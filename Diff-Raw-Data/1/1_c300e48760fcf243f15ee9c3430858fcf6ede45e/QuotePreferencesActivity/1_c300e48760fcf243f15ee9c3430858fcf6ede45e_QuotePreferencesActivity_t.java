 package fr.quoteBrowser.activity;
 
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 import fr.quoteBrowser.R;
 import fr.quoteBrowser.service.QuoteProviderPreferencesDescription;
 import fr.quoteBrowser.service.QuoteProviderService;
 
 public class QuotePreferencesActivity extends PreferenceActivity {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	 
 	addPreferencesFromResource(R.xml.preferences);
     PreferenceCategory targetCategory = (PreferenceCategory)findPreference("quotes_providers_preference_category");
     for (QuoteProviderPreferencesDescription qpd:QuoteProviderService.getInstance(this).getQuoteProvidersPreferences()){
     	CheckBoxPreference checkBoxPreference = new CheckBoxPreference(this);
         checkBoxPreference.setKey(qpd.getKey());
         checkBoxPreference.setTitle(qpd.getTitle());
         checkBoxPreference.setSummary(qpd.getSummary());
        checkBoxPreference.setDefaultValue(true);
         targetCategory.addPreference(checkBoxPreference);
     }
     
 	}
 	
 }
