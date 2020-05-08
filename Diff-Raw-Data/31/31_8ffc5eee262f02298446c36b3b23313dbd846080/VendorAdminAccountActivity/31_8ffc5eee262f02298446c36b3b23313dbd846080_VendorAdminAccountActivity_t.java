 package com.intuitive.yummy.activities;
 
 import com.intuitive.yummy.R;
 import com.intuitive.yummy.models.Vendor;
 import com.intuitive.yummy.models.Vendor.VendorStatus;
 import com.intuitive.yummy.webservices.IntentExtraKeys;
 
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.BitmapFactory;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class VendorAdminAccountActivity extends Activity {
 	private Vendor vendor = new Vendor(1, "Jack's Pizza", "We sell Pizzas!", "123 Main St", new int[][] {{830,1700}, {830,1700}, {830,1700}, {830,1700}, {830,1700}, {0,0}, {0,0}}, VendorStatus.CLOSED, null);
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_vendor_admin_account);
         Intent intent = getIntent();
         if (intent.getStringExtra("Activity").equals("Edit"))
         	vendor = (Vendor)intent.getSerializableExtra("Vendor");
 		
         ImageView vendorPicture = (ImageView) findViewById(R.id.adminVendorPicture);
         if (vendor.getPictureUrl() != null) {
         	vendorPicture.setImageBitmap(BitmapFactory.decodeFile(vendor.getPictureUrl()));
         }
        TextView vendorName = (TextView)findViewById(R.id.adminVendorName); 
 		vendorName.setText(vendor.getName());
 		TextView vendorAddress = (TextView)findViewById(R.id.adminVendorAddress);
 		vendorAddress.setText(vendor.getLocation());
 		TextView vendorStatus = (TextView)findViewById(R.id.adminVendorStatus);
 		if (vendor.getStatus() == VendorStatus.OPEN)
 			vendorStatus.setText("Open");
 		else
			vendorStatus.setText("Closed");
 		
 		if (intent.getBooleanExtra(IntentExtraKeys.PARAMETER, true)){
 			Button genInfo = (Button)findViewById(R.id.button_edit_general_info);
 			genInfo.setVisibility(View.GONE);
 			Button menuEdit = (Button)findViewById(R.id.button_edit_menu);
 			menuEdit.setVisibility(View.GONE);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_vender_admin_account, menu);
 		return true;
 	}
 	
 	public void editGenInfo(View v) {
 			Intent intent = new Intent(this, VendorEditActivity.class);
 			intent.putExtra("Vendor", (Parcelable) vendor);
 			intent.putExtra("VendorID", vendor.getId());
 			startActivity(intent);
 	}
 	
 	public void editMenu(View v) {
 		Intent intent = new Intent(this, VendorEditMenuActivity.class);
 		intent.putExtra("VendorID", vendor.getId());
 		startActivity(intent);
 	}
 	
 	public void viewPendOrder(View v){
 		Intent intent = new Intent(this, PendingOrdersActivity.class);
 		intent.putExtra("VendorID", vendor.getId());
 		startActivity(intent);
 	}
 	
 	public void viewOrderHist(View v){
 		Intent intent = new Intent(this, VendorEditActivity.class); //OrderHistoryActivity.class will be made
 		intent.putExtra("VendorID", vendor.getId());
 		startActivity(intent);
 	}
 	
 	public void setWaitTime(View v){
 		Intent intent = new Intent(this, OrderConfirmationActivity.class);
 		EditText waitTime = (EditText)findViewById(R.id.waitTime);
     	intent.putExtra("waitTime", waitTime.getText());
     	Toast.makeText(getApplicationContext(), "Wait time set to " + waitTime.getText(), Toast.LENGTH_LONG).show();
 	}
 }
