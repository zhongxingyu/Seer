 // Copyright 2009 Google Inc.
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 
 // dbartists - Douban artists client for Android
 // Copyright (C) 2011 Max Lv <max.c.lv@gmail.com>
 //
 // Licensed under the Apache License, Version 2.0 (the "License"); you may not
 // use this file except in compliance with the License.  You may obtain a copy
 // of the License at
 //
 //      http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 // WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 // License for the specific language governing permissions and limitations
 // under the License.
 //
 //
 //                           ___====-_  _-====___
 //                     _--^^^#####//      \\#####^^^--_
 //                  _-^##########// (    ) \\##########^-_
 //                 -############//  |\^^/|  \\############-
 //               _/############//   (@::@)   \\############\_
 //              /#############((     \\//     ))#############\
 //             -###############\\    (oo)    //###############-
 //            -#################\\  / VV \  //#################-
 //           -###################\\/      \//###################-
 //          _#/|##########/\######(   /\   )######/\##########|\#_
 //          |/ |#/\#/\#/\/  \#/\##\  |  |  /##/\#/  \/\#/\#/\#| \|
 //          `  |/  V  V  `   V  \#\| |  | |/#/  V   '  V  V  \|  '
 //             `   `  `      `   / | |  | | \   '      '  '   '
 //                              (  | |  | |  )
 //                             __\ | |  | | /__
 //                            (vvv(VVV)(VVV)vvv)
 //
 //                             HERE BE DRAGONS
 
 package org.dbartists;
 
 import java.net.URLEncoder;
 
 import org.dbartists.api.Artist;
 
 import com.flurry.android.FlurryAgent;
 
 import android.app.SearchManager;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 
 public class SearchArtistsListActivity extends PlayerActivity implements
 		OnItemClickListener {
 
 	private final static String TAG = "TopArtistsListActivity";
 	private String apiUrl = Constants.SEARCH_ARTIST_API_URL;
 	private String description;
 	private String searchKeywords;
 
 	protected SearchArtistsListAdapter listAdapter;

 	public void onStart() {
 		super.onStart();
 		FlurryAgent.onStartSession(this, "X51AT1EBV972SS9GNXTP");
 	}
 
 	public void onStop() {
 		super.onStop();
 		FlurryAgent.onEndSession(this);
 	}
 
 	private void addArtists() {
 
 		listAdapter.addMoreArtists(apiUrl, 0);
 
 	}
 
 	@Override
 	public CharSequence getMainTitle() {
 		Log.d(TAG, description);
 		return description;
 	}
 
 	@Override
 	public boolean isRefreshable() {
 		return true;
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		final Intent queryIntent = getIntent();
 		searchKeywords = queryIntent.getStringExtra(SearchManager.QUERY);
 		description = getString(R.string.msg_search);
 
 		if (searchKeywords != null) {
 			apiUrl = apiUrl + "?p=" + URLEncoder.encode(searchKeywords.trim());
 			description = description + searchKeywords;
 		}
 
 		super.onCreate(savedInstanceState);
 		ViewGroup container = (ViewGroup) findViewById(R.id.Content);
 		View.inflate(this, R.layout.basic_list, container);
 
 		ListView listView = (ListView) findViewById(R.id.ListView01);
 		listView.setOnItemClickListener(this);
		listAdapter = new SearchArtistsListAdapter(
				SearchArtistsListActivity.this);
 		listView.setAdapter(listAdapter);
 
 		addArtists();
 
 		if (searchKeywords == null) {
 			onSearchRequested();
 			ProgressBar titleProgressBar;
 			titleProgressBar = (ProgressBar) findViewById(R.id.leadProgressBar);
 			// hide the progress bar if it is not needed
 			titleProgressBar.setVisibility(View.GONE);
 		}
 
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position,
 			long id) {
 		Artist s = (Artist) parent.getAdapter().getItem(position);
 		if (s == null) {
 			onSearchRequested();
 		} else {
 			Intent i = new Intent(this, TrackListActivity.class);
 			i.putExtra(Constants.EXTRA_ARTIST_NAME, s.getName());
 			i.putExtra(Constants.EXTRA_ARTIST_IMG, s.getImg());
 			i.putExtra(Constants.EXTRA_ARTIST_URL, s.getUrl());
 			startActivityWithoutAnimation(i);
 		}
 	}
 
 	@Override
 	public void onNewIntent(Intent intent) {
		if (intent == null)
			return;
 		String queryAction = intent.getAction();
		if (queryAction == null)
			return;
 		if (queryAction.equals(Intent.ACTION_SEARCH)) {
 			searchKeywords = intent.getStringExtra(SearchManager.QUERY);
 			description = getString(R.string.msg_search);
 			ProgressBar titleProgressBar;
 			titleProgressBar = (ProgressBar) findViewById(R.id.leadProgressBar);
 			// hide the progress bar if it is not needed
 			titleProgressBar.setVisibility(View.VISIBLE);
 			if (searchKeywords != null) {
 				apiUrl = Constants.SEARCH_ARTIST_API_URL + "?p="
 						+ URLEncoder.encode(searchKeywords.trim());
 				description = description + searchKeywords;
 				if (titleText != null)
 					titleText.setText(getMainTitle());
 				refresh();
 			}
 		}
 	}
 
 	@Override
 	public void refresh() {
 		listAdapter.clear();
 		addArtists();
 	}
 }
