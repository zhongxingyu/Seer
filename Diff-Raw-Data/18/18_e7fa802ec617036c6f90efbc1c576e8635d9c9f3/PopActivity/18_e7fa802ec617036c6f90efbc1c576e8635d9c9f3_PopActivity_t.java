 package com.tonyjhuang.listpop;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class PopActivity extends Activity {
 	private TextView listHeader, popResult;
 	private Button pop;
 	private ArrayList<String> list = new ArrayList<String>();
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.pop);
 		
 		//Initialize passed in intent and retrieve extras.
		Bundle extras = getIntent().getExtras();
		String listName = extras
				.getString(DbAdapter.LIST_HEADER);
		String codifiedList = extras
				.getString(DbAdapter.LIST);
 		
 		//Set list header text.
		listHeader = (TextView) findViewById(R.id.listname);
 		listHeader.setText(listName);
 		
 		//Initialize ArrayList variable.
 		list = interpret(codifiedList);
 		
 		popResult = (TextView) findViewById(R.id.popresult);
 		
 		//Initialize and add OnClickListener to pop button.
 		pop = (Button) findViewById(R.id.pop);
 		hookUpPop();
 
 		//Add up navigation affordance to the Action Bar.
 		ActionBar actionBar = getActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
         actionBar.setTitle("Add List");
 		
 	}
 
 	//Create a random number generator, and set the popResult textview
 	// text to a random String returned from the list field.
 	private void hookUpPop() {
 		pop.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View view) {
 				Random generator = new Random();
 
 				int totalNumberOfItems = list.size();
 				
 				int randomIndex = generator.nextInt(totalNumberOfItems);
 				String randomString = list.get(randomIndex);
 				popResult.setText(randomString);
 			}
 		});
 	}
 
 	//Returns an ArrayList of Strings given a codified String
 	// returned from the SQLite database.
 	private ArrayList<String> interpret(String s) {
 		ArrayList<String> a = new ArrayList<String>();
 		String current = s;
 
 		int nextCommaIndex = current.indexOf("|");
 
 		while (current.length() != 0) {
 			a.add(current.substring(0, nextCommaIndex));
 			current = current.substring(nextCommaIndex + 1);
 			nextCommaIndex = current.indexOf("|");
 		}
 
 		return a;
 	}
 
 	//Implementation of Ancestral Navigation via the Action Bar.
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	        case android.R.id.home:
 	            // This is called when the Home (Up) button is pressed
 	            // in the Action Bar. Code was taken from:
 	        	// http://developer.android.com/training/implementing-navigation/ancestral.html
 	            Intent upIntent = new Intent(this, StartActivity.class);
 	            upIntent.addFlags(
 	                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
 	                    Intent.FLAG_ACTIVITY_NEW_TASK);
 	            startActivity(upIntent);
 	            finish();
 	            return true;
 	    }
 	    return super.onOptionsItemSelected(item);
 	}
 }
