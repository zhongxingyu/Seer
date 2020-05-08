 package com.ctg.ctginventory;
 
 
 
 
 import android.app.Fragment;
 import android.content.Intent;
 import android.widget.TextView;
 
 public class Scan extends Fragment {  
 	
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
 		    if (scanResult != null) {
 		    	String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                 // Handle successful scan
                 TextView assetName = (TextView) getActivity().findViewById(R.id.menu_settings);
                 assetName.setText(contents);
              } else  {
                 // Handle cancel
              }
 		         
 		      
 		    }
 		    // else continue with any other code you need in the method
 		    
 		  }
 
 
