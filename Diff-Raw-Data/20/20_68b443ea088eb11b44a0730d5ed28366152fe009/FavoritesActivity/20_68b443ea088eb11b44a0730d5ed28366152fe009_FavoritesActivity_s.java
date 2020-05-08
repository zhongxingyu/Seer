 // FavoritesActivity.java
 /**
  * Copyright 2012 Kristin Mead
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.mapper.activities;
 
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class FavoritesActivity extends ListActivity
 {

<<<<<<< HEAD
	  // Add a click listener
	  lv.setOnItemClickListener(new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	        
	        // When clicked, show a toast with the TextView text
	        Toast.makeText(getApplicationContext(), ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
	    }
	  });
	}
=======
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
 
         // Create a menu of items
         String[] menuItems = getResources().getStringArray(
                 R.array.user_favorites);
         setListAdapter(new ArrayAdapter<String>(this,
                 android.R.layout.simple_list_item_1, menuItems));
 
         // Create the list view
         ListView lv = getListView();
         lv.setTextFilterEnabled(true);
 
         // Add a click listener
         lv.setOnItemClickListener(new OnItemClickListener()
         {
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
             {

                 // When clicked, show a toast with the TextView text
                 Toast.makeText(getApplicationContext(),
                         ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
             }
         });
     }
>>>>>>> cd4de3b12902a3bc145ca536559af9c23e2c2406
 }
