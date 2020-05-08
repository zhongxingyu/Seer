 package com.powerSearch;
 
 import android.app.Activity;
 import android.app.SearchManager;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Text_screen1 extends Activity {
 
 	Button start_search;
 	EditText enter_string;
 	ImageView go_home;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_text_screen1);
 	
 		start_search = (Button) findViewById(R.id.button_start_search);
 		enter_string = (EditText) findViewById(R.id.editText_textsearch);
 		go_home =(ImageView) findViewById(R.id.homeFromTextSearch1);
 		
 		enter_string.setFocusableInTouchMode(true);
 		enter_string.requestFocus();
 		
 		start_search.setOnClickListener(new OnClickListener(){
 			public void onClick(View V){
 			//	System.out.println("IN onclick in location screen 1");
 				text_search();
 				finish();
 			}
 	});
 		
 		enter_string.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
 		enter_string.setOnEditorActionListener(new TextView.OnEditorActionListener(){
 			public boolean onEditorAction(	TextView V,int actionid, KeyEvent event)
 			{
 				System.out.println("In check for enter");
 				if(actionid == EditorInfo.IME_ACTION_SEARCH){
 					text_search();
 					Toast.makeText(getApplicationContext(), enter_string.getText().toString(), Toast.LENGTH_SHORT).show();
 					return true;
 					}
 				
 				return false;
 			}
 		});
 	}
 	
 	/*
 	 *  This method takes the user back to the home screen
 	 */
 	
 	public void goHome(View view){
 		Intent intent = new Intent(this, HomeScreen.class);
 		startActivity(intent);
 	}
 	
 	/*
	 *  Starts a google search in the browser. The Intent.ACTION_WEB_SEARCH is used to start hte search.
 	 */
 	
 	private void text_search(){
 		
 		String search_input = enter_string.getText().toString() ;
 		
 		/*
 		 *  If nothing has been entered in the edit text, ask the user to enter input again.
 		 */
 		if (search_input.length() == 0){
 			Toast.makeText(getApplicationContext(), "No text to search", Toast.LENGTH_LONG).show();
 			Intent restart = new Intent(this, Text_screen1.class);
 			startActivity(restart);
 		}
 		
 		Intent search = new Intent(Intent.ACTION_WEB_SEARCH);  
 		search.putExtra(SearchManager.QUERY, search_input);  
 		startActivity(search);  
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.text_screen1, menu);
 		return true;
 	}
 
 }
