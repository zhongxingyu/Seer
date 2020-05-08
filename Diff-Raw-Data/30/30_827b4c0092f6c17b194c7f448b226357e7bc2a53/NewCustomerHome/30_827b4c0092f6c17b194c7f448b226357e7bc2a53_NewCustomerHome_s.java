 package com.andriod.tailorassist;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class NewCustomerHome extends Activity {
 	
 	boolean edit;
 	Context Ctxt;
 	long custId;
 	String custName ;
 	String custMobile;
 	String custAddress ;
 	
 	private void initValues(Bundle savedInstanceState){
 		if(savedInstanceState!= null && savedInstanceState.get("custId") != null){
 			custId =  savedInstanceState.getLong("custId");
 		}
 	}
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	initValues(savedInstanceState);
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_new_customer_home);    
         getActionBar().setHomeButtonEnabled(true);
         Ctxt= this;
         Button btnSAddMeasurement = (Button) findViewById(R.id.button_addMeasurement);
         
         btnSAddMeasurement.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 
 			
 			EditText Name = (EditText) findViewById(R.id.editText_name);
 			EditText Mobile = (EditText) findViewById(R.id.editText_mobileNumber);
 			EditText Address = (EditText) findViewById(R.id.editText_address);
 			String lcustName = Name.getText().toString().trim();			
 			String lcustMobile = Mobile.getText().toString().trim();			
 			String lcustAddress = Address.getText().toString().trim();
 			boolean valid = true;
 			if((lcustName.length()== 0) && (lcustMobile.length()== 0) ){
 				valid = false;
 				Toast.makeText(Ctxt, R.string.alertTxt_NewCustomer_Name_mobile_empty, Toast.LENGTH_LONG).show();
 			}
 			if(lcustMobile.length()> 0 && !Util.isValidMobileNumber(lcustMobile)){
 				valid = false;
 				Toast.makeText(Ctxt, R.string.alertTxt_NewCustomer_invalid_mobile, Toast.LENGTH_LONG).show();
 			}
 
 			if(valid){
 			
 //				Intent intent = new Intent(Ctxt,AddMeasurement.class );
 				Intent intent = new Intent(Ctxt,MeasurementsEntryActivity.class );
 				/* sending the customer details to next activity 			 */
 				Bundle bundle = new Bundle();
 				
 			
 				bundle.putString("previous.screen", "profile.edit");
 				if(edit){
 					bundle.putString("mode", "edit");
 					Log.d("", "going to edit measurment");
					if(!isChangesSaved()){
						custName = Name.getText().toString().trim();			
						custMobile = Mobile.getText().toString().trim();			
						custAddress = Address.getText().toString().trim();
						bundle.putString("changes", "preserve");
						Log.d("btnSAddMeasurement.setOnClickListener", "going to edit measurment with preserve profile edit");
					}
					bundle.putString("custName", custName);
					bundle.putString("custMobile", custMobile);
					bundle.putString("custAddress", custAddress);		
 					bundle.putLong("custId", custId);
 				}
 				intent.putExtras(bundle);			
 				
 				//start the activity			
 				Ctxt.startActivity(intent);
 			}			
 			
 		}
 	});
         
         com.andriod.tailorassist.screen.Styles.setActionBarStyle(getActionBar(), getResources());
         //TODO render with current values
         /*
          * use below keys and render values accordingly and enable save buttun if custId>0
          bundle.putLong("custId", custId);
 			bundle.putString("custName", custName);
 			bundle.putString("custMobile", custMobile);
 			bundle.putString("custAddress", custAddress);
 			bundle.putString("mode", "edit");
          * */
         Bundle bundle = this.getIntent().getExtras();
         if(bundle != null && "edit".equals(bundle.getString("mode"))){
         	
         	edit = true;
         	custId = bundle.getLong("custId");
         	
         	EditText Name = (EditText) findViewById(R.id.editText_name);
 			EditText Mobile = (EditText) findViewById(R.id.editText_mobileNumber);
 			EditText Address = (EditText) findViewById(R.id.editText_address);
 			Name.setText(bundle.getString("custName"));
 			Mobile.setText(bundle.getString("custMobile"));
 			Address.setText(bundle.getString("custAddress"));
         }else{
         	edit = false;
         }
    }
     public boolean onCreateOptionsMenu(Menu menu) {
     	if(edit){
     		getMenuInflater().inflate(R.menu.customer_edit_action_menu, menu);
     	}
         return true;
     }
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
         case R.id.save_btn:
         	
         	return save();      
         case android.R.id.home:
         	if(isChangesSaved()){
 	           return goHome();
         	}else{
         		unSavedAlert().show();
         		
         	}
         	return false;
            
 
         default:
             return super.onOptionsItemSelected(item);}
         }
     public boolean save(){
     	if(isChangesSaved())
     		return true;
     	if(updateCustomerProfile()){				
     		Bundle details = this.getIntent().getExtras();
         	details.putLong("custId", custId);
 				
 			//Show the next layout view
 			Intent intent = new Intent(Ctxt,ShowCustomerDetails.class);				
 			/* sending the customer details to next activity 			 */
 			Bundle bundle = new Bundle();			
 			bundle.putString("newCustmerNumber", ""+custId);				
 			intent.putExtras(bundle);				
 			//start the activity				
 			Ctxt.startActivity(intent);
 			return true;
 		}
     	return false;
     }
     private boolean goHome(){
     	 Intent intent = new Intent(this,Matrix.class);
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          startActivity(intent);
          return true;
     }
     private boolean updateCustomerProfile(){
     	Log.d("updateCustomerProfile", " updating custId "+custId);
     	if(custId>0){
     		EditText Name = (EditText) findViewById(R.id.editText_name);
 			EditText Mobile = (EditText) findViewById(R.id.editText_mobileNumber);
 			EditText Address = (EditText) findViewById(R.id.editText_address);
 			custName = Name.getText().toString().trim();			
 			custMobile = Mobile.getText().toString().trim();			
 			custAddress = Address.getText().toString().trim();
     		CustomerTable custTable = new CustomerTable(this);
         	custTable.open();
     		custTable.updateCustomerProfile(custId, custName, custMobile, custAddress);   	
     		custTable.close();
         	return true;
     	}else
     		return false;
     	
     }
     public boolean isChangesSaved(){
     	boolean changesSaved = true;
 
 		if(Util.isModified((EditText)findViewById(R.id.editText_name), custName) ||
 				Util.isModified((EditText)findViewById(R.id.editText_mobileNumber), custMobile) ||
 				Util.isModified((EditText)findViewById(R.id.editText_address), custAddress)){
 			changesSaved = false;
 		}
 		Log.d("CustomerDetail  ", "change saved "+changesSaved);
     	return changesSaved;
     }
     
     public Dialog unSavedAlert(){
     	
     	
     	
     	
     	AlertDialog.Builder builder = new AlertDialog.Builder(Ctxt);
 		builder.setMessage("Customer details have been modified. Save changes?");
 		builder.setInverseBackgroundForced(true);
 		builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 				save();
 				goHome();
 			}		
 		});
 				
 		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int which) {
 				
 				dialog.dismiss();
 				goHome();
 			}
 		});
 		builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int which) {
 				
 				dialog.dismiss();
 			}
 		});
 		return builder.create();
     }
     public void onBackPressed() {
 //    	if(custId <=0){
 //    		super.onBackPressed();
 //    	}
 //    	else 
     		if(isChangesSaved()){
 //    			super.onBackPressed();
 //    			 Bundle bundle = this.getIntent().getExtras();
 // 		        if(bundle != null && "measurment.edit".equals(bundle.getString("previous.screen"))){
 // 		        	
 // 		        } else {
  		        	goHome();
 // 		        }
      	}else{
      		unSavedAlert().show();
      		
      	}
 	 }
 }
