 /*
  * Copyright (C) 2009 University of Washington
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package org.odk.collect.android.activities;
 
 import java.util.HashMap;
 
 import org.odk.collect.android.R;
 import org.odk.collect.android.application.Collect;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTabHost;
 import android.util.Log;
 import android.view.View;
 import android.widget.TabHost;
 import android.widget.TabHost.TabContentFactory;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
 /**
  * An example of tab content that launches an activity via
  * {@link android.widget.TabHost.TabSpec#setContent(android.content.Intent)}
  */
 public class FileManagerTabs extends SherlockFragmentActivity {
 
     private FragmentTabHost mTabHost;
     
     private static final String FORMS_TAB = "forms_tab";
     private static final String DATA_TAB = "data_tab";
 
      @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setTitle(getString(R.string.manage_files));
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 
         setContentView(R.layout.file_manager_tabs_layout);
         
         
        if (savedInstanceState != null) {
            mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab")); //set the tab as per the saved state
        }
         
         mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
         mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
         TabHost.TabSpec tabSpec = this.mTabHost.newTabSpec("forms").setIndicator(getString(R.string.forms));
         mTabHost.addTab(tabSpec,FormManagerList.class, savedInstanceState);
         TabHost.TabSpec tabSpec2 = this.mTabHost.newTabSpec("data").setIndicator(getString(R.string.data));
         mTabHost.addTab(tabSpec2,DataManagerList.class, savedInstanceState);
         mTabHost.setCurrentTab(0);
     }
     
      @Override
      public boolean onCreateOptionsMenu(Menu menu) {
     	 System.out.println("FileManagerTabs : OnCreateOptionsMenu");
          Collect.getInstance().getActivityLogger().logAction(this, "onCreateOptionsMenu", "show");
          getSupportMenuInflater().inflate(R.menu.menu_form_manager, menu);
          return true;
      }
      
      @Override
      public boolean onOptionsItemSelected(MenuItem item) {
          switch (item.getItemId()) {
          case R.id.select_all:
         	 Log.i("FileManagertabs", "Current tab : "+mTabHost.getCurrentTab());
         	 if (mTabHost.getCurrentTab() == 1){
         		 ((DataManagerList) getSupportFragmentManager().findFragmentByTag("data")).selectAll();
         	 }else{
         		 ((FormManagerList) getSupportFragmentManager().findFragmentByTag("forms")).selectAll();
         	 }
         	 return true;
          case R.id.delete:
         	 if (mTabHost.getCurrentTab() == 1){
         		 ((DataManagerList) getSupportFragmentManager().findFragmentByTag("data")).delete();
         	 }else{
         		 ((FormManagerList) getSupportFragmentManager().findFragmentByTag("forms")).delete();
         	 }
         	 return true;
 	     case android.R.id.home:
 	         // This is called when the Home (Up) button is pressed
 	         // in the Action Bar.
 	         Intent parentActivityIntent = new Intent(this, MainMenuActivity.class);
 	         parentActivityIntent.addFlags(
 	                 Intent.FLAG_ACTIVITY_CLEAR_TOP |
 	                 Intent.FLAG_ACTIVITY_NEW_TASK);
 	         startActivity(parentActivityIntent);
 	         finish();
 	         return true;
          }
          return super.onOptionsItemSelected(item);
      }
 
     protected void onSaveInstanceState(Bundle outState) {
         outState.putString("tab", mTabHost.getCurrentTabTag()); //save the tab selected
         super.onSaveInstanceState(outState);
     }
 
 	@Override
     protected void onStart() {
     	super.onStart();
 		Collect.getInstance().getActivityLogger().logOnStart(this); 
     }
     
     @Override
     protected void onStop() {
 		Collect.getInstance().getActivityLogger().logOnStop(this); 
     	super.onStop();
     }
 }
