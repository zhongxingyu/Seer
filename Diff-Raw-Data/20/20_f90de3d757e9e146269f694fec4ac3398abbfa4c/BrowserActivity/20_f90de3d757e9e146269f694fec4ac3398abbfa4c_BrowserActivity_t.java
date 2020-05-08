 /*
 	Adventure App - Allows you to create an Adventure Book, or Download
  	books from other authors.
     Copyright (C) Fall 2013 Team 5 CMPUT 301 University of Alberta
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.uofa.adventure_app.activity;
 
 import java.util.ArrayList;
 import java.util.UUID;
 

 import java.util.UUID;
 

 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.GridView;
 
 import com.uofa.adventure_app.R;
 import com.uofa.adventure_app.application.AdventureApplication;
 
 public class BrowserActivity extends Activity implements AdventureActivity {
 	private ArrayAdapter<String> adapter;
 	ArrayList<String> List;
 	GridView grid;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_browser);
 		grid = (GridView) findViewById(R.id.gridView1);
 		List = new ArrayList<String>();
 		List.add("Story " + 1);
 		for (int i = 10; i<20; i++)
 			List.add("Story " + i);
 
 		adapter = new ArrayAdapter<String>(this,
 				R.layout.list_item, List);
 		grid.setAdapter(adapter);
 		 grid.setOnItemClickListener(new GridView.OnItemClickListener() {
 		       // @Override
 		        public void onItemClick(AdapterView<?> a, View v, int i, long l) {
 		    		viewStory(v);
 		        }
 		 });
 		/*
 		 //TESTING
 		Story tStory = new Story();
 		tStory.addAuthor(new Author("Chris"));
 		tStory.addFragement(new Fragement());
 		Fragement tFrag = new Fragement();
 		tFrag.addChoice(new Choice(new Fragement()));
 		tFrag.addChoice(new Choice(new Fragement()));
 		tFrag.addChoice(new Choice(new Fragement()));
 		tFrag.addChoice(new Choice(new Fragement()));
 		tFrag.addChoice(new Choice(new Fragement()));
 		tStory.addFragement(tFrag);
 		*/
 		// Search Example See Log of output.
 		/*
 		AdventureApplication.getWebServiceController().search("Chris2");
 		*/
 		AdventureApplication.getWebServiceController().fetch(UUID.fromString("1b2356dd-7681-4c0b-8079-399af94ee29f"));
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.main, menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle presses on the action bar items
 		switch (item.getItemId()) {
 		case R.id.new_story:
 			newStory();
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 	public void newStory() {
 
 		Intent myIntent = new Intent(this, EditStoryActivity.class);
 		this.startActivity(myIntent);
 		
 	}
 	
 	public void viewStory(View v) {
 		
 		Intent myIntent = new Intent(this, StoryActivity.class);
 		this.startActivity(myIntent);
 	}
 
 	
 }
