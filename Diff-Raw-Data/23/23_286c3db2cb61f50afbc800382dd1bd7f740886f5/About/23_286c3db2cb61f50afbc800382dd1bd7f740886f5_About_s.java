 package com.WordFinder;
 
 import android.app.Activity;
 import android.os.Bundle;
 
 /**
  * About activity. Shows a simple view crediting the authors and describing the
  * project.
  *
  * @author John Mooring (jmooring)
 * @author Chris Buck (cmbuck)
  * @author Bryan Malyn (bmalyn)
  * @version 2011.12.29
  */
 public class About
     extends Activity
 {
     /**
      * Called when the activity is created
      * @param savedInstanceState unused
      */
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.about);
     }
 }
