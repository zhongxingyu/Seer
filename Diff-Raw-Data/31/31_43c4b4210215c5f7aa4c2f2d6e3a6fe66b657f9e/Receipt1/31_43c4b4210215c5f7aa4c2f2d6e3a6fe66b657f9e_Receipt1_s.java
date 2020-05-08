 package com.example.noq;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.app.Dialog;
 import android.app.AlertDialog.Builder;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 
 
 // This is Receipt1 page
 public class Receipt1 extends Activity {
 	
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		// to click on clear all button
 		Button b1 = (Button) findViewById(R.id.button1);
 			b1.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View arg0) {
 					EditText et1 = (EditText) findViewById(R.id.editText1);
 					et1.setText("");
 					EditText et2 = (EditText) findViewById(R.id.editText2);
 					et2.setText("");
 					EditText et3 = (EditText) findViewById(R.id.editText3);
 					et3.setText("");
 				}
 			});
 	       
 			// to click on submit button
 			EditText et = (EditText) findViewById(R.id.editText3);
 	        Button b2 = (Button) findViewById(R.id.button2);			
 			
 	        b2.setOnClickListener(new OnClickListener() {											
 				@Override
 				public void onClick(View arg0) {
 					EditText receiptNum = (EditText) findViewById(R.id.editText1);
 					EditText shopName = (EditText) findViewById(R.id.editText2);
 					if (validateNum(receiptNum.getText().toString())){
 						receiptNum.setError(null);
 						if (validateShop(shopName.getText().toString())){
 							shopName.setError(null);
 							// Brings up dialog to ask if want to submit
 							AlertDialog.Builder toContinue = new AlertDialog.Builder(Receipt1.this);
 							toContinue.setTitle("Confirm submission");
							toContinue.setMessage("You may not edit once you submit!");
 							toContinue.setPositiveButton("Submit", new DialogInterface.OnClickListener(){
 								// Changes edittext to textview, hides and disables buttons, then continues to submit
 								public void onClick(DialogInterface cont, int id){
 									EditText et = (EditText) findViewById(R.id.editText3);
 									Fixtext();
 									Disablebuttons();
 									Intent returnIntent = new Intent(Receipt1.this, Receipts.class); // Going back to Receipts
 									returnIntent.putExtra("amount", Double.parseDouble(et.getText().toString()));
 									returnIntent.putExtra("isValid1", true);
 									returnIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); 
 									// bring an existing instance of the called activity type present in the current stack to the 
 									// foreground instead of creating a new instance
 									setResult(RESULT_OK,returnIntent);   
 									startActivity(returnIntent);
 								}
 							})
 							.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
 								// Cancel and go back to Receipts1 page
 								public void onClick(DialogInterface cancel, int id){
 									if (id == Dialog.BUTTON_NEGATIVE)
 										cancel.dismiss();
 								}
 							});
 							toContinue.show();
 						}
 						else{
 							shopName.setError("Invalid shop");
 						}
 					}
 					else{
 						receiptNum.setError("Invalid receipt code");
 					}
 				}		
 			}); 
 	}// end of onCreate()			
 	
 	@Override //if (hardware) back button is pressed and no data is returned to Receipts
 	public void onBackPressed(){
 		Intent returnIntent = new Intent(Receipt1.this, Receipts.class);
 		returnIntent.putExtra("isValid1", true);
 		returnIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); 
 		// bring an existing instance of the called activity type present in the current stack to the 
 		// foreground instead of creating a new instance
 		setResult(RESULT_OK,returnIntent);   
 		startActivity(returnIntent);
 		
 	//	setResult(RESULT_CANCELED, returnIntent);        
 	//	finish();
 	} 
 	
 	// activity is partially visible
 	public void onPause() {
 	    super.onPause();  // Always call the superclass method first    
 	} // end of onPause    
 	
 	// called by system when first creating activity as well as when resuming from 'paused' state
 	public void onResume() {
 	    super.onResume();  // Always call the superclass method first
 	}// end of onResume
 	
 	// called by system when second activity is created
 	protected void onStop() {
 	    super.onStop();  // Always call the superclass method first	    
 	} // end of onStop
 	
 	// Activity being restarted from stopped state    
 	protected void onRestart() {
 	    super.onRestart();  // Always call the superclass method first
 	    onNewIntent(getIntent());
 		
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	// returns true if receipt number is valid
 	// right now assume that the first character must be a letter 
 	// followed by 3 numbers for receipt to be valid
 	// TO DO: research on security, hashing, etc.
 	private boolean validateNum(String receiptNum){
 		char[] CreceiptNum = receiptNum.toCharArray();
 		
 		if (CreceiptNum.length != 4)
 			return false;
 		if (!Character.isLetter(CreceiptNum[0])) 
 			return false;
 		if (!Character.isDigit(CreceiptNum[1]) || 
 			!Character.isDigit(CreceiptNum[2]) || !Character.isDigit(CreceiptNum[3]))
 			return false;
 		return true;
 	}
 	
 	// returns true if shop is valid
 	// right now assume only 3 shops called abc, def and ghi
 	// TO DO: make a database of shops, ignore case and enhance searching
 	private boolean validateShop(String SshopName){
 		int numShops = 3;
 		String[] shops = {"abc", "def", "ghi"};
 
 		for (int i=0; i<numShops; i++){
 			if (SshopName.equals(shops[i])){
 				return true;
 			}
 		}	
 		return false;
 	}
 	
 	private void Fixtext(){
 		EditText et1 = (EditText) findViewById(R.id.editText1);
 		et1.setEnabled(false);
 		et1.setFocusable(false);
 		et1.setFocusableInTouchMode(false);
 		et1.setClickable(false);
 		EditText et2 = (EditText) findViewById(R.id.editText2);
 		et2.setEnabled(false);
 		et2.setFocusable(false);
 		et2.setFocusableInTouchMode(false);
 		et2.setClickable(false);
 		EditText et3 = (EditText) findViewById(R.id.editText3);	
 		et3.setEnabled(false);
 		et3.setFocusable(false);
 		et3.setFocusableInTouchMode(false);
 		et3.setClickable(false);
 	}
 	
 	private void Disablebuttons(){
 		Button clear = (Button) findViewById(R.id.button1);
 		clear.setVisibility(View.INVISIBLE);
 		clear.setEnabled(false);
 		Button submit = (Button) findViewById(R.id.button2);
 		submit.setVisibility(View.INVISIBLE);
 		submit.setEnabled(false);
 	}
 }
