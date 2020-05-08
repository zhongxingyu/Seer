 package edu.pitt.designs1635.ParkIt;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.widget.Button;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.content.DialogInterface;
 import android.content.Intent;
 
 import android.content.DialogInterface.OnShowListener;
 
 import android.widget.ArrayAdapter;
 
 import android.widget.Toast;
 import android.util.Log;
 import android.widget.Spinner;
 
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class Information extends Activity
 {
 	private ParkingLocation pl;
 	private TextView nameValue, typeValue, paymentValue, limitValue, rateValue;
 	private EditText nameValueEdit, limitValueEdit, rateValueEdit;
 	private Spinner typeValueEdit, paymentValueEdit;
 	private Button edit, save;
 	private dbAdapter mDbHelper;
 
     public static final int SAVE_TO_PHONE = 10;
     public static final int SAVE_TO_SERVER = 11;
     public final int SAVE_DIALOG = 1;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.info);
 
 		Bundle extras = getIntent().getExtras();
 
 		pl = extras.getParcelable("edu.pitt.designs1635.ParkIt.location");
 
 		nameValue = (TextView) this.findViewById(R.id.namevalue);
 		nameValueEdit = (EditText) findViewById(R.id.namevalue_edit);
 		typeValue = (TextView) this.findViewById(R.id.typevalue);
 		paymentValue = (TextView) this.findViewById(R.id.paytypevalue);
 		limitValue = (TextView) this.findViewById(R.id.limitvalue);
 		limitValueEdit = (EditText) findViewById(R.id.limitvalue_edit);
 		rateValue = (TextView) this.findViewById(R.id.ratevalue);
 		rateValueEdit = (EditText) findViewById(R.id.ratevalue_edit);
 
 		typeValueEdit = (Spinner) findViewById(R.id.tv_edit);
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
 			this, R.array.park_type, android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		typeValueEdit.setAdapter(adapter);
 
 		paymentValueEdit = (Spinner) findViewById(R.id.ptv_edit);
 		ArrayAdapter<CharSequence> payment_adapter = ArrayAdapter.createFromResource(
 			this, R.array.park_pay_type, android.R.layout.simple_spinner_item);
 		payment_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		paymentValueEdit.setAdapter(payment_adapter);
 
 		nameValue.setText(pl.getName());
 		typeValue.setText(pl.getType().toString());
 		paymentValue.setText(pl.getPayment().toString());
 		if(pl.getLimit() == 0)
 			limitValue.setText("No limit recorded");
 		else
 			limitValue.setText(String.valueOf(pl.getRate()));
 		if(pl.getRate() == 0)
 			rateValue.setText("No rate recorded");
 		else
 			rateValue.setText(String.valueOf(pl.getRate()));
 
 		edit = (Button) findViewById(R.id.edit_button);
 		save = (Button) findViewById(R.id.save_button);
 
 		edit.setOnClickListener(new View.OnClickListener(){
 			public void onClick(View v){
 				nameValue.setVisibility(View.INVISIBLE);
 				nameValueEdit.setVisibility(View.VISIBLE);
 				nameValueEdit.setText(nameValue.getText());
 
 				typeValue.setVisibility(View.INVISIBLE);
 				typeValueEdit.setVisibility(View.VISIBLE);
 				//typeValueEdit.setText(typeValue.getText());
 
 				paymentValue.setVisibility(View.INVISIBLE);
 				paymentValueEdit.setVisibility(View.VISIBLE);
 				//paymentValueEdit.setText(paymentValue.getText());
 
 				limitValue.setVisibility(View.INVISIBLE);
 				limitValueEdit.setVisibility(View.VISIBLE);
 				limitValueEdit.setText(limitValue.getText());
 
 				rateValue.setVisibility(View.INVISIBLE);
 				rateValueEdit.setVisibility(View.VISIBLE);
 				rateValueEdit.setText(rateValue.getText());
 
 				save.setEnabled(true);
 				edit.setEnabled(false);
 			}
 		});
 
 		mDbHelper = new dbAdapter(this);
 		mDbHelper.open();
 
 		save.setOnClickListener(new View.OnClickListener(){
 			public void onClick(View v)
 			{
 				showDialog(SAVE_DIALOG);
 				
 				nameValue.setText(nameValueEdit.getText().toString());
 				nameValue.setVisibility(View.VISIBLE);
 				nameValueEdit.setVisibility(View.INVISIBLE);
 
 				typeValue.setVisibility(View.VISIBLE);
 				typeValueEdit.setVisibility(View.INVISIBLE);
 
 				paymentValue.setVisibility(View.VISIBLE);
 				paymentValueEdit.setVisibility(View.INVISIBLE);
 
 				limitValue.setVisibility(View.VISIBLE);
 				limitValueEdit.setVisibility(View.INVISIBLE);
 
 				rateValue.setVisibility(View.VISIBLE);
 				rateValueEdit.setVisibility(View.INVISIBLE);
 
 				edit.setEnabled(true);
 				save.setEnabled(false);
 				mDbHelper.close();
				finish();
 			}
 		});
 	}
 
 	@Override
     protected void onStop()
     {
 	    super.onStop();
         mDbHelper.close();
     }
 
     @Override
     protected void onPause()
     {
 	    super.onPause();
         mDbHelper.close();
     }
 
     @Override
     protected void onResume()
     {
 	    super.onResume();
         mDbHelper.open();
     }
     
     protected Dialog onCreateDialog(int id, Bundle b)
     {
     	//Toast.makeText(this, "in dialog " + id, Toast.LENGTH_SHORT).show();
 		
     	switch(id)
     	{
     	case SAVE_DIALOG:
     		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	        
 	        builder.setMessage("Do you want to save the changes to your phone only or to the public server?")
 	               .setCancelable(false)
 	               .setPositiveButton("Phone", new DialogInterface.OnClickListener() {
 	                   public void onClick(DialogInterface dialog, int id) {
 	                        saveChanges(SAVE_TO_PHONE);
 	                   }
 	               })
 	               .setNegativeButton("Server", new DialogInterface.OnClickListener() {
 	                   public void onClick(DialogInterface dialog, int id) {
 	                	   saveChanges(SAVE_TO_SERVER);
 	                   }
 	               })
 	               .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
 	                       public void onClick(DialogInterface dialog, int id) {
 	                            dialog.cancel();
 	                       }
 	                       
 	               });
 	        AlertDialog ad = builder.create();
 	       
 	        ad.setOnShowListener(new OnShowListener() {				
 	        	 public void onShow(DialogInterface dialog) {
 	        		 ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
 	        	 }
 	        	});
 	        
 	        return ad;
     	}
 	    
     	return null;
     	
     }
     
     private void saveChanges(int whereToSave)
     {
     	switch(whereToSave)
     	{
     	case SAVE_TO_PHONE:
 							ParkingLocation newPl = new ParkingLocation();
 							newPl.setName(nameValueEdit.getText().toString());
 							newPl.setType(0);
 							newPl.setPayment(0);
 							if(limitValueEdit.getText().toString().compareTo("No limit recorded") != 0)
 							{
 								newPl.setLimit((int)Float.parseFloat(limitValueEdit.getText().toString()));
 								limitValue.setText(limitValueEdit.getText().toString());
 							}
 							if(rateValueEdit.getText().toString().compareTo("No rate recorded") != 0)
 							{
 								newPl.setRate(Float.parseFloat(rateValueEdit.getText().toString()));
 								rateValue.setText(rateValueEdit.getText().toString());
 							}
 							newPl.setLatitude(pl.getLatitude());
 							newPl.setLongitude(pl.getLongitude());
 				
 							if(mDbHelper.deletePoint(pl.getLatitude(), pl.getLongitude()))
 								Log.i("PARKIT INFO", "DELETE SUCCESSFUL");
 							else
 								Log.i("PARKIT INFO", "DELETE FAILUREEEEE");
 				
 							mDbHelper.addPoint(newPl);
     						break;
     	case SAVE_TO_SERVER:
 							break;
     	default:
     						break;
     	}
     }
 }
