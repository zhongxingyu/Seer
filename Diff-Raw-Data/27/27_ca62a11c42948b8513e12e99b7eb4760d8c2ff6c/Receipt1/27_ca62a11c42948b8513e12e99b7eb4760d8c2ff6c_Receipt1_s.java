 package com.example.noq;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.smartmobilesofware.ocrapiservice.OCR1;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.text.InputFilter;
 import android.text.Spanned;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.app.Dialog;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 
 public class Receipt1 extends Activity {
 	
 	String receiptNo = "";
 	EditText amtSpent;
 	EditText receiptNum;
 	EditText shopName;
	private static final int numShops = 12;
 	private static final String[] shops = new String[] {
 		"Bakerzin", "Ben & Jerry's",
 		"Cheers Store",
 		"Daiso", "Desigual",
 		"Golden Village",
 		"Honeymoon Dessert", "Hang Ten", 
 		"Mango", 
 		"Old Chang Kee", 
 		"Prima Deli",
 		"Starbucks Coffee", 
 		"Toys \"R\" Us",
 		"ZARA"
 	};
 	
 	 // Progress Dialog
     private ProgressDialog pDialog;
  
     // JSON parser class
     JSONParser jsonParser = new JSONParser();
  
     // single receipt url
    private static final String url_receipt_details = "http://192.168.1./android_connect/get_receipt_details.php";
  
     // JSON Node names
     private static final String TAG_SUCCESS = "success";
 	private static final String TAG_RECEIPT = "receipt";
     private static final String TAG_RECEIPT_NUM = "receipt_no";
     private static final String TAG_SHOP_NAME = "shop_name";
     private static final String TAG_AMT_SPENT= "amt_spent";
 
     protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		receiptNum = (EditText) findViewById(R.id.vehnumber);
 		shopName = (EditText) findViewById(R.id.shopName);
 		amtSpent = (EditText) findViewById(R.id.amtSpent);
 		amtSpent.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(2)});	// limits input of amount spent
 		
 		Button ocrButton = (Button) findViewById(R.id.ocrButton);	
 		ocrButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				Intent intent = new Intent(Receipt1.this, OCR1.class); // Going to OCR page
 				startActivity(intent);
 			}
 		});
 		
 		// autocomplete for the shop name
 		AutoCompleteTextView autocompShops = (AutoCompleteTextView) findViewById(R.id.shopName);
 		autocompShops.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, shops));
 		autocompShops.setDropDownHeight(200);
 		autocompShops.performCompletion();
 		
 		// to click on clear all button
 		clearAll();
 	 
 		// to click on submit button
         Button submit = (Button) findViewById(R.id.button2);			    		
         submit.setOnClickListener(new OnClickListener() {											
 			@Override
 			public void onClick(View arg0) {
 				if (validateNum(receiptNum.getText().toString())){
 					receiptNum.setError(null);
 					if (validateShop(shopName.getText().toString())){
 						shopName.setError(null);
 						// Brings up dialog to ask if want to submit
 						AlertDialog.Builder toContinue = new AlertDialog.Builder(Receipt1.this);
 						toContinue.setTitle("Confirm submission");
 						toContinue.setMessage("Submit Now?");
 						toContinue.setPositiveButton("Submit", new DialogInterface.OnClickListener(){
 							// Changes edittext to textview, hides and disables buttons, then continues to submit
 							public void onClick(DialogInterface cont, int id){
 								EditText et = (EditText) findViewById(R.id.amtSpent);
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
 				if (amtSpent.getText().length() == 0) {
 					amtSpent.setError("Required field cannot be left blank");
 				}
 				if (shopName.getText().length() == 0) {
 					shopName.setError("Required field cannot be left blank");
 				}
 				if (receiptNum.getText().length() == 0) {
 					receiptNum.setError("Required field cannot be left blank");
 				}
 			}		
 		}); 
 	}// end of onCreate()			
 	
 	//Override of hardware back button
 	@Override 
 	public void onBackPressed(){
 		Intent returnIntent = new Intent(Receipt1.this, Receipts.class);
 		returnIntent.putExtra("isValid1", true);
 		returnIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); 
 		// bring an existing instance of the called activity type present in the current stack to the 
 		// foreground instead of creating a new instance
 		setResult(RESULT_OK,returnIntent);   
 		startActivity(returnIntent);
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
 	
 	// To make sure getIntent() always return the most recent intent
     protected void onNewIntent(Intent intent) {
         super.onNewIntent(intent);
         setIntent(intent);
     }
 
     // Activity being restarted from stopped state    
 	@SuppressLint("NewApi")
 	protected void onRestart() {
 	    super.onRestart();  // Always call the superclass method first
 	    onNewIntent(getIntent());
 	    
 	    if(getIntent().getBooleanExtra("ocrDone", false)) {
 		    String text = getIntent().getCharSequenceExtra("text").toString();
 	    	
 	    	// if gst num cannot be found
 		    if (!text.contains("GST No:")) {
 		    	receiptNum.setError("Please fill in manually.");
 		    	shopName.setError("Please fill in manually.");
 		    	amtSpent.setError("Please fill in manually.");
 		    }
 		    else {  // if gst num found
 		    	int startIndex = text.indexOf("GST No:")+8;
 		    	CharSequence receiptCode = text.subSequence(startIndex, startIndex+12);
 		    	receiptNum.setText(receiptCode);
 		    	receiptNo = receiptCode.toString();
 		    	
 		        // Getting complete receipt details in background thread
 		        new GetReceiptDetails().execute();
 		    }
 	    }
 	}
 	
 	/**
 	  * Background Async Task to Get complete product details
 	  * */
 	 class GetReceiptDetails extends AsyncTask<String, String, String> {
 		 private JSONObject receipt;
 		 private int success;
 	     /**
 	      * Before starting background thread Show Progress Dialog
 	      * */
 	     @Override
 	     protected void onPreExecute() {
 	         super.onPreExecute();
 	         pDialog = new ProgressDialog(Receipt1.this);
 	         pDialog.setMessage("Loading receipt details. Please wait...");
 	         pDialog.setIndeterminate(false);
 	         pDialog.setCancelable(true);
 	         pDialog.show();
 	     }
 
 	     /**
 	      * Getting receipt details in background thread
 	      * */
 	     protected String doInBackground(String... params) {
 	    	// Building Parameters
             List<NameValuePair> params1 = new ArrayList<NameValuePair>();
             params1.add(new BasicNameValuePair("receipt_no", receiptNo)); 
             
           	// getting receipt details by making HTTP request
             JSONObject json = jsonParser.makeHttpRequest(url_receipt_details, "GET", params1);
 
             // check your log for json response
             Log.d("Single Receipt Details", json.toString());
             
             try {
             	// json success tag
                 success = json.getInt(TAG_SUCCESS);
                 if (success == 1) {
                     // successfully received product details
                     JSONArray receiptObj = json.getJSONArray(TAG_RECEIPT); // JSON Array
                     
 					// get first receipt object from JSON Array
 					receipt = receiptObj.getJSONObject(0);
                  }
                  else {
                     return null;
         	    }
              } catch (JSONException e) {
                  e.printStackTrace();
              }
 	         return null;
 	     }
 
 	     /**
 	      * After completing background task Dismiss the progress dialog
 	      * **/
 	     protected void onPostExecute(String file_url) {
 	        // dismiss the dialog once got all details
 	        pDialog.dismiss();
 	        try {
 	        	if (success == 1){
 				receiptNum.setText(receipt.getString(TAG_RECEIPT_NUM));
 				shopName.setText(receipt.getString(TAG_SHOP_NAME));
 				amtSpent.setText(receipt.getString(TAG_AMT_SPENT));
 	        	}
 	        	else {
 	        	// product with receiptNo not found
 	        	receiptNum.setError("Please fill in manually.");
     	    	shopName.setError("Please fill in manually.");
     	    	amtSpent.setError("Please fill in manually.");
 	        	}
 			} catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	     }
 	}
 	 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	// returns true if receipt number is valid
 	// TODO: check for the hyphen at char 2 and 10
 	private boolean validateNum(String receiptNum){
 		char[] CreceiptNum = receiptNum.toCharArray();
 	
 		if (CreceiptNum.length != 12)
 			return false;
 		if (!Character.isLetter(CreceiptNum[11])) 
 			return false;
 		if (!Character.isDigit(CreceiptNum[0]) || !Character.isDigit(CreceiptNum[1]) || !Character.isDigit(CreceiptNum[3]) || !Character.isDigit(CreceiptNum[4])
 			|| !Character.isDigit(CreceiptNum[5]) || !Character.isDigit(CreceiptNum[6]) || !Character.isDigit(CreceiptNum[7])
 				|| !Character.isDigit(CreceiptNum[8]) || !Character.isDigit(CreceiptNum[9]))
 			return false;
 		return true;
 	}
 	
 	// returns true if shop is valid
 	// TO DO: make a database of shops and enhance searching
 	private boolean validateShop(String SshopName){
 		for (int i=0; i<numShops; i++){
 			if (SshopName.equals(shops[i])){
 				return true;
 			}
 		}	
 		return false;
 	}
 	
 	// Converts all editable text fields to textview only
 	private void Fixtext(){
 		receiptNum.setEnabled(false);
 		receiptNum.setFocusable(false);
 		receiptNum.setFocusableInTouchMode(false);
 		receiptNum.setClickable(false);
 		shopName.setEnabled(false);
 		shopName.setFocusable(false);
 		shopName.setFocusableInTouchMode(false);
 		shopName.setClickable(false);
 		amtSpent.setEnabled(false);
 		amtSpent.setFocusable(false);
 		amtSpent.setFocusableInTouchMode(false);
 		amtSpent.setClickable(false);
 	}
 	
 	// Disables buttons
 	private void Disablebuttons(){
 		Button clear = (Button) findViewById(R.id.button1);
 		clear.setVisibility(View.INVISIBLE);
 		clear.setEnabled(false);
 		Button submit = (Button) findViewById(R.id.button2);
 		submit.setVisibility(View.INVISIBLE);
 		submit.setEnabled(false);
 	}
 	
 	private void clearAll() {
 		Button b1 = (Button) findViewById(R.id.button1);
 		b1.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				receiptNum.setText("");
 				shopName.setText("");
 				amtSpent.setText("");
 			}
 		});	
 	
 	}
 
 	public class DecimalDigitsInputFilter implements InputFilter {
 		  private final int decimalDigits;
 		  /**Constructor.
 		   * @param decimalDigits maximum decimal digits
 		   */
 		  public DecimalDigitsInputFilter(int decimalDigits) {
 		    this.decimalDigits = decimalDigits;
 		  }
 		  @Override
 		  public CharSequence filter(CharSequence source,
 		      int start,
 		      int end,
 		      Spanned dest,
 		      int dstart,
 		      int dend) {
 		    int dotPos = -1;
 		    int len = dest.length();
 		    for (int i = 0; i < len; i++) {
 		      char c = dest.charAt(i);
 		      if (c == '.' || c == ',') {
 		        dotPos = i;
 		        break;
 		      }
 		    }
 		    if (dotPos >= 0) {
 		      // protects against many dots
 		      if (source.equals(".") || source.equals(","))
 		      {
 		          return "";
 		      }
 		      // if the text is entered before the dot
 		      if (dend <= dotPos) {
 		        return null;
 		      }
 		      if (len - dotPos > decimalDigits) {
 		        return "";
 		      }
 		    }
 		    return null;
 		  }
 		}
 }
