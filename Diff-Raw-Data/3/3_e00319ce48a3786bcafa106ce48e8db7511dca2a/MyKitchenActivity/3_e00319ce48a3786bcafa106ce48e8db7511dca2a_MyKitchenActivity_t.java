 package com.example.mykitchen;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.content.Intent;
 import android.os.Build;
 import android.app.*;
 import android.view.View;
 import android.content.DialogInterface;
 import android.widget.*;
 import android.content.Context;
 import android.view.View.OnClickListener;
 
 public class MyKitchenActivity extends Activity {
 	final Context context = this;
 	private Activity activity = this;
	ScrollView scrollview = (ScrollView)findViewById(R.id.sv1);
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_my_kitchen);
 		// Show the Up button in the action bar.
 		setupActionBar();
 		Intent intent = getIntent();
 		Button button = (Button)findViewById(R.id.buttonAdd);
 		button.setOnClickListener(new OnClickListener(){
 			public void onClick(View view){
 			    final Dialog dialog = new Dialog(context);
 			    dialog.setContentView(R.layout.dialogue);
 			    dialog.setTitle("Title...");
 			    TextView text1 = (TextView) dialog.findViewById(R.id.tv1);
 			    text1.setText("Ingredient:");
 			    final EditText editI = (EditText) dialog.findViewById(R.id.etI);
 			    editI.setText("");
 			    TextView text2 = (TextView) dialog.findViewById(R.id.tv2);
 			    text2.setText("Quantity:");
 			    final EditText editQ = (EditText) dialog.findViewById(R.id.etq);
 			    editQ.setText("");
 			    Button dButton = (Button)dialog.findViewById(R.id.buttonOk);
 			    dButton.setOnClickListener(new OnClickListener(){
 			    	public void onClick(View v){
 						String ingredient = editI.getText().toString();
 						TextView viewText1 = new TextView(context);
 						viewText1.setText("Ingredient:" + ingredient);
 						String quantity = editQ.getText().toString();
 						TextView viewText2 = new TextView(context);
 						viewText1.setText("#:" + quantity);
 						
 						LinearLayout lila = new LinearLayout(context);
 						lila.setOrientation(LinearLayout.HORIZONTAL);
 						lila.addView(viewText1);
 						lila.addView(viewText2);
 					    scrollview.addView(lila);
 			    		dialog.dismiss();
 			    	}
 			    	;
 			    });
 			    dialog.show();
 			    
 			}
 		});
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void setupActionBar() {
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.my_kitchen, menu);
 		return true;
 	}
 	
 	public void addToScroll(){
 	    final Dialog dialog = new Dialog(context);
 	    dialog.setContentView(R.layout.dialogue);
 	    dialog.setTitle("Title...");
 	    TextView text1 = (TextView) dialog.findViewById(R.id.tv1);
 	    text1.setText("Ingredient:");
 	    EditText editI = (EditText) dialog.findViewById(R.id.etI);
 	    editI.setText("Yo nig");
 	    TextView text2 = (TextView) dialog.findViewById(R.id.tv2);
 	    text2.setText("Quantity:");
 	    EditText editQ = (EditText) dialog.findViewById(R.id.etq);
 	    editQ.setText("yoyonig");
 	    Button dButton = (Button)dialog.findViewById(R.id.buttonOk);
 	    dButton.setOnClickListener(new OnClickListener(){
 	    	public void onClick(View v){
 	    		dialog.dismiss();
 	    	}
 	    });
 	    
 	    dialog.show();
 	    
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 }
