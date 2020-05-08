 package org.utn.proyecto.helpful.integrart.integrar_t_android.activities.comosehace;
 
 import org.utn.proyecto.helpful.integrart.integrar_t_android.R;
 
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 
 public class ComoSeHaceActivityPreference extends PreferenceActivity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         // TODO Auto-generated method stub
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.csh_preference_simple_menu);
     }
 }
