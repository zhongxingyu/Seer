 /**
 * @contributor(s): Rune Sætre (NTNU), Jacqueline Floch (SINTEF)
  * @version: 		0.1
  * @date:			22 November 2011
 * @revised:		12 June 2012
  *
  * Copyright (C) 2011-2012 UbiCompForAll Consortium (SINTEF, NTNU)
  * for the UbiCompForAll project
  *
  * Licensed under the Apache License, Version 2.0.
  * You may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied.
  *
  * See the License for the specific language governing permissions
  * and limitations under the License.
  * 
  */
 
 /**
  * @description: 
  * This class tracks existing DBs.
  * This class keeps hold of the tabs used in import mode.
  */
 
 package org.ubicompforall.CityExplorer.gui;
 
 import org.ubicompforall.CityExplorer.CityExplorer;
 import org.ubicompforall.CityExplorer.R;
 
 import android.app.TabActivity;
 import android.content.*;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.os.*;
 import android.widget.*;
 
 public class ImportActivity extends TabActivity {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.tablayout);
 		debug(2, "");
 		initTabs();
 	}//onCreate
 
 	
 	private void debug( int level, String message ) {
 		CityExplorer.debug( level, message );		
 	} //debug
 
 
 
 	/**
 	 * Initializes the tabs used in this activity.
 	 */
 	private void initTabs() {
 		Resources res = getResources(); // Resource object to get Drawables
 		TabHost tabHost = getTabHost();  // The activity TabHost
 		TabHost.TabSpec spec;  // Reusable TabSpec for each tab
 		Intent intent;  // Reusable Intent for each tab
 
 		// TAB 1 Intent
 		intent = new Intent().setClass(this, ImportLocalTab.class);
 		spec = tabHost.newTabSpec("local").setIndicator("LOCAL", res.getDrawable(R.drawable.tab_selector)).setContent(intent);
 		tabHost.addTab(spec);
 		
 		// TAB 2 Intent
 		intent = new Intent().setClass(this, ImportWebTab.class);
 		spec = tabHost.newTabSpec("web").setIndicator("DOWNLOAD", res.getDrawable(R.drawable.tab_selector)).setContent(intent);
 		tabHost.addTab(spec);
 
 		tabHost.getTabWidget().getChildAt(0).setBackgroundColor(Color.parseColor("#000000"));
 		tabHost.getTabWidget().getChildAt(1).setBackgroundColor(Color.parseColor("#000000"));
 
 		tabHost.setCurrentTab(0);
 	}//initTabs
 
	
	/**
	 * Terminates activity when the user when another activity starts (either on user behalf or not).
	 * Done to avoid keeping the activity on the stack when a user selects a downloaded database in the
	 * Android notification window.
	 */	
	public void onStop () {
		super.onStop ();
		finish ();
	}//onCreate

	
 }//class
