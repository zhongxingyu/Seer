 package com.app.getconnected.activities;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AutoCompleteTextView;
 
 import com.app.getconnected.R;
 import com.app.getconnected.network.PlacesAutoCompleteAdapter;
 
 public class LocationSelectorActivity extends BaseActivity implements
 		OnItemClickListener {
 
 	private AutoCompleteTextView autoCompView;
 	private String type;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_location_selector);
 		initLayout(R.string.title_activity_location_selector, true, true, true,
 				true);
 
 		type = getIntent().getExtras().getString("type");
 
 		autoCompView = (AutoCompleteTextView) findViewById(R.id.location_selector_input);
 		autoCompView.setHint(type);
 
 		autoCompView.setAdapter(new PlacesAutoCompleteAdapter(this,
 				R.layout.places_list_item));
 		autoCompView.setOnItemClickListener(this);
 		
 		autoCompView.setOnKeyListener(new OnKeyListener() {
 			@Override
 			public boolean onKey(View view, int keyCode, KeyEvent event) {
 		        if (event.getAction()!=KeyEvent.ACTION_DOWN)
 		            return false;
 		        if(keyCode == KeyEvent.KEYCODE_ENTER ){
 		            selectLocation(view, autoCompView.getText().toString());
 		            return true;
 		        }
 		        return false;
 			}
 		});
 		
 		buttonOk.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				selectLocation(v, autoCompView.getText().toString());
 			}
 		});
 		
 		autoCompView.requestFocus();
 	}
 
 	protected void selectLocation(View v, String location) {
 		Intent resultIntent = new Intent();
 		resultIntent.putExtra("location", location);
 		resultIntent.putExtra("type", type);
 		setResult(Activity.RESULT_OK, resultIntent);
 		finish();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.location_selector, menu);
 		return true;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> adapterView, View view,
 			int position, long id) {
 		String location = (String) adapterView.getItemAtPosition(position);
 		
 		selectLocation(view, location);
 	}
 
 }
