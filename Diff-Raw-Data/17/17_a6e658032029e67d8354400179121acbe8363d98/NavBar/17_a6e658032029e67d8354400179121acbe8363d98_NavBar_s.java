 package com.android.settings;
 
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.Preference;
 import android.preference.PreferenceScreen;
 import android.provider.Settings;
 import com.android.settings.R;
 
     public class NavBar extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
 
     private static final String PREF_NAV_COLOR = "nav_button_color";
     private static final String SHOW_SEARCH_BUTTON = "search_button";
 	private static final String SHOW_LEFT_MENU_BUTTON = "left_menu_button";
 	private static final String SHOW_RIGHT_MENU_BUTTON = "right_menu_button";
     private static final String SHOW_SEARCH_BUTTON_LAND = "search_button_land";
 	private static final String SHOW_TOP_MENU_BUTTON_LAND = "top_menu_button_land";
 	private static final String SHOW_BOT_MENU_BUTTON_LAND = "bottom_menu_button_land";
 
 	ColorPickerPreference mNavigationBarColor;
     CheckBoxPreference mShowSearchButton;
     CheckBoxPreference mShowLeftMenuButton;
     CheckBoxPreference mShowRightMenuButton;
     CheckBoxPreference mShowSearchButtonLand;
     CheckBoxPreference mShowTopMenuButtonLand;
     CheckBoxPreference mShowBotMenuButtonLand;
 	
 
         @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
 
             // Load the preferences from an XML resource
             addPreferencesFromResource(R.xml.navbar);
 
             mShowSearchButton = (CheckBoxPreference) findPreference(SHOW_SEARCH_BUTTON);
             mShowSearchButton.setChecked(Settings.System.getInt(getActivity()
                     .getContentResolver(), Settings.System.SHOW_SEARCH_BUTTON,
                     0) == 1);
             
             mShowLeftMenuButton = (CheckBoxPreference) findPreference(SHOW_LEFT_MENU_BUTTON);
             mShowLeftMenuButton.setChecked(Settings.System.getInt(getActivity()
                     .getContentResolver(), Settings.System.SHOW_LEFT_MENU_BUTTON,
                     0) == 1);            
             
             mShowRightMenuButton = (CheckBoxPreference) findPreference(SHOW_RIGHT_MENU_BUTTON);
             mShowRightMenuButton.setChecked(Settings.System.getInt(getActivity()
                     .getContentResolver(), Settings.System.SHOW_RIGHT_MENU_BUTTON,
                     0) == 1);            
             
             mShowSearchButtonLand = (CheckBoxPreference) findPreference(SHOW_SEARCH_BUTTON_LAND);
             mShowSearchButtonLand.setChecked(Settings.System.getInt(getActivity()
                     .getContentResolver(), Settings.System.SHOW_SEARCH_BUTTON_LAND,
                     0) == 1);
             
             mShowTopMenuButtonLand = (CheckBoxPreference) findPreference(SHOW_TOP_MENU_BUTTON_LAND);
             mShowTopMenuButtonLand.setChecked(Settings.System.getInt(getActivity()
                     .getContentResolver(), Settings.System.SHOW_TOP_MENU_BUTTON_LAND,
                     0) == 1);            
             
             mShowBotMenuButtonLand = (CheckBoxPreference) findPreference(SHOW_BOT_MENU_BUTTON_LAND);
             mShowBotMenuButtonLand.setChecked(Settings.System.getInt(getActivity()
                     .getContentResolver(), Settings.System.SHOW_BOT_MENU_BUTTON_LAND,
                     0) == 1);                        
             
             
             
 
             mNavigationBarColor = (ColorPickerPreference) findPreference(PREF_NAV_COLOR);
             mNavigationBarColor.onColorChanged(Settings.System.getInt(getActivity()
                     .getContentResolver(), Settings.System.NAVIGATION_BAR_TINT,
                     0));
             mNavigationBarColor.setOnPreferenceChangeListener(this);
         }
 
 
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                 Preference preference) {
            if (preference == mShowSearchButton) {
                 Settings.System.putInt(getActivity().getContentResolver(),
                         Settings.System.SHOW_SEARCH_BUTTON,
                        ((CheckBoxPreference) preference).isChecked() ? 0 : 1);
 
            } else if (preference == mShowLeftMenuButton) {
               Settings.System.putInt(getActivity().getContentResolver(),
                       Settings.System.SHOW_LEFT_MENU_BUTTON,
                      ((CheckBoxPreference) preference).isChecked() ? 0 : 1);        	  
 
            } else if (preference == mShowRightMenuButton) {
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.SHOW_RIGHT_MENU_BUTTON,
                       ((CheckBoxPreference) preference).isChecked() ? 0 : 1);        	  
 
            } else if (preference == mShowSearchButtonLand) {
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.SHOW_SEARCH_BUTTON_LAND,
                       ((CheckBoxPreference) preference).isChecked() ? 0 : 1);        	  
 
            } else if (preference == mShowTopMenuButtonLand) {
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.SHOW_TOP_MENU_BUTTON_LAND,
                       ((CheckBoxPreference) preference).isChecked() ? 0 : 1);                  
                
            } else if (preference == mShowBotMenuButtonLand) {
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.SHOW_BOT_MENU_BUTTON_LAND,
                       ((CheckBoxPreference) preference).isChecked() ? 0 : 1);                  
            } 
 
             return super.onPreferenceTreeClick(preferenceScreen, preference);
         }
        
        
 
         @Override
         public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference == mNavigationBarColor) {
                 String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                         .valueOf(newValue)));
                 preference.setSummary(hex);
 
                 int intHex = ColorPickerPreference.convertToColorInt(hex);
                 Settings.System.putInt(getActivity().getContentResolver(),
                         Settings.System.NAVIGATION_BAR_TINT, intHex);
                 return true;
 
             }
            
            
            
            
            
             return false;
         }
 
 }
