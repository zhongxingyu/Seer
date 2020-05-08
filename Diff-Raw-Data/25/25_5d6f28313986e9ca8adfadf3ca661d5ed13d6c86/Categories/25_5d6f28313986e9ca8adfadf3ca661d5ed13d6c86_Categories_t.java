 package no.group09.fragments;
 
 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import no.group09.arduinoair.R;
 import no.group09.arduinoair.R.id;
 import no.group09.arduinoair.R.layout;
 import android.app.Activity;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.SearchView;
 import android.widget.Toast;
 
 public class Categories extends MyFragment{
 
 	private int mCurrentPage;
 	private Activity activity;
 //	private Fragment fragment;
 	
 	private ListView list;
     private ListAdapterCategory adapter;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		/** Getting the arguments to the Bundle object */
 		Bundle data = getArguments();
 
 		/** Getting integer data of the key current_page from the bundle */
 		mCurrentPage = data.getInt("current_page", 0);
 		
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
 		final View v = inflater.inflate(R.layout.main, container,false);
 //        activity = (Activity) v.getContext();
 //        SharedPreferences prefs = activity.getSharedPreferences("no.group09.arduinoair", Context.MODE_PRIVATE);
 		
 		ArrayList<HashMap<String, String>> category_list = new ArrayList<HashMap<String, String>>();
 		
 		////////////////////////////////////////////////////////////////////////////////////
 		//TODO: change this with SQLLite or something
 
 		HashMap<String, String> map = new HashMap<String, String>();
 		map.put(ListAdapterCategory.KEY_ID, "1");
 		map.put(ListAdapterCategory.APP_NAME, "Games");
 		map.put(ListAdapterCategory.DISTRIBUTOR, "");
 		map.put(ListAdapterCategory.RATING, "");
 		category_list.add(map);
 		
 		map = new HashMap<String, String>();
 		map.put(ListAdapterCategory.KEY_ID, "2");
		map.put(ListAdapterCategory.APP_NAME, "Medical");
 		map.put(ListAdapterCategory.DISTRIBUTOR, "");
 		map.put(ListAdapterCategory.RATING, "");
 		category_list.add(map);
 		
 		map = new HashMap<String, String>();
 		map.put(ListAdapterCategory.KEY_ID, "3");
 		map.put(ListAdapterCategory.APP_NAME, "Tools");
 		map.put(ListAdapterCategory.DISTRIBUTOR, "");
 		map.put(ListAdapterCategory.RATING, "");
 		category_list.add(map);
 		
 		map = new HashMap<String, String>();
 		map.put(ListAdapterCategory.KEY_ID, "4");
 		map.put(ListAdapterCategory.APP_NAME, "Media");
 		map.put(ListAdapterCategory.DISTRIBUTOR, "");
 		map.put(ListAdapterCategory.RATING, "");
 		category_list.add(map);
 		
 		////////////////////////////////////////////////////////////////////////////////////
 		
 		list = (ListView)v.findViewById(R.id.list);
 		
 		// Getting adapter by passing xml data ArrayList
         adapter = new ListAdapterCategory(v.getContext(), category_list);        
         list.setAdapter(adapter);
 
         // Click event for single list row
         list.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				Toast.makeText(view.getContext(), "WOW, nice try!", Toast.LENGTH_SHORT).show();
 			}
 		});	
 		
 		return v;
 	}
 }
