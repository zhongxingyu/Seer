 package com.example.project381;
 
 import android.inputmethodservice.Keyboard;
 import android.inputmethodservice.KeyboardView;
 import android.os.Bundle;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Context;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	
     ProjectTextView pview;
     TextView lineNumbers;
     KeyboardView keyboardView;
     
     String text;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         
     	super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main); 
     	
         pview = (ProjectTextView) findViewById(R.id.main_text_input);
         
         InputMethodManager inputMethod = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
         inputMethod.hideSoftInputFromWindow(pview.getWindowToken(), 0);
         
         lineNumbers = (TextView) findViewById(R.id.lineNumbers);
        
         Menu menu = (Menu) findViewById(R.id.file);
         
         MenuItem item1 = (MenuItem) findViewById(R.id.delete);      
         
         TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
         tabHost.setup();
         
         TabSpec mainSpec = tabHost.newTabSpec("Main");
         mainSpec.setIndicator("main");
         mainSpec.setContent(R.id.main_text_input);
         tabHost.addTab(mainSpec);
         
         ActionBar aBar = getActionBar();
         aBar.setDisplayShowTitleEnabled(false);
         aBar.setDisplayShowHomeEnabled(false);
         
         keyboardView = (KeyboardView) findViewById(R.id.keyboardView);
         Keyboard keyboard = new Keyboard(this, R.xml.keyboard);
         keyboardView.setKeyboard(keyboard);
         keyboardView.setEnabled(true);
         keyboardView.setPreviewEnabled(true);
         KeyboardListener kbl = new KeyboardListener(this, null);
         keyboardView.setOnKeyListener(kbl);
         keyboardView.setOnKeyboardActionListener(kbl);
         keyboardView.setVisibility(View.INVISIBLE);
         
      	pview.setOnTouchListener(new OnTouchListener() {
 
			public boolean onTouch(View v, MotionEvent event) {
 		           if(keyboardView.getVisibility() == View.VISIBLE) {
 		        	   keyboardView.setVisibility(View.INVISIBLE);
		        	   Log.v(null, "Invisible");
 		           } 
 		           else 
 		           { 
 		        	   keyboardView.setVisibility(View.VISIBLE);
		        	   Log.v(null, "Visible");
		           }
		           Log.v(null, "Fuck");
		           return true;
 			}
 			
       	});
         
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.menu, menu);
         return true;
     }
     
     public boolean onCreateNewFileMenu(Menu menu) {
     	getMenuInflater().inflate(R.menu.new_file, menu);
     	return true;
     }
     
     public void createNewText() {
     //	ProjectTextView newText = new ProjectTextView();
     }
     
     public void inflateLineNumbers(TextView view, int numbers) {
     	String lineNumbers = null;
     	for(int i = 1; i <= numbers; i++) {
     		lineNumbers = lineNumbers + i + "\n";
     		System.out.println(i);
     	}
     	view.setText(lineNumbers);
     }
     
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch(item.getItemId()) {
     	case R.id.new_file:
     	case R.id.open:
     	case R.id.save:
     	case R.id.save_all:
     	case R.id.save_as:
     	case R.id.numbers:
     		if(lineNumbers.isShown()) {
     			lineNumbers.setVisibility(View.INVISIBLE);
     			item.setChecked(false);
     		} else {
     			lineNumbers.setVisibility(View.VISIBLE);
     			item.setChecked(true);
     		}
     	}
     	return true;
     }
     
 }
 
 
