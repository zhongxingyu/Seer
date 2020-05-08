 package org.measureit.androet;
 
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.widget.ListView;
 
//TODO: add PIN settings    
 /**
  * 
  * @author ezsi
  */
 public class Preferences extends PreferenceActivity{
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         ListView view = new ListView(this);
         setContentView(view);
     }
 }
