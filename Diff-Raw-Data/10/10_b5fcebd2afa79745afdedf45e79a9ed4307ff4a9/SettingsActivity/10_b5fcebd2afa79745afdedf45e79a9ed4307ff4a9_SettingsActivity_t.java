 package com.app.getconnected.activities;
 
 import java.util.Locale;
 
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.text.Layout;
 import android.util.Log;
 import android.view.View;
import android.widget.ArrayAdapter;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
import android.widget.Spinner;
 
 import com.app.getconnected.R;
 
 public class SettingsActivity extends BaseActivity {
 
 	RadioGroup languageOptions;
 	String[] fontSpinnerArray = new String[3];
 	int currentScaleChoice = 0;
 	final static int ENGLISH = 0;
 	final static int DUTCH = 1;
 	String[] languages = {"en", "nl"};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_settings);
 		initLayout(R.string.title_activity_settings, true, true, true, false);
 		
 		updateSpinner();
 		languageOptions = (RadioGroup) findViewById(R.id.languagePicker);
 		initLanguagePicker();
 	}
 
 	private void initLanguagePicker() {
 		for(int i = 0; i < languages.length; i++) {
 			RadioButton button;
 		    button = new RadioButton(this);
 		    button.setText(languages[i]);
 		    button.setId(i);
 		    button.setOnClickListener(new View.OnClickListener() {
 		        @Override
 		        public void onClick(View v) {
 		            onButtonClicked(v);
 		        }
 		    });
 		    languageOptions.addView(button);		    
 		}
 	}
 	
 	protected void onButtonClicked(View v) {
 		changeLanguage(languages[v.getId()]);		
 	}
 
 	public void applySettings(View view){
 		Spinner s = (Spinner) findViewById(R.id.font_scale);
 		String scale_choice = s.getSelectedItem().toString();
 		int choice = s.getSelectedItemPosition();
		
		if (scale_choice.equals("Small")){
			changeScale(choice, 0.7);
		}
 		else if(scale_choice.equals("Medium")){
 			changeScale(choice, 1);
 		}
 		else if(scale_choice.equals("Big")){
 			changeScale(choice, 1.3);
 		}
 		else{
 			Log.e("This should not happen error", "Not a valid choice!");
 		}
 	}
 
 	public void changeLanguage(String language) {
 	    String languageToLoad = language;
 	    Locale locale = new Locale(languageToLoad);
             Locale.setDefault(locale);
 	    Configuration config = new Configuration();
 	    config.locale = locale;
 	    initLanguagePicker();
 	    updateContent(config);
 	}
 	
 	public void changeScale(int choice, double scale) {
 	    Configuration config = new Configuration();
 	    Log.d("Default font scale", ""+config.fontScale);
 	    config.fontScale = (float) scale;
 	    currentScaleChoice = choice;
 	    updateContent(config);
 	}
 	
 	public void updateContent(Configuration config){
 	    getBaseContext().getResources().updateConfiguration(config, 
 	    getBaseContext().getResources().getDisplayMetrics());
 	    this.setContentView(R.layout.activity_settings);
 	    updateSpinner();
 	}
 
 	public void updateSpinner(){
 	    fontSpinnerArray[0] = "Small";
 	    fontSpinnerArray[1] = "Medium";
 	    fontSpinnerArray[2] = "Big";
 		
         Spinner s = (Spinner) findViewById(R.id.font_scale);
         ArrayAdapter adapter = new ArrayAdapter(this,
         android.R.layout.simple_spinner_item, fontSpinnerArray);
         s.setAdapter(adapter);
         s.setSelection(currentScaleChoice);
 	}
 		
 }
