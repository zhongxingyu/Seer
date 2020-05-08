 package org.utn.proyecto.helpful.integrart.integrar_t_android.activities.comosehace;
 
 import org.utn.proyecto.helpful.integrart.integrar_t_android.R;
 
 import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceActivity;
 
 public class ComoSeHaceActivityPreference extends PreferenceActivity {
 
    /*OnPreferenceChangeListener listener = new OnPreferenceChangeListener() {    
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            // newValue is the value you choose
            return false;
        }
    };*/

    

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         // TODO Auto-generated method stub
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.csh_preference_simple_menu);
        
        
        //listPreference.setOnPreferenceChangeListener(listener);
     }
 }
