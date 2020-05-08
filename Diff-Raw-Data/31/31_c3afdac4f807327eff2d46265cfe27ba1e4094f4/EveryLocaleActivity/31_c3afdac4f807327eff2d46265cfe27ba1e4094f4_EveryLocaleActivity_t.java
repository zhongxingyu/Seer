 /*
     Copyright (C) 2012 Sweetie Piggy Apps <sweetiepiggyapps@gmail.com>
 
     This file is part of Every Locale.
 
     Every Locale is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation; either version 3 of the License, or
     (at your option) any later version.
 
     Every Locale is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Every Locale; if not, see <http://www.gnu.org/licenses/>.
 */
 
 package com.sweetiepiggy.everylocale;
 
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Locale;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
import android.widget.EditText;
 
 
 public class EveryLocaleActivity extends Activity {
 	
 	HashMap<String, String> language_map = new HashMap<String, String>();
 	HashMap<String, String> country_map = new HashMap<String, String>();
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         Locale default_locale = Locale.getDefault();
         
         ArrayAdapter<String> language_names = new ArrayAdapter<String>(this,
         		R.layout.list_language_names);
 
         for (String language : Locale.getISOLanguages()) {
         		Locale locale = new Locale(language);
         		String local_name = locale.getDisplayLanguage();
         		String native_name = locale.getDisplayLanguage(locale);
         		
         		language_names.add(language);
         		
         		if (!local_name.equals(language)) {
         			language_names.add(local_name);
         		}
         		
         		language_map.put(local_name, language);
         		if (!local_name.equals(native_name)) {
         			language_names.add(native_name);
             		language_map.put(native_name, language);
         		}
         }
         
         ArrayAdapter<String> country_names = new ArrayAdapter<String>(this,
         		R.layout.list_country_names);
 
         String default_language = default_locale.getLanguage();
         
         for (String country : Locale.getISOCountries()) {
         		Locale locale = new Locale(default_language, country);
         		String local_name = locale.getDisplayCountry();
         		String native_name = locale.getDisplayCountry(locale);
         		
         		country_names.add(country);
         		
         		if (!local_name.equals(country)) {
         			country_names.add(local_name);
         		}
         		
         		country_map.put(local_name, country);
         		if (!local_name.equals(native_name)) {
         			country_names.add(native_name);
             		country_map.put(native_name, country);
         		}
         }
         
         AutoCompleteTextView languageTextView = (AutoCompleteTextView) findViewById(R.id.language_autocomplete);
         languageTextView.setAdapter(language_names);
         
         AutoCompleteTextView countryTextView = (AutoCompleteTextView) findViewById(R.id.country_autocomplete);
         countryTextView.setAdapter(country_names);
         
         String language_name = default_locale.getDisplayLanguage();
         String country_name = default_locale.getDisplayCountry();
        String variant_code = default_locale.getVariant();
         
         ((AutoCompleteTextView)findViewById(R.id.language_autocomplete)).setText(language_name);
         ((AutoCompleteTextView)findViewById(R.id.country_autocomplete)).setText(country_name);
        ((EditText)findViewById(R.id.variant_edittext)).setText(variant_code);
         
    	Button save_button = (Button)findViewById(R.id.save_button);
 
    	save_button.setOnClickListener(new View.OnClickListener() {
     		public void onClick(View v) {
     			String language = ((AutoCompleteTextView) findViewById(R.id.language_autocomplete)).getText().toString();
     			String country = ((AutoCompleteTextView) findViewById(R.id.country_autocomplete)).getText().toString();
    			String variant = ((EditText) findViewById(R.id.variant_edittext)).getText().toString();
     			
     			String language_code = language_map.containsKey(language) ? language_map.get(language) : language;
     			String country_code = country_map.containsKey(country) ? country_map.get(country) : country;
     			
     			try {
     				Class ActivityManagerNative = Class.forName("android.app.ActivityManagerNative");  
     				Class IActivityManager = Class.forName("android.app.IActivityManager");  
     		      
     				Method getDefault =  ActivityManagerNative.getMethod("getDefault", null);  
     				Object am = IActivityManager.cast(getDefault.invoke(ActivityManagerNative, null));  
     		      
     				Method getConfiguration = am.getClass().getMethod("getConfiguration", null);
     		      
     				Configuration config = (Configuration) getConfiguration.invoke(am, null);
         			Locale locale = new Locale(language_code, country_code, variant);
         			Locale.setDefault(locale);
     				config.locale = locale;
     		      
     				Class[] args = new Class[1];  
     				args[0] = Configuration.class;  
     				Method updateConfiguration =  am.getClass().getMethod("updateConfiguration", args);  
     				updateConfiguration.invoke(am, config);
     			} catch (Exception e) {
     			}
     		}
     	});
    	
    	
    	Button cancel_button = (Button)findViewById(R.id.cancel_button);

    	cancel_button.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) {
    	        Locale default_locale = Locale.getDefault();
    	        String language_name = default_locale.getDisplayLanguage();
    	        String country_name = default_locale.getDisplayCountry();
    	        String variant_code = default_locale.getVariant();
    	        
    	        ((AutoCompleteTextView)findViewById(R.id.language_autocomplete)).setText(language_name);
    	        ((AutoCompleteTextView)findViewById(R.id.country_autocomplete)).setText(country_name);
    	        ((EditText)findViewById(R.id.variant_edittext)).setText(variant_code);
    		}
    	});
     }
     
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.options_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.settings:
 			Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
 			startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 }
