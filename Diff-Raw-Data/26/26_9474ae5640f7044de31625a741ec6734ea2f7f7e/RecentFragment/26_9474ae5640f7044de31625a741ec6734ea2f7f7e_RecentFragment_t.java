 /**
 See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  This code is licensed
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */	
 package edu.rit.csh.androidwebnews;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class RecentFragment extends Fragment {
 	ListView lv;
 	RecentListAdapter<PostThread> listAdapter;
 	NewsgroupListMenu newsgroupListMenu;
 	ArrayList<PostThread> recentPostThreads;
 	HttpsConnector hc;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		Log.d("jddebug", "frag");
 		newsgroupListMenu = ((RecentActivity)getActivity()).newsgroupListMenu;
 		lv = new WebnewsListView(getActivity(), newsgroupListMenu);
 
 		recentPostThreads = new ArrayList<PostThread>();
 
 		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
 		hc = new HttpsConnector(getActivity());
 
 		String ng;
 		if ((ng = sharedPref.getString("newsgroups_json_string", "")) != "") {
			if (sharedPref.getBoolean("first_time", true)) {
				listAdapter = new RecentListAdapter<PostThread>(getActivity(), R.layout.rowlayout, new ArrayList<PostThread>());
			} else {
				listAdapter = new RecentListAdapter<PostThread>(getActivity(), R.layout.rowlayout, hc.getNewestFromString(ng));
			}
 			lv.setAdapter(listAdapter);
 			Log.d("jddebug - from file", ng);
 		}
 		Log.d("jddebug - from file", ng);
 		listAdapter = new RecentListAdapter<PostThread>(getActivity(), R.layout.rowlayout, new ArrayList<PostThread>());
 		lv.setAdapter(listAdapter);
 
 
 		lv.setOnItemClickListener(new OnItemClickListener()
 		{
 
 			@Override
 			public void onItemClick(AdapterView<?> adapter, View arg1, int position, long id) {
 				PostThread thread = (PostThread) adapter.getItemAtPosition(position);
 				((RecentActivity)getActivity()).onThreadSelected(thread);
 
 			}
 
 
 		});
 
 
 
 		return lv;
 	}
 
 
 	public void update(ArrayList<PostThread> newestFromString) {
 		listAdapter.clear();
 		recentPostThreads = newestFromString;
 		for (PostThread t : newestFromString) {
 			listAdapter.add(t);
 		}
 		listAdapter.notifyDataSetChanged();
 
 	}
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 		Log.d("MyDebugging", "Refreshing view!");
 		if(listAdapter != null)
 		{
			hc.getNewsGroups();
 			hc.getNewest(false);
 		}
 	}
 }
