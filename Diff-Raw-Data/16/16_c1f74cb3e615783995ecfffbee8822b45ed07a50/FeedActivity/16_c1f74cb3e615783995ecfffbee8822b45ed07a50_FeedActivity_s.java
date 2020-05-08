 /*Copyright 2013 Daniel Benedykt
 
 This file is part of AngelList Mobile.
 
 AngelList Mobile is free software: you can redistribute it and/or modify 
 it under the terms of the GNU General Public License as published by 
 the Free Software Foundation, either version 2 of the License, or 
 (at your option) any later version.
 
 AngelList Mobile is distributed in the hope that it will be useful, 
 but WITHOUT ANY WARRANTY; without even the implied warranty of 
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License 
 along with AngelList Mobile. If not, see http://www.gnu.org/licenses/.
 */
 
 
 package org.angellist.angellistmobile;
 
 import org.json.JSONArray;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ListView;
 
 public class FeedActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_feed);
 		
 		new AsyncHttpGet().execute();
 		//JSONArray jsonarray = ApiCalls.GetActivityFeed();
 		/*ListView listView = (ListView) findViewById(R.id.listViewFeed);
 		listView.setAdapter(new JSONAdapter(this, jsonarray));
 		listView.setTextFilterEnabled(true);
 */
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.login, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle presses on the action bar items
 	    switch (item.getItemId()) {
 	        case R.id.action_about:
 	        	Intent intent = new Intent(this, AboutActivity.class);
 	    		startActivity(intent);
 	    }
 		return true;
 	}
 	
 	public class AsyncHttpGet extends AsyncTask<Void, Void, JSONArray> {
 	    
 
 	    /**
 	     * constructor
 	     */
 	    public AsyncHttpGet() {
 	        
 	    }
 
 	    /**
 	     * background
 	     */
 	    @Override
 	    protected JSONArray doInBackground(Void... params) {
 	    	JSONArray jsonarray = ApiCalls.GetActivityFeed();
 	        return jsonarray;
 	    }
 
 	    /**
 	     * on getting result
 	     */
 	    @Override
 	    protected void onPostExecute(JSONArray result) {
 	        //access_token
 	    	ListView listView = (ListView) findViewById(R.id.listViewFeed);
 			listView.setAdapter(new JSONAdapter(FeedActivity.this, result));
 			listView.setTextFilterEnabled(true);
 	        
 	    }
 
 	}
 
 }
