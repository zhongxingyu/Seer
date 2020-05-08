 package com.example.noq;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import java.text.DateFormat;
 import java.util.Date;
 
 public class Receipts extends Activity {
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.receipts);
 		
 		// shows the current date
 		TextView date = (TextView) findViewById(R.id.date);
 		date.setText(DateFormat.getDateInstance().format(new Date()));
 	
 		// clicking on receipt1 button 
 		Button b1 = (Button) findViewById(R.id.button1);
 		b1.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// clicking again to view info on Receipt 1
 				if (getIntent().getBooleanExtra("isValid1", false)) {
 					Intent viewIntent = new Intent(Receipts.this, Receipt1.class);
 					viewIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 					startActivity(viewIntent);
 				}
 				// first time clicking to key in Receipt 1
 				else{
 					Intent intent = new Intent(Receipts.this, Receipt1.class); // Going to Receipt1
 					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 					startActivityForResult(intent, 1);
 				}	
 			}
 		});
 		
 		// clicking on receipt2 button 
 		Button b2 = (Button) findViewById(R.id.button2);
 		b2.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
				// clicking again to view info on Receipt 2
				if (getIntent().getBooleanExtra("isValid2", false)) {
					Intent viewIntent = new Intent(Receipts.this, Receipt2.class);
					viewIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(viewIntent);
				}
				// first time clicking to key in Receipt 2
				else{
					Intent intent = new Intent(Receipts.this, Receipt2.class); // Going to Receipt2
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivityForResult(intent, 1);
				}	
 			}
 		});
 		
 		// clicking on receipt3 button 
 		Button b3 = (Button) findViewById(R.id.button3);
 		b3.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
				// clicking again to view info on Receipt 3
				if (getIntent().getBooleanExtra("isValid3", false)) {
					Intent viewIntent = new Intent(Receipts.this, Receipt3.class);
					viewIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(viewIntent);
				}
				// first time clicking to key in Receipt 3
				else{
					Intent intent = new Intent(Receipts.this, Receipt3.class); // Going to Receipt3
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivityForResult(intent, 1);
				}	
 			}
 		});
 		
 	} // end of onCreate()
 	
 /*	//Override of hardware back button
 	@Override 
 	public void onBackPressed(){
 		// do nothing
 	} 
 */	
 	// activity is partially visible
 	public void onPause() {
 	    super.onPause();  // Always call the superclass method first
 	    
 	} // end of onPause    
 		
 	// called by system when first creating activity as well as when resuming from 'paused' state
 	public void onResume() {
 	    super.onResume();  // Always call the superclass method first
 	}// end of onResume
 	
 	// called by system when another activity is created
 	protected void onStop() {
 	    super.onStop();  // Always call the superclass method first
 	} // end of onStop
 	
 	// To make sure getIntent() always return the most recent intent
     protected void onNewIntent(Intent intent) {
         super.onNewIntent(intent);
         setIntent(intent);
     }
 	
 	// Activity being restarted from stopped state    
 	protected void onRestart() {
 	    super.onRestart();  // Always call the superclass method first
 	    onNewIntent(getIntent());
 	    
 	    TextView tv1 = (TextView) findViewById(R.id.textView6);
 	    double oldAmt = Double.parseDouble(tv1.getText().toString());
 		double newAmt = updateAmt(getIntent());
 	    
 	    // inserts tick1 image if a valid receipt was entered  
 	 	if (getIntent().getBooleanExtra("isValid1", false) && oldAmt != newAmt) {
 	 		ImageView tick1 = (ImageView) findViewById(R.id.imageView4);
 	 		tick1.setVisibility(View.VISIBLE);
 	 		tick1.setImageResource(R.drawable.tick1);
 	 	}
 	 	if (getIntent().getBooleanExtra("isValid2", false) && oldAmt != newAmt) {
 	 		ImageView tick2 = (ImageView) findViewById(R.id.imageView5);
 	 		tick2.setVisibility(View.VISIBLE);
 	 		tick2.setImageResource(R.drawable.tick1);
 	 	}
 	 	if (getIntent().getBooleanExtra("isValid3", false) && oldAmt != newAmt) {
 	 		ImageView tick3 = (ImageView) findViewById(R.id.imageView6);
 	 		tick3.setVisibility(View.VISIBLE);
 	 		tick3.setImageResource(R.drawable.tick1);
 	 	}		
 	 	
 	 	final Button redeem = (Button) findViewById(R.id.button4);
 	 	if (canRedeem()) {
 	 		redeem.setOnClickListener(new OnClickListener() {
 	 			@Override
 				public void onClick(View v) {
 	 				redeem.setVisibility(View.INVISIBLE);
 	 				// Supposedly enters some extra receipt validation
 					// Brings up dialog 
 					AlertDialog.Builder redeem = new AlertDialog.Builder(Receipts.this);
 					redeem.setMessage("Validation successful!");
 					redeem.setPositiveButton("Back to Home", new DialogInterface.OnClickListener(){
 						public void onClick(DialogInterface r, int id){
 							Intent goHome = new Intent(Receipts.this, HomePg.class);
 							goHome.putExtra("claimToday", true);
 							goHome.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
 							setResult(RESULT_OK, goHome);
 							startActivity(goHome);
 						}
 					});
 					redeem.show();
 	 			}
 	 		});
 	 	}
 		else{
 			redeem.setClickable(false);
 		}
 	}
 	
 	// To check if the total amount spent has exceeded $100
 	// If yes, then change button to "Redeem Now" and green
 	private boolean canRedeem() {
 	 	TextView tv1 = (TextView) findViewById(R.id.textView6);
 	 	Button b4 = (Button) findViewById(R.id.button4);
 	 	if (Double.parseDouble(tv1.getText().toString()) >= 100.00) {	
 	 		b4.setText("Redeem Now!");
 	 		b4.setClickable(true);
 	 		b4.setBackgroundColor(0xFF1BE04C);
 	 		return true;
 	 	}
 	 	return false;
 	}
 	
 	// To update the total spent
 	private double updateAmt(Intent data) {
 		TextView tv = (TextView) findViewById(R.id.textView6);
 		double newAmt = data.getDoubleExtra("amount", 0.00);
 		double amt = Double.parseDouble(tv.getText().toString());
 		amt += newAmt;
 		tv.setText(String.valueOf(amt));
 		return amt;
 	}
 	
 }
