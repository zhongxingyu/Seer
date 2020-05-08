 package com.agilevent.colorssample;
 
 

 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ColorsActivity extends Activity {
    
 	protected ColorsFragment cf; 
 	protected ColorDetailFragment cdf;
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.main);
         
         // 1. Get ColorsFragment
         // 2. Get ColorDetailFragment
         // 3. Set the listener of the ColorFragment to that of the ColorFragment
         cf = (ColorsFragment)getFragmentManager().findFragmentById(R.id.colors);
         cdf = (ColorDetailFragment)getFragmentManager().findFragmentById(R.id.details); 
         
         // Let the ColorsFragment know who to notify when the color changes. 
        cf.setColorChangedListener(cdf); 
         
     }
     
     @Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		new MenuInflater(this).inflate(R.menu.color_menu, menu);
 		
 		EditText add=(EditText)menu.findItem(R.id.add)
 			.getActionView().findViewById(R.id.color);
 		
 		add.setOnEditorActionListener(onAdd);
 
 		return(super.onCreateOptionsMenu(menu));
 	}
     
     @Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		
 		
 			case R.id.reset:
 				cf.reset(); 
 				return(true);
 
 			case R.id.about:				
 			case android.R.id.home:
 				Toast.makeText(this,"Color Sample App. Uses Fragments and the Action Bar",Toast.LENGTH_LONG).show();
 				return(true);
 		}
 		
 		return(super.onOptionsItemSelected(item));
 	}
 
     
 	@SuppressWarnings("unchecked")
 	private void addColor(String colorWithCode) {
		ArrayAdapter<SimpleColor> adapter = (ArrayAdapter<SimpleColor>) cf.getListAdapter();
		
		String colorName = colorWithCode.substring(0, colorWithCode.indexOf(':'));
		String colorHex = colorWithCode.substring(colorWithCode.indexOf(':') + 1, colorWithCode.length());
	
		
		adapter.add(new SimpleColor(colorName, Color.parseColor(colorHex)));
 	}
     
     
     private TextView.OnEditorActionListener onAdd=
 		new TextView.OnEditorActionListener() {
 		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 			if (event==null || event.getAction()==KeyEvent.ACTION_UP) {
 				
 				addColor(v.getText().toString());
 				
 				// Clear it out now
 				v.setText("");
 				
 				InputMethodManager imm=(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
 
 				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
 			}
 			
 			return(true);
 		}
 	};
 	
     
     
    
     
    
 }
